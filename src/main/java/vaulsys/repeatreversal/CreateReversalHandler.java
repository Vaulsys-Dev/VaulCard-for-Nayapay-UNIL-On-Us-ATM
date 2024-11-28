package vaulsys.repeatreversal;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.routing.base.RoutDestination;
import vaulsys.routing.components.Routing;
import vaulsys.routing.exception.ChannelNotFoundException;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.StringFormat;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

import java.util.List;

public class CreateReversalHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(CreateReversalHandler.class);

	public static final CreateReversalHandler Instance = new CreateReversalHandler();

	private CreateReversalHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		logger.debug("Try to Send reversal msg");
		ScheduleMessage scheduleMessage;
		if (processContext.getInputMessage().isScheduleMessage()) {
			scheduleMessage = (ScheduleMessage) processContext.getInputMessage();
			//m.rehman: adding checks for reversal repear, loro reversal, loro reversal
			if (scheduleMessage.getMessageType().equals(SchedulerConsts.REVERSAL_MSG_TYPE)
					|| scheduleMessage.getMessageType().equals(SchedulerConsts.REVERSAL_REPEAT_MSG_TYPE)
					|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_MSG_TYPE)
					|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_REPEAT_MSG_TYPE)) {
		        //TODO: double check referenceTransaction or firstTransaction
				Transaction refTranaction = scheduleMessage.getTransaction().getReferenceTransaction();
				logger.debug("Try to Send reversal msg "+ refTranaction.getDebugTag()+"("+ refTranaction.getId()+")");

				Transaction trans = processContext.getTransaction();

				Message message = refTranaction.getOutputMessage();
				if (message != null) {
					Message outMsg = new Message(MessageType.OUTGOING);

					outMsg.setEndPointTerminal(scheduleMessage.getEndPointTerminal());
					outMsg.setTransaction(trans);

					//m.rehman: need to update current logic as system can send reversals to any entity
					/*
					Channel channel = message.getChannel();
					String channelName = message.getChannelName();
					//m.rehman: adding conditions for loro reversal as loro reversal should be send to cms
					if ((channel == null && channelName.toUpperCase().indexOf("CMS") >= 0)
							|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_MSG_TYPE)
							|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_REPEAT_MSG_TYPE)) {
						if(FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())){
							channel = ChannelManager.getInstance().getChannel(502229L, "out");
						}else{
							channel = ChannelManager.getInstance().getChannel(ProcessContext.get().getMyInstitution().getBin(), "out");
						}
					}
					
					outMsg.setChannel(channel);
					*/
					
					outMsg.setRequest(true);
					outMsg.setNeedToBeSent(true);
					outMsg.setNeedToBeInstantlyReversed(false);
					outMsg.setNeedResponse(false);

					Ifx	newIfx = message.getIfx().copy();

					newIfx.setRsCode(scheduleMessage.getResponseCode());
					//m.rehman: setting mti as reversal as it will be different in original message
					newIfx.setMti(ISOMessageTypes.getReversalRequestMTI(newIfx.getMti()));

					Ifx ifx = CreateReversalComponent.createReversalIfx(newIfx);
//					StringFormat format12 = new StringFormat(12, StringFormat.JUST_RIGHT);
					if (scheduleMessage.getAmount()!= null){
						ifx.setNew_AmtAcqCur(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, scheduleMessage.getAmount().toString(), '0') );
						ifx.setNew_AmtIssCur(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, scheduleMessage.getAmount().toString(), '0') );
						ifx.setReal_Amt(scheduleMessage.getAmount());
//						message.getIfx().setReal_Amt(scheduleMessage.getAmount());
//						getGeneralDao().saveOrUpdate(message.getIfx());
						/*Transaction responseTrx = getTransactionService().findResponseTrx(refTranaction.getLifeCycle(), refTranaction);
						if (responseTrx!=null){
							try {
								responseTrx.getInputMessage().getIfx().setReal_Amt(scheduleMessage.getAmount());
								getGeneralDao().saveOrUpdate(message.getIfx());
							} catch (Exception e) {
							}
						}*/

					}

					Ifx refIfx = refTranaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
					ifx.setReceivedDt(outMsg.getStartDateTime());
					ifx.setFirstTrxId(trans.getId());
					ifx.setTrnDt(outMsg.getStartDateTime());

					//m.rehman: setting trn type and ifx type for loro reversal
					TrnType loroTrnType;
					if (newIfx.getTrnType().equals(TrnType.WITHDRAWAL))
						loroTrnType = TrnType.WITHDRAWAL_LORO;
					else if (newIfx.getTrnType().equals(TrnType.PURCHASE))
						loroTrnType = TrnType.PURCHASE_LORO;
					else
						loroTrnType = newIfx.getTrnType();

					if (scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_MSG_TYPE)) {
						ifx.setMti(ISOMessageTypes.LORO_REVERSAL_ADVICE_87);
						ifx.setIfxType(IfxType.LORO_REVERSAL_REPEAT_RQ);
						ifx.setTrnType(loroTrnType);
					} else if (scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_REPEAT_MSG_TYPE)) {
						ifx.setMti(ISOMessageTypes.LORO_REVERSAL_ADVICE_REPEAT_87);
						ifx.setIfxType(IfxType.LORO_REVERSAL_REPEAT_RQ);
						ifx.setTrnType(loroTrnType);
					} else if (scheduleMessage.getMessageType().equals(SchedulerConsts.REVERSAL_REPEAT_MSG_TYPE)) {
						ifx.setMti(ISOMessageTypes.REVERSAL_ADVICE_REPEAT_87);
					}

					outMsg.setIfx(ifx);

					////////////////////////////////////////////////////////////////////////////
					//m.rehman: need to route according to the type of message
					//RoutingHandler.Instance.execute(processContext);
					//outMsg.setChannel((Channel)processContext.get().getOutputChannel("out"));
					Channel channel = message.getChannel();
					String channelName = message.getChannelName();
					if ((channel == null && channelName.toUpperCase().indexOf("CMS") >= 0)) {
						if(FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())){
							channel = ChannelManager.getInstance().getChannel("502229", "out");
						}else{
							channel = ChannelManager.getInstance().getChannel(ProcessContext.get().getMyInstitution().getBin().toString(), "out");
						}
					} else if (scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_MSG_TYPE)
							|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REVERSAL_REPEAT_MSG_TYPE)) {

						List<RoutDestination> destinations =
								Routing.getDestination(outMsg, ProcessContext.get().getRoutingTable("default"));
						channel = null;
						for (RoutDestination destination : destinations) {
							try {
								channel = ProcessContext.get().getChannel(destination.getChannelName());
							} catch (Exception e) {
								logger.error("RoutingComponent : Channel Not found. " + e);
								throw new ChannelNotFoundException("RoutingComponent : Channel Not found");
							}
						}
					} else {
						channel = processContext.getChannel(channelName);
					}
					outMsg.setChannel(channel);
					////////////////////////////////////////////////////////////////////////////

					trans.addOutputMessage(outMsg);

					TransactionService.updateLifeCycleStatusNormally(trans, ifx);
//					getCellChargeService().unlockCharge(newIfx, message.getTransaction());
					TransactionService.updateMessageForNotSuccessful(ifx, refIfx, trans);

					GeneralDao.Instance.saveOrUpdate(ifx);
					GeneralDao.Instance.saveOrUpdate(outMsg);
			        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
					GeneralDao.Instance.saveOrUpdate(trans);

				}else{
					logger.debug("RefTransaction doesn't have outputmessage, so reversal message cannot be created!");
				}
			} else
				logger.debug("input message is not of applicable type (Reversal_ScheduleMessage): "+ scheduleMessage.getMessageType());
		}
	}
}
