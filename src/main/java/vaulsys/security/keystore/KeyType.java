package vaulsys.security.keystore;

public class KeyType {
    public static final String TYPE_ZMK = "ZMK";
    /**
     * ZPK: Zone PIN Key.
     * <p/>
     * is a DES (or Triple-DES) data-encrypting key which is distributed
     * automatically and is used to encrypt PINs for transfer between
     * communicating parties (e.g. between acquirers and issuers).
     */
    public static final String TYPE_ZPK = "ZPK";

    public static final String TYPE_ZPK_PAS = "ZPK_PAS"; //Raza adding for Passive ZPK

    /**
     * TMK: Terminal Master Key.
     * <p/>
     * is a  DES (or Triple-DES) key-encrypting key which is distributed
     * manually, or automatically under a previously installed TMK. It is
     * used to distribute data-encrypting keys, whithin a local network,
     * to an ATM or POS terminal or similar.
     */
    public static final String TYPE_TMK = "TMK";

    /**
     * TPK: Terminal PIN Key.
     * <p/>
     * is a  DES (or Triple-DES) data-encrypting key which is used
     * to encrypt PINs for transmission, within a local network,
     * between the terminal and the terminal data acquirer.
     */
    public static final String TYPE_TPK = "TPK";

    /**
     * TAK: Terminal Authentication Key.
     * <p/>
     * is a  DES (or Triple-DES) data-encrypting key which is used to
     * generate and verify a Message Authentication Code (MAC) when data
     * is transmitted, within a local network, between the terminal and
     * the terminal data acquirer.
     */
    public static final String TYPE_TAK = "TAK";
//    public static final String TYPE_MPK = "MPK";

    /**
     * PVK: PIN Verification Key.
     * is a  DES (or Triple-DES) data-encrypting key which is used to
     * generate and verify PIN verification data and thus verify the
     * authenticity of a PIN.
     */
    public static final String TYPE_PVK = "PVK";
    public static final String TYPE_PEK = "PEK";

    /**
     * CVK: Card Verification Key.
     * <p/>
     * is similar for PVK but for card information instead of PIN
     */
    //m.rehman
    public static final String TYPE_CVK = "CVK";
    public static final String TYPE_CVKA = "CVKA";
    public static final String TYPE_CVKB = "CVKB";

    /**
     * BDK: Base Derivation Key.
     * is a  Triple-DES key-encryption key used to derive transaction
     * keys in DUKPT (see ANSI X9.24)
     */
    public static final String TYPE_BDK = "BDK";

    public static final String TYPE_AES_KEY = "AES_KEY"; //Raza for WebService Encryption Key
    public static final String TYPE_AES_VALUE = "AES_VALUE"; //Raza for WebService Encryption Key

    //m.rehman: 05-08-2020, list updated ...
    //m.rehman: for pan encryption bc <start>
    public static final String TYPE_AES = "AES";
    public static final String TYPE_RSA_PUBLIC = "RSA_PUBLIC";
    public static final String TYPE_RSA_PRIVATE = "RSA_PRIVATE";
    public static final String TYPE_3DES = "3DES";
    //m.rehman: for pan encryption bc <end>

    public static final String TYPE_IMK_AC = "IMKAC";  //m.rehman: commonly known as mdk
    public static final String TYPE_IMK_SMI = "IMKSMI";    //m.rehman: commonly known as mdk smi
    public static final String TYPE_IMK_SMC = "IMKSMC";    //m.rehman: commonly known as mdk smc
}
