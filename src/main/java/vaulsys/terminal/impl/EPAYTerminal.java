package vaulsys.terminal.impl;

import vaulsys.contact.Website;
import vaulsys.entity.impl.Shop;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.TerminalClearingMode;

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
@Table(name = "term_epay")
@ForeignKey(name="epay_terminal_fk")
public class EPAYTerminal extends Terminal {

	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "owner")
    @ForeignKey(name="epay_owner_fk")
    private Shop owner;

	@Column(name = "owner", insertable = false, updatable = false)
	private Long ownerId;
	
	public Long getOwnerId() {
		return ownerId;
	}
	
    
	/******** EPAY Terminal Version Properties ********/
	/******** Start ********/
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
	
	
	
    public String getDescription() {
        return description;
    }

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
    /******** End ********/
	/******** EPAY Terminal Version Properties ********/
	
    
    public EPAYTerminal() {
    }

    public EPAYTerminal(Long code) {
        super(code);
    }

    public String getSafeWebSiteAddress(){
     return ((website == null || website.getWebsiteAddress() == null) ? owner.getSafeWebsiteAddress() :  website.getWebsiteAddress());
    }

    @Override
	public Shop getOwner() {
		return  owner;
	}
    
    @Override
	public TerminalType getTerminalType() {
		return TerminalType.INTERNET;
	}

    public void setOwner(Shop owner) {
        this.owner = owner;
        if(owner!=null)
        	ownerId = owner.getId();
    }

    public TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.TERMINAL;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((IP == null) ? 0 : IP.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((website == null) ? 0 : website.hashCode());
		result = prime * result + ((timeOut == null) ? 0 : timeOut.hashCode());
		result = prime * result + ((allowedMultiPayment == null) ? 0 : allowedMultiPayment.hashCode());
		result = prime * result + ((twoStep == null) ? 0 : twoStep.hashCode());
		return result;
	}

    public boolean isAllowedMultiPayment() {
        return allowedMultiPayment == null ? false : allowedMultiPayment;
    }

    public boolean isTwoStep() {
        return twoStep == null ? false : twoStep;
    }
}