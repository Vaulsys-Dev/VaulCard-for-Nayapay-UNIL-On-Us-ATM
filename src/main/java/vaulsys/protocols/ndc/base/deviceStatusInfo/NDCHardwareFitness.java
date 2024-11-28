package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.ndc.base.config.ErrorSeverity;

public class NDCHardwareFitness {
    private ErrorSeverity timeOfDayClock;
    private ErrorSeverity highOrderCommunications;
    private ErrorSeverity systemDisk;
    private ErrorSeverity cardReader;
    private ErrorSeverity cashHandler;
    private ErrorSeverity depository;
    private ErrorSeverity receiptPrinter;
    private ErrorSeverity journalPrinter;
    private ErrorSeverity enhancedThermalStatementPrinter;
    private ErrorSeverity nightSafeDepository;
    private ErrorSeverity encryptor;
    private ErrorSeverity securityCamera;
    private ErrorSeverity doorAccess;
    private ErrorSeverity flexDisk;
    private ErrorSeverity cassette1;
    private ErrorSeverity cassette2;
    private ErrorSeverity cassette3;
    private ErrorSeverity cassette4;
    private ErrorSeverity statementPrinter;

    @Override
    public String toString() {
    	return 
    	"\r\n" +
    	"timeOfDayClock:\t\t" + timeOfDayClock.toString() + "\r\n" +
    	"highOrderCommunications:\t\t" + highOrderCommunications.toString() + "\r\n" +
    	"systemDisk:\t\t" + systemDisk.toString() + "\r\n" +
    	"cardReader:\t\t" + cardReader.toString() + "\r\n" +
    	"cashHandler:\t\t" + cashHandler.toString() + "\r\n" +
    	"depository:\t\t" + depository.toString() + "\r\n" +
    	"receiptPrinter:\t\t" + receiptPrinter.toString() + "\r\n" +
    	"journalPrinter:\t\t" + journalPrinter.toString() + "\r\n" +
    	"enhancedThermalStatementPrinter:\t\t" + enhancedThermalStatementPrinter.toString() + "\r\n" +
    	"nightSafeDepository:\t\t" + nightSafeDepository.toString() + "\r\n" +
    	"encryptor:\t\t" + encryptor.toString() + "\r\n" +
    	"securityCamera:\t\t" + securityCamera.toString() + "\r\n" +
    	"doorAccess:\t\t" + doorAccess.toString() + "\r\n" +
    	"flexDisk:\t\t" + flexDisk.toString() + "\r\n" +
    	"cassette1:\t\t" + cassette1.toString() + "\r\n" +
    	"cassette2:\t\t" + cassette2.toString() + "\r\n" +
    	"cassette3:\t\t" + cassette3.toString() + "\r\n" +
    	"cassette4:\t\t" + cassette4.toString() + "\r\n" +
    	"statementPrinter:\t\t" + statementPrinter.toString() + "\r\n"
    	;
    }
    
    public NDCHardwareFitness(byte[] allData) {
        int index = 0;
        timeOfDayClock = ErrorSeverity.getByCode((char) allData[index++]);
        highOrderCommunications = ErrorSeverity.getByCode((char) allData[index++]);
        systemDisk = ErrorSeverity.getByCode((char) allData[index++]);
        cardReader = ErrorSeverity.getByCode((char) allData[index++]);
        cashHandler = ErrorSeverity.getByCode((char) allData[index++]);
        depository = ErrorSeverity.getByCode((char) allData[index++]);
        receiptPrinter = ErrorSeverity.getByCode((char) allData[index++]);
        journalPrinter = ErrorSeverity.getByCode((char) allData[index++]);
        index += 1;
        enhancedThermalStatementPrinter = ErrorSeverity.getByCode((char) allData[index++]);
        nightSafeDepository = ErrorSeverity.getByCode((char) allData[index++]);
        encryptor = ErrorSeverity.getByCode((char) allData[index++]);
        securityCamera = ErrorSeverity.getByCode((char) allData[index++]);
        doorAccess = ErrorSeverity.getByCode((char) allData[index++]);
        flexDisk = ErrorSeverity.getByCode((char) allData[index++]);
        cassette1 = ErrorSeverity.getByCode((char) allData[index++]);
        cassette2 = ErrorSeverity.getByCode((char) allData[index++]);
        cassette3 = ErrorSeverity.getByCode((char) allData[index++]);
        cassette4 = ErrorSeverity.getByCode((char) allData[index++]);
        index += 2;
        statementPrinter = ErrorSeverity.getByCode((char) allData[index++]);
    }

