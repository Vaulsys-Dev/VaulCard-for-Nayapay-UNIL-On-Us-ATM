package vaulsys.terminal.impl;

import vaulsys.auditlog.Auditable;
import vaulsys.auditlog.AuditableProperty;
import vaulsys.auditlog.SimpleProperty;
import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.IMD;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.TerminalStatus;
import vaulsys.transaction.Transaction;
import vaulsys.user.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_terminal")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Terminal implements IEntity<Long>,Auditable<Long>  {

	@Id
	@GeneratedValue(generator = "term-seq-gen2")
    // @SequenceGenerator(name = "term-seq-gen2", allocationSize = 1, sequenceName = "term_code_seq")
    @org.hibernate.annotations.GenericGenerator(name = "term-seq-gen2", strategy = "vaulsys.IdKeepingSequenceGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "term_code_seq"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "200010")
            })
//    @GeneratedValue(generator="term-seq-gen")
//    @GenericGenerator(name="term_seq_gen", strategy="org.hibernate.id.enhanced.SequenceStyleGenerator", 
//    		parameters={
//    			@Parameter(name="optimizer", value="pooled"),
//    			@Parameter(name="increment_size", value="100"),
//    			@Parameter(name="sequence_name", value="terminal_sequence"),
//    			@Parameter(name="initial_value", value="100000")
//    	})    
	private Long code;

//    @ManyToOne(fetch = FetchType.LAZY)
//    protected FinancialEntity owner;

	@Column
	private DateTime firstTrxDate = DateTime.UNKNOWN;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_trx")
	@ForeignKey(name = "term_lasttrx_fk")
	private Transaction lastTransaction;

	@Column(name = "last_trx", insertable = false, updatable = false)
	private Long lastTransactionId;
	
	public Long getLastTransactionId() {
		return lastTransactionId;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_trx_in")
	@ForeignKey(name = "term_lasttrxin_fk")
	private Transaction lastIncomingTransaction;
	
	@Column(name = "last_trx_in", insertable = false, updatable = false)
	private Long lastIncomingTransactionId;
	
	public Long getLastIncomingTransactionId() {
		return lastIncomingTransactionId;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "terminal")
	private Set<SecureKey> keySet;

	@Embedded
	@AttributeOverride(name = "status", column = @Column(name = "status"))
	TerminalStatus status = TerminalStatus.NOT_INSTALL;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "term_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
			})
	protected DateTime createdDateTime;

	/******** Abstract Terminal Version Properties ********/
	/**
	 * ***** Start *******
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sec_prof")
	@ForeignKey(name = "term_secprof_fk")
	private SecurityProfile securityProfile;

	@Column(name = "sec_prof", insertable = false, updatable = false)
	private Long securityProfileId;

	@Column(name = "doc_desc_pat")
	private String documentDescriptionPattern;
	
	
	public String getDocumentDescriptionPattern() {
		return documentDescriptionPattern;
	}

	public void setDocumentDescriptionPattern(String documentDescription) {
		this.documentDescriptionPattern = documentDescription;
	}
	
	@Column(name = "trx_num_pat")
	private String transactionNumberPattern;
  
  
	public String getTransactionNumberPattern() {
		return transactionNumberPattern;
	}

	public void setTransactionNumberPattern(String documentNumber) {
		this.transactionNumberPattern = documentNumber;
	}

	public Long getSecurityProfileId() {
		return securityProfileId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auth_prof")
	@ForeignKey(name = "term_authprof_fk")
	private AuthorizationProfile authorizationProfile;

	@Column(name = "auth_prof", insertable = false, updatable = false)
	private Long authorizationProfileId;

	public Long getAuthorizationProfileId() {
		return authorizationProfileId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fee_prof")
	@ForeignKey(name = "term_feeprof_fk")
	private FeeProfile feeProfile;

	@Column(name = "fee_prof", insertable = false, updatable = false)
	private Long feeProfileId;

	public Long getFeeProfileId() {
		return feeProfileId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clr_prof")
	@ForeignKey(name = "term_clrprof_fk")
	private ClearingProfile clearingProfile;

	@Column(name = "clr_prof", insertable = false, updatable = false)
	private Long clearingProfileId;

	public Long getClearingProfileId() {
		return clearingProfileId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mtn_charge")
	@ForeignKey(name = "term_charge_plc_fk")
	private ChargeAssignmentPolicy chargePolicy;

	@Column(name = "mtn_charge", insertable = false, updatable = false)
	private Integer chargePolicyId;

	public Integer getChargePolicyId() {
		return chargePolicyId;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lottery")
	@ForeignKey(name = "lotery_plc_fk")
	private LotteryAssignmentPolicy lotteryPolicy;
	
	@Column(name = "lottery", insertable = false, updatable = false)
	private Integer lotteryPolicyId;
	
	public Integer getLotteryPolicyId() {
		return lotteryPolicyId;
	}

	private boolean enabled = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shared_feature")
	@ForeignKey(name = "term_shaftur_fk")
	protected TerminalSharedFeature sharedFeature;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_group")
	@ForeignKey(name = "term_parentgrp_fk")
	private TerminalGroup parentGroup;

	@Column(name = "parent_group", insertable = false, updatable = false)
	private Long parentGroupId;

	public Long getParentGroupId() {
		return parentGroupId;
	}

	public SecurityProfile getOwnOrParentSecurityProfile() {
		if (securityProfile != null)
			return securityProfile;
		if (sharedFeature != null)
			return sharedFeature.getSecurityProfile();
		return null;
	}

	public Long getOwnOrParentSecurityProfileId() {
		if (securityProfileId != null)
			return securityProfileId;
		if (sharedFeature != null)
			return sharedFeature.getSecurityProfileId();
		return null;
	}
	
	public SecurityProfile getSecurityProfile() {
		return securityProfile;
	}

	public void setSecurityProfile(SecurityProfile securityProfile) {
		this.securityProfile = securityProfile;
		if(securityProfile!=null)
			securityProfileId = securityProfile.getId();
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
		if(authorizationProfile!=null)
			authorizationProfileId = authorizationProfile.getId();
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
		if(feeProfile!=null)
			feeProfileId = feeProfile.getId();
	}

	public ClearingProfile getOwnOrParentClearingProfile() {
		if (clearingProfile != null)
			return clearingProfile;
		if (sharedFeature != null)
			return sharedFeature.getClearingProfile();
		return null;
	}

	public Long getOwnOrParentClearingProfileId() {
		if (clearingProfileId != null)
			return clearingProfileId;
		if (sharedFeature != null)
			return sharedFeature.getClearingProfileId();
		return null;
	}

	public ClearingProfile getClearingProfile() {
		return clearingProfile;
	}

	public void setClearingProfile(ClearingProfile clearingProfile) {
		this.clearingProfile = clearingProfile;
		if(clearingProfile!=null)
			clearingProfileId = clearingProfile.getId();
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
		if(parentGroup!=null)
			parentGroupId = parentGroup.getId();
	}
	/******** End ********/
	/**
	 * ***** Abstract Terminal Version Properties *******
	 */

	protected Terminal() {
	}

	public Terminal(Long code) {
		this.code = code;
	}

	public abstract FinancialEntity getOwner();
	
	public abstract Long getOwnerId();

	public abstract TerminalType getTerminalType();
