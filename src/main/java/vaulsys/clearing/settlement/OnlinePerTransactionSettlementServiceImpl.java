package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.*;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.*;
import vaulsys.util.ConfigUtil;
import vaulsys.util.SwitchRuntimeException;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class OnlinePerTransactionSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(OnlinePerTransactionSettlementServiceImpl.class);
	
	private OnlinePerTransactionSettlementServiceImpl(){}
	
	public static final OnlinePerTransactionSettlementServiceImpl Instance = new OnlinePerTransactionSettlementServiceImpl();
	
	@Override
	public void account(ClearingProfile clearingProfile, DateTime accountUntilTime, DateTime settleUntilTime, Boolean update,
			Boolean waitForSyncObject, Boolean onlyFanapAccount, Boolean considerClearingProcessType) throws Exception {
		
		GeneralDao.Instance.beginTransaction();
		postPrepareForSettlement(null, clearingProfile, settleUntilTime, onlyFanapAccount);
		GeneralDao.Instance.endTransaction();
		
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

    public void deleteFromSettlementRecord(ClearingProfile clearingProfile, List terminals){
        logger.info("started deleteFromSettlementRecord");
        GeneralDao.Instance.beginTransaction();
        try{
            Map< String, Object> params = new HashMap<String, Object>();
            String query = "delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where type in (:srType) and clr_prof=:clr_prof and terminal in (:terminals)";
//            params.put("clr_prof", clearingProfile.getId());
//            params.put("terminals", terminals);
//            params.put("srType", new ArrayList<Integer>() {{
//                add(new Integer(SettlementRecordType.ONLYFORFORM1.getType()));
//                add(new Integer(SettlementRecordType.THIRDPARTHY.getType()));
//            }});
//            GeneralDao.Instance.executeSqlUpdate(query, params);


            params = new HashMap<String, Object>();
            query = "delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in ( select t.id from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_transaxion  t " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record sr on sr.trx=t.id " +
                    "left outer join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_flg_clearing fc on fc.id=t.src_clr_flg " +
                    // "inner join trx_flg_settlement fs on fs.id=t.src_stl_flg " +
                    "where sr.type in (:srType) and sr.clr_prof=:clr_prof and (fc.clr_state is null or fc.clr_state = :clr_state) and sr.terminal in (:terminals))";

            params.put("terminals", terminals);
            params.put("clr_state", ClearingState.CLEARED.getState());
            params.put("srType", new ArrayList<Integer>() {{
                add(new Integer(SettlementRecordType.ONLYFORFORM1.getType()));
                add(new Integer(SettlementRecordType.THIRDPARTHY.getType()));
            }});
            params.put("clr_prof", clearingProfile.getId());
            GeneralDao.Instance.executeSqlUpdate(query, params);

            /*if(li != null && li.size()>0){
                logger.debug("deleteFromSettlementRecord ONLYFORFORM1, trxs: "+ ToStringBuilder.reflectionToString(li.toArray(), ToStringStyle.MULTI_LINE_STYLE));
                params = new HashMap<String, Object>();
                params.put("trx", li);
                GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in (:trx)", params);
            }*/

            params = new HashMap<String, Object>();
            query = "delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in ( select t.id from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_transaxion  t " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record sr on sr.trx=t.id " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_flg_clearing fc on fc.id=t.src_clr_flg " +
                    //   "inner join trx_flg_settlement fs on fs.id=t.src_stl_flg " +
                    "where sr.clr_prof=:clr_prof and fc.clr_state = :clr_state and sr.terminal in (:terminals))";

            params.put("terminals", terminals);
            params.put("clr_state", ClearingState.RECONCILED.getState());
            //params.put("srType", SettlementRecordType.ONLYFORFORM1);
            params.put("clr_prof", clearingProfile.getId());
            GeneralDao.Instance.executeSqlUpdate(query, params);

            /*if(li != null && li.size()>0){
                logger.debug("deleteFromSettlementRecord RECONCILED, trxs: "+ ToStringBuilder.reflectionToString(li.toArray(), ToStringStyle.MULTI_LINE_STYLE));
                params = new HashMap<String, Object>();
                params.put("trx", li);
                GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in (:trx)", params);
            }*/
            params = new HashMap<String, Object>();
            query = "delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in ( select t.id from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_transaxion  t " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record sr on sr.trx=t.id " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_lifecycle li on t.lifecycle=li.id " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_flg_clearing fc on fc.id=t.src_clr_flg " +
                    //    "inner join trx_flg_settlement fs on fs.id=t.src_stl_flg " +
                    "where li.isfullyreveresed_state = 3 and sr.clr_prof=:clr_prof and fc.clr_state in (:clr_state) and sr.terminal in (:terminals))";

            params.put("terminals", terminals);
            params.put("clr_state", new ArrayList<Integer>() {{
                add(new Integer(ClearingState.DISAGREEMENT.getState()));
                add(new Integer(ClearingState.NO_CARD_REJECTED.getState()));
            }});
            //params.put("srType", SettlementRecordType.ONLYFORFORM1);
            params.put("clr_prof", clearingProfile.getId());
            GeneralDao.Instance.executeSqlUpdate(query, params);

            /*if(li != null && li.size()>0){
                logger.debug("deleteFromSettlementRecord DISAGREEMENT, trxs: "+ ToStringBuilder.reflectionToString(li.toArray(), ToStringStyle.MULTI_LINE_STYLE));
                params = new HashMap<String, Object>();
                params.put("trx", li);
                GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in (:trx)", params);
            }*/

            params = new HashMap<String, Object>();
            query = "delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in ( select t.id from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_transaxion  t " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record sr on sr.trx=t.id " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_flg_clearing fc on fc.id=t.src_clr_flg " +
                    "inner join " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".trx_flg_settlement fs on fs.id=t.src_stl_flg " +
                    "where sr.type in (:srType) and sr.clr_prof=:clr_prof and sr.terminal in (:terminals) and ((fs.acc_state=:acc_state1 and fc.clr_state = :clr_state) or (fs.acc_state = :acc_state)) )";

            params.put("terminals", terminals);
            params.put("clr_state", ClearingState.CLEARED.getState());
            params.put("acc_state1", AccountingState.COUNTED.getState());
            params.put("acc_state", AccountingState.NO_NEED_TO_BE_COUNTED.getState());
            params.put("srType", new ArrayList<Integer>() {{
                add(new Integer(SettlementRecordType.SETTLEMENTRECORD.getType()));
                add(new Integer(SettlementRecordType.ONLYFORFORM1.getType()));
            }});
            params.put("clr_prof", clearingProfile.getId());
            GeneralDao.Instance.executeSqlUpdate(query, params);

            /*if(li != null && li.size()>0){
                logger.debug("deleteFromSettlementRecord CLEARED, COUNTED, NO_NEED_TO_BE_COUNTED trxs: "+ ToStringBuilder.reflectionToString(li.toArray(), ToStringStyle.MULTI_LINE_STYLE));
                params = new HashMap<String, Object>();
                params.put("trx", li);
                GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA) + ".settlement_record where trx in (:trx)", params);
            }*/

        }catch (Exception e){
            logger.error("error in deleteFromSettlementRecord:" + e, e);
        } finally {
            GeneralDao.Instance.endTransaction();
        }
        logger.info("ended deleteFromSettlementRecord");
    }

}
