package vaulsys.log;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.*;

@MappedSuperclass
public abstract class Log implements IEntity<Long> {
	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="log-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "log-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "10"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "log_seq")
    				})	
	private Long id;

	@Column(name = "log_level")
	private LogLevel logLevel;

	@Embedded
	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "log_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "log_time"))
			})
	private DateTime logTime;

	protected Log() {
		logTime = DateTime.now();
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public DateTime getActivityTime() {
		return logTime;
	}

	public void setActivityTime(DateTime activityTime) {
		this.logTime = activityTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
