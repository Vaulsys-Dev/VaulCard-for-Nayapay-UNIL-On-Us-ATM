package vaulsys.migration;

import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="mgr_data")
public class MigrationData implements IEntity<String>, Cloneable{
	
	@Id
	@Column(name="ngn_appPan")
	private String neginAppPan;
	
	@Column(name="ngn_trk2")
	private String neginTrack2;
	
	@Column(name="ngn_cvv2")
	private String neginCVV2;
	
	@Column(name="ngn_expdt")
	private Long neginExpDt;
	
	@Column(name="ngn_firstpinblk")
	private String neginFirstPinBlock;
	
	@Column(name="ngn_secondpinblk")
	private String neginSecondPinBlock; 
	
	@Column(name="fnp_appPan")
	private String fanapAppPan;
	
	@Column(name="fnp_trk2")
	private String fanapTrack2;
	
	@Column(name="fnp_cvv2")
	private String fanapCVV2;
	
	@Column(name="fnp_expdt")
	private Long fanapExpDt;
	
	@Column(name="fnp_firstpinblk")
	private String fanapFirstPinBlock;
	
	@Column(name="fnp_secondpinblk")
	private String fanapSecondPinBlock;
	
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "transfer_status"))
	})
	private CardMigrationTransferStatusType migrationStatus;
	
	@Column(name="pin1_valid")
	private Boolean isValidPin1;
	
	@Column(name="pin2_valid")
	private Boolean isValidPin2;
	
	@Column(name="pin1_trxdate")
	private Long pin1TransactionDate;
	
	@Column(name="pin1_trxid")
	private Long pin1TransactionId;
	
	@Column(name="pin1_rscode")	
	private String pin1RsCode;
	
	
	@Column(name="pin2_trxdate")
	private Long pin2TransactionDate;
	
	@Column(name="pin2_trxid")
	private Long pin2TransactionId;
	
	@Column(name="pin2_rscode")	
	private String pin2RsCode;
	
	private Boolean sendToNegin = false;
	
	@Column(name = "chg_flg_trx")
	private Long changedFlagTrxId;
	
	@Column(name = "chg_flg_date")
	private Long changedFlagDate;
	

	public String getId() {
		return this.getNeginAppPan();
	}

	public void setId(String id) {
		this.setNeginAppPan(id);
	}

	public String getFanapAppPan() {
		return fanapAppPan;
	}

	public void setFanapAppPan(String fanapAppPan) {
		this.fanapAppPan=fanapAppPan;
	}

	public String getNeginAppPan() {
		return neginAppPan;
	}

	public void setNeginAppPan(String neginAppPan) {
		this.neginAppPan = neginAppPan;
	}
	
	public String getFanapTrack2() {
		return fanapTrack2;
	}

	public void setFanapTrack2(String fanapTrack2) {
		this.fanapTrack2 = fanapTrack2;
	}

	public String getNeginTrack2() {
		return neginTrack2;
	}

	public void setNeginTrack2(String neginTrack2) {
		this.neginTrack2 = neginTrack2;
	}

	public String getFanapCVV2() {
		return fanapCVV2;
	}

	public void setFanapCVV2(String fanapCVV2) {
		this.fanapCVV2 = fanapCVV2;
	}

	public String getNeginCVV2() {
		return neginCVV2;
	}

	public void setNeginCVV2(String neginCVV2) {
		this.neginCVV2 = neginCVV2;
	}

	public Long getFanapExpDt() {
		return fanapExpDt;
	}

	public void setFanapExpDt(Long fanapExpDt) {
		this.fanapExpDt = fanapExpDt;
	}

	public Long getNeginExpDt() {
		return neginExpDt;
	}

	public void setNeginExpDt(Long neginExpDt) {
		this.neginExpDt = neginExpDt;
	}

	public Long getPin1TransactionDate() {
		return pin1TransactionDate;
	}

	public void setPin1TransactionDate(Long pin1TransactionDate) {
		this.pin1TransactionDate = pin1TransactionDate;
	}

	public Long getPin1TransactionId() {
		return pin1TransactionId;
	}

	public void setPin1TransactionId(Long pin1TransactionId) {
		this.pin1TransactionId = pin1TransactionId;
	}

	public Long getPin2TransactionDate() {
		return pin2TransactionDate;
	}

	public void setPin2TransactionDate(Long pin2TransactionDate) {
		this.pin2TransactionDate = pin2TransactionDate;
	}

	public Long getPin2TransactionId() {
		return pin2TransactionId;
	}

	public void setPin2TransactionId(Long pin2TransactionId) {
		this.pin2TransactionId = pin2TransactionId;
	}

	

	public String getPin1RsCode() {
		return pin1RsCode;
	}

	public void setPin1RsCode(String pin1RsCode) {
		this.pin1RsCode = pin1RsCode;
	}

	public String getPin2RsCode() {
		return pin2RsCode;
	}

	public void setPin2RsCode(String pin2RsCode) {
		this.pin2RsCode = pin2RsCode;
	}

	public Boolean getSendToNegin() {
		if(sendToNegin==null)
			return true;
		return sendToNegin;
	}

	public void setSendToNegin(Boolean sendToNegin) {
		this.sendToNegin = sendToNegin;
	}

	public Long getChangedFlagTrxId() {
		return changedFlagTrxId;
	}

	public void setChangedFlagTrxId(Long changedFlagTrxId) {
		this.changedFlagTrxId = changedFlagTrxId;
	}

	public Long getChangedFlagDate() {
		return changedFlagDate;
	}

	public void setChangedFlagDate(Long changedFlagDate) {
		this.changedFlagDate = changedFlagDate;
	}

	public Boolean getIsValidPin1() {
		if(isValidPin1 == null)
			return false;
		return isValidPin1;
	}

	public void setIsValidPin1(Boolean isValidPin1) {
		this.isValidPin1 = isValidPin1;
	}

	public Boolean getIsValidPin2() {
		if(isValidPin2 == null)
			return false;
		return isValidPin2;
	}

	public void setIsValidPin2(Boolean isValidPin2) {
		this.isValidPin2 = isValidPin2;
	}

	public CardMigrationTransferStatusType getMigrationStatus() {
		return migrationStatus;
	}

	public void setMigrationStatus(CardMigrationTransferStatusType migrationStatus) {
		this.migrationStatus = migrationStatus;
	}

	public String getFanapFirstPinBlock() {
		return fanapFirstPinBlock;
	}

	public void setFanapFirstPinBlock(String fanapFirstPinBlock) {
		this.fanapFirstPinBlock = fanapFirstPinBlock;
	}

	public String getFanapSecondPinBlock() {
		return fanapSecondPinBlock;
	}

	public void setFanapSecondPinBlock(String fanapSecondPinBlock) {
		this.fanapSecondPinBlock = fanapSecondPinBlock;
	}

	public String getNeginFirstPinBlock() {
		return neginFirstPinBlock;
	}

	public void setNeginFirstPinBlock(String neginFirstPinBlock) {
		this.neginFirstPinBlock = neginFirstPinBlock;
	}

	public String getNeginSecondPinBlock() {
		return neginSecondPinBlock;
	}

	public void setNeginSecondPinBlock(String neginSecondPinBlock) {
		this.neginSecondPinBlock = neginSecondPinBlock;
	}
	

	
}
