package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Encryptor")
public class Encryptor extends ATMDevice {

    public Encryptor() {
    }

}