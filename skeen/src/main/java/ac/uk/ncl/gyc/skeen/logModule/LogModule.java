package ac.uk.ncl.gyc.skeen.logModule;

/**
 *
 * @see LogEntry
 * @author Yuchen Guo
 */
public interface LogModule {

    void write(LogEntry logEntry);

    LogEntry read(Long index);

    void removeOnStartIndex(Long startIndex);

    LogEntry getLast();

    Long getLastIndex();
}
