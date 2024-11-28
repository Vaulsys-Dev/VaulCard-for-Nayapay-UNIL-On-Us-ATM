package vaulsys.protocols.ifx.imp;

import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.cms.base.CMSCardAuthorizationFlags;
import vaulsys.cms.base.CMSCardRelation;
import vaulsys.cms.base.CMSCardLimit;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Organization;
import vaulsys.lottery.Lottery;
import vaulsys.lottery.consts.LotteryState;
import vaulsys.migration.MigrationData;
import vaulsys.mtn.MTNCharge;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.KeyManagementMode;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.base.config.TransactionStatusType;
import vaulsys.protocols.ndc.constants.LastStatusIssued;
import vaulsys.protocols.ndc.constants.ReceiptOptionType;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wallet.base.WalletCardRelation;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.webservices.ghasedak.GhasedakItemType;
import vaulsys.webservices.ghasedak.GhasedakRsItem;
//import vaulsys.webservices.mcivirtualvosoli.common.MCIVosoliState;
import vaulsys.wfe.GlobalContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class Ifx implements IEntity<Long>, Cloneable {
    
	@Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="ifx-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "ifx-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "ifx_seq")
    				})
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trx", nullable = true, updatable = true)
    @ForeignKey(name="ifx_trx_fk")
    private Transaction transaction;
    
    @Column(name = "trx", insertable = false, updatable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal")
    @ForeignKey(name="ifx_term_fk")
    private Terminal endPointTerminal;
    
    @Column(name = "terminal",insertable = false, updatable = false)
    private Long endPointTerminalCode;

    @Column(name="thrd_termid")
	private Long ThirdPartyTerminalCode;
    
    @Transient
    private transient Terminal originatorTerminal;
    
    @Transient
    private transient String actionCode;

	private Boolean request;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "direction"))
    })
    private IfxDirection ifxDirection;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "ifx_type"))
    })
    private IfxType ifxType; // Note e.g: BalInqRq; BalInqRs

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "trntype"))
    })
    private TrnType trnType = TrnType.UNKNOWN; // Note e.g: Purchase; Cash Withdraw

//    @Transient
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "recieved_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "recieved_time"))
    })
    private DateTime receivedDt;
    
    @Column(name="received_dt")
    private Long receivedDtLong;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "date", column = @Column(name = "post_date")))
    private MonthDayDate postedDt;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "date", column = @Column(name = "settle_date")))
    private MonthDayDate settleDt; //P15

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emvrqdata")
	@Cascade(value = CascadeType.ALL )
	@ForeignKey(name="ifx_emvrqdata_fk")
	private EMVRqData eMVRqData;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emvrsdata")
	@Cascade(value = CascadeType.ALL )
	@ForeignKey(name="ifx_emvrsdata_fk")
	private EMVRsData eMVRsData;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nettrninfo")
	@Cascade(value = CascadeType.ALL )
	@ForeignKey(name="ifx_nettrninfo_fk")
    @Index(name="idx_ifx_nettrninfo")
	private NetworkTrnInfo networkTrnInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="msgrefdata", nullable = true)
    @Cascade(value = CascadeType.ALL )
	@ForeignKey(name="ifx_msgrefdata_fk")
    private MessageReferenceData originalDataElements; // S90

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="atmdata", nullable = true)
    @Cascade(value = CascadeType.ALL )
	@ForeignKey(name="ifx_atmdata_fk")
    private ATMSpecificData atmSpecificData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="posdata", nullable = true)
    @Cascade(value = CascadeType.ALL )
    @ForeignKey(name="ifx_posdata_fk")
    private POSSpecificData posSpecificData;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="saderatdata", nullable = true)
//    @Cascade(value = CascadeType.ALL )
//    @ForeignKey(name="ifx_saderatdata_fk")
//    private SaderatSpecificData saderatSpecificData;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="charge", nullable = true)
	@Cascade(value = { CascadeType.ALL })
	@ForeignKey(name="ifx_cellchargedata_fk")
	private CellChargingData chargeData;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="topup", nullable = true)
	@Cascade(value = { CascadeType.ALL })
	@ForeignKey(name="ifx_topupdata_fk")
	private TopupData topupData;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ghasedak", nullable = true)
	@Cascade(value = { CascadeType.ALL })
	@ForeignKey(name="ifx_ghasedakdata_fk")
	private GhasedakData ghasedakData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="lottery", nullable = true)
    @Cascade(value = { CascadeType.ALL })
    @ForeignKey(name="ifx_lotterydata_fk")
    private LotteryData lotteryData;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="migdata", nullable = true)
    @Cascade(value = { CascadeType.ALL })
    @ForeignKey(name="ifx_migrationdata_fk")
    private MigrationData migrationData;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="migSecdata", nullable = true)
    @Cascade(value = { CascadeType.ALL })
    @ForeignKey(name="ifx_migrationseconddata_fk")
    private MigrationData migrationSecondData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="keymng", nullable = true)
    @Cascade(value = { CascadeType.ALL })
    @ForeignKey(name="ifx_keymng_fk")
    private KeyManagement keyManagement;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="recdata", nullable = true)
    @Cascade(value = { CascadeType.ALL })
    @ForeignKey(name="ifx_recdata_fk")
    private ReconciliationData reconciliationData;
    
//    @Embedded
//    @AttributeOverrides({
//    	@AttributeOverride(name = "StatusCode", column = @Column(name = "status_code")),
//    	@AttributeOverride(name = "StatusDesc", column = @Column(name = "status_desc", length=2000))
//    })
    @OneToMany(fetch = FetchType.LAZY, mappedBy="ifx")
    @Cascade(value = {CascadeType.ALL})
    private List<Status> status;
 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="thirdpartydata", nullable = true)
    @Cascade(value = { CascadeType.ALL })
    @ForeignKey(name="ifx_thirdpartydata_fk")
    private ThirdPartyData thirdPartyData;
    
    public ThirdPartyData getThirdPartyData() {
		return thirdPartyData;
	}

	public void setThirdPartyData(ThirdPartyData thirdPartyData) {
		this.thirdPartyData = thirdPartyData;
	}
	
	public ThirdPartyData getSafeThirdPartyData() {
		if (thirdPartyData == null)
			thirdPartyData = new ThirdPartyData();
		return thirdPartyData;
	}
    
    private String mti;
    
    @Transient
    private transient Long firstTrxId;
        
	@OneToMany(mappedBy="clearingIfx")
	@Cascade(value = {CascadeType.ALL})
	List<ProcessdTransaction> clearingTransactions;
	
	@OneToMany(mappedBy="clearingIfx")
	@Cascade(value = {CascadeType.ALL})
	List<ClearingIfxReconcilementData> clearingData;
//	= new ArrayList<ClearingIfxReconcilementData>();
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="uidata", nullable = true)
    @Cascade(value = CascadeType.ALL )
	@ForeignKey(name="ifx_uidata_fk")
	private UiSpecificData uiSpecificData;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="onlinebillpaymentdata", nullable = true)
    @Cascade(value = CascadeType.ALL )
    @ForeignKey(name="ifx_onlinebillpaymentdata_fk")
    private OnlineBillPaymentData onlineBillPaymentData;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="mizanspecificdata")
//    @Cascade(value = CascadeType.ALL )
//    @ForeignKey(name="ifx_mizanspecificdata_fk")
//    private MizanSpecificData mizanSpecificData;
	
    @Column(name="dummycol")
	private Byte dummycol;
    
    @Transient
    private byte[] transientCardHolderName;
    
    @Transient
    private byte[] transientCardHolderFamily;
    
    @Transient
    private transient String transferFromDesc;

    @Transient
    private transient String transferToDesc;
    
