package vaulsys.clearing.base;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "clr_date")
public class ClearingDate implements IEntity<Long>{

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="clrdate-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "clrdate-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "clrdate_seq")
    				})    
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "date", column = @Column(name = "day")))
    private MonthDayDate date = MonthDayDate.UNKNOWN;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "recieved_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "recieved_time"))
            })
    private DateTime recievedDate = DateTime.UNKNOWN;

    @Column(nullable = false)
    Boolean valid = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
	@ForeignKey(name="clrdate_owner_fk")
    FinancialEntity owner;

    public ClearingDate(MonthDayDate date) {
        this.date = date;
    }

    public ClearingDate() {
        valid = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Boolean isValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public DateTime getRecievedDate() {
        return recievedDate;
    }

    public void setRecievedDate(DateTime recievedDate) {
        this.recievedDate = recievedDate;
    }

    public MonthDayDate getDate() {
        return date;
    }

    public void setDate(MonthDayDate date) {
        this.date = date;
    }

    public FinancialEntity getOwner() {
        return owner;
    }

    public void setOwner(FinancialEntity owner) {
        this.owner = owner;
    }
}
