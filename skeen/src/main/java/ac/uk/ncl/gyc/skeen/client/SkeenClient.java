package ac.uk.ncl.gyc.skeen.client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

//    static List<String> nodeList = Lists.newArrayList("localhost:8775", "localhost:8776", "localhost:8777");
static List<String> nodeList = Lists.newArrayList("100.70.49.128:8775", "100.70.49.85:8776", "100.70.48.5:8777");
    public static void main(String[] args) throws RemotingException, InterruptedException {
       main0();

    }

    public static void main0() throws InterruptedException {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        AtomicLong count = new AtomicLong(3);

        int message = 0;
        for(int j =0; j<1200; j++){
            for(int i=0;i<5;i++){
                message = message+1;
                int m = message;
                int index = (int) (count.incrementAndGet() % nodeList.size());
                String req_address = nodeList.get(index);
                SkeenThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        String key = "client1:"+m;
                        ClientRequest obj = ClientRequest.newBuilder().key("client1:"+m).value("world:").type(ClientRequest.PUT).build();

                        Request<ClientRequest> r = new Request<>();
                        r.setObj(obj);
                        r.setUrl(req_address);
                        r.setCmd(Request.REQ_CLIENT);

                        Response<ClientResponse> response;

                        try {
                            response = client.send(r);
                            ClientResponse clientResponse = response.getResult();
                            System.out.println("message: " + obj.key + ", latency: " + response.getResult().getLatency() + ", extraM: " + response.getResult().getExtraMessage());

                            Message message1 = new Message(obj.key, 6, response.getResult().getLatency());
                            messages.add(message1);
                        } catch (Exception e) {

                        }
                    }
                });
            }
            Thread.sleep(1000);
        }
        Thread.sleep(20000);
        String s = JSON.toJSONString(messages);
        FileWriter fw = null;
        File f = new File("D:/_case1Skeen1.txt");
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
       }





}
