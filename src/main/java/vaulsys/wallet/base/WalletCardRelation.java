package vaulsys.wallet.base;

import vaulsys.cms.base.CMSAccount;
import vaulsys.cms.base.CMSCard;
import vaulsys.cms.base.CMSCardAuthorization;
import vaulsys.cms.base.CMSCustomer;
import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name="cms_cardrelation")
public class WalletCardRelation implements IEntity<Long> {
    @Id
    private Long id;

    @Column(name="cardrelationid")
    private String card_relid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CR_CARDNO_FK")
    private WalletCard card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CR_ACCNO_FK")
    private WalletAccount account;

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

    public WalletCardRelation()
    {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WalletCard getCard() {
        return card;
    }

    public void setCard(WalletCard card) {
        this.card = card;
    }

    public WalletAccount getAccount() {
        return account;
    }

    public void setAccount(WalletAccount account) {
        this.account = account;
    }

    public CMSCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CMSCustomer customer) {
        this.customer = customer;
    }

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
        this.txn_perm = txn_perm;
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
