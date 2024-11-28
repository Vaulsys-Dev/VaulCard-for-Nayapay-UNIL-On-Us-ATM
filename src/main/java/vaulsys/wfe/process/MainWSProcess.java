package vaulsys.wfe.process;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.WebServiceUtil;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsListingEntity;
import vaulsys.webservice.walletcardmgmtwebservice.handler.WebServiceHandler;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import java.util.concurrent.Callable;

/**
 * Created by Raza Murtaza on 08-Mar-18.
 */
public class MainWSProcess implements Callable<WalletCMSWsEntity> {

    private static final Logger logger = Logger.getLogger(MainWSProcess.class);
    public WalletCMSWsEntity wsobj;
    public long id;
    DateTime d, nowplushour, nowlesshour;

    //m.rehman: 10-11-2021 - Nayapay Optimization
    private WalletCMSWsListingEntity listing;
    /////////////////////////////////////////////////////////////////

    public MainWSProcess(long Id, WalletCMSWsEntity wsobj) {
        this.id = Id;
        this.wsobj = wsobj;
    }

    @Override
    public WalletCMSWsEntity call() {
        try {
            logger.info("Executing WS Thread with id [" + id + "] for [" + wsobj.getServicename() + "]");

            //m.rehman: 10-11-2021 - Nayapay Optimization
            listing = new WalletCMSWsListingEntity();
            listing = listing.copy(wsobj);
            /////////////////////////////////////////////////////////////////

            if(!WebServiceUtil.ValidateSource(wsobj)) //Raza not making IP validation as part of Authorization
            {
                logger.error("Failed to Validate Source, rejecting....");

                //m.rehman: 10-11-2021 - Nayapay Optimization, adding listing object
                WebServiceUtil.PrintWSMsg(wsobj, false, listing);

                GeneralDao.Instance.endTransaction();
                return null;
            }

            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);

            //m.rehman: 10-11-2021 - Nayapay Optimization, adding listing object
            WebServiceUtil.PrintWSMsg(wsobj, true, listing);

            String servicename = wsobj.getServicename();
            switch(servicename)
            {
                case "CheckCnic":
                {
                    WebServiceHandler.processCheckCnic(wsobj);
                    break;
                }
                case "CreateWallet":
                {
                    WebServiceHandler.processCreateWallet(wsobj);
                    break;
                }
                case "CreateWalletPIN":
                {
                    WebServiceHandler.processCreateWalletPIN(wsobj);
                    break;
                }
                // Asim Shahzad, Date : 9th March 2021, Tracking ID : VP-NAP-202103112 / VC-NAP-202103112
                case "CreateWalletPINWithSecretQuestions":
                {
                    WebServiceHandler.processCreateWalletPINWithSecretQuestions(wsobj);
                    break;
                }
                // ====================================================================================================
                case "DeleteProvisionalWallet":
                {
                    WebServiceHandler.processDeleteProvisionalWallet(wsobj);
                    break;
                }
                case "ActivateProvisionalWallet":
                {
                    WebServiceHandler.processActivateProvisionalWallet(wsobj);
                    break;
                }
                case "CreateWalletLevelOne":
                {
                    WebServiceHandler.processCreateWalletLevelOne(wsobj);
                    break;
                }
                case "CustomerEnableWalletAccount":
                {
                    WebServiceHandler.processCustomerEnableWalletAccount(wsobj);
                    break;
                }
                case "ChangeWalletPin":
                {
                    WebServiceHandler.processChangeWalletPin(wsobj);
                    break;
                }
                case "VerifyWalletPin":
                {
                    WebServiceHandler.processVerifyWalletPin(wsobj);
                    break;
                }
                case "DebitCardRequest":
                {
                    WebServiceHandler.processDebitCardRequest(wsobj);
                    break;
                }
                case "EnableDebitCard":
                {
                    WebServiceHandler.processEnableDebitCard(wsobj);
                    break;
                }
                case "SupportPortalEnableDebitCard":
                {
                    WebServiceHandler.processSupportPortalEnableDebitCard(wsobj);
                    break;
                }
                case "ChangeDebitCardPin":
                {
                    WebServiceHandler.processChangeDebitCardPin(wsobj);
                    break;
                }
                case "LinkBankAccountInquiry":
                {
                    WebServiceHandler.processLinkBankAccountInquiry(wsobj);
                    break;
                }
                case "LinkBankAccount":
                {
                    WebServiceHandler.processLinkBankAccount(wsobj);
                    break;
                }
                case "LinkBankAccountOTP":
                {
                    WebServiceHandler.processLinkBankAccountOTP(wsobj);
                    break;
                }
                case "VerifyLinkAccountOTP":
                {
                    WebServiceHandler.processVerifyLinkAccountOTP(wsobj);
                    break;
                }
                case "ConfirmLinkBankAccountOTP":
                {
                    WebServiceHandler.processConfirmLinkBankAccountOTP(wsobj);
                    break;
                }
                case "UnLinkBankAccountInquiry":
                {
                    WebServiceHandler.processUnLinkBankAccountInquiry(wsobj);
                    break;
                }
                case "UnLinkBankAccount":
                {
                    WebServiceHandler.processUnLinkBankAccount(wsobj);
                    break;
                }
                case "UpdateLinkedAccountAlias":
                {
                    WebServiceHandler.processUpdateLinkedAccountAlias(wsobj);
                    break;
                }
                case "SetPrimaryLinkedAccount":
                {
                    WebServiceHandler.processSetPrimaryLinkedAccount(wsobj);
                    break;
                }
                case "GetUserToken":
                {
                    WebServiceHandler.processGetUserToken(wsobj);
                    break;
                }
                case "GetUserWallet":
                {
                    WebServiceHandler.processGetUserWallet(wsobj);
                    break;
                }
                case "GetUserdebitCard":
                {
                    WebServiceHandler.processGetUserDebitCard(wsobj);
                    break;
                }
                case "GetUserLinkedAccountList":
                {
                    WebServiceHandler.processGetUserLinkedAccountList(wsobj);
                    break;
                }
				case "GetUserLinkedAccountListWithoutToken":
                {
                    WebServiceHandler.processGetUserLinkedAccountListWithoutToken(wsobj);
                    break;
                }
                case "GetUserTransaction":
                {
                    WebServiceHandler.processGetUserTransaction(wsobj);
                    break;
                }
                case "GetUserTransactionforChat":
                {
                    WebServiceHandler.processGetUserTransactionforChat(wsobj);
                    break;
                }
                case "GetUserTransactionList":
                {
                    WebServiceHandler.processGetUserTransactionList(wsobj);
                    break;
                }
                case "LoadWalletInquiry":
                {
                    WebServiceHandler.processLoadWalletInquiry(wsobj);
                    break;
                }
                case "LoadWallet":
                {
                    WebServiceHandler.processLoadWallet(wsobj);
                    break;
                }
                case "UnloadWalletInquiry":
                {
                    WebServiceHandler.processUnloadWalletInquiry(wsobj);
                    break;
                }
                case "UnloadWallet":
                {
                    WebServiceHandler.processUnloadWallet(wsobj);
                    break;
                }
                case "WalletTransaction":
                {
                    WebServiceHandler.processWalletTransaction(wsobj);
                    break;
                }
                case "MerchantBillerTransaction":
                case "MerchantRetailTransaction":
                {
                    WebServiceHandler.processMerchantTransaction(wsobj);
                    break;
                }
                case "MerchantBillerCoreTransaction":
                case "MerchantRetailCoreTransaction":
                {
                    WebServiceHandler.processMerchantCoreTransaction(wsobj);
                    break;
                }
                case "ConfirmFraudOtp":
                {
                    WebServiceHandler.processConfirmFraudOtp(wsobj);
                    break;
                }
                case "ConfirmOtp":
                {
                    WebServiceHandler.processConfirmOtp(wsobj);
                    break;
                }
                case "FetchProvisionalWallet":
                {
                    WebServiceHandler.processFetchProvisionalWallet(wsobj);
                    break;
                }
                case "FetchProvisionalWalletList":
                {
                    WebServiceHandler.processFetchProvisionalWalletList(wsobj);
                    break;
                }
                case "UpdateProvisionalWalletAddress":
                {
                    WebServiceHandler.processUpdateProvisionalWalletAddress(wsobj);
                    break;
                }
                case "SupportPortalGetBasicInfo":
                {
                    WebServiceHandler.processSupportPortalGetBasicInfo(wsobj);
                    break;
                }
                case "GetConsumerTransactions":
                {
                    WebServiceHandler.processGetConsumerTransactions(wsobj);
                    break;
                }
                case "GetTransactionDetails":
                {
                    WebServiceHandler.processGetTransactionDetails(wsobj);
                    break;
                }
                case "UpdateUserKYCAddress":
                {
                    WebServiceHandler.processUpdateUserKYCAddress(wsobj);
                    break;
                }
                case "UpdateWalletSecondaryPhoneNumber":
                {
                    WebServiceHandler.processUpdateWalletSecondaryPhoneNumber(wsobj);
                    break;
                }
                case "ResetWalletPin":
                {
                    WebServiceHandler.processResetWalletPin(wsobj);
                    break;
                }
                case "AdminBlockWalletAccount":
                {
                    WebServiceHandler.processAdminBlockWalletAccount(wsobj);
                    break;
                }
                case "ActivateDebitCard":
                {
                    WebServiceHandler.processActivateDebitCard(wsobj);
                    break;
                }
                case "SupportPortalActivateDebitCard":
                {
                    WebServiceHandler.processSupportPortalActivateDebitCard(wsobj);
                    break;
                }
                case "BlockDebitCard":
                {
                    WebServiceHandler.processBlockDebitCard(wsobj);
                    break;
                }
                case "SupportPortalBlockDebitCard":
                {
                    WebServiceHandler.processSupportPortalBlockDebitCard(wsobj);
                    break;
                }
                case "TempBlockDebitCard":
                {
                    WebServiceHandler.processTempBlockDebitCard(wsobj);
                    break;
                }
                case "BlockChannel":
                {
                    WebServiceHandler.processBlockChannel(wsobj);
                    break;
                }
                case "CustomerInquiry":
                {
                    WebServiceHandler.processCustomerInquiry(wsobj);
                    break;
                }
                case "CashDeposit":
                {
                    WebServiceHandler.processCashDeposit(wsobj);
                    break;
                }
                case "ChequeClearing":
                {
                    WebServiceHandler.processChequeClearing(wsobj);
                    break;
                }
                case "ChequeFT":
                {
                    WebServiceHandler.processChequeFT(wsobj);
                    break;
                }
                case "ChequeBounce":
                {
                    WebServiceHandler.processChequeBounce(wsobj);
                    break;
                }
                case "OneLinkBillInquiry":
                {
                    WebServiceHandler.processBillInquiry(wsobj);
                    break;
                }
                case "OneLinkBillPayment":
                {
                    WebServiceHandler.processBillPayment(wsobj);
                    break;
                }
                case "BalanceInquiry":
                {
                    WebServiceHandler.processBalanceInquiry(wsobj);
                    break;
                }
                case "CashWithDrawal":
                {
                    WebServiceHandler.processCashWithDrawal(wsobj);
                    break;
                }
                case "CashWithDrawalReversal":
                {
                    WebServiceHandler.processCardBasedReversal(wsobj);
                    break;
                }
                case "Purchase":
                {
                    WebServiceHandler.processPurchase(wsobj);
                    break;
                }
                case "PurchaseReversal":
                {
                    WebServiceHandler.processCardBasedReversal(wsobj);
                    break;
                }
                case "EnvelopLoad":
                {
                    WebServiceHandler.processEnvelopLoad(wsobj);
                    break;
                }
                case "EnvelopUnload":
                {
                    WebServiceHandler.processEnvelopUnload(wsobj);
                    break;
                }
                case "ReverseEnvelop":
                {
                    WebServiceHandler.processReverseEnvelop(wsobj);
                    break;
                }
                case "UpdateUserSecretQuestions":
                {
                    WebServiceHandler.processUpdateSecretQuestions(wsobj);
                    break;
                }
                case "MerchantReversalTransaction":
                {
                    WebServiceHandler.processMerchantReversalTransaction(wsobj);
                    break;
                }
                case "OnelinkBillerTransaction":
                {
                    WebServiceHandler.processOnelinkBillerTransaction(wsobj);
                    break;
                }
                case "OnelinkBillerCoreTransaction":
                {
                    WebServiceHandler.processOnelinkBillerCoreTransaction(wsobj);
                    break;
                }
                case "AdminPortalGetUserWallet":
                {
                    WebServiceHandler.processAdminPortalGetUserWallet(wsobj);
                    break;
                }
                case "AdminPortalGetUserdebitCard":
                {
                    WebServiceHandler.processAdminPortalGetUserdebitCard(wsobj);
                    break;
                }
                case "AdminPortalGetUserLinkedAccountList":
                {
                    WebServiceHandler.processAdminPortalGetUserLinkedAccountList(wsobj);
                    break;
                }
                case "AdminPortalGetUserTransactionList":
                {
                    WebServiceHandler.processAdminPortalGetUserTransactionList(wsobj);
                    break;
                }
                case "AdminPortalGetTransactionDetail":
                {
                    WebServiceHandler.processAdminPortalGetTransactionDetail(wsobj);
                    break;
                }
                case "SupportPortalGetUserTransactionList":
                {
                    WebServiceHandler.processSupportPortalGetUserTransactionList(wsobj);
                    break;
                }
                case "AdminPortalBlockWalletAccount":
                {
                    WebServiceHandler.processAdminPortalBlockWalletAccount(wsobj);
                    break;
                }
                case "GetTransactionCharge":
                {
                    WebServiceHandler.processGetTransactionCharge(wsobj);
                    break;
                }
                case "VerifyWalletByCNIC":
                {
                    WebServiceHandler.processVerifyWalletByCNIC(wsobj);
                    break;
                }
                case "BioOpsUpgradeWalletAccount":
                {
                    WebServiceHandler.processBioOpsUpgradeWalletAccount(wsobj);
                    break;
                }
                case "BioOpsEnableWalletAccount":
                {
                    WebServiceHandler.processBioOpsEnableWalletAccount(wsobj);
                    break;
                }
                case "VerifyWalletByCNICforCash":
                {
                    WebServiceHandler.processVerifyWalletByCNICforCash(wsobj);
                    break;
                }
                case "CNICBasedCashWithdrawal":
                {
                    WebServiceHandler.processCNICBasedCashWithdrawal(wsobj);
                    break;
                }
                case "CNICBasedCashWithdrawalReversal":
                {
                    WebServiceHandler.processCNICBasedCashWithdrawalReversal(wsobj);
                    break;
                }
                case "WalletInquiryForReversal":
                {
                    WebServiceHandler.processWalletInquiryForReversal(wsobj);
                    break;
                }
                case "GeneratePINBlock":
                {
                    WebServiceHandler.processGeneratePINBlock(wsobj);
                    break;
                }
                case "ReversalTransaction":
                {
                    WebServiceHandler.processReversalTransaction(wsobj);
                    break;
                }
                case "SupportPortalGetUserTransaction":
                {
                    WebServiceHandler.processSupportPortalGetUserTransaction(wsobj);
                    break;
                }
				case "GetUserKYCAddress":
                {
                    WebServiceHandler.processGetUserKYCAddress(wsobj);
                    break;
                }
				case "GetUserKYCQuestionList":
                {
                    WebServiceHandler.processGetUserKYCQuestionList(wsobj);
                    break;
                }
				case "VerifyUserSecretQuestion":
                {
                    WebServiceHandler.processVerifyUserSecretQuestion(wsobj);
                    break;
                }
				case "GetUserCNICName":
                {
                    WebServiceHandler.processGetUserCNICName(wsobj);
                    break;
                }
				case "GetWalletState":
                {
                    WebServiceHandler.processGetWalletState(wsobj);
                    break;
                }
				case "DisableDebitCard":
                {
                    WebServiceHandler.processDisableDebitCard(wsobj);
                    break;
                }
				case "GetDebitCardPAN":
                {
                    WebServiceHandler.processGetDebitCardPAN(wsobj);
                    break;
                }
				case "CreateMerchantWallet":
                {
                    WebServiceHandler.processCreateMerchantWallet(wsobj);
                    break;
                }
                case "CreatePrepaidCardWallet":
                {
                    WebServiceHandler.processCreatePrepaidCardWallet(wsobj);
                    break;
                }
                case "MarkDisputedTransaction":
                {
                    WebServiceHandler.processMarkDisputedTransaction(wsobj);
                    break;
                }
                case "AdminPortalLockMerchantWallet":
                {
                    WebServiceHandler.processAdminPortalLockMerchantWallet(wsobj);
                    break;
                }
                case "SupportPortalGetAdvanceInfo":
                {
                    WebServiceHandler.processSupportPortalGetAdvanceInfo(wsobj);
                    break;
                }
                case "SupportPortalLockWallet":
                {
                    WebServiceHandler.processSupportPortalLockWallet(wsobj);
                    break;
                }
                case "SupportPortalUpdateLockState":
                {
                    WebServiceHandler.processSupportPortalUpdateLockState(wsobj);
                    break;
                }
                case "SupportPortalGetWallet":
                {
                    WebServiceHandler.processSupportPortalGetWallet(wsobj);
                    break;
                }
                case "SupportPortalGetLinkedAccountList":
                {
                    WebServiceHandler.processSupportPortalGetLinkedAccountList(wsobj);
                    break;
                }
                case "SupportPortalMarkDisputedTransaction":
                {
                    WebServiceHandler.processSupportPortalMarkDisputedTransaction(wsobj);
                    break;
                }
                case "SupportPortalEnableMerchantDebitCard":
                {
                    WebServiceHandler.processSupportPortalEnableMerchantDebitCard(wsobj);
                    break;
                }
                case "SupportPortalBlockMerchantDebitCard":
                {
                    WebServiceHandler.processSupportPortalBlockMerchantDebitCard(wsobj);
                    break;
                }
                case "SupportPortalGetUserDebitCard":
                {
                    WebServiceHandler.processSupportPortalGetUserDebitCard(wsobj);
                    break;
                }
                case "GetMerchantWallet":
                {
                    WebServiceHandler.processGetMerchantWallet(wsobj);
                    break;
                }
                case "GetMerchantDebitCardWallet":
                {
                    WebServiceHandler.processGetMerchantDebitCardWallet(wsobj);
                    break;
                }
                case "GetMerchantDebitCard":
                {
                    WebServiceHandler.processGetMerchantDebitCard(wsobj);
                    break;
                }
                case "RequestMerchantDebitCard":
                {
                    WebServiceHandler.processRequestMerchantDebitCard(wsobj);
                    break;
                }
                case "ActivateMerchantDebitCard":
                {
                    WebServiceHandler.processActivateMerchantDebitCard(wsobj);
                    break;
                }
                case "UpdateMerchantDebitCardPIN":
                {
                    WebServiceHandler.processUpdateMerchantDebitCardPIN(wsobj);
                    break;
                }
                case "EnableMerchantDebitCard":
                {
                    WebServiceHandler.processEnableMerchantDebitCard(wsobj);
                    break;
                }
                case "MerchantRefundTransaction":
                {
                    WebServiceHandler.processMerchantRefundTransaction(wsobj);
                    break;
                }
                case "MerchantLoadDebitCard":
                {
                    WebServiceHandler.processMerchantLoadDebitCard(wsobj);
                    break;
                }
                case "MerchantUnloadDebitCard":
                {
                    WebServiceHandler.processMerchantUnloadDebitCard(wsobj);
                    break;
                }
                case "MerchantUnloadWallet":
                {
                    WebServiceHandler.processMerchantUnloadWallet(wsobj);
                    break;
                }
                case "GetMerchantWalletTransactionCharge":
                {
                    WebServiceHandler.processGetMerchantWalletTransactionCharge(wsobj);
                    break;
                }
                case "GetMerchantWalletTransactionList":
                {
                    WebServiceHandler.processGetMerchantWalletTransactionList(wsobj);
                    break;
                }
                case "GetMerchantWalletTransactionDetail":
                {
                    WebServiceHandler.processGetMerchantWalletTransactionDetail(wsobj);
                    break;
                }
                case "GetMerchantDebitCardTransactionList":
                {
                    WebServiceHandler.processGetMerchantDebitCardTransactionList(wsobj);
                    break;
                }
                case "GetMerchantDebitCardTransactionDetails":
                {
                    WebServiceHandler.processGetMerchantDebitCardTransactionDetails(wsobj);
                    break;
                }
                case "GetUserID":
                {
                    WebServiceHandler.processGetUserID(wsobj);
                    break;
                }
                case "SettlementLoadMerchantWallet":
                {
                    WebServiceHandler.processSettlementLoadMerchantWallet(wsobj);
                    break;
                }
                case "OnelinkTopupBillPayment":
                {
                    WebServiceHandler.processOnelinkTopupBillPayment(wsobj);
                    break;
                }
                case "TitleFetch":
                {
                    WebServiceHandler.processTitleFetch(wsobj);
                    break;
                }
                case "IBFTIn":
                {
                    WebServiceHandler.processIBFTIn(wsobj);
                    break;
                }
                case  "FundManagement":
                {
                    WebServiceHandler.processFundManagement(wsobj);
                    break;
                }
                // Author: Asim Shahzad, Date : 25th Feb 2020, Desc : For getting Nayapay mobile application download counts from middleware
                case "GetAppDownloadCount":
                {
                    WebServiceHandler.processGetAppDownloadCount(wsobj);
                    break;
                }
                case "GetUserCardTransactionList":
                {
                    WebServiceHandler.processGetUserCardTransactionList(wsobj);
                    break;
                }
                case "ReorderDebitCard":
                {
                    WebServiceHandler.processReorderDebitCard(wsobj);
                    break;
                }
                case "MerchantSettlementLogging":
                {
                    WebServiceHandler.processMerchantSettlementLogging(wsobj);
                    break;
                }
				//m.rehman: Euronet Integration
                case "ECommerce":
                {
                    WebServiceHandler.processECommerce(wsobj);
                    break;
                }
				//m.rehman: Euronet Integration
                case "ECommerceReversal":
                {
                    WebServiceHandler.processCardBasedReversal(wsobj);
                    break;
                }
				//m.rehman: Euronet Integration
                case "VCASStepup":
                {
                    WebServiceHandler.processVCASStepup(wsobj);
                    break;
                }
				//m.rehman: Euronet Integration
                case "QRMerchantPayment":
                {
                    WebServiceHandler.processQRMerchantPayment(wsobj);
                    break;
                }
				//m.rehman: Euronet Integration
                case "QRMerchantPaymentReversal":
                {
                    WebServiceHandler.processQRMerchantPaymentReversal(wsobj);
                    break;
                }
				//m.rehman: Euronet Integration
                case "QRMerchantPaymentRefund":
                {
                    WebServiceHandler.processQRMerchantPaymentRefund(wsobj);
                    break;
                }
                case "IBFT":
                {
                    WebServiceHandler.processIBFT(wsobj);
                    break;
                }
                case "TitleFetchInquiry":
                {
                    WebServiceHandler.processTitleFetchInquiry(wsobj);
                    break;
                }
				//m.rehman: Euronet integration
				////////////////////////////////////////////////////////////////////////////
                case "ChallengeMPIN":
                {
                    WebServiceHandler.processChallengeMPIN(wsobj);
                    break;
                }
                case "MOTO":
                {
                    WebServiceHandler.processECommerce(wsobj);
                    break;
                }
                case "MOTOReversal":
                {
                    WebServiceHandler.processCardBasedReversal(wsobj);
                    break;
                }
                case "AccountVerification":
                {
                    WebServiceHandler.processAccountVerification(wsobj);
                    break;
                }
                case "PreAuthorization":
                {
                    WebServiceHandler.processPurchase(wsobj);
                    break;
                }
                case "PreAuthCompletion":
                {
                    WebServiceHandler.processPurchase(wsobj);
                    break;
                }
                case "PreAuthorizationReversal":
                {
                    WebServiceHandler.processCardBasedReversal(wsobj);
                    break;
                }
                case "Refund":
                {
                    WebServiceHandler.processAccountVerification(wsobj);
                    break;
                }
                case "STIP":
                {
                    WebServiceHandler.processSTIP(wsobj);
                    break;
                }
                case "GetUserIDFromPAN":
                {
                    WebServiceHandler.processGetUserIDFromPAN(wsobj);
                    break;
                }
				////////////////////////////////////////////////////////////////////////////
                // Asim Shahzad, Date : 17th Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104
                case "LoadWalletInquiryWithoutMPIN":
                {
                    WebServiceHandler.processLoadWalletInquiryWithoutMPIN(wsobj);
                    break;
                }
                // =====================================================================================
                //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
                case "DisputeRefundTransaction":
                {
                    WebServiceHandler.processDisputeRefundTransaction(wsobj);
                    break;
                }
                // Asim Shahzad, Date : 13th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)
                case "UpdateCardControls":
                {
                    WebServiceHandler.processUpdateCardControls(wsobj);
                    break;
                }
                case "SupportPortalUpdateCardControls":
                {
                    WebServiceHandler.processSupportPortalUpdateCardControls(wsobj);
                    break;
                }
                case "AdminPortalUpdateCardControls":
                {
                    WebServiceHandler.processUpdateCardControls(wsobj);
                    break;
                }
                // ========================================================================================================

                // Asim Shahzad, Date : 20th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
                case "GetUserVirtualCardCvvTwo":
                {
                    WebServiceHandler.processGetUserVirtualCardCvvTwo(wsobj);
                    break;
                }
                // ========================================================================================================
				//m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
                case "UpdateCardLimits":
                {
                    WebServiceHandler.processUpdateCardLimits(wsobj);
                    break;
                }

                //m.rehman: 05-03-2021, VP-NAP-202103041/ VC-NAP-202103041 - Merchant Transaction Listing Issue
                case "GetMerchantTransactionList":
                {
                    WebServiceHandler.processGetMerchantTransactionList(wsobj);
                    break;
                }
                case "GetMerchantTransaction":
                {
                    WebServiceHandler.processGetMerchantTransaction(wsobj);
                    break;
                }
                //////////////////////////////////////////////////////////////////////////////////////////
                // Asim Shahzad, Date : 11th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111

                case "CloseWallet":
                {
                    WebServiceHandler.processCloseWallet(wsobj);
                    break;
                }
                // =====================================================================================================
            

                //m.rehman: 12-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
                case "OpenDebitCredit":
                {
                    WebServiceHandler.processOpenDebitCredit(wsobj);
                    break;
                }
                //////////////////////////////////////////////////////////////////////////////////

                //m.rehman: 29-04-2021, VG-NAP-202104271 / VP-NAP-202104261 / VC-NAP-202104261 - VISA transaction charging update
                case "GetDebitCardCharge":
                {
                    WebServiceHandler.processGetDebitCardCharge(wsobj);
                    break;
                }
                ///////////////////////////////////////////////////////////////////////////////////////////////////////

                // Asim Shahzad, Date : 10th Aug 2021, Tracking ID : VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091
                case "GetUserWalletStatement":
                {
                    WebServiceHandler.processGetUserWalletStatement(wsobj);
                    break;
                }
                // =======================================================================================================

                //Arsalan Akhter, Date: 23rd-Aug-2021, Tracking ID: VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091
                case "SetWalletStatusLock":
                {
                    WebServiceHandler.processSetWalletStatusLock(wsobj);
                    break;
                }
                //=======================================================================================================

                default:
                {
                    logger.error("Unrecognised Service [" + wsobj.getServicename() + "]");
                    wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED); //46 Unable to Process Refer to Doc
                    break;
                }
            }

