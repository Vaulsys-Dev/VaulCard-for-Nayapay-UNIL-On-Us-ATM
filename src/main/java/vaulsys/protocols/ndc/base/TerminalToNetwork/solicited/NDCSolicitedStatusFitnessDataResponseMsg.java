package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriter;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCJournalPrinter;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCReceiptPrinter;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
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
import vaulsys.util.constants.ASCIIConstants;

import org.apache.log4j.Logger;

public class NDCSolicitedStatusFitnessDataResponseMsg extends NDCSolicitedStatusTerminalStateMsg {
	transient static Logger logger = Logger.getLogger(NDCSolicitedStatusFitnessDataResponseMsg.class);
	public NDCCardReaderWriter cardReaderWriter;
	public NDCCashHandler cashHandler;
	public NDCReceiptPrinter receiptPrinter;
	public NDCJournalPrinter journalPrinter;
	
	public NDCSolicitedStatusFitnessDataResponseMsg(MyInteger offset,
			byte[] rawdata) throws NotParsedBinaryToProtocolException {
		super(offset, rawdata);
		offset.value++;
		
		while(offset.value < rawdata.length && rawdata[offset.value] != ASCIIConstants.FS){
			NDCDeviceIdentifier deviceIdentifier = NDCDeviceIdentifier.getByCode((char) rawdata[offset.value]);
	
			offset.value++;
			
			if (NDCDeviceIdentifier.CARD_READER_WRITER.equals(deviceIdentifier)){
				cardReaderWriter= NDCCardReaderWriter.getFitnessStatus(rawdata, offset);
			}else if (NDCDeviceIdentifier.CASH_HANDLER.equals(deviceIdentifier)){
				cashHandler = NDCCashHandler.getFitnessStatus(rawdata, offset);
			}else if (NDCDeviceIdentifier.RECEIPT_PRINTER.equals(deviceIdentifier)){
				receiptPrinter = NDCReceiptPrinter.getFitnessStatus(rawdata, offset);
			}else if (NDCDeviceIdentifier.JOURNAL_PRINTER.equals(deviceIdentifier)){
				journalPrinter = NDCJournalPrinter.getFitnessStatus(rawdata, offset);
			}else if (NDCDeviceIdentifier.COIN_DISPENCER.equals(deviceIdentifier)){
//				return new NDCCoinDispencer(rawdata, offset);
			}else if (NDCDeviceIdentifier.DEPOSIT.equals(deviceIdentifier)){
//				return new NDCEnvelopeDepository(rawdata, offset);
			}else{
			}

			NDCParserUtils.readUntilGS(rawdata, offset);
			offset.value++;
		}
	}

	public NDCSolicitedStatusFitnessDataResponseMsg() {
		statusDescriptor = NDCStatusDescriptor.TERMINAL_STATE;
	}
	

	public void updateStatus(ATMTerminal atm) {

		DateTime now = DateTime.now();
		
		if (receiptPrinter != null) {
			NDCReceiptPrinter receiptPrinterInfo = receiptPrinter;
			ReceiptPrinter receiptPrinterDevice = atm.getDevice(ReceiptPrinter.class);
			receiptPrinterDevice.setErrorSeverity(receiptPrinterInfo.errorSeverity);
			receiptPrinterDevice.setErrorSeverityDate(now);
			
			GeneralDao.Instance.saveOrUpdate(receiptPrinterDevice);
		}
		
		if (journalPrinter != null) {
			NDCJournalPrinter journalPrinterInfo = journalPrinter;
			JournalPrinter journalPrinterDevice = atm.getDevice(JournalPrinter.class);
			journalPrinterDevice.setErrorSeverity(journalPrinterInfo.errorSeverity);
			journalPrinterDevice.setErrorSeverityDate(now);
			
			GeneralDao.Instance.saveOrUpdate(journalPrinterDevice);
		}
		
		if (cashHandler != null) {
			NDCCashHandler cashHandlerInfo = cashHandler;
			
			CassetteA cassetteADecvice = atm.getDevice(CassetteA.class);
			cassetteADecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[1]);
			CassetteB cassetteBDecvice = atm.getDevice(CassetteB.class);
			cassetteBDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[2]);
			CassetteC cassetteCDecvice = atm.getDevice(CassetteC.class);
			cassetteCDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[3]);
			CassetteD cassetteDDecvice = atm.getDevice(CassetteD.class);
			cassetteDDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[4]);
			
			cassetteADecvice.setErrorSeverityDate(now);
			cassetteBDecvice.setErrorSeverityDate(now);
			cassetteCDecvice.setErrorSeverityDate(now);
			cassetteDDecvice.setErrorSeverityDate(now);
			
			ErrorSeverity errorSeverity0 = cashHandlerInfo.errorSeverity[0];
			if(errorSeverity0 != null && !ErrorSeverity.UNKNOWN.equals(errorSeverity0)) {
				cassetteADecvice.setTotalErrorSeverity(errorSeverity0);
				cassetteBDecvice.setTotalErrorSeverity(errorSeverity0);
				cassetteCDecvice.setTotalErrorSeverity(errorSeverity0);
				cassetteDDecvice.setTotalErrorSeverity(errorSeverity0);
	        }
			
			GeneralDao.Instance.saveOrUpdate(cassetteADecvice);
			GeneralDao.Instance.saveOrUpdate(cassetteBDecvice);
			GeneralDao.Instance.saveOrUpdate(cassetteCDecvice);
			GeneralDao.Instance.saveOrUpdate(cassetteDDecvice);
		}
		
		GeneralDao.Instance.saveOrUpdate(atm);
		
//		if(prevStatus != null){
//			ATMLog log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" + ReceiptPrinterStatus.getByCode(prevStatus.getStatus()).toString(), "LAST_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//			
//			log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" + printer.getStatus().toString(), "NEXT_STATE", ActionType.DEVICE_UPDATE);
//			GeneralDao.Instance.saveOrUpdate(log);
//		}
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder();
		st.append(super.toString() + "\r\n");
		
        if (cardReaderWriter != null)
        	st.append("SeverityCardReaderWriter:\t\t" + cardReaderWriter.errorSeverity.toString() + "\r\n");
    	
        if (cashHandler != null) {
	    	st.append("Severity Cassette 0:\t\t" + cashHandler.errorSeverity[0].toString() + "\r\n" +
	        "SeverityCass1:\t\t" + cashHandler.errorSeverity[1].toString() + "\r\n" +
	        "SeverityCass2:\t\t" + cashHandler.errorSeverity[2].toString() + "\r\n" +
	        "SeverityCass3:\t\t" + cashHandler.errorSeverity[3].toString() + "\r\n" +
	        "SeverityCass4:\t\t" + cashHandler.errorSeverity[4].toString() + "\r\n");
        }
         
        if (receiptPrinter != null)
        	st.append("SeverityReceiptPrinter:\t\t" + receiptPrinter.errorSeverity.toString() + "\r\n");
    	
        if (journalPrinter != null)
        	st.append("SeverityJournalPrinter:\t\t" + journalPrinter.errorSeverity.toString() + "\r\n");

        return st.toString();
	}
}
