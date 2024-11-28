package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.KeyManagementMode;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ifx_key_mng")
public class KeyManagement implements IEntity<Long>, Cloneable {
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="keymanagement-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "keymanagement-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "keymanagement_seq")
    				})
	private Long id;
	
    @Column(length=32, name = "key_key")
    private String key;
    
    @Column(length=4)
    private String CheckDigit;
    
    @Column(length=6)
    private String KeyType;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "mode_type"))
    })
    private KeyManagementMode mode; 
    
    
    private String digits;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "netmng_code"))
    })
    private NetworkManagementInfo networkManagementInformationCode;
    
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getCheckDigit() {
		return CheckDigit;
	}

	public void setCheckDigit(String checkDigit) {
		CheckDigit = checkDigit;
	}

	public String getKeyType() {
		return KeyType;
	}

	public void setKeyType(String keyType) {
		KeyType = keyType;
	}

	public KeyManagementMode getMode() {
		return mode;
	}

	public void setMode(KeyManagementMode mode) {
		this.mode = mode;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDigits() {
		return digits;
	}
	
	public void setDigits(String digits) {
		this.digits = digits;
	}
	
	
	protected Object clone() {
		KeyManagement obj = new KeyManagement();
		obj.setKey(key);
		obj.setMode(mode);
		obj.setKeyType(KeyType);
		obj.setCheckDigit(CheckDigit);
		obj.setDigits(digits);
		obj.setNetworkManagementInformationCode(networkManagementInformationCode);
		return obj;
	}
	
	
	public KeyManagement copy() {
		return (KeyManagement) clone();
	}

	public void setNetworkManagementInformationCode(NetworkManagementInfo networkManagementInformationCode) {
		this.networkManagementInformationCode = networkManagementInformationCode;
	}

	public NetworkManagementInfo getNetworkManagementInformationCode() {
		return networkManagementInformationCode;
	}
	
}
