package vaulsys.log.log4j.hibernate;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.*;

@Entity
@Table(name = "log_switch")
public class LogEvent implements HibernateLoggingEvent, IEntity<Long> {
	@Id
	@GeneratedValue(generator = "log-seq-gen")
	@SequenceGenerator(name = "log-seq-gen", allocationSize = 1, sequenceName = "log_code_seq", initialValue = 1000)
	private Long id;

	@Column(name = "msg")
	private String message;

	@Column(name = "class_name")
	private String className;

	@Column(name = "line_no")
	private String lineNumber;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "log_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "log_time"))})
	private DateTime logDate;

	@Column(name = "logger_name")
	private String loggerName;

	@Column(name = "method_name")
	private String methodName;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "start_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "start_time"))})
	private DateTime startDate;

	@Column(name = "thread_name")
	private String threadName;

	@Embedded
	@AttributeOverrides({@AttributeOverride(name = "level", column = @Column(name = "lvl"))})
	private LogLevel level;

	@Lob
	private String throwables;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public DateTime getLogDate() {
		return logDate;
	}

	public void setLogDate(DateTime logDate) {
		this.logDate = logDate;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}

	public String getThrowables() {
		return throwables;
	}

	public void setThrowables(String throwables) {
		this.throwables = throwables;
	}
}
