package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.InvalidCompanyCodeException;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Institution;
import vaulsys.entity.impl.Organization;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
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
import vaulsys.thirdparty.exception.ThirdPartyPurchaseException;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class ThirdPartyPaymentProcessor extends MessageProcessor{
	transient Logger logger = Logger.getLogger(GeneralMessageProcessor.class);

	public static final ThirdPartyPaymentProcessor Instance = new ThirdPartyPaymentProcessor();
	private ThirdPartyPaymentProcessor(){};
	
	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
	throws Exception {
		Message outgoingMessage = null;
		Ifx outgoingIfx = null;

		Ifx incomingIfx = incomingMessage.getIfx();
		/*if(IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(incomingIfx.getIfxType())){
			if( incomingIfx.getRsCode()==null && !Util.hasText(incomingIfx.getStatusDesc())) {
				*//*** is needed? ***//*
				Organization organization = OrganizationService.findOrganizationByCode(incomingIfx.getThirdPartyCode(), OrganizationType.THIRDPARTYPURCHASE);
				ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(organization);
				incomingIfx.setThirdPartyName(organization.getName());
				incomingIfx.setThirdPartyNameEn(organization.getNameEn());
				incomingIfx.setThirdPartyTerminalId(thPVT.getCode());
				
				incomingIfx.setRsCode(ErrorCodes.APPROVED);
			}
			
			
		} else*/if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType())) {
			
			
			if (incomingIfx.getAuth_Amt() == null || incomingIfx.getAuth_Amt() <= 0)
				throw new MandatoryFieldException("Failed: Bad Amount ThirdParty Payment");
			
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			outgoingIfx = outgoingMessage.getIfx();
			
			//Mirkamali: 112297
//			if (TerminalType.ATM.equals(incomingIfx.getTerminalType()) ||
//					TerminalType.PINPAD.equals(incomingIfx.getTerminalType())) {
				outgoingIfx.setIfxType(IfxType.BILL_PMT_RQ);
				outgoingIfx.setTrnType(TrnType.BILLPAYMENT);
				outgoingIfx.setBillOrgType(OrganizationType.UNDEFINED);
				outgoingIfx.setBillID(incomingIfx.getThirdPartyCode().toString());
				outgoingIfx.setBillPaymentID(incomingIfx.getThirdPartyCode().toString() + incomingIfx.getAuth_Amt().toString());
//			} else {
//				outgoingIfx.setIfxType(IfxType.PURCHASE_RQ);
//				outgoingIfx.setTrnType(TrnType.PURCHASE);
//			}
			
			/*** for atm is needed?!***/
//			if (incomingIfx.getThirdPartyCode() == null) {
				Organization organization = OrganizationService.findOrganizationByCode(incomingIfx.getThirdPartyCode(), OrganizationType.THIRDPARTYPURCHASE);
				if (organization != null) {
					ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(organization); 
					outgoingIfx.setThirdPartyTerminalId(thPVT.getCode());
					outgoingIfx.setThirdPartyName(organization.getName());
					outgoingIfx.setThirdPartyNameEn(organization.getNameEn());
					incomingIfx.setThirdPartyTerminalId(thPVT.getCode());
				}
//			}
			
			GeneralDao.Instance.saveOrUpdate(incomingIfx);
			
	        transaction.setOutgoingIfx(outgoingIfx);
		} else if (ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType())) {
			try {
				outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
				outgoingIfx = outgoingMessage.getIfx();
				outgoingIfx.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RS);
				outgoingIfx.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
				transaction.setDebugTag(outgoingIfx.getIfxType().toString());
				Organization organization = OrganizationService.findOrganizationByCode(incomingIfx.getThirdPartyCode(), OrganizationType.THIRDPARTYPURCHASE);
				if (organization != null) {
					ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(organization); 
					outgoingIfx.setThirdPartyTerminalId(thPVT.getCode());
					outgoingIfx.setThirdPartyName(organization.getName());
					outgoingIfx.setThirdPartyNameEn(organization.getNameEn());
					incomingIfx.setThirdPartyTerminalId(thPVT.getCode());
				}
			} catch (Exception e) {
				transaction.setDebugTag(IfxType.THIRD_PARTY_PURCHASE_RS.toString());
				logger.error("ThirdPartyPaymentException! "+ e.getClass().getSimpleName()+": "+ e.getMessage());
				throw new ThirdPartyPurchaseException(e);
			}
		}

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
		
		logger.debug("Process Third Party Payment incoming message ");
		
		Ifx outgoingIfx = MsgProcessor.processor(incomingMessage.getIfx());
		
		outgoingIfx.setFwdBankId((channel.getInstitutionId() == null ? null : (FinancialEntityService.findEntity(
				Institution.class, channel.getInstitutionId())).getBin().toString()));
		
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
		//super.messageValidation(ifx, incomingMessage);
		
		if (OrganizationService.findOrganizationByCode(ifx.getThirdPartyCode(), OrganizationType.THIRDPARTYPURCHASE) == null) {
			throw new InvalidCompanyCodeException();
		}
		
		OrganizationService.validation(ifx, ifx.getThirdPartyCode(), OrganizationType.THIRDPARTYPURCHASE);
	}
}
