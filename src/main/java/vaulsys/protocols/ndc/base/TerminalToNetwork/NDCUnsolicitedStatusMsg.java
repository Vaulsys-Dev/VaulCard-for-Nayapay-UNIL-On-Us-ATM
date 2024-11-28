package vaulsys.protocols.ndc.base.TerminalToNetwork;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCardReaderWriterStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCashHandlerStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCoinDispenserStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedDigitalAudioServiceStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedDoorAccessStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedEncryptorStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedJournalPrinterStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedPowerFailureStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedReceiptPrinterStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSecurityCameraStatusMsg;
//import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedRetractMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSupervisorKeyStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCDeviceStatusInfo;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.constants.NDCMessageClassSolicitedUnSokicited;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;
import vaulsys.util.NotYetImplementedException;


public abstract class NDCUnsolicitedStatusMsg<T extends NDCDeviceStatusInfo> extends NDCTerminalToNetworkMsg {

    public T statusInformation;

    public NDCUnsolicitedStatusMsg() {
        messageType = NDCMessageClassTerminalToNetwork.STATUS_MESSAGE;
        solicited = NDCMessageClassSolicitedUnSokicited.UNSOLICITED_MESSAGE;
    }

    protected NDCUnsolicitedStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        messageType = NDCMessageClassTerminalToNetwork.STATUS_MESSAGE;
        solicited = NDCMessageClassSolicitedUnSokicited.UNSOLICITED_MESSAGE;
        logicalUnitNumber = Long.valueOf(NDCParserUtils.readUntilFS(rawdata, offset));
        NDCParserUtils.readFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
    }

    public Ifx toIfx() throws Exception {
        throw new UnsupportedOperationException("Cannot convert to Ifx message");
    }

    public static ProtocolMessage fromBinary(byte[] rawdata, int index) throws NotParsedBinaryToProtocolException {
		NDCDeviceIdentifier deviceIdentifier = NDCDeviceIdentifier.getByCode(findDeviceIdentifier(rawdata, index));
		MyInteger offset = new MyInteger(index);

		if (NDCDeviceIdentifier.CASH_HANDLER.equals(deviceIdentifier))
			return new NDCUnsolicitedCashHandlerStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.ENCRYPTOR.equals(deviceIdentifier))
			return new NDCUnsolicitedEncryptorStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.CARD_READER_WRITER.equals(deviceIdentifier))
			return new NDCUnsolicitedCardReaderWriterStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.RECEIPT_PRINTER.equals(deviceIdentifier))
			return new NDCUnsolicitedReceiptPrinterStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.JOURNAL_PRINTER.equals(deviceIdentifier))
			return new NDCUnsolicitedJournalPrinterStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.DOOR_ACCESS.equals(deviceIdentifier))
			return new NDCUnsolicitedDoorAccessStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.SENSORS.equals(deviceIdentifier))
			return new NDCUnsolicitedSensorsStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.COIN_DISPENCER.equals(deviceIdentifier))
			return new NDCUnsolicitedCoinDispenserStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.DIGITAL_AUDIO_SERVICE.equals(deviceIdentifier))
			return new NDCUnsolicitedDigitalAudioServiceStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.POWER_FAILURE.equals(deviceIdentifier))
			return new NDCUnsolicitedPowerFailureStatusMsg(rawdata, offset);
		
		if (NDCDeviceIdentifier.SUPERVISOR_KEYS.equals(deviceIdentifier))
			return new NDCUnsolicitedSupervisorKeyStatusMsg(rawdata, offset);
		

		if (NDCDeviceIdentifier.SECURITY_CAMERA.equals(deviceIdentifier))
			return new NDCUnsolicitedSecurityCameraStatusMsg(rawdata, offset);

		//if (NDCDeviceIdentifier.RETRACT.equals(deviceIdentifier))
			//return new NDCUnsolicitedRetractMsg(rawdata,offset);

		throw new NotYetImplementedException("Undefined device identifier.");
	}

    private static char findDeviceIdentifier(byte[] rawdata, int index) throws NotParsedBinaryToProtocolException {
        MyInteger offset = new MyInteger(index);
        NDCParserUtils.readUntilFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        return (char)rawdata[offset.value];
    }

    public String toString() {
    	return super.toString() + "statusInfo:\t\t" + statusInformation + "\r\n";

    }

    public void updateStatus(ATMTerminal terminal) {
        statusInformation.updateStatus(terminal);
    }

}
