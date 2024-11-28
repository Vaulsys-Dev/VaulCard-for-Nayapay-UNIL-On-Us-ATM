package vaulsys.security.keystore;

import vaulsys.security.securekey.SecureDESKey;

public class HSMKeyStore implements SecureKeyStore {

    private static HSMKeyStore hsmKeyStore;
    private ConfigKeyStore configKeyStore;

    public HSMKeyStore() {
        configKeyStore = new ConfigKeyStore();
    }

    synchronized public static HSMKeyStore getInstance() {
        if (hsmKeyStore == null)
            hsmKeyStore = new HSMKeyStore();

        return hsmKeyStore;
    }

    public SecureDESKey getKey(int index, String keyType)
            throws SecureKeyStoreException {
        // TODO Auto-generated method stub

        return configKeyStore.getKey(index, keyType);
    }

    public void setKey(int index, SecureDESKey key) throws SecureKeyStoreException {
        // TODO Auto-generated method stub
        configKeyStore.setKey(index, key);
    }


}
