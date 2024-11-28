package vaulsys.eft.base;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.impl.TxnRule;
import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.eft.base.ifxTypeProcessor.IfxTypeProcessMap;
import vaulsys.eft.base.ifxTypeProcessor.MessageProcessor;
import vaulsys.eft.exception.PinBlockException;
import vaulsys.message.Message;
import vaulsys.message.ScheduleMessage;
import vaulsys.mtn.exception.NoChargeAvailableException;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMConnectionStatus;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

//@SuppressWarnings("serial") FIXME: redundant
public class MessageProcessHandler extends BaseHandler {
	private static Logger logger = Logger.getLogger(MessageProcessHandler.class);

	public static final MessageProcessHandler Instance = new MessageProcessHandler();

	private MessageProcessHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {

		try {
			logger.info("Process Message ...."); //Raza LOGGING ENHANCED

			Message incomingMessage = processContext.getInputMessage();
			Transaction transaction = incomingMessage.getTransaction();

			// Update ATM connection status or POS application version
			saveTerminalProperties(incomingMessage);
			Object outputchannel ;
//			if(incomingMessage.getIfx().getTerminalType().equals(TerminalType.INTERNET)&& incomingMessage.getIfx().getIfxType().equals(IfxType.PREPARE_ONLINE_BILLPAYMENT_RQ)){
//				outputchannel = incomingMessage.getChannel();
//				
//			}
//			else
//				outputchannel =/*processContext.getVariable("outputChannel")*/;
			outputchannel = processContext.getOutputChannel("outputChannel");
			Channel channel = null;
			if (outputchannel instanceof String)
				channel = null;
			else
				channel = (Channel) outputchannel;

			
			IfxType firstIfxType = IfxType.UNDEFINED;
			IfxType refIfxType = IfxType.UNDEFINED;
 
			if (!transaction.getFirstTransaction().getInputMessage().isScheduleMessage()) {
				firstIfxType = transaction.getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType();
			}else{
				ScheduleMessage message = (ScheduleMessage) transaction.getFirstTransaction().getInputMessage();
				if (SchedulerConsts.SETTLEMENT_MSG_TYPE.equals(message.getMessageType()))
					firstIfxType = transaction.getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType();
			}
			if (transaction.getReferenceTransaction() != null &&
				(!transaction.getReferenceTransaction().getInputMessage().isScheduleMessage()
					|| (transaction.getReferenceTransaction().getInputMessage().isScheduleMessage()&& SchedulerConsts.SETTLEMENT_MSG_TYPE.equals(((ScheduleMessage) transaction.getReferenceTransaction().getInputMessage()).getMessageType()))
						) ) {
				refIfxType = transaction.getReferenceTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType();
			}

			MessageProcessor processor = IfxTypeProcessMap.getProcessor(incomingMessage.getIfx(), firstIfxType, refIfxType);
//			processor.setProcessContext(processContext);
			logger.info(processor.getClass().getSimpleName() + " is going to create outgoing message");
			Message outgoingMessage = processor.createOutgoingMessage(transaction, incomingMessage, channel, processContext);
			outgoingMessage = processor.postProcess(transaction, incomingMessage, outgoingMessage, channel);

			if (outgoingMessage != null) {
				if (outgoingMessage.getPendingRequests() != null) {
					processContext.setPendingRequests(outgoingMessage.getPendingRequests());

					/*for (Message pending : outgoingMessage.getPendingRequests())
						processContext.addPendingRequests(pending);*/
					outgoingMessage.setPendingRequests(null);
				}
			} else /*if (outgoingMessage == null)*/ { //FIXME: redundant if
				if (incomingMessage.getPendingRequests() != null) {
					processContext.setPendingRequests(incomingMessage.getPendingRequests());
					incomingMessage.setPendingRequests(null);
				}
			}

			if (incomingMessage.getChannel().getProtocol().getSecurityFunctions().isTranslatePIN(incomingMessage.getIfx())){
//				Pair<SecurityProfile, Set<SecureKey>> secData = getProfileAndKeySet(incomingMessage);
				Long inProfileId = incomingMessage.getEndPointTerminal().getOwnOrParentSecurityProfileId();
				Set<SecureKey> inKeySet = incomingMessage.getEndPointTerminal().getKeySet();
//				SecurityProfile inProfile = secData.first;
//				Set<SecureKey> inKeySet = secData.second;

				if (outgoingMessage != null) {
					Ifx outgoingIFX = outgoingMessage.getIfx();

//					Pair<SecurityProfile, Set<SecureKey>> outSecData = getProfileAndKeySet(outgoingMessage);
					Long profileId = outgoingMessage.getEndPointTerminal().getOwnOrParentSecurityProfileId();
					Set<SecureKey> keySet = outgoingMessage.getEndPointTerminal().getKeySet();
//					SecurityProfile profile = outSecData.first;
//					Set<SecureKey> keySet = outSecData.second;
					/*System.out.println("MessageProcessHandler:: Not Going To Translate PINBLOCK...!"); //Raza TEMP
					try {
						setPinBlock(outgoingIFX, 
								profileId, keySet, 
								inProfileId, inKeySet, 
								incomingMessage.getChannel().getPinTrans_enable(), 
								outgoingMessage.getChannel().getPinTrans_enable());

					} catch (Exception e) {
//						System.err.println("Only For Test: Catch " + e.getClass().getSimpleName() + "- " + e.getMessage());
						logger.error("Catch " + e.getClass().getSimpleName() + "- " + e.getMessage());
						preProcessPinBlockException(incomingMessage,
							outgoingMessage);
//						TODO uncomment this line! (only for test!)
						throw new PinBlockException("PinBlock Error for Message number: "+incomingMessage.getId(),true);
					}*/
				}
			}

			//Apply Transaction Rule After PIN Translation
			//try {
				logger.debug("Validating TXN by Rules");
				if (TxnRule.ValidateTxn(incomingMessage.getIfx(), incomingMessage.getChannel().getName(), ((Channel) processContext.getOutputChannel(null)).getName())) {
					logger.debug("TXN Validation Done OK");
				} else {
					logger.info("TXN Validation Failed"); //or Not Applied");
					throw new AuthorizationException();
				}
			//}
			//catch (Exception e)
			//{
				//logger.debug("TXN Validation Failed or Not Applied");
			//}

		} catch (ScheduleMessageFlowBreakDown e1) {
			logger.warn(e1.getClass().getSimpleName()+": "+ e1.getMessage());
			//logger.error(e);
			throw e1;
		} catch (Exception e) {
			if( e instanceof NoChargeAvailableException ||
				e instanceof InvalidBusinessDateException ||
				e instanceof PinBlockException){
				logger.warn(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
			}else{
				logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
			}
			//logger.error(e);
			throw e;
		} finally {
			if (processContext.getOutputMessage()!= null)
				GeneralDao.Instance.saveOrUpdate(processContext.getOutputMessage().getIfx());
		}
	}

	private void preProcessPinBlockException(Message incomingMessage,
			Message outgoingMessage) {
		Terminal endpointTerminal = TerminalService.findEndpointTerminal(outgoingMessage, outgoingMessage.getIfx(), incomingMessage.getChannel().getEndPointType());//me
		outgoingMessage.setEndPointTerminal(endpointTerminal);
		outgoingMessage.setChannel(incomingMessage.getChannel());

		//m.rehman: set response code as General Processing Error
		if (!Util.hasText(outgoingMessage.getIfx().getRsCode()))
			outgoingMessage.getIfx().setRsCode(ISOResponseCodes.ORIGINAL_ALREADY_REJECTED);

		GeneralDao.Instance.saveOrUpdate(outgoingMessage.getIfx());
	}

	private void saveTerminalProperties(Message incomingMessage) {
		Terminal endpointTerminal = incomingMessage.getEndPointTerminal();
		try {
			if (TerminalType.ATM.equals(endpointTerminal.getTerminalType())) {
				IoSession session = NetworkManager.getInstance().getResponseOnSameSocketConnectionById(
						incomingMessage.getId());

				/****************/
				NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(incomingMessage.getId());
				logger.info("removing removeResponseOnSameSocketConnectionById: " + incomingMessage.getId());
				/****************/

//				NetworkManager.getInstance().addTerminalOpenConnection(endpointTerminal.getCode(), session);
				NetworkManager.getInstance().addTerminalOpenConnection(((ATMTerminal)endpointTerminal).getIP(), session);
				if (!ATMConnectionStatus.CONNECTED.equals(((ATMTerminal)endpointTerminal).getConnection())){
					((ATMTerminal)endpointTerminal).setConnection(ATMConnectionStatus.CONNECTED);
					GeneralDao.Instance.saveOrUpdate(endpointTerminal);
				}
			} else if (TerminalType.POS.equals(endpointTerminal.getTerminalType())) {
				if (ISOFinalMessageType.isRequestMessage(incomingMessage.getIfx().getIfxType())) {
					((POSTerminal)endpointTerminal).setApplicationVersion(incomingMessage.getIfx().getApplicationVersion());
				}
			} else if (TerminalType.PINPAD.equals(endpointTerminal.getTerminalType())) {
				if (ISOFinalMessageType.isRequestMessage(incomingMessage.getIfx().getIfxType())) {
					((PINPADTerminal)endpointTerminal).setApplicationVersion(incomingMessage.getIfx().getApplicationVersion());
				}
			}
		} catch (Exception e) {
			logger.info("Session of ATM[" + endpointTerminal.getCode() + "] cannot be saved!");
		}
	}

//	private Pair<SecurityProfile, Set<SecureKey>> getProfileAndKeySet(Message message) throws Exception {
//		SecurityProfile securityProfile = message.getEndPointTerminal().getOwnOrParentSecurityProfile();
//		Set<SecureKey> keySet = message.getEndPointTerminal().getKeySet();
//
//		return new Pair<SecurityProfile, Set<SecureKey>>(securityProfile, keySet);
//	}
//
	private void setPinBlock(Ifx ifx, Long profileId, Set<SecureKey> keySet, Long inProfileId,
			Set<SecureKey> inKeySet, boolean incommingEnable, boolean outgoingEnable) throws Exception {
		
		if(incommingEnable == true && outgoingEnable == true){
			//Pinblock encryption is enabled on both sides, so we need to translate pinblock!
			if (profileId != null && keySet != null) {
				
//Raza Verify PIN translation cases - cureent using simple for HSM
				/*if ( ifx.getIfxType() != null &&  ISOFinalMessageType.isHotCardMessage(ifx.getIfxType()))
					ifx.setPINBlock(translatePIN(ifx.getPINBlock(), ifx.getSecondAppPan(), profileId, keySet, inProfileId, inKeySet,ifx.getAppPAN()));
				
				else
					ifx.setPINBlock(translatePIN(ifx.getPINBlock(), ifx.getAppPAN(), profileId, keySet, inProfileId, inKeySet));

				ifx.setNewPINBlock(translatePIN(ifx.getNewPINBlock(), ifx.getAppPAN(), profileId, keySet, inProfileId, inKeySet));

				ifx.setOldPINBlock(translatePIN(ifx.getOldPINBlock(), ifx.getAppPAN(), profileId, keySet, inProfileId, inKeySet));*/
				translatePIN(ifx, keySet, inKeySet);
			}
		}else if(incommingEnable == false && outgoingEnable == true){
			//Pinblock encryption is enabled on outgoing side, so we need to encrypt clear pin with outgoing side key!
			if (profileId != null && keySet != null ) {

				ifx.setPINBlock(encryptPIN(ifx.getPINBlock(), ifx.getAppPAN(), profileId, keySet));

				ifx.setNewPINBlock(encryptPIN(ifx.getNewPINBlock(), ifx.getAppPAN(), profileId, keySet));

				ifx.setOldPINBlock(encryptPIN(ifx.getOldPINBlock(), ifx.getAppPAN(), profileId, keySet));
			}
		}else if(incommingEnable == true && outgoingEnable == false){
			//Pinblock encryption is enabled on incoming side, so we need to extract clear pin for outgoing side!
			if (inProfileId != null && inKeySet != null) {
				ifx.setPINBlock(decryptPIN(ifx.getPINBlock(), ifx.getAppPAN(), inProfileId, inKeySet));

				ifx.setNewPINBlock(decryptPIN(ifx.getNewPINBlock(), ifx.getAppPAN(), inProfileId, inKeySet));

				ifx.setOldPINBlock(decryptPIN(ifx.getOldPINBlock(), ifx.getAppPAN(), inProfileId, inKeySet));
			}
		}
	}

	private String decryptPIN(String pin, String appPAN, Long inProfileId, Set<SecureKey> inKeySet)
			throws SMException, Exception {
		if(pin == null || "".equals(pin))
			return null;

		String pinBlock = SecurityComponent.decryptPINByKey(inProfileId, inKeySet, Hex.decode(pin), appPAN);
		if (Util.hasText(pinBlock))
			return pinBlock;

		return null;
	}


	private String encryptPIN(String pin, String appPAN, Long profileId, Set<SecureKey> keySet) throws SMException,
			Exception {
		if(pin == null || "".equals(pin))
			return null;

		byte[] pinBlock = SecurityComponent.encryptPINByKey(profileId, keySet, pin, appPAN);
		if (pinBlock != null)
			return new String(Hex.encode(pinBlock)).toUpperCase();

		return null;
	}

	private String translatePIN(String pin, String appPAN, Long profileId, Set<SecureKey> keySet,
			Long inProfileId, Set<SecureKey> inKeySet) throws SMException, Exception {

		if(pin == null || "".equals(pin))
			return null;

		byte[] pinBlock = SecurityComponent.translatePIN(profileId, keySet, Hex.decode(pin), appPAN, inProfileId, inKeySet);
		if (pinBlock != null)
			return new String(Hex.encode(pinBlock)).toUpperCase();

		return null;
	}
	
	private String translatePIN(String pin, String appPAN, Long profileId, Set<SecureKey> keySet,
			Long inProfileId, Set<SecureKey> inKeySet,String desAppPAN) throws SMException, Exception {

		if(pin == null || "".equals(pin))
			return null;

		byte[] pinBlock = SecurityComponent.translatePIN(profileId, keySet, Hex.decode(pin), appPAN, inProfileId, inKeySet,desAppPAN);
		if (pinBlock != null)
			return new String(Hex.encode(pinBlock)).toUpperCase();

		return null;
	}	

	//m.rehman
	private void translatePIN(Ifx ifxObj, Set<SecureKey> keySet, Set<SecureKey> inKeySet)
			throws Exception {

		String oldPinBlock, newPinBlock, respCode;

		oldPinBlock = ifxObj.getPINBlock();

		if(oldPinBlock == null || "".equals(oldPinBlock)) {
			logger.error("PIN Block field is empty. Returning back.");
			return;
		}

		//HardwareSecurityModule.getInstance().PINTranslation(ifxObj, inKeySet, keySet);

		newPinBlock = ifxObj.getNewPINBlock();
		respCode = ifxObj.getRsCode();

		if (!Util.hasText(respCode)) {
			logger.error("No response received. HSM Time Out");
			ifxObj.setRsCode(ISOResponseCodes.INVALID_CARD);
			throw new Exception("No response received. HSM Time Out");
		}
		if (!respCode.equals(ISOResponseCodes.APPROVED)) {
			logger.error("PIN Translation Failed with response code: " + respCode);
			ifxObj.setRsCode(ISOResponseCodes.INVALID_CARD);
			throw new Exception("PIN Translation Failed with response code: " + respCode);
		} else {
			ifxObj.setPINBlock(newPinBlock);
		}
	}
}
