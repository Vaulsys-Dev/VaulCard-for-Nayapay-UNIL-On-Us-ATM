package vaulsys.protocols.cms;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class CMSHttpProtocolFunctions implements ProtocolFunctions{

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction)
			throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction)
			throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
		try {
			CMSHttpMessage message = new CMSHttpMessage();
			message.unpack(rawdata);
			return message;
		} catch (Exception e) {
			logger.error("Exception in producing CMSHttpMessage from Binary: "+e , e);
			throw new NotParsedBinaryToProtocolException(e);
		}
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {

		try {
			return CMSIFXToHttpMapper.Instance.map(ifx, convertor);
		} catch (Exception e) {
			logger.error("Exception in mapping IFX to CMSHttpMessage" + e, e);
			throw new NotMappedIfxToProtocolException(e);
		}
	}

	@Override
	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX, EncodingConvertor convertor) throws Exception {
		return null;
	}

	@Override
	public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException {
	}

	@Override
	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
		try {
			CMSHttpMessage message = (CMSHttpMessage) protocolMessage;
			return message.pack();
		} catch (Exception e) {
			logger.error("Exception in producing OutgoingMessage.Binary from CMSHttpMessage"+ e/*, e*/);
			throw new NotProducedProtocolToBinaryException(e);
		}
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor) throws NotMappedProtocolToIfxException {
		try {
			return CMSHttpToIFXMapper.Instance.map(protocolMessage, convertor);
		} catch (Exception e) {
			logger.error("Exception in mapping CMSHttpMessage to IFX: "+ e);
			throw new NotMappedProtocolToIfxException(e);
		}
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incoMessage) throws CantPostProcessBinaryDataException {
		return incoMessage.getBinaryData();
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
