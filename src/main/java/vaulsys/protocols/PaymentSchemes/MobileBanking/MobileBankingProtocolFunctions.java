package vaulsys.protocols.PaymentSchemes.MobileBanking;

import vaulsys.message.Message;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class MobileBankingProtocolFunctions extends ISOFunctions {
    @Override
    public ISOPackager getPackager() {
        return ((MobileBankingProtocol) ProtocolProvider
                .Instance.getByClass(MobileBankingProtocol.class))
                .getPackager();
    }

    @Override
    public ProtocolDialog getDialog() {
        return new MobileBankingProtocolDialog();
    }

    @Override
    public IfxToProtocolMapper getIfxToProtocolMapper() {
        return MobileBankingIFXToISOMapper.Instance;
    }

    @Override
    public ProtocolToIfxMapper getProtocolToIfxMapper() {
        return MobileBankingISOToIFXMapper.Instance;
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
