package vaulsys.protocols.PaymentSchemes.NetworkUi;

import vaulsys.calendar.DateTime;
import vaulsys.cms.base.CMSCardRelation;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.*;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.CardAcctId;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISONetworkInfoCodes;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

public class NetworkUiProtocolFunctions implements ProtocolFunctions {

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
	public ProtocolMessage fromBinary(byte[] rawdata) throws Exception {
		try {

			int length;
			String messageString, key, value;
			MessageObject message;
			DocumentBuilderFactory documentBuilderFactory;
			DocumentBuilder documentBuilder;
			InputSource inputSource;
			Document document;
			NodeList nodeList;
			Element element, line;
			NodeList internalNodeList;
			Node keyNode, valueNode;
			CharacterData characterData;

			message = new MessageObject();
			messageString = new String(rawdata, "UTF-8");
			System.out.println("Received NetworkUI msg [" + messageString + "]"); //Raza TEMP

			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(messageString));

			document = documentBuilder.parse(inputSource);
			nodeList = document.getElementsByTagName("Node");

			length = nodeList.getLength();

			for (int i=0; i<length; i++) {
				element = (Element) nodeList.item(i);

				internalNodeList = element.getElementsByTagName("Key");
				line = (Element) internalNodeList.item(0);
				keyNode = line.getFirstChild();
				characterData = (CharacterData) keyNode;
				key = characterData.getData();

				internalNodeList = element.getElementsByTagName("Value");
				line = (Element) internalNodeList.item(0);
				valueNode = line.getFirstChild();
				characterData = (CharacterData) valueNode;
				value = characterData.getData();

				if (key.equals("COMMAND"))
					message.setCommand(value);
				else if (key.equals("CHANNELID"))
					message.setChannelId(value);
				else if (key.equals("WALLET_NO"))
					message.setWalletNo(value);
				else if (key.equals("AMOUNT"))
					message.setAmount(value);
				else if (key.equals("WALLET_FLAG"))
					message.setWalletFlag(value);
				else if (key.equals("PRODUCT_ID"))
					message.setProductId(value);
				else if (key.equals("PAN"))
					message.setPan(value);
				else if (key.equals("EXPIRY"))
					message.setExpiry(value);
				else if (key.equals("SERVICE_CODE"))
					message.setServiceCode(value);
				else if (key.equals("STAN"))
					message.setStan(value);
				else if (key.equals("DATE_TIME"))
					message.setDateTime(value);
			}

			message.setRequest(Boolean.TRUE);

			return message;

		} catch (Exception e) {
			logger.error("Exception in producing UIMessage from Binary: "+e , e);
			throw new NotParsedBinaryToProtocolException(e);
		}
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {

		MessageObject messageObject;
		try {
			messageObject = new MessageObject();
			if (ifx.getCardAcctId() != null && ifx.getCardAcctId().getCVV() != null)
				messageObject.setCvv(ifx.getCardAcctId().getCVV());
			messageObject.setResponseCode(ifx.getRsCode());
			messageObject.setRequest(Boolean.FALSE);
			return messageObject;
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
			String messageXML;
			messageXML = "<SwitchCommand><Map><Node><Key>RESPONSE_CODE</Key><Value>"
					+ message.getResponseCode() + "</Value>";

			if (Util.hasText(message.getCvv()))
				messageXML += "<Key>CVV</Key><Value>" + message.getCvv() + "</Value>";

			messageXML += "</Node></Map></SwitchCommand>";
			return messageXML.getBytes();

		} catch (Exception e) {
			logger.error("Exception in producing OutgoingMessage.Binary from UIMessage"+ e/*, e*/);
			throw new NotProducedProtocolToBinaryException(e);
		}
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor)
			throws NotMappedProtocolToIfxException {
		try {
			MessageObject messageObject;
			Ifx ifx;
			Channel channel;
			String command, value;
			Long amount;

			messageObject = (MessageObject) protocolMessage;
			ifx = new Ifx();
			command = messageObject.getCommand();

			if (command.equals("WALLET_TOPUP") || command.equals("WALLET_TOPUP_REVERSAL")) {
				value = messageObject.getWalletNo();
				if (!Util.hasText(value)) {
					throw new Exception("Wallet not found");

				} else {
					ifx.setAppPAN(value);
				}

				amount = Long.parseLong(messageObject.getAmount().replace(".", ""));
				if (amount == null) {
					throw new Exception("Amount not found");

				} else {
					ifx.setAuth_Amt(amount);
				}

				value = messageObject.getStan();
				if (!Util.hasText(value)) {
					throw new Exception("STAN not found");

				} else {
					ifx.setSrc_TrnSeqCntr(value);
					ifx.setMy_TrnSeqCntr(value);
				}

				value = messageObject.getDateTime();
				if (!Util.hasText(value)) {
					throw new Exception("DateTime not found");

				} else {
					ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", value)));
					ifx.setOrigDt(ifx.getTrnDt());
					if (value.length() > 10) {
						ifx.setDateLocalTran(value.substring(4,8));
						ifx.setTimeLocalTran(value.substring(8,14));
					} else {
						ifx.setDateLocalTran(value.substring(0,4));
						ifx.setTimeLocalTran(value.substring(4,10));
					}
				}

				ifx.setAddDataPrivate(messageObject.getWalletFlag());
				ifx.setTrnType(TrnType.WALLET_TOPUP);

				if (command.equals("WALLET_TOPUP")) {
					ifx.setIfxType(IfxType.WALLET_TOPUP_RQ);
					ifx.setMti(ISOMessageTypes.FINANCIAL_REQUEST_87);

				} else {
					ifx.setIfxType(IfxType.WALLET_TOPUP_REV_REPEAT_RQ);
					ifx.setMti(ISOMessageTypes.REVERSAL_ADVICE_87);
				}

			} else {
				if (command.equals("CVV_GENERATION")) {
					ifx.setCmsCardRelation(new CMSCardRelation());
					value = messageObject.getProductId();

					if (!Util.hasText(value)) {
						throw new Exception("Product no found");

					} else {
						ifx.getCmsCardRelation().setProductId(value);
					}

					ifx.setCardAccId(new CardAcctId());
					value = messageObject.getPan();
					if (!Util.hasText(value)) {
						throw new Exception("PAN no found");

					} else {
						ifx.getCardAcctId().setAppPAN(value);
					}

					value = messageObject.getExpiry();
					if (!Util.hasText(value)) {
						throw new Exception("Expiry no found");

					} else {
						ifx.getCardAcctId().setExpDt(Long.parseLong(value));
					}

					value = messageObject.getServiceCode();
					if (!Util.hasText(value)) {
						throw new Exception("Service Code no found");

					} else {
						ifx.getCardAcctId().setServiceCode(value);
					}

					ifx.setIfxType(IfxType.CVV_GENERATION_RQ);
					ifx.setTrnType(TrnType.CVV_GENERATION);
					ifx.setMti(ISOMessageTypes.ADMINISTRATIVE_REQUEST_87);
					channel = ProcessContext.get().getInputMessage().getChannel();

				} else {
					if (command.equals("SIGNIN")) {
						ifx.setIfxType(IfxType.SIGN_ON_RQ);

					} else if (command.equals("SIGNOFF")) {
						ifx.setIfxType(IfxType.SIGN_OFF_RQ);

					} else if (messageObject.getCommand().equals("ECHO")) {
						ifx.setIfxType(IfxType.ECHO_RQ);

					} else if (command.equals("KEYEXCHANGE")) {
						ifx.setIfxType(IfxType.KEY_EXCHANGE_RQ);

					} else {
						throw new Exception("Invalid command received");
					}

					channel = GlobalContext.getInstance().getChannel(messageObject.getChannelId());
					setChannelNetworkManageInfoCode(channel.getChannelId(), ifx);
					ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
					setChannelNetworkMTI(channel.getChannelId(), ifx);
					ifx.setFwdBankId(ProcessContext.get().getInstitution(
							channel.getInstitutionId()).getBin().toString());
				}

				value = String.format("%6s",
						GlobalContext.getInstance().getSysTraceAuditNo(channel.getName())).replace(' ', '0');
				ifx.setSrc_TrnSeqCntr(value);
				ifx.setMy_TrnSeqCntr(value);
				DateTime now = DateTime.now();
				ifx.setTrnDt(now);
				ifx.setOrigDt(now);
			}

			ifx.setNetworkRefId(String.format("%12s", ifx.getSrc_TrnSeqCntr()).replace(' ', '0'));
			ifx.setBankId(ProcessContext.get().getMyInstitution().getBin().toString());
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

	public void setChannelNetworkManageInfoCode (String channelId, Ifx ifx) {

		if (channelId.equals(ChannelCodes.UNION_PAY)) {
			if (ifx.getIfxType().equals(IfxType.SIGN_ON_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.SIGN_ON_001);
			} else if (ifx.getIfxType().equals(IfxType.SIGN_OFF_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.SIGN_OFF_002);
			} else if (ifx.getIfxType().equals(IfxType.ECHO_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.ECHO);
			} else if (ifx.getIfxType().equals(IfxType.KEY_EXCHANGE_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.KEY_EXCHANGE_101);
			}
		} else if (channelId.equals(ChannelCodes.VISA_BASE_I)) {
			if (ifx.getIfxType().equals(IfxType.SIGN_ON_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.SIGN_ON_071);
			} else if (ifx.getIfxType().equals(IfxType.SIGN_OFF_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.SIGN_OFF_072);
			} else if (ifx.getIfxType().equals(IfxType.ECHO_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.ECHO);
			} else if (ifx.getIfxType().equals(IfxType.KEY_EXCHANGE_RQ)) {
				ifx.setNetworkManageInfoCode(ISONetworkInfoCodes.KEY_EXCHANGE_161);
			}
		}
	}

	public void setChannelNetworkMTI (String channelId, Ifx ifx) {

		if (channelId.equals(ChannelCodes.UNION_PAY)) {
			ifx.setMti(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87);

		} else {
			ifx.setMti(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87);

		}
	}
}
