package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CassetteC")
public class CassetteC extends Cassette {
    public CassetteC() {
    }
}
