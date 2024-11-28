package vaulsys.protocols.ndc.base;

import vaulsys.persistence.IEnum;
import vaulsys.protocols.ndc.constants.NDCExtEncryptionKeyChangeType;

public class NDCWriteCommandTypes implements IEnum {    
	public static final NDCWriteCommandTypes SCREEN_TABLE_LOAD = new NDCWriteCommandTypes('1');
	public static final NDCWriteCommandTypes STATE_TABLE_LOAD = new NDCWriteCommandTypes('2');
	public static final NDCWriteCommandTypes PARAMETER_TABLE_LOAD = new NDCWriteCommandTypes('3');
	public static final NDCWriteCommandTypes FIT_TABLE_LOAD = new NDCWriteCommandTypes('5');
	public static final NDCWriteCommandTypes CONFIGURATION_ID_LOAD = new NDCWriteCommandTypes('6');
	public static final NDCWriteCommandTypes ENHANCED_PARAMETER_TABLE_LOAD = new NDCWriteCommandTypes('A');
//	public static final NDCWriteCommandTypes INTERACTIVE_RESPONSE = new NDCWriteCommandTypes('2');
	public static final NDCWriteCommandTypes DATE_TIME_LOAD = new NDCWriteCommandTypes('C');
    
	private byte code;
	
	public NDCWriteCommandTypes(){}
	
	public NDCWriteCommandTypes(char code) {
		this.code = (byte) code;
	}
	
	public byte getCode() {
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof NDCWriteCommandTypes))
			return false;
		NDCWriteCommandTypes ext = (NDCWriteCommandTypes) obj;
		return ext.code==this.code;
	}

	@Override
	public int hashCode() {
		return code;
	}

	@Override
	public String toString() {
		switch(this.code){
		case '1':
			return "SCREEN_TABLE_LOAD";
		case '2':
			return "STATE_TABLE_LOAD";
		case '3':
			return "PARAMETER_TABLE_LOAD";
		case '5':
			return "FIT_TABLE_LOAD";
		case '6':
			return "CONFIGURATION_ID_LOAD";
		case 'A':
			return "ENHANCED_PARAMETER_TABLE_LOAD";
		case 'C':
			return "DATE_TIME_LOAD";
		}
		return code > 0 ? ""+code : "-";
	}
}
