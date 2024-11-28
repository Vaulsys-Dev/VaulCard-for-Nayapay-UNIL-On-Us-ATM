package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.MerchantSettlementServiceImpl;
import vaulsys.clearing.settlement.SaderatSettlementServiceImpl;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ClearingProfileSettlement {
	private static final Logger logger = Logger.getLogger(ClearingProfileSettlement.class);
	public static void main(String[] args) {
		ClearingProfile clearingProfile = null;
//		GeneralDao.Instance.beginTransaction();
//		GlobalContext.getInstance().startup();
//		ProcessContext.get().init();
		List<Terminal> terminals = null;
		
		try {
			if (args.length > 1) {
				GeneralDao.Instance.beginTransaction();
				GlobalContext.getInstance().startup();
				ProcessContext.get().init();
				clearingProfile = ClearingService.findClearingProfile(Util.longValueOf(args[0]));
			} else {
				logger.error("clr Prof in needed!!");
				GeneralDao.Instance.endTransaction();
				System.exit(0);
			}
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
				result = GeneralDao.Instance.find("from ThirdPartyVirtualTerminal where code in ("+args[3]+")");
				if (result != null && result.size() > 0) {
					if(terminals == null)
						terminals = new ArrayList<Terminal>();
					terminals.addAll(result);
				}
			}
			
			int day = -1;
			int hour = 23;
			if (args.length >= 3) {
				day = Integer.parseInt(args[1]);
				hour = Integer.parseInt(args[2]);
			}
			
			GeneralDao.Instance.endTransaction();
			
			DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
			ClearingService.getSettlementService(clearingProfile).settle(terminals, clearingProfile, settleUntilTime, false, true, true, false);
		}
//		System.exit(0);
	}
}
