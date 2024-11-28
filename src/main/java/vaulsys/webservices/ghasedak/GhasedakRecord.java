package vaulsys.webservices.ghasedak;

public class GhasedakRecord {
	public String name;
	public Double amount;
	public String currency;
	public String creditDate;
	public Long code;
	
	public GhasedakRecord(){
	}
	
	public GhasedakRecord(String name, Double amount, String currency, String creditDate, Long code){
		this.name = name;
		this.amount = amount;
		this.currency = currency;
		this.creditDate = creditDate;
		this.code = code;
	}
}
