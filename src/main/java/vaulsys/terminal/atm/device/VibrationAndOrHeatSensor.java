package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("VibrationAndOrHeatSensor")
public class VibrationAndOrHeatSensor extends ATMDevice {

    public VibrationAndOrHeatSensor() {
    }
}
