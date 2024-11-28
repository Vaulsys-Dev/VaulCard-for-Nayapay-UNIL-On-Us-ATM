package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.BalType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "ifx_emv_rs_data")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class EMVRsData implements IEntity<Long>, Cloneable {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="emvrsdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "emvrsdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "emvrsdata_seq")
    				})
	private Long id;

	private String ApprovalCode; // P38

	private String RsCode; // P39

//	private String AdditionalRsData; // P-44

//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "acctbal_avlb")
//	@Cascade(value = { CascadeType.ALL })
//	@ForeignKey(name="emvrsdata_accbalavlb_fk")
//	private AcctBal AcctBalAvailable; // = null;
//
//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "acctbal_ldg")
//	@Cascade(value = { CascadeType.ALL })
//	@ForeignKey(name="emvrsdata_accballdg_fk")
//	private AcctBal AcctBalLedger; // = null;
	
	
	@OneToMany(mappedBy = "emvrsdata", fetch = FetchType.LAZY)
	@Cascade(value = { CascadeType.ALL })
	private Set<AcctBal> acctBals;
	
	@OneToMany(mappedBy = "emvrsdata",fetch = FetchType.LAZY)
	@Transient
	private Set<AcctBal> transientAcctBals;
	

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creditcard_data")
	@Cascade(value = { CascadeType.ALL })
	@ForeignKey(name="emvrsdata_creditcarddata_fk")
	private CreditCardData creditCardData;
	
	private String MainAccountNumber; 

	private String DocumentNumber;
	
	@OneToMany(mappedBy="emvRsData")
	@Cascade(value = {CascadeType.ALL})
	private List<BankStatementData> bankStatementData;
	
	@OneToMany(mappedBy="emvRsData")
	@Cascade(value = {CascadeType.ALL})
	@OrderBy("index")
	private List<CardAccountInformation> cardAccountInformation;
	
	
	private Long totalFeeAmt;

	@Transient
	private String shebaCode;
	
	public String getShebaCode()
	{
		return shebaCode;
	}
	
	public void setShebaCode(String shebacode)
	{
		shebaCode = shebacode;
	}
	
	//TASK Task081 : ATM Saham Feature
	@Transient
	private String stockCode;
	
	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	@Transient
	private Long stockCount;
	
	public Long getStockCount() {
		return stockCount;
	}

	public void setStockCount(Long stockCount) {
		this.stockCount = stockCount;
	}
	
	//END Task081	
	
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getApprovalCode() {
		return ApprovalCode;
	}

	
	public void setApprovalCode(String approvalCode) {
		ApprovalCode = approvalCode;
	}

	
	public String getRsCode() {
		return RsCode;
	}

	
	public void setRsCode(String rsCode) {
		RsCode = rsCode;
	}

	
	public AcctBal getAcctBalAvailable() {
		if(this.acctBals == null)
    		return getTransientAcctBalAvailable();
   		for (AcctBal acctBal: acctBals) {
    			if(BalType.AVAIL.equals(acctBal.getBalType())) {
    				return acctBal;
    			}
    	}
        return null;
	}
	
	public AcctBal getTransientAcctBalAvailable() {
		if(this.transientAcctBals == null)
    		return null;
    	
    	for(AcctBal transientAcctBal: transientAcctBals){
    			if(BalType.AVAIL.equals(transientAcctBal.getBalType())){
    				return transientAcctBal;
    			}
   		}
        return null;
	}
	
	public AcctBal getSafeAcctBalAvailable() {
		if(this.acctBals == null) {
    		this.acctBals = new HashSet<AcctBal>();
    		AcctBal acctBal = new AcctBal();
    		acctBal.setBalType(BalType.AVAIL);
    		setAcctBalAvailable(acctBal);
    		return acctBal;
		}
    	
        for (AcctBal acctBal: acctBals) {
        	if(BalType.AVAIL.equals(acctBal.getBalType())) {
        		return acctBal;
        	}
        }
        
    	AcctBal acctBal = new AcctBal();
		acctBal.setBalType(BalType.AVAIL);
		
		setAcctBalAvailable(acctBal);
		
        return acctBal;
	}
	
	
	public AcctBal getSafeTransientAcctBalAvailable() {
		if(this.transientAcctBals == null) {
    		this.transientAcctBals = new HashSet<AcctBal>();
    		AcctBal transientAcctBal = new AcctBal();
    		transientAcctBal.setBalType(BalType.AVAIL);
    		setTransientAcctBalAvailable(transientAcctBal);
    		return transientAcctBal;
		}
    	
        for (AcctBal transientAcctBal: transientAcctBals) {
        	if(BalType.AVAIL.equals(transientAcctBal.getBalType())) {
        		return transientAcctBal;
        	}
        }
        
    	AcctBal transientAcctBal = new AcctBal();
		transientAcctBal.setBalType(BalType.AVAIL);
		
		setTransientAcctBalAvailable(transientAcctBal);
		
        return transientAcctBal;
	}

	
	public void setAcctBalAvailable(AcctBal acctBalAvailable) {
		if(this.acctBals == null)
    		this.acctBals = new HashSet<AcctBal>();
		
        if(acctBals != null){
        	acctBalAvailable.setEmvrsdata(this);
        	this.acctBals.add(acctBalAvailable);       	
        }
	}
	
	public void setTransientAcctBalAvailable(AcctBal acctBalAvailable) {
		if(this.transientAcctBals == null)
			this.transientAcctBals =  new HashSet<AcctBal>();
		
        if(transientAcctBals != null){
        	acctBalAvailable.setEmvrsdata(this);
        	this.transientAcctBals.add(acctBalAvailable);
        }
	}

	public AcctBal getAcctBalLedger() {
		if(this.acctBals == null)
    		return getTransientAcctBalLedger();
    	
   		for (AcctBal acctBal: acctBals) {
    			if(BalType.LEDGER.equals(acctBal.getBalType())) {
    				return acctBal;
    			}
   		}    		
        return null;

	}
	public AcctBal getTransientAcctBalLedger() {
		if(this.transientAcctBals == null)
    		return null;
    	
   		for(AcctBal transientAcctBal: transientAcctBals){
    			if(BalType.LEDGER.equals(transientAcctBal.getBalType())){
    				return transientAcctBal;
    			}
   		}
        return null;

	}
	
	public AcctBal getSafeAcctBalLedger() {
		if (this.acctBals == null) {
			this.acctBals = new HashSet<AcctBal>();
			AcctBal acctBal = new AcctBal();
			acctBal.setBalType(BalType.LEDGER);
			setAcctBalLedger(acctBal);
			
			return acctBal;
		}
		
		for (AcctBal acctBal: acctBals){
			if (BalType.LEDGER.equals(acctBal.getBalType())) {
				return acctBal;
			}
		}
		
		AcctBal acctBal = new AcctBal();
		acctBal.setBalType(BalType.LEDGER);
		
		setAcctBalLedger(acctBal);
		
		return acctBal;
	}
	
	public AcctBal getSafeTransientAcctBalLedger() {
		if (this.transientAcctBals == null) {
			this.transientAcctBals = new HashSet<AcctBal>();
			AcctBal transientAcctBal = new AcctBal();
			transientAcctBal.setBalType(BalType.LEDGER);
			setTransientAcctBalLedger(transientAcctBal);
			
			return transientAcctBal;
		}
		
		for (AcctBal transientAcctBal: transientAcctBals){
			if (BalType.LEDGER.equals(transientAcctBal.getBalType())) {
				return transientAcctBal;
			}
		}
		
		AcctBal transientAcctBal = new AcctBal();
		transientAcctBal.setBalType(BalType.LEDGER);
		
		setTransientAcctBalLedger(transientAcctBal);
		
		return transientAcctBal;
	}


	
	public void setAcctBalLedger(AcctBal acctBalLedger) {
		if(this.acctBals == null)
    		this.acctBals = new HashSet<AcctBal>();
		
		if(this.acctBals != null){
			acctBalLedger.setEmvrsdata(this);
			this.acctBals.add(acctBalLedger);
		}
	}
	
	public void setTransientAcctBalLedger(AcctBal acctBalLedger) {
		if(this.transientAcctBals == null)
			this.transientAcctBals = new HashSet<AcctBal>();
		
		if(this.transientAcctBals != null){
			acctBalLedger.setEmvrsdata(this);
			this.transientAcctBals.add(acctBalLedger);
		}
        
	}
