package vaulsys.protocols.ifx.imp;

import java.io.Serializable;

import vaulsys.util.Util;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

//@Entity
//@Table(name = "ifx_card_Acct_Id")
@Embeddable
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class CardAcctId implements Serializable, Cloneable {

//    @Id
//    @GeneratedValue(generator="switch-gen")
//	private Long id;

	//m.rehman: need expiry date in response message
	//@Transient
	//private transient Long ExpDt;
	private Long ExpDt;

//	private String PINBlock;

	private String AppPAN;

	//m.rehman: adding support for cvv and service code
	@Transient
	private transient String CVV;

	@Transient
	private transient String serviceCode;

	@Transient
	private transient String CVV2;
	
	@Transient
	private transient String Trk2EquivData;

	//m.rehman: adding support for Track 1 and Track 3 Data
	@Transient
	private transient String Track1Data;
	@Transient
	private transient String Track3Data;
	
	private String cardHolderName;
	
	private String cardHolderFamily;
	
	private String actualAppPAN;
	
//	@Override
//	public Long getId() {
//		return this.id;
//	}
//
//	@Override
//	public void setId(Long id) {
//		this.id = id;
//	}
		
    public void setExpDt(Long expDt) {
        ExpDt = expDt;
    }

    
    public Long getExpDt() {
    	return this.ExpDt;
    }
    
    
//	public void setPINBlock(String block) {
//		this.PINBlock = block;
//	}

	
//	public String getPINBlock() {
//		return this.PINBlock;
//	}

	
	public String getAppPAN() {
		return this.AppPAN;
	}

	
	public void setAppPAN(String appPAN) {
		this.AppPAN = appPAN;
	}

	
	protected Object clone() {
		CardAcctId obj = new CardAcctId();
		obj.setAppPAN(AppPAN);
//		obj.setAcctId(AcctId);
//		obj.setSubsidiaryAcct(subsidiaryAcct);
		obj.setExpDt(ExpDt);
//		obj.setPINBlock(PINBlock);
		obj.setCVV2(CVV2);
		obj.setTrk2EquivData(Trk2EquivData);
		obj.setCardHolderFamily(cardHolderFamily);
		obj.setCardHolderName(cardHolderName);
		obj.setActualAppPAN(actualAppPAN);
		//m.rehman
		obj.setTrack1Data(Track1Data);
		obj.setTrack3Data(Track3Data);
		
		return obj;
	}
	
	
	public CardAcctId copy() {
		return (CardAcctId) clone();
	}

	
	public String getCVV2() {
		if(CVV2 == null || CVV2.isEmpty())
			return "0000";
		return CVV2;
	}

	
	public void setCVV2(String cvv2) {
		CVV2 = cvv2;
	}

	
	public String getCardHolderFamily() {
		return this.cardHolderFamily;
	}

	
	public String getCardHolderName() {
		return this.cardHolderName;
	}

	
	public void setCardHolderFamily(String family) {
		this.cardHolderFamily = family;
	}

	
	public void setCardHolderName(String name) {
		this.cardHolderName = name;
	}
	
	
	public String getTrk2EquivData() {
		return Trk2EquivData;
	}
	
	
	public void setTrk2EquivData(String trk2EquivData) {
		this.Trk2EquivData = trk2EquivData;
	}

	//m.rehman
	public String getTrack1Data() {
		return Track1Data;
	}

	public void setTrack1Data(String track1Data) {
		this.Track1Data = track1Data;
	}

	public String getTrack3Data() {
		return Track3Data;
	}

	public void setTrack3Data(String track3Data) {
		this.Track3Data = track3Data;
	}

	public String getCVV() {
		return CVV;
	}

	public void setCVV(String CVV) {
		this.CVV = CVV;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public void copyFields(CardAcctId source) {
		if (!Util.hasText(getTrk2EquivData()) && Util.hasText(source.getTrk2EquivData()))
			setTrk2EquivData(source.getTrk2EquivData());
		
		if (!Util.hasText(getCardHolderFamily()) && Util.hasText(source.getCardHolderFamily()))
			setCardHolderFamily(source.getCardHolderFamily());
		
		if (!Util.hasText(getCardHolderName()) && Util.hasText(source.getCardHolderName()))
			setCardHolderName(source.getCardHolderName());
		
		if (!Util.hasText(CVV2) && Util.hasText(source.getCVV2()))
			setCVV2(source.getCVV2()); 
		
		if (ExpDt == null && source.getExpDt() != null)
			setExpDt(source.getExpDt());
//		if (!Util.hasText(PINBlock) && Util.hasText(source.getPINBlock()))
//			setPINBlock(source.getPINBlock()); 
		
		if (!Util.hasText(actualAppPAN) && Util.hasText(source.getActualAppPAN()))
			setActualAppPAN(source.getActualAppPAN()); 
		
//		if (!Util.hasText(getSubsidiaryAcct()) && Util.hasText(source.getSubsidiaryAcct()))
//			setSubsidiaryAcct(source.getSubsidiaryAcct()); 

		//m.rehman
		if (!Util.hasText(getTrack1Data()) && Util.hasText(source.getTrack1Data()))
			setTrack1Data(source.getTrack1Data());

		if (!Util.hasText(getTrack3Data()) && Util.hasText(source.getTrack3Data()))
			setTrack3Data(source.getTrack3Data());
	}


	public String getActualAppPAN() {
		return actualAppPAN;
	}


	public void setActualAppPAN(String actualAppPAN) {
		this.actualAppPAN = actualAppPAN;
	}

//	public String getSubsidiaryAcct() {
//		return subsidiaryAcct;
//	}
//
//	public void setSubsidiaryAcct(String subsidiaryAcct) {
//		this.subsidiaryAcct = subsidiaryAcct;
//	}

//	public String getAcctId() {
//		return AcctId;
//	}
//
//	public void setAcctId(String acctId) {
//		AcctId = acctId;
//	}



}
