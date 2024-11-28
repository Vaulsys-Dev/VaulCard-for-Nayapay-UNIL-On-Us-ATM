package vaulsys.terminal.impl;

import vaulsys.contact.Website;
import vaulsys.entity.impl.Shop;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_epay_ver")
public class EPAYTerminalVersion extends TerminalVersion {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	@ForeignKey(name = "epay_vers_owner_fk")
	private Shop owner;


	@ManyToOne
	@JoinColumn(name = "parent")
	@ForeignKey(name = "epay_vers_parent_fk")
	private EPAYTerminal parent;

	@Column(length = 100)
	private String description;

	private String IP;

	@Embedded
	@AttributeOverrides({
	@AttributeOverride(name = "websiteAddress", column = @Column(name = "website")),
    @AttributeOverride(name = "email", column = @Column(name = "email"))
			})
	private Website website;
	
	@Column(name = "multi_payment")
	private Boolean allowedMultiPayment;
	private Boolean twoStep;
	@Column(name = "time_out")
	private Integer timeOut;
	
	

	public Boolean getAllowedMultiPayment() {
		return allowedMultiPayment;
	}

	public void setAllowedMultiPayment(Boolean allowedMultiPayment) {
		this.allowedMultiPayment = allowedMultiPayment;
	}

	public Boolean getTwoStep() {
		return twoStep;
	}

	public void setTwoStep(Boolean twoStep) {
		this.twoStep = twoStep;
	}

	public Integer getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Integer timeOut) {
		this.timeOut = timeOut;
	}

	public Shop getOwner() {
		return owner;
	}

	public void setOwner(Shop owner) {
		this.owner = owner;
	}

	public EPAYTerminal getParent() {
		return parent;
	}

	public void setParent(EPAYTerminal parent) {
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

	public String getIP() {
		return IP;
	}

	public void setIP(String ip) {
		IP = ip;
	}

	public Website getWebsite() {
		return website;
	}

	public void setWebsite(Website website) {
		this.website = website;
	}
}
