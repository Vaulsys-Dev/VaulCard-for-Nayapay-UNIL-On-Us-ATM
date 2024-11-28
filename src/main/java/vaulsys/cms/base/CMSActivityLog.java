package vaulsys.cms.base;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by Mati on 22/10/2019.
 */
@Entity
@Table(name = "CMS_ACTIVITY_LOG")
public class CMSActivityLog implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="CMS_ACTIVITY_LOG_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_ACTIVITY_LOG_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_ACTIVITY_LOG_SEQ")
            })
    private Long id;

    @Column(name = "RELATION")
    private String relation;

    @Column(name = "PREVIOUS_STATUS")
    private String previousStatus;

    @Column(name = "CURRENT_STATUS")
    private String currentStatus;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "ACTIVITY_DATE")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "ACTIVITY_TIME"))
    })
    private DateTime activityDateTime;

    @Column(name = "SOURCE_TYPE")
    private String sourceType;

    @Column(name = "SOURCE_NAME")
    private String sourceName;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public DateTime getActivityDateTime() {
        return activityDateTime;
    }

    public void setActivityDateTime(DateTime activityDateTime) {
        this.activityDateTime = activityDateTime;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    // Asim Shahzad, Date : 11th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111

    @Column(name = "REASON_OF_CLOSURE")
    private String reasonOfClosure;

    @Column(name = "APPROVING_USER")
    private String approvingUser;

    @Column(name = "CLOSURE_REQ_DATETIME")
    private String closureRequestDateTime;

    public String getReasonOfClosure() {
        return reasonOfClosure;
    }

    public void setReasonOfClosure(String reasonOfClosure) {
        this.reasonOfClosure = reasonOfClosure;
    }

    public String getApprovingUser() {
        return approvingUser;
    }

    public void setApprovingUser(String approvingUser) {
        this.approvingUser = approvingUser;
    }

    public String getClosureRequestDateTime() {
        return closureRequestDateTime;
    }

    public void setClosureRequestDateTime(String closureRequestDateTime) {
        this.closureRequestDateTime = closureRequestDateTime;
    }

    // =======================================================================================
}
