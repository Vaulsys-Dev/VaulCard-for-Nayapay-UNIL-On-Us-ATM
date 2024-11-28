package vaulsys.clearing.base;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.consts.ClearingProcessType;
import vaulsys.clearing.consts.LockObject;
import vaulsys.clearing.consts.SettlementDataCriteria;
import vaulsys.clearing.cyclecriteria.CycleCriteria;
import vaulsys.job.JobSchedule;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.scheduler.job.CycleSettlementJob;
import vaulsys.user.User;
import vaulsys.util.SettlementApplication;
import vaulsys.util.Util;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "clr_prof")
public class ClearingProfile implements IEntity<Long> {
    @Id
//    @GeneratedValue(generator="switch-gen")
	private Long id;

	private String name;

	@Embedded
	@AttributeOverrides( {
		@AttributeOverride(name = "cycleType.type", column = @Column(name = "cycletype")),
		@AttributeOverride(name = "cycleCount", column = @Column(name = "cyclecount")) })
	private CycleCriteria cycleCriteria;
	

	@CollectionOfElements(fetch = FetchType.LAZY)
	@JoinTable(name = "clr_prof_criteria", 
			joinColumns = {@JoinColumn(name = "clrprof")}
	)
	@Embedded
	private Set<SettlementDataCriteria> criterias;
	
	private String cronExpression;

	private Integer settleTimeOffsetDay;
	private Integer settleTimeOffsetHour;
	private Integer settleTimeOffsetMinute;
	
	private Integer accountTimeOffsetMinute;
	
	@AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "fire_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "fire_time"))
            })
	private DateTime fireTime;
	
	private Class settlementClass;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "clrprof_user_fk")
	protected User creatorUser;

    @AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
            })
	protected DateTime createdDateTime;
    
    @Column(name="stl_guarntee_day")
    private Integer settleGuaranteeDay;
    
    @Column(name="acc_guarntee_min")
    private Integer accountingGuaranteeMinute;

    private Boolean hasFee;
    
    @Embedded
	@AttributeOverride(name = "type", column = @Column(name = "process_type"))
	ClearingProcessType processType;
    
    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "lock_obj"))
    LockObject lockObject;
    
    
	
