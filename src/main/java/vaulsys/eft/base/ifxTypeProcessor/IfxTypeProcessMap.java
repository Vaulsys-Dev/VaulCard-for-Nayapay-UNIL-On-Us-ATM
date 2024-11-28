package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.Institution;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class IfxTypeProcessMap {

	private static Logger logger = Logger.getLogger(IfxTypeProcessMap.class);
	
	public static MessageProcessor getProcessor(Ifx ifx, IfxType firstIfxType, IfxType refIfxType) {

		String destBankId = ifx.getDestBankId();
		String recvBankId = ifx.getRecvBankId();
		String bankId = ifx.getBankId();
		IfxType ifxType = ifx.getIfxType();

//		Institution myInstitution = GlobalContext.getInstance().getMyInstitution();
		Institution myInstitution = ProcessContext.get().getMyInstitution();
		Long myBin = myInstitution.getBin();
		//m.rehman: for pre-auth completion from onelink
		//Channel channel = (Channel)ProcessContext.get().getOutputChannel("out");

//		if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
//			return GeneralMessageProcessor.Instance;
//		}
		//m.rehman: IfxTypeProcessMap for Advice message SAF/Loro
		if (IfxType.REFUND_ADVICE_RQ.equals(ifxType) || IfxType.IBFT_ADVICE_RQ.equals(ifxType)
				|| IfxType.PREAUTH_COMPLET_ADVICE_RQ.equals(ifxType)) {
			return AdviceMessageProcessor.Instance;

		}
		//m.rehman: for pin change request
		else if (IfxType.CHANGE_PIN_BLOCK_RQ.equals(ifxType)) { // || IfxType.PREAUTH_COMPLET_ADVICE_RQ.equals(ifxType)) {
			return PinChangeMessageProcessor.Instance;

		}
		//m.rehman: for void transaction from NAC
		else if (IfxType.VOID_RQ.equals(ifxType)) {
			return ReversalMessageProcess.Instance;
		}
		//m.rehman: for batch transctions from NAC
		else if (ifx.getMti().equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_87)
				|| ifx.getMti().equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_93)) {
			return BatchUploadMessageProcessor.Instance;

		} else if(TransactionService.IsSorush(ifx) || TransactionService.IsSorushReverce(ifx)){
			return SorushTransferProcessor.Instance;

		} else if (IfxType.PURCHASE_TOPUP_RQ.equals(ifxType) || IfxType.PURCHASE_TOPUP_RQ.equals(firstIfxType)
				|| IfxType.PURCHASE_TOPUP_REV_REPEAT_RQ.equals(ifxType) || IfxType.PURCHASE_TOPUP_REV_REPEAT_RS.equals(ifxType)) {
			return PurchaseTopupProcessor.Instance;

		} else if (IfxType.PURCHASE_CHARGE_RQ.equals(ifxType) || IfxType.PURCHASE_CHARGE_RQ.equals(firstIfxType)) {
			return PurchaseChargeProcessor.Instance;
			
		} else if (IfxType.SORUSH_REV_REPEAT_RQ.equals(ifxType) || IfxType.SORUSH_REV_REPEAT_RQ.equals(firstIfxType)) {
			return SorushProcessor.Instance;
			
		} else if (IfxType.TRANSFER_RQ.equals(ifxType) || IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType)) {
			if (ISOFinalMessageType.isForwardingTransferRq(destBankId, recvBankId, ifxType, myBin, ifx))
				return GeneralMessageProcessor.Instance;
			
			else
				return TransferProcessor.Instance;
			
		} else if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(ifxType) && IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ.equals(firstIfxType)
					|| IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS.equals(ifxType) && IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ.equals(refIfxType)){
			return OnlineSettlementMessageProcess.Instance;
			
		} else if (	
//					(IfxType.TRANSFER_RS.equals(ifxType)) ||
					(IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(ifxType) && !IfxType.TRANSFER_FROM_ACCOUNT_RQ.equals(firstIfxType)) ||
					(IfxType.TRANSFER_TO_ACCOUNT_RS.equals(ifxType) && !IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(firstIfxType)) ||
					(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS.equals(ifxType)) && !IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(firstIfxType)) {
			return TransferProcessor.Instance;
			
		} else if (IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifxType) || IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)) {
			if (ISOFinalMessageType.isForwardingTransferRevRq(Util.longValueOf(destBankId), Util.longValueOf(recvBankId), ifxType, myBin))
				//TODO test this part carefully!! all possible scenarios
				return /*new GeneralMessageProcessor();*/ ReversalMessageProcess.Instance;
			else
				return /*new TransferReversalProcessor()*/ReversalMessageProcess.Instance;
			
		} else if (/*IfxType.BILL_PMT_RQ.equals(ifxType) || */IfxType.BILL_PMT_RS.equals(ifxType) &&
				ISOFinalMessageType.isBillPaymentMessage(firstIfxType)){
			return BillPaymentProcess.Instance;
			

		} else if(ISOFinalMessageType.isOnlineBillPayment(ifxType)||ISOFinalMessageType.isOnlineBillPayment(firstIfxType)||
				ISOFinalMessageType.isPrepareOnlineBillPayment(ifxType)||
				ISOFinalMessageType.isTrackingMessage(ifxType)){
			return OnlineBillPaymentProcessor.Instance;
			/*
			 * if (ShetabFinalMessageType.isTrackingMessage(ifxType)|| ShetabFinalMessageType.isOnlineBillPayment(ifxType)||
					ShetabFinalMessageType.isPrepareOnlineBillPayment(ifxType)){		
				return OnlineBillPaymentProcessor.Instance;
			}
			 * */
		}else if(	
//					IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifxType) ||
					( ( /*IfxType.TRANSFER_FROM_ACCOUNT_REV_RS.equals(ifxType)|| */
							IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS.equals(ifxType) ) 
							&& !IfxType.TRANSFER_FROM_ACCOUNT_RQ.equals(refIfxType)) ||
					((/*IfxType.TRANSFER_TO_ACCOUNT_REV_RS.equals(ifxType)||*/
							IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS.equals(ifxType)) 
							&& !IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(refIfxType)) ||
							IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS.equals(ifxType)
							&& !IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(refIfxType)) {
			return TransferReversalProcessor.Instance;
		} else if (ISOFinalMessageType.isReversalRqMessage(ifxType) &&
				!FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
			/*
			 * 1- if we are aqcuier, we'll send reversal response
			 * immediately, 
			 * 2- if we are not issuer and aqcuier (we're only
			 * forwarding switch), we'll send reversal response immediately,
			 * 3- otherwise, we are only issuer so we should send reversal
			 * request to CMS
			 */
			//m.rehman: for all reversal cases, we'll send reversal response immediately
			/*
			if (myBin.equals(bankId))
				return ReversalMessageProcess.Instance;
			else if (!myBin.equals(destBankId))
				return ReversalMessageProcess.Instance;
			*/
			return ReversalMessageProcess.Instance;

		} else if (ISOFinalMessageType.isChangePinBlockMessage(ifxType)) {
			// TODO return new ChangePINBlockProcessor();
		
		} else if (IfxType.PARTIAL_DISPENSE_RQ.equals(ifxType)) {
			return PartialDispenseProcess.Instance;
		
		} else if (IfxType.LAST_PURCHASE_CHARGE_RQ.equals(ifxType)) {
			return LastPurchaseChargeProcess.Instance;

		} else if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifxType)) {
			if (ISOResponseCodes.shouldBeCaptured(ifx.getRsCode())) {
				ifx.setRsCode(ISOResponseCodes.INVALID_MERCHANT);
			} else 
				return TransferCheckAccountProcessor.Instance;
			
		} else if (/*myBin.equals(bankId) &&
				myBin.equals(destBankId) &&*/
				(IfxType.PURCHASE_RQ.equals(ifxType) ||
					IfxType.PURCHASE_REV_REPEAT_RQ.equals(ifxType)) ||
					IfxType.RETURN_RQ.equals(ifxType) ||
					IfxType.RETURN_REV_REPEAT_RQ.equals(ifxType)) {
			return PurchaseProcessor.Instance;
			
		} else if(IfxType.THIRD_PARTY_PURCHASE_RQ.equals(ifxType) || IfxType.THIRD_PARTY_PURCHASE_RQ.equals(firstIfxType)){
			return ThirdPartyPaymentProcessor.Instance;
			
		} else if(IfxType.GHASEDAK_RQ.equals(ifxType)) {
			return GhasedakProcessor.Instance;
			
		
		} else if(IfxType.WITHDRAWAL_CUR_RQ.equals(ifxType)|| IfxType.WITHDRAWAL_CUR_RQ.equals(firstIfxType)) {	//Mirkamali(Task179): Currency ATM
			return WithdrawalProcess.Instance;
		}
		else if (!ISOFinalMessageType.isRequestMessage(ifxType) &&
				!ISOFinalMessageType.isResponseMessage(ifxType)) {
			return NotRequestNotResponseProcess.Instance;
		}
			
		
		return GeneralMessageProcessor.Instance;
	}

	public static MessageProcessor getAuthorizationProcessor(Ifx ifx, Channel channel) {
		IfxType ifxType = ifx.getIfxType();

//		Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
//		
//		if (ifx.getBankId().equals(myBin)) {
			
			if ((ISOFinalMessageType.isBillPaymentRqMessage(ifxType) ||
					ISOFinalMessageType.isPrepareBillPaymentRqMessage(ifxType))) {
				return BillPaymentProcess.Instance;
			}
			//Mirkamali(Task179): Currency ATM
			if (((ISOFinalMessageType.isWithdrawalRqMessage(ifxType) || ISOFinalMessageType.isWithdrawalRqMessage(ifx.getSecIfxType())
					|| ISOFinalMessageType.isWithdrawalCurRqMessage(ifxType) || ISOFinalMessageType.isWithdrawalCurRqMessage(ifx.getSecIfxType())
					|| IfxType.PREPARE_WITHDRAWAL.equals(ifxType) || IfxType.PREPARE_WITHDRAWAL_REV_REPEAT.equals(ifxType))
					&& TerminalType.ATM.equals(ifx.getTerminalType())
					&& EndPointType.ATM_TERMINAL.equals(channel.getEndPointType()))) {
				return WithdrawalProcess.Instance;
			}
			
			if (ISOFinalMessageType.isRequestMessage(ifxType) &&
					ISOFinalMessageType.isTransferMessage(ifxType)) {
				return TransferProcessor.Instance;
			}
			
			if (ISOFinalMessageType.isTrackingMessage(ifxType)|| ISOFinalMessageType.isOnlineBillPayment(ifxType)||
					ISOFinalMessageType.isPrepareOnlineBillPayment(ifxType)){
				return OnlineBillPaymentProcessor.Instance;
			}
			
			if(ISOFinalMessageType.isThirdPartyPurchaseMessage(ifxType)
					|| IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(ifxType)
					|| IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT.equals(ifxType)){
				return ThirdPartyPaymentProcessor.Instance;
			}
			
			if (ISOFinalMessageType.isHotCardMessage(ifxType)) {
					return HotCardProcessor.Instance;
			}


//		}
		
		return GeneralMessageProcessor.Instance;
	}
}
