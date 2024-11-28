package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.config.HardwareConfig;
import vaulsys.protocols.ndc.base.config.SensorStatus;
import vaulsys.protocols.ndc.base.config.SupplyStatus;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCHardwareFitness;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.terminal.atm.device.CassetteA;
import vaulsys.terminal.atm.device.CassetteB;
import vaulsys.terminal.atm.device.CassetteC;
import vaulsys.terminal.atm.device.CassetteD;
import vaulsys.terminal.atm.device.JournalPrinter;
import vaulsys.terminal.atm.device.ReceiptPrinter;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;

public class NDCSolicitedStatusConfigTerminalStateMsg extends NDCSolicitedStatusTerminalStateMsg {
    public String configId;
    public NDCHardwareFitness hardwareFitness;
    public HardwareConfig hardwareConfig;
    public SupplyStatus supplyStatus;
    public SensorStatus sensorStatus;
    public String ndcReleaseNo;
    public String ndcSoftwareId;

    public NDCSolicitedStatusConfigTerminalStateMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        super(offset, rawdata);
//        NDCParserUtils.readFS(rawdata, offset);

//        configId = new String(rawdata, offset.value, 4);
//        offset.value += 4;
        
        configId = NDCParserUtils.readUntilFS(rawdata, offset);
        
        NDCParserUtils.readFS(rawdata, offset);
        hardwareFitness = new NDCHardwareFitness(NDCParserUtils.readUntilFS(rawdata, offset).getBytes());
        NDCParserUtils.readFS(rawdata, offset);
        hardwareConfig = new HardwareConfig(NDCParserUtils.readUntilFS(rawdata, offset).getBytes());
        NDCParserUtils.readFS(rawdata, offset);
        supplyStatus = new SupplyStatus(NDCParserUtils.readUntilFS(rawdata, offset).getBytes());
        NDCParserUtils.readFS(rawdata, offset);
        sensorStatus = new SensorStatus(NDCParserUtils.readUntilFS(rawdata, offset).getBytes());
        if(offset.value == rawdata.length)
        	return;
        
