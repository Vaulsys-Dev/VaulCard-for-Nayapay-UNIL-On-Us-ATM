package vaulsys.cms.base;

import vaulsys.persistence.IEntity;
import vaulsys.util.Util;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name="cms_cardrelation")
public class CMSCardRelation implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="CMS_CARD_REL_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_CARD_REL_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_CARD_REL_ID_SEQ")
            })
    private Long id;

    @Column(name="cardrelationid")
    private String card_relid;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nettrninfo")
	@Cascade(value = CascadeType.ALL )
	@ForeignKey(name="ifx_nettrninfo_fk")
    @Index(name="idx_ifx_nettrninfo")
    */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CR_CARDNO_FK")
    private CMSCard card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CR_ACCNO_FK")
    private CMSAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CR_CARDNO_FK")
    private CMSCustomer customer;

    @Column(name = "channel")
    private String channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_auth")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CR_CARDAUTH_FK")
    private CMSCardAuthorization cardAuth;

    @Column(name="TXN_PERM")
    private String txn_perm;

    @Column(name="product")
    String product;

    @Column(name="isdefault")
    private String isdefault;
    public CMSCardRelation()
    {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public CMSCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CMSCustomer customer) {
        this.customer = customer;
    }

    //public CMSProduct getProduct() {
    //    return product;
    //}

    //public void setProduct(CMSProduct product) {
    //    this.product = product;
    //}

//    public Channel getChannel() {
//        return channel;
//    }

//    public void setChannel(Channel channel) {
//        this.channel = channel;
//    }

    public CMSCardAuthorization getCardAuth() {
        return cardAuth;
    }

    public void setCardAuth(CMSCardAuthorization cardAuth) {
        this.cardAuth = cardAuth;
    }

    public String getCard_relid() {
        return card_relid;
    }

    public void setCard_relid(String card_relid) {
        this.card_relid = card_relid;
    }

    public String getTxn_perm() {
        return txn_perm;
    }

    public void setTxn_perm(String txn_perm) {
        if(Util.hasText(this.txn_perm)) {
            this.txn_perm += txn_perm + ",";
        }
        else {
            this.txn_perm = txn_perm + ",";
        }
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getProductId()
    {
        return product;
    }

    public void setProductId(String pid)
    {
        this.product = pid;
    }
    public String getIsdefault() {
        return isdefault;
    }

    public void setIsdefault(String isdefault) {
        this.isdefault = isdefault;
    }
}
