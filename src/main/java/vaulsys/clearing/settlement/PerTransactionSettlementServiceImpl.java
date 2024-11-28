package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.TransactionFinancialProcessor;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.customer.Core;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.SwitchRuntimeException;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class PerTransactionSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(PerTransactionSettlementServiceImpl.class);
	
	private PerTransactionSettlementServiceImpl(){}
	
	public static final PerTransactionSettlementServiceImpl Instance = new PerTransactionSettlementServiceImpl();
	
	@Override
	protected void doProcess(ClearingProfile clearingProfile, DateTime settleUntilTime, Terminal terminal,
			SettlementDataType type, List<Ifx> desiredMsgs, Boolean settleTime) {
		try {
			if (desiredMsgs != null && desiredMsgs.size() > 0) {
				TransactionFinancialProcessor.doProcessPerTransaction(terminal, clearingProfile, type, desiredMsgs, settleUntilTime, settleTime);
				GeneralDao.Instance.endTransaction();
				GeneralDao.Instance.beginTransaction();
				GeneralDao.Instance.refresh(clearingProfile);
			}
		} catch (Exception e) {
			logger.error("Exception in doProcess of terminal: " + terminal.getCode() + e, e);
		}
	}
	

	
	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminals(POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminals(EPAYTerminal.class, clearingProfile);
		
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
		
		return terminals;
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, EPAYTerminal.class, clearingProfile);
		
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
		return terminals;
	}
	
	@Override
	protected List<Terminal> findAllTerminalsBasedOnSettlementRecord(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTimeBasedOnSettlementRecord(terminals, termCodes, POSTerminal.class, clearingProfile);
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		return terminals;
	}
	
	@Override
	List<String> findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
		List<String> terminals = new ArrayList<String>();
		Integer guaranteePeriod = 0;
		if(justToday){
			guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
		}else{
			guaranteePeriod = clearingProfile.getSettleGuaranteeDay();			
		}
		List<String> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(POSTerminal.class, accountUntilTime, justToday, guaranteePeriod);
