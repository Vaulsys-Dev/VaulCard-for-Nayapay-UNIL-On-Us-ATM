package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "ifx_card_acc_data")
public class CardAccountInformation implements IEntity<Long>, Cloneable {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="cardacctinfo-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "cardacctinfo-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "cardacctinfo_seq")
    				})
	private Long id;

    @ManyToOne
	@JoinColumn(name="emv_rs_data")
	@ForeignKey(name="card_acc_data__rs_dt_fk")
	private EMVRsData emvRsData;
    
    @Column(name = "length")
	private Integer length;
	
    @Column(name = "acc_num")
	private String accountNumber;
	
    @Column(name = "indx")
	private String index;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public void setEmvRsData(EMVRsData emvRsData) {
		this.emvRsData = emvRsData;
	}

	public EMVRsData getEmvRsData() {
		return emvRsData;
	}
	
	protected Object clone() {
		CardAccountInformation obj = new CardAccountInformation();
		obj.setAccountNumber(accountNumber);
		obj.setLength(length);
		obj.setIndex(index);
		return obj;
	}
	
	public CardAccountInformation copy() {
		return (CardAccountInformation) clone();
	}
	
}
