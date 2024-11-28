package vaulsys.protocols.PaymentSchemes.UnionPay;

import vaulsys.protocols.PaymentSchemes.base.ISOPOSEntryMode;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOPOSConditionCodes;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class UnionPayIFXToISOMapper extends IfxToISOMapper {

    transient Logger logger = Logger.getLogger(UnionPayIFXToISOMapper.class);
    public static final UnionPayIFXToISOMapper Instance = new UnionPayIFXToISOMapper();

    protected UnionPayIFXToISOMapper() {
    }

    @Override
    public ProtocolMessage map(Ifx ifxObj, EncodingConvertor convertor) throws Exception {

        String value;
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager isoPackager = ((vaulsys.protocols.PaymentSchemes.UnionPay.UnionPayProtocol) ProtocolProvider.Instance.getByClass(vaulsys.protocols.PaymentSchemes.UnionPay.UnionPayProtocol.class)).getPackager();
        isoMsg.setPackager(isoPackager);

        isoMsg.setMTI(ifxObj.getMti());

        if (ifxObj.getAppPAN() != null)
            isoMsg.set(2, ifxObj.getAppPAN());

        String processCode = "";
        if (ifxObj.getAccTypeFrom().equals(AccType.MAIN_ACCOUNT))
            processCode = "00";
        else
            processCode = Integer.toString(ifxObj.getAccTypeFrom().getType());

        if (ifxObj.getAccTypeTo().equals(AccType.MAIN_ACCOUNT))
            processCode += "00";
        else
            processCode += Integer.toString(ifxObj.getAccTypeTo().getType());

        isoMsg.set(3, mapUnionPayTrnType(ifxObj.getTrnType()) + processCode);

        Long amt = ifxObj.getAuth_Amt();
        if (ISOFinalMessageType.isRequestMessage(ifxObj.getIfxType())) {
            if (ifxObj.getAuth_Amt() != null)
                isoMsg.set(4, ifxObj.getAuth_Amt().toString());

        } else {
            if (ifxObj.getTrx_Amt() != null) {
                isoMsg.set(4, ifxObj.getTrx_Amt().toString());
                amt = ifxObj.getTrx_Amt();
            }
        }

        if (ifxObj.getSec_Amt() != null) {
            isoMsg.set(6, ifxObj.getSec_Amt());
        }

        if (ifxObj.getTrnDt() != null)
            isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifxObj.getTrnDt().toDate()));

        isoMsg.set(10, ifxObj.getSec_CurRate());

        isoMsg.set(11, ifxObj.getSrc_TrnSeqCntr());

        isoMsg.set(12, ifxObj.getTimeLocalTran());
        isoMsg.set(13, ifxObj.getDateLocalTran());

        if (ifxObj.getExpDt() != null)
            isoMsg.set(14, ifxObj.getExpDt());

        if (ifxObj.getSettleDt() != null)
            isoMsg.set(15, MyDateFormatNew.format("MMdd", ifxObj.getSettleDt()));

        if (ifxObj.getPostedDt() != null)
            isoMsg.set(17, MyDateFormatNew.format("MMdd", ifxObj.getPostedDt()));

        isoMsg.set(25, fillTerminalType(ifxObj));
        isoMsg.set(25, ifxObj.getPosConditionCode());

        if (ifxObj.getBankId() != null)
            isoMsg.set(32, ifxObj.getBankId().toString());

        //m.rehman: require in
        //if (ifxObj.getFwdBankId() != null)
        //    isoMsg.set(33, ifxObj.getFwdBankId());
        ProcessContext processContext = ProcessContext.get();
        /*isoMsg.set(33,
            String.format("%-11s",
                    processContext.getAllInstitutions().get(
                            processContext.getOutputMessage().getChannel().getInstitutionId()).getBin().toString()
            ).replace(" ", "0")
        );*/
        if (!Util.hasText(ifxObj.getFwdBankId()))
            isoMsg.set(33, processContext.getAllInstitutions().get(
                processContext.getOutputMessage().getChannel().getInstitutionId()).getBin().toString());
        else
            isoMsg.set(33, ifxObj.getFwdBankId());

        isoMsg.set(35, ifxObj.getTrk2EquivData());

        isoMsg.set(37, ifxObj.getNetworkRefId());

        isoMsg.set(38, ifxObj.getApprovalCode());

        isoMsg.set(39, mapError(ifxObj.getIfxType(), ifxObj.getRsCode()));

        isoMsg.set(41, ifxObj.getTerminalId());

        isoMsg.set(42, ifxObj.getOrgIdNum());

        value = ifxObj.getCardAcceptNameLoc();
        if (!Util.hasText(value)) {
            value = " ";
            value = StringUtils.leftPad(value, 40);//format("%40s", value);
            ifxObj.setCardAcceptNameLoc(value);
        }
        isoMsg.set(43, ifxObj.getCardAcceptNameLoc());

        isoMsg.set(44, ifxObj.getAddResponseData());

        isoMsg.set(49, ifxObj.getAuth_Currency());

        if (ifxObj.getSec_Currency() != null)
            isoMsg.set(51, ifxObj.getSec_Currency());

        if (ifxObj.getPINBlock() != null && !ifxObj.getPINBlock().equals(""))
            isoMsg.set(52, ifxObj.getPINBlock());

        String P54 = "";
        String strBal = "";
        String balance;
        String balanceType;

        for (int i = 0; i < 2; ++i) {
            AcctBal acctBal = null;
            if (i == 0)
                acctBal = ifxObj.getAcctBalAvailable();
            else if (i == 1)
                acctBal = ifxObj.getAcctBalLedger();

            if (acctBal == null)
                continue;

            strBal = String.valueOf(acctBal.getAcctType().getType());

            if (acctBal.getBalType().equals(BalType.LEDGER))
                strBal += "01";
            else if (acctBal.getBalType().equals(BalType.AVAIL))
                strBal += "02";
            else
                strBal += "00";

            strBal += acctBal.getCurCode();
            balance = acctBal.getAmt();
            balanceType = balance.substring(0, 1);
            balance = balance.substring(1, balance.length());
            balance = String.format("%12s", balance).replace(' ', '0');

            P54 += strBal + balanceType + balance;
        }

        isoMsg.set(54, P54);

        isoMsg.set(70, ifxObj.getNetworkManageInfoCode());

        StringBuilder S90 = new StringBuilder();
        String stan;

        if (ISOFinalMessageType.isReversalOrRepeatMessage(ifxObj.getIfxType())
                || ISOFinalMessageType.isReturnMessage(ifxObj.getIfxType())
                //m.rehman: for void transaction from NAC
                || ifxObj.getIfxType().equals(IfxType.VOID_RQ)) {
            if (ifxObj.getSafeOriginalDataElements().getMessageType() == null) {
                throw new ISOException("Invalid original data element: No Message Type for field 90");
            }

            S90.append(ifxObj.getSafeOriginalDataElements().getMessageType());

            try {
                stan = StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getTrnSeqCounter(), '0');
                S90.append(stan);
                //CUP needs same STAN in Reversal
                isoMsg.set(11, stan);
            } catch (Exception e) {
                S90.append("000000");
            }
            try {
                S90.append(MyDateFormatNew.format("MMddHHmmss", ifxObj.getSafeOriginalDataElements().getOrigDt().toDate()));
            } catch (Exception e) {
                S90.append("0000000000");
            }
            try {
                S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getBankId(), '0'));
            } catch (Exception e) {
                S90.append("00000000000");
            }
            try {
                S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getFwdBankId(), '0'));
            } catch (Exception e) {
                S90.append("00000000000");
            }
            isoMsg.set(90, S90.toString());
        }

        if (ifxObj.getKeyManagement() != null && Util.hasText(ifxObj.getKeyManagement().getKey()))
            isoMsg.set(new ISOBinaryField(96, Hex.decode(ifxObj.getKeyManagement().getKey())));

        isoMsg.set(102, ifxObj.getMainAccountNumber());

        if (!isoMsg.getMTI().equals(ISOMessageTypes.REVERSAL_ADVICE_RESPONSE_87)) {
            if (ifxObj.getSett_Amt() != null)
                isoMsg.set(5, ifxObj.getSett_Amt());

            if (Util.hasText(""+ifxObj.getConvRate_Sett()))
                isoMsg.set(9, ifxObj.getConvRate_Sett());

            if (ifxObj.getSett_Currency() != null)
                isoMsg.set(50, ifxObj.getSett_Currency());
        }

        //setting Conversion Date
        if (Util.hasText(ifxObj.getSec_CurDate())) {
            isoMsg.set(16, ifxObj.getSec_CurDate());
        }

        //setting Merchant Type
        if (ISOFinalMessageType.isRequestMessage(ifxObj.getIfxType())) {
            if (Util.hasText(ifxObj.getPosConditionCode()) &&
                    ifxObj.getPosConditionCode().equals(ISOPOSConditionCodes.UN_ATTENDED_TERMINAL)) {
                value = "6011";
            } else {
                if (ifxObj.getTrnType().equals(TrnType.PURCHASE) || ifxObj.getTrnType().equals(TrnType.VOID))
                    value = "6012";
                else
                    value = "6010";
            }
            ifxObj.setMerchantType(value);
        }
        isoMsg.set(18, ifxObj.getMerchantType());

        //setting Merchant Country Code
        if (Util.hasText(ifxObj.getMerchCountryCode())) {
            isoMsg.set(19, ifxObj.getMerchCountryCode());
        }

        //setting POS Entry Mode Code
        isoMsg.set(22, ifxObj.getPosEntryModeCode());

        //setting POS PIN Capture Code
        value = ifxObj.getPosPinCaptureCode();
        if (!Util.hasText(value) && ifxObj.getPINBlock() != null) {
            value = "06";
        } else {
            value = "00";
        }
        ifxObj.setPosPinCaptureCode(value);
        isoMsg.set(26, ifxObj.getPosPinCaptureCode());

        //setting Amount Transaction Fee
        if (Util.hasText(ifxObj.getAmountTranFee())) {
            isoMsg.set(28, ifxObj.getAmountTranFee());
        }

        value = ifxObj.getSecRelatedControlInfo();
        if (!Util.hasText(value) && ISOFinalMessageType.isRequestMessage(ifxObj.getIfxType())) {
            if (ifxObj.getPosEntryModeCode().substring(2,3).
                    equals(ISOPOSEntryMode.PINEntryCapability.TERMINAL_CANNOT_ACCEPT_PINS))
                value = "0000000000000000";
            else
                value = "2600000000000000";
            ifxObj.setSecRelatedControlInfo(value);
        }
        isoMsg.set(53, ifxObj.getSecRelatedControlInfo());


        //setting chip data  //Raza commenting
        /*if (Util.hasText(ifxObj.getIccCardData())) {
            isoMsg.set(55, ifxObj.getIccCardData());
        }*/

        //setting Self Defined Field
        String selfDefinedData, msgReasonCode, terminalType, terminalEntryCapability, track2Data, indicator;
        Integer index;

        selfDefinedData = ifxObj.getSelfDefineData();
        if (Util.hasText(selfDefinedData)) {

            if (ISOFinalMessageType.isReversalRqMessage(ifxObj.getIfxType())) {
                if (Util.hasText(ifxObj.getRsCode()))
                    msgReasonCode = UnionPayResponseCodes.mapMsgReasonCode(Integer.parseInt(ifxObj.getRsCode()));
                else
                    msgReasonCode = "4020";
                //to append updated message reason code
                selfDefinedData = msgReasonCode + selfDefinedData.substring(4,selfDefinedData.length());
            } else {
                //appending Field 60.3.9 and 60.3.10
                selfDefinedData += "000";
            }
        } else {

            if (ISOFinalMessageType.isRequestMessage(ifxObj.getIfxType())) {
                msgReasonCode = "0000";
                selfDefinedData = msgReasonCode;

                //account holder type 2.1
                selfDefinedData += "0";

                //Terminal Entry Capability 2.2
                terminalEntryCapability = "0";
                //if (Util.hasText(ifxObj.getIccCardData())) { //Raza commenting
                    //terminalEntryCapability = "5";
                //} else {
                if (Util.hasText(ifxObj.getTrk2EquivData())) {
                    track2Data = ifxObj.getTrk2EquivData();
                    index = track2Data.indexOf("=");
                    if (index <= 0)
                        index = track2Data.indexOf("D");

                    indicator = track2Data.substring(index + 4, index + 5);

                    if (indicator.equals("2") || indicator.equals("6")) {
                        terminalEntryCapability = "5";
                    } else {
                        terminalEntryCapability = "2";
                    }
                }
                //}
                selfDefinedData += terminalEntryCapability;

                //chip condition code 2.3
                selfDefinedData += "0";

                //reserved 2.4
                selfDefinedData += "0";

                //Terminal Type info 2.5
                if (ifxObj.getIfxType().equals(IfxType.WITHDRAWAL_RQ)) {
                    if (Util.hasText(ifxObj.getPosConditionCode()) &&
                            ifxObj.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT)) {
                        terminalType = "06";
                    } else {
                        terminalType = "01";
                    }
                } else {
                    terminalType = "03";
                }
                selfDefinedData += terminalType;

                //signature only indicator / receivers currency indicator 2.6
                selfDefinedData += "0";

                //IC Card Authentication 2.7
                selfDefinedData += "0";

                //ECI 2.8
                selfDefinedData += "00";

                //Interactive mode id 2.9
                selfDefinedData += "0";

                //Special Pricing type and level 3.1 3.2
                selfDefinedData += "000";

                //Minor Unit 3.3
                selfDefinedData += "000";

                //Partial Approval id 3.4
                selfDefinedData += "0";

                //Txn Initiation mode 3.5
                if (Util.hasText(ifxObj.getPosConditionCode())) {
                    if (ifxObj.getPosConditionCode().equals(ISOPOSConditionCodes.UN_ATTENDED_TERMINAL))
                        selfDefinedData += "2";
                    else
                        selfDefinedData += "1";
                } else
                    selfDefinedData += "1";
            }
        }
        ifxObj.setSelfDefineData(selfDefinedData);
        isoMsg.set(60, ifxObj.getSelfDefineData());

        if (ISOFinalMessageType.isResponseMessage(ifxObj.getIfxType())) {
            if (ifxObj.getRecvBankId() != null) {
                isoMsg.set(100, ifxObj.getRecvBankId().toString());
            }
        }

        //setting Message Authentication Code
        if (Util.hasText(ifxObj.getMsgAuthCode())) {
            isoMsg.set(128, ifxObj.getMsgAuthCode());
        }

        isoMsg.setDirection(ISOMsg.OUTGOING);

        return isoMsg;
    }

    public String mapUnionPayTrnType(TrnType trnType) {
        String processCode;
        //m.rehman: for void transaction from NAC
        if (TrnType.VOID.equals(trnType))
            processCode = ISOTransactionCodes.REFUND;
        else
            processCode = mapTrnType(trnType);

        return processCode;
    }
}
