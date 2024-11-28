package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class SeveralPerDaySettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(SeveralPerDaySettlementServiceImpl.class);
	
	private SeveralPerDaySettlementServiceImpl(){}
	
	public static final SeveralPerDaySettlementServiceImpl Instance = new SeveralPerDaySettlementServiceImpl();

	static List<Integer> hours = new ArrayList<Integer>() {{
		add (new Integer(6)); 
		add (new Integer(12));
		add (new Integer(18));
		add (new Integer(24));
	}};
	
	
	@Override
	public void account(ClearingProfile clearingProfile, DateTime accountUntilTime, DateTime settleUntilTime, Boolean update,
			Boolean waitForSyncObject, Boolean onlyFanapAccount, Boolean considerClearingProcessType) throws Exception {
		
		DateTime realSettleUntilTime = new DateTime(settleUntilTime.getDateTimeLong());
		realSettleUntilTime.decrease(clearingProfile.getAccountTimeOffsetMinute());
		
		int stlHour = settleUntilTime.getDayTime().getHour();
		int realStlHour = realSettleUntilTime.getDayTime().getHour();
		int hour = 0;
		int realHour = 0;
		for (Integer i : hours) {
			if (stlHour >= i) {
				continue;
			} else {
				hour = i;
				break;
			}
		}
		for (Integer i : hours) {
			if (realStlHour >= i) {
				continue;
			} else {
				realHour = i;
				break;
			}
		}
		
		settleUntilTime.getDayTime().setHour(hour - 1);
		settleUntilTime.getDayTime().setMinute(59);
		settleUntilTime.getDayTime().setSecond(59);
		realSettleUntilTime.getDayTime().setHour(realHour - 1);
		realSettleUntilTime.getDayTime().setMinute(59);
		realSettleUntilTime.getDayTime().setSecond(59);
		if(realSettleUntilTime.before(settleUntilTime))
			settleUntilTime = realSettleUntilTime;
		
		
		
		super.account(clearingProfile, accountUntilTime, settleUntilTime, update, waitForSyncObject, onlyFanapAccount, considerClearingProcessType);
	}
	
	@Override
	public void settle(ClearingProfile clearingProfile, DateTime settleUntilTime, Boolean update, Boolean settleTime, Boolean generateSettleState) {
		int hour = 0;
		int nextHour = 0;
		int sttlHour = settleUntilTime.getDayTime().getHour();
		DayDate day = new DayDate(settleUntilTime.getDayDate().toDate());
		DateTime nextSettleUntilTime = new DateTime(day, new DayTime(0, 0, 0));
		for(int i = 0; i < hours.size(); i++){
			if(sttlHour >= hours.get(i)){
				continue;
			} else {
				if(i == 0){
					hour = hours.get(hours.size() - 1);
					nextHour = hours.get(0);
					settleUntilTime.setDayDate(settleUntilTime.getDayDate().previousDay());
//					sttlDay = sttlDay - 1;
				} 
				else {
					hour = hours.get(i - 1);
					nextHour = hours.get(i);
				}
				break;
			}
		}
		settleUntilTime.setDayTime(new DayTime(hour - 1, 59, 59));
		nextSettleUntilTime.setDayTime(new DayTime(nextHour - 1, 59 ,59));
		DateTime accountUntilTime = clearingProfile.getAccountUntilTime(settleUntilTime/*DateTime.now()*/); 
		try{
			super.account(clearingProfile, accountUntilTime, settleUntilTime, update, false, false, false);
			super.account(clearingProfile, settleUntilTime, nextSettleUntilTime, update, false, false, false);
		}catch (Exception e) {
			logger.error(e);
		}
		settle(null, clearingProfile, settleUntilTime, update, settleTime, generateSettleState, false);
	}
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("source");
		return result;
	}

	@Override
	public void generateDesiredSettlementReports(
			ClearingProfile clearingProfile, DateTime settleDate)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSettlementTypeDesc() {
		return "چند بار در روز";
	}

	@Override
	boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return (FinancialEntityRole.SHOP.equals(entity.getRole()) ||
				FinancialEntityRole.MERCHANT.equals(entity.getRole()));
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminals(POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminals(EPAYTerminal.class, clearingProfile);
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
		return terminals;
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, EPAYTerminal.class, clearingProfile);
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
		return terminals;
	}

	@Override
	List<String> findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday,
			ClearingProfile clearingProfile) {
		List<String> terminals = new ArrayList<String>();
		Integer guaranteePeriod = 0;
		if(justToday){
			guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
		}else{
			guaranteePeriod = clearingProfile.getSettleGuaranteeDay();			
		}
		logger.debug("for clr_prof " + clearingProfile.getId() + " accountiUntilTime is: " + accountUntilTime + " , guatanteePeriod is: " + guaranteePeriod);
		List<String> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(POSTerminal.class, accountUntilTime, justToday, guaranteePeriod);
		
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		logger.debug("number of temrinal found: " + terminals.size());
		return terminals;
	}
}
