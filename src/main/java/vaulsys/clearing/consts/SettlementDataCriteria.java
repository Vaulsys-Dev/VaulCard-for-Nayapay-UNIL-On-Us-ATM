package vaulsys.clearing.consts;

import vaulsys.persistence.IEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "clr_criteria")
public class SettlementDataCriteria implements IEntity<Long> {
   
	@Id
    @GeneratedValue(generator="switch-gen")
	private Long id;
    
    @Embedded
	@AttributeOverride(name = "type", column = @Column(name = "type"))
	private SettlementDataType type;
    
    private String docDesc;
	
   
    @OneToMany(mappedBy = "criteria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<CriteriaData> criteriaDatas;
	
    public SettlementDataCriteria() {
    }
    
    public SettlementDataCriteria(SettlementDataType type) {
    	this.type = type;
    }
    
    public SettlementDataCriteria(SettlementDataType type, Class criteriaName, int criteriaValue) {
    	this.type = type;
    	if (criteriaDatas == null)
    		criteriaDatas = new HashSet<CriteriaData>();
    	CriteriaData criteriaData = new CriteriaData(criteriaName, criteriaValue);
		criteriaDatas.add(criteriaData);
		criteriaData.setCriteria(this);
    }
    
	public SettlementDataType getType() {
		return type;
	}
	public void setType(SettlementDataType type) {
		this.type = type;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getDocDesc() {
		return docDesc;
	}

	public void setDocDesc(String docDesc) {
		this.docDesc = docDesc;
	}

	public Set<CriteriaData> getCriteriaDatas() {
		return criteriaDatas;
	}

	public void addCriteriaDatas(CriteriaData criteriaData) {
		if (criteriaDatas == null)
    		criteriaDatas = new HashSet<CriteriaData>();
		this.criteriaDatas.add(criteriaData);
		criteriaData.setCriteria(this);
	}
	
	public void addCriteriaDatas(Class criteriaName, int criteriaValue) {
		if (criteriaDatas == null)
			criteriaDatas = new HashSet<CriteriaData>();
		CriteriaData criteriaData = new CriteriaData(criteriaName, criteriaValue);
		this.criteriaDatas.add(criteriaData);
		criteriaData.setCriteria(this);
	}
	
}
