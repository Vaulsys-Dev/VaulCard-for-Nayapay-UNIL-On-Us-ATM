package vaulsys.entity.impl;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.entity.Contract;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "fine_organization_ver")
public class OrganizationVersion extends FinancialEntityVersion {

	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "org_vers_parent_fk")
	private Organization parent;

	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "startDate.date", column = @Column(name = "contract_start_date")),
			@AttributeOverride(name = "endDate.date", column = @Column(name = "contract_end_date")),
			@AttributeOverride(name = "contractNumber", column = @Column(name = "contract_num"))})
	private Contract contract;

	private OrganizationType type;

	private Integer companyCode;

	public Organization getParent() {
		return parent;
	}

	public void setParent(Organization parent) {
		this.parent = parent;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public OrganizationType getType() {
		return type;
	}

	public void setType(OrganizationType type) {
		this.type = type;
	}

	public Integer getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(Integer companyCode) {
		this.companyCode = companyCode;
	}
}
