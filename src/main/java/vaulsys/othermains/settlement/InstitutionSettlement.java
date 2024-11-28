package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.InstitutionSettlementServiceImpl;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

public class InstitutionSettlement {

	public static void main(String[] args) {
		ClearingProfile clearingProfile = null;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();

		try {
			clearingProfile = ClearingService.findClearingProfile(2303701L);
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
			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
			InstitutionSettlementServiceImpl.Instance.settle(clearingProfile, settleUntilTime, false, true, true);
		}
//		System.exit(0);
	}
}
