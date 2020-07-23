package ac.uk.ncl.gyc.skeen.logModule;

/**
 * @author Yuchen Guo
 * @see LogEntry
 */
public interface LogModule {

    void write(LogEntry logEntry);

    LogEntry read(Long index);

    void removeOnStartIndex(Long startIndex);

    LogEntry getLast();

    Long getLastIndex();
}
