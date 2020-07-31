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
import ac.uk.ncl.gyc.skeen.logModule.Command;
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
                node.sentAdd.put(key,logEntry.getSentAdd());
                node.ack.put(key,1);

                List<Long> lcList = new CopyOnWriteArrayList<>();
                lcList.add(node.logicClock);
                lcList.add(lcSendRequest.getTs());
                node.lcMap.put(key, lcList);

                node.pending.add(key);
            }else {
                int ack_count = node.ack.get(key);
                ack_count = ack_count+1;
                node.ack.put(key,ack_count);
                System.out.println("second receive message: " + key);

                List<Long> lcList = node.lcMap.get(key);
                lcList.add(lcSendRequest.getTs());
                node.lcMap.put(key, lcList);
            }

            for(String pendingName : lcSendRequest.getLogEntry().getPendings()){
                if(node.received.get(pendingName)==null){
                    System.out.println("First receive message: " + pendingName);
                    node.received.put(pendingName, node.logicClock);
                    node.startTime.put(pendingName, receiveTime);
                    node.sentAdd.put(pendingName,logEntry.getSentAdd());
                    node.ack.put(pendingName,1);

                    List<Long> lcList = new CopyOnWriteArrayList<>();
                    lcList.add(node.logicClock);
                    lcList.add(lcSendRequest.getTs());
                    node.lcMap.put(pendingName, lcList);

                    node.pending.add(pendingName);
                }else {
                    int ack_count = node.ack.get(pendingName);
                    ack_count = ack_count+1;
                    node.ack.put(pendingName,ack_count);
                    System.out.println("second receive message: " + pendingName);

                    List<Long> lcList = node.lcMap.get(pendingName);
                    lcList.add(lcSendRequest.getTs());
                    node.lcMap.put(pendingName, lcList);
                }

                if(node.ack.get(pendingName)>=2){
                    System.out.println(pendingName+ " have been commited!");
                    long latency = System.currentTimeMillis() - node.startTime.get(pendingName);
                    if(node.sentAdd.get(pendingName).equals(node.nodes.getSelf().getAddress())){
                        LogEntry le = new LogEntry();
                        le.setLatency(latency);
                        le.setMessage(pendingName);
                        le.setCommand(Command.newBuilder().value("").key(pendingName).build());
                        node.logModule.write(le);
                    }
                    node.received.remove(pendingName);
                    node.startTime.remove(pendingName);
                    node.sentAdd.remove(pendingName);
                    node.ack.remove(pendingName);
                    node.lcMap.remove(pendingName);
                }
            }


            result.setSuccess(true);
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

        lock2.lock();
        try {


            String key = request.getMessage();

            if(node.received.get(key)==null){
                System.out.println("First receive message: " + key);
                node.received.put(key, node.logicClock);
                node.startTime.put(key, receiveTime);
                node.sentAdd.put(key,request.getLogEntry().getSentAdd());
                node.ack.put(key,1);

                List<Long> lcList = new CopyOnWriteArrayList<>();
                lcList.add(node.logicClock);
                lcList.add(request.getTs());
                node.lcMap.put(key, lcList);
            }else {
                int ack_count = node.ack.get(key);
                ack_count = ack_count+1;
                node.ack.put(key,ack_count);
                System.out.println("second receive message: " + key);

                List<Long> lcList = node.lcMap.get(key);
                lcList.add(request.getTs());
                node.lcMap.put(key, lcList);
            }

            if(node.ack.get(key)>=2){
                System.out.println(key+ " have been commited!");
                long latency = System.currentTimeMillis() - node.startTime.get(key);
                if(node.sentAdd.get(key).equals(node.nodes.getSelf().getAddress())){
                    LogEntry logEntry = request.getLogEntry();
                    logEntry.setLatency(latency);
                    node.logModule.write(logEntry);
                }
                node.received.remove(key);
                node.startTime.remove(key);
                node.sentAdd.remove(key);
                node.ack.remove(key);
                node.lcMap.remove(key);
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