//    gholami(Task45875)
    @Transient
    private transient String shenaseOfTransferToAccount;
    
   //gholami(Task112483)
    @Transient
    private transient int statementRowNumber;


	/*************new ifx fields from other entity****************/

    @Column(name="ifx_src_trn_seq_cntr")
    private String ifxSrcTrnSeqCntr;

    @Column(name="ifx_bankId")
    private String ifxBankId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "ifx_orig_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "ifx_orig_time"))
    })
    private DateTime ifxOrigDt;

    @Column(name="ifx_enc_appPAN")
    private String ifxEncAppPAN;

    @Column(name="ifx_rsCode")
    private String ifxRsCode;

    @Transient
    private String ifxPlnAppPAN;

    //Field 12 in ISO
    @Column(name = "time_loc_tran")
    private String timeLocalTran;

    //Field 13 in ISO
    @Column(name = "date_loc_tran")
    private String dateLocalTran;

    //Field 16 in ISO
    //@Column(name = "date_conv") //Raza just using to save value if received from TPSP
    //private String dateConversion;

    //Field 18 in ISO
    @Column(name = "merch_type")
    private String merchantType;

    //Field 19 in ISO
    @Column(name = "merch_ctry_code")
    private String merchCountryCode;

    //Field 20 in ISO
    @Column(name = "pan_ctry_code")
    private String panCountryCode;

    //Field 22 in ISO
    @Column(name = "pos_ent_mode_code")
    private String posEntryModeCode;

    //Field 23 in ISO
    @Column(name = "card_seq_no")
    private String cardSequenceNo;

    //Field 24 in ISO
    @Column(name = "network_inst_id")
    private String networkInstId;

    //Field 25 in ISO
    @Column(name = "pos_cond_code")
    private String posConditionCode;

    //Field 26 in ISO
    @Column(name = "pos_pin_cap_code")
    private String posPinCaptureCode;
    //Field 28 in ISO
    @Column(name = "amt_tran_fee")
    private String amountTranFee;

    //Field 43 in ISO
    @Column(name = "card_accept_name_loc")
    private String cardAcceptNameLoc;

    //Field 44 in ISO
    @Column(name = "add_resp_data")
    private String addResponseData;

    //Field 47 in ISO
    @Column(name = "add_data_national")
    private String addDataNational;

    //Field 48 in ISO
    @Column(name = "add_data_private")
    private String addDataPrivate;

    //Field 50 in ISO
    //@Column(name = "curr_code_sett")
    //private String currCodeSettlement;

    //Field 58 in ISO
    @Column(name = "authagent_inst_id") //Raza MASTERCARD
    private String authAgentInstId;

    //Field 60 in ISO
    @Column(name = "self_define_data")
    private String selfDefineData;

    //Field 61 in ISO
    @Column(name = "other_amounts")
    private String otheramounts;

    //Field 62 in ISO
    @Column(name = "custom_payment_service")
    private String customPaymentService;

    //Field 63 in ISO
    @Column(name = "network_data")
    private String NetworkData;

    //Field 70 in ISO
    @Column(name = "net_info_code")
    private String networkManageInfoCode;

    //Field 96 in ISO
    @Column(name = "mesg_sec_code")
    private String mesgSecurityCode;

    //Field 102
    @Column(name = "account_id_1")
    private String accountId1;

    //Field 103
    @Column(name = "account_id_2")
    private String accountId2;

    //Field 120
    @Column(name = "record_data")
    private String recordData;

    @Transient
    private String InstitutionId; //Raza in order to use Channel Institution

    /* Added by : Asim Shahzad, Date 9th Nov 2016, Desc : Adding new ISO field <start> */
    //Field 53 in ISO
    @Column(name = "sec_relat_cont_info")
    private String secRelatedControlInfo;

    /*
    //Field 62 in ISO
    @Transient
    private String Bitmap_62;

    //Field 63 in ISO
    @Transient
    private String Bitmap_63;
    */

    //Field 126 in ISO
    @Column(name = "scheme_private_use")
    private String schemePrivateUse;

    //Raza Adding CMSCardRelation object for PIN offset and product related work
    @Transient
    public CMSCardRelation cmsCardRelation;
    /* Adding new ISO field <end> */

    //m.rehman: for wallet operations
    @Transient
    public WalletCardRelation walletCardRelation;

    public String getIfxSrcTrnSeqCntr() {
        return ifxSrcTrnSeqCntr;
    }

    public void setIfxSrcTrnSeqCntr(String ifxSrcTrnSeqCntr) {
        this.ifxSrcTrnSeqCntr = ifxSrcTrnSeqCntr;
    }

    public String getIfxBankId() {
        return ifxBankId;
    }

    public void setIfxBankId(String ifxBankId) {
        this.ifxBankId = ifxBankId;
    }

    public DateTime getIfxOrigDt() {
        return ifxOrigDt;
    }

    public void setIfxOrigDt(DateTime ifxOrigDt) {
        this.ifxOrigDt = ifxOrigDt;
    }

    public String getIfxEncAppPAN() {
        return ifxEncAppPAN;
    }

    public void setIfxEncAppPAN(String ifxEncAppPAN) {
        this.ifxEncAppPAN = ifxEncAppPAN;
    }

    public String getIfxPlnAppPAN() {
        return ifxPlnAppPAN;
    }

    public void setIfxPlnAppPAN(String ifxPlnAppPAN) {
        this.ifxPlnAppPAN = ifxPlnAppPAN;
//        ifxEncAppPAN =
    }

    public String getIfxRsCode() {
        return ifxRsCode;
    }

    public void setIfxRsCode(String ifxRsCode) {
        this.ifxRsCode = ifxRsCode;
    }

    /*************new ifx fields from other entity****************/

	public String getTransferFromDesc() {
		return transferFromDesc;
	}

	public void setTransferFromDesc(String transferFromDesc) {
		this.transferFromDesc = transferFromDesc;
	}

	public String getTransferToDesc() {
		return transferToDesc;
	}

	public void setTransferToDesc(String transferToDesc) {
		this.transferToDesc = transferToDesc;
	}
	
	public String getShenaseOfTransferToAccount() {
		return shenaseOfTransferToAccount;
	}

	public void setShenaseOfTransferToAccount(String shenaseOfTransferToAccount) {
		this.shenaseOfTransferToAccount = shenaseOfTransferToAccount;
	}
	
    public int getStatementRowNumber() {
		return statementRowNumber;
	}

	public void setStatementRowNumber(int statementRowNumber) {
		this.statementRowNumber = statementRowNumber;
	}

	public byte[] getTransientCardHolderName() {
		return transientCardHolderName;
	}

	public void setTransientCardHolderName(byte[] transientCardHolderName) {
		this.transientCardHolderName = transientCardHolderName;
	}

	public byte[] getTransientCardHolderFamily() {
		return transientCardHolderFamily;
	}

	public void setTransientCardHolderFamily(byte[] transientCardHolderFamily) {
		this.transientCardHolderFamily = transientCardHolderFamily;
	}

	public Ifx() {
    	this.dummycol = (byte) ((int)(Math.random() * 1000) % 10);
	}

    public Transaction getTransaction() {
		return this.transaction;
	}

	public OnlineBillPaymentData getOnlineBillPaymentData() {
		return onlineBillPaymentData;
	}

	public void setOnliBillPaymentData(OnlineBillPaymentData onliBillPaymentData) {
		this.onlineBillPaymentData = onliBillPaymentData;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	    
	public List<ProcessdTransaction> getClearingTransactions() {
		return clearingTransactions;
	}

	public void setClearingTransactions(List<ProcessdTransaction> transactions) {
		this.clearingTransactions = transactions;
	}

	
	public List<ClearingIfxReconcilementData> getClearingData() {
		return clearingData;
	}

	public void setClearingData(List<ClearingIfxReconcilementData> clearingData) {
		this.clearingData = clearingData;
	}
	
	
	public void addClearingTransaction(Transaction t, ClearingInfo former, ClearingInfo latter, SourceDestination srcDest){
		if (t != null){
			ProcessdTransaction pt = new ProcessdTransaction(t, former, latter, srcDest);
			pt.setClearingIfx(this);
			if( clearingTransactions == null )
				clearingTransactions = new ArrayList<ProcessdTransaction>();
			clearingTransactions.add(pt);
		}
	}
	
	public void addClearingTransaction(Transaction t, ClearingInfo former, SourceDestination srcDest){
		if (t!= null){
			ProcessdTransaction pt = new ProcessdTransaction(t, former, t.getSourceClearingInfo(), srcDest);
			pt.setClearingIfx(this);
			if( clearingTransactions == null )
				clearingTransactions = new ArrayList<ProcessdTransaction>();
			clearingTransactions.add(pt);
		}
	}
	
	public void removeClearingTransaction(Transaction t){
		boolean flag = false;
		int index =0;
		if(clearingTransactions == null)
			return;
		
		while(!flag && index < clearingTransactions.size()){
			if (clearingTransactions.get(index).getTransaction().getId().equals(t.getId())){
				clearingTransactions.remove(clearingTransactions);
				flag = true;
			}
			index++;
		}
	}
    
    public boolean isResponse() {
    	if (ifxType == null)
    		return false;
        return ifxType.toString().endsWith("RS");
    }

    @SuppressWarnings("unchecked")
    public void set(String address, Object value) {
        String[] addrs = address.split("\\.");

        Object currObj = this;
        try {
            for (int i = 1; i < addrs.length - 1; i++) {
                Class currClass = currObj.getClass();
                String fldName = addrs[i];
                Field fld = currClass.getField(fldName);
                Object obj = fld.get(currObj);
                if (obj == null) {
                    obj = fld.getType().newInstance();
                    fld.set(currObj, obj);
                }
                currObj = obj;
            }
            Field fld = currObj.getClass().getField(addrs[addrs.length - 1]);
            fld.set(currObj, value);

        } catch (Exception ex) {
//            getLogger().error("Error in Creating Ifx _Object", ex);
        }
    }

    @Transient
    @SuppressWarnings("unchecked")
    public Object get(String address) {
        String[] addrs = address.split("\\.");

        Object currObj = this;
        try {
    		String fldName = addrs[addrs.length-1];
			Method method = currObj.getClass().getMethod("get"+fldName.substring(0,1).toUpperCase()+fldName.substring(1));
			Object obj = method.invoke(currObj,(Object[])null);
			return obj;
		} catch (Exception e) {
			try {
				for (int i = 1; i < addrs.length; i++) {
					Class currClass = currObj.getClass();
					String fldName = addrs[i];

					Method method = currClass.getMethod("get"+fldName.substring(0,1).toUpperCase()+fldName.substring(1));
					Object obj = method.invoke(currObj,(Object[])null);
//					Field fld = currClass.getField(fldName);
//					Object obj = fld.get(currObj);
					if (obj == null) {
						return null;
					}
					currObj = obj;
				}
				return currObj;

			} catch (Exception ex) {
//				getLogger().error("Error in Creating Ifx _Object", ex);
				return null;
			}
		}
    }

    public Long getId() {
        return id;
    }

	public EMVRqData getSafeEMVRqData() {
		if (eMVRqData == null)
			eMVRqData = new EMVRqData();
		return eMVRqData;
	}
	
	public EMVRqData getEMVRqData() {
		return eMVRqData;
	}

	public void setEMVRqData(EMVRqData rqData) {
		eMVRqData = rqData;
        if(rqData != null){
            if (eMVRqData.getCardAcctId() != null){
                setIfxPlnAppPAN(rqData.getCardAcctId().getAppPAN());
                setIfxEncAppPAN(rqData.getCardAcctId().getAppPAN());
            }
        }

    }

	public NetworkTrnInfo getSafeNetworkTrnInfo() {
		if (networkTrnInfo == null)
			networkTrnInfo = new NetworkTrnInfo();
		return networkTrnInfo;
	}
	
	public NetworkTrnInfo getNetworkTrnInfo() {
		return networkTrnInfo;
	}

	public void setNetworkTrnInfo(NetworkTrnInfo networkTrnInfo) {
		this.networkTrnInfo = networkTrnInfo;
        if(networkTrnInfo != null){
            if(networkTrnInfo.getOrigDt() != null)
                setIfxOrigDt(networkTrnInfo.getOrigDt());
            if(networkTrnInfo.getSrc_TrnSeqCntr() != null)
                setIfxSrcTrnSeqCntr(networkTrnInfo.getSrc_TrnSeqCntr());
            if(networkTrnInfo.getBankId() != null)
                setIfxBankId(networkTrnInfo.getBankId());
        }
    }

	public EMVRsData getSafeEMVRsData() {
		if (eMVRsData == null)
			eMVRsData = new EMVRsData();
		return eMVRsData;
	}
	
	public EMVRsData getEMVRsData() {
		return eMVRsData;
	}

	public void setEMVRsData(EMVRsData rsData) {
		eMVRsData = rsData;
        if(eMVRsData != null)setIfxRsCode(eMVRsData.getRsCode());
        else setIfxRsCode(null);
	}

	public CellChargingData getChargeData() {
		return chargeData;
	}

	public void setChargeData(CellChargingData charge) {
		this.chargeData = charge;
	}
	
	public CellChargingData getSafeChargeData() {
		if (chargeData == null)
			chargeData = new CellChargingData();
		return chargeData;
	}
	
	public Long getFirstTrxId() {
		return firstTrxId;
	}

	public void setFirstTrxId(Long firstTrxId) {
		this.firstTrxId = firstTrxId;
	}

	public TopupData getTopupData() {
		return topupData;
	}

	public void setTopupData(TopupData topup) {
		this.topupData = topup;
	}

	public TopupData getSafeTopupData() {
		if (topupData == null)
			topupData = new TopupData();
		return topupData;
	}
	
	//ghasedak
	public GhasedakData getGhasedakData() {
		return ghasedakData;
	}

	public void setGhasedakData(GhasedakData ghasedak) {
		this.ghasedakData = ghasedak;
	}

	public GhasedakData getSafeGhasedakData() {
		if (ghasedakData == null)
			ghasedakData = new GhasedakData();
		return ghasedakData;
	}
	
	public OnlineBillPaymentData getSafeOnlineBillPaymentData(){
		if(onlineBillPaymentData == null)
			onlineBillPaymentData = new OnlineBillPaymentData();
		return onlineBillPaymentData;
	}

	public LotteryData getLotteryData() {
		return lotteryData;
	}
	
	public void setLotteryData(LotteryData lottery) {
		this.lotteryData = lottery;
	}
	
	public LotteryData getSafeLotteryData() {
		if (lotteryData == null)
			lotteryData = new LotteryData();
		return lotteryData;
	}
	
	public ReconciliationData getSafeReconciliationData(){
		if (reconciliationData == null)
			reconciliationData = new ReconciliationData();
		return reconciliationData;
	}
	
	
	public String getCheckDigit() {
		if (keyManagement == null)
			return null;
		return getKeyManagement().getCheckDigit();
	}

	public void setCheckDigit(String checkDigit) {
		if (Util.hasText(checkDigit))
			getSafeKeyManagement().setCheckDigit(checkDigit);
	}

	
	public void setNetworkManagementInformationCode(NetworkManagementInfo networkManagementInformationCode) {
		if (networkManagementInformationCode != null)
			getSafeKeyManagement().setNetworkManagementInformationCode( networkManagementInformationCode);
	}

	public NetworkManagementInfo getNetworkManagementInformationCode() {
		if (keyManagement == null)
			return null;
		return keyManagement.getNetworkManagementInformationCode();
	}
	
	
	public String getKeyType() {
		if (keyManagement == null)
			return null;
		return getKeyManagement().getKeyType();
	}

	public void setKeyType(String keyType) {
		if (Util.hasText(keyType))
			getSafeKeyManagement().setKeyType(keyType);
	}

	public KeyManagementMode getMode() {
		if (keyManagement == null)
			return null;
		return getKeyManagement().getMode();
	}

	public void setMode(KeyManagementMode mode) {
		if (mode != null)
			getSafeKeyManagement().setMode(mode);
	}

	public String getKey() {
		if (keyManagement == null)
			return null;
		return getKeyManagement().getKey();
	}

	public void setKey(String key) {
		if (Util.hasText(key))
			getSafeKeyManagement().setKey(key);
	}

	public String getDigits() {
		if (keyManagement == null)
			return null;
		return getKeyManagement().getDigits();
	}
	
	public void setDigits(String digits) {
		if (Util.hasText(digits))
			getSafeKeyManagement().setDigits(digits);
	}
	
	public KeyManagement getKeyManagement() {
		return keyManagement;
	}
	
	public void setKeyManagement(KeyManagement keyManagement) {
		this.keyManagement = keyManagement;
	}
	
	public KeyManagement getSafeKeyManagement() {
		if (keyManagement == null)
			keyManagement = new KeyManagement();
		return keyManagement;
	}
	
	public ATMSpecificData getSafeAtmSpecificData() {
		if (this.atmSpecificData == null)
			this.atmSpecificData = new ATMSpecificData();
		return this.atmSpecificData;
	}
	
	public ATMSpecificData getAtmSpecificData() {
		return this.atmSpecificData;
	}

	public void setAtmSpecificData(ATMSpecificData atmSpecificData) {
		this.atmSpecificData = atmSpecificData;
	}
	 
	public POSSpecificData getSafePosSpecificData() {
		if (this.posSpecificData == null)
			this.posSpecificData = new POSSpecificData();
		return this.posSpecificData;
	}
	
//	public SaderatSpecificData getSafeSaderatSpecificData() {
//		if (this.saderatSpecificData == null)
//			this.saderatSpecificData = new SaderatSpecificData();
//		return this.saderatSpecificData;
//	}
	
	public POSSpecificData getPosSpecificData() {
		return this.posSpecificData;
	}
	
	public void setPosSpecificData(POSSpecificData PosSpecificData) {
		this.posSpecificData = PosSpecificData;
	}
	
//	public SaderatSpecificData getSaderatSpecificData() {
//		return this.saderatSpecificData;
//	}
	
//	public void setSaderatSpecificData(SaderatSpecificData saderatSpecificData) {
//		this.saderatSpecificData = saderatSpecificData;
//	}
	
	public String getApplicationVersion() {
		if (posSpecificData == null)
			return null;
		return posSpecificData.getApplicationVersion();
	}

	public void setApplicationVersion(String applicationVersion) {
		if (applicationVersion!= null && applicationVersion.length()>0)
			getSafePosSpecificData().setApplicationVersion(applicationVersion);
	}

	public String getSerialno() {
		if (posSpecificData == null)
			return null;
		return posSpecificData.getSerialno();
	}

	public void setSerialno(String serialno) {
		if (serialno !=null)
			getSafePosSpecificData().setSerialno(serialno);
	}
	
	public String getResetingPassword() {
		if (posSpecificData == null)
			return null;
		return posSpecificData.getResetingPassword();
	}
	
	public void setResetingPassword(String resetingPassword) {
		if (resetingPassword !=null)
			getSafePosSpecificData().setResetingPassword(resetingPassword);
	}
	
	public Integer getDialIndicator(){
		if (posSpecificData == null)
			return null;
		return posSpecificData.getDialIndicator(); 
	}
	
	public void setDialIndicator(Integer dialIndicator){
		if (dialIndicator != null)
			getSafePosSpecificData().setDialIndicator(dialIndicator);
	}
	
	public String getANI(){
		if (posSpecificData == null)
			return null;
		return posSpecificData.getANI(); 
	}
	
	public void setANI(String ANI){
		if (Util.hasText(ANI))
			getSafePosSpecificData().setANI(ANI);
	}
	
	public String getDNIS(){
		if (posSpecificData == null)
			return null;
		return posSpecificData.getDNIS(); 
	}
	
	public void setDNIS(String DNIS){
		if (Util.hasText(DNIS))
			getSafePosSpecificData().setDNIS(DNIS);
	}
	
	public String getLRI(){
		if (posSpecificData == null)
			return null;
		return posSpecificData.getLRI(); 
	}
	
	public void setLRI(String LRI){
		if (Util.hasText(LRI))
			getSafePosSpecificData().setLRI(LRI);
	}
	
	public Long getMerchantUnsettledAmount(){
		if (posSpecificData == null)
			return null;
		return posSpecificData.getMerchantUnsettledAmount();
	}
	
	public void setMerchantUnsettledAmount(Long merchantUnsettledAmount){
		if (merchantUnsettledAmount != null)
			getSafePosSpecificData().setMerchantUnsettledAmount(merchantUnsettledAmount);
	}
	
	public AcctBal getSafeAcctBalAvailable() {
		return getSafeEMVRsData().getSafeAcctBalAvailable();
	}
	public AcctBal getSafeTransientAcctBalAvailable(){
		return getSafeEMVRsData().getSafeTransientAcctBalAvailable();
	}
	
	
	public void setTransientAcctBalAvailableAmt(String amt) {
		if (Util.hasText(amt))
			getSafeTransientAcctBalAvailable().setAmt(amt);
	}
	 
	public void setTransientAcctBalAvailableBalType(BalType balType) {
		if (balType != null)
			getSafeTransientAcctBalAvailable().setBalType(balType);
	}

	public void setTransientAcctBalAvailableCurCode(String curCode) {
		if (Util.hasText(curCode))
			getSafeTransientAcctBalAvailable().setCurCode(curCode);
	}
	 
	public void setTransientAcctBalAvailableType(AccType acctType) {
		if (acctType != null)
			getSafeTransientAcctBalAvailable().setAcctType(acctType);
	}
	
	
	

	public AcctBal getAcctBalAvailable() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getAcctBalAvailable();
	}
	
	public AcctBal getTransientAcctBalAvailable() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getTransientAcctBalAvailable();
	}
	
	public AcctBal getSafeAcctBalLedger() {
		return getSafeEMVRsData().getSafeAcctBalLedger();
	}
	
	public AcctBal getSafeTransientAcctBalLedger(){
		return getSafeEMVRsData().getSafeTransientAcctBalLedger();
	}
		
	public void setTransientAcctBalLedgerAmt(String amt) {
		if (Util.hasText(amt))
			getSafeTransientAcctBalLedger().setAmt(amt);
	}
	 
	public void setTransientAcctBalLedgerBalType(BalType balType) {
		if (balType != null)
			getSafeTransientAcctBalLedger().setBalType(balType);
	}
	 
	public void setTransientAcctBalLedgerCurCode(String curCode) {
		if (Util.hasText(curCode))
			getSafeTransientAcctBalLedger().setCurCode(curCode);
	}
	 
	public void setTransientAcctBalLedgerType(AccType acctType) {
		if (acctType != null)
			getSafeTransientAcctBalLedger().setAcctType(acctType);
	}
	
	
	
	
	
	public CreditCardData getSafeCreditCardData() {
		return getSafeEMVRsData().getSafeCreditCardData();
	}

	public AcctBal getAcctBalLedger() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getAcctBalLedger();
	}
	
	public CreditCardData getCreditCardData() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getCreditCardData();
	}
	
		
	public String getApprovalCode() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getApprovalCode();
	}

	 
	public CardAcctId getSafeCardAcctId() {
		return getSafeEMVRqData().getSafeCardAcctId();
	}
	
	public CardAcctId getCardAcctId() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getCardAcctId();
	}

	public IfxDirection getIfxDirection() {
		return this.ifxDirection;
	}
	 
	public IfxType getIfxType() {
		return this.ifxType;
	}

	public String getNew_AmtAcqCur() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getNew_AmtAcqCur();
	}
	
	public String getNew_AmtIssCur() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getNew_AmtIssCur();
	}
	
	public OrgRec getSafeOrgRec() {
		return getSafeNetworkTrnInfo().getSafeOrgRec();
	}
	 
	public OrgRec getOrgRec() {
		if(getNetworkTrnInfo() == null)
			return null;
		return getNetworkTrnInfo().getOrgRec();
	}
	
	public DateTime getOrigDt() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getOrigDt();
	}
	
	public MessageReferenceData getSafeOriginalDataElements() {
		if (this.originalDataElements == null)
			this.originalDataElements = new MessageReferenceData();
		return this.originalDataElements;
	}
	
	public MessageReferenceData getOriginalDataElements() {
		return this.originalDataElements;
	}
	 
	public MonthDayDate getPostedDt() {
		return postedDt;
	}

	
	public void setPostedDt(MonthDayDate postedDt) {
		this.postedDt = postedDt;
	}

	
	public MonthDayDate getSettleDt() {
		return settleDt;
	}

	
	public void setSettleDt(MonthDayDate settleDt) {
		this.settleDt = settleDt;
	}
		 
	public DateTime getReceivedDt() {
//		if (this.receivedDt != null)
			return this.receivedDt;
//		if (receivedDtLong != null)
//			return new DateTime(receivedDtLong);
//		return null;
	}
	
	public String getRsCode() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getRsCode();
	}
	 
	public Long getTotalFeeAmt() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getTotalFeeAmt();
	}
	
	
	public String getDocumentNumber() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getDocumentNumber();

	}

	
	public Status getSafeStatus() {
		if (this.status == null){
			this.status = new ArrayList<Status>();
			this.status.add(new Status(this));
		}
		return this.status.get(0);
	}
	
	public Status getStatus() {
		if (this.status == null || this.status.size() == 0){
			return null;
		}
		return this.status.get(0);
	}

	 
	public TrnType getTrnType() {
		return this.trnType;
	}
	 
	public void setAcctBalAvailable(AcctBal acctBalAvailable) {
		if (acctBalAvailable != null)
			getSafeEMVRsData().setAcctBalAvailable(acctBalAvailable);
	}
	 
	public void setTransientAcctBalAvailable(AcctBal acctBalAvailable) {
		if (acctBalAvailable != null)
			getSafeEMVRsData().setTransientAcctBalAvailable(acctBalAvailable);
	}
	 
	public void setAcctBalLedger(AcctBal acctBalLedger) {
		if (acctBalLedger != null)
			getSafeEMVRsData().setAcctBalLedger(acctBalLedger);
	}
	 
	public void setTransientAcctBalLedger(AcctBal acctBalLedger){
		if (acctBalLedger != null )
			getSafeEMVRsData().setTransientAcctBalLedger(acctBalLedger);
	}
	 
	public void setCreditCardData(CreditCardData creditCardData) {
		if (creditCardData != null)
			getSafeEMVRsData().setCreditCardData(creditCardData);
	}
	
	public void setApprovalCode(String approvalCode) {
		if (Util.hasText(approvalCode))
			getSafeEMVRsData().setApprovalCode(approvalCode);
	}
	 
	public void setCardAccId(CardAcctId cardAcctId) {
		if (cardAcctId != null)
			getSafeEMVRqData().setCardAcctId(cardAcctId);
	}

	public void setIfxDirection(IfxDirection ifxDirection) {
		this.ifxDirection = ifxDirection;
	}
	 
	public void setIfxType(IfxType ifxType) {
		this.ifxType = ifxType;
	}
	 
	public void setNew_AmtAcqCur(String new_AmtAcqCur) {
		if (Util.hasText(new_AmtAcqCur))
			getSafeEMVRqData().setNew_AmtAcqCur ( new_AmtAcqCur);
	}

	public void setOrigDt(DateTime origDt) {
		if (!origDt.isNull())
			getSafeNetworkTrnInfo().setOrigDt(origDt);
        setIfxOrigDt(origDt);
	}
	 
	public void setOriginalDataElements(MessageReferenceData OriginalDataElements) {
		this.originalDataElements = OriginalDataElements;
	}
	 
	public void setRsCode(String rsCode) {
        if (Util.hasText(rsCode)){
            getSafeEMVRsData().setRsCode ( rsCode);
            setIfxRsCode(rsCode);
        }
    }
	 
	public void setTotalFeeAmt(Long totalFeeAmt) {
		if (totalFeeAmt != null)
			getSafeEMVRsData().setTotalFeeAmt(totalFeeAmt);
	}
	
	public void setDocumentNumber(String documentNumber) {
		if (Util.hasText(documentNumber))
			getSafeEMVRsData().setDocumentNumber(documentNumber);
	}
	
	public void setStatus(Status status) {
        if (this.status == null){
            this.status = new ArrayList<Status>();
            this.status.add(status);
        }else{
            this.status.set(0, status);
        }
	}
	 
	public void setTrnType(TrnType trnType) {
		this.trnType = trnType;
	}
	 
	public void setId(Long id) {
		this.id = id;
	}
	 
	public void setNew_AmtIssCur(String new_AmtIssCur) {
		if (Util.hasText(new_AmtIssCur))
			getSafeEMVRqData().setNew_AmtIssCur ( new_AmtIssCur);
	}
	 
	public void setReceivedDt(DateTime recievedDt) {
		this.receivedDt = recievedDt;
		if(recievedDt != null)
			this.receivedDtLong = recievedDt.getDateTimeLong();
	}
	 
	public AccType getAccTypeFrom() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getAccTypeFrom();
	}
	
	public AccType getAccTypeTo() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getAccTypeTo();
	}

	public String getAppPAN() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
		return getEMVRqData().getCardAcctId().getAppPAN();
	}
	 
	public String getActualAppPAN() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
		return getEMVRqData().getCardAcctId().getActualAppPAN();
	}
	
