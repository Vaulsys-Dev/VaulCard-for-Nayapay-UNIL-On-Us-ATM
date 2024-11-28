package vaulsys.security.hsm.safenet;

import vaulsys.util.encoders.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by HP on 8/9/2016.
 */
public class SafeNetHSMHandler {

    Logger logger = Logger.getLogger(SafeNetHSMHandler.class);
    private static SafeNetHSMHandler safeNetHSMHandler = null;

    public static SafeNetHSMHandler getInstance() {
        if (safeNetHSMHandler == null) {
            safeNetHSMHandler = new SafeNetHSMHandler();
        }
        return safeNetHSMHandler;
    }

    public byte[] PINValidation (String pinBlock, String pinOffset, String acctNo, String netKey, String pinFormat) {

        String funcCode, funcModifier, command, keySpec, keyLength, updatedAcctNo;
        Integer acctNoLength;

        funcCode = SafeNetHSMConst.FunctionCode.PIN_VERIFY;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        //pinFormat = SafeNetHSMConst.PINFormat.PIN_FORMAT_01;

        acctNoLength = acctNo.length();
        updatedAcctNo = acctNo.substring((acctNoLength-13), (acctNoLength-1));

        keySpec = GetKeySpecifier(netKey);

        keyLength = GetStringLength(keySpec + netKey);

        command = funcCode + funcModifier + pinBlock + keyLength + keySpec + netKey + pinFormat
                + updatedAcctNo + pinOffset;

        command = AddMessageHeader(command);

        //TODO: comment below logging
        logger.debug("Safenet command [" + command + "]");
        logger.debug("HEX Safenet command [" + new String(Hex.encode((Hex.decode(command))))  + "]");

        return Hex.decode(command);

        //return command.getBytes();
    }

    public byte[] PINGeneration (String pinBlock, String acctNo, String netKey, String pinFormat) {

        String funcCode, funcModifier, command, keySpec, keyLength, updatedAcctNo;
        Integer acctNoLength;

        funcCode = SafeNetHSMConst.FunctionCode.PIN_GENERATE_CHANGE;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        //pinFormat = SafeNetHSMConst.PINFormat.PIN_FORMAT_01;

        acctNoLength = acctNo.length();
        updatedAcctNo = acctNo.substring((acctNoLength-13), (acctNoLength-1));

        keySpec = GetKeySpecifier(netKey);

        keyLength = GetStringLength(keySpec + netKey);

        command = funcCode + funcModifier + pinBlock + keyLength + keySpec + netKey + pinFormat
                + updatedAcctNo;

        command = AddMessageHeader(command);


        //TODO: comment below logging
        logger.debug("Safenet command [" + command + "]");
        logger.debug("HEX Safenet command [" + new String(Hex.encode((Hex.decode(command))))  + "]");

        return Hex.decode(command);


        /*logger.debug("Command [" + command + "]");
        return command.getBytes();*/
    }

    public byte[] PINChange (String pinBlock, String acctNo, String key, String pinFormat) {

        String funcCode, funcModifier, command, keySpec, keyLength;

        funcCode = SafeNetHSMConst.FunctionCode.PIN_GENERATE_CHANGE;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        //pinFormat = SafeNetHSMConst.PINFormat.PIN_FORMAT_01;

        keySpec = GetKeySpecifier(key);

        keyLength = GetStringLength(keySpec + key);

        command = funcCode + funcModifier + pinBlock + keyLength + keySpec + key + pinFormat + acctNo;

        command = AddMessageHeader(command);

        return Hex.decode(command);
    }

