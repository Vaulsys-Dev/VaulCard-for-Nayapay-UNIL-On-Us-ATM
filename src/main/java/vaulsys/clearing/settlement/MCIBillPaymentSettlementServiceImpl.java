package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.SettledState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.exception.LockAcquisitionException;

public class MCIBillPaymentSettlementServiceImpl extends BillPaymentSettlementServiceImpl {
	private static final Logger logger = Logger.getLogger(MCIBillPaymentSettlementServiceImpl.class);
	
	private MCIBillPaymentSettlementServiceImpl(){}
	
	public static final MCIBillPaymentSettlementServiceImpl Instance = new MCIBillPaymentSettlementServiceImpl();

	
	@Override
	public void account(ClearingProfile clearingProfile, DateTime accountUntilTime, DateTime settleUntilTime,
			Boolean update, Boolean waitForSyncObject, Boolean onlyFanapAccount, Boolean considerClearingProcessType) throws Exception {
		logger.info("No accounting is done!!");
	}
	
	public void settle(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime accountUntilTime, DateTime settleUntilTime, Boolean update) {
		//A trick to settle in case that we encounter an exception in accounting step
		//We will try for 3 times, if error exist then we do not proceed settlement process
		int numTries = 0;
		int maxTries = 3;
		boolean isFinishedAccounting = false;
		
		boolean justToday = true;
		if (settleUntilTime.equals(accountUntilTime)){
			justToday = false;
		}
		
		DateTime realSettleUntilTime = settleUntilTime;
		if (settleUntilTime.before(accountUntilTime)){
			realSettleUntilTime = accountUntilTime.fromNow(1000);
		}
		
		while(numTries < maxTries && !isFinishedAccounting){
			try{
				account(terminals, clearingProfile, accountUntilTime, realSettleUntilTime, update, true, false, true, false);
				isFinishedAccounting = true;
			}catch(LockAcquisitionException e){
				logger.error("Exception in accounting. LockAcquisitionException: "+numTries+" ",e);
				try {
					Thread.sleep(10000L);
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
			return;
		}

		try {
			logger.info("Generating Settlement Data Report...");
			try {
				generateSettlementDataReport(terminals, clearingProfile, realSettleUntilTime);
			} catch (Exception e) {
				logger.error("Exception in Generating Settlement Data Report " + e);
			}

			GeneralDao.Instance.beginTransaction();
			GeneralDao.Instance.refresh(clearingProfile);
			
			logger.info("Generating Desired Terminal Settlement Report...");
			try {
				generateDesiredSettlementReports(clearingProfile, realSettleUntilTime);
			} catch (Exception e) {
				logger.error("Exception in Generating Desired Terminal Settlement Report  " + e);
			}
			
			GeneralDao.Instance.endTransaction();
			
//			GeneralDao.Instance.beginTransaction();
//			GeneralDao.Instance.refresh(clearingProfile);
//			
//			logger.info("Puting Settle Flag...");
//			settleTransactions(clearingProfile, accountUntilTime);
//			logger.info("End of Put Settle Flag.");
//			
//			GeneralDao.Instance.endTransaction();

			if (!justToday) {
				GeneralDao.Instance.beginTransaction();
				GeneralDao.Instance.refresh(clearingProfile);
				logger.info("Generating Settlement State Report...");
				try {
					generateSettlementStateAndReport(terminals, clearingProfile, settleUntilTime, getSettlementTypeDesc());
				} catch (Exception e) {
					logger.error("Exception in Generating Settlement State Report, must be rollback beacuase incorrect SettlementState created! "+ e);
					GeneralDao.Instance.rollback();
					return;
				}

				GeneralDao.Instance.endTransaction();
				
				GeneralDao.Instance.beginTransaction();
				GeneralDao.Instance.refresh(clearingProfile);
				
				logger.info("Generating Final Settlement State Report...");
				try {
					generateDocumentSettlementState(clearingProfile, getSettlementTypeDesc(), settleUntilTime);
				} catch (Exception e) {
					logger.error("Exception in Generating Final Settlement State Report  " + e);
				}
				GeneralDao.Instance.endTransaction();
			}

		} catch (Exception e) {
			logger.error(e);
			GeneralDao.Instance.rollback();
			return;
		}
//		GeneralDao.Instance.endTransaction();
	}

	@Override
	public void settleTransactions(ClearingProfile clearingProfile, DateTime settleDate) {
		DateTime currentTime = DateTime.now();

		List<String> srcDestList = getSrcDest();
		
		if (srcDestList != null && srcDestList.size() > 0) {
			for (String srcDest: srcDestList) { 
				String query = "update SettlementInfo s set " 
						+ " s.settledState = :settledState " 
						+ " , "
						+ " s.settledDate.dayDate = :settledDate " 
						+ " , " 
						+ " s.settledDate.dayTime = :settledTime " 
						+ " where "
						+ " s.settledState = :notSettled " 
						+ " and s in (" 
						+		"select t." + srcDest + "SettleInfo from Transaction t "
//						+		" , SettlementDataReport rd "
//						+		" , SettlementData d" 
						+		" where " 
//						+		" t." + srcDest + "SettleInfo.settlementData.id = d.id "
//						+		" and rd.settlementData.id = d.id "
//						+		" and rd.type = :thirdType "
						+		" t." + srcDest + "SettleInfo.accountingState = :counted " 
						+		" and t." + srcDest + "SettleInfo.settlementData.clearingProfile = :clearingProfile"
						+		")";
		
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("settledDate", currentTime.getDayDate());
				params.put("settledTime", currentTime.getDayTime());
				params.put("settledState", SettledState.SENT_FOR_SETTLEMENT);
				params.put("counted", AccountingState.COUNTED);
				params.put("notSettled", SettledState.NOT_SETTLED);
				params.put("clearingProfile", clearingProfile);
//				params.put("thirdType", SettlementDataReportType.THIRDPARTY_REPORT);
				getGeneralDao().executeUpdate(query, params);
				getGeneralDao().flush();
			}
		}
	}
	
	
	public void settleTransactions(List<SettlementData> settlementData) {
		DateTime currentTime = DateTime.now();

		String query = "update SettlementInfo s set " +
					 	" s.settledState = :settledState " +
					 	" , s.settledDate.dayDate = :settledDate " +
					 	" , s.settledDate.dayTime = :settledTime " +
					 	" where s.settledState = :notSettled " +
					 	" and s.settlementData in ( :stlData)";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("settledDate", currentTime.getDayDate());
		params.put("settledTime", currentTime.getDayTime());
		params.put("settledState", SettledState.SENT_FOR_SETTLEMENT);
		params.put("notSettled", SettledState.NOT_SETTLED);
		params.put("stlData", settlementData);
		getGeneralDao().executeUpdate(query, params);
		getGeneralDao().flush();
	}
	
	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
		List<SettlementData> settlementDatas = new ArrayList<SettlementData>();
		List<SettlementData> settlementDataList = AccountingService.findAllNotSettledMCISettlementDataUntilTime(clearingProfile, settleDate);
		for (SettlementData settlementData : settlementDataList) {
			if (settlementData.getThirdPartyReport() == null)
				settlementDatas.add(settlementData);
		}
		if (settlementDatas!= null && !settlementDatas.isEmpty()){
			ReportGenerator.generateThirdPartyReportBySettlementData(getThirdPartyType(), settlementDatas, null, null);
			logger.info("Putting Settle Flag...");
			settleTransactions(settlementDatas);
			logger.info("End of Putting Settle Flag.");
		}
	}
}
