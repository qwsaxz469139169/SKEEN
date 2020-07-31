package ac.uk.ncl.gyc.skeen.client;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * Created by GYC on 2020/7/5.
 */
public class Message implements Serializable{

    @JSONField(name = "message")
    private String message;


    @JSONField(name = "latency")
    private String latency;

    public Message() {

    }

    public Message(String message,  String latency) {
        this.message = message;

        this.latency = latency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getLatency() {
        return latency;
    }

    public void setLatency(String latency) {
        this.latency = latency;
    }
}
