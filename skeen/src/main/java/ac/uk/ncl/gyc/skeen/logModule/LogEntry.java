package ac.uk.ncl.gyc.skeen.logModule;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LogEntry implements Serializable, Comparable {

    private Long index;

    private Long logic_clock;

    private Long startTime;

    private String message;

    private String initialNode;

    private int extraMessages;

    private List<String> pendings;

    private Map<String,Long> commits;
    private Map<String,Long> res_commits;

    private Command command;

    public LogEntry() {
    }

    public Map<String, Long> getRes_commits() {
        return res_commits;
    }

    public void setRes_commits(Map<String, Long> res_commits) {
        this.res_commits = res_commits;
    }

    public  Map<String,Long> getCommits() {
        return commits;
    }

    public void setCommits( Map<String,Long> commits) {
        this.commits = commits;
    }

    public List<String> getPendings() {
        return pendings;
    }

    public void setPendings(List<String> pendings) {
        this.pendings = pendings;
    }

    public int getExtraMessages() {
        return extraMessages;
    }

    public void setExtraMessages(int extraMessages) {
        this.extraMessages = extraMessages;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInitialNode() {
        return initialNode;
    }

    public void setInitialNode(String initialNode) {
        this.initialNode = initialNode;
    }

    public LogEntry(long logic_clock, Command command) {
        this.logic_clock = logic_clock;
        this.command = command;
    }

    public LogEntry(Long index, long logic_clock, Command command) {
        this.index = index;
        this.logic_clock = logic_clock;
        this.command = command;
    }

    private LogEntry(Builder builder) {
        setIndex(builder.index);
        setLogic_clock(builder.logic_clock);
        setCommand(builder.command);
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public Long getLogic_clock() {
        return logic_clock;
    }

    public void setLogic_clock(Long logic_clock) {
        this.logic_clock = logic_clock;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "{" +
            "index=" + index +
            ", logic_clock=" + logic_clock +
            ", command=" + command +
            '}';
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (this.getIndex() > ((LogEntry) o).getIndex()) {
            return 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogEntry logEntry = (LogEntry) o;
        return logic_clock == logEntry.logic_clock &&
            Objects.equals(index, logEntry.index) &&
            Objects.equals(command, logEntry.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, logic_clock, command);
    }

    public static final class Builder {

        private Long index;
        private long logic_clock;
        private Command command;

        private Builder() {
        }

        public Builder index(Long val) {
            index = val;
            return this;
        }

        public Builder logic_clock(long val) {
            logic_clock = val;
            return this;
        }

        public Builder command(Command val) {
            command = val;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(this);
        }
    }
}
