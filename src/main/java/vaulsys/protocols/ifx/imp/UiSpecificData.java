package vaulsys.protocols.ifx.imp;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TerminalType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ifx_ui_specific")
public class UiSpecificData implements IEntity<Long> {

	@Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="uispecificdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "uispecificdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "uispecificdata_seq")
    				})
	private Long id;

	private String username;

	@Column(name = "terminal_codes")
	private String terminalCodes;
	
	private TerminalType terminalType;

	@Override
	public Long getId() {
		return id;
	}
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getTerminalCodes() {
		return terminalCodes;
	}
	public void setTerminalCodes(String terminalCodes) {
		this.terminalCodes = terminalCodes;
	}

	public TerminalType getTerminalType() {
		return terminalType;
	}
	public void setTerminalType(TerminalType terminalType) {
		this.terminalType = terminalType;
	}
}
