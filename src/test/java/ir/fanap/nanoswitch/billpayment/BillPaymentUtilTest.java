package vaulsys.billpayment;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class BillPaymentUtilTest{

	@Test
	public void generateCheckDigitTest() throws Exception
	{
		assertEquals(3, BillPaymentUtil.getCheckDigit("77226391314"));
		assertEquals(7, BillPaymentUtil.getCheckDigit("966625230412"));
		assertEquals(2, BillPaymentUtil.getCheckDigit("955146700412"));
		assertEquals(0, BillPaymentUtil.getCheckDigit("603234540122"));
	}

}
