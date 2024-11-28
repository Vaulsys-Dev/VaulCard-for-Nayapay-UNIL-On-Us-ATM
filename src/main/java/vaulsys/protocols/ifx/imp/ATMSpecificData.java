package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.base.config.TransactionStatusType;
import vaulsys.protocols.ndc.constants.LastStatusIssued;
import vaulsys.protocols.ndc.constants.ReceiptOptionType;
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
@Table(name = "ifx_atm_specific")
public class ATMSpecificData implements IEntity<Long>, Cloneable{
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="atmspecificdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "atmspecificdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "atmspecificdata_seq")
    				})
    private Long id;

    private String opkey;
    
    private String nextOpkey;
    
    private String bufferB;
    
    private String bufferC;
    
    @Column(name = "desired_note_cas1")
    private Integer desiredDispenseCaset1;

    @Column(name = "desired_note_cas2")
    private Integer desiredDispenseCaset2; 

    @Column(name = "desired_note_cas3")
    private Integer desiredDispenseCaset3; 

    @Column(name = "desired_note_cas4")
    private Integer desiredDispenseCaset4; 

    @Column(name = "actual_note_cas1")
    private Integer actualDispenseCaset1;
    
    @Column(name = "actual_note_cas2")
    private Integer actualDispenseCaset2; 
    
    @Column(name = "actual_note_cas3")
    private Integer actualDispenseCaset3; 
    
    @Column(name = "actual_note_cas4")
    private Integer actualDispenseCaset4; 

    @Column(name = "current_note_cas1")
    private Integer currentDispenseCaset1;
    
    @Column(name = "current_note_cas2")
    private Integer currentDispenseCaset2; 
    
    @Column(name = "current_note_cas3")
    private Integer currentDispenseCaset3; 
    
    @Column(name = "current_note_cas4")
    private Integer currentDispenseCaset4; 
    
    private String timeVariantNumber;
    private char coordinationNumber;
    private Integer totalStep;
    private Integer currentStep;
    private String currentDispense;
	
    private Long actualDispenseAmt;
    private Long currentDispenseAmt;
    
    @Column(name = "last_status_id")
    private char lastTrxStatusId;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "trx_status"))
    })
    private TransactionStatusType transactionStatus;
    
