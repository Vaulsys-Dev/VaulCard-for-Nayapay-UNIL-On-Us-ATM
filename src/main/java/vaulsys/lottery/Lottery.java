package vaulsys.lottery;

import vaulsys.calendar.DateTime;
import vaulsys.lottery.consts.LotteryState;
import vaulsys.lottery.consts.LotteryType;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.LifeCycle;

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
@Table(name = "lottery")
public class Lottery implements IEntity<Long> {

	@Id
	@GeneratedValue(generator="switch-gen")
	private Long serial;
	
	private Long credit;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="lifecycle", nullable = true)
	@ForeignKey(name="lottery_lifecycle_fk")
	private LifeCycle lifeCycle;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "state_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "state_time"))
	})
	private DateTime stateDate;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "state")) 
	})
	private LotteryState state = LotteryState.NOT_ASSIGNED;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "type", column = @Column(name = "type")) 
	})
	private LotteryType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="assignment_plc", nullable = false)
	@ForeignKey(name="lottery_policy_fk")
	private LotteryAssignmentPolicy assignmentPolicy;
	
	
	public Lottery() {
		
	}
	
	public Lottery(Long cardAppPan, Long credit, LotteryState state, LotteryAssignmentPolicy policy) {
		this.serial = cardAppPan;
		this.credit = credit;
		this.state = state;
		this.stateDate = DateTime.now();
		this.type = LotteryType.GIFT_CARD;
		this.assignmentPolicy = policy;
	}
	
	public Lottery(Long credit, LotteryState state, LotteryAssignmentPolicy policy) {
		this.credit = credit;
		this.state = state;
		this.stateDate = DateTime.now();
		this.type = LotteryType.CHARGE;
		this.assignmentPolicy = policy;
	}
	
	@Override
	public Long getId() {
		return serial;
	}

	@Override
	public void setId(Long id) {
		this.serial = id;
	}
	
	public Long getSerial() {
		return serial;
	}

	public void setSerial(Long serial) {
		this.serial = serial;
	}

	public Long getCredit() {
		return credit;
	}

	public void setCredit(Long credit) {
		this.credit = credit;
	}

	public LifeCycle getLifeCycle() {
		return lifeCycle;
	}

	public void setLifeCycle(LifeCycle lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public DateTime getStateDate() {
		return stateDate;
	}

	public void setStateDate(DateTime stateDate) {
		this.stateDate = stateDate;
	}

	public LotteryState getState() {
		return state;
	}

	public void setState(LotteryState state) {
		this.state = state;
		setStateDate(DateTime.now());
	}

	public LotteryType getType() {
		return type;
	}

	public void setType(LotteryType type) {
		this.type = type;
	}
	
	@Override
	public String toString(){
		return getId() != null ? getId().toString() : "";
	}

	public LotteryAssignmentPolicy getAssignmentPolicy() {
		return assignmentPolicy;
	}

	public void setAssignmentPolicy(LotteryAssignmentPolicy assignmentPolicy) {
		this.assignmentPolicy = assignmentPolicy;
	}
	
	
}