/*	public AcctBal getAcctBalAvailable() {
		return AcctBalAvailable;
	}
	
	public AcctBal getSafeAcctBalAvailable() {
		if (this.AcctBalAvailable == null)
			this.AcctBalAvailable = new AcctBal();
		return AcctBalAvailable;
	}
	
	
	public void setAcctBalAvailable(AcctBal acctBalAvailable) {
		AcctBalAvailable = acctBalAvailable;
	}
	
	public AcctBal getAcctBalLedger() {
		return AcctBalLedger;
	}
	
	public AcctBal getSafeAcctBalLedger() {
		if (AcctBalLedger == null)
			AcctBalLedger = new AcctBal();
		return AcctBalLedger;
	}
	
	
	public void setAcctBalLedger(AcctBal acctBalLedger) {
		AcctBalLedger = acctBalLedger;
	}
*/
	public CreditCardData getCreditCardData() {
		return creditCardData;
	}

	public void setCreditCardData(CreditCardData creditCardData) {
		this.creditCardData = creditCardData;
	}
	
	public CreditCardData getSafeCreditCardData() {
		if (creditCardData == null)
			creditCardData = new CreditCardData();
		return creditCardData;
	}


	protected Object clone() {
		EMVRsData obj = new EMVRsData();
		if (getAcctBalAvailable() != null)
			obj.setAcctBalAvailable(getAcctBalAvailable().copy());
		if (getAcctBalLedger() != null)
			obj.setAcctBalLedger(getAcctBalLedger().copy());
		if (getTransientAcctBalAvailable() != null)
			obj.setTransientAcctBalAvailable(getTransientAcctBalAvailable().copy());
		if (getTransientAcctBalLedger() != null)
			obj.setTransientAcctBalLedger(getTransientAcctBalLedger().copy());
		if (getCreditCardData() != null)
			obj.setCreditCardData(getCreditCardData().copy());
		obj.setMainAccountNumber(MainAccountNumber);
		obj.setApprovalCode(ApprovalCode);
		obj.setRsCode(RsCode);
		obj.setDocumentNumber(DocumentNumber);
		obj.setTotalFeeAmt(totalFeeAmt);
		obj.setShebaCode(shebaCode);
		//TASK Task081 : ATM Saham feature
		obj.setStockCode(stockCode);
		obj.setStockCount(stockCount);		
		return obj;
	}

	
	public EMVRsData copy() {
		return (EMVRsData) clone();
	}

	public void setMainAccountNumber(String mainAccountNumber) {
		MainAccountNumber = mainAccountNumber;
	}

	public String getMainAccountNumber() {
		return MainAccountNumber;
	}

	public void setBankStatementData(List<BankStatementData> bankStatementData) {
		this.bankStatementData = bankStatementData;
	}

	public List<BankStatementData> getBankStatementData() {
		return bankStatementData;
	}

	public String getDocumentNumber() {
		return DocumentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		DocumentNumber = documentNumber;
	}

	public List<CardAccountInformation> getCardAccountInformation() {
		return cardAccountInformation;
	}

	public void setCardAccountInformation(List<CardAccountInformation> cardAccountInformation) {
		this.cardAccountInformation = cardAccountInformation;
	}

	public Long getTotalFeeAmt() {
		return totalFeeAmt;
	}

	public void setTotalFeeAmt(Long totalFeeAmt) {
		this.totalFeeAmt = totalFeeAmt;
	}

	public Set<AcctBal> getAcctBals() {
		return acctBals;
	}

	public void setAcctBals(Set<AcctBal> acctBals) {
		this.acctBals = acctBals;
	}
	
	public Set<AcctBal> getTransientAcctBals() {
		return transientAcctBals;
	}

	public void setTransientAcctBals(Set<AcctBal> transientAcctBals) {
		this.transientAcctBals = transientAcctBals;
	}
	
}
