package vaulsys.eft.base.ifxTypeProcessor;

import java.util.List;

import vaulsys.calendar.DateTime;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.migration.MigrationData;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class TransferReversalProcessor extends MessageProcessor {
	transient Logger logger = Logger.getLogger(TransferReversalProcessor.class);

	public static final TransferReversalProcessor Instance = new TransferReversalProcessor();
	private TransferReversalProcessor(){};


	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Ifx incomingIfx = incomingMessage.getIfx();
		int seqCntrLength = incomingIfx.getSrc_TrnSeqCntr().length();

		String primaryInstitution = incomingIfx.getDestBankId();
		String secondaryInstitution = incomingIfx.getRecvBankId();
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);
		Ifx outIfx = MsgProcessor.processor(incomingIfx);
		outgoingMessage.setIfx(outIfx);

		if (ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType()) && ISOResponseCodes.cannotBeDone(incomingIfx.getRsCode())) {
			throw new ScheduleMessageFlowBreakDown();
		} else {
			DateTime currentTime = DateTime.now();
			if (ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType())
					&& (IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS.equals(incomingIfx.getIfxType())||
							IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS.equals(incomingIfx.getIfxType()))
					&& !ISOResponseCodes.isReversalMessageDone(incomingIfx.getRsCode())) {

				Transaction refTrx = transaction.getFirstTransaction();

				if (refTrx == null
						|| refTrx.getInputMessage().isScheduleMessage()
						|| (refTrx != null && !ISOFinalMessageType.isReversalRqMessage(refTrx.getIncomingIfx()/*getInputMessage().getIfx()*/
								.getIfxType()))) {

//					transaction.setEndDateTime(currentTime);
					throw new ScheduleMessageFlowBreakDown();
				}

				/********************/
				String primaryAccount = incomingIfx.getAppPAN();
				String secondaryAccount = incomingIfx.getSecondAppPan();
				
//				secondaryInstitution = Long.parseLong(secondaryAccount.substring(0, 6));
//				primaryInstitution = Long.parseLong(primaryAccount.substring(0, 6));

				MigrationData migData = incomingIfx.getMigrationData();
				MigrationData secMigData = incomingIfx.getMigrationSecondData();
				
				outIfx.setMigrationData(secMigData);
				outIfx.setMigrationSecondData(migData);
				
				outIfx.setAppPAN(secondaryAccount);
                if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                    incomingIfx.setIfxEncAppPAN(secondaryAccount);
                outIfx.setActualAppPAN(incomingIfx.getActualSecondAppPan());
				
				outIfx.setSecondAppPan(primaryAccount);
				outIfx.setActualSecondAppPAN(incomingIfx.getActualAppPAN());
				
				outIfx.setDestBankId(secondaryInstitution);
				outIfx.setRecvBankId(primaryInstitution);
				/********************/
				
				if (TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(incomingIfx.getTrnType())){
					outIfx.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
					outIfx.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS);
				}else{
					outIfx.setIfxType(IfxType.TRANSFER_REV_REPEAT_RS);
					outIfx.setTrnType(TrnType.TRANSFER);
				}


				Message originatorIncomingMessage = refTrx.getInputMessage();

				channel = ((InputChannel) originatorIncomingMessage.getChannel()).getOriginatorChannel();
				String nextSeq = originatorIncomingMessage.getIfx().getSrc_TrnSeqCntr();
				outIfx.setSrc_TrnSeqCntr(nextSeq);
				outIfx.setMy_TrnSeqCntr(nextSeq);
                if(outIfx.getNetworkTrnInfo() != null && incomingIfx.getNetworkTrnInfo() != null && outIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId().equals(outIfx.getNetworkTrnInfo().getId()))
                    incomingIfx.setIfxSrcTrnSeqCntr(nextSeq);
				setMessageFlag(outgoingMessage, false, false, true, false);
				
				/*// Note: in the previous phase, the trigger is deleted
				transaction.setEndDateTime(currentTime);

				throw new ScheduleMessageFlowBreakDown();
*/
			} else {
				Long masterCode = FinancialEntityService.getMasterInstitution().getCode();
				
				
				if (IfxType.TRANSFER_REV_REPEAT_RQ.equals(incomingIfx.getIfxType())||
						IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(incomingIfx.getIfxType())) {
//				|| IfxType.TRANSFER_REV_RQ.equals(incomingIfx.getIfxType())

					/****************/
					String primaryAccount = incomingIfx.getAppPAN();
					String actualAppPAN = incomingIfx.getActualAppPAN();
					String secondaryAccount = incomingIfx.getSecondAppPan();
					String actualSecAppPAN = incomingIfx.getActualSecondAppPan();

					outIfx.setAppPAN(secondaryAccount);
                    if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                        incomingIfx.setIfxEncAppPAN(secondaryAccount);
                    outIfx.setActualAppPAN(actualSecAppPAN);
					
					outIfx.setSecondAppPan(primaryAccount);
					outIfx.setActualSecondAppPAN(actualAppPAN);
					
					secondaryInstitution = secondaryAccount.substring(0, 6);
					primaryInstitution = primaryAccount.substring(0, 6);
					
					MigrationData migData = incomingIfx.getMigrationData();
					MigrationData secMigData = incomingIfx.getMigrationSecondData();
					
					outIfx.setMigrationData(secMigData);
					outIfx.setMigrationSecondData(migData);
					
					outIfx.setDestBankId(secondaryInstitution);
					outIfx.setRecvBankId(primaryInstitution);
					/**************/
					
					if (secMigData != null) {
						secondaryAccount = secMigData.getFanapAppPan();
					}
					if (migData != null) {
						primaryAccount = migData.getFanapAppPan();
					}
					
					outIfx.setAppPAN(secondaryAccount);
                    if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                        incomingIfx.setIfxEncAppPAN(secondaryAccount);
                    outIfx.setActualAppPAN(actualSecAppPAN);
					if (!secondaryAccount.equals(actualSecAppPAN)) {
						if (secondaryAccount.startsWith("502229")) {
							outIfx.setAppPAN(secondaryAccount);
                            if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                                incomingIfx.setIfxEncAppPAN(secondaryAccount);
                            outIfx.setActualAppPAN(actualSecAppPAN);
							secondaryInstitution = secondaryAccount.substring(0, 6);
							outIfx.setDestBankId(secondaryInstitution);
						}
					}
					/**************/
					outIfx.setSecondAppPan(primaryAccount);
					outIfx.setActualSecondAppPAN(actualAppPAN);
					if (!primaryAccount.equals(actualAppPAN)) {
						if (primaryAccount.startsWith("502229")) {
							outIfx.setSecondAppPan(actualAppPAN);
							outIfx.setActualSecondAppPAN(actualAppPAN);
							primaryInstitution = actualAppPAN.substring(0, 6);
						}
					}
					/**************/
					/****************/
					if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(incomingIfx.getTrnType())){
						outIfx.setTrnType(TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT);
						outIfx.setIfxType(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ);
					}else{
						outIfx.setIfxType(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
						outIfx.setTrnType(TrnType.INCREMENTALTRANSFER);
					}



					channel = ChannelManager.getInstance().getChannel(secondaryInstitution, "out");
					if (channel == null)
						channel = ChannelManager.getInstance().getChannel(masterCode.toString(), "out");
					

					String nextSeq = Util.generateTrnSeqCntr(seqCntrLength);
					outIfx.setSrc_TrnSeqCntr(nextSeq);
					outIfx.setMy_TrnSeqCntr(nextSeq);
                    if(outIfx.getNetworkTrnInfo() != null && incomingIfx.getNetworkTrnInfo() != null && outIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId().equals(outIfx.getNetworkTrnInfo().getId()))
                        incomingIfx.setIfxSrcTrnSeqCntr(nextSeq);

					Transaction referenceTransaction = TransactionService.findcorrespondingResponse(transaction.getReferenceTransaction());
					Ifx referenceTransactionIfx = referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;

					outIfx.setOriginalDataElements(new MessageReferenceData());
					outIfx.getSafeOriginalDataElements().setTrnSeqCounter(referenceTransactionIfx.getSrc_TrnSeqCntr());
					outIfx.getSafeOriginalDataElements().setOrigDt(referenceTransactionIfx.getOrigDt());
					outIfx.getSafeOriginalDataElements().setBankId(referenceTransactionIfx.getBankId());
					outIfx.getSafeOriginalDataElements().setFwdBankId(referenceTransactionIfx.getDestBankId());
					outIfx.getSafeOriginalDataElements().setAppPAN(secondaryAccount);
					outIfx.getSafeOriginalDataElements().setTerminalId(incomingIfx.getTerminalId());
					

					setMessageFlag(outgoingMessage, true, false, true, false);
					SchedulerService.createReversalJobInfo(referenceTransaction, 0L);
					transaction.setReferenceTransaction(referenceTransaction);

				} else if (IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS.equals(incomingIfx.getIfxType())){
//					|| IfxType.TRANSFER_FROM_ACCOUNT_REV_RS.equals(incomingIfx.getIfxType())) {

					Transaction refTrx = transaction.getFirstTransaction();
					refTrx = (refTrx != null) ? refTrx.getFirstTransaction() : null;

					if (refTrx == null
							|| refTrx.getInputMessage().isScheduleMessage()
							|| (refTrx != null && !ISOFinalMessageType.isReversalRqMessage(refTrx.getIncomingIfx()/*getInputMessage().getIfx()*/
									.getIfxType()))) {

//						transaction.setEndDateTime(currentTime);
						TransactionService.putFlagOnOurReversalTransaction(transaction, null, null);
						throw new ScheduleMessageFlowBreakDown();
					}
//					if (TrnType.DECREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(incomingIfx.getTrnType())){
//						outIfx.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
//						
//					}
					if(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(incomingIfx.getTransaction().getIncomingIfx().getTrnType())){
						outIfx.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS);
						outIfx.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
					}
					else{
						outIfx.setIfxType(IfxType.TRANSFER_REV_REPEAT_RS);
						outIfx.setTrnType(TrnType.TRANSFER);
					}


					Message originatorIncomingMessage = refTrx.getInputMessage();

					channel = ((InputChannel) originatorIncomingMessage.getChannel()).getOriginatorChannel();
					String nextSeq = originatorIncomingMessage.getIfx().getSrc_TrnSeqCntr();
					outIfx.setSrc_TrnSeqCntr(nextSeq);
                    if(outIfx.getNetworkTrnInfo() != null && incomingIfx.getNetworkTrnInfo() != null && outIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId().equals(outIfx.getNetworkTrnInfo().getId()))
                        incomingIfx.setIfxSrcTrnSeqCntr(nextSeq);
					outIfx.setMy_TrnSeqCntr(nextSeq);
					setMessageFlag(outgoingMessage, false, false, true, false);
					TransactionService.putFlagOnOurReversalTransaction(transaction, false, null);

				} else if (IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS.equals(incomingIfx.getIfxType())||
						IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS.equals(incomingIfx.getIfxType())){
//					|| IfxType.TRANSFER_TO_ACCOUNT_REV_RS.equals(incomingIfx.getIfxType())) {
					Transaction refTransaction = transaction.getReferenceTransaction().getFirstTransaction();
					transaction.setReferenceTransaction(refTransaction);
					Ifx referenceTransactionIfx = refTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/;

					/**************/
					String primaryAccount = incomingIfx.getAppPAN();
					String actualAppPAN = incomingIfx.getActualAppPAN();
					String secondaryAccount = incomingIfx.getSecondAppPan();
					String actualSecAppPAN = incomingIfx.getActualSecondAppPan();

					MigrationData migData = incomingIfx.getMigrationData();
					MigrationData secMigData = incomingIfx.getMigrationSecondData();
					
					outIfx.setMigrationData(secMigData);
					outIfx.setMigrationSecondData(migData);
					
					secondaryInstitution = secondaryAccount.substring(0, 6);
					if(!incomingIfx.getIfxType().equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS))
						primaryInstitution = primaryAccount.substring(0, 6);

					outIfx.setDestBankId(secondaryInstitution);
					outIfx.setRecvBankId(primaryInstitution);
					/**************/
					outIfx.setAppPAN(secondaryAccount);
                    if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                        incomingIfx.setIfxEncAppPAN(secondaryAccount);
                    outIfx.setActualAppPAN(actualSecAppPAN);
					
					if (secMigData != null) {
						secondaryAccount = secMigData.getFanapAppPan();
					}
					if (migData != null) {
						primaryAccount = migData.getFanapAppPan();
					}
					if (!secondaryAccount.equals(actualSecAppPAN)) {
						if (secondaryAccount.startsWith("502229")) {
							outIfx.setAppPAN(secondaryAccount);
                            if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                                incomingIfx.setIfxEncAppPAN(secondaryAccount);
                            outIfx.setActualAppPAN(actualSecAppPAN);
							secondaryInstitution = secondaryAccount.substring(0, 6);
							outIfx.setDestBankId(secondaryInstitution);
						}
					}
					/**************/
					outIfx.setSecondAppPan(primaryAccount);
					outIfx.setActualSecondAppPAN(actualAppPAN);
					if (!primaryAccount.equals(actualAppPAN)) {
						if (primaryAccount.startsWith("502229")) {
							outIfx.setSecondAppPan(actualAppPAN);
							outIfx.setActualSecondAppPAN(actualAppPAN);
							primaryInstitution = actualAppPAN.substring(0, 6);
						}
					}
					/**************/
					
					
					
