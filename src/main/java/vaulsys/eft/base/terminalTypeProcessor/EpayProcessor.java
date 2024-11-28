package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.authorization.exception.DuplicateEPaymentMessageException;
import vaulsys.authorization.exception.InvalidFieldException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import org.apache.log4j.Logger;

public class EpayProcessor extends TerminalTypeProcessor {
	transient Logger logger = Logger.getLogger(EpayProcessor.class);

	public static final EpayProcessor Instance = new EpayProcessor();
	private EpayProcessor(){};

	@Override
	public void messageValidation(Ifx ifx, Long messageId) throws Exception {
		
		// Check the transfer rq destination should be pasargad
		if (IfxType.TRANSFER_RQ.equals(ifx.getIfxType()) ||
			IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifx.getIfxType())) {
//			if (!ifx.getDestBankId().equals(GlobalContext.getInstance().getMyInstitution().getBin())  
			if (!ifx.getDestBankId().equals(ProcessContext.get().getMyInstitution().getBin())
				&& /*!ifx.getDestBankId().equals(639347L)*/
//				!GlobalContext.getInstance().isPeerInstitution(ifx.getDestBankId())){
				!ProcessContext.get().isPeerInstitution(Util.longValueOf(ifx.getDestBankId()))){
				logger.error("Failed: Pan not allowed on the Transfer service: "
						+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
				throw new PanPrefixServiceNotAllowedException("Failed: Pan not allowed on the Transfer service: "
						+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
			}
		}
		
		// Check the check account rq destination should be pasargad
		if (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())) {
//			if (!ifx.getRecvBankId().equals(GlobalContext.getInstance().getMyInstitution().getBin())  
			if (!ifx.getRecvBankId().equals(ProcessContext.get().getMyInstitution().getBin())
					&& /*!ifx.getDestBankId().equals(639347L)*/
//					!GlobalContext.getInstance().isPeerInstitution(ifx.getRecvBankId())){
					!ProcessContext.get().isPeerInstitution(Util.longValueOf(ifx.getRecvBankId()))){
				logger.error("Failed: Pan not allowed on the Check Account service: "
						+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
				throw new PanPrefixServiceNotAllowedException("Failed: Pan not allowed on the Check Account service: "
						+ ifx.getAppPAN() + ", " + ifx.getTrnType().toString());
			}
		}
		
		if (Util.hasText(ifx.getIP()) && ifx.getIP().length() > 16){
			throw new InvalidFieldException("Length of IP field exceeds 15 characters! (ip: "+ ifx.getIP()+")");
		}
		
		List<Transaction> payTransactions = TransactionService.getEPayTransactions(ifx.getInvoiceNumber(),
				ifx.getInvoiceDate(), ifx.getTerminalId(), ifx.getOrgIdNum(), ifx.getTrnType());
		if (payTransactions != null && payTransactions.size() > 1)
			throw new DuplicateEPaymentMessageException("Duplicate Message Recieved from EPay Switch "
					+ "with invoiceNO: " + ifx.getInvoiceNumber() + ", invoiceDate: " + ifx.getInvoiceDate()
					+ ", terminalId: " + ifx.getTerminalId() + ", merchantId: " + ifx.getOrgIdNum());
	
//		super.messageValidation(ifx, messageId);
	}
//	@Override
//	public void postBindingResponseMessage(Ifx incomingIfx, Transaction referenceTrx){
//			try {
//				if(referenceTrx.getIncomingIfx().getTerminalType().equals(TerminalType.MOBILE))
//					incomingIfx.setTerminalType(TerminalType.MOBILE);
//			} catch (Exception e) {
//				logger.error("Encounter an exception to lock atm terminal", e);
//			}
//	}
}
