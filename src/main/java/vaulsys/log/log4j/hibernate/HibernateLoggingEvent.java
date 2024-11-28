package vaulsys.log.log4j.hibernate;

import vaulsys.calendar.DateTime;

public interface HibernateLoggingEvent {

	public String getMessage();

	public String getClassName();

	public String getLineNumber();

	public DateTime getLogDate();

	public String getLoggerName();

	public String getMethodName();

	public DateTime getStartDate();

	public String getThreadName();

	public LogLevel getLevel();

	public String getThrowables();

	public void setMessage(String message);

	public void setClassName(String className);

	public void setLineNumber(String lineNumber);

	public void setLogDate(DateTime logDate);

	public void setLoggerName(String loggerName);

	public void setMethodName(String methodName);

	public void setStartDate(DateTime startDate);

	public void setThreadName(String threadName);

	public void setLevel(LogLevel level);

	public void setThrowables(String str);
}
