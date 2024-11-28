package vaulsys.terminal.atm;

import vaulsys.log.Log;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "term_atm_log")
public class ATMLog extends Log {

	@Column(name = "terminal_code")
	Long terminalCode;

//	@Column(name = "transaction_id")
//	Long transactionId;

//	@Column(name = "card_captured")
//	boolean cardCaptured;

//	String dispensed;
//	Integer retracted;
//	Boolean done;
	
	@Column(name = "note_cassette")
	private String notesInCassette;
	
	@Column(name = "note_reject")
	private String notesRejected;
	
	@Column(name = "note_dispense")
	private String notesDispensed;
	
	@Column(name = "last_note_dispense")
	private String lastTrxNotesDispensed;
	
	@Column(name = "card_captured_no")
	private Integer cardCapturedNo;
	
	@Column(name = "device_status")
	private String deviceStatus;
	
	@Column(name = "desp")
	private String desc;
	

	@Embedded
	@AttributeOverride(name = "type", column = @Column(name = "action_type"))
	ActionType actionType;
	
	private Double balance;

//	@Embedded
//	@AttributeOverride(name = "type", column = @Column(name = "transaction_type"))
//	TrnType transactionType;

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public ATMLog() {
	}

	/*public ATMLog(Long terminalCode, Long transactionId, ActionType actionType) {
		this.terminalCode = terminalCode;
		this.transactionId = transactionId;
		this.actionType = actionType;
	}*/

	/*public ATMLog(Long terminalCode, Long transactionId, boolean cardCaptured, ActionType actionType, TrnType transactionType) {
		this.terminalCode = terminalCode;
		this.transactionId = transactionId;
		this.cardCaptured = cardCaptured;
		this.actionType = actionType;
		this.transactionType = transactionType;
	}*/

	/*public ATMLog(Long terminalCode, Long transactionId, String dispensed, ActionType actionType, TrnType transactionType) {
		this.terminalCode = terminalCode;
		this.transactionId = transactionId;
		this.dispensed = dispensed;
		this.actionType = actionType;
		this.transactionType = transactionType;
	}*/

	/*public ATMLog(Long terminalCode, Long transactionId, Integer retracted, ActionType actionType, TrnType transactionType) {
		this.terminalCode = terminalCode;
		this.transactionId = transactionId;
		this.retracted = retracted;
		this.actionType = actionType;
		this.transactionType = transactionType;
	}*/
	
	public ATMLog(Long terminalCode, String notesInCassette, String notesRejected, String notesDispensed, String lastTrxNotesDispensed, 
			Integer cardCapturedNo, String desc, ActionType actionType) {
		this.terminalCode = terminalCode;
		this.notesInCassette = notesInCassette;
		this.notesRejected = notesRejected;
		this.notesDispensed = notesDispensed;
		this.lastTrxNotesDispensed = lastTrxNotesDispensed;
		this.cardCapturedNo = cardCapturedNo;
		this.actionType = actionType;
		this.desc = desc;
	}
	
	public ATMLog(Long terminalCode, String status, String desc, ActionType actionType) {
		this.terminalCode = terminalCode;
		this.deviceStatus = status;
		this.actionType = actionType;
		this.desc = desc;
	}
	
	public ATMLog(Long terminalCode, String desc, ActionType actionType) {
		this.terminalCode = terminalCode;
		this.actionType = actionType;
		this.desc = desc;
	}

	public Long getTerminalCode() {
		return terminalCode;
	}

	public void setTerminalCode(Long terminalCode) {
		this.terminalCode = terminalCode;
	}

	/*public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}*/

	/*public boolean isCardCaptured() {
		return cardCaptured;
	}

	public void setCardCaptured(boolean cardCaptured) {
		this.cardCaptured = cardCaptured;
	}*/

	/*public String getDispensed() {
		return dispensed;
	}

	public void setDispensed(String dispensed) {
		this.dispensed = dispensed;
	}*/

	/*public Integer getRetracted() {
		return retracted;
	}

	public void setRetracted(Integer retracted) {
		this.retracted = retracted;
	}*/

	/*public Boolean getDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}*/

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public String getNotesInCassette() {
		return notesInCassette;
	}

	public void setNotesInCassette(String notesInCassette) {
		this.notesInCassette = notesInCassette;
	}

	public String getNotesRejected() {
		return notesRejected;
	}

	public void setNotesRejected(String notesRejected) {
		this.notesRejected = notesRejected;
	}

	public String getNotesDispensed() {
		return notesDispensed;
	}

	public void setNotesDispensed(String notesDispensed) {
		this.notesDispensed = notesDispensed;
	}

	public String getLastTrxNotesDispensed() {
		return lastTrxNotesDispensed;
	}

	public void setLastTrxNotesDispensed(String lastTrxNotesDispensed) {
		this.lastTrxNotesDispensed = lastTrxNotesDispensed;
	}

	public Integer getCardCapturedNo() {
		return cardCapturedNo;
	}

	public void setCardCapturedNo(Integer cardCapturedNo) {
		this.cardCapturedNo = cardCapturedNo;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public final static class LogState {
		public final static String LAST_STATE = "LAST_STATE";
		public final static String NEXT_STATE = "NEXT_STATE";
	}

	public String getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(String deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
}