//	public String getAcctId() {
//		if (eMVRqData == null)
//			return null;
//		if (eMVRqData.getCardAcctId() == null)
//			return null;
//		return getEMVRqData().getCardAcctId().getAcctId();
//	}
	
	public Long getAuth_Amt() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getAuth_Amt();
	}
	
	public Long getReal_Amt() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getReal_Amt();
	}
	
	public Long getTrx_Amt() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getTrx_Amt();
	}

    public Long getSett_Amt() { //Raza MASTERCARD
        if (eMVRqData == null)
            return null;
        return getEMVRqData().getSett_Amt();
    }
	
	public void setNewPINBlock(String newPINBlock) {
		if (Util.hasText(newPINBlock))
			getSafeEMVRqData().setNewPINBlock(newPINBlock);
	}


	public String getNewPINBlock() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getNewPINBlock();
	}
	
	public void setOldPINBlock(String oldPINBlock) {
		if (Util.hasText(oldPINBlock))
			getSafeEMVRqData().setOldPINBlock(oldPINBlock);
	}

	public String getOldPINBlock() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getOldPINBlock();
	}
	
	public Integer getAuth_Currency() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getAuth_Currency();
	}

    public String getSett_Currency() {
        if (eMVRqData == null)
            return null;
        return getEMVRqData().getSett_Currency();
    }

	public String getAuth_CurRate() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getAuth_CurRate();
	}
	 
	public String getBankId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getBankId();
	}
	 
	public String getThirdPartyIds() {
		if (thirdPartyData == null)
			return null;
		return getThirdPartyData().getThirdPartyIds();
	}
	
//	public City getCity() {
//		if (getOrgRec() == null)
//			return null;
//		return getOrgRec().getCity();
//	}
//	 
	public Long getCityCode() {
		if (getOrgRec() == null)
			return null;
		return getOrgRec().getCityCode();
	}
	
	public char getCoordinationNumber() {
		if (atmSpecificData == null)
			return 0;
		return getAtmSpecificData().getCoordinationNumber();
	}
	 
	public String getCurrentDispense() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCurrentDispense();
	}

	public void setCurrentDispense(String currentDispense) {
		if (Util.hasText(currentDispense))
			getSafeAtmSpecificData().setCurrentDispense(currentDispense);
	}

	public Integer getTotalStep() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getTotalStep();
	}

	public void setTotalStep(Integer totalStep) {
		if (totalStep != null)
			getSafeAtmSpecificData().setTotalStep(totalStep);
	}

	public Integer getCurrentStep() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCurrentStep();
	}

	public void setCurrentStep(Integer currentStep) {
		if (currentStep != null)
			getSafeAtmSpecificData().setCurrentStep(currentStep);
	}

	public Integer getDesiredDispenseCaset1() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getDesiredDispenseCaset1();
	}

	public void setDesiredDispenseCaset1(Integer desiredDispenseCaset1) {
		if (desiredDispenseCaset1 != null)
			getSafeAtmSpecificData().setDesiredDispenseCaset1(desiredDispenseCaset1);
	}

	public Integer getDesiredDispenseCaset2() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getDesiredDispenseCaset2();
	}

	public void setDesiredDispenseCaset2(Integer desiredDispenseCaset2) {
		if (desiredDispenseCaset2 != null)
			getSafeAtmSpecificData().setDesiredDispenseCaset2(desiredDispenseCaset2);
	}

	public Integer getDesiredDispenseCaset3() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getDesiredDispenseCaset3();
	}

	public void setDesiredDispenseCaset3(Integer desiredDispenseCaset3) {
		if (desiredDispenseCaset3 != null)
			getSafeAtmSpecificData().setDesiredDispenseCaset3(desiredDispenseCaset3);
	}

	public Integer getDesiredDispenseCaset4() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getDesiredDispenseCaset4();
	}

	public void setDesiredDispenseCaset4(Integer desiredDispenseCaset4) {
		if (desiredDispenseCaset4 != null)
			getSafeAtmSpecificData().setDesiredDispenseCaset4(desiredDispenseCaset4);
	}

	public Integer getActualDispenseCaset1() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getActualDispenseCaset1();
	}

	public void setActualDispenseCaset1(Integer actualDispenseCaset1) {
		if (actualDispenseCaset1 != null)
			getSafeAtmSpecificData().setActualDispenseCaset1(actualDispenseCaset1);
	}

	public Integer getActualDispenseCaset2() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getActualDispenseCaset2();
	}

	public void setActualDispenseCaset2(Integer actualDispenseCaset2) {
		if (actualDispenseCaset2 != null)
			getSafeAtmSpecificData().setActualDispenseCaset2(actualDispenseCaset2);
	}

	public Integer getActualDispenseCaset3() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getActualDispenseCaset3();
	}

	public void setActualDispenseCaset3(Integer actualDispenseCaset3) {
		if (actualDispenseCaset3 != null)
			getSafeAtmSpecificData().setActualDispenseCaset3(actualDispenseCaset3);
	}

	public Integer getActualDispenseCaset4() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getActualDispenseCaset4();
	}

	public void setActualDispenseCaset4(Integer actualDispenseCaset4) {
		if (actualDispenseCaset4 != null)
			getSafeAtmSpecificData().setActualDispenseCaset4(actualDispenseCaset4);
	}
	
	public Integer getCurrentDispenseCaset1() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCurrentDispenseCaset1();
	}
	
	public void setCurrentDispenseCaset1(Integer currentDispenseCaset1) {
		if (currentDispenseCaset1 != null)
			getSafeAtmSpecificData().setCurrentDispenseCaset1(currentDispenseCaset1);
	}
	
	public Integer getCurrentDispenseCaset2() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCurrentDispenseCaset2();
	}
	
	public void setCurrentDispenseCaset2(Integer currentDispenseCaset2) {
		if (currentDispenseCaset2 != null)
			getSafeAtmSpecificData().setCurrentDispenseCaset2(currentDispenseCaset2);
	}
	
	public Integer getCurrentDispenseCaset3() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCurrentDispenseCaset3();
	}
	
	public void setCurrentDispenseCaset3(Integer currentDispenseCaset3) {
		if (currentDispenseCaset3 != null)
			getSafeAtmSpecificData().setCurrentDispenseCaset3(currentDispenseCaset3);
	}
	
	public Integer getCurrentDispenseCaset4() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCurrentDispenseCaset4();
	}
	
	public void setCurrentDispenseCaset4(Integer currentDispenseCaset4) {
		if (currentDispenseCaset4 != null)
			getSafeAtmSpecificData().setCurrentDispenseCaset4(currentDispenseCaset4);
	}
	
	public Long getActualDispenseAmt() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getActualDispenseAmt();
	}

	public void setActualDispenseAmt(Long actualDispenseAmt) {
		if (actualDispenseAmt != null)
			getSafeAtmSpecificData().setActualDispenseAmt(actualDispenseAmt);
	}

	public Long getCurrentDispenseAmt() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCurrentDispenseAmt();
	}

	public void setCurrentDispenseAmt(Long currentDispenseAmt) {
		if (currentDispenseAmt != null)
			getSafeAtmSpecificData().setCurrentDispenseAmt(currentDispenseAmt);
	}
	
	public TransactionStatusType getTransactionStatus() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getTransactionStatus();
	}

	public void setTransactionStatus(TransactionStatusType transactionStatus) {
		if (transactionStatus!=null)
			getSafeAtmSpecificData().setTransactionStatus(transactionStatus);
	}
	
	public char getLastTrxStatusId() {
		if (atmSpecificData == null)
			return 0;
		return getAtmSpecificData().getLastTrxStatusId();
	}
	
	public void setLastTrxStatusId(char lastTrxStatusId) {
		if (Util.hasText(lastTrxStatusId + "") && !Character.isIdentifierIgnorable(lastTrxStatusId))
			getSafeAtmSpecificData().setLastTrxStatusId(lastTrxStatusId);
	}
	
	public LastStatusIssued getLastTrxStatusIssue() {
		if (atmSpecificData == null)
			return LastStatusIssued.UNKNOWN;
		return getAtmSpecificData().getLastTrxStatusIssue();
	}
	
	public void setLastTrxStatusIssue(char lastTrxStatusIssue) {
		if (Util.hasText(lastTrxStatusIssue + "") && !Character.isIdentifierIgnorable(lastTrxStatusIssue))
			getSafeAtmSpecificData().setLastTrxStatusIssue(LastStatusIssued.get(lastTrxStatusIssue));
	}
	
	public void setLastTrxStatusIssue(LastStatusIssued lastTrxStatusIssue) {
		if (Util.hasText(lastTrxStatusIssue + "")/* && !Character.isIdentifierIgnorable(lastTrxStatusIssue)*/)
			getSafeAtmSpecificData().setLastTrxStatusIssue(lastTrxStatusIssue);
	}
	
	
	public String getLastTrxNotesDispensed() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getLastTrxNotesDispensed();
	}
	
	public void setLastTrxNotesDispensed(String lastTrxNotesDispensed) {
		if (Util.hasText(lastTrxNotesDispensed ))
			getSafeAtmSpecificData().setLastTrxNotesDispensed(lastTrxNotesDispensed);
	}
	
//	public Country getCountry() {
//		if (getOrgRec() == null)
//			return null;
//		return getOrgRec().getCountry();
//	}
//	
	public Long getCountryCode() {
		if (getOrgRec() == null)
			return null;
		return getOrgRec().getCountryCode();
	}
	
//	public String getAddress() {
//		if (getOrgRec() == null)
//			return null;
//		return getOrgRec().getAddress();
//	}
	
	public String getDestBankId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getDestBankId();
	}
	
	//public Long getFwdToBankId(){
//		if(networkTrnInfo == null)
		//	return null;
	//	return getNetworkTrnInfo().getFwdToBankId();
	//}
	 
	public Long getExpDt() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
		return getEMVRqData().getCardAcctId().getExpDt();
	}
	
	public String getFwdBankId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getFwdBankId();
	}

	public String getMsgAuthCode() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getMsgAuthCode();
	}

	public String getName() {
		if (getOrgRec() == null)
			return null;
		return getOrgRec().getName();
	}
	 
	public String getNetworkRefId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getNetworkRefId();
	}
	
	public String getMyNetworkRefId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getMyNetworkRefId();
	}
	
	public String getPINBlock() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
//		return getEMVRqData().getCardAcctId().getPINBlock();
		return getEMVRqData().getPINBlock();
	}
	 
	public String getCVV2() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
		return getEMVRqData().getCardAcctId().getCVV2();
	}
	 
	public String getSubsidiaryAccTo() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getSubsidiaryAccTo();
	}
	
	public String getSubsidiaryAccFrom() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getSubsidiaryAccFrom();
	}
	
	public String getRecvBankId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getRecvBankId();
	}
	 
	public Long getSec_Amt() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getSec_Amt();
	}

    public String getConvRate_Sett() { //Raza MASTERCARD
        if (eMVRqData == null)
            return null;
        return getEMVRqData().getConvRate_Sett();
    }
	 
	public Integer getSec_Currency() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getSec_Currency();
	}
	 
	public String getSec_CurRate() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getSec_CurRate();
	}

    public String getSec_CurDate() { //Raza MasterCard for DE-16
        if (eMVRqData == null)
            return null;
        return getEMVRqData().getSec_CurDate();
    }

	public Severity getSeverity() {
		if (status == null)
			return null;
		return getStatus().getSeverity();
	}

//	public State getStateProv() {
//		if (getOrgRec() == null)
//			return null;
//		return getOrgRec().getStateProv();
//	}
	 
	public Long getStateCode() {
		if (getOrgRec() == null)
			return null;
		return getOrgRec().getStateCode();
	}
	
