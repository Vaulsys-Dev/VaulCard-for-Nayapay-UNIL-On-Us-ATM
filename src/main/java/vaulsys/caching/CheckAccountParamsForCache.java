package vaulsys.caching;

import vaulsys.protocols.ifx.enums.TrnType;

public class CheckAccountParamsForCache {
	
	private String appPAN;
	
	private String terminalId;
	
	private Integer terminalType;
	
	private Long receivedDt;
	
	private Long bankId;
	
	private String networkRefId;
	
	private Long endPointTerminalCode;
	
	public CheckAccountParamsForCache() {
		super();
		this.terminalId = "";
		this.networkRefId = "";
	}
	
	

	public CheckAccountParamsForCache(String appPAN, TrnType trnType, String terminalId, Integer terminalType, Long receivedDt, Long bankId, String networkRefId, Long endPointTerminalCode) {
		
		super();
		
		this.appPAN = appPAN;
		
		this.terminalId = terminalId;
		
		this.terminalType = terminalType;
		
		this.receivedDt = receivedDt;
		
		this.bankId = bankId;
		
		this.networkRefId = networkRefId;
		
		this.endPointTerminalCode = endPointTerminalCode;
	}


	public String getAppPAN() {
		return appPAN;
	}

	public void setAppPAN(String appPAN) {
		this.appPAN = appPAN;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public Integer getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(Integer terminalType) {
		this.terminalType = terminalType;
	}

	public Long getReceivedDt() {
		return receivedDt;
	}

	public void setReceivedDt(Long receivedDt) {
		this.receivedDt = receivedDt;
	}

	public Long getBankId() {
		return bankId;
	}

	public void setBankId(Long bankId) {
		this.bankId = bankId;
	}

	public String getNetworkRefId() {
		return networkRefId;
	}

	public void setNetworkRefId(String networkRefId) {
		this.networkRefId = networkRefId;
	}
	
	public Long getEndPointTerminalCode() {
		return endPointTerminalCode;
	}

	public void setEndPointTerminalCode(Long endPointTerminalCode) {
		this.endPointTerminalCode = endPointTerminalCode;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appPAN == null) ? 0 : appPAN.hashCode());
		result = prime * result + ((bankId == null) ? 0 : bankId.hashCode());
		result = prime * result + ((networkRefId == null) ? 0 : networkRefId.hashCode());
//		result = prime * result + ((receivedDt == null) ? 0 : receivedDt.hashCode());
		result = prime * result + ((terminalId == null) ? 0 : terminalId.hashCode());
		result = prime * result + ((terminalType == null) ? 0 : terminalType.hashCode());
		result = prime * result + ((endPointTerminalCode == null) ? 0 : endPointTerminalCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CheckAccountParamsForCache other = (CheckAccountParamsForCache) obj;
		
		if (appPAN == null) {
			if (other.appPAN != null)
				return false;
		} else if (!appPAN.equals(other.appPAN))
			return false;
		
		if (bankId == null) {
			if (other.bankId != null)
				return false;
		} else if (!bankId.equals(other.bankId))
			return false;
		
		if (networkRefId == null) {
			if (other.networkRefId != null)
				return false;
		} else if (!networkRefId.equals(other.networkRefId))
			return false;
		
		/********* comment: because it is not clear! **********/
		/*if (receivedDt == null) {
			if (other.receivedDt != null)
				return false;
		} else if (!receivedDt.equals(other.receivedDt))
			return false;*/
		
		if (terminalId == null) {
			if (other.terminalId != null)
				return false;
		} else if (!terminalId.equals(other.terminalId))
			return false;
		
		if (terminalType == null) {
			if (other.terminalType != null)
				return false;
		} else if (!terminalType.equals(other.terminalType))
			return false;
		
		if (endPointTerminalCode == null) {
			if (other.endPointTerminalCode != null)
				return false;
		} else if (!endPointTerminalCode.equals(other.endPointTerminalCode))
			return false;
		
		
		
		return true;
	}

}
