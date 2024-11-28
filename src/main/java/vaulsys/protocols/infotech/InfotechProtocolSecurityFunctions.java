package vaulsys.protocols.infotech;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.message.Message;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.POSTerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public class InfotechProtocolSecurityFunctions extends ISOSecurityFunctions {

	transient Logger logger = Logger.getLogger(InfotechProtocolSecurityFunctions.class);

	@Override
	public void verifyMac(Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, String mac,
			byte[] binariData, boolean enable) throws MacFailException {

		try {
			if (keySet == null || keySet.isEmpty()) {
				POSTerminalService.addDefaultKeySetForTerminal(terminal);
				keySet = terminal.getKeySet();
			}
		} catch (Exception e) {
			throw new MacFailException(e);
		}
		super.verifyMac(terminal, securityProfileId, keySet, mac, binariData, enable);
	}

	@Override
	public void setMac(ProcessContext processContext, Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, Message message,
			Boolean enabled) throws Exception {

		if (keySet == null || keySet.isEmpty()) {
			POSTerminalService.addDefaultKeySetForTerminal(terminal);
			keySet = terminal.getKeySet();
		}
		super.setMac(processContext, terminal, securityProfileId, keySet, message, enabled);
	}
}
