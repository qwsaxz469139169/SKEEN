package ac.uk.ncl.gyc.skeen.entity;

import ac.uk.ncl.gyc.skeen.logModule.LogEntry;

import java.io.Serializable;

/**
 * Created by GYC on 2020/6/29.
 */
public class InitialTaskRequest implements Serializable {

    String serverId;

    LogEntry logEntry;

    long ts;

    String message;


    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public void setLogEntry(LogEntry logEntry) {
        this.logEntry = logEntry;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
