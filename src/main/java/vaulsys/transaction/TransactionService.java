package vaulsys.transaction;

import vaulsys.caching.CheckAccountParamsForCache;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementRecord;
import vaulsys.clearing.consts.ClearingProcessType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.settlement.OnlinePerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.OnlinePerTransactionSettlementThread;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.fee.FeeService;
import vaulsys.fee.base.FeeInfo;
import vaulsys.fee.impl.Fee;
import vaulsys.lottery.LotteryService;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.message.exception.MessageAlreadyReversedException;
import vaulsys.message.exception.ReversalOriginatorNotFoundException;
import vaulsys.modernpayment.onlinebillpayment.OnlineBillPaymentService;
import vaulsys.mtn.MTNChargeService;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.exception.exception.ReferenceTransactionNotFoundException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.EMVRqData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.batch.IfxSettlement;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.base.config.TransactionStatusType;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.protocols.ndc.constants.LastStatusIssued;
import vaulsys.scheduler.ReversalJobInfo;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.SwitchTerminalType;
import vaulsys.terminal.TerminalStatus;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.ConfigUtil;
import vaulsys.util.NotUsed;
import vaulsys.util.Pair;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.transform.AliasToBeanResultTransformer;

public class TransactionService {
    private static final Logger logger = Logger.getLogger(TransactionService.class);

    public static void updateMessageForNotSuccessful(Ifx ifx, Transaction transaction) {
        OnlineBillPaymentService.unlockOnlineBillPayment(ifx, transaction);
        MTNChargeService.unlockCharge(ifx, transaction);
        LotteryService.unlockLottery(ifx, transaction);
        ifx.setMti(ISOMessageTypes.getResponseMTI(ifx.getMti())); //Raza Update MTI for Failed Txn.
    }

    public static void updateMessageForNotSuccessful(Ifx ifx, Ifx refIfx, Transaction transaction) {
        OnlineBillPaymentService.unlockOnlineBillPayment(refIfx, transaction);
        MTNChargeService.unlockCharge(ifx, refIfx, transaction);
        LotteryService.unlockLottery(ifx, transaction);
    }

    /**
     * @author k.khodadi
     * @param trx
     * @param ifx
     * @throws Exception
     * check mikonad ke in trx reverse sorushi nakhorde bashad. va dobare reverse az trafe switch nakhorad
     *
     */
    public static void checkReverseSorush(Transaction trx,Ifx ifx) throws Exception{

        if(trx.getReferenceTransaction() != null &&
                trx.getReferenceTransaction().getLifeCycle() != null &&
                trx.getReferenceTransaction().getLifeCycle().getIsFullyReveresed() != null &&
                trx.getReferenceTransaction().getLifeCycle().getSorushLifeCycle() != null && trx.getReferenceTransaction().getLifeCycle().getSorushLifeCycle() != 0L &&
                ifx.getIfxType() != null &&
                (ifx.getBankId() == null || !ifx.getBankId().equals(936450L)) &&
                trx.getReferenceTransaction().getLifeCycle().getIsFullyReveresed()!= null &&
                ISOFinalMessageType.isReversalMessage(ifx.getIfxType()) &&
                ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
                ){
            throw new MessageAlreadyReversedException("Message already reversed:");
        }
    }

    public static boolean checkReverseSorush(ReversalJobInfo reversalJobInfo) {
        boolean retVal = false;
        try {
            Transaction trx = reversalJobInfo.getTransaction();
            Ifx ifx = trx.getIncomingIfx();
            if(trx != null &&
                    trx.getLifeCycle() != null &&
                    trx.getLifeCycle().getIsFullyReveresed() != null &&
                    trx.getLifeCycle().getSorushLifeCycle() != null && trx.getLifeCycle().getSorushLifeCycle() != 0L &&
                    ifx.getIfxType() != null &&
                    (ifx.getBankId() == null || !ifx.getBankId().equals(936450L)) &&
                    trx.getLifeCycle().getIsFullyReveresed()!= null &&
                    ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
                    ){
                retVal = true;
            }

        } catch (Exception e) {
            logger.error("Encouter with an Exception "+ e.getClass().getSimpleName()+" in checkReverseSorush "+ e, e);
        }
        return retVal;
    }

