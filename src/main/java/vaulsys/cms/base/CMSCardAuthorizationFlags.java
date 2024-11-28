package vaulsys.cms.base;

import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOPOSEntryMode;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by HP on 4/27/2017.
 */
@Embeddable
@Entity(dynamicUpdate = true, dynamicInsert = true)
public class CMSCardAuthorizationFlags implements Serializable, Cloneable {

    private Boolean isAuthRequiredFlag;
    private Boolean isPINRequiredFlag;
    private Boolean isCVVRequiredFlag;
    private Boolean isCVV2RequiredFlag;
    private Boolean isARQCRequiredFlag;
    private Boolean isARPCRequiredFlag;

    private static Logger logger = Logger.getLogger(CMSCardAuthorizationFlags.class);

    public Boolean getAuthRequiredFlag() {
        return isAuthRequiredFlag;
    }

    public void setAuthRequiredFlag(Boolean authRequiredFlag) {
        isAuthRequiredFlag = authRequiredFlag;
    }

    public Boolean getPINRequiredFlag() {
        return isPINRequiredFlag;
    }

    public void setPINRequiredFlag(Boolean PINRequiredFlag) {
        isPINRequiredFlag = PINRequiredFlag;
    }

    public Boolean getCVVRequiredFlag() {
        return isCVVRequiredFlag;
    }

    public void setCVVRequiredFlag(Boolean CVVRequiredFlag) {
        isCVVRequiredFlag = CVVRequiredFlag;
    }

    public Boolean getCVV2RequiredFlag() {
        return isCVV2RequiredFlag;
    }

    public void setCVV2RequiredFlag(Boolean CVV2RequiredFlag) {
        isCVV2RequiredFlag = CVV2RequiredFlag;
    }

    public Boolean getARQCRequiredFlag() {
        return isARQCRequiredFlag;
    }

    public void setARQCRequiredFlag(Boolean ARQCRequiredFlag) {
        isARQCRequiredFlag = ARQCRequiredFlag;
    }

    public Boolean getARPCRequiredFlag() {
        return isARPCRequiredFlag;
    }

    public void setARPCRequiredFlag(Boolean ARPCRequiredFlag) {
        isARPCRequiredFlag = ARPCRequiredFlag;
    }

    public void checkPINValidationRequired(ProcessContext processContext) throws Exception {
        String pinData, posEntryMode, pinEntryCapability;
        Ifx ifx;

        ifx = processContext.getInputMessage().getIfx();
        posEntryMode = ifx.getPosEntryModeCode();
        pinData = ifx.getPINBlock();

        if(posEntryMode != null) {
        try {
            pinEntryCapability = posEntryMode.substring(2, 3);
            if (pinEntryCapability.equals(ISOPOSEntryMode.PINEntryCapability.TERMINAL_CAN_ACCEPT_PINS)) {
                logger.info("Terminal can accept PIN");

                if (!Util.hasText(pinData) && ifx.getTrnType().equals(TrnType.PURCHASE)) {
                    logger.info("Transaction is Purchase, bypassing PIN");
                    this.setPINRequiredFlag(Boolean.FALSE);

                } else if (Util.hasText(pinData)) {
                    this.setPINRequiredFlag(Boolean.TRUE);
                    logger.info("PIN found, setting flags");

                } else {
                    this.setPINRequiredFlag(Boolean.FALSE);
                    ifx.setRsCode(ISOResponseCodes.INVALID_IMD);
                    logger.info("PIN Data required but not present.");
                    throw new Exception("PIN Data required but not present. Error!!!");
                }
            } else {
                logger.info("Terminal cannot accept PIN. Resetting flag");
                this.setPINRequiredFlag(Boolean.FALSE);
            }
        } catch (Exception e) {
            throw e;
        }
    }
        else
        {
            logger.info("POS Entry Mode not present. Resetting PIN flag");
            this.setPINRequiredFlag(Boolean.FALSE);
        }
    }

