package vaulsys.wallet.base.ledgers;

import vaulsys.cms.base.CMSAccount;
import vaulsys.persistence.IEntity;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Raza on 05-Nov-18.
 */
@Entity
@Table(name = "SALESTAX_LEDGER")
public class SalesTaxLedger implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="SALESTAX_LEDGER_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "SALESTAX_LEDGER_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SALESTAX_LEDGER_ID_SEQ")
            })
    private Long id;

    //m.rehman: for NayaPay, change column name as previous one is keyword
    //@Column(name = "DATE")
    @Column(name = "TXNDATE")
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="SALESTAX_ACCT_FK")
    private CMSAccount account;

    @Column(name = "TRANSACTION")
    private String transaction; //Transaction Description (Funds Tranfer, Withdrawal, BalanceInquiry)

    @Column(name = "TXNFLAG")
    private String txnFlag; //Debit/Credit

    @Column(name = "DEBITAMOUNT")
    private String debitAmount; //Segregated debit and Credit Amount For Reporting and UI

    @Column(name = "CREDITAMOUNT")
    private String creditAmount; //Segregated debit and Credit Amount For Reporting and UI

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "CLOSING_BALANCE")
    private String closingBalance;

    @Column(name = "PREVIOUS_BALANCE")
    private String previousBalance;

    @Column(name = "TXNREFERENCE")
    private String txnReference; //STAN as it will be used for tracing involving all entities (bank,1Link etc.)

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public CMSAccount getAccount() {
        return account;
    }

    public void setAccount(CMSAccount account) {
        this.account = account;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getTxnFlag() {
        return txnFlag;
    }

    public void setTxnFlag(String txnFlag) {
        this.txnFlag = txnFlag;
    }

    public String getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(String debitAmount) {
        this.debitAmount = debitAmount;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(String closingBalance) {
        this.closingBalance = closingBalance;
    }

    public String getTxnReference() {
        return txnReference;
    }

    public void setTxnReference(String txnReference) {
        this.txnReference = txnReference;
    }

    public String getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(String previousBalance) {
        this.previousBalance = previousBalance;
    }
}
