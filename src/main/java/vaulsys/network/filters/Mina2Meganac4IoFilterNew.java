package vaulsys.network.filters;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.bouncycastle.util.encoders.Hex;

public class Mina2Meganac4IoFilterNew extends IoFilterAdapter {
	transient Logger logger = Logger.getLogger(Mina2Meganac4IoFilterNew.class);
//	public static byte[] syncHDLCHeader = new byte[] { 0x60, 0x00, 0x57 };
//	public static int realLen = 2 + syncHDLCHeader.length + 2 + 2;
    public byte[] syncHDLCHeader;
	public int realLen;
	public final int numHeaderBytes = 4;

	public boolean enableDebug = true;
	public static final int MAX_LEN = 500;


	public void setParameter(Object obj){
		syncHDLCHeader = Hex.decode((String) obj);
		realLen = numHeaderBytes+syncHDLCHeader.length+2+2;
	}

	public void setEnableDebug() {
		enableDebug = true;
	}

	public void unsetEnableDebug() {
		enableDebug = false;
	}

	@SuppressWarnings("unchecked")
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		if (enableDebug)
			logger.info("Filter Message Received from: " + session.getRemoteAddress());
		ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");

		if (binaryReceiveBytes == null) {
			binaryReceiveBytes = new ArrayList<Byte>();
			session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
		}

		IoBuffer byteMessage = (IoBuffer) message;

		while (byteMessage.hasRemaining()){
			binaryReceiveBytes.add(byteMessage.get());
		}

		if (enableDebug)
			logger.info("Total yet received:" + binaryReceiveBytes.toString());

		int index = 0;
		// session.setAttribute("HDLC-1", binaryReceiveBytes.get(index++));
		// session.setAttribute("HDLC-2", binaryReceiveBytes.get(index++));
		index++;
		index++;
				
		if (binaryReceiveBytes.size() < realLen){
			for (; index < syncHDLCHeader.length + 2; index++) {
				if (binaryReceiveBytes.get(index) != syncHDLCHeader[index - 2]) {
					if (enableDebug)
						logger.error("Invalid syncHDLCHeader, expected: " + syncHDLCHeader[index] + " found: "+ binaryReceiveBytes.get(index));
					binaryReceiveBytes.clear();
				}
			}
			return;
		}

		while (binaryReceiveBytes.size() >= realLen) {
			for (; index < syncHDLCHeader.length + 2; index++) {
				if (binaryReceiveBytes.get(index) != syncHDLCHeader[index - 2]) {
					if (enableDebug)
						logger.error("Invalid syncHDLCHeader, expected: " + syncHDLCHeader[index] + " found: "+ binaryReceiveBytes.get(index));
					binaryReceiveBytes.clear();
					return;
				}
			}
			index = syncHDLCHeader.length + 2;
			
			byte hdlc1 = binaryReceiveBytes.get(index++);
			byte hdlc2 = binaryReceiveBytes.get(index++);
//			session.setAttribute("HDLC-1", binaryReceiveBytes.get(index++));
//			session.setAttribute("HDLC-2", binaryReceiveBytes.get(index++));
			// index += 2;

			int b1 = binaryReceiveBytes.get(index++) - 48;
	      	int b2 = binaryReceiveBytes.get(index++) - 48;
	        int b3 = binaryReceiveBytes.get(index++) - 48;
	        int b4 = binaryReceiveBytes.get(index++) - 48;

	        int len = (b1 * 1000) + b2 * 100 + b3 * 10 + b4;

			if(len > MAX_LEN){
				if (enableDebug)
					logger.error("Message len ("+len+") is greater than maximum defined len ("+MAX_LEN+"), message dropped....");
					binaryReceiveBytes.clear();
				return;
			}

			if (binaryReceiveBytes.size() >= len+realLen) {
				byte[] actualMessage = new byte[len+2];

				actualMessage[0] = hdlc1;
				actualMessage[1] = hdlc2;
				
				for (int i = 0; i < len; i++)
					actualMessage[i+2] = (byte) binaryReceiveBytes.get(i + realLen);

				ArrayList<Byte> subList = new ArrayList<Byte>();
				for (int i = len+realLen; i < binaryReceiveBytes.size(); i++)
					subList.add(binaryReceiveBytes.get(i));
				binaryReceiveBytes.clear();// = (ArrayList<Byte>)
				binaryReceiveBytes.addAll(subList);

				super.messageReceived(nextFilter, session, actualMessage);
			} else {
				return;
			}
		}
	}

	@Override
	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {

		byte[] messageBytes = (byte[]) writeRequest.getMessage(); // ((OutgoingMessage)writeRequest.getMessage()).getBinaryData();
		IoBuffer buff = IoBuffer.wrap(messageBytes);

		byte[] data = buff.array();
		byte[] binData = new byte[data.length + realLen -2];
		System.arraycopy(data, 2, binData, realLen, data.length-2);

		int index = 0;

		// binData[index+0] = (Byte) session.getAttribute("HDLC-1");
		// binData[index+1] = (Byte) session.getAttribute("HDLC-2");
		binData[index + 0] = (byte) ((binData.length - 2) / 256);
		binData[index + 1] = (byte) ((binData.length - 2) % 256);
		binData[index + 2] = (byte) syncHDLCHeader[index + 2 - 2];
//		binData[index + 3] = (Byte) session.getAttribute("HDLC-1");
//		binData[index + 4] = (Byte) session.getAttribute("HDLC-2");
		binData[index + 3] = data[0];
		binData[index + 4] = data[1];
		binData[index + 5] = (byte) syncHDLCHeader[index + 3 - 2];
		binData[index + 6] = (byte) syncHDLCHeader[index + 4 - 2];

		index = syncHDLCHeader.length + 2 + 2;

		binData[index+0] = (byte) (data.length / 1000 + 48);
	    binData[index+1] = (byte) ((data.length % 1000) / 100 + 48);
	    binData[index+2] = (byte) ((data.length % 100) / 10 + 48);
	    binData[index+3] = (byte) (data.length % 10 + 48);

		super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
	}
}
