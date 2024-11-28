package vaulsys.lottery;

import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity
@Table(name = "lottery_plc_rule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "plc_name")
public abstract class LotteryAssignmentPolicy implements IEntity<Integer> {
	@Transient
	private transient Logger logger = Logger.getLogger(this.getClass());

	@Id
    @GeneratedValue(generator="switch-gen")
	protected Integer id;
	
	protected String name;

	
	@OneToMany(mappedBy = "policy", fetch = FetchType.LAZY)
//	@CollectionOfElements(fetch = FetchType.EAGER)
//	@JoinTable(name = "lottery__criteria", 
//			joinColumns = {@JoinColumn(name = "lottery")}
//	)
	@OrderBy("index")
	private Set<LotteryCriteria> criterias;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Transient
	protected transient GeneralDao generalDao;
	 
	public List<Long> calculate(Ifx ifx) {
		Ifx dummy = ifx.copy();
		
		for (LotteryCriteria criteria : criterias) {
			List<Long> credit = criteria.calculate(dummy);
			if (credit != null && credit.size() > 0)
				return credit;
		}
		return null;
	}
/*	public Long calculate(Ifx ifx) {
		Ifx dummy = ifx.copy();
		double random = GlobalContext.getInstance().generateRandomDouble();
//		double random = Math.random();
		
		logger.debug("random: " +random);
		
		for (LotteryCriteria criteria : criterias) {
			Long credit = criteria.calculate(dummy, random);
			if (credit != null)
				return credit;
		}
		return null;
	}
*/	
	abstract public Lottery getLottery(Ifx ifx) throws Exception;
	
	abstract public Lottery update(Ifx ifx) throws Exception;
	
	public void setGeneralDao(GeneralDao generalDao) {
		this.generalDao = generalDao;
	}

	public Set<LotteryCriteria> getCriterias() {
		return criterias;
	}

	public void addCriterias(LotteryCriteria criteria) {
		if (criterias == null)
			criterias = new HashSet<LotteryCriteria>();
		criterias.add(criteria);
		criteria.setPolicy(this);
	}
	
//	public List<LotteryCriteria> getCriteriasWithIndex() {
//		List<LotteryCriteria> result = new ArrayList<LotteryCriteria>();
//
//		criterias.
//		
//		for (LotteryCriteria criteria: criterias) {
//			result.add
//		}
//		
//		return result;
//	}
	
//	public String getFormula() {
//		return formula;
//	}
//
//	public void setFormula(String formula) {
//		this.formula = formula;
//	}
//
//	public String getRule() {
//		return rule;
//	}
//
//	public void setRule(String rule) {
//		this.rule = rule;
//	}

	@Override
	public String toString(){
		return name != null ? name : "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LotteryAssignmentPolicy))
			return false;
		LotteryAssignmentPolicy other = (LotteryAssignmentPolicy) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