        NDCParserUtils.readFS(rawdata, offset);
        ndcReleaseNo = NDCParserUtils.readUntilFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        ndcSoftwareId = NDCParserUtils.readUntilFS(rawdata, offset);
    }

    public NDCSolicitedStatusConfigTerminalStateMsg() {
        statusDescriptor = NDCStatusDescriptor.TERMINAL_STATE;
    }

 public void updateStatus(ATMTerminal terminal) {
    	
    	DateTime now = DateTime.now();
    	/*************/
    		
		CassetteA cassetteA = terminal.getDevice(CassetteA.class);
		
		cassetteA.setErrorSeverity(hardwareFitness.getCassette1());
		cassetteA.setTotalErrorSeverity(hardwareFitness.getCassette1());
//		cassetteA.setStatus(getATMTerminalService().getDeviceStatus(hardwareFitness.getCassette1()));
		cassetteA.setErrorSeverityDate(now);
		cassetteA.setSupplyStatus(supplyStatus.getCassete1());
        /*************/

        CassetteB cassetteB = terminal.getDevice(CassetteB.class);

        cassetteB.setErrorSeverity(hardwareFitness.getCassette2());
        cassetteB.setTotalErrorSeverity(hardwareFitness.getCassette2());
//        cassetteB.setStatus(getATMTerminalService().getDeviceStatus(hardwareFitness.getCassette2()));
        cassetteB.setErrorSeverityDate(now);
        cassetteB.setSupplyStatus(supplyStatus.getCassete2());
    	/*************/
		
		CassetteC cassetteC = terminal.getDevice(CassetteC.class);
		
		cassetteC.setErrorSeverity(hardwareFitness.getCassette3());
		cassetteC.setTotalErrorSeverity(hardwareFitness.getCassette3());
//		cassetteC.setStatus(getATMTerminalService().getDeviceStatus(hardwareFitness.getCassette3()));
		cassetteC.setErrorSeverityDate(now);
		cassetteC.setSupplyStatus(supplyStatus.getCassete3());
    	/*************/
		
		CassetteD cassetteD = terminal.getDevice(CassetteD.class);
		
		cassetteD.setErrorSeverity(hardwareFitness.getCassette4());
		cassetteD.setTotalErrorSeverity(hardwareFitness.getCassette4());
//		cassetteD.setStatus(getATMTerminalService().getDeviceStatus(hardwareFitness.getCassette4()));
		cassetteD.setErrorSeverityDate(now);
		cassetteD.setSupplyStatus(supplyStatus.getCassete4());
		/*************/
		
		ReceiptPrinter receiptPrinter = terminal.getDevice(ReceiptPrinter.class);
		
//		DeviceStatus printerDeviceStatus = getATMTerminalService().getDeviceStatus(hardwareFitness.getReceiptPrinter());
//		receiptPrinter.setStatus(printerDeviceStatus);
		receiptPrinter.setErrorSeverity(hardwareFitness.getReceiptPrinter());
		
		/*if (DeviceStatus.NORMAL.equals(printerDeviceStatus)) {
			receiptPrinter.setKnifeStatus(NDCSupplyStatusConstants.GOOD_STATE);
			receiptPrinter.setPaperStatus(NDCSupplyStatusConstants.GOOD_STATE);
			receiptPrinter.setRibbonStatus(NDCSupplyStatusConstants.GOOD_STATE);
			receiptPrinter.setPrintheadStatus(NDCSupplyStatusConstants.GOOD_STATE);
			
		} else if (DeviceStatus.FATAL.equals(printerDeviceStatus)) {
			receiptPrinter.setKnifeStatus(NDCSupplyStatusConstants.MEDIA_OUT);
			receiptPrinter.setPaperStatus(NDCSupplyStatusConstants.MEDIA_OUT);
			receiptPrinter.setRibbonStatus(NDCSupplyStatusConstants.MEDIA_OUT);
			receiptPrinter.setPrintheadStatus(NDCSupplyStatusConstants.MEDIA_OUT);
		}*/
		
		receiptPrinter.setErrorSeverityDate(now);
		receiptPrinter.setSupplyStatus(supplyStatus.getReceiptPaper());
		/*************/
		
		JournalPrinter journalPrinter = terminal.getDevice(JournalPrinter.class);
		
//		printerDeviceStatus = getATMTerminalService().getDeviceStatus(hardwareFitness.getJournalPrinter());
//		journalPrinter.setStatus(printerDeviceStatus);
		journalPrinter.setErrorSeverity(hardwareFitness.getJournalPrinter());
//		journalPrinter.setState(getATMTerminalService().getPrinterStatus(hardwareFitness.getJournalPrinter()));
		
		/*if (DeviceStatus.NORMAL.equals(printerDeviceStatus)) {
			journalPrinter.setKnifeStatus(NDCSupplyStatusConstants.GOOD_STATE);
			journalPrinter.setPaperStatus(NDCSupplyStatusConstants.GOOD_STATE);
			journalPrinter.setRibbonStatus(NDCSupplyStatusConstants.GOOD_STATE);
			journalPrinter.setPrintheadStatus(NDCSupplyStatusConstants.GOOD_STATE);
			
		} else if (DeviceStatus.FATAL.equals(printerDeviceStatus)) {
			journalPrinter.setKnifeStatus(NDCSupplyStatusConstants.MEDIA_OUT);
			journalPrinter.setPaperStatus(NDCSupplyStatusConstants.MEDIA_OUT);
			journalPrinter.setRibbonStatus(NDCSupplyStatusConstants.MEDIA_OUT);
			journalPrinter.setPrintheadStatus(NDCSupplyStatusConstants.MEDIA_OUT);
		}*/
		
		journalPrinter.setErrorSeverityDate(now);
		journalPrinter.setSupplyStatus(supplyStatus.getJournalPaper());
        
    }
    
	 @Override
	 public String toString() {
	 	return super.toString()+
	 			"ConfID:\t\t" + configId + "\r\n" +
	 	    	"\r\nhardwareFitness:\t\t" + hardwareFitness.toString() + "\r\n" +
	 	    	"hardwareConf:\t\t" + hardwareConfig.toString() + "\r\n" +
	 	    	"supplyStatus:\t\t" + supplyStatus.toString() + "\r\n" +
	 	    	"sensorStatus:\t\t" + sensorStatus.toString() + "\r\n" +
	 	    	"ndcReleaseNo:\t\t" + ndcReleaseNo + "\r\n" +
	 	    	"ndcSoftwareId:\t\t" + ndcSoftwareId + "\r\n";
	
	 }
}