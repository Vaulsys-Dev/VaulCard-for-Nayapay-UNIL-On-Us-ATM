package vaulsys.modernpayment.onlinebillpayment;

import javax.persistence.Embeddable;

import vaulsys.persistence.IEnum;

@Embeddable
public class OnlineBillPaymentStatus implements IEnum {
	private static final int UNDEFINED_VALUE = 0;
	private static final int NOT_PAID_VALUE = 1;
	private static final int PAID_VALUE = 2;
	private static final int IN_THE_PROCESS_VALUE = 3;

	public static OnlineBillPaymentStatus UNDEFINED = new OnlineBillPaymentStatus(UNDEFINED_VALUE);
	public static OnlineBillPaymentStatus NOT_PAID = new OnlineBillPaymentStatus(NOT_PAID_VALUE);
	public static OnlineBillPaymentStatus PAID = new OnlineBillPaymentStatus(PAID_VALUE);
	public static OnlineBillPaymentStatus IN_THE_PROCESS = new OnlineBillPaymentStatus(IN_THE_PROCESS_VALUE);

	private int code;

	public OnlineBillPaymentStatus() {
		// TODO Auto-generated constructor stub
	}

	public OnlineBillPaymentStatus(int code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof OnlineBillPaymentStatus))
			return false;
		OnlineBillPaymentStatus that = (OnlineBillPaymentStatus) o;
		return code == that.code;
	}


	public int getCode() {
		return code;
	}

//	@Override
//	public String toString() {
//		return code + "";
//	}
	@Override
	public String toString() {
		if (code == UNDEFINED_VALUE)
			return "تعریف نشده";
		if (code == NOT_PAID_VALUE)
			return "پرداخت نشده";
		if (code == PAID_VALUE)
			return "پرداخت شده";
		if (code == IN_THE_PROCESS_VALUE)
			return "در حال پرداخت";
	
		return "ناشناخته";
	}
	public String toStringEnglish() {
		if(code == UNDEFINED_VALUE)
			return "UNDEFINE";
		if(code == NOT_PAID_VALUE)
			return "NOT PAID";
		if(code == PAID_VALUE)
			return "PAID";
		if(code == IN_THE_PROCESS_VALUE)
			return "IN THE PROCESS";
		
		return "UNKNOWN";
	}
	@Override
	public int hashCode() {
		return code;
	}
}
