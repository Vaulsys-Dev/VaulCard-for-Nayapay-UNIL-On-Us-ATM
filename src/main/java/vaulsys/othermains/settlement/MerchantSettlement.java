package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.terminal.impl.Terminal;
import vaulsys.clearing.settlement.MerchantSettlementServiceImpl;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantSettlement {

	public static void main(String[] args) {
		ClearingProfile clearingProfile = null;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		List<Terminal> terminals = null;
		
		try {
			clearingProfile = ClearingService.findClearingProfile(170601L);
			//GlobalContext.getInstance().getMyInstitution(); //Raza commenting for MultiInstitution
			
			
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			return;
		}

		if (clearingProfile != null) {
			
			if (args.length > 2) {
				System.out.println("terms="+args[2]);
				List<Terminal> result = new ArrayList<Terminal>();
				result = GeneralDao.Instance.find("from POSTerminal where code in ("+args[2]+")");
				if (result != null && result.size() > 0) {
					terminals = new ArrayList<Terminal>();
					terminals.addAll(result);
				}
				result = GeneralDao.Instance.find("from EPAYTerminal where code in ("+args[2]+")");
				if (result != null && result.size() > 0) {
					if(terminals == null)
						terminals = new ArrayList<Terminal>();
					terminals.addAll(result);
				}
			}
			
			int day = -1;
			int hour = 23;
			if (args.length >= 2) {
				day = Integer.parseInt(args[0]);
				hour = Integer.parseInt(args[1]);
			}
			
			GeneralDao.Instance.endTransaction();
			
			DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
			MerchantSettlementServiceImpl.Instance.settle(terminals, clearingProfile, settleUntilTime, false, true, true, false);
		}
//		System.exit(0);
	}
}
