package vaulsys.cms.base;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import vaulsys.webservice.walletcardmgmtwebservice.entity.SwitchTransactionCodes;

import javax.persistence.*;

/**
 * Created by HP on 5/12/2017.
 */
@Entity
@Table(name = "CMS_SHARED_INDIVIDUAL_LIMIT")
public class CMSSharedIndividualLimit implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="CMS_REMAININGLIMIT_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_REMAININGLIMIT_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_REMAININGLIMIT_ID_SEQ")
            })
    private Long Id;

    //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEBIT_PROD_LIMIT_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="cms_debit_prod_sharedlimit_fk")
    private CMSProductDebitLimit cmsProdDebitLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREDIT_PROD_LIMIT_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="cms_credit_prod_sharedlimit_fk")
    private CMSProductCreditLimit cmsProductCreditLimit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ForeignKey(name = "ind_limit_prod_fk")
    private CMSProduct productId;

    @Column(name = "channel_id")
    private String channelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_type")
    @ForeignKey(name = "debit_limit_tran_fk")
    private SwitchTransactionCodes transactionType;

    @Column(name = "limit_type")
    private String limitType;

    @Column(name = "frequency_type")
    private String frequencyType;

    @Column(name = "amount")
    private String amount;

    @Column(name = "cycle_length")
    private String cycleLength;

    @Column(name = "cycle_length_type")
    private String cycleLengthType;

    @Column(name = "frequency_length")
    private String frequencyLength;

    @Column(name = "frequency_length_type")
    private String frequencyLengthType;

    @Column(name = "CARD_NUMBER")
    private String relation;

    @Column(name = "LIMIT_CATEGORY")
    private String limitCategory;

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    public CMSProductDebitLimit getCmsProdDebitLimit() {
        return cmsProdDebitLimit;
    }

    public void setCmsProdDebitLimit(CMSProductDebitLimit limitId) {
        this.cmsProdDebitLimit = limitId;
    }

    public CMSProduct getProductId() {
        return productId;
    }

    public void setProductId(CMSProduct productId) {
        this.productId = productId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public SwitchTransactionCodes getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(SwitchTransactionCodes transactionType) {
        this.transactionType = transactionType;
    }

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(String cycleLength) {
        this.cycleLength = cycleLength;
    }

    public String getCycleLengthType() {
        return cycleLengthType;
    }

    public void setCycleLengthType(String cycleLengthType) {
        this.cycleLengthType = cycleLengthType;
    }

    public String getFrequencyLength() {
        return frequencyLength;
    }

    public void setFrequencyLength(String frequencyLength) {
        this.frequencyLength = frequencyLength;
    }

    public String getFrequencyLengthType() {
        return frequencyLengthType;
    }

    public void setFrequencyLengthType(String frequencyLengthType) {
        this.frequencyLengthType = frequencyLengthType;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getLimitCategory() {
        return limitCategory;
    }

    public void setLimitCategory(String limitCategory) {
        this.limitCategory = limitCategory;
    }

    public CMSProductCreditLimit getCmsProductCreditLimit() {
        return cmsProductCreditLimit;
    }

    public void setCmsProductCreditLimit(CMSProductCreditLimit cmsProductCreditLimit) {
        this.cmsProductCreditLimit = cmsProductCreditLimit;
    }
}
