package vaulsys.cms.base;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by HP on 5/12/2017.
 */
@Entity
@Table(name = "cms_accountlimit")
public class CMSAccountLimit implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="CMS_CARDLIMIT_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_CARDLIMIT_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_CARDLIMIT_ID_SEQ")
            })
    private Long Id;

//    @Column(name = "limit_id")
//    private Long limitId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_id")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="cms_limit_fk")
    private CMSLimit limitId;

    @Column(name = "ACCT_NUMBER")
    private String acctNumber;

    @Column(name = "remaining_amount")
    private String remainingAmount;

    @Column(name = "remaining_frequency")
    private String remainingFrequency;

    @Column(name = "cycle_start_date")
    private String cycleStartDate;

    @Column(name = "cycle_end_date")
    private String cycleEndDate;

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    public CMSLimit getLimitId() {
        return limitId;
    }

    public void setLimitId(CMSLimit limitId) {
        this.limitId = limitId;
    }

    public String getAcctNumber() {
        return acctNumber;
    }

    public void setAcctNumber(String acctNumber) {
        this.acctNumber = acctNumber;
    }

    public String getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(String remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getRemainingFrequency() {
        return remainingFrequency;
    }

    public void setRemainingFrequency(String remainingFrequency) {
        this.remainingFrequency = remainingFrequency;
    }

    public String getCycleStartDate() {
        return cycleStartDate;
    }

    public void setCycleStartDate(String cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }

    public String getCycleEndDate() {
        return cycleEndDate;
    }

    public void setCycleEndDate(String cycleEndDate) {
        this.cycleEndDate = cycleEndDate;
    }

    /*
    public void checkCardLimit(Ifx ifx, CMSLimit limit) throws Exception {
        String limitType, query, acctNumber;
        List<CMSCardLimit> cardLimitFromDb;
        Map<java.lang.String, Object> dbParam;

        try {
            dbParam = new HashMap<String, Object>();

            dbParam.put("limitId", limit.getId());
            dbParam.put("acctNumber", ifx.getAppPAN());
            query = "from " + this.getClass().getName() + " cl "
                    + "where cl.limitId = :limitId "
                    + "and "
                    + "cl.acctNumber = :acctNumber";
            cardLimitFromDb = GeneralDao.Instance.find(query, dbParam);

            if (cardLimitFromDb.isEmpty()) {
                logger.error("Card Limit not found");
                ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
                throw new LimitNotFoundException("Card Limit not found");

            } else if (cardLimitFromDb.size() > 1) {
                logger.error("Multiple Card Limits found");
                ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
                throw new LimitNotFoundException("Multiple Card Limits found");

            } else {
                this. = cardLimitFromDb.get(0);
            }
        } catch(Exception e) {

        }
    }
    */
}
