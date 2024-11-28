package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.SynchronizationService;
import vaulsys.clearing.TransactionFinancialProcessor;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.ClearingProcessType;
import vaulsys.clearing.consts.LockObject;
import vaulsys.clearing.consts.SettlementDataCriteria;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.customer.Account;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.batch.IfxSettlement;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;

import vaulsys.customer.AccountType;

public abstract class SettlementService {
    private static final Logger logger = Logger.getLogger(SettlementService.class);

    public void settle(ClearingProfile clearingProfile, DateTime settleUntilTime, Boolean update, Boolean settleTime, Boolean generateSettleState) {
        settle(null, clearingProfile, settleUntilTime, update, settleTime, generateSettleState, false);
    }

    public void settle(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleUntilTime,
                       Boolean update, Boolean settleTime, Boolean generateSettleState, Boolean considerClearingProcessType) {
        //A trick to settle in case that we encounter an exception in accounting step
        //We will try for 3 times, if error exist then we do not proceed settlement process
        int numTries = 0;
        int maxTries = 3;
        boolean isFinishedAccounting = false;

//		if (!OnlineSettlementService.class.equals(clearingProfile.getSettlementClass())){
        while(numTries < maxTries && !isFinishedAccounting){
            try{
                boolean waiting = true;
                if(RequestBasedSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()) ||
                        OnlinePerTransactionSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()))
                    waiting = false;
                account(terminals, clearingProfile, settleUntilTime, settleUntilTime, update, waiting, settleTime, settleTime, considerClearingProcessType);
                isFinishedAccounting = true;
            }catch(LockAcquisitionException e){
                logger.error("Exception in accounting. LockAcquisitionException: "+numTries+" ",e);
                try {
                    Thread.sleep(60000L);
                } catch (InterruptedException e1) {
                    continue;
                }
            }catch(Exception e){
                logger.error("Exception in accounting. numTries: "+numTries+" ",e);
                numTries++;
            }
        }

        if(!isFinishedAccounting){
            logger.error("We faced to maxTries Exception in accounting, so we don't proceed in settlement...");
            return;
        }
//		} else {
//			try {
//				account(terminals, clearingProfile, settleUntilTime, settleUntilTime, update, true, settleTime, settleTime, false);
//			} catch (Exception e) {
//				logger.error("Exception in online accounting at settle time" , e);
//			}
//			
//		}

        DateTime settleDate = settleUntilTime;

        try {
            logger.info("Generating Settlement Data Report...");
            try {
                generateSettlementDataReport(terminals, clearingProfile, settleDate);
            } catch (Exception e) {
                logger.error("Exception in Generating Settlement Data Report " + e, e);
            }

            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.refresh(clearingProfile);

            if (generateSettleState) {

                logger.info("Generating Settlement State Report...");
                try {
                    generateSettlementStateAndReport(terminals, clearingProfile, settleDate, getSettlementTypeDesc());
                } catch (Exception e) {
                    logger.error("Exception in Generating Settlement State Report, must be rollback beacuase incorrect SettlementState created! "+ e);
                    GeneralDao.Instance.rollback();
                    return;
                }

                GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.beginTransaction();
                GeneralDao.Instance.refresh(clearingProfile);

                logger.info("Generating Desired Terminal Settlement Report...");
                try {
                    generateDesiredSettlementReports(clearingProfile, settleDate);
                } catch (Exception e) {
                    logger.error("Exception in Generating Desired Terminal Settlement Report  " + e, e);
                }

            }

            if (settleTime) {
                logger.info("Generating Final Settlement State Report...");
                try {
                    generateDocumentSettlementState(clearingProfile, getSettlementTypeDesc(), settleDate);
                } catch (Exception e) {
                    logger.error("Exception in Generating Final Settlement State Report  " + e, e);
                }
            }

            GeneralDao.Instance.endTransaction();
            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.refresh(clearingProfile);

            logger.info("Puting Settle Flag...");
//			settleTransactions(clearingProfile, settleDate);

            logger.info("End of Put Settle Flag.");

        } catch (Exception e) {
            logger.error(e);
            GeneralDao.Instance.rollback();
            return;
        }
        GeneralDao.Instance.endTransaction();
    }

    protected void generateSettlementDataReport(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
        ReportGenerator.generateSettlementDataReport(terminals, clearingProfile, settleDate);
    }

    protected void generateSettlementStateAndReport(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate, String settlementTypeDesc) throws Exception {
        ReportGenerator.generateSettlementStateAndReportNew(terminals, clearingProfile, settleDate, getSettlementTypeDesc());
    }

    public void account(ClearingProfile clearingProfile,
                        DateTime accountUntilTime, DateTime settleUntilTime,
                        Boolean update, Boolean waitForSyncObject,
                        Boolean onlyFanapAccount, Boolean considerClearingProcessType) throws Exception {
        account(null, clearingProfile, accountUntilTime, settleUntilTime, update, waitForSyncObject, onlyFanapAccount, false, considerClearingProcessType);
    }


