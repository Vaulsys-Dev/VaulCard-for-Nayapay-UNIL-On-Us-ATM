package vaulsys.protocols.ifx.imp;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;


@Entity
@Table(name = "ifx_clr_proced_trx")
public class ProcessdTransaction implements IEntity<Long>{

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="processdtrx-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "processdtrx-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "processdtrx_seq")
    				})
    private Long id;
	
	@Override
	public Long getId() {
		return id;
	} 
	@Override
	public void setId(Long id) {
		this.id = id; 
	}
	
	@ManyToOne
	@JoinColumn(name="clr_ifx")
	@ForeignKey(name="proced_trx__clr_ifx_fk")
	private Ifx clearingIfx;
	
	@OneToOne
	@JoinColumn(name ="trx")
	@ForeignKey(name="proced_trx__trx_fk")
	private Transaction transaction;
	
	@Embedded
	@AttributeOverrides({
	    @AttributeOverride(name = "state", column = @Column(name = "srcDst"))})
	private SourceDestination sourceDestination; 
	
	@Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "prev_clr_state"))})
    private ClearingState prevState;

    @Column(nullable = true)
    @Embedded
	@AttributeOverrides({
	    @AttributeOverride(name = "dayDate.date", column = @Column(name = "prev_clr_date")),
	    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "prev_clr_time"))})
    private DateTime prevStateDate;
	
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "next_clr_state"))})
    private ClearingState nextState;
    
    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "next_clr_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "next_clr_time"))})
    private DateTime nextStateDate;
    
	
//	@Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "clearingDate.dayDate.date", column = @Column(name = "prev_clr_date")),
//    @AttributeOverride(name = "clearingDate.dayTime.dayTime", column = @Column(name = "prev_clr_time")),
//    @AttributeOverride(name = "clearingState.state", column = @Column(name = "prev_clr_state")),
//    @AttributeOverride(name = "accountingState.state", column = @Column(name = "prev_acc_state"))
//            })
//	private ClearingInfo prevState;
	
//	@Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "clearingDate.dayDate.date", column = @Column(name = "next_clr_date")),
//    @AttributeOverride(name = "clearingDate.dayTime.dayTime", column = @Column(name = "next_clr_time")),
//    @AttributeOverride(name = "clearingState.state", column = @Column(name = "next_clr_state")),
//    @AttributeOverride(name = "accountingState.state", column = @Column(name = "next_acc_state"))
//            })
//    private ClearingInfo nextState;


    public ProcessdTransaction() {
    }

    public ProcessdTransaction(Transaction transaction, ClearingInfo prevState, ClearingInfo nextState, SourceDestination srcDest) {
		super();
		this.transaction = transaction;
		this.prevState = prevState.getClearingState();
		this.prevStateDate = prevState.getClearingDate();
		
		this.sourceDestination = srcDest;
		
		if(nextState != null) {
			this.nextState = nextState.getClearingState();
			this.nextStateDate = nextState.getClearingDate();
		}
	}
	
	
	public Transaction getTransaction() {
		return transaction;
	}
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	public Ifx getClearingIfx() {
		return clearingIfx;
	}
	public void setClearingIfx(Ifx clearingIfx) {
		this.clearingIfx = clearingIfx;
	}
	public ClearingState getPrevState() {
		return prevState;
	}
	public void setPrevState(ClearingState prevState) {
		this.prevState = prevState;
	}
	public DateTime getPrevStateDate() {
		return prevStateDate;
	}
	public void setPrevStateDate(DateTime prevStateDate) {
		this.prevStateDate = prevStateDate;
	}
	public ClearingState getNextState() {
		return nextState;
	}
	public void setNextState(ClearingState nextState) {
		this.nextState = nextState;
	}
	public DateTime getNextStateDate() {
		return nextStateDate;
	}
	public void setNextStateDate(DateTime nextStateDate) {
		this.nextStateDate = nextStateDate;
	}
	public SourceDestination getSourceDestination() {
		return sourceDestination;
	}
	public void setSourceDestination(SourceDestination sourceDestination) {
		this.sourceDestination = sourceDestination;
	}
	
	
}
