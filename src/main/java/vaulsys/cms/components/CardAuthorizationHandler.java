package vaulsys.cms.components;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.calendar.DateTime;
import vaulsys.cms.base.*;
import vaulsys.cms.exception.CardNotFoundException;
import vaulsys.cms.exception.CardValidationException;
import vaulsys.cms.exception.LimitExceededException;
import vaulsys.cms.exception.LimitNotFoundException;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.hsm.HardwareSecurityModule;
import vaulsys.util.MyDateFormatNew;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by HP on 24-Apr-17.
 */
public class CardAuthorizationHandler extends BaseHandler {

    private static Logger logger = Logger.getLogger(CardAuthorizationHandler.class);

    public static final CardAuthorizationHandler Instance = new CardAuthorizationHandler();

    private CardAuthorizationHandler()
    {
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        try {
//            AuthorizationComponent auth = new AuthorizationComponent();
//            auth.setProcessContext(processContext);
            //AuthorizationComponent.authorize(processContext);
            //if()
            //{
            Ifx ifx = processContext.getInputMessage().getIfx();
            if (ifx != null) {
                    int iretval = CMSDBOperations.ValidateCardbyPan(ifx);
                    if (iretval > 0) //Validate Card, Account, Customer and Permission
                    {
                        logger.info("Card Validated Successfully");
                        //Now validate PIN
                    if (ISOMessageTypes.isRequestMessage(ifx.getMti())) {

                            //following operations will be performed after customer validation
                            //if transaction is pin change, perform pin change and return
                            if (ifx.getIfxType().equals(IfxType.CHANGE_PIN_BLOCK_RQ)) {
                                HardwareSecurityModule.getInstance().PINChange(processContext);
                                return;
                            }

                            //setting auth flags
                            SetAuthorizationFlags(processContext);

                            //validating card information from HSM, if required
                            if (ifx.getCardAuthFlags().getAuthRequiredFlag()) {

                            HardwareSecurityModule.getInstance().ValidateOnUsCardInfo(processContext);

                            } else {
                                logger.info("No HSM Authorization is required, moving forward");
                            }

                            //TODO: if transaction is PIN Validation, no need to check limit, return
                            //m.rehman: separating BI from financial incase of limit
                            //validating transaction limit
                            if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType(), true)) {
                                logger.info("Deducting Limit for Financial Transaction");
                                CheckLimit(processContext, Boolean.FALSE);
                            }

//                    } else if (ISOMessageTypes.isResponseMessage(ifx.getIfxType())) { //Raza commenting Handled Response from MainProcess
//
//                        if (!ifx.getRsCode().equals(ISOResponseCodes.APPROVED)) {
//                            //if financial transaction, reverse the limit
//                            if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType())) {
//                                logger.info("Reversing Limit for Financial Transaction");
//                                CheckLimit(processContext, Boolean.TRUE);
//                            }
//                        }
                    } else if (ISOMessageTypes.isReversalRqMessage(ifx.getIfxType())) {
                        //m.rehman: separating BI from financial incase of limit
                        //if financial transaction, reverse the limit
                        if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType(), true)) {
                            logger.info("Reversing Limit for Reversal Financial Transaction");
                            CheckLimit(processContext, Boolean.TRUE);
                        }
                    }
                } else if (iretval == 0) {
                    //Off-Us transaction flow
                    logger.info("OFFUS Card Flow");
                    //Translate PIN
                    if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {

                        //setting auth flags
                        SetAuthorizationFlags(processContext);

                        if (ifx.getCardAuthFlags().getAuthRequiredFlag()) {
                            HardwareSecurityModule.getInstance().ValidateOffUsCardInfo(processContext);
                        }
                    }
                } else {
                        logger.error("Card Validation Failed");
                        ifx.setRsCode(ISOResponseCodes.LIMIT_EXCEEDED); //Raza verify this return code
                        throw new CardValidationException();
                }
            } else {
                logger.error("Ifx not found for Card Validation..!");
                throw new Exception(); //Raza Specify Exception
            }
        } catch (Exception e) {
            //if( 	//e instanceof DuplicateBillPaymentMessageException ||
                    //e instanceof FITControlNotAllowedException ||
                    //e instanceof NotPaperReceiptException ||
                    //e instanceof NotRoundAmountException ||
                    //e instanceof PanPrefixServiceNotAllowedException ||
                    //e instanceof MandatoryFieldException ||
                    //e instanceof NotValidBillPaymentMessageException ||
                    //e instanceof TransactionAmountNotAcceptableException ||
                    //e instanceof NotSubsidiaryAccountException ||
                    //e instanceof CardAuthorizerException ||
                    //e instanceof TransactionAmountNotAcceptableException ||
                    //e instanceof ServiceTypeNotAllowedException){
                //Just for exceptions that are not so important....
              //  logger.warn(e);
            //}else{
             //   logger.error(e);
            //}
            e.printStackTrace();
            processContext.getTransaction().getInputMessage().setNeedToBeInstantlyReversed(false);
            throw e;
        }
        return;
    }

    public static void SetAuthorizationFlags (ProcessContext processContext) throws Exception {
        Ifx ifx;
        CMSCardAuthorizationFlags cardAuthorizationFlags;

        ifx = processContext.getInputMessage().getIfx();
        cardAuthorizationFlags = new CMSCardAuthorizationFlags();

        //checking pin validation flags
        cardAuthorizationFlags.checkPINValidationRequired(processContext);

        //checking cvv validation flags
        cardAuthorizationFlags.checkCVVValidationRequired(processContext);

        //checking for cvv2 validation flags
        cardAuthorizationFlags.checkCVV2ValidationRequired(processContext);

        //checking for arqc validation flags
        cardAuthorizationFlags.checkARQCValidationRequired(processContext);

        //checking for arpc generation flags
        cardAuthorizationFlags.checkARPCGenerationRequired(processContext);

        //checking authorization requirement
        cardAuthorizationFlags.checkAuthRequired();

        ifx.setCardAuthFlags(cardAuthorizationFlags);
    }

    /**
     * Following routine is used to check limit both apply and reverse.
     * isReverse parameter defines the operation of the routine.
     * if isReverse is false, routine will deduct the limit, else reverse the limit.
     * @param processContext
     * @param isReverse
     * @throws Exception
     */
    private void CheckLimit(ProcessContext processContext, Boolean isReverse) throws Exception {
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

            //check transaction is valid for limit deduction/reverse


            dbParam = new HashMap<String, Object>();

            productId = ifx.getCmsCardRelation().getProductId();

            trnType = Integer.toString(ifx.getTrnType().getType());

            //if it is a request/reversal request message, get channel from input message
            //else get channel from output message
            channelId = null;
            if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ||
                    ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
                //channelId = processContext.getInputMessage().getChannelId(); //Raza commenting
                channelId =  processContext.getChannel(processContext.getInputMessage().getChannelName()).getChannelId();
            } else if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
                //channelId = processContext.getOutputMessage().getChannelId(); //Raza commenting
                //channelId = processContext.getChannel(processContext.getOutputMessage().getChannelName()).getChannelId();
                //if (!Util.hasText(channelId)) {
                    //channelId = processContext.getTransaction().getFirstTransaction().getInputMessage().getChannelId(); //Raza commenting
                    channelId = processContext.getChannel(processContext.getTransaction().getFirstTransaction().getInputMessage().getChannelName()).getChannelId();
                //}
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
                cardNumber = ifx.getCmsCardRelation().getAccount().getAccountNumber();

            } else if (limit.getLimitType().equals(CMSLimitType.CUSTOMER_LIMIT)) {
                logger.info("Checking Customer Limit");
                cardNumber = ifx.getCmsCardRelation().getCustomer().getCustomerId();

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
                        if (ifx.getCmsCardRelation() == null) {
                            retVal = CMSDBOperations.ValidateCardbyPan(ifx);
                            if (retVal > 0) {
                                isOnUsCard = Boolean.TRUE;
                            } else if (retVal == 0) {
                                logger.info("Off-Us card, forwarding transaction");
                                isOnUsCard = Boolean.FALSE;
                            } else
                                throw new CardNotFoundException("Card relationship not found. Error!!!");
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
}