	public ErrorSeverity getTimeOfDayClock() {
		return timeOfDayClock;
	}

	public void setTimeOfDayClock(ErrorSeverity timeOfDayClock) {
		this.timeOfDayClock = timeOfDayClock;
	}

	public ErrorSeverity getHighOrderCommunications() {
		return highOrderCommunications;
	}

	public void setHighOrderCommunications(ErrorSeverity highOrderCommunications) {
		this.highOrderCommunications = highOrderCommunications;
	}

	public ErrorSeverity getSystemDisk() {
		return systemDisk;
	}

	public void setSystemDisk(ErrorSeverity systemDisk) {
		this.systemDisk = systemDisk;
	}

	public ErrorSeverity getCardReader() {
		return cardReader;
	}

	public void setCardReader(ErrorSeverity cardReader) {
		this.cardReader = cardReader;
	}

	public ErrorSeverity getCashHandler() {
		return cashHandler;
	}

	public void setCashHandler(ErrorSeverity cashHandler) {
		this.cashHandler = cashHandler;
	}

	public ErrorSeverity getDepository() {
		return depository;
	}

	public void setDepository(ErrorSeverity depository) {
		this.depository = depository;
	}

	public ErrorSeverity getReceiptPrinter() {
		return receiptPrinter;
	}

	public void setReceiptPrinter(ErrorSeverity receiptPrinter) {
		this.receiptPrinter = receiptPrinter;
	}

	public ErrorSeverity getJournalPrinter() {
		return journalPrinter;
	}

	public void setJournalPrinter(ErrorSeverity journalPrinter) {
		this.journalPrinter = journalPrinter;
	}

	public ErrorSeverity getEnhancedThermalStatementPrinter() {
		return enhancedThermalStatementPrinter;
	}

	public void setEnhancedThermalStatementPrinter(ErrorSeverity enhancedThermalStatementPrinter) {
		this.enhancedThermalStatementPrinter = enhancedThermalStatementPrinter;
	}

	public ErrorSeverity getNightSafeDepository() {
		return nightSafeDepository;
	}

	public void setNightSafeDepository(ErrorSeverity nightSafeDepository) {
		this.nightSafeDepository = nightSafeDepository;
	}

	public ErrorSeverity getEncryptor() {
		return encryptor;
	}

	public void setEncryptor(ErrorSeverity encryptor) {
		this.encryptor = encryptor;
	}

	public ErrorSeverity getSecurityCamera() {
		return securityCamera;
	}

	public void setSecurityCamera(ErrorSeverity securityCamera) {
		this.securityCamera = securityCamera;
	}

	public ErrorSeverity getDoorAccess() {
		return doorAccess;
	}

	public void setDoorAccess(ErrorSeverity doorAccess) {
		this.doorAccess = doorAccess;
	}

	public ErrorSeverity getFlexDisk() {
		return flexDisk;
	}

	public void setFlexDisk(ErrorSeverity flexDisk) {
		this.flexDisk = flexDisk;
	}

	public ErrorSeverity getCassette1() {
		return cassette1;
	}

	public void setCassette1(ErrorSeverity cassette1) {
		this.cassette1 = cassette1;
	}

	public ErrorSeverity getCassette2() {
		return cassette2;
	}

	public void setCassette2(ErrorSeverity cassette2) {
		this.cassette2 = cassette2;
	}

	public ErrorSeverity getCassette3() {
		return cassette3;
	}

	public void setCassette3(ErrorSeverity cassette3) {
		this.cassette3 = cassette3;
	}

	public ErrorSeverity getCassette4() {
		return cassette4;
	}

	public void setCassette4(ErrorSeverity cassette4) {
		this.cassette4 = cassette4;
	}

	public ErrorSeverity getStatementPrinter() {
		return statementPrinter;
	}

	public void setStatementPrinter(ErrorSeverity statementPrinter) {
		this.statementPrinter = statementPrinter;
	}

}
