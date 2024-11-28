package vaulsys.terminal.impl;

import vaulsys.auditlog.AuditableProperty;
import vaulsys.auditlog.SimpleProperty;
import vaulsys.contact.Country;
import vaulsys.entity.impl.Branch;
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
@Table(name = "term_pinpad")
@ForeignKey(name = "pinpad_terminal_fk")
public class PINPADTerminal extends Terminal {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	@ForeignKey(name = "pinpad_owner_fk")
	private Branch owner;

	@Column(name = "owner", insertable = false, updatable = false)
	private Long ownerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "config")
	@ForeignKey(name = "pinpad_config_fk")
	private POSConfiguration configuration;

	public Long getOwnerId() {
		return ownerId;
	}

	@Column(length = 100)
	private String description;
	
	@Column(length = 10)
	private String serialno;

	@Column(length = 20, name = "reg_num")
	private String registrationNumber;
	
	@Column(length = 4)
	private String resetCode;
	
	@Column(name = "app_ver")
	private String applicationVersion;

	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "contype"))
	private POSConnectionType connectionType = POSConnectionType.LAN;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "files_ver")
	@ForeignKey(name = "files_ver_fk")
	private POSSpecificFilesVersion filesVersion;

	@Embedded
	@AttributeOverride(name = "status", column = @Column(name = "updt_status"))
	private UpdateStatus updateStatus;

	@Column(name = "cardacceptornamelocation", length = 40)
	String cardacceptnamelocation;

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
	/******** End ********/
	/**
	 * ***** POS Terminal Version Properties *******
	 */
	public PINPADTerminal() {
	}

	public PINPADTerminal(Long code) {
		super(code);
	}

	@Override
	public Branch getOwner() {
		return owner;
	}

	@Override
	public TerminalType getTerminalType() {
		return TerminalType.PINPAD;
	}

	public void setOwner(Branch owner) {
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
	@ForeignKey(name = "pinpad_country_fk")
	Country country;

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	public String getcardacceptornamelocation() //Raza Adding for Field 43 as TPSP require it in English ; not using current columns as they are used by Shetab etc
	{
		return cardacceptnamelocation;
	}
	public void setcardacceptornamelocation(String cardacceptornamelocation)
	{
		this.cardacceptnamelocation = cardacceptornamelocation;
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
	@Override
	public List<AuditableProperty> getAuditableFields() {
		List<AuditableProperty> props = new ArrayList<AuditableProperty>();
		props.addAll(super.getAuditableFields());
		props.add(new SimpleProperty("owner"));
		props.add(new SimpleProperty("configuration"));
		props.add(new SimpleProperty("description"));
		props.add(new SimpleProperty("serialno"));
		props.add(new SimpleProperty("registrationNumber"));
		props.add(new SimpleProperty("resetCode"));
		props.add(new SimpleProperty("applicationVersion"));
		props.add(new SimpleProperty("connectionType"));
		return props;
	}
}