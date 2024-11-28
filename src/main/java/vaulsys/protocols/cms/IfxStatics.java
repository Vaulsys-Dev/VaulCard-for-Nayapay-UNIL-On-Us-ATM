package vaulsys.protocols.cms;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Feb 4, 2008
 * Time: 4:41:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class IfxStatics {
    public static final String COLLECTION_STREAM_INDICATOR="Collection";
    public static final String COLLECTION_STREAM_SEPARATOR=".";

    public static final String IFX_EXPDT = "msgRqHdr_EMVRqData_ExpDt";
    public static final String IFX_CVV2 = "msgRqHdr_EMVRqData_CVV2";
    public static final String IFX_ACC_TYPE_FROM = "msgRqHdr_EMVRqData_AccTypeFrom";
    public static final String IFX_ACC_TYPE_TO = "msgRqHdr_EMVRqData_AccTypeTo";
    
    public static final String IFX_IFX_TYPE = "ifxType";
    public static final String IFX_TRN_TYPE = "trnType";
    public static final String IFX_APP_PAN = "msgRqHdr_EMVRqData_AppPAN";
    public static final String IFX_EMV_TRN_TYPE = "msgRqHdr_EMVRqData_EMVTrnType";
    public static final String IFX_AUTH_AMT = "msgRqHdr_EMVRqData_Auth_Amt";
    public static final String IFX_AUTH_CUR_CODE = "msgRqHdr_EMVRqData_Auth_CurCode";
    public static final String IFX_AUTH_CUR_RATE = "msgRqHdr_EMVRqData_Auth_CurRate";
    public static final String IFX_SEC_AMT = "msgRqHdr_EMVRqData_Sec_Amt";
    public static final String IFX_SEC_CUR_CODE = "msgRqHdr_EMVRqData_Sec_CurCode";
    public static final String IFX_SEC_CUR_RATE = "msgRqHdr_EMVRqData_Sec_CurRate";
    public static final String IFX_TRN_SEQ_CNTR = "msgRqHdr_EMVRqData_TrnSeqCntr";
    public static final String IFX_MY_TRN_SEQ_CNTR = "msgRqHdr_EMVRqData_My_TrnSeqCntr";
    public static final String IFX_TRN_DT = "msgRqHdr_EMVRqData_TrnDt";
    public static final String IFX_TRK2_EQUIV_DATA = "msgRqHdr_EMVRqData_Trk2EquivData";
    public static final String IFX_MSG_AUTH_CODE = "msgRqHdr_EMVRqData_MsgAuthCode";
    public static final String IFX_TERMINAL_TYPE = "msgRqHdr_NetworkTrnInfo_TerminalType";
    public static final String IFX_BANK_ID = "msgRqHdr_NetworkTrnInfo_BankId";
    public static final String IFX_FWD_BANK_ID = "msgRqHdr_NetworkTrnInfo_FwdBankId";
    public static final String IFX_NETWORKTRNINFO_NAME = "msgRqHdr_NetworkTrnInfo_Name";
    public static final String IFX_CITY = "msgRqHdr_NetworkTrnInfo_City";
    public static final String IFX_STATE_PROV = "msgRqHdr_NetworkTrnInfo_StateProv";
    public static final String IFX_NETWORK_REF_ID = "msgRqHdr_NetworkTrnInfo_NetworkRefId";
    public static final String IFX_COUNTRY = "msgRqHdr_NetworkTrnInfo_Country";
    public static final String IFX_TERMINAL_ID = "msgRqHdr_NetworkTrnInfo_TerminalId";
    public static final String IFX_PIN_BLOCK = "signonRq_PINBlock";
    public static final String IFX_NEW_PIN_BLOCK = "signonRq_NewPINBlock";
    public static final String IFX_OLD_PIN_BLOCK = "signonRq_OldPINBlock";
    public static final String IFX_STATUS_CODE = "status_StatusCode";
    public static final String IFX_STATUS_SEVERITY = "status_Severity";
    public static final String IFX_STATUS_DESC = "status_StatusDesc";
    public static final String IFX_FIELD_STATUS_ADDITIONALSTATUS_STATUS_CODE = "status_AdditionalStatus_StatusCode";
    public static final String IFX_FIELD_STATUS_ADDITIONALSTATUS_SEVERITY = "status_AdditionalStatus_Severity";
    public static final String IFX_FIELD_STATUS_ADDITIONALSTATUS_STATUS_DESC = "status_AdditionalStatus_StatusDesc";
    public static final String IFX_ORIG_DT = "origDt";
    public static final String IFX_POSTED_DT = "postedDt";
    public static final String IFX_SETTLE_DT = "settleDt";
    public static final String IFX_APPROVAL_CODE = "approvalCode";
    public static final String IFX_RS_CODE = "rsCode";
    public static final String IFX_DOCUMENT_NUMBER = "documentNumber";
    public static final String IFX_ORIGINALDATAELEMENTS_MESSAGE_TYPE = "originalDataElements_MessageType";
    public static final String IFX_ORIGINALDATAELEMENTS_TRN_SEQ_CNTR = "originalDataElements_TrnSeqCntr";
    public static final String IFX_ORIGINALDATAELEMENTS_ORIG_DT = "originalDataElements_OrigDt";
    public static final String IFX_ORIGINALDATAELEMENTS_BANK_ID = "originalDataElements_BankId";
    public static final String IFX_ORIGINALDATAELEMENTS_FWD_BANK_ID = "originalDataElements_FwdBankId";
    public static final String IFX_NEW_AMT_ACQ_CUR = "new_AmtAcqCur";
    public static final String IFX_NEW_AMT_ISS_CUR = "new_AmtIssCur";
    public static final String IFX_ORG_ID_NUM = "orgRec_OrgId_OrgIdNum";
    public static final String IFX_ORG_ID_TYPE = "orgRec_OrgId_OrgIdType";
    public static final String IFX_ACCTBALAVAILABLE_ACCT_TYPE = "acctBalAvailable_AcctType";
    public static final String IFX_ACCTBALAVAILABLE_BAL_TYPE = "acctBalAvailable_BalType";
    public static final String IFX_ACCTBALAVAILABLE_AMT = "acctBalAvailable_Amt";
    public static final String IFX_ACCTBALAVAILABLE_CUR_CODE = "acctBalAvailable_CurCode";
    public static final String IFX_ACCTBALLEDGER_ACCT_TYPE = "acctBalLedger_AcctType";
    public static final String IFX_ACCTBALLEDGER_BAL_TYPE = "acctBalLedger_BalType";
    public static final String IFX_ACCTBALLEDGER_AMT = "acctBalLedger_Amt";
    public static final String IFX_ACCTBALLEDGER_CUR_CODE = "acctBalLedger_CurCode";
    public static final String IFX_RECIEVED_DT = "recievedDt";
    public static final String IFX_CORE_BRANCH_CODE = "atmSpecificData_coreBranchCode";
    public static final String IFX_P48 = "ExtISO_P48";
    
    public static final String IFX_SECONDARY_APP_PAN = "msgRqHdr_EMVRqData_SecondaryAppPAN";
    public static final String IFX_CARD_HOLDER_NAME = "msgRqHdr_EMVRqData_CardHolderName";
    public static final String IFX_CARD_HOLDER_FAMILY = "msgRqHdr_EMVRqData_CardHolderFamily";
    public static final String IFX_MAIN_ACCOUNT_NUMBER = "msgRqHdr_EMVRqData_MainAccountNumber";
    public static final String IFX_RECV_BANK_ID = "msgRqHdr_NetworkTrnInfo_RecvBankId";
    
    public static final String IFX_BANK_STATEMENT_DATA = "statement";
    public static final String IFX_SUBSIDIARY_DATA = "subsidiary"; // subsidiary accounts of card

    public static final String IFX_CREDITCARDDATA_TRANSACTION_AMOUNT = "EMVRsData_CreditCardData_TotalTransactionAmount";
    public static final String IFX_CREDITCARDDATA_FEE_AMOUNT = "EMVRsData_CreditCardData_TotalFeeAmount";
    public static final String IFX_CREDITCARDDATA_INTEREST = "EMVRsData_CreditCardData_Interest";
    public static final String IFX_CREDITCARDDATA_AMOUNT = "EMVRsData_CreditCardData_StatementAmount";
    public static final String IFX_CREDITCARDDATA_OPEN_TO_BUY = "EMVRsData_CreditCardData_OpenToBuy";
    
//    public static final String IFX_STATEMENT_NUMBER = "msgRqHdr_EMVRqData_StatementNumber";
//    public static final String IFX_STATEMENT_AMOUNT = "msgRqHdr_EMVRqData_StatementAmount";
	public static final String IFX_USER_LANGUAGE = "msgRqHdr_NetworkTrnInfo_UserLang";
	
	public static final String IFX_SUBSIDIARY_ACC_TO = "subsidiary_Acc_To";
	public static final String IFX_SUBSIDIARY_ACC_FROM = "subsidiary_Acc_From";
	public static final String IFX_TOTAL_FEE_AMT = "totalFeeAmt";
	public static final String ERROR_CAUSE  = "errMsg";
	
	public static final String ACTUAL_APP_PAN  = "actualAppPAN";
	public static final String ACTUAL_SECONDARY_APP_PAN = "actualSecondaryAppPAN";
	
	public static final String IFX_STATEMENT_ACC_NUMBER="statementAccountNumber";

	public static final String IFX_BILL_ID = "billId";
	public static final String IFX_BILL_PAYMENT_ID = "billPaymentId";
	public static final String IFX_BILL_TYPE = "billType";
	public static final String IFX_BILL_TYPE_NAME = "billTypeName";
	
	public static final String IFX_CARD_HOLDER_MOBILE_NO = "cardHolderMobileNo";
	
	public static final String IFX_FIRST_TRANSACTION_ID = "trx";
}
