package vaulsys.security.base;

import javax.persistence.Embeddable;

@Embeddable
public class KeyStore {


    private static final byte DB_VALUE = 1;
    private static final byte ESM_VALUE = 2;

    public static final KeyStore DB = new KeyStore(DB_VALUE);
    public static final KeyStore ESM = new KeyStore(ESM_VALUE);

    private byte type;

    public KeyStore() {
    }

    public KeyStore(byte type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyStore keyStore = (KeyStore) o;

        if (type != keyStore.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
}
