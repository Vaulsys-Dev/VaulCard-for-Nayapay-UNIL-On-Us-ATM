package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.MCIBillPaymentUtil;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.billpayment.exception.DuplicateBillPaymentMessageException;
import vaulsys.billpayment.exception.NotValidBillPaymentMessageException;
import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.MCIVirtualVosoliJobInfo;
import vaulsys.scheduler.NAJAVosoliJobInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Notification;
import vaulsys.util.Util;
import vaulsys.util.phoneUtil;
//import vaulsys.webservices.mcivirtualvosoli.common.MCIVosoliState;
import vaulsys.wfe.ProcessContext;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class BillPaymentProcess extends MessageProcessor {

    private Logger logger = Logger.getLogger(this.getClass());


    public static final BillPaymentProcess Instance = new BillPaymentProcess();
    private BillPaymentProcess(){};


    @Override
    public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
            throws Exception {
        Ifx incomingIfx = transaction.getIncomingIfx();

        try{
            if (
                    processContext.getMyInstitution().getBin().equals(incomingIfx.getBankId()) &&
                            ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType()) &&
                            ISOResponseCodes.APPROVED.equals(incomingIfx.getRsCode())) {
                if (ConfigUtil.getBoolean(ConfigUtil.MCI_HAS_VOSOLI) &&
                        OrganizationType.MOBILE.equals(incomingIfx.getBillOrgType()) &&
                        incomingIfx.getBillCompanyCode() != null &&
                        incomingIfx.getBillOrgType() != null) {

                    Map<String, String> map = ConfigUtil.getProperties("mci.companycode");

                    if (map != null && map.size() > 0 && map.containsValue(incomingIfx.getBillCompanyCode().toString())) {
                        Transaction firstTrx = transaction.getFirstTransaction();
                        logger.debug("creating MCI virtual vosoli job info for trx: " + firstTrx.getId());
//                        incomingIfx.setMciVosoliState(MCIVosoliState.NOT_SEND);
//                        firstTrx.getIncomingIfx().setMciVosoliState(MCIVosoliState.NOT_SEND);
                        MCIVirtualVosoliJobInfo mciVosoliJob = new MCIVirtualVosoliJobInfo(DateTime.now(), transaction, ConfigUtil.getInteger(ConfigUtil.MCIVOSOLI_COUNT));
                        GeneralDao.Instance.saveOrUpdate(mciVosoliJob);
                    }
                }
                else if (ConfigUtil.getBoolean(ConfigUtil.NAJAVOSOLI_ENABLE)
               		 && OrganizationType.FREEZONE.equals(incomingIfx.getBillOrgType())
               		)
               {
               	 Date nowDate = new Date( );
               	 long addhour=ConfigUtil.getLong(ConfigUtil.NAJAVOSOLI_XHOURAFTERFIRE);
               	 Date nextDate = new Date(nowDate.getTime() + (1000 * 60 * 60 * addhour));  
               	
               	 NAJAVosoliJobInfo nvji=new NAJAVosoliJobInfo(new DateTime(nextDate), transaction, ConfigUtil.getInteger(ConfigUtil.NAJAVOSOLI_COUNT));
               	 GeneralDao.Instance.saveOrUpdate(nvji);
               }
            }
        }catch (Exception e){
            logger.error("error in MCI" + e, e);
        }
        return GeneralMessageProcessor.Instance.createOutgoingMessage(transaction, incomingMessage, channel, processContext);
    }

    @Override
    public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
        try {
            if (ifx.getBillOrgType().equals(OrganizationType.UNDEFINED) && ifx.getBillID().equals("0") && ifx.getBillPaymentID().equals("0")) {
                return;
            }
            if (!Util.hasText(ifx.getBillID()))
                throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has empty BillID");

            if (!Util.hasText(ifx.getBillPaymentID()))
                throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has empty getBillPaymentID");

            validateBillPaymentMessage(ifx);

            if (isDuplicateBillPaymentMessage(ifx, incomingMessage))
                throw new DuplicateBillPaymentMessageException("BillId: "+ ifx.getBillID()+"- PaymentId: "+ ifx.getBillPaymentID());

            if(! MCIBillPaymentUtil.isBillPaymentWithMobileNumber(ifx)){
            	// Set billOrgType generally for valid bill
            	ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(ifx.getBillID()));
            	if (ifx.getBillOrgType().equals(OrganizationType.MOBILE)) {
            		if (ifx.getBillID().length() < 13) {
            			Map<String, String> map = ConfigUtil.getProperties("mci.companycode");
            			if (map != null && map.size() > 0 && map.containsValue(ifx.getBillCompanyCode().toString()))
            				throw new MandatoryFieldException("Failed: " + "BillID for MOBILE Must be have 13 digit but billId is: " + ifx.getBillID());
            		}
            	}
            }

