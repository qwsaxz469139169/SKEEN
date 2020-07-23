package ac.uk.ncl.gyc.skeen.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import ac.uk.ncl.gyc.skeen.entity.*;
import ac.uk.ncl.gyc.skeen.logModule.LogEntry;
import ac.uk.ncl.gyc.skeen.node.NodeImpl;
import ac.uk.ncl.gyc.skeen.node.PeerNode;
import ac.uk.ncl.gyc.skeen.rpc.Request;
import ac.uk.ncl.gyc.skeen.rpc.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsensusImpl implements Consensus {


    private static final Logger LOGGER = LoggerFactory.getLogger(ConsensusImpl.class);


    public final NodeImpl node;

    public final ReentrantLock lock = new ReentrantLock();
    Condition condition =lock.newCondition();
    Condition condition2 =lock.newCondition();

    public final ReentrantLock lock2 = new ReentrantLock();

    public ConsensusImpl(NodeImpl node) {
        this.node = node;
    }

    @Override
    public LcSendResponse sendLogicTime(LcSendRequest lcSendRequest) {
        LcSendResponse result = new LcSendResponse();
        result.setSuccess(false);
        long receiveTime = System.currentTimeMillis();

        lock.lock();

        try {
            System.out.println("Receive Logic time: " + lcSendRequest);

            long ts = lcSendRequest.getTs();
            LogEntry logEntry = lcSendRequest.getLogEntry();
            String key = logEntry.getMessage();

            node.logicClock = node.logicClock + 1;

            if (ts > node.logicClock) {
                node.logicClock = ts;
            }

            if(node.received.get(key)==null){
                System.out.println("First receive message: " + key);
                node.received.put(key, node.logicClock);
                node.startTime.put(key, receiveTime);
            }else {
                System.out.println("second receive message: " + key);
            }

            if(node.extraM.get(key)==null){
                node.extraM.put(key, new AtomicInteger(0));
            }


            if(node.lcMap.get(key)==null){
                List<Long> lcList = new CopyOnWriteArrayList<>();
                lcList.add(node.logicClock);
                lcList.add(lcSendRequest.getTs());
                node.lcMap.put(key, lcList);
            }else{
                List<Long> lcList = node.lcMap.get(key);
                lcList.add(lcSendRequest.getTs());
                node.lcMap.put(key, lcList);
            }


                node.received.put(key, node.logicClock);
                node.extraM.put(key, new AtomicInteger(0));

                List<Long> lcList = new ArrayList<>();
                lcList.add(node.logicClock);
                lcList.add(logEntry.getLogic_clock());
                node.lcMap.put(key, lcList);

                //如果是初始节点发过来的消息，需要请求其他节点的承认
                System.out.println("初始节点： "+logEntry.getInitialNode());
                System.out.println("lcSendRequest.getServerId()： "+lcSendRequest.getServerId());


                    String add = "";
                    for (PeerNode peer : node.nodes.getPeersWithOutSelf()) {
                        // TODO check self and SkeenThreadPool
                        // 并行发起 RPC 复制
                        if (peer.getAddress() != lcSendRequest.getServerId()) {
                            add = peer.getAddress();
                        }

                    }

                    InitialTaskRequest initialTaskRequest = new InitialTaskRequest();
                    initialTaskRequest.setServerId(node.nodes.getSelf().getAddress());
                    initialTaskRequest.setLogEntry(logEntry);
                    initialTaskRequest.setTs(node.received.get(logEntry.getMessage()));
                    initialTaskRequest.setMessage(key);


                    Request request = new Request();
                    request.setCmd(Request.REQ_INI_TASK);
                    request.setObj(initialTaskRequest);
                    request.setUrl(add);
//


//                    LcSendRequest lcRequest = new LcSendRequest();
//                    lcRequest.setServerId(node.nodes.getSelf().getAddress());
//                    lcRequest.setLogEntry(logEntry);
//                    lcRequest.setTs(node.received.get(logEntry.getMessage()));
//                    lcRequest.setMessage(logEntry.getMessage());
//
//
//                    Request request = new Request();
//                    request.setCmd(Request.REQ_SEND_LC);
//                    request.setObj(lcRequest);
//                    request.setUrl(add);

                    AtomicInteger em = node.extraM.get(logEntry.getMessage());
            em.incrementAndGet();
                    node.extraM.put(logEntry.getMessage(), em);

                    Response response = node.SKEEN_RPC_CLIENT.send(request);


                    System.out.println("Current node send ack to other node : " + add);


                InitialTaskResponse lcResponse = (InitialTaskResponse) response.getResult();
                    if (lcResponse != null && lcResponse.isSuccess()) {
                        LOGGER.info("send to " + add + " successful");

//                            receive event lc = max+1
                        if (lcResponse.getLogicClock() > node.logicClock) {
                            node.logicClock = lcResponse.getLogicClock() + 1;
                        } else {
                            node.logicClock = node.logicClock + 1;
                        }


                        List<Long> lcList2 = node.lcMap.get(key);
                        lcList2.add(lcResponse.getLogicClock());
                        node.lcMap.put(key, lcList2);

                        List<Long> maxList = node.lcMap.get(key);
                        long snM = 0;
                        for (int i = 0; i<maxList.size();i++){
                            if(maxList.get(i)>snM){
                                snM = maxList.get(i);
                            }
                        }

                        node.stamped.put(key,snM);

                        node.lcMap.remove(key);

                        node.logModule.write(logEntry);



                        AtomicInteger e = node.extraM.get(logEntry.getMessage());
                        e.incrementAndGet();
                        node.extraM.put(logEntry.getMessage(), e );

                        long latency = System.currentTimeMillis()- node.startTime.get(key);
                        System.out.println("Commit success, latency: " + latency+", extra message: "+node.extraM.get(logEntry.getMessage()));
                        result.setLogicClock(node.received.get(key));
                        result.setExtraM(node.extraM.get(key).get());
                        result.setLatency(latency);
                        node.received.remove(key);
                        node.startTime.remove(key);
                        node.extraM.remove(key);
                        node.stamped.remove(key);

                        result.setSuccess(true);

                    }

            return result;
        }finally {
            lock.unlock();
        }
    }


    @Override
    public InitialTaskResponse InitialTask(InitialTaskRequest request) {
        long receiveTime = System.currentTimeMillis();
        InitialTaskResponse result = new InitialTaskResponse();
        result.setSuccess(false);
        if (!lock2.tryLock()) {
            return result;
        }
        try {


            String key = request.getMessage();

            if(node.received.get(key)==null){
                node.received.put(key, node.logicClock);
                node.startTime.put(key, receiveTime);
            }

            if(node.extraM.get(key)==null){
                node.extraM.put(key, new AtomicInteger());
            }


            if(node.lcMap.get(key)==null){
                List<Long> lcList = new CopyOnWriteArrayList<>();
                lcList.add(node.logicClock);
                lcList.add(request.getTs());
                node.lcMap.put(key, lcList);
            }else{
                List<Long> lcList = node.lcMap.get(key);
                lcList.add(request.getTs());
                node.lcMap.put(key, lcList);
            }

            System.out.println("receive ack: "+key+"     "+request.getServerId());
            result.setSuccess(true);

        }finally {
lock2.unlock();
        }
        return result;
    }


    public NodeImpl getNode() {
        return node;
    }
}
