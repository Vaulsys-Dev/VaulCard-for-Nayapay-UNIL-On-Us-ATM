package vaulsys.util;

import vaulsys.cms.base.CMSAccount;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wallet.base.ledgers.WalletGeneralLedger;
import vaulsys.webservice.walletcardmgmtwebservice.entity.SwitchTransactionCodes;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsListingEntity;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Raza Murtaza on 31-Jan-18.
 */
public class WebServiceUtil {
    private static final Logger logger = Logger.getLogger(WebServiceUtil.class);

    public static String uuidregex = "^[a-zA-Z0-9]*$";
    //public static String mobregex = "^[\\+?\\d+]{14}"; //[0-9+#-]+
    public static String mobregex = "^0092\\d{10}$"; //"^[\\00?\\d+]{14}"; //[0-9+#-]+
    public static String cnicregex = "^[0-9]{13}$";
    public static String amountregex = "^[0-9]{12}$";
    public static String numregex = "\\d+";
    public static String strregex = "[a-zA-Z]+";
    public static String strspceregex = "[a-zA-Z\\s']+";
    public static Pattern numpattern = Pattern.compile(numregex);
    public static Pattern strpattern = Pattern.compile(strregex);
    public static Pattern strspcpattern = Pattern.compile(strspceregex);
    public static Pattern mobpattern = Pattern.compile(mobregex);
    public static Pattern uuidpattern = Pattern.compile(uuidregex);
    public static Pattern cnicpattern = Pattern.compile(cnicregex);
    public static Pattern amountpattern = Pattern.compile(amountregex);
    public static SimpleDateFormat dobdFormat = new SimpleDateFormat("dd-MM-yyyy"); //("yyyymmdd");
    //public static SimpleDateFormat transdatetimeFormat = new SimpleDateFormat("MMddHHmmss");
    public static String transdatetimeFormat = "MMddHHmmss"; //length 10
    public static SimpleDateFormat cnicExpiryFormat = new SimpleDateFormat("dd-MM-yyyy");
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static DateFormat tokenFormat = new SimpleDateFormat("MMddHHmmss");
    public static DateFormat limitcycleDateFormat = new SimpleDateFormat("yyyyMMdd");
    public static DateFormat cardExpiryFormat = new SimpleDateFormat("yyMM");
    public static DateFormat AccessTokendateFormat = new SimpleDateFormat("MMddHHmmss");
    public static String StrAccessTokendateFormat = "MMddHHmmss";
    public static Calendar calender;

    public WebServiceUtil()
    {

    }


    //m.rehman: 10-11-2021 - Nayapay Optimization, adding listing object
    public static void PrintWSMsg(WalletCMSWsEntity wsmodel, boolean isrequest, WalletCMSWsListingEntity listing)
    {
        if(isrequest) {
            logger.info("********************Request********************");
            PrintMsg(wsmodel);
            logger.info("********************Request********************");

            //m.rehman: 14-09-2021, Optimization on Nayapay
            SwitchTransactionCodes txnCode = GlobalContext.getInstance().getTransactionCodeDescbyCode(wsmodel.getServicename());
            if (txnCode != null &&
                    ((txnCode.getIsfinancial() != null && txnCode.getIsfinancial())
                            || (txnCode.getIsBypass() != null && !txnCode.getIsBypass()))) {
                if (wsmodel.getSecurityparams() != null) {
                    wsmodel.getSecurityparams().setId(null);
                }
                wsmodel.setId(null);
                GeneralDao.Instance.saveOrUpdate(wsmodel);

                //m.rehman: 10-11-2021 - Nayapay Optimization
                listing = listing.copy(wsmodel);
                GeneralDao.Instance.saveOrUpdate(listing);
                /////////////////////////////////////////////////////////////////

                GeneralDao.Instance.flush(); //Raza Adding for Unique TxnRefNum and other Constraints for WSMODEL class
                //GeneralDao.Instance.commit(); //Raza Testing for getiing constraint wxception for same txnrefnum & transdatetime composite key
                logger.info("Request msg saved in Db..!");
                //////wsmodel.setId(wsmodel.getId()+"");
                //////GeneralDao.Instance.saveOrUpdate(nayaPayWsEntity);
                //////wsmodel.setId(nayaPayWsEntity.getId()+"");
                //GeneralDao.Instance.commit();
            }
        }
        else
        {

            logger.info("********************Response********************");
            PrintMsg(wsmodel);
            logger.info("********************Response********************");

            //m.rehman: 14-09-2021, Optimization on Nayapay
            SwitchTransactionCodes txnCode = GlobalContext.getInstance().getTransactionCodeDescbyCode(wsmodel.getServicename());
            if (txnCode != null &&
                    ((txnCode.getIsfinancial() != null && txnCode.getIsfinancial())
                            || (txnCode.getIsBypass() != null && !txnCode.getIsBypass()))) {
                if (wsmodel.getId() != null) {
                    //PrintWSMsg(wsmodel, true);
                    GeneralDao.Instance.saveOrUpdate(wsmodel);

                    //m.rehman: 10-11-2021 - Nayapay Optimization
                    listing = listing.copy(wsmodel);
                    GeneralDao.Instance.saveOrUpdate(listing);
                    /////////////////////////////////////////////////////////////////

                } else {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("id", wsmodel.getId());
//                params.put("nayaid", wsmodel.getNayapayid());
//                params.put("mobnum", (Util.hasText(wsmodel.getMobilenumber())) ? wsmodel.getMobilenumber() : ""); //wsmodel.getMobilenumber());
//                params.put("bnkcode", wsmodel.getBankcode());
                    //WebServiceEntity ent = (WebServiceEntity) GeneralDao.Instance.findObject("from WebServiceEntity c where c.nayapayid= :nayaid " + "and c.mobilenumber = :mobnum " + "and c.bankcode = :bnkcode " + "and c.id = :id", params);
                    WalletCMSWsEntity ent = (WalletCMSWsEntity) GeneralDao.Instance.findObject("from WalletCMSWsEntity c where c.id = :id", params);
                    if (ent != null) {
                        ent.setRespcode(wsmodel.getRespcode());
                        GeneralDao.Instance.saveOrUpdate(ent);

                        //m.rehman: 10-11-2021 - Nayapay Optimization
                        listing = listing.copy(wsmodel);
                        GeneralDao.Instance.saveOrUpdate(listing);
                        /////////////////////////////////////////////////////////////////

                        GeneralDao.Instance.flush(); //Raza Adding for Unique TxnRefNum and other Constraints for WSMODEL class
                    } else {
                        logger.error("WSEntity not found in DB for Response..!");
//                ent.setRespcode(ISOResponseCodes.ERROR_UNKOWN);
//                GeneralDao.Instance.saveOrUpdate(ent);
                    }
                }
                //GeneralDao.Instance.commit();
            }
        }
        wsmodel.setDecryptedotp(null);
        wsmodel.setCiphereddata(null);
    }

