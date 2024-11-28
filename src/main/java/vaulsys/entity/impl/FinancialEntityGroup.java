package vaulsys.entity.impl;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
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
@Table(name = "fine_financial_entity_group")
public class FinancialEntityGroup implements IEntity<Long> {
	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name = "own_fee_prof")
	private boolean ownFeeProfile;
	
	@Column(name = "own_clr_prof")
	private boolean ownClearingProfile;
	
	@Column(name = "own_auth_prof")
	private boolean ownAuthorizationProfile;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_group")
	@ForeignKey(name = "finegrp_parentgrp_fk")
	private FinancialEntityGroup parentGroup;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "shared_feature")
	@ForeignKey(name = "finegrp_sharedfeature_fk")
	private FinancialEntitySharedFeature sharedFeature;
	
	private boolean enabled = true;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "finegrp_user_fk")
	protected User creatorUser;
	
	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))})
	protected DateTime createdDateTime;

	public FinancialEntityGroup() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOwnFeeProfile() {
		return ownFeeProfile;
	}

	public void setOwnFeeProfile(boolean ownFeeProfile) {
		this.ownFeeProfile = ownFeeProfile;
	}

	public boolean isOwnClearingProfile() {
		return ownClearingProfile;
	}

	public void setOwnClearingProfile(boolean ownClearingProfile) {
		this.ownClearingProfile = ownClearingProfile;
	}

	public boolean isOwnAuthorizationProfile() {
		return ownAuthorizationProfile;
	}

	public void setOwnAuthorizationProfile(boolean ownAuthorizationProfile) {
		this.ownAuthorizationProfile = ownAuthorizationProfile;
	}

	public FinancialEntityGroup getParentGroup() {
		return parentGroup;
	}

	public void setParentGroup(FinancialEntityGroup parentGroup) {
		this.parentGroup = parentGroup;
	}

	public void addChild(FinancialEntityGroup entityGroup) {
		entityGroup.setParentGroup(this);
	}

	public FinancialEntitySharedFeature getSafeSharedFeature() {
		if (sharedFeature == null)
			sharedFeature = new FinancialEntitySharedFeature();
		return sharedFeature;
	}

	public FinancialEntitySharedFeature getSharedFeature() {
		return sharedFeature;
	}

	public void setSharedFeature(FinancialEntitySharedFeature sharedFeature) {
		this.sharedFeature = sharedFeature;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return name;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FinancialEntityGroup))
			return false;
		FinancialEntityGroup other = (FinancialEntityGroup) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
