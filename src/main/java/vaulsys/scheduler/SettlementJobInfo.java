package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.SettlementData;
import vaulsys.transaction.Transaction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "Settlement")
public class SettlementJobInfo extends JobInfo {

    @ManyToOne(targetEntity = Transaction.class)
    @JoinColumn(name = "trx")
//    @ForeignKey(name="settlement_jobinfo_trx_fk")
    private Transaction transaction;
    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stl_data", nullable = true, updatable = true)
	@ForeignKey(name = "stljob_stldata_fk")
    private SettlementData settlementData;
    
    public SettlementJobInfo() {
    }

    public SettlementJobInfo(DateTime fireTime, Transaction transaction, SettlementData settlementData, int count) {
        super(fireTime);
        this.transaction = transaction;
        this.count = count;
        this.settlementData = settlementData;
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

	public SettlementData getSettlementData() {
		return settlementData;
	}

	public void setSettlementData(SettlementData settlementData) {
		this.settlementData = settlementData;
	}
}
