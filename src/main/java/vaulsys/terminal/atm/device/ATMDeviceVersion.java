package vaulsys.terminal.atm.device;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: Hamid Reza Khanmirza
 * Date: May 9, 2012
 * Time: 12:16:49 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "term_atm_device_ver")
public abstract class ATMDeviceVersion implements IEntity<Long> {
    @Id
    @GeneratedValue(generator = "termatmdev-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "termatmdev-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "termatmdev_seq")
            })
    protected Long id;

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

    @ManyToOne
    @JoinColumn(name = "atm")
    @ForeignKey(name = "term_atm_device_ver_atm_fk")
    private ATMTerminal atm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    protected User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    protected DateTime createdDateTime;

    //////////////////////////////////////Getter and Setter Methods/////////////////////////////////////////////////////

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

    public DeviceLocation getLocation() {
        return location;
    }

    public void setLocation(DeviceLocation location) {
        this.location = location;
    }

    public ATMTerminal getAtm() {
        return atm;
    }

    public void setAtm(ATMTerminal atm) {
        this.atm = atm;
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
}
