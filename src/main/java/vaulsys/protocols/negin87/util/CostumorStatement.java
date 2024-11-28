package vaulsys.protocols.negin87.util;

import com.ghasemkiani.util.icu.PersianCalendar;
import com.ghasemkiani.util.icu.PersianDateFormat;

import vaulsys.calendar.DateTime;

public class CostumorStatement {
	
	 int BrnCode;
     DateTime DocDate;
     long    DocSrl;
     double  Amount;
     double  Bal;
     //1:Charged 0:Normal
     ChargeFlag m_byChargFlg;
     String    m_szDocDesc; //61
	
     public int getBrnCode() {
		return BrnCode;
	}
	public void setBrnCode(int brnCode) {
		BrnCode = brnCode;
	}
	public DateTime getDocDate() {
		return DocDate;
	}
	public void setDocDate(DateTime docDate) {
		DocDate = docDate;
	}
	public long getDocSrl() {
		return DocSrl;
	}
	public void setDocSrl(long docSrl) {
		DocSrl = docSrl;
	}
	public double getAmount() {
		return Amount;
	}
	public void setAmount(double amount) {
		Amount = amount;
	}
	public double getBal() {
		return Bal;
	}
	public void setBal(double bal) {
		Bal = bal;
	}
	public ChargeFlag getM_byChargFlg() {
		return m_byChargFlg;
	}
	public void setM_byChargFlg(ChargeFlag chargFlg) {
		m_byChargFlg = chargFlg;
	}
	public String getM_szDocDesc() {
		return m_szDocDesc;
	}
	public void setM_szDocDesc(String docDesc) {
		m_szDocDesc = docDesc;
	}
	
	@Override
	public String toString() {
		PersianDateFormat format = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
		return getBrnCode()+", "+ format.format(getDocDate().toDate())+ 
		", "+ getDocSrl()+ ", "+ getAmount()+", "+ getBal()+", "+ getM_byChargFlg() +", "+ getM_szDocDesc();
	}
}
