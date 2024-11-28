package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.ATMSettlementServiceImpl;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.SchedulerService;
import vaulsys.scheduler.job.CycleSettlementJob;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.List;

public class CycleAccounting {
	public static void main(String[] args) {
		try {
			ClearingProfile clearingProfile = null;
			List<CycleSettlementJob> allCycleSettlementJob = null;
			DateTime now = null;

			GeneralDao.Instance.beginTransaction();
			GlobalContext.getInstance().startup();
			ProcessContext.get().init();

			try {
				allCycleSettlementJob = SchedulerService.getAllCycleSettlementJob();
				now = DateTime.now();
				GlobalContext.getInstance().getMyInstitution();
				GeneralDao.Instance.endTransaction();
			} catch (Exception e) {
				e.printStackTrace();
				GeneralDao.Instance.rollback();
				return;
			}
			for (CycleSettlementJob cycleSettlementJob : allCycleSettlementJob) {
				try {
					clearingProfile = cycleSettlementJob.getClearingProfile();
					if(clearingProfile.getId() != 170603)
						continue;
					
					System.out.println("Accounting ClearingProfile: " + clearingProfile.getId());
					System.out.println("First step...");
//					cycleSettlementJob.getSettlementService().account(clearingProfile,
//							clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(now), false,
//							false, false, false);
					System.out.println("Second step...");
					if (ATMSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass())) {
//						cycleSettlementJob.getSettlementService().account(clearingProfile,
//								clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(now),
//								false, false, false);
						continue;
					} else {
						cycleSettlementJob.getSettlementService().account(clearingProfile,
								clearingProfile.getAccountUntilTime(now), clearingProfile.calcNextSettleTime(now),
								false, false, false, false);
					}
				} catch (Exception e) {
					System.out.println("Exception in Accounting ClearingProfile: " + clearingProfile.getId() + ": " + e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