    public byte[] PINTranslation (String pinBlock, String acctNo, String sourceKey, String sourcePinFormat,
                                  String destinationKey, String destinationPinFormat) {

        String funcCode, funcModifier, command, sourceKeySpec, destinationKeySpec, lenSourceKey, lenDestinationKey,
                updatedAcctNo;
        Integer acctNoLength;

        funcCode = SafeNetHSMConst.FunctionCode.PIN_TRANSLATE;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        //pinFormat = SafeNetHSMConst.PINFormat.PIN_FORMAT_01;
        acctNoLength = acctNo.length();
        updatedAcctNo = acctNo.substring((acctNoLength-13), (acctNoLength-1));

        sourceKeySpec = GetKeySpecifier(sourceKey);
        destinationKeySpec = GetKeySpecifier(destinationKey);

        lenSourceKey = GetStringLength(sourceKeySpec + sourceKey);
        lenDestinationKey = GetStringLength(destinationKeySpec + destinationKey);

        command = funcCode + funcModifier + pinBlock + lenSourceKey + sourceKeySpec + sourceKey + sourcePinFormat
                + updatedAcctNo + destinationPinFormat + lenDestinationKey + destinationKeySpec + destinationKey;

        command = AddMessageHeader(command);

        //TODO: comment below logging
        logger.debug("Safenet command [" + command + "]");
        logger.debug("HEX Safenet command [" + new String(Hex.encode((Hex.decode(command))))  + "]");

        return Hex.decode(command);
        //return command.getBytes();
    }

    public byte[] GenerateKey (String masterKey, String keyType) {
        String funcCode, funcModifier, command, keySpec, keyLength, keyFlag;

        funcCode = SafeNetHSMConst.FunctionCode.GENERATE_KEY;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        keySpec = GetKeySpecifier(masterKey);
        keyFlag = GetKeyFlag(keyType, masterKey.length());

        keyLength = GetStringLength(keySpec + masterKey);

        command = funcCode + funcModifier + keyLength + keySpec + masterKey + keyFlag;

        command = AddMessageHeader(command);

        return Hex.decode(command);
    }

    public byte[] TranslateKey (String masterKey, String keyType, String key) {
        String funcCode, funcModifier, command, keySpec, keyLength, masterKeySpec, masterKeyLength, keyFlag;

        funcCode = SafeNetHSMConst.FunctionCode.TRANSLATE_KEY;
        funcModifier = SafeNetHSMConst.FUNC_CODE;

        masterKeySpec = GetKeySpecifier(masterKey);
        masterKeyLength = GetStringLength(masterKeySpec + masterKey);

        keySpec = GetKeySpecifier(key);
        keyLength = GetStringLength(keySpec + key);

        keyFlag = GetKeyFlag(keyType, masterKey.length());

        command = funcCode + funcModifier + masterKeyLength + masterKeySpec + masterKey + keyFlag
                + keyLength + keySpec + key;

        command = AddMessageHeader(command);

        return Hex.decode(command);
    }

    public byte[] ARQCValidationARPCGeneration (String acctNo, String tranData, String ARQC, String authRespCode,
                                                  String macKey, String atc) {

        String funcCode, funcModifier, command, keySpecMacKey, keyLengthMacKey, action, mkMethod, acKeyMethod,
                acMethod, acData, arpcKeyMethod, arpcMethod, respCode, acKeyData, arpcData;

        funcCode = SafeNetHSMConst.FunctionCode.ARQC_VALIDATE_ARPC_GENERATE_KEY;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        action = SafeNetHSMConst.ARQCAction.VERIFY_ARQC_GENERATE_ARPC;
        mkMethod = SafeNetHSMConst.MacKeyMethod.COMMON;

        //TODO: need to handle cases according to schemes
        acKeyMethod = "01";
        acKeyData = atc + "00000000";
        acMethod = "02";
        arpcKeyMethod = "00";
        arpcData = "00";
        arpcMethod = "02";
        //authRespCode = authRespCode + "00000000";

        keySpecMacKey = GetKeySpecifier(macKey);
        keyLengthMacKey = GetStringLength(keySpecMacKey + macKey);

        acctNo = GetStringLength(acctNo) + acctNo;

        acData = GetStringLength(tranData) + tranData;

        respCode = GetStringLength(authRespCode) + authRespCode;

        acKeyData = GetStringLength(acKeyData) + acKeyData;

        command = funcCode + funcModifier + action + keyLengthMacKey + keySpecMacKey + macKey + mkMethod + acctNo + acKeyMethod
                + acKeyData + acMethod + acData + ARQC + arpcKeyMethod + arpcData + arpcMethod + respCode;

        command = AddMessageHeader(command);

        //TODO: comment below logging
        logger.debug("Safenet command [" + command + "]");
        logger.debug("HEX Safenet command [" + new String(Hex.encode((Hex.decode(command))))  + "]");

        return Hex.decode(command);
    }

