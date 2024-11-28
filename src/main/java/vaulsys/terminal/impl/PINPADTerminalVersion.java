package vaulsys.terminal.impl;

import vaulsys.entity.impl.Branch;
import vaulsys.entity.impl.Shop;
import vaulsys.terminal.POSConnectionType;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "term_pinpad_ver")
public class PINPADTerminalVersion extends TerminalVersion {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	@ForeignKey(name = "pinpad_vers_owner_fk")
	private Branch owner;

	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "pinpad_vers_parent_fk")
	private PINPADTerminal parent;


	@Column(length = 100)
	private String description;

	@Column(length = 10)
	private String serialno;

	@Column(length = 4)
	String resetCode;

	@Column(length = 20, name = "reg_num")
	private String registrationNumber;
	
	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "contype"))
	POSConnectionType connectionType;

	public Branch getOwner() {
		return owner;
	}

	public void setOwner(Branch owner) {
		this.owner = owner;
	}

	public PINPADTerminal getParent() {
		return parent;
	}

	public void setParent(PINPADTerminal parent) {
		this.parent = parent;
	}

//    public IVersion clone() {
//        POSTerminalVersion version = new POSTerminalVersion();
//        version.validRange = validRange.clone();
//        version.description = description;
//        version.serialno = serialno;
//        version.resetCode = resetCode;
//        version.parent = parent;
////        version.status = status;
//        return version;
//    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSerialno() {
		return serialno;
	}

	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}

	public String getResetCode() {
		return resetCode;
	}

	public void setResetCode(String resetCode) {
		this.resetCode = resetCode;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public POSConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(POSConnectionType connectionType) {
		this.connectionType = connectionType;
	}
}
