package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.NDCUnsolicitedDeviceStatusTypes;
import vaulsys.protocols.ndc.base.config.SensorStatus;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;

public class NDCSensors extends NDCDeviceStatusInfo {
    public SensorStatus deviceStatusDescription;
    public byte deviceStatusIndicator;
    public byte supervisorMode;

    public NDCSensors(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        deviceStatusIndicator = rawdata[offset.value++];
        supervisorMode = rawdata[offset.value++];
        if (isSensorChange())
            deviceStatusDescription = new SensorStatus(rawdata, offset);// , rawdata.length - offset.value);
    }

    public boolean isSensorChange() {
        if (deviceStatusIndicator == NDCUnsolicitedDeviceStatusTypes.SENSOR_CHANGE)
            return true;
        return false;
    }

    public boolean isSupervisorEntry() {
        if (deviceStatusIndicator == NDCUnsolicitedDeviceStatusTypes.MODE_CHANGE) {
            if (supervisorMode == '1')
                return true;
        }
        return false;
    }

    public boolean isSupervisorExit() {
        if (deviceStatusIndicator == NDCUnsolicitedDeviceStatusTypes.MODE_CHANGE) {
            if (supervisorMode == '0')
                return true;
        }
        return false;
    }

    public boolean isAlarmStatusChange() {
        if (deviceStatusIndicator == NDCUnsolicitedDeviceStatusTypes.ALARM_STATE_CHANE) {
            return true;
        }
        return false;
    }

    public void updateStatus(ATMTerminal terminal) {
        deviceStatusDescription.updateStatus(terminal);
    }
}
