package vaulsys.eft.base.ifxTypeProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import vaulsys.authorization.exception.HotCardNotApprovedPanException;
import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Pair;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

//TASK Task015 : HotCard
public class HotCardProcessor extends MessageProcessor {
	private Logger logger = Logger.getLogger(this.getClass());
	
	
	public static final HotCardProcessor Instance = new HotCardProcessor();
	private HotCardProcessor(){}
	@Override
	public Message createOutgoingMessage(Transaction transaction,
			Message incomingMessage, Channel channel,
			ProcessContext processContext) throws Exception {

		return GeneralMessageProcessor.Instance.createOutgoingMessage(transaction, incomingMessage, channel, processContext);
	
	};
	
	
	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
//		super.messageValidation(ifx, incomingMessage);
		if (IfxType.HOTCARD_INQ_RQ.equals(ifx.getIfxType())) {//AldTODO Task015 : Aya REV_RQ Ham niaz ast check shavad
			if (!ifx.getDestBankId().equals(ProcessContext.get().getMyInstitution().getBin()) //AldQuestion az getRecvBankId estefade konam ya DestBankId
				&& 
				!ProcessContext.get().isPeerInstitution(Util.longValueOf(ifx.getDestBankId()))){ //AldQuestion Task015 : isPeerInstitution //AldQuestion az getRecvBankId estefade konam ya DestBankId
				logger.error("Failed: Pan not allowed on the HotCard service: "
						+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
				throw new HotCardNotApprovedPanException("Failed: Pan not allowed on the HotCard service: "
						+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
				
			}
			//AldTODO Task015 : Inja Bayesti Carte Peer ra be peer befrestam
		} 
			
		
	}
}