    public static boolean IsSorush(Ifx ifx){
        boolean retVal =  false;
        try {
            if(ifx != null && ifx.getIfxType()!= null && ifx.getBankId() != null && ifx.getBankId().equals(936450L) && ifx.getIfxType() != null && (IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifx.getIfxType())|| IfxType.TRANSFER_TO_ACCOUNT_RS.equals(ifx.getIfxType()) )){
                retVal = true;
            }
        } catch (Exception e) {
            logger.error("Encouter with an Exception "+ e.getClass().getSimpleName()+" in IsSorush "+ e, e);
        }
        return retVal;
    }

    public static boolean IsSorushReverce(Ifx ifx){
        boolean retVal =  false;
        try {
            if(ifx != null && ifx.getIfxType() != null && ifx.getBankId() != null && ifx.getBankId().equals(936450L) && (IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifx.getIfxType())|| IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS.equals(ifx.getIfxType()) )){
                retVal = true;
            }
        } catch (Exception e) {
            logger.error("Encouter with an Exception "+ e.getClass().getSimpleName()+" in IsSorushReverce "+ e, e);
        }
        return retVal;
    }

    public static List<Transaction> findAllNotRevTransactionsReferenceTo(Transaction transaction) {
        String query = "select distinct i.transaction from Ifx as i "
                + " where (i.transaction.referenceTransaction = :trnx "
                + " or i.transaction.firstTransaction = :trnx) "
                + " and i.ifxType not in "
                + IfxType.strRevRqOrdinals
                + " and i.ifxType not in "
                + IfxType.strRevRsOrdinals
                ;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trnx", transaction);

        return GeneralDao.Instance.find(query, params);
    }

    public static void checkValidityOfLastTransactionStatus(Terminal terminal, Ifx ifx) throws ReversalOriginatorNotFoundException {
        try {
            if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
                logger.info("Response transaction: " + ifx.getIfxType() + ", no action!");
                return;
            }

            // check all terminal types
            if (!TerminalType.POS.equals(terminal.getTerminalType()) &&
                    !TerminalType.PINPAD.equals(terminal.getTerminalType()) &&
                    !TerminalType.ATM.equals(terminal.getTerminalType()) &&
                    !TerminalType.KIOSK_CARD_PRESENT.equals(terminal.getTerminalType())
                    ) {
                logger.info(terminal.getTerminalType() + " is not Pos, PinPad or Atm, no action!");
                return;
            }

            Transaction myLastTransaction = terminal.getLastTransaction();
            Transaction myRealLastTransaction = myLastTransaction;

            if (TerminalType.ATM.equals(terminal.getTerminalType())) {
                myRealLastTransaction = ((ATMTerminal)terminal).getLastRealTransaction();
            }
            if (myLastTransaction == null) {
                logger.info("myLastTransaction is null, no action!");
                return;
            }

            Ifx myLastIfx = null;
            try {
                if (/*myLastTransaction.getInputMessage() == null ||*/
                        myLastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/ == null) {
                    logger.info("last transaction is special, don't change flag...(trx: " + myLastTransaction.getId() + ")");
                    return;
                }
                myLastIfx = myLastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
                if (IfxType.PARTIAL_DISPENSE_RQ.equals(myLastIfx.getIfxType())) {
                    myLastIfx = myLastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
                }
                else {
                    if (myLastTransaction.getFirstTransaction() == null ||
//							myLastTransaction.getFirstTransaction().getInputMessage() == null ||
                            myLastTransaction.getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/ == null) {
                        logger.info("first transaction of last transaction is special, don't change flag...(trx: " + myLastTransaction.getId() + ")");
                        return;
                    }
                    myLastIfx = myLastTransaction.getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;

                    if (IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(myLastIfx.getIfxType()))
                        myLastIfx = myLastTransaction.getFirstTransaction().getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
                }

            } catch (Exception e) {
                logger.error("invalid last trx!!!!!");
                return;
            }
            String myLastSeqCntr = myLastIfx.getSrc_TrnSeqCntr();
            String terminalLastSeqCntr = ifx.getLast_TrnSeqCntr();
            logger.debug("myLastSeqCntr: " + myLastSeqCntr + ", terminalLastSeqCntr: " + terminalLastSeqCntr);

            if (terminalLastSeqCntr == null)
                return;

            if (ISOFinalMessageType.isReversalOrRepeatMessage(myLastIfx.getIfxType())
                    || ISOFinalMessageType.isNetworkMessage(myLastIfx.getIfxType())
                    || ISOFinalMessageType.isClearingMessage(myLastIfx.getIfxType())) {
                logger.debug("myLastIfx: " + myLastIfx.getIfxType().toString());
                return;
            }

            //last seqCntr of first transaction of new application is "00"
            try {
                if (Util.longValueOf(terminalLastSeqCntr).equals(0L))
                    return;
            } catch (Exception e) {
                logger.error("Invalid terminalLastSeqCntr: "+ terminalLastSeqCntr);
            }

            SettlementInfo sourceSettleInfo = myLastTransaction.getSourceSettleInfo();
//			SettlementInfo mySrcSettleInfo = sourceSettleInfo;

            // source trx ghabli ma ra ghabool nadarad!
            ClearingState disagreement = ClearingState.DISAGREEMENT;
            if (Integer.parseInt(myLastSeqCntr) != Integer.parseInt(terminalLastSeqCntr)) {

                if (!myLastTransaction.getId().equals(myRealLastTransaction.getId())) {
                    if (/*myRealLastTransaction.getInputMessage() != null &&*/
                            myRealLastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/ != null) {
                        Ifx myRealLastIfx = myRealLastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
                        if (Integer.parseInt(myRealLastIfx.getSrc_TrnSeqCntr()) == Integer.parseInt(terminalLastSeqCntr)) {
                            logger.debug("last transaction is special, we were set real transaction: " + myRealLastTransaction.getId());
                            return;
                        }
                    }

                    else {
                        Ifx myRealLastIfx = myRealLastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
                        if (Integer.parseInt(myRealLastIfx.getSrc_TrnSeqCntr()) == Integer.parseInt(terminalLastSeqCntr)) {
                            logger.debug("last transaction is special, we were set real transaction: " + myRealLastTransaction.getId());
                            return;
                        }
                    }
                }

                if (myLastTransaction.getSourceClearingInfo() != null
                        && ClearingState.CLEARED.equals(myLastTransaction.getSourceClearingInfo().getClearingState())) {
                    logger.error("OHOH! source clearing state of trx: " + myLastTransaction.getId() + " is CLEARED but we want to put DISAGREE flag on it!!");
                    //do nothing

                } else {
                    if (sourceSettleInfo != null && !SettledState.NOT_SETTLED.equals(sourceSettleInfo.getSettledState())) {

                        putDesiredFlagForNormalTransaction(myLastTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE/*,
								SourceDestination.DESTINATION*/ }, ClearingState.SUSPECTED_DISPUTE);
                    } else {

                        putDesiredFlagForNormalTransaction(myLastTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE },
                                disagreement);

                        //NOTE: 1388/09/18: To decrease our disagreement amount!!
						/*putDesiredFlagForNormalTransaction(myLastTransaction, ifx, new SourceDestination[] { SourceDestination.DESTINATION },
								ClearingState.SUSPECTED_DISPUTE);*/
                    }
                }
            } else {
                if (ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType())) {

                    // current trx is rev, and agree the last trx, then last trx
                    // must be dispute(Ghat'an pool bayad bargasht zade shavad)

                    //TODO Refactor MessageBinder, add this method to MessageBinder
                    if (LastStatusIssued.GOOD_TERMINATION_SENT.equals(ifx.getLastTrxStatusIssue())) {
                        if (!IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(ifx.getIfxType())) {

                            // MessageBinderComponent may find the right reference, but the last transaction is different with it!
                            if (ifx.getTransaction().getReferenceTransaction()!= null
                                    && myLastTransaction.getFirstTransaction().getId().equals(ifx.getTransaction().getReferenceTransaction().getId())) {

                                ifx.getTransaction().setReferenceTransaction(null);
                                LifeCycle lifeCycle = new LifeCycle();
                                GeneralDao.Instance.saveOrUpdate(lifeCycle);
                                ifx.getTransaction().setLifeCycle(lifeCycle);
                                GeneralDao.Instance.saveOrUpdate(ifx.getTransaction());
                                putDesiredFlagForNormalTransaction(myLastTransaction, ifx,
                                        new SourceDestination[] { SourceDestination.SOURCE }, ClearingState.CLEARED);
                                logger.error("Trx with seqCntr " + myLastSeqCntr
                                        + " is successfully completed but a time-out request has been received!");
                                throw new ReversalOriginatorNotFoundException("No request has been received for this reversal request");

                            }
                        }
                    }


                    Transaction refRevTransaction = ifx.getTransaction().getReferenceTransaction();
                    if (refRevTransaction == null) {
                        logger.warn("we want to put DISAGREE on refrence trx of reverse trx: " + ifx.getTransaction().getId()
                                + " but ref trx is null!");

                        /*******************/
                        ClearingState flag = ClearingState.CLEARED;

                        if (ifx.getLastTrxStatusIssue()!= null
                                && !LastStatusIssued.UNKNOWN.equals(ifx.getLastTrxStatusIssue())
                                && !LastStatusIssued.GOOD_TERMINATION_SENT.equals(ifx.getLastTrxStatusIssue())) {

                            logger.info("Though myLastSeqCntr is equall to terminalLastSeqCntr, its LastTrxStatus is not GOOD_TERMINATION!");
                            flag = ClearingState.SUSPECTED_DISAGREEMENT;
                        }

                        putDesiredFlagForNormalTransaction(myLastTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE },flag);
                        /*******************/

                        return;
                    }

                    SettlementInfo refRevSettleInfo = refRevTransaction.getSourceSettleInfo();
                    ClearingState dispute = ClearingState.DISPUTE;

                    if (refRevSettleInfo != null && !SettledState.NOT_SETTLED.equals(refRevSettleInfo.getSettledState())) {

                        putDesiredFlagForNormalTransaction(refRevTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE,
                                SourceDestination.DESTINATION }, dispute);
                    } else {
                        Pair<Boolean,ClearingState> reverseTransaction = isReverseTransaction(ifx);


                        //Mirkamali(Task139, Task140)
                        if(reverseTransaction.first != null){
                            if (!reverseTransaction.first){
                                disagreement = reverseTransaction.second;
                                dispute = ClearingState.NOT_CLEARED;
                            }	else {
                                if (reverseTransaction.second != null){
                                    disagreement = reverseTransaction.second;
                                }
                                if (Util.hasText(ifx.getNew_AmtAcqCur()) &&
                                        Util.longValueOf(ifx.getNew_AmtAcqCur()).equals(ifx.getAuth_Amt())) {
                                    dispute = ClearingState.NOT_CLEARED;
//									dispute = ClearingState.PARTIALLY_CLEARED;
                                }
                            }

                            putDesiredFlagForNormalTransaction(refRevTransaction, ifx,
                                    new SourceDestination[] { SourceDestination.SOURCE }, disagreement);

                            putDesiredFlagForNormalTransaction(refRevTransaction, ifx,
                                    new SourceDestination[] { SourceDestination.DESTINATION }, dispute);
                        }
                    }
                } else{

                    ClearingState flag = ClearingState.CLEARED;

                    if (TerminalType.ATM.equals(terminal.getTerminalType())) {
                        Ifx myLastIfxOut = myLastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
                        flag = ATMTerminalService.checkValidityOfLastTransactionStatus(terminal, ifx, myLastIfxOut);
                    }
//					if (ifx.getLastTrxStatusIssue()!= null
//						&& !LastStatusIssued.UNKNOWN.equals(ifx.getLastTrxStatusIssue())
//						&& !LastStatusIssued.GOOD_TERMINATION_SENT.equals(ifx.getLastTrxStatusIssue())) {
//
//						logger.info("Though myLastSeqCntr is equall to terminalLastSeqCntr, its LastTrxStatus is not GOOD_TERMINATION!");
//						flag = ClearingState.SUSPECTED_DISAGREEMENT;
//
//					} else if (myLastIfx.getTransaction().getSourceClearingInfo() != null) {
//						if (ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED.equals(myLastIfx.getTransaction().getSourceClearingInfo().getClearingState())) {
//							logger.info("OHOH last clearingState is NOT_NOTE_SUCCESSFULLY_DISPENSED but we want to clear it!");
//							flag = ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED;
//						}
//					}

                    putDesiredFlagForNormalTransaction(myLastTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE },
                            flag);

					/*if (sourceSettleInfo != null && SettledState.NOT_SETTLED.equals(sourceSettleInfo.getSettledState())){
						if (AccountingState.RETURNED.equals(sourceSettleInfo.getAccountingState())){
							logger.info("Source.AccountingState "+ sourceSettleInfo.getAccountingState()+" is changed to "+ AccountingState.NOT_COUNTED);
							sourceSettleInfo.setAccountingState(AccountingState.NOT_COUNTED);
							sourceSettleInfo.setAccountingDate(DateTime.now());
						}
					}else
						logger.info("Trx "+ myLastTransaction.getId()+" is settled so Source.AccountingState cannot be changed!");*/
                }
            }

            terminal.setLastTransaction(myLastTransaction);
        } catch (ReversalOriginatorNotFoundException e) {
            throw e;
        }catch (Exception e) {
        }
    }

    public static List<Message> getDesiredMessages(ClearingState clearingState, Terminal terminal, TrnType trnType) {
        List<ClearingState> clrStates = new ArrayList<ClearingState>();
        clrStates.add(clearingState);

        List<TrnType> trnTypes = new ArrayList<TrnType>();
        trnTypes.add(trnType);
        return getDesiredMessages(clrStates, trnTypes, null, null, null, terminal, null, null, true);
    }

    public static List<Message> getDesiredMessages(List<ClearingState> toBeClrState, List<TrnType> toBeClrTrx, List<TerminalType> termTypes,
                                                   AccountingState accState, SettledState settleState, Terminal terminal, DateTime untilTime, MonthDayDate stlDate, boolean foundRs) {
        String query  =
                "select m from Message m " +
                        " inner join m.ifx i " +
                        " inner join m.transaction t " +
                        " where ";

        Map<String, Object> parameters = new HashMap<String, Object>();

        if(foundRs) {
            query += " i.eMVRsData.RsCode = :success";
            parameters.put("success", ISOResponseCodes.APPROVED);

            query += " and m.request = :req";
            parameters.put("req", false);
        }else{
            query += " m.request = :req";
            parameters.put("req", true);
        }

        if (untilTime != null) {
            query += " and m.ifx.receivedDtLong <= :untilTime ";
            parameters.put("untilTime", untilTime.getDateTimeLong());
        }

        if (stlDate != null) {
            if (TerminalType.SWITCH.equals(terminal.getTerminalType())
                    && SwitchTerminalType.ISSUER.equals(((SwitchTerminal) terminal).getType()))
                query += " and i.postedDt = :stlDate ";
            else
                query += " and i.settleDt = :stlDate ";

            parameters.put("stlDate", stlDate);
        }

        if (toBeClrTrx != null) {
            query += " and i.trnType in (:toBeClrTrx) ";
            parameters.put("toBeClrTrx", toBeClrTrx);
        }

        if (termTypes != null) {
            query += " and i.networkTrnInfo.TerminalType in (:termTypes) ";
            parameters.put("termTypes", termTypes);
        }

        String srcDest = findSrcDest(terminal);
        if (!TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
            query += " and m.endPointTerminal = :terminal ";
            parameters.put("terminal", terminal);

            if (toBeClrState != null) {
                query += " and t." + srcDest + "ClearingInfo.clearingState in (:toBeClrState) ";
                parameters.put("toBeClrState", toBeClrState);
            }
        } else {
            query += " and i.networkTrnInfo.ThirdPartyTerminalId = :terminalId ";
            parameters.put("terminalId", terminal.getCode());

            if (toBeClrState != null) {
                query += " and t.sourceClearingInfo.clearingState in (:toBeClrState) ";
                parameters.put("toBeClrState", toBeClrState);
            }

            if (foundRs){
                query += " and m.type = :msgType ";
                parameters.put("msgType", MessageType.OUTGOING);
            }else {
                query += " and m.type = :msgType ";
                parameters.put("msgType", MessageType.INCOMING);
            }

        }

        if (accState != null) {
            query += " and t." + srcDest + "SettleInfo.accountingState = :accState";
            parameters.put("accState", accState);
        }

        if (settleState != null) {
            query += " and t." + srcDest + "SettleInfo.settledState = :settleState";
            parameters.put("settleState", settleState);
        }

        return GeneralDao.Instance.find(query, parameters);
    }

    public static String getBaseCriteria(
            DateTime untilTime,
            Map<String, Object> parameters, boolean justToday, Integer guaranteePeriod/*, List<Long> trxSettleRecord*/, Boolean onlineProcess) {
        String query  =
                "select i "+
                        " from Ifx i " +
                        " inner join i.transaction t " +
                        " where ";
        query += " i.dummycol in (0,1,2,3,4,5,6,7,8,9) and ";
        query += " i.request = :false ";
        parameters.put("false", false);

        if (!onlineProcess) {
            if (untilTime != null) {
                if (justToday) {
                    if(guaranteePeriod != null && guaranteePeriod < 0){
                        DateTime guaranteeTime = untilTime.clone();
                        guaranteeTime.increase(guaranteePeriod);

                        query +=  " and 1=1 and i.receivedDtLong between :fromDate and :toDate ";
                        parameters.put("fromDate", guaranteeTime.getDateTimeLong());
                        parameters.put("toDate", untilTime.getDateTimeLong());
//						query +=  " and i.receivedDtLong between " + guaranteeTime.getDateTimeLong() + " and "+untilTime.getDateTimeLong();
                    }else{
                        DateTime today = untilTime.clone();
                        today.setDayTime(new DayTime(0, 0, 0));
                        query += " and 2=2 and i.receivedDtLong between :fromDate and :toDate ";
                        parameters.put("fromDate", today.getDateTimeLong());
                        parameters.put("toDate", untilTime.getDateTimeLong());
//						query += " and i.receivedDtLong between "+today.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
                    }
                } else {
                    DateTime untilDate2 = new DateTime(untilTime.getDayDate().nextDay(guaranteePeriod), untilTime.getDayTime());

                    DateTime fromDate = untilDate2.clone();
                    fromDate.increase(1);
                    fromDate.setDayTime(new DayTime(0, 0, 0));

                    DateTime now = DateTime.now();
                    DateTime toDate = untilTime;
                    if (toDate.after(now))
                        toDate = now;

                    query +=  " and 3=3 and i.receivedDtLong between :fromDate and :toDate ";
                    parameters.put("fromDate", fromDate.getDateTimeLong());
                    parameters.put("toDate", toDate.getDateTimeLong());
//					query +=  " and i.receivedDtLong between "+fromDate.getDateTimeLong()+" and "+toDate.getDateTimeLong();
                }
            }
        }
/*
		if (!onlineProcess) {
			if (untilTime != null) {
				if (justToday) {
					if(guaranteePeriod != null && guaranteePeriod < 0){
						DateTime guaranteeTime = untilTime.clone();
						guaranteeTime.increase(guaranteePeriod);

						query +=  " and i.receivedDtLong between " + guaranteeTime.getDateTimeLong() + " and "+untilTime.getDateTimeLong();
					}else{
						DateTime today = untilTime.clone();
						today.setDayTime(new DayTime(0, 0, 0));
						query += " and i.receivedDtLong between "+today.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
					}
				} else {
					DateTime untilDate2 = new DateTime(untilTime.getDayDate().nextDay(guaranteePeriod), untilTime.getDayTime());

					DateTime fromDate = untilDate2.clone();
					fromDate.increase(1);
					fromDate.setDayTime(new DayTime(0, 0, 0));

					DateTime now = DateTime.now();
					DateTime toDate = untilTime;
					if (toDate.after(now))
						toDate = now;

					query +=  " and i.receivedDtLong between "+fromDate.getDateTimeLong()+" and "+toDate.getDateTimeLong();
				}
			}
		}
*/

        return query;
    }

    public static String getBaseCriteriaNew(
            DateTime untilTime,
            Map<String, Object> parameters, boolean justToday, Integer guaranteePeriod/*, List<Long> trxSettleRecord*/, Boolean onlineProcess) {
        String query  =
                "select i.id as id, " +
                        " i.ifxType as ifxType, " +
                        " rq.Real_Amt as Real_Amt, " +
                        " i.transactionId as transactionId, " +
                        " rq.Sec_CurRate as Sec_CurRate, "+
                        " rq.Sec_Amt as Sec_Amt, " +
                        " i.endPointTerminalCode as endPointTerminalCode "+
                        " from Ifx i " +
                        " inner join i.eMVRqData rq " +
                        " inner join i.transaction t " +
                        " where ";

        query += " i.dummycol in (0,1,2,3,4,5,6,7,8,9) and ";
        query += " i.request = :false ";
        parameters.put("false", false);

        if (!onlineProcess) {
            if (untilTime != null) {
                if (justToday) {
                    if(guaranteePeriod != null && guaranteePeriod < 0){
                        DateTime guaranteeTime = untilTime.clone();
                        guaranteeTime.increase(guaranteePeriod);

                        query +=  " and 1=1 and i.receivedDtLong between :fromDate and :toDate ";
                        parameters.put("fromDate", guaranteeTime.getDateTimeLong());
                        parameters.put("toDate", untilTime.getDateTimeLong());
//						query +=  " and i.receivedDtLong between "+guaranteeTime.getDateTimeLong()+" and "+untilTime.getDateTimeLong();

                    }else{
                        DateTime today = untilTime.clone();
                        today.setDayTime(new DayTime(0, 0, 0));

                        query += " and 2=2 and i.receivedDtLong between :fromDate and :toDate ";
                        parameters.put("fromDate", today.getDateTimeLong());
                        parameters.put("toDate", untilTime.getDateTimeLong());
//						query += " and i.receivedDtLong between "+today.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
                    }
                } else {
                    DateTime untilDate2 = new DateTime(untilTime.getDayDate().nextDay(guaranteePeriod), untilTime.getDayTime());
                    query +=  " and 3=3 and i.receivedDtLong between :fromDate and :toDate ";
                    parameters.put("fromDate", untilDate2.getDateTimeLong());
                    parameters.put("toDate", untilTime.getDateTimeLong());
//					query +=  " and i.receivedDtLong between "+untilDate2.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
                }
            }
        }

		/*
		if (!onlineProcess) {
			if (untilTime != null) {
				if (justToday) {
					if(guaranteePeriod != null && guaranteePeriod < 0){
						DateTime guaranteeTime = untilTime.clone();
						guaranteeTime.increase(guaranteePeriod);

						query +=  " and i.receivedDtLong between "+guaranteeTime.getDateTimeLong()+" and "+untilTime.getDateTimeLong();

					}else{
						DateTime today = untilTime.clone();
						today.setDayTime(new DayTime(0, 0, 0));

						query += " and i.receivedDtLong between "+today.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
					}
				} else {
					DateTime untilDate2 = new DateTime(untilTime.getDayDate().nextDay(guaranteePeriod), untilTime.getDayTime());
					query +=  " and i.receivedDtLong between "+untilDate2.getDateTimeLong()+" and "+untilTime.getDateTimeLong();
				}
			}
		}*/

        return query;
    }

    public static String addSettleRecordTransactionCriteria(DateTime untilTime, Terminal terminal, Map<String, Object> parameters) {
        String query = "";

        List<Long> termCode = Arrays.asList(terminal.getCode());
        List<Long> trxSettleRecord = getTransactionFromSettlementRecord(untilTime, termCode);

        if (trxSettleRecord != null && trxSettleRecord.size() > 0) {
            query += " and i.transactionId in (:list) ";
            parameters.put("list", trxSettleRecord);
        }

        logger.info("trx from settlementRecord: " + query);
        return query;
    }

    public static String addTerminalCriteria(
            SettledState settleState,
            Terminal terminal,
            Map<String, Object> parameters ) {
        String query = "";
        String srcDest = findSrcDest(terminal);


        if (!TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
            query += " and i.endPointTerminalCode = :termCode ";
//			query += " and i.endPointTerminal = "+terminal.getCode();
        } else {
            query += " and i.ThirdPartyTerminalCode = :termCode ";
//			query += " and i.ThirdPartyTerminalCode = "+terminal.getCode();

            query += " and i.ifxDirection = :ifxDirection ";
            parameters.put("ifxDirection", IfxDirection.OUTGOING);
        }

        parameters.put("termCode", terminal.getCode());
/*

		if (!TerminalType.THIRDPARTY.equals(terminal.getTerminalType())) {
			query += " and i.endPointTerminal = "+terminal.getCode();
		} else {
			query += " and i.ThirdPartyTerminalCode = "+terminal.getCode();

			query += " and i.ifxDirection = :ifxDirection ";
			parameters.put("ifxDirection", IfxDirection.OUTGOING);
		}
*/
        if (settleState != null) {
            query += " and t." + srcDest + "SettleInfo.settledState = :settleState";
            parameters.put("settleState", settleState);
        }
        return query;
    }

    public static List<Ifx> getResultCriteria(String query, Map<String, Object> parameters, int firstResult, int maxResults) {
        return GeneralDao.Instance.find(query, parameters, firstResult, maxResults);
    }

    public static List<IfxSettlement> getResultCriteriaNew(String query, Map<String, Object> parameters, int firstResult, int maxResults) {
        return GeneralDao.Instance.find(query, parameters, firstResult, maxResults, new AliasToBeanResultTransformer(IfxSettlement.class));
    }


    public static List<Transaction> getReversalOriginatorTransaction(Ifx ifx) {
        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = "select i.transaction from Ifx as i "
                + "where "
                + " i.networkTrnInfo.BankId = :BankId "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.request = true "
                + " and not(i.id = :msgId) "
                ;

        params.put("msgId", ifx.getId());
        logger.info("msgId [" + ifx.getId() + "]"); //Raza TEMP
        params.put("BankId", ifx.getSafeOriginalDataElements().getBankId());
        logger.info("BankId [" + ifx.getSafeOriginalDataElements().getBankId() + "]"); //Raza TEMP

        params.put("IfxDirection", IfxDirection.INCOMING);

        if (Util.hasText(ifx.getAppPAN())){
            logger.info("appPAN [" + ifx.getAppPAN() + "]"); //Raza TEMP
            queryString += " and i.eMVRqData.CardAcctId.AppPAN = :appPAN ";
            params.put("appPAN", ifx.getAppPAN());
        }

        if (ifx.getSafeOriginalDataElements().getOrigDt() != null) {
            logger.info("OrigDt [" + ifx.getSafeOriginalDataElements().getOrigDt() + "]"); //Raza TEMP
            queryString += " and i.networkTrnInfo.OrigDt = :OrigDt ";
            params.put("OrigDt", ifx.getSafeOriginalDataElements().getOrigDt());
        }

        if (ifx.getSafeOriginalDataElements().getNetworkTrnInfo() != null) {
            logger.info("NetworkRefId [" + ifx.getSafeOriginalDataElements().getNetworkTrnInfo() + "]"); //Raza TEMP
            queryString += " and i.networkTrnInfo.NetworkRefId = :NetworkRefId ";
            params.put("NetworkRefId", ifx.getSafeOriginalDataElements().getNetworkTrnInfo());
        }

        if (ifx.getSafeOriginalDataElements().getTrnSeqCounter() != null) {
            logger.info("TrnSecCntr [" + ifx.getSafeOriginalDataElements().getTrnSeqCounter() + "]"); //Raza TEMP
            queryString += " and i.networkTrnInfo.Src_TrnSeqCntr = :TrnSecCntr ";
            params.put("TrnSecCntr", ifx.getSafeOriginalDataElements().getTrnSeqCounter());
        }

        boolean endPointTerminalQuery = false;

        if (!IfxType.RETURN_RQ.equals(ifx.getIfxType())
                //m.rehman: for void transaction from NAC
                && !IfxType.VOID_RQ.equals(ifx.getIfxType())) {
            queryString += " and i.trnType= :TrnType ";
            //m.rehman: for pre-auth completion
            TrnType trnType;
            if (ifx.getIfxType().equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ))
                trnType = TrnType.PREAUTH;
            else
                trnType = ifx.getTrnType();

            logger.info("TrnType [" + trnType + "]"); //Raza TEMP

           //params.put("TrnType", ifx.getTrnType());
            params.put("TrnType", trnType);

            //List<IfxType> ifxTypes = TrnType.getIfxType(ifx.getTrnType());
            List<IfxType> ifxTypes = TrnType.getIfxType(trnType);
            List<IfxType> list = new ArrayList<IfxType>();
            for(IfxType ifxType: ifxTypes) {
                if (!ISOFinalMessageType.isReversalMessage(ifxType) &&
                        !ISOFinalMessageType.isResponseMessage(ifxType)) {
                    list.add(ifxType);
                }
            }

            if (list.size() > 0) {
                queryString += " and i.ifxType in (:ifxList) ";
                params.put("ifxList", list);

//				queryString += " and i.ifxType in ";
//				queryString += IfxType.getIfxTypeOrdinalsOfList(list);
            }
        } else {
            queryString += " and i.ifxType = :IfxType ";
            params.put("IfxType", IfxType.PURCHASE_RQ);

//			queryString += " and i.ifxType = " +IfxType.PURCHASE_RQ.getType();

            if (TerminalType.POS.equals(ifx.getTerminalType())){
                endPointTerminalQuery = true;
            }

        }

        if (endPointTerminalQuery) {
            queryString += " and i.endPointTerminalCode = :terminalCode ";
            params.put("terminalCode", Long.parseLong(ifx.getTerminalId()));

            logger.info("terminalCode [" + Long.parseLong(ifx.getTerminalId()) + "]"); //Raza TEMP
        } else {
            //m.rehman: for wallet topup from ui, adding below condition
            if (Util.hasText(ifx.getTerminalId())) {
                queryString += " and i.networkTrnInfo.TerminalId = :terminalId ";
                params.put("terminalId", ifx.getTerminalId());
                logger.info("terminalId [" + ifx.getTerminalId() + "]"); //Raza TEMP
            }
        }

        if (ifx.getTrx_Amt()!= null && !ifx.getTrx_Amt().equals(0L)){
            queryString += " and i.eMVRqData.Trx_Amt = :amount ";
            params.put("amount", ifx.getTrx_Amt());
            logger.info("amount [" + ifx.getTrx_Amt() + "]"); //Raza TEMP
        }

        queryString += " order by i.receivedDtLong desc";
        logger.info("param [" + params.toString() + "]"); //Raza TEMP
        logger.info("queryString [" + queryString + "]"); //Raza TEMP
        if (IfxType.RETURN_RQ.equals(ifx.getIfxType())) {
            return GeneralDao.Instance.find(queryString, params);
        } else {
            List<Transaction> list = new ArrayList<Transaction>();
            Transaction transaction = (Transaction) GeneralDao.Instance.findObject(queryString, params);
            if (transaction != null)
                list.add(transaction);
            return list;
        }
    }

    public static Ifx getReversalOriginatorTransactionForAcqTerminal(Ifx ifx, DateTime lastReceivedDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = "select i from Ifx as i "
                + "where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9) and "
                + " i.endPointTerminal = :endPoint "
                + " and i.networkTrnInfo.BankId = :BankId "
                + " and i.networkTrnInfo.TerminalId = :terminalId "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.request = true "
                + " and not(i.id = :msgId) "
                ;

        if (lastReceivedDate!= null){
            queryString += " and i.receivedDtLong > :lstRcvDt ";
            params.put("lstRcvDt", lastReceivedDate.getDateTimeLong());

            queryString += " and i.receivedDtLong <= :now ";
            params.put("now", DateTime.now().getDateTimeLong());
            int difDay = DateTime.now().getDayDate().getDay() - lastReceivedDate.getDayDate().getDay();
            int difHour = DateTime.now().getDayTime().getHour() - lastReceivedDate.getDayTime().getHour();
            int dif = difDay * 100 + difHour;
            queryString += " and " + dif + " = " + dif + " ";
        }


        /*** change for reversal checkAccount card to account from atm, in buffer B is seperated account and in this step appPan(buffer B) may be fee or incorrect accountNo. (20130709) ***/
        if (Util.hasText(ifx.getAppPAN())) {
            if (TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType())) {
                queryString += " and i.eMVRqData.secondAppPan = :secondAppPAN ";
                params.put("secondAppPAN", ifx.getSecondAppPan());

            } else {
                queryString += " and i.eMVRqData.CardAcctId.AppPAN = :appPAN ";
                params.put("appPAN", ifx.getAppPAN());
            }
        }
		/*if (Util.hasText(ifx.getAppPAN())){
			queryString += " and i.eMVRqData.CardAcctId.AppPAN = :appPAN ";
			params.put("appPAN", ifx.getAppPAN());
		}*/

        if (ifx.getSafeOriginalDataElements().getOrigDt() != null) {
            queryString += " and i.networkTrnInfo.OrigDt = :OrigDt ";
            params.put("OrigDt", ifx.getSafeOriginalDataElements().getOrigDt());
        }

        if (ifx.getSafeOriginalDataElements().getNetworkTrnInfo() != null) {
            queryString += " and i.networkTrnInfo.NetworkRefId = :NetworkRefId ";
            params.put("NetworkRefId", ifx.getSafeOriginalDataElements().getNetworkTrnInfo());
        }

        if (ifx.getSafeOriginalDataElements().getTrnSeqCounter() != null) {
            queryString += " and i.networkTrnInfo.Src_TrnSeqCntr = :TrnSecCntr ";
            params.put("TrnSecCntr", ifx.getSafeOriginalDataElements().getTrnSeqCounter());
        }

        if (!IfxType.RETURN_RQ.equals(ifx.getIfxType())) {
            if (ifx.getSecTrnType() != null) {
                queryString += " and i.trnType in (:TrnType, :SecTrnType) ";
                params.put("TrnType", ifx.getTrnType());
                params.put("SecTrnType", ifx.getSecTrnType());
            } else {
                queryString += " and i.trnType= :TrnType ";
                params.put("TrnType", ifx.getTrnType());
            }

            List<IfxType> ifxTypes = TrnType.getIfxType(ifx.getTrnType());
            if (ifx.getSecTrnType() != null) {
                List<IfxType> secIfxTypes = TrnType.getIfxType(ifx.getSecTrnType());
                if (secIfxTypes != null && secIfxTypes.size() > 0)
                    ifxTypes.addAll(secIfxTypes);
            }

            List<IfxType> list = new ArrayList<IfxType>();
            for(IfxType ifxType: ifxTypes) {
                if (!ISOFinalMessageType.isReversalMessage(ifxType) &&
                        !ISOFinalMessageType.isResponseMessage(ifxType))
                    list.add(ifxType);
            }

            if (list.size() > 0) {

                queryString += " and i.ifxType in (:ifxList) ";
                params.put("ifxList", list);

//				queryString += " and i.ifxType in ";
//				queryString += IfxType.getIfxTypeOrdinalsOfList(list);
            }
        } else {
            queryString += " and i.ifxType = :IfxType ";
            params.put("IfxType", IfxType.PURCHASE_RQ);
        }

        if (ifx.getTrx_Amt()!= null && !ifx.getTrx_Amt().equals(0L)){
            queryString += " and i.eMVRqData.Trx_Amt= :amount ";
            params.put("amount", ifx.getTrx_Amt());
        }

        queryString += " order by i.receivedDtLong desc";

        params.put("msgId", ifx.getId());
        params.put("BankId", ifx.getSafeOriginalDataElements().getBankId());
        params.put("terminalId", ifx.getTerminalId());
        params.put("endPoint", ifx.getEndPointTerminal());
        params.put("IfxDirection", IfxDirection.INCOMING);

        return (Ifx) GeneralDao.Instance.findUniqueObject(queryString, params);
    }

    public static List<Long> hasReversalRequest(Ifx ifx) {
        if (ISOFinalMessageType.isReversalMessage(ifx.getIfxType()))
            return new ArrayList<Long>();

        String queryString = "select i.id from Ifx as i "
                + "where "
                + " i.ifxDirection = :IfxDirection "
                + " and not(i.id = :msgId) "
                + " and i.ifxType in "+ IfxType.strRevRqOrdinals
                ;

        Map<String, Object> params = new HashMap<String, Object>();

        queryString	+= " and i.originalDataElements.BankId = :BankId ";
        params.put("BankId", ifx.getBankId());

        queryString	+= " and i.originalDataElements.TerminalId = :TerminalId ";
        params.put("TerminalId", ifx.getTerminalId());

        queryString	+= " and i.originalDataElements.AppPAN = :appPAN ";
        params.put("appPAN", ifx.getAppPAN());

        queryString += " and (i.originalDataElements.OrigDt = :OrigDt "
                +" or i.originalDataElements.OrigDt is null )";
        params.put("OrigDt", ifx.getOrigDt());

        queryString += " and (i.originalDataElements.networkTrnInfo = :NetworkRefId "
                +" or i.originalDataElements.networkTrnInfo is null ) ";
        params.put("NetworkRefId", ifx.getNetworkTrnInfo().getNetworkRefId());


//		//change this condition for atm when rev received before main rq, in this state origDataElement.trnSeqCntr can not be set...
//		queryString += " and (i.originalDataElements.TrnSeqCounter = :TrnSecCntr "
//			+ " or i.originalDataElements.TrnSeqCounter is null ) ";
//		params.put("TrnSecCntr", ifx.getSrc_TrnSeqCntr());

        queryString += " and i.originalDataElements.TrnSeqCounter = :TrnSecCntr ";
        params.put("TrnSecCntr", ifx.getSrc_TrnSeqCntr());

        if (ifx.getSecTrnType() != null) {
            queryString += " and i.trnType in (:TrnType, :SecTrnType) ";
            params.put("TrnType", ifx.getTrnType());
            params.put("SecTrnType", ifx.getSecTrnType());
        }else {
            queryString += " and i.trnType= :TrnType ";
            params.put("TrnType", ifx.getTrnType());
        }

        params.put("msgId", ifx.getId());
        params.put("IfxDirection", IfxDirection.INCOMING);

        return GeneralDao.Instance.find(queryString, params);
    }

    public static Transaction getPrepareOnlineBillPayment(Ifx ifx){
        Map<String , Object> params = new HashMap<String, Object>();
        String st = "select i.transaction from Ifx as i "
                +" inner join i.onlineBillPaymentData as online "
                +" inner join i.networkTrnInfo as net "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9) "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.ifxType = :IfxType"
                + " and i.trnType = :TrnType "

                + " and i.receivedDtLong >= :tenMinBefore "
                + " and i.receivedDtLong <= :now "
                + " and i.request = true "
                + " and online.refNum = :onlin "
                + " and net.NetworkRefId = :netRef";


