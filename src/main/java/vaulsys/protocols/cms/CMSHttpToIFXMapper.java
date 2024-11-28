package vaulsys.protocols.cms;

import static vaulsys.protocols.cms.utils.CMSMapperUtil.*;
import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.calendar.PersianCalendar;
import vaulsys.customer.Currency;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.StatusCode;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.CardAccountInformation;
import vaulsys.protocols.ifx.imp.EMVRsData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class CMSHttpToIFXMapper implements ProtocolToIfxMapper {


	public static final CMSHttpToIFXMapper Instance = new CMSHttpToIFXMapper();
	
	private CMSHttpToIFXMapper(){}
	
	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {

		CMSHttpMessage cmsMessage = (CMSHttpMessage) message;
		ConcurrentHashMap<String, String> map = cmsMessage.getMap();

		/** **************** Map CMSMsg to IFX **************** */
		Ifx ifx = new Ifx();

		if(map.containsKey(IfxStatics.IFX_CARD_HOLDER_MOBILE_NO))
			ifx.setCardHolderMobileNo(map.get(IfxStatics.IFX_CARD_HOLDER_MOBILE_NO).trim());
		
		
		if (map.containsKey(IfxStatics.IFX_IFX_TYPE)){
			ifx.setIfxType( ToIfxType.get((Integer.parseInt(map.get(IfxStatics.IFX_IFX_TYPE).trim()))));
		}

		if (map.containsKey(IfxStatics.IFX_TRN_TYPE))
			ifx.setTrnType((TrnType) ToTrnType.get(Integer.parseInt(map.get(IfxStatics.IFX_TRN_TYPE).trim())));

		if (map.containsKey(IfxStatics.IFX_APP_PAN))
			ifx.setAppPAN(map.get(IfxStatics.IFX_APP_PAN).trim());
		
		if (map.containsKey(IfxStatics.ACTUAL_APP_PAN))
			ifx.setActualAppPAN(map.get(IfxStatics.ACTUAL_APP_PAN).trim());

		if (ISOFinalMessageType.isReversalMessage(ifx.getIfxType())) {
			if (map.containsKey(IfxStatics.IFX_AUTH_AMT)) {
				ifx.setAuth_Amt(Util.longValueOf(map.get(IfxStatics.IFX_AUTH_AMT).trim()));
			}
		} else {
			if (map.containsKey(IfxStatics.IFX_AUTH_AMT)) {
				ifx.setReal_Amt(Util.longValueOf(map.get(IfxStatics.IFX_AUTH_AMT).trim()));
			}
		}

		if (map.containsKey(IfxStatics.IFX_TOTAL_FEE_AMT)){
			ifx.setTotalFeeAmt(Util.longValueOf(map.get(IfxStatics.IFX_TOTAL_FEE_AMT).trim()));
		}
		if (map.containsKey(IfxStatics.IFX_STATEMENT_ACC_NUMBER)){
			ifx.setSubsidiaryAccFrom(map.get(IfxStatics.IFX_STATEMENT_ACC_NUMBER).trim());
		}
		String acquire_currency ="";
		
		if (map.containsKey(IfxStatics.IFX_AUTH_CUR_CODE))
			acquire_currency = map.get(IfxStatics.IFX_AUTH_CUR_CODE).trim();
		
		Currency currency = null;
		if (Util.hasText(acquire_currency)) {
			currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
			if (currency == null) {
				Exception e = new Exception("Ivalid Currency Code: " + acquire_currency);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			} else {
				currency = ProcessContext.get().getRialCurrency();//GlobalContext.getInstance().getRialCurrency();
			}
		}
		
		
		if (map.containsKey(IfxStatics.IFX_SECONDARY_APP_PAN))
			ifx.setSecondAppPan(map.get(IfxStatics.IFX_SECONDARY_APP_PAN).trim());
		
		if (map.containsKey(IfxStatics.ACTUAL_SECONDARY_APP_PAN))
			ifx.setActualSecondAppPAN(map.get(IfxStatics.ACTUAL_SECONDARY_APP_PAN).trim());
		
		if (map.containsKey(IfxStatics.IFX_CARD_HOLDER_NAME))
			ifx.setCardHolderName( convertor.decode( map.get(IfxStatics.IFX_CARD_HOLDER_NAME).trim().getBytes()));
		
		if (map.containsKey(IfxStatics.IFX_CARD_HOLDER_FAMILY))
			ifx.setCardHolderFamily(convertor.decode(map.get(IfxStatics.IFX_CARD_HOLDER_FAMILY).trim().getBytes()));
	
		if (map.containsKey(IfxStatics.IFX_MAIN_ACCOUNT_NUMBER))
			ifx.setMainAccountNumber(map.get(IfxStatics.IFX_MAIN_ACCOUNT_NUMBER).trim());

		
		ifx.setAuth_Currency(currency.getCode());
		
		if (map.containsKey(IfxStatics.IFX_AUTH_CUR_RATE))
			ifx.setAuth_CurRate(map.get(IfxStatics.IFX_AUTH_CUR_RATE).trim());

		if (map.containsKey(IfxStatics.IFX_SEC_AMT))
			ifx.setSec_Amt(Util.longValueOf(map.get(IfxStatics.IFX_SEC_AMT).trim()));
		
		String issuer_currency = "";
		if (map.containsKey(IfxStatics.IFX_SEC_CUR_CODE))
			issuer_currency = map.get(IfxStatics.IFX_SEC_CUR_CODE).trim();
		
		if (Util.hasText(issuer_currency)) {
			Currency iCurrency = null;
			iCurrency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
			if (iCurrency == null) {
				Exception e = new Exception("Ivalid Currency Code: " + acquire_currency);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			ifx.setSec_Currency(iCurrency.getCode());
		}

		if (map.containsKey(IfxStatics.IFX_SEC_CUR_RATE))
			ifx.setSec_CurRate(map.get(IfxStatics.IFX_SEC_CUR_RATE).trim());

		if (map.containsKey(IfxStatics.IFX_TRN_SEQ_CNTR))
			ifx.setSrc_TrnSeqCntr(map.get(IfxStatics.IFX_TRN_SEQ_CNTR));
		
		if (map.containsKey(IfxStatics.IFX_MY_TRN_SEQ_CNTR))
			ifx.setMy_TrnSeqCntr(map.get(IfxStatics.IFX_MY_TRN_SEQ_CNTR));

		if (map.containsKey(IfxStatics.IFX_TRN_DT)){
			Long trnDt = Util.longValueOf(map.get(IfxStatics.IFX_TRN_DT).trim());
			if (trnDt != null)
				ifx.setTrnDt(new DateTime(new Date(trnDt)));
		}

		if (map.containsKey(IfxStatics.IFX_TRK2_EQUIV_DATA))
			ifx.setTrk2EquivData(map.get(IfxStatics.IFX_TRK2_EQUIV_DATA).trim());
		
		if (map.containsKey(IfxStatics.IFX_MSG_AUTH_CODE))
			ifx.setMsgAuthCode(map.get(IfxStatics.IFX_MSG_AUTH_CODE).trim());

		try {
			if (map.containsKey(IfxStatics.IFX_EXPDT)){
				Long expDt = Util.longValueOf(map.get(IfxStatics.IFX_EXPDT).trim());
				if (expDt != null)
					ifx.setExpDt(expDt);
			}
		} catch (Exception e) {
		}

		if (map.containsKey(IfxStatics.IFX_CVV2))
			ifx.setCVV2(map.get(IfxStatics.IFX_CVV2));

		if (map.containsKey(IfxStatics.IFX_ACC_TYPE_TO)) {
			ifx.setAccTypeTo((AccType) ToAccType.get(Integer.parseInt(map
					.get(IfxStatics.IFX_ACC_TYPE_TO).trim())));
		}

		if (map.containsKey(IfxStatics.IFX_ACC_TYPE_FROM)) {
			ifx.setAccTypeFrom((AccType) ToAccType.get(Integer.parseInt(map
					.get(IfxStatics.IFX_ACC_TYPE_FROM).trim())));
		}
		
		if (map.containsKey(IfxStatics.IFX_USER_LANGUAGE)) {
			ifx.setUserLanguage((UserLanguage) ToUserLang.get(Integer.parseInt(map
					.get(IfxStatics.IFX_USER_LANGUAGE).trim())));
		}

		if (map.containsKey(IfxStatics.IFX_TERMINAL_TYPE))
			ifx.setTerminalType((TerminalType) ToTerminalType.get(Integer.parseInt(map
					.get(IfxStatics.IFX_TERMINAL_TYPE).trim())));

		

		if (map.containsKey(IfxStatics.IFX_BANK_ID))
			ifx.setBankId(map.get(IfxStatics.IFX_BANK_ID).trim());
		
		if (map.containsKey(IfxStatics.IFX_FWD_BANK_ID))
			ifx.setFwdBankId(map.get(IfxStatics.IFX_FWD_BANK_ID).trim());
		
		if (map.containsKey(IfxStatics.IFX_FWD_BANK_ID))
			ifx.setDestBankId(map.get(IfxStatics.IFX_FWD_BANK_ID).trim());
		
		if (map.containsKey(IfxStatics.IFX_RECV_BANK_ID))
			ifx.setRecvBankId(map.get(IfxStatics.IFX_RECV_BANK_ID).trim());

		if (map.containsKey(IfxStatics.IFX_NETWORKTRNINFO_NAME)) {
			String name = map.get(IfxStatics.IFX_NETWORKTRNINFO_NAME).trim(); 
//			new String(map.get(IfxStatics.IFX_NETWORKTRNINFO_NAME).getBytes("ISO-8859-1"), "UTF-8"); 
			if (name!= null)
				ifx.setName(convertor.decode(name.getBytes()));
		}

		
		/***** these properties set in copy fields *****/
/*		if (map.containsKey(IfxStatics.IFX_CITY)){
			String city = map.get(IfxStatics.IFX_CITY).trim();
	//			new String(map.get(IfxStatics.IFX_CITY).getBytes("ISO-8859-1"), "UTF-8");
			if (city != null)
				ifx.setCity(convertor.decode(city.getBytes()));
		}
		
		if (map.containsKey(IfxStatics.IFX_STATE_PROV))
			ifx.setStateProv(map.get(IfxStatics.IFX_STATE_PROV).trim());

		if (map.containsKey(IfxStatics.IFX_COUNTRY))
			ifx.setCountry(map.get(IfxStatics.IFX_COUNTRY).trim());
*/
		/*************************************************/
		
		if (map.containsKey(IfxStatics.IFX_NETWORK_REF_ID))
			ifx.setNetworkRefId(map.get(IfxStatics.IFX_NETWORK_REF_ID));
		
		if (map.containsKey(IfxStatics.IFX_TERMINAL_ID))
			ifx.setTerminalId(map.get(IfxStatics.IFX_TERMINAL_ID));

		if (map.containsKey(IfxStatics.IFX_CORE_BRANCH_CODE))
			ifx.setCoreBranchCode(map.get(IfxStatics.IFX_CORE_BRANCH_CODE));

		if (map.containsKey(IfxStatics.IFX_PIN_BLOCK))
			ifx.setPINBlock(map.get(IfxStatics.IFX_PIN_BLOCK).trim());

		if (map.containsKey(IfxStatics.IFX_STATUS_SEVERITY))
			ifx.setSeverity((Severity) ToSeverity.get(Integer.parseInt(map.get(IfxStatics.IFX_STATUS_SEVERITY).toString().trim())));

		if (map.containsKey(IfxStatics.IFX_STATUS_DESC))
			ifx.setStatusDesc(map.get(IfxStatics.IFX_STATUS_DESC));

		if (map.containsKey(IfxStatics.ERROR_CAUSE)){
			String error_cause = "CMS: "+ map.get(IfxStatics.ERROR_CAUSE);
			if (Util.hasText(ifx.getStatusDesc()))
				error_cause = ifx.getStatusDesc() + "\r\n"+ error_cause;
			ifx.setStatusDesc(error_cause);
			if (ifx.getSeverity()==null)
				ifx.setSeverity(Severity.INFO);
		}
			
		
		try {
			if (map.containsKey(IfxStatics.IFX_ORIG_DT))	
				ifx.setOrigDt(new DateTime(new Date(Long.valueOf(map.get(IfxStatics.IFX_ORIG_DT).trim()))));
		} catch (Exception e) {
			ISOException isoe = new ISOException("Unparsable Original Date.", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
	
		try {
			if (map.containsKey(IfxStatics.IFX_RECIEVED_DT)){	
				Long recievedDate = Util.longValueOf(map.get(IfxStatics.IFX_RECIEVED_DT).trim());
				if (recievedDate != null)
					ifx.setReceivedDt(new DateTime(new Date(recievedDate)));
			}
		} catch (Exception e) {
		}

		try {
			if (map.containsKey(IfxStatics.IFX_POSTED_DT)){	
				Long postedDate = Util.longValueOf(map.get(IfxStatics.IFX_POSTED_DT).trim());
				if (postedDate != null)
					ifx.setPostedDt(new MonthDayDate(new Date(postedDate)));
			}
		} catch (Exception e) {
		}

		try {
			if (map.containsKey(IfxStatics.IFX_SETTLE_DT)){	
				Long settleDate = Util.longValueOf(map.get(IfxStatics.IFX_SETTLE_DT).trim());
				if (settleDate != null)
					ifx.setSettleDt(new MonthDayDate( new Date(settleDate) ));
			}
		} catch (Exception e) {
		}

		if (map.containsKey(IfxStatics.IFX_APPROVAL_CODE))
			ifx.setApprovalCode(map.get(IfxStatics.IFX_APPROVAL_CODE));

		if (map.containsKey(IfxStatics.IFX_RS_CODE))
			ifx.setRsCode( ToErrorCode.get( map.get(IfxStatics.IFX_RS_CODE).trim()));
		
		if (map.containsKey(IfxStatics.IFX_DOCUMENT_NUMBER))	
			ifx.setDocumentNumber( map.get(IfxStatics.IFX_DOCUMENT_NUMBER).trim());

		if (map.containsKey(IfxStatics.IFX_ORIGINALDATAELEMENTS_MESSAGE_TYPE))
			ifx.getSafeOriginalDataElements().setMessageType(
					map.get(IfxStatics.IFX_ORIGINALDATAELEMENTS_MESSAGE_TYPE).trim());

		if (map.containsKey(IfxStatics.IFX_ORIGINALDATAELEMENTS_TRN_SEQ_CNTR)) {
			ifx.getSafeOriginalDataElements().setTrnSeqCounter(map.get(IfxStatics.IFX_ORIGINALDATAELEMENTS_TRN_SEQ_CNTR));
			if (map.containsKey(IfxStatics.IFX_TERMINAL_ID))
				ifx.getSafeOriginalDataElements().setTerminalId(map.get(IfxStatics.IFX_TERMINAL_ID).trim());
			if (map.containsKey(IfxStatics.IFX_APP_PAN))
				ifx.getSafeOriginalDataElements().setAppPAN(map.get(IfxStatics.IFX_APP_PAN).trim());
			
		}
		
		if (map.containsKey(IfxStatics.IFX_ORIGINALDATAELEMENTS_ORIG_DT)) {
				Long refOrigDt = Util.longValueOf(map.get(IfxStatics.IFX_ORIGINALDATAELEMENTS_ORIG_DT).trim());
				ifx.getSafeOriginalDataElements().setOrigDt(new DateTime(new Date(refOrigDt)));
		}
		
		if (map.containsKey(IfxStatics.IFX_ORIGINALDATAELEMENTS_BANK_ID))
			ifx.getSafeOriginalDataElements().setBankId(
					map.get(IfxStatics.IFX_ORIGINALDATAELEMENTS_BANK_ID).trim());
		if (map.containsKey(IfxStatics.IFX_ORIGINALDATAELEMENTS_FWD_BANK_ID))
			ifx.getSafeOriginalDataElements().setFwdBankId(
					map.get(IfxStatics.IFX_ORIGINALDATAELEMENTS_FWD_BANK_ID).trim());

		if (map.containsKey(IfxStatics.IFX_NEW_AMT_ACQ_CUR))
			ifx.setNew_AmtAcqCur(map.get(IfxStatics.IFX_NEW_AMT_ACQ_CUR).trim());
		
		if (map.containsKey(IfxStatics.IFX_NEW_AMT_ISS_CUR))
			ifx.setNew_AmtIssCur(map.get(IfxStatics.IFX_NEW_AMT_ISS_CUR).trim());
		
		if (map.containsKey(IfxStatics.IFX_ORG_ID_NUM))
			ifx.setOrgIdNum(map.get(IfxStatics.IFX_ORG_ID_NUM));

		
		if(ISOFinalMessageType.isTransferMessage(ifx.getIfxType())){
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_ACCT_TYPE))
				ifx.setAcctBalAvailableType((AccType) ToAcctType.get((Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALAVAILABLE_ACCT_TYPE).trim()))));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_BAL_TYPE))
				ifx.setAcctBalAvailableBalType((BalType) ToBalType.get((Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALAVAILABLE_BAL_TYPE).trim()))));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_AMT))
				ifx.setAcctBalAvailableAmt(map.get(IfxStatics.IFX_ACCTBALAVAILABLE_AMT).trim());
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_CUR_CODE))
				ifx.setAcctBalAvailableCurCode(map.get(IfxStatics.IFX_ACCTBALAVAILABLE_CUR_CODE).trim());
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_ACCT_TYPE))
				ifx.setAcctBalLedgerType((AccType) ToAcctType.get(Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALLEDGER_ACCT_TYPE).trim())));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_BAL_TYPE))
				ifx.setAcctBalLedgerBalType((BalType) ToBalType.get((Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALLEDGER_BAL_TYPE).trim()))));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_AMT))
				ifx.setAcctBalLedgerAmt(map.get(IfxStatics.IFX_ACCTBALLEDGER_AMT).trim());
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_CUR_CODE))
				ifx.setAcctBalLedgerCurCode(map.get(IfxStatics.IFX_ACCTBALLEDGER_CUR_CODE).trim());
		}else if(!ISOFinalMessageType.isTransferMessage(ifx.getIfxType())){
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_ACCT_TYPE))
				ifx.setTransientAcctBalAvailableType((AccType) ToAcctType.get((Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALAVAILABLE_ACCT_TYPE).trim()))));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_BAL_TYPE))
				ifx.setTransientAcctBalAvailableBalType((BalType) ToBalType.get((Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALAVAILABLE_BAL_TYPE).trim()))));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_AMT))
				ifx.setTransientAcctBalAvailableAmt(map.get(IfxStatics.IFX_ACCTBALAVAILABLE_AMT).trim());
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALAVAILABLE_CUR_CODE))
				ifx.setTransientAcctBalAvailableCurCode(map.get(IfxStatics.IFX_ACCTBALAVAILABLE_CUR_CODE).trim());
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_ACCT_TYPE))
				ifx.setTransientAcctBalLedgerType((AccType) ToAcctType.get(Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALLEDGER_ACCT_TYPE).trim())));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_BAL_TYPE))
				ifx.setTransientAcctBalLedgerBalType((BalType) ToBalType.get((Integer.parseInt(map
						.get(IfxStatics.IFX_ACCTBALLEDGER_BAL_TYPE).trim()))));
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_AMT))
				ifx.setTransientAcctBalLedgerAmt(map.get(IfxStatics.IFX_ACCTBALLEDGER_AMT).trim());
			
			if (map.containsKey(IfxStatics.IFX_ACCTBALLEDGER_CUR_CODE))
				ifx.setTransientAcctBalLedgerCurCode(map.get(IfxStatics.IFX_ACCTBALLEDGER_CUR_CODE).trim());
		}

		if (map.containsKey(IfxStatics.IFX_NEW_PIN_BLOCK))
			ifx.setNewPINBlock(map.get(IfxStatics.IFX_NEW_PIN_BLOCK).trim());
		
		if (map.containsKey(IfxStatics.IFX_OLD_PIN_BLOCK))
			ifx.setOldPINBlock(map.get(IfxStatics.IFX_OLD_PIN_BLOCK).trim());
		
		
		if (map.containsKey(IfxStatics.IFX_SUBSIDIARY_ACC_TO))
			ifx.setSubsidiaryAccTo(map.get(IfxStatics.IFX_SUBSIDIARY_ACC_TO).trim());
		
		if (map.containsKey(IfxStatics.IFX_SUBSIDIARY_ACC_FROM))
			ifx.setSubsidiaryAccFrom(map.get(IfxStatics.IFX_SUBSIDIARY_ACC_FROM).trim());
		
