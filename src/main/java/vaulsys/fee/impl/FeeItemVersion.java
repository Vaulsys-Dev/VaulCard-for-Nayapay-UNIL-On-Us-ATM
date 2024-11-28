package vaulsys.fee.impl;

import javax.persistence.*;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fee_item_ver")
public class FeeItemVersion implements IEntity<Long> {
	
	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "feeProfile_vers_parent_fk")
	private FeeItem parent;
	
	private String formula;
	
	@ManyToOne
	@JoinColumn(name = "base_fee")
	private BaseFee baseFee;
	
	@ManyToOne
	@JoinColumn(name = "acc_credited")
	@ForeignKey(name = "feeitem_acccredited_fk")
	private AbstractEntityAccount accountToBeCredited;

	@ManyToOne
	@JoinColumn(name = "acc_debited")
	@ForeignKey(name = "feeitem_accdebited_fk")
	private AbstractEntityAccount accountToBeDebited;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "fine_user_fk")
    protected User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    protected DateTime createdDateTime;

	private boolean enabled = true;

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public BaseFee getBaseFee() {
		return baseFee;
	}

	public void setBaseFee(BaseFee baseFee) {
		this.baseFee = baseFee;
	}

	public AbstractEntityAccount getAccountToBeCredited() {
		return accountToBeCredited;
	}

	public void setAccountToBeCredited(AbstractEntityAccount accountToBeCredited) {
		this.accountToBeCredited = accountToBeCredited;
	}

	public AbstractEntityAccount getAccountToBeDebited() {
		return accountToBeDebited;
	}

	public void setAccountToBeDebited(AbstractEntityAccount accountToBeDebited) {
		this.accountToBeDebited = accountToBeDebited;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FeeItem getParent() {
		return parent;
	}

	public void setParent(FeeItem parent) {
		this.parent = parent;
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
