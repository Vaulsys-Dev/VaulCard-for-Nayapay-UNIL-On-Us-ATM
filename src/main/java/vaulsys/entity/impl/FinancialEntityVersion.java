package vaulsys.entity.impl;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.calendar.DateTime;
import vaulsys.contact.Contact;
import vaulsys.customer.Account;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

//@Entity
//@Table(name = "fine_financial_entity_ver")

//@Inheritance(strategy = InheritanceType.JOINED)
@MappedSuperclass
public abstract class FinancialEntityVersion implements IEntity<Long> {

	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="finever-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "finever-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "finever_seq")
    				})

//	@GeneratedValue(generator="foreign")
//	@GenericGenerator(name="foreign", strategy="foreign", parameters={
//			@Parameter(name="property", value="parent")
//	})
	protected Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
//	@ForeignKey(name = "finever_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))})
	protected DateTime createdDateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fee_prof")
	protected FeeProfile feeProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auth_prof")
	protected AuthorizationProfile authorizationProfile;

	//	@ManyToOne(cascade = CascadeType.ALL)
	@Embedded
	protected Account account;

	protected boolean enabled = true;

	@Embedded
	protected Contact contact;

	protected String name;

	protected String nameEn;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shared_feature")
	protected FinancialEntitySharedFeature sharedFeature;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_group")
	private FinancialEntityGroup parentGroup;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public AuthorizationProfile getAuthorizationProfile() {
		return authorizationProfile;
	}
	
	public void setAuthorizationProfile(AuthorizationProfile authorizationProfile) {
		this.authorizationProfile = authorizationProfile;
	}

	public FinancialEntitySharedFeature getSharedFeature() {
//        if (sharedFeature == null)
//            sharedFeature = new FinancialEntitySharedFeature();
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
	}

	public FeeProfile getNearestFeeProfile() {
		return getOwnOrParentFeeProfile();
		// IFeeProfile ownFP = getFeeProfile();
		// if (ownFP != null)
		// return ownFP;
		// return sharedFeature.getFeeProfile();
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
}
