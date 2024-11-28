package vaulsys.mtn;

import vaulsys.mtn.exception.CellChargePurchaseException;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "mtn_plc_rule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rule")
public abstract class ChargeAssignmentPolicy<D extends ChargePolicyData> implements IEntity<Integer> {

    @Id
    @GeneratedValue(generator="switch-gen")
	protected Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "policydata")
	@ForeignKey(name = "charge_plc_data_fk")
	private ChargePolicyData policyData;

	
	 @Transient
	protected
    transient GeneralDao generalDao;
	 
	abstract public MTNCharge getCharge(Ifx ifx) throws CellChargePurchaseException;
	
	abstract public MTNCharge update(Ifx ifx) throws CellChargePurchaseException;
	
	

	public ChargePolicyData getPolicyData() {
		return policyData;
	}

	public void setPolicyData(ChargePolicyData policyData) {
		this.policyData = policyData;
	}


	public void setGeneralDao(GeneralDao generalDao) {
		this.generalDao = generalDao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((policyData == null) ? 0 : policyData.hashCode());
		return result;
	}

}
