package vaulsys.protocols.ifx.imp;

import java.io.Serializable;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

//@Entity
//@Table(name = "ifx_e_payment_data")
@Embeddable
public class EPaymentData implements  Serializable, Cloneable {
//    @Id
//    @GeneratedValue(generator="switch-gen")
//	private Long id;

	private String invoiceNumber;
	
	private String email;
	
	@Column(length=15)
	private String IP;

	private String invoiceDate;

	public EPaymentData() {
	}

	public EPaymentData(String invoiceNumber, String invoiceDate) {
		this.invoiceNumber = invoiceNumber;
		this.invoiceDate = invoiceDate;
	}

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
//
	@Override
	protected Object clone() {
		EPaymentData obj = new EPaymentData();
		obj.setInvoiceNumber(invoiceNumber);
		obj.setInvoiceDate(invoiceDate);
		obj.setIP(IP);
		obj.setEmail(email);
		return obj;
	}

	
	public EPaymentData copy() {
		return (EPaymentData) clone();
	}

	public void copyFields(EPaymentData source) {
		if (invoiceNumber == null || invoiceNumber.isEmpty())
			invoiceNumber = source.getInvoiceNumber();
		
		if (invoiceDate == null)
			invoiceDate = source.getInvoiceDate();
		
		if(email == null || email.equals(""))
			email = source.getEmail();

		if(IP == null || IP.equals(""))
			IP = source.getIP();
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String ip) {
		IP = ip;
	}
}
