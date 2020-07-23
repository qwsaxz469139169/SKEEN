package ac.uk.ncl.gyc.skeen.current;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkeenThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkeenThread.class);
    private static final UncaughtExceptionHandler uncaughtExceptionHandler = (t, e)
            -> LOGGER.warn("Exception occurred from thread {}", t.getName(), e);

    public SkeenThread(String threadName, Runnable r) {
        super(r, threadName);
        setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

}
