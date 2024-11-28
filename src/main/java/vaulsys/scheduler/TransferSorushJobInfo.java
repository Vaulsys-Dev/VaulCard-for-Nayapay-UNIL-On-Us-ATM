package vaulsys.scheduler;

import vaulsys.transaction.Transaction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "TransferSorushJob")
public class TransferSorushJobInfo extends JobInfo {

	@ManyToOne
    @JoinColumn(name = "trx")
//  @ForeignKey(name="mci_jobinfo_trx_fk")
	private Transaction transaction;
	
	private Integer count;
	
	
	private Boolean deleted = false; 
	
	
	private String urlFile;
	
	
	public String getUrlFile() {
		return urlFile;
	}

	public void setUrlFile(String urlFile) {
		this.urlFile = urlFile;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	
	
}
