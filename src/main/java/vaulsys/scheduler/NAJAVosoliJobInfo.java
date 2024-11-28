package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.scheduler.JobInfo;
import vaulsys.transaction.Transaction;


import javax.persistence.*;

@Entity
@DiscriminatorValue(value = "NAJA_VirtualVosoli")
public class NAJAVosoliJobInfo extends JobInfo {
	
	@ManyToOne
	@JoinColumn(name = "trx")
	private Transaction transaction;
	
    @Column(name = "trx", insertable = false, updatable = false)
    private Long transactionId;

	private int count;
	
	
	private Boolean deleted = false; 
    
	public NAJAVosoliJobInfo(){
	}


	
	public NAJAVosoliJobInfo(DateTime fireTime, Transaction transaction, int count){
		super(fireTime);
		this.transaction = transaction;
		this.count = count;
	}
	
	public Boolean getDeleted() {
		return deleted;
	}
   
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

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
	
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }


}
