package vaulsys.security.hsm.atalla;

import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.util.encoders.Hex;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by HP on 8/9/2016.
 */
public class AtallaHSMHandler {

    Logger logger = Logger.getLogger(AtallaHSMHandler.class);
    private static AtallaHSMHandler AtallaHSMHandler = null;
    private static Integer messageHeader = 0;

    public static AtallaHSMHandler getInstance() {
        if (AtallaHSMHandler == null) {
            AtallaHSMHandler = new AtallaHSMHandler();
        }
        return AtallaHSMHandler;
    }

    //m.rehman: 22-11-2021, HSM response logging
    //adding sequence parameter as a context
    public byte[] PINValidation (String pinBlock, String pinOffset, String acctNo, String pekKey, String pvkKey,
                                 String pinLength, String sequence) {

        String commandStart, command, commandCode, pinGenerateMethod, separator, pinFormat, commandEnd, convertionTable,
                pad, contextStart;

        commandStart = AtallaHSMConst.COMMAND_START;
        commandCode = AtallaHSMConst.Commands.PIN_VERIFY;
        pinFormat = AtallaHSMConst.PINFormat.ANSI;
        pinGenerateMethod = AtallaHSMConst.PINVerificationMethod.IBM3624;
        separator = AtallaHSMConst.SEPARATOR;
        commandEnd = AtallaHSMConst.COMMAND_END;
        convertionTable = "0123456789012345";
        pad = "F";
        contextStart = "^";

        acctNo = GetUpdatedAccountNo(acctNo);

        command = commandStart + commandCode + separator + pinGenerateMethod + separator + pinFormat +
                separator + pinBlock + separator + pekKey + separator + convertionTable + separator + pinOffset +
                separator + acctNo + separator + pad + separator + pinLength + separator + pvkKey + separator + acctNo +
                separator + contextStart + sequence + separator + commandEnd;

        logger.info("PIN Validation command [" + command + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
        return command.getBytes();
    }

    //m.rehman: 22-11-2021, HSM response logging
    //adding sequence parameter as a context
    public byte[] PINGeneration (String pinBlock, String acctNo, String pekKey, String pvkKey, String sequence) {

        String commandStart, command, commandCode, separator, pinFormat, commandEnd, bankId, conversionTable, pad, contextStart;

        commandStart = AtallaHSMConst.COMMAND_START;
        commandCode = AtallaHSMConst.Commands.PIN_GENERATE;
        pinFormat = AtallaHSMConst.PINFormat.ANSI;
        separator = AtallaHSMConst.SEPARATOR;
        commandEnd = AtallaHSMConst.COMMAND_END;
        bankId = "26";
        conversionTable = "0123456789012345";
        pad = "F";
        contextStart = "^";

        acctNo = GetUpdatedAccountNo(acctNo);

        command = commandStart + commandCode + separator + pinFormat + separator + pinBlock + separator + pekKey +
                separator + bankId + separator + acctNo + separator + conversionTable + separator + acctNo + separator +
                pad + separator + pvkKey + separator + acctNo + separator + contextStart + sequence + separator + commandEnd;

        logger.info("PIN Generation command [" + command + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
        return command.getBytes();
    }

    //m.rehman: 22-11-2021, HSM response logging
    //adding sequence parameter as a context
    public byte[] PINChange (String pinBlock, String newPinBlock, String pinOffset, String acctNo, String pekKey,
                             String pvkKey, String pinLength, String sequence) {

        String commandStart, command, commandCode, pinGenerateMethod, separator, pinFormat, commandEnd, conversionTable,
                pad, contextStart;

        commandStart = AtallaHSMConst.COMMAND_START;
        commandCode = AtallaHSMConst.Commands.PIN_CHANGE;
        pinFormat = AtallaHSMConst.PINFormat.ANSI;
        pinGenerateMethod = AtallaHSMConst.PINVerificationMethod.IBM3624;
        separator = AtallaHSMConst.SEPARATOR;
        commandEnd = AtallaHSMConst.COMMAND_END;
        conversionTable = "0123456789012345";
        pad = "F";
        contextStart = "^";

        acctNo = GetUpdatedAccountNo(acctNo);

        command = commandStart + commandCode + separator + pinGenerateMethod + separator + pinFormat + separator +
                pinBlock + separator + pekKey + separator + conversionTable + separator + pinOffset + separator +
                acctNo + separator + pad + separator + pinLength + separator + pvkKey + separator +
                newPinBlock + separator + acctNo + separator + contextStart + sequence + separator + commandEnd;

        logger.info("PIN Generation command [" + command + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
        return command.getBytes();
    }

    //m.rehman: 22-11-2021, HSM response logging
    //adding sequence parameter as a context
    public byte[] PINTranslation (String pinBlock, String acctNo, String sourceKey, String destinationKey, String sequence) {

        String commandStart, command, commandCode, pinFormat, separator, commandEnd, contextStart;

        commandStart = AtallaHSMConst.COMMAND_START;
        commandCode = AtallaHSMConst.Commands.PIN_TRANSLATE;
        pinFormat = AtallaHSMConst.PINFormat.ANSI;
        separator = AtallaHSMConst.SEPARATOR;
        commandEnd = AtallaHSMConst.COMMAND_END;
        contextStart = "^";

        acctNo = GetUpdatedAccountNo(acctNo);

        command = commandStart + commandCode + separator + pinFormat + separator + sourceKey + separator +
                destinationKey + separator + pinBlock + separator + acctNo + separator + contextStart + sequence + separator + commandEnd;

        logger.debug("PIN Translate command [" + command + "]");
        return command.getBytes();
    }

    public byte[] GenerateKey (String masterKey, String keyType) {
        String commandCode, keySpec;
        byte[] command;

        commandCode = AtallaHSMConst.Commands.GENERATE_TPK;
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
        if (keyType.equals(AtallaHSMConst.ZPK_TYPE))
            commandCode = AtallaHSMConst.Commands.TRANSLATE_ZPK;

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

    //m.rehman: 22-11-2021, HSM response logging
    //adding sequence parameter as a context
    public byte[] ARQCValidationARPCGeneration (String acctNo, String tranData, String ARQC,
                                                  String authRespCode, String macKey, String channelId,
                                                  String panSeqNo, String atc, String unIdentifiedNo, String sequence) {

        String command, commandCode, derivationType, tranDataLen, diversificationData, separator, commandStart, commandEnd, contextStart;

        commandStart = AtallaHSMConst.COMMAND_START;
        commandCode = AtallaHSMConst.Commands.ARQC_VALIDATE_ARPC_GENERATE;
        separator = AtallaHSMConst.SEPARATOR;
        commandEnd = AtallaHSMConst.COMMAND_END;
        contextStart = "^";

        //TODO: need to update this logic
        derivationType = "";
        diversificationData = "";
        if (channelId.equals(ChannelCodes.VISA_BASE_I)) {
            derivationType = AtallaHSMConst.SchemeID.VISA;
            diversificationData = "";
        }
        else if (channelId.equals(ChannelCodes.MASTERCARD)) {
            derivationType = AtallaHSMConst.SchemeID.EUROPAY_MASTERCARD;
            diversificationData = atc + "0000" + unIdentifiedNo;
        }
        else if (channelId.equals(ChannelCodes.UNION_PAY)) {
            derivationType = AtallaHSMConst.SchemeID.UNIONPAY_INTL;
            diversificationData = atc;

        }
		//m.rehman: Euronet integration
        //m.rehman: 10-12-2021, VP-NAP-202111291 / VC-NAP-202111291 / VG-NAP-202111291 - Meezan ATM On-Us Withdrawal/Balance Inquiry and Reversal
        //adding meezan bank on us atm channel
        //Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
        else if (channelId.equals(ChannelCodes.ONELINK) || channelId.equals(ChannelCodes.EURONET) || channelId.equals(ChannelCodes.UNILONUSATM)) {
            derivationType = AtallaHSMConst.SchemeID.COMMON_SESSION;
            diversificationData = atc + "000000000000";
        }

        command = commandStart + commandCode + separator + derivationType + separator + macKey + separator + acctNo + separator +
                panSeqNo + separator + diversificationData + separator + ARQC + separator + tranData + separator +
                authRespCode + separator + contextStart + sequence + separator + commandEnd;

        logger.info("ARQC Validation ARPC Generation command [" + command + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
        return command.getBytes();
    }

    //m.rehman: 22-11-2021, HSM response logging
    //adding sequence parameter as a context
    public byte[] CVVValidation (String cvk, String cvkB, String cvv, String acctNo, String expiry, String serviceCode, String sequence) {

        String commandStart, command, commandCode, algorithm, separator, commandEnd, contextStart;

        commandStart = AtallaHSMConst.COMMAND_START;
        commandCode = AtallaHSMConst.Commands.CVV_VERIFY;
        separator = AtallaHSMConst.SEPARATOR;
        commandEnd = AtallaHSMConst.COMMAND_END;
        algorithm = AtallaHSMConst.CVVAlgorithm.STANDARD_ALGO;
        contextStart = "^";

        command = commandStart + commandCode + separator + algorithm + separator + cvk + separator +cvkB + separator +
                acctNo + expiry + serviceCode + separator + cvv + separator + contextStart + sequence + separator + commandEnd;

        logger.info("CVV Validation command [" + command + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
        return command.getBytes();
    }

    //m.rehman: 22-11-2021, HSM response logging
    //adding sequence parameter as a context
    public byte[] CVVGeneration (String cvk, String cvkB, String acctNo, String expiry, String serviceCode, String sequence) {

        String commandStart, command, commandCode, algorithm, separator, commandEnd, contextStart;

        commandStart = AtallaHSMConst.COMMAND_START;
        commandCode = AtallaHSMConst.Commands.CVV_GENERATE;
        separator = AtallaHSMConst.SEPARATOR;
        commandEnd = AtallaHSMConst.COMMAND_END;
        algorithm = AtallaHSMConst.CVVAlgorithm.STANDARD_ALGO;
        contextStart = "^";

        command = commandStart + commandCode + separator + algorithm + separator + cvk + separator + cvkB + separator +
                acctNo + expiry + serviceCode + separator + contextStart + sequence + separator + commandEnd;

        logger.info("CVV Generation command [" + command + "]"); // Asim Shahzad, Date : 3rd Nov 2021, Tracking ID : VC-NAP-202111031
        return command.getBytes();
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
            keySpecifier = AtallaHSMConst.KeySpecifierFormat.DOUBLE_LEN_KEY_SPEC;
        } else {
            keySpecifier = "";
        }

        return keySpecifier;
    }

    public String GetUpdatedAccountNo (String acctNo) {
        Integer acctNoLength;

        acctNoLength = acctNo.length();
        if (acctNoLength > 12)
            acctNo = acctNo.substring((acctNoLength-13), (acctNoLength-1));

        return acctNo;
    }
}
