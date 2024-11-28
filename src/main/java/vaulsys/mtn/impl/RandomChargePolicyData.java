package vaulsys.mtn.impl;

import vaulsys.entity.impl.Organization;
import vaulsys.mtn.ChargePolicyData;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "random")
public class RandomChargePolicyData extends ChargePolicyData {

	
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "mtn_random_data_portion", 
    		joinColumns = {@JoinColumn(name = "random_charge_plc_data")},
    		inverseJoinColumns = {@JoinColumn(name = "organization")}
    		)
    @Cache(usage = CacheConcurrencyStrategy.NONE)
    @ForeignKey(name = "chrgplcdt_plcdt_fk", inverseName = "chrgplcdt_org_fk")
	private Map<Integer, Organization> companyPortions;

	private Integer portions =0;
	
	public RandomChargePolicyData() {
	}
	

	public RandomChargePolicyData(Map<Organization, Integer> portions){
		setCompanyPortions(portions);
	}


	public void setCompanyPortions(Map<Organization, Integer> companyPortions) {
//		this.companyPortions = companyPortions;
		if (this.companyPortions == null)
			this.companyPortions = new HashMap<Integer, Organization>();
		portions = 0;
		for (Organization p: companyPortions.keySet()){
			portions += companyPortions.get(p);
			this.companyPortions.put(portions, p );
		}
	}


	public Map<Integer, Organization> getCompanyPortions() {
		return companyPortions;
	}

	
	public void addCompanyPortion(Organization company, Integer portion){
		if (companyPortions ==null)
			companyPortions = new HashMap<Integer, Organization>();
		portions += portion;
		companyPortions.put(portions, company);
	}


	public Integer getPortions() {
		return portions;
	}


	public void setPortions(Integer portions) {
		this.portions = portions;
	}
	
	
	
}
