package vaulsys.billpayment;

import vaulsys.billpayment.exception.NotValidBillPaymentMessageException;
import vaulsys.billpayment.exception.SetBillDataException;
import vaulsys.exception.WebServiceFailException;
import vaulsys.exception.validation.InvalidMobilePhoneNumberException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.ConfigUtil;
import vaulsys.util.phoneUtil;
//import vaulsys.webservices.mci.billpayment.BillInfo; //Raza commenting
//import vaulsys.webservices.mci.billpayment.BillPaymentServiceClient; //Raza commenting

import org.apache.log4j.Logger;

public class MCIBillPaymentUtil {
	private transient Logger logger = Logger.getLogger(MCIBillPaymentUtil.class);
	public static final String RESERVED_PAY_ID_FOR_BILLPMT_WITH_MOBILE_NUMBER = "11111111";

	public static boolean isBillPaymentWithMobileNumber(String receivedPaymentId) {
		return receivedPaymentId.equals(RESERVED_PAY_ID_FOR_BILLPMT_WITH_MOBILE_NUMBER);
	}
	public void retreiveSetBillInfo(Ifx mciBillPaymentIfx) throws WebServiceFailException, InvalidMobilePhoneNumberException, NotValidBillPaymentMessageException, SetBillDataException {
		if(! isBillPaymentWithMobileNumber(mciBillPaymentIfx.getBillPaymentID())){
			logger.warn("Something has gone wrong! The message is not supposed to be here!");
			throw new NotValidBillPaymentMessageException("This is not a valid MCI bill payment message. There is a problem with pay id.");
		}
		String phoneNumber = mciBillPaymentIfx.getBillID();
		if(! phoneUtil.isValidMCIMobilePhoneNumber(phoneNumber))
			throw new InvalidMobilePhoneNumberException(phoneNumber);
//		BillInfo billInfo; //Raza commenting
//		try { //Raza commenting
//			billInfo = retreiveBillInfo(phoneNumber); //Raza commenting
//		} catch (WebServiceFailException e) { //Raza commenting
//			logger.error(e.getMessage()); //Raza commenting
//			throw e; //Raza commenting
//		} //Raza commenting
//		BillPaymentUtil.setBillData(mciBillPaymentIfx, billInfo.getBillId(), billInfo.getPaymentId()); //Raza commenting
	}
//	public BillInfo retreiveBillInfo(String mobilePhoneNumber) throws InvalidMobilePhoneNumberException, WebServiceFailException{
//		String ENDPOINT = ConfigUtil.getProperty(ConfigUtil.MCI_BILL_PAYMENT_WEBSERVICE_ENDPOINT);
//		int retryCount = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.MCI_BILL_PAYMENT_WEBSERVICE_RETRY_COUNT));
//		return new BillPaymentServiceClient(ENDPOINT).retreiveBillInfo(mobilePhoneNumber, retryCount);
//	}

	public static boolean isBillPaymentWithMobileNumber(Ifx incomingBillPaymentIfx) {
		return isBillPaymentWithMobileNumber(incomingBillPaymentIfx.getBillPaymentID());
	}

}
