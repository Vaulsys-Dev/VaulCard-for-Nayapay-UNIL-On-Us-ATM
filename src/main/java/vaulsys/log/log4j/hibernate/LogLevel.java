package vaulsys.log.log4j.hibernate;

import vaulsys.persistence.IEnum;

public class LogLevel implements IEnum {
	public static final LogLevel UNKNOWN = new LogLevel((byte) 0); 
	public static final LogLevel ALL = new LogLevel((byte) 1);
	public static final LogLevel DEBUG = new LogLevel((byte) 2);
	public static final LogLevel ERROR = new LogLevel((byte) 3);
	public static final LogLevel FATAL = new LogLevel((byte) 4);
	public static final LogLevel INFO = new LogLevel((byte) 5);
	public static final LogLevel OFF = new LogLevel((byte) 6);
	public static final LogLevel WARN = new LogLevel((byte) 7);

	private byte level;

	public LogLevel() {
	}

	public LogLevel(byte level) {
		this.level = level;
	}

	public byte getLevel() {
		return level;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LogLevel)) return false;

		LogLevel logLevel = (LogLevel) o;

		if (level != logLevel.level) return false;

		return true;
	}

	public int hashCode() {
		return (int) level;
	}
}
