package vaulsys.wallet.base.ledgers;

import vaulsys.cms.base.*;
import vaulsys.persistence.IEntity;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "WALLET_GENERAL_LEDGER")
public class WalletGeneralLedger implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="WALLET_LEDGER_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "WALLET_LEDGER_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "WALLET_LEDGER_ID_SEQ")
            })
    private Long id;

    @Column(name = "TXN_NAME")
    private String txnname;

    @Column(name = "TRANS_DT")
    private String transdatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WALLET")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WALLET_GL_FK1")
    private CMSAccount wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LNK_ACCOUNT")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WALLET_GL_FK3")
    private CMSAccount linkedaccount;

    @Column(name = "TXN_REF")
    private String txnrefnum;

    @Column(name = "AMOUNT")
    private String amount;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "TXNFLAG")
    private String txnflag;

    @Column(name = "WALLETFLAG")
    private Boolean walletflag;

    @Column(name = "MERCHANT_ID")
    private String merchantid;

    @Column(name = "AGENT_ID")
    private String agentid;

    @Column(name = "BILLER_ID")
    private String billerid;

    @Column(name = "SETTLEMENT_ACCOUNT") //Raza Using it as GL account configured on Switch against channel
    private String settlementaccount;           //Switch will send this account in ACCT_ID-2 Field

    @Column(name = "CLOSING_BALANCE")
    private String closingBalance;

    @Column(name = "PREVIOUS_BALANCE")
    private String previousBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WLLTGL_WSLOG_FK")
    private WalletCMSWsEntity transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMI_ACCOUNT")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WLLTGL_EMIWLLT_FK2")
    private CMSEMIWallet emiaccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUSINESS_ACCOUNT")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WLLTGL_FUNDWLLT_FK2")
    private CMSBusinessWallet businessaccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCT_COLLECTION_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="WLLTGL_COLACCT_FK")
    private CMSEMIAccountCollection collectionaccount;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTxnname() {
        return txnname;
    }

    public void setTxnname(String txnname) {
        this.txnname = txnname;
    }

    public String getTransdatetime() {
        return transdatetime;
    }

    public void setTransdatetime(String transdatetime) {
        this.transdatetime = transdatetime;
    }

    public CMSAccount getWallet() {
        return wallet;
    }

    public void setWallet(CMSAccount wallet) {
        this.wallet = wallet;
    }

    public String getTxnrefnum() {
        return txnrefnum;
    }

    public void setTxnrefnum(String txnrefnum) {
        this.txnrefnum = txnrefnum;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTxnflag() {
        return txnflag;
    }

    public void setTxnflag(String txnflag) {
        this.txnflag = txnflag;
    }

    public Boolean getWalletflag() {
        return walletflag;
    }

    public void setWalletflag(Boolean walletflag) {
        this.walletflag = walletflag;
    }

    public String getMerchantid() {
        return merchantid;
    }

    public void setMerchantid(String merchantid) {
        this.merchantid = merchantid;
    }

    public String getSettlementaccount() {
        return settlementaccount;
    }

    public void setSettlementaccount(String settlementaccount) {
        this.settlementaccount = settlementaccount;
    }

    public String getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(String closingBalance) {
        this.closingBalance = closingBalance;
    }

    public String getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(String previousBalance) {
        this.previousBalance = previousBalance;
    }

    public CMSAccount getLinkedaccount() {
        return linkedaccount;
    }

    public void setLinkedaccount(CMSAccount linkedaccount) {
        this.linkedaccount = linkedaccount;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getBillerid() {
        return billerid;
    }

    public void setBillerid(String billerid) {
        this.billerid = billerid;
    }

    public WalletCMSWsEntity getTransaction() {
        return transaction;
    }

    public void setTransaction(WalletCMSWsEntity transaction) {
        this.transaction = transaction;
    }

    public CMSEMIWallet getEmiaccount() {
        return emiaccount;
    }

    public void setEmiaccount(CMSEMIWallet emiaccount) {
        this.emiaccount = emiaccount;
    }

    public CMSBusinessWallet getBusinessaccount() {
        return businessaccount;
    }

    public void setBusinessaccount(CMSBusinessWallet businessaccount) {
        this.businessaccount = businessaccount;
    }

    public CMSEMIAccountCollection getCollectionaccount() {
        return collectionaccount;
    }

    public void setCollectionaccount(CMSEMIAccountCollection collectionaccount) {
        this.collectionaccount = collectionaccount;
    }
}