    public static void PrintMsg(WalletCMSWsEntity wsmodel)
    {
        if (Util.hasText(wsmodel.getServicename())) {
            logger.info("ServiceName [" + wsmodel.getServicename() + "]");
        }

        if (Util.hasText(wsmodel.getMobilenumber())) {
            logger.info("Mobile Num [" + getFullMaskedValue(wsmodel.getMobilenumber()) + "]");
        }

        if (Util.hasText(wsmodel.getCnic())) {
            logger.info("CNIC [" + getFullMaskedValue(wsmodel.getCnic()) + "]");
        }

        if (Util.hasText(wsmodel.getCustomername())) {
            logger.info("CustomerName [" + wsmodel.getCustomername() + "]");
        }

        if (Util.hasText(wsmodel.getCnicexpiry())) {
            logger.info("CnicExpiry [" + wsmodel.getCnicexpiry() + "]");
        }

        if (Util.hasText(wsmodel.getBankcode())) {
            logger.info("BankCode [" + wsmodel.getBankcode() + "]");
        }

        if (Util.hasText(wsmodel.getDestbankcode())) {
            logger.info("DestBankCode [" + wsmodel.getDestbankcode() + "]");
        }

        if (Util.hasText(wsmodel.getBankname())) {
            logger.info("BankName [" + wsmodel.getBankname() + "]");
        }

        if (Util.hasText(wsmodel.getAccountnumber())) {
            logger.info("AccountNumber [" + getFullMaskedValue(wsmodel.getAccountnumber()) + "]");
        }

        if (Util.hasText(wsmodel.getAccountcurrency())) {
            logger.info("AccountCurrency [" + wsmodel.getAccountcurrency() + "]");
        }

        if (Util.hasText(wsmodel.getTranrefnumber())) {
            logger.info("TransactionReferenceNo [" + wsmodel.getTranrefnumber() + "]");
        }

        if (Util.hasText(wsmodel.getMothername())) {
            logger.info("Mother Name [" + wsmodel.getMothername() + "]");
        }

        if (Util.hasText(wsmodel.getDateofbirth())) {
            logger.info("Date of Birth [" + wsmodel.getDateofbirth() + "]");
        }

        if (Util.hasText(wsmodel.getTransdatetime())) {
            logger.info("TransmissionDateTime [" + wsmodel.getTransdatetime() + "]");
        }

        if (Util.hasText(wsmodel.getPindata())) {
            logger.info("PinData [" + getFullMaskedValue(wsmodel.getPindata()) + "]");
        }

        if (Util.hasText(wsmodel.getOldpindata())) {
            logger.info("OldPinData [" + getFullMaskedValue(wsmodel.getOldpindata()) + "]");
        }

        if (Util.hasText(wsmodel.getNewpindata())) {
            logger.info("NewPinData [" + getFullMaskedValue(wsmodel.getNewpindata()) + "]");
        }

        if (Util.hasText(wsmodel.getEncryptkey())) {
            logger.info("EncryptKey [" + getFullMaskedValue(wsmodel.getEncryptkey()) + "]");
        }

        if (Util.hasText(wsmodel.getAmounttransaction())) {
            logger.info("Amount Txn [" + wsmodel.getAmounttransaction() + "]");
        }

        if (Util.hasText(wsmodel.getSrcchargeamount())) {
            logger.info("Source Amount Charges [" + wsmodel.getSrcchargeamount() + "]");
        }

        if (Util.hasText(wsmodel.getDestchargeamount())) {
            logger.info("Destination Amount Charges [" + wsmodel.getDestchargeamount() + "]");
        }

        if (Util.hasText(wsmodel.getDestaccount())) {
            logger.info("Dest Account [" + getFullMaskedValue(wsmodel.getDestaccount()) + "]");
        }

        if (Util.hasText(wsmodel.getDestaccountcurrency())) {
            logger.info("Dest Account Currency [" + wsmodel.getDestaccountcurrency() + "]");
        }

        if (Util.hasText(wsmodel.getOtp())) {
            logger.info("OTP [" + getFullMaskedValue(wsmodel.getOtp()) + "]");
        }

        if (Util.hasText(wsmodel.getBiometricdata())) {
            //logger.info("BioMetric Data [" + wsmodel.getBiometricdata() + "]");
            logger.info("BioMetric Data Received ...");
        }

        if (Util.hasText(wsmodel.getRespcode())) {
            logger.info("Response Code [" + wsmodel.getRespcode() + "]");
        }

        if (wsmodel.getPlaceofbirth() != null) {
            logger.info("Place of Birth [" + wsmodel.getPlaceofbirth() + "]");
        }

        if (wsmodel.getFathername() != null) {
            logger.info("Father/Husband name [" + wsmodel.getFathername() + "]");
        }

        if (wsmodel.getProvince() != null) {
            logger.info("Province [" + wsmodel.getProvince() + "]");
        }

        if (wsmodel.getTsp() != null) {
            logger.info("Telecom Service Provider [" + wsmodel.getTsp() + "]");
        }

        if (Util.hasText(wsmodel.getDestnayapayid())) {
            logger.info("Destination Cnic [" + wsmodel.getDestnayapayid() + "]");
        }

        if (Util.hasText(wsmodel.getOrigdataelement())) {
            logger.info("Orig Data Element [" + wsmodel.getOrigdataelement() + "]");
        }

        if (Util.hasText(wsmodel.getAtmid())) {
            logger.info("ATM ID [" + wsmodel.getAtmid() + "]");
        }

        if (Util.hasText(wsmodel.getCorebankcode())) {
            logger.info("Core Bank Code [" + wsmodel.getCorebankcode() + "]");
        }

        if (Util.hasText(wsmodel.getCoreaccount())) {
            logger.info("Core Account number [" + getFullMaskedValue(wsmodel.getCoreaccount()) + "]");
        }

        if (Util.hasText(wsmodel.getCoreaccountcurrency())) {
            logger.info("Core Account Currency [" + wsmodel.getCoreaccountcurrency() + "]");
        }

        if(Util.hasText(wsmodel.getCardnumber()))
        {
            logger.info("Card Number [" + getFullMaskedValue(wsmodel.getCardnumber()) + "]");
        }

        if(Util.hasText(wsmodel.getCardpindata()))
        {
            logger.info("Card Pin Data [" + getFullMaskedValue(wsmodel.getCardpindata()) + "]");
        }

        if(Util.hasText(wsmodel.getEnableflag()))
        {
            logger.info("Card Enable Flag [" + wsmodel.getEnableflag() + "]");
        }

        if(Util.hasText(wsmodel.getMerchantid()))
        {
            logger.info("Merchant Id [" + wsmodel.getMerchantid() + "]");
        }
    //Added by Moiz for VP-NAP-202405061 => link CW (OffUs) channel Limit Authentication Change
        if(Util.hasText(wsmodel.getRemainingLimit()))
        {
            logger.info("Remaining Limit [" + wsmodel.getRemainingLimit() + "]");
        }

        if(Util.hasText(wsmodel.getDailylimit()))
        {
            logger.info("Daily Limit [" + wsmodel.getDailylimit() + "]");
        }

        if(Util.hasText(wsmodel.getMonthlylimit()))
        {
            logger.info("Monthly Limit [" + wsmodel.getMonthlylimit() + "]");
        }

        if(Util.hasText(wsmodel.getYearlylimit()))
        {
            logger.info("Yearly Limit [" + wsmodel.getYearlylimit() + "]");
        }

        if(Util.hasText(wsmodel.getStatus()))
        {
            logger.info("Status [" + wsmodel.getStatus() + "]");
        }

        if(Util.hasText(wsmodel.getStan()))
        {
            logger.info("Stan [" + wsmodel.getStan() + "]");
        }

        if(Util.hasText(wsmodel.getRrn()))
        {
            logger.info("Rrn [" + wsmodel.getRrn() + "]");
        }

        if(Util.hasText(wsmodel.getUserid()))
        {
            logger.info("UserId [" + wsmodel.getUserid() + "]");
        }

        if(Util.hasText(wsmodel.getChannelid()))
        {
            logger.info("ChannelId [" + wsmodel.getChannelid() + "]");
        }

        if(Util.hasText(wsmodel.getAllowed()))
        {
            logger.info("AllowedFlag [" + wsmodel.getAllowed() + "]");
        }

        if(Util.hasText(wsmodel.getDeletetype()))
        {
            logger.info("DeleteType [" + wsmodel.getDeletetype() + "]");
        }

        if(Util.hasText(wsmodel.getComments()))
        {
            logger.info("Comments [" + wsmodel.getComments() + "]");
        }

        if(Util.hasText(wsmodel.getAcctid()))
        {
            logger.info("AcctId [" + wsmodel.getAcctid() + "]");
        }

        if(Util.hasText(wsmodel.getAcctalias()))
        {
            logger.info("AcctAlias [" + wsmodel.getAcctalias() + "]");
        }

        if(Util.hasText(wsmodel.getIsprimary()))
        {
            logger.info("IsPrimary Flag [" + wsmodel.getIsprimary() + "]");
        }

        if(Util.hasText(wsmodel.getAcctbalance()))
        {
            logger.info("AcctBalance [" + wsmodel.getAcctbalance() + "]");
        }

        if(Util.hasText(wsmodel.getAcctlimit()))
        {
            logger.info("AcctLimit [" + wsmodel.getAcctlimit() + "]");
        }

        if(Util.hasText(wsmodel.getAvaillimit()))
        {
            logger.info("AvailLimit [" + wsmodel.getAvaillimit() + "]");
        }

        if(Util.hasText(wsmodel.getAvaillimitfreq()))
        {
            logger.info("AvailLimitFreq [" + wsmodel.getAvaillimitfreq() + "]");
        }

        if(Util.hasText(wsmodel.getState()))
        {
            logger.info("State [" + wsmodel.getState() + "]");
        }

        if(Util.hasText(wsmodel.getRequesttime()))
        {
            logger.info("RequestTime [" + wsmodel.getRequesttime() + "]");
        }

        if(Util.hasText(wsmodel.getActivationtime()))
        {
            logger.info("ActivationTime [" + wsmodel.getActivationtime() + "]");
        }

        if(Util.hasText(wsmodel.getNayapaycharges()))
        {
            logger.info("NayaPayCharges [" + wsmodel.getNayapaycharges() + "]");
        }

        if(Util.hasText(wsmodel.getDestuserid()))
        {
            logger.info("Destuserid [" + wsmodel.getDestuserid() + "]");
        }

        if(Util.hasText(wsmodel.getAddress()))
        {
            logger.info("Address [" + wsmodel.getAddress() + "]");
        }

        if(Util.hasText(wsmodel.getCity()))
        {
            logger.info("City [" + wsmodel.getCity() + "]");
        }

        if(Util.hasText(wsmodel.getCountry()))
        {
            logger.info("Country [" + wsmodel.getCountry() + "]");
        }

        if(Util.hasText(wsmodel.getAdvanceflag()))
        {
            logger.info("AdvanceFlag [" + wsmodel.getAdvanceflag() + "]");
        }

        if(Util.hasText(wsmodel.getSecondarynumber()))
        {
            logger.info("SecondaryNumber [" + getFullMaskedValue(wsmodel.getSecondarynumber()) + "]");
        }

        if(Util.hasText(wsmodel.getAccesstoken()))
        {
            logger.info("AccessToken [" + getFullMaskedValue(wsmodel.getAccesstoken()) + "]");
        }

        if(Util.hasText(wsmodel.getInoutfilter()))
        {
            logger.info("InOutFilter [" + wsmodel.getInoutfilter() + "]");
        }

        // Asim Shahzad, Date : 17th March 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115
//        if(Util.hasText(wsmodel.getTypefilter()))
//        {
//            logger.info("TypeFilter [" + wsmodel.getTypefilter() + "]");
//        }
        if(wsmodel.getTypefilter() != null) {
            for(String typeFilter : wsmodel.getTypefilter()) {
                logger.info("TypeFilter [" + typeFilter + "]");
            }
        }

        if(Util.hasText(wsmodel.getTotalamountspent())) {
            logger.info("TotalAmountSpent [" + wsmodel.getTotalamountspent() + "]");
        }

        if(Util.hasText(wsmodel.getTotalamountreceived())) {
            logger.info("TotalAmountReceived [" + wsmodel.getTotalamountreceived() + "]");
        }
        // =======================================================================================

        if(Util.hasText(wsmodel.getSearchtext()))
        {
            logger.info("SearchText [" + wsmodel.getSearchtext() + "]");
        }

        if(Util.hasText(wsmodel.getUsername()))
        {
            logger.info("UserName [" + wsmodel.getUsername() + "]");
        }

        if(Util.hasText(wsmodel.getNayapayid()))
        {
            logger.info("NayaPayId [" + wsmodel.getNayapayid() + "]");
        }

        if(Util.hasText(wsmodel.getParentid()))
        {
            logger.info("ParentId [" + wsmodel.getParentid() + "]");
        }

        if(Util.hasText(wsmodel.getMerchantname()))
        {
            logger.info("MerchantName [" + wsmodel.getMerchantname() + "]");
        }

        if(Util.hasText(wsmodel.getCategoryid()))
        {
            logger.info("CategoryId [" + wsmodel.getCategoryid() + "]");
        }

        if(Util.hasText(wsmodel.getTrustedflag()))
        {
            logger.info("TrustedFlag [" + wsmodel.getTrustedflag() + "]");
        }

        if(Util.hasText(wsmodel.getPhonenumber()))
        {
            logger.info("PhoneNumber [" + getFullMaskedValue(wsmodel.getPhonenumber()) + "]");
        }

        if(Util.hasText(wsmodel.getTransactionlimit()))
        {
            logger.info("Transactionlimit [" + wsmodel.getTransactionlimit() + "]");
        }

        if(Util.hasText(wsmodel.getCategoryname()))
        {
            logger.info("CategoryName [" + wsmodel.getCategoryname() + "]");
        }

        if(Util.hasText(wsmodel.getMerchantname()))
        {
            logger.info("MerchantName [" + wsmodel.getMerchantname() + "]");
        }

        if(Util.hasText(wsmodel.getMerchantenabled()))
        {
            logger.info("MerchantEnabled [" + wsmodel.getMerchantenabled() + "]");
        }

        if(Util.hasText(wsmodel.getMerchantblocked()))
        {
            logger.info("MerchantBlocked [" + wsmodel.getMerchantblocked() + "]");
        }

        if(Util.hasText(wsmodel.getMinimumamount()))
        {
            logger.info("MinimumAmount [" + wsmodel.getMinimumamount() + "]");
        }

        if(Util.hasText(wsmodel.getMaximumamount()))
        {
            logger.info("MaximumAmount [" + wsmodel.getMaximumamount() + "]");
        }

        if(Util.hasText(wsmodel.getSourcechargetype()))
        {
            logger.info("SourceChargeType [" + wsmodel.getSourcechargetype() + "]");
        }

        if(Util.hasText(wsmodel.getDestinationchargetype()))
        {
            logger.info("DestinationChargeType [" + wsmodel.getDestinationchargetype() + "]");
        }

        if(Util.hasText(wsmodel.getConsumerno()))
        {
            logger.info("ConsumerNo [" + wsmodel.getConsumerno() + "]");
        }

        if(Util.hasText(wsmodel.getUtilcompanyid()))
        {
            logger.info("UtilCompanyId [" + wsmodel.getUtilcompanyid() + "]");
        }

        if(Util.hasText(wsmodel.getConsumerdetail()))
        {
            logger.info("ConsumerDetail [" + wsmodel.getConsumerdetail() + "]");
        }

        if(Util.hasText(wsmodel.getBillstatus())) {
            logger.info("BillStatus [" + wsmodel.getBillstatus() + "]");
        }

        if(Util.hasText(wsmodel.getDuedate())) {
            logger.info("DueDate [" + wsmodel.getDuedate() + "]");
        }

        if(Util.hasText(wsmodel.getAmtwithinduedate())) {
            logger.info("AmtWithInDueDate [" + wsmodel.getAmtwithinduedate() + "]");
        }

        if(Util.hasText(wsmodel.getAmtafterduedate())) {
            logger.info("AmtAfterDueDate [" + wsmodel.getAmtafterduedate() + "]");
        }

        if(Util.hasText(wsmodel.getBillingmonth())) {
            logger.info("BillingMonth [" + wsmodel.getBillingmonth() + "]");
        }

        if(Util.hasText(wsmodel.getDatepaid())) {
            logger.info("DatePaid [" + wsmodel.getDatepaid() + "]");
        }

        if(Util.hasText(wsmodel.getAmtpaid())) {
            logger.info("AmtPaid [" + wsmodel.getAmtpaid() + "]");
        }

        if(Util.hasText(wsmodel.getTranauthid())) {
            logger.info("TranAuthId [" + wsmodel.getTranauthid() + "]");
        }

        if(Util.hasText(wsmodel.getReserved()))
        {
            logger.info("Reserved [" + wsmodel.getReserved() + "]");
        }

        if(Util.hasText(wsmodel.getIdentificationno()))
        {
            logger.info("IdentificationNo [" + wsmodel.getIdentificationno() + "]");
        }

        if(Util.hasText(wsmodel.getPing()))
        {
            logger.info("Ping [" + wsmodel.getPing() + "]");
        }

        if(Util.hasText(wsmodel.getNayapaytrantype()))
        {
            logger.info("NayapayTranType [" + wsmodel.getNayapaytrantype() + "]");
        }

        if(Util.hasText(wsmodel.getDestusername()))
        {
            logger.info("DestUserName [" + wsmodel.getDestusername() + "]");
        }

        if(Util.hasText(wsmodel.getAgentid()))
        {
            logger.info("AgentId [" + wsmodel.getAgentid() + "]");
        }

        if(Util.hasText(wsmodel.getReferencenumber()))
        {
            logger.info("ReferenceNumber [" + wsmodel.getReferencenumber() + "]");
        }

        if(Util.hasText(wsmodel.getInvoiceid()))
        {
            logger.info("InvoiceId [" + wsmodel.getInvoiceid() + "]");
        }

        if(Util.hasText(wsmodel.getVerifiedflag()))
        {
            logger.info("VerifiedFlag [" + wsmodel.getVerifiedflag() + "]");
        }

        if(Util.hasText(wsmodel.getStartdate()))
        {
            logger.info("StartDate [" + wsmodel.getStartdate() + "]");
        }

        if(Util.hasText(wsmodel.getEnddate()))
        {
            logger.info("EndDate [" + wsmodel.getEnddate() + "]");
        }

        if(Util.hasText(wsmodel.getBanktxnflag()))
        {
            logger.info("BankTxnFlag [" + wsmodel.getBanktxnflag() + "]");
        }

        if(Util.hasText(wsmodel.getBlockedflag()))
        {
            logger.info("BlockedFlag [" + wsmodel.getBlockedflag() + "]");
        }

        if(Util.hasText(wsmodel.getNayapaytxnid()))
        {
            logger.info("NayapayTxnId [" + wsmodel.getNayapaytxnid() + "]");
        }

        if(Util.hasText(wsmodel.getCreationdate()))
        {
            logger.info("CreationDate [" + wsmodel.getCreationdate() + "]");
        }

        if(Util.hasText(wsmodel.getBankmnemonic()))
        {
            logger.info("BankMnemonic [" + wsmodel.getBankmnemonic() + "]");
        }

        if(Util.hasText(wsmodel.getAcqbin()))
        {
            logger.info("AcqBin [" + wsmodel.getAcqbin() + "]");
        }

        if(Util.hasText(wsmodel.getBranchcode()))
        {
            logger.info("BranchCode [" + wsmodel.getBranchcode() + "]");
        }

        if(Util.hasText(wsmodel.getBranchname()))
        {
            logger.info("BranchName [" + wsmodel.getBranchname() + "]");
        }

        if(Util.hasText(wsmodel.getSlipnumber()))
        {
            logger.info("SlipNumber [" + wsmodel.getSlipnumber() + "]");
        }

        if(Util.hasText(wsmodel.getTellerid()))
        {
            logger.info("TellerId [" + wsmodel.getTellerid() + "]");
        }

        if(Util.hasText(wsmodel.getDepositamount()))
        {
            logger.info("DepositAmount [" + wsmodel.getDepositamount() + "]");
        }

        if(Util.hasText(wsmodel.getCardexpiry()))
        {
            logger.info("CardExpiry [" + wsmodel.getCardexpiry() + "]");
        }

        if (Util.hasText(wsmodel.getTotalamount())) {
            logger.info("Total Amount [" + wsmodel.getTotalamount() + "]");
        }

        if (Util.hasText(wsmodel.getBankcharges())) {
            logger.info("Bank Charges [" + wsmodel.getBankcharges() + "]");
        }

        if (Util.hasText(wsmodel.getBanktaxamount())) {
            logger.info("Bank Tax Amount [" + wsmodel.getBanktaxamount() + "]");
        }

        if (Util.hasText(wsmodel.getNayapaytaxamount())) {
            logger.info("NayaPay Tax Amount [" + wsmodel.getNayapaytaxamount() + "]");
        }

        if(Util.hasText(wsmodel.getPosentrymode()))
        {
            logger.info("PosEntryMode [" + wsmodel.getPosentrymode() + "]");
        }

        if(Util.hasText(wsmodel.getAmttranfee()))
        {
            logger.info("AmtTranFee [" + wsmodel.getAmttranfee() + "]");
        }

        if(Util.hasText(wsmodel.getSecretquestion1()))
        {
            logger.info("SecretQuestion1 [" + wsmodel.getSecretquestion1() + "]");
        }

        if(Util.hasText(wsmodel.getSecretquestionanswer1()))
        {
            logger.info("SecretQuestionAnswer1 [" + getFullMaskedValue(wsmodel.getSecretquestionanswer1()) + "]");
        }

        if(Util.hasText(wsmodel.getSecretquestion2()))
        {
            logger.info("SecretQuestion2 [" + wsmodel.getSecretquestion2() + "]");
        }

        if(Util.hasText(wsmodel.getSecretquestionanswer2()))
        {
            logger.info("SecretQuestionAnswer2 [" + getFullMaskedValue(wsmodel.getSecretquestionanswer2()) + "]");
        }

        if(Util.hasText(wsmodel.getCodflag()))
        {
            logger.info("CodFlag [" + wsmodel.getCodflag() + "]");
        }

        if(wsmodel.getNayapaylimits() != null)
        {
            logger.info("Nayapaylimits Received in Request..!");
        }

        if(wsmodel.getLinkedaccounts() != null)
        {
            logger.info("Linkedaccounts Received in Request..!");
        }

        if(wsmodel.getProvisionalwallets() != null)
        {
            logger.info("Provisionalwallets Received in Request..!");
        }

        if(wsmodel.getAccountlist() != null)
        {
            logger.info("Accountlist Received in Request..!");
        }

        if(wsmodel.getTransactions() != null)
        {
            logger.info("Transactions Received in Request..!");
        }

        if(wsmodel.getUsertransactions() != null)
        {
            logger.info("Usertransactions Received in Request..!");
        }

        if(wsmodel.getTransactionDetail() != null)
        {
            logger.info("TransactionDetail Received in Request..!");
        }

        if(Util.hasText(wsmodel.getCurrency()))
        {
            logger.info("Currency [" + wsmodel.getCurrency() + "]");
        }

        if(Util.hasText(wsmodel.getBank()))
        {
            logger.info("Bank [" + wsmodel.getBank() + "]");
        }

        if(Util.hasText(wsmodel.getDestbank()))
        {
            logger.info("Destbank [" + wsmodel.getDestbank() + "]");
        }

        if(Util.hasText(wsmodel.getCorebank()))
        {
            logger.info("Corebank [" + wsmodel.getCorebank() + "]");
        }

        if(Util.hasText(wsmodel.getDestcurrency()))
        {
            logger.info("Destcurrency [" + wsmodel.getDestcurrency() + "]");
        }

        if(Util.hasText(wsmodel.getCorecurrency()))
        {
            logger.info("Corecurrency [" + wsmodel.getCorecurrency() + "]");
        }

        if(Util.hasText(wsmodel.getTrack2Data()))
        {
            logger.info("Track2Data [" + getFullMaskedValue(wsmodel.getTrack2Data()) + "]");
        }

        if(Util.hasText(wsmodel.getTrack3Data()))
        {
            logger.info("Track3Data [" + getFullMaskedValue(wsmodel.getTrack3Data()) + "]");
        }

        if(Util.hasText(wsmodel.getTrack1Data()))
        {
            logger.info("Track1Data [" + getFullMaskedValue(wsmodel.getTrack1Data()) + "]");
        }

        if(Util.hasText(wsmodel.getIcccarddata()))
        {
            logger.info("ICCCardData [" + getFullMaskedValue(wsmodel.getIcccarddata()) + "]");
        }

        if(Util.hasText(wsmodel.getSettlementdelay()))
        {
            logger.info("SettlementDelay [" + wsmodel.getSettlementdelay() + "]");
        }

        if(Util.hasText(wsmodel.getPagecount()))
        {
            logger.info("PageCount [" + wsmodel.getPagecount() + "]");
        }

        if(Util.hasText(wsmodel.getPagesize()))
        {
            logger.info("PageSize [" + wsmodel.getPagesize() + "]");
        }

        if(Util.hasText(wsmodel.getTotalcount()))
        {
            logger.info("TotalCount [" + wsmodel.getTotalcount() + "]");
        }

        if(Util.hasText(wsmodel.getBillerid()))
        {
            logger.info("BillerId [" + wsmodel.getBillerid() + "]");
        }

        if(Util.hasText(wsmodel.getBillername()))
        {
            logger.info("BillerName [" + wsmodel.getBillername() + "]");
        }

        if(Util.hasText(wsmodel.getPartialflag()))
        {
            logger.info("PartialFlag [" + wsmodel.getPartialflag() + "]");
        }

        if(Util.hasText(wsmodel.getTempblockflag()))
        {
            logger.info("TempBlockFlag [" + wsmodel.getTempblockflag() + "]");
        }

        if(Util.hasText(wsmodel.getIncomingip()))
        {
            logger.info("IncomingIP [" + wsmodel.getIncomingip() + "]");
        }

		//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
        if(Util.hasText(wsmodel.getCardlastdigits()))
        {
            logger.info("CardNoLastDigits [" + wsmodel.getCardlastdigits() + "]");
        }
		//===============================================================================================================

        if(Util.hasText(wsmodel.getAcctStatus()))
        {
            logger.info("AcctStatus [" + wsmodel.getAcctStatus() + "]");
        }

        if (Util.hasText(wsmodel.getTermloc())) {
            logger.info("Termloc [" + wsmodel.getTermloc() + "]");
        }

        if(Util.hasText(wsmodel.getIsmerchantonline()))
        {
            logger.info("IsMerchantOnline [" + wsmodel.getIsmerchantonline() + "]");
        }

        if(Util.hasText(wsmodel.getIswalletaccount()))
        {
            logger.info("IsWalletAccount [" + wsmodel.getIswalletaccount() + "]");
        }

        if(Util.hasText(wsmodel.getMerchantamount()))
        {
            logger.info("MerchantAmount [" + wsmodel.getMerchantamount() + "]");
        }

        if(Util.hasText(wsmodel.getOriginalapi()))
        {
            logger.info("OriginalAPI [" + wsmodel.getOriginalapi() + "]");
        }

        if(Util.hasText(wsmodel.getServicecode()))
        {
            logger.info("ServiceCode [" + wsmodel.getServicecode() + "]");
        }

        if(Util.hasText(wsmodel.getCvv()))
        {
            logger.info("CVV [" + getFullMaskedValue(wsmodel.getCvv()) + "]");
        }

        if(Util.hasText(wsmodel.getCvv2()))
        {
            logger.info("CVV2 [" + getFullMaskedValue(wsmodel.getCvv2()) + "]");
        }

        if(Util.hasText(wsmodel.getIcvv()))
        {
            logger.info("ICCV [" + getFullMaskedValue(wsmodel.getIcvv()) + "]");
        }

        if(Util.hasText(wsmodel.getSelfdefinedata()))
        {
            logger.info("SelftDefinedData [" + getFullMaskedValue(wsmodel.getSelfdefinedata()) + "]");
        }

        if(Util.hasText(wsmodel.getDecryptedotp())) {
            logger.info("DecryptedOTP [" + wsmodel.getDecryptedotp() + "]");
        }

        if(Util.hasText(wsmodel.getCiphereddata())) {
            logger.info("CipheredData [" + wsmodel.getCiphereddata() + "]");
        }

        if(Util.hasText(wsmodel.getAcctlevel())) {
            logger.info("AcctLevel [" + wsmodel.getAcctlevel() + "]");
        }

        if(Util.hasText(wsmodel.getTrackingid())) {
            logger.info("TrackingId [" + wsmodel.getTrackingid() + "]");
        }

        if(Util.hasText(wsmodel.getMapid())) {
            logger.info("MapId [" + wsmodel.getMapid() + "]");
        }

        if(Util.hasText(wsmodel.getPosinvoiceref())) {
            logger.info("POSInvoiceRef [" + wsmodel.getPosinvoiceref() + "]");
        }

        if(Util.hasText(wsmodel.getDisputeflag())) {
            logger.info("DisputeFlag [" + wsmodel.getDisputeflag() + "]");
        }

        if(Util.hasText(wsmodel.getBlocktype())) {
            logger.info("BlockType [" + wsmodel.getBlocktype() + "]");
        }

        if(Util.hasText(wsmodel.getTrancurrency())) {
            logger.info("TranCurrency [" + wsmodel.getTrancurrency() + "]");
        }

        if(Util.hasText(wsmodel.getLockstate())) {
            logger.info("LockState [" + wsmodel.getLockstate() + "]");
        }

        if(Util.hasText(wsmodel.getBranchname())) {
            logger.info("BranchName [" + wsmodel.getBranchname() + "]");
        }

        if(Util.hasText(wsmodel.getBranchcode())) {
            logger.info("BranchCode [" + wsmodel.getBranchcode() + "]");
        }

        if(Util.hasText(wsmodel.getFinancialflag())) {
            logger.info("FinancialFlag [" + wsmodel.getFinancialflag() + "]");
        }

        if (Util.hasText(wsmodel.getBalanceinquirycharges())) {
            logger.info("Balance inquiry charges [" + wsmodel.getBalanceinquirycharges() + "]");
        }

        if (Util.hasText(wsmodel.getReceiptcharges())) {
            logger.info("Receipt charges [" + wsmodel.getReceiptcharges() + "]");
        }

        if(wsmodel.getSecurityparams() != null)
        {
            //wsmodel.getSecurityparams().setId(null); //Raza When receiving Object from Switch
            logger.info("Securityparams Received in Request..!");

            if(Util.hasText(wsmodel.getSecurityparams().getFirebasetoken()))
            {
                logger.info("SecurityParam-Firebasetoken [" + wsmodel.getSecurityparams().getFirebasetoken() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getDevicemodel()))
            {
                logger.info("SecurityParam-Devicemodel [" + wsmodel.getSecurityparams().getDevicemodel() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getOperatingsystem()))
            {
                logger.info("SecurityParam-Operatingsystem [" + wsmodel.getSecurityparams().getOperatingsystem() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getScreenresolution()))
            {
                logger.info("SecurityParam-Screenresolution [" + wsmodel.getSecurityparams().getScreenresolution() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getGpslatitude()))
            {
                logger.info("SecurityParam-GpsLatitude [" + wsmodel.getSecurityparams().getGpslatitude() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getGpslongitude()))
            {
                logger.info("SecurityParam-GpsLongitude [" + wsmodel.getSecurityparams().getGpslongitude() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getRootedflag()))
            {
                logger.info("SecurityParam-Rootedflag [" + wsmodel.getSecurityparams().getRootedflag() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getBaseintegrityflag()))
            {
                logger.info("SecurityParam-Baseintegrityflag [" + wsmodel.getSecurityparams().getBaseintegrityflag() + "]");
            }

            if(Util.hasText(wsmodel.getSecurityparams().getCtsprofileflag()))
            {
                logger.info("SecurityParam-Ctsprofileflag [" + wsmodel.getSecurityparams().getCtsprofileflag() + "]");
            }

        }

        if(Util.hasText(wsmodel.getOrigstan())) {
            logger.info("origStan [" + wsmodel.getOrigstan() + "]");
        }

        if(Util.hasText(wsmodel.getOrigtransdatetime())) {
            logger.info("origTransDateTime [" + wsmodel.getOrigtransdatetime() + "]");
        }

        if(Util.hasText(wsmodel.getOccupation())) {
            logger.info("Occupation [" + wsmodel.getOccupation() + "]");
        }

        if(Util.hasText(wsmodel.getNpappdownloadcount())) {
            logger.info("AppDownloadCount [" + wsmodel.getNpappdownloadcount() + "]");
        }

        if(Util.hasText(wsmodel.getAvailablebaldebitacc())) {
            logger.info("AvailableBalDebitAcc [" + wsmodel.getAvailablebaldebitacc() + "]");
        }

        if(Util.hasText(wsmodel.getActualbaldebitacc())) {
            logger.info("ActualBalDebitAcc [" + wsmodel.getActualbaldebitacc() + "]");
        }

        if(Util.hasText(wsmodel.getAvailablebalcreditacc())) {
            logger.info("AvailableBalCreditAcc [" + wsmodel.getAvailablebalcreditacc() + "]");
        }

        if(Util.hasText(wsmodel.getActualbalcreditacc())) {
            logger.info("getActualBalCreditAcc [" + wsmodel.getActualbalcreditacc() + "]");
        }

        if(Util.hasText(wsmodel.getFundsvoucherid())) {
            logger.info("FundsVoucherId [" + wsmodel.getFundsvoucherid() + "]");
        }

        // Asim Shahzad, Date : 2nd Feb 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
        if (Util.hasText(wsmodel.getCardtype())) {
            logger.info("CardType [" + wsmodel.getCardtype() + "]");
        }
        // ======================================================================================================================

        // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115
        if (Util.hasText(wsmodel.getTotalamountreceived())) {
            logger.info("TotalAmountReceived [" + wsmodel.getTotalamountreceived() + "]");
        }
        if (Util.hasText(wsmodel.getTotalamountspent())) {
            logger.info("TotalAmountSpent [" + wsmodel.getTotalamountspent() + "]");
        }
        // =======================================================================================

        if(Util.hasText(wsmodel.getCavvdata()))
        {
            logger.info("CAVV [" + getFullMaskedValue(wsmodel.getCavvdata()) + "]");
        }

        if (Util.hasText(wsmodel.getCbillamount())) {
            logger.info("CbillAmount [" + wsmodel.getCbillamount() + "]");
        }

        if (Util.hasText(wsmodel.getCbillcurrency())) {
            logger.info("CbillCurrency [" + wsmodel.getCbillcurrency() + "]");
        }

        if (Util.hasText(wsmodel.getCbillrate())) {
            logger.info("CbillRate [" + wsmodel.getCbillrate() + "]");
        }

        if (Util.hasText(wsmodel.getBenebankcode())) {
            logger.info("BeneBankCode [" + wsmodel.getBenebankcode() + "]");
        }

        if (Util.hasText(wsmodel.getBenebankaccountno())) {
            logger.info("BeneBankAccountNo [" + WebServiceUtil.getFullMaskedValue(wsmodel.getBenebankaccountno()) + "]");
        }

        if (Util.hasText(wsmodel.getBeneaccounttitle())) {
            logger.info("BeneAccountTitle [" + wsmodel.getBeneaccounttitle() + "]");
        }

        if (Util.hasText(wsmodel.getBeneflag())) {
            logger.info("BeneFlag [" + wsmodel.getBeneflag() + "]");
        }

        if (Util.hasText(wsmodel.getBeneaccountalias())) {
            logger.info("BeneAcctAlias [" + wsmodel.getBeneaccountalias() + "]");
        }

        if (Util.hasText(wsmodel.getBeneid())) {
            logger.info("BeneID [" + wsmodel.getBeneid() + "]");
        }

        if (Util.hasText(wsmodel.getBeneemailid())) {
            logger.info("BeneEmailID [" + wsmodel.getBeneemailid() + "]");
        }

        if (Util.hasText(wsmodel.getBenemobileno())) {
            logger.info("BeneMobileNo [" + wsmodel.getBenemobileno() + "]");
        }

        if (Util.hasText(wsmodel.getPurposeoftransaction())) {
            logger.info("PurposeofTransaction [" + wsmodel.getPurposeoftransaction() + "]");
        }

        if (Util.hasText(wsmodel.getCardscheme())) {
            logger.info("CardScheme [" + wsmodel.getCardscheme() + "]");
        }

        if (Util.hasText(wsmodel.getOrigretrefno())) {
            logger.info("OrigRetRefNo [" + wsmodel.getOrigretrefno() + "]");
        }

        if (Util.hasText(wsmodel.getNpticket())) {
            logger.info("NpTicket [" + wsmodel.getNpticket() + "]");
        }

        if (Util.hasText(wsmodel.getVrolticket())) {
            logger.info("VrolTicket [" + wsmodel.getVrolticket() + "]");
        }

        if (Util.hasText(wsmodel.getIsChipPinEnabled())) {
            logger.info("IsChipPinEnabled [" + wsmodel.getIsChipPinEnabled() + "]");
        }

        if (Util.hasText(wsmodel.getIsMagStripeEnabled())) {
            logger.info("IsMagStripeEnabled [" + wsmodel.getIsMagStripeEnabled() + "]");
        }

        if (Util.hasText(wsmodel.getIsCashWithdrawalEnabled())) {
            logger.info("IsCashWithdrawalEnabled [" + wsmodel.getIsCashWithdrawalEnabled() + "]");
        }

        if (Util.hasText(wsmodel.getIsNFCEnabled())) {
            logger.info("IsNFCEnabled [" + wsmodel.getIsNFCEnabled() + "]");
        }

        if (Util.hasText(wsmodel.getIsOnlineEnabled())) {
            logger.info("IsOnlineEnabled [" + wsmodel.getIsOnlineEnabled() + "]");
        }

        if (Util.hasText(wsmodel.getIsInternationalTxnsEnabled())) {
            logger.info("IsInternationalTxnsEnabled [" + wsmodel.getIsInternationalTxnsEnabled() + "]");
        }

        if (Util.hasText(wsmodel.getGlobalcardlimit())) {
            logger.info("GlobalCardLimit [" + wsmodel.getGlobalcardlimit() + "]");
        }

        if (Util.hasText(wsmodel.getCashwithdrawallimit())) {
            logger.info("CashWithDrawalLimit [" + wsmodel.getCashwithdrawallimit() + "]");
        }

        if (Util.hasText(wsmodel.getPurchaselimit())) {
            logger.info("PurchaseLimit [" + wsmodel.getPurchaselimit() + "]");
        }

        if (Util.hasText(wsmodel.getOnlinetxnlimit())) {
            logger.info("OnlineTxnLimit [" + wsmodel.getOnlinetxnlimit() + "]");
        }

        if (Util.hasText(wsmodel.getAddresponsedata())) {
            logger.info("AddResponseData [" + wsmodel.getAddresponsedata() + "]");
        }

        if (Util.hasText(wsmodel.getCustomlimitflag())) {
            logger.info("CustomLimitFlag [" + wsmodel.getCustomlimitflag() + "]");
        }

        if(wsmodel.getWsloglist() != null)
        {
            logger.info("WsLogList Received in Request..!");
        }

        if(wsmodel.getWslog() != null)
        {
            logger.info("WsLog Received in Request..!");
        }

        if (Util.hasText(wsmodel.getReasonofclosure())) {
            logger.info("ReasonofClosure [" + wsmodel.getReasonofclosure() + "]");
        }

        if (Util.hasText(wsmodel.getApprovinguser())) {
            logger.info("ApprovingUser [" + wsmodel.getApprovinguser() + "]");
        }

        if (Util.hasText(wsmodel.getClosurerequestdatetime())) {
            logger.info("ClosureRequestDateTime [" + wsmodel.getClosurerequestdatetime() + "]");
        }

        if (Util.hasText(wsmodel.getFromdatetime())) {
            logger.info("FromDateTime [" + wsmodel.getFromdatetime() + "]");
        }

        if (Util.hasText(wsmodel.getTodatetime())) {
            logger.info("ToDateTime [" + wsmodel.getTodatetime() + "]");
        }

        if (Util.hasText(wsmodel.getSettledflag())) {
            logger.info("SettledFlag [" + wsmodel.getSettledflag() + "]");
        }

        if (Util.hasText(wsmodel.getMerchantfavorflag())) {
            logger.info("MerchantFavorFlag [" + wsmodel.getMerchantfavorflag() + "]");
        }

        if (Util.hasText(wsmodel.getDebitwalletflag())) {
            logger.info("DebitWalletFlag [" + wsmodel.getDebitwalletflag() + "]");
        }

        if (Util.hasText(wsmodel.getCreditwalletflag())) {
            logger.info("CreditWalletFlag [" + wsmodel.getCreditwalletflag() + "]");
        }

        if (Util.hasText(wsmodel.getJustification())) {
            logger.info("Justification [" + wsmodel.getJustification() + "]");
        }

        if(wsmodel.getCardchargeslist() != null)
        {
            logger.info("CardChargesList Received in Request..!");
        }

        if (Util.hasText(wsmodel.getSettlementamount())) {
            logger.info("SettlementAmount [" + wsmodel.getSettlementamount() + "]");
        }

        if (Util.hasText(wsmodel.getNameoncard())) {
            logger.info("NameOnCard [" + wsmodel.getNameoncard() + "]");
        }

        if (Util.hasText(wsmodel.getSettlementrate())) {
            logger.info("SettlementRate [" + wsmodel.getSettlementrate() + "]");
        }

        if (Util.hasText(wsmodel.getWithholdingtaxamount())) {
            logger.info("WithholdingTaxAmount [" + wsmodel.getWithholdingtaxamount() + "]");
        }

        if(Util.hasText(wsmodel.getIban()))
        {
            logger.info("Iban [" + getFullMaskedValue(wsmodel.getIban()) + "]");
        }

        if (Util.hasText(wsmodel.getMonthstartingbalance())) {
            logger.info("MonthStartingBalance [" + wsmodel.getMonthstartingbalance() + "]");
        }

        if (Util.hasText(wsmodel.getMonthendingbalance())) {
            logger.info("MonthEndingBalance [" + wsmodel.getMonthendingbalance() + "]");
        }

        if (Util.hasText(wsmodel.getAcqcountrycode())) {
            logger.info("AcqCountryCode [" + wsmodel.getAcqcountrycode() + "]");
        }

        if (Util.hasText(wsmodel.getOrigChannelId())) {
            logger.info("OrigChannelId [" + wsmodel.getOrigChannelId() + "]");
        }

        if (Util.hasText(wsmodel.getReserved3())) {
            logger.info("Reserved3 [" + wsmodel.getReserved3() + "]");
        }
    }



//    public static void PrintNayaPayMsg(WalletCMSWsEntity wsmodel, boolean isrequest)
//    {
////        GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
////        //GeneralDao.Instance.saveOrUpdate(wsmodel);
////        if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) {
////            GeneralDao.Instance.saveOrUpdate(wsmodel);
////            GeneralDao.Instance.commit();
////        }
////        else
////        {
////            GeneralDao.Instance.getCurrentSession().beginTransaction();
////            GeneralDao.Instance.saveOrUpdate(wsmodel);
////            GeneralDao.Instance.commit();
////        }
//        //if (wsmodel != null) {
//        //isrequest = true;
//        if(isrequest) {
//            //WalletCMSWsEntity nayaPayWsEntity = new WalletCMSWsEntity();
//            logger.info("********************Request********************");
//
//            if (Util.hasText(wsmodel.getServicename())) {
//                logger.info("ServiceName [" + wsmodel.getServicename() + "]");
//                //nayaPayWsEntity.setServicename(wsmodel.getServicename());
//            }
//
//            if (Util.hasText(wsmodel.getMerchantid())) {
//                logger.info("MerchantId [" + wsmodel.getMerchantid() + "]");
//                //nayaPayWsEntity.setNayapayid(wsmodel.getNayapayid());
//            }
//
//            if (Util.hasText(wsmodel.getMobilenumber())) {
//                logger.info("Mobile Num [" + wsmodel.getMobilenumber() + "]");
//                //nayaPayWsEntity.setMobilenumber(wsmodel.getMobilenumber());
//            }
//
//            if (Util.hasText(wsmodel.getCnic())) {
//                logger.info("CNIC [" + wsmodel.getCnic() + "]");
//                //nayaPayWsEntity.setCnic(wsmodel.getCnic());
//            }
//
//            if (Util.hasText(wsmodel.getBankcode())) {
//                logger.info("BankCode [" + wsmodel.getBankcode() + "]");
//                //nayaPayWsEntity.setBankcode(wsmodel.getBankcode());
//            }
//
//            if (Util.hasText(wsmodel.getBankname())) {
//                logger.info("BankName [" + wsmodel.getBankname() + "]");
//                //nayaPayWsEntity.setBankname(wsmodel.getBankname());
//            }
//
//            if (Util.hasText(wsmodel.getAccountnumber())) {
//                logger.info("AccountNumber [" + wsmodel.getAccountnumber() + "]");
//                //nayaPayWsEntity.setAccountnumber(wsmodel.getAccountnumber());
//            }
//
//            if (Util.hasText(wsmodel.getAccountcurrency())) {
//                logger.info("AccountCurrency [" + wsmodel.getAccountcurrency() + "]");
//                //nayaPayWsEntity.setAccountcurrency(wsmodel.getAccountcurrency());
//            }
//
//            if (Util.hasText(wsmodel.getTranrefnumber())) {
//                logger.info("TransactionReferenceNo [" + wsmodel.getTranrefnumber() + "]");
//                //nayaPayWsEntity.setTranrefnumber(wsmodel.getTranrefnumber());
//            }
//
//            if (wsmodel.getCnicpicturefront() != null) {
//                logger.info("CNIC picture Front [" + wsmodel.getCnicpicturefront() + "]");
//            }
//
//            if (wsmodel.getCnicpictureback() != null) {
//                logger.info("CNIC picture Back [" + wsmodel.getCnicpictureback() + "]");
//            }
//
//            if (wsmodel.getPlaceofbirth() != null) {
//                logger.info("Place of Birth [" + wsmodel.getPlaceofbirth() + "]");
//            }
//
//            if (wsmodel.getCnicexpiry() != null) {
//                logger.info("Cnic Expiry [" + wsmodel.getCnicexpiry() + "]");
//            }
//
//            if (wsmodel.getFathername() != null) {
//                logger.info("Father/Husband name [" + wsmodel.getFathername() + "]");
//            }
//
//            if (wsmodel.getProvince() != null) {
//                logger.info("Province [" + wsmodel.getProvince() + "]");
//            }
//
//            if (wsmodel.getTsp() != null) {
//                logger.info("Telecom Service Provider [" + wsmodel.getTsp() + "]");
//            }
//
//            if (wsmodel.getCustomerpicture() != null) {
//                logger.info("Customer picture [" + wsmodel.getCustomerpicture() + "]");
//                //nayaPayWsEntity.setCustomerpicture(wsmodel.getCustomerpicture());
//            }
//
//            if (Util.hasText(wsmodel.getMothername())) {
//                logger.info("Mother Name [" + wsmodel.getMothername() + "]");
//                //nayaPayWsEntity.setMothername(wsmodel.getMothername());
//            }
//
//            if (Util.hasText(wsmodel.getDateofbirth())) {
//                logger.info("Date of Birth [" + wsmodel.getDateofbirth() + "]");
//                //nayaPayWsEntity.setDateofbirth(wsmodel.getDateofbirth());
//            }
//
//            if (Util.hasText(wsmodel.getTransdatetime())) {
//                logger.info("TransmissionDateTime [" + wsmodel.getTransdatetime() + "]");
//                //nayaPayWsEntity.setTransdatetime(wsmodel.getTransdatetime());
//            }
//
//            if (Util.hasText(wsmodel.getPindata())) {
//                logger.info("PinData [" + wsmodel.getPindata() + "]");
//                //nayaPayWsEntity.setPindata(wsmodel.getPindata());
//            }
//
//            if (Util.hasText(wsmodel.getOldpindata())) {
//                logger.info("OldPinData [" + wsmodel.getOldpindata() + "]");
//                //nayaPayWsEntity.setOldpindata(wsmodel.getOldpindata());
//            }
//
//            if (Util.hasText(wsmodel.getNewpindata())) {
//                logger.info("NewPinData [" + wsmodel.getNewpindata() + "]");
//                //nayaPayWsEntity.setNewpindata(wsmodel.getNewpindata());
//            }
//
//            if (Util.hasText(wsmodel.getEncryptkey())) {
//                logger.info("EncryptKey [" + wsmodel.getEncryptkey() + "]");
//                //nayaPayWsEntity.setEncryptkey(wsmodel.getEncryptkey());
//            }
//
//            if (Util.hasText(wsmodel.getAmounttransaction())) {
//                logger.info("Amount Txn [" + wsmodel.getAmounttransaction() + "]");
//                //nayaPayWsEntity.setAmounttransaction(wsmodel.getAmounttransaction());
//            }
//
//            if (Util.hasText(wsmodel.getSrcchargeamount())) {
//                logger.info("Source Amount Charges [" + wsmodel.getSrcchargeamount() + "]");
//                //nayaPayWsEntity.setAmounttranfee(wsmodel.getAmounttranfee());
//            }
//
//            if (Util.hasText(wsmodel.getDestchargeamount())) {
//                logger.info("Destination Amount Charges [" + wsmodel.getDestchargeamount() + "]");
//                //nayaPayWsEntity.setAmounttranfee(wsmodel.getAmounttranfee());
//            }
//
////            if (Util.hasText(wsmodel.getChargeflag())) {
////                logger.info("Charge Flag [" + wsmodel.getChargeflag() + "]");
////                //nayaPayWsEntity.setChargeflag(wsmodel.getChargeflag());
////            }
//
////            if (Util.hasText(wsmodel.getSrcaccount())) {
////                logger.info("Src Account [" + wsmodel.getSrcaccount() + "]");
////                //nayaPayWsEntity.setSrcaccount(wsmodel.getSrcaccount());
////            }
//
////            if (Util.hasText(wsmodel.getSrcaccountcode())) {
////                logger.info("Src Account Code [" + wsmodel.getSrcaccountcode() + "]");
////                //nayaPayWsEntity.setSrcaccountcode(wsmodel.getSrcaccountcode());
////            }
//
////            if (Util.hasText(wsmodel.getSrcaccountcurrency())) {
////                logger.info("Src Account Currency [" + wsmodel.getSrcaccountcurrency() + "]");
////                //nayaPayWsEntity.setSrcaccountcurrency(wsmodel.getSrcaccountcurrency());
////            }
//
//            if (Util.hasText(wsmodel.getDestaccount())) {
//                logger.info("Dest Account [" + wsmodel.getDestaccount() + "]");
//                //nayaPayWsEntity.setDestaccount(wsmodel.getDestaccount());
//            }
//
////            if (Util.hasText(wsmodel.getDestaccountcode())) {
////                logger.info("Dest Account Code [" + wsmodel.getDestaccountcode() + "]");
////                //nayaPayWsEntity.setDestaccountcode(wsmodel.getDestaccountcode());
////            }
//
//            if (Util.hasText(wsmodel.getDestaccountcurrency())) {
//                logger.info("Dest Account Currency [" + wsmodel.getDestaccountcurrency() + "]");
//                //nayaPayWsEntity.setDestaccountcurrency(wsmodel.getDestaccountcurrency());
//            }
//
//            if (Util.hasText(wsmodel.getOtp())) {
//                logger.info("OTP [" + wsmodel.getOtp() + "]");
//                //nayaPayWsEntity.setOtp(wsmodel.getOtp());
//            }
//
////            if (Util.hasText(wsmodel.getMpin())) {
////                logger.info("MPIN [" + wsmodel.getMpin() + "]");
////                //nayaPayWsEntity.setMpin(wsmodel.getMpin());
////            }
////
////            if (Util.hasText(wsmodel.getSrcSettaccountnumber())) {
////                logger.info("Src Sett Account [" + wsmodel.getSrcSettaccountnumber() + "]");
////                //nayaPayWsEntity.setSrcSettaccountnumber(wsmodel.getSrcSettaccountnumber());
////            }
////
////            if (Util.hasText(wsmodel.getDestSettaccountnumber())) {
////                logger.info("Dest Sett Account [" + wsmodel.getDestSettaccountnumber() + "]");
////                //nayaPayWsEntity.setDestSettaccountnumber(wsmodel.getDestSettaccountnumber());
////            }
//
////            if (Util.hasText(wsmodel.getWalletaccountnumber())) {
////                logger.info("Wallet Account Num [" + wsmodel.getWalletaccountnumber() + "]");
////                //nayaPayWsEntity.setWalletaccountnumber(wsmodel.getWalletaccountnumber());
////            }
//
////            if (Util.hasText(wsmodel.getWalletaccountnumber())) {
////                logger.info("Wallet Account Currency [" + wsmodel.getWalletaccountcurrency() + "]");
////                //nayaPayWsEntity.setWalletaccountcurrency(wsmodel.getWalletaccountcurrency());
////            }
//
//            if (Util.hasText(wsmodel.getBiometricdata())) {
//                logger.info("BioMetric Data [" + wsmodel.getBiometricdata() + "]");
//                //nayaPayWsEntity.setBiometricdata(wsmodel.getBiometricdata());
//            }
//
//            if (wsmodel.getTranlist() != null) {
//                logger.info("TranList Received in Request..!");
//                //nayaPayWsEntity.setTranlist(wsmodel.getTranlist());
//            }
//
//            if (Util.hasText(wsmodel.getRespcode())) {
//                logger.info("Response Code [" + wsmodel.getRespcode() + "]");
//                //nayaPayWsEntity.setRespcode(wsmodel.getRespcode());
//            }
//
//            if (wsmodel.getBankform() != null) {
//                logger.info("Bank Form Received in Request..!");
//                //nayaPayWsEntity.setBankform(wsmodel.getBankform());
//            }
//
//            if (Util.hasText(wsmodel.getOrigdataelement())) {
//                logger.info("Orig Data Element [" + wsmodel.getOrigdataelement() + "]");
//                //nayaPayWsEntity.setBankform(wsmodel.getBankform());
//            }
//
//            if (Util.hasText(wsmodel.getAtmid())) {
//                logger.info("ATM ID [" + wsmodel.getAtmid() + "]");
//                //nayaPayWsEntity.setBankform(wsmodel.getBankform());
//            }
//
//            if (Util.hasText(wsmodel.getCorebankcode())) {
//                logger.info("Core Bank Code [" + wsmodel.getCorebankcode() + "]");
//                //nayaPayWsEntity.setBankform(wsmodel.getBankform());
//            }
//
//            if (Util.hasText(wsmodel.getCoreaccount())) {
//                logger.info("Core Account number [" + wsmodel.getCoreaccount() + "]");
//                //nayaPayWsEntity.setBankform(wsmodel.getBankform());
//            }
//
//            if (Util.hasText(wsmodel.getCoreaccountcurrency())) {
//                logger.info("Core Account Currency [" + wsmodel.getCoreaccountcurrency() + "]");
//                //nayaPayWsEntity.setBankform(wsmodel.getBankform());
//            }
//
//            if(Util.hasText(wsmodel.getMerchantid()))
//            {
//                logger.info("Merchant Id [" + wsmodel.getMerchantid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getDailylimit()))
//            {
//                logger.info("Daily Limit [" + wsmodel.getDailylimit() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getMonthlylimit()))
//            {
//                logger.info("Monthly Limit [" + wsmodel.getMonthlylimit() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getYearlylimit()))
//            {
//                logger.info("Yearly Limit [" + wsmodel.getYearlylimit() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getCardnumber()))
//            {
//                logger.info("Card Number [" + wsmodel.getCardnumber() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getCardpindata()))
//            {
//                logger.info("Card Pin Data [" + wsmodel.getCardpindata() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getEnableflag()))
//            {
//                logger.info("Card Enable Flag [" + wsmodel.getEnableflag() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getChannelid()))
//            {
//                logger.info("ChannelId [" + wsmodel.getChannelid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAllowed()))
//            {
//                logger.info("AllowedFlag [" + wsmodel.getAllowed() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getBillstatus())) {
//                logger.info("BillStatus [" + wsmodel.getBillstatus() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getDuedate())) {
//                logger.info("DueDate [" + wsmodel.getDuedate() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAmtwithinduedate())) {
//                logger.info("AmtWithInDueDate [" + wsmodel.getAmtwithinduedate() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAmtafterduedate())) {
//                logger.info("AmtAfterDueDate [" + wsmodel.getAmtafterduedate() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getBillingmonth())) {
//                logger.info("BillingMonth [" + wsmodel.getBillingmonth() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getDatepaid())) {
//                logger.info("DatePaid [" + wsmodel.getDatepaid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAmtpaid())) {
//                logger.info("AmtPaid [" + wsmodel.getAmtpaid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getTranauthid())) {
//                logger.info("TranAuthId [" + wsmodel.getTranauthid() + "]");
//            }
//
//            logger.info("********************Request********************");
//
////            if (Util.hasText(wsmodel.getTxnfeetype())) {
////                logger.info("Fee Type [" + wsmodel.getTxnfeetype() + "]");
////                //nayaPayWsEntity.setTxnfeetype(wsmodel.getTxnfeetype());
////            }
//
////            } else {
////                logger.error("wsmodel object not found.. reject txn");
////            }
//
//            //GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
//
//            wsmodel.setId(null);
//            GeneralDao.Instance.saveOrUpdate(wsmodel);
//            GeneralDao.Instance.flush(); //Raza Adding for Unique TxnRefNum and other Constraints for WSMODEL class
//            //GeneralDao.Instance.commit(); //Raza Testing for getiing constraint wxception for same txnrefnum & transdatetime composite key
//            logger.info("Request msg saved in Db..!");
//            //////wsmodel.setId(wsmodel.getId()+"");
//            //////GeneralDao.Instance.saveOrUpdate(nayaPayWsEntity);
//            //////wsmodel.setId(nayaPayWsEntity.getId()+"");
//            //GeneralDao.Instance.commit();
//        }
//        else
//        {
//
//            logger.info("********************Response********************");
//
//            if (Util.hasText(wsmodel.getServicename())) {
//                logger.info("ServiceName [" + wsmodel.getServicename() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getMerchantid())) {
//                logger.info("MerchantId [" + wsmodel.getMerchantid() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getMobilenumber())) {
//                logger.info("Mobile Num [" + wsmodel.getMobilenumber() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getCnic())) {
//                logger.info("CNIC [" + wsmodel.getCnic() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getBankcode())) {
//                logger.info("BankCode [" + wsmodel.getBankcode() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getBankname())) {
//                logger.info("BankName [" + wsmodel.getBankname() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getAccountnumber())) {
//                logger.info("AccountNumber [" + wsmodel.getAccountnumber() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getAccountcurrency())) {
//                logger.info("AccountCurrency [" + wsmodel.getAccountcurrency() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getTranrefnumber())) {
//                logger.info("TransactionReferenceNo [" + wsmodel.getTranrefnumber() + "]");
//            }
//
//            if (wsmodel.getCnicpicturefront() != null) {
//                logger.info("CNIC picture Front [" + wsmodel.getCnicpicturefront() + "]");
//            }
//
//            if (wsmodel.getCnicpictureback() != null) {
//                logger.info("CNIC picture Back [" + wsmodel.getCnicpictureback() + "]");
//            }
//
//            if (wsmodel.getPlaceofbirth() != null) {
//                logger.info("Place of Birth [" + wsmodel.getPlaceofbirth() + "]");
//            }
//
//            if (wsmodel.getCnicexpiry() != null) {
//                logger.info("Cnic Expiry [" + wsmodel.getCnicexpiry() + "]");
//            }
//
//            if (wsmodel.getFathername() != null) {
//                logger.info("Father/Husband name [" + wsmodel.getFathername() + "]");
//            }
//
//            if (wsmodel.getProvince() != null) {
//                logger.info("Province [" + wsmodel.getProvince() + "]");
//            }
//
//            if (wsmodel.getTsp() != null) {
//                logger.info("Telecom Service Provider [" + wsmodel.getTsp() + "]");
//            }
//
//            if (wsmodel.getCustomerpicture() != null) {
//                logger.info("Customer picture [" + wsmodel.getCustomerpicture() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getMothername())) {
//                logger.info("Mother Name [" + wsmodel.getMothername() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getDateofbirth())) {
//                logger.info("Date of Birth [" + wsmodel.getDateofbirth() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getTransdatetime())) {
//                logger.info("TransmissionDateTime [" + wsmodel.getTransdatetime() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getPindata())) {
//                logger.info("PinData [" + wsmodel.getPindata() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getOldpindata())) {
//                logger.info("OldPinData [" + wsmodel.getOldpindata() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getNewpindata())) {
//                logger.info("NewPinData [" + wsmodel.getNewpindata() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getEncryptkey())) {
//                logger.info("EncryptKey [" + wsmodel.getEncryptkey() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getAmounttransaction())) {
//                logger.info("Amount Txn [" + wsmodel.getAmounttransaction() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getSrcchargeamount())) {
//                logger.info("Source Amount Charges [" + wsmodel.getSrcchargeamount() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getDestchargeamount())) {
//                logger.info("Destination Amount Charges [" + wsmodel.getDestchargeamount() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getDestaccount())) {
//                logger.info("Dest Account [" + wsmodel.getDestaccount() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getDestaccountcurrency())) {
//                logger.info("Dest Account Currency [" + wsmodel.getDestaccountcurrency() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getOtp())) {
//                logger.info("OTP [" + wsmodel.getOtp() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getBiometricdata())) {
//                logger.info("BioMetric Data [" + wsmodel.getBiometricdata() + "]");
//            }
//
//            if (wsmodel.getTranlist() != null) {
//                logger.info("TranList Received in Request..!");
//            }
//
//            if (Util.hasText(wsmodel.getRespcode())) {
//                logger.info("Response Code [" + wsmodel.getRespcode() + "]");
//            }
//
//            if (wsmodel.getBankform() != null) {
//                logger.info("Bank Form Received in Request..!");
//            }
//
//            if (Util.hasText(wsmodel.getOrigdataelement())) {
//                logger.info("Orig Data Element [" + wsmodel.getOrigdataelement() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getAtmid())) {
//                logger.info("ATM ID [" + wsmodel.getAtmid() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getCorebankcode())) {
//                logger.info("Core Bank Code [" + wsmodel.getCorebankcode() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getCoreaccount())) {
//                logger.info("Core Account number [" + wsmodel.getCoreaccount() + "]");
//            }
//
//            if (Util.hasText(wsmodel.getCoreaccountcurrency())) {
//                logger.info("Core Account Currency [" + wsmodel.getCoreaccountcurrency() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getMerchantid()))
//            {
//                logger.info("Merchant Id [" + wsmodel.getMerchantid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getDailylimit()))
//            {
//                logger.info("Daily Limit [" + wsmodel.getDailylimit() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getMonthlylimit()))
//            {
//                logger.info("Monthly Limit [" + wsmodel.getMonthlylimit() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getYearlylimit()))
//            {
//                logger.info("Yearly Limit [" + wsmodel.getYearlylimit() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getCardnumber()))
//            {
//                logger.info("Card Number [" + wsmodel.getCardnumber() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getCardpindata()))
//            {
//                logger.info("Card Pin Data [" + wsmodel.getCardpindata() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getEnableflag()))
//            {
//                logger.info("Card Enable Flag [" + wsmodel.getEnableflag() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getStatus()))
//            {
//                logger.info("Status [" + wsmodel.getStatus() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getChannelid()))
//            {
//                logger.info("ChannelId [" + wsmodel.getChannelid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAllowed()))
//            {
//                logger.info("AllowedFlag [" + wsmodel.getAllowed() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getBillstatus())) {
//                logger.info("BillStatus [" + wsmodel.getBillstatus() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getDuedate())) {
//                logger.info("DueDate [" + wsmodel.getDuedate() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAmtwithinduedate())) {
//                logger.info("AmtWithInDueDate [" + wsmodel.getAmtwithinduedate() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAmtafterduedate())) {
//                logger.info("AmtAfterDueDate [" + wsmodel.getAmtafterduedate() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getBillingmonth())) {
//                logger.info("BillingMonth [" + wsmodel.getBillingmonth() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getDatepaid())) {
//                logger.info("DatePaid [" + wsmodel.getDatepaid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getAmtpaid())) {
//                logger.info("AmtPaid [" + wsmodel.getAmtpaid() + "]");
//            }
//
//            if(Util.hasText(wsmodel.getTranauthid())) {
//                logger.info("TranAuthId [" + wsmodel.getTranauthid() + "]");
//            }
//
//            logger.info("********************Response********************");
//
//            if(wsmodel.getId() != null)
//            {
//                //PrintNayaPayMsg(wsmodel, true);
//                GeneralDao.Instance.saveOrUpdate(wsmodel);
//                GeneralDao.Instance.flush(); //Raza Adding for Unique TxnRefNum and other Constraints for WSMODEL class
//            }
//            else {
//                HashMap<String, Object> params = new HashMap<String, Object>();
//                params.put("id", wsmodel.getId());
////                params.put("nayaid", wsmodel.getNayapayid());
////                params.put("mobnum", (Util.hasText(wsmodel.getMobilenumber())) ? wsmodel.getMobilenumber() : ""); //wsmodel.getMobilenumber());
////                params.put("bnkcode", wsmodel.getBankcode());
//                //WalletCMSWsEntity ent = (WalletCMSWsEntity) GeneralDao.Instance.findObject("from WalletCMSWsEntity c where c.nayapayid= :nayaid " + "and c.mobilenumber = :mobnum " + "and c.bankcode = :bnkcode " + "and c.id = :id", params);
//                WalletCMSWsEntity ent = (WalletCMSWsEntity) GeneralDao.Instance.findObject("from WalletCMSWsEntity c where c.id = :id", params);
//                if (ent != null) {
//                    ent.setRespcode(wsmodel.getRespcode());
//                    PrintNayaPayMsg(wsmodel, true);
//                    GeneralDao.Instance.saveOrUpdate(ent);
//                    GeneralDao.Instance.flush(); //Raza Adding for Unique TxnRefNum and other Constraints for WSMODEL class
//                } else {
//                    logger.error("WSEntity not found in DB for Response..!");
////                ent.setRespcode(ISOResponseCodes.ERROR_UNKOWN);
////                GeneralDao.Instance.saveOrUpdate(ent);
//                }
//            }
//            //GeneralDao.Instance.commit();
//        }
//    }

