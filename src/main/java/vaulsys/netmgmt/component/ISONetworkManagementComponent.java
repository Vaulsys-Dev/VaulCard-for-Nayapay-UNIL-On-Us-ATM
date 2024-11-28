package vaulsys.netmgmt.component;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.exception.base.DecisionMakerException;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.netmgmt.exceptions.InvalidNetworkMessageException;
import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.ifx.enums.*;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.hsm.HardwareSecurityModule;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

public class ISONetworkManagementComponent{

    private static transient Logger logger = Logger.getLogger(ISONetworkManagementComponent.class);
    private ISONetworkManagementComponent() {}

    public static NetworkManagementAction processISONetworkManagementMessage(ProcessContext processContext, Message inputMessage)
            throws InvalidNetworkMessageException, Exception {

        ISOMsg isoMsg = (ISOMsg) inputMessage.getProtocolMessage();

        NetworkManagementInfo netMgmtCode = new NetworkManagementInfo(Integer.parseInt(isoMsg.getString(70)));

        Terminal endpointTerminal = TerminalService.findEndpointTerminalForMessageWithoutIFX(inputMessage, null);
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
			return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
		}
		
        
        if (NetworkManagementInfo.SIGN_ON.equals(netMgmtCode)) {
            return NetworkManagementAction.NO_ACTION_MESSAGE_UNSUPPORTED;

        } else if (NetworkManagementInfo.SIGN_OFF.equals(netMgmtCode)) {
            return NetworkManagementAction.NO_ACTION_MESSAGE_UNSUPPORTED;

        } else if (NetworkManagementInfo.CUTOVER.equals(netMgmtCode)) {
            return NetworkManagementAction.NO_ACTION_MESSAGE_UNSUPPORTED;

        } else if (NetworkManagementInfo.ECHOTEST.equals(netMgmtCode)) {
            Message outMsg = generateEchoTestRs(inputMessage);
            processContext.getTransaction().addOutputMessage(outMsg);
            return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;

        } else if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)
                || NetworkManagementInfo.PIN_CHANGE.equals(netMgmtCode)) {

			Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
			Set<SecureKey> keySet = null;

//            SecurityProfile incomingSecProfile = null; 
			Terminal targetTerminal = null;

			if (KeyManagementMode.ISSUER_MAC.equals(inputMessage.getIfx().getMode())
					|| KeyManagementMode.ISSUER_PIN.equals(inputMessage.getIfx().getMode())) {
//            	targetTerminal = FinancialEntityService.getAcquireSwitchTerminal(targetInst);
				targetTerminal = ProcessContext.get().getAcquireSwitchTerminal(targetInst);
//                processContext.setVariable("forcedKeySet", keySet);
			} else if (KeyManagementMode.ACQUIER_MAC.equals(inputMessage.getIfx().getMode())
					|| KeyManagementMode.ACQUIER_PIN.equals(inputMessage.getIfx().getMode())) {
//                targetTerminal = FinancialEntityService.getIssuerSwitchTerminal(targetInst);
				targetTerminal = ProcessContext.get().getIssuerSwitchTerminal(targetInst);
			}

			keySet = targetTerminal.getKeySet();
//            incomingSecProfile = targetTerminal.getOwnOrParentSecurityProfile();

			String key = isoMsg.getString(96);
			byte[] keyBytes = Hex.decode(key);

			SecureDESKey importedKey = null;
			SecureDESKey lastKey = null;

			if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)) {
				importedKey = SecurityComponent.importKey(keySet, (short) (8 * keyBytes.length), SMAdapter.TYPE_TAK, keyBytes, false);
				lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySet);
			} else {
				importedKey = SecurityComponent.importKey(keySet, (short) (8 * keyBytes.length), SMAdapter.TYPE_TPK, keyBytes, false);
				lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
			}

			logger.info("Received new key: " + inputMessage.getIfx().getMode() + " : " + importedKey.getKeyBytes());

			if (inputMessage.getIfx().getMode().equals(KeyManagementMode.ISSUER_MAC)) {
				SecureDESKey lastAcqMac = new SecureDESKey();
//            	lastAcqMac.setBKeyBytes(lastKey.getBKeyBytes());
//            	lastAcqMac.setBKeyCheckValue(lastKey.getBKeyCheckValue());
				lastAcqMac.setKeyBytes(lastKey.getKeyBytes());
				lastAcqMac.setKeyCheckValue(lastKey.getKeyCheckValue());
				lastAcqMac.setKeyType(lastKey.getKeyType());
				lastAcqMac.setKeyLength(lastKey.getKeyLength());
				processContext.setLastAcqMacKey(lastAcqMac);
			}

			if (lastKey != null) {
//				lastKey.setBKeyBytes(importedKey.getBKeyBytes());
//				lastKey.setBKeyCheckValue(importedKey.getBKeyCheckValue());
				lastKey.setKeyBytes(importedKey.getKeyBytes());
				lastKey.setKeyCheckValue(importedKey.getKeyCheckValue());
				GeneralDao.Instance.saveOrUpdate(lastKey);
			} else {
				targetTerminal.addSecureKey(importedKey);
				GeneralDao.Instance.saveOrUpdate(importedKey);
			}

			Message outMsg = generateKeyChangeRs(inputMessage);
			processContext.getTransaction().addOutputMessage(outMsg);

			GeneralDao.Instance.saveOrUpdate(targetInst);
			GlobalContext.getInstance().setAllInstitutions();

			return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;
		}

        return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
    }

	//Raza TPSP Channel Add start
	public static NetworkManagementAction processPaymentSchemesISONetworkManagementMessage(ProcessContext processContext, Message inputMessage)
			throws InvalidNetworkMessageException, Exception {

		vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg isoMsg = (vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg) inputMessage.getProtocolMessage();
		NetworkManagementInfo netMgmtCode = new NetworkManagementInfo(Integer.parseInt(isoMsg.getString(70)));
		System.out.println("ISONetworkManagementComponent:: netMgmtCode [" + netMgmtCode.getType() + "]"); //Raza TEMP
		Terminal endpointTerminal = TerminalService.findEndpointTerminalForMessageWithoutIFX(inputMessage, null);
		inputMessage.setEndPointTerminal(endpointTerminal);
		inputMessage.setIfx(createPaymentSchemesIncomingIfx(inputMessage));

		//inputMessage.getTransaction().setDebugTag(inputMessage.getIfx().getIfxType().toString()); //Raza commenting

		LifeCycle lifeCycle = new LifeCycle();
		lifeCycle.setIsComplete(true);
		GeneralDao.Instance.saveOrUpdate(lifeCycle);
		inputMessage.getTransaction().setLifeCycle(lifeCycle);
		GeneralDao.Instance.saveOrUpdate(inputMessage.getIfx());
		GeneralDao.Instance.saveOrUpdate(inputMessage);
		GeneralDao.Instance.saveOrUpdate(inputMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(inputMessage.getTransaction());

		System.out.println("ISONetworkManagementComponent:: Not calling Authorize Message..!"); //Raza TEMP
		/*try { //Raza commenting TEMP
//			inputMessage.getTransaction().setAuthorized(
			authorizeMessage(inputMessage);
//					);
		} catch (AuthorizationException e) {
//			inputMessage.getTransaction().setAuthorized(false);
			if (e instanceof DecisionMakerException) {
				DecisionMakerException dec = ((DecisionMakerException) e);
				dec.showCause(inputMessage.getIfx());
			}
			return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
		}*/

		//m.rehman: for response message from network
		if (ISOFinalMessageType.isRequestMessage(inputMessage.getIfx().getIfxType())) {
			if (NetworkManagementInfo.SIGN_ON.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNON_ISO.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNON_ISO_071.equals(netMgmtCode)) {
				logger.info("SIGNON Message, generating response..."); //Raza TEMP
				Terminal targetTerminal = null;
				Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
				targetTerminal = ProcessContext.get().getAcquireSwitchTerminal(targetInst);

				Message outMsg = generateSIGNONRs(inputMessage);
				processContext.getTransaction().addOutputMessage(outMsg);
				GeneralDao.Instance.saveOrUpdate(inputMessage.getTransaction()); //Saving/Updating OutPut Message in DB for Processing Status.
				return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;

			} else if (NetworkManagementInfo.SIGN_OFF.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNOFF_ISO.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNOFF_ISO_072.equals(netMgmtCode)) {
				logger.info("SIGNOFF Message, generating response..."); //Raza TEMP
				Terminal targetTerminal = null;
				Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
				targetTerminal = ProcessContext.get().getAcquireSwitchTerminal(targetInst);

				Message outMsg = generateSIGNOFFRs(inputMessage);
				processContext.getTransaction().addOutputMessage(outMsg);

				return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;

			} else if (NetworkManagementInfo.CUTOVER.equals(netMgmtCode)
					|| NetworkManagementInfo.CUTOVER_1LINK.equals(netMgmtCode)) {	//m.rehman: adding check for 1link cutover
				logger.info("CUTOVER Message, generating response..."); //Raza TEMP
				Terminal targetTerminal = null;
				Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
				targetTerminal = ProcessContext.get().getAcquireSwitchTerminal(targetInst);

				Message outMsg = generateCUTOFFRs(inputMessage,netMgmtCode);
				processContext.getTransaction().addOutputMessage(outMsg);

				return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;
			} else if (NetworkManagementInfo.ECHOTEST_ISO.equals(netMgmtCode)
					|| NetworkManagementInfo.ECHOTEST.equals(netMgmtCode)
					|| NetworkManagementInfo.ECHOTEST_1LINK.equals(netMgmtCode)) {
				logger.info("Echo TEST Message, generating response..."); //Raza TEMP

				Terminal targetTerminal = null;
				Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
				targetTerminal = ProcessContext.get().getAcquireSwitchTerminal(targetInst);

				Message outMsg = generateEchoTestRs(inputMessage);

				// Asim Shahzad, Date : 10th Dec 2016, Desc : For VISA SMS, returning header in response message
				if (outMsg.getHeaderData() == null && outMsg.getChannel().getHeaderLen() > 0) {
					Channel channel = inputMessage.getChannel();
					ProtocolFunctions mapper = channel.getProtocol().getMapper();
					byte[] data = mapper.toBinary(outMsg.getProtocolMessage());

					outMsg.setHeaderData(inputMessage.getTransaction().getFirstTransaction().getInputMessage().getHeaderData());

					data = outMsg.setBinaryDataWithHeader(data);
					outMsg.setBinaryData(data);
				}
				//**************************************************************************************************

				processContext.getTransaction().addOutputMessage(outMsg);
				return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;

			} else if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)
					|| NetworkManagementInfo.PIN_CHANGE.equals(netMgmtCode)) {
				Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
				Set<SecureKey> keySet = null;

//            SecurityProfile incomingSecProfile = null;
				Terminal targetTerminal = null;
				if (KeyManagementMode.ISSUER_MAC.equals(inputMessage.getIfx().getMode())
						|| KeyManagementMode.ISSUER_PIN.equals(inputMessage.getIfx().getMode())) {
//            	targetTerminal = FinancialEntityService.getAcquireSwitchTerminal(targetInst);
					targetTerminal = ProcessContext.get().getAcquireSwitchTerminal(targetInst);
//                processContext.setVariable("forcedKeySet", keySet);
				} else if (KeyManagementMode.ACQUIER_MAC.equals(inputMessage.getIfx().getMode())
						|| KeyManagementMode.ACQUIER_PIN.equals(inputMessage.getIfx().getMode())) {
//                targetTerminal = FinancialEntityService.getIssuerSwitchTerminal(targetInst);
					targetTerminal = ProcessContext.get().getIssuerSwitchTerminal(targetInst);
				}
				keySet = targetTerminal.getKeySet();
//            incomingSecProfile = targetTerminal.getOwnOrParentSecurityProfile();

				String key = isoMsg.getString(96);
				byte[] keyBytes = Hex.decode(key);

				SecureDESKey importedKey = null;
				SecureDESKey lastKey = null;
				if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)) {
					importedKey = SecurityComponent.importKey(keySet, (short) (8 * keyBytes.length), SMAdapter.TYPE_TAK, keyBytes, false);
					lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySet);
				} else {
					importedKey = SecurityComponent.importKey(keySet, (short) (8 * keyBytes.length), SMAdapter.TYPE_TPK, keyBytes, false);
					lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
				}

				logger.info("Received new key: " + inputMessage.getIfx().getMode() + " : " + importedKey.getKeyBytes());

				if (inputMessage.getIfx().getMode().equals(KeyManagementMode.ISSUER_MAC)) {
					SecureDESKey lastAcqMac = new SecureDESKey();
//            	lastAcqMac.setBKeyBytes(lastKey.getBKeyBytes());
//            	lastAcqMac.setBKeyCheckValue(lastKey.getBKeyCheckValue());
					lastAcqMac.setKeyBytes(lastKey.getKeyBytes());
					lastAcqMac.setKeyCheckValue(lastKey.getKeyCheckValue());
					lastAcqMac.setKeyType(lastKey.getKeyType());
					lastAcqMac.setKeyLength(lastKey.getKeyLength());
					processContext.setLastAcqMacKey(lastAcqMac);
				}

				if (lastKey != null) {
//				lastKey.setBKeyBytes(importedKey.getBKeyBytes());
//				lastKey.setBKeyCheckValue(importedKey.getBKeyCheckValue());
					lastKey.setKeyBytes(importedKey.getKeyBytes());
					lastKey.setKeyCheckValue(importedKey.getKeyCheckValue());
					GeneralDao.Instance.saveOrUpdate(lastKey);
				} else {
					targetTerminal.addSecureKey(importedKey);
					GeneralDao.Instance.saveOrUpdate(importedKey);
				}

				Message outMsg = generateKeyChangeRs(inputMessage);
				processContext.getTransaction().addOutputMessage(outMsg);

				GeneralDao.Instance.saveOrUpdate(targetInst);
				GlobalContext.getInstance().setAllInstitutions();

				return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;
			} else if (NetworkManagementInfo.KEY_EXCHANGE.equals(netMgmtCode) || NetworkManagementInfo.KEYEXCHANGE_ISO.equals(netMgmtCode)) //Raza TPSP Channel Add
			{
				logger.info("KEY EXCHANGE Message Received...!");
				int retval = 0;
				//return NetworkManagementAction.NO_ACTION_MESSAGE_UNSUPPORTED;
				if (inputMessage.getChannel().getChannelId().equals(ChannelCodes.MASTERCARD)) {
					retval = ProcessMSKeyExchange(processContext, inputMessage, isoMsg, netMgmtCode);
				} else if (inputMessage.getChannel().getChannelId().equals(ChannelCodes.UNION_PAY)) {
					retval = ProcessCUPKeyExchange(processContext, inputMessage, isoMsg, netMgmtCode);
				} else if (inputMessage.getChannel().getChannelId().equals(ChannelCodes.VISA_BASE_I)) {
				} else if (inputMessage.getChannel().getChannelId().equals(ChannelCodes.VISA_SMS)) {
				}
//				else if(inputMessage.getChannel().getChannelId().equals(ChannelCodes.JCB))
//				{}
				else {
					logger.error("Channel Not Found for Key Exchange Msg");
					throw new InvalidNetworkMessageException();
				}

				if (retval > 0) {
					return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;
				} else {
					return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
				}
				//Raza end
			}

		} else if(ISOFinalMessageType.isResponseMessage(inputMessage.getIfx().getIfxType())) {
			return processNetworkResponseMessage(inputMessage);
			//processContext.getTransaction().addOutputMessage(outMsg);
			//return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
		}

		return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
	}
	//Raza TPSP Channel Add end

    private static Boolean authorizeMessage(Message message) throws Exception {

    	KeyManagementMode keyManagementMode = message.getIfx().getKeyManagement().getMode();
//    	Long ourInstCode = GlobalContext.getInstance().getMyInstitution().getCode();
    	Long ourInstCode = ProcessContext.get().getMyInstitution().getCode();
    	
    	if (KeyManagementMode.ISSUER_MAC.equals(keyManagementMode) || KeyManagementMode.ISSUER_PIN.equals(keyManagementMode)){
    		if (!message.getIfx().getDestBankId().equals(ourInstCode))
    			throw new AuthorizationException(message.getIfx().getIfxType()+ " is sent to "+ message.getIfx().getDestBankId() +" but our code is "+ ourInstCode);
    		
    	}else if (KeyManagementMode.ACQUIER_MAC.equals(keyManagementMode) || KeyManagementMode.ACQUIER_PIN.equals(keyManagementMode)){
    		if (!message.getIfx().getBankId().equals(ourInstCode))
    			throw new AuthorizationException(message.getIfx().getIfxType()+ " is sent to "+ message.getIfx().getBankId()+" but our code is "+ ourInstCode);
    	}
    	
    	if (message.getChannel().getMacEnable()) {
			try {
				String mac = ((ISOMsg)message.getProtocolMessage()).getString(128);
				if (mac == null) {
					logger.error("Failed: Mac Verification failed! (mac =null)");
					
					//TODO check more carefully later!
					throw new MacFailException("Failed:Mac verification failed.(mac = null)");
//					throw new AuthorizationException("Failed:Mac verification failed.(mac = null)");
				}

				ProtocolSecurityFunctions securityFunctions = message.getChannel().getProtocol().getSecurityFunctions();
//	            Institution targetInst = getFinancialEntityService().findEntity(Institution.class, message.getChannel().getInstitution());
//	            Terminal terminal = getFinancialEntityService().getAcquireSwitchTerminal(targetInst);
				Terminal terminal = message.getEndPointTerminal();
				securityFunctions.verifyMac(terminal, terminal.getOwnOrParentSecurityProfileId(), terminal.getKeySet(), mac, message.getBinaryData(), message.getChannel().getMacEnable());

			} catch (Exception e) {
				throw e;
			}
		}
    	
		return true;
	}

	private static Message generateKeyChangeRs(Message inputMessage) throws ISOException {
    	Message outgoingMessage = new Message(MessageType.OUTGOING);
        outgoingMessage.setTransaction(inputMessage.getTransaction());

        if (CommunicationMethod.ANOTHER_SOCKET.equals(inputMessage.getChannel().getCommunicationMethod()))
            outgoingMessage.setChannel(((InputChannel) inputMessage.getChannel()).getOriginatorChannel());
        else
            outgoingMessage.setChannel(inputMessage.getChannel());

        outgoingMessage.setEndPointTerminal(inputMessage.getEndPointTerminal());

        ISOMsg outMsg = (ISOMsg) inputMessage.getProtocolMessage();
        outMsg.unset(53);
        outMsg.set(39, ISOResponseCodes.APPROVED);
        outMsg.set(128, "1111111111111111");
        outMsg.setMTI(Integer.valueOf(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87).toString());
        outgoingMessage.setProtocolMessage(outMsg);
        outgoingMessage.setNeedToBeSent(true);
        outgoingMessage.setNeedResponse(false);
        outgoingMessage.setNeedToBeInstantlyReversed(false);
        outgoingMessage.setRequest(false);
        try {
			outgoingMessage.setIfx(creatOutgoingIfx(outgoingMessage, inputMessage.getIfx()));
			GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
		} catch (CloneNotSupportedException e) {
			logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
//			e.printStackTrace();
		}
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        return outgoingMessage;
    }


    private static Message generateEchoTestRs(Message inputMessage) {
    	Message outgoingMessage = new Message(MessageType.OUTGOING);
        outgoingMessage.setTransaction(inputMessage.getTransaction());
        outgoingMessage.setChannel(inputMessage.getChannel());
        outgoingMessage.setEndPointTerminal(inputMessage.getEndPointTerminal());
        outgoingMessage.setProtocolMessage(inputMessage.getProtocolMessage());
		vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg outIsoMsg = (vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg)outgoingMessage.getProtocolMessage();

		if(outIsoMsg.getMessageStatus() == vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg.INVALID)
		{
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			//NetworkManager.SetEchoCount(outgoingMessage.getChannel()); //Set Echo Count
			if(outgoingMessage.getChannel().getEchoCount() != null) {
				outgoingMessage.getChannel().setEchoCount(outgoingMessage.getChannel().getEchoCount() + 1L);
			}
			else
			{
				outgoingMessage.getChannel().setEchoCount(1L);
			}
			saveChannel(outgoingMessage);

			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.APPROVED);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		try {
			Integer mti = new Integer(outIsoMsg.getMTI());
			logger.info("Message MTI Get[" + mti + "]"); //Raza TEMP
			mti += 10;
			outIsoMsg.setMTI("0" + mti.toString());
			logger.info("Message MTI Set [" + outIsoMsg.getMTI() + "]"); //Raza TEMP
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		/*if (outIsoMsg.getMessageStatus() == vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg.INVALID) {
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.APPROVED);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/

		outgoingMessage.setProtocolMessage((vaulsys.protocols.base.ProtocolMessage)outIsoMsg);
        try {
			outgoingMessage.setIfx(creatOutgoingIfx(outgoingMessage, inputMessage.getIfx()));
			GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
		} catch (CloneNotSupportedException e) {
			logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
//			e.printStackTrace();
		}
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        return outgoingMessage;
    }
	private static Message generateSIGNONRs(Message inputMessage) {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(inputMessage.getTransaction());
		outgoingMessage.setChannel(inputMessage.getChannel());
		outgoingMessage.setEndPointTerminal(inputMessage.getEndPointTerminal());
		outgoingMessage.setProtocolMessage(inputMessage.getProtocolMessage());
		vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg outIsoMsg = (vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg)outgoingMessage.getProtocolMessage();

		if(outIsoMsg.getMessageStatus() == vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg.INVALID)
		{
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			//NetworkManager.SetProcEnabled(outgoingMessage.getChannel()); //Set Processing Enabled for SignON
			outgoingMessage.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED);
			saveChannel(outgoingMessage);

			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.APPROVED);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		try {
			Integer mti = new Integer(outIsoMsg.getMTI());
			logger.info("Message MTI Get[" + mti + "]"); //Raza TEMP
			mti += 10;
			outIsoMsg.setMTI("0" + mti.toString());
			logger.info("Message MTI Set [" + outIsoMsg.getMTI() + "]"); //Raza TEMP
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		outgoingMessage.setProtocolMessage((vaulsys.protocols.base.ProtocolMessage)outIsoMsg);

		try {
			outgoingMessage.setIfx(creatOutgoingIfx(outgoingMessage, inputMessage.getIfx()));
			GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
		} catch (CloneNotSupportedException e) {
			logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		}

		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		return outgoingMessage;
	}

	private static Message generateSIGNOFFRs(Message inputMessage) {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(inputMessage.getTransaction());
		outgoingMessage.setChannel(inputMessage.getChannel());
		outgoingMessage.setEndPointTerminal(inputMessage.getEndPointTerminal());
		outgoingMessage.setProtocolMessage(inputMessage.getProtocolMessage());
		vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg outIsoMsg = (vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg)outgoingMessage.getProtocolMessage();

		if(outIsoMsg.getMessageStatus() == vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg.INVALID)
		{
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			//NetworkManager.SetProcDisabled(outgoingMessage.getChannel()); //Set Processing Disabled
			outgoingMessage.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_DISABLED);
			saveChannel(outgoingMessage);
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.APPROVED);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}



		try {
			Integer mti = new Integer(outIsoMsg.getMTI());
			mti += 10;
			outIsoMsg.setMTI("0" + mti.toString());
			logger.info("Message MTI Set [" + outIsoMsg.getMTI() + "]"); //Raza TEMP
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		outgoingMessage.setProtocolMessage((vaulsys.protocols.base.ProtocolMessage)outIsoMsg);

		try {
			outgoingMessage.setIfx(creatOutgoingIfx(outgoingMessage, inputMessage.getIfx()));
			GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
		} catch (CloneNotSupportedException e) {
			logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		}
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		return outgoingMessage;
	}


	private static Message generateCUTOFFRs(Message inputMessage,NetworkManagementInfo netinfo) {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(inputMessage.getTransaction());
		outgoingMessage.setChannel(inputMessage.getChannel());
		outgoingMessage.setEndPointTerminal(inputMessage.getEndPointTerminal());
		outgoingMessage.setProtocolMessage(inputMessage.getProtocolMessage());
		int iRetVal = 0;
		vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg outIsoMsg;
		outIsoMsg = (vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg)inputMessage.getProtocolMessage(); //.getprotocolMessage.getString(33))


		if (outIsoMsg.getMessageStatus() == vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg.INVALID) {
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String CutOffDate = outIsoMsg.getString(15);
			logger.info("CutOffDate Received [" + CutOffDate + "]");
			//m.rehman: settlement date should be YYYYMMDD format
			if (Util.hasText(CutOffDate)) {
				try {
					CutOffDate = Long.toString(new DateTime(MyDateFormatNew.parse("MMdd", CutOffDate)).getDateTimeLong()).substring(0,8);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			//iRetVal = NetworkManager.SetCutOffDate(outgoingMessage.getChannel(), CutOffDate);
			if(NetworkManagementInfo.CUTOVER.equals(netinfo)
					|| NetworkManagementInfo.CUTOVER_1LINK.equals(netinfo)) {	//m.rehman: adding check for 1link cutover

				try {
					Long daysDiff;
					Date prevDate, currDate;
					if (Util.hasText(inputMessage.getChannel().getSettlementDate())) {
						if (Util.hasText(inputMessage.getChannel().getLastSettlementDate())) {
							prevDate = new SimpleDateFormat("yyyyMMdd").parse(inputMessage.getChannel().getLastSettlementDate());
							currDate = new SimpleDateFormat("yyyyMMdd").parse(inputMessage.getChannel().getSettlementDate());
							daysDiff = ChronoUnit.DAYS.between(prevDate.toInstant(), currDate.toInstant());
						} else {
							daysDiff = 1L;
						}

						inputMessage.getChannel().setLastSettlementDate(inputMessage.getChannel().getSettlementDate());

					} else {
						daysDiff = 1L;
						inputMessage.getChannel().setLastSettlementDate(CutOffDate);
					}

					inputMessage.getChannel().setSettlementDate(CutOffDate);
					inputMessage.getChannel().setLastSettlementDateDiff(daysDiff);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if(NetworkManagementInfo.CUTOVER_202.equals(netinfo))
			{
				inputMessage.getChannel().setSettlementEndDate(CutOffDate);
			}

			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.APPROVED);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		/*try {
			switch (iRetVal) {
				case 0: //Invalid Cutoff Date
					outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.CASH_RETRACT);
					break;
				case 1: //Valid
					outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.APPROVED);
					break;
				case -1: //Processing Disabled
					outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.ISSUER_REVERSAL);
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
		}
		try {
			Integer mti = new Integer(outIsoMsg.getMTI());
			mti += 10;
			outIsoMsg.setMTI("0" + mti.toString());
			logger.info("Message MTI Set [" + outIsoMsg.getMTI() + "]"); //Raza TEMP
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		outgoingMessage.setProtocolMessage((vaulsys.protocols.base.ProtocolMessage)outIsoMsg);

		try {
			outgoingMessage.setIfx(creatOutgoingIfx(outgoingMessage, inputMessage.getIfx()));
			GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
		} catch (CloneNotSupportedException e) {
			logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		}
		//m.rehman: saving channel in order to update settlement date in db
		saveChannel(outgoingMessage);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        return outgoingMessage;
    }

	private static Message generateKeyExchangeRs(Message inputMessage) {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(inputMessage.getTransaction());
		outgoingMessage.setChannel(inputMessage.getChannel());
		outgoingMessage.setEndPointTerminal(inputMessage.getEndPointTerminal());
		outgoingMessage.setProtocolMessage(inputMessage.getProtocolMessage());
		vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg outIsoMsg = (vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg)outgoingMessage.getProtocolMessage();

		if(outIsoMsg.getMessageStatus() == vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg.INVALID)
		{
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				outIsoMsg.set(39, vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.APPROVED);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			Integer mti = new Integer(outIsoMsg.getMTI());
			logger.info("Message MTI Get[" + mti + "]"); //Raza TEMP
			mti += 10;
			outIsoMsg.setMTI("0" + mti.toString());
			logger.info("Message MTI Set [" + outIsoMsg.getMTI() + "]"); //Raza TEMP
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		outgoingMessage.setProtocolMessage((vaulsys.protocols.base.ProtocolMessage)outIsoMsg);

		try {
			outgoingMessage.setIfx(creatOutgoingIfx(outgoingMessage, inputMessage.getIfx()));
			GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
		} catch (CloneNotSupportedException e) {
			logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		}

		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
		//GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		return outgoingMessage;
	}

    private static Ifx createIncomingIfx(Message message){
    	Ifx ifx = new Ifx();
    	 ISOMsg isoMsg = (ISOMsg) message.getProtocolMessage();
    	 NetworkManagementInfo netMgmtCode = new NetworkManagementInfo(Integer.parseInt(isoMsg.getString(70)));

		if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)){
			ifx.setIfxType(IfxType.MAC_KEY_CHANGE_RQ);
			ifx.setKeyType(SMAdapter.TYPE_TAK);
			
		}else if (NetworkManagementInfo.PIN_CHANGE.equals(netMgmtCode)){
			ifx.setIfxType(IfxType.PIN_KEY_CHANGE_RQ);
			ifx.setKeyType(SMAdapter.TYPE_TPK);
			
		} if (NetworkManagementInfo.SIGN_ON.equals(netMgmtCode)) {
			ifx.setIfxType(IfxType.SIGN_ON_RQ);
        } else if (NetworkManagementInfo.SIGN_OFF.equals(netMgmtCode)) {
        	ifx.setIfxType(IfxType.SIGN_OFF_RQ);
        } else if (NetworkManagementInfo.ECHOTEST.equals(netMgmtCode)) {
        	ifx.setIfxType(IfxType.ECHO_RQ);
        }
		
		if (IfxType.MAC_KEY_CHANGE_RQ.equals(ifx.getIfxType())
			|| IfxType.PIN_KEY_CHANGE_RQ.equals(ifx.getIfxType())){
			
			ifx.getKeyManagement().setKey(isoMsg.getString(96));
			ISOBinaryField bin48 = (ISOBinaryField) isoMsg.getComponent(48);
			String f48 = new String (bin48.getBytes());
			int mode = Integer.parseInt(f48.substring(0, 1));
			ifx.setMode(KeyManagementMode.getMode(mode));
			ifx.setCheckDigit(isoMsg.getString(53).substring(1,5));
			ifx.setDigits(f48.substring(1));
		}
		
		try {
//			MyDateFormat MMdd = new MyDateFormat("MMdd");
			MonthDayDate stlDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", isoMsg.getString(15)));
			ifx.setSettleDt(stlDate);
			ifx.setTrnDt ( new DateTime( MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));
		} catch (ParseException e) {
			logger.error("Creating ISO_NetMsg Ifx( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
//			new Exception("Creating ISO_NetMsg Ifx: ", e).printStackTrace();
		}
		
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11)));
		ifx.setMy_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
		ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());
		ifx.setFwdBankId(isoMsg.getString(33));
		ifx.setDestBankId(isoMsg.getString(33));
		ifx.setBankId(isoMsg.getString(32));
		ifx.setIfxDirection(IfxDirection.INCOMING);
		ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		ifx.setTerminalType(TerminalType.SWITCH);
		ifx.setOrigDt(DateTime.now());
		ifx.setTerminalId(message.getEndPointTerminal().getCode()+"");
		ifx.setReceivedDt( message.getStartDateTime());
		ifx.setMsgAuthCode(isoMsg.getString(128));
    	return ifx;
    }
    
    private static Ifx creatOutgoingIfx(Message outMessage, Ifx ifx) throws CloneNotSupportedException {
		Ifx outIfx = ifx.clone();
		outIfx.setIfxType(IfxType.getResponseIfxType(ifx.getIfxType()));
		outIfx.setRsCode(ISOResponseCodes.APPROVED);
		outIfx.setIfxDirection(IfxDirection.OUTGOING);
		outIfx.setReceivedDt(outMessage.getStartDateTime());
		return outIfx;
	}

	private static Ifx createPaymentSchemesIncomingIfx(Message message) throws ISOException { //Raza TPSP Channel Add
		Ifx ifx = new Ifx();
		vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg isoMsg = (vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg) message.getProtocolMessage();
		NetworkManagementInfo netMgmtCode = new NetworkManagementInfo(Integer.parseInt(isoMsg.getString(70)));
		String mti = isoMsg.getString(2);

		//m.rehman: need to take care of network response message in case of request message coming from UI
		if (isoMsg.isNetworkRequest().equals(Boolean.TRUE)) {
			if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.MAC_KEY_CHANGE_RQ);
				ifx.setKeyType(SMAdapter.TYPE_TAK);

			} else if (NetworkManagementInfo.PIN_CHANGE.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.PIN_KEY_CHANGE_RQ);
				ifx.setKeyType(SMAdapter.TYPE_TPK);

			}
			if (NetworkManagementInfo.SIGNON_ISO.equals(netMgmtCode) || NetworkManagementInfo.SIGN_ON.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNON_ISO_071.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.SIGN_ON_RQ);
			} else if (NetworkManagementInfo.SIGNOFF_ISO.equals(netMgmtCode) || NetworkManagementInfo.SIGN_OFF.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNOFF_ISO_072.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.SIGN_OFF_RQ);
			} else if (NetworkManagementInfo.ECHOTEST.equals(netMgmtCode) || NetworkManagementInfo.ECHOTEST_ISO.equals(netMgmtCode) || NetworkManagementInfo.ECHOTEST_1LINK.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.ECHO_RQ);
			} else if (NetworkManagementInfo.CUTOVER.equals(netMgmtCode) || NetworkManagementInfo.CUTOVER_1LINK.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.CUTOVER_RQ);
			} else if (NetworkManagementInfo.KEY_EXCHANGE.equals(netMgmtCode) || NetworkManagementInfo.KEYEXCHANGE_ISO.equals(netMgmtCode) || NetworkManagementInfo.KEYEXCHANGE_INIT.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.PIN_KEY_CHANGE_RQ);
			} else {
				System.out.println("NetManagementCode not recognised [" + netMgmtCode + "]"); //Raza TEMP
			}

			if (IfxType.MAC_KEY_CHANGE_RQ.equals(ifx.getIfxType())
					|| IfxType.PIN_KEY_CHANGE_RQ.equals(ifx.getIfxType())) {

				try {

					if (message.getChannel().getChannelId().equals(ChannelCodes.MASTERCARD)) {
						ifx = GetMasterCardKeyExchangeIfx(isoMsg, ifx);
					} else if (message.getChannel().getChannelId().equals(ChannelCodes.UNION_PAY)) {
						ifx = GetCupKeyExchangeIfx(isoMsg, ifx);
					}

				} catch (Exception e) {
					//TODO: handle Exception here
					logger.error("Error creating ifx for PaymentScheme KeyExchange");
					e.printStackTrace();
				}
			}
		} else {
			if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.MAC_KEY_CHANGE_RS);
				ifx.setKeyType(SMAdapter.TYPE_TAK);

			} else if (NetworkManagementInfo.PIN_CHANGE.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.PIN_KEY_CHANGE_RS);
				ifx.setKeyType(SMAdapter.TYPE_TPK);

			} else if (NetworkManagementInfo.SIGNON_ISO.equals(netMgmtCode) || NetworkManagementInfo.SIGN_ON.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNON_ISO_071.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.SIGN_ON_RS);

			} else if (NetworkManagementInfo.SIGNOFF_ISO.equals(netMgmtCode) || NetworkManagementInfo.SIGN_OFF.equals(netMgmtCode) ||
					NetworkManagementInfo.SIGNOFF_ISO_072.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.SIGN_OFF_RS);

			} else if (NetworkManagementInfo.ECHOTEST.equals(netMgmtCode) || NetworkManagementInfo.ECHOTEST_ISO.equals(netMgmtCode) || NetworkManagementInfo.ECHOTEST_1LINK.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.ECHO_RS);

			} else if (NetworkManagementInfo.CUTOVER.equals(netMgmtCode) || NetworkManagementInfo.CUTOVER_1LINK.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.CUTOVER_RS);

			} else if (NetworkManagementInfo.KEY_EXCHANGE.equals(netMgmtCode) || NetworkManagementInfo.KEYEXCHANGE_ISO.equals(netMgmtCode) || NetworkManagementInfo.KEYEXCHANGE_INIT.equals(netMgmtCode)) {
				ifx.setIfxType(IfxType.PIN_KEY_CHANGE_RS);

			} else {
				System.out.println("NetManagementCode not recognised [" + netMgmtCode + "]"); //Raza TEMP
			}
		}

		try {
			MonthDayDate stlDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", isoMsg.getString(15)));
			ifx.setSettleDt(stlDate);
			ifx.setTrnDt ( new DateTime( MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));
		} catch (ParseException e) {
			logger.error("Creating ISO_NetMsg Ifx( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		}
		System.out.println("ISONetworkManagementComponent:: Setting Field-2 in ISO Message...!"); //Raza TEMP
		if(Util.hasText(isoMsg.getString(2))) //Raza MasterCard
		{
			System.out.println("ISONetworkManagementComponent:: Field-2 in ISO Message [" + isoMsg.getString(2) + "]"); //Raza TEMP
			ifx.setAppPAN(isoMsg.getString(2));
		}

		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11)));
		ifx.setMy_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
		ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());
		ifx.setFwdBankId(isoMsg.getString(33));
		ifx.setDestBankId(isoMsg.getString(33));
		ifx.setBankId(isoMsg.getString(32));

		System.out.println("ISONetworkManagementComponent:: Setting Field-63 in ISO Message...!"); //Raza TEMP
		if(Util.hasText(isoMsg.getString(63))) { //Raza MasterCard
			System.out.println("ISONetworkManagementComponent:: Field-63 in ISO Message [" + isoMsg.getString(63) + "]"); //Raza TEMP
			ifx.setNetworkData(isoMsg.getString(63));
		}

		ifx.setIfxDirection(IfxDirection.INCOMING);
		ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		ifx.setTerminalType(TerminalType.SWITCH);
		ifx.setOrigDt(DateTime.now());
		//ifx.setTerminalId(message.getEndPointTerminal().getCode()+""); //Raza commenting TEMP
		ifx.setReceivedDt( message.getStartDateTime());
		ifx.setMsgAuthCode(isoMsg.getString(128));
		//m.rehman: set response code for response message
		if (Util.hasText(isoMsg.getString(39)))
			ifx.setRsCode(isoMsg.getString(39));
		return ifx;
	}

	private static Ifx GetMasterCardKeyExchangeIfx(ISOMsg isoMsg, Ifx ifx)
	{
		String f48 = isoMsg.getString(48);
		try {
			//Raza MasterCard KeyExchange start
			if (f48 != null) {
				String TagName, TagLength, TagValue, MsgType;
				System.out.println("Field 48 [" + f48 + "]"); //Raza TEMP
				MsgType = isoMsg.getMTI();
				TagName = f48.substring(0, 2);
				TagLength = f48.substring(2, 4);
				//TagValue = f48.substring(4, Integer.parseInt(TagLength));
				TagValue = f48.substring(4, (4 + Integer.parseInt(TagLength)) - 1);
				logger.info("Tag [" + TagName + "] Length [" + TagLength + "] Value [" + TagValue + "]");

				if (TagName.equals("11") && ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87.equals(MsgType)) {
					if (TagLength.equals("54") || TagLength.equals("70")) //Generate ZPK
					{
						ifx.setMode(KeyManagementMode.getMode(4)); //AUTHMODE for both Acquirer and Issuer
						//set Generate AWK flag
						//set Generate IWK flag

						int keylen = Integer.parseInt(TagLength) - 22;
						String AWK_INDEX_NAME = TagValue.substring(2, 4);
						System.out.println("AWK_INDEX_NAME [" + AWK_INDEX_NAME + "]");//Raza TEMP
						String IWK_INDEX_NAME = TagValue.substring(2, 4);
						System.out.println("IWK_INDEX_NAME [" + IWK_INDEX_NAME + "]");//Raza TEMP
						String AWK_ZMK = TagValue.substring(6, (6 + keylen) - 1);
						System.out.println("AWK under ZMK [" + AWK_ZMK + "]");//Raza TEMP
						String IWK_ZMK = TagValue.substring(6, (6 + keylen) - 1);
						System.out.println("IWK under ZMK [" + IWK_ZMK + "]");//Raza TEMP
						String AWK_CHECKSUM = TagValue.substring((6 + keylen), (6 + keylen) + 4 - 1);
						System.out.println("AWK_CHECKSUM [" + AWK_CHECKSUM + "]");//Raza TEMP
						String IWK_CHECKSUM = TagValue.substring((6 + keylen), (6 + keylen) + 4 - 1);
						System.out.println("IWK_CHECKSUM [" + IWK_CHECKSUM + "]");//Raza TEMP

						ifx.setAddDataPrivate(AWK_ZMK); //IWK is same as AWK

						//ifx.setCheckDigit(isoMsg.getString(53).substring(1, 5));
						//ifx.setDigits(f48.substring(1));
					} else {
						logger.info("Invalid Tag Length [" + TagLength + "]");
					}

				} else if (TagName.equals("11") && (ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87.equals(MsgType) || ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_REPEAT_87.equals(MsgType))) //Load ZPK
				{
					ifx.setMode(KeyManagementMode.getMode(4)); //AUTHMODE for both Acquirer and Issuer
					//set AUTHMODE
					//set LOAD_PASSIVE_ZPK_FLAG


				} else {
					logger.info("Invalid Tag [" + TagName + "] with MsgType [" + MsgType + "]");
				}
			} else {
				logger.error("Unable to get field 48.. rejecting txn");
			}
		}
		catch(Exception e)
		{
			logger.error("Error Creating IFX for MS Key Exchange");
			e.printStackTrace();
		}
		return ifx;
		//Raza MasterCard KeyExchange end
	}

	private static Ifx GetCupKeyExchangeIfx(ISOMsg isoMsg, Ifx ifx)
	{
		String f48 = isoMsg.getString(48);
		try {
			if (f48 != null) {
				String TagName, TagLength, TagValue, MsgType;
				System.out.println("Field 48 [" + f48 + "]"); //Raza TEMP
				MsgType = isoMsg.getMTI();
				TagName = f48.substring(0, 4);

				logger.info("Tag [" + TagName + "]");

				if (TagName.equals("4E4B")) { //compare HEX representation of "NK" directly, no need for conversion
					ifx.setAddDataPrivate(f48.substring(5,f48.length()-1));
				} else {
					logger.error("Invalid Tag [" + TagName + "] with MsgType [" + MsgType + "]");
					throw new InvalidNetworkMessageException();
				}
			} else {
				logger.error("Unable to get field 48.. rejecting txn");
				throw new InvalidNetworkMessageException();
			}
		}
		catch(Exception e)
		{
			logger.error("Error Creating IFX for UnionPay Key Exchange");
			e.printStackTrace();
		}
		return ifx;
	}

	private static void GetVisaKeyExchangeIfx()
	{

	}

	private static void GetJcbKeyExchangeIfx()
	{

	}

	private static int ProcessMSKeyExchange(ProcessContext processContext, Message inputMessage, ISOMsg isoMsg, NetworkManagementInfo netMgmtCode)
	{
		try {
			Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
			Set<SecureKey> keySetAcq = null, keySetIss = null;
			String MsgType = isoMsg.getMTI();

//            SecurityProfile incomingSecProfile = null;
			Terminal targetTerminalAcq = null, targetTerminalIss = null;
			if (KeyManagementMode.ACQ_ISS_PIN.equals(inputMessage.getIfx().getMode())) {
				//Raza update key for both Acquirer and Issuing Terminals as there is single connection for Acquiring and Issuing
				if (ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87.equals(MsgType)) //Net Request
				{
					targetTerminalAcq = ProcessContext.get().getAcquireSwitchTerminal(targetInst);
					targetTerminalIss = ProcessContext.get().getIssuerSwitchTerminal(targetInst);

					if (targetTerminalAcq != null) {
						keySetAcq = targetTerminalAcq.getKeySet();
						SwitchTerminal st = (SwitchTerminal) targetTerminalAcq;
					} else {
						logger.info("Acquirer terminal not Found..");
					}

					if (targetTerminalIss != null) {
						keySetIss = targetTerminalIss.getKeySet();
					} else {
						logger.info("Issuer terminal not Found..");
					}

					SecureDESKey newKey = null, tempkey = null;

					logger.info("Received new key: [" + inputMessage.getIfx().getMode() + "]");
					newKey = HardwareSecurityModule.getInstance().TranslateKey(keySetIss, inputMessage.getIfx().getAddDataPrivate(), KeyType.TYPE_ZPK, inputMessage.getIfx());

					if (newKey != null) {
						GeneralDao.Instance.saveOrUpdate(newKey);
					} else {
						logger.info("Key not Received.. KeyExchange Falied! Response Initiator");
					}
				} else if (ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87.equals(MsgType)) //Net Advice
				{
					SecureDESKey newKey = null, oldKey = null;
					targetTerminalIss = ProcessContext.get().getIssuerSwitchTerminal(targetInst);
					keySetIss = targetTerminalIss.getKeySet();
					newKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK_PAS, keySetIss);
					oldKey = SecureDESKey.getKeyByType(KeyType.TYPE_ZPK, keySetIss);

					newKey.setIsActive("1");
					newKey.setKeyType(KeyType.TYPE_ZPK);
					oldKey.setIsActive("0");
					oldKey.setKeyType(KeyType.TYPE_ZPK_PAS);

					GeneralDao.Instance.saveOrUpdate(newKey);
					GeneralDao.Instance.saveOrUpdate(oldKey);
				} else {
					logger.error("Invalid Msg Type for KeyExchange Msg");
				}
			} else if (KeyManagementMode.ISSUER_PIN.equals(inputMessage.getIfx().getMode())) {

				targetTerminalIss = ProcessContext.get().getIssuerSwitchTerminal(targetInst);
				keySetIss = targetTerminalIss.getKeySet();

				String key = isoMsg.getString(96);
				byte[] keyBytes = Hex.decode(key);

				SecureDESKey importedKey = null;
				SecureDESKey lastKey = null;
				if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)) {
					importedKey = SecurityComponent.importKey(keySetIss, (short) (8 * keyBytes.length), SMAdapter.TYPE_TAK, keyBytes, false);
					lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySetIss);
				} else {
					importedKey = SecurityComponent.importKey(keySetIss, (short) (8 * keyBytes.length), SMAdapter.TYPE_TPK, keyBytes, false);
					lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySetIss);
				}

				logger.info("Received new key: " + inputMessage.getIfx().getMode() + " : " + importedKey.getKeyBytes());

				if (inputMessage.getIfx().getMode().equals(KeyManagementMode.ISSUER_MAC)) {
					SecureDESKey lastAcqMac = new SecureDESKey();

					lastAcqMac.setKeyBytes(lastKey.getKeyBytes());
					lastAcqMac.setKeyCheckValue(lastKey.getKeyCheckValue());
					lastAcqMac.setKeyType(lastKey.getKeyType());
					lastAcqMac.setKeyLength(lastKey.getKeyLength());
					processContext.setLastAcqMacKey(lastAcqMac);
				}

				if (lastKey != null) {

					lastKey.setKeyBytes(importedKey.getKeyBytes());
					lastKey.setKeyCheckValue(importedKey.getKeyCheckValue());
					GeneralDao.Instance.saveOrUpdate(lastKey);
				} else {
					targetTerminalIss.addSecureKey(importedKey);
					GeneralDao.Instance.saveOrUpdate(importedKey);
				}

			} else if (KeyManagementMode.ACQUIER_PIN.equals(inputMessage.getIfx().getMode())) {
//                targetTerminal = FinancialEntityService.getIssuerSwitchTerminal(targetInst);
				targetTerminalAcq = ProcessContext.get().getAcquireSwitchTerminal(targetInst);
				keySetAcq = targetTerminalIss.getKeySet();

				String key = isoMsg.getString(96);
				byte[] keyBytes = Hex.decode(key);

				SecureDESKey importedKey = null;
				SecureDESKey lastKey = null;
				if (NetworkManagementInfo.MAC_CHANGE.equals(netMgmtCode)) {
					importedKey = SecurityComponent.importKey(keySetAcq, (short) (8 * keyBytes.length), SMAdapter.TYPE_TAK, keyBytes, false);
					lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySetAcq);
				} else {
					importedKey = SecurityComponent.importKey(keySetAcq, (short) (8 * keyBytes.length), SMAdapter.TYPE_TPK, keyBytes, false);
					lastKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySetAcq);
				}

				logger.info("Received new key: " + inputMessage.getIfx().getMode() + " : " + importedKey.getKeyBytes());

				if (inputMessage.getIfx().getMode().equals(KeyManagementMode.ISSUER_MAC)) {
					SecureDESKey lastAcqMac = new SecureDESKey();

					lastAcqMac.setKeyBytes(lastKey.getKeyBytes());
					lastAcqMac.setKeyCheckValue(lastKey.getKeyCheckValue());
					lastAcqMac.setKeyType(lastKey.getKeyType());
					lastAcqMac.setKeyLength(lastKey.getKeyLength());
					processContext.setLastAcqMacKey(lastAcqMac);
				}

				if (lastKey != null) {
//				lastKey.setBKeyBytes(importedKey.getBKeyBytes());
//				lastKey.setBKeyCheckValue(importedKey.getBKeyCheckValue());
					lastKey.setKeyBytes(importedKey.getKeyBytes());
					lastKey.setKeyCheckValue(importedKey.getKeyCheckValue());
					GeneralDao.Instance.saveOrUpdate(lastKey);
				} else {
					targetTerminalIss.addSecureKey(importedKey);
					GeneralDao.Instance.saveOrUpdate(importedKey);
				}
			}

			Message outMsg = generateKeyExchangeRs(inputMessage);
			processContext.getTransaction().addOutputMessage(outMsg);

			GeneralDao.Instance.saveOrUpdate(targetInst);
			GlobalContext.getInstance().setAllInstitutions();

			return 1;
			//Raza end
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	private static int ProcessCUPKeyExchange(ProcessContext processContext, Message inputMessage, ISOMsg isoMsg, NetworkManagementInfo netMgmtCode)
	{
		try {
			Institution targetInst = FinancialEntityService.findEntity(Institution.class, inputMessage.getChannel().getInstitutionId());
			Set<SecureKey> keySetAcq = null, keySetIss = null;
			String MsgType = isoMsg.getMTI();

//            SecurityProfile incomingSecProfile = null;
			Terminal targetTerminalAcq = null, targetTerminalIss = null;
			if (KeyManagementMode.ACQ_ISS_PIN.equals(inputMessage.getIfx().getMode())) {
				//Raza update key for both Acquirer and Issuing Terminals as there is single connection for Acquiring and Issuing
				if (ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87.equals(MsgType)) //Net Request
				{
					targetTerminalAcq = ProcessContext.get().getAcquireSwitchTerminal(targetInst);
					targetTerminalIss = ProcessContext.get().getIssuerSwitchTerminal(targetInst);

					if (targetTerminalAcq != null) {
						keySetAcq = targetTerminalAcq.getKeySet();
						SwitchTerminal st = (SwitchTerminal) targetTerminalAcq;
					} else {
						logger.info("Acquirer terminal not Found..");
					}

					if (targetTerminalIss != null) {
						keySetIss = targetTerminalIss.getKeySet();
					} else {
						logger.info("Issuer terminal not Found..");
					}

					SecureDESKey newKey = null, tempkey = null;

					logger.info("Received new key: [" + inputMessage.getIfx().getMode() + "]");
					newKey = HardwareSecurityModule.getInstance().TranslateKey(keySetIss, inputMessage.getIfx().getAddDataPrivate(), KeyType.TYPE_ZPK, inputMessage.getIfx());

					if (newKey != null) {
						GeneralDao.Instance.saveOrUpdate(newKey);
					} else {
						logger.info("Key not Received.. KeyExchange Falied! Response Initiator");
					}
				}
			}
			Message outMsg = generateKeyExchangeRs(inputMessage);
			processContext.getTransaction().addOutputMessage(outMsg);

			GeneralDao.Instance.saveOrUpdate(targetInst);
			GlobalContext.getInstance().setAllInstitutions();

			return 1;
			//Raza end
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	private static void ProcessVISAKeyExchange()
	{}

	private static void ProcessJCBKeyExchange()
	{}

	//m.rehman
	private static NetworkManagementAction processNetworkResponseMessage(Message inputMessage) {
		Ifx ifx = inputMessage.getIfx();
		Channel channel = inputMessage.getChannel();
		String respCode = ifx.getRsCode();

		if (respCode != null && respCode.equals(ISOResponseCodes.APPROVED)) {
			if (ifx.getIfxType().equals(IfxType.ECHO_RS)) {
				channel.setEchoCount((channel.getEchoCount() == null) ? 1 : channel.getEchoCount() + 1);

			} else if (ifx.getIfxType().equals(IfxType.SIGN_ON_RS)) {
				channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED);

			} else if (ifx.getIfxType().equals(IfxType.SIGN_OFF_RS)) {
				channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_DISABLED);

			} else if (ifx.getIfxType().equals(IfxType.CUTOVER_RS) ||
					ifx.getIfxType().equals(IfxType.CUTOVER_REPEAT_RS)) {

			} else if (ifx.getIfxType().equals(IfxType.KEY_EXCHANGE_RS)) {

			}
		} else {
			logger.info("Transaction is not approved. No operation required.");
		}

		GeneralDao.Instance.saveOrUpdate(channel);
		return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
	}

	private static void saveChannel(Message inMsg) //Raza save reflect channel update  in DB
	{
		if(GeneralDao.Instance.getCurrentSession().getTransaction().isActive())
		{
			GeneralDao.Instance.saveOrUpdate(inMsg.getChannel());
		}
		else
		{
			GeneralDao.Instance.beginTransaction();
			GeneralDao.Instance.saveOrUpdate(inMsg.getChannel());
			GeneralDao.Instance.endTransaction();
		}
	}
}