//    @Column(name = "trx_num_pat")
//	private String transactionNumberPattern;
//  
//  
//	public String getTransactionNumberPattern() {
//		return transactionNumberPattern;
//	}
//
//	public void setTransactionNumberPattern(String documentNumber) {
//		this.transactionNumberPattern = documentNumber;
//	}
	
	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

    public GeneralDao getGeneralManager() {
        return SettlementApplication.get().getGeneralDao();
    }
	
	public ClearingProfile(){
	
	}
	
	public ClearingProfile(CycleCriteria cycleCriteria, CycleSettlementJob settlementJob){
		this.cycleCriteria = cycleCriteria;
		getGeneralManager().saveOrUpdate(this);
		cronExpression = Util.generateCronExpression(cycleCriteria, fireTime.toDate());
		submitJob(settlementJob);
	}
	
	public ClearingProfile(String name, CycleCriteria cycleCriteria, CycleSettlementJob settlementJob){
		this.name = name;
		this.cycleCriteria = cycleCriteria;
		getGeneralManager().saveOrUpdate(this);
		cronExpression = Util.generateCronExpression(cycleCriteria, fireTime.toDate());
		submitJob(settlementJob);
	}
	
	public void submitJob(CycleSettlementJob settlementJob){
		if (settlementJob == null)
			return; 
		JobSchedule jobSchedule = new JobSchedule(cronExpression);
		settlementJob.setJobSchedule(jobSchedule);
		settlementJob.setClearingProfile(this);
		SettlementApplication.get().submitJob(settlementJob);
	}

	public String getCronExpression(){
		return cronExpression;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public CycleCriteria getCycleCriteria(){
		return cycleCriteria;
	}

	public void setCycleCriteria(CycleCriteria cycleCriteria, CycleSettlementJob settlementJob)	{
		this.cycleCriteria = cycleCriteria;
		getGeneralManager().saveOrUpdate(this);
		cronExpression = Util.generateCronExpression(cycleCriteria, fireTime.toDate());
		submitJob(settlementJob);
	}

    public void setCronExpression(String cronExpression){
		this.cronExpression = cronExpression;
	}

	public User getCreatorUser() {
		return creatorUser;
	}

	public void setCreatorUser(User creatorUser) {
		this.creatorUser = creatorUser;
	}

	public DateTime getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(DateTime createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	@Override
	public String toString() {
		return name;
	}

	public Integer getSettleTimeOffsetDay() {
		return settleTimeOffsetDay;
	}

	public void setSettleTimeOffsetDay(Integer settleTimeOffsetDay) {
		this.settleTimeOffsetDay = settleTimeOffsetDay;
	}

	public Integer getSettleTimeOffsetHour() {
		return settleTimeOffsetHour;
	}

	public void setSettleTimeOffsetHour(Integer settleTimeOffsetHour) {
		this.settleTimeOffsetHour = settleTimeOffsetHour;
	}
	
	public DateTime getAccountUntilTime(DateTime fromDate) {
		DateTime accountUntilTime = fromDate;
		if (accountTimeOffsetMinute == null)
			return accountUntilTime;
		
		accountUntilTime = DateTime.toDateTime(accountUntilTime.getTime() - Math.abs(accountTimeOffsetMinute) * DateTime.ONE_MINUTE_MILLIS);
		
		return accountUntilTime;
	}
	
	public DateTime getSettleUntilTime(DateTime fromDate) {
		DateTime settleUntilTime = fromDate;
		if (settleTimeOffsetDay == null && settleTimeOffsetHour == null && settleTimeOffsetMinute == null)
			return settleUntilTime;

		if (settleTimeOffsetDay != null) {
			if (settleTimeOffsetDay <= 0)
				settleUntilTime = DateTime.toDateTime(settleUntilTime.getTime() - (Math.abs(settleTimeOffsetDay) * DateTime.ONE_DAY_MILLIS));
		}
		
		if (settleTimeOffsetHour <= 0)
//			settleUntilTime.setDayTime(new DayTime(fromDate.getDayTime().getHour() + settleTimeOffsetHour, 59, 59));
			settleUntilTime = DateTime.toDateTime(settleUntilTime.getTime() - (Math.abs(settleTimeOffsetHour) * DateTime.ONE_HOUR_MILLIS));
		
		else if(settleTimeOffsetHour > 0)
			settleUntilTime.setDayTime(new DayTime(settleTimeOffsetHour, 59, 59));
		
		if (settleTimeOffsetMinute != null) {
			if (settleTimeOffsetMinute <= 0)
//				settleUntilTime.setDayTime(new DayTime(fromDate.getDayTime().getHour(), fromDate.getDayTime().getMinute() + settleTimeOffsetMinute, 59));
				settleUntilTime = DateTime.toDateTime(settleUntilTime.getTime() - (Math.abs(settleTimeOffsetMinute) * DateTime.ONE_MINUTE_MILLIS));
			
			else if(settleTimeOffsetMinute > 0)
				settleUntilTime.setDayTime(new DayTime(settleUntilTime.getDayTime().getHour(), settleTimeOffsetMinute, 59));
		}
		
		return settleUntilTime;
	}

	public DateTime getFireTime() {
		return fireTime;
	}

	public void setFireTime(DateTime fireTime) {
		this.fireTime = fireTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cycleCriteria == null) ? 0 : cycleCriteria.hashCode());
		result = prime * result + ((fireTime == null) ? 0 : fireTime.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((settleTimeOffsetDay == null) ? 0 : settleTimeOffsetDay.hashCode());
		result = prime * result + ((settleTimeOffsetHour == null) ? 0 : settleTimeOffsetHour.hashCode());
		result = prime * result + ((settleTimeOffsetMinute == null) ? 0 : settleTimeOffsetMinute.hashCode());
//		result = prime * result + ((toBeClrTrx == null) ? 0 : toBeClrTrx.hashCode());
		return result;
	}

	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || !(o instanceof ClearingProfile))
			return false;

		ClearingProfile that = (ClearingProfile) o;
		if (id != null ? !id.equals(that.id) : that.id != null) return false;

		return true;
	}

	public Integer getAccountTimeOffsetMinute() {
		return accountTimeOffsetMinute;
	}

	public void setAccountTimeOffsetMinute(Integer accountTimeOffsetMinute) {
		this.accountTimeOffsetMinute = accountTimeOffsetMinute;
	}
	
	public DateTime calcNextSettleTime(DateTime fromDate) {
			DateTime nextPeriod = DateTime.toDateTime(fromDate.getTime() + cycleCriteria.getCycleCount() * DateTime.getTimeMillisByCycleType(cycleCriteria.getCycleType()));
			return getSettleUntilTime(nextPeriod);
	}

	public Set<SettlementDataCriteria> getCriterias() {
		return criterias;
	}

	public void addCriterias(SettlementDataCriteria criteria) {
		if (criterias == null)
			criterias = new HashSet<SettlementDataCriteria>();
		criterias.add(criteria);
	}

	public Class getSettlementClass() {
		return settlementClass;
	}

	public void setSettlementClass(Class settlementClass) {
		this.settlementClass = settlementClass;
	}

	public Integer getSettleTimeOffsetMinute() {
		return settleTimeOffsetMinute;
	}

	public void setSettleTimeOffsetMinute(Integer settleTimeOffsetMinute) {
		this.settleTimeOffsetMinute = settleTimeOffsetMinute;
	}

	public Integer getSettleGuaranteeDay() {
		return settleGuaranteeDay;
	}

	public void setSettleGuaranteeDay(Integer settleGuaranteeDay) {
		this.settleGuaranteeDay = settleGuaranteeDay;
	}

	public Boolean getHasFee() {
		return hasFee;
	}

	public void setHasFee(Boolean hasFee) {
		this.hasFee = hasFee;
	}

	public Integer getAccountingGuaranteeMinute() {
		return accountingGuaranteeMinute;
	}

	public void setAccountingGuaranteeMinute(Integer accountingGuaranteeMinute) {
		this.accountingGuaranteeMinute = accountingGuaranteeMinute;
	}

	public ClearingProcessType getProcessType() {
		return processType;
	}

	public void setProcessType(ClearingProcessType processType) {
		this.processType = processType;
	}

	public LockObject getLockObject() {
		return lockObject;
	}

	public void setLockObject(LockObject lockObject) {
		this.lockObject = lockObject;
	}
}
