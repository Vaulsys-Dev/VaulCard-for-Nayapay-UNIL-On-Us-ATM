package vaulsys.authorization.data;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
 
/**
 * 
 * @author p.moosavi
 * Storing total amount of transactions per cycle for each card 
 * Storing fire time for each card in order to delete its record from table if its fire time is passed
 *
 */

@Entity
@Table(name = "auth_plc_dt_card")
public class CardData implements IEntity<Long> {
	@Id
	// @GeneratedValue(generator="switch-gen")
	@GeneratedValue(generator = "authplccarddata-seq-gen")
	@org.hibernate.annotations.GenericGenerator(name = "authplccarddata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "authplccarddata_seq") })
	private Long id;

	private long amount;
	
	private String appPAN;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "fire_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "fire_time"))
            })
    protected DateTime fireTime;
    
    @AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "last_transaction_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "last_transaction_time"))
            })
    private DateTime lastTransactionTime = DateTime.UNKNOWN;
    
    

	@Override
	public Long getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(Long id) {
		// TODO Auto-generated method stub

	}

	public long getAmount() {
		return amount;
	}
	
    public void addAmount(long newAmount) {
        this.amount += newAmount;
    }


	public void setAmount(long amount) {
		this.amount = amount;
	}
	

	public String getAppPAN() {
		return appPAN;
	}

	public void setAppPAN(String appPAN) {
		this.appPAN = appPAN;
	}

	public DateTime getFireTime() {
		return fireTime;
	}

	public void setFireTime(DateTime fireTime) {
		this.fireTime = fireTime;
	}

	public DateTime getLastTransactionTime() {
		return lastTransactionTime;
	}

	public void setLastTransactionTime(DateTime lastTransactionTime) {
		this.lastTransactionTime = lastTransactionTime;
	}

}
