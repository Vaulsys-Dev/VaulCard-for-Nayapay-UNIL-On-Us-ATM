package vaulsys.clearing.report;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;

public class ShetabReportRecord {
	public Long row;
	public IfxType type;
	public DateTime origDt;
	public String trnSeqCntr;
	public String appPan;
	public Long destBankId;
	public String terminalId;
	public Long amount;
	public TerminalType terminalType;
	public Long feeAmount;
	public String secondAppPan;
	public String merchantId;
	
	public ShetabReportRecord() {
	}

	public ShetabReportRecord(Long row, IfxType type, DateTime origDt, String trnSeqCntr, String appPan,
			Long destBankId, String terminalId, Long amount, TerminalType terminalType, Long feeAmount, String secondAppPan, String merchantId) {
		this.row = row;
		this.type = type;
		this.origDt = origDt;
		this.trnSeqCntr = trnSeqCntr;
		this.appPan = appPan;
		this.destBankId = destBankId;
		this.terminalId = terminalId;
		this.amount = amount;
		this.terminalType = terminalType;
		this.feeAmount = feeAmount;
		this.secondAppPan = secondAppPan;
		this.merchantId = merchantId;
	}
	
	@Override
	public String toString(){
		return 
			this.row + "|" +
			this.type + "|" +
			this.origDt + "|" +
			this.trnSeqCntr + "|" +		
			this.appPan + "|" +
			this.destBankId + "|" +
			this.terminalId + "|" +
			this.amount + "|" +
			this.terminalType + "|" +
			this.feeAmount;
	}
}
