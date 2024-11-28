package vaulsys.transfermanual;

import vaulsys.calendar.DayDate;

public class BeanSorushNotTrn {

	public Long row;
	public DayDate persianDt;
	public String trnSeqCntr;
	public Long amountTrx;
	public Long amountSod;
	public Long amount;
	public String appPan;
	
	public Long getRow() {
		return row;
	}
	public void setRow(Long row) {
		this.row = row;
	}
	public DayDate getPersianDt() {
		return persianDt;
	}
	public void setPersianDt(DayDate persianDt) {
		this.persianDt = persianDt;
	}
	public String getTrnSeqCntr() {
		return trnSeqCntr;
	}
	public void setTrnSeqCntr(String trnSeqCntr) {
		this.trnSeqCntr = trnSeqCntr;
	}
	public Long getAmountTrx() {
		return amountTrx;
	}
	public void setAmountTrx(Long amountTrx) {
		this.amountTrx = amountTrx;
	}
	public Long getAmountSod() {
		return amountSod;
	}
	public void setAmountSod(Long amountSod) {
		this.amountSod = amountSod;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public String getAppPan() {
		return appPan;
	}
	public void setAppPan(String appPan) {
		this.appPan = appPan;
	}
	@Override
	public boolean equals(Object arg0) {
		try {
			if(this.getTrnSeqCntr().trim().equals(((BeanSorushNotTrn)arg0).getTrnSeqCntr())   &&
					this.getAmount().equals(((BeanSorushNotTrn)arg0).getAmount())             &&
					this.getAppPan().trim().equals(((BeanSorushNotTrn)arg0).getAppPan())      &&
					this.getPersianDt().equals(((BeanSorushNotTrn)arg0).getPersianDt())){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	
}
