package vaulsys.entity;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fine_merchant_category")
public class MerchantCategory implements IEntity<Long> {
	@Id
	private Long code;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_cat")
	@ForeignKey(name = "merchcat_parentcat_fk")
	private MerchantCategory parentCategory;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "merch_cat_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
	@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time")) })
	protected DateTime createdDateTime;

	@Column(name = "en_name", length = 150)
	private String englishName;

	private Boolean isVisible;

	private Boolean isAssignable;

	public MerchantCategory() {
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MerchantCategory getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(MerchantCategory parentCategory) {
		this.parentCategory = parentCategory;
	}

	public void addChild(MerchantCategory merchantCategory) {
		merchantCategory.setParentCategory(this);
	}

	public String toString() {
		return String.format("%s (%s)", name == null ? englishName : name, code);
	}

	public String getEnglishName() {
		return englishName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MerchantCategory))
			return false;
		MerchantCategory other = (MerchantCategory) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

/*
	public Boolean getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}

	public Boolean getIsAssignable() {
		return isAssignable;
	}

	public void setIsAssignable(Boolean isAssignable) {
		this.isAssignable = isAssignable;
	}
*/

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

	public Boolean getVisible() {
		return isVisible;
	}

	public void setVisible(Boolean visible) {
		isVisible = visible;
	}

	public Boolean getAssignable() {
		return isAssignable;
	}

	public void setAssignable(Boolean assignable) {
		isAssignable = assignable;
	}
}
