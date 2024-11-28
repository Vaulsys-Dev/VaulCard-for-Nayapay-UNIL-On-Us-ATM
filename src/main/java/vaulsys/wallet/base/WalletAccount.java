package vaulsys.wallet.base;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by HP on 4/21/2017.
 */
@Entity
@Table(name = "wallet_account")
public class WalletAccount implements IEntity<Long> {

    @Id
    private Long id;

    @Column(name = "WALLET_NUMBER")
    private String WalletNumber;

    @Column(name = "WALLET_TITLE")
    private String WalletTitle;

    @Column(name = "CURRENCY")
    private String Currency;

    @Column(name = "AVAILABLE_BALANCE")
    private String AvailableBalance;

    @Column(name = "ACTUAL_BALANCE")
    private String ActualBalance;

    @Column(name = "CUSTOMER_ID")
    private String CustomerID;

    @Column(name = "STATUS")
    private String Status;

    @Column(name = "LASTUPDATEDATE")
    private String LastUpdateDate;

    @Column(name = "CREATEDATE")
    private String CreateDate;

    public WalletAccount()
    {}

    public Long getId()
    {
        return this.id;
    }

    public void setId(Long Id)
    {
        this.id = Id;
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

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getLastUpdateDate() {
        return LastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        LastUpdateDate = lastUpdateDate;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public String getWalletNumber() {
        return WalletNumber;
    }

    public void setWalletNumber(String walletNumber) {
        WalletNumber = walletNumber;
    }

    public String getWalletTitle() {
        return WalletTitle;
    }

    public void setWalletTitle(String walletTitle) {
        WalletTitle = walletTitle;
    }

    @Override
    public String toString() {
        return WalletNumber;
    }
}
