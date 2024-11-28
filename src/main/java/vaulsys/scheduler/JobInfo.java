package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "job_info")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "job", discriminatorType = DiscriminatorType.STRING)
public abstract class JobInfo implements IEntity<Long> {

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="jobinfo-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "jobinfo-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "10"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "jobinfo_seq")
    				})
    protected Long id;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "fire_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "fire_time"))
            })
    protected DateTime fireTime;

    private Long amount;
    
    protected JobInfo() {
    }

    protected JobInfo(DateTime fireTime) {
        this.fireTime = fireTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateTime getFireTime() {
        return fireTime;
    }

    public void setFireTime(DateTime fireTime) {
        this.fireTime = fireTime;
    }

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Long getAmount() {
		return amount;
	}
}
