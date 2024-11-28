package vaulsys.protocols.epay.base;

import java.text.DecimalFormat;
import java.util.List;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.CardAccountInformation;
import vaulsys.util.Util;

public class EpayMsg implements Cloneable, ProtocolMessage{
	public String xml;
	
	//general data	
	public Integer commandID;
	public String referenceNumber;
	public String seqCounter;
	public String cardNumber;
	public String pin2;
	public String cvv2;
	public Integer expireYear;
	public Integer expireMonth;
	
	public String pin1;
	public String trk2;
	
	public Long amount;
	public String transactionDate;
	public String IP;
	public String eMail;
	public Integer terminalType;
	public Integer origTerminalType;
	
	//bill payment message data
	public String billIdentificationNum;
	public String paymentIdentificationNum;

	//purchase & refund message data 
	public Long merchantCode;
	public Long terminalCode;
	public String invoiceNumber;
	public String invoiceDate; 

	//reversal message data
	public String originalTransactionDate;
	public String originalReferenceNumber;
	public String originalSeqCounter;
	public String originalInvoiceNumber;
	
	public Byte pinType; //1:2
	public String oldPin;
	public String newPin;
	
	public String destinationCardNum;
	public Long payAmount;
	public String language; //fa-en
	
	//response message general data
	public Integer Result;
	public String Message;
	
	//purchase charge response message data
	public String CardPin;
	public String CardSerial;
	public String CardAmount;
	public String CardRealAmount;
	
	public String Name;
//	public String Bank;
	
	public String CurrentAmount;
	public String AvailableAmount;
	public String CurrentAmountCurrency;
	public String AvailableAmountCurrency;
	public AccType AccountType;
	
	public List<BankStatementData> statementDataList;
	
	//purchase topup
	public Long phoneNumber;
	public Long company;
	
	//thirdParty purchase
	public String companyName;
	
	public String onlineBillRef;
	public String Description;
	public String OnlineBillStatus;
	
	public String cardHolderMobileNo;
	
	public String transferFromDesc;
	public String transferToDesc;
	
	//TASK Task049 : Epay TransferToCard From subsidiary Account
	public String subAccFrom;
	
	
	
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	private static final DecimalFormat df = new DecimalFormat("#");
	private static final String STR_NEW_LINE = "\r\n";
	private static final String STR_MSG_START = "<command>";
	private static final String STR_MSG_END = "</command>";
	private static final String STR_CMD_ID = "id=";
	private static final String STR_REF_NUM = "refNum=";
	private static final String STR_SEQ_CNT = "seqCnt=";
	private static final String STR_PAN = "pan=";
	private static final String STR_AMT = "amt=";
	private static final String STR_TRX_DT = "trxDt=";
	private static final String STR_IP = "ip=";
	private static final String STR_EMAIL = "eMail=";
	private static final String STR_TRM_TYP = "trmTyp=";
	private static final String STR_ORIG_TRM_TYP = "orgTrmTyp=";
	private static final String STR_BIL_ID = "bilId=";
	private static final String STR_PAY_ID = "payId=";
	private static final String STR_MERCH_CODE = "merch=";
	private static final String STR_TRM_CODE = "term=";
	private static final String STR_INV_NUM = "invNum=";
	private static final String STR_INV_DT = "invDt=";
	private static final String STR_ORIG_TRX_DT = "origTrxDt=";
	private static final String STR_ORIG_REF_NUM = "origRefNum=";
	private static final String STR_ORIG_SEQ_CNT = "origSeqCnt=";
	private static final String STR_ORIG_INV_NUM = "origInvNum=";
	private static final String STR_PIN_TYP = "pinTyp=";
	private static final String STR_DEST_PAN = "destPAN=";
	private static final String STR_PAY_AMT = "payAmt=";
	private static final String STR_LANG = "lang=";
	private static final String STR_RS = "res=";
	private static final String STR_MSG = "msg=";
	private static final String STR_CRD_SER = "crdSer=";
	private static final String STR_CRD_AMT = "crdAmt=";
	private static final String STR_CRD_R_AMT = "crdRAmt=";
	private static final String STR_CRD_NAME = "nm=";
	private static final String STR_CUR_AMT = "curAmt=";
	private static final String STR_AVI_AMT = "aviAmt=";
	private static final String STR_CUR_AMT_CUR = "curAmtCur=";
	private static final String STR_AVI_AMT_CUR = "aviAmtCur=";
	private static final String STR_ACC_TYP = "accTyp=";
	private static final String STR_PH_NUM = "phNum=";
	private static final String STR_COMP = "comp=";
	private static final String STR_COMP_NAME = "compNam=";
	private static final String STR_BIL_REF = "bilRef=";
	private static final String STR_DESC = "desc=";
	private static final String STR_BIL_ST = "bilSt=";
	private static final String STR_MOB_NO = "mobNo=";
	private static final String STR_TRF_DESC = "trfDesc=";
	private static final String STR_TRT_DESC = "trtDesc=";
	//TASK Task049 : Epay TransferToCard From subsidiary Account	
	private static final String STR_TRF_SUBACC_FROM = "trfSubAccFrom=";

