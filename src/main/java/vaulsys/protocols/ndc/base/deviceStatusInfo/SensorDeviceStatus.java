package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.util.MyInteger;

public class SensorDeviceStatus {
    public byte vibration_heatSensor;
    public byte door;
    public byte silentSingnalSensor;
    public byte electronicEnclosureSensor;
    public byte depositBin;
    public byte cardBin;
    public byte currencyRejectBin;
    public byte cassetteAPosition;
    public byte cassetteBPosition;
    public byte cassetteCPosition;
    public byte cassetteDPosition;

    public SensorDeviceStatus(byte[] rawdata, MyInteger offset) {
        vibration_heatSensor = rawdata[offset.value++];
        door = rawdata[offset.value++];
        silentSingnalSensor = rawdata[offset.value++];
        electronicEnclosureSensor = rawdata[offset.value++];
        depositBin = rawdata[offset.value++];
        cardBin = rawdata[offset.value++];
        currencyRejectBin = rawdata[offset.value++];
        cassetteAPosition = rawdata[offset.value++];
        cassetteBPosition = rawdata[offset.value++];
        cassetteCPosition = rawdata[offset.value++];
        cassetteDPosition = rawdata[offset.value++];
    }
}
