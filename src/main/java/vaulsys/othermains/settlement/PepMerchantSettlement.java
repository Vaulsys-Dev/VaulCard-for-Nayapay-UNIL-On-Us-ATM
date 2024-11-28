package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.PEPSettlementServiceImpl;
import vaulsys.persistence.GeneralDao;

public class PepMerchantSettlement {
	public static void main(String[] args) {
		ClearingProfile clearingProfile = null;
		GeneralDao.Instance.beginTransaction();
		try {
			clearingProfile = ClearingService.findClearingProfile(6801L);
			GeneralDao.Instance.commit();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			return;
		}

		if (clearingProfile != null) {

			try {
				int day = -1;
				int hour = 23;
				if (args.length == 2) {
					day = Integer.parseInt(args[0]);
					hour = Integer.parseInt(args[1]);
				}
//				int day = Integer.parseInt(args[0]);
//				int hour = Integer.parseInt(args[1]);
				DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
				settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
				PEPSettlementServiceImpl.Instance.settle(clearingProfile, settleUntilTime, false, true, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