//	public void account(List<Terminal> terminals, ClearingProfile clearingProfile, 
//			DateTime accountUntilTime, DateTime settleUntilTime, 
//			Boolean update, Boolean waitForSyncObject, 
//			Boolean onlyFanapAccount, Boolean settleTime, 
//			Boolean considerClearingProcessType) throws Exception {
//		
//		if (clearingProfile != null) {
//			logger.info("Starting Terminal Accounting... ClrProf("+clearingProfile.getId()+")");
//		} else {
//			logger.info("Starting Terminal Accounting...");
//		}
//		
//		List<Message> postPrep = new ArrayList<Message>();
//		
//		boolean lockAcq = false;
//		try {
//			GeneralDao.Instance.beginTransaction();
//			if(waitForSyncObject.equals(true)){
//				SynchronizationService.getSynchornizationObject(clearingProfile, ClearingProfile.class, LockMode.UPGRADE);
//			}else{
//				SynchronizationService.getSynchornizationObject(clearingProfile, ClearingProfile.class, LockMode.UPGRADE_NOWAIT);
//			}
//			SynchronizationService.lock(clearingProfile, ClearingProfile.class);
//			GeneralDao.Instance.endTransaction();
//			lockAcq = true;
//		} catch (Exception e) {
//			logger.error("ClearingProfile "+ clearingProfile.getId()+" is busy now! "+ e.getClass().getSimpleName()+": "+ e.getMessage());
//			GeneralDao.Instance.rollback();
//			throw e;
//		}
//		
//		GeneralDao.Instance.beginTransaction();
//		GeneralDao.Instance.refresh(clearingProfile);
//		try {
//			boolean justToday = true;
//			if (settleUntilTime.equals(accountUntilTime))
//				justToday = false;
//			
//			boolean onlineProcess = false;
//			List<Long> trxSettleRecord = new ArrayList<Long>();
//			List terminalCodes = new ArrayList();
//			if (terminals== null || terminals.isEmpty()){
//				DateTime realTimeForAccounting = accountUntilTime;
//				if (settleUntilTime.before(accountUntilTime))
//					realTimeForAccounting = settleUntilTime;
//				
//				if(ClearingProcessType.ONLINE.equals(clearingProfile.getProcessType()) && considerClearingProcessType.equals(true)){
//					onlineProcess = true;
//					terminalCodes = findDesiredTerminalCodesBasedOnSettlementRecord(accountUntilTime, justToday, clearingProfile);
//					
//				}else{
//					terminalCodes = findDesiredTerminalCodes(realTimeForAccounting, justToday, clearingProfile);
//				}
//			}else{
//				for (Terminal terminal : terminals) {
//					terminalCodes.add(terminal.getCode());
//				}
//				terminals = null;
//			}
//			
//			GeneralDao.Instance.endTransaction();
//			
//			List<Terminal> tmpTerminals = null;
//			
//			int counter = 0;
//			int maxCounter = 100;
//			
////			if(OnlineSettlementService.class.equals(clearingProfile.getSettlementClass())){
////				maxCounter = terminalCodes.size();
////			}
//			
//			List<Long> termForQuery = new ArrayList<Long>();
//			List<Long> nextRoundTerminal;
//			for (int i = 0; i < terminalCodes.size(); i++) {
//				logger.info("Term:"+terminalCodes.get(i));
//				if(terminalCodes.get(i) instanceof String)
//					termForQuery.add(Long.parseLong((String)terminalCodes.get(i)));
//				else if(terminalCodes.get(i) instanceof Long)
//					termForQuery.add((Long)terminalCodes.get(i));
//				else if(terminalCodes.get(i) instanceof Terminal)
//					termForQuery.add(((Terminal)terminalCodes.get(i)).getCode());
//				counter++;
//				
//				
//				if(counter == maxCounter || i == terminalCodes.size() - 1) {
//					nextRoundTerminal = termForQuery;
//					List<Terminal> firstRoundTerminal = null;
//					boolean firstRound = true;
//					
//					while (firstRound || !nextRoundTerminal.isEmpty()) {
//						GeneralDao.Instance.beginTransaction();
//						GeneralDao.Instance.refresh(clearingProfile);
//						logger.info("terminalCodes: " + terminalCodes.size() + " termForQuery: " + nextRoundTerminal.size());
//						
//						if (onlineProcess)
//							trxSettleRecord = TransactionService.getTransactionFromSettlementRecord(termForQuery);
//						
////						if(OnlineSettlementService.class.equals(clearingProfile.getSettlementClass())) {
////							tmpTerminals = new ArrayList<Terminal>();
////							for(int k=0; k<terminalCodes.size(); k+=500){
////								if (onlineProcess)
////									tmpTerminals.addAll(findAllTerminalsBasedOnSettlementRecord(terminals, nextRoundTerminal.subList(k, Math.min(terminalCodes.size(),k+500)), clearingProfile));
////								else 
////									tmpTerminals.addAll(findAllTerminals(terminals, nextRoundTerminal.subList(k, Math.min(terminalCodes.size(),k+500)), clearingProfile));
////							}
////						}else{
//						tmpTerminals = findAllTerminals(terminals, nextRoundTerminal, clearingProfile);
////						}
//						if (tmpTerminals != null && tmpTerminals.size() > 0) {
//							try {
//								logger.info(tmpTerminals.size() + " terminal with clrProfile: "	+ clearingProfile.getId());
//								
//								logger.info("Preparing Settlement...");
//								
//								if (firstRound){
//									firstRoundTerminal = tmpTerminals;
//									firstRound = false;
//								}
//								
//								nextRoundTerminal = prepareForSettlement(clearingProfile, tmpTerminals, accountUntilTime, settleUntilTime, settleTime, considerClearingProcessType, trxSettleRecord);
//								
//								if (nextRoundTerminal.isEmpty()) {
//									if (OnlineSettlementService.class.equals(clearingProfile.getSettlementClass())/*&& tmpRes != null*/) {
//										List<Message> tmpRes = (List<Message>) postPrepareForSettlement(tmpTerminals, clearingProfile, settleUntilTime, onlyFanapAccount);
//										if (tmpRes != null)
//											postPrep.addAll(tmpRes);
//									}
//									if (update)
//										updateToNowSettlementData(clearingProfile, firstRoundTerminal, accountUntilTime,settleUntilTime);
//								}
//								
//							} catch (Exception e) {
//								logger.error(e);
//								GeneralDao.Instance.rollback();
//								GeneralDao.Instance.beginTransaction();
//								if (lockAcq)
//									SynchronizationService.release(clearingProfile, ClearingProfile.class);
//								GeneralDao.Instance.endTransaction();
//								throw e;
//							}
//							
//						} else {
//							nextRoundTerminal.clear();
//							if (firstRound)
//								firstRound = false;
//						}
//						
//						GeneralDao.Instance.endTransaction();
//					}
//					
//					counter = 0;
//					termForQuery = new ArrayList<Long>();
//				}
//			}
//		} catch (Exception e) {
//			logger.error("Encounter with an exception in terminal accounting..."+ e.getClass().getSimpleName()+": "+ e.getMessage());
//			try {
//				GeneralDao.Instance.rollback();
//			} catch (Exception e1) {
//				logger.error(e1);
//				GeneralDao.Instance.beginTransaction();
//				if (lockAcq)
//					SynchronizationService.release(clearingProfile, ClearingProfile.class);
//				GeneralDao.Instance.endTransaction();
//				throw e1;
//			}
//			throw e;
//		}finally{
//			GeneralDao.Instance.beginTransaction();
//			if (lockAcq)
//				SynchronizationService.release(clearingProfile, ClearingProfile.class);
//			GeneralDao.Instance.endTransaction();
//		}
//		
//		if (OnlineSettlementService.class.equals(clearingProfile.getSettlementClass()) && postPrep != null && !postPrep.isEmpty()) {
//			for (Message m : postPrep) {
//				logger.debug("Adding msg " + m + " type:" + ((ScheduleMessage) m).getMessageType());
//			}
//			
//			MessageManager.getInstance().putRequests(postPrep);
//		}
//		
//		logger.info("Ending Terminal Accounting...");
//	}

    public void account(List<Terminal> terminals, ClearingProfile clearingProfile,
                        DateTime accountUntilTime, DateTime settleUntilTime,
                        Boolean update, Boolean waitForSyncObject,
                        Boolean onlyFanapAccount, Boolean settleTime,
                        Boolean considerClearingProcessType) throws Exception {

        if (clearingProfile != null) {
            logger.info("Starting Terminal Accounting... ClrProf("+clearingProfile.getId()+")");
        } else {
            logger.info("Starting Terminal Accounting...");
        }

        List<Message> postPrep = new ArrayList<Message>();

        boolean lockAcq = false;
        try {
            GeneralDao.Instance.beginTransaction();
            LockObject lockObject = clearingProfile.getLockObject();
            if(waitForSyncObject.equals(true)){
                if (LockObject.CLEARING_PROFILE.equals(lockObject)) {
                    SynchronizationService.getSynchornizationObject(clearingProfile, ClearingProfile.class, LockMode.UPGRADE);
                }
            } else {
                if (LockObject.CLEARING_PROFILE.equals(lockObject)) {
                    SynchronizationService.getSynchornizationObject(clearingProfile, ClearingProfile.class, LockMode.UPGRADE_NOWAIT);
                }
            }

            if (LockObject.CLEARING_PROFILE.equals(lockObject)) {
                SynchronizationService.lock(clearingProfile, ClearingProfile.class);
            }

            GeneralDao.Instance.endTransaction();
            lockAcq = true;
        } catch (Exception e) {
            logger.error("ClearingProfile "+ clearingProfile.getId()+" is busy now! "+ e.getClass().getSimpleName()+": "+ e.getMessage());
            GeneralDao.Instance.rollback();
            throw e;
        }

        int MAX_THREADS = 1;
        Semaphore semaphore = new Semaphore(MAX_THREADS);

        GeneralDao.Instance.beginTransaction();
        GeneralDao.Instance.refresh(clearingProfile);
        try {
            boolean justToday = true;
            if (settleUntilTime.equals(accountUntilTime))
                justToday = false;

            boolean onlineProcess = false;
            List<Long> trxSettleRecord = new ArrayList<Long>();
            List terminalCodes = new ArrayList();

            if (ClearingProcessType.ONLINE.equals(clearingProfile.getProcessType()) && considerClearingProcessType.equals(true)) {
                onlineProcess = true;
            }

            if (terminals== null || terminals.isEmpty()){
                DateTime realTimeForAccounting = accountUntilTime;
                if (settleUntilTime.before(accountUntilTime))
                    realTimeForAccounting = settleUntilTime;

                if(ClearingProcessType.ONLINE.equals(clearingProfile.getProcessType()) && considerClearingProcessType.equals(true)){
                    onlineProcess = true;
                    terminalCodes = findDesiredTerminalCodesBasedOnSettlementRecord(accountUntilTime, justToday, clearingProfile);

                }else{
                    terminalCodes = findDesiredTerminalCodes(realTimeForAccounting, justToday, clearingProfile);
                }
            }else {
                for (Terminal terminal : terminals) {
                    terminalCodes.add(terminal.getCode());
                }
//				terminals = null;

            }

            GeneralDao.Instance.endTransaction();

            List<Terminal> tmpTerminals = null;

            int counter = 0;
            int maxCounter = 100;

//			if(OnlineSettlementService.class.equals(clearingProfile.getSettlementClass())){
//				maxCounter = terminalCodes.size();
//			}

            List<Long> termForQuery = new ArrayList<Long>();

            for (int i = 0; i < terminalCodes.size(); i++) {
//				logger.info("Term:"+terminalCodes.get(i));
                if(terminalCodes.get(i) instanceof String)
                    termForQuery.add(Long.parseLong((String)terminalCodes.get(i)));
                else if(terminalCodes.get(i) instanceof Long)
                    termForQuery.add((Long)terminalCodes.get(i));
                else if(terminalCodes.get(i) instanceof Terminal)
                    termForQuery.add(((Terminal)terminalCodes.get(i)).getCode());
                counter++;
				
/*
				boolean isNopad = termForQuery.remove(223636L);
				
				if (isNopad) {
					logger.debug("Nopad is removed!");
					counter--;
				}


*/
                if(counter == maxCounter || i == terminalCodes.size() - 1) {
                    String termForQueryStr = ToStringBuilder.reflectionToString(termForQuery.toArray(), ToStringStyle.MULTI_LINE_STYLE);
                    termForQueryStr = termForQueryStr.substring(termForQueryStr.indexOf("{")+1, termForQueryStr.indexOf("}"));
                    logger.debug("About to account for terminals: " + termForQueryStr);
                    deleteFromSettlementRecord(clearingProfile, termForQuery);
                    List<Terminal> tt = null;
//					if(terminals != null){
//						tt = new ArrayList<Terminal>();
//						tt.addAll(terminals);
//					}
                    AccountingThread accountingThread = new AccountingThread(terminals, termForQuery, clearingProfile.getId(),
                            accountUntilTime, settleUntilTime, update, onlineProcess,
                            settleTime, considerClearingProcessType, onlyFanapAccount, lockAcq, semaphore, waitForSyncObject);
                    Thread thread = new Thread(accountingThread);
                    logger.debug("Thread: " + thread.getName() + " is starting...");
                    semaphore.acquire();
                    thread.start();

                    counter = 0;
                    termForQuery = new ArrayList<Long>();
                }
            }

            logger.info("Wait for all threads to finish...");
            semaphore.acquire(MAX_THREADS);
            logger.info("All threads finished...");
        } catch (Exception e) {
            logger.error("Encounter with an exception in terminal accounting..."+ e.getClass().getSimpleName()+": "+ e.getMessage());
            logger.info("Wait for all threads to finish (in exception)...");
            semaphore.acquire(MAX_THREADS);
            logger.info("All threads finished (in exception)...");
//			try {
//				GeneralDao.Instance.rollback();
//			} catch (Exception e1) {
//				logger.error(e1);
            GeneralDao.Instance.beginTransaction();
            if (lockAcq)
                SynchronizationService.release(clearingProfile, ClearingProfile.class);
            GeneralDao.Instance.endTransaction();
//				throw e1;
//			}
            throw e;
        }finally{

            GeneralDao.Instance.beginTransaction();
            if (lockAcq)
                SynchronizationService.release(clearingProfile, ClearingProfile.class);
            GeneralDao.Instance.endTransaction();
        }

        if (OnlineSettlementService.class.equals(clearingProfile.getSettlementClass()) && postPrep != null && !postPrep.isEmpty()) {
            for (Message m : postPrep) {
                logger.debug("Adding msg " + m + " type:" + ((ScheduleMessage) m).getMessageType());
            }

            MessageManager.getInstance().putRequests(postPrep);
        }

        logger.info("Ending Terminal Accounting...");
    }



    protected void updateToNowSettlementData(ClearingProfile clearingProfile, List<Terminal> terminals,
                                             DateTime accountUntilTime, DateTime settleUntilTime) {

        if (terminals == null || terminals.isEmpty() )
            return;

        List<SettlementDataType> types = ClearingService.getSettlementDataTypes(clearingProfile);
        AccountingService.updateSettlementData(clearingProfile, terminals, accountUntilTime, settleUntilTime, types);
    }

    protected Object postPrepareForSettlement(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate, Boolean onlyFanapAccount) {
        return null;
    }

    public Object  postPrepareForSettlement(List<SettlementData> settlementDatas) {
        return null;
    }

    public List<Long> prepareForSettlement(ClearingProfile clearingProfile, List<Terminal> terminals, DateTime accountUntilTime,
                                           DateTime settleUntilTime, Boolean settleTime, Boolean considerClearingProcessType, /*List<Long> trxSettleRecord,*/ Boolean waitForSyncObject) throws Exception {

        int i=0;
        int maxRecordSize = ConfigUtil.getInteger(ConfigUtil.GLOBAL_MAX_TRX_IN_ACCOUNTING_ITERATION);

        List<Long> nextRoundTerminal = new ArrayList<Long>();

        Boolean onlineProcess = false;
        if (ClearingProcessType.ONLINE.equals(clearingProfile.getProcessType()) && considerClearingProcessType.equals(true)) {
            onlineProcess = true;
        }

        DateTime realTimeForAccounting = accountUntilTime;
        if (settleUntilTime.before(accountUntilTime))
            realTimeForAccounting = settleUntilTime;

        boolean justToday = true;
        if (settleUntilTime.equals(accountUntilTime))
            justToday = false;

        Integer guaranteePeriod = 0;
        if(justToday){
            guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
        }else{
            guaranteePeriod = clearingProfile.getSettleGuaranteeDay();
        }

        List<SettlementDataType> types = ClearingService.getSettlementDataTypes(clearingProfile);

        List<Object> accState_NOT_COUNTED = new ArrayList<Object>();
        accState_NOT_COUNTED.add(AccountingState.NOT_COUNTED);

        List<Object> accState_COUNTED = new ArrayList<Object>();
        accState_COUNTED.add(AccountingState.COUNTED);

        List<Object> notToBeClrState = new ArrayList<Object>();
        notToBeClrState.add(ClearingState.DISAGREEMENT.getState());
        notToBeClrState.add(ClearingState.DISPUTE.getState());
        notToBeClrState.add(ClearingState.SUSPECTED_DISPUTE.getState());

        List<Object> toBePartiallyClrState = new ArrayList<Object>();
        toBePartiallyClrState.add(ClearingState.PARTIALLY_REVERSED.getState());


        for (SettlementDataType type: types) {

            SettlementDataCriteria criteria = ClearingService.getSettlementDataCriteria(clearingProfile, type);

            if (criteria==null || criteria.getCriteriaDatas()== null || criteria.getCriteriaDatas().isEmpty()){
                logger.info("There is no criteria data for settlementDataType: "+ clearingProfile.getId()+"."+ type);
                continue;
            }

            Map<Class, List<Object>> criteriaNameValues = ClearingService.getSeperateCriteriaByName(criteria.getCriteriaDatas());
            Set<Class> keySet = criteriaNameValues.keySet();

            Map<String, Object> baseParameters1 = new HashMap<String, Object>();
            String baseQuery1 = "";

            if (Boolean.TRUE.equals(clearingProfile.getHasFee())) {
                baseQuery1 = TransactionService.getBaseCriteria(realTimeForAccounting, baseParameters1, justToday, guaranteePeriod/*, trxSettleRecord*/, onlineProcess);

            } else {
                baseQuery1 = TransactionService.getBaseCriteriaNew(realTimeForAccounting, baseParameters1, justToday, guaranteePeriod/*, trxSettleRecord*/, onlineProcess);
            }

            LockObject lockObject = clearingProfile.getLockObject();

            for (Terminal terminal : terminals) {
                Boolean suspectedToHaveMoreData = false;
                if (terminal == null)
                    continue;
                i++;
                try {
                    if (LockObject.TERMINAL.equals(lockObject)) {
                        TerminalService.synchObject(terminal, waitForSyncObject);
                    }
                } catch (LockAcquisitionException e) {
                    logger.error("Exception in preparing for terminal: " + terminal.getCode() , e);
//					nextRoundTerminal.add(terminal.getCode());
                    continue;
                }

                if(terminal instanceof EPAYTerminal)
                    terminal = GeneralDao.Instance.load(EPAYTerminal.class, terminal.getCode());
                else if(terminal instanceof POSTerminal)
                    terminal = GeneralDao.Instance.load(POSTerminal.class, terminal.getCode());
                else
                    GeneralDao.Instance.refresh(terminal);

                logger.info("Preparing Settlement For terminal: " + terminal.getCode() + " ( "+i+" of "+terminals.size()+" )");
                if(terminal.getOwner() == null){
                    logger.info("No owner is defined for terminal....Returning...");
                    return nextRoundTerminal;
                }

                if (isDesiredOwnerForPreprocessing(terminal.getOwner())) {

                    String baseQuery = baseQuery1;
                    Map<String, Object> baseParameters = new HashMap<String, Object>(baseParameters1);
                    if (onlineProcess) {
                        String stlRecordTrx = TransactionService.addSettleRecordTransactionCriteria(realTimeForAccounting, terminal, baseParameters);
                        if (!Util.hasText(stlRecordTrx)) {
                            logger.info("No transaction is defined in settleRecord ....Continue...");
                            if (LockObject.TERMINAL.equals(lockObject)) {
                                SynchronizationService.release(terminal, terminal.getTerminalType().getClassType());
                            }
                            continue;
                        } else {
                            baseQuery += stlRecordTrx;
                        }

                    }

                    baseQuery += TransactionService.addTerminalCriteria(SettledState.NOT_SETTLED, terminal, baseParameters);

                    Class clearingState = null;
                    for (Class criteriaName: keySet) {
                        if (!criteriaName.equals(ClearingState.class))
                            baseQuery += ClearingService.addCriteriaQuery(baseParameters, terminal, criteriaName, criteriaNameValues.get(criteriaName));
                        else {
                            clearingState = criteriaName;
                        }
                    }

//					baseQuery = addDesiredCriteria(baseQuery, baseParameters, justToday, realTimeForAccounting, guaranteePeriod, terminal, settleUntilTime);
                    String toBeClrStateQuery = baseQuery;
                    Map<String, Object> toBeClrStateParameters = new HashMap<String, Object>(baseParameters);
                    if (clearingState != null) {
                        List<Object> clearingStateValues = criteriaNameValues.get(clearingState);
                        if ( (settleTime &&
                                (OnlineSettlementService.class.equals(clearingProfile.getSettlementClass()) ||
                                        PerTransactionSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()) ||
                                        OnlinePerTransactionSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass())))

                                ||

                                (!justToday &&
									/*(OnlineSettlementService.class.equals(clearingProfile.getSettlementClass()) ||
											PerTransactionSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()) ||
											OnlinePerTransactionSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass())) || */
                                        RequestBasedSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()))

                                ) {

                            logger.info("It is settlement time so Not_Cleared Trxs are added: "+ clearingProfile.getId()+"."+ type.getType());
                            clearingStateValues.add(ClearingState.NOT_CLEARED.getState());
                        }
                        toBeClrStateQuery += ClearingService.addCriteriaQuery(toBeClrStateParameters, terminal, clearingState, clearingStateValues);
                    }

                    toBeClrStateQuery += ClearingService.addCriteriaQuery(toBeClrStateParameters, terminal, AccountingState.class, accState_NOT_COUNTED);


                    List desiredMsgs;
                    if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
                        desiredMsgs = getResultCriteria(toBeClrStateQuery, toBeClrStateParameters, 0 , maxRecordSize, clearingProfile);
                    }else{
//						if(ClearingProcessType.ONLINE.equals(clearingProfile.getProcessType()) && considerClearingProcessType.equals(true)){
//							desiredMsgs = getResultCriteri(toBeClrStateQuery, toBeClrStateParameters, 0 , maxRecordSize);
//							
//						} else {
                        desiredMsgs = getResultCriteriaNew(toBeClrStateQuery, toBeClrStateParameters, 0 , maxRecordSize);
//						}
                    }

                    if (desiredMsgs.size()>= maxRecordSize)
                        suspectedToHaveMoreData = true;

                    String toBeRemovedClrStateQuery = baseQuery;
                    Map<String, Object> toBeRemovedClrStateParameters = new HashMap<String, Object>(baseParameters);
                    toBeRemovedClrStateQuery += ClearingService.addCriteriaQuery(toBeRemovedClrStateParameters, terminal, ClearingState.class, notToBeClrState);

                    toBeRemovedClrStateQuery += ClearingService.addCriteriaQuery(toBeRemovedClrStateParameters, terminal, AccountingState.class, accState_COUNTED);

                    List mustBeRemovedMsgs = new ArrayList<Ifx>();
                    if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
                        mustBeRemovedMsgs = getResultCriteria(toBeRemovedClrStateQuery, toBeRemovedClrStateParameters, 0, maxRecordSize, clearingProfile);
                    }else{
                        mustBeRemovedMsgs = getResultCriteriaNew(toBeRemovedClrStateQuery, toBeRemovedClrStateParameters, 0, maxRecordSize);
                    }
                    if (mustBeRemovedMsgs.size()>= maxRecordSize)
                        suspectedToHaveMoreData = true;

                    String toBePartiallyRemovedClrStateQuery = baseQuery;
                    Map<String, Object> toBePartiallyRemovedClrStateParameters = new HashMap<String, Object>(baseParameters);
                    toBePartiallyRemovedClrStateQuery += ClearingService.addCriteriaQuery(toBePartiallyRemovedClrStateParameters, terminal, ClearingState.class, toBePartiallyClrState);
