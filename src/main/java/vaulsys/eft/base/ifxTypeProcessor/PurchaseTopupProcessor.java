package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.topup.UnsupportedTopupOrganization;
import vaulsys.authorization.exception.topup.UnsupportedTransation;
import vaulsys.billpayment.consts.OrganizationType;
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
import vaulsys.scheduler.MCITopupJobInfo;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class PurchaseTopupProcessor extends MessageProcessor {
	transient Logger logger = Logger.getLogger(GeneralMessageProcessor.class);

	public static final PurchaseTopupProcessor Instance = new PurchaseTopupProcessor();
	private PurchaseTopupProcessor(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Message outgoingMessage = null;
		Ifx outgoingIfx = null;

		Ifx incomingIfx = incomingMessage.getIfx();
		if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType())) {
			if(ISOFinalMessageType.isReversalOrRepeatMessage(incomingIfx.getIfxType())) {
				logger.error("Reversal messages are not supported for topup transactions...");
				throw new UnsupportedTransation();
			}
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			
			outgoingIfx = outgoingMessage.getIfx();
			
			if (TerminalType.ATM.equals(incomingIfx.getTerminalType()) ||
					TerminalType.PINPAD.equals(incomingIfx.getTerminalType())) {
				outgoingIfx.setIfxType(IfxType.BILL_PMT_RQ);
				outgoingIfx.setTrnType(TrnType.BILLPAYMENT);
				outgoingIfx.setBillOrgType(OrganizationType.UNDEFINED);
				outgoingIfx.setBillID(incomingIfx.getTopupCompanyCode().toString());
				outgoingIfx.setBillPaymentID(incomingIfx.getTopupCompanyCode().toString() + incomingIfx.getAuth_Amt().toString());
			} else {
				outgoingIfx.setIfxType(IfxType.PURCHASE_RQ);
				outgoingIfx.setTrnType(TrnType.PURCHASE);
			}
			
			Organization organization = FinancialEntityService.findEntity(Organization.class, incomingIfx.getTopupCompanyCode().toString());
			if(organization == null){
				logger.error("could not find topup organization...");
				throw new UnsupportedTopupOrganization();
			}
			
			ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(organization);
			if(thPVT == null){
				logger.error("could not find topup organization virtual terminal...");
				throw new UnsupportedTopupOrganization();
			}
			outgoingIfx.setThirdPartyTerminalId(thPVT.getCode());
			incomingIfx.setThirdPartyTerminalId(thPVT.getCode());
			
			GeneralDao.Instance.saveOrUpdate(incomingIfx);

		} else {
			outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
			outgoingIfx = outgoingMessage.getIfx();
			outgoingIfx.setIfxType(IfxType.PURCHASE_TOPUP_RS);
			outgoingIfx.setTrnType(TrnType.PURCHASETOPUP);
			transaction.setDebugTag(outgoingIfx.getIfxType().toString());
			if (ISOResponseCodes.isSuccess(outgoingIfx.getRsCode())) {
				logger.debug("creating topup job info...");
				MCITopupJobInfo mciTopupJob = new MCITopupJobInfo(transaction, incomingIfx.getTopupCellPhoneNumber().toString(), incomingIfx.getAuth_Amt());
				GeneralDao.Instance.saveOrUpdate(mciTopupJob);
				logger.debug("topup job info ("+mciTopupJob.getId()+") has been created...");
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

		logger.debug("Process purchase_topup incoming message ");
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
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
	}
}
