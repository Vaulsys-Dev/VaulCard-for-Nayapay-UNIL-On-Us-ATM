package vaulsys.wallet.base.ledgers;

import vaulsys.cms.base.*;
import vaulsys.persistence.IEntity;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Raza on 05-Nov-18.
 */
@Entity
@Table(name = "CMS_WALLETBAL_LOG")
public class WalletBalanceLog implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="CMS_WALLETBAL_LOG_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_WALLETBAL_LOG_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_WALLETBAL_LOG_ID_SEQ")
            })
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WALLET")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_LEDG_TXN_FK")
    private CMSAccount wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LINKED_ACCOUNT")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_LEDG_TXN_FK")
    private CMSAccount linkedaccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMI_WALLET")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_LEDG_TXN_FK")
    private CMSEMIWallet emiwallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUSINESS_WALLET")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_WLLTBAL_FUNDWLLT_FK")
    private CMSBusinessWallet businesswallet;

    @Column(name = "TXN_NAME")
    private String Txnname;

    @Column(name = "TXN_AMOUNT")
    private String amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_WLLTBAL_TXN_FK")
    private WalletCMSWsEntity transaction;

    @Column(name = "CHANNEL_ID")
    private String channelid;

    @Column(name = "TXN_NATURE")
    private String txnnature;

    @Column(name = "ORIGINAL_BALANCE")
    private String originalbalance;

    @Column(name = "UPDATEED_BALANCE")
    private String updatedbalance;

    @Column(name = "CREATE_DATE")
    private Date createdate;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public CMSAccount getWallet() {
        return wallet;
    }

    public void setWallet(CMSAccount wallet) {
        this.wallet = wallet;
    }

    public CMSAccount getLinkedaccount() {
        return linkedaccount;
    }

    public void setLinkedaccount(CMSAccount linkedaccount) {
        this.linkedaccount = linkedaccount;
    }

    public CMSEMIWallet getEmiwallet() {
        return emiwallet;
    }

    public void setEmiwallet(CMSEMIWallet emiwallet) {
        this.emiwallet = emiwallet;
    }

    public String getTxnname() {
        return Txnname;
    }

    public void setTxnname(String txnname) {
        Txnname = txnname;
    }

    public WalletCMSWsEntity getTransaction() {
        return transaction;
    }

    public void setTransaction(WalletCMSWsEntity transaction) {
        this.transaction = transaction;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public String getTxnnature() {
        return txnnature;
    }

    public void setTxnnature(String txnnature) {
        this.txnnature = txnnature;
    }

    public String getOriginalbalance() {
        return originalbalance;
    }

    public void setOriginalbalance(String originalbalance) {
        this.originalbalance = originalbalance;
    }

    public String getUpdatedbalance() {
        return updatedbalance;
    }

    public void setUpdatedbalance(String updatedbalance) {
        this.updatedbalance = updatedbalance;
    }

    public CMSBusinessWallet getBusinesswallet() {
        return businesswallet;
    }

    public void setBusinesswallet(CMSBusinessWallet businesswallet) {
        this.businesswallet = businesswallet;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }
}
