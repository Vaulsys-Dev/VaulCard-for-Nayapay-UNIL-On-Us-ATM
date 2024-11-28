package vaulsys.clearing.report;

import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;


public class TotalShetabReportRecord  {
	TrnType trnType;
	IfxType ifxType;
	Long bankId;
//	String rsCode;
	TerminalType terminalType;
	int isShetab;
	Long transactionAmount;
	
	

	public TotalShetabReportRecord() {
//		rsCode = "00";
	}

	@Override
	public String toString(){
		return 
			this.isShetab + "|" +
			this.trnType + "|" +
			this.terminalType +"|" +
			this.transactionAmount + "|" + "\t"+ 
			this.ifxType + "|" +
			this.bankId;
//			this.bankId + "|" +		
//			this.rsCode + "|";
	}
}
