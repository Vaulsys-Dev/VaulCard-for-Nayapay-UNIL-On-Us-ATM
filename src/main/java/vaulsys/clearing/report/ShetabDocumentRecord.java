package vaulsys.clearing.report;

import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "shetab_repbal")
public class ShetabDocumentRecord implements IEntity<Long> {
	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="shetabrepbal-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "shetabrepbal-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "shetabrepbal_seq")
    				})
	private Long id;

	public String trxType;

	public Long numTrxAcq_C;
	public Long amtTrxAcq_C;
	public Long numTrxAcq_D;
	public Long amtTrxAcq_D;

	public Long numTrxIss_C;
	public Long amtTrxIss_C;
	public Long numTrxIss_D;
	public Long amtTrxIss_D;

	// +:C / -:D
	public Long amtTrxTotal;
	
	// A: transaction / F: fee
	public String strAmtType;
	
	//Relative TASK Task060 : Resalat sanad repball
	//TASK Task061 : Add repextra indicator to shetab_repball
	//TASK Task084 : Add repextra indicator to shetab_repball (Pasargad)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "state", column = @Column(name = "repExtraState"))
    })	
	public RepExteraState repExtraState;
	
	@Column(name="persian_dt")
	public String persianDateStr; 		

	public String getTrxTypeDesc(){
		String trx = "";
		
		if(this.trxType.equals("SHBOX"))
			return "صندوق مشاع";
		if(this.trxType.startsWith("SHB"))
			return "شتاب";
				
		if(this.trxType.endsWith("W"))
			trx = "برداشت وجه";
		else if(this.trxType.endsWith("BP"))
			trx = "پرداخت قبض";
		else if(this.trxType.endsWith("TF"))
			trx = "انتقال از";
		else if(this.trxType.endsWith("TT"))
			trx = "انتقال به";
		else if(this.trxType.endsWith("P"))
			trx = "خرید";
		else if(this.trxType.endsWith("BI"))
			trx = "دریافت موجودی";
		else if(this.trxType.endsWith("AA"))
			trx = "بررسی حساب";
		else if(this.trxType.endsWith("T"))
			trx = "انتقال";
		else  
			trx = "ناشناخته";

		String term = "";
		if(this.trxType.startsWith("ATM"))
			term = "خودپرداز";
		else if(this.trxType.startsWith("PAD"))
			term = "پایانه شعب";
		else if(this.trxType.startsWith("MOB"))
			term = "موبایل";
		else if(this.trxType.startsWith("VRU"))
			term = "تلفن";
		else if(this.trxType.startsWith("POS"))
			term = "پایانه فروشگاهی";
		else if(this.trxType.startsWith("INT"))
			term = "اینترنت";
		else  
			term = "ناشناخته";

		return trx + "-" + term;
	}
	
	public boolean isFee(){
		return this.strAmtType.equals("F");
	}
	
	@Override
	public String toString(){
		return
			this.trxType + "|" +
			this.numTrxAcq_C + "|" +
			this.amtTrxAcq_C + "|" +
			this.numTrxAcq_D + "|" +
			this.amtTrxAcq_D + "|" +
			this.numTrxIss_C + "|" +
			this.amtTrxIss_C + "|" +
			this.numTrxIss_D + "|" +
			this.amtTrxIss_D + "|" +
			this.amtTrxTotal + "|" +
			this.strAmtType + "|" +
			this.persianDateStr;
	}

	public String getTrxType() {
		return trxType;
	}

	public void setTrxType(String trxType) {
		this.trxType = trxType;
	}

	public Long getNumTrxAcq_C() {
		return numTrxAcq_C;
	}

	public void setNumTrxAcq_C(Long numTrxAcq_C) {
		this.numTrxAcq_C = numTrxAcq_C;
	}

	public Long getAmtTrxAcq_C() {
		return amtTrxAcq_C;
	}

	public void setAmtTrxAcq_C(Long amtTrxAcq_C) {
		this.amtTrxAcq_C = amtTrxAcq_C;
	}

	public Long getNumTrxAcq_D() {
		return numTrxAcq_D;
	}

	public void setNumTrxAcq_D(Long numTrxAcq_D) {
		this.numTrxAcq_D = numTrxAcq_D;
	}

	public Long getAmtTrxAcq_D() {
		return amtTrxAcq_D;
	}

	public void setAmtTrxAcq_D(Long amtTrxAcq_D) {
		this.amtTrxAcq_D = amtTrxAcq_D;
	}

	public Long getNumTrxIss_C() {
		return numTrxIss_C;
	}

	public void setNumTrxIss_C(Long numTrxIss_C) {
		this.numTrxIss_C = numTrxIss_C;
	}

	public Long getAmtTrxIss_C() {
		return amtTrxIss_C;
	}

	public void setAmtTrxIss_C(Long amtTrxIss_C) {
		this.amtTrxIss_C = amtTrxIss_C;
	}

	public Long getNumTrxIss_D() {
		return numTrxIss_D;
	}

	public void setNumTrxIss_D(Long numTrxIss_D) {
		this.numTrxIss_D = numTrxIss_D;
	}

	public Long getAmtTrxIss_D() {
		return amtTrxIss_D;
	}

	public void setAmtTrxIss_D(Long amtTrxIss_D) {
		this.amtTrxIss_D = amtTrxIss_D;
	}

	public Long getAmtTrxTotal() {
		return amtTrxTotal;
	}

	public void setAmtTrxTotal(Long amtTrxTotal) {
		this.amtTrxTotal = amtTrxTotal;
	}

	public String getStrAmtType() {
		return strAmtType;
	}

	public void setStrAmtType(String strAmtType) {
		this.strAmtType = strAmtType;
	}

	public String getPersianDateStr() {
		return persianDateStr;
	}

	public void setPersianDateStr(String persianDateStr) {
		this.persianDateStr = persianDateStr;
	}

	//Relative TASK Task060 : Resalat sanad repball
	//TASK Task061 : Add repextra indicator to shetab_repball
	//TASK Task084 : Add repextra indicator to shetab_repball (Pasargad)
	public RepExteraState getRepExtraState() {
		return repExtraState;
	}

	//Relative TASK Task060 : Resalat sanad repball
	//TASK Task061 : Add repextra indicator to shetab_repball
	//TASK Task084 : Add repextra indicator to shetab_repball (Pasargad)
	public void setRepExtraState(RepExteraState repExtraState) {
		this.repExtraState = repExtraState;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
}
