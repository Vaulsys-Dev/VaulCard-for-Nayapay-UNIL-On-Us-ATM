package vaulsys.webservice.walletcardmgmtwebservice.entity;

import vaulsys.persistence.IEntity;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by HP on 3/13/2019.
 */
@Entity
@Table(name = "fund_bank_account")
public class FundBankAccount implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="FUND_BANK_ACCOUNT_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "FUND_BANK_ACCOUNT_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "FUND_BANK_ACCOUNT_ID_SEQ")
            })
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BANK_ID")
    @ForeignKey(name = "fund_account_bank_fk")
    private FundBank bankId;

    @Column(name = "ACCOUNT_NATURE")
    private String accountNature;
	
	@Column(name = "ACCOUNT_DESCRIPTION")
    private String accountDescription;

    @Column(name = "ACCOUNT_NUMBER")
    private String accountNumber;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FUND_INVEST", updatable = false)
    protected FundInvestment fundInvest;
    */

    @Column(name = "TITLE")
    private String title;

    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "BRANCH_NAME")
    private String branchName;

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

    public String getAccountDescription() {
        return accountDescription;
    }
	
    public void setAccountDescription(String accountDescription) {
        this.accountDescription = accountDescription;
    }
	
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /*
    public FundInvestment getFundInvest() {
        return fundInvest;
    }

    public void setFundInvest(FundInvestment fundInvest) {
        this.fundInvest = fundInvest;
    }
    */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public String toString() {
        return bankId.getBankAcro() + " " + accountNumber;
    }
}
