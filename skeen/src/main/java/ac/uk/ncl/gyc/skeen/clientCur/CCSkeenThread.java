package ac.uk.ncl.gyc.skeen.clientCur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCSkeenThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCSkeenThread.class);
    private static final UncaughtExceptionHandler uncaughtExceptionHandler = (t, e)
        -> LOGGER.warn("Exception occurred from thread {}", t.getName(), e);

    public CCSkeenThread(String threadName, Runnable r) {
        super(r, threadName);
        setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

}
