package vaulsys.protocols.ifx.imp;

import vaulsys.billpayment.consts.OrganizationType;
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
@Table(name = "ifx_rec_data")
public class ReconciliationData implements IEntity<Long>, Cloneable {
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="reconciliationdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "reconciliationdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "reconciliationdata_seq")
    				})
	private Long id;

    private Integer creditNumber ;
    private Integer creditReversalNumber;
	private Integer debitNumber ;
	private Integer debitReversalNumber;
	private Integer transferNumber;
	private Integer transferReversalNumber;
	private Integer authorizationNumber ;
	private Integer ballInqNumber;
	
	private Long debitFee;
	private Long creditFee;

	private Long debitAmount ;
	private Long creditAmount;
	private Long debitReversalAmount;
	private Long creditReversalAmount;

	public ReconciliationData() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	@Override
	protected Object clone() {
		ReconciliationData obj = new ReconciliationData();
		obj.setDebitNumber(debitNumber);
		obj.setDebitReversalNumber(debitReversalNumber);
		obj.setCreditNumber(creditNumber);
		obj.setCreditReversalNumber(creditReversalNumber);
		obj.setTransferNumber(transferNumber);
		obj.setTransferReversalNumber(transferReversalNumber);
		obj.setBallInqNumber(ballInqNumber);
		obj.setAuthorizationNumber(authorizationNumber);
		obj.setCreditFee(creditFee);
		obj.setDebitFee(debitFee);
		obj.setCreditAmount(creditAmount);
		obj.setCreditReversalAmount(creditReversalAmount);
		obj.setDebitAmount(debitAmount);
		obj.setDebitReversalAmount(debitReversalAmount);
		return obj;
	}

	
	public ReconciliationData copy() {
		return (ReconciliationData) clone();
	}

	public void copyFields(ReconciliationData source) {
		setDebitNumber(source.getDebitNumber());
		setDebitReversalNumber(source.getDebitReversalNumber());
		setCreditNumber(source.getCreditNumber());
		setCreditReversalNumber(source.getCreditReversalNumber());
		setTransferNumber(source.getTransferNumber());
		setTransferReversalNumber(source.getTransferReversalNumber());
		setBallInqNumber(source.getBallInqNumber());
		setAuthorizationNumber(source.getAuthorizationNumber());
		setCreditFee(source.getCreditFee());
		setDebitFee(source.getDebitFee());
		setCreditAmount(source.getCreditAmount());
		setCreditReversalAmount(source.getCreditReversalAmount());
		setDebitAmount(source.getDebitAmount());
		setDebitReversalAmount(source.getDebitReversalAmount());
	}

	public Integer getCreditNumber() {
		return (creditNumber==null)?0:creditNumber;
	}

	public void setCreditNumber(Integer creditNumber) {
		this.creditNumber = creditNumber;
	}

	public Integer getCreditReversalNumber() {
		return (creditReversalNumber==null)?0:creditReversalNumber;
	}

	public void setCreditReversalNumber(Integer creditReversalNumber) {
		this.creditReversalNumber = creditReversalNumber;
	}

	public Integer getDebitNumber() {
		return (debitNumber==null)?0:debitNumber;
	}

	public void setDebitNumber(Integer debitNumber) {
		this.debitNumber = debitNumber;
	}

	public Integer getDebitReversalNumber() {
		return (debitReversalNumber==null)?0:debitReversalNumber;
	}

	public void setDebitReversalNumber(Integer debitReversalNumber) {
		this.debitReversalNumber = debitReversalNumber;
	}

	public Integer getTransferNumber() {
		return (transferNumber==null)?0:transferNumber;
	}

	public void setTransferNumber(Integer transferNumber) {
		this.transferNumber = transferNumber;
	}

	public Integer getTransferReversalNumber() {
		return (transferReversalNumber==null)?0:transferReversalNumber;
	}

	public void setTransferReversalNumber(Integer transferReversalNumber) {
		this.transferReversalNumber = transferReversalNumber;
	}

	public Integer getAuthorizationNumber() {
		return (authorizationNumber==null)?0:authorizationNumber;
	}

	public void setAuthorizationNumber(Integer authorizationNumber) {
		this.authorizationNumber = authorizationNumber;
	}

	public Integer getBallInqNumber() {
		return (ballInqNumber==null)?0:ballInqNumber;
	}

	public void setBallInqNumber(Integer ballInqNumber) {
		this.ballInqNumber = ballInqNumber;
	}

	public Long getDebitFee() {
		return (debitFee==null)?0L:debitFee;
	}

	public void setDebitFee(Long debitFee) {
		this.debitFee = debitFee;
	}

	public Long getCreditFee() {
		return (creditFee==null)?0L:creditFee;
	}

	public void setCreditFee(Long creditFee) {
		this.creditFee = creditFee;
	}

	public Long getDebitAmount() {
		return (debitAmount==null)?0L:debitAmount;
	}

	public void setDebitAmount(Long debitAmount) {
		this.debitAmount = debitAmount;
	}

	public Long getCreditAmount() {
		return (creditAmount==null)?0L:creditAmount;
	}

	public void setCreditAmount(Long creditAmount) {
		this.creditAmount = creditAmount;
	}

	public Long getDebitReversalAmount() {
		return (debitReversalAmount==null)?0L:debitReversalAmount;
	}

	public void setDebitReversalAmount(Long debitReversalAmount) {
		this.debitReversalAmount = debitReversalAmount;
	}

	public Long getCreditReversalAmount() {
		return (creditReversalAmount==null)?0L:creditReversalAmount;
	}

	public void setCreditReversalAmount(Long creditReversalAmount) {
		this.creditReversalAmount = creditReversalAmount;
	}
}
