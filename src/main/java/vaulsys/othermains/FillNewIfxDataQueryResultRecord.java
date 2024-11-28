package vaulsys.othermains;

import vaulsys.calendar.DateTime;

//TASK Task110 : new ifx Data
public class  FillNewIfxDataQueryResultRecord {

  //query.append(" select ifx.id as ifxId,net.Src_TrnSeqCntr as trnSeqCntr ,net.BankId as bankId , net.OrigDt as origDt ")

	Long ifxId;
	String trnSeqCntr;
	String bankId; //Raza Changing to String from Long
	String appPan;
	DateTime origDt;
	String rsCode;

	public FillNewIfxDataQueryResultRecord() {
	}

	public FillNewIfxDataQueryResultRecord(final Long ifxId, final String trnSeqCntr, final String bankId, final String appPan, final DateTime origDt , final String rsCode) {
		this.ifxId = ifxId;
		this.trnSeqCntr = trnSeqCntr;
		this.bankId = bankId;
		this.appPan = appPan;
		this.origDt = origDt;
		this.rsCode = rsCode;
  }


	@Override
	public final String toString() {
		return
			this.ifxId + "|"
				+ this.trnSeqCntr + "|"
				+ this.bankId + "|"
				+ this.appPan + "|"
				+ (this.origDt != null ? this.origDt : "") + "|"
				+ this.rsCode;
	}
}
