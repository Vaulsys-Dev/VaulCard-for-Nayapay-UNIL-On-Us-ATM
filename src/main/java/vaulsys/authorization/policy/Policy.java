package vaulsys.authorization.policy;

import vaulsys.authorization.data.PolicyData;
import vaulsys.authorization.data.TerminalPolicyData;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "auth_plc_rule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rule")
public abstract class Policy implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="switch-gen")
    protected Long id;
    
    protected String name;

//    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "policydata")
//	@ForeignKey(name="policy_data_fk")
//	protected PolicyData policyData;

//    @Transient
//    protected PolicyData policyData;
    
    private Boolean notAuthorizer = false;
    
    @Transient
    protected List<AuthorizationProfile> profiles;

    protected Policy() {
    }

    public abstract Policy clone();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Policy newInstance() {
        return clone();
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void authorize(Ifx ifx, Terminal terminal) throws AuthorizationException{
    	if (getNotAuthorizer())
    		authorizeNotCondition(ifx, terminal);
    	else
    		authorizeNormalCondition(ifx, terminal);
    }
    
    abstract protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws AuthorizationException;
    abstract protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException;

    abstract public void update(Ifx ifx, Terminal terminal);

//    abstract public PolicyData getPolicyData();
    
//	public PolicyData getPolicyData() {
//		return policyData;
//	}

//	public TerminalPolicyData getPolicyData() {
//		if (policyData == null) {
//			policyData = new TerminalPolicyData();
////			policyData.setPolicy(this);
//			setPolicyData(policyData);
////			GeneralDao.Instance.saveOrUpdate(policyData);
//		}
//		return (TerminalPolicyData) policyData;
//	}
//	
//	public void setPolicyData(PolicyData policyData) {
//		this.policyData = policyData;
//		policyData.setPolicy(this);
//	}

	public List<AuthorizationProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<AuthorizationProfile> profiles) {
		this.profiles = profiles;
	}
	
	public void addProfile(AuthorizationProfile profile) {
		if (profiles == null)
			profiles = new ArrayList<AuthorizationProfile>();
		profiles.add(profile);
		profile.addPolicy(this);
	}

	abstract public boolean isSynchronized();

	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Policy))
			return false;
		Policy other = (Policy) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Boolean getNotAuthorizer() {
		return notAuthorizer;
	}

	public void setNotAuthorizer(Boolean notAuthorizer) {
		this.notAuthorizer = notAuthorizer;
	}

	@Override
	public String toString() {
		return name != null ? name : "";
	}
}
