package vaulsys.lottery;

import groovy.lang.Binding;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "lottery_criteria",
		uniqueConstraints={@UniqueConstraint(columnNames={"inx", "policy"})})
public class LotteryCriteria implements IEntity<Long> {

	@Transient
	private transient Logger logger = Logger.getLogger(this.getClass());
	
	@Id
    @GeneratedValue(generator="switch-gen")
	private Long id;
	
	@Column(length = 1000)
	private String rule;
	
	private String formula;
	
	@Column(name = "prop")
	private String propability;
	
	@Column(name="inx", nullable=false)
	private int index;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy", nullable = true, updatable = true)
    @ForeignKey(name="lot_criteris_plc_fk")
    @Index(name="idx_lotcrt_plc_trx")
    private LotteryAssignmentPolicy policy;
	
	public LotteryCriteria() {
	}

	public LotteryCriteria(String rule, String formula) {
		this.rule = rule;
		this.formula = formula;
	}
	
	public LotteryCriteria(String rule, String formula, String propability, int index) {
		this.rule = rule;
		this.formula = formula;
		this.propability = propability;
		this.index = index;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}
	
	public List<Long> calculate(Ifx ifx/*, double random*/) {
		Binding scriptBinding = new Binding();
		scriptBinding.setProperty("ifx", ifx);
		
		if ((Boolean) GlobalContext.getInstance().evaluateScript(rule, scriptBinding)) {
			if (checkPropability(propability))
				return calculateFormula(ifx);
		}
		return null;
	}
	
	private boolean checkPropability(String propability) {
		if (!Util.hasText(propability))
			return true;
		double random = GlobalContext.getInstance().generateRandomDouble();
		logger.debug("random: " + random);
		
		Binding scriptBinding = new Binding();
		scriptBinding.setProperty("rnd", random);
		
		return ((Boolean) GlobalContext.getInstance().evaluateScript(propability, scriptBinding));
	}

	public List<Long> calculateFormula(Ifx ifx) {
//		Script script = GlobalContext.getInstance().getGroovyScript(String.format("vaulsys.lottery.LotteryCriteria.getCredit(\"%s\")", formula));
		
		Binding scriptBinding = new Binding();
		scriptBinding.setProperty("ifx", ifx);
		return (List<Long>) GlobalContext.getInstance().evaluateScript(String.format("vaulsys.lottery.LotteryCriteria.getCredit(\"%s\")", formula), scriptBinding);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public LotteryAssignmentPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(LotteryAssignmentPolicy policy) {
		this.policy = policy;
	}
	
	@Override
	public String toString(){
		return id != null ? id.toString() : "";
	}

	public static List<Long> getCredit(String amounts){
		String[] splits = amounts.split(",");
		int size = splits.length;
		List<Long> creditList = new ArrayList<Long>();
		for (int i=0; i<size; i++)
			creditList.add(new Long(splits[i].trim()));
		return creditList;
	}

	public String getPropability() {
		return propability;
	}

	public void setPropability(String propability) {
		this.propability = propability;
	}
}