	@Override
	public String toString() {
        StringBuilder str = new StringBuilder("");
        str.append(STR_MSG_START).append(STR_NEW_LINE);
        if (commandID != null) {
            str.append(STR_CMD_ID).append(commandID).append(STR_NEW_LINE);
        }
        if (Util.hasText(referenceNumber)) {
        	str.append(STR_REF_NUM).append(referenceNumber).append(STR_NEW_LINE);
        }
        if (Util.hasText(seqCounter)) {
        	str.append(STR_SEQ_CNT).append(seqCounter).append(STR_NEW_LINE);
        }
        if (Util.hasText(cardNumber)) {
        	str.append(STR_PAN).append(cardNumber).append(STR_NEW_LINE);
        }
        if (amount != null) {
        	str.append(STR_AMT).append(df.format(amount)).append(STR_NEW_LINE);
        }
        if (Util.hasText(transactionDate)) {
        	str.append(STR_TRX_DT).append(transactionDate).append(STR_NEW_LINE);
        }
        if (Util.hasText(IP)) {
        	str.append(STR_IP).append(IP).append(STR_NEW_LINE);
        }
        if (Util.hasText(eMail)) {
        	str.append(STR_EMAIL).append(eMail).append(STR_NEW_LINE);
        }
        if (terminalType != null) {
        	str.append(STR_TRM_TYP).append(terminalType).append(STR_NEW_LINE);
        }
        if (origTerminalType != null) {
        	str.append(STR_ORIG_TRM_TYP).append(origTerminalType).append(STR_NEW_LINE);
        }
        if (Util.hasText(billIdentificationNum)) {
        	str.append(STR_BIL_ID).append(billIdentificationNum).append(STR_NEW_LINE);
        }
        if (Util.hasText(paymentIdentificationNum)) {
        	str.append(STR_PAY_ID).append(paymentIdentificationNum).append(STR_NEW_LINE);
        }
        if (merchantCode != null) {
        	str.append(STR_MERCH_CODE).append(df.format(merchantCode)).append(STR_NEW_LINE);
        }
        if (terminalCode != null) {
        	str.append(STR_TRM_CODE).append(df.format(terminalCode)).append(STR_NEW_LINE);
        }
        if (Util.hasText(invoiceNumber)) {
        	str.append(STR_INV_NUM).append(invoiceNumber).append(STR_NEW_LINE);
        }
        if (Util.hasText(invoiceDate)) {
        	str.append(STR_INV_DT).append(invoiceDate).append(STR_NEW_LINE);
        }
        if (Util.hasText(originalTransactionDate)) {
        	str.append(STR_ORIG_TRX_DT).append(originalTransactionDate).append(STR_NEW_LINE);
        }
        if (Util.hasText(originalReferenceNumber)) {
        	str.append(STR_ORIG_REF_NUM).append(originalReferenceNumber).append(STR_NEW_LINE);
        }
        if (Util.hasText(originalSeqCounter)) {
        	str.append(STR_ORIG_SEQ_CNT).append(originalSeqCounter).append(STR_NEW_LINE);
        }
        if (Util.hasText(originalInvoiceNumber)) {
        	str.append(STR_ORIG_INV_NUM).append(originalInvoiceNumber).append(STR_NEW_LINE);
        }
        if (pinType != null) {
        	str.append(STR_PIN_TYP).append(df.format(pinType)).append(STR_NEW_LINE);
        }
        if (Util.hasText(destinationCardNum)) {
        	str.append(STR_DEST_PAN).append(destinationCardNum).append(STR_NEW_LINE);
        }
        if (payAmount != null) {
        	str.append(STR_PAY_AMT).append(df.format(payAmount)).append(STR_NEW_LINE);
        }
        if (Util.hasText(language)) {
        	str.append(STR_LANG).append(language).append(STR_NEW_LINE);
        }
        if (Result != null) {
        	str.append(STR_RS).append(Result).append(STR_NEW_LINE);
        }
        if (Util.hasText(Message)) {
        	str.append(STR_MSG).append(Message).append(STR_NEW_LINE);
        }
        if (Util.hasText(CardSerial)) {
        	str.append(STR_CRD_SER).append(CardSerial).append(STR_NEW_LINE);
        }
        if (Util.hasText(CardAmount)) {
        	str.append(STR_CRD_AMT).append(CardAmount).append(STR_NEW_LINE);
        }
        if (Util.hasText(CardRealAmount)) {
        	str.append(STR_CRD_R_AMT).append(CardRealAmount).append(STR_NEW_LINE);
        }
        if (Util.hasText(Name)) {
        	str.append(STR_CRD_NAME).append(Name).append(STR_NEW_LINE);
        }
        if (Util.hasText(CurrentAmount)) {
        	str.append(STR_CUR_AMT).append(CurrentAmount).append(STR_NEW_LINE);
        }
        if (Util.hasText(AvailableAmount)) {
        	str.append(STR_AVI_AMT).append(AvailableAmount).append(STR_NEW_LINE);
        }
        if (Util.hasText(CurrentAmountCurrency)) {
        	str.append(STR_CUR_AMT_CUR).append(CurrentAmountCurrency).append(STR_NEW_LINE);
        }
        if (Util.hasText(AvailableAmountCurrency)) {
        	str.append(STR_AVI_AMT_CUR).append(AvailableAmountCurrency).append(STR_NEW_LINE);
        }
        if (AccountType != null) {
        	str.append(STR_ACC_TYP).append(AccountType.getType()).append(STR_NEW_LINE);
        }
        if (phoneNumber != null) {
        	str.append(STR_PH_NUM).append(df.format(phoneNumber)).append(STR_NEW_LINE);
        }
        if (company != null) {
        	str.append(STR_COMP).append(df.format(company)).append(STR_NEW_LINE);
        }
        if (Util.hasText(companyName)) {
        	str.append(STR_COMP_NAME).append(companyName).append(STR_NEW_LINE);
        }
        if (Util.hasText(onlineBillRef)) {
        	str.append(STR_BIL_REF).append(onlineBillRef).append(STR_NEW_LINE);
        }
        if (Util.hasText(Description)) {
        	str.append(STR_DESC).append(Description).append(STR_NEW_LINE);
        }
        if (Util.hasText(OnlineBillStatus)) {
        	str.append(STR_BIL_ST).append(OnlineBillStatus).append(STR_NEW_LINE);
        }
        if (Util.hasText(cardHolderMobileNo)) {
        	str.append(STR_MOB_NO).append(cardHolderMobileNo).append(STR_NEW_LINE);
        }
        if (Util.hasText(transferFromDesc)) {
        	str.append(STR_TRF_DESC).append(transferFromDesc).append(STR_NEW_LINE);
        }
        if (Util.hasText(transferToDesc)) {
        	str.append(STR_TRT_DESC).append(transferToDesc).append(STR_NEW_LINE);
        } 
        //TASK Task049 : Epay TransferToCard From subsidiary Account 
        if (Util.hasText(subAccFrom)) {
        	str.append(STR_TRF_SUBACC_FROM).append(subAccFrom).append(STR_NEW_LINE);
        }         
      
        str.append(STR_MSG_END);
        
        return str.toString();
	}

	@Override
	public Boolean isRequest() throws Exception {
		if(EpayConstants.isFinancialMsg(this) || EpayConstants.isReversalMsg(this))
			return true;
		return null;
	}
}
