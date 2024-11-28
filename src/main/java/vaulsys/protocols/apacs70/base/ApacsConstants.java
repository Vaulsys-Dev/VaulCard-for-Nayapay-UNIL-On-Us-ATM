package vaulsys.protocols.apacs70.base;

import vaulsys.persistence.IEnum;

public class ApacsConstants implements IEnum {
//	public static final String ESC = Character.toString((char)27);  //1b
//	public static final String FS = Character.toString((char)28);  //1c
//	public static final String GS = Character.toString((char)29);  //1d
//	public static final String RS = Character.toString((char)30);  //1e
//	public static final String US = Character.toString((char)31);  //1f

	public static final byte ESC_VALUE = 27;  //1b
	public static final byte FS_VALUE = 28;  //1c
	public static final byte GS_VALUE = 29;  //1d
	public static final byte RS_VALUE = 30;  //1e
	public static final byte US_VALUE = 31;  //1f
	
	public static final ApacsConstants ESC = new ApacsConstants(ESC_VALUE);
	public static final ApacsConstants FS = new ApacsConstants(FS_VALUE);
	public static final ApacsConstants GS = new ApacsConstants(GS_VALUE);
	public static final ApacsConstants RS = new ApacsConstants(RS_VALUE);
	public static final ApacsConstants US = new ApacsConstants(US_VALUE);
	
	private byte _byte;
	
	public ApacsConstants(byte _byte) {
		this._byte = _byte;
	}

	public byte getByte() {
		return _byte;
	}
	
	@Override
	public String toString() {
		return Character.toString((char)_byte);
	}
}
