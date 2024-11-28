package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.LockObject;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.TransactionService;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

public class AccountingThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());

	List<Terminal> terminals;
	List<Long> termForQuery;
	Long clrProfId;
	DateTime accountUntilTime;
	DateTime settleUntilTime; 
	Boolean update;
	Boolean onlineProcess;
	Boolean settleTime;
	Boolean considerClearingProcessType;
	Boolean onlyFanapAccount;
	Boolean lockAcq;
	Semaphore semaphore;
	Boolean waitForSyncObject;
	
	public AccountingThread(List<Terminal> terminals, List<Long> termForQuery, Long clrProfId, 
			DateTime accountUntilTime, DateTime settleUntilTime, Boolean update, Boolean onlineProcess,
			Boolean settleTime, Boolean considerClearingProcessType, Boolean onlyFanapAccount, Boolean lockAcq, Semaphore semaphore, Boolean waitForSyncObject) {
		super();
		this.terminals = terminals;
		this.termForQuery = termForQuery;
		this.clrProfId = clrProfId;
		this.accountUntilTime = accountUntilTime;
		this.settleUntilTime = settleUntilTime;
		this.update = update;
		this.onlineProcess = onlineProcess;
		this.settleTime = settleTime;
		this.considerClearingProcessType = considerClearingProcessType;
		this.onlyFanapAccount = onlyFanapAccount;
		this.lockAcq = lockAcq;
		this.semaphore = semaphore;
		this.waitForSyncObject = waitForSyncObject;
	}


	@Override
	public void run() {
		logger.debug("I am here...");
		
		try {
			accountThreadProcess(terminals, termForQuery, clrProfId, 
					accountUntilTime, settleUntilTime, update, onlineProcess,
					settleTime, considerClearingProcessType, onlyFanapAccount, lockAcq, semaphore, waitForSyncObject);
		} catch (Exception e) {
			logger.error(e,e);
		}
		
		logger.debug("I am exiting....");		
	}


	public void accountThreadProcess(List<Terminal> terminals, List<Long> termForQuery, Long clrProfId, 
			DateTime accountUntilTime, DateTime settleUntilTime, Boolean update, Boolean onlineProcess,
			Boolean settleTime, Boolean considerClearingProcessType, Boolean onlyFanapAccount, Boolean lockAcq, Semaphore semaphore, Boolean waitForSyncObject) throws Exception {
		
		List<Long> nextRoundTerminal = termForQuery;
		List<Terminal> firstRoundTerminal = null;
		List<Terminal> tmpTerminals = null;
		List<Long> trxSettleRecord = new ArrayList<Long>();
		List<Message> postPrep = new ArrayList<Message>();
		boolean firstRound = true;
		ClearingProfile clearingProfile;

		
		ProcessContext.get().init();
		GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
//		GeneralDao.Instance.refresh(clearingProfile);
		clearingProfile = GeneralDao.Instance.load(ClearingProfile.class, clrProfId);
		SettlementService settlementService = ClearingService.getSettlementService(clearingProfile);
		
		ProcessContext.get().init();
		
		while (firstRound || !nextRoundTerminal.isEmpty()) {

//			if (onlineProcess)
//				trxSettleRecord = TransactionService.getTransactionFromSettlementRecord(termForQuery);

			tmpTerminals = settlementService.findAllTerminals(terminals, nextRoundTerminal, clearingProfile);
			if (tmpTerminals != null && tmpTerminals.size() > 0) {
				try {
					logger.info(tmpTerminals.size() + " terminal with clrProfile: " + clearingProfile.getId());

					logger.info("Preparing Settlement...");

					if (firstRound) {
						firstRoundTerminal = tmpTerminals;
						firstRound = false;
					}

					nextRoundTerminal = settlementService.prepareForSettlement(clearingProfile, tmpTerminals, accountUntilTime, settleUntilTime, settleTime,
							considerClearingProcessType, /*trxSettleRecord,*/ waitForSyncObject);

					if (nextRoundTerminal.isEmpty()) {
//						if (OnlineSettlementService.class.equals(clearingProfile.getSettlementClass()) ||
//								PerTransactionSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()) ||
//								PerTransactionOnlineBillSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass())) {
							List<Message> tmpRes = (List<Message>) settlementService.postPrepareForSettlement(tmpTerminals, clearingProfile, settleUntilTime,
									onlyFanapAccount);
//							if (tmpRes != null)
//								postPrep.addAll(tmpRes);
//						}
						if (update)
							settlementService.updateToNowSettlementData(clearingProfile, firstRoundTerminal, accountUntilTime, settleUntilTime);
					}

				} catch (Exception e) {
					logger.error(e);
					GeneralDao.Instance.rollback();
//					GeneralDao.Instance.beginTransaction();
//					if (lockAcq)
//						SynchronizationService.release(clearingProfile, ClearingProfile.class);
//					GeneralDao.Instance.endTransaction();
					semaphore.release();
					throw e;
				}

			} else {
				nextRoundTerminal.clear();
				if (firstRound)
					firstRound = false;
			}

			GeneralDao.Instance.endTransaction();
			GeneralDao.Instance.beginTransaction();
		}
		
		GeneralDao.Instance.endTransaction();
		semaphore.release();

		// counter = 0;
		// termForQuery = new ArrayList<Long>();
		}
}
