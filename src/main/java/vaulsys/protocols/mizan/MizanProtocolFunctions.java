package vaulsys.protocols.mizan;

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

public class MizanProtocolFunctions extends ISOFunctions {

	@Override
	public ProtocolDialog getDialog() {
		return new MizanProtocolDialog();
	}

	@Override
	public IfxToProtocolMapper getIfxToProtocolMapper() {
		return MizanIfxToISOMapper.Instance;
	}

	@Override
	public ISOPackager getPackager() {
		return ((MizanProtocol) ProtocolProvider.Instance.getByClass(MizanProtocol.class)).getPackager();
	}

	@Override
	public ProtocolToIfxMapper getProtocolToIfxMapper() {
		return MizanISOToIfxMapper.Instance;
	}

	@Override
	public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public void addOutgoingNecessaryData(Ifx outgoingIFX,
										 Transaction transaction) throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incommingMessage) throws Exception {
		return incommingMessage.getBinaryData();
	}

	@Override
	public byte[] decryptSecureBinaryMessage(byte[] encryptedData,
			Message incomingMessage) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] encryptBinaryMessage(byte[] rawdata, Message incomingMessage)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
