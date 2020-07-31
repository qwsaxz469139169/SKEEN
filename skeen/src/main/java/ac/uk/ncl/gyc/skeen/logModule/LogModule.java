package ac.uk.ncl.gyc.skeen.logModule;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 *
 * @see LogEntry
 * @author Yuchen Guo
 */
public interface LogModule {

    void write(LogEntry logEntry);

    LogEntry read(Long index);

    List<String> readAll();

    void removeOnStartIndex(Long startIndex);

    LogEntry getLast();

    Long getLastIndex();
}
