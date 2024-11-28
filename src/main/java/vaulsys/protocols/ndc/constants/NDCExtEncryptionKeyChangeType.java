package vaulsys.protocols.ndc.constants;

import java.lang.reflect.Field;

import vaulsys.persistence.IEnum;
import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;

/**
 * In database, for every ATM,
 * 		KeyType : Switch : NDC 
 * 		TMK     ~ Master ~ Master
 * 		TPK     ~ PIN    ~ Communication
 * 		TAK     ~ MAC    ~ MAC
 */
public class NDCExtEncryptionKeyChangeType extends NDCWriteCommandTypes {
	public static final NDCExtEncryptionKeyChangeType NEW_MASTER_BY_CUR_MASTER_1 = new NDCExtEncryptionKeyChangeType('1');
	public static final NDCExtEncryptionKeyChangeType NEW_COMMUN_BY_CUR_MASTER_2 = new NDCExtEncryptionKeyChangeType('2');
	//public static final NDCExtEncryptionKeyChangeType NEW_COMMUN_BY_CUR_COMMUN_3 = new NDCExtEncryptionKeyChangeType('3');
	public static final NDCExtEncryptionKeyChangeType LOCAL_COMMUN_AS_CUR_COMMUN_4 = new NDCExtEncryptionKeyChangeType('4');

	public static final NDCExtEncryptionKeyChangeType NEW_MAC_BY_CUR_MASTER_5 = new NDCExtEncryptionKeyChangeType('5');
	//public static final NDCExtEncryptionKeyChangeType NEW_MAC_BY_CUR_COMMUN_6 = new NDCExtEncryptionKeyChangeType('6');
	public static final NDCExtEncryptionKeyChangeType LOCAL_COMMUN_AS_CUR_MAC_7 = new NDCExtEncryptionKeyChangeType('7');
	
	public static final NDCExtEncryptionKeyChangeType SEND_EPP_SERIAL_NUMBER_F = new NDCExtEncryptionKeyChangeType('F');
	public static final NDCExtEncryptionKeyChangeType SEND_EPP_PUBLIC_KEY_G = new NDCExtEncryptionKeyChangeType('G');
	public static final NDCExtEncryptionKeyChangeType SEND_ALL_KVV_H = new NDCExtEncryptionKeyChangeType('H');

	private byte code;
	
	public NDCExtEncryptionKeyChangeType(){}
	
	public NDCExtEncryptionKeyChangeType(char code) {
		this.code = (byte) code;
	}
	
	public byte getCode() {
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof NDCExtEncryptionKeyChangeType))
			return false;
		NDCExtEncryptionKeyChangeType ext = (NDCExtEncryptionKeyChangeType) obj;
		return ext.code==this.code;
	}

	@Override
	public int hashCode() {
		return code;
	}

	@Override
	public String toString() {
		return code > 0 ? ""+code : "-";
	}

	public static boolean isExtEncryptionKeyChangeType(byte modifier) {
		try {
			Field[] fields = NDCExtEncryptionKeyChangeType.class.getFields();
			for(Field f : fields) {
				NDCExtEncryptionKeyChangeType type = (NDCExtEncryptionKeyChangeType) f.get(null);
				if(type.code == modifier)
					return true;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		return false;
	}
}
