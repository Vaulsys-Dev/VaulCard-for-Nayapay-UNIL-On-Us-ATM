package vaulsys.netmgmt.extended;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;

import java.text.SimpleDateFormat;

/**
 * Created by m.rehman on 4/30/2016.
 */
@Entity
@Table(name = "network_info")
public class NetworkInfo implements IEntity<Long> {

    @Id
    private Long id;
	
	@Column(name = "NETNAME",unique = true, nullable = false)
    public String NetName;
	
	@Column(name = "IP_ADDRESS")
    public String ip;
	
	@Column(name = "PORT")
    public int port;

    @Column(name="CHANNEL_ID")
    private String channelId;

    @Column(name = "CONNECT_STATUS")
    public int Connect_Status;

    @Column(name = "PROC_STATUS")
    public int Processing_Status;

    
	

    //@Column(name = "NET_NAME") //Raza commenting
    //private String networkName; //Raza commenting

    @Column(name = "NET_TYPE")
    public String networkType;
	
	@Column(name = "CUTOFF_STARTDATE", updatable = true)
    public String CutOFFDate;
	
	@Column(name = "ECHO_COUNT", updatable = true)
    public Long EchoCount;

    //@Column(name = "IP") //Raza commenting
    //private String ip; //Raza commenting

    //@Column(name = "PORT")
    //private String port;

    @Column(name = "PROTOCOL")
    private String protocol;

    @Column(name = "PROTOCOL_NAME")
    private String protocolName;

    @Column(name = "PROTOCOL_CLASS")
    private String protocolClass;

    @Column(name = "IO_FILTER")
    private String ioFilter;

    @Column(name = "ENCODING_CONVERTER")
    private String encodingConverter;

    @Column(name = "METHOD")
    private String method;

    @Column(name = "INSTITUTION_ID")
    private String institutionId;

    @Column(name = "CLEARING_ACTION_MAPPER")
    private String clearingActionMapper;

    @Column(name = "CLEARING_ACTION_JOBS")
    private String clearingActionJobs;

    @Column(name = "END_POINT")
    private String endPoint;

    @Column(name = "MAC_ENABLE")
    private Integer macEnable;

    @Column(name = "PIN_ENABLE")
    private Integer pinEnable;

    @Column(name = "KEEP_ALIVE")
    private Integer keepAlive;

    @Column(name = "SESSION_NUMBER")
    private Integer sessionNumber;

    @Column(name = "SRC_TPDU_LENGTH")
    private Integer srcTPDULength;

    @Column(name = "HEADER_LENGTH")
    private Integer headerLength;

    public NetworkInfo() {    }

