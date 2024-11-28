package vaulsys.protocols.cmsnew;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.utils.CMSMapperUtil;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.Util;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import com.thoughtworks.xstream.XStream;
//import org.simpleframework.xml.Default;

//@Default
public class CMSMessage implements Cloneable, Serializable, ProtocolMessage{
    public Long expDt;
    public String cvv2;
    public Integer ifx;
    public Integer trn;
    public String PAN;
    public Long amt;
    public Long realAmt;
    public Integer cur;
//    public String curRate;
//    public Long secAmt;
//    public Integer secCurCode;
//    public String secCureRate;
    public String seqCntr;
    public String mySeqCntr;
    public String trk2;
    public Integer termType;
    public String bnk;
    public String fwdBnk;
    public String rcvBnk;
    public String orgName;
    public String netRef;
    public String term;
    public String pin;
    public String newPin;
    public String oldPin;
    public Date origDt;
    public Date trnDt;
    public String rsCode;
    public String docNum;
    public String origType;
    public String origSeqCntr;
    public Date origOrigDt;
    public String origBnk;
    public String origFwdBnk;
    public String newAmtAcqCur;
    public String newAmtIssCur;
    public String orgNum;
    public Long orgType;
    public Integer balAvbAcctType;
    public Integer balAvbBalType;
    public String balAvbAmt;
    public String balAvbCurCode;
    public Integer balLdgAcctType;
    public Integer balLdgBalType;
    public String balLdgAmt;
    public String balLdgCurCode;
    public String secPAN;
    public String name;
    public String family;
    public String mainAccNum;
    public String statement;
    public String subAccs;
    public Long ccardTrxAmt;
    public Long ccardFee;
    public Long ccardInt;
    public Long ccardStmtAmt;
    public Long ccardOTB;
	public Integer lang;
	public String subAccTo;
	public String subAccFr;
	public Long feeAmt;
	public String errMsg;
	public String actPAN;
	public String actSecPAN;
	public String stmtAccNum;
	public String billId;
	public String billPayId;
	public Integer billType;
	public String billTypeName;
	public String holderMobile;
	public Long trx;
    public Integer accTypeFr;
    public Integer accTypeTo;
    public String transferFromDesc;
    public String transferToDesc;
    public String shenaseOfTransferToAcc;//gholami(Task45875)
    /**
     * @author khodadi
     * baray trakonesh hay khas USSD    
     */
    public Integer origTerminalType;

	public String shebaCode;

    //TASK Task081 : ATM Saham Feature
    public String shareCode; //StockCode
    public Long shareCount; //StockCount	

    public transient String xml;
    private transient String logXML;

    //These two fields are specially designed for cms to manage it's threadpools!!!!
    public transient byte isReversal;
    public transient byte cardType;
    
    
    //Mirkamali(Task175): Restriction
    public Integer cardServiceTrnType;
    public Integer cardServiceTerminalType;
    public Integer cycleType;
    public String authIdResponse; //Raza MasterCard
    
//    public static final String IFX_TRN_TYPE = "trnType";
//    public static final String IFX_EMV_TRN_TYPE = "msgRqHdr_EMVRqData_EMVTrnType";
//    public static final String IFX_MY_TRN_SEQ_CNTR = "msgRqHdr_EMVRqData_My_TrnSeqCntr";
//    public static final String IFX_TRN_DT = "msgRqHdr_EMVRqData_TrnDt";
//    public static final String IFX_MSG_AUTH_CODE = "msgRqHdr_EMVRqData_MsgAuthCode";
//    public String city;
//    public String province;
//    public static final String IFX_COUNTRY = "msgRqHdr_NetworkTrnInfo_Country";
//    public static final String IFX_STATUS_CODE = "status_StatusCode";
//    public static final String IFX_STATUS_SEVERITY = "status_Severity";
//    public static final String IFX_STATUS_DESC = "status_StatusDesc";
//    public static final String IFX_FIELD_STATUS_ADDITIONALSTATUS_STATUS_CODE = "status_AdditionalStatus_StatusCode";
//    public static final String IFX_FIELD_STATUS_ADDITIONALSTATUS_SEVERITY = "status_AdditionalStatus_Severity";
//    public static final String IFX_FIELD_STATUS_ADDITIONALSTATUS_STATUS_DESC = "status_AdditionalStatus_StatusDesc";
//    public static final String IFX_POSTED_DT = "postedDt";
//    public static final String IFX_SETTLE_DT = "settleDt";
//    public static final String IFX_APPROVAL_CODE = "approvalCode";
//    public static final String IFX_RECIEVED_DT = "recievedDt";
//    public static final String IFX_CORE_BRANCH_CODE = "atmSpecificData_coreBranchCode";
//    public static final String IFX_P48 = "ExtISO_P48";  
//    public static final String IFX_RECV_BANK_ID = "msgRqHdr_NetworkTrnInfo_RecvBankId";


