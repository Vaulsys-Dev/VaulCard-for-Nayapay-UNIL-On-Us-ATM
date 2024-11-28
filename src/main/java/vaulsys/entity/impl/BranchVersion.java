package vaulsys.entity.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fine_branch_ver")
@DiscriminatorValue(value = "Branch")
public class BranchVersion extends FinancialEntityVersion
{
	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name="branch_vers_parent_fk")
	private Branch parent;

    public Branch getParent() {
        return parent;
    }

    public void setParent(Branch parent) {
        this.parent = parent;
    }
}
