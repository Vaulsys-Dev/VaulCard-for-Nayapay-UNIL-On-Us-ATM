package vaulsys.job;

import vaulsys.calendar.DateTime;
import vaulsys.util.ConfigUtil;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Transient;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

//@Entity
//@Table(name = "job_schedule")
@Embeddable
public class JobSchedule{

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "start_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "start_time"))
            })
    private DateTime startTime ;

    @Column(name = "repeat_interval")
    private long repeatInterval;
    @Transient

    @Column(name = "repeat_count")
    private int repeatCount;

    @Column(name = "cron_expression")
    private String cronExpression;

    private boolean executeNow;
    
    private int misfireInstruction;

  /*  public static JobSchedule fromNow() {
        return new JobSchedule(DateTime.now(), ConfigUtil.getLong(ConfigUtil.REPEAT_INTERVAL) , ConfigUtil.getInteger(ConfigUtil.REPEAT_COUNT));
    }*/


    public JobSchedule(SimpleTrigger trigger) {
        this.startTime = new DateTime(trigger.getStartTime());
        this.repeatInterval = trigger.getRepeatInterval();
        this.repeatCount = trigger.getRepeatCount();
    }

    @Transient
    private TriggerPreparingStrategy triggerPreparingStrategy;

    public TriggerPreparingStrategy getTriggerPreparingStrategy() {
        if (triggerPreparingStrategy == null) {
            if (cronExpression == null)
                triggerPreparingStrategy = new DefaultTriggerPreparingStratgey();
            else
                triggerPreparingStrategy = new CronTriggerPreparingStratgey();
        }
        return triggerPreparingStrategy;
    }

    public void setTriggerPreparingStrategy(TriggerPreparingStrategy triggerPreparingStrategy) {
        this.triggerPreparingStrategy = triggerPreparingStrategy;
    }


    public static interface TriggerPreparingStrategy {
        public Trigger prepareTrigger(String triggerName, JobSchedule jobSchedule);
    }

    public static final class DefaultTriggerPreparingStratgey implements TriggerPreparingStrategy {
        public Trigger prepareTrigger(String triggerName, JobSchedule jobSchedule) {
            SimpleTrigger trigger = new SimpleTrigger();
            trigger.setName(triggerName);
            trigger.setStartTime(new Date(jobSchedule.startTime.getTime() + jobSchedule.repeatInterval));
            trigger.setRepeatCount(jobSchedule.repeatCount);
            trigger.setRepeatInterval(jobSchedule.repeatInterval);
            return trigger;
        }
    }

    public static final class CronTriggerPreparingStratgey implements TriggerPreparingStrategy {
        public Trigger prepareTrigger(String triggerName, JobSchedule jobSchedule) {
            Trigger trigger;
            try {
                trigger = new CronTrigger(triggerName, null, jobSchedule.cronExpression);
            } catch (ParseException e) {
                throw new SwitchJobException(e);
            }
            trigger.setName(triggerName);
            return trigger;
        }
    }

    public JobSchedule() {
//    	this.startTime = DateTime.now();
    }

    public JobSchedule(DateTime startTime, long repeatInterval, int repeatCount) {
        this.startTime = startTime;
        this.repeatInterval = repeatInterval;
        this.repeatCount = repeatCount;
    }

    public JobSchedule(String cronExpression) {
        this.cronExpression = cronExpression;
        this.startTime = DateTime.now();
    }

    public Trigger prepareTrigger(String triggerName) {
        TriggerPreparingStrategy strategy = getTriggerPreparingStrategy();
        return strategy.prepareTrigger(triggerName, this);
    }

    public String toString() {
        return "job.once_in" + " " + startTime;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public boolean isExecuteNow() {
        return executeNow;
    }

    public void setExecuteNow(boolean executeNow) {
        this.executeNow = executeNow;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

	public int getMisfireInstruction() {
		return misfireInstruction;
	}


	public void setMisfireInstruction(int misfireInstruction) {
		this.misfireInstruction = misfireInstruction;
	}
}
