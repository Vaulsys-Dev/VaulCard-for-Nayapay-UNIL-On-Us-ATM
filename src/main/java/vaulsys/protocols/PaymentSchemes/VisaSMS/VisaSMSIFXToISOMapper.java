package vaulsys.protocols.PaymentSchemes.VisaSMS;

import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by HP on 11/23/2016.
 */
public class VisaSMSIFXToISOMapper extends IfxToISOMapper {
    transient Logger logger = Logger.getLogger(VisaSMSIFXToISOMapper.class);
    public static final VisaSMSIFXToISOMapper Instance = new VisaSMSIFXToISOMapper();

    protected VisaSMSIFXToISOMapper() {
    }

    @Override
    public ProtocolMessage map(Ifx ifxObj, EncodingConvertor convertor) throws Exception {

        String S63 = "";
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager isoPackager = ((VisaSMSProtocol) ProtocolProvider.Instance.getByClass(VisaSMSProtocol.class)).getPackager();
        isoMsg.setPackager(isoPackager);

        isoMsg.setMTI(ifxObj.getMti());

        if (ifxObj.getAppPAN() != null)
            isoMsg.set(2, StringUtils.leftPad(ifxObj.getAppPAN(), 20, "0"));

        String processCode = "";
        if (ifxObj.getAccTypeFrom().equals(AccType.MAIN_ACCOUNT))
            processCode = "00";
        else
            processCode = Integer.toString(ifxObj.getAccTypeFrom().getType());

        if (ifxObj.getAccTypeTo().equals(AccType.MAIN_ACCOUNT))
            processCode += "00";
        else
            processCode += Integer.toString(ifxObj.getAccTypeTo().getType());

        isoMsg.set(3, mapTrnType(ifxObj.getTrnType()) + processCode);

        Long amt = ifxObj.getAuth_Amt();
        if (ISOMessageTypes.isRequestMessage(ifxObj.getIfxType()))
        {
            if (ifxObj.getAuth_Amt() != null)
                isoMsg.set(4, ifxObj.getAuth_Amt().toString());
        }
        else
        {
            if (ifxObj.getTrx_Amt() != null)
            {
                isoMsg.set(4, ifxObj.getTrx_Amt().toString());
                amt = ifxObj.getTrx_Amt();
            }
        }

        if(isoMsg.getMTI().equals(ISOMessageTypes.FINANCIAL_REQUEST_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.REVERSAL_ADVICE_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.REVERSAL_RESPONSE_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87))
        {
            isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifxObj.getTrnDt().toDate()));

            isoMsg.set(11, ifxObj.getSrc_TrnSeqCntr());

            if (ifxObj.getFwdBankId() != null)
                isoMsg.set(33, StringUtils.leftPad(ifxObj.getFwdBankId().toString(), 12, "0"));

            isoMsg.set(37, ifxObj.getMyNetworkRefId());

            isoMsg.set(39, mapError(ifxObj.getIfxType(), ifxObj.getRsCode()));

            ifxObj.setNetworkData(S63);
            isoMsg.set(63, ifxObj.getNetworkData());

