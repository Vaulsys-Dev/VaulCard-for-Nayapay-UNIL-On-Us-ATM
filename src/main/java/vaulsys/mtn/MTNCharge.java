package vaulsys.mtn;

import vaulsys.calendar.DateTime;
import vaulsys.entity.impl.Organization;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.IEntity;
import vaulsys.transaction.LifeCycle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "mtn_charge")
public class MTNCharge implements IEntity<Long> {

    @Id
    private Long cardSerialNo;

    @Column(length = 1024)
    private String cardPIN;

    private Long credit;

    private Integer fileId;

    private Integer year;

    private String helpDesk;

    private Integer ir;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lifecycle", nullable = true)
    @ForeignKey(name = "charge_lifecycle_fk")
    private LifeCycle lifeCycle;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "state_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "state_time"))
    })
    private DateTime stateDate;

    private String provider;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "charge_state"))
    })
    private MTNChargeState state = MTNChargeState.NOT_ASSIGNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company")
    @Cascade(value = CascadeType.ALL)
    @ForeignKey(name = "mtncharge_company_fk")
    private Organization entity;

    @Column(name = "pinlen")
    private Integer pinlen;

    @Override
    public Long getId() {
        return cardSerialNo;
    }

    @Override
    public void setId(Long id) {
        this.cardSerialNo = id;
    }

    public Long getCredit() {
        return credit;
    }

    public void setCredit(Long credit) {
        this.credit = credit;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getCardSerialNo() {
        return cardSerialNo;
    }

    public void setCardSerialNo(Long cardSerialNo) {
        this.cardSerialNo = cardSerialNo;
    }

    public String getCardPIN() {
        return cardPIN;
    }

    public void setCardPIN(String cardPIN) {
        this.cardPIN = cardPIN;
    }

    public MTNChargeState getState() {
        return state;
    }

    public void setState(MTNChargeState state) {
        this.state = state;
        setStateDate(DateTime.now());
    }

    public DateTime getStateDate() {
        return stateDate;
    }

    public void setStateDate(DateTime stateDate) {
        this.stateDate = stateDate;
    }

//	public OrganizationType getType() {
//		return type;
//	}
//
//	public void setType(OrganizationType type) {
//		this.type = type;
//	}

    public Organization getEntity() {
        return entity;
    }

    public void setEntity(Organization entity) {
        this.entity = entity;
    }

    public String getHelpDesk() {
        return helpDesk;
    }

    public void setHelpDesk(String helpDesk) {
        this.helpDesk = helpDesk;
    }

    public Integer getIr() {
        return ir;
    }

    public void setIr(Integer ir) {
        this.ir = ir;
    }

    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(LifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public Integer getPinlen() {
        return pinlen;
    }

    public void setPinlen(Integer pinlen) {
        this.pinlen = pinlen;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
