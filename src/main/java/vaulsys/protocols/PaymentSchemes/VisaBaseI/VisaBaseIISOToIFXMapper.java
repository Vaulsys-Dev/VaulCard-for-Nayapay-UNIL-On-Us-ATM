package vaulsys.protocols.PaymentSchemes.VisaBaseI;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Currency;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.text.ParseException;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class VisaBaseIISOToIFXMapper extends ISOtoIfxMapper {

    public static final VisaBaseIISOToIFXMapper Instance = new VisaBaseIISOToIFXMapper();

    private VisaBaseIISOToIFXMapper() {}

    Logger logger = Logger.getLogger(this.getClass());

    @Override
    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {

        ISOMsg isoMsg;
        Ifx ifxObj;

        Currency currency;
        String currencyCode, mti, value;
        Long convertedAmount;

        ifxObj = new Ifx();
        isoMsg = (ISOMsg) message;
        value = null;

        System.out.println("Setting Institution of Channel [" + ProcessContext.get().getInputMessage().getChannel().getName() + "]"); //Raza TEMP
        ifxObj.setInstitutionId(ProcessContext.get().getInputMessage().getChannel().getInstitutionId()); //Raza Set Institution in IFX from channel

        mti = isoMsg.getMTI();
        ifxObj.setMti(mti);

        //ifxObj.setAppPAN(StringUtils.stripStart(isoMsg.getString(2), "0"));
        ifxObj.setAppPAN(isoMsg.getString(2));

        String str_fld3 = isoMsg.getString(3);
        String emvTrnType = null;
        if (str_fld3 != null && str_fld3.length() == 6) {
            try {
                emvTrnType = str_fld3.substring(0, 2).trim();
                //m.rehman: mapping Acct Types
                ifxObj.setAccTypeFrom(AccType.mapAcctType(str_fld3.substring(2, 4)));
                ifxObj.setAccTypeTo(AccType.mapAcctType(str_fld3.substring(4, 6)));
            } catch (NumberFormatException e) {
                ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3, e);
                if (!Util.hasText(ifxObj.getStatusDesc())) {
                    ifxObj.setSeverity(Severity.ERROR);
                    ifxObj.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
                logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }

            mapTrnType(ifxObj, emvTrnType);
        }

        try {
            currencyCode = isoMsg.getString(49);
            if (Util.hasText(currencyCode)) {
                currency = ProcessContext.get().getCurrency(Integer.parseInt(currencyCode.substring(1,currencyCode.length())));
                if (currency == null) {
                    throw new ISOException("Invalid Currency Code: " + currencyCode);
                } else {
                    ifxObj.setAuth_Currency(currency.getCode());
                    ifxObj.setAuth_CurRate(Double.toString(currency.getExchangeRate()));

                    if (isoMsg.isRequest()) {
                        ifxObj.setTrx_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
                    }

                    ifxObj.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
                    ifxObj.setReal_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
                }
            }
        } catch (Exception e) {
            if (!Util.hasText(ifxObj.getStatusDesc())) {
                ifxObj.setSeverity(Severity.ERROR);
                ifxObj.setStatusDesc(e.getClass().getSimpleName() + ": "
                        + e.getMessage());
            }
            logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        try {
            if (Util.hasText(isoMsg.getString(51))) {
                currencyCode = isoMsg.getString(51).trim();
                currency = ProcessContext.get().getCurrency(Integer.parseInt(currencyCode.substring(1, currencyCode.length())));
                if (currency == null) {
                    throw new ISOException("Invalid Currency Code: " + currencyCode);
                } else {
                    ifxObj.setSec_Currency(currency.getCode());
                    ifxObj.setSec_CurRate(isoMsg.getString(10).trim());
                    ifxObj.setSec_Amt(Util.longValueOf(isoMsg.getString(6)));
                }
            } else {
                if (ISOMessageTypes.isRequestMessage(mti)) {
                    if (ISOTransactionCodes.isFinancialTransaction(emvTrnType) && !(emvTrnType.equals(ISOTransactionCodes.REFUND))) {

                        if (ifxObj.getSett_Currency() != null) {
                            if (ifxObj.getSett_Currency().equals(GlobalContext.getInstance().getBaseCurrency().getCode())) {
                                ifxObj.setSec_Amt(ifxObj.getSett_Amt());
                                ifxObj.setSec_Currency(Integer.parseInt(ifxObj.getSett_Currency()));
                                ifxObj.setSec_CurRate(ifxObj.getConvRate_Sett().toString());

                            } else if (!ifxObj.getSett_Currency().equals(GlobalContext.getInstance().getBaseCurrency().getCode())) {
                                currency = ProcessContext.get().getCurrency(Integer.parseInt(ifxObj.getSett_Currency()));
                                convertedAmount = ifxObj.getSett_Amt() * currency.getExchangeRate();
                                ifxObj.setSec_Amt(convertedAmount);
                                ifxObj.setSec_Currency(GlobalContext.getInstance().getBaseCurrency().getCode());
                                ifxObj.setSec_CurRate(Long.toString(GlobalContext.getInstance().getBaseCurrency().getExchangeRate()));

                            } else if (!ifxObj.getAuth_Currency().equals(GlobalContext.getInstance().getBaseCurrency().getCode())) {
                                currency = ProcessContext.get().getCurrency(ifxObj.getAuth_Currency());
                                convertedAmount = ifxObj.getAuth_Amt() * currency.getExchangeRate();
                                ifxObj.setSec_Amt(convertedAmount);
                                ifxObj.setSec_Currency(GlobalContext.getInstance().getBaseCurrency().getCode());
                                ifxObj.setSec_CurRate(Long.toString(GlobalContext.getInstance().getBaseCurrency().getExchangeRate()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (!Util.hasText(ifxObj.getStatusDesc())) {
                ifxObj.setSeverity(Severity.ERROR);
                ifxObj.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        if (!isoMsg.getString(7).equals(""))
            ifxObj.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

        ifxObj.setSrc_TrnSeqCntr( isoMsg.getString(11));
        ifxObj.setMy_TrnSeqCntr( isoMsg.getString(11));

        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();
        DateTime now = DateTime.now();

        try {
            if (Util.hasText(localDate) && Util.hasText(localTime)) {
                DateTime d = new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
                if (d != null && ProcessContext.get().getMyInstitution().getBin().equals(Long.valueOf(isoMsg.getString(32).trim()))
                        && FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {

                    if (d.getDayDate().getMonth() == 12 && now.getDayDate().getMonth() == 1) {
                        logger.info("set origDt year to parsal!");
                        d.getDayDate().setYear(now.getDayDate().getYear() - 1);

                    } else if (d.getDayDate().getMonth() == 1 && now.getDayDate().getMonth() == 12) {
                        logger.info("set origDt year to sale dige!");
                        d.getDayDate().setYear(now.getDayDate().getYear() + 1);
                    }
                }

                ifxObj.setOrigDt(d);

            } else {
                //ifxObj.setOrigDt(now);
                ifxObj.setOrigDt(ifxObj.getTrnDt());
            }
        } catch (Exception e) {
            ISOException isoe = new ISOException("Unparsable Original Date.", e);
            if (!Util.hasText(ifxObj.getStatusDesc())) {
                ifxObj.setSeverity(Severity.ERROR);
                ifxObj.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }

        //Setting Time Local Tran
        if (Util.hasText(localTime)) {
            ifxObj.setTimeLocalTran(localTime);
        }

        //Setting Date Local Tran
        if (Util.hasText(localDate)) {
            ifxObj.setDateLocalTran(localDate);
        }

        try {

            String expDate = isoMsg.getString(14);
            if(expDate != null && !expDate.equals("")){
                expDate = expDate.trim();
                ifxObj.setExpDt( Long.parseLong(expDate) );
            }
        }catch (Exception e) {
            logger.info("Exception in setting ExpDate(Field 14)!");
        }

        //Setting Merchant Type
        ifxObj.setMerchantType(isoMsg.getString(18));

        //setting Merchant Country Code
        value = isoMsg.getString(19);
        if (Util.hasText(value)) {
            ifxObj.setMerchCountryCode(value.substring(1,value.length()));
        }

        //setting POS Entry Mode
        ifxObj.setPosEntryModeCode(isoMsg.getString(22));

        //setting Card Sequence Number
        /*value = isoMsg.getString(23);
        if (Util.hasText(value)) {
            ifxObj.setCardSequenceNo(value.substring(1, value.length()));
        }*/

        String f_25 = isoMsg.getString(25);
        if (Util.hasText(f_25)) {
            mapTerminalType(ifxObj, f_25);

            //setting POS Condition Code
            ifxObj.setPosConditionCode(f_25);
        }

        //setting POS Pin Capture Code
        if (Util.hasText(isoMsg.getString(26))) {
            ifxObj.setPosPinCaptureCode(isoMsg.getString(26));
        }

        //setting Amount Transaction Fee
        if (Util.hasText(isoMsg.getString(28))) {
            ifxObj.setAmountTranFee(isoMsg.getString(28));
        }

        value = isoMsg.getString(32);
        if(Util.hasText(value)) {
            //ifxObj.setBankId(Long.valueOf(StringUtils.stripStart(value, "0")));
            ifxObj.setBankId(value);
            ifxObj.setDestBankId(value);
        }

        value = isoMsg.getString(33);
        if(Util.hasText(value)) {
            //value = StringUtils.stripStart(value, "0");
            ifxObj.setFwdBankId(value);
            //ifxObj.setDestBankId(Long.valueOf(value));
        }

        value = isoMsg.getString(35);
        if (Util.hasText(value)) {
            //ifxObj.setTrk2EquivData(StringUtils.stripStart(value, "0"));
            ifxObj.setTrk2EquivData(value);
        }

        ifxObj.setMyNetworkRefId(isoMsg.getString(37));

        if (Util.hasText(isoMsg.getString(38))) {
            ifxObj.setApprovalCode(isoMsg.getString(38));
        }

        if (!Util.hasText(ifxObj.getRsCode())) {
            ifxObj.setRsCode(mapError(isoMsg.getString(39)));
        }

        ifxObj.setTerminalId(isoMsg.getString(41));
        ifxObj.setOrgIdNum(isoMsg.getString(42));
        ifxObj.setCardAcceptNameLoc(isoMsg.getString(43));

        ifxObj.setAddResponseData(isoMsg.getString(44));

        if (!Util.hasText(isoMsg.getString(45))) {
            ifxObj.setTrack1Data(isoMsg.getString(45));
        }

        if (isoMsg.hasField(48)) {
            ifxObj.setAddDataPrivate(isoMsg.getString(48));
        }

        ifxObj.setPINBlock ( isoMsg.getString(52));

        ifxObj.setSecRelatedControlInfo(isoMsg.getString(53));

        String P54 = isoMsg.getString(54);

        if (ISOResponseCodes.APPROVED.equals(ifxObj.getRsCode())) {
            while (P54 != null && P54.length() >= 20) {
                AcctBal acctBal = new AcctBal();

                Integer acctType = Integer.parseInt(P54.substring(0, 2));

                switch (acctType) {
                    case 1:
                        acctBal.setAcctType(AccType.CURRENT);
                        break;
                    case 2:
                        acctBal.setAcctType(AccType.SAVING);
                        break;
                    default:
                        acctBal.setAcctType(AccType.UNKNOWN);
                        break;
                }

                Integer amtType = null;
                try {
                    amtType = Integer.parseInt(P54.substring(2, 4));
                } catch (NumberFormatException e) {
                    ISOException isoe = new ISOException("Bad Format: Amount Type [field 54]", e);
                    if (!Util.hasText(ifxObj.getStatusDesc())) {
                        ifxObj.setSeverity(Severity.ERROR);
                        ifxObj.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                    }
                    logger.error(isoe.getClass().getSimpleName() + ": "	+ isoe.getMessage());
                }

                switch (amtType) {
                    case 1:
                        acctBal.setBalType(BalType.LEDGER);
                        ifxObj.setTransientAcctBalLedger(acctBal);
                        break;
                    case 2:
                        acctBal.setBalType(BalType.AVAIL);
                        ifxObj.setTransientAcctBalAvailable(acctBal);
                        break;
                    default:
                        acctBal.setBalType(BalType.UNKNOWN);
                        break;
                }

                acctBal.setCurCode(P54.substring(4, 7));
                acctBal.setAmt(P54.substring(7, 20));

                P54 = P54.substring(20);
            }
        }

        //setting chip data Field
        /*if (Util.hasText(isoMsg.getString(55))) {
            ifxObj.setIccCardData(isoMsg.getString(55));
        }*/

        if (!Util.hasText(f_25.trim()) && ifxObj.getTerminalId() != null) {
            if (ProcessContext.get().getMyInstitution().getBin().equals(ifxObj.getBankId()) &&
                    FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole()) &&
                    ISOMessageTypes.isResponseMessage(ifxObj.getIfxType())) {
                TerminalType terminalType = GlobalContext.getInstance().getTerminalType(ifxObj.getTerminalId());
                if (ifxObj.getTerminalId() != null && terminalType != null && TerminalType.isPhisycalDeviceTerminal(terminalType)){
                    ifxObj.setTerminalType(terminalType);
                }
            }
        }

        //setting Self Defined Field
        if (Util.hasText(isoMsg.getString(60))) {
            ifxObj.setSelfDefineData(isoMsg.getString(60));
        }

        //setting Other Amounts Field
        if (Util.hasText(isoMsg.getString(61))) {
            ifxObj.setOtherAmounts(isoMsg.getString(61));
        }

        //setting Custom Payment Service
        if (Util.hasText(isoMsg.getString(62))) {
            ifxObj.setCustomPaymentService(isoMsg.getString(62));
        }

        //setting Network Management Info Code
        value = isoMsg.getString(70);
        if (Util.hasText(value)) {
            ifxObj.setNetworkManageInfoCode(value.substring(1, value.length()));
        }

        String S90 = isoMsg.getString(90);
        if (S90 != null && S90.length() >= 20) {
            ifxObj.setOriginalDataElements(new MessageReferenceData());
            ifxObj.getSafeOriginalDataElements().setTrnSeqCounter(S90.substring(4, 10));

            String msgType = S90.substring(0, 4);
            if (Integer.parseInt(msgType) != 0)
                ifxObj.getSafeOriginalDataElements().setMessageType ( msgType);
            else{
                ISOException isoe = new ISOException("Invalid Format( F_90: "+
                        " OriginalData.msgType= NULL, OriginalData.TrnSeqCounter = "+ ifxObj.getSafeOriginalDataElements().getTrnSeqCounter()+", temrinalId= "+ ifxObj.getTerminalId() +")");
                if (!Util.hasText(ifxObj.getStatusDesc())) {
                    ifxObj.setSeverity(Severity.ERROR);
                    ifxObj.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
                }
                logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }

            String origDt = S90.substring(10, 20);
            if (Integer.parseInt(origDt) != 0) {
                try {
                    ifxObj.getSafeOriginalDataElements().setOrigDt(new DateTime( MyDateFormatNew.parse("MMddHHmmss", origDt)));
                } catch (ParseException e) {
                    ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.origDt= NULL, OriginalData.TrnSeqCounter = "+
                            ifxObj.getSafeOriginalDataElements().getTrnSeqCounter()
                            +", temrinalId= "+ ifxObj.getTerminalId() +")");
                    if (!Util.hasText(ifxObj.getStatusDesc())) {
                        ifxObj.setSeverity(Severity.ERROR);
                        ifxObj.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
                    }
                    logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
            }

            String bankId = S90.substring(20, 31).trim();
            if (Long.parseLong(bankId) != 0)
                ifxObj.getSafeOriginalDataElements().setBankId (bankId);
            else {
                ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.bankId= NULL, OriginalData.TrnSeqCounter = "+
                        ifxObj.getSafeOriginalDataElements().getTrnSeqCounter()
                        +", temrinalId= "+ ifxObj.getTerminalId() +", OriginalData.origDt= "+ ifxObj.getSafeOriginalDataElements().getOrigDt()+ ")");
                if (!Util.hasText(ifxObj.getStatusDesc())) {
                    ifxObj.setSeverity(Severity.ERROR);
                    ifxObj.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
                }
                logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }

            String fwdBankId = S90.substring(31);
            ifxObj.getSafeOriginalDataElements().setFwdBankId (fwdBankId);
            /*
            if (Long.parseLong(fwdBankId) != 0)
                ifxObj.getSafeOriginalDataElements().setFwdBankId ( Util.longValueOf(fwdBankId));
            else{
                ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.FwdBankId = NULL, OriginalData.TrnSeqCounter = "+
                        ifxObj.getSafeOriginalDataElements().getTrnSeqCounter()
                        +", OriginalData.temrinalId= "+ ifxObj.getTerminalId() +", OriginalData.origDt= "+ ifxObj.getSafeOriginalDataElements().getOrigDt()+
                        ", OriginalData.bankId ="+ ifxObj.getSafeOriginalDataElements().getBankId() +")" );
                if (!Util.hasText(ifxObj.getStatusDesc())) {
                    ifxObj.setSeverity(Severity.ERROR);
                    ifxObj.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
                }
                logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            */

            ifxObj.getOriginalDataElements().setTerminalId(ifxObj.getTerminalId());
            ifxObj.getOriginalDataElements().setAppPAN(ifxObj.getAppPAN());
        }

        String S95 = isoMsg.getString(95);
        if (S95 != null && S95.length() >= 24) {
            ifxObj.setNew_AmtAcqCur(S95.substring(0, 12));
            ifxObj.setNew_AmtIssCur(S95.substring(12, 24));
            Long real_Amt = Util.longValueOf(ifxObj.getNew_AmtAcqCur());
            real_Amt = (real_Amt!=null && !real_Amt.equals(0L))? real_Amt :Util.longValueOf(ifxObj.getNew_AmtIssCur());
            if (real_Amt!= null && !real_Amt.equals(0L))
                ifxObj.setReal_Amt(real_Amt);
        }

        value = isoMsg.getString(100);
        if (Util.hasText(value)) {
            //ifxObj.setRecvBankId(Util.longValueOf(StringUtils.stripStart(value, "0")));
            ifxObj.setRecvBankId(value);
        }

        if (Util.hasText(isoMsg.getString(102))) {
            ifxObj.setAccountId1(isoMsg.getString(102));
        }

        if (Util.hasText(isoMsg.getString(103))) {
            ifxObj.setAccountId2(isoMsg.getString(103));
        }

        //setting private data
        if (isoMsg.getComponent(126) != null) {
            //checking for cvv2
            value = ((ISOMsg) isoMsg.getComponent(126)).getString(10);
            if (Util.hasText(value)) {
                ifxObj.getSafeCardAcctId().setCVV2(value);
            }
        }
        /*
        if (Util.hasText(isoMsg.getString(126))) {
            ifxObj.setSchemePrivateUse(isoMsg.getString(126));
        }*/

        if (ifxObj.getIfxType()!= null && !ISOMessageTypes.isResponseMessage(ifxObj.getIfxType())
                && ifxObj.getTerminalType() ==null ){
            ISOException isoe = new ISOException("Invalid terminal type code: " + Integer.parseInt("0"+f_25.trim()));
            if (!Util.hasText(ifxObj.getStatusDesc())) {
                ifxObj.setSeverity(Severity.ERROR);
                ifxObj.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }

        mapIfxType(ifxObj, mti, emvTrnType);

        return ifxObj;
    }
}
