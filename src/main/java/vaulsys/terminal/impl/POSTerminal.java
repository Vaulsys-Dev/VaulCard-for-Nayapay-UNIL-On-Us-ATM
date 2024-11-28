package vaulsys.terminal.impl;

import vaulsys.auditlog.AuditableProperty;
import vaulsys.auditlog.SimpleProperty;
import vaulsys.contact.Country;
import vaulsys.discount.DiscountProfile;
import vaulsys.entity.impl.Shop;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.POSConnectionType;
import vaulsys.terminal.TerminalClearingMode;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "term_pos")
@ForeignKey(name = "pos_terminal_fk")
public class POSTerminal extends Terminal {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	@ForeignKey(name = "pos_owner_fk")
	private Shop owner;

	@Column(name = "owner", insertable = false, updatable = false)
	private Long ownerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "config")
	@ForeignKey(name = "pos_config_fk")
	private POSConfiguration configuration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dis_prof")
	@ForeignKey(name = "term_disprof_fk")
	private DiscountProfile discountProfile;
	
	@Column(name = "dis_prof", insertable = false, updatable = false)
	private Long discountProfileId;

	public Long getDiscountProfileId() {
		return discountProfileId;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	@Column(length = 100)
	private String description;
	
	@Column(length = 20)
	private String serialno;

	@Column(length = 20, name = "reg_num")
	private String registrationNumber;
	
	@Column(length = 4)
	String resetCode;
	
	@Column(name = "app_ver")
	String applicationVersion;

	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "contype"))
	POSConnectionType connectionType = POSConnectionType.MODEM;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "files_ver")
	@ForeignKey(name = "files_ver_fk")
	private POSSpecificFilesVersion filesVersion;

	@Embedded
	@AttributeOverride(name = "status", column = @Column(name = "updt_status"))
	private UpdateStatus updateStatus;

	@Column(name = "cardacceptornamelocation", length = 40)
	//m.rehman:
	//String cardacceptnamelocation;
	String cardacceptornamelocation;

	//m.rehman: pos terminal type required in reporting
	@Column(name = "pos_terminal_type")
	private Integer posTerminalType;

	//m.rehman: pos manufacturer required in reporting
	@Column(name = "producer")
	private String posProducer;

	//m.rehman: pos model no required in reporting
	@Column(name = "model_no")
	private String posModelNo;

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSafeDescription() {
		return description==null ? "" : description;
	}

	public String getSerialno() {
		return serialno;
	}

	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}

	public String getResetCode() {
		return resetCode;
	}

	public void setResetCode(String resetCode) {
		this.resetCode = resetCode;
	}

	public DiscountProfile getOwnOrParentDiscountProfile() {
		if (discountProfile != null)
			return discountProfile;
		if (sharedFeature != null)
			return sharedFeature.getDiscountProfile();
		return null;
	}

	public DiscountProfile getDiscountProfile() {
		return discountProfile;
	}

	public void setDiscountProfile(DiscountProfile discountProfile) {
		this.discountProfile = discountProfile;
		if(discountProfile != null)
			discountProfileId = discountProfile.getId();
	}

	/******** End ********/
	/**
	 * ***** POS Terminal Version Properties *******
	 */
	public POSTerminal() {
	}

	public POSTerminal(Long code) {
		super(code);
	}

	@Override
	public Shop getOwner() {
		return owner;
	}

	@Override
	public TerminalType getTerminalType() {
		return TerminalType.POS;
	}

	public void setOwner(Shop owner) {
		this.owner = owner;
		if(owner!=null)
			ownerId = owner.getId();
	}

	public TerminalClearingMode getClearingMode() {
		return TerminalClearingMode.TERMINAL;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((resetCode == null) ? 0 : resetCode.hashCode());
		result = prime * result + ((serialno == null) ? 0 : serialno.hashCode());
		result = prime * result + ((registrationNumber == null) ? 0 : registrationNumber.hashCode());
		result = prime * result + ((connectionType == null) ? 0 : connectionType.hashCode());
		//result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((discountProfileId == null) ? 0 : discountProfileId.hashCode());
		//result = prime * result + ((applicationVersion == null) ? 0 : applicationVersion.hashCode());
		//result = prime * result + ((filesVersionI == null) ? 0 : filesVersion.hashCode());
		result = prime * result + ((updateStatus == null) ? 0 : updateStatus.hashCode());
		return result;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public POSConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(POSConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public POSConfiguration getOwnOrParentConfiguration() {
		if (configuration != null)
			return configuration;
		if (sharedFeature != null)
			return sharedFeature.getPosConfiguration();
		return null;
//		return configuration;
	}

	public POSConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(POSConfiguration configuration) {
		this.configuration = configuration;
	}

	public POSSpecificFilesVersion getFilesVersion() {
		return filesVersion;
	}

	public void setFilesVersion(POSSpecificFilesVersion filesVersion) {
		this.filesVersion = filesVersion;
	}
	
	public POSGroupFilesVersion getGroupFilesVersion() {
		return sharedFeature!=null ? sharedFeature.getPosFilesVersion() : null;
	}

	public UpdateStatus getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(UpdateStatus updateStatus) {
		this.updateStatus = updateStatus;
	}

	/* Added by : Asim Shahzad, Date : 20th October 2016, Desc : To add Card Acceptor Name Location from UI */
	@Transient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "country")
	@ForeignKey(name = "pos_country_fk")
	Country country;

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	public String getcardacceptornamelocation() //Raza Adding for Field 43 as TPSP require it in English
	{
		return cardacceptornamelocation;
	}
	public void setcardacceptornamelocation(String cardacceptornamelocation)
	{
		this.cardacceptornamelocation = cardacceptornamelocation;
	}
	@Transient
	@Column(length = 12)
	private String cityenglishname;

	public String getCityenglishname()
	{
		return this.cityenglishname;
	}

	public void setCityenglishname(String cityenglishname)
	{
		this.cityenglishname = cityenglishname;
	}

	public Integer getPosTerminalType() {
		return posTerminalType;
	}

	public void setPosTerminalType(Integer posTerminalType) {
		this.posTerminalType = posTerminalType;
	}

	public String getPosProducer() {
		return posProducer;
	}

	public void setPosProducer(String posProducer) {
		this.posProducer = posProducer;
	}

	public String getPosModelNo() {
		return posModelNo;
	}

	public void setPosModelNo(String posModelNo) {
		this.posModelNo = posModelNo;
	}

	@Override
	public List<AuditableProperty> getAuditableFields() {
		List<AuditableProperty> props = new ArrayList<AuditableProperty>();
		props.addAll(super.getAuditableFields());
		props.add(new SimpleProperty("owner"));
		props.add(new SimpleProperty("serialno"));
		props.add(new SimpleProperty("configuration"));
		props.add(new SimpleProperty("discountProfile"));
		props.add(new SimpleProperty("filesVersion"));
		props.add(new SimpleProperty("updateStatus"));
		props.add(new SimpleProperty("applicationVersion"));
		props.add(new SimpleProperty("registrationNumber"));		
		return props;
	}
}