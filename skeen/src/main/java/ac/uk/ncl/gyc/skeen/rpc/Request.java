package ac.uk.ncl.gyc.skeen.rpc;

import java.io.Serializable;

import ac.uk.ncl.gyc.skeen.client.ClientRequest;

public class Request<T> implements Serializable {
    public static final int REQ_CLIENT = 0;

    public static final int REQ_REDICT= 1;
    public static final int REQ_SEND_LC = 2;
    public static final int REQ_INI_TASK = 3;
    public static final int REQ_GET = 4;


    /** 请求类型 */
    private int cmd = -1;

    private T obj;

    String url;

    public Request() {
    }

    public Request(T obj) {
        this.obj = obj;
    }

    public Request(int cmd, T obj, String url) {
        this.cmd = cmd;
        this.obj = obj;
        this.url = url;
    }

    private Request(Builder builder) {
        setCmd(builder.cmd);
        setObj((T) builder.obj);
        setUrl(builder.url);
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static  Builder newBuilder() {
        return new Builder<>();
    }


    public final static class Builder<T> {

        private int cmd;
        private Object obj;
        private String url;

        private Builder() {
        }

        public Builder cmd(int val) {
            cmd = val;
            return this;
        }

        public Builder obj(Object val) {
            obj = val;
            return this;
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Request<T> build() {
            return new Request<T>(this);
        }
    }

}
