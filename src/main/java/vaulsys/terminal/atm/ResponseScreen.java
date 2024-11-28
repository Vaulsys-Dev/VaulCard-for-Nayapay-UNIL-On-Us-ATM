package vaulsys.terminal.atm;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.UserLanguage;

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
@Table(name = "term_atm_response_screen")
public class ResponseScreen implements IEntity<Long> {
    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    @Column(name = "description")
    private String desc;
    
    @Column(name = "screen_no")
    private String screenno;

    private String screenData;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "user_lang"))
    })
    private UserLanguage language;
    
    @Transient
    private String replacedScreenData;

    public String getReplacedScreenData() {
		return replacedScreenData;
	}

	public void setReplacedScreenData(String replacedScreenData) {
		this.replacedScreenData = replacedScreenData;
	}

    public ResponseScreen() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
    
    @Override
    public String toString() {
    	return String.format("%s, %s", id, desc != null ? desc : "-");
    }
    
	public String getScreenData() {
		return screenData;
	}

	public void setScreenData(String screenData) {
		this.screenData = screenData;
	}

	public UserLanguage getLanguage() {
		return language;
	}

	public void setLanguage(UserLanguage language) {
		this.language = language;
	}

	public String getScreenno() {
		return screenno;
	}

	public void setScreenno(String screenno) {
		this.screenno = screenno;
	}
}
