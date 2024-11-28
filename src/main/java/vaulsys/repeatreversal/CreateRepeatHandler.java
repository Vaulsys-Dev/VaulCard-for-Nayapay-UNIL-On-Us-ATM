package vaulsys.repeatreversal;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class CreateRepeatHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(CreateRepeatComponent.class);

	public static final CreateRepeatHandler Instance = new CreateRepeatHandler();

	private CreateRepeatHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		ScheduleMessage scheduleMessage;
		if (processContext.getInputMessage().isScheduleMessage()) {
			scheduleMessage = (ScheduleMessage) processContext.getInputMessage();
			if (SchedulerConsts.REPEAT_MSG_TYPE.equals(scheduleMessage.getMessageType())) {

				//TODO: double check referenceTransaction or firstTransaction
				Transaction refTranaction = scheduleMessage.getTransaction().getReferenceTransaction();
//				CreateRepeatComponent createRepeatComponent = new CreateRepeatComponent();
				Transaction trans = processContext.getTransaction();

				Message message = refTranaction.getOutputMessage();
				logger.debug("try to trigger repeat message for transaction with id= " + refTranaction.getId());
				if (message != null) {
					Message outMsg = new Message(MessageType.OUTGOING);
					outMsg.setTransaction(trans);
					outMsg.setEndPointTerminal(message.getEndPointTerminal());
					outMsg.setChannel(message.getChannel());

					outMsg.setRequest(true);
					outMsg.setNeedToBeSent(true);
					outMsg.setNeedToBeInstantlyReversed(false);
					outMsg.setNeedResponse(false);

					ISOMsg isoMsg = null;
					logger.debug("ifxtype for repeat message: " + message.getIfx().getIfxType());

					if (message.getIfx() != null
							&& !(message.getIfx().getIfxType().equals(IfxType.CUTOVER_RQ)
									|| message.getIfx().getIfxType().equals(IfxType.ACQUIRER_REC_RQ)
									|| message.getIfx().getIfxType()
									.equals(IfxType.CARD_ISSUER_REC_RQ))) {
						Ifx ifx = CreateRepeatComponent.createRepeatIfx(message.getIfx());
						String trnSeqCntr = Util.trimLeftZeros(Util.generateTrnSeqCntr(6));
						ifx.setSrc_TrnSeqCntr(trnSeqCntr);
						ifx.setMy_TrnSeqCntr(trnSeqCntr);
						ifx.setReceivedDt(outMsg.getStartDateTime());
						outMsg.setIfx(ifx);
						trans.addOutputMessage(outMsg);
						TransactionService.updateLifeCycleStatusNormally(trans, ifx);
						TransactionService.updateMessageForNotSuccessful(ifx, message.getTransaction());

						GeneralDao.Instance.saveOrUpdate(ifx);
						GeneralDao.Instance.saveOrUpdate(outMsg);
				        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
						GeneralDao.Instance.saveOrUpdate(trans);

						leaveNode(processContext, "to IFX to Protocol");
						return;
					} else if (message.getProtocolMessage() != null) {
						isoMsg = CreateRepeatComponent.createRepeatIsoMsg((ISOMsg) message.getProtocolMessage());
						outMsg.setProtocolMessage(isoMsg);
						trans.addOutputMessage(outMsg);
						TransactionService.updateLifeCycleStatusNormally(trans, null);

						GeneralDao.Instance.saveOrUpdate(outMsg);
				        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
						GeneralDao.Instance.saveOrUpdate(trans);

						leaveNode(processContext, "to Protocol to Binary");
						return;
					} else {
						// throw new Exception("Original Message doesn't have
						// neither ifx nor protocolMessage");
						logger.warn("Message of RefTrnx "+ refTranaction.getId()+"("+ refTranaction.getDebugTag()+") doesn't have neither ifx nor protocolMessage, so repeat message cannot be created");
					}
				}else{
					logger.debug("RefTrnx "+ refTranaction.getId()+"("+ refTranaction.getDebugTag()+") doesn't have outputmessage, so repeat message cannot be created");
			        //TODO: double check referenceTransaction or firstTransaction
					refTranaction = refTranaction.getReferenceTransaction();

					if (refTranaction != null) {
						logger.debug("Try to repeat(reverse) RefTrnx "+ refTranaction.getId()+"("+ refTranaction.getDebugTag()+")");
//						CreateReversalComponent reversalComponent  = new CreateReversalComponent();
						message = refTranaction.getOutputMessage();
						Ifx ifx = CreateReversalComponent.createReversalIfx(message.getIfx());
						Message outMsg = new Message(MessageType.OUTGOING);
						outMsg.setTransaction(trans);
						outMsg.setEndPointTerminal(message.getEndPointTerminal());
						outMsg.setChannel(message.getChannel());

						outMsg.setRequest(true);
						outMsg.setNeedToBeSent(true);
						outMsg.setNeedToBeInstantlyReversed(false);
						outMsg.setNeedResponse(false);

						ifx.setReceivedDt(outMsg.getStartDateTime());
						outMsg.setIfx(ifx);
						trans.addOutputMessage(outMsg);

						GeneralDao.Instance.saveOrUpdate(ifx);
						GeneralDao.Instance.saveOrUpdate(outMsg);
				        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
						GeneralDao.Instance.saveOrUpdate(trans);

						leaveNode(processContext, "to IFX to Protocol");
						return;
					}

				}

				// processContext.getTransaction().addOutputMessage();
			} else
				logger.debug("input message is not applicable type (ScheduleMessage)");
		}
		leaveToEndState(processContext);
	}
}