//    {
//    	return owner;
//    }

//    public void setOwner(FinancialEntity owner){
//    	this.owner = owner;
//    }

	/* public boolean enabled() {
			  return getVersion().isEnabled();
		 }

		 public void setEnabled(boolean enabled) {
			  createVersionCopy().setEnabled(enabled);
		 }

		 public ITerminalSharedFeature getSharedFeature() {
			 return getVersion().getSharedFeature();
		 }

		 public void setSharedFeature(ITerminalSharedFeature sharedFeature) {
			  createVersionCopy().setSharedFeature(sharedFeature);
		 }

		 public ITerminalGroup getParentGroup() {
			  return getVersion().getParentGroup();
		 }

		 public void setParentGroup(ITerminalGroup parentGroup) {
			 createVersionCopy().setParentGroup(parentGroup);
		 }
		  public IFeeProfile getFeeProfile() {
			  return getVersion().getFeeProfile();
		 }

		 public void setFeeProfile(IFeeProfile feeProfile) {
			  createVersionCopy().setFeeProfile(feeProfile);
		 }

		 public ClearingProfile getClearingProfile() {
			 return getVersion().getClearingProfile();
		 }

		 public void setClearingProfile(ClearingProfile clearingProfile) {
			  createVersionCopy().setClearingProfile(clearingProfile);
		 }

		 public AuthorizationProfile getAuthorizationProfile() {
			 return getVersion().getAuthorizationProfile();
		 }

		 public void setAuthorizationProfile(AuthorizationProfile authorizationProfile) {
			  createVersionCopy().setAuthorizationProfile(authorizationProfile);
		 }

		 public SecurityProfile getSecurityProfile() {
			 return getVersion().getSecurityProfile();
		 }

		 public void setSecurityProfile(SecurityProfile securityProfile) {
			  createVersionCopy().setSecurityProfile(securityProfile);
		 }
		 */

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

	abstract public TerminalClearingMode getClearingMode();

	public DateTime getFirstTrxDate() {
		return firstTrxDate;
	}

	public void setFirstTrxDate(DateTime firstTrxDate) {
		if (DateTime.UNKNOWN.equals(this.firstTrxDate) || TerminalStatus.NOT_INSTALL.equals(this.status)) {
			this.firstTrxDate = firstTrxDate;
			this.status = TerminalStatus.INSTALL;
		}
//        if (this instanceof POSTerminal) {
//            POSTerminal pos = (POSTerminal) this;
//            pos.setStatus(TerminalStatus.INSTALL);
//        }
	}

