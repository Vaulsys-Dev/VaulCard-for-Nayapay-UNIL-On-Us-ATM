package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CassetteA")
public class CassetteA extends Cassette {
    public CassetteA() {
    }
}
