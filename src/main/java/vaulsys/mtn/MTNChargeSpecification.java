package vaulsys.mtn;

import vaulsys.entity.impl.Organization;
import vaulsys.persistence.IEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "mtn_charge_spec")
public class MTNChargeSpecification implements IEntity<Long> {

	@Id
    @GeneratedValue(generator="switch-gen")
	private Long id;
	
	private Long credit;
	
	private Long tax;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company")
	@Cascade(value = CascadeType.ALL )
	@ForeignKey(name="mtncharge_spec_company_fk")
	private Organization company;
 
	public MTNChargeSpecification(){
		
	}
	
	public MTNChargeSpecification(Long credit, Long tax, Organization company){
		this.credit = credit;
		this.tax = tax;
		this.company = company;
	}
	 	 
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getCredit() {
		return credit;
	}

	public void setCredit(Long credit) {
		this.credit = credit;
	}

	public Long getTax() {
		return tax;
	}

	public void setTax(Long tax) {
		this.tax = tax;
	}

	public Organization getCompany() {
		return company;
	}

	public void setCompany(Organization entity) {
		this.company = entity;
	}
	
}