//		if (ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())) {
        st += " and i.endPointTerminalCode = :terminalId ";

        params.put("terminalId", ifx.getEndPointTerminal().getCode());
        params.put("onlin", ifx.getOnlineBillPaymentRefNum());
        params.put("netRef", ifx.getNetworkRefId());

	/*	} else {
			st += " and i.networkTrnInfo.TerminalId = :terminalId ";

			params.put("terminalId", ifx.getTerminalId());
		}*/

        params.put("IfxDirection", IfxDirection.INCOMING);

        DateTime tenMinBefore = DateTime.toDateTime(DateTime.now().getTime() - 10 * DateTime.ONE_MINUTE_MILLIS);
        params.put("tenMinBefore", tenMinBefore.getDateTimeLong());
        params.put("now",  + DateTime.now().getDateTimeLong());


        params.put("TrnType", TrnType.PREPARE_ONLINE_BILLPAYMENT);
        params.put("IfxType", IfxType.PREPARE_ONLINE_BILLPAYMENT);

        st += " order by i.receivedDtLong desc";

        return (Transaction) GeneralDao.Instance.findObject(st, params);
    }
    public static Transaction getCheckAccountTransactionOfTransfer(Ifx ifx) {

        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = "select i.transaction from Ifx as i "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9) "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.eMVRqData.CardAcctId.AppPAN = :secAppPAN "
                + " and i.trnType = :TrnType "
                + " and i.receivedDtLong >= :tenMinBefore "
                + " and i.receivedDtLong <= :now "
                + " and i.request = false ";

        if (ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())) {
            queryString += " and i.endPointTerminalCode = :endPoint ";
            params.put("endPoint", ifx.getEndPointTerminal().getCode());
        } else {
            queryString += " and i.endPointTerminalCode = :endPoint ";
            params.put("endPoint", ifx.getEndPointTerminal().getCode());

            queryString += " and i.networkTrnInfo.TerminalId = :terminalId ";
            params.put("terminalId", ifx.getTerminalId());
        }

        params.put("IfxDirection", IfxDirection.OUTGOING);

        DateTime tenMinBefore = DateTime.toDateTime(DateTime.now().getTime() - 2 * DateTime.ONE_MINUTE_MILLIS);
        params.put("tenMinBefore", tenMinBefore.getDateTimeLong());
        params.put("now",  + DateTime.now().getDateTimeLong());

        /****** NOTE: some of banks don't following this rule *******/

        if(ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())){
            if (Util.hasText(ifx.getNetworkRefId())) {
                queryString += " and i.networkTrnInfo.NetworkRefId= :NetworkRefId ";
                params.put("NetworkRefId", ifx.getNetworkRefId());
            }

        }

        /************************************************************/

        TrnType trnType = TrnType.UNKNOWN;

        if (TrnType.TRANSFER.equals(ifx.getTrnType()) ){
            trnType = TrnType.CHECKACCOUNT;
            params.put("TrnType", trnType);
            params.put("secAppPAN", ifx.getSecondAppPan());

        }else if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())){
            trnType = TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT;
            params.put("TrnType", trnType);
            params.put("secAppPAN", ifx.getSecondAppPan());

        }else if (TrnType.INCREMENTALTRANSFER.equals(ifx.getTrnType())){
            trnType = TrnType.CHECKACCOUNT;
            params.put("TrnType", trnType);
            params.put("secAppPAN", ifx.getAppPAN());

        }else if (TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())){
            trnType = TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT;
            params.put("secAppPAN", ifx.getAppPAN());
            params.put("TrnType", trnType);

        }


        List<IfxType> ifxTypes = TrnType.getIfxType(trnType);
        List<IfxType> list = new ArrayList<IfxType>();
        for(IfxType ifxType: ifxTypes) {
            if (ISOFinalMessageType.isResponseMessage(ifxType))
                list.add(ifxType);
        }


        if (list.size() > 0 && ifxTypes != null && !ifxTypes.isEmpty()) {
            queryString += " and i.ifxType in (:ifxTypes) ";
            params.put("ifxTypes", list);
//			queryString += " and i.ifxType in ";
//			queryString += IfxType.getIfxTypeOrdinalsOfList(list);
        }

		/*if (list.size() > 0) {
            queryString += " and i.ifxType in ";
            queryString += IfxType.getIfxTypeOrdinalsOfList(list);
		}*/

//		queryString += " order by i.receivedDtLong desc";

//		return (Transaction) GeneralDao.Instance.findObject(queryString, params);
        List<Transaction> listTransaction = GeneralDao.Instance.find(queryString, params);
        if(listTransaction == null || listTransaction.size()==0)
            return null;
        if(listTransaction != null && listTransaction.size() == 1)
            return (Transaction)listTransaction.get(0);

        Transaction[] sortedTransactions = new Transaction[listTransaction.size()];

        listTransaction.toArray(sortedTransactions);
        Arrays.sort(sortedTransactions, new Comparator<Transaction>(){
            @Override
            public int compare(Transaction arg0, Transaction arg1) {
                if(arg0.getBeginDateTime().after(arg1.getBeginDateTime()))
                    return -1;
                if(arg0.getBeginDateTime().before(arg1.getBeginDateTime()))
                    return 1;

                return 0;
            }
        });
        return sortedTransactions[0];

    }

    public static CheckAccountParamsForCache createCheckAccountObjForAddOrGet(Ifx ifx){
        try{
            CheckAccountParamsForCache checkAccount = new CheckAccountParamsForCache("", TrnType.UNKNOWN, "", null, null, null, "", null);

            checkAccount.setEndPointTerminalCode(ifx.getEndPointTerminal().getCode());

            if (!ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())) {
                checkAccount.setTerminalId(ifx.getTerminalId());
            }


            if(ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())){
                if (Util.hasText(ifx.getNetworkRefId())) {
                    checkAccount.setNetworkRefId(ifx.getNetworkRefId());
                }

            }

            //By defult(for add or get)
            if(IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())) {

                checkAccount.setAppPAN(ifx.getAppPAN());

                //Just neeed for adding.Because when removing this, we need it!
                checkAccount.setReceivedDt(ifx.getReceivedDtLong());
            }


            if (TrnType.TRANSFER.equals(ifx.getTrnType()) || TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())){
                checkAccount.setAppPAN(ifx.getSecondAppPan());

            } else if(TrnType.INCREMENTALTRANSFER.equals(ifx.getTrnType()) || TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())){
                checkAccount.setAppPAN(ifx.getAppPAN());

            }


            return checkAccount;

        }catch (Exception e) {
            logger.error("checkAccountCache: An Exception occure in creating CheckAccountParamsForCache obj for add to or get from cache " +e.getMessage());
            return null;
        }
    }

    public static Transaction getPrepareBillPayment(Ifx ifx) {

        String queryString = "select i.transaction from Ifx as i "
                + " where i.networkTrnInfo.TerminalId = :terminalId "
                + " and i.eMVRqData.billPaymentData.billID = :billID "
                + " and i.eMVRqData.billPaymentData.billPaymentID = :billpaymentID "
                + " and i.eMVRqData.CardAcctId.AppPAN = :appPAN "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.trnType = :TrnType "
                ;

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("terminalId", ifx.getTerminalId());
        params.put("TrnType", TrnType.PREPARE_BILL_PMT);
        params.put("billID", ifx.getBillID());
        params.put("billpaymentID", ifx.getBillPaymentID());
        params.put("appPAN", ifx.getAppPAN());
        params.put("IfxDirection", IfxDirection.OUTGOING);

        queryString += " order by i.receivedDtLong desc ";

        return (Transaction) GeneralDao.Instance.findObject(queryString, params);
    }

    public static List<Transaction> getBillTransaction(String billID, String billpaymentID, Message incomingMessage) {
        String queryString = "select i.transaction from Ifx as i "
                + " left join i.eMVRsData as rs,"
                + " LifeCycle l "
                + " where "
                + " l.id = i.transaction.lifeCycleId "
                + " and i.eMVRqData.billPaymentData.billID = :billID "
                + " and i.eMVRqData.billPaymentData.billPaymentID = :billpaymentID "
                + " and (rs.RsCode = :rsCode or l.isComplete = :false)"
                + " and l.isFullyReveresed is null "
                + " and l.isPartiallyReveresed is null "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.trnType = :TrnType ";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("TrnType", TrnType.BILLPAYMENT);
        params.put("billID", billID);
        params.put("billpaymentID", billpaymentID);
        params.put("rsCode", ISOResponseCodes.APPROVED);
        params.put("IfxDirection", IfxDirection.OUTGOING);
        params.put("false", false);

        if (incomingMessage.getTransaction().getLifeCycle() != null) {
            queryString += " and l.id != :lifeCycle ";
            params.put("lifeCycle", incomingMessage.getTransaction().getLifeCycleId());
        }

        queryString += " order by i.receivedDtLong ";

        return GeneralDao.Instance.find(queryString, params);
    }

    public static List<Transaction> getEPayTransactions(String invoiceNumber, String invoiceDate, String terminalId, String merchantId, TrnType trnType) {
        if (invoiceNumber == null || invoiceNumber.isEmpty() || invoiceDate == null){
            return new ArrayList<Transaction>();
        }

        String queryString = "select i.transaction from Ifx as i, "
                + " LifeCycle l "
                + " where "
                + " l.id = i.transaction.lifeCycleId and "
                + " i.eMVRqData.paymentData.invoiceNumber = :invoiceNumber "
                + " and i.eMVRqData.paymentData.invoiceDate = :invoiceDate "
                + " and i.networkTrnInfo.TerminalId = :TerminalId "
                + " and i.networkTrnInfo.OrgIdNum = :OrgIdNum "
                + " and i.trnType = :trnType "
                + " and (i.eMVRsData.RsCode = :rsCode or l.isComplete = false) "
                //TODO: nothing or null?!
                + " and l.isFullyReveresed = :nothing "
                + " and l.isPartiallyReveresed = :nothing ";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("invoiceNumber", invoiceNumber);
        params.put("invoiceDate", invoiceDate);
        params.put("TerminalId", terminalId);
        params.put("OrgIdNum", merchantId);
        params.put("trnType", trnType);
        params.put("rsCode", ISOResponseCodes.APPROVED);
        params.put("nothing", LifeCycleStatus.NOTHING);

        return GeneralDao.Instance.find(queryString, params);
    }

    public static Transaction getLastPurchaseCharge(Ifx ifx) {
        String queryString = "select i.transaction from Ifx as i, "
                + " LifeCycle l "
                + " where "
                + " l.id = i.transaction.lifeCycleId "
                + " and i.networkTrnInfo.TerminalId = :terminalId "
                + " and i.networkTrnInfo.OrgIdNum = :orgIdNum "
                + " and i.eMVRsData.RsCode = :rsCode "
                //TODO check this query
                + " and l.isFullyReveresed is null "
                + " and l.isPartiallyReveresed is null "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.trnType = :TrnType "
                + " and i.request = false "
                + " order by i.receivedDtLong desc ";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("TrnType", TrnType.PURCHASECHARGE);
        params.put("terminalId", ifx.getTerminalId());
        params.put("orgIdNum", ifx.getOrgIdNum());
        params.put("rsCode", ISOResponseCodes.APPROVED);
        params.put("IfxDirection", IfxDirection.OUTGOING);

        return (Transaction) GeneralDao.Instance.findObject(queryString, params);
    }

    public static String findResponseOfFirstTransactionType(/*LifeCycle lifeCycle*/Long lifeCycleId, IfxType ifxType) {
        String query = "select i.eMVRsData.RsCode from Ifx i "
                + " inner join  i.transaction as t "
                + " where t.lifeCycleId = :lifeCycleId "
                + " and i.ifxType = :ifxtype "
                + " order by t.beginDateTime.dayDate asc, t.beginDateTime.dayTime asc";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lifeCycleId", lifeCycleId);
        parameters.put("ifxtype", ifxType);

        return (String) GeneralDao.Instance.findObject(query, parameters);
    }

    public static Transaction findResponseTrx(/*LifeCycle lifeCycle*/Long lifeCycleId, Transaction transaction) {
        String query = "select i.transaction from Ifx i"
                + " inner join i.transaction t "
                + " where t.lifeCycleId = :lifecycleId "
                + " and t.firstTransaction = :trx "
                + " and i.request = false "
                + " order by t.beginDateTime.dayDate asc, t.beginDateTime.dayTime asc";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lifecycleId", lifeCycleId);
        parameters.put("trx", transaction);

        return (Transaction) GeneralDao.Instance.findObject(query, parameters);
    }

    public static Boolean canBeSettledReturnedTransaction(Ifx ifx){
        String query = "from Ifx i "
                +" inner join i.transaction as t"
                +" where t.referenceTransaction = :refTrnx "
                +" and i.trnType = :return "
                +" and (t.sourceClearingInfo.clearingState = :disagreement or "
                +" t.sourceClearingInfo.clearingState = :cleared)"
                +" and t.beginDateTime.dayDate >= :date "
                +" and t.beginDateTime.dayTime >= :time "
                +" and i.ifxDirection = :outgoing"
                +" and i.request = false"
                ;

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("refTrnx", ifx.getTransaction().getFirstTransaction());
        parameters.put("return", TrnType.RETURN);
        parameters.put("disagreement", ClearingState.DISAGREEMENT);
        parameters.put("cleared", ClearingState.CLEARED);
        parameters.put("date", ifx.getTransaction().getBeginDateTime().getDayDate());
        parameters.put("time", ifx.getTransaction().getBeginDateTime().getDayTime());
        parameters.put("outgoing", IfxDirection.OUTGOING);

        List<Message> find = GeneralDao.Instance.find(query, parameters);
        return (find.size()>0);
    }

    public static void updateLifeCycleStatusForNotSuccessful(Transaction transaction, Ifx ifx) {
        if (transaction.getLifeCycle() == null || ifx == null)
            return;


        IfxType ifxType = ifx.getIfxType();

        if (!ISOFinalMessageType.isRequestMessage(ifxType) &&
                !ISOFinalMessageType.isResponseMessage(ifxType))
            return;

        logger.debug("Try to get Lock of LifeCycle["+ transaction.getLifeCycleId()+")");

        transaction.getAndLockLifeCycle(LockMode.UPGRADE);

        long currentTimeMillis = System.currentTimeMillis();
        GeneralDao.Instance.optimizedSynchObject(transaction.getLifeCycle());
        logger.debug("LifeCycle[" + transaction.getLifeCycleId() + ") has beeb locked and it's reloaded!, " + (System.currentTimeMillis()-currentTimeMillis));

        transaction.getLifeCycle().setIsComplete(false);
        if (IfxType.RETURN_RQ.equals(ifxType)){
            if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturned())
                    || LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsReturned()) ){
                transaction.getLifeCycle().setIsReturned(LifeCycleStatus.NOTHING);
            }

        }else if (IfxType. RETURN_RS.equals(ifxType)){
            if ((LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsReturned())
                    || LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturned()))
                    && !ISOResponseCodes.INVALID_ACCOUNT.equals(ifx.getRsCode())){
                transaction.getLifeCycle().setIsReturned(LifeCycleStatus.NOTHING);
            }
            if (!(transaction.getInputMessage().isScheduleMessage() && SchedulerConsts.REVERSAL_TIME_OUT_MSG_TYPE.equals(((ScheduleMessage)transaction.getInputMessage()).getMessageType())))
                transaction.getLifeCycle().setIsComplete(true);

        }else if (/*IfxType. RETURN_REV_RQ.equals(ifxType) || */IfxType.RETURN_REV_REPEAT_RQ.equals(ifxType)){
            if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed())
                    || 	LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsReturnReversed())){
                transaction.getLifeCycle().setIsReturnReversed(LifeCycleStatus.NOTHING);
            }

        }else if (/*IfxType. RETURN_REV_RS.equals(ifxType) || */IfxType. RETURN_REV_REPEAT_RS.equals(ifxType)){
            if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsReturnReversed())
                    || 	LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturnReversed()) ){
                transaction.getLifeCycle().setIsReturnReversed(LifeCycleStatus.NOTHING);
            }

            if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturnReversed())) {
                transaction.getLifeCycle().setIsReturned(LifeCycleStatus.NOTHING);
            }

        }else if (IfxType. TRANSFER_CHECK_ACCOUNT_RQ.equals(ifxType)){
            if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getHasAuthorization())
                    || LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getHasAuthorization())	)
                transaction.getLifeCycle().setHasAuthorization(LifeCycleStatus.NOTHING);

        }else if (IfxType. TRANSFER_CHECK_ACCOUNT_RS.equals(ifxType)){
            if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getHasAuthorization())
                    || LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getHasAuthorization())	)
                transaction.getLifeCycle().setHasAuthorization(LifeCycleStatus.NOTHING);
            transaction.getLifeCycle().setIsComplete(true);

        }else{
            if (ISOFinalMessageType.isReversalRqMessage(ifxType)) {
                if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed())
                        || LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsFullyReveresed())	){
                    transaction.getLifeCycle().setIsFullyReveresed(LifeCycleStatus.NOTHING);
                }
            } else if ((ISOFinalMessageType.isReversalRsMessage(ifxType)
                    || ISOFinalMessageType.isRepeatRsMessage(ifxType) )) {
                if (transaction.getIncomingIfx()/*getInputMessage().getIfx()*/!= null) {
                    IfxType inIfxType = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType();
                    if ((ISOFinalMessageType.isReversalRsMessage(inIfxType)
                            || ISOFinalMessageType.isRepeatRsMessage(inIfxType) ))
                        transaction.getLifeCycle().setIsFullyReveresed(LifeCycleStatus.REQUEST);
                }
            }else{
                if (ISOFinalMessageType.isRequestMessage(ifxType))
                    transaction.getLifeCycle().setIsComplete(false);
                else if (ISOFinalMessageType.isResponseMessage(ifxType)){
                    if (!(transaction.getInputMessage().isScheduleMessage() && SchedulerConsts.REVERSAL_TIME_OUT_MSG_TYPE.equals(((ScheduleMessage)transaction.getInputMessage()).getMessageType()))) {
                        if (!ISOFinalMessageType.isTransferToMessage(ifxType) /*&& ProcessContext.get().getMyInstitution().getBin().equals(ifx.getDestBankId())*/){
                            transaction.getLifeCycle().setIsComplete(true);
                        }
                    }
                }
            }
        }

        GeneralDao.Instance.saveOrUpdate(transaction.getLifeCycle());
    }

    public static void updateLifeCycleStatus(Transaction transaction, Ifx ifx) {
        if(ifx == null)
            return;

        IfxType ifxType = ifx.getIfxType();

        if (ISOFinalMessageType.isRequestMessage(ifxType) || isSuccessResponse(ifx)) {
            updateLifeCycleStatusNormally(transaction, ifx);
        } else if (ISOFinalMessageType.isResponseMessage(ifxType)) {
            updateLifeCycleStatusForNotSuccessful(transaction, ifx);
        }
    }

    public static boolean isSuccessResponse(Ifx ifx) {
        if (ifx.getIfxType() == null)
            return false;

        if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {

            if (ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType())) {
                if (ISOResponseCodes.shouldBeRepeated(ifx.getRsCode()))
                    return false;
                else
                    return true;
            } else if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode()))
                return true;
        }
        return false;
    }

    public static void updateLifeCycleStatusNormally(Transaction transaction, Ifx ifx) {
        if (transaction.getLifeCycle() == null || ifx == null)
            return;

        if (ifx != null) {
            IfxType ifxType = ifx.getIfxType();

            if (!ISOFinalMessageType.isRequestMessage(ifxType) &&
                    !ISOFinalMessageType.isResponseMessage(ifxType))
                return;


            boolean isNeedToLockLifeCycle = true;

            if (transaction.getIncomingIfx() != null) {
                IfxType inIfxType = transaction.getIncomingIfx().getIfxType();

                if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsFullyReveresed()) && ISOFinalMessageType.isReversalRsMessage(inIfxType))
                    isNeedToLockLifeCycle = false;
            }

            if (isNeedToLockLifeCycle) {
                logger.debug("Try to get Lock of LifeCycle["+ transaction.getLifeCycleId()+")");

                transaction.getAndLockLifeCycle(LockMode.UPGRADE);

                long currentTimeMillis = System.currentTimeMillis();
                GeneralDao.Instance.optimizedSynchObject(transaction.getLifeCycle());
                logger.debug("LifeCycle[" + transaction.getLifeCycleId() + ") has beeb locked and it's reloaded!, " + (System.currentTimeMillis()-currentTimeMillis));
            }

            if (IfxType.RETURN_RQ.equals(ifxType)) {
                if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturned())) {
                    transaction.getLifeCycle().setIsReturned(LifeCycleStatus.REQUEST);
                    transaction.getLifeCycle().setIsReturnReversed(LifeCycleStatus.NOTHING);
                }
                transaction.getLifeCycle().setIsComplete(false);

            } else if (IfxType.RETURN_RS.equals(ifxType)) {
                if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsReturned())){
                    transaction.getLifeCycle().setIsReturned(LifeCycleStatus.RESPONSE);
                }
                transaction.getLifeCycle().setIsComplete(true);

            } else if (/*IfxType.RETURN_REV_RQ.equals(ifxType) || */IfxType.RETURN_REV_REPEAT_RQ.equals(ifxType)) {
                if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed())){
                    transaction.getLifeCycle().setIsReturnReversed(LifeCycleStatus.REQUEST);
                }
                transaction.getLifeCycle().setIsComplete(false);

            } else if (/*IfxType.RETURN_REV_RS.equals(ifxType) || */IfxType.RETURN_REV_REPEAT_RS.equals(ifxType)) {
                if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsReturnReversed())) {
                    transaction.getLifeCycle().setIsReturnReversed(LifeCycleStatus.RESPONSE);
                }
                transaction.getLifeCycle().setIsComplete(true);

                /*************************/
                //TODO HATMAN daghigh barresi shavad!!!
                if (transaction.getIncomingIfx()/*getInputMessage().getIfx()*/!= null) {
                    IfxType inIfxType = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType();
                    if (/*IfxType.RETURN_REV_RQ.equals(inIfxType) || */IfxType.RETURN_REV_REPEAT_RQ.equals(inIfxType))
                        if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed())){
                            transaction.getLifeCycle().setIsReturnReversed(LifeCycleStatus.REQUEST);
                            transaction.getLifeCycle().setIsComplete(false);
                        }
                }
                /*************************/

                if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturnReversed())) {
                    transaction.getLifeCycle().setIsReturned(LifeCycleStatus.NOTHING);
                }

            } else if (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifxType)) {
                if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getHasAuthorization())){
                    transaction.getLifeCycle().setHasAuthorization(LifeCycleStatus.REQUEST);
                }
            } else if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifxType)) {
                if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getHasAuthorization()))
                    transaction.getLifeCycle().setHasAuthorization(LifeCycleStatus.RESPONSE);
            } else {
                if (ISOFinalMessageType.isReversalRqMessage(ifxType)) {

                    if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed())){
                        transaction.getLifeCycle().setIsFullyReveresed(LifeCycleStatus.REQUEST);
                    }
                    transaction.getLifeCycle().setIsComplete(false);

                } else if (ISOFinalMessageType.isReversalRsMessage(ifxType)
                        || ISOFinalMessageType.isRepeatRsMessage(ifxType)) {

                    /*************************/
                    //TODO HATMAN daghigh barresi shavad!!!
                    //zamani ke az terminal rev miresad
                    //age dorost bood baraye reverse return ham bezarim
                    if (transaction.getIncomingIfx()/*getInputMessage().getIfx()*/!= null) {
                        IfxType inIfxType = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType();

                        if (!ISOFinalMessageType.isReversalRsMessage(inIfxType)
                                && !ISOFinalMessageType.isRepeatRsMessage(inIfxType) ){

                            if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed())){
                                transaction.getLifeCycle().setIsFullyReveresed(LifeCycleStatus.REQUEST);
                                transaction.getLifeCycle().setIsComplete(false);
                            }

                        }else{
                            if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsFullyReveresed())){
                                transaction.getLifeCycle().setIsFullyReveresed(LifeCycleStatus.RESPONSE);
                            }
                            transaction.getLifeCycle().setIsComplete(true);
                        }
                    }
                    /*************************/

                }else{

                    if (ISOFinalMessageType.isRequestMessage(ifxType))
                        transaction.getLifeCycle().setIsComplete(false);
                    else if (ISOFinalMessageType.isResponseMessage(ifxType)) {
                        transaction.getLifeCycle().setIsComplete(true);

                        if (!(transaction.getInputMessage().isScheduleMessage() && SchedulerConsts.REVERSAL_TIME_OUT_MSG_TYPE.equals(((ScheduleMessage) transaction.getInputMessage()).getMessageType()))) {

                            /***** Modified: 2011/11/28 *****/

                            Long mybin = ProcessContext.get().getMyInstitution().getBin();

                            if (ConfigUtil.getBoolean(ConfigUtil.REV_TRANSFER_TO)) {
                                if (ISOResponseCodes.isMessageDone(ifx.getRsCode())) {
                                    transaction.getLifeCycle().setIsComplete(true);

                                } else {
                                    if (mybin.equals(ifx.getBankId())) {
                                        if (!ISOFinalMessageType.isTransferMessage(ifxType)) {
                                            transaction.getLifeCycle().setIsComplete(true);

                                        } else {
                                            transaction.getLifeCycle().setIsComplete(false);
                                        }
                                    } else {
                                        transaction.getLifeCycle().setIsComplete(true);
                                    }
                                }

                            } else {
                                transaction.getLifeCycle().setIsComplete(true);
                                if (!mybin.equals(ifx.getDestBankId()) &&
                                        !ISOResponseCodes.isMessageDone(ifx.getRsCode()) &&
                                        !IfxType.TRANSFER_TO_ACCOUNT_RS.equals(ifxType)) {
                                    transaction.getLifeCycle().setIsComplete(false);
                                }
                            }
                        }
                    }
                }
            }

