package vaulsys.clearing.report;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.protocols.ifx.enums.TrnType;


public class IssShetabReportRecord extends ShetabReportRecord {
	public TrnType trnType;
	public String srcAccountNumber;
	public String destAccountNumber;
	public String destAppPan;
	public Long bankId;
	public OrganizationType billOrgType;
	

	public IssShetabReportRecord() {
	}

	@Override
	public String toString(){
		return super.toString() + "|" +
			this.srcAccountNumber + "|" +
			this.destAccountNumber + "|" +
			this.destAppPan + "|" +
			this.bankId + "|" +		
			this.billOrgType;
	}
}
