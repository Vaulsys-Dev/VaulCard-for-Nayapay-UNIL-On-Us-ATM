package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.base.config.TransactionStatusType;
import vaulsys.protocols.ndc.constants.CassetteSupplyStatus;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.constants.RejectBinSupplyStatus;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCCashHandler extends NDCDeviceStatusInfo {
    
	public TransactionStatusType transactionStatus;

    public int[] notesDispensed = new int[4];
    public String notesDispensedS;
    public ErrorSeverity[] errorSeverity = new ErrorSeverity[5];
    public String MStatusDiagnostic;
    public String MDataDiagnostic;
    public RejectBinSupplyStatus rejectBinSuppliesStatus;
    public CassetteSupplyStatus[] cassetteSuppliesStatus = new CassetteSupplyStatus[4];

	public NDCCashHandler() {
		super();
	}

	public NDCCashHandler(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        transactionStatus = TransactionStatusType.getByType((char) rawdata[offset.value++]);
        notesDispensed[0] = Integer.parseInt(new String(rawdata, offset.value, 2));
        notesDispensed[1] = Integer.parseInt(new String(rawdata, offset.value + 2, 2));
        notesDispensed[2] = Integer.parseInt(new String(rawdata, offset.value + 4, 2));
        notesDispensed[3] = Integer.parseInt(new String(rawdata, offset.value + 6, 2));
        notesDispensedS = new String(rawdata, offset.value, 8);
        offset.value += 8;
        NDCParserUtils.readFS(rawdata, offset);
       
        errorSeverity[0] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
        errorSeverity[1] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
        errorSeverity[2] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
        errorSeverity[3] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
        errorSeverity[4] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
        NDCParserUtils.readFS(rawdata, offset);
        
        MStatusDiagnostic = new String(rawdata, offset.value, 2);
        offset.value += 2;
        MDataDiagnostic = NDCParserUtils.readUntilFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        rejectBinSuppliesStatus = RejectBinSupplyStatus.getByCode((char) rawdata[offset.value++]);
        cassetteSuppliesStatus[0] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
        cassetteSuppliesStatus[1] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
        cassetteSuppliesStatus[2] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
        cassetteSuppliesStatus[3] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
    }

    public String toString() {
        return super.toString()
                + "transactionStatus:\t\t" + transactionStatus.getType() + "\r\n"
                + "notesDispensedCassette1:\t\t" + notesDispensed[0] + "\r\n"
                + "notesDispensedCassette2:\t\t" + notesDispensed[1] + "\r\n"
                + "notesDispensedCassette3:\t\t" + notesDispensed[2] + "\r\n"
                + "notesDispensedCassette4:\t\t" + notesDispensed[3] + "\r\n"
                + "Sevirity Cassette 0:\t\t" + errorSeverity[0].toString() + "\r\n"
                + "Sevirity Cassette 1:\t\t" + errorSeverity[1].toString() + "\r\n"
                + "Sevirity Cassette 2:\t\t" + errorSeverity[2].toString() + "\r\n"
                + "Sevirity Cassette 3:\t\t" + errorSeverity[3].toString() + "\r\n"
                + "Sevirity Cassette 4:\t\t" + errorSeverity[4].toString() + "\r\n"
                + "MStatusDiagnostic:\t\t" + MStatusDiagnostic + "\r\n"
                + "MDataDiagnostic:\t\t" + MDataDiagnostic + "\r\n"
                + "rejectBinSuppliesStatus:\t\t" + rejectBinSuppliesStatus.toString() + "\r\n"
                + "cassetteSuppliesStatus[1]:\t\t" + cassetteSuppliesStatus[0].toString() + "\r\n"
                + "cassetteSuppliesStatus[2]:\t\t" + cassetteSuppliesStatus[1].toString() + "\r\n"
                + "cassetteSuppliesStatus[3]:\t\t" + cassetteSuppliesStatus[2].toString() + "\r\n"
                + "cassetteSuppliesStatus[4]:\t\t" + cassetteSuppliesStatus[3].toString() + "\r\n";
    }

    public static NDCCashHandler getSuppliesStatus(byte[] rawdata, MyInteger offset){
    	NDCCashHandler cashHandler = new NDCCashHandler();
    	cashHandler.rejectBinSuppliesStatus = RejectBinSupplyStatus.getByCode((char) rawdata[offset.value++]);
    	cashHandler.cassetteSuppliesStatus[0] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
    	cashHandler.cassetteSuppliesStatus[1] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
    	cashHandler.cassetteSuppliesStatus[2] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
    	cashHandler.cassetteSuppliesStatus[3] = CassetteSupplyStatus.getByCode((char) rawdata[offset.value++]);
    	return cashHandler;
    }
    
    public static NDCCashHandler getFitnessStatus(byte[] rawdata, MyInteger offset){
    	NDCCashHandler cashHandler = new NDCCashHandler();
    	cashHandler.errorSeverity[0] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
    	cashHandler.errorSeverity[1] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
    	cashHandler.errorSeverity[2] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
    	cashHandler.errorSeverity[3] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
    	cashHandler.errorSeverity[4] = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
    	return cashHandler;
    }
}
