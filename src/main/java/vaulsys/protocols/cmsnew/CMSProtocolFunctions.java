package vaulsys.protocols.cmsnew;

import static vaulsys.protocols.cms.utils.CMSMapperUtil.*;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.customer.Currency;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.CMSHttpToIFXMapper;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.constants.RestrictionOnTrxAndTermType;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class CMSProtocolFunctions implements ProtocolFunctions {
    Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction)
            throws CantAddNecessaryDataToIfxException {
    }

    @Override
    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction)
            throws CantAddNecessaryDataToIfxException {
    }

    @Override
    public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX,
                                                EncodingConvertor convertor) throws Exception {
        return null;
    }

    @Override
    public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage)
            throws CantPostProcessBinaryDataException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //System.out.println("CMSProtocolFunctions:: outgoingMessage [" + outgoingMessage.getBinaryData() + "]"); //Raza TEMP
        //String temp = new String(outgoingMessage.getBinaryData());
        //System.out.println("CMSProtocolFunctions:: outgoingMessage Str [" + temp + "]"); //Raza TEMP

        SwitchTerminal terminal = ProcessContext.get().getAcquierSwitchTerminal(outgoingMessage.getChannel().getInstitutionId());
        //System.out.println("CMSProtocolFunctions:: Terminal ID [" + terminal.getId() + "]"); //Raza TEMP
        //System.out.println("CMSProtocolFunctions:: Terminal Code [" + terminal.getCode() + "]"); //Raza TEMP
        //System.out.println("CMSProtocolFunctions:: Terminal Owner-ID [" + terminal.getOwnerId() + "]"); //Raza TEMP
        //System.out.println("CMSProtocolFunctions:: Terminal Owner-ID [" + terminal.getKeySet().toString() + "]"); //Raza TEMP
        try {
            byte[] b = SecurityComponent.encrypt(outgoingMessage.getBinaryData(), SecureDESKey.getKeyByType(SMAdapter.TYPE_TMK, terminal.getKeySet()));

            out.write(((CMSMessage)outgoingMessage.getProtocolMessage()).isReversal);
            out.write(((CMSMessage)outgoingMessage.getProtocolMessage()).cardType);
            try {
                out.write(b);
            } catch (IOException e) {
                logger.error(e,e);
                throw new NotProducedProtocolToBinaryException("Error in filling output byte buffer....");
            }

            //String temp2 = new String(out.toByteArray());
            //System.out.println("CMSProtocolFunctions:: outgoingMessage Str NOW [" + temp2 + "]"); //Raza TEMP
            outgoingMessage.setBinaryData(out.toByteArray());
        } catch (Exception e) {
            logger.error("Cannot encrypt data for transmission..."+ e);
            throw new CantPostProcessBinaryDataException("Cannot encrypt data for transmission...");
        }
    }

    @Override
    public byte[] preProcessBinaryMessage(Message incommingMessage) throws Exception {
        SwitchTerminal terminal = ProcessContext.get().getAcquierSwitchTerminal(incommingMessage.getChannel().getInstitutionId());

        byte[] b = SecurityComponent.decrypt(incommingMessage.getBinaryData(), terminal.getKeySet());
        incommingMessage.setBinaryData(b);

        return b;
    }

    @Override
    public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
        CMSMessage cmsMessage;

        try{
            cmsMessage = CMSMessage.fromString(new String(rawdata));
        }catch (Exception e){
            logger.error("Exception fromBinary" + e, e);
            InputStream is = new ByteArrayInputStream(rawdata);
            XStream xStream = new XStream();
            xStream.alias("msg", CMSMessage.class);
            cmsMessage = (CMSMessage) xStream.fromXML(is);
        }

        return cmsMessage;
    }

    @Override
    public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {
        CMSMessage message = new CMSMessage();

        message.isReversal = (byte) (ISOFinalMessageType.isReversalMessage(ifx.getIfxType())?0x31:0x30);
        message.cardType = (byte) (ifx.getAppPAN().charAt(6));

        message.ifx = FromIfxType.get(ifx.getIfxType());

        message.trn = FromTrnType.get(ifx.getTrnType());

        message.PAN = ifx.getAppPAN();
        if(!ifx.getAppPAN().equals(ifx.getActualAppPAN()))
            message.actPAN = ifx.getActualAppPAN();

        //m.rehman: set card holder billing amount and currency if available
        if (ifx.getSec_Amt() != null) {
            message.amt = ifx.getSec_Amt();
            message.realAmt = ifx.getSec_Amt();
        } else {
        message.amt = ifx.getAuth_Amt();
            message.realAmt = ifx.getAuth_Amt();
        }
        /*
        if(ifx.getTrx_Amt() != null && !ifx.getAuth_Amt().equals(ifx.getTrx_Amt())) {
            message.realAmt = ifx.getTrx_Amt();
        }
        */
        //if(!ProcessContext.get().getRialCurrency().getCode().equals(ifx.getAuth_Currency()))
        if (ifx.getSec_Currency() != null)
            message.cur = ifx.getSec_Currency();
        else
            message.cur = ifx.getAuth_Currency();

        message.seqCntr = ifx.getSrc_TrnSeqCntr();
        if(!ifx.getSrc_TrnSeqCntr().equals(ifx.getMy_TrnSeqCntr()))
            message.mySeqCntr = ifx.getMy_TrnSeqCntr();

        message.trnDt = ifx.getTrnDt().toDate();
        message.trk2 = ifx.getTrk2EquivData();
        message.expDt = ifx.getExpDt();
        if(ifx.getCVV2() != null && Util.hasText(ifx.getCVV2().trim()) && !ifx.getCVV2().equals("0000"))
            message.cvv2 = ifx.getCVV2();

        if(!AccType.MAIN_ACCOUNT.equals(ifx.getAccTypeFrom()))
            message.accTypeFr = FromAccType.get(ifx.getAccTypeFrom());

        if(!AccType.MAIN_ACCOUNT.equals(ifx.getAccTypeTo()))
            message.accTypeTo = FromAccType.get(ifx.getAccTypeTo());

        if(!UserLanguage.FARSI_LANG.equals(ifx.getUserLanguage()))
            message.lang = FromUserLang.get(ifx.getUserLanguage());

        message.termType = FromTerminalType.get(ifx.getTerminalType());
        message.bnk = ifx.getBankId();
        if(!ifx.getDestBankId().equals(ifx.getBankId())){
            message.fwdBnk = ifx.getDestBankId();
        }
        if(ifx.getRecvBankId() != null && !ifx.getRecvBankId().equals(ifx.getBankId())){
            message.rcvBnk = ifx.getRecvBankId();
        }

        message.orgName = ifx.getName();
        message.netRef = ifx.getNetworkRefId();
        message.term = ifx.getTerminalId();

        message.pin = ifx.getPINBlock();

        message.origDt = ifx.getOrigDt().toDate();
        //m.rehman: map response code to CMS
        if (ifx.getRsCode() != null)
            message.rsCode = FromErrorCode.get(ifx.getRsCode());
        message.docNum = ifx.getDocumentNumber();

        message.name = ifx.getCardHolderName();
        message.family = ifx.getCardHolderFamily();
        message.secPAN = ifx.getSecondAppPan();
        if(Util.hasText(ifx.getActualSecondAppPan()) && !ifx.getSecondAppPan().equals(ifx.getActualSecondAppPan()))
            message.actSecPAN = ifx.getActualSecondAppPan();
        message.mainAccNum = ifx.getMainAccountNumber();

        if(ifx.getNetworkTrnInfo() != null && ifx.getNetworkTrnInfo().getOrigTerminalType() != null  ){

            message.origTerminalType = FromTerminalType.get(ifx.getNetworkTrnInfo().getOrigTerminalType());
        }



        if (ifx.getOriginalDataElements() != null) {
            message.origType = ifx.getOriginalDataElements().getMessageType();
            message.origSeqCntr = ifx.getOriginalDataElements().getTrnSeqCounter();
            message.origOrigDt = ifx.getOriginalDataElements().getOrigDt().toDate();
            message.origBnk = ifx.getOriginalDataElements().getBankId();
            message.origFwdBnk = ifx.getOriginalDataElements().getFwdBankId();

            message.newAmtAcqCur = ifx.getNew_AmtAcqCur() != null ? ifx.getNew_AmtAcqCur() : "000000000000";
            message.newAmtIssCur = ifx.getNew_AmtIssCur() != null ? ifx.getNew_AmtIssCur() : "000000000000";
        }

        message.orgNum = ifx.getOrgIdNum();

        message.newPin = ifx.getNewPINBlock();
        message.oldPin = ifx.getOldPINBlock();

        message.subAccTo = ifx.getSubsidiaryAccTo();
        message.subAccFr = ifx.getSubsidiaryAccFrom();

        message.billId = ifx.getBillID();
        message.billPayId = ifx.getBillPaymentID();
        if (ifx.getBillOrgType() != null) {
            message.billType = (int) OrganizationType.getCode(ifx.getBillOrgType());
            message.billTypeName = ifx.getBillOrgType().toString();
        }

        if(ifx.getFirstTrxId() != null){
            message.trx = ifx.getFirstTrxId();
        }

        if (Util.hasText(ifx.getTransferFromDesc()))
            message.transferFromDesc = ifx.getTransferFromDesc();

        if (Util.hasText(ifx.getTransferToDesc()))
            message.transferToDesc = ifx.getTransferToDesc();
        
//        gholami(Task45875)
        if (Util.hasText(ifx.getShenaseOfTransferToAccount()))
            message.shenaseOfTransferToAcc = ifx.getShenaseOfTransferToAccount();
        
        message.shebaCode = ifx.getShebaCode();

			
			
		//Mirkamali(Task175): Restriction
		if(IfxType.RESTRICTION_RQ.equals(ifx.getIfxType())) {
			message.cardServiceTrnType = FromTrnType.get(RestrictionOnTrxAndTermType.getTransactionType(ifx.getBufferB()));
			message.cardServiceTerminalType = FromTerminalType.get(RestrictionOnTrxAndTermType.getTerminalType(ifx.getBufferB()));
			message.cycleType = Integer.valueOf(ifx.getBufferC());
		}

        /********************Mirkamali: change some data for sorush transfer for CREDIT card*****************/
        if(TransactionService.IsSorush(ifx) && ifx.getOriginalDataElements() != null)
            message.term = ifx.getOriginalDataElements().getTerminalId();
        if(TransactionService.IsSorushReverce(ifx) && ifx.getTransaction().getReferenceTransaction() != null){
            if(ifx.getTransaction().getReferenceTransaction().getIncomingIfx()!=null &&
                    ifx.getTransaction().getReferenceTransaction().getIncomingIfx().getOriginalDataElements() != null)
                message.term = ifx.getTransaction().getReferenceTransaction().getIncomingIfx().getOriginalDataElements().getTerminalId();
        }
        /**************************************************************************************************/
        //m.rehman: for testing, will correct it according the actual CMS in future
        

        if (Util.hasText(ifx.getApprovalCode()))
            message.authIdResponse = ifx.getApprovalCode();

        return message;
    }

    @Override
    public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
        CMSMessage msg = (CMSMessage) protocolMessage;
        if(Util.hasText(msg.xml))
            return msg.xml.getBytes();

        try{
            msg.xml = msg.getXML();
        }catch (Exception e){
            logger.error("Exception toBinary" + e, e);
            XStream xStream = new XStream();
            msg.xml = xStream.toXML(msg).replaceAll("vaulsys.protocols.cmsnew.CMSMessage", "msg");
        }
        return msg.xml.getBytes();

    }

    @Override
    public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor)
            throws NotMappedProtocolToIfxException {
        CMSMessage cmsMessage = (CMSMessage) protocolMessage;
        Ifx ifx = new Ifx();

        if(Util.hasText(cmsMessage.holderMobile))
            ifx.setCardHolderMobileNo(cmsMessage.holderMobile.trim());

        ifx.setIfxType(ToIfxType.get(cmsMessage.ifx));

        ifx.setTrnType(ToTrnType.get(cmsMessage.trn));

        if (Util.hasText(cmsMessage.PAN))
            ifx.setAppPAN(cmsMessage.PAN.trim());

        if (Util.hasText(cmsMessage.actPAN))
            ifx.setActualAppPAN(cmsMessage.actPAN.trim());


        ifx.setReal_Amt(cmsMessage.amt);
        ifx.setAuth_Amt(cmsMessage.amt);

        if(cmsMessage.realAmt != null)
            ifx.setTrx_Amt(cmsMessage.realAmt);
        else
            ifx.setTrx_Amt(cmsMessage.amt);

//		if (ShetabFinalMessageType.isReversalMessage(ifx.getIfxType())
//				&& ifx.getNew_AmtAcqCur() != null
//				&& !ifx.getNew_AmtAcqCur().equals("000000000000")
//				) {
//
//			ifx.setAuth_Amt(cmsMessage.amt);
////			ifx.setSent_Amt(cmsMessage.amt);
//
//			if(cmsMessage.realAmt != null)
//				ifx.setReal_Amt(cmsMessage.realAmt);
//			else
//				ifx.setReal_Amt(cmsMessage.amt);
//
//		} else {
//			ifx.setReal_Amt(cmsMessage.amt);
//			ifx.setSent_Amt(cmsMessage.amt);
//
//			if(cmsMessage.realAmt != null)
//				ifx.setAuth_Amt(cmsMessage.realAmt);
//			else
//				ifx.setAuth_Amt(cmsMessage.amt);
//		}

//		if (ShetabFinalMessageType.isReversalMessage(ifx.getIfxType())) {
//			ifx.setAuth_Amt(cmsMessage.amt);
//		} else {
//			ifx.setReal_Amt(cmsMessage.amt);
//		}

        if(cmsMessage.feeAmt != null){
            ifx.setTotalFeeAmt(cmsMessage.feeAmt);
        }else{
            ifx.setTotalFeeAmt(new Long(0));
        }

        if (Util.hasText(cmsMessage.stmtAccNum)){
            ifx.setSubsidiaryAccFrom(cmsMessage.stmtAccNum.trim());
        }

        Currency currency;
        if(cmsMessage.cur == null){
            currency = ProcessContext.get().getRialCurrency();
        }else{
            currency = ProcessContext.get().getCurrency(cmsMessage.cur);
            if (currency == null) {
                Exception e = new Exception("Ivalid Currency Code: " + cmsMessage.cur);
                if (!Util.hasText(ifx.getStatusDesc())) {
                    ifx.setSeverity(Severity.ERROR);
                    ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }


        if (Util.hasText(cmsMessage.secPAN))
            ifx.setSecondAppPan(cmsMessage.secPAN.trim());

        if (Util.hasText(cmsMessage.actSecPAN))
            ifx.setActualSecondAppPAN(cmsMessage.actSecPAN);

        if (Util.hasText(cmsMessage.name))
            ifx.setCardHolderName(convertor.decode(cmsMessage.name.trim().getBytes()));

        if (Util.hasText(cmsMessage.family))
            ifx.setCardHolderFamily(convertor.decode(cmsMessage.family.trim().getBytes()));

        if (Util.hasText(cmsMessage.mainAccNum))
            ifx.setMainAccountNumber(cmsMessage.mainAccNum.trim());


        ifx.setAuth_Currency(currency.getCode());

        //m.rehman: sec currency will set in ifx.copyField() from request message
        //ifx.setSec_Currency(currency.getCode());

//		if (Util.hasText(cmsMessage.curRate))
//			ifx.setAuth_CurRate(cmsMessage.curRate.trim());

//		if (Util.hasText(cmsMessage.secAmt))
//			ifx.setSec_Amt(cmsMessage.secAmt);

//		Integer issuer_currency = cmsMessage.secCurCode;
//		if (Util.hasText(map.containsKey(IfxStatics.IFX_SEC_CUR_CODE))
//			issuer_currency = map.get(IfxStatics.IFX_SEC_CUR_CODE).trim();

//		if (Util.hasText(issuer_currency)) {
//			Currency iCurrency = null;
//			iCurrency = ProcessContext.get().getCurrency(issuer_currency);
//			if (iCurrency == null) {
//				Exception e = new Exception("Ivalid Currency Code: " + acquire_currency);
//				if (!Util.hasText(ifx.getStatusDesc())) {
//					ifx.setSeverity(Severity.ERROR);
//					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
//				}
//				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
//			}
//			ifx.setSec_Currency(iCurrency.getCode());
//		}

//		if (Util.hasText(cmsMessage.secCureRate))
//			ifx.setSec_CurRate(cmsMessage.secCureRate.trim());

        if (Util.hasText(cmsMessage.seqCntr))
            ifx.setSrc_TrnSeqCntr(cmsMessage.seqCntr);

        if (Util.hasText(cmsMessage.mySeqCntr)){
            ifx.setMy_TrnSeqCntr(cmsMessage.mySeqCntr);
        }else{
            ifx.setMy_TrnSeqCntr(cmsMessage.seqCntr);
        }

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_TRN_DT)){
//			Long trnDt = Util.longValueOf(map.get(IfxStatics.IFX_TRN_DT).trim());
//			if (trnDt != null)
//				ifx.setTrnDt(new DateTime(new Date(trnDt)));
//		}
//
        if (cmsMessage.trnDt != null) {
            ifx.setTrnDt(new DateTime(cmsMessage.trnDt));
        }

        if (Util.hasText(cmsMessage.trk2))
            ifx.setTrk2EquivData(cmsMessage.trk2.trim());

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_MSG_AUTH_CODE))
//			ifx.setMsgAuthCode(map.get(IfxStatics.IFX_MSG_AUTH_CODE).trim());

