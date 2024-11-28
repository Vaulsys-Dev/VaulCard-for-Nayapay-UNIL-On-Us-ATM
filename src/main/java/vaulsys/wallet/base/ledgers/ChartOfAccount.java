package vaulsys.wallet.base.ledgers;


import org.hibernate.annotations.ForeignKey;
import vaulsys.persistence.IEntity;
import vaulsys.webservice.walletcardmgmtwebservice.entity.FundBankAccount;

import javax.persistence.*;

/**
 * Created by HP on 24/05/2019.
 */

@Entity
@Table(name = "CHARTOFACCOUNT")
public class ChartOfAccount implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="CHARTOFACCOUNT_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CHARTOFACCOUNT_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CHARTOFACCOUNT_ID_SEQ")
            })
    private Long id;

    @Column(name = "ACCOUNT_NAME")
    private String accountName;

//    @Column(name = "LEVEL_TYPE")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LEVEL_TYPE")
    @ForeignKey(name = "chartOfAccLevelType_fk")
    private ChartOfAccountLevel levelType;

    @Column(name = "ACCOUNT_CODE")
    private String accountCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARENT")
    @ForeignKey(name = "chartOfAccSelfJoin_fk")
    private ChartOfAccount parent;

    @Column(name = "LEVEL_VALUE")
    private String levelValue;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LINK_ACCOUNT")
    private CMSEMIAccountCollection linkAcc;

    @Column(name = "IS_ACCOUNT")
    private Boolean isAccount;

    @Column(name = "AVAILABLE_BALANCE")
    private String availabaleBalance;

    @Column(name = "CREATE_DATE")
    private String createDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public ChartOfAccountLevel getLevelType() {
        return levelType;
    }

    public void setLevelType(ChartOfAccountLevel levelType) {
        this.levelType = levelType;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public ChartOfAccount getParent() {
        return parent;
    }

    public void setParent(ChartOfAccount parent) {
        this.parent = parent;
    }

    public String getLevelValue() {
        return levelValue;
    }

    public void setLevelValue(String levelValue) {
        this.levelValue = levelValue;
    }

    public CMSEMIAccountCollection getLinkAcc() {
        return linkAcc;
    }

    public void setLinkAcc(CMSEMIAccountCollection linkAcc) {
        this.linkAcc = linkAcc;
    }

    public Boolean getAccount() {
        return isAccount;
    }

    public void setAccount(Boolean account) {
        isAccount = account;
    }

    public String getAvailabaleBalance() {
        return availabaleBalance;
    }

    public void setAvailabaleBalance(String availabaleBalance) {
        this.availabaleBalance = availabaleBalance;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return accountCode+ "-" +accountName  ;
    }
}
