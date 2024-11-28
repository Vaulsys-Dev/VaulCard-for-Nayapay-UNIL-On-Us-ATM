package vaulsys.clearing.report;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;

public class SaderatReportRecord {
	TrnType trnType;
	String trnSeqCntr;
	DateTime OrgDt;
	String rsCode;
	TerminalType terminalType;
	String appPan;
	Long amount;
	String terminalId;
	String NetworkRefId;

	public SaderatReportRecord(){
		
	}
	@Override
	public String toString(){
		return 
			this.trnSeqCntr + "|" +
			this.OrgDt + "|" +
			this.appPan + "|"+
			this.amount + "|"+
			this.trnType + "|" +
			this.terminalType +"|" +
			this.terminalId + "|"+
			this.NetworkRefId + "|"+
			this.rsCode + "|" ;
		
	}
}