/*			
			//TODO: To be removed Yaraneh
        	List<String> appPans = new ArrayList<String>();
        	//TODO: To be removed
        	appPans.add("6362141000010109");
        	if(ifx.getOpkey().equals("AAHC    ") && appPans.contains(ifx.getAppPAN())){
        		if(ifx.getAuth_Amt() >= 1000){
        			ifx.setAuth_Amt(ifx.getReal_Amt()-1000);
//        			ifx.setSec_Amt(ifx.getReal_Amt());
        		}else{
        			ifx.setAuth_Amt(0L);
//        			ifx.setSec_Amt(0L);
        		}
        	}else if(appPans.contains(ifx.getAppPAN()) && IfxType.PREPARE_BILL_PMT.equals(ifx.getIfxType())){
        		ifx.setReal_Amt(1000L);
        	}
        	//TODO: End
*/
            GeneralMessageProcessor.Instance.messageValidation(ifx, incomingMessage);
        } catch (Exception e) {
            logger.warn(e.getClass().getSimpleName()+": "+ e.getMessage());
            throw e;
        }
    }

    private boolean isDuplicateBillPaymentMessage(Ifx ifx, Message incomingMessage) {
        String billID = ifx.getBillID();
        String paymentID = ifx.getBillPaymentID();

        if (ifx.getBillOrgType().equals(OrganizationType.UNDEFINED) && billID.startsWith("12345678"))
            return false;

        List<Transaction> transactions = TransactionService.getBillTransaction(billID, paymentID, incomingMessage);
        if (transactions == null || transactions.size() == 0)
            return false;
        return true;
    }

    private boolean validateBillPaymentMessage(Ifx ifx) throws NotValidBillPaymentMessageException {
        String billID = ifx.getBillID();
        String paymentID = ifx.getBillPaymentID();
        Notification notification = new Notification();
        if(MCIBillPaymentUtil.isBillPaymentWithMobileNumber(paymentID)){
        	String shomareMobile = billID;
        	if(! phoneUtil.isValidMCIMobilePhoneNumber(shomareMobile))
        		notification.addError("invalide phone number : " + shomareMobile);
        }else
        {
        	if(! BillPaymentUtil.hasValidLength(billID))
        		notification.addError("bill id '" + billID + "' has an invalid length");
        	if(! BillPaymentUtil.hasValidLength(paymentID))
        		notification.addError("payment id '" + paymentID + "' has an invalid length");
        	if(notification.hasError())
        		throw new NotValidBillPaymentMessageException(notification.getErrorMessages());
        	if(! BillPaymentUtil.isCorrectAmount(paymentID, ifx.getAuth_Amt()))
        		notification.addError("incorrect amount '" + ifx.getAuth_Amt() + "' with payment  id '" + paymentID +"'");
        	if(! BillPaymentUtil.isSupportedOrganization(billID))
        		notification.addError("The organization is not supported. Company Code : " + BillPaymentUtil.extractCompanyCode(billID) + " , Organization Type : " + BillPaymentUtil.extractBillOrgType(billID));
        	if(! BillPaymentUtil.isCorrectCheckDigitNoOne(billID.substring(0, billID.length() - 1), billID.substring(billID.length() - 1)))
        		notification.addError("incorrect CheckDigitNoOne in bill id : " + billID);
        	if(! BillPaymentUtil.isCorrectCheckDigitNoOne(paymentID.substring(0, paymentID.length() - 2), paymentID.substring(paymentID.length() - 2, paymentID.length() - 1)))
        		notification.addError("incorrect CheckDigitNoOne in payment id: " + paymentID);
        	if(! BillPaymentUtil.isCorrectCheckDigitNoOne(Util.trimLeftZeros(billID) + Util.trimLeftZeros(paymentID.substring(0, paymentID.length() - 1)), paymentID.substring(paymentID.length() - 1)))
        		notification.addError("incorrect CheckDigitNoOne (left zeros trimmed)");
        }
        if(notification.hasError())
        	throw new NotValidBillPaymentMessageException(notification.getErrorMessages());
        return true;
    }

}
