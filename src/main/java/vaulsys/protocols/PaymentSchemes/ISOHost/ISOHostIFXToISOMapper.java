package vaulsys.protocols.PaymentSchemes.ISOHost;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class ISOHostIFXToISOMapper extends IfxToISOMapper {

    transient Logger logger = Logger.getLogger(ISOHostIFXToISOMapper.class);
    public static final ISOHostIFXToISOMapper Instance = new ISOHostIFXToISOMapper();

    protected ISOHostIFXToISOMapper() {
    }

    @Override
    public ProtocolMessage map(Ifx ifxObj, EncodingConvertor convertor) throws Exception {

        String value;
        TerminalType terminalType;

        ISOMsg isoMsg = new ISOMsg();
        ISOPackager isoPackager = ((ISOHostProtocol) ProtocolProvider.Instance.getByClass(ISOHostProtocol.class)).getPackager();
        isoMsg.setPackager(isoPackager);

        isoMsg.setMTI(ifxObj.getMti());

        if (ifxObj.getAppPAN() != null)
            isoMsg.set(2, ifxObj.getAppPAN());

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

        if (ISOFinalMessageType.isRequestMessage(ifxObj.getIfxType())) {
            if (ifxObj.getAuth_Amt() != null)
                isoMsg.set(4, ifxObj.getAuth_Amt().toString());

        } else {
            if (ifxObj.getTrx_Amt() != null)
                isoMsg.set(4, ifxObj.getTrx_Amt().toString());
        }

        if (ifxObj.getSett_Amt() != null)
            isoMsg.set(5, ifxObj.getSett_Amt());

        if (ifxObj.getSec_Amt() != null)
            isoMsg.set(6, ifxObj.getSec_Amt());

        if (ifxObj.getTrnDt() != null)
            isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifxObj.getTrnDt().toDate()));

        if (Util.hasText(ifxObj.getConvRate_Sett()))
            isoMsg.set(9, ifxObj.getConvRate_Sett());

        isoMsg.set(10, ifxObj.getSec_CurRate());

        isoMsg.set(11, ifxObj.getSrc_TrnSeqCntr());

        isoMsg.set(12, ifxObj.getTimeLocalTran());
        isoMsg.set(13, ifxObj.getDateLocalTran());

        if (ifxObj.getExpDt() != null)
            isoMsg.set(14, ifxObj.getExpDt());

        if (ifxObj.getSettleDt() != null)
            isoMsg.set(15, MyDateFormatNew.format("MMdd", ifxObj.getSettleDt()));

        //setting Conversion Date
        if (Util.hasText(ifxObj.getSec_CurDate()))
            isoMsg.set(16, ifxObj.getSec_CurDate());

        //setting Merchant Type
        terminalType = ifxObj.getTerminalType();
        if (terminalType.equals(TerminalType.POS)) {
            if (ifxObj.getTrnType().equals(TrnType.WITHDRAWAL))
                value = "6010";
            else
                value = "6012";
        } else if (terminalType.equals(TerminalType.ATM))
            value = "6011";
        else
            value = "0000";

        ifxObj.setMerchantType(value);
        isoMsg.set(18, ifxObj.getMerchantType());

        //setting POS Entry Mode Code
        ifxObj.setMerchantType(ISOHostProtocolFunctions.getPOSEntryMode(ifxObj));
        isoMsg.set(22, ifxObj.getMerchantType());

        //setting function code
        value = ISOHostProtocolFunctions.getFunctionCode(ifxObj.getMti(),
                ifxObj.getNetworkManagementInformationCode());
        isoMsg.set(24, value);

        //setting POS PIN Capture Code
        value = ifxObj.getPosPinCaptureCode();
        if (!Util.hasText(value)) {
            value = "06";
            ifxObj.setPosPinCaptureCode(value);
        }
        isoMsg.set(26, ifxObj.getPosPinCaptureCode());

        //setting Amount Transaction Fee
        if (Util.hasText(ifxObj.getAmountTranFee()))
            isoMsg.set(28, ifxObj.getAmountTranFee());

        if (ifxObj.getBankId() != null)
            isoMsg.set(32, ifxObj.getBankId().toString());

        if (ifxObj.getFwdBankId() != null)
            isoMsg.set(33, ifxObj.getFwdBankId());

        isoMsg.set(35, ifxObj.getTrk2EquivData());

        isoMsg.set(37, ifxObj.getMyNetworkRefId());

        isoMsg.set(39, mapError(ifxObj.getIfxType(), ifxObj.getRsCode()));

        isoMsg.set(41, fillFieldANFix(ifxObj, 41));

        isoMsg.set(42, fillFieldANFix(ifxObj, 42));

        //isoMsg.set(new ISOBinaryField(43, fillField43(ifxObj, convertor)));
        isoMsg.set(43, ifxObj.getCardAcceptNameLoc());

        isoMsg.set(49, ifxObj.getAuth_Currency());

        if (ifxObj.getSett_Currency() != null)
            isoMsg.set(50, ifxObj.getSett_Currency());

        if (ifxObj.getSec_Currency() != null)
            isoMsg.set(51, ifxObj.getSec_Currency());

        if (ifxObj.getNetworkManagementInformationCode() != null)
            isoMsg.set(70, Integer.toString(ifxObj.getNetworkManagementInformationCode().getType()));

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

        if (ISOFinalMessageType.isResponseMessage(ifxObj.getIfxType())) {
            if (ifxObj.getRecvBankId() != null) {
                isoMsg.set(100, ifxObj.getRecvBankId().toString());
            }
        }

        if (Util.hasText(ifxObj.getAccountId1()))
            isoMsg.set(102, ifxObj.getAccountId1());

        if (Util.hasText(ifxObj.getAccountId2()))
            isoMsg.set(103, ifxObj.getAccountId2());

        isoMsg.setDirection(ISOMsg.OUTGOING);

        return isoMsg;
    }
}
