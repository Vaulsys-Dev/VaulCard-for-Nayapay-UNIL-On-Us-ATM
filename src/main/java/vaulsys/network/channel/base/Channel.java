package vaulsys.network.channel.base;

import vaulsys.clearing.base.ClearingActionMapper;
import vaulsys.clearing.jobs.ClearingActionJobs;
import vaulsys.cms.base.CMSAccount;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.base.Protocol;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.wfe.GlobalContext;
//import org.apache.axis.types.Time; //Raza commenting
import org.apache.mina.core.filterchain.IoFilter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.net.InetSocketAddress;

@MappedSuperclass
public abstract class Channel implements java.io.Serializable, IEntity<Long> {

    @Id
    private Long id;

    @Transient
    public static final String NAME = "name";

    @Transient
    private static final Integer MIN_KEEP_ALIVE_TIME = 120;

    @Transient
    CommunicationMethod communicationMethod;

    @Column(name = "METHOD")
    private String communicationMethodName;

    @Column(name = "NETNAME")
    private String name;

    @Column(name = "PROTOCOL")
	private String protocolName;

    @Column(name = "PROTOCOL_NAME")
	private String protocolGenericName;

    @Column(name = "PROTOCOL_CLASS")
	private String protocolClass;

    @Column(name = "IO_FILTER")
	private String ioFilterClassName;

    @Transient
    private IoFilter ioFilterObject = null;

    @Column(name = "IP_ADDRESS")
    private String ip;

    @Column(name = "PORT")
    private Integer port;

    @Column(name = "INSTITUTION_ID")
    private String institutionId;

    @Transient
    private Boolean isSuspended = false;

    @Transient
    private EndPointType endPointType;

    @Column(name = "END_POINT_TYPE")
    private String endPointTypeName;

    @Column(name = "MAC_ENABLE")
    private Boolean macEnable;

    @Column(name = "PIN_ENABLE")
    private Boolean pinTransEnable;

    @Transient
    private Boolean sslEnable;

    @Column(name = "CLEARING_ACTION_JOBS")
    private String clearingActionJobsBean;

    @Column(name = "CLEARING_ACTION_MAPPER")
    private String clearingActionMapperBean;

    @Transient
    private ClearingActionMapper clearingMapper = null;

    @Transient
    private ClearingActionJobs clearingActionJobs = null;

	@Transient
	protected InetSocketAddress address; //Raza making Transient

    @Column(name = "KEEP_ALIVE")
	private Integer keepAlive;

    @Column(name = "SESSION_NUMBER")
	private Integer maxSessionNumber;

    @Column(name = "ENCODING_CONVERTER")
    private String encodingConverter;

    @Transient
    private Boolean masterDependant;

    @Transient
    private boolean open = false;

    @Column(name = "ORIG_CHANNEL_ID")
    private String originatorChannelId;

    @Column(name = "SRC_TPDU_LENGTH")
    private int srcTPDULen = 0;

    @Transient
    private Boolean isSecure;

	// Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Added header handling for VISA SMS
    @Column(name = "HEADER_LENGTH")
	private int headerLen = 0;
	// End
    @Column(name = "CHANNEL_ID")
    private String channelId;

    @Column(name = "CHANNEL_TYPE")
    private String channelType;

	@Column(name = "TIMEOUT")
	private int timeout;

    @Column(name = "CONNECT_STATUS")
    private Integer connectionStatus;

    @Column(name = "PROC_STATUS")
    private Integer processingStatus;

    @Column(name = "CUTOFF_STARTDATE") //Raza for CUP Cutoff; only use this column for other cases
    private String settlementDate;

    @Column(name = "CUTOFF_ENDDATE") //Raza for CUP Cutoff
    private String settlementEndDate;

    @Column(name = "NET_TYPE")
    private String networkType;

    @Column(name = "ECHO_COUNT")
    private Long echoCount;

