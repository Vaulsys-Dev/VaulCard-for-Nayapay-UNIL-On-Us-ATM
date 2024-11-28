package vaulsys.clearing.reconcile.negin;

import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.reconcile.IFinancialProcessor;
import vaulsys.clearing.reconcile.ReconcilementInfo;
import vaulsys.terminal.impl.Terminal;

public class NeginFinancialProcessor implements IFinancialProcessor {
	
	public static final NeginFinancialProcessor Instance = new NeginFinancialProcessor();
	
	private NeginFinancialProcessor(){}
	
	@Override
	public ReconcilementInfo processFinancialData(Terminal terminal, MonthDayDate stlDate) {
		ReconcilementInfo reconcilementInfo = new ReconcilementInfo();

//		List<IfxType> ifxTypes = new ArrayList<IfxType>();
//		ifxTypes.add(IfxType.TRANSFER_TO_ACCOUNT_RS);
//		ifxTypes.add(IfxType.PURCHASE_RS);
//		ifxTypes.add(IfxType.PURCHASE_CHARGE_RS);
//		ifxTypes.add(IfxType.PURCHASE_TOPUP_RS);
//		Long amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setDebitAmount(reconcilementInfo.getDebitAmount() + amount);
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS);
////		ifxTypes.add(IfxType.TRANSFER_TO_ACCOUNT_REV_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setDebitAmount(reconcilementInfo.getDebitAmount() - amount);
//
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
//		ifxTypes.add(IfxType.RETURN_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setCreditAmount(reconcilementInfo.getCreditAmount() + amount);
//		ifxTypes.clear();
////		ifxTypes.add(IfxType.TRANSFER_FROM_ACCOUNT_REV_RS);
//		ifxTypes.add(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setCreditAmount(reconcilementInfo.getDebitReversalAmount() - amount);
//
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.PURCHASE_CHARGE_REV_REPEAT_RS);
//		ifxTypes.add(IfxType.PURCHASE_REV_REPEAT_RS);
////		ifxTypes.add(IfxType.PURCHASE_CHARGE_REV_RS);
////		ifxTypes.add(IfxType.PURCHASE_REV_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setDebitReversalAmount(reconcilementInfo.getDebitReversalAmount() + amount);
//
//		ifxTypes.clear();
//		ifxTypes.add(IfxType.RETURN_REV_REPEAT_RS);
////		ifxTypes.add(IfxType.RETURN_REV_RS);
//		amount = TransactionService.getDesiredMessages(terminal, ifxTypes, stlDate, true);
//		if (amount != null)
//			reconcilementInfo.setCreditReversalAmount(reconcilementInfo.getCreditReversalAmount() + amount);
//
		return reconcilementInfo;
	}
}
