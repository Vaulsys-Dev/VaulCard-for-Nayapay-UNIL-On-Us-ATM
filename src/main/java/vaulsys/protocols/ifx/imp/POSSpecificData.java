package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;
import vaulsys.util.Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ifx_pos_specific")
public class POSSpecificData implements IEntity<Long>, Cloneable {
	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="posspecificdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "posspecificdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "posspecificdata_seq")
    				})
	private Long id;

	@Column(name = "app_ver")
	private String ApplicationVersion;

	@Column(name = "reset_pass")
	private String ResetingPassword;

	@Column(name = "dial_ind")
	private Integer dialIndicator;

	@Column(name = "mrch_unstdl_amt")
	private Long merchantUnsettledAmount;
	
	@Column(name = "updt_rqrd")
	private Boolean updateRequired;
	
	@Column(name = "updt_recpt_rqrd")
	private Boolean updateReceiptRequired;

	@Column(length = 20)
	private String serialno;

	@Column(name = "cnfrm_code")
	private Integer confirmationCode;

	@Column(name = "extra_info")
	private String extraInfo;

	private String ANI;
    
	private String DNIS;
    
	private String LRI;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getSerialno() {
		return serialno;
	}

	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}

	public String getApplicationVersion() {
		return ApplicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		ApplicationVersion = applicationVersion;
	}

	public String getResetingPassword() {
		return ResetingPassword;
	}

	public void setResetingPassword(String resetingPassword) {
		this.ResetingPassword = resetingPassword;
	}

	public Integer getDialIndicator() {
		return dialIndicator;
	}

	public void setDialIndicator(Integer dialIndicator) {
		this.dialIndicator = dialIndicator;
	}

	public Long getMerchantUnsettledAmount() {
		return merchantUnsettledAmount;
	}

	public void setMerchantUnsettledAmount(Long merchantUnsettledAmount) {
		this.merchantUnsettledAmount = merchantUnsettledAmount;
	}

	public Boolean getUpdateRequired() {
		return updateRequired;
	}
	
	public void setUpdateRequired(Boolean updateRequired) {
		this.updateRequired = updateRequired;
	}

	public Boolean getUpdateReceiptRequired() {
		return updateReceiptRequired;
	}

	public void setUpdateReceiptRequired(Boolean updateReceiptRequired) {
		this.updateReceiptRequired = updateReceiptRequired;
	}
	
	public Integer getConfirmationCode() {
		return confirmationCode;
	}

	public void setConfirmationCode(Integer confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	public String getANI() {
		return ANI;
	}

	public void setANI(String aNI) {
		ANI = aNI;
	}

	public String getDNIS() {
		return DNIS;
	}

	public void setDNIS(String dNIS) {
		DNIS = dNIS;
	}

	public String getLRI() {
		return LRI;
	}

	public void setLRI(String lRI) {
		LRI = lRI;
	}

	protected Object clone() {
		POSSpecificData obj = new POSSpecificData();
		obj.setApplicationVersion(ApplicationVersion);
		obj.setResetingPassword(ResetingPassword);
		obj.setDialIndicator(dialIndicator);
		obj.setUpdateRequired(updateRequired);
		obj.setUpdateReceiptRequired(updateReceiptRequired);
		obj.setMerchantUnsettledAmount(merchantUnsettledAmount);
		obj.setSerialno(serialno);
		obj.setConfirmationCode(confirmationCode);
		obj.setExtraInfo(extraInfo);

		obj.setANI(ANI);
		obj.setDNIS(DNIS);
		obj.setLRI(LRI);
		return obj;
	}

	public POSSpecificData copy() {
		return (POSSpecificData) clone();
	}

	public void copyFields(POSSpecificData source) {
		if (!Util.hasText(getApplicationVersion()) && Util.hasText(source.getApplicationVersion()))
			setApplicationVersion(source.getApplicationVersion());
		if (getResetingPassword() == null)
			setResetingPassword(source.getResetingPassword());
		if (getDialIndicator() == null && source.getDialIndicator() != null)
			setDialIndicator(source.getDialIndicator());
		if(getUpdateRequired() == null)
			setUpdateRequired(source.getUpdateRequired());
		if(getUpdateReceiptRequired() == null)
			setUpdateReceiptRequired(source.getUpdateReceiptRequired());
		if(getMerchantUnsettledAmount() == null && source.getMerchantUnsettledAmount() != null)
			setMerchantUnsettledAmount(source.getMerchantUnsettledAmount());
		if (getSerialno() == null)
			setSerialno(source.getSerialno());
		if(getConfirmationCode() == null)
			setConfirmationCode(source.getConfirmationCode());
		if(getExtraInfo() == null)
			setExtraInfo(source.getExtraInfo());

		if(getANI() == null)
			setANI(source.getANI());
		if(getDNIS() == null)
			setDNIS(source.getDNIS());
		if(getLRI() == null)
			setLRI(source.getLRI());
	}
}
