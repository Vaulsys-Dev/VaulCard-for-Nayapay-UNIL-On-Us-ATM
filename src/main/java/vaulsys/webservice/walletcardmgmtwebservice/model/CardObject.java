package vaulsys.webservice.walletcardmgmtwebservice.model;

import vaulsys.cms.base.CMSCardControlConfig;

import java.util.List;

/**
 * Created by Mati on 26/09/2019.
 */
public class CardObject {
    private String id;
    private String cardnumber;
    private String cardexpiry;
    private String creationdate;
    private String cardstatus;
    private String cardnumberlastdigits;
    private String activationdate;
    private List<NayaPayLimit> nayapaylimits;
	
    // Asim Shahzad, Date : 13th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)

    private String isChipPinEnabled;
    private String isMagStripeEnabled;
    private String isCashWithdrawalEnabled;
    private String isNFCEnabled;
    private String isOnlineEnabled;
    private String isInternationalTxnsEnabled;
    private  String personalizationstatus ; //Arsalan Akhter, Date: 12_March_2021, Tracking ID: VP-NAP-202103114_VC-NAP-202103114

    public String getIsChipPinEnabled() {
        return isChipPinEnabled;
    }

    public void setIsChipPinEnabled(String isChipPinEnabled) {
        this.isChipPinEnabled = isChipPinEnabled;
    }

    public String getIsMagStripeEnabled() {
        return isMagStripeEnabled;
    }

    public void setIsMagStripeEnabled(String isMagStripeEnabled) {
        this.isMagStripeEnabled = isMagStripeEnabled;
    }

    public String getIsCashWithdrawalEnabled() {
        return isCashWithdrawalEnabled;
    }

    public void setIsCashWithdrawalEnabled(String isCashWithdrawalEnabled) {
        this.isCashWithdrawalEnabled = isCashWithdrawalEnabled;
    }

    public String getIsNFCEnabled() {
        return isNFCEnabled;
    }

    public void setIsNFCEnabled(String isNFCEnabled) {
        this.isNFCEnabled = isNFCEnabled;
    }

    public String getIsOnlineEnabled() {
        return isOnlineEnabled;
    }

    public void setIsOnlineEnabled(String isOnlineEnabled) {
        this.isOnlineEnabled = isOnlineEnabled;
    }

    public String getIsInternationalTxnsEnabled() {
        return isInternationalTxnsEnabled;
    }

    public void setIsInternationalTxnsEnabled(String isInternationalTxnsEnabled) {
        this.isInternationalTxnsEnabled = isInternationalTxnsEnabled;
    }

    // ========================================================================================================

    // Asim Shahzad, Date : 20th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)

    private String lastStatusChangeDate;

    public String getLastStatusChangeDate() {
        return lastStatusChangeDate;
    }

    public void setLastStatusChangeDate(String lastStatusChangeDate) {
        this.lastStatusChangeDate = lastStatusChangeDate;
    }

    // ========================================================================================================

    // Asim Shahzad, Date : 4th Feb 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 3)

    private String cardtype;

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    // ========================================================================================================

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCardexpiry() {
        return cardexpiry;
    }

    public void setCardexpiry(String cardexpiry) {
        this.cardexpiry = cardexpiry;
    }

    public String getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardstatus() {
        return cardstatus;
    }

    public void setCardstatus(String cardstatus) {
        this.cardstatus = cardstatus;
    }

    public String getCardnumberlastdigits() {
        return cardnumberlastdigits;
    }

    public void setCardnumberlastdigits(String cardnumberlastdigits) {
        this.cardnumberlastdigits = cardnumberlastdigits;
    }

    public String getActivationdate() {
        return activationdate;
    }

    public void setActivationdate(String activationdate) {
        this.activationdate = activationdate;
    }

    public List<NayaPayLimit> getNayapaylimits() {
        return nayapaylimits;
    }

    public void setNayapaylimits(List<NayaPayLimit> nayapaylimits) {
        this.nayapaylimits = nayapaylimits;
    }

    //Arsalan Akhter, Date: 12_March_2021, Tracking ID: VP-NAP-202103114_VC-NAP-202103114
    public String getPersonalizationstatus() { return personalizationstatus; }

    public void setPersonalizationstatus(String personalizationstatus) { this.personalizationstatus = personalizationstatus; }
    //*********************************************

    // Asim Shahzad, Date : 25th May 2021, Tracking ID : VP-NAP-202105212 / VC-NAP-202105211
    //m.rehman: 19-07-2021, VP-NAP-202107161 - ATM time out on biometrics verification - MBL
    //changing access modifier from public to private, due to pojo conversion to XML failure
    private String nameoncard;

    public String getNameoncard() {
        return nameoncard;
    }

    public void setNameoncard(String nameoncard) {
        this.nameoncard = nameoncard;
    }

    // =====================================================================================
}