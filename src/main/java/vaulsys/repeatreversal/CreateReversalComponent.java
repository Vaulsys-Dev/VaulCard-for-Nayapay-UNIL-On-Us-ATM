package vaulsys.repeatreversal;

import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.KeyManagementMode;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class CreateReversalComponent {
	Logger logger = Logger.getLogger(CreateReversalComponent.class);

	private CreateReversalComponent() {}
	
	public static Ifx createReversalIfx(Ifx inIfx) throws Exception {

		Ifx outIfx = inIfx.copy();
		
//		Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
		Long myBin = ProcessContext.get().getMyInstitution().getBin();

		IfxType ifxType = outIfx.getIfxType();

		if (IfxType.PURCHASE_RQ.equals(ifxType) ||
				IfxType.WITHDRAWAL_RQ.equals(ifxType) ||
				IfxType.WITHDRAWAL_CUR_RQ.equals(ifxType) ||	//Mirkamali(Task179)
				IfxType.DEPOSIT_RQ.equals(ifxType) ||
				IfxType.BILL_PMT_RQ.equals(ifxType) || 
				IfxType.BILL_PMT_REV_REPEAT_RQ.equals(ifxType) || 
				IfxType.TRANSFER_RQ.equals(ifxType) || 
				IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifxType) || 
				IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ.equals(ifxType) || 
				IfxType.TRANSFER_FROM_ACCOUNT_RQ.equals(ifxType) || 
				IfxType.BAL_INQ_RQ.equals(ifxType) || 
				IfxType.RETURN_RQ.equals(ifxType) || 
				IfxType.CHANGE_PIN_BLOCK_RQ.equals(ifxType) ||
				IfxType.BANK_STATEMENT_RQ.equals(ifxType) ||
				IfxType.GET_ACCOUNT_RQ.equals(ifxType)||
				IfxType.ONLINE_BILLPAYMENT_RQ.equals(ifxType)||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType)||
				IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(ifxType)||
				IfxType.SHEBA_INQ_RQ.equals(ifxType) ||
				IfxType.HOTCARD_INQ_RQ.equals(ifxType) ||
				IfxType.STOCK_INQ_RQ.equals(ifxType) || //TASK Task081 : ATM Saham Feature
				IfxType.RESTRICTION_RQ.equals(ifxType) || //gholami
				IfxType.PREAUTH_RQ.equals(ifxType) || //m.rehman
				//IfxType.REFUND_RQ.equals(ifxType)	//m.rehman
				IfxType.PREAUTH_COMPLET_RQ.equals(ifxType) ||	//m.rehman
				IfxType.PREAUTH_COMPLET_CANCEL_RQ.equals(ifxType) ||		//m.rehman
				IfxType.PREAUTH_CANCEL_RQ.equals(ifxType) ||		//m.rehman
				IfxType.PURCHASE_CANCEL_RQ.equals(ifxType)		//m.rehman
				) {
			
			if (IfxType.RESTRICTION_RQ.equals(ifxType)) {//gholami
				outIfx.setIfxType(IfxType.RESTRICTION_REV_REPEAT_RQ);
			}
			if(IfxType.RESTRICTION_RQ.equals(ifxType)){
				outIfx.setIfxType(IfxType.RESTRICTION_REV_REPEAT_RQ);
			}
			if (IfxType.PURCHASE_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.PURCHASE_REV_REPEAT_RQ);

			} else if (IfxType.WITHDRAWAL_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);

			}else if (IfxType.WITHDRAWAL_CUR_RQ.equals(ifxType)) {	//Mirkamali(Task179)
				outIfx.setIfxType(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);

			} else if (IfxType.DEPOSIT_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.DEPOSIT_REV_REPEAT_RQ);
				
			} else if (IfxType.BILL_PMT_RQ.equals(ifxType) || IfxType.BILL_PMT_REV_REPEAT_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
			
			} else if (IfxType.BAL_INQ_RQ.equals(ifxType) || IfxType.BAL_REV_REPEAT_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.BAL_REV_REPEAT_RQ);

			} else if (ISOFinalMessageType.isForwardingTransferRq(inIfx.getDestBankId(), inIfx.getRecvBankId(), ifxType, myBin, inIfx)) {
				if(IfxType.TRANSFER_RQ.equals(ifxType))
					outIfx.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
				else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType))
					outIfx.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);

			}else if (IfxType.TRANSFER_RQ.equals(ifxType)) {
				// !ShetabFinalMessageType.isForwardingTransferRq(inIfx.getDestBankId(),
				// inIfx.getRecvBankId(), ifxType, myBin)){
				// IfxType.TRANSFER_RQ.equals(ifxType) &&
				// (inIfx.getDestBankId().equals(myBin) ^
				// inIfx.getRecvBankId().equals(myBin))) {
				outIfx.setIfxType(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);

			}else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType)) {
				// !ShetabFinalMessageType.isForwardingTransferRq(inIfx.getDestBankId(),
				// inIfx.getRecvBankId(), ifxType, myBin)){
				// IfxType.TRANSFER_RQ.equals(ifxType) &&
				// (inIfx.getDestBankId().equals(myBin) ^
				// inIfx.getRecvBankId().equals(myBin))) {
				outIfx.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);

			} else if (IfxType.TRANSFER_FROM_ACCOUNT_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ);

			} else if (IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifxType) || IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(ifxType)) {
				outIfx.setIfxType(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);

				
			}else if (IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(ifxType) || IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)){
				outIfx.setIfxType(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ);
				
			} 
			else if (IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
				
				
			} else if (IfxType.RETURN_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.RETURN_REV_REPEAT_RQ);

			} else if (IfxType.CHANGE_PIN_BLOCK_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
				
			}else if (IfxType.BANK_STATEMENT_RQ.equals(ifxType) || IfxType.BANK_STATEMENT_REV_REPEAT_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);

			}else if (IfxType.GET_ACCOUNT_RQ.equals(ifxType) || IfxType.GET_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)){
				outIfx.setIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
			}else if (IfxType.ONLINE_BILLPAYMENT_RQ.equals(ifxType) || IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RQ.equals(ifxType)){
				outIfx.setIfxType(IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RQ);
			}else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType)|| IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)){
				outIfx.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);			
			}else if (IfxType.SHEBA_INQ_RQ.equals(ifxType)){ 
				outIfx.setIfxType(IfxType.SHEBA_REV_REPEAT_RQ);
			}else if (IfxType.HOTCARD_INQ_RQ.equals(ifxType)){ 
				outIfx.setIfxType(IfxType.HOTCARD_REV_REPEAT_RQ);
			}else if (IfxType.STOCK_INQ_RQ.equals(ifxType)){ //TASK Task081 : ATM Saham feature
				outIfx.setIfxType(IfxType.STOCK_REV_REPEAT_RQ);
			} else if (IfxType.CARD_AUTHENTICATE_RQ.equals(ifxType)) { // TASK Task129 [26604] - Authenticate Cart (Pasargad)
				outIfx.setIfxType(IfxType.CARD_AUTHENTICATE_RS);
			} else if (IfxType.PREAUTH_RQ.equals(ifxType)) {	//m.rehman
				outIfx.setIfxType(IfxType.PREAUTH_REV_REPEAT_RQ);
			} else if (IfxType.PREAUTH_COMPLET_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.PREAUTH_COMPLET_REV_REPEAT_RQ);
			} else if (IfxType.PREAUTH_COMPLET_CANCEL_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ);
			} else if (IfxType.PREAUTH_CANCEL_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ);
			} else if (IfxType.PURCHASE_CANCEL_RQ.equals(ifxType)) {
				outIfx.setIfxType(IfxType.PURCHASE_CANCEL_REV_REPEAT_RQ);
			}

			if (outIfx.getNew_AmtAcqCur() == null)
				outIfx.setNew_AmtAcqCur("000000000000");
			if (outIfx.getNew_AmtIssCur() == null)
				outIfx.setNew_AmtIssCur("000000000000");

			outIfx.setOriginalDataElements(new MessageReferenceData());
			outIfx.getSafeOriginalDataElements().setTrnSeqCounter(outIfx.getSrc_TrnSeqCntr());
			//outIfx.getSafeOriginalDataElements().setMessageType("0200");

			// outIfx.RsCode = ShetabErrorCodes.SHETAB_ERROR_SUCCESS;
			// outIfx.RsCode = ShetabErrorCodes.SHETAB_TIME_OUT;

			outIfx.getSafeOriginalDataElements().setOrigDt(outIfx.getOrigDt());
			outIfx.getSafeOriginalDataElements().setBankId(outIfx.getBankId());
			//m.rehman: forward bank id should be same as received if available, adding condition
			if (outIfx.getFwdBankId() != null)
				outIfx.getSafeOriginalDataElements().setFwdBankId(outIfx.getFwdBankId());
			else
				outIfx.getSafeOriginalDataElements().setFwdBankId(outIfx.getDestBankId());
			outIfx.getOriginalDataElements().setTerminalId(outIfx.getTerminalId());
			outIfx.getOriginalDataElements().setAppPAN(outIfx.getAppPAN());
			outIfx.setSrc_TrnSeqCntr(Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));
			outIfx.setMy_TrnSeqCntr(outIfx.getSrc_TrnSeqCntr());
			outIfx.setIfxDirection(IfxDirection.SELF_GENERATED);

			// outIfx.setExpDt( 0);
			// outIfx.setTerminalType ( TerminalType.UNKNOWN);
			outIfx.setPINBlock(null);

		}else if (IfxType.CUTOVER_RQ.equals(ifxType) ||
				IfxType.CUTOVER_REPEAT_RQ.equals(ifxType)){
				outIfx.setIfxType(IfxType.CUTOVER_REPEAT_RQ);
				outIfx.setSrc_TrnSeqCntr(Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));
				outIfx.setMy_TrnSeqCntr(outIfx.getSrc_TrnSeqCntr());
				outIfx.setMode(KeyManagementMode.ISSUER_PIN);
				outIfx.setCheckDigit("0000");
				outIfx.setNetworkManagementInformationCode(NetworkManagementInfo.CUTOVER);
				outIfx.getKeyManagement().setKey("0000000000000000");
		}else if (IfxType.ACQUIRER_REC_REPEAT_RQ.equals(ifxType) ||IfxType.ACQUIRER_REC_RQ.equals(ifxType)
				 || IfxType.CARD_ISSUER_REC_REPEAT_RQ.equals(ifxType) ||IfxType.CARD_ISSUER_REC_RQ.equals(ifxType)){
			
			if (IfxType.ACQUIRER_REC_REPEAT_RQ.equals(ifxType) ||IfxType.ACQUIRER_REC_RQ.equals(ifxType))
				outIfx.setIfxType(IfxType.ACQUIRER_REC_REPEAT_RQ);
			else if (IfxType.CARD_ISSUER_REC_REPEAT_RQ.equals(ifxType) ||IfxType.CARD_ISSUER_REC_RQ.equals(ifxType))
				outIfx.setIfxType(IfxType.CARD_ISSUER_REC_REPEAT_RQ);
				
			outIfx.setSrc_TrnSeqCntr(Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));
			outIfx.setMy_TrnSeqCntr(outIfx.getSrc_TrnSeqCntr());
			
			outIfx.setReconciliationData(inIfx.getReconciliationData());
		}
		
		
		return outIfx;
	}
}