//			if (ShetabFinalMessageType.isResponseMessage(ifxType) &&
//					transaction.getLifeCycle().getIsComplete()) {
//				if (ErrorCodes.shouldNotBeReversedForTransfer(ifx.getRsCode())) {
//					transaction.getLifeCycle().setIsComplete(false);
//				}
//			}

            if (ISOFinalMessageType.isReturnResponseMessage(ifxType)  ||
                    (ISOFinalMessageType.isResponseMessage(ifxType) && ISOFinalMessageType.isReturnReverseMessage(ifxType))) {
                if (LifeCycleStatus.REQUEST.equals(transaction.getLifeCycle().getIsFullyReveresed())) {
                    transaction.getLifeCycle().setIsComplete(false);
                }

            }
        }
    }

    /*
   * we put clearing flag on transaction of response message.
   * not_cleared flag is to be put on the source of the non-reversal financial transaction
   * disagreement flag is to be put on the source of the reversal transaction
   */
    public static void putFlagOnTransaction(Transaction transaction) {
        Message outgoingMessage = transaction.getOutputMessage();

        if (outgoingMessage == null)
            return;

        Ifx outgoingIfx = outgoingMessage.getIfx();

        Message inputMessage = transaction.getInputMessage();
        if (!inputMessage.isIncomingMessage() || !outgoingIfx.isResponse()) {
            /***
             * Whenever schedule message is being sent out, we put disagreement flag on the source!
             ***/
            if (inputMessage.isScheduleMessage()) {
                Transaction refTrx = transaction.getReferenceTransaction();
                if (refTrx != null) {

                    SettlementInfo refSrcSettleInfo = refTrx.getSourceSettleInfo();
                    if (refSrcSettleInfo != null && !SettledState.NOT_SETTLED.equals(refSrcSettleInfo.getSettledState())) {
                        logger.warn("Put Dispute Flag on Source of trx: " + refTrx.getId()
                                + ", because we generate SCHEDULE message, and trx is settled!");

                        putDesiredFlagForRevTransaction(refTrx, outgoingIfx, new SourceDestination[] { SourceDestination.SOURCE }, ClearingState.DISPUTE);
                    } else {

                        logger.warn("Put Disagree Flag on Source of trx: " + refTrx.getId() + ", because we generate SCHEDULE message!");

                        ClearingState clrState = ClearingState.DISAGREEMENT;
                        if (isPartialyReversalMessage(outgoingIfx)){
                            if (refTrx.getSourceSettleInfo()!= null && AccountingState.COUNTED.equals(refTrx.getSourceSettleInfo().getAccountingState()))
                                clrState = ClearingState.PARTIALLY_REVERSED;
                            else
                                clrState = ClearingState.PARTIALLY_CLEARED;
                        }

                        putDesiredFlagForRevTransaction(refTrx, outgoingIfx, new SourceDestination[] { SourceDestination.SOURCE }, clrState);
                    }
                }
            } else
                return;
        } else {

            DateTime currentTime = DateTime.now();

            //m.rehman: separating BI from financial incase of limit
            //if (ISOFinalMessageType.isFinancialMessage(outgoingIfx.getIfxType())
            if (ISOFinalMessageType.isFinancialMessage(outgoingIfx.getIfxType(),false)
                    && !(ISOFinalMessageType.isReversalMessage(outgoingIfx.getIfxType()))
                    && ! (ISOFinalMessageType.isPartialDispenseMessage(outgoingIfx.getIfxType()))
                    ) {

                if (!"".equals(outgoingIfx.getRsCode())) {
                    setFirstTransactionOfTerminal(outgoingMessage);
//					logger.warn("Transaction not successful; Response code:(" + outgoingIfx.getRsCode() + ")");
                }


                // put not_cleared flag on financial trnx except reversal trnx
                // from POS terminal

                if (ISOResponseCodes.APPROVED.equals(outgoingIfx.getRsCode())) {
                    AccountingState srcAccState = AccountingState.NOT_COUNTED;

                    if ((ISOFinalMessageType.isNeedThirdParty(outgoingIfx.getIfxType())
                            ||
                            ISOFinalMessageType.isLastPurchaseChargeMessage(outgoingIfx.getIfxType())
                            ||
                            ISOFinalMessageType.isBalanceInqueryMessage(outgoingIfx.getIfxType())
                            ||
                            (ISOFinalMessageType.isTransferMessage(outgoingIfx.getIfxType()) && TerminalType.POS.equals(outgoingIfx.getTerminalType()))	)
//	        		&& GlobalContext.getInstance().getMyInstitution().getBin().equals(outgoingIfx.getBankId())) {
                            && ProcessContext.get().getMyInstitution().getBin().equals(outgoingIfx.getBankId())) {
                        srcAccState = AccountingState.NO_NEED_TO_BE_COUNTED;
                        List<Transaction> transactions = new ArrayList<Transaction>();
                        transactions.add(transaction);
                        AccountingService.removeSettlementRecord(transactions, null, null);
                    }

                    SettlementInfo srcSettleInfo = new SettlementInfo(SettledState.NOT_SETTLED,
                            srcAccState, currentTime, transaction);

                    SettlementInfo destSettleInfo = new SettlementInfo(SettledState.NOT_SETTLED,
                            AccountingState.NOT_COUNTED, currentTime, transaction);

                    ClearingInfo destClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);
                    ClearingInfo sourceClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);
                    putFlagOnTransaction(transaction, sourceClearingInfo, destClearingInfo, srcSettleInfo,
                            destSettleInfo);
                }
            } else if ((ISOFinalMessageType.isReversalMessage(outgoingIfx.getIfxType()))) {

                Transaction refTransaction = transaction.getReferenceTransaction();
                if (refTransaction == null) {
                    logger.warn("Reversal Message doesn't have reference trx. switch must have thrown proper exception! (trx: "+transaction.getId()+")");
                    return;
                }

                //TODO check if outputMessage is build, get ifx from outputMessage
                Ifx ifx = inputMessage.getIfx();

//		    Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
                Long myBin = ProcessContext.get().getMyInstitution().getBin();
                /*********************/
                //we are acquirer
                if (myBin.equals(ifx.getBankId())) {

                    boolean setFlag = true;

                    //Mirkamali: Side effect of Task139 and Task140
                    if (isReverseTransaction(ifx).first != null && isReverseTransaction(ifx).first) {
                        try {
                            if (ISOFinalMessageType.isPartialDispenseRevMessage(ifx.getIfxType())) {
                                if (ifx.getAuth_Amt().equals(Util.longValueOf(ifx.getNew_AmtAcqCur()))) {
                                    setFlag = false;
                                }
                            }
                        } catch(Exception e) {
                            logger.error("Exception in partial dispense setFlag");
                        }

                        if (setFlag) {
                            SettlementInfo refSrcSettleInfo = refTransaction.getSourceSettleInfo();
                            if (refSrcSettleInfo != null && !SettledState.NOT_SETTLED.equals(refSrcSettleInfo.getSettledState())) {
                                putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE
									/*,SourceDestination.DESTINATION*/ }, ClearingState.DISPUTE);
                            } else {
                                ClearingState clrState = ClearingState.DISAGREEMENT;
                                if (isPartialyReversalMessage(outgoingIfx)) {
                                    if (refSrcSettleInfo != null && AccountingState.COUNTED.equals(refSrcSettleInfo.getAccountingState()))
                                        clrState = ClearingState.PARTIALLY_REVERSED;
                                    else
                                        clrState = ClearingState.PARTIALLY_CLEARED;
                                }
                                putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE },
                                        clrState);

                            }
                        }
                    }
                }
                //we are issuer
                else if (myBin.equals(ifx.getDestBankId())
                        ) {
                    if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode()) || ISOResponseCodes.INVALID_ACCOUNT.equals(ifx.getRsCode())) {
                        if (ifx.getBankId() != null && ifx.getBankId().equals(Util.longValueOf(ConfigUtil.getProperty(ConfigUtil.SORUSH_CODE).trim()))) {
//							logger.error("SORUSH: 1");
//							logger.error("Rcvd: " + ifx.getReceivedDt().getDayDate().getDate() + "Posted: " + ifx.getTransaction().getReferenceTransaction().getOutgoingIfx().getPostedDt().getDate());
                        }
                        ClearingState clrState = ClearingState.DISAGREEMENT;
                        if (ifx.getBankId() != null && ifx.getBankId().equals(Util.longValueOf(ConfigUtil.getProperty(ConfigUtil.SORUSH_CODE).trim()))
                                && IfxType.SORUSH_REV_REPEAT_RS.equals(ifx.getTransaction().getOutgoingIfx().getIfxType())
//							&& ifx.getReceivedDt().getDayDate().getDate().equals(ifx.getTransaction().getReferenceTransaction().getOutgoingIfx().getPostedDt().getDate()))
                                && ifx.getReceivedDt().getDayDate().getDate().equals(findResponseTrx(ifx.getTransaction().getLifeCycleId(), ifx.getTransaction().getReferenceTransaction()).getOutgoingIfx().getPostedDt().getDate()))
                        {
//						logger.error("SORUSH: 2");
                            clrState = ClearingState.NOT_CLEARED;
                        }

                        if (isPartialyReversalMessage(outgoingIfx)){
                            if (refTransaction.getSourceSettleInfo()!= null && AccountingState.COUNTED.equals(refTransaction.getSourceSettleInfo().getAccountingState()))
                                clrState = ClearingState.PARTIALLY_REVERSED;
                            else
                                clrState = ClearingState.PARTIALLY_CLEARED;
                        }
                        putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE,
                                SourceDestination.DESTINATION }, clrState);

                        //Mirkamali(Task150)
                    } else if (ISOResponseCodes.HOST_NOT_PROCESSING.equals(ifx.getRsCode())
                            || (TrnType.INCREMENTALTRANSFER.equals(ifx.getTrnType()) && ISOResponseCodes.shouldChangeFlagForTransferTo(ifx.getRsCode()))) {

                        logger.fatal("NOT_CLEARDED flag on src of ifx: " + ifx.getId());

                        ClearingState srcClrState = ClearingState.NOT_CLEARED;
                        ClearingState destClrState = ClearingState.DISPUTE;

                        putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.SOURCE}, srcClrState);
                        putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION}, destClrState);
                    } else {
                        putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE,
                                SourceDestination.DESTINATION }, ClearingState.DISPUTE);

                    }
                } else {
                    ClearingState clrState = ClearingState.DISAGREEMENT;
                    if (ifx.getBankId() != null && ifx.getBankId().equals(Util.longValueOf(ConfigUtil.getProperty(ConfigUtil.SORUSH_CODE).trim()))
                            && IfxType.SORUSH_REV_REPEAT_RS.equals(ifx.getTransaction().getOutgoingIfx().getIfxType())
//						&& ifx.getReceivedDt().getDayDate().getDate().equals(ifx.getTransaction().getReferenceTransaction().getOutgoingIfx().getPostedDt().getDate()))
                            && ifx.getReceivedDt().getDayDate().getDate().equals(findResponseTrx(ifx.getTransaction().getLifeCycleId(), ifx.getTransaction().getReferenceTransaction()).getOutgoingIfx().getPostedDt().getDate()))
                    {
                        clrState = ClearingState.NOT_CLEARED;
                    }
                    if (isPartialyReversalMessage(outgoingIfx) ){
                        if (refTransaction.getSourceSettleInfo()!= null && AccountingState.COUNTED.equals(refTransaction.getSourceSettleInfo().getAccountingState()))
                            clrState = ClearingState.PARTIALLY_REVERSED;
                        else
                            clrState = ClearingState.PARTIALLY_CLEARED;
                    }
                    putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[] { SourceDestination.SOURCE },
                            clrState);

                }
            }
            putFlagOnThirdParty(transaction);
            setFirstTransactionOfTerminal(outgoingMessage);
        }
    }

    //Mirkamali(Task140): Put flag on Transfer_From_RS
    public static void putFlagOnTrasnferFromTransaction(Transaction transaction) {

        Message outgoingMessage = transaction.getOutputMessage();

        Ifx outgoingIfx = outgoingMessage.getIfx();

        DateTime currentTime = DateTime.now();

        AccountingState srcAccState = AccountingState.NOT_COUNTED;

        if(ProcessContext.get().getMyInstitution().getBin().equals(outgoingIfx.getBankId())){

            SettlementInfo srcSettleInfo = new SettlementInfo(SettledState.NOT_SETTLED, srcAccState, currentTime, transaction);

            SettlementInfo destSettleInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, currentTime, transaction);

            ClearingInfo destClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);

            ClearingInfo sourceClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);

            transaction.getAndLockLifeCycle(LockMode.UPGRADE);
            if (transaction.getLifeCycle() != null && !LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed()))
                return;
            transaction.setSourceClearingInfo(sourceClearingInfo);
            transaction.setDestinationClearingInfo(destClearingInfo);
            transaction.setSourceSettleInfo(srcSettleInfo);
            transaction.setDestinationSettleInfo(destSettleInfo);
            GeneralDao.Instance.saveOrUpdate(srcSettleInfo);
            GeneralDao.Instance.saveOrUpdate(destSettleInfo);
            GeneralDao.Instance.saveOrUpdate(sourceClearingInfo);
            GeneralDao.Instance.saveOrUpdate(destClearingInfo);
            GeneralDao.Instance.saveOrUpdate(transaction);

