package vaulsys.protocols.apacs70;

import java.util.Arrays;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.apacs70.base.ApacsMsgType;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.apacs70.base.RsBaseMsg;
import vaulsys.protocols.apacs70.base.RsFinNetMsg;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class Apacs70ProtocolFunctions implements ProtocolFunctions {
	private static final Logger logger = Logger.getLogger(Apacs70ProtocolFunctions.class);

	@Override
	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
		try {
			//Apacs70Msg message = (Apacs70Msg) protocolMessage;
			//return message.pack();

			ApacsByteArrayWriter out = new ApacsByteArrayWriter();
			RsBaseMsg rsMsg = (RsBaseMsg) protocolMessage;
			rsMsg.pack(out);
			return out.toByteArray();
		} catch (Exception e) {
			logger.error("Exception in producing OutgoingMessage.Binary from Apacs70Msg" + e, e);
			throw new NotProducedProtocolToBinaryException(e);
		}
	}

	@Override
	public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
		try {
			//Apacs70Msg message = new Apacs70Msg(true);
			//message.unpack(rawdata);
			//return message;

			RqBaseMsg rqMsg = RqBaseMsg.createRqMsg(rawdata);
			return rqMsg;
		} catch (Exception e) {
			logger.error("Exception in producing Apacs70Msg from Binary: " + e, e);
			throw new NotParsedBinaryToProtocolException(e);
		}
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor) {
		//return Apacs70ToIfxMapper.Instance.map(protocolMessage, convertor);

		Ifx ifx = new Ifx();
		RqBaseMsg rqMsg = (RqBaseMsg) protocolMessage;
		rqMsg.toIfx(ifx);
		return ifx;
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {
		try {
			//return IfxToApacs70Mapper.Instance.map(ifx, convertor);

			RsBaseMsg rsMsg = RsBaseMsg.createRs(ifx);
			return rsMsg;
		} catch (Exception ex) {
			logger.error("Exception in mapping IFX to Apacs70Msg", ex);
			throw new NotMappedIfxToProtocolException(ex);
		}
	}

	@Override
	public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException {
		try {
			Channel channel = outgoingMessage.getChannel();
			ProtocolSecurityFunctions securityFunctions = channel.getProtocol().getSecurityFunctions();
			Terminal t = outgoingMessage.getEndPointTerminal();

			if (t != null)
				securityFunctions.setMac(processContext, t, t.getOwnOrParentSecurityProfileId(), t.getKeySet(), outgoingMessage, channel.getMacEnable());

			// ISOMACUtils.findProfilesAndSetMac(outgoingMessage);

		} catch (Exception e) {
			throw new CantPostProcessBinaryDataException(e);
		}
	}

	public byte[] preProcessBinaryMessage(Message incommingMessage) throws Exception {
		return incommingMessage.getBinaryData();
	}

	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX,
			EncodingConvertor convertor) throws Exception {
		logger.info(">> OUTGOING FROM INCOMING <<");
		RqBaseMsg inMsg = (RqBaseMsg) incomingMessage;
		RsFinNetMsg outMsg = new RsFinNetMsg();
		outMsg.dialIndicator = inMsg.dialIndicator;
		outMsg.terminalIdentity = inMsg.terminalIdentity;
		outMsg.messageNumber = inMsg.messageNumber;
		outMsg.messageType = ApacsMsgType.toRs(inMsg.messageType);
		outMsg.acquirerResponseCode = "12";
		outMsg.dateAndTime = MyDateFormatNew.format("yyMMddHHmmss", DateTime.now().toDate());
		outMsg.confirmationRequest = 0;

		outMsg.MAC = "01020304";
		return outMsg;
	}

	@Override
	public byte[] decryptSecureBinaryMessage(byte[] encryptedData, Message incomingMessage) throws Exception {
		byte[] encryptedKey = Arrays.copyOfRange(encryptedData, 0, 128);
		byte[] encryptedMsgBody = Arrays.copyOfRange(encryptedData, 128, encryptedData.length);
		
		byte[] messageKeys = Arrays.copyOfRange(SecurityComponent.rsaDecrypt(encryptedKey), 0, 24);
		byte[] decryptedMsgBody = SecurityComponent.tripleDesDecrypt(encryptedMsgBody, Arrays.copyOfRange(messageKeys, 0, 8), Arrays.copyOfRange(messageKeys, 8, 16), Arrays.copyOfRange(messageKeys, 16, 24));

        int b1 = HSMUtil.byteToInt(decryptedMsgBody[0]);
        int b2 = HSMUtil.byteToInt(decryptedMsgBody[1]);
        int len = (b1 * 256) + b2 + 2;
        byte[] result = Arrays.copyOfRange(decryptedMsgBody, 2, len);

        incomingMessage.setSecurityKey(messageKeys);
        incomingMessage.setBinaryData(result);
        GeneralDao.Instance.saveOrUpdate(incomingMessage);
        return result;
	}
	
	@Override
	public byte[] encryptBinaryMessage(byte[] rawdata, Message incomingMessage) throws Exception {
		byte[] messageKeys = incomingMessage.getSecurityKey();
		
		int len = rawdata.length + 2;
		int mod8 = (len % 8);
		int newSize = mod8 == 0 ? len : len + (8 - mod8); 
		byte[] newData = new byte[newSize];
		newData[0] = (byte) (rawdata.length / 256); 
		newData[1] = (byte) (rawdata.length % 256);
		for (int i = 2; i < newSize; i++) {
			if (i < (rawdata.length + 2)) {
				newData[i] = rawdata[i - 2];
			} else {
				newData[i] = 0;
			}
		}
		
//		System.out.println("\n=========\n newData = " + new String(Hex.encode(newData)));
		return SecurityComponent.tripleDesEncrypt(newData, Arrays.copyOfRange(messageKeys, 0, 8), Arrays.copyOfRange(messageKeys, 8, 16), Arrays.copyOfRange(messageKeys, 16, 24));
	}

}
