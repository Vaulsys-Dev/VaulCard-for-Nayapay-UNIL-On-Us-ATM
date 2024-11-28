package vaulsys.terminal.atm.customizationdata;

import vaulsys.persistence.IEntity;
import vaulsys.terminal.atm.constants.ATMCustomizationDataType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity
@Table(name = "term_atm_custom_data")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "custom_data", discriminatorType = DiscriminatorType.STRING)
public abstract class ATMCustomizationData implements IEntity<Long> {
	
	transient public Logger logger = Logger.getLogger(this.getClass());
	
	@Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    @Column(name = "num", length=3)
    private String number;

    @Column(length = 108)
    private String value;

    @Column(name="configid")
    private Integer configid;
    
    @Transient
    private ATMCustomizationDataType type;
    
    private String description;
    
    public ATMCustomizationData() {
	}

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public abstract byte[] getValueForDownload();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public abstract ATMCustomizationDataType getType();

	public Integer getConfigid() {
		return configid;
	}

	public void setConfigid(Integer configid) {
		this.configid = configid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
