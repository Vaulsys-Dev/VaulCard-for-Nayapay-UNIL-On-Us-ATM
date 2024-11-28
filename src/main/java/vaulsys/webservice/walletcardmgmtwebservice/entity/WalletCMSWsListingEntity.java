package vaulsys.webservice.walletcardmgmtwebservice.entity;


import com.owlike.genson.annotation.JsonIgnore;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import vaulsys.persistence.IEntity;
import vaulsys.util.WebServiceUtil;
import vaulsys.webservice.walletcardmgmtwebservice.model.*;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

/**
 * Created by m.rehman: 10-11-2021 - Nayapay Optimization.
 */
@Entity
@Table(name = "TRANSACTION_LISTING")
public class WalletCMSWsListingEntity implements IEntity<Long>, Cloneable {

    private static final Logger logger = Logger.getLogger(WalletCMSWsListingEntity.class);

    @Id
    private Long id;

    @Column(name = "SERVICENAME")
    private String servicename;

//    @Column(name = "NAYAPAYID") //Raza removing from V1.8
//    private String nayapayid;

    @Column(name = "MOBILENUMBER")
    private String mobilenumber;

    @Column(name = "CNIC")
    private String cnic;

    @Column(name = "CUSTOMERNAME")
    private String customername;

    @Column(name = "CNICEXPIRY")
    private String cnicexpiry;

    @Column(name = "BANKCODE")
    private String bankcode;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DESTBANKCODE")
    @Transient
    private String destbankcode;

    @Column(name = "BANKNAME")
    private String bankname;

    @Column(name = "ACCOUNTNUMBER")
    private String accountnumber;

    @Column(name = "ACCOUNTCURRENCY")
    private String accountcurrency;

    @Column(name = "TXNREFNUM")
    private String tranrefnumber;

    @Column(name = "MOTHERNAME")
    private String mothername;

    @Column(name = "DATEOFBIRTH")
    private String dateofbirth;

    @Column(name = "TRANSDATETIME")
    private String transdatetime;

    //@Column(name = "PINDATA")
    @Transient
    private String pindata;

    //@Column(name = "OLDPINDATA")
    @Transient
    private String oldpindata;

    //@Column(name = "NEWPINDATA")
    @Transient
    private String newpindata;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ENCRYPTEDKEY")
    @Transient
    private String encryptkey;

    @Column(name = "AMOUNTTRAN")
    private String amounttransaction;

    @Column(name = "SRCAMOUNTCHARGE")
    private String srcchargeamount;

    @Column(name = "DESTAMOUNTCHARGE")
    private String destchargeamount;

    @Column(name = "DESTACCOUNT")
    private String destaccount;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DESTACCOUNTCURRENCY")
    @Transient
    private String destaccountcurrency;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "OTP")
    @Transient
    private String otp;

    @Transient
    private String biometricdata;

    @Transient
    private List<Transaction> tranlist;

    @Column(name = "RESPCODE")
    private String respcode;

    @Column(name = "PLACEOFBIRTH")
    private String placeofbirth;

    @Column(name = "FATHERNAME")
    private String fathername;

    @Column(name = "PROVINCE")
    private String province;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "TELECOMSP")
    @Transient
    private String tsp; //telecommunication service provider (required for OTP by banks)

    @Column(name = "DESTNAYAPAYID")
    private String destnayapayid;

    @Column(name = "ORIG_DATA_ELEMENT")
    private String origdataelement;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ATMID")
    @Transient
    private String atmid;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "COREBANKCODE")
    @Transient
    private String corebankcode;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "COREACCOUNT")
    @Transient
    private String coreaccount;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "COREACCOUNTCURRENCY")
    @Transient
    private String coreaccountcurrency;

    @Column(name = "CARDNUMBER")
    private String cardnumber;

    //@Column(name = "CARDPINDATA")
    @Transient
    private String cardpindata;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ENABLEFLAG")
    @Transient
    private String enableflag;

    @Column(name = "MERCHANTID")
    private String merchantid;

    @Column(name = "DAILYLIMIT")
    private String dailylimit;

    @Column(name = "MONTHLYLIMIT")
    private String monthlylimit;

    @Column(name = "YEARLYLIMIT")
    private String yearlylimit;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "STATUS")
    @Transient
    private String status;

    @Column(name = "STAN")
    private String stan;

    @Column(name = "RRN")
    private String rrn;

    @Column(name = "USERID")
    private String userid;

    @Column(name = "CHANNELID")
    private String channelid;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ALLOWED")
    @Transient
    private String allowed;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DELETETYPE")
    @Transient
    private String deletetype;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "COMMENTS")
    @Transient
    private String comments;

    @Column(name = "ACCTID")
    private String acctid;

    @Column(name = "ACCT_ALIAS")
    private String acctalias;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ISPRIMARY")
    @Transient
    private String isprimary;

    @Column(name = "ACCTBALANCE")
    private String acctbalance;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ACCTLIMIT")
    @Transient
    private String acctlimit;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "AVAILLIMIT")
    @Transient
    private String availlimit;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "AVAILLIMITFREQ")
    @Transient
    private String availlimitfreq;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "STATE")
    @Transient
    private String state;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "REQUESTTIME")
    @Transient
    private String requesttime;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ACTIVATIONTIME")
    @Transient
    private String activationtime;

    @Column(name = "NAYAPAYCHARGES")
    private String nayapaycharges;

    @Column(name = "DESTUSERID")
    private String destuserid;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CITY")
    private String city;

    @Column(name = "COUNTRY")
    private String country;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ADVANCEFLAG")
    @Transient
    private String advanceflag;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "SECONDARYNUMBER")
    @Transient
    private String secondarynumber;

    @Column(name = "USERTOKEN")
    private String accesstoken;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "INOUTFILTER")
    @Transient
    private String inoutfilter;

    // Asim Shahzad, Date : 17th March 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115
    //@Column(name = "TYPEFILTER")
    //private String typefilter;
    @Transient
    private List<String> typefilter;
    // =======================================================================================

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "SEARCHTEXT")
    @Transient
    private String searchtext;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "NAYAPAYID")
    private String nayapayid;

    //m.rehman: Parent merchant ID
    @Column(name = "PARENTID")
    private String parentid;

    //m.rehman: Merchant name
    @Column(name = "MERCHANTNAME")
    private String merchantname;

    //m.rehman: Merchant category ID
    @Column(name = "MERCHANTCATEGORYID")
    private String categoryid;

    //m.rehman: Trusted flag for linked account transactions (true/false)
    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "TRUSTEDFLAG")
    @Transient
    private String trustedflag;

    //m.rehman: Merchant phone number
    @Column(name = "PHONENUMBER")
    private String phonenumber;

    //m.rehman: Merchant transaction limit
    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "TRANSACTIONLIMIT")
    @Transient
    private String transactionlimit;

    //m.rehman: Merchant category name
    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "CATEGORYNAME")
    @Transient
    private String categoryname;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "MERCHANTSTATE")
    @Transient
    private String merchantstate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "MERCHANTENABLED")
    @Transient
    private String merchantenabled;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "MERCHANTBLOCKED")
    @Transient
    private String merchantblocked;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "MINIMUMAMOUNT")
    @Transient
    private String minimumamount;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "MAXIMUMAMOUNT")
    @Transient
    private String maximumamount;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "SOURCECHARGETYPE")
    @Transient
    private String sourcechargetype;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DESTINATIONCHARGPE")
    @Transient
    private String destinationchargetype;

    @Column(name = "CONSUMERNO")
    private String consumerno;

    @Column(name = "UTILCOMPANYID")
    private String utilcompanyid;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "CONSUMERDETAIL")
    @Transient
    private String consumerdetail;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BILLSTATUS")
    @Transient
    private String billstatus;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DUEDATE")
    @Transient
    private String duedate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "AMTWITHINDUEDATE")
    @Transient
    private String amtwithinduedate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "AMTAFTERDUEDATE")
    @Transient
    private String amtafterduedate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BILLINGMONTH")
    @Transient
    private String billingmonth;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DATEPAID")
    @Transient
    private String datepaid;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "AMOUNTPAID")
    @Transient
    private String amtpaid;

    @Column(name = "TRANAUTHID")
    private String tranauthid;

    @Column(name = "RESERVED")
    private String reserved;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "IDENTIFICATIONNO")
    @Transient
    private String identificationno;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "PING")
    @Transient
    private String ping;

    @Column(name = "NAYAPAYTRANTYPE")
    private String nayapaytrantype;

    @Column(name = "DESTUSERNAME")
    private String destusername;

    @Column(name = "AGENTID")
    private String agentid;

    @Column(name = "REFERENCENUMBER")
    private String referencenumber;

    @Column(name = "INVOICEID")
    private String invoiceid;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "VERIFIEDFLAG")
    @Transient
    private String verifiedflag;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "STARTDATE")
    @Transient
    private String startdate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ENDDATE")
    @Transient
    private String enddate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BANKTXNFLAG")
    @Transient
    private String banktxnflag;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BLOCKEDFLAG")
    @Transient
    private String blockedflag;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "NAYAPAYTXNID")
    @Transient
    private String nayapaytxnid;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "CREATIONDATE")
    @Transient
    private String creationdate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BANK_MNEMONIC") //Raza Adding for OneLink Biller in-case if it is required, currently for logging only
    @Transient
    private String bankmnemonic;

    //m.rehman: for NayaPay, adding more fields
    @Column(name = "TOTALAMOUNT")
    private String totalamount;

    @Column(name = "BANKCHARGES")
    private String bankcharges;

    @Column(name = "BANKTAXAMOUNT")
    private String banktaxamount;

    @Column(name = "NAYAPAYTAXAMOUNT")
    private String nayapaytaxamount;

    @Column(name = "DEPOSITAMOUNT")
    private String depositamount;

    @Column(name = "SECRET_QUESTION_1")
    private String secretquestion1;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "SECRET_QUET_ANS_1")
    @Transient
    private String secretquestionanswer1;

    @Column(name = "SECRET_QUESTION_2")
    private String secretquestion2;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "SECRET_QUET_ANS_2")
    @Transient
    private String secretquestionanswer2;

    @Transient
    private List<NayaPayLimit> nayapaylimits;

    @Transient
    private List<NayaPayLinkedAccount> linkedaccounts;

    @Transient
    private List<ProvisionalWallet> provisionalwallets;

    @Transient
    private List<NayaPayAccount> accountlist;

    @Transient
    private List<Transaction> transactions;

    @Transient
    private List<UserTransaction> usertransactions;

    //m.rehman: for NayaPay
    @Transient
    private TransactionDetail transactionDetail;

    //Raza adding 02072019
    @Transient
    private List<TransactionDetail> reversal;

    @Transient
    private String currency; //Raza using for GetWallet

    @Transient
    @JsonIgnore
    private String bank;

    @Transient
    @JsonIgnore
    private String destbank;

    @Transient
    @JsonIgnore
    private String corebank;

    @Transient
    @JsonIgnore
    private String destcurrency;

    @Transient
    @JsonIgnore
    private String corecurrency;

    //Raza adding for OTC start
    @Column(name = "ACQBIN")
    private String acqbin;

    @Column(name = "BRANCHCODE")
    private String branchcode;

    @Column(name = "BRANCHNAME")
    private String branchname;

    @Column(name = "SLIPNUMBER")
    private String slipnumber;

    @Column(name = "TELLERID")
    private String tellerid;
    //Raza adding for OTC end

    @Transient
    private SecurityParams securityparams;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "CODFLAG")
    @Transient
    private String codflag;

    //m.rehman: for NayaPay, adding new fields for document 2.0 <start>
    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "SETTLEMENT_DELAY")
    @Transient
    private String settlementdelay;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "PAGE_COUNT")
    @Transient
    private String pagecount;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "PAGE_SIZE")
    @Transient
    private String pagesize;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "TOTAL_COUNT")
    @Transient
    private String totalcount;

    @Column(name = "BILLER_ID")
    private String billerid;

    @Column(name = "BILLER_NAME")
    private String billername;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "PARTIALFLAG")
    @Transient
    private String partialflag;

    //m.rehman: for validating incomcing ip address also save in DB for logging
    @Column(name = "SOURCE_IP")
    private String incomingip;
    //m.rehman: for NayaPay, adding new fields for document 2.0 <end>

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "TEMPBLOCKFLAG")
    @Transient
    private String tempblockflag;

    @Column(name = "CARDEXPIRY")
    private String cardexpiry;

    //Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
    @Column(name = "CARDNOLASTDIGITS")
    private String cardlastdigits;
    //===============================================================================================================

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "ACCTSTATUS")
    @Transient
    private String acctStatus;

    @Column(name = "AMTTRANFEE")
    private String amttranfee;

    @Column(name = "TERMLOC")
    private String termloc;

    @Transient
    private String ismerchantonline;

    @Transient
    private String iswalletaccount;

    @Column(name = "MERCHANT_AMT")
    private String merchantamount;
	
    @Column(name = "ORIGINALAPI")
    private String originalapi;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DECRYPTEDOTP")
    @Transient
    private String decryptedotp;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "CIPHEREDDATA")
    @Transient
    private String ciphereddata;

    //m.rehman: for onelink issuing
    @Transient
    private String track2Data;

    @Transient
    private String track3Data;

    @Transient
    private String track1Data;

    @Transient
    private String icccarddata;

    @Transient
    private String servicecode;

    @Transient
    private String cvv;

    @Transient
    private String cvv2;

    @Transient
    private String icvv;

    // Asim Shahzad, Date : 21st Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104
    @Column(name = "POS_ENT_MODE")
    private String posentrymode;

    @Transient
    private String selfdefinedata;

    @Transient
    private String acctlevel;

    @Column(name = "TRACKING_ID")
    private String trackingid;

    @Transient
    private List<CardObject> cardobjectlist;

    @Column(name = "MAP_ID")
    private String mapid;

    @Column(name = "POS_INVOICE_REF")
    private String posinvoiceref;

    @Column(name = "TERMINALID")
    private String terminalid;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "DISPUTE_FLAG")
    @Transient
    private String disputeflag;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BLOCK_TYPE")
    @Transient
    private String blocktype;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "LOCK_STATE")
    @Transient
    private String lockstate;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "FINANCIAL_FLAG")
    @Transient
    private String financialflag;

    @Column(name = "TRANCURRENCY")
    private String trancurrency; //Raza also add column in DB

