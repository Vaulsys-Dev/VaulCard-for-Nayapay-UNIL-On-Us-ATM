package vaulsys.protocols.PaymentSchemes.ISOHost;

import vaulsys.message.Message;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class ISOHostProtocolFunctions extends ISOFunctions {
    @Override
    public ISOPackager getPackager() {
        return ((ISOHostProtocol) ProtocolProvider
                .Instance.getByClass(ISOHostProtocol.class))
                .getPackager();
    }

    @Override
    public ProtocolDialog getDialog() {
        return new ISOHostProtocolDialog();
    }

    @Override
    public IfxToProtocolMapper getIfxToProtocolMapper() {
        return ISOHostIFXToISOMapper.Instance;
    }

    @Override
    public ProtocolToIfxMapper getProtocolToIfxMapper() {
        return ISOHostISOToIFXMapper.Instance;
    }

    @Override
    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {

    }

    @Override
    public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {

    }

    @Override
    public byte[] preProcessBinaryMessage(Message incommingMessage) throws Exception {
        return incommingMessage.getBinaryData();
    }

    @Override
    public byte[] decryptSecureBinaryMessage(byte[] encryptedData, Message incomingMessage) throws Exception {
        return null;
    }

    @Override
    public byte[] encryptBinaryMessage(byte[] rawdata, Message incomingMessage) throws Exception {
        return null;
    }

    public static String getFunctionCode(String mti, NetworkManagementInfo networkManagementInfo) {
        String funcCode;

        switch (Integer.parseInt(mti)) {
            case 200:
                funcCode = "200";
                break;
            case 1200:
                funcCode = "200";
                break;
            case 800:
                if (networkManagementInfo.equals(NetworkManagementInfo.SIGN_ON))
                    funcCode = "001";
                else if (networkManagementInfo.equals(NetworkManagementInfo.SIGN_OFF))
                    funcCode = "002";
                else if (networkManagementInfo.equals(NetworkManagementInfo.ECHOTEST))
                    funcCode = "003";
                else
                    funcCode = "";
                break;
            default:
                funcCode = "";
                break;
        }

        return funcCode;
    }

    public static String getPOSEntryMode(Ifx ifxObj)
    {
        Boolean isPINAvailable, isTrack2DataAvailable;
        String posEntryMode;

        isTrack2DataAvailable = Util.hasText(ifxObj.getTrk2EquivData());
        isPINAvailable = Util.hasText(ifxObj.getPINBlock());

        if (isTrack2DataAvailable) {
            posEntryMode = "90";
        } else {
            posEntryMode = "01";
        }

        if (isPINAvailable)
            posEntryMode += "1";
        else
            posEntryMode += "2";

        return posEntryMode;
    }
}
