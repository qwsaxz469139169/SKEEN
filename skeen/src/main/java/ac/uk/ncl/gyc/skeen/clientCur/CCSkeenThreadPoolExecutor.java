package ac.uk.ncl.gyc.skeen.clientCur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CCSkeenThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCSkeenThreadPoolExecutor.class);

    private static final ThreadLocal<Long> COST_TIME_WATCH = ThreadLocal.withInitial(System::currentTimeMillis);

    public CCSkeenThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                     BlockingQueue<Runnable> workQueue, CCSkeenThreadPool.NameThreadFactory nameThreadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, nameThreadFactory);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        COST_TIME_WATCH.get();
        LOGGER.debug("skeen thread pool before Execute");
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        LOGGER.debug("skeen thread pool after Execute, cost time : {}", System.currentTimeMillis() - COST_TIME_WATCH.get());
        COST_TIME_WATCH.remove();
    }

    @Override
    protected void terminated() {
        LOGGER.info("active count : {}, queueSize : {}, poolSize : {}", getActiveCount(), getQueue().size(), getPoolSize());
    }
}
