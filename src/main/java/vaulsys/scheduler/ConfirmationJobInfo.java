package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.transaction.Transaction;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "Confirmation")
public class ConfirmationJobInfo extends JobInfo {

    @ManyToOne(targetEntity = Transaction.class)
    @JoinColumn(name = "trx")
    @ForeignKey(name = "conf_jobinfo_trx_fk") //(name="reversal_jobinfo_trx_fk")
    private Transaction transaction;
    private int count;
    private String responseCode;

    /**
     * created in 2011/11/24 for performance **
     */
    private Boolean deleted = false;

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * **
     */

    public ConfirmationJobInfo() {
    }

    public ConfirmationJobInfo(DateTime fireTime, Transaction transaction, Long amount, int count) {
        super(fireTime);
        this.transaction = transaction;
        this.count = count;
        this.deleted = false;
        setAmount(amount);
    }

//    public ConfirmationJobInfo(DateTime fireTime, Transaction transaction, Long amount, String cause, int count) {
//    	super(fireTime);
//    	this.transaction = transaction;
//    	this.count = count;
//    	this.responseCode = cause;
//    	this.deleted = false;
//    	setAmount(amount);
//    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
}
