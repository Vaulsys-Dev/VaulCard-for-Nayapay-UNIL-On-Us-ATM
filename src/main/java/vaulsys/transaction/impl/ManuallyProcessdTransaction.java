package vaulsys.transaction.impl;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;
import vaulsys.user.User;

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
@Table(name = "trx_manu_proced")
public class ManuallyProcessdTransaction implements IEntity<Long>{

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="trxmanuproced-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "trxmanuproced-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "trxmanuproced_seq")
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
	@JoinColumn(name="usr")
	@ForeignKey(name="trx_manu_proced__usr_fk")
	private User user;
	
	@OneToOne
	@JoinColumn(name ="trx")
	@ForeignKey(name="trx_manu_proced__trx_fk")
	private Transaction transaction;
	
	@Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "prev_clr_state"))})
    private ClearingState prevClrState;
    
    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "prev_clr_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "prev_clr_time"))})
    private DateTime prevClrDate;
	
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "prev_acc_state"))})
    private AccountingState prevAccState;
	
    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "prev_acc_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "prev_acc_time"))})
    private DateTime prevAccDate;
    
//	@Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "clearingDate.dayDate.date", column = @Column(name = "prev_clr_date")),
//    @AttributeOverride(name = "clearingDate.dayTime.dayTime", column = @Column(name = "prev_clr_time")),
//    @AttributeOverride(name = "clearingState.state", column = @Column(name = "prev_clr_state")),
//    @AttributeOverride(name = "accountingState.state", column = @Column(name = "prev_acc_state"))
//            })
//	private ClearingInfo prevClrState;
	
	
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "prev_stl_state"))})
    private SettledState prevStlState;
    
    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "prev_stl_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "prev_stl_time"))})
    private DateTime prevStlDate;
    
//	@Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "settledDate.dayDate.date", column = @Column(name = "prev_stl_date")),
//    @AttributeOverride(name = "settledDate.dayTime.dayTime", column = @Column(name = "prev_stl_time")),
//    @AttributeOverride(name = "settledState.state", column = @Column(name = "prev_stl_state"))
//        	})
//    private SettlementInfo prevStlState;
	
	
	@Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "next_clr_state"))})
    private ClearingState nextClrState;
    
    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "next_clr_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "next_clr_time"))})
    private DateTime nextClrDate;
	
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "next_acc_state"))})
    private AccountingState nextAccState;
    
    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "next_acc_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "next_acc_time"))})
    private DateTime nextAccDate;
    
//	@Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "clearingDate.dayDate.date", column = @Column(name = "next_clr_date")),
//    @AttributeOverride(name = "clearingDate.dayTime.dayTime", column = @Column(name = "next_clr_time")),
//    @AttributeOverride(name = "clearingState.state", column = @Column(name = "next_clr_state")),
//    @AttributeOverride(name = "accountingState.state", column = @Column(name = "next_acc_state"))
//            })
//    private ClearingInfo nextClrState;

	
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "next_stl_state"))})
    private SettledState nextStlState;
    
    @Column(nullable = true)
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "next_stl_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "next_stl_time"))})
    private DateTime nextStlDate;
    
    
