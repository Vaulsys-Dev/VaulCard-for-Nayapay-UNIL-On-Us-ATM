package vaulsys.clearing.reconcile;

import vaulsys.calendar.DayDate;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.impl.Terminal;

import java.util.HashMap;
import java.util.Map;

public class ISORequestDataProcessor extends AbstractISODataProcessor {

	public static final ISORequestDataProcessor Instance = new ISORequestDataProcessor();
	
	private ISORequestDataProcessor(){}
	
    public Map<Integer, String> process(ProtocolMessage message, Terminal terminal, DayDate stlDate) {
    	Long entityCode = terminal.getOwner().getCode();
        TerminalClearingMode mode = terminal.getClearingMode();
        Map<Integer, String> result = generateRequest(entityCode, terminal, mode, stlDate);
        return result;
    }

	private Map<Integer, String> generateRequest(Long entityCode, Terminal terminal, TerminalClearingMode mode,
			DayDate stlDate) {
		
//		ReconcilementInfo recInfo = processFinancialData(terminal,stlDate);
		Map<Integer, String> result = new HashMap<Integer, String>();
		result.put(74, "0");
		result.put(75, "0");
		result.put(76, "0");
		result.put(77, "0");
		result.put(78, "0");
		result.put(79, "0");
		result.put(80, "0");
		result.put(81, "0");
		result.put(82, "0");
		result.put(83, "0");
		result.put(84, "0");
		result.put(85, "0");
//		long creditAmount = recInfo.getCreditAmount();
		result.put(86, "0");
//		long creditReversalAmount = recInfo.getCreditReversalAmount();
		result.put(87, "0");
//		long debitAmount = recInfo.getDebitAmount();
		result.put(88, "0");
//		long debitReversalAmount = recInfo.getDebitReversalAmount();
		result.put(89, "0");
//		Long totalAmount = creditAmount - creditReversalAmount - debitAmount + debitReversalAmount;
//		if (totalAmount <0)
//			totalAmount *=-1;
		result.put(97, "0");
		return result;
	}
}
