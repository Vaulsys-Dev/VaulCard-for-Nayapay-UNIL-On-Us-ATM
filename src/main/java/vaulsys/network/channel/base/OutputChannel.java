package vaulsys.network.channel.base;

import vaulsys.network.mina2.Mina2Connector;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.net.InetSocketAddress;

@Entity
@Table(name = "network_info")
public class OutputChannel extends Channel {
    @Transient
	private Mina2Connector connector;

    public OutputChannel() {
        super();
    }

    public OutputChannel(InetSocketAddress remoteAddress, String name,
            String protocolName, String protocolGenericName, String protocolClass, String ioFilter, 
            CommunicationMethod requestMethod, String institutionId, Integer keepAlive, boolean macEnable, 
            boolean pinTranEnable,String encodingConvertor, Integer maxSessionNumber, 
            Boolean masterDependant, int srcTPDULen, boolean isSecure, int headerLen, String channelId, String channelType, Integer timeout) // Asim Shahzad, Date : 10th Dec 2016, Desc : For VISA SMS header length handling
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        super(remoteAddress, name, protocolName, protocolGenericName, protocolClass, ioFilter, requestMethod, institutionId, macEnable, pinTranEnable, keepAlive, encodingConvertor, maxSessionNumber, masterDependant, srcTPDULen, isSecure, headerLen, channelId, channelType, timeout);
    }

    public OutputChannel(String remoteIp, int remotePort, String name,
            String protocolName, String protocolGenericName, String protocolClass, String ioFilter, 
            CommunicationMethod requestMethod, String institutionId, Integer keepAlive, 
            boolean macEnable, boolean pinTranEnable, String encodingConvertor, 
            Integer maxSessionNumber, Boolean masterDependant, int srcTPDULen, boolean isSecure, int headerLen, String channelId, String channelType, Integer timeout) // Asim Shahzad, Date : 10th Dec 2016, Desc : For VISA SMS header length handling
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        super(remoteIp, remotePort, name, protocolName, protocolGenericName, protocolClass, ioFilter, requestMethod, institutionId, macEnable, pinTranEnable,keepAlive,encodingConvertor, maxSessionNumber, masterDependant, srcTPDULen, isSecure, headerLen, channelId, channelType, timeout);
    }


    /*public OutputChannel(String iP, int port, String string, String string2,
			String string3, String string4, String name,
			CommunicationMethod sameSocket, long l, int i, boolean b,
			boolean c, Object object, Object object2, boolean d) {
		// TODO Auto-generated constructor stub
	}*/

	public InetSocketAddress getRemoteAddress() {
        return getAddress();
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        setAddress(remoteAddress);
    }

	public Mina2Connector getConnector() {
		return connector;
	}

	public void setConnector(Mina2Connector connector) {
		this.connector = connector;
	}
}
