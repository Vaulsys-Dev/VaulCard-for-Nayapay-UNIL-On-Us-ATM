package vaulsys.protocols.PaymentSchemes.NAC;

import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class NACIFXToISOMapper extends IfxToISOMapper {

    transient Logger logger = Logger.getLogger(NACIFXToISOMapper.class);
    public static final NACIFXToISOMapper Instance = new NACIFXToISOMapper();
    public static Map<String, String> outGoingRespCode;

    protected NACIFXToISOMapper() {
        loadOutgoingRespCode();
    }

    @Override
    public ProtocolMessage map(Ifx ifxObj, EncodingConvertor convertor) throws Exception {

        String value;
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager isoPackager = ((NACProtocol) ProtocolProvider.Instance.getByClass(NACProtocol.class)).getPackager();
        isoMsg.setPackager(isoPackager);

        isoMsg.setMTI(ifxObj.getMti());

        if (ifxObj.getAppPAN() != null) {
            value = ifxObj.getAppPAN();
            if (value.length() % 2 != 0)
                value = StringUtils.leftPad(value, value.length()+1, "0");
            isoMsg.set(2, value);
        }

        String processCode = "";
        if (ifxObj.getAccTypeFrom().equals(AccType.MAIN_ACCOUNT) || ifxObj.getAccTypeFrom().equals(AccType.SUBSIDIARY_ACCOUNT))
            processCode += StringUtils.leftPad(Integer.toString(ifxObj.getAccTypeFrom().getType()), 2, "0");//processCode += "00";
        else
            processCode += Integer.toString(ifxObj.getAccTypeFrom().getType());

        if (ifxObj.getAccTypeTo().equals(AccType.MAIN_ACCOUNT) || ifxObj.getAccTypeTo().equals(AccType.SUBSIDIARY_ACCOUNT))
            processCode += StringUtils.leftPad(Integer.toString(ifxObj.getAccTypeTo().getType()), 2, "0");//processCode += "00";
        else
            processCode += Integer.toString(ifxObj.getAccTypeTo().getType());

        //isoMsg.set(3, mapTrnType(ifxObj.getTrnType()) + processCode);
        isoMsg.set(3, mapNACTrnType(ifxObj) + processCode);

        if (ifxObj.getAuth_Amt() != null)
            isoMsg.set(4, ifxObj.getAuth_Amt());

        if (ifxObj.getTrx_Amt() != null)
            isoMsg.set(4, ifxObj.getTrx_Amt());

        isoMsg.set(11, ifxObj.getSrc_TrnSeqCntr());

        if (!ifxObj.getIfxType().equals(IfxType.OFFLINE_TIP_ADJUST_REPEAT_RS)) {
            isoMsg.set(12, ifxObj.getTimeLocalTran());
            isoMsg.set(13, ifxObj.getDateLocalTran());

            if (ifxObj.getExpDt() != null)
                isoMsg.set(14, ifxObj.getExpDt());
        }

        if (!Util.hasText(ifxObj.getNetworkInstId())) {
            value = "001";
            ifxObj.setNetworkInstId(value);
        }
        isoMsg.set(24, ifxObj.getNetworkInstId());

        isoMsg.set(37, ifxObj.getNetworkRefId());

        isoMsg.set(38, ifxObj.getApprovalCode());

        //isoMsg.set(39, mapError(ifxObj.getIfxType(), ifxObj.getRsCode()));
        isoMsg.set(39, mapOutgoingRespCode(ifxObj.getRsCode()));

        isoMsg.set(41, String.format("%-8s", ifxObj.getTerminalId()));

        if (Util.hasText(ifxObj.getAddDataPrivate()))
            isoMsg.set(48, ifxObj.getAddDataPrivate());

        isoMsg.setDirection(ISOMsg.OUTGOING);

        return isoMsg;
    }

    public String mapNACTrnType(Ifx ifx) {
        String processCode;// = "00";
        if (ifx.getTrnType().equals(TrnType.PREAUTH)
                && (ifx.getIfxType() != null && ifx.getIfxType().equals(IfxType.PREAUTH_RS)))
            processCode = ISOTransactionCodes.BALANCE_INQUERY;
        else
            processCode = mapTrnType(ifx.getTrnType());

        return processCode;
    }

    public void loadOutgoingRespCode() {
        outGoingRespCode = new HashMap<String, String>();
        outGoingRespCode.put("00", "00");
        outGoingRespCode.put("01", "00");
        outGoingRespCode.put("02", "25");
        outGoingRespCode.put("03", "14");
        outGoingRespCode.put("04", "51");
        outGoingRespCode.put("05", "14");
        outGoingRespCode.put("06", "31");
        outGoingRespCode.put("07", "14");
        outGoingRespCode.put("08", "14");
        outGoingRespCode.put("09", "30");
        outGoingRespCode.put("10", "94");
        outGoingRespCode.put("11", "12");
        outGoingRespCode.put("12", "14");
        outGoingRespCode.put("13", "96");
        outGoingRespCode.put("14", "02");
        outGoingRespCode.put("15", "41");
        outGoingRespCode.put("16", "14");
        outGoingRespCode.put("17", "57");
        outGoingRespCode.put("18", "57");
        outGoingRespCode.put("19", "57");
        outGoingRespCode.put("20", "57");
        outGoingRespCode.put("21", "57");
        outGoingRespCode.put("22", "57");
        outGoingRespCode.put("23", "57");
        outGoingRespCode.put("24", "55");
        outGoingRespCode.put("25", "54");
        outGoingRespCode.put("26", "57");
        outGoingRespCode.put("27", "57");
        outGoingRespCode.put("28", "25");
        outGoingRespCode.put("29", "57");
        outGoingRespCode.put("30", "96");
        outGoingRespCode.put("31", "57");
        outGoingRespCode.put("32", "57");
        outGoingRespCode.put("33", "57");
        outGoingRespCode.put("34", "96");
        outGoingRespCode.put("35", "96");
        outGoingRespCode.put("36", "57");
        outGoingRespCode.put("37", "57");
        outGoingRespCode.put("38", "12");
        outGoingRespCode.put("39", "12");
        outGoingRespCode.put("40", "57");
        outGoingRespCode.put("41", "57");
        outGoingRespCode.put("42", "57");
        outGoingRespCode.put("43", "57");
        outGoingRespCode.put("44", "57");
        outGoingRespCode.put("45", "57");
        outGoingRespCode.put("46", "96");
        outGoingRespCode.put("47", "96");
        outGoingRespCode.put("48", "13");
        outGoingRespCode.put("49", "57");
        outGoingRespCode.put("50", "12");
        outGoingRespCode.put("51", "12");
        outGoingRespCode.put("52", "12");
        outGoingRespCode.put("53", "12");
        outGoingRespCode.put("54", "12");
        outGoingRespCode.put("55", "12");
        outGoingRespCode.put("56", "12");
        outGoingRespCode.put("57", "57");
        outGoingRespCode.put("58", "58");
        outGoingRespCode.put("59", "57");
        outGoingRespCode.put("60", "55");
        outGoingRespCode.put("61", "57");
        outGoingRespCode.put("62", "57");
        outGoingRespCode.put("63", "57");
        outGoingRespCode.put("64", "57");
        outGoingRespCode.put("65", "12");
        outGoingRespCode.put("66", "57");
        outGoingRespCode.put("67", "14");
        outGoingRespCode.put("68", "25");
        outGoingRespCode.put("69", "57");
        outGoingRespCode.put("70", "57");
        outGoingRespCode.put("71", "57");
        outGoingRespCode.put("72", "57");
        outGoingRespCode.put("73", "57");
        outGoingRespCode.put("74", "57");
        outGoingRespCode.put("75", "57");
        outGoingRespCode.put("76", "57");
        outGoingRespCode.put("77", "01");
        outGoingRespCode.put("78", "03");
        outGoingRespCode.put("79", "57");
        outGoingRespCode.put("80", "30");
        outGoingRespCode.put("81", "57");
        outGoingRespCode.put("82", "57");
        outGoingRespCode.put("83", "57");
        outGoingRespCode.put("84", "57");
        outGoingRespCode.put("85", "57");
        outGoingRespCode.put("86", "55");
        outGoingRespCode.put("87", "57");
        outGoingRespCode.put("88", "57");
        outGoingRespCode.put("89", "57");
        outGoingRespCode.put("90", "57");
        outGoingRespCode.put("91", "57");
        outGoingRespCode.put("92", "96");
        outGoingRespCode.put("93", "57");
        outGoingRespCode.put("94", "57");
        outGoingRespCode.put("95", "95");
        outGoingRespCode.put("96", "96");
        outGoingRespCode.put("97", "57");
        outGoingRespCode.put("98", "57");
        outGoingRespCode.put("99", "57");
    }

    public String mapOutgoingRespCode(String inRespCode) {
        String outRespCode;
        if (outGoingRespCode.containsKey(inRespCode))
            outRespCode = outGoingRespCode.get(inRespCode);
        else
            outRespCode = "57";

        return outRespCode;
    }
}
