package vaulsys.protocols.ui;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.CMSIFXToHttpMapper;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.UiSpecificData;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

public class UiProtocolFunctions implements ProtocolFunctions {

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
			ByteArrayInputStream msgIn = new ByteArrayInputStream(rawdata);
			ObjectInputStream in = new ObjectInputStream(msgIn);
			MessageObject message = (MessageObject) in.readObject();
			return message;
		} catch (Exception e) {
			logger.error("Exception in producing UIMessage from Binary: "+e , e);
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
			MessageObject message = (MessageObject) protocolMessage;
			ByteArrayOutputStream msg = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(msg);
			out.writeObject(message);
			return msg.toByteArray();
			
		} catch (Exception e) {
			logger.error("Exception in producing OutgoingMessage.Binary from UIMessage"+ e/*, e*/);
			throw new NotProducedProtocolToBinaryException(e);
		}
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor) throws NotMappedProtocolToIfxException {
		try {
			MessageObject messageObject = (MessageObject) protocolMessage;
			IfxType type = messageObject.getIfxType();

			Ifx ifx = new Ifx();
			ifx.setIfxType(type);

			UiSpecificData data = new UiSpecificData();
			data.setUsername(messageObject.getUsername());
			
			if(IfxType.ATM_GO_IN_SERVICE.equals(type) || IfxType.ATM_GO_OUT_OF_SERVICE.equals(type) || 
			   IfxType.ATM_CONFIG_ID_LOAD.equals(type) || IfxType.ATM_ENHANCED_PARAMETER_TABLE_LOAD.equals(type) ||
			   IfxType.ATM_FIT_TABLE_LOAD.equals(type) || IfxType.ATM_SCREEN_TABLE_LOAD.equals(type) ||
			   IfxType.ATM_STATE_TABLE_LOAD.equals(type) || IfxType.ATM_DATE_TIME_LOAD.equals(type) ||
			   IfxType.MASTER_KEY_CHANGE_RQ.equals(type) || IfxType.MAC_KEY_CHANGE_RQ.equals(type) ||
			   IfxType.PIN_KEY_CHANGE_RQ.equals(type) || IfxType.ATM_GET_ALL_KVV.equals(type)){
				data.setTerminalCodes(messageObject.getParameter("atms").toString());
				data.setTerminalType(TerminalType.ATM);
			}
			
			ifx.setUiSpecificData(data);
			return ifx;
		} catch (Exception e) {
			logger.error("Exception in mapping UIMessage to IFX: "+ e);
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
