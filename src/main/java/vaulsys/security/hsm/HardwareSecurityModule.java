package vaulsys.security.hsm;

import org.apache.commons.lang.StringUtils;
import vaulsys.calendar.DateTime;
import vaulsys.cms.base.*;
import vaulsys.cms.components.CMSDBOperations;
import vaulsys.cms.exception.*;
import vaulsys.config.IMDType;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.EMV.BERTLV;
import vaulsys.protocols.PaymentSchemes.EMV.EMVTags;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOPOSEntryMode;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.SecurityService;
import vaulsys.security.base.SecurityFunction;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.hsm.atalla.AtallaHSMConst;
import vaulsys.security.hsm.atalla.AtallaHSMHandler;
import vaulsys.security.hsm.base.CommandType;
import vaulsys.security.hsm.base.HSMNetworkManager;
import vaulsys.security.hsm.base.exception.NotAvailableHSMChannelFoundException;
import vaulsys.security.hsm.safenet.SafeNetHSMConst;
import vaulsys.security.hsm.safenet.SafeNetHSMHandler;
import vaulsys.security.hsm.thales.ThalesHSMConst;
import vaulsys.security.hsm.thales.ThalesHSMHandler;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.Util;
import vaulsys.util.WebServiceUtil;
import vaulsys.util.encoders.Hex;
import vaulsys.wallet.base.WalletCard;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by HP on 8/12/2016.
 */
public class HardwareSecurityModule {

    private static Logger logger = Logger.getLogger(HardwareSecurityModule.class);

    private static HardwareSecurityModule hardwareSecurityModule = null;

    public static HardwareSecurityModule getInstance() {
        if (hardwareSecurityModule == null) {
            hardwareSecurityModule = new HardwareSecurityModule();
        }
        return hardwareSecurityModule;
    }

