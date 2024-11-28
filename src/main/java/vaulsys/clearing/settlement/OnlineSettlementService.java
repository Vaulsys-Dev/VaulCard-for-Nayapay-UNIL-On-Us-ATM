package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.*;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.customer.Core;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.SwitchRuntimeException;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.*;

import org.apache.log4j.Logger;

public class OnlineSettlementService extends SettlementService {
	private static final Logger logger = Logger.getLogger(OnlineSettlementService.class);
	
	private OnlineSettlementService(){}
	
	public static final OnlineSettlementService Instance = new OnlineSettlementService();
	
	
	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminals(POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminals(EPAYTerminal.class, clearingProfile);
//		List<ATMTerminal> atmTerminals = TerminalService.findAllTerminals(ATMTerminal.class, clearingProfile);
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
//		if (atmTerminals != null && atmTerminals.size() > 0)
//			terminals.addAll(atmTerminals);
		
		return terminals;
	}

	@Override
	List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, POSTerminal.class, clearingProfile);
		List<EPAYTerminal> epayTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, EPAYTerminal.class, clearingProfile);
//		List<ATMTerminal> atmTerminals = TerminalService.findAllTerminals(ATMTerminal.class, clearingProfile);
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
		if (epayTerminals != null && epayTerminals.size() > 0)
			terminals.addAll(epayTerminals);
