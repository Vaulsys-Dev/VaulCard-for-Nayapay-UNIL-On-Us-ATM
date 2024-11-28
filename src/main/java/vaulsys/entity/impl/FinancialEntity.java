package vaulsys.entity.impl;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.contact.City;
import vaulsys.contact.Contact;
import vaulsys.contact.Country;
import vaulsys.contact.State;
import vaulsys.customer.Account;
import vaulsys.customer.AccountType;
import vaulsys.customer.Core;
import vaulsys.customer.Currency;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;
import vaulsys.util.Util;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "fine_financial_entity")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FinancialEntity implements IEntity<Long> {

    @Id
    @GeneratedValue(generator = "fine-seq-gen2")
    @org.hibernate.annotations.GenericGenerator(name = "fine-seq-gen2", strategy = "vaulsys.IdKeepingSequenceGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "fine_code_seq"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "200010")
            })
//	@SequenceGenerator(name = "fine-seq-gen2", allocationSize = 1, sequenceName = "fine_code_seq")
//    @GeneratedValue(generator="fine-seq-gen")
//    @GenericGenerator(name="fine_seq
//
//
// _gen", strategy="org.hibernate.id.enhanced.SequenceStyleGenerator",
//    		parameters={
//    			@Parameter(name="optimizer", value="pooled"),
//    			@Parameter(name="increment_size", value="100"),
//    			@Parameter(name="sequence_name", value="financial_entity_sequence"),
//    			@Parameter(name="initial_value", value="100000")
//    	})
    protected Long code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "fine_user_fk")
    protected User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    protected DateTime createdDateTime;

    /******** Financial Entity Version Properties ********/
    /**
     * ***** Start *******
     */
    protected String name;

    protected String nameEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_prof")
    @ForeignKey(name = "fine_feeprof_fk")
    protected FeeProfile feeProfile;

    @Column(name = "fee_prof", insertable = false, updatable = false)
    private Long feeProfileId;

    public Long getFeeProfileId() {
        return feeProfileId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_prof")
    @ForeignKey(name = "fine_authprof_fk")
    protected AuthorizationProfile authorizationProfile;

    @Column(name = "auth_prof", insertable = false, updatable = false)
    private Long authorizationProfileId;

    public Long getAuthorizationProfileId() {
        return authorizationProfileId;
    }

    //	@ManyToOne(fetch=FetchType.LAZY)
    //	@JoinColumn(name = "account")
    //	@ForeignKey(name="fine_account_fk")
    @Embedded
    protected Account account;

    protected boolean enabled = true;

    @Embedded
    protected Contact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_feature")
    @ForeignKey(name = "fine_sharedfeature_fk")
    protected FinancialEntitySharedFeature sharedFeature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group")
    @ForeignKey(name = "fine_parentgrp_fk")
    private FinancialEntityGroup parentGroup;

    @Column(name = "parent_group", insertable = false, updatable = false)
    private Long parentGroupId;

    public Long getParentGroupId() {
        return parentGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FeeProfile getOwnOrParentFeeProfile() {
        if (feeProfile != null)
            return feeProfile;
        if (sharedFeature != null)
            return sharedFeature.getFeeProfile();
        return null;
    }

    public FeeProfile getFeeProfile() {
        return feeProfile;
    }

    public void setFeeProfile(FeeProfile feeProfile) {
        this.feeProfile = feeProfile;
        if (feeProfile != null)
            feeProfileId = feeProfile.getId();
    }

    public Account getOwnOrParentAccount() {
        return account;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isOwnOrParentEnabled() {
        if (sharedFeature == null)
            return enabled;
        return (enabled && sharedFeature.isEnabled());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public AuthorizationProfile getOwnOrParentAuthorizationProfile() {
        if (authorizationProfile != null)
            return authorizationProfile;
        if (sharedFeature != null)
            return sharedFeature.getAuthorizationProfile();
        return null;
    }

    public Long getOwnOrParentAuthorizationProfileId() {
        if (authorizationProfileId != null)
            return authorizationProfileId;
        if (sharedFeature != null)
            return sharedFeature.getAuthorizationProfileId();
        return null;
    }

    public AuthorizationProfile getAuthorizationProfile() {
        return authorizationProfile;
    }

    public void setAuthorizationProfile(AuthorizationProfile authorizationProfile) {
        this.authorizationProfile = authorizationProfile;
        if (authorizationProfile != null)
            authorizationProfileId = authorizationProfile.getId();
    }

    public FinancialEntitySharedFeature getSharedFeature() {
        return sharedFeature;
    }

    public void setSharedFeature(FinancialEntitySharedFeature sharedFeature) {
        this.sharedFeature = sharedFeature;
    }

    public FinancialEntityGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(FinancialEntityGroup parent) {
        this.parentGroup = parent;
        if (parent != null)
            parentGroupId = parent.getId();
    }

    public FeeProfile getNearestFeeProfile() {
        return getOwnOrParentFeeProfile();
        // IFeeProfile ownFP = getFeeProfile();
        // if (ownFP != null)
        // return ownFP;
        // return sharedFeature.getFeeProfile();
    }
    /******** End ********/
    /**
     * ***** Financial Entity Version Properties *******
     */

    protected FinancialEntity() {
    }

    public FinancialEntity(Long code) {
        this.code = code;
    }

    public Long getId() {
        return this.getCode();
    }

    public void setId(Long id) {
        this.setCode(id);
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public DateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(DateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public abstract FinancialEntityRole getRole();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((account == null) ? 0 : account.hashCode());
        result = prime * result + ((authorizationProfileId == null) ? 0 : authorizationProfileId.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + ((feeProfileId == null) ? 0 : feeProfileId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parentGroupId == null) ? 0 : parentGroupId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof FinancialEntity))
            return false;
        FinancialEntity that = (FinancialEntity) o;
        if (this.code != null)
            return this.code.equals(that.code);
        return o == this;
    }

    @Override
    public String toString() {
        return name != null ? name : "-";
    }

    public String getSafeAddress() {
        return (contact == null || contact.getAddress() == null) ? null : contact.getAddress().getAddress();
    }

    public String getSafeFullPhoneNumber() {
        if (contact == null || contact.getPhoneNumber() == null)
            return null;

        String phoneNumber = "";
        if (Util.hasText(contact.getPhoneNumber().getNumber())) {
            phoneNumber = contact.getPhoneNumber().getNumber();
            String phoneAreaCode = contact.getPhoneNumber().getAreaCode();
            if (Util.hasText(phoneAreaCode))
                phoneNumber = phoneAreaCode + "-" + phoneNumber;
        }
        return phoneNumber;
    }

    public String getSafePhoneAreaCode() {
        if (contact == null || contact.getPhoneNumber() == null)
            return null;

        String phoneAreaCode = "";
        if (Util.hasText(contact.getPhoneNumber().getAreaCode()))
            phoneAreaCode = contact.getPhoneNumber().getAreaCode();

        return phoneAreaCode;
    }

    public String getSafePhoneNumber() {
        if (contact == null || contact.getPhoneNumber() == null)
            return null;

        String phoneNumber = "";
        if (Util.hasText(contact.getPhoneNumber().getNumber()))
            phoneNumber = contact.getPhoneNumber().getNumber();

        return phoneNumber;
    }

    public String getSafeContactName() {
        return (contact == null) ? null : contact.getName();
    }

    public String getSafeWebsiteAddress() {
        return (contact == null || contact.getWebsite() == null) ? null : contact.getWebsite().getWebsiteAddress();
    }

    public String getSafeEmail() {
        return (contact == null || contact.getWebsite() == null) ? null : contact.getWebsite().getEmail();
    }

    public String getSafeMobileAreaCode() {
        if (contact == null || contact.getMobileNumber() == null)
            return null;

        String phoneAreaCode = "";
        if (Util.hasText(contact.getMobileNumber().getAreaCode()))
            phoneAreaCode = contact.getMobileNumber().getAreaCode();

        return phoneAreaCode;
    }

    public String getSafeFullMobileNumber() {
        if (contact == null || contact.getMobileNumber() == null)
            return null;

        String phoneNumber = "";
        if (Util.hasText(contact.getMobileNumber().getNumber())) {
            phoneNumber = contact.getMobileNumber().getNumber();

            if (Util.hasText(contact.getMobileNumber().getAreaCode()))
                phoneNumber = contact.getMobileNumber().getAreaCode() + "-" + phoneNumber;
        }

        return phoneNumber;
    }

    public String getSafeMobileNumber() {
        if (contact == null || contact.getMobileNumber() == null)
            return null;

        String phoneNumber = "";
        if (Util.hasText(contact.getMobileNumber().getNumber())) {
            phoneNumber = contact.getMobileNumber().getNumber();
        }
        return phoneNumber;
    }

    public Country getSafeCountry() {
        return (contact == null || contact.getAddress() == null) ? null : contact.getAddress().getCountry();
    }

    public State getSafeState() {
        return (contact == null || contact.getAddress() == null) ? null : contact.getAddress().getState();
    }

    public City getSafeCity() {
        return (contact == null || contact.getAddress() == null) ? null : contact.getAddress().getCity();
    }

    public Long getSafeCountryCode() {
        return (contact == null || contact.getAddress() == null) ? null : contact.getAddress().getCountryId();
    }

    public Long getSafeStateCode() {
        return (contact == null || contact.getAddress() == null) ? null : contact.getAddress().getStateId();
    }

    public Long getSafeCityCode() {
        return (contact == null || contact.getAddress() == null) ? null : contact.getAddress().getCityId();
    }

    public String getSafePostalCode() {
        return ((contact == null || contact.getAddress() == null) ? null : contact.getAddress().getPostalCode());
    }

    public String getSafeAccountHolderName() {
        return ((account == null || account.getAccountHolderName() == null) ? null : account.getAccountHolderName());
    }

    public String getSafeAccountNumber() {
        return ((account == null || account.getAccountNumber() == null) ? null : account.getAccountNumber());
    }
    public String getSafeCardNumber() {
        return ((account == null || account.getCardNumber() == null) ? null : account.getCardNumber());
    }

    public Core getSafeCore() {
        return ((account == null || account.getCore() == null) ? null : account.getCore());
    }

    public Currency getSafeCurrency() {
        return ((account == null || account.getCurrency() == null) ? null : account.getCurrency());
    }

    public AccountType getSafeAccountType() {
        return ((account == null || account.getType() == null) ? null : account.getType());
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }


}
