package vaulsys.protocols.handlers;

import vaulsys.authorization.exception.NotPaperReceiptException;
import vaulsys.authorization.exception.NotSubsidiaryAccountException;
import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.cms.components.CMSDBOperations;
import vaulsys.cms.components.CardAuthorizationHandler;
import vaulsys.config.IMDType;
import vaulsys.eft.base.terminalTypeProcessor.TerminalTypeProcessMap;
import vaulsys.eft.base.terminalTypeProcessor.TerminalTypeProcessor;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.exception.base.DecisionMakerException;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.CMSHttpMessage;
import vaulsys.protocols.cmsnew.CMSMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.NotApplicableTypeMessageException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.util.Util;
import vaulsys.wallet.components.WalletDBOperations;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public class ExceptionBinaryHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(ExceptionBinaryHandler.class);

	public static final ExceptionBinaryHandler Instance = new ExceptionBinaryHandler();

	private ExceptionBinaryHandler(){
	}

    @Override
    public void execute(ProcessContext processContext) throws Exception {

    	List<Exception> exceptions = processContext.getExceptions();
    	if (exceptions == null || exceptions.isEmpty())
    		return;

    	boolean isNotComplete = false;

        try {
            Transaction transaction = processContext.getTransaction();
//			transaction.setEndDateTime(DateTime.now());
			if (transaction.getLifeCycle() == null){
				LifeCycle lifeCycle = new LifeCycle();
				GeneralDao.Instance.saveOrUpdate(lifeCycle);
				transaction.setLifeCycle(lifeCycle);
				GeneralDao.Instance.saveOrUpdate(transaction);
			}

            Message incommingMessage =  processContext.getInputMessage();

            if (processContext.getOutputMessage() == null) {
                Message outgoingMessage = new Message(MessageType.OUTGOING);
                outgoingMessage.setTransaction(transaction);
                transaction.addOutputMessage(outgoingMessage);
            }

            Message outMessage = processContext.getOutputMessage();
//            outMessage.setNeedResponse(false);
//            outMessage.setNeedToBeInstantlyReversed(false);
//            //If we got exception for a response message we won't sent anything else to any destination
//            outMessage.setNeedToBeSent(incommingMessage.getRequest());
//            outMessage.setRequest(false);

            Ifx ifx = null;
            Channel channel = null;

            channel = setFlagOnDesiredExceptionMessage(processContext);

            try {
                if (outMessage.isOutgoingMessage()) {

                	if (channel == null)
                		channel = incommingMessage.getChannel();

                    ProtocolFunctions mapper = channel.getProtocol().getMapper();

                    if (channel.getCommunicationMethod().equals(CommunicationMethod.ANOTHER_SOCKET)) {
                        channel = ((InputChannel) channel).getOriginatorChannel();
                    }

                    ifx = incommingMessage.getIfx();

                    // we will send a message with proper error code to the originator of the message;
                    // If incoming message is a response, we won't send any exception message to its sender, so there's no outgoing message
                    if (outMessage.getNeedToBeSent() != null && outMessage.getNeedToBeSent()==true) {

                        ifx = MsgProcessor.processor(ifx);
                        ifx.setReceivedDt(outMessage.getStartDateTime());

						//m.rehman: reverse limit if it is a financial transaction
						//m.rehman: separating BI from financial incase of limit
						if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType(), true)) {
							logger.info("Reversing limit");
							String addData =  ifx.getAddDataPrivate();
							IMDType imdType = CMSDBOperations.getIMDType(ifx.getAppPAN());
							if ((Util.hasText(addData) && addData.substring(0,1).equals("W")) ||
									(imdType != null && imdType.equals(IMDType.Wallet)))
								WalletDBOperations.Instance.ReverseCardLimit(processContext);
							else
								CardAuthorizationHandler.Instance.ReverseCardLimit(processContext);
						}

						//m.rehman: setting response code, if any
						if (outMessage.getIfx() != null) {
							if (Util.hasText(outMessage.getIfx().getRsCode()))
								if (!ifx.getRsCode().equals(ISOResponseCodes.APPROVED))
									ifx.setRsCode(outMessage.getIfx().getRsCode());
								else
									ifx.setRsCode(ISOResponseCodes.INVALID_IMD);
							else
								ifx.setRsCode(ISOResponseCodes.INVALID_IMD);

						} else {
							if (!Util.hasText(ifx.getRsCode()) || ifx.getRsCode().equals(ISOResponseCodes.APPROVED))
								ifx.setRsCode(ISOResponseCodes.INVALID_IMD);
						}

                        Exception ex;
                        GeneralDao.Instance.saveOrUpdate(ifx);
                        
						if ((ex = exceptions.get(0)) instanceof DecisionMakerException) {
                            DecisionMakerException dec = ((DecisionMakerException) ex);
                            dec.alterIfxByErrorType(ifx);
                            if (!dec.returnError()) {
                                processContext.removeAllOutputMessage();
                                ((DecisionMakerException) ex).showCause(incommingMessage.getIfx());
                                TransactionService.updateMessageForNotSuccessful(ifx, transaction);
                                return;
                            }
                        } else {
                        	showCauseForNotDecisionMakerException(ex, incommingMessage.getIfx());
                        	processContext.removeAllOutputMessage();
                        	TransactionService.updateMessageForNotSuccessful(ifx, transaction);
                            return;
                        }

						if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ) {
							ifx.setIfxType(IfxType.getResponseIfxType(ifx.getIfxType()));
							//m.rehman: setting response message type
							ifx.setMti(ISOMessageTypes.getResponseMTI(ifx.getMti()));
						}

                        outMessage.setIfx(ifx);
                        outMessage.setChannel(channel);
						//m.rehman: for UI Exception messages, we need to update terminal from input message
						Terminal endpointTerminal;
						if (incommingMessage.getChannel().getEndPointType().equals(EndPointType.UI_TERMINAL)) {
							endpointTerminal = incommingMessage.getEndPointTerminal();
						} else {
							endpointTerminal = TerminalService.findEndpointTerminalForExceptionMessage(outMessage);
						}
                        outMessage.setEndPointTerminal(endpointTerminal);
                        mapper.addOutgoingNecessaryData(outMessage.getIfx(), outMessage.getTransaction());
//                        EncodingConvertor convertor = GlobalContext.getInstance().getConvertor( channel.getEncodingConvertor());
                        EncodingConvertor convertor = ProcessContext.get().getConvertor(channel.getEncodingConverter());
                        ProtocolMessage protocolMessage = getProtocolMessage(incommingMessage.getProtocolMessage(), outMessage.getIfx(), mapper, convertor);

                        /********* set cleared flag on last transaction *******/
                        TerminalTypeProcessor binderProcessor = TerminalTypeProcessMap.getMessageBinderProcessor(incommingMessage.getIfx());
                        binderProcessor.checkValidityOfLastTransactionStatus(incommingMessage.getIfx());
                        
                        /********* set last transaction *******/
                        updateEndPointTerminal(endpointTerminal, outMessage);

                        if (protocolMessage == null) {
                        	logger.info("No Protocol Message, so all output messages are removed from process context.");
                            processContext.removeAllOutputMessage();
                            showExceptionCause(ifx, incommingMessage.getIfx(), ex);
                            if (!Util.hasText(outMessage.getTransaction().getDebugTag()))
                            	outMessage.getTransaction().setDebugTag(outMessage.getIfx().getIfxType().toString());
    						
                            
                            if (incommingMessage.getIfx()!= null){
    							incommingMessage.getIfx().setStatusDesc(incommingMessage.getIfx().getStatusDesc()+";\r\nExceptionBinaryHandler:No Protocol Message, so all output messages are removed from process context.");
    							incommingMessage.getIfx().setSeverity(Severity.ERROR);
    							GeneralDao.Instance.saveOrUpdate(incommingMessage.getIfx());
    						}else{
	    						ifx.setStatusDesc(ifx.getStatusDesc()+";\r\nExceptionBinaryHandler:No Protocol Message, so all output messages are removed from process context.");
	    						incommingMessage.getIfx().setSeverity(Severity.ERROR);
	    						GeneralDao.Instance.saveOrUpdate(ifx);
	    						GeneralDao.Instance.saveOrUpdate(outMessage);
	    				        GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
    						}
							TransactionService.putFlagOnTransaction(outMessage.getTransaction());
							TransactionService.updateMessageForNotSuccessful(ifx, transaction);
                            return;
                        }

                        showExceptionCause(ifx, incommingMessage.getIfx(), ex);
//                        ((DecisionMakerException) ex).rollBack(ifx, incommingMessage.getTransaction(), incommingMessage.getEndPointTerminal());

                        outMessage.setProtocolMessage(protocolMessage);

                        if (!Util.hasText(outMessage.getTransaction().getDebugTag()))
                        	outMessage.getTransaction().setDebugTag(outMessage.getIfx().getIfxType().toString());

                        byte[] data = mapper.toBinary(outMessage.getProtocolMessage());

						// Added By : Asim Shahzad, Date : 8th Dec 2016, Desc : For VISA SMS handling
						if (outMessage.getHeaderData() == null && outMessage.getChannel().getHeaderLen() > 0)
							outMessage.setHeaderData(outMessage.getTransaction().getFirstTransaction().getInputMessage().getHeaderData());

						data = outMessage.setBinaryDataWithHeader(data);

	       				outMessage.setBinaryData(data);

//                        outMessage.setBinaryData(mapper.toBinary(outMessage.getProtocolMessage()));

                        try {
                            mapper.postProcessBinaryMessage(processContext, outMessage);
                        } catch (Exception e) {
                            logger.error("An Exception is encountered in posProcessBinaryMessage("+ e.getClass().getSimpleName()+" :"+ e.getMessage(), e);
                        }

			if (Boolean.TRUE.equals(channel.getIsSecure())) {
            			if (outMessage.getTransaction().getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) {
            				outMessage.setBinaryData(mapper.encryptBinaryMessage(outMessage.getBinaryData(), outMessage.getTransaction().getReferenceTransaction().getInputMessage()));
            			} else {
            				outMessage.setBinaryData(mapper.encryptBinaryMessage(outMessage.getBinaryData(), outMessage.getTransaction().getFirstTransaction().getInputMessage()));
            			}
            		}

                        ProtocolToXmlUtils.setXMLdata(outMessage);
						if (outMessage.getNeedToBeSent()) {

							GeneralDao.Instance.saveOrUpdate(ifx);
							GeneralDao.Instance.saveOrUpdate(outMessage);
							
							if (!(outMessage.getProtocolMessage() instanceof CMSHttpMessage))
								if (!(outMessage.getProtocolMessage() instanceof CMSMessage))
									GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
							
							TransactionService.putFlagOnTransaction(outMessage.getTransaction());

							logger.debug("Message is going to sent to " + channel.getName() + ":\n"
									+ outMessage.getXML());
						} else {
							logger.debug("No Message sent to " + channel.getName() + ":\n" + outMessage.getXML()/*outMessage.getProtocolMessage().toString()*/);
							processContext.removeAllOutputMessage();
						}
                    } else {
						isNotComplete = generateExceptionMessageToReverse(processContext, incommingMessage, ifx);
						processContext.removeAllOutputMessage();
					}

                } else{
                    throw new NotApplicableTypeMessageException("Recieved message must be an OutgoingMessage instead of "+ outMessage.getType());
                }

            } catch (Exception ex) {
                logger.error("An Exception in ExceptionBinaryHandler: " + ex, ex);
                if (outMessage != null) {
                	if (outMessage.getIfx()!= null)
                		showExceptionCause(outMessage.getIfx(), null, ex);
                	else 
                		showExceptionCause(incommingMessage.getIfx(), null, ex);
                	
					GeneralDao.Instance.saveOrUpdate(outMessage.getIfx());
					GeneralDao.Instance.saveOrUpdate(outMessage);
			        GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
				}
                throw ex;
            } finally {
            	if (ifx == null)
            		ifx = incommingMessage.getIfx();

            	TransactionService.updateMessageForNotSuccessful(ifx, transaction);

            	if (!isNotComplete) {
            		/*************************************/
            		//TODO HATMAN daghighan barresi shavad!!!
//            		getTransactionService().updateLifeCycleStatusForNotSuccessful(incommingMessage.getTransaction(), ifx);
            		TransactionService.updateLifeCycleStatus(transaction, ifx);
            		/*************************************/
            	}
            }
        } catch (Exception e) {
        	logger.error("An Exception in ExceptionBinaryHandler("+ e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
            throw e;
        }
    }

	private Channel setFlagOnDesiredExceptionMessage(ProcessContext processContext) {
		Message outMessage = processContext.getOutputMessage();
		Message inputMessage = processContext.getInputMessage();

		outMessage.setNeedResponse(false);
        outMessage.setNeedToBeInstantlyReversed(false);
        //If we got exception for a response message we won't sent anything else to any destination
		outMessage.setNeedToBeSent(inputMessage.getRequest());
        outMessage.setRequest(false);

        if ((processContext.getExceptions().get(0) instanceof NotPaperReceiptException &&
        		ISOFinalMessageType.isForceShowIfReceiptErrorRsMessage(inputMessage.getIfx().getIfxType())) ||
        	(processContext.getExceptions().get(0) instanceof NotSubsidiaryAccountException)) {
        		outMessage.setNeedResponse(true);
                outMessage.setNeedToBeInstantlyReversed(false);
        		outMessage.setNeedToBeSent(true);
                outMessage.setRequest(false);
                return inputMessage.getTransaction().getFirstTransaction().getInputMessage().getChannel();
        	}

        return null;
	}

	private void showExceptionCause(Ifx ifx, Ifx incommingIfx, Exception ex) {
		
		if (ex instanceof DecisionMakerException) {

			if (incommingIfx != null)
				((DecisionMakerException) ex).showCause(incommingIfx);

			((DecisionMakerException) ex).showCause(ifx);
		} else {
			showCause(ifx, ex);
		}
	}
	
	public void showCause(Ifx ifx, Exception e) {
		if(ifx != null) {
			ifx.setSeverity(Severity.ERROR);
			
			String strErr = this.getClass().getSimpleName() + ": " + e.getMessage();
			if (Util.hasText(ifx.getStatusDesc()))
				strErr = ifx.getStatusDesc()+";\r\n\r\n"+ strErr;
			ifx.setStatusDesc(strErr);
			
			if (e!= null && e.getStackTrace()!= null){
				strErr ="";
				for (StackTraceElement s: e.getStackTrace()){
					if (s.toString().startsWith("vaulsys")){
						strErr += ";\r\n"+s.toString();
					}
				}
				ifx.setStatusDesc(ifx.getStatusDesc()+strErr);
			}
		}
	}

	private void updateEndPointTerminal(Terminal endpointTerminal, Message outMsg) {
		if(endpointTerminal == null){
			logger.info("endpoint terminal of outgoing message was not found! so last transaction couldn't be set.");
			return;
		}
//		if(!TerminalType.SWITCH.equals(endpointTerminal.getTerminalType())){
        Channel channel = outMsg.getChannel();
        if (endpointTerminal != null && /*!TerminalType.SWITCH.equals(endPointTerminal.getTerminalType())*/  !EndPointType.isSwitchTerminal(channel.getEndPointType())) {
			TerminalService.setLastTransaction(endpointTerminal, outMsg);
			GeneralDao.Instance.saveOrUpdate(endpointTerminal);
		}

		/*if (TerminalType.POS.equals(endpointTerminal.getTerminalType())){
		    TerminalService.removeKeySet(endpointTerminal);

		    try {
				POSTerminalService.addDefaultKeySetForTerminal((POSTerminal) endpointTerminal);
			} catch (Exception e1) {
				logger.error("Old Pos terminal keyset is removed, but in generating the new set an exception is encounterd("+ e1.getClass().getSimpleName()+" :"+ e1.getMessage()+")");
			}
		}*/
	}

	private boolean generateExceptionMessageToReverse(ProcessContext processContext, Message incommingMessage, Ifx ifx) {
		Exception ex;
		Ifx dummyIfx = new Ifx();
		if ((ex = processContext.getExceptions().get(0)) instanceof DecisionMakerException ) {
		    DecisionMakerException dec = ((DecisionMakerException) ex);
		    dec.showCause(ifx);
		    dec.rollBack(ifx, incommingMessage.getTransaction(), incommingMessage.getEndPointTerminal());
		    dec.alterIfxByErrorType(dummyIfx);
		    GeneralDao.Instance.saveOrUpdate(ifx);
		    GeneralDao.Instance.saveOrUpdate(incommingMessage);
	        GeneralDao.Instance.saveOrUpdate(incommingMessage.getMsgXml());
		} else {
			showCauseForNotDecisionMakerException(ex, ifx);
			GeneralDao.Instance.saveOrUpdate(ifx);
		}

		String rsCode = Util.hasText(dummyIfx.getRsCode())? dummyIfx.getRsCode(): ISOResponseCodes.INVALID_CARD_STATUS;

		try {

			incommingMessage.getTransaction().getAndLockLifeCycle(LockMode.UPGRADE);
			
			//TODO double check this condition carefully!!
			if (!LifeCycleStatus.NOTHING.equals(incommingMessage.getTransaction().getLifeCycle().getIsFullyReveresed()) &&
					!LifeCycleStatus.NOTHING.equals(incommingMessage.getTransaction().getLifeCycle().getIsPartiallyReveresed()))
				return false;
			else
				return addReversalTrigger(processContext, incommingMessage, rsCode);

		} catch (Exception e) {
			logger.error("Couldn't reverse "
					+ incommingMessage.getTransaction().getDebugTag()+": "+ e.getClass().getSimpleName()+": "+ e.getMessage());
		}
		return false;
	}

	private boolean addReversalTrigger(ProcessContext processContext, Message responseMessage, String rsCode) {

		if (ISOFinalMessageType.isReversalMessage(responseMessage.getIfx().getIfxType())
			||
			ISOFinalMessageType.isRepeatMessage(responseMessage.getIfx().getIfxType())
			||
			ISOFinalMessageType.isMessageNotToBeReverse(responseMessage.getIfx().getIfxType())
			|| !ISOResponseCodes.APPROVED.equals(responseMessage.getIfx().getRsCode())
			){
			return false;
		}

		logger.debug("Trying to put reversal schedule message for "+ responseMessage.getTransaction().getDebugTag());
		ScheduleMessage reverseMessage = SchedulerService.addInstantReversalAndRepeatTriggerAndRemoveOldTriggers(
				responseMessage.getTransaction().getFirstTransaction(), rsCode, 0L);
		processContext.addPendingRequests(reverseMessage);
		return true;
	}

    private ProtocolMessage getProtocolMessage(ProtocolMessage message, Ifx ifx, ProtocolFunctions mapper, EncodingConvertor covertor){
    	ProtocolMessage  protocolMessage = null;

    	if (!Util.hasText(ifx.getStatusDesc())) {
			if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ) {

				ifx.setIfxType(IfxType.getResponseIfxType(ifx.getIfxType()));
			}

			try {
				protocolMessage = mapper.fromIfx(ifx, covertor);
			} catch (NotMappedIfxToProtocolException e) {
				logger.error("Encounter an exception in fromIfx: "+ e.getClass().getSimpleName()+"- "+ e.getMessage());
			}
		}else {
			try {
				protocolMessage = mapper.outgoingFromIncoming(message, ifx, covertor);
			} catch (Exception e) {
				logger.error("Encounter an exception in generating outgoing msg from incoming: "+ e.getClass().getSimpleName()+"- "+ e.getMessage());
			}
		}

    	return protocolMessage;
    }

    public void showCauseForNotDecisionMakerException(Exception e , Ifx ifx){
    	StackTraceElement[] stackTrace = e.getStackTrace();
    	String strErr = e.toString();

		for (StackTraceElement s: stackTrace){
			if (s.toString().startsWith("vaulsys")){
				strErr += ";\r\n"+s.toString();
//				break;
			}
		}

		ifx.setStatusDesc(strErr);
    }

}