    public void ValidateOnUsCardInfo (ProcessContext processContext) throws Exception {
        Ifx ifx;
        String pinOffset, respCode, cmsProductId;
        Long incomingProfileId;
        Set<SecureKey> incomingKeySet;
        Message incomingMessage;
        CMSCardAuthorizationFlags cardAuthFlags;
        List<CMSProductKeys> cmsProductKeys;
        Integer remRetries;
        CMSCardAuthorization cardAuth;
        CMSCard cmsCard;
        WalletCard walletCard;

        try {

            incomingMessage = processContext.getInputMessage();
            ifx = incomingMessage.getIfx();
            cardAuthFlags = ifx.getCardAuthFlags();
            cmsCard = null;
            walletCard = null;

            //pinOffset = "12345";
            //pinOffset = cardAuth.getEncryptedPin(); //Raza Fetch PIN Offset from IFX

            if (cardAuthFlags.getAuthRequiredFlag()) {

                logger.info("HSM Authorization Required");

                incomingProfileId = incomingMessage.getEndPointTerminal().getOwnOrParentSecurityProfileId();
                incomingKeySet = incomingMessage.getEndPointTerminal().getKeySet();

                //cmsProductId = "0001";
                //m.rehman: adding check for wallet
                if (ifx.getCmsCardRelation() != null) {
                    cmsProductId = ifx.getCmsCardRelation().getProductId(); //Raza Fetch Product Id from Ifx
                    cardAuth = ifx.getCmsCardRelation().getCardAuth();
                    cmsCard = ifx.getCmsCardRelation().getCard();

                }  else {
                    cmsProductId = ifx.getWalletCardRelation().getProductId();
                    cardAuth = ifx.getWalletCardRelation().getCardAuth();
                    walletCard = ifx.getWalletCardRelation().getCard();
                }

                //pinOffset = "12345";
                pinOffset = cardAuth.getEncryptedPin(); //Raza Fetch PIN Offset from IFX

                //validating pin if required
                if (ifx.getCardAuthFlags().getPINRequiredFlag()) {

                    PINValidation(ifx, incomingKeySet, incomingProfileId, pinOffset);

                    respCode = ifx.getRsCode();
                    if (!Util.hasText(ifx.getRsCode())) {
                        logger.error("No response received. HSM Time Out");
                        ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                        throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");

                    } else if (respCode.equals(ISOResponseCodes.LIMIT_EXCEEDED)) {
                        logger.error("HSM Respond with PIN Verification Failure: " + respCode);
                        remRetries = Integer.parseInt(cardAuth.getRemainingRetries());
                        remRetries--;
                        if (remRetries <= 0) {
                            //set card status to warm, which is 01
                            cardAuth.setStatus("01");
                            cardAuth.setReasonCode("0100");

                            //set card status
                            if (cmsCard != null)
                                cmsCard.setCardStatus("01");
                            else if (walletCard != null)
                                walletCard.setCardStatus("01");

                            ifx.setRsCode(ISOResponseCodes.TRANSACTION_CODE_MISMATCH);
                        } else {
                            ifx.setRsCode(ISOResponseCodes.HOST_LINK_DOWN);
                        }
                        cardAuth.setRemainingRetries(remRetries.toString());
                        respCode = ifx.getIfxRsCode();

                        GeneralDao.Instance.saveOrUpdate(cardAuth);

                        throw new PINValidationException("PIN Validation Failed with response code: " + respCode);

                    } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                        logger.error("HSM Respond with: " + respCode);
                        respCode = ISOResponseCodes.INCORRECT_PIN_LENGTH;
                        ifx.setRsCode(respCode);
                        throw new PINValidationException("PIN Validation Failed with response code: " + respCode);

                    } else {
                        logger.info("PIN Validated Successfully!!!");
                        cardAuth.setRemainingRetries(cardAuth.getMaximumRetries());
                        GeneralDao.Instance.saveOrUpdate(cardAuth);
                    }
                }

                //validating CVV if required
                if (ifx.getCardAuthFlags().getCVVRequiredFlag()) {
                    cmsProductKeys = processContext.getCMSProductKeys(cmsProductId);
                    CVVValidation(ifx, cmsProductKeys, Boolean.TRUE);

                    respCode = ifx.getRsCode();
                    if (!Util.hasText(ifx.getRsCode())) {
                        logger.error("No response received. HSM Time Out");
                        ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                        throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");

                    } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                        logger.error("CVV Validation Failed with response code: " + respCode);
                        ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                        throw new CVVValidationException("CVV Validation Failed with response code: " + respCode);

                    } else {
                        logger.info("CVV Validated Successfully!!!");
                    }
                }

                //validating CVV2 if required
                if (ifx.getCardAuthFlags().getCVV2RequiredFlag()) {
                    cmsProductKeys = processContext.getCMSProductKeys(cmsProductId);
                    CVVValidation(ifx, cmsProductKeys, Boolean.FALSE);

                    if (ifx.getCardAuthFlags().getCVVRequiredFlag()) {
                        cmsProductKeys = processContext.getCMSProductKeys(cmsProductId);
                        CVVValidation(ifx, cmsProductKeys, Boolean.TRUE);

                        respCode = ifx.getRsCode();
                        if (!Util.hasText(ifx.getRsCode())) {
                            logger.error("No response received. HSM Time Out");
                            ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                            throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");

                        } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                            logger.error("CVV2 Validation Failed with response code: " + respCode);
                            ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                            throw new CVVValidationException("CVV2 Validation Failed with response code: " + respCode);

                        } else {
                            logger.info("CVV2 Validated Successfully!!!");
                        }
                    }
                }
            } else {
                logger.info("HSM Authorization not Required");
            }
        } catch(Exception e) {
            throw e;
        }
    }

    public void ValidateOffUsCardInfo (ProcessContext processContext) throws Exception {
        Ifx ifx;
        String respCode, oldPinBlock, newPinBlock;
        Long incomingProfileId, outgoingProfileId;
        Set<SecureKey> incomingKeySet, outgoingKeySet;
        Message incomingMessage;
        Channel outChannel;
        Terminal endPointTerminal;

        try {

            incomingMessage = processContext.getInputMessage();
            outChannel = (Channel) processContext.getOutputChannel("out");
            ifx = incomingMessage.getIfx();

            incomingProfileId = incomingMessage.getEndPointTerminal().getOwnOrParentSecurityProfileId();
            incomingKeySet = incomingMessage.getEndPointTerminal().getKeySet();
            endPointTerminal = processContext.getIssuerSwitchTerminal(processContext.getInstitution(
                    outChannel.getInstitutionId()));
            outgoingProfileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            outgoingKeySet = endPointTerminal.getKeySet();

            oldPinBlock = ifx.getPINBlock();

            if (oldPinBlock == null || "".equals(oldPinBlock)) {
                logger.error("PIN Block field is empty. Returning back.");
                return;
            }

            PINTranslation(ifx, incomingKeySet, incomingProfileId, outgoingKeySet, outgoingProfileId);

            newPinBlock = ifx.getNewPINBlock();
            respCode = ifx.getRsCode();

            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Translation Failed with response code: " + respCode);
                ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                throw new PINTranslationException("PIN Translation Failed with response code: " + respCode);
            } else {
                ifx.setPINBlock(newPinBlock);
                logger.info("PIN Translated Successfully!!!");
            }
        } catch(Exception e) {
            throw e;
        }
    }

    public void PINTranslation (Ifx ifx, Set<SecureKey> sourceKeySet, Long sourceProfileId,
                                Set<SecureKey> destinationKeySet, Long destinationProfileId)
            throws Exception {

        String sourceKey, destinationKey, hsmResponse, updateHsmResponse, respCode, oldPinBlock, acctNo,
                newPinBlock, hsmType, sourcePinFormat, destinationPinFormat, type;
        SecureDESKey sourceDESKey, destinationDESKey;
        byte[] hsmResponseInBytes, command;
        SecurityFunction sourcePinSecFunc, destinationPinSecFunc;

        try {

            sourceDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, sourceKeySet);
            if (sourceDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                sourceDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, sourceKeySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            sourceKey = sourceDESKey.getKeyBytes();
            hsmType = sourceDESKey.getHsmType();
            destinationDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, destinationKeySet);
            destinationKey = destinationDESKey.getKeyBytes();

            //set pin format if security profile available. Please note that no security profile available for POS
            if (sourceProfileId != null && destinationProfileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(sourceProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                destinationPinSecFunc = SecurityService.findSecurityFunction(destinationProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");
                destinationPinFormat = destinationPinSecFunc.getParameterValue("PIN Format");
                logger.info("Source and Destination Security Profile are available. Setting values");

            } else if (sourceProfileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(sourceProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");
                destinationPinFormat = sourcePinSecFunc.getParameterValue("PIN Format");
                logger.info("Only Source Security Profile is available. Setting values");

            } else if (destinationProfileId != null) {
                destinationPinSecFunc = SecurityService.findSecurityFunction(destinationProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                sourcePinFormat = destinationPinSecFunc.getParameterValue("PIN Format");
                destinationPinFormat = destinationPinSecFunc.getParameterValue("PIN Format");
                logger.info("Only Destination Security Profile is available. Setting values");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
                destinationPinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
                logger.info("Source and Destination Security Profile are not available. Setting default values");
            }

            acctNo = ifx.getAppPAN();
            oldPinBlock = ifx.getPINBlock();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINTranslation(oldPinBlock, acctNo, sourceKey,
                        sourcePinFormat, destinationKey, destinationPinFormat);

                //TODO: comment below logging
                logger.debug("Safenet command [" + command + "]");

                //send and receive message from HSM
                logger.info("HSM for bin [" + ProcessContext.get().getMyInstitution().getBin().toString() + "]"); //Raza TEMP
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                        ProcessContext.get().getMyInstitution().getBin().toString()));

//                hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
//                        ProcessContext.get().getMyInstitution().getBin().toString());

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    logger.debug("updateHsmResponse without Header [" + updateHsmResponse + "]");
                    logger.info("ResponseCode [" +  updateHsmResponse.substring(6, 8) + "]");
                    respCode = updateHsmResponse.substring(6, 8);

                    if (respCode.equals(ISOResponseCodes.APPROVED)) {
                        newPinBlock = updateHsmResponse.substring(8, 24);
                    } else {
                        newPinBlock = null;
                    }

                    logger.info("HardwareSecurityModule:: IFX-respCode [" + ifx.getRsCode() + "]"); //Raza TEMP
                    if(ifx.getRsCode() != null)
                    {
                    if (ifx.getRsCode().equals(ISOResponseCodes.APPROVED) || ifx.getRsCode().equals("")) {
                        ifx.setRsCode(respCode); //Only Update Response Code it is OK or not set
                    }
                    } else {
                        ifx.setRsCode(respCode); //Update PIN Translation Fail Response. PIN Translation comes before sending to Channel
                    }

                    ifx.setNewPINBlock(newPinBlock);
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINTranslation(oldPinBlock, acctNo, sourceKey, sourcePinFormat,
                        destinationKey, destinationPinFormat, type);

                //TODO: comment below logging
                logger.debug("Thales command [" + command + "]");

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);

                    if (respCode.equals(ISOResponseCodes.APPROVED)) {
                        newPinBlock = hsmResponse.substring(10, 26);
                    } else {
                        newPinBlock = null;
                    }

                    ifx.setRsCode(respCode);
                    ifx.setNewPINBlock(newPinBlock);
                }
            }
            else {
                logger.error("No HSM Type Available");
                throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }
        }
        catch (Exception e) {
            logger.error("HSM Channel Not Available" + e.getMessage());
            throw e;
        }
    }

    public SecureDESKey TranslateKey (Set<SecureKey> keySet, String newKey, String keyType, Ifx ifx)
            throws Exception {

        byte[] hsmResponseInBytes, command;
        String masterKey, hsmResponse, updateHsmResponse, newHSMKey, newKeyCheckValue,
                respCode, hsmType;

        SecureDESKey masterDESKey, zpkDESKey;
        Integer keyLength;

        try {
            newKeyCheckValue = null;
            respCode = null;

            masterDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZMK, keySet);
            masterKey = masterDESKey.getKeyBytes().toString();
            hsmType = masterDESKey.getHsmType();
            zpkDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK_PAS, keySet); //Raza changing to ZPK passive, as this should be updated

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().TranslateKey(masterKey, keyType, newKey);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);

                    if (respCode.equals(ISOResponseCodes.APPROVED)) {
                        keyLength = (Integer.parseInt(updateHsmResponse.substring(10, 12), 16) * 2) - 2;
                        newHSMKey = updateHsmResponse.substring(14, 14 + keyLength);

                        zpkDESKey.setKeyBytes(newHSMKey);

                        if(newKeyCheckValue != null) { //Raza adding null check, as KCV is not used
                            zpkDESKey.setKeyCheckValue(newKeyCheckValue);
                        }
                        else
                        {
                            zpkDESKey.setKeyCheckValue("");
                        }
                        return zpkDESKey; //Raza return new Key in order to update in DB for respected terminal
                    }
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().TranslateKey(masterKey, keyType, newKey);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);

                    if (respCode.equals(ISOResponseCodes.APPROVED)) {
                        newHSMKey = hsmResponse.substring(8, 9 + newKey.length());
                        newKeyCheckValue = hsmResponse.substring(hsmResponse.length() - 6, hsmResponse.length());

                        if (newHSMKey.length() > 32)
                            newHSMKey = newHSMKey.substring(1, newHSMKey.length());

                        zpkDESKey.setKeyBytes(newHSMKey);

                        if(newKeyCheckValue != null) { //Raza adding null check, as KCV is not used
                            zpkDESKey.setKeyCheckValue(newKeyCheckValue);
                        }
                        return zpkDESKey; //Raza return new Key in order to update in DB for respected terminal
                    }
                }
            } else {
                logger.error("No HSM Type Available");
                throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            ifx.setRsCode(respCode);

        } catch (Exception e) {
            logger.error("HSM Channel not Available" + e.getMessage());
            throw e;
        }
        return null;
    }

    public void PINValidation (Ifx ifx, Set<SecureKey> sourceKeySet, Long sourceProfileId, String pinOffset)
            throws Exception {

        String key, hsmResponse, updateHsmResponse, respCode, pinBlock, acctNo, hsmType, sourcePinFormat, type;
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;

        try {

            //if source security profile is not available, set default values
            if (sourceProfileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(sourceProfileId,
                        SecurityComponent.FUNC_VALIDATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, sourceKeySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, sourceKeySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = ifx.getAppPAN();
            pinBlock = ifx.getPINBlock();

            //m.rehman: decrypting card number
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
            acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
            /////////////////////////////////////////////////////////////////////////////

            if (!Util.hasText(acctNo)) {
                logger.error("Card No decryption fail, rejecting...");
                throw new Exception("Card No decryption fail");
            }

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINValidation(pinBlock, pinOffset, acctNo, key,
                        sourcePinFormat);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {

                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    ifx.setRsCode(respCode); //Update PIN Translation Fail Response. PIN Translation comes before sending to Channel
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, pinOffset, acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    ifx.setRsCode(respCode);
                }
            } else {
                logger.error("No HSM Type Available");
                throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Validation: " + e.getMessage());
            throw e;
        }
    }

    public void CVVValidation(Ifx ifx, List<CMSProductKeys> cmsProductKeys, Boolean cvvFlag)
            throws Exception {

        String cvv, serviceCode, acctNo, expiry, hsmType, keySchemeCVKA, keySchemeCVKB, key, hsmResponse, respCode,
                updateHsmResponse;
        CMSProductKeys cvka, cvkb;
        byte[] command, hsmResponseInBytes;

        try {

            acctNo = ifx.getAppPAN();
            expiry = ifx.getExpDt().toString();

            cvka = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKA, cmsProductKeys);
            keySchemeCVKA = cvka.getKeyScheme();
            cvkb = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKB, cmsProductKeys);
            keySchemeCVKB = cvkb.getKeyScheme();
            hsmType = cvka.getHsmType();
            key = null;
            command = null;

            if (cvvFlag.equals(Boolean.TRUE)) {    //1 - cvv
                cvv = ifx.getCardAcctId().getCVV();
                serviceCode = ifx.getCardAcctId().getServiceCode();
            } else {
                cvv = ifx.getCardAcctId().getCVV2();
                //for cvv2 validation, all parameters are same except service code and expiry, so changing service code and expiry
                serviceCode = "000";
                expiry = expiry.substring(2,4) + expiry.substring(0,2);
            }

            if (hsmType == null)
                throw new CVVValidationException("HSM Type is null");

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {

                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if ((keySchemeCVKA.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC) &&
                            keySchemeCVKB.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC))
                            ||
                            keySchemeCVKA.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_EBC) &&
                                    keySchemeCVKB.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_EBC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = SafeNetHSMHandler.getInstance().CVVValidation(key, cvv, acctNo, expiry, serviceCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    ifx.setRsCode(respCode);
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {

                //if key is double length, append key specifier and merge cvka and cvkb
                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if (keySchemeCVKA.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC) &&
                            keySchemeCVKB.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = ThalesHSMHandler.getInstance().CVVValidation(key, cvv, acctNo, expiry, serviceCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    ifx.setRsCode(respCode);
                }
            } else {
                logger.error("No HSM Type Available");
                throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": CVV/CVV2 Validation: " + e.getMessage());
            throw e;
        }
    }

    public void PINChange(ProcessContext processContext) throws Exception {
        Ifx ifx;
        String appPanIMD, respCode, newPinBlock, key, hsmResponse, updateHsmResponse, acctNo, hsmType, pinFormat,
                newEncryptedPIN;
        //Boolean isLocalFlag;
        Long profileId;
        Set<SecureKey> keySet;
        Message incomingMessage;
        SecureDESKey fromDESKey;
        SecurityFunction pinSecFunc;
        byte[] hsmResponseInBytes, command;
        CMSCardAuthorization cardAuth;
        IMDType type;


        try {

            incomingMessage = processContext.getInputMessage();
            ifx = incomingMessage.getIfx();

            if (ifx.getCmsCardRelation() != null)
                cardAuth = ifx.getCmsCardRelation().getCardAuth();
            else
                cardAuth = ifx.getWalletCardRelation().getCardAuth();

            //flag true for On-Us and false for Off-Us
            appPanIMD = ifx.getAppPAN().substring(0, 11);
            //isLocalFlag = CMSDBOperations.Instance.getIMDType(appPanIMD);
            type = CMSDBOperations.Instance.getIMDType(appPanIMD);

            //if (isLocalFlag.equals(Boolean.TRUE)) {
            if (type != null && type.equals(IMDType.Local)) {

                profileId = incomingMessage.getEndPointTerminal().getOwnOrParentSecurityProfileId();
                keySet = incomingMessage.getEndPointTerminal().getKeySet();
                pinSecFunc = SecurityService.findSecurityFunction(profileId, SecurityComponent.FUNC_CHANGEPIN);
                pinFormat = pinSecFunc.getParameterValue("PIN Format");

                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
                key = fromDESKey.getKeyBytes();

                hsmType = fromDESKey.getHsmType();

                acctNo = ifx.getAppPAN();
                newPinBlock = ifx.getNewPINBlock();
                newEncryptedPIN = cardAuth.getEncryptedPin();

                //m.rehman: decrypting card number
                //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
                //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
                acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
                /////////////////////////////////////////////////////////////////////////

                if (!Util.hasText(acctNo)) {
                    logger.error("Card No decryption fail, rejecting...");
                    throw new Exception("Card No decryption fail");

                }

                if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                    command = SafeNetHSMHandler.getInstance().PINChange(newPinBlock, acctNo, key, pinFormat);

                    //send and receive message from HSM
                    hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                            processContext.getMyInstitution().getBin().toString()));

                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                        respCode = updateHsmResponse.substring(6, 8);
                        newEncryptedPIN = hsmResponse.substring(8, hsmResponse.length());
                        ifx.setRsCode(respCode);
                    }

                } else if (hsmType.equals(CommandType.THALES.toString())) {
                    command = ThalesHSMHandler.getInstance().PINChange(newPinBlock, acctNo, key, pinFormat);

                    //send and receive message from HSM
                    hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                            processContext.getMyInstitution().getBin().toString()));
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        respCode = hsmResponse.substring(6, 8);
                        newEncryptedPIN = hsmResponse.substring(8, hsmResponse.length());
                        ifx.setRsCode(respCode);
                    }
                } else {
                    logger.error("No HSM Type Available");
                    throw new Exception("No HSM Type Available");
                }

                respCode = ifx.getRsCode();
                if (!Util.hasText(ifx.getRsCode())) {
                    logger.error("No response received. HSM Time Out");
                    ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                    throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");

                } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                    logger.error("PIN Change Failed with response code: " + respCode);
                    ifx.setRsCode(ISOResponseCodes.INVALID_CARD);
                    throw new PINChangeException("PIN Change Failed with response code: " + respCode);

                } else {
                    logger.info("PIN Change Successfully!!!");

                    cardAuth.setEncryptedPin(newEncryptedPIN);
                    cardAuth.setReasonCode("0000");
                    cardAuth.setStatus("00");
                    cardAuth.setRemainingRetries(cardAuth.getMaximumRetries());

                    GeneralDao.Instance.saveOrUpdate(cardAuth);
                }
            } else {
                logger.error("PIN Change not allowed on Off-Us Card");
                throw new PINChangeException("PIN Change not allowed on Off-Us Card");
            }
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Change: " + e.getMessage());
            throw e;
        }
    }

    public void CVVGeneration(Ifx ifx, List<CMSProductKeys> cmsProductKeys, Boolean cvvFlag)
            throws Exception {

        String cvv, serviceCode, acctNo, expiry, hsmType, keySchemeCVKA, keySchemeCVKB, key, hsmResponse, respCode,
                updateHsmResponse;
        CMSProductKeys cvka, cvkb;
        byte[] command, hsmResponseInBytes;

        try {

            acctNo = ifx.getAppPAN();
            expiry = ifx.getExpDt().toString();

            cvka = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKA, cmsProductKeys);
            keySchemeCVKA = cvka.getKeyScheme();
            cvkb = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKB, cmsProductKeys);
            keySchemeCVKB = cvkb.getKeyScheme();
            hsmType = cvka.getHsmType();
            key = null;
            command = null;

            if (cvvFlag.equals(Boolean.TRUE)) {
                serviceCode = ifx.getCardAcctId().getServiceCode();
            } else {
                //for cvv2 validation, all parameters are same except service code, so changing service code
                serviceCode = "000";
            }

            if (hsmType == null)
                throw new CVVValidationException("HSM Type is null");

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {

                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if ((keySchemeCVKA.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC) &&
                            keySchemeCVKB.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC))
                            ||
                            keySchemeCVKA.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_EBC) &&
                                    keySchemeCVKB.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_EBC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = SafeNetHSMHandler.getInstance().CVVGeneration(key, acctNo, expiry, serviceCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    ifx.setRsCode(respCode);
                    if (hsmResponse.length() > 8) {
                        cvv = hsmResponse.substring(8, 11);
                        if (Util.hasText(cvv))
                            ifx.getSafeCardAcctId().setCVV(cvv);
                    }
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {

                //if key is double length, append key specifier and merge cvka and cvkb
                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if (keySchemeCVKA.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC) &&
                            keySchemeCVKB.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = ThalesHSMHandler.getInstance().CVVGeneration(key, acctNo, expiry, serviceCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    ifx.setRsCode(respCode);
                    if (hsmResponse.length() > 8) {
                        cvv = hsmResponse.substring(8, 11);
                        if (Util.hasText(cvv))
                            ifx.getSafeCardAcctId().setCVV(cvv);
                    }
                }
            } else {
                logger.error("No HSM Type Available");
                throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": CVV/CVV2 Validation: " + e.getMessage());
            throw e;
        }
    }

    public boolean PINValidation (WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse, updateHsmResponse, respCode, pinBlock, acctNo, hsmType, sourcePinFormat, type, tmk;
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {

            if (cardrelation == null) //Raza Also Check Here Secure_Secure :D
            {
                logger.error("CardAuth not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_NOT_FOUND); //93-CustomerRelationNotFound ; Refer to Doc
                //throw new Exception("CardAuth not found");
                return false;
            } else if (!cardrelation.getAccount().getStatus().equals("00")) {
                if (cardrelation.getAccount().getStatus().equals("90")) {
                    logger.error("Account in Blocked State, rejcting...");
                    wsmodel.setRespcode(ISOResponseCodes.NP_SRC_BLOCKED); //67-InvalidAccountStatus ; Refer to Doc
                    return false;
                } else if (cardrelation.getAccount().getStatus().equals("03")) {
                    logger.error("Account in Warm State, rejcting...");
                    wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LOCKED); //67-InvalidAccountStatus ; Refer to Doc
                    return false;
                } else {
                    logger.error("Invalid Account Auth Status [ " + cardrelation.getAccount().getStatus() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INVALID_STATE); //67-InvalidAccountStatus ; Refer to Doc
                    return false;
                }
            } else if (Long.parseLong(cardrelation.getRemRetries()) <= 0) {
                logger.error("PIN Remaing Retries Exhausted, updating staus at warm, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_PIN_RETRIES_EXHUASTED); //67-InvalidAccountStatus ; Refer to Doc
                cardrelation.getAccount().setStatus("03"); //Raza - 03 for TempBlock/Warm
                GeneralDao.Instance.saveOrUpdate(cardrelation.getAccount());
                return false;
            }

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();

            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_VALIDATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = cardrelation.getRelation();
            if (acctNo.contains("=")) //Raza adding if Relationshipid is of CardNumber
            {
                acctNo = acctNo.substring(0, acctNo.indexOf('='));
            }

            if (Util.hasText(wsmodel.getPindata())) {
                pinBlock = wsmodel.getPindata();
            } else {
                pinBlock = wsmodel.getCardpindata();
            }

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINValidation(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    wsmodel.setRespcode(respCode);
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINValidation(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    wsmodel.setRespcode(respCode);
                }

            } else if (hsmType.equals(CommandType.ATALLA.toString())) {
                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINValidation(pinBlock.toUpperCase(), cardrelation.getOffset(), acctNo, tmk,
                        key, Integer.toString(cardrelation.getOffset().length()), sequence);
                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.info("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_VERIFY_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            } else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
                return false;
            }

            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Validation Failed with response code: " + respCode);
                cardrelation.setRemRetries((Long.parseLong(cardrelation.getRemRetries()) - 1) + "");
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                if (Long.parseLong(cardrelation.getRemRetries()) == 0) {
                    logger.error("Pin Retries exhausted, rejecting...");
                    cardrelation.getAccount().setStatus("03");
                    GeneralDao.Instance.saveOrUpdate(cardrelation.getAccount());
                    wsmodel.setRespcode(ISOResponseCodes.NP_PIN_RETRIES_EXHUASTED); //60-PIN RETRIES EXHAUSTED ; Refer to Doc
                    return false;
                }
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN);
                return false;
                //throw new PINChangeException("PIN Validation Failed with response code: " + respCode);

            } else {
                logger.info("PIN Validated Successfully!!!");

                cardrelation.setRemRetries(cardrelation.getMaxRetries());
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                return true;
            }

        } catch (Exception e) {
            logger.error("Exception caught while validating wallet pin!");
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            logger.error(WebServiceUtil.getStrException(e));
            return false;
            //throw e;
        }
    }

    public boolean PINChange (WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType, sourcePinFormat,
                type, tmk, oldPinBlock, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {
            //Raza not checking CardRelation as
            /*if (cardrelation == null || cardrelation.getCardAuth() == null) //Raza Also Check Here Secure_Secure :D
            {
                logger.error("CardRelation or CardAuth not found, rejecting...");
                throw new Exception("CardAuth not found");
            }*/

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();



            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = cardrelation.getRelation();
            pinBlock = wsmodel.getNewpindata();
            /*if(wsmodel.getServicename().equals("ChangeWalletPin") || wsmodel.getServicename().equals("ChangeDebitCardPin")) { //Raza for PIN Change Request
                pinBlock = wsmodel.getOldpindata();
            }
            else
            {
                pinBlock = wsmodel.getPindata();
            }*/

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key,
                        sourcePinFormat);

                //logger.info("HSM Command [" + command + "]");

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                oldPinBlock = wsmodel.getPindata();
                if (!Util.hasText(oldPinBlock)) {
                    oldPinBlock = "";
                }

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINChange(oldPinBlock.toUpperCase(), pinBlock.toUpperCase(), cardrelation.getOffset(), acctNo, tmk,
                        key, Integer.toString(cardrelation.getOffset().length()), sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_CHANGE_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(6, 6 + cardrelation.getOffset().length());
                            } else if (hsmResponse.substring(4, 6).equals("NO")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(7, 7 + cardrelation.getOffset().length());
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Change Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN);
                //throw new PINChangeException("PIN Change Failed with response code: " + respCode);
                return false;

            } else {
                logger.info("PIN Changed Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");


                cardrelation.setRemRetries(cardrelation.getMaxRetries());
                //cardrelation.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                cardrelation.setOffset(newOffset);
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Change: " + e.getMessage());
            e.printStackTrace();
            return false;
            //throw e;
        }
    }

    public boolean PINChange (WalletCMSWsEntity wsmodel, List<CMSAuth> listcardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType, sourcePinFormat,
                type, tmk, oldPinBlock, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {
            //Raza not checking CardRelation as
            /*if (cardrelation == null || cardrelation.getCardAuth() == null) //Raza Also Check Here Secure_Secure :D
            {
                logger.error("CardRelation or CardAuth not found, rejecting...");
                throw new Exception("CardAuth not found");
            }*/

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();



            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = listcardrelation.get(0).getRelation();
            pinBlock = wsmodel.getNewpindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key,
                        sourcePinFormat);

                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, listcardrelation.get(0).getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                oldPinBlock = wsmodel.getPindata();
                if (!Util.hasText(oldPinBlock)) {
                    oldPinBlock = "";
                }

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINChange(oldPinBlock.toUpperCase(), pinBlock.toUpperCase(), listcardrelation.get(0).getOffset(),
                        acctNo, tmk, key, Integer.toString(listcardrelation.get(0).getOffset().length()), sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_CHANGE_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(6, 6 + listcardrelation.get(0).getOffset().length());
                            } else if (hsmResponse.substring(4, 6).equals("NO")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(7, 7 + listcardrelation.get(0).getOffset().length());
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Change Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN);
                //throw new PINChangeException("PIN Change Failed with response code: " + respCode);
                return false;

            } else {
                logger.info("PIN Changed Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");

                for(CMSAuth cr : listcardrelation)
                {
                    cr.setRemRetries(cr.getMaxRetries());
                    //cr.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                    cr.setOffset(newOffset);
                    cr.setStatus("00");
                    cr.setReasonCode("0000");
                    GeneralDao.Instance.saveOrUpdate(cr);
                }
                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Change: " + e.getMessage());
            e.printStackTrace();
            return false;
            //throw e;
        }
    }

    public boolean PINTranslation (WalletCMSWsEntity wsmodel, Set<SecureKey> sourceKeySet, Long sourceProfileId,
                                Set<SecureKey> destinationKeySet, Long destinationProfileId)
            throws Exception {

        String sourceKey, destinationKey, hsmResponse, updateHsmResponse, respCode, oldPinBlock, acctNo,
                newPinBlock, hsmType, sourcePinFormat, destinationPinFormat, type;
        SecureDESKey sourceDESKey, destinationDESKey;
        byte[] hsmResponseInBytes, command;
        SecurityFunction sourcePinSecFunc, destinationPinSecFunc;

        try {

            sourceDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, sourceKeySet);
            if (sourceDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                sourceDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, sourceKeySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            sourceKey = sourceDESKey.getKeyBytes();
            hsmType = sourceDESKey.getHsmType();
            destinationDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, destinationKeySet);
            destinationKey = destinationDESKey.getKeyBytes();

            //set pin format if security profile available. Please note that no security profile available for POS
            if (sourceProfileId != null && destinationProfileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(sourceProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                destinationPinSecFunc = SecurityService.findSecurityFunction(destinationProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");
                destinationPinFormat = destinationPinSecFunc.getParameterValue("PIN Format");
                logger.info("Source and Destination Security Profile are available. Setting values");

            } else if (sourceProfileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(sourceProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");
                destinationPinFormat = sourcePinSecFunc.getParameterValue("PIN Format");
                logger.info("Only Source Security Profile is available. Setting values");

            } else if (destinationProfileId != null) {
                destinationPinSecFunc = SecurityService.findSecurityFunction(destinationProfileId,
                        SecurityComponent.FUNC_TRANSLATEPIN);
                sourcePinFormat = destinationPinSecFunc.getParameterValue("PIN Format");
                destinationPinFormat = destinationPinSecFunc.getParameterValue("PIN Format");
                logger.info("Only Destination Security Profile is available. Setting values");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
                destinationPinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
                logger.info("Source and Destination Security Profile are not available. Setting default values");
            }

            acctNo = wsmodel.getCardnumber();
            oldPinBlock = wsmodel.getPindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINTranslation(oldPinBlock, acctNo, sourceKey,
                        sourcePinFormat, destinationKey, destinationPinFormat);

                //TODO: comment below logging
                logger.debug("Safenet command [" + command + "]");

                //send and receive message from HSM
                logger.info("HSM for bin [" + ProcessContext.get().getMyInstitution().getBin().toString() + "]"); //Raza TEMP
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                        ProcessContext.get().getMyInstitution().getBin().toString()));

//                hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
//                        ProcessContext.get().getMyInstitution().getBin().toString());

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    logger.debug("updateHsmResponse without Header [" + updateHsmResponse + "]");
                    logger.info("ResponseCode [" +  updateHsmResponse.substring(6, 8) + "]");
                    respCode = updateHsmResponse.substring(6, 8);

                    if (respCode.equals(ISOResponseCodes.APPROVED)) {
                        newPinBlock = updateHsmResponse.substring(8, 24);
                    } else {
                        newPinBlock = null;
                    }

                    logger.info("HardwareSecurityModule:: respCode [" + wsmodel.getRespcode() + "]"); //Raza TEMP
                    if(wsmodel.getRespcode() != null)
                    {
                        if (wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED) || !Util.hasText(wsmodel.getRespcode())) {
                            wsmodel.setRespcode(respCode); //Only Update Response Code it is OK or not set
                        }
                    } else {
                        wsmodel.setRespcode(respCode); //Update PIN Translation Fail Response. PIN Translation comes before sending to Channel
                    }

                    wsmodel.setPindata(newPinBlock);
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINTranslation(oldPinBlock, acctNo, sourceKey, sourcePinFormat,
                        destinationKey, destinationPinFormat, type);

                //TODO: comment below logging
                logger.debug("Thales command [" + command + "]");

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);

                    if (respCode.equals(ISOResponseCodes.APPROVED)) {
                        newPinBlock = hsmResponse.substring(10, 26);
                    } else {
                        newPinBlock = null;
                    }

                    wsmodel.setRespcode(respCode);
                    wsmodel.setPindata(newPinBlock);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {
                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINTranslation(oldPinBlock.toUpperCase(), acctNo, sourceKey, destinationKey, sequence);

                //send and receive message from HSM
                //logger.info("HSM for bin [" + ProcessContext.get().getMyInstitution().getBin().toString() + "]"); //Raza TEMP
                //hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString()));
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());

                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");

                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_TRANSLATE_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                newPinBlock = hsmResponse.substring(6, 22);
                                respCode = ISOResponseCodes.APPROVED;
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                                newPinBlock = "";
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                            newPinBlock = "";
                        }
                        wsmodel.setRespcode(respCode);
                        wsmodel.setPindata(newPinBlock);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                return false;
            }

            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Translation Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN);
                return false;

            } else {
                logger.info("PIN Translation Successfully!!!");
                return true;
            }
        }
        catch (Exception e) {
            logger.error("HSM Channel Not Available" + e.getMessage());
            return false;
        }
    }

    public boolean CardPINValidation (WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse, updateHsmResponse, respCode, pinBlock, acctNo, hsmType, sourcePinFormat, type, tmk;
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;
        String institutionCode = "";

        try {
            if (cardrelation == null) //Raza Also Check Here Secure_Secure :D
            {
                logger.error("CardRelation or CardAuth not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_NOT_FOUND); //08-InvalidCardRecord ; Refer to Doc
                return false;
            }
            else if(!cardrelation.getCard().getCardStatus().equals("00"))
            {
                if(cardrelation.getCard().getCardStatus().equals("90"))
                {
                    logger.error("Card in Blocked State, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NP_SRC_BLOCKED); //15-Hot Card ; Refer to Doc
                    return false;
                }
                else if(cardrelation.getCard().getCardStatus().equals("03"))
                {
                    logger.error("Card in Warm State, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LOCKED); //14-Warm Card ; Refer to Doc
                    return false;
                }
                else
                {
                    logger.error("Invalid Card Auth Status [ " + cardrelation.getCard().getCardStatus() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INVALID_STATE); //16-BadCardStatus ; Refer to Doc
                    return false;
                }
            }
            //Committed By Huzaifa - 04042024
//            else if(Long.parseLong(cardrelation.getRemRetries()) <= 0)
//            {
//                logger.error("PIN Remaining Retries Exhausted, updating status at warm, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_LOCKED); //14-Warm Card ; Refer to Doc);
//
//                //Arsalan Akhter, Date: 01-Oct-2021, Ticket: VC-NAP-202110011(Temporary block debit card on pin retries exhausted)
//                CMSActivityLog cmsActivityLog;
//                logger.info("Saving card activity ....");
//                cmsActivityLog = new CMSActivityLog();
//                cmsActivityLog.setRelation(cardrelation.getCard().getCardNumber());
//                cmsActivityLog.setPreviousStatus(cardrelation.getCard().getCardStatus());
//
//                cardrelation.getCard().setCardStatus("02"); //Raza - 03 for TempBlock/Warm
//                GeneralDao.Instance.saveOrUpdate(cardrelation.getCard());
//
//                cmsActivityLog.setCurrentStatus(cardrelation.getCard().getCardStatus());
//                cmsActivityLog.setSourceType("API");
//                cmsActivityLog.setSourceName(wsmodel.getServicename());
//                cmsActivityLog.setActivityDateTime(DateTime.now());
//                GeneralDao.Instance.saveOrUpdate(cmsActivityLog);
//                //=====================================================================================================================
//                return false;
//            }

            if(GlobalContext.getInstance().getChannelbyId(wsmodel.getChannelid()) != null)
            {
                institutionCode = (GlobalContext.getInstance().getChannelbyId(wsmodel.getChannelid())).getInstitutionId();
            }
            else {
                institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            }
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();

            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_VALIDATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = cardrelation.getRelation();
            //m.rehman: decrypting card number
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
            acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
            /////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Acct No [" + acctNo + "]");
            if (!Util.hasText(acctNo)) {
                logger.error("Card No decryption fail, rejecting...");
                //throw new Exception("Card No decryption fail");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            if(acctNo.contains("=")) //Raza adding if Relationshipid is of CardNumber
            {
                acctNo = acctNo.substring(0,acctNo.indexOf('='));
            }
			
			//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
            if (Util.hasText(wsmodel.getCardlastdigits()) && !acctNo.substring(acctNo.length()-4, acctNo.length()).equals(wsmodel.getCardlastdigits())) {
            //===============================================================================================================
				logger.error("Card Last 4 digits do not match, rejecting...");
                //throw new Exception("Card Last 4 digits do not match");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            pinBlock = wsmodel.getCardpindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINValidation(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINValidation(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {
                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }
                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINValidation(pinBlock.toUpperCase(), cardrelation.getOffset(), acctNo, tmk,
                        key, Integer.toString(cardrelation.getOffset().length()), sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.info("Response from HSM [" + hsmResponse + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_VERIFY_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
                    else {
                        logger.error("NULL response received from HSM...");
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;

            }
            else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("Card PIN Validation Failed with response code: " + respCode);
                cardrelation.setRemRetries((Long.parseLong(cardrelation.getRemRetries()) -1) + "");
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                if(Long.parseLong(cardrelation.getRemRetries()) == 0)
                {
                    logger.error("Pin Retries exhausted, rejecting...");

                    //Arsalan Akhter, Date: 01-Oct-2021, Ticket: VC-NAP-202110011(Temporary block debit card on pin retries exhausted)
                    CMSActivityLog cmsActivityLog;
                    logger.info("Saving card activity ....");
                    cmsActivityLog = new CMSActivityLog();
                    cmsActivityLog.setRelation(cardrelation.getCard().getCardNumber());
                    cmsActivityLog.setPreviousStatus(cardrelation.getCard().getCardStatus());

                    cardrelation.getCard().setCardStatus("02");
                    GeneralDao.Instance.saveOrUpdate(cardrelation.getCard());

                    cmsActivityLog.setCurrentStatus(cardrelation.getCard().getCardStatus());
                    cmsActivityLog.setSourceType("API");
                    cmsActivityLog.setSourceName(wsmodel.getServicename());
                    cmsActivityLog.setActivityDateTime(DateTime.now());
                    GeneralDao.Instance.saveOrUpdate(cmsActivityLog);
                    //=====================================================================================================================

                    wsmodel.setRespcode(ISOResponseCodes.NP_PIN_RETRIES_EXHUASTED); //60-PIN RETRIES EXHAUSTED ; Refer to Doc
                    return false;
                }
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_CARD_PIN);
                return false;

            }
            else {
                logger.info("Card PIN Validated Successfully!!!");

                cardrelation.setRemRetries(cardrelation.getMaxRetries());
                wsmodel.setRespcode(respCode);
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                return true;
            }


        } catch (Exception e) {
            logger.error("Exception caught while validating card pin!");
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            logger.error(WebServiceUtil.getStrException(e));
            return false;
            //throw e;
        }
    }

    public boolean CardPINChange (WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType, sourcePinFormat,
                type, tmk, oldPinBlock, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();

            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = cardrelation.getRelation();

            //m.rehman: decrypting card number
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
            acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
            /////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Acct No [" + acctNo + "]");
            if (!Util.hasText(acctNo)) {
                logger.error("Card No decryption fail, rejecting...");
                //throw new Exception("Card No decryption fail");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;

            }

            //m.rehman: problem
            Integer index;
            if (acctNo.contains("=") || acctNo.contains("D")) {
                index = acctNo.indexOf("=");
                if (index < 0)
                    index = acctNo.indexOf("D");
                acctNo = acctNo.substring(0, index);
            }

			//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
            if (Util.hasText(wsmodel.getCardlastdigits()) && !acctNo.substring(acctNo.length()-4, acctNo.length()).equals(wsmodel.getCardlastdigits())) {
            //===============================================================================================================
				logger.error("Card Last 4 digits do not match, rejecting...");
                //throw new Exception("Card Last 4 digits do not match");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            pinBlock = wsmodel.getNewpindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key, sourcePinFormat);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                oldPinBlock = wsmodel.getCardpindata();
                if (!Util.hasText(oldPinBlock)) {
                    oldPinBlock = "";
                }

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINChange(oldPinBlock.toUpperCase(), pinBlock.toUpperCase(), cardrelation.getOffset(), acctNo, tmk,
                        key, Integer.toString(cardrelation.getOffset().length()), sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_CHANGE_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(6, 6 + cardrelation.getOffset().length());
                            } else if (hsmResponse.substring(4, 6).equals("NO")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(7, 7 + cardrelation.getOffset().length());
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("Card PIN Change Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_CARD_PIN);
                //throw new PINChangeException("Card PIN Change Failed with response code: " + respCode);
                return false;

            } else {
                logger.info("Card PIN Changed Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");


                cardrelation.setRemRetries(cardrelation.getMaxRetries());
                //cardrelation.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                cardrelation.setOffset(newOffset);
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": Card PIN Change: " + e.getMessage());
            e.printStackTrace();
            return false;
            //throw e;
        }
    }

    public boolean CardPINChange (WalletCMSWsEntity wsmodel, List<CMSAuth> listcardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode="", pinBlock, acctNo, hsmType, sourcePinFormat,
                type, tmk, oldPinBlock, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();

            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = listcardrelation.get(0).getRelation();
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
            acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
            /////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Acct No [" + acctNo + "]");
            if (!Util.hasText(acctNo)) {
                logger.error("Card No decryption fail, rejecting...");
                //throw new Exception("Card No decryption fail");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;

            }
            //m.rehman: problem
            Integer index;
            if (acctNo.contains("=") || acctNo.contains("D")) {
                index = acctNo.indexOf("=");
                if (index < 0)
                    index = acctNo.indexOf("D");
                acctNo = acctNo.substring(0, index);
            }
            pinBlock = wsmodel.getNewpindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key, sourcePinFormat);

                //logger.debug("HSM Command [" + command + "]");

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    logger.debug("HSM Response: [" + updateHsmResponse + "]");
                    respCode = updateHsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    //wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, listcardrelation.get(0).getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    //wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                oldPinBlock = wsmodel.getCardpindata();
                if (!Util.hasText(oldPinBlock)) {
                    oldPinBlock = "";
                }

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINChange(oldPinBlock.toUpperCase(), pinBlock.toUpperCase(), listcardrelation.get(0).getOffset(), acctNo, tmk,
                        key, Integer.toString(listcardrelation.get(0).getOffset().length()), sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_CHANGE_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(6, 6 + listcardrelation.get(0).getOffset().length());
                            } else if (hsmResponse.substring(4, 6).equals("NO")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(7, 7 + listcardrelation.get(0).getOffset().length());
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            //logger.debug("HSM Response: [" + updateHsmResponse + "]");
            //respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;
            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("Card PIN Change Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_CARD_PIN);
                //throw new PINChangeException("Card PIN Change Failed with response code: " + respCode);
                return false;
            } else {
                logger.info("Card PIN Changed Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");

                for(CMSAuth cr : listcardrelation)
                {
                    cr.setRemRetries(cr.getMaxRetries());
                    //cr.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                    cr.setOffset(newOffset);
                    cr.setStatus("00");
                    cr.setReasonCode("0000");
                    GeneralDao.Instance.saveOrUpdate(cr);
                }

                return true;
            }
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": Card PIN Change: " + e.getMessage());
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            return false;
            //throw e;
        }
    }

    public boolean PINGeneration (WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType, sourcePinFormat,
                type, tmk, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {
            //Raza not checking CardRelation as
            /*if (cardrelation == null || cardrelation.getCardAuth() == null) //Raza Also Check Here Secure_Secure :D
            {
                logger.error("CardRelation or CardAuth not found, rejecting...");
                throw new Exception("CardAuth not found");
            }*/

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();



            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = cardrelation.getRelation();
            pinBlock = wsmodel.getNewpindata();
            /*if(wsmodel.getServicename().equals("ChangeWalletPin") || wsmodel.getServicename().equals("ChangeDebitCardPin")) { //Raza for PIN Change Request
                pinBlock = wsmodel.getOldpindata();
            }
            else
            {
                pinBlock = wsmodel.getPindata();
            }*/

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key,
                        sourcePinFormat);

                //logger.debug("HSM Command [" + command + "]");

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, hsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    newOffset = hsmResponse.substring(8, hsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINGeneration(pinBlock.toUpperCase(), acctNo, tmk, key, sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_GENERATE_RESP)) {
                            if (!hsmResponse.substring(4, 5).equals("S") && !hsmResponse.substring(4, 5).equals("L")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(13, hsmResponse.indexOf("#", 13));
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Generation Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN);
                return false;
                //throw new PINChangeException("PIN Validation Failed with response code: " + respCode);

            } else {
                logger.info("PIN Generated Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");

                cardrelation.setRemRetries(cardrelation.getMaxRetries());
                //cardrelation.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                cardrelation.setOffset(newOffset);
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Validation: " + e.getMessage());
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            e.printStackTrace();
            return false;
            //throw e;
        }
    }

    public boolean PINGeneration (WalletCMSWsEntity wsmodel, List<CMSAuth> listcardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType, sourcePinFormat,
                type, prevWalletStatus, prevCusStatus, tmk, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;
        CMSActivityLog cmsActivityLog;

        try {
            //Raza not checking CardRelation as
            /*if (cardrelation == null || cardrelation.getCardAuth() == null) //Raza Also Check Here Secure_Secure :D
            {
                logger.error("CardRelation or CardAuth not found, rejecting...");
                throw new Exception("CardAuth not found");
            }*/

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();



            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = listcardrelation.get(0).getRelation();
            pinBlock = wsmodel.getPindata();


            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key,
                        sourcePinFormat);

                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    logger.debug("Response WithOut Header [" + updateHsmResponse + "]"); //Raza TEMP
                    respCode = updateHsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) { //Raza TODO -- Update Thales Generate/Change PIN
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, listcardrelation.get(0).getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    newOffset = hsmResponse.substring(8, hsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINGeneration(pinBlock.toUpperCase(), acctNo, tmk, key, sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_GENERATE_RESP)) {
                            if (!hsmResponse.substring(4, 5).equals("S") && !hsmResponse.substring(4, 5).equals("L")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(13, hsmResponse.indexOf("#", 13));
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Generation Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN);
                return false;
                //throw new PINChangeException("PIN Generation Failed with response code: " + respCode);

            } else {
                logger.info("PIN Generated Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");

                for(CMSAuth cr : listcardrelation)
                {
                    cr.setRemRetries(cr.getMaxRetries());
                    //cr.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                    cr.setOffset(newOffset);
                    cr.setStatus("00");
                    cr.setReasonCode("0000");
                    GeneralDao.Instance.saveOrUpdate(cr);
                }
                prevWalletStatus = listcardrelation.get(0).getAccount().getStatus();
                prevCusStatus = listcardrelation.get(0).getAccount().getCustomer().getStatus();
                listcardrelation.get(0).getAccount().setStatus("00"); //Same Account for all
                listcardrelation.get(0).getAccount().getCustomer().setStatus("00");
                listcardrelation.get(0).getAccount().setLastUpdateDate(new Date());
                listcardrelation.get(0).getAccount().getCustomer().setLastUpdateDate(new Date());
                GeneralDao.Instance.saveOrUpdate(listcardrelation.get(0).getAccount());
                GeneralDao.Instance.saveOrUpdate(listcardrelation.get(0).getAccount().getCustomer());

                logger.info("Saving wallet activity ....");
                cmsActivityLog = new CMSActivityLog();
                cmsActivityLog.setRelation(listcardrelation.get(0).getAccount().getAccountNumber());
                cmsActivityLog.setPreviousStatus(prevWalletStatus);
                cmsActivityLog.setCurrentStatus(listcardrelation.get(0).getAccount().getStatus());
                cmsActivityLog.setSourceType("API");
                cmsActivityLog.setSourceName(wsmodel.getServicename());
                cmsActivityLog.setActivityDateTime(DateTime.now());
                GeneralDao.Instance.saveOrUpdate(cmsActivityLog);

                logger.info("Saving customer activity ....");
                cmsActivityLog = new CMSActivityLog();
                cmsActivityLog.setRelation(listcardrelation.get(0).getAccount().getCustomer().getCustomerId());
                cmsActivityLog.setPreviousStatus(prevCusStatus);
                cmsActivityLog.setCurrentStatus(listcardrelation.get(0).getAccount().getCustomer().getStatus());
                cmsActivityLog.setSourceType("API");
                cmsActivityLog.setSourceName(wsmodel.getServicename());
                cmsActivityLog.setActivityDateTime(DateTime.now());
                GeneralDao.Instance.saveOrUpdate(cmsActivityLog);

                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Generation: " + e.getMessage());
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            e.printStackTrace();
            return false;
            //throw e;
        }
    }

    public boolean CardPINGeneration (WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType, sourcePinFormat,
                type, tmk, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();



            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId,
                        SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = cardrelation.getRelation();
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
            acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
            /////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Acct No [" + acctNo + "]");
            if (!Util.hasText(acctNo)) {
                logger.error("Card No decryption fail, rejecting...");
                return false;
            }

            //m.rehman: problem
            Integer index;
            if (acctNo.contains("=") || acctNo.contains("D")) {
                index = acctNo.indexOf("=");
                if (index < 0)
                    index = acctNo.indexOf("D");
                acctNo = acctNo.substring(0, index);
            }
			
			//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
            if (Util.hasText(wsmodel.getCardlastdigits()) && !acctNo.substring(acctNo.length()-4, acctNo.length()).equals(wsmodel.getCardlastdigits())) {
            //===============================================================================================================
			    logger.error("Card Last 4 digits do not match, rejecting...");
                //throw new Exception("Card Last 4 digits do not match");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            pinBlock = wsmodel.getPindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key, sourcePinFormat);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) {
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, cardrelation.getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    newOffset = hsmResponse.substring(8, hsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINGeneration(pinBlock.toUpperCase(), acctNo, tmk, key, sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_GENERATE_RESP)) {
                            if (!hsmResponse.substring(4, 5).equals("S") && !hsmResponse.substring(4, 5).equals("L")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(13, hsmResponse.indexOf("#", 13));
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Generation Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_CARD_PIN);
                //throw new PINChangeException("PIN Validation Failed with response code: " + respCode);
                return false;

            } else {
                logger.info("PIN Generated Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");

                cardrelation.setRemRetries(cardrelation.getMaxRetries());
                //cardrelation.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                cardrelation.setOffset(newOffset);
                GeneralDao.Instance.saveOrUpdate(cardrelation);
                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Validation: " + e.getMessage());
            e.printStackTrace();
            return false;
            //throw e;
        }
    }

    public boolean CardPINGeneration (WalletCMSWsEntity wsmodel, List<CMSAuth> listcardrelation)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType, sourcePinFormat,
                type, tmk, newOffset="";
        SecureDESKey fromDESKey;
        SecurityFunction sourcePinSecFunc;
        byte[] hsmResponseInBytes, command;
        Long profileId;
        Set<SecureKey> keySet;

        try {
            //Raza not checking CardRelation as
            /*if (cardrelation == null || cardrelation.getCardAuth() == null) //Raza Also Check Here Secure_Secure :D
            {
                logger.error("CardRelation or CardAuth not found, rejecting...");
                throw new Exception("CardAuth not found");
            }*/

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            profileId = endPointTerminal.getOwnOrParentSecurityProfileId();
            keySet = endPointTerminal.getKeySet();

            if (profileId != null) {
                sourcePinSecFunc = SecurityService.findSecurityFunction(profileId, SecurityComponent.FUNC_GENERATEPIN);
                sourcePinFormat = sourcePinSecFunc.getParameterValue("PIN Format");

            } else {
                sourcePinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
            }

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            if (fromDESKey != null) {
                type = ThalesHSMConst.LOCAL_NETWORK;
            } else {
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySet);
                type = ThalesHSMConst.ZONAL_NETWORK;
            }
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            //m.rehman: pan decryption
            acctNo = listcardrelation.get(0).getRelation();
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
            acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
            /////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Acct No [" + acctNo + "]");
            if (!Util.hasText(acctNo)) {
                logger.error("Card No decryption fail, rejecting...");
                return false;
            }
            //acctNo = listcardrelation.get(0).getRelation().substring(0,listcardrelation.get(0).getRelation().indexOf('='));
            acctNo = acctNo.substring(0,acctNo.indexOf('='));
            pinBlock = wsmodel.getCardpindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINGeneration(pinBlock, acctNo, key,
                        sourcePinFormat);

                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    logger.debug("Response WithOut Header [" + updateHsmResponse + "]"); //Raza TEMP
                    respCode = updateHsmResponse.substring(6, 8);
                    newOffset = updateHsmResponse.substring(8, updateHsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            } else if (hsmType.equals(CommandType.THALES.toString())) { //Raza TODO -- Update Thales Generate/Change PIN
                command = ThalesHSMHandler.getInstance().PINGeneration(pinBlock, listcardrelation.get(0).getOffset(), acctNo, key,
                        sourcePinFormat, type);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    newOffset = hsmResponse.substring(8, hsmResponse.length());
                    wsmodel.setRespcode(respCode);
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                tmk = "";
                fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
                if (fromDESKey != null) {
                    tmk = fromDESKey.getKeyBytes();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().PINGeneration(pinBlock.toUpperCase(), acctNo, tmk, key, sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        ProcessContext.get().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.PIN_GENERATE_RESP)) {
                            if (!hsmResponse.substring(4, 5).equals("S") && !hsmResponse.substring(4, 5).equals("L")) {
                                respCode = ISOResponseCodes.APPROVED;
                                newOffset = hsmResponse.substring(13, hsmResponse.indexOf("#", 13));
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }
                        wsmodel.setRespcode(respCode);
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("Card PIN Generation Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_CARD_PIN);
                //throw new PINChangeException("Card PIN Generation Failed with response code: " + respCode);
                return false;

            } else {
                logger.info("Card PIN Generated Successfully!!!");
                //logger.debug("Offset [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                logger.debug("Offset [" + newOffset + "]");

                for(CMSAuth cr : listcardrelation)
                {
                    cr.setRemRetries(cr.getMaxRetries());
                    //cr.setOffset(updateHsmResponse.substring(8, updateHsmResponse.length()));
                    cr.setOffset(newOffset);
                    cr.setStatus("00");
                    GeneralDao.Instance.saveOrUpdate(cr);
                }
                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": Card PIN Generation: " + e.getMessage());
            e.printStackTrace();
            return false;
            //throw e;
        }
    }
	
    public boolean CVVValidation(WalletCMSWsEntity wsmodel, CMSAuth cardrelation, Boolean cvvFlag)
            throws Exception {

        String cvv, serviceCode, acctNo, expiry, hsmType="", keySchemeCVKA="", keySchemeCVKB="", key="", hsmResponse,
                respCode, updateHsmResponse, track2Data, dbQuery, relation, keyB="", keySchemeCVK="";
        CMSProductKeys cvka, cvkb, cvk;
        List<CMSProductKeys> cmsProductKeys;
        byte[] command, hsmResponseInBytes;
        Integer index;
        Map<String, Object> params;

        try {

            //acctNo = wsmodel.getCardnumber();
            relation = cardrelation.getRelation();
            //m.rehman: decrypting card number
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //relation = WebServiceUtil.getPANDecryptedValue(relation, ChannelCodes.SWITCH);
            relation = WebServiceUtil.getPANDecryptedValue(relation);
            /////////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Card No [" + relation + "]");
            if (!Util.hasText(relation)) {
                logger.error("Card No decryption fail, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION);
                return false;
            }

            if(relation.contains("=")) //Raza adding if Relationshipid is of CardNumber
            {
                acctNo = relation.substring(0,relation.indexOf('='));
            }
            else
            {
                acctNo = relation;
            }

			//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
            if (Util.hasText(wsmodel.getCardlastdigits()) && !acctNo.substring(acctNo.length()-4, acctNo.length()).equals(wsmodel.getCardlastdigits())) {
            //===============================================================================================================
			    logger.error("Card Last 4 digits do not match, rejecting...");
                //throw new Exception("Card Last 4 digits do not match");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            expiry = wsmodel.getCardexpiry();
            if (!Util.hasText(expiry)) {
                expiry = relation.substring(relation.indexOf('=')+1, relation.indexOf('=')+4);
            }

            //get product keys from product id
            dbQuery = "from " + CMSProductKeys.class.getName() + " pk " +
                    "where " +
                    "pk.cmsProductId.productId = :PROD_ID";
            params = new HashMap<String, Object>();
            params.put("PROD_ID", cardrelation.getCard().getProduct().getProductId());
            cmsProductKeys = GeneralDao.Instance.find(dbQuery, params);
            if (cmsProductKeys.size() <= 0) {
                //throw new CVVValidationException("CVV Keys not found");
                logger.error("CVV Keys not found ...");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            cvka = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKA, cmsProductKeys);
            if (cvka != null) {
                keySchemeCVKA = cvka.getKeyScheme();
                hsmType = cvka.getHsmType();
            }

            cvkb = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKB, cmsProductKeys);
            if (cvkb != null) {
                keySchemeCVKB = cvkb.getKeyScheme();
                hsmType = cvkb.getHsmType();
            }

            cvk = CMSProductKeys.getKeyByType(KeyType.TYPE_CVK, cmsProductKeys);
            if (cvk != null) {
                keySchemeCVK = cvk.getKeyScheme();
                hsmType = cvk.getHsmType();
            }

            if (!Util.hasText(hsmType)) {
                //throw new CVVValidationException("HSM Type is null");
                logger.error("HSM Type is null");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            if (cvvFlag.equals(Boolean.TRUE)) {    //1 - cvv
                //TODO: need to get service code and cvv according to track format
                //cmsTrack2Format.getTrack2InfoFromCard(wsmodel);
                //serviceCode = wsmodel.getServicecode();
                //cvv = wsmodel.getCvv();
                track2Data = wsmodel.getTrack2Data();
                if (!Util.hasText(track2Data) && Util.hasText(wsmodel.getIcccarddata())) {
                    track2Data = BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.TRK2_EQUIV_DATA);
                }

                if (!Util.hasText(track2Data)) {
                    logger.error("Track 2 Data not available. Unable to proceed");
                    //throw new Exception("CVV check possible but Track 2 Data not present. Error!!!");
                    wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                    return false;
                }

                index = track2Data.indexOf("=");
                if (index <= 0)
                    index = track2Data.indexOf("D");

                //service code => separator + expiry (4) + 3
                index += 5;
                if (Util.hasText(wsmodel.getIcccarddata())
                        ||
                        (Util.hasText(wsmodel.getPosentrymode())
                                && (wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_CVV_POSSIBLE)
                                     || wsmodel.getPosentrymode().substring(0,2).equals(ISOPOSEntryMode.PANEntryMode.ICC_READ_CONTACTLESS)))) {
                    serviceCode = "999";
                    logger.info("Validating ICVV ...");
                }
                else {
                    serviceCode = track2Data.substring(index, index + 3);
                    logger.info("Validating CVV ...");
                }

                index += 3;
                cvv = track2Data.substring(index, index + 3);

            } else {
                cvv = wsmodel.getCvv2();
                //for cvv2 validation, all parameters are same except service code and expiry, so changing service code and expiry
                serviceCode = "000";
                expiry = expiry.substring(2,4) + expiry.substring(0,2);
                logger.info("Validating CVV2 ...");
            }

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {

                if (keySchemeCVKA != null && keySchemeCVKB != null) {
                    key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                } else {
                    key = SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC + cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = SafeNetHSMHandler.getInstance().CVVValidation(key, cvv, acctNo, expiry, serviceCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, GlobalContext.getInstance().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);

                    if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                        wsmodel.setRespcode(respCode);
                        return true;
                    } else {
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                        return false;
                    }
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {

                //if key is double length, append key specifier and merge cvka and cvkb
                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if (keySchemeCVKA.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC) &&
                            keySchemeCVKB.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = ThalesHSMHandler.getInstance().CVVValidation(key, cvv, acctNo, expiry, serviceCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, GlobalContext.getInstance().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);

                    if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                        wsmodel.setRespcode(respCode);
                        return true;
                    } else {
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                        return false;
                    }
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                if (cvka != null && cvkb != null) {
                    key = cvka.getKeyValue();
                    keyB = cvkb.getKeyValue();
                } else {
                    key = cvk.getKeyValue();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().CVVValidation(key, keyB, cvv, acctNo, expiry, serviceCode, sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        GlobalContext.getInstance().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, GlobalContext.getInstance().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());

                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.info("Response from HSM [" + hsmResponse + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.CVV_VERIFY_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;
                            } else {
                                //m.rehman: Euronet Integration
                                respCode = ISOResponseCodes.INVALID_CARD_DATA;
                            }
                        } else {
                            //m.rehman: Euronet Integration
                            respCode = ISOResponseCodes.INVALID_CARD_DATA;
                        }

                        if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                            wsmodel.setRespcode(respCode);

                            GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                            currentThread.setName(threadOldName);

                            return true;
                        } else {
                            wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);

                            GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                            currentThread.setName(threadOldName);

                            return false;
                        }
                    }
                    // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
                    else {
                        logger.error("NULL response received from HSM...");
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
                return false;
            }

            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            return false;
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": CVV/CVV2 Validation: " + e.getMessage());
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            logger.error(WebServiceUtil.getStrException(e));
            throw e;
        }
    }

	//m.rehman: Euronet Integration
    public boolean CAVVValidation(WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception {

        String cavv, authResultCode, secFactorAuthCode, unPredNo, acctNo, hsmType="", keySchemeCVKA="", keySchemeCVKB="", key="", hsmResponse,
                respCode, updateHsmResponse, track2Data, dbQuery, relation, keyB="", keySchemeCVK="", value="";
        CMSProductKeys cvka, cvkb, cvk;
        List<CMSProductKeys> cmsProductKeys;
        byte[] command, hsmResponseInBytes;
        Integer index;
        Map<String, Object> params;

        try {

            //acctNo = wsmodel.getCardnumber();
            relation = cardrelation.getRelation();
            //m.rehman: decrypting card number
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //relation = WebServiceUtil.getPANDecryptedValue(relation, ChannelCodes.SWITCH);
            relation = WebServiceUtil.getPANDecryptedValue(relation);
            /////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Card No [" + relation + "]");
            if (!Util.hasText(relation)) {
                logger.error("Card No decryption fail, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION);
                return false;
            }

            if(relation.contains("=")) //Raza adding if Relationshipid is of CardNumber
            {
                acctNo = relation.substring(0,relation.indexOf('='));
            }
            else
            {
                acctNo = relation;
            }
			
			//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
            if (Util.hasText(wsmodel.getCardlastdigits()) && !acctNo.substring(acctNo.length()-4, acctNo.length()).equals(wsmodel.getCardlastdigits())) {
            //===============================================================================================================
			    logger.error("Card Last 4 digits do not match, rejecting...");
                //throw new Exception("Card Last 4 digits do not match");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            if (Util.hasText(wsmodel.getChannelid()) && wsmodel.getChannelid().equals(ChannelCodes.EURONET)) {
                value = wsmodel.getCavvdata();
                if (Util.hasText(value)) {
                    //Auth Result Code
                    authResultCode = value.substring(1, 2);

                    //Secondary Factor Auth Code
                    secFactorAuthCode = value.substring(2, 4);

                    //cavv
                    cavv = value.substring(7, 10);

                    //Unpredictable no
                    unPredNo = value.substring(10, 14);

                    //combining auth result code and secondary factor auth code
                    authResultCode = authResultCode + secFactorAuthCode;
                } else {
                    logger.error("CAVV data not present, rejecting ...");
                    return false;
                }
            } else {
                logger.error("Channel ID not present or registered [" + wsmodel.getChannelid() + "], rejecting ...");
                return false;
            }

            //get product keys from product id
            dbQuery = "from " + CMSProductKeys.class.getName() + " pk " +
                    "where " +
                    "pk.cmsProductId.productId = :PROD_ID";
            params = new HashMap<String, Object>();
            params.put("PROD_ID", cardrelation.getCard().getProduct().getProductId());
            cmsProductKeys = GeneralDao.Instance.find(dbQuery, params);
            if (cmsProductKeys.size() <= 0) {
                //throw new CVVValidationException("CVV Keys not found");
                logger.error("CAVV Keys not found ...");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            cvka = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKA, cmsProductKeys);
            if (cvka != null) {
                keySchemeCVKA = cvka.getKeyScheme();
                hsmType = cvka.getHsmType();
            }

            cvkb = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKB, cmsProductKeys);
            if (cvkb != null) {
                keySchemeCVKB = cvkb.getKeyScheme();
                hsmType = cvkb.getHsmType();
            }

            cvk = CMSProductKeys.getKeyByType(KeyType.TYPE_CVK, cmsProductKeys);
            if (cvk != null) {
                keySchemeCVK = cvk.getKeyScheme();
                hsmType = cvk.getHsmType();
            }

            if (!Util.hasText(hsmType)) {
                //throw new CVVValidationException("HSM Type is null");
                logger.error("HSM Type is null");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            logger.info("Validating CAVV ...");
            if (hsmType.equals(CommandType.SAFE_NET.toString())) {

                if (keySchemeCVKA != null && keySchemeCVKB != null) {
                    key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                } else {
                    key = SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC + cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = SafeNetHSMHandler.getInstance().CVVValidation(key, cavv, acctNo, unPredNo, authResultCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, GlobalContext.getInstance().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);

                    if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                        wsmodel.setRespcode(respCode);
                        return true;
                    } else {
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                        return false;
                    }
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {

                //if key is double length, append key specifier and merge cvka and cvkb
                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if (keySchemeCVKA.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC) &&
                            keySchemeCVKB.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = ThalesHSMHandler.getInstance().CVVValidation(key, cavv, acctNo, unPredNo, authResultCode);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, GlobalContext.getInstance().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);

                    if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                        wsmodel.setRespcode(respCode);
                        return true;
                    } else {
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                        return false;
                    }
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                if (cvka != null && cvkb != null) {
                    key = cvka.getKeyValue();
                    keyB = cvkb.getKeyValue();
                } else {
                    key = cvk.getKeyValue();
                }

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().CVVValidation(key, keyB, cavv, acctNo, unPredNo, authResultCode, sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        GlobalContext.getInstance().getMyInstitution().getBin().toString());
                HSMNetworkManager.getInstance().sendRequestToHSM(command, GlobalContext.getInstance().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.debug("Response from HSM [" + hsmResponse + "]");
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.CVV_VERIFY_RESP)) {
                            if (hsmResponse.substring(4, 5).equals("Y")) {
                                respCode = ISOResponseCodes.APPROVED;

                                //m.rehman: Euronet Integration
                                if (Util.hasText(wsmodel.getSelfdefinedata()) && wsmodel.getSelfdefinedata().substring(8, 10).equals("05")) {
                                    value = "2";
                                } else {
                                    value = "3";
                                }
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;

                                //m.rehman: Euronet Integration
                                if (Util.hasText(wsmodel.getSelfdefinedata()) && wsmodel.getSelfdefinedata().substring(8, 10).equals("05")) {
                                    value = "1";
                                } else {
                                    value = "4";
                                }
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;

                            //m.rehman: Euronet Integration
                            if (Util.hasText(wsmodel.getSelfdefinedata()) && wsmodel.getSelfdefinedata().substring(8, 10).equals("05")) {
                                value = "1";
                            } else {
                                value = "4";
                            }
                        }

                        //m.rehman: Euronet integration
                        if (wsmodel.getChannelid().equals(ChannelCodes.EURONET)) {
                            String addRespData = wsmodel.getAddresponsedata();
                            if (Util.hasText(addRespData)) {
                                if (addRespData.length() > 15) {
                                    addRespData = addRespData.substring(0, 14) + value + addRespData.substring(15, addRespData.length());
                                } else {
                                    addRespData = StringUtils.rightPad(addRespData, 14, " ") + value;
                                }
                            } else {
                                addRespData = StringUtils.rightPad(addRespData, 14, " ") + value;
                            }
                            wsmodel.setAddresponsedata(addRespData);
                        }

                        if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                            wsmodel.setRespcode(respCode);

                            GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                            currentThread.setName(threadOldName);

                            return true;
                        } else {
                            wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);

                            GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                            currentThread.setName(threadOldName);

                            return false;
                        }
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
                return false;
            }

            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            return false;
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": CVV/CVV2 Validation: " + e.getMessage());
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            throw e;
        }
    }

    public boolean CryptogramValidation(WalletCMSWsEntity wsmodel, CMSAuth cardrelation)
            throws Exception {

        String acctNo, hsmType, key, hsmResponse, respCode, updateHsmResponse, tranData, arqc, authRsCode,
                channelId, panSeqNo, atc, unIdentifiedNo, arpc, dbQuery, emvData;
        CMSProductKeys mdk;
        List<CMSProductKeys> cmsProductKeys;
        byte[] command, hsmResponseInBytes;
        Map<String, Object> params;
        boolean cvn18Flag = false;   //m.rehman: Euronet Integration

        try {

            //get product keys from product id
            dbQuery = "from " + CMSProductKeys.class.getName() + " pk " +
                    "where " +
                    "pk.cmsProductId.productId = :PROD_ID";
            params = new HashMap<String, Object>();
            params.put("PROD_ID", cardrelation.getCard().getProduct().getProductId());
            cmsProductKeys = GeneralDao.Instance.find(dbQuery, params);
            if (cmsProductKeys.size() <= 0) {
                //throw new CVVValidationException("ICC Keys not found");
                logger.error("ICC Keys not available in system ...");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            mdk = CMSProductKeys.getKeyByType(KeyType.TYPE_IMK_AC, cmsProductKeys);
            hsmType = mdk.getHsmType();

            if (hsmType == null) {
                //throw new CardValidationException("HSM Type is null");
                logger.error("HSM Type is null");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            acctNo = wsmodel.getCardnumber();
            //m.rehman: decrypting card number
            //m.rehman: 05-08-2020, pan encryption/decryption using vault keys
            //acctNo = WebServiceUtil.getPANDecryptedValue(acctNo, ChannelCodes.SWITCH);
            acctNo = WebServiceUtil.getPANDecryptedValue(acctNo);
            /////////////////////////////////////////////////////////////////////////////

            //TODO: remove this logging
            logger.debug("Decrypted Card No [" + acctNo + "]");
            if (!Util.hasText(acctNo)) {
                logger.error("Card No decryption fail, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION);
                return false;
            }
			
			//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
            if (Util.hasText(wsmodel.getCardlastdigits()) && !acctNo.substring(acctNo.length()-4, acctNo.length()).equals(wsmodel.getCardlastdigits())) {
            //===============================================================================================================
			    logger.error("Card Last 4 digits do not match, rejecting...");
                //throw new Exception("Card Last 4 digits do not match");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
            }

            key = mdk.getKeyValue();
            channelId = wsmodel.getChannelid();
            //m.rehman: Euronet Integration: authorization response code is dependent on Cryptogram Verification Number (CVN) which can be found in 9F10
            //changing below
            //not applied on 1Link
            String _9F10Value = BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.ISS_APP_DATA);
            if (Util.hasText(_9F10Value)) {
                if (_9F10Value.substring(4, 6).equals("12")) {
                    cvn18Flag = true;
                }
            }

            //m.rehman: 14-04-2022 - VP-NAP-202111291 / VC-NAP-202111291 / VG-NAP-202111291 - Meezan ATM On-Us Withdrawal/Balance Inquiry and Reversal
            //adding check for PayPak Cards
            if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK) || cvn18Flag
                    || cardrelation.getCard().getProduct().getProductType().equals("CARD_LVL0")
                    || cardrelation.getCard().getProduct().getProductType().equals("CARD_LVL1")) {
                authRsCode = "00820000";
                //authRsCode = "008A0080"; //Mati 22-08-2019 Case:37  1-Link Ceritification
                //authRsCode = "008A0000"; //Mati 22-08-2019 Case:38  1-Link Ceritification
            } else {
                authRsCode = "3030";
            }

            tranData = BERTLV.getTranData(wsmodel.getIcccarddata());
            if (!Util.hasText(tranData)) {

                //TODO: need to update below according to scheme requirement
                //setting icc data without 9F26 (Application Cryptogram) tag for 1Link
                String iccData = wsmodel.getIcccarddata();
                String iccDataUpdate;
                int index = iccData.indexOf(EMVTags.APP_CRYPT);
                iccDataUpdate = iccData.substring(0, index);
                iccDataUpdate += iccData.substring(index + 22, iccData.length());
                wsmodel.setIcccarddata(iccDataUpdate);

                emvData = wsmodel.getSelfdefinedata();
                if (Util.hasText(emvData))
                    emvData = emvData.substring(0, 2) + "0";
                wsmodel.setSelfdefinedata(emvData);

                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA); //1Link Certification ; 07-Invalid Card Data Mati updating 19-08-2019
                return false;
            }

            //m.rehman: adding for Euronet Integration and Visa Card Issuance, need pan seq no for ARPC validation
            //also need to check transaction data, append 8000000000000000
            panSeqNo = BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.PAN_SEQ_NO);