//					String primaryAccount = incomingIfx.getSecondAppPan();
//					String secondaryAccount = incomingIfx.getAppPAN();

//					secondaryInstitution = Long.parseLong(primaryAccount.substring(0, 6));
//					primaryInstitution = Long.parseLong(secondaryAccount.substring(0, 6));
					
//					outIfx.setDestBankId(secondaryInstitution);
//					outIfx.setRecvBankId(primaryInstitution);
					
//					outIfx.setAppPAN(primaryAccount);
//					outIfx.setActualAppPAN(incomingIfx.getActualSecondAppPan());
					
//					outIfx.setSecondAppPan(secondaryAccount);
//					outIfx.setActualSecondAppPAN(incomingIfx.getActualAppPAN());

					outIfx.setOriginalDataElements(new MessageReferenceData());
					outIfx.getSafeOriginalDataElements().setTrnSeqCounter(referenceTransactionIfx.getSrc_TrnSeqCntr());
					outIfx.getSafeOriginalDataElements().setOrigDt(referenceTransactionIfx.getOrigDt());
					outIfx.getSafeOriginalDataElements().setBankId(referenceTransactionIfx.getBankId());
					outIfx.getSafeOriginalDataElements().setFwdBankId(referenceTransactionIfx.getDestBankId());
					outIfx.getSafeOriginalDataElements().setAppPAN(/*primaryAccount*/secondaryAccount);
					outIfx.getSafeOriginalDataElements().setTerminalId(incomingIfx.getTerminalId());

					outIfx.setIfxType(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ);
					outIfx.setTrnType(TrnType.DECREMENTALTRANSFER);


					channel = ChannelManager.getInstance().getChannel(secondaryInstitution, "out");
					
					if (channel == null)
						channel = ChannelManager.getInstance().getChannel(masterCode.toString(), "out");
					

					String nextSeq = Util.generateTrnSeqCntr(seqCntrLength);
					outIfx.setSrc_TrnSeqCntr(nextSeq);
					outIfx.setMy_TrnSeqCntr(nextSeq);
                    if(outIfx.getNetworkTrnInfo() != null && incomingIfx.getNetworkTrnInfo() != null && outIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId().equals(outIfx.getNetworkTrnInfo().getId()))
                        incomingIfx.setIfxSrcTrnSeqCntr(nextSeq);
					setMessageFlag(outgoingMessage, true, false, true, false);
					SchedulerService.createReversalJobInfo(refTransaction, 0L);
				}
			}
		}
		// }
		outgoingMessage.setChannel(channel);

		transaction.addOutputMessage(outgoingMessage);
		Terminal findEndpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), false, processContext);
		
		/**** for migration transfer *****/
		if ((IfxType.TRANSFER_RS.equals(outIfx.getIfxType())||IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(outIfx.getIfxType())) &&
				(outIfx.getMigrationData() != null || outIfx.getMigrationSecondData() != null)) {
//			if (GlobalContext.getInstance().getMyInstitution().getBin().equals(outgoingMessage.getIfx().getBankId())) {
			if (ProcessContext.get().getMyInstitution().getBin().equals(outgoingMessage.getIfx().getBankId())) {
				findEndpointTerminal = (Terminal) GeneralDao.Instance.load(channel.getEndPointType().getClassType(), outIfx.getTerminalId());
			}
		}
		
		outgoingMessage.setEndPointTerminal(findEndpointTerminal);

		addNecessaryDataToIfx(outIfx, channel, findEndpointTerminal);

		transaction.setDebugTag(outIfx.getIfxType().toString());
		GeneralDao.Instance.saveOrUpdate(outIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(transaction);

		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
//		super.messageValidation(ifx, incomingMessage); //***coz of transfer to account***//
	}

}
