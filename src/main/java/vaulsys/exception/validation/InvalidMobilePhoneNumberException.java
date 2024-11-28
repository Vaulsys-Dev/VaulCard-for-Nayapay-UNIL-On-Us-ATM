package vaulsys.exception.validation;

public class InvalidMobilePhoneNumberException extends Exception{
	private static final long serialVersionUID = 1L;

	public InvalidMobilePhoneNumberException(String mobilePhoneNumber) {
		super(mobilePhoneNumber);
	}

}
