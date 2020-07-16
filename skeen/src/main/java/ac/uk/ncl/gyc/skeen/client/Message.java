package ac.uk.ncl.gyc.skeen.client;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by GYC on 2020/7/5.
 */
public class Message {

    @JSONField(name = "message")
    private String message;

    @JSONField(name = "extra_message")
    private int extra_message;

    @JSONField(name = "latency")
    private long latency;

    public Message(String message, int extra_message, long latency) {
        this.message = message;
        this.extra_message = extra_message;
        this.latency = latency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getExtra_message() {
        return extra_message;
    }

    public void setExtra_message(int extra_message) {
        this.extra_message = extra_message;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }
}