    private static final DecimalFormat df = new DecimalFormat("#");
    private static final String STR_NEW_LINE = "\r\n";
    private static final String STR_MSG_START = "<cmsmsg>";
    private static final String STR_MSG_END = "</cmsmsg>";
    private static final String STR_PAN = "PAN = ";
    private static final String STR_IFX = "ifx = ";
    private static final String STR_AMT = "amt = ";
    private static final String STR_R_AMT = "realAmt = ";
    private static final String STR_CUR = "cur = ";
    private static final String STR_SEQ_CNTR = "seqCntr = ";
    private static final String STR_M_SEQ_CNTR = "mySeqCntr = ";
    private static final String STR_TERM_TYPE = "termType = ";
    private static final String STR_BNK = "bnk = ";
    private static final String STR_FW_BNK = "fwdBnk = ";
    private static final String STR_REC_BNK = "rcvBnk = ";
    private static final String STR_ORG = "orgName = ";
    private static final String STR_NET_REF = "netRef = ";
    private static final String STR_TERM = "term = ";
    private static final String STR_ORIGDT = "origDt = ";
    private static final String STR_RSCODE = "rsCode = ";
    private static final String STR_DOCNUM = "docNum = ";
    private static final String STR_ORIG_TYPE = "origType = ";
    private static final String STR_ORIG_TRSQ = "origSeqCntr = ";
    private static final String STR_ORIG_ORDT = "origOrigDt = ";
    private static final String STR_ORIG_BNK = "origBnk = ";
    private static final String STR_ORIG_FW_BNK = "origFwdBnk = ";
    private static final String STR_NEW_AMT_ACQ = "newAmtAcqCur = ";
    private static final String STR_NEW_AMT_ISS = "newAmtIssCur = ";
    private static final String STR_ORG_NUM = "orgNum = ";
    private static final String STR_ORG_TYP = "orgType = ";
    private static final String STR_BAL_AV_ACC_TYP = "balAvbAcctType = ";
    private static final String STR_BAL_AV_BAL_TYP = "balAvbBalType = ";
    private static final String STR_BAL_AV_AMT = "balAvbAmt = ";
    private static final String STR_BAL_AV_CUR = "balAvbCurCode = ";
    private static final String STR_BAL_LG_ACC_TYP = "balLdgAcctType = ";
    private static final String STR_BAL_LG_BAL_TYP = "balLdgBalType = ";
    private static final String STR_BAL_LG_AMT = "balLdgAmt = ";
    private static final String STR_BAL_LG_CUR = "balLdgCurCode = ";
    private static final String STR_SEC_PAN = "secPAN = ";
    private static final String STR_NAME = "name = ";
    private static final String STR_FAMILY = "family = ";
    private static final String STR_MAIN_ACC = "mainAccNum = ";
    private static final String STR_STAT = "statement = ";
    private static final String STR_SUB_ACCS = "subAccs = ";
    private static final String STR_CCRD_TRX = "ccardTrxAmt = ";
    private static final String STR_CCRD_FEE = "ccardFee = ";
    private static final String STR_CCRD_INT = "ccardInt = ";
    private static final String STR_CCRD_STM = "ccardStmtAmt = ";
    private static final String STR_CCRD_OTB = "ccardOTB = ";
    private static final String STR_CCRD_LNG = "lang = ";
    private static final String STR_SUB_ACC_TO = "subAccTo = ";
    private static final String STR_SUB_ACC_FR = "subAccFr = ";
    private static final String STR_FEE_AMT = "feeAmt = ";
    private static final String STR_ERR_MSG = "errMsg = ";
    private static final String STR_ACT_PAN = "actPAN = ";
    private static final String STR_ACT_SEC_PAN = "actSecPAN = ";
    private static final String STR_ST_ACC_NUM = "stmtAccNum = ";
    private static final String STR_BILL_ID = "billId = ";
    private static final String STR_BILL_PAY_ID = "billPayId = ";
    private static final String STR_BILL_TYPE = "billType = ";
    private static final String STR_BILL_TYPE_NAME = "billTypeName = ";
    private static final String STR_HOLDER_MOB = "holderMobile = ";
    private static final String STR_TRX = "trx = ";
    private static final String STR_ACC_TYP_FR = "accTypeFr = ";
    private static final String STR_ACC_TYP_TO = "accTypeTo = ";
    private static final String STR_TRN = "trn = ";
    private static final String STR_TRN_DT = "trnDt = ";
    private static final String STR_TRF_DESC = "trfDesc = ";
    private static final String STR_TRT_DESC = "trtDesc = ";
    private static final String STR_ORIG_TRM_TYP = "orgTrmTyp = ";
    private static final String STR_SHENASE_TRN = "shenaseOfTransferToAcc = ";
    
    //TASK Task081 : ATM Saham Feature
    private static final String STR_SHARE_CODE = "shareCode";
    private static final String STR_SHARE_COUNT = "shareCount";
    
    //Mirkamali(Task175): Restriction
    private static final String STR_RESTRICTION_CYCLETYPE = "restrictionCycleType = ";
    private static final String STR_RESTRICTION_TRNTYPE = "restrictionTrnType = ";
    private static final String STR_RESTRICTION_TERMTYPE = "restrictionTermType = ";

    private static final String STR_AUTH_ID_RESPONSE = "authIdResponse"; //Raza MasterCard
    @Override
	public Boolean isRequest() throws Exception {
    	return ISOFinalMessageType.isRequestMessage(CMSMapperUtil.ToIfxType.get(ifx));
	}

//    @Override
//    public String toString(){
//    	if(Util.hasText(this.xml))
//    		return this.xml;
//		XStream xStream = new XStream();
//    	this.xml = xStream.toXML(this).replaceAll("vaulsys.protocols.cmsnew.CMSMessage", "msg");
//    	return this.xml;
//    }

