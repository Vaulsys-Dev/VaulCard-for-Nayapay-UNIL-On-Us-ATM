package vaulsys.protocols.ndc.base.config;

import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;

public class SupplyStatus {

    private NDCSupplyStatusConstants cardCapturedBin;
    private NDCSupplyStatusConstants cardHandlerRejectBin;
    private NDCSupplyStatusConstants depositBin;
    private NDCSupplyStatusConstants receiptPaper;
    private NDCSupplyStatusConstants journalPaper;
    private NDCSupplyStatusConstants nightSafe;
    private NDCSupplyStatusConstants cassete1;
    private NDCSupplyStatusConstants cassete2;
    private NDCSupplyStatusConstants cassete3;
    private NDCSupplyStatusConstants cassete4;
    private NDCSupplyStatusConstants statementPaper;
    private NDCSupplyStatusConstants statementRibbon;

    @Override
    public String toString() {
    	return
    	"\r\n" +
    	"cardCapturedBin:\t\t" + cardCapturedBin.toString() + "\r\n" +
    	"cardHandlerRejectBin:\t\t" + cardHandlerRejectBin.toString() + "\r\n" +
    	"depositBin:\t\t" + depositBin.toString() + "\r\n" +
    	"receiptPaper:\t\t" + receiptPaper.toString() + "\r\n" +
    	"journalPaper:\t\t" + journalPaper.toString() + "\r\n" +
    	"nightSafe:\t\t" + nightSafe.toString() + "\r\n" +
    	"cassete1:\t\t" + cassete1.toString() + "\r\n" +
    	"cassete2:\t\t" + cassete2.toString() + "\r\n" +
    	"cassete3:\t\t" + cassete3.toString() + "\r\n" +
    	"cassete4:\t\t" + cassete4.toString() + "\r\n" +
    	"statementPaper:\t\t" + statementPaper.toString() + "\r\n" +
    	"statementRibbon:\t\t" + statementRibbon.toString() + "\r\n"
    	;
    }
    
    public SupplyStatus(byte[] data) {
        int index = 0;
        index += 3;
        cardCapturedBin = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        cardHandlerRejectBin = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        depositBin = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        receiptPaper = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        journalPaper = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        index += 2;
        nightSafe = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        index += 4;
        cassete1 = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        cassete2 = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        cassete3 = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        cassete4 = NDCSupplyStatusConstants.getByCode((char) data[index++]);
        index += 2;
        statementPaper = NDCSupplyStatusConstants.getByCode((char) data[index]);
        statementRibbon = NDCSupplyStatusConstants.getByCode((char) data[index]);
    }

	public NDCSupplyStatusConstants getCardCapturedBin() {
		return cardCapturedBin;
	}

	public void setCardCapturedBin(NDCSupplyStatusConstants cardCapturedBin) {
		this.cardCapturedBin = cardCapturedBin;
	}

	public NDCSupplyStatusConstants getCardHandlerRejectBin() {
		return cardHandlerRejectBin;
	}

	public void setCardHandlerRejectBin(NDCSupplyStatusConstants cardHandlerRejectBin) {
		this.cardHandlerRejectBin = cardHandlerRejectBin;
	}

	public NDCSupplyStatusConstants getDepositBin() {
		return depositBin;
	}

	public void setDepositBin(NDCSupplyStatusConstants depositBin) {
		this.depositBin = depositBin;
	}

	public NDCSupplyStatusConstants getReceiptPaper() {
		return receiptPaper;
	}

	public void setReceiptPaper(NDCSupplyStatusConstants receiptPaper) {
		this.receiptPaper = receiptPaper;
	}

	public NDCSupplyStatusConstants getJournalPaper() {
		return journalPaper;
	}

	public void setJournalPaper(NDCSupplyStatusConstants journalPaper) {
		this.journalPaper = journalPaper;
	}

	public NDCSupplyStatusConstants getCassete1() {
		return cassete1;
	}

	public void setCassete1(NDCSupplyStatusConstants cassete1) {
		this.cassete1 = cassete1;
	}

	public NDCSupplyStatusConstants getCassete2() {
		return cassete2;
	}

	public void setCassete2(NDCSupplyStatusConstants cassete2) {
		this.cassete2 = cassete2;
	}

	public NDCSupplyStatusConstants getCassete3() {
		return cassete3;
	}

	public void setCassete3(NDCSupplyStatusConstants cassete3) {
		this.cassete3 = cassete3;
	}

	public NDCSupplyStatusConstants getCassete4() {
		return cassete4;
	}

	public void setCassete4(NDCSupplyStatusConstants cassete4) {
		this.cassete4 = cassete4;
	}

	public NDCSupplyStatusConstants getStatementPaper() {
		return statementPaper;
	}

	public void setStatementPaper(NDCSupplyStatusConstants statementPaper) {
		this.statementPaper = statementPaper;
	}

}