//            if (Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) {
            if ((Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) || cardrelation.getCard().getProduct().getProductType().equals("VISA_DEBIT")) {
                if (!Util.hasText(panSeqNo)) {
                    panSeqNo = "01";        //TODO: for visa card, need to update the logic
                }
                tranData += "8000000000000000";     //TODO: need to review this
            }

            arqc = BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.APP_CRYPT);
            atc = BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.ATC);
            unIdentifiedNo = BERTLV.findTLVTagValue(wsmodel.getIcccarddata(), EMVTags.UNPRED_NO);

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {

                if (acctNo.length() > 14) {
                    acctNo = acctNo.substring((acctNo.length()-14), acctNo.length());
                }
                acctNo = acctNo + panSeqNo;

                command = SafeNetHSMHandler.getInstance().ARQCValidationARPCGeneration(acctNo, tranData, arqc, authRsCode, key, atc);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, GlobalContext.getInstance().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {

						//m.rehman: Euronet Integration
                        if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK) || cvn18Flag) {
                            //arpc = "910A" + updateHsmResponse.substring(8, 24) + "3030";
                            arpc = "9108" + updateHsmResponse.substring(10, 18) + authRsCode;
                            //arpc = "9110" + updateHsmResponse.substring(10, 18) + authRsCode + "0000000000000000"; //Mati 22-08-2019 Case:33  1-Link Ceritification
                        } else {
                            arpc = "910A" + hsmResponse.substring(5, 21) + authRsCode;
                        }

                        if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK)) {
                            emvData = wsmodel.getSelfdefinedata();
                            if (Util.hasText(emvData))
                                emvData = emvData.substring(0, 2) + "2";
                            wsmodel.setSelfdefinedata(emvData);
						//m.rehman: Euronet Integration
                        } else if (Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) {
                            String addRespData = wsmodel.getAddresponsedata();
                            if (Util.hasText(addRespData)) {
                                if (addRespData.length() > 8) {
                                    addRespData = addRespData.substring(0,7) + "1" + addRespData.substring(8,addRespData.length());
                                } else {
                                    addRespData = StringUtils.rightPad(addRespData, 7, " ") + "1";
                                }
                            } else {
                                addRespData = "";
                                addRespData = StringUtils.rightPad(addRespData, 7, " ") + "1";
                            }
                            wsmodel.setAddresponsedata(addRespData);
                        }

                        wsmodel.setIcccarddata(arpc);
                        logger.debug("ARPC [" + arpc + "]");
                        wsmodel.setRespcode(respCode);

                        return true;

                    } else {
                        wsmodel.setIcccarddata(null);
                        wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION);

						//m.rehman: Euronet Integration
                        if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK)) {
                            emvData = wsmodel.getSelfdefinedata();
                            if (Util.hasText(emvData))
                                emvData = emvData.substring(0, 2) + "1";
                            wsmodel.setSelfdefinedata(emvData);

                        } else if (Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) {
                            //m.rehman: on request of Euronet
                            wsmodel.setRespcode(ISOResponseCodes.CHECK_DIGIT_FAILED);
                            String addRespData = wsmodel.getAddresponsedata();
                            if (Util.hasText(addRespData)) {
                                if (addRespData.length() > 8) {
                                    addRespData = addRespData.substring(0,7) + "2" + addRespData.substring(8,addRespData.length());
                                } else {
                                    addRespData = StringUtils.rightPad(addRespData, 7, " ") + "2";
                                }
                            } else {
                                addRespData = "";
                                addRespData = StringUtils.rightPad(addRespData, 7, " ") + "2";
                            }
                            wsmodel.setAddresponsedata(addRespData);
                        }

                        return false;
                    }
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {

                command = ThalesHSMHandler.getInstance().ARQCValidationARPCGeneration(acctNo, tranData, arqc, authRsCode,
                        key, channelId, panSeqNo, atc, unIdentifiedNo);

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, GlobalContext.getInstance().getMyInstitution().getBin().toString()));
                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
                    if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                        arpc = "910A" + hsmResponse.substring(8, 24) + "3030";
                        wsmodel.setIcccarddata(arpc);
                        wsmodel.setRespcode(respCode);

						//m.rehman: Euronet Integration
                        if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK)) {
                            emvData = wsmodel.getSelfdefinedata();
                            if (Util.hasText(emvData))
                                emvData = emvData.substring(0, 2) + "2";
                            wsmodel.setSelfdefinedata(emvData);

						//m.rehman: Euronet Integration
                        } else if (Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) {
                            String addRespData = wsmodel.getAddresponsedata();
                            if (Util.hasText(addRespData)) {
                                if (addRespData.length() > 8) {
                                    addRespData = addRespData.substring(0,7) + "1" + addRespData.substring(8,addRespData.length());
                                } else {
                                    addRespData = StringUtils.rightPad(addRespData, 7, " ") + "1";
                                }
                            } else {
                                addRespData = "";
                                addRespData = StringUtils.rightPad(addRespData, 7, " ") + "1";
                            }
                            wsmodel.setAddresponsedata(addRespData);
                        }

                        return true;

                    } else {
                        wsmodel.setIcccarddata(null);
                        wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION);

						//m.rehman: Euronet Integration			
                        if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK)) {
                            emvData = wsmodel.getSelfdefinedata();
                            if (Util.hasText(emvData))
                                emvData = emvData.substring(0, 2) + "1";
                            wsmodel.setSelfdefinedata(emvData);

						//m.rehman: Euronet Integration
                        } else if (Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) {
                            //m.rehman: on request of Euronet
                            wsmodel.setRespcode(ISOResponseCodes.CHECK_DIGIT_FAILED);
                            String addRespData = wsmodel.getAddresponsedata();
                            if (Util.hasText(addRespData)) {
                                if (addRespData.length() > 8) {
                                    addRespData = addRespData.substring(0,7) + "2" + addRespData.substring(8,addRespData.length());
                                } else {
                                    addRespData = StringUtils.rightPad(addRespData, 7, " ") + "2";
                                }
                            } else {
                                addRespData = "";
                                addRespData = StringUtils.rightPad(addRespData, 7, " ") + "2";
                            }
                            wsmodel.setAddresponsedata(addRespData);
                        }

                        return false;
                    }
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())) {

                //m.rehman: 22-11-2021, HSM response logging
                String sequence = wsmodel.getTransdatetime() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                wsmodel.setThreadId(currentThread.getId() + "");
                wsmodel.setThreadName(currentThread.getName());
                wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().ARQCValidationARPCGeneration(acctNo, tranData, arqc, authRsCode,
                        key, channelId, panSeqNo, atc, unIdentifiedNo, sequence);

                //send and receive message from HSM
                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(command,
                //        GlobalContext.getInstance().getMyInstitution().getBin().toString());

                HSMNetworkManager.getInstance().sendRequestToHSM(command, GlobalContext.getInstance().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.info("Response from HSM [" + hsmResponse + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
                        if (hsmResponse.substring(1, 4).equals(AtallaHSMConst.Commands.ARQC_VALIDATE_ARPC_GENERATE_RESP)) {
                            if (!hsmResponse.substring(5, 6).equals(AtallaHSMConst.SEPARATOR)) {
                                respCode = ISOResponseCodes.APPROVED;
                            } else {
                                respCode = ISOResponseCodes.NP_BAD_PIN;
                            }
                        } else {
                            respCode = ISOResponseCodes.NP_AUTHENTICATION_FAILED;
                        }

                        if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
                            //m.rehman: Euronet integration, handling of cnv18 flag
                            if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK) || cvn18Flag
                                    || cardrelation.getCard().getProduct().getProductType().equals("CARD_LVL0")
                                    || cardrelation.getCard().getProduct().getProductType().equals("CARD_LVL1")) {
                                //arpc = "910A" + updateHsmResponse.substring(8, 24) + "3030";
                                arpc = "9108" + hsmResponse.substring(5, 13) + authRsCode;
                                //arpc = "9110" + updateHsmResponse.substring(10, 18) + authRsCode + "0000000000000000"; //Mati 22-08-2019 Case:33  1-Link Ceritification
                            } else {
                                arpc = "910A" + hsmResponse.substring(5, 21) + authRsCode;
                            }

                            if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK)) {
                                emvData = wsmodel.getSelfdefinedata();
                                if (Util.hasText(emvData))
                                    emvData = emvData.substring(0, 2) + "2";
                                wsmodel.setSelfdefinedata(emvData);

                            } else if (Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) {
                                String addRespData = wsmodel.getAddresponsedata();
                                if (Util.hasText(addRespData)) {
                                    if (addRespData.length() > 8) {
                                        addRespData = addRespData.substring(0, 7) + "2" + addRespData.substring(8, addRespData.length());
                                    } else {
                                        addRespData = StringUtils.rightPad(addRespData, 7, " ") + "2";
                                    }
                                } else {
                                    addRespData = "";
                                    addRespData = StringUtils.rightPad(addRespData, 7, " ") + "2";
                                }
                                wsmodel.setAddresponsedata(addRespData);
                            }

                            wsmodel.setIcccarddata(arpc);
                            logger.debug("ARPC [" + arpc + "]");
                            wsmodel.setRespcode(respCode);

                            GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                            currentThread.setName(threadOldName);

                            return true;

                        } else {

                            //m.rehman: 20-08-2020, Euronet Integration, response code not returning in negative case
                            wsmodel.setIcccarddata(null);
                            wsmodel.setRespcode(ISOResponseCodes.SECURITY_VIOLATION);

                            if (Util.hasText(channelId) && channelId.equals(ChannelCodes.ONELINK)) {
                                emvData = wsmodel.getSelfdefinedata();
                                if (Util.hasText(emvData))
                                    emvData = emvData.substring(0, 2) + "1";
                                wsmodel.setSelfdefinedata(emvData);

                                //m.rehman: Euronet Integration
                            } else if (Util.hasText(channelId) && channelId.equals(ChannelCodes.EURONET)) {
                                //m.rehman: on request of Euronet
                                wsmodel.setRespcode(ISOResponseCodes.CHECK_DIGIT_FAILED);
                                String addRespData = wsmodel.getAddresponsedata();
                                if (Util.hasText(addRespData)) {
                                    if (addRespData.length() > 8) {
                                        addRespData = addRespData.substring(0, 7) + "1" + addRespData.substring(8, addRespData.length());
                                    } else {
                                        addRespData = StringUtils.rightPad(addRespData, 7, " ") + "1";
                                    }
                                } else {
                                    addRespData = "";
                                    addRespData = StringUtils.rightPad(addRespData, 7, " ") + "1";
                                }
                                wsmodel.setAddresponsedata(addRespData);
                            }

                            GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                            currentThread.setName(threadOldName);

                            return false;
                        }
                    }
                    // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
                    else {
                        logger.error("NULL response received from HSM...");
                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }
            }
            else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
                return false;
            }

            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            return false;
        } catch (Exception e) {
            wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
            logger.error(this.getClass().getName() + ": Cryptogram Validation: " + e.getMessage());
            throw e;
        }
    }

    public boolean GeneratePINBlock (WalletCMSWsEntity wsmodel)
            throws Exception { //Raza NayaPay

        String key, hsmResponse = null, updateHsmResponse=null, respCode, pinBlock, acctNo, hsmType;
        SecureDESKey fromDESKey;
        byte[] hsmResponseInBytes, command;
        Set<SecureKey> keySet;

        try {
            //Raza not checking CardRelation as
            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.SWITCH)).getInstitutionId();
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            Terminal endPointTerminal = null;
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);

            keySet = endPointTerminal.getKeySet();

            fromDESKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
            key = fromDESKey.getKeyBytes();

            hsmType = fromDESKey.getHsmType();

            acctNo = wsmodel.getAccountnumber();
            pinBlock = wsmodel.getPindata();

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {
                command = SafeNetHSMHandler.getInstance().PINBlockGeneration(pinBlock, acctNo, key);

                logger.debug("HSM Command [" + command + "]");

                //send and receive message from HSM
                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, ProcessContext.get().getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    logger.debug("Response from HSM [" + hsmResponse + "]");
                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
                    respCode = updateHsmResponse.substring(6, 8);
                    wsmodel.setRespcode(respCode);
                }
            } else {
                logger.error("No HSM Type Available");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                return false;
                //throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }

            logger.debug("HSM Response: [" + hsmResponse + "]");
            respCode = wsmodel.getRespcode();
            if (!Util.hasText(respCode)) {
                logger.error("No response received. HSM Time Out");
                wsmodel.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
                //throw new NotAvailableHSMChannelFoundException("No response received. HSM Time Out");
                return false;

            } else if (!respCode.equals(ISOResponseCodes.APPROVED)) {
                logger.error("PIN Generation Failed with response code: " + respCode);
                wsmodel.setRespcode(ISOResponseCodes.NP_BAD_PIN);
                //throw new PINChangeException("PIN Validation Failed with response code: " + respCode);
                return false;

            } else {
                logger.info("PIN Generated Successfully!!!");
                logger.debug("PIN Block [" + updateHsmResponse.substring(8, updateHsmResponse.length()) + "]");
                wsmodel.setNewpindata(updateHsmResponse.substring(8, updateHsmResponse.length()));
                return true;
            }


        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": PIN Validation: " + e.getMessage());
            e.printStackTrace();
            return false;
            //throw e;
        }
    }

    // Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)

    public String CVVGeneration(String productServiceCode, String cardPAN, String cardExpiry, List<CMSProductKeys> cmsProductKeys, Boolean cvvFlag, Boolean icvvFlag)
            throws Exception {

        String result = "", serviceCode = "", acctNo, expiry = "", hsmType = "", keySchemeCVKA="", keySchemeCVKB="", keySchemeCVK="", key, hsmResponse, respCode,
                updateHsmResponse;
        CMSProductKeys cvka, cvkb, cvk;
        byte[] command, hsmResponseInBytes;

        ProcessContext processContext = new ProcessContext();
        processContext.init();
        Channel hsm = GlobalContext.getInstance().getHSMChannel("HSM-Pass-1");

        try {

            acctNo = cardPAN;

            cvka = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKA, cmsProductKeys);
            cvkb = CMSProductKeys.getKeyByType(KeyType.TYPE_CVKB, cmsProductKeys);

            /********** Added by Mehtab for Atalla HSM **********/

            cvk = CMSProductKeys.getKeyByType(KeyType.TYPE_CVK, cmsProductKeys);

            /********** END **********/

            // Asim Shahzad, Date : 12th May 2020, Desc : Added checks to enable different HSM types and read their keys accordingly
            if(null != cvka){
                keySchemeCVKA = cvka.getKeyScheme();
                keySchemeCVKB = cvkb.getKeyScheme();
                hsmType = cvka.getHsmType();
            }

            else if(null != cvk){
                hsmType = cvk.getHsmType();
                keySchemeCVK = cvk.getKeyScheme();
            }
            else {
                logger.error("Keys not found for HSM");
                throw new NotAvailableHSMChannelFoundException("Keys not found for HSM");
            }
            // =======================================================================================================================

            key = null;
            command = null;

            if (cvvFlag.equals(Boolean.TRUE)) {
                serviceCode = productServiceCode;
                expiry = cardExpiry;
            }
            if (cvvFlag.equals(Boolean.FALSE)) {
                if(icvvFlag.equals(Boolean.FALSE)) {
                    //for cvv2 validation, all parameters are same except service code, so changing service code
                    serviceCode = "000";

                    // Author : Asim Shahzad, Date : 25th Jun 2019, Desc : Reverse expiry date as MMYY
                    expiry = this.getCVV2Expiry(cardExpiry);
                }
            }

            // Author : Asim Shahzad, Date : 25th Jun 2019, Desc : For ICVV generation
            if (cvvFlag.equals(Boolean.FALSE)) {
                if (icvvFlag.equals(Boolean.TRUE)) {
                    serviceCode = "999";
                    expiry = cardExpiry;
                }
            }

//            Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Service Code [" + serviceCode + "]", Enums.LogEvent.Information);

            if (hsmType.equals(CommandType.SAFE_NET.toString())) {

                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Command Type Received [" + CommandType.SAFE_NET.toString() + "]", Enums.LogEvent.Information);
                logger.info("Command Type Received [" + CommandType.SAFE_NET.toString() + "]");

                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if ((keySchemeCVKA.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC) &&
                            keySchemeCVKB.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC))
                            ||
                            keySchemeCVKA.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_EBC) &&
                                    keySchemeCVKB.equals(SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_EBC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

//                Logging.InsertLine();
                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Building Safenet Command...", Enums.LogEvent.Information);
                logger.info("Building Safenet Command...");
                command = SafeNetHSMHandler.getInstance().CVVGeneration(key, acctNo, expiry, serviceCode);
                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "HSM Command [" + new String(command, "UTF-8") + "]", Enums.LogEvent.Information);

                //send and receive message from HSM
