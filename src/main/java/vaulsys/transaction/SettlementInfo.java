package vaulsys.transaction;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "trx_flg_settlement")
public class SettlementInfo implements IEntity<Long> {
//	@Embedded
//    @AttributeOverrides({
//    @AttributeOverride(name = "settledDate.dayDate.date", column = @Column(name = "src_stl_date")),
//    @AttributeOverride(name = "settledDate.dayTime.dayTime", column = @Column(name = "src_stl_time")),
//    @AttributeOverride(name = "settledState.state", column = @Column(name = "src_stl_state"))
//        	})

	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="stlflg-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "stlflg-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "stlflg_seq")
    				})
	private Long id;

	@Embedded
	@AttributeOverrides({
	@AttributeOverride(name = "state", column = @Column(name = "stl_state"))})
	private SettledState settledState = SettledState.NOT_SETTLED;

	@Column(nullable = true)
	@Embedded
	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "stl_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "stl_time"))})
	private DateTime settledDate;

	@Embedded
	@AttributeOverrides({@AttributeOverride(name = "state", column = @Column(name = "acc_state"))})
	private AccountingState accountingState;

	@Column(nullable = true)
	@Embedded
	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "acc_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "acc_time"))})
	private DateTime accountingDate;

	@Column(name = "amount")
	private Long totalAmount;

	@Column(name = "fee")
	private Long totalFee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stl_data", nullable = true, updatable = true)
	@ForeignKey(name = "stlinfo_stldata_fk")
	private SettlementData settlementData;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trx", nullable = true)
	@Cascade(value = {CascadeType.ALL})
	@ForeignKey(name = "stlinfo_trx_fk")
	private Transaction transaction;
	
//	@ManyToOne
//    @JoinColumn(name = "stl_report")
//	@ForeignKey(name="stlinfo_stlreport_fk")
//	private SettlementReport settlementReport;

	public SettlementInfo() {
	}

	public SettlementInfo(SettledState settledState, AccountingState accountingState, DateTime stateTime, Transaction transaction) {
		this.settledState = settledState;
		this.accountingState = accountingState;
		this.settledDate = stateTime;
		this.accountingDate = stateTime;
		this.transaction = transaction;
	}

	public DateTime getSettledDate() {
		return settledDate;
	}

	public void setSettledDate(DateTime settlementDate) {
		this.settledDate = settlementDate;
	}

	public SettledState getSettledState() {
		return settledState;
	}

	public void setSettledState(SettledState settledState) {
		this.settledState = settledState;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public AccountingState getAccountingState() {
		return accountingState;
	}

	public void setAccountingState(AccountingState accountingState) {
		this.accountingState = accountingState;
	}

	public DateTime getAccountingDate() {
		return accountingDate;
	}

	public void setAccountingDate(DateTime accountingDate) {
		this.accountingDate = accountingDate;
	}

	public SettlementData getSettlementData() {
		return settlementData;
	}

	public void setSettlementData(SettlementData settlementData) {
		this.settlementData = settlementData;
	}

	public Long getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Long totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Long getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Long totalFee) {
		this.totalFee = totalFee;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
//	public SettlementReport getSettlementReport() {
//		return settlementReport;
//	}
//
//	public void setSettlementReport(SettlementReport settlementReport) {
//		this.settlementReport = settlementReport;
//	}
}
