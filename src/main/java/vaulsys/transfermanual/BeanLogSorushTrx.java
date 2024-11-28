package vaulsys.transfermanual;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.Transaction;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;


@Entity
@Table(name = "sorush_trx_records")
public class BeanLogSorushTrx implements IEntity<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	 @Id
	    @GeneratedValue(generator="sorushTransferRecords-seq-gen")
	    @org.hibernate.annotations.GenericGenerator(name = "sorushTransferRecords-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
	            parameters = {
	                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
	                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
	                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "sorushTransferRecords_seq")
	            })
	 private Long id;
	 private String appPan;
	 
	 @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "tuser")
	 @ForeignKey(name = "sorush_transfer_records_user_fk")
	 private User user;
	 
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trxRef")
	@ForeignKey(name = "sorush_transfer_records_trxRef_fk")
	private Transaction trxRef;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trxSorush")
	@ForeignKey(name = "sorush_transfer_records_trxSorush_fk")
	private Transaction trxSorush;
	
	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "sorush_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "sorush_time"))
			})
	private DateTime sorushDateTime;

	private Long amountRefTrx;
	
	private Long amountSodSorush;
	
	private Long amountTotal;
	
	private String trnSeqCntr;
	
	@Override
	public Long getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void setId(Long id) {
		// TODO Auto-generated method stub
		this.id = id;
	}

	public String getAppPan() {
		return appPan;
	}

	public void setAppPan(String appPan) {
		this.appPan = appPan;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Transaction getTrxRef() {
		return trxRef;
	}

	public void setTrxRef(Transaction trxRef) {
		this.trxRef = trxRef;
	}

	public Transaction getTrxSorush() {
		return trxSorush;
	}

	public void setTrxSorush(Transaction trxSorush) {
		this.trxSorush = trxSorush;
	}

	public DateTime getSorushDateTime() {
		return sorushDateTime;
	}

	public void setSorushDateTime(DateTime sorushDateTime) {
		this.sorushDateTime = sorushDateTime;
	}

	public Long getAmountRefTrx() {
		return amountRefTrx;
	}

	public void setAmountRefTrx(Long amountRefTrx) {
		this.amountRefTrx = amountRefTrx;
	}

	public Long getAmountSodSorush() {
		return amountSodSorush;
	}

	public void setAmountSodSorush(Long amountSodSorush) {
		this.amountSodSorush = amountSodSorush;
	}

	public Long getAmountTotal() {
		return amountTotal;
	}

	public void setAmountTotal(Long amountTotal) {
		this.amountTotal = amountTotal;
	}

	public String getTrnSeqCntr() {
		return trnSeqCntr;
	}

	public void setTrnSeqCntr(String trnSeqCntr) {
		this.trnSeqCntr = trnSeqCntr;
	}
	
	

}
