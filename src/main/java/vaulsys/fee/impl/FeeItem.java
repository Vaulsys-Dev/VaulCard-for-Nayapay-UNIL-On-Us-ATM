package vaulsys.fee.impl;

import groovy.lang.Binding;
import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.user.User;
import vaulsys.wfe.GlobalContext;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "fee_item")
public class FeeItem implements IEntity<Long> {

	@Id
	@GeneratedValue(generator = "switch-gen")
	private Long id;

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

	private Boolean enabled = true;

	private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user")
    @ForeignKey(name = "fine_user_fk")
    protected User creatorUser;

    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
    })
    protected DateTime createdDateTime;

	public FeeItem() {
	}

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

	public double calculate(Ifx ifx) {
//		GroovyShell shell = new GroovyShell();
//		Script script = shell.parse(formula);
//		script.setProperty("ifx", ifx);
		Binding scriptBinding = new Binding();
		scriptBinding.setProperty("ifx", ifx);

//		formula = formula.replaceAll("Auth_Amt", "Real_Amt");
		
//		Script script = GlobalContext.getInstance().getGroovyScript(formula);
		Number no = (Number) GlobalContext.getInstance().evaluateScript(formula, scriptBinding);
		return no.doubleValue();
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

//	public List<Variable> getVariables() {
//		return variables;
//	}
//
//	public void addVariable(Variable variable) {
//		if (variables == null)
//			variables = new ArrayList<Variable>();
//		variable.setFeeItem(this);
//		variables.add(variable);
//	}

    /*public boolean isEnabled() {
		return enabled;
     }*/


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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BaseFee getBaseFee() {
		return baseFee;
	}

	public void setBaseFee(BaseFee baseFee) {
		this.baseFee = baseFee;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountToBeCredited == null) ? 0 : accountToBeCredited.hashCode());
		result = prime * result + ((accountToBeDebited == null) ? 0 : accountToBeDebited.hashCode());
        //result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
//		result = prime * result + ((variables == null) ? 0 : variables.hashCode());
		return result;
	}

    @Override
    public String toString() {
        return this.id.toString();
}
}
