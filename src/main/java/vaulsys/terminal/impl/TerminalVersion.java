package vaulsys.terminal.impl;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.persistence.IEntity;
import vaulsys.security.base.SecurityProfile;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

//@Entity
//@Table(name = "term_terminal_ver")

//@Inheritance(strategy = InheritanceType.JOINED)
@MappedSuperclass
public abstract class TerminalVersion implements IEntity<Long> {

	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="termver-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "termver-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "termver_seq")
    				})
	protected Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sec_prof")
//	@ForeignKey(name="term_vers_secprof_fk")
	private SecurityProfile securityProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auth_prof")
//	@ForeignKey(name="term_vers_authprof_fk")
	private AuthorizationProfile authorizationProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fee_prof")
//	@ForeignKey(name="term_vers_feeprof_fk")
	private FeeProfile feeProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clr_prof")
//	@ForeignKey(name="term_vers_clrprof_fk")
	private ClearingProfile clearingProfile;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lottery")
	private LotteryAssignmentPolicy lotteryPolicy;

	private boolean enabled = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shared_feature")
//	@ForeignKey(name="term_vers_shaftur_fk")
	protected TerminalSharedFeature sharedFeature;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_group")
//	@ForeignKey(name="term_vers_parentgrp_fk")
	private TerminalGroup parentGroup;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
//	@ForeignKey(name = "termver_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
			})
	protected DateTime createdDateTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SecurityProfile getOwnOrParentSecurityProfile() {
		if (securityProfile != null)
			return securityProfile;
		if (sharedFeature != null)
			return sharedFeature.getSecurityProfile();
		return null;
	}

	public SecurityProfile getSecurityProfile() {
		return securityProfile;
	}

	public void setSecurityProfile(SecurityProfile securityProfile) {
		this.securityProfile = securityProfile;
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

	public ClearingProfile getOwnOrParentClearingProfile() {
		if (clearingProfile != null)
			return clearingProfile;
		if (sharedFeature != null)
			return sharedFeature.getClearingProfile();
		return null;
	}

	public ClearingProfile getClearingProfile() {
		return clearingProfile;
	}

	public void setClearingProfile(ClearingProfile clearingProfile) {
		this.clearingProfile = clearingProfile;
	}
	
	public LotteryAssignmentPolicy getLotteryPolicy() {
		return lotteryPolicy;
	}

	public void setLotteryPolicy(LotteryAssignmentPolicy lotteryPolicy) {
		this.lotteryPolicy = lotteryPolicy;
	}

	public boolean isOwnOrParentEnabled() {
		if (sharedFeature == null)
			return enabled;
		return enabled && sharedFeature.isEnabled();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public TerminalSharedFeature getSharedFeature() {
//		if (sharedFeature == null)
//			sharedFeature = new TerminalSharedFeature();
		return sharedFeature;
	}

	public void setSharedFeature(TerminalSharedFeature sharedFeature) {
		this.sharedFeature = sharedFeature;
	}

	public TerminalGroup getParentGroup() {
		return parentGroup;
	}

	public void setParentGroup(TerminalGroup parentGroup) {
		this.parentGroup = parentGroup;
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
}
