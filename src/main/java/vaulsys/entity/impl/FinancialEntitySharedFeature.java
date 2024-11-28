package vaulsys.entity.impl;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.persistence.IEntity;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "fine_shared_feature")
public class FinancialEntitySharedFeature implements IEntity<Long> {

	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "fee_prof")
	@ForeignKey(name = "fineshaftur_feeprof_fk")
	private FeeProfile feeProfile;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "clr_prof")
	@ForeignKey(name = "fineshaftur_clrprof_fk")
	private ClearingProfile clearingProfile;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "auth_prof")
	@ForeignKey(name = "fineshaftur_authprof_fk")
	private AuthorizationProfile authorizationProfile;

	@Column(name = "auth_prof", insertable = false, updatable = false)
	private Long authorizationProfileId;

	private boolean enabled = true;

	public FinancialEntitySharedFeature() {
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

	public void setClearingProfile(ClearingProfile clearingProfile) {
		this.clearingProfile = clearingProfile;
	}

	public AuthorizationProfile getAuthorizationProfile() {
		return authorizationProfile;
	}

	public void setAuthorizationProfile(AuthorizationProfile authorizationProfile) {
		this.authorizationProfile = authorizationProfile;
	}

	public Long getAuthorizationProfileId() {
		return authorizationProfileId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FinancialEntitySharedFeature copy() {
		FinancialEntitySharedFeature sharedFeature = new FinancialEntitySharedFeature();
		sharedFeature.setAuthorizationProfile(authorizationProfile);
		sharedFeature.setClearingProfile(clearingProfile);
		sharedFeature.setEnabled(enabled);
		sharedFeature.setFeeProfile(feeProfile);
		return sharedFeature;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("الگوی کارمزد: ").append(feeProfile != null ? feeProfile.getName() : "-").append("\n");

		builder.append("الگوی تسويه حساب: ").append(clearingProfile != null ? clearingProfile.getName() : "-").append("\n");

		builder.append("الگوی مجازشماری: ").append(authorizationProfile != null ? authorizationProfile.getName() : "-").append("\n");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorizationProfile == null) ? 0 : authorizationProfile.hashCode());
		result = prime * result + ((clearingProfile == null) ? 0 : clearingProfile.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((feeProfile == null) ? 0 : feeProfile.hashCode());
		return result;
	}
}