//			putFlagOnTransaction(transaction, sourceClearingInfo, destClearingInfo, srcSettleInfo, destSettleInfo);

        }
    }

    private static Boolean isPartialyReversalMessage(Ifx outgoingIfx) {
        Long amt_acq = Util.longValueOf(outgoingIfx.getNew_AmtAcqCur());
        Long amt_iss = Util.longValueOf(outgoingIfx.getNew_AmtIssCur());
        if ((amt_acq != null && !amt_acq.equals(0L)) || (amt_iss != null && !amt_iss.equals(0L)))
            return true;
        return false;
    }

    private static void putDesiredFlagForRevTransaction(Transaction refTransaction, Ifx ifx, SourceDestination[] srcDest, ClearingState desiredClrState) {
        DateTime currentTime = DateTime.now();
        ClearingInfo changeClrInfo;
        ClearingInfo newClearingInfo = new ClearingInfo(desiredClrState, currentTime);
        for(SourceDestination sd : srcDest) {
            changeClrInfo = SourceDestination.SOURCE.equals(sd) ?
                    refTransaction.getSourceClearingInfo() :
                    refTransaction.getDestinationClearingInfo();
            changeClearingInfoAndPutDataToIfx(ifx, refTransaction, changeClrInfo, newClearingInfo, sd);
        }

        // don't put any flag on reversal trx
        for (Transaction t :findAllNotRevTransactionsReferenceTo(refTransaction)) {
            if (t.getIncomingIfx()/*getInputMessage().getIfx()*/!= null && t.getIncomingIfx()/*getInputMessage().getIfx()*/.isResponse()) {
                for(SourceDestination sd : srcDest) {
                    changeClrInfo = SourceDestination.SOURCE.equals(sd) ?
                            t.getSourceClearingInfo() :
                            t.getDestinationClearingInfo();
                    changeClearingInfoAndPutDataToIfx(ifx, t, changeClrInfo, newClearingInfo, sd);
                }
            }
        }
        MTNChargeService.unlockCharge(ifx, refTransaction, ClearingState.DISAGREEMENT.equals(newClearingInfo.getClearingState()));
    }

    public static void putDesiredFlagForNormalTransaction(Transaction refTransaction, Ifx ifx, SourceDestination[] srcDest, ClearingState desiredClrState) {
        DateTime currentTime = DateTime.now();
        ClearingInfo changeClrInfo;
        ClearingInfo newClearingInfo = new ClearingInfo(desiredClrState, currentTime);
        for(SourceDestination sd : srcDest) {
            changeClrInfo = SourceDestination.SOURCE.equals(sd) ?
                    refTransaction.getSourceClearingInfo() :
                    refTransaction.getDestinationClearingInfo();
            changeClearingInfoAndPutDataToIfx(ifx, refTransaction, changeClrInfo, newClearingInfo, sd);
        }
    }

    public static void putFlagOnOurReversalTransaction(Transaction transaction, Boolean isPartially, String cause) {
        Transaction refTransaction = transaction.getReferenceTransaction();

        //age flag nadare, lozoomi nadare kare khasi konim
        if (refTransaction.getDestinationClearingInfo() == null)
            return;

        Ifx ifx = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/;

        if( refTransaction == null ||
                refTransaction.getSourceClearingInfo() == null )
            return;

        if ((ISOResponseCodes.APPROVED.equals(ifx.getRsCode())
                || ISOResponseCodes.INVALID_ACCOUNT.equals(ifx.getRsCode()))) {


            ClearingState srcClrState = ClearingState.DISAGREEMENT;
            ClearingState destClrState = ClearingState.DISAGREEMENT;

            if ((ATMErrorCodes.ATM_NO_CARD_REJECTED+"").equals(cause)){
                srcClrState = ClearingState.NO_CARD_REJECTED;
            }else if ((ATMErrorCodes.ATM_CACH_HANDLER+"").equals(cause)){
                srcClrState = ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED;
                destClrState = ClearingState.NOT_CLEARED;
            }

            if (isPartially!= null && isPartially){
                if (refTransaction.getSourceSettleInfo()!= null && AccountingState.COUNTED.equals(refTransaction.getSourceSettleInfo().getAccountingState())) {
                    srcClrState = ClearingState.PARTIALLY_REVERSED;
                    destClrState = ClearingState.PARTIALLY_REVERSED;
                } else {
                    srcClrState = ClearingState.PARTIALLY_CLEARED;
                    destClrState = ClearingState.PARTIALLY_CLEARED;
                }
            }


            putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.SOURCE}, srcClrState);
            putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION}, destClrState);

        } else if (ISOResponseCodes.HOST_NOT_PROCESSING.equals(ifx.getRsCode())) {

            logger.fatal("NOT_CLEARDED flag on dest of ifx: " + ifx.getId());

            ClearingState srcClrState = ClearingState.DISPUTE;
            ClearingState destClrState = ClearingState.NOT_CLEARED;

            putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.SOURCE}, srcClrState);
            putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION}, destClrState);

        } else {
            putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION, SourceDestination.SOURCE}, ClearingState.DISPUTE);
            /******************/
/*			 Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
			 //we are acquirer
			if (myBin.equals(ifx.getBankId()))
				putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION}, ClearingState.DISPUTE);
			//we are a switch between other switches
			else if (!myBin.equals(ifx.getBankId()) && !myBin.equals(ifx.getDestBankId()))
				putDesiredFlagForRevTransaction(refTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION}, ClearingState.DISPUTE);
*/
            /******************/

        }
	/*	else if ((ErrorCodes.APPROVED.equals(inputMessage.getIfx().getRsCode())
					|| ErrorCodes.INVALID_ACCOUNT.equals(inputMessage.getIfx().getRsCode()))
				&& referenceTransaction != null) {
			ClearingInfo destClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);
			ClearingInfo sourceClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);

			transaction.setSourceClearingInfo(sourceClearingInfo);
	        transaction.setDestinationClearingInfo(destClearingInfo);
//	        transaction.setSourceSettleInfo(settlementInfo);
//	        GeneralDao.Instance.saveOrUpdate(settlementInfo);
	        GeneralDao.Instance.saveOrUpdate(sourceClearingInfo);
	        GeneralDao.Instance.saveOrUpdate(destClearingInfo);
	        GeneralDao.Instance.saveOrUpdate(transaction);
		}*/
    }

    public static void putFlagOnOurSettlementTransaction(Transaction transaction) {
        Transaction firstTransaction = transaction.getFirstTransaction();

//		if (firstTransaction.getDestinationClearingInfo() == null)
//			return;

        Ifx ifx = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/;

        if( firstTransaction == null)
            return;

        if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode())) {

            putDesiredFlagForNormalTransaction(firstTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION}, ClearingState.CLEARED);
        } else {
            putDesiredFlagForNormalTransaction(firstTransaction, ifx, new SourceDestination[]{SourceDestination.DESTINATION}, ClearingState.DISAGREEMENT);
        }
    }

    public static Transaction getRefrenceTransactionSorush(Ifx ifx) throws Exception{
        Transaction retVal = new Transaction();
        List<Transaction> result = TransactionService.getReferenceOfSorushTransaction(ifx, false);
        if (result == null || result.size() <= 0) {
            throw new ReferenceTransactionNotFoundException("No Reference Transaction was found for the SORUSH request: "+ ifx.getTransaction().getId());
        } else if (result.size() != 1) {
            List<Transaction> trxOk = new ArrayList<Transaction>();
            for (Transaction transaction: result) {
                Transaction responseTrx = TransactionService.findResponseTrx(transaction.getLifeCycleId(), transaction);
                if (responseTrx != null && responseTrx.getOutgoingIfx() != null) {
                    if (ISOResponseCodes.APPROVED.equals(responseTrx.getOutgoingIfx().getRsCode())) {
                        trxOk.add(transaction);
                    }
                }
            }
            if (trxOk.size() <= 0) {
                throw new ReferenceTransactionNotFoundException("No Reference Transaction was found for the SORUSH request: "+ ifx.getTransaction().getId());
            } else if (trxOk.size() != 1) {
                throw new ReferenceTransactionNotFoundException("more than one Reference Transaction were found for the SORUSH request: "+ ifx.getTransaction().getId() +
                        ", " + ToStringBuilder.reflectionToString(result.toArray(), ToStringStyle.MULTI_LINE_STYLE));
            } else {
                retVal = trxOk.get(0);
                ifx = setOriginalData(ifx, retVal);
            }
        } else {
            retVal = result.get(0);
            ifx = setOriginalData(ifx, retVal);
        }
        return retVal;
    }


    public static Ifx setOriginalData(Ifx incomingIfx, Transaction refTrnx) {
        Ifx refIncomingIfx;
        if (refTrnx != null) {
            refIncomingIfx = refTrnx.getIncomingIfx()/*getInputMessage().getIfx()*/;

            if (ISOFinalMessageType.isReturnRq(incomingIfx.getIfxType())) {
                incomingIfx.setAuth_Amt(refIncomingIfx.getAuth_Amt());
                incomingIfx.setReal_Amt(incomingIfx.getReal_Amt());
                incomingIfx.setTrx_Amt(incomingIfx.getTrx_Amt());
                incomingIfx.setSec_Amt(refIncomingIfx.getSec_Amt());
            }

            if (incomingIfx.getOriginalDataElements().getFwdBankId() == null)
                incomingIfx.getOriginalDataElements().setFwdBankId(refIncomingIfx.getDestBankId());
            if (incomingIfx.getOriginalDataElements().getBankId() == null ||
                    incomingIfx.getOriginalDataElements().getBankId().equals(Util.longValueOf(ConfigUtil.getProperty(ConfigUtil.SHAPARAK_BIN).trim())))
                incomingIfx.getOriginalDataElements().setBankId(refIncomingIfx.getBankId());
            if (incomingIfx.getOriginalDataElements().getOrigDt() == null)
                incomingIfx.getOriginalDataElements().setOrigDt(refIncomingIfx.getOrigDt());
            if (incomingIfx.getOriginalDataElements().getTrnSeqCounter() == null)
                incomingIfx.getOriginalDataElements().setTrnSeqCounter(refIncomingIfx.getSrc_TrnSeqCntr());
            if(incomingIfx.getOriginalDataElements().getNetworkTrnInfo() == null)
                incomingIfx.getOriginalDataElements().setNetworkTrnInfo(refIncomingIfx.getNetworkTrnInfo().toString());

            if (!Util.hasText(incomingIfx.getPINBlock())) {
                if (Util.hasText(incomingIfx.getAppPAN())
                        && incomingIfx.getAppPAN().equals(refIncomingIfx.getAppPAN())) {
                    incomingIfx.setPINBlock(refIncomingIfx.getPINBlock());
                }
            }
        }

        return incomingIfx;
    }

    private static void putFlagOnThirdParty(Transaction transaction) {
        DateTime currentTime = DateTime.now();
        Ifx ifx = transaction./*getFirstTransaction().*/getIncomingIfx()/*getInputMessage().getIfx()*/;
        Ifx refIfx = transaction.getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
        if (transaction.getSourceClearingInfo() != null
                &&
                (ClearingState.CLEARED.equals(transaction.getSourceClearingInfo().getClearingState()) ||
                        ClearingState.NOT_CLEARED.equals(transaction.getSourceClearingInfo().getClearingState()))
                && ISOFinalMessageType.isNeedThirdParty(refIfx.getIfxType())
//        		&& GlobalContext.getInstance().getMyInstitution().getBin().equals(refIfx.getBankId())) {
                && ProcessContext.get().getMyInstitution().getBin().equals(refIfx.getBankId())) {

            FinancialEntity entity = ifx.getThirdParty(refIfx.getIfxType());

            if (entity != null) {
                SettlementInfo thirdSettleInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, currentTime, transaction);
                putFlagOnTransaction(transaction, thirdSettleInfo);
            }
        }
    }

    private static void putFlagOnTransaction(Transaction transaction, SettlementInfo thirdSettleInfo) {
        transaction.getAndLockLifeCycle(LockMode.UPGRADE);
        if (transaction.getLifeCycle() != null && !LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed()))
            return;
        transaction.setThirdPartySettleInfo(thirdSettleInfo);
        GeneralDao.Instance.saveOrUpdate(thirdSettleInfo);
        GeneralDao.Instance.saveOrUpdate(transaction);

        Transaction firstTransaction = transaction.getFirstTransaction();
        if (firstTransaction != null) {
            firstTransaction.setThirdPartySettleInfo(transaction.getThirdPartySettleInfo());
            GeneralDao.Instance.saveOrUpdate(firstTransaction);
        }
    }

    private static void putFlagOnTransaction(Transaction transaction, ClearingInfo srcClearingInfo,
                                             ClearingInfo destClearingInfo, SettlementInfo srcSettleInfo, SettlementInfo destSettleInfo) {
        transaction.getAndLockLifeCycle(LockMode.UPGRADE);
        if (transaction.getLifeCycle() != null && !LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed()))
            return;
        transaction.setSourceClearingInfo(srcClearingInfo);
        transaction.setDestinationClearingInfo(destClearingInfo);
        transaction.setSourceSettleInfo(srcSettleInfo);
        transaction.setDestinationSettleInfo(destSettleInfo);
        GeneralDao.Instance.saveOrUpdate(srcSettleInfo);
        GeneralDao.Instance.saveOrUpdate(destSettleInfo);
        GeneralDao.Instance.saveOrUpdate(srcClearingInfo);
        GeneralDao.Instance.saveOrUpdate(destClearingInfo);
        GeneralDao.Instance.saveOrUpdate(transaction);
        copyFlagsToFirstTransaction(transaction);
    }

    private static ClearingInfo changeClearingInfoAndPutDataToIfx(Ifx ifx, Transaction transaction, ClearingInfo src, ClearingInfo finalClr, SourceDestination srcDest) {
        if (src == null) {
            logger.info("last clr info of trx:" + transaction.getId() + " is NULL!!");
            logger.info("new clr state:" + finalClr.getClearingState().getState() + " not to be replaced!");
            return null;
        }

        if (src.getClearingState().equals(finalClr.getClearingState())) {
            logger.info("last and new clr state of trx:" + transaction.getId() + " is same: " + src.getClearingState().getState());
            return src;
        }

        if (ClearingState.RECONCILED.equals(src.getClearingState())) {
            logger.info("last clr state of trx:" + transaction.getId() + " is RECONCILED!");
            logger.info("new clr state:" + finalClr.getClearingState().getState() + " not to be replaced!");
            return null;
        }

        if (ClearingState.CLEARED.equals(src.getClearingState()) ||
                ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED.equals((src.getClearingState()))) {
            logger.info("last clr state of trx:" + transaction.getId() + " is " + src.getClearingState().getName() + " !");
            logger.info("new clr state:" + finalClr.getClearingState().getState() + " not to be replaced!");
            return null;
        }


        if (ClearingState.CLEARED.equals(finalClr.getClearingState())){
            if (!ISOFinalMessageType.isBalanceInqueryMessage(transaction.getOutgoingIfx().getIfxType()))
                if (OnlinePerTransactionSettlementServiceImpl.class.equals(ifx.getEndPointTerminal().getOwnOrParentClearingProfile().getSettlementClass())) {
                    List<Terminal> terminals = new ArrayList<Terminal>();
                    terminals.add(ifx.getEndPointTerminal());
                    OnlinePerTransactionSettlementThread thread = new OnlinePerTransactionSettlementThread(terminals);
                    Thread settlementThread = new Thread(thread);
                    logger.debug("Thread: " + settlementThread.getName() + " is starting...");
                    settlementThread.start();
                }
            //Mirkamali(Task151)
//            if(SinapsServices.isSinapsTransaction(transaction))
//                SchedulerService.createSinapsJobInfo(transaction);

        }


        ifx.addClearingTransaction(transaction, src, finalClr, srcDest);

        src.setClearingState(finalClr.getClearingState());
        src.setClearingDate(finalClr.getClearingDate());

        putNewSettlementRecordToRemoving(transaction, finalClr.getClearingState(), srcDest);

        GeneralDao.Instance.saveOrUpdate(src);
        return src;
    }
