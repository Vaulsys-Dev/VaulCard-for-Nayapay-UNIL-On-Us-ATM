package vaulsys.security.hsm.eracom.status;

public class HSMStatusResp {
    public byte returnCode;
    public byte ramStatus;
    public byte romStatus;
    public byte desStatus;
    public byte hostPortStatus;
    public byte batteryStatus;
    public byte hardDiskStatus;
    public byte rsaAccelarator;
    public byte performanceLevel;
    public short resetCount;
    public int callsInLastMin;
    public int calssIn10LastMins;
    public byte softwareIDLength;
    public String softwareID;

    public static final byte HW_PASSED = 0;
    public static final byte HW_FAILED = 1;
    public static final byte HW_NOT_FOUND = 2;

    public HSMStatusResp(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            ramStatus =
                    romStatus = desStatus = hostPortStatus = batteryStatus =
                            hardDiskStatus =
                                    rsaAccelarator = performanceLevel =
                                            softwareIDLength = -1;
            resetCount = -1;
            callsInLastMin = calssIn10LastMins = -1;
            softwareID = "";
            return;
        }

        ramStatus = result[offset++];
        romStatus = result[offset++];
        desStatus = result[offset++];
        hostPortStatus = result[offset++];
        batteryStatus = result[offset++];
        hardDiskStatus = result[offset++];
        rsaAccelarator = result[offset++];
        performanceLevel = result[offset++];
        resetCount =
                (short) ((result[offset] >= 0 ? result[offset++] : result[offset++] +
                        256) +
                        256 * (result[offset] >= 0 ? result[offset++] : result[offset++] +
                                256));
        callsInLastMin =
                (int) ((result[offset] >= 0 ? result[offset++] : result[offset++] +
                        256) +
                        256 * (result[offset] >= 0 ? result[offset++] : result[offset++] +
                                256) +
                        256 * 256 * (result[offset] >= 0 ? result[offset++] :
                                result[offset++] + 256) +
                        256 * 256 * 256 * (result[offset] >= 0 ?
                                result[offset++] :
                                result[offset++] + 256));
        calssIn10LastMins =
                (int) ((result[offset] >= 0 ? result[offset++] : result[offset++] +
                        256) +
                        256 * (result[offset] >= 0 ? result[offset++] : result[offset++] +
                                256) +
                        256 * 256 * (result[offset] >= 0 ? result[offset++] :
                                result[offset++] + 256) +
                        256 * 256 * 256 * (result[offset] >= 0 ?
                                result[offset++] :
                                result[offset++] + 256));
        softwareIDLength = result[offset++];
        softwareID = "";
        for (int j = 0; j < softwareIDLength; j++)
            softwareID += (char) result[offset++];
    }

    public String toString() {
        return "ramStatus: " + ramStatus + " romStatus: " + romStatus +
                " desStatus: " + desStatus + " hostPortStatus: " + hostPortStatus +
                " batteryStatus: " + batteryStatus + " hardDiskStatus: " +
                hardDiskStatus + " rsaAccelarator: " + rsaAccelarator +
                " performanceLevel: " + performanceLevel + " resetCount: " +
                resetCount + " callsInLastMin: " + callsInLastMin +
                " calssIn10LastMins: " + calssIn10LastMins +
                " softwareIDLength: " + softwareIDLength + " softwareID: " +
                softwareID;
    }
}
