package vaulsys.authorization.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.ghasemkiani.util.icu.PersianCalendar;

import vaulsys.authorization.data.CardData;
import vaulsys.authorization.data.CardPolicyData;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.DailyAmountExceededException;
import vaulsys.calendar.DateTime;

import vaulsys.clearing.cyclecriteria.CycleCriteria;
import vaulsys.clearing.cyclecriteria.CycleType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;

/**
 * 
 * @author p.moosavi Check if per cycle maximum transaction amount of each card
 *         is reached or not
 * 
 */
@Entity
@DiscriminatorValue(value = "CardMaxAmtPerCycle")
public class CardMaxAmountPerCyclePolicy extends Policy {

	@Transient
	public static transient final long UNBOUNDED = -1;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "cycleType.type", column = @Column(name = "cycletype")),
			@AttributeOverride(name = "cycleCount", column = @Column(name = "cyclecount")) })
	private CycleCriteria criteria;

	@Column(name = "max_amt")
	private Long maxAmount;

	public CycleCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(CycleCriteria criteria) {
		this.criteria = criteria;
	}

	public Long getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(Long maxAmount) {
		this.maxAmount = maxAmount;
	}

	@Override
	public CardMaxAmountPerCyclePolicy clone() {
		CardMaxAmountPerCyclePolicy policy = new CardMaxAmountPerCyclePolicy();
		policy.setMaxAmount(maxAmount);
		return policy;
	}

	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal)
			throws AuthorizationException {
		if( ISOFinalMessageType.isWithdrawalCurMessage(ifx.getIfxType())){
		String appPan = ifx.getAppPAN();
		Object[] list = TerminalService.getPolicyCardData(appPan, this);

		CardData cardData = null;
		CardPolicyData policyData = null;
		CardMaxAmountPerCyclePolicy maxAmtPerCyclePlc = null;

		if (list != null) {
			cardData = (CardData) list[0];
			policyData = (CardPolicyData) list[1];
			maxAmtPerCyclePlc = (CardMaxAmountPerCyclePolicy) list[2];
		}

		if (cardData == null) {
			cardData = new CardData();
			cardData.setAppPAN(ifx.getAppPAN());
			cardData.setFireTime(setFireTime(criteria, ifx.getReceivedDt()));

			if (policyData == null) {
				policyData = new CardPolicyData();
				policyData.setPolicy(this);
			}
			policyData.setCardData(cardData);
		}

		boolean cpResult = isInCurrentCycle(criteria, ifx.getReceivedDt(),
						cardData.getLastTransactionTime().toDate());
//		boolean cfResult = isInCurrentCycle(criteria,
//				ifx.getReceivedDt(), cardData.getFireTime().toDate());

		if (!cpResult /*&& !cfResult*/) {
			cardData.setAmount(0);
			cardData.setFireTime(setFireTime(criteria, ifx.getReceivedDt()));
			GeneralDao.Instance.saveOrUpdate(cardData);
			policyData.setCardData(cardData);
			GeneralDao.Instance.saveOrUpdate(policyData);
		}

		long transactionAmount = ifx.getReal_Amt();

		if (!ISOFinalMessageType.isReversalMessage(ifx.getIfxType())
				&& !ISOFinalMessageType.isReturnMessage(ifx.getIfxType())
				&& ISOFinalMessageType.isWithdrawalCurMessage(ifx.getIfxType())) {

			long newAmount = cardData.getAmount() + transactionAmount;
			if (maxAmount != null && maxAmount.longValue() != UNBOUNDED
					&& maxAmount < newAmount) {
				throw new DailyAmountExceededException(
						"Failed: Allowed quota exceeded. max:" + maxAmount
								+ " current:" + newAmount);
			}
		}
		}
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal)
			throws AuthorizationException {
		// TODO Auto-generated method stub
		ifx.getIfxType();
	}

	@Override
	public void update(Ifx ifx, Terminal terminal) {
		Transaction transaction= ifx.getTransaction();
		if(transaction !=null ){
		if((transaction.getIncomingIfx() != null &&ISOFinalMessageType.isWithdrawalCurMessage(transaction.getIncomingIfx().getIfxType())) ||
				(transaction.getReferenceTransaction() != null && ISOFinalMessageType.isWithdrawalCurMessage(transaction.getReferenceTransaction().getIncomingIfx().getIfxType()))){
		Object[] list = TerminalService
				.getPolicyCardData(ifx.getAppPAN(), this);
		if( list != null && list.length != 0 )
		{
		CardData cardData = (CardData) list[0];
		CardPolicyData policyData = (CardPolicyData) list[1];
		CardMaxAmountPerCyclePolicy maxAmtPerCyclePlc = (CardMaxAmountPerCyclePolicy) list[2];

		long transactionAmount = ifx.getReal_Amt();
		
		if (ISOFinalMessageType.isReversalMessage(ifx.getIfxType())
				|| ISOFinalMessageType.isReturnMessage(ifx.getIfxType())){
			if(isReversalinCurrenctCycle(maxAmtPerCyclePlc.getCriteria(),cardData, ifx.getTransaction().getReferenceTransaction().getIncomingIfx().getReceivedDt())){
			cardData.addAmount(-transactionAmount);
			}
		}
		else
			cardData.addAmount(transactionAmount);

		cardData.setLastTransactionTime(ifx.getReceivedDt());
		GeneralDao.Instance.saveOrUpdate(cardData);
		policyData.setCardData(cardData);
		GeneralDao.Instance.saveOrUpdate(policyData);
		}
		}
		}

	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CardMaxAmountPerCyclePolicy policy = (CardMaxAmountPerCyclePolicy) obj;
		if (maxAmount == policy.maxAmount)
			return true;
		return false;
	}

	@Override
	public boolean isSynchronized() {
		return true;
	}

	public DateTime setFireTime(CycleCriteria criteria, DateTime currentDate) {

		CycleType cycleType = criteria.getCycleType();
		long offset = criteria.calculateOffset();

		PersianCalendar currentPC = new PersianCalendar();
		currentPC.setTime(currentDate.toDate());
		DateTime localDT = DateTime.toDateTime(currentPC.getTimeInMillis()+ offset);
		Calendar localCL = Calendar.getInstance();
		localCL.setTime(localDT.toDate());
		Calendar fireTimeCL = Calendar.getInstance();

		switch (cycleType.getType()) {
		case CycleType.PER_MINUTE_VALUE:
			fireTimeCL.set(localCL.get(Calendar.YEAR),
					localCL.get(Calendar.MONTH),
					localCL.get(Calendar.DAY_OF_MONTH),
					localCL.get(Calendar.HOUR), localCL.get(Calendar.MINUTE),
					59);

		case CycleType.PER_HOUR_VALUE:
			fireTimeCL.set(localCL.get(Calendar.YEAR),
					localCL.get(Calendar.MONTH),
					localCL.get(Calendar.DAY_OF_MONTH),
					localCL.get(Calendar.HOUR), 59, 59);

		case CycleType.PER_DAY_VALUE:
			fireTimeCL.set(localCL.get(Calendar.YEAR),
					localCL.get(Calendar.MONTH),
					localCL.get(Calendar.DAY_OF_MONTH), 23, 59, 59);

		case CycleType.PER_WEEK_VALUE:
			// fireTime.set(
			// currentPC.get(PersianCalendar.YEAR),
			// currentPC.get(PersianCalendar.MONTH),
			// currentPC.get(PersianCalendar.WEEK_OF_MONTH),
			// currentPC.get(PersianCalendar.HOUR),
			// currentPC.get(PersianCalendar.MINUTE),59);
			// return newPC.get(PersianCalendar.YEAR) ==
			// currentPC.get(PersianCalendar.YEAR)
			// && newPC.get(PersianCalendar.MONTH) ==n
			// currentPC.get(PersianCalendar.MONTH)
			// && newPC.get(PersianCalendar.WEEK_OF_MONTH) ==
			// currentPC.get(PersianCalendar.WEEK_OF_MONTH);
		case CycleType.PER_MONTH_VALUE:
			// return newPC.get(PersianCalendar.YEAR) ==
			// currentPC.get(PersianCalendar.YEAR)
			// && newPC.get(PersianCalendar.MONTH) ==
			// currentPC.get(PersianCalendar.MONTH);
		case CycleType.PER_YEAR_VALUE:
			// return newPC.get(PersianCalendar.YEAR) ==
			// currentPC.get(PersianCalendar.YEAR);
		}

		DateTime fireTime = DateTime.toDateTime(fireTimeCL.getTimeInMillis()
				- offset);
		return fireTime;

	}

	public static boolean isInCurrentCycle(CycleCriteria criteria,DateTime currentDate, Date newDate) {
		CycleType cycleType = criteria.getCycleType();
		long offset = criteria.calculateOffset();

		PersianCalendar currentPC = new PersianCalendar();
		currentPC.setTime(currentDate.toDate());
		DateTime localCurDT = DateTime.toDateTime(currentPC.getTimeInMillis()+ offset);
		Calendar localCurCL = Calendar.getInstance();
		localCurCL.setTime(localCurDT.toDate());

		PersianCalendar newPC = new PersianCalendar();
		newPC.setTime(newDate);
		DateTime localNewDT = DateTime.toDateTime(newPC.getTimeInMillis()+ offset);
		Calendar localNewCL = Calendar.getInstance();
		localNewCL.setTime(localNewDT.toDate());

		switch (cycleType.getType()) {
		case CycleType.PER_MINUTE_VALUE:
			return localNewCL.get(Calendar.YEAR) == localCurCL
					.get(Calendar.YEAR)
					&& localNewCL.get(Calendar.MONTH) == localCurCL
							.get(Calendar.MONTH)
					&& localNewCL.get(Calendar.DAY_OF_MONTH) == localCurCL
							.get(Calendar.DAY_OF_MONTH)
					&& localNewCL.get(Calendar.HOUR) == localCurCL
							.get(Calendar.HOUR)
					&& localNewCL.get(Calendar.MINUTE) == localCurCL
							.get(Calendar.MINUTE);
		case CycleType.PER_HOUR_VALUE:
			return localNewCL.get(Calendar.YEAR) == localCurCL
					.get(Calendar.YEAR)
					&& localNewCL.get(Calendar.MONTH) == localCurCL
							.get(Calendar.MONTH)
					&& localNewCL.get(Calendar.DAY_OF_MONTH) == localCurCL
							.get(Calendar.DAY_OF_MONTH)
					&& localNewCL.get(Calendar.HOUR) == localCurCL
							.get(Calendar.HOUR);
		case CycleType.PER_DAY_VALUE:
			return localNewCL.get(Calendar.YEAR) == localCurCL
					.get(Calendar.YEAR)
					&& localNewCL.get(Calendar.MONTH) == localCurCL
							.get(Calendar.MONTH)
					&& localNewCL.get(Calendar.DAY_OF_MONTH) == localCurCL
							.get(Calendar.DAY_OF_MONTH);
		case CycleType.PER_WEEK_VALUE:
			return localNewCL.get(Calendar.YEAR) == localCurCL
					.get(Calendar.YEAR)
					&& localNewCL.get(Calendar.MONTH) == localCurCL
							.get(Calendar.MONTH)
					&& localNewCL.get(Calendar.WEEK_OF_MONTH) == localCurCL
							.get(Calendar.WEEK_OF_MONTH);
		case CycleType.PER_MONTH_VALUE:
			return localNewCL.get(Calendar.YEAR) == localCurCL
					.get(Calendar.YEAR)
					&& localNewCL.get(Calendar.MONTH) == localCurCL
							.get(Calendar.MONTH);
		case CycleType.PER_YEAR_VALUE:
			return localNewCL.get(Calendar.YEAR) == localCurCL
					.get(Calendar.YEAR);
		}

		return true;
	}
	public static boolean isReversalinCurrenctCycle(CycleCriteria criteria,CardData cardData, DateTime recievedDate){
		if(cardData.getAmount() == 0 && DateTime.UNKNOWN.equals(cardData.getLastTransactionTime()))
	        return false;
		else if(!isInCurrentCycle(criteria, cardData.getLastTransactionTime(), recievedDate.toDate()))
			return false;
		else
			return true;
	}

}
