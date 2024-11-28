package vaulsys.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.HashMap;

/**
 *  Asim Shahzad, Date : 29th June 2021, Tracking ID : VP-NAP-202106241 / VC-NAP-202106241
 */
public class IBANUtil {
    private static final Logger logger = Logger.getLogger(IBANUtil.class);

    private static final String modValue = "97";
    private static final String subtractioinValue = "98";

    public static HashMap<String, String> ibanMap = new HashMap<String, String>();

    public IBANUtil()
    {
        ibanMap.put("A", "10");
        ibanMap.put("B", "11");
        ibanMap.put("C", "12");
        ibanMap.put("D", "13");
        ibanMap.put("E", "14");
        ibanMap.put("F", "15");
        ibanMap.put("G", "16");
        ibanMap.put("H", "17");
        ibanMap.put("I", "18");
        ibanMap.put("J", "19");
        ibanMap.put("K", "20");
        ibanMap.put("L", "21");
        ibanMap.put("M", "22");
        ibanMap.put("N", "23");
        ibanMap.put("O", "24");
        ibanMap.put("P", "25");
        ibanMap.put("Q", "26");
        ibanMap.put("R", "27");
        ibanMap.put("S", "28");
        ibanMap.put("T", "29");
        ibanMap.put("U", "30");
        ibanMap.put("V", "31");
        ibanMap.put("W", "32");
        ibanMap.put("X", "33");
        ibanMap.put("Y", "34");
        ibanMap.put("Z", "35");
    }

    public String generateIBAN(String accountCode) {
//        logger.info("===========================================================");
//        logger.info("Generating IBAN...");

        String finalIbanValue = "";

        String iban = "PK00NAYA" + accountCode;

//        logger.info("Initial IBAN value [" + iban + "]");

        String iBanValue = iban.substring(4) + iban.substring(0, 4);

//        logger.info("IBAN value after placing CCCD at the end of IBAN [" + iBanValue +"]");

        String BI = iBanValue.substring(0,4);
        String cc = iBanValue.substring(20,22);
        String accCode = iBanValue.substring(4,20);
        String convIBan = "";

//        logger.info("Bank Identifier [" + BI + "]");
//        logger.info("Country Code value [" + cc + "]");
//        logger.info("Account Code [" + accCode + "]");

        for(int i=0; i<BI.length();i++) {
            char temp = BI.charAt(i);
            convIBan += ibanMap.get(String.valueOf(temp));
        }

        convIBan += accCode;
//        logger.info("IBAN value after replacing bank identifier with mapping values [" + convIBan + "]");

        for(int i=0; i<cc.length();i++) {
            char temp = cc.charAt(i);
            convIBan += ibanMap.get(String.valueOf(temp));
        }

        convIBan += iBanValue.substring(22,24);
//        logger.info("IBAN value after replacing CC with mapping values [" + convIBan + "]");

        BigInteger num1 = new BigInteger(convIBan);
        BigInteger num2 = new BigInteger(modValue);
        BigInteger num3 = new BigInteger(subtractioinValue);
        BigInteger result=num1.mod(num2);
        BigInteger checkDigit = num3.subtract(result);

//        logger.info("The result after modulus operation is [" + result + "]");
//        logger.info("The check digit is [" + checkDigit + "]");

        finalIbanValue = cc + StringUtils.leftPad(String.valueOf(checkDigit),2,"0") + BI + accCode;

        logger.info("Final IBAN value is [" + finalIbanValue + "]");
//        logger.info("===========================================================");

        return finalIbanValue;
    }

    public Boolean validateIBAN(String iBan) {
//        logger.info("===========================================================");
//        logger.info("Validating IBAN....");

//        logger.info("Incoming IBAN value [" + iBan + "]");
        String iBanValue = iBan.substring(4) + iBan.substring(0, 4);
//        logger.info("IBAN value after placing CCCD at the end of IBAN [" + iBanValue +"]");

        String BI = iBanValue.substring(0,4);
        String cc = iBanValue.substring(20,22);
        String accCode = iBanValue.substring(4,20);
        String convIBan = "";

        for(int i=0; i<BI.length();i++) {
            char temp = BI.charAt(i);
            convIBan += ibanMap.get(String.valueOf(temp));
        }

        convIBan += accCode;
//        logger.info("IBAN value after replacing bank identifier with mapping values [" + convIBan + "]");

        for(int i=0; i<cc.length();i++) {
            char temp = cc.charAt(i);
            convIBan += ibanMap.get(String.valueOf(temp));
        }

        convIBan += iBanValue.substring(22,24);
//        logger.info("IBAN value after replacing CC with mapping values [" + convIBan + "]");

        BigInteger num1 = new BigInteger(convIBan);
        BigInteger num2 = new BigInteger("97");
        BigInteger result=num1.mod(num2);

//        logger.info("The result after modulus operation is [" + result + "]");
//        logger.info("===========================================================");

        if(String.valueOf(result).equals("1"))
            return true;
        else
            return false;
    }
}
