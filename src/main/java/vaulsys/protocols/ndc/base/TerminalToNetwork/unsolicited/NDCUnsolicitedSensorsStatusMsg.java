package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCSensors;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedSensorsStatusMsg extends NDCUnsolicitedStatusMsg<NDCSensors> {

    public NDCUnsolicitedSensorsStatusMsg() {
    }

    public NDCUnsolicitedSensorsStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = new NDCSensors(rawdata, offset);
    }

    public boolean isSensorChange() {
        return statusInformation.isSensorChange();
    }

    public boolean isSupervisorEntry() {
        return statusInformation.isSupervisorEntry();
    }

    public boolean isSupervisorExit() {
        return statusInformation.isSupervisorExit();
    }

    public boolean isAlarmStatusChange() {
        return statusInformation.isAlarmStatusChange();
    }

}