    public void checkCVVValidationRequired(ProcessContext processContext) throws Exception {
        String posEntryMode, panEntryCapability, productId;
        CMSTrack2Format cmsTrack2Format;
        Ifx ifx;

        ifx = processContext.getInputMessage().getIfx();
        posEntryMode = ifx.getPosEntryModeCode();

        if(posEntryMode != null) {
        try {
            panEntryCapability = posEntryMode.substring(0, 2);
            if (panEntryCapability.equals(ISOPOSEntryMode.PANEntryMode.MST_READ_CVV_POSSIBLE)) {
                logger.info("CVV Check is possible");

                productId = ifx.getCmsCardRelation().getProductId();
                cmsTrack2Format = processContext.getCMSTrack2Format(productId);
                cmsTrack2Format.getTrack2InfoFromCard(ifx);

                this.setCVVRequiredFlag(Boolean.TRUE);

            } else {
                logger.info("CVV Check not possible. Resetting flag.");
                this.setCVVRequiredFlag(Boolean.FALSE);
            }
        } catch (Exception e) {
            throw e;
            }
        }
        else
        {
            logger.info("POS Entry Mode not present. Resetting CVV flag.");
            this.setCVVRequiredFlag(Boolean.FALSE);
        }
    }

    public void checkCVV2ValidationRequired(ProcessContext processContext) throws Exception {
        //NOTE: CVV2 appear in different fields for different payment schemes
        Ifx ifx;
        Channel channel;
        String cvv2Data, addDataPrivate;
        Integer index;

        ifx = processContext.getInputMessage().getIfx();
        channel = processContext.getInputMessage().getChannel();

        try {
            if (channel.getChannelId().equals(ChannelCodes.VISA_BASE_I)) {

                cvv2Data = ifx.getCVV2();
                if (Util.hasText(cvv2Data)) {
                    logger.info("CVV2 Data available in message");

                    //Position 1: Present indicator
                    if (cvv2Data.substring(0, 1).equals("1")) {
                        logger.info("Position 1: Present indicator => CVV2 value is present");

                        if (cvv2Data.length() == 6) {
                            logger.info("Position 3-6: CVV2 Value => CVV2 value found");
                            cvv2Data = cvv2Data.substring(2, 5);
                            ifx.getSafeCardAcctId().setCVV2(cvv2Data);
                            this.setCVV2RequiredFlag(Boolean.TRUE);

                        } else {
                            logger.info("Position 3-6: CVV2 Value => CVV2 value not found");
                            throw new Exception("Present Indicator shows CVV2 available but not found. Error!!!");
                        }
                    } else {
                        logger.info("Position 1: Present indicator => Other than CVV2 value is present. Resetting Flag.");
                        this.setCVV2RequiredFlag(Boolean.FALSE);
                    }
                } else {
                    logger.info("CVV2 Data not available. Resetting Flag");
                    this.setCVV2RequiredFlag(Boolean.FALSE);
                }
            } else if (channel.getChannelId().equals(ChannelCodes.MASTERCARD)) {
                //field 48
                addDataPrivate = ifx.getAddDataPrivate();

                //tag 92 for cvv2
                if (addDataPrivate.contains("92")) {
                    logger.info("CVV2 Data available in message");
                    index = addDataPrivate.indexOf("92");
                    index += 2;
                    cvv2Data = addDataPrivate.substring(index, index + 3);
                    ifx.getSafeCardAcctId().setCVV2(cvv2Data);
                    this.setCVV2RequiredFlag(Boolean.TRUE);

                } else {
                    logger.info("Tag 92 not available for CVV2. Resetting flags.");
                    this.setCVV2RequiredFlag(Boolean.FALSE);
                }
            } else {
                logger.info("No Payment Scheme available. Resetting flags.");
                this.setCVV2RequiredFlag(Boolean.FALSE);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void checkARQCValidationRequired(ProcessContext processContext) throws Exception {
        //TODO: need to add arqc validation flag handing
        try {
            this.setARQCRequiredFlag(Boolean.FALSE);
        } catch (Exception e) {
            throw e;
        }
    }

    public void checkARPCGenerationRequired(ProcessContext processContext) throws Exception {
        //TODO: need to add arpc generation flag handing
        try {
            this.setARPCRequiredFlag(Boolean.FALSE);
        } catch (Exception e) {
            throw e;
        }
    }

    public void checkAuthRequired() {
        setAuthRequiredFlag(this.getPINRequiredFlag() || this.getARPCRequiredFlag() || this.getARQCRequiredFlag()
                || this.getCVV2RequiredFlag() || this.getCVVRequiredFlag());
    }
}
