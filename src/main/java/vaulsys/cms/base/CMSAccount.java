package vaulsys.cms.base;

import vaulsys.persistence.IEntity;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name = "cms_account")
public class CMSAccount implements IEntity<Long> { //Raza This Class should be ReadOnly, Not removing public Setter as this class is also used by UI

    @Id
    @GeneratedValue(generator="CMS_ACCOUNT_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_ACCOUNT_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_ACCOUNT_ID_SEQ")
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
    @ForeignKey(name="CMSACCOUNT_CUST_FK")
    private CMSCustomer customer;

    @Column(name = "BRANCH_ID")
    private String BranchId;

    @Column(name = "STATUS")
    private String Status;

    @Column(name = "LASTUPDATEDATE")
    private Date LastUpdateDate;

    @Column(name = "CREATEDATE")
    private Date CreateDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="PRODUCT_ID")
    private CMSProduct product;

    @Column(name="ACC_LEVEL")
    private String level;

    @Column(name="UPGRADE_DATE")
    private Date upgradedate;

    @Column(name="CATEGORY")
    private String category; //Raza for Wallet and Linked Account

    @Column(name = "ACCT_ALIAS")
    private String acctalias;

    @Column(name = "ISPRIMARY")
    private String isprimary;

    @Column(name = "ACCT_ID")
    private String acctId;

    @Column(name = "USER_ID")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID", nullable = true, updatable = true)
    @ForeignKey(name="account_card_fk")
    @Index(name="idx_account_card")
    private CMSCard card; //Raza adding for Card-To-Account mapping

    // Asim Shahzad, Date : 12th March 2021, Tracking ID : VP-NAP-202103113 / VC-NAP-202103113
    @Column(name = "NAME_ON_CARD")
    private String nameOnCard;

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }
    // ========================================================================================

    // Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241

    @Column(name = "IBAN")
    private String iBan;

    public String getiBan() {
        return iBan;
    }

    public void setiBan(String iBan) {
        this.iBan = iBan;
    }

    // ======================================================================================

    public CMSAccount()
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

    public CMSProduct getProduct() {
        return product;
    }

    public void setProduct(CMSProduct product) {
        this.product = product;
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

    public CMSCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CMSCustomer customer) {
        this.customer = customer;
    }

    public String getAcctalias() {
        return acctalias;
    }

    public void setAcctalias(String acctalias) {
        this.acctalias = acctalias;
    }

    public String getIsprimary() {
        return isprimary;
    }

    public void setIsprimary(String isprimary) {
        this.isprimary = isprimary;
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

    public CMSCard getCard() {
        return card;
    }

    public void setCard(CMSCard card) {
        this.card = card;
    }
}
