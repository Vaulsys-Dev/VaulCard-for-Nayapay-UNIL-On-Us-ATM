package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;
import vaulsys.util.NotYetImplementedException;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

public abstract class NDCDeviceStatusInfo {
    transient static Logger logger = Logger.getLogger(NDCDeviceStatusInfo.class);

    public NDCDeviceIdentifier deviceIdentifier;

    public static NDCDeviceStatusInfo fromBinary(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
		NDCDeviceIdentifier deviceIdentifier = NDCDeviceIdentifier.getByCode((char) rawdata[offset.value]);

		if (NDCDeviceIdentifier.CASH_HANDLER.equals(deviceIdentifier))
			return new NDCCashHandler(rawdata, offset);
		
		if (NDCDeviceIdentifier.ENCRYPTOR.equals(deviceIdentifier))
			return NDCEncryptor.fromBinary(rawdata, offset);
		
		if (NDCDeviceIdentifier.CARD_READER_WRITER.equals(deviceIdentifier))
			return NDCCardReaderWriter.fromBinary(rawdata, offset);
		
		if (NDCDeviceIdentifier.RECEIPT_PRINTER.equals(deviceIdentifier))
			return NDCReceiptPrinter.fromBinary(rawdata, offset);
		
		if (NDCDeviceIdentifier.JOURNAL_PRINTER.equals(deviceIdentifier))
			return NDCJournalPrinter.fromBinary(rawdata, offset);
		
		if (NDCDeviceIdentifier.DOOR_ACCESS.equals(deviceIdentifier))
			return new NDCDoorAccess(rawdata, offset);
		
		if (NDCDeviceIdentifier.SENSORS.equals(deviceIdentifier))
			return new NDCSensors(rawdata, offset);
		
		if (NDCDeviceIdentifier.COIN_DISPENCER.equals(deviceIdentifier))
			return new NDCCoinDispencer(rawdata, offset);
		
		if (NDCDeviceIdentifier.DIGITAL_AUDIO_SERVICE.equals(deviceIdentifier))
			return new NDCDigitalAudioService(rawdata, offset);
		
		if (NDCDeviceIdentifier.POWER_FAILURE.equals(deviceIdentifier))
			return new NDCPowerFailure(rawdata, offset);
		
		if (NDCDeviceIdentifier.DEPOSIT.equals(deviceIdentifier))
			return new NDCEnvelopeDepository(rawdata, offset);
		
		if (NDCDeviceIdentifier.SUPERVISOR_KEYS.equals(deviceIdentifier))
			return new NDCEnvelopeDepository(rawdata, offset);

		logger.error("UnsupportedOperationException: Undefined device identifier.");
		throw new UnsupportedOperationException("Undefined device identifier.");
	}

    public String toString() {
        String result = "";
        for (Field field : getClass().getFields()) {
            try {
                result += "\r\n" + field.getName() + ":\t\t" +
                        getClass().getField(field.getName()).get(this);
            } catch (Exception e) {
                logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(),e);
            }
        }
        result += "\r\n";
        return result;
     }

    public void updateStatus(ATMTerminal status) {
        throw new NotYetImplementedException("Must be owerride by subclasses.");
    }

}
