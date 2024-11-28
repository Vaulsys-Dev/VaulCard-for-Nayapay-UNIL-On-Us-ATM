package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.onlineBillPayment.DuplicateOnlineBillPaymentRefNumException;
import vaulsys.authorization.exception.onlineBillPayment.ExpireDateException;
import vaulsys.authorization.exception.onlineBillPayment.NotValidOnlineBillPaymentMessageException;
import vaulsys.authorization.exception.onlineBillPayment.onlineBillPaymentIsInTheProcessException;
import vaulsys.authorization.exception.topup.UnsupportedTransation;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.modernpayment.onlinebillpayment.OnlineBillPayment;
import vaulsys.modernpayment.onlinebillpayment.OnlineBillPaymentStatus;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;

public class OnlineBillPaymentProcessor extends MessageProcessor{
	
	transient Logger logger = Logger.getLogger(GeneralMessageProcessor.class);

	public static final OnlineBillPaymentProcessor Instance = new OnlineBillPaymentProcessor();
	private OnlineBillPaymentProcessor(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Message outgoingMessage = null;
		Ifx outgoingIfx = null;
		OnlineBillPayment onlineBillPayment = null;
		Ifx incomingIfx = incomingMessage.getIfx();
		
		if(IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(incomingIfx.getIfxType())||
				IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(incomingIfx.getIfxType())){
			String refNum=incomingIfx.getOnlineBillPaymentRefNum();
			Map<String,Object> param=new HashMap<String, Object>();			
			param.put("refnum", refNum);
			String st="from OnlineBillPayment onbill where onbill.refNum = :refnum";
			onlineBillPayment = (OnlineBillPayment)GeneralDao.Instance.findUniqueObject(st,param);
			outgoingMessage = new Message(MessageType.OUTGOING);
			outgoingIfx = createOutgoingIfx(incomingMessage.getIfx(), incomingIfx);
			outgoingIfx.setOnlineBillPaymentDescription(onlineBillPayment.getDescription());
			outgoingMessage.setTransaction(transaction);

			transaction.addOutputMessage(outgoingMessage);
			outgoingMessage.setChannel(channel);

			outgoingIfx.setFwdBankId((channel.getInstitutionId() == null ? null : (FinancialEntityService.findEntity(
					Institution.class, channel.getInstitutionId())).getBin().toString()));
			

			setMessageFlag(outgoingMessage, incomingMessage.getRequest(), incomingMessage.getNeedResponse(),
					incomingMessage.getNeedToBeSent(), false);
			incomingIfx.getOnlineBillPaymentData().setOnlineBillPayment(onlineBillPayment);
			incomingIfx.getOnlineBillPaymentData().setPreviousPaymentStatus(onlineBillPayment.getPaymentStatus());
			
			outgoingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setLifeCycle(transaction.getLifeCycle());
			outgoingMessage.setIfx(outgoingIfx);
			
			outgoingIfx.setOnlineBillPaymentCompanyCode(Long.parseLong(onlineBillPayment.getEntity().getCompanyCode().toString()));
			
			ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(onlineBillPayment.getEntity());
			outgoingIfx.setThirdPartyTerminalId(thPVT.getCode());
			incomingIfx.setThirdPartyTerminalId(thPVT.getCode());
			
			Terminal endpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), true, processContext);
			outgoingMessage.setEndPointTerminal(endpointTerminal);
//			addNecessaryDataToIfx(outgoingIfx, channel, endpointTerminal);
			
		}else if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType())) {
			if(ISOFinalMessageType.isReversalOrRepeatMessage(incomingIfx.getIfxType())) {
				logger.error("Reversal messages are not supported for online billpayment transactions...");
				throw new UnsupportedTransation();
			}
			String refNum=incomingIfx.getOnlineBillPaymentRefNum();
			Map<String,Object> param=new HashMap<String, Object>();			
			param.put("refnum", refNum);
			String st="from OnlineBillPayment onbill where onbill.refNum = :refnum";
			onlineBillPayment = (OnlineBillPayment)GeneralDao.Instance.findUniqueObject(st,param);
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			outgoingIfx = outgoingMessage.getIfx();
			outgoingIfx.setOnlineBillPaymentDescription(onlineBillPayment.getDescription());
			if (TerminalType.ATM.equals(incomingIfx.getTerminalType()) ||
					TerminalType.PINPAD.equals(incomingIfx.getTerminalType()) ||
					TerminalType.VRU.equals(incomingIfx.getTerminalType())) {
				outgoingIfx.setIfxType(IfxType.BILL_PMT_RQ);
				outgoingIfx.setTrnType(TrnType.BILLPAYMENT);
				outgoingIfx.setBillOrgType(OrganizationType.UNDEFINED);
//				outgoingIfx.setSec_Amt(incomingIfx.getAuth_Amt());
				outgoingIfx.setAuth_Amt(incomingIfx.getSec_Amt());
				
			} else {
				outgoingIfx.setIfxType(IfxType.PURCHASE_RQ);
				outgoingIfx.setTrnType(TrnType.PURCHASE);
			}
			incomingIfx.getOnlineBillPaymentData().setOnlineBillPayment(onlineBillPayment);
			incomingIfx.getOnlineBillPaymentData().setPreviousPaymentStatus(OnlineBillPaymentStatus.NOT_PAID);
			incomingIfx.getOnlineBillPaymentData().setNextPaymentStatus(OnlineBillPaymentStatus.IN_THE_PROCESS);
			onlineBillPayment.setPaymentStatus(OnlineBillPaymentStatus.IN_THE_PROCESS);
			onlineBillPayment.setChangePaymentStatusTime(DateTime.now().getDateTimeLong());
			outgoingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setLifeCycle(transaction.getLifeCycle());
//			logger.error("previous status: "+onlineBillPayment.getPreviousPaymentStatus()+"next status: "+onlineBillPayment.getNextPaymentStatus());
//			logger.error("previous status: " +incomingIfx.getOnlineBillPaymentData().getPreviousPaymentStatus()+" next state: "+incomingIfx.getOnlineBillPaymentData().getNextPaymentStatus());
				
			outgoingIfx.setOnlineBillPaymentCompanyCode(Long.parseLong(onlineBillPayment.getEntity().getCompanyCode().toString()));
			
			ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(onlineBillPayment.getEntity());
			outgoingIfx.setThirdPartyTerminalId(thPVT.getCode());
			incomingIfx.setThirdPartyTerminalId(thPVT.getCode());
			
			GeneralDao.Instance.saveOrUpdate(incomingIfx);
		} else {
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			outgoingIfx = outgoingMessage.getIfx();
			if(ISOFinalMessageType.isTrackingMessage(incomingIfx.getIfxType())){
				outgoingIfx.setIfxType(IfxType.ONLINE_BILLPAYMENT_TRACKING);
			}else{
				outgoingIfx.setIfxType(IfxType.ONLINE_BILLPAYMENT_RS);
			
			}
			outgoingIfx.setTrnType(TrnType.ONLINE_BILLPAYMENT);
			outgoingIfx.setOnlineBillPaymentDescription(incomingIfx.getOnlineBillPaymentDescription());
			outgoingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setLifeCycle(transaction.getLifeCycle());
			transaction.setDebugTag(outgoingIfx.getIfxType().toString());
			if (ISOResponseCodes.isSuccess(outgoingIfx.getRsCode())) {
				//inja bayad paid satus pardakht shode beshavad
				incomingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setPaymentStatus(OnlineBillPaymentStatus.PAID);
				incomingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setAppPAN(incomingIfx.getActualAppPAN());
				incomingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setTrnSeqCntr(incomingIfx.getSrc_TrnSeqCntr());
				incomingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setChangePaymentStatusTime(DateTime.now().getDateTimeLong());
				incomingIfx.getOnlineBillPaymentData().setPreviousPaymentStatus(OnlineBillPaymentStatus.IN_THE_PROCESS);
				incomingIfx.getOnlineBillPaymentData().setNextPaymentStatus(OnlineBillPaymentStatus.PAID);
//				logger.error("previous status: "+incomingIfx.getOnlineBillPaymentData().getPreviousPaymentStatus()+
//						"next status: "+incomingIfx.getOnlineBillPaymentData().getNextPaymentStatus());
			}else{
				incomingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setPaymentStatus(OnlineBillPaymentStatus.NOT_PAID);
				incomingIfx.getOnlineBillPaymentData().getOnlineBillPayment().setChangePaymentStatusTime(DateTime.now().getDateTimeLong());
				incomingIfx.getOnlineBillPaymentData().setNextPaymentStatus(OnlineBillPaymentStatus.NOT_PAID);
//				logger.error("previous status: "+incomingIfx.getOnlineBillPaymentData().getPreviousPaymentStatus()+
//						"next status: "+incomingIfx.getOnlineBillPaymentData().getNextPaymentStatus());
			}				
		}
		
		

		GeneralDao.Instance.saveOrUpdate(incomingIfx.getOnlineBillPaymentData().getOnlineBillPayment());
		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        GeneralDao.Instance.saveOrUpdate(transaction);

		return outgoingMessage;
	}

	private Message createMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws CloneNotSupportedException {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);

		transaction.addOutputMessage(outgoingMessage);
		outgoingMessage.setChannel(channel);

		logger.debug("Process online billpayment incoming message ");
		Ifx outgoingIfx = MsgProcessor.processor(incomingMessage.getIfx());

		outgoingIfx.setFwdBankId((channel.getInstitutionId() == null ? null : (FinancialEntityService.findEntity(
				Institution.class, channel.getInstitutionId())).getBin().toString()));

		outgoingMessage.setIfx(outgoingIfx);

		setMessageFlag(outgoingMessage, incomingMessage.getRequest(), incomingMessage.getNeedResponse(),
				incomingMessage.getNeedToBeSent(), false);

		Terminal endpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), true, processContext);
		outgoingMessage.setEndPointTerminal(endpointTerminal);
		addNecessaryDataToIfx(outgoingIfx, channel, endpointTerminal);
		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {//in jaie seda zade nemishe
		if(!Util.hasText(ifx.getOnlineBillPaymentRefNum())){
			throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has empty RefNum");
		}
//		if(!Util.hasText(ifx.getBillID())){
//			throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has empty RefNum");
//		}
		if (!isValidOnlineBillPaymentMessage(ifx))
			throw new NotValidOnlineBillPaymentMessageException("there is not such a refNum: "+ifx.getOnlineBillPaymentRefNum());
	}
	
	private boolean isValidOnlineBillPaymentMessage(Ifx ifx)throws Exception {
		String refNum = ifx.getOnlineBillPaymentRefNum();
		String st="from OnlineBillPayment onbill where onbill.refNum = :refnum";//farz kardam ke tekrari nadarim va refnum uniq ast
		Map<String,Object>param = new HashMap<String, Object>();
		param.put("refnum", refNum);
	    OnlineBillPayment onbill=(OnlineBillPayment)GeneralDao.Instance.findUniqueObject(st,param);
	    if(onbill == null){
	    	logger.error("there isn't such a refrence number! ");
	    	throw new NotValidOnlineBillPaymentMessageException("there isn't such a refrence number! ");
	    }
	   
		try {
			logger.debug("Try to get Lock of OnlineBillPayment["+ onbill.getId()+"]");
		    onbill = (OnlineBillPayment)GeneralDao.Instance.synchObject(onbill, LockMode.UPGRADE_NOWAIT);
			logger.debug("OnlineBillPayment["+onbill.getId()+"] has beeb locked and it's reloaded!");
		} catch (LockAcquisitionException e) {
    		logger.error("OnlineBillPayment is locked now!");
    		throw new onlineBillPaymentIsInTheProcessException("OnlineBillPayment is locked now!");
		}

	    
	    if(!ISOFinalMessageType.isReversalMessage(ifx.getIfxType())){
		    if(ifx.getAuth_Amt() == null || !onbill.getAmount().equals(ifx.getAuth_Amt())){
		    	logger.error("invalid amount! ");
		    	throw new NotValidOnlineBillPaymentMessageException("invalid amount! ");
		    }
		    
//	    	Long todayDate = System.currentTimeMillis();
//	    	Date d=new Date(todayDate);
//	    	Date dd=new Date(onbill.getExpDt());
	    	if(onbill.getExpDt()<DateTime.now().getDateTimeLong()){
	    		logger.error("refrence number has been expired! ");
	    		throw new ExpireDateException("refrence number has been expired! ");
	    	}
	    	if(onbill.getPaymentStatus()== null){
	    		onbill.setPaymentStatus(OnlineBillPaymentStatus.NOT_PAID);
	    		logger.error("payment status was null.it is not paid now! ");    		
	    	}
	    	if(OnlineBillPaymentStatus.IN_THE_PROCESS.equals(onbill.getPaymentStatus())){
	    		logger.error("Someone else is Paying this refrence number! ");
	    		throw new onlineBillPaymentIsInTheProcessException("Someone else is Paying this refrence number! ");
	    	}
	    	if(!IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(ifx.getIfxType())&& OnlineBillPaymentStatus.PAID.equals(onbill.getPaymentStatus())){
	    		logger.error("This refrence number has been paid befor. ");
	    		throw new DuplicateOnlineBillPaymentRefNumException("This refrence number has been paid befor.");		
	    	}
	    }
    	
    	return true;
	}
	private Ifx createOutgoingIfx(Ifx incomingIfx, Ifx refIfx) throws CloneNotSupportedException {
		Ifx outgoingIfx = MsgProcessor.processor(incomingIfx);
//		outgoingIfx.setNetworkTrnInfo(refIfx.getNetworkTrnInfo().copy());
		if(IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(incomingIfx.getIfxType())){
			outgoingIfx.setIfxType(IfxType.PREPARE_ONLINE_BILLPAYMENT);
			outgoingIfx.setTrnType(TrnType.PREPARE_ONLINE_BILLPAYMENT);
			if(!TerminalType.ATM.equals(incomingIfx.getTerminalType()))
				outgoingIfx.setRsCode(ISOResponseCodes.APPROVED);
		}
		
		if(IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(incomingIfx.getIfxType())){
			outgoingIfx.setIfxType(IfxType.ONLINE_BILLPAYMENT_TRACKING);
			outgoingIfx.setTrnType(TrnType.ONLINE_BILLPAYMENT);
//			if(!TerminalType.ATM.equals(incomingIfx.getTerminalType()))
				outgoingIfx.setRsCode(ISOResponseCodes.APPROVED);
			Map<String,Object> param = new HashMap<String, Object>();
			param.put("refnum", incomingIfx.getOnlineBillPaymentRefNum());
			String st="from OnlineBillPayment onbill where onbill.refNum = :refnum";
			GeneralDao.Instance.findUniqueObject(st, param);
			
		}
		
//		outgoingIfx.setMy_TrnSeqCntr(incomingIfx.getMy_TrnSeqCntr());
//		outgoingIfx.setSrc_TrnSeqCntr(outgoingIfx.getMy_TrnSeqCntr());
//		outgoingIfx.setRsCode(ErrorCodes.APPROVED);
		return outgoingIfx;
	}
}
