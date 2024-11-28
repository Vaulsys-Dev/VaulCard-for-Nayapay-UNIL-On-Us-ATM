package vaulsys.protocols.PaymentSchemes.ISOHost;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class ISOHostISOToIFXMapper extends ISOtoIfxMapper {

    public static final ISOHostISOToIFXMapper Instance = new ISOHostISOToIFXMapper();

    private ISOHostISOToIFXMapper() {}

    Logger logger = Logger.getLogger(this.getClass());

    @Override
    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {

        ISOMsg isoMsg;
        Ifx ifxObj;
        String mti, emvTrnType;

        ifxObj = new Ifx();
        isoMsg = (ISOMsg) message;

        mti = isoMsg.getMTI();
        ifxObj.setMti(mti);

        ifxObj.setAppPAN(isoMsg.getString(2));

        String str_fld3 = isoMsg.getString(3);
        emvTrnType = null;
        if (str_fld3 != null && str_fld3.length() == 6) {
            try {
                emvTrnType = str_fld3.substring(0, 2);
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

        ifxObj.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
        ifxObj.setReal_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
        ifxObj.setTrx_Amt(Util.longValueOf(isoMsg.getString(4).trim()));

        ifxObj.setSett_Amt(Util.longValueOf(isoMsg.getString(5)));

        ifxObj.setSec_Amt(Util.longValueOf(isoMsg.getString(6)));

        if (!isoMsg.getString(7).equals(""))
            ifxObj.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

        ifxObj.setConvRate_Sett(isoMsg.getString(9));

        ifxObj.setSec_CurRate(isoMsg.getString(10));

        ifxObj.setSrc_TrnSeqCntr( isoMsg.getString(11));
        ifxObj.setMy_TrnSeqCntr( isoMsg.getString(11));

        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();
        DateTime d;
        DateTime now = DateTime.now();

        try {
            if (Util.hasText(localDate) && Util.hasText(localTime)) {
                d = new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
                ifxObj.setOrigDt(d);

            } else if (ifxObj.getTrnDt() != null) {
                ifxObj.setOrigDt(ifxObj.getTrnDt());
            } else {
                ifxObj.setOrigDt(now);
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
        if (Util.hasText(localTime))
            ifxObj.setTimeLocalTran(localTime);

        //Setting Date Local Tran
        if (Util.hasText(localDate))
            ifxObj.setDateLocalTran(localDate);

        try {
            String expDate = isoMsg.getString(14);
            if(expDate != null && !expDate.equals("")){
                expDate = expDate.trim();
                ifxObj.setExpDt( Long.parseLong(expDate) );
            }
        }catch (Exception e) {
            logger.info("Exception in setting ExpDate(Field 14)!");
        }

        try {
            if (Util.hasText(isoMsg.getString(15)))
                ifxObj.setSettleDt(new MonthDayDate(MyDateFormatNew.parse("MMdd", isoMsg.getString(15))));
        } catch (Exception e) {
            logger.info("Exception in setting settleDate(15)!");
        }

        if (Util.hasText(isoMsg.getString(16)))
            ifxObj.setSec_CurDate(isoMsg.getString(16));

        if (Util.hasText(isoMsg.getString(28)))
            ifxObj.setAmountTranFee(isoMsg.getString(28));

        if(Util.hasText(isoMsg.getString(32))) {
            ifxObj.setBankId(isoMsg.getString(32));
        }
        if(Util.hasText(isoMsg.getString(33))) {
            ifxObj.setFwdBankId(isoMsg.getString(33).trim());
            //ifxObj.setDestBankId(isoMsg.getString(33).trim());
        }
        ifxObj.setDestBankId(ifxObj.getAppPAN().substring(0,6));

        ifxObj.setTrk2EquivData(isoMsg.getString(35));

        ifxObj.setMyNetworkRefId(isoMsg.getString(37));

        ifxObj.setRsCode(mapError(isoMsg.getString(39)));

        mapFieldANFix(ifxObj, isoMsg, 41);
        mapFieldANFix(ifxObj, isoMsg, 42);

        if (Util.hasText(isoMsg.getString(49)))
            ifxObj.setAuth_Currency(Integer.parseInt(isoMsg.getString(49)));

        if (Util.hasText(isoMsg.getString(50)))
            ifxObj.setSett_Currency(isoMsg.getString(50));

        if (Util.hasText(isoMsg.getString(51)))
            ifxObj.setSec_Currency(Integer.parseInt(isoMsg.getString(51)));

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

        if (ifxObj.getTerminalId() != null) {
            if (ProcessContext.get().getMyInstitution().getBin().equals(ifxObj.getBankId()) &&
                    FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole()) &&
                    ISOFinalMessageType.isResponseMessage(ifxObj.getIfxType())) {
                TerminalType terminalType = GlobalContext.getInstance().getTerminalType(ifxObj.getTerminalId());
                if (ifxObj.getTerminalId() != null && terminalType != null && TerminalType.isPhisycalDeviceTerminal(terminalType)){
                    ifxObj.setTerminalType(terminalType);
                }
            }
        }

        //setting Network Management Info Code
        if (Util.hasText(isoMsg.getString(70)))
            ifxObj.setNetworkManageInfoCode(isoMsg.getString(70));

        ifxObj.setRecvBankId(isoMsg.getString(100));

        mapIfxType(ifxObj, mti, emvTrnType);

        return ifxObj;
    }
}
