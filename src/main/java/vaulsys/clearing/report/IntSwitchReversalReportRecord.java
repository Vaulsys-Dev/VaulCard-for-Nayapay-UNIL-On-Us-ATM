package vaulsys.clearing.report;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.Transaction;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

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
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "intswitch_rev_records")
public class IntSwitchReversalReportRecord implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="rown")
	public Long row;
	public DayDate persianDt;
	public String trnSeqCntr;
	public String appPan;
	public Long amount;
	public Long bankId;
	public Long recordCode;
	public String recordType;	
	public String recordTypeInsert;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trx")
	@ForeignKey(name = "intswitch_rev_rec_trx_fk")
	public Transaction trx;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tuser")
	@ForeignKey(name = "intswitch_rev_rec_user_fk")
	public User user;
	
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "issue_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "issue_time"))
				})
	public DateTime issueDateTime;
	
	public String terminalId;

	public IntSwitchReversalReportRecord() {
	}

	public IntSwitchReversalReportRecord(Long row, DayDate persianDt, String trnSeqCntr, String appPan, Long amount, Long bankId,
			Long recordCode, User user, String recordType,String terminalId) {
		this.row = row;
		this.persianDt = persianDt;
		this.trnSeqCntr = trnSeqCntr;
		this.appPan = appPan;
		this.amount = amount;
		this.bankId = bankId;
		this.recordCode = recordCode;
		this.user = user;
		this.issueDateTime = DateTime.now();
		this.recordType = recordType;
		this.terminalId = terminalId;
	}

	
	@Override
	public String toString(){
		return 
			this.row + "|" +
			this.persianDt + "|" +
			this.trnSeqCntr + "|" +
			this.appPan + "|" +
			this.amount + "|" +		
			this.bankId + "|" +
			this.recordCode + "|" +
			this.recordType + "|" +
			(this.trx != null ? this.trx.getDebugTag():"null")+ "|" +
			this.user + "|" +
			this.issueDateTime + "|" +
			this.terminalId;
	}

}
