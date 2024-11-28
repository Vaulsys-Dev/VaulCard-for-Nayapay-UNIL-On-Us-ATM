package vaulsys.terminal.atm;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "OARResponse")
public class OARResponse extends ATMResponse {

	@Embedded
	@AttributeOverride(name = "flag", column = @Column(name = "display_flag"))
	private ATMDisplayFlag displayFlag;

	@Column(name = "all_numeric_keys")
	private boolean allNumericKeys;
	private boolean opKeyA;
	private boolean opKeyB;
	private boolean opKeyC;
	private boolean opKeyD;
	private boolean cancelKey;
	private boolean opKeyF;
	private boolean opKeyG;
	private boolean opKeyH;
	private boolean opKeyI;

	@Column(name = "screen_timer")
	private String screenTimer;

	/*
	 * @Column(name = "screen_data") private String screenData;
	 */
	
	@Column(name = "cancel_state")
	private String cancelState;

	@Column(name = "cancel_screen")
	private String cancelScreen;

	@Column(name = "cancel_screen_data")
	private String cancelScreenData;

	@Column(name = "timeout_state")
	private String timeoutState;

	@Column(name = "timeout_screen")
	private String timeoutScreen;

	@Column(name = "timeout_screen_data")
	private String timeoutScreenData;

	public OARResponse() {
	}

	public OARResponse(boolean allNumericKeys, boolean opKeyA, boolean opKeyB, boolean opKeyC, boolean opKeyD,
			boolean cancelKey, boolean opKeyF, boolean opKeyG, boolean opKeyH, boolean opKeyI) {
		this.allNumericKeys = allNumericKeys;
		this.opKeyA = opKeyA;
		this.opKeyB = opKeyB;
		this.opKeyC = opKeyC;
		this.opKeyD = opKeyD;
		this.cancelKey = cancelKey;
		this.opKeyF = opKeyF;
		this.opKeyG = opKeyG;
		this.opKeyH = opKeyH;
		this.opKeyI = opKeyI;
	}

	public OARResponse(ATMDisplayFlag displayFlag, boolean allNumericKeys, boolean opKeyA, boolean opKeyB,
			boolean opKeyC, boolean opKeyD, boolean cancelKey, boolean opKeyF, boolean opKeyG, boolean opKeyH,
			boolean opKeyI) {
		this.displayFlag = displayFlag;
		this.allNumericKeys = allNumericKeys;
		this.opKeyA = opKeyA;
		this.opKeyB = opKeyB;
		this.opKeyC = opKeyC;
		this.opKeyD = opKeyD;
		this.cancelKey = cancelKey;
		this.opKeyF = opKeyF;
		this.opKeyG = opKeyG;
		this.opKeyH = opKeyH;
		this.opKeyI = opKeyI;
	}

	public OARResponse(ATMDisplayFlag displayFlag, boolean allNumericKeys, boolean opKeyA, boolean opKeyB,
			boolean opKeyC, boolean opKeyD, boolean cancelKey, boolean opKeyF, boolean opKeyG, boolean opKeyH,
			boolean opKeyI, String screenTimer/*, String screenData*/) {
		this.displayFlag = displayFlag;
		this.allNumericKeys = allNumericKeys;
		this.opKeyA = opKeyA;
		this.opKeyB = opKeyB;
		this.opKeyC = opKeyC;
		this.opKeyD = opKeyD;
		this.cancelKey = cancelKey;
		this.opKeyF = opKeyF;
		this.opKeyG = opKeyG;
		this.opKeyH = opKeyH;
		this.opKeyI = opKeyI;
		this.screenTimer = screenTimer;
//		this.screenData = screenData;
	}

	public ATMDisplayFlag getDisplayFlag() {
		return displayFlag;
	}

	public void setDisplayFlag(ATMDisplayFlag displayFlag) {
		this.displayFlag = displayFlag;
	}

	public boolean isAllNumericKeys() {
		return allNumericKeys;
	}

	public void setAllNumericKeys(boolean allNumericKeys) {
		this.allNumericKeys = allNumericKeys;
	}

	public boolean isOpKeyA() {
		return opKeyA;
	}

	public void setOpKeyA(boolean opKeyA) {
		this.opKeyA = opKeyA;
	}

	public boolean isOpKeyB() {
		return opKeyB;
	}

	public void setOpKeyB(boolean opKeyB) {
		this.opKeyB = opKeyB;
	}

	public boolean isOpKeyC() {
		return opKeyC;
	}

	public void setOpKeyC(boolean opKeyC) {
		this.opKeyC = opKeyC;
	}

	public boolean isOpKeyD() {
		return opKeyD;
	}

	public void setOpKeyD(boolean opKeyD) {
		this.opKeyD = opKeyD;
	}

	public boolean isCancelKey() {
		return cancelKey;
	}

	public void setCancelKey(boolean cancelKey) {
		this.cancelKey = cancelKey;
	}

	public boolean isOpKeyF() {
		return opKeyF;
	}

	public void setOpKeyF(boolean opKeyF) {
		this.opKeyF = opKeyF;
	}

	public boolean isOpKeyG() {
		return opKeyG;
	}

	public void setOpKeyG(boolean opKeyG) {
		this.opKeyG = opKeyG;
	}

	public boolean isOpKeyH() {
		return opKeyH;
	}

	public void setOpKeyH(boolean opKeyH) {
		this.opKeyH = opKeyH;
	}

	public boolean isOpKeyI() {
		return opKeyI;
	}

	public void setOpKeyI(boolean opKeyI) {
		this.opKeyI = opKeyI;
	}

	public String getScreenTimer() {
		return screenTimer;
	}

	public void setScreenTimer(String screenTimer) {
		this.screenTimer = screenTimer;
	}

	public String getCancelState() {
		return cancelState;
	}

	public void setCancelState(String cancelState) {
		this.cancelState = cancelState;
	}

	public String getCancelScreen() {
		return cancelScreen;
	}

	public void setCancelScreen(String cancelScreen) {
		this.cancelScreen = cancelScreen;
	}

	public String getCancelScreenData() {
		return cancelScreenData;
	}

	public void setCancelScreenData(String cancelScreenData) {
		this.cancelScreenData = cancelScreenData;
	}

	public String getTimeoutState() {
		return timeoutState;
	}

	public void setTimeoutState(String timeoutState) {
		this.timeoutState = timeoutState;
	}

	public String getTimeoutScreen() {
		return timeoutScreen;
	}

	public void setTimeoutScreen(String timeoutScreen) {
		this.timeoutScreen = timeoutScreen;
	}

	public String getTimeoutScreenData() {
		return timeoutScreenData;
	}

	public void setTimeoutScreenData(String timeoutScreenData) {
		this.timeoutScreenData = timeoutScreenData;
	}
}
