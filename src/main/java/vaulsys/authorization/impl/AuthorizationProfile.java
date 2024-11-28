package vaulsys.authorization.impl;

import vaulsys.authorization.policy.Policy;
import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "auth_prof")
public class AuthorizationProfile implements IEntity<Long> {
	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;

	private String name;

	private boolean enabled = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "auth_plc_authprof", 
    		joinColumns = {@JoinColumn(name = "auth_prof")}, 
    		inverseJoinColumns = {@JoinColumn(name = "plc")}
    )
    @ForeignKey(name = "plc_auth_prof_fk", inverseName = "plc_fk")
	private List<Policy> policies;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "authprof_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
			})
	protected DateTime createdDateTime;

	public AuthorizationProfile() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<Policy> getPolicies() {
		return policies;
	}

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

	public void addPolicy(Policy policy) {
		if (policies == null) {
			policies = new ArrayList<Policy>(1);
		}
//        policy.addProfile(this);
		policies.add(policy);
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
		int result = id != null ? id.hashCode() : 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
//		result = prime * result + ((policies == null) ? 0 : policies.hashCode());
		return result;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof AuthorizationProfile)) return false;

		AuthorizationProfile that = (AuthorizationProfile) o;

//		if (enabled != that.enabled) return false;
//		if (createdDateTime != null ? !createdDateTime.equals(that.createdDateTime) : that.createdDateTime != null)
//			return false;
//		if (creatorUser != null ? !creatorUser.equals(that.creatorUser) : that.creatorUser != null) return false;
		if (id != null ? !id.equals(that.id) : that.id != null) return false;
//		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}
}
