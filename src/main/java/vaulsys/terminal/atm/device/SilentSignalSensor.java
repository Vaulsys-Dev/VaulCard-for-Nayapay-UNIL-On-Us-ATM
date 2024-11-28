package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("SilentSignal")
public class SilentSignalSensor extends ATMDevice {


    public SilentSignalSensor() {
    }
}
