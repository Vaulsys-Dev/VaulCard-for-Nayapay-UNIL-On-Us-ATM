package vaulsys.discount;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "dis_prof")
public class DiscountProfile implements IEntity<Long> {
	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
	@ForeignKey(name = "disprof_basedis_fk")
	List<BaseDiscount> baseDiscounts;

	@Column(unique = true)
	String name;
	
	String description;
	
	boolean enabled = true;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "disprof_user_fk")
	protected User creatorUser;
	
	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
			})
	protected DateTime createdDateTime;

	public DiscountProfile(String name) {
		this.baseDiscounts = new ArrayList<BaseDiscount>();
		this.name = name;
		description = "";
		this.enabled = true;
	}

	public DiscountProfile() {
		this.baseDiscounts = new ArrayList<BaseDiscount>();
		this.enabled = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return name != null ? name : "id=" + id;
	}

	public List<BaseDiscount> getBaseDiscounts() {
		return baseDiscounts;
	}

	public void addBaseDiscount(BaseDiscount baseDiscount) {
		if (baseDiscounts == null)
			baseDiscounts = new ArrayList<BaseDiscount>();
		baseDiscount.setOwner(this);
		baseDiscounts.add(baseDiscount);
	}

	public BaseDiscount getBaseDiscount(int index) {
		return baseDiscounts.get(index);
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
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DiscountProfile))
			return false;
		DiscountProfile other = (DiscountProfile) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
