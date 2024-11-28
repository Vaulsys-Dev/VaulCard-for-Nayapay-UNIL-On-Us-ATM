package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ifx_credit_card_data")
public class CreditCardData implements IEntity<Long>, Cloneable {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="creditcarddata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "creditcarddata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "creditcarddata_seq")
    				})
    private Long id;

    @Column(name = "trx_amount")
	private Long creditTotalTransactionAmount;
	
    @Column(name = "fee_amount")
	private Long creditTotalFeeAmount;
	
    @Column(name = "interest")
	private Long creditInterest;
	
    @Column(name = "statement_amount")
	private Long creditStatementAmount;
	
    @Column(name = "opentobuy")
	private Long creditOpenToBuy;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getCreditTotalTransactionAmount() {
		return creditTotalTransactionAmount;
	}

	public void setCreditTotalTransactionAmount(Long creditTotalTransactionAmount) {
		this.creditTotalTransactionAmount = creditTotalTransactionAmount;
	}

	public Long getCreditTotalFeeAmount() {
		return creditTotalFeeAmount;
	}

	public void setCreditTotalFeeAmount(Long creditTotalFeeAmount) {
		this.creditTotalFeeAmount = creditTotalFeeAmount;
	}

	public Long getCreditInterest() {
		return creditInterest;
	}

	public void setCreditInterest(Long creditInterest) {
		this.creditInterest = creditInterest;
	}

	public Long getCreditStatementAmount() {
		return creditStatementAmount;
	}

	public void setCreditStatementAmount(Long creditStatementAmount) {
		this.creditStatementAmount = creditStatementAmount;
	}

	public Long getCreditOpenToBuy() {
		return creditOpenToBuy;
	}

	public void setCreditOpenToBuy(Long creditOpenToBuy) {
		this.creditOpenToBuy = creditOpenToBuy;
	}

	protected Object clone() {
		CreditCardData obj = new CreditCardData();
		obj.setCreditTotalTransactionAmount(creditTotalTransactionAmount);
		obj.setCreditTotalFeeAmount(creditTotalFeeAmount);
		obj.setCreditInterest(creditInterest);
		obj.setCreditStatementAmount(creditStatementAmount);
		obj.setCreditOpenToBuy(creditOpenToBuy);
		return obj;
	}
	
	public CreditCardData copy() {
		return (CreditCardData) clone();
	}
}
