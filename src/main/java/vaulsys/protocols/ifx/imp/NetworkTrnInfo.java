/**
 *
 */
package vaulsys.protocols.ifx.imp;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.UserLanguage;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

@Entity
@javax.persistence.Table(name="ifx_network_trn_info")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class NetworkTrnInfo implements IEntity<Long>, Cloneable {
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="networktrninfo-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "networktrninfo-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "networktrninfo_seq")
    				})
	private Long id;

	private TerminalType TerminalType = vaulsys.protocols.ifx.enums.TerminalType.UNKNOWN;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "origtermtype"))
    })
	private TerminalType OrigTerminalType = vaulsys.protocols.ifx.enums.TerminalType.UNKNOWN;


	private String BankId; // P32
	
	private String FwdBankId; // P33
	
	//private Long FwdToBankId;
    
	@Index(name="idx_nettrninfo_destbank")
	private String DestBankId;
	
	private String RecvBankId; // S100

    @Index(name="idx_nettrninfo_termid")
	private String TerminalId;

    @Column(name="thrd_termid")
	private Long ThirdPartyTerminalId;

    @Column(name="thrd_prt_code")
    private Long ThirdPartyCode;
    
    @Transient
    private transient String ThirdPartyName;
    
    @Transient
    private transient String ThirdPartyNameEn;
    
	@Index(name="idx_nettrninfo_netrefid")
	private String NetworkRefId;
	
	private String MyNetworkRefId;
    
    @Index(name="idx_nettrninfo_trnseqcntr")
    private String Src_TrnSeqCntr;
    
    private String My_TrnSeqCntr;
    
    private String Last_TrnSeqCntr;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "orig_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "orig_time"))
    })
    private DateTime OrigDt;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "user_lang"))
    })
    private UserLanguage userLanguage = UserLanguage.FARSI_LANG;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "orgrec")
