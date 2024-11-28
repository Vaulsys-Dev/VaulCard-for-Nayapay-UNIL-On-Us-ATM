package vaulsys.repeatreversal;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public class CreateTimeOutHandler extends BaseHandler {

	private static final Logger logger = Logger.getLogger(CreateTimeOutHandler.class);

	public static final CreateTimeOutHandler Instance = new CreateTimeOutHandler();

	private CreateTimeOutHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		logger.debug("Try to Send time out msg");
		ScheduleMessage scheduleMessage;
		if (processContext.getInputMessage().isScheduleMessage()) {
			scheduleMessage = (ScheduleMessage) processContext.getInputMessage();
			if (SchedulerConsts.TIME_OUT_MSG_TYPE.equals(scheduleMessage.getMessageType())
				|| SchedulerConsts.REVERSAL_TIME_OUT_MSG_TYPE.equals(scheduleMessage.getMessageType())) {

				Transaction refTranaction = scheduleMessage.getTransaction().getReferenceTransaction();

				if (refTranaction.getOutputMessage()!= null && refTranaction.getOutgoingIfx()!= null
					&& (IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(refTranaction.getOutgoingIfx().getIfxType())||
						IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(refTranaction.getOutgoingIfx().getIfxType()))){
					if(!ISOFinalMessageType.isTransferCheckAccountMessage(refTranaction.getReferenceTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())
							&& !ISOFinalMessageType.isTransferToacChechAccountMessage(refTranaction.getReferenceTransaction().getIncomingIfx().getIfxType()))
						refTranaction = refTranaction.getReferenceTransaction();
				}

				Transaction trans = processContext.getTransaction();
				Message message = refTranaction.getInputMessage();

				/***********/
//				NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(message.getId());
//				logger.info("removing removeResponseOnSameSocketConnectionById: " + message.getId());
				/***********/
				
				Ifx oldifx = message.getIfx();
//				if(TerminalType.ATM.equals(oldifx.getTerminalType()) &&
//						GlobalContext.getInstance().getMyInstitution().getBin().equals(oldifx.getBankId()) &&
//						!TerminalService.isOriginatorSwitchTerminal(message)){
//					logger.error("It is ATMTerminal and we are acquirer.... ignore ");
//					throw new Exception("It is ATMTerminal and we are acquirer.... ignore ");
//				}
				

				if (TransactionService.isMessageExpired(oldifx)) {
					throw new Exception("It is Expired message.... ignore ");
				}
				
				LifeCycle lifeCycle = refTranaction.getAndLockLifeCycle(LockMode.UPGRADE);
				if(lifeCycle.getIsFullyReveresed() != null && !SchedulerConsts.TIME_OUT_MSG_TYPE.equals(scheduleMessage.getMessageType())) { //Raza TIMEOUT case... need to verify
					throw new Exception("It is Revesed trx.... ignore ");
				}
				
				if (IfxType.ACQUIRER_REC_RQ.equals(oldifx.getIfxType())||
					IfxType.CARD_ISSUER_REC_RQ.equals(oldifx.getIfxType())||
					IfxType.CUTOVER_RQ.equals(oldifx.getIfxType())||
					IfxType.ACQUIRER_REC_RS.equals(oldifx.getIfxType())||
					IfxType.CARD_ISSUER_REC_RS.equals(oldifx.getIfxType())||
					IfxType.CUTOVER_RS.equals(oldifx.getIfxType())){
					logger.error("It is EOD Message.... ignore ");
					throw new Exception("It is EOD Message.... ignore ");
				}

				if (message != null) {
					Message outMsg = new Message(MessageType.OUTGOING);

					outMsg.setEndPointTerminal(scheduleMessage.getEndPointTerminal());
					outMsg.setTransaction(trans);
					Channel channel = message.getChannel();
					if (channel.getCommunicationMethod().equals(CommunicationMethod.ANOTHER_SOCKET)) {
                        channel = ((InputChannel) channel).getOriginatorChannel();
                    }
					outMsg.setChannel(channel);

					outMsg.setRequest(false);
					outMsg.setNeedToBeSent(true);
					outMsg.setNeedToBeInstantlyReversed(false);
					outMsg.setNeedResponse(false);

					Ifx newIfx = message.getIfx().copy();
					newIfx.setRsCode(scheduleMessage.getResponseCode());

			        newIfx.setIfxType(IfxType.getResponseIfxType(newIfx.getIfxType()));
//			        trans.setDebugTag("TIME_OUT_"+ newIfx.getIfxType());

//					Ifx ifx = createReversalComponent.createReversalIfx(newIfx);
//					outMsg.setIfx(ifx);
			        newIfx.setReceivedDt(outMsg.getStartDateTime());
					outMsg.setIfx(newIfx);
					trans.addOutputMessage(outMsg);
					TransactionService.updateLifeCycleStatusForNotSuccessful(trans, newIfx);
					TransactionService.updateMessageForNotSuccessful(newIfx, message.getTransaction());

					GeneralDao.Instance.saveOrUpdate(newIfx);
					GeneralDao.Instance.saveOrUpdate(outMsg);
			        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
					GeneralDao.Instance.saveOrUpdate(trans);


				}else{
					logger.debug("RefTransaction doesn't have inputMessage, so Time out message cannot be created!");
				}
			} else
				logger.debug("input message is not of applicable type (Time out_ScheduleMessage): "+ scheduleMessage.getMessageType());
		}
	}
}
