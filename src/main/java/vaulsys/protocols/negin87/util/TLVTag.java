package vaulsys.protocols.negin87.util;

public class TLVTag {
	
	public final static byte[] ADD_DATA_TAG  = new byte[]{0x71};
	public final static byte[] INQUIRY_DATE_TAG  = new byte[]{(byte)0xFF, 0x01};
	public final static byte[] CUSTOMER_ACCOUNT_TAG  = new byte[]{(byte)0xDF, 0x02};
	public final static byte[] EXP_DATE_TAG  = new byte[]{(byte)0x9F, 0x02};
	public final static byte[] CVV2_TAG  = new byte[]{(byte)0x9F, 0x03};
	
}
