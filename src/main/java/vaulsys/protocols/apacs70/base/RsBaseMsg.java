package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.UpdateStatus;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class RsBaseMsg extends BaseMsg {
	private static final List<IfxType> IFX_TYPE_NO_CONF = Arrays.asList(
			IfxType.ACQUIRER_REC_REPEAT_RS, IfxType.BAL_INQ_RS, IfxType.CHANGE_PIN_BLOCK_RS,
			IfxType.TRANSFER_CHECK_ACCOUNT_RS, IfxType.ONLINE_BILLPAYMENT_TRACKING);
	public String  acquirerResponseCode;
	public Integer confirmationRequest;

	public void fromIfx(Ifx ifx) {
		dialIndicator = ifx.getDialIndicator();
		terminalIdentity = Long.parseLong(ifx.getTerminalId());
		messageNumber = Integer.parseInt(ifx.getSrc_TrnSeqCntr());

		messageType = ApacsMsgType.toApacsType(ifx.getIfxType(), ifx.getTrnType());
		acquirerResponseCode = Apacs70Utils.mapError(ifx.getRsCode());

		confirmationRequest = 0;
		IfxType type = ifx.getIfxType();
		String apacsType = ApacsMsgType.getRsType(messageType);
		if(ifx.getRsCode().equals("00") && !ApacsMsgType.NET_RES.equals(apacsType) &&
				!ApacsMsgType.RECON_RES.equals(apacsType) && !IFX_TYPE_NO_CONF.contains(type))
			confirmationRequest |= 1; // Immediate confirmation required

		Terminal terminal = ProcessContext.get().getOriginatorTerminal();
		if (terminal != null) {
			UpdateStatus updateStatus = null;
			if (TerminalType.POS.equals(ifx.getTerminalType()))
				updateStatus = ((POSTerminal) terminal).getUpdateStatus();
			else if (TerminalType.PINPAD.equals(ifx.getTerminalType()))
				updateStatus = ((PINPADTerminal) terminal).getUpdateStatus();

			if (UpdateStatus.NEED_UPDATE.equals(updateStatus))
				confirmationRequest |= 4; // 4=Call TMS after current transaction is complete
		}
		MAC = "01020304";
	}

	public void pack(ApacsByteArrayWriter out) throws IOException {
		out.writePadded(dialIndicator, 1, false);
		out.writePadded(terminalIdentity, 8, false);
		out.writePadded(messageNumber, 4, false);
		out.writePadded(messageType, 2, false);
		out.writePadded(acquirerResponseCode, 2, false);
		out.writePadded(confirmationRequest, 1, false);
	}

	public static RsBaseMsg createRs(Ifx ifx) {
		RsBaseMsg rsMsg = null;
		String apacsCode = ApacsMsgType.toApacsType(ifx.getIfxType(), ifx.getTrnType());
		String type = ApacsMsgType.getRsType(apacsCode);
		//ghasedak
		if(ApacsMsgType.FIN_RES.equals(type) || ApacsMsgType.NET_RES.equals(type) || ApacsMsgType.INFO_RES.equals(type))
			rsMsg = new RsFinNetMsg();
		else if(ApacsMsgType.RECON_RES.equals(type))
			rsMsg = new RsReconMsg();

		if(rsMsg != null)
			rsMsg.fromIfx(ifx);

		return rsMsg;
	}

	@Override
	protected void msgString(StringBuilder builder) {
		builder.append("\nRsCode: ").append(acquirerResponseCode);
		builder.append("\nConfirmation Request: ").append(confirmationRequest);
	}

	@Override
	public final Boolean isRequest() throws Exception {
		return false;
	}
}