/*
	public static void putNewSettlementRecordToRemoving(Transaction transaction, ClearingState clearingState, SourceDestination srcDest) {
		try {
			Terminal endPointTerminal = transaction.getOutgoingIfx().getEndPointTerminal();
			ClearingProfile clrProf = null;
			if(endPointTerminal != null && endPointTerminal.getOwnOrParentClearingProfileId() != null){
				clrProf = ProcessContext.get().getClearingProfile(endPointTerminal.getOwnOrParentClearingProfileId());
			}

			if ( clrProf != null &&
					clrProf.getProcessType() != null &&
					ClearingProcessType.ONLINE.equals(clrProf.getProcessType())) {

				if (ClearingState.DISAGREEMENT.equals(clearingState) ||
						ClearingState.DISPUTE.equals(clearingState) ||
						ClearingState.SUSPECTED_DISPUTE.equals(clearingState)) {

					if (SourceDestination.SOURCE.equals(srcDest)) {
						if(SettledState.NOT_SETTLED.equals(transaction.getSourceSettleInfo().getSettledState())){
							if (!AccountingState.NOT_COUNTED.equals(transaction.getSourceSettleInfo().getAccountingState())
									&&
									!AccountingState.NO_NEED_TO_BE_COUNTED.equals(transaction.getSourceSettleInfo().getAccountingState())) {

								logger.debug("insert new settlement record, transaction: " + transaction.getId() + ", clrState:  " + clearingState.getName());
								SettlementRecord record = new SettlementRecord(transaction, transaction.getOutgoingIfx().getId(), endPointTerminal, transaction.getBeginDateTime().getDateTimeLong());
								GeneralDao.Instance.saveOrUpdate(record);
							}if (AccountingState.COUNTED.equals(transaction.getSourceSettleInfo().getAccountingState())){
								List<Transaction> transactions = new ArrayList<Transaction>();
								transactions.add(transaction);
								AccountingService.removeSettlementRecord(transactions, clrProf, null);
								logger.debug("clearingstate is " + clearingState
										+ " and settledState is NOT_SETTLED "
										+ " and accountingState is COUNTED ... so we should remove it from settlementRecord ");
							}
						}
						logger.debug("clearingSatet is " + clearingState +" but settledState is not NOT_SETTLED "
								+" so we cant add it to SettlementRecord");

					}
//					else if (SourceDestination.DESTINATION.equals(srcDest)) {
//						if (!AccountingState.NOT_COUNTED.equals(transaction.getDestinationSettleInfo().getAccountingState())
//								&&
//							!AccountingState.NO_NEED_TO_BE_COUNTED.equals(transaction.getDestinationSettleInfo().getAccountingState())) {
//
//							logger.debug("insert new settlement record, transaction: " + transaction.getId() + ", clrState:  " + clearingState.getName());
//							SettlementRecord record = new SettlementRecord(transaction, transaction.getOutputMessage().getIfxId(), endPointTerminal, transaction.getBeginDateTime().getDateTimeLong());
//							GeneralDao.Instance.saveOrUpdate(record);
//						}
//					}

//					logger.debug("insert new settlement record, transaction: " + transaction.getId() + ", clrState:  " + clearingState.getName());
//					SettlementRecord record = new SettlementRecord(transaction, transaction.getOutputMessage().getIfxId(), endPointTerminal, transaction.getBeginDateTime().getDateTimeLong());
//					GeneralDao.Instance.saveOrUpdate(record);
				}
			}
		} catch(Exception e) {
			logger.info("No Settlement Record adde to transaction: " + transaction.getId());
		}

	}
*/

    public static void putNewSettlementRecordToRemoving(Transaction transaction, ClearingState clearingState, SourceDestination srcDest) {
        try {
            Terminal endPointTerminal = transaction.getOutgoingIfx().getEndPointTerminal();
            ClearingProfile clrProf = null;
            if(endPointTerminal != null && endPointTerminal.getOwnOrParentClearingProfileId() != null){
                clrProf = ProcessContext.get().getClearingProfile(endPointTerminal.getOwnOrParentClearingProfileId());
            }

            if ( clrProf != null &&
                    clrProf.getProcessType() != null &&
                    ClearingProcessType.ONLINE.equals(clrProf.getProcessType())) {

                if (ClearingState.DISAGREEMENT.equals(clearingState) ||
                        ClearingState.DISPUTE.equals(clearingState) ||
                        ClearingState.SUSPECTED_DISPUTE.equals(clearingState)) {

                    if (SourceDestination.SOURCE.equals(srcDest)) {
                        if(SettledState.NOT_SETTLED.equals(transaction.getSourceSettleInfo().getSettledState())){
                            if (!AccountingState.NOT_COUNTED.equals(transaction.getSourceSettleInfo().getAccountingState())
                                    &&
                                    !AccountingState.NO_NEED_TO_BE_COUNTED.equals(transaction.getSourceSettleInfo().getAccountingState())) {

                                logger.debug("insert new settlement record, transaction: " + transaction.getId() + ", clrState:  " + clearingState.getName());
                                try{
                                    SettlementRecord record = SettlementRecord.getInstance(transaction, transaction.getOutgoingIfx(), endPointTerminal.getClearingProfile(), endPointTerminal, transaction.getBeginDateTime().getDateTimeLong());
                                    GeneralDao.Instance.saveOrUpdate(record);
                                }catch (Exception e){
                                    logger.error("Exception in add stlRecord" + e, e);
                                }
                            }else if (AccountingState.NOT_COUNTED.equals(transaction.getSourceSettleInfo().getAccountingState())){
                                List<Transaction> transactions = new ArrayList<Transaction>();
                                transactions.add(transaction);
                                AccountingService.removeSettlementRecord(transactions, clrProf, null);
                                logger.debug("clearingstate is " + clearingState
                                        + " and settledState is NOT_SETTLED "
                                        + " and accountingState is COUNTED ... so we should remove it from settlementRecord ");
                            }
                        }
                        logger.debug("clearingSatet is " + clearingState +" but settledState is not NOT_SETTLED "
                                +" so we cant add it to SettlementRecord");

                    }
//					else if (SourceDestination.DESTINATION.equals(srcDest)) {
//						if (!AccountingState.NOT_COUNTED.equals(transaction.getDestinationSettleInfo().getAccountingState())
//								&&
//							!AccountingState.NO_NEED_TO_BE_COUNTED.equals(transaction.getDestinationSettleInfo().getAccountingState())) {
//
//							logger.debug("insert new settlement record, transaction: " + transaction.getId() + ", clrState:  " + clearingState.getName());
//							SettlementRecord record = new SettlementRecord(transaction, transaction.getOutputMessage().getIfxId(), endPointTerminal, transaction.getBeginDateTime().getDateTimeLong());
//							GeneralDao.Instance.saveOrUpdate(record);
//						}
//					}

//					logger.debug("insert new settlement record, transaction: " + transaction.getId() + ", clrState:  " + clearingState.getName());
//					SettlementRecord record = new SettlementRecord(transaction, transaction.getOutputMessage().getIfxId(), endPointTerminal, transaction.getBeginDateTime().getDateTimeLong());
//					GeneralDao.Instance.saveOrUpdate(record);
                }
            }
        } catch(Exception e) {
            logger.info("No Settlement Record adde to transaction: " + transaction.getId());
        }

    }

    public static void copyFlagsToFirstTransaction(Transaction transaction) {
        Transaction firstTransaction = transaction.getFirstTransaction();
        if (firstTransaction != null) {
            firstTransaction.setSourceClearingInfo(transaction.getSourceClearingInfo());
            firstTransaction.setDestinationClearingInfo(transaction.getDestinationClearingInfo());
            firstTransaction.setSourceSettleInfo(transaction.getSourceSettleInfo());
            firstTransaction.setDestinationSettleInfo(transaction.getDestinationSettleInfo());

            firstTransaction.setThirdPartySettleInfo(transaction.getThirdPartySettleInfo());
            GeneralDao.Instance.saveOrUpdate(firstTransaction);
            Transaction refFirstTransaction = firstTransaction.getFirstTransaction();
            if (refFirstTransaction!=null){
                try {
                    if (IfxType.TRANSFER_FROM_ACCOUNT_RQ.equals(refFirstTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())){
                        //Mirkamali(Task140)
                        if(refFirstTransaction.getSourceClearingInfo() != null) {
                            refFirstTransaction.setSourceClearingInfo(null);
                            GeneralDao.Instance.delete(refFirstTransaction.getSourceClearingInfo());
                        }
                        if(refFirstTransaction.getDestinationClearingInfo() != null) {
                            refFirstTransaction.setDestinationClearingInfo(null);
                            GeneralDao.Instance.delete(refFirstTransaction.getDestinationClearingInfo());
                        }
                        if(refFirstTransaction.getSourceSettleInfo() != null){
                            refFirstTransaction.setSourceSettleInfo(null);
                            GeneralDao.Instance.delete(refFirstTransaction.getSourceSettleInfo());
                        }
                        if(refFirstTransaction.getDestinationSettleInfo() != null) {
                            refFirstTransaction.setDestinationSettleInfo(null);
                            GeneralDao.Instance.delete(refFirstTransaction.getDestinationSettleInfo());
                        }
                        refFirstTransaction.setSourceClearingInfo(transaction.getSourceClearingInfo());
                        refFirstTransaction.setDestinationClearingInfo(transaction.getDestinationClearingInfo());
                        refFirstTransaction.setSourceSettleInfo(transaction.getSourceSettleInfo());
                        refFirstTransaction.setDestinationSettleInfo(transaction.getDestinationSettleInfo());

                        refFirstTransaction.setThirdPartySettleInfo(transaction.getThirdPartySettleInfo());
                        GeneralDao.Instance.saveOrUpdate(refFirstTransaction);
                    }
                } catch (Exception e) {
                    logger.error("Encouter with an Exception "+ e.getClass().getSimpleName()+" in setting flag on "+ refFirstTransaction.getId());
                }
            }
        }
    }

    private static void setFirstTransactionOfTerminal(Message outgoingMessage) {
        Terminal terminal = outgoingMessage.getEndPointTerminal();
        if(terminal == null)
            return;

        if (DateTime.UNKNOWN.equals(terminal.getFirstTrxDate()) || terminal.getStatus().equals(TerminalStatus.NOT_INSTALL)) {
            terminal.setFirstTrxDate(outgoingMessage.getTransaction().getBeginDateTime());
            GeneralDao.Instance.saveOrUpdate(terminal);
        }
    }

    public static List<Transaction> getTransactionsFromMessages(List<Message> messages) {
        List<Transaction> result = new ArrayList<Transaction>();
        if (messages != null && messages.size() > 0) {
            for (Message message : messages) {
                result.add(message.getTransaction());
            }
        }
        return result;
    }

    @NotUsed
    public static List<Transaction> getTransactionsFromIfxs(List<Ifx> ifxs) {
        List<Transaction> result = new ArrayList<Transaction>();
        if (ifxs != null && ifxs.size() > 0) {
            for (Ifx ifx: ifxs) {
                result.add(ifx.getTransaction());
            }
        }
        return result;
    }

    public static List<Transaction> getTransactionsFromIfx(List<Ifx> ifxList) {
        List<Transaction> result = new ArrayList<Transaction>();
        if (ifxList != null && ifxList.size() > 0) {
            for (Ifx ifx : ifxList) {
                result.add(ifx.getTransaction());
            }
        }
        return result;
    }

    public static List<Transaction> getTransactionsFromIfxSettlement(List<IfxSettlement> ifxList) {
        List<Transaction> result = new ArrayList<Transaction>();
        if (ifxList != null && ifxList.size() > 0) {
            for (IfxSettlement ifx : ifxList) {
                result.add(GeneralDao.Instance.load(Transaction.class, ifx.getTransactionId()));
            }
        }
        return result;
    }

    public static String findSrcDest(Terminal terminal) {
        switch (terminal.getClearingMode()) {
            case ACQUIER:
                return "destination";
            case ISSUER:
                return "source";
            case TERMINAL:
                return "source";
            case THIRDPARTY:
                return "thirdParty";
        }
        return null;
    }

    public static SettlementInfo getRelatedSettleInfo(Transaction transaction, Terminal terminal) {
        switch (terminal.getClearingMode()) {
            case ACQUIER:
                return transaction.getDestinationSettleInfo();
            case ISSUER:
                return transaction.getSourceSettleInfo();
            case TERMINAL:
                return transaction.getSourceSettleInfo();
            case THIRDPARTY:
                return transaction.getThirdPartySettleInfo();
        }
        return null;
    }

    @NotUsed
    public boolean isSentForSettlementAndNotDisAgreement(ClearingInfo clearingInfo, SettlementInfo settlementInfo) {
        if (clearingInfo == null || settlementInfo == null)
            return false;
        if ((ClearingState.CLEARED.equals(clearingInfo.getClearingState()) || ClearingState.NOT_CLEARED.equals(
                clearingInfo.getClearingState()))
                && !SettledState.NOT_SETTLED.equals(settlementInfo.getSettledState()))
            return true;
        return false;
    }

    public static Transaction findcorrespondingResponse(Transaction referenceTransaction) {
        Ifx ifx = referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;

        String queryString = "select i.transaction from Ifx as i"
//				+ " inner join m.ifx as i"
                + " where i.networkTrnInfo.Src_TrnSeqCntr = :TrnSeqCntr "
                + "and i.trnType= :TrnType "
                + "and i.networkTrnInfo.NetworkRefId= :NetworkRefId "
                + "and i.networkTrnInfo.BankId = :BankId "
                + "and i.networkTrnInfo.DestBankId = :FwdBankId "
                + "and i.networkTrnInfo.OrigDt = :OrigDt "
                + "and (i.ifxDirection = :IfxDirection) "
                + "and i.request = false"
//				+ " and i.ifxType in "
//				+ IfxType.getRsOrdinalsCollectionString()
                ;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("TrnSeqCntr", ifx.getSrc_TrnSeqCntr());
        params.put("TrnType", ifx.getTrnType());
        params.put("NetworkRefId", ifx.getNetworkRefId());
        params.put("BankId", ifx.getBankId());
        params.put("FwdBankId", ifx.getDestBankId());
        params.put("OrigDt", ifx.getOrigDt());
        params.put("IfxDirection", IfxDirection.INCOMING);

        return (Transaction) GeneralDao.Instance.findObject(queryString, params);
    }

    @NotUsed
    public Transaction findLastTransaction(Terminal terminal) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @NotUsed
    public long getTotalTransactionAmountToday(Terminal terminal) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public static List<Transaction> getDesiredTrxs(ClearingState clearingState, Date startDate, Date endDate,
                                                   boolean isDestination) {
        String query = "";
        Map<String, Object> params = new HashMap<String, Object>();

        String clearingInfo = "destinationClearingInfo";
        if (!isDestination)
            clearingInfo = "sourceClearingInfo";

        query = "from Transaction t where " +
                " t." + clearingInfo + ".clearingState = :clearingState ";

        //FIXME: startDate~String?
        if (/*!"".equals(startDate) &&*/ startDate != null) {
            query +=
                    " and (t." + clearingInfo + ".clearingDate.dayDate = :startDate "
                            +
                            " and t." + clearingInfo + ".clearingDate.dayTime >= :startTime) "
                            +
                            " or (t." + clearingInfo + ".clearingDate.dayDate > :startDate) "
            ;
            params.put("startDate", new DateTime(startDate).getDayDate());
            params.put("startTime", new DateTime(startDate).getDayTime());
        }

        //FIXME: startDate~String?
        if (/*!"".equals(endDate) &&*/ endDate != null) {
            query +=
                    " and (t." + clearingInfo + ".clearingDate.dayDate = :endDate "
                            +
                            " and t." + clearingInfo + ".clearingDate.dayTime <= :endTime) "
                            +
                            " or (t." + clearingInfo + ".clearingDate.dayDate < :endDate)"
            ;
            params.put("endDate", new DateTime(endDate).getDayDate());
            params.put("endTime", new DateTime(endDate).getDayTime());
        }
        params.put("clearingState", clearingState);

        return GeneralDao.Instance.find(query, params);
    }

    //TASK Task136 [26143] - Change Mojodi Pazirande query Pasargad
    public static Long findPosTransactions(Long terminalCode, List<SettledState> stlList, List<ClearingState> clrList,
                                           List<IfxType> ifxTypes, List<TrnType> trnTypes, DateTime settlementTime) {
        String query = "select sum(i.eMVRqData.Real_Amt) " +
                " from Ifx i "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9) and "
                + " i.endPointTerminalCode = " + terminalCode
                + " and i.request = false "
                + " and i.ifxDirection = " + IfxDirection.OUTGOING.getType()
                + " and i.receivedDtLong >  " + settlementTime.getDateTimeLong()
                + " and i.receivedDtLong <= " + DateTime.now().getDateTimeLong();

        Map<String, Object> params = new HashMap<String, Object>();
