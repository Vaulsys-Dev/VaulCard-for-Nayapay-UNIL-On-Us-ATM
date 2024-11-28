package vaulsys.billpayment;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DayDate;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public class BillPaymentUtil {
	static Logger logger = Logger.getLogger(BillPaymentUtil.class);


	public static Integer extractCompanyCode(String shenaseGhabz) {
		try {
			String strCompanyCode = shenaseGhabz.substring(shenaseGhabz.length() - 5, shenaseGhabz.length() - 2);
			return Integer.parseInt(strCompanyCode);
		} catch (Exception e) {
			logger.warn("Exception in extractCompanyCode with BillID: " + shenaseGhabz + ", " + e);
			return null;
		}
	}

	public static OrganizationType extractBillOrgType(String shenaseGhabz) {
		try {
			String strCode = shenaseGhabz.substring(shenaseGhabz.length() - 2, shenaseGhabz.length() - 1);
			Integer code = Integer.parseInt(strCode);
			return OrganizationType.getByCode(code);
		} catch (Exception e) {
			logger.warn("Exception in extractBillOrgType with BillID: " + shenaseGhabz + ", " + e);
			return OrganizationType.UNKNOWN;
		}
	}

	public static Long extractAmount(String shenasePardakht) {
		try {
			String amount = shenasePardakht.substring(0, shenasePardakht.length() - 2);
			return Long.parseLong(amount) / 1000 * 1000;
		} catch (Exception e) {
			logger.warn("Exception in extractAmount with BillPaymentID: " + shenasePardakht + ", " + e);
			return null;
		}
	}

	public static boolean isCorrectAmount(String paymentID, long ifxAmount) {
		long billIDAmount = 0;
		try {
			billIDAmount = Long.parseLong(paymentID.substring(0, paymentID.length() - 5)) * 1000;
		} catch (Exception e) {
			logger.warn("Exception in isCorrectAmount with BillPaymentID: " + paymentID + ", " + e);
			return false;
		}
		return billIDAmount == ifxAmount;
	}

	public static boolean isSupportedOrganization(String billID) {
		try {
			Integer companyCode = extractCompanyCode(billID);

			Organization org = OrganizationService.findOrganizationByCompanyCode(companyCode,
					extractBillOrgType(billID));
			if (org == null)
				return false;

			if (!org.isOwnOrParentEnabled())
				return false;

			if (org.getContract().getStartDate() != null) {
				if (!(org.getContract().getStartDate().isValid()
						&& org.getContract().getStartDate().before(DayDate.now())))
					return false;
			}

			if (org.getContract().getEndDate() != null) {
				if (!org.getContract().getEndDate().after(DayDate.now()))
					return false;
			}

			return true;

		} catch (Exception e) {
			logger.warn("Exception in isSupportedOrganization with BillID: " + billID + ", " + e);
			return false;
		}
	}

	public static Organization getOrganization(String billID) {
		try {
			Integer companyCode = extractCompanyCode(billID);

			Organization org = OrganizationService.findOrganizationByCompanyCode(companyCode, extractBillOrgType(billID));
			return org;
		} catch (Exception e) {
			logger.warn("Exception in getOrganization with BillID: " + billID + ", " + e);
			return null;
		}
	}

	public static Long getThirdPartyTerminalId(String billID) {
		try {
			Integer companyCode = extractCompanyCode(billID);

			//Organization org = OrganizationService.findOrganizationByCompanyCode(companyCode, extractBillOrgType(billID));
			ThirdPartyVirtualTerminal term = OrganizationService.findThirdPartyVirtualTerminalByCompanyCode(companyCode, extractBillOrgType(billID));
			if(term != null)
				return term.getCode();
			return null;
		} catch (Exception e) {
			logger.warn("Exception in getOrganization with BillID: " + billID + ", " + e);
			return null;
		}
	}

	public static boolean isCorrectCheckDigitNoOne(String identifierWithoutCheckDigit, String checkDigit) {
		try {
			int myCheckDigit = getCheckDigit(identifierWithoutCheckDigit);
			if (myCheckDigit == Integer.parseInt(checkDigit))
				return true;
			return false;
		} catch(Exception e) {
			logger.warn("Exception in isCorrectCheckDigitNoOne, " + e);
			return false;
		}
	}

	public static Boolean hasValidLength(String s){
		return Util.hasText(s) && 5 < s.length() && s.length() < 14;
	}


	public static int getCheckDigit(String identifier) {
		int myCheckDigit;
		String[] allDigits = new String[identifier.length()];
		for (int i = 0; i < identifier.length(); i++) {
			allDigits[i] = identifier.substring(i, i + 1);
		}
		int mult = 1;
		int result = 0;
		for (int i = identifier.length() - 1; i >= 0; i--) {
			if (++mult == 8)
				mult = 2;
			result += mult * Integer.parseInt(allDigits[i]);
		}
		result = result % 11;
		if (result == 0 || result == 1)
			myCheckDigit = 0;
		else {
			myCheckDigit = 11 - result;
		}
		return myCheckDigit;
	}
	public static void setBillData(Ifx inputIfx, String billId, String payId){
		inputIfx.setAuth_Amt(BillPaymentUtil.extractAmount(payId));
		inputIfx.setReal_Amt(inputIfx.getAuth_Amt());
		inputIfx.setTrx_Amt(inputIfx.getAuth_Amt());
		inputIfx.setBillPaymentID(payId);
		inputIfx.setBillID(billId);
		inputIfx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billId));
		inputIfx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billId));
		inputIfx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billId));
	}
}
