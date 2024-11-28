package vaulsys.webservice.walletcardmgmtwebservice.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RAZA MURTAZA BAIG on 1/27/2018.
 */
@XmlRootElement
public class NayaPayWsModel {

    private String id;

    private String servicename;


    private String nayapayid;


    private String mobilenumber;


    private String cnic;


    private String bankcode;


    private String bankname;


    private String accountnumber;


    private String accountcurrency;


    private String tranrefnumber;


    private String cnicpicture; //byte[] cnicpicture;


    private String customerpicture; //selfie //byte[] customerpicture; //selfie


    private String mothername;


    private String dateofbirth;


    private String transdatetime;


    private String pindata;


    private String oldpindata;


    private String newpindata;


    private String encryptkey;


    private String amounttransaction;


    private String amounttranfee;


    private String chargeflag;


    private String srcaccount;


    private String srcaccountcode;


    private String srcaccountcurrency;


    private String destaccount;

    private String destaccountcode;


    private String destaccountcurrency;


    private String otp;


    private String mpin;


    private String srcSettaccountnumber;


    private String destSettaccountnumber;


    private String merchantflag;


    private String walletaccountnumber;


    private String walletaccountcurrency;


    private String biometricdata;


    private List<Transaction> tranlist;


    private String RespCode;


    private HashMap<String,String> bankform;


    private String txnfeetype;


    public NayaPayWsModel()
    {
        this.bankform = new HashMap<String, String>();
    }

//    public NayaPayWsModel(String nayapayid,String mobilenumber,
//                          String cnic,String bankcode,
//                          String bankname,String accountnumber,
//                          String tranrefnumber,String cnicpicture,
//                          String customerpicture,String mothername,
//                          String dateofbirth,String transdatetime,
//                          String pindata,String oldpindata,
//                          String newpindata,String encryptkey,
//                          String amounttransaction,String amounttranfee,
//                          String chargeflag,String srcaccount,
//                          String srcaccountcode,String srcaccountcurrency,
//                          String destaccount,String destaccountcode,
//                          String destaccountcurrency,String otp,
//                          String mpin,String srcSettaccountnumber,
//                          String destSettaccountnumber,String merchantflag,
//                          String walletaccountcurrency,String biometricdata,
//                          String RespCode,String txnfeetype)
//    {
//
//    }

    public NayaPayWsModel(String NayaPayId, String MobileNum, String Cnic, String BankCode,
                          String BankName, String AccountNum, String TranRefNum, String CnicPicture,
                          String CustomerPicture, String MotherName, String DataOBirth, String TransDateTime,
                          String PinData, String OldPinData, String NewPinData, String EncryptKey,
                          String AmountTran, String AmountCharges, String ChargeFlag, String SrcAccount,
                          String SrcAccountCode, String SrcAccountCurrency, String DestAccount, String DestAccountCode,
                          String DestAccountCurrency, String Otp, String MPin, String SrcSettAccount,
                          String DestSettAccount, List<Transaction> TranList, String BioMetric,
                          String WalletAccountcurrency, String MerchantFlag, HashMap<String,String> BankForm, String TxnFeeType)
    {
        this.nayapayid = NayaPayId;
        this.mobilenumber = MobileNum;
        this.cnic = Cnic;
        this.bankcode = BankCode;
        this.bankname = BankName;
        this.accountnumber = AccountNum;
        this.tranrefnumber = TranRefNum;
        this.cnicpicture = CnicPicture;
        this.customerpicture = CustomerPicture; //selfie
        this.mothername = MotherName;
        this.dateofbirth = DataOBirth;
        this.transdatetime = TransDateTime;
        this.pindata = PinData;
        this.oldpindata = OldPinData;
        this.newpindata = NewPinData;
        this.encryptkey = EncryptKey;
        this.amounttransaction = AmountTran;
        this.amounttranfee = AmountCharges;
        this.chargeflag = ChargeFlag;
        this.srcaccount = SrcAccount;
        this.srcaccountcode = SrcAccountCode;
        this.srcaccountcurrency = SrcAccountCurrency;
        this.destaccount = DestAccount;
        this.destaccountcode = DestAccountCode;
        this.destaccountcurrency = DestAccountCurrency;
        this.otp = Otp;
        this.mpin = MPin;
        this.srcSettaccountnumber = SrcSettAccount;
        this.destSettaccountnumber = DestSettAccount;
        this.tranlist = TranList;
        this.biometricdata = BioMetric;

        this.walletaccountcurrency = WalletAccountcurrency;
        this.merchantflag = MerchantFlag;
        this.bankform = BankForm;
        this.txnfeetype = TxnFeeType;
    }

    public String getNayapayid() {
        return nayapayid;
    }

