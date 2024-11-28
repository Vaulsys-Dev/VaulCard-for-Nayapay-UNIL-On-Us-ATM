package vaulsys.protocols.PaymentSchemes.VisaSMS;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.customer.Currency;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import java.text.ParseException;

/**
 * Created by HP on 11/23/2016.
 */
public class VisaSMSISOToIFXMapper extends ISOtoIfxMapper {
    public static final VisaSMSISOToIFXMapper Instance = new VisaSMSISOToIFXMapper();

    private VisaSMSISOToIFXMapper() {}

    Logger logger = Logger.getLogger(this.getClass());

    @Override
    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {

        ISOMsg isoMsg;
        Ifx ifxObj;
        Currency currency;
        String currencyCode, mti;
        Long convertedAmount;
//        byte[] headerData;
//        byte[] rejectCode;
//        Integer headerDataLength;

        ifxObj = new Ifx();
        isoMsg = (ISOMsg) message;

        //check header data, if message is request message and reject code in header is Format error
        //change the MTI
        mti = isoMsg.getMTI();
        ifxObj.setMti(mti);

        // ISO Field 2, PAN
        ifxObj.setAppPAN(StringUtils.stripStart(isoMsg.getString(2), "0"));

        //ISO Field 3, Processing Code
        String str_fld3 = isoMsg.getString(3);
        String emvTrnType = null;
        if (str_fld3 != null && str_fld3.length() == 6) {
            try
            {
                emvTrnType = str_fld3.substring(0, 2).trim();
                //m.rehman: mapping Acct Types
                //ISO Field 3, Processing Code; setting From Account and To Account
                ifxObj.setAccTypeFrom(AccType.mapAcctType(str_fld3.substring(2, 4)));
                ifxObj.setAccTypeTo(AccType.mapAcctType(str_fld3.substring(4, 6)));
            }
            catch (NumberFormatException e)
            {
                ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3, e);

                if (!Util.hasText(ifxObj.getStatusDesc()))
                {
                    ifxObj.setSeverity(Severity.ERROR);
                    ifxObj.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
                logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }

            //ISO Field 3, Processing Code; setting Transaction Type
            mapTrnType(ifxObj, emvTrnType);
        }


        if(emvTrnType == ISOTransactionCodes.WITHDRAWAL && mti != ISOMessageTypes.REVERSAL_ADVICE_87.toString())
        {
            //ISO Field 4, Amount Transaction
            ifxObj.setTrx_Amt(Util.longValueOf(isoMsg.getString(4).trim()));

            //ISO Field 38, Auth ID Response
            if (Util.hasText(isoMsg.getString(38))) {
                ifxObj.setApprovalCode(isoMsg.getString(38));
            }
        }

        if(isoMsg.getMTI().equals(ISOMessageTypes.FINANCIAL_REQUEST_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.REVERSAL_ADVICE_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.REVERSAL_RESPONSE_87) ||
                isoMsg.getMTI().equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87))
        {
            //ISO Field 7, Transmission Date Time
            if (!isoMsg.getString(7).equals(""))
                ifxObj.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

            //ISO Field 11, System Trace Audit Number
            ifxObj.setSrc_TrnSeqCntr( isoMsg.getString(11));
            ifxObj.setMy_TrnSeqCntr( isoMsg.getString(11));

            //ISO Field 37, Reterival Reference Number (RRN)
            ifxObj.setMyNetworkRefId(isoMsg.getString(37));

            //ISO Field 39, Response Code
            if (!Util.hasText(ifxObj.getRsCode())) {
                ifxObj.setRsCode(mapError(isoMsg.getString(39)));
            }

            //ISO Field 63, Bitmap Field 63
            String P63 = isoMsg.getString(63);
			//ifxObj.setVipPrivateUse(P63); //Raza from TPSP
            ifxObj.setNetworkData(P63);

            //setting Network Management Info Code
            if (Util.hasText(isoMsg.getString(70))) {
                ifxObj.setNetworkManageInfoCode(isoMsg.getString(70));
            }
        }

        //ISO Field 15, Date Settlement
        try {
            if (Util.hasText(isoMsg.getString(15)))
                ifxObj.setSettleDt(new MonthDayDate(MyDateFormatNew.parse("MMdd", isoMsg.getString(15))));
        } catch (Exception e) {
            logger.info("Exception in setting settleDate(15)!");
        }

        //ISO Field 19, Accquiring Instition Country Code
        if (Util.hasText(isoMsg.getString(19))) {
            ifxObj.setMerchCountryCode(isoMsg.getString(19));
        }

        //ISO Field 25, POS Condition Code
        String f_25 = isoMsg.getString(25);
        if (Util.hasText(f_25)) {
            mapTerminalType(ifxObj, f_25);
            ifxObj.setPosConditionCode(isoMsg.getString(25));
        }

        //ISO Field 32, Accquiring Instition ID Code
        if(Util.hasText(isoMsg.getString(32)))
            ifxObj.setBankId(StringUtils.stripStart(isoMsg.getString(32), "0"));

