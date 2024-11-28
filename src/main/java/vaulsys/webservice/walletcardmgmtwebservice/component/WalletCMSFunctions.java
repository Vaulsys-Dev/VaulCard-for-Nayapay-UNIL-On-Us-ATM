package vaulsys.webservice.walletcardmgmtwebservice.component;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import vaulsys.calendar.DateTime;
import vaulsys.cms.base.*;
import vaulsys.cms.components.CardNumberGenerator;
import vaulsys.config.SystemConfig;
import vaulsys.entity.Tax;
import vaulsys.entity.TaxType;
import vaulsys.entity.impl.TransactionCharges;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.EMV.BERTLV;
import vaulsys.protocols.PaymentSchemes.EMV.EMVTags;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOPOSEntryMode;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.security.hsm.HardwareSecurityModule;
import vaulsys.util.IBANUtil;
import vaulsys.util.Util;
import vaulsys.customer.Currency;
import vaulsys.util.WebServiceUtil;
import vaulsys.wallet.components.FinanceManager;
import vaulsys.webservice.walletcardmgmtwebservice.entity.SwitchTransactionCodes;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.wfe.GlobalContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Raza on 01-Oct-18.
 */
public class WalletCMSFunctions {

    private static final Logger logger = Logger.getLogger(WalletCMSFunctions.class);
    private static IBANUtil obj_IBANUtil = new IBANUtil(); // Asim Shahzad, Date : 30th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241

    public static boolean CreateAccountProfile(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            String dbQuery, question1Retries="", question2Retries=""; // Asim Shahzad, Date : 1st Nov 2021, Call ID : VC-NAP-202110282
            Map<String, Object> params;

            // Asim Shahzad, Date : 27th Aug 2020, Call ID : VC-NAP-202008073/ VC-NAP-202009301
            //new start
            dbQuery = "from " + SystemConfig.class.getName() + " c where c.identifier in (:IDENTIFIER)"; // Asim Shahzad, Date : 1st Nov 2021, Call ID : VC-NAP-202110282
            params = new HashMap<String, Object>();

            // Asim Shahzad, Date : 1st Nov 2021, Call ID : VC-NAP-202110282
            List<String> identifiers = new ArrayList<>();
            identifiers.add("SEC_QUES_1_RETRIES");
            identifiers.add("SEC_QUES_2_RETRIES");
            params.put("IDENTIFIER", identifiers);

            List<SystemConfig> list_obj_SystemConfig = GeneralDao.Instance.find(dbQuery, params);

            if(list_obj_SystemConfig.size() > 0) {
                for (int i = 0; i < list_obj_SystemConfig.size(); i++) {
                    if (list_obj_SystemConfig.get(i).getIdentifier().equals("SEC_QUES_1_RETRIES"))
                        question1Retries = list_obj_SystemConfig.get(i).getValue();
                    if (list_obj_SystemConfig.get(i).getIdentifier().equals("SEC_QUES_2_RETRIES"))
                        question2Retries = list_obj_SystemConfig.get(i).getValue();
                }
            }
            else {
                logger.error("Secret Questions retries not found in DB...");
            }
            // ===============================================================

            if(account == null)
            {
                CMSCustomer customer = new CMSCustomer();
                customer.setMobileNumber(wsmodel.getMobilenumber());
                customer.setCnic(wsmodel.getCnic());
                customer.setCustomerId(wsmodel.getCnic());
                customer.setFirstname(wsmodel.getCustomername());
                customer.setMotherName(wsmodel.getMothername());
                Date d = new SimpleDateFormat("dd-MM-yyyy").parse(wsmodel.getDateofbirth());
                customer.setDateofBirth(d);
                //logger.info("Date of Birth [" + customer.getDateofBirth() + "]");
                //customer.setCnicpictureFront(wsmodel.getCnicpicturefront());
                //customer.setCnicpictureBack(wsmodel.getCnicpictureback());
                //customer.setCustomerpicture(wsmodel.getCustomerpicture());
                customer.setFatherName(wsmodel.getFathername());

                // Asim Shahzad, Date : 24th Aug 2020, Call ID : VP-NAP-202008071
                if(Util.hasText(wsmodel.getProvince())) {
                    customer.setProvince(wsmodel.getProvince());
                }
                //===============================================================

                customer.setCnicexpiry(wsmodel.getCnicexpiry());
                customer.setPlaceofbirth(wsmodel.getPlaceofbirth());
                customer.setHomeAddress(wsmodel.getAddress());

                // Asim Shahzad, Date : 24th Aug 2020, Call ID : VP-NAP-202008071
                if(Util.hasText(wsmodel.getCity())) {
                    customer.setCity(wsmodel.getCity());
                }
                if(Util.hasText(wsmodel.getCountry())) {
                    customer.setCountry(wsmodel.getCountry());
                }
                //===============================================================

                customer.setTsp(wsmodel.getTsp());
                customer.setStatus("08"); //Raza setting 08 as PIN is not Created Yet
                customer.setActivationDate(new Date());
                //logger.info("ActivationDate [" + customer.getActivationDate() + "]");
                customer.setLastUpdateDate(new Date());
                //logger.info("LastUpdateDate [" + customer.getLastUpdateDate() + "]");
                if (customer.getCreateDate() == null) {
                    customer.setCreateDate(new Date());
                    //logger.info("CreateDate [" + customer.getCreateDate() + "]");
                }
                customer.setLastUpdateDate(new Date());
                //logger.info("LastUpdateDate [" + customer.getLastUpdateDate() + "]");
                customer.setIsBioVerified("0");
                customer.setOccupation(wsmodel.getOccupation());

                // Asim Shahzad, Date : 27th Aug 2020, Call ID : VC-NAP-202008073/ VC-NAP-202009301
                // Asim Shahzad, Date : 1st Nov 2021, Call ID : VC-NAP-202110282
                if(Util.hasText(question1Retries))
                    customer.setSecretQuestion1Retries(question1Retries);
                if(Util.hasText(question2Retries))
                    customer.setSecretQuestion2Retries(question2Retries);
                //===============================================================

                GeneralDao.Instance.saveOrUpdate(customer);

                account = new CMSAccount();
                account.setCustomer(customer); //wsmodel.getCnic());
                account.setAccountTitle(wsmodel.getCustomername());
                account.setAvailableBalance("000000000000");
                account.setActualBalance("000000000000");
                //DateFormat dateFormat = new SimpleDateFormat("MMddhhmmss");
                account.setCreateDate(new Date());
                //logger.info("account.setCreateDate [" + account.getCreateDate() + "]");
                //account.setCurrency(wsmodel.getCurrency());
                account.setLastUpdateDate(new Date());
                //logger.info("account.setLastUpdateDate [" + account.getLastUpdateDate() + "]");
                account.setStatus("08"); //Raza Setting Status 08 as PIN is not created Yet
                account.setAccountType(AccType.WALLET.toString());
                account.setBranchId(wsmodel.getBank());
                account.setLevel(AccType.LEVEL_ZERO.toString());
                account.setCategory(AccType.CAT_WALLET_VALUE);
                account.setUserId(wsmodel.getUserid());

                // Asim Shahzad, Date : 12th March 2021, Tracking ID : VP-NAP-202103113 / VC-NAP-202103113
                account.setNameOnCard(wsmodel.getNameoncard());
                // =======================================================================================

                //account.setCurrency("586");
                Currency currency = GlobalContext.getInstance().getCurrencybySwiftCode(wsmodel.getAccountcurrency());
                if (currency != null) {
                    account.setCurrency(currency.getCode().toString());
                } else {
                    account.setCurrency(GlobalContext.getInstance().getBaseCurrency().getCode().toString());
                }
                //m.rehman: for NayaPay, setting isPrimary to false for new wallet account
                account.setIsprimary("1");
                params = null;
                String encryptpin="";
                List<CMSProduct> productlist;
                params = new HashMap<String, Object>();
                dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
                params = new HashMap<String, Object>();
                params.put("prdcttype", "LVL0");//"PROV"); //"LVL0");
                params.put("default", "1");
                productlist = GeneralDao.Instance.find(dbQuery, params);

                if (productlist != null && productlist.size() > 0) {
                    //Raza ignore if there are multiple default wallet products
                    logger.info("Assigning Wallet Product to Account..");
                    CMSProduct product = productlist.get(0);
                    account.setProduct(product);

                    //m.rehman: 15-09-2021, VP-NAP-202109092 / VG-NAP-202109101 - Non financial Transactions on VaulGuard
                    //to avoid duplicate account number generation in craete wallet on transaction load
                    //account.setAccountNumber(GenerateWalletAccountNumber(product));
                    account.setAccountNumber(GenerateWalletAccountNumberFromSeq(product, "CMS_ACCOUNT_NO_SEQ"));
                    //logger.info("Account No [" + account.getAccountNumber() + "]");
                    ////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241
                    account.setiBan(obj_IBANUtil.generateIBAN(account.getAccountNumber()));
                    // ======================================================================================

                    GeneralDao.Instance.saveOrUpdate(account);

                    //Raza User-Id To Account Relation
                    NayaPayRelation npr = new NayaPayRelation();
                    npr.setUser_Acct_Id(account.getUserId());
                    npr.setAccount(account);
                    GeneralDao.Instance.saveOrUpdate(npr);
                    //Raza User-Id To Account Relation

                    if (AssignAccountLimits(product, account, wsmodel)) {
                        logger.info("Wallet limits assign successfully ...");
                        logger.info("Wallet Data Created! Going to add security questions...");

                        // Asim Shahzad, Date : 9th March 2021, Tracking ID : VP-NAP-202103112 / VC-NAP-202103112
//                        logger.info("Adding Security Questions...");
//                        if(!AddSecurityQuestions(wsmodel, customer))
//                        {
//                            logger.error("Unable to Add Security Questions for cnic [" + wsmodel.getCnic() + "]");
//                            wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
//                            //GeneralDao.Instance.evict(account); //Raza Dont save if declined
//                            //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
//                            GeneralDao.Instance.clear();
//                            GeneralDao.Instance.saveOrUpdate(wsmodel);
//                            return false;
//                        }
                        // ===================================================================================================

                        logger.info("Wallet Account Profile created successfully");
                        //logger.info("Creating Wallet Account PIN, as requested through PIN Block");

                    } else {
                        logger.info("Unable assign Account limits, rejecting ...");
                        //GeneralDao.Instance.evict(account); //Raza Dont save if declined
                        //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                        GeneralDao.Instance.clear();
                        GeneralDao.Instance.saveOrUpdate(wsmodel);
                        return false;
                    }

                    /*
                    account.setProduct(product);
                    account.setAccountNumber(GenerateWalletAccountNumber(product));
                    GeneralDao.Instance.saveOrUpdate(account);

                    //Raza User-Id To Account Relation
                    NayaPayRelation npr = new NayaPayRelation();
                    npr.setUser_Acct_Id(wsmodel.getUserid());
                    npr.setAccount(account);
                    GeneralDao.Instance.saveOrUpdate(npr);
                    //Raza User-Id To Account Relation

                    List<String> channelDebitList;
                    List<String> channelCreditList;
                    List<CMSProductDebitLimit> cmsDebitLimitList;
                    List<CMSProductCreditLimit> cmsCreditLimitList;

                    dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product.getProductId());
                    params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                    channelDebitList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelDebitList != null && channelDebitList.size() > 0) {
                        for (String channel : channelDebitList) {

                            CMSAuth cauth = new CMSAuth();
                            cauth.setRelation(account.getAccountNumber());
                            cauth.setAccount(account);
                            cauth.setCustomer(customer);
                            cauth.setChannelId(channel);
                            cauth.setOffset("0000");
                            cauth.setRemRetries("3");
                            cauth.setMaxRetries("3");
                            cauth.setReasonCode("0000");
                            cauth.setStatus("08");
                            cauth.setIsDefault("1");
                            GeneralDao.Instance.saveOrUpdate(cauth);

                            dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    " and c.channelId = :channel " +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product.getProductId());
                            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                            params.put("channel", channel);
                            cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                                for (CMSProductDebitLimit climit : cmsDebitLimitList) {

                                    Integer cycleLength = 0;
                                    CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                                    acctLimit.setRelation(account.getAccountNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product.getProductId());
                    params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                    channelCreditList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelCreditList != null && channelCreditList.size() > 0) {
                        for (String channel : channelCreditList) {

                            if (channelDebitList != null && channelDebitList.size() > 0
                                    && !channelDebitList.contains(channel)) {
                                CMSAuth cauth = new CMSAuth();
                                cauth.setRelation(account.getAccountNumber());
                                cauth.setAccount(account);
                                cauth.setCustomer(customer);
                                cauth.setChannelId(channel);
                                cauth.setOffset("0000");
                                cauth.setRemRetries("3");
                                cauth.setMaxRetries("3");
                                cauth.setReasonCode("0000");
                                cauth.setStatus("08");
                                cauth.setIsDefault("1");
                                GeneralDao.Instance.saveOrUpdate(cauth);
                            }

                            dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    " and c.channelId = :channel " +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product.getProductId());
                            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                            params.put("channel", channel);
                            cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                                for (CMSProductCreditLimit climit : cmsCreditLimitList) {

                                    Integer cycleLength = 0;
                                    CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                                    acctLimit.setRelation(account.getAccountNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    if (channelDebitList.size() <= 0 && channelCreditList.size() <= 0) {
                        logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
                        wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                        GeneralDao.Instance.evict(account); //Raza Dont save if declined
                        GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                        return false;
                    }
                    */

                } else {
                    logger.error("No Wallet Product found to assign.. rejecting txn..");
                    //GeneralDao.Instance.evict(account);//no account object saved
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                    return false;
                }

                /*
                logger.info("Wallet Data Created! Going to add security questions...");

                logger.info("Adding Security Questions...");
                if(!AddSecurityQuestions(wsmodel, customer))
                {
                    logger.error("Unable to Add Security Questions for cnic [" + wsmodel.getCnic() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    GeneralDao.Instance.evict(account); //Raza Dont save if declined
                    GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                    return false;
                }


                logger.info("Wallet Account Profile created successfully");
                logger.info("Creating Wallet Account PIN, as requested through PIN Block");
                */

            }
            else
            {
                logger.info("Updating Status of Soft Deleted Wallet..");
                account.getCustomer().setStatus("08"); //Raza Rest PIN or Generate PIN
                account.getCustomer().setLastUpdateDate(new Date());
                account.setStatus("08"); //Raza Rest PIN or Generate PIN
                account.setLastUpdateDate(new Date());
                GeneralDao.Instance.saveOrUpdate(account.getCustomer());
                GeneralDao.Instance.saveOrUpdate(account);

            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            e.printStackTrace();
            //m.rehman: 20-09-2021, VP-NAP-202109092 / VG-NAP-202109101 - Non financial Transactions on VaulGuard
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
        wsmodel.setAccountnumber(account.getAccountNumber()); //Raza Return Account Number to Middleware For PIN Generation
        return true;
    }

    // Asim Shahzad, Date : 16th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111

    public static boolean ReCreateAccountProfile(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        logger.info("Executing ReCreateAccountProfile for wallet creation!!!");

        try {
            String dbQuery;
            Map<String, Object> params;

            CMSCustomer customer = account.getCustomer();
            customer.setStatus("08"); //Raza setting 08 as PIN is not Created Yet
            GeneralDao.Instance.saveOrUpdate(customer);

            account = new CMSAccount();
            account.setCustomer(customer); //wsmodel.getCnic());
            account.setAccountTitle(wsmodel.getCustomername());
            account.setAvailableBalance("000000000000");
            account.setActualBalance("000000000000");
            DateFormat dateFormat = new SimpleDateFormat("MMddhhmmss");
            account.setCreateDate(new Date());
            account.setLastUpdateDate(new Date());
            account.setStatus("08"); //Raza Setting Status 08 as PIN is not created Yet
            account.setAccountType(AccType.WALLET.toString());
            account.setBranchId(wsmodel.getBank());
            account.setLevel(AccType.LEVEL_ZERO.toString());
            account.setCategory(AccType.CAT_WALLET_VALUE);
            account.setUserId(wsmodel.getUserid());

            // Asim Shahzad, Date : 12th March 2021, Tracking ID : VP-NAP-202103113 / VC-NAP-202103113
            account.setNameOnCard(wsmodel.getNameoncard());
            // =======================================================================================

            //account.setCurrency("586");
            Currency currency = GlobalContext.getInstance().getCurrencybySwiftCode(wsmodel.getAccountcurrency());
            if (currency != null) {
                account.setCurrency(currency.getCode().toString());
            } else {
                account.setCurrency(GlobalContext.getInstance().getBaseCurrency().getCode().toString());
            }
            //m.rehman: for NayaPay, setting isPrimary to false for new wallet account
            account.setIsprimary("1");
            params = null;
            String encryptpin = "";
            List<CMSProduct> productlist;
            params = new HashMap<String, Object>();
            dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
            params = new HashMap<String, Object>();
            params.put("prdcttype", "LVL0");//"PROV"); //"LVL0");
            params.put("default", "1");
            productlist = GeneralDao.Instance.find(dbQuery, params);

            if (productlist != null && productlist.size() > 0) {
                //Raza ignore if there are multiple default wallet products
                logger.info("Assigning Wallet Product to Account..");
                CMSProduct product = productlist.get(0);
                account.setProduct(product);

                //m.rehman: 15-09-2021, VP-NAP-202109092 / VG-NAP-202109101 - Non financial Transactions on VaulGuard
                //to avoid duplicate account number generation in craete wallet on transaction load
                //account.setAccountNumber(GenerateWalletAccountNumber(product));
                account.setAccountNumber(GenerateWalletAccountNumberFromSeq(product, "CMS_ACCOUNT_NO_SEQ"));
                ////////////////////////////////////////////////////////////////////////////////////////////////////////

                // Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241
                account.setiBan(obj_IBANUtil.generateIBAN(account.getAccountNumber()));
                // ======================================================================================

                GeneralDao.Instance.saveOrUpdate(account);

                //Raza User-Id To Account Relation
                NayaPayRelation npr = new NayaPayRelation();
                npr.setUser_Acct_Id(account.getUserId());
                npr.setAccount(account);
                GeneralDao.Instance.saveOrUpdate(npr);
                //Raza User-Id To Account Relation

                if (AssignAccountLimits(product, account, wsmodel)) {
                    logger.info("Wallet limits assign successfully ...");
                    logger.info("Wallet Data Created! Going to add security questions...");
                    logger.info("Wallet Account Profile created successfully");

                } else {
                    logger.info("Unable assign Account limits, rejecting ...");
                    GeneralDao.Instance.clear();
                    GeneralDao.Instance.saveOrUpdate(wsmodel);
                    return false;
                }
            } else {
                logger.error("No Wallet Product found to assign.. rejecting txn..");
                //GeneralDao.Instance.evict(account);//no account object saved
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            e.printStackTrace();
            return false;
        }
        wsmodel.setAccountnumber(account.getAccountNumber()); //Raza Return Account Number to Middleware For PIN Generation
        return true;
    }

    // =======================================================================================

    public static boolean CreateCardProfile(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            String dbQuery;
            Map<String, Object> params;
            if(account != null)
            {
                CMSCard card = new CMSCard();
                card.setCardName(account.getAccountTitle());
                //card.setCreateDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                card.setRequestDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                if (Util.hasText(wsmodel.getCardtype()) && !wsmodel.getCardtype().equals("visa_virtual")) {
                    Set<CMSAccount> acctlist = new HashSet<CMSAccount>();
                    acctlist.add(account);
                    card.setList_CustAccounts(acctlist);
                }

                //Expiry
                card.setCardStatus("14"); //Raza Update THIS
                //card.setCustomerID(account.getCustomer().getCustomerId());
                //card.setCustomerID(account.getCustomer().getId()+""); //Raza updating 22-02-2019
                card.setCustomer(account.getCustomer()); //Raza updating 22-02-2019

                //m.rehman: 11-08-2020: saving tracking id for card embossing file
                card.setTrackingId(wsmodel.getTrackingid());

                params = null;
                List<CMSProduct> productlist;
                dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
                params = new HashMap<String, Object>();

                if(account.getLevel().equals(AccType.LEVEL_ZERO.toString()))
                {
                    //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                    if (Util.hasText(wsmodel.getCardtype()) && wsmodel.getCardtype().equals("visa_virtual")) {
                        params.put("prdcttype", "VISA_VIRTUAL");

                    } else if (Util.hasText(wsmodel.getCardtype()) && wsmodel.getCardtype().equals("visa_physical")) {
                        params.put("prdcttype", "VISA_DEBIT");

                    }
                    else {
                        params.put("prdcttype", "CARD_LVL0"); //"LVL0");
                    }
                }
                else if(account.getLevel().equals(AccType.LEVEL_ONE.toString()))
                {
                    //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                    if (Util.hasText(wsmodel.getCardtype()) && wsmodel.getCardtype().equals("visa_virtual")) {
                        params.put("prdcttype", "VISA_VIRTUAL");

                    } else if (Util.hasText(wsmodel.getCardtype()) && wsmodel.getCardtype().equals("visa_physical")) {
                        params.put("prdcttype", "VISA_DEBIT");

                    }
                    else {
                        params.put("prdcttype", "CARD_LVL1");
                    }
                }
                else
                {
                    logger.error("Invalid Account Level [" + account.getLevel() + "], rejecting...");
                    return false;
                }

                params.put("default", "1");
                productlist = GeneralDao.Instance.find(dbQuery, params);

                if (productlist != null && productlist.size() > 0) {
                    //Raza ignore if there are multiple default wallet products
                    logger.info("Assigning Card Product to Card..");
                    CMSProduct product = productlist.get(0);
                    card.setProduct(product);

                    //Expiry
                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    c.add(Calendar.YEAR, Integer.parseInt(product.getProductDetail().getValidYearsRenewal()));
                    card.setExpiryDate(WebServiceUtil.limitcycleDateFormat.format(c.getTime()));

                    //m.rehman: commenting below line, need to save encrypted value of pan in db
                    //card.setCardNumber(GenerateWalletAccountNumber(product));
                    //m.rehman: 06-08-2020, Euronet Integration, PAN generation according to pan format defined for scheme
                    //String cardNo = GenerateWalletAccountNumber(product);
                    String cardNo = GeneratePAN(product);
                    if (!Util.hasText(cardNo)) {
                        logger.error("Card number generation failed !!!");
                        return false;
                    }

                    //TODO: m.rehman: remove below logging
                    logger.debug("cardNo [" + cardNo + "]");
                    card.setCardNoLastDigits(cardNo.substring(cardNo.length() - 4, cardNo.length()));

                    //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
                    //card.setCardNumber(WebServiceUtil.getPANEncryptedValue(cardNo, ChannelCodes.SWITCH));
                    card.setCardNumber(WebServiceUtil.getPANEncryptedValue(cardNo));
                    ///////////////////////////////////////////////////////////////////////////////////////
                    card.setPrimaryCardNumber(card.getCardNumber());

                    //m.rehman: commenting below line, need to save encrypted value of pan in db
                    //card.setCardNumberExpiryRelation(card.getCardNumber() + "=" + card.getExpiryDate().substring(2,6));
                    logger.debug("Rel [" + cardNo + "=" + card.getExpiryDate().substring(2, 6) + "]");
                    //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
                    //card.setCardNumberExpiryRelation(WebServiceUtil.getPANEncryptedValue((cardNo + "=" + card.getExpiryDate().substring(2, 6)), ChannelCodes.SWITCH));
                    card.setCardNumberExpiryRelation(WebServiceUtil.getPANEncryptedValue(cardNo + "=" + card.getExpiryDate().substring(2, 6)));
                    ///////////////////////////////////////////////////////////////////////////////////////

                    //Arsalan Akhter, Date: 12_March_2021, Tracking ID: VP-NAP-202103114_VC-NAP-202103114
                    card.setIsExported("0");
                    /////////////////////////////////////////////////////////////////////////////////////

                    GeneralDao.Instance.saveOrUpdate(card);

                    //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                    if (Util.hasText(wsmodel.getCardtype()) && !wsmodel.getCardtype().equals("visa_virtual")) {
                        account.setCard(card);
                    }

                    //s.mehtab 31/Dec/2020 VC-NAP-202012291 Include the mobile number received in RequestDebitCard
                    account.getCustomer().setMobileNumber(wsmodel.getMobilenumber());

                    if (productlist != null && productlist.size() > 0) {
                        //Raza ignore if there are multiple default wallet products
                        if (AssignCardLimits(productlist.get(0), account, card)) {
                            logger.info("Card limits assign successfully ...");

                            //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                            logger.info("Assigning card controls ...");
                            CMSCardControlConfig ccccc  = new CMSCardControlConfig();
                            ccccc.setCard(card);
                            ccccc.setCashWithdrawalEnabled(true);
                            ccccc.setChipPinEnabled(true);
                            ccccc.setInternationalTxnsEnabled(true);
                            ccccc.setMagStripeEnabled(true);
                            ccccc.setNFCEnabled(true);
                            ccccc.setOnlineEnabled(true);
                            GeneralDao.Instance.save(ccccc);

                            logger.info("Card Profile created successfully");
                            return true;

                        } else {
                            logger.info("Unable assign Card limits, rejecting ...");
                            wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                            GeneralDao.Instance.evict(card);
                            return false;
                        }

                    } else {
                        logger.error("No Card Product found to assign.. rejecting txn..");
                        wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND);
                        //GeneralDao.Instance.evict(account);//no account object saved
                        GeneralDao.Instance.evict(card); //Raza Dont save if declined
                        return false;
                    }