//    @Transient
//    @JsonIgnore
//    private ProcessContext processContext; //Raza commenting

	    //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
    @Column(name = "ORIG_STAN")
    private String origstan;

	    //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
    @Column(name = "ORIG_TRANS_DATE_TIME")
    private String origtransdatetime;

    //m.rehman: 08-12-2021, Nayapay Optimization
    @Transient
    private String occupation;

    // Author: Asim Shahzad, Date : 24th Feb 2020, Desc : For getting Nayapay mobile application download counts from middleware

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "NP_APP_DOWNLOAD_COUNT")
    @Transient
    private String npappdownloadcount;

    //*******************************************************************************************************
    // Asim Shahzad, Date : 30th March 2020, Desc : Added available and actual balances for Ledger management
    @Transient
    private String availablebaldebitacc; // availableBalDebitAcc

    @Transient
    private String actualbaldebitacc; // actualBalDebitAcc

    @Transient
    private String availablebalcreditacc; // availableBalCreditAcc

    @Transient
    private String actualbalcreditacc; // actualBalCreditAcc

    @Transient
    private String fundsvoucherid; // actualBalCreditAcc
    //*******************************************************************************************************

    //m.rehman: Euronet Integration, for 3D Secure implementation
    @Transient
    private String cavvdata;
	
	// Asim Shahzad, Date : 24th May 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115
    @Column(name = "CBILL_AMT")
    private String cbillamount;
	// =======================================================================================
    @Column(name = "CBILL_CURR")
    private String cbillcurrency;

    @Column(name = "CBILL_RATE")
    private String cbillrate;

    //m.rehman: for IBFT Out
    @Column(name = "BENE_BANK_CODE")
    private String benebankcode;

    @Column(name = "BENE_BANK_ACCT_NO")
    private String benebankaccountno;

    @Column(name = "BENE_ACCT_TITLE")
    private String beneaccounttitle;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BENE_FLAG")
    @Transient
    private String beneflag;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BENE_ACCT_ALIAS")
    @Transient
    private String beneaccountalias;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name = "BENEFICIARY_ID")
    @Transient
    private String beneid;

    @Column(name = "BENE_EMAIL_ID")
    private String beneemailid;

    @Column(name = "BENE_MOBILE_NO")
    private String benemobileno;

    @Column(name = "PURPOSE_OF_TXN")
    private String purposeoftransaction;

    // Asim Shahzad, Date : 22nd Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104
    @Column(name = "CARD_SCHEME")
    private String cardscheme;
    // =====================================================================================
    //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
    @Column(name = "ORIG_RET_REF_NO")
    private String origretrefno;

    @Column(name = "NP_TICKET_ID")
    private String npticket;

    @Column(name = "VROL_TICKET_ID")
    private String vrolticket;
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 19th Nov 2020, Tracking ID : VP-NAP-202011131 / VC-NAP-202011131 / VG-NAP-202011131

    @Transient
    private String maxmerchantwalletlimit;

    public String getMaxmerchantwalletlimit() {
        return maxmerchantwalletlimit;
    }

    public void setMaxmerchantwalletlimit(String maxmerchantwalletlimit) {
        this.maxmerchantwalletlimit = maxmerchantwalletlimit;
    }

    // ========================================================================================================

    // Asim Shahzad, Date : 13th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)

