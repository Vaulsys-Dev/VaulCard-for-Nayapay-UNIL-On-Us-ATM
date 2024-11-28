package vaulsys.network.filters;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.bouncycastle.util.encoders.Hex;

public class Mina2NCC4IoFilterNew extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2NCC4IoFilterNew.class);
    public byte[] syncHDLCHeader;
	public int realLen;
	public final int numHeaderBytes = 4;
	public boolean enableDebug = false;
	public static final int MAX_LEN = 500;

	public void setParameter(Object obj){
		syncHDLCHeader = Hex.decode((String) obj);
		realLen = numHeaderBytes+syncHDLCHeader.length+2;
	}

	public void setEnableDebug(){
		enableDebug = true;
	}
	
	public void unsetEnableDebug(){
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

		int ii=0;
		while (byteMessage.hasRemaining()){
			binaryReceiveBytes.add(byteMessage.get());
			ii++;
			if(ii >= MAX_LEN){
				logger.info("Break binaryReceiveBytes.add loop");
				break;
			}
		}

		if (enableDebug)
			logger.info("Total yet received:" + binaryReceiveBytes.toString());

		if (binaryReceiveBytes.size() < realLen){
			int index = 0;
			for (; index < syncHDLCHeader.length; index++) {
				if (binaryReceiveBytes.get(index) != syncHDLCHeader[index]) {
					if (enableDebug)
						logger.error("Invalid syncHDLCHeader, expected: " + syncHDLCHeader[index] + " found: "+ binaryReceiveBytes.get(index));
						binaryReceiveBytes.clear();
				}
			}
			return;
		}

		while (binaryReceiveBytes.size() >= realLen) {
			int index = 0;
			for (; index < syncHDLCHeader.length; index++) {
				if (binaryReceiveBytes.get(index) != syncHDLCHeader[index]) {
					if (enableDebug)
						logger.error("Invalid syncHDLCHeader, expected: " + syncHDLCHeader[index] + " found: "+ binaryReceiveBytes.get(index));
						binaryReceiveBytes.clear();
					return;
				}
			}
			index = syncHDLCHeader.length;
			byte hdlc1 = binaryReceiveBytes.get(index++);
			byte hdlc2 = binaryReceiveBytes.get(index++);
			// index += 2;

			int LRILength = 0;
			if(binaryReceiveBytes.get(index) == (byte)'L' && binaryReceiveBytes.get(index+1) == (byte)'R' && binaryReceiveBytes.get(index+2) == (byte)'I') {
				//It hase Logging Record Index with extended LRI Information
				if (enableDebug)
					logger.debug("LRI is detected...");
				index = 40 - 2; // 2 ta az meganac kamtare chon, avale meganac 2 byte tool dare azizam
				LRILength = 3 + //LRI
							2 + //LLLL
							28; //Fixed Length
			}

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
			
			if (binaryReceiveBytes.size() >= len+realLen+LRILength) {
				byte[] actualMessage = new byte[len+2+LRILength];

				actualMessage[0] = hdlc1;
				actualMessage[1] = hdlc2;
				
				if(LRILength > 0){
					for (int i = 0; i < LRILength; i++)
						actualMessage[i+2] = (byte) binaryReceiveBytes.get(i + realLen - numHeaderBytes);					
				}

				for (int i = 0; i < len; i++)
					actualMessage[i+LRILength+2] = (byte) binaryReceiveBytes.get(i + realLen + LRILength);

				ArrayList<Byte> subList = new ArrayList<Byte>();
				for (int i = len+realLen+LRILength; i < binaryReceiveBytes.size(); i++)
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
		byte[] binData = new byte[data.length + realLen-2];
		System.arraycopy(data, 2, binData, realLen, data.length-2);

		int index = 0;

		binData[index + 0] = (byte) syncHDLCHeader[index + 0];
//		binData[index + 1] = (Byte) session.getAttribute("HDLC-1");
//		binData[index + 2] = (Byte) session.getAttribute("HDLC-2");
		binData[index + 1] = data[0];
		binData[index + 2] = data[1];
		binData[index + 3] = (byte) syncHDLCHeader[index + 1];
		binData[index + 4] = (byte) syncHDLCHeader[index + 2];

		index = syncHDLCHeader.length + 2;

		binData[index+0] = (byte) (data.length / 1000 + 48);
	    binData[index+1] = (byte) ((data.length % 1000) / 100 + 48);
	    binData[index+2] = (byte) ((data.length % 100) / 10 + 48);
	    binData[index+3] = (byte) (data.length % 10 + 48);

		super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
	}
}
