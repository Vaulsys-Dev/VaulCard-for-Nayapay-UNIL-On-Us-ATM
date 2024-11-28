package vaulsys.terminal.atm.device;

import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Printer")
public class Printer extends ATMDevice {

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "paper_status"))
    })
    private NDCSupplyStatusConstants paperStatus;
   
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "ribbon_status"))
    })
    private NDCSupplyStatusConstants ribbonStatus;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "printhead_status"))
    })
    private NDCSupplyStatusConstants printheadStatus;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "knife_status"))
    })
    private NDCSupplyStatusConstants knifeStatus;

    public Printer() {
    }

	public NDCSupplyStatusConstants getPaperStatus() {
		return paperStatus;
	}

	public void setPaperStatus(NDCSupplyStatusConstants paperStatus) {
		this.paperStatus = paperStatus;
	}

	public NDCSupplyStatusConstants getRibbonStatus() {
		return ribbonStatus;
	}

	public void setRibbonStatus(NDCSupplyStatusConstants ribbonStatus) {
		this.ribbonStatus = ribbonStatus;
	}

	public NDCSupplyStatusConstants getPrintheadStatus() {
		return printheadStatus;
	}

	public void setPrintheadStatus(NDCSupplyStatusConstants printheadStatus) {
		this.printheadStatus = printheadStatus;
	}

	public NDCSupplyStatusConstants getKnifeStatus() {
		return knifeStatus;
	}

	public void setKnifeStatus(NDCSupplyStatusConstants knifeStatus) {
		this.knifeStatus = knifeStatus;
	}
}
