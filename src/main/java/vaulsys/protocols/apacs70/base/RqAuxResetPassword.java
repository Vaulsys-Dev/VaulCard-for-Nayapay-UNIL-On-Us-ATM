package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;

public class RqAuxResetPassword extends RqAuxBase {
	public String temporaryTerminalPassword;

	@Override
	public void unpack(ApacsByteArrayReader in) {
		temporaryTerminalPassword = in.getStringFixedToSep("temporaryTerminalPassword", 4, ApacsConstants.GS);
		
		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		ifx.setResetingPassword(temporaryTerminalPassword);
	}
	
	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nTemporary Terminal Password: ").append(temporaryTerminalPassword);
	}
}
