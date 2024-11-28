package vaulsys.terminal.atm;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ndc.constants.NDCPrinterFlag;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "term_atm_receipt")
public class Receipt implements IEntity<Integer> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Integer id;
    private String name;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "printer_flag"))
    })
    private NDCPrinterFlag printerFlag;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "user_lang"))
    })
    private UserLanguage language;
    
    @Column(length=2000)
    private String text;

    @Transient
    private String replacedText;

    public String getReplacedText() {
		return replacedText;
	}

	public void setReplacedText(String replacedText) {
		this.replacedText = replacedText;
	}

    public Receipt() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public NDCPrinterFlag getPrinterFlag() {
        return printerFlag;
    }

    public void setPrinterFlag(NDCPrinterFlag printerFlag) {
        this.printerFlag = printerFlag;
    }

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public UserLanguage getLanguage() {
		return language;
	}

	public void setLanguage(UserLanguage language) {
		this.language = language;
	}
	
	@Override
	public String toString() {
		return name!=null ? name:"";
	}
}
