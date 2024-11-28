package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.settlement.ATMSettlementServiceImpl;
import vaulsys.clearing.settlement.CoreConfigDataManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.TerminalService;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.transaction.impl.ManuallyProcessdTransaction;
import vaulsys.user.User;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ReturnDocumentSettleInfo {

	private static final Logger logger = Logger.getLogger(ReturnDocumentSettleInfo.class); 
	
	public static void main(String[] args) {
		if (args.length == 1) {
			
			try { 
				long transactionId = Long.parseLong(args[0]);
				
				issueATMReturnedDocument(transactionId);
			} catch (Exception e) {
				logger.error("Invalid input parameters!!!" + e, e);
			}
		} else {
			logger.error("Invalid input parameters!!!");
		}
		
	}
	public static void issueATMReturnedDocument(long transactionId) {

		GeneralDao.Instance.beginTransaction();
		Transaction transaction = null;
		String accountNumber = "";
		SettlementInfo sourceSettleInfo = null;
		try {

			GlobalContext.getInstance().startup();
			ProcessContext.get().init();
			GlobalContext.getInstance().getMyInstitution();


			transaction = GeneralDao.Instance.load(Transaction.class, transactionId);
			
			if (transaction == null) {
				logger.error("transaction == null, trx: " + transactionId);
				GeneralDao.Instance.rollback();
				return;
			}
			
			sourceSettleInfo = transaction.getSourceSettleInfo();
			
			Ifx inIfx = transaction.getIncomingIfx();
			String myBin = ""+GlobalContext.getInstance().getMyInstitution().getBin();
			
			if (inIfx.getRequest()) {
				transaction = TransactionService.findResponseTrx(transaction.getLifeCycleId(), transaction);
				transactionId = transaction.getId();
				logger.info("incoming trx is request, rs trx is : " + transactionId);
				
			}
			
			if (!inIfx.getBankId().equals(myBin)) {
				logger.error("!inIfx.getBankId().equals(myBin)");
				GeneralDao.Instance.rollback();
				return;
			}
			
			if (!inIfx.getTrnType().equals(TrnType.WITHDRAWAL)) {
				logger.error("!inIfx.getTrnType().equals(TrnType.WITHDRAWAL)");
				GeneralDao.Instance.rollback();
				return;
			}
			
			if (!sourceSettleInfo.getAccountingState().equals(AccountingState.NEED_TO_BE_RETURNED)) {
				logger.error("!transaction.getSourceSettleInfo().getAccountingState().equals(AccountingState.NEED_TO_BE_RETURNED)");
				GeneralDao.Instance.rollback();
				return;
			}
			
			if (sourceSettleInfo.getSettlementData() == null || !Util.hasText(sourceSettleInfo.getSettlementData().getDocumentNumber())) {
				logger.error("transaction.getSourceSettleInfo().getSettlementData() == null || !Util.hasText(transaction.getSourceSettleInfo().getSettlementData().getDocumentNumber())");
				GeneralDao.Instance.rollback();
				return;
			}
			
			ClearingInfo destClrInfo = transaction.getDestinationClearingInfo();
			if (!destClrInfo.getClearingState().equals(ClearingState.NOT_CLEARED) &&
					!destClrInfo.getClearingState().equals(ClearingState.CLEARED) &&
					!destClrInfo.getClearingState().equals(ClearingState.PARTIALLY_CLEARED) &&
					!destClrInfo.getClearingState().equals(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED)) {
				logger.error("destClrState not in (NOT_CLEARED, CLEARED, PARTIALLY_CLEARED, NOT_NOTE_SUCCESSFULLY_DISPENSED)");
				GeneralDao.Instance.rollback();
				return;
			}
			
			if (inIfx.getDestBankId() == myBin) {
				accountNumber = CoreConfigDataManager.getValue(CoreConfigDataManager.OnUsATMReturnedDocumentAccount);
			} else {
				accountNumber = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIDisagreementAccount);
			}
			
		} catch (Exception e) {
			GeneralDao.Instance.rollback();
			return;
		}
		
		try {
			String docDesc = "برگشت تراکنش " + transactionId;
			
			List<SettlementInfo> infos = new ArrayList<SettlementInfo>();
			infos.add(sourceSettleInfo);
			ATMSettlementServiceImpl.Instance.generateATMReturnedReport(infos, accountNumber, docDesc);
			
			DateTime now = DateTime.now();
			
			User user = GlobalContext.getInstance().getSwitchUser();
			if (transaction.getManuallyProcessdTransactions() != null && transaction.getManuallyProcessdTransactions().size() > 0) {
				ManuallyProcessdTransaction mpt = new ManuallyProcessdTransaction(transaction, transaction.getSourceClearingInfo(), sourceSettleInfo, transaction.getSourceClearingInfo(), new SettlementInfo(sourceSettleInfo.getSettledState(), AccountingState.RETURNED, now, transaction), SourceDestination.SOURCE, user);
				GeneralDao.Instance.save(mpt);
			} else if (transaction.getFirstTransaction().getManuallyProcessdTransactions() != null && transaction.getFirstTransaction().getManuallyProcessdTransactions().size() > 0) {
				ManuallyProcessdTransaction mpt = new ManuallyProcessdTransaction(transaction.getFirstTransaction(), transaction.getSourceClearingInfo(), sourceSettleInfo, transaction.getSourceClearingInfo(), new SettlementInfo(sourceSettleInfo.getSettledState(), AccountingState.RETURNED, now, transaction.getFirstTransaction()), SourceDestination.SOURCE, user);
				GeneralDao.Instance.save(mpt);
			}
			
			sourceSettleInfo.setAccountingState(AccountingState.RETURNED);
			sourceSettleInfo.setAccountingDate(now);
			GeneralDao.Instance.saveOrUpdate(sourceSettleInfo);
			
			GeneralDao.Instance.endTransaction();
			
		} catch (Exception e) {
			GeneralDao.Instance.rollback();
			return;
		}
	}
}
