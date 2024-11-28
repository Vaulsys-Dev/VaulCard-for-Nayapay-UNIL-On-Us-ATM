package vaulsys.protocols.cms;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.MonthDayDate;
import vaulsys.calendar.YearMonthDate;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.utils.CMSMapperUtil;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class CMSHttpMessage implements ProtocolMessage {
	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Boolean isRequest() throws Exception {
		String mainMTI = getMap().get(IfxStatics.IFX_IFX_TYPE);
		if(mainMTI == null || mainMTI.trim().equals("")) {
			logger.error("Message MTI is null!");
			throw new NotParsedBinaryToProtocolException();
//			return false;
		}
		String mti = mainMTI.trim();
		
		IfxType ifxType = CMSMapperUtil.ToIfxType.get(Integer.valueOf(mti));

		return ISOFinalMessageType.isRequestMessage(ifxType);
	}

	private List<String> header;

	private ConcurrentHashMap<String, String> map;

	public ConcurrentHashMap<String, String> getMap() {
		if (map == null)
			map = new ConcurrentHashMap<String, String>();
		return map;
	}

	public void setMap(ConcurrentHashMap<String, String> map) {
		this.map = map;
	}

	public List<String> getHeader() {
		if (header == null)
			header = new ArrayList<String>();
		return header;
	}

	public void setHeader(List<String> header) {
		this.header = header;
	}

	public void set(String parameterName, Object value) {
		if (value == null)
			return;

		if (value instanceof Date) {
			set(parameterName, (Date) value);
		} else if (value instanceof String) {
			if (!((String) value).isEmpty())
				getMap().put(parameterName, (String) value);
		} else if (value instanceof MonthDayDate) {
			set(parameterName, ((MonthDayDate) value).toDate());
		} else if (value instanceof YearMonthDate) {
			set(parameterName, ((YearMonthDate) value).toDate());
		} else if (value instanceof DateTime) {
			set(parameterName, ((DateTime) value).toDate());
		} else if (value instanceof DayDate) {
			set(parameterName, ((DayDate) value).toDate());
		} else {
			getMap().put(parameterName, value.toString());
		}
	}

	private void set(String parameterName, Date value) {
		getMap().put(parameterName, String.valueOf(value.getTime()));
	}

	public void addToHeader(String line) {
		getHeader().add(line + "\r\n");
	}

	public void unpack(byte[] binaryData) {
		String respose = new String(binaryData);
		String[] lines = respose.split("\r\n");
		for (String line : lines) {
			String[] params = line.split(": ");
			if (params.length == 2)
				set(params[0].trim(), params[1]);

		}
	}

	public byte[] pack() {
//		String strContent = "";
		StringBuilder content = new StringBuilder();
		if (map != null) {
			for (String param : map.keySet()) {
				content.append(param + "=" + map.get(param) + "&");
			}
//			strContent = content.substring(0, content.length() - 1);
			return content.substring(0, content.length() - 1).getBytes();
		}
		return null;
	}

	@Override
	public String toString() {
		if (!getMap().isEmpty() && getMap().keys() != null){
//			String str = "";
			StringBuilder str = new StringBuilder();
			for (String s : getMap().keySet()) {
				str.append(s +" = "+getMap().get(s)+";\n");
			}
			return str.toString();
		}else{
			return null;
		}
	}

}
