package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CurrencyReject")
public class CurrencyRejectBin extends ATMDevice {

    protected Integer notes;

    public CurrencyRejectBin() {
    }
    
    public Integer getNotes() {
        return notes;
    }

    public void setNotes(Integer notes) {
        this.notes = notes;
    }

    public void increseNotes(int note) {
        notes += note;
    }
}
