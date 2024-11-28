package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.MCIBillPaymentSettlementServiceImpl;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Organization;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

public class NewMCIBillSettlement {
	public static void main(String[] args) {
		ClearingProfile clearingProfile = null;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		try {
			clearingProfile = ClearingService.findClearingProfile(170604L);
			GlobalContext.getInstance().getMyInstitution();
			
			GeneralDao.Instance.endTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			return;
		}

		if (clearingProfile != null) {
			int day = -1;
			int hour = 23;
			if (args.length == 2) {
				day = Integer.parseInt(args[0]);
				hour = Integer.parseInt(args[1]);
			}
			
			DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
			DateTime untilTime = DateTime.now();
			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
			
			DateTime now = DateTime.now();
			DateTime accountUntilTime = clearingProfile.getAccountUntilTime(now);
			
//			DateTime settleUntilTime = clearingProfile.getSettleUntilTime(now);
			
//			DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
//			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
			MCIBillPaymentSettlementServiceImpl.Instance.settle(null, clearingProfile, accountUntilTime,settleUntilTime, false);
		}
		System.exit(0);
	}

}