    public NetworkInfo(String Netname, String IP_Address,int Port,String Type) //Raza old Constructor
    {
        this.NetName = Netname;
        this.ip = IP_Address;
        this.port = Port;
        this.Connect_Status = NetworkInfoStatus.SOCKET_RESET;
        this.Processing_Status = NetworkInfoStatus.PROCESSING_RESET;
        this.networkType = Type;
        this.setId(this.id);

        //GeneralDao.Instance.saveOrUpdate(this);
    } //Raza old Constructor
	
	
	public NetworkInfo(String channelId, Integer connectionStatus, Integer processingStatus, String settlementDate)
    {
        this.channelId = channelId;
        this.Connect_Status = connectionStatus;
        this.Processing_Status = processingStatus;
        this.CutOFFDate = settlementDate;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getConnect_status()
    {
        return this.Connect_Status;
    }

    public void setConnect_status(int Conn_Status)
    {
        this.Connect_Status = Conn_Status;
        this.Processing_Status = NetworkInfoStatus.PROCESSING_DISABLED;
        if(!GeneralDao.Instance.getCurrentSession().getTransaction().isActive())
        {
            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.saveOrUpdate(this);
            GeneralDao.Instance.endTransaction();
        }
        else
        {
            GeneralDao.Instance.saveOrUpdate(this);
        }
    }

    public int getProcessing_Status() //For SIGNOFF
    {
        return this.Processing_Status;
    }

    public void setProcessing_Status(int Proc_Status) //For SIGNOFF
    {
        //this.Processing_Status = Proc_Status;
        if(Proc_Status == 1)
            Bring_Online();
        else if(Proc_Status == 0)
            Bring_OFFLINE();
        else
            this.Processing_Status = Proc_Status; //Set as Received - case of Reset.
            //System.out.println("Unable to set Processing Status, invalid Status Code...!");
            //logger.info("Unable to set Processing Status, invalid Status Code...!");
    }

    /*public String getSettlementDate() {
        return CutOFFDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }*/

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    public String getNetworkName() {
        return NetName;
    }

    public void setNetworkName(String networkName) {
        this.NetName = networkName;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolClass() {
        return protocolClass;
    }

    public void setProtocolClass(String protocolClass) {
        this.protocolClass = protocolClass;
    }

    public String getIoFilter() {
        return ioFilter;
    }

    public void setIoFilter(String ioFilter) {
        this.ioFilter = ioFilter;
    }

    public String getEncodingConverter() {
        return encodingConverter;
    }

    public void setEncodingConverter(String encodingConverter) {
        this.encodingConverter = encodingConverter;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getClearingActionMapper() {
        return clearingActionMapper;
    }

    public void setClearingActionMapper(String clearingActionMapper) {
        this.clearingActionMapper = clearingActionMapper;
    }

    public String getClearingActionJobs() {
        return clearingActionJobs;
    }

    public void setClearingActionJobs(String clearingActionJobs) {
        this.clearingActionJobs = clearingActionJobs;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public Integer getMacEnable() {
        return macEnable;
    }

    public void setMacEnable(Integer macEnable) {
        this.macEnable = macEnable;
    }

    public Integer getPinEnable() {
        return pinEnable;
    }

    public void setPinEnable(Integer pinEnable) {
        this.pinEnable = pinEnable;
    }

    public Integer getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(Integer sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public Integer getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(Integer headerLength) {
        this.headerLength = headerLength;
    }

    public Integer getSrcTPDULength() {
        return srcTPDULength;
    }

    public void setSrcTPDULength(Integer srcTPDULength) {
        this.srcTPDULength = srcTPDULength;
    }

    public Integer getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Integer keepAlive) {
        this.keepAlive = keepAlive;
    }
	
	public void Bring_Online() //For SIGNON
    {
        System.out.println("Bringing Channel Online"); //Raza TEMP
        if(this.Connect_Status == 1)
            this.Processing_Status = 1;
        else {
            System.out.println("Unable to Process Bring-ONLINE, socket is disconnected...!");
            this.Connect_Status = NetworkInfoStatus.SOCKET_RESET;
            this.Processing_Status = NetworkInfoStatus.SOCKET_RESET;
        }
            //logger.info("Unable to Process Bring-ONLINE, socket is disconnected...!");

        GeneralDao.Instance.saveOrUpdate(this);
        //GeneralDao.Instance.flush();
        //GeneralDao.Instance.commit();


        //System.out.println("This Objects PROC_status [" + this.Processing_Status + "]"); //Raza TEMP
        //System.out.println("This Objects NAME [" + this.NetName + "]"); //Raza TEMP
        //System.out.println("This Objects COMMS_status [" + this.Connect_Status + "]"); //Raza TEMP
    }
	
	public void Bring_OFFLINE() //For SIGNON
    {
        if(this.Connect_Status == 1) {
            this.Processing_Status = 0;
            GeneralDao.Instance.saveOrUpdate(this);
        }
        else {
            System.out.println("Unable to Process Bring-ONLINE, socket is disconnected...!");
            this.Connect_Status = NetworkInfoStatus.SOCKET_RESET;
            this.Processing_Status = NetworkInfoStatus.SOCKET_RESET;
        }
            //logger.info("Unable to Process Bring-ONLINE, socket is disconnected...!");

        GeneralDao.Instance.saveOrUpdate(this);
    }
	
	public void setEchoCount()
    {
        if(this.EchoCount == null) {
            this.EchoCount = 0L;
        }
        this.EchoCount = this.EchoCount + 1;
        GeneralDao.Instance.saveOrUpdate(this);
    }

    public String getEchoCount()
    {
        return  "" + this.EchoCount;
    }
	
	public void setCutOffDate(String CutOffDate) //For SIGNON
    {
        System.out.println("CutOver Date to SET [" + CutOffDate + "]");
        this.CutOFFDate = CutOffDate;
        System.out.println("CutOver Date for DB [" + this.CutOFFDate + "]");
        GeneralDao.Instance.saveOrUpdate(this);
    }

    public String getCutOffDate() //For SIGNON
    {
        return this.CutOFFDate;
    }
}
