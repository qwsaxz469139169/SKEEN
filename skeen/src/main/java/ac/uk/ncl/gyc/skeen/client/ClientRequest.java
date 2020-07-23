package ac.uk.ncl.gyc.skeen.client;

import java.io.Serializable;

public class ClientRequest implements Serializable {

    public static int PUT = 0;
    public static int GET = 1;

    int type;

    String key;

    String value;

    String message;

    public ClientRequest() {
    }

    private ClientRequest(Builder builder) {
        setType(builder.type);
        setKey(builder.key);
        setValue(builder.value);
        setMessage(builder.message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public enum Type {
        PUT(0), GET(1);
        int code;

        Type(int code) {
            this.code = code;
        }

        public static Type value(int code) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "message='" + message + '\'' +
                '}';
    }

    public static final class Builder {

        private int type;
        private String key;
        private String value;
        private String message;

        private Builder() {
        }


        public Builder type(int val) {
            type = val;
            return this;
        }

        public Builder key(String val) {
            key = val;
            return this;
        }

        public Builder value(String val) {
            value = val;
            return this;
        }

        public Builder message(String message) {
            message = message;
            return this;
        }

        public ClientRequest build() {
            return new ClientRequest(this);
        }
    }
}
