package vaulsys.terminal.atm.device;

import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.base.config.SupplyStatus;
import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Cassette")
public class Cassette extends ATMDevice {

    protected Integer notes;
    
    @Column(name = "notes_dispensed")
    protected Integer notesDispensed;
    
    @Column(name = "notes_rejected")
    protected Integer notesRejected;

    @Column(name = "notes_retracted")
    protected Integer notesRetracted;
    
    @Column(name = "denomination")
    protected Integer denomination;
    
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "code", column = @Column(name = "total_err_severity")))
    private ErrorSeverity totalErrorSeverity;
    
//	@Embedded
//	Currency currency;

    public Cassette() {
    	this.notes = 0;
    	this.notesDispensed = 0;
    	this.notesRejected = 0;
    	this.notesRetracted = 0;
    	this.denomination = 0;
    	this.totalErrorSeverity = ErrorSeverity.UNKNOWN;
    }
    
    public Cassette(Integer denomination, Integer notes, ErrorSeverity errorSeverity, ErrorSeverity totalErrorSeverity, DeviceLocation location) {
    	this.notes = notes;
    	this.notesDispensed = 0;
    	this.notesRejected = 0;
    	this.notesRetracted = 0;
    	this.denomination = denomination;
    	this.totalErrorSeverity = totalErrorSeverity;
    	this.setErrorSeverity(errorSeverity);
    	this.setLocation(location);
    }

    public Integer getNotes() {
        return notes;
    }

    public void setNotes(int notes) {
        this.notes = notes;
    }

    public void decreseNotes(int note) {
	   	notes -= note;
    	notesDispensed += note;
    }

    public void increaseDispensedNotes(int note) {
        notes -= note;
        notesDispensed += note;
    }

    public void increaseRejectedNotes(int note) {
    	notes -= note;
    	notesRejected += note;
    }
    
    public void increaseRetractedNotes(int note) {
    	notes -= note;
    	notesRetracted += note;
    }
    
    public Integer getNotesDispensed() {
        return notesDispensed;
    }

    public void setNotesDispensed(int notesDispensed) {
        this.notesDispensed = notesDispensed;
    }

    public Integer getNotesRejected() {
        return notesRejected;
    }

    public void setNotesRejected(int notesRejected) {
        this.notesRejected = notesRejected;
    }

	public Integer getDenomination() {
		return denomination;
	}

	public void setDenomination(Integer denomination) {
		this.denomination = denomination;
	}

	public ErrorSeverity getTotalErrorSeverity() {
		return totalErrorSeverity;
	}

	public void setTotalErrorSeverity(ErrorSeverity totalErrorSeverity) {
		this.totalErrorSeverity = totalErrorSeverity;
	}
	
	public Integer getNotesRetracted() {
		return notesRetracted;
	}

	public void setNotesRetracted(int notesRetracted) {
		this.notesRetracted = notesRetracted;
	}

	
//	public Currency getCurrency() {
//		return currency;
//	}
//
//	public void setCurrency(Currency currency) {
//		this.currency = currency;
//	}
}