//    @Column(name = "last_status_issue")
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "value", column = @Column(name = "last_status_issue"))
    })
    private LastStatusIssued lastTrxStatusIssue;
    
    @Column(name = "last_notes")
    private String lastTrxNotesDispensed;
    
    @Column(name = "branch_code")
    public String coreBranchCode;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "sec_ifxtype"))
    })
    private IfxType secIfxType;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "sec_trntype"))
    })
    private TrnType secTrnType; // Note e.g: Purchase; Cash Withdraw

    private Boolean forceReceipt;
    
    //TASK Task019 : Receipt option
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "rcpt_type"))
    })
    private ReceiptOptionType receiptOption;
    
    //TASK Task019 : Receipt option
    public ReceiptOptionType getReceiptOption() {
		return receiptOption;
	}

    //TASK Task019 : Receipt option
	public void setReceiptOption(ReceiptOptionType receiptOption) {
		this.receiptOption = receiptOption;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

    
	public char getCoordinationNumber() {
		return coordinationNumber;
	}
	
	
	public String getTimeVariantNumber() {
		return timeVariantNumber;
	}
	
	
	public void setCoordinationNumber(char coordinationNumber) {
		this.coordinationNumber = coordinationNumber;
	}

	
	public void setTimeVariantNumber(String timeVariantNumber) {
		this.timeVariantNumber = timeVariantNumber;
	}

	public String getOpkey() {
		return this.opkey;
	}

	public void setOpkey(String opkey) {
		this.opkey = opkey;		
	}

	protected Object clone() {
		ATMSpecificData obj = new ATMSpecificData();
		obj.setCoordinationNumber(this.getCoordinationNumber());
		obj.setOpkey(this.opkey);
		obj.setNextOpkey(this.nextOpkey);
		obj.setBufferB(this.bufferB);
		obj.setBufferC(this.bufferC);
		obj.setTimeVariantNumber(this.timeVariantNumber);
		obj.setActualDispenseCaset1(this.actualDispenseCaset1);
		obj.setActualDispenseCaset2(this.actualDispenseCaset2);
		obj.setActualDispenseCaset3(this.actualDispenseCaset3);
		obj.setActualDispenseCaset4(this.actualDispenseCaset4);
		obj.setCurrentDispenseCaset1(this.currentDispenseCaset1);
		obj.setCurrentDispenseCaset2(this.currentDispenseCaset2);
		obj.setCurrentDispenseCaset3(this.currentDispenseCaset3);
		obj.setCurrentDispenseCaset4(this.currentDispenseCaset4);
		obj.setDesiredDispenseCaset1(this.desiredDispenseCaset1);
		obj.setDesiredDispenseCaset2(this.desiredDispenseCaset2);
		obj.setDesiredDispenseCaset3(this.desiredDispenseCaset3);
		obj.setDesiredDispenseCaset4(this.desiredDispenseCaset4);
		obj.setCurrentDispense(this.currentDispense);
		obj.setCurrentDispenseAmt(this.currentDispenseAmt);
		obj.setActualDispenseAmt(this.actualDispenseAmt);
		obj.setCurrentStep(currentStep);
		obj.setTotalStep(totalStep);
		obj.setLastTrxStatusId(lastTrxStatusId);
		obj.setLastTrxStatusIssue(lastTrxStatusIssue);
		obj.setLastTrxNotesDispensed(lastTrxNotesDispensed);
		obj.setCoreBranchCode(coreBranchCode);
		obj.setSecIfxType(secIfxType);
		obj.setSecTrnType(secTrnType);
		obj.setForceReceipt(forceReceipt);
		obj.setTransactionStatus(transactionStatus);
		obj.setReceiptOption(receiptOption);//TASK Task019 : Receipt Option
		return obj;
	}
	
	
	public ATMSpecificData copy() {
		return (ATMSpecificData) clone();
	}

	public void copyFields(ATMSpecificData source) {
		if (!Util.hasText(getOpkey()) && Util.hasText(source.getOpkey()))
			setOpkey(source.getOpkey());
		
		if (!Util.hasText(getNextOpkey()) && Util.hasText(source.getNextOpkey()))
			setNextOpkey(source.getNextOpkey());
		
		if (Util.hasText(source.getCoordinationNumber() + "")
				&& Character.isIdentifierIgnorable(getCoordinationNumber()) 
				)
			setCoordinationNumber(source.getCoordinationNumber());
		
		if (Util.hasText(source.getLastTrxStatusId() + "") && Character.isIdentifierIgnorable(getLastTrxStatusId()))
				setLastTrxStatusId(source.getLastTrxStatusId());
		
		if (getLastTrxStatusIssue()== null || LastStatusIssued.UNKNOWN.equals(getLastTrxStatusIssue()))
			setLastTrxStatusIssue(source.getLastTrxStatusIssue());
		
		if (!Util.hasText(getLastTrxNotesDispensed()) && Util.hasText(source.getLastTrxNotesDispensed()))
			setLastTrxNotesDispensed(source.getLastTrxNotesDispensed());
		
		if (!Util.hasText(getBufferB()) && Util.hasText(source.getBufferB()))
			setBufferB(source.getBufferB());
		
		if (!Util.hasText(getBufferC()) && Util.hasText(source.getBufferC()))
			setBufferC(source.getBufferC());
		
		if (!Util.hasText(getCoreBranchCode()) && Util.hasText(source.getCoreBranchCode()))
			setCoreBranchCode(source.getCoreBranchCode());
		
		if (getSecIfxType() == null && source.getSecIfxType() != null)
			setSecIfxType(source.getSecIfxType());
		
		if (getSecTrnType() == null && source.getSecTrnType() != null)
			setSecTrnType(source.getSecTrnType());
		
		if (getSecTrnType() == null && source.getSecTrnType() != null)
			setSecTrnType(source.getSecTrnType());
		
		if (getTransactionStatus()== null)
			setTransactionStatus(source.getTransactionStatus());
		
		if (getTotalStep() == null)
			setTotalStep(source.getTotalStep());
		
		if (getCurrentStep() == null)
			setCurrentStep(source.getCurrentStep());
		
		if (getCurrentDispenseAmt() == null)
			setCurrentDispenseAmt(currentDispenseAmt);
		
		if (getActualDispenseAmt()== null)
			setActualDispenseAmt(actualDispenseAmt);
		
		//TASK Task019 : Receipt Option
		//AldQuestion Task019 : Aya Niaz Hast
		if (getReceiptOption() == null && source.getReceiptOption() != null)
			setReceiptOption(source.getReceiptOption());
	}

	public Integer getTotalStep() {
		return totalStep;
	}

	public void setTotalStep(Integer totalStep) {
		this.totalStep = totalStep;
	}

	public Integer getCurrentStep() {
		return currentStep;
	}

	public void setCurrentStep(Integer currentStep) {
		this.currentStep = currentStep;
	}

	public String getCurrentDispense() {
		return currentDispense;
	}

	public void setCurrentDispense(String currentDispense) {
		this.currentDispense = currentDispense;
	}

	public Integer getDesiredDispenseCaset1() {
		return desiredDispenseCaset1;
	}

	public void setDesiredDispenseCaset1(Integer desiredDispenseCaset1) {
		this.desiredDispenseCaset1 = desiredDispenseCaset1;
	}

	public Integer getDesiredDispenseCaset2() {
		return desiredDispenseCaset2;
	}

	public void setDesiredDispenseCaset2(Integer desiredDispenseCaset2) {
		this.desiredDispenseCaset2 = desiredDispenseCaset2;
	}

	public Integer getDesiredDispenseCaset3() {
		return desiredDispenseCaset3;
	}

	public void setDesiredDispenseCaset3(Integer desiredDispenseCaset3) {
		this.desiredDispenseCaset3 = desiredDispenseCaset3;
	}

	public Integer getDesiredDispenseCaset4() {
		return desiredDispenseCaset4;
	}

	public void setDesiredDispenseCaset4(Integer desiredDispenseCaset4) {
		this.desiredDispenseCaset4 = desiredDispenseCaset4;
	}

	public Integer getActualDispenseCaset1() {
		return actualDispenseCaset1;
	}

	public void setActualDispenseCaset1(Integer actualDispenseCaset1) {
		this.actualDispenseCaset1 = actualDispenseCaset1;
	}

	public Integer getActualDispenseCaset2() {
		return actualDispenseCaset2;
	}

	public void setActualDispenseCaset2(Integer actualDispenseCaset2) {
		this.actualDispenseCaset2 = actualDispenseCaset2;
	}

	public Integer getActualDispenseCaset3() {
		return actualDispenseCaset3;
	}

	public void setActualDispenseCaset3(Integer actualDispenseCaset3) {
		this.actualDispenseCaset3 = actualDispenseCaset3;
	}

	public Integer getActualDispenseCaset4() {
		return actualDispenseCaset4;
	}

	public void setActualDispenseCaset4(Integer actualDispenseCaset4) {
		this.actualDispenseCaset4 = actualDispenseCaset4;
	}

	public char getLastTrxStatusId() {
		return lastTrxStatusId;
	}

	public void setLastTrxStatusId(char lastTrxStatusId) {
		this.lastTrxStatusId = lastTrxStatusId;
	}

	public LastStatusIssued getLastTrxStatusIssue() {
		return lastTrxStatusIssue;
	}

	public void setLastTrxStatusIssue(LastStatusIssued lastTrxStatusIssue) {
		this.lastTrxStatusIssue = lastTrxStatusIssue;
	}

	public String getLastTrxNotesDispensed() {
		return lastTrxNotesDispensed;
	}

	public void setLastTrxNotesDispensed(String lastTrxNotesDispensed) {
		this.lastTrxNotesDispensed = lastTrxNotesDispensed;
	}

	public String getBufferB() {
		return bufferB;
	}

	public void setBufferB(String bufferB) {
		this.bufferB = bufferB;
	}

	public String getBufferC() {
		return bufferC;
	}

	public void setBufferC(String bufferC) {
		this.bufferC = bufferC;
	}

	public IfxType getSecIfxType() {
		return secIfxType;
	}

	public void setSecIfxType(IfxType secIfxType) {
		this.secIfxType = secIfxType;
	}

	public String getNextOpkey() {
		return nextOpkey;
	}

	public void setNextOpkey(String nextOpkey) {
		this.nextOpkey = nextOpkey;
	}

	public TrnType getSecTrnType() {
		return secTrnType;
	}

	public void setSecTrnType(TrnType secTrnType) {
		this.secTrnType = secTrnType;
	}

	public String getProperOpkey() {
		if (Util.hasText(nextOpkey) && secIfxType != null && secTrnType != null &&
				ISOFinalMessageType.isGetAccountMessage(secIfxType) &&
				TrnType.GETACCOUNT.equals(secTrnType))
			return nextOpkey;
		return opkey;
	}

	public String getCoreBranchCode() {
		return coreBranchCode;
	}

	public void setCoreBranchCode(String coreBranchCode) {
		this.coreBranchCode = coreBranchCode;
	}

	public Boolean getForceReceipt() {
		return forceReceipt;
	}

	public void setForceReceipt(Boolean forceReceipt) {
		this.forceReceipt = forceReceipt;
	}

	public Integer getCurrentDispenseCaset1() {
		return currentDispenseCaset1;
	}

	public void setCurrentDispenseCaset1(Integer currentDispenseCaset1) {
		this.currentDispenseCaset1 = currentDispenseCaset1;
	}

	public Integer getCurrentDispenseCaset2() {
		return currentDispenseCaset2;
	}

	public void setCurrentDispenseCaset2(Integer currentDispenseCaset2) {
		this.currentDispenseCaset2 = currentDispenseCaset2;
	}

	public Integer getCurrentDispenseCaset3() {
		return currentDispenseCaset3;
	}

	public void setCurrentDispenseCaset3(Integer currentDispenseCaset3) {
		this.currentDispenseCaset3 = currentDispenseCaset3;
	}

	public Integer getCurrentDispenseCaset4() {
		return currentDispenseCaset4;
	}

	public void setCurrentDispenseCaset4(Integer currentDispenseCaset4) {
		this.currentDispenseCaset4 = currentDispenseCaset4;
	}

	public TransactionStatusType getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(TransactionStatusType transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public Long getActualDispenseAmt() {
		return actualDispenseAmt;
	}

	public void setActualDispenseAmt(Long actualDispenseAmt) {
		this.actualDispenseAmt = actualDispenseAmt;
	}

	public Long getCurrentDispenseAmt() {
		return currentDispenseAmt;
	}

	public void setCurrentDispenseAmt(Long currentDispenseAmt) {
		this.currentDispenseAmt = currentDispenseAmt;
	}
}