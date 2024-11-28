package vaulsys.network.remote;

import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.endpoint.EndPointType;

import java.io.Serializable;

public class ChannelInfo implements Serializable{
	private static final long serialVersionUID = 5196982245008757460L;

    private String name;
    private String protocolName;
    private String protocolGenericName;
    private String protocolClass;
    private String ioFilterClassName;
    private String ip;
    private Integer port;
    private Boolean isSuspended = false;
    private Long institution;
    private Boolean mac_enable;
    private Boolean pinTrans_enable;
    private String clearingActionJobsBean;
    private String clearingActionMapperBean;
    private Integer keepAlive;
    private String encodingConvertor;
    private boolean input;
    private boolean open;
    private CommunicationMethod method;
    private EndPointType endPointType;
    private String originatorChannel;
    	
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getProtocolName() {
		return protocolName;
	}
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public String getProtocolGenericName() {
		return protocolGenericName;
	}
	public void setProtocolGenericName(String protocolGenericName) {
		this.protocolGenericName = protocolGenericName;
	}

	public String getProtocolClass() {
		return protocolClass;
	}
	public void setProtocolClass(String protocolClass) {
		this.protocolClass = protocolClass;
	}

	public String getIoFilterClassName() {
		return ioFilterClassName;
	}
	public void setIoFilterClassName(String ioFilterClassName) {
		this.ioFilterClassName = ioFilterClassName;
	}

	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}

	public Boolean getIsSuspended() {
		return isSuspended;
	}
	public void setIsSuspended(Boolean isSuspended) {
		this.isSuspended = isSuspended;
	}
	
	public Long getInstitution() {
		return institution;
	}
	public void setInstitution(Long institution) {
		this.institution = institution;
	}
	
	public Boolean getMac_enable() {
		return mac_enable;
	}
	public void setMac_enable(Boolean mac_enable) {
		this.mac_enable = mac_enable;
	}

	public Boolean getPinTrans_enable() {
		return pinTrans_enable;
	}
	public void setPinTrans_enable(Boolean pinTrans_enable) {
		this.pinTrans_enable = pinTrans_enable;
	}

	public String getClearingActionJobsBean() {
		return clearingActionJobsBean;
	}
	public void setClearingActionJobsBean(String clearingActionJobsBean) {
		this.clearingActionJobsBean = clearingActionJobsBean;
	}

	public String getClearingActionMapperBean() {
		return clearingActionMapperBean;
	}
	public void setClearingActionMapperBean(String clearingActionMapperBean) {
		this.clearingActionMapperBean = clearingActionMapperBean;
	}

	public Integer getKeepAlive() {
		return keepAlive;
	}
	public void setKeepAlive(Integer keepAlive) {
		this.keepAlive = keepAlive;
	}

	public String getEncodingConvertor() {
		return encodingConvertor;
	}
	public void setEncodingConvertor(String encodingConvertor) {
		this.encodingConvertor = encodingConvertor;
	}

	public boolean isInput() {
		return input;
	}
	public void setInput(boolean input) {
		this.input = input;
	}

	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public String toString(){
		return String.format("%s, %s:%s, %s", name, ip, port, mac_enable);
	}
	public void setMethod(CommunicationMethod method) {
		this.method = method;
	}
	public CommunicationMethod getMethod() {
		return method;
	}
	public void setEndPointType(EndPointType endPointType) {
		this.endPointType = endPointType;
	}
	public EndPointType getEndPointType() {
		return endPointType;
	}
	public void setOriginatorChannel(String originatorChannel) {
		this.originatorChannel = originatorChannel;
	}
	public String getOriginatorChannel() {
		return originatorChannel;
	}
}
