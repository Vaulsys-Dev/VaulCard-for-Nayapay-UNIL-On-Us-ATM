package vaulsys.terminal.impl;

import vaulsys.entity.impl.Organization;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.TerminalClearingMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_thirdparty")
@ForeignKey(name = "thirdparty_terminal_fk")
public class ThirdPartyVirtualTerminal extends Terminal {
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	@ForeignKey(name = "thirdparty_owner_fk")
	private Organization owner;
	
	@Column(name = "owner", insertable = false, updatable = false)
	private Long ownerId;

	public Long getOwnerId() {
		return ownerId;
	}


	public ThirdPartyVirtualTerminal() {
	}

	public ThirdPartyVirtualTerminal(Long code) {
		super(code);
	}

	@Override
	public Organization getOwner() {
		return owner;
	}

	@Override
	public TerminalType getTerminalType() {
		return TerminalType.THIRDPARTY;
	}

	public void setOwner(Organization owner) {
		this.owner = owner;
	}

	public TerminalClearingMode getClearingMode() {
		return TerminalClearingMode.THIRDPARTY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		return result;
	}
	
}