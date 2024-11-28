package vaulsys.fee.impl;

import groovy.lang.Binding;
import vaulsys.authorization.policy.AllowedCard;
import vaulsys.calendar.DateTime;
import vaulsys.customer.CardService;
import vaulsys.fee.base.FeeInfo;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.user.User;
import vaulsys.wfe.GlobalContext;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "fee_base")
public abstract class BaseFee implements IEntity<Long> {

	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;

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

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "baseFee")
	@ForeignKey(name = "fee_base_feeitem_fk")
	protected Set<FeeItem> feeItemList;

	protected String description;

	@Column(name = "enabled")
	protected boolean enabled;

//	@Transient
//	protected transient MyParser parser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	protected FeeProfile owner;
	
	@Column(name = "owner", insertable = false, updatable = false)
	protected Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "fine_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
			@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
			@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
	})
	protected DateTime createdDateTime;

	public BaseFee() {
		this.rule = "";
		this.feeItemList = new HashSet<FeeItem>();
		this.description = "";
		this.enabled = true;
	}

	public BaseFee(String rule, Set<FeeItem> feeItemList) {
		this.rule = rule;
		this.feeItemList = feeItemList;
		this.description = "";
		this.enabled = true;
	}

	public BaseFee(String rule, Set<FeeItem> feeItemList, String description) {
		this.rule = rule;
		this.feeItemList = feeItemList;
		this.description = description;
		this.enabled = true;
	}

	public BaseFee(String rule, Set<FeeItem> feeItemList, boolean isEnabled) {
		this.rule = rule;
		this.feeItemList = feeItemList;
		this.description = "";
		this.enabled = isEnabled;
	}

	public BaseFee(String rule, Set<FeeItem> feeItemList, String description,
						boolean isEnabled) {
		this.rule = rule;
		this.feeItemList = feeItemList;
		this.description = description;
		this.enabled = isEnabled;
	}

	public List<FeeInfo> feeCalculator(Object data) {
		if (!enabled)
			return null;

		if (data instanceof Ifx)
			return calculate((Ifx) data);
		
//		String ruletmp = rule.trim();
//		ruletmp = putField(data, ruletmp);
//		parser.consume(new MyString(ruletmp));
//		setVariableValues(data);
//		if (parser.calc()) {
//			List<FeeInfo> feeList = new ArrayList<FeeInfo>();
//			for (FeeItem feeItem : feeItemList) {
//				if (!feeItem.isEnabled())
//					continue;
//				long feeAmount = (long) Math.ceil(feeItem.calculate(null)); //TODO
//				FeeInfo fee = new FeeInfo(feeItem.getAccountToBeCredited().getFinancialEntity(null), feeItem.getAccountToBeDebited().getFinancialEntity(null), feeAmount, description);
//				feeList.add(fee);
//			}
//			return feeList;
//		}
		return null;
	}

	public List<FeeInfo> calculate(Ifx ifx) {
		if (!enabled)
			return null;

		boolean refund = false;
		Ifx dummy = ifx.copy();
		
        if (getRefundable() && TrnType.RETURN.equals(dummy.getTrnType())) {
			refund = true;
			dummy.setTrnType(TrnType.PURCHASE);
			if (IfxType.RETURN_RQ.equals(dummy.getIfxType()))
				dummy.setIfxType(IfxType.PURCHASE_RQ);
			else if (IfxType.RETURN_RS.equals(dummy.getIfxType()))
				dummy.setIfxType(IfxType.PURCHASE_RS);
			else if (IfxType.RETURN_REV_REPEAT_RQ.equals(dummy.getIfxType()))
				dummy.setIfxType(IfxType.PURCHASE_REV_REPEAT_RQ);
			else if (IfxType.RETURN_REV_REPEAT_RS.equals(dummy.getIfxType()))
				dummy.setIfxType(IfxType.PURCHASE_REV_REPEAT_RS);
//			else if (IfxType.RETURN_REV_RQ.equals(dummy.getIfxType()))
//				dummy.setIfxType(IfxType.PURCHASE_REV_RQ);
//			else if (IfxType.RETURN_REV_RS.equals(dummy.getIfxType()))
//				dummy.setIfxType(IfxType.PURCHASE_REV_RS);
		}
		
//		GroovyShell shell = new GroovyShell();
//		Script script = shell.parse(rule);
		Binding scriptBinding = new Binding();
		scriptBinding.setProperty("ifx", dummy);

//		Script script = GlobalContext.getInstance().getGroovyScript(rule);
		
//		rule = rule.replaceAll("Auth_Amt", "Real_Amt");
		
		boolean isAllowdCard = true;
		if (Boolean.FALSE.equals(getIsAllowedCard()))
			isAllowdCard = false;
		
		if ((Boolean) GlobalContext.getInstance().evaluateScript(rule, scriptBinding) && !(CardService.isAllowedCard(ifx, cards) ^ isAllowdCard)) {
			List<FeeInfo> feeList = new ArrayList<FeeInfo>();
			for (FeeItem feeItem : feeItemList) {
				if (!feeItem.isEnabled())
					continue;
				long feeAmount = (long) Math.ceil(feeItem.calculate(dummy));
				
//				if(feeCoefficient != null){
//					feeAmount = (long) Math.ceil(feeAmount * feeCoefficient);
//				}
				
				if (refund)
					feeAmount *=-1;
				FeeInfo fee = new FeeInfo(feeItem.getAccountToBeCredited().getFinancialEntity(dummy), feeItem.getAccountToBeDebited().getFinancialEntity(dummy), feeAmount, feeItem, description);
				feeList.add(fee);
			}
			
			return feeList;
		}
		return null;

//		String ruletmp = rule.trim();
//		ruletmp = putField(ifx, ruletmp);
//		parser.consume(new MyString(ruletmp));
//		setVariableValues(ifx);
//		if (parser.calc()) {
//			List<FeeInfo> feeList = new ArrayList<FeeInfo>();
//			for (FeeItem feeItem : feeItemList) {
//				if (!feeItem.isEnabled())
//					continue;
//				long feeAmount = (long) Math.ceil(feeItem.calculate());
//				FeeInfo fee = new FeeInfo(feeItem.getAccountToBeCredited().getFinancialEntity(ifx), feeItem.getAccountToBeDebited().getFinancialEntity(ifx), feeAmount, feeItem, description);
//				feeList.add(fee);
//			}
//			return feeList;
//		}
//		return null;
	}

