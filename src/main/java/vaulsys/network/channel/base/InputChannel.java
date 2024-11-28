package vaulsys.network.channel.base;

import vaulsys.network.mina2.Mina2Acceptor;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import javax.persistence.*;
import java.net.InetSocketAddress;

@Entity
@Table(name = "network_info")
public class InputChannel extends Channel {
	@Transient
	private Mina2Acceptor acceptor;

	@Column(name = "ORIG_CHANNEL_ID", insertable = false, updatable = false)
	private String originChannelId;

	public InputChannel() {
		super();
	}

	public InputChannel(InetSocketAddress localAddress, String name, String protocolName, String protocolGenericName,
			String protocolClass, String ioFilter, CommunicationMethod responseMethod, String institutionId, String originChannelId,
			boolean macEnable, boolean pinTranEnable, Integer keepAlive, String encodingConvertor, 
			Integer maxSessionNumber, Boolean masterDependant, int srcTPDULen, boolean isSecure, int headerLen,String channelId,String channelType, Integer timeout)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(localAddress, name, protocolName, protocolGenericName, protocolClass, ioFilter, responseMethod, institutionId,
				macEnable, pinTranEnable, keepAlive, encodingConvertor, maxSessionNumber, masterDependant, srcTPDULen, isSecure, headerLen, channelId, channelType, timeout);
		if (!Util.hasText(originChannelId) || responseMethod == CommunicationMethod.SAME_SOCKET)
			this.originChannelId = institutionId;
		else
//			this.originatorChannel = originator;
			this.originChannelId = originChannelId;
	}

	public InputChannel(String localIp, int localPort, String name, String protocolName, String protocolGenericName,
			String protocolClass, String ioFilter, CommunicationMethod responseMethod, String institutionId,
			String originChannelId, boolean macEnable, boolean pinTranEnable, 
			Integer keepAlive, String encodingConvertor, Integer maxSessionNumber, Boolean masterDependant, int srcTPDULen, boolean isSecure, int headerLen, String channelId, String channelType, Integer timeout)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(localIp, localPort, name, protocolName, protocolGenericName, protocolClass, ioFilter, responseMethod,
				institutionId, macEnable, pinTranEnable, keepAlive, encodingConvertor, maxSessionNumber, masterDependant, srcTPDULen, isSecure, headerLen, channelId, channelType, timeout);

		if (!Util.hasText(originChannelId) || responseMethod == CommunicationMethod.SAME_SOCKET)
			this.originChannelId = institutionId;
		else
			this.originChannelId = originChannelId;
	}

	/*public InputChannel(String string, int i, String string2, String string3, //Raza commenting
			String string4, String string5, String string6,
			CommunicationMethod sameSocket, long l, Object object, boolean b,
			boolean c, int j, String string7, int k, boolean d) {
		// TODO Auto-generated constructor stub
	}*/

	public InetSocketAddress getLocalAddress() {
		return getAddress();
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		setAddress(localAddress);
	}

	public String getOriginatorChannelId() {
		if (!Util.hasText(originChannelId)|| communicationMethod == CommunicationMethod.SAME_SOCKET)
			return null;

		return this.originChannelId;
	}

	public Channel getOriginatorChannel() {
		if (!Util.hasText(originChannelId)|| communicationMethod == CommunicationMethod.SAME_SOCKET)
			return this;

//		return GlobalContext.getInstance().getChannel(this.originatorChannelName);
		return ProcessContext.get().getChannel(this.originChannelId);
	}

	public void setOriginatorChannelId(String originChannelId) {
		/*if (!Util.hasText(originChannelId)|| communicationMethod == CommunicationMethod.SAME_SOCKET)
			this.originChannelId = this.NAME;
		else*/
			this.originChannelId = originChannelId;
	}

	public Mina2Acceptor getAcceptor() {
		return acceptor;
	}

	public void setAcceptor(Mina2Acceptor acceptor) {
		this.acceptor = acceptor;
	}
}
