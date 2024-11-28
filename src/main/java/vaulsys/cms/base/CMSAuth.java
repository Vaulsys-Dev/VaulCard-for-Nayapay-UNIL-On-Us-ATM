package vaulsys.cms.base;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name="cms_auth")
public class CMSAuth implements IEntity<Integer> {
    @Id
    @GeneratedValue(generator="CMS_CARD_AUTH_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_CARD_AUTH_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_CARD_AUTH_ID_SEQ")
            })
    private Integer id;

    @Column(name="RELATIONSHIP")
    private String relation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CMS_CARD_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="cms_auth_card_fk")
    private CMSCard card;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CMS_ACCOUNT_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="cms_auth_acct_fk")
    private CMSAccount account;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CMS_CUSTOMER_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="cms_auth_cust_fk")
    private CMSCustomer customer;

    @Column(name="CHANNEL")
    private String channelId;

    @Column(name="OFFSET")
    private String offset;

    @Column(name="rem_retries")
    private String remRetries;

    @Column(name="status")
    private String status;

    @Column(name="reason_code")
    private String reasonCode;

    @Column(name="max_retries")
    private String maxRetries;

    @Column(name="is_default")
    private String isDefault;

    public CMSAuth()
    {}

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getRemRetries() {
        return remRetries;
    }

    public void setRemRetries(String remRetries) {
        this.remRetries = remRetries;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(String maxRetries) {
        this.maxRetries = maxRetries;
    }

    public CMSCard getCard() {
        return card;
    }

    public void setCard(CMSCard card) {
        this.card = card;
    }

    public CMSAccount getAccount() {
        return account;
    }

    public void setAccount(CMSAccount account) {
        this.account = account;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public CMSCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CMSCustomer customer) {
        this.customer = customer;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
