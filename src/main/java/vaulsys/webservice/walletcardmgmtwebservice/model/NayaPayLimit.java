package vaulsys.webservice.walletcardmgmtwebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class NayaPayLimit {

    private String transaction;

    private String amount;

    private String availlimit;

    private String availlimitfreq;

    //m.rehman: 15-02-2021, VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
    private String customlimitflag;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAvaillimit() {
        return availlimit;
    }

    public void setAvaillimit(String availlimit) {
        this.availlimit = availlimit;
    }

    public String getAvaillimitfreq() {
        return availlimitfreq;
    }

    public void setAvaillimitfreq(String availlimitfreq) {
        this.availlimitfreq = availlimitfreq;
    }

    //m.rehman: 15-02-2021, VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
    public String getCustomlimitflag() {
        return customlimitflag;
    }

    public void setCustomlimitflag(String customlimitflag) {
        this.customlimitflag = customlimitflag;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