//	private void setVariableValues(Object data) {
//		for (FeeItem f : feeItemList) {
//			List<Variable> vecv = f.getVariables();
//			for (Variable v : vecv) {
//				String name = v.getName();
//				if (name.equals("DUMMY"))
//					continue;
//				name = putField(data, name);
//				v.setValue(Double.parseDouble(name));
//			}
//		}
//	}
//
//	private void setVariableValues(Ifx ifx) {
//		for (FeeItem f : feeItemList) {
//			List<Variable> vecv = f.getVariables();
////            List<Variable> vecv = f.getVariables();
//			//logger.error("Listing Variables:" + vecv.size());
//			for (Variable v : vecv) {
//				String name = v.getName();
//				if (name.equals("DUMMY"))
//					continue;
//				name = putField(ifx, name);
//				v.setValue(Double.parseDouble(name));
//			}
//		}
//	}


    public Boolean getAllowedCard() {
        return isAllowedCard;
    }

    public void setAllowedCard(Boolean allowedCard) {
        isAllowedCard = allowedCard;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public Set<FeeItem> getFeeItemList() {
		return feeItemList;
	}

	public void setFeeItemList(Set<FeeItem> feeItemList) {
		this.feeItemList = feeItemList;
	}

	public boolean addFeeItemList(FeeItem feeItem) {
		if (feeItemList == null)
			feeItemList = new HashSet<FeeItem>();
		feeItem.setBaseFee(this);
		return feeItemList.add(feeItem);
	}

//    public FeeItem getFeeItemList(int index) {
//        return feeItemList.get(index);
//    }

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.enabled = isEnabled;
	}

//	protected abstract String putField(Object data, String rule);
//

//	protected abstract String putField(Ifx ifx, String rule);
//
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FeeProfile getOwner() {
		return owner;
	}

	public void setOwner(FeeProfile owner) {
		this.owner = owner;
	}
	
	public Long getOwnerId() {
		return ownerId;
	}

	@Override
	public String toString() {
        return id.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
        //result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((refundable == null) ? 0 : refundable.hashCode());
        result = prime * result + ((isAllowedCard == null) ? 0 : isAllowedCard.hashCode());
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((cards == null) ? 0 : cards.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		return result;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BaseFee)) return false;

		BaseFee baseFee = (BaseFee) o;

		if (id != null ? !id.equals(baseFee.id) : baseFee.id != null) return false;
		if (rule != null ? !rule.equals(baseFee.rule) : baseFee.rule != null) return false;
		if (ownerId != null ? !ownerId.equals(baseFee.ownerId) : baseFee.ownerId != null) return false;
		if (enabled != baseFee.enabled) return false;
		if (description != null ? !description.equals(baseFee.description) : baseFee.description != null) return false;

		return true;
	}

	public void setRefundable(Boolean refundable) {
		this.refundable = refundable;
	}

	public Boolean getRefundable() {
		return refundable;
	}

	public List<AllowedCard> getCards() {
		return cards;
	}

	public void addCard(AllowedCard card) {
		if (cards == null) {
			cards = new ArrayList<AllowedCard>(1);
		}
		cards.add(card);
	}

	public void setCards(List<AllowedCard> cards) {
		this.cards = cards;
	}

	public Boolean getIsAllowedCard() {
		return isAllowedCard;
	}

	public void setIsAllowedCard(Boolean isAllowedCard) {
		this.isAllowedCard = isAllowedCard;
	}

	
}
