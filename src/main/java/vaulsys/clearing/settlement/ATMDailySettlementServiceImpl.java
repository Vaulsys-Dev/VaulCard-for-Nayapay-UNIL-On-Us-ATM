package vaulsys.clearing.settlement;

import com.fanap.cms.exception.BusinessException;
import com.fanap.cms.valueobjects.corecommunication.DepositInfoForIssueDocument;
import com.fanap.cms.valueobjects.corecommunication.DocumentItemEntity;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DepositActionType;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DocumentItemEntityType;
import com.ghasemkiani.util.icu.PersianDateFormat;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.*;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.customer.Account;
import vaulsys.customer.Core;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.*;
import vaulsys.util.NotUsed;
import vaulsys.util.Pair;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.hibernate.exception.LockAcquisitionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ATMDailySettlementServiceImpl extends SettlementService {
    private static final Logger logger = Logger.getLogger(ATMDailySettlementServiceImpl.class);
    private static final int MAX_COUNTER = 100;

    private ATMDailySettlementServiceImpl() {
    }

    public static final ATMDailySettlementServiceImpl Instance = new ATMDailySettlementServiceImpl();

    @Override
    public void account(List<Terminal> terminals, ClearingProfile clearingProfile,
                        DateTime accountUntilTime, DateTime settleUntilTime
            , Boolean update, Boolean waitForSyncObject, Boolean onlyFanapAccount, Boolean settleTime, Boolean considerClearingProcessType) throws Exception {

        logger.info("Starting Terminal Accounting...");
        GeneralDao.Instance.beginTransaction();
        GeneralDao.Instance.refresh(clearingProfile);

        try {
            boolean justToday = true;
            if (settleUntilTime.equals(accountUntilTime))
                justToday = false;

            List terminalCodes = new ArrayList();
            if (terminals == null || terminals.isEmpty()) {
                terminalCodes.addAll(findDesiredTerminalCodes(accountUntilTime, justToday, clearingProfile));
            } else {
                for (Terminal terminal : terminals) {
                    terminalCodes.add(terminal.getCode());
                }
                terminals = null;
            }

            GeneralDao.Instance.endTransaction();

            List<Terminal> tmpTerminals;
            List<Terminal> freeTerminals;

            int counter = 0;

            List<Long> termForQuery = new ArrayList<Long>();
            for (int i = 0; i < terminalCodes.size(); i++) {
//				logger.info("Term:" + terminalCodes.get(i));
                if (terminalCodes.get(i) instanceof String)
                    termForQuery.add(Long.parseLong((String) terminalCodes.get(i)));
                else if (terminalCodes.get(i) instanceof Long)
                    termForQuery.add((Long) terminalCodes.get(i));
                else if (terminalCodes.get(i) instanceof Terminal)
                    termForQuery.add(((Terminal) terminalCodes.get(i)).getCode());
                counter++;

                if (counter == MAX_COUNTER || i == terminalCodes.size() - 1) {
                    logger.debug("About to account for terminals: " + ToStringBuilder.reflectionToString(termForQuery.toArray(), ToStringStyle.MULTI_LINE_STYLE));
                    if (!termForQuery.isEmpty()) {
                        GeneralDao.Instance.beginTransaction();
                        GeneralDao.Instance.refresh(clearingProfile);

                        logger.info("terminalCodes: " + terminalCodes.size() + " termForQuery: " + termForQuery.size());
                        tmpTerminals = findAllTerminals(terminals, termForQuery, clearingProfile);

                        List<Terminal> notFree = new ArrayList<Terminal>();
                        freeTerminals = new ArrayList<Terminal>();

                        if (tmpTerminals != null && tmpTerminals.size() > 0) {
                            logger.info(tmpTerminals.size() + " terminal with clrProfile: " + clearingProfile.getId());
                            for (Terminal atm : tmpTerminals) {
                                try {
//									if (waitForSyncObject.equals(true)) {
//										SynchronizationService.getSynchornizationObject((ATMTerminal) atm,ATMTerminal.class, LockMode.UPGRADE);
//									} else {
//										SynchronizationService.getSynchornizationObject((ATMTerminal) atm,ATMTerminal.class, LockMode.UPGRADE_NOWAIT);
//									}
//									SynchronizationService.lock(atm, ATMTerminal.class);
                                    GeneralDao.Instance.refresh(atm);

                                    List<Terminal> atmList = new ArrayList<Terminal>();
//									List<Long> trxSettlemRecordList = new ArrayList<Long>();
                                    atmList.add(atm);
                                    prepareForSettlement(clearingProfile, atmList, accountUntilTime, settleUntilTime, settleTime, considerClearingProcessType/*, trxSettlemRecordList*/, waitForSyncObject);

//									SynchronizationService.release(atm, ATMTerminal.class);

                                    GeneralDao.Instance.endTransaction();
                                    GeneralDao.Instance.beginTransaction();
                                    GeneralDao.Instance.refresh(clearingProfile);
                                    freeTerminals.add(atm);
                                } catch (Exception e) {
                                    logger.error("ATM Terminal " + atm.getId() + " is busy now! "
                                            + e.getClass().getSimpleName() + ": " + e.getMessage());
                                    notFree.add(atm);
                                    GeneralDao.Instance.rollback();
                                    GeneralDao.Instance.beginTransaction();
                                    GeneralDao.Instance.refresh(clearingProfile);
                                }
                            }
                        }

                        /********* Leila ************/
/*						if (freeTerminals != null && freeTerminals.size() > 0) {
							try {
								logger.info("Preparing Settlement...");
								GeneralDao.Instance.refresh(clearingProfile);
									List<Message> tmpRes = (List<Message>) 
								postPrepareForSettlement(clearingProfile, settleUntilTime, freeTerminals);

								if (update)
									updateToNowSettlementData(clearingProfile, freeTerminals, accountUntilTime, settleUntilTime);
							} catch (Exception e) {
								logger.error(e);
								GeneralDao.Instance.rollback();
								GeneralDao.Instance.beginTransaction();
								GeneralDao.Instance.endTransaction();
								throw e;
							}
						} 
*/                        /********* Leila ************/
                        GeneralDao.Instance.endTransaction();
                    }
                    counter = 0;
                    termForQuery = new ArrayList<Long>();
                }
            }

        } catch (Exception e) {
            logger.error("Encounter with an exception in terminal accounting..." + e.getClass().getSimpleName() + ": "
                    + e.getMessage());
            try {
                GeneralDao.Instance.rollback();
            } catch (Exception e1) {
                logger.error(e1);
//				throw e1;
            }
            throw e;
        } finally {
        }

        logger.info("Ending Terminal Accounting...");
    }

    @NotUsed
    @Override
    List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
        if (terminals == null)
            terminals = new ArrayList<Terminal>();
        List<ATMTerminal> atmTerminals = TerminalService
                .findAllTerminals(terminals, ATMTerminal.class, clearingProfile);
        if (atmTerminals != null && atmTerminals.size() > 0)
            terminals.addAll(atmTerminals);
        return terminals;
    }

    @Override
    List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
        if (terminals == null)
            terminals = new ArrayList<Terminal>();
        List<ATMTerminal> atmTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes,
                ATMTerminal.class, clearingProfile);
        if (atmTerminals != null && atmTerminals.size() > 0)
            terminals.addAll(atmTerminals);
        return terminals;
    }

    @Override
    List<String> findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
        List<String> terminals = new ArrayList<String>();
        List<String> atmTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(ATMTerminal.class, accountUntilTime, justToday, clearingProfile.getSettleGuaranteeDay());
        if (atmTerminals != null && atmTerminals.size() > 0)
            terminals.addAll(atmTerminals);
        return terminals;
    }

    public void postPrepareForSettlement(ClearingProfile clearingProfile, DateTime settleDate, Boolean update, List<Terminal> terminals, DateTime accountUntilTime) throws Exception {
        List<SettlementData> settlementDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate, terminals);
        if (update) {
            try {
                generateATMDesiredSettlementReports(settlementDatas, update, terminals, settleDate, accountUntilTime, clearingProfile);
            } catch (Exception e) {
                logger.error("Error was occured in ATM Desired Settlement Reports. (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")", e);
            }
        } else {
            postPrepareForSettlement(settlementDatas);
        }
    }

    @Override
    public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
        List<SettlementData> settlementDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, settleDate);

