package vaulsys.clearing.reconcile;

import vaulsys.calendar.MonthDayDate;
import vaulsys.terminal.impl.Terminal;

public class ISOFinancialProcessor implements IFinancialProcessor {
	
	public static final ISOFinancialProcessor Instance = new ISOFinancialProcessor();
	
	private ISOFinancialProcessor(){}

	@Override
	public ReconcilementInfo processFinancialData(Terminal terminal, MonthDayDate stlDate) {
		ReconcilementInfo reconcilementInfo = new ReconcilementInfo();

//		Long amount = 0L;
//		Long number = 0L;
//
//		
//		List<IfxType> ifxTypes = new ArrayList<IfxType>();
//		ifxTypes.add(IfxType.BILL_PMT_RS);
//		ifxTypes.add(IfxType.PURCHASE_RS);
//		ifxTypes.add(IfxType.PURCHASE_CHARGE_RS);
//		ifxTypes.add(IfxType.PURCHASE_TOPUP_RS);
//		Long amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		Long number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setDebitAmount(reconcilementInfo.getDebitAmount() + amount);
//		if (number != null && number != 0)
//			reconcilementInfo.setDebitNumber(reconcilementInfo.getDebitNumber() + number.intValue());
//		
//		
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.TRANSFER_TO_ACCOUNT_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setDebitAmount(reconcilementInfo.getDebitAmount() + amount);
//		if (number != null && number != 0)
//			reconcilementInfo.setTransferNumber(reconcilementInfo.getTransferNumber() + number.intValue());
//		
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS);
////		ifxTypes.add(IfxType.TRANSFER_TO_ACCOUNT_REV_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setDebitAmount(reconcilementInfo.getDebitAmount() - amount);
//		if (number != null)
//			reconcilementInfo.setTransferNumber(reconcilementInfo.getTransferNumber() + number.intValue());
//		
//		ifxTypes.clear();
////		ifxTypes.add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
//		ifxTypes.add(IfxType.RETURN_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setCreditAmount(reconcilementInfo.getCreditAmount() + amount);
//		if (number != null)
//			reconcilementInfo.setCreditNumber(reconcilementInfo.getDebitNumber() + number.intValue());
//		
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setCreditAmount(reconcilementInfo.getCreditAmount() + amount);
//		if (number != null)
//			reconcilementInfo.setTransferNumber(reconcilementInfo.getTransferNumber() + number.intValue());
//		
//		
//		
//		ifxTypes.clear();
////		ifxTypes.add(IfxType.TRANSFER_FROM_ACCOUNT_REV_RS);
//		ifxTypes.add(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setCreditAmount(reconcilementInfo.getDebitReversalAmount() - amount);
//		if (number != null)
//			reconcilementInfo.setTransferNumber(reconcilementInfo.getTransferNumber() + number.intValue());
		
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.BILL_PMT_REV_REPEAT_RS);
//		ifxTypes.add(IfxType.PURCHASE_CHARGE_REV_REPEAT_RS);
//		ifxTypes.add(IfxType.PURCHASE_REV_REPEAT_RS);
////		ifxTypes.add(IfxType.BILL_PMT_REV_RS);
////		ifxTypes.add(IfxType.PURCHASE_CHARGE_REV_RS);
////		ifxTypes.add(IfxType.PURCHASE_REV_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setDebitReversalAmount(reconcilementInfo.getDebitReversalAmount() + amount);
//		if (number != null)
//			reconcilementInfo.setDebitReversalNumber(reconcilementInfo.getDebitNumber() + number.intValue());
//		
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.RETURN_REV_REPEAT_RS);
////		ifxTypes.add(IfxType.RETURN_REV_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		number = TransactionService.getNumberOfDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setCreditReversalAmount(reconcilementInfo.getCreditReversalAmount() + amount);
//		if (number != null)
//			reconcilementInfo.setCreditReversalNumber(reconcilementInfo.getDebitNumber() + number.intValue());
		
		return reconcilementInfo;
	}
}
