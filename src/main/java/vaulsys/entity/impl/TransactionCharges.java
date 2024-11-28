package vaulsys.entity.impl;

import vaulsys.calendar.DayDate;
import vaulsys.cms.base.CMSProduct;
import vaulsys.entity.Tax;
import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by Mati on 16/10/2019.
 */
@Entity
@Table(name = "TRANSACTION_CHARGES")
public class TransactionCharges implements IEntity<Long> {
    @Id
    private Long id;

    @Column(name = "TXN_TYPE")
    private String txnType;

    @Column(name = "CHANNEL_ID")
    private String channelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private CMSProduct product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAX_ID")
    private Tax tax;

    @Column(name = "AMOUNT_TYPE")
    private String amountType;

    @Column(name = "AMOUNT_VALUE")
    private String amountValue;

    @Column(name = "STATUS")
    private String status;

    @AttributeOverride(name = "date", column = @Column(name = "CREATE_DATE"))
    private DayDate createDate;

    @AttributeOverride(name = "date", column = @Column(name = "UPDATE_DATE"))
    private DayDate updateDate;

    @Column(name = "IS_SLAB")
    private String isSlab;

    @Column(name = "MIN_SLAB_AMOUNT")
    private String minSlabAmount;

    @Column(name = "MAX_SLAB_AMOUNT")
    private String maxSlabAmount;

    //m.rehman: Euronet integration, adding local/international txn check
    @Column(name = "IS_INTL_TXN")
    private String isIntlTxn;

    //Arsalan Akhter, Date: 08_Apr_2021, Ticket# VP-NAP-202103291_VC-NAP-202103291 Change in Debit card charges
    @Column(name = "IS_PREV_AVAIL")
    private String isPrevAvail;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String tcnType) {
        this.txnType = tcnType;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public CMSProduct getProduct() {
        return product;
    }

    public void setProduct(CMSProduct product) {
        this.product = product;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public String getAmountValue() {
        return amountValue;
    }

    public void setAmountValue(String amountValue) {
        this.amountValue = amountValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DayDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DayDate createDate) {
        this.createDate = createDate;
    }

    public DayDate getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DayDate updateDate) {
        this.updateDate = updateDate;
    }

    public String getIsSlab() {
        return isSlab;
    }

    public void setIsSlab(String isSlab) {
        this.isSlab = isSlab;
    }

    public String getMinSlabAmount() {
        return minSlabAmount;
    }

    public void setMinSlabAmount(String minSlabAmount) {
        this.minSlabAmount = minSlabAmount;
    }

    public String getMaxSlabAmount() {
        return maxSlabAmount;
    }

    public void setMaxSlabAmount(String maxSlabAmount) {
        this.maxSlabAmount = maxSlabAmount;
    }

    //m.rehman: Euronet integration, adding local/international txn check
    public String getIsIntlTxn() {
        return isIntlTxn;
    }

    public void setIsIntlTxn(String isIntlTxn) {
        this.isIntlTxn = isIntlTxn;
    }

    //Arsalan Akhter, Date: 08_Apr_2021, Ticket# VP-NAP-202103291_VC-NAP-202103291 Change in Debit card charges
    public String getIsPrevAvail() { return isPrevAvail; }

    public void setIsPrevAvail(String isPrevAvail) { this.isPrevAvail = isPrevAvail; }
    //////////////////////////////////////////////////////////
}
