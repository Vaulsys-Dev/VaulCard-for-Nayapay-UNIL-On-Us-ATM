package vaulsys.fee.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vaulsys.authorization.policy.AllowedCard;
import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.terminal.impl.POSTerminal;

import javax.persistence.*;

import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fee_base_ver")
public class BaseFeeVersion implements IEntity<Long> {
	
	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;
	
    /*@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "baseFee_vers_parent_fk")
     private BaseFee parent;*/
	
	protected String rule;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "fee_base_card", 
    		joinColumns = {@JoinColumn(name = "fee_base")}, 
    		inverseJoinColumns = {@JoinColumn(name = "card")}
    )
	@ForeignKey(name = "fee_base_card_fk")
	private List<AllowedCard> cards;
	
	private Boolean isAllowedCard = true;
	
	private Boolean refundable = true;
	
//	protected String description;

	@Column(name = "enabled")
	protected boolean enabled;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	protected FeeProfile owner;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "baseFee")
	@ForeignKey(name = "fee_base_feeitem_fk")
	protected Set<FeeItem> feeItemList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "fine_user_fk")
    protected User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    protected DateTime createdDateTime;
	
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}
	
	public List<AllowedCard> getCards() {
		return cards;
	}

//	public void addCard(AllowedCard card) {
//		if (cards == null) {
//			cards = new ArrayList<AllowedCard>(1);
//		}
//		cards.add(card);
//	}

	public void setCards(List<AllowedCard> cards) {
		this.cards = cards;
	}
	
	public Boolean getIsAllowedCard() {
		return isAllowedCard;
	}

	public void setIsAllowedCard(Boolean isAllowedCard) {
		this.isAllowedCard = isAllowedCard;
	}
	
	public void setRefundable(Boolean refundable) {
		this.refundable = refundable;
	}

	public Boolean getRefundable() {
		return refundable;
	}
	
	public Set<FeeItem> getFeeItemList() {
		return feeItemList;
	}

	public void setFeeItemList(Set<FeeItem> feeItemList) {
		this.feeItemList = feeItemList;
	}

//	public boolean addFeeItemList(FeeItem feeItem) {
//		if (feeItemList == null)
//			feeItemList = new HashSet<FeeItem>();
//		feeItem.setBaseFee(this);
//		return feeItemList.add(feeItem);
//	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.enabled = isEnabled;
	}
	
	public FeeProfile getOwner() {
		return owner;
	}

	public void setOwner(FeeProfile owner) {
		this.owner = owner;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
    public Boolean getAllowedCard() {
        return isAllowedCard;
}

    public void setAllowedCard(Boolean allowedCard) {
        isAllowedCard = allowedCard;
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
}
