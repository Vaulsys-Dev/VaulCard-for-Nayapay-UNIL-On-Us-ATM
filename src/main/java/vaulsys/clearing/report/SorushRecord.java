package vaulsys.clearing.report;

import vaulsys.calendar.DayDate;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.TransactionType;

//TASK Task056
//TASK Task103
public class SorushRecord {
	
	public DayDate trxDate;
	public String persianDt;
	public String trnSeqCntr;
	public String appPan;
	public Long amount;
	public Long bankId;
	public String terminalId;
	public String data;
	public String rsCode;
	public String workingDay;
	public String lifeCycleId;//Task103 
	//me
	public String trxType;
	
	public boolean isDuplicate = false;
	public String disagrementSupplementaryDocnum;
	public String state_of_I_O;
	public Boolean isChangeBin;
	
	public boolean in8Shetab = false;
	public boolean in8Bank = false;
	
	public String sorushPersianDt;
	public String sorushTrnSeqCntr;
	public String sorushWorkingDay;
	
	
	public boolean sorushIn8Shetab = false;
	public boolean soroushIn8Bank = false;
	
	public boolean isNotValid_in8Sh_in8b = false;
	public String sorushFileDate;
	
	//TASK Task080
	//Add in 93.02.09
	public String documentNumber;
	public String documentPattern;
	
	//Add in 93.05.26
	public TransactionType sorushTransactionType;
	public Long sorushAmount; //Amount with gain and fee
	//Add in 93.06..02
	public Long fee;
	public Long gain_delta;
	public String trxId;
	public String sorushTrxId;
	

	@Override
	public String toString() {
		return trxDate+"|"+persianDt+"|"+terminalId+"|"+trnSeqCntr+"|"+appPan+"|"+amount+"|"+trxType+"|"+workingDay+"|"+documentNumber+"|"+documentPattern;
	}
}
