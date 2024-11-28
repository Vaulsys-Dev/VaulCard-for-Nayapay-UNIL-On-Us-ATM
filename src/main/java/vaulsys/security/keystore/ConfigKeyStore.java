package vaulsys.security.keystore;

import vaulsys.config.ConfigurationManager;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.security.securekey.SecureDESKey;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class ConfigKeyStore implements SecureKeyStore {

    private static ConfigKeyStore configKeyStore;
    private Logger logger;
    private Configuration securityConfig;

    public ConfigKeyStore() {
        // TODO Auto-generated constructor stub
        logger = Logger.getLogger(ConfigKeyStore.class);
    }

    synchronized public static ConfigKeyStore getInstance() {
        if (configKeyStore == null)
            configKeyStore = new ConfigKeyStore();
        return configKeyStore;
    }

    public SecureDESKey getKey(int index, String keyType) throws SecureKeyStoreException {

        String queryString = "keys/key[@index = '" + index + "'][@keyType ='" + keyType + "']";
        securityConfig = ConfigurationManager.getInstance().getConfiguration("security");
        if (securityConfig.containsKey(queryString + "/value")) {
            String keyValue = securityConfig.getString(queryString + "/value");
            String keyLength = securityConfig.getString(queryString + "/length");
            SecureDESKey secureKey = new SecureDESKey(Short.parseShort(keyLength), keyType, ISOUtil.hex2byte(keyValue));
            return secureKey;
        }
        return null;
    }

    public void setKey(int index, SecureDESKey key) throws SecureKeyStoreException {
        String queryString = "keys.key[@index = '" + index + "'][@keyType ='" + key.getKeyType() + "']";
        securityConfig = ConfigurationManager.getInstance().getConfiguration("security");

        if (securityConfig.containsKey(queryString + "/value")) {
            securityConfig.setProperty(queryString + " value", key.getKeyBytes());
            securityConfig.setProperty(queryString + " length", key.getKeyLength());
        } else {
            securityConfig.addProperty("keys.key @index", index);
            securityConfig.addProperty("keys.key[@index = '" + index + "'] @keyType", key.getKeyType());
            securityConfig.setProperty(queryString + " value", key.getKeyBytes());
            securityConfig.setProperty(queryString + " length", key.getKeyLength());
        }
    }

}