//    public KeySet getKeySet() {
//		return keySet;
//	}
//
//	public void setKeySet(KeySet keySet) {
//		this.keySet = keySet;
//	}

//	public Set<SecureDESKey> getOldKeySet() {
//		return oldKeySet;
//	}
//
//	public void setOldKeySet(Set<SecureDESKey> oldKeySet) {
//		this.oldKeySet = oldKeySet;
//	}

	public TerminalStatus getStatus() {
		return status;
	}

	public void setStatus(TerminalStatus status) {
		this.status = status;
	}

	 public String getSafeDailyMessage() {
                String result = null;
                if (sharedFeature != null)
                        result =  sharedFeature.getDailyMessage();
                if (result == null)
                        result = "";
                return result;
        }


	public String getDailyMessage() {
		if (sharedFeature != null)
			return sharedFeature.getDailyMessage();
		return null;
	}

	public SecureKey getKeyByType(String keyType) {
		for (SecureKey key : keySet) {
			if (key.getKeyType().equals(keyType))
				return key;
		}
		return null;
	}

	public Set<SecureKey> getKeySet() {
		return keySet;
	}

	public void addSecureKey(SecureKey secureKey) {
		if (keySet == null)
			keySet = new HashSet<SecureKey>();
		secureKey.setTerminal(this);
		keySet.add(secureKey);
	}

	public void removeKeySet() {
		keySet = null;
	}

	public void setChargePolicy(ChargeAssignmentPolicy chargePolicy) {
		this.chargePolicy = chargePolicy;
		if(chargePolicy!=null)
			chargePolicyId = chargePolicy.getId();
	}

	public ChargeAssignmentPolicy getOwnOrParentChargePolicy() {
		if (chargePolicy != null)
			return chargePolicy;

		if (sharedFeature != null)
			return sharedFeature.getChargePolicy();

		return null;
	}

	public ChargeAssignmentPolicy getChargePolicy() {
		return chargePolicy;
	}
	
	public void setLotteryPolicy(LotteryAssignmentPolicy lotteryPolicy) {
		this.lotteryPolicy = lotteryPolicy;
		if(lotteryPolicy!=null)
			lotteryPolicyId = lotteryPolicy.getId();
	}
	
	public LotteryAssignmentPolicy getOwnOrParentLotteryPolicy() {
		if (lotteryPolicy != null)
			return lotteryPolicy;
		
		if (sharedFeature != null)
			return sharedFeature.getLotteryPolicy();
		
		return null;
	}
	
	public Integer getOwnOrParentLotteryPolicyId() {
		if (lotteryPolicy != null)
			return lotteryPolicyId;
		
		if (sharedFeature != null)
			return sharedFeature.getLotteryPolicyId();
		
		return null;
	}
	
	
	public LotteryAssignmentPolicy getLotteryPolicy() {
		return lotteryPolicy;
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
		if (code != null)
			return code.toString();
		return "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorizationProfileId == null) ? 0 : authorizationProfileId.hashCode());
		result = prime * result + ((chargePolicyId == null) ? 0 : chargePolicyId.hashCode());
		result = prime * result + ((lotteryPolicyId == null) ? 0 : lotteryPolicyId.hashCode());
		result = prime * result + ((clearingProfileId == null) ? 0 : clearingProfileId.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((feeProfileId == null) ? 0 : feeProfileId.hashCode());
//		result = prime * result + ((keySet == null) ? 0 : keySet.hashCode());
		result = prime * result + ((parentGroupId == null) ? 0 : parentGroupId.hashCode());
		result = prime * result + ((securityProfileId == null) ? 0 : securityProfileId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	public Transaction getLastTransaction() {
		return lastTransaction;
	}

	public void setLastTransaction(Transaction lastTransaction) {
		this.lastTransaction = lastTransaction;
	}

	public Transaction getLastIncomingTransaction() {
		return lastIncomingTransaction;
	}

	public void setLastIncomingTransaction(Transaction lastIncomingTransaction) {
		this.lastIncomingTransaction = lastIncomingTransaction;
	}
	@Override
	public List<AuditableProperty> getAuditableFields() {
		List<AuditableProperty> props = new ArrayList<AuditableProperty>();
        props.add(new SimpleProperty("status.status"));        
        props.add(new SimpleProperty("securityProfile"));
        props.add(new SimpleProperty("documentDescriptionPattern"));
        props.add(new SimpleProperty("transactionNumberPattern"));
        props.add(new SimpleProperty("authorizationProfile"));
        props.add(new SimpleProperty("feeProfile"));
        props.add(new SimpleProperty("clearingProfile"));
        props.add(new SimpleProperty("chargePolicy"));
        props.add(new SimpleProperty("lotteryPolicy"));
        props.add(new SimpleProperty("sharedFeature"));
        props.add(new SimpleProperty("parentGroup"));
        props.add(new SimpleProperty("enabled"));
		return props;
	}

}
