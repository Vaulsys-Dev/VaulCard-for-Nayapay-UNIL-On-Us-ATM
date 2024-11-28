package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.calendar.DateTime;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.exception.MessageAlreadyReversedException;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class SorushProcessor extends MessageProcessor {

	transient Logger logger = Logger.getLogger(SorushProcessor.class);

	public static final SorushProcessor Instance = new SorushProcessor();
	private SorushProcessor(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Message outgoingMessage = null;
		Ifx outgoingIfx = null;

		Ifx incomingIfx = incomingMessage.getIfx();
		if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType())) {
			
			Ifx refInIfx = transaction.getReferenceTransaction().getIncomingIfx();
			
			if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsFullyReveresed()) /*&&
					Boolean.TRUE.equals(transaction.getLifeCycle().getIsComplete())*/) {
				throw new MessageAlreadyReversedException("Message already reversed! Sorush msg ignored...");
			}
			
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			outgoingIfx = outgoingMessage.getIfx();
			
			
			
			outgoingIfx.setIfxType(IfxType.getReversalIfxType(refInIfx.getIfxType()));
			if (outgoingIfx.getIfxType() == null || IfxType.UNDEFINED.equals(outgoingIfx.getIfxType())) {
				if (IfxType.BAL_INQ_RQ.equals(refInIfx.getIfxType())) {
					outgoingIfx.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
				} else {
					logger.error("Exception is changing ifxType: " + refInIfx.getIfxType() + " to rev");
				}
			}
			outgoingIfx.setTrnType(IfxType.getTrnType(refInIfx.getIfxType()));
			outgoingIfx.getOriginalDataElements().setOrigDt(refInIfx.getOrigDt());
			outgoingIfx.setTerminalId(refInIfx.getTerminalId());
			

			if (IfxType.WITHDRAWAL_RQ.equals(refInIfx.getIfxType())) {
				Long amtRev = refInIfx.getAuth_Amt() - incomingIfx.getAuth_Amt();

				if (amtRev < 0)
					amtRev = 0L;

				outgoingIfx.setNew_AmtAcqCur(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, String.valueOf(amtRev), '0'));
			}

	        transaction.setOutgoingIfx(outgoingIfx);
		} else {
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			Ifx firstInIfx = transaction.getFirstTransaction().getIncomingIfx();
			outgoingIfx = outgoingMessage.getIfx();
			outgoingIfx.setIfxType(IfxType.SORUSH_REV_REPEAT_RS);
			outgoingIfx.setTrnType(TrnType.INCREMENTALTRANSFER);
			outgoingIfx.setTerminalId(firstInIfx.getTerminalId());
			
			String acctBalAvailableAmt = incomingIfx.getAcctBalAvailableAmt();
			if (!Util.hasText(acctBalAvailableAmt)) {
				outgoingIfx.setAcctBalAvailableAmt("C000000000000");
			}
			
			String acctBalAvailableCurCode = incomingIfx.getAcctBalAvailableCurCode();
			String curCode = ProcessContext.get().getRialCurrency().getCode().toString();
			
			if (!Util.hasText(acctBalAvailableCurCode)) {
				outgoingIfx.setAcctBalAvailableCurCode(curCode);
			}
			
			
			String acctBalLedgereAmt = incomingIfx.getAcctBalLedgerAmt();
			String acctBalLedgereCurCode = incomingIfx.getAcctBalLedgerCurCode();
			
			if (!Util.hasText(acctBalLedgereAmt)) {
				outgoingIfx.setAcctBalLedgerAmt("C000000000000");
			}
			
			if (!Util.hasText(acctBalLedgereCurCode)) {
				outgoingIfx.setAcctBalLedgerCurCode(curCode);
			}
			
			outgoingIfx.setRsCode(getErroreCodeMap(incomingIfx.getRsCode()));
			
			DateTime currentTime = DateTime.now();
			
			
			if (ISOResponseCodes.APPROVED.equals(outgoingIfx.getRsCode())) {
				SettlementInfo srcSettleInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, currentTime, transaction);			
				SettlementInfo destSettleInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, currentTime, transaction);
				
				ClearingInfo destClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);
				ClearingInfo sourceClearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, currentTime);
	
				putFlagOnTransaction(transaction, sourceClearingInfo, destClearingInfo, srcSettleInfo, destSettleInfo);
			}
			
			transaction.setDebugTag(outgoingIfx.getIfxType().toString());
		}

		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(transaction);

		return outgoingMessage;
	}

	private String getErroreCodeMap(String rsCode) {
		return rsCode;
	}

	private Message createMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws CloneNotSupportedException {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);

		transaction.addOutputMessage(outgoingMessage);
		outgoingMessage.setChannel(channel);

		logger.debug("Process Sorush_Transfer incoming message ");
		Ifx outgoingIfx = MsgProcessor.processor(incomingMessage.getIfx());

		outgoingIfx.setFwdBankId((channel.getInstitutionId() == null ? null : (FinancialEntityService.findEntity(
				Institution.class, channel.getInstitutionId())).getCode().toString()));//For dependent switch we should use getCode() instead of getBin()

		outgoingMessage.setIfx(outgoingIfx);

		setMessageFlag(outgoingMessage, incomingMessage.getRequest(), incomingMessage.getNeedResponse(),
				incomingMessage.getNeedToBeSent(), incomingMessage.getNeedToBeInstantlyReversed());

		Terminal endpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), true, processContext);
		outgoingMessage.setEndPointTerminal(endpointTerminal);
		addNecessaryDataToIfx(outgoingIfx, channel, endpointTerminal);
		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
//		super.messageValidation(ifx, incomingMessage);
	}
	
	private static void putFlagOnTransaction(Transaction transaction, ClearingInfo srcClearingInfo,
    		ClearingInfo destClearingInfo, SettlementInfo srcSettleInfo, SettlementInfo destSettleInfo) {
    	transaction.setSourceClearingInfo(srcClearingInfo);
    	transaction.setDestinationClearingInfo(destClearingInfo);
    	transaction.setSourceSettleInfo(srcSettleInfo);
    	transaction.setDestinationSettleInfo(destSettleInfo);
    	GeneralDao.Instance.saveOrUpdate(srcSettleInfo);
    	GeneralDao.Instance.saveOrUpdate(destSettleInfo);
    	GeneralDao.Instance.saveOrUpdate(srcClearingInfo);
    	GeneralDao.Instance.saveOrUpdate(destClearingInfo);
    	GeneralDao.Instance.saveOrUpdate(transaction);
    	TransactionService.copyFlagsToFirstTransaction(transaction);
    }
}