//		try {
//			if (Util.hasText(map.containsKey(IfxStatics.IFX_EXPDT)){
        Long expDt = cmsMessage.expDt;
        if (expDt != null)
            ifx.setExpDt(expDt);
//			}
//		} catch (Exception e) {
//		}

        if (Util.hasText(cmsMessage.cvv2))
            ifx.setCVV2(cmsMessage.cvv2);


        ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);
//		if (Util.hasText(cmsMessage.accTypeTo) {
        ifx.setAccTypeTo((AccType) ToAccType.get(cmsMessage.accTypeTo));
//		}

        ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
//		if (Util.hasText(map.containsKey(IfxStatics.IFX_ACC_TYPE_FROM)) {
        ifx.setAccTypeFrom((AccType) ToAccType.get(cmsMessage.accTypeFr));
//		}

        ifx.setUserLanguage(UserLanguage.FARSI_LANG);
//		if (Util.hasText(map.containsKey(IfxStatics.IFX_USER_LANGUAGE)) {
        ifx.setUserLanguage((UserLanguage) ToUserLang.get(cmsMessage.lang));
//		}

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_TERMINAL_TYPE))
        ifx.setTerminalType((TerminalType) ToTerminalType.get(cmsMessage.termType));


        /**
         * @author khodadi
         */
        if (cmsMessage != null && cmsMessage.origTerminalType != null) {
            TerminalType origTerminalType = (TerminalType) ToTerminalType.get(cmsMessage.origTerminalType);
            ifx.setOrigTerminalType(origTerminalType);
        }

        ifx.setBankId(cmsMessage.bnk);

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_FWD_BANK_ID))
//			ifx.setFwdBankId(cmsMessage.fwdBnk);

        if(cmsMessage.fwdBnk != null){
            ifx.setDestBankId(cmsMessage.fwdBnk);
        }else{
            ifx.setDestBankId(cmsMessage.bnk);
        }

        if(cmsMessage.rcvBnk != null){
            ifx.setRecvBankId(cmsMessage.rcvBnk);
        }else{
            ifx.setRecvBankId(cmsMessage.bnk);
        }

        if (Util.hasText(cmsMessage.orgName)) {
//			String name = cmsMessage.orgName.trim();
//			if (name!= null)
            ifx.setName(convertor.decode(cmsMessage.orgName.getBytes()));
        }

        if (Util.hasText(cmsMessage.netRef))
            ifx.setNetworkRefId(cmsMessage.netRef);

