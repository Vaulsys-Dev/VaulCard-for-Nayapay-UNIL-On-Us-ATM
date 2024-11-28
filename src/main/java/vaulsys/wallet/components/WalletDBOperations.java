package vaulsys.wallet.components;


import vaulsys.authorization.exception.card.CardNotFoundException;
import vaulsys.authorization.exception.onlineBillPayment.ExpireDateException;
import vaulsys.calendar.DateTime;
import vaulsys.cms.base.CMSCardLimit;
import vaulsys.cms.base.CMSLimit;
import vaulsys.cms.base.CMSLimitType;
import vaulsys.cms.components.CMSDBOperations;
import vaulsys.cms.exception.*;
import vaulsys.config.IMDType;
import vaulsys.customer.Currency;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wallet.base.WalletAccount;
import vaulsys.wallet.base.WalletCardRelation;
import vaulsys.wallet.exception.WalletAccountNotFoundException;
import vaulsys.wallet.exception.WalletAmountExceededException;
import vaulsys.wallet.exception.WalletCardNotFoundException;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 24-Apr-17.
 */
public class WalletDBOperations {

	private static Logger logger = Logger.getLogger(WalletDBOperations.class);

    public static final WalletDBOperations Instance = new WalletDBOperations();
	
	private WalletDBOperations()
    {}

