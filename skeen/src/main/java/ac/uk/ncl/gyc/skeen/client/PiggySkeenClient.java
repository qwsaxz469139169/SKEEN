package ac.uk.ncl.gyc.skeen.client;

import ac.uk.ncl.gyc.skeen.current.SkeenThreadPool;
import ac.uk.ncl.gyc.skeen.rpc.Request;
import ac.uk.ncl.gyc.skeen.rpc.Response;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClient;
import ac.uk.ncl.gyc.skeen.rpc.SkeenRpcClientImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.util.concurrent.atomic.AtomicLong;

public class PiggySkeenClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PiggySkeenClient.class);

    private final static SkeenRpcClient client = new SkeenRpcClientImpl();

    static String req_address = "";

    private static List<Message> messages = new ArrayList<Message>();

    //static List<String> nodeList = Lists.newArrayList("localhost:8775", "localhost:8776", "localhost:8777");
    static List<String> nodeList = Lists.newArrayList("100.70.49.128:8775", "100.70.49.85:8776", "100.70.48.5:8777");

    public static void main(String[] args) throws RemotingException, InterruptedException {
        main0();

    }

    public static void main0() throws InterruptedException {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        AtomicLong count = new AtomicLong(3);

        int message = 0;
        for (int j = 0; j < 605; j++) {
            for (int i = 0; i < 15; i++) {
                message = message + 1;
                int m = message;
                int index = (int) (count.incrementAndGet() % nodeList.size());
                String req_address = nodeList.get(index);
                SkeenThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        String key = "client1:" + m;
                        ClientRequest obj = ClientRequest.newBuilder().key("client1:" + m).value("world:").type(ClientRequest.PUT).build();

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
                                jsonArray.add(message1);
                            } else {
                                System.out.println("message: " + obj.key + " has been sent");
                            }
//                            JSONObject mes=new JSONObject();
//                            mes.put("name",key);
//                            mes.put("latency", response.getResult().getLatency());
//                            mes.put("extraM", response.getResult().getExtraMessage());
//                            jsonArray.add(mes);
//                            JSONArray array= JSONArray.parseArray(JSON.toJSONString(mess));

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
        File f = new File("D:/_case2Skeen1.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            fw = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(s, 0, s.length() - 1);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("end");
    }


}
