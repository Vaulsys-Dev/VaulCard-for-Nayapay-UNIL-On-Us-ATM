package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.apacs70.encoding.Apacs70FarsiConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;

public class Apacs70Utils {
	private static Logger logger = Logger.getLogger(Apacs70Utils.class);

	public static void Prefixer(StringBuilder strBuilder, String str, int len) {
//		if (!Util.hasText(str))
//			return "";
		if (str.length() > len) {
			System.out.println("Error in Prefixer: " + str + " must have" + len + " length");
//			return str;
			strBuilder.append(str);
		}
		
//		for (int i = str.length(); i < len; i++)
//			str = "0" + str;
//		StringBuilder strBuilder = new StringBuilder();
		for (int i = str.length(); i < len; i++)
			strBuilder.append("0");
		strBuilder.append(str);
		
//		return strBuilder.toString();
	}
	
	public static void Prefixer(StringBuilder strBuilder, String str, int len, char filled) {
//		if (!Util.hasText(str))
//			return "";
		if (str.length() > len) {
			System.out.println("Error in Prefixer: " + str + " must have" + len + " length");
//			return str;
			strBuilder.append(str);
		}
//		for (int i = str.length(); i < len; i++)
//			str = filled + str;
//		return str;
//		StringBuilder strBuilder = new StringBuilder();
		for (int i = str.length(); i < len; i++)
			strBuilder.append(filled);
		strBuilder.append(str);
		
//		return strBuilder.toString();
	}

	
	public static String Prefixer(String str, int len) {
		if (!Util.hasText(str))
			return "";
		if (str.length() > len) {
			System.out.println("Error in Prefixer: " + str + " must have" + len + " length");
			return str;
		}
		
//		for (int i = str.length(); i < len; i++)
//			str = "0" + str;
		StringBuilder strBuilder = new StringBuilder();
		for (int i = str.length(); i < len; i++)
			strBuilder.append("0");
		strBuilder.append(str);
		
		return strBuilder.toString();
	}
	
	public static String Prefixer(String str, int len, char filled) {
		if (!Util.hasText(str))
			return "";
		if (str.length() > len) {
			System.out.println("Error in Prefixer: " + str + " must have" + len + " length");
			return str;
		}
//		for (int i = str.length(); i < len; i++)
//			str = filled + str;
//		return str;
		StringBuilder strBuilder = new StringBuilder();
		for (int i = str.length(); i < len; i++)
			strBuilder.append(filled);
		strBuilder.append(str);
		
		return strBuilder.toString();
	}
	
	public static String convertNull(String str){
		return str != null ? str : "";
	}
	
	public static String convertZero(int num){
		if (num == 0)
			return "";
		return String.valueOf(num);
	}

	public static String indentToString(Apacs70Component cmp, int indntNo) {
		String indent = "";
		for(int i=0; i<indntNo; i++)
			indent += "\t";
		if(cmp == null)
			return indent + "-";
		else
			return "\r\n" + indent + cmp.toString().replace("\n", "\n" + indent);
	}
	
	public static String convertAcctBalAmt(String amt) {
		if (Util.hasText(amt)) {
			if (amt.length() >= 1) {
				if (amt.charAt(0)=='D' || amt.charAt(0)=='d')
					return "-" + amt.substring(1);
				else if (amt.charAt(0)=='C' || amt.charAt(0)=='c')
					return "+" + amt.substring(1);
			}
		}
		return "";
	}

	public static Object safeToString(Object obj) {
		return obj != null ? obj : "-";		
	}

	public static String mapError(String rsCode) {
		if (ISOResponseCodes.RESTRICTED_MIN_WITHDRAWAL_AMOUNT.equals(rsCode)) {
			logger.warn("Apacs original RsCode: " + rsCode);
			return ISOResponseCodes.BANK_LINK_DOWN;
		}
		
		if(ISOResponseCodes.INCORRECT_ONLINE_REFNUMBER.equals(rsCode))
			return "89";
		
		if(ISOResponseCodes.INVALID_COMPANY_CODE.equals(rsCode))
			return "89";

		if(rsCode != null && rsCode.length() > 2) {
			logger.warn("Apacs original RsCode: " + rsCode);
			return ISOResponseCodes.CUSTOMER_RELATION_NOT_FOUND;
		}

		return rsCode;
	}

	public static String issuerCode(Ifx ifx) {
		String issuerCode = null;
		if(IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType()) || 
			IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {

			if(ifx.getRecvBankId() != null)
				issuerCode = ProcessContext.get().getProtocolConfig(ProtocolType.APACS70,ifx.getRecvBankId().toString());
		}
		else if(ifx.getDestBankId() != null)
			issuerCode = ProcessContext.get().getProtocolConfig(ProtocolType.APACS70,ifx.getDestBankId().toString());

		if(ProcessContext.get().getMyInstitution().getBin() == 502229L && "16".equals(issuerCode)) { // Only for Pasargad 
			String appPan = null;
			int cardProductCode = -1;
			if(IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType()) ||
					IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(ifx.getIfxType()))
				appPan = ifx.getSecondAppPan();
			else
				appPan = ifx.getAppPAN();

			if(Util.hasText(appPan))
				cardProductCode = Integer.parseInt(appPan.substring(6,7));

			switch(cardProductCode){
				case 1: 
				case 2: 
					issuerCode = "31"; // Debit
					break;
				case 3: 
				case 4: 
					issuerCode = "32"; // Bon/Gift
					break;
				case 6: 
					issuerCode = "33"; // Credit
				}
		}

/*		
		if (!Util.hasText(issuerCode)) {
			if (Util.hasText(ifx.getApplicationVersion()) && ifx.getApplicationVersion().startsWith("1"))
				issuerCode = "225";
		}
		return issuerCode;
*/
		return changeIssuerCode(ifx, issuerCode);

	}

	public static String changeIssuerCode(Ifx ifx, String issuerCode) {
		if (!Util.hasText(issuerCode)) {
			if (Util.hasText(ifx.getApplicationVersion()) && ifx.getApplicationVersion().startsWith("1"))
				issuerCode = "225";
		}
		return issuerCode;
	}

	public static void truncateReceiptWithoutNL(ApacsByteArrayWriter out, byte[] b) throws IOException {
		final int maxLen = 80;
		final byte enter = (byte)Apacs70FarsiConvertor.ENTER;
		if(b != null && b.length > 0) {
			if(b.length < maxLen) {
				out.write(b, maxLen);
				if(b[b.length - 1] != enter)
					out.write(enter);
			}
			else {
				byte[] tmp = Arrays.copyOf(b, maxLen - 1);
				out.write(tmp, maxLen - 1);
				out.write(enter);
			}
		}
	}

	public static void checkValidityOfLastTransactionStatus(Terminal terminal, Ifx incomingIfx) throws Exception {
		TransactionService.checkValidityOfLastTransactionStatus(terminal, incomingIfx);
		Transaction lastTransaction = terminal.getLastTransaction();
		if (lastTransaction != null) {
			try {
				ClearingInfo srcClrInfo = lastTransaction.getSourceClearingInfo();
				if (srcClrInfo != null && ClearingState.DISAGREEMENT.equals(srcClrInfo.getClearingState())) {
					SchedulerService.processReversalJob(lastTransaction.getFirstTransaction(), lastTransaction, ISOResponseCodes.APPROVED, null, false);
				} 
			}catch (ObjectNotFoundException e) {
				logger.warn("checkValidityOfLastTransactionStatus: ", e);
			}
		}
	}

}

