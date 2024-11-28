package vaulsys.protocols.ndc;

import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandOARMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusEncryptorInitialisationDataMsg;
import vaulsys.protocols.ndc.constants.NDCMessageClassSolicitedUnSokicited;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.protocols.ndc.parsers.NDCConsumerRequestMapper;
import vaulsys.protocols.ndc.parsers.NDCFunctionCommandMapper;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.constants.NDCUtil;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public class NDCProtocolFunctions implements ProtocolFunctions{
	transient Logger logger = Logger.getLogger(this.getClass());

	@Override
	public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
		NDCMessageClassSolicitedUnSokicited solicited = NDCMessageClassSolicitedUnSokicited.getByCode((char) rawdata[0]);
		NDCMessageClassTerminalToNetwork messageType = NDCMessageClassTerminalToNetwork.getByCode((char)rawdata[1]);

		if (NDCMessageClassTerminalToNetwork.CONSUMER_REQUEST_OPERATIONAL_MESSAGE.equals(messageType))
			return NDCConsumerRequestMapper.fromBinary(3, rawdata, messageType, solicited);

		if (NDCMessageClassTerminalToNetwork.STATUS_MESSAGE.equals(messageType)) {
			if (NDCMessageClassSolicitedUnSokicited.UNSOLICITED_MESSAGE.equals(solicited))
				return NDCUnsolicitedStatusMsg.fromBinary(rawdata, 3);
			else
				return NDCSolicitedStatusMsg.fromBinary(rawdata, 3);
		}

//		FIXME: Solicited and MessageType='3' is 'Encryptor Initialisation Data'
//		if(NDCMessageClassTerminalToNetwork.WRITE_COMMAND.equals(messageType)) {
//			throw new NotParsedBinaryToProtocolException("Write commands are not designed to be transmitted to network.");
//		}

		if (NDCMessageClassSolicitedUnSokicited.SOLICITED_MESSAGE.equals(solicited) && 
				NDCMessageClassTerminalToNetwork.ENCRYPTOR_INITIALISATION_DATA.equals(messageType)) {
			return new NDCSolicitedStatusEncryptorInitialisationDataMsg(rawdata, 3);
		}

		if (NDCMessageClassTerminalToNetwork.FUNCTION_COMMAND.equals(messageType))
			throw new NotParsedBinaryToProtocolException(
					"Function commands are not designed to be transmitted to network.");

		throw new NotParsedBinaryToProtocolException("Invalid NDC message identifier.");
	}

	@Override
	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
		NDCMsg ndcMsg = (NDCMsg) protocolMessage;
		try{
			return ndcMsg.toBinary();
		} catch (Exception e) {
			throw new NotProducedProtocolToBinaryException("Can't convert ndcmessage to binary", e);
		}
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException{
		try{
			return NDCFunctionCommandMapper.fromIfx(ifx, convertor);
		} catch (Exception e){
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
		}

		return null;
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor) throws NotMappedProtocolToIfxException{
		try{
			NDCMsg ndcMessage = (NDCMsg) protocolMessage;
			return ndcMessage.toIfx();
		} catch (Exception e) {
			throw new NotMappedProtocolToIfxException(e);
		}
	}

	@Override
	public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction)
			throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction)
			throws CantAddNecessaryDataToIfxException{
	}

	@Override
	public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException{
		try	{
			Channel channel = outgoingMessage.getChannel();
			ProtocolSecurityFunctions securityFunctions = channel.getProtocol().getSecurityFunctions();
			Terminal terminal = null;
			
			if (outgoingMessage.getEndPointTerminal() != null)
				terminal = outgoingMessage.getEndPointTerminal();
			else {
				terminal = TerminalService.findTerminal(ATMTerminal.class, ((NDCMsg)outgoingMessage.getProtocolMessage()).getLogicalUnitNumber());
				outgoingMessage.setEndPointTerminal(terminal);
			}
			
			NDCMsg ndcMsg = (NDCMsg) outgoingMessage.getProtocolMessage();
			if(!(ndcMsg instanceof NDCWriteCommandOARMsg)){
				
				if (NDCUtil.isNeedSetMac(ndcMsg))
					securityFunctions.setMac(processContext, terminal, terminal.getOwnOrParentSecurityProfileId(), terminal.getKeySet(), outgoingMessage, channel.getMacEnable());
			}
			
		} catch (Exception e){
			throw new CantPostProcessBinaryDataException(e);
		}
	}

	private void findProfileAndSetMac(Message outgoingMessage) throws Exception{
		Channel channel = outgoingMessage.getChannel();
		NDCMsg msg = (NDCMsg) outgoingMessage.getProtocolMessage();

		if (channel.getMacEnable()){
			Long securityProfileId = null;
			Set<SecureKey> keySet = null;

			Long code;
//			if (outgoingMessage.getEndPoint() != null)
//				code = extractEndPointCode(outgoingMessage);
			if (outgoingMessage.getEndPointTerminal() != null){
				code = outgoingMessage.getEndPointTerminal().getCode(); 
			} else{
				code = msg.getLogicalUnitNumber();
//				EndPoint endPoint = new EndPoint(code, EndPointType.Terminal);
//				outgoingMessage.setEndPoint(endPoint);
				Terminal t = TerminalService.findTerminal(ATMTerminal.class, code);
				outgoingMessage.setEndPointTerminal(t);
			}

			if (outgoingMessage.getChannel().getEndPointType() != null
					&& outgoingMessage.getChannel().getEndPointType().equals(EndPointType.ATM_TERMINAL)){
				// TerminalManager manager = new TerminalManager();
				Terminal t = TerminalService.findTerminal(ATMTerminal.class, code);
				// Terminal t = manager.get(code);
				if (t != null){
					securityProfileId = t.getOwnOrParentSecurityProfileId();
					keySet = t.getKeySet();
				}

				byte[] binaryData = outgoingMessage.getBinaryData();
				byte[] data = new byte[binaryData.length - 9];
				System.arraycopy(binaryData, 0, data, 0, data.length);
				byte[] mac = SecurityComponent.generateCBC_MAC(securityProfileId, keySet, data);
				String MAC = new String(Hex.encode(mac)).toUpperCase();
				logger.debug("SENT MAC: " + MAC);
				byte[] binaryMsg = new byte[outgoingMessage.getBinaryData().length];
				System.arraycopy(binaryData, 0, binaryMsg, 0, binaryMsg.length - MAC.length());
				System.arraycopy(MAC.getBytes(), 0, binaryMsg, binaryMsg.length - MAC.length(), MAC.length());
				outgoingMessage.setBinaryData(binaryMsg);
			}
		}
	}

	@Override
	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIfx, EncodingConvertor convertor) throws Exception {
		return NDCFunctionCommandMapper.fromIfx(incomingIfx, convertor);
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incoMessage) throws CantPostProcessBinaryDataException {
		return incoMessage.getBinaryData();
	}

	@Override
	public byte[] decryptSecureBinaryMessage(byte[] encryptedData, Message incomingMessage) throws Exception {
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
