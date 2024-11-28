package vaulsys.protocols.cms;

import static vaulsys.protocols.cms.utils.CMSMapperUtil.*;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.StringFormat;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

public class CMSIFXToHttpMapper implements IfxToProtocolMapper {
	
	public static final CMSIFXToHttpMapper Instance = new CMSIFXToHttpMapper();
	
	private CMSIFXToHttpMapper(){}
	
	@Override
	public ProtocolMessage map(Ifx ifx, EncodingConvertor convertor) throws Exception {
		CMSHttpMessage message = new CMSHttpMessage();

		
		//Integer ifxType = FromIfxType.get(ifx.getIfxType());
		//message.set(IfxStatics.IFX_IFX_TYPE, ifxType!= null ? ifxType : ifx.getIfxType().getType());
		//Integer trnType = FromTrnType.get(ifx.getTrnType());
		//message.set(IfxStatics.IFX_TRN_TYPE, trnType!= null? trnType : ifx.getTrnType().getType());
		message.set(IfxStatics.IFX_IFX_TYPE, FromIfxType.get(ifx.getIfxType()));
		message.set(IfxStatics.IFX_TRN_TYPE, FromTrnType.get(ifx.getTrnType()));
		
		message.set(IfxStatics.IFX_APP_PAN, ifx.getAppPAN());
		message.set(IfxStatics.ACTUAL_APP_PAN, ifx.getActualAppPAN());

		if (ISOFinalMessageType.isReversalMessage(ifx.getIfxType())) {
			message.set(IfxStatics.IFX_AUTH_AMT, ifx.getAuth_Amt());
			
		} else {
			message.set(IfxStatics.IFX_AUTH_AMT, ifx.getReal_Amt());
			
		}
		message.set(IfxStatics.IFX_AUTH_CUR_CODE, ifx.getAuth_Currency());
		message.set(IfxStatics.IFX_AUTH_CUR_RATE, ifx.getAuth_CurRate());
		message.set(IfxStatics.IFX_SEC_AMT, ifx.getSec_Amt());
		message.set(IfxStatics.IFX_SEC_CUR_CODE, ifx.getSec_Currency());
		message.set(IfxStatics.IFX_SEC_CUR_RATE, ifx.getSec_CurRate());
		
		message.set(IfxStatics.IFX_TOTAL_FEE_AMT, ifx.getTotalFeeAmt());
		
		message.set(IfxStatics.IFX_TRN_SEQ_CNTR, ifx.getSrc_TrnSeqCntr());
		message.set(IfxStatics.IFX_MY_TRN_SEQ_CNTR, ifx.getMy_TrnSeqCntr());
		
		message.set(IfxStatics.IFX_TRN_DT, ifx.getTrnDt());
		message.set(IfxStatics.IFX_TRK2_EQUIV_DATA, ifx.getTrk2EquivData());
		message.set(IfxStatics.IFX_MSG_AUTH_CODE, ifx.getMsgAuthCode());
		message.set(IfxStatics.IFX_EXPDT, ifx.getExpDt());
		message.set(IfxStatics.IFX_CVV2, ifx.getCVV2());
		message.set(IfxStatics.IFX_ACC_TYPE_FROM, FromAccType.get(ifx.getAccTypeFrom()));
		message.set(IfxStatics.IFX_ACC_TYPE_TO, FromAccType.get(ifx.getAccTypeTo()));
		message.set(IfxStatics.IFX_USER_LANGUAGE, FromUserLang.get(ifx.getUserLanguage()));

		message.set(IfxStatics.IFX_TERMINAL_TYPE, FromTerminalType.get(ifx.getTerminalType()));
		message.set(IfxStatics.IFX_BANK_ID, ifx.getBankId());
		message.set(IfxStatics.IFX_FWD_BANK_ID, ifx.getDestBankId());
		message.set(IfxStatics.IFX_RECV_BANK_ID, ifx.getRecvBankId());
		
		message.set(IfxStatics.IFX_NETWORKTRNINFO_NAME, ifx.getName());
		message.set(IfxStatics.IFX_CITY, ifx.getCityCode());
		message.set(IfxStatics.IFX_STATE_PROV, ifx.getStateCode());
		message.set(IfxStatics.IFX_NETWORK_REF_ID, ifx.getNetworkRefId());
		message.set(IfxStatics.IFX_COUNTRY, ifx.getCountryCode());
		message.set(IfxStatics.IFX_TERMINAL_ID, ifx.getTerminalId());
		message.set(IfxStatics.IFX_CORE_BRANCH_CODE, ifx.getCoreBranchCode());
		
		message.set(IfxStatics.IFX_PIN_BLOCK, ifx.getPINBlock());

		message.set(IfxStatics.IFX_STATUS_SEVERITY, FromSeverity.get(ifx.getSeverity()));
		message.set(IfxStatics.IFX_STATUS_DESC, ifx.getStatusDesc());

		message.set(IfxStatics.IFX_ORIG_DT, ifx.getOrigDt());
		message.set(IfxStatics.IFX_POSTED_DT, ifx.getPostedDt());
		message.set(IfxStatics.IFX_SETTLE_DT, ifx.getSettleDt());
		message.set(IfxStatics.IFX_RECIEVED_DT, ifx.getReceivedDt());
		
		
		message.set(IfxStatics.IFX_APPROVAL_CODE, ifx.getApprovalCode());
		message.set(IfxStatics.IFX_RS_CODE, ifx.getRsCode());
		message.set(IfxStatics.IFX_DOCUMENT_NUMBER, ifx.getDocumentNumber());
		
		message.set(IfxStatics.IFX_CARD_HOLDER_NAME, ifx.getCardHolderName());
		message.set(IfxStatics.IFX_CARD_HOLDER_FAMILY, ifx.getCardHolderFamily());
		message.set(IfxStatics.IFX_SECONDARY_APP_PAN, ifx.getSecondAppPan());
		message.set(IfxStatics.ACTUAL_SECONDARY_APP_PAN, ifx.getActualSecondAppPan());
		message.set(IfxStatics.IFX_MAIN_ACCOUNT_NUMBER, ifx.getMainAccountNumber());
		
		
		if (ifx.getOriginalDataElements() != null) {
			message.set(IfxStatics.IFX_ORIGINALDATAELEMENTS_MESSAGE_TYPE, ifx.getOriginalDataElements()
					.getMessageType());
			message.set(IfxStatics.IFX_ORIGINALDATAELEMENTS_TRN_SEQ_CNTR, ifx.getOriginalDataElements()
					.getTrnSeqCounter());
			message.set(IfxStatics.IFX_ORIGINALDATAELEMENTS_ORIG_DT, ifx.getOriginalDataElements().getOrigDt());
			message.set(IfxStatics.IFX_ORIGINALDATAELEMENTS_BANK_ID, ifx.getOriginalDataElements().getBankId());
			message.set(IfxStatics.IFX_ORIGINALDATAELEMENTS_FWD_BANK_ID, ifx.getOriginalDataElements().getFwdBankId());			

		message.set(IfxStatics.IFX_NEW_AMT_ACQ_CUR, ifx.getNew_AmtAcqCur() != null ? ifx.getNew_AmtAcqCur() : "000000000000");
		message.set(IfxStatics.IFX_NEW_AMT_ISS_CUR, ifx.getNew_AmtIssCur() != null ? ifx.getNew_AmtIssCur() : "000000000000");
		}

		message.set(IfxStatics.IFX_ORG_ID_NUM, ifx.getOrgIdNum());
		message.set(IfxStatics.IFX_ORG_ID_TYPE, ifx.getOrgIdType());

		message.set(IfxStatics.IFX_ACCTBALAVAILABLE_ACCT_TYPE, ifx.getAcctBalAvailableType());
		message.set(IfxStatics.IFX_ACCTBALAVAILABLE_BAL_TYPE, ifx.getAcctBalAvailableBalType());
		message.set(IfxStatics.IFX_ACCTBALAVAILABLE_AMT, ifx.getAcctBalAvailableAmt());
		message.set(IfxStatics.IFX_ACCTBALAVAILABLE_CUR_CODE, ifx.getAcctBalAvailableCurCode());
		
		message.set(IfxStatics.IFX_ACCTBALLEDGER_ACCT_TYPE, ifx.getAcctBalLedgerType());
		message.set(IfxStatics.IFX_ACCTBALLEDGER_BAL_TYPE, ifx.getAcctBalLedgerBalType());
		message.set(IfxStatics.IFX_ACCTBALLEDGER_AMT, ifx.getAcctBalLedgerAmt());
		message.set(IfxStatics.IFX_ACCTBALLEDGER_CUR_CODE, ifx.getAcctBalLedgerCurCode());
		
		message.set(IfxStatics.IFX_NEW_PIN_BLOCK, ifx.getNewPINBlock());
		message.set(IfxStatics.IFX_OLD_PIN_BLOCK, ifx.getOldPINBlock());
		
		message.set(IfxStatics.IFX_SUBSIDIARY_ACC_TO, ifx.getSubsidiaryAccTo());
		message.set(IfxStatics.IFX_SUBSIDIARY_ACC_FROM, ifx.getSubsidiaryAccFrom());

//		message.set(IfxStatics.IFX_STATEMENT_NUMBER, ifx.getStatementNumber());
//		message.set(IfxStatics.IFX_STATEMENT_AMOUNT, ifx.getStatementAmount());
		
		message.set(IfxStatics.IFX_BILL_ID, ifx.getBillID());
		message.set(IfxStatics.IFX_BILL_PAYMENT_ID, ifx.getBillPaymentID());
		if (ifx.getBillOrgType() != null) {
			message.set(IfxStatics.IFX_BILL_TYPE, OrganizationType.getCode(ifx.getBillOrgType()));
			message.set(IfxStatics.IFX_BILL_TYPE_NAME, ifx.getBillOrgType().toString());
		}
		
		message.set(IfxStatics.IFX_P48, fillField48(ifx));
		
		if(ifx.getFirstTrxId() != null){
			message.set(IfxStatics.IFX_FIRST_TRANSACTION_ID, ifx.getFirstTrxId());
		}
		
		return message;
	}

	
	@Override
	public String mapError(IfxType type, String rsCode) {
		return null;
	}
	
	public String fillField48(Ifx ifx) {
		StringBuilder p48 = new StringBuilder();
//		String p48 = "";
		if (TerminalType.INTERNET.equals(ifx.getTerminalType())) {
			p48.append("00");
			p48.append(ifx.getCVV2());
		}
		if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())
				&& ifx.getBankId().equals(ProcessContext.get().getMyInstitution().getBin())) {
//			p48.append(StringFormat.formatNew(10, StringFormat.JUST_RIGHT, "0", '0'));
			p48.append("0000000000");
			p48.append(ifx.getBillOrgType().getType());
			p48.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0'));
			p48.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0'));
		}
		return p48.toString();
	}

}