    @Override
    public String toString() {
        if (Util.hasText(this.logXML))
            return this.logXML;

        StringBuilder str = new StringBuilder("");
        str.append(STR_MSG_START).append(STR_NEW_LINE);

        if (PAN != null) {
            str.append(STR_PAN).append(PAN).append(STR_NEW_LINE);
        }
        if (ifx != null) {
            str.append(STR_IFX).append(ifx).append(STR_NEW_LINE);
        }
        if (trn != null) {
            str.append(STR_TRN).append(trn).append(STR_NEW_LINE);
        }
        if (amt != null) {
        	str.append(STR_AMT).append(df.format(amt)).append(STR_NEW_LINE);
        }
        if (realAmt != null) {
        	str.append(STR_R_AMT).append(df.format(realAmt)).append(STR_NEW_LINE);
        }
        if (cur != null) {
            str.append(STR_CUR).append(cur).append(STR_NEW_LINE);
        }
        if (seqCntr != null) {
            str.append(STR_SEQ_CNTR).append(seqCntr).append(STR_NEW_LINE);
        }
        if (mySeqCntr != null) {
            str.append(STR_M_SEQ_CNTR).append(mySeqCntr).append(STR_NEW_LINE);
        }
        if (origDt != null) {
            str.append(STR_ORIGDT).append(origDt).append(STR_NEW_LINE);
        }
        if (trnDt != null) {
            str.append(STR_TRN_DT).append(trnDt).append(STR_NEW_LINE);
        }
        if (termType != null) {
            str.append(STR_TERM_TYPE).append(termType).append(STR_NEW_LINE);
        }
        if (bnk != null) {
            str.append(STR_BNK).append(bnk).append(STR_NEW_LINE);
        }
        if (fwdBnk != null) {
            str.append(STR_FW_BNK).append(fwdBnk).append(STR_NEW_LINE);
        }
        if (rcvBnk != null) {
            str.append(STR_REC_BNK).append(rcvBnk).append(STR_NEW_LINE);
        }
        if (orgName != null) {
            str.append(STR_ORG).append(orgName).append(STR_NEW_LINE);
        }
        if (netRef != null) {
            str.append(STR_NET_REF).append(netRef).append(STR_NEW_LINE);
        }
        if (term != null) {
            str.append(STR_TERM).append(term).append(STR_NEW_LINE);
        }

        if (rsCode != null) {
            str.append(STR_RSCODE).append(rsCode).append(STR_NEW_LINE);
        }
        if (docNum != null) {
            str.append(STR_DOCNUM).append(docNum).append(STR_NEW_LINE);
        }
        if (origType != null) {
            str.append(STR_ORIG_TYPE).append(origType).append(STR_NEW_LINE);
        }
        if (origSeqCntr != null) {
            str.append(STR_ORIG_TRSQ).append(origSeqCntr).append(STR_NEW_LINE);
        }
        if (origOrigDt != null) {
            str.append(STR_ORIG_ORDT).append(origOrigDt).append(STR_NEW_LINE);
        }
        if (origBnk != null) {
            str.append(STR_ORIG_BNK).append(origBnk).append(STR_NEW_LINE);
        }
        if (origFwdBnk != null) {
            str.append(STR_ORIG_FW_BNK).append(origFwdBnk).append(STR_NEW_LINE);
        }
        if (newAmtAcqCur != null) {
            str.append(STR_NEW_AMT_ACQ).append(newAmtAcqCur).append(STR_NEW_LINE);
        }
        if (newAmtIssCur != null) {
            str.append(STR_NEW_AMT_ISS).append(newAmtIssCur).append(STR_NEW_LINE);
        }
        if (orgNum != null) {
            str.append(STR_ORG_NUM).append(orgNum).append(STR_NEW_LINE);
        }
        if (orgType != null) {
            str.append(STR_ORG_TYP).append(orgType).append(STR_NEW_LINE);
        }
//        if (balAvbAcctType != null) {
//            str.append(STR_BAL_AV_ACC_TYP).append(balAvbAcctType).append(STR_NEW_LINE);
//        }
//        if (balAvbBalType != null) {
//            str.append(STR_BAL_AV_BAL_TYP).append(balAvbBalType).append(STR_NEW_LINE);
//        }
//        if (balAvbAmt != null) {
//            str.append(STR_BAL_AV_AMT).append(balAvbAmt).append(STR_NEW_LINE);
//        }
//        if (balAvbCurCode != null) {
//            str.append(STR_BAL_AV_CUR).append(balAvbCurCode).append(STR_NEW_LINE);
//        }
//        if (balLdgAcctType != null) {
//            str.append(STR_BAL_LG_ACC_TYP).append(balLdgAcctType).append(STR_NEW_LINE);
//        }
//        if (balLdgBalType != null) {
//            str.append(STR_BAL_LG_BAL_TYP).append(balLdgBalType).append(STR_NEW_LINE);
//        }
//        if (balLdgAmt != null) {
//            str.append(STR_BAL_LG_AMT).append(balLdgAmt).append(STR_NEW_LINE);
//        }
//        if (balLdgCurCode != null) {
//            str.append(STR_BAL_LG_CUR).append(balLdgCurCode).append(STR_NEW_LINE);
//        }
        if (secPAN != null) {
            str.append(STR_SEC_PAN).append(secPAN).append(STR_NEW_LINE);
        }
        if (name != null) {
            str.append(STR_NAME).append(name).append(STR_NEW_LINE);
        }
        if (family != null) {
            str.append(STR_FAMILY).append(family).append(STR_NEW_LINE);
        }
        if (mainAccNum != null) {
            str.append(STR_MAIN_ACC).append(mainAccNum).append(STR_NEW_LINE);
        }
        if (statement != null) {
            str.append(STR_STAT).append(statement).append(STR_NEW_LINE);
        }
        if (subAccs != null) {
            str.append(STR_SUB_ACCS).append(subAccs).append(STR_NEW_LINE);
        }
        if (ccardTrxAmt != null) {
            str.append(STR_CCRD_TRX).append(df.format(ccardTrxAmt)).append(STR_NEW_LINE);
        }
        if (ccardFee != null) {
            str.append(STR_CCRD_FEE).append(df.format(ccardFee)).append(STR_NEW_LINE);
        }
        if (ccardInt != null) {
            str.append(STR_CCRD_INT).append(df.format(ccardInt)).append(STR_NEW_LINE);
        }
        if (ccardStmtAmt != null) {
            str.append(STR_CCRD_STM).append(df.format(ccardStmtAmt)).append(STR_NEW_LINE);
        }
        if (ccardOTB != null) {
            str.append(STR_CCRD_OTB).append(df.format(ccardOTB)).append(STR_NEW_LINE);
        }
        if (lang != null) {
            str.append(STR_CCRD_LNG).append(lang).append(STR_NEW_LINE);
        }
        if (subAccTo != null) {
            str.append(STR_SUB_ACC_TO).append(subAccTo).append(STR_NEW_LINE);
        }
        if (subAccFr != null) {
            str.append(STR_SUB_ACC_FR).append(subAccFr).append(STR_NEW_LINE);
        }
        if (feeAmt != null) {
            str.append(STR_FEE_AMT).append(df.format(feeAmt)).append(STR_NEW_LINE);
        }
        if (errMsg != null) {
            str.append(STR_ERR_MSG).append(errMsg).append(STR_NEW_LINE);
        }
        if (actPAN != null) {
            str.append(STR_ACT_PAN).append(actPAN).append(STR_NEW_LINE);
        }
        if (actSecPAN != null) {
            str.append(STR_ACT_SEC_PAN).append(actSecPAN).append(STR_NEW_LINE);
        }
        if (stmtAccNum != null) {
            str.append(STR_ST_ACC_NUM).append(stmtAccNum).append(STR_NEW_LINE);
        }
        if (billId != null) {
            str.append(STR_BILL_ID).append(billId).append(STR_NEW_LINE);
        }
        if (billPayId != null) {
            str.append(STR_BILL_PAY_ID).append(billPayId).append(STR_NEW_LINE);
        }
        if (billType != null) {
            str.append(STR_BILL_TYPE).append(billType).append(STR_NEW_LINE);
        }
        if (billTypeName != null) {
            str.append(STR_BILL_TYPE_NAME).append(billTypeName).append(STR_NEW_LINE);
        }
//        if (holderMobile != null) {
//            str.append(STR_HOLDER_MOB).append(holderMobile).append(STR_NEW_LINE);
//        }
        if(origTerminalType != null && origTerminalType != TerminalType.UNKNOWN.getCode()){
            str.append(STR_ORIG_TRM_TYP).append(origTerminalType).append(STR_NEW_LINE);
        }
        if (trx != null) {
            str.append(STR_TRX).append(df.format(trx)).append(STR_NEW_LINE);
        }
        if (accTypeFr != null) {
            str.append(STR_ACC_TYP_FR).append(accTypeFr).append(STR_NEW_LINE);
        }
        if (accTypeTo != null) {
            str.append(STR_ACC_TYP_TO).append(accTypeTo).append(STR_NEW_LINE);
        }

        if (Util.hasText(transferFromDesc)) {
            str.append(STR_TRF_DESC).append(transferFromDesc).append(STR_NEW_LINE);
        }
        if (Util.hasText(transferToDesc)) {
            str.append(STR_TRT_DESC).append(transferToDesc).append(STR_NEW_LINE);
        }
        
        if (Util.hasText(shenaseOfTransferToAcc)) {
            str.append(STR_SHENASE_TRN).append(shenaseOfTransferToAcc).append(STR_NEW_LINE);
        }
        
        //TASK Task081 : ATM Saham Feature
        if (Util.hasText(shareCode)){
        	str.append(STR_SHARE_CODE).append(shareCode).append(STR_NEW_LINE);
        }
        //TASK Task081 : ATM Saham Feature
        if (shareCount != null){
        	str.append(STR_SHARE_COUNT).append(shareCount).append(STR_NEW_LINE);
        }    
        
        //Mirkamali(Task175): Restriction
        if(cardServiceTrnType != null){
        	str.append(STR_RESTRICTION_TRNTYPE).append(cardServiceTrnType).append(STR_NEW_LINE);
        }
        if(cardServiceTerminalType != null){
        	str.append(STR_RESTRICTION_TERMTYPE).append(cardServiceTerminalType).append(STR_NEW_LINE);
        }
        if(cycleType != null){
        	str.append(STR_RESTRICTION_CYCLETYPE).append(cycleType).append(STR_NEW_LINE);
        }

        if (Util.hasText(authIdResponse)) { //Raza MasterCard
            str.append(STR_AUTH_ID_RESPONSE).append(authIdResponse).append(STR_NEW_LINE);
        }
        str.append(STR_MSG_END);

        this.logXML = str.toString();
        return this.logXML;
    }

