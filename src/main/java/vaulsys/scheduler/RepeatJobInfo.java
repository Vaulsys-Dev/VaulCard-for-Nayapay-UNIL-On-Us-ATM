package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.transaction.Transaction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "Repeat")
public class RepeatJobInfo extends JobInfo {

    @ManyToOne(targetEntity = Transaction.class)
    @JoinColumn(name = "trx")
    @ForeignKey(name="repeat_jobinfo_trx_fk")
    private Transaction transaction;
    private int count;

    public RepeatJobInfo() {
    }

    public RepeatJobInfo(DateTime fireTime, Transaction transaction, Long amount, int count) {
        super(fireTime);
        this.transaction = transaction;
        this.count = count;
        setAmount(amount);
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
}
