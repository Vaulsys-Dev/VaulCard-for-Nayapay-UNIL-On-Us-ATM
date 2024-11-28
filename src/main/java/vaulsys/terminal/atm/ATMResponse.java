package vaulsys.terminal.atm;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.UserLanguage;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm_response")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class ATMResponse implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;
    
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "term_atm_response__screen", 
		joinColumns = { @JoinColumn(name = "response") },
		inverseJoinColumns = { @JoinColumn(name = "screen") })
	@ForeignKey(name = "atmres_scrresponse_fk", inverseName = "atmres_screen_fk")
	private List<ResponseScreen> screen;
    
    public ATMResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
	public List<ResponseScreen> getScreen() {
		if (this.screen == null)
			this.screen = new ArrayList<ResponseScreen>();
		return this.screen;
	}

	public ResponseScreen getScreen(UserLanguage lang) {
		List<ResponseScreen> result = new ArrayList<ResponseScreen>();
		if (this.screen == null)
			this.screen = new ArrayList<ResponseScreen>();

		for (ResponseScreen scr : this.screen) {
			if (scr.getLanguage() == null || lang.equals(scr.getLanguage()))
				return scr;
		}

		return null;
	}

	public void setScreen(List<ResponseScreen> screen) {
		this.screen = screen;
	}

	public void addScreen(ResponseScreen screen) {
		if (this.screen == null)
			this.screen = new ArrayList<ResponseScreen>();
		this.screen.add(screen);
	}
	
    @Override
    public String toString() {
    	return String.format("%s, %s", id, name != null ? name : "-");
    }
}
