package ac.uk.ncl.gyc.skeen.entity;

import java.io.Serializable;

public class LcSendResponse implements Serializable {

    boolean success;

    long logicClock;

    long latency;

    int extraM;

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public int getExtraM() {
        return extraM;
    }

    public void setExtraM(int extraM) {
        this.extraM = extraM;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public long getLogicClock() {
        return logicClock;
    }

    public void setLogicClock(long logicClock) {
        this.logicClock = logicClock;
    }
}
