package vaulsys.webservice.walletcardmgmtwebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Dell on 4/29/2021.
 * //m.rehman: 29-04-2021, VG-NAP-202104271 / VP-NAP-202104261 / VC-NAP-202104261 - VISA transaction charging update
 */
@XmlRootElement
public class CardCharge {
    private String type;
    private String charge;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }
}
