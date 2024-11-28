package vaulsys.terminal.impl;

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
@Table(name = "term_terminal_group")
public class TerminalGroup implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "own_fee_profile")
    private boolean ownFeeProfile;

    @Column(name = "own_clearing_profile")
    private boolean ownClearingProfile;

    @Column(name = "own_auth_profile")
    private boolean ownAuthorizationProfile;

    @Column(name = "own_sec_profile")
    private boolean ownSecurityProfile;
    
    @Column(name = "own_daily_message")
    private boolean ownDailyMessage;

    @Column(name = "own_lottery_plc")
    private boolean ownLotteryPolicy;
    
    @Column(name = "own_pos_grp_files_ver")
    private boolean ownPOSGrpFilesVersion;

    @Column(name = "own_atm_config")
    private boolean ownATMConfiguration;

    @Column(name = "own_pos_config")
    private boolean ownPOSConfiguration;
    
    @Column(name = "own_disc_profile")
    private boolean ownDiscountProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group")
	@ForeignKey(name="termgrp_parentgrp_fk")
    private TerminalGroup parentGroup;
    
    @Column(name="parent_group", insertable=false, updatable=false)
    private Long parentGroupId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "shared_feature")
    @ForeignKey(name="termgrp_shaftur_fk")
    private TerminalSharedFeature sharedFeature;
    
    @Column(name="shared_feature", insertable=false, updatable=false)
    private Long sharedFeatureId;

    private boolean enabled = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "termgrp_user_fk")
	protected User creatorUser;

    @AttributeOverrides({
        @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
        @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time")) })
	protected DateTime createdDateTime;

    public TerminalGroup() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isOwnDailyMessage() {
        return ownDailyMessage;
    }

    public void setOwnDailyMessage(boolean ownDailyMessage) {
        this.ownDailyMessage = ownDailyMessage;
    }

    public boolean isOwnLotteryPolicy() {
		return ownLotteryPolicy;
	}

	public void setOwnLotteryPolicy(boolean ownLotteryPolicy) {
		this.ownLotteryPolicy = ownLotteryPolicy;
	}

	public boolean isOwnSecurityProfile() {
    	return ownSecurityProfile;
    }
    
    public void setOwnSecurityProfile(boolean ownSecurityProfile) {
    	this.ownSecurityProfile = ownSecurityProfile;
    }

	public boolean isOwnPOSGrpFilesVersion() {
		return ownPOSGrpFilesVersion;
	}

	public void setOwnPOSGrpFilesVersion(boolean ownPOSGrpFilesVersion) {
		this.ownPOSGrpFilesVersion = ownPOSGrpFilesVersion;
	}
	
	public boolean isOwnATMConfiguration() {
		return ownATMConfiguration;
	}

	public void setOwnATMConfiguration(boolean ownATMConfiguration) {
		this.ownATMConfiguration = ownATMConfiguration;
	}
	
	public boolean isOwnPOSConfiguration() {
		return ownPOSConfiguration;
	}

	public void setOwnPOSConfiguration(boolean ownPOSConfiguration) {
		this.ownPOSConfiguration = ownPOSConfiguration;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public boolean isOwnDiscountProfile() {
		return ownDiscountProfile;
	}

	public void setOwnDiscountProfile(boolean ownDiscountProfile) {
		this.ownDiscountProfile = ownDiscountProfile;
	}

	public TerminalGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(TerminalGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    public void addChild(TerminalGroup terminalGroup) {
        terminalGroup.setParentGroup(this);
    }

    public TerminalSharedFeature getSafeSharedFeature() {
        if (sharedFeature == null)
            sharedFeature = new TerminalSharedFeature();
        return sharedFeature;
    }
    
    public TerminalSharedFeature getSharedFeature() {
		return sharedFeature;
	}

    public void setSharedFeature(TerminalSharedFeature sharedFeature) {
        this.sharedFeature = sharedFeature;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof TerminalGroup))
			return false;

		IEntity entity = (IEntity) obj;
		if (this.getId() != null)
			return this.getId().equals(entity.getId());

		return this == obj;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (ownAuthorizationProfile ? 1231 : 1237);
		result = prime * result + (ownClearingProfile ? 1231 : 1237);
		result = prime * result + (ownDailyMessage ? 1231 : 1237);
		result = prime * result + (ownFeeProfile ? 1231 : 1237);
		result = prime * result + (ownSecurityProfile ? 1231 : 1237);
		result = prime * result + (ownATMConfiguration ? 1231 : 1237);
		result = prime * result + (ownPOSConfiguration ? 1231 : 1237);
		result = prime * result + ((parentGroupId == null) ? 0 : parentGroupId.hashCode());
		result = prime * result + ((sharedFeatureId == null) ? 0 : sharedFeatureId.hashCode());
		return result;
	}
}
