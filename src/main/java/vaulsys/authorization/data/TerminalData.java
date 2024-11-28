package vaulsys.authorization.data;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "auth_plc_dt_trm")
public class TerminalData implements IEntity<Long> {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="authplctermdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "authplctermdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "authplctermdata_seq")
    				})
    private Long id;

    private long amount;

    private long count;

    @AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "last_transaction_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "last_transaction_time"))
            })
    private DateTime lastTransactionTime = DateTime.UNKNOWN;

    public TerminalData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public DateTime getLastTransactionTime() {
        return lastTransactionTime;
    }

    public void setLastTransactionTime(DateTime lastTransactionTime) {
        this.lastTransactionTime = lastTransactionTime;
    }

    public void addAmount(long additive) {
        this.amount += additive;
    }

	public long getCount()
	{
		return count;
	}

	public void setCount(long count)
	{
		this.count = count;
	}

}
