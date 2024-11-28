package vaulsys.authorization.policy;

import java.util.ArrayList;
import java.util.List;

import vaulsys.authorization.data.TerminalData;
import vaulsys.authorization.data.TerminalPolicyData;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.DailyAmountExceededException;
import vaulsys.authorization.exception.DelayBetweenTransactionsException;
import vaulsys.authorization.exception.TrnTypeNotAuthorizedException;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.cyclecriteria.CycleCriteria;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.Util;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Table;

@Entity
@DiscriminatorValue(value = "CycleConstraint")
public class CycleConstraintTransactionPolicy extends Policy/*<TerminalPolicyData>*/ {

	@Transient
	public static transient final long UNBOUNDED = -1;
	
	@Embedded
	@AttributeOverrides( {
		@AttributeOverride(name = "cycleType.type", column = @Column(name = "cycletype")),
		@AttributeOverride(name = "cycleCount", column = @Column(name = "cyclecount")) })
	private CycleCriteria criteria;

	@Column(name = "max_amt")
	private Long maxAmount;

	@Column(name = "max_trx")
	private Long maxTransaction;
	
	@Column(name = "required_delay")
	private Long requiredDelay;

	@CollectionOfElements(fetch = FetchType.LAZY)
    @ForeignKey(name="auth_plc_cycle_trx_trnType_fk")
    @Enumerated(value = EnumType.STRING)
    @JoinTable(name = "AUTH_PLC_CYCLE_TRX__TRNTYPE")
	private List<TrnType> trnType;
	
	public CycleConstraintTransactionPolicy() {
//    	setSynchronized(true);
	}

//	public TerminalPolicyData getPolicyData() {
//		if (policyData == null) {
//			policyData = new TerminalPolicyData();
//			policyData.setPolicy(this);
//			setPolicyData(policyData);
//		}
//		return (TerminalPolicyData) policyData;
//	}
	
	public CycleConstraintTransactionPolicy clone() {
		CycleConstraintTransactionPolicy policy = new CycleConstraintTransactionPolicy();
		policy.setMaxAmount(maxAmount);
		policy.setMaxTransaction(maxTransaction);
		policy.setRequiredDelay(requiredDelay);
		policy.setTrnType(new ArrayList<TrnType>(trnType));
		return policy;
	}

	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		
		Object[] list =  TerminalService.getPolicyTerminalData(terminal, this);

		TerminalData terminalData = null;
		TerminalPolicyData policyData = null;
		CycleConstraintTransactionPolicy cycleConstTrnPlc = null;
		
		if (list != null) {
			terminalData = (TerminalData) list[0];
			policyData = (TerminalPolicyData) list[1];
			cycleConstTrnPlc = (CycleConstraintTransactionPolicy) list[2];
		}
			
		if (terminalData == null || terminalData.getLastTransactionTime().equals(DateTime.UNKNOWN)) {
			terminalData = new TerminalData();
			if (policyData == null) {
				policyData = new TerminalPolicyData();
				policyData.setPolicy(this);
			}
			policyData.getTermianlData().put(terminal, terminalData); 
		}

		boolean cpResult = Util.isInCurrentCycle(criteria.getCycleType(), ifx.getReceivedDt(), terminalData
				.getLastTransactionTime().toDate());

		if (!cpResult) {
			terminalData.setAmount(0);
			terminalData.setCount(0);
			GeneralDao.Instance.saveOrUpdate(terminalData);
			policyData.getTermianlData().put(terminal, terminalData);
			GeneralDao.Instance.saveOrUpdate(policyData);
		}

		if(cycleConstTrnPlc != null)
			if(cycleConstTrnPlc.getTrnType() != null && cycleConstTrnPlc.getTrnType().size() > 0)
				if(!cycleConstTrnPlc.getTrnType().contains(ifx.getTrnType()))
					return;
//					throw new TrnTypeNotAuthorizedException("Failed: Not Allowed trnType " + ifx.getTrnType().toString() + " for this terminal");

