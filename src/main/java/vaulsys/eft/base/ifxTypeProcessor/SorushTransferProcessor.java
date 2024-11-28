package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.lottery.LotteryService;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.transaction.TransactionType;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;



public class SorushTransferProcessor extends MessageProcessor {

    transient Logger logger = Logger.getLogger(GeneralMessageProcessor.class);

    public static final SorushTransferProcessor Instance = new SorushTransferProcessor();
    private SorushTransferProcessor(){

    };


    @Override
    public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws Exception {

        Transaction refTrx = transaction.getFirstTransaction();
        Transaction refTrxSorush;
        Ifx incomingIfx = incomingMessage.getIfx();
        if(incomingMessage.getIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RQ)){

            refTrxSorush = TransactionService.getRefrenceTransactionSorush(transaction.getIncomingIfx());

            if(refTrxSorush != null && !( refTrxSorush.getLifeCycle().getIsFullyReveresed() == null || refTrxSorush.getLifeCycle().getIsFullyReveresed().equals(LifeCycleStatus.NOTHING)) ){

                Message outgoingMessage = new Message(MessageType.OUTGOING);

                outgoingMessage.setChannel(((InputChannel) incomingMessage.getChannel()).getOriginatorChannel());
                outgoingMessage.setEndPointTerminal(incomingMessage.getEndPointTerminal());

                Ifx outgoingIfx = MsgProcessor.processor(incomingIfx);
                outgoingIfx.setIfxType(IfxType.getResponseIfxType(outgoingIfx.getIfxType()));
                setMessageFlag(outgoingMessage, false, false, true, false);

                outgoingIfx.setRsCode(ISOResponseCodes.CARD_EXPIRED);

                outgoingMessage.setRequest(false);
                outgoingMessage.setNeedResponse(false);
                outgoingMessage.setNeedToBeSent(true);
                
                try {

                    if(incomingMessage.getTransaction().getFirstTransaction().getTransactionType().equals(TransactionType.SELF_GENERATED)){
                    	outgoingMessage.setNeedToBeSent(false);
                    	outgoingMessage.setNeedToBeInstantlyReversed(false);
                    }
                } catch (Exception e) {
                    logger.error("Exception in detecting sorush:" + e.toString());
                    logger.error(e, e);
                }
                outgoingMessage.setTransaction(transaction);
                transaction.addOutputMessage(outgoingMessage);

                GeneralDao.Instance.saveOrUpdate(outgoingIfx);
                GeneralDao.Instance.saveOrUpdate(outgoingMessage);
                GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
                transaction.setOutgoingIfx(outgoingIfx);
                GeneralDao.Instance.saveOrUpdate(transaction);

                outgoingMessage.setIfx(outgoingIfx);
                return outgoingMessage;
            }else if (refTrxSorush != null){
                if(transaction.getIncomingIfx().getOriginalDataElements() != null ){
                    transaction.getIncomingIfx().getOriginalDataElements().setOrigDt(refTrxSorush.getIncomingIfx().getOrigDt());
                    transaction.getIncomingIfx().getOriginalDataElements().setTerminalId(refTrxSorush.getIncomingIfx().getTerminalId());
                    //mirkamali
//					transaction.getIncomingIfx().setTerminalId(refTrxSorush.getIncomingIfx().getTerminalId());
                }
            }


        }else if(incomingMessage.getIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RS)){
            try {

                if(incomingMessage.getTransaction().getFirstTransaction().getTransactionType().equals(TransactionType.SELF_GENERATED)){
                    incomingMessage.setNeedToBeSent(false);
                    incomingMessage.setNeedToBeInstantlyReversed(false);
                }
            } catch (Exception e) {
                logger.error("Exception in detecting sorush:" + e.toString());
                logger.error(e, e);
            }
            try{
            	transaction.getIncomingIfx().getSafeOriginalDataElements().setRefSorushiTransaction( incomingMessage.getTransaction().getFirstTransaction().getIncomingIfx().getSafeOriginalDataElements().getRefSorushiTransaction());          	
            }catch(Exception e){
            	
            }
           
            refTrxSorush = TransactionService.getRefrenceTransactionSorush(transaction.getIncomingIfx());
            if(ISOResponseCodes.APPROVED.equals(incomingMessage.getIfx().getRsCode())){
                refTrxSorush.getLifeCycle().setIsFullyReveresed(LifeCycleStatus.RESPONSE);
                refTrxSorush.getLifeCycle().setSorushLifeCycle(transaction.getInputMessage().getTransaction().getId());
//				transaction.setReferenceTransaction(refTrxSorush);
                GeneralDao.Instance.saveOrUpdate(refTrxSorush.getLifeCycle());
            }
        }else if(incomingMessage.getIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ)){

        }else if(incomingMessage.getIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS)){
            if(ISOResponseCodes.APPROVED.equals(incomingMessage.getIfx().getRsCode()) ||
                    ISOResponseCodes.INVALID_ACCOUNT.equals(incomingMessage.getIfx().getRsCode())){
                refTrxSorush = TransactionService.getRefrenceTransactionSorush(transaction.getReferenceTransaction().getIncomingIfx());
                if(refTrxSorush != null){
                    refTrxSorush.getLifeCycle().setIsFullyReveresed(null);
                    refTrxSorush.getLifeCycle().setSorushLifeCycle(null);
                    GeneralDao.Instance.saveOrUpdate(	refTrxSorush.getLifeCycle());
                }
            }

        }
        if (refTrx != null) {

            Message refMessage = refTrx.getInputMessage();

            boolean isPartiallyReversed = false;

            if (ISOFinalMessageType.isReversalRsMessage(incomingMessage.getIfx().getIfxType()) &&
                    (ISOResponseCodes.APPROVED.equals(incomingMessage.getIfx().getRsCode())
                            ||ISOResponseCodes.INVALID_ACCOUNT.equals(incomingMessage.getIfx().getRsCode())
                    ) ) {
                Ifx outIfx = refTrx.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
                Long amt_acq = Util.longValueOf(outIfx.getNew_AmtAcqCur());
                Long amt_iss = Util.longValueOf(outIfx.getNew_AmtIssCur());
                if ((amt_acq != null && !amt_acq.equals(0L)) || (amt_iss != null && !amt_iss.equals(0L))) {
                    isPartiallyReversed = true;
                }

                Transaction referenceTransaction = refTrx.getReferenceTransaction();
                if (referenceTransaction != null) {
                    Long real_Amt = (amt_acq != null && !amt_acq.equals(0L)) ? amt_acq : amt_iss;
                    incomingMessage.getIfx().setReal_Amt(real_Amt);
                    referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.setReal_Amt(real_Amt);
                    GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
                    GeneralDao.Instance.saveOrUpdate(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/);
                    Transaction responseTrx = TransactionService.findResponseTrx(referenceTransaction.getLifeCycleId(), referenceTransaction);
                    if (responseTrx != null) {
                        try {
                            responseTrx.getIncomingIfx()/*getInputMessage().getIfx()*/.setReal_Amt(real_Amt);
                            GeneralDao.Instance.saveOrUpdate(responseTrx.getIncomingIfx()/*getInputMessage().getIfx()*/);
                        } catch (Exception e) {
                        }
                    }
                }
            } else if (!refMessage.isScheduleMessage() && ISOFinalMessageType.isReversalRsMessage(incomingMessage.getIfx().getIfxType())
                    && ISOResponseCodes.FIELD_ERROR.equals(incomingMessage.getIfx().getRsCode())
                    && refTrx.getReferenceTransaction()!=null){

                ScheduleMessage reverseMessage = SchedulerService.addInstantReversalAndRepeatTriggerAndRemoveOldTriggers(
                        refTrx.getReferenceTransaction(), refMessage.getIfx().getRsCode(), Util.longValueOf(refMessage.getIfx().getNew_AmtIssCur()));

                if (reverseMessage.getId() != null) {
                    GeneralDao.Instance.saveOrUpdate(reverseMessage);
                    GeneralDao.Instance.saveOrUpdate(reverseMessage.getMsgXml());
                }

                processContext.addPendingRequests(reverseMessage);
            }

            if (refMessage.isScheduleMessage() && refMessage.getIfx()==null)
            // TODO:Schedule should create IFX!
            {
                logger.info("Breaking down normal flow into own-schedule-answer handler: " +
                        "RSCode:" + transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode());

//                 finalizeSelfEndedTransaction(transaction);
                String cause = ((ScheduleMessage)refMessage).getResponseCode();

                TransactionService.putFlagOnOurReversalTransaction(transaction, isPartiallyReversed, cause);
//                 transaction.setEndDateTime(DateTime.now());
                logger.info("ScheduleMessageFlowBreakDown");
                throw new ScheduleMessageFlowBreakDown();
            }

        }

        return createOutgoingMessageForNormalTransactions(transaction, incomingMessage, channel, processContext);
    }

    private Message createOutgoingMessageForNormalTransactions(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws CloneNotSupportedException {
        Message outgoingMessage = new Message(MessageType.OUTGOING);
        outgoingMessage.setTransaction(transaction);
        transaction.addOutputMessage(outgoingMessage);
        outgoingMessage.setChannel(channel);

//        logger.debug("Process incoming message ");
        Ifx incomingIfx = incomingMessage.getIfx();

        if (IfxType.RETURN_RS.equals(incomingIfx.getIfxType()) &&
                ISOResponseCodes.APPROVED.equals(incomingIfx.getRsCode()))
            LotteryService.unlockLottery(incomingIfx, transaction);

        Ifx outgoingIfx = MsgProcessor.processor(incomingIfx);

        //TODO only for reversal messages(shetab duplicate)
        outgoingIfx.setMy_TrnSeqCntr(outgoingIfx.getSrc_TrnSeqCntr());

        outgoingIfx.setFwdBankId(""+channel.getInstitutionId());

        outgoingMessage.setIfx(outgoingIfx);


        setMessageFlag(outgoingMessage, incomingMessage.getRequest(), incomingMessage.getNeedResponse(), incomingMessage.getNeedToBeSent(), incomingMessage.getNeedToBeInstantlyReversed());

        Terminal endpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), true, processContext);
        outgoingMessage.setEndPointTerminal(endpointTerminal);
        addNecessaryDataToIfx(outgoingIfx, channel, endpointTerminal);

        GeneralDao.Instance.saveOrUpdate(outgoingIfx);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());

        transaction.setOutgoingIfx(outgoingIfx);
        GeneralDao.Instance.saveOrUpdate(transaction);

        return outgoingMessage;
    }

    @Override
    public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
        IfxType ifxType = ifx.getIfxType();
        if (ISOFinalMessageType.isRequestMessage(ifxType) &&
                !ISOFinalMessageType.isReversalMessage(ifxType)) {

            if (ifx.getTrnType().equals(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT))
                return;

            if (ifx.getAppPAN() == null || (ifx.getAppPAN().length() != 16 && ifx.getAppPAN().length() != 19))
                throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has wrong AppPan: " + ifx.getAppPAN());
        }
    }




}
