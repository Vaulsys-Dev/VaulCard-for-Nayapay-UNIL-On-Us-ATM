package vaulsys.protocols.PaymentSchemes.NAC;

import vaulsys.calendar.DateTime;
import vaulsys.customer.Currency;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOPOSConditionCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class NACISOToIFXMapper extends ISOtoIfxMapper {

    public static final NACISOToIFXMapper Instance = new NACISOToIFXMapper();

    private NACISOToIFXMapper() {}

    Logger logger = Logger.getLogger(this.getClass());

    @Override
    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {

        ISOMsg isoMsg;
        Ifx ifx;
        Currency currency;
        String currencyCode, mti, value;
        Integer time;

        ifx = new Ifx();
        isoMsg = (ISOMsg) message;

        //System.out.println("Setting Institution of Channel [" + ProcessContext.get().getInputMessage().getChannel().getName() + "]"); //Raza TEMP
        ifx.setInstitutionId(ProcessContext.get().getInputMessage().getChannel().getInstitutionId()); //Raza Set Institution in IFX from channel

        mti = isoMsg.getMTI();
        ifx.setMti(mti);

        value = isoMsg.getString(2);
        if (Util.hasText(value))
            if (value.length() % 2 != 0)
                value = value.substring(1, value.length());
        ifx.setAppPAN(value);

        String str_fld3 = isoMsg.getString(3).trim(); //Raza using trim value padded with space
        String emvTrnType = null;
        if (str_fld3 != null && str_fld3.length() == 6) {
            try {
                emvTrnType = str_fld3.substring(0, 2).trim();
                //m.rehman: mapping Acct Types
                ifx.setAccTypeFrom(AccType.mapAcctType(str_fld3.substring(2, 4)));
                ifx.setAccTypeTo(AccType.mapAcctType(str_fld3.substring(4, 6)));
            } catch (NumberFormatException e) {
                ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3, e);
                if (!Util.hasText(ifx.getStatusDesc())) {
                    ifx.setSeverity(Severity.ERROR);
                    ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
                logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            //mapTrnType(ifx, emvTrnType);
        }
        else if(str_fld3 != null && str_fld3.length() == 4)
        {
            try {
                emvTrnType = str_fld3.substring(0, 2).trim();
                ifx.setAccTypeFrom(AccType.mapAcctType(str_fld3.substring(2, 4))); //Raza map as per received value
                ifx.setAccTypeTo(AccType.mapAcctType(str_fld3.substring(2, 4))); //Raza map as per received value
            } catch (NumberFormatException e) {
                ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3, e);
                if (!Util.hasText(ifx.getStatusDesc())) {
                    ifx.setSeverity(Severity.ERROR);
                    ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
                logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            //mapTrnType(ifx, emvTrnType);
        }
        else
        {
            ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3);
            if (!Util.hasText(ifx.getStatusDesc())) {
                ifx.setSeverity(Severity.ERROR);
                ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }

        /*String f_25 = isoMsg.getString(25);
        if (!Util.hasText(f_25)) {
            f_25 = "00";
        }
        mapTerminalType(ifx, f_25);*/
        ifx.setTerminalType(TerminalType.SWITCH);
        //setting POS Condition Code
        ifx.setPosConditionCode(isoMsg.getString(25));

        mapNACTrnType(ifx, emvTrnType);

        try {
            currencyCode = isoMsg.getString(49);
            if (Util.hasText(currencyCode)) {
                currency = ProcessContext.get().getCurrency(Integer.parseInt(currencyCode));
                if (currency == null) {
                    throw new ISOException("Invalid Currency Code: " + currencyCode);
                }
            } else {
                currency = GlobalContext.getInstance().getBaseCurrency();
                if (currency == null) {
                    throw new ISOException("Base Currency not found");
                }
            }

            ifx.setAuth_Currency(currency.getCode());
            ifx.setAuth_CurRate("1");

            if (isoMsg.isRequest()) {
                ifx.setTrx_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
            }

            ifx.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
            ifx.setReal_Amt(Util.longValueOf(isoMsg.getString(4).trim()));

        } catch (Exception e) {
            if (!Util.hasText(ifx.getStatusDesc())) {
                ifx.setSeverity(Severity.ERROR);
                ifx.setStatusDesc(e.getClass().getSimpleName() + ": "
                        + e.getMessage());
            }
            logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        if (!isoMsg.getString(7).equals(""))
            ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));
        else
            ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss",
                    new SimpleDateFormat("MMddHHmmss").format(new Date()))));

        ifx.setSrc_TrnSeqCntr( isoMsg.getString(11));
        ifx.setMy_TrnSeqCntr( isoMsg.getString(11));

        String localTime = isoMsg.getString(12);
        String localDate = isoMsg.getString(13);
        DateTime now = DateTime.now();

        try {
            if (Util.hasText(localDate) && Util.hasText(localTime)) {
                DateTime d = new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
                if (d != null) {
                    if (d.getDayDate().getMonth() == 12 && now.getDayDate().getMonth() == 1) {
                        logger.info("set origDt year to parsal!");
                        d.getDayDate().setYear(now.getDayDate().getYear() - 1);

                    } else if (d.getDayDate().getMonth() == 1 && now.getDayDate().getMonth() == 12) {
                        logger.info("set origDt year to sale dige!");
                        d.getDayDate().setYear(now.getDayDate().getYear() + 1);
                    }
                }

                ifx.setOrigDt(d);

            } else {
                ifx.setOrigDt(now);
            }
        } catch (Exception e) {
            ISOException isoe = new ISOException("Unparsable Original Date.", e);
            if (!Util.hasText(ifx.getStatusDesc())) {
                ifx.setSeverity(Severity.ERROR);
                ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }

        //Setting Time Local Tran
        if (Util.hasText(localTime)) {
            value = localTime;
        } else {
            time = ifx.getOrigDt().getDayTime().getDayTime();
            value = StringUtils.leftPad(time.toString(), 6, "0");
        }
        ifx.setTimeLocalTran(value);

        //Setting Date Local Tran
        if (Util.hasText(localDate)) {
            value = localDate;
        } else {
            value = ifx.getOrigDt().getDayDate().getDate().toString().substring(4,8);
        }
        ifx.setDateLocalTran(value);

        try {
            String expDate = isoMsg.getString(14);
            if(expDate != null && !expDate.equals("")){
                expDate = expDate.trim();
                ifx.setExpDt( Long.parseLong(expDate) );
            }
        }catch (Exception e) {
            logger.info("Exception in setting ExpDate(Field 14)!");
        }

        //setting POS Entry Mode
        value = isoMsg.getString(22);
        if (Util.hasText(value))
            ifx.setPosEntryModeCode(value.substring(1,4));

        value = isoMsg.getString(24);
        if (Util.hasText(value))
            ifx.setNetworkInstId(value.substring(1,4));

        //setting Amount Transaction Fee
        if (Util.hasText(isoMsg.getString(25))) {
            ifx.setPosConditionCode(isoMsg.getString(25));
        }

        //TODO: acquiring institution id code should be according to channel
        //value = ProcessContext.get().getMyInstitution().getBin().toString();
        value = "999910";
        if(Util.hasText(isoMsg.getString(32))) {
            ifx.setBankId(isoMsg.getString(32));

        } else {
            ifx.setBankId(value);
        }

        if(Util.hasText(isoMsg.getString(33))) {
            ifx.setFwdBankId(isoMsg.getString(33));
        }

        ifx.setTrk2EquivData(isoMsg.getString(35));
        if (!Util.hasText(ifx.getAppPAN()) && Util.hasText(ifx.getTrk2EquivData())) {
            value = ifx.getTrk2EquivData();
            if (value.contains("=")) {
                value = value.substring(0, value.indexOf("="));
            } else if (value.contains("D")) {
                value = value.substring(0, value.indexOf("D"));
            }
            ifx.setAppPAN(value);
        }

        if (Util.hasText(ifx.getAppPAN()))
            ifx.setDestBankId(ifx.getAppPAN().substring(0,6));

        value = isoMsg.getString(37);
        if (!Util.hasText(value)) {
            value = ifx.getSrc_TrnSeqCntr() + ifx.getSrc_TrnSeqCntr();
        }
        ifx.setNetworkRefId(value);
        //ifx.setMyNetworkRefId(isoMsg.getString(37));

        if(Util.hasText(isoMsg.getString(38))) {
            ifx.setApprovalCode(isoMsg.getString(38).trim());
        }

        ifx.setTerminalId(isoMsg.getString(41).trim());

        ifx.setOrgIdNum(isoMsg.getString(42).trim());

        //setting Card Acceptor Name Location
        if (Util.hasText(isoMsg.getString(43)))
            ifx.setCardAcceptNameLoc(isoMsg.getString(43));

        if (Util.hasText(isoMsg.getString(45)))
            ifx.setTrack1Data(isoMsg.getString(45));

        if (Util.hasText(isoMsg.getString(48)))
            ifx.setAddDataPrivate(isoMsg.getString(48));

        ifx.setPINBlock (isoMsg.getString(52));
        if (Util.hasText(ifx.getPINBlock()))
            ifx.getSafeEMVRqData().setIsPinAvailable("1");
        else
            ifx.getSafeEMVRqData().setIsPinAvailable("0");

        //using field 47 to save additional amount
        if (Util.hasText(isoMsg.getString(54)))
            ifx.setAddDataNational(isoMsg.getString(54));

        /*if (Util.hasText(isoMsg.getString(55)))
            ifx.setIccCardData(isoMsg.getString(55));*/

        if (Util.hasText(isoMsg.getString(60)))
            ifx.setSelfDefineData(isoMsg.getString(60));

        if (Util.hasText(isoMsg.getString(61)))
            ifx.setOtherAmounts(isoMsg.getString(61));

        if (Util.hasText(isoMsg.getString(62)))
            ifx.setCustomPaymentService(isoMsg.getString(62));

        if (Util.hasText(isoMsg.getString(63)))
            ifx.setNetworkData(isoMsg.getString(63));

        ifx.setMesgSecurityCode(isoMsg.getString(64));

        //mapIfxType(ifx, mti, emvTrnType);
        mapNACIfxType(ifx, mti, emvTrnType);
        
        if (ifx.getIfxType().equals(IfxType.VOID_RQ) || ifx.getIfxType().equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ)
                || ifx.getIfxType().equals(IfxType.REFUND_RQ)
                || ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {

            ifx.setOriginalDataElements(new MessageReferenceData());

            //stan
            if (ifx.getIfxType().equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ)) {
                value = ifx.getApprovalCode();

            } else if (ifx.getIfxType().equals(IfxType.VOID_RQ) || ifx.getIfxType().equals(IfxType.OFFLINE_TIP_ADJUST_REPEAT_RQ)) {
                value = ifx.getNetworkRefId().substring(0, 6);
                //updating ret ref no
                ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr() + ifx.getSrc_TrnSeqCntr());

            } else if (ifx.getIfxType().equals(IfxType.REFUND_RQ)) {
                value = ifx.getCustomPaymentService().substring(0, 6);

            } else if (Util.hasText(ifx.getSrc_TrnSeqCntr())) {
                value = ifx.getSrc_TrnSeqCntr();

            } else {
                value = new String("000000");
            }
            ifx.getSafeOriginalDataElements().setTrnSeqCounter(value);

            /*
            if (Util.hasText(ifx.getNetworkRefId()))
                ifx.getSafeOriginalDataElements().setTrnSeqCounter(ifx.getNetworkRefId().substring(0, 6));
            else if (Util.hasText(ifx.getSrc_TrnSeqCntr()))
                ifx.getSafeOriginalDataElements().setTrnSeqCounter(ifx.getSrc_TrnSeqCntr());
            else
                ifx.getSafeOriginalDataElements().setTrnSeqCounter("000000");
            */

            //date time
            if (ifx.getIfxType().equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ))
                ifx.getSafeOriginalDataElements().setMessageType(ISOMessageTypes.AUTHORIZATION_REQUEST_87);
            else
                ifx.getSafeOriginalDataElements().setMessageType(ISOMessageTypes.FINANCIAL_REQUEST_87);

            try {
                if (ifx.getIfxType().equals(IfxType.VOID_RQ)
                        || ifx.getIfxType().equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ)
                        || ifx.getIfxType().equals(IfxType.OFFLINE_TIP_ADJUST_REPEAT_RQ)) {
                    ifx.getSafeOriginalDataElements().setOrigDt(new DateTime(
                            MyDateFormatNew.parse("MMddHHmmss", ifx.getDateLocalTran() + ifx.getTimeLocalTran())));
                } else {
                    logger.info("TimeLocalTran/DateLocalTran missing");
                }
            } catch (ParseException e) {
                ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.origDt= NULL, OriginalData.TrnSeqCounter = " +
                        ifx.getSafeOriginalDataElements().getTrnSeqCounter()
                        + ", temrinalId= " + ifx.getTerminalId() + ")");
                if (!Util.hasText(ifx.getStatusDesc())) {
                    ifx.setSeverity(Severity.ERROR);
                    ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
                logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }

            //TODO: acquiring institution id code should be according to channel
            //acq inst id
            ifx.getSafeOriginalDataElements().setBankId("999910");
            //forw inst id
            ifx.getSafeOriginalDataElements().setFwdBankId("00000000000");

            ifx.getSafeOriginalDataElements().setTerminalId(ifx.getTerminalId());
            ifx.getSafeOriginalDataElements().setAppPAN(ifx.getAppPAN());
        }

        return ifx;
    }

    public void mapNACTrnType(Ifx ifx, String emvTrnType) {
        //m.rehman: for void transaction from NAC
        String trnCode;
        if (emvTrnType.equals(ISOTransactionCodes.BALANCE_INQUERY)
                && (ifx.getPosConditionCode() != null
                    && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
            )
            trnCode = ISOTransactionCodes.PREAUTH;
        else
            trnCode = emvTrnType;

        mapTrnType(ifx, trnCode);
    }

    public void mapNACIfxType(Ifx ifx, String mti, String emvTrnType) {

        IfxType finalMessageType = null;
        if (mti.equals(ISOMessageTypes.FINANCIAL_REQUEST_87) || mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87)
                || mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_REPEAT_87)) {
            if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)
                    && (ifx.getPosConditionCode() != null
                        && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))) {
                finalMessageType = IfxType.PREAUTH_RQ;
            } else if ((emvTrnType.toString()).equals(ISOTransactionCodes.ADJUSTMENT)
                    && (ifx.getPosConditionCode() != null
                    && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))) {
                finalMessageType = IfxType.VOID_RQ;
            } else if ((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)
                    && (ifx.getPosConditionCode() != null
                    && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))) {
                finalMessageType = IfxType.REFUND_RQ;
            }
        } else if (mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_87) || mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_93)) {
            if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)
                    && (ifx.getPosConditionCode() != null
                        && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))) {
                finalMessageType = IfxType.PREAUTH_COMPLET_ADVICE_RQ;
            } else if ((emvTrnType.toString()).equals(ISOTransactionCodes.ADJUSTMENT)
                    && (ifx.getPosConditionCode() != null
                    && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))) {
                finalMessageType = IfxType.OFFLINE_TIP_ADJUST_REPEAT_RQ;
            }
        } else if (mti.equals(ISOMessageTypes.REVERSAL_REQUEST_87) || mti.equals(ISOMessageTypes.REVERSAL_REQUEST_93)
                || mti.equals(ISOMessageTypes.REVERSAL_ADVICE_87) || mti.equals(ISOMessageTypes.REVERSAL_ADVICE_93)
                || mti.equals(ISOMessageTypes.REVERSAL_ADVICE_REPEAT_87)
                || mti.equals(ISOMessageTypes.REVERSAL_ADVICE_REPEAT_93)) {

            if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)
                    && (ifx.getPosConditionCode() != null
                    && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))) {
                finalMessageType = IfxType.PREAUTH_REV_REPEAT_RQ;

            } else if ((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)
                    && (ifx.getPosConditionCode() != null
                    && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))) {
                finalMessageType = IfxType.REFUND_REVERSAL_REPEAT_RQ;

            }
        }

        if (finalMessageType == null)
            mapIfxType(ifx, mti, emvTrnType);
        else
            ifx.setIfxType(finalMessageType);
    }
}
