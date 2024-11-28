package vaulsys.webservice.walletcardmgmtwebservice.entity;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by HP on 3/18/2019.
 */
@Entity
@Table(name = "fund_bank_gl")
public class FundBankGl implements IEntity<Long> {
    @Id
    @GeneratedValue(generator = "FUND_BANK_GL_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "FUND_BANK_GL_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "FUND_BANK_GL_ID_SEQ")
            })
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BANK_ID")
    @ForeignKey(name = "fund_bank_bal_fk")
    private FundBank bankId;

    @Column(name = "ACCOUNT_NATURE")
    private String accountNature;

    @Column(name = "TXN_ID")
    private String txnId;

    @Column(name = "TRANS_DT", updatable = false)
    private String tranDate;

    @Column(name = "DEBIT_ACCOUNT")
    private String debitAccount;

    @Column(name = "DEBIT_AMOUNT")
    private String debitAmount;

    @Column(name = "CREDIT_ACCOUNT")
    private String creditAccount;

    @Column(name = "CREDIT_AMOUNT")
    private String creditAmount;

    @Column(name = "CLOSING_BALANCE")
    private String closingBalance;

    @Column(name = "VOUCHER_ID")
    private String voucherId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public FundBank getBankId() {
        return bankId;
    }

    public void setBankId(FundBank bankId) {
        this.bankId = bankId;
    }

    public String getAccountNature() {
        return accountNature;
    }

    public void setAccountNature(String accountNature) {
        this.accountNature = accountNature;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getTranDate() {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Date lastSettDate = null;

        try {
            lastSettDate = format.parse(tranDate);

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            tranDate = df.format(lastSettDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tranDate;
    }

    public void setTranDate(String tranDetail) {
        this.tranDate = tranDetail;
    }

    public String getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(String debitAccount) {
        this.debitAccount = debitAccount;
    }

    public String getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(String debitAmount) {
        this.debitAmount = debitAmount;
    }

    public String getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(String creditAccount) {
        this.creditAccount = creditAccount;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(String closingBalance) {
        this.closingBalance = closingBalance;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }
}
