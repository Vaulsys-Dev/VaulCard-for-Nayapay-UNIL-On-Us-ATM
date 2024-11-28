package vaulsys.network.remote;

import org.apache.log4j.Logger;
import vaulsys.network.channel.base.Channel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class RemoteMessageUtil {
	private static final Logger logger = Logger.getLogger(RemoteMessageUtil.class);

	public static byte[] sendCommandToHSM(MessageType type, Channel entity, byte[] command) {
		byte[] response = null;

		logger.info(String.format("RemoteMessageUtil.sendCommandToHSM: Type[%s], Channel[%s], Command[%s]", type, entity.getChannelId(), entity.getCommand()));

		Socket socket = new Socket();
		try {
			socket.setSoTimeout(Integer.valueOf(entity.getTimeOut()));
			socket.connect(new InetSocketAddress(entity.getIp(), Integer.valueOf(entity.getPort())));

			DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
			byte[] core_msg = command;
			oos.write(core_msg);
			logger.info("message has been written!");

			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			int length = dIn.readInt();                    // read length of incoming message
			if(length>0) {
				byte[] message = new byte[length];
				dIn.read(message, 0, message.length); // read the message

				response = message;
			}

			oos.close();
		} catch (Exception e) {
			logger.info("exception in RemoteMessageManager:" + e);
			throw new RuntimeException(e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return response;
	}


	public static byte[] sendCommandToAtallaHSM(MessageType type, Channel entity, byte[] command) {
		byte[] response = null;

		logger.info(String.format("RemoteMessageUtil.sendCommandToHSM: Type[%s], Channel[%s], Command[%s]", type, entity.getChannelId(), entity.getCommand()));

		Socket socket = new Socket();
		try {
			socket.setSoTimeout(Integer.valueOf(entity.getTimeOut()));
			socket.connect(new InetSocketAddress(entity.getIp(), Integer.valueOf(entity.getPort())));

			DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
			byte[] core_msg = command;
			oos.write(core_msg);
			logger.info("message has been written!");

			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			byte[] message = new byte[1024];
			int length = dIn.read(message); // read the message
			if(length>0) {
				response = Arrays.copyOf(message, length);
			}

			oos.close();
		} catch (Exception e) {
			logger.info("exception in RemoteMessageManager:" + e);
			throw new RuntimeException(e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return response;
	}
}
