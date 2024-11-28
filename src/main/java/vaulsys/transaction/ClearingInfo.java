package vaulsys.transaction;

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

@Entity
@Table(name = "trx_flg_clearing")
public class ClearingInfo implements IEntity<Long> {

	@Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="clrflg-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "clrflg-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "clrflg_seq")
    				})
    private Long id;
	
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "clr_state"))})
    private ClearingState clearingState = ClearingState.NOT_CLEARED;

    @Column(nullable = true)
    @Embedded
	    @AttributeOverrides({
	    @AttributeOverride(name = "dayDate.date", column = @Column(name = "clr_date")),
	    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "clr_time"))})
    private DateTime clearingDate;

    public ClearingInfo() {
    }

    public ClearingInfo(ClearingInfo clearingInfo) {
    	this(clearingInfo.getClearingState(), clearingInfo.getClearingDate());
    }
    
    public ClearingInfo(ClearingState clearingState, DateTime clearingDate) {
        this.clearingState = clearingState;
        this.clearingDate = clearingDate;
    }

    public ClearingState getClearingState() {
        return clearingState;
    }

    public void setClearingState(ClearingState clearingState) {
        this.clearingState = clearingState;
    }

    public DateTime getClearingDate() {
        return clearingDate;
    }

    public void setClearingDate(DateTime clearingDate) {
        this.clearingDate = clearingDate;
    }

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
}
