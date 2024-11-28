package vaulsys.transfermanual;

import vaulsys.clearing.report.ShetabReversalReportRecord;
import vaulsys.transaction.Transaction;


public class BeanDataTransfer {
	
	public Transaction trx;
	public ShetabReversalReportRecord reverslSorush;
	
	
	public Transaction getTrx() {
		return trx;
	}
	public void setTrx(Transaction trx) {
		this.trx = trx;
	}
	public ShetabReversalReportRecord getReverslSorush() {
		return reverslSorush;
	}
	public void setReverslSorush(ShetabReversalReportRecord reverslSorush) {
		this.reverslSorush = reverslSorush;
	}
	
	
	
}
