package vaulsys.log.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SMSAppender extends AppenderSkeleton {
	private String ip;
	private int port;
	private int timeout = 2000;

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	protected void append(final LoggingEvent event) {
		if (event.getMessage() == null)
			return;

		new Thread() {
			@Override
			public void run() {
				String msg = event.getMessage().toString();
				String[] p = msg.split("\\|\\|");
				if (p.length == 2) {
					try {
						Socket socket = new Socket();
						socket.setSoTimeout(timeout);
						socket.connect(new InetSocketAddress(ip, port));
						PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"), true);
						String[] numbers = p[0].split("[,]");
						if(numbers.length > 0)
							for (String num : numbers) {
								String data = "<Message><Number>" + num + "</Number><Body>" + p[1] + "</Body></Message>";
								out.println(data);
							}
						else
							LogLog.error("No number to send SMS");
						socket.close();
					} catch (IOException e) {
						LogLog.error("SMSAppender connection problem: ", e);
					} catch (Exception e) {
						LogLog.error("Sending SMS problem: ", e);
					}
				}
				else
					LogLog.error("Wrong message format: " + msg);
			}
		}.start();
	}

	public boolean requiresLayout() {
		return false;
	}

	public void close() {
		System.out.println("SMSAppender.close()");
	}
}
