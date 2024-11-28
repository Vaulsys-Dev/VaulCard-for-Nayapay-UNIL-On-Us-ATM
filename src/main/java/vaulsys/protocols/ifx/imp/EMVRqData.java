/**
 *
 */
package vaulsys.protocols.ifx.imp;

import vaulsys.calendar.DateTime;
import vaulsys.cms.base.CMSCardAuthorizationFlags;
import vaulsys.cms.base.CMSCardLimit;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.util.Util;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "ifx_EMV_Rq_Data")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class EMVRqData implements IEntity<Long>, Cloneable {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="emvrqdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "emvrqdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "emvrqdata_seq")
    				})
	private Long id;

//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "cardacct")
//	@Cascade(value = { CascadeType.ALL })
//	@ForeignKey(name="emvrqdata_cardacc_fk")    
	@Embedded
	@AttributeOverrides( { 
		@AttributeOverride(name = "ExpDt", column = @Column(name = "ExpDt")),
//		@AttributeOverride(name = "PINBlock", column = @Column(name = "PINBlock")),
		@AttributeOverride(name = "AppPAN", column = @Column(name = "AppPAN")),
		@AttributeOverride(name = "CVV2", column = @Column(name = "CVV2")),
		@AttributeOverride(name = "Trk2EquivData", column = @Column(name = "Track2")),
		@AttributeOverride(name = "cardHolderName", column = @Column(name = "holderName")),
		@AttributeOverride(name = "cardHolderFamily", column = @Column(name = "HolderFamily")),
		@AttributeOverride(name = "actualAppPAN", column = @Column(name = "ActualAppPAN")) })
	private CardAcctId CardAcctId;

//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "billdata")
//	@Cascade(value = { CascadeType.ALL })
//	@ForeignKey(name="emvrqdata_billdata_fk")
	@Embedded
	@AttributeOverrides( { 
		@AttributeOverride(name = "billID", column = @Column(name = "billID")),
		@AttributeOverride(name = "billPaymentID", column = @Column(name = "billPaymentID")),
		@AttributeOverride(name = "billCompanyCode", column = @Column(name = "billCompanyCode")),
		@AttributeOverride(name = "billUnParsedData", column = @Column(name = "billUnParsedData")) })
	private BillPaymentData billPaymentData;
	
