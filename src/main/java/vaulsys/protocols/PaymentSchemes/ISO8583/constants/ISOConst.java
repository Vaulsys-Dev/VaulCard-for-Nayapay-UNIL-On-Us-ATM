package vaulsys.protocols.PaymentSchemes.ISO8583.constants;

import vaulsys.persistence.IEnum;
import vaulsys.protocols.ifx.enums.TerminalType;

public class ISOConst implements IEnum{

	//PAN ENTRY MODEs
	public static final String UNSPECIFIED_PAN_ENT_MODE = "00";
	public static final String MANUAL_PAN_ENT_MODE = "01";
	public static final String MAGSTRIP_PAN_ENT_MODE = "02";
	public static final String BARCODE_PAN_ENT_MODE = "03";
	public static final String OCR_PAN_ENT_MODE = "04";
	public static final String ICC_PAN_ENT_MODE = "05";
	public static final String UICS_PAN_ENT_MODE = "07";
	public static final String MAGSTRIP_RELIABLE_PAN_ENT_MODE = "90";
	public static final String ICC_UNRELIAB_PAN_ENT_MODE = "95";
	public static final String SUICS_PAN_ENT_MODE = "98";

	//PIN ENTRY MODEs
	public static final String UNKNOWN_PIN_ENT_MODE = "0";
	public static final String PINENABLED_PIN_ENT_MODE = "1";
	public static final String NOPIN_PIN_ENT_MODE = "2";

}
