package ac.uk.ncl.gyc.skeen.client;

import java.io.Serializable;
import java.util.List;

/**
 * Created by GYC on 2020/7/31.
 */
public class GetResponse implements Serializable{
    List<Message> messages;

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
