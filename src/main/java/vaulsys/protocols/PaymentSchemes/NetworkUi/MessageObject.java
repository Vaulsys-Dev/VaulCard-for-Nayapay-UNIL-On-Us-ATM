package vaulsys.protocols.PaymentSchemes.NetworkUi;

import vaulsys.protocols.base.ProtocolMessage;
import org.quartz.SimpleTrigger;

public class  MessageObject implements ProtocolMessage {
	private static final long serialVersionUID = -4850681616717268812L;

	private String command;

	private String channelId;

	private String walletNo;

	private String amount;

	private String walletFlag;

	private String productId;

	private String pan;

	private String expiry;

	private String serviceCode;

	private String cvv;

	private String stan;

	private String dateTime;

	private String responseCode;

	private Boolean isRequest;

	public MessageObject() {
	}

	@Override
	public Boolean isRequest() throws Exception {
		return isRequest;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Boolean getRequest() {
		return isRequest;
	}

	public void setRequest(Boolean request) {
		isRequest = request;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public String getWalletNo() {
		return walletNo;
	}

	public void setWalletNo(String walletNo) {
		this.walletNo = walletNo;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getWalletFlag() {
		return walletFlag;
	}

	public void setWalletFlag(String walletFlag) {
		this.walletFlag = walletFlag;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}
}
