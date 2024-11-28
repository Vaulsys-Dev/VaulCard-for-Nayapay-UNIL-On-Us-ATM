package vaulsys.protocols.ifx.imp;

import vaulsys.modernpayment.onlinebillpayment.OnlineBillPayment;
import vaulsys.modernpayment.onlinebillpayment.OnlineBillPaymentStatus;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "IFX_ONLINE_BILLPAYMENT_DATA")
public class OnlineBillPaymentData {
	@Id
	@GeneratedValue(generator="onlinebillpaymentdata-seq-gen")
	@org.hibernate.annotations.GenericGenerator(name = "onlinebillpaymentdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
   			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
   			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
   			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "onlinebillpaymentdata_seq")
   				})
   	Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="onlinebillpayment", nullable = true)
	@ForeignKey(name="onlinebillpaymentdata_fk")
	private OnlineBillPayment onlineBillPayment;
	 
	private Long company;
	
	private String refNum;
	
	private String description;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "PREVIOUS_PAYMENT_STATUS"))
    })
    private OnlineBillPaymentStatus previousPaymentStatus;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "NEXT_PAYMENT_STATUS"))
    })
    private OnlineBillPaymentStatus nextPaymentStatus;
	
	public OnlineBillPaymentStatus getPreviousPaymentStatus() {
		return previousPaymentStatus;
	}

	public void setPreviousPaymentStatus(
			OnlineBillPaymentStatus previousPaymentStatus) {
		this.previousPaymentStatus = previousPaymentStatus;
	}

	public OnlineBillPaymentStatus getNextPaymentStatus() {
		return nextPaymentStatus;
	}

	public void setNextPaymentStatus(OnlineBillPaymentStatus nextPaymentStatus) {
		this.nextPaymentStatus = nextPaymentStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRefNum() {
		return refNum;
	}

	public void setRefNum(String refNum) {
		this.refNum = refNum;
	}

	public OnlineBillPaymentData() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public Long getCompany() {
		return company;
	}

	public void setCompany(Long company) {
		this.company = company;
	}

	public OnlineBillPayment getOnlineBillPayment() {
//		if(onlineBillPayment == null)
//			return 
		return onlineBillPayment;
	}

	public void setOnlineBillPayment(OnlineBillPayment onlineBillPayment) {
		this.onlineBillPayment = onlineBillPayment;
	}
	public void copyFields(OnlineBillPaymentData source) {
		if( company== null)
			company = source.getCompany();
		
		if(onlineBillPayment == null)
			onlineBillPayment = source.getOnlineBillPayment();
		
		if(refNum == null)
			refNum = source.getRefNum();
		
		if(description == null)
			description = source.getDescription();
	
		if(nextPaymentStatus == null)
			nextPaymentStatus = source.getNextPaymentStatus();
		
		if(previousPaymentStatus == null)
			previousPaymentStatus = source.getPreviousPaymentStatus();
	}

	public OnlineBillPaymentData copy() {
		return (OnlineBillPaymentData) clone();
	}

	@Override
	protected Object clone() {
		OnlineBillPaymentData obj = new OnlineBillPaymentData();
		obj.setCompany(this.company);
		obj.setOnlineBillPayment(this.onlineBillPayment);
		obj.setRefNum(this.refNum);
		obj.setDescription(this.description);
		obj.setNextPaymentStatus(this.nextPaymentStatus);
		obj.setPreviousPaymentStatus(this.previousPaymentStatus);
		return obj;
	}
}
