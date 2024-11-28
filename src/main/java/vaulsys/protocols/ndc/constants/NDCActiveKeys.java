package vaulsys.protocols.ndc.constants;

import java.io.ByteArrayOutputStream;

public class NDCActiveKeys {
    public byte allNumericKeys;
    public byte opKeyA;
    public byte opKeyB;
    public byte opKeyC;
    public byte opKeyD;
    public byte cancelKey;
    public byte opKeyF;
    public byte opKeyG;
    public byte opKeyH;
    public byte opKeyI;

    public NDCActiveKeys(byte allNumericKeys, byte opKeyA, byte opKeyB, byte opKeyC, byte opKeyD, byte cancelKey, byte opKeyF, byte opKeyG, byte opKeyH, byte opKeyI) {
        this.allNumericKeys = allNumericKeys;
        this.opKeyA = opKeyA;
        this.opKeyB = opKeyB;
        this.opKeyC = opKeyC;
        this.opKeyD = opKeyD;
        this.cancelKey = cancelKey;
        this.opKeyF = opKeyF;
        this.opKeyG = opKeyG;
        this.opKeyH = opKeyH;
        this.opKeyI = opKeyI;
    }

    public NDCActiveKeys(boolean allNumericKeys, boolean opKeyA, boolean opKeyB, boolean opKeyC, boolean opKeyD, boolean cancelKey, boolean opKeyF, boolean opKeyG, boolean opKeyH, boolean opKeyI) {
        this.allNumericKeys = convert(allNumericKeys);
        this.opKeyA = convert(opKeyA);
        this.opKeyB = convert(opKeyB);
        this.opKeyC = convert(opKeyC);
        this.opKeyD = convert(opKeyD);
        this.cancelKey = convert(cancelKey);
        this.opKeyF = convert(opKeyF);
        this.opKeyG = convert(opKeyG);
        this.opKeyH = convert(opKeyH);
        this.opKeyI = convert(opKeyI);
    }

    public byte[] getBytes() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(allNumericKeys);
        result.write(opKeyA);
        result.write(opKeyB);
        result.write(opKeyC);
        result.write(opKeyD);
        result.write(cancelKey);
        result.write(opKeyF);
        result.write(opKeyG);
        result.write(opKeyH);
        result.write(opKeyI);
        return result.toByteArray();
    }

    private byte convert(boolean value) {
        if (value)
            return '1';
        return '0';
    }
    
    private boolean convertToBoolean(byte value) {
    	if (value == '1')
    		return true;
    	return false;
    }
    
    @Override
    public String toString() {
    	return
    	"\r\n"
    	+ "allNumericKeys:\t\t" + convertToBoolean(allNumericKeys) + "\r\n"
    	+ "opKeyA:\t\t" + convertToBoolean(opKeyA) + "\r\n"
    	+ "opKeyB:\t\t" + convertToBoolean(opKeyB) + "\r\n"
    	+ "opKeyC:\t\t" + convertToBoolean(opKeyC) + "\r\n"
    	+ "opKeyD:\t\t" + convertToBoolean(opKeyD) + "\r\n"
    	+ "cancelKey:\t\t" + convertToBoolean(cancelKey) + "\r\n"
    	+ "opKeyF:\t\t" + convertToBoolean(opKeyF) + "\r\n"
    	+ "opKeyG:\t\t" + convertToBoolean(opKeyG) + "\r\n"
    	+ "opKeyH:\t\t" + convertToBoolean(opKeyH) + "\r\n"
    	+ "opKeyI:\t\t" + convertToBoolean(opKeyI) + "\r\n"
    	;
    }

}
