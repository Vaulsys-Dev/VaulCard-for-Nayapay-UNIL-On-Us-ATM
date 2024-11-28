package vaulsys.entity.impl;

import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.terminal.impl.Terminal;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fine_branch")
@ForeignKey(name="branch_fine_fk")
public class Branch extends FinancialEntity{

	@Column(name="branchCode", unique=true)
	protected String coreBranchCode;
	
	
    @Transient
    protected Set<Terminal> terminals;

    public Branch() {
    }

    @Override
    public FinancialEntityRole getRole() {
        return FinancialEntityRole.BRANCH;
    }

    public Set<Terminal> getTerminals() {
        return terminals;
    }

    public void addTerminal(Terminal terminal) {
        if (terminals == null)
            terminals = new HashSet<Terminal>();
        terminals.add(terminal);
    }

	public String getCoreBranchCode() {
		return coreBranchCode;
	}

	public void setCoreBranchCode(String coreBranchCode) {
		this.coreBranchCode = coreBranchCode;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s",
				coreBranchCode != null ? coreBranchCode : "-",
				name != null ? name : "-");
	}
}