//		params.put("terminal", terminalCode);
//		params.put("stlDate", settlementTime.getDateTimeLong());
//		params.put("dir" , IfxDirection.OUTGOING);


        if (ifxTypes != null && !ifxTypes.isEmpty()) {
//			query += " and i.ifxType in (:ifxTypes) ";
//			params.put("ifxTypes", ifxTypes);
            query += " and i.ifxType in "+IfxType.getIfxTypeOrdinalsOfList(ifxTypes);
        }

        // Task136 [26143] - Change Mojodi Pazirande query Pasargad
        if (trnTypes != null && !trnTypes.isEmpty()) {
            query += " and i.trnType in (:trnTypes) ";
            params.put("trnTypes", trnTypes);
        }

        if (stlList != null && stlList.size() > 0) {
            query += " and i.transaction.sourceSettleInfo.settledState in (:settledState) ";
            params.put("settledState", stlList);
        }

        if (clrList != null && clrList.size() > 0) {
            query += " and i.transaction.sourceClearingInfo.clearingState in (:clearingState) ";
            params.put("clearingState", clrList);
        }

        return (Long) GeneralDao.Instance.findUnique(query, params);
    }

    public static Transaction findWithdrawalResponse(Transaction requestTransaction){
        //Mirkamali(Task179): Currency ATM
        String query = "select i.transaction from "+ Ifx.class.getName()+" i "
                + " where i.transaction.firstTransaction = :reqTrx "
                + " and i.ifxType in (:withrawalRs)";
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("reqTrx", requestTransaction);

        parameters.put("withrawalRs", new ArrayList<IfxType>() {
            {
                add(IfxType.WITHDRAWAL_RS);
                add(IfxType.WITHDRAWAL_CUR_RS);
            }
        });
        return (Transaction) GeneralDao.Instance.findObject(query, parameters);
    }

    public static Pair<Boolean, ClearingState> isReverseTransaction(Ifx ifx/*, String cause*/){
        Pair<Boolean, ClearingState> result = new Pair<Boolean, ClearingState>(true, null);

        if (!ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())) {
            return result;
        }

        if (!TerminalType.ATM.equals(ifx.getTerminalType()))
            return result;

        // check this!
        Transaction transaction = ifx.getTransaction();

        Transaction referenceTransaction = transaction.getReferenceTransaction();
        if (referenceTransaction == null)
            return new Pair<Boolean, ClearingState>(false, ClearingState.DISAGREEMENT);

        String refTrx_seqCntr = referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getSrc_TrnSeqCntr();
        if (!IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(ifx.getIfxType())
                &&	!Util.longValueOf(refTrx_seqCntr).equals(Util.longValueOf(ifx.getLast_TrnSeqCntr()))) {

            //Mirkamali(Task139, Task140)
            if(IfxType.TRANSFER_FROM_ACCOUNT_RQ.equals(referenceTransaction.getOutgoingIfx().getIfxType())) {
                return new Pair<Boolean, ClearingState>(null, null);
            }

            logger.info("refTrxSeqCntr: " + refTrx_seqCntr + ", lastTrxSeqCntr: " + ifx.getLast_TrnSeqCntr());
            return result;
        }

        if (!LastStatusIssued.ERROR_STATUS_SENT.equals(ifx.getLastTrxStatusIssue())	)
            return result;
        //Mirkamali(Task179)
        if (!TrnType.WITHDRAWAL.equals(ifx.getTrnType()) && !TrnType.WITHDRAWAL_CUR.equals(ifx.getTrnType()))
            return result;

        String query = "from "+ Message.class.getName() + " m "
                + " where m.ifx.ifxType in (:cash_handler, :card_reader_writer) "
                + " and m.transaction.lifeCycleId = :lifeCycle";
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("cash_handler", IfxType.CASH_HANDLER);
        parameters.put("card_reader_writer", IfxType.CARD_READER_WRITER);
        parameters.put("lifeCycle", transaction.getLifeCycleId());

        List<Message> msgList = GeneralDao.Instance.find(query, parameters);
        for (Message m: msgList){
            Ifx i = m.getIfx();
            if (IfxType.CASH_HANDLER.equals(i.getIfxType())){
                if (TransactionStatusType.NOTES_DISPENSED_UNKNOWN.equals(i.getTransactionStatus())) {
                    return new Pair<Boolean, ClearingState>(false, ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
                } else {
                    try {
                        if (IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(ifx.getIfxType())) {
                            if (transaction.getFirstTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/.getActualDispenseAmt()!= null){
                                ifx.setNew_AmtAcqCur(transaction.getFirstTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/.getActualDispenseAmt()+"");
                                ifx.setNew_AmtIssCur(ifx.getNew_AmtAcqCur());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Encounter with an exception in setting new amount!");
                    }
                    return result;
                }
            }else if (IfxType.CARD_READER_WRITER.equals(i.getIfxType()))
                return new Pair<Boolean, ClearingState>(true, ClearingState.NO_CARD_REJECTED);
            else
                return new Pair<Boolean, ClearingState>(false, ClearingState.SUSPECTED_DISAGREEMENT);
        }

        if (IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(ifx.getIfxType())) {
            return new Pair<Boolean, ClearingState>(true, ClearingState.SUSPECTED_DISAGREEMENT);
        }

        return new Pair<Boolean, ClearingState>(false, ClearingState.SUSPECTED_DISAGREEMENT);
    }


    @NotUsed
    public static Boolean getReturnedSettlementInfo(Transaction transaction){
        String query = "from "+ SettlementInfo.class.getName()+" s "
                + " where s.transaction= :trx "
                +" and s.settlementData.type= :type "
                +" and s.settlementData.settlementReport is not null";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trx", transaction);
        params.put("type", SettlementDataType.RETURNED);

        return GeneralDao.Instance.find(query, params).size() > 0;
    }
    public static Transaction getTransferToTrx(Transaction transaction) {
        String query = "select i.transaction from "+ Ifx.class.getName()+" i"
                + " where i.transaction.firstTransaction = :trx "
                + " and i.transaction.referenceTransaction = :trx"
                + " and i.request = true "
                + " and i.ifxType in (:transferTo)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trx", transaction);
        List<IfxType> transferToList = new ArrayList<IfxType>();
        transferToList.add(IfxType.TRANSFER_TO_ACCOUNT_RQ);
        transferToList.add(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ);
        params.put("transferTo", transferToList);
        return (Transaction) GeneralDao.Instance.findObject(query, params);
    }

    public static String getSecurityString(EMVRqData rqData) {
        if (rqData == null)
            return null;


        String securityParam = DateTime.now() + "|";

        if (rqData.getCardAcctId() != null && Util.hasText(rqData.getCardAcctId().getTrk2EquivData())) {
            securityParam += rqData.getCardAcctId().getTrk2EquivData();
        }
        securityParam += "|";

        if (Util.hasText(rqData.getPINBlock())) {
            securityParam += rqData.getPINBlock();
        }
        securityParam += "|";

        if (rqData.getCardAcctId() != null && Util.hasText(rqData.getCardAcctId().getCVV2())) {
            securityParam += rqData.getCardAcctId().getCVV2();
        }
        securityParam += "|";

        if (rqData.getCardAcctId() != null && rqData.getCardAcctId().getExpDt() != null) {
            securityParam += rqData.getCardAcctId().getExpDt();
        }
        securityParam += "|";

        ;

        return securityParam;
    }

    public static Object getFromSecurity(String securityString, String item) {
        String[] split = securityString.split("\\|");
        if (item.equalsIgnoreCase("INSERT_DATE")) {
            return split[0];

        } else if (item.equalsIgnoreCase("Trk2EquivData")) {
            return split[1];

        } else if (item.equalsIgnoreCase("PINBlock")) {
            return split[2];

        } else if (item.equalsIgnoreCase("CVV2")) {
            return split[3];

        } else if (item.equalsIgnoreCase("ExpDt")) {
            if (split.length >= 5)
                return split[4];
            else
                return null;

        } else
            return null;
    }

    public static List<Transaction> findFirstTransactionsWithoutQuery(Ifx ifx) {
        List<Transaction> result = new ArrayList<Transaction>();
        Transaction lastIncomingTrx = null;
        boolean isNeedQuery = false;
        boolean flag = false;

        Long myBin = ProcessContext.get().getMyInstitution().getBin();
        if (myBin.equals(ifx.getBankId()) &&
                !ISOFinalMessageType.isReversalRsMessage(ifx.getIfxType()) &&
                !ISOMessageTypes.isFinancialAdviceResponseMessage(ifx.getMti()) &&  //m.rehman: for advice
                !ISOMessageTypes.isLoroAdviceResponseMessage(ifx.getMti()) &&   //m.rehman: for loro
                ifx.getTerminalId() != null) {

            TerminalType terminalType = GlobalContext.getInstance().getTerminalType(ifx.getTerminalId());
            if (TerminalType.isPhisycalDeviceTerminal(terminalType)) {

                logger.debug("Searching lastIncoming transaction for bankid:" + ifx.getBankId()
                        + ", origdt:" + ifx.getOrigDt()
                        + ", destbankid:" + ifx.getDestBankId()
                        + ", networkRefid:" + ifx.getNetworkRefId()
                        + ", myNetworkRefId:" + ifx.getMyNetworkRefId()
                        + ", TrnType: " + ifx.getTrnType()
                        + ", TerminalId: " + ifx.getTerminalId());

                String terminalId = ifx.getTerminalId();
                Terminal endPointTerminal = GeneralDao.Instance.load(terminalType.getClassType(), Long.parseLong(terminalId));
                lastIncomingTrx = endPointTerminal.getLastIncomingTransaction();

                if (lastIncomingTrx == null) {
                    logger.debug("incoming RS trx is NOT response of last trx rq: NULL, Query is needed!");
                    return findFirstTransactions(ifx);
                }


                if (myBin.equals(ifx.getDestBankId()) && ifx.getFirstTrxId() != null) {
                    if (ifx.getFirstTrxId().equals(lastIncomingTrx.getId())) {
                        logger.debug("We are acquire and issuer, last incomingTrx of terminal and firstTrx in cms msg is same:"+ lastIncomingTrx);
                        result.add(lastIncomingTrx);
                        return result;

                    } else {
                        logger.debug("We are acquire and issuer, but last incomingTrx of terminal: " + lastIncomingTrx + " different to firstTrx in cms msg: " + ifx.getFirstTrxId());
                    }
                }

                String query = "from Ifx i where i.transaction.id = :trx "
                        + " and i.ifxDirection = :outgoing ";

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("trx", lastIncomingTrx.getId());
                params.put("outgoing", IfxDirection.OUTGOING);

                Ifx lastOutIfx = (Ifx) GeneralDao.Instance.findUniqueObject(query, params);

                if (lastOutIfx != null &&
                        !ISOFinalMessageType.isReversalMessage(lastOutIfx.getIfxType()) &&
                        lastOutIfx.getOrigDt().equals(ifx.getOrigDt()) &&
                        lastOutIfx.getDestBankId().equals(ifx.getDestBankId()) &&
                        lastOutIfx.getTrnType().equals(ifx.getTrnType())) {

                    if (Util.hasText(ifx.getNetworkRefId())) {
                        if (!lastOutIfx.getNetworkRefId().equals(ifx.getNetworkRefId())) {
                            isNeedQuery = true;
                        }
                    }

                    if (Util.hasText(ifx.getMyNetworkRefId())) {//honarmand
                        if (!Util.trimLeftZeros(lastOutIfx.getMyNetworkRefId()).equals(Util.trimLeftZeros(ifx.getMyNetworkRefId()))) {
                            isNeedQuery = true;
                        }
                    }

                    if (ifx.getReal_Amt()!= null && !ifx.getReal_Amt().equals(0L)){
                        if (!lastOutIfx.getReal_Amt().equals(ifx.getReal_Amt())) {
                            isNeedQuery = true;
                        }
                    }
                } else
                    isNeedQuery = true;
            } else {
                if (!ProcessContext.get().getMyInstitution().getBin().equals(ifx.getDestBankId())
                        && FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {

                    String bindingParam = TransactionService.getBindingString(ifx);

                    Long transaction = GlobalContext.getInstance().getBindTransaction(bindingParam);

                    if (transaction != null) {
                        flag = true;
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("trx", transaction);
                        lastIncomingTrx = (Transaction) GeneralDao.Instance.findUniqueObject("from Transaction t where t.id=:trx", params);
                        logger.debug("This isn't phisycal device but we finding first transaction: " + lastIncomingTrx
                                + " with Globalcontext Map...");

                        GlobalContext.getInstance().removeBindTransaction(bindingParam);
                    } else {
                        logger.debug("This isn't phisycal device and we don't find first transaction with Globalcontext Map...");
                        isNeedQuery = true;
                    }
                } else if (ifx.getFirstTrxId() == null) {
                    isNeedQuery = true;
                } else {
                    flag = true;
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("trx", ifx.getFirstTrxId());
                    lastIncomingTrx = (Transaction) GeneralDao.Instance.findUniqueObject("from Transaction t where t.id=:trx", params);
                    logger.debug("This isn't phisycal device but we finding first transaction: " + lastIncomingTrx + " whithout query...");
                }
            }

        } else {
            if (ifx.getFirstTrxId() == null)
                isNeedQuery = true;
            else {
                flag = true;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("trx", ifx.getFirstTrxId());
                lastIncomingTrx = (Transaction) GeneralDao.Instance.findUniqueObject("from Transaction t where t.id=:trx", params);
                logger.debug("We aren't acquire but we finding first transaction:" + lastIncomingTrx + "whithout query...");
            }
        }

        if(flag && ifx.getFirstTrxId() == null){
            List<IfxDirection> direction = new ArrayList<IfxDirection>();
            direction.add(IfxDirection.OUTGOING);
            direction.add(IfxDirection.SELF_GENERATED);
            String query = "from Ifx i where i.transaction.id = :trx "
                    + " and i.ifxDirection in (:outgoing) ";

            Map<String, Object> params = new HashMap<String, Object>();
            if (ifx.getFirstTrxId() != null) {
                params.put("trx", ifx.getFirstTrxId());
            } else {
                params.put("trx", lastIncomingTrx.getId());

            }
            params.put("outgoing", direction);

            Ifx lastOutIfx = (Ifx) GeneralDao.Instance.findUniqueObject(query, params);

            if (lastOutIfx != null &&
					/*!ShetabFinalMessageType.isReversalMessage(lastOutIfx.getIfxType()) &&*/
                    lastOutIfx.getOrigDt().equals(ifx.getOrigDt()) &&
                    lastOutIfx.getDestBankId().equals(ifx.getDestBankId()) &&
                    lastOutIfx.getTrnType().equals(ifx.getTrnType())) {

                if (Util.hasText(ifx.getNetworkRefId())) {
                    if (!lastOutIfx.getNetworkRefId().equals(ifx.getNetworkRefId())) {
                        isNeedQuery = true;
                        logger.debug("Oops! we suppose to find transaction :"+lastIncomingTrx+ "whithout query. but lastOutIfx.networkRefId is different from ifx.networkRefId!");
                    }
                }

                if (Util.hasText(ifx.getMyNetworkRefId())) {//honarmand
                    if (!Util.trimLeftZeros(lastOutIfx.getMyNetworkRefId()).equals(Util.trimLeftZeros(ifx.getMyNetworkRefId()))) {
                        isNeedQuery = true;
                        logger.debug("Oops! we suppose to find transaction :"+lastIncomingTrx+ "whithout query. but lastOutIfx.networkRefId is different from ifx.networkRefId!");
                    }
                }

                if (ifx.getAuth_Amt()!= null && !ifx.getAuth_Amt().equals(0L)){
                    if (!lastOutIfx.getAuth_Amt().equals(ifx.getAuth_Amt())) {
                        isNeedQuery = true;
                        logger.debug("Oops! we suppose to find transaction :"+lastIncomingTrx+ "whithout query. but lastOutIfx.Auth_Amt is different from ifx.Auth_Amt!");
                    }
                }

                if (lastIncomingTrx != null && Util.hasText(ifx.getTerminalId())) {
                    if (!lastOutIfx.getTerminalId().equals(ifx.getTerminalId())) {
                        isNeedQuery = true;
                        logger.debug("Oops! we are acquier with phisycal device, we suppose to find transaction :"+lastIncomingTrx+ "whithout query. but lastOutIfx.terminalId is different from ifx.terminalId!");
                    }
                }

            } else {
                isNeedQuery = true;
                logger.debug("Oops! we suppose to find transaction :"+lastIncomingTrx+ "whithout query. but lastOutIfx is null or OrigDt or DestBankId or TrnType is different in lastOutIfx and ifx!");
            }
        }


        if (!isNeedQuery) {
            logger.debug("incoming RS trx is response of last trx rq: " + lastIncomingTrx.getId());
            result.add(lastIncomingTrx);
            return result;

        } else {
            logger.debug("incoming RS trx is NOT response of last trx rq, Query is needed!");
            return findFirstTransactions(ifx);
        }
    }

    public static String getBindingString(Ifx ifx) {
        String bindingParam =
                DateTime.now() + "|" +
                        ifx.getOrigDt().toString() + "|" +
                        ifx.getDestBankId() + "|" +
                        ifx.getTrnType().toString() + "|" +
                        Util.longValueOf(ifx.getMy_TrnSeqCntr()) + "|" /*+
        ifx.getTerminalId() + "|" +
        ifx.getNetworkRefId() + "|"*/
                ;

        if (ifx.getAuth_Amt() != null && !ifx.getAuth_Amt().equals(0L))
            bindingParam += ifx.getAuth_Amt();

        return bindingParam;
    }


    public static List<Transaction> findFirstTransactions(Ifx ifx) {
        String seqid = ifx.getMy_TrnSeqCntr();
        DateTime origdt = ifx.getOrigDt();
        String bankid = ifx.getBankId();
        String destbankid = ifx.getDestBankId();
        // BindChange
        IfxDirection ifxDirection = IfxDirection.OUTGOING;
        String terminalID = ifx.getTerminalId();
        String networkRefId = ifx.getNetworkRefId();
        String myNetworkRefId = ifx.getMyNetworkRefId();

        logger.debug("Query-Searching for trnSeqNmbr:" + seqid + ", bankid:" + bankid + ", origdt:" + origdt
                + ", destbankid:" + destbankid + ", networkRefid:" + networkRefId + ", myNetworkRefId:" + myNetworkRefId + ", TrnType: " + ifx.getTrnType()
                + ", TerminalId: " + terminalID);

        String queryString = "select i.transaction from Ifx i "
                + " where "
                + " i.request = true "
                + " and i.trnType= :TrnType "
                + " and (i.ifxDirection = :IfxDirection or i.ifxDirection = :SelfGenerated) "
                + " and i.networkTrnInfo.BankId = :BankId "
                + " and i.networkTrnInfo.DestBankId = :FwdBankId "
                + " and i.networkTrnInfo.OrigDt.dayDate = :OrigDate "
                + " and i.networkTrnInfo.OrigDt.dayTime = :OrigTime ";


        if (ISOFinalMessageType.isReversalRsMessage(ifx.getIfxType())) {
//			queryString += " and i.ifxType in "+IfxType.getRevRqOrdinalsCollectionString()+" ";
            queryString += " and i.ifxType in "+IfxType.strRevRqOrdinals+" ";
            if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())) {
                queryString += " and i.networkTrnInfo.Src_TrnSeqCntr = :TrnSeqCntr ";
            } else {
                queryString += " and i.networkTrnInfo.My_TrnSeqCntr = :TrnSeqCntr ";
            }
        } else {
            queryString += " and i.networkTrnInfo.My_TrnSeqCntr = :TrnSeqCntr ";
        }

        Map<String, Object> params = new HashMap<String, Object>();

        if (ifx.getAuth_Amt()!= null && !ifx.getAuth_Amt().equals(0L)){
            queryString += " and i.eMVRqData.Auth_Amt = :amount ";
            params.put("amount", ifx.getAuth_Amt());
        }

        //TEMP start
        logger.debug("seqid:" + seqid + ", bankid:" + bankid + ", origdt:" + origdt
                + ", destbankid:" + destbankid + ", networkRefid:" + networkRefId + ", myNetworkRefId:" + myNetworkRefId + ", TrnType: " + ifx.getTrnType()
                + ", TerminalId: " + terminalID);
        //TEMP end


        params.put("TrnSeqCntr", seqid);
        params.put("TrnType", ifx.getTrnType());
        params.put("BankId", bankid);
        params.put("FwdBankId", destbankid);
        params.put("OrigDate", origdt.getDayDate());
        params.put("OrigTime", origdt.getDayTime());
        params.put("IfxDirection", ifxDirection);
        params.put("SelfGenerated", IfxDirection.SELF_GENERATED);

        if (Util.hasText(terminalID)) {
            queryString += " and i.networkTrnInfo.TerminalId = :terminalId";
            params.put("terminalId", terminalID);
        }

        if (Util.hasText(networkRefId)) {
            queryString += " and i.networkTrnInfo.NetworkRefId = :NetworkRefId";
            params.put("NetworkRefId", networkRefId);
        }

        if (Util.hasText(myNetworkRefId)) {
            queryString += " and i.networkTrnInfo.MyNetworkRefId = :MyNetworkRefId";
            params.put("MyNetworkRefId", StringFormat.formatNew(12, StringFormat.JUST_RIGHT, Util.trimLeftZeros(myNetworkRefId), '0'));
        }

        return GeneralDao.Instance.find(queryString, params);
    }

    public static String getPreviousReversalRsCode(Transaction transaction, boolean isReturn) {
        Map<String, Object> params = new HashMap<String, Object>();
        Ifx ifx = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
        String queryString = "select i.transaction from Ifx as i "
                + "where"
                + " i.networkTrnInfo.NetworkRefId= :NetworkRefId "
                + "and i.originalDataElements.TrnSeqCounter = :TrnSecCntr "
                + "and i.originalDataElements.BankId = :BankId "
                + "and i.originalDataElements.OrigDt = :OrigDt "
                + "and i.originalDataElements.TerminalId = :terminalId "
                + "and i.originalDataElements.AppPAN = :appPAN ";


        if (!isReturn) {
            queryString += "and i.trnType= :TrnType ";
            params.put("TrnType", ifx.getTrnType());
        }

        queryString +=	" and i.request = false "
                + " order by i.receivedDtLong desc";

        params.put("NetworkRefId", ifx.getNetworkRefId());
        params.put("TrnSecCntr", ifx.getSrc_TrnSeqCntr());
        params.put("BankId", ifx.getBankId());
        params.put("OrigDt", ifx.getOrigDt());
        params.put("appPAN", ifx.getAppPAN());
        params.put("terminalId", ifx.getTerminalId());

        Transaction trnx = (Transaction) GeneralDao.Instance.findUniqueObject(queryString, params);
        String rsCode = "";
        try{
            if(trnx.getOutputMessage() != null ){
                rsCode = (String) trnx.getOutgoingIfx().getRsCode();
            }else{
                logger.info("Previous Reversal was Self Generated!");
                rsCode = (String) trnx.getIncomingIfx().getRsCode();
            }
        } catch (Exception e) {
            logger.info(e, e);
            rsCode = "";
        }
        return rsCode;
    }

    public static boolean isReferenceTrxSettled(Transaction referenceTransaction){
        if (referenceTransaction.getSourceSettleInfo() != null){
            if (AccountingState.NO_NEED_TO_BE_COUNTED.equals(referenceTransaction.getSourceSettleInfo().getAccountingState())){
                if ( referenceTransaction.getThirdPartySettleInfo()!= null
                        && !SettledState.NOT_SETTLED.equals(referenceTransaction.getThirdPartySettleInfo().getSettledState())){
					/*throw new InvalidBusinessDateException("Originator Transaction already settled.(refTrx: "
							+ referenceTransaction.getId() + ")");*/
                    return true;
                }

            } else {
                if (!SettledState.NOT_SETTLED.equals(referenceTransaction.getSourceSettleInfo().getSettledState())) {
					/*throw new InvalidBusinessDateException("Originator Transaction already settled.(refTrx: "
							+ referenceTransaction.getId() + ")");*/
                    return true;
                }
            }
        }
        return false;
    }

    public static Transaction findTerminalLastTransaction(Terminal terminal, Ifx inIfx) {
        Transaction lastTransaction = terminal.getLastTransaction();
        DateTime failureOrigDt = inIfx.getSafeOriginalDataElements().getOrigDt();

        failureOrigDt.setDayTime(new DayTime(failureOrigDt.getDayTime().getHour(), failureOrigDt.getDayTime().getMinute(), 0));

        if (lastTransaction != null && lastTransaction.getFirstTransaction() != null) {
            Transaction firstTrx = lastTransaction.getFirstTransaction();
            Ifx ifx = firstTrx.getIncomingIfx()/*getInputMessage().getIfx()*/;

            if (ifx != null && ifx.getOrigDt() != null && ifx.getOrigDt().equals(failureOrigDt) ) {
                if (ISOFinalMessageType.isReturnRq(ifx.getIfxType()) ||
                        (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())&& !ISOFinalMessageType.isTransferCheckAccountMessage(ifx.getIfxType())) ||
                        (ISOFinalMessageType.isTransferCardToAccountMessage(ifx.getIfxType())&& !ISOFinalMessageType.isTransferToacChechAccountMessage(ifx.getIfxType()))) {

                    if (ifx.getSrc_TrnSeqCntr().equals(inIfx.getSrc_TrnSeqCntr()))
                        return lastTransaction;

                } else {
                    if (ifx.getNetworkRefId().equals(inIfx.getNetworkRefId()))
                        return lastTransaction;

                }
            }
        }

        if (terminal.getLastIncomingTransaction()!= null &&
                terminal.getLastIncomingTransaction().getInputMessage() != null &&
                terminal.getLastIncomingTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/ != null &&
                terminal.getLastIncomingTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getOrigDt() != null &&
                terminal.getLastIncomingTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getOrigDt().equals(failureOrigDt)) {

            Ifx lastInIfx = terminal.getLastIncomingTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
            if (ISOFinalMessageType.isReturnRq(lastInIfx.getIfxType()) ||
                    (ISOFinalMessageType.isTransferMessage(lastInIfx.getIfxType())&& !ISOFinalMessageType.isTransferCheckAccountMessage(lastInIfx.getIfxType()))) {
                if (lastInIfx.getSrc_TrnSeqCntr().equals(inIfx.getSrc_TrnSeqCntr())) {
                    logger.debug("received trx is POS_FALURE of LAST_INCOMING_TRX( " + terminal.getLastIncomingTransactionId() + ")");
                    return terminal.getLastIncomingTransaction();
                } else {
                    logger.debug("received trx is not POS_FALURE of LAST_INCOMING_TRX! return NULL");
                    return null;

                }

            } else {
                if (lastInIfx.getNetworkRefId().equals(inIfx.getNetworkRefId())) {
                    logger.debug("received trx is POS_FALURE of LAST_INCOMING_TRX( " + terminal.getLastIncomingTransactionId() + ")");
                    return terminal.getLastIncomingTransaction();
                } else {
                    logger.debug("received trx is not POS_FALURE of LAST_INCOMING_TRX! return NULL");
                    return null;
                }
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = "select i from Ifx as i "
                + " where i.networkTrnInfo.BankId = " + inIfx.getBankId()
//            + " and i.endPointTerminal = " + inIfx.getEndPointTerminal()
                + " and i.networkTrnInfo.TerminalId = '" + inIfx.getTerminalId() +"'"
                + " and i.ifxDirection = " + IfxDirection.INCOMING.getType()
                + " and i.request = true "
                + " and (i.networkTrnInfo.NetworkRefId = '" + inIfx.getSafeOriginalDataElements().getNetworkTrnInfo() + "'"
                + " or i.networkTrnInfo.Src_TrnSeqCntr = '" + inIfx.getSafeOriginalDataElements().getTrnSeqCounter() + "')"
                ;

        if (failureOrigDt != null) {
//            queryString += " and i.receivedDtLong > " + lastIncomeTrx.getDateTimeLong();
            queryString += " and i.networkTrnInfo.OrigDt.dayDate = " + failureOrigDt.getDayDate().getDate();
            queryString += " and i.networkTrnInfo.OrigDt.dayTime = " + failureOrigDt.getDayTime().getDayTime();
        }

        queryString += " order by i.receivedDtLong desc";

//		params.put("lstRcvDt", lastIncomeTrx.getDateTimeLong());
//		params.put("BankId", inIfx.getBankId());
//		params.put("terminalId", inIfx.getTerminalId());
//		params.put("IfxDirection", IfxDirection.INCOMING);
//		params.put("NetworkRefId", inIfx.getSafeOriginalDataElements().getNetworkTrnInfo());
        Ifx refIfx = (Ifx) GeneralDao.Instance.findObject(queryString, params);
        Transaction result = refIfx != null ? refIfx.getTransaction() : null;
        if (result != null)
            logger.warn("received trx is POS_FALURE of " + result.getId());
        else
            logger.warn("received trx is POS_FALURE of " + null);
        return result;
    }

    public static List<Long> getTransactionFromSettlementRecord(DateTime untilTime, List<Long> termForQuery) {

        Map<String, Object> parameters = new HashMap<String, Object>();

        String query = "select sr.transactionId from " + SettlementRecord.class.getName() + " sr " +
                " where sr.terminalId in (:list) ";
        parameters.put("list", termForQuery);

        if (untilTime != null) {
            query +=  " and sr.receivedDt <= :toDate ";
            parameters.put("toDate", untilTime.getDateTimeLong());
        }
        return GeneralDao.Instance.find(query, parameters, 0, 1000);
    }

    public static boolean isMessageExpired(Ifx ifx) {
        Transaction transaction = ifx.getTransaction();
        Transaction firstTransaction = transaction.getFirstTransaction();
        Ifx firstIfx = firstTransaction.getIncomingIfx();

        Terminal endPointTerminal = null;

        if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(ifx.getIfxType()) &&
                IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(firstIfx.getIfxType())) {
            endPointTerminal = firstTransaction.getFirstTransaction().getIncomingIfxOrMessageEndpoint();

        } else {
            endPointTerminal = firstTransaction.getIncomingIfxOrMessageEndpoint();

        }

        if (endPointTerminal != null &&
                endPointTerminal.getLastIncomingTransaction() != null) {
            Transaction lastInTrx = endPointTerminal.getLastIncomingTransaction();

            if (firstTransaction == lastInTrx) {
                logger.debug("received RS message is response of terminal_last incoming trx, OK ");
                return false;
            }

            if (lastInTrx.getIncomingIfx().getOrigDt().after(ifx.getOrigDt())) {
                logger.debug("received RS message is NOT response of terminal_last incoming trx, NOK ");
                return true;

            } else {
                logger.debug("terminal(" + endPointTerminal.getId() + ") last incoming trx: " + lastInTrx.getId() + ", incoming RS trx: " + transaction.getId() + " , OK ");
                return false;
            }
        }

        return false;

    }


    public static Transaction getPrepareThirdPartyPayment(Ifx ifx){
        Map<String , Object> params = new HashMap<String, Object>();
        String st = "select i.transaction from Ifx as i "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9) "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.ifxType = :IfxType"
                + " and i.trnType = :TrnType "

                + " and i.receivedDtLong >= :tenMinBefore "
                + " and i.receivedDtLong <= :now "
                + " and i.request = true ";

