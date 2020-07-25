package ac.uk.ncl.gyc.skeen.client;

import ac.uk.ncl.gyc.skeen.clientCur.CCSkeenThreadPool;
import ac.uk.ncl.gyc.skeen.rpc.Request;
import ac.uk.ncl.gyc.skeen.rpc.Response;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClient;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClientImpl;
import com.alibaba.fastjson.JSON;
import com.alipay.remoting.exception.RemotingException;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SkeenClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkeenClient.class);

    private final static SkeenRpcClient client = new SkeenRpcClientImpl();

    static String req_address = "";

    private static List<Message> messages = new ArrayList<Message>();
    private static  AtomicLong count = new AtomicLong(3);
    private static  AtomicLong receiveCount = new AtomicLong(0);
    private static AtomicInteger m_index = new AtomicInteger(0);

    private static final int clientNum = 8;
    private static final int runtime= 620;
    private static final int c = 2;
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

//                            String[] mess = {};
//                            List<ReMes> m1 = new ArrayList<>();
            if (response.getResult().getLatency() != null) {
                System.out.println("message: " + obj.key + " has been sent");

                int con = 0;
                for (String mmmmm : response.getResult().getRequests()) {
//                                    ReMes reMes = new ReMes(mmmmm);
//                                    m1.add(reMes);
                    con++;
                    System.out.println("message: " + mmmmm + ", latency: " + response.getResult().getLatency());
                }

                Message message1 = new Message(con, 6, response.getResult().getLatency());

                messages.add(message1);

            } else {
                System.out.println("message: " + obj.key + " has been sent");
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
