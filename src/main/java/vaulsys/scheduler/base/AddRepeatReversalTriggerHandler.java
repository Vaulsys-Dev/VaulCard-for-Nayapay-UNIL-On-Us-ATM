package vaulsys.scheduler.base;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.ProcessContext;

import org.quartz.SchedulerException;

public class AddRepeatReversalTriggerHandler extends BaseHandler {
	//private final transient Logger logger = Logger.getLogger(AddRepeatReversalTriggerHandler.class);

	public static final AddRepeatReversalTriggerHandler Instance = new AddRepeatReversalTriggerHandler();

	private AddRepeatReversalTriggerHandler() {
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		try {

			addToScheduler(processContext);
			Transaction transaction = processContext.getTransaction();

			Message outputMessage = transaction.getOutputMessage();
			if (outputMessage == null)
				return;

			Ifx ifx = outputMessage.getIfx();
			if (ifx == null)
				return;

			//TODO this line must be checked much more carefully!
			if (!ISOFinalMessageType.isReversalRsMessage(ifx.getIfxType()) )
				TransactionService.updateLifeCycleStatusNormally(transaction, ifx);
		} catch (SchedulerException e) {
			throw e;
		}
	}

	public void addToScheduler(ProcessContext processContext) throws SchedulerException {
		Transaction transaction = processContext.getTransaction();
		Message outgoingMsg = transaction.getOutputMessage();

		if (outgoingMsg == null)
			return;

		Ifx ifx = outgoingMsg.getIfx();

		if(ifx == null)
			return;

		if (
			IfxType.ACQUIRER_REC_RQ.equals(ifx.getIfxType())
			|| IfxType.CARD_ISSUER_REC_RQ.equals(ifx.getIfxType())
			|| IfxType.CUTOVER_RQ.equals(ifx.getIfxType())) {

			if (ConfigUtil.getInteger(ConfigUtil.REVERSAL_COUNT) > 0 && 
				!outgoingMsg.getChannel().getMasterDependant()) {
				SchedulerService.createReversalJobInfo(transaction, null);
			}

		} else {
			try {

				if (outgoingMsg.getNeedResponse()) {

					SchedulerService.createReversalJobInfo(transaction, 0L);
				}

			} catch (Exception e) {
				throw new SchedulerException("Can't add reversal trigger:", e);
			}
		}
	}
}
