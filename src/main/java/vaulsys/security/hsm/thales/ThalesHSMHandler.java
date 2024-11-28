package vaulsys.security.hsm.thales;

import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.util.encoders.Hex;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Created by HP on 8/9/2016.
 */
public class ThalesHSMHandler {

    private static ThalesHSMHandler ThalesHSMHandler = null;
    private static Integer messageHeader = 0;

    public static ThalesHSMHandler getInstance() {
        if (ThalesHSMHandler == null) {
            ThalesHSMHandler = new ThalesHSMHandler();
        }
        return ThalesHSMHandler;
    }

    public byte[] PINValidation (String pinBlock, String pinOffset, String acctNo, String key, String pinFormat,
                                 String networkType) {

        String commandCode, keySpec;
        byte[] command;

        commandCode = null;
        if (networkType.equals(ThalesHSMConst.LOCAL_NETWORK))
            commandCode = ThalesHSMConst.Commands.PIN_VERIFY_LOCAL;
        else if (networkType.equals(ThalesHSMConst.ZONAL_NETWORK))
            commandCode = ThalesHSMConst.Commands.PIN_VERIFY_ZONAL;

        //pinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;

        keySpec = GetKeySpecifier(key);

        acctNo = GetUpdatedAccountNo(acctNo);

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, ((keySpec != null && keySpec != "") ? keySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, key.getBytes());
        command = ArrayUtils.addAll(command, pinBlock.getBytes());
        command = ArrayUtils.addAll(command, pinFormat.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());
        command = ArrayUtils.addAll(command, pinOffset.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] PINGeneration (String pinBlock, String pinOffset, String acctNo, String key, String pinFormat,
                                 String networkType) { //Raza TODO: Verify THIS; not doing due TimeConstraint issue for NayaPay

        String commandCode, keySpec;
        byte[] command;

        commandCode = null;
        if (networkType.equals(ThalesHSMConst.LOCAL_NETWORK))
            commandCode = ThalesHSMConst.Commands.PIN_GENERATE_LOCAL;
        else if (networkType.equals(ThalesHSMConst.ZONAL_NETWORK))
            commandCode = ThalesHSMConst.Commands.PIN_VERIFY_ZONAL;

        //pinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;

        keySpec = GetKeySpecifier(key);

        acctNo = GetUpdatedAccountNo(acctNo);

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, ((keySpec != null && keySpec != "") ? keySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, key.getBytes());
        command = ArrayUtils.addAll(command, pinBlock.getBytes());
        command = ArrayUtils.addAll(command, pinFormat.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());
        command = ArrayUtils.addAll(command, pinOffset.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] PINChange (String pinBlock, String acctNo, String key, String pinFormat) {

        String commandCode, keySpec;
        byte[] command;

        commandCode = ThalesHSMConst.Commands.PIN_CHANGE;
        //pinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;

        keySpec = GetKeySpecifier(key);

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, ((keySpec != null && keySpec != "") ? keySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, key.getBytes());
        command = ArrayUtils.addAll(command, pinBlock.getBytes());
        command = ArrayUtils.addAll(command, pinFormat.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] PINTranslation (String pinBlock, String acctNo, String sourceKey, String sourcePinFormat,
                                       String destinationKey, String destinationPinFormat, String networkType) {

        String commandCode, maxPinLength, sourceKeySpec, destinationKeySpec;
        byte[] command;

        commandCode = null;
        if (networkType.equals(ThalesHSMConst.LOCAL_NETWORK))
            commandCode = ThalesHSMConst.Commands.PIN_TRANSLATE_LOCAL;
        else if (networkType.equals(ThalesHSMConst.ZONAL_NETWORK))
            commandCode = ThalesHSMConst.Commands.PIN_TRANSLATE_ZONAL;

        //pinFormat = ThalesHSMConst.PINFormat.PIN_FORMAT_01;
        maxPinLength = ThalesHSMConst.MAX_PIN_LENGTH;

        acctNo = GetUpdatedAccountNo(acctNo);

        sourceKeySpec = GetKeySpecifier(sourceKey);
        destinationKeySpec = GetKeySpecifier(destinationKey);

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, ((sourceKeySpec != null && sourceKeySpec != "") ? sourceKeySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, sourceKey.getBytes());
        command = ArrayUtils.addAll(command, ((destinationKeySpec != null && destinationKeySpec != "") ? destinationKeySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, destinationKey.getBytes());
        command = ArrayUtils.addAll(command, maxPinLength.getBytes());
        command = ArrayUtils.addAll(command, pinBlock.getBytes());
        command = ArrayUtils.addAll(command, sourcePinFormat.getBytes());
        command = ArrayUtils.addAll(command, destinationPinFormat.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] GenerateKey (String masterKey, String keyType) {
        String commandCode, keySpec;
        byte[] command;

        commandCode = ThalesHSMConst.Commands.GENERATE_TPK;
        keySpec = GetKeySpecifier(masterKey);

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, ((keySpec != null && keySpec != "") ? keySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, masterKey.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] TranslateKey (String masterKey, String keyType, String key) {
        String commandCode, keySpec, masterKeySpec;
        byte[] command;

        commandCode = null;
        if (keyType.equals(ThalesHSMConst.ZPK_TYPE))
            commandCode = ThalesHSMConst.Commands.TRANSLATE_ZPK;

        masterKeySpec = GetKeySpecifier(masterKey);
        keySpec = GetKeySpecifier(key);

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, ((masterKeySpec != null && masterKeySpec != "") ? masterKeySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, masterKey.getBytes());
        command = ArrayUtils.addAll(command, ((keySpec != null && keySpec != "") ? keySpec.getBytes() : null));
        command = ArrayUtils.addAll(command, key.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] ARQCValidationARPCGeneration (String acctNo, String tranData, String ARQC,
                                                  String authRespCode, String macKey, String channelId,
                                                  String panSeqNo, String atc, String unIdentifiedNo) {

        String commandCode, keySpecMacKey, modeFlag, schemeID, tranDataLen, delimiter;
        byte[] command;

        commandCode = ThalesHSMConst.Commands.ARQC_VALIDATE_ARPC_GENERATE_KEY;
        modeFlag = ThalesHSMConst.ARQCAction.VERIFY_ARQC_GENERATE_ARPC;

        schemeID = null;
        if (channelId.equals(ChannelCodes.VISA_BASE_I))
            schemeID = ThalesHSMConst.SchemeID.VISA;
        else if (channelId.equals(ChannelCodes.MASTERCARD))
            schemeID = ThalesHSMConst.SchemeID.EUROPAY_MASTERCARD;

        delimiter = ";";

        keySpecMacKey = GetKeySpecifier(macKey);

        acctNo = GetUpdatedAccountNo(acctNo);
        acctNo = GetStringLength(acctNo) + acctNo;

        tranDataLen = StringUtils.leftPad(Integer.toString(tranData.length()), 2, "0");

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, modeFlag.getBytes());
        command = ArrayUtils.addAll(command, schemeID.getBytes());
        command = ArrayUtils.addAll(command, ((keySpecMacKey != null && keySpecMacKey != "") ? keySpecMacKey.getBytes() : null));
        command = ArrayUtils.addAll(command, macKey.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());
        command = ArrayUtils.addAll(command, panSeqNo.getBytes());
        command = ArrayUtils.addAll(command, tranDataLen.getBytes());
        command = ArrayUtils.addAll(command, atc.getBytes());
        command = ArrayUtils.addAll(command, unIdentifiedNo.getBytes());
        command = ArrayUtils.addAll(command, delimiter.getBytes());
        command = ArrayUtils.addAll(command, ARQC.getBytes());
        command = ArrayUtils.addAll(command, authRespCode.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] ARQCValidation (String acctNo, String tranData, String ARQC,
                                    String authRespCode, String macKey, String channelId,
                                    String panSeqNo, String atc, String unIdentifiedNo) {

        String commandCode, keySpecMacKey, modeFlag, schemeID, tranDataLen, delimiter;
        byte[] command;

        commandCode = ThalesHSMConst.Commands.ARQC_VALIDATE_ARPC_GENERATE_KEY;
        modeFlag = ThalesHSMConst.ARQCAction.VERIFY_ARQC_ONLY;

        schemeID = null;
        if (channelId.equals(ChannelCodes.VISA_BASE_I))
            schemeID = ThalesHSMConst.SchemeID.VISA;
        else if (channelId.equals(ChannelCodes.MASTERCARD))
            schemeID = ThalesHSMConst.SchemeID.EUROPAY_MASTERCARD;

        delimiter = ";";

        keySpecMacKey = GetKeySpecifier(macKey);

        acctNo = GetUpdatedAccountNo(acctNo);
        acctNo = GetStringLength(acctNo) + acctNo;

        tranDataLen = StringUtils.leftPad(Integer.toString(tranData.length()), 2, "0");

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, modeFlag.getBytes());
        command = ArrayUtils.addAll(command, schemeID.getBytes());
        command = ArrayUtils.addAll(command, ((keySpecMacKey != null && keySpecMacKey != "") ? keySpecMacKey.getBytes() : null));
        command = ArrayUtils.addAll(command, macKey.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());
        command = ArrayUtils.addAll(command, panSeqNo.getBytes());
        command = ArrayUtils.addAll(command, tranDataLen.getBytes());
        command = ArrayUtils.addAll(command, atc.getBytes());
        command = ArrayUtils.addAll(command, unIdentifiedNo.getBytes());
        command = ArrayUtils.addAll(command, delimiter.getBytes());
        command = ArrayUtils.addAll(command, ARQC.getBytes());
        command = ArrayUtils.addAll(command, authRespCode.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] CVVValidation (String cvk, String cvv, String acctNo, String expiry, String serviceCode) {

        String commandCode, delimiter;
        byte[] command;

        commandCode = ThalesHSMConst.Commands.CVV_VERIFY;
        delimiter = ";";

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, cvk.getBytes());
        command = ArrayUtils.addAll(command, cvv.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());
        command = ArrayUtils.addAll(command, delimiter.getBytes());
        command = ArrayUtils.addAll(command, expiry.getBytes());
        command = ArrayUtils.addAll(command, serviceCode.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    /*
    public byte[] CVVValidation (String cvka, String cvkb, String cvv, String acctNo, String expiry,
                                   String serviceCode) {

        String commandCode, delimiter;
        byte[] command;

        commandCode = ThalesHSMConst.Commands.CVV_VERIFY;
        delimiter = ";";

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, cvka.getBytes());
        command = ArrayUtils.addAll(command, cvkb.getBytes());
        command = ArrayUtils.addAll(command, cvv.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());
        command = ArrayUtils.addAll(command, delimiter.getBytes());
        command = ArrayUtils.addAll(command, expiry.getBytes());
        command = ArrayUtils.addAll(command, serviceCode.getBytes());

        command = AddMessageHeader(command);

        return command;
    }
    */

    public byte[] CVVGeneration (String cvk, String acctNo, String expiry, String serviceCode) {

        String commandCode, delimiter;
        byte[] command;

        commandCode = ThalesHSMConst.Commands.CVV_GENERATE;
        delimiter = ";";

        command = null;
        command = ArrayUtils.addAll(command, commandCode.getBytes());
        command = ArrayUtils.addAll(command, cvk.getBytes());
        command = ArrayUtils.addAll(command, acctNo.getBytes());
        command = ArrayUtils.addAll(command, delimiter.getBytes());
        command = ArrayUtils.addAll(command, expiry.getBytes());
        command = ArrayUtils.addAll(command, serviceCode.getBytes());

        command = AddMessageHeader(command);

        return command;
    }

    public byte[] AddMessageHeader (byte[] command) {
        String lenInHexString;

        if (messageHeader > 255)
            messageHeader = 0;

        lenInHexString = StringUtils.leftPad(Integer.toHexString(++messageHeader).toUpperCase(), 4, "0");
        return (ArrayUtils.addAll(lenInHexString.getBytes(), command));
    }

    public String GetKeySpecifier (String key) {
        String keySpecifier;

        if (key.length() == 32) {
            keySpecifier = ThalesHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC;
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

    public String GetUpdatedAccountNo (String acctNo) {
        Integer acctNoLength;

        acctNoLength = acctNo.length();
        if (acctNoLength > 12)
            acctNo = acctNo.substring((acctNoLength-13), (acctNoLength-1));

        return acctNo;
    }
}
