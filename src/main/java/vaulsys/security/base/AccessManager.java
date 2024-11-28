package vaulsys.security.base;

import vaulsys.config.ConfigurationManager;
import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureDESKey;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AccessManager {

    private Logger logger = Logger.getLogger(AccessManager.class);

    private static Map<String, Driver> deviceDrivers;

    private final static String configFile = "Drivers";

    public static void initiate() throws Exception {
        Configuration config = ConfigurationManager.getInstance().getConfiguration(configFile);
        String[] fileName = config.getStringArray("Driver/ConfigFile");
        for (String fn : fileName) {
            Driver driver = loadDrivers(fn);
            AccessManager.addDeviceDriver(config.getString("Driver[ConfigFile='" + fn + "']/Name"), driver);
        }
    }

    @SuppressWarnings("unchecked")
    public static Driver loadDrivers(String configFile) throws ClassNotFoundException, Exception, NoSuchMethodException {
        Configuration config = ConfigurationManager.getInstance().getConfiguration(configFile);
        String className = config.getString("Driver/Class");
        Class c = Class.forName(className);
        Method getInstance = c.getMethod("getInstance");
        // getInstance.invoke(c);
        return (Driver) getInstance.invoke(c);
    }

    public synchronized static void addDeviceDriver(String name, Driver driver) {
        if (deviceDrivers == null) {
            deviceDrivers = new HashMap<String, Driver>();
        }
        deviceDrivers.put(name, driver);
    }

    public static Driver getDeviceDriver(String name) {
        if (deviceDrivers.containsKey(name)) {
            return deviceDrivers.get(name);
        } else {
            return null;
        }
    }

    public SecureDESKey generateKey(short keyLength, String keyType, String destinationName) throws SMException {

        Driver driver = AccessManager.getDeviceDriver(destinationName);
        if (driver == null) {
            logger.error("No proper driver was found for " + destinationName);
            return null;
        }

        SecureDESKey secureDESKey = driver.generateKey(keyLength, keyType);
        return secureDESKey;
    }

    public byte[] getKey(String destinationName, String keyIndex) throws Exception {
        if (destinationName.equals(KeyStore.DB.toString())) {
            return KeyManager.getInstance().loadStoreKey(destinationName + "[" + keyIndex + "]");
        } else {
            Driver driver = getDeviceDriver(destinationName);
            //TODO
        }

        return null;
    }

    public byte[] generateMAC(int keyIndex, byte[] keyData, byte[] data, int algorithm, byte[] iv, int macLength, int padding, String destinationName, byte[] masterKey) {
        Driver driver = AccessManager.getDeviceDriver(destinationName);
        if (driver == null) {
            logger.error("No proper driver was found for " + destinationName);
            return null;
        }

        return driver.generateMAC(keyIndex, keyData, data, algorithm, iv, macLength, padding, masterKey);
    }

    public boolean verifyMAC(int keyIndex, byte[] keyData, byte[] data, byte[] mac, int algorithm, byte[] iv, int padding, String destinationName, byte[] masterKey) {
        Driver driver = AccessManager.getDeviceDriver(destinationName);
        if (driver == null) {
            logger.error("No proper driver was found for " + destinationName);
            return false;
        }

        return driver.verifyMAC(keyIndex, keyData, data, mac, algorithm, iv, padding, masterKey);
    }

    public byte[] translatePIN(byte[] inputPinBlock, int inputIndex, byte[] inputKeySpec, byte PFi, String AccountNumberBlock, byte PFo, int outputIndex, byte[] outputKeySpec, String destinationName, byte[] masterKey) {
        Driver driver = AccessManager.getDeviceDriver(destinationName);
        if (driver == null) {
            logger.error("No proper driver was found for " + destinationName);
            return null;
        }
        return driver.translatePIN(inputPinBlock, inputIndex, inputKeySpec, PFi, AccountNumberBlock, PFo, outputIndex, outputKeySpec, masterKey);
    }


    public byte[] decrypt(byte[] keyData, int keyIndex, int mode, byte[] iv, byte[] data, int padding, String destinationName) {
        Driver driver = AccessManager.getDeviceDriver(destinationName);
        if (driver == null) {
            logger.error("No proper driver was found for " + destinationName);
            return null;
        }
        return driver.decrypt(keyData, keyIndex, mode, iv, data, padding);
    }

    public byte[] encrypt(byte[] keyData, int keyIndex, int mode, byte[] iv, byte[] data, int padding, String destinationName) {
        Driver driver = AccessManager.getDeviceDriver(destinationName);
        if (driver == null) {
            logger.error("No proper driver was found for " + destinationName);
            return null;
        }
        return driver.encrypt(keyData, keyIndex, mode, iv, data, padding);
    }

    public byte[] importKey(int masterIndex, byte[] masterKey, int mode, int type, byte[] key, String destinationName) {
        Driver driver = AccessManager.getDeviceDriver(destinationName);
        if (driver == null) {
            logger.error("No proper driver was found for " + destinationName);
            return null;
        }
        return driver.KEY_IMPORT(masterIndex, masterKey, mode, type, key);
    }


}
