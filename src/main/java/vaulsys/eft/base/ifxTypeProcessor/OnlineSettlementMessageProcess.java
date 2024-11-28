package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementState;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ghasemkiani.util.icu.PersianDateFormat;

public class OnlineSettlementMessageProcess extends MessageProcessor {

	transient Logger logger = Logger.getLogger(OnlineSettlementMessageProcess.class);
	
	transient private static final PersianDateFormat pFormat = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static final OnlineSettlementMessageProcess Instance = new OnlineSettlementMessageProcess();
	private OnlineSettlementMessageProcess(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel/*=null*/, ProcessContext processContext) throws Exception {

		if(ISOFinalMessageType.isReversalMessage(incomingMessage.getIfx().getIfxType())){
			processReversalMesssage(transaction);
		} else{
			processNormalMesssage(transaction);
		}

		logger.info("Breaking down normal flow into own-schedule(Settlement)-answer handler: " +
         		"RSCode:" + transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode());

//         finalizeSelfEndedTransaction(transaction);
//         getTransactionService().putFlagOnOurReversalTransaction(transaction);
//         transaction.setEndDateTime(DateTime.now());
         logger.warn("ScheduleMessageFlowBreakDown");
         throw new ScheduleMessageFlowBreakDown();
	}

	private void processNormalMesssage(Transaction transaction) throws InvalidBusinessDateException {
		Message firstMsg = transaction.getFirstTransaction().getInputMessage();
		Ifx ifx = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
		
		if (!firstMsg.isScheduleMessage() || !ISOResponseCodes.APPROVED.equals(ifx.getRsCode()))
			return;
		
		SettlementData settlementData = transaction.getFirstTransaction().getSourceSettleInfo().getSettlementData();
		
		logger.debug("Try to get Lock of settlementData["+ settlementData.getId()+")");
		/*SettlementData */
		settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
		logger.debug("settlementData["+  settlementData.getId()+") has beeb locked and it's reloaded!");
		
		
		if (Util.hasText(settlementData.getDocumentNumber())){
			throw new InvalidBusinessDateException("Settlement Data "+ settlementData.getId()+" has been settled! "+ settlementData.getDocumentNumber());
		}
			
		settlementData.setDocumentNumber(settlementData.getClearingProfile().getName()+"- "+"Transaction:"+transaction.getId()+"-"
										+OnlineSettlementMessageProcess.pFormat.format(transaction.getBeginDateTime().toDate()));
		
		List<SettlementData> d = new ArrayList<SettlementData>();
		d.add(settlementData);
		AccountingService.updateSettlementInfo(d, SettledState.SETTLED);
		GeneralDao.Instance.saveOrUpdate(settlementData);
		
		DateTime now = DateTime.now();
		ClearingInfo clearingInfo = new ClearingInfo(ClearingState.NOT_CLEARED, now);
		transaction.setDestinationClearingInfo(clearingInfo);
		transaction.getFirstTransaction().setDestinationClearingInfo(clearingInfo);
		GeneralDao.Instance.saveOrUpdate(clearingInfo);
		GeneralDao.Instance.saveOrUpdate(transaction.getFirstTransaction());
		
		SettlementInfo settleInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, now, transaction);
		transaction.setDestinationSettleInfo(settleInfo);
		transaction.getFirstTransaction().setDestinationSettleInfo(settleInfo);
		settleInfo.setTransaction(transaction);
		GeneralDao.Instance.saveOrUpdate(settleInfo);
		GeneralDao.Instance.saveOrUpdate(transaction);
				
		TransactionService.putFlagOnOurSettlementTransaction(transaction);
	}
	
	private void processReversalMesssage(Transaction transaction) {
		Message firstMsg = transaction.getFirstTransaction().getInputMessage();
		Ifx firstOutIfx = transaction.getFirstTransaction().getOutgoingIfx()/*getOutputMessage()*/;
		Message refMsg = transaction.getReferenceTransaction().getInputMessage();
		
		if (!firstMsg.isScheduleMessage() && !refMsg.isScheduleMessage()
			&& !ISOResponseCodes.APPROVED.equals(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode()))
			return;
		
		SettlementData settlementData = transaction.getReferenceTransaction().getSourceSettleInfo().getSettlementData();
		
		String rsCode = firstOutIfx/*.getIfx()*/.getRsCode();
		
		if (!rsCode.equals(ISOResponseCodes.REFER_TO_ISSUER) && !rsCode.equals(ISOResponseCodes.INVALID_TO_ACCOUNT)) {
			settlementData.setDocumentNumber(null);
			GeneralDao.Instance.saveOrUpdate(settlementData);
			SettlementState settlementState = settlementData.getSettlementState();
			List<SettlementData> d = new ArrayList<SettlementData>();
			d.add(settlementData);
			AccountingService.updateSettlementInfo(d, SettledState.NOT_SETTLED);
			if(settlementState != null){
				settlementState.setState(null);
				settlementState.setSettlementDate(transaction.getBeginDateTime());
//				settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
				settlementState.setSettlingUser(ProcessContext.get().getSwitchUser());
				GeneralDao.Instance.saveOrUpdate(settlementState);
			}
		}
	}
	
	@Override
	public Message postProcess(Transaction transaction, Message incomingMessage, Message outgoingMessage, Channel channel)
			throws Exception {
		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
