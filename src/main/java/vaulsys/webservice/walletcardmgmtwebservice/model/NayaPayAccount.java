package vaulsys.webservice.walletcardmgmtwebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class NayaPayAccount {

    private String accountid;

    private String bankname;

    private String accountnumber;

    private String linkdate;


    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public String getLinkdate() {
        return linkdate;
    }

    public void setLinkdate(String linkdate) {
        this.linkdate = linkdate;
    }
}