//	public StatusCode getStatusCode() {
//		if (status == null)
//			return null;
//		return getStatus().getStatusCode();
//	}
	 
	public String getStatusDesc() {
		if (status == null)
			return null;
		return getStatus().getStatusDesc();
	}
	 
	public String getTerminalId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getTerminalId();
	}

	public Long getThirdPartyTerminalId() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getThirdPartyTerminalId();
	}

	public Long getThirdPartyCode() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getThirdPartyCode();
	}
	
	public String getThirdPartyName() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getThirdPartyName();
	}
	
	public String getThirdPartyNameEn() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getThirdPartyNameEn();
	}
	
	public TerminalType getTerminalType() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getTerminalType();
	}
	
	public TerminalType getOrigTerminalType() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getOrigTerminalType();
	}
	 
	public String getTimeVariantNumber() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getTimeVariantNumber();
	}

	public String getTrk2EquivData() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
		return getEMVRqData().getCardAcctId().getTrk2EquivData();
	}

	public DateTime getTrnDt() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getTrnDt();
	}
	 
	public String getSrc_TrnSeqCntr() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getSrc_TrnSeqCntr();
	}
	 
	public String getMy_TrnSeqCntr() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getMy_TrnSeqCntr();
	}
	 
	public String getLast_TrnSeqCntr() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getLast_TrnSeqCntr();
	}
	
	public void setAccTypeFrom(AccType accTypeFrom) {
		if (accTypeFrom != null)
			getSafeEMVRqData().setAccTypeFrom(accTypeFrom);
	}
	 
	public void setAccTypeTo(AccType AccTypeTo) {
		if (AccTypeTo != null)
			getSafeEMVRqData().setAccTypeTo(AccTypeTo);
	}
	 
	public void setAppPAN(String appPAN) {
		if (Util.hasText(appPAN)) {
			getSafeCardAcctId().setAppPAN(appPAN);
            setIfxPlnAppPAN(appPAN);
            setIfxEncAppPAN(appPAN);
		}
	}
	 
	public void setActualAppPAN(String actualAppPAN) {
		if (Util.hasText(actualAppPAN))
			getSafeCardAcctId().setActualAppPAN(actualAppPAN);
	}
	
//	public void setAcctId(String acctId) {
//		if (Util.hasText(acctId))
//			getSafeCardAcctId().setAcctId(acctId);
//	}
	
	public void setAuth_Amt(Long auth_Amt) {
		if (auth_Amt != null)
			getSafeEMVRqData().setAuth_Amt(auth_Amt);
	}

	public void setReal_Amt(Long real_Amt) {
		if (real_Amt != null)
			getSafeEMVRqData().setReal_Amt(real_Amt);
	}
	
	public void setTrx_Amt(Long trx_Amt) {
		if (trx_Amt != null)
			getSafeEMVRqData().setTrx_Amt(trx_Amt);
	}

    public void setSett_Amt(Long sett_Amt) { //Raza MasterCard
        if (sett_Amt != null)
            getSafeEMVRqData().setSett_Amt(sett_Amt);
    }
	
	public void setAuth_Currency(Integer auth_Code) {
		if (auth_Code != null)
			getSafeEMVRqData().setAuth_Currency(auth_Code);
	}

    public void setSett_Currency(String sett_Code) {
        if (sett_Code != null)
            getSafeEMVRqData().setSett_Currency(sett_Code);
    }
	 
	public void setAuth_CurRate(String auth_CurRate) {
		if (Util.hasText(auth_CurRate))
			getSafeEMVRqData().setAuth_CurRate(auth_CurRate);
	}

	public void setBankId(String bankId) {
		if (bankId != null)
			if(!bankId.equals(getBankId()))
				getSafeNetworkTrnInfo().setBankId(bankId);
        setIfxBankId(bankId);
	}

//	public void setCity(City city) {
//		if (city != null)
//			getSafeOrgRec().setCity(city);
//	}
	 
	public void setCityCode(Long cityCode) {
		if (cityCode != null)
			getSafeOrgRec().setCityCode(cityCode);
	}
	
	public void setCoordinationNumber(char coordinationNumber) {
		//TODO
		if (Util.hasText(coordinationNumber + "") && !Character.isIdentifierIgnorable(coordinationNumber))
			getSafeAtmSpecificData().setCoordinationNumber(coordinationNumber);
	}
	 
//	public void setCountry(Country country) {
//		if (country != null)
//			getSafeOrgRec().setCountry(country);
//	}
	 
	public void setCountryCode(Long countryCode) {
		if (countryCode != null)
			getSafeOrgRec().setCountryCode(countryCode);
	}
	
//	public void setAddress(String address) {
//		if (Util.hasText(address))
//			getSafeOrgRec().setAddress(address);
//	}
	
	public void setDestBankId(String destBankId) {
		if (destBankId != null)
			if(!destBankId.equals(getDestBankId()))
				getSafeNetworkTrnInfo().setDestBankId(destBankId);
	}
	
//	public void setFwdToBankId(Long fwdToBankId){
//		if(fwdToBankId != null)
//			if(!fwdToBankId.equals(getFwdToBankId()))
//				getSafeNetworkTrnInfo().setFwdToBankId(fwdToBankId);
//	}

	public void setExpDt(long expDt) {
		//TODO
		if (Util.hasText(Long.toString(expDt)))
//		if (expDt != null)
			getSafeCardAcctId().setExpDt(expDt);
	}
	 
	public void setFwdBankId(String fwdBankId) {
		if (fwdBankId != null)
			if(!fwdBankId.equals(getFwdBankId()))
				getSafeNetworkTrnInfo().setFwdBankId(fwdBankId);
		}
	 
	public void setMsgAuthCode(String msgAuthCode) {
		if (Util.hasText(msgAuthCode))
			getSafeEMVRqData().setMsgAuthCode(msgAuthCode);
	}
	 
	public void setName(String name) {
		if (Util.hasText(name))
			getSafeOrgRec().setName(name);
	}
	 
	public void setNetworkRefId(String networkRefId) {
//		if (networkRefId != null && !"".equals(networkRefId) && networkRefId.length() > 0)
		if (Util.hasText(networkRefId))
//		if (Util.hasText(networkRefId))
			getSafeNetworkTrnInfo().setNetworkRefId(networkRefId);
	}
	
	public void setMyNetworkRefId(String networkRefId) {
//		if (networkRefId != null && !"".equals(networkRefId) && networkRefId.length() > 0)
		if (Util.hasText(networkRefId))
//		if (Util.hasText(networkRefId))
			getSafeNetworkTrnInfo().setMyNetworkRefId(networkRefId);
	}

	public void setPINBlock(String block) {
		if (Util.hasText(block))
			getSafeEMVRqData().setPINBlock(block);
//			getSafeCardAcctId().setPINBlock(block);
			
	}

	public void setCVV2(String cvv2) {
		if (Util.hasText(cvv2))
		getSafeCardAcctId().setCVV2(cvv2);
	}
	 
	public void setSubsidiaryAccTo(String subsidiaryAccTo) {
		if (Util.hasText(subsidiaryAccTo))
			getSafeEMVRqData().setSubsidiaryAccTo(subsidiaryAccTo);
	}
	
	public void setSubsidiaryAccFrom(String subsidiaryAccFrom) {
		if (Util.hasText(subsidiaryAccFrom))
			getSafeEMVRqData().setSubsidiaryAccFrom(subsidiaryAccFrom);
	}
	
	public void setRecvBankId(String recvBankId) {
		if (recvBankId != null)
			if(!recvBankId.equals(getRecvBankId()))
				getSafeNetworkTrnInfo().setRecvBankId(recvBankId);
	}
	 
	public void setSec_Amt(Long sec_Amt) {
		if (sec_Amt != null)
			getSafeEMVRqData().setSec_Amt(sec_Amt);
	}

    public void setConvRate_Sett(String ConvRateSett) { //Raza MASTERCARD
        if (ConvRateSett != null)
            getSafeEMVRqData().setConvRate_Sett(ConvRateSett);
    }


	 
	public void setSec_Currency(Integer sec_CurCode) {
		if (sec_CurCode != null)
			getSafeEMVRqData().setSec_Currency(sec_CurCode);
	}

	public void setSec_CurRate(String sec_CurRate) {
		if (Util.hasText(sec_CurRate))
			getSafeEMVRqData().setSec_CurRate(sec_CurRate);
	}

    public void setSec_CurDate(String sec_CurDate) { //Raza MasterCard for DE-16
        if (Util.hasText(sec_CurDate))
            getSafeEMVRqData().setSec_CurDate(sec_CurDate);
    }
	 
	public void setSeverity(Severity severity) {
		if (severity != null)
			getSafeStatus().setSeverity(severity);
	}
	 
//	public void setStateProv(State stateProv) {
//		if (stateProv != null)
//			getSafeOrgRec().setStateProv(stateProv);
//	}
	 
	public void setStateCode(Long stateCode) {
		if (stateCode != null)
			getSafeOrgRec().setStateCode(stateCode);
	}
	
//	public void setStatusCode(StatusCode statusCode) {
//		if (statusCode != null)
//			getSafeStatus().setStatusCode(statusCode);
//	}
	
	public void setStatusDesc(String statusDesc) {
		if (Util.hasText(statusDesc))
			getSafeStatus().setStatusDesc(statusDesc);
	}
	 
	public void setTerminalId(String terminalId) {
//		if(terminalId != null && !"".equals(terminalId) && terminalId.length() > 0)
		if (Util.hasText(terminalId))
//		if (Util.hasText(terminalId))
			getSafeNetworkTrnInfo().setTerminalId(terminalId);
	}
	 
	public void setThirdPartyTerminalId(Long thirdPartyTermId) {
		if (thirdPartyTermId != null) {
			getSafeNetworkTrnInfo().setThirdPartyTerminalId(thirdPartyTermId);
			setThirdPartyTerminalCode(thirdPartyTermId);
		}
	}
	
	public void setThirdPartyIds(String thirdPartyIds) {
		if (Util.hasText(thirdPartyIds))
			getSafeThirdPartyData().setThirdPartyIds(thirdPartyIds);
	}

	public void setThirdPartyCode(Long thirdPartyCode) {
		if (thirdPartyCode != null) {
			getSafeNetworkTrnInfo().setThirdPartyCode(thirdPartyCode);
			getSafeThirdPartyData().setThirdPartyCode(thirdPartyCode);
		}
	}

	public void setThirdPartyName(String thirdPartyName) {
		if (Util.hasText(thirdPartyName))
			getSafeNetworkTrnInfo().setThirdPartyName(thirdPartyName);
	}

	public void setThirdPartyNameEn(String thirdPartyName) {
		if (Util.hasText(thirdPartyName))
			getSafeNetworkTrnInfo().setThirdPartyNameEn(thirdPartyName);
	}
	
	public void setTerminalType(TerminalType terminalType) {
		if (terminalType != null/* && !TerminalType.UNKNOWN.equals(terminalType)*/)
			getSafeNetworkTrnInfo().setTerminalType(terminalType);
	}
	
	public void setOrigTerminalType(TerminalType origTerminalType) {
		if (origTerminalType != null/* && !TerminalType.UNKNOWN.equals(origTerminalType)*/)
			getSafeNetworkTrnInfo().setOrigTerminalType(origTerminalType);
	}

	public void setTimeVariantNumber(String timeVariantNumber) {
		if (Util.hasText(timeVariantNumber))
			getSafeAtmSpecificData().setTimeVariantNumber(timeVariantNumber);
	}
	 
	public void setTrk2EquivData(String trk2EquivData) {
		if (Util.hasText(trk2EquivData))
			getSafeEMVRqData().getSafeCardAcctId().setTrk2EquivData(trk2EquivData);
	}
	 
	public void setTrnDt(DateTime trnDt) {
		if (!trnDt.isNull())
			getSafeEMVRqData().setTrnDt(trnDt);
	}
	 
	public void setSrc_TrnSeqCntr(String trnSeqCntr) {
		if (Util.hasText(trnSeqCntr))
			getSafeNetworkTrnInfo().setSrc_TrnSeqCntr(trnSeqCntr);
        setIfxSrcTrnSeqCntr(trnSeqCntr);
	}

	public void setMy_TrnSeqCntr(String trnSeqCntr) {
		if (Util.hasText(trnSeqCntr))
			getSafeNetworkTrnInfo().setMy_TrnSeqCntr(trnSeqCntr);
	}
	 
	public void setLast_TrnSeqCntr(String last_TrnSeqCntr) {
		if (Util.hasText(last_TrnSeqCntr))
			getSafeNetworkTrnInfo().setLast_TrnSeqCntr(last_TrnSeqCntr);
	}
	
	public String getAcctBalAvailableAmt() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalAvailable() == null)
			return null;
		return getEMVRsData().getAcctBalAvailable().getAmt();
	}
	 
	public BalType getAcctBalAvailableBalType() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalAvailable() == null)
			return null;
		return getEMVRsData().getAcctBalAvailable().getBalType();
	}
	 
	public String getAcctBalAvailableCurCode() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalAvailable() == null)
			return null;
		return getEMVRsData().getAcctBalAvailable().getCurCode();
	}

	public AccType getAcctBalAvailableType() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalAvailable() == null)
			return null;
		return getEMVRsData().getAcctBalAvailable().getAcctType();
	}

	public String getAcctBalLedgerAmt() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalLedger() == null)
			return null;
		return getEMVRsData().getAcctBalLedger().getAmt();
	}

	public BalType getAcctBalLedgerBalType() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalLedger() == null)
			return null;
		return getEMVRsData().getAcctBalLedger().getBalType();
	}

	public String getAcctBalLedgerCurCode() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalLedger() == null)
			return null;
		return getEMVRsData().getAcctBalLedger().getCurCode();
	}

	public AccType getAcctBalLedgerType() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getAcctBalLedger() == null)
			return null;
		return getEMVRsData().getAcctBalLedger().getAcctType();
	}
	 
	public void setAcctBalAvailableAmt(String amt) {
		if (Util.hasText(amt))
			getSafeAcctBalAvailable().setAmt(amt);
	}
	 
	public void setAcctBalAvailableBalType(BalType balType) {
		if (balType != null)
			getSafeAcctBalAvailable().setBalType(balType);
	}

	public void setAcctBalAvailableCurCode(String curCode) {
		if (Util.hasText(curCode))
			getSafeAcctBalAvailable().setCurCode(curCode);
	}
	 
	public void setAcctBalAvailableType(AccType acctType) {
		if (acctType != null)
			getSafeAcctBalAvailable().setAcctType(acctType);
	}
	 
	public void setAcctBalLedgerAmt(String amt) {
		if (Util.hasText(amt))
			getSafeAcctBalLedger().setAmt(amt);
	}
	 
	public void setAcctBalLedgerBalType(BalType balType) {
		if (balType != null)
			getSafeAcctBalLedger().setBalType(balType);
	}
	 
	public void setAcctBalLedgerCurCode(String curCode) {
		if (Util.hasText(curCode))
			getSafeAcctBalLedger().setCurCode(curCode);
	}
	 
	public void setAcctBalLedgerType(AccType acctType) {
		if (acctType != null)
			getSafeAcctBalLedger().setAcctType(acctType);
	}

	public Long getCreditTotalTransactionAmount() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getCreditCardData() == null)
			return null;
		return getEMVRsData().getCreditCardData().getCreditTotalTransactionAmount();
	}

	public void setCreditTotalTransactionAmount(Long totalTransactionAmount) {
		if (totalTransactionAmount != null)
		 getSafeCreditCardData().setCreditTotalTransactionAmount(totalTransactionAmount);
	}

	public Long getCreditTotalFeeAmount() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getCreditCardData() == null)
			return null;
		return getEMVRsData().getCreditCardData().getCreditTotalFeeAmount();
	}

	public void setCreditTotalFeeAmount(Long totalFeeAmount) {
		if (totalFeeAmount != null)
			 getSafeCreditCardData().setCreditTotalFeeAmount(totalFeeAmount);
	}

	public Long getCreditInterest() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getCreditCardData() == null)
			return null;
		return getEMVRsData().getCreditCardData().getCreditInterest();
	}

	public void setCreditInterest(Long interest) {
		if (interest != null)
			 getSafeCreditCardData().setCreditInterest(interest);
	}

	public Long getCreditStatementAmount() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getCreditCardData() == null)
			return null;
		return getEMVRsData().getCreditCardData().getCreditStatementAmount();
	}

	public void setCreditStatementAmount(Long statementAmount) {
		if (statementAmount != null)
			getSafeCreditCardData().setCreditStatementAmount(statementAmount);
	}

	public Long getCreditOpenToBuy() {
		if (eMVRsData == null)
			return null;
		if (eMVRsData.getCreditCardData() == null)
			return null;
		return getEMVRsData().getCreditCardData().getCreditOpenToBuy();
	}

	public void setCreditOpenToBuy(Long openToBuy) {
		if (openToBuy != null)
			getSafeCreditCardData().setCreditOpenToBuy(openToBuy);
	}
	
	public String getOrgIdNum() {
		if (getNetworkTrnInfo() == null)
			return null;
		return getNetworkTrnInfo().getOrgIdNum();
	}
	 
	public Long getOrgIdType() {
		if (getOrgRec() == null)
			return null;
		return getOrgRec().getOrgIdType();
	}
	 
	public void setOrgIdNum(String orgIdNum) {
//		if (orgIdNum != null && !"".equals(orgIdNum) && orgIdNum.length() > 0)
		if (Util.hasText(orgIdNum))
//		if (Util.hasText(orgIdNum))
			getSafeNetworkTrnInfo().setOrgIdNum(orgIdNum);
	}
	 
	public void setOrgIdType(Long orgIdType) {
		if (orgIdType != null)
			getSafeOrgRec().setOrgIdType(orgIdType);
	}

