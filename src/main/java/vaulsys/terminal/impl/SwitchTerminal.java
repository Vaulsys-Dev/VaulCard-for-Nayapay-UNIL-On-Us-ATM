package vaulsys.terminal.impl;

import vaulsys.entity.impl.Institution;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.SwitchTerminalType;
import vaulsys.terminal.TerminalClearingMode;

import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;

import java.util.Set;

@Entity
@Table(name = "term_switch")
@ForeignKey(name="switch_terminal_fk")
public class SwitchTerminal extends Terminal{

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "owner")
	@ForeignKey(name="switch_owner_fk")
    private Institution owner;
    
    @Column(name = "owner", insertable = false, updatable = false)
	private Long ownerId;

	public Long getOwnerId() {
		return ownerId;
	}

    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "type"))
    private SwitchTerminalType type;

    public SwitchTerminal(SwitchTerminalType type) {
        this.type = type;
    }

    public SwitchTerminal() {
    }

    @Override
    public Institution getOwner() {
    	return owner;
    }

    @Override
	public TerminalType getTerminalType() {
		return TerminalType.SWITCH;
	}
    
    public void setOwner(Institution owner) {
        this.owner = owner;
		if (owner != null)
			ownerId = owner.getId();

    }

    public TerminalClearingMode getClearingMode() {
        switch (type.getType()) {
            case SwitchTerminalType.ACQUIER_VALUE:
                return TerminalClearingMode.ISSUER;
            case SwitchTerminalType.ISSUER_VALUE:
                return TerminalClearingMode.ACQUIER;
        }
        return TerminalClearingMode.TERMINAL;
    }

	public SwitchTerminalType getType() {
		return type;
	}

	public void setType(SwitchTerminalType type) {
		this.type = type;
	}

}