        //ifxObj.setBankId(StringUtils.stripStart(isoMsg.getString(32), "0"));

        //ISO Field 41, Card Acceptor Terminal ID
        ifxObj.setTerminalId(isoMsg.getString(41));

        //ISO Field 42, Card Acceptor ID Code
        ifxObj.setOrgIdNum(isoMsg.getString(42));

        if(mti != ISOMessageTypes.REVERSAL_ADVICE_87.toString()) {
            //ISO Field 49, Currency Code Transaction
            currency = null;
            currencyCode = null;
            try {
                currencyCode = isoMsg.getString(49);
                if (Util.hasText(currencyCode)) {
                    currency = ProcessContext.get().getCurrency(Integer.parseInt(currencyCode));
                    if (currency == null) {
                        throw new ISOException("Invalid Currency Code: " + currencyCode);
                    } else {
                        ifxObj.setAuth_Currency(currency.getCode());
                    }
                }
            } catch (Exception e) {
                if (!Util.hasText(ifxObj.getStatusDesc())) {
                    ifxObj.setSeverity(Severity.ERROR);
                    ifxObj.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            //ISO Field 54, Additional Amounts
            String P54 = StringUtils.stripStart(isoMsg.getString(54), "0");

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
                        logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
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
        }

        //ISO Field 62, Bitmap Field 62
        String P62 = isoMsg.getString(62);
        ifxObj.setCustomPaymentService(P62);

        if(mti == ISOMessageTypes.REVERSAL_ADVICE_87.toString())
        {
            //ISO Field 90, Original Data Elements
            String S90 = isoMsg.getString(90);
            if (S90 != null && S90.length() >= 20) {
                ifxObj.setOriginalDataElements(new MessageReferenceData());
                ifxObj.getSafeOriginalDataElements().setTrnSeqCounter( ISOUtil.zeroUnPad(S90.substring(4, 10)));

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
                if (Integer.parseInt(bankId) != 0)
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
                if (Integer.parseInt(fwdBankId) != 0)
                    ifxObj.getSafeOriginalDataElements().setFwdBankId (fwdBankId);
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

                ifxObj.getOriginalDataElements().setTerminalId(ifxObj.getTerminalId());
                ifxObj.getOriginalDataElements().setAppPAN(ifxObj.getAppPAN());
            }
        }

        //ISO Field 33, Forwarding Institution Identification Code
        if(Util.hasText(isoMsg.getString(33))) {
            ifxObj.setFwdBankId(StringUtils.stripStart(isoMsg.getString(33), "0").trim());
            ifxObj.setDestBankId(StringUtils.stripStart(isoMsg.getString(33), "0").trim());
        }

        //ISO Field 48, Additional Data
        if (isoMsg.hasField(48)) {
            ifxObj.setAddDataPrivate(StringUtils.stripStart(isoMsg.getString(48), "0"));
        }

        //ISO Field 53, Security-Related Control Information
        ifxObj.setSecRelatedControlInfo(isoMsg.getString(53));

        //ISO Field 96, Message Security Code
        if (Util.hasText(isoMsg.getString(96))) {
            ifxObj.setMesgSecurityCode(isoMsg.getString(96));
        }

        //ISO Field 100, Receiving Institution Identification Code
        ifxObj.setRecvBankId(StringUtils.stripStart(isoMsg.getString(100), "0"));

        //ISO Field 102, Account ID 1
        ifxObj.setMainAccountNumber(StringUtils.stripStart(isoMsg.getString(102), "0"));

        currency = null;
        currencyCode = null;
        try {
            if (Util.hasText(isoMsg.getString(50))) {
                currencyCode = isoMsg.getString(50);
                currency = ProcessContext.get().getCurrency(Integer.parseInt(currencyCode));
                if (currency == null) {
                    throw new ISOException("Invalid Currency Code: " + currencyCode);
                } else {
                    ifxObj.setSett_Currency(isoMsg.getString(50));
                    ifxObj.setSett_Amt(Util.longValueOf(isoMsg.getString(5)));
                    ifxObj.setConvRate_Sett(isoMsg.getString(9));
                    //ifxObj.setCurrCodeSettlement(isoMsg.getString(50));
                    //ifxObj.setAmountSettlement(Util.longValueOf(isoMsg.getString(5)));
                    //ifxObj.setConvRateSettlement(isoMsg.getString(9));
                }
            }
        } catch (Exception e) {
            if (!Util.hasText(ifxObj.getStatusDesc())) {
                ifxObj.setSeverity(Severity.ERROR);
                ifxObj.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        currency = null;
        currencyCode = null;
        try {
            if (Util.hasText(isoMsg.getString(51))) {
                currencyCode = isoMsg.getString(51).trim();
                currency = ProcessContext.get().getCurrency(Integer.parseInt(currencyCode));
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

        //Setting Date Conversion
        if (Util.hasText(isoMsg.getString(16))) {
            ifxObj.setSec_CurDate(isoMsg.getString(16));
        }

        //setting Card Acceptor Name Location
        ifxObj.setCardAcceptNameLoc(isoMsg.getString(43));

        return ifxObj;
    }
}