    public static String getStrException(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String getMaskedValue(String value) //Raza Mask any Field
    { //Raza TODO: verify value with small length like <= 3
        String maskedValue = "";
        //maskedValue = value.substring(0,4) + value.substring(3,value.length()-3).replaceAll(".", "*") + value.substring(value.length()-3,value.length());
        maskedValue = value;
        return maskedValue;
    }

    public static String getFullMaskedValue(String value) //Raza Mask any Field
    { //Raza TODO: verify value with small length like <= 3

        //Raza Please REMOVE ME start
        //return value;
        //Raza Please REMOVE ME end


        String maskedValue = "";
        maskedValue = value.replaceAll(".", "*");
        //maskedValue = value;
        return maskedValue;
    }

    //m.rehman: for pan encryption bc <start>
    public static String getPANEncryptedValue(String value, String channelId) {
        String encryptedValue = "", aesKey = "";

        try {

            //Implement Encrypt Key verification here
            Terminal endPointTerminal = null;

            if(GlobalContext.getInstance().getChannelbyId(channelId) == null)
            {
                logger.error("Invalid ChannelId, rejecting...");
            }

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(channelId)).getInstitutionId();
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);
            Set<SecureKey> incomingKeySet = endPointTerminal.getKeySet();
            SecureDESKey AESKey = SecureDESKey.getKeyByType(KeyType.TYPE_AES, incomingKeySet);

            if (AESKey == null) {
                logger.error("AES key not found!!!");

            } else {
                aesKey = AESKey.getKeyBytes();
                logger.debug("Key [" + aesKey + "]");
                encryptedValue = WSEncrptionUtil.AES256GCMEncryptWithoutVector(value, aesKey);

                //TODO: need to comment below logging
                logger.debug("Encrypted Card Number [" + encryptedValue + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to encrypt Card Number!!!");
            encryptedValue = "";
        }
        return encryptedValue;
    }

    public static String getPANDecryptedValue(String value, String channelId) {
        String decryptedValue = "", aesKey = "";

        try {

            //Implement Encrypt Key verification here
            Terminal endPointTerminal = null;

            if(GlobalContext.getInstance().getChannelbyId(channelId) == null)
            {
                logger.error("Invalid ChannelId, rejecting...");
            }

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(channelId)).getInstitutionId();
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);
            Set<SecureKey> incomingKeySet = endPointTerminal.getKeySet();
            SecureDESKey AESKey = SecureDESKey.getKeyByType(KeyType.TYPE_AES, incomingKeySet);

            if (AESKey == null) {
                logger.error("AES key not found!!!");
            } else {
                aesKey = AESKey.getKeyBytes();
                decryptedValue = WSEncrptionUtil.AES256GCMDecryptWithoutVector(value, aesKey);

                //TODO: need to comment below logging
                logger.debug("Decrypted Card Number [" + decryptedValue + "]");
            }
        } catch (Exception e) {
            logger.error("Unable to deccrypt Card Number!!!");
            decryptedValue = "";
        }
        return decryptedValue;
    }
    //m.rehman: for pan encryption bc <end>

    //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
    //////////////////////////////////////////////////////////////////
    public static String getPANEncryptedValue(String value) {
        String encryptedValue = "", aesKey = "";
        try {
            aesKey = GlobalContext.getInstance().getPanEncryptionKeys().get(KeyType.TYPE_AES);
            if (!Util.hasText(aesKey)) {
                logger.error("AES key not found!!!");

            } else {
                logger.debug("Key [" + aesKey + "]");
                encryptedValue = WSEncrptionUtil.AES256GCMEncryptWithoutVector(value, aesKey);

                //TODO: need to comment below logging
                logger.debug("Encrypted Card Number [" + encryptedValue + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to encrypt Card Number!!!");
            encryptedValue = "";
        }
        return encryptedValue;
    }

    public static String getPANDecryptedValue(String value) {
        String decryptedValue = "", aesKey = "";
        try {
            aesKey = GlobalContext.getInstance().getPanEncryptionKeys().get(KeyType.TYPE_AES);
            if (!Util.hasText(aesKey)) {
                logger.error("AES key not found!!!");

            } else {
                //TODO: remove this logging
                logger.debug("Key [" + aesKey + "]");
                decryptedValue = WSEncrptionUtil.AES256GCMDecryptWithoutVector(value, aesKey);

                //TODO: need to comment below logging
                logger.debug("Decrypted Card Number [" + decryptedValue + "]");
            }
        } catch (Exception e) {
            logger.error("Unable to deccrypt Card Number!!!");
            logger.error(WebServiceUtil.getStrException(e));
            decryptedValue = "";
        }
        return decryptedValue;
    }
    /////////////////////////////////////////////////////////////////

    public static Boolean ValidateSource(WalletCMSWsEntity wsmodel)
    {
        try
        {
            logger.info("Request [" + wsmodel.getServicename() + "] received from IP [" + wsmodel.getIncomingip() + "]");
            if(Util.hasText(wsmodel.getChannelid()))
            {
                //m.rehman: need to change following logic, as wallet only communicates with Switch
                //Channel channel = GlobalContext.getInstance().getChannelbyId(wsmodel.getChannelid());
                Channel channel = GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH);
                if(channel == null)
                {
                    logger.error("Invalid ChannelId [" + wsmodel.getChannelid() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.ERROR_CHANNELID);
                    return false;
                }

                if (!Util.hasText(channel.getAllowedips()) || !channel.getAllowedips().contains(wsmodel.getIncomingip())) {
                    logger.error("Client IP Address [" + wsmodel.getIncomingip() + "mismatch, rejecting transaction ...");
                    wsmodel.setRespcode(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE);  //75 - UNKNOWN TRANSACTION SOURCE - Refer to doc
                    return false;
                }
            }
            else
            {
                logger.error("ChannelId not present in Request, cannot validate source IP, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.ERROR_CHANNELID);
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while validating source IP address, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE);  //75 - UNKNOWN TRANSACTION SOURCE - Refer to doc
            return false;
        }
        return true;
    }

    // Asim Shahzad, Date : 12 Aug 2021, Tracking ID : VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091

    public static String GetWalletBalance(String fromDate, String toDate, String sortFlag, CMSAccount wallet) {
        String walletBalance="";

        try {
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + WalletGeneralLedger.class.getName() + " c where c.wallet= :WALLETID and substr(c.transdatetime,0,6) between :FROMDATE and :TODATE ";

            if(sortFlag.equals("asc")) {
                dbQuery += "order by c.id asc";
            }
            else {
                dbQuery += "order by c.id desc";
            }

            params = new HashMap<String, Object>();
            params.put("WALLETID", wallet);
            params.put("FROMDATE", fromDate);
            params.put("TODATE", toDate);

            List<WalletGeneralLedger> walletGLList = GeneralDao.Instance.find(dbQuery, params);

            if(sortFlag.equals("asc")) {
                walletBalance = walletGLList.get(0).getPreviousBalance();
            }
            else {
                walletBalance = walletGLList.get(0).getClosingBalance();
            }
        }
        catch (Exception e){
            walletBalance="";

            logger.error(e);//s.mehtab on 25-11-2020
            logger.error("Exception caught while Executing GetWalletBalance..!");
        }

        return walletBalance;
    }

    public static String GetWalletTxnBalance(String txnRefNum, String txnDateTime, String sortFlag, CMSAccount wallet) { // Asim Shahzad, Date : 1st Sep 2021, Tracking : to be logged
        String walletBalance="";

        try {
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + WalletGeneralLedger.class.getName() + " c where c.wallet= :WALLETID and c.txnrefnum= :TXNREFNUM and c.walletflag= '1' and c.transdatetime= :TRANSDATETIME "; // Asim Shahzad, Date : 1st Sep 2021, Tracking : to be logged

            if(sortFlag.equals("asc")) {
                dbQuery += "order by c.id asc";
            }
            else {
                dbQuery += "order by c.id desc";
            }

            params = new HashMap<String, Object>();
            params.put("WALLETID", wallet);
            params.put("TXNREFNUM", txnRefNum);
            params.put("TRANSDATETIME", txnDateTime); // Asim Shahzad, Date : 1st Sep 2021, Tracking : to be logged

            List<WalletGeneralLedger> walletGLList = GeneralDao.Instance.find(dbQuery, params);

            if (walletGLList != null && walletGLList.size() > 0) {
                if (sortFlag.equals("asc")) {
                    walletBalance = walletGLList.get(0).getPreviousBalance();
                } else {
                    walletBalance = walletGLList.get(0).getClosingBalance();
                }
            }
        }
        catch (Exception e){
            walletBalance="";

            logger.error(e);
            logger.error("Exception caught while Executing GetWalletTxnBalance..!");
        }

        return walletBalance;
    }

    // =====================================================================================================

}
