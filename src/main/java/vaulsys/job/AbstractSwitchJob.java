package vaulsys.job;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.scheduler.JobLog;
import vaulsys.wfe.ProcessContext;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Entity
@Table(name = "job_switch")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "job", discriminatorType = DiscriminatorType.STRING)
public abstract class AbstractSwitchJob implements Job,IEntity<String> {

    @Id
    protected String jobName;

    @Embedded
    @AttributeOverride(name = "group", column = @Column(name = "job_group"))
    protected SwitchJobGroup group;

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "job_status"))
    protected SwitchJobStatus status;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "request_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "request_time"))
            })
    protected DateTime requestDateTime;

    @Embedded
    protected JobSchedule jobSchedule;

    protected boolean scheduled;

    protected boolean volatilizeable;
    
    protected boolean durabale;
    
    protected boolean shouldRecover;
    
    public AbstractSwitchJob() {
    }


    public final void executeJob(JobExecutionContext context) {
//        boolean finished = false;
//        while (!finished && !exceptionallyFinished()) {
//            refreshStatus();
//            if (checkJobCanContinue()) {
               	startJob();
               	execute(context);
                postExecute(context);
                finishJob();
//            }
            // this method waits to check for any other condition to be applied, the problem is if this method doesnt work
            // in this loop, it will return from the executeJob method and quartz will think the job is finished, so it wont
            // resume in case of a pause .
            // NOTE : This might be a memory leak if number of threads go up and all of them are just looping/waiting
//            defaultWait();
//        }
    }
    
    public final void execute(JobExecutionContext avicennaJobContext){
    	boolean doLog = doLog();
    	JobLog log = new JobLog();
    	if(doLog) {
	    	log.setName(getClass().getSimpleName());
	    	log.setStatus(SwitchJobStatus.RUNNING);
	    	log.setStartTime(DateTime.now());

	    	ProcessContext.get().init();
	    	
	    	GeneralDao.Instance.beginTransaction();
	    	GeneralDao.Instance.saveOrUpdate(log);
	    	GeneralDao.Instance.endTransaction();
    	}

    	try {
			execute(avicennaJobContext, log);
		} catch (Exception e) {
			log.setExceptionMessage(e.getMessage());
			log.setStatus(SwitchJobStatus.FAILED);
		}

		if(doLog) {
	    	log.setEndTime(DateTime.now());
	    	GeneralDao.Instance.beginTransaction();
	    	GeneralDao.Instance.saveOrUpdate(log);
	    	GeneralDao.Instance.endTransaction();
		}
    }

    public abstract void execute(JobExecutionContext avicennaJobContext, JobLog log);

	public abstract void updateExecutionInfo();

    public abstract void submitJob() throws Exception;
	
    public void updateStatus(SwitchJobStatus status) {
        this.status = status;
    }

    protected void startJob() {
        updateStatus(SwitchJobStatus.RUNNING);
    }

    protected void finishJob() {
        updateStatus(SwitchJobStatus.FINISHED);
    }

    protected void postExecute(JobExecutionContext context) {

    }

    protected boolean checkJobCanContinue() {
        if (status.equals(SwitchJobStatus.NOT_STARTED)
                || status.equals(SwitchJobStatus.RUNNING)
                || status.equals(SwitchJobStatus.UNKNOWN)
                || status.equals(SwitchJobStatus.FINISHED)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "jobName = " + jobName + ", group =" + group + ", status = " + status;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public SwitchJobGroup getGroup() {
        return group;
    }

    public void setGroup(SwitchJobGroup group) {
        this.group = group;
    }

    public SwitchJobStatus getStatus() {
        return status;
    }

    public void setStatus(SwitchJobStatus status) {
        this.status = status;
    }

    public DateTime getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime(DateTime requestDateTime) {
        this.requestDateTime = requestDateTime;
    }

    public JobSchedule getJobSchedule() {
        return jobSchedule;
    }

    public void setJobSchedule(JobSchedule jobSchedule) {
        this.jobSchedule = jobSchedule;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

	public boolean isVolatilizeable() {
		return volatilizeable;
	}


	public void setVolatilizeable(boolean volatilizeable) {
		this.volatilizeable = volatilizeable;
	}


	public boolean isDurabale() {
		return durabale;
	}


	public void setDurabale(boolean durabale) {
		this.durabale = durabale;
	}


	public boolean isShouldRecover() {
		return shouldRecover;
	}


	public void setShouldRecover(boolean shouldRecover) {
		this.shouldRecover = shouldRecover;
	}

	@Override
	public String getId() {
		return jobName;
	}

	@Override
	public void setId(String jobName) {
		this.jobName = jobName;
	}

	public boolean doLog(){
		return true;
	}
}
