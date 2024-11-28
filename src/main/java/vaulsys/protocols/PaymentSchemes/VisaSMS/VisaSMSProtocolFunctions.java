package vaulsys.protocols.PaymentSchemes.VisaSMS;

import vaulsys.message.Message;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.transaction.Transaction;

/**
 * Created by HP on 11/23/2016.
 */
public class VisaSMSProtocolFunctions extends ISOFunctions {
    @Override
    public ISOPackager getPackager() {
        return ((VisaSMSProtocol) ProtocolProvider
                .Instance.getByClass(VisaSMSProtocol.class))
                .getPackager();
    }

    @Override
    public ProtocolDialog getDialog() {
        return new VisaSMSProtocolDialog();
    }

    @Override
    public IfxToProtocolMapper getIfxToProtocolMapper() {
        return VisaSMSIFXToISOMapper.Instance;
    }

    @Override
    public ProtocolToIfxMapper getProtocolToIfxMapper() {
        return VisaSMSISOToIFXMapper.Instance;
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
}
