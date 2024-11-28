package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.message.exception.ReversalOriginatorNotFoundException;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public class PINPADProcessor extends TerminalTypeProcessor {

	Logger logger = Logger.getLogger(PINPADProcessor.class);

	
	public static final PINPADProcessor Instance = new PINPADProcessor();
	private PINPADProcessor(){};

	
	@Override
	public void messageValidation(Ifx ifx, Long messageId) throws Exception {
		Terminal endPointTerminal = ifx.getEndPointTerminal();
		PINPADTerminal pinpad = null;
		
//		pos = (POSTerminal) ifx.getOriginatorTerminal();
		if (TerminalType.PINPAD.equals(endPointTerminal.getTerminalType()))
			pinpad = (PINPADTerminal) endPointTerminal;
		else{
			pinpad = TerminalService.findTerminal(PINPADTerminal.class, Long.valueOf(ifx.getTerminalId()));
			ifx.setOriginatorTerminal(pinpad);
		}
		
		if (pinpad == null)
			return;
		
		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
			if (pinpad.getId().equals(endPointTerminal.getId())) {
				if (Util.hasText(ifx.getSerialno())) {
					if (!ifx.getSerialno().equals(pinpad.getSerialno())) {
						throw new AuthorizationException("PINPAD: " + pinpad.getCode() + ", incoming serialno: " + ifx.getSerialno() + ", pinpad serialno: " + pinpad.getSerialno());
					}
				}
			}
		}

		super.messageValidation(ifx, messageId);
	}
	

	@Override
	public void checkValidityOfLastTransactionStatus(Ifx incomingIfx) {
		try {
			PINPADTerminal pinpad = TerminalService.findTerminal(PINPADTerminal.class, Util.longValueOf(incomingIfx.getTerminalId()));
			TransactionService.checkValidityOfLastTransactionStatus(pinpad, incomingIfx);
			ProtocolMessage protocolMessage = incomingIfx.getTransaction().getInputMessage().getProtocolMessage();
			if(protocolMessage instanceof RqBaseMsg) {
				Transaction lastTransaction = pinpad.getLastTransaction();
				if (lastTransaction != null) {
					ClearingInfo srcClrInfo = lastTransaction.getSourceClearingInfo();
					if(srcClrInfo != null && ClearingState.DISAGREEMENT.equals(srcClrInfo.getClearingState())) {
						Transaction trxforReverse = lastTransaction.getFirstTransaction();
							if (lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType().equals(IfxType.TRANSFER_RQ)||
								lastTransaction.getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ)) {
							trxforReverse = TransactionService.findResponseTrx(lastTransaction.getLifeCycleId(), lastTransaction);
						}
						SchedulerService.processReversalJob(trxforReverse, lastTransaction, ISOResponseCodes.APPROVED, null, false);
					}
				}
			}
		} catch (ReversalOriginatorNotFoundException e) {
			logger.error("Check Validity of Last Transaction "+ e.getClass().getSimpleName()+": "+ e.getMessage());
		}
	}
	
	
	@Override
	protected Transaction ifxTypeBindingProcess(Ifx ifx) throws Exception {
		Transaction referenceTransaction = null;

		if (ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType()) && ifx.getOriginalDataElements() != null) {

			Ifx refIncomingIfx = getReversalOriginatorTransaction(ifx);
			referenceTransaction = (refIncomingIfx != null) ? refIncomingIfx.getTransaction() : null;

		} else if ((ISOFinalMessageType.isTransferMessage(ifx.getIfxType()) && !ISOFinalMessageType.isTransferCheckAccountMessage(ifx.getIfxType()))) {

			PINPADTerminal pinpad = (PINPADTerminal) ifx.getEndPointTerminal();
			Transaction lastTransaction = pinpad.getLastTransaction();
			if (ISOFinalMessageType.isTransferCheckAccountMessage(lastTransaction.getIncomingIfx().getIfxType()) ||
					ISOFinalMessageType.isTransferToacChechAccountMessage(lastTransaction.getIncomingIfx().getIfxType())) {

				referenceTransaction = lastTransaction;
                String networkRefIdIncoming = ifx.getNetworkRefId();
                copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
                ifx.setNetworkRefId(networkRefIdIncoming);

			}
		} else {
			referenceTransaction = super.ifxTypeBindingProcess(ifx);
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
		PINPADTerminal pinpad = (PINPADTerminal) incomingIfx.getEndPointTerminal();
		Transaction lastTransaction = pinpad.getLastTransaction();
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
