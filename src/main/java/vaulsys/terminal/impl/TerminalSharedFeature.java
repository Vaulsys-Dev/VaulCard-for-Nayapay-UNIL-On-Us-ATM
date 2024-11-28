package vaulsys.terminal.impl;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.discount.DiscountProfile;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.persistence.IEntity;
import vaulsys.security.base.SecurityProfile;
import vaulsys.terminal.atm.ATMConfiguration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_shared_feature")
public class TerminalSharedFeature implements IEntity<Long> {

	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fee_prof")
	@ForeignKey(name = "term_sha_ftur_feeprof_fk")
	private FeeProfile feeProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dis_prof")
	@ForeignKey(name = "term_sha_ftur_disprof_fk")
	private DiscountProfile discountProfile;
	
	@Column(name = "dis_prof", insertable = false, updatable = false)
	private Long discountProfileId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clr_prof")
	@ForeignKey(name = "term_sha_ftur_clrprof_fk")
	private ClearingProfile clearingProfile;

	@Column(name = "clr_prof", insertable = false, updatable = false)
	private Long clearingProfileId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auth_prof")
	@ForeignKey(name = "term_sha_ftur_authprof_fk")
	private AuthorizationProfile authorizationProfile;
	
	@Column(name = "auth_prof", insertable = false, updatable = false)
	private Long authorizationProfileId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sec_prof")
	@ForeignKey(name = "term_sha_ftur_secprof_fk")
	private SecurityProfile securityProfile;

	@Column(name = "sec_prof", insertable = false, updatable = false)
	private Long securityProfileId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mtn_policy")
	@ForeignKey(name = "term_sha_ftur_mtnPolicy_fk")
	private ChargeAssignmentPolicy chargePolicy;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lotery_policy")
	@ForeignKey(name = "term_sha_ftur_loteryPolicy_fk")
	private LotteryAssignmentPolicy lotteryPolicy;
	
	@Column(name = "lotery_policy", insertable = false, updatable = false)
	private Integer lotteryPolicyId;

	private boolean enabled = true;

	@Column(name = "daily_message")
	private String dailyMessage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "atmconfig")
	@ForeignKey(name = "term_sha_ftur_atmconfig_fk")
	private ATMConfiguration configuration;
	
	@Column(name = "atmconfig", insertable = false, updatable = false)
	private Long configurationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pos_grp_files_ver")
	@ForeignKey(name = "pos_grp_files_ver_fk")
	private POSGroupFilesVersion posFilesVersion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pos_cfg")
	@ForeignKey(name = "term_sha_ftur_posconfig_fk")
	private POSConfiguration posConfiguration;

	public TerminalSharedFeature() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FeeProfile getFeeProfile() {
		return feeProfile;
	}

	public void setFeeProfile(FeeProfile feeProfile) {
		this.feeProfile = feeProfile;
	}

	public ClearingProfile getClearingProfile() {
		return clearingProfile;
	}

	public Long getDiscountProfileId() {
		return discountProfileId;
	}
	
	public Long getClearingProfileId() {
		return clearingProfileId;
	}

	public void setClearingProfile(ClearingProfile clearingProfile) {
		this.clearingProfile = clearingProfile;
	}

	public AuthorizationProfile getAuthorizationProfile() {
		return authorizationProfile;
	}

	public Long getAuthorizationProfileId() {
		return authorizationProfileId;
	}

	public void setAuthorizationProfile(AuthorizationProfile authorizationProfile) {
		this.authorizationProfile = authorizationProfile;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDailyMessage() {
		return dailyMessage;
	}

	public void setDailyMessage(String dailyMessage) {
		this.dailyMessage = dailyMessage;
	}

	public SecurityProfile getSecurityProfile() {
		return securityProfile;
	}

	public Long getSecurityProfileId() {
		return securityProfileId;
	}
	
	public Integer getLotteryPolicyId(){
		return lotteryPolicyId;
	}

	public void setSecurityProfile(SecurityProfile securityProfile) {
		this.securityProfile = securityProfile;
	}

	public Long getConfigurationId() {
		return configurationId;
	}

	public ATMConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ATMConfiguration configuration) {
		this.configuration = configuration;
	}

	public void setChargePolicy(ChargeAssignmentPolicy chargePolicy) {
		this.chargePolicy = chargePolicy;
	}

	public ChargeAssignmentPolicy getChargePolicy() {
		return chargePolicy;
	}
	
	public void setLotteryPolicy(LotteryAssignmentPolicy lotteryPolicy) {
		this.lotteryPolicy = lotteryPolicy;
	}
	
	public LotteryAssignmentPolicy getLotteryPolicy() {
		return lotteryPolicy;
	}

	public POSGroupFilesVersion getPosFilesVersion() {
		return posFilesVersion;
	}

	public void setPosFilesVersion(POSGroupFilesVersion posFilesVersion) {
		this.posFilesVersion = posFilesVersion;
	}

	public POSConfiguration getPosConfiguration() {
		return posConfiguration;
	}

	public void setPosConfiguration(POSConfiguration posConfiguration) {
		this.posConfiguration = posConfiguration;
	}

	public TerminalSharedFeature copy() {
		TerminalSharedFeature sharedFeature = new TerminalSharedFeature();

		sharedFeature.setFeeProfile(feeProfile);
		sharedFeature.setDiscountProfile(discountProfile);
		sharedFeature.setAuthorizationProfile(authorizationProfile);
		sharedFeature.setChargePolicy(chargePolicy);
		sharedFeature.setLotteryPolicy(lotteryPolicy);
		sharedFeature.setClearingProfile(clearingProfile);
		sharedFeature.setConfiguration(configuration);
		sharedFeature.setDailyMessage(dailyMessage);
		sharedFeature.setEnabled(enabled);
		sharedFeature.setSecurityProfile(securityProfile);
		sharedFeature.setPosFilesVersion(posFilesVersion);
		sharedFeature.setPosConfiguration(posConfiguration);

		return sharedFeature;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("الگوی کارمزد: ").append(feeProfile != null ? feeProfile.getName() : "-").append("\n");
		
		builder.append("الگوی تخفیف: ").append(discountProfile != null ? discountProfile.getName() : "-").append("\n");

		builder.append("الگوی تسويه حساب: ").append(clearingProfile != null ? clearingProfile.getName() : "-").append("\n");

		builder.append("الگوی مجازشماری: ").append(authorizationProfile != null ? authorizationProfile.getName() : "-").append("\n");

		builder.append("الگوی امنيتی: ").append(securityProfile != null ? securityProfile.getName() : "-").append("\n");

		builder.append("پيام روز: ").append(dailyMessage != null ? dailyMessage : "-").append("\n");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorizationProfile == null) ? 0 : authorizationProfile.hashCode());
		result = prime * result + ((chargePolicy == null) ? 0 : chargePolicy.hashCode());
		result = prime * result + ((lotteryPolicy == null) ? 0 : lotteryPolicy.hashCode());
		result = prime * result + ((clearingProfile == null) ? 0 : clearingProfile.hashCode());
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((dailyMessage == null) ? 0 : dailyMessage.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((feeProfile == null) ? 0 : feeProfile.hashCode());
		result = prime * result + ((discountProfile == null) ? 0 : discountProfile.hashCode());
		result = prime * result + ((securityProfile == null) ? 0 : securityProfile.hashCode());
		return result;
	}

	public DiscountProfile getDiscountProfile() {
		return discountProfile;
	}

	public void setDiscountProfile(DiscountProfile discountProfile) {
		this.discountProfile = discountProfile;
	}
}
