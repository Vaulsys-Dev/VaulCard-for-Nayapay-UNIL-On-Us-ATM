package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.MerchantSettlementServiceImpl;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantAccount {

	public static void main(String[] args) {
		ClearingProfile clearingProfile = null;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		List<Terminal> terminals = null;
		
		try {
			clearingProfile = ClearingService.findClearingProfile(170601L);
			GlobalContext.getInstance().getMyInstitution();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			return;
		}

		if (clearingProfile != null) {
			
			if (args.length > 3) {
				System.out.println("terms="+args[3]);
				List<Terminal> result = new ArrayList<Terminal>();
				result = GeneralDao.Instance.find("from POSTerminal where code in ("+args[3]+")");
				if (result != null && result.size() > 0) {
					terminals = new ArrayList<Terminal>();
					terminals.addAll(result);
				}
				result = GeneralDao.Instance.find("from EPAYTerminal where code in ("+args[3]+")");
				if (result != null && result.size() > 0) {
					if(terminals == null)
						terminals = new ArrayList<Terminal>();
					terminals.addAll(result);
				}
			}
			
			int day = -1;
			int hour = 23;
			int hourAcc = 9;
			if (args.length >= 2) {
				day = Integer.parseInt(args[0]);
				hour = Integer.parseInt(args[1]);
			}
			
			if (args.length >= 2) {
				hourAcc = Integer.parseInt(args[2]);
			}
			
			GeneralDao.Instance.endTransaction();
			
			DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
			
			DateTime accountUntilTime = DateTime.beforeNow(Math.abs(day));
			accountUntilTime.setDayTime(new DayTime(hourAcc, 59, 59));
			
//			List<String> terminalCodes = MerchantSettlementServiceImpl.Instance.findDesiredTerminalCodes(accountUntilTime, true, clearingProfile);
//			List<Terminal> result = new ArrayList<Terminal>();
//			Map<String, Object> params = new HashMap<String, Object>();
//			params.put("termCode", terminalCodes);
//			result = GeneralDao.Instance.find("from POSTerminal where code in (:termCode)", params);
//			if (result != null && result.size() > 0) {
//				terminals = new ArrayList<Terminal>();
//				terminals.addAll(result);
//			}
//			result = GeneralDao.Instance.find("from EPAYTerminal where code in (:termCode)");
//			if (result != null && result.size() > 0) {
//				if(terminals == null)
//					terminals = new ArrayList<Terminal>();
//				terminals.addAll(result);
//			}
			
			
			try {
				MerchantSettlementServiceImpl.Instance.account(terminals, clearingProfile, accountUntilTime, settleUntilTime, false, false, false, false, false);
			} catch (Exception e) {
			}
		}
//		System.exit(0);
	}
}
