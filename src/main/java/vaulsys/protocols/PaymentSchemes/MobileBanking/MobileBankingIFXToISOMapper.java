package vaulsys.protocols.PaymentSchemes.MobileBanking;

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
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class MobileBankingIFXToISOMapper extends IfxToISOMapper {

    transient Logger logger = Logger.getLogger(MobileBankingIFXToISOMapper.class);
    public static final MobileBankingIFXToISOMapper Instance = new MobileBankingIFXToISOMapper();

    protected MobileBankingIFXToISOMapper() {
    }

    @Override
    public ProtocolMessage map(Ifx ifxObj, EncodingConvertor convertor) throws Exception {

        String value;
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager isoPackager = ((MobileBankingProtocol) ProtocolProvider.Instance.getByClass(MobileBankingProtocol.class)).getPackager();
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

        isoMsg.set(3, mapTrnType(ifxObj.getTrnType()) + processCode);

        if (ifxObj.getAuth_Amt() != null)
            isoMsg.set(4, ifxObj.getAuth_Amt());

        if (ifxObj.getTrx_Amt() != null)
            isoMsg.set(4, ifxObj.getTrx_Amt());

        if (ifxObj.getSec_Amt() != null)
            isoMsg.set(6, ifxObj.getSec_Amt());

        if (ifxObj.getTrnDt() != null)
            isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifxObj.getTrnDt().toDate()));

        //isoMsg.set(10, ifxObj.getSec_CurRate());

        isoMsg.set(11, ifxObj.getSrc_TrnSeqCntr());

        isoMsg.set(12, ifxObj.getTimeLocalTran());
        isoMsg.set(13, ifxObj.getDateLocalTran());

        if (ifxObj.getExpDt() != null)
            isoMsg.set(14, ifxObj.getExpDt());

        if (ifxObj.getSettleDt() != null)
            isoMsg.set(15, MyDateFormatNew.format("MMdd", ifxObj.getSettleDt()));

        //setting Conversion Date
        //if (Util.hasText(ifxObj.getDateConversion()))
        //    isoMsg.set(16, ifxObj.getDateConversion());
        if (Util.hasText(ifxObj.getSec_CurDate()))
            isoMsg.set(16, ifxObj.getSec_CurDate());


        //setting Merchant Type
        if (ISOMessageTypes.isRequestMessage(ifxObj.getMti())) {
            if (ifxObj.getTrnType().equals(TrnType.PURCHASE))
                value = "0005";
            else
                value = "6011";

            ifxObj.setMerchantType(value);

            //setting POS Entry Mode Code
            ifxObj.setPosEntryModeCode(getPOSEntryMode(ifxObj));
            isoMsg.set(22, ifxObj.getPosEntryModeCode());

            //setting POS PIN Capture Code
            value = "12";   //maximum length of a pin, can change if needed
            ifxObj.setPosPinCaptureCode(value); //for saving purpose
            isoMsg.set(26, value);

        }
        isoMsg.set(18, ifxObj.getMerchantType());

        //need to add support for Field 23 - Card Sequence No

        if (!Util.hasText(ifxObj.getNetworkInstId())) {
            value = "001";
            ifxObj.setNetworkInstId(value);
        }
        isoMsg.set(24, ifxObj.getNetworkInstId());

        //setting Amount Transaction Fee
        if (Util.hasText(ifxObj.getAmountTranFee()))
            isoMsg.set(28, ifxObj.getAmountTranFee());

        if (ifxObj.getBankId() != null)
            isoMsg.set(32, ifxObj.getBankId().toString());

        if (ifxObj.getFwdBankId() != null)
            isoMsg.set(33, ifxObj.getFwdBankId());

        isoMsg.set(35, ifxObj.getTrk2EquivData());

        isoMsg.set(37, ifxObj.getMyNetworkRefId());

        isoMsg.set(38, ifxObj.getApprovalCode());

        isoMsg.set(39, mapError(ifxObj.getIfxType(), ifxObj.getRsCode()));

        isoMsg.set(41, ifxObj.getTerminalId());

        isoMsg.set(42, ifxObj.getOrgIdNum());

        isoMsg.set(43, ifxObj.getCardAcceptNameLoc());

        isoMsg.set(44, ifxObj.getAddResponseData());

        if (Util.hasText(ifxObj.getTrack1Data()))
            isoMsg.set(45, ifxObj.getTrack1Data());

        if (Util.hasText(ifxObj.getAddDataPrivate()))
            isoMsg.set(48, ifxObj.getAddDataPrivate());

        isoMsg.set(49, ifxObj.getAuth_Currency());

        if (ifxObj.getTrnType().equals(TrnType.WITHDRAWAL) || ifxObj.getTrnType().equals(TrnType.PURCHASE) ||
                ifxObj.getTrnType().equals(TrnType.REFUND) || ifxObj.getTrnType().equals(TrnType.ORIGINAL_CREDIT)
                ) {
            //if (ifxObj.getAmountSettlement() != null) //Raza commenting
            if (ifxObj.getSett_Amt() != null)
                isoMsg.set(5, ifxObj.getSett_Amt());

            //if (Util.hasText(ifxObj.getConvRateSettlement())) //Raza commenting
            if (Util.hasText(ifxObj.getConvRate_Sett()))
                isoMsg.set(9, ifxObj.getConvRate_Sett());

            //if (ifxObj.getCurrCodeSettlement() != null) //Raza commenting
            if (ifxObj.getSett_Currency() != null)
                isoMsg.set(50, ifxObj.getSett_Currency());
        }

        if (ifxObj.getPINBlock() != null && !ifxObj.getPINBlock().equals(""))
            isoMsg.set(52, ifxObj.getPINBlock());

        String P54 = "";
        String strBal = "";
        String balance;
        String balanceType;

        if (ifxObj.getMti().equals(ISOMessageTypes.FINANCIAL_RESPONSE_87)
                || ifxObj.getMti().equals(ISOMessageTypes.FINANCIAL_RESPONSE_93)) {
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
        }

        //setting chip data
        /*if (Util.hasText(ifxObj.getIccCardData()))
            isoMsg.set(55, ifxObj.getIccCardData());*/

        //need to add support for Field 60 - Additional EMV Information
        //setting Self Defined Field

        isoMsg.set(70, ifxObj.getNetworkManageInfoCode());

        if (ISOMessageTypes.isReversalRequestMessage(ifxObj.getIfxType())) {
            StringBuilder S90 = new StringBuilder();
            if (ifxObj.getSafeOriginalDataElements().getMessageType() == null) {
                throw new ISOException("Invalid original data element: No Message Type for field 90");
            }

            S90.append(ifxObj.getSafeOriginalDataElements().getMessageType());

            try {
                S90.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getTrnSeqCounter(), '0'));
            } catch (Exception e) {
                S90.append("000000");
            }
            //reserved
            S90.append("      ");
            try {
                S90.append(ifxObj.getDateLocalTran() + ifxObj.getTimeLocalTran());
            } catch (Exception e) {
                S90.append("0000000000");
            }
            //reserved
            S90.append("      ");
            try {
                S90.append(MyDateFormatNew.format("MMddHHmmss", ifxObj.getSafeOriginalDataElements().getOrigDt().toDate()));
            } catch (Exception e) {
                S90.append("0000000000");
            }
            /*try {
                S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getBankId(), '0'));
            } catch (Exception e) {
                S90.append("00000000000");
            }
            try {
                S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifxObj.getSafeOriginalDataElements().getFwdBankId(), '0'));
            } catch (Exception e) {
                S90.append("00000000000");
            }*/
            isoMsg.set(90, S90.toString());

            String S95 = "";
            if (ifxObj.getNew_AmtAcqCur() != null && ifxObj.getNew_AmtIssCur() != null) {
                S95 += ifxObj.getNew_AmtAcqCur();
                S95 += ifxObj.getNew_AmtIssCur();
                S95 = String.format("-%18s", S95).replace(' ', '0');
            } else {
                S95 = String.format("-%42s", S95).replace(' ', '0');
            }
            isoMsg.set(95, S95);
        }


        if (Util.hasText(ifxObj.getAccountId1()))
            isoMsg.set(102, ifxObj.getAccountId1());

        if (Util.hasText(ifxObj.getAccountId2()))
            isoMsg.set(103, ifxObj.getAccountId2());

        if (Util.hasText(ifxObj.getRecordData()))
            isoMsg.set(120, ifxObj.getRecordData());

        isoMsg.setDirection(ISOMsg.OUTGOING);

        return isoMsg;
    }
}