//	@Cascade(value = CascadeType.ALL )
//	@ForeignKey(name="ifx_orgrec_fk")
	@Embedded
	@AttributeOverrides( { 
//		@AttributeOverride(name = "OrgIdNum", column = @Column(name = "OrgIdNum")),
		@AttributeOverride(name = "OrgIdType", column = @Column(name = "OrgIdType")),
		@AttributeOverride(name = "Name", column = @Column(name = "Name")),
		@AttributeOverride(name = "cityCode", column = @Column(name = "cityCode")),
		@AttributeOverride(name = "stateCode", column = @Column(name = "stateCode")),
		@AttributeOverride(name = "countryCode", column = @Column(name = "countryCode"))
//		@AttributeOverride(name = "NameEn", column = @Column(name = "NameEn"))
//		@AttributeOverride(name = "Address", column = @Column(name = "Address")) 
		})
    private OrgRec orgRec;

	@Column(name="OrgIdNum")
	private String OrgIdNum;


    @Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public void setTerminalType(TerminalType terminalType) {
		TerminalType = terminalType;
	}
	
	public void setOrigTerminalType(TerminalType origTerminalType) {
		OrigTerminalType = origTerminalType;
	}

	public void setBankId(String bankId) {
		BankId = bankId;
	}

	public void setFwdBankId(String fwdBankId) {
		FwdBankId = fwdBankId;
	}

	//public Long getFwdToBankId() {
	//	return FwdToBankId;
	//}

	//public void setFwdToBankId(Long fwdToBankId) {
	//	FwdToBankId = fwdToBankId;
	//}

	public String getDestBankId() {
		return DestBankId;
	}

	public void setDestBankId(String destBankId) {
		DestBankId = destBankId;
	}

	public void setNetworkRefId(String networkRefId) {
		NetworkRefId = networkRefId;
	}
	
	public void setMyNetworkRefId(String networkRefId) {
		MyNetworkRefId = networkRefId;
	}

	public void setTerminalId(String terminalId) {
		TerminalId = terminalId;
	}

	
	public String getBankId() {
		return BankId;
	}

	
	public String getFwdBankId() {
		return FwdBankId;
	}

	
	public String getNetworkRefId() {
		return NetworkRefId;
	}

	public String getMyNetworkRefId() {
		return MyNetworkRefId;
	}
	
	public String getRecvBankId() {
		return RecvBankId;
	}

	
	public String getTerminalId() {
		return TerminalId;
	}

	
	public TerminalType getTerminalType() {
		return TerminalType;
	}
	
	public TerminalType getOrigTerminalType() {
		return OrigTerminalType;
	}

	
	public void setRecvBankId(String recvBankId) {
		this.RecvBankId = recvBankId; 
	}

	public void setOrgIdNum(String orgIdNum) {
        OrgIdNum = orgIdNum;
    }

	
	public String getOrgIdNum() {
		return OrgIdNum;
	}

	protected Object clone() {
		NetworkTrnInfo obj = new NetworkTrnInfo();

		if (orgRec != null)
			obj.setOrgRec(orgRec);

		obj.setOrgIdNum(OrgIdNum);
		
		obj.setBankId(BankId);
		obj.setDestBankId(DestBankId);
		obj.setFwdBankId(FwdBankId);
		obj.setRecvBankId(RecvBankId);
		
		obj.setNetworkRefId(NetworkRefId);
		obj.setMyNetworkRefId(MyNetworkRefId);
		
		obj.setTerminalId(TerminalId);
		obj.setTerminalType(TerminalType);
		obj.setOrigTerminalType(OrigTerminalType);
		
		obj.setSrc_TrnSeqCntr(Src_TrnSeqCntr);
		obj.setMy_TrnSeqCntr(My_TrnSeqCntr);
		obj.setLast_TrnSeqCntr(Last_TrnSeqCntr);
		
		obj.setUserLanguage(userLanguage);
		
		obj.setOrigDt(OrigDt);
		obj.setThirdPartyTerminalId(ThirdPartyTerminalId);
		obj.setThirdPartyCode(ThirdPartyCode);

		return obj;
	}
	
	
	public NetworkTrnInfo copy() {
		return (NetworkTrnInfo) clone();
	}

	
	public String getSrc_TrnSeqCntr() {
		return Src_TrnSeqCntr;
	}

	
	public void setSrc_TrnSeqCntr(String src_TrnSeqCntr) {
		Src_TrnSeqCntr = src_TrnSeqCntr;
	}

	
	public String getMy_TrnSeqCntr() {
		return My_TrnSeqCntr;
	}

	
	public void setMy_TrnSeqCntr(String my_TrnSeqCntr) {
		My_TrnSeqCntr = my_TrnSeqCntr;
	}

	
	public DateTime getOrigDt() {
		return OrigDt;
	}

	
	public void setOrigDt(DateTime origDt) {
		OrigDt = origDt;
	}


	public void copyFields(NetworkTrnInfo source) {
		if (source.getOrgRec() != null)
			setOrgRec(source.getOrgRec().copy());

		if (getTerminalId() == null && source.getTerminalId() != null)
			setTerminalId(source.getTerminalId());
		
		if ( (getTerminalType() == null || vaulsys.protocols.ifx.enums.TerminalType.UNKNOWN.equals(getTerminalType()))
				&& 
				source.getTerminalType() != null)
			setTerminalType(source.getTerminalType());
		
		if ( (getOrigTerminalType() == null || vaulsys.protocols.ifx.enums.TerminalType.UNKNOWN.equals(getOrigTerminalType()))
				&& 
				source.getOrigTerminalType() != null)
			setOrigTerminalType(source.getOrigTerminalType());
		
		if (getNetworkRefId() == null || "".equals(getNetworkRefId()))
			setNetworkRefId(source.getNetworkRefId());
		
		setMyNetworkRefId(source.getMyNetworkRefId());
		
		if(getThirdPartyTerminalId() == null && source.getThirdPartyTerminalId() != null)
			setThirdPartyTerminalId(source.getThirdPartyTerminalId());
		
		if(getThirdPartyCode() == null && source.getThirdPartyCode() != null)
			setThirdPartyCode(source.getThirdPartyCode());
		
		if(getLast_TrnSeqCntr() == null && source.getLast_TrnSeqCntr() != null)
			setLast_TrnSeqCntr(source.getLast_TrnSeqCntr());
		
		if(/*getUserLanguage() == null && */source.getUserLanguage() != null)
			setUserLanguage(source.getUserLanguage());
		
		if (source.getRecvBankId()!= null && getRecvBankId()==null)
			setRecvBankId(source.getRecvBankId());
		
		if (source.getOrgIdNum()!= null && getOrgIdNum()==null)
			setOrgIdNum(source.getOrgIdNum());
	}

	public void setUserLanguage(UserLanguage userLanguage) {
		this.userLanguage = userLanguage;
	}

	public UserLanguage getUserLanguage() {
		return userLanguage;
	}

	public Long getThirdPartyTerminalId() {
		return ThirdPartyTerminalId;
	}

	public void setThirdPartyTerminalId(Long thirdPartyTerminalId) {
		ThirdPartyTerminalId = thirdPartyTerminalId;
	}

	public String getLast_TrnSeqCntr() {
		return Last_TrnSeqCntr;
	}

	public void setLast_TrnSeqCntr(String last_TrnSeqCntr) {
		Last_TrnSeqCntr = last_TrnSeqCntr;
	}

	public Long getThirdPartyCode() {
		return ThirdPartyCode;
	}

	public void setThirdPartyCode(Long thirdPartyCode) {
		ThirdPartyCode = thirdPartyCode;
	}
	
	public String getThirdPartyName() {
		return ThirdPartyName;
	}

	public void setThirdPartyName(String thirdPartyName) {
		ThirdPartyName = thirdPartyName;
	}
	
	public String getThirdPartyNameEn() {
		return ThirdPartyNameEn;
	}

	public void setThirdPartyNameEn(String thirdPartyNameEn) {
		ThirdPartyNameEn = thirdPartyNameEn;
	}
	
	public OrgRec getSafeOrgRec() {
		if (this.orgRec == null)
			this.orgRec = new OrgRec();
		return this.orgRec;
	}
	 
	public OrgRec getOrgRec() {
		return orgRec;
	}

	public void setOrgRec(OrgRec orgRec) {
		this.orgRec = orgRec;
	}
}