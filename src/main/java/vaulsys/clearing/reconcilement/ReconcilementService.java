package vaulsys.clearing.reconcilement;

import vaulsys.persistence.GeneralDao;

import org.apache.log4j.Logger;

public class ReconcilementService {

	Logger logger = Logger.getLogger(this.getClass());

	protected GeneralDao generalDao;
	
//	public void reconcile(DateTime settleUntilTime) {
//		reconcile(null, null, settleUntilTime);
//	}
//	
//	public void reconcile(List<Long> msgIds, List<Message> messages, DateTime settleUntilTime) {
//		if (settleUntilTime == null)
//			settleUntilTime = DateTime.now();
//		account(msgIds, messages, settleUntilTime, settleUntilTime);
//
//		DateTime settleDate = settleUntilTime;
//		
//		try {
//			logger.info("Generating Settlement Data Report...");
//			try {
//				ReportGenerator.generateSettlementDataReport(null, null, settleDate);
//			} catch (Exception e) {
//				logger.error("Exception in Generating Settlement Data Report " + e);
//			}
//
//			GeneralDao.Instance.beginTransaction();
//			
//			logger.info("Generating Settlement State Report...");
//			try {
//				ReportGenerator.generateSettlementStateAndReport(null, null, settleDate, getSettlementTypeDesc());
//			} catch (Exception e) {
//				logger
//						.error("Exception in Generating Settlement State Report, must be rollback beacuase incorrect SettlementState created! "
//								+ e);
//				GeneralDao.Instance.rollback();
//				return;
//			}
//			
//			GeneralDao.Instance.endTransaction();
//			GeneralDao.Instance.beginTransaction();
//
//			logger.info("Generating Desired Terminal Settlement Report...");
//			try {
////				generateDesiredSettlementReports(null, settleDate);
//			} catch (Exception e) {
//				logger.error("Exception in Generating Desired Terminal Settlement Report  " + e);
//			}
//
//			logger.info("Generating Final Settlement State Report...");
//			try {
//				ReportGenerator.generateDocumentSettlementState(null, getSettlementTypeDesc(), /*settleDate,*/ false);
//			} catch (Exception e) {
//				logger.error("Exception in Generating Final Settlement State Report  " + e);
//			}
//
//			logger.info("Puting Settle Flag...");
////			settleTransactions(null, settleDate);
//
//			logger.info("End of Put Settle Flag.");
//
//		} catch (Exception e) {
//			logger.error("Encounter with an exception: "+ e);
//			GeneralDao.Instance.rollback();
//			return;
//		}
//		GeneralDao.Instance.endTransaction();
//	}
//	
//	private String getSettlementTypeDesc() {
//		return "مغايرت";
//	}
//
//	synchronized public void account(DateTime accountUntilTime, DateTime settleUntilTime) {
//		account(null, null, accountUntilTime, settleUntilTime);
//	}
//	
//	synchronized public void account(List<Long> msgIds, List<Message> messages, DateTime accountUntilTime, DateTime settleUntilTime) {
//		logger.info("Starting Terminal Accounting...");
////		TransactionManager manager = new TransactionManager(SwitchApplication.get().getHibernateSessionFactory());
////		manager.beginTransaction();
//		
//		for (Long id: msgIds) {
//			Message msg = GeneralDao.Instance.load(Message.class, id);
//			messages.add(msg);
//		}
//		
//		if (messages != null && messages.size() > 0) {
//			try {
//				logger.info("message size: " + messages.size());
//				
//				logger.info("Preparing Settlement...");
//				prepareForSettlement(messages, accountUntilTime, settleUntilTime);
//				
//			} catch (Exception e) {
//				logger.error("Encounter with an exception: "+e);
////				manager.rollback();
//				return;
//			}
////			manager.endTransaction2();
//		}
//		logger.info("Ending Terminal Accounting...");
//	}
//
//	public void prepareForSettlement(List<Message> messages, DateTime accountUntilTime, DateTime settleUntilTime) throws Exception {
//		
//		if (messages != null)
//			TransactionFinancialProcessor.returnProcess(messages, settleUntilTime, SettlementDataType.RECONCILE);
//		
//		else {
//			List<ClearingState> notToBeClrState = new ArrayList<ClearingState>();
//			notToBeClrState.add(ClearingState.DISPUTE);
//			
//			List<Message> desiredMsgs = TransactionService.getDesiredMessages(notToBeClrState, null, null, null, null, null, settleUntilTime, null, true);
//			try {
//				if (desiredMsgs != null && desiredMsgs.size() > 0)
//					TransactionFinancialProcessor.returnProcess(desiredMsgs, settleUntilTime, SettlementDataType.RECONCILE);
//			} catch (Exception e) {
//				logger.error("Exception in returnProcess! " , e);
//			}
//		}
//	}

//	protected List<Message> getDesiredMessages(List<ClearingState> toBeClrState, List<TrnType> toBeClrTrx, List<TerminalType> termTypes,
//			AccountingState accountingState, SettledState settledState, Terminal terminal, DateTime realTimeForAccounting) {
//		return TransactionService.getDesiredMessages(toBeClrState, toBeClrTrx, termTypes, accountingState, settledState, 
//				terminal, realTimeForAccounting, null, true);
//	}
}