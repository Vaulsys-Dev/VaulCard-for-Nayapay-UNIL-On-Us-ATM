package vaulsys.wallet.base.ledgers;

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
@Table(name = "BUSINESS_WALLETBAL_LOG")
public class BusinessWalletBalanceLog implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="BUSINESSWLLTBAL_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "BUSINESSWLLTBAL_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "BUSINESSWLLTBAL_SEQ")
            })
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUSINESS_WALLET_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="BUSINESS_WLLTBALLOG_BUSINESSWLLT_FK")
    private CMSBusinessWallet businessWallet;

    @Column(name = "TXN_NAME")
    private String Txnname;

    @Column(name = "TXN_AMOUNT")
    private String amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="BUSINESS_COLBALLOG_TXN_FK")
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


    public String getTxnname() {
        return Txnname;
    }

    public void setTxnname(String txnname) {
        Txnname = txnname;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public CMSBusinessWallet getBusinessWallet() {
        return businessWallet;
    }

    public void setBusinessWallet(CMSBusinessWallet businessWallet) {
        this.businessWallet = businessWallet;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }
}
