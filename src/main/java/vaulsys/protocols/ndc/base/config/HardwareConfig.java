package vaulsys.protocols.ndc.base.config;

import vaulsys.util.MyInteger;

public class HardwareConfig {

    private String productClass;
    private String systemDisk;
    private String cardReader;
    private String cashHandler;
    private String depository;
    private String receiptPrinter;
    private String journalPaper;
    private String nightSafeDepository;
    private String encryptor;
    private String securityCamera;
    private String doorAccess;
    private String flexDisk;
    private String tamperIndicatingBins;
    private String cardHolderKeyboard;
    private String operatorKeyboard;
    private String cardHolderDisplay;
    private String statementPrinter;

    @Override
    public String toString() {
    	return 
    	"\r\n" +
    	"productClass:\t\t" + productClass + "\r\n" +
    	"cardReader:\t\t" + cardReader + "\r\n" +
    	"cashHandler:\t\t" + cashHandler + "\r\n" +
    	"depository:\t\t" + depository + "\r\n" +
    	"receiptPrinter:\t\t" + receiptPrinter + "\r\n" +
    	"journalPaper:\t\t" + journalPaper + "\r\n" +
    	"depository2:\t\t" + nightSafeDepository + "\r\n" +
    	"encryptor:\t\t" + encryptor + "\r\n" +
    	"securityCamera:\t\t" + securityCamera + "\r\n" +
    	"doorAccess:\t\t" + doorAccess + "\r\n" +
    	"flexDisk:\t\t" + flexDisk + "\r\n" +
    	"tamperIndicatingBins:\t\t" + tamperIndicatingBins + "\r\n" +
    	"cardHolderKeyboard:\t\t" + cardHolderKeyboard + "\r\n" +
    	"operatorKeyboard:\t\t" + operatorKeyboard + "\r\n" +
    	"cardHolderDisplay:\t\t" + cardHolderDisplay + "\r\n" +
    	"statementPrinter:\t\t" + statementPrinter + "\r\n"
        ;
    }
    
    public HardwareConfig(byte[] rawdata) {
        MyInteger offset = new MyInteger(0);
        
        productClass = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        offset.value += 2;
        
        systemDisk = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        cardReader = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        cashHandler = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        depository = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        receiptPrinter = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        journalPaper = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        offset.value += 2;
        
        offset.value += 2;
        
        nightSafeDepository = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        encryptor = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        securityCamera = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        doorAccess = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        flexDisk = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        tamperIndicatingBins = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        cardHolderKeyboard = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        operatorKeyboard = new String(rawdata, offset.value, 2);
        offset.value += 2;
        
        cardHolderDisplay = new String(rawdata, offset.value, 2);
        offset.value += 2;

//        byte chd = data[index++];
//        chd &= 11110000;
//        cardHolderDisplay = (char) chd;
        
        offset.value += 2;
        
        offset.value += 2;
        
        statementPrinter = new String(rawdata, offset.value, 2);
        offset.value += 2;
    }

	public String getProductClass() {
		return productClass;
	}

	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}

	public String getSystemDisk() {
		return systemDisk;
	}

	public void setSystemDisk(String systemDisk) {
		this.systemDisk = systemDisk;
	}

	public String getCardReader() {
		return cardReader;
	}

	public void setCardReader(String cardReader) {
		this.cardReader = cardReader;
	}

	public String getCashHandler() {
		return cashHandler;
	}

	public void setCashHandler(String cashHandler) {
		this.cashHandler = cashHandler;
	}

	public String getDepository() {
		return depository;
	}

	public void setDepository(String depository) {
		this.depository = depository;
	}

	public String getReceiptPrinter() {
		return receiptPrinter;
	}

	public void setReceiptPrinter(String receiptPrinter) {
		this.receiptPrinter = receiptPrinter;
	}

	public String getJournalPaper() {
		return journalPaper;
	}

	public void setJournalPaper(String journalPaper) {
		this.journalPaper = journalPaper;
	}

	public String getNightSafeDepository() {
		return nightSafeDepository;
	}

	public void setNightSafeDepository(String nightSafeDepository) {
		this.nightSafeDepository = nightSafeDepository;
	}

	public String getEncryptor() {
		return encryptor;
	}

	public void setEncryptor(String encryptor) {
		this.encryptor = encryptor;
	}

	public String getSecurityCamera() {
		return securityCamera;
	}

	public void setSecurityCamera(String securityCamera) {
		this.securityCamera = securityCamera;
	}

	public String getDoorAccess() {
		return doorAccess;
	}

	public void setDoorAccess(String doorAccess) {
		this.doorAccess = doorAccess;
	}

	public String getFlexDisk() {
		return flexDisk;
	}

	public void setFlexDisk(String flexDisk) {
		this.flexDisk = flexDisk;
	}

	public String getTamperIndicatingBins() {
		return tamperIndicatingBins;
	}

	public void setTamperIndicatingBins(String tamperIndicatingBins) {
		this.tamperIndicatingBins = tamperIndicatingBins;
	}

	public String getCardHolderKeyboard() {
		return cardHolderKeyboard;
	}

	public void setCardHolderKeyboard(String cardHolderKeyboard) {
		this.cardHolderKeyboard = cardHolderKeyboard;
	}

	public String getOperatorKeyboard() {
		return operatorKeyboard;
	}

	public void setOperatorKeyboard(String operatorKeyboard) {
		this.operatorKeyboard = operatorKeyboard;
	}

	public String getCardHolderDisplay() {
		return cardHolderDisplay;
	}

	public void setCardHolderDisplay(String cardHolderDisplay) {
		this.cardHolderDisplay = cardHolderDisplay;
	}

	public String getStatementPrinter() {
		return statementPrinter;
	}

	public void setStatementPrinter(String statementPrinter) {
		this.statementPrinter = statementPrinter;
	}

}