    public int ValidateWalletByPan(Ifx ifx) //Note: 0:OFFUS 1:ONUS-Success -1:Failure
    {
        try {
             if(!CMSDBOperations.ValidateExpiry(ifx)) //Check Expiry
             {
                 logger.info("Expired Card");
                 throw new ExpireDateException();
             }

            String CardRelId = ifx.getTrk2EquivData();

            if (CardRelId != null) {
                int index = CardRelId.indexOf('=');
                if (index == -1) {
                    index = CardRelId.indexOf('D');
                    CardRelId = CardRelId.replace('D', '=');
                }

                CardRelId = CardRelId.substring(0, index) + CardRelId.substring(index, index + 5);
            }
            else
            {
                CardRelId = ifx.getAppPAN() + "=" + ifx.getExpDt(); //Make Card_Relation_ID with PAN & Expiry
            }

            String Channelid = ifx.getTransaction().getInputMessage().getChannel().getChannelId();

            if (CMSDBOperations.getIMDType(CardRelId.substring(0, 11)).equals(IMDType.Wallet)) { //For Wallet Card
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("cardrelationid", CardRelId);
                param.put("isdefault", "1");
                param.put("channel", Channelid);
                String query = "from " + WalletCardRelation.class.getName() + " cr " +
                        " where "
                        + " cr.card_relid = :cardrelationid "
                        + "and cr.channel = :channel "
                        + "and cr.isdefault = :isdefault";


                //try {
                WalletCardRelation walletCardRelation = (WalletCardRelation) GeneralDao.Instance.findObject(query, param);

                if (walletCardRelation == null) {
                    throw new WalletCardNotFoundException();
                } else if (walletCardRelation.getAccount() == null) {
                    throw new WalletAccountNotFoundException();
                } else if (walletCardRelation.getCustomer() == null) {
                    throw new CustomerNotFoundException();
                } else {
                    //Check Card Status
                    String cstatus = walletCardRelation.getCardAuth().getStatus();
                    if (!cstatus.equals("00")) //palce it in like ISOResponseCodes
                    {
                        logger.info("Card found with Negative Status [" + WalletStatusCodes.CardStausMap.get(cstatus) + "]");
                        throw new CardNotFoundException();
                    } else {
                        logger.info("Card found with Status [" + WalletStatusCodes.CardStausMap.get(cstatus) + "]"); //Raza TEMP
                        String astatus = walletCardRelation.getAccount().getStatus();
                        if (!astatus.equals("00")) //Check Account Status
                        {
                            logger.info("Card found with Negative Status [" + WalletStatusCodes.AcctStausMap.get(astatus) + "]");
                            throw new AccountNotFoundException();
                        } else {
                            logger.info("Account found with Status [" + WalletStatusCodes.AcctStausMap.get(astatus) + "]"); //Raza TEMP
                            String csstatus = walletCardRelation.getCustomer().getStatus();

                            if (!csstatus.equals("00")) //Check Customer Status
                            {
                                logger.info("Customer found with Negative Status [" + WalletStatusCodes.CustStausMap.get(csstatus) + "]");
                                throw new CustomerNotFoundException();
                            } else {
                                logger.info("Customer found with Status [" + WalletStatusCodes.CustStausMap.get(csstatus) + "]"); //Raza TEMP
                                System.out.println("Customer Validated OK!");
                                ifx.setWalletCardRelation(walletCardRelation);
                            }
                        }
                    }

                    System.out.println("Wallet Card [" + walletCardRelation.getCard().getCardNumber() + "]"); //Raza TEMP
                    System.out.println("Wallet Account [" + walletCardRelation.getAccount().getWalletNumber() + "]"); //Raza TEMP
                    System.out.println("Wallet Customer [" + walletCardRelation.getCustomer().getFirstname() + "]"); //Raza TEMP
                    System.out.println("Wallet PIN Offset [" + walletCardRelation.getCardAuth().getEncryptedPin() + "]");

                    //Check Txn Permission
                    if (walletCardRelation.getTxn_perm() != null) {
                        if (walletCardRelation.getTxn_perm().contains(ifx.getTrnType().toString())) {
                            System.out.println("Access Granted");
                        } else {
                            System.out.println("Txn Not Allowed");
                            throw new TransactionNotAllowedException();
                            //throw TxnNotAllowed Exception
                        }
                    } else {
                        logger.info("No Permission Defined for Card and Channel");
                    }

                    return 1;
                }
            } else {
                logger.info("Offus Card");
                return 0; //return true for OFFUS card
            }
            //CARDRELATIONID
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public int ValidateWalletByOtherInfo(Ifx ifx) {
        try {

            String CardRelId = ifx.getAppPAN();

            String Channelid = ifx.getTransaction().getInputMessage().getChannel().getChannelId();

            String defaultValue = "1";

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("cardrelationid", CardRelId);
            param.put("channelid", Channelid);
            param.put("defaultvalue", defaultValue);
            String query = "from " + WalletCardRelation.class.getName() + " cr " +
                    "where "
                    + "cr.card_relid = :cardrelationid "
                    + "and cr.channel = :channelid "
                    + "and cr.isdefault = :defaultvalue";

            WalletCardRelation walletCardRelation = (WalletCardRelation) GeneralDao.Instance.findObject(query, param);

            if (walletCardRelation == null) {
                throw new WalletCardNotFoundException();
            } else if (walletCardRelation.getAccount() == null) {
                throw new WalletAccountNotFoundException();
            } else if (walletCardRelation.getCustomer() == null) {
                throw new CustomerNotFoundException();
            } else {
                //Check Card Status
                String cstatus = walletCardRelation.getCardAuth().getStatus();
                if (!cstatus.equals("00")) //place it in like ISOResponseCodes
                {
                    logger.info("Wallet Card found with Negative Status [" + WalletStatusCodes.CardStausMap.get(cstatus) + "]");
                    throw new WalletCardNotFoundException();
                } else {
                    logger.info("Wallet Card found with Status [" + WalletStatusCodes.CardStausMap.get(cstatus) + "]"); //Raza TEMP
                    String astatus = walletCardRelation.getAccount().getStatus();
                    if (!astatus.equals("00")) //Check Account Status
                    {
                        logger.info("Wallet Card found with Negative Status [" + WalletStatusCodes.AcctStausMap.get(astatus) + "]");
                        throw new WalletAccountNotFoundException();
                    } else {
                        logger.info("Wallet Account found with Status [" + WalletStatusCodes.AcctStausMap.get(astatus) + "]"); //Raza TEMP
                        String csstatus = walletCardRelation.getCustomer().getStatus();

                        if (!csstatus.equals("00")) //Check Customer Status
                        {
                            logger.info("Wallet Customer found with Negative Status [" + WalletStatusCodes.CustStausMap.get(csstatus) + "]");
                            throw new CustomerNotFoundException();
                        } else {
                            logger.info("Wallet Customer found with Status [" + WalletStatusCodes.CustStausMap.get(csstatus) + "]"); //Raza TEMP
                            System.out.println("Wallet Customer Validated OK!");
                            ifx.setWalletCardRelation(walletCardRelation);
                        }
                    }
                }

                //System.out.println("Wallet Card [" + walletCardRelation.getCard().getAcctNumber() + "]"); //Raza TEMP
                System.out.println("Wallet Account [" + walletCardRelation.getAccount().getWalletNumber() + "]"); //Raza TEMP
                System.out.println("Wallet Customer [" + walletCardRelation.getCustomer().getFirstname() + "]"); //Raza TEMP
                System.out.println("Wallet PIN Offset [" + walletCardRelation.getCardAuth().getEncryptedPin() + "]");

                //Check Txn Permission
                if (walletCardRelation.getTxn_perm() != null) {
                    if (walletCardRelation.getTxn_perm().contains(ifx.getTrnType().toString())) {
                        System.out.println("Access Granted");
                    } else {
                        System.out.println("Txn Not Allowed");
                        throw new TransactionNotAllowedException();
                    }
                } else {
                    logger.info("No Permission Defined for Wallet Card and Channel");
                }

                return 1;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Following routine is used to check limit both apply and reverse.
     * isReverse parameter defines the operation of the routine.
     * if isReverse is false, routine will deduct the limit, else reverse the limit.
     * @param processContext
     * @param isReverse
     * @throws Exception
     */
    public void CheckLimit(ProcessContext processContext, Boolean isReverse) throws Exception {
        String productId, query, trnType, channelId, cardNumber;
        DateTime currentDate, cardCycleLimitDate;
        List<CMSLimit> limitFromDb;
        CMSLimit limit;
        List<CMSCardLimit> cardLimitFromDb;
        CMSCardLimit cardLimit;
        Map<String, Object> dbParam;
        Ifx ifx;
        Long amount, cardLimitAmount;
        Integer frequency;

        try {
            ifx = processContext.getInputMessage().getIfx();
            dbParam = new HashMap<String, Object>();

            productId = ifx.getWalletCardRelation().getProductId();

            trnType = Integer.toString(ifx.getTrnType().getType());

            //if it is a request/reversal request message, get channel from input message
            //else get channel from output message
            channelId = null;
            if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ||
                    ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
                channelId = processContext.getChannel(processContext.getInputMessage().getChannelName()).getChannelId();
            } else if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
                channelId = processContext.getChannel(processContext.getTransaction().getFirstTransaction().getInputMessage().getChannelName()).getChannelId();
            }

            dbParam.put("productId", productId);
            dbParam.put("channelId", channelId);
            dbParam.put("trnType", trnType);
            query = "from " + CMSLimit.class.getName() + " l "
                    + "where l.productId = :productId "
                    + "and "
                    + "l.channelId = :channelId "
                    + "and "
                    + "l.transactionType = :trnType";
            limitFromDb = GeneralDao.Instance.find(query, dbParam);

            if (limitFromDb.isEmpty()) {
                logger.error("Limit not found");
                ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
                throw new LimitNotFoundException("Limit not found");

            } else if (limitFromDb.size() > 1) {
                logger.error("Multiple Limits found");
                ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
                throw new LimitNotFoundException("Multiple Limits found");

            } else {
                limit = limitFromDb.get(0);
            }

            if (ifx.getSec_Amt() != null)
                amount = ifx.getSec_Amt();
            else
                amount = ifx.getAuth_Amt();

            //checking card/account/customer limit, according to limit type
            cardNumber = null;
            if (limit.getLimitType().equals(CMSLimitType.CARD_LIMIT)) {
                logger.info("Checking Card Limit");
                cardNumber = ifx.getAppPAN();

            } else if (limit.getLimitType().equals(CMSLimitType.ACCOUNT_LIMIT)) {
                logger.info("Checking Account Limit");
                cardNumber = ifx.getWalletCardRelation().getAccount().getWalletNumber();

            } else if (limit.getLimitType().equals(CMSLimitType.CUSTOMER_LIMIT)) {
                logger.info("Checking Customer Limit");
                cardNumber = ifx.getWalletCardRelation().getCustomer().getCustomerId();

            }

            dbParam = new HashMap<String, Object>();

            dbParam.put("limitId", limit.getId());
            dbParam.put("cardNumber", cardNumber);
            query = "from " + CMSCardLimit.class.getName() + " cl "
                    + "where cl.limitId = :limitId "
                    + "and "
                    + "cl.cardNumber = :cardNumber";
            cardLimitFromDb = GeneralDao.Instance.find(query, dbParam);

            if (cardLimitFromDb.isEmpty()) {
                logger.error("Card/Account/Customer Limit not found");
                ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
                throw new LimitNotFoundException("Card/Account/Customer Limit not found");

            } else if (cardLimitFromDb.size() > 1) {
                logger.error("Multiple Card/Account/Customer Limits found");
                ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
                throw new LimitNotFoundException("Multiple Card/Account/Customer Limits found");

            } else {
                cardLimit = cardLimitFromDb.get(0);
            }

            currentDate = DateTime.now();
            cardCycleLimitDate = new DateTime(MyDateFormatNew.parse("yyyyMMdd", cardLimit.getCycleEndDate()));

            //check if its a first transaction of the day
            //update cardLimit object with new values
            if (currentDate.getDayDate().compareTo(cardCycleLimitDate.getDayDate()) >= 1) {
                cardLimit.setCycleStartDate(currentDate.getDayDate().getDate().toString());
                currentDate.increase((Integer.parseInt(limit.getCycleLength()) - 1) * 60 * 24);
                cardLimit.setCycleEndDate(currentDate.getDayDate().getDate().toString());
                cardLimit.setRemainingAmount(limit.getAmount());
                cardLimit.setRemainingFrequency(limit.getFrequencyLength());
            }
            cardLimitAmount = Long.parseLong(cardLimit.getRemainingAmount());
            frequency = Integer.parseInt(cardLimit.getRemainingFrequency());

            if (isReverse.equals(Boolean.FALSE)) {

                if (amount > cardLimitAmount) {
                    logger.error("Amount is greater than available limit. Error!!!");
                    ifx.setRsCode(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE);
                    throw new LimitExceededException("Amount is greater than available limit. Error!!!");

                } else {
                    cardLimitAmount -= amount;
                }

                if (frequency <= 0) {
                    logger.error("Transaction Frequency Exceeded. Error!!!");
                    ifx.setRsCode(ISOResponseCodes.CASH_TRANSACTION_NOT_ALLOWED);
                    throw new LimitExceededException("Transaction Frequency Exceeded. Error!!!");

                } else {
                    frequency--;
                }
            } else {
                //reversing amount and frequency
                //if cycle limit is available, reverse limit, else do nothing
                if (currentDate.getDayDate().compareTo(cardCycleLimitDate.getDayDate()) <= 1) {
                    cardLimitAmount += amount;
                    frequency++;
                }
            }
            cardLimit.setRemainingAmount(Long.toString(cardLimitAmount));
            cardLimit.setRemainingFrequency(frequency.toString());
            ifx.setCardLimit(cardLimit);
            GeneralDao.Instance.saveOrUpdate(cardLimit);

        } catch(Exception e) {
            logger.error("Unable to Validate Limit. Error!!!");
            throw e;
        }
    }

    public void ReverseCardLimit(ProcessContext processContext) throws Exception {

        try {

            Ifx ifx;
            int retVal;
            Boolean isOnUsCard;

            ifx = processContext.getInputMessage().getIfx();

            if (ifx != null) {

                //if it is a request, then check if card limit object is available, omit it
                if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
                    if (ifx.getCardLimit() != null)
                        GeneralDao.Instance.evict(ifx.getCardLimit());
                }
                //if it is a response/reversal request and card limit not reverse, reverse the limit
                else if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType()) ||
                        ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {

                    if (ifx.getRsCode().equals(ISOResponseCodes.APPROVED) ||
                            (!ifx.getRsCode().equals(ISOResponseCodes.APPROVED) && ifx.getCardLimit() == null)) {

                        //Validate Card, Account, Customer and Permission
                        if (ifx.getWalletCardRelation() == null) {
                            if (Util.hasText(ifx.getAddDataPrivate()) &&
                                    ifx.getAddDataPrivate().substring(0,1).equals("W"))
                                retVal = WalletDBOperations.Instance.ValidateWalletByOtherInfo(ifx);
                            else
                                retVal = WalletDBOperations.Instance.ValidateWalletByPan(ifx);

                            if (retVal > 0) {
                                isOnUsCard = Boolean.TRUE;
                            } else if (retVal == 0) {
                                logger.info("Off-Us card, forwarding transaction");
                                isOnUsCard = Boolean.FALSE;
                            } else
                                throw new WalletCardNotFoundException("Card relationship not found. Error!!!");
                        } else {
                            isOnUsCard = Boolean.TRUE;
                        }

                        if (isOnUsCard.equals(Boolean.TRUE)) {
                            logger.info("On-Us card, reversing limit");
                            CheckLimit(processContext, Boolean.TRUE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void CheckWalletAmount(ProcessContext processContext, Boolean isReverse) throws Exception {
        try {
            Ifx ifx;
            WalletAccount walletAccount;
            Long transactionAmount, walletAmount;
            AcctBal availableBal, actualBal;

            ifx = processContext.getInputMessage().getIfx();
            walletAccount = ifx.getWalletCardRelation().getAccount();
            transactionAmount = ifx.getAuth_Amt();
            walletAmount = Long.parseLong(walletAccount.getAvailableBalance());
            availableBal = new AcctBal();
            actualBal = new AcctBal();

            if (isReverse.equals(Boolean.FALSE)) {
                if (transactionAmount > walletAmount) {
                    logger.error("Transaction Amount Exceeded. Error!!!");
                    ifx.setRsCode(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE);
                    throw new WalletAmountExceededException("Transaction Amount Exceeded. Error!!!");

                } else {
                    walletAmount = walletAmount - transactionAmount;
                    logger.info("Transaction Amount Deducted Successfully!!!");
                }
            } else {
                walletAmount = walletAmount + transactionAmount;
                logger.info("Transaction Amount reversed Successfully!!!");
            }

            walletAccount.setAvailableBalance(walletAmount.toString());
            walletAccount.setActualBalance(walletAmount.toString());

            availableBal.setAcctType(AccType.CURRENT);
            availableBal.setAmt(walletAccount.getAvailableBalance());
            availableBal.setBalType(BalType.AVAIL);
            availableBal.setCurCode(walletAccount.getCurrency());
            ifx.setAcctBalAvailable(availableBal);

            actualBal.setAcctType(AccType.CURRENT);
            actualBal.setAmt(walletAccount.getActualBalance());
            actualBal.setBalType(BalType.LEDGER);
            actualBal.setCurCode(walletAccount.getCurrency());
            ifx.setAcctBalAvailable(actualBal);

            GeneralDao.Instance.saveOrUpdate(walletAccount);

        } catch (Exception e) {
            logger.error("Unable to deduct wallet amount");
            throw e;
        }
    }

    public void TopupWalletAmount(Ifx ifx, Boolean isReverse) throws Exception {
        try {
            //Ifx ifx;
            WalletAccount walletAccount;
            Long transactionAmount, walletAmount;
            AcctBal availableBal, actualBal;
            DateFormat dateFormat;
            Date date;
            Currency currency;

            //ifx = processContext.getInputMessage().getIfx();
            walletAccount = ifx.getWalletCardRelation().getAccount();
            //currency = ProcessContext.get().getCurrency(Integer.parseInt(walletAccount.getCurrency()));
            transactionAmount = ifx.getAuth_Amt();
            //transactionAmount = transactionAmount * (long)Math.pow(10, currency.getDecimalPosition());

            if (!Util.hasText(walletAccount.getAvailableBalance()))
                walletAmount = 0L;
            else
                walletAmount = Long.parseLong(walletAccount.getAvailableBalance());

            availableBal = new AcctBal();
            actualBal = new AcctBal();
            dateFormat = new SimpleDateFormat("dd-MMM-yy");
            date = new Date();

            if (isReverse.equals(Boolean.FALSE)) {
                walletAmount = walletAmount + transactionAmount;
                logger.info("Wallet Amount Topup Successfully!!!");

            } else {
                if (transactionAmount > walletAmount) {
                    logger.error("Transaction Amount Exceeded. Error!!!");
                    ifx.setRsCode(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE);
                    throw new WalletAmountExceededException("Transaction Amount Exceeded. Error!!!");

                } else {
                    walletAmount = walletAmount - transactionAmount;
                    logger.info("Wallet Amount reversed Successfully!!!");
                }
            }

            walletAccount.setAvailableBalance(walletAmount.toString());
            walletAccount.setActualBalance(walletAmount.toString());
            walletAccount.setLastUpdateDate(dateFormat.format(date));

            availableBal.setAcctType(AccType.CURRENT);
            availableBal.setAmt(walletAccount.getAvailableBalance());
            availableBal.setBalType(BalType.AVAIL);
            availableBal.setCurCode(walletAccount.getCurrency());
            ifx.setAcctBalAvailable(availableBal);

            actualBal.setAcctType(AccType.CURRENT);
            actualBal.setAmt(walletAccount.getActualBalance());
            actualBal.setBalType(BalType.LEDGER);
            actualBal.setCurCode(walletAccount.getCurrency());
            ifx.setAcctBalAvailable(actualBal);

            GeneralDao.Instance.saveOrUpdate(walletAccount);

        } catch (Exception e) {
            logger.error("Unable to topup wallet amount");
            throw e;
        }
    }
}