    public String getXML(){
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z", Locale.getDefault()); 
    	
    	StringBuilder str = new StringBuilder();
    	
         	str.append("<msg>" )
    			.append(this.expDt!=null? ("<expDt>"+this.expDt+"</expDt>"):"")
        		.append(this.cvv2!=null? ("<cvv2>"+this.cvv2 +"</cvv2>"): "")
        		.append(this.ifx!=null? ("<ifx>"+this.ifx+"</ifx>"): "" )
        		.append(this.trn!=null? ("<trn>"+this.trn+"</trn>"): "")
        		.append(this.PAN!=null? ("<PAN>"+this.PAN+"</PAN>"):"")
        		.append(this.amt!=null? ("<amt>"+this.amt+"</amt>"): "" )
        		.append(this.realAmt!=null? ("<realAmt>"+this.realAmt +"</realAmt>"): "" )
        		.append(this.cur!=null? ("<cur>"+this.cur+"</cur>"): "")
        		.append(this.seqCntr!=null? ("<seqCntr>"+this.seqCntr+"</seqCntr>"): "")
        		.append(this.mySeqCntr!=null? ("<mySeqCntr>"+this.mySeqCntr+"</mySeqCntr>"): "" )
                .append(this.trk2!=null?		"<trk2>"+ this.trk2+"</trk2>" : "")
                .append(this.termType!=null?	"<termType>"+ this.termType+"</termType>" :"")
                .append(this.bnk!=null?		"<bnk>"+ this.bnk+"</bnk>" : "" )
                .append(this.fwdBnk!=null?	"<fwdBnk>"+   this.fwdBnk+"</fwdBnk>":"")
                .append(this.rcvBnk!=null?	"<rcvBnk>"+   this.rcvBnk+"</rcvBnk>":"")
                .append(this.orgName!=null?	"<orgName>"+  this.orgName+"</orgName>":"")
                .append(this.netRef!=null?	"<netRef>"+   this.netRef+"</netRef>":"")
                .append(this.term!=null?		"<term>"+     this.term+"</term>":"")
                .append(this.pin!=null?		"<pin>"+      this.pin+"</pin>":"")
                .append(this.newPin!=null?	"<newPin>"+   this.newPin+"</newPin>":"")
                .append(this.oldPin!=null?	"<oldPin>"+   this.oldPin+"</oldPin>":"")
                .append(this.origDt!=null?		"<origDt>"+   	format.format(this.origDt)  +"</origDt>":"")
                .append(this.trnDt!=null?		"<trnDt>"+    	format.format(this.trnDt) + "</trnDt>":"")
                .append(this.rsCode!=null?		"<rsCode>"+   	this.rsCode+"</rsCode>":"")
                .append(this.docNum!=null?		"<docNum>"+   	this.docNum+"</docNum>":"")
                .append(this.origType!=null?	"<origType>"+ 	this.origType+"</origType>":"")
                .append(this.origSeqCntr!=null?"<origSeqCntr>"+this.origSeqCntr+"</origSeqCntr>":"")
                .append(this.origOrigDt!=null?"<origOrigDt>"+(format.format(this.origOrigDt)  +"</origOrigDt>") : "" )
                
                		
                
                
                .append(this.origBnk!=null?			"<origBnk>"+		this.origBnk+"</origBnk>":"")
                .append(this.origFwdBnk!=null?		"<origFwdBnk>"+		this.origFwdBnk+"</origFwdBnk>":"")
                .append(this.newAmtAcqCur!=null?	"<newAmtAcqCur>"+	this.newAmtAcqCur+"</newAmtAcqCur>":"")
                .append(this.newAmtIssCur!=null?	"<newAmtIssCur>"+	this.newAmtIssCur+"</newAmtIssCur>":"")
                .append(this.orgNum!=null?			"<orgNum>"+			this.orgNum+"</orgNum>":"")
                .append(this.orgType!=null?			"<orgType>"+		this.orgType+"</orgType>":"")
                .append(this.balAvbAcctType!=null?	"<balAvbAcctType>"+	this.balAvbAcctType+"</balAvbAcctType>":"")
                .append(this.balAvbBalType!=null?	"<balAvbBalType>"+	this.balAvbBalType+"</balAvbBalType>":"")
                .append(this.balAvbAmt!=null?		"<balAvbAmt>"+		this.balAvbAmt+"</balAvbAmt>":"")
                .append(this.balAvbCurCode!=null?	"<balAvbCurCode>"+	this.balAvbCurCode+"</balAvbCurCode>":"")
                .append(this.balLdgAcctType!=null?	"<balLdgAcctType>"+	this.balLdgAcctType+"</balLdgAcctType>":"")
                .append(this.balLdgBalType!=null?	"<balLdgBalType>"+	this.balLdgBalType+"</balLdgBalType>":"")
                .append(this.balLdgAmt!=null?		"<balLdgAmt>"+		this.balLdgAmt+"</balLdgAmt>":"")
                .append(this.balLdgCurCode!=null?	"<balLdgCurCode>"+	this.balLdgCurCode+"</balLdgCurCode>":"")
                .append(this.secPAN!=null?			"<secPAN>"+			this.secPAN+"</secPAN>":"")
                .append(this.name!=null?			"<name>"+			this.name+"</name>":"")
                .append(this.family!=null?			"<family>"+			this.family+"</family>":"")
                .append(this.mainAccNum!=null?		"<mainAccNum>"+		this.mainAccNum+"</mainAccNum>":"")
                .append(this.statement!=null?		"<statement>"+		this.statement+"</statement>":"")
                .append(this.subAccs!=null?			"<subAccs>"+		this.subAccs+"</subAccs>":"")
                .append(this.ccardTrxAmt!=null?		"<ccardTrxAmt>"+	this.ccardTrxAmt+"</ccardTrxAmt>":"")
                .append(this.ccardFee!=null?		"<ccardFee>"+		this.ccardFee+"</ccardFee>":"")
                .append(this.ccardInt!=null?		"<ccardInt>"+		this.ccardInt+"</ccardInt>":"")
                .append(this.ccardStmtAmt!=null?	"<ccardStmtAmt>"+	this.ccardStmtAmt+"</ccardStmtAmt>":"")
                .append(this.ccardOTB!=null?		"<ccardOTB>"+		this.ccardOTB+"</ccardOTB>":"")
                .append(this.lang!=null?			"<lang>"+			this.lang+"</lang>":"")
                .append(this.subAccTo!=null?		"<subAccTo>"+		this.subAccTo+"</subAccTo>":"")
                .append(this.subAccFr!=null?		"<subAccFr>"+		this.subAccFr+"</subAccFr>":"")
                .append(this.feeAmt!=null?			"<feeAmt>"+			this.feeAmt+"</feeAmt>":"")
                .append(this.errMsg!=null?			"<errMsg>"+			this.errMsg+"</errMsg>":"")
                .append(this.actPAN!=null?			"<actPAN>"+			this.actPAN+"</actPAN>":"")
                .append(this.actSecPAN!=null?		"<actSecPAN>"+		this.actSecPAN+"</actSecPAN>":"")
                .append(this.stmtAccNum!=null?		"<stmtAccNum>"+		this.stmtAccNum+"</stmtAccNum>":"")
                .append(this.billId!=null?			"<billId>"+			this.billId+"</billId>":"")
                .append(this.billPayId!=null?		"<billPayId>"+		this.billPayId+"</billPayId>":"")
                .append(this.billType!=null?		"<billType>"+		this.billType+"</billType>":"")
                .append(this.billTypeName!=null?	"<billTypeName>"+	this.billTypeName+"</billTypeName>":"")
                .append(this.holderMobile!=null?	"<holderMobile>"+	this.holderMobile+"</holderMobile>":"")
                .append(this.trx!=null?				"<trx>"+			this.trx+"</trx>":"")
                .append(this.accTypeFr!=null?		"<accTypeFr>"+		this.accTypeFr+"</accTypeFr>":"")
                .append(this.accTypeTo!=null?		"<accTypeTo>"+		this.accTypeTo+"</accTypeTo>":"")
                
                
                .append(this.transferFromDesc!=null?	"<transferFromDesc>"+	this.transferFromDesc+"</transferFromDesc>":"")
                .append(this.transferToDesc!=null?		"<transferToDesc>"+		this.transferToDesc+"</transferToDesc>":"")
                .append(this.shenaseOfTransferToAcc !=null?		"<shenaseOfTransferToAcc>"+		this.shenaseOfTransferToAcc+"</shenaseOfTransferToAcc>":"")
                .append(this.origTerminalType!=null?	"<origTerminalType>"+	this.origTerminalType+"</origTerminalType>":"")
                .append(this.shebaCode!=null?			"<shebaCode>"+			this.shebaCode+"</shebaCode>":"")
                .append(this.shareCode!=null?			"<shareCode>"+			this.shareCode+"</shareCode>":"")
                .append(this.shareCount!=null?			"<shareCount>"+			this.shareCount+"</shareCount>":"")
                
                //Mirkamali(Task175): Restriction
                .append(this.cycleType != null ? "<cycleType>"+this.cycleType+"</cycleType>" : "")
                .append(this.cardServiceTrnType != null ? "<cardServiceTrnType>"+this.cardServiceTrnType+"</cardServiceTrnType>" : "")
                .append(this.cardServiceTerminalType != null ? "<cardServiceTerminalType>"+this.cardServiceTerminalType+"</cardServiceTerminalType>" : "")
                
                
                .append(this.authIdResponse != null ? "<authIdResponse>"+this.authIdResponse+"</authIdResponse>" : "") //Raza MasterCard
                .append("</msg>");
         	
         	return str.toString();

    }


