package vaulsys.security.base;

import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureDESKey;

public interface Driver {

    public SecureDESKey generateKey(short keyLength, String keyType) throws SMException;

    public byte[] generateMAC(int keyIndex, byte[] keyData, byte[] data, int algorithm, byte[] IV, int macLength, int padding, byte[] masterKey);

    public boolean verifyMAC(int keyIndex, byte[] keyData, byte[] data, byte[] mac, int algorithm, byte[] IV, int padding, byte[] masterKey);

    public byte[] translatePIN(byte[] inputPinBlock, int inputIndex, byte[] inputKey, byte PFi, String AccountNumberBlock, byte PFo,
                               int outputIndex, byte[] outputKey, byte[] masterKey);

    public byte[] encrypt(byte[] keyData, int index, int mode, byte[] iv, byte[] data, int padding);

    public byte[] decrypt(byte[] keyData, int index, int mode, byte[] iv, byte[] data, int padding);

    public byte[] KEY_IMPORT(int encryptingKeyIndex, byte[] encryptingKeyData, int mode, int type, byte[] keyData);
//	public byte[] KEY_RCV(int encryptingKeyIndex, byte[] encryptingKeyData, int mode, int type, byte[] keyData);
}
