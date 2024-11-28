package vaulsys.modernpayment.onlinebillpayment;

import java.io.Serializable;

import vaulsys.calendar.DateTime;
import vaulsys.entity.impl.Organization;
import vaulsys.persistence.IEntity;
import vaulsys.persistence.IEnum;
import vaulsys.transaction.LifeCycle;

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
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "online_bill")
public class OnlineBillPayment implements IEntity<Long>{
	@Id
	@GeneratedValue(generator="onlinebillpayment-seq-gen")
	@org.hibernate.annotations.GenericGenerator(name = "onlinebillpayment-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
   			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
   			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
   			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "onlinebillpayment_seq")
   				})
	private Long id;
//	@Column(name="refNum", unique=true)
	private String refNum;
	private Long amount;
	private String description;
	private Long expDt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company")
	@ForeignKey(name="onlinebillpay_company_fk")
	private Organization entity;	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="lifecycle", nullable = true)
	@ForeignKey(name="lottery_lifecycle_fk")
	private LifeCycle lifeCycle;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "PAYMENT_STATUS"))
    })
    private OnlineBillPaymentStatus paymentStatus;
	
	private Long ChangePaymentStatusTime;
	
	private String appPAN;
	
	private String trnSeqCntr;
	
	
	public String getTrnSeqCntr() {
		return trnSeqCntr;
	}
	public void setTrnSeqCntr(String trnSeqCntr) {
		this.trnSeqCntr = trnSeqCntr;
	}
	public String getAppPAN() {
		return appPAN;
	}
	public void setAppPAN(String appPAN) {
		this.appPAN = appPAN;
	}
	public Long getChangePaymentStatusTime() {
		return ChangePaymentStatusTime;
	}
	public void setChangePaymentStatusTime(Long changeStateTime) {
		ChangePaymentStatusTime = changeStateTime;
	}
	
	public OnlineBillPaymentStatus getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(OnlineBillPaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public Long getChangeStatusTime() {
		return ChangePaymentStatusTime;
	}
	public void setChangeStatusTime(Long changeStatusTime) {
		ChangePaymentStatusTime = changeStatusTime;
	}
	public LifeCycle getLifeCycle() {
		return lifeCycle;
	}
	public void setLifeCycle(LifeCycle lifeCycle) {
		this.lifeCycle = lifeCycle;
	}
	@Override
	public Long getId() {
		return this.id;
	}
	@Override
	public void setId(Long id) {
		this.id=id;
	}

	public String getRefNum() {
		return refNum;
	}
	public void setRefNum(String refNum) {
		this.refNum = refNum;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	
	public Long getExpDt() {
		return expDt;
	}	
	public void setExpDt(Long expDt) {
		this.expDt = expDt;
	}
	public void setExpDt(DateTime expDt) {
		this.expDt = expDt.getDateTimeLong();
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Organization getEntity() {
		return entity;
	}
	public void setEntity(Organization entity) {
		this.entity = entity;
	}
}
