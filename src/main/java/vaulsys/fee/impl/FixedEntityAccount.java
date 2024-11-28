package vaulsys.fee.impl;

import vaulsys.customer.Account;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.protocols.ifx.imp.Ifx;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "Fixed")
public class FixedEntityAccount extends AbstractEntityAccount {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fin_entity")
	@ForeignKey(name="fixacc_fine_fk")
    FinancialEntity financialEntity;

    public FixedEntityAccount() {
    }

    public FinancialEntity getFinancialEntity() {
        return financialEntity;
    }

    public void setFinancialEntity(FinancialEntity financialEntity) {
        this.financialEntity = financialEntity;
    }

    public Account getAccount(final Ifx ifx) {
        return financialEntity.getOwnOrParentAccount();
    }

    public FinancialEntity getFinancialEntity(Ifx ifx) {
        return financialEntity;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((financialEntity == null) ? 0 : financialEntity.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return financialEntity!=null ? financialEntity.toString():"";
	}
}