    public void setNayapayid(String nayapayid) {
        this.nayapayid = nayapayid;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
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

    public String getTranrefnumber() {
        return tranrefnumber;
    }

    public void setTranrefnumber(String tranrefnumber) {
        this.tranrefnumber = tranrefnumber;
    }

    public String getCnicpicture() {
        return cnicpicture;
    }

    public void setCnicpicture(String cnicpicture) {
        this.cnicpicture = cnicpicture;
    }

    public String getCustomerpicture() {
        return customerpicture;
    }

    public void setCustomerpicture(String customerpicture) {
        this.customerpicture = customerpicture;
    }

    public String getMothername() {
        return mothername;
    }

    public void setMothername(String mothername) {
        this.mothername = mothername;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getTransdatetime() {
        return transdatetime;
    }

    public void setTransdatetime(String transdatetime) {
        this.transdatetime = transdatetime;
    }

    public String getPindata() {
        return pindata;
    }

    public void setPindata(String pindata) {
        this.pindata = pindata;
    }

    public String getOldpindata() {
        return oldpindata;
    }

    public void setOldpindata(String oldpindata) {
        this.oldpindata = oldpindata;
    }

    public String getNewpindata() {
        return newpindata;
    }

    public void setNewpindata(String newpindata) {
        this.newpindata = newpindata;
    }

    public String getEncryptkey() {
        return encryptkey;
    }

    public void setEncryptkey(String encryptkey) {
        this.encryptkey = encryptkey;
    }

    public String getAmounttransaction() {
        return amounttransaction;
    }

    public void setAmounttransaction(String amounttransaction) {
        this.amounttransaction = amounttransaction;
    }

    public String getAmounttranfee() {
        return amounttranfee;
    }

    public void setAmounttranfee(String amounttranfee) {
        this.amounttranfee = amounttranfee;
    }

    public String getChargeflag() {
        return chargeflag;
    }

    public void setChargeflag(String chargeflag) {
        this.chargeflag = chargeflag;
    }

    public String getSrcaccount() {
        return srcaccount;
    }

    public void setSrcaccount(String srcaccount) {
        this.srcaccount = srcaccount;
    }

    public String getSrcaccountcode() {
        return srcaccountcode;
    }

    public void setSrcaccountcode(String srcaccountcode) {
        this.srcaccountcode = srcaccountcode;
    }

    public String getSrcaccountcurrency() {
        return srcaccountcurrency;
    }

    public void setSrcaccountcurrency(String srcaccountcurrency) {
        this.srcaccountcurrency = srcaccountcurrency;
    }

    public String getDestaccount() {
        return destaccount;
    }

    public void setDestaccount(String destaccount) {
        this.destaccount = destaccount;
    }

    public String getDestaccountcode() {
        return destaccountcode;
    }

    public void setDestaccountcode(String destaccountcode) {
        this.destaccountcode = destaccountcode;
    }

    public String getDestaccountcurrency() {
        return destaccountcurrency;
    }

    public void setDestaccountcurrency(String destaccountcurrency) {
        this.destaccountcurrency = destaccountcurrency;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    public String getSrcSettaccountnumber() {
        return srcSettaccountnumber;
    }

    public void setSrcSettaccountnumber(String srcSettaccountnumber) {
        this.srcSettaccountnumber = srcSettaccountnumber;
    }

    public String getDestSettaccountnumber() {
        return destSettaccountnumber;
    }

    public void setDestSettaccountnumber(String destSettaccountnumber) {
        this.destSettaccountnumber = destSettaccountnumber;
    }

    public List<Transaction> getTranlist() {
        return tranlist;
    }

    public void setTranlist(List<Transaction> tranlist) {
        this.tranlist = tranlist;
    }

    public String getRespCode() {
        return RespCode;
    }

    public void setRespCode(String respCode) {
        RespCode = respCode;
    }

    public String getMerchantflag() {
        return merchantflag;
    }

    public void setMerchantflag(String merchantflag) {
        this.merchantflag = merchantflag;
    }

    public String getWalletaccountcurrency() {
        return walletaccountcurrency;
    }

    public void setWalletaccountcurrency(String walletaccountcurrency) {
        this.walletaccountcurrency = walletaccountcurrency;
    }

    public String getBiometricdata() {
        return biometricdata;
    }

    public void setBiometricdata(String biometricdata) {
        this.biometricdata = biometricdata;
    }

    public Map<String, String> getBankform() {
        return bankform;
    }

    public void setBankform(HashMap<String, String> bankform) {
        this.bankform = bankform;
    }

    public String getTxnfeetype() {
        return txnfeetype;
    }

    public void setTxnfeetype(String txnfeetype) {
        this.txnfeetype = txnfeetype;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getWalletaccountnumber() {
        return walletaccountnumber;
    }

    public void setWalletaccountnumber(String walletaccountnumber) {
        this.walletaccountnumber = walletaccountnumber;
    }

    public String getAccountcurrency() {
        return accountcurrency;
    }

    public void setAccountcurrency(String accountcurrency) {
        this.accountcurrency = accountcurrency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
