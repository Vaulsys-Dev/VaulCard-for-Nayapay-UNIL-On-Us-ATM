package vaulsys.security.base;

import vaulsys.util.encoders.Hex;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

//@Entity
//@Table(name = "prf_security_stored_key")
public class StoredKey {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    private byte[] storeKey;

    @Column(name = "num")
    private int index;

    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "store"))
    private KeyStore store;


    public StoredKey() {
        index = 0;
    }

    public StoredKey(KeyStore keyStore, int index, byte[] key) {
        this.index = index;
        this.storeKey = key;
        this.store = keyStore;
    }

    public Long getId() {
        return id;
    }

    public byte[] getStoredKey() {
        return storeKey;
    }

    public void setStoredKey(byte[] key) {
        this.storeKey = key;
    }

    public void setStoreKey(String key) {
        this.storeKey = Hex.decode(key);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public KeyStore getStore() {
        return store;
    }

    public void setStore(KeyStore store) {
        this.store = store;
    }

    public String getStoreInfo() {
        return (store == KeyStore.DB) ? store + "[" + id + "]" : store + "[" + index + "]";
    }

    @Override
    public String toString() {
        return (store == KeyStore.DB) ? store + "[" + id + "] = " + storeKey :
                store + "[" + index + "] = " + storeKey;
//		return super.toString(); 
    }

}