//                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(command, cardPAN.substring(0,6)));
                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Sending command to HSM..", Enums.LogEvent.Information);
                logger.info("Sending command to HSM..");
                //hsmResponseInBytes = Hex.encode(RemoteMessageUtil.sendCommandToHSM(MessageType.HSMCommand, hsm, command));

                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, processContext.getMyInstitution().getBin().toString()));

                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Response received from HSM..", Enums.LogEvent.Information);
                logger.info("Response received from HSM..");
                //Logging.InsertLine();

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
//                    Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Response received from HSM : [" + hsmResponse.substring(0,106) + "]", Enums.LogEvent.Information);
//                    logger.debug("Response from HSM [" + hsmResponse + "]");
//                    updateHsmResponse = SafeNetHSMHandler.getInstance().GetMessageWithoutHeader(hsmResponse);
//                    logger.debug("updateHsmResponse without Header [" + updateHsmResponse + "]");
//                    logger.debug("ResponseCode [" +  updateHsmResponse.substring(6, 8) + "]");
//                    respCode = updateHsmResponse.substring(6, 8);
//                    logger.error(this.getClass().getName() + ": Response Returned By HSM : " + respCode);
//                    ifx.setRsCode(respCode);
                    if (hsmResponse.length() > 8) {
                        int index = hsmResponse.toLowerCase().indexOf("ee0802") + 6;

                        if(hsmResponse.substring(index,index + 2).equals("00")) {
                            result = hsmResponse.substring(index + 2,index + 5);
//                            Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Response received from HSM : [" + result + "]", Enums.LogEvent.Information);
                        }
                        else {
                            result = hsmResponse.substring(index + 2,index + 5);
                            //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Response received from HSM : [" + result + "]", Enums.LogEvent.Information);
                            logger.info("Response received from HSM : [" + result + "]");

                            //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Error encountered while generating CVV/CVV2..", Enums.LogEvent.Error_Message);
                            logger.error("Error encountered while generating CVV/CVV2..");
                        }
                    }
                }

            } else if (hsmType.equals(CommandType.THALES.toString())) {

                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Command Type Received [" + CommandType.THALES.toString() + "]", Enums.LogEvent.Information);
                logger.info("Command Type Received [" + CommandType.THALES.toString() + "]");

                //if key is double length, append key specifier and merge cvka and cvkb
                if (keySchemeCVKA != null && keySchemeCVKB != null) {

                    if (keySchemeCVKA.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC) &&
                            keySchemeCVKB.equals(ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC)) {

                        key = keySchemeCVKA + cvka.getKeyValue() + cvkb.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvka.getKeyValue() + cvkb.getKeyValue();
                }

                command = ThalesHSMHandler.getInstance().CVVGeneration(key, acctNo, expiry, serviceCode);

                //send and receive message from HSM
//                hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(
//                        command, ProcessContext.get().getMyInstitution().getBin().toString());

                hsmResponseInBytes = Hex.encode(HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                        command, processContext.getMyInstitution().getBin().toString()));

                if (hsmResponseInBytes != null) {
                    hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                    respCode = hsmResponse.substring(6, 8);
//                    ifx.setRsCode(respCode);
                    logger.error(this.getClass().getName() + ": Response Returned By HSM : " + respCode);
                    if (hsmResponse.length() > 8) {
                        result = hsmResponse.substring(8, 11);
                        if (Util.hasText(result))
//                            ifx.getSafeCardAcctId().setCVV(cvv);
                            logger.error(this.getClass().getName() + ": CVV generated : " + result);
                    }
                }
            }
            else if (hsmType.equals(CommandType.ATALLA.toString())){

                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Command Type Received [" + CommandType.ATALLA.toString() + "]", Enums.LogEvent.Information);
                logger.info("Command Type Received [" + CommandType.ATALLA.toString() + "]");

                if (keySchemeCVK != null && keySchemeCVKB != null) {

                    if (keySchemeCVKA.equals(AtallaHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC)) {
                        key = keySchemeCVK + cvk.getKeyValue();
                    }
                }

                if (key == null) {
                    key = cvk.getKeyValue();
                }

                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Building ATALLA HSM Command...", Enums.LogEvent.Information);
                logger.info("Building ATALLA HSM Command...");
                //m.rehman: 22-11-2021, HSM response logging
                String sequence = DateTime.now().getDateTimeLong() + StringUtils.leftPad(Util.generateTrnSeqCntr(10), 10, "0");
                Thread currentThread = Thread.currentThread();
                String threadOldName = currentThread.getName();
                currentThread.setName(threadOldName + "-" + sequence);
                //wsmodel.setThreadId(currentThread.getId() + "");
                //wsmodel.setThreadName(currentThread.getName());
                //wsmodel.setRespcode("");
                command = AtallaHSMHandler.getInstance().CVVGeneration(key, "", acctNo, expiry, serviceCode, sequence);

                //send and receive message from HSM
                //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Sending command to ATALLA HSM..", Enums.LogEvent.Information);
                logger.info("Sending command to ATALLA HSM..");
//                hsmResponseInBytes = RemoteMessageUtil.sendCommandToAtallaHSM(MessageType.HSMCommand, hsm, command);

                //hsmResponseInBytes = HSMNetworkManager.getInstance().sendRequestReceiveResponse(
                //        command, processContext.getMyInstitution().getBin().toString());

                HSMNetworkManager.getInstance().sendRequestToHSM(command, ProcessContext.get().getMyInstitution().getBin().toString());
                logger.info("Waiting for response from Atalla HSM ...");
                synchronized (currentThread) {
                    currentThread.wait(10000);

                    logger.info("Response received from HSM or Timeout occurred ...");
                    hsmResponseInBytes = GlobalContext.getInstance().getHsmResponse(currentThread.getName());

                    //Logging.Log(Log_File_Prefix, ClassName, "CVVGeneration", "Response received from ATALLA HSM..", Enums.LogEvent.Information);
                    //logger.info("Response received from ATALLA HSM..");
                    //Logging.InsertLine();
                    if (hsmResponseInBytes != null) {
                        hsmResponse = new String(hsmResponseInBytes, "UTF-8");
                        logger.info("Response from HSM [" + hsmResponse + "]");
                        respCode = hsmResponse.substring(1, 3);
                        if (hsmResponse.substring(1, 3).equals(AtallaHSMConst.Commands.CVV_VERIFY_GENERATE)) {
                            result = hsmResponse.substring(hsmResponse.indexOf('#') + 1, hsmResponse.indexOf('#') + 4);

                        } else {
                            logger.error(this.getClass().getName() + ": Response Returned By HSM : " + respCode);
                        }

                    }
                    GlobalContext.getInstance().removeHsmResponse(currentThread.getName());
                    currentThread.setName(threadOldName);
                }

            }else {
                logger.error("No HSM Type Available");
                throw new NotAvailableHSMChannelFoundException("No HSM Type Available");
            }
        } catch (Exception e) {
            logger.error(this.getClass().getName() + ": CVV/CVV2 Validation: " + e.getMessage());
            throw e;
        }

        return result;
    }

    public static String getCVV2Expiry (String expiryDate){
        String compMM = expiryDate.substring(4,6);
        String compYY = expiryDate.substring(2,4);

        logger.info("Expiry for CVV2 : [" + compMM + compYY + "]");

        return compMM + compYY;
    }

    // =============================================================================================================
}
