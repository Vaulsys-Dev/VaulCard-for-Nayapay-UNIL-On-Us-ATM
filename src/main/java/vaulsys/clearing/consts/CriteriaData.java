package vaulsys.clearing.consts;

import vaulsys.persistence.IEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "clr_criteria_data")
public class CriteriaData implements IEntity<Long> {
   
	@Id
    @GeneratedValue(generator="switch-gen")
	private Long id;
	
	Class criteriaName;
	
    int criteriaValue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria", nullable = true, updatable = true)
    @ForeignKey(name="data_criteria_fk")
    private SettlementDataCriteria criteria;

    public CriteriaData() {
    	
    }
    
    public CriteriaData(Class criteriaName, int criteriaValue) {
    	this.criteriaName = criteriaName;
    	this.criteriaValue = criteriaValue;
    }
    
	public Class getCriteriaName() {
		return criteriaName;
	}

	public void setCriteriaName(Class criteriaName) {
		this.criteriaName = criteriaName;
	}

	public int getCriteriaValue() {
		return criteriaValue;
	}

	public void setCriteriaValue(int criteriaValue) {
		this.criteriaValue = criteriaValue;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SettlementDataCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(SettlementDataCriteria criteria) {
		this.criteria = criteria;
	}
    
    
}
