package vaulsys.network.filters;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

public class Mina2CMSIoFilter extends IoFilterAdapter{

    private String applicationName = "CMST";
    
	transient Logger logger = Logger.getLogger(Mina2CMSIoFilter.class);

	public void setParameter(Object obj){
		this.applicationName = (String) obj;
	}
	
    @SuppressWarnings("unchecked")
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        logger.debug("Filter Message Received from: " + session.getRemoteAddress());
        ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");

        if (binaryReceiveBytes == null) {
            binaryReceiveBytes = new ArrayList<Byte>();
            session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
        }

        IoBuffer byteMessage = (IoBuffer) message;
        
        while (byteMessage.hasRemaining())
            binaryReceiveBytes.add(byteMessage.get());
        
        logger.debug("Total yet received:" + binaryReceiveBytes.toString());
        
        if(binaryReceiveBytes.size()==0)
        	return;

        if (binaryReceiveBytes.size() >= 0) {
        	int len = binaryReceiveBytes.size();
        	

            byte[] tmpMsg = new byte[len];
            for (int i = 0; i < len; i++) {
                tmpMsg[i] = (byte) binaryReceiveBytes.get(i);
            }

            int index = -1;
            for (int i = 0; i+3 < len && index == -1; i++) {
                if( tmpMsg[i] != 13 || tmpMsg[i+1] != 10 || tmpMsg[i+2] != 13 || tmpMsg[i+3] != 10)
                	continue;
                else
                	index = i;
            }

            if(index == -1)
        		return;
        	
            byte[] actualMsg = new byte[index];
            
            System.arraycopy(tmpMsg, 0, actualMsg, 0, index);

            ArrayList<Byte> subList = new ArrayList<Byte>();
            for (int i = index+4; i < binaryReceiveBytes.size(); i++)
                subList.add(binaryReceiveBytes.get(i));
            binaryReceiveBytes.clear();// = (ArrayList<Byte>)
            binaryReceiveBytes.addAll(subList);

            super.messageReceived(nextFilter, session, actualMsg);
        } else {
            return;
        }        
    }

    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
    	String ip = session.getServiceAddress().toString();
    	
    	byte[] messageBytes = (byte[]) writeRequest.getMessage();
    	IoBuffer buff = IoBuffer.wrap(messageBytes);
		byte[] data = buff.array();
    	
    	int beginIndexOf = ip.indexOf("/");
//    	int lastIndexOf = ip.indexOf(":");
    	ip = ip.substring(beginIndexOf+1/*, lastIndexOf*/);
    	
    	String header = "POST /"+ applicationName +"/starterTP?sequence=process-transaction&pageEntry=1 HTTP/1.1\r\n"
    					+ "User-Agent: Jakarta Commons-HttpClient/3.1\r\n"
    					+ "Host: "+ip+"\r\n"
    					+ "Content-Length: "+new Integer(data.length).toString()+"\r\n"
    					+ "Content-Type: application/x-www-form-urlencoded\r\n\r\n";
    	
    		
    	byte[] headerBytes = header.getBytes();
    	byte[] binData = new byte[data.length + headerBytes.length];
    	
    	System.arraycopy(headerBytes, 0, binData, 0, headerBytes.length);
    	System.arraycopy(data, 0, binData, headerBytes.length, data.length);

    	super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
    }
}
