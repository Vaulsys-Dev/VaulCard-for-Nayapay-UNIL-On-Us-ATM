package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.transaction.Transaction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "MCI")
public class MCITopupJobInfo extends JobInfo {
    @ManyToOne
    @JoinColumn(name = "trx")
//    @ForeignKey(name="mci_jobinfo_trx_fk")
	private Transaction transaction;

    @Column(name = "trx", insertable = false, updatable = false)
    private Long transactionId;
    
    @Column(name = "mobile_no")
	private String mobileNo;

    private String responseCode;
    
    public MCITopupJobInfo() {
    }
    
	public MCITopupJobInfo(Transaction transaction, String mobileNo, Long amount) {
		super(new DateTime());
		this.transaction = transaction;
		this.mobileNo = mobileNo;
		setAmount(amount);
	}

	public Transaction getTransaction() {
		return transaction;
	}
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
}
