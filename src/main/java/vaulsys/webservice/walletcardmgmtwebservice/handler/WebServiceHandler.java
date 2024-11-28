package vaulsys.webservice.walletcardmgmtwebservice.handler;


import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.WebServiceUtil;
import vaulsys.webservice.walletcardmgmtwebservice.component.WSOperation;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import org.apache.log4j.Logger;

/**
 * Created by RAZA MURTAZA BAIG on 1/28/2018.
 */
public class WebServiceHandler {
    private static final Logger logger = Logger.getLogger(WebServiceHandler.class);

    public static WalletCMSWsEntity processCheckCnic(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            //Call Fraud Here
//            try {
//                FraudManager.Instance.execute(wsmodel);
//            }
//            catch (Exception e)
//            {
//                logger.error("Exception caught while authorizing from Fraud!");
//                wsmodel.setRespcode(ISOResponseCodes.ERROR_GENERALERROR);
//                return wsmodel;
//            }

            return WSOperation.ExecuteCheckCnic(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCreateWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            //Call Fraud Here
//            try {
//                FraudManager.Instance.execute(wsmodel);
//            }
//            catch (Exception e)
//            {
//                logger.error("Exception caught while authorizing from Fraud!");
//                wsmodel.setRespcode(ISOResponseCodes.ERROR_GENERALERROR);
//                return wsmodel;
//            }

            return WSOperation.ExecuteCreateWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCreateWalletPIN(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            //Call Fraud Here
//            try {
//                FraudManager.Instance.execute(wsmodel);
//            }
//            catch (Exception e)
//            {
//                logger.error("Exception caught while authorizing from Fraud!");
//                wsmodel.setRespcode(ISOResponseCodes.ERROR_GENERALERROR);
//                return wsmodel;
//            }

            return WSOperation.ExecuteCreateWalletPIN(wsmodel);
        }
    }

    // Asim Shahzad, Date : 9th March 2021, Tracking ID : VP-NAP-202103112 / VC-NAP-202103112
    public static WalletCMSWsEntity processCreateWalletPINWithSecretQuestions(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCreateWalletPINWithSecretQuestions(wsmodel);
        }
    }
    // =====================================================================================================

    public static WalletCMSWsEntity processDeleteProvisionalWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            //Call Fraud Here
//            try {
//                FraudManager.Instance.execute(wsmodel);
//            }
//            catch (Exception e)
//            {
//                logger.error("Exception caught while authorizing from Fraud!");
//                wsmodel.setRespcode(ISOResponseCodes.ERROR_GENERALERROR);
//                return wsmodel;
//            }

            return WSOperation.ExecuteDeleteProvisionalWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processActivateProvisionalWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            //Call Fraud Here
//            try {
//                FraudManager.Instance.execute(wsmodel);
//            }
//            catch (Exception e)
//            {
//                logger.error("Exception caught while authorizing from Fraud!");
//                wsmodel.setRespcode(ISOResponseCodes.ERROR_GENERALERROR);
//                return wsmodel;
//            }

            return WSOperation.ExecuteActivateProvisionalWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCreateWalletLevelOne(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            //Call Fraud Here
//            try {
//                FraudManager.Instance.execute(wsmodel);
//            }
//            catch (Exception e)
//            {
//                logger.error("Exception caught while authorizing from Fraud!");
//                wsmodel.setRespcode(ISOResponseCodes.ERROR_GENERALERROR);
//                return wsmodel;
//            }

            return WSOperation.ExecuteCreateWalletLevelOne(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCustomerEnableWalletAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCustomerEnableWalletAccountRequest(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateUserProfile(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateUserProfile(wsmodel);
        }
    }

    public static WalletCMSWsEntity processChangeWalletPin(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteChangeWalletPin(wsmodel);
        }
    }

    public static WalletCMSWsEntity processVerifyWalletPin(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteVerifyWalletPin(wsmodel);
        }
    }

