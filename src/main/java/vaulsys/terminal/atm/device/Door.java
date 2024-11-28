package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Door")
public class Door extends ATMDevice {


    public Door() {
    }
}
