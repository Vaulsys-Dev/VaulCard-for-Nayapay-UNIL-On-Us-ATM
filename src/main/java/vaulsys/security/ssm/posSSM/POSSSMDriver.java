package vaulsys.security.ssm.posSSM;

import vaulsys.config.ConfigurationManager;
import vaulsys.security.base.SSMDriver;
import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureDESKey;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.util.concurrent.Semaphore;

public class POSSSMDriver implements SSMDriver {

    private static POSSSMDriver fanapSSMDriver;

    public static int MAX_NUMBER_OF_CONNECTION = 1;

    private static String NAME = "Fanap SSM Driver";

    private final Semaphore available = new Semaphore(MAX_NUMBER_OF_CONNECTION, true);

    private Logger logger = Logger.getLogger(POSSSMDriver.class);

    public static POSSSMDriver getInstance() {
        if (fanapSSMDriver == null) {
            POSSSMDriver.configDriver();
            fanapSSMDriver = new POSSSMDriver();
        }
        return fanapSSMDriver;
    }

    public static void configDriver(/* String configFile */) {
        Configuration config = ConfigurationManager.getInstance().getConfiguration("FSM_Driver");
        POSSSMDriver.NAME = config.getString("Driver/Name");
        POSSSMDriver.MAX_NUMBER_OF_CONNECTION = config.getInt("Driver/NumberOfConnection");
    }

    public static String getName() {
        return NAME;
    }

    private POSSSMDriver() {
    }

    @Override
    public byte[] generateMAC(int index, byte[] keyData, byte[] data, int algorithm, byte[] IV, int macLength,
                              int padding, byte[] masterKey) {
        try {
            available.acquire();
            POSSSM fsm = new POSSSM();
            byte[] mac = fsm.generateMAC(index, keyData, data, algorithm, IV, macLength, padding, masterKey);
            available.release();
            return mac;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean verifyMAC(int keyIndex, byte[] keyData, byte[] data, byte[] mac, int algorithm, byte[] IV,
                             int padding, byte[] masterKey) {
        try {
            available.acquire();
            available.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public byte[] translatePIN(byte[] inputPinBlock, int inputIndex, byte[] inputKey, byte PFi,
                               String AccountNumberBlock, byte PFo, int outputIndex, byte[] outputKey, byte[] masterKey) {
        try {
            available.acquire();
            POSSSM fsm = new POSSSM();
            byte[] pin = fsm.translatePIN(inputPinBlock, inputIndex, inputKey, PFi, AccountNumberBlock, PFo, outputIndex, outputKey, masterKey);
            available.release();
            return pin;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SecureDESKey generateKey(short keyLength, String keyType) throws SMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] decrypt(byte[] keyData, int index, int mode, byte[] iv, byte[] data, int padding) {
        try {
            available.acquire();
            POSSSM fsm = new POSSSM();
            byte[] decryptedData = fsm.decrypt(data, keyData);
            available.release();
            return decryptedData;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] encrypt(byte[] keyData, int index, int mode, byte[] iv, byte[] data, int padding) {
        try {
            available.acquire();
            POSSSM fsm = new POSSSM();
            byte[] encryptedData = fsm.encrypt(data, keyData);
            return encryptedData;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] KEY_IMPORT(int encryptingKeyIndex, byte[] encryptingKeyData, int mode, int type, byte[] keyData) {
        try {
            available.acquire();
            POSSSM fsm = new POSSSM();
            byte[] hostStoredKey = fsm.encrypt(keyData, encryptingKeyData);
            return hostStoredKey;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
