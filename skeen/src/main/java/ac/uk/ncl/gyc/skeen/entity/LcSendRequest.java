package ac.uk.ncl.gyc.skeen.entity;

import ac.uk.ncl.gyc.skeen.logModule.LogEntry;

import java.io.Serializable;
import java.util.List;

public class LcSendRequest implements Serializable {

	long ts;
	
	String Message;

	LogEntry logEntry;

	List<LogEntry> LogEntries;
	
	String serverId;

	public LcSendRequest() {
	}

	public List<LogEntry> getLogEntries() {
		return LogEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		LogEntries = logEntries;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public LogEntry getLogEntry() {
		return logEntry;
	}

	public void setLogEntry(LogEntry logEntry) {
		this.logEntry = logEntry;
	}

	@Override
	public String toString() {
		return "LcSendRequest{" +
				"ts=" + ts +
				", Message='" + Message + '\'' +
				", serverId='" + serverId + '\'' +
				'}';
	}
}
