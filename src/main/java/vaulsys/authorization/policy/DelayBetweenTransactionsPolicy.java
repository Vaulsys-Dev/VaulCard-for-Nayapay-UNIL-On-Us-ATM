package vaulsys.authorization.policy;

import vaulsys.authorization.data.TerminalData;
import vaulsys.authorization.data.TerminalPolicyData;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.DelayBetweenTransactionsException;
import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "DelayBetweenTransactions")
public class DelayBetweenTransactionsPolicy extends Policy/*<TerminalPolicyData>*/ {

	@Column(name = "required_delay")
	private long requiredDelay;

//	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	@JoinColumn(name = "delay_policy_id")
//	private DelayBetweenTransactionsPolicyData delayBetweenTransactionsPolicyData;

	public DelayBetweenTransactionsPolicy() {
//    	setSynchronized(true);
	}

//	public TerminalPolicyData getPolicyData() {
//		if (policyData == null) {
//			policyData = new TerminalPolicyData();
//			policyData.setPolicy(this);
//		}
////		return (TerminalPolicyData) policyData;
//		return GeneralDao.Instance.load(TerminalPolicyData.class, policyData.getId());
//	}

//	public void setPolicyData(DelayBetweenTransactionsPolicyData policyData) {
//		this.delayBetweenTransactionsPolicyData = policyData;
//	}

	public Policy clone() {
		DelayBetweenTransactionsPolicy policy = new DelayBetweenTransactionsPolicy();
//		policy.setGeneralDao(generalDao);
//		policy.setProfile(profile);
//		policy.setTerminalService(terminalService);
		policy.setRequiredDelay(requiredDelay);
		return policy;
	}

	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {

		Object[] list =  TerminalService.getPolicyTerminalData(terminal, this);

		TerminalData terminalData = null;
		TerminalPolicyData policyData = null;
		
		if (list != null) {
			terminalData = (TerminalData) list[0];
			policyData = (TerminalPolicyData) list[1];
		}
		
		if (terminalData == null || terminalData.getLastTransactionTime().equals(DateTime.UNKNOWN)) {
			terminalData = new TerminalData();
			if (policyData == null) {
				policyData = new TerminalPolicyData();
				policyData.setPolicy(this);
			}
			policyData.getTermianlData().put(terminal, terminalData); 
		}

		long realDelay = ifx.getReceivedDt().getTime() - terminalData.getLastTransactionTime().getTime();
		if (realDelay > 0 && realDelay < requiredDelay)
			throw new DelayBetweenTransactionsException("Two close transactions from one terminal. delay:" + realDelay
					+ " required:" + requiredDelay);

	}

	public long getRequiredDelay() {
		return requiredDelay;
	}

	public void setRequiredDelay(long requiredDelay) {
		this.requiredDelay = requiredDelay;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DelayBetweenTransactionsPolicy policy = (DelayBetweenTransactionsPolicy) obj;
		if (requiredDelay == policy.requiredDelay)
			return true;
		return false;
	}

	public int hashCode() {
		return (int) requiredDelay;
	}

	@Override
	public void update(Ifx ifx, Terminal terminal) {
		
		Object[] list =  TerminalService.getPolicyTerminalData(terminal, this);
		TerminalData terminalData = (TerminalData) list[0];	
		TerminalPolicyData policyData = (TerminalPolicyData) list[1];
		
		terminalData.setLastTransactionTime(ifx.getReceivedDt());
		GeneralDao.Instance.saveOrUpdate(terminalData);
		policyData.getTermianlData().put(terminal, terminalData);
		GeneralDao.Instance.saveOrUpdate(policyData);
	}

	@Override
	public boolean isSynchronized() {
		return true;
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		// TODO Auto-generated method stub
		
	}

}
