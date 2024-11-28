package vaulsys.transaction.base;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Raza on 19-Jul-17.
 */
@Entity
@Table(name = "POS_BATCH_TXN_LOG")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class POSBatchTransactionLog implements IEntity<Long>, Cloneable{

    private transient Logger logger = Logger.getLogger(POSBatchTransactionLog.class);

    @Id
    @GeneratedValue(generator="pos-batch-trx-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "pos-batch-trx-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "pos_batch_trx_seq")
            })
    private Long id;

    private String MsgType; //Message Type

    private String Pan; //DE-2

    private String proc_code; //DE-3

    private String amt_tran; //DE-4

    private String amt_sett; //DE-5

    private String amt_cbill; //DE-6

    private String trans_DateTime; //DE-7

    private String amt_cbill_fee; //DE-8

    private String conv_rateSett; //DE-9

    private String conv_rateCbill; //DE-10

    private String SysTrcAudtNo; //DE-11

    private String time_locTran; //DE-12

    private String date_locTran; //DE-13

    private String exp_date; //DE-14

    private String date_sett; //DE-15

    private String date_conv; //DE-16

    private String date_capt; //DE-17

    private String merchant_type; //DE-18

    private String Acq_InsCountryCode; //DE-19

    private String PanExtCountryCode; //DE-20

    private String FwdInstCountryCode; //DE-21

    private String PosEntMode; //DE-22

    private String appPanSeqNo; //DE-23

    private String netInternId; //DE-24

    private String PosCondCode; //DE-25

    private String PosCaptCode; //DE-26

    private String AuthidResponseLength; //DE-27

    private String amt_tranFee; //DE-28

    private String amt_settFee; //DE-29

    private String amt_tranProcFee; //DE-30

    private String amt_settProcFee; //DE-31

    private String AcqInsIdCode; //DE-32

    private String FwdInsIdCode; //DE-33

    private String PanExt; //DE-34

    private String track2Data; //DE-35

    private String track3Data; //DE-36

    private String RetRefNo; //DE-37

    private String AuthIdResp; //DE-38

    private String RespCode; //DE-39

    private String ServiceRestrictCode; //DE-40

    private String CAccptTermId; //DE-41

    private String CAccptIdCode; //DE-42

    private String CAccptNameLoc; //DE-43

    private String AddRespData; //DE-44

    private String track1Data; //DE-45

    private String AddDataISO; //DE-46

    private String AddDataNat; //DE-47

    private String AddDataPrv; //DE-48

    private String CurrCodeTran; //DE-49

    private String CurrCodeSett; //DE-50

    private String CurrCodeCBill; //DE-51

    private String PinData; //DE-52

    private String SecurRelCntrlInfo; //DE-53

    private String add_amt; //DE-54

    private String ICC_Data; //DE-55

    private String selfDefineData; //m.rehman: DE-60

    private String otherAmounts; //m.rehman: DE-61

    private String customPaymentService; //m.rehman: DE-62

    private String NetworkData; //DE-56-63 NetworkData

    private String MACcode; //DE-64

    private String sett_code; //DE-66

    private String extPaymentCode; //DE-67

    private String RecInsCntryCode; //DE-68

    private String SettInsCntryCode; //DE-69

    private String NetInfoCode; //DE-70

    private String MessageNo; //DE-71

    private String MessageNoLast; //DE-72

    private String date_action; //DE-73

    private String credits_no; //DE-74

    private String credits_revno; //DE-75

    private String debits_no; //DE-76

    private String debits_revno; //DE-77

    private String transfer_no; //DE-78

    private String transfer_revno; //DE-79

    private String Inqno; //DE-80

    private String auth_no; //DE-81

    private String credits_procFeeAmt; //DE-82

    private String credits_tranFeeAmt; //DE-83

    private String debits_procFeeAmt; //DE-84

    private String debits_tranFeeAmt; //DE-85

    private String credits_amt; //DE-86

    private String credits_revamt; //DE-87

    private String debits_amt; //DE-88

    private String debits_revamt; //DE-89

    private String OrigDataElemnt; //DE-90

    private String file_updatecode; //DE-91

    private String file_seccode; //DE-92

    private String resp_indicator; //DE-93

    private String service_indicator; //DE-94

    private String replcmnt_amt; //DE-95

    private String msg_seccode; //DE-96

    private String netsett_amt; //DE-97

    private String payee; //DE-98

    private String sett_InsIdCode; //DE-99

    private String rec_InsIdCode; //DE-100

    private String filename; //DE-101

    private String Acc_id_1; //DE-102

    private String Acc_id_2; //DE-103

    private String tran_desc; //DE-104

    //DE-105-127 Reserved

    private String MsgAuthCode; //DE-128

    private String origChannel;

    private String destChannel;

    private String TerminalId; //Raza adding for ATM & POS Terminal Ids

    private TerminalType terminalType = TerminalType.UNKNOWN; //Raza adding for Terminal Type (refer Class for Details)

    //m.rehman: for ifx trntype
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "trntype"))
    })
    private TrnType trnType = TrnType.UNKNOWN;

    @Embedded
    private AccType AccTypeFrom = AccType.UNKNOWN; //Raza adding to support AccTypeFrom EMVRqData

    @Embedded
    private AccType AccTypeTo = AccType.UNKNOWN; //Raza adding to support AccTypeFrom EMVRqData


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPan() {
        return Pan;
    }

    public void setPan(String pan) {
        Pan = pan;
    }

    public String getProc_code() {
        return proc_code;
    }

    public void setProc_code(String proc_code) {
        this.proc_code = proc_code;
    }

    public void setTran_code(String tran_code)
    {
        this.proc_code = tran_code + proc_code.substring(2,proc_code.length()-2) ;
    }

    public String getTran_code()
    {
        return proc_code.substring(0,2);
    }

    public String getAmt_tran() {
        return amt_tran;
    }

    public void setAmt_tran(String amt_tran) {
        this.amt_tran = amt_tran;
    }

    public String getAmt_sett() {
        return amt_sett;
    }

    public void setAmt_sett(String amt_sett) {
        this.amt_sett = amt_sett;
    }

    public String getAmt_cbill() {
        return amt_cbill;
    }

    public void setAmt_cbill(String amt_cbill) {
        this.amt_cbill = amt_cbill;
    }

    public String getTrans_DateTime() {
        return trans_DateTime;
    }

    public void setTrans_DateTime(String tran_DateTime) {
        this.trans_DateTime = tran_DateTime;
    }

    public String getAmt_cbill_fee() {
        return amt_cbill_fee;
    }

    public void setAmt_cbill_fee(String amt_cbill_fee) {
        this.amt_cbill_fee = amt_cbill_fee;
    }

    public String getConv_rateSett() {
        return conv_rateSett;
    }

    public void setConv_rateSett(String conv_rateSett) {
        this.conv_rateSett = conv_rateSett;
    }

    public String getConv_rateCbill() {
        return conv_rateCbill;
    }

    public void setConv_rateCbill(String conv_rateCbill) {
        this.conv_rateCbill = conv_rateCbill;
    }

    public String getSysTrcAudtNo() {
        return SysTrcAudtNo;
    }

    public void setSysTrcAudtNo(String STAN) {
        this.SysTrcAudtNo = STAN;
    }

    public String getTime_locTran() {
        return time_locTran;
    }

    public void setTime_locTran(String time_locTran) {
        this.time_locTran = time_locTran;
    }

    public String getDate_locTran() {
        return date_locTran;
    }

    public void setDate_locTran(String date_locTran) {
        this.date_locTran = date_locTran;
    }

    public String getExp_date() {
        return exp_date;
    }

    public void setExp_date(String exp_date) {
        this.exp_date = exp_date;
    }

    public String getDate_sett() {
        return date_sett;
    }

    public void setDate_sett(String date_sett) {
        this.date_sett = date_sett;
    }

    public String getDate_conv() {
        return date_conv;
    }

    public void setDate_conv(String date_conv) {
        this.date_conv = date_conv;
    }

    public String getDate_capt() {
        return date_capt;
    }

    public void setDate_capt(String date_capt) {
        this.date_capt = date_capt;
    }

    public String getMerchant_type() {
        return merchant_type;
    }

    public void setMerchant_type(String merchant_type) {
        this.merchant_type = merchant_type;
    }

    public String getAcq_InsCountryCode() {
        return Acq_InsCountryCode;
    }

    public void setAcq_InsCountryCode(String acq_InsCountryCode) {
        Acq_InsCountryCode = acq_InsCountryCode;
    }

    public String getPanExtCountryCode() {
        return PanExtCountryCode;
    }

    public void setPanExtCountryCode(String panExtCountryCode) {
        PanExtCountryCode = panExtCountryCode;
    }

    public String getFwdInstCountryCode() {
        return FwdInstCountryCode;
    }

    public void setFwdInstCountryCode(String fwdInstCountryCode) {
        FwdInstCountryCode = fwdInstCountryCode;
    }

    public String getPosEntMode() {
        return PosEntMode;
    }

    public void setPosEntMode(String posEntMode) {
        PosEntMode = posEntMode;
    }

    public String getAppPanSeqNo() {
        return appPanSeqNo;
    }

    public void setAppPanSeqNo(String appPanSeqNo) {
        this.appPanSeqNo = appPanSeqNo;
    }

    public String getNetInternId() {
        return netInternId;
    }

    public void setNetInternId(String netInternId) {
        this.netInternId = netInternId;
    }

    public String getPosCondCode() {
        return PosCondCode;
    }

    public void setPosCondCode(String posCondCode) {
        PosCondCode = posCondCode;
    }

    public String getPosCaptCode() {
        return PosCaptCode;
    }

    public void setPosCaptCode(String posCaptCode) {
        PosCaptCode = posCaptCode;
    }

    public String getAuthidResponseLength() {
        return AuthidResponseLength;
    }

    public void setAuthidResponseLength(String authidResponseLength) {
        AuthidResponseLength = authidResponseLength;
    }

    public String getAmt_tranFee() {
        return amt_tranFee;
    }

    public void setAmt_tranFee(String amt_tranFee) {
        this.amt_tranFee = amt_tranFee;
    }

    public String getAmt_settFee() {
        return amt_settFee;
    }

    public void setAmt_settFee(String amt_settFee) {
        this.amt_settFee = amt_settFee;
    }

    public String getAmt_tranProcFee() {
        return amt_tranProcFee;
    }

    public void setAmt_tranProcFee(String amt_tranProcFee) {
        this.amt_tranProcFee = amt_tranProcFee;
    }

    public String getAmt_settProcFee() {
        return amt_settProcFee;
    }

    public void setAmt_settProcFee(String amt_settProcFee) {
        this.amt_settProcFee = amt_settProcFee;
    }

    public String getAcqInsIdCode() {
        return AcqInsIdCode;
    }

    public void setAcqInsIdCode(String acqInsIdCode) {
        AcqInsIdCode = acqInsIdCode;
    }

    public String getFwdInsIdCode() {
        return FwdInsIdCode;
    }

    public void setFwdInsIdCode(String fwdInsIdCode) {
        FwdInsIdCode = fwdInsIdCode;
    }

    public String getPanExt() {
        return PanExt;
    }

    public void setPanExt(String panExt) {
        PanExt = panExt;
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

    public String getRetRefNo() {
        return RetRefNo;
    }

    public void setRetRefNo(String RetRefno) {
        this.RetRefNo = RetRefno;
    }

    public String getAuthIdResp() {
        return AuthIdResp;
    }

    public void setAuthIdResp(String authIdResp) {
        AuthIdResp = authIdResp;
    }

    public String getRespCode() {
        return RespCode;
    }

    public void setRespCode(String respCode) {
        RespCode = respCode;
    }

    public String getServiceRestrictCode() {
        return ServiceRestrictCode;
    }

    public void setServiceRestrictCode(String serviceRestrictCode) {
        ServiceRestrictCode = serviceRestrictCode;
    }

    public String getCAccptTermId() {
        return CAccptTermId;
    }

    public void setCAccptTermId(String CAccptTermId) {
        this.CAccptTermId = CAccptTermId;
    }

    public String getCAccptIdCode() {
        return CAccptIdCode;
    }

    public void setCAccptIdCode(String CAccptIdCode) {
        this.CAccptIdCode = CAccptIdCode;
    }

    public String getCAccptNameLoc() {
        return CAccptNameLoc;
    }

    public void setCAccptNameLoc(String CAccptNameLoc) {
        this.CAccptNameLoc = CAccptNameLoc;
    }

    public String getAddRespData() {
        return AddRespData;
    }

    public void setAddRespData(String addRespData) {
        AddRespData = addRespData;
    }

    public String getTrack1Data() {
        return track1Data;
    }

    public void setTrack1Data(String track1Data) {
        this.track1Data = track1Data;
    }

    public String getAddDataISO() {
        return AddDataISO;
    }

    public void setAddDataISO(String addDataISO) {
        AddDataISO = addDataISO;
    }

    public String getAddDataNat() {
        return AddDataNat;
    }

    public void setAddDataNat(String addDataNat) {
        AddDataNat = addDataNat;
    }

    public String getAddDataPrv() {
        return AddDataPrv;
    }

    public void setAddDataPrv(String addDataPrv) {
        AddDataPrv = addDataPrv;
    }

    public String getCurrCodeTran() {
        return CurrCodeTran;
    }

    public void setCurrCodeTran(String currCodeTran) {
        CurrCodeTran = currCodeTran;
    }

    public String getCurrCodeSett() {
        return CurrCodeSett;
    }

    public void setCurrCodeSett(String currCodeSett) {
        CurrCodeSett = currCodeSett;
    }

    public String getCurrCodeCBill() {
        return CurrCodeCBill;
    }

    public void setCurrCodeCBill(String currCodeCBill) {
        CurrCodeCBill = currCodeCBill;
    }

    public String getPinData() {
        return PinData;
    }

    public void setPinData(String pinData) {
        PinData = pinData;
    }

    public String getSecurRelCntrlInfo() {
        return SecurRelCntrlInfo;
    }

    public void setSecurRelCntrlInfo(String securRelCntrlInfo) {
        SecurRelCntrlInfo = securRelCntrlInfo;
    }

    public String getAdd_amt() {
        return add_amt;
    }

    public void setAdd_amt(String add_amt) {
        this.add_amt = add_amt;
    }

    public String getICC_Data() {
        return ICC_Data;
    }

    public void setICC_Data(String ICC_Data) {
        this.ICC_Data = ICC_Data;
    }

    public String getMACcode() {
        return MACcode;
    }

    public void setMACcode(String MACcode) {
        this.MACcode = MACcode;
    }

    public String getSett_code() {
        return sett_code;
    }

    public void setSett_code(String sett_code) {
        this.sett_code = sett_code;
    }

    public String getExtPaymentCode() {
        return extPaymentCode;
    }

    public void setExtPaymentCode(String extPaymentCode) {
        this.extPaymentCode = extPaymentCode;
    }

    public String getRecInsCntryCode() {
        return RecInsCntryCode;
    }

    public void setRecInsCntryCode(String recInsCntryCode) {
        RecInsCntryCode = recInsCntryCode;
    }

    public String getSettInsCntryCode() {
        return SettInsCntryCode;
    }

    public void setSettInsCntryCode(String settInsCntryCode) {
        SettInsCntryCode = settInsCntryCode;
    }

    public String getNetInfoCode() {
        return NetInfoCode;
    }

    public void setNetInfoCode(String netInfoCode) {
        NetInfoCode = netInfoCode;
    }

    public String getMessageNo() {
        return MessageNo;
    }

    public void setMessageNo(String messageNo) {
        MessageNo = messageNo;
    }

    public String getMessageNoLast() {
        return MessageNoLast;
    }

    public void setMessageNoLast(String messageNoLast) {
        MessageNoLast = messageNoLast;
    }

    public String getDate_action() {
        return date_action;
    }

    public void setDate_action(String date_action) {
        this.date_action = date_action;
    }

    public String getCredits_no() {
        return credits_no;
    }

    public void setCredits_no(String credits_no) {
        this.credits_no = credits_no;
    }

    public String getCredits_revno() {
        return credits_revno;
    }

    public void setCredits_revno(String credits_revno) {
        this.credits_revno = credits_revno;
    }

    public String getDebits_no() {
        return debits_no;
    }

    public void setDebits_no(String debits_no) {
        this.debits_no = debits_no;
    }

    public String getDebits_revno() {
        return debits_revno;
    }

    public void setDebits_revno(String debits_revno) {
        this.debits_revno = debits_revno;
    }

    public String getTransfer_no() {
        return transfer_no;
    }

    public void setTransfer_no(String transfer_no) {
        this.transfer_no = transfer_no;
    }

    public String getTransfer_revno() {
        return transfer_revno;
    }

    public void setTransfer_revno(String transfer_revno) {
        this.transfer_revno = transfer_revno;
    }

    public String getInqno() {
        return Inqno;
    }

    public void setInqno(String inqno) {
        Inqno = inqno;
    }

    public String getAuth_no() {
        return auth_no;
    }

    public void setAuth_no(String auth_no) {
        this.auth_no = auth_no;
    }

    public String getCredits_procFeeAmt() {
        return credits_procFeeAmt;
    }

    public void setCredits_procFeeAmt(String credits_procFeeAmt) {
        this.credits_procFeeAmt = credits_procFeeAmt;
    }

    public String getCredits_tranFeeAmt() {
        return credits_tranFeeAmt;
    }

    public void setCredits_tranFeeAmt(String credits_tranFeeAmt) {
        this.credits_tranFeeAmt = credits_tranFeeAmt;
    }

    public String getDebits_procFeeAmt() {
        return debits_procFeeAmt;
    }

    public void setDebits_procFeeAmt(String debits_procFeeAmt) {
        this.debits_procFeeAmt = debits_procFeeAmt;
    }

    public String getDebits_tranFeeAmt() {
        return debits_tranFeeAmt;
    }

    public void setDebits_tranFeeAmt(String debits_tranFeeAmt) {
        this.debits_tranFeeAmt = debits_tranFeeAmt;
    }

    public String getCredits_amt() {
        return credits_amt;
    }

    public void setCredits_amt(String credits_amt) {
        this.credits_amt = credits_amt;
    }

    public String getCredits_revamt() {
        return credits_revamt;
    }

    public void setCredits_revamt(String credits_revamt) {
        this.credits_revamt = credits_revamt;
    }

    public String getDebits_amt() {
        return debits_amt;
    }

    public void setDebits_amt(String debits_amt) {
        this.debits_amt = debits_amt;
    }

    public String getDebits_revamt() {
        return debits_revamt;
    }

    public void setDebits_revamt(String debits_revamt) {
        this.debits_revamt = debits_revamt;
    }

    public String getOrigDataElemnt() {
        return OrigDataElemnt;
    }

    public void setOrigDataElemnt(String ODE) {
        this.OrigDataElemnt = ODE;
    }

    public String getFile_updatecode() {
        return file_updatecode;
    }

    public void setFile_updatecode(String file_updatecode) {
        this.file_updatecode = file_updatecode;
    }

    public String getFile_seccode() {
        return file_seccode;
    }

    public void setFile_seccode(String file_seccode) {
        this.file_seccode = file_seccode;
    }

    public String getResp_indicator() {
        return resp_indicator;
    }

    public void setResp_indicator(String resp_indicator) {
        this.resp_indicator = resp_indicator;
    }

    public String getService_indicator() {
        return service_indicator;
    }

    public void setService_indicator(String service_indicator) {
        this.service_indicator = service_indicator;
    }

    public String getReplcmnt_amt() {
        return replcmnt_amt;
    }

    public void setReplcmnt_amt(String replcmnt_amt) {
        this.replcmnt_amt = replcmnt_amt;
    }

    public String getMsg_seccode() {
        return msg_seccode;
    }

    public void setMsg_seccode(String msg_seccode) {
        this.msg_seccode = msg_seccode;
    }

    public String getNetsett_amt() {
        return netsett_amt;
    }

    public void setNetsett_amt(String netsett_amt) {
        this.netsett_amt = netsett_amt;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getSett_InsIdCode() {
        return sett_InsIdCode;
    }

    public void setSett_InsIdCode(String sett_InsIdCode) {
        this.sett_InsIdCode = sett_InsIdCode;
    }

    public String getRec_InsIdCode() {
        return rec_InsIdCode;
    }

    public void setRec_InsIdCode(String rec_InsIdCode) {
        this.rec_InsIdCode = rec_InsIdCode;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAcc_id_1() {
        return Acc_id_1;
    }

    public void setAcc_id_1(String acc_id_1) {
        Acc_id_1 = acc_id_1;
    }

    public String getAcc_id_2() {
        return Acc_id_2;
    }

    public void setAcc_id_2(String acc_id_2) {
        Acc_id_2 = acc_id_2;
    }

    public String getTran_desc() {
        return tran_desc;
    }

    public void setTran_desc(String tran_desc) {
        this.tran_desc = tran_desc;
    }

    public String getMsgAuthCode() {
        return MsgAuthCode;
    }

    public void setMsgAuthCode(String msgAuthCode) {
        MsgAuthCode = msgAuthCode;
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setMsgType(String msgType) {
        MsgType = msgType;
    }

    public String getOrigChannel() {
        return origChannel;
    }

    public void setOrigChannel(String origChannel) {
        this.origChannel = origChannel;
    }

    public String getDestChannel() {
        return destChannel;
    }

    public void setDestChannel(String destChannel) {
        this.destChannel = destChannel;
    }

    public String getNetworkData() {
        return NetworkData;
    }

    public void setNetworkData(String networkData) {
        NetworkData = networkData;
    }

    public String getTerminalId() {
        return TerminalId;
    }

    public void setTerminalId(String terminalId) {
        TerminalId = terminalId;
    }

    public TerminalType getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(TerminalType terminalType) {
        this.terminalType = terminalType;
    }

    public TrnType getTrnType() {
        return trnType;
    }

    public void setTrnType(TrnType TrnType) {
        this.trnType = TrnType;
    }

    public AccType getAccTypeFrom() {
        return AccTypeFrom;
    }

    public void setAccTypeFrom(AccType accTypeFrom) {
        AccTypeFrom = accTypeFrom;
    }

    public AccType getAccTypeTo() {
        return AccTypeTo;
    }

    public void setAccTypeTo(AccType accTypeTo) {
        AccTypeTo = accTypeTo;
    }

    public String getSelfDefineData() {
        return selfDefineData;
    }

    public void setSelfDefineData(String selfDefineData) {
        this.selfDefineData = selfDefineData;
    }

    public String getOtherAmounts() {
        return otherAmounts;
    }

    public void setOtherAmounts(String otherAmounts) {
        this.otherAmounts = otherAmounts;
    }

    public String getCustomPaymentService() {
        return customPaymentService;
    }

    public void setCustomPaymentService(String customPaymentService) {
        this.customPaymentService = customPaymentService;
    }

    @SuppressWarnings("unchecked") //Raza adding to support NDCParserUtils.setTlogFields/setIfxFields
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

    public POSBatchTransactionLog copyFields(ProcessContext processContext) {
        Ifx ifx = processContext.getInputMessage().getIfx();

        this.setMsgType(ifx.getMti());
        this.setPan(ifx.getAppPAN());
        this.setTrnType(ifx.getTrnType());
        this.setAccTypeFrom(ifx.getAccTypeFrom());
        this.setAccTypeTo(ifx.getAccTypeTo());

        if (ifx.getAuth_Amt() != null)
            this.setAmt_tran(ifx.getAuth_Amt().toString());

        if (ifx.getSett_Amt() != null)
            this.setAmt_sett(ifx.getSett_Amt().toString());

        if (ifx.getSec_Amt() != null)
            this.setAmt_cbill(ifx.getSec_Amt().toString());

        if (ifx.getTrnDt() != null)
            this.setTrans_DateTime(Long.toString(ifx.getTrnDt().getDateTimeLong()));

        this.setConv_rateSett(ifx.getConvRate_Sett());
        this.setConv_rateCbill(ifx.getSec_CurRate());
        this.setSysTrcAudtNo(ifx.getSrc_TrnSeqCntr());
        this.setTime_locTran(ifx.getTimeLocalTran());
        this.setDate_locTran(ifx.getDateLocalTran());

        if (ifx.getExpDt() != null)
            this.setExp_date(ifx.getExpDt().toString());

        if (ifx.getSettleDt() != null)
            this.setDate_sett(ifx.getSettleDt().toString());

        if (ifx.getPostedDt() != null)
            this.setDate_capt(ifx.getPostedDt().toString());

        this.setMerchant_type(ifx.getMerchantType());
        this.setAcq_InsCountryCode(ifx.getMerchCountryCode());
        this.setPanExtCountryCode(ifx.getPanCountryCode());
        this.setPosEntMode(ifx.getPosEntryModeCode());
        this.setAppPanSeqNo(ifx.getCardSequenceNo());
        this.setNetInternId(ifx.getNetworkInstId());
        this.setPosCondCode(ifx.getPosConditionCode());
        this.setPosCaptCode(ifx.getPosPinCaptureCode());
        this.setAmt_tranFee(ifx.getAmountTranFee());
        this.setAcqInsIdCode(ifx.getBankId());
        this.setFwdInsIdCode(ifx.getFwdBankId());
        this.setTrack2Data(ifx.getTrk2EquivData());
        this.setTrack3Data(ifx.getTrack3Data());
        this.setRetRefNo(ifx.getNetworkRefId());
        this.setAuthIdResp(ifx.getActionCode());
        this.setProc_code(ifx.getApprovalCode());
        this.setTerminalId(ifx.getTerminalId());
        this.setCAccptTermId(ifx.getTerminalId());
        this.setTerminalType(ifx.getTerminalType());
        this.setCAccptIdCode(ifx.getOrgIdNum());
        this.setCAccptNameLoc(ifx.getCardAcceptNameLoc());
        this.setAddRespData(ifx.getAddResponseData());
        this.setTrack1Data(ifx.getTrack1Data());
        this.setAddDataNat(ifx.getAddDataNational());
        this.setAddDataPrv(ifx.getAddDataPrivate());

        if (ifx.getAuth_Currency() != null)
            this.setCurrCodeTran(ifx.getAuth_Currency().toString());

        this.setCurrCodeSett(ifx.getSett_Currency());
        this.setCurrCodeCBill(ifx.getSec_CurDate());
        this.setSecurRelCntrlInfo(ifx.getSecRelatedControlInfo());
        //this.setAdd_amt(ifx.getAddResponseData());
        this.setSelfDefineData(ifx.getSelfDefineData());
        this.setOtherAmounts(ifx.getOtherAmounts());
        this.setCustomPaymentService(ifx.getCustomPaymentService());
        this.setNetworkData(ifx.getNetworkData());
        this.setNetInfoCode(ifx.getNetworkManageInfoCode());
        this.setAcc_id_1(ifx.getAccountId1());
        this.setAcc_id_2(ifx.getAccountId2());
        //this.setOrigChannel(processContext.getInputMessage().getChannelName());
        //this.setDestChannel(processContext.getOutputMessage().getChannelName());

        return this;
    }
}
