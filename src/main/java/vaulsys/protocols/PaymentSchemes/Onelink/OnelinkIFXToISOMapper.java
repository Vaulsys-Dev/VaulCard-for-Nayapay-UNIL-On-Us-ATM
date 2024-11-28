package vaulsys.protocols.PaymentSchemes.Onelink;

import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class OnelinkIFXToISOMapper extends IfxToISOMapper {

    transient Logger logger = Logger.getLogger(OnelinkIFXToISOMapper.class);
    public static final OnelinkIFXToISOMapper Instance = new OnelinkIFXToISOMapper();

    protected OnelinkIFXToISOMapper() {
    }

    @Override
    public ProtocolMessage map(Ifx ifx, EncodingConvertor convertor) throws Exception {

        String value;
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager isoPackager = ((OnelinkProtocol) ProtocolProvider.Instance.getByClass(OnelinkProtocol.class)).getPackager();
        isoMsg.setPackager(isoPackager);

        /*value = ifx.getMti();
        if (value.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87)
                || value.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_93))
            value = ISOMessageTypes.FINANCIAL_REQUEST_87;
        ifx.setMti(value);
        isoMsg.setMTI(value);*/
        isoMsg.setMTI(ifx.getMti());

        if (ifx.getAppPAN() != null)
            isoMsg.set(2, ifx.getAppPAN());

        String processCode = "";
        if (ifx.getAccTypeFrom().equals(AccType.MAIN_ACCOUNT))
            processCode = "00";
        else
            processCode = Integer.toString(ifx.getAccTypeFrom().getType());

        if (ifx.getAccTypeTo().equals(AccType.MAIN_ACCOUNT))
            processCode += "00";
        else
            processCode += Integer.toString(ifx.getAccTypeTo().getType());

        isoMsg.set(3, mapTrnType(ifx.getTrnType()) + processCode);
        //isoMsg.set(3, mapOnelinkTrnType(ifx) + processCode);

        if (ifx.getAuth_Amt() != null)
            isoMsg.set(4, ifx.getAuth_Amt());

        if (ifx.getTrx_Amt() != null)
            isoMsg.set(4, ifx.getTrx_Amt());

        if (ifx.getSec_Amt() != null)
            isoMsg.set(6, ifx.getSec_Amt());

        if (ifx.getTrnDt() != null)
            isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));

        //isoMsg.set(10, ifx.getSec_CurRate());

        isoMsg.set(11, ifx.getSrc_TrnSeqCntr());

        isoMsg.set(12, ifx.getTimeLocalTran());
        isoMsg.set(13, ifx.getDateLocalTran());

        if (ifx.getExpDt() != null)
            isoMsg.set(14, ifx.getExpDt());

        if (ifx.getSettleDt() != null) { //Raza changing for OneLink
            logger.info("Ifx Settlement Date [" + ifx.getSettleDt() + "]");
            isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));
        }
        else
        {
            if(Util.hasText(ProcessContext.get().getOutputMessage().getChannel().getSettlementDate()))
            {
                value = ProcessContext.get().getOutputMessage().getChannel().getSettlementDate();
                if (value.length() > 4)
                    value = value.substring(value.length()-4, value.length());
                logger.info("Settlement Date [" + value + "]");
                isoMsg.set(15,value);
            }
        }

        //setting Conversion Date
        //if (Util.hasText(ifx.getDateConversion()))
        //    isoMsg.set(16, ifx.getDateConversion());
        if (Util.hasText(ifx.getSec_CurDate()))
            isoMsg.set(16, ifx.getSec_CurDate());


        //setting Merchant Type
        if (ISOMessageTypes.isRequestMessage(ifx.getMti())) {
//            if (ifx.getTrnType().equals(TrnType.PURCHASE))
//                value = "0005";
//            else
//                value = "6011";
            //onelink pos merchant type in 0047
            value = "0047";

            ifx.setMerchantType(value);

            //setting POS Entry Mode Code
            if (!Util.hasText(ifx.getPosEntryModeCode()))
                ifx.setPosEntryModeCode(getPOSEntryMode(ifx));
            isoMsg.set(22, ifx.getPosEntryModeCode());

            //setting POS PIN Capture Code
            value = "08";   //maximum length of a pin, can change if needed
            ifx.setPosPinCaptureCode(value); //for saving purpose
            isoMsg.set(26, value);

        }
        isoMsg.set(18, ifx.getMerchantType());

        //need to add support for Field 23 - Card Sequence No

        if (!Util.hasText(ifx.getNetworkInstId())) {
            value = "001";
            ifx.setNetworkInstId(value);
        }
        isoMsg.set(24, ifx.getNetworkInstId());

        //setting Amount Transaction Fee
        if (Util.hasText(ifx.getAmountTranFee()))
            isoMsg.set(28, ifx.getAmountTranFee());

        ifx.setBankId(ProcessContext.get().getOutputMessage().getChannel().getInstitutionId());
        if (ifx.getBankId() != null)
            isoMsg.set(32, StringUtils.rightPad(ifx.getBankId().toString(), 11, "0"));

        //if (ifx.getFwdBankId() != null)
        //    isoMsg.set(33, ifx.getFwdBankId());

        isoMsg.set(35, ifx.getTrk2EquivData());

        //isoMsg.set(37, ifx.getMyNetworkRefId()); //commenting only using NetworkRefId
        isoMsg.set(37, ifx.getNetworkRefId());

        isoMsg.set(38, ifx.getApprovalCode());

        isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));

        isoMsg.set(41, String.format("%-8s", ifx.getTerminalId()));

        isoMsg.set(42, String.format("%-15s", ifx.getOrgIdNum()));

        isoMsg.set(43, StringUtils.rightPad(ifx.getCardAcceptNameLoc(), 40));

        isoMsg.set(44, ifx.getAddResponseData());

        if (Util.hasText(ifx.getTrack1Data()))
            isoMsg.set(45, ifx.getTrack1Data());

        if (Util.hasText(ifx.getAddDataNational()))
            isoMsg.set(47, ifx.getAddDataNational());

        if (Util.hasText(ifx.getAddDataPrivate()))
            isoMsg.set(48, ifx.getAddDataPrivate());

        isoMsg.set(49, ifx.getAuth_Currency());

        if (ifx.getTrnType().equals(TrnType.WITHDRAWAL) || ifx.getTrnType().equals(TrnType.PURCHASE) ||
                ifx.getTrnType().equals(TrnType.REFUND) || ifx.getTrnType().equals(TrnType.ORIGINAL_CREDIT)
                ) {
            //if (ifx.getAmountSettlement() != null) //Raza commenting
            if (ifx.getSett_Amt() != null)
                isoMsg.set(5, ifx.getSett_Amt());

            //if (Util.hasText(ifx.getConvRateSettlement())) //Raza commenting
            if (Util.hasText(ifx.getConvRate_Sett()))
                isoMsg.set(9, ifx.getConvRate_Sett());

            //if (ifx.getCurrCodeSettlement() != null) //Raza commenting
            if (ifx.getTrnType().equals(TrnType.PURCHASE)) {
                if (ifx.getSett_Currency() == null)
                    ifx.setSett_Currency("586");
                isoMsg.set(50, ifx.getSett_Currency());
            }
        }

        if (ifx.getPINBlock() != null && !ifx.getPINBlock().equals(""))
            isoMsg.set(52, ifx.getPINBlock().toUpperCase());

        String P54 = "";
        String strBal = "";
        String balance;
        String balanceType;

        if (ifx.getMti().equals(ISOMessageTypes.FINANCIAL_RESPONSE_87)
                || ifx.getMti().equals(ISOMessageTypes.FINANCIAL_RESPONSE_93)) {
            for (int i = 0; i < 2; ++i) {
                AcctBal acctBal = null;
                if (i == 0)
                    acctBal = ifx.getAcctBalAvailable();
                else if (i == 1)
                    acctBal = ifx.getAcctBalLedger();

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
        }

        //setting chip data
        /*if (Util.hasText(ifx.getIccCardData()))
            isoMsg.set(55, ifx.getIccCardData());*/

        //need to add support for Field 60 - Additional EMV Information
        //setting Self Defined Field

        isoMsg.set(70, ifx.getNetworkManageInfoCode());

        //m.rehman: adding check for refund as it requires original transaction data element
        if (ISOMessageTypes.isReversalRequestMessage(ifx.getIfxType()) || ifx.getIfxType().equals(IfxType.REFUND_RQ)) {
            StringBuilder S90 = new StringBuilder();
            if (ifx.getSafeOriginalDataElements().getMessageType() == null) {
                throw new ISOException("Invalid original data element: No Message Type for field 90");
            }

            S90.append(ifx.getSafeOriginalDataElements().getMessageType());

            try {
                S90.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getTrnSeqCounter(), '0'));
            } catch (Exception e) {
                S90.append("000000");
            }
            //reserved
            S90.append("      ");
            try {
                //S90.append(ifx.getDateLocalTran() + ifx.getTimeLocalTran());
                S90.append(MyDateFormatNew.format("MMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate()));
            } catch (Exception e) {
                S90.append("0000000000");
            }
            //reserved
            S90.append("      ");
            try {
                S90.append(MyDateFormatNew.format("MMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate()));
            } catch (Exception e) {
                S90.append("0000000000");
            }
            /*try {
                S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getBankId(), '0'));
            } catch (Exception e) {
                S90.append("00000000000");
            }
            try {
                S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getFwdBankId(), '0'));
            } catch (Exception e) {
                S90.append("00000000000");
            }*/
            isoMsg.set(90, S90.toString());

            /*
            String S95 = "";
            if (ifx.getNew_AmtAcqCur() != null && ifx.getNew_AmtIssCur() != null) {
                S95 += ifx.getNew_AmtAcqCur();
                S95 += ifx.getNew_AmtIssCur();
                S95 = String.format("-%18s", S95).replace(' ', '0');
            } else {
                S95 = String.format("-%42s", S95).replace(' ', '0');
            }
            isoMsg.set(95, S95);
            */
            String S95 = "0";
            S95 = StringUtils.rightPad(S95, 42, "0");
            isoMsg.set(95, S95);
        }


        if (Util.hasText(ifx.getAccountId1()))
            isoMsg.set(102, ifx.getAccountId1());

        if (Util.hasText(ifx.getAccountId2()))
            isoMsg.set(103, ifx.getAccountId2());

        if (Util.hasText(ifx.getRecordData()))
            isoMsg.set(120, ifx.getRecordData());

        isoMsg.setDirection(ISOMsg.OUTGOING);

        return isoMsg;
    }

    private String mapOnelinkTrnType(Ifx ifx) {

        if (ifx.getTrnType().equals(TrnType.PREAUTH))
            ifx.setTrnType(TrnType.PURCHASE);

        return mapTrnType(ifx.getTrnType());
    }
}
