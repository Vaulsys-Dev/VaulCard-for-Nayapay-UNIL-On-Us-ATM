package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CassetteB")
public class CassetteB extends Cassette {

    public CassetteB() {
    }
}