		if (!ISOFinalMessageType.isReversalMessage(ifx.getIfxType())) {
			long realDelay = ifx.getReceivedDt().getTime() - terminalData.getLastTransactionTime().getTime();
			if (requiredDelay != null && realDelay > 0 && realDelay < requiredDelay)
				throw new DelayBetweenTransactionsException("Two close transactions from one terminal. delay:"
						+ realDelay + " required:" + requiredDelay);
			
		}

		long transactionAmount = ifx.getAuth_Amt();

		long newCount = terminalData.getCount() + 1;
		if (maxTransaction!= null && maxTransaction.longValue() != UNBOUNDED && maxTransaction < newCount) {
			throw new DailyAmountExceededException("Failed: Allowed quota exceeded. max:" + maxTransaction
					+ " current:" + newCount);
		}

		if (!ISOFinalMessageType.isReversalMessage(ifx.getIfxType())
				&& !ISOFinalMessageType.isReturnMessage(ifx.getIfxType())
				&& !ISOFinalMessageType.isBalanceInqueryMessage(ifx.getIfxType())) {

			long newAmount = terminalData.getAmount() + transactionAmount;
			if (maxAmount!= null && maxAmount.longValue() != UNBOUNDED && maxAmount < newAmount) {
				throw new DailyAmountExceededException("Failed: Allowed quota exceeded. max:" + maxAmount
						+ " current:" + newAmount);
			}
		}
	}

	public void update(Ifx ifx, Terminal terminal) {

		Object[] list =  TerminalService.getPolicyTerminalData(terminal, this);
		TerminalData terminalData = (TerminalData) list[0];	
		TerminalPolicyData policyData = (TerminalPolicyData) list[1];
		CycleConstraintTransactionPolicy cycleConstTrnPlc = (CycleConstraintTransactionPolicy) list[2];
		
		long transactionAmount = ifx.getAuth_Amt();
		if(
			(cycleConstTrnPlc.getTrnType() != null && cycleConstTrnPlc.getTrnType().size() > 0 && 
			   cycleConstTrnPlc.getTrnType().contains(ifx.getTrnType())) 
			||
			(cycleConstTrnPlc.getTrnType() == null || cycleConstTrnPlc.getTrnType().size() == 0)
		 ){
			
			if (ISOFinalMessageType.isReturnReverseMessage(ifx.getIfxType()))
				terminalData.addAmount(transactionAmount);
			else if (ISOFinalMessageType.isReversalMessage(ifx.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifx.getIfxType()))
				terminalData.addAmount(-transactionAmount);
			else
				terminalData.addAmount(transactionAmount);

			terminalData.setLastTransactionTime(ifx.getReceivedDt());
			terminalData.setCount(terminalData.getCount() + 1);
			GeneralDao.Instance.saveOrUpdate(terminalData);
			policyData.getTermianlData().put(terminal, terminalData);
			GeneralDao.Instance.saveOrUpdate(policyData);
		}
	}

	public Long getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(Long maxAmount) {
		this.maxAmount = maxAmount;
	}

	public Long getMaxTransaction() {
		return maxTransaction;
	}

	public void setMaxTransaction(Long maxTransaction) {
		this.maxTransaction = maxTransaction;
	}
	
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CycleConstraintTransactionPolicy policy = (CycleConstraintTransactionPolicy) obj;
		if (maxAmount == policy.maxAmount && maxTransaction == policy.maxTransaction)
			return true;
		return false;
	}

	public CycleCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(CycleCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public boolean isSynchronized() {
		return true;
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		// TODO Auto-generated method stub
	}

	public Long getRequiredDelay() {
		return requiredDelay;
	}

	public void setRequiredDelay(Long requiredDelay) {
		this.requiredDelay = requiredDelay;
	}
	
	//Mirkamali
	public List<TrnType> getTrnType() {
		return trnType;
	}

	public void setTrnType(List<TrnType> trnType) {
		this.trnType = trnType;
	}
}
