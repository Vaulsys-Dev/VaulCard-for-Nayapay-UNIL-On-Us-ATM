package vaulsys.entity.impl;

import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Account;
import vaulsys.terminal.impl.SwitchTerminal;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fine_institution")
@ForeignKey(name = "institution_fine_fk")
public class Institution extends FinancialEntity {

	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "inst_type"))
	private FinancialEntityRole institutionType;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cur_wrkday")
	@ForeignKey(name = "inst_curworkingday_fk")
	private ClearingDate currentWorkingDay;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_wrkday")
	@ForeignKey(name = "inst_lastworkingday_fk")
	private ClearingDate lastWorkingDay;

	@Embedded
	private Account coreAccountNumber;

	@Transient
	protected Set<SwitchTerminal> terminals;

	Long bin;
	
	@Column(length=10, name="abbr_bnk_name")
	private String abbreviatedBankName;
	
	@Column(length=6, name = "branch_code")
	private String branchCardCode;

	public String getBranchCardCode() {
		return branchCardCode;
	}

	public void setBranchCardCode(String branchCardCode) {
		this.branchCardCode = branchCardCode;
	}

	public Institution() {
	}

	@Override
	public FinancialEntityRole getRole() {
		return getInstitutionType();
	}

	public Set<SwitchTerminal> getTerminals() {
		return terminals;
	}

	public void addTerminal(SwitchTerminal terminal) {
		if (terminals == null)
			terminals = new HashSet<SwitchTerminal>();
		terminals.add(terminal);
	}

	public FinancialEntityRole getInstitutionType() {
		return institutionType;
	}

	public void setInstitutionType(FinancialEntityRole institutionType) {
		this.institutionType = institutionType;
	}

	public Long getBin() {
		return bin;
	}

	public void setBin(Long bin) {
		this.bin = bin;
	}

	public void setCurrentWorkingDay(ClearingDate currentWorkingDay) {
		this.currentWorkingDay = currentWorkingDay;
	}

	public ClearingDate getCurrentWorkingDay() {
		return currentWorkingDay;
	}

	@Override
	public String toString() {
		return code.toString();
	}

	public ClearingDate getLastWorkingDay() {
		return lastWorkingDay;
	}

	public void setLastWorkingDay(ClearingDate lastWorkingDay) {
		this.lastWorkingDay = lastWorkingDay;
	}

	public Account getCoreAccountNumber() {
		return coreAccountNumber;
	}

	public void setCoreAccountNumber(Account coreAccountNumber) {
		this.coreAccountNumber = coreAccountNumber;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof Institution)) return false;

		Institution that = (Institution) o;

		if (bin != null ? !bin.equals(that.bin) : that.bin != null) return false;
		if (coreAccountNumber != null ? !coreAccountNumber.equals(that.coreAccountNumber) : that.coreAccountNumber != null)
			return false;
//		if (currentWorkingDay != null ? !currentWorkingDay.equals(that.currentWorkingDay) : that.currentWorkingDay != null)
//			return false;
		if (institutionType != null ? !institutionType.equals(that.institutionType) : that.institutionType != null)
			return false;
		if (branchCardCode != null ? !branchCardCode.equals(that.branchCardCode) : that.branchCardCode != null)
			return false;
//		if (lastWorkingDay != null ? !lastWorkingDay.equals(that.lastWorkingDay) : that.lastWorkingDay != null)
//			return false;

		return true;
	}

	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (institutionType != null ? institutionType.hashCode() : 0);
//		result = 31 * result + (currentWorkingDay != null ? currentWorkingDay.hashCode() : 0);
//		result = 31 * result + (lastWorkingDay != null ? lastWorkingDay.hashCode() : 0);
		result = 31 * result + (coreAccountNumber != null ? coreAccountNumber.hashCode() : 0);
		result = 31 * result + (bin != null ? bin.hashCode() : 0);
		result = 31 * result + (branchCardCode != null ? branchCardCode.hashCode() : 0);
		return result;
	}

	public String getAbbreviatedBankName() {
		return abbreviatedBankName;
	}

	public void setAbbreviatedBankName(String abbreviatedBankName) {
		this.abbreviatedBankName = abbreviatedBankName;
	}
}
