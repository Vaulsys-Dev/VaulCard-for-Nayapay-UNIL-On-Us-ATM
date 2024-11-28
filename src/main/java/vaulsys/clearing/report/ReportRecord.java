package vaulsys.clearing.report;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.ifx.enums.IfxType;

public class ReportRecord {

	public IfxType ifxType;
	public DateTime recievedDt;
	public String trnSeqCntr;
	public String terminalId;
	public String appPAN;
	public Long auth_Amt;
	public long totalFee;
	public long totalAmount;
	public long bankId;
	
}
