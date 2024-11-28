package vaulsys.protocols;

import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "protocol_cfg", 
		uniqueConstraints={@UniqueConstraint(columnNames = {"key", "protocol_type"})})
public class ProtocolConfig implements IEntity<Long> {
	@Id
    @GeneratedValue(generator="switch-gen")
	private Long id;
	
	private String key;
	
	private String value;
	
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "type", column = @Column(name = "protocol_type"))})
	private ProtocolType protocolType;
    
	public ProtocolConfig() {
	}
	
	public ProtocolConfig(ProtocolType protocolType, String key, String value) {
		this.protocolType = protocolType;
		this.key = key;
		this.value = value;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ProtocolType getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(ProtocolType protocolType) {
		this.protocolType = protocolType;
	}
}