//		for (SettlementData settlementData : settlementDatas) {
        try {
            generateATMDesiredSettlementReports(settlementDatas);
        } catch (Exception e) {
            logger.error("Error was occured in ATMDaily Settlement. (" + e.getClass().getSimpleName() + ": "
                    + e.getMessage() + ")");
        }

//		}
    }

    @Override
    public String getSettlementTypeDesc() {
        return "خودپردازها";
    }

    @Override
    public List<String> getSrcDest() {
        List<String> result = new ArrayList<String>();
        result.add("source");
        return result;
    }

    @Override
    boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
        return FinancialEntityRole.BRANCH.equals(entity.getRole());
    }

    @Override
    public void settle(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleUntilTime,
                       Boolean update, Boolean settleTime, Boolean generateSettleState, Boolean considerClearingProcessType) {
        // A trick to settle in case that we encounter an exception in
        // accounting step
        // We will try for maxTries times, if error exist then we do not proceed
        // settlement process
        int numTries = 0;
        int maxTries = 3;
        boolean isFinishedAccounting = false;

//		if (!OnlineSettlementService.class.equals(clearingProfile.getSettlementClass())) {
        while (numTries < maxTries && !isFinishedAccounting) {
            try {
                account(terminals, clearingProfile, settleUntilTime, settleUntilTime, update, true, false, false, false);
                isFinishedAccounting = true;
            } catch (LockAcquisitionException e) {
                logger.error("Exception in accounting. LockAcquisitionException: " + numTries + " ", e);
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e1) {
                    continue;
                }
            } catch (Exception e) {
                logger.error("Exception in accounting. numTries: " + numTries + " ", e);
                numTries++;
            }
        }
