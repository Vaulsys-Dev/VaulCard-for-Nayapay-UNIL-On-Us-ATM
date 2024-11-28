package vaulsys.protocols.apacs70.base;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

public abstract class RqBaseMsg extends BaseMsg {
	public Integer confirmationCode;

	protected abstract void unpack(ApacsByteArrayReader in);
	
	public void toIfx(Ifx ifx) {
		ifx.setIfxDirection(IfxDirection.INCOMING);
		ifx.setRequest(true);
		ifx.setDialIndicator(dialIndicator);
		if(terminalIdentity != null)
			ifx.setTerminalId(terminalIdentity.toString());

		String msgNo = null;
		if(messageNumber != null)
			msgNo = messageNumber.toString();
		ifx.setSrc_TrnSeqCntr(msgNo);
		ifx.setMy_TrnSeqCntr(msgNo);
		ifx.setNetworkRefId(msgNo);

		ifx.setTrnType(ApacsMsgType.toTrnType(messageType));
		ifx.setIfxType(ApacsMsgType.toIfxType(messageType));

//		ifx.setBankId(GlobalContext.getInstance().getMyInstitution().getBin());
		ifx.setBankId(ProcessContext.get().getMyInstitution().getBin().toString());
        //ifx.setReceivedDt() TODO: always set from Message.startDateTime
		ifx.setTrnDt(DateTime.now());
		ifx.setMsgAuthCode(MAC);
		ifx.setConfirmationCode(confirmationCode);
	}

	@Override
	public final Boolean isRequest() throws Exception {
		return true;
	}

	// A Factory Method
	public static RqBaseMsg createRqMsg(byte[] data) {
		ApacsByteArrayReader in = new ApacsByteArrayReader(data);

		Integer dialIndicator = in.getIntegerFixed("dialIndicator", 1);
		Long terminalIdentity = in.getLongFixed("terminalIdentity", 8);
		Integer messageNumber = in.getIntegerFixed("messageNumber", 4);
		in.skipFixed(4); // terminal Type, must be "1111"
		String messageType = in.getStringFixed("messageType", 2);
		String type = ApacsMsgType.getRqType(messageType);

		RqBaseMsg rqMsg = null;
		if (ApacsMsgType.FIN_REQ.equals(type))
			rqMsg = new RqFinMsg();

		else if(ApacsMsgType.NET_REQ.equals(type))
			rqMsg = new RqNetMsg();

		else if (ApacsMsgType.RECON_REQ.equals(type))
			rqMsg = new RqReconMsg();

		else if (ApacsMsgType.CONF.equals(type)) 
			rqMsg = new RqConfMsg();
		
		//ghasedak
		else if(ApacsMsgType.INFO_REQ.equals(type))
			rqMsg = new RqInfoMsg();
		
		if (rqMsg != null) {
			rqMsg.dialIndicator = dialIndicator;
			rqMsg.terminalIdentity = terminalIdentity;
			rqMsg.messageNumber = messageNumber;
			rqMsg.messageType = messageType;
			rqMsg.unpack(in);
			if(in.getRemainSize() >= 8)
				rqMsg.MAC = in.getStringFixed("MAC", 8);
		}

		return rqMsg;
	}
}
