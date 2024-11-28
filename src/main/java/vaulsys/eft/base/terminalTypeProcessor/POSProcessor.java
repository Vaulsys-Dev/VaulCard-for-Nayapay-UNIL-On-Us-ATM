package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.authentication.exception.AuthenticationException;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.message.exception.ReversalOriginatorNotFoundException;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.exception.exception.ReferenceTransactionNotFoundException;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public class POSProcessor extends TerminalTypeProcessor {

    Logger logger = Logger.getLogger(POSProcessor.class);


    public static final POSProcessor Instance = new POSProcessor();
    private POSProcessor(){};


    @Override
    public void messageValidation(Ifx ifx, Long messageId) throws Exception {
        Terminal endPointTerminal = ifx.getEndPointTerminal();
        POSTerminal pos = null;

//		pos = (POSTerminal) ifx.getOriginatorTerminal();
        if (TerminalType.POS.equals(endPointTerminal.getTerminalType()))
            pos = (POSTerminal) endPointTerminal;
        else{
            pos = TerminalService.findTerminal(POSTerminal.class, Long.valueOf(ifx.getTerminalId()));
            ifx.setOriginatorTerminal(pos);
        }

        if (pos == null)
            return;

        if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
            if (pos.getId().equals(endPointTerminal.getId())) {
                if (Util.hasText(ifx.getSerialno())) {
                    if (!ifx.getSerialno().equals(pos.getSerialno())) {
                        throw new AuthorizationException("POS: " + pos.getCode() + ", incoming serialno: " + ifx.getSerialno() + ", pos serialno: " + pos.getSerialno());
                    }
                }
            }
        }


        if (!ISOFinalMessageType.isReversalMessage(ifx.getIfxType()) && !ISOFinalMessageType.isReturnMessage(ifx.getIfxType()) && !ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
            Transaction lastTransaction = pos.getLastTransaction();
            if (lastTransaction != null) {
                Ifx lastIfx = lastTransaction.getIncomingIfx();
                Long currTrnSeqCntr = Long.parseLong(ifx.getSrc_TrnSeqCntr());
                Long lastTrnSeqCntr_switch = Long.parseLong(lastIfx.getSrc_TrnSeqCntr());
                Long lastTrnSeqCntr_term = Long.parseLong(ifx.getLast_TrnSeqCntr());
//                Long lastLastTrnSeqCntr_term = Long.valueOf(lastIfx.getLast_TrnSeqCntr());

                /******** Mirkamali(Task134) : So IMPORTANT! **********/
                 /*
                  *  1- ifx.getLast_TrnSeqCntr() ----> is TERMINAL last trnSeqCntr
                  *  2- lastIfx.getSrc_TrnSeqCntr() ----> is SWITCH last trnSeqCntr
                 */
                if(ifx.getPosSpecificData().getSerialno() != null && ifx.getPosSpecificData().getSerialno().equals(lastIfx.getPosSpecificData().getSerialno())	//Don't Change pos
                        && Long.parseLong(ifx.getLast_TrnSeqCntr()) != 0L	//Don't install new app
                        && currTrnSeqCntr.compareTo(lastTrnSeqCntr_switch) <= 0	//A trx is missed
                        && currTrnSeqCntr.compareTo(lastTrnSeqCntr_term) >= 0	//loop not accured
                        ){
                    throw new AuthorizationException("POS: " + pos.getCode() + ", incoming trnSeqCntr: " + ifx.getSrc_TrnSeqCntr() + ", last trnSeqCntr: " + lastIfx.getSrc_TrnSeqCntr(), false);
                }
            }
        }

        //super.messageValidation(ifx, messageId);
        if (!Util.isAccount(ifx.getAppPAN()) && Util.hasText(ifx.getAppPAN()) && !ifx.getAppPAN().substring(0, 6).equals(ifx.getDestBankId().toString()))
            throw new AuthenticationException(ifx.getAppPAN() + " doesn't belong to bank " + ifx.getDestBankId(), true);

        if (!ISOFinalMessageType.isReversalMessage(ifx.getIfxType()) && !ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
            if (Util.hasText(ifx.getTrk2EquivData()) && !ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
                if (!ifx.getTrk2EquivData().contains(ifx.getAppPAN()))
                    throw new AuthenticationException(ifx.getTrk2EquivData() + " doesn't correspond with "
                            + ifx.getAppPAN(), true);
            }
        }
    }


    @Override
    public void checkValidityOfLastTransactionStatus(Ifx incomingIfx) {
        try {
//			POSTerminal pos = TerminalService.findTerminal(POSTerminal.class, Util.longValueOf(incomingIfx.getTerminalId()));
            POSTerminal pos = (POSTerminal) incomingIfx.getEndPointTerminal();
            if(pos == null)
                return;

            TransactionService.checkValidityOfLastTransactionStatus(pos, incomingIfx);
            ProtocolMessage protocolMessage = incomingIfx.getTransaction().getInputMessage().getProtocolMessage();
			if(protocolMessage instanceof RqBaseMsg) { // Apacs
                Transaction lastTransaction = pos.getLastTransaction();
                if (lastTransaction != null) {
                    ClearingInfo srcClrInfo = lastTransaction.getSourceClearingInfo();
                    if(srcClrInfo != null && ClearingState.DISAGREEMENT.equals(srcClrInfo.getClearingState())) {
                        SchedulerService.processReversalJob(lastTransaction.getFirstTransaction(), lastTransaction, ISOResponseCodes.APPROVED, null, false);
                    }
                }
            }
        } catch (ReversalOriginatorNotFoundException e) {
            logger.error("Check Validity of Last Transaction "+ e.getClass().getSimpleName()+": "+ e.getMessage(), e);
        }
    }


    @Override
    protected Transaction ifxTypeBindingProcess(Ifx ifx) throws Exception {
        Transaction referenceTransaction = null;
        POSTerminal pos = (POSTerminal) ifx.getEndPointTerminal();
        Transaction transaction = ifx.getTransaction();
        Transaction lastTransaction = pos.getLastTransaction();

        if (IfxType.LAST_PURCHASE_CHARGE_RQ.equals(ifx.getIfxType()) && lastTransaction!= null) {

            if (lastTransaction.getOutgoingIfx()/*getOutputMessage()*/!= null ){
                Ifx lastIfx = lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
                if (IfxType.PURCHASE_CHARGE_RS.equals(lastIfx.getIfxType()) && ISOResponseCodes.APPROVED.equals(lastIfx.getRsCode()) ){
                    referenceTransaction = lastTransaction;
                    copyFieldToIncomingIfx(ifx, lastIfx);
                }
            }

            if (referenceTransaction == null)
                throw new ReferenceTransactionNotFoundException(
                        "No Last Charge Purchase Transaction was found for the last purchase charge request.(trx: "
                                + transaction.getId() + ")");

            if (!referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getAppPAN().equals(ifx.getAppPAN()))
                throw new ReferenceTransactionNotFoundException(
                        "No Last Charge Purchase Transaction with this AppPan was found for the transfer request.(trx: "
                                + transaction.getId() + ")");

            //if (!referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getTrk2EquivData().equals(ifx.getTrk2EquivData()))
            //	throw new ReferenceTransactionNotFoundException(
            //			"No Last Charge Purchase Transaction with this Trk2 was found for the transfer request.(trx: "
            //					+ transaction.getId() + ")");
            //
        } else if (IfxType.ONLINE_BILLPAYMENT_RQ.equals(ifx.getIfxType()) && lastTransaction != null) {
            if (lastTransaction.getOutgoingIfx() != null) {
                Ifx lastIfx = lastTransaction.getOutgoingIfx();
                if (ISOFinalMessageType.isPrepareOnlineBillPayment(lastTransaction.getOutgoingIfx().getIfxType()) && ISOResponseCodes.APPROVED.equals(lastIfx.getRsCode())) {
                    if (lastTransaction.getIncomingIfx().getAppPAN().equals(ifx.getAppPAN()) &&
                            lastTransaction.getIncomingIfx().getOnlineBillPaymentRefNum().equals(ifx.getOnlineBillPaymentRefNum()))
                        referenceTransaction = lastTransaction;
                    copyFieldToIncomingIfx(ifx, lastIfx);
                }
            }

            if (referenceTransaction == null) {
                throw new ReferenceTransactionNotFoundException(
                        "No Prepare Online BillPayment Transaction was found for the online billpayment request.(trx: "
                                + transaction.getId() + ")");

            }
        } else if ( ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType()) && ifx.getOriginalDataElements() != null ) {

            Ifx refIncomingIfx = getReversalOriginatorTransaction(ifx);
            referenceTransaction = (refIncomingIfx!=null)? refIncomingIfx.getTransaction(): null;

        } else if (ISOFinalMessageType.isReturnRq(ifx.getIfxType()) && ifx.getOriginalDataElements() != null ){
            Ifx referenceIncomingIfx = super.getReversalOriginatorTransaction(ifx);
            if (referenceIncomingIfx != null){
                referenceTransaction = referenceIncomingIfx.getTransaction();
                ifx = checkOriginalData(ifx, referenceIncomingIfx);
            }
        } else if ((ISOFinalMessageType.isTransferMessage(ifx.getIfxType())&& !ISOFinalMessageType.isTransferCheckAccountMessage(ifx.getIfxType()))&&
                ISOFinalMessageType.isTransferCheckAccountMessage(lastTransaction.getIncomingIfx().getIfxType())){
            referenceTransaction = lastTransaction;

        } else if ((ISOFinalMessageType.isTransferCardToAccountMessage(ifx.getIfxType())&& !ISOFinalMessageType.isTransferToacChechAccountMessage(ifx.getIfxType()))&&
                ISOFinalMessageType.isTransferToacChechAccountMessage(lastTransaction.getIncomingIfx().getIfxType())){
            referenceTransaction = lastTransaction;

        }

        return referenceTransaction;
    }

    private Boolean isReferenceTransaction(Transaction lastTransaction, Transaction referenceTransaction, Ifx ifx) throws Exception {
        String terminalId = ifx.getTerminalId();
        String bankId = ifx.getBankId();
        String orgIdNum = ifx.getOrgIdNum();
        String appPAN = ifx.getAppPAN();
        TrnType trnType = ifx.getTrnType();
//		Long amount = ifx.getAuth_Amt();
        DateTime refOrigDt = ifx.getSafeOriginalDataElements().getOrigDt();
        String networkTrnInfo = ifx.getSafeOriginalDataElements().getNetworkTrnInfo();
        String refTrnSeqCounter = ifx.getSafeOriginalDataElements().getTrnSeqCounter();
        Ifx refIncomingIfx = null;

        refIncomingIfx = referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;

        if (!ISOFinalMessageType.isReversalMessage(refIncomingIfx.getIfxType())
                && bankId.equals(refIncomingIfx.getBankId())
                && terminalId.equals(refIncomingIfx.getTerminalId())
                && orgIdNum.equals(refIncomingIfx.getOrgIdNum())
                && appPAN.equals(refIncomingIfx.getAppPAN())
                && trnType.equals(refIncomingIfx.getTrnType())
//				&& (amount == null || amount.equals(refIncomingIfx.getAuth_Amt()))
                && (refOrigDt == null || refOrigDt.equals(refIncomingIfx.getOrigDt()))
                && (!Util.hasText(networkTrnInfo) || networkTrnInfo.equals(refIncomingIfx.getNetworkRefId()))
                && (!Util.hasText(refTrnSeqCounter) || refTrnSeqCounter.equals(refIncomingIfx.getSrc_TrnSeqCntr()))) {
            ifx.getTransaction().setReferenceTransaction(referenceTransaction);
            return true;
        }

        return false;
    }


    @Override
    protected Ifx checkOriginalData(Ifx incomingIfx, Ifx refIncomingIfx) throws InvalidBusinessDateException {
        if (refIncomingIfx == null)
            return incomingIfx;

        Transaction referenceTransaction = refIncomingIfx.getTransaction();
		
		/*if (referenceTransaction.getSourceSettleInfo() != null
				&& !SettledState.NOT_SETTLED.equals(referenceTransaction.getSourceSettleInfo().getSettledState())) {
			throw new InvalidBusinessDateException("Originator Transaction already settled.(refTrx: "
					+ referenceTransaction.getId() + ")");
		}*/

        if (TransactionService.isReferenceTrxSettled(referenceTransaction))
            throw new InvalidBusinessDateException("Originator Transaction already settled.(refTrx: "
                    + referenceTransaction.getId() + ")");


        if (ISOFinalMessageType.isReturnRq(incomingIfx.getIfxType())) {
            incomingIfx.setAuth_Amt(refIncomingIfx.getAuth_Amt());
            incomingIfx.setReal_Amt(incomingIfx.getReal_Amt());
            incomingIfx.setTrx_Amt(incomingIfx.getTrx_Amt());
            incomingIfx.setSec_Amt(refIncomingIfx.getSec_Amt());
        }

        if (incomingIfx.getOriginalDataElements().getFwdBankId() == null)
            incomingIfx.getOriginalDataElements().setFwdBankId(refIncomingIfx.getDestBankId());
        if (incomingIfx.getOriginalDataElements().getBankId() == null)
            incomingIfx.getOriginalDataElements().setBankId(refIncomingIfx.getBankId());
        if (incomingIfx.getOriginalDataElements().getOrigDt() == null)
            incomingIfx.getOriginalDataElements().setOrigDt(refIncomingIfx.getOrigDt());
        if (incomingIfx.getOriginalDataElements().getTrnSeqCounter() == null)
            incomingIfx.getOriginalDataElements().setTrnSeqCounter(refIncomingIfx.getSrc_TrnSeqCntr());
        if (incomingIfx.getOriginalDataElements().getTerminalId() == null)
            incomingIfx.getOriginalDataElements().setTerminalId(refIncomingIfx.getTerminalId());
        if (incomingIfx.getOriginalDataElements().getAppPAN() == null)
            incomingIfx.getOriginalDataElements().setAppPAN(refIncomingIfx.getAppPAN());

        if (!Util.hasText(incomingIfx.getPINBlock())) {
            if (Util.hasText(incomingIfx.getAppPAN())
                    && incomingIfx.getAppPAN().equals(refIncomingIfx.getAppPAN())) {
                incomingIfx.setPINBlock(refIncomingIfx.getPINBlock());
            }
        }
        return incomingIfx;
    }

    @Override
    protected Ifx getReversalOriginatorTransaction(Ifx incomingIfx) throws Exception {
        POSTerminal pos = (POSTerminal) incomingIfx.getEndPointTerminal();
        Transaction lastTransaction = pos.getLastTransaction();
        DateTime lastReceivedDate = null;
        Ifx refIncomingIfx = null;

        if (lastTransaction != null && lastTransaction.getFirstTransaction() != null) {
            Transaction refTrnx = lastTransaction.getFirstTransaction();

            if (refTrnx.getInputMessage().isScheduleMessage())
                //Time-out response
                refTrnx = refTrnx.getReferenceTransaction();

            if (isReferenceTransaction(lastTransaction, refTrnx, incomingIfx))
                refIncomingIfx = refTrnx.getIncomingIfx()/*getInputMessage().getIfx()*/;
            else if (ISOFinalMessageType.isReversalRqMessage(refTrnx.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())){
                refTrnx = lastTransaction.getReferenceTransaction();
                if (refTrnx!= null && isReferenceTransaction(lastTransaction, refTrnx, incomingIfx)){
                    refIncomingIfx = refTrnx.getIncomingIfx()/*getInputMessage().getIfx()*/;
                }
            }
            if (lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/!= null)
                lastReceivedDate = lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getReceivedDt();
        }

        if (refIncomingIfx == null)
            refIncomingIfx =  TransactionService.getReversalOriginatorTransactionForAcqTerminal(incomingIfx, lastReceivedDate);

        incomingIfx = checkOriginalData(incomingIfx, refIncomingIfx);

        return refIncomingIfx;
    }

}
