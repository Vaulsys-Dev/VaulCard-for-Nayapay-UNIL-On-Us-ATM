package vaulsys.protocols.ndc.base.config;

import vaulsys.terminal.atm.device.CardBin;
import vaulsys.terminal.atm.device.CassetteA;
import vaulsys.terminal.atm.device.CassetteB;
import vaulsys.terminal.atm.device.CassetteC;
import vaulsys.terminal.atm.device.CassetteD;
import vaulsys.terminal.atm.device.CurrencyRejectBin;
import vaulsys.terminal.atm.device.DepositBin;
import vaulsys.terminal.atm.device.DeviceLocation;
import vaulsys.terminal.atm.device.Door;
import vaulsys.terminal.atm.device.ElectronicsEnclosureSensor;
import vaulsys.terminal.atm.device.SilentSignalSensor;
import vaulsys.terminal.atm.device.VibrationAndOrHeatSensor;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

public class SensorStatus {
    transient static Logger logger = Logger.getLogger(SensorStatus.class);

    public DeviceLocation door;
    public DeviceLocation silentSignal;
    public DeviceLocation vibration;
    public DeviceLocation electronicEncl;
    public DeviceLocation depositBin;
    public DeviceLocation cardBin;
    public DeviceLocation rejectCassete;
    public DeviceLocation cassette1;
    public DeviceLocation cassette2;
    public DeviceLocation cassette3;
    public DeviceLocation cassette4;

    public SensorStatus(byte[] data) {
        int index = 0;
        index += 2;
        door = DeviceLocation.getByChar((char) data[index++]);
        vibration = DeviceLocation.getByChar((char) data[index++]);
        electronicEncl = DeviceLocation.getByChar((char) data[index++]);
        depositBin = DeviceLocation.getByChar((char) data[index++]);
        cardBin = DeviceLocation.getByChar((char) data[index++]);
        rejectCassete = DeviceLocation.getByChar((char) data[index++]);
        cassette1 = DeviceLocation.getByChar((char) data[index++]);
        cassette2 = DeviceLocation.getByChar((char) data[index++]);
        cassette3 = DeviceLocation.getByChar((char) data[index++]);
        cassette4 = DeviceLocation.getByChar((char) data[index]);
    }

    public SensorStatus(byte[] data, MyInteger offset) {
        vibration = DeviceLocation.getByChar((char) data[offset.value++]);
        door = DeviceLocation.getByChar((char) data[offset.value++]);
        silentSignal = DeviceLocation.getByChar((char) data[offset.value++]);
        electronicEncl = DeviceLocation.getByChar((char) data[offset.value++]);
        depositBin = DeviceLocation.getByChar((char) data[offset.value++]);
        cardBin = DeviceLocation.getByChar((char) data[offset.value++]);
        rejectCassete = DeviceLocation.getByChar((char) data[offset.value++]);
        cassette1 = DeviceLocation.getByChar((char) data[offset.value++]);
        cassette2 = DeviceLocation.getByChar((char) data[offset.value++]);
        cassette3 = DeviceLocation.getByChar((char) data[offset.value++]);
        cassette4 = DeviceLocation.getByChar((char) data[offset.value]);
    }

    public void updateStatus(ATMTerminal terminal) {
        VibrationAndOrHeatSensor vibration = terminal.getDevice(VibrationAndOrHeatSensor.class);
        vibration.setLocation(this.getVibration());

        Door door = terminal.getDevice(Door.class);
        door.setLocation(this.getDoor());

        SilentSignalSensor silentSignal = terminal.getDevice(SilentSignalSensor.class);
        silentSignal.setLocation(this.getSilentSignal());

        ElectronicsEnclosureSensor electronicsEncl = terminal.getDevice(ElectronicsEnclosureSensor.class);
        electronicsEncl.setLocation(this.getElectronicEncl());

        DepositBin deposit = terminal.getDevice(DepositBin.class);
        deposit.setLocation(this.getDepositBin());

        CardBin card = terminal.getDevice(CardBin.class);
        card.setLocation(this.getCardBin());

        CurrencyRejectBin currencyReject = terminal.getDevice(CurrencyRejectBin.class);
        currencyReject.setLocation(this.getRejectCassete());

        CassetteA cassetteA = terminal.getDevice(CassetteA.class);
        cassetteA.setLocation(this.getCassette1());

        CassetteB cassetteB = terminal.getDevice(CassetteB.class);
        cassetteB.setLocation(this.getCassette2());

        CassetteC cassetteC = terminal.getDevice(CassetteC.class);
        cassetteC.setLocation(this.getCassette3());

        CassetteD cassetteD = terminal.getDevice(CassetteD.class);
        cassetteD.setLocation(this.getCassette4());
    }

   
	public DeviceLocation getDoor() {
		return door;
	}

	public void setDoor(DeviceLocation door) {
		this.door = door;
	}

	public DeviceLocation getSilentSignal() {
		return silentSignal;
	}

	public void setSilentSignal(DeviceLocation silentSignal) {
		this.silentSignal = silentSignal;
	}

	public DeviceLocation getVibration() {
		return vibration;
	}

	public void setVibration(DeviceLocation vibration) {
		this.vibration = vibration;
	}

	public DeviceLocation getElectronicEncl() {
		return electronicEncl;
	}

	public void setElectronicEncl(DeviceLocation electronicEncl) {
		this.electronicEncl = electronicEncl;
	}

	public DeviceLocation getDepositBin() {
		return depositBin;
	}

	public void setDepositBin(DeviceLocation depositBin) {
		this.depositBin = depositBin;
	}

	public DeviceLocation getCardBin() {
		return cardBin;
	}

	public void setCardBin(DeviceLocation cardBin) {
		this.cardBin = cardBin;
	}

	public DeviceLocation getRejectCassete() {
		return rejectCassete;
	}

	public void setRejectCassete(DeviceLocation rejectCassete) {
		this.rejectCassete = rejectCassete;
	}

	public DeviceLocation getCassette1() {
		return cassette1;
	}

	public void setCassette1(DeviceLocation cassette1) {
		this.cassette1 = cassette1;
	}

	public DeviceLocation getCassette2() {
		return cassette2;
	}

	public void setCassette2(DeviceLocation cassette2) {
		this.cassette2 = cassette2;
	}

	public DeviceLocation getCassette3() {
		return cassette3;
	}

	public void setCassette3(DeviceLocation cassette3) {
		this.cassette3 = cassette3;
	}

	public DeviceLocation getCassette4() {
		return cassette4;
	}

	public void setCassette4(DeviceLocation cassette4) {
		this.cassette4 = cassette4;
	}
	   
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Field field : getClass().getFields()) {
            try {
                result.append("\r\n" + field.getName() + ":\t\t" +
                        getClass().getField(field.getName()).get(this));
            } catch (Exception e) {
            	logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(),e);
            }
        }
        result.append("\r\n");
        return result.toString();
    }
}
