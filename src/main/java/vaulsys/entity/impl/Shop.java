package vaulsys.entity.impl;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.calendar.DayDate;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.contact.City;
import vaulsys.contact.Country;
import vaulsys.contact.State;
import vaulsys.customer.Account;
import vaulsys.customer.AccountType;
import vaulsys.customer.Core;
import vaulsys.customer.Currency;
import vaulsys.entity.Contract;
import vaulsys.entity.MerchantCategory;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.terminal.impl.Terminal;
import org.apache.commons.httpclient.StatusLine;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fine_shop")
@ForeignKey(name = "shop_fine_fk")
public class Shop extends FinancialEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    @ForeignKey(name = "shop_category_fk")
    private MerchantCategory category;

    @Column(name = "category", insertable = false, updatable = false)
    private Long categoryId;

    private String economicCode;

    private String agentCode;

    private Long nationalNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate.date", column = @Column(name = "contract_start_date")),
            @AttributeOverride(name = "endDate.date", column = @Column(name = "contract_end_date")),
            @AttributeOverride(name = "contractNumber", column = @Column(name = "contract_num"))})
    protected Contract contract;

    @AttributeOverride(name = "date", column = @Column(name = "lease_dt"))
    private DayDate leaseDate;

    @Transient
    protected Set<Terminal> terminals;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    @ForeignKey(name = "shop_merch_fk")
    protected Merchant owner;

    @Column(name = "owner", insertable = false, updatable = false)
    private Long ownerId;

    private Boolean mgrToShprk = false;

    public Long getOwnerId() {
        return ownerId;
    }

    /******** Financial Entity Version Properties ********/
    /**
     * ***** Start *******
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor")
    @ForeignKey(name = "shop_visitor_fk")
    protected Visitor visitor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "vis_type"))})
    private VisitorType visitorType = VisitorType.VISITOR_SUPPORTER;

    @Column(name = "visitor", insertable = false, updatable = false)
    private Long visitorId;

    public Long getVisitorId() {
        return visitorId;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
        if (visitor != null)
            visitorId = visitor.getId();
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public VisitorType getVisitorType() {
        return visitorType;
    }

    public void setVisitorType(VisitorType visitorType) {
        this.visitorType = visitorType;
    }


    /*protected Double discount;

     public Double getDiscount() {
         return discount;
     }

     public void setDiscount(Double discount) {
         this.discount = discount;
     }


     protected Double feeCoefficient;

     public Double getFeeCoefficient() {
         return feeCoefficient;
     }

     public void setFeeCoefficient(Double feeCoefficient) {
         this.feeCoefficient = feeCoefficient;
     }*/

    /******** End ********/
    /**
     * ***** Financial Entity Version Properties *******
     */

    public Shop() {
    }

    public MerchantCategory getOwnOrParentCategory() {
        if (this.category == null && owner != null)
            return owner.getCategory();
        return category;
    }

    public MerchantCategory getCategory() {
        return category;
    }

    public void setCategory(MerchantCategory category) {
        this.category = category;
        if (category != null)
            categoryId = category.getId();
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getOwnOrParentCategoryId() {
        if (this.categoryId == null && owner != null)
            return owner.getCategoryId();

        return this.categoryId;
    }

    @Override
    public FinancialEntityRole getRole() {
        return FinancialEntityRole.SHOP;
    }

    public Set<Terminal> getTerminals() {
        return terminals;
    }

    public void addTerminal(Terminal terminal) {
        if (terminals == null)
            terminals = new HashSet<Terminal>();
        terminals.add(terminal);
    }

    public Merchant getOwner() {
        return owner;
    }

    public void setOwner(Merchant owner) {
        this.owner = owner;
        if (owner != null)
            ownerId = owner.getId();
    }

    public Boolean isMgrToShprk() {
        return mgrToShprk;
    }

    public void setMgrToShprk(Boolean mgrToShprk) {
        this.mgrToShprk = mgrToShprk;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", name, code);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((categoryId == null) ? 0 : categoryId.hashCode());
        result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
        result = prime * result + ((visitorId == null) ? 0 : visitorId.hashCode());
        result = prime * result + ((visitorType == null) ? 0 : visitorType.hashCode());
        result = prime * result + ((contract == null) ? 0 : contract.hashCode());
        result = prime * result + ((economicCode == null) ? 0 : economicCode.hashCode());
        result = prime * result + ((nationalNumber == null) ? 0 : nationalNumber.hashCode());
        return result;
    }

    public String getEconomicCode() {
        return economicCode;
    }

    public void setEconomicCode(String economicCode) {
        this.economicCode = economicCode;
    }

    public Long getNationalNumber() {
        return nationalNumber;
    }

    public void setNationalNumber(Long nationalNumber) {
        this.nationalNumber = nationalNumber;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public DayDate getLeaseDate() {
        return leaseDate;
    }

    public void setLeaseDate(DayDate leaseDate) {
        this.leaseDate = leaseDate;
    }

    public Contract getOwnOrParentContract() {
        if (this.contract == null && owner != null)
            return owner.getContract();
        return contract;
    }

    public Long getOwnOrParentNationalNumber() {
        if (this.nationalNumber == null && owner != null)
            return owner.getNationalNumber();
        return nationalNumber;
    }

    public String getOwnOrParentEconomicCode() {
        if ((this.economicCode == null || this.economicCode.length() == 0) && owner != null)
            return owner.getEconomicCode();
        return economicCode;
    }

    @Override
    public Account getOwnOrParentAccount() {
        if (getAccount() == null && owner != null)
            return owner.getAccount();
        return getAccount();
    }

    @Override
    public AuthorizationProfile getOwnOrParentAuthorizationProfile() {
        if (this.authorizationProfile == null && owner != null)
            return owner.getOwnOrParentAuthorizationProfile();

        return super.getOwnOrParentAuthorizationProfile();

//		if (super.getOwnOrParentAuthorizationProfile() == null && owner != null)
//			return owner.getOwnOrParentAuthorizationProfile();
//		return super.getOwnOrParentAuthorizationProfile();
    }

    @Override
    public FeeProfile getOwnOrParentFeeProfile() {
        if (this.feeProfile == null && owner != null)
            return owner.getOwnOrParentFeeProfile();

        return super.getOwnOrParentFeeProfile();

//		if (super.getOwnOrParentFeeProfile() == null && owner != null)
//			return owner.getOwnOrParentFeeProfile();
//		return super.getOwnOrParentFeeProfile();
    }

    @Override
    public String getSafeAddress() {
        String address = super.getSafeAddress();
        if (address == null && owner != null)
            address = owner.getSafeAddress();
        return address;
    }

    @Override
    public String getSafeFullPhoneNumber() {
        String fullPhoneNumber = super.getSafeFullPhoneNumber();
        if (fullPhoneNumber == null && owner != null)
            fullPhoneNumber = owner.getSafeFullPhoneNumber();
        return fullPhoneNumber;
    }

    @Override
    public String getSafePhoneNumber() {
        String phoneNumber = super.getSafePhoneNumber();
        if (phoneNumber == null && owner != null)
            phoneNumber = owner.getSafePhoneNumber();
        return phoneNumber;
    }

    @Override
    public String getSafePhoneAreaCode() {
        String phoneAreaCode = super.getSafePhoneAreaCode();
        if (phoneAreaCode == null && owner != null)
            phoneAreaCode = owner.getSafePhoneAreaCode();
        return phoneAreaCode;
    }

    @Override
    public String getSafeWebsiteAddress() {
        String site = super.getSafeWebsiteAddress();
        if (site == null && owner != null)
            site = owner.getSafeWebsiteAddress();
        return site;
    }

    @Override
    public String getSafeEmail() {
        String email = super.getSafeEmail();
        if (email == null && owner != null)
            email = owner.getSafeEmail();
        return email;
    }

    @Override
    public Country getSafeCountry() {
        Country country = super.getSafeCountry();
        if (country == null && owner != null)
            country = owner.getSafeCountry();
        return country;
    }

    @Override
    public State getSafeState() {
        State state = super.getSafeState();
        if (state == null && owner != null)
            state = owner.getSafeState();
        return state;
    }

    @Override
    public City getSafeCity() {
        City city = super.getSafeCity();
        if (city == null && owner != null)
            city = owner.getSafeCity();
        return city;
    }

    @Override
    public String getSafePostalCode() {
        String postalCode = super.getSafePostalCode();
        if (postalCode == null && owner != null)
            postalCode = owner.getSafePostalCode();
        return postalCode;
    }

    @Override
    public String getSafeFullMobileNumber() {
        String mobile = super.getSafeFullMobileNumber();
        if (mobile == null && owner != null)
            mobile = owner.getSafeFullMobileNumber();
        return mobile;
    }

    @Override
    public String getSafeMobileAreaCode() {
        String areaCode = super.getSafeMobileAreaCode();
        if (areaCode == null && owner != null)
            areaCode = owner.getSafeMobileAreaCode();
        return areaCode;
    }

    @Override
    public String getSafeMobileNumber() {
        String mobile = super.getSafeMobileNumber();
        if (mobile == null && owner != null)
            mobile = owner.getSafeMobileNumber();
        return mobile;
    }

    @Override
    public String getSafeAccountHolderName() {
        String accountHolderName = super.getSafeAccountHolderName();
        if (accountHolderName == null && owner != null)
            accountHolderName = owner.getSafeAccountHolderName();
        return accountHolderName;
    }

    @Override
    public String getSafeAccountNumber() {
        String accNum = super.getSafeAccountNumber();
        if (accNum == null && owner != null)
            accNum = owner.getSafeAccountNumber();
        return accNum;
    }

    @Override
    public String getSafeCardNumber() {
        String cardNum = super.getSafeCardNumber();
        if (cardNum == null && owner != null)
            cardNum = owner.getSafeCardNumber();
        return cardNum;

    }

    @Override
    public Core getSafeCore() {
        Core core = super.getSafeCore();
        if (core == null && owner != null)
            core = owner.getSafeCore();
        return core;
    }

    @Override
    public Currency getSafeCurrency() {
        Currency currency = super.getSafeCurrency();
        if (currency == null && owner != null)
            currency = owner.getSafeCurrency();
        return currency;
    }

    @Override
    public AccountType getSafeAccountType() {
        AccountType type = super.getSafeAccountType();
        if (type == null && owner != null)
            type = owner.getSafeAccountType();
        return type;

    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }


    public String getSafeContractNumber() {
        return ((contract == null || contract.getContractNumber() == null) ? null : contract.getContractNumber());
    }

    public DayDate getSafeContractEndDate() {
        return ((contract == null || contract.getEndDate() == null) ? null : contract.getEndDate());
    }

public DayDate getSafeContractStartDate() {
        return ((contract == null || contract.getStartDate() == null)? null : contract.getStartDate());
    }
}
