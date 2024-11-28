package vaulsys.wallet.base.ledgers;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name = "CMS_BUSINESS_WALLET")
public class CMSBusinessWallet implements IEntity<Long> { //Raza This Class should be ReadOnly, Not removing public Setter as this class is also used by UI

    @Id
    @GeneratedValue(generator="CMS_BUSINESS_WALLET_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_BUSINESS_WALLET_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_BUSINESS_WALLET_ID_SEQ")
            })
    private Long id;

    @Column(name = "ACC_NUMBER")
    private String AccountNumber;

    @Column(name = "ACC_TITLE")
    private String AccountTitle;

    @Column(name = "TYPE")
    private String AccountType;

    @Column(name = "CURRENCY")
    private String Currency;

    @Column(name = "AVAILABLE_BALANCE")
    private String AvailableBalance;

    @Column(name = "ACTUAL_BALANCE")
    private String ActualBalance;

//    @Column(name = "CUSTOMER_ID")
//    private String CustomerID;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="CMS_FUNDS_ACCT_CUST_FK")
    private CMSBusinessEntity customer;

    @Column(name = "BRANCH_ID")
    private String BranchId;

    @Column(name = "STATUS")
    private String Status;

    @Column(name = "LASTUPDATEDATE")
    private Date LastUpdateDate;

    @Column(name = "CREATEDATE")
    private Date CreateDate;

    @Column(name="ACC_LEVEL")
    private String level;

    @Column(name="UPGRADE_DATE")
    private Date upgradedate;

    @Column(name="CATEGORY")
    private String category; //Raza for Wallet and Linked Account

    @Column(name = "ACCT_ID")
    private String acctId;

    @Column(name = "USER_ID")
    private String userId;

    public CMSBusinessWallet()
    {}

    public Long getId()
    {
        return this.id;
    }

    public void setId(Long Id)
    {
        this.id = Id;
    }

    public String getAccountNumber()
    {
        return this.AccountNumber;
    }

    public void setAccountNumber(String accNumber)
    {
        this.AccountNumber = accNumber;
    }

    public String getAccountTitle()
    {
        return this.AccountTitle;
    }

    public void setAccountTitle(String accTitle)
    {
        this.AccountTitle = accTitle;
    }


    public String getAccountType() {
        return AccountType;
    }

    public void setAccountType(String accountType) {
        AccountType = accountType;
    }


    public String getCurrency() {
        return Currency;
    }

    public void setCurrency(String currency) {
        Currency = currency;
    }

    public String getAvailableBalance() {
        return AvailableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        AvailableBalance = availableBalance;
    }

    public String getActualBalance() {
        return ActualBalance;
    }

    public void setActualBalance(String actualBalance) {
        ActualBalance = actualBalance;
    }

//    public String getCustomerID() {
//        return CustomerID;
//    }
//
//    public void setCustomerID(String customerID) {
//        CustomerID = customerID;
//    }

    public String getBranchId() {
        return BranchId;
    }

    public void setBranchId(String branchId) {
        BranchId = branchId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Date getLastUpdateDate() {
        return LastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        LastUpdateDate = lastUpdateDate;
    }

    public Date getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(Date createDate) {
        CreateDate = createDate;
    }

    @Override
    public String toString() {
        return AccountNumber;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Date getUpgradedate() {
        return upgradedate;
    }

    public void setUpgradedate(Date upgradedate) {
        this.upgradedate = upgradedate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public CMSBusinessEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CMSBusinessEntity customer) {
        this.customer = customer;
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
