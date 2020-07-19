package ac.uk.ncl.gyc.skeen.client;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by GYC on 2020/7/19.
 */
public class ReMes {

    private String message;

    public ReMes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
