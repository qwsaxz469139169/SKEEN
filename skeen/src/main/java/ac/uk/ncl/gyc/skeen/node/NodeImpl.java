package ac.uk.ncl.gyc.skeen.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import ac.uk.ncl.gyc.skeen.StateMachine.StateMachine;
import ac.uk.ncl.gyc.skeen.StateMachine.StateMachineImpl;
import ac.uk.ncl.gyc.skeen.consensus.Consensus;
import ac.uk.ncl.gyc.skeen.consensus.ConsensusImpl;
import ac.uk.ncl.gyc.skeen.entity.*;
import ac.uk.ncl.gyc.skeen.logModule.LogEntry;
import ac.uk.ncl.gyc.skeen.logModule.LogModule;
import ac.uk.ncl.gyc.skeen.logModule.LogModuleImpl;
import ac.uk.ncl.gyc.skeen.mysql.DBdriver;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClient;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClientImpl;
import ac.uk.ncl.gyc.skeen.rpc.Request;
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

    public volatile long cur_index = 0;

    public static long SYSTEM_START_TIME = System.currentTimeMillis();

    public static AtomicLong LAXT_INDEX = new AtomicLong(0);

//    public static Map<String,AtomicInteger> extraM = new ConcurrentHashMap();

    public static Map<String,Long> received = new ConcurrentHashMap();

    public static Map<String,Long> startTime = new ConcurrentHashMap();

    public static Map<String,Long> stamped = new ConcurrentHashMap();

    public static Map<Long,CopyOnWriteArrayList<PiggybackingLog>> REQUEST_LIST= new ConcurrentHashMap();

    public static Map<String,Long> latency = new ConcurrentHashMap();

    public static Map<String,List<Long>> lcMap= new ConcurrentHashMap();

    public static Map<String,List<Long>> latency_temp = new ConcurrentHashMap();


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

    public ReentrantLock lock = new ReentrantLock();


    private NodeImpl() {
    }

    public static NodeImpl getInstance() {
        return DefaultNodeLazyHolder.INSTANCE;
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

//            SkeenThreadPool.execute(replicationFailQueueConsumer);

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
    public synchronized ClientResponse handlerClientRequest(ClientRequest request) {

        LOGGER.warn("handlerClientRequest handler {} operation, Key : [{}], Value : [{}].", request.getKey(),request.getValue());

        if (!StringUtil.isEmpty(request.getKey())&&!StringUtil.isEmpty(request.getValue())){
            System.out.println("Current node receive message: " + request.getKey()+", "+request.getValue());
        }else{
            return ClientResponse.fail();
        }

        //receive event lc +1
        logicClock++;

        PiggybackingLog piggybackingLog = new PiggybackingLog();
        piggybackingLog.setMessage(request.getKey());
        piggybackingLog.setStartTime(System.currentTimeMillis());


        long RUN_TIME = System.currentTimeMillis() - SYSTEM_START_TIME;
        long req_index = RUN_TIME / 2;

        System.out.println("cur_time" + req_index );


        CopyOnWriteArrayList<PiggybackingLog> req_list = null;
        PiggybackingLog firstPiggy = null;
//lock.lock();
//try{
    if(REQUEST_LIST.get(req_index)!=null){
        System.out.println("cur_req" + request.getKey()+" is  not first p" );
        req_list = REQUEST_LIST.get(req_index);
        req_list.add(piggybackingLog);
        REQUEST_LIST.put(req_index,req_list);
    }else{
        System.out.println("cur_req" + request.getKey()+" is first p          req_index"+ req_index );
        piggybackingLog.setFirstIndex(true);
        req_list = new CopyOnWriteArrayList();
        req_list.add(piggybackingLog);
        REQUEST_LIST.put(req_index,req_list);
    }

    System.out.println("last_index   " + LAXT_INDEX.get() );
//        System.out.println("last_index   " + cur_index );



    if(req_index<=LAXT_INDEX.get()){
        System.out.println("return 1111111 size" );
        return ClientResponse.ok();
    }

    long last_index = LAXT_INDEX.get();

    LAXT_INDEX.getAndSet(req_index);
    if(last_index == 0){
        System.out.println("return 3333333 size" );

//        cur_index = req_index;
        return ClientResponse.ok();
    }

    if(REQUEST_LIST.get(last_index)==null){
        System.out.println("return 2222222 size" );
        return ClientResponse.ok();
    }else{
        req_list = REQUEST_LIST.get(last_index);
    }

    REQUEST_LIST.remove(last_index);

    System.out.println("req_list size" + req_list.size());
    for(PiggybackingLog p : req_list){
        if(p.isFirstIndex()){
            firstPiggy = p;
        }
    }

    System.out.println("FIST PPP " + firstPiggy.getMessage());

    long ts = logicClock;
    received.put(firstPiggy.getMessage(), ts);
//        extraM.put(request.getKey(),new AtomicInteger(0));
    latency_temp.put(firstPiggy.getMessage(),new CopyOnWriteArrayList<>());
//}finally {
//   lock.unlock();
//}


        List<Long> lcList = new CopyOnWriteArrayList<>();
        lcList.add(logicClock);
        lcMap.put(firstPiggy.getMessage(),lcList);

        List<LogEntry> logEntries = new ArrayList<>();
        for(PiggybackingLog p : req_list){
            LogEntry logEntry = new LogEntry();
            logEntry.setInitialNode(nodes.getSelf().getAddress());
            logEntry.setMessage(p.getMessage());
            logEntry.setLogic_clock(logicClock);

            if(p.isFirstIndex()){
                logEntry.setFirst_index(true);
            }

            logEntries.add(logEntry);
        }


        // 预提交到本地日志, TODO 预提交



//        logModule.write(logEntry);
//        System.out.println("Current precommit to log module.");

        System.out.println(firstPiggy.getMessage()+"---Start send logic time of the message to other node.");

//        int count = 0;

        //  复制到其他机器
        //send event lc+
        final AtomicInteger success = new AtomicInteger(0);

        List<Future<Boolean>> futureList = new CopyOnWriteArrayList<>();

        int count = 0;

        for (PeerNode peer : nodes.getPeersWithOutSelf()) {
           // TODO check self and SkeenThreadPool
            count++;
            // 并行发起 RPC 复制
            futureList.add(sendLC(peer, logEntries));
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
            System.out.println(firstPiggy.getMessage()+"---Receive success! Response Client.");

            List<Long> maxList = lcMap.get(firstPiggy.getMessage());
            long snM = 0;
            for (int i = 0; i<maxList.size();i++){
                if(maxList.get(i)>snM){
                    snM = maxList.get(i);
                }
            }
            stamped.put(firstPiggy.getMessage(),snM);
            received.remove(firstPiggy.getMessage());
            lcMap.remove(firstPiggy.getMessage());

            List<String> requests = new ArrayList<>();
            for(LogEntry l : logEntries){
                logModule.write(l);
                requests.add(l.getMessage());
            }

            stamped.remove(firstPiggy.getMessage());

            long _latency = System.currentTimeMillis()- firstPiggy.getStartTime();

            for(long la:latency_temp.get(firstPiggy.getMessage())){

                _latency=_latency+la;
            }

            _latency= _latency/3;
            System.out.println(firstPiggy.getMessage()+"---latency la: ."+_latency);
            logicClock++;
            ClientResponse clientResponse = new ClientResponse(true);
            clientResponse.setExtraMessage(6);
//            extraM.remove(firstPiggy.getMessage());
            clientResponse.setLatency(_latency);
            clientResponse.setRequests(requests);
            latency_temp.remove(firstPiggy.getMessage());
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

    public Future<Boolean> sendLC(PeerNode peer, List<LogEntry> LogEntries) {
        return SkeenThreadPool.submit(new Callable() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis(), end = start;

                // 20 秒重试时间
//                while (end - start < 20 * 1000L) {
                    LogEntry firstLog = null;
                    for(LogEntry l : LogEntries ){
                        if(l.isFirst_index()){
                            firstLog = l;
                        }
                    }
                    LcSendRequest lcRequest = new LcSendRequest();
                    lcRequest.setServerId(nodes.getSelf().getAddress());
                    lcRequest.setLogEntries(LogEntries);
                    lcRequest.setTs(received.get(firstLog.getMessage()));
                    lcRequest.setMessage(firstLog.getMessage());


                    Request request = new Request();
                    request.setCmd(Request.REQ_SEND_LC);
                    request.setObj(lcRequest);
                    request.setUrl(peer.getAddress());

                    try {
                        Response response = SKEEN_RPC_CLIENT.send(request);

//                        AtomicInteger em = extraM.get(logEntry.getMessage());
//                        em.incrementAndGet();
//                        extraM.put(logEntry.getMessage(),em);

                        System.out.println(firstLog.getMessage()+"---Current node send ack to other node : "+peer.getAddress());

                        if (response == null) {
                            LOGGER.info(firstLog.getMessage()+"---send to "+peer.getAddress()+" fail");
                            return false;
                        }

                        LcSendResponse result = (LcSendResponse) response.getResult();
                        if (result != null && result.isSuccess()) {
                            LOGGER.info(firstLog.getMessage()+"---send to "+peer.getAddress()+" successful");

//                            receive event lc = max+1
                            if(result.getLogicClock()>logicClock){
                                logicClock = result.getLogicClock()+1;
                            }else {
                                logicClock = logicClock+1;
                            }

//                            System.out.println(logEntry.getMessage()+" "+result.getLatency());
                            List<Long> lcList = lcMap.get(firstLog.getMessage());
                            lcList.add(result.getLogicClock());
                            lcMap.put(firstLog.getMessage(),lcList);

                            List<Long> laList = latency_temp.get(firstLog.getMessage());
                            laList.add(result.getLatency());
                            latency_temp.put(firstLog.getMessage(),laList);
//
//                            AtomicInteger e =  extraM.get(firstLog.getMessage());
//                            e.addAndGet(result.getExtraM());
//                            extraM.put(firstLog.getMessage(),e);


                            return true;
                        }

                        end = System.currentTimeMillis();

                    } catch (Exception e) {
e.printStackTrace();

                    }



                return false;

            }
        });
    }

    @Override
    public LcSendResponse handlerSendLcRequest(LcSendRequest request) throws InterruptedException {
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
