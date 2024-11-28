package vaulsys.protocols.apacs70;

import vaulsys.protocols.apacs70.base.ApacsConstants;

import org.apache.log4j.Logger;

public class ApacsByteArrayReader {
	private static final Logger logger = Logger.getLogger(ApacsByteArrayReader.class);

	private int idx, end;
	private byte[] buffer;

	private ApacsByteArrayReader(){}

	public ApacsByteArrayReader(byte[] buffer) {
		this.buffer = buffer;
		idx = 0;
		end = buffer.length; // the last index, inclusive 
	}

	public int getRemainSize() {
		return end - idx;
	}

	// ************************
	//  Fixed-Length Retrieval
	// ************************

	public String getStringFixed(String fieldName, int len) {
		if(idx + len > end) {
			//throw new ArrayIndexOutOfBoundsException("Wanted Index: " + (idx + len) + ", Last Index: " + end);
			logger.error(String.format("Field(%s): Wanted Index [%s], Last Index [%s]", fieldName, idx + len, end));
			return null;
		}
		String string = new String(buffer, idx, len);
		idx += len;
		return string;
	}

	public Integer getIntegerFixed(String fieldName, int len) {
		String str = getStringFixed(fieldName, len);
		if(str != null) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Integer!", fieldName, str));
			}
		}
		return null;
	}

	public Integer getIntegerFixed(String fieldName, int len, int radix) {
		String str = getStringFixed(fieldName, len);
		if(str != null) {
			try {
				return Integer.parseInt(str, radix);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Integer!", fieldName, str));
			}
		}
		return null;
	}

	public Long getLongFixed(String fieldName, int len) {
		String str = getStringFixed(fieldName, len);
		if(str != null) {
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Long!", fieldName, str));
			}
		}
		return null;
	}

	public void skipFixed(int len) {
		if(idx + len > end)
			logger.error(String.format("Try to skip fixed length, Wanted Index [%s], Last Index [%s]", idx + len, end));
		else
			idx += len;
	}

	// ************************
	//  Fixed-Length Retrieval
	// ************************

	public String getStringMax(String fieldName, int maxLen) {
		int remain = getRemainSize();
		if(remain > maxLen) {
			logger.error(String.format("Field(%s): Wanted MaxLen [%s] is less than remaining [%s]", fieldName, maxLen, remain));
			return null;
		}
		return getStringFixed(fieldName, remain);
	}

	public Integer getIntegerMax(String fieldName, int maxLen) {
		String str = getStringMax(fieldName, maxLen);
		if(str != null) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Integer!", fieldName, str));
			}
		}
		return null;
	}

	// *************************************
	//  Fixed-Length-to-Separator Retrieval
	// *************************************

	public String getStringFixedToSep(String fieldName, int len, ApacsConstants sep) {
		int readLen = 0;
		for(; buffer[idx+readLen]!=sep.getByte() && (idx+readLen) < end; readLen++);
		if(idx + readLen > end) {
			logger.error(String.format("Field(%s): Wanted Index [%s], Last Index [%s]", fieldName, idx + readLen, end));
			return null;
		}
		if(readLen != len) {
			logger.error(String.format("Field(%s): Read Length [%s], Expecting Length [%s]", fieldName, readLen, len));
			return null;
		}
		String string = new String(buffer, idx, readLen);
		idx += readLen;
		if(buffer[idx]==sep.getByte())
			idx++;
		return string;
	}

	public Integer getIntegerFixedToSep(String fieldName, int len, ApacsConstants sep) {
		String str = getStringFixedToSep(fieldName, len, sep);
		if(str != null) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Integer!", fieldName, str));
			}
		}
		return null;
	}

	public Long getLongFixedToSep(String fieldName, int len, ApacsConstants sep) {
		String str = getStringFixedToSep(fieldName, len, sep);
		if(str != null) {
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Long!", fieldName, str));
			}
		}
		return null;
	}

	// ***************************
	//  Variable-Length-to-Separator Retrieval
	// ***************************

	public ApacsByteArrayReader getBytesMaxToSep(String fieldName, int maxLen, ApacsConstants sep) {
		int len = 0;
		for(; buffer[idx+len]!=sep.getByte() && (idx+len) < end; len++);
		if(idx + len > end) {
			logger.error(String.format("Compound Field(%s): Wanted Index [%s], Last Index [%s]", fieldName, idx + len, end));
			return new ApacsByteArrayReader();
		}
		ApacsByteArrayReader res = new ApacsByteArrayReader();
		res.idx = idx;
		res.end = idx + len;
		res.buffer = buffer;
		idx += len;
		if(buffer[idx]==sep.getByte())
			idx++;
		return res;
	}

	public String getStringMaxToSep(String fieldName, int maxLen, ApacsConstants sep) {
		int len = 0;
		for(; buffer[idx+len]!=sep.getByte() && (idx+len) < end; len++);
		if(len > maxLen) {
			logger.error(String.format("Field(%s): Read Length [%s], Expeting Length [%s]", fieldName, len, maxLen));
			return null;
		}
		if(idx + len > end) {
			logger.error(String.format("Field(%s): Wanted Index [%s], Last Index [%s]", fieldName, idx + len, end));
			return null;
		}
		String string = new String(buffer, idx, len);
		idx += len;
		if(buffer[idx]==sep.getByte())
			idx++;
		return string;
	}

	public Integer getIntegerMaxToSep(String fieldName, int maxLen, ApacsConstants sep) {
		String s = getStringMaxToSep(fieldName, maxLen, sep);
		if(s != null) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Integer!", fieldName, s));
			}
		}
		return null;
	}

	public Long getLongMaxToSep(String fieldName, int maxLen, ApacsConstants sep) {
		String s = getStringMaxToSep(fieldName, maxLen, sep);
		if(s != null) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				logger.warn(String.format("Field(%s): [%s] is not Long!", fieldName, s));
			}
		}
		return null;
	}

	public void skipToSep(ApacsConstants sep) {
		for(; buffer[idx] != sep.getByte() && idx <= end; idx++);
		idx ++; //for sep
	}

	public void skipToSep(String fieldName, int checkMaxLen, ApacsConstants sep) {
		int len = 0;
		for(; buffer[idx+len]!=sep.getByte() && (idx+len) < end; len++);
		if(len > checkMaxLen)
			logger.error(String.format("Field(%s): Read Length [%s], Expecting Length [%s]", fieldName, len, checkMaxLen));
		else if(idx + len > end)
			logger.error(String.format("Field(%s): Wanted Index [%s], Last Index [%s]", fieldName, idx + len, end));
		else
			idx += len + 1; // 1 for sep
	}
}