//		if (atmTerminals != null && atmTerminals.size() > 0)
//			terminals.addAll(atmTerminals);
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
		return "برخط";
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
		List<Message> messages = null;
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
				messages = generateOnlineDocumentSettlementData(terminals, clearingProfile, getSettlementTypeDesc(), settleDate, onlyFanapAccount);
			} catch (Exception e) {
				logger.error("Exception in Generating Final Settlement State Report  " + e, e);
				throw e;
			}
			
		} catch (Exception e) {
			logger.error(e);
			throw new SwitchRuntimeException(e);
		}
		return messages;
	}
	
	
	@Override
	protected void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc,DateTime settleDate) throws Exception {
		List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, /*settleDate,*/ null);
    	for (SettlementState settlementState: settlementStates) {
	    	if (settlementState != null && AccountingService.isAllSettlementDataSettled(settlementState)) {
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

	protected List<Message> generateOnlineDocumentSettlementData(List<Terminal> terminals, ClearingProfile clearingProfile, String docDesc,DateTime settleDate, Boolean onlyFanapAccount) throws Exception {
		List<SettlementData> notSettledSettlementData = AccountingService.findAllNotSettledOnlineSettlementDataUntilTime(terminals, clearingProfile, settleDate);
		List<SettlementData> fanapSettlementData = new ArrayList<SettlementData>();
		List<SettlementData> neginSettlementData = new ArrayList<SettlementData>();
		List<Message> messages = new ArrayList<Message>();
		for (SettlementData settlementData : notSettledSettlementData) {
			if (settlementData != null) {
				FinancialEntity entity = settlementData.getFinancialEntity();
				if (Core.FANAP_CORE.equals(entity.getOwnOrParentAccount().getCore())) {
					fanapSettlementData.add(settlementData);
				} else {
					neginSettlementData.add(settlementData);
				}
			}
		}
		
		if (fanapSettlementData.size() != 0) {
			issueFanapSettlementDataReport(fanapSettlementData, docDesc, settleDate);
		}
				
		if (neginSettlementData.size() != 0) {
			if (!onlyFanapAccount) {
				for (SettlementData settlementData : neginSettlementData) {
					try {
						GeneralDao.Instance.refresh(settlementData);
						List<Message> list = generateFinalNeginSettlementDataReport(settlementData, docDesc, settleDate);
						if (list != null)
							messages.addAll(list);
					} catch (Exception e) {
						logger.error(e, e);
					}
				}
			}
		}
//			if (settlementData != null) {
//				FinancialEntity entity = settlementData.getFinancialEntity();
//				Terminal terminal = settlementData.getTerminal();
//				logger.debug("entity: "+entity.getCode()+ " terminal: "+terminal);
//				
//				Account account = entity.getOwnOrParentAccount();
//				try {
//					if (Core.FANAP_CORE.equals(account.getCore()))
//						issueFanapSettlementDataReport(settlementData, docDesc, settleDate);
//					else if (!onlyFanapAccount){
//						List<Message> list = generateFinalNeginSettlementDataReport(settlementData, docDesc, settleDate);
//						if (list!= null)
//							messages.addAll(list);
//					}
//				} catch (Exception e) {
//					logger.error(e, e);
//				}
//			}
//		}
		if (!messages.isEmpty())
			return messages;
		
		return null;
	}
	
	
	private void issueFanapSettlementDataReport(List<SettlementData> settlementData, String docDesc, DateTime settleDate) throws Exception {
		ReportGenerator.issueFanapSettlementDataReport(settlementData, docDesc, settleDate);
	}

	private List<Message> generateFinalNeginSettlementDataReport(SettlementData settlementData, String docDesc,
			DateTime settleDate) {
		List<Message> messages = new ArrayList<Message>();
		if (settlementData == null)
			return null;

		if (((Long) settlementData.getTotalSettlementAmount()).equals(0L)
				|| Util.hasText(settlementData.getDocumentNumber())){
			AccountingService.settleSettlementData(settlementData);
			return null;
		}
		
		logger.debug("settlementData: " + settlementData.getId() + " with amount "
				+ settlementData.getTotalSettlementAmount());
		FinancialEntity entity = settlementData.getFinancialEntity();
		Terminal terminal = settlementData.getTerminal();
		logger.debug("entity: " + entity.getId());
		if (terminal != null)
			logger.debug("terminal: " + terminal.getId());

		if (!isDesiredOwnerForPreprocessing(entity))
			return null;

		ScheduleMessage message = SchedulerService.createSettlementScheduleMsg(settlementData, ISOResponseCodes.APPROVED);
		if (message != null) {
			Transaction transaction = message.getTransaction();
			SettlementInfo settleInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NO_NEED_TO_BE_COUNTED, DateTime.now(), transaction);
			transaction.setSourceSettleInfo(settleInfo);
//			transaction.getFirstTransaction().setDestinationSettleInfo(settleInfo);
			settleInfo.setTransaction(transaction);
			settleInfo.setSettlementData(settlementData);
			getGeneralDao().saveOrUpdate(settleInfo);
			getGeneralDao().saveOrUpdate(transaction);
			
			messages.add(message);
		}
		return messages;
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

	public void generatedAndPutNeginSettlement(Long id){
		GeneralDao.Instance.beginTransaction();
		List<Message> list = null;
		ProcessContext.get().init();
		try {
			SettlementData settlementData = GeneralDao.Instance.load(SettlementData.class, id);
			FinancialEntity entity = settlementData.getFinancialEntity();
			if (settlementData.getClearingProfile() != null && 
					OnlineSettlementService.class.equals(settlementData.getClearingProfile().getSettlementClass()) &&
					settlementData.getDocumentNumber() == null) {
				String docDesc = getSettlementTypeDesc();
				if (Core.FANAP_CORE.equals(entity.getOwnOrParentAccount().getCore())) {
					try {
						issueFanapSettlementDataReport(Arrays.asList(settlementData), docDesc, DateTime.now());
					} catch (Exception e) {
						logger.error("Error in issueFanapSettlementDataReport", e);
					}
				} else {
					list = generateFinalNeginSettlementDataReport(settlementData, docDesc, DateTime.now());
				}
			} else {
				logger.debug("else OnlineSettlementService.class.equals(settlementData.getClearingProfile().getSettlementClass()) && settlementData.getDocumentNumber() == null");
			}
		} catch (Exception e) {
			logger.error("Error in issueFanapSettlementDataReport", e);
		} finally {
			GeneralDao.Instance.endTransaction();
		}
		
		if(list != null && !list.isEmpty()){
			for (Message m : list) {
				logger.debug("Adding msg " + m + " type:" + ((ScheduleMessage) m).getMessageType());
			}
			MessageManager.getInstance().putRequests(list);
		}

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