//		if (map.containsKey(IfxStatics.IFX_STATEMENT_NUMBER))
//			ifx.setStatementNumber(map.get(IfxStatics.IFX_STATEMENT_NUMBER).trim());
//		
//		if (map.containsKey(IfxStatics.IFX_STATEMENT_AMOUNT))
//			ifx.setStatementAmount(Util.longValueOf(map.get(IfxStatics.IFX_STATEMENT_AMOUNT).trim()));
		
		/*****************Card Credit Data************/
		if (map.containsKey(IfxStatics.IFX_CREDITCARDDATA_TRANSACTION_AMOUNT))
			ifx.setCreditTotalTransactionAmount(Util.longValueOf(map.get(IfxStatics.IFX_CREDITCARDDATA_TRANSACTION_AMOUNT).trim()));
		
		if (map.containsKey(IfxStatics.IFX_CREDITCARDDATA_FEE_AMOUNT))
			ifx.setCreditTotalFeeAmount(Util.longValueOf(map.get(IfxStatics.IFX_CREDITCARDDATA_FEE_AMOUNT).trim()));
		
		if (map.containsKey(IfxStatics.IFX_CREDITCARDDATA_INTEREST))
			ifx.setCreditInterest(Util.longValueOf(map.get(IfxStatics.IFX_CREDITCARDDATA_INTEREST).trim()));
		
		if (map.containsKey(IfxStatics.IFX_CREDITCARDDATA_AMOUNT))
			ifx.setCreditStatementAmount(Util.longValueOf(map.get(IfxStatics.IFX_CREDITCARDDATA_AMOUNT).trim()));
		
		if (map.containsKey(IfxStatics.IFX_CREDITCARDDATA_OPEN_TO_BUY))
			ifx.setCreditOpenToBuy(Util.longValueOf(map.get(IfxStatics.IFX_CREDITCARDDATA_OPEN_TO_BUY).trim()));
		/*****************Card Credit Data************/
		
		if (map.containsKey(IfxStatics.IFX_P48)) {
			mapField48(ifx, (map.get(IfxStatics.IFX_P48).trim()));
		}

		if (map.containsKey(IfxStatics.IFX_BANK_STATEMENT_DATA)) {
			mapBankStatementData(ifx, (map.get(IfxStatics.IFX_BANK_STATEMENT_DATA).trim()));
		}
		
		if (map.containsKey(IfxStatics.IFX_SUBSIDIARY_DATA)) {
			mapSubsidiaryData(ifx, (map.get(IfxStatics.IFX_SUBSIDIARY_DATA).trim()));
		}
		
		if (map.containsKey(IfxStatics.IFX_FIRST_TRANSACTION_ID))
			ifx.setFirstTrxId(Util.longValueOf(map.get(IfxStatics.IFX_FIRST_TRANSACTION_ID)));
		return ifx;
	}

	public static void mapSubsidiaryData(Ifx ifx, String accountData) {
		if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType())){
    		List<CardAccountInformation> list = parseAccountData(ifx.getEMVRsData(), accountData);
    		ifx.setCardAccountInformation(list);
    	}
	}
		
	public static void mapBankStatementData(Ifx ifx, String statementData) {
		if (ISOFinalMessageType.isBankStatementMessage(ifx.getIfxType())){
			List<BankStatementData> list2 = parseBankStatement(ifx.getEMVRsData(), statementData);
			ifx.setBankStatementData(list2);
//			List<BankStatementData> list = parseBankStatement(ifx.getEMVRsData(), statementData);
//			ifx.setBankStatementData(list);
			
		}
	}

	@Override
	public String mapError(String rsCode) {
		return null;
	}

	public void mapField48(Ifx ifx, String f_48) {
    	if(ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())) {
    		String billID = f_48.substring(18, 18+13);
    		String paymentID = f_48.substring(18+13);
    		ifx.setBillID(billID);
    		ifx.setBillPaymentID(paymentID);
    		ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billID));
    		ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billID));
    		ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billID));
    	}
	}

	private static List<CardAccountInformation> parseAccountData(EMVRsData rsData, String accountData) {
		List<CardAccountInformation> result = new ArrayList<CardAccountInformation>();
		
		int offset = 0;
		int index = 1;
		while (Util.hasText(accountData) && offset < accountData.length()) {
			Integer length = Integer.parseInt(accountData.substring(offset, offset + 2));
			String accNum = accountData.substring(offset + 2, offset + 2 + length);
			
			CardAccountInformation data = new CardAccountInformation();
			data.setAccountNumber(accNum);
			data.setLength(length);
			data.setIndex(index+"");
			index++;
			data.setEmvRsData(rsData);
			result.add(data);
			
			offset  += (length + 2);
		}
		
		return result;
	}
	private static List<BankStatementData> parseBankStatement(EMVRsData rsData, String f_48){
		List<BankStatementData> result = new ArrayList<BankStatementData>();
		StringTokenizer tokenizer;
		StringTokenizer timeTokenizer;
		StringTokenizer dateTokenizer;
		StringTokenizer lineTokenizer;
		lineTokenizer = new StringTokenizer(f_48, "!");
		
		
		while(lineTokenizer.hasMoreTokens()){
			String ans=lineTokenizer.nextToken().trim();
			tokenizer = new StringTokenizer(ans, "|");
			String row = (tokenizer.nextToken().trim());
			
			String dateStr = tokenizer.nextToken().trim();
			dateTokenizer = new StringTokenizer(dateStr, "/");
			int year = Integer.parseInt(dateTokenizer.nextToken().trim());
			int month = Integer.parseInt(dateTokenizer.nextToken().trim());
			int day = Integer.parseInt(dateTokenizer.nextToken().trim());		
			
			String timeStr = tokenizer.nextToken().trim();
			timeTokenizer = new StringTokenizer(timeStr, "-");
			int hour = Integer.parseInt(timeTokenizer.nextToken().trim());
			int minute = Integer.parseInt(timeTokenizer.nextToken().trim());
			
			DateTime persianDateTime = new DateTime(new DayDate(year, month, day), new DayTime(hour, minute));
			
			String trxtyp =tokenizer.nextToken().trim();
			
			Long amount = Util.longValueOf(tokenizer.nextToken().trim());
			
			Long balance = Util.longValueOf(tokenizer.nextToken().trim());
	
			BankStatementData data = new BankStatementData();
			data.setTrxDt(PersianCalendar.toGregorian(persianDateTime));
			data.setTrnType(trxtyp);
			data.setAmount(amount);
			data.setBalance(balance);
			data.setEmvRsData(rsData);
			result.add(data);
		}
		return result;
		
	}
