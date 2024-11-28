package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.ATMDailyRecordSettlementServiceImpl;
import vaulsys.clearing.settlement.ATMDailySettlementServiceImpl;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.List;

public class ATMDailySettlementRecord {
    private static final Logger logger = Logger.getLogger(ATMDailySettlementRecord.class);

	public static void main(String[] args) {
		 atmSettlement(args);
	}

	private static void atmSettlement(String[] args) {
		GeneralDao.Instance.beginTransaction();

		GlobalContext.getInstance().startup();
		ProcessContext.get().init();

		ClearingProfile clearingProfile = ClearingService.findClearingProfile(170600L);
		if(args.length < 2){
			logger.info("bad arguements list: args.length < 3");
			System.exit(0);
		}


		List<Terminal> terminals = null;

		if(args.length == 3) {
			terminals = GeneralDao.Instance.find("from ATMTerminal where code in ("+args[2]+")");
		}
		GlobalContext.getInstance().getMyInstitution();
		GeneralDao.Instance.endTransaction();

		 if (clearingProfile != null) {
			int day = -1;
			int hour = 23;
			if (args.length >= 2) {
				day = Integer.parseInt(args[0]);
				hour = Integer.parseInt(args[1]);
			}
			DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));

//			 ATMSettlementServiceImpl.Instance.settle(clearingProfile, settleUntilTime, true);
			 try {
				ATMDailyRecordSettlementServiceImpl.Instance.settle(terminals, clearingProfile, settleUntilTime, true, true, false, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }


		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


//		 ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
//		 ThreadGroup parentGroup;
//		 while((parentGroup = rootGroup.getParent()) != null) {
//			 rootGroup = parentGroup;
//		 }
//
//		 Thread[] arr = new Thread[100];
//		 int noOfThread = rootGroup.enumerate(arr, true);
//
//		 for(int i=0; i<noOfThread; i++) {
//			 Thread th = arr[i];
//			 System.out.printf("Name[%s], isAlive[$%s]\n", th.getName(), th.isAlive());
//		 }

	}

}