            //m.rehman: 28-07-2021, VC-NAP-202107271 - NP_6002 in GetUserTransactionforChat
            //finally {
                try {
                    //GeneralDao.Instance.endTransaction();
                    //GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
                    GeneralDao.Instance.flush();
                } catch (Exception e) {
                    logger.error(WebServiceUtil.getStrException(e));
                    wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED); //Raza Refer to Document '12'
                    //return wsmodel;
                }
            //}
            ///////////////////////////////////////////////////////////////////////////////

            //m.rehman: 10-11-2021 - Nayapay Optimization, adding listing object
            WebServiceUtil.PrintWSMsg(wsobj, false, listing);

            GeneralDao.Instance.endTransaction();

            return wsobj;


        } catch (Exception e) {
            logger.error("Exception Caught while executing webservice request");
			//m.rehman: 10-11-2021 - Nayapay Optimization
            logger.error(WebServiceUtil.getStrException(e));
            if (e instanceof ConstraintViolationException) {
                logger.error("Duplicate Transaction Exception...!");
                wsobj.setRespcode(ISOResponseCodes.DUPLICATE_TRANSACTION); //Duplicate Transaction Refer to Document //For same txnrefnum and transdatetime
            } else {
                wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED); //46 Unable to Process Refer to Doc
            }
            GeneralDao.Instance.endTransaction();
            return wsobj;
        }
    }


}
