package vaulsys.protocols.apacs70.base;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.util.MyDateFormatNew;

import java.text.ParseException;

import org.apache.log4j.Logger;

public final class RqConfMsg extends RqBaseMsg {
	private final static Logger logger = Logger.getLogger(RqConfMsg.class);

	public String dateAndTime;

	@Override
	protected void unpack(ApacsByteArrayReader in) {
		confirmationCode = in.getIntegerFixed("confirmationCode", 1, 16);
		in.getIntegerFixed("balance code", 1); // balance code, 0x31 fixed
		dateAndTime = in.getStringFixed("dateAndTime", 12); //YYMMDDhhmmss
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		if(ApacsMsgType.CONF_OK.equals(confirmationCode)){
			ifx.setIfxType(IfxType.POS_CONFIRMATION);
			String msgNo = null;
			if(messageNumber != null)
				msgNo = messageNumber.toString();
			ifx.setLast_TrnSeqCntr(msgNo);
		}else{
			ifx.setIfxType(IfxType.POS_FAILURE);
		

			String msgNo = null;
			if(messageNumber != null)
				msgNo = messageNumber.toString();
			MessageReferenceData ode = ifx.getSafeOriginalDataElements();
			ode.setNetworkTrnInfo(msgNo);
			ode.setTrnSeqCounter(msgNo);
			if(terminalIdentity != null)
				ode.setTerminalId(terminalIdentity.toString());
			try {
				ode.setOrigDt(new DateTime(MyDateFormatNew.parse("yyMMddHHmmss", dateAndTime)));
			} catch (ParseException e) {
				logger.error("", e);
			}
			ifx.setLast_TrnSeqCntr(msgNo);
		}
	}

	@Override
	protected void msgString(StringBuilder builder) {
		builder.append("\r\nConfirmation Code: ").append(confirmationCode);
		builder.append("\r\nDateTime: ").append(dateAndTime);
	}
}
