package vaulsys.entity.impl;

import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Account;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "fine_institution_ver")
public class InstitutionVersion extends FinancialEntityVersion {

	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "inst_vers_parent_fk")
	private Institution parent;

	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "inst_type"))
	private FinancialEntityRole institutionType;

	private Long bin;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "accountNumber", column = @Column(name = "cor_account_num")),
		@AttributeOverride(name = "accountHolderName", column = @Column(name = "cor_account_holder")),
		@AttributeOverride(name = "cardNumber", column = @Column(name = "cor_card_num")),		
		@AttributeOverride(name = "core.type", column = @Column(name = "cor_core")),
		@AttributeOverride(name = "type.type", column = @Column(name = "cor_account_type"))
	})
	@AssociationOverride(name = "currency", joinColumns = @JoinColumn(name = "cor_account_currency"))
	Account coreAccountNumber;
	
	@Column(length=6, name = "branch_code")
	private String branchCardCode;

	public InstitutionVersion() {
	}

	public Institution getParent() {
		return parent;
	}

	public void setParent(Institution parent) {
		this.parent = parent;
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

	public Account getCoreAccountNumber() {
		return coreAccountNumber;
	}

	public void setCoreAccountNumber(Account coreAccountNumber) {
		this.coreAccountNumber = coreAccountNumber;
	}

	public String getBranchCardCode() {
		return branchCardCode;
	}

	public void setBranchCardCode(String branchCardCode) {
		this.branchCardCode = branchCardCode;
	}
}
