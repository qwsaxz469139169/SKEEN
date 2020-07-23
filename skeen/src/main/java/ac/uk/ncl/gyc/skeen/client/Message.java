package ac.uk.ncl.gyc.skeen.client;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by GYC on 2020/7/5.
 */
public class Message {

    @JSONField(name = "message")
    private int messages;

    @JSONField(name = "extra_message")
    private int extra_message;

    @JSONField(name = "latency")
    private long latency;

    public Message(int messages, int extra_message, long latency) {
        this.messages = messages;
        this.extra_message = extra_message;
        this.latency = latency;
    }

    public int getMessages() {
        return messages;
    }

    public void setMessages(int messages) {
        this.messages = messages;
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

    @Override
    public String toString() {
        return "Message{" +
                "messages=" + messages +
                ", extra_message=" + extra_message +
                ", latency=" + latency +
                '}';
    }
}
