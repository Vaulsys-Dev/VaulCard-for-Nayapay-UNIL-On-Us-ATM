package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.OnlinePerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.exception.LockAcquisitionException;

public class OnlinePerTransactionSettlement {
	public static final Logger logger = Logger.getLogger(OnlinePerTransactionSettlement.class);
	
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
			params.put("onlineSettlement", OnlinePerTransactionSettlementServiceImpl.class);
			clearingProfile = (ClearingProfile) GeneralDao.Instance.findObject(query, params);
			if (clearingProfile == null){
				GeneralDao.Instance.endTransaction();
				logger.debug("Returning Online PerTransaction Settlement Job");
				return;
			}
			
			if(args.length < 2){
				System.out.printf("bad arguements list: args.length < 3");
				System.exit(0);
			}


			

			if (args.length > 2) {
				System.out.println("terms="+args[2]);
				terminals = GeneralDao.Instance.find("from POSTerminal where code in ("+args[2]+")");
//				terminals = GeneralDao.Instance.find("from EPAYTerminal where code in ("+args[2]+")");
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
				OnlinePerTransactionSettlementServiceImpl.Instance.settle(terminals, clearingProfile, clearingProfile.getSettleUntilTime(afterNow), false, true, false, false);
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
				
		logger.debug("Ending Online PerTransaction Settlement Job");		
	}
}


