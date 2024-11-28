package vaulsys.clearing.reconcile;

import vaulsys.calendar.DayDate;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.impl.Terminal;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ISOResponseDataProcessor extends AbstractISODataProcessor {

    static Logger logger = Logger.getLogger(ISOResponseDataProcessor.class);

    public static final ISOResponseDataProcessor Instance = new ISOResponseDataProcessor();
    
    protected ISOResponseDataProcessor() {}

    public Map<Integer, String> process(ProtocolMessage message, Terminal terminal, DayDate stlDate) {
//    	Long entityCode = terminal.getOwner().getCode();
    	Long entityCode = terminal.getOwnerId();
        TerminalClearingMode mode = terminal.getClearingMode();
        String responseCode = generateResponceCode((ISOMsg) message, entityCode, terminal, mode, stlDate);
        Map<Integer, String> result = new HashMap<Integer, String>();
        result.put(66, responseCode);
        return result;
    }

    private String generateResponceCode(ISOMsg isoMsg, Long entityCode, Terminal terminal, TerminalClearingMode mode, DayDate stlDate) {
        String responceCode = "2";
//        Long amount = new Long(isoMsg.getString(97).substring(1));

//        String additionalInformation = isoMsg.getString(124);
//        ReconcilementInfo atmRecievedRecInfo = null;
//        ReconcilementInfo pinPadRecievedRecInfo = null;
//        ReconcilementInfo posRecievedRecInfo = null;
//        ReconcilementInfo intRecievedRecInfo = null;
//        ReconcilementInfo vruRecievedRecInfo = null;
//
//        // TODO 124 for internet & vru
//        if (additionalInformation != null && !additionalInformation.equals("")) {
//            List<ReconcilementInfo> recsInfo = parseAdditionalInformation(additionalInformation);
//            atmRecievedRecInfo = recsInfo.get(0);
//            pinPadRecievedRecInfo = recsInfo.get(1);
//            posRecievedRecInfo = recsInfo.get(2);
//
//        }
//        String incomingType = isoMsg.getString(97).substring(0, 1);
//
//        ReconcilementInfo recInfo = processFinancialData(terminal,stlDate);
//        
//        long creditAmount = recInfo.getCreditAmount();
//        long creditReversalAmount = recInfo.getCreditReversalAmount();
//		long debitAmount = recInfo.getDebitAmount();
//		long debitReversalAmount = recInfo.getDebitReversalAmount();
//		long totalAmount = creditAmount - creditReversalAmount - debitAmount + debitReversalAmount;
//        // TODO make decision when we must put D and when C?! does it depend on Issuer/Acquire mode?!
//        // I think this problem will solve with Acquire/Issuer recInfo
//        String myType = (totalAmount > 0) ? "D" : "C";
//        totalAmount = Math.abs(totalAmount);
//
//        logger.info("Out credit Amount:" + creditAmount);
//        logger.info("Out creditRev Amount:" + creditReversalAmount);
//        logger.info("Out Debit Amount:" + debitAmount);
//        logger.info("Out DebitRev Amount:" + debitReversalAmount);
//
//        if (additionalInformation != null && !additionalInformation.equals("")) {
//
//            logger.info("my type: " + myType);
//            logger.info("incoming type: " + incomingType);
//            logger.info("totalAmount:" + totalAmount);
//            logger.info("calc-ed amount:" + amount.toString());
//
//            if (/*atmCalculatedRecInfo.equals(atmRecievedRecInfo)
//                    && pinPadCalculatedRecInfo.equals(pinPadRecievedRecInfo)
//                    && posCalculatedRecInfo.equals(posRecievedRecInfo)
//                    && vruCalculatedRecInfo.equals(vruRecievedRecInfo)
//                    && intCalculatedRecInfo.equals(intRecievedRecInfo) && */myType.equals(incomingType)
//                    && amount.equals(totalAmount))
//                responceCode = "1";
//            else
//                responceCode = "2";
//        } else {
//            logger.info("my type: " + myType);
//            logger.info("incoming type: " + incomingType);
//            logger.info("totalAmount" + totalAmount);
//            logger.info("amount" + amount.toString());
//
//            // Commented because of Negin's error
//            /*
//                * if ((myType.equals(incomingType) && totalAmount.equals(amount)) || (totalAmount.equals(0) && totalAmount.equals(amount)) ) responceCode =
//                * "1";
//                */// And replaced by:
//            if (Long.parseLong(isoMsg.getString(86)) == debitAmount
//                    && Long.parseLong(isoMsg.getString(87)) == creditReversalAmount
//                    && Long.parseLong(isoMsg.getString(88)) == creditAmount
//                    && Long.parseLong(isoMsg.getString(89)) == debitReversalAmount
//                    && amount.equals(totalAmount) && (amount == 0 || myType.equals(incomingType)))
//                responceCode = "1";
//            else
//                responceCode = "2";
//            return responceCode;
//        }

        return responceCode;
    }

//    private List<ReconcilementInfo> parseAdditionalInformation(String data) {
//        List<ReconcilementInfo> result = new ArrayList<ReconcilementInfo>();
//
//        ReconcilementInfo recInfoPos = new ReconcilementInfo();
//        ReconcilementInfo recInfoAtm = new ReconcilementInfo();
//        ReconcilementInfo recInfoPinPad = new ReconcilementInfo();
//
//        StringTokenizer tokenizer = new StringTokenizer(data, "!");
//
//        recInfoPinPad.setCreditNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoAtm.setCreditNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoPos.setCreditNumber(Integer.parseInt(tokenizer.nextToken()));
//
//        recInfoPinPad.setCreditReversalNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoAtm.setCreditReversalNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoPos.setCreditReversalNumber(Integer.parseInt(tokenizer.nextToken()));
//
//        recInfoPinPad.setDebitNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoAtm.setDebitNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoPos.setDebitNumber(Integer.parseInt(tokenizer.nextToken()));
//
//        recInfoPinPad.setDebitReversalNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoAtm.setDebitReversalNumber(Integer.parseInt(tokenizer.nextToken()));
//        recInfoPos.setDebitReversalNumber(Integer.parseInt(tokenizer.nextToken()));
//
//        recInfoPinPad.setCreditAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoAtm.setCreditAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoPos.setCreditAmount(Long.valueOf(tokenizer.nextToken()));
//
//        recInfoPinPad.setCreditReversalAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoAtm.setCreditReversalAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoPos.setCreditReversalAmount(Long.valueOf(tokenizer.nextToken()));
//
//        recInfoPinPad.setDebitAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoAtm.setDebitAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoPos.setDebitAmount(Long.valueOf(tokenizer.nextToken()));
//
//        recInfoPinPad.setDebitReversalAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoAtm.setDebitReversalAmount(Long.valueOf(tokenizer.nextToken()));
//        recInfoPos.setDebitReversalAmount(Long.valueOf(tokenizer.nextToken()));
//
//        recInfoAtm.setCreditFee(Long.valueOf(tokenizer.nextToken()));
//        recInfoAtm.setDebitFee(Long.valueOf(tokenizer.nextToken()));
//
//        recInfoPos.setCreditFee(Long.valueOf(tokenizer.nextToken()));
//        recInfoPos.setDebitFee(Long.valueOf(tokenizer.nextToken()));
//
//        recInfoPos.setBallInqNumber(Integer.parseInt(tokenizer.nextToken()));
//
//        recInfoAtm.setBallInqNumber(Integer.parseInt(tokenizer.nextToken()));
//
//        result.add(recInfoAtm);
//        result.add(recInfoPinPad);
//        result.add(recInfoPos);
//        return result;
//    }

}
