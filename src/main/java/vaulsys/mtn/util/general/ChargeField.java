package vaulsys.mtn.util.general;

import vaulsys.calendar.DateTime;

public class ChargeField {
	
	public String company; 
	public Long credit;
	public Integer pinLen;
	public Long serial;
	public String pin;
	public String createdDate;
	public DateTime expireDate;
	public String fileId;
	public String provider;
	
	public ChargeField(){
	}
	
	public ChargeField(String company, Long credit, Integer pinLen, Long serial, String pin, String createdDate, DateTime expireDate, String fileId, String provider){
		this.company = company;
		this.credit = credit;
		this.pinLen = pinLen;
		this.serial = serial;
		this.pin = pin;
		this.createdDate = createdDate;
		this.expireDate = expireDate;
		this.fileId = fileId;
		this.provider = provider;
	}
}
