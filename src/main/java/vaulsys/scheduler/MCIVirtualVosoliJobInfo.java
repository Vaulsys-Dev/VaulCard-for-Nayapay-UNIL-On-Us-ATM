package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.scheduler.JobInfo;
import vaulsys.transaction.Transaction;
//import vaulsys.webservices.mcivirtualvosoli.common.VirtualVosoliRqParameters;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = "MCI_VirtualVosoli")
public class MCIVirtualVosoliJobInfo extends JobInfo {
	
	@ManyToOne
	@JoinColumn(name = "trx")
//	@ForeignKey(name="mci_jobinfo_trx_fk")
	private Transaction transaction;
	
    @Column(name = "trx", insertable = false, updatable = false)
    private Long transactionId;

	private int count;
	
//	private MCIVosoliState billState;
	
	private Boolean deleted = false; 
    
	public MCIVirtualVosoliJobInfo(){
	}

//    @Transient
//    VirtualVosoliRqParameters virtualVosoliRqParameters;
	
	public MCIVirtualVosoliJobInfo(DateTime fireTime, Transaction transaction, int count/*, MCIVosoliState billState*/){
		super(fireTime);
		this.transaction = transaction;
		this.count = count;
//		this.billState = billState;
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
	
//	public MCIVosoliState getBillState() {
//		return billState;
//	}
//
//	public void setBillState(MCIVosoliState billState) {
//		this.billState = billState;
//	}

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

//    public VirtualVosoliRqParameters getVirtualVosoliRqParameters() {
//        return virtualVosoliRqParameters;
//    }

//    public void setVirtualVosoliRqParameters(VirtualVosoliRqParameters virtualVosoliRqParameters) {
//        this.virtualVosoliRqParameters = virtualVosoliRqParameters;
//    }
}
