package vaulsys.security.hsm.eracom.base;

import vaulsys.util.encoders.Hex;

public class FunctionCode {

    //HSM Status Functions
    public static byte[] HSM_STATUS = new byte[]{(byte) 0x01};
    public static byte[] HSM_ERRORLOG_STATUS = new byte[]{(byte) 0xFF, (byte) 0xF0, (byte) 0x00};
    public static byte[] HSM_GET_ERRORLOG = new byte[]{(byte) 0xFF, (byte) 0xF1, (byte) 0x00};


    // MAC Management
    public static byte[] MAC_GEN_UPDATE = new byte[]{(byte) 0xEE, (byte) 0x07, (byte) 0x00};
    public static byte[] MAC_GEN_FINAL = new byte[]{(byte) 0xEE, (byte) 0x07, (byte) 0x01};
    public static byte[] MAC_VER_FINAL = new byte[]{(byte) 0xEE, (byte) 0x07, (byte) 0x02};

    //Data Ciphering
    public static byte[] ENCIPHER_2 = new byte[]{(byte) 0xEE, (byte) 0x08, (byte) 0x00};
    public static byte[] DECIPHER_2 = new byte[]{(byte) 0xEE, (byte) 0x08, (byte) 0x01};
    public static byte[] ENCIPHER_3 = new byte[]{(byte) 0xEE, (byte) 0x08, (byte) 0x04};
    public static byte[] DECIPHER_3 = new byte[]{(byte) 0xEE, (byte) 0x08, (byte) 0x05};
    public static byte[] ENCIPHER_KTM1 = new byte[]{(byte) 0xEE, (byte) 0x08, (byte) 0x06};
    public static byte[] B_ENCIPHER_ECB = new byte[]{84};
    public static byte[] B_DECIPHER_ECB = new byte[]{85};

    //PIN Management
    public static byte[] CLR_PIN_ENCRYPT = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x00};
    public static byte[] MIGRATEPIN = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x01};
    public static byte[] PIN_TRAN_2 = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x02};
    public static byte[] PIN_VER_IBM_MULTI = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x03};
    public static byte[] PIN_TRAN_3624 = new byte[]{(byte) 0x63};
    public static byte[] KB_PIN_VER = new byte[]{(byte) 0x64};
    public static byte[] VAR_KB_PIN_VER = new byte[]{(byte) 0x69};
    public static byte[] PIN_OFF = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x04};
    public static byte[] PIN_FROM_OFF = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x09};
    public static byte[] Generate_KM_encrypted_PIN = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x40};
    public static byte[] Print_a_KM_encrypted_PIN = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x41};
    public static byte[] Verify_a_PIN_Using_KM_encrypted_PIN = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x42};
    public static byte[] Translate_a_PIN_from_PPK_to_LMK = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x43};
    public static byte[] Migrate_PIN = new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x44};
    public static byte[] IT_PVK_EXPORT = new byte[]{(byte) 0xEF, (byte) 0x02, (byte) 0x10};

    //Transfer Functions
    public static byte[] Retrieve_Key = Hex.decode("21");
    public static byte[] Store_Key = Hex.decode("22");
    public static byte[] KEY_IMPORT = Hex.decode("EE0200");
    public static byte[] KEY_EXPORT = Hex.decode("EE0201");
    public static byte[] Get_Key_Details = Hex.decode("EE0202");

    // Visa Functions
    public static byte[] PVV_VER = Hex.decode("EE0605");
    public static byte[] PVV_CALC_3624 = Hex.decode("EE0606");
    public static byte[] PVV_CALC = Hex.decode("EE0607");
    public static byte[] DIEBOLD_PIN_VER = Hex.decode("EE0614");
    public static byte[] PIN_TRANS_SEED_DES = Hex.decode("EE0615");
    public static byte[] CVV_GENERATE = Hex.decode("EE0802");
    public static byte[] CVV_VERIFY = Hex.decode("EE0803");


    //Terminal Management Functions
    public static byte[] IT_KEY_GEN = Hex.decode("EE0400");
    public static byte[] NT_KEY_GEN = Hex.decode("EE0401");
    public static byte[] TERM_VER_2 = Hex.decode("EE0406");
    public static byte[] BDK_GEN = Hex.decode("EE0408");


    //Interchane Functions
    public static byte[] II_KEY_GEN = Hex.decode("EE0402");
    public static byte[] II_KEY_RCV = Hex.decode("EE0403");
    public static byte[] NI_KEY_GEN = Hex.decode("EE0404");
    public static byte[] NI_KEY_RCV = Hex.decode("EE0405");
}