package vaulsys.entity.impl;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.Contract;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@SuppressWarnings("serial")
@Entity
@Table(name = "fine_organization")
@ForeignKey(name="organization_fine_fk")
public class Organization extends FinancialEntity{

	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "startDate.date", column = @Column(name = "contract_start_date")),
			@AttributeOverride(name = "endDate.date", column = @Column(name = "contract_end_date")),
			@AttributeOverride(name = "contractNumber", column = @Column(name = "contract_num"))})
    private Contract contract;
    
    @Embedded
    @AttributeOverrides({
      @AttributeOverride(name = "type", column = @Column(name = "type"))})
    private OrganizationType type;

    private Integer companyCode;
    
    private String validation;
    
    private String report;
    
    public String getValidation() {
		return validation;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "owner")
    @Transient
	protected ThirdPartyVirtualTerminal terminal;
    
	public Organization() {
    }

	@Override
    public FinancialEntityRole getRole() {
        return FinancialEntityRole.ORGANIZATION;
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

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, companyCode);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((companyCode == null) ? 0 : companyCode.hashCode());
		result = prime * result + ((contract == null) ? 0 : contract.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof Organization)) return false;

		Organization that = (Organization) o;

		if (code != null ? !code.equals(that.code) : that.code != null) return false;

		return true;
	}

	public ThirdPartyVirtualTerminal getTerminal() {
		return terminal;
	}

	public void setTerminal(ThirdPartyVirtualTerminal terminal) {
		this.terminal = terminal;
	}
}