                    /*
                    CMSProduct product = productlist.get(0);
                    card.setProduct(product);

                    //m.rehman: commenting below line, need to save encrypted value of pan in db
                    //card.setCardNumber(GenerateWalletAccountNumber(product));
                    String cardNo = GenerateWalletAccountNumber(product);
                    //TODO: m.rehman: remove below logging
                    logger.debug("cardNo [" + cardNo + "]");
                    card.setCardNoLastDigits(cardNo.substring(cardNo.length() - 4, cardNo.length()));
                    card.setCardNumber(WebServiceUtil.getPANEncryptedValue(cardNo, ChannelCodes.SWITCH));
                    card.setPrimaryCardNumber(card.getCardNumber());

                    //m.rehman: commenting below line, need to save encrypted value of pan in db
                    //card.setCardNumberExpiryRelation(card.getCardNumber() + "=" + card.getExpiryDate().substring(2,6));
                    logger.debug("Rel [" + cardNo + "=" + card.getExpiryDate().substring(2, 6) + "]");
                    card.setCardNumberExpiryRelation(WebServiceUtil.getPANEncryptedValue((cardNo + "=" + card.getExpiryDate().substring(2, 6)), ChannelCodes.SWITCH));
                    GeneralDao.Instance.saveOrUpdate(card);
                    account.setCard(card);

                    List<String> channelDebitList;
                    List<String> channelCreditList;
                    List<CMSProductDebitLimit> cmsDebitLimitList;
                    List<CMSProductCreditLimit> cmsCreditLimitList;

                    dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product.getProductId());
                    params.put("limittype", CMSLimitType.CARD_LIMIT);
                    channelDebitList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelDebitList != null && channelDebitList.size() > 0) {
                        for (String channel : channelDebitList) {

                            CMSAuth cauth = new CMSAuth();
                            cauth.setRelation(card.getCardNumberExpiryRelation());
                            cauth.setCard(card);
                            cauth.setAccount(account);
                            cauth.setCustomer(account.getCustomer());
                            cauth.setChannelId(channel);
                            cauth.setOffset("0000");
                            cauth.setRemRetries("3");
                            cauth.setMaxRetries("3");
                            cauth.setReasonCode("0000");
                            cauth.setStatus("07");
                            cauth.setIsDefault("1");
                            GeneralDao.Instance.saveOrUpdate(cauth);

                            dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    " and c.channelId = :channel " +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product.getProductId());
                            params.put("limittype", CMSLimitType.CARD_LIMIT);
                            params.put("channel", channel);
                            cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                                for (CMSProductDebitLimit climit : cmsDebitLimitList) {

                                    Integer cycleLength = 0;
                                    CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                                    acctLimit.setRelation(card.getCardNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product.getProductId());
                    params.put("limittype", CMSLimitType.CARD_LIMIT);
                    channelCreditList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelCreditList != null && channelCreditList.size() > 0) {
                        for (String channel : channelCreditList) {

                            if (!channelDebitList.contains(channel)) {
                                CMSAuth cauth = new CMSAuth();
                                cauth.setRelation(card.getCardNumberExpiryRelation());
                                cauth.setCard(card);
                                cauth.setAccount(account);
                                cauth.setCustomer(account.getCustomer());
                                cauth.setChannelId(channel);
                                cauth.setOffset("0000");
                                cauth.setRemRetries("3");
                                cauth.setMaxRetries("3");
                                cauth.setReasonCode("0000");
                                cauth.setStatus("07");
                                cauth.setIsDefault("1");
                                GeneralDao.Instance.saveOrUpdate(cauth);
                            }

                            dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    " and c.channelId = :channel " +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product.getProductId());
                            params.put("limittype", CMSLimitType.CARD_LIMIT);
                            params.put("channel", channel);
                            cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                                for (CMSProductCreditLimit climit : cmsCreditLimitList) {

                                    Integer cycleLength = 0;
                                    CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                                    acctLimit.setRelation(card.getCardNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    if (channelDebitList.size() <= 0 && channelCreditList.size() <= 0) {
                        logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
                        GeneralDao.Instance.evict(card); //Raza Dont save if declined
                        return false;
                    }
                    */
                } else {
                    logger.error("No Card Product found to assign.. rejecting txn..");
                    wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND);
                    //GeneralDao.Instance.evict(account);//no account object saved
                    GeneralDao.Instance.evict(card); //Raza Dont save if declined
                    return false;
                }
            }
            else
            {
                logger.error("No account found to create card profile");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_ACCOUNT);
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            logger.error(e);
            return false;
        }
    }

    public static boolean UpgradeAccountProfile(CMSAccount account, String serviceName)
    {
        try
        {
            logger.info("Upgrading Wallet Account Profile...");
            /*if(!UpgradeFraudProfile(account)) //Raza commenting 29-04-2019
            {
                logger.error("Unable to Upgrade Fraud Profile for account [" + account.getAccountNumber() + "]");
                return false;
            }*/

            String dbQuery, prevWalletStatus;
            Map<String, Object> params;
            if(account == null)
            {
                logger.error("No Account found while upgarding account profile to Level1, rejecting...");
                return false;
            }

            //getting existing channel list of account
            List<String> existingAuthChannelList;
            dbQuery = "select c.channelId from " + CMSAuth.class.getName() + " c where c.relation = :relation ";
            params = new HashMap<String, Object>();
            params.put("relation", account.getAccountNumber());
            existingAuthChannelList = GeneralDao.Instance.find(dbQuery, params);

            //getting existing channel list of account
            List<String> existingDebitChannelList;
            dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c"
                    + " where c.productId= :prdctid "
                    + " and c.limitType = :limittype ";
            params = new HashMap<String, Object>();
            params.put("prdctid", account.getProduct());
            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
            existingDebitChannelList = GeneralDao.Instance.find(dbQuery, params);

            List<String> existingCreditChannelList;
            dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c"
                    + " where c.productId= :prdctid "
                    + " and c.limitType = :limittype ";
            params = new HashMap<String, Object>();
            params.put("prdctid", account.getProduct());
            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
            existingCreditChannelList = GeneralDao.Instance.find(dbQuery, params);

            prevWalletStatus = account.getLevel();
            account.setLevel(AccType.LEVEL_ONE.toString());
            account.setUpgradedate(new Date());
            params = null;
            List<CMSProduct> productlist;
            dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
            params = new HashMap<String, Object>();
            params.put("prdcttype", "LVL1");
            params.put("default", "1");
            productlist = GeneralDao.Instance.find(dbQuery, params);

            if (productlist != null && productlist.size() > 0) {
                //Raza ignore if there are multiple default wallet products
                logger.info("Assigning Wallet Product to Account..");
                CMSProduct product = productlist.get(0);
                account.setProduct(product);
                GeneralDao.Instance.saveOrUpdate(account);

                List<CMSProductDebitLimit> cmsDebitLimitList;
                List<CMSProductCreditLimit> cmsCreditLimitList;

                dbQuery = "from " + CMSProductDebitLimit.class.getName()
                        + " c where c.productId= :prdctid "
                        + " and c.limitType = :limittype "
                        + " and c.parentLimitId is null "
                        + " and c.limitCategory = 2 "    //limit category 2 is Financial limit
                        //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                        //adding is active check
                        + " and c.isActive='1' ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                List<CMSDebitRemainingLimit> currentDebitList;
                dbQuery = "from " + CMSDebitRemainingLimit.class.getName() + " c where c.relation= :acct ";
                params = new HashMap<String, Object>();
                params.put("acct", account.getAccountNumber());
                currentDebitList = GeneralDao.Instance.find(dbQuery, params);

                dbQuery = "from " + CMSProductCreditLimit.class.getName()
                        + " c where c.productId= :prdctid "
                        + " and c.limitType = :limittype "
                        + " and c.parentLimitId is null "
                        + " and c.limitCategory = 2 "     //limit category 2 is Financial limit
                        //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                        //adding is active check
                        + " and c.isActive='1' ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                List<CMSCreditRemainingLimit> currentCreditList;
                dbQuery = "from " + CMSCreditRemainingLimit.class.getName() + " c where c.relation= :acct ";
                params = new HashMap<String, Object>();
                params.put("acct", account.getAccountNumber());
                currentCreditList = GeneralDao.Instance.find(dbQuery, params);

                if((currentDebitList == null || currentDebitList.size() <= 0)
                    && (currentCreditList == null || currentCreditList.size() <= 0))
                {
                    logger.error("No Limit Found for Account [" + account.getAccountNumber() + "], rejecting...");
                    return false;
                }

                if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {

                    for (CMSProductDebitLimit climit : cmsDebitLimitList) {
                        boolean found = false;
                        for (CMSDebitRemainingLimit al : currentDebitList) {
                            // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                            
                            /*if (al.getLimitId().getTransactionType() != null && al.getLimitId().getTransactionType().equals(climit.getTransactionType())
                                    && (Util.hasText(al.getLimitId().getChannelId()) && al.getLimitId().getChannelId().equals(climit.getChannelId())))*/
                            if ((al.getLimitId().getTransactionType() != null && al.getLimitId().getTransactionType().equals(climit.getTransactionType())
                                    && (Util.hasText(al.getLimitId().getChannelId()) && al.getLimitId().getChannelId().equals(climit.getChannelId())))
                                    || (al.getLimitId().getTransactionType() == null && !Util.hasText(al.getLimitId().getChannelId())
                                    && al.getLimitId().getParentLimitId() == null && Util.hasText(al.getLimitId().getDescription())
                                    && al.getLimitId().getDescription().equals(climit.getDescription()))) {
                                found = true;

                                //al.setRemainingAmount(climit.getAmount());
                                //al.setRemainingFrequency(climit.getFrequencyLength());
                                // =================================================================

                                //Arsalan Akhter, Date: 18-Oct-2021, Ticket: VC-NAP-202110121(Issue in daily wallet transaction limit)
                                al.setIsIndividual(null);
                                al.setIndividualLimitId(null);
                                //====================================================================================================

                                al.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                DateTime cycleEndDate = DateTime.now();
                                Calendar calendar = Calendar.getInstance();
                                Integer cycleLength = Integer.parseInt(climit.getCycleLength());
                                CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                                if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                    al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                                    // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                                    if(WebServiceUtil.limitcycleDateFormat.format(new Date()).substring(4,6).equals(al.getCycleEndDate().substring(4,6))) {
                                        Long limitAmount = Long.valueOf(climit.getAmount()) - (Long.valueOf(al.getLimitId().getAmount()) - Long.valueOf(al.getRemainingAmount()));

                                        logger.info("Executing Monthly Limit flow...");
                                        logger.info("Remaining Limit Amount calculated [" + limitAmount + "]");

                                        al.setRemainingAmount(StringUtils.leftPad(limitAmount.toString(), 12, '0'));

                                        Long remFrequency = Long.valueOf(climit.getFrequencyLength()) - (Long.valueOf(al.getLimitId().getFrequencyLength()) - Long.valueOf(al.getRemainingFrequency()));

                                        logger.info("Remaining Frequency calculated [" + remFrequency + "]");
                                        al.setRemainingFrequency(remFrequency.toString());
                                    }
                                    else {
                                        al.setRemainingAmount(climit.getAmount());
                                        al.setRemainingFrequency(climit.getFrequencyLength());

                                        logger.info("Executing Monthly Limit flow...");
                                        logger.info("Remaining Limit Amount [" + al.getRemainingAmount() + "]");
                                        logger.info("Remaining Frequency [" + al.getRemainingFrequency() + "]");
                                    }
                                    // =================================================================

                                } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                                    calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                                    al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                                    // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                                    if(WebServiceUtil.limitcycleDateFormat.format(new Date()).substring(2,4).equals(al.getCycleEndDate().substring(2,4))) {
                                        Long limitAmount = Long.valueOf(climit.getAmount()) - (Long.valueOf(al.getLimitId().getAmount()) - Long.valueOf(al.getRemainingAmount()));

                                        logger.info("Executing Yearly Limit flow...");
                                        logger.info("Remaining Limit Amount calculated [" + limitAmount + "]");

                                        al.setRemainingAmount(StringUtils.leftPad(limitAmount.toString(), 12, '0'));

                                        Long remFrequency = Long.valueOf(climit.getFrequencyLength()) - (Long.valueOf(al.getLimitId().getFrequencyLength()) - Long.valueOf(al.getRemainingFrequency()));

                                        logger.info("Remaining Frequency calculated [" + remFrequency + "]");
                                        al.setRemainingFrequency(remFrequency.toString());
                                    }
                                    else {
                                        al.setRemainingAmount(climit.getAmount());
                                        al.setRemainingFrequency(climit.getFrequencyLength());

                                        logger.info("Executing Yearly Limit flow...");
                                        logger.info("Remaining Limit Amount [" + al.getRemainingAmount() + "]");
                                        logger.info("Remaining Frequency [" + al.getRemainingFrequency() + "]");
                                    }
                                    // =================================================================

                                } else {

                                    // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                                    if(WebServiceUtil.limitcycleDateFormat.format(new Date()).equals(al.getCycleEndDate())) {
                                        logger.info("Executing Daily Limit flow...");
                                        logger.info("Product Debit Limit Amount: [" + climit.getAmount() + "]");
                                        logger.info("Current Debit Limit Amount: [" + al.getLimitId().getAmount() + "]");
                                        logger.info("Remaining Debit Limit Amount: [" + al.getRemainingAmount() + "]");

                                        Long temp1 = Long.valueOf(climit.getAmount());
                                        Long temp2 = Long.valueOf(al.getLimitId().getAmount());
                                        Long temp3 = Long.valueOf(al.getRemainingAmount());

                                        Long limitAmount = Long.valueOf(climit.getAmount()) - (Long.valueOf(al.getLimitId().getAmount()) - Long.valueOf(al.getRemainingAmount()));


                                        logger.info("Remaining Debit Limit Amount calculated : [" + limitAmount + "]");

                                        al.setRemainingAmount(StringUtils.leftPad(limitAmount.toString(), 12, '0'));

                                        Long remFrequency = Long.valueOf(climit.getFrequencyLength()) - (Long.valueOf(al.getLimitId().getFrequencyLength()) - Long.valueOf(al.getRemainingFrequency()));

                                        logger.info("Remaining Debit Frequency calculated : [" + remFrequency + "]");
                                        al.setRemainingFrequency(remFrequency.toString());
                                    }
                                    else {
                                        al.setRemainingAmount(climit.getAmount());
                                        al.setRemainingFrequency(climit.getFrequencyLength());

                                        logger.info("Executing Daily Limit flow...");
                                        logger.info("Remaining Debit Limit Amount [" + al.getRemainingAmount() + "]");
                                        logger.info("Remaining Debit Frequency [" + al.getRemainingFrequency() + "]");
                                    }

                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                    }
                                    al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                }

                                al.setLimitId(climit);
                                // =================================================================

                                GeneralDao.Instance.saveOrUpdate(al);
                                break;
                            }
                        }

                        if (!found) {
                            Integer cycleLength = 0;
                            CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                            acctLimit.setRelation(account.getAccountNumber());
                            acctLimit.setLimitId(climit);
                            acctLimit.setRemainingAmount(climit.getAmount());
                            acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                            cycleLength = Integer.parseInt(climit.getCycleLength());
                            acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                            DateTime cycleEndDate = DateTime.now();
                            Calendar calendar = Calendar.getInstance();
                            CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                            if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                            } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                            } else {
                                if (cycleLength > 1) {
                                    cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                }
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                            }
                            acctLimit.setIsCustProfile("0");
                            GeneralDao.Instance.saveOrUpdate(acctLimit);
                        }
                    }
                }

                if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {

                    for (CMSProductCreditLimit climit : cmsCreditLimitList) {
                        boolean found = false;
                        for (CMSCreditRemainingLimit al : currentCreditList) {
                            // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                            /*if (al.getLimitId().getTransactionType() != null && al.getLimitId().getTransactionType().equals(climit.getTransactionType())
                                    && (Util.hasText(al.getLimitId().getChannelId()) && al.getLimitId().getChannelId().equals(climit.getChannelId())))*/
                            if ((al.getLimitId().getTransactionType() != null && al.getLimitId().getTransactionType().equals(climit.getTransactionType())
                                    && (Util.hasText(al.getLimitId().getChannelId()) && al.getLimitId().getChannelId().equals(climit.getChannelId())))
                                    || (al.getLimitId().getTransactionType() == null && !Util.hasText(al.getLimitId().getChannelId())
                                    && al.getLimitId().getParentLimitId() == null && Util.hasText(al.getLimitId().getDescription())
                                    && al.getLimitId().getDescription().equals(climit.getDescription()))) {
                                found = true;


                                //al.setRemainingAmount(climit.getAmount());
                                //al.setRemainingFrequency(climit.getFrequencyLength());
                                // =================================================================

                                //Arsalan Akhter, Date: 18-Oct-2021, Ticket: VC-NAP-202110121(Issue in daily wallet transaction limit)
                                al.setIsIndividual(null);
                                al.setIndividualLimitId(null);
                                //====================================================================================================

                                al.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                DateTime cycleEndDate = DateTime.now();
                                Calendar calendar = Calendar.getInstance();
                                Integer cycleLength = Integer.parseInt(climit.getCycleLength());
                                CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                                if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                    al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                                    // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                                    if(WebServiceUtil.limitcycleDateFormat.format(new Date()).substring(4,6).equals(al.getCycleEndDate().substring(4,6))) {
                                        Long limitAmount = Long.valueOf(climit.getAmount()) - (Long.valueOf(al.getLimitId().getAmount()) - Long.valueOf(al.getRemainingAmount()));

                                        logger.info("Executing Monthly Limit flow...");
                                        logger.info("Remaining Limit Amount calculated : [" + limitAmount + "]");

                                        al.setRemainingAmount(StringUtils.leftPad(limitAmount.toString(), 12, '0'));

                                        Long remFrequency = Long.valueOf(climit.getFrequencyLength()) - (Long.valueOf(al.getLimitId().getFrequencyLength()) - Long.valueOf(al.getRemainingFrequency()));

                                        logger.info("Remaining Frequency calculated : [" + remFrequency + "]");
                                        al.setRemainingFrequency(remFrequency.toString());
                                    }
                                    else {
                                        al.setRemainingAmount(climit.getAmount());
                                        al.setRemainingFrequency(climit.getFrequencyLength());

                                        logger.info("Executing Monthly Limit flow...");
                                        logger.info("Remaining Limit Amount [" + al.getRemainingAmount() + "]");
                                        logger.info("Remaining Frequency [" + al.getRemainingFrequency() + "]");
                                    }
                                    // =================================================================

                                } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                                    calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                                    al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                                    // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                                    if(WebServiceUtil.limitcycleDateFormat.format(new Date()).substring(2,4).equals(al.getCycleEndDate().substring(2,4))) {
                                        Long limitAmount = Long.valueOf(climit.getAmount()) - (Long.valueOf(al.getLimitId().getAmount()) - Long.valueOf(al.getRemainingAmount()));

                                        logger.info("Executing Yearly Limit flow...");
                                        logger.info("Remaining Limit Amount calculated : [" + limitAmount + "]");

                                        al.setRemainingAmount(StringUtils.leftPad(limitAmount.toString(), 12, '0'));

                                        Long remFrequency = Long.valueOf(climit.getFrequencyLength()) - (Long.valueOf(al.getLimitId().getFrequencyLength()) - Long.valueOf(al.getRemainingFrequency()));

                                        logger.info("Remaining Frequency calculated : [" + remFrequency + "]");
                                        al.setRemainingFrequency(remFrequency.toString());
                                    }
                                    else {
                                        al.setRemainingAmount(climit.getAmount());
                                        al.setRemainingFrequency(climit.getFrequencyLength());

                                        logger.info("Executing Yearly Limit flow...");
                                        logger.info("Remaining Limit Amount [" + al.getRemainingAmount() + "]");
                                        logger.info("Remaining Frequency [" + al.getRemainingFrequency() + "]");
                                    }
                                    // =================================================================

                                } else {

                                    // Asim Shahzad, Date : 6th Oct 2020, Tracking ID : VC-NAP-202009231
                                    if(WebServiceUtil.limitcycleDateFormat.format(new Date()).equals(al.getCycleEndDate())) {

                                        logger.info("Executing Daily Limit flow...");
                                        logger.info("Product Debit Limit Amount: [" + climit.getAmount() + "]");
                                        logger.info("Current Debit Limit Amount: [" + al.getLimitId().getAmount() + "]");
                                        logger.info("Remaining Debit Limit Amount: [" + al.getRemainingAmount() + "]");

                                        Long temp1 = Long.valueOf(climit.getAmount());
                                        Long temp2 = Long.valueOf(al.getLimitId().getAmount());
                                        Long temp3 = Long.valueOf(al.getRemainingAmount());

                                        Long limitAmount = Long.valueOf(climit.getAmount()) - (Long.valueOf(al.getLimitId().getAmount()) - Long.valueOf(al.getRemainingAmount()));

                                        logger.info("Remaining Debit Limit Amount calculated : [" + limitAmount + "]");

                                        al.setRemainingAmount(StringUtils.leftPad(limitAmount.toString(), 12, '0'));

                                        Long remFrequency = Long.valueOf(climit.getFrequencyLength()) - (Long.valueOf(al.getLimitId().getFrequencyLength()) - Long.valueOf(al.getRemainingFrequency()));

                                        logger.info("Remaining Debit Frequency calculated : [" + remFrequency + "]");
                                        al.setRemainingFrequency(remFrequency.toString());
                                    }
                                    else {
                                        al.setRemainingAmount(climit.getAmount());
                                        al.setRemainingFrequency(climit.getFrequencyLength());

                                        logger.info("Executing Daily Limit flow...");
                                        logger.info("Remaining Debit Limit Amount [" + al.getRemainingAmount() + "]");
                                        logger.info("Remaining Debit Frequency [" + al.getRemainingFrequency() + "]");
                                    }


                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                    }
                                    al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                }

                                al.setLimitId(climit);
                                // =================================================================

                                GeneralDao.Instance.saveOrUpdate(al);
                                break;
                            }
                        }

                        if (!found) {
                            Integer cycleLength = 0;
                            CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                            acctLimit.setRelation(account.getAccountNumber());
                            acctLimit.setLimitId(climit);
                            acctLimit.setRemainingAmount(climit.getAmount());
                            acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                            cycleLength = Integer.parseInt(climit.getCycleLength());
                            acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                            DateTime cycleEndDate = DateTime.now();
                            Calendar calendar = Calendar.getInstance();
                            CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                            if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                            } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                            } else {
                                if (cycleLength > 1) {
                                    cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                }
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                            }
                            acctLimit.setIsCustProfile("0");
                            GeneralDao.Instance.saveOrUpdate(acctLimit);
                        }
                    }
                }

                if (cmsDebitLimitList.size() <= 0 && cmsCreditLimitList.size() <= 0) {
                    logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
                    return false;
                }

                //adding new channel auth of account
                List<String> newDebitChannelList;
                dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c"
                        + " where c.productId= :prdctid "
                        + " and c.limitType = :limittype ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                newDebitChannelList = GeneralDao.Instance.find(dbQuery, params);

                for (String channelId : newDebitChannelList) {
                    if (!existingDebitChannelList.contains(channelId)) {

                        if (existingAuthChannelList != null && existingAuthChannelList.size() > 0
                                && !existingAuthChannelList.contains(channelId)) {

                            if (Util.hasText(channelId)) {
                                CMSAuth cauth = new CMSAuth();
                                cauth.setRelation(account.getAccountNumber());
                                cauth.setAccount(account);
                                cauth.setCustomer(account.getCustomer());
                                cauth.setChannelId(channelId);
                                cauth.setOffset("0000");
                                //cauth.setRemRetries("3");
                                //cauth.setMaxRetries("3");
                                cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                                cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                                cauth.setReasonCode("0000");
                                cauth.setStatus("08");
                                cauth.setIsDefault("1");
                                GeneralDao.Instance.saveOrUpdate(cauth);
                            }
                        }

                        /*
                        dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                                " where c.productId= :prdctid " +
                                " and c.limitType = :limittype " +
                                //" and c.channelId = :channel " +
                                " and c.parentLimitId is null " +
                                " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                        params = new HashMap<String, Object>();
                        params.put("prdctid", product);
                        params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                        //params.put("channel", channelId);

                        if (Util.hasText(channelId)) {
                            dbQuery += "and c.channelId = :channel ";
                            params.put("channel", channelId);
                        } else {
                            dbQuery += "and c.channelId is null ";
                        }
                        cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                        if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                            for (CMSProductDebitLimit cmsProductLimit : cmsDebitLimitList) {

                                Integer cycleLength = 0;
                                CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                                acctLimit.setRelation(account.getAccountNumber());
                                acctLimit.setLimitId(cmsProductLimit);
                                acctLimit.setRemainingAmount(cmsProductLimit.getAmount());
                                acctLimit.setRemainingFrequency(cmsProductLimit.getFrequencyLength());
                                cycleLength = Integer.parseInt(cmsProductLimit.getCycleLength());
                                acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                DateTime cycleEndDate = DateTime.now();
                                if (cycleLength > 1) {
                                    cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                } else {
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                }
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                acctLimit.setIsCustProfile("0");
                                GeneralDao.Instance.saveOrUpdate(acctLimit);
                            }
                        }
                        */
                    }
                }

                //adding new channel auth of account
                List<String> newCreditChannelList;
                dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c"
                        + " where c.productId= :prdctid "
                        + " and c.limitType = :limittype ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                newCreditChannelList = GeneralDao.Instance.find(dbQuery, params);

                for (String channelId : newCreditChannelList) {
                    if (!existingCreditChannelList.contains(channelId)) {

                        if (existingAuthChannelList != null && existingAuthChannelList.size() > 0
                                && !existingAuthChannelList.contains(channelId)
                                && existingDebitChannelList != null && existingDebitChannelList.size() > 0
                                && !existingDebitChannelList.contains(channelId)
                                ) {
                            CMSAuth cauth = new CMSAuth();
                            cauth.setRelation(account.getAccountNumber());
                            cauth.setAccount(account);
                            cauth.setCustomer(account.getCustomer());
                            cauth.setChannelId(channelId);
                            cauth.setOffset("0000");
                            //cauth.setRemRetries("3");
                            //cauth.setMaxRetries("3");
                            cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                            cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                            cauth.setReasonCode("0000");
                            cauth.setStatus("08");
                            cauth.setIsDefault("1");
                            GeneralDao.Instance.saveOrUpdate(cauth);
                        }

                        /*
                        dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                                " where c.productId= :prdctid " +
                                " and c.limitType = :limittype " +
                                //" and c.channelId = :channel" +
                                " and c.parentLimitId is null " +
                                " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                        params = new HashMap<String, Object>();
                        params.put("prdctid", product);
                        params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                        //params.put("channel", channelId);

                        if (Util.hasText(channelId)) {
                            dbQuery += "and c.channelId = :channel ";
                            params.put("channel", channelId);
                        } else {
                            dbQuery += "and c.channelId is null ";
                        }
                        cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                        if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                            for (CMSProductCreditLimit cmsProductLimit : cmsCreditLimitList) {

                                Integer cycleLength = 0;
                                CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                                acctLimit.setRelation(account.getAccountNumber());
                                acctLimit.setLimitId(cmsProductLimit);
                                acctLimit.setRemainingAmount(cmsProductLimit.getAmount());
                                acctLimit.setRemainingFrequency(cmsProductLimit.getFrequencyLength());
                                cycleLength = Integer.parseInt(cmsProductLimit.getCycleLength());
                                acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                DateTime cycleEndDate = DateTime.now();
                                if (cycleLength > 1) {
                                    cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                } else {
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                }
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                acctLimit.setIsCustProfile("0");
                                GeneralDao.Instance.saveOrUpdate(acctLimit);
                            }
                        }
                        */
                    }
                }

            } else {
                logger.error("No upgraded Wallet Product found to assign.. rejecting txn..");
                return false;
            }

            logger.info("Saving wallet upgrade activity ....");
            CMSActivityLog cmsActivityLog = new CMSActivityLog();
            cmsActivityLog.setRelation(account.getAccountNumber());
            cmsActivityLog.setPreviousStatus(prevWalletStatus);
            cmsActivityLog.setCurrentStatus(account.getLevel());
            cmsActivityLog.setSourceType("API");
            cmsActivityLog.setSourceName(serviceName);
            cmsActivityLog.setActivityDateTime(DateTime.now());
            GeneralDao.Instance.saveOrUpdate(cmsActivityLog);

            logger.info("Wallet Account Profile upgraded successfully");
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Upgrading Wallet Account profile to Level 1!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean SoftDeleteWalletProfile(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            logger.info("Soft Deleting Wallet Account Profile...");
//            if(!UpgradeFraudProfile(wsmodel)) //Raza Donot Delete Fraud Profile. Confirm from Shoaib bhai
//            {
//                logger.error("Unable to Upgrade Fraud Profile for cnic [" + wsmodel.getCnic() + "]");
//                return false;
//            }
            if(account == null) //This case will never happen as previous check of status
            {
                logger.error("No Account found for customer [" + wsmodel.getUserid() + "], rejecting...");
                return false;
            }

            if(wsmodel.getDeletetype().equals("KYC")) {
                logger.info("Soft Deleting Wallet through KYC ...");
                account.getCustomer().setStatus("07"); //TODO: Raza Verify and Update This -- Once Soft Delete as it is through AML, customer should be soft deleted
                account.setStatus("07"); //TODO: Raza Update This
            }
            else //it will always be AML
            {
                logger.info("Soft Deleting Wallet through AML ...");
                account.getCustomer().setStatus("09"); //TODO: Raza Verify and Update This -- Once Soft Delete as it is through AML, customer should be soft deleted
                account.setStatus("09"); //TODO: Raza Update This
            }
            account.setLastUpdateDate(new Date());
            GeneralDao.Instance.saveOrUpdate(account.getCustomer());
            GeneralDao.Instance.saveOrUpdate(account);
            logger.info("Wallet Account Profile upgraded successfully");
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Soft Deleting Upgrading Wallet Account profile to Level 1!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean ActivateLevelZeroWalletProfile(CMSAccount account)
    {
        try
        {
            logger.info("Activating Wallet Account Profile...");

            String dbQuery;
            Map<String, Object> params;
            if(account == null)
            {
                logger.error("No Account found while activating provisional profile of Level0, rejecting...");
                return false;
            }

            //getting existing channel list of account
            List<String> existingDebitChannelList;
            dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c"
                    + " where c.productId= :prdctid "
                    + " and c.limitType = :limittype ";
            params = new HashMap<String, Object>();
            params.put("prdctid", account.getProduct());
            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
            existingDebitChannelList = GeneralDao.Instance.find(dbQuery, params);

            List<String> existingCreditChannelList;
            dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c"
                    + " where c.productId= :prdctid "
                    + " and c.limitType = :limittype ";
            params = new HashMap<String, Object>();
            params.put("prdctid", account.getProduct());
            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
            existingCreditChannelList = GeneralDao.Instance.find(dbQuery, params);

            account.setCategory(AccType.CAT_WALLET.StringValue());
            account.setUpgradedate(new Date());
            List<CMSProduct> productlist;
            dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
            params = new HashMap<String, Object>();
            params.put("prdcttype", "LVL0");
            params.put("default", "1");
            productlist = GeneralDao.Instance.find(dbQuery, params);

            if (productlist != null && productlist.size() > 0) {
                //Raza ignore if there are multiple default wallet products
                logger.info("Assigning Wallet Product to Account..");
                CMSProduct product = productlist.get(0);
                account.setProduct(product);
                GeneralDao.Instance.saveOrUpdate(account);

                List<CMSProductDebitLimit> cmsDebitLimitList;
                dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c " +
                        " where c.productId= :prdctid " +
                        " and c.limitType = :limittype " +
                        " and c.parentLimitId is null " +
                        " and c.limitCategory = 2";     //limit category 2 is Financial limit
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                List<CMSDebitRemainingLimit> currentDebitList;
                dbQuery = "from " + CMSDebitRemainingLimit.class.getName() + " c where c.relation= :acct ";
                params = new HashMap<String, Object>();
                params.put("acct", account.getAccountNumber());
                currentDebitList = GeneralDao.Instance.find(dbQuery, params);

                List<CMSProductCreditLimit> cmsCreditLimitList;
                dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c " +
                        " where c.productId= :prdctid " +
                        " and c.limitType = :limittype " +
                        " and c.parentLimitId is null " +
                        " and c.limitCategory = 2";     //limit category 2 is Financial limit
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                List<CMSCreditRemainingLimit> currentCreditList;
                dbQuery = "from " + CMSCreditRemainingLimit.class.getName() + " c where c.relation= :acct ";
                params = new HashMap<String, Object>();
                params.put("acct", account.getAccountNumber());
                currentCreditList = GeneralDao.Instance.find(dbQuery, params);

                if((currentDebitList == null || currentDebitList.size() <= 0)
                    && (currentCreditList == null || currentCreditList.size() <= 0))
                {
                    logger.error("No Limit Found for Account [" + account.getAccountNumber() + "], rejecting...");
                    return false;
                }

                if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                    for (CMSProductDebitLimit climit : cmsDebitLimitList) {
                        boolean found = false;
                        for (CMSDebitRemainingLimit al : currentDebitList) {
                            if (al.getLimitId().getTransactionType() != null && al.getLimitId().getTransactionType().equals(climit.getTransactionType())
                                    && (Util.hasText(al.getLimitId().getChannelId()) && al.getLimitId().getChannelId().equals(climit.getChannelId()))) {
                                found = true;
                                al.setLimitId(climit);
                                al.setRemainingAmount(climit.getAmount());
                                al.setRemainingFrequency(climit.getFrequencyLength());
                                al.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                GeneralDao.Instance.saveOrUpdate(al);
                                break;
                            }
                        }

                        if (!found) {
                            Integer cycleLength = 0;
                            CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                            acctLimit.setRelation(account.getAccountNumber());
                            acctLimit.setLimitId(climit);
                            acctLimit.setRemainingAmount(climit.getAmount());
                            acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                            cycleLength = Integer.parseInt(climit.getCycleLength());
                            acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                            DateTime cycleEndDate = DateTime.now();
                            if (cycleLength > 1) {
                                cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                            } else {
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                            }
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                            acctLimit.setIsCustProfile("0");
                            GeneralDao.Instance.saveOrUpdate(acctLimit);
                        }
                    }
                }

                if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                    for (CMSProductCreditLimit climit : cmsCreditLimitList) {
                        boolean found = false;
                        for (CMSCreditRemainingLimit al : currentCreditList) {
                            if (al.getLimitId().getTransactionType() != null && al.getLimitId().getTransactionType().equals(climit.getTransactionType())
                                    && (Util.hasText(al.getLimitId().getChannelId()) && al.getLimitId().getChannelId().equals(climit.getChannelId()))) {
                                found = true;
                                al.setLimitId(climit);
                                al.setRemainingAmount(climit.getAmount());
                                al.setRemainingFrequency(climit.getFrequencyLength());
                                al.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                al.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                GeneralDao.Instance.saveOrUpdate(al);
                                break;
                            }
                        }

                        if (!found) {
                            Integer cycleLength = 0;
                            CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                            acctLimit.setRelation(account.getAccountNumber());
                            acctLimit.setLimitId(climit);
                            acctLimit.setRemainingAmount(climit.getAmount());
                            acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                            cycleLength = Integer.parseInt(climit.getCycleLength());
                            acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                            DateTime cycleEndDate = DateTime.now();
                            if (cycleLength > 1) {
                                cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                            } else {
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                            }
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                            acctLimit.setIsCustProfile("0");
                            GeneralDao.Instance.saveOrUpdate(acctLimit);
                        }
                    }
                }

                if (cmsDebitLimitList.size() <= 0 && cmsCreditLimitList.size() <= 0) {
                    logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
                    return false;
                }

                //adding new channel auth of account
                List<String> newDebitChannelList;
                dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c"
                        + " where c.productId= :prdctid "
                        + " and c.limitType = :limittype ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                newDebitChannelList = GeneralDao.Instance.find(dbQuery, params);

                for (String channelId : newDebitChannelList) {
                    if (!existingDebitChannelList.contains(channelId)) {

                        if (Util.hasText(channelId)) {
                            CMSAuth cauth = new CMSAuth();
                            cauth.setRelation(account.getAccountNumber());
                            cauth.setAccount(account);
                            cauth.setCustomer(account.getCustomer());
                            cauth.setChannelId(channelId);
                            cauth.setOffset("0000");
                            //cauth.setRemRetries("3");
                            //cauth.setMaxRetries("3");
                            cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                            cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                            cauth.setReasonCode("0000");
                            cauth.setStatus("08");
                            cauth.setIsDefault("1");
                            GeneralDao.Instance.saveOrUpdate(cauth);
                        }

                        dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                                " where c.productId= :prdctid " +
                                " and c.limitType = :limittype " +
                                " and c.channelId = :channel" +
                                " and c.parentLimitId is null " +
                                " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                        params = new HashMap<String, Object>();
                        params.put("prdctid", product);
                        params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                        //params.put("channel", channelId);

                        if (Util.hasText(channelId)) {
                            dbQuery += "and c.channelId = :channel ";
                            params.put("channel", channelId);
                        } else {
                            dbQuery += "and c.channelId is null ";
                        }
                        cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                        if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                            for (CMSProductDebitLimit cmsProductLimit : cmsDebitLimitList) {

                                Integer cycleLength = 0;
                                CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                                acctLimit.setRelation(account.getAccountNumber());
                                acctLimit.setLimitId(cmsProductLimit);
                                acctLimit.setRemainingAmount(cmsProductLimit.getAmount());
                                acctLimit.setRemainingFrequency(cmsProductLimit.getFrequencyLength());
                                cycleLength = Integer.parseInt(cmsProductLimit.getCycleLength());
                                acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                DateTime cycleEndDate = DateTime.now();
                                if (cycleLength > 1) {
                                    cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                } else {
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                }
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                acctLimit.setIsCustProfile("0");
                                GeneralDao.Instance.saveOrUpdate(acctLimit);
                            }
                        }
                    }
                }

                //adding new channel auth of account
                List<String> newCreditChannelList;
                dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c"
                        + " where c.productId= :prdctid "
                        + " and c.limitType = :limittype ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                newCreditChannelList = GeneralDao.Instance.find(dbQuery, params);

                for (String channelId : newCreditChannelList) {
                    if (!existingCreditChannelList.contains(channelId)) {

                        if (Util.hasText(channelId)) {
                            CMSAuth cauth = new CMSAuth();
                            cauth.setRelation(account.getAccountNumber());
                            cauth.setAccount(account);
                            cauth.setCustomer(account.getCustomer());
                            cauth.setChannelId(channelId);
                            cauth.setOffset("0000");
                            //cauth.setRemRetries("3");
                            //cauth.setMaxRetries("3");
                            cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                            cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                            cauth.setReasonCode("0000");
                            cauth.setStatus("08");
                            cauth.setIsDefault("1");
                            GeneralDao.Instance.saveOrUpdate(cauth);
                        }

                        dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                                " where c.productId= :prdctid " +
                                " and c.limitType = :limittype " +
                                " and c.channelId = :channel" +
                                " and c.parentLimitId is null " +
                                " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                        params = new HashMap<String, Object>();
                        params.put("prdctid", product);
                        params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                        //params.put("channel", channelId);

                        if (Util.hasText(channelId)) {
                            dbQuery += "and c.channelId = :channel ";
                            params.put("channel", channelId);
                        } else {
                            dbQuery += "and c.channelId is null ";
                        }
                        cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                        if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                            for (CMSProductCreditLimit cmsProductLimit : cmsCreditLimitList) {

                                Integer cycleLength = 0;
                                CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                                acctLimit.setRelation(account.getAccountNumber());
                                acctLimit.setLimitId(cmsProductLimit);
                                acctLimit.setRemainingAmount(cmsProductLimit.getAmount());
                                acctLimit.setRemainingFrequency(cmsProductLimit.getFrequencyLength());
                                cycleLength = Integer.parseInt(cmsProductLimit.getCycleLength());
                                acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                DateTime cycleEndDate = DateTime.now();
                                if (cycleLength > 1) {
                                    cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                } else {
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                }
                                acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                acctLimit.setIsCustProfile("0");
                                GeneralDao.Instance.saveOrUpdate(acctLimit);
                            }
                        }
                    }
                }

            } else {
                logger.error("No upgraded Wallet Product found to assign.. rejecting txn..");
                return false;
            }

            logger.info("Wallet Account Profile Activated successfully");
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Activating Wallet Account profile for Level 0!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean LinkAccount(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            logger.info("Linking Bank Account...");
            String dbQuery;
            Map<String, Object> params;
            if(account != null)
            {
                CMSAccount linkedaccount = new CMSAccount();
                linkedaccount.setCustomer(account.getCustomer()); //wsmodel.getCnic());

                // Asim Shahzad, Date : 16th Sep 2020, Tracking ID : VC-NAP-202009163
                //linkedaccount.setAccountTitle("Linked "+account.getAccountTitle());
                linkedaccount.setAccountTitle(account.getAccountTitle());
                // ==================================================================

                linkedaccount.setAvailableBalance("000000000000");
                linkedaccount.setActualBalance("000000000000");
                DateFormat dateFormat = new SimpleDateFormat("MMddhhmmss");
                linkedaccount.setCreateDate(new Date());
                //linkedaccount.setCurrency(wsmodel.getCurrency());
                linkedaccount.setLastUpdateDate(new Date());
                linkedaccount.setStatus("00");

                Currency currency = GlobalContext.getInstance().getCurrencybySwiftCode(wsmodel.getAccountcurrency());
                if (currency != null) {
                    linkedaccount.setCurrency(currency.getCode().toString());
                } else {
                    linkedaccount.setCurrency(GlobalContext.getInstance().getBaseCurrency().getCode().toString());
                }
                //linkedaccount.setCurrency("586");

                linkedaccount.setAccountType(AccType.CURRENT.toString());
                //m.rehman for NayaPay, setting bin from bankcode
                //linkedaccount.setBranchId(wsmodel.getBank());
                linkedaccount.setBranchId(GlobalContext.getInstance().getBinByBankCode(wsmodel.getBankcode()));
                linkedaccount.setAcctalias(wsmodel.getAcctalias());
                linkedaccount.setUserId(wsmodel.getUserid());
                linkedaccount.setAcctId(wsmodel.getAcctid());
                if(wsmodel.getIsprimary().equals("true"))
                {
                    linkedaccount.setIsprimary("1");
                }
                else
                {
                    linkedaccount.setIsprimary("0");
                }

                linkedaccount.setLevel(account.getLevel());
                linkedaccount.setCategory(AccType.CAT_LINKED.StringValue());

                params = null;
                List<CMSProduct> productlist;
                params = new HashMap<String, Object>();
                dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
                params = new HashMap<String, Object>();
                if(account.getLevel().equals(AccType.LEVEL_ZERO.toString()))
                {
                    logger.info("Linking Account for Level Zero wallet..");
                    params.put("prdcttype", "LNK_LVL0");

                }
                else if(account.getLevel().equals(AccType.LEVEL_ONE.toString()))
                {
                    logger.info("Linking Account for Level One wallet..");
                    params.put("prdcttype", "LNK_LVL1");
                }
                else
                {
                    logger.error("Cannot Link Account for Invalid Wallet Level [" + account.getLevel() + "]");
                    return false;
                }

                params.put("default", "1");
                productlist = GeneralDao.Instance.find(dbQuery, params);

                if (productlist != null && productlist.size() > 0) {
                    //Raza ignore if there are multiple default wallet products
                    logger.info("Assigning Wallet Product to Account..");
                    CMSProduct product = productlist.get(0);
                    linkedaccount.setProduct(product);
                    linkedaccount.setAccountNumber(wsmodel.getAccountnumber());
                    GeneralDao.Instance.saveOrUpdate(linkedaccount);

                    //Raza User-Id To Account Relation
                    NayaPayRelation npr = new NayaPayRelation();
                    npr.setUser_Acct_Id(wsmodel.getAcctid());
                    npr.setAccount(linkedaccount);
                    GeneralDao.Instance.saveOrUpdate(npr);
                    //Raza User-Id To Account Relation

                    if (AssignAccountLimits(product, account, wsmodel)) {
                        logger.info("Linked Account limits assign successfully ...");
                        //logger.info("Linked Account Profile created successfully");

                    } else {
                        logger.info("Unable assign Linked Account limits, rejecting ...");
                        //GeneralDao.Instance.evict(account); //Raza Dont save if declined
                        //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                        GeneralDao.Instance.clear();
                        GeneralDao.Instance.saveOrUpdate(wsmodel);
                        return false;
                    }

                    /*
                    List<String> channelDebitList, channelCreditList;
                    List<CMSProductDebitLimit> cmsDebitLimitList;
                    List<CMSProductCreditLimit> cmsCreditLimitList;

                    dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product);
                    params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                    channelDebitList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelDebitList != null && channelDebitList.size() > 0) {
                        for (String channel : channelDebitList) {

                            CMSAuth cauth = new CMSAuth();
                            cauth.setRelation(linkedaccount.getAccountNumber());
                            cauth.setAccount(linkedaccount);
                            cauth.setCustomer(linkedaccount.getCustomer());
                            cauth.setChannelId(channel);
                            cauth.setOffset("0000");
                            cauth.setRemRetries("3");
                            cauth.setMaxRetries("3");
                            cauth.setReasonCode("0000");
                            cauth.setStatus("08");
                            cauth.setIsDefault("1");
                            GeneralDao.Instance.saveOrUpdate(cauth);

                            dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    " and c.channelId = :channel" +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product);
                            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                            params.put("channel", channel);
                            cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                                for (CMSProductDebitLimit climit : cmsDebitLimitList) {
                                    Integer cycleLength = 0;
                                    CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                                    acctLimit.setRelation(linkedaccount.getAccountNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product);
                    params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                    channelCreditList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelCreditList != null && channelCreditList.size() > 0) {
                        for (String channel : channelCreditList) {

                            CMSAuth cauth = new CMSAuth();
                            cauth.setRelation(linkedaccount.getAccountNumber());
                            cauth.setAccount(linkedaccount);
                            cauth.setCustomer(linkedaccount.getCustomer());
                            cauth.setChannelId(channel);
                            cauth.setOffset("0000");
                            cauth.setRemRetries("3");
                            cauth.setMaxRetries("3");
                            cauth.setReasonCode("0000");
                            cauth.setStatus("08");
                            cauth.setIsDefault("1");
                            GeneralDao.Instance.saveOrUpdate(cauth);

                            dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    " and c.channelId = :channel" +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product);
                            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                            params.put("channel", channel);
                            cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                                for (CMSProductCreditLimit climit : cmsCreditLimitList) {
                                    Integer cycleLength = 0;
                                    CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                                    acctLimit.setRelation(linkedaccount.getAccountNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    if (channelDebitList.size() <= 0 && channelCreditList.size() <= 0) {
                        logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
                        GeneralDao.Instance.evict(linkedaccount); //Raza Dont save if declined
                        //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                        return false;
                    }
                    */

                } else {
                    logger.error("No Linked Account Product found to assign.. rejecting txn..");
                    GeneralDao.Instance.evict(linkedaccount);//no account object saved
                    //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                    return false;
                }

                logger.info("Linked Account Profile created successfully");
            }
            else
            {
                logger.info("No Wallet Found for Customer, cannot link account! rejecting..");
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean LinkAccountOTP(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            logger.info("Linking Bank Account...");
            String dbQuery;
            Map<String, Object> params;
            if(account != null)
            {
                CMSAccount linkedaccount = new CMSAccount();
                linkedaccount.setCustomer(account.getCustomer()); //wsmodel.getCnic());

                // Asim Shahzad, Date : 16th Sep 2020, Tracking ID : VC-NAP-202009163
                //linkedaccount.setAccountTitle("Linked "+account.getAccountTitle());
                linkedaccount.setAccountTitle(account.getAccountTitle());
                // ==================================================================

                linkedaccount.setAvailableBalance("000000000000");
                linkedaccount.setActualBalance("000000000000");
                DateFormat dateFormat = new SimpleDateFormat("MMddhhmmss");
                linkedaccount.setCreateDate(new Date());
                //linkedaccount.setCurrency(wsmodel.getCurrency());
                linkedaccount.setLastUpdateDate(new Date());
                linkedaccount.setStatus("OTP");

                Currency currency = GlobalContext.getInstance().getCurrencybySwiftCode(wsmodel.getAccountcurrency());
                if (currency != null) {
                    linkedaccount.setCurrency(currency.getCode().toString());
                } else {
                    linkedaccount.setCurrency(GlobalContext.getInstance().getBaseCurrency().getCode().toString());
                }
                //linkedaccount.setCurrency("586");

                linkedaccount.setAccountType(AccType.CURRENT.toString());
                //m.rehman for NayaPay, setting bin from bankcode
                //linkedaccount.setBranchId(wsmodel.getBank());
                linkedaccount.setBranchId(GlobalContext.getInstance().getBinByBankCode(wsmodel.getBankcode()));
                linkedaccount.setAcctalias(wsmodel.getAcctalias());
                linkedaccount.setUserId(wsmodel.getUserid());
                linkedaccount.setAcctId(wsmodel.getAcctid());
                if(wsmodel.getIsprimary().equals("true"))
                {
                    linkedaccount.setIsprimary("1");
                }
                else
                {
                    linkedaccount.setIsprimary("0");
                }

                linkedaccount.setLevel(account.getLevel());
                linkedaccount.setCategory(AccType.CAT_LINKED.StringValue());

                params = null;
                List<CMSProduct> productlist;
                params = new HashMap<String, Object>();
                dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
                params = new HashMap<String, Object>();
                if(account.getLevel().equals(AccType.LEVEL_ZERO.toString()))
                {
                    logger.info("Linking Account for Level Zero wallet..");
                    params.put("prdcttype", "LNK_LVL0");

                }
                else if(account.getLevel().equals(AccType.LEVEL_ONE.toString()))
                {
                    logger.info("Linking Account for Level One wallet..");
                    params.put("prdcttype", "LNK_LVL1");
                }
                else
                {
                    logger.error("Cannot Link Account for Invalid Wallet Level [" + account.getLevel() + "]");
                    return false;
                }

                params.put("default", "1");
                productlist = GeneralDao.Instance.find(dbQuery, params);

                if (productlist != null && productlist.size() > 0) {
                    //Raza ignore if there are multiple default wallet products
                    logger.info("Assigning Wallet Product to Account..");
                    CMSProduct product = productlist.get(0);
                    linkedaccount.setProduct(product);
                    linkedaccount.setAccountNumber(wsmodel.getAccountnumber());
                    GeneralDao.Instance.saveOrUpdate(linkedaccount);

                    //Raza User-Id To Account Relation
                    NayaPayRelation npr = new NayaPayRelation();
                    npr.setUser_Acct_Id(wsmodel.getAcctid());
                    npr.setAccount(linkedaccount);
                    GeneralDao.Instance.saveOrUpdate(npr);
                    //Raza User-Id To Account Relation

                    if (AssignAccountLimits(product, account, wsmodel)) {
                        logger.info("Linked Account limits assign successfully ...");
                        //logger.info("Linked Account Profile created successfully");

                    } else {
                        logger.info("Unable assign Linked Account limits, rejecting ...");
                        //GeneralDao.Instance.evict(account); //Raza Dont save if declined
                        //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                        GeneralDao.Instance.clear();
                        GeneralDao.Instance.saveOrUpdate(wsmodel);
                        return false;
                    }

                    /*
                    List<String> channelDebitList, channelCreditList;
                    List<CMSProductDebitLimit> cmsDebitLimitList;
                    List<CMSProductCreditLimit> cmsCreditLimitList;

                    dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product);
                    params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                    channelDebitList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelDebitList != null && channelDebitList.size() > 0) {
                        for (String channel : channelDebitList) {

                            if (Util.hasText(channel)) {
                                CMSAuth cauth = new CMSAuth();
                                cauth.setRelation(linkedaccount.getAccountNumber());
                                cauth.setAccount(linkedaccount);
                                cauth.setCustomer(linkedaccount.getCustomer());
                                cauth.setChannelId(channel);
                                cauth.setOffset("0000");
                                cauth.setRemRetries("3");
                                cauth.setMaxRetries("3");
                                cauth.setReasonCode("0000");
                                cauth.setStatus("08");
                                cauth.setIsDefault("1");
                                GeneralDao.Instance.saveOrUpdate(cauth);
                            }

                            dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    //" and c.channelId = :channel" +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product);
                            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                            //params.put("channel", channel);

                            if (Util.hasText(channel)) {
                                dbQuery += " and c.channelId = :channel ";
                                params.put("channel", channel);
                            } else {
                                dbQuery += " and c.channelId is null ";
                            }
                            cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                                for (CMSProductDebitLimit climit : cmsDebitLimitList) {
                                    Integer cycleLength = 0;
                                    CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                                    acctLimit.setRelation(linkedaccount.getAccountNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c" +
                            " where c.productId= :prdctid " +
                            " and c.limitType = :limittype ";
                    params = new HashMap<String, Object>();
                    params.put("prdctid", product);
                    params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                    channelCreditList = GeneralDao.Instance.find(dbQuery, params);

                    if (channelCreditList != null && channelCreditList.size() > 0) {
                        for (String channel : channelCreditList) {

                            if (Util.hasText(channel)) {
                                CMSAuth cauth = new CMSAuth();
                                cauth.setRelation(linkedaccount.getAccountNumber());
                                cauth.setAccount(linkedaccount);
                                cauth.setCustomer(linkedaccount.getCustomer());
                                cauth.setChannelId(channel);
                                cauth.setOffset("0000");
                                cauth.setRemRetries("3");
                                cauth.setMaxRetries("3");
                                cauth.setReasonCode("0000");
                                cauth.setStatus("08");
                                cauth.setIsDefault("1");
                                GeneralDao.Instance.saveOrUpdate(cauth);
                            }

                            dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                                    " where c.productId= :prdctid " +
                                    " and c.limitType = :limittype " +
                                    " and c.channelId = :channel" +
                                    " and c.parentLimitId is null " +
                                    " and c.limitCategory = 2";     //limit category 2 is Financial limit;
                            params = new HashMap<String, Object>();
                            params.put("prdctid", product);
                            params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                            //params.put("channel", channel);

                            if (Util.hasText(channel)) {
                                dbQuery += " and c.channelId = :channel ";
                                params.put("channel", channel);
                            } else {
                                dbQuery += " and c.channelId is null ";
                            }
                            cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                            if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                                for (CMSProductCreditLimit climit : cmsCreditLimitList) {
                                    Integer cycleLength = 0;
                                    CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                                    acctLimit.setRelation(linkedaccount.getAccountNumber());
                                    acctLimit.setLimitId(climit);
                                    acctLimit.setRemainingAmount(climit.getAmount());
                                    acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                                    cycleLength = Integer.parseInt(climit.getCycleLength());
                                    acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                                    DateTime cycleEndDate = DateTime.now();
                                    if (cycleLength > 1) {
                                        cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                                    } else {
                                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    }
                                    acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                                    acctLimit.setIsCustProfile("0");
                                    GeneralDao.Instance.saveOrUpdate(acctLimit);
                                }
                            }
                        }
                    }

                    if (channelDebitList.size() <= 0 && channelCreditList.size() <= 0) {
                        logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
                        GeneralDao.Instance.evict(linkedaccount); //Raza Dont save if declined
                        //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                        return false;
                    }
                    */

                } else {
                    logger.error("No Wallet Product found to assign.. rejecting txn..");
                    GeneralDao.Instance.evict(linkedaccount);//no account object saved
                    //GeneralDao.Instance.evict(customer); //Raza Dont save if declined
                    return false;
                }
                logger.info("Linked Account Profile created successfully");
            }
            else
            {
                logger.info("No Wallet Found for Customer, cannot link account! rejecting..");
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String GenerateWalletAccountNumber(CMSProduct product) //Raza Using for both Card and Account Number
    {
        //Set Account Number start
        CardNumberGenerator obj_CNGen = new CardNumberGenerator();
        Integer panSequenceLength = 16 - (product.getBin().length() + 1);
        String strpanSequence = product.getProductDetail().getPanRange().getLastValue();
        Integer panSequence = Integer.parseInt(strpanSequence);
        panSequence++;
        product.getProductDetail().getPanRange().setLastValue(panSequence+"");
        GeneralDao.Instance.saveOrUpdate(product.getProductDetail().getPanRange()); //Raza Update last Value in DB
        if(strpanSequence.length() < panSequenceLength)
        {
            strpanSequence = StringUtils.leftPad(panSequence.toString(), panSequenceLength, "0");
        }
        return obj_CNGen.generate(product.getBin(), strpanSequence);
        //Set Account Number end
    }

    //m.rehman: 06-08-2020, Euronet Integration, PAN generation according to pan format defined for scheme
    public static String GeneratePAN(CMSProduct product)
    {
        String finalPan = "";
        String panRangeMaxValue = "";
        String panRangeMinValue = "";

        try {
            //Set Account Number start
            CardNumberGenerator obj_CNGen = new CardNumberGenerator();
            CMSPANFormat cmsPanFormat = product.getProductDetail().getPanFormatId();
            List<CMSPANFormatFields> cmsPanFormatFieldsList = cmsPanFormat.getCmsPanFormatFieldsList();

            // Asim Shahzad, Date : 12th July 2021, Tracking ID : VC-NAP-202107121
            int panSequence=0;
            CMSCardSequenceTracker objCardSeqTracker = new CMSCardSequenceTracker();
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date();

            String dbQuery;
            Map<String, Object> params;
            Boolean generateSequence = true;
            // ===================================================================

            Integer panLength = Integer.parseInt(cmsPanFormat.getPanLength());
            Integer panSequenceLength = 0/*, panSequence*/;
            String imdValue = "", productCodeValue = "", panRangeLastValue= "";

            for (CMSPANFormatFields cmspanFormatFields : cmsPanFormatFieldsList) {
                if (cmspanFormatFields.getFieldName().equals("IMD")) {
                    imdValue = cmspanFormatFields.getValue();
                    logger.info("IMD [" + imdValue + "]");
                }

                if (cmspanFormatFields.getFieldName().equals("Product Code")) {
                    productCodeValue = cmspanFormatFields.getValue();
                    logger.info("Product Code [" + productCodeValue + "]");
                }

                if (cmspanFormatFields.getFieldName().equals("Sequence")) {
                    panSequenceLength = Integer.parseInt(cmspanFormatFields.getLength());
                    logger.info("Sequence [" + panSequenceLength + "]");
                }
            }

            // Asim Shahzad, Date : 12th July 2021, Tracking ID : VC-NAP-202107121
//            panRangeLastValue = product.getProductDetail().getPanRange().getLastValue();
//            logger.info("panRangeLastValue [" + panRangeLastValue + "]");
//            panSequence = Integer.parseInt(panRangeLastValue);
//            panSequence++;
//            logger.info("panSequence [" + panSequence + "]");
//            product.getProductDetail().getPanRange().setLastValue(panSequence + "");
//            GeneralDao.Instance.saveOrUpdate(product.getProductDetail().getPanRange()); //Raza Update last Value in DB

            int lowerRange = Integer.valueOf(product.getProductDetail().getPanRange().getLowRange());
            int upperRange = Integer.valueOf(product.getProductDetail().getPanRange().getHighRange());

            // Checking if PAN range is exhausted
            dbQuery = "from " + CMSCardSequenceTracker.class.getName() + " c where c.bin = :IMD and c.sequenceValue between :lowerRange and :upperRange ";
            params = new HashMap<String, Object>();
            params.put("IMD", imdValue);
            params.put("lowerRange", String.valueOf(lowerRange));
            params.put("upperRange", String.valueOf(upperRange));

            String query = "select count(*) " + dbQuery;

            Long size = (Long)GeneralDao.Instance.findObject(query, params);

            logger.info("Random sequence number count [" + size + "]");

            if(size < upperRange) {
                dbQuery = "from " + CMSCardSequenceTracker.class.getName() + " c where c.bin = :IMD and c.sequenceValue between :lowerRange and :upperRange ";
                params = new HashMap<String, Object>();
                params.put("IMD", imdValue);
                params.put("lowerRange", String.valueOf(lowerRange));
                params.put("upperRange", String.valueOf(upperRange));

                int[] arrRandomNumbers = Util.convertListToArray(GeneralDao.Instance.find(dbQuery, params));

                while (generateSequence) {
                    SecureRandom secRan = null;
                    try {
                        secRan = SecureRandom.getInstance("NativePRNG");
                    } catch (NoSuchAlgorithmException e) {
                        logger.error("Error occurred while random number generation...");
                        logger.error(e);
                    }

                    panRangeMaxValue = product.getProductDetail().getPanRange().getHighRange().substring(0, panSequenceLength);
                    panSequence = secRan.nextInt(Integer.valueOf(panRangeMaxValue));

                    if (product.getProductDetail().getPanRange().getLowRange().length() < panSequenceLength)
                        panRangeMinValue = product.getProductDetail().getPanRange().getLowRange();
                    else
                        panRangeMinValue = product.getProductDetail().getPanRange().getLowRange().substring(0, panSequenceLength);

                    while (!(panSequence >= Integer.valueOf(panRangeMinValue) && panSequence < Integer.valueOf(panRangeMaxValue))) {
                        panSequence = secRan.nextInt(Integer.valueOf(panRangeMaxValue));
                    }

                    if (!Util.binarySearch(arrRandomNumbers, panSequence)) {
                        objCardSeqTracker.setSequenceValue(String.valueOf(panSequence));
                        objCardSeqTracker.setInsertedOn(dateFormat.format(date));
                        objCardSeqTracker.setBin(imdValue);
                        GeneralDao.Instance.save(objCardSeqTracker);

                        generateSequence = false;
                        panRangeLastValue = String.valueOf(panSequence);
                    }
                }

                if (panRangeLastValue.length() < panSequenceLength) {
                    panRangeLastValue = StringUtils.leftPad(panRangeLastValue, panSequenceLength, "0");
                    logger.debug("panRangeLastValue [" + panRangeLastValue + "]");
                }
                // ===================================================================

                finalPan = obj_CNGen.generate(imdValue, productCodeValue, panRangeLastValue);
                logger.debug("finalPan [" + finalPan + "]");
                if (Util.hasText(finalPan) && finalPan.length() != panLength) {
                    logger.error("Generated PAN length [" + finalPan.length() + "] is not equal to defined pan length [" + panLength + "]");
                    throw new Exception("Generated PAN length [" + finalPan.length() + "] is not equal to defined pan length [" + panLength + "]");
                }
            }
            else {
                logger.error("PAN Range exhausted for product [" + product.getProductId() + "-" + product.getProductName() + "]");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception => " + e.getMessage());
        }

        return finalPan;
    }

    public static boolean ValidatePIN(WalletCMSWsEntity wsmodel, CMSAuth cr)
    {
        logger.info("Validating PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().PINValidation(wsmodel, cr)) {
                logger.error("Unable to validate PIN for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while validating PIN!");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            return false;
        }

        return true;
    }

    public static boolean ChangePIN(WalletCMSWsEntity wsmodel, List<CMSAuth> crlist)
    {
        logger.info("Changing PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().PINChange(wsmodel, crlist)) {
                logger.error("Unable to change PIN for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while changing PIN!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean GeneratePIN(WalletCMSWsEntity wsmodel, CMSAuth cr)
    {
        logger.info("Generating PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().PINGeneration(wsmodel, cr)) {
                logger.error("Unable to generate PIN for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating PIN!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean ValidateCardPIN(WalletCMSWsEntity wsmodel, CMSAuth cr)
    {
        logger.info("Validating Card PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().CardPINValidation(wsmodel, cr)) {
                logger.error("Unable to validate Card PIN for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while validating Card PIN!");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            return false;
        }

        return true;
    }

    public static boolean ChangeCardPIN(WalletCMSWsEntity wsmodel, CMSAuth cr)
    {
        logger.info("Changing Card PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().CardPINChange(wsmodel, cr)) {
                logger.error("Unable to change Card PIN for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while changing Card PIN!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean ChangeCardPIN(WalletCMSWsEntity wsmodel, List<CMSAuth> crlist)
    {
        logger.info("Changing Card PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().CardPINChange(wsmodel, crlist)) {
                logger.error("Unable to change Card PIN for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc -- Raza overwriting...
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while changing Card PIN!");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            return false;
        }

        return true;
    }

    public static boolean GenerateCardPIN(WalletCMSWsEntity wsmodel, List<CMSAuth> crlist)
    {
        logger.info("Generating Card PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().CardPINGeneration(wsmodel, crlist)) {
                logger.error("Unable to generate PIN for Card from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating Card PIN!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean GeneratePIN(WalletCMSWsEntity wsmodel, List<CMSAuth> crlist)
    {
        logger.info("Generating PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().PINGeneration(wsmodel, crlist)) {
                logger.error("Unable to generate PIN for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating PIN!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean ResetPIN()
    {
        logger.info("Resetting PIN.....");
        return false;
    }

    public static boolean AddSecurityQuestions(WalletCMSWsEntity wsmodel, CMSCustomer customer)
    {
        try {
            logger.info("Adding Security Questions...");

            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + CMSSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
            params = new HashMap<String, Object>();
            params.put("QUEST", wsmodel.getSecretquestion1());

            CMSSecurQuestions dbquestion = (CMSSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);
            CMSSecurQuestions question;

            if(dbquestion != null)
            {
                question = dbquestion;
            }
            else
            {
                question = new CMSSecurQuestions(wsmodel.getSecretquestion1(), wsmodel.getSecretquestion1());
            }

            CMSCustSecurQuestions customerquestion = new CMSCustSecurQuestions(1, question, wsmodel.getSecretquestionanswer1(), customer);

            GeneralDao.Instance.saveOrUpdate(question);
            GeneralDao.Instance.saveOrUpdate(customerquestion);

            dbQuery = "from " + CMSSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
            params = new HashMap<String, Object>();
            params.put("QUEST", wsmodel.getSecretquestion2());

            CMSSecurQuestions dbquestion2 = (CMSSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);
            CMSSecurQuestions question2;
            if(dbquestion2 != null)
            {
                question2 = dbquestion2;
            }
            else
            {
                question2 = new CMSSecurQuestions(wsmodel.getSecretquestion2(), wsmodel.getSecretquestion2());
            }


            CMSCustSecurQuestions customerquestion2 = new CMSCustSecurQuestions(2, question2, wsmodel.getSecretquestionanswer2(), customer);

            GeneralDao.Instance.saveOrUpdate(question2);
            GeneralDao.Instance.saveOrUpdate(customerquestion2);


            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception caught while adding security questions!");
            return false;
        }
    }

    public static boolean UpdateSecurityQuestions(WalletCMSWsEntity wsmodel, CMSCustomer customer)
    {
        try {
            logger.info("Updating Security Questions...");

            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + CMSSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
            params = new HashMap<String, Object>();
            params.put("QUEST", wsmodel.getSecretquestion1());

            CMSSecurQuestions dbquestion = (CMSSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);
            CMSSecurQuestions question;

            if(dbquestion != null)
            {
                question = dbquestion;
            }
            else
            {
                question = new CMSSecurQuestions(wsmodel.getSecretquestion1(), wsmodel.getSecretquestion1());
                GeneralDao.Instance.saveOrUpdate(question);
            }

            dbQuery = "from " + CMSSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
            params = new HashMap<String, Object>();
            params.put("QUEST", wsmodel.getSecretquestion2());

            CMSSecurQuestions dbquestion2 = (CMSSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);
            CMSSecurQuestions question2;
            if(dbquestion2 != null)
            {
                question2 = dbquestion2;
            }
            else
            {
                question2 = new CMSSecurQuestions(wsmodel.getSecretquestion2(), wsmodel.getSecretquestion2());
                GeneralDao.Instance.saveOrUpdate(question2);
            }




            dbQuery = "from " + CMSCustSecurQuestions.class.getName() + " c where c.customer= :CUST  ";
            params = new HashMap<String, Object>();
            params.put("CUST", customer);

            List<CMSCustSecurQuestions> customerquestionlist = GeneralDao.Instance.find(dbQuery,params);

            if(customerquestionlist != null && customerquestionlist.size() > 0)
            {
                    for(CMSCustSecurQuestions custquest : customerquestionlist)
                    {
                        if(custquest.getQuestionnumber() == 1)
                        {
                            custquest.setQuestion(question);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer1());
                        }
                        else //it will always be 2 --  as make sure by Field validation
                        {
                            custquest.setQuestion(question2);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer2());
                        }
                        GeneralDao.Instance.saveOrUpdate(custquest);
                    }
            }
            else
            {
                //logger.error("No Secret Questions found for Customer [" + WebServiceUtil.getMaskedValue(customer.getCustomerId()) + "] , rejecting...");
                logger.error("No Secret Questions found for Customer [" + customer.getCustomerId() + "] , rejecting...");
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception caught while updating security questions!");
            return false;
        }
    }

    public static boolean GetSecurityQuestions(WalletCMSWsEntity wsmodel, CMSCustomer customer)
    {
        try {
            logger.info("Getting Security Questions...");

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + CMSCustSecurQuestions.class.getName() + " c where c.customer= :CUST  ";
            params = new HashMap<String, Object>();
            params.put("CUST", customer);

            List<CMSCustSecurQuestions> customerquestionlist = GeneralDao.Instance.find(dbQuery,params);

            if(customerquestionlist != null && customerquestionlist.size() > 0)
            {
                for(CMSCustSecurQuestions custquest : customerquestionlist)
                {
                    if(custquest.getQuestionnumber() == 1)
                    {
                        wsmodel.setSecretquestion1(custquest.getQuestion().getQuestion());
                        //wsmodel.setSecretquestionanswer1(custquest.getAnswer());
                    }
                    else //it will always be 2 --  as make sure by Field validation
                    {
                        wsmodel.setSecretquestion2(custquest.getQuestion().getQuestion());
                        //wsmodel.setSecretquestionanswer2(custquest.getAnswer());
                    }
                }
            }
            else
            {
                logger.error("No Secret Questions found for Customer [" + customer.getCustomerId() + "] , rejecting...");
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception caught while getting security questions!");
            return false;
        }
    }

    public static CMSAccount getNayaPayPoolAccount()
    {
        String dbQuery;
        Map<String, Object> params;
        dbQuery = "from " + CMSAccount.class.getName() + " c where c.AccountNumber= :ACCNUM ";
        params = new HashMap<String, Object>();
        params.put("ACCNUM", "9999999999999999"); //TODO Raza UPDATE ME PLEASE MAN PLEASE

        return (CMSAccount)GeneralDao.Instance.findObject(dbQuery,params);
    }

    //m.rehman: verify and update wallet/account limit
    public static boolean VerifyWalletLimit(WalletCMSWsEntity wsmodel, CMSAccount cmsAccount, CMSCard cmsCard, boolean isDebit)
    {
        logger.info("Checking Limit ...");

        try {

            String relation, query;
            Map<String, Object> params;
            List<CMSProductDebitLimit> cmsDebitLimitList = null;
            List<CMSProductCreditLimit> cmsCreditLimitList = null;

            SwitchTransactionCodes tranCode = GlobalContext.getInstance().getTransactionCodeDescbyCode(wsmodel.getServicename());
            CMSProduct cmsProduct = cmsAccount.getProduct();

            if (isDebit) {
                params = new HashMap<String, Object>();
                query = "from " + CMSProductDebitLimit.class.getName() + " c " +
                        "where " +
                        "c.channelId = :CHANNEL_ID " +
                        "and c.productId = :PRODUCT_ID " +
                        "and c.transactionType = :TRAN_TYPE " +
                        "and c.isActive = '1' ";

                params.put("CHANNEL_ID", wsmodel.getChannelid());
                params.put("PRODUCT_ID", cmsProduct);
                params.put("TRAN_TYPE", tranCode);
                cmsDebitLimitList = GeneralDao.Instance.find(query, params);

                if (cmsDebitLimitList.size() > 0) {
                    for (CMSProductDebitLimit cmsLimit : cmsDebitLimitList) {

                        if (tranCode.getIsfinancial()) {
                            logger.info("Transaction is a Financial Type, processing limit ...");
                            //check if limit is shared
                            //if limit has parent id, means it is shared and uses parent limit info
                            if (cmsLimit.getParentLimitId() != null) {
                                query = "from " + CMSProductDebitLimit.class.getName() + " c " +
                                        "where " +
                                        "c.Id = :PARENT_LIMIT_ID " +
                                        "and c.isActive = '1' ";
                                params = new HashMap<String, Object>();
                                params.put("PARENT_LIMIT_ID", cmsLimit.getParentLimitId().getId());
                                cmsLimit = (CMSProductDebitLimit) GeneralDao.Instance.findObject(query, params);

                                if (cmsLimit == null) {
                                    logger.error("Product Limit not found for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                                    return false;
                                }
                            }

                            //setting relation according to limit type
                            if (Util.hasText(cmsLimit.getLimitType()) && cmsLimit.getLimitType().equals(CMSLimitType.CARD_LIMIT)) {
                                if (cmsCard != null) {
                                    relation = cmsCard.getCardNumber();
                                } else {
                                    logger.error("Card object is null for Card Limit, rejecting transaction ...");
                                    return false;
                                }
                            } else {
                                relation = cmsAccount.getAccountNumber();
                            }

                            params = new HashMap<String, Object>();
                            query = "from " + CMSDebitRemainingLimit.class.getName() + " c  " +
                                    "where " +
                                    "c.limitId = :LIMIT_ID " +
                                    "and c.relation = :RELATION ";
                            params.put("LIMIT_ID", cmsLimit);
                            params.put("RELATION", relation);
                            List<CMSDebitRemainingLimit> cmsAccountLimitList = GeneralDao.Instance.find(query, params);

                            if (cmsAccountLimitList.size() > 0) {
                                for (CMSDebitRemainingLimit cmsAccountLimit : cmsAccountLimitList) {
                                    Long remainingLimit = Long.parseLong(cmsAccountLimit.getRemainingAmount());
                                    Integer remainingFreq = Integer.parseInt(cmsAccountLimit.getRemainingFrequency());
                                    String cycleEndDate = cmsAccountLimit.getCycleEndDate();
                                    String currentDate = DateTime.now().getDayDate().getDate().toString();

                                    //m.rehman: 25-02-2021, Euronet integration, amount must deduct in local currency
                                    //Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                    Long amountTran = 0L;
                                    if (Util.hasText(wsmodel.getCbillamount())) {
                                        logger.info("Using Converted amount in Limit ...");
                                        amountTran = Long.parseLong(wsmodel.getCbillamount());
                                    } else {
                                        logger.info("Using Transaction amount in Limit ...");
                                        amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                    }
                                    ///////////////////////////////////////////////////////////////////////////////////////

                                    //m.rehman: 22-10-2020, VC-NAP-202009231 - Transaction limits issues and updates
                                    /*
                                    Long sourceCharge = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
                                    if (sourceCharge > 0) {
                                        amountTran = amountTran + sourceCharge;
                                    }
                                    Long fee = (Util.hasText(wsmodel.getAmttranfee())) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L;
                                    if (fee > 0) {
                                        amountTran = amountTran + fee;
                                    }
                                    */

                                    //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                                    logger.info("Limit ID [" + cmsAccountLimit.getLimitId().getId() + "], Remaining Amount [" + remainingLimit + "], Remaining Frequency [" + remainingFreq + "]");

                                    CMSLimitCycleType cmsLimitCycleType = null;
                                    cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(cmsLimit.getCycleLengthType()));

                                    if (cmsLimitCycleType.equals(CMSLimitCycleType.DAILY)) {

                                        if (!Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate)) {

                                            //m.rehman: 25-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6)
                                            //adding individual limit check ...
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                logger.info("Daily Individual limit found, fetching individual limit ...");
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());

                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Daily Transaction Amount Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Daily Transaction Frequency Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Daily Transaction Amount is greater than Remaining Daily Limit, rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Daily Limit verified successfully for Account/Wallet [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {

                                            //m.rehman: 25-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6)
                                            //adding individual limit check ...
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                logger.info("Monthly Individual limit found, fetching individual limit ...");
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());

                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Monthly Transaction Amount Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Monthly Transaction Frequency Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Monthly Limit for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Monthly Limit Verified successfully for Account/Wallet [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {

                                            //m.rehman: 25-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6)
                                            //adding individual limit check ...
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                logger.info("Yearly Individual limit found, fetching individual limit ...");
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());

                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Yearly Transaction Amount Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "]...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Yearly Transaction Frequency Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Yearly Limit for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Yearly Limit verified successfully for Account/Wallet [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else {
                                        logger.error("Invalid Limit Cycle Type for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                        return false;
                                    }
                                }
                            } else {
                                logger.error("Account/Wallet Limit not found for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                return false;
                            }
                        } else {
                            logger.info("Transaction is a non-financial type, skipping limit, moving forward ...");
                            return true;
                        }
                    }
                }
            }
            else
            {
                params = new HashMap<String, Object>();
                query = "from " + CMSProductCreditLimit.class.getName() + " c " +
                        "where " +
                        "c.channelId = :CHANNEL_ID " +
                        "and c.productId = :PRODUCT_ID " +
                        "and c.transactionType = :TRAN_TYPE " +
                        "and c.isActive = '1' ";

                params.put("CHANNEL_ID", wsmodel.getChannelid());
                params.put("PRODUCT_ID", cmsProduct);
                params.put("TRAN_TYPE", tranCode);
                cmsCreditLimitList = GeneralDao.Instance.find(query, params);

                if (cmsCreditLimitList.size() > 0) {
                    for (CMSProductCreditLimit cmsLimit : cmsCreditLimitList) {

                        if (tranCode.getIsfinancial()) {
                            logger.info("Transaction is a Financial Type, processing limit ...");
                            //check if limit is shared
                            //if limit has parent id, means it is shared and uses parent limit info
                            if (cmsLimit.getParentLimitId() != null) {
                                query = "from " + CMSProductCreditLimit.class.getName() + " c " +
                                        "where " +
                                        "c.Id = :PARENT_LIMIT_ID " +
                                        "and c.isActive = '1' ";
                                params = new HashMap<String, Object>();
                                params.put("PARENT_LIMIT_ID", cmsLimit.getParentLimitId().getId());
                                cmsLimit = (CMSProductCreditLimit) GeneralDao.Instance.findObject(query, params);

                                if (cmsLimit == null) {
                                    logger.error("Product Limit not found for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                                    return false;
                                }
                            }

                            //setting relation according to limit type
                            if (Util.hasText(cmsLimit.getLimitType()) && cmsLimit.getLimitType().equals(CMSLimitType.CARD_LIMIT)) {
                                if (cmsCard != null) {
                                    relation = cmsCard.getCardNumber();
                                } else {
                                    logger.error("Card object is null for Card Limit, rejecting transaction ...");
                                    return false;
                                }
                            } else {
                                relation = cmsAccount.getAccountNumber();
                            }

                            params = new HashMap<String, Object>();
                            query = "from " + CMSCreditRemainingLimit.class.getName() + " c  " +
                                    "where " +
                                    "c.limitId = :LIMIT_ID " +
                                    "and c.relation = :RELATION ";
                            params.put("LIMIT_ID", cmsLimit);
                            params.put("RELATION", relation);
                            List<CMSCreditRemainingLimit> cmsAccountLimitList = GeneralDao.Instance.find(query, params);

                            if (cmsAccountLimitList.size() > 0) {
                                for (CMSCreditRemainingLimit cmsAccountLimit : cmsAccountLimitList) {
                                    Long remainingLimit = Long.parseLong(cmsAccountLimit.getRemainingAmount());
                                    Integer remainingFreq = Integer.parseInt(cmsAccountLimit.getRemainingFrequency());
                                    String cycleEndDate = cmsAccountLimit.getCycleEndDate();
                                    String currentDate = DateTime.now().getDayDate().getDate().toString();

                                    //m.rehman: 25-02-2021, Euronet integration, amount must deduct in local currency
                                    //Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                    Long amountTran = 0L;
                                    if (Util.hasText(wsmodel.getCbillamount())) {
                                        logger.info("Using Converted amount in Limit ...");
                                        amountTran = Long.parseLong(wsmodel.getCbillamount());
                                    } else {
                                        logger.info("Using Transaction amount in Limit ...");
                                        amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                    }
                                    ///////////////////////////////////////////////////////////////////////////////////////

                                    //m.rehman: 22-10-2020, VC-NAP-202009231 - Transaction limits issues and updates
                                    /*
                                    Long sourceCharge = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
                                    if (sourceCharge > 0) {
                                        amountTran = amountTran + sourceCharge;
                                    }
                                    Long fee = (Util.hasText(wsmodel.getAmttranfee())) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L;
                                    if (fee > 0) {
                                        amountTran = amountTran + fee;
                                    }
                                    */

                                    //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                                    logger.info("Limit ID [" + cmsAccountLimit.getLimitId().getId() + "], Remaining Amount [" + remainingLimit + "], Remaining Frequency [" + remainingFreq + "]");

                                    CMSLimitCycleType cmsLimitCycleType = null;
                                    cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(cmsLimit.getCycleLengthType()));

                                    if (cmsLimitCycleType.equals(CMSLimitCycleType.DAILY)) {

                                        if (!Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate)) {

                                            //m.rehman: 25-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6)
                                            //adding individual limit check ...
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                logger.info("Daily Individual limit found, fetching individual limit ...");
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());

                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Daily Transaction Amount Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Daily Transaction Frequency Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Daily Transaction Amount is greater than Remaining Daily Limit, rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Daily Limit verified successfully for Account/Wallet [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {

                                            //m.rehman: 25-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6)
                                            //adding individual limit check ...
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                logger.info("Monthly Individual limit found, fetching individual limit ...");
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());

                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Monthly Transaction Amount Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Monthly Transaction Frequency Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Monthly Limit for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Monthly Limit Verified successfully for Account/Wallet [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {

                                            //m.rehman: 25-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6)
                                            //adding individual limit check ...
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                logger.info("Yearly Individual limit found, fetching individual limit ...");
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());

                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Yearly Transaction Amount Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "]...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Yearly Transaction Frequency Limit Exceeded for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Yearly Limit for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Yearly Limit verified successfully for Account/Wallet [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else {
                                        logger.error("Invalid Limit Cycle Type for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                        return false;
                                    }
                                }
                            } else {
                                logger.error("Account/Wallet Limit not found for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                return false;
                            }
                        } else {
                            logger.info("Transaction is a non-financial type, skipping limit, moving forward ...");
                            return true;
                        }
                    }
                }
            }

            if ((cmsDebitLimitList == null || (cmsDebitLimitList != null && cmsDebitLimitList.size() <=0))
                    && (cmsCreditLimitList == null || (cmsCreditLimitList != null && cmsCreditLimitList.size() <= 0))){
                logger.error("Product Limit not found for Account/Wallet [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                return false;
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Unable to process Account/Wallet/Card/Card Limit, rejecting transaction ...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return false;
        }

        /*
        try {

            CMSProduct cmsProduct = cmsAccount.getProduct();
            Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());

            Map<String, Object> params = new HashMap<String, Object>();
            String query = "from " + CMSLimit.class.getName() + " c " +
                    "where " +
                    "c.channelId = :CHANNEL_ID " +
                    "and c.productId = :PRODUCT_ID " +
                    "and c.limitType = :LIMIT_TYPE ";
            params.put("CHANNEL_ID", wsmodel.getChannelid());
            params.put("PRODUCT_ID", cmsProduct.getProductId());
            params.put("LIMIT_TYPE", CMSLimitType.ACCOUNT_LIMIT);

            String tranCode = GlobalContext.getInstance().getTranCodeByAPI(wsmodel.getServicename());
            query += "and c.transactionType = :TRAN_TYPE ";
            params.put("TRAN_TYPE", tranCode);

            if (!tranCode.equals(ISOTransactionCodes.TRANSFER) && !tranCode.equals(ISOTransactionCodes.IBFT)) {
                query += "and c.isShared = 1 ";
            }

            query += "and c.isActive = 1 ";
            query += "order by c.cycleLengthType asc";

            List<CMSLimit> cmsLimitList = GeneralDao.Instance.find(query, params);

            if (cmsLimitList.size() > 0) {
                for (CMSLimit cmsLimit : cmsLimitList) {
                    params = new HashMap<String, Object>();
                    query = "from " + CMSAccountLimit.class.getName() + " c  " +
                            "where " +
                            "c.limitId = :LIMIT_ID " +
                            "and c.acctNumber = :ACCT_NO ";
                    params.put("LIMIT_ID", cmsLimit);
                    params.put("ACCT_NO", cmsAccount.getAccountNumber());
                    List<CMSAccountLimit> cmsAccountLimitList = GeneralDao.Instance.find(query, params);

                    if (cmsAccountLimitList.size() > 0) {
                        for (CMSAccountLimit cmsAccountLimit : cmsAccountLimitList) {
                            Long remainingLimit = Long.parseLong(cmsAccountLimit.getRemainingAmount());
                            Long remainingFreq = Long.parseLong(cmsAccountLimit.getRemainingFrequency());
                            String cycleEndDate = cmsAccountLimit.getCycleEndDate();
                            String currentDate = DateTime.now().getDayDate().getDate().toString();
                            //Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());

                            CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(cmsLimit.getCycleLengthType()));
                            if (cmsLimitCycleType.equals(CMSLimitCycleType.DAILY)) {

                                if (!Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate)) {
                                    remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                    remainingFreq = Long.parseLong(cmsLimit.getFrequencyLength());
                                }

                                if (remainingLimit <= 0) {
                                    logger.error("Daily Transaction Amount Limit Exceeded ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                if (remainingFreq <= 0) {
                                    logger.error("Daily Transaction Frequency Limit Exceeded, rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                if (amountTran > remainingLimit) {
                                    logger.error("Daily Transaction Amount is greater than Remaining Daily Limit, rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                logger.info("Daily Limit verified successfully!!!");

                            } else if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {

                                if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                    remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                    remainingFreq = Long.parseLong(cmsLimit.getFrequencyLength());
                                }

                                if (remainingLimit <= 0) {
                                    logger.error("Monthly Transaction Amount Limit Exceeded ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                if (remainingFreq <= 0) {
                                    logger.error("Monthly Transaction Frequency Limit Exceeded, rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                if (amountTran > remainingLimit) {
                                    logger.error("Amount transaction is greater than Remaining Monthly Limit, rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                logger.info("Monthly Limit verified successfully!!!");

                            } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {

                                if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                    remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                    remainingFreq = Long.parseLong(cmsLimit.getFrequencyLength());
                                }

                                if (remainingLimit <= 0) {
                                    logger.error("Yearly Transaction Amount Limit Exceeded ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                if (remainingFreq <= 0) {
                                    logger.error("Yearly Transaction Frequency Limit Exceeded, rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                if (amountTran > remainingLimit) {
                                    logger.error("Amount transaction is greater than Remaining Yearly Limit, rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                                    return false;
                                }

                                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                logger.info("Yearly Limit verified successfully!!!");

                            } else {
                                logger.error("Invalid Limit Cycle Type, rejecting transaction ...");
                                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                return false;
                            }
                        }
                    } else {
                        logger.error("Account/Wallet/Card Limit not found, rejecting transaction ...");
                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                        return false;
                    }
                }
            } else {
                logger.error("Product Limit not found, rejecting transaction ...");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                return false;
            }

        } catch (Exception e) {
            logger.error("Unable to process Account/Wallet/Card Limit, rejecting transaction ...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return false;
        }
        */

        return true;
    }

    public static boolean ProcessDebitWalletLimit(WalletCMSWsEntity wsmodel, CMSAccount cmsAccount, CMSCard cmsCard, Boolean isApplyLimit)
    {
        logger.info("Checking Debit Limit ...");

        List<CMSDebitRemainingLimit> globalCMSAccountLimitList = new ArrayList<CMSDebitRemainingLimit>();
        try {

            CMSProduct cmsProduct = cmsAccount.getProduct();

            Map<String, Object> params = new HashMap<String, Object>();
            SwitchTransactionCodes tranCode = GlobalContext.getInstance().getTransactionCodeDescbyCode(wsmodel.getServicename());
            String query = "from " + CMSProductDebitLimit.class.getName() + " c " +
                    "where " +
                    "c.channelId = :CHANNEL_ID " +
                    "and c.productId = :PRODUCT_ID " +
                    //"and c.limitType = :LIMIT_TYPE " +
                    "and c.transactionType = :TRAN_TYPE " +
                    "and c.isActive = '1' ";

            params.put("CHANNEL_ID", wsmodel.getChannelid());
            params.put("PRODUCT_ID", cmsProduct);
            //params.put("LIMIT_TYPE", CMSLimitType.ACCOUNT_LIMIT);
            params.put("TRAN_TYPE", tranCode);

            List<CMSProductDebitLimit> cmsLimitList = GeneralDao.Instance.find(query, params);
            String relation;

            if (cmsLimitList.size() > 0) {
                for (CMSProductDebitLimit cmsLimit : cmsLimitList) {

                    if (tranCode.getIsfinancial()) {
                        logger.info("Transaction is a Financial Type, processing limit ...");
                        //check if limit is shared
                        //if limit has parent id, means it is shared and uses parent limit info
                        if (cmsLimit.getParentLimitId() != null) {
                            query = "from " + CMSProductDebitLimit.class.getName() + " c " +
                                    "where " +
                                    "c.Id = :PARENT_LIMIT_ID " +
                                    "and c.isActive = '1' ";
                            params = new HashMap<String, Object>();
                            params.put("PARENT_LIMIT_ID", cmsLimit.getParentLimitId().getId());
                            cmsLimit = (CMSProductDebitLimit) GeneralDao.Instance.findObject(query, params);

                            if (cmsLimit == null) {
                                logger.error("Product Limit not found for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                                return false;
                            }
                        }

                        //setting relation according to limit type
						//m.rehman: 10-07-2021 - Online transaction failure on VISA physical on production
                        //if (Util.hasText(cmsLimit.getLimitType()) && cmsLimit.getLimitType().equals(CMSLimitType.CARD_LIMIT)) {
                        //    if (cmsCard != null) {
                        //        relation = cmsCard.getCardNumber();
                        //    } else {
                        //        logger.error("Card object is null for Card Limit, rejecting transaction ...");
                        //        return false;
                        //    }
                        //} else {
                            relation = cmsAccount.getAccountNumber();
                        //}

                        params = new HashMap<String, Object>();
                        query = "from " + CMSDebitRemainingLimit.class.getName() + " c  " +
                                "where " +
                                "c.limitId = :LIMIT_ID " +
                                "and c.relation = :RELATION ";
                        params.put("LIMIT_ID", cmsLimit);
                        params.put("RELATION", relation);
                        List<CMSDebitRemainingLimit> cmsAccountLimitList = GeneralDao.Instance.find(query, params);

                        if (cmsAccountLimitList.size() > 0) {
                            for (CMSDebitRemainingLimit cmsAccountLimit : cmsAccountLimitList) {
                                Long remainingLimit = Long.parseLong(cmsAccountLimit.getRemainingAmount());
                                Integer remainingFreq = Integer.parseInt(cmsAccountLimit.getRemainingFrequency());
                                String cycleStartDate = cmsAccountLimit.getCycleStartDate();
                                String cycleEndDate = cmsAccountLimit.getCycleEndDate();
                                String currentDate = DateTime.now().getDayDate().getDate().toString();

                                //m.rehman: 25-02-2021, Euronet integration, amount must deduct in local currency
                                //Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                Long amountTran = 0L;
                                if (Util.hasText(wsmodel.getCbillamount())) {
                                    logger.info("Using Converted amount in Limit ...");
                                    amountTran = Long.parseLong(wsmodel.getCbillamount());
                                } else {
                                    logger.info("Using Transaction amount in Limit ...");
                                    amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                }
                                ///////////////////////////////////////////////////////////////////////////////////////

                                //m.rehman: 22-10-2020, VC-NAP-202009231 - Transaction limits issues and updates
                                /*
                                Long sourceCharge = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
                                if (sourceCharge > 0) {
                                    amountTran = amountTran + sourceCharge;
                                }
                                Long fee = (Util.hasText(wsmodel.getAmttranfee())) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L;
                                if (fee > 0) {
                                    amountTran = amountTran + fee;
                                }
                                */

                                //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                                logger.info("Limit ID [" + cmsAccountLimit.getLimitId().getId() + "], Remaining Amount [" + remainingLimit + "], Remaining Frequency [" + remainingFreq + "]");

                                if (isApplyLimit.equals(Boolean.TRUE)) {

                                    CMSLimitCycleType cmsLimitCycleType = null;
                                    cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(cmsLimit.getCycleLengthType()));

                                    if (cmsLimitCycleType.equals(CMSLimitCycleType.DAILY)) {

                                        if (!Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate)) {
                                            cycleStartDate = currentDate;
                                            cycleEndDate = currentDate;

                                            //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                            //adding individual limit check
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                && cmsAccountLimit.getIndividualLimitId() != null) {
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());
                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Daily Transaction Amount Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Daily");
                                            //======================================================================================================

                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Daily Transaction Frequency Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Daily Transaction Amount is greater than Remaining Daily Limit, rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            // ====================================================================================

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setLimittype("Daily");
                                            //======================================================================================================

                                            return false;
                                        }

                                        logger.info("remainingLimit befr: "+remainingLimit);
                                        logger.info("amountTran: "+amountTran);

                                        remainingLimit = remainingLimit - amountTran;
                                        remainingFreq--;
                                        logger.info("remainingLimit after: "+remainingLimit);

                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                        cmsAccountLimit.setCycleEndDate(cycleEndDate);
    									//Added by Moiz for VP-NAP-202405061 => link CW (OffUs) channel Limit Authentication Change
                                        wsmodel.setRemainingLimit(remainingLimit.toString());

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Daily Limit processed successfully for Account [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                            cycleStartDate = currentDate;
                                            Calendar c = Calendar.getInstance();
                                            //c.setTime(new Date());
                                            //c.add(Calendar.DAY_OF_MONTH, 30);
                                            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                                            cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());

                                            //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                            //adding individual limit check
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());
                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Monthly Transaction Amount Limit Exceeded for Account [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Monthly");
                                            //======================================================================================================

                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Monthly Transaction Frequency Limit Exceeded for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Monthly Limit for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Monthly");
                                            //======================================================================================================

                                            return false;
                                        }
                                        logger.info("remainingLimit befr: "+remainingLimit);
                                        logger.info("amountTran: "+amountTran);

                                        remainingLimit = remainingLimit - amountTran;
                                        remainingFreq--;
                                        logger.info("remainingLimit after: "+remainingLimit);
                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                        cmsAccountLimit.setCycleEndDate(cycleEndDate);
    									//Added by Moiz for VP-NAP-202405061 => link CW (OffUs) channel Limit Authentication Change
                                        wsmodel.setRemainingLimit(remainingLimit.toString());

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Monthly Limit processed successfully for Account [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                            cycleStartDate = currentDate;
                                            Calendar c = Calendar.getInstance();
                                            //c.setTime(new Date());
                                            //c.add(Calendar.DAY_OF_MONTH, 365);
                                            c.set(Calendar.DAY_OF_YEAR, c.getActualMaximum(Calendar.DAY_OF_YEAR));
                                            cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());

                                            //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                            //adding individual limit check
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());
                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Yearly Transaction Amount Limit Exceeded for Account [" + cmsAccount.getAccountNumber() + "]...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Yearly");
                                            //======================================================================================================

                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Yearly Transaction Frequency Limit Exceeded for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Yearly Limit for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Yearly");
                                            //======================================================================================================

                                            return false;
                                        }

                                        logger.info("remainingLimit befr: "+remainingLimit);
                                        logger.info("amountTran: "+amountTran);
                                        remainingLimit = remainingLimit - amountTran;
                                        remainingFreq--;


                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                        cmsAccountLimit.setCycleEndDate(cycleEndDate);
    									//Added by Moiz for VP-NAP-202405061 => link CW (OffUs) channel Limit Authentication Change
                                        wsmodel.setRemainingLimit(remainingLimit.toString());

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Yearly Limit processed successfully for Account [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else {
                                        logger.error("Invalid Limit Cycle Type for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                        return false;
                                    }
                                } else {
                                    if (Util.hasText(cycleEndDate) && (Long.parseLong(cycleEndDate) <= Long.parseLong(currentDate))) {
                                        remainingLimit = remainingLimit + amountTran;
                                        remainingFreq++;
                                        logger.info("remainingLimit rev: "+remainingLimit);
                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
    									//Added by Moiz for VP-NAP-202405061 => link CW (OffUs) channel Limit Authentication Change
                                        wsmodel.setRemainingLimit(remainingLimit.toString());
                                        logger.info("Limit reversed successfully!!!");
                                    }
                                }
                                //saving limits in db
                                //GeneralDao.Instance.saveOrUpdate(cmsAccountLimit);
                                globalCMSAccountLimitList.add(cmsAccountLimit);
                            }
                            //GeneralDao.Instance.saveOrUpdate(cmsAccountLimitList);
                            //globalCMSAccountLimitList = cmsAccountLimitList;
                        } else {
                            logger.error("Account Limit not found for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                            return false;
                        }
                    } else {
                        logger.info("Transaction is a non-financial type, skipping limit, moving forward ...");
                        return true;
                    }
                }
            } else {
                logger.error("Product Limit not found for Account [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                //return false;		//m.rehman: 10-07-2021 - Online transaction failure on VISA physical on production
            }

            //saving limits in db
            if (globalCMSAccountLimitList != null && globalCMSAccountLimitList.size() > 0) {
                for (CMSDebitRemainingLimit cmsAccountLimit : globalCMSAccountLimitList) {
                    GeneralDao.Instance.saveOrUpdate(cmsAccountLimit);
                }
            }

            //m.rehman: 18-04-2021, Euronet Integration
            if (cmsCard != null) {
                cmsProduct = cmsCard.getProduct();

                params = new HashMap<String, Object>();
                tranCode = GlobalContext.getInstance().getTransactionCodeDescbyCode(wsmodel.getServicename());
                query = "from " + CMSProductDebitLimit.class.getName() + " c " +
                        "where " +
                        "c.channelId = :CHANNEL_ID " +
                        "and c.productId = :PRODUCT_ID " +
						//m.rehman: 10-07-2021 - Online transaction failure on VISA physical on production
                        //"and c.limitType = :LIMIT_TYPE " +
                        "and c.transactionType = :TRAN_TYPE " +
                        "and c.isActive = '1' ";

                params.put("CHANNEL_ID", wsmodel.getChannelid());
                params.put("PRODUCT_ID", cmsProduct);
				//m.rehman: 10-07-2021 - Online transaction failure on VISA physical on production
                //params.put("LIMIT_TYPE", CMSLimitType.CARD_LIMIT);
                params.put("TRAN_TYPE", tranCode);

                cmsLimitList = GeneralDao.Instance.find(query, params);

                if (cmsLimitList.size() > 0) {
                    for (CMSProductDebitLimit cmsLimit : cmsLimitList) {

                        if (tranCode.getIsfinancial()) {
                            logger.info("Transaction is a Financial Type, processing limit ...");
                            //check if limit is shared
                            //if limit has parent id, means it is shared and uses parent limit info
                            if (cmsLimit.getParentLimitId() != null) {
                                query = "from " + CMSProductDebitLimit.class.getName() + " c " +
                                        "where " +
                                        "c.Id = :PARENT_LIMIT_ID " +
                                        "and c.isActive = '1' ";
                                params = new HashMap<String, Object>();
                                params.put("PARENT_LIMIT_ID", cmsLimit.getParentLimitId().getId());
                                cmsLimit = (CMSProductDebitLimit) GeneralDao.Instance.findObject(query, params);

                                if (cmsLimit == null) {
                                    logger.error("Product Limit not found for Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                    //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                    ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);
                                    wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                                    return false;
                                }
                            }

                            //setting relation according to limit type
							//m.rehman: 10-07-2021 - Online transaction failure on VISA physical on production
                            //if (Util.hasText(cmsLimit.getLimitType()) && cmsLimit.getLimitType().equals(CMSLimitType.CARD_LIMIT)) {
                            //    if (cmsCard != null) {
                                    relation = cmsCard.getCardNumber();
                            //    } else {
                            //        logger.error("Card object is null for Card Limit, rejecting transaction ...");
                            //        return false;
                            //    }
                            //}
                            //else {
                            //    relation = cmsAccount.getAccountNumber();
                            //}

                            params = new HashMap<String, Object>();
                            query = "from " + CMSDebitRemainingLimit.class.getName() + " c  " +
                                    "where " +
                                    "c.limitId = :LIMIT_ID " +
                                    "and c.relation = :RELATION ";
                            params.put("LIMIT_ID", cmsLimit);
                            params.put("RELATION", relation);
                            List<CMSDebitRemainingLimit> cmsCardLimitList = GeneralDao.Instance.find(query, params);

                            if (cmsCardLimitList.size() > 0) {
                                for (CMSDebitRemainingLimit cmsCardLimit : cmsCardLimitList) {
                                    Long remainingLimit = Long.parseLong(cmsCardLimit.getRemainingAmount());
                                    Integer remainingFreq = Integer.parseInt(cmsCardLimit.getRemainingFrequency());
                                    String cycleStartDate = cmsCardLimit.getCycleStartDate();
                                    String cycleEndDate = cmsCardLimit.getCycleEndDate();
                                    String currentDate = DateTime.now().getDayDate().getDate().toString();

                                    //m.rehman: 25-02-2021, Euronet integration, amount must deduct in local currency
                                    //Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                    Long amountTran = 0L;
                                    if (Util.hasText(wsmodel.getCbillamount())) {
                                        logger.info("Using Converted amount in Limit ...");
                                        amountTran = Long.parseLong(wsmodel.getCbillamount());
                                    } else {
                                        logger.info("Using Transaction amount in Limit ...");
                                        amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                    }
                                    ///////////////////////////////////////////////////////////////////////////////////////

                                    //m.rehman: 22-10-2020, VC-NAP-202009231 - Transaction limits issues and updates
                                    /*
                                    Long sourceCharge = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
                                    if (sourceCharge > 0) {
                                        amountTran = amountTran + sourceCharge;
                                    }
                                    Long fee = (Util.hasText(wsmodel.getAmttranfee())) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L;
                                    if (fee > 0) {
                                        amountTran = amountTran + fee;
                                    }
                                    */

                                    //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                                    logger.info("Limit ID [" + cmsCardLimit.getLimitId().getId() + "], Remaining Amount [" + remainingLimit + "], Remaining Frequency [" + remainingFreq + "]");

                                    if (isApplyLimit.equals(Boolean.TRUE)) {

                                        CMSLimitCycleType cmsLimitCycleType = null;
                                        cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(cmsLimit.getCycleLengthType()));

                                        if (cmsLimitCycleType.equals(CMSLimitCycleType.DAILY)) {

                                            if (!Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate)) {
                                                cycleStartDate = currentDate;
                                                cycleEndDate = currentDate;

                                                //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                                //adding individual limit check
                                                if (Util.hasText(cmsCardLimit.getIsIndividual()) && cmsCardLimit.getIsIndividual().equals("1")
                                                        && cmsCardLimit.getIndividualLimitId() != null) {
                                                    remainingLimit = Long.parseLong(cmsCardLimit.getIndividualLimitId().getAmount());
                                                    remainingFreq = Integer.parseInt(cmsCardLimit.getIndividualLimitId().getFrequencyLength());
                                                } else {
                                                    remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                    remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                                }
                                            }

                                            if (remainingLimit <= 0) {
                                                logger.error("Daily Transaction Amount Limit Exceeded for Card [" + cmsCard.getCardNumber() + "] ...");
                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);
                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                                //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                                wsmodel.setDailylimit(remainingLimit.toString());
                                                wsmodel.setLimittype("Daily");
                                                //======================================================================================================

                                                return false;
                                            }

                                            if (remainingFreq <= 0) {
                                                logger.error("Daily Transaction Frequency Limit Exceeded for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");
                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);
                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                                return false;
                                            }

                                            if (amountTran > remainingLimit) {
                                                logger.error("Daily Transaction Amount is greater than Remaining Daily Limit, rejecting transaction ...");

                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);

                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                                                wsmodel.setDailylimit(remainingLimit.toString());
                                                // ====================================================================================

                                                //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                                wsmodel.setLimittype("Daily");
                                                //======================================================================================================
                                                return false;
                                            }


                                            remainingLimit = remainingLimit - amountTran;
                                            remainingFreq--;

                                            cmsCardLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                            cmsCardLimit.setRemainingFrequency(remainingFreq.toString());
                                            cmsCardLimit.setCycleStartDate(cycleStartDate);
                                            cmsCardLimit.setCycleEndDate(cycleEndDate);

                                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                            logger.info("Daily Limit processed successfully for Card [" + cmsCard.getCardNumber() + "]!!!");

                                        } else if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {

                                            if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                                cycleStartDate = currentDate;
                                                Calendar c = Calendar.getInstance();
                                                //c.setTime(new Date());
                                                //c.add(Calendar.DAY_OF_MONTH, 30);
                                                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                                                cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());

                                                //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                                //adding individual limit check
                                                if (Util.hasText(cmsCardLimit.getIsIndividual()) && cmsCardLimit.getIsIndividual().equals("1")
                                                        && cmsCardLimit.getIndividualLimitId() != null) {
                                                    remainingLimit = Long.parseLong(cmsCardLimit.getIndividualLimitId().getAmount());
                                                    remainingFreq = Integer.parseInt(cmsCardLimit.getIndividualLimitId().getFrequencyLength());
                                                } else {
                                                    remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                    remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                                }
                                            }

                                            if (remainingLimit <= 0) {
                                                logger.error("Monthly Transaction Amount Limit Exceeded Card [" + cmsCard.getCardNumber() + "] ...");

                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);
                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                                //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                                wsmodel.setDailylimit(remainingLimit.toString());
                                                wsmodel.setLimittype("Monthly");
                                                //======================================================================================================

                                                return false;
                                            }

                                            if (remainingFreq <= 0) {
                                                logger.error("Monthly Transaction Frequency Limit Exceeded for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");

                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);
                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                                return false;
                                            }

                                            if (amountTran > remainingLimit) {
                                                logger.error("Amount transaction is greater than Remaining Monthly Limit for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");

                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);

                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                                //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                                wsmodel.setDailylimit(remainingLimit.toString());
                                                wsmodel.setLimittype("Monthly");
                                                //======================================================================================================

                                                return false;
                                            }

                                            remainingLimit = remainingLimit - amountTran;
                                            remainingFreq--;

                                            cmsCardLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                            cmsCardLimit.setRemainingFrequency(remainingFreq.toString());
                                            cmsCardLimit.setCycleStartDate(cycleStartDate);
                                            cmsCardLimit.setCycleEndDate(cycleEndDate);

                                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                            logger.info("Monthly Limit processed successfully for Card [" + cmsCard.getCardNumber() + "]!!!");

                                        } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {

                                            if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                                cycleStartDate = currentDate;
                                                Calendar c = Calendar.getInstance();
                                                //c.setTime(new Date());
                                                //c.add(Calendar.DAY_OF_MONTH, 365);
                                                c.set(Calendar.DAY_OF_YEAR, c.getActualMaximum(Calendar.DAY_OF_YEAR));
                                                cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());

                                                //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                                //adding individual limit check
                                                if (Util.hasText(cmsCardLimit.getIsIndividual()) && cmsCardLimit.getIsIndividual().equals("1")
                                                        && cmsCardLimit.getIndividualLimitId() != null) {
                                                    remainingLimit = Long.parseLong(cmsCardLimit.getIndividualLimitId().getAmount());
                                                    remainingFreq = Integer.parseInt(cmsCardLimit.getIndividualLimitId().getFrequencyLength());
                                                } else {
                                                    remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                    remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                                }
                                            }

                                            if (remainingLimit <= 0) {
                                                logger.error("Yearly Transaction Amount Limit Exceeded for Card [" + cmsCard.getCardNumber() + "]...");

                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);

                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                                //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                                wsmodel.setDailylimit(remainingLimit.toString());
                                                wsmodel.setLimittype("Yearly");
                                                //======================================================================================================

                                                return false;
                                            }

                                            if (remainingFreq <= 0) {
                                                logger.error("Yearly Transaction Frequency Limit Exceeded for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");

                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);

                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                                return false;
                                            }

                                            if (amountTran > remainingLimit) {
                                                logger.error("Amount transaction is greater than Remaining Yearly Limit for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");

                                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);

                                                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                                //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                                wsmodel.setDailylimit(remainingLimit.toString());
                                                wsmodel.setLimittype("Yearly");
                                                //======================================================================================================

                                                return false;
                                            }

                                            remainingLimit = remainingLimit - amountTran;
                                            remainingFreq--;

                                            cmsCardLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                            cmsCardLimit.setRemainingFrequency(remainingFreq.toString());
                                            cmsCardLimit.setCycleStartDate(cycleStartDate);
                                            cmsCardLimit.setCycleEndDate(cycleEndDate);

                                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                            logger.info("Yearly Limit processed successfully for Card [" + cmsCard.getCardNumber() + "]!!!");

                                        } else {
                                            logger.error("Invalid Limit Cycle Type for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");

                                            //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                            ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);

                                            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                            return false;
                                        }
                                    } else {
                                        if (Util.hasText(cycleEndDate) && (Long.parseLong(cycleEndDate) <= Long.parseLong(currentDate))) {
                                            remainingLimit = remainingLimit + amountTran;
                                            remainingFreq++;
                                            cmsCardLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                            cmsCardLimit.setRemainingFrequency(remainingFreq.toString());
                                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                            logger.info("Limit reversed successfully!!!");
                                        }
                                    }
                                    //saving limits in db
                                    //GeneralDao.Instance.saveOrUpdate(cmsAccountLimit);
                                    globalCMSAccountLimitList.add(cmsCardLimit);
                                }
                                //GeneralDao.Instance.saveOrUpdate(cmsCardLimitList);
                                //globalCMSAccountLimitList = cmsCardLimitList;
                            } else {
                                logger.error("Card Limit not found for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");

                                //m.rehman: 30-12-2021, VC-NAP-202112301 - Limits on Cards Issue - Production
                                ProcessDebitWalletLimit(wsmodel, cmsAccount, null, false);

                                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                return false;
                            }
                        } else {
                            logger.info("Transaction is a non-financial type, skipping limit, moving forward ...");
                            return true;
                        }
                    }
                } else {
                    logger.error("Product Limit not found for Card [" + cmsCard.getCardNumber() + "], rejecting transaction ...");
                    wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
//                    return false;		//m.rehman: 10-07-2021 - Online transaction failure on VISA physical on production
                }

                //saving limits in db
                if (globalCMSAccountLimitList != null && globalCMSAccountLimitList.size() > 0) {
                    for (CMSDebitRemainingLimit cmsAccountLimit : globalCMSAccountLimitList) {
                        GeneralDao.Instance.saveOrUpdate(cmsAccountLimit);
                    }
                }
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//m.rehman: 10-07-2021 - Online transaction failure on VISA physical on production
            if (globalCMSAccountLimitList == null || globalCMSAccountLimitList.size() <= 0) {
                logger.error("No limit found for transaction [" + wsmodel.getServicename() + "], rejecting ...");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to process Account/Wallet/Card Limit, rejecting transaction ...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            if (globalCMSAccountLimitList != null && globalCMSAccountLimitList.size() > 0) {
                for (CMSDebitRemainingLimit cmsAccountLimit : globalCMSAccountLimitList) {
                    GeneralDao.Instance.evict(cmsAccountLimit);
                }
            }
            return false;
        }
        return true;
    }

    public static boolean ProcessCreditWalletLimit(WalletCMSWsEntity wsmodel, CMSAccount cmsAccount, CMSCard cmsCard, Boolean isApplyLimit)
    {
        logger.info("Checking Credit Limit ...");

        List<CMSCreditRemainingLimit> globalCMSAccountLimitList = new ArrayList<CMSCreditRemainingLimit>();
        try {

            CMSProduct cmsProduct = cmsAccount.getProduct();

            Map<String, Object> params = new HashMap<String, Object>();
            SwitchTransactionCodes tranCode = GlobalContext.getInstance().getTransactionCodeDescbyCode(wsmodel.getServicename());
            String query = "from " + CMSProductCreditLimit.class.getName() + " c " +
                    "where " +
                    "c.channelId = :CHANNEL_ID " +
                    "and c.productId = :PRODUCT_ID " +
                    //"and c.limitType = :LIMIT_TYPE " +
                    "and c.transactionType = :TRAN_TYPE " +
                    "and c.isActive = '1'";

            params.put("CHANNEL_ID", wsmodel.getChannelid());
            params.put("PRODUCT_ID", cmsProduct);
            //params.put("LIMIT_TYPE", CMSLimitType.ACCOUNT_LIMIT);
            params.put("TRAN_TYPE", tranCode);

            List<CMSProductCreditLimit> cmsLimitList = GeneralDao.Instance.find(query, params);
            String relation;

            if (cmsLimitList.size() > 0) {
                for (CMSProductCreditLimit cmsLimit : cmsLimitList) {

                    if (tranCode.getIsfinancial()) {
                        logger.info("Transaction is a Financial Type, processing limit ...");
                        //check if limit is shared
                        //if limit has parent id, means it is shared and uses parent limit info
                        params = new HashMap<String, Object>();
                        if (cmsLimit.getParentLimitId() != null) {
                            query = "from " + CMSProductCreditLimit.class.getName() + " c " +
                                    "where " +
                                    "c.Id = :PARENT_LIMIT_ID " +
                                    "and c.isActive = '1' ";
                            params.put("PARENT_LIMIT_ID", cmsLimit.getParentLimitId().getId());
                            cmsLimit = (CMSProductCreditLimit) GeneralDao.Instance.findObject(query, params);

                            if (cmsLimit == null) {
                                logger.error("Product Limit not found for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                                return false;
                            }
                        }

                        //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                        //only account limit check here
                        //setting relation according to limit type
                        //if (Util.hasText(cmsLimit.getLimitType()) && cmsLimit.getLimitType().equals(CMSLimitType.CARD_LIMIT)) {
                        //    if (cmsCard != null) {
                        //        relation = cmsCard.getCardNumber();
                        //    } else {
                        //        logger.error("Card object is null for Card Limit, rejecting transaction ...");
                        //        return false;
                        //    }
                        //} else {
                            relation = cmsAccount.getAccountNumber();
                        //}

                        params = new HashMap<String, Object>();
                        query = "from " + CMSCreditRemainingLimit.class.getName() + " c  " +
                                "where " +
                                "c.limitId = :LIMIT_ID " +
                                "and c.relation = :RELATION ";
                        params.put("LIMIT_ID", cmsLimit);
                        params.put("RELATION", relation);
                        List<CMSCreditRemainingLimit> cmsAccountLimitList = GeneralDao.Instance.find(query, params);

                        if (cmsAccountLimitList.size() > 0) {
                            for (CMSCreditRemainingLimit cmsAccountLimit : cmsAccountLimitList) {
                                Long remainingLimit = Long.parseLong(cmsAccountLimit.getRemainingAmount());
                                Integer remainingFreq = Integer.parseInt(cmsAccountLimit.getRemainingFrequency());
                                String cycleStartDate = cmsAccountLimit.getCycleStartDate();
                                String cycleEndDate = cmsAccountLimit.getCycleEndDate();
                                String currentDate = DateTime.now().getDayDate().getDate().toString();

                                //m.rehman: 25-02-2021, Euronet integration, amount must deduct in local currency
                                //Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                Long amountTran = 0L;
                                if (Util.hasText(wsmodel.getCbillamount())) {
                                    logger.info("Using Converted amount in Limit ...");
                                    amountTran = Long.parseLong(wsmodel.getCbillamount());
                                } else {
                                    logger.info("Using Transaction amount in Limit ...");
                                    amountTran = Long.parseLong(wsmodel.getAmounttransaction());
                                }
                                ///////////////////////////////////////////////////////////////////////////////////////

                                //m.rehman: 22-10-2020, VC-NAP-202009231 - Transaction limits issues and updates
                                /*
                                Long sourceCharge = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
                                if (sourceCharge > 0) {
                                    amountTran = amountTran + sourceCharge;
                                }
                                Long fee = (Util.hasText(wsmodel.getAmttranfee())) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L;
                                if (fee > 0) {
                                    amountTran = amountTran + fee;
                                }
                                */

                                //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                                logger.info("Limit ID [" + cmsAccountLimit.getLimitId().getId() + "], Remaining Amount [" + remainingLimit + "], Remaining Frequency [" + remainingFreq + "]");

                                if (isApplyLimit.equals(Boolean.TRUE)) {

                                    CMSLimitCycleType cmsLimitCycleType = null;
                                    cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(cmsLimit.getCycleLengthType()));

                                    if (cmsLimitCycleType.equals(CMSLimitCycleType.DAILY)) {

                                        if (!Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate)) {
                                            cycleStartDate = currentDate;
                                            cycleEndDate = currentDate;

                                            //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                            //adding individual limit check
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());
                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Daily Transaction Amount Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Daily");
                                            //======================================================================================================

                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Daily Transaction Frequency Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Daily Transaction Amount is greater than Remaining Daily Limit, rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Daily");
                                            //======================================================================================================

                                            return false;
                                        }

                                        remainingLimit = remainingLimit - amountTran;
                                        remainingFreq--;

                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                        cmsAccountLimit.setCycleEndDate(cycleEndDate);

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Daily Limit processed successfully for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                            cycleStartDate = currentDate;
                                            Calendar c = Calendar.getInstance();
                                            //c.setTime(new Date());
                                            //c.add(Calendar.DAY_OF_MONTH, 30);
                                            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                                            cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());

                                            //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                            //adding individual limit check
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());
                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Monthly Transaction Amount Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "] ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Monthly");
                                            //======================================================================================================

                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Monthly Transaction Frequency Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Monthly Limit for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Monthly");
                                            //======================================================================================================

                                            return false;
                                        }

                                        remainingLimit = remainingLimit - amountTran;
                                        remainingFreq--;

                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                        cmsAccountLimit.setCycleEndDate(cycleEndDate);

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Monthly Limit processed successfully for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {

                                        if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                            cycleStartDate = currentDate;
                                            Calendar c = Calendar.getInstance();
                                            //c.setTime(new Date());
                                            //c.add(Calendar.DAY_OF_MONTH, 365);
                                            c.set(Calendar.DAY_OF_YEAR, c.getActualMaximum(Calendar.DAY_OF_YEAR));
                                            cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());

                                            //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                                            //adding individual limit check
                                            if (Util.hasText(cmsAccountLimit.getIsIndividual()) && cmsAccountLimit.getIsIndividual().equals("1")
                                                    && cmsAccountLimit.getIndividualLimitId() != null) {
                                                remainingLimit = Long.parseLong(cmsAccountLimit.getIndividualLimitId().getAmount());
                                                remainingFreq = Integer.parseInt(cmsAccountLimit.getIndividualLimitId().getFrequencyLength());
                                            } else {
                                                remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                                remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                            }
                                        }

                                        if (remainingLimit <= 0) {
                                            logger.error("Yearly Transaction Amount Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "]...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Yearly");
                                            //======================================================================================================

                                            return false;
                                        }

                                        if (remainingFreq <= 0) {
                                            logger.error("Yearly Transaction Frequency Limit Exceeded for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                            return false;
                                        }

                                        if (amountTran > remainingLimit) {
                                            logger.error("Amount transaction is greater than Remaining Yearly Limit for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);

                                            //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
                                            wsmodel.setDailylimit(remainingLimit.toString());
                                            wsmodel.setLimittype("Yearly");
                                            //======================================================================================================

                                            return false;
                                        }

                                        remainingLimit = remainingLimit - amountTran;
                                        remainingFreq--;

                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                        cmsAccountLimit.setCycleEndDate(cycleEndDate);

                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Yearly Limit processed successfully for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "]!!!");

                                    } else {
                                        logger.error("Invalid Limit Cycle Type for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                                        return false;
                                    }
                                } else {
                                    if (Util.hasText(cycleEndDate) && (Long.parseLong(cycleEndDate) <= Long.parseLong(currentDate))) {
                                        remainingLimit = remainingLimit + amountTran;
                                        remainingFreq++;
                                        cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                        cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        logger.info("Limit reversed successfully!!!");
                                    }
                                }
                                //saving limits in db
                                //GeneralDao.Instance.saveOrUpdate(cmsAccountLimit);
                                globalCMSAccountLimitList.add(cmsAccountLimit);
                            }
                            //GeneralDao.Instance.saveOrUpdate(cmsAccountLimitList);
                            //globalCMSAccountLimitList = cmsAccountLimitList;
                        } else {
                            logger.error("Account/Wallet/Card Limit not found for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                            return false;
                        }
                    } else {
                        logger.info("Transaction is a non-financial type, skipping limit, moving forward ...");
                        return true;
                    }
                }
            } else {
                logger.error("Product Limit not found for Account/Wallet/Card [" + cmsAccount.getAccountNumber() + "], rejecting transaction ...");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                return false;
            }

            //saving limits in db
            if (globalCMSAccountLimitList != null && globalCMSAccountLimitList.size() > 0) {
                for (CMSCreditRemainingLimit cmsAccountLimit : globalCMSAccountLimitList) {
                    GeneralDao.Instance.saveOrUpdate(cmsAccountLimit);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to process Account/Wallet/Card Limit, rejecting transaction ...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            if (globalCMSAccountLimitList != null && globalCMSAccountLimitList.size() > 0) {
                for (CMSCreditRemainingLimit cmsAccountLimit : globalCMSAccountLimitList) {
                    GeneralDao.Instance.evict(cmsAccountLimit);
                }
            }
            return false;
        }
        return true;
    }

    //m.rehman: verify maximum account balance limit
    public static boolean VerifyMaxBalanceLimit(WalletCMSWsEntity wsmodel, CMSAccount cmsAccount)
    {
        //m.rehman: checking account balance limit
        logger.info("Checking Maximum Account Balance Limit!");
        Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());
        Long acctBalanceLimit = Long.parseLong(cmsAccount.getProduct().getMaxBalanceLimit());
        Long acctActualBalance = Long.parseLong(cmsAccount.getActualBalance());
        Long finalAmount = amountTran + acctActualBalance;
        if (finalAmount > acctBalanceLimit) {
            logger.error("Maximum Account Balance Limit Exceeding, rejecting transaction!!!");
            wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);    //01 - Limit Exceeded - Refer to Document
            return false;
        }
        return true;
    }

    public static boolean ProcessMerchantWalletLimit(WalletCMSWsEntity wsmodel, CMSAccount cmsAccount, Boolean isApplyLimit)
    {
        logger.info("Checking Limit ...");

        List<CMSAccountLimit> globalCMSAccountLimitList = new ArrayList<CMSAccountLimit>();
        try {

            CMSProduct cmsProduct = cmsAccount.getProduct();

            Map<String, Object> params = new HashMap<String, Object>();
            String query = "from " + CMSLimit.class.getName() + " c " +
                    "where " +
                    "c.channelId = :CHANNEL_ID " +
                    "and c.productId = :PRODUCT_ID " +
                    "and c.limitType = :LIMIT_TYPE ";
            params.put("CHANNEL_ID", wsmodel.getChannelid());
            params.put("PRODUCT_ID", cmsProduct.getProductId());
            params.put("LIMIT_TYPE", CMSLimitType.ACCOUNT_LIMIT);

            String tranCode = GlobalContext.getInstance().getTransactionCodeDescbyCode(wsmodel.getServicename()).getTxncode();
            query += "and c.transactionType = :TRAN_TYPE ";
            params.put("TRAN_TYPE", tranCode);

            if (!tranCode.equals(ISOTransactionCodes.TRANSFER) && !tranCode.equals(ISOTransactionCodes.IBFT)) {
                query += "or c.isShared = 1 ";
            }

            query += "and c.isActive = 1 ";
            query += "order by c.cycleLengthType asc";

            List<CMSLimit> cmsLimitList = GeneralDao.Instance.find(query, params);

            if (cmsLimitList.size() > 0) {
                for (CMSLimit cmsLimit : cmsLimitList) {
                    params = new HashMap<String, Object>();
                    query = "from " + CMSAccountLimit.class.getName() + " c  " +
                            "where " +
                            "c.limitId = :LIMIT_ID " +
                            "and c.acctNumber = :ACCT_NO ";
                    params.put("LIMIT_ID", cmsLimit);
                    params.put("ACCT_NO", cmsAccount.getAccountNumber());
                    List<CMSAccountLimit> cmsAccountLimitList = GeneralDao.Instance.find(query, params);

                    if (cmsAccountLimitList.size() > 0) {
                        for (CMSAccountLimit cmsAccountLimit : cmsAccountLimitList) {
                            Long remainingLimit = Long.parseLong(cmsAccountLimit.getRemainingAmount());
                            Integer remainingFreq = Integer.parseInt(cmsAccountLimit.getRemainingFrequency());
                            String cycleStartDate = cmsAccountLimit.getCycleStartDate();
                            String cycleEndDate = cmsAccountLimit.getCycleEndDate();
                            String currentDate = DateTime.now().getDayDate().getDate().toString();
                            Long amountTran = Long.parseLong(wsmodel.getAmounttransaction());

                            if (isApplyLimit.equals(Boolean.TRUE)) {
                                CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(cmsLimit.getCycleLengthType()));
                                if (cmsLimitCycleType.equals(CMSLimitCycleType.DAILY)) {

                                    if (!Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate)) {
                                        cycleStartDate = currentDate;
                                        cycleEndDate = currentDate;
                                        remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                        remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                    }

                                    if (remainingLimit <= 0) {
                                        logger.error("Daily Transaction Amount Limit Exceeded ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    if (remainingFreq <= 0) {
                                        logger.error("Daily Transaction Frequency Limit Exceeded, rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    if (amountTran > remainingLimit) {
                                        logger.error("Daily Transaction Amount is greater than Remaining Daily Limit, rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    remainingLimit = remainingLimit - amountTran;
                                    remainingFreq--;

                                    cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                    cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                    cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                    cmsAccountLimit.setCycleEndDate(cycleEndDate);

                                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                    logger.info("Daily Limit processed successfully!!!");

                                } else if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {

                                    if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                        cycleStartDate = currentDate;
                                        Calendar c = Calendar.getInstance();
                                        //c.setTime(new Date());
                                        //c.add(Calendar.DAY_OF_MONTH, 30);
                                        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                                        cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());
                                        //cycleEndDate = DateTime.now().currentDate;
                                        remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                        remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                    }

                                    if (remainingLimit <= 0) {
                                        logger.error("Monthly Transaction Amount Limit Exceeded ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    if (remainingFreq <= 0) {
                                        logger.error("Monthly Transaction Frequency Limit Exceeded, rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    if (amountTran > remainingLimit) {
                                        logger.error("Amount transaction is greater than Remaining Monthly Limit, rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    remainingLimit = remainingLimit - amountTran;
                                    remainingFreq--;

                                    cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                    cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                    cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                    cmsAccountLimit.setCycleEndDate(cycleEndDate);

                                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                    logger.info("Monthly Limit processed successfully!!!");

                                } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {

                                    if (!Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate))) {
                                        cycleStartDate = currentDate;
                                        Calendar c = Calendar.getInstance();
                                        //c.setTime(new Date());
                                        //c.add(Calendar.DAY_OF_MONTH, 365);
                                        c.set(Calendar.DAY_OF_YEAR, c.getActualMaximum(Calendar.DAY_OF_YEAR));
                                        cycleEndDate = WebServiceUtil.limitcycleDateFormat.format(c.getTime());
                                        //cycleEndDate = DateTime.now().currentDate;
                                        remainingLimit = Long.parseLong(cmsLimit.getAmount());
                                        remainingFreq = Integer.parseInt(cmsLimit.getFrequencyLength());
                                    }

                                    if (remainingLimit <= 0) {
                                        logger.error("Yearly Transaction Amount Limit Exceeded ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    if (remainingFreq <= 0) {
                                        logger.error("Yearly Transaction Frequency Limit Exceeded, rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    if (amountTran > remainingLimit) {
                                        logger.error("Amount transaction is greater than Remaining Yearly Limit, rejecting transaction ...");
                                        wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LIMIT_EXCEEDED);
                                        return false;
                                    }

                                    remainingLimit = remainingLimit - amountTran;
                                    remainingFreq--;

                                    cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                    cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                    cmsAccountLimit.setCycleStartDate(cycleStartDate);
                                    cmsAccountLimit.setCycleEndDate(cycleEndDate);

                                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                    logger.info("Yearly Limit processed successfully!!!");

                                } else {
                                    logger.error("Invalid Limit Cycle Type, rejecting transaction ...");
                                    wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                                    return false;
                                }
                            } else {
                                if (Util.hasText(cycleEndDate) && (Long.parseLong(cycleEndDate) <= Long.parseLong(currentDate))) {
                                    remainingLimit = remainingLimit + amountTran;
                                    remainingFreq++;
                                    cmsAccountLimit.setRemainingAmount(StringUtils.leftPad(remainingLimit.toString(), 12, '0'));
                                    cmsAccountLimit.setRemainingFrequency(remainingFreq.toString());
                                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                    logger.info("Limit reversed successfully!!!");
                                }
                            }
                            //saving limits in db
                            GeneralDao.Instance.saveOrUpdate(cmsAccountLimit);
                            globalCMSAccountLimitList.add(cmsAccountLimit);
                        }
                        //GeneralDao.Instance.saveOrUpdate(cmsAccountLimitList);
                        //globalCMSAccountLimitList = cmsAccountLimitList;
                    } else {
                        logger.error("Account/Wallet/Card Limit not found, rejecting transaction ...");
                        wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                        return false;
                    }
                }
            } else {
                logger.error("Product Limit not found, rejecting transaction ...");
                wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to process Account/Wallet/Card Limit, rejecting transaction ...");
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            if (globalCMSAccountLimitList != null && globalCMSAccountLimitList.size() > 0) {
                for (CMSAccountLimit cmsAccountLimit : globalCMSAccountLimitList) {
                    GeneralDao.Instance.evict(cmsAccountLimit);
                }
            }
            return false;
        }
        return true;
    }

    public static boolean GeneratePINBlock(WalletCMSWsEntity wsmodel)
    {
        logger.info("Generating PIN.....");
        try {
            if (!HardwareSecurityModule.getInstance().GeneratePINBlock(wsmodel)) {
                logger.error("Unable to generate PIN Block for wallet from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating PIN!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean ValidateCVV(WalletCMSWsEntity wsmodel, CMSAuth cr, Boolean cvvFlag)
    {
        logger.info("Validating CVV/ICVV/CVV2.....");
        try {
            if (!HardwareSecurityModule.getInstance().CVVValidation(wsmodel, cr, cvvFlag)) {
                logger.error("Unable to validate CVV/ICVV/CVV2 for Card from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while validating CVV/ICVV!!!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

	//m.rehman: Euronet Integration
    public static boolean ValidateCAVV(WalletCMSWsEntity wsmodel, CMSAuth cr)
    {
        logger.info("Validating CAVV.....");
        try {
            if (!HardwareSecurityModule.getInstance().CAVVValidation(wsmodel, cr)) {
                logger.error("Unable to validate CAVV for Card from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while validating CAVV!!!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean ValidateCryptogram(WalletCMSWsEntity wsmodel, CMSAuth cr)
    {
        logger.info("Validating Cryptogram.....");
        try {
            if (!HardwareSecurityModule.getInstance().CryptogramValidation(wsmodel, cr)) {
                logger.error("Unable to validate Cryptogram for Card from HSM, rejecting...");
                //wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION); //24-BAD PIN; refer to Doc
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while generating PIN!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean CVVCheckRequired(WalletCMSWsEntity wsmodel) throws Exception
    {
        String posEntryMode, panEntryCapability;
        posEntryMode = wsmodel.getPosentrymode();

        if(posEntryMode != null) {
            panEntryCapability = posEntryMode.substring(0, 2);
            if (panEntryCapability.equals(ISOPOSEntryMode.PANEntryMode.MST_READ_CVV_POSSIBLE)
                    || panEntryCapability.equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_FAIL_MST_READ)) {

                //m.rehman: Euronet Integration, euronet donot sending field-35 as well as tag 57 in chip data so need to ignore cvv operation
                if (Util.hasText(wsmodel.getChannelid())
                        //m.rehman: 10-12-2021, VP-NAP-202111291 / VC-NAP-202111291 / VG-NAP-202111291 - Meezan ATM On-Us Withdrawal/Balance Inquiry and Reversal
                        //adding meezan bank on us atm channel
                        // Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
                        && (wsmodel.getChannelid().equals(ChannelCodes.EURONET) || wsmodel.getChannelid().equals(ChannelCodes.UNILONUSATM))
                        && !Util.hasText(wsmodel.getTrack2Data())
                        && Util.hasText(wsmodel.getIcccarddata())
                        && !Util.hasText(BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.TRK2_EQUIV_DATA))) {
                    logger.info("CVV Validation is not required");
                    return false;
                }

                logger.info("CVV Validation is required");
                return true;
            } else {
                logger.info("CVV Validation is not required");
                return false;
            }
        }
        else
        {
            logger.info("POS Entry Mode not present.");
            return false;
        }
    }

    public static boolean ICVVCheckRequired(WalletCMSWsEntity wsmodel) throws Exception
    {
        String posEntryMode, panEntryCapability;
        posEntryMode = wsmodel.getPosentrymode();

        if(posEntryMode != null) {
            panEntryCapability = posEntryMode.substring(0, 2);
            if (panEntryCapability.equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_CVV_POSSIBLE)
                    || panEntryCapability.equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_CONTACTLESS)) {

                //m.rehman: Euronet Integration, euronet not sending field-35 as well as tag 57 in chip data so need to ignore cvv operation
                if (Util.hasText(wsmodel.getChannelid())
                        //m.rehman: 10-12-2021, VP-NAP-202111291 / VC-NAP-202111291 / VG-NAP-202111291 - Meezan ATM On-Us Withdrawal/Balance Inquiry and Reversal
                        //adding meezan bank on us atm channel
                        // Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
                        && (wsmodel.getChannelid().equals(ChannelCodes.EURONET) || wsmodel.getChannelid().equals(ChannelCodes.UNILONUSATM))
                        && !Util.hasText(wsmodel.getTrack2Data())
                        && Util.hasText(wsmodel.getIcccarddata())
                        && !Util.hasText(BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.TRK2_EQUIV_DATA))) {
                    logger.info("CVV Validation is not required");
                    return false;
                }

                logger.info("ICVV Check is possible");
                return true;
            } else {
                logger.info("ICVV Check not possible");
                return false;
            }
        }
        else
        {
            logger.info("POS Entry Mode not present.");
            return false;
        }
    }

	//m.rehman: Euronet Integration
    public static boolean CAVVCheckRequired(WalletCMSWsEntity wsmodel) throws Exception
    {
        if (Util.hasText(wsmodel.getCavvdata())) {
            logger.info("CAVV data present ...");
            return true;

        } else {
            logger.error("CAVV data not present ...");
            return false;
        }
    }

	//m.rehman: Euronet Integration
    public static boolean CVV2CheckRequired(WalletCMSWsEntity wsmodel) throws Exception
    {
        if (Util.hasText(wsmodel.getCvv2())) {
            logger.info("CVV2 data present ...");
            return true;

        } else {
            logger.error("CVV2 data not present ...");
            return false;
        }
    }

    public static boolean CreateMerchantWalletProfile(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            String dbQuery;
            Map<String, Object> params;
            if(account == null)
            {
                account = new CMSAccount();
                account.setAccountTitle(wsmodel.getMerchantname());
                account.setAvailableBalance("000000000000");
                account.setActualBalance("000000000000");
                account.setCreateDate(new Date());
                //account.setCurrency(wsmodel.getAccountcurrency());
                account.setLastUpdateDate(new Date());
                account.setStatus("00"); //Raza Setting Status 08 as PIN is not created Yet
                account.setAccountType(AccType.MERCHANT_WALLET.toString());
                account.setBranchId(wsmodel.getBank());
                account.setLevel(AccType.MERCHANT_WALLET.toString());
                account.setCategory(AccType.CAT_MERCHANT_WALLET_VALUE);
                account.setUserId(wsmodel.getMerchantid());

                Currency currency = GlobalContext.getInstance().getCurrencybySwiftCode(wsmodel.getAccountcurrency());
                if (currency != null) {
                    account.setCurrency(currency.getCode().toString());
                } else {
                    account.setCurrency(GlobalContext.getInstance().getBaseCurrency().getCode().toString());
                }
                //account.setCurrency(wsmodel.getAccountcurrency());

                account.setIsprimary("1");

                List<CMSProduct> productlist;
                dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
                params = new HashMap<String, Object>();
                params.put("prdcttype", "MER_WLLT"); //"LVL0");
                params.put("default", "1");
                productlist = GeneralDao.Instance.find(dbQuery, params);

                if (productlist != null && productlist.size() > 0) {
                    //Raza ignore if there are multiple default wallet products
                    logger.info("Assigning Wallet Product to Account..");
                    CMSProduct product = productlist.get(0);
                    account.setProduct(product);

                    //m.rehman: 15-09-2021, VP-NAP-202109092 / VG-NAP-202109101 - Non financial Transactions on VaulGuard
                    //to avoid duplicate account number generation in craete wallet on transaction load
                    //account.setAccountNumber(GenerateWalletAccountNumber(product));
                    account.setAccountNumber(GenerateWalletAccountNumberFromSeq(product, "CMS_MERCHANT_ACCOUNT_NO_SEQ"));
                    ////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241
                    account.setiBan(obj_IBANUtil.generateIBAN(account.getAccountNumber()));
                    // ======================================================================================

                    GeneralDao.Instance.saveOrUpdate(account);

                    //Raza User-Id To Account Relation
                    NayaPayRelation npr = new NayaPayRelation();
                    npr.setUser_Acct_Id(account.getUserId());
                    npr.setAccount(account);
                    GeneralDao.Instance.saveOrUpdate(npr);
                    //Raza User-Id To Account Relation

                    if (AssignAccountLimits(product, account, wsmodel)) {
                        logger.info("Wallet limits assign successfully ...");
                        logger.info("Wallet Account Profile created successfully");

                        logger.info("Moving forward for Prepaid Card Wallet ...");

                        if (WalletCMSFunctions.CreatePrepaidWalletProfile(wsmodel, null)) {
                            logger.info("Prepaid Card Wallet Profile Created Successfully for Merchant [" + wsmodel.getMerchantid() + "]");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                            return true;

                        } else {
                            logger.error("Unable to create Prepaid Card Wallet Profile for Merchant [" + wsmodel.getMerchantid() + "]");
                            //GeneralDao.Instance.evict(account); //Raza Dont save if declined
                            wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                            GeneralDao.Instance.clear();
                            GeneralDao.Instance.saveOrUpdate(wsmodel);
                            return false;
                        }

                    } else {
                        logger.info("Unable assign Merchant Wallet limits, rejecting ...");
                        //GeneralDao.Instance.evict(account); //Raza Dont save if declined
                        GeneralDao.Instance.clear();
                        GeneralDao.Instance.saveOrUpdate(wsmodel);
                        return false;
                    }

                } else {
                    logger.error("No Wallet Product found to assign.. rejecting txn..");
                    //GeneralDao.Instance.evict(account);//no account object saved
                    wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND);
                    //GeneralDao.Instance.clear();
                    //GeneralDao.Instance.saveOrUpdate(wsmodel);
                    return false;
                }
            }
            else
            {
                logger.info("Updating Status of Soft Deleted Wallet..");
                account.setStatus("00"); //Raza Rest PIN or Generate PIN
                account.setLastUpdateDate(new Date());
                GeneralDao.Instance.saveOrUpdate(account);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            e.printStackTrace();
            return false;
        }
        wsmodel.setAccountnumber(account.getAccountNumber()); //Raza Return Account Number to Middleware For PIN Generation
        return true;
    }

    public static boolean CreatePrepaidWalletProfile(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            String dbQuery, userId;
            Map<String, Object> params;
            if(account == null)
            {
                //in order to use prepaid wallet for both user and merchant id
                if (Util.hasText(wsmodel.getMerchantid())) {
                    userId = wsmodel.getMerchantid();
                } else {
                    userId = wsmodel.getUserid();
                }
                account = new CMSAccount();
                account.setAccountTitle(wsmodel.getMerchantname());
                account.setCreateDate(new Date());
                //account.setCurrency(wsmodel.getAccountcurrency());
                account.setLastUpdateDate(new Date());
                account.setStatus("00"); //Raza Setting Status 08 as PIN is not created Yet
                account.setAccountType(AccType.PREPAID_WALLET.toString());
                account.setBranchId(wsmodel.getBank());
                account.setLevel(AccType.PREPAID_WALLET.toString());
                account.setCategory(AccType.CAT_PREPAID_WALLET_VALUE);
                account.setUserId(userId);

                Currency currency = GlobalContext.getInstance().getCurrencybySwiftCode(wsmodel.getAccountcurrency());
                if (currency != null) {
                    account.setCurrency(currency.getCode().toString());
                } else {
                    account.setCurrency(GlobalContext.getInstance().getBaseCurrency().getCode().toString());
                }
                //account.setCurrency(wsmodel.getAccountcurrency());

                account.setIsprimary("1");

                List<CMSProduct> productlist;
                dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
                params = new HashMap<String, Object>();
                params.put("prdcttype", "PREPAID"); //"LVL0");
                params.put("default", "1");
                productlist = GeneralDao.Instance.find(dbQuery, params);

                if (productlist != null && productlist.size() > 0) {
                    //Raza ignore if there are multiple default wallet products
                    logger.info("Assigning Prepaid Wallet Product to Account..");
                    CMSProduct product = productlist.get(0);
                    account.setActualBalance(product.getProductDetail().getPrepProdAmount());
                    account.setAvailableBalance(product.getProductDetail().getPrepProdAmount());
                    account.setProduct(product);

                    //m.rehman: 15-09-2021, VP-NAP-202109092 / VG-NAP-202109101 - Non financial Transactions on VaulGuard
                    //to avoid duplicate account number generation in craete wallet on transaction load
                    //account.setAccountNumber(GenerateWalletAccountNumber(product));
                    account.setAccountNumber(GenerateWalletAccountNumberFromSeq(product, "CMS_PREPAID_ACCOUNT_NO_SEQ"));
                    ////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241
                    account.setiBan(obj_IBANUtil.generateIBAN(account.getAccountNumber()));
                    // ======================================================================================

                    GeneralDao.Instance.saveOrUpdate(account);

                    //Raza User-Id To Account Relation
                    NayaPayRelation npr = new NayaPayRelation();
                    npr.setUser_Acct_Id(account.getUserId());
                    npr.setAccount(account);
                    GeneralDao.Instance.saveOrUpdate(npr);
                    //Raza User-Id To Account Relation

                    if (AssignAccountLimits(product, account, wsmodel)) {
                        logger.info("Prepaid Wallet limits assign successfully ...");
                        logger.info("Prepaid Wallet Account Profile created successfully");

                        logger.info("Moving forward for Prepaid Card ...");

                        if (WalletCMSFunctions.CreatePrepaidCardProfile(wsmodel, account)) {
                            logger.info("Prepaid Card Profile Created Successfully for Merchant [" + wsmodel.getMerchantid() + "]");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                            return true;

                        } else {
                            logger.error("Unable to Prepaid Create Card Profile for Merchant [" + wsmodel.getMerchantid() + "]");
                            //GeneralDao.Instance.evict(account); //Raza Dont save if declined
                            //wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                            GeneralDao.Instance.clear();
                            GeneralDao.Instance.saveOrUpdate(wsmodel);
                            return false;
                        }

                    } else {
                        logger.info("Unable assign Prepaid Card limits, rejecting ...");
                        //GeneralDao.Instance.evict(account); //Raza Dont save if declined
                        GeneralDao.Instance.clear();
                        GeneralDao.Instance.saveOrUpdate(wsmodel);
                        return false;
                    }

                } else {
                    logger.error("No Prepaid Wallet Product found to assign.. rejecting txn..");
                    //GeneralDao.Instance.evict(account);//no account object saved
                    wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND);
                    return false;
                }
            }
            else
            {
                logger.info("Updating Status of Soft Deleted Wallet..");
                account.setStatus("00"); //Raza Rest PIN or Generate PIN
                account.setLastUpdateDate(new Date());
                GeneralDao.Instance.saveOrUpdate(account);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            e.printStackTrace();
            return false;
        }
        wsmodel.setAccountnumber(account.getAccountNumber()); //Raza Return Account Number to Middleware For PIN Generation
        return true;
    }

    public static boolean CreatePrepaidCardProfile(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try
        {
            String dbQuery;
            Map<String, Object> params;
            if(account != null)
            {
                CMSCard card = new CMSCard();
                card.setCardName(account.getAccountTitle());
                //card.setCreateDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                card.setRequestDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                //Set<CMSAccount> acctlist = new HashSet<CMSAccount>();
                //acctlist.add(account);
                //card.setList_CustAccounts(acctlist);
                //Expiry
                /*
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.YEAR, 5);
                card.setExpiryDate(WebServiceUtil.limitcycleDateFormat.format(c.getTime()));
                */

                //Expiry
                card.setCardStatus("14"); //Raza Update THIS
                card.setCustomer(account.getCustomer()); //Raza updating 22-02-2019

                //m.rehman: 11-08-2020: saving tracking id for card embossing file
                card.setTrackingId(wsmodel.getTrackingid());

                params = null;
                List<CMSProduct> productlist;
                dbQuery = "from " + CMSProduct.class.getName() + " c where c.productType= :prdcttype " + " and c.isdefault = :default ";
                params = new HashMap<String, Object>();
                params.put("prdcttype", "PREPAID"); //"LVL0");
                params.put("default", "1");
                productlist = GeneralDao.Instance.find(dbQuery, params);

                if (productlist != null && productlist.size() > 0) {
                    //Raza ignore if there are multiple default wallet products
                    logger.info("Assigning Prepaid Card Product to Card..");
                    CMSProduct product = productlist.get(0);
                    card.setProduct(product);

                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    //c.add(Calendar.YEAR, 5);
                    c.add(Calendar.YEAR, Integer.parseInt(product.getProductDetail().getValidYearsRenewal()));
                    card.setExpiryDate(WebServiceUtil.limitcycleDateFormat.format(c.getTime()));

                    //m.rehman: commenting below line, need to save encrypted value of pan in db
                    //card.setCardNumber(GenerateWalletAccountNumber(product));
                    //m.rehman: 06-08-2020, Euronet Integration, PAN generation according to pan format defined for scheme
                    //String cardNo = GenerateWalletAccountNumber(product);
                    String cardNo = GeneratePAN(product);
                    if (!Util.hasText(cardNo)) {
                        logger.error("Card number generation failed !!!");
                        return false;
                    }

                    //TODO: m.rehman: remove below logging
                    logger.debug("cardNo [" + cardNo + "]");
                    card.setCardNoLastDigits(cardNo.substring(cardNo.length() - 4, cardNo.length()));

                    //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
                    //card.setCardNumber(WebServiceUtil.getPANEncryptedValue(cardNo, ChannelCodes.SWITCH));
                    card.setCardNumber(WebServiceUtil.getPANEncryptedValue(cardNo));
                    //////////////////////////////////////////////////////////////////////////////////////

                    card.setPrimaryCardNumber(card.getCardNumber());

                    //m.rehman: commenting below line, need to save encrypted value of pan in db
                    //card.setCardNumberExpiryRelation(card.getCardNumber() + "=" + card.getExpiryDate().substring(2,6));
                    logger.debug("Rel [" + cardNo + "=" + card.getExpiryDate().substring(2, 6) + "]");

                    //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
                    //card.setCardNumberExpiryRelation(WebServiceUtil.getPANEncryptedValue((cardNo + "=" + card.getExpiryDate().substring(2, 6)), ChannelCodes.SWITCH));
                    card.setCardNumberExpiryRelation(WebServiceUtil.getPANEncryptedValue(cardNo + "=" + card.getExpiryDate().substring(2, 6)));
                    //////////////////////////////////////////////////////////////////////////////////////

                    //Arsalan Akhter, Date: 12_March_2021, Tracking ID: VP-NAP-202103114_VC-NAP-202103114
                    card.setIsExported("0");
                    /////////////////////////////////////////////////////////////////////////////////////

                    GeneralDao.Instance.saveOrUpdate(card);
                    account.setCard(card);

                    if (AssignCardLimits(productlist.get(0), account, card)) {
                        logger.info("Prepaid Card limits assign successfully ...");

                        //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                        logger.info("Assigning card controls ...");
                        CMSCardControlConfig ccccc  = new CMSCardControlConfig();
                        ccccc.setCard(card);
                        ccccc.setCashWithdrawalEnabled(true);
                        ccccc.setChipPinEnabled(true);
                        ccccc.setInternationalTxnsEnabled(true);
                        ccccc.setMagStripeEnabled(true);
                        ccccc.setNFCEnabled(true);
                        ccccc.setOnlineEnabled(true);
                        GeneralDao.Instance.save(ccccc);

                        logger.info("Prepaid Card Profile created successfully");
                        return true;

                    } else {
                        logger.info("Unable assign Prepaid Card limits, rejecting ...");
                        wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                        GeneralDao.Instance.saveOrUpdate(wsmodel);
                        return false;
                    }

                } else {
                    logger.error("No Prepaid Card Product found to assign.. rejecting txn..");
                    wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND);
                    //GeneralDao.Instance.evict(account);//no account object saved
                    GeneralDao.Instance.evict(card); //Raza Dont save if declined
                    return false;
                }
            }
            else
            {
                logger.error("No Prepaid wallet found to create card profile");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_NOT_FOUND);
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean AssignCardLimits(CMSProduct product, CMSAccount account, CMSCard card)
    {

        List<String> channelDebitList;
        List<String> channelCreditList;
        List<CMSProductDebitLimit> cmsDebitLimitList;
        List<CMSProductCreditLimit> cmsCreditLimitList;
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                " where c.productId= :prdctid " +
                " and c.limitType = :limittype ";// +
                //" and c.channelId is not null ";
        params = new HashMap<String, Object>();
        params.put("prdctid", product);
        params.put("limittype", CMSLimitType.CARD_LIMIT);
        channelDebitList = GeneralDao.Instance.find(dbQuery, params);

        if (channelDebitList != null && channelDebitList.size() > 0) {
            for (String channel : channelDebitList) {

                if (Util.hasText(channel)) {
                    CMSAuth cauth = new CMSAuth();
                    cauth.setRelation(card.getCardNumberExpiryRelation());
                    cauth.setCard(card);
                    cauth.setAccount(account);
                    cauth.setCustomer(account.getCustomer());
                    cauth.setChannelId(channel);
                    cauth.setOffset("0000");
                    //cauth.setRemRetries("3");
                    //cauth.setMaxRetries("3");
                    cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                    cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                    cauth.setReasonCode("0000");
                    cauth.setStatus("07");
                    cauth.setIsDefault("1");
                    GeneralDao.Instance.saveOrUpdate(cauth);
                }

                dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                        " where c.productId= :prdctid " +
                        " and c.limitType = :limittype " +
                        //" and c.channelId = :channel " +
                        " and c.parentLimitId is null " +
                        " and c.limitCategory = 2 " +    //limit category 2 is Financial limit
                        //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                        //adding is active check
                        " and c.isActive='1' ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.CARD_LIMIT);
                //params.put("channel", channel);

                if (Util.hasText(channel)) {
                    dbQuery += " and c.channelId = :channel ";
                    params.put("channel", channel);
                } else {
                    dbQuery += " and c.channelId is null ";
                }
                cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                    for (CMSProductDebitLimit climit : cmsDebitLimitList) {

                        Integer cycleLength = 0;
                        CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                        acctLimit.setRelation(card.getCardNumber());
                        acctLimit.setLimitId(climit);
                        acctLimit.setRemainingAmount(climit.getAmount());
                        acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                        cycleLength = Integer.parseInt(climit.getCycleLength());
                        acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                        DateTime cycleEndDate = DateTime.now();
                        Calendar calendar = Calendar.getInstance();
                        CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                        if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else {
                            if (cycleLength > 1) {
                                cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                            }
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                        }
                        acctLimit.setIsCustProfile("0");
                        GeneralDao.Instance.saveOrUpdate(acctLimit);
                    }
                }
            }
        }

        dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c" +
                " where c.productId= :prdctid " +
                " and c.limitType = :limittype ";// +
                //" and c.channelId is not null ";
        params = new HashMap<String, Object>();
        params.put("prdctid", product);
        params.put("limittype", CMSLimitType.CARD_LIMIT);
        channelCreditList = GeneralDao.Instance.find(dbQuery, params);

        if (channelCreditList != null && channelCreditList.size() > 0) {
            for (String channel : channelCreditList) {

                if ((channelDebitList == null || (channelDebitList != null && channelDebitList.size() <= 0)) ||
                        (channelDebitList != null && channelDebitList.size() > 0 && !channelDebitList.contains(channel))
                ) {
                    if (Util.hasText(channel)) {
                        CMSAuth cauth = new CMSAuth();
                        cauth.setRelation(card.getCardNumberExpiryRelation());
                        cauth.setCard(card);
                        cauth.setAccount(account);
                        cauth.setCustomer(account.getCustomer());
                        cauth.setChannelId(channel);
                        cauth.setOffset("0000");
                        //cauth.setRemRetries("3");
                        //cauth.setMaxRetries("3");
                        cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                        cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                        cauth.setReasonCode("0000");
                        cauth.setStatus("07");
                        cauth.setIsDefault("1");
                        GeneralDao.Instance.saveOrUpdate(cauth);
                    }
                }

                dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                        " where c.productId= :prdctid " +
                        " and c.limitType = :limittype " +
                        //" and c.channelId = :channel " +
                        " and c.parentLimitId is null " +
                        " and c.limitCategory = 2 " +    //limit category 2 is Financial limit
                        //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                        //adding is active check
                        " and c.isActive='1' ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.CARD_LIMIT);
                //params.put("channel", channel);

                if (Util.hasText(channel)) {
                    dbQuery += " and c.channelId = :channel ";
                    params.put("channel", channel);
                } else {
                    dbQuery += " and c.channelId is null ";
                }
                cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                    for (CMSProductCreditLimit climit : cmsCreditLimitList) {

                        Integer cycleLength = 0;
                        CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                        acctLimit.setRelation(card.getCardNumber());
                        acctLimit.setLimitId(climit);
                        acctLimit.setRemainingAmount(climit.getAmount());
                        acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                        cycleLength = Integer.parseInt(climit.getCycleLength());
                        acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                        DateTime cycleEndDate = DateTime.now();
                        Calendar calendar = Calendar.getInstance();
                        CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                        if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else {
                            if (cycleLength > 1) {
                                cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                            }
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                        }
                        acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));
                        acctLimit.setIsCustProfile("0");
                        GeneralDao.Instance.saveOrUpdate(acctLimit);
                    }
                }
            }
        }

        if (channelDebitList.size() <= 0 && channelCreditList.size() <= 0) {
            logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
            GeneralDao.Instance.clear();
            //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
            //commenting below check as card limits are optional
            //return false;
        }

        return true;
    }

    public static boolean AssignAccountLimits(CMSProduct product, CMSAccount account, WalletCMSWsEntity wsmodel)
    {
        logger.info("Assigning Account Limits ...");

        List<String> channelDebitList;
        List<String> channelCreditList;
        List<CMSProductDebitLimit> cmsDebitLimitList;
        List<CMSProductCreditLimit> cmsCreditLimitList;
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                " where c.productId= :prdctid " +
                " and c.limitType = :limittype ";// +
                //" and c.channelId is not null ";
        params = new HashMap<String, Object>();
        params.put("prdctid", product);
        params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
        channelDebitList = GeneralDao.Instance.find(dbQuery, params);

        if (channelDebitList != null && channelDebitList.size() > 0) {
            for (String channel : channelDebitList) {

                if (Util.hasText(channel)) {
                    logger.info("Adding Auth Entries ...");
                    CMSAuth cauth = new CMSAuth();
                    cauth.setRelation(account.getAccountNumber());
                    cauth.setAccount(account);
                    cauth.setCustomer(account.getCustomer());
                    cauth.setChannelId(channel);
                    cauth.setOffset("0000");
                    //cauth.setRemRetries("3");
                    //cauth.setMaxRetries("3");
                    cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                    cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                    cauth.setReasonCode("0000");
                    cauth.setStatus("08");
                    cauth.setIsDefault("1");
                    GeneralDao.Instance.saveOrUpdate(cauth);
                }

                dbQuery = "from " + CMSProductDebitLimit.class.getName() + " c" +
                        " where c.productId= :prdctid " +
                        " and c.limitType = :limittype " +
                        //" and c.channelId = :channel " +
                        " and c.parentLimitId is null " +
                        " and c.limitCategory = 2 " +   //limit category 2 is Financial limit
                        //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                        //adding is active check
                        " and c.isActive='1' ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                //params.put("channel", channel);

                if (Util.hasText(channel)) {
                    dbQuery += " and c.channelId = :channel ";
                    params.put("channel", channel);
                } else {
                    dbQuery += " and c.channelId is null ";
                }
                cmsDebitLimitList = GeneralDao.Instance.find(dbQuery, params);

                if (cmsDebitLimitList != null && cmsDebitLimitList.size() > 0) {
                    logger.info("Adding Debit Limit Entries ...");
                    for (CMSProductDebitLimit climit : cmsDebitLimitList) {

                        Integer cycleLength = 0;
                        CMSDebitRemainingLimit acctLimit = new CMSDebitRemainingLimit();
                        acctLimit.setRelation(account.getAccountNumber());
                        acctLimit.setLimitId(climit);
                        acctLimit.setRemainingAmount(climit.getAmount());
                        acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                        cycleLength = Integer.parseInt(climit.getCycleLength());
                        acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                        DateTime cycleEndDate = DateTime.now();
                        Calendar calendar = Calendar.getInstance();
                        CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                        if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else {
                            if (cycleLength > 1) {
                                cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                            }
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                        }
                        acctLimit.setIsCustProfile("0");
                        GeneralDao.Instance.saveOrUpdate(acctLimit);
                    }
                }
            }
        }

        dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c" +
                " where c.productId= :prdctid " +
                " and c.limitType = :limittype ";// +
                //" and c.channelId is not null ";
        params = new HashMap<String, Object>();
        params.put("prdctid", product);
        params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
        channelCreditList = GeneralDao.Instance.find(dbQuery, params);

        if (channelCreditList != null && channelCreditList.size() > 0) {
            for (String channel : channelCreditList) {

                if ((channelDebitList == null || (channelDebitList != null && channelDebitList.size() <= 0)) ||
                        (channelDebitList != null && channelDebitList.size() > 0 && !channelDebitList.contains(channel))
                ) {
                    if (Util.hasText(channel)) {
                        logger.info("Adding Auth Entries ...");
                        CMSAuth cauth = new CMSAuth();
                        cauth.setRelation(account.getAccountNumber());
                        cauth.setAccount(account);
                        cauth.setCustomer(account.getCustomer());
                        cauth.setChannelId(channel);
                        cauth.setOffset("0000");
                        //cauth.setRemRetries("3");
                        //cauth.setMaxRetries("3");
                        cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                        cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                        cauth.setReasonCode("0000");
                        cauth.setStatus("08");
                        cauth.setIsDefault("1");
                        GeneralDao.Instance.saveOrUpdate(cauth);
                    }
                }

                dbQuery = "from " + CMSProductCreditLimit.class.getName() + " c" +
                        " where c.productId= :prdctid " +
                        " and c.limitType = :limittype " +
                        //" and c.channelId = :channel " +
                        " and c.parentLimitId is null " +
                        " and c.limitCategory = 2 " +    //limit category 2 is Financial limit
                        //m.rehman: 30-08-2021, VC-NAP-202108021 - Limit Structure update
                        //adding is active check
                        " and c.isActive='1' ";
                params = new HashMap<String, Object>();
                params.put("prdctid", product);
                params.put("limittype", CMSLimitType.ACCOUNT_LIMIT);
                //params.put("channel", channel);

                if (Util.hasText(channel)) {
                    dbQuery += " and c.channelId = :channel ";
                    params.put("channel", channel);
                } else {
                    dbQuery += " and c.channelId is null ";
                }
                cmsCreditLimitList = GeneralDao.Instance.find(dbQuery, params);

                if (cmsCreditLimitList != null && cmsCreditLimitList.size() > 0) {
                    logger.info("Adding Credit Limit Entries ...");
                    for (CMSProductCreditLimit climit : cmsCreditLimitList) {

                        Integer cycleLength = 0;
                        CMSCreditRemainingLimit acctLimit = new CMSCreditRemainingLimit();
                        acctLimit.setRelation(account.getAccountNumber());
                        acctLimit.setLimitId(climit);
                        acctLimit.setRemainingAmount(climit.getAmount());
                        acctLimit.setRemainingFrequency(climit.getFrequencyLength());
                        cycleLength = Integer.parseInt(climit.getCycleLength());
                        acctLimit.setCycleStartDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                        DateTime cycleEndDate = DateTime.now();
                        Calendar calendar = Calendar.getInstance();
                        CMSLimitCycleType cmsLimitCycleType = new CMSLimitCycleType(Integer.parseInt(climit.getCycleLengthType()));
                        if (cmsLimitCycleType.equals(CMSLimitCycleType.MONTHLY)) {
                            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else if (cmsLimitCycleType.equals(CMSLimitCycleType.YEARLY)) {
                            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(calendar.getTime()));

                        } else {
                            if (cycleLength > 1) {
                                cycleEndDate.increase((cycleLength - 1) * 24 * 60);
                            }
                            acctLimit.setCycleEndDate(WebServiceUtil.limitcycleDateFormat.format(cycleEndDate.toDate()));
                        }
                        acctLimit.setIsCustProfile("0");
                        GeneralDao.Instance.saveOrUpdate(acctLimit);
                    }
                }
            }
        }

        if (channelDebitList.size() <= 0 && channelCreditList.size() <= 0) {
            logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
            wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND);
            GeneralDao.Instance.clear();
            GeneralDao.Instance.saveOrUpdate(wsmodel);
            return false;
        }

        return true;
    }

    public static boolean RenewCardProfile(WalletCMSWsEntity wsmodel, CMSAccount account, CMSCard cmsCard)
    {
        try
        {
            if(account != null)
            {
                CMSCard card = new CMSCard();
                card.setCardName(account.getAccountTitle());
                card.setRequestDate(WebServiceUtil.limitcycleDateFormat.format(new Date()));

                //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                if (Util.hasText(wsmodel.getCardtype()) && !wsmodel.getCardtype().equals("visa_virtual")) {
                    Set<CMSAccount> acctlist = new HashSet<CMSAccount>();
                    acctlist.add(account);
                    card.setList_CustAccounts(acctlist);
                }

                //Expiry
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                //c.add(Calendar.YEAR, 5);
                c.add(Calendar.YEAR, Integer.parseInt(cmsCard.getProduct().getProductDetail().getValidYearsRenewal()));
                card.setExpiryDate(WebServiceUtil.limitcycleDateFormat.format(c.getTime()));

                card.setCardStatus("14"); //Raza Update THIS
                card.setCustomer(account.getCustomer()); //Raza updating 22-02-2019
                card.setProduct(cmsCard.getProduct());
                card.setCardNoLastDigits(cmsCard.getCardNoLastDigits());
                card.setCardNumber(cmsCard.getCardNumber());
                card.setPrimaryCardNumber(cmsCard.getCardNumber());

                //m.rehman: 11-08-2020: saving tracking id for card embossing file
                card.setTrackingId(wsmodel.getTrackingid());

                //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
                //String cardNo = WebServiceUtil.getPANDecryptedValue(cmsCard.getCardNumber(), ChannelCodes.SWITCH);
                String cardNo = WebServiceUtil.getPANDecryptedValue(cmsCard.getCardNumber());
                /////////////////////////////////////////////////////////////////////////////////////////////////////

                logger.debug("Rel [" + cardNo + "=" + card.getExpiryDate().substring(2, 6) + "]");

                //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
                //card.setCardNumberExpiryRelation(WebServiceUtil.getPANEncryptedValue((cardNo + "=" + card.getExpiryDate().substring(2, 6)), ChannelCodes.SWITCH));
                card.setCardNumberExpiryRelation(WebServiceUtil.getPANEncryptedValue(cardNo + "=" + card.getExpiryDate().substring(2, 6)));
                //////////////////////////////////////////////////////////////////////////////////////////

                //Arsalan Akhter, Date: 12_March_2021, Tracking ID: VP-NAP-202103114_VC-NAP-202103114
                card.setIsExported("0");
                /////////////////////////////////////////////////////////////////////////////////////

                GeneralDao.Instance.saveOrUpdate(card);

                //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                if (Util.hasText(wsmodel.getCardtype()) && !wsmodel.getCardtype().equals("visa_virtual")) {
                    account.setCard(card);
                }
                //account.setCard(card);

                if (AssignRenewCardLimits(card.getProduct(), account, card)) {
                    logger.info("Card limits assign successfully ...");

                    //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
                    logger.info("Assigning card controls ...");
                    CMSCardControlConfig ccccc  = new CMSCardControlConfig();
                    ccccc.setCard(card);
                    ccccc.setCashWithdrawalEnabled(true);
                    ccccc.setChipPinEnabled(true);
                    ccccc.setInternationalTxnsEnabled(true);
                    ccccc.setMagStripeEnabled(true);
                    ccccc.setNFCEnabled(true);
                    ccccc.setOnlineEnabled(true);
                    GeneralDao.Instance.save(ccccc);

                    logger.info("Card Profile created successfully");
                    return true;

                } else {
                    logger.info("Unable assign Card limits, rejecting ...");
                    wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
                    GeneralDao.Instance.evict(card);
                    return false;
                }
            }
            else
            {
                logger.error("No account found to create card profile");
                wsmodel.setRespcode(ISOResponseCodes.NP_PRI_DATA_ELEM_NOT_FOUND);
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while creating Wallet Account profile!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean AssignRenewCardLimits(CMSProduct product, CMSAccount account, CMSCard card)
    {

        List<String> channelDebitList;
        List<String> channelCreditList;
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "select distinct c.channelId from " + CMSProductDebitLimit.class.getName() + " c" +
                " where c.productId= :prdctid " +
                " and c.limitType = :limittype ";// +
        //" and c.channelId is not null ";
        params = new HashMap<String, Object>();
        params.put("prdctid", product);
        params.put("limittype", CMSLimitType.CARD_LIMIT);
        channelDebitList = GeneralDao.Instance.find(dbQuery, params);

        if (channelDebitList != null && channelDebitList.size() > 0) {
            for (String channel : channelDebitList) {

                if (Util.hasText(channel)) {
                    CMSAuth cauth = new CMSAuth();
                    cauth.setRelation(card.getCardNumberExpiryRelation());
                    cauth.setCard(card);
                    cauth.setAccount(account);
                    cauth.setCustomer(account.getCustomer());
                    cauth.setChannelId(channel);
                    cauth.setOffset("0000");
                    cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                    cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                    cauth.setReasonCode("0000");
                    cauth.setStatus("07");
                    cauth.setIsDefault("1");
                    GeneralDao.Instance.saveOrUpdate(cauth);
                }
            }
        }

        dbQuery = "select distinct c.channelId from " + CMSProductCreditLimit.class.getName() + " c" +
                " where c.productId= :prdctid " +
                " and c.limitType = :limittype ";// +
        //" and c.channelId is not null ";
        params = new HashMap<String, Object>();
        params.put("prdctid", product);
        params.put("limittype", CMSLimitType.CARD_LIMIT);
        channelCreditList = GeneralDao.Instance.find(dbQuery, params);

        if (channelCreditList != null && channelCreditList.size() > 0) {
            for (String channel : channelCreditList) {

                if ((channelDebitList == null || (channelDebitList != null && channelDebitList.size() <= 0)) ||
                        (channelDebitList != null && channelDebitList.size() > 0 && !channelDebitList.contains(channel))
                        ) {
                    if (Util.hasText(channel)) {
                        CMSAuth cauth = new CMSAuth();
                        cauth.setRelation(card.getCardNumberExpiryRelation());
                        cauth.setCard(card);
                        cauth.setAccount(account);
                        cauth.setCustomer(account.getCustomer());
                        cauth.setChannelId(channel);
                        cauth.setOffset("0000");
                        cauth.setRemRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                        cauth.setMaxRetries((product.getRetriesCount() != null) ? product.getRetriesCount().toString() : "");
                        cauth.setReasonCode("0000");
                        cauth.setStatus("07");
                        cauth.setIsDefault("1");
                        GeneralDao.Instance.saveOrUpdate(cauth);
                    }
                }
            }
        }

        if (channelDebitList.size() <= 0 && channelCreditList.size() <= 0) {
            logger.error("Limit configuration not found for Productid [" + product.getProductId() + "]");
            GeneralDao.Instance.clear();
            return false;
        }

        return true;
    }


    // Asim Shahzad, Date : 20th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)

    public static boolean UpdateCardControls(WalletCMSWsEntity wsmodel, CMSCard card)
    {
        try {
            logger.info("Updating Card Controls...");

            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + CMSCardControlConfig.class.getName() + " c where c.card= :CARD  ";
            params = new HashMap<String, Object>();
            params.put("CARD", card);

            CMSCardControlConfig cardControlConfig = (CMSCardControlConfig)GeneralDao.Instance.findObject(dbQuery, params);

            if(cardControlConfig != null)
            {
                cardControlConfig.setChipPinEnabled((wsmodel.getIsChipPinEnabled().toLowerCase().equals("true") ? true : false));
                cardControlConfig.setMagStripeEnabled((wsmodel.getIsMagStripeEnabled().toLowerCase().equals("true") ? true : false));
                cardControlConfig.setCashWithdrawalEnabled((wsmodel.getIsCashWithdrawalEnabled().toLowerCase().equals("true") ? true : false));
                cardControlConfig.setNFCEnabled((wsmodel.getIsNFCEnabled().toLowerCase().equals("true") ? true : false));
                cardControlConfig.setOnlineEnabled((wsmodel.getIsOnlineEnabled().toLowerCase().equals("true") ? true : false));
                cardControlConfig.setInternationalTxnsEnabled((wsmodel.getIsInternationalTxnsEnabled().toLowerCase().equals("true") ? true : false));

                GeneralDao.Instance.saveOrUpdate(cardControlConfig);
                return true;
            }
            else
            {
                logger.error("Card Controls not found for Card Number [" + wsmodel.getCardnumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND); //NP_6005 - refer to Document
                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception caught while updating card controls!");
            return false;
        }
    }

    // =============================================================================================================================

    //m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 3
    public static boolean CheckCardControls(WalletCMSWsEntity wsmodel, CMSCard card)
    {
        try {
            logger.info("Checking Card Controls...");

            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + CMSCardControlConfig.class.getName() + " c where c.card= :CARD  ";
            params = new HashMap<String, Object>();
            params.put("CARD", card);

            CMSCardControlConfig cardControlConfig = (CMSCardControlConfig)GeneralDao.Instance.findObject(dbQuery, params);

            if(cardControlConfig != null)
            {
                //chip and pin flag
                if (((Util.hasText(wsmodel.getPosentrymode()) && wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_CVV_POSSIBLE))
                        && !cardControlConfig.getChipPinEnabled())
                    ||
                    (Util.hasText(wsmodel.getCardpindata()) && !cardControlConfig.getChipPinEnabled())) {
                    logger.error("Transaction decline by ChpPinEnabled card control, rejecting ...");
                    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
                    wsmodel.setDeclinedbycardctrl("ChipPin");
                    //=====================================================================================================================
                    return false;
                }

                //magstrip flag
                if ((Util.hasText(wsmodel.getPosentrymode())
                        &&
                        (wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_FAIL_MST_READ)
                            || wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.MST_READ_CVV_NOT_POSSIBLE)
                            || wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.MST_READ_CVV_POSSIBLE)))
                        && !cardControlConfig.getMagStripeEnabled()) {
                    logger.error("Transaction decline by MagStripeEnabled card control, rejecting ...");
                    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
                    wsmodel.setDeclinedbycardctrl("MagStripe");
                    //=====================================================================================================================
                    return false;
                }

                //Cash Withdrawal flag
                if (wsmodel.getServicename().toLowerCase().equals("cashwithdrawal") && !cardControlConfig.getCashWithdrawalEnabled()) {
                    logger.error("Transaction decline by CashWithdrawalEnabled card control, rejecting ...");
                    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
                    wsmodel.setDeclinedbycardctrl("CashWithdrawal");
                    //=====================================================================================================================
                    return false;
                }

                //NFC flag
                if ((Util.hasText(wsmodel.getPosentrymode()) && wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_CONTACTLESS))
                        && !cardControlConfig.getNFCEnabled()) {
                    logger.error("Transaction decline by NFCEnabled card control, rejecting ...");
                    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
                    wsmodel.setDeclinedbycardctrl("NFC");
                    //=====================================================================================================================
                    return false;
                }

                //Online flag
                if ((Util.hasText(wsmodel.getPosentrymode()) && (wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.MANUAL_ENTRY)
                        //Arsalan Akhter, Date: 18-Oct-2021, Ticket: VC-NAP-202110181(Online card control not working on production)
                        || wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.CREDENTIALS_ON_FILE)))
                        //==========================================================================================================
                        && !cardControlConfig.getOnlineEnabled()) {
                    logger.error("Transaction decline by OnlineEnabled card control, rejecting ...");
                    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
                    wsmodel.setDeclinedbycardctrl("Online");
                    //=====================================================================================================================
                    return false;
                }

                //International flag
                if ((Util.hasText(wsmodel.getBranchcode()) && (wsmodel.getChannelid().equals(ChannelCodes.EURONET) && (wsmodel.getBranchcode().equals("ATM") || wsmodel.getBranchcode().equals("PSI") || wsmodel.getBranchcode().equals("ESI"))))
                        && !cardControlConfig.getInternationalTxnsEnabled()) {
                    logger.error("Transaction decline by InternationalTxnsEnabled card control, rejecting ...");
                    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
                    wsmodel.setDeclinedbycardctrl("International");
                    //=====================================================================================================================
                    return false;
                }

                logger.info("Transaction is allowed on Card controls, proceeding ...");
                return true;
            }
            else
            {
                logger.error("Card Controls not found for Card Number [" + wsmodel.getCardnumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND); //NP_6005 - refer to Document
                return false;
            }
        }
        catch (Exception e)
        {
            WebServiceUtil.getStrException(e);
            logger.error("Exception caught while checking card controls!");
            return false;
        }
    }

	//m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
    public static boolean UpdateCardLimits(WalletCMSWsEntity wsmodel, CMSAccount account, CMSCard card)
    {
        try {
            String query = "from " + CMSDebitRemainingLimit.class.getName() + " c where " +
                    " c.relation = :RELATION " +
                    " or c.relation = :CARD_NO ";
            Map<String, Object> params = new HashMap<>();
            params.put("RELATION", account.getAccountNumber());
            params.put("CARD_NO", card.getCardNumber());
            List<CMSDebitRemainingLimit> limitList = GeneralDao.Instance.find(query, params);
            if (limitList != null && limitList.size() > 0) {
                logger.info("Limits found against account number [" + account.getAccountNumber() + "], updating limits ...");

                Long inAmount;
                for (CMSDebitRemainingLimit limit : limitList) {
                    inAmount = 0L;
                    if (Util.hasText(limit.getLimitId().getDescription()) && limit.getLimitId().getDescription().equals("GlobalCardLimit")
                            && Util.hasText(wsmodel.getGlobalcardlimit())

                            //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                            //adding check for custom limit flag
                            && (Util.hasText(wsmodel.getCustomlimitflag()) && wsmodel.getCustomlimitflag().equals("true"))
                            ////////////////////////////////////////////////////////////////////////////////////////////////

                            ) {
                        logger.info("Global Card Limit received, updating existing limit ...");
                        inAmount = Long.parseLong(wsmodel.getGlobalcardlimit());

                    } else if (Util.hasText(limit.getLimitId().getDescription()) && limit.getLimitId().getDescription().equals("CashWithdrawalLimit")
                            && Util.hasText(wsmodel.getCashwithdrawallimit())

                            //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                            //adding check for custom limit flag
                            && (Util.hasText(wsmodel.getCustomlimitflag()) && wsmodel.getCustomlimitflag().equals("true"))
                            ////////////////////////////////////////////////////////////////////////////////////////////////

                            ) {
                        logger.info("Cash Withdrawal Limit received, updating existing limit ...");
                        inAmount = Long.parseLong(wsmodel.getCashwithdrawallimit());

                    } else if (Util.hasText(limit.getLimitId().getDescription()) && limit.getLimitId().getDescription().equals("PurchaseLimit")
                            && Util.hasText(wsmodel.getPurchaselimit())

                            //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                            //adding check for custom limit flag
                            && (Util.hasText(wsmodel.getCustomlimitflag()) && wsmodel.getCustomlimitflag().equals("true"))
                            ////////////////////////////////////////////////////////////////////////////////////////////////

                            ) {
                        logger.info("Purchase Limit received, updating existing limit ...");
                        inAmount = Long.parseLong(wsmodel.getPurchaselimit());

                    } else if (Util.hasText(limit.getLimitId().getDescription()) && limit.getLimitId().getDescription().equals("ECommerce")
                            && Util.hasText(wsmodel.getPurchaselimit())

                            //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                            //adding check for custom limit flag
                            && (Util.hasText(wsmodel.getCustomlimitflag()) && wsmodel.getCustomlimitflag().equals("true"))
                            ////////////////////////////////////////////////////////////////////////////////////////////////

                            ) {
                        logger.info("Online Transaction Limit received, updating existing limit ...");
                        inAmount = Long.parseLong(wsmodel.getOnlinetxnlimit());
                    }

                    //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                    //adding check for custom limit flag
                    else {
                        inAmount = Long.parseLong(limit.getLimitId().getAmount());
                    }

                    //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                    //adding below commenting chunk here
                    logger.info("Calculating limit amount ...");

                    Long remAmount = Long.parseLong(limit.getRemainingAmount());
                    Long prodLimitAmount = Long.parseLong(limit.getLimitId().getAmount());
                    Long updatedAmount = 0L;
                    String currentDate = DateTime.now().getDayDate().getDate().toString();
                    String cycleEndDate = limit.getCycleEndDate();
                    /////////////////////////////////////////////////////////////////////////////////////////////////////


                    if (inAmount > 0L

                            //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                            //adding check for custom limit flag
                            && (Util.hasText(wsmodel.getCustomlimitflag()) && wsmodel.getCustomlimitflag().equals("true"))
                            ////////////////////////////////////////////////////////////////////////////////////////////////

                            ) {

                        //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                        //commenting below
                        /*
                        logger.info("Calculating limit amount ...");

                        Long remAmount = Long.parseLong(limit.getRemainingAmount());
                        Long prodLimitAmount = Long.parseLong(limit.getLimitId().getAmount());
                        Long updatedAmount = 0L;

                        if (inAmount < (prodLimitAmount - remAmount)) {
                            logger.error("Requested limit is less than consumed limit, rejecting ...");
                            wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);     //TODO: need to update response code
                            return false;
                        }

                        updatedAmount = inAmount - (prodLimitAmount - remAmount);
                        */

                        if ((limit.getLimitId().getCycleLengthType().equals(CMSLimitCycleType.DAILY)
                                && !Util.hasText(cycleEndDate) || !cycleEndDate.equals(currentDate))
                            ||
                                (limit.getLimitId().getCycleLengthType().equals(CMSLimitCycleType.MONTHLY)
                                        && !Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate)))
                            ||
                                (limit.getLimitId().getCycleLengthType().equals(CMSLimitCycleType.YEARLY)
                                        && !Util.hasText(cycleEndDate) || (Long.parseLong(cycleEndDate) < Long.parseLong(currentDate)))) {
                            updatedAmount = inAmount;
                        } else {
                            updatedAmount = prodLimitAmount - remAmount;
                            logger.info("updatedAmount amount [" + updatedAmount.toString() + "]");
                            logger.info("prodLimitAmount amount [" + prodLimitAmount.toString() + "]");
                            logger.info("remAmount amount [" + remAmount.toString() + "]");

                            if (inAmount < updatedAmount) {
                                logger.error("Requested limit is less than consumed limit, rejecting ...");
                                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);     //TODO: need to update response code
                                return false;
                            }

                            updatedAmount = inAmount - updatedAmount;
                            logger.info("updatedAmount amount [" + updatedAmount.toString() + "]");
                            logger.info("prodLimitAmount amount [" + prodLimitAmount.toString() + "]");
                            logger.info("remAmount amount [" + remAmount.toString() + "]");
                        }

                        logger.info("Updated limit amount [" + updatedAmount.toString() + "]");
                        ////////////////////////////////////////////////////////////////////////////////////////////////

                        CMSSharedIndividualLimit individualLimit = new CMSSharedIndividualLimit();
                        individualLimit.setAmount(inAmount.toString());
                        individualLimit.setChannelId(limit.getLimitId().getChannelId());
                        individualLimit.setCmsProdDebitLimit(limit.getLimitId());
                        individualLimit.setCycleLength(limit.getLimitId().getCycleLength());
                        individualLimit.setCycleLengthType(limit.getLimitId().getCycleLengthType());
                        individualLimit.setFrequencyLength(limit.getLimitId().getFrequencyLength());
                        individualLimit.setFrequencyLengthType(limit.getLimitId().getFrequencyLengthType());
                        individualLimit.setFrequencyType(limit.getLimitId().getFrequencyType());
                        individualLimit.setLimitCategory("2");      //1-Shared, 2-Individual
                        individualLimit.setLimitType(limit.getLimitId().getLimitType());
                        individualLimit.setProductId(limit.getLimitId().getProductId());
                        individualLimit.setTransactionType(limit.getLimitId().getTransactionType());
                        GeneralDao.Instance.save(individualLimit);

                        limit.setRemainingAmount(updatedAmount.toString());
                        limit.setIsIndividual("1");
                        limit.setIndividualLimitId(individualLimit);
                        GeneralDao.Instance.saveOrUpdate(limit);

                        logger.info("Limits updated successfully ...");

                    } else {
                        logger.info("No limit found for update ...");
                        //m.rehman: VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
                        //adding check for custom limit flag
                        if (Util.hasText(limit.getLimitId().getDescription()) && limit.getLimitId().getDescription().equals("GlobalCardLimit")) {
                            logger.info("Custom limit flag is false, restoring default limit for Global limit only ...");
                            logger.info("Limit amount [" + prodLimitAmount + "]");
                            limit.setRemainingAmount(prodLimitAmount.toString());
                            limit.setIsIndividual("0");
                            limit.setIndividualLimitId(null);
                            GeneralDao.Instance.saveOrUpdate(limit);
                        }
                        ////////////////////////////////////////////////////////////////////////////////////////////////
                    }
                }
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return true;
            } else {
                logger.error("Limits not found against account number [" + account.getAccountNumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_DATA_NOT_FOUND);
                return false;
            }
        } catch (Exception e)
        {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while updating card controls!");
            return false;
        }
    }

    //m.rehman: 15-09-2021, VP-NAP-202109092 / VG-NAP-202109101 - Non financial Transactions on VaulGuard
    //to avoid duplicate account number generation in craete wallet on transaction load
    public static String getSequenceNextValue(String seqName){
        String query = "select " + seqName + ".nextval from dual";
        List<BigDecimal> result = GeneralDao.Instance.executeSqlQuery(query);
        return result.get(0).toString();
    }

    public static String GenerateWalletAccountNumberFromSeq(CMSProduct product, String seqName)
    {
        CardNumberGenerator obj_CNGen = new CardNumberGenerator();
        Integer panSequenceLength = 16 - (product.getBin().length() + 1);
        String strpanSequence = WalletCMSFunctions.getSequenceNextValue(seqName);
        logger.info("Account Seq [" + strpanSequence + "]");
        strpanSequence = StringUtils.leftPad(strpanSequence, panSequenceLength, "0");
        return obj_CNGen.generate(product.getBin(), strpanSequence);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
