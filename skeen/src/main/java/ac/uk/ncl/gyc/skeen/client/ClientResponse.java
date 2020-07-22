package ac.uk.ncl.gyc.skeen.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ClientResponse implements Serializable {

    Object result;

    boolean success;

    int extraMessage;

    Map<String,Long> messages ;

    Long Latency;

    @Override
    public String toString() {
        return "ClientResponse{" +
                "result=" + result +
                "success="+success+
                '}';
    }

    public Map<String, Long> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Long> messages) {
        this.messages = messages;
    }

    public ClientResponse(Object result) {
        this.result = result;
    }

    public ClientResponse(boolean success) {
        this.success = success;
    }

    private ClientResponse(Builder builder) {
        setResult(builder.result);
    }

    public static ClientResponse ok() {
        return new ClientResponse(true);
    }

    public static ClientResponse fail() {
        return new ClientResponse(false);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getExtraMessage() {
        return extraMessage;
    }

    public void setExtraMessage(int extraMessage) {
        this.extraMessage = extraMessage;
    }

    public Long getLatency() {
        return Latency;
    }

    public void setLatency(Long latency) {
        Latency = latency;
    }

    public static final class Builder {

        private Object result;

        private Builder() {
        }

        public Builder result(Object val) {
            result = val;
            return this;
        }

        public ClientResponse build() {
            return new ClientResponse(this);
        }
    }
}
