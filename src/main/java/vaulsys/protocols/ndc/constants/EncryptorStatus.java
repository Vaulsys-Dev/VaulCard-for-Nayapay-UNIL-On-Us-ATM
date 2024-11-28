package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;
import vaulsys.terminal.atm.device.DeviceStatus;

public class EncryptorStatus extends DeviceStatus implements IEnum {
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char ENCRYPTOR_ERROR_VALUE = '1';
	private static final char ENCRYPTOR_NOT_CONFIGURED_VALUE = '2';
	
	public static EncryptorStatus UNKNOWN = new EncryptorStatus(UNKNOWN_VALUE);
	public static EncryptorStatus ENCRYPTOR_ERROR = new EncryptorStatus(ENCRYPTOR_ERROR_VALUE);
	public static EncryptorStatus ENCRYPTOR_NOT_CONFIGURED = new EncryptorStatus(ENCRYPTOR_NOT_CONFIGURED_VALUE);
	
    public EncryptorStatus() {
		super();
	}

	public EncryptorStatus(char status) {
		super(status);
	}

	public static EncryptorStatus getByCode(char code) {
    	
    	if (code == '1')
    		return ENCRYPTOR_ERROR;
    	
    	if (code == '2')
    		return ENCRYPTOR_NOT_CONFIGURED;
    	
    	return UNKNOWN;
    	
    }
    
    @Override
	public String toString() {
    	if (getStatus() == '1')
    		return "ENCRYPTOR_ERROR";
    	
    	if (getStatus() == '2')
    		return "ENCRYPTOR_NOT_CONFIGURED";
    	
    	return "UNKNOWN";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getStatus();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EncryptorStatus other = (EncryptorStatus) obj;
		if (getStatus() != other.getStatus())
			return false;
		return true;
	}
	
}
