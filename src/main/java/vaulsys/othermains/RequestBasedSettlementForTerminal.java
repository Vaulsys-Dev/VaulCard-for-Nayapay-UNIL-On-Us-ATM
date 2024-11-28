package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.RequestBasedSettlementServiceImpl;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.exception.LockAcquisitionException;

public class RequestBasedSettlementForTerminal {
	public static final Logger logger = Logger.getLogger(RequestBasedSettlementForTerminal.class);
	
	public static void main(String[] args) {
		ClearingProfile clearingProfile =null;
		DateTime now = null;
		DateTime afterNow = null;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		List<Terminal> terminals = null;
		//GlobalContext.getInstance().getMyInstitution(); //Raza commenting for Multi-Institution

		
		try{
			String query = "from "+ ClearingProfile.class.getName() +" cp where cp.settlementClass = :onlineSettlement";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("onlineSettlement", RequestBasedSettlementServiceImpl.class);
			clearingProfile = (ClearingProfile) GeneralDao.Instance.findObject(query, params);
			if (clearingProfile == null){
				GeneralDao.Instance.endTransaction();
				logger.debug("Returning RequestBasedSettlementForTerminal");
				return;
			}
			
			if(args.length < 2){
				System.out.printf("bad arguements list: args.length < 3");
				System.exit(0);
			}


			if (args.length > 2) {
				terminals = GeneralDao.Instance.find("from POSTerminal where code in ("+args[2]+")");
				
			}
			
			
			int day = -1;
			int hour = 23;
			if (args.length >= 2) {
				day = Integer.parseInt(args[0]);
				hour = Integer.parseInt(args[1]);
			}
			now = DateTime.beforeNow(Math.abs(day));
			now.setDayTime(new DayTime(hour, 59, 59));
			
			afterNow = now;
			
//			terminals = GeneralDao.Instance.find("from POSTerminal where code in (207796)");
			GeneralDao.Instance.endTransaction();
		}catch(Exception e){
			logger.error(e);
			GeneralDao.Instance.rollback();
			return;
		}

		int numTries = 0;
		int maxTries = 3;
		boolean isFinishedAccounting = false;
		
		while(numTries < maxTries && !isFinishedAccounting){
			try{
				RequestBasedSettlementServiceImpl.Instance.settle(terminals, clearingProfile, clearingProfile.getAccountUntilTime(now), true, true, true, false);
				isFinishedAccounting = true;
			}catch(LockAcquisitionException e){
				logger.error("Exception in accounting. LockAcquisitionException: "+numTries+" ",e);
				try {
					Thread.sleep(60000L);
				} catch (InterruptedException e1) {
					continue;
				}
			}catch(Exception e){
				logger.error("Exception in accounting. numTries: "+numTries+" ",e);
				numTries++;
			}
		}

		if(!isFinishedAccounting){
			logger.error("We faced to maxTries Exception in accounting, so we don't proceed in settlement...");
		}
				
		logger.debug("Ending RequestBasedSettlementForTerminal");		
	}


	//	public static void main(String[] args) {
//		
//		GeneralDao.Instance.beginTransaction();
//		GlobalContext.getInstance().startup();
//		ProcessContext.get().init();
//		ClearingProfile clearingProfile = ClearingService.findClearingProfile(26320101L);
//		
//		GeneralDao.Instance.endTransaction();
//		if (clearingProfile != null) {
//			
//			int day = -1;
//			int hour = 23;
//			if (args.length == 2) {
//				day = Integer.parseInt(args[0]);
//				hour = Integer.parseInt(args[1]);
//			}
//			DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
//			settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
//			
//			OnlineSettlementService.Instance.settle(clearingProfile, settleUntilTime, false, true);
//		}
//		System.exit(0);
//	}
}




/*package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class OnlineSettlement {
	public static final Logger logger = Logger.getLogger(SettlementService.class);

	
	public static void main(String[] args) {
		ClearingProfile clearingProfile =null;
		DateTime now = null;
		DateTime afterNow = null;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		List<Terminal> terminals = null;
		GlobalContext.getInstance().getMyInstitution();
		
		try{
			String query = "from "+ ClearingProfile.class.getName() +" cp where cp.settlementClass = :onlineSettlement";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("onlineSettlement", OnlineSettlementService.class);
			clearingProfile = (ClearingProfile) GeneralDao.Instance.findObject(query, params);
			if (clearingProfile == null){
				GeneralDao.Instance.endTransaction();
				logger.debug("Returning Online Settlement Job");
				return;
			}
			
			DateTime prevFireTime = DateTime.now();
//			prevFireTime.decrease(15);
			DateTime curFireTime = prevFireTime;
	//		DateTime next = new DateTime(switchJobContext.getNextFireTime());
	
			now = DateTime.now();
			
			afterNow = DateTime.fromNow(60000);
			afterNow = now;
			
			if (prevFireTime.getDayTime().getHour() != curFireTime.getDayTime().getHour() 
				|| prevFireTime.getDayDate().getDay() != curFireTime.getDayDate().getDay()
				|| prevFireTime.getDayDate().getMonth() != curFireTime.getDayDate().getMonth()
				|| prevFireTime.getDayDate().getYear() != curFireTime.getDayDate().getYear() ){
				afterNow = now;
				logger.info("Different Date, previous: "+ prevFireTime +" current: "+ curFireTime);
			}
//			terminals = GeneralDao.Instance.find("from POSTerminal where code in (207796)");
			GeneralDao.Instance.endTransaction();
//			log.setStatus(SwitchJobStatus.FINISHED);
		}catch(Exception e){
			logger.error(e);
			GeneralDao.Instance.rollback();
//			log.setStatus(SwitchJobStatus.FAILED);
//			log.setExceptionMessage(e.getMessage());
			return;
		}

		
		try{
			OnlineSettlementService.Instance.account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(afterNow), false, false, false, true);
		}catch(Exception e){
			logger.error("Exception in Online Settlement",e);
//			log.setStatus(SwitchJobStatus.FAILED);
//			log.setExceptionMessage(e.getMessage());
		}
		logger.debug("Ending Online Settlement Job");		
	}
}
*/