//					accState = new ArrayList<Object>();
//					accState.add(AccountingState.COUNTED);
                    toBePartiallyRemovedClrStateQuery += ClearingService.addCriteriaQuery(toBePartiallyRemovedClrStateParameters, terminal, AccountingState.class, accState_COUNTED);

                    List mustBePartiallyRemovedMsgs;
                    if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
                        mustBePartiallyRemovedMsgs = getResultCriteria(toBePartiallyRemovedClrStateQuery, toBePartiallyRemovedClrStateParameters, 0, maxRecordSize, clearingProfile);
                    }else{
                        mustBePartiallyRemovedMsgs = getResultCriteriaNew(toBePartiallyRemovedClrStateQuery, toBePartiallyRemovedClrStateParameters, 0, maxRecordSize);
                    }

                    if (mustBePartiallyRemovedMsgs.size()>= maxRecordSize)
                        suspectedToHaveMoreData = true;

                    if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
                        doProcess(clearingProfile, settleUntilTime, terminal, type, desiredMsgs, settleTime);
                        removeProcess(clearingProfile, settleUntilTime, terminal, mustBeRemovedMsgs);
                        partiallyRemoveProcess(clearingProfile, settleUntilTime, terminal, mustBePartiallyRemovedMsgs);
                    }else{
                        doProcessNew(clearingProfile, settleUntilTime, terminal, type, desiredMsgs, settleTime);
                        removeProcessNew(clearingProfile, settleUntilTime, terminal, mustBeRemovedMsgs);
                        partiallyRemoveProcessNew(clearingProfile, settleUntilTime, terminal, mustBePartiallyRemovedMsgs);
                    }

                    if (suspectedToHaveMoreData && !nextRoundTerminal.contains(terminal.getCode()))
                        nextRoundTerminal.add(terminal.getCode());

                }

                if (LockObject.TERMINAL.equals(lockObject)) {
                    SynchronizationService.release(terminal, terminal.getTerminalType().getClassType());
                }
            }
        }

        return nextRoundTerminal;
    }

    private void partiallyRemoveProcess(ClearingProfile clearingProfile, DateTime settleUntilTime, Terminal terminal,
                                        List<Ifx> mustBePartiallyRemovedMsgs) {
        try {
            if (mustBePartiallyRemovedMsgs != null && mustBePartiallyRemovedMsgs.size() > 0)
                TransactionFinancialProcessor.partiallyRemoveProcess(terminal, clearingProfile,
                        TransactionService.getTransactionsFromIfx(mustBePartiallyRemovedMsgs), settleUntilTime);
        } catch (Exception e) {
            logger.error("Exception in removeProcess of terminal: " + terminal.getCode() + e, e);
        }
    }

    private void partiallyRemoveProcessNew(ClearingProfile clearingProfile, DateTime settleUntilTime, Terminal terminal,
                                           List<IfxSettlement> mustBePartiallyRemovedMsgs) {
        try {
            if (mustBePartiallyRemovedMsgs != null && mustBePartiallyRemovedMsgs.size() > 0)
                TransactionFinancialProcessor.partiallyRemoveProcess(terminal, clearingProfile,
                        TransactionService.getTransactionsFromIfxSettlement(mustBePartiallyRemovedMsgs), settleUntilTime);
        } catch (Exception e) {
            logger.error("Exception in removeProcess of terminal: " + terminal.getCode() + e, e);
        }
    }

    private void removeProcess(ClearingProfile clearingProfile, DateTime settleUntilTime, Terminal terminal,
                               List<Ifx> mustBeRemovedMsgs) {
        try {
            if (mustBeRemovedMsgs != null && mustBeRemovedMsgs.size() > 0){
                TransactionFinancialProcessor.removeProcess(terminal, clearingProfile,
                        TransactionService.getTransactionsFromIfx(mustBeRemovedMsgs), settleUntilTime);
            }
        } catch (Exception e) {
            logger.error("Exception in removeProcess of terminal: " + terminal.getCode() + e, e);
        }
    }

    private void removeProcessNew(ClearingProfile clearingProfile, DateTime settleUntilTime, Terminal terminal,
                                  List<IfxSettlement> mustBeRemovedMsgs) {
        try {
            if (mustBeRemovedMsgs != null && mustBeRemovedMsgs.size() > 0){
                TransactionFinancialProcessor.removeProcess(terminal, clearingProfile,
                        TransactionService.getTransactionsFromIfxSettlement(mustBeRemovedMsgs), settleUntilTime);
            }
        } catch (Exception e) {
            logger.error("Exception in removeProcess of terminal: " + terminal.getCode() + e, e);
        }
    }

    protected void doProcess(ClearingProfile clearingProfile, DateTime settleUntilTime, Terminal terminal,
                             SettlementDataType type, List<Ifx> desiredMsgs, Boolean settleTime) {
        try {
            if (desiredMsgs != null && desiredMsgs.size() > 0)
                TransactionFinancialProcessor.doProcess(terminal, clearingProfile, type, desiredMsgs, settleUntilTime, settleTime);
        } catch (Exception e) {
            logger.error("Exception in doProcess of terminal: " + terminal.getCode() + e, e);
        }
    }

    private void doProcessNew(ClearingProfile clearingProfile, DateTime settleUntilTime, Terminal terminal,
                              SettlementDataType type, List<IfxSettlement> desiredMsgs, Boolean settleTime) {
        try {
            if (desiredMsgs != null && desiredMsgs.size() > 0)
                TransactionFinancialProcessor.doProcessNew(terminal, clearingProfile, type, desiredMsgs, settleUntilTime, settleTime);
        } catch (Exception e) {
            logger.error("Exception in doProcess of terminal: " + terminal.getCode() + e, e);
        }
    }

