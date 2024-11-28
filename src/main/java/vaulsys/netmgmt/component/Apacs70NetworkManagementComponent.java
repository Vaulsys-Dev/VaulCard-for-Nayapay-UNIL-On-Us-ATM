package vaulsys.netmgmt.component;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.calendar.DateTime;
import vaulsys.exception.base.DecisionMakerException;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.apacs70.base.Apacs70Utils;
import vaulsys.protocols.apacs70.base.OtherDataComponent;
import vaulsys.protocols.apacs70.base.RqAuxResetPassword;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.apacs70.base.RsAuxLogon;
import vaulsys.protocols.apacs70.base.RsFinNetMsg;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.POSTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.wfe.ProcessContext;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public class Apacs70NetworkManagementComponent {
	private static final Logger logger = Logger.getLogger(Apacs70NetworkManagementComponent.class);

	public static NetworkManagementAction processNetworkManagementMessage(ProcessContext processContext, Message inputMessage) throws Exception {
		ProtocolToXmlUtils.setXMLdata(inputMessage);
		RqBaseMsg rqMsg = (RqBaseMsg) inputMessage.getProtocolMessage();
		Terminal terminal = (Terminal) GeneralDao.Instance.getObject(inputMessage.getChannel().getEndPointType().getClassType(), rqMsg.terminalIdentity);

        Ifx inIfx = new Ifx();
        rqMsg.toIfx(inIfx);
        inIfx.setReceivedDt(inputMessage.getStartDateTime());
        IfxType inIfxType = inIfx.getIfxType();
        LifeCycle lifeCycle = null;
        if(terminal == null) {
        	inIfx.setSeverity(Severity.ERROR);
        	inIfx.setStatusDesc("Invalid POS/PINPAD id: " + rqMsg.terminalIdentity);
        	logger.error("Apacs Network Message: Invalid POS/PINPAD id: " + rqMsg.terminalIdentity);
        	lifeCycle = new LifeCycle();
    		lifeCycle.setIsComplete(true);
        }
        else {
        	inIfx.setTerminalType(terminal.getTerminalType());
			if (IfxType.POS_CONFIRMATION.equals(inIfxType)) {
				lifeCycle = terminal.getLastTransaction().getAndLockLifeCycle(LockMode.UPGRADE);
	    		lifeCycle.setIsComplete(true);
				inputMessage.setNeedToBeSent(false);
			} else if (IfxType.POS_FAILURE.equals(inIfxType)) {
				Transaction lastTransaction = TransactionService.findTerminalLastTransaction(terminal, inIfx);
				if(lastTransaction != null) {
					Transaction trxForReverse = lastTransaction.getFirstTransaction();
					
					if (!(trxForReverse.getInputMessage().isScheduleMessage() && SchedulerConsts.TIME_OUT_MSG_TYPE.equals(((ScheduleMessage) trxForReverse.getInputMessage()).getMessageType()))) {
//						trxForReverse = lastTransaction.getReferenceTransaction();
//					}

					lifeCycle = lastTransaction.getAndLockLifeCycle(LockMode.UPGRADE);
					if (!ISOFinalMessageType.isMessageNotToBeReverse(trxForReverse.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())) {
						MessageReferenceData ode = inIfx.getSafeOriginalDataElements();
						ode.setAppPAN(lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getAppPAN());
						if (lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType().equals(IfxType.TRANSFER_RQ)||
								lastTransaction.getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ)) {
							trxForReverse = TransactionService.findResponseTrx(lastTransaction.getLifeCycleId(), lastTransaction);
							
						} else {
							SettlementInfo srcStlInfo = trxForReverse.getSourceSettleInfo();
							if(srcStlInfo != null && !SettledState.NOT_SETTLED.equals(srcStlInfo.getSettledState())) {
								if (Long.parseLong(trxForReverse.getIncomingIfx()/*getInputMessage().getIfx()*/.getSrc_TrnSeqCntr()) 
										== Long.parseLong(inIfx.getSrc_TrnSeqCntr()))
									TransactionService.putDesiredFlagForNormalTransaction(trxForReverse, inIfx, new SourceDestination[] { SourceDestination.SOURCE/*,
										SourceDestination.DESTINATION*/ }, ClearingState.SUSPECTED_DISPUTE);
								logger.warn("Failure is received, but transcation: " + trxForReverse.getId() + " settled before! ignore reverse it");
							} else {
								SchedulerService.processReversalJob(trxForReverse, lastTransaction, ISOResponseCodes.APPROVED, null, false);
							}
						}
						lifeCycle = lastTransaction.getAndLockLifeCycle(LockMode.UPGRADE);
//						lifeCycle.setIsComplete(false);
//						lifeCycle.setIsFullyReveresed(LifeCycleStatus.REQUEST);
//						GeneralDao.Instance.saveOrUpdate(lifeCycle);
//						SchedulerService.createReversalJobInfo(lastTransaction.getFirstTransaction(), ErrorCodes.APPROVED, null);
					}
				}
				} else {
					lifeCycle = new LifeCycle();
					lifeCycle.setIsComplete(true);
				}
			} else {
				lifeCycle = new LifeCycle();
	    		lifeCycle.setIsComplete(true);
				inputMessage.setNeedToBeSent(true);
				if (IfxType.RESET_PASSWORD_RQ.equals(inIfxType)) {
					if(rqMsg.auxiliaryData != null) {
						if(rqMsg.auxiliaryData.odc != null)
							inIfx.setResetingPassword(rqMsg.auxiliaryData.odc.temporaryTerminalPassword);
						else if(rqMsg.auxiliaryData.rqAux != null) {
							RqAuxResetPassword reset = (RqAuxResetPassword) rqMsg.auxiliaryData.rqAux;
							inIfx.setResetingPassword(reset.temporaryTerminalPassword);
						}
					}
				}
				Apacs70Utils.checkValidityOfLastTransactionStatus(terminal, inIfx);
			}
		}
        if(inIfx.getIfxType().equals(IfxType.POS_CONFIRMATION) || inIfx.getIfxType().equals(IfxType.POS_FAILURE)){
        	inIfx.setOrigDt(DateTime.now());
        }
		inputMessage.setRequest(true);
        inputMessage.setNeedResponse(false);
        inputMessage.setNeedToBeInstantlyReversed(false);
		inputMessage.setEndPointTerminal(terminal);
		inputMessage.setIfx(inIfx);
		Transaction inTrx = inputMessage.getTransaction();
		inTrx.setDebugTag(inIfxType.toString());
		GeneralDao.Instance.saveOrUpdate(lifeCycle);
		inTrx.setLifeCycle(lifeCycle);
//		inTrx.setAuthorized(terminal != null);
		GeneralDao.Instance.saveOrUpdate(inIfx);
		GeneralDao.Instance.saveOrUpdate(inputMessage);
		GeneralDao.Instance.saveOrUpdate(inputMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(inTrx);

        if(IfxType.POS_CONFIRMATION.equals(inIfxType)) {
        	if(terminal != null)
        		TransactionService.checkValidityOfLastTransactionStatus(terminal, inIfx);
        }
        else if (IfxType.LOG_ON_RQ.equals(inIfxType) || IfxType.RESET_PASSWORD_RQ.equals(inIfxType)) {
			String rsCode = ISOResponseCodes.APPROVED;
			if (terminal != null) {
				if (IfxType.LOG_ON_RQ.equals(inIfx.getIfxType())) {
					
					try {
					/**** Don't remove keySet(POS's logOn with same terminalNumber & different serialNo) ****/
					if ((TerminalType.PINPAD.equals(terminal.getTerminalType()) && (inputMessage.getIfx().getSerialno()).equals(((PINPADTerminal)terminal).getSerialno())) ||
							(TerminalType.POS.equals(terminal.getTerminalType()) && (inputMessage.getIfx().getSerialno()).equals(((POSTerminal)terminal).getSerialno()))) {
						TerminalService.removeKeySet(terminal);
						POSTerminalService.addDefaultKeySetForTerminal(terminal);
						GeneralDao.Instance.flush();
					}
					} catch(Exception e) {
						logger.error("Exception in removing KeySet of terminal: " + terminal.getId() + e , e);
						TerminalService.removeKeySet(terminal);
						POSTerminalService.addDefaultKeySetForTerminal(terminal);
						GeneralDao.Instance.flush();
					}
				}

				try {
//					inTrx.setAuthorized(
							authorizeMessage(inputMessage);
//							);
				} catch (Exception e) {
//				} catch (AuthorizationException e) {
//					inTrx.setAuthorized(false);
					if (e instanceof DecisionMakerException) {
						DecisionMakerException dec = ((DecisionMakerException) e);
						dec.showCause(inputMessage.getIfx());
					}
					rsCode = ISOResponseCodes.INVALID_CARD_STATUS;
				}
				GeneralDao.Instance.saveOrUpdate(inTrx);
			} else {
				logger.error("Network message: invalid pos terminal id [" + rqMsg.terminalIdentity + "]");
				rsCode = ISOResponseCodes.WALLET_IN_PROVISIONAL_STATE;
			}
			Message outMsg = generateRs(processContext, inputMessage, rsCode);
			processContext.getTransaction().addOutputMessage(outMsg);
			return NetworkManagementAction.OUTPUT_MESSAGE_CREATED;
		}

        NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(inputMessage.getId());
        
		return NetworkManagementAction.DONE_WITHOUT_OUTPUT;
	}
	
    private static Boolean authorizeMessage(Message message) throws Exception {
    	Terminal terminal = message.getEndPointTerminal();
    	String serialNo = null;
    	if(terminal instanceof POSTerminal) {
    		serialNo = ((POSTerminal)terminal).getSerialno();
    		if(serialNo == null && message.getIfx().getBankId().equals(603769L)) {
        		serialNo = message.getIfx().getSerialno();
        		((POSTerminal)terminal).setSerialno(serialNo);
        	}
    	} else
    		serialNo = ((PINPADTerminal)terminal).getSerialno();

    	if (!serialNo.equals(message.getIfx().getSerialno())) {
    		logger.error("Incorrect serial No. POS/PINPAD serialNo: " + serialNo + ", message serialNo: " + message.getIfx().getSerialno());
    		throw new AuthorizationException("Incorrect serial No. POS/PINPAD serialNo: " + serialNo + ", message serialNo: " + message.getIfx().getSerialno());
    	}

    	if (message.getChannel().getMacEnable()) {
    		RqBaseMsg msg = (RqBaseMsg)message.getProtocolMessage();
			try {
				String mac = msg.MAC;
				if (mac == null) {
					logger.error("Failed: Mac Verification failed! (mac =null)");
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

    private static Message generateRs(ProcessContext processContext, Message inputMessage, String rsCode) {
    	Message outgoingMessage = new Message(MessageType.OUTGOING);
        outgoingMessage.setTransaction(inputMessage.getTransaction());
        if (CommunicationMethod.ANOTHER_SOCKET.equals(inputMessage.getChannel().getCommunicationMethod()))
            outgoingMessage.setChannel(((InputChannel) inputMessage.getChannel()).getOriginatorChannel());
        else
            outgoingMessage.setChannel(inputMessage.getChannel());

        Terminal terminal = inputMessage.getEndPointTerminal();
		outgoingMessage.setEndPointTerminal(terminal);
		
        Ifx outIfx = null;
        try {
        	outIfx = creatOutgoingIfx(outgoingMessage, inputMessage.getIfx());
        	outIfx.setRsCode(rsCode);
			outgoingMessage.setIfx(outIfx);
        } catch (CloneNotSupportedException e) {
        	logger.info("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
        	rsCode = ISOResponseCodes.MESSAGE_FORMAT_ERROR;
        }
        
        RsFinNetMsg outMsg = new RsFinNetMsg();
        outMsg.fromIfx(outIfx);
        if(ISOResponseCodes.APPROVED.equals(rsCode)) {
        	if (IfxType.LOG_ON_RS.equals(outIfx.getIfxType())) {
				try {
					SecureDESKey macKey = (SecureDESKey) terminal.getKeyByType(KeyType.TYPE_TAK);
					processContext.setLastAcqMacKey(macKey);
					TerminalService.removeKeySet(terminal);

					if(outMsg.auxiliaryData.rsAux != null) {
						RsAuxLogon logon = (RsAuxLogon)outMsg.auxiliaryData.rsAux;
						logon.encryptedPINKey = genKey(macKey, KeyType.TYPE_TPK, terminal);
						logon.encryptedMACKey = genKey(macKey, KeyType.TYPE_TAK, terminal);
						logon.encryptedMasterKey = logon.encryptedMACKey;
					}
					else {
						OtherDataComponent odc = outMsg.auxiliaryData.odc;
						odc.encryptedPINKey = genKey(macKey, KeyType.TYPE_TPK, terminal);
						odc.encryptedMACKey = genKey(macKey, KeyType.TYPE_TAK, terminal);
						odc.encryptedMasterKey = odc.encryptedMACKey;
					}
				} catch (Exception e) {
					logger.error("Key generation problem: ", e);
					rsCode = ISOResponseCodes.MESSAGE_FORMAT_ERROR;
				}
			}
        	else if(IfxType.RESET_PASSWORD_RS.equals(outIfx.getIfxType())) {
        		String rc = null;
        		if(terminal instanceof POSTerminal)
        			rc = ((POSTerminal)terminal).getResetCode();
        		else
        			rc = ((PINPADTerminal)terminal).getResetCode();
        		if (rc != null) {
					if (rc.equals(inputMessage.getIfx().getResetingPassword())) {
		        		if(terminal instanceof POSTerminal)
		        			((POSTerminal)terminal).setResetCode(null);
		        		else
		        			((PINPADTerminal)terminal).setResetCode(null);
						GeneralDao.Instance.saveOrUpdate(terminal);
					}
					else
						rsCode = ISOResponseCodes.INVALID_CARD_STATUS;
				}
        		else
        			rsCode = ISOResponseCodes.INVALID_CARD_STATUS;
        	}
        }

        outMsg.acquirerResponseCode = rsCode;
        outMsg.confirmationRequest = 0;
        outIfx.setRsCode(rsCode);

        outgoingMessage.setProtocolMessage(outMsg);
        outgoingMessage.setNeedToBeSent(true);
        outgoingMessage.setNeedResponse(false);
        outgoingMessage.setNeedToBeInstantlyReversed(false);
        outgoingMessage.setRequest(false);
        ProtocolToXmlUtils.setXMLdata(outgoingMessage);

        GeneralDao.Instance.saveOrUpdate(outIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        return outgoingMessage;
    }

    private static Ifx creatOutgoingIfx(Message outgoingMessage, Ifx inIfx) throws CloneNotSupportedException {
    	Ifx out = inIfx.clone();
		out.setIfxType(IfxType.getResponseIfxType(inIfx.getIfxType()));
		out.setIfxDirection(IfxDirection.OUTGOING);
		out.setReceivedDt(outgoingMessage.getStartDateTime());
		out.setTrnDt(DateTime.now());
		out.setUpdateReceiptRequired(false);
		out.setUpdateRequired(false);
    	return out;
    }
    
    private static byte[] genKey(SecureDESKey master, String keyType, Terminal terminal) throws Exception {
		SecureDESKey genKey = SecurityComponent.generateKey(SMAdapter.LENGTH_DES, keyType);
		genKey.setTerminal(terminal);
		GeneralDao.Instance.saveOrUpdate(genKey);
		terminal.addSecureKey(genKey);
		logger.debug(String.format("GenKey[%s]: new[%s]", keyType, genKey.getKeyBytes()));

		byte[] encNewKey = SecurityComponent.exportKey(genKey, (SecureDESKey) master);
		logger.debug(String.format("EncGenKey[%s]: encKey%s", keyType, Arrays.toString(encNewKey)));
		return encNewKey;
    }
}
