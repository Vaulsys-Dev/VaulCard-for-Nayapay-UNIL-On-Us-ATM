package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.discount.DiscountProfile;
import vaulsys.discount.DiscountService;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class PurchaseProcessor extends MessageProcessor {
	
	transient Logger logger = Logger.getLogger(PurchaseProcessor.class); 

	public static final PurchaseProcessor Instance = new PurchaseProcessor();
	private PurchaseProcessor(){};


	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		//m.rehman: we are not pos processor and we get deducted amount, so no need to apply discount
		/*
		Long myBin = ProcessContext.get().getMyInstitution().getBin();
		Ifx ifx = incomingMessage.getIfx();
		IfxType ifxType = ifx.getIfxType();
		
		if ((IfxType.PURCHASE_RQ.equals(ifxType ) ||
					IfxType.PURCHASE_REV_REPEAT_RQ.equals(ifxType)) ||
					IfxType.RETURN_RQ.equals(ifxType) ||
					IfxType.RETURN_REV_REPEAT_RQ.equals(ifxType)) {
			
			Terminal endPointTerminal = incomingMessage.getEndPointTerminal();
			
			if (TerminalType.POS.equals(endPointTerminal.getTerminalType())) {
				Long discountProfileId = ((POSTerminal)endPointTerminal).getDiscountProfileId();
				if(discountProfileId != null){
					DiscountProfile discountProfile = ProcessContext.get().getDiscountProfile(discountProfileId);
					Double discount = 0D;
					
					if (discountProfile != null && discountProfile.isEnabled()) {
						discount = DiscountService.computeDiscount(discountProfileId, ifx);
						if (discount == null)
							discount = 0D;
						
					}
					ifx.setReal_Amt(new Long(ifx.getTrx_Amt() - (long) Math.ceil(ifx.getTrx_Amt() * discount)));
					ifx.setAuth_Amt(ifx.getReal_Amt());
				}
			}
		}
		
		if (ifx.getReal_Amt() == null || ifx.getReal_Amt() <= 0)
			throw new MandatoryFieldException("Failed: Bad Amount Purchase");
		*/
		return GeneralMessageProcessor.Instance.createOutgoingMessage(transaction, incomingMessage, channel, processContext);
     }

	
	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
		
		if (ifx.getReal_Amt() == null || ifx.getReal_Amt() <= 0)
			throw new MandatoryFieldException("Failed: Bad Amount Purchase");
		
		GeneralMessageProcessor.Instance.messageValidation(ifx, incomingMessage);
	}
	
}
