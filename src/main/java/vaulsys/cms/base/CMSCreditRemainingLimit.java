package vaulsys.cms.base;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by HP on 5/12/2017.
 */
@Entity
@Table(name = "CMS_CREDIT_REMAININGLIMIT")
public class CMSCreditRemainingLimit implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="CMS_REMAININGLIMIT_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_REMAININGLIMIT_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_REMAININGLIMIT_ID_SEQ")
            })
    private Long Id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_id")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
    @ForeignKey(name="cms_prod_credit_limit_fk")
    private CMSProductCreditLimit limitId;

    @Column(name = "RELATION")
    private String relation;

    @Column(name = "remaining_amount")
    private String remainingAmount;

    @Column(name = "remaining_frequency")
    private String remainingFrequency;

    @Column(name = "cycle_start_date")
    private String cycleStartDate;

    @Column(name = "cycle_end_date")
    private String cycleEndDate;

    @Column(name = "IS_INDIVIDUAL")
    private String isIndividual;

    @Column(name = "REMAINING_PROFILE_LIMIT")
    private String remProfileLimit;

    @Column(name = "IS_CUST_PROFILE")
    private String isCustProfile;

    @Column(name = "MIN_AMOUNT")
    private String minAmount;

    @Column(name = "MAX_AMOUNT")
    private String maxAmount;

    @Column(name = "AVG_AMOUNT")
    private String avgAmount;

    @Column(name = "TXN_COUNT")
    private String txnAmount;

    //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="INDIVIDUAL_LIMIT_ID")
    @ForeignKey(name = "ind_limit_credit_limit_fk")
    private CMSSharedIndividualLimit individualLimitId;

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

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

    public String getIsIndividual() {
        return isIndividual;
    }

    public void setIsIndividual(String isIndividual) {
        this.isIndividual = isIndividual;
    }

    public String getRemProfileLimit() {
        return remProfileLimit;
    }

    public void setRemProfileLimit(String remProfileLimit) {
        this.remProfileLimit = remProfileLimit;
    }

    public String getIsCustProfile() {
        return isCustProfile;
    }

    public void setIsCustProfile(String isCustProfile) {
        this.isCustProfile = isCustProfile;
    }

    public String getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(String minAmount) {
        this.minAmount = minAmount;
    }

    public String getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(String maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getAvgAmount() {
        return avgAmount;
    }

    public void setAvgAmount(String avgAmount) {
        this.avgAmount = avgAmount;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public CMSProductCreditLimit getLimitId() {
        return limitId;
    }

    public void setLimitId(CMSProductCreditLimit limitId) {
        this.limitId = limitId;
    }

	//m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
    public CMSSharedIndividualLimit getIndividualLimitId() {
        return individualLimitId;
    }

    public void setIndividualLimitId(CMSSharedIndividualLimit individualLimitId) {
        this.individualLimitId = individualLimitId;
    }
	//////////////////////////////////////////////////////////////////////////////////////////

    /*
    public void checkCardLimit(Ifx ifx, CMSLimit limit) throws Exception {
        String limitType, query, cardNumber;
        List<CMSCardLimit> cardLimitFromDb;
        Map<java.lang.String, Object> dbParam;

        try {
            dbParam = new HashMap<String, Object>();

            dbParam.put("limitId", limit.getId());
            dbParam.put("cardNumber", ifx.getAppPAN());
            query = "from " + this.getClass().getName() + " cl "
                    + "where cl.limitId = :limitId "
                    + "and "
                    + "cl.cardNumber = :cardNumber";
            cardLimitFromDb = GeneralDao.Instance.find(query, dbParam);

            if (cardLimitFromDb.isEmpty()) {
                logger.error("Card Limit not found");
                ifx.setRsCode(ISOResponseCodes.INTERNAL_ERROR);
                throw new LimitNotFoundException("Card Limit not found");

            } else if (cardLimitFromDb.size() > 1) {
                logger.error("Multiple Card Limits found");
                ifx.setRsCode(ISOResponseCodes.INTERNAL_ERROR);
                throw new LimitNotFoundException("Multiple Card Limits found");

            } else {
                this. = cardLimitFromDb.get(0);
            }
        } catch(Exception e) {

        }
    }
    */
}
