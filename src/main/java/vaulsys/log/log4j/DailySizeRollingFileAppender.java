package vaulsys.log.log4j;

import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

public class DailySizeRollingFileAppender extends FileAppender {
	static final int TOP_OF_TROUBLE = -1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR = 1;
	static final int HALF_DAY = 2;
	static final int TOP_OF_DAY = 3;
	static final int TOP_OF_WEEK = 4;
	static final int TOP_OF_MONTH = 5;

	private String datePattern = "'.'yyyy-MM-dd";

	private String scheduledFilename = null;

	private long nextCheck = System.currentTimeMillis() - 1;

	private Date now = new Date();

	private SimpleDateFormat sdf;

	private RollingCalendar rc = new RollingCalendar();

	private long maxFileSize = 10 * 1024 * 1024;

	public void setDatePattern(String pattern) {
		datePattern = pattern;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public long getMaximumFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public void setMaxFileSize(String value) {
		maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
	}

	@Override
	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
		super.setFile(fileName, append, bufferedIO, bufferSize);
		if (append) {
			File f = new File(fileName);
			((CountingQuietWriter) qw).setCount(f.length());
		}
	}

	@Override
	protected void setQWForFiles(Writer writer) {
		qw = new CountingQuietWriter(writer, errorHandler);
	}

	protected int computeCheckPeriod() {
		RollingCalendar c = new RollingCalendar();
		// set sate to 1970-01-01 00:00:00 GMT
		Date epoch = new Date(0);
		if (datePattern != null) {
			for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				String r0 = sdf.format(epoch);
				c.setType(i);
				Date next = new Date(c.getNextCheckMillis(epoch));
				String r1 = sdf.format(next);
				//LogLog.debug("Type = "+i+", r0 = "+r0+", r1 = "+r1);
				if (r0 != null && r1 != null && !r0.equals(r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; // Deliberately head for trouble...
	}

	@Override
	protected void subAppend(LoggingEvent event) {
		long n = System.currentTimeMillis();
		if (n >= nextCheck) {
			now.setTime(n);
			nextCheck = rc.getNextCheckMillis(now);
			rollOverTime();
		}

		if (((CountingQuietWriter) qw).getCount() >= maxFileSize)
			rollOverSize();

		super.subAppend(event);
	}

	@Override
	public void activateOptions() {
		super.activateOptions();

		if (datePattern != null) {
			now.setTime(System.currentTimeMillis());
			sdf = new SimpleDateFormat(datePattern);
			int type = computeCheckPeriod();
			rc.setType(type);
		}
		else
			LogLog.error("Either DatePattern or rollingStyle options are not set for [" + name + "].");

		if (fileName != null) {
			File file = new File(fileName);
			scheduledFilename = fileName + sdf.format(new Date(file.lastModified()));
		}
	}

	protected void rollOverTime() {
		String datedFilename = fileName + sdf.format(now);

		if (scheduledFilename.equals(datedFilename))
			return;

		this.closeFile();

		rollOverSize();

		scheduledFilename = datedFilename;
	}

	protected void rollOverSize() {
		this.closeFile();

		Calendar cal = Calendar.getInstance();
		rollFile(fileName, scheduledFilename + String.format("_%02d-%02d-%02d",
				cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));

		try {
			this.setFile(fileName, false, this.bufferedIO, this.bufferSize);
		}
		catch (IOException e) {
			LogLog.error("setFile(" + fileName + ", false) call failed.", e);
		}
	}

	protected static void rollFile(String from, String to) {
		File target = new File(to);
		if (target.exists()) {
			LogLog.debug("renaming existing target file: " + target);
			recursiveSafeBackup(to);
		}

		File file = new File(from);
		file.renameTo(new File(to));
		LogLog.debug(from + " -> " + to);
	}

	private static void recursiveSafeBackup(String from){
		String to = from + ".old";
		File fromFile = new File(from);
		File toFile = new File(to);
		if(toFile.exists())
			recursiveSafeBackup(to);
		fromFile.renameTo(toFile);
	}

	class RollingCalendar extends GregorianCalendar {
		private static final long serialVersionUID = -3560331770601814177L;
		int type = TOP_OF_TROUBLE;

		RollingCalendar() {
			super();
		}

		RollingCalendar(TimeZone tz, Locale locale) {
			super(tz, locale);
		}

		void setType(int type) {
			this.type = type;
		}

		public long getNextCheckMillis(Date now) {
			return getNextCheckDate(now).getTime();
		}

		public Date getNextCheckDate(Date now) {
			this.setTime(now);

			switch (type) {
				case TOP_OF_MINUTE:
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.MINUTE, 1);
					break;
				case TOP_OF_HOUR:
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case HALF_DAY:
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					int hour = get(Calendar.HOUR_OF_DAY);
					if (hour < 12) {
						this.set(Calendar.HOUR_OF_DAY, 12);
					}
					else {
						this.set(Calendar.HOUR_OF_DAY, 0);
						this.add(Calendar.DAY_OF_MONTH, 1);
					}
					break;
				case TOP_OF_DAY:
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.DATE, 1);
					break;
				case TOP_OF_WEEK:
					this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				case TOP_OF_MONTH:
					this.set(Calendar.DATE, 1);
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.MONTH, 1);
					break;
				default:
					throw new IllegalStateException("Unknown periodicity type.");
			}
			return getTime();
		}
	}
}