package vaulsys.security.component;

import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.security.base.KeyStore;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.base.StoredKey;
import vaulsys.security.exception.SMException;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.util.encoders.Hex;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;

/**
 * @author Ali Lahijani
 */
public class KeySetManager {

    private static GeneralDao generalDao;

    public static SecureDESKey lookup(Map<String, String> keySet, String keyType) throws IllegalArgumentException {
        String storeInfo = keySet.get(keyType);
        if (storeInfo == null) {
//            throw new IllegalArgumentException("Unknown key type: " + keyType + ", should be one of " + keySet.keySet());
            return null;
        }
        StoredKey storedKey = lookupStoreInfo(storeInfo);
        if (storedKey == null) {
            return null;
        }
        byte[] keyBytes = storedKey.getStoredKey();
        return new SecureDESKey((short) (keyBytes.length * 8), translateForJPos(keyType), keyBytes);
    }

    public static StoredKey lookupStoreInfo(String storeInfo)
            throws ObjectNotFoundException {
        int bracket = storeInfo.indexOf("[");
        String index = storeInfo.substring(bracket + 1, storeInfo.indexOf("]"));
        String store = storeInfo.substring(0, bracket);

        StoredKey sk =
        	GeneralDao.Instance.getObject(StoredKey.class, Long.valueOf(index));
//                    session.session.getNamedQuery("findStoredKey")
//                            .setParameter("store", KeyStore.valueOf(store))
//                            .setParameter("index", Integer.valueOf(index))
//                            .uniqueResult();
//            if (sk == null) {
//                throw new IllegalArgumentException("No such key was found: " + storeInfo);
//            }

        return sk;
    }

    /**
     * @param keySet      the keySet to add the created storeInfo string to
     * @param importedKey the SecureDESKey from which to retrieve and save the {@link
     *                    SecureKey#getKeyBytes() keyBytes}
     */
    public static void store(Map<String, String> keySet, SecureDESKey importedKey) {
        String storeInfo = assignStoreInfo(importedKey);
        keySet.put(translateFromJPos(importedKey.getKeyType()), storeInfo);
    }

    public static String assignStoreInfo(SecureDESKey importedKey) {
        StoredKey newKey = new StoredKey(KeyStore.DB, -1, importedKey.getBKeyBytes());
        GeneralDao.Instance.save(newKey);
        return newKey.getStoreInfo();
    }

    public static Map<String, String> createKeySet(SecurityProfile profile) throws SMException {
        // todo: don't just use the default SecurityModule, use the one specified in the SecurityProfile
//        SMAdapter adapter = SecurityComponent.getDefaultSecurityModule();

//        the generateKey approach does not work, the HSM does not support that concept, we need
//            to rephrase it in terms of a master key generation phase (with mailer prints) and
//            subsequent PPK and MPK generation commands

        SecureDESKey ppk = SecurityComponent.generateKey((short) (8 * 8), translateForJPos("PPK"));
        SecureDESKey mpk = SecurityComponent.generateKey((short) (8 * 8), translateForJPos("MPK"));
        // SecureDESKey kis = adapter.generateKey((short) 8, "KIS");

        HashMap<String, String> keySet = new HashMap<String, String>();
        store(keySet, mpk);
        store(keySet, ppk);
        // store(keySet, kis);
        return keySet;
    }

    public static void main(String[] args) throws SMException {
        createKeySet(null);
    }
    /**
     * the keySet should be updated, since StoredKey indices will change. So this method returns the
     * new keySet. The plain keys should be subsequently deleted from the database
     *
     * @param keySet
     * @param importedStoreInfos maps storeInfos for all imported StoredKeys to their respective
     *                           imported StoredKeys. Will be augmented with the values from keySet
     * @param adapter
     * @param checkParity
     * @return
     * @throws Exception
     */
    public static Map<String, String> importAllPlain(Map<String, String> keySet, Map<String, String> importedStoreInfos, SMAdapter adapter, boolean checkParity) throws Exception {
        if (keySet == null) return null;


        Map<String, String> newKeySet = new HashMap<String, String>();
        for (String keyType : keySet.keySet()) {
            String storeInfo = keySet.get(keyType);
            if (importedStoreInfos.containsKey(storeInfo)) {
                newKeySet.put(keyType, importedStoreInfos.get(storeInfo));
                continue;
            }

            if ("KIS".equals(keyType)) continue; // todo more logic goes here than just a continue!

            try {
                StoredKey storedKey = lookupStoreInfo(storeInfo);
                byte[] keyBytes = storedKey.getStoredKey();
                SecureDESKey importedKey = adapter.importKey((short) (keyBytes.length * 8), translateForJPos(keyType), keyBytes, null, checkParity);
                String newStoreInfo = assignStoreInfo(importedKey);
                newKeySet.put(keyType, newStoreInfo);
                importedStoreInfos.put(storeInfo, newStoreInfo);
            } catch (HibernateException e) {/*ignore*/}
        }


        return newKeySet;
    }

    private static Map<String, String> translatorForJPos = new HashMap<String, String>();
    private static Map<String, String> translatorFromJPos = new HashMap<String, String>();

    static {
        translatorForJPos.put(KeyType.TYPE_TAK, KeyType.TYPE_TAK);
        translatorForJPos.put(KeyType.TYPE_TPK, KeyType.TYPE_TPK);
        translatorForJPos.put("KIS", KeyType.TYPE_TMK);

        translatorFromJPos.put(KeyType.TYPE_TAK, KeyType.TYPE_TAK);
        translatorFromJPos.put(KeyType.TYPE_TPK, KeyType.TYPE_TPK);
        translatorFromJPos.put(KeyType.TYPE_TMK, "KIS");
    }

    public static String translateForJPos(String keyType) {
        String translated = translatorForJPos.get(keyType);
        if (translated == null) {
            return keyType;
        }

        return translated;
    }

    private static String translateFromJPos(String keyType) {
        String translated = translatorFromJPos.get(keyType);
        if (translated == null) {
            return keyType;
        }

        return translated;
    }

    static byte[] xor(byte[] clearComponent1, byte[] clearComponent2, byte[] clearComponent3) {
        return ISOUtil.xor(ISOUtil.xor(clearComponent1, clearComponent2), clearComponent3);
    }

/*
    public static void main(String[] args) throws Throwable {
        importAllTerminalKeys();
    }
*/

    private static void set30L31L() throws Throwable {
        try {
            StoredKey mpk = GeneralDao.Instance.getObject(StoredKey.class, 30L);
            mpk.setStoredKey(Hex.decode("1DB3B0A7E43C0728"));     // hasin Mac key for serialNo 1030000127, encrypted under HSM MK
            GeneralDao.Instance.update(mpk);

            StoredKey ppk = GeneralDao.Instance.getObject(StoredKey.class, 31L);
            ppk.setStoredKey(Hex.decode("DA8E1177B055BB56"));    // hasin Pin key for serialNo 1030000127, encrypted under HSM MK
            GeneralDao.Instance.update(ppk);
        } catch (Throwable e) {
            throw e;
        }
    }
}
