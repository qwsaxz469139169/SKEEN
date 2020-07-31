package ac.uk.ncl.gyc.skeen.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import ac.uk.ncl.gyc.skeen.StateMachine.StateMachine;
import ac.uk.ncl.gyc.skeen.StateMachine.StateMachineImpl;
import ac.uk.ncl.gyc.skeen.client.GetResponse;
import ac.uk.ncl.gyc.skeen.client.Message;
import ac.uk.ncl.gyc.skeen.consensus.Consensus;
import ac.uk.ncl.gyc.skeen.consensus.ConsensusImpl;
import ac.uk.ncl.gyc.skeen.entity.*;
import ac.uk.ncl.gyc.skeen.logModule.Command;
import ac.uk.ncl.gyc.skeen.logModule.LogEntry;
import ac.uk.ncl.gyc.skeen.logModule.LogModule;
import ac.uk.ncl.gyc.skeen.logModule.LogModuleImpl;
import ac.uk.ncl.gyc.skeen.mysql.DBdriver;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClient;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClientImpl;
import ac.uk.ncl.gyc.skeen.rpc.Request;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.common.profile.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ac.uk.ncl.gyc.skeen.current.SkeenThreadPool;;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcServerImpl;
import ac.uk.ncl.gyc.skeen.rpc.Response;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcServer;
import ac.uk.ncl.gyc.skeen.client.ClientResponse;
import ac.uk.ncl.gyc.skeen.client.ClientRequest;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NodeImpl<T> implements Node<T>, LifeCycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeImpl.class);

 	/* ============ Base ============= */

    public volatile long logicClock = 0;


    public static Map<String,Long> received = new ConcurrentHashMap();

    public static Map<String,Long> startTime = new ConcurrentHashMap();

    public static Map<String,Long> stamped = new ConcurrentHashMap();

    public static Map<String,String> sentAdd = new ConcurrentHashMap();

    public static Map<String,List<Long>> lcMap= new ConcurrentHashMap();


    public static ConcurrentHashMap<String,Integer> ack = new ConcurrentHashMap();

    /* ============ Node ============= */
    public NodesConfigration setting;

    public Nodes nodes;

    /* ============ Rpc ============= */
    public volatile boolean started;

    public static SkeenRpcServer SKEEN_RPC_SERVER;

    public static SkeenRpcClient SKEEN_RPC_CLIENT = new SkeenRpcClientImpl();

    /* ============================== */
    public Consensus consensus;

    public LogModule logModule;

    public StateMachine stateMachine;


    private NodeImpl() {
    }

    public static NodeImpl getInstance() {
        return DefaultNodeLazyHolder.INSTANCE;
    }

    public GetResponse handlerGet(ClientRequest request) {
        GetResponse response = new GetResponse();
        List<String> logEntries = logModule.readAll();

        List<Message> messages = new ArrayList<>();
        int f = 0;
        for(String s: logEntries){
            Message message = new Message();
            int lflag = s.indexOf("latency");

            lflag = lflag+9;
            int mflag = s.indexOf("key");
            mflag = mflag+6;
            String l = "";
            String n = "";
            for(int i =lflag ; i<s.length();i++){
                String ss = String.valueOf(s.charAt(i));
                if(ss.equals(",")){
                    break;
                }
                l = l+ss;
            }

            for(int i =mflag ; i<s.length();i++){
                String ss = String.valueOf(s.charAt(i));
                if(ss.equals("\"")){
                    break;
                }
                n = n+ss;
            }


            message.setMessage(n);
            message.setLatency(l);
            messages.add(message);

        }
        response.setMessages(messages);
        return response;

    }


    private static class DefaultNodeLazyHolder {
        private static final NodeImpl INSTANCE = new NodeImpl();
    }

    @Override
    public void init() throws Throwable {
        if (started) {
            return;
        }
        synchronized (this) {
            if (started) {
                return;
            }
            SKEEN_RPC_SERVER.start();

            consensus = new ConsensusImpl(this);

//            CCSkeenThreadPool.execute(replicationFailQueueConsumer);

            started = true;

            LOGGER.info("start success, Current server id : {} ", nodes.getSelf());
        }
    }

    @Override
    public void setConfig(NodesConfigration config) {
        this.setting = config;

        nodes = Nodes.getInstance();
        logModule = LogModuleImpl.getInstance();
        stateMachine = StateMachineImpl.getInstance();
        
        for (String s : config.getPeerAddrs()) {
            PeerNode peer = new PeerNode(s);
            nodes.addPeer(peer);
            
            if (s.equals("localhost:" + config.getSelfPort())) {
                nodes.setSelf(peer);
            }
        }

        SKEEN_RPC_SERVER = new SkeenRpcServerImpl(config.selfPort, this);
    }


    /**
     * 客户端的每一个请求都包含一条被复制状态机执行的指令。
     * 领导人把这条指令作为一条新的日志条目附加到日志中去，然后并行的发起附加条目 RPCs 给其他的服务器，让他们复制这条日志条目。
     * 当这条日志条目被安全的复制（下面会介绍），领导人会应用这条日志条目到它的状态机中然后把执行的结果返回给客户端。
     * 如果跟随者崩溃或者运行缓慢，再或者网络丢包，
     *  领导人会不断的重复尝试附加日志条目 RPCs （尽管已经回复了客户端）直到所有的跟随者都最终存储了所有的日志条目。
     * @param request
     * @return
     */
    @Override
    public synchronized ClientResponse handlerClientRequest(ClientRequest request,long receiveTime) {

        LOGGER.warn("handlerClientRequest handler {} operation, Key : [{}], Value : [{}].", request.getKey(),request.getValue());

        if (!StringUtil.isEmpty(request.getKey())&&!StringUtil.isEmpty(request.getValue())){
            System.out.println("Current node receive message: " + request.getKey()+", "+request.getValue());
        }else{
            return ClientResponse.fail();
        }

        //receive event lc +1
        logicClock++;

        long ts = logicClock;
        received.put(request.getKey(), ts);
        sentAdd.put(request.getKey(),nodes.getSelf().getAddress());
        startTime.put(request.getKey(),receiveTime);

        ack.put(request.getKey(),0);

        List<Long> lcList = new CopyOnWriteArrayList<>();
        lcList.add(logicClock);
        lcMap.put(request.getKey(),lcList);




        // 预提交到本地日志, TODO 预提交
        LogEntry logEntry = new LogEntry();
        logEntry.setInitialNode(nodes.getSelf().getAddress());
        logEntry.setMessage(request.getKey());
        logEntry.setLogic_clock(logicClock);
        logEntry.setStartTime(receiveTime);
        logEntry.setSentAdd(nodes.getSelf().getAddress());
        logEntry.setCommand(Command.newBuilder().
                key(request.getKey()).
                value(request.getValue()).
                build());


//        logModule.write(logEntry);
//        System.out.println("Current precommit to log module.");

        System.out.println("Start send logic time of the message to other node.");

//        int count = 0;

        //  复制到其他机器
        //send event lc+
        final AtomicInteger success = new AtomicInteger(0);

        List<Future<Boolean>> futureList = new CopyOnWriteArrayList<>();

        int count = 0;

        for (PeerNode peer : nodes.getPeersWithOutSelf()) {
           // TODO check self and CCSkeenThreadPool
            count++;
            // 并行发起 RPC 复制
            futureList.add(sendLC(peer, logEntry));
        }
        CountDownLatch latch = new CountDownLatch(futureList.size());
        List<Boolean> resultList = new CopyOnWriteArrayList<>();

        getRPCSendLCResult(futureList, latch, resultList);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Boolean aBoolean : resultList) {
            if (aBoolean) {
                success.incrementAndGet();
            }
        }

        //  响应客户端(成功一半)
        if (success.get()==count) {
            System.out.println("Receive success! Response Client.");

//            List<Long> maxList = lcMap.get(request.getKey());
//            long snM = 0;
//            for (int i = 0; i<maxList.size();i++){
//                if(maxList.get(i)>snM){
//                    snM = maxList.get(i);
//                }
//            }
//            stamped.put(request.getKey(),snM);
//            received.remove(request.getKey());
//            lcMap.remove(request.getKey());
//
//            logModule.write(logEntry);
//            stamped.remove(request.getKey());
//
//            long _latency = System.currentTimeMillis()- logEntry.getStartTime();
//            System.out.println("latency la: ."+_latency);
//            for(long la:latency_temp.get(request.getKey())){
//                System.out.println("latency la: ."+la);
//                _latency=_latency+la;
//            }
//
//            _latency= _latency/3;
//
//            logicClock++;
            ClientResponse clientResponse = new ClientResponse(true);
//            clientResponse.setExtraMessage(extraM.get(request.getKey()).get());
//            extraM.remove(request.getKey());
//            clientResponse.setLatency(_latency);
//            latency_temp.remove(request.getKey());

            // 返回成功.
            return clientResponse;
        } else {

            return ClientResponse.fail();
        }
    }

    private void getRPCSendLCResult(List<Future<Boolean>> futureList, CountDownLatch latch, List<Boolean> resultList) {
        for (Future<Boolean> future : futureList) {
            SkeenThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        resultList.add(future.get(3000, MILLISECONDS));
                    } catch (CancellationException | TimeoutException | ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        resultList.add(false);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

    }

    public Future<Boolean> sendLC(PeerNode peer, LogEntry logEntry) {
        return SkeenThreadPool.submit(new Callable() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis(), end = start;

                    LcSendRequest lcRequest = new LcSendRequest();
                    lcRequest.setServerId(nodes.getSelf().getAddress());
                    lcRequest.setLogEntry(logEntry);
                    lcRequest.setTs(received.get(logEntry.getMessage()));
                    lcRequest.setMessage(logEntry.getMessage());


                    Request request = new Request();
                    request.setCmd(Request.REQ_SEND_LC);
                    request.setObj(lcRequest);
                    request.setUrl(peer.getAddress());

                    try {
                        Response response = SKEEN_RPC_CLIENT.send(request);


                        System.out.println("Current node send ack to other node : "+peer.getAddress());

                        if (response == null) {
                            LOGGER.info("send to "+peer.getAddress()+" fail");
                            return false;
                        }

                        LcSendResponse result = (LcSendResponse) response.getResult();
                        if (result != null && result.isSuccess()) {
                            LOGGER.info("send to "+peer.getAddress()+" successful");


                            return true;
                        }

                        end = System.currentTimeMillis();

                    } catch (Exception e) {


                    }



                return false;

            }
        });
    }

    @Override
    public LcSendResponse handlerSendLcRequest(LcSendRequest request) {
        LOGGER.warn("handlerRequestVote will be invoke, request info : {}", request);
        return consensus.sendLogicTime(request);
    }

    public InitialTaskResponse handlerInitialTask(InitialTaskRequest request) {
        return consensus.InitialTask(request);
    }


    @Override
    public void destroy() throws Throwable {
        SKEEN_RPC_SERVER.stop();
    }


}