//	protected String addDesiredCriteria(String toBeClrStateQuery, Map<String, Object> toBeClrStateParameters, boolean justToday, DateTime untilTime, Integer guaranteePeriod, Terminal terminal, DateTime settleUntilTime) {
//		
//		if (untilTime != null) {
//			if (justToday) {
//				if(guaranteePeriod != null && guaranteePeriod < 0){
//					DateTime guaranteeTime = untilTime.clone();
//					guaranteeTime.increase(guaranteePeriod);					
//					toBeClrStateQuery +=  " and i.receivedDtLong between "+guaranteeTime.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
//
//				}else{
//					DateTime today = untilTime.clone();
//					today.setDayTime(new DayTime(0, 0, 0));
//					toBeClrStateQuery += " and i.receivedDtLong between "+today.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
//				}
//			} else {
//				DateTime untilDate2 = new DateTime(untilTime.getDayDate().nextDay(guaranteePeriod), untilTime.getDayTime());
//				
//				DateTime fromDate = untilDate2.clone();
//				fromDate.increase(1);
//				fromDate.setDayTime(new DayTime(0, 0, 0));
//				
//				DateTime now = DateTime.now();
//				DateTime toDate = untilTime;
//				if (toDate.after(now))
//					toDate = now;
//				
//				toBeClrStateQuery +=  " and i.receivedDtLong between "+fromDate.getDateTimeLong()+" and "+toDate.getDateTimeLong();
//			}
//		}
//		return toBeClrStateQuery;
//	}

    protected List<Ifx> getResultCriteria(String query, Map<String, Object> Params,int firstResult, int maxResults, ClearingProfile clearingProfile) {
        return TransactionService.getResultCriteria(query, Params, firstResult, maxResults);
    }

    protected List<IfxSettlement> getResultCriteriaNew(String query, Map<String, Object> Params,int firstResult, int maxResults) {
        return TransactionService.getResultCriteriaNew(query, Params, firstResult, maxResults);
    }

	/*	protected List<Message> getDesiredMessages(List<ClearingState> toBeClrState, List<TrnType> toBeClrTrx, List<TerminalType> termTypes,
			AccountingState accountingState, SettledState settledState, Terminal terminal, DateTime realTimeForAccounting) {
		return getTransactionService().getDesiredMessages(toBeClrState, toBeClrTrx, termTypes, accountingState, settledState, 
				terminal, realTimeForAccounting, null, true);
	}*/

    public void settleTransactions(ClearingProfile clearingProfile, DateTime settleDate) {
        DateTime currentTime = DateTime.now();

        List<String> srcDestList = getSrcDest();

        if (srcDestList != null && srcDestList.size() > 0) {
            for (String srcDest: srcDestList) {

                String query = "update SettlementInfo s set "
                        + " s.settledState = :settledState "
                        + " , "
                        + " s.settledDate.dayDate = :settledDate "
                        + " , "
                        + " s.settledDate.dayTime = :settledTime "
                        + " where "
//						+ " s.settledState = :notSettled " 
//						+ " and " 
                        + " s in (select t." + srcDest + "SettleInfo from Transaction t where "
                        + " t." + srcDest + "SettleInfo.accountingState = :counted "
                        + " and t." + srcDest + "SettleInfo.settledState = :notSettled "
                        + " and t." + srcDest + "SettleInfo.settlementData.settlementState.state in (:states) "
                        + " and t." + srcDest + "SettleInfo.settlementData.settlementState.clearingProfile = :clearingProfile "
                        + " and t." + srcDest + "SettleInfo.settlementData.settlementState.settlementFileCreationDate.dayDate > :stlDate"
//						+ " and t." + srcDest + "SettleInfo.settlementData.settlementState.state is not null "
//						+ " and t." + srcDest + "SettleInfo.settlementData in (:stlDatas)"
                        + ")";


                Map<String, Object> params = new HashMap<String, Object>();
                params.put("settledDate", currentTime.getDayDate());
                params.put("settledTime", currentTime.getDayTime());
                params.put("settledState", SettledState.SENT_FOR_SETTLEMENT);
                params.put("counted", AccountingState.COUNTED);
                params.put("notSettled", SettledState.NOT_SETTLED);
                params.put("states", SettlementStateType.getAllStates());
                params.put("clearingProfile", clearingProfile);
                params.put("stlDate" , currentTime.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()));
                //			params.put("stlDatas", settlementDatas);
                int count = getGeneralDao().executeUpdate(query, params);
                logger.debug("Num trx_settlement_flag update: "+count);
            }
        }
    }

    abstract public List<String> getSrcDest();

    abstract public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate)
            throws Exception;

    protected void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc, DateTime settleDate) throws Exception {
        ReportGenerator.generateDocumentSettlementState(clearingProfile, docDesc, /*settleDate,*/ false);
    }

    public void generateDocumentSettlementState(List<SettlementState> settlementStates) throws Exception {
        ReportGenerator.generateDocumentSettlementState(settlementStates, getSettlementTypeDesc(), false, true);
    }

    public Account getAccount(SettlementData settlementData) {
        FinancialEntity entity = settlementData.getFinancialEntity();
        //TODO: Leila check it!
        return entity.getOwnOrParentAccount();
    }

    public String getBranchCode(String accountId, AccountType accType, SettlementData settlementData) {//honarmand koisk beCarefull
        /**** set merchant branch code ****/

        String branchCode = "995";

        try {
            if (AccountType.ACCOUNT.equals(accType)) {

                String[] split = accountId.split("\\-");
                if (Util.hasText(split[1]))
                    branchCode = split[1];

            } else {
                String[] split = accountId.split("\\.");
                if (Util.hasText(split[0]))
                    branchCode = split[0];

            }
        } catch (Exception e) {
            logger.info("exception in getting branchCode, set 995");
            branchCode = "995";
        }

        return branchCode;
    }

    public abstract String getSettlementTypeDesc();

    abstract boolean isDesiredOwnerForPreprocessing(FinancialEntity entity);

    abstract List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile);

    abstract List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile);

    protected List<Terminal> findAllTerminalsBasedOnSettlementRecord(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
        return findAllTerminals(terminals, termCodes, clearingProfile);
    }

    protected  List findDesiredTerminalCodesBasedOnSettlementRecord(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
        return findDesiredTerminalCodes(accountUntilTime, justToday, clearingProfile);
    }

//	abstract List<Terminal> findAllTerminalsBasedOnSettlementRecord(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile);
//	abstract List findDesiredTerminalCodesBasedOnSettlementRecord(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile);

    abstract List findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile);


    public GeneralDao getGeneralDao() {
        return GeneralDao.Instance;
    }

    public void deleteFromSettlementRecord(ClearingProfile clearingProfile, List terminals){}

}
