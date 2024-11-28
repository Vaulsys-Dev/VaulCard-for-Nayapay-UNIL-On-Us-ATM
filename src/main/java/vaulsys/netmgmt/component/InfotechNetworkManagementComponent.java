package vaulsys.netmgmt.component;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.exception.base.DecisionMakerException;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.netmgmt.exceptions.InvalidNetworkMessageException;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.POSTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.KIOSKCardPresentTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.TransactionService;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.MyInteger;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class InfotechNetworkManagementComponent {

    private static transient Logger logger = Logger.getLogger(InfotechNetworkManagementComponent.class);

	private InfotechNetworkManagementComponent() {}
    
    public static NetworkManagementAction processISONetworkManagementMessage(ProcessContext processContext, Message inputMessage)
            throws InvalidNetworkMessageException, Exception {

        ISOMsg isoMsg = (ISOMsg) inputMessage.getProtocolMessage();

        Integer emvTrnType = Integer.parseInt(isoMsg.getString(3).substring(0, 2).trim());

        Terminal endpointTerminal = TerminalService.findEndpointTerminalForMessageWithoutIFX(inputMessage, Long.parseLong(isoMsg.getString(41)));
        inputMessage.setEndPointTerminal(endpointTerminal);
        inputMessage.setIfx(createIncomingIfx(inputMessage));
        
        inputMessage.getTransaction().setDebugTag(inputMessage.getIfx().getIfxType().toString());
        LifeCycle lifeCycle = new LifeCycle();
        lifeCycle.setIsComplete(true);
        GeneralDao.Instance.saveOrUpdate(lifeCycle);
		inputMessage.getTransaction().setLifeCycle(lifeCycle);
        GeneralDao.Instance.saveOrUpdate(inputMessage.getIfx());
        GeneralDao.Instance.saveOrUpdate(inputMessage);
        GeneralDao.Instance.saveOrUpdate(inputMessage.getMsgXml());
        GeneralDao.Instance.saveOrUpdate(inputMessage.getTransaction());

        if(endpointTerminal == null) {
//        	inputMessage.getTransaction().setAuthorized(false);
        	return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
        }
        
		if(IfxType.LOG_ON_RQ.equals(inputMessage.getIfx().getIfxType())){
    		TerminalService.removeKeySet(endpointTerminal);
    		
    		if (inputMessage.getIfx().getTerminalType().equals(TerminalType.POS))
    			POSTerminalService.addDefaultKeySetForTerminal((POSTerminal) endpointTerminal);
    		
    		else if (inputMessage.getIfx().getTerminalType().equals(TerminalType.KIOSK_CARD_PRESENT))
    			POSTerminalService.addDefaultKeySetForTerminal((KIOSKCardPresentTerminal) endpointTerminal);

		}
		
		
		if (emvTrnType.equals(ISOTransactionCodes.LOG_ON) || emvTrnType.equals(ISOTransactionCodes.RESET_PASSWORD)) {
			try {
				TransactionService.checkValidityOfLastTransactionStatus(endpointTerminal, inputMessage.getIfx());
			} catch(Exception e) {
				logger.error("Error in putting desired flag on last transaction, received message is LOG_ON or RESET_PASSWORD", e);
			}
		}
			
        try {
//			inputMessage.getTransaction().setAuthorized(
					authorizeMessage(inputMessage);
//					);
		} catch (AuthorizationException e) {
//			inputMessage.getTransaction().setAuthorized(false);
			if (e instanceof DecisionMakerException) {
                DecisionMakerException dec = ((DecisionMakerException) e);
                dec.showCause(inputMessage.getIfx());
			}
			/****LEILA???***/
			return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
		}
		
		if (emvTrnType.equals(ISOTransactionCodes.LOG_ON) || emvTrnType.equals(ISOTransactionCodes.RESET_PASSWORD)) {
            Message outMsg = generateRs(processContext, inputMessage);
            processContext.getTransaction().addOutputMessage(outMsg);
            GeneralDao.Instance.saveOrUpdate(endpointTerminal);
            return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;

        }

        return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
    }

    private static Boolean authorizeMessage(Message message) throws Exception {

    	Terminal terminal = message.getEndPointTerminal();
    	
    	
    	if (terminal instanceof POSTerminal && !message.getIfx().getSerialno().equals(((POSTerminal)terminal).getSerialno())) {
    		logger.warn("Incorrect serial No. pos serialNo: " + ((POSTerminal)terminal).getSerialno() + ", message serialNo: " + message.getIfx().getSerialno());
    		throw new AuthorizationException("Incorrect serial No. pos serialNo: " + ((POSTerminal)terminal).getSerialno() + ", message serialNo: " + message.getIfx().getSerialno(), true);
    	} 
    	
    	if (terminal instanceof KIOSKCardPresentTerminal && !message.getIfx().getSerialno().equals(((KIOSKCardPresentTerminal)terminal).getSerialno())) {
    		logger.warn("Incorrect serial No. kiosk serialNo: " + ((KIOSKCardPresentTerminal)terminal).getSerialno() + ", message serialNo: " + message.getIfx().getSerialno());
    		throw new AuthorizationException("Incorrect serial No. kiosk serialNo: " + ((KIOSKCardPresentTerminal)terminal).getSerialno() + ", message serialNo: " + message.getIfx().getSerialno(), true);
    	}
    	
    	if (message.getChannel().getMacEnable()) {
			try {
				String mac = ((ISOMsg)message.getProtocolMessage()).getString(64);
				if (mac == null) {
					logger.warn("Failed: Mac Verification failed! (mac =null)");
					
					//TODO check more carefully later!
					throw new MacFailException("Failed:Mac verification failed.(mac = null)");
				}

				ProtocolSecurityFunctions securityFunctions = message.getChannel().getProtocol().getSecurityFunctions();
				securityFunctions.verifyMac(terminal, terminal.getOwnOrParentSecurityProfileId(), terminal.getKeySet(), mac, message.getBinaryData(), message.getChannel().getMacEnable());

			} catch (Exception e) {
				throw e;
			}
		}
    	
		return true;
	}

	private static Message generateRs(ProcessContext processContext, Message inputMessage) throws ISOException, IOException {
    	Message outgoingMessage = new Message(MessageType.OUTGOING);
        outgoingMessage.setTransaction(inputMessage.getTransaction());

        if (CommunicationMethod.ANOTHER_SOCKET.equals(inputMessage.getChannel().getCommunicationMethod()))
            outgoingMessage.setChannel(((InputChannel) inputMessage.getChannel()).getOriginatorChannel());
        else
            outgoingMessage.setChannel(inputMessage.getChannel());

        Terminal terminal = inputMessage.getEndPointTerminal();
		outgoingMessage.setEndPointTerminal(terminal);

        ISOMsg outMsg = (ISOMsg) inputMessage.getProtocolMessage();
        Ifx outIfx = null;
        try {
        	outIfx = creatOutgoingIfx(outgoingMessage, inputMessage.getIfx());
			outgoingMessage.setIfx(outIfx);
        	GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
        } catch (CloneNotSupportedException e) {
        	logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
        }
        outMsg.unset(25);
        outMsg.unset(53);
        
//        MyDateFormat dateFormatYYYYMMDDhhmmss = new MyDateFormat("yyyyMMddHHmmss");
        outMsg.set(41, outIfx.getTerminalId());
        outMsg.set(42, outIfx.getOrgIdNum());
        outMsg.set(7, MyDateFormatNew.format("yyyyMMddHHmmss", outIfx.getTrnDt().toDate()));
        outMsg.set(39, outIfx.getRsCode());
        
//        EncodingConvertor convertor = GlobalContext.getInstance().getConvertor(inputMessage.getChannel().getEncodingConvertor());
        EncodingConvertor convertor = ProcessContext.get().getConvertor(inputMessage.getChannel().getEncodingConverter());
        byte[] field48Rs = TerminalService.generalInfotechField48Rs(outIfx, convertor, terminal);
        ByteArrayOutputStream field48 = new ByteArrayOutputStream();

        field48.write(field48Rs);
        
        Integer emvTrnType = Integer.parseInt(outMsg.getString(3).substring(0, 2).trim());
        if (emvTrnType.equals(ISOTransactionCodes.LOG_ON)) {
//    		field48.write(field48Rs);
    		SecureKey tmk = terminal.getKeyByType(KeyType.TYPE_TAK);
    		processContext.setLastAcqMacKey(tmk);
    		TerminalService.removeKeySet(terminal);
    		field48.write(generateMacKey(terminal, tmk).getBytes());
    		field48.write(ASCIIConstants.FS);
    		field48.write(generatePinKey(terminal, tmk).getBytes());
    		field48.write(ASCIIConstants.FS);
    		
        } else if (emvTrnType.equals(ISOTransactionCodes.RESET_PASSWORD)) {
        	
        }
        
		outMsg.set(new ISOBinaryField(48, field48.toByteArray()));
        outMsg.set(64, "1111111111111111");
        outMsg.setMTI(Integer.valueOf(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87).toString());
        outgoingMessage.setProtocolMessage(outMsg);
        outgoingMessage.setNeedToBeSent(true);
        outgoingMessage.setNeedResponse(false);
        outgoingMessage.setNeedToBeInstantlyReversed(false);
        outgoingMessage.setRequest(false);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        return outgoingMessage;
    }

    private static String generateKey(Terminal terminal, SecureKey tmk, String keyType) {
		try {
			SecureDESKey newKey = SecurityComponent.generateKey(SMAdapter.LENGTH_DES, keyType);
			/*********
			JCESecurityModule ssm = new JCESecurityModule("config/LMK.jceks", "$3cureP@$$".toCharArray(), "org.bouncycastle.jce.provider.BouncyCastleProvider");
			Key key = new SecretKeySpec(Hex.decode("1C1C1C1C1C1C1C1C"), "DES");
			SecureDESKey newKey = ssm.encryptToLMK(SMAdapter.LENGTH_DES, keyType, key);
			*********/
			GeneralDao.Instance.saveOrUpdate(newKey);
			terminal.addSecureKey(newKey);
			logger.debug("New Key ("+keyType+"):"+newKey.getKeyBytes());
			byte[] encNewTak = SecurityComponent.exportKey(newKey, (SecureDESKey) tmk);
			String newKeyString = new String(Hex.encode(encNewTak));
			logger.debug("New Encrypted Key ("+keyType+"):"+newKeyString);

			return newKeyString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    private static String generateMacKey(Terminal terminal, SecureKey tmk) {
		return generateKey(terminal, tmk, KeyType.TYPE_TAK);
	}

    private static String generatePinKey(Terminal terminal, SecureKey tmk) {
		return generateKey(terminal, tmk, KeyType.TYPE_TPK);
	}

	private static Ifx createIncomingIfx(Message message){
    	Ifx ifx = new Ifx();
    	 ISOMsg isoMsg = (ISOMsg) message.getProtocolMessage();
    	 Integer emvTrnType = Integer.parseInt(isoMsg.getString(3).substring(0, 2).trim());

//		MyDateFormat dateFormatYYYYMMDDhhmmss = new MyDateFormat("yyyyMMddHHmmss");
        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();

        try {
			ifx.setOrigDt (new DateTime( MyDateFormatNew.parse("yyyyMMddHHmmss", localDate + localTime)));
		} catch (Exception e) {
			ISOException isoe = new ISOException("Unparsable Original Date.", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(  Severity.ERROR);
				ifx.setStatusDesc( (isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}

		
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11)));
		ifx.setMy_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
		ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());
		ifx.setBankId(isoMsg.getString(32));
		ifx.setTerminalId(ISOUtil.zeroUnPad(isoMsg.getString(41).trim()));
        ifx.setOrgIdNum(ISOUtil.zeroUnPad((isoMsg.getString(42).trim())));
		ifx.setIfxDirection(IfxDirection.INCOMING);
		ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		
		Integer terminalTypeCode = Integer.parseInt("0" + isoMsg.getString(25).trim());
		switch (terminalTypeCode) {
		 case (14):
	         ifx.setTerminalType ( TerminalType.POS);
	         break;
	     case (43):
	         ifx.setTerminalType ( TerminalType.KIOSK_CARD_PRESENT);
	         break;
	         
         default:
        	 ifx.setTerminalType(TerminalType.POS);
	        	 
		}
		
		ifx.setReceivedDt( message.getStartDateTime());
		
		 /***P48 INGENICO POS format: 
         * 6 byte last sequence counter,
         * application version,
         * Other data: 
         * -) return data : main transaction sequence counter
         * -) bill payment data: billID paymentID
         * -) reset password data: helpdesk password
         * ***/ 
        
		try {
			byte[] field48 = null;
			if (isoMsg.hasField(48)) {
				field48 = (byte[]) isoMsg.getValue(48);
			}
			MyInteger offset = new MyInteger(0);
			ifx.setLast_TrnSeqCntr(NDCParserUtils.readUntilFS(field48, offset));
			NDCParserUtils.readFS(field48, offset);

			ifx.setApplicationVersion(NDCParserUtils.readUntilFS(field48, offset));
			NDCParserUtils.readFS(field48, offset);

			if (emvTrnType.equals(ISOTransactionCodes.LOG_ON)){
				ifx.setIfxType(IfxType.LOG_ON_RQ);
				
			}else if (emvTrnType.equals(ISOTransactionCodes.RESET_PASSWORD)){
				ifx.setIfxType(IfxType.RESET_PASSWORD_RQ);
				ifx.setResetingPassword(NDCParserUtils.readUntilFS(field48, offset));
				NDCParserUtils.readFS(field48, offset);
				
			}
		} catch (Exception e) {
			logger.error("Exception in parsing field 48", e);
		}

		ifx.setSerialno(isoMsg.getString(53));
		ifx.setMsgAuthCode(isoMsg.getString(68));
    	return ifx;
    }
    
    private static Ifx creatOutgoingIfx(Message outMessage, Ifx ifx) throws CloneNotSupportedException {
		Ifx outIfx = ifx.clone();
		outIfx.setIfxType(IfxType.getResponseIfxType(ifx.getIfxType()));
		outIfx.setIfxDirection(IfxDirection.OUTGOING);
		outIfx.setReceivedDt(outMessage.getStartDateTime());
		outIfx.setTrnDt(DateTime.now());
		
		if (ifx.getIfxType().equals(IfxType.LOG_ON_RQ)) {
			outIfx.setRsCode(ISOResponseCodes.APPROVED);
		} else if (ifx.getIfxType().equals(IfxType.RESET_PASSWORD_RQ)) {
			if (outMessage.getEndPointTerminal().getTerminalType().equals(TerminalType.POS)) {
				POSTerminal terminal = (POSTerminal) outMessage.getEndPointTerminal();
				if(Util.hasText(terminal.getResetCode()) && ifx.getResetingPassword().equals(terminal.getResetCode())){
					outIfx.setRsCode(ISOResponseCodes.APPROVED);
				}else{
					outIfx.setRsCode(ISOResponseCodes.INVALID_CARD_STATUS);
				}
				terminal.setResetCode(null);
			}
		}else{
			outIfx.setRsCode(ISOResponseCodes.APPROVED);
		}
		return outIfx;
	}
}