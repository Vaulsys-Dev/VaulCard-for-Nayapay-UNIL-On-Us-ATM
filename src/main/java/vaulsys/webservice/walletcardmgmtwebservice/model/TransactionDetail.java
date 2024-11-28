package vaulsys.webservice.walletcardmgmtwebservice.model;

/**
 * Created by RAZA MURTAZA BAIG on 1/27/2018.
 */
public class TransactionDetail {

    private String nayapaytype;
    private String acctid;
    private String acctalias;
    private String srcid;
    private String srcname;
    private String srcnayapayid;
    private String destid;
    private String destname;
    private String destnayapayid;
    private String merchantid;
    private String parentid;
    private String amount;
    private String srccharge;
    private String destcharge;
    private String transdatetime;
    private String transrefnum;
    private String bankid;
    private String invoiceid;
    private String currency;
    private String respcode;
    private String stan;
    private String mapid;
    private String posinvoiceref;
    private String terminalid;
    private String disputeflag;
    private String referencenumber;
    private String agentid;
    private String settlementbatchid;
    private String transactionstatus;
    private String originaltransactionid;
    private String bankcharge;
    private String terminalloc;
    private String bankcode;
    private String accountnumber;
    private String branchname;
    private String depositorname;
    private String merchantname;
    private String depositslipnumber;

    // Asim Shahzad, Date : 22nd Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104

    private String cardlastdigits;
    private String cardscheme;
    private String posentrymode;
    private String merchantcategorycode;

    // =====================================================================================
    //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
    private String taxamount;
    private String originalapi;
    private String originalstan;
    private String originalrrn;
    private String originaldatetime;
    private String npticket;
    private String vrolticket;
    private String refundtype;

    // Asim Shahzad, Date : 12th Nov 2020, Tracking ID : VP-NAP-202010271

    private String bankimd;
    private String originaldataelement;

    public String getBankimd() {
        return bankimd;
    }

    public void setBankimd(String bankimd) {
        this.bankimd = bankimd;
    }

    public String getOriginaldataelement() {
        return originaldataelement;
    }

    public void setOriginaldataelement(String originaldataelement) {
        this.originaldataelement = originaldataelement;
    }

    // ==================================================================

    //m.rehman: 07-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    private String justification;
    //////////////////////////////////////////////////////////////////////////////////

    public String getNayapaytype() {
        return nayapaytype;
    }

    public void setNayapaytype(String nayapaytype) {
        this.nayapaytype = nayapaytype;
    }

    public String getAcctid() {
        return acctid;
    }

    public void setAcctid(String acctid) {
        this.acctid = acctid;
    }

    public String getAcctalias() {
        return acctalias;
    }

    public void setAcctalias(String acctalias) {
        this.acctalias = acctalias;
    }

    public String getSrcid() {
        return srcid;
    }

    public void setSrcid(String srcid) {
        this.srcid = srcid;
    }

    public String getSrcname() {
        return srcname;
    }

    public void setSrcname(String srcname) {
        this.srcname = srcname;
    }

    public String getSrcnayapayid() {
        return srcnayapayid;
    }

    public void setSrcnayapayid(String srcnayapayid) {
        this.srcnayapayid = srcnayapayid;
    }

    public String getDestid() {
        return destid;
    }

    public void setDestid(String destid) {
        this.destid = destid;
    }

    public String getDestname() {
        return destname;
    }

    public void setDestname(String destname) {
        this.destname = destname;
    }

    public String getDestnayapayid() {
        return destnayapayid;
    }

    public void setDestnayapayid(String destnayapayid) {
        this.destnayapayid = destnayapayid;
    }

    public String getMerchantid() {
        return merchantid;
    }

    public void setMerchantid(String merchantid) {
        this.merchantid = merchantid;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSrccharge() {
        return srccharge;
    }

    public void setSrccharge(String srccharge) {
        this.srccharge = srccharge;
    }

    public String getDestcharge() {
        return destcharge;
    }

    public void setDestcharge(String destcharge) {
        this.destcharge = destcharge;
    }

    public String getTransdatetime() {
        return transdatetime;
    }

    public void setTransdatetime(String transdatetime) {
        this.transdatetime = transdatetime;
    }

    public String getTransrefnum() {
        return transrefnum;
    }

    public void setTransrefnum(String transrefnum) {
        this.transrefnum = transrefnum;
    }

    public String getBankid() {
        return bankid;
    }

    public void setBankid(String bankid) {
        this.bankid = bankid;
    }

    public String getInvoiceid() {
        return invoiceid;
    }

    public void setInvoiceid(String invoiceid) {
        this.invoiceid = invoiceid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRespcode() {
        return respcode;
    }

    public void setRespcode(String respcode) {
        this.respcode = respcode;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getMapid() {
        return mapid;
    }

    public void setMapid(String mapid) {
        this.mapid = mapid;
    }

    public String getPosinvoiceref() {
        return posinvoiceref;
    }

    public void setPosinvoiceref(String posinvoiceref) {
        this.posinvoiceref = posinvoiceref;
    }

    public String getTerminalid() {
        return terminalid;
    }

    public void setTerminalid(String terminalid) {
        this.terminalid = terminalid;
    }

    public String getDisputeflag() {
        return disputeflag;
    }

    public void setDisputeflag(String disputeflag) {
        this.disputeflag = disputeflag;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getSettlementbatchid() {
        return settlementbatchid;
    }

    public void setSettlementbatchid(String settlementbatchid) {
        this.settlementbatchid = settlementbatchid;
    }

    public String getTransactionstatus() {
        return transactionstatus;
    }

    public void setTransactionstatus(String transactionstatus) {
        this.transactionstatus = transactionstatus;
    }

    public String getOriginaltransactionid() {
        return originaltransactionid;
    }

    public void setOriginaltransactionid(String originaltransactionid) {
        this.originaltransactionid = originaltransactionid;
    }

    public String getBankcharge() {
        return bankcharge;
    }

    public void setBankcharge(String bankcharge) {
        this.bankcharge = bankcharge;
    }

    public String getTerminalloc() {
        return terminalloc;
    }

    public void setTerminalloc(String terminalloc) {
        this.terminalloc = terminalloc;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public String getBranchname() {
        return branchname;
    }

    public void setBranchname(String branchname) {
        this.branchname = branchname;
    }

    public String getDepositorname() {
        return depositorname;
    }

    public void setDepositorname(String depositorname) {
        this.depositorname = depositorname;
    }

    public String getMerchantname() {
        return merchantname;
    }

    public void setMerchantname(String merchantname) {
        this.merchantname = merchantname;
    }

    public String getDepositslipnumber() {
        return depositslipnumber;
    }

    public void setDepositslipnumber(String depositslipnumber) {
        this.depositslipnumber = depositslipnumber;
    }

    // Asim Shahzad, Date : 22nd Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104

    public String getCardlastdigits() {
        return cardlastdigits;
    }

    public void setCardlastdigits(String cardlastdigits) {
        this.cardlastdigits = cardlastdigits;
    }

    public String getCardscheme() {
        return cardscheme;
    }

    public void setCardscheme(String cardscheme) {
        this.cardscheme = cardscheme;
    }

    public String getPosentrymode() {
        return posentrymode;
    }

    public void setPosentrymode(String posentrymode) {
        this.posentrymode = posentrymode;
    }

    public String getMerchantcategorycode() {
        return merchantcategorycode;
    }

    public void setMerchantcategorycode(String merchantcategorycode) {
        this.merchantcategorycode = merchantcategorycode;
    }
    // ======================================================================================
	
    public String getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(String taxamount) {
        this.taxamount = taxamount;
    }

    public String getOriginalapi() {
        return originalapi;
    }

    public void setOriginalapi(String originalapi) {
        this.originalapi = originalapi;
    }

    public String getOriginalstan() {
        return originalstan;
    }

    public void setOriginalstan(String originalstan) {
        this.originalstan = originalstan;
    }

    public String getOriginalrrn() {
        return originalrrn;
    }

    public void setOriginalrrn(String originalrrn) {
        this.originalrrn = originalrrn;
    }

    public String getOriginaldatetime() {
        return originaldatetime;
    }

    public void setOriginaldatetime(String originaldatetime) {
        this.originaldatetime = originaldatetime;
    }

    public String getNpticket() {
        return npticket;
    }

    public void setNpticket(String npticket) {
        this.npticket = npticket;
    }

    public String getVrolticket() {
        return vrolticket;
    }

    public void setVrolticket(String vrolticket) {
        this.vrolticket = vrolticket;
    }

    public String getRefundtype() {
        return refundtype;
    }

    public void setRefundtype(String refundtype) {
        this.refundtype = refundtype;
    }

    // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116

    private String acquiringcurrency;
    private String acquiringamount;
    private String conversionrate;
    private Boolean internationaltransactionflag;
    private String totalamount;
    private String baseamountafterconversion;
    private String foreigntransactionfee;
    private String taxappliedonforeigntransactionfee;
    private String withholdingtax;

    public String getAcquiringcurrency() {
        return acquiringcurrency;
    }

    public void setAcquiringcurrency(String acquiringcurrency) {
        this.acquiringcurrency = acquiringcurrency;
    }

    public String getAcquiringamount() {
        return acquiringamount;
    }

    public void setAcquiringamount(String acquiringamount) {
        this.acquiringamount = acquiringamount;
    }

    public String getConversionrate() {
        return conversionrate;
    }

    public void setConversionrate(String conversionrate) {
        this.conversionrate = conversionrate;
    }

    public Boolean getInternationaltransactionflag() {
        return internationaltransactionflag;
    }

    public void setInternationaltransactionflag(Boolean internationaltransactionflag) {
        this.internationaltransactionflag = internationaltransactionflag;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }

    public String getBaseamountafterconversion() {
        return baseamountafterconversion;
    }

    public void setBaseamountafterconversion(String baseamountafterconversion) {
        this.baseamountafterconversion = baseamountafterconversion;
    }

    public String getForeigntransactionfee() {
        return foreigntransactionfee;
    }

    public void setForeigntransactionfee(String foreigntransactionfee) {
        this.foreigntransactionfee = foreigntransactionfee;
    }

    public String getTaxappliedonforeigntransactionfee() {
        return taxappliedonforeigntransactionfee;
    }

    public void setTaxappliedonforeigntransactionfee(String taxappliedonforeigntransactionfee) {
        this.taxappliedonforeigntransactionfee = taxappliedonforeigntransactionfee;
    }

    public String getWithholdingtax() {
        return withholdingtax;
    }

    public void setWithholdingtax(String withholdingtax) {
        this.withholdingtax = withholdingtax;
    }

    // =======================================================================================

    //m.rehman: 07-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
    /////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 25th May 2021, Tracking ID : VP-NAP-202105212 / VC-NAP-202105211

    private String originaltxnamount;
    private String withholdingtaxpercentage;

    public String getOriginaltxnamount() {
        return originaltxnamount;
    }

    public void setOriginaltxnamount(String originaltxnamount) {
        this.originaltxnamount = originaltxnamount;
    }

    public String getWithholdingtaxpercentage() {
        return withholdingtaxpercentage;
    }

    public void setWithholdingtaxpercentage(String withholdingtaxpercentage) {
        this.withholdingtaxpercentage = withholdingtaxpercentage;
    }

    // =====================================================================================

    // Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241

    private String iban;

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    // ======================================================================================

    // Asim Shahzad, Date : 1st Sep 2021, Tracking ID : VC-NAP-202108271

    private String amountFCY;
    private String currencyFCY;
    private String exchangeRate;

    public String getAmountFCY() {
        return amountFCY;
    }

    public void setAmountFCY(String amountFCY) {
        this.amountFCY = amountFCY;
    }

    public String getCurrencyFCY() {
        return currencyFCY;
    }

    public void setCurrencyFCY(String currencyFCY) {
        this.currencyFCY = currencyFCY;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    // =================================================================

    //Arsalan Akhter, Date: 09-Sept-2021, Ticket: VC-NAP-202108231(Complete transaction details missing in dispute refund transaction)
    private String origamount;

    public String getOrigamount() { return origamount; }

    public void setOrigamount(String origamount) { this.origamount = origamount; }
    //==================================================================================================================
}
