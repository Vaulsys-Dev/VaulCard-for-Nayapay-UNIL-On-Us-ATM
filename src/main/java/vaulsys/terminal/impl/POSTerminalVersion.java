package vaulsys.terminal.impl;

import vaulsys.discount.DiscountProfile;
import vaulsys.entity.impl.Shop;
import vaulsys.terminal.POSConnectionType;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_pos_ver")
public class POSTerminalVersion extends TerminalVersion {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	@ForeignKey(name = "pos_vers_owner_fk")
	private Shop owner;

	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "pos_vers_parent_fk")
	private POSTerminal parent;


	@Column(length = 100)
	private String description;

	@Column(length = 20)
	private String serialno;

	@Column(length = 4)
	String resetCode;

	@Column(length = 20, name = "reg_num")
	private String registrationNumber;
	
	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "contype"))
	POSConnectionType connectionType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dis_prof")
	private DiscountProfile discountProfile;
	
	public Shop getOwner() {
		return owner;
	}

	public void setOwner(Shop owner) {
		this.owner = owner;
	}

	public POSTerminal getParent() {
		return parent;
	}

	public void setParent(POSTerminal parent) {
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

	public DiscountProfile getDiscountProfile() {
		return discountProfile;
	}

	public void setDiscountProfile(DiscountProfile discountProfile) {
		this.discountProfile = discountProfile;
	}
}
