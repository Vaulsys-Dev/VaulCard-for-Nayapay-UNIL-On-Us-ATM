package vaulsys.clearing.report;

public class ShetabSanadRecord {
	
	public long disagreeAmt = 0;
	public long feePardakhti = 0;
	public long feeDaryafti = 0;
	public long totalIssuer = 0;
	public long totalAcquirer = 0;
	public String strOut;
	
	public ShetabSanadRecord(){
	}
	
	public ShetabSanadRecord(long disagreeAmt, long feePardakhti, long feeDaryafti, long totalIssuer, long totalAcquirer, String strOut){
		this.disagreeAmt = disagreeAmt;
		this.feePardakhti = feePardakhti;
		this.feeDaryafti = feeDaryafti;
		this.totalIssuer = totalIssuer;
		this.totalAcquirer = totalAcquirer;
		this.strOut = strOut;
	}                        
}                            
                             