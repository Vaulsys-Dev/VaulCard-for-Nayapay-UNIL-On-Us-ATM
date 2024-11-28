package vaulsys.discount;

import vaulsys.authorization.policy.AllowedCard;
import vaulsys.persistence.IEntity;

import java.util.ArrayList;
import java.util.List;

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
@Table(name = "dis_base")
public class BaseDiscount implements IEntity<Long> {

	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "dis_base_card", 
    		joinColumns = {@JoinColumn(name = "dis_base")}, 
    		inverseJoinColumns = {@JoinColumn(name = "card")}
    )
	@ForeignKey(name = "dis_base_card_fk")
	private List<AllowedCard> cards;
	
	private Boolean isAllowedCard = true;
	
	private Double discount;
	
	private String description;

	@Column(name = "enabled")
	private boolean enabled;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	private DiscountProfile owner;
	
	@Column(name = "owner", insertable = false, updatable = false)
	private Long ownerId;

	public BaseDiscount() {
		this.description = "";
		this.enabled = true;
	}

	public BaseDiscount(String description) {
		this.description = description;
		this.enabled = true;
	}

	public BaseDiscount(boolean isEnabled) {
		this.description = "";
		this.enabled = isEnabled;
	}

	public BaseDiscount(String description, boolean isEnabled) {
		this.description = description;
		this.enabled = isEnabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.enabled = isEnabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DiscountProfile getOwner() {
		return owner;
	}

	public void setOwner(DiscountProfile owner) {
		this.owner = owner;
	}
	
	public Long getOwnerId() {
		return ownerId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		return result;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BaseDiscount)) return false;

		BaseDiscount baseDiscount = (BaseDiscount) o;

		if (id != null ? !id.equals(baseDiscount.id) : baseDiscount.id != null) return false;
		if (ownerId != null ? !ownerId.equals(baseDiscount.ownerId) : baseDiscount.ownerId != null) return false;
		if (enabled != baseDiscount.enabled) return false;
		if (description != null ? !description.equals(baseDiscount.description) : baseDiscount.description != null) return false;

		return true;
	}

	public List<AllowedCard> getCards() {
		return cards;
	}

	public void setCards(List<AllowedCard> cards) {
		this.cards = cards;
	}
	
	public void addCard(AllowedCard card) {
		if (cards == null) {
			cards = new ArrayList<AllowedCard>(1);
		}
		cards.add(card);
	}
	
	public Double getDiscount() {
		return discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsAllowedCard() {
		return isAllowedCard;
	}

	public void setIsAllowedCard(Boolean isAllowedCard) {
		this.isAllowedCard = isAllowedCard;
	}

}
