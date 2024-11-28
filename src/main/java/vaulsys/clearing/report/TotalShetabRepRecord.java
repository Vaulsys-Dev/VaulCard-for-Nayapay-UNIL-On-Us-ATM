package vaulsys.clearing.report;

import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;

public class TotalShetabRepRecord {
	public TrnType trnType;
	public IfxType ifxType;
	public TerminalType terminalType;
	public Long amount_acq_credit_3;
	public Long amount_acq_debit_5;
	public Long amount_iss_credit_7;
	public Long amount_iss_debit_9;
	public String recordType;
}
