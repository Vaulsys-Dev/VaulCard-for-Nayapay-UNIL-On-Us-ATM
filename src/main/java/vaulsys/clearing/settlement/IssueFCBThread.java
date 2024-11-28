package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.persistence.GeneralDao;
import vaulsys.transaction.SettledState;
import vaulsys.util.Util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public class IssueFCBThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());

	SettlementData[] settlementDatas;
	
	public IssueFCBThread(SettlementData[] sortedSettlementData) {
		super();
		this.settlementDatas = sortedSettlementData;
	}


	@Override
	public void run() {
		logger.debug("I am here...");
		for (SettlementData settlementData : settlementDatas) {
			try{
				GeneralDao.Instance.beginTransaction();
				try {
					logger.debug("Try to lock settlementData " + settlementData.getId());
					settlementData = GeneralDao.Instance.load(SettlementData.class, settlementData.getId());
					settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData, LockMode.UPGRADE_NOWAIT);
					logger.debug("settlementData locked.... " + settlementData.getId());
				} catch (Exception e) {
					logger.warn("settlementData ("+settlementData.getId()+") was locked, ignore it!", e);
//					GeneralDao.Instance.endTransaction();
					continue;
				}
	
				Long amount = settlementData.getTotalSettlementAmount();
				SettlementReport report = settlementData.getSettlementReport();
	
				if (report == null) {
					logger.error("report of settlementData: " + settlementData.getId() + " is NULL!");
//					GeneralDao.Instance.endTransaction();
//					continue;
				}else if (Util.hasText(settlementData.getDocumentNumber())) {
					logger.warn("settlementData: " + settlementData.getId() + " has documentNumber: "
							+ settlementData.getDocumentNumber() + " !!!");
				} else {
					String transactionId;
					if (amount.equals(0L)) {
						transactionId = "========";
	
					} else {
//						 transactionId = "1234";
						transactionId = AccountingService.issueFCBDocument(report, true);
						logger.debug("generate transaction id: " + transactionId + " for settledata: " + settlementData.getId());
						report.setDocumentNumber(transactionId);
					}
	
					if (transactionId != null) {
	
						settlementData.setDocumentNumber(transactionId);
						if (settlementData.getSettlementState() == null) {
							logger.debug("set correct settle time to settleData: " + settlementData.getId());
//							settlementData.setSettlementTimeLong(DateTime.now().getDateTimeLong());
							settlementData.setSettlementTime(DateTime.now());
						}
						logger.debug("1 settlementData are settled in document-" + transactionId);
	
						if (report != null)
							GeneralDao.Instance.saveOrUpdate(report);
						List<SettlementData> stlData = new ArrayList<SettlementData>();
						stlData.add(settlementData);
						int updateSettlementInfo = AccountingService.updateSettlementInfo(stlData, SettledState.SETTLED);
						logger.debug(updateSettlementInfo + " settlementInfo are settled in document-" + transactionId);
					}
					if (report != null)
						GeneralDao.Instance.saveOrUpdate(report);
					GeneralDao.Instance.saveOrUpdate(settlementData);
				}
			}catch(Exception e){
				logger.error(e,e);
			}finally{
				GeneralDao.Instance.endTransaction();
			}
		}
		logger.debug("I am exiting....");		
	}
}