    //m.rehman: for NAC POS Switch
    @Column(name = "is_pos_switch")
    private Boolean isPosSwitch;

    @Column(name = "cutoff_time")
    private String settlementTime;
	
	@Column(name = "sign_on_req") //Raza adding handling when SIGNON is not required
    private Boolean signonreq;

    //m.rehman: adding echo send on tcp if system is idle to avoid disconnectivity
    @Column(name = "is_echo_req")
    private Boolean isEchoReq;

    @Column(name = "idle_time_sec")
    private Integer idleTimeSec;

    @Column(name = "last_cutoff_date_diff")
    private Long lastSettlementDateDiff;

    @Column(name = "last_cutoff_date")
    private String lastSettlementDate;

//    //@ManyToOne(fetch = FetchType.LAZY)
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "FUNDS_ACCOUNT")
//    //@Cascade(value = CascadeType.ALL)
//    @ForeignKey(name="CHANNEL_ACCT_FK")
//    private CMSAccount fundsaccount;

    //m.rehman: adding url column for runtime path of different webservices
    @Column(name = "webservice_url")
    private String webserviceURL;

    @Column(name = "MONREQ") //Raza adding for WebService
    private Boolean monreq;

    @Column(name = "CRED_USERNAME") //Raza adding for WebService
    private String credusername;

    @Column(name = "CRED_PASSWORD") //Raza adding for WebService
    private String credpassword;

    @Column(name = "CRED_CHANNELTYPE") //Raza adding for WebService
    private String credchanneltype;

    @Column(name = "CRED_CHANNELSUBTYPE") //Raza adding for WebService
    private String credchannelsubtype;

    @Column(name = "PING_REQ") //Raza adding for areyoualive message of WebService
    private Boolean pingreq;

    @Column(name = "CONNECT_TIMEOUT") //Raza adding for connecttimeout of WebService
    private Integer connecttimeout;

    @Column(name = "READ_TIMEOUT") //Raza adding for readtimeout of WebService
    private Integer readtimeout;

    @Column(name = "ALLOWED_IPS") //Raza adding for allowedips
    private String allowedips;

    // Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
    @Transient
    private String Command;

    public String getCommand() {
        return Command;
    }

    public void setCommand(String command) {
        Command = command;
    }

    // ========================================================================================================

	protected Channel() {
		super();
		address = new InetSocketAddress(0);
	}

	protected Channel(InetSocketAddress address, String name, String protocolName, String protocolGenericName,
					  String protocolClass, String ioFilter, CommunicationMethod method, String institutionId, boolean macEnable, boolean pinEnable,
					  Integer keepAlive, String encodingConverter, Integer maxSessionNumber, Boolean masterDependant, int srcTPDULen, boolean isSecure, int headerLen, String channelId, String channelType, Integer timeout) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		super();
		this.address = address;
		this.name = name; //Raza commenting
		this.protocolName = protocolName;
		this.protocolGenericName = protocolGenericName;
		this.protocolClass = protocolClass;
        this.macEnable = macEnable;
        this.pinTransEnable = pinEnable;
		this.setIoFilterClassName(ioFilter);
		this.communicationMethod = method;
        this.institutionId = institutionId;
		this.keepAlive = keepAlive;
        this.encodingConverter = encodingConverter;
		this.maxSessionNumber = maxSessionNumber;
		this.masterDependant = masterDependant;
		this.srcTPDULen = srcTPDULen;
		this.isSecure = isSecure;
		this.headerLen = headerLen; // Asim Shahzad, Date : 10th Dec 2016, Desc : For VISA SMS header length handling
		this.timeout = timeout;
		this.channelId = channelId;
        this.channelType = channelType;
	}

	protected Channel(String ip, int port, String name, String protocolName, String protocolGenericName, String protocolClass, String ioFilter, CommunicationMethod method,
					  String institutionId, boolean mac_enable, boolean pinEnable, Integer keepAlive, String encodingConverter,
					  Integer maxSessionNumber, Boolean masterDependant, int srcTPDULen, boolean isSecure, int headerLen, String channelId, String channelType, Integer timeout) // Asim Shahzad, Date : 10th Dec 2016, Desc : For VISA SMS header length handling
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(new InetSocketAddress(ip, port), name, protocolName, protocolGenericName, protocolClass, ioFilter, method, institutionId,
				mac_enable, pinEnable, keepAlive, encodingConverter, maxSessionNumber, masterDependant, srcTPDULen, isSecure, headerLen, channelId, channelType, timeout);
        this.ip = ip;
		this.port = port;
        this.institutionId = institutionId;
        this.channelId = channelId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//public String getIp() { //Raza commenitng
	//	return this.address.getAddress().getHostAddress(); //Raza commenitng
	//} //Raza commenitng

    public String getIp() {
        return this.ip;
    }
	public void setIp(String ip) {
		//this.address = new InetSocketAddress(ip, this.address.getPort()); //Raza commenting
        this.ip = ip;
	}

	//public int getPort() { //Raza commenting
	//	return this.address.getPort();
	//}

    public int getPort() {
        return this.port;
    }
	public void setPort(int port) {
		this.address = new InetSocketAddress(this.address.getAddress().getHostAddress(), port);
	}

	protected InetSocketAddress getAddress() {
		if (address.getHostName().equals("0.0.0.0")) {
			// byte[] ByteIP = new byte[4];
			// StringTokenizer tokenizer = new StringTokenizer(this.IP,".");
			// for(int i =0;i<4;++i)
			// ByteIP[0] = Byte.parseByte((String)tokenizer.nextElement());
			//
			// try {
			// return new InetSocketAddress(InetAddress.getByAddress(ByteIP),
			// this.port);
			// } catch (UnknownHostException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

            return new InetSocketAddress(this.ip, this.port);
		}
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public Protocol getProtocol() {
//		return ProtocolProvider.getInstance().getByName(protocolName);
		return ProtocolProvider.Instance.getByName(protocolName);
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

    public Boolean getIsSuspended() {
        return isSuspended;
    }

    public void setIsSuspended(boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public String getInstitutionId() {
        return institutionId;
	}

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
	}

	public EndPointType getEndPointType() {
		return endPointType;
	}

	public void setEndPointType(EndPointType endPointType) {
		this.endPointType = endPointType;
	}

    public Boolean getMacEnable() {
        return macEnable;
    }

    public void setMacEnable(Boolean macEnable) {
        this.macEnable = macEnable;
    }

    public Boolean getPinTransEnable() {
        return pinTransEnable;
    }

    public void setPinTransEnable(Boolean pinTransEnable) {
        this.pinTransEnable = pinTransEnable;
    }

    public Boolean getSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(Boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

	public String getProtocolClass() {
		return protocolClass;
	}

	public void setProtocolClass(String protocolClass) {
		this.protocolClass = protocolClass;
	}

	public String getProtocolGenericName() {
		return protocolGenericName;
	}

	public void setProtocolGenericName(String protocolGenericName) {
		this.protocolGenericName = protocolGenericName;
	}

	public String getIoFilterClassName() {
		return ioFilterClassName;
	}

	public void setIoFilterClassName(String ioFilter) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		this.ioFilterClassName = ioFilter;
		if (ioFilter != null)
			this.ioFilterObject = (IoFilter) Class.forName(ioFilter).newInstance();
	}

	public IoFilter getIoFilterObject() {
		return ioFilterObject;
	}

    public ClearingActionMapper getClearingMapper() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        if (clearingMapper == null) {
            clearingMapper = GlobalContext.getInstance().getClearingActionMapper(clearingActionMapperBean);
            // (ClearingActionMapper)
            // SwitchApplication.get().getBean(clearingActionMapperBean);
        }
        return clearingMapper;
    }

	public ClearingActionJobs getClearingActionJobs() throws ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		if (clearingActionJobs == null) {
			clearingActionJobs = GlobalContext.getInstance().getClearingActionJobs(clearingActionJobsBean);
			// (ClearingActionJobs)
			// SwitchApplication.get().getBean(clearingActionJobsBean);
		}
		return clearingActionJobs;
	}

	public String getClearingActionMapperBean() {
		return clearingActionMapperBean;
	}

	public void setClearingActionMapperBean(String clearingActionMapperBean) {
		this.clearingActionMapperBean = clearingActionMapperBean;
	}

	public String getClearingActionJobsBean() {
		return clearingActionJobsBean;
	}

	public void setClearingActionJobsBean(String clearingActionJobsBean) {
		this.clearingActionJobsBean = clearingActionJobsBean;
	}

	public CommunicationMethod getCommunicationMethod() {
		return communicationMethod;
	}

	public void setCommunicationMethod(CommunicationMethod communicationMethod) {
		this.communicationMethod = communicationMethod;
	}

	public Integer getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(Integer keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isKeepAlive() {
		if (getKeepAlive() >= Channel.MIN_KEEP_ALIVE_TIME)
			return true;
		if (getKeepAlive() < 0)
			return true;
		return false;
	}

    public String getEncodingConverter() {
        return encodingConverter;
    }

    public void setEncodingConverter(String encodingConverter) {
        this.encodingConverter = encodingConverter;
	}

	public Integer getTimeOut(){
		return this.timeout;
	}

	public void setTimeOut(Integer Timeout){
		this.timeout = Timeout;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public String toString() {
        return this.name + "-" + this.ip + ":" + this.port;
	}

	public Integer getMaxSessionNumber() {
		return maxSessionNumber;
	}

	public void setMaxSessionNumber(Integer maxSessionNumber) {
		this.maxSessionNumber = maxSessionNumber;
	}

	public Boolean getMasterDependant() {
		return masterDependant;
	}

	public void setMasterDependant(Boolean masterDependant) {
		this.masterDependant = masterDependant;
	}

    public void setOriginatorChannelId(String originatorChannelId) {
        this.originatorChannelId = originatorChannelId;
    }

    public String getOriginatorChannelId() {
        return originatorChannelId;
    }

	public int getSrcTPDULen() {
		return srcTPDULen;
	}

	public void setSrcTPDULen(int srcTPDULen) {
		this.srcTPDULen = srcTPDULen;
	}

	public Boolean getIsSecure() {
		return isSecure;
	}

	public void setIsSecure(Boolean isSecure) {
		this.isSecure = isSecure;
	}

	// Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Added header handling for VISA SMS
	public int getHeaderLen() {
		return headerLen;
	}

	public void setHeaderLen(int headerLen) {
		this.headerLen = headerLen;
	}
	// End
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }
    public Integer getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(Integer connectionStatus) {
        this.connectionStatus = connectionStatus;
        //GeneralDao.Instance.saveOrUpdate(this); //Raza to save in DB
    }

    public Integer getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(Integer processingStatus) {
        this.processingStatus = processingStatus;
        //GeneralDao.Instance.saveOrUpdate(this); //Raza to save in DB
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getCommunicationMethodName() {
        return communicationMethodName;
    }

    public void setCommunicationMethodName(String communicationMethodName) {
        this.communicationMethodName = communicationMethodName;
    }

    public String getEndPointTypeName() {
        return endPointTypeName;
    }

    public void setEndPointTypeName(String endPointTypeName) {
        this.endPointTypeName = endPointTypeName;
    }

    //m.rehman: for NAC POS Switch
    public Boolean getIsPosSwitch() {
        return isPosSwitch;
    }

    public void setIsPosSwitch(Boolean isPosSwitch) {
        this.isPosSwitch = isPosSwitch;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getEchoCount() {
        return echoCount;
    }

    public void setEchoCount(Long echoCount) {
        this.echoCount = echoCount;
    }

    public String getSettlementEndDate() {
        return settlementEndDate;
    }

    public void setSettlementEndDate(String settlementEndDate) {
        this.settlementEndDate = settlementEndDate;
    }

    public String getSettlementTime() {
        return settlementTime;
    }

    public void setSettlementTime(String settlementTime) {
        this.settlementTime = settlementTime;
    }
	
	public Boolean getSignonreq() {
        return signonreq;
    }

    public void setSignonreq(Boolean signonreq) {
        this.signonreq = signonreq;
    }

    public Boolean getEchoReq() {
        return isEchoReq;
    }

    public void setEchoReq(Boolean echoReq) {
        isEchoReq = echoReq;
    }

    public Integer getIdleTimeSec() {
        return idleTimeSec;
    }

    public void setIdleTimeSec(Integer idleTimeSec) {
        this.idleTimeSec = idleTimeSec;
    }

    public String getLastSettlementDate() {
        return lastSettlementDate;
    }

    public void setLastSettlementDate(String lastSettlementDate) {
        this.lastSettlementDate = lastSettlementDate;
    }

    public Long getLastSettlementDateDiff() {
        return lastSettlementDateDiff;
    }

    public void setLastSettlementDateDiff(Long lastSettlementDateDiff) {
        this.lastSettlementDateDiff = lastSettlementDateDiff;
    }

    public String getConnectionStatusDescription() {
        String description;
        Integer status = this.connectionStatus;

        if (status.equals(1))
            description = "Connected";
        else if (status.equals(0))
            description = "Not Connected";
        else
            description = "Unknown";

        return description;
    }

    public String getProcessingStatusDescription() {
        String description;
        Integer status = this.processingStatus;

        if (status.equals(1))
            description = "Processing";
        else if (status.equals(0))
            description = "Not Processing";
        else
            description = "Unknown";

        return description;
    }


//    public CMSAccount getFundsaccount() {
//        return fundsaccount;
//    }
//
//    public void setFundsaccount(CMSAccount poolaccount) {
//        this.fundsaccount = poolaccount;
//    }

    //m.rehman: adding url column for runtime path of different webservices
    public String getWebserviceURL() {
        return webserviceURL;
    }

    public void setWebserviceURL(String webserviceURL) {
        this.webserviceURL = webserviceURL;
    }

    public Boolean getMonreq() {
        return monreq;
    }

    public void setMonreq(Boolean monreq) {
        this.monreq = monreq;
    }

    public String getCredusername() {
        return credusername;
    }

    public void setCredusername(String credusername) {
        this.credusername = credusername;
    }

    public String getCredpassword() {
        return credpassword;
    }

    public void setCredpassword(String credpassword) {
        this.credpassword = credpassword;
    }

    public String getCredchanneltype() {
        return credchanneltype;
    }

    public void setCredchanneltype(String credchanneltype) {
        this.credchanneltype = credchanneltype;
    }

    public String getCredchannelsubtype() {
        return credchannelsubtype;
    }

    public void setCredchannelsubtype(String credchannelsubtype) {
        this.credchannelsubtype = credchannelsubtype;
    }

    public Boolean getPingreq() {
        return pingreq;
    }

    public void setPingreq(Boolean pingreq) {
        this.pingreq = pingreq;
    }

    public Integer getConnecttimeout() {
        return connecttimeout;
    }

    public void setConnecttimeout(Integer connecttimeout) {
        this.connecttimeout = connecttimeout;
    }

    public Integer getReadtimeout() {
        return readtimeout;
    }

    public void setReadtimeout(Integer readtimeout) {
        this.readtimeout = readtimeout;
    }

    public String getAllowedips() {
        return allowedips;
    }

    public void setAllowedips(String allowedips) {
        this.allowedips = allowedips;
    }
}
