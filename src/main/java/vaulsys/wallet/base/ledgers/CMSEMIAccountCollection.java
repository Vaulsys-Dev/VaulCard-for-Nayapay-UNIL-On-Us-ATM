package vaulsys.wallet.base.ledgers;

import vaulsys.persistence.IEntity;
import vaulsys.webservice.walletcardmgmtwebservice.entity.FundBank;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name = "EMI_ACCOUNT_COLLECTION")
public class CMSEMIAccountCollection implements IEntity<Long> { //Raza This Class should be ReadOnly, Not removing public Setter as this class is also used by UI

    @Id
    @GeneratedValue(generator="CMS_EMI_COLLECTION_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "CMS_EMI_COLLECTION_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CMS_EMI_COLLECTION_ID_SEQ")
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

    @Column(name = "BRANCH_CODE")
    private String BranchCode;

    @Column(name = "STATUS")
    private String Status;

    //@Column(name = "BANK_CODE")
    //private String BankCode;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BANK_CODE")
    @ForeignKey(name = "fund_bank_fk")
    private FundBank BankCode;

    @Column(name = "LASTUPDATEDATE")
    private Date LastUpdateDate;

    @Column(name = "CREATEDATE")
    private Date CreateDate;

    @Column(name = "SOURCE_ENTITY")
    private String SourceEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMI_WALLET")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="EMI_ACCTCOLL_WLLT_FK1")
    private CMSEMIWallet emiwallet;

    @Column(name="CATEGORY")
    private String category;

    @Column(name="USER_ID")
    private String userid;

    //m.rehman: 12-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUSINESS_WALLET")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL )
    @ForeignKey(name="EMI_ACCTCOLL_BUS_WLLT_FK1")
    private CMSBusinessWallet businesswallet;
    //////////////////////////////////////////////////////////////////////////////////

    public CMSEMIAccountCollection()
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

    public String getBranchCode() {
        return BranchCode;
    }

    public void setBranchCode(String branchCode) {
        BranchCode = branchCode;
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

    public FundBank getBankCode() {
        return BankCode;
    }

    public void setBankCode(FundBank bankCode) {
        BankCode = bankCode;
    }

    public String getSourceEntity() {
        return SourceEntity;
    }

    public void setSourceEntity(String sourceEntity) {
        SourceEntity = sourceEntity;
    }

    public CMSEMIWallet getEmiwallet() {
        return emiwallet;
    }

    public void setEmiwallet(CMSEMIWallet emiwallet) {
        this.emiwallet = emiwallet;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    //m.rehman: 12-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    public CMSBusinessWallet getBusinesswallet() {
        return businesswallet;
    }

    public void setBusinesswallet(CMSBusinessWallet businesswallet) {
        this.businesswallet = businesswallet;
    }
    //////////////////////////////////////////////////////////////////////////////////
}
