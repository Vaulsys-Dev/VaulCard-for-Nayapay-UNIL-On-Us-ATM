package vaulsys.security.securekey;

import javax.persistence.Entity;
import javax.persistence.Column;

/**
 * Represents an HSM-stored secure DES key. The key is specified with an {@link
 * HSMStoredSecureDESKey#getIndex() index}. The {@link SecureKey#getKeyBytes() keyBytes} property of
 * the instance will be <code>null</code>.
 */
@Entity
public class HSMStoredSecureDESKey extends SecureDESKey {

    @Column(name = "num")
    private int index;

    public HSMStoredSecureDESKey() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public HSMStoredSecureDESKey(short keyLength, String keyType, int index) {
        super(keyLength, keyType, (byte[]) null);
        this.index = index;
    }

}
