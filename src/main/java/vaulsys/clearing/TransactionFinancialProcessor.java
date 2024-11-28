package vaulsys.clearing;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.fee.FeeService;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.batch.IfxSettlement;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.NotUsed;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class TransactionFinancialProcessor {
    private static Logger logger = Logger.getLogger(TransactionFinancialProcessor.class);

    public static void doProcess(Terminal terminal, ClearingProfile clearingProfile, SettlementDataType type, List<Ifx> ifxList,
			DateTime stlDate, Boolean settleTime) throws Exception {
		List<Transaction> transactions = new ArrayList<Transaction>();
		try {
			doProcessor(terminal, clearingProfile, type, ifxList, transactions, stlDate);
		} catch (Exception e) {
			logger.error(e,e);
		} finally {
			applyDesiredFlag(terminal, transactions, AccountingState.COUNTED, clearingProfile, ClearingService.getSettledStateAfterAccounting(clearingProfile, settleTime));
			AccountingService.removeSettlementRecord(transactions, clearingProfile, null);
		}
	}
    
    public static void doProcessPerTransaction(Terminal terminal, ClearingProfile clearingProfile, SettlementDataType type, List<Ifx> ifxList,
    		DateTime stlDate, Boolean settleTime) throws Exception {
    	List<Transaction> transactions = new ArrayList<Transaction>();
    	try {
    		doProcessorPerTransaction(terminal, clearingProfile, type, ifxList, transactions, stlDate);
    	} catch (Exception e) {
    		logger.error(e,e);
    	} finally {
    		applyDesiredFlag(terminal, transactions, AccountingState.COUNTED, clearingProfile, ClearingService.getSettledStateAfterAccounting(clearingProfile, settleTime));
    		AccountingService.removeSettlementRecord(transactions, clearingProfile, null);
    	}
    }

    public static void doProcessNew(Terminal terminal, ClearingProfile clearingProfile, SettlementDataType type, 
    		List<IfxSettlement> ifxList, DateTime stlDate, Boolean settleTime) throws Exception {
		List<Transaction> transactions = new ArrayList<Transaction>();
		try {
			doProcessorNew(terminal, clearingProfile, type, ifxList, transactions, stlDate);
		} catch (Exception e) {
			logger.error(e,e);
		} finally {
			applyDesiredFlag(terminal, transactions, AccountingState.COUNTED, clearingProfile, ClearingService.getSettledStateAfterAccounting(clearingProfile, settleTime));
			AccountingService.removeSettlementRecord(transactions, clearingProfile, null);
		}
	}

    public static void removeProcess(Terminal terminal, ClearingProfile clearingProfile, List<Transaction> transactions, DateTime stlDate) throws Exception {
    	if(transactions != null && transactions.size() > 0){
    		logger.debug("Try to run removeProcess for terminal: "+terminal);
    		for(Transaction t:transactions){
    			logger.debug("removeProcess trx: "+t.getId() +" "+t.getDebugTag());
    		}
    	}
    	
//    	List<Transaction> notBeReturned =
    	removeProcessor(terminal, clearingProfile, transactions, stlDate);
//    	List<Transaction> tmp = new ArrayList<Transaction>();
//    	tmp.addAll(transactions);
//    	tmp.removeAll(notBeReturned);
//    	applyDesiredFlag(terminal, tmp, AccountingState.RETURNED);
//    	applyDesiredFlag(terminal, notBeReturned, AccountingState.NEED_TO_BE_RETURNED);
    	AccountingService.removeSettlementRecord(transactions, clearingProfile, null);
    }
    
    public static void partiallyRemoveProcess(Terminal terminal, ClearingProfile clearingProfile, List<Transaction> transactions, DateTime stlDate) throws Exception {
    	List<Transaction> list = partiallyRemoveProcessor(terminal, clearingProfile, transactions, stlDate);
    	List<Transaction> tmp = new ArrayList<Transaction>();
    	tmp.addAll(transactions);
    	tmp.removeAll(list);
    	applyDesiredFlag(terminal, tmp, AccountingState.RETURNED, clearingProfile, null);
    	applyDesiredFlag(terminal, list, AccountingState.NEED_TO_BE_RETURNED, clearingProfile, null);
    	AccountingService.removeSettlementRecord(transactions, clearingProfile, null);
    }
    
    
    public static void returnProcess(List<Message> messages, DateTime stlDate, SettlementDataType type) throws Exception {
    	List<Transaction> transactions = new ArrayList<Transaction>();
    	returnProcessor(messages, transactions, stlDate, type);
//    	applyDesiredFlag(terminal, transactions, AccountingState.RETURNED);
    }
    
    @NotUsed
    // Used only other mains
    public static void reconcileSettleProcess(List<Ifx> ifxList, SourceDestination[] sourceDestinations, DateTime stlDate,
			SettlementDataType type) throws Exception {
    	Map<Terminal, List<Transaction>> termTrx = new HashMap<Terminal, List<Transaction>>();
    	reconcileSettleProcessor(ifxList, termTrx, sourceDestinations, stlDate, type);
    	for (Terminal terminal: termTrx.keySet()) {
    		applyDesiredFlag(terminal, termTrx.get(terminal), AccountingState.COUNTED, null, null);
    	}
//    	applyDesiredFlag(terminal, transactions, AccountingState.RETURNED);
	}
    
    private static void applyDesiredFlag(List<SettlementInfo> settlementInfos, List<Transaction> transactions, AccountingState state) {
    	List<SettlementInfo> stlInfos = new ArrayList<SettlementInfo>();
    	Map<String, Object> params = new HashMap<String, Object>();
		String query = "";
		int counter = 0;
		
		DateTime currentTime = DateTime.now();
    	
    	for (int i = 0; i < settlementInfos.size(); i++) {
    		stlInfos.add(settlementInfos.get(i));
			counter++;
			if(counter == 500 || i == settlementInfos.size() - 1) {
				query = "update " + SettlementInfo.class.getName() + " t set "
					+ " t.accountingState = :accState "
					+ " , t.accountingDate.dayDate = :accDate "
					+ " , t.accountingDate.dayTime = :accTime "
					+ " where t in (:stlInfos) "
					+ " and t.transaction in (:trxList) ";
				
				params = new HashMap<String, Object>();
				params.put("accState", state);
				params.put("accDate", currentTime.getDayDate());
				params.put("accTime", currentTime.getDayTime());
				params.put("stlInfos", stlInfos);
				params.put("trxList", transactions);
				
				int numAffected = GeneralDao.Instance.executeUpdate(query, params);
				logger.debug("Num affected trx with batch update of counted flag:"+numAffected);
				
				counter = 0;
				stlInfos = new ArrayList<SettlementInfo>();
			}
		}
    }
    
	private static void applyDesiredFlag(Terminal terminal, List<Transaction> transactions, AccountingState state, ClearingProfile clearingProfile, SettledState settledState) {
  		List<Transaction> allTransactions = new ArrayList<Transaction>();
		if (transactions != null && !transactions.isEmpty()) {
			allTransactions.addAll(transactions);
			String srcDest = TransactionService.findSrcDest(terminal);
			
			boolean srcNeedToAccount = true;
			if ("thirdParty".equals(srcDest))
				srcNeedToAccount = false;

			List<Transaction> trxForQuery = new ArrayList<Transaction>();
			Map<String, Object> params = new HashMap<String, Object>();
			String query = "";
			int counter = 0;
			
			DateTime currentTime = DateTime.now();
			
			for (int i = 0; i < allTransactions.size(); i++) {
				trxForQuery.add(allTransactions.get(i));
				counter++;
				if(counter == 500 || i == allTransactions.size() - 1) {
					params = new HashMap<String, Object>();

					query = "update " + SettlementInfo.class.getName() + " t set "
						+ " t.accountingState = :accState "
						+ " , t.accountingDate.dayDate = :accDate "
						+ " , t.accountingDate.dayTime = :accTime ";
					
					if (settledState != null) {
						query += " , t.settledState = :stState "
						+ " , t.settledDate.dayDate = :stlDate "
						+ " , t.settledDate.dayTime = :stlTime ";
						
						params.put("stState", settledState);
						params.put("stlDate", currentTime.getDayDate());
						params.put("stlTime", currentTime.getDayTime());
					}
					
					
					/******** sub select query: start ********/
					String querySub = "select tx." + srcDest + "SettleInfo.id" +" from Transaction tx where tx in(:trnList)";
					Map<String, Object> paramsSub = new HashMap<String, Object>();
					paramsSub.put("trnList", trxForQuery);
					List<Long> stlInfos = GeneralDao.Instance.find(querySub, paramsSub);
					/******** sub select query: start ********/
					
					query += " where t.id in (:stlInfos) " 
//						+ " and t.transaction in (:trnList) "
						;
					
					params.put("accState", state);
					params.put("accDate", currentTime.getDayDate());
					params.put("accTime", currentTime.getDayTime());
//					params.put("trnList", trxForQuery);
					params.put("stlInfos", stlInfos);
					
					int numAffected = GeneralDao.Instance.executeUpdate(query, params);
					logger.debug("Num affected trx with batch update of counted flag:"+numAffected);
					
					if (!srcNeedToAccount) {
						query = "update " + SettlementInfo.class.getName() + " t set "
						+ " t.accountingState = :accState "
						+ " , t.accountingDate.dayDate = :accDate "
						+ " , t.accountingDate.dayTime = :accTime "
						+ " where t.id in (select tx." + "sourceSettleInfo" +" from Transaction tx where tx in(:trnList)) "
						+ " and t.transaction in (:trnList) ";
						
						params = new HashMap<String, Object>();
						params.put("accState", AccountingState.NO_NEED_TO_BE_COUNTED);
						params.put("accDate", currentTime.getDayDate());
						params.put("accTime", currentTime.getDayTime());
						params.put("trnList", trxForQuery);
						
						numAffected = GeneralDao.Instance.executeUpdate(query, params);
						logger.debug("Num affected trx with batch update of NO_NEED_TO_BE_COUNTED flag:"+numAffected);
						
						AccountingService.removeSettlementRecord(trxForQuery, null, clearingProfile);
					}
					
					counter = 0;
					trxForQuery = new ArrayList<Transaction>();
				}
			}
		}
	}

    private static void doProcessor(Terminal terminal, ClearingProfile clearingProfile, SettlementDataType type, List<Ifx> ifxList,
    		List<Transaction> transactions, DateTime settleDate) throws Exception {
		int i = 0;
		
		if (clearingProfile == null) {
			logger.info("clearing profile of terminal: " + terminal.getId() + " is null!!");
			return;
		}
		
		Map<FinancialEntity, Map<SettlementDataType, SettlementData>> settlementDatas = new HashMap<FinancialEntity, Map<SettlementDataType,SettlementData>>();
		
		for (Ifx ifx : ifxList) {
			i++;
			logger.info("before transactionProcessor, terminal:" + terminal.getCode() + ", ifx: " + ifx.getId() + " ifx no: " + i
					+ " of " + ifxList.size() + " ifxList");
			Transaction transaction = ifx.getTransaction();
//			Ifx ifx = msg.getIfx();

			int sumFlag = 1;
			if (toBeDecreasedAmount(ifx.getIfxType()))
				sumFlag = -1;
			try {
				
//				long mainAmount = sumFlag * ifx.getAuth_Amt();
				long mainAmount = sumFlag * ifx.getReal_Amt();
				
				//Mirkamali(Task179)
				if(TrnType.WITHDRAWAL_CUR.equals(ifx.getTrnType()))
					mainAmount = ifx.getAuth_Amt()/*ifx.getReal_Amt() * Long.valueOf(ifx.getAuth_CurRate()) */;
				
				
				long endPointAmount = 0;
				Terminal endPointTerminal = terminal;
				FinancialEntity ownerTerminal = terminal.getOwner();
				
				if (TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
//					endPointTerminal = transaction.getOutputMessage().getEndPointTerminal();
					endPointTerminal = transaction.getOutgoingIfxOrMessageEndpoint();
					computeAllFees(ownerTerminal.getOwnOrParentFeeProfile(), clearingProfile, ifx);
				} else {

					if ((ownerTerminal.getRole().equals(FinancialEntityRole.MASTER) && 
							endPointTerminal.getClearingMode().equals(TerminalClearingMode.ISSUER))
							|| (ownerTerminal.getRole().equals(FinancialEntityRole.SLAVE) && 
									endPointTerminal.getClearingMode().equals(TerminalClearingMode.ACQUIER))
							|| (ownerTerminal.getRole().equals(FinancialEntityRole.PEER) && 
									endPointTerminal.getClearingMode().equals(TerminalClearingMode.ACQUIER))) {
						if (!Util.hasText(ifx.getSec_CurRate()) || "1".equals(ifx.getSec_CurRate()))
//							endPointAmount = sumFlag * ifx.getAuth_Amt();
							endPointAmount = sumFlag * ifx.getReal_Amt();
						
						else
							endPointAmount = sumFlag * ifx.getSec_Amt();
					} else
//						endPointAmount = sumFlag * ifx.getAuth_Amt();
						endPointAmount = sumFlag * ifx.getReal_Amt();
					//Mirkamali(Task179)
					if(TrnType.WITHDRAWAL_CUR.equals(ifx.getTrnType()))
						endPointAmount = sumFlag * ifx.getAuth_Amt()/*ifx.getReal_Amt() * Long.valueOf(ifx.getAuth_CurRate())*/;
					
				}
				//Mirkamali(Task179)
				if(!TrnType.WITHDRAWAL_CUR.equals(ifx.getTrnType())) {
					computeAllFees(endPointTerminal.getOwner().getOwnOrParentFeeProfile(), clearingProfile, ifx);
					computeAllFees(endPointTerminal.getOwnOrParentFeeProfile(), clearingProfile, ifx);
				}
				

				logger.info(ifx.getIfxType() + "-Trx[" + ifx.getTransactionId() + ", amount:" + /*ifx.getAuth_Amt()*/ifx.getReal_Amt() + "] is about to be cleared!");

				AccountingService.generateSettlementDataAndUpdateSettleInfosForAllEntities(settlementDatas, terminal, endPointTerminal, transaction,
						mainAmount, endPointAmount, clearingProfile, type, settleDate);

				transactions.add(transaction);
			} catch (Exception ex) {
				logger.error("Exception! Rollback Transaction" + ex);
				throw ex;
			}
		}
	}
    
    private static void doProcessorPerTransaction(Terminal terminal, ClearingProfile clearingProfile, SettlementDataType type, List<Ifx> ifxList,
    		List<Transaction> transactions, DateTime settleDate) throws Exception {
    	int i = 0;
    	
    	if (clearingProfile == null) {
    		logger.info("clearing profile of terminal: " + terminal.getId() + " is null!!");
    		return;
    	}
    	
    	Map<FinancialEntity, Map<SettlementDataType, List<SettlementData>>> settlementDatas = new HashMap<FinancialEntity, Map<SettlementDataType, List<SettlementData>>>();
    	
    	for (Ifx ifx : ifxList) {
    		i++;
    		logger.info("before transactionProcessor, terminal:" + terminal.getCode() + ", ifx: " + ifx.getId() + " ifx no: " + i
    				+ " of " + ifxList.size() + " ifxList");
    		Transaction transaction = ifx.getTransaction();
//			Ifx ifx = msg.getIfx();
    		
    		int sumFlag = 1;
    		if (toBeDecreasedAmount(ifx.getIfxType()))
    			sumFlag = -1;
    		try {
    			
//				long mainAmount = sumFlag * ifx.getAuth_Amt();
    			long mainAmount = sumFlag * ifx.getReal_Amt();
    			long endPointAmount = 0;
    			Terminal endPointTerminal = terminal;
    			FinancialEntity ownerTerminal = terminal.getOwner();
    			
    			if (TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
//					endPointTerminal = transaction.getOutputMessage().getEndPointTerminal();
    				endPointTerminal = transaction.getOutgoingIfxOrMessageEndpoint();
    				computeAllFees(ownerTerminal.getOwnOrParentFeeProfile(), clearingProfile, ifx);
    			} else {
    				
    				if ((ownerTerminal.getRole().equals(FinancialEntityRole.MASTER) && 
    						endPointTerminal.getClearingMode().equals(TerminalClearingMode.ISSUER))
    						|| (ownerTerminal.getRole().equals(FinancialEntityRole.SLAVE) && 
    								endPointTerminal.getClearingMode().equals(TerminalClearingMode.ACQUIER))
    								|| (ownerTerminal.getRole().equals(FinancialEntityRole.PEER) && 
    										endPointTerminal.getClearingMode().equals(TerminalClearingMode.ACQUIER))) {
    					if (!Util.hasText(ifx.getSec_CurRate()) || "1".equals(ifx.getSec_CurRate()))
//							endPointAmount = sumFlag * ifx.getAuth_Amt();
    						endPointAmount = sumFlag * ifx.getReal_Amt();
    					else
    						endPointAmount = sumFlag * ifx.getSec_Amt();
    				} else
//						endPointAmount = sumFlag * ifx.getAuth_Amt();
    					endPointAmount = sumFlag * ifx.getReal_Amt();
    			}
    			
    			computeAllFees(endPointTerminal.getOwner().getOwnOrParentFeeProfile(), clearingProfile, ifx);
    			computeAllFees(endPointTerminal.getOwnOrParentFeeProfile(), clearingProfile, ifx);
    			
    			logger.info(ifx.getIfxType() + "-Trx[" + ifx.getTransactionId() + ", amount:" + /*ifx.getAuth_Amt()*/ifx.getReal_Amt() + "] is about to be cleared!");
    			
    			AccountingService.generateSettlementDataAndUpdateSettleInfosForAllEntitiesPer(settlementDatas, terminal, endPointTerminal, transaction,
    					mainAmount, endPointAmount, clearingProfile, type, /*settleDate*/ifx.getReceivedDt());
    			
    			transactions.add(transaction);
    		} catch (Exception ex) {
    			logger.error("Exception! Rollback Transaction" + ex);
    			throw ex;
    		}
    	}
    }

    private static void doProcessorNew(Terminal terminal, ClearingProfile clearingProfile, SettlementDataType type, List<IfxSettlement> ifxList,
    		List<Transaction> transactions, DateTime settleDate) throws Exception {
		int i = 0;
		
		if (clearingProfile == null) {
			logger.info("clearing profile of terminal: " + terminal.getId() + " is null!!");
			return;
		}
		
		Map<FinancialEntity, Map<SettlementDataType, SettlementData>> settlementDatas = new HashMap<FinancialEntity, Map<SettlementDataType,SettlementData>>();
		
		for (IfxSettlement ifx : ifxList) {
			i++;
			logger.info("before transactionProcessor, terminal:" + terminal.getCode() + ", ifx: " + ifx.getId() + " ifx no: " + i
					+ " of " + ifxList.size() + " ifxList");
			Transaction transaction = GeneralDao.Instance.load(Transaction.class, ifx.getTransactionId());
//			Ifx ifx = msg.getIfx();

			int sumFlag = 1;
			if (toBeDecreasedAmount(ifx.getIfxType()))
				sumFlag = -1;
			try {

//				long mainAmount = sumFlag * ifx.getAuth_Amt();
				long mainAmount = sumFlag * ifx.getReal_Amt();
				long endPointAmount = 0;
				Terminal endPointTerminal = terminal;
				FinancialEntity ownerTerminal = terminal.getOwner();
				if (TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
//					endPointTerminal = transaction.getOutputMessage().getEndPointTerminal();
					endPointTerminal = TerminalService.findTerminal(Terminal.class, ifx.getEndPointTerminalCode());
					if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
						computeAllFees(ownerTerminal.getOwnOrParentFeeProfile(), clearingProfile, ifx);
					}
				} else {

					if ((ownerTerminal.getRole().equals(FinancialEntityRole.MASTER) && 
							endPointTerminal.getClearingMode().equals(TerminalClearingMode.ISSUER))
							|| (ownerTerminal.getRole().equals(FinancialEntityRole.SLAVE) && 
									endPointTerminal.getClearingMode().equals(TerminalClearingMode.ACQUIER))
							|| (ownerTerminal.getRole().equals(FinancialEntityRole.PEER) && 
									endPointTerminal.getClearingMode().equals(TerminalClearingMode.ACQUIER))) {
						if (!Util.hasText(ifx.getSec_CurRate()) || "1".equals(ifx.getSec_CurRate()))
//							endPointAmount = sumFlag * ifx.getAuth_Amt();
							endPointAmount = sumFlag * ifx.getReal_Amt();
						else
							endPointAmount = sumFlag * ifx.getSec_Amt();
					} else
//						endPointAmount = sumFlag * ifx.getAuth_Amt();
						endPointAmount = sumFlag * ifx.getReal_Amt();
				}
				if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
					computeAllFees(endPointTerminal.getOwner().getOwnOrParentFeeProfile(), clearingProfile, ifx);
					computeAllFees(endPointTerminal.getOwnOrParentFeeProfile(), clearingProfile, ifx);
				}

				logger.info(ifx.getIfxType() + "-Trx[" + ifx.getTransactionId() + ", amount:" + /*ifx.getAuth_Amt()*/ifx.getReal_Amt() + "] is about to be cleared!");

				AccountingService.generateSettlementDataAndUpdateSettleInfosForAllEntities(settlementDatas, terminal, endPointTerminal, transaction,
						mainAmount, endPointAmount, clearingProfile, type, settleDate);

				transactions.add(transaction);
			} catch (Exception ex) {
				logger.error("Exception! Rollback Transaction" + ex);
				throw ex;
			}
		}
	}

    private static boolean toBeDecreasedAmount(IfxType ifxType) {
		if (ISOFinalMessageType.isTransferToMessage(ifxType) ||
				ISOFinalMessageType.isTransferToAccountTransferToMessage(ifxType)||
				ISOFinalMessageType.isReturnMessage(ifxType))
			return true;
		if (ISOFinalMessageType.isReversalOrRepeatMessage(ifxType)) {
			if (ISOFinalMessageType.isReturnReverseMessage(ifxType))
				return false;
			if (ISOFinalMessageType.isTransferToRevMessage(ifxType))
				return false;
			if (ISOFinalMessageType.isTransferAccountToRevMessage(ifxType))
				return false;
			else
				return true;
		}
		return false;

	}

	private static List<Transaction> removeProcessor(Terminal terminal, ClearingProfile clearingProfile, List<Transaction> transactions, DateTime settleDate)
			throws Exception {
		
//		List<Transaction> needToBeReturned = new ArrayList<Transaction>();
		if (transactions == null || transactions.size() == 0)
			return null;
		
		String query = "select " 
				+ "d.settlementData.id, sum(d.totalAmount), sum(d.totalFee) from " 
				+ " SettlementInfo d "
				+ " where "
				// + " d.settlementData = :settlementData "
				// + " and "
				+ " d.settlementData.clearingProfile = :clrProf and "
				+ " d.transaction in (:trxList) group by d.settlementData.id";
		Map<String, Object> params = new HashMap<String, Object>();
		// params.put("settlementData", settlementData);
		params.put("clrProf", clearingProfile);
		params.put("trxList", transactions);
		List<Object[]> stlDataAmtFeeList = GeneralDao.Instance.find(query, params);

		Iterator<Object[]> iterator = stlDataAmtFeeList.iterator();

		while (iterator.hasNext()) {
			Object[] stlDataAmtFee = iterator.next();
			Long settlementDataId = (Long) stlDataAmtFee[0];
			
			if (settlementDataId == null)
				continue;
			
			Long amount = (Long) stlDataAmtFee[1];
			Long fee = (Long) stlDataAmtFee[2];
			if (settlementDataId != null) {
				if (fee == null)
					fee = new Long(0L);
				if (amount == null)
					amount = new Long(0L);
				SettlementData settlementData = GeneralDao.Instance.getObject(SettlementData.class, settlementDataId);
				
				try {
					logger.debug("Try to lock settlementData " + settlementData.getId());
					settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
//					logger.debug("settlementData locked.... " + settlementData.getId());
				} catch (Exception e) {
					logger.error("Encounter an exception to lock settlementData", e);
				}
				
				if (Util.hasText(settlementData.getDocumentNumber()) ||settlementData.getSettlementReport()!= null){
					logger.error("settlementData "+ settlementData.getId()+" has settlement report so it cannot be updated!");
//					List<Transaction>tmp = new ArrayList<Transaction>();
//					tmp.addAll(transactions);
//					tmp.retainAll(settlementData.getTransactions());
//					needToBeReturned.addAll(tmp);
					
					String query2 = "from SettlementInfo d "
						+ " where "
						+ " d.settlementData.id = :stlData and "
						+ " d.transaction in (:trxList)";
					Map<String, Object> params2 = new HashMap<String, Object>();
					params2.put("stlData", settlementDataId);
					params2.put("trxList", transactions);
					List<SettlementInfo> needToBeReturned = GeneralDao.Instance.find(query2, params2);
					
					for(SettlementInfo stlInfo:needToBeReturned){
						logger.debug("need to be returned stlInfo: "+stlInfo.getId());
					}
					applyDesiredFlag(needToBeReturned, transactions, AccountingState.NEED_TO_BE_RETURNED);
					logger.debug("put NEED_TO_BE_RETURNED flag on settleInfos of settlementData: " + settlementData.getId());
				}else{
					String query2 = "from SettlementInfo d "
						+ " where "
						+ " d.settlementData.id = :stlData and "
						+ " d.transaction in (:trxList)";
					Map<String, Object> params2 = new HashMap<String, Object>();
					params2.put("stlData", settlementDataId);
					params2.put("trxList", transactions);
					List<SettlementInfo> returned = GeneralDao.Instance.find(query2, params2);
					
					for(SettlementInfo stlInfo:returned){
						logger.debug("returned stlInfo: "+stlInfo.getId());
					}

					applyDesiredFlag(returned, transactions, AccountingState.RETURNED);
		    		logger.debug("put RETURNED flag on settleInfos of settlementData: " + settlementData.getId());
		    		
					
					query = "update " + SettlementInfo.class.getName() + " d " 
						+ " set d.settlementData = null "
						+ " where d.settlementData.id = :settlementData " 
						+ " and d.transaction in (:trxList) ";
					params = new HashMap<String, Object>();
					params.put("settlementData", settlementDataId);
					params.put("trxList", transactions);
					int numStlInfo = GeneralDao.Instance.executeUpdate(query, params);
		
					settlementData.setTotalAmount(settlementData.getTotalAmount() - amount);
					settlementData.setTotalFee(settlementData.getTotalFee() - fee);
					settlementData.setTotalSettlementAmount(settlementData.getTotalSettlementAmount() - (amount + fee));
					settlementData.setNumTransaction(settlementData.getNumTransaction() - numStlInfo);
		
					for(int i=0; i<transactions.size(); i++){
						GeneralDao.Instance.executeSqlUpdate("delete from "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".settlement_data_trx_transaxion " +
								"where SETTLEMENT_DATA_ID ="+settlementDataId+" and TRANSACTIONS_ID ="+transactions.get(i).getId());
					}
					
		//			settlementData.removeTransactions(transactions);
		
					GeneralDao.Instance.saveOrUpdate(settlementData);
				}
			}
		}
		
		return null;
	}
	
	private static List<Transaction> partiallyRemoveProcessor(Terminal terminal, ClearingProfile clearingProfile, List<Transaction> transactions, DateTime settleDate)
	throws Exception {
		List<Transaction> needToBeReturned = new ArrayList<Transaction>();
		if (transactions == null)
			return needToBeReturned;
		
		String query = "select " 
			+ "d.settlementData.id "
			+", sum(d.totalAmount) "
			+", sum(i.eMVRqData.Real_Amt) "
//			+"sum(d.totalFee)" 
			+" from " 
			+ SettlementInfo.class.getName() + " d "
			+", "+ Ifx.class.getName()+" i "
			+ " where "
			// + " d.settlementData = :settlementData "
			// + " and "
			+ " d.transaction in (:trxList) "
			+" and i.transaction = d.transaction"
			+" group by d.settlementData.id";
		Map<String, Object> params = new HashMap<String, Object>();
		// params.put("settlementData", settlementData);
		params.put("trxList", transactions);
		List<Object[]> stlDataAmtFeeList = GeneralDao.Instance.find(query, params);
		
		Iterator<Object[]> iterator = stlDataAmtFeeList.iterator();
		
		while (iterator.hasNext()) {
			Object[] stlDataAmtFee = iterator.next();
			Long settlementDataId = (Long) stlDataAmtFee[0];
			if (settlementDataId == null)
				continue;
			
				SettlementData settlementData = GeneralDao.Instance.getObject(SettlementData.class, settlementDataId);
				
				if (Util.hasText(settlementData.getDocumentNumber()) ||settlementData.getSettlementReport()!= null){
					logger.error("settlementData "+ settlementData.getId()+" has settlement report so it cannot be updated!");
					List<Transaction>tmp = new ArrayList<Transaction>();
					tmp.addAll(transactions);
					tmp.retainAll(settlementData.getTransactions());
					needToBeReturned.addAll(tmp);
//					needToBeReturned.addAll(settlementData.getTransactions());
					continue;
				}
				
				
//				try {
//		    		logger.debug("Try to lock settlementData " + settlementData.getId());
//					settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
//					logger.debug("settlementData locked.... " + settlementData.getId());
//	    		} catch (Exception e) {
//					logger.error("Encounter an exception to lock settlementData", e);
//				}
	    		
				
				
				query = "select i.eMVRqData.Real_Amt"
					+", (d.totalAmount)"
					+" from "+ Ifx.class.getName()+ " i, "
					+ SettlementInfo.class.getName()+ " d "
				+ " where i.transaction = d.transaction"
				+ " and d.settlementData.id = :settlementData " 
				+ " and d.transaction in (:trxList) ";
				params = new HashMap<String, Object>();
				params.put("settlementData", settlementDataId);
				params.put("trxList", transactions);
				
				List<Object[]> amountList = GeneralDao.Instance.find(query, params);
				Long realAmount = (Long) amountList.get(0)[0];
				Long amount = (Long) amountList.get(0)[1];
				if (settlementDataId != null) {
					if (realAmount == null)
						realAmount = new Long(0L);
					if (amount == null)
						amount = new Long(0L);
				
				query = "update " + SettlementInfo.class.getName() + " d " 
//				+ " set d.settlementData = null "
				+ "set d.totalAmount = :realAmount "
				+ " where d.settlementData.id = :settlementData " 
				+ " and d.transaction in (:trxList) ";
				params.put("realAmount", realAmount);
				int numStlInfo = GeneralDao.Instance.executeUpdate(query, params);
				
				settlementData.setTotalAmount(settlementData.getTotalAmount() - amount+ realAmount);
//				settlementData.setTotalFee(settlementData.getTotalFee() - realAmount);
				settlementData.setTotalSettlementAmount(settlementData.getTotalSettlementAmount() - amount + realAmount);
//				settlementData.setNumTransaction(settlementData.getNumTransaction() - numStlInfo);
//				settlementData.removeTransactions(transactions);
				
				GeneralDao.Instance.saveOrUpdate(settlementData);
			}
		}
		
		return needToBeReturned;
	}
	
	private static void returnProcessor(List<Message> messages, List<Transaction> transactions, DateTime settleDate, SettlementDataType type)
			throws Exception {
		int i = 0;
//		Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
		Long myBin = ProcessContext.get().getMyInstitution().getBin();
		List<SettlementInfo> finalSettleInfos = new ArrayList<SettlementInfo>();
		List<Ifx> shetabIfxList = new ArrayList<Ifx>();
		for (Message msg : messages) {
			i++;
			logger.info("before returnProcessor, message: " + msg.getId() + "message no: " + i + " of " + messages.size() + " messages");

			Transaction transaction = msg.getTransaction();
			logger.info("before returnProcessor, transaction: " + transaction.getId() + "transaction no: " + i + " of " + messages.size() + " messages");

			SettlementInfo srcStlInfo = transaction.getSourceSettleInfo();
			SettlementInfo destStlInfo = transaction.getDestinationSettleInfo();
			SettlementInfo thirdStlInfo = transaction.getThirdPartySettleInfo();

			Ifx ifx = msg.getIfx();
			logger.info(ifx.getIfxType() + "[" + transaction.getId() + ", amount:" + ifx.getReal_Amt()/*ifx.getAuth_Amt() */+ "] is about to be returned!");
			try {
				List<SettlementInfo> settleInfos = AccountingService.getRelatedNonRoutinSettlementInfo(transaction);
				if (settleInfos != null)
					finalSettleInfos.addAll(settleInfos);
				
				if (srcStlInfo != null)
					finalSettleInfos.add(srcStlInfo);

				if (myBin.equals(ifx.getBankId())) {
					if (thirdStlInfo != null)
						finalSettleInfos.add(thirdStlInfo);
				}
				
				if (myBin.equals(ifx.getDestBankId())) {
					SchedulerService.processReversalJob(transaction.getFirstTransaction(), transaction, ISOResponseCodes.APPROVED, null, true);
//					lifeCycle.setIsComplete(false);
//					lifeCycle.setIsFullyReveresed(LifeCycleStatus.REQUEST);
//					GeneralDao.Instance.saveOrUpdate(lifeCycle);
//					
//					SchedulerService.createReversalJobInfo(transaction.getFirstTransaction(), ErrorCodes.APPROVED, null);
					
				} else {
					if (destStlInfo != null)
						finalSettleInfos.add(destStlInfo);
					
					shetabIfxList.add(ifx);
				}

				if (!myBin.equals(ifx.getBankId()) && !myBin.equals(ifx.getDestBankId())) {

				}
				transactions.add(transaction);

			} catch (Exception ex) {
				logger.error("Exception! Rollback Transaction" + ex);
				throw ex;
			}
		}
		
		try {
			ShetabReconciliationService.generateReconcileShetabReport(shetabIfxList);
		} catch (Exception e) {
			
		}
		
		AccountingService.generateReconcilementDataForAllEntities(finalSettleInfos, type, settleDate);
	}
	
	public static void returnProcess(List<Ifx> ifxList, SourceDestination[] sourceDestinations, DateTime settleDate, SettlementDataType type) throws Exception {
		int i = 0;
		List<SettlementInfo> finalSettleInfos = new ArrayList<SettlementInfo>();
		for (Ifx ifx: ifxList) {
			i++;
			logger.info("before returnProcessor, ifx: " + ifx.getId() + "ifx no: " + i + " of " + ifxList.size() + " ifxList");

			Transaction transaction = ifx.getTransaction();
			logger.info("before returnProcessor, transaction: " + transaction.getId() + " transaction no: " + i + " of " + ifxList.size() + " ifxList");

			SettlementInfo destStlInfo = transaction.getDestinationSettleInfo();

			logger.info(ifx.getIfxType() + "[" + transaction.getId() + ", amount:" + ifx.getReal_Amt()/*ifx.getAuth_Amt() */+ "] is about to be returned!");
			try {
				if (destStlInfo != null)
					finalSettleInfos.add(destStlInfo);

			} catch (Exception ex) {
				logger.error("Exception! Rollback Transaction" + ex);
				throw ex;
			}
		}
		
		AccountingService.generateReconcilementDataForAllEntities(finalSettleInfos, type, settleDate);
	}
	
	private static void reconcileSettleProcessor(List<Ifx> ifxList, Map<Terminal, List<Transaction>> termTrx, SourceDestination[] srcDst, DateTime settleDate, SettlementDataType type)
	throws Exception {
		int i = 0;
		List<SettlementInfo> finalSettleInfos = new ArrayList<SettlementInfo>();
		for (Ifx ifx : ifxList) {
			i++;
			logger.info("before settleProcessor, ifx: " + ifx.getId() + "message no: " + i + " of " + ifxList.size() + " ifxList");
			
			Transaction transaction = ifx.getTransaction();
			logger.info("before settleProcessor, transaction: " + transaction.getId() + "transaction no: " + i + " of " + ifxList.size() + " ifxList");
			
			SettlementInfo srcStlInfo = transaction.getSourceSettleInfo();
			SettlementInfo destStlInfo = transaction.getDestinationSettleInfo();
			
//			Ifx ifx = msg.getIfx();
			logger.info(ifx.getIfxType() + "[" + transaction.getId() + ", amount:" + ifx.getReal_Amt()/*ifx.getAuth_Amt() */+ "] is about to be returned!");
			try {
			
				for (SourceDestination sd: srcDst) {
					if (SourceDestination.SOURCE.equals(sd) && srcStlInfo != null)
						finalSettleInfos.add(srcStlInfo);
					else if (SourceDestination.DESTINATION.equals(sd) && destStlInfo != null)
						finalSettleInfos.add(destStlInfo);
				}
				
				Terminal terminal = transaction.getFirstTransaction().getOutgoingIfxOrMessageEndpoint();
				List<Transaction> trxList = termTrx.get(terminal);
				if (trxList == null) {
					trxList = new ArrayList<Transaction>();
					termTrx.put(terminal, trxList);
				}
				trxList.add(transaction);
				
			} catch (Exception ex) {
				logger.error("Exception! Rollback Transaction" + ex);
				throw ex;
			}
		}
		
		for (Terminal terminal : termTrx.keySet()) {
			doProcessor(terminal, ProcessContext.get().getClearingProfile(terminal.getOwnOrParentClearingProfileId()), type, ifxList, termTrx.get(terminal), settleDate);
		}
//		accountingService.generateReconcilementSettlementDataForAllEntities(termTrx, type, settleDate);
	}

//	private static void computeAllFees(FeeProfile feeProfile, ClearingProfile clearingProfile, Message msg) {
//		if (feeProfile != null && msg != null) {
//			FeeService.computeFees(feeProfile, clearingProfile, msg);
//		}
//	}
	
	private static void computeAllFees(FeeProfile feeProfile, ClearingProfile clearingProfile, Ifx ifx) {
		if (feeProfile != null && ifx != null && Boolean.TRUE.equals(clearingProfile.getHasFee())) {
			FeeService.computeFees(feeProfile, clearingProfile, ifx);
		}
	}

	private static void computeAllFees(FeeProfile feeProfile, ClearingProfile clearingProfile, IfxSettlement ifx) {
		if (feeProfile != null && ifx != null && Boolean.TRUE.equals(clearingProfile.getHasFee())) {
			logger.error("OHOH, bad situation in computeAllFees....");
//			FeeService.computeFees(feeProfile, clearingProfile, ifx);
		}
	}
}