//		if (ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())) {
        st += " and i.endPointTerminalCode = :terminalId ";

        params.put("terminalId", ifx.getEndPointTerminal().getCode());

	/*	} else {
			st += " and i.networkTrnInfo.TerminalId = :terminalId ";

			params.put("terminalId", ifx.getTerminalId());
		}*/

        params.put("IfxDirection", IfxDirection.INCOMING);

        DateTime tenMinBefore = DateTime.toDateTime(DateTime.now().getTime() - 10 * DateTime.ONE_MINUTE_MILLIS);
        params.put("tenMinBefore", tenMinBefore.getDateTimeLong());
        params.put("now",  + DateTime.now().getDateTimeLong());


        params.put("TrnType", TrnType.PREPARE_THIRD_PARTY_PAYMENT);
        params.put("IfxType", IfxType.PREPARE_THIRD_PARTY_PURCHASE);

        st += " order by i.receivedDtLong desc";

        return (Transaction) GeneralDao.Instance.findObject(st, params);
    }

    public static List<Transaction> getReferenceOfSorushTransaction(Ifx ifx, boolean yesterday) {

        if (ifx.getSafeOriginalDataElements().getBankId() != null && ifx.getSafeOriginalDataElements().getBankId().equals(936450L)) {
            logger.info("sorush query: empty because it is sorush bin not bank bin" );
            return Collections.emptyList();
        }

        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = "select i.transaction from Ifx as i "
                + "where "
                + " i.ifxDirection = :IfxDirection "
                + " and i.request = false "
                + " and not(i.id = :msgId) "
                + " and i.dummycol in (0,1,2,3,4,5,6,7,8,9) "
                + " and i.ifxRsCode = '00' ";

        params.put("msgId", ifx.getId());

//        if (ifx.getSafeOriginalDataElements().getBankId() != null && ifx.getSafeOriginalDataElements().getBankId().equals(581672000L)) {
//            queryString += " and i.networkTrnInfo.BankId between :BankIdFrom and :BankIdTo ";
//            params.put("BankIdFrom", 581672000L);
//            params.put("BankIdTo", 581672999L);
//
//        } else {
//            queryString += " and i.networkTrnInfo.BankId = :BankId ";
//            params.put("BankId", ifx.getSafeOriginalDataElements().getBankId());
//
//        }

        params.put("IfxDirection", IfxDirection.INCOMING);

        if (Util.hasText(ifx.getAppPAN())){
            queryString += " and i.ifxEncAppPAN = :appPAN ";
            params.put("appPAN", ifx.getAppPAN());
        }

        if(!yesterday){
            if (ifx.getSafeOriginalDataElements().getOrigDt().getDayTime().equals(new DayTime(0, 0, 0))) {
                queryString += " and i.ifxOrigDt.dayDate = :OrigDt ";
                params.put("OrigDt", ifx.getSafeOriginalDataElements().getOrigDt().getDayDate());

            } else {
                queryString += " and i.ifxOrigDt = :OrigDt ";
                params.put("OrigDt", ifx.getSafeOriginalDataElements().getOrigDt());
            }
        } else {
            if (ifx.getSafeOriginalDataElements().getOrigDt().getDayTime().equals(new DayTime(0, 0, 0))) {
                queryString += " and i.ifxOrigDt.dayDate = :OrigDt ";
                params.put("OrigDt", ifx.getSafeOriginalDataElements().getOrigDt().getDayDate().previousDay());

            } else {
                DateTime origDt = ifx.getSafeOriginalDataElements().getOrigDt().clone();
                origDt.decrease(Integer.parseInt("" + (DateTime.ONE_DAY_MILLIS / 1000)));
                queryString += " and i.ifxOrigDt = :OrigDt ";
                params.put("OrigDt", origDt);
            }
        }

        if(ifx.getSafeOriginalDataElements().getOrigDt().before(DateTime.now()) && ifx.getSafeOriginalDataElements().getOrigDt().after(DateTime.beforeNow(Math.abs(3)))){
            queryString +=  " and i.receivedDtLong between :fromDate and :toDate ";
            params.put("fromDate", DateTime.beforeNow(Math.abs(3)).getDateTimeLong());
            params.put("toDate", DateTime.now().getDateTimeLong());
        }

        queryString += " and i.ifxSrcTrnSeqCntr = :TrnSecCntr ";
        params.put("TrnSecCntr", ifx.getSafeOriginalDataElements().getTrnSeqCounter());

        queryString += " order by i.receivedDtLong desc";

        List<Transaction> list = null;
        try {

            if(ifx != null &&
                    ifx.getSafeOriginalDataElements() != null &&
                    ifx.getSafeOriginalDataElements().getRefSorushiTransaction() != null &&
                    ifx.getSafeOriginalDataElements().getRefSorushiTransaction().getId() != null){
                list = new ArrayList<Transaction>();
                list.add(ifx.getSafeOriginalDataElements().getRefSorushiTransaction());
            }

        } catch (Exception e) {
            // TODO: handle exception
            logger.info("Problem in RefSorushiTransaction()");
        }




        try {
            if(list == null || list.size() == 0 ){
                logger.debug("query new in baze in ref sorush....");
                list = GeneralDao.Instance.find(queryString, params);
            }

        } catch (Exception e) {
            //agha log nandazid
        }

        if((list == null || list.size() == 0 ) && !yesterday){
            logger.debug("sorush not found with origDt, use yesterday!");
            list = getReferenceOfSorushTransaction(ifx, !yesterday);
        }


        return list;
    }


    public static List<Transaction> getReferenceOfSorushTransactionOld(Ifx ifx) {
        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = "select i.transaction from Ifx as i "
                + "where "
                + " i.ifxDirection = :IfxDirection "
                + " and i.request = false "
                + " and not(i.id = :msgId) "
                + " and i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
                + " and i.eMVRsData.RsCode = '00' "
                ;

        params.put("msgId", ifx.getId());

//        if (ifx.getSafeOriginalDataElements().getBankId() != null && ifx.getSafeOriginalDataElements().getBankId().equals(581672000L)) {
//            queryString += " and i.networkTrnInfo.BankId between :BankIdFrom and :BankIdTo ";
//            params.put("BankIdFrom", 581672000L);
//            params.put("BankIdTo", 581672999L);
//
//        } else {
//            queryString += " and i.networkTrnInfo.BankId = :BankId ";
//            params.put("BankId", ifx.getSafeOriginalDataElements().getBankId());
//
//        }

        params.put("IfxDirection", IfxDirection.INCOMING);

        if (Util.hasText(ifx.getAppPAN())){
            queryString += " and i.eMVRqData.CardAcctId.AppPAN = :appPAN ";
            params.put("appPAN", ifx.getAppPAN());
        }

        if (ifx.getSafeOriginalDataElements().getOrigDt().getDayTime().equals(new DayTime(0, 0, 0))) {
            queryString += " and i.networkTrnInfo.OrigDt.dayDate = :OrigDt ";
            params.put("OrigDt", ifx.getSafeOriginalDataElements().getOrigDt().getDayDate());

        } else {
            queryString += " and i.networkTrnInfo.OrigDt = :OrigDt ";
            params.put("OrigDt", ifx.getSafeOriginalDataElements().getOrigDt());
        }

        if(ifx.getSafeOriginalDataElements().getOrigDt().after(DateTime.beforeNow(Math.abs(3)))){
            queryString +=  " and i.receivedDtLong between :fromDate and :toDate ";
            params.put("fromDate", DateTime.beforeNow(Math.abs(3)).getDateTimeLong());
            params.put("toDate", DateTime.now().getDateTimeLong());
        }

        queryString += " and i.networkTrnInfo.Src_TrnSeqCntr = :TrnSecCntr ";
        params.put("TrnSecCntr", ifx.getSafeOriginalDataElements().getTrnSeqCounter());

        queryString += " order by i.receivedDtLong desc";


        List<Transaction> list = GeneralDao.Instance.find(queryString, params);

        long now = DateTime.now().getDateTimeLong();
        if( DateTime.getDiffLong(now , ifx.getTransaction().getBeginDateTime().getDateTimeLong()) > 20L){
            logger.info("sorush query not ok." );
            //return Collections.emptyList();
        } else
            logger.info("sorush query ok." );

        return list;
    }

    //Mirkamali(Task139)
    public static List<Ifx> getATMPowerFailure(Long receiveDtFrom, Long receiveDtTo, String trnSeqCntr, String termId){
        String query = "from Ifx i"
                + " where "
                + " i.ifxDirection = :ifxDirection"
                + " and i.ifxType = :ifxType"
                + " and i.trnType = :trnType"
                + " and i.receivedDtLong between :from and :to"
                + " and i.networkTrnInfo.TerminalId = :termId "
                + " and i.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr"
                + " and i.dummycol in (0,1,2,3,4,5,6,7,8,9) and 9=9"
                ;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ifxDirection", IfxDirection.INCOMING);
        params.put("ifxType", IfxType.POWER_FAILURE);
        params.put("trnType", TrnType.UNKNOWN);
        params.put("from", receiveDtFrom);
        params.put("to", receiveDtTo);
        params.put("termId", termId);
        params.put("trnSeqCntr", trnSeqCntr);

        return (List<Ifx>)GeneralDao.Instance.find(query, params);

    }

    //Mirkamali(Task166)
    public static void matchTrnSeqCntr(Ifx ifx) {
        if(ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())){
            Transaction transaction = ifx.getTransaction();

            Transaction refTrx =  transaction.getReferenceTransaction();
            if(refTrx != null && refTrx.getOutgoingIfx() != null) {
                String trnSeqCntr = refTrx.getOutgoingIfx().getMy_TrnSeqCntr();
                if(transaction.getIncomingIfx() != null)
                    transaction.getIncomingIfx().setMy_TrnSeqCntr(trnSeqCntr);
                if(transaction.getOutgoingIfx() != null)
                    transaction.getOutgoingIfx().setMy_TrnSeqCntr(trnSeqCntr);
            }

            Transaction firstTrx = transaction.getFirstTransaction();
            if(firstTrx != null && firstTrx.getOutgoingIfx() != null) {
                if(transaction.getIncomingIfx() != null)
                    transaction.getIncomingIfx().setSrc_TrnSeqCntr(firstTrx.getOutgoingIfx().getSrc_TrnSeqCntr());
                if(transaction.getOutgoingIfx() != null)
                    transaction.getOutgoingIfx().setSrc_TrnSeqCntr(firstTrx.getOutgoingIfx().getSrc_TrnSeqCntr());
            }
        }
    }

    //Mirkamali(Task179): Currency ATM
    public static Long computeAllFees(ATMTerminal atm, Ifx ifx) {

        if (atm.getOwnOrParentFeeProfile() != null && ifx != null) {
            List<FeeInfo> feeInfos = FeeService.calculateFees(atm.getOwnOrParentFeeProfile(), ifx);
            Long totalFeeAmt = 0L;
            if (feeInfos == null || feeInfos.size() <= 0) {
                logger.info("FEEINFO is null!");
                return totalFeeAmt;
            }

            logger.info("FEEINFO.size: " + feeInfos.size());

            for (FeeInfo feeInfo : feeInfos) {
                Fee fee = new Fee();
                fee.setTransaction(ifx.getTransaction());
                fee.setFeeProfile(atm.getOwnOrParentFeeProfile());
                fee.setClearingProfile(atm.getOwnOrParentClearingProfile());
                fee.setAmount(feeInfo.getAmount());
                fee.setInsertionTime(DateTime.now());

                FinancialEntity debited = feeInfo.getEntityToBeDebited();
                if (debited != null)
                    logger.info("FEEINFO, DEBITED: " + debited.getId());

                FinancialEntity credited = feeInfo.getEntityToBeCredited();
                if (credited != null)
                    logger.info("FEEINFO, CREDITED: " + credited.getId());

                totalFeeAmt += feeInfo.getAmount();

                fee.setEntityToBeDebited(feeInfo.getEntityToBeDebited());
                fee.setEntityToBeCredited(feeInfo.getEntityToBeCredited());


                fee.setFeeItem(feeInfo.getFeeItem());
                GeneralDao.Instance.saveOrUpdate(fee);

            }
            return totalFeeAmt;
        }
        return 0L;
    }
}
