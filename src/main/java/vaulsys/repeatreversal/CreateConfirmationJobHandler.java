package vaulsys.repeatreversal;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;

import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.routing.components.RoutingHandler;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

public class CreateConfirmationJobHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(CreateConfirmationJobHandler.class);

	public static final CreateConfirmationJobHandler Instance = new CreateConfirmationJobHandler();

	private CreateConfirmationJobHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		logger.debug("Try to Send Advice msg");
		ScheduleMessage scheduleMessage;
		String key;
		if (processContext.getInputMessage().isScheduleMessage()) {
			scheduleMessage = (ScheduleMessage) processContext.getInputMessage();
			if (scheduleMessage.getMessageType().equals(SchedulerConsts.CONFIRMATION_TRX_TYP)
					|| scheduleMessage.getMessageType().equals(SchedulerConsts.ADVICE_MSG_TYPE)
					|| scheduleMessage.getMessageType().equals(SchedulerConsts.ADVICE_REPEAT_MSG_TYPE)
					|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_MSG_TYPE)
					|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REPEAT_MSG_TYPE)) {
		        //TODO: double check referenceTransaction or firstTransaction
				Transaction refTranaction = scheduleMessage.getTransaction().getReferenceTransaction();
				logger.debug("Try to Send advice msg "+ refTranaction.getDebugTag()+"("+ refTranaction.getId()+")");

				Transaction trans = processContext.getTransaction();

				Message message = refTranaction.getInputMessage();
				if (message != null) {
					Message outMsg = new Message(MessageType.OUTGOING);

					outMsg.setEndPointTerminal(scheduleMessage.getEndPointTerminal());
					outMsg.setTransaction(trans);

					//get destination channel
					/*Channel channel = message.getChannel();
					String channelName = message.getChannelName();
					if (channel == null && channelName.toUpperCase().indexOf("CMS") >= 0) {
						if(FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())){
							channel = ChannelManager.getInstance().getChannel(502229L, "out");
						}else{
							channel = ChannelManager.getInstance().getChannel(ProcessContext.get().getMyInstitution().getBin(), "out");
						}
					}

					outMsg.setChannel(channel);*/

					/*outMsg.setChannel(
							ChannelManager.getInstance().getChannel(
									ProcessContext.get().getMyInstitution().getBin(), "out"
							)
					);*/

					outMsg.setRequest(true);
					outMsg.setNeedToBeSent(true);
					outMsg.setNeedToBeInstantlyReversed(false);
					outMsg.setNeedResponse(false);

					Ifx messageIfx = message.getIfx();
					Ifx	newIfx = messageIfx.copy();
					newIfx.setIfxDirection(IfxDirection.SELF_GENERATED);
					newIfx.setReceivedDt(outMsg.getStartDateTime());
					newIfx.setFirstTrxId(trans.getId());

					TrnType loroTrnType;
					if (newIfx.getTrnType().equals(TrnType.WITHDRAWAL))
						loroTrnType = TrnType.WITHDRAWAL_LORO;
					else if (newIfx.getTrnType().equals(TrnType.PURCHASE))
						loroTrnType = TrnType.WITHDRAWAL_LORO;
					else
						loroTrnType = newIfx.getTrnType();

					if (scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_MSG_TYPE)) {
						newIfx.setMti(ISOMessageTypes.LORO_ADVICE_87);
						newIfx.setIfxType(IfxType.LORO_ADVICE_RQ);
						newIfx.setTrnType(loroTrnType);
					} else if (scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REPEAT_MSG_TYPE)) {
						newIfx.setMti(ISOMessageTypes.LORO_ADVICE_REPEAT_87);
						newIfx.setIfxType(IfxType.LORO_ADVICE_RQ);
						newIfx.setTrnType(loroTrnType);
					} else if (scheduleMessage.getMessageType().equals(SchedulerConsts.ADVICE_REPEAT_MSG_TYPE)) {
						newIfx.setMti(ISOMessageTypes.FINANCIAL_ADVICE_REPEAT_87);
					}

					outMsg.setIfx(newIfx);



					////////////////////////////////////////////////////////////////////////////
					processContext.getInputMessage().setIfx(newIfx);
					processContext.getInputMessage().setChannel(ChannelManager.getInstance().getChannel(refTranaction.getInputMessage().getChannelName()));
					RoutingHandler.Instance.execute(processContext);
					outMsg.setChannel((Channel)processContext.get().getOutputChannel("out"));

					/*
					Message refMessage;
					if (scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_MSG_TYPE)
							|| scheduleMessage.getMessageType().equals(SchedulerConsts.LORO_REPEAT_MSG_TYPE))
						refMessage = outMsg;
					else
						refMessage = message;

					List<RoutDestination> destinations =
							Routing.getDestination(refMessage, ProcessContext.get().getRoutingTable("default"));

					Channel channel = null;
					for (RoutDestination destination : destinations) {
						try {
							channel = ProcessContext.get().getChannel(destination.getChannelName());
						} catch (Exception e) {
							logger.error("RoutingComponent : Channel Not found. "+ e);
							throw new ChannelNotFoundException("RoutingComponent : Channel Not found");
						}
					}
					outMsg.setChannel(channel);
					*/
					////////////////////////////////////////////////////////////////////////////

					//if (NetworkManager.GetCommsStatus(outMsg.getChannel()) != NetworkInfoStatus.SOCKET_CONNECTED)
					if (outMsg.getChannel().getConnectionStatus() != NetworkInfoStatus.SOCKET_CONNECTED)
							//|| NetworkManager.GetProcStatus(outMsg.getChannel()) != NetworkInfoStatus.PROCESSING_ENABLED)
						newIfx.setRsCode(ISOResponseCodes.ISSUER_REVERSAL);
					else
						newIfx.setRsCode(ISOResponseCodes.APPROVED);

					GeneralDao.Instance.saveOrUpdate(newIfx);
					GeneralDao.Instance.saveOrUpdate(outMsg);
					GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
					GeneralDao.Instance.saveOrUpdate(trans);

					trans.addOutputMessage(outMsg);

					TransactionService.updateLifeCycleStatusNormally(trans, newIfx);

					/*
					GeneralDao.Instance.saveOrUpdate(newIfx);
					GeneralDao.Instance.saveOrUpdate(outMsg);
			        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
					GeneralDao.Instance.saveOrUpdate(trans);
					*/
				}else{
					logger.debug("RefTransaction doesn't have outputmessage, so advice message cannot be created!");
				}
			} else
				logger.debug("input message is not of applicable type (Advice ScheduleMessage): "+ scheduleMessage.getMessageType());
		}
	}
}
