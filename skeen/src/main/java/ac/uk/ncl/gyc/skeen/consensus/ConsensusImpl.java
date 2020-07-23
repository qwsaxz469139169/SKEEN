package ac.uk.ncl.gyc.skeen.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                node.pending.add(key);
                node.acks.put(key,1);
            }else {
                int ackcount = node.acks.get(key);
                ackcount++;
                node.acks.put(key,ackcount);
                System.out.println("second receive message: " + key);
            }

                        //pending

                        List<String> pendings = logEntry.getPendings();
                        List<String> preCommit = new ArrayList<>();
                        for(String mName: pendings){

                            if(node.received.get(mName)==null){
                                System.out.println("First receive message: " + mName);
                                node.received.put(mName, node.logicClock);
                                node.startTime.put(mName, receiveTime);
                                node.pending.add(mName);
                                node.acks.put(mName,1);
                            }else {
                                int ackcount = node.acks.get(mName);
                                ackcount++;
                                node.acks.put(mName,ackcount);
                                System.out.println("second receive message: " + mName);
                            }


                            if(node.acks.get(mName)>=2){
                                preCommit.add(mName);
                                node.acks.remove(mName);
                            }
                        }

                        for(String com : preCommit){
                            LogEntry en = new LogEntry();
                            en.setMessage(com);
                            node.logModule.write(en);
                            //latency
                            long cur_latency = System.currentTimeMillis() - node.startTime.get(com);
                            if(node.latency_temp.get(com)!=null){
                                List<Long> latency_temp = node.latency_temp.get(com);
                                latency_temp.add(cur_latency);
                                node.latency_temp.put(com,latency_temp);
                            }else{
                                List<Long> latency_temp =new CopyOnWriteArrayList<>();
                                latency_temp.add(cur_latency);
                                node.latency_temp.put(com,latency_temp);
                            }

                            if(node.latency_temp.get(com).size()<3){
                                  node.commits.put(com,cur_latency);
                                System.out.println(com+" add latency to own list(pre to broadcast)!");
                            }
                            if(node.latency_temp.get(com).size()==3){
                                long latency = 0;
                                for(long l :node.latency_temp.get(com)){
                                    latency = latency +l ;

                                }
                                latency = latency/3;
                                System.out.println(com+" add final latency wait to response!");

                                node.commit_response.put(com,latency);
                                node.latency_temp.remove(com);
                            }


                            node.received.remove(com);
                            node.startTime.remove(com);
                            System.out.println(com+" has been commit!");
                        }


                        Map<String, Long> commits = logEntry.getCommits();
            System.out.println(commits.size()+" for commits(received)");
                        for(Map.Entry<String, Long> entry : commits.entrySet()){
                            String cl = entry.getKey();
                            long other_la = entry.getValue();
                            if(node.latency_temp.get(cl)!=null){
                                List<Long> latency_temp = node.latency_temp.get(cl);
                                latency_temp.add(other_la);
                                node.latency_temp.put(cl,latency_temp);
                            }else{
                                List<Long> latency_temp =new CopyOnWriteArrayList<>();
                                latency_temp.add(other_la);
                                node.latency_temp.put(cl,latency_temp);
                            }

                            if(node.latency_temp.get(cl).size()==3){
                                long latency = 0;
                                for(long l :node.latency_temp.get(cl)){
                                    latency = latency +l ;
                                }
                                latency = latency/3;
                                System.out.println(cl+" add final latency wait to response222!");

                                node.commit_response.put(cl,latency);
                                node.latency_temp.remove(cl);
                            }

                        }

            Map<String, Long> res_commits = logEntry.getRes_commits();
            for(Map.Entry<String, Long> entry : res_commits.entrySet()){
                String mNmae = entry.getKey();
                            if(node.commit_response.get(mNmae)!=null){
                                node.commit_response.remove(mNmae);
                            }

                            if(node.latency_temp.get(mNmae)!=null){
                                node.latency_temp.remove(mNmae);
                            }

                        }

            result.setLogicClock(node.received.get(key));
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
        if (!lock2.tryLock()) {
            return result;
        }
        try {


            String key = request.getMessage();

            if(node.received.get(key)==null){
                node.received.put(key, node.logicClock);
                node.startTime.put(key, receiveTime);
                node.pending.add(key);
                node.acks.put(key,1);
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