    public byte[] ARQCValidation (String acctNo, String tranData, String ARQC, String authRespCode, String macKey) {

        String funcCode, funcModifier, command, keySpecMacKey, keyLengthMacKey, action, mkMethod, acKeyMethod,
                acMethod, acData, arpcKeyMethod, arpcData, arpcMethod, respCode, acKeyData;

        funcCode = SafeNetHSMConst.FunctionCode.ARQC_VALIDATE_ARPC_GENERATE_KEY;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        action = SafeNetHSMConst.ARQCAction.VERIFY_ARQC_ONLY;
        mkMethod = SafeNetHSMConst.MacKeyMethod.COMMON;

        acKeyMethod = "00";
        acKeyData = "00";
        acMethod = "02";
        arpcKeyMethod = "00";
        arpcData = "00";
        arpcMethod = "01";

        keySpecMacKey = GetKeySpecifier(macKey);
        keyLengthMacKey = GetStringLength(keySpecMacKey + macKey);

        acctNo = GetStringLength(acctNo) + acctNo;

        acData = GetStringLength(tranData) + tranData;

        respCode = "3030";

        command = funcCode + funcModifier + action + keyLengthMacKey + keySpecMacKey + macKey + mkMethod + acctNo + acKeyMethod
                + acKeyData + acMethod + acData + ARQC + arpcKeyMethod + arpcData + arpcMethod + respCode;

        command = AddMessageHeader(command);

        return Hex.decode(command);
    }

    public byte[] CVVValidation (String cvk, String cvv, String acctNo, String expiry, String serviceCode) {

        String funcCode, funcModifier, command, keyLength, cvvData, cvvKeySpec;

        funcCode = SafeNetHSMConst.FunctionCode.CVV_VERIFY;
        funcModifier = SafeNetHSMConst.FUNC_CODE;

        cvvKeySpec = GetKeySpecifier(cvk); //CVK-32Length --> cvvKeySpec=32
        keyLength = GetStringLength(cvk);

        cvvData = acctNo + expiry + serviceCode;
        cvvData = StringUtils.rightPad(cvvData, 32, "0");

        cvv = StringUtils.rightPad(cvv, 4, "F");

        command = funcCode + funcModifier + keyLength + cvvKeySpec + cvk + cvvData + cvv;

        command = AddMessageHeader(command);

        //TODO: comment below logging
        logger.debug("Safenet command [" + command + "]");
        logger.debug("HEX Safenet command [" + new String(Hex.encode((Hex.decode(command))))  + "]");


        return Hex.decode(command);
    }

    public byte[] CVVGeneration (String cvk, String acctNo, String expiry, String serviceCode) {

        String funcCode, funcModifier, command, keyLength, cvvData, cvvKeySpec;

        funcCode = SafeNetHSMConst.FunctionCode.CVV_GENERATE;
        funcModifier = SafeNetHSMConst.FUNC_CODE;

        // Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
        //cvvKeySpec = GetKeySpecifier(cvk); //CVK-32Length --> cvvKeySpec=32
        cvvKeySpec = GetCVVKeySpecifier(cvk); //CVK-32Length --> cvvKeySpec=32
        // ==========================================================================================================

        keyLength = GetStringLength(cvvKeySpec + cvk); //11+CVK-32Length

        cvvData = acctNo + expiry + serviceCode;

        command = funcCode + funcModifier + keyLength + cvvKeySpec + cvk + cvvData + "000000000";

        command = AddMessageHeader(command);

        //TODO: comment below logging
        logger.debug("Safenet command [" + command + "]");

        return Hex.decode(command);
    }

    // Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
    public String GetCVVKeySpecifier (String key) {
        String keySpecifier;

        if (key.length() == 16) {
            keySpecifier = SafeNetHSMConst.KeySpecifierFormat.SINGLE_LEN_KEY_SPEC;
        } else if (key.length() == 32) {
            keySpecifier = SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_CBC; //DOUBLE_LEN_KEY_SPEC_CBC;
        } else {
            keySpecifier = "";
        }

        return keySpecifier;
    }
    // ==========================================================================================================

