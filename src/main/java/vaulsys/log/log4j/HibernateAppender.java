package vaulsys.log.log4j;

import vaulsys.calendar.DateTime;
import vaulsys.log.log4j.hibernate.HibernateLoggingEvent;
import vaulsys.log.log4j.hibernate.HibernateSessionService;
import vaulsys.log.log4j.hibernate.LogLevel;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.Vector;

public class HibernateAppender extends AppenderSkeleton implements Appender {
	private String sessionServiceClass;
	private String loggingEventClass;
	private HibernateSessionService hibernateSessionServiceImplementation;
	private Class<?> loggingEventWrapperImplementationClass;
	private static final Vector<LoggingEvent> buffer = new Vector<LoggingEvent>();
	private static Boolean appending = Boolean.FALSE;

	protected void append(LoggingEvent loggingEvent) {
		// Ensure exclusive access to the buffer in case another thread is currently writing the buffer.
		synchronized (buffer) {
			// Add the current event into the buffer
			buffer.add(loggingEvent);
			// Ensure exclusive access to the appending flag to guarantee that it doesn't change in between checking it's value and setting it
			synchronized (appending) {
				if (!appending.booleanValue())
					// No other thread is appending to the log, so this thread can perform the append
					appending = Boolean.TRUE;
				else
					//Another thread is already appending to the log and it will take care of emptying the buffer
					return;
			}
		}

		Transaction trx = null;
		Session session = null;
		try {
			session = hibernateSessionServiceImplementation.openSession();
			trx = session.beginTransaction();

			// Ensure exclusive access to the buffer in case another thread is currently adding to the buffer.
			synchronized (buffer) {
				LoggingEvent bufferLoggingEvent;
				HibernateLoggingEvent loggingEventWrapper;

				/* Get the current buffer length.  We only want to process
									  * events that are currently in the buffer.  If events get
									  * added to the buffer after this point, they must have
									  * been caused by this loop, as we have synchronized on the
									  * buffer, so no other thread could be adding an event.  Any
									  * events that get added to the buffer as a result of this
									  * loop will be discarded, as to attempt to process them will
									  * result in an infinite loop.
									  */

				int bufferLength = buffer.size();

				for (int i = 0; i < bufferLength; i++) {
					bufferLoggingEvent = buffer.get(i);

					try {
						loggingEventWrapper = (HibernateLoggingEvent) loggingEventWrapperImplementationClass.newInstance();
					} catch (IllegalAccessException iae) {
						this.errorHandler.error("Unable to instantiate class " + loggingEventWrapperImplementationClass.getName(), iae, ErrorCode.GENERIC_FAILURE);
						return;
					} catch (InstantiationException ie) {
						this.errorHandler.error("Unable to instantiate class " + loggingEventWrapperImplementationClass.getName(), ie, ErrorCode.GENERIC_FAILURE);
						return;
					}

					loggingEventWrapper.setMessage(bufferLoggingEvent.getRenderedMessage());
					loggingEventWrapper.setClassName(bufferLoggingEvent.getLocationInformation().getClassName());
					loggingEventWrapper.setLineNumber(bufferLoggingEvent.getLocationInformation().getLineNumber());
					loggingEventWrapper.setMethodName(bufferLoggingEvent.getLocationInformation().getMethodName());

					loggingEventWrapper.setLogDate(new DateTime(new Date(bufferLoggingEvent.timeStamp)));

					loggingEventWrapper.setLoggerName(bufferLoggingEvent.getLoggerName());

					loggingEventWrapper.setStartDate(new DateTime(new Date(LoggingEvent.getStartTime())));
					loggingEventWrapper.setThreadName(bufferLoggingEvent.getThreadName());

					String[] thrs = bufferLoggingEvent.getThrowableStrRep();
					if (thrs != null && thrs.length > 0) {
						StringBuilder builder = new StringBuilder();
						for (String thr : thrs)
							builder.append(thr).append("\n");
						loggingEventWrapper.setThrowables(builder.toString());
					}

					if (bufferLoggingEvent.getLevel().equals(Level.ALL))
						loggingEventWrapper.setLevel(LogLevel.ALL);
					else if (bufferLoggingEvent.getLevel().equals(Level.DEBUG))
						loggingEventWrapper.setLevel(LogLevel.DEBUG);
					else if (bufferLoggingEvent.getLevel().equals(Level.ERROR))
						loggingEventWrapper.setLevel(LogLevel.ERROR);
					else if (bufferLoggingEvent.getLevel().equals(Level.FATAL))
						loggingEventWrapper.setLevel(LogLevel.FATAL);
					else if (bufferLoggingEvent.getLevel().equals(Level.INFO))
						loggingEventWrapper.setLevel(LogLevel.INFO);
					else if (bufferLoggingEvent.getLevel().equals(Level.OFF))
						loggingEventWrapper.setLevel(LogLevel.OFF);
					else if (bufferLoggingEvent.getLevel().equals(Level.WARN))
						loggingEventWrapper.setLevel(LogLevel.WARN);
					else
						loggingEventWrapper.setLevel(LogLevel.UNKNOWN);
					session.save(loggingEventWrapper);
				}
				trx.commit();
				buffer.clear();

				/* Ensure exclusive access to the appending flag - this really
									  * shouldn't be needed as the only other check on this flag is
									  * also synchronized on the buffer.  We don't want to do this
									  * in the finally block as between here and the finally block
									  * we will not be synchronized on the buffer and another
									  * process could add an event to the buffer, but the appending
									  * flag will still be true, so that event would not get
									  * written until another log event triggers the buffer to
									  * be emptied.
									  */
				synchronized (appending) {
					appending = Boolean.FALSE;
				}
			}
		} catch (HibernateException he) {
			if (trx != null)
				trx.rollback();
			this.errorHandler.error("HibernateException", he, ErrorCode.GENERIC_FAILURE);
			// Reset the appending flag
			appending = Boolean.FALSE;
		} finally {
			try {
				if (session != null)
					session.close();
			} catch (HibernateException e) {
				this.errorHandler.error("HibernateException", e, ErrorCode.GENERIC_FAILURE);
			}
		}
	}

	public void close() {
	}

	public boolean requiresLayout() {
		return false;
	}

	public String getSessionServiceClass() {
		return sessionServiceClass;
	}

	public void setSessionServiceClass(String string) {
		this.sessionServiceClass = string;
		try {
			Class<?> c = Class.forName(string);
			try {
				hibernateSessionServiceImplementation = (HibernateSessionService) c.newInstance();
			} catch (InstantiationException ie) {
				this.errorHandler.error("Unable to instantiate class " + c.getName(), ie, ErrorCode.GENERIC_FAILURE);
			} catch (IllegalAccessException iae) {
				this.errorHandler.error("Unable to instantiate class " + c.getName(), iae, ErrorCode.GENERIC_FAILURE);
			}
		} catch (ClassNotFoundException cnfe) {
			this.errorHandler.error("Invalid HibernateAppenderSessionService class " + string, cnfe, ErrorCode.GENERIC_FAILURE);
		}
	}

	public String getLoggingEventClass() {
		return loggingEventClass;
	}

	public void setLoggingEventClass(String string) {
		loggingEventClass = string;
		try {
			loggingEventWrapperImplementationClass = Class.forName(loggingEventClass);
		} catch (ClassNotFoundException cnfe) {
			this.errorHandler.error("Invalid LoggingEvent class " + string, cnfe, ErrorCode.GENERIC_FAILURE);
		}
	}
}
