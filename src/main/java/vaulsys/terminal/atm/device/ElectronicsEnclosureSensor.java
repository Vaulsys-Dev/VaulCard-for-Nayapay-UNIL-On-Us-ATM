package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ElectronicsEnclosure")
public class ElectronicsEnclosureSensor extends ATMDevice {


    public ElectronicsEnclosureSensor() {
    }
}