//	@Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "settledDate.dayDate.date", column = @Column(name = "next_stl_date")),
//    @AttributeOverride(name = "settledDate.dayTime.dayTime", column = @Column(name = "next_stl_time")),
//    @AttributeOverride(name = "settledState.state", column = @Column(name = "next_stl_state"))
//        	})
//    private SettlementInfo nextStlState;
	

	@Embedded
	@AttributeOverrides({
	    @AttributeOverride(name = "state", column = @Column(name = "srcDst"))})
	private SourceDestination sourceDestination; 
	
	
    public ManuallyProcessdTransaction() {
    }

    public ManuallyProcessdTransaction(Transaction transaction, ClearingInfo prevClrState, SettlementInfo prevStlInfo, ClearingInfo nextClrState, SettlementInfo nextStlInfo, SourceDestination sourceDestination, User user) {
		super();
		this.transaction = transaction;
		
		this.prevClrState = prevClrState.getClearingState();
		this.prevClrDate = prevClrState.getClearingDate();
		this.prevAccState = prevStlInfo.getAccountingState();
		this.prevStlState= prevStlInfo.getSettledState();
		this.prevStlDate= prevStlInfo.getSettledDate();
		
		this.nextClrState = nextClrState.getClearingState();
		this.nextClrDate = nextClrState.getClearingDate();
		this.nextAccState = nextStlInfo.getAccountingState();
		this.nextStlState = nextStlInfo.getSettledState();
		this.nextStlDate = nextStlInfo.getSettledDate();
		
		this.sourceDestination = sourceDestination;
		this.user = user;
	}
	
	
	public Transaction getTransaction() {
		return transaction;
	}
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
/*	public ClearingInfo getPrevState() {
		return prevClrState;
	}
	public void setPrevState(ClearingInfo prevState) {
		this.prevClrState = prevState;
	}
	public ClearingInfo getNextState() {
		return nextClrState;
	}
	public void setNextState(ClearingInfo nextState) {
		this.nextClrState = nextState;
	}
	public void setPrevStlState(SettlementInfo prevStlState) {
		this.prevStlState = prevStlState;
	}
	public SettlementInfo getPrevStlState() {
		return prevStlState;
	}
	public void setNextStlState(SettlementInfo nextStlState) {
		this.nextStlState = nextStlState;
	}
	public SettlementInfo getNextStlState() {
		return nextStlState;
	}*/
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setSourceDestination(SourceDestination sourceDestination) {
		this.sourceDestination = sourceDestination;
	}
	public SourceDestination getSourceDestination() {
		return sourceDestination;
	}
	public ClearingState getPrevClrState() {
		return prevClrState;
	}
	public void setPrevClrState(ClearingState prevClrState) {
		this.prevClrState = prevClrState;
	}
	public DateTime getPrevClrDate() {
		return prevClrDate;
	}
	public void setPrevClrDate(DateTime prevClrDate) {
		this.prevClrDate = prevClrDate;
	}
	public AccountingState getPrevAccState() {
		return prevAccState;
	}
	public void setPrevAccState(AccountingState prevAccState) {
		this.prevAccState = prevAccState;
	}
	public SettledState getPrevStlState() {
		return prevStlState;
	}
	public void setPrevStlState(SettledState prevStlState) {
		this.prevStlState = prevStlState;
	}
	public DateTime getPrevStlDate() {
		return prevStlDate;
	}
	public void setPrevStlDate(DateTime prevStlDate) {
		this.prevStlDate = prevStlDate;
	}
	public ClearingState getNextClrState() {
		return nextClrState;
	}
	public void setNextClrState(ClearingState nextClrState) {
		this.nextClrState = nextClrState;
	}
	public DateTime getNextClrDate() {
		return nextClrDate;
	}
	public void setNextClrDate(DateTime nextClrDate) {
		this.nextClrDate = nextClrDate;
	}
	public AccountingState getNextAccState() {
		return nextAccState;
	}
	public void setNextAccState(AccountingState nextAccState) {
		this.nextAccState = nextAccState;
	}
	public SettledState getNextStlState() {
		return nextStlState;
	}
	public void setNextStlState(SettledState nextStlState) {
		this.nextStlState = nextStlState;
	}
	public DateTime getNextStlDate() {
		return nextStlDate;
	}
	public void setNextStlDate(DateTime nextStlDate) {
		this.nextStlDate = nextStlDate;
	}
	public DateTime getPrevAccDate() {
		return prevAccDate;
	}
	public void setPrevAccDate(DateTime prevAccDate) {
		this.prevAccDate = prevAccDate;
	}
	public DateTime getNextAccDate() {
		return nextAccDate;
	}
	public void setNextAccDate(DateTime nextAccDate) {
		this.nextAccDate = nextAccDate;
	}
	
}
