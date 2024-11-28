package vaulsys.fee.impl;

import vaulsys.customer.Account;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "fine_account")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class AbstractEntityAccount implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    protected AbstractEntityAccount() {
    }

    public abstract Account getAccount(Ifx ifx);

    public abstract FinancialEntity getFinancialEntity(Ifx ifx);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractEntityAccount)) return false;

		AbstractEntityAccount that = (AbstractEntityAccount) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;

		return true;
	}

	public int hashCode() {
		return (id != null ? id.hashCode() : 0);
	}
}
