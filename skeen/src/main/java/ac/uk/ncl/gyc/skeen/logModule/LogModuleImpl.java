package ac.uk.ncl.gyc.skeen.logModule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class LogModuleImpl implements LogModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogModuleImpl.class);


    /** public just for test */
    public static String dbDir;
    public static String logsDir;

    private static RocksDB logDb;

    public final static byte[] LAST_INDEX_KEY = "LAST_INDEX_KEY".getBytes();

    ReentrantLock lock = new ReentrantLock();

    static {
        if (dbDir == null) {
            dbDir = "./rocksDB-raft/" + System.getProperty("serverPort");
        }
        if (logsDir == null) {
            logsDir = dbDir + "/logModule";
        }
        RocksDB.loadLibrary();
    }

    private LogModuleImpl() {
        Options options = new Options();
        options.setCreateIfMissing(true);

        File file = new File(logsDir);
        boolean success = false;
        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (success) {
            LOGGER.warn("make a new dir : " + logsDir);
        }
        try {
            logDb = RocksDB.open(options, logsDir);
        } catch (RocksDBException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public static LogModuleImpl getInstance() {
        return DefaultLogsLazyHolder.INSTANCE;
    }

    private static class DefaultLogsLazyHolder {

        private static final LogModuleImpl INSTANCE = new LogModuleImpl();
    }

    /**
     * logEntry 的 index 就是 key. 严格保证递增.
     *
     * @param logEntry
     */
    @Override
    public void write(LogEntry logEntry) {

        boolean success = false;
        try {
            lock.tryLock(3000, MILLISECONDS);
            logEntry.setIndex(getLastIndex() + 1);
            logDb.put(logEntry.getIndex().toString().getBytes(), JSON.toJSONBytes(logEntry));
            success = true;
            LOGGER.info("LogModuleImpl write rocksDB success, logEntry info : [{}]", logEntry);
        } catch (RocksDBException | InterruptedException e) {
            LOGGER.warn(e.getMessage());
        } finally {
            if (success) {
                updateLastIndex(logEntry.getIndex());
            }
            lock.unlock();
        }
    }


    @Override
    public LogEntry read(Long index) {
        try {
            byte[] result = logDb.get(convert(index));
            if (result == null) {
                return null;
            }

            return JSON.parseObject(result, LogEntry.class);
        } catch (RocksDBException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }
    @Override
    public List<String> readAll() {
        List<String> logEntries = new ArrayList<>();

        RocksIterator iter = logDb.newIterator();
        int i = 0;
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {

            byte[] result = iter.value();
            String s =new String(result);
            if(i==0){
                System.out.println(s);
            }

            logEntries.add(s);
            i++;
        }
        return logEntries;
    }

    @Override
    public void removeOnStartIndex(Long startIndex) {
        boolean success = false;
        int count = 0;
        try {
            lock.tryLock(3000, MILLISECONDS);
            for (long i = startIndex; i <= getLastIndex(); i++) {
                logDb.delete(String.valueOf(i).getBytes());
                ++count;
            }
            success = true;
            LOGGER.warn("rocksDB removeOnStartIndex success, count={} startIndex={}, lastIndex={}", count, startIndex, getLastIndex());
        } catch (InterruptedException | RocksDBException e) {
            LOGGER.warn(e.getMessage());
        } finally {
            if (success) {
                updateLastIndex(getLastIndex() - count);
            }
            lock.unlock();
        }
    }


    @Override
    public LogEntry getLast() {
        try {
            byte[] result = logDb.get(convert(getLastIndex()));
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, LogEntry.class);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long getLastIndex() {
        byte[] lastIndex = "-1".getBytes();
        try {
            lastIndex = logDb.get(LAST_INDEX_KEY);
            if (lastIndex == null) {
                lastIndex = "-1".getBytes();
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return Long.valueOf(new String(lastIndex));
    }

    private byte[] convert(Long key) {
        return key.toString().getBytes();
    }

    // on lock
    private void updateLastIndex(Long index) {
        try {
            // overWrite
            logDb.put(LAST_INDEX_KEY, index.toString().getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }


}