//	 
//	public DecimalFormat getAuth_Precision() {
//		return getEMVRqData().getAuth_Precision();
//	}

//	 
//	public void setAuth_Precision(DecimalFormat auth_Precision) {
//		getEMVRqData().setAuth_Precision(auth_Precision);
//	}

	 
	public String getOpkey() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getOpkey();
	}
	 
	public void setOpkey(String opkey) {
		if (Util.hasText(opkey))
			getSafeAtmSpecificData().setOpkey(opkey);
	}
	
	public String getCoreBranchCode() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getCoreBranchCode();
	}
	
	public void setCoreBranchCode(String coreBranchCode) {
		if (Util.hasText(coreBranchCode))
			getSafeAtmSpecificData().setCoreBranchCode(coreBranchCode);
	}
	
	public String getNextOpkey() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getNextOpkey();
	}
	
	public void setNextOpkey(String nextOpkey) {
		if (Util.hasText(nextOpkey))
			getSafeAtmSpecificData().setNextOpkey(nextOpkey);
	}
	
	public String getProperOpkey() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getProperOpkey();
	}
	
	public String getBufferB() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getBufferB();
	}
	
	public void setBufferB(String bufferB) {
		if (Util.hasText(bufferB))
			getSafeAtmSpecificData().setBufferB(bufferB);
	}
	
	public String getBufferC() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getBufferC();
	}
	
	public void setBufferC(String bufferC) {
		if (Util.hasText(bufferC))
			getSafeAtmSpecificData().setBufferC(bufferC);
	}
	
	public IfxType getSecIfxType() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getSecIfxType();
	}
	
	public void setSecIfxType(IfxType secIfxType) {
		if (secIfxType != null)
			getSafeAtmSpecificData().setSecIfxType(secIfxType);
	}
	
	public TrnType getSecTrnType() {
		if (atmSpecificData == null)
			return null;
		return getAtmSpecificData().getSecTrnType();
	}
	
	public void setSecTrnType(TrnType secTrnType) {
		if (secTrnType != null)
			getSafeAtmSpecificData().setSecTrnType(secTrnType);
	}
	
	public Boolean getForceReceipt() {
		if (atmSpecificData == null)
			return false;
		return getAtmSpecificData().getForceReceipt();
	}
	
	public void setForceReceipt(Boolean forceReceipt) {
		if (forceReceipt != null)
			getSafeAtmSpecificData().setForceReceipt(forceReceipt);
	}

	public String getSecondAppPan() {
		if (eMVRqData == null)
			return null;
		return getEMVRqData().getSecondAppPan();
	}
	
	public String getActualSecondAppPan(){
		if(eMVRqData==null)
			return null;
		return getEMVRqData().getActualSecondAppPan();
	}

	public void setSecondAppPan(String secondAppPan) {
		if (Util.hasText(secondAppPan)) {
			getSafeEMVRqData().setSecondAppPan(secondAppPan);
		}
	}
	
	public void setActualSecondAppPAN(String actualSecondAppPan){
		if(Util.hasText(actualSecondAppPan))
			getSafeEMVRqData().setActualSecondAppPAN(actualSecondAppPan);
	}
	
	public void setSafeActualAppPAN(String actualAppPAN) {
		if (Util.hasText(actualAppPAN))
			setActualAppPAN(actualAppPAN);
		else 
			setActualAppPAN(getAppPAN());
	}
	
	public void setSafeActualSecondAppPAN(String actualSecAppPAN) {
		if (Util.hasText(actualSecAppPAN))
			setActualSecondAppPAN(actualSecAppPAN);
		else 
			setActualSecondAppPAN(getSecondAppPan());
	}
//	public String getSecondAcctId() {
//		if (eMVRqData == null)
//			return null;
//		return getEMVRqData().getSecondAcctId();
//	}
//
//	public void setSecondAcctId(String secondAcctId) {
//		if (Util.hasText(secondAcctId))
//			getSafeEMVRqData().setSecondAcctId(secondAcctId);
//	}

	public static String getAmountPath() {
        return "ifx.eMVRqData.auth_Amt";
    }
	 
	public String getInvoiceNumber() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getPaymentData() == null)
			return null;
		return getEMVRqData().getPaymentData().getInvoiceNumber();
	}

	public void setInvoiceNumber(String invoiceNumber) {
		if (Util.hasText(invoiceNumber))
			getSafeEMVRqData().getSafePaymentData().setInvoiceNumber(invoiceNumber);
	}

	public String getInvoiceDate() {
			if (eMVRqData == null)
				return null;
			if (eMVRqData.getPaymentData() == null)
				return null;
			return getEMVRqData().getPaymentData().getInvoiceDate();
	}

	public void setInvoiceDate(String invoiceDate) {
		if (Util.hasText(invoiceDate))
			getSafeEMVRqData().getSafePaymentData().setInvoiceDate(invoiceDate);
	}
	
	public String getIP() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getPaymentData() == null)
			return null;
		return getEMVRqData().getPaymentData().getIP();
	}

	public void setIP(String ip) {
		if (Util.hasText(ip))
			getSafeEMVRqData().getSafePaymentData().setIP(ip);
	}

	public String getEmail() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getPaymentData() == null)
			return null;
		return getEMVRqData().getPaymentData().getEmail();
	}

	public void setEmail(String email) {
		if (Util.hasText(email))
			getSafeEMVRqData().getSafePaymentData().setEmail(email);
	}

//	public String getStatementNumber() {
//		if (eMVRqData == null)
//			return null;
//		if (eMVRqData.getCreditStatementData() == null)
//			return null;
//		return getEMVRqData().getCreditStatementData().getStatementNumber();
//	}
//
//	public void setStatementNumber(String statementNumber) {
//		if (Util.hasText(statementNumber))
//			getSafeEMVRqData().getSafeCreditStatementData().setStatementNumber(statementNumber);
//	}
//	
//	public Long getStatementAmount() {
//		if (eMVRqData == null)
//			return null;
//		if (eMVRqData.getCreditStatementData() == null)
//			return null;
//		return getEMVRqData().getCreditStatementData().getStatementAmount();
//	}
//	
//	public void setStatementAmount(Long statementAmount) {
//		if (statementAmount != null)
//			getSafeEMVRqData().getSafeCreditStatementData().setStatementAmount(statementAmount);
//	}
	
	public Integer getBillCompanyCode() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getBillPaymentData() == null)
			return null;
		return getEMVRqData().getBillPaymentData().getBillCompanyCode();
	}

	public String getBillID() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getBillPaymentData() == null)
			return null;
		return getEMVRqData().getBillPaymentData().getBillID();
	}
	 
	public OrganizationType getBillOrgType() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getBillPaymentData() == null)
			return null;
		return getEMVRqData().getBillPaymentData().getBillOrgType();
	}
	 
	public String getBillPaymentID() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getBillPaymentData() == null)
			return null;
		return getEMVRqData().getBillPaymentData().getBillPaymentID();
	}
	 
	public String getBillUnParsedData() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getBillPaymentData() == null)
			return null;
		return getEMVRqData().getBillPaymentData().getBillUnParsedData();
	}
	
//	public MCIVosoliState getMciVosoliState(){
//		if (eMVRqData == null)
//			return null;
//		if (eMVRqData.getBillPaymentData() == null)
//			return null;
//		return getEMVRqData().getBillPaymentData().getMciVosoliState();
//	}
	
//	public void setMciVosoliState(MCIVosoliState mciVosoliState) {
//		if (mciVosoliState != null) {
//			getSafeEMVRqData().getSafeBillPaymentData().setMciVosoliState(mciVosoliState);
//		}
//	}
	
	public String getMciVosoliDesc(){
		if(eMVRqData == null)
			return null;
		if(eMVRqData.getBillPaymentData() == null)
			return null;
		return getEMVRqData().getBillPaymentData().getMciVosoliDesc();
	}
	
	public void setMciVosoliDesc(String mciVosoliDesc) {
		if (Util.hasText(mciVosoliDesc)) {
			getSafeEMVRqData().getSafeBillPaymentData().setMciVosoliDesc(mciVosoliDesc);
		}
	}
	
	public void setBillCompanyCode(Integer billCompanyCode) {
		if (billCompanyCode != null)
			getSafeEMVRqData().getSafeBillPaymentData().setBillCompanyCode(billCompanyCode);
	}
	 
	public void setBillID(String billID) {
		if (Util.hasText(billID)) {
			getSafeEMVRqData().getSafeBillPaymentData().setBillID(billID);
//			setBillIDRelated(billID);
		}
	}

	public void setBillPaymentID(String billPaymentID) {
		if (Util.hasText(billPaymentID)) {
			getSafeEMVRqData().getSafeBillPaymentData().setBillPaymentID(billPaymentID);
//			setBillPaymentIDRelated(billPaymentID);
		}
	}
	 
	public void setBillUnParsedData(String unParsedData) {
		if (Util.hasText(unParsedData))
			getSafeEMVRqData().getSafeBillPaymentData().setBillUnParsedData(unParsedData);
	}
	
	public void setBillOrgType(OrganizationType billOrgType) {
		if (billOrgType != null /*&& !OrganizationType.UNKNOWN.equals(billOrgType)*/)
			getSafeEMVRqData().getSafeBillPaymentData().setBillOrgType(billOrgType);
	}
	
	public void setBillIDRelated(String billID) {
		try {
	    	setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billID));
	    	setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billID));
	    	setBillOrgType(BillPaymentUtil.extractBillOrgType(billID));
		} catch(Exception e) {
		}
	}
	
	public void setBillPaymentIDRelated(String billPaymentID) {
		try {
			setAuth_Amt(BillPaymentUtil.extractAmount(billPaymentID ));
	    	setReal_Amt(getAuth_Amt());
	    	setTrx_Amt(getAuth_Amt());
//	    	setBillPaymentID(billPaymentID);
		} catch(Exception e) {
			
		}
	}
	
	public FinancialEntity getThirdParty(IfxType refIfxType) {
		if ((ISOFinalMessageType.isBillPaymentMessage(refIfxType) || ISOFinalMessageType.isBillPaymentReverseMessage(refIfxType)) && getBillCompanyCode() != null){
			return OrganizationService.findOrganizationByCompanyCode(getBillCompanyCode(), getBillOrgType());
		} 
		
		if ((ISOFinalMessageType.isPurchaseChargeMessage(refIfxType) || ISOFinalMessageType.isPurchaseChargeReverseMessage(refIfxType)) && getCharge() != null) {
			return getCharge().getEntity();
		}
		
		if ((ISOFinalMessageType.isPurchaseTopupMessage(refIfxType) || ISOFinalMessageType.isPurchaseTopupReverseMessage(refIfxType)) && getTopupData() != null) {
			return FinancialEntityService.findEntity(Organization.class, getTopupData().getTopupCompanyCode().toString());
		}
		
		if ((ISOFinalMessageType.isOnlineBillPaymentMessage(refIfxType) || ISOFinalMessageType.isOnlineBillPaymentReverseMessage(refIfxType)) && getOnlineBillPaymentData() != null) {
			return getOnlineBillPaymentData().getOnlineBillPayment().getEntity();
		}
		if ((ISOFinalMessageType.isThirdPartyPurchaseMessage(refIfxType) || ISOFinalMessageType.isThirdPartyPaymentReverseMessage(refIfxType)) && getThirdPartyCode() !=null) {
			return OrganizationService.findOrganizationByCode(getThirdPartyCode(), OrganizationType.THIRDPARTYPURCHASE);
		}
		
		
		return null;
	}
	
    protected Ifx getNewIfxInstance() {
    	return new Ifx();
	}
    
	public String getCardHolderFamily() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
		return getEMVRqData().getCardAcctId().getCardHolderFamily();
	}
	
	public String getCardHolderMobileNo(){
		if(eMVRqData == null)
			return null;
		if(eMVRqData.getCardHolderMobileNo() == null)
			return null;
		return getEMVRqData().getCardHolderMobileNo();
	}
	 
	public String getCardHolderName() {
		if (eMVRqData == null)
			return null;
		if (eMVRqData.getCardAcctId() == null)
			return null;
		return getEMVRqData().getCardAcctId().getCardHolderName();
	}
	 
	public void setCardHolderFamily(String family) {
//		if (Util.hasText(family))
			getSafeEMVRqData().getSafeCardAcctId().setCardHolderFamily(family);
	}
	 
	public void setCardHolderName(String name) {
//		if (Util.hasText(name))
			getSafeEMVRqData().getSafeCardAcctId().setCardHolderName(name);
	}
	
	public void setCardHolderMobileNo(String mobileNo){
		getSafeEMVRqData().setCardHolderMobileNo(mobileNo);
	}

	public MTNCharge getCharge() {
		if (chargeData == null)
			return null;
		return getChargeData().getCharge();
	}

	public void setCharge(MTNCharge charge) {
		if (charge != null)
			getSafeChargeData().setCharge(charge);
	}

	public MTNChargeState getChargeStatePrv() {
		if (chargeData == null)
			return null;
		return getChargeData().getChargeStatePrv();
	}

	public void setChargeStatePrv(MTNChargeState state) {
		if (state != null)
			getSafeChargeData().setChargeStatePrv(state);
	}
	
	public MTNChargeState getChargeStateNxt() {
		if (chargeData == null)
			return null;
		return getChargeData().getChargeStateNxt();
	}
	
	public void setChargeStateNxt(MTNChargeState state) {
		if (state != null)
			getSafeChargeData().setChargeStateNxt(state);
	}
	
	public Integer getChargeCompanyCode() {
		if (chargeData == null)
			return null;
		return getChargeData().getChargeCompanyCode();
	}
	
	public void setChargeCompanyCode(Integer companyCode) {
		if (companyCode != null)
			getSafeChargeData().setChargeCompanyCode(companyCode);
	}

	/*******************************/