    public static CMSMessage fromString(String msg) throws Exception{
    	
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z", Locale.getDefault());
        CMSMessage cmsMsg = new CMSMessage();
        if(msg.indexOf("<expDt>") != -1 && msg.lastIndexOf("</expDt>") != -1)cmsMsg.expDt = new Long(msg.substring(msg.indexOf("<expDt>")+("<expDt>".length()), msg.lastIndexOf("</expDt>")));
        if(msg.indexOf("<cvv2>") != -1 && msg.lastIndexOf("</cvv2>") != -1)cmsMsg.cvv2 = msg.substring(msg.indexOf("<cvv2>")+("<cvv2>".length()), msg.lastIndexOf("</cvv2>"));
        if(msg.indexOf("<ifx>") != -1 && msg.lastIndexOf("</ifx>") != -1)cmsMsg.ifx = new Integer(msg.substring(msg.indexOf("<ifx>")+("<ifx>".length()), msg.lastIndexOf("</ifx>")));
        if(msg.indexOf("<trn>") != -1 && msg.lastIndexOf("</trn>") != -1)cmsMsg.trn = new Integer(msg.substring(msg.indexOf("<trn>")+("<trn>".length()), msg.lastIndexOf("</trn>")));
        if(msg.indexOf("<PAN>") != -1 && msg.lastIndexOf("</PAN>") != -1)cmsMsg.PAN = msg.substring(msg.indexOf("<PAN>")+("<PAN>".length()), msg.lastIndexOf("</PAN>"));
        if(msg.indexOf("<amt>") != -1 && msg.lastIndexOf("</amt>") != -1)cmsMsg.amt = new Long(msg.substring(msg.indexOf("<amt>")+("<amt>".length()), msg.lastIndexOf("</amt>")));
        if(msg.indexOf("<realAmt>") != -1 && msg.lastIndexOf("</realAmt>") != -1)cmsMsg.realAmt = new Long(msg.substring(msg.indexOf("<realAmt>")+("<realAmt>".length()), msg.lastIndexOf("</realAmt>")));
        if(msg.indexOf("<cur>") != -1 && msg.lastIndexOf("</cur>") != -1)cmsMsg.cur = new Integer(msg.substring(msg.indexOf("<cur>")+("<cur>".length()), msg.lastIndexOf("</cur>")));
        if(msg.indexOf("<seqCntr>") != -1 && msg.lastIndexOf("</seqCntr>") != -1)cmsMsg.seqCntr = msg.substring(msg.indexOf("<seqCntr>")+("<seqCntr>".length()), msg.lastIndexOf("</seqCntr>"));
        if(msg.indexOf("<mySeqCntr>") != -1 && msg.lastIndexOf("</mySeqCntr>") != -1)cmsMsg.mySeqCntr = msg.substring(msg.indexOf("<mySeqCntr>")+("<mySeqCntr>".length()), msg.lastIndexOf("</mySeqCntr>"));
        if(msg.indexOf("<trk2>") != -1 && msg.lastIndexOf("</trk2>") != -1)cmsMsg.trk2 = msg.substring(msg.indexOf("<trk2>")+("<trk2>".length()), msg.lastIndexOf("</trk2>"));
        if(msg.indexOf("<termType>") != -1 && msg.lastIndexOf("</termType>") != -1)cmsMsg.termType = new Integer(msg.substring(msg.indexOf("<termType>")+("<termType>".length()), msg.lastIndexOf("</termType>")));
        if(msg.indexOf("<bnk>") != -1 && msg.lastIndexOf("</bnk>") != -1)cmsMsg.bnk = new String(msg.substring(msg.indexOf("<bnk>")+("<bnk>".length()), msg.lastIndexOf("</bnk>")));
        if(msg.indexOf("<fwdBnk>") != -1 && msg.lastIndexOf("</fwdBnk>") != -1)cmsMsg.fwdBnk = new String(msg.substring(msg.indexOf("<fwdBnk>")+("<fwdBnk>".length()), msg.lastIndexOf("</fwdBnk>")));
        if(msg.indexOf("<rcvBnk>") != -1 && msg.lastIndexOf("</rcvBnk>") != -1)cmsMsg.rcvBnk = new String(msg.substring(msg.indexOf("<rcvBnk>")+("<rcvBnk>".length()), msg.lastIndexOf("</rcvBnk>")));
        if(msg.indexOf("<orgName>") != -1 && msg.lastIndexOf("</orgName>") != -1)cmsMsg.orgName = msg.substring(msg.indexOf("<orgName>")+("<orgName>".length()), msg.lastIndexOf("</orgName>"));
        if(msg.indexOf("<netRef>") != -1 && msg.lastIndexOf("</netRef>") != -1)cmsMsg.netRef = msg.substring(msg.indexOf("<netRef>")+("<netRef>".length()), msg.lastIndexOf("</netRef>"));
        if(msg.indexOf("<term>") != -1 && msg.lastIndexOf("</term>") != -1)cmsMsg.term = msg.substring(msg.indexOf("<term>")+("<term>".length()), msg.lastIndexOf("</term>"));
        if(msg.indexOf("<pin>") != -1 && msg.lastIndexOf("</pin>") != -1)cmsMsg.pin = msg.substring(msg.indexOf("<pin>")+("<pin>".length()), msg.lastIndexOf("</pin>"));
        if(msg.indexOf("<newPin>") != -1 && msg.lastIndexOf("</newPin>") != -1)cmsMsg.newPin = msg.substring(msg.indexOf("<newPin>")+("<newPin>".length()), msg.lastIndexOf("</newPin>"));
        if(msg.indexOf("<oldPin>") != -1 && msg.lastIndexOf("</oldPin>") != -1)cmsMsg.oldPin = msg.substring(msg.indexOf("<oldPin>")+("<oldPin>".length()), msg.lastIndexOf("</oldPin>"));
        if(msg.indexOf("<origDt>") != -1 && msg.lastIndexOf("</origDt>") != -1)cmsMsg.origDt = format.parse(msg.substring(msg.indexOf("<origDt>")+("<origDt>".length()), msg.lastIndexOf("</origDt>")));
        if(msg.indexOf("<trnDt>") != -1 && msg.lastIndexOf("</trnDt>") != -1)cmsMsg.trnDt = format.parse(msg.substring(msg.indexOf("<trnDt>")+("<trnDt>".length()), msg.lastIndexOf("</trnDt>")));
        if(msg.indexOf("<rsCode>") != -1 && msg.lastIndexOf("</rsCode>") != -1)cmsMsg.rsCode = msg.substring(msg.indexOf("<rsCode>")+("<rsCode>".length()), msg.lastIndexOf("</rsCode>"));
        if(msg.indexOf("<docNum>") != -1 && msg.lastIndexOf("</docNum>") != -1)cmsMsg.docNum = msg.substring(msg.indexOf("<docNum>")+("<docNum>".length()), msg.lastIndexOf("</docNum>"));
        if(msg.indexOf("<origType>") != -1 && msg.lastIndexOf("</origType>") != -1)cmsMsg.origType = msg.substring(msg.indexOf("<origType>")+("<origType>".length()), msg.lastIndexOf("</origType>"));
        if(msg.indexOf("<origSeqCntr>") != -1 && msg.lastIndexOf("</origSeqCntr>") != -1)cmsMsg.origSeqCntr = msg.substring(msg.indexOf("<origSeqCntr>")+("<origSeqCntr>".length()), msg.lastIndexOf("</origSeqCntr>"));
        if(msg.indexOf("<origOrigDt>") != -1 && msg.lastIndexOf("</origOrigDt>") != -1)cmsMsg.origOrigDt = format.parse(msg.substring(msg.indexOf("<origOrigDt>")+("<origOrigDt>".length()), msg.lastIndexOf("</origOrigDt>")));
        if(msg.indexOf("<origBnk>") != -1 && msg.lastIndexOf("</origBnk>") != -1)cmsMsg.origBnk = new String(msg.substring(msg.indexOf("<origBnk>")+("<origBnk>".length()), msg.lastIndexOf("</origBnk>")));
        if(msg.indexOf("<origFwdBnk>") != -1 && msg.lastIndexOf("</origFwdBnk>") != -1)cmsMsg.origFwdBnk = new String(msg.substring(msg.indexOf("<origFwdBnk>")+("<origFwdBnk>".length()), msg.lastIndexOf("</origFwdBnk>")));
        if(msg.indexOf("<newAmtAcqCur>") != -1 && msg.lastIndexOf("</newAmtAcqCur>") != -1)cmsMsg.newAmtAcqCur = msg.substring(msg.indexOf("<newAmtAcqCur>")+("<newAmtAcqCur>".length()), msg.lastIndexOf("</newAmtAcqCur>"));
        if(msg.indexOf("<newAmtIssCur>") != -1 && msg.lastIndexOf("</newAmtIssCur>") != -1)cmsMsg.newAmtIssCur = msg.substring(msg.indexOf("<newAmtIssCur>")+("<newAmtIssCur>".length()), msg.lastIndexOf("</newAmtIssCur>"));
        if(msg.indexOf("<orgNum>") != -1 && msg.lastIndexOf("</orgNum>") != -1)cmsMsg.orgNum = msg.substring(msg.indexOf("<orgNum>")+("<orgNum>".length()), msg.lastIndexOf("</orgNum>"));
        if(msg.indexOf("<orgType>") != -1 && msg.lastIndexOf("</orgType>") != -1)cmsMsg.orgType = new Long(msg.substring(msg.indexOf("<orgType>")+("<orgType>".length()), msg.lastIndexOf("</orgType>")));
        if(msg.indexOf("<balAvbAcctType>") != -1 && msg.lastIndexOf("</balAvbAcctType>") != -1)cmsMsg.balAvbAcctType = new Integer(msg.substring(msg.indexOf("<balAvbAcctType>")+("<balAvbAcctType>".length()), msg.lastIndexOf("</balAvbAcctType>")));
        if(msg.indexOf("<balAvbBalType>") != -1 && msg.lastIndexOf("</balAvbBalType>") != -1)cmsMsg.balAvbBalType = new Integer(msg.substring(msg.indexOf("<balAvbBalType>")+("<balAvbBalType>".length()), msg.lastIndexOf("</balAvbBalType>")));
        if(msg.indexOf("<balAvbAmt>") != -1 && msg.lastIndexOf("</balAvbAmt>") != -1)cmsMsg.balAvbAmt = msg.substring(msg.indexOf("<balAvbAmt>")+("<balAvbAmt>".length()), msg.lastIndexOf("</balAvbAmt>"));
        if(msg.indexOf("<balAvbCurCode>") != -1 && msg.lastIndexOf("</balAvbCurCode>") != -1)cmsMsg.balAvbCurCode = msg.substring(msg.indexOf("<balAvbCurCode>")+("<balAvbCurCode>".length()), msg.lastIndexOf("</balAvbCurCode>"));
        if(msg.indexOf("<balLdgAcctType>") != -1 && msg.lastIndexOf("</balLdgAcctType>") != -1)cmsMsg.balLdgAcctType = new Integer(msg.substring(msg.indexOf("<balLdgAcctType>")+("<balLdgAcctType>".length()), msg.lastIndexOf("</balLdgAcctType>")));
        if(msg.indexOf("<balLdgBalType>") != -1 && msg.lastIndexOf("</balLdgBalType>") != -1)cmsMsg.balLdgBalType = new Integer(msg.substring(msg.indexOf("<balLdgBalType>")+("<balLdgBalType>".length()), msg.lastIndexOf("</balLdgBalType>")));
        if(msg.indexOf("<balLdgAmt>") != -1 && msg.lastIndexOf("</balLdgAmt>") != -1)cmsMsg.balLdgAmt = msg.substring(msg.indexOf("<balLdgAmt>")+("<balLdgAmt>".length()), msg.lastIndexOf("</balLdgAmt>"));
        if(msg.indexOf("<balLdgCurCode>") != -1 && msg.lastIndexOf("</balLdgCurCode>") != -1)cmsMsg.balLdgCurCode = msg.substring(msg.indexOf("<balLdgCurCode>")+("<balLdgCurCode>".length()), msg.lastIndexOf("</balLdgCurCode>"));
        if(msg.indexOf("<secPAN>") != -1 && msg.lastIndexOf("</secPAN>") != -1)cmsMsg.secPAN = msg.substring(msg.indexOf("<secPAN>")+("<secPAN>".length()), msg.lastIndexOf("</secPAN>"));
        if(msg.indexOf("<name>") != -1 && msg.lastIndexOf("</name>") != -1)cmsMsg.name = msg.substring(msg.indexOf("<name>")+("<name>".length()), msg.lastIndexOf("</name>"));
        if(msg.indexOf("<family>") != -1 && msg.lastIndexOf("</family>") != -1)cmsMsg.family = msg.substring(msg.indexOf("<family>")+("<family>".length()), msg.lastIndexOf("</family>"));
        if(msg.indexOf("<mainAccNum>") != -1 && msg.lastIndexOf("</mainAccNum>") != -1)cmsMsg.mainAccNum = msg.substring(msg.indexOf("<mainAccNum>")+("<mainAccNum>".length()), msg.lastIndexOf("</mainAccNum>"));
        if(msg.indexOf("<statement>") != -1 && msg.lastIndexOf("</statement>") != -1)cmsMsg.statement = msg.substring(msg.indexOf("<statement>")+("<statement>".length()), msg.lastIndexOf("</statement>"));
        if(msg.indexOf("<subAccs>") != -1 && msg.lastIndexOf("</subAccs>") != -1)cmsMsg.subAccs = msg.substring(msg.indexOf("<subAccs>")+("<subAccs>".length()), msg.lastIndexOf("</subAccs>"));
        if(msg.indexOf("<ccardTrxAmt>") != -1 && msg.lastIndexOf("</ccardTrxAmt>") != -1)cmsMsg.ccardTrxAmt = new Long(msg.substring(msg.indexOf("<ccardTrxAmt>")+("<ccardTrxAmt>".length()), msg.lastIndexOf("</ccardTrxAmt>")));
        if(msg.indexOf("<ccardFee>") != -1 && msg.lastIndexOf("</ccardFee>") != -1)cmsMsg.ccardFee = new Long(msg.substring(msg.indexOf("<ccardFee>")+("<ccardFee>".length()), msg.lastIndexOf("</ccardFee>")));
        if(msg.indexOf("<ccardInt>") != -1 && msg.lastIndexOf("</ccardInt>") != -1)cmsMsg.ccardInt = new Long(msg.substring(msg.indexOf("<ccardInt>")+("<ccardInt>".length()), msg.lastIndexOf("</ccardInt>")));
        if(msg.indexOf("<ccardStmtAmt>") != -1 && msg.lastIndexOf("</ccardStmtAmt>") != -1)cmsMsg.ccardStmtAmt = new Long(msg.substring(msg.indexOf("<ccardStmtAmt>")+("<ccardStmtAmt>".length()), msg.lastIndexOf("</ccardStmtAmt>")));
        if(msg.indexOf("<ccardOTB>") != -1 && msg.lastIndexOf("</ccardOTB>") != -1)cmsMsg.ccardOTB = new Long(msg.substring(msg.indexOf("<ccardOTB>")+("<ccardOTB>".length()), msg.lastIndexOf("</ccardOTB>")));
        if(msg.indexOf("<lang>") != -1 && msg.lastIndexOf("</lang>") != -1)cmsMsg.lang = new Integer(msg.substring(msg.indexOf("<lang>")+("<lang>".length()), msg.lastIndexOf("</lang>")));
        if(msg.indexOf("<subAccTo>") != -1 && msg.lastIndexOf("</subAccTo>") != -1)cmsMsg.subAccTo = msg.substring(msg.indexOf("<subAccTo>")+("<subAccTo>".length()), msg.lastIndexOf("</subAccTo>"));
        if(msg.indexOf("<subAccFr>") != -1 && msg.lastIndexOf("</subAccFr>") != -1)cmsMsg.subAccFr = msg.substring(msg.indexOf("<subAccFr>")+("<subAccFr>".length()), msg.lastIndexOf("</subAccFr>"));
        if(msg.indexOf("<feeAmt>") != -1 && msg.lastIndexOf("</feeAmt>") != -1)cmsMsg.feeAmt = new Long(msg.substring(msg.indexOf("<feeAmt>")+("<feeAmt>".length()), msg.lastIndexOf("</feeAmt>")));
        if(msg.indexOf("<errMsg>") != -1 && msg.lastIndexOf("</errMsg>") != -1)cmsMsg.errMsg = msg.substring(msg.indexOf("<errMsg>")+("<errMsg>".length()), msg.lastIndexOf("</errMsg>"));
        if(msg.indexOf("<actPAN>") != -1 && msg.lastIndexOf("</actPAN>") != -1)cmsMsg.actPAN = msg.substring(msg.indexOf("<actPAN>")+("<actPAN>".length()), msg.lastIndexOf("</actPAN>"));
        if(msg.indexOf("<actSecPAN>") != -1 && msg.lastIndexOf("</actSecPAN>") != -1)cmsMsg.actSecPAN = msg.substring(msg.indexOf("<actSecPAN>")+("<actSecPAN>".length()), msg.lastIndexOf("</actSecPAN>"));
        if(msg.indexOf("<stmtAccNum>") != -1 && msg.lastIndexOf("</stmtAccNum>") != -1)cmsMsg.stmtAccNum = msg.substring(msg.indexOf("<stmtAccNum>")+("<stmtAccNum>".length()), msg.lastIndexOf("</stmtAccNum>"));
        if(msg.indexOf("<billId>") != -1 && msg.lastIndexOf("</billId>") != -1)cmsMsg.billId = msg.substring(msg.indexOf("<billId>")+("<billId>".length()), msg.lastIndexOf("</billId>"));
        if(msg.indexOf("<billPayId>") != -1 && msg.lastIndexOf("</billPayId>") != -1)cmsMsg.billPayId = msg.substring(msg.indexOf("<billPayId>")+("<billPayId>".length()), msg.lastIndexOf("</billPayId>"));
        if(msg.indexOf("<billType>") != -1 && msg.lastIndexOf("</billType>") != -1)cmsMsg.billType = new Integer(msg.substring(msg.indexOf("<billType>")+("<billType>".length()), msg.lastIndexOf("</billType>")));
        if(msg.indexOf("<billTypeName>") != -1 && msg.lastIndexOf("</billTypeName>") != -1)cmsMsg.billTypeName = msg.substring(msg.indexOf("<billTypeName>")+("<billTypeName>".length()), msg.lastIndexOf("</billTypeName>"));
        if(msg.indexOf("<holderMobile>") != -1 && msg.lastIndexOf("</holderMobile>") != -1)cmsMsg.holderMobile = msg.substring(msg.indexOf("<holderMobile>")+("<holderMobile>".length()), msg.lastIndexOf("</holderMobile>"));
        if(msg.indexOf("<trx>") != -1 && msg.lastIndexOf("</trx>") != -1)cmsMsg.trx = new Long(msg.substring(msg.indexOf("<trx>")+("<trx>".length()), msg.lastIndexOf("</trx>")));
        if(msg.indexOf("<accTypeFr>") != -1 && msg.lastIndexOf("</accTypeFr>") != -1)cmsMsg.accTypeFr = new Integer(msg.substring(msg.indexOf("<accTypeFr>")+("<accTypeFr>".length()), msg.lastIndexOf("</accTypeFr>")));
        if(msg.indexOf("<accTypeTo>") != -1 && msg.lastIndexOf("</accTypeTo>") != -1)cmsMsg.accTypeTo = new Integer(msg.substring(msg.indexOf("<accTypeTo>")+("<accTypeTo>".length()), msg.lastIndexOf("</accTypeTo>")));
        if(msg.indexOf("<transferFromDesc>") != -1 && msg.lastIndexOf("</transferFromDesc>") != -1)cmsMsg.transferFromDesc = msg.substring(msg.indexOf("<transferFromDesc>")+("<transferFromDesc>".length()), msg.lastIndexOf("</transferFromDesc>"));
        if(msg.indexOf("<transferToDesc>") != -1 && msg.lastIndexOf("</transferToDesc>") != -1)cmsMsg.transferToDesc = msg.substring(msg.indexOf("<transferToDesc>")+("<transferToDesc>".length()), msg.lastIndexOf("</transferToDesc>"));
        if(msg.indexOf("<shenaseOfTransferToAcc>") != -1 && msg.lastIndexOf("</shenaseOfTransferToAcc>") != -1)cmsMsg.shenaseOfTransferToAcc = msg.substring(msg.indexOf("<shenaseOfTransferToAcc>")+("<shenaseOfTransferToAcc>".length()), msg.lastIndexOf("</shenaseOfTransferToAcc>"));//gholamiCOMMENT
        if(msg.indexOf("<origTerminalType>") != -1 && msg.lastIndexOf("</origTerminalType>") != -1)cmsMsg.origTerminalType = new Integer(msg.substring(msg.indexOf("<origTerminalType>")+("<origTerminalType>".length()), msg.lastIndexOf("</origTerminalType>")));
        if(msg.indexOf("<shebaCode>") != -1 && msg.lastIndexOf("</shebaCode>") != -1)cmsMsg.shebaCode = msg.substring(msg.indexOf("<shebaCode>")+("<shebaCode>".length()), msg.lastIndexOf("</shebaCode>"));
        if(msg.indexOf("<shareCode>") != -1 && msg.lastIndexOf("</shareCode>") != -1)cmsMsg.shareCode = msg.substring(msg.indexOf("<shareCode>")+("<shareCode>".length()), msg.lastIndexOf("</shareCode>"));
        if(msg.indexOf("<shareCount>") != -1 && msg.lastIndexOf("</shareCount>") != -1)cmsMsg.shareCount = new Long(msg.substring(msg.indexOf("<shareCount>")+("<shareCount>".length()), msg.lastIndexOf("</shareCount>")));
        if(msg.indexOf("<authIdResponse>") != -1 && msg.lastIndexOf("</authIdResponse>") != -1)cmsMsg.authIdResponse = msg.substring(msg.indexOf("<authIdResponse>")+("<authIdResponse>".length()), msg.lastIndexOf("</authIdResponse>")); //Raza MasterCard
        try{
        if( cmsMsg.trk2 != null && cmsMsg.trk2.indexOf("&") != -1){
            cmsMsg.trk2 = cmsMsg.trk2.replaceAll("&lt;", "<");
            cmsMsg.trk2 = cmsMsg.trk2.replaceAll("&amp;", "&");
            cmsMsg.trk2 = cmsMsg.trk2.replaceAll("&gt;", ">");
            cmsMsg.trk2 = cmsMsg.trk2.replaceAll("&quot;", "\"");
            cmsMsg.trk2 = cmsMsg.trk2.replaceAll("&apos;", "'");
//            &lt; (<), &amp; (&), &gt; (>), &quot; ("), and &apos; (').
        }
        }catch (Exception e){}
        return cmsMsg;


    }
}

