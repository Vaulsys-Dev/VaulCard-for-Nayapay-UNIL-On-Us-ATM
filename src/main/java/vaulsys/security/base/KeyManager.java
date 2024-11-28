package vaulsys.security.base;

import vaulsys.config.ConfigurationManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.keystore.ConfigKeyStore;
import vaulsys.security.keystore.HSMKeyStore;
import vaulsys.security.keystore.KeyStoreTypes;
import vaulsys.security.keystore.SecureKeyStore.SecureKeyStoreException;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class KeyManager {

    private static KeyManager keyManager = null;
    private Configuration securityConfig;
    private Logger logger = Logger.getLogger(KeyManager.class);

    synchronized public static KeyManager getInstance() {
        if (keyManager == null)
            keyManager = new KeyManager();
        return keyManager;
    }

    public SecureDESKey getKey(int id, String keyType) throws SecureKeyStoreException {
        securityConfig = ConfigurationManager.getInstance().getConfiguration("security");
        String storeType;
        if (securityConfig.containsKey("keyStore/key[@id='" + id + "'][@keyType='" + keyType + "']/store")) {
            storeType = securityConfig.getString("keyStore/key[@id='" + id + "'][@keyType='" + keyType + "']/store");
        } else {
            logger.error("the key with id=" + id + ",keyType=" + keyType + "does not exist.");
            throw new SecureKeyStoreException("the key with id=" + id + ",keyType=" + keyType + "does not exist.");
        }

        int index = securityConfig.getInt("keyStore/key[@id='" + id + "'][@keyType='" + keyType + "']/index");
        if (storeType.equals(KeyStoreTypes.HARDWARE_SECURITY_MODULE)) {
            // getKey From HSMKeyStore
            return HSMKeyStore.getInstance().getKey(index, keyType);
        } else if (storeType.equals(KeyStoreTypes.CONFIG_FILE)) {
            // getKey From config file
            return ConfigKeyStore.getInstance().getKey(index, keyType);

        } else if (storeType.equals(KeyStoreTypes.SOFTWARE_SECURITY_MODULE)) {
            // getKey from Database
        }
        return null;
    }

    public boolean setKey(int id, SecureDESKey key) throws SecureKeyStoreException {
        securityConfig = ConfigurationManager.getInstance().getConfiguration("Security");
        String storeType = securityConfig.getString("keyStore/key[@id=" + id + ",@keyType=" + key.getKeyType() + "]/store");
        if (storeType != null) {
            int index = securityConfig.getInt("keyStore/key[@id=" + id + ",@keyType=" + key.getKeyType() + "]/index");
            if (storeType.equals(KeyStoreTypes.HARDWARE_SECURITY_MODULE)) {
                // setKey in HSMKeyStore
                HSMKeyStore.getInstance().setKey(index, key);
            } else if (storeType.equals(KeyStoreTypes.CONFIG_FILE)) {
                // setKey in config
                ConfigKeyStore.getInstance().setKey(index, key);
            } else if (storeType.equals(KeyStoreTypes.SOFTWARE_SECURITY_MODULE)) {
                // setKey in Database
            }
            return true;
        }
        return false;
    }

    public void storeKey(SecureKey key, String storeType) throws SecureKeyStoreException {

        // ConfigurationManager configurationManager =
        // ConfigurationManager.getInstance();
        //
        // if (storeType == KeyStoreTypes.HARDWARE_SECURITY_MODULE) {
        // HSMKeyStore hsmKeyStore = new HSMKeyStore();
        // // setKey in HSMKeyStore
        // } else if (storeType == KeyStoreTypes.CONFIG_FILE) {
        //
        // ConfigKeyStore configKeyStore = new ConfigKeyStore();
        // configKeyStore.setKey(index, key);
        //
        // } else if (storeType == KeyStoreTypes.SOFTWARE_SECURITY_MODULE) {
        // // setKey in Database
        // }
    }

    public boolean removeKey(int id, String keyType) {
        return true;
    }

    public boolean transferKey(int id, String keyType, String destinationKeyStore) {
        return true;
    }

//    public Map<String, String> getKey(String keyType, Terminal originator) {
//        Map<String, String> keyAddress = new HashMap<String, String>();
//        keyAddress.put("KeyAddress", originator.getKeyStoreName(keyType));
//        keyAddress.put("KeyIndex", new Integer(originator.getKeyIndex(keyType))
//                .toString());
//        return keyAddress;
//    }

    public void storeKey(KeyStore store, int index, byte[] key) throws Exception {
        StoredKey sk = new StoredKey(store, index, key);
        GeneralDao.Instance.saveOrUpdate(sk);
    }

    public byte[] loadStoreKey(String address) throws Exception {
        Long index = new Long(address.substring(address.indexOf("[") + 1, address.indexOf("]")));
        StoredKey key = GeneralDao.Instance.getObject(StoredKey.class, index);
        return (key != null) ? key.getStoredKey() : null;
    }
}
