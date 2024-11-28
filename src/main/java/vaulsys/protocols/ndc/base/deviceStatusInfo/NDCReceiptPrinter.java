package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;
import vaulsys.protocols.ndc.constants.ReceiptPrinterStatus;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCReceiptPrinter extends NDCDeviceStatusInfo {
    public ReceiptPrinterStatus deviceStatus;
    public ErrorSeverity errorSeverity;
    public String Mstatus;
    public String Mdata;

    public NDCSupplyStatusConstants paperStatus;
    public NDCSupplyStatusConstants ribbonStatus;
    public NDCSupplyStatusConstants printheadStatus;
    public NDCSupplyStatusConstants knifeStatus;

    public NDCReceiptPrinter() {
		super();
	}
    
    public static NDCReceiptPrinter fromBinary(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
    	return new NDCReceiptPrinter(rawdata, offset);
    	/*NDCPrinterStatus ds = NDCPrinterStatus.getByCode((char)rawdata[offset.value + 1]);
            if (NDCPrinterStatus.SUCCESSFUL_PRINT.equals(ds))
                return new NDCReceiptPrinterSuccessfulPrint(rawdata, offset);
            
            if (NDCPrinterStatus.PRINT_OPERATION_NOT_SUCCESS_COMPLETED.equals(ds))
                return new NDCReceiptPrinterNotSuccessfulPrint(rawdata, offset);
            
            if (NDCPrinterStatus.DEVICE_NOT_CONFIGURED.equals(ds) ||
            		NDCPrinterStatus.CANCEL_KEY_PRESSED_DURING_SIDEWAYS_RECEIPT_PRINT.equals(ds))
                return new NDCReceiptPrinterNotConfigured(rawdata, offset);
            
        return null;*/
    }

    public NDCReceiptPrinter(byte[] rawdata, MyInteger offset)
            throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        deviceStatus = ReceiptPrinterStatus.getByCode((char) rawdata[offset.value++]);
        NDCParserUtils.readFS(rawdata, offset);
        errorSeverity = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
        if (offset.value < rawdata.length) {
            NDCParserUtils.readFS(rawdata, offset);
            Mstatus = new String(rawdata, offset.value, 2);
            offset.value += 2;
            Mdata = NDCParserUtils.readUntilFS(rawdata, offset);
            NDCParserUtils.readFS(rawdata, offset);
            paperStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
            ribbonStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
            printheadStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
            knifeStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
            //should I read anything else at the end? such as FS or ...
        }
    }
    
	public static NDCReceiptPrinter getSuppliesStatus(byte[] rawdata, MyInteger offset){
    	NDCReceiptPrinter receiptPrinter = new NDCReceiptPrinter();
    	receiptPrinter.paperStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	receiptPrinter.ribbonStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	receiptPrinter.printheadStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	receiptPrinter.knifeStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	return receiptPrinter;
    }
	
	public static NDCReceiptPrinter getFitnessStatus(byte[] rawdata, MyInteger offset){
    	NDCReceiptPrinter receiptPrinter = new NDCReceiptPrinter();
    	receiptPrinter.errorSeverity = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
//    	receiptPrinter.ribbonStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
//    	receiptPrinter.printheadStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
//    	receiptPrinter.knifeStatus = NDCSupplyStatusConstants.getByCode((char) rawdata[offset.value++]);
    	return receiptPrinter;
    }
}
