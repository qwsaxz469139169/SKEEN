package ac.uk.ncl.gyc.skeen.client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ac.uk.ncl.gyc.skeen.clientCur.CCSkeenThreadPool;
import ac.uk.ncl.gyc.skeen.current.SkeenThreadPool;
import ac.uk.ncl.gyc.skeen.exception.SkeenRemotingException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.remoting.exception.RemotingException;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClientImpl;
import ac.uk.ncl.gyc.skeen.rpc.Request;
import ac.uk.ncl.gyc.skeen.rpc.Response;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClient;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SkeenClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkeenClient.class);

    private final static SkeenRpcClient client = new SkeenRpcClientImpl();

    static String req_address = "";

    private static List<Message> messages = new ArrayList<Message>();
    private static  AtomicLong count = new AtomicLong(3);
    private static  AtomicLong receiveCount = new AtomicLong(0);
    private static AtomicInteger m_index = new AtomicInteger(0);

    private static final int clientNum = 3;
    private static final int runtime= 620;
    private static final int c = 1;
    private static final int delay= 13;
    private static final int endcount= 3000;
    private static final int arriveRate = 30;
    private static final String arriveRateNum = "0.75";

//    static List<String> nodeList = Lists.newArrayList("localhost:8775", "localhost:8776", "localhost:8777");
static List<String> nodeList = Lists.newArrayList("100.70.49.128:8775", "100.70.49.85:8776", "100.70.49.226:8777");
    public static void main(String[] args) throws RemotingException, InterruptedException {
       main0();

    }

    public static void main0() throws InterruptedException {

        MyTask myTask = new MyTask();
        CCSkeenThreadPool.scheduleWithFixedDelay(myTask,delay);

        while(true){
    if(m_index.get()>endcount){
        String s = JSON.toJSONString(messages);
        FileWriter fw = null;
        File f = new File("D:/"+arriveRateNum+"_case"+c+"Skeen"+clientNum+".txt");
        try {
            if(!f.exists()){
                f.createNewFile();
            }
            fw = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(s, 0, s.length()-1);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("end");
        System.exit(0);
    }

}
}



static class MyTask implements Runnable{

    @Override
    public void run() {
        int m = m_index.addAndGet(1);
        int index = (int) (count.incrementAndGet() % nodeList.size());
        String req_address = nodeList.get(index);
        ClientRequest obj = ClientRequest.newBuilder().key("client"+clientNum+":"+m).value("world:").type(ClientRequest.PUT).build();

        Request<ClientRequest> r = new Request<>();
        r.setObj(obj);
        r.setUrl(req_address);
        r.setCmd(Request.REQ_CLIENT);

        Response<ClientResponse> response;

        try {
            response = client.send(r);
            ClientResponse clientResponse = response.getResult();
            if(response.getResult().getLatency()!=null){
                System.out.println("message: " + obj.key + ", latency: " + response.getResult().getLatency() + ", extraM: " + response.getResult().getExtraMessage());

                Message message1 = new Message(obj.key, 6, response.getResult().getLatency());
                messages.add(message1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String s = JSON.toJSONString(messages);
            FileWriter fw = null;
            File f = new File("D:/"+arriveRateNum+"_case"+c+"Skeen"+clientNum+".txt");
            try {
                if(!f.exists()){
                    f.createNewFile();
                }
                fw = new FileWriter(f);
                BufferedWriter out = new BufferedWriter(fw);
                out.write(s, 0, s.length()-1);
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("end");
            System.exit(0);
        }
    }
}


}
