package vaulsys.mtn;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "mtn_charge_report")
public class InsertedChargeReport implements IEntity<Long> {
    @Id
    @GeneratedValue(generator = "charge_data_id-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "charge_data_id-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "charge_data_id_seq")
            })
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "useracc")
    @ForeignKey(name = "user_acc_fk")
    private User userAcc;

    private Integer numberOfCharges;

    private Long amount;

    private Integer companyCode;

    private Long InsertDate;

    private String fileName;

    private String errorMsg;

    private Boolean repository = false;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    private DateTime createdDateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUserAcc() {
        return userAcc;
    }

    public void setUserAcc(User userAcc) {
        this.userAcc = userAcc;
    }

    public Integer getNumberOfCharges() {
        return numberOfCharges;
    }

    public void setNumberOfCharges(Integer numberOfCharges) {
        this.numberOfCharges = numberOfCharges;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(Integer companyCode) {
        this.companyCode = companyCode;
    }

    public Long getInsertDate() {
        return InsertDate;
    }

    public void setInsertDate(Long insertDate) {
        InsertDate = insertDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isRepository() {
        return repository == null ? false : repository;
    }

    public void setRepository(boolean repository) {
        this.repository = repository;
    }

    public DateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(DateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
}
