package vaulsys.security.keystore;

import vaulsys.security.securekey.SecureDESKey;

/**
 * <p>
 * Represents a collection of Secure Keys and typically stores them in some
 * persistent storage. SecureKeyStore isolates from particular DB implementations.
 * A Secure Key Store need not implement any security itself, it just holds keys
 * that are inherently secure (like SecureDESKey).
 * </p>
 */
public interface SecureKeyStore {
    public static class SecureKeyStoreException extends Exception { //ISOException {

        private static final long serialVersionUID = 1976885367352075834L;

        public SecureKeyStoreException() {
            super();
        }

        public SecureKeyStoreException(String detail) {
            super(detail);
        }

        public SecureKeyStoreException(Exception nested) {
            super(nested);
        }

        public SecureKeyStoreException(String detail, Exception nested) {
            super(detail, nested);
        }
    }


    /**
     * returns the key associated with the given alias
     *
     * @param alias the alias name
     * @return the requested key, or null if the given alias does not exist.
     * @throws SecureKeyStoreException if SecureKeyStore is not initialized or if
     *                                 the operation fails for some other reason.
     */
    public SecureDESKey getKey(int index, String keyType) throws SecureKeyStoreException;


    /**
     * Assigns the given key to the given alias.
     * If the given alias already exists, the keystore information associated
     * with it is overridden by the given key.
     *
     * @param alias the alias name
     * @param key   the key to be associated with the alias
     * @throws SecureKeyStoreException if SecureKeyStore is not initialized or the key
     *                                 can't be recovered.
     */
    public void setKey(int index, SecureDESKey key) throws SecureKeyStoreException;


}



