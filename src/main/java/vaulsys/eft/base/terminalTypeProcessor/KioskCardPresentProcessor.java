package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.message.exception.ReversalOriginatorNotFoundException;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.exception.exception.ReferenceTransactionNotFoundException;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.KIOSKCardPresentTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public class KioskCardPresentProcessor extends TerminalTypeProcessor {

	Logger logger = Logger.getLogger(KioskCardPresentProcessor.class);

	
	public static final KioskCardPresentProcessor Instance = new KioskCardPresentProcessor();
	private KioskCardPresentProcessor(){};

	
	@Override
	public void messageValidation(Ifx ifx, Long messageId) throws Exception {
		Terminal endPointTerminal = ifx.getEndPointTerminal();
		KIOSKCardPresentTerminal kiosk = null;
		
//		pos = (POSTerminal) ifx.getOriginatorTerminal();
		if (TerminalType.KIOSK_CARD_PRESENT.equals(endPointTerminal.getTerminalType()))
			kiosk = (KIOSKCardPresentTerminal) endPointTerminal;
		else{
			kiosk = TerminalService.findTerminal(KIOSKCardPresentTerminal.class, Long.valueOf(ifx.getTerminalId()));
			ifx.setOriginatorTerminal(kiosk);
		}
		
		
		if (kiosk == null)
			return;
		
		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
			if (kiosk.getId().equals(endPointTerminal.getId())) {
				if (Util.hasText(ifx.getSerialno())) {
					
					if (!ifx.getSerialno().equals(kiosk.getSerialno())) {
						throw new AuthorizationException("KIOSK: " + kiosk.getCode() + ", incoming serialno: " + ifx.getSerialno() + ", kiosk serialno: " + kiosk.getSerialno());
					}
				}
			}
			
			if (!ISOFinalMessageType.isReversalMessage(ifx.getIfxType()) && !ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
				Transaction lastTransaction = kiosk.getLastTransaction();
				if (lastTransaction != null) {
					Ifx lastIfx = lastTransaction.getIncomingIfx();
					Long currTrnSeqCntr = Long.parseLong(ifx.getSrc_TrnSeqCntr());
					Long lastTrnSeqCntr = Long.parseLong(lastIfx.getSrc_TrnSeqCntr());
					if (currTrnSeqCntr.compareTo(lastTrnSeqCntr) <= 0 && currTrnSeqCntr.compareTo(0L) > 0) {
						throw new AuthorizationException("KIOSK: " + kiosk.getCode() + ", incoming trnSeqCntr: " + ifx.getSrc_TrnSeqCntr() + ", last trnSeqCntr: " + lastIfx.getSrc_TrnSeqCntr(), true); 
					}
				}
			}
		}

		super.messageValidation(ifx, messageId);
	}
	

	@Override
	public void checkValidityOfLastTransactionStatus(Ifx incomingIfx) {
		try {
			KIOSKCardPresentTerminal kiosk = (KIOSKCardPresentTerminal) incomingIfx.getEndPointTerminal();
			if(kiosk == null)
				return;
			
			TransactionService.checkValidityOfLastTransactionStatus(kiosk, incomingIfx);
		} catch (ReversalOriginatorNotFoundException e) {
			logger.error("Check Validity of Last Transaction "+ e.getClass().getSimpleName()+": "+ e.getMessage(), e);
		}
	}
	
	
	@Override
	protected Transaction ifxTypeBindingProcess(Ifx ifx) throws Exception {
		Transaction referenceTransaction = null;
		KIOSKCardPresentTerminal kiosk = (KIOSKCardPresentTerminal) ifx.getEndPointTerminal();
		Transaction transaction = ifx.getTransaction();
		Transaction lastTransaction = kiosk.getLastTransaction();
		
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

			/*if (!referenceTransaction.getIncomingIfx()getInputMessage().getIfx().getTrk2EquivData().equals(ifx.getTrk2EquivData()))
				throw new ReferenceTransactionNotFoundException(
						"No Last Charge Purchase Transaction with this Trk2 was found for the transfer request.(trx: "
								+ transaction.getId() + ")");*/

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
            String networkRefIdIncoming = ifx.getNetworkRefId();
            copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
            ifx.setNetworkRefId(networkRefIdIncoming);

        } /*else if (IfxType.SADERAT_BILL_PMT_RQ.equals(ifx.getIfxType()) && lastTransaction != null) {
			if (lastTransaction.getOutgoingIfx() != null) {
				Ifx lastIfx = lastTransaction.getOutgoingIfx();
				if (IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RS.equals(lastTransaction.getOutgoingIfx().getIfxType()) && ErrorCodes.APPROVED.equals(lastIfx.getRsCode())) {
					if (lastTransaction.getIncomingIfx().getAppPAN().equals(ifx.getAppPAN()) &&
							lastTransaction.getIncomingIfx().getNetworkRefId().equals(ifx.getNetworkRefId()) &&
							lastTransaction.getIncomingIfx().getBillID().equals(ifx.getBillID()) &&
							lastTransaction.getIncomingIfx().getBillPaymentID().equals(ifx.getBillPaymentID()))
					referenceTransaction = lastTransaction;
					copyFieldToIncomingIfx(ifx, lastIfx);
				}
			}
			
			
			if (referenceTransaction == null) {
				throw new ReferenceTransactionNotFoundException(
						"No Saderat Auth BillPayment Transaction was found for the Saderat Billpayment request.(trx: "
								+ transaction.getId() + ")");

			}
		}*/
		
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
		KIOSKCardPresentTerminal pos = (KIOSKCardPresentTerminal) incomingIfx.getEndPointTerminal();
		Transaction lastTransaction = pos.getLastTransaction();
		DateTime lastReceivedDate = null;
		Ifx refIncomingIfx = null;
		
//		if (incomingIfx.getTrnType().equals(TrnType.TRANSFER)) {
			
		
		
		if (lastTransaction != null && lastTransaction.getFirstTransaction() != null) {			
			Transaction refTrnx = lastTransaction.getFirstTransaction();	
			
			
			if (incomingIfx.getTrnType().equals(TrnType.TRANSFER) && lastTransaction.getReferenceTransaction() != null) {
				refTrnx = lastTransaction.getReferenceTransaction();
			}
			
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
