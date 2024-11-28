package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.job.SwitchJobStatus;
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
@Table(name = "job_log")
public class JobLog implements IEntity<Long> {
	@Id
    @GeneratedValue(generator="job-log-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "job-log-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "10"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "job_log_seq") })	
	private Long id;
	
	private String name;

	@Column(name = "exc_msg")
	private String exceptionMessage;

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "job_status"))
    private SwitchJobStatus status;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "start_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "start_time")) })
	private DateTime startTime;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "end_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "end_time")) })
	private DateTime endTime;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}
	public void setExceptionMessage(String exceptionMessage) {
		if(exceptionMessage != null && exceptionMessage.length() > 255)
			exceptionMessage = exceptionMessage.substring(0, 255);
		this.exceptionMessage = exceptionMessage;
	}

	public SwitchJobStatus getStatus() {
		return status;
	}
	public void setStatus(SwitchJobStatus status) {
		this.status = status;
	}

	public DateTime getStartTime() {
		return startTime;
	}
	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}
	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}
}