//		}
        if (!isFinishedAccounting) {
            logger.error("We faced to maxTries Exception in accounting, so we don't proceed in settlement...");
            return;
        }

        DateTime settleDate = settleUntilTime;

        try {

            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.refresh(clearingProfile);

            logger.info("Generating Desired Terminal Settlement Report...");
            try {

                postPrepareForSettlement(clearingProfile, settleDate, update, terminals, DateTime.now());

            } catch (Exception e) {
                logger.error("Exception in Generating Desired Terminal Settlement Report  " + e);
            }
            GeneralDao.Instance.endTransaction();


            logger.info("Generating Settlement Data Report...");
            try {
                ReportGenerator.generateSettlementDataReport(terminals, clearingProfile, settleDate);
            } catch (Exception e) {
                logger.error("Exception in Generating Settlement Data Report " + e);
            }

            if (!update) {
                GeneralDao.Instance.beginTransaction();

                logger.info("Generating Settlement State Report...");
                try {
                    generateSettlementStateAndReport(terminals, clearingProfile, settleDate, getSettlementTypeDesc());
                } catch (Exception e) {
                    logger.error("Exception in Generating Settlement State Report, must be rollback beacuase incorrect SettlementState created! "
                            + e);
                    GeneralDao.Instance.rollback();
                    return;
                }

                GeneralDao.Instance.endTransaction();
            }

            GeneralDao.Instance.beginTransaction();

            GeneralDao.Instance.refresh(clearingProfile);

//			logger.info("Generating Desired Terminal Settlement Report...");
//			try {
//				
//				postPrepareForSettlement(clearingProfile, settleDate, update, terminals, DateTime.now());
//				
//			} catch (Exception e) {
//				logger.error("Exception in Generating Desired Terminal Settlement Report  " + e);
//			}

            if (!update) {
                logger.info("Put Flag on SettlementState...");
                try {
                    putFlagOnSettlementState(clearingProfile);
                } catch (Exception e) {
                    logger.error("Exception in Put Flag on SettlementState " + e);
                }
            }

            if (!update) {
                logger.info("Puting Settle Flag...");
//				settleTransactions(clearingProfile, settleDate);
                logger.info("End of Put Settle Flag.");
            }


        } catch (Exception e) {
            logger.error(e);
            GeneralDao.Instance.rollback();
            return;
        }
        GeneralDao.Instance.endTransaction();
    }

    private void putFlagOnSettlementState(ClearingProfile clearingProfile) {
        List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, null);
        for (SettlementState settlementState : settlementStates) {
            if (settlementState != null /*&& AccountingService.isAllSettlementDataSettled(settlementState)*/) {
                settlementState.setState(SettlementStateType.AUTOSETTLED);
                DateTime now = DateTime.now();
                settlementState.setSettlementFileCreationDate(now);
                settlementState.setSettlementDate(now);
                settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
                GeneralDao.Instance.saveOrUpdate(settlementState);
            }
        }
    }

    @Override
