package vaulsys.eft.base.ifxTypeProcessor;

import org.apache.log4j.Logger;

import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

public class TransferCheckAccountProcessor extends MessageProcessor{
	transient Logger logger = Logger.getLogger(GeneralMessageProcessor.class);

	
	public static final TransferCheckAccountProcessor Instance = new TransferCheckAccountProcessor();
	private TransferCheckAccountProcessor(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {
		logger.debug("transfercheckaccount: channel: " + channel);
		Ifx incomingIfx = incomingMessage.getIfx();
		Ifx firstTrxIncomingIfx = transaction.getFirstTransaction().getIncomingIfx();
		Ifx trxIncomingIfx =transaction.getIncomingIfx();
		
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);
		Ifx outIfx = MsgProcessor.processor(incomingIfx);
		outIfx.setNetworkTrnInfo(incomingIfx.getNetworkTrnInfo().copy());
		if(UserLanguage.ENGLISH_LANG.equals(firstTrxIncomingIfx.getUserLanguage()) && ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType())){
			if(trxIncomingIfx.getTransientCardHolderName() != null ){
//				incomingIfx.setCardHolderName(new String(trxIncomingIfx.getTransientCardHolderName()));
				outIfx.setCardHolderName(new String(trxIncomingIfx.getTransientCardHolderName()));
			}
			
			if(trxIncomingIfx.getTransientCardHolderFamily() != null ){
//				incomingIfx.setCardHolderFamily(new String(trxIncomingIfx.getTransientCardHolderFamily()));
				outIfx.setCardHolderFamily(new String(trxIncomingIfx.getTransientCardHolderFamily()));
			}
		}
		outgoingMessage = createMessage(transaction, incomingMessage, channel, processContext);
//		Terminal findEndpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), false, processContext);
//		outgoingMessage.setEndPointTerminal(findEndpointTerminal);

//		addNecessaryDataToIfx(outIfx, channel, findEndpointTerminal);

		transaction.addOutputMessage(outgoingMessage);
		GeneralDao.Instance.saveOrUpdate(outIfx);
		outgoingMessage.setIfx(outIfx);
	
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
		
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		
		GeneralDao.Instance.saveOrUpdate(transaction);
		return outgoingMessage;
//		return GeneralMessageProcessor.Instance.createOutgoingMessage(transaction, incomingMessage, channel, processContext);
	}
	
	private Message createMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
	throws CloneNotSupportedException {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);
		
		transaction.addOutputMessage(outgoingMessage);
		outgoingMessage.setChannel(channel);
		
		logger.debug("Process Transfer check account incoming message ");
		
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
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception{
	}
	
}
