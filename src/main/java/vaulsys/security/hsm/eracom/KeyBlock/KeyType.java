package vaulsys.security.hsm.eracom.KeyBlock;

public class KeyType {

    public enum keys {
        DPK,
        PPK,
        MPK,
        KTM,
        TAK
    }

    ;

    public static final int DPK = 0;
    public static final int PPK = 1;
    public static final int MPK = 2;
    public static final int KTM = 5;
    public static final int TAK = 6;

}