//		List<String> epayTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(EPAYTerminal.class, accountUntilTime, justToday, clearingProfile.getSettleGuaranteeDay());
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
//		if (epayTerminals != null && epayTerminals.size() > 0)
//			terminals.addAll(epayTerminals);
		return terminals;
	}

	@Override
	protected List findDesiredTerminalCodesBasedOnSettlementRecord(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
		Integer guaranteePeriod = 0;
		if(justToday){
			guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
		}else{
			guaranteePeriod = clearingProfile.getSettleGuaranteeDay();			
		}
		List<String> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTimeBasedOnSettlementRecord(POSTerminal.class, accountUntilTime, justToday, guaranteePeriod, clearingProfile);
		return posTerminals;
	}

	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
	}

	@Override
	public String getSettlementTypeDesc() {
		return "تراکنشی";
	}

	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("source");
		return result;
	}

	@Override
	boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return FinancialEntityRole.SHOP.equals(entity.getRole()) ||
				FinancialEntityRole.MERCHANT.equals(entity.getRole());
	}
	
	@Override
	protected Object postPrepareForSettlement(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate, Boolean onlyFanapAccount) {
		try {
			logger.info("Generating Settlement Data Report...");
			try {
				ReportGenerator.generateSettlementDataReportWithoutState(terminals, clearingProfile, settleDate);
			} catch (Exception e) {
				logger.error("Exception in Generating Settlement Data Report " + e, e);
				throw e;
			}

			logger.info("Generating Final Settlement State Report...");
			try {
				generatePerTransactionDocumentSettlementData(terminals, clearingProfile, getSettlementTypeDesc(), settleDate, onlyFanapAccount);
//				generatePerTransactionDocumentForAllTerminals(clearingProfile, getSettlementTypeDesc(), settleDate);
			} catch (Exception e) {
				logger.error("Exception in Generating Final Settlement State Report  " + e, e);
				throw e;
			}
			
		} catch (Exception e) {
			logger.error(e);
			throw new SwitchRuntimeException(e);
		}
		return null;
	}
	
	protected void generatePerTransactionDocumentForAllTerminals(ClearingProfile clearingProfile, String docDesc,DateTime settleDate) throws Exception {
		logger.debug("Try to issue for all terminal");
		List<SettlementData> stlDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate);
		if (stlDatas.size() != 0) {
			issueFanapSettlementDataReport(stlDatas, docDesc, settleDate);
		}
	}
	
	@Override
	protected void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc,DateTime settleDate) throws Exception {
		generatePerTransactionDocumentForAllTerminals(clearingProfile, docDesc, settleDate);
//		List<SettlementData> stlDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate);
//		if (stlDatas.size() != 0) {
//			issueFanapSettlementDataReport(stlDatas, docDesc, settleDate);
//		}
		
		List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, /*settleDate,*/ null);
    	for (SettlementState settlementState: settlementStates) {
	    	if (settlementState != null /*&& AccountingService.isAllSettlementDataSettled(settlementState)*/) {
	    		settlementState.setState(SettlementStateType.AUTOSETTLED);
	    		DateTime now = DateTime.now();
				settlementState.setSettlementFileCreationDate(now );
	    		settlementState.setSettlementDate(now);
//	    		settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
	    		settlementState.setSettlingUser(ProcessContext.get().getSwitchUser());
	    		GeneralDao.Instance.saveOrUpdate(settlementState);
	    	}
    	}
    	ReportGenerator.generateDocumentSettlementState(clearingProfile, docDesc, /*settleDate,*/ true);
	}

	protected void generatePerTransactionDocumentSettlementData(List<Terminal> terminals, ClearingProfile clearingProfile, String docDesc,DateTime settleDate, Boolean onlyFanapAccount) throws Exception {
		logger.debug("Try to issue for terminals with transaction");
		List<SettlementData> notSettledSettlementData = AccountingService.findAllNotSettledOnlineSettlementDataUntilTime(terminals, clearingProfile, settleDate);
//		List<SettlementData> fanapSettlementData = new ArrayList<SettlementData>();
//		for (SettlementData settlementData : notSettledSettlementData) {
//			if (settlementData != null) {
//				FinancialEntity entity = settlementData.getFinancialEntity();
//				if (Core.FANAP_CORE.equals(entity.getOwnOrParentAccount().getCore())) {
//					Object[] transactions = settlementData.getTransactions().toArray();
//					
//					if (transactions.length != 1) {
//						logger.error("settlementData(" + settlementData.getId() + ") hasn't One transaction!");
//						return;
//					}
//					Transaction transaction = (Transaction) transactions[0];
//					if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturned()) &&
//							LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed())){
//						logger.error("transaction: " + transaction.getId() + " has been returned, so don't issue document!");
//						settlementData.setDocumentNumber("---");
//					} else {
//						fanapSettlementData.add(settlementData);
//					}
//				} else {
//					logger.error("account of entity: " + entity.getId() + " is not in FANAP!!!");
//				}
//			}
//		}
		
		if (notSettledSettlementData.size() != 0) {
			issueFanapSettlementDataReport(notSettledSettlementData, docDesc, settleDate);
		}
				
		return;
	}
	
	
	private void issueFanapSettlementDataReport(List<SettlementData> settlementDatas, String docDesc, DateTime settleDate) throws Exception {
		List<SettlementData> fanapSettlementData = new ArrayList<SettlementData>();
		for (SettlementData settlementData : settlementDatas) {
			if (settlementData != null) {
				FinancialEntity entity = settlementData.getFinancialEntity();
				if (Core.FANAP_CORE.equals(entity.getOwnOrParentAccount().getCore())) {
					Object[] transactions = settlementData.getTransactions().toArray();
					
					if (transactions.length != 1) {
						logger.error("settlementData(" + settlementData.getId() + ") hasn't One transaction!");
						return;
					}
					Transaction transaction = (Transaction) transactions[0];
					if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturned()) &&
							LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed())){
						logger.error("transaction: " + transaction.getId() + " has been returned, so don't issue document!");
						settlementData.setDocumentNumber("---");
					} else {
						fanapSettlementData.add(settlementData);
					}
				} else {
					logger.error("account of entity: " + entity.getId() + " is not in FANAP!!!");
				}
			}
		}
		
		if (fanapSettlementData.size() != 0) {
//			issueFanapSettlementDataReport(fanapSettlementData, docDesc, settleDate);
			ReportGenerator.issueFanapSettlementDataReport(fanapSettlementData, docDesc, settleDate);
		}
	}

	@Override
	protected List<Ifx> getResultCriteria(String query, Map<String, Object> Params,int firstResult, int maxResults, ClearingProfile clearingProfile) {
		List<Ifx> ifxList = super.getResultCriteria(query, Params,firstResult,maxResults, clearingProfile);
		List<Ifx> deletedItems = new ArrayList<Ifx>();
		for (Ifx ifx : ifxList) {
			if (!LifeCycleStatus.NOTHING.equals(ifx.getTransaction().getLifeCycle().getIsReturned())
				&& !TrnType.RETURN.equals(ifx.getTrnType())
				&& !TransactionService.canBeSettledReturnedTransaction(ifx)){
					deletedItems.add(ifx);
			}
			if (TrnType.RETURN.equals(ifx.getTrnType()) 
				&& !ClearingState.CLEARED.equals(ifx.getTransaction().getSourceClearingInfo().getClearingState())){
				deletedItems.add(ifx);
			}
			
		}
		if (!deletedItems.isEmpty()) {
			ifxList.removeAll(deletedItems);
			String ids = "[";
			List<Transaction> trxList = new ArrayList<Transaction>();
			for (Ifx deletedIfx : deletedItems) {
				ids += deletedIfx.getId() + " ,";
				trxList.add(deletedIfx.getTransaction());
			}
			
			logger.info(trxList.size() + " trx's delete from settlement record without settling!!!");
			AccountingService.removeSettlementRecord(trxList, clearingProfile, null);
			
			ids = ids.substring(0, ids.length() - 2) + "]";
			logger.warn(deletedItems.size() + " ifx's have tried to be returned so they aren't settled! " + ids );
		}
		return ifxList;
	}

}
