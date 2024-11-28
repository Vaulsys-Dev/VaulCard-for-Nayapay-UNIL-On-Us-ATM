package vaulsys.protocols.PaymentSchemes.JCB;

import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class JCBIFXToISOMapper extends IfxToISOMapper {

    transient Logger logger = Logger.getLogger(JCBIFXToISOMapper.class);
    public static final JCBIFXToISOMapper Instance = new JCBIFXToISOMapper();

    protected JCBIFXToISOMapper() {
    }

    @Override
    public ProtocolMessage map(Ifx ifxObj, EncodingConvertor convertor) throws Exception {

        String value = null;
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager isoPackager = ((JCBProtocol) ProtocolProvider.Instance.getByClass(JCBProtocol.class)).getPackager();
        isoMsg.setPackager(isoPackager);

        isoMsg.setMTI(ifxObj.getMti());

        if (Util.hasText(ifxObj.getAppPAN())) {
            value = ifxObj.getAppPAN();
            isoMsg.set(2, value);
        }

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

        if (ISOFinalMessageType.isRequestMessage(ifxObj.getIfxType())) {
            if (ifxObj.getAuth_Amt() != null)
                isoMsg.set(4, ifxObj.getAuth_Amt().toString());

        } else {
            if (ifxObj.getTrx_Amt() != null) {
                isoMsg.set(4, ifxObj.getTrx_Amt().toString());
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

        isoMsg.set(19, ifxObj.getMerchCountryCode());

        //isoMsg.set(25, fillTerminalType(ifxObj));
        isoMsg.set(25, ifxObj.getPosConditionCode());

        if (ifxObj.getBankId() != null) {
            value = ifxObj.getBankId().toString();
            /*if (value.length() % 2 != 0)
                value = StringUtils.leftPad(value, value.length()+1, "0");*/
            isoMsg.set(32, value);
        }

        if (ifxObj.getFwdBankId() != null) {
            value = ifxObj.getFwdBankId().toString();
            /*if (value.length() % 2 != 0)
                value = StringUtils.leftPad(value, value.length()+1, "0");*/
            isoMsg.set(33, value);
        }

        isoMsg.set(37, ifxObj.getMyNetworkRefId());

        isoMsg.set(38, ifxObj.getApprovalCode());

        isoMsg.set(39, mapError(ifxObj.getIfxType(), ifxObj.getRsCode()));

        isoMsg.set(41, ifxObj.getTerminalId());

        isoMsg.set(42, ifxObj.getOrgIdNum());

        isoMsg.set(43, ifxObj.getCardAcceptNameLoc());

        isoMsg.set(44, ifxObj.getAddResponseData());

        if (ifxObj.getAuth_Currency() != null) {
            value = ifxObj.getAuth_Currency().toString();
            value = StringUtils.leftPad(value, 4, "0");
            isoMsg.set(49, value);
        }

        if (ifxObj.getSec_Currency() != null) {
            value = ifxObj.getSec_Currency().toString();
            value = StringUtils.leftPad(value, 4, "0");
            isoMsg.set(51, value);
        }

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

        /*if (Util.hasText(ifxObj.getIccCardData())) {
            isoMsg.set(55, ifxObj.getIccCardData());
        }*/

        value = ifxObj.getNetworkManageInfoCode();
        if (Util.hasText(value)) {
            value = StringUtils.leftPad(value, 4, "0");
            isoMsg.set(70, value);
        }

        if (Util.hasText(ifxObj.getAccountId1())) {
            isoMsg.set(102, ifxObj.getAccountId1());
        }

        if (Util.hasText(ifxObj.getAccountId2())) {
            isoMsg.set(103, ifxObj.getAccountId2());
        }

        isoMsg.setDirection(ISOMsg.OUTGOING);

        return isoMsg;
    }
}
