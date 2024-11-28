package vaulsys.cms.base;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by HP on 5/12/2017.
 */
@Entity
@Table(name = "cms_limit")
//@Table(name = "cms_product_limit")
public class CMSLimit implements IEntity<Long> {

    @Id
    private Long Id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "transaction_type")
    private String transactionType;

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

    @Column(name = "is_shared")
    private String isShared;

    @Column(name = "is_active")
    private String isActive;

    @Override
    public Long getId() {
        return Id;
    }

    @Override
    public void setId(Long id) {

    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
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

    public String getIsShared() {
        return isShared;
    }

    public void setIsShared(String isShared) {
        this.isShared = isShared;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
}
