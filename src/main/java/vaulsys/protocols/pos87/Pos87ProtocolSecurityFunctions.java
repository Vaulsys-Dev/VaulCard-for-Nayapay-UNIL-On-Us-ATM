package vaulsys.protocols.pos87;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.message.Message;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.POSTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public class Pos87ProtocolSecurityFunctions extends ISOSecurityFunctions {

	transient Logger logger = Logger.getLogger(Pos87ProtocolSecurityFunctions.class);

	@Override
	public void verifyMac(Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, String mac,
			byte[] binariData, boolean enable) throws MacFailException {

		try {
			if (keySet == null || keySet.isEmpty()) {
				POSTerminalService.addDefaultKeySetForTerminal((POSTerminal) terminal);
				keySet = terminal.getKeySet();
			}

			super.verifyMac(terminal, securityProfileId, keySet, mac, binariData, enable);
		} catch (Exception e) {
			TerminalService.removeKeySet(terminal);
			try {
				POSTerminalService.addDefaultKeySetForTerminal((POSTerminal) terminal);
				keySet = terminal.getKeySet();
				super.verifyMac(terminal, securityProfileId, keySet, mac, binariData, enable);
			} catch (Exception e1) {
				if (e1 instanceof MacFailException)
					throw (MacFailException)e1;
				
				throw new MacFailException(e1);
			}
			
//			if (e instanceof MacFailException)
//				throw (MacFailException)e;
//			
//			throw new MacFailException(e);
		}
	}

	@Override
	public void setMac(ProcessContext processContext, Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, Message message,
			Boolean enabled) throws Exception {

		if (keySet == null || keySet.isEmpty()) {
			if(!(terminal instanceof POSTerminal))
				terminal = TerminalService.findTerminal(POSTerminal.class, terminal.getCode());
			
			POSTerminalService.addDefaultKeySetForTerminal((POSTerminal) terminal);
			keySet = terminal.getKeySet();
		}
		super.setMac(processContext, terminal, securityProfileId, keySet, message, enabled);
	}

}