//	private List<BankStatementData> parseBankStatement(EMVRsData rsData, String f_48) {
//	
//	List<BankStatementData> result = new ArrayList<BankStatementData>();
//	Parse parse = new Parse();
//	String bankStatementSchema = ConfigUtil.getProperty(ConfigUtil.GLOBAL_PATH_SCHEMA_BANKSTATEMENT);
//	Document document = parse.parse(f_48, new File(bankStatementSchema));
//	
//	if (document != null) {
//		Element root = document.getRootElement();
//		for (Iterator rowItr = root.elementIterator("Row"); rowItr.hasNext(); ) {
//			Element row = (Element) rowItr.next();
//			
//			int year = Integer.parseInt(row.element("Date").attributeValue("Year"));
//			int month = Integer.parseInt(row.element("Date").attributeValue("Mounth"));
//			int day = Integer.parseInt(row.element("Date").attributeValue("Day"));
//			
//			int hour = Integer.parseInt(row.element("Time").attributeValue("Hour"));
//			int minute = Integer.parseInt(row.element("Time").attributeValue("Minute"));
//			
//			DateTime persianDateTime = new DateTime(new DayDate(year, month, day), new DayTime(hour, minute));
//			String trnType = row.elementText("TransactionType").trim();
//			Long amount = Util.longValueOf(row.elementText("Amount").trim());
//			Long balance = Util.longValueOf(row.elementText("Balance").trim());
//			BankStatementData data = new BankStatementData();
//			data.setAmount(amount);
//			data.setBalance(balance);
//			data.setTrnType(trnType);
//			data.setEmvRsData(rsData);
//			data.setTrxDt(PersianCalendar.toGregorian(persianDateTime));
//			result.add(data);
//		}
//	}
//	return result;
//}

//	@Override
//	public EncodingConvertor getEncodingConvertor() {
//		return GlobalContext.getInstance().getConvertor("UTF8_CONVERTOR");
//	}
	
}