//	@NotUsed
    public void generateDocumentSettlementState(List<SettlementState> settlementStates) throws Exception {
        List<SettlementData> settlementDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(settlementStates);
        postPrepareForSettlement(settlementDatas);
    }

    @Override
    protected void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc, DateTime settleDate)
            throws Exception {
        List<SettlementData> settlementDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(
                clearingProfile, settleDate);

//		for (SettlementData settlementData : settlementDatas) {
        try {
            generateATMDesiredSettlementReports(settlementDatas);
        } catch (Exception e) {
            logger.error("Error was occured in ATMDaily Settlement. (" + e.getClass().getSimpleName() + ": "
                    + e.getMessage() + ")");
        }

//		}
    }

//	@Override
//	protected Object postPrepareForSettlement(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate, Boolean onlyFanapAccount) {
//		List<SettlementData> settlementDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(
//				clearingProfile, settleDate, terminals);
//		return postPrepareForSettlement(settlementDatas);
//	}

//	private Object postPrepareForSettlement(ClearingProfile clearingProfile, DateTime settleDate, List<Terminal> terminals) {
//		List<SettlementData> settlementDatas = AccountingService.findAllNotSettledATMSettlementDataUntilTime(
//				clearingProfile, settleDate, terminals);
//		return postPrepareForSettlement(settlementDatas);
//	}

    @Override
    public Object postPrepareForSettlement(List<SettlementData> settlementDatas) {
        try {
            generateATMDesiredSettlementReports(settlementDatas);
        } catch (Exception e) {
            logger.error("Error was occured in ATM Desired Settlement Reports. (" + e.getClass().getSimpleName() + ": "
                    + e.getMessage() + ")", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public void generateATMDesiredSettlementReports(List<SettlementData> settlementDatas) throws Exception {
//		for (SettlementData settlementData : settlementDatas) {
        try {
            generateATMDesiredSettlementReports(settlementDatas, false, null, null, null, null);
        } catch (Exception e) {
            logger.error("Error was occured in ATM Settlement. (" + e.getClass().getSimpleName() + ": "
                    + e.getMessage() + ")");
        }
//		}
    }

    public void generateATMDesiredSettlementReports(List<SettlementData> settlementDatas, Boolean update, List<Terminal> terminals,
                                                    DateTime settleDate, DateTime accountUntilTime, ClearingProfile clearingProfile) throws Exception {
        //Here
        SettlementData[] sortedSettlementData = new SettlementData[settlementDatas.size()];
        settlementDatas.toArray(sortedSettlementData);
        Arrays.sort(sortedSettlementData, new Comparator<SettlementData>() {
            @Override
            public int compare(SettlementData arg0, SettlementData arg1) {
                if (arg0.getId() > arg1.getId())
                    return 1;
                if (arg0.getId() < arg1.getId())
                    return -1;

                return 0;
            }
        });

        for (SettlementData settlementData : sortedSettlementData) {
            try {
                generateReportDocument(settlementData, update, terminals, settleDate, accountUntilTime, clearingProfile);
//				generateATMDesiredSettlementReports(settlementData);
            } catch (Exception e) {
                logger.error("Error was occured in ATM Settlement. (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
            }
        }

        GeneralDao.Instance.endTransaction();
        issueATMFCB(sortedSettlementData);
        GeneralDao.Instance.beginTransaction();
    }

    private void issueATMFCB(SettlementData[] sortedSettlementData) {
        IssueATMFCBThread issueFCBThread = new IssueATMFCBThread(sortedSettlementData);
        Thread issueThread = new Thread(issueFCBThread);
        logger.debug("Thread: " + issueThread.getName() + " is starting...");
        issueThread.start();
    }

    private void generateReportDocument(SettlementData settlementData, Boolean update, List<Terminal> terminals, DateTime settleDate, DateTime accountUntilTime, ClearingProfile clearingProfile) throws BusinessException {
        if (settlementData.getSettlementReport() != null) {
            logger.error("settlementData: " + settlementData.getId() + " has been report: "
                    + settlementData.getSettlementReport().getId() + " !!!");
            return;
        }

        try {
            logger.debug("Try to lock settlementData " + settlementData.getId());
            settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
            logger.debug("settlementData locked.... " + settlementData.getId());
        } catch (Exception e) {
            logger.error("Encounter an exception to lock settlementData", e);
        }

        if (settlementData == null)
            return;

        if (update) {
//			settlementData.setSettlementTimeLong(DateTime.now().getDateTimeLong());
            settlementData.setSettlementTime(DateTime.now());
//			updateToNowSettlementData(settlementData);
//			updateToNowSettlementData(clearingProfile, terminals, accountUntilTime, settleDate);
        }

        DepositActionType actionType = DepositActionType.Debtor_Deposit;
        DepositInfoForIssueDocument deposit;
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String commentOfDocumentItem = "";

        logger.info("settlementData: " + settlementData.getId());
        FinancialEntity entity = settlementData.getFinancialEntity();
        logger.info("entity: " + entity.getId());
        Terminal terminal = settlementData.getTerminal();
        logger.info("terminal: " + terminal.getId());
        String persianDate = " مورخ " + dateFormatPers.format(settlementData.getSettlementTime().toDate()) + " ساعت "
                + settlementData.getSettlementTime().getDayTime();

        logger.info("persianDate : " + persianDate);
        String clrProfDesc = "";
        clearingProfile = settlementData.getClearingProfile();

        if (clearingProfile != null) {
            clrProfDesc = ClearingService.getDocDesc(settlementData);
        }

        logger.info("clrProfDesc : " + clrProfDesc);

        commentOfDocumentItem = Util.ansiFormat(clrProfDesc + persianDate);

        actionType = DepositActionType.Debtor_Box;
        deposit = new DepositInfoForIssueDocument(null, actionType, new Double(settlementData
                .getTotalSettlementAmount()), commentOfDocumentItem);

        // -------------------
        DocumentItemEntityType topic;
        ATMTerminal atm = GeneralDao.Instance.load(ATMTerminal.class, settlementData.getTerminalId());

        String atmBranchId = /*((ATMTerminal) settlementData.getTerminal())*/atm.getOwner().getCoreBranchCode();

        logger.info("ATMBranchId : " + atmBranchId);

		Account fanapAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
        /*logger.info("fanapAccount ProcessContext.get().getMyInstitution().getAccount() :");
        logger.info(ProcessContext.get().getMyInstitution().getAccount());*/
        //Account fanapAccount = ProcessContext.get().getMyInstitution().getAccount();
//		String switchBranchId = CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchBranchId);

        if (AccountingService.isTopic(fanapAccount.getAccountNumber()))
            topic = DocumentItemEntityType.Topic;
        else
            topic = DocumentItemEntityType.Account;

        DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(settlementData
                .getTotalSettlementAmount()), true, atmBranchId, commentOfDocumentItem, fanapAccount
                .getAccountNumber(), topic);
        // -------------------
        IssueGeneralDocVO issueGeneralDocVO = new IssueGeneralDocVO(commentOfDocumentItem);
        issueGeneralDocVO.getDeposits().add(deposit);
        issueGeneralDocVO.getEntities().add(documentItemEntity);

        try {
            String username = "switch,ATM" + settlementData.getTerminal().getCode();
            String password = "ATM" + settlementData.getTerminal().getCode();
            List<DepositInfoForIssueDocument> depositList = new ArrayList<DepositInfoForIssueDocument>();
            depositList.add(deposit);
            List<DocumentItemEntity> documentItemEntityList = new ArrayList<DocumentItemEntity>();
            documentItemEntityList.add(documentItemEntity);
            List<Transaction> trx = new ArrayList<Transaction>();
            trx.addAll(settlementData.getTransactions());

            Pair<String, String> document = AccountingService.generateFCBDocumentByUserName(commentOfDocumentItem,
                    documentItemEntityList, depositList, atmBranchId, username, password, "stlData-"
                    + settlementData.getId());
            SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);
            settlementData.setSettlementReport(report);
            getGeneralDao().saveOrUpdate(settlementData);
            getGeneralDao().saveOrUpdate(report);

        } catch (BusinessException e) {
            logger.error("Encounter exception as issuing document. (" + e.getClass().getSimpleName() + ": "
                    + e.getMessage());
            logger.error(e);
            throw new BusinessException(e);
        }
    }

    protected void updateToNowSettlementData(SettlementData settlementData) {

        AccountingService.updateSettlementData(settlementData);
    }

    /*private void generateATMDesiredSettlementReports(SettlementData settlementData) throws Exception {

         SettlementReport report = settlementData.getSettlementReport();
         if (report != null)
             return;


         try {
             logger.debug("Try to lock settlementData " + settlementData.getId());
             settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
             logger.debug("settlementData locked.... " + settlementData.getId());
         } catch (Exception e) {
             logger.error("Encounter an exception to lock settlementData", e);
         }


         DepositActionType actionType = DepositActionType.Debtor_Deposit;
         DepositInfoForIssueDocument deposit;
         PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
         String commentOfDocumentItem = "";

         if (settlementData == null)
             return;

         logger.debug("settlementData: " + settlementData.getId());
         FinancialEntity entity = settlementData.getFinancialEntity();
         logger.debug("entity: " + entity.getId());
         Terminal terminal = settlementData.getTerminal();
         logger.debug("terminal: " + terminal.getId());
         String persianDate = " مورخ " + dateFormatPers.format(settlementData.getSettlementTime().toDate()) + " ساعت "
                 + settlementData.getSettlementTime().getDayTime();

         String clrProfDesc = "";
         ClearingProfile clearingProfile = settlementData.getClearingProfile();
         if (clearingProfile != null) {
             clrProfDesc = ClearingService.getDocDesc(settlementData);
         }

         commentOfDocumentItem = Util.ansiFormat(clrProfDesc + persianDate);

         actionType = DepositActionType.Debtor_Box;
         deposit = new DepositInfoForIssueDocument(null, actionType, new Double(settlementData
                 .getTotalSettlementAmount()), commentOfDocumentItem);

         // -------------------
         DocumentItemEntityType topic;
         String atmBranchId = ((ATMTerminal) settlementData.getTerminal()).getOwner().getCoreBranchCode();

         Account fanapAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
 //		String switchBranchId = CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchBranchId);

         if (AccountingService.isTopic(fanapAccount.getAccountNumber()))
             topic = DocumentItemEntityType.Topic;
         else
             topic = DocumentItemEntityType.Account;

         DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(settlementData
                 .getTotalSettlementAmount()), true, atmBranchId, commentOfDocumentItem, fanapAccount
                 .getAccountNumber(), topic);
         // -------------------
         IssueGeneralDocVO issueGeneralDocVO = new IssueGeneralDocVO(commentOfDocumentItem);
         issueGeneralDocVO.getDeposits().add(deposit);
         issueGeneralDocVO.getEntities().add(documentItemEntity);

         try {
             String username = "switch,ATM" + settlementData.getTerminal().getCode();
             String password = "ATM" + settlementData.getTerminal().getCode();
             List<DepositInfoForIssueDocument> depositList = new ArrayList<DepositInfoForIssueDocument>();
             depositList.add(deposit);
             List<DocumentItemEntity> documentItemEntityList = new ArrayList<DocumentItemEntity>();
             documentItemEntityList.add(documentItemEntity);
             Pair<String, String> document = AccountingService.generateFCBDocument(commentOfDocumentItem,
                     documentItemEntityList, depositList, atmBranchId, username, password, "stlData-"
                             + settlementData.getId());
             report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);
             settlementData.setSettlementReport(report);
             getGeneralDao().saveOrUpdate(settlementData);
             getGeneralDao().saveOrUpdate(report);

         } catch (BusinessException e) {
             logger.error("Encounter exception as issuing document. (" + e.getClass().getSimpleName() + ": "
                     + e.getMessage());
             logger.error(e);
             throw new BusinessException(e);
         }
         String transactionId = AccountingService.issueFCBDocument(report, true);
         report.setDocumentNumber(transactionId);
         getGeneralDao().saveOrUpdate(report);

         logger.debug("trans-id = " + transactionId);
         // -------------
         settlementData.setDocumentNumber(transactionId);
         getGeneralDao().saveOrUpdate(settlementData);
     }*/

    public void generateATMReturnedReport(List<SettlementInfo> settlementInfoList) throws Exception {
        SettlementInfo settlementInfo = settlementInfoList.get(0);

        if (TransactionService.getReturnedSettlementInfo(settlementInfo.getTransaction()))
            return;

        SettlementData settlementData = settlementInfo.getSettlementData();
        SettlementReport report = null;

        if (settlementData == null || settlementData.getSettlementReport() == null)
            return;

        DepositActionType actionType = DepositActionType.Creditor_Deposit;
        DepositInfoForIssueDocument deposit;
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String commentOfDocumentItem = "";

        logger.debug("Trying to return transaction: " + settlementInfo.getTransaction().getId());
        FinancialEntity entity = settlementData.getFinancialEntity();
        logger.debug("entity: " + entity.getId());
        Terminal terminal = settlementData.getTerminal();
        logger.debug("terminal: " + terminal.getId());

        SettlementInfo returnedSettlementInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.COUNTED,
                DateTime.now(), settlementInfo.getTransaction());
        SettlementData returnedSettlementData = new SettlementData(settlementData.getFinancialEntity(), settlementData
                .getTerminal(), settlementData.getClearingProfile(), SettlementDataType.RETURNED, DateTime.now());
        returnedSettlementData.addSettlementInfo(returnedSettlementInfo);
        returnedSettlementData.setNumTransaction(1);
        returnedSettlementData.setTotalAmount(settlementInfo.getTotalAmount());
        returnedSettlementData.setTotalFee(settlementInfo.getTotalFee());
        returnedSettlementData.setTotalSettlementAmount(settlementInfo.getTotalAmount() - settlementInfo.getTotalFee());
        getGeneralDao().saveOrUpdate(returnedSettlementInfo);
        getGeneralDao().saveOrUpdate(returnedSettlementData);

        String persianDate = " مورخ " + dateFormatPers.format(returnedSettlementData.getSettlementTime().toDate())
                + " ساعت " + returnedSettlementData.getSettlementTime().getDayTime();

        String clrProfDesc = "";
        ClearingProfile clearingProfile = returnedSettlementData.getClearingProfile();
        if (clearingProfile != null) {
            clrProfDesc = ClearingService.getDocDesc(returnedSettlementData);
        }

        commentOfDocumentItem = Util.ansiFormat(clrProfDesc + persianDate);

        actionType = DepositActionType.Creditor_Box;
        deposit = new DepositInfoForIssueDocument(null, actionType, new Double(returnedSettlementData
                .getTotalSettlementAmount()), commentOfDocumentItem);

        // -------------------
        DocumentItemEntityType topic;

//		Account fanapAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
        Account fanapAccount = ProcessContext.get().getMyInstitution().getAccount();
        String switchBranchId = CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchBranchId);

        if (AccountingService.isTopic(fanapAccount.getAccountNumber()))
            topic = DocumentItemEntityType.Topic;
        else
            topic = DocumentItemEntityType.Account;

        boolean debtor = false;

        DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(returnedSettlementData
                .getTotalSettlementAmount()), debtor, switchBranchId, commentOfDocumentItem, fanapAccount
                .getAccountNumber(), topic);
        // -------------------
        IssueGeneralDocVO issueGeneralDocVO = new IssueGeneralDocVO(commentOfDocumentItem);
        issueGeneralDocVO.getDeposits().add(deposit);
        issueGeneralDocVO.getEntities().add(documentItemEntity);

        try {
            String username = "switch,ATM" + returnedSettlementData.getTerminal().getCode();
            String password = "ATM" + returnedSettlementData.getTerminal().getCode();
            ATMTerminal atm = GeneralDao.Instance.load(ATMTerminal.class, returnedSettlementData.getTerminalId());
            String atmBranchId = /*((ATMTerminal) returnedSettlementData.getTerminal())*/atm.getOwner().getCoreBranchCode();
            List<DepositInfoForIssueDocument> depositList = new ArrayList<DepositInfoForIssueDocument>();
            depositList.add(deposit);
            List<DocumentItemEntity> documentItemEntityList = new ArrayList<DocumentItemEntity>();
            documentItemEntityList.add(documentItemEntity);

            List<Transaction> trx = new ArrayList<Transaction>();
            trx.addAll(returnedSettlementData.getTransactions());


            Pair<String, String> document = AccountingService.generateFCBDocumentByUserName(commentOfDocumentItem,
                    documentItemEntityList, depositList, atmBranchId, username, password, "stlData-"
                    + returnedSettlementData.getId());
            report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);

            returnedSettlementData.setSettlementReport(report);
            getGeneralDao().saveOrUpdate(report);
            getGeneralDao().saveOrUpdate(returnedSettlementData);
        } catch (BusinessException e) {
            logger.error("Encounter exception as issuing document. (" + e.getClass().getSimpleName() + ": "
                    + e.getMessage());
            logger.error(e);
            throw new BusinessException(e);
        }
        String transactionId = AccountingService.issueFCBDocument(report, true);
        logger.debug("trans-id = " + transactionId);

        if (transactionId != null) {
            report.setDocumentNumber(transactionId);
            returnedSettlementInfo.setSettledState(SettledState.SETTLED);
            returnedSettlementData.setDocumentNumber(transactionId);
            getGeneralDao().saveOrUpdate(report);
            getGeneralDao().saveOrUpdate(returnedSettlementInfo);
            getGeneralDao().saveOrUpdate(returnedSettlementData);
        }

    }

    protected String getDocumentPersianDate(DateTime time) {
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        return dateFormatPers.format(time.toDate());
    }

}