//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "epaydata")
//	@Cascade(value = { CascadeType.ALL })
//	@ForeignKey(name="emvrqdata_epaydata_fk")
	@Embedded
	@AttributeOverrides( { 
		@AttributeOverride(name = "invoiceNumber", column = @Column(name = "invoiceNumber")),
		@AttributeOverride(name = "email", column = @Column(name = "email")),
		@AttributeOverride(name = "IP", column = @Column(name = "IP")),
		@AttributeOverride(name = "invoiceDate", column = @Column(name = "invoiceDate")) })
	private EPaymentData paymentData;
	
	@Transient
	private transient String NewPINBlock;
	
	@Transient
	private transient String OldPINBlock;
	
	@Transient
	private transient String PINBlock;
	
	@Embedded
	private AccType AccTypeFrom = AccType.UNKNOWN;

	@Embedded
	private AccType AccTypeTo = AccType.UNKNOWN;

	private Long Auth_Amt;
	
	private Long Real_Amt;
	
	private Long Trx_Amt;

	private Long Sett_Amt; //Raza MASTERCARD DE-5
	
	private Integer Auth_Currency;
	private String Auth_CurRate;


	private Long Sec_Amt;
	private String ConvRateSett; //Raza MasterCard DE-9
	private Integer Sec_Currency;

	private String Sec_CurRate;

	private String Sec_CurDate; //Raza MasterCard DE-16

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "dayDate.date", column = @Column(name = "trn_date")),
			@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "trn_time")) })
	private DateTime TrnDt;

	@Transient
	private transient String MsgAuthCode;

	private String Sett_Currency; //Raza MASTERCARD DE-50

	private String New_AmtAcqCur; // S-95

	private String New_AmtIssCur; // S-95
	
	private String secondAppPan;
	private String actualSecondAppPan;
	
	private String subsidiaryAccTo;
	private String subsidiaryAccFrom;

	 @Transient
	    private transient String cardHolderMobileNo;

	//m.rehman: for authorization flags
	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "isAuthRequiredFlag", column = @Column(name = "auth_required")),
			@AttributeOverride(name = "isPINRequiredFlag", column = @Column(name = "pin_required")),
			@AttributeOverride(name = "isCVVRequiredFlag", column = @Column(name = "cvv_required")),
			@AttributeOverride(name = "isCVV2RequiredFlag", column = @Column(name = "cvv2_required")),
			@AttributeOverride(name = "isARQCRequiredFlag", column = @Column(name = "arqc_required")),
			@AttributeOverride(name = "isARPCRequiredFlag", column = @Column(name = "arpc_required"))})
	private CMSCardAuthorizationFlags cardAuthorizationFlags;

	//m.rehman: for limits
	@Transient
	private CMSCardLimit cardLimit;

	//m.rehman: check for in availablility
	@Column(name = "pinblock")
	private String isPinAvailable;

	public String getCardHolderMobileNo() {
		return cardHolderMobileNo;
	}

	public void setCardHolderMobileNo(String cardHolderMobileNo) {
		this.cardHolderMobileNo = cardHolderMobileNo;
	}

	public Long getId() {
		return id;
	}

	
	public void setId(Long id) {
		this.id = id;
	}

	public AccType getAccTypeFrom() {
		return AccTypeFrom;
	}

	public AccType getAccTypeTo() {
		return AccTypeTo;
	}

	
	public Long getAuth_Amt() {
		return Auth_Amt;
	}

	
	public Integer getAuth_Currency() {
		return Auth_Currency;
	}

	public String getSett_Currency() {
		return Sett_Currency;
	} //Raza MASTERCARD

	public String getAuth_CurRate() {
		return Auth_CurRate;
	}

	
	public String getMsgAuthCode() {
		return MsgAuthCode;
	}

	
	public Long getSec_Amt() {
		return Sec_Amt;
	}

	public String getConvRate_Sett() {
		return ConvRateSett;
	}

	
	public Integer getSec_Currency() {
		return Sec_Currency;
	}

	
	public String getSec_CurRate() {
		return Sec_CurRate;
	}

	public String getSec_CurDate() {
		return Sec_CurDate;
	} //Raza MasterCard for DE-16

	
	public DateTime getTrnDt() {
		return TrnDt;
	}

	
	public void setAccTypeFrom(AccType accTypeFrom) {
		this.AccTypeFrom = accTypeFrom;
	}

	
	public void setAccTypeTo(AccType AccTypeTo) {
		this.AccTypeTo = AccTypeTo;
	}

	
	public void setAuth_Amt(Long auth_Amt) {
        this.Auth_Amt = auth_Amt;
	}

	
	public void setAuth_Currency(Integer auth_Code) {
		this.Auth_Currency = auth_Code;
	}

	public void setSett_Currency(String sett_Code) { //Raza MASTERCARD
		this.Sett_Currency = sett_Code;
	}

	public void setAuth_CurRate(String auth_CurRate) {
		this.Auth_CurRate = auth_CurRate;
	}

	
	public void setMsgAuthCode(String msgAuthCode) {
		this.MsgAuthCode = msgAuthCode;
	}

	
	public void setSec_Amt(Long sec_Amt) {
        this.Sec_Amt = sec_Amt;
	}

	public void setConvRate_Sett(String ConvRateSett) { //Raza MASTERCARD
		this.ConvRateSett = ConvRateSett;
	}

	public void setSec_Currency(Integer sec_CurCode) {
		this.Sec_Currency = sec_CurCode;
	}


	public void setSec_CurRate(String sec_CurRate) {
		this.Sec_CurRate = sec_CurRate;
	}

	public void setSec_CurDate(String sec_CurDate) { //Raza MasterCard for DE-16
		this.Sec_CurDate = sec_CurDate;
	}

	
	public void setTrnDt(DateTime trnDt) {
		this.TrnDt = trnDt;
	}

	
	protected Object clone() {
		EMVRqData obj = new EMVRqData();
		
		if (getCardAcctId() != null)
			obj.setCardAcctId(getCardAcctId().copy());
		
		if(getBillPaymentData() != null)
			obj.setBillPaymentData(getBillPaymentData().copy());
		
		if(getPaymentData() != null)
			obj.setPaymentData(getPaymentData().copy());
		
//		if(creditStatementData != null)
//			obj.setCreditStatementData(creditStatementData.copy());
		
		obj.setAccTypeFrom(getAccTypeFrom().copy());
		obj.setAccTypeTo(getAccTypeTo().copy());
		obj.setAuth_Amt(Auth_Amt);
		obj.setReal_Amt(Real_Amt);
		obj.setTrx_Amt(Trx_Amt);
		obj.setSett_Amt(Sett_Amt);
		obj.setAuth_Currency(Auth_Currency); //Raza MASTERCARD
		obj.setSett_Currency(Sett_Currency);
		obj.setAuth_CurRate(Auth_CurRate);
		obj.setMsgAuthCode(MsgAuthCode);
		obj.setSec_Amt(Sec_Amt);
		obj.setConvRate_Sett(ConvRateSett); //Raza MASTERCARD
		obj.setSec_Currency(Sec_Currency);
		obj.setSec_CurRate(Sec_CurRate);
		obj.setTrnDt(TrnDt);
		obj.setNew_AmtAcqCur(New_AmtAcqCur);
		obj.setNew_AmtIssCur(New_AmtIssCur);
		obj.setSecondAppPan(secondAppPan);
		obj.setActualSecondAppPAN(actualSecondAppPan);
		obj.setNewPINBlock(NewPINBlock);
		obj.setOldPINBlock(OldPINBlock);
		obj.setSubsidiaryAccTo(subsidiaryAccTo);
		obj.setSubsidiaryAccFrom(subsidiaryAccFrom);
		obj.setPINBlock(PINBlock);
		obj.setCardHolderMobileNo(cardHolderMobileNo);
//		obj.setSecondAcctId(secondAcctId);
		obj.setIsPinAvailable(isPinAvailable);
		return obj;
	}

	public EMVRqData copy() {
		return (EMVRqData) clone();
	}


	public CardAcctId getCardAcctId() {
		return CardAcctId;
	}
	
	public CardAcctId getSafeCardAcctId() {
		if (this.CardAcctId == null)
			this.CardAcctId = new CardAcctId();
		return CardAcctId;
	}

	
	public void setCardAcctId(CardAcctId cardAcctId) {
		CardAcctId = cardAcctId;
	}

	
	public String getNew_AmtAcqCur() {
		return New_AmtAcqCur;
	}

	
	public void setNew_AmtAcqCur(String new_AmtAcqCur) {
		New_AmtAcqCur = new_AmtAcqCur;
	}

	
	public String getNew_AmtIssCur() {
		return New_AmtIssCur;
	}

	
	public void setNew_AmtIssCur(String new_AmtIssCur) {
		New_AmtIssCur = new_AmtIssCur;
	}

	
	public String getSecondAppPan() {
		return secondAppPan;
	}

	
	public void setSecondAppPan(String secondAppPan) {
		this.secondAppPan = secondAppPan;
	}

	public BillPaymentData getBillPaymentData() {
		return billPaymentData;
	}
	
	public BillPaymentData getSafeBillPaymentData() {
		if(billPaymentData == null)
			billPaymentData = new BillPaymentData();
		return billPaymentData;
	}
	
	public void setBillPaymentData(BillPaymentData billPaymentData) {
		this.billPaymentData = billPaymentData;
	}
	
	public void copyFields(EMVRqData source) {
		
		if (source.getCardAcctId() != null)
			getSafeCardAcctId().copyFields(source.getCardAcctId());
		
		if (source.getBillPaymentData() != null)
//			setBillPaymentData(source.getBillPaymentData());
			getSafeBillPaymentData().copyFields(source.getBillPaymentData());

		if (source.getPaymentData() != null)
//			setPaymentData(source.getPaymentData());
			getSafePaymentData().copyFields(source.getPaymentData());
		
//		if (source.getCreditStatementData() != null)
//			getSafeCreditStatementData().copyFields(source.getCreditStatementData());
		
		if (!Util.hasText(getSecondAppPan()) && Util.hasText(source.getSecondAppPan()) )
			setSecondAppPan(source.getSecondAppPan());
		
		if (!Util.hasText(getActualSecondAppPan()) && Util.hasText(source.getActualSecondAppPan()) )
			setActualSecondAppPAN(source.getActualSecondAppPan());
		
		if (!Util.hasText(getNewPINBlock()) && Util.hasText(source.getNewPINBlock()) )
			setNewPINBlock(source.getNewPINBlock());

		if (!Util.hasText(getOldPINBlock()) && Util.hasText(source.getOldPINBlock()) )
			setOldPINBlock(source.getOldPINBlock());
		
		if (!Util.hasText(getPINBlock()) && Util.hasText(source.getPINBlock()))
			setPINBlock(source.getPINBlock());

		if (getAccTypeTo() == null && source.getAccTypeTo() != null && !source.getAccTypeTo().equals(AccType.UNKNOWN))
			setAccTypeTo(source.getAccTypeTo());
		
		if (getAccTypeFrom() == null && source.getAccTypeFrom() != null && !source.getAccTypeFrom().equals(AccType.UNKNOWN))
			setAccTypeFrom(source.getAccTypeFrom());
		
		if (getAuth_Amt() == null && source.getAuth_Amt() != null)
			setAuth_Amt(source.getAuth_Amt());
		
		if ((getAuth_Amt() != null && getAuth_Amt().equals(0L)) && 
				(source.getAuth_Amt() != null && !source.getAuth_Amt().equals(0L)))
			setAuth_Amt(source.getAuth_Amt());
		
		if (getReal_Amt() == null && source.getReal_Amt()!= null)
			setReal_Amt(source.getReal_Amt());
		
		if ((getReal_Amt() != null && getReal_Amt().equals(0L)) && 
				(source.getReal_Amt() != null && !source.getReal_Amt().equals(0L)))
			setReal_Amt(source.getReal_Amt());
		
		if (getTrx_Amt() == null && source.getTrx_Amt()!= null)
			setTrx_Amt(source.getTrx_Amt());

		if (getSett_Amt() == null && source.getSett_Amt()!= null) //Raza MASTERCARD
			setSett_Amt(source.getSett_Amt());
		
		if ((getTrx_Amt() != null && getTrx_Amt().equals(0L)) && 
				(source.getTrx_Amt() != null && !source.getTrx_Amt().equals(0L)))
			setTrx_Amt(source.getTrx_Amt());

		if ((getSett_Amt() != null && getSett_Amt().equals(0L)) && //Raza MASTERCARD
				(source.getSett_Amt() != null && !source.getSett_Amt().equals(0L)))
			setSett_Amt(source.getSett_Amt());
		
		if (!Util.hasText(getSubsidiaryAccTo()) && Util.hasText(source.getSubsidiaryAccTo()))
			setSubsidiaryAccTo(source.getSubsidiaryAccTo()); 
			
		if (!Util.hasText(getSubsidiaryAccFrom()) && Util.hasText(source.getSubsidiaryAccFrom()))
			setSubsidiaryAccFrom(source.getSubsidiaryAccFrom()); 
		
		
		if (!Util.hasText(getCardHolderMobileNo()) && Util.hasText(source.getCardHolderMobileNo()) )
			setCardHolderMobileNo(source.getCardHolderMobileNo());
		//m.rehman: copying currency and rate if available
		setAuth_Currency(source.getAuth_Currency());

		if (source.getSec_Currency() != null)
			this.setSec_Currency(source.getSec_Currency());

		if (Util.hasText((source.getSec_CurRate())))
			this.setSec_CurRate(source.getSec_CurRate());
	}

	public EPaymentData getPaymentData() {
		return paymentData;
	}
	
	public EPaymentData getSafePaymentData() {
		if (paymentData == null)
			paymentData = new EPaymentData();
		return paymentData;
	}


	public void setPaymentData(EPaymentData paymentData) {
		this.paymentData = paymentData;
	}


	public void setNewPINBlock(String newPINBlock) {
		NewPINBlock = newPINBlock;
	}


	public String getNewPINBlock() {
		return NewPINBlock;
	}

	public String getOldPINBlock() {
		return OldPINBlock;
	}


	public void setOldPINBlock(String oldPINBlock) {
		OldPINBlock = oldPINBlock;
	}


	public Long getReal_Amt() {
		return Real_Amt;
	}


	public void setReal_Amt(Long real_Amt) {
		Real_Amt = real_Amt;
	}


	public String getSubsidiaryAccTo() {
		return subsidiaryAccTo;
	}


	public void setSubsidiaryAccTo(String subsidiaryAccTo) {
		this.subsidiaryAccTo = subsidiaryAccTo;
	}


	public String getSubsidiaryAccFrom() {
		return subsidiaryAccFrom;
	}


	public void setSubsidiaryAccFrom(String subsidiaryAccFrom) {
		this.subsidiaryAccFrom = subsidiaryAccFrom;
	}


	public String getActualSecondAppPan() {
		return actualSecondAppPan;
	}


	public void setActualSecondAppPAN(String actualSecondAppPan) {
		this.actualSecondAppPan = actualSecondAppPan;
	}
	
	public void setPINBlock(String block) {
		this.PINBlock = block;
	}
	
	public String getPINBlock() {
		return this.PINBlock;
	}

	public Long getTrx_Amt() {
		return Trx_Amt;
	}

	public Long getSett_Amt() {
		return Sett_Amt;
	} //Raza MASTERCARD

	public void setTrx_Amt(Long trx_Amt) {
		Trx_Amt = trx_Amt;
	}

	public void setSett_Amt(Long sett_Amt) { //Raza MASTERCARD
		Sett_Amt = sett_Amt;
	}

	//m.rehman
	public CMSCardAuthorizationFlags getCardAuthorizationFlags() {
		return cardAuthorizationFlags;
	}

	public void setCardAuthorizationFlags(CMSCardAuthorizationFlags cardAuthorizationFlags) {
		this.cardAuthorizationFlags = cardAuthorizationFlags;
	}

	public CMSCardLimit getCardLimit() {
		return cardLimit;
	}

	public void setCardLimit(CMSCardLimit cardLimit) {
		this.cardLimit = cardLimit;
	}

	public String getIsPinAvailable() {
		return isPinAvailable;
	}

	public void setIsPinAvailable(String isPinAvailable) {
		this.isPinAvailable = isPinAvailable;
	}
}