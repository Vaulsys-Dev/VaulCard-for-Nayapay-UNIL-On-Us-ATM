package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementRecordType;
import vaulsys.clearing.consts.ClearingProcessType;
import vaulsys.clearing.base.SettlementRecord;
import vaulsys.customer.Currency;
import vaulsys.entity.impl.Institution;
import vaulsys.lottery.Lottery;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.lottery.LotteryService;
import vaulsys.lottery.consts.LotteryState;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.BERTLV;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.LotteryData;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public abstract class MessageProcessor{

    private Logger logger = Logger.getLogger(this.getClass());

    // Used in MessageProcessHandler
    abstract public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws Exception;

    // Used in AuthorizationComponent
    abstract public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception;

    public Message postProcess(Transaction transaction, Message incomingMessage, Message outgoingMessage, Channel channel)
            throws Exception {

        //FIXME: needed in if
        //Lottery lottery = null;

        Ifx incomingIfx = incomingMessage.getIfx();
        Ifx outgoingIfx = outgoingMessage.getIfx();
        
        
        //Mirkamali(Task179): Currency ATM
        if(IfxType.PREPARE_WITHDRAWAL.equals(incomingIfx.getIfxType())){
        	try{
        		incomingIfx.setAuth_CurRate(((ATMTerminal)incomingMessage.getEndPointTerminal()).getCurrency().getCurRate());
        		incomingIfx.setAuth_Amt(incomingIfx.getAuth_Amt() * Long.valueOf(incomingIfx.getAuth_CurRate()));
        		Long totalFeeAmt = TransactionService.computeAllFees((ATMTerminal)incomingMessage.getEndPointTerminal(), incomingIfx);
        		incomingIfx.setTotalFeeAmt(totalFeeAmt);
        		incomingIfx.setAuth_Amt(incomingIfx.getAuth_Amt() + totalFeeAmt);
    		  
        	} catch(Exception e){
        		logger.warn("Exception in computing Fee for currency withdrawal" + e);
        	}
        }
        //FIXME: needed in the if
        //LotteryData lotteryData = outgoingIfx.getLotteryData();
        if (ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType())) {
            try {
                if (!ISOFinalMessageType.isReversalOrRepeatMessage(outgoingIfx.getIfxType())) {
                    if ((ISOResponseCodes.isSuccess(outgoingIfx.getRsCode()) || (ISOResponseCodes.isSuccess(incomingIfx.getRsCode()) && IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(incomingIfx.getIfxType()))) &&
                            !IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(incomingIfx.getIfxType())&&
                            !IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(incomingIfx.getIfxType())) {// this part has been added to avoid inserting transferCheckAccount to settlementRecord

                        Message inputFirstMessage = transaction.getFirstTransaction().getInputMessage();
                        Ifx incomingFirstIfx = inputFirstMessage.getIfx();
                        Ifx outgoingFirstIfx = transaction.getFirstTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;

                        /********* Clearing Process: start **********/
                        Terminal endPointTerminal = inputFirstMessage.getEndPointTerminal();
                        if (endPointTerminal != null && /*!TerminalType.SWITCH.equals(endPointTerminal.getTerminalType())*/  EndPointType.isSwitchTerminal(channel.getEndPointType())
                                && transaction.getFirstTransaction().getReferenceTransaction() != null && transaction.getFirstTransaction().getReferenceTransaction().getIncomingIfx() != null )//for transfer to add to stlRecord
                            endPointTerminal = transaction.getFirstTransaction().getReferenceTransaction().getIncomingIfx().getEndPointTerminal();
                        try {
                            ClearingProfile clrProf = null;

                            if(endPointTerminal != null && endPointTerminal.getOwnOrParentClearingProfileId() != null){
                                clrProf = ProcessContext.get().getClearingProfile(endPointTerminal.getOwnOrParentClearingProfileId());
                            }

                            if( clrProf != null &&
                                    clrProf.getProcessType() != null &&
                                    ClearingProcessType.ONLINE.equals(clrProf.getProcessType())) {
                                SettlementRecord record = SettlementRecord.getInstance(transaction, IfxType.TRANSFER_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType()) || IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(incomingIfx.getIfxType())?incomingIfx:outgoingIfx, clrProf,
										/*outgoingIfx.getIfxType(),*/ endPointTerminal, 
										/*inputFirstMessage.getEndPointTerminal().getOwnOrParentClearingProfile(),*/
                                        transaction.getBeginDateTime().getDateTimeLong());
                                if(record != null) {
                                    if( ( incomingFirstIfx.getThirdPartyTerminalCode() != null && incomingFirstIfx.getThirdPartyTerminalCode() > 0) || ( outgoingFirstIfx.getThirdPartyTerminalCode() != null && outgoingFirstIfx.getThirdPartyTerminalCode() > 0) ){
                                        record.setSettlementRecordType(SettlementRecordType.THIRDPARTHY);
                                    }
                                    logger.info("trx: " + transaction.getId() + " is added to settlement record!");
                                    GeneralDao.Instance.saveOrUpdate(record);
                                } else
                                    logger.info("trx: " + transaction.getId() + " isnt added to settlement record!");

                            }
                        } catch(Exception e) {
                            logger.info("No Settlement Record adde to message: " + incomingMessage.getId());
                        }
                        /********* Clearing Process: end **********/

                        /*******************/
                        //FIXME: define lottery here
                        Lottery lottery = null;
                        try {
//							LotteryAssignmentPolicy lotteryPolicy = endPointTerminal.getOwnOrParentLotteryPolicy();
                            LotteryAssignmentPolicy lotteryPolicy = ProcessContext.get().getLotteryAssignmentPolicy(endPointTerminal.getOwnOrParentLotteryPolicyId());
                            if (lotteryPolicy == null) {
//								logger.info("No Lottery Policy Assigned to terminal: " + endPointTerminal);
                                return outgoingMessage;
                            }
                            lottery = lotteryPolicy.getLottery(incomingFirstIfx);

                        } catch (Exception e) {
                            logger.info("No Lottery Assigned to message: " + inputFirstMessage.getId());
                            logger.info(e);
                            return outgoingMessage;
                        }

                        LotteryState oldState = lottery.getState();
                        lottery.setState(LotteryState.ASSIGNED);

                        outgoingIfx.setLottery(lottery);
                        outgoingIfx.setLotteryStatePrv(oldState);
                        outgoingIfx.setLotteryStateNxt(lottery.getState());
                        outgoingIfx.getLottery().setLifeCycle(transaction.getLifeCycle());

//						outgoingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setLifeCycle(transaction.getLifeCycle());

                        outgoingMessage.getEndPointTerminal().getOwnOrParentLotteryPolicy().update(outgoingIfx);

                        //FIXME: define variable LotteryData here
                        LotteryData lotteryData = outgoingIfx.getLotteryData();
                        incomingIfx.setLotteryData(lotteryData);
                        incomingFirstIfx.setLotteryData(lotteryData);
                        outgoingFirstIfx.setLotteryData(lotteryData);
                        GeneralDao.Instance.saveOrUpdate(incomingIfx);

                        /*******************/
                        GeneralDao.Instance.saveOrUpdate(outgoingIfx.getLottery());
                        GeneralDao.Instance.saveOrUpdate(outgoingIfx);
                        GeneralDao.Instance.saveOrUpdate(incomingFirstIfx);
                        GeneralDao.Instance.saveOrUpdate(outgoingFirstIfx);
                        GeneralDao.Instance.saveOrUpdate(incomingIfx);
                        /*******************/

                    } else {
                        LotteryService.unlockLottery(outgoingIfx, transaction);
                    }
                }
            } catch (Exception e) {
                logger.info("No Lottery Assigned to message: " + incomingMessage.getId());
                return outgoingMessage;
            }
        }

        return outgoingMessage;
    }

    protected void setMessageFlag(Message outgoingMessage, Boolean isRequest, Boolean needResponse, Boolean needToBeSent,
                                  Boolean needToBeInstantlyRev) {
        outgoingMessage.setRequest(isRequest);
        outgoingMessage.setNeedResponse(needResponse);
        outgoingMessage.setNeedToBeSent(needToBeSent);
        outgoingMessage.setNeedToBeInstantlyReversed(needToBeInstantlyRev);
    }

    // protected void finalizeSelfEndedTransaction(Transaction transaction)
    // throws Exception {
    //
    // ClearingInfo sourceClearingInfo = new
    // ClearingInfo(ClearingState.NOT_CLEARED, DateTime.now());
    // ClearingInfo destClearingInfo = new
    // ClearingInfo(ClearingState.NOT_CLEARED, DateTime.now());
    // transaction.setSourceClearingInfo(sourceClearingInfo);
    // transaction.setDestinationClearingInfo(destClearingInfo);
    // getGeneralDao().saveOrUpdate(transaction);
    // getTransactionService().copyFlagsToReferenceTransaction(transaction);
    // }

    protected void addNecessaryDataToIfx(Ifx ifx, Channel channel, Terminal endpointTerminal) {
        //m.rehman: no need to fix the value for Message Auth Code
        /*
        if (channel != null && !channel.getMacEnable())
            ifx.setMsgAuthCode("0102030405060708");
        else
            ifx.setMsgAuthCode(null);
        */
        Institution outputNetworkInst;
        Integer baseCurrency;
        Currency currency;
        Long convertedAmount;

        EndPointType endPointType = EndPointType.getEndPointType(endpointTerminal.getTerminalType());
        MonthDayDate daydate = null;

        if (EndPointType.isSwitchTerminal(endPointType)) {
            if(!(endpointTerminal instanceof SwitchTerminal))
                endpointTerminal = TerminalService.findTerminal(SwitchTerminal.class, endpointTerminal.getCode());

            ClearingDate wDate = ((SwitchTerminal)endpointTerminal).getOwner().getCurrentWorkingDay();
            daydate = wDate == null ? MonthDayDate.now() : new MonthDayDate(wDate.getDate());
        } else
        // if (EndPointType.POS_TERMINAL.equals(endPointType))
        {
            if (ifx.getSettleDt() == null)
                daydate = MonthDayDate.now();
            else
                daydate = ifx.getSettleDt();

            if (ifx.getTrnDt() == null)
                ifx.setTrnDt(DateTime.now());
        }

        //m.rehman: no need to set settlement date
        //ifx.setSettleDt(daydate);
        if (ifx.getPostedDt() == null)
            ifx.setPostedDt(daydate);

        //m.rehman: adding Country code if not available
        if (!Util.hasText(ifx.getMerchCountryCode())) {
            System.out.println("MYINS [" + ProcessContext.get().getMyInstitution().getCode() + "]");
            ifx.setMerchCountryCode(ProcessContext.get().getMyInstitution().getSafeCountryCode().toString());
        }

        outputNetworkInst = ProcessContext.get().getInstitution(channel.getInstitutionId());

        //m.rehman
        if ((ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) || ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType()))
                && ifx.getFwdBankId() == null) {
            ifx.setFwdBankId(outputNetworkInst.getBin().toString());
        }

        if ((ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) || ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType()))
                && ifx.getDestBankId() == null) {
            ifx.setDestBankId(outputNetworkInst.getBin().toString());
        }

        // if my institution base currency is different from out network institution currency
        // and if de 51 currency card holder billing is present and different from out network
        // institution currency then convert amount
        baseCurrency = GlobalContext.getInstance().getBaseCurrency().getCode();
        if (ifx.getSec_Amt() != null
                && !baseCurrency.equals(outputNetworkInst.getSafeCurrency().getCode())) {
            if (!ifx.getSec_Currency().equals(outputNetworkInst.getSafeCurrency().getCode())) {
                currency = ProcessContext.get().getCurrency(outputNetworkInst.getSafeCurrency().getCode());
                if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
                    convertedAmount = ifx.getSec_Amt() * currency.getExchangeRate();
                    ifx.setSec_Amt(convertedAmount);
                    ifx.setSec_Currency(currency.getCode());
                    ifx.setSec_CurRate(currency.getExchangeRate().toString());
                } else {
                    convertedAmount = ifx.getSec_Amt() / currency.getExchangeRate();
                    ifx.setSec_Amt(convertedAmount);
                    currency = ProcessContext.get().getCurrency(baseCurrency);
                    ifx.setSec_Currency(currency.getCode());
                    ifx.setSec_CurRate(currency.getExchangeRate().toString());
                }
            }
        }

        //if transaction is emv, make emv data for out network and set it back
        /*if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) && Util.hasText(ifx.getIccCardData())) {
            if (channel.getEmvTags() != null)
                ifx.setIccCardData(BERTLV.getTLVDataForARPC(ifx.getIccCardData(), channel.getEmvTags()));
        }*/ //Raza commenting EMV handling
    }

    protected Terminal getEndpointTerminal(Message outgoingMessage, EndPointType endPointType, boolean senderterminal, ProcessContext processContext) {
        if (senderterminal && processContext.getOriginatorTerminal() != null)
            return processContext.getOriginatorTerminal();

        Terminal endpointTerminal = TerminalService.findEndpointTerminal(outgoingMessage, outgoingMessage.getIfx(), endPointType);
        return endpointTerminal;
    }

}
