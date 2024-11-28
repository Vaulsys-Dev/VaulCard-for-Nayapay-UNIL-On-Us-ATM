package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.TransactionFinancialProcessor;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ReportGenerator;
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

public class CyclePerTransactionSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(CyclePerTransactionSettlementServiceImpl.class);
	
	private CyclePerTransactionSettlementServiceImpl(){}
	
	public static final CyclePerTransactionSettlementServiceImpl Instance = new CyclePerTransactionSettlementServiceImpl();
	
	List<Integer> hours = new ArrayList<Integer>() {{
		add (new Integer(2)); 
		add (new Integer(4));
		add (new Integer(6));
		add (new Integer(8));
		add (new Integer(10));
		add (new Integer(12));
		add (new Integer(14));
		add (new Integer(16));
		add (new Integer(18));
		add (new Integer(20));
		add (new Integer(22));
		add (new Integer(24));
	}};
	
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
	public void account(ClearingProfile clearingProfile, DateTime accountUntilTime, DateTime settleUntilTime, Boolean update,
			Boolean waitForSyncObject, Boolean onlyFanapAccount, Boolean considerClearingProcessType) throws Exception {
		
//		GeneralDao.Instance.beginTransaction();
//		postPrepareForSettlement(null, clearingProfile, settleUntilTime, onlyFanapAccount);
//		GeneralDao.Instance.endTransaction();
		
		int accHour = accountUntilTime.getDayTime().getHour();
		int hour = 0;
		for (Integer i : hours) {
			if (i < accHour) {
				continue;
			} else {
				hour = i;
				break;
			}
		}
		
		settleUntilTime.setDayTime(new DayTime(hour - 1, 59, 59));
		
		super.account(clearingProfile, accountUntilTime, settleUntilTime, update, waitForSyncObject, onlyFanapAccount, considerClearingProcessType);
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
		
		List<Terminal> result = new ArrayList<Terminal>();
		
		List<POSTerminal> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, EPAYTerminal.class, clearingProfile);
		
		if (posTerminals != null && posTerminals.size() > 0)
			result.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			result.addAll(epayTerminals);
		return result;
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
		return "برخط فروشنده جدید";
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
			List<SettlementData> settlementDataList = new ArrayList<SettlementData>();
			try {
				
		    	if (terminals == null) {
		    		logger.debug("try to get settlementData for all terminals...");
		    		settlementDataList = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate, null);
		    		
		    	} else {
		    		logger.debug("try to get settlementData for some terminals...");
		    		settlementDataList = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate, terminals);
		    		
		    	}
		    	
		    	try {
		    		for (SettlementData settlementData : settlementDataList)
		    			if (settlementData.getReport() == null)
		    				ReportGenerator.generateSettlementDataReport(settlementData, settlementData.getSettlementTime());
		    	} catch (Exception e) {
		    		logger.error(e);
		    	}
				
//				ReportGenerator.generateSettlementDataReportWithoutState(terminals, clearingProfile, settleDate);
			} catch (Exception e) {
				logger.error("Exception in Generating Settlement Data Report " + e, e);
				throw e;
			}

			logger.info("Generating Final Settlement State Report...");
			try {
				generatePerTransactionDocumentSettlementData(settlementDataList, getSettlementTypeDesc(), settleDate);
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

	protected void generatePerTransactionDocumentSettlementData(List<SettlementData> notSettledSettlementData, String docDesc, DateTime settleDate) throws Exception {
		logger.debug("Try to issue for terminals with transaction");
//		List<SettlementData> notSettledSettlementData = new ArrayList<SettlementData>();
//		
//		if (terminals == null) {
//			notSettledSettlementData = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate, null);
//		} else {
//			notSettledSettlementData = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate, terminals);
////			notSettledSettlementData = AccountingService.findAllNotSettledOnlineSettlementDataUntilTime(terminals, clearingProfile, settleDate);
//		}
		
		if (notSettledSettlementData.size() != 0) {
			issueFanapSettlementDataReport(notSettledSettlementData, docDesc, settleDate);
		}
				
		return;
	}
	
	
	private void issueFanapSettlementDataReport(List<SettlementData> settlementDatas, String docDesc, DateTime settleDate) throws Exception {
//		return;
		ReportGenerator.issueFanapSettlementDataReport(settlementDatas, docDesc, settleDate);
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