    public static WalletCMSWsEntity processDebitCardRequest(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteDebitCardRequest(wsmodel);
        }
    }

    public static WalletCMSWsEntity processEnableDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteEnableDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalEnableDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalEnableDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processChangeDebitCardPin(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteChangeDebitCardPin(wsmodel);
        }
    }

    public static WalletCMSWsEntity processLinkBankAccountInquiry(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteLinkBankAccountInquiry(wsmodel);
        }
    }

    public static WalletCMSWsEntity processLinkBankAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteLinkBankAccount(wsmodel);
        }
    }

    public static WalletCMSWsEntity processLinkBankAccountOTP(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteLinkBankAccountOTP(wsmodel);
        }
    }

    public static WalletCMSWsEntity processVerifyLinkAccountOTP(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteVerifyLinkAccountOTP(wsmodel);
        }
    }

    public static WalletCMSWsEntity processConfirmLinkBankAccountOTP(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteConfirmLinkBankAccountOTP(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUnLinkBankAccountInquiry(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUnLinkBankAccountInquiry(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUnLinkBankAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUnLinkBankAccount(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateLinkedAccountAlias(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateLinkedAccountAlias(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSetPrimaryLinkedAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSetPrimaryLinkedAccount(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserToken(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserToken(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserLinkedAccountList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserLinkedAccountList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserLinkedAccountListWithoutToken(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserLinkedAccountListWithoutToken(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserTransactionforChat(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserTransactionforChat(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalGetUserTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalGetUserTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserTransactionList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserTransactionList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processLoadWalletInquiry(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteLoadWalletInquiry(wsmodel);
        }
    }

    public static WalletCMSWsEntity processLoadWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteLoadWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUnloadWalletInquiry(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUnLoadWalletInquiry(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUnloadWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUnLoadWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processWalletTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteWalletTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMerchantTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMerchantCoreTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantCoreTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processConfirmFraudOtp(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteConfirmFraudOtp(wsmodel);
        }
    }

    public static WalletCMSWsEntity processConfirmOtp(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteConfirmOtp(wsmodel);
        }
    }

    public static WalletCMSWsEntity processFetchProvisionalWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteFetchProvisionalWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processFetchProvisionalWalletList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteFetchProvisionalWalletList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateProvisionalWalletAddress(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateProvisionalWalletAddress(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalGetBasicInfo(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalGetBasicInfo(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetConsumerTransactions(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetConsumerTransactions(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetTransactionDetails(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetTransactionDetails(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateWalletAddress(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateWalletAddress(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateWalletSecondaryPhoneNumber(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateWalletSecondaryPhoneNumber(wsmodel);
        }
    }

    public static WalletCMSWsEntity processResetWalletPin(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteResetWalletPin(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminBlockWalletAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteAdminBlockWalletAccount(wsmodel);
        }
    }

    public static WalletCMSWsEntity processActivateDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteActivateDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalActivateDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalActivateDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processBlockDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteBlockDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalBlockDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalBlockDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processTempBlockDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteTempBlockDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processBlockChannel(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteBlockChannel(wsmodel);
        }
    }

    //m.rehman: for NayaPay, handling for Askari Bank Services
    public static WalletCMSWsEntity processCustomerInquiry(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCustomerInquiry(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCashDeposit(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCashDeposit(wsmodel);
        }
    }

    public static WalletCMSWsEntity processChequeClearing(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteChequeClearing(wsmodel);
        }
    }

    public static WalletCMSWsEntity processChequeFT(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteChequeFT(wsmodel);
        }
    }

    public static WalletCMSWsEntity processChequeBounce(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteChequeBounce(wsmodel);
        }
    }

    //m.rehman: for NayaPay, Onelink Bill Payment Topup
    public static WalletCMSWsEntity processBillInquiry(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        return WSOperation.ExecuteBillInquiry(wsmodel);
        /*if(!WSOperation.ValidateEncryptedKey(wsmodel)) //TODO: Will be Done after Routing i.e Destination Channel
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteBillPayment(wsmodel);
        }*/
    }

    public static WalletCMSWsEntity processBillPayment(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        return WSOperation.ExecuteBillPayment(wsmodel);
        /*if(!WSOperation.ValidateEncryptedKey(wsmodel)) //TODO: Will be Done after Routing i.e Destination Channel
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteBillPayment(wsmodel);
        }*/
    }

    public static WalletCMSWsEntity processBalanceInquiry(WalletCMSWsEntity wsmodel)
    {
        //Added by Moiz for RE: VC-NAP-202406121==>[ Details Required ]==>UBL ONUS ORA - Production for proper response code send
        try{
            WSOperation.ExecuteBalanceInquiry(wsmodel);
            GeneralDao.Instance.endTransaction();
        }
        catch (Exception e){
            logger.error("Exception caught while committing [" + wsmodel.getServicename() + "] transaction, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
        }
        GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
        return wsmodel;
    }

    public static WalletCMSWsEntity processCashWithDrawal(WalletCMSWsEntity wsmodel)
    {
        //Added by Moiz for RE: VC-NAP-202406121==>[ Details Required ]==>UBL ONUS ORA - Production for proper response code send
        /*if(wsmodel.getAcqbin().equals("1000009")) {
            WSOperation.ExecuteOnUsCashWithDrawal(wsmodel);
        }
        else {
            WSOperation.ExecuteCashWithDrawal(wsmodel);
        }*/

        try{
            if(wsmodel.getAcqbin().equals("1000009")) {
                 WSOperation.ExecuteOnUsCashWithDrawal(wsmodel);
            }
            else {
                WSOperation.ExecuteCashWithDrawal(wsmodel);
            }
            GeneralDao.Instance.endTransaction();
        }
        catch (Exception e){
            logger.error("Exception caught while committing [" + wsmodel.getServicename() + "] transaction, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
        }
        GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
        return wsmodel;

    }

    public static WalletCMSWsEntity processPurchase(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecutePurchase(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCardBasedReversal(WalletCMSWsEntity wsmodel)
    {
        //Added by Moiz for RE: VC-NAP-202406121==>[ Details Required ]==>UBL ONUS ORA - Production for proper response code send
        try{
            if(wsmodel.getAcqbin().equals("1000009")) {
                WSOperation.ExecuteOnUsATMCashWithdrawalReversal(wsmodel);
            }
            else {
                WSOperation.ExecuteCardBasedReversal(wsmodel);
            }
            GeneralDao.Instance.endTransaction();
        }
        catch (Exception e){
            logger.error("Exception caught while committing [" + wsmodel.getServicename() + "] transaction, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
        }
        GeneralDao.Instance.beginTransaction();
        return wsmodel;
    }

    public static WalletCMSWsEntity processEnvelopLoad(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteEnvelopLoad(wsmodel);
        }
    }

    public static WalletCMSWsEntity processEnvelopUnload(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteEnvelopUnload(wsmodel);
        }
    }

    public static WalletCMSWsEntity processReverseEnvelop(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteReverseEnvelop(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateSecretQuestions(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateSecretQuestions(wsmodel);
        }
    }
    //m.rehman: for NayaPay, adding new call for document 2.0 <start>
    public static WalletCMSWsEntity processMerchantReversalTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantReversalTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processOnelinkBillerTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processOnelinkBillerCoreTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteOnelinkCoreTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminPortalGetUserWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteAdminPortalGetUserWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminPortalGetUserdebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteAdminPortalGetUserdebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminPortalGetUserLinkedAccountList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteAdminPortalGetUserLinkedAccountList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminPortalGetUserTransactionList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteAdminPortalGetUserTransactionList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminPortalGetTransactionDetail(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteAdminPortalGetTransactionDetail(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalGetUserTransactionList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalGetUserTransactionList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminPortalBlockWalletAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteAdminPortalBlockWalletAccount(wsmodel);
        }
    }
    //m.rehman: for NayaPay, adding new call for document 2.0 <end>

    public static WalletCMSWsEntity processGetTransactionCharge(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetTransactionCharge(wsmodel);
        }
    }

    public static WalletCMSWsEntity processVerifyWalletByCNIC(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteVerifyWalletByCNIC(wsmodel);
        }
    }

    public static WalletCMSWsEntity processBioOpsUpgradeWalletAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteBioOpsUpgradeWalletAccount(wsmodel);
        }
    }

    public static WalletCMSWsEntity processBioOpsEnableWalletAccount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteBioOpsEnableWalletAccount(wsmodel);
        }
    }

    public static WalletCMSWsEntity processVerifyWalletByCNICforCash(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteVerifyWalletByCNICforCash(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCNICBasedCashWithdrawal(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCNICBasedCashWithdrawal(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCNICBasedCashWithdrawalReversal(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCNICBasedCashWithdrawalReversal(wsmodel);
        }
    }

    public static WalletCMSWsEntity processWalletInquiryForReversal(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteWalletInquiryForReversal(wsmodel);
        }
    }

//    public static WalletCMSWsEntity processMerchantCreditTransaction(WalletCMSWsEntity wsmodel)
//    {
//        logger.info("Verifying EncryptedKey...");
//        if(!WSOperation.ValidateEncryptedKey(wsmodel))
//        {
//            logger.error("Encrypted Key Verification Failed...");
//            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
//            return wsmodel;
//        }
//        else
//        {
//            return WSOperation.ExecuteMerchantCreditTransaction(wsmodel);
//        }
//    }
//
//    public static WalletCMSWsEntity processMerchantDebitTransaction(WalletCMSWsEntity wsmodel)
//    {
//        logger.info("Verifying EncryptedKey...");
//        if(!WSOperation.ValidateEncryptedKey(wsmodel))
//        {
//            logger.error("Encrypted Key Verification Failed...");
//            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
//            return wsmodel;
//        }
//        else
//        {
//            return WSOperation.ExecuteMerchantDebitTransaction(wsmodel);
//        }
//    }

    public static WalletCMSWsEntity processGetUserKYCAddress(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserKYCAddress(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserKYCQuestionList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserKYCQuestionList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processVerifyUserSecretQuestion(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteVerifyUserSecretQuestion(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserCNICName(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserCNICName(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetWalletState(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetWalletState(wsmodel);
        }
    }

    public static WalletCMSWsEntity processDisableDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteDisableDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetDebitCardPAN(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetDebitCardPAN(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCreateMerchantWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCreateMerchantWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processCreatePrepaidCardWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteCreatePrepaidCardWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMarkDisputedTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMarkDisputedTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processAdminPortalLockMerchantWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecutesAdminPortalLockMerchantWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalGetAdvanceInfo(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalGetAdvanceInfo(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalLockWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalLockWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalUpdateLockState(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalUpdateLockState(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalGetWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalGetWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalGetLinkedAccountList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalGetLinkedAccountList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalMarkDisputedTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalMarkDisputedTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantDebitCardWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantDebitCardWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processRequestMerchantDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteRequestMerchantDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processActivateMerchantDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteActivateMerchantDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateMerchantDebitCardPIN(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateMerchantDebitCardPIN(wsmodel);
        }
    }

    public static WalletCMSWsEntity processEnableMerchantDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteEnableMerchantDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalEnableMerchantDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteEnableMerchantDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalBlockMerchantDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalBlockMerchantDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalGetUserDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalGetUserDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMerchantRefundTransaction(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantRefundTransaction(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMerchantLoadDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantLoadUnloadDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMerchantUnloadDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantLoadUnloadDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMerchantUnloadWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantUnloadWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantWalletTransactionCharge(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantWalletTransactionCharge(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantWalletTransactionList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantWalletTransactionList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantWalletTransactionDetail(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantWalletTransactionDetail(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantDebitCardTransactionList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantDebitCardTransactionList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetMerchantDebitCardTransactionDetails(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetMerchantDebitCardTransactionDetails(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserID(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserID(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSettlementLoadMerchantWallet(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSettlementLoadMerchantWallet(wsmodel);
        }
    }

    public static WalletCMSWsEntity processUpdateUserKYCAddress(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateUserKYCAddress(wsmodel);
        }
    }

    public static WalletCMSWsEntity processTitleFetch(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteTitleFetch(wsmodel);
        }
    }

    public static WalletCMSWsEntity processIBFTIn(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteIBFTIn(wsmodel);
        }
    }

    public static WalletCMSWsEntity processFundManagement(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteFundManagement(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGeneratePINBlock(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteGeneratePINBlock(wsmodel);
    }

    public static WalletCMSWsEntity processReversalTransaction(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteReversalTransaction(wsmodel);
    }

    public static WalletCMSWsEntity processOnelinkTopupBillPayment(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else {
            return WSOperation.ExecuteOnelinkTopupBillPayment(wsmodel);
        }
    }

    // Author: Asim Shahzad, Date : 25th Feb 2020, Desc : For getting Nayapay mobile application download counts from middleware

    public static WalletCMSWsEntity processGetAppDownloadCount(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetAppDownloadCount(wsmodel);
        }
    }

    public static WalletCMSWsEntity processGetUserCardTransactionList(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserCardTransactionList(wsmodel);
        }
    }

    public static WalletCMSWsEntity processReorderDebitCard(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteReorderDebitCard(wsmodel);
        }
    }

    public static WalletCMSWsEntity processMerchantSettlementLogging(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteMerchantSettlementLogging(wsmodel);
        }
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processECommerce(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteECommerce(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processVCASStepup(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteVCASStepup(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processQRMerchantPayment(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteQRMerchantPayment(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processQRMerchantPaymentReversal(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteQRMerchantPaymentReversal(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processQRMerchantPaymentRefund(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteQRMerchantPaymentRefund(wsmodel);
    }

    public static WalletCMSWsEntity processIBFT(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteIBFT(wsmodel);
    }

    public static WalletCMSWsEntity processTitleFetchInquiry(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteTitleFetchInquiry(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processChallengeMPIN(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteChallengeMPIN(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processAccountVerification(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteAccountVerification(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processSTIP(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteSTIP(wsmodel);
    }

	//m.rehman: Euronet Integration
    public static WalletCMSWsEntity processGetUserIDFromPAN(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteGetUserIDFromPAN(wsmodel);
    }

    // Asim Shahzad, Date : 17th Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104
    public static WalletCMSWsEntity processLoadWalletInquiryWithoutMPIN(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteLoadWalletInquiryWithoutMPIN(wsmodel);
        }
    }
    // ====================================================================================
	
    //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
    public static WalletCMSWsEntity processDisputeRefundTransaction(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteDisputeRefundTransaction(wsmodel);
    }

    // Asim Shahzad, Date : 13th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)
    public static WalletCMSWsEntity processUpdateCardControls(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteUpdateCardControls(wsmodel);
        }
    }

    public static WalletCMSWsEntity processSupportPortalUpdateCardControls(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteSupportPortalUpdateCardControls(wsmodel);
        }
    }
    // ========================================================================================================

    // Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)

    public static WalletCMSWsEntity processGetUserVirtualCardCvvTwo(WalletCMSWsEntity wsmodel)
    {
        logger.info("Verifying EncryptedKey...");
        if(!WSOperation.ValidateEncryptedKey(wsmodel))
        {
            logger.error("Encrypted Key Verification Failed...");
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
            return wsmodel;
        }
        else
        {
            return WSOperation.ExecuteGetUserVirtualCardCvvTwo(wsmodel);
        }
    }

    // ========================================================================================================================
	
	//m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
    public static WalletCMSWsEntity processUpdateCardLimits(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteUpdateCardLimits(wsmodel);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 05-03-2021, VP-NAP-202103041/ VC-NAP-202103041 - Merchant Transaction Listing Issue
    public static WalletCMSWsEntity processGetMerchantTransactionList(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteGetMerchantTransactionList(wsmodel);
    }

    public static WalletCMSWsEntity processGetMerchantTransaction(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteGetMerchantTransaction(wsmodel);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 11th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111

    public static WalletCMSWsEntity processCloseWallet(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteCloseWallet(wsmodel);
    }
    // =====================================================================================================
    //m.rehman: 12-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    public static WalletCMSWsEntity processOpenDebitCredit(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteOpenDebitCredit(wsmodel);
    }
    //////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 29-04-2021, VG-NAP-202104271 / VP-NAP-202104261 / VC-NAP-202104261 - VISA transaction charging update
    public static WalletCMSWsEntity processGetDebitCardCharge(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteGetDebitCardCharge(wsmodel);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 10th Aug 2021, Tracking ID : VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091
    public static WalletCMSWsEntity processGetUserWalletStatement(WalletCMSWsEntity wsmodel)
    {
        //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
//        return WSOperation.ExecuteGetUserWalletStatement(wsmodel);
        return WSOperation.ExecuteGetUserWalletStatementbyDB(wsmodel);
    }
    // =======================================================================================================

    //Arsalan Akhter, Date: 23rd-Aug-2021, Tracking ID: VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091
    public static WalletCMSWsEntity processSetWalletStatusLock(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteSetWalletStatusLock(wsmodel);
    }
    //=======================================================================================================

    // Asim Shahzad, Date : 6th June 2023, Tracking ID : VP-NAP-202303091
    public static WalletCMSWsEntity processOnUsCashWithDrawalInquiry(WalletCMSWsEntity wsmodel)
    {
        return WSOperation.ExecuteOnUsCashWithDrawalInquiry(wsmodel);
    }
    // ==================================================================
}
