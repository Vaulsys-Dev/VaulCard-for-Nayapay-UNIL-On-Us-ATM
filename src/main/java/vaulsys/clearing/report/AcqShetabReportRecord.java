package vaulsys.clearing.report;

import vaulsys.protocols.ifx.enums.TrnType;


public class AcqShetabReportRecord extends ShetabReportRecord {
	public TrnType trnType;
	public String srcAccountNumber;
	public String destAccountNumber;
	public String destAppPan;
	public Long bankId;
//	public String rsCode;
	public String orgIdNum;
	

	public AcqShetabReportRecord() {
	}

	@Override
	public String toString(){
		return super.toString() + "|" +
			this.srcAccountNumber + "|" +
			this.destAccountNumber + "|" +
			this.destAppPan + "|" +
			this.bankId + "|" +		
//			this.rsCode + "|" +
			this.orgIdNum;
	}
}
