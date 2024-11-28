package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriter;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCJournalPrinter;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCReceiptPrinter;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.terminal.atm.device.JournalPrinter;
import vaulsys.terminal.atm.device.ReceiptPrinter;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;
import vaulsys.util.constants.ASCIIConstants;

import org.apache.log4j.Logger;

public class NDCSolicitedStatusSuppliesDataResponseMsg extends NDCSolicitedStatusTerminalStateMsg {
	transient static Logger logger = Logger.getLogger(NDCSolicitedStatusSuppliesDataResponseMsg.class);
	NDCCardReaderWriter cardReaderWriter;
	NDCCashHandler cashHandler;
	NDCReceiptPrinter receiptPrinter;
	NDCJournalPrinter journalPrinter;
	
	public NDCSolicitedStatusSuppliesDataResponseMsg(MyInteger offset,
			byte[] rawdata) throws NotParsedBinaryToProtocolException {
		super(offset, rawdata);
		offset.value++;
		
		while(offset.value < rawdata.length && rawdata[offset.value] != ASCIIConstants.FS){
			NDCDeviceIdentifier deviceIdentifier = NDCDeviceIdentifier.getByCode((char) rawdata[offset.value]);
	
			offset.value++;
			
			if (NDCDeviceIdentifier.CARD_READER_WRITER.equals(deviceIdentifier)){
				cardReaderWriter= NDCCardReaderWriter.getSuppliesStatus(rawdata, offset);
			}else if (NDCDeviceIdentifier.CASH_HANDLER.equals(deviceIdentifier)){
				cashHandler = NDCCashHandler.getSuppliesStatus(rawdata, offset);
			}else if (NDCDeviceIdentifier.RECEIPT_PRINTER.equals(deviceIdentifier)){
				receiptPrinter = NDCReceiptPrinter.getSuppliesStatus(rawdata, offset);
			}else if (NDCDeviceIdentifier.JOURNAL_PRINTER.equals(deviceIdentifier)){
				journalPrinter = NDCJournalPrinter.getSuppliesStatus(rawdata, offset);
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

	public NDCSolicitedStatusSuppliesDataResponseMsg() {
		statusDescriptor = NDCStatusDescriptor.TERMINAL_STATE;
	}
	

	public void updateStatus(ATMTerminal atm) {
		NDCReceiptPrinter receiptPrinterInfo = receiptPrinter;
		ReceiptPrinter receiptPrinterDevice = atm.getDevice(ReceiptPrinter.class);
		receiptPrinterDevice.setPaperStatus(receiptPrinterInfo.paperStatus);
		receiptPrinterDevice.setRibbonStatus(receiptPrinterInfo.ribbonStatus);
		receiptPrinterDevice.setPrintheadStatus(receiptPrinterInfo.printheadStatus);
		receiptPrinterDevice.setKnifeStatus(receiptPrinterInfo.knifeStatus);

		NDCJournalPrinter journalPrinterInfo = journalPrinter;
		JournalPrinter journalPrinterDevice = atm.getDevice(JournalPrinter.class);
		journalPrinterDevice.setPaperStatus(journalPrinterInfo.paperStatus);
		journalPrinterDevice.setRibbonStatus(journalPrinterInfo.ribbonStatus);
		journalPrinterDevice.setPrintheadStatus(journalPrinterInfo.printheadStatus);
		if (journalPrinterInfo.knifeStatus != null) 
			journalPrinterDevice.setKnifeStatus(journalPrinterInfo.knifeStatus);

		/***************
		NDCCashHandler cashHandlerInfo = cashHandler;
		CassetteA cassetteADecvice = atm.getDevice(CassetteA.class);
		cassetteADecvice.set(cashHandlerInfo.cassetteSuppliesStatus[1]);
		CassetteB cassetteBDecvice = atm.getDevice(CassetteB.class);
		cassetteBDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[2]);
		CassetteC cassetteCDecvice = atm.getDevice(CassetteC.class);
		cassetteCDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[3]);
		CassetteD cassetteDDecvice = atm.getDevice(CassetteD.class);
		cassetteDDecvice.setErrorSeverity(cashHandlerInfo.errorSeverity[4]);
		 **/

		GeneralDao.Instance.saveOrUpdate(receiptPrinterDevice);
		GeneralDao.Instance.saveOrUpdate(journalPrinterDevice);
//		GeneralDao.Instance.saveOrUpdate(cassetteADecvice);
//		GeneralDao.Instance.saveOrUpdate(cassetteBDecvice);
//		GeneralDao.Instance.saveOrUpdate(cassetteCDecvice);
//		GeneralDao.Instance.saveOrUpdate(cassetteDDecvice);
		GeneralDao.Instance.saveOrUpdate(atm);

		// if(prevStatus != null){
		// ATMLog log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" +
		// ReceiptPrinterStatus.getByCode(prevStatus.getStatus()).toString(),
		// "LAST_STATE", ActionType.DEVICE_UPDATE);
		// GeneralDao.Instance.saveOrUpdate(log);
		//		
		// log = new ATMLog(atm.getCode(), "RECEIPT_PRINTER:" +
		// printer.getStatus().toString(), "NEXT_STATE",
		// ActionType.DEVICE_UPDATE);
		// GeneralDao.Instance.saveOrUpdate(log);
		// }
	}

	@Override
	public String toString() {
		String result = super.toString(); 
		result += "\r\n" +
		"Supplies Status:\r\n" +
		
		"Card Reader Writer:\t\t" + cardReaderWriter.suppliesStatus.toString() + "\r\n";
		
		if (cashHandler != null) {
			result += 
			"RejectBin:\t\t" + cashHandler.rejectBinSuppliesStatus.toString() + "\r\n" +
	        "cassette[1]:\t\t" + cashHandler.cassetteSuppliesStatus[0].toString() + "\r\n" +
	        "cassette[2]:\t\t" + cashHandler.cassetteSuppliesStatus[1].toString() + "\r\n" +
	        "cassette[3]:\t\t" + cashHandler.cassetteSuppliesStatus[2].toString() + "\r\n" +
	        "cassette[4]:\t\t" + cashHandler.cassetteSuppliesStatus[3].toString() + "\r\n";
		}
		
		if (receiptPrinter != null) {
	        result +=
	        "Receipt Paper:\t\t" + receiptPrinter.paperStatus.toString() + "\r\n" +
			"Receipt Ribbon:\t\t" + receiptPrinter.ribbonStatus.toString() + "\r\n" +
			"Receipt Printhead:\t\t" + receiptPrinter.printheadStatus.toString() + "\r\n" +
			"Receipt Knife:\t\t" + receiptPrinter.knifeStatus.toString() + "\r\n";
		}
		
		if (journalPrinter != null) {
			result +=
			"Journal Paper:\t\t" + journalPrinter.paperStatus.toString() + "\r\n" +
			"Journal Ribbon:\t\t" + journalPrinter.ribbonStatus.toString() + "\r\n" +
			"Journal Printhead:\t\t" + journalPrinter.printheadStatus.toString() + "\r\n" +
			(journalPrinter.knifeStatus != null ? 
					("Journal Knife:\t\t" + journalPrinter.knifeStatus.toString() + "\r\n") :
						""
			)
			;
		}
		return result;
	}
}