//		Mirkamali: change some data for sorush transfer for CREDIT card
        if (Util.hasText(cmsMessage.term) && !TransactionService.IsSorush(ifx) && !TransactionService.IsSorushReverce(ifx))
            ifx.setTerminalId(cmsMessage.term);

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_CORE_BRANCH_CODE))
//			ifx.setCoreBranchCode(map.get(IfxStatics.IFX_CORE_BRANCH_CODE));

        if (Util.hasText(cmsMessage.pin))
            ifx.setPINBlock(cmsMessage.pin.trim());

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_STATUS_CODE))
//			ifx.setStatusCode(new StatusCode(Integer.parseInt(map.get(IfxStatics.IFX_STATUS_CODE).trim())));

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_STATUS_SEVERITY))
//			ifx.setSeverity((Severity) ToSeverity.get(Integer.parseInt(map.get(IfxStatics.IFX_STATUS_SEVERITY).toString().trim())));

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_STATUS_DESC))
//			ifx.setStatusDesc(map.get(IfxStatics.IFX_STATUS_DESC));

        if (Util.hasText(cmsMessage.errMsg)){
            String error_cause = "";
            try {
                error_cause = new String(("CMS: "+ cmsMessage.errMsg).getBytes(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
            if (Util.hasText(ifx.getStatusDesc()))
                error_cause = ifx.getStatusDesc() + "\r\n"+ error_cause;
            ifx.setStatusDesc(error_cause);
            if (ifx.getSeverity()==null)
                ifx.setSeverity(Severity.INFO);
        }


        try {
//			if (Util.hasText(map.containsKey(IfxStatics.IFX_ORIG_DT))
            ifx.setOrigDt(new DateTime(cmsMessage.origDt));
        } catch (Exception e) {
            ISOException isoe = new ISOException("Unparsable Original Date.", e);
            if (!Util.hasText(ifx.getStatusDesc())) {
                ifx.setSeverity(Severity.ERROR);
                ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }

		//Raza Set Auth-ID-RESP DE-38
        if (!Util.hasText(cmsMessage.authIdResponse))
            ifx.setApprovalCode(String.format("%6s", GlobalContext.getInstance().getAuthIdResponseValue("cms")).replace(' ', '0'));
        else
            ifx.setApprovalCode(cmsMessage.authIdResponse);
//		try {
//			if (Util.hasText(map.containsKey(IfxStatics.IFX_RECIEVED_DT)){
//				Long recievedDate = Util.longValueOf(map.get(IfxStatics.IFX_RECIEVED_DT).trim());
//				if (recievedDate != null)
//					ifx.setReceivedDt(new DateTime(new Date(recievedDate)));
//			}
//		} catch (Exception e) {
//		}
//
//		try {
//			if (Util.hasText(map.containsKey(IfxStatics.IFX_POSTED_DT)){
//				Long postedDate = Util.longValueOf(map.get(IfxStatics.IFX_POSTED_DT).trim());
//				if (postedDate != null)
//					ifx.setPostedDt(new MonthDayDate(new Date(postedDate)));
//			}
//		} catch (Exception e) {
//		}
//
//		try {
//			if (Util.hasText(map.containsKey(IfxStatics.IFX_SETTLE_DT)){
//				Long settleDate = Util.longValueOf(map.get(IfxStatics.IFX_SETTLE_DT).trim());
//				if (settleDate != null)
//					ifx.setSettleDt(new MonthDayDate( new Date(settleDate) ));
//			}
//		} catch (Exception e) {
//		}
//
//		if (Util.hasText(map.containsKey(IfxStatics.IFX_APPROVAL_CODE))
//			ifx.setApprovalCode(map.get(IfxStatics.IFX_APPROVAL_CODE));
//
        if (Util.hasText(cmsMessage.rsCode)) {
            String rsCode = ToErrorCode.get(cmsMessage.rsCode.trim());
            if (Util.hasText(rsCode))
                ifx.setRsCode(rsCode);
            else
                ifx.setRsCode(cmsMessage.rsCode.trim());
        }
//		ifx.setRsCode("51");
        if (Util.hasText(cmsMessage.docNum))
            ifx.setDocumentNumber(cmsMessage.docNum.trim());


        if (Util.hasText(cmsMessage.origSeqCntr)) {
            ifx.getSafeOriginalDataElements().setTrnSeqCounter(cmsMessage.origSeqCntr);
            if (Util.hasText(cmsMessage.origType))
                ifx.getSafeOriginalDataElements().setMessageType(cmsMessage.origType.trim());
            if (Util.hasText(cmsMessage.term))
                ifx.getSafeOriginalDataElements().setTerminalId(cmsMessage.term.trim());
            if (Util.hasText(cmsMessage.PAN))
                ifx.getSafeOriginalDataElements().setAppPAN(cmsMessage.PAN.trim());
            ifx.getSafeOriginalDataElements().setBankId(cmsMessage.origBnk);
            ifx.getSafeOriginalDataElements().setFwdBankId(cmsMessage.origFwdBnk);
        }

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_ORIGINALDATAELEMENTS_ORIG_DT)) {
//				Long refOrigDt = Util.longValueOf(map.get(IfxStatics.IFX_ORIGINALDATAELEMENTS_ORIG_DT).trim());
        if (cmsMessage.origOrigDt != null)
            ifx.getSafeOriginalDataElements().setOrigDt(new DateTime(cmsMessage.origOrigDt));
//		}

//		if (Util.hasText(cmsMessage.origBankId))
//		if (Util.hasText(map.containsKey(IfxStatics.IFX_ORIGINALDATAELEMENTS_FWD_BANK_ID))

        if (Util.hasText(cmsMessage.newAmtAcqCur))
            ifx.setNew_AmtAcqCur(cmsMessage.newAmtAcqCur.trim());

        if (Util.hasText(cmsMessage.newAmtIssCur))
            ifx.setNew_AmtIssCur(cmsMessage.newAmtIssCur.trim());

        if (Util.hasText(cmsMessage.orgNum))
            ifx.setOrgIdNum(cmsMessage.orgNum);


        /***************************************** CMT for delete accBall *****************************************/
//		if(ShetabFinalMessageType.isTransferMessage(ifx.getIfxType())){
//
//			ifx.setAcctBalAvailableType((AccType) ToAcctType.get(cmsMessage.balAvbAcctType));
//
////			ifx.setAcctBalAvailableBalType((BalType) ToBalType.get(cmsMessage.balAvbBalType));
//			ifx.setAcctBalAvailableBalType(BalType.AVAIL);
//
//			if (Util.hasText(cmsMessage.balAvbAmt))
//				ifx.setAcctBalAvailableAmt(cmsMessage.balAvbAmt.trim());
//
//			if (Util.hasText(cmsMessage.balAvbCurCode)){
//				ifx.setAcctBalAvailableCurCode(cmsMessage.balAvbCurCode.trim());
//			}else{
//				ifx.setAcctBalAvailableCurCode(ProcessContext.get().getRialCurrency().getCode().toString());
//			}
//
//			ifx.setAcctBalLedgerType((AccType) ToAcctType.get(cmsMessage.balLdgAcctType));
//
////			ifx.setAcctBalLedgerBalType((BalType) ToBalType.get(cmsMessage.balLdgBalType));
//			ifx.setAcctBalLedgerBalType(BalType.LEDGER);
//
//			if (Util.hasText(cmsMessage.balLdgAmt)){
//				ifx.setAcctBalLedgerAmt(cmsMessage.balLdgAmt.trim());
//			}else{
//				if (Util.hasText(cmsMessage.balAvbAmt))
//					ifx.setAcctBalLedgerAmt(cmsMessage.balAvbAmt.trim());
//			}
//
//			if (Util.hasText(cmsMessage.balLdgCurCode)){
//				ifx.setAcctBalLedgerCurCode(cmsMessage.balLdgCurCode.trim());
//			}else{
//				ifx.setAcctBalLedgerCurCode(ProcessContext.get().getRialCurrency().getCode().toString());
//			}
//		}else if(!ShetabFinalMessageType.isTransferMessage(ifx.getIfxType())){
//			ifx.setTransientAcctBalAvailableType((AccType) ToAcctType.get(cmsMessage.balAvbAcctType));
////			ifx.setTransientAcctBalAvailableBalType((BalType) ToBalType.get(cmsMessage.balAvbBalType));
//			ifx.setTransientAcctBalAvailableBalType(BalType.AVAIL);
//			if (Util.hasText(cmsMessage.balAvbAmt))
//				ifx.setTransientAcctBalAvailableAmt(cmsMessage.balAvbAmt.trim());
//
//			if (Util.hasText(cmsMessage.balAvbCurCode)){
//				ifx.setTransientAcctBalAvailableCurCode(cmsMessage.balAvbCurCode.trim());
//			}else{
//				ifx.setTransientAcctBalAvailableCurCode(ProcessContext.get().getRialCurrency().getCode().toString());
//			}
//
//			ifx.setTransientAcctBalLedgerType((AccType) ToAcctType.get(cmsMessage.balLdgAcctType));
//
////			ifx.setTransientAcctBalLedgerBalType((BalType) ToBalType.get(cmsMessage.balLdgBalType));
//			ifx.setTransientAcctBalLedgerBalType(BalType.LEDGER);
//
//			if (Util.hasText(cmsMessage.balLdgAmt)){
//				ifx.setTransientAcctBalLedgerAmt(cmsMessage.balLdgAmt.trim());
//			}else{
//				if (Util.hasText(cmsMessage.balAvbAmt)){
//					ifx.setTransientAcctBalLedgerAmt(cmsMessage.balAvbAmt.trim());
//				}
//			}
//
//			if (Util.hasText(cmsMessage.balLdgCurCode)){
//				ifx.setTransientAcctBalLedgerCurCode(cmsMessage.balLdgCurCode.trim());
//			}else{
//				ifx.setTransientAcctBalLedgerCurCode(ProcessContext.get().getRialCurrency().getCode().toString());
//			}
//		}
        /*****************************************  *****************************************/

        /*****************************************  *****************************************/
        ifx.setTransientAcctBalAvailableType((AccType) ToAcctType.get(cmsMessage.balAvbAcctType));
        ifx.setTransientAcctBalAvailableBalType(BalType.AVAIL);
        if (Util.hasText(cmsMessage.balAvbAmt))
            ifx.setTransientAcctBalAvailableAmt(cmsMessage.balAvbAmt.trim());
        if (Util.hasText(cmsMessage.balAvbCurCode)){
            ifx.setTransientAcctBalAvailableCurCode(cmsMessage.balAvbCurCode.trim());
        }else{
            ifx.setTransientAcctBalAvailableCurCode(ProcessContext.get().getRialCurrency().getCode().toString());
        }

        ifx.setTransientAcctBalLedgerType((AccType) ToAcctType.get(cmsMessage.balLdgAcctType));


        ifx.setTransientAcctBalLedgerBalType(BalType.LEDGER);

        if (Util.hasText(cmsMessage.balLdgAmt)){
            ifx.setTransientAcctBalLedgerAmt(cmsMessage.balLdgAmt.trim());
        }else{
            if (Util.hasText(cmsMessage.balAvbAmt)){
                ifx.setTransientAcctBalLedgerAmt(cmsMessage.balAvbAmt.trim());
            }
        }

        if (Util.hasText(cmsMessage.balLdgCurCode)){
            ifx.setTransientAcctBalLedgerCurCode(cmsMessage.balLdgCurCode.trim());
        }else{
            ifx.setTransientAcctBalLedgerCurCode(ProcessContext.get().getRialCurrency().getCode().toString());
        }
        /***************************************** *****************************************/


        if (Util.hasText(cmsMessage.newPin))
            ifx.setNewPINBlock(cmsMessage.newPin.trim());

        if (Util.hasText(cmsMessage.oldPin))
            ifx.setOldPINBlock(cmsMessage.oldPin.trim());


        if (Util.hasText(cmsMessage.subAccTo))
            ifx.setSubsidiaryAccTo(cmsMessage.subAccTo.trim());

        if (Util.hasText(cmsMessage.subAccFr))
            ifx.setSubsidiaryAccFrom(cmsMessage.subAccFr.trim());

        /*****************Card Credit Data************/
        ifx.setCreditTotalTransactionAmount(cmsMessage.ccardTrxAmt);
        ifx.setCreditTotalFeeAmount(cmsMessage.ccardFee);
        ifx.setCreditInterest(cmsMessage.ccardInt);
        ifx.setCreditStatementAmount(cmsMessage.ccardStmtAmt);
        ifx.setCreditOpenToBuy(cmsMessage.ccardOTB);
        /*****************Card Credit Data************/

//		if (Util.hasText(map.containsKey(IfxStatics.IFX_P48)) {
//			mapField48(ifx, (map.get(IfxStatics.IFX_P48).trim()));
//		}

        if (Util.hasText(cmsMessage.statement)) {
            CMSHttpToIFXMapper.mapBankStatementData(ifx, cmsMessage.statement.trim());
        }

        if (Util.hasText(cmsMessage.subAccs)) {
            CMSHttpToIFXMapper.mapSubsidiaryData(ifx, cmsMessage.subAccs.trim());
        }

        if (Util.hasText(cmsMessage.shebaCode))
            ifx.setShebaCode(cmsMessage.shebaCode.trim());

        //TASK Task081 : ATM Saham feature
        if (Util.hasText(cmsMessage.shareCode)){
            ifx.setStockCode(cmsMessage.shareCode.trim());
        } else {
            ifx.setStockCode("0");
        }
        
//       gholami(Task45875)
        if(Util.hasText(cmsMessage.shenaseOfTransferToAcc))
        	ifx.setShenaseOfTransferToAccount(cmsMessage.shenaseOfTransferToAcc);

        //TASK Task081 : ATM Saham feature
        if (cmsMessage.shareCount != null){
            ifx.setStockCount(cmsMessage.shareCount);
        } else {
            ifx.setStockCount(0L);//AldTODO Task081 : think
        }

//		if (Util.hasText(ap.containsKey(IfxStatics.IFX_FIRST_TRANSACTION_ID))
        ifx.setFirstTrxId(cmsMessage.trx);

        return ifx;
    }

    @Override
    public byte[] decryptSecureBinaryMessage(byte[] encryptedData, Message incomingMessage) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public byte[] encryptBinaryMessage(byte[] rawdata, Message incomingMessage)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
