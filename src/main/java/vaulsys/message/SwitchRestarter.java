package vaulsys.message;

import vaulsys.util.ConfigUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class SwitchRestarter {
	private static final Logger logger = Logger.getLogger(SwitchRestarter.class);

	private AtomicInteger noOfRejection = new AtomicInteger(0);
	private File switchRootDir;
	private boolean restarted = false;

	public SwitchRestarter() {
		this(".");
	}

	public SwitchRestarter(String switchRootDir) {
		this.switchRootDir = new File(switchRootDir);

		if(!this.switchRootDir.isDirectory()) {
			logger.error("Not Directory Error: " + switchRootDir);
			throw new RuntimeException();
		}
		logger.info("Switch Root Directory: " + this.switchRootDir.getAbsolutePath());
	}

	public void processExecuted() {
		noOfRejection.set(0);
	}

	public void processRejected() {
		int no = noOfRejection.incrementAndGet();
		logger.warn("No Of Rejection: " + no);

		if(no >= ConfigUtil.getInteger(ConfigUtil.THREADPOOL_MAIN_REJECTED_THRESHOLD)) {
			logger.error("No Of Rejection Exceeded: " + no);
			if(!restarted) {
				restarted = true;
				restartSwitch();
			}
		}
	}

	private void restartSwitch() {
		logger.error("Restarting Switch ...");
		ProcessBuilder starterBuilder = new ProcessBuilder("./run.sh", "-b");
		starterBuilder.directory(switchRootDir);
		try {
			starterBuilder.start();
			logger.info("Switch Restart Process Executed!");
			try {
				String msg = String.format("Switch Restarted!\n%1$tH:%1$tM:%1$tS\nServer: Pasargad\n*Fanap Monitoring*\n", new Date());
				for(String num : NUMBERS)
					sendSMS(num, msg);
			} catch (Exception e) {
				logger.error("Sending SMS: ", e);
			}
		} catch (IOException e) {
			logger.error("Restart Switch Execution Problem: ", e);
		}
	}
	
    private static List<String> NUMBERS = Arrays.asList(
            "09123177842", "09195385355", "09398412512", "09123858380", "09126213866", "09126207143", "09123582599", "09126227331", "09124631562", "09379659158", "09122390466", "09125158936","09125849557");
    // Mohammad Nejad Sedaghat, SHIFT, ON_CALL, Samane Nemati, Ali Fardad, Sahar Kia, Leila Pakravan, Mehdi Torki, Mostafa Ahmadi, Kamelia MirKamali, Arezu Morovati, Hadis Mansuri, Mehdi Honarmand


	public static void sendSMS(String dest, String msg) {
		String data = "<Message><Number>" + dest + "</Number><Body>" + msg + "</Body></Message>";
		Socket socket = null;
		try {
			String ip = ConfigUtil.getProperty(ConfigUtil.SMS_SERVER_IP);
			Integer port = ConfigUtil.getInteger(ConfigUtil.SMS_SERVER_PORT);
			if (ip != null && port != null) {
				socket = new Socket();
				socket.connect(new InetSocketAddress(ip, port), 30000);
				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"), true);
				out.println(data);
			} else
				logger.warn("Try to Connect SMS Server, Null IP or Port Configuration!");
		} catch (UnknownHostException e) {
			logger.error("Unknown host for SMS Server", e);
		} catch (NoRouteToHostException e) {
			logger.error("No Route To SMS Server", e);
		} catch (IOException e) {
			logger.error("No I/O for SMS Server", e);
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					logger.error("Socket close exception: ", e);
				}
		}

		logger.info("Send SMS TO: " + dest + "::" + data);
	}

}
