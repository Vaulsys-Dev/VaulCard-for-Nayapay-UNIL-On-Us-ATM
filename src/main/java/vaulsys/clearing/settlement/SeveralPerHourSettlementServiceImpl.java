package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SeveralPerHourSettlementServiceImpl extends SettlementService{
	private static final Logger logger = Logger.getLogger(SeveralPerHourSettlementServiceImpl.class);
	
	private SeveralPerHourSettlementServiceImpl(){}
	
	public static final SeveralPerHourSettlementServiceImpl Instance = new SeveralPerHourSettlementServiceImpl();
	
	List<Integer> minutes = new ArrayList<Integer>() {{
		add (new Integer(15)); 
		add (new Integer(30));
		add (new Integer(45));
		add (new Integer(60));
	}};
	
	@Override
	public void account(ClearingProfile clearingProfile, DateTime accountUntilTime, DateTime settleUntilTime, Boolean update,
			Boolean waitForSyncObject, Boolean onlyFanapAccount, Boolean considerClearingProcessType) throws Exception {
		
		DateTime realSettleUntilTime = new DateTime(settleUntilTime.getDateTimeLong());
		realSettleUntilTime.decrease(clearingProfile.getAccountTimeOffsetMinute());
		
		int stlMinute = settleUntilTime.getDayTime().getMinute();
		int realStlMinute = realSettleUntilTime.getDayTime().getMinute();
		int minute = 0;
		int realMinute = 0;
		for(Integer i : minutes){
			if(stlMinute >= i) {
				continue;
			} else {
				minute = i;
				break;
			}
		}
		for(Integer i : minutes){
			if(realStlMinute >= i){
				continue;
			}else {
				realMinute = i;
				break;
			}
		}
		settleUntilTime.getDayTime().setMinute(minute - 1);
		settleUntilTime.getDayTime().setSecond(59);
		realSettleUntilTime.getDayTime().setMinute(realMinute - 1);
		realSettleUntilTime.getDayTime().setSecond(59);
		if(realSettleUntilTime.before(settleUntilTime))
			settleUntilTime = realSettleUntilTime;
		
		super.account(clearingProfile, accountUntilTime, settleUntilTime, update, waitForSyncObject, onlyFanapAccount, considerClearingProcessType);
	}
	
	@Override
	public void settle(ClearingProfile clearingProfile, DateTime settleUntilTime, Boolean update, Boolean settleTime, Boolean generateSettleState) {

		int minute = 0;
		int sttlMinute = settleUntilTime.getDayTime().getMinute();
//		int hr = settleUntilTime.getDayTime().getHour();
		for(int i = 0; i < minutes.size(); i++){
			if(sttlMinute >= minutes.get(i)){
				continue;
			} else {
				if(i == 0){
					minute = minutes.get(minutes.size() - 1);
					settleUntilTime.decrease(60);
//					hr = hr -1;
				}else
					minute = minutes.get(i - 1);
				break;
			}
		}
		settleUntilTime.getDayTime().setMinute(minute - 1);
		settleUntilTime.getDayTime().setSecond(59);
		/*********************************************************/
		try{
			super.account(clearingProfile, clearingProfile.getAccountUntilTime(DateTime.now()), settleUntilTime, update, false, false, false);
		}catch(Exception e){
			
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
		return "چند بار در ساعت";
	}

	@Override
	boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return (FinancialEntityRole.SHOP.equals(entity.getRole()) ||
				FinancialEntityRole.MERCHANT.equals(entity.getRole()));
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals,
			ClearingProfile clearingProfile) {
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
	List<Terminal> findAllTerminals(List<Terminal> terminals,
			List<Long> termCodes, ClearingProfile clearingProfile) {
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
		
		List<String> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(POSTerminal.class, accountUntilTime, justToday, guaranteePeriod);

		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		return terminals;
	}
}
