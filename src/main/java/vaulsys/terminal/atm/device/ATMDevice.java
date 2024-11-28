package vaulsys.terminal.atm.device;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm_device")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class ATMDevice implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "status", column = @Column(name = "device_status")))
    private DeviceStatus status;
    
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "code", column = @Column(name = "error_severity")))
    private ErrorSeverity errorSeverity;
    
    @Column(nullable = true)
    @Embedded
	    @AttributeOverrides({
	    @AttributeOverride(name = "dayDate.date", column = @Column(name = "err_severity_date")),
	    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "err_severity_time"))})
    private DateTime errorSeverityDate;
    
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "status", column = @Column(name = "location")))
    private DeviceLocation location;
    
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "code", column = @Column(name = "supply_status")))
    private NDCSupplyStatusConstants supplyStatus;

    @ManyToOne
	@JoinColumn(name = "atm")
	@ForeignKey(name = "term_atm_device_atm_fk")    
    private ATMTerminal atm;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "fine_user_fk")
    protected User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    protected DateTime createdDateTime;

    protected ATMDevice() {
        location = DeviceLocation.UNKOWN;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public DeviceLocation getLocation() {
        return location;
    }

    public void setLocation(DeviceLocation location) {
        this.location = location;
    }
    
	public NDCSupplyStatusConstants getSupplyStatus() {
		return supplyStatus;
	}

	public void setSupplyStatus(NDCSupplyStatusConstants supplyStatus) {
		this.supplyStatus = supplyStatus;
	}

	public ATMTerminal getAtm() {
		return atm;
	}

	public void setAtm(ATMTerminal atm) {
		this.atm = atm;
	}

	public ErrorSeverity getErrorSeverity() {
		return errorSeverity;
	}

	public void setErrorSeverity(ErrorSeverity errorSeverity) {
		this.errorSeverity = errorSeverity;
	}

	public DateTime getErrorSeverityDate() {
		return errorSeverityDate;
	}

	public void setErrorSeverityDate(DateTime errorSeverityDate) {
		this.errorSeverityDate = errorSeverityDate;
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
      return id.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((status == null) ? 0  : status.hashCode());
        result = prime * result + ((errorSeverity == null) ? 0  : errorSeverity.hashCode());
        result = prime * result + ((errorSeverityDate == null) ? 0  : errorSeverityDate.hashCode());
        result = prime * result + ((location == null) ? 0  : location.hashCode());
        result = prime * result + ((errorSeverityDate == null) ? 0  : errorSeverityDate.hashCode());
        result = prime * result + ((atm == null) ? 0  : atm.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ATMDevice))
			return false;
		ATMDevice other = (ATMDevice) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
