package vaulsys.clearing.base;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.*;

@Entity
@Table(name = "sync_obj", uniqueConstraints = @UniqueConstraint(columnNames={"type", "object"}))
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name = "type")
public class SynchronizationObject implements IEntity<Long> {

	@Id
	@GeneratedValue(generator = "switch-gen")
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "type", updatable = false)
	protected String objClass;
	
	@Column(name = "application")
	protected String application;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "value", column = @Column(name = "state"))
    })
	protected SynchronizationFlag lock;
	
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "lock_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "lock_time"))
    })
    protected DateTime lockDate;

	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "release_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "release_time"))
	})
	protected DateTime releaseDate;
	
	
	@Column(name = "object", updatable = false)
	protected Long objectId;
	
	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	
	
	public SynchronizationObject() {
	}
	
	public SynchronizationObject(IEntity object) {
		this.objectId = (Long) object.getId();
		this.objClass = object.getClass().getSimpleName();
		this.lock = SynchronizationFlag.Free;
	}

//	abstract public T getObject();
//	abstract public void setObject(T obj);

	public String getObjClass() {
		return objClass;
	}
	
	public void setObjClass(String objClass) {
		this.objClass = objClass;
	}
	
	public SynchronizationFlag getLock() {
		return lock;
	}

	public void setLock(SynchronizationFlag lock) {
		this.lock = lock;
	}

	public DateTime getLockDate() {
		return lockDate;
	}

	public void setLockDate(DateTime lockDate) {
		this.lockDate = lockDate;
	}

	public DateTime getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(DateTime releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}
}
