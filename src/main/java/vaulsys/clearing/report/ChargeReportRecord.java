package vaulsys.clearing.report;

import vaulsys.protocols.ifx.enums.TerminalType;

public class ChargeReportRecord extends ReportRecord {
	public TerminalType terminalType;
	public Long cardSerialNo;
	public Integer year;
	public Integer fileId;
	public String terminalId;
	public Long endPointTerminalCode;
}