//    @Transient
//    private Boolean atlStatus;
//
//    public Boolean getAtlStatus() {
//        return atlStatus;
//    }
//
//    public void setAtlStatus(Boolean atlStatus) {
//        this.atlStatus = atlStatus;
//    }
    // Asim Shahzad, Date : 13th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name="IS_CHIP_PIN_ENABLED")
    @Transient
    private String isChipPinEnabled;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name="IS_MAG_STRIPE_ENABLED")
    @Transient
    private String isMagStripeEnabled;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name="IS_CASH_WITHDRAWAL_ENABLED")
    @Transient
    private String isCashWithdrawalEnabled;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name="IS_NFC_ENABLED")
    @Transient
    private String isNFCEnabled;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name="IS_ONLINE_ENABLED")
    @Transient
    private String isOnlineEnabled;

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name="IS_INT_TXNS_ENABLED")
    @Transient
    private String isInternationalTxnsEnabled;

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

    //m.rehman: 08-12-2021, Nayapay Optimization
    //@Column(name="CARD_TYPE")
    @Transient
    private String cardtype;

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    // ========================================================================================================
    
	//m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
	@Transient
    private String globalcardlimit;

    @Transient
    private String cashwithdrawallimit;

    @Transient
    private String purchaselimit;

    @Transient
    private String onlinetxnlimit;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	//m.rehman: Euronet integration
	@Transient
    private String addresponsedata;

    //m.rehman: 15-02-2021, VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
    @Transient
    private String customlimitflag;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 05-03-2021, VP-NAP-202103041/ VC-NAP-202103041 - Merchant Transaction Listing Issue
    @Transient
    private List<WalletCMSWsListingEntity> wsloglist;

    @Transient
    private WalletCMSWsListingEntity wslog;
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 11th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111

    @Transient
    private String reasonofclosure;

    @Transient
    private String approvinguser;

    @Transient
    private String closurerequestdatetime;

    // =======================================================================================

    // Asim Shahzad, Date : 16th March 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115

    @Transient
    private String totalamountspent;

    @Transient
    private String totalamountreceived;

    @Transient
    private String fromdatetime;

    @Transient
    private String todatetime;

    // =======================================================================================

    //m.rehman: 07-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    @Transient
    private String settledflag;

    @Transient
    private String merchantfavorflag;

    @Transient
    private String debitwalletflag;

    @Transient
    private String creditwalletflag;

    @Column(name = "JUSTIFICATION")
    private String justification;
    //////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 29-04-2021, VG-NAP-202104271 / VP-NAP-202104261 / VC-NAP-202104261 - VISA transaction charging update
    @Transient
    private List<CardCharge> cardchargeslist;
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 30th July 2021, Tracking ID : VP-NAP-202104011 / VC-NAP-202104012
    @Column(name="SETTLEMENT_AMOUNT")
    private String settlementamount;

    public String getSettlementamount() {
        return settlementamount;
    }

    public void setSettlementamount(String settlementamount) {
        this.settlementamount = settlementamount;
    }
    // ======================================================================================

    //m.rehman: 02-09-2021, VC-NAP-202108231 - Complete transaction details missing in dispute refund transaction
    @Column(name = "DESTCHANNEL")
    private String origChannelId;
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Arsalan Akhter, Date: 09-Sept-2021, Ticket: VC-NAP-202108231(Complete transaction details missing in dispute refund transaction)
    @Column(name="RESERVED3")
    private String reserved3;
    //==================================================================================================================

    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
    @Transient
    private String declinedbycardctrl;
    //=====================================================================================================================


    //Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
    @Transient
    private String limittype;
    //======================================================================================================


    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    @Column(name = "INCLUDE_IN_STATEMENT", columnDefinition = "integer default 0")
    private int includeinstatement = 0;

    @Column(name = "SOURCE_OPENING_BALANCE")
    private String openingbalance;

    @Column(name = "SOURCE_CLOSING_BALANCE")
    private String closingbalance;

    @Column(name = "DEST_OPENING_BALANCE")
    private String destOpeningbalance;

    @Column(name = "DEST_CLOSING_BALANCE")
    private String destClosingbalance;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public WalletCMSWsListingEntity()
    {
        //this.bankform = new HashMap<String, String>();
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
//                          String respcode,String txnfeetype)
//    {
//
//    }

    public WalletCMSWsListingEntity(String NayaPayId, String MobileNum, String Cnic, String BankCode,
                                    String BankName, String AccountNum, String TranRefNum, String CnicPictureFront, String CnicPictureBack,
                                    String CustomerPicture, String MotherName, String DataOBirth, String TransDateTime,
                                    String PinData, String OldPinData, String NewPinData, String EncryptKey,
                                    String AmountTran, String SrcAmountCharges, String DestAmountCharges , String ChargeFlag, String SrcAccount,
                                    String SrcAccountCode, String SrcAccountCurrency, String DestAccount, String DestAccountCode,
                                    String DestAccountCurrency, String Otp, /*String MPin,*/ /*String SrcSettAccount,*/
                           /*String DestSettAccount,*/ List<Transaction> TranList, String BioMetric,
                                    HashMap<String,String> BankForm, String TxnFeeType)
    {
        //this.nayapayid = NayaPayId; //Raza removing from V1.8
        this.mobilenumber = MobileNum;
        this.cnic = Cnic;
        this.bankcode = BankCode;
        this.bankname = BankName;
        this.accountnumber = AccountNum;
        this.tranrefnumber = TranRefNum;
        this.mothername = MotherName;
        this.dateofbirth = DataOBirth;
        this.transdatetime = TransDateTime;
        this.pindata = PinData;
        this.oldpindata = OldPinData;
        this.newpindata = NewPinData;
        this.encryptkey = EncryptKey;
        this.amounttransaction = AmountTran;
        this.srcchargeamount = SrcAmountCharges;
        //this.chargeflag = ChargeFlag;
        //this.srcaccount = SrcAccount;
        //this.accountcode = SrcAccountCode;
        //this.srcaccountcurrency = SrcAccountCurrency;
        this.destaccount = DestAccount;
        //this.destaccountcode = DestAccountCode;
        this.destaccountcurrency = DestAccountCurrency;
        this.otp = Otp;
        //this.mpin = MPin;
        //this.srcSettaccountnumber = SrcSettAccount;
        //this.destSettaccountnumber = DestSettAccount;
        this.tranlist = TranList;
        this.biometricdata = BioMetric;

        //this.walletaccountcurrency = WalletAccountcurrency;
        //this.txnfeetype = TxnFeeType;
    }

//    public String getNayapayid() { //Raza removing from V1.8
//        return nayapayid;
//    }

//    public void setNayapayid(String nayapayid) { //Raza removing from V1.8
//        this.nayapayid = nayapayid;
//    }

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

//    public String getaccountcode() {
//        return accountcode;
//    }

//    public void setaccountcode(String srcaccountcode) {
//        this.accountcode = srcaccountcode;
//    }

    public String getDestaccount() {
        return destaccount;
    }

    public void setDestaccount(String destaccount) {
        this.destaccount = destaccount;
    }

//    public String getDestaccountcode() {
//        return destaccountcode;
//    }

//    public void setDestaccountcode(String destaccountcode) {
//        this.destaccountcode = destaccountcode;
//    }

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

//    public String getMpin() {
//        return mpin;
//    }
//
//    public void setMpin(String mpin) {
//        this.mpin = mpin;
//    }

//    public String getSrcSettaccountnumber() {
//        return srcSettaccountnumber;
//    }
//
//    public void setSrcSettaccountnumber(String srcSettaccountnumber) {
//        this.srcSettaccountnumber = srcSettaccountnumber;
//    }

//    public String getDestSettaccountnumber() {
//        return destSettaccountnumber;
//    }
//
//    public void setDestSettaccountnumber(String destSettaccountnumber) {
//        this.destSettaccountnumber = destSettaccountnumber;
//    }

    public List<Transaction> getTranlist() {
        return tranlist;
    }

    public void setTranlist(List<Transaction> tranlist) {
        this.tranlist = tranlist;
    }

    public String getRespcode() {
        return respcode;
    }

    public void setRespcode(String respcode) {
        this.respcode = respcode;
    }

    public String getBiometricdata() {
        return biometricdata;
    }

    public void setBiometricdata(String biometricdata) {
        this.biometricdata = biometricdata;
    }

    @JsonIgnore
    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getAccountcurrency() {
        return accountcurrency;
    }

    public void setAccountcurrency(String accountcurrency) {
        this.accountcurrency = accountcurrency;
    }

    public String getPlaceofbirth() {
        return placeofbirth;
    }

    public void setPlaceofbirth(String placeofbirth) {
        this.placeofbirth = placeofbirth;
    }

    public String getCnicexpiry() {
        return cnicexpiry;
    }

    public void setCnicexpiry(String cnicexpiry) {
        this.cnicexpiry = cnicexpiry;
    }

    public String getFathername() {
        return fathername;
    }

    public void setFathername(String fathername) {
        this.fathername = fathername;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getTsp() {
        return tsp;
    }

    public void setTsp(String tsp) {
        this.tsp = tsp;
    }

    public String getDestbankcode() {
        return destbankcode;
    }

    public void setDestbankcode(String destbankcode) {
        this.destbankcode = destbankcode;
    }

    public String getDestnayapayid() {
        return destnayapayid;
    }

    public void setDestnayapayid(String destCnic) {
        this.destnayapayid = destCnic;
    }

    public String getSrcchargeamount() {
        return srcchargeamount;
    }

    public void setSrcchargeamount(String srcchargeamount) {
        this.srcchargeamount = srcchargeamount;
    }

    public String getDestchargeamount() {
        return destchargeamount;
    }

    public void setDestchargeamount(String destchargeamount) {
        this.destchargeamount = destchargeamount;
    }

    public String getOrigdataelement() {
        return origdataelement;
    }

    public void setOrigdataelement(String origdataelement) {
        this.origdataelement = origdataelement;
    }

    public String getAtmid() {
        return atmid;
    }

    public void setAtmid(String atmid) {
        this.atmid = atmid;
    }

    public String getCustomername() {
        return customername;
    }

    public void setCustomername(String customername) {
        this.customername = customername;
    }

    public String getCorebankcode() {
        return corebankcode;
    }

    public void setCorebankcode(String corebankcode) {
        this.corebankcode = corebankcode;
    }

    public String getCoreaccount() {
        return coreaccount;
    }

    public void setCoreaccount(String coreaccount) {
        this.coreaccount = coreaccount;
    }

    public String getCoreaccountcurrency() {
        return coreaccountcurrency;
    }

    public void setCoreaccountcurrency(String coreaccountcurrency) {
        this.coreaccountcurrency = coreaccountcurrency;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCardpindata() {
        return cardpindata;
    }

    public void setCardpindata(String cardpindata) {
        this.cardpindata = cardpindata;
    }

    public String getEnableflag() {
        return enableflag;
    }

    public void setEnableflag(String cardenableflag) {
        this.enableflag = cardenableflag;
    }

    public String getMerchantid() {
        return merchantid;
    }

    public void setMerchantid(String merchantid) {
        this.merchantid = merchantid;
    }

    public String getDailylimit() {
        return dailylimit;
    }

    public void setDailylimit(String dailylimit) {
        this.dailylimit = dailylimit;
    }

    public String getMonthlylimit() {
        return monthlylimit;
    }

    public void setMonthlylimit(String monthlylimit) {
        this.monthlylimit = monthlylimit;
    }

    public String getYearlylimit() {
        return yearlylimit;
    }

    public void setYearlylimit(String yearlylimit) {
        this.yearlylimit = yearlylimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonIgnore
    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    @JsonIgnore
    public String getDestcurrency() {
        return destcurrency;
    }

    public void setDestcurrency(String destcurrency) {
        this.destcurrency = destcurrency;
    }

    @JsonIgnore
    public String getCorecurrency() {
        return corecurrency;
    }

    public void setCorecurrency(String corecurrency) {
        this.corecurrency = corecurrency;
    }

    @JsonIgnore
    public String getDestbank() {
        return destbank;
    }

    public void setDestbank(String destbank) {
        this.destbank = destbank;
    }

    @JsonIgnore
    public String getCorebank() {
        return corebank;
    }

    public void setCorebank(String corebank) {
        this.corebank = corebank;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public String getAllowed() {
        return allowed;
    }

    public void setAllowed(String allowed) {
        this.allowed = allowed;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDeletetype() {
        return deletetype;
    }

    public void setDeletetype(String deletetype) {
        this.deletetype = deletetype;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public String getIsprimary() {
        return isprimary;
    }

    public void setIsprimary(String isprimary) {
        this.isprimary = isprimary;
    }

    public String getAcctbalance() {
        return acctbalance;
    }

    public void setAcctbalance(String acctbalance) {
        this.acctbalance = acctbalance;
    }

    public String getAcctlimit() {
        return acctlimit;
    }

    public void setAcctlimit(String acctLimit) {
        this.acctlimit = acctLimit;
    }

    public String getAvaillimit() {
        return availlimit;
    }

    public void setAvaillimit(String availLimit) {
        this.availlimit = availLimit;
    }

    public String getAvaillimitfreq() {
        return availlimitfreq;
    }

    public void setAvaillimitfreq(String availLimitfreq) {
        this.availlimitfreq = availLimitfreq;
    }

    public List<NayaPayLimit> getNayapaylimits() {
        return nayapaylimits;
    }

    public void setNayapaylimits(List<NayaPayLimit> nayapaylimits) {
        this.nayapaylimits = nayapaylimits;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRequesttime() {
        return requesttime;
    }

    public void setRequesttime(String requesttime) {
        this.requesttime = requesttime;
    }

    public String getActivationtime() {
        return activationtime;
    }

    public void setActivationtime(String activationtime) {
        this.activationtime = activationtime;
    }

    public List<NayaPayLinkedAccount> getLinkedaccounts() {
        return linkedaccounts;
    }

    public void setLinkedaccounts(List<NayaPayLinkedAccount> linkedaccounts) {
        this.linkedaccounts = linkedaccounts;
    }

    public String getNayapaycharges() {
        return nayapaycharges;
    }

    public void setNayapaycharges(String nayapaycharges) {
        this.nayapaycharges = nayapaycharges;
    }

    public String getDestuserid() {
        return destuserid;
    }

    public void setDestuserid(String destuserid) {
        this.destuserid = destuserid;
    }

    public List<ProvisionalWallet> getProvisionalwallets() {
        return provisionalwallets;
    }

    public void setProvisionalwallets(List<ProvisionalWallet> provisionalwallets) {
        this.provisionalwallets = provisionalwallets;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAdvanceflag() {
        return advanceflag;
    }

    public void setAdvanceflag(String advanceflag) {
        this.advanceflag = advanceflag;
    }

    public List<NayaPayAccount> getAccountlist() {
        return accountlist;
    }

    public void setAccountlist(List<NayaPayAccount> accountlist) {
        this.accountlist = accountlist;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String getSecondarynumber() {
        return secondarynumber;
    }

    public void setSecondarynumber(String secondarynumber) {
        this.secondarynumber = secondarynumber;
    }

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }

    public String getInoutfilter() {
        return inoutfilter;
    }

    public void setInoutfilter(String inoutfilter) {
        this.inoutfilter = inoutfilter;
    }

    // Asim Shahzad, Date : 17th March 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115
//    public String getTypefilter() {
//        return typefilter;
//    }
//
//    public void setTypefilter(String typefilter) {
//        this.typefilter = typefilter;
//    }

    public List<String> getTypefilter() {
        return typefilter;
    }

    public void setTypefilter(List<String> typefilter) {
        this.typefilter = typefilter;
    }
    // =======================================================================================

    public String getSearchtext() {
        return searchtext;
    }

    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNayapayid() {
        return nayapayid;
    }

    public void setNayapayid(String nayapayid) {
        this.nayapayid = nayapayid;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getMerchantname() {
        return merchantname;
    }

    public void setMerchantname(String merchantname) {
        this.merchantname = merchantname;
    }

    public String getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }

    public String getTrustedflag() {
        return trustedflag;
    }

    public void setTrustedflag(String trustedflag) {
        this.trustedflag = trustedflag;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getTransactionlimit() {
        return transactionlimit;
    }

    public void setTransactionlimit(String transactionlimit) {
        this.transactionlimit = transactionlimit;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getMerchantstate() {
        return merchantstate;
    }

    public void setMerchantstate(String merchantstate) {
        this.merchantstate = merchantstate;
    }

    public String getMerchantenabled() {
        return merchantenabled;
    }

    public void setMerchantenabled(String merchantenabled) {
        this.merchantenabled = merchantenabled;
    }

    public String getMerchantblocked() {
        return merchantblocked;
    }

    public void setMerchantblocked(String merchantblocked) {
        this.merchantblocked = merchantblocked;
    }

    public String getMinimumamount() {
        return minimumamount;
    }

    public void setMinimumamount(String minimumamount) {
        this.minimumamount = minimumamount;
    }

    public String getMaximumamount() {
        return maximumamount;
    }

    public void setMaximumamount(String maximumamount) {
        this.maximumamount = maximumamount;
    }

    public String getSourcechargetype() {
        return sourcechargetype;
    }

    public void setSourcechargetype(String sourcechargetype) {
        this.sourcechargetype = sourcechargetype;
    }

    public String getDestinationchargetype() {
        return destinationchargetype;
    }

    public void setDestinationchargetype(String destinationchargetype) {
        this.destinationchargetype = destinationchargetype;
    }

    public String getConsumerno() {
        return consumerno;
    }

    public void setConsumerno(String consumerno) {
        this.consumerno = consumerno;
    }

    public String getUtilcompanyid() {
        return utilcompanyid;
    }

    public void setUtilcompanyid(String utilcompanyid) {
        this.utilcompanyid = utilcompanyid;
    }

    public String getConsumerdetail() {
        return consumerdetail;
    }

    public void setConsumerdetail(String consumerdetail) {
        this.consumerdetail = consumerdetail;
    }

    public String getBillstatus() {
        return billstatus;
    }

    public void setBillstatus(String billStatus) {
        this.billstatus = billStatus;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String dueDate) {
        this.duedate = dueDate;
    }

    public String getAmtwithinduedate() {
        return amtwithinduedate;
    }

    public void setAmtwithinduedate(String amtWithinDueDate) {
        this.amtwithinduedate = amtWithinDueDate;
    }

    public String getAmtafterduedate() {
        return amtafterduedate;
    }

    public void setAmtafterduedate(String amtAfterDueDate) {
        this.amtafterduedate = amtAfterDueDate;
    }

    public String getBillingmonth() {
        return billingmonth;
    }

    public void setBillingmonth(String billingMonth) {
        this.billingmonth = billingMonth;
    }

    public String getDatepaid() {
        return datepaid;
    }

    public void setDatepaid(String datePaid) {
        this.datepaid = datePaid;
    }

    public String getAmtpaid() {
        return amtpaid;
    }

    public void setAmtpaid(String amtPaid) {
        this.amtpaid = amtPaid;
    }

    public String getTranauthid() {
        return tranauthid;
    }

    public void setTranauthid(String tranAuthId) {
        this.tranauthid = tranAuthId;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getIdentificationno() {
        return identificationno;
    }

    public void setIdentificationno(String identificationNo) {
        this.identificationno = identificationNo;
    }

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

    public String getNayapaytrantype() {
        return nayapaytrantype;
    }

    public void setNayapaytrantype(String nayapaytrantype) {
        this.nayapaytrantype = nayapaytrantype;
    }

    public String getDestusername() {
        return destusername;
    }

    public void setDestusername(String destusername) {
        this.destusername = destusername;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public String getInvoiceid() {
        return invoiceid;
    }

    public void setInvoiceid(String invoiceid) {
        this.invoiceid = invoiceid;
    }

    public String getVerifiedflag() {
        return verifiedflag;
    }

    public void setVerifiedflag(String verifiedflag) {
        this.verifiedflag = verifiedflag;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getBanktxnflag() {
        return banktxnflag;
    }

    public void setBanktxnflag(String banktxnflag) {
        this.banktxnflag = banktxnflag;
    }

    public String getBlockedflag() {
        return blockedflag;
    }

    public void setBlockedflag(String blockedflag) {
        this.blockedflag = blockedflag;
    }

    public String getNayapaytxnid() {
        return nayapaytxnid;
    }

    public void setNayapaytxnid(String nayapaytxnid) {
        this.nayapaytxnid = nayapaytxnid;
    }

    public List<UserTransaction> getUsertransactions() {
        return usertransactions;
    }

    public void setUsertransactions(List<UserTransaction> usertransactions) {
        this.usertransactions = usertransactions;
    }

    public String getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }

    public TransactionDetail getTransactionDetail() {
        return transactionDetail;
    }

    public void setTransactionDetail(TransactionDetail transactionDetail) {
        this.transactionDetail = transactionDetail;
    }



    public String getBankcharges() {
        return bankcharges;
    }

    public void setBankcharges(String bankCharges) {
        this.bankcharges = bankCharges;
    }

    public String getBanktaxamount() {
        return banktaxamount;
    }

    public void setBanktaxamount(String bankTaxAmount) {
        this.banktaxamount = bankTaxAmount;
    }

    public String getNayapaytaxamount() {
        return nayapaytaxamount;
    }

    public void setNayapaytaxamount(String nayapayTaxAmount) {
        this.nayapaytaxamount = nayapayTaxAmount;
    }

    public String getDepositamount() {
        return depositamount;
    }

    public void setDepositamount(String depositAmount) {
        this.depositamount = depositAmount;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }

    public String getBankmnemonic() {
        return bankmnemonic;
    }

    public void setBankmnemonic(String bankMnemonic) {
        this.bankmnemonic = bankMnemonic;
    }

    public String getAcqbin() {
        return acqbin;
    }

    public void setAcqbin(String acqbin) {
        this.acqbin = acqbin;
    }

    public String getBranchcode() {
        return branchcode;
    }

    public void setBranchcode(String branchcode) {
        this.branchcode = branchcode;
    }

    public String getBranchname() {
        return branchname;
    }

    public void setBranchname(String branchname) {
        this.branchname = branchname;
    }

    public String getSlipnumber() {
        return slipnumber;
    }

    public void setSlipnumber(String slipnumber) {
        this.slipnumber = slipnumber;
    }

    public String getTellerid() {
        return tellerid;
    }

    public void setTellerid(String tellerid) {
        this.tellerid = tellerid;
    }

    public String getSecretquestion1() {
        return secretquestion1;
    }

    public void setSecretquestion1(String secretquestion1) {
        this.secretquestion1 = secretquestion1;
    }

    public String getSecretquestionanswer1() {
        return secretquestionanswer1;
    }

    public void setSecretquestionanswer1(String secretquestionanswer1) {
        this.secretquestionanswer1 = secretquestionanswer1;
    }

    public String getSecretquestion2() {
        return secretquestion2;
    }

    public void setSecretquestion2(String secretquestion2) {
        this.secretquestion2 = secretquestion2;
    }

    public String getSecretquestionanswer2() {
        return secretquestionanswer2;
    }

    public void setSecretquestionanswer2(String secretquestionanswer2) {
        this.secretquestionanswer2 = secretquestionanswer2;
    }

    public SecurityParams getSecurityparams() {
        return securityparams;
    }

    public void setSecurityparams(SecurityParams securityparams) {
        this.securityparams = securityparams;
    }
	
    public String getPartialflag() {
        return partialflag;
    }

    public void setPartialflag(String partialflag) {
        this.partialflag = partialflag;
    }

    public String getSettlementdelay() {
        return settlementdelay;
    }

    public void setSettlementdelay(String settlementdelay) {
        this.settlementdelay = settlementdelay;
    }

    public String getPagecount() {
        return pagecount;
    }

    public void setPagecount(String pagecount) {
        this.pagecount = pagecount;
    }

    public String getPagesize() {
        return pagesize;
    }

    public void setPagesize(String pagesize) {
        this.pagesize = pagesize;
    }

    public String getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(String totalcount) {
        this.totalcount = totalcount;
    }

    public String getBillerid() {
        return billerid;
    }

    public void setBillerid(String billerid) {
        this.billerid = billerid;
    }

    public String getBillername() {
        return billername;
    }

    public void setBillername(String billername) {
        this.billername = billername;
    }

    public String getIncomingip() {
        return incomingip;
    }

    public void setIncomingip(String incomingip) {
        this.incomingip = incomingip;
    }

    public String getCodflag() {
        return codflag;
    }

    public void setCodflag(String codflag) {
        this.codflag = codflag;
    }

    public String getTempblockflag() {
        return tempblockflag;
    }

    public void setTempblockflag(String tempblockflag) {
        this.tempblockflag = tempblockflag;
    }

    public String getCardexpiry() {
        return cardexpiry;
    }

    public void setCardexpiry(String cardexpiry) {
        this.cardexpiry = cardexpiry;
    }

    //Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
    public String getCardlastdigits() { return cardlastdigits; }

    public void setCardlastdigits(String cardlastdigits) {
        this.cardlastdigits = cardlastdigits;
    }
    //===============================================================================================================

    public String getAcctStatus() {
        return acctStatus;
    }

    public void setAcctStatus(String acctStatus) {
        this.acctStatus = acctStatus;
    }



    public String getTermloc() {
        return termloc;
    }

    public void setTermloc(String termloc) {
        this.termloc = termloc;
    }

    public String getIsmerchantonline() {
        return ismerchantonline;
    }

    public void setIsmerchantonline(String ismerchantonline) {
        this.ismerchantonline = ismerchantonline;
    }

    public String getIswalletaccount() {
        return iswalletaccount;
    }

    public void setIswalletaccount(String iswalletaccount) {
        this.iswalletaccount = iswalletaccount;
    }

    public String getMerchantamount() {
        return merchantamount;
    }

    public void setMerchantamount(String merchantamount) {
        this.merchantamount = merchantamount;
    }
	
    public String getOriginalapi() {
        return originalapi;
    }

    public void setOriginalapi(String origialapi) {
        this.originalapi = origialapi;
    }

    public List<TransactionDetail> getReversal() {
        return reversal;
    }

    public void setReversal(List<TransactionDetail> reversal) {
        this.reversal = reversal;
    }

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public String getTrack3Data() {
        return track3Data;
    }

    public void setTrack3Data(String track3Data) {
        this.track3Data = track3Data;
    }

    public String getTrack1Data() {
        return track1Data;
    }

    public void setTrack1Data(String track1Data) {
        this.track1Data = track1Data;
    }

    public String getIcccarddata() {
        return icccarddata;
    }

    public void setIcccarddata(String icccarddata) {
        this.icccarddata = icccarddata;
    }

    public String getServicecode() {
        return servicecode;
    }

    public void setServicecode(String servicecode) {
        this.servicecode = servicecode;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(String cvv2) {
        this.cvv2 = cvv2;
    }

    public String getIcvv() {
        return icvv;
    }

    public void setIcvv(String icvv) {
        this.icvv = icvv;
    }

    public String getPosentrymode() {
        return posentrymode;
    }

    public void setPosentrymode(String posentrymode) {
        this.posentrymode = posentrymode;
    }
	
    public String getAmttranfee() {
        return amttranfee;
    }

    public void setAmttranfee(String amttranfee) {
        this.amttranfee = amttranfee;
    }

    public String getSelfdefinedata() {
        return selfdefinedata;
    }

    public void setSelfdefinedata(String selfdefinedata) {
        this.selfdefinedata = selfdefinedata;
    }

    public String getDecryptedotp() {
        return decryptedotp;
    }

    public void setDecryptedotp(String decryptedotp) {
        this.decryptedotp = decryptedotp;
    }

    public String getCiphereddata() {
        return ciphereddata;
    }

    public void setCiphereddata(String ciphereddata) {
        this.ciphereddata = ciphereddata;
    }

    public String getAcctlevel() {
        return acctlevel;
    }

    public void setAcctlevel(String acctlevel) {
        this.acctlevel = acctlevel;
    }

    public String getTrackingid() {
        return trackingid;
    }

    public void setTrackingid(String trackingid) {
        this.trackingid = trackingid;
    }

    public List<CardObject> getCardobjectlist() {
        return cardobjectlist;
    }

    public void setCardobjectlist(List<CardObject> cardobjectlist) {
        this.cardobjectlist = cardobjectlist;
    }

    public String getPosinvoiceref() {
        return posinvoiceref;
    }

    public void setPosinvoiceref(String posinvoiceref) {
        this.posinvoiceref = posinvoiceref;
    }

    public String getMapid() {
        return mapid;
    }

    public void setMapid(String mapid) {
        this.mapid = mapid;
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

    public String getBlocktype() {
        return blocktype;
    }

    public void setBlocktype(String blocktype) {
        this.blocktype = blocktype;
    }

    public String getLockstate() {
        return lockstate;
    }

    public void setLockstate(String lockstate) {
        this.lockstate = lockstate;
    }

    public String getFinancialflag() {
        return financialflag;
    }

    public void setFinancialflag(String financialflag) {
        this.financialflag = financialflag;
    }

    public String getTrancurrency() {
        return trancurrency;
    }

    public void setTrancurrency(String trancurrency) {
        this.trancurrency = trancurrency;
    }

    public String getOrigstan() {
        return origstan;
    }

    public void setOrigstan(String origstan) {
        this.origstan = origstan;
    }

    public String getOrigtransdatetime() {
        return origtransdatetime;
    }

    public void setOrigtransdatetime(String origtransdatetime) {
        this.origtransdatetime = origtransdatetime;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    // Author: Asim Shahzad, Date : 24th Feb 2020, Desc : For getting Nayapay mobile application download counts from middleware

    public String getNpappdownloadcount() {
        return npappdownloadcount;
    }

    public void setNpappdownloadcount(String npAppDownloadCount) {
        this.npappdownloadcount = npAppDownloadCount;
    }

    public String getAvailablebaldebitacc() {
        return availablebaldebitacc;
    }

    public void setAvailablebaldebitacc(String availablebaldebitacc) {
        this.availablebaldebitacc = availablebaldebitacc;
    }

    public String getActualbaldebitacc() {
        return actualbaldebitacc;
    }

    public void setActualbaldebitacc(String actualbaldebitacc) {
        this.actualbaldebitacc = actualbaldebitacc;
    }

    public String getAvailablebalcreditacc() {
        return availablebalcreditacc;
    }

    public void setAvailablebalcreditacc(String availablebalcreditacc) {
        this.availablebalcreditacc = availablebalcreditacc;
    }

    public String getActualbalcreditacc() {
        return actualbalcreditacc;
    }

    public void setActualbalcreditacc(String actualbalcreditacc) {
        this.actualbalcreditacc = actualbalcreditacc;
    }

    public String getFundsvoucherid() {
        return fundsvoucherid;
    }

    public void setFundsvoucherid(String fundsvoucherid) {
        this.fundsvoucherid = fundsvoucherid;
    }

	//m.rehman: Euronet Integration
    public String getCavvdata() {
        return cavvdata;
    }

    public void setCavvdata(String cavvdata) {
        this.cavvdata = cavvdata;
    }
    // Asim Shahzad, Date : 24th May 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115
    public String getCbillamount() {
        return cbillamount;
    }

    public void setCbillamount(String cbillamount) {
        this.cbillamount = cbillamount;
    }
	// =======================================================================================
    public String getCbillcurrency() {
        return cbillcurrency;
    }

    public void setCbillcurrency(String cbillcurrency) {
        this.cbillcurrency = cbillcurrency;
    }

    public String getCbillrate() {
        return cbillrate;
    }

    public void setCbillrate(String cbillrate) {
        this.cbillrate = cbillrate;
    }

    public String getBenebankcode() {
        return benebankcode;
    }

    public void setBenebankcode(String benebankcode) {
        this.benebankcode = benebankcode;
    }

    public String getBenebankaccountno() {
        return benebankaccountno;
    }

    public void setBenebankaccountno(String benebankaccountno) {
        this.benebankaccountno = benebankaccountno;
    }

    public String getBeneaccounttitle() {
        return beneaccounttitle;
    }

    public void setBeneaccounttitle(String beneaccounttitle) {
        this.beneaccounttitle = beneaccounttitle;
    }

    public String getBeneflag() {
        return beneflag;
    }

    public void setBeneflag(String beneflag) {
        this.beneflag = beneflag;
    }

    public String getBeneaccountalias() {
        return beneaccountalias;
    }

    public void setBeneaccountalias(String beneaccountalias) {
        this.beneaccountalias = beneaccountalias;
    }

    public String getBeneid() {
        return beneid;
    }

    public void setBeneid(String beneid) {
        this.beneid = beneid;
    }

    public String getBeneemailid() {
        return beneemailid;
    }

    public void setBeneemailid(String beneemailid) {
        this.beneemailid = beneemailid;
    }

    public String getBenemobileno() {
        return benemobileno;
    }

    public void setBenemobileno(String benemobileno) {
        this.benemobileno = benemobileno;
    }

    public String getPurposeoftransaction() {
        return purposeoftransaction;
    }

    public void setPurposeoftransaction(String purposeoftransaction) {
        this.purposeoftransaction = purposeoftransaction;
    }

    // Asim Shahzad, Date : 22nd Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104

    public String getCardscheme() {
        return cardscheme;
    }

    public void setCardscheme(String cardscheme) {
        this.cardscheme = cardscheme;
    }

    // =====================================================================================
	
    //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
    public String getOrigretrefno() {
        return origretrefno;
    }

    public void setOrigretrefno(String origretrefno) {
        this.origretrefno = origretrefno;
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

	//m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
 	public String getGlobalcardlimit() {
        return globalcardlimit;
    }

    public void setGlobalcardlimit(String globalcardlimit) {
        this.globalcardlimit = globalcardlimit;
    }

    public String getCashwithdrawallimit() {
        return cashwithdrawallimit;
    }

    public void setCashwithdrawallimit(String cashwithdrawallimit) {
        this.cashwithdrawallimit = cashwithdrawallimit;
    }

    public String getPurchaselimit() {
        return purchaselimit;
    }

    public void setPurchaselimit(String purchaselimit) {
        this.purchaselimit = purchaselimit;
    }

    public String getOnlinetxnlimit() {
        return onlinetxnlimit;
    }

    public void setOnlinetxnlimit(String onlinetxnlimit) {
        this.onlinetxnlimit = onlinetxnlimit;
    }
	///////////////////////////////////////////////////////////////////////////////////////////
    
	//m.rehman: Euronet Integration
	public String getAddresponsedata() {
        return addresponsedata;
    }

    public void setAddresponsedata(String addresponsedata) {
        this.addresponsedata = addresponsedata;
    }

    //m.rehman: 15-02-2021, VP-NAP-202102101 / VC-NAP-202102101 - Visa - Switch Middleware Integration Document V_4.7.7-A - Release 2
    public String getCustomlimitflag() {
        return customlimitflag;
    }

    public void setCustomlimitflag(String customlimitflag) {
        this.customlimitflag = customlimitflag;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 05-03-2021, VP-NAP-202103041/ VC-NAP-202103041 - Merchant Transaction Listing Issue
    public List<WalletCMSWsListingEntity> getWsloglist() {
        return wsloglist;
    }

    public void setWsloglist(List<WalletCMSWsListingEntity> wsloglist) {
        this.wsloglist = wsloglist;
    }

    public WalletCMSWsListingEntity getWslog() {
        return wslog;
    }

    public void setWslog(WalletCMSWsListingEntity wslog) {
        this.wslog = wslog;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //    @JsonIgnore
//    public ProcessContext getProcessContext() {
//        if(processContext == null) {
//            processContext = new ProcessContext(true);
//            return processContext;
//        }
//        else
//        {
//            return processContext;
//        }
//    }

//    public void setProcessContext(ProcessContext processContext) {
//        this.processContext = processContext;
//    }

    // Asim Shahzad, Date : 12th March 2021, Tracking ID : VP-NAP-202103113 / VC-NAP-202103113

    @Transient
    private String nameoncard;

    public String getNameoncard() {
        return nameoncard;
    }

    public void setNameoncard(String nameoncard) {
        this.nameoncard = nameoncard;
    }

    // ========================================================================================

    // Asim Shahzad, Date : 11th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111

    public String getReasonofclosure() {
        return reasonofclosure;
    }

    public void setReasonofclosure(String reasonofclosure) {
        this.reasonofclosure = reasonofclosure;
    }

    public String getApprovinguser() {
        return approvinguser;
    }

    public void setApprovinguser(String approvinguser) {
        this.approvinguser = approvinguser;
    }

    public String getClosurerequestdatetime() {
        return closurerequestdatetime;
    }

    public void setClosurerequestdatetime(String closurerequestdatetime) {
        this.closurerequestdatetime = closurerequestdatetime;
    }


    // ========================================================================================

    // Asim Shahzad, Date : 16th March 2021, Tracking ID : VP-NAP-202103115 / VC-NAP-202103115

    public String getTotalamountspent() {
        return totalamountspent;
    }

    public void setTotalamountspent(String totalamountspent) {
        this.totalamountspent = totalamountspent;
    }

    public String getTotalamountreceived() {
        return totalamountreceived;
    }

    public void setTotalamountreceived(String totalamountreceived) {
        this.totalamountreceived = totalamountreceived;
    }

    public String getFromdatetime() {
        return fromdatetime;
    }

    public void setFromdatetime(String fromdatetime) {
        this.fromdatetime = fromdatetime;
    }

    public String getTodatetime() {
        return todatetime;
    }

    public void setTodatetime(String todatetime) {
        this.todatetime = todatetime;
    }

    // ========================================================================================

    // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116

    @Column(name="SETT_RATE")
    private String settlementrate;

    public String getSettlementrate() {
        return settlementrate;
    }

    public void setSettlementrate(String settlementrate) {
        this.settlementrate = settlementrate;
    }

    @Column(name = "WITHHOLDING_TAX_AMT")
    private String withholdingtaxamount;

    public String getWithholdingtaxamount() {
        return withholdingtaxamount;
    }

    public void setWithholdingtaxamount(String withholdingtaxamount) {
        this.withholdingtaxamount = withholdingtaxamount;
    }

    // =======================================================================================

    // Asim Shahzad, Date : 7th May 2021, Tracking ID : VP-NAP-202105051 / VG-NAP-202105051/ VC-NAP-202105051
    @Column(name = "DESTCNIC")
    private String destcnic;

    public String getDestcnic() {
        return destcnic;
    }

    public void setDestcnic(String destcnic) {
        this.destcnic = destcnic;
    }
    // ======================================================================================================
    //m.rehman: 07-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    public String getSettledflag() {
        return settledflag;
    }

    public void setSettledflag(String settledflag) {
        this.settledflag = settledflag;
    }

    public String getMerchantfavorflag() {
        return merchantfavorflag;
    }

    public void setMerchantfavorflag(String merchantfavorflag) {
        this.merchantfavorflag = merchantfavorflag;
    }

    public String getDebitwalletflag() {
        return debitwalletflag;
    }

    public void setDebitwalletflag(String debitwalletflag) {
        this.debitwalletflag = debitwalletflag;
    }

    public String getCreditwalletflag() {
        return creditwalletflag;
    }

    public void setCreditwalletflag(String creditwalletflag) {
        this.creditwalletflag = creditwalletflag;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 29-04-2021, VG-NAP-202104271 / VP-NAP-202104261 / VC-NAP-202104261 - VISA transaction charging update
    public WalletCMSWsListingEntity copy(WalletCMSWsEntity cmsWsEntity) {
        //WalletCMSWsListingEntity wsEntity = new WalletCMSWsListingEntity();
        this.setServicename(cmsWsEntity.getServicename());
        this.setMobilenumber(cmsWsEntity.getMobilenumber());
        this.setCnic(cmsWsEntity.getCnic());
        this.setCustomername(cmsWsEntity.getCustomername());
        this.setCnicexpiry(cmsWsEntity.getCnicexpiry());
        this.setBankcode(cmsWsEntity.getBankcode());
        this.setDestbankcode(cmsWsEntity.getDestbankcode());
        this.setBankname(cmsWsEntity.getBankname());
        this.setAccountnumber(cmsWsEntity.getAccountnumber());
        this.setAccountcurrency(cmsWsEntity.getAccountcurrency());
        this.setTranrefnumber(cmsWsEntity.getTranrefnumber());
        this.setMothername(cmsWsEntity.getMothername());
        this.setDateofbirth(cmsWsEntity.getDateofbirth());
        this.setTransdatetime(cmsWsEntity.getTransdatetime());
        this.setPindata(cmsWsEntity.getPindata());
        this.setOldpindata(cmsWsEntity.getOldpindata());
        this.setNewpindata(cmsWsEntity.getNewpindata());
        this.setEncryptkey(cmsWsEntity.getEncryptkey());
        this.setAmounttransaction(cmsWsEntity.getAmounttransaction());
        this.setSrcchargeamount(cmsWsEntity.getSrcchargeamount());
        this.setDestchargeamount(cmsWsEntity.getDestchargeamount());
        this.setDestaccount(cmsWsEntity.getDestaccount());
        this.setDestaccountcurrency(cmsWsEntity.getDestaccountcurrency());
        this.setOtp(cmsWsEntity.getOtp());
        this.setBiometricdata(cmsWsEntity.getBiometricdata());
        this.setRespcode(cmsWsEntity.getRespcode());
        this.setPlaceofbirth(cmsWsEntity.getPlaceofbirth());
        this.setFathername(cmsWsEntity.getFathername());
        this.setProvince(cmsWsEntity.getProvince());
        this.setTsp(cmsWsEntity.getTsp());
        this.setDestnayapayid(cmsWsEntity.getDestnayapayid());
        this.setOrigdataelement(cmsWsEntity.getOrigdataelement());
        this.setAtmid(cmsWsEntity.getAtmid());
        this.setCorebankcode(cmsWsEntity.getCorebankcode());
        this.setCoreaccount(cmsWsEntity.getCoreaccount());
        this.setCoreaccountcurrency(cmsWsEntity.getCoreaccountcurrency());
        this.setCardnumber(cmsWsEntity.getCardnumber());
        this.setCardpindata(cmsWsEntity.getCardpindata());
        this.setEnableflag(cmsWsEntity.getEnableflag());
        this.setMerchantid(cmsWsEntity.getMerchantid());
        this.setDailylimit(cmsWsEntity.getDailylimit());
        this.setMonthlylimit(cmsWsEntity.getMonthlylimit());
        this.setYearlylimit(cmsWsEntity.getYearlylimit());
        this.setStatus(cmsWsEntity.getStatus());
        this.setStan(cmsWsEntity.getStan());
        this.setRrn(cmsWsEntity.getRrn());
        this.setUserid(cmsWsEntity.getUserid());
        this.setChannelid(cmsWsEntity.getChannelid());
        this.setAllowed(cmsWsEntity.getAllowed());
        this.setDeletetype(cmsWsEntity.getDeletetype());
        this.setComments(cmsWsEntity.getComments());
        this.setAcctid(cmsWsEntity.getAcctid());
        this.setAcctalias(cmsWsEntity.getAcctalias());
        this.setIsprimary(cmsWsEntity.getIsprimary());
        this.setAcctbalance(cmsWsEntity.getAcctbalance());
        this.setAcctlimit(cmsWsEntity.getAcctlimit());
        this.setAvaillimit(cmsWsEntity.getAvaillimit());
        this.setAvaillimitfreq(cmsWsEntity.getAvaillimitfreq());
        this.setState(cmsWsEntity.getState());
        this.setRequesttime(cmsWsEntity.getRequesttime());
        this.setActivationtime(cmsWsEntity.getActivationtime());
        this.setNayapaycharges(cmsWsEntity.getNayapaycharges());
        this.setDestuserid(cmsWsEntity.getDestuserid());
        this.setAddress(cmsWsEntity.getAddress());
        this.setCity(cmsWsEntity.getCity());
        this.setCountry(cmsWsEntity.getCountry());
        this.setAdvanceflag(cmsWsEntity.getAdvanceflag());
        this.setSecondarynumber(cmsWsEntity.getSecondarynumber());
        this.setAccesstoken(cmsWsEntity.getAccesstoken());
        this.setInoutfilter(cmsWsEntity.getInoutfilter());
        this.setTypefilter(cmsWsEntity.getTypefilter());
        this.setSearchtext(cmsWsEntity.getSearchtext());
        this.setUsername(cmsWsEntity.getUsername());
        this.setNayapayid(cmsWsEntity.getNayapayid());
        this.setParentid(cmsWsEntity.getParentid());
        this.setMerchantname(cmsWsEntity.getMerchantname());
        this.setCategoryid(cmsWsEntity.getCategoryid());
        this.setTrustedflag(cmsWsEntity.getTrustedflag());
        this.setPhonenumber(cmsWsEntity.getPhonenumber());
        this.setTransactionlimit(cmsWsEntity.getTransactionlimit());
        this.setCategoryname(cmsWsEntity.getCategoryname());
        this.setMerchantstate(cmsWsEntity.getMerchantstate());
        this.setMerchantenabled(cmsWsEntity.getMerchantenabled());
        this.setMerchantblocked(cmsWsEntity.getMerchantblocked());
        this.setMinimumamount(cmsWsEntity.getMinimumamount());
        this.setMaximumamount(cmsWsEntity.getMaximumamount());
        this.setSourcechargetype(cmsWsEntity.getSourcechargetype());
        this.setDestinationchargetype(cmsWsEntity.getDestinationchargetype());
        this.setConsumerno(cmsWsEntity.getConsumerno());
        this.setUtilcompanyid(cmsWsEntity.getUtilcompanyid());
        this.setConsumerdetail(cmsWsEntity.getConsumerdetail());
        this.setBillstatus(cmsWsEntity.getBillstatus());
        this.setDuedate(cmsWsEntity.getDuedate());
        this.setAmtwithinduedate(cmsWsEntity.getAmtwithinduedate());
        this.setAmtafterduedate(cmsWsEntity.getAmtafterduedate());
        this.setBillingmonth(cmsWsEntity.getBillingmonth());
        this.setDatepaid(cmsWsEntity.getDatepaid());
        this.setAmtpaid(cmsWsEntity.getAmtpaid());
        this.setTranauthid(cmsWsEntity.getTranauthid());
        this.setReserved(cmsWsEntity.getReserved());
        this.setIdentificationno(cmsWsEntity.getIdentificationno());
        this.setPing(cmsWsEntity.getPing());
        this.setNayapaytrantype(cmsWsEntity.getNayapaytrantype());
        this.setDestusername(cmsWsEntity.getDestusername());
        this.setAgentid(cmsWsEntity.getAgentid());
        this.setReferencenumber(cmsWsEntity.getReferencenumber());
        this.setInvoiceid(cmsWsEntity.getInvoiceid());
        this.setVerifiedflag(cmsWsEntity.getVerifiedflag());
        this.setStartdate(cmsWsEntity.getStartdate());
        this.setEnddate(cmsWsEntity.getEnddate());
        this.setBanktxnflag(cmsWsEntity.getBanktxnflag());
        this.setBlockedflag(cmsWsEntity.getBlockedflag());
        this.setMerchantenabled(cmsWsEntity.getMerchantenabled());
        this.setNayapaytxnid(cmsWsEntity.getNayapaytxnid());
        this.setCreationdate(cmsWsEntity.getCreationdate());
        this.setBankmnemonic(cmsWsEntity.getBankmnemonic());
        this.setNayapaylimits(cmsWsEntity.getNayapaylimits());
        this.setLinkedaccounts(cmsWsEntity.getLinkedaccounts());
        this.setProvisionalwallets(cmsWsEntity.getProvisionalwallets());
        this.setAccountlist(cmsWsEntity.getAccountlist());
        this.setTransactions(cmsWsEntity.getTransactions());
        this.setUsertransactions(cmsWsEntity.getUsertransactions());
        this.setMerchantenabled(cmsWsEntity.getMerchantenabled());
        this.setTransactionDetail(cmsWsEntity.getTransactionDetail());
        this.setCurrency(cmsWsEntity.getCurrency());
        this.setBank(cmsWsEntity.getBank());
        this.setDestbank(cmsWsEntity.getDestbank());
        this.setCorebank(cmsWsEntity.getCorebank());
        this.setDestcurrency(cmsWsEntity.getDestcurrency());
        this.setCorecurrency(cmsWsEntity.getCorecurrency());
        this.setBillerid(cmsWsEntity.getBillerid());
        this.setBillername(cmsWsEntity.getBillername());
        this.setConsumerno(cmsWsEntity.getConsumerno());
        this.setAcctStatus(cmsWsEntity.getAcctStatus());
        this.setTerminalid(cmsWsEntity.getTerminalid());
        this.setBankcode(cmsWsEntity.getBankcode());
        this.setDecryptedotp(cmsWsEntity.getDecryptedotp());
        this.setCiphereddata(cmsWsEntity.getCiphereddata());
        this.setAmttranfee(cmsWsEntity.getAmttranfee());
        this.setAcctlevel(cmsWsEntity.getAcctlevel());
        this.setMapid(cmsWsEntity.getMapid());
        this.setPosinvoiceref(cmsWsEntity.getPosinvoiceref());
        this.setDisputeflag(cmsWsEntity.getDisputeflag());
        this.setBlocktype(cmsWsEntity.getBlocktype());
        this.setLockstate(cmsWsEntity.getLockstate());
        this.setFinancialflag(cmsWsEntity.getFinancialflag());
        this.setNameoncard(cmsWsEntity.getNameoncard());
        this.setReasonofclosure(cmsWsEntity.getReasonofclosure());
        this.setApprovinguser(cmsWsEntity.getApprovinguser());
        this.setClosurerequestdatetime(cmsWsEntity.getClosurerequestdatetime());
        this.setTotalamountspent(cmsWsEntity.getTotalamountspent());
        this.setTotalamountreceived(cmsWsEntity.getTotalamountreceived());
        this.setFromdatetime(cmsWsEntity.getFromdatetime());
        this.setTodatetime(cmsWsEntity.getTodatetime());
        this.setBranchcode(cmsWsEntity.getBranchcode());
        this.setTrancurrency(cmsWsEntity.getTrancurrency());
        this.setSettlementrate(cmsWsEntity.getSettlementrate());
        this.setCbillamount(cmsWsEntity.getCbillamount());
        this.setCbillcurrency(cmsWsEntity.getCbillcurrency());
        this.setCardexpiry(cmsWsEntity.getCardexpiry());
        this.setPosentrymode(cmsWsEntity.getPosentrymode());
        this.setTrack1Data(cmsWsEntity.getTrack1Data());
        this.setTrack2Data(cmsWsEntity.getTrack2Data());
        this.setTrack3Data(cmsWsEntity.getTrack3Data());
        this.setIcccarddata(cmsWsEntity.getIcccarddata());
        this.setSelfdefinedata(cmsWsEntity.getSelfdefinedata());
        this.setBranchname(cmsWsEntity.getBranchname());
        this.setTermloc(cmsWsEntity.getTermloc());
        this.setAcqbin(cmsWsEntity.getAcqbin());
        this.setCavvdata(cmsWsEntity.getCavvdata());
        this.setCvv2(cmsWsEntity.getCvv2());
        this.setCardscheme(cmsWsEntity.getCardscheme());
		//Arsalan Akhter, Date:15-Sept-2021, Ticket:VP-NAP-202109151/VC-NAP-202109151(cardlastdigits parameter name should be same in all relevant calls)
        this.setCardlastdigits(cmsWsEntity.getCardlastdigits());
		//===============================================================================================================
        this.setId(cmsWsEntity.getId());
        //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
        this.setDeclinedbycardctrl(cmsWsEntity.getDeclinedbycardctrl());
        //=====================================================================================================================
        //Arsalan Akhter, Date: 20-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
        this.setLimittype(cmsWsEntity.getLimittype());
        //======================================================================================================
        this.setTotalamount(cmsWsEntity.getTotalamount());
        this.setBankcharges(cmsWsEntity.getBankcharges());
        this.setBanktaxamount(cmsWsEntity.getBanktaxamount());
        this.setNayapaytaxamount(cmsWsEntity.getNayapaytaxamount());
        this.setDepositamount(cmsWsEntity.getDepositamount());
        this.setSecretquestion1(cmsWsEntity.getSecretquestion1());
        this.setSecretquestion2(cmsWsEntity.getSecretquestion2());
        this.setSecretquestionanswer1(cmsWsEntity.getSecretquestionanswer1());
        this.setSecretquestionanswer2(cmsWsEntity.getSecretquestionanswer2());
        this.setReversal(cmsWsEntity.getReversal());
        this.setSlipnumber(cmsWsEntity.getSlipnumber());
        this.setTellerid(cmsWsEntity.getTellerid());
        this.setCodflag(cmsWsEntity.getCodflag());
        this.setSettlementdelay(cmsWsEntity.getSettlementdelay());
        this.setPagecount(cmsWsEntity.getPagecount());
        this.setPagesize(cmsWsEntity.getPagesize());
        this.setTotalcount(cmsWsEntity.getTotalcount());
        this.setPartialflag(cmsWsEntity.getPartialflag());
        this.setIncomingip(cmsWsEntity.getIncomingip());
        this.setTempblockflag(cmsWsEntity.getTempblockflag());
        this.setMerchantamount(cmsWsEntity.getMerchantamount());
        this.setOriginalapi(cmsWsEntity.getOriginalapi());
        this.setTrackingid(cmsWsEntity.getTrackingid());
        this.setOrigstan(cmsWsEntity.getOrigstan());
        this.setOrigtransdatetime(cmsWsEntity.getOrigtransdatetime());
        this.setOccupation(cmsWsEntity.getOccupation());
        this.setNpappdownloadcount(cmsWsEntity.getNpappdownloadcount());
        this.setBenebankcode(cmsWsEntity.getBenebankcode());
        this.setBenebankaccountno(cmsWsEntity.getBenebankaccountno());
        this.setBeneaccounttitle(cmsWsEntity.getBeneaccounttitle());
        this.setBeneflag(cmsWsEntity.getBeneflag());
        this.setBeneaccountalias(cmsWsEntity.getBeneaccountalias());
        this.setBeneid(cmsWsEntity.getBeneid());
        this.setBeneemailid(cmsWsEntity.getBeneemailid());
        this.setBenemobileno(cmsWsEntity.getBenemobileno());
        this.setPurposeoftransaction(cmsWsEntity.getPurposeoftransaction());
        this.setCardscheme(cmsWsEntity.getCardscheme());
        this.setOrigretrefno(cmsWsEntity.getOrigretrefno());
        this.setNpticket(cmsWsEntity.getNpticket());
        this.setVrolticket(cmsWsEntity.getVrolticket());
        this.setIsChipPinEnabled(cmsWsEntity.getIsChipPinEnabled());
        this.setIsMagStripeEnabled(cmsWsEntity.getIsMagStripeEnabled());
        this.setIsCashWithdrawalEnabled(cmsWsEntity.getIsCashWithdrawalEnabled());
        this.setIsNFCEnabled(cmsWsEntity.getIsNFCEnabled());
        this.setIsOnlineEnabled(cmsWsEntity.getIsOnlineEnabled());
        this.setIsInternationalTxnsEnabled(cmsWsEntity.getIsInternationalTxnsEnabled());
        this.setCardtype(cmsWsEntity.getCardtype());
        this.setSettlementamount(cmsWsEntity.getSettlementamount());
        this.setReserved3(cmsWsEntity.getReserved3());
        this.setSettlementrate(cmsWsEntity.getSettlementrate());
        this.setWithholdingtaxamount(cmsWsEntity.getWithholdingtaxamount());
        this.setDestcnic(cmsWsEntity.getDestcnic());
        this.setIban(cmsWsEntity.getIban());
        this.setAcqcountrycode(cmsWsEntity.getAcqcountrycode());
        this.setAmountFCY(cmsWsEntity.getAmountFCY());
        this.setCurrencyFCY(cmsWsEntity.getCurrencyFCY());
        this.setExchangeRate(cmsWsEntity.getExchangeRate());
        // Asim Shahzad, Date : 20th Dec 2023, Tracking ID : NAP-P5-23
        this.setIsonustranx(cmsWsEntity.getIsonustranx());
        this.setReceiptcharges(cmsWsEntity.getReceiptcharges());
        this.setBalanceinquirycharges(cmsWsEntity.getBalanceinquirycharges());
        // ===========================================================

        this.setIncludeinstatement(cmsWsEntity.getIncludeinstatement());  //Added By Huzaifa 29/04/2024

        this.setOpeningbalance(cmsWsEntity.getOpeningbalance());//Added By Huzaifa 29/04/2024
        this.setClosingbalance(cmsWsEntity.getClosingbalance()); //Added By Huzaifa 29/04/2024

        this.setDestOpeningbalance(cmsWsEntity.getDestOpeningbalance());//Added By Huzaifa 29/04/2024
        this.setDestClosingbalance(cmsWsEntity.getDestClosingbalance()); //Added By Huzaifa 29/04/2024

        return this;
    }

    public List<CardCharge> getCardchargeslist() {
        return cardchargeslist;
    }

    public void setCardchargeslist(List<CardCharge> cardchargeslist) {
        this.cardchargeslist = cardchargeslist;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241

    @Column(name = "IBAN")
    private String iban;

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    // ======================================================================================

    // Asim Shahzad, Date : 10 Aug 2021, Tracking ID : VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091

    @Transient
    private String monthstartingbalance;

    @Transient
    private String monthendingbalance;

    public String getMonthstartingbalance() {
        return monthstartingbalance;
    }

    public void setMonthstartingbalance(String monthstartingbalance) {
        this.monthstartingbalance = monthstartingbalance;
    }

    public String getMonthendingbalance() {
        return monthendingbalance;
    }

    public void setMonthendingbalance(String monthendingbalance) {
        this.monthendingbalance = monthendingbalance;
    }

    // =====================================================================================================

    // Asim Shahzad, Date : 24th Aug 2021, Tracking ID : VP-NAP-202108161 / VC-NAP-202108161
    @Column(name = "ACQ_COUNTRY")
    private String acqcountrycode;

    public String getAcqcountrycode() {
        return acqcountrycode;
    }

    public void setAcqcountrycode(String acqcountrycode) {
        this.acqcountrycode = acqcountrycode;
    }
    // =====================================================================================

    // Asim Shahzad, Date : 1st Sep 2021, Tracking ID : VC-NAP-202108271

    @Column(name = "FCY_AMOUNT")
    private String amountFCY;

    @Column(name = "FCY_CURRENCY")
    private String currencyFCY;

    @Column(name = "EXCHANGE_RATE")
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
    
	//m.rehman: 02-09-2021, VC-NAP-202108231 - Complete transaction details missing in dispute refund transaction
    public String getOrigChannelId() {
        return origChannelId;
    }

    public void setOrigChannelId(String origChannelId) {
        this.origChannelId = origChannelId;
    }
    //Arsalan Akhter, Date: 09-Sept-2021, Ticket: VC-NAP-202108231(Complete transaction details missing in dispute refund transaction)
    public String getReserved3() { return reserved3; }

    public void setReserved3(String reserved3) { this.reserved3 = reserved3; }
    //==================================================================================================================

    //Arsalan Akhter, Date: 07-Oct-2021, Ticket: VP-NAP-202110051 / VC-NAP-202110053(Document 4.9.1 - Notifications Update)
    public String getDeclinedbycardctrl() { return declinedbycardctrl; }

    public void setDeclinedbycardctrl(String declinedbycardctrl) { this.declinedbycardctrl = declinedbycardctrl; }
    //=====================================================================================================================

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    public int getIncludeinstatement() {
        return includeinstatement;
    }

    public void setIncludeinstatement(int includeInStatement) {
        this.includeinstatement = includeInStatement;
    }

    public String getOpeningbalance() {
        return openingbalance;
    }

    public void setOpeningbalance(String openingbalance) {
        this.openingbalance = openingbalance;
    }

    public String getClosingbalance() {
        return closingbalance;
    }

    public void setClosingbalance(String closingbalance) {
        this.closingbalance = closingbalance;
    }

    public String getDestOpeningbalance() {
        return destOpeningbalance;
    }

    public void setDestOpeningbalance(String destOpeningbalance) {
        this.destOpeningbalance = destOpeningbalance;
    }

    public String getDestClosingbalance() {
        return destClosingbalance;
    }

    public void setDestClosingbalance(String destClosingbalance) {
        this.destClosingbalance = destClosingbalance;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Arsalan Akhter, Date: 21-Oct-2021, Ticket: VP-NAP-202110202 / VC-NAP-202110203(Change in notification)
    public String getLimittype() { return limittype; }

    public void setLimittype(String limittype) { this.limittype = limittype; }
    //======================================================================================================

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
    @Column(name = "RECEIPTCHARGES")
    private String receiptcharges;

    @Column(name = "BI_CHARGES")
    private String balanceinquirycharges;

    @Transient
    private Boolean isonustranx;

    @Transient
    private String receiptchargestax;

    @Transient
    private String balinqchargestax;

    @Transient
    private String onuschargestax;

    @Transient
    private String nayapaychargestax;

    @Column(name = "ONUSTRANXCOUNTS")
    private String onustranxcounts;

    @Column(name = "ONUSBALINQCOUNTS")
    private String onusbalinqcounts;

    @Column(name = "ONUSRECEIPTCOUNTS")
    private String onusreceiptcounts;

    public String getReceiptcharges() {
        return receiptcharges;
    }

    public void setReceiptcharges(String receiptcharges) {
        this.receiptcharges = receiptcharges;
    }

    public String getBalanceinquirycharges() {
        return balanceinquirycharges;
    }

    public void setBalanceinquirycharges(String balanceinquirycharges) {
        this.balanceinquirycharges = balanceinquirycharges;
    }

    public Boolean getIsonustranx() {
        return isonustranx;
    }

    public void setIsonustranx(Boolean isonustranx) {
        this.isonustranx = isonustranx;
    }

    public String getReceiptchargestax() {
        return receiptchargestax;
    }

    public void setReceiptchargestax(String receiptchargestax) {
        this.receiptchargestax = receiptchargestax;
    }

    public String getBalinqchargestax() {
        return balinqchargestax;
    }

    public void setBalinqchargestax(String balinqchargestax) {
        this.balinqchargestax = balinqchargestax;
    }

    public String getOnuschargestax() {
        return onuschargestax;
    }

    public void setOnuschargestax(String onuschargestax) {
        this.onuschargestax = onuschargestax;
    }

    public String getNayapaychargestax() {
        return nayapaychargestax;
    }

    public void setNayapaychargestax(String nayapaychargestax) {
        this.nayapaychargestax = nayapaychargestax;
    }

    public String getOnustranxcounts() {
        return onustranxcounts;
    }

    public void setOnustranxcounts(String onustranxcounts) {
        this.onustranxcounts = onustranxcounts;
    }

    public String getOnusbalinqcounts() {
        return onusbalinqcounts;
    }

    public void setOnusbalinqcounts(String onusbalinqcounts) {
        this.onusbalinqcounts = onusbalinqcounts;
    }

    public String getOnusreceiptcounts() {
        return onusreceiptcounts;
    }

    public void setOnusreceiptcounts(String onusreceiptcounts) {
        this.onusreceiptcounts = onusreceiptcounts;
    }
    //===========================================================================================================

    public <T extends Object, Y extends Object> void copyFields(T from, Y too) {

        Class<? extends Object> fromClass = from.getClass();
        Field[] fromFields = fromClass.getDeclaredFields();

        Class<? extends Object> tooClass = too.getClass();
        Field[] tooFields = tooClass.getDeclaredFields();

        if (fromFields != null && tooFields != null) {
            for (Field tooF : tooFields) {
                logger.debug("field name " + tooF.getName() + " and type " + tooF.getType().toString());
                try {
                    // Check if that fields exists in the other method
                    Field fromF = fromClass.getDeclaredField(tooF.getName());
                    if (fromF.getType().equals(tooF.getType())) {
                        tooF.set(tooF, fromF);
                    }
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    logger.error(WebServiceUtil.getStrException(e));
                } catch (NoSuchFieldException e) {
                    // TODO Auto-generated catch block
                    logger.error(WebServiceUtil.getStrException(e));
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    logger.error(WebServiceUtil.getStrException(e));
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    logger.error(WebServiceUtil.getStrException(e));
                }
            }
        }
    }
}