    public byte[] PINBlockGeneration (String clearPIN, String acctNo, String netKey) {

        String funcCode, funcModifier, command, keySpec, keyLength, updatedAcctNo, pinLength, clearPinLength;
        Integer acctNoLength;

        funcCode = SafeNetHSMConst.FunctionCode.CLEAR_PIN_ENCRYPT;
        funcModifier = SafeNetHSMConst.FUNC_CODE;
        //pinFormat = SafeNetHSMConst.PINFormat.PIN_FORMAT_01;
        pinLength = "06";

        acctNoLength = acctNo.length();
        updatedAcctNo = acctNo.substring((acctNoLength-13), (acctNoLength-1));

        keySpec = GetKeySpecifier(netKey);

        keyLength = GetStringLength(keySpec + netKey);
        clearPinLength = GetStringLength(clearPIN);

        command = funcCode + funcModifier + pinLength + clearPinLength + clearPIN + updatedAcctNo + keyLength + keySpec + netKey;

        command = AddMessageHeader(command);


        //TODO: comment below logging
        logger.debug("Safenet command [" + command + "]");
        logger.debug("HEX Safenet command [" + new String(Hex.encode((Hex.decode(command))))  + "]");

        return Hex.decode(command);


        /*logger.debug("Command [" + command + "]");
        return command.getBytes();*/
    }

    public String AddMessageHeader (String command) {
        Integer len;
        String lenInHexString;
        String updatedCommand;

        len = command.length();
        lenInHexString = Integer.toHexString(len/2).toUpperCase();

        if ((len/2) <= 127) {
            updatedCommand = lenInHexString;
        } else {
            lenInHexString = String.format("%3s", lenInHexString).replace(' ', '0');
            updatedCommand = "8" + lenInHexString;
        }

        updatedCommand = "E300010100000000" + updatedCommand;

        len = len + updatedCommand.length();

        if ((len/2) > 255) {
            len = (len/2) - 255;
        }
        lenInHexString = Integer.toHexString(len/2).toUpperCase();

        updatedCommand = lenInHexString + updatedCommand;
        updatedCommand = "0101000000" + updatedCommand;
        updatedCommand += command;

        return updatedCommand;
    }

    public String GetKeySpecifier (String key) {
        String keySpecifier;

        if (key.length() == 16) {
            keySpecifier = SafeNetHSMConst.KeySpecifierFormat.SINGLE_LEN_KEY_SPEC;
        } else if (key.length() == 32) {
            keySpecifier = SafeNetHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC_EBC; //DOUBLE_LEN_KEY_SPEC_CBC;
        } else {
            keySpecifier = "";
        }

        return keySpecifier;
    }

    public String GetStringLength (String string) {
        Integer len;
        String lenInHex;
        len = string.length() / 2;
        lenInHex = Integer.toHexString(len);
        if (lenInHex.length() % 2 != 0)
            lenInHex = "0" + lenInHex;

        return lenInHex;
    }

    public String GetKeyFlag (String keyType, Integer length) {
        String keyFlag;

        if (keyType.equals(SafeNetHSMConst.TPK_TYPE) || keyType.equals(SafeNetHSMConst.ZPK_TYPE)) {
            if (length.equals(16)) {
                keyFlag = SafeNetHSMConst.KeyFlags.SINGLE_LEN_PPK_FLAG;
            } else {
                keyFlag = SafeNetHSMConst.KeyFlags.DOUBLE_LEN_PPK_FLAG;
            }
        } else if (keyType.equals(SafeNetHSMConst.MACKEY_TYPE)) {
            if (length.equals(16)) {
                keyFlag = SafeNetHSMConst.KeyFlags.SINGLE_LEN_MPK_FLAG;
            } else {
                keyFlag = SafeNetHSMConst.KeyFlags.DOUBLE_LEN_MPK_FLAG;
            }
        } else {
            keyFlag = "";
        }

        return keyFlag;
    }

    public String GetMessageWithoutHeader (String message) {
        //return message.substring(32,message.length()); //Raza TEMP commenting

         //Raza TEMP
        logger.debug("GetMessageWithoutHeader::message [" + message + "]");
        return message.substring(32,message.length()); //Raza TEMP parsing header of length 32
    }
}
