package vaulsys.clearing.settlement;

import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.atm.ATMLog;
import vaulsys.terminal.atm.ActionType;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.Util;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

import com.fanap.cms.business.corecommunication.biz.CoreGateway;
import com.fanap.cms.exception.BusinessException;

public class IssueATMFCBThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());

	SettlementData[] settlementDatas;
	
	public IssueATMFCBThread(SettlementData[] sortedSettlementData) {
		super();
		this.settlementDatas = sortedSettlementData;
	}


	@Override
	public void run() {
		logger.debug("I am here...");
		for (SettlementData settlementData : settlementDatas) {
			try {
				GeneralDao.Instance.beginTransaction();
				try {
					logger.debug("Try to lock settlementData " + settlementData.getId());
					settlementData = GeneralDao.Instance.load(SettlementData.class, settlementData.getId());
					settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData, LockMode.UPGRADE_NOWAIT);
					logger.debug("settlementData locked.... " + settlementData.getId());
				} catch (Exception e) {
					logger.warn("settlementData (" + settlementData.getId() + ") was locked, ignore it!", e);
//					GeneralDao.Instance.endTransaction();
					continue;
				}

				SettlementReport report = settlementData.getSettlementReport();

				if (report == null) {
					logger.error("report of settlementData: " + settlementData.getId() + " is NULL!");
//					GeneralDao.Instance.endTransaction();
//					continue;
				}else if (Util.hasText(settlementData.getDocumentNumber())) {
					logger.warn("settlementData: " + settlementData.getId() + " has documentNumber: "
							+ settlementData.getDocumentNumber() + " !!!");
				} else {
					String transactionId = AccountingService.issueFCBDocument(report, true);
					report.setDocumentNumber(transactionId);

					logger.debug("generate transaction id: " + transactionId + " for settledata: " + settlementData.getId());
					// -------------
					settlementData.setDocumentNumber(transactionId);




Double balance = 0D;
					
					try {
						ATMTerminal terminal = GeneralDao.Instance.load(ATMTerminal.class, settlementData.getTerminalId());
//						ATMTerminal terminal = (ATMTerminal) settlementData.getTerminal();
						/*balance = CoreGateway
								.getATMBoxBalance(terminal.getCode().toString(), terminal.getOwner().getCoreBranchCode(), CoreConfigDataManager.getValue(CoreConfigDataManager.CoreUrl));
*/						ATMLog log = new ATMLog(terminal.getCode(),
								"-", "-", "-", "-", null,
								ATMLog.LogState.LAST_STATE,
								ActionType.SUPERVISOR_EXIT);
						log.setBalance(balance);
						GeneralDao.Instance.saveOrUpdate(log);
					} catch (Exception e) {
						logger.error("Exception in getting ATM Box Balance, "
								+ e, e);
					}

					GeneralDao.Instance.saveOrUpdate(report);
					GeneralDao.Instance.saveOrUpdate(settlementData);

				}
//				GeneralDao.Instance.endTransaction();
			} catch (Exception e) {
				logger.error(e, e);
			} finally {
				GeneralDao.Instance.endTransaction();
			}
		}
		logger.debug("I am exiting....");
	}
}
