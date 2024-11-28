package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CassetteD")
public class CassetteD extends Cassette {
    public CassetteD() {
    }
}
