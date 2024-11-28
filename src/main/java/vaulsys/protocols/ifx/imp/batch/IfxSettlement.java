package vaulsys.protocols.ifx.imp.batch;

import vaulsys.protocols.ifx.enums.IfxType;

public class IfxSettlement {
	private Long id;
    private IfxType ifxType; // Note e.g: BalInqRq; BalInqRs
	private Long Real_Amt;
    private Long transactionId;
	private String Sec_CurRate;
	private Long Sec_Amt;
    private Long endPointTerminalCode;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public IfxType getIfxType() {
		return ifxType;
	}
	public void setIfxType(IfxType ifxType) {
		this.ifxType = ifxType;
	}
	public Long getReal_Amt() {
		return Real_Amt;
	}
	public void setReal_Amt(Long real_Amt) {
		Real_Amt = real_Amt;
	}
	public Long getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
	public String getSec_CurRate() {
		return Sec_CurRate;
	}
	public void setSec_CurRate(String sec_CurRate) {
		Sec_CurRate = sec_CurRate;
	}
	public Long getSec_Amt() {
		return Sec_Amt;
	}
	public void setSec_Amt(Long sec_Amt) {
		Sec_Amt = sec_Amt;
	}
	public Long getEndPointTerminalCode() {
		return endPointTerminalCode;
	}
	public void setEndPointTerminalCode(Long endPointTerminalCode) {
		this.endPointTerminalCode = endPointTerminalCode;
	}
}
