package ac.uk.ncl.gyc.skeen.entity;

import java.io.Serializable;

/**
 * Created by GYC on 2020/6/29.
 */
public class InitialTaskResponse implements Serializable {
    boolean success;

    long logicClock;

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