            isoMsg.set(70, ifxObj.getNetworkManageInfoCode());
        }

        isoMsg.set(12, ifxObj.getTimeLocalTran());

        isoMsg.set(13, ifxObj.getDateLocalTran());

        if (ifxObj.getExpDt() != null)
            isoMsg.set(14, ifxObj.getExpDt());

        isoMsg.set(18, ifxObj.getMerchantType());

        if(ifxObj.getMerchCountryCode() != null)
            isoMsg.set(19, ifxObj.getMerchCountryCode());

        isoMsg.set(22, ifxObj.getPosEntryModeCode());

        isoMsg.set(25, ifxObj.getPosConditionCode());

        if(!ifxObj.getMti().equals(ISOMessageTypes.REVERSAL_ADVICE_87))
        {
            if (ifxObj.getPosPinCaptureCode() != null)
                isoMsg.set(26, ifxObj.getPosPinCaptureCode());

            isoMsg.set(35, ifxObj.getTrk2EquivData());

            if (ifxObj.getPINBlock() != null && !ifxObj.getPINBlock().equals(""))
                isoMsg.set(52, StringUtils.leftPad(ifxObj.getPINBlock(), 38, "0"));

            isoMsg.set(53, ifxObj.getSecRelatedControlInfo());

            S63 = "1110002";
        }

        if(ifxObj.getAmountTranFee() != null)
            isoMsg.set(28, ifxObj.getAmountTranFee());

        isoMsg.set(32, StringUtils.leftPad(ifxObj.getBankId().toString(), 12, "0"));

        if(ifxObj.getMti() == ISOMessageTypes.REVERSAL_ADVICE_87.toString())
        {
            isoMsg.set(38, ifxObj.getMsgAuthCode());

            S63 = "310";

            StringBuilder S90 = new StringBuilder();

            if (ISOFinalMessageType.isReversalOrRepeatMessage(ifxObj.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifxObj.getIfxType())) {
                if (ifxObj.getSafeOriginalDataElements().getMessageType() == null) {
                    throw new ISOException("Invalid original data element: No Message Type for field 90");
                }

                S90.append(ifxObj.getSafeOriginalDataElements().getMessageType());

                try {
                    S90.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getTrnSeqCounter(), '0'));
                } catch (Exception e) {
                    S90.append("000000");
                }
                try {
                    S90.append(MyDateFormatNew.format("MMddHHmmss", ifxObj.getSafeOriginalDataElements().getOrigDt().toDate()));
                } catch (Exception e) {
                    S90.append("0000000000");
                }
                try {
                    S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getBankId(), '0'));
                } catch (Exception e) {
                    S90.append("00000000000");
                }
                try {
                    S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getFwdBankId(), '0'));
                } catch (Exception e) {
                    S90.append("00000000000");
                }
                isoMsg.set(90, S90.toString());
            }

            if(ifxObj.getMainAccountNumber() != null)
                isoMsg.set(102, StringUtils.leftPad(ifxObj.getMainAccountNumber(), 30, "0"));


        }
        //ifxObj.setVipPrivateUse(S63); //Raza commenting
        //isoMsg.set(63, ifxObj.getVipPrivateUse()); //Raza commenting

        isoMsg.set(41, ifxObj.getTerminalId());

        isoMsg.set(42, ifxObj.getOrgIdNum());

        isoMsg.set(43, ifxObj.getCardAcceptNameLoc());

        if (ifxObj.getAddDataPrivate() != null)
            isoMsg.set(48, StringUtils.leftPad(ifxObj.getAddDataPrivate(), 256, "0"));

        if (ifxObj.getAuth_Currency() != null)
            isoMsg.set(49, ifxObj.getAuth_Currency());

        String Bitmap_62 = "30000000";
        ifxObj.setCustomPaymentService(Bitmap_62);
        isoMsg.set(62, ifxObj.getCustomPaymentService());

        if (ifxObj.getKeyManagement() != null && Util.hasText(ifxObj.getKeyManagement().getKey()))
            isoMsg.set(new ISOBinaryField(96, Hex.decode(ifxObj.getKeyManagement().getKey())));

        if (ISOFinalMessageType.isResponseMessage(ifxObj.getIfxType())) {
            if (ifxObj.getRecvBankId() != null) {
                isoMsg.set(100, StringUtils.leftPad(ifxObj.getRecvBankId().toString(), 12, "0"));
            }
        }

        if (!isoMsg.getMTI().equals(ISOMessageTypes.REVERSAL_ADVICE_RESPONSE_87)) {
            //if (ifxObj.getAmountSettlement() != null)
                //isoMsg.set(5, ifxObj.getAmountSettlement());
            if (ifxObj.getSett_Amt() != null)
                isoMsg.set(5, ifxObj.getSett_Amt());

            //if (Util.hasText(ifxObj.getConvRateSettlement()))
                //isoMsg.set(9, ifxObj.getConvRateSettlement());
            if (ifxObj.getConvRate_Sett() != null)
                isoMsg.set(9, ifxObj.getConvRate_Sett());

            //if (ifxObj.getCurrCodeSettlement() != null)
                //isoMsg.set(50, ifxObj.getCurrCodeSettlement());

            if (ifxObj.getSett_Currency() != null)
                isoMsg.set(50, ifxObj.getSett_Currency());
        }

        if (ifxObj.getSec_Amt() != null) {
            isoMsg.set(6, ifxObj.getSec_Amt());
        }

        //setting Conversion Date
        //if (Util.hasText(ifxObj.getDateConversion())) {
        //    isoMsg.set(16, ifxObj.getDateConversion());
        //}
        if (Util.hasText(ifxObj.getSec_CurDate())) {
            isoMsg.set(16, ifxObj.getSec_CurDate());
        }


        if (ifxObj.getSec_Currency() != null)
            isoMsg.set(51, ifxObj.getSec_Currency());

        return isoMsg;
    }
}
