package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.JournalPrinterStatus;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;
import vaulsys.util.constants.ASCIIConstants;

public class NDCJournalPrinter extends NDCDeviceStatusInfo {
    public JournalPrinterStatus deviceStatus;
    public ErrorSeverity errorSeverity;
    public String Mstatus;
    public String Mdata;
    public NDCSupplyStatusConstants paperStatus;
    public NDCSupplyStatusConstants ribbonStatus;
    public NDCSupplyStatusConstants printheadStatus;
    public NDCSupplyStatusConstants knifeStatus;

    public NDCJournalPrinter() {
		super();
	}

    public static NDCJournalPrinter fromBinary(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
    	return new NDCJournalPrinter(rawdata, offset);
/*    	NDCPrinterStatus ds = NDCPrinterStatus.getByCode((char)rawdata[offset.value + 1]);
        if (NDCPrinterStatus.SUCCESSFUL_PRINT.equals(ds))
                return new NDCJournalPrinterSuccessfulPrint(rawdata, offset);
            
        if (NDCPrinterStatus.PRINT_OPERATION_NOT_SUCCESS_COMPLETED.equals(ds))
                return new NDCJournalPrinterNotSuccessfulPrint(rawdata, offset);
            
        if (NDCPrinterStatus.DEVICE_NOT_CONFIGURED.equals(ds) ||
        		NDCPrinterStatus.CANCEL_KEY_PRESSED_DURING_SIDEWAYS_RECEIPT_PRINT.equals(ds))
                return new NDCJournalPrinterNotConfigured(rawdata, offset);
        
        return null;
*/    }

    public NDCJournalPrinter(byte[] rawdata, MyInteger offset)
            throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        if(rawdata[offset.value] == ASCIIConstants.FS)
        	NDCParserUtils.readFS(rawdata, offset);
        deviceStatus = JournalPrinterStatus.getByCode((char) rawdata[offset.value++]);
        NDCParserUtils.readFS(rawdata, offset);
        errorSeverity = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
        if (offset.value < rawdata.length) {
        	if(rawdata[offset.value] == ASCIIConstants.FS)
        		NDCParserUtils.readFS(rawdata, offset);
            Mstatus = new String(rawdata, offset.value, 2);
            offset.value += 2;
            Mdata = NDCParserUtils.readUntilFS(rawdata, offset);
            NDCParserUtils.readFS(rawdata, offset);
            paperStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
            ribbonStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
            printheadStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
            knifeStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
        }
    }

	public static NDCJournalPrinter getSuppliesStatus(byte[] rawdata, MyInteger offset){
    	NDCJournalPrinter journalPrinter = new NDCJournalPrinter();
    	journalPrinter.paperStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	journalPrinter.ribbonStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	journalPrinter.printheadStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	if(offset.value < rawdata.length)
    		journalPrinter.knifeStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	return journalPrinter;
    }
	
	public static NDCJournalPrinter getFitnessStatus(byte[] rawdata, MyInteger offset){
    	NDCJournalPrinter journalPrinter = new NDCJournalPrinter();
    	journalPrinter.errorSeverity = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
    	return journalPrinter;
    }
}
