package vaulsys.billpayment.exception;

public class SetBillDataException extends Exception {

	private static final long serialVersionUID = 1L;
	public SetBillDataException(String billId, String payId, Throwable cause) {
		super("error occurred setting bill data. bill id: " + billId + " , payment id: " + payId, cause);
	}
}