//	public Organization getTopupCompany() {
//		if (topupData == null)
//			return null;
//		return getTopupData().getTopupCompany();
//	}
//
//	public void setTopupCompany(Organization topupCompany) {
//		if(topupCompany != null)
//			getSafeTopupData().setTopupCompany(topupCompany);
//	}

	public Long getTopupCompanyCode() {
		if (topupData == null)
			return null;
		return getTopupData().getTopupCompanyCode();
	}

	public void setTopupCompanyCode(Long topupCompanyCode) {
		if(topupCompanyCode != null)
			getSafeTopupData().setTopupCompanyCode(topupCompanyCode);
	}
	
	
	/********************* ghasedak data *************************/
	public GhasedakItemType getGhasedakItemType(){
		if(ghasedakData == null)
			return null;
		return getGhasedakData().getItemType();
	}
	public void setGhasedakItemType(GhasedakItemType ghasedakItemType){
		if(ghasedakItemType != null)
			getSafeGhasedakData().setItemType(ghasedakItemType);
	}
	public List<GhasedakRsItem> getGhasedakResponseItem(){
		if(ghasedakData == null)
			return null;
		return getGhasedakData().getGhasedakRsItems();
	}
	public void setGhasedakResponseItems(List<GhasedakRsItem> ghasedakResponseItem){
		if(ghasedakResponseItem != null && !ghasedakResponseItem.isEmpty())
			getSafeGhasedakData().setGhasedakRsItems(ghasedakResponseItem);
	}
	/*************************************************************************/
	
	public Long getOnlineBillPaymentCompanyCode(){
		if(onlineBillPaymentData == null)
			return null;
		return getOnlineBillPaymentData().getCompany();
	}
	
	public void setOnlineBillPaymentCompanyCode(Long onlineBillpaymentCompanyCode){
		if(onlineBillpaymentCompanyCode != null)
			getSafeOnlineBillPaymentData().setCompany(onlineBillpaymentCompanyCode);			
	}

	public Long getTopupCellPhoneNumber() {
		if (topupData == null)
			return null;
		return getTopupData().getCellPhoneNumber();
	}

	public void setTopupCellPhoneNumber(Long cellPhoneNumber) {
		if(cellPhoneNumber != null)
			getSafeTopupData().setCellPhoneNumber(cellPhoneNumber);
	}

	public Long getTopupSerialNo() {
		if (topupData == null)
			return null;
		return getTopupData().getSerialNo();
	}
	
	public void setTopupSerialNo(Long serialNo) {
		if(serialNo != null)
			getSafeTopupData().setSerialNo(serialNo);
	}
	/*******************************/
	
	public Lottery getLottery() {
		if (lotteryData == null)
			return null;
		return getLotteryData().getLottery();
	}
	
	public void setLottery(Lottery lottery) {
		if (lottery != null)
			getSafeLotteryData().setLottery(lottery);
	}
	
	public LotteryState getLotteryStatePrv() {
		if (lotteryData == null)
			return null;
		return getLotteryData().getLotteryStatePrv();
	}
	
	public void setLotteryStatePrv(LotteryState state) {
		if (state != null)
			getSafeLotteryData().setLotteryStatePrv(state);
	}
	
	public LotteryState getLotteryStateNxt() {
		if (lotteryData == null)
			return null;
		return getLotteryData().getLotteryStateNxt();
	}
	
	public void setLotteryStateNxt(LotteryState state) {
		if (state != null)
			getSafeLotteryData().setLotteryStateNxt(state);
	}
	/*******************************/
	
	public void setMainAccountNumber(String mainAccountNumber) {
		if (Util.hasText(mainAccountNumber))
			getSafeEMVRsData().setMainAccountNumber(mainAccountNumber);
//		if (getEMVRsData() == null)
//			setEMVRsData(new eMVRsData());
//		getEMVRsData().setMainAccountNumber( mainAccountNumber);
	}

	public String getMainAccountNumber() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getMainAccountNumber();
	}
	

	public void setUserLanguage(UserLanguage userLanguage) {
		if (userLanguage != null)
			getSafeNetworkTrnInfo().setUserLanguage(userLanguage);
	}

	public UserLanguage getUserLanguage() {
		if (networkTrnInfo == null)
			return null;
		return getNetworkTrnInfo().getUserLanguage();
	}

	public void setBankStatementData(List<BankStatementData> bankStatementData) {
		if (bankStatementData != null && !bankStatementData.isEmpty())
			getSafeEMVRsData().setBankStatementData(bankStatementData);
	}

	public List<BankStatementData> getBankStatementData() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getBankStatementData();
	}
	
	public void setCardAccountInformation(List<CardAccountInformation> cardAccountInformation) {
		if (cardAccountInformation != null && !cardAccountInformation.isEmpty())
			getSafeEMVRsData().setCardAccountInformation(cardAccountInformation);
	}
	
	public List<CardAccountInformation> getCardAccountInformation() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getCardAccountInformation();
	}
		
	public Ifx clone() {
		Ifx newIfx = getNewIfxInstance();

		if (ifxDirection != null)
			newIfx.setIfxDirection(getIfxDirection().copy());

		newIfx.setIfxType(getIfxType());
		newIfx.setTrnType(getTrnType());

		if (eMVRqData != null)
			newIfx.setEMVRqData(eMVRqData);

		if (eMVRsData != null)
			newIfx.setEMVRsData(eMVRsData);

//		if (status != null)
//			newIfx.setStatus(status.get(0));

		if (networkTrnInfo != null)
			newIfx.setNetworkTrnInfo(networkTrnInfo);

		if (thirdPartyData != null)
			newIfx.setThirdPartyData(thirdPartyData);
		
//		if (getOrgRec() != null)
//			newIfx.setOrgRec(getOrgRec());

		newIfx.setReceivedDt(getReceivedDt());
		newIfx.setPostedDt(getPostedDt());
		newIfx.setSettleDt(getSettleDt());

		if (originalDataElements != null)
			newIfx.setOriginalDataElements(originalDataElements);

		if (atmSpecificData != null)
			newIfx.setAtmSpecificData(atmSpecificData);

		if (posSpecificData != null)
			newIfx.setPosSpecificData(posSpecificData);
		
//		if (saderatSpecificData != null)
//			newIfx.setSaderatSpecificData(saderatSpecificData);

		if (chargeData != null)
			newIfx.setChargeData(chargeData);
		
		if (topupData != null)
			newIfx.setTopupData(topupData);
		
		if(ghasedakData != null)
			newIfx.setGhasedakData(ghasedakData);
		
		if(onlineBillPaymentData != null)
			newIfx.setOnliBillPaymentData(onlineBillPaymentData);

		if (lotteryData != null)
			newIfx.setLotteryData(lotteryData);

		if (migrationData != null)
			newIfx.setMigrationData(migrationData);
		
		if (migrationSecondData != null)
			newIfx.setMigrationSecondData(migrationSecondData);

		if (keyManagement != null)
			newIfx.setKeyManagement(keyManagement);

		if (reconciliationData != null)
			newIfx.setReconciliationData(reconciliationData);

		if (clearingData != null && !clearingData.isEmpty())
			newIfx.setClearingData(clearingData);

		if (Util.hasText(mti))
			newIfx.setMti(mti);

		if (ThirdPartyTerminalCode != null)
			newIfx.setThirdPartyTerminalCode(ThirdPartyTerminalCode);
		
        if (actionCode != null)
            newIfx.setActionCode(actionCode);
//		if(mizanSpecificData != null)
//			newIfx.setMizanSpecificData(mizanSpecificData);
		
		if(firstTrxId != null)
			newIfx.setFirstTrxId(firstTrxId);

        //if(this.getAmountSettlement() != null) {
        //    newIfx.setAmountSettlement(this.getAmountSettlement());
        //}

        //if(Util.hasText(this.getConvRateSettlement())) {
        //    newIfx.setConvRateSettlement(this.getConvRateSettlement());
        //}

        if(Util.hasText(this.getTimeLocalTran())) {
            newIfx.setTimeLocalTran(this.getTimeLocalTran());
        }

		newIfx.setTransferFromDesc(transferFromDesc);
		
		if(Util.hasText(transferToDesc))
			newIfx.setTransferToDesc(transferToDesc);

        if(Util.hasText(this.getNetworkData()))
            newIfx.setNetworkData(NetworkData);

        if(Util.hasText(this.getOtherAmounts()))
            newIfx.setOtherAmounts(otheramounts); //Raza MASTERCARD

//		gholami(Task45875)
		if(Util.hasText(shenaseOfTransferToAccount))
			newIfx.setShenaseOfTransferToAccount(shenaseOfTransferToAccount);
        newIfx.setDateLocalTran(this.getDateLocalTran());
        //newIfx.setDateConversion(this.getDateConversion());
        newIfx.setMerchantType(this.getMerchantType());
        newIfx.setMerchCountryCode(this.getMerchCountryCode());
        newIfx.setPanCountryCode(this.getPanCountryCode());
        newIfx.setPosEntryModeCode(this.getPosEntryModeCode());
        newIfx.setCardSequenceNo(this.getCardSequenceNo());
        newIfx.setAmountTranFee(this.getAmountTranFee());
        newIfx.setMyNetworkRefId(this.getMyNetworkRefId());
        newIfx.setPosConditionCode(this.getPosConditionCode());
        newIfx.setApprovalCode(this.getApprovalCode());
        newIfx.setCardAcceptNameLoc(this.getCardAcceptNameLoc());
        //newIfx.setCurrCodeSettlement(this.getCurrCodeSettlement());
        //newIfx.setIccCardData(this.getIccCardData()); //Raza commenitng not using this column
        newIfx.setAuthAgentInstId(this.getAuthAgentInstId()); //Raza MASTERCARD
        newIfx.setSelfDefineData(this.getSelfDefineData());
        newIfx.setNetworkManageInfoCode(this.getNetworkManageInfoCode());
        //newIfx.setPosData(this.getPosData()); //Raza commenting use other column - Other Amounts
        newIfx.setAccountId1(this.getAccountId1());
        newIfx.setAccountId2(this.getAccountId2());
        newIfx.setRecordData(this.getRecordData());
		newIfx.setInstitutionId(this.getInstitutionId()); //Raza use Channel Institution
		newIfx.id = null;
		return newIfx;
	}

	public Ifx copy() {
        Ifx newIfx = getNewIfxInstance();
        if (getIfxDirection() != null)
            newIfx.setIfxDirection(getIfxDirection().copy());

        newIfx.setIfxType(getIfxType());
        newIfx.setTrnType(getTrnType());

        if (getEMVRqData() != null)
            newIfx.setEMVRqData(getEMVRqData().copy());

        if (getEMVRsData() != null)
            newIfx.setEMVRsData(getEMVRsData().copy());

//		if (getStatus() != null)
//			newIfx.setStatus(getStatus().copy());

        if (getNetworkTrnInfo() != null)
            newIfx.setNetworkTrnInfo(getNetworkTrnInfo().copy());

        if (getThirdPartyData() != null)
            newIfx.setThirdPartyData(getThirdPartyData().copy());

//		if (getOrgRec() != null)
//			newIfx.setOrgRec(getOrgRec().copy());

        newIfx.setReceivedDt(getReceivedDt());

        if (getOriginalDataElements() != null)
            newIfx.setOriginalDataElements(getOriginalDataElements().copy());

        if (getAtmSpecificData() != null)
            newIfx.setAtmSpecificData(getAtmSpecificData().copy());

        if (getPosSpecificData() != null)
            newIfx.setPosSpecificData(getPosSpecificData().copy());

//		if (getSaderatSpecificData() != null)
//			newIfx.setSaderatSpecificData(getSaderatSpecificData().copy());

        if (getChargeData() != null)
            newIfx.setChargeData(getChargeData().copy());

        if (getTopupData() != null)
            newIfx.setTopupData(getTopupData().copy());

        if (getGhasedakData() != null)
            newIfx.setGhasedakData(getGhasedakData().copy());

        if (getOnlineBillPaymentData() != null)
            newIfx.setOnliBillPaymentData(getOnlineBillPaymentData().copy());

        if (getMigrationData() != null)
            newIfx.setMigrationData(getMigrationData()/*.copy()*/);

        if (getMigrationSecondData() != null)
            newIfx.setMigrationSecondData(getMigrationSecondData()/*.copy()*/);

        if (getLotteryData() != null)
            newIfx.setLotteryData(getLotteryData().copy());

        if (settleDt != null)
            newIfx.setSettleDt(settleDt);

        if (postedDt != null)
            newIfx.setPostedDt(postedDt);

        if (receivedDt != null)
            newIfx.setReceivedDt(receivedDt);

        if (reconciliationData != null)
            newIfx.setReconciliationData(reconciliationData);

        if (ThirdPartyTerminalCode != null)
            newIfx.setThirdPartyTerminalCode(ThirdPartyTerminalCode);

        if (actionCode != null)
            newIfx.setActionCode(actionCode);
        if (Util.hasText(mti))
            newIfx.setMti(mti);
        if (firstTrxId != null)
            newIfx.setFirstTrxId(firstTrxId);

        //if (Util.hasText(this.getConvRateSettlement())) {
        //    newIfx.setConvRateSettlement(this.getConvRateSettlement());
        //}

        //if (this.getAmountSettlement() != null) {
         //   newIfx.setAmountSettlement(this.getAmountSettlement());
        //}

        if (Util.hasText(this.getTimeLocalTran())) {
            newIfx.setTimeLocalTran(this.getTimeLocalTran());
        }

        if(Util.hasText(this.getDateLocalTran())) {
            newIfx.setDateLocalTran(this.getDateLocalTran());
        }

		//if (Util.hasText(this.getDateConversion())) {
        //    newIfx.setDateConversion(this.getDateConversion());
        //}

        if (Util.hasText(this.getMerchantType())) {
            newIfx.setMerchantType(this.getMerchantType());
        }

        if(Util.hasText(this.getMerchCountryCode())) {
            newIfx.setMerchCountryCode(this.getMerchCountryCode());
        }

        if(Util.hasText(this.getPanCountryCode())) {
            newIfx.setPanCountryCode(this.getPanCountryCode());
        }

        if(Util.hasText(this.getPosEntryModeCode())) {
            newIfx.setPosEntryModeCode(this.getPosEntryModeCode());
        }

        if(Util.hasText(this.getCardSequenceNo())) {
            newIfx.setCardSequenceNo(this.getCardSequenceNo());
        }

        if(Util.hasText(this.getAmountTranFee())) {
            newIfx.setAmountTranFee(this.getAmountTranFee());
        }

        if(Util.hasText(this.getPosConditionCode())) {
            newIfx.setPosConditionCode(this.getPosConditionCode());
        }

        if(Util.hasText(this.getApprovalCode())) {
            newIfx.setApprovalCode(this.getApprovalCode());
        }

        if(Util.hasText(this.getCardAcceptNameLoc())) {
            newIfx.setCardAcceptNameLoc(this.getCardAcceptNameLoc());
        }

        //if(Util.hasText(this.getCurrCodeSettlement())) {
        //    newIfx.setCurrCodeSettlement(this.getCurrCodeSettlement());
        //}

        if(Util.hasText(this.getSelfDefineData())) {
            newIfx.setSelfDefineData(this.getSelfDefineData());
        }

        if(Util.hasText(this.getAuthAgentInstId()))
        {
            newIfx.setAuthAgentInstId(this.authAgentInstId);
        }

        if(Util.hasText(this.getNetworkManageInfoCode())) {
            newIfx.setNetworkManageInfoCode(this.getNetworkManageInfoCode());
        }
		if(Util.hasText(transferFromDesc))
			newIfx.setTransferFromDesc(transferFromDesc);

        if(Util.hasText(this.getNetworkData()))
            newIfx.setNetworkData((NetworkData));

        if(Util.hasText(this.getOtherAmounts()))
            newIfx.setOtherAmounts((otheramounts)); //Raza MASTERCARD


		if(Util.hasText(transferToDesc))
			newIfx.setTransferToDesc(transferToDesc);
			
		newIfx.setAccountId1(this.getAccountId1());
        newIfx.setAccountId2(this.getAccountId2());
        newIfx.setRecordData(this.getRecordData());
	
			
        newIfx.setInstitutionId(this.getInstitutionId()); //Raza use Channel Institution
		newIfx.id = null;
		return newIfx;

	}

	public void copyFields(Ifx source) {
        if (source.getEMVRqData() != null) {
            this.getSafeEMVRqData().copyFields(source.getEMVRqData());

            setSecurityFields(source);
        }

        if (source.getOriginalDataElements() != null)
            this.getSafeOriginalDataElements().copyFields(source.getOriginalDataElements());

        if (source.getNetworkTrnInfo() != null) {
            this.getSafeNetworkTrnInfo().copyFields(source.getNetworkTrnInfo());
        }

        if (source.getThirdPartyData() != null) {
            this.getSafeThirdPartyData().copyFields(source.getThirdPartyData());
        }

        if (source.getOrgRec() != null) {
            this.getSafeOrgRec().copyFields(source.getOrgRec());
        }

        if (source.getAtmSpecificData() != null) {
            this.getSafeAtmSpecificData().copyFields(source.getAtmSpecificData());
        }

        if (source.getPosSpecificData() != null) {
            if (getPosSpecificData() == null)
                this.setPosSpecificData(source.getPosSpecificData());
        }

//		if (source.getSaderatSpecificData() != null) {
//			if (getSaderatSpecificData() == null)
//				this.setSaderatSpecificData(source.getSaderatSpecificData());
//		}

        if (source.getChargeData() != null) {
            if (IfxType.LAST_PURCHASE_CHARGE_RQ.equals(this.getIfxType()))
                this.setChargeData(source.getChargeData());
            else
                this.getSafeChargeData().copyFields(source.getChargeData());
        }

        if (source.getTopupData() != null) {
            this.setTopupData(source.getTopupData());
//			this.getSafeTopupData().copyFields(source.getTopupData());
        }

        if (source.getGhasedakData() != null) {
            this.setGhasedakData(source.getGhasedakData());
        }

        if (source.getOnlineBillPaymentData() != null) {
            this.getSafeOnlineBillPaymentData().copyFields(source.getOnlineBillPaymentData());
        }

        if ((!(IfxType.TRANSFER_RQ.equals(this.getIfxType()) &&
                (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(source.getIfxType()) || IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(source.getIfxType()))) ||
                !(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(this.getIfxType()) &&
                        (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(source.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(source.getIfxType())))) &&
                source.getMigrationData() != null) {
            this.setMigrationData(source.getMigrationData());
        }

        if ((!(IfxType.TRANSFER_RQ.equals(this.getIfxType()) &&
                (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(source.getIfxType()) || IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(source.getIfxType()))) ||
                !(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(this.getIfxType()) &&
                        (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(source.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(source.getIfxType())))) &&
                source.getMigrationSecondData() != null) {
            this.setMigrationSecondData(source.getMigrationSecondData());
        }

        if ((IfxType.TRANSFER_RQ.equals(this.getIfxType()) &&
                (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(source.getIfxType()) || IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(source.getIfxType()))) ||
                (IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(this.getIfxType()) &&
                        (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(source.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(source.getIfxType())))) {
//			if (!Util.hasText(getSecondAppPan())) {
            try {
                this.setSecondAppPan(source.getAppPAN());
                this.setActualSecondAppPAN(source.getAppPAN());
                this.getRecvBankId();
                if (!Util.isAccount(getSecondAppPan()))
                    this.setRecvBankId(getSecondAppPan().substring(0, 6));
                //else
                //this.setRecvBankId(GlobalContext.getInstance().getMyInstitution().getBin()); //Raza commenting for MultiInstitution
            } catch (Exception e) {

            }
//			}
        }

        if (source.getLotteryData() != null) {
            this.getSafeLotteryData().copyFields(source.getLotteryData());
        }

        if (source.getReconciliationData() != null) {
            this.getSafeReconciliationData().copyFields(source.getReconciliationData());
        }

        if (this.getSettleDt() == null && source.getSettleDt() != null)
            this.setSettleDt(source.getSettleDt());

        if (this.getPostedDt() == null && source.getPostedDt() != null)
            this.setPostedDt(source.getPostedDt());

        if (getThirdPartyTerminalCode() == null && source.getThirdPartyTerminalCode() != null)
            this.setThirdPartyTerminalCode(source.getThirdPartyTerminalCode());

        //System.out.println("Copying MTI.... IFX-ID [" + this.getId() + "]"); //Raza TEMP
        if (!Util.hasText(this.getMti())) {
            //System.out.println("Copying MTI not found...."); //Raza TEMP
            if (Util.hasText(source.getMti())) {
                //System.out.println("Copying MTI Source MTI found...."); //Raza TEMP
                if (this.isResponse()) {
                    //System.out.println("Copying MTI This is Response.."); //Raza TEMP
                    this.setMti(ISOMessageTypes.getResponseMTI(source.getMti()));
                    //System.out.println("Copying MTI Setting MTI [" + this.getMti() + "]"); //Raza TEMP
                } else {
                    //System.out.println("Copying MTI same as source [" + source.getMti() + "]"); //Raza TEMP
                    this.setMti(source.getMti());
                }
            }
        }

        if (source.getFirstTrxId() != null)
            this.setFirstTrxId(source.getFirstTrxId());

        if (Util.hasText(source.getActionCode()))
            this.setActionCode(source.getActionCode());
        //if (source.getAmountSettlement() != null)
            //this.setAmountSettlement(source.getAmountSettlement());
        //if (Util.hasText(source.getConvRateSettlement()))
            //this.setConvRateSettlement(source.getConvRateSettlement());
        if (Util.hasText(source.getTransferFromDesc()))
            this.setTransferFromDesc(source.getTransferFromDesc());

        if (Util.hasText(source.getTransferToDesc()))
            this.setTransferToDesc(source.getTransferToDesc());
        if (Util.hasText(source.getTimeLocalTran()))
            this.setTimeLocalTran(source.getTimeLocalTran());

        if (Util.hasText(source.getDateLocalTran()))
            this.setDateLocalTran(source.getDateLocalTran());
        //m.rehman: if getSettleDt() return returns null, exception will occur in toString() method
        //commnemting below condition and change
        //if (Util.hasText(source.getSettleDt().toString()))
        if (source.getSettleDt() != null)
            this.setSettleDt(source.getSettleDt());
        //if (Util.hasText(source.getDateConversion()))
            //this.setDateConversion(source.getDateConversion());

        //System.out.println("Copying Sec_CurRate"); //Raza TEMP
        if (Util.hasText(source.getSec_CurRate())) //Raza MasterCard for DE-10
        {
            //System.out.println("Sec_CurRate DE-10 [" + source.getSec_CurRate() + "]"); //Raza TEMP
            this.setSec_CurRate(source.getSec_CurRate());
        }
        //System.out.println("Copying Sec_CurDate"); //Raza TEMP
        if (Util.hasText(source.getSec_CurDate())) //Raza MasterCard for DE-16
        {
            //System.out.println("Sec_CurDate DE-16 [" + source.getSec_CurDate() + "]"); //Raza TEMP
            this.setSec_CurDate(source.getSec_CurDate());
        }

        if (Util.hasText(source.getMerchantType()))
            this.setMerchantType(source.getMerchantType());

        if (Util.hasText(source.getMerchCountryCode()))
            this.setMerchCountryCode(source.getMerchCountryCode());

        if (Util.hasText(source.getPanCountryCode()))
            this.setPanCountryCode(source.getPanCountryCode());

        if (Util.hasText(source.getPosConditionCode()))
            this.setPosConditionCode(source.getPosConditionCode());

        if (Util.hasText(source.getApprovalCode()))
            this.setApprovalCode(source.getApprovalCode());

        if (Util.hasText(source.getAmountTranFee()))
            this.setAmountTranFee(source.getAmountTranFee());

        //if (source.getCurrCodeSettlement() != null)
            //this.setCurrCodeSettlement(source.getCurrCodeSettlement());

        //System.out.println("Copying Network Data.."); //Raza TEMP
        if(Util.hasText(source.getNetworkData()))
            this.setNetworkData((source.getNetworkData()));

        if(Util.hasText(source.getOtherAmounts()))
            this.setOtherAmounts((source.getOtherAmounts())); //Raza MASTERCARD

        if(source.getInstitutionId() != null) //Raza use Channel Institution
            this.setInstitutionId(source.getInstitutionId());
			
        if (!Util.hasText(this.getAccountId1()))
            if (Util.hasText(source.getAccountId1()))
                this.setAccountId1(source.getAccountId1());

        if (!Util.hasText(this.getAccountId2()))
            if (Util.hasText(source.getAccountId2()))
                this.setAccountId2(source.getAccountId2());

        if (!Util.hasText(this.getRecordData()))
            if (Util.hasText(source.getRecordData()))
                this.setRecordData(source.getRecordData());

        //m.rehman: copying sec currency from request message
        if (source.getSec_Currency() != null)
            this.setSec_Currency(source.getSec_Currency());

        if (source.getExpDt() != null)
            this.setExpDt(source.getExpDt());

        if (this.getFwdBankId() == null)
            this.setFwdBankId(source.getFwdBankId());

        if (Util.hasText(source.getRecvBankId()))
            this.setRecvBankId(source.getRecvBankId());
	}
	
	private void setSecurityFields(Ifx source) {
		String securityData = GlobalContext.getInstance().getSecurityData(source.getTransaction().getLifeCycleId());
		if (!Util.hasText(securityData))
			return;
		
		String trk2 = (String) TransactionService.getFromSecurity(securityData, "Trk2EquivData");
		String pin = (String) TransactionService.getFromSecurity(securityData, "PINBlock");
		String cvv2 = (String) TransactionService.getFromSecurity(securityData, "CVV2");
		String expDt = (String) TransactionService.getFromSecurity(securityData, "ExpDt");
		
		if (!Util.hasText(getTrk2EquivData()) && Util.hasText(trk2))
			setTrk2EquivData(trk2);
		
		if (!Util.hasText(getPINBlock()) && Util.hasText(pin))
			setPINBlock(pin);
		
		if (!Util.hasText(getCVV2()) && Util.hasText(cvv2))
			setCVV2(cvv2);
		
		if (getExpDt() != null && Util.hasText(expDt))
			setExpDt(Util.longValueOf(expDt));
		
	}
	
	public String getMti() {
		return mti;
	}

	public void setMti(String mit) {
		this.mti = mit;
	}
	
	public String toString(){
		return id != null ? id.toString() : "";
	}

	public void setReconciliationData(ReconciliationData reconciliationData) {
		this.reconciliationData = reconciliationData;
	}

	public ReconciliationData getReconciliationData() {
		return reconciliationData;
	}

	public String getSubsidiaryAccByIndex(String index) {
		List<CardAccountInformation> list = getCardAccountInformation();
		if(list ==  null || list.size() == 0)
			return "";
		
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getIndex().equals(index))
				return list.get(i).getAccountNumber();
		}
		return "";
	}

	public void setEndPointTerminal(Terminal endPointTerminal) {
		if(endPointTerminal!=null){
			if(!endPointTerminal.equals(getEndPointTerminal()))
				this.endPointTerminal = endPointTerminal;			
		}else if(this.endPointTerminal !=null)			
			this.endPointTerminal = endPointTerminal;			
	}

	public Terminal getEndPointTerminal() {
		return endPointTerminal;
	}

	public Boolean getRequest() {
		return request;
	}

	public void setRequest(Boolean request) {
		this.request = request;
	}

	public UiSpecificData getUiSpecificData() {
		return uiSpecificData;
	}
	public void setUiSpecificData(UiSpecificData uiSpecificData) {
		this.uiSpecificData = uiSpecificData;
	}

	public Terminal getOriginatorTerminal() {
		return originatorTerminal;
	}

	public void setOriginatorTerminal(Terminal originatorTerminal) {
		this.originatorTerminal = originatorTerminal;
	}

	public Long getEndPointTerminalCode() {
		return endPointTerminalCode;
	}

	public void setEndPointTerminalCode(Long endPointTerminalCode) {
		this.endPointTerminalCode = endPointTerminalCode;
	}

	public Boolean getUpdateRequired() {
		if(posSpecificData == null)
			return null;
		return posSpecificData.getUpdateRequired();
	}

	public void setUpdateRequired(Boolean updateRequired) {
		if(updateRequired != null)
			getSafePosSpecificData().setUpdateRequired(updateRequired);
	}

	public Boolean getUpdateReceiptRequired() {
		return posSpecificData == null ? null : posSpecificData.getUpdateReceiptRequired();
	}

	public void setUpdateReceiptRequired(Boolean updateReceiptRequired) {
		if(updateReceiptRequired != null)
			getSafePosSpecificData().setUpdateReceiptRequired(updateReceiptRequired);
	}

	public Integer getConfirmationCode() {
		return posSpecificData == null ? null : posSpecificData.getConfirmationCode();
	}

	public void setConfirmationCode(Integer confirmationCode) {
		if(confirmationCode != null)
			getSafePosSpecificData().setConfirmationCode(confirmationCode);
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Long getReceivedDtLong() {
		return receivedDtLong;
	}

	public void setReceivedDtLong(Long receivedDtLong) {
		this.receivedDtLong = receivedDtLong;
	}

	public Long getThirdPartyTerminalCode() {
		return ThirdPartyTerminalCode;
	}

	public void setThirdPartyTerminalCode(Long thirdPartyTerminalCode) {
		ThirdPartyTerminalCode = thirdPartyTerminalCode;
	}

	public MigrationData getSafeMigrationData() {
		if (migrationData == null)
			migrationData = new MigrationData();
		return migrationData;
	}
	
	public MigrationData getMigrationData() {
		return migrationData;
	}

	public void setMigrationData(MigrationData migrationData) {
		this.migrationData = migrationData;
	}

	public MigrationData getSafeMigrationSecondData() {
		if (migrationSecondData == null)
			migrationSecondData = new MigrationData();
		return migrationSecondData;
	}
	
	public MigrationData getMigrationSecondData() {
		return migrationSecondData;
	}

	public void setMigrationSecondData(MigrationData migrationSecondData) {
		this.migrationSecondData = migrationSecondData;
	}

//	public String getSaderatExtraInfo() {
//		if (saderatSpecificData == null)
//			return null;
//		return saderatSpecificData.getExtraInfo();
//	}

//	public void setSaderatExtraInfo(String extraInfo) {
//		if(Util.hasText(extraInfo))
//			getSafeSaderatSpecificData().setExtraInfo(extraInfo);
//	}
	
	public String getExtraInfo() {
		if(posSpecificData == null)
			return null;
		return posSpecificData.getExtraInfo();
	}

	public void setExtraInfo(String extraInfo) {
		if(Util.hasText(extraInfo))
			getSafePosSpecificData().setExtraInfo(extraInfo);
	}
	public String getOnlineBillPaymentRefNum(){
		 if(onlineBillPaymentData == null)
			 return null;
		 return getOnlineBillPaymentData().getRefNum();			 
	 }
	public void setOnlineBillPaymentRefNum(String refNum){
		if(Util.hasText(refNum)){
			getSafeOnlineBillPaymentData().setRefNum(refNum);
		}
	}
	public String getOnlineBillPaymentDescription(){
		if(onlineBillPaymentData== null)
			return null;
		return getOnlineBillPaymentData().getDescription();
	}
	public void setOnlineBillPaymentDescription(String description){
		if(Util.hasText(description))
			getSafeOnlineBillPaymentData().setDescription(description);
	}

//	public MizanSpecificData getMizanSpecificData() {
//		return mizanSpecificData;
//	}
//	public void setMizanSpecificData(MizanSpecificData mizanSpecificData) {
//		this.mizanSpecificData = mizanSpecificData;
//	}

	public byte getDummycol() {
		return dummycol;
	}

	public void setDummycol(byte dummycol) {
		this.dummycol = dummycol;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}


	public String getShebaCode() {
		if (eMVRsData == null)
			return null;
		return getEMVRsData().getShebaCode();
	}

	public void setShebaCode(String shebaCode) {
		if (Util.hasText(shebaCode))
			getSafeEMVRsData().setShebaCode(shebaCode);
	}
	
	//TASK Task081 : ATM Saham feature
	public String getStockCode() {
		if (eMVRsData == null)
			return null;
		return eMVRsData.getStockCode();
	}

	public void setStockCode(String stockCode) {
		if (Util.hasText(stockCode))
			getSafeEMVRsData().setStockCode(stockCode);
	}
	
	//TASK Task081 : ATM Saham feature
	public Long getStockCount() {
		if (eMVRsData == null)
			return null;
		return eMVRsData.getStockCount();
	}

	public void setStockCount(Long stockCount) {
		if (stockCount != null)
			getSafeEMVRsData().setStockCount(stockCount);
	}	
	
	 public ReceiptOptionType getReceiptOption() {
	    	if (atmSpecificData == null)
	    		return null;
			return getAtmSpecificData().getReceiptOption();
		}

    public String getPosConditionCode() {
        return posConditionCode;
    }

    public void setPosConditionCode(String posConditionCode) {
        this.posConditionCode = posConditionCode;
    }
		public void setReceiptOption(ReceiptOptionType receiptOption) {
			if (receiptOption != null)
				getSafeAtmSpecificData().setReceiptOption(receiptOption);
		}
		
    public void setInstitutionId(String institutionId) { //Raza for Channel Institution
        this.InstitutionId = institutionId;
    }

    public String getInstitutionId() { return this.InstitutionId; }//Raza for Channel Institution

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    //public String getDateConversion() {
      //  return dateConversion;
    //}

    //public void setDateConversion(String dateConversion) {
      //  this.dateConversion = dateConversion;
    //}

    public String getDateLocalTran() {
        return dateLocalTran;
    }

    public void setDateLocalTran(String dateLocalTran) {
        this.dateLocalTran = dateLocalTran;
    }

    public String getMesgSecurityCode() {
        return mesgSecurityCode;
    }

    public void setMesgSecurityCode(String mesgSecurityCode) {
        this.mesgSecurityCode = mesgSecurityCode;
    }

    public String getNetworkManageInfoCode() {
        return networkManageInfoCode;
    }

    public void setNetworkManageInfoCode(String networkManageInfoCode) {
        this.networkManageInfoCode = networkManageInfoCode;
    }

    public String getSelfDefineData() {
        return selfDefineData;
    }

    public void setSelfDefineData(String selfDefineData) {
        this.selfDefineData = selfDefineData;
    }

    public String getNetworkInstId() {
        return networkInstId;
    }

    public void setNetworkInstId(String networkInstId) {
        this.networkInstId = networkInstId;
    }

    public String getCardAcceptNameLoc() {
        return cardAcceptNameLoc;
    }

    public void setCardAcceptNameLoc(String cardAcceptNameLoc) {
        this.cardAcceptNameLoc = cardAcceptNameLoc;
    }
	
	public String getPosPinCaptureCode() {
        return posPinCaptureCode;
    }

    public void setPosPinCaptureCode(String posPinCaptureCode) {
        this.posPinCaptureCode = posPinCaptureCode;
    }

    public String getAmountTranFee() {
        return amountTranFee;
    }

    public void setAmountTranFee(String amountTranFee) {
        this.amountTranFee = amountTranFee;
    }

    public String getPosEntryModeCode() {
        return posEntryModeCode;
    }

    public String getCardSequenceNo() {
        return cardSequenceNo;
    }

    public void setPosEntryModeCode(String posEntryModeCode) {
        this.posEntryModeCode = posEntryModeCode;
    }

    public void setCardSequenceNo(String cardSequenceno) {
        this.cardSequenceNo = cardSequenceno;
    }

    public String getAddResponseData() {
        return addResponseData;
    }

    public void setAddResponseData(String addResponseData) {
        this.addResponseData = addResponseData;
    }

    public String getAddDataPrivate() {
        return addDataPrivate;
    }

    public void setAddDataPrivate(String addDataPrivate) {
        this.addDataPrivate = addDataPrivate;
    }

    public String getAuthAgentInstId() {
        return authAgentInstId;
    } //Raza MASTERCARD

    public void setAuthAgentInstId(String authagentInstId) { //Raza MASTERCARD
        this.authAgentInstId = authagentInstId;
    }

	public String getAddDataNational() {
        return addDataNational;
    }

    public void setAddDataNational(String addDataNational) {
        this.addDataNational = addDataNational;
    }

    public String getTimeLocalTran() {
        return timeLocalTran;
    }

    public void setTimeLocalTran(String timeloctran) {
        this.timeLocalTran = timeloctran;
    }

    //public String getConvRateSettlement() {
        //return convRateSettlement;
    //}

    //public void setConvRateSettlement(String convRateSettlement) {
        //this.convRateSettlement = convRateSettlement;
    //}

    public String getMerchCountryCode() {
        return merchCountryCode;
    }

    public String getPanCountryCode() {
        return panCountryCode;
    }

    public void setMerchCountryCode(String merchCountryCode) {
        this.merchCountryCode = merchCountryCode;
    }

    public void setPanCountryCode(String panCountryCode) {
        this.panCountryCode = panCountryCode;
    }

    //public String getCurrCodeSettlement() {
      //  return currCodeSettlement;
    //}

    //public void setCurrCodeSettlement(String currCodeSettlement) {
     //   this.currCodeSettlement = currCodeSettlement;
    //}

    //public Long getAmountSettlement() {
        //return amountSettlement;
    //}

   // public void setAmountSettlement(Long amountSettlement) {
       // this.amountSettlement = amountSettlement;
    //}

    public String getNetworkData() {
        return NetworkData;
    }

    public void setNetworkData(String networkdata) {
        this.NetworkData = networkdata;
    }

    public String getOtherAmounts() {
        return otheramounts;
    }

    public void setOtherAmounts(String other_amounts) {
        this.otheramounts = other_amounts;
    }

    public String getTrack1Data() {
        if (eMVRqData == null)
            return null;
        if (eMVRqData.getCardAcctId() == null)
            return null;
        return getEMVRqData().getCardAcctId().getTrack1Data();
    }

    public void setTrack1Data(String track1Data) {
        if (Util.hasText(track1Data))
            getSafeEMVRqData().getSafeCardAcctId().setTrack1Data(track1Data);
    }

    public String getTrack3Data() {
        if (eMVRqData == null)
            return null;
        if (eMVRqData.getCardAcctId() == null)
            return null;
        return getEMVRqData().getCardAcctId().getTrack3Data();
    }

    public void setTrack3Data(String track3Data) {
        if (Util.hasText(track3Data))
            getSafeEMVRqData().getSafeCardAcctId().setTrack3Data(track3Data);
    }

    public String getAccountId1() {
        return accountId1;
    }

    public void setAccountId1(String accountId1) {
        this.accountId1 = accountId1;
    }

    public String getAccountId2() {
        return accountId2;
    }

    public void setAccountId2(String accountId2) {
        this.accountId1 = accountId2;
    }

    public String getRecordData() {
        return recordData;
    }

    public void setRecordData(String recordData) {
        this.recordData = recordData;
    }
	//Raza Adding for MTI start
    public static String fillMTI(IfxType ifxType, String firstMTI){
        System.out.println("FillMti Called...! firstMti [" + firstMTI + "], IFX-Type [" + ifxType + "]"); //Raza TEMP
        String mti = "0";

        if (ifxType.equals(IfxType.BAL_INQ_RQ)
                || ifxType.equals(IfxType.BILL_PMT_RQ)
                || ifxType.equals(IfxType.PURCHASE_RQ)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_RQ)
                || ifxType.equals(IfxType.PURCHASE_TOPUP_RQ)
                || ifxType.equals(IfxType.LAST_PURCHASE_CHARGE_RQ)
                || ifxType.equals(IfxType.WITHDRAWAL_RQ)
                || ifxType.equals(IfxType.RETURN_RQ)
                || ifxType.equals(IfxType.TRANSFER_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_RQ)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.DEPOSIT_RQ)
                || ifxType.equals(IfxType.DEPOSIT_CHECK_ACCOUNT_RQ)
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_RQ)
                || ifxType.equals(IfxType.GET_ACCOUNT_RQ)
                || ifxType.equals(IfxType.BANK_STATEMENT_RQ)
                || ifxType.equals(IfxType.CREDIT_PURCHASE_RQ)
                || ifxType.equals(IfxType.CREDIT_BAL_INQ_RQ)
                || ifxType.equals(IfxType.SADERAT_BILL_PMT_RQ)
                || ifxType.equals(IfxType.SORUSH_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.SHAPARAK_CONFIRM_RQ)
                || ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_RQ)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_RQ)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_RQ)
                || ifxType.equals(IfxType.PURCHASE_CANCEL_RQ)
                ){
            if ("400".equals(firstMTI))
                mti = ISOMessageTypes.REVERSAL_ADVICE_87;
            else if ("220".equals(firstMTI))
                mti = ISOMessageTypes.FINANCIAL_ADVICE_87;
            else
                mti = ISOMessageTypes.FINANCIAL_REQUEST_87;
        }else if (ifxType.equals(IfxType.BAL_INQ_RS)
                || ifxType.equals(IfxType.BILL_PMT_RS)
                || ifxType.equals(IfxType.PURCHASE_RS)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_RS)
                || ifxType.equals(IfxType.LAST_PURCHASE_CHARGE_RS)
                || ifxType.equals(IfxType.PURCHASE_TOPUP_RS)
                || ifxType.equals(IfxType.WITHDRAWAL_RS)
                || ifxType.equals(IfxType.RETURN_RS)
                || ifxType.equals(IfxType.TRANSFER_RS)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_RS)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS)
                || ifxType.equals(IfxType.DEPOSIT_RS)
                || ifxType.equals(IfxType.DEPOSIT_CHECK_ACCOUNT_RS)
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_RS)
                || ifxType.equals(IfxType.GET_ACCOUNT_RS)
                || ifxType.equals(IfxType.BANK_STATEMENT_RS)
                || ifxType.equals(IfxType.CREDIT_PURCHASE_RS)
                || ifxType.equals(IfxType.CREDIT_BAL_INQ_RS)
                || ifxType.equals(IfxType.SADERAT_BILL_PMT_RS)
                || ifxType.equals(IfxType.SORUSH_REV_REPEAT_RS)
                || ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_RS)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_RS)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_RS)
                || ifxType.equals(IfxType.PURCHASE_CANCEL_RS)
                ){
            if ("400".equals(firstMTI))
                mti = ISOMessageTypes.REVERSAL_RESPONSE_87;

            else if ("220".equals(firstMTI))
                mti = ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87;
            else
                mti = ISOMessageTypes.FINANCIAL_RESPONSE_87;
        }else if (ifxType.equals(IfxType.BAL_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.BILL_PMT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PURCHASE_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.WITHDRAWAL_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.RETURN_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.DEPOSIT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.GET_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.BANK_STATEMENT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.CREDIT_PURCHASE_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PURCHASE_CANCEL_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PREAUTH_REV_REPEAT_RQ)
                )
            if ("400".equals(firstMTI))
                mti = ISOMessageTypes.REVERSAL_ADVICE_87;
            else
                mti = ISOMessageTypes.REVERSAL_ADVICE_REPEAT_87;

        else if (ifxType.equals(IfxType.BAL_REV_REPEAT_RS)
                || ifxType.equals(IfxType.BILL_PMT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PURCHASE_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_REV_REPEAT_RS)
                || ifxType.equals(IfxType.WITHDRAWAL_REV_REPEAT_RS)
                || ifxType.equals(IfxType.RETURN_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.DEPOSIT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS)
                || ifxType.equals(IfxType.GET_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.BANK_STATEMENT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.CREDIT_PURCHASE_REV_REPEAT_RS)
                || ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PREAUTH_CANCEL_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PURCHASE_CANCEL_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PREAUTH_REV_REPEAT_RS)
                )
            if ("400".equals(firstMTI))
                mti = ISOMessageTypes.REVERSAL_RESPONSE_87;
            else
                mti = ISOMessageTypes.REVERSAL_ADVICE_RESPONSE_87;

        else if (ifxType.equals(IfxType.RECONCILIATION_RQ)|| ifxType.equals(IfxType.ACQUIRER_REC_RQ))
            mti = ISOMessageTypes.ACQUIRER_RECON_REQUEST_87;

        else if (ifxType.equals(IfxType.RECONCILIATION_REPEAT_RQ) || ifxType.equals(IfxType.ACQUIRER_REC_REPEAT_RQ))
            mti = ISOMessageTypes.ACQUIRER_RECON_ADVICE_87;
        else if (ifxType.equals(IfxType.RECONCILIATION_RS) || ifxType.equals(IfxType.ACQUIRER_REC_RS))
            mti = ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87;
        else if (ifxType.equals(IfxType.CARD_ISSUER_REC_RQ))
            mti = ISOMessageTypes.ISSUER_RECON_REQUEST_87;
        else if (ifxType.equals(IfxType.CARD_ISSUER_REC_REPEAT_RQ))
            mti = ISOMessageTypes.ISSUER_RECON_ADVICE_87;
        else if (ifxType.equals(IfxType.CUTOVER_RQ))
            mti = ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87;
        else if (ifxType.equals(IfxType.CUTOVER_REPEAT_RQ))
            mti = ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87;

        else if (ifxType.equals(IfxType.TRANSFER_CHECK_ACCOUNT_RQ) ||
                IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RQ.equals(ifxType) ||
                ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ) ||
                ifxType.equals(IfxType.PREAUTH_RQ) ||
                ifxType.equals(IfxType.PREAUTH_CANCEL_RQ)
                )
            mti = ISOMessageTypes.AUTHORIZATION_REQUEST_87;
        else if (ifxType.equals(IfxType.TRANSFER_CHECK_ACCOUNT_RS) ||
                IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RS.equals(ifxType) ||
                ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS) ||
                ifxType.equals(IfxType.PREAUTH_RS) ||
                ifxType.equals(IfxType.PREAUTH_CANCEL_RS)
                )
            mti = ISOMessageTypes.AUTHORIZATION_RESPONSE_87;
        else if(ifxType.equals(IfxType.REFUND_ADVICE_RQ) ||
                ifxType.equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ))
        {
            mti = ISOMessageTypes.FINANCIAL_ADVICE_87;
        }
        else if(ifxType.equals(IfxType.REFUND_ADVICE_RS) ||
                ifxType.equals(IfxType.PREAUTH_COMPLET_ADVICE_RS))
        {
            mti = ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87;
        }

        return mti;
    }
    //Raza Adding for MTI end

    /* Added by : Asim Shahzad, Date 9th Nov 2016, Desc : Adding getter and setter for new ISO field <start> */

    public String getSecRelatedControlInfo() {
        return secRelatedControlInfo;
    }

    public void setSecRelatedControlInfo(String secRelatedControlInfo) {
        this.secRelatedControlInfo = secRelatedControlInfo;
    }

    /*
    public String getBitmap_62()
    {
        return this.Bitmap_62;
    }

    public void setBitmap_62(String bitmap_62)
    {
        this.Bitmap_62 = bitmap_62;
    }

    public String getBitmap_63()
    {
        return this.Bitmap_63;
    }

    public void setBitmap_63(String bitmap_63)
    {
        this.Bitmap_63 = bitmap_63;
    }
    */

    /* Adding new ISO field <end> */

    //public String getOtherAmounts() {
    //    return otherAmounts;
    //}

    //public void setOtherAmounts(String otherAmounts) {
    //    this.otherAmounts = otherAmounts;
    //}

    public String getCustomPaymentService() {
        return customPaymentService;
    }

    public void setCustomPaymentService(String customPaymentService) {
        this.customPaymentService = customPaymentService;
    }

    public String getSchemePrivateUse() {
        return schemePrivateUse;
    }

    public void setSchemePrivateUse(String schemePrivateUse) {
        this.schemePrivateUse = schemePrivateUse;
    }

    //m.rehman: for card authorization flags
    public void setCardAuthFlags(CMSCardAuthorizationFlags cardAuthFlags) {
        if (cardAuthFlags != null)
            getSafeEMVRqData().setCardAuthorizationFlags(cardAuthFlags);
    }

    public CMSCardAuthorizationFlags getCardAuthFlags() {
        if (eMVRqData == null)
            return null;
        if (eMVRqData.getCardAuthorizationFlags() == null)
            return null;
        return getEMVRqData().getCardAuthorizationFlags();
    }

	public CMSCardRelation getCmsCardRelation() {
        return cmsCardRelation;
    }

    public void setCmsCardRelation(CMSCardRelation cmsCardRelation) {
        this.cmsCardRelation = cmsCardRelation;
    }

    //m.rehman: for card authorization flags
    public void setCardLimit(CMSCardLimit cardLimit) {
        if (cardLimit != null)
            getSafeEMVRqData().setCardLimit(cardLimit);
    }

    public CMSCardLimit getCardLimit() {
        if (eMVRqData == null)
            return null;
        if (eMVRqData.getCardLimit() == null)
            return null;
        return getEMVRqData().getCardLimit();
    }

    //m.rehman: for wallet operations
    public WalletCardRelation getWalletCardRelation() {
        return walletCardRelation;
    }

    public void setWalletCardRelation(WalletCardRelation walletCardRelation) {
        this.walletCardRelation = walletCardRelation;
    }

}
