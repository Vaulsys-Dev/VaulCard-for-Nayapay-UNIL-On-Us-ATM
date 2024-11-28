package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.protocols.ifx.imp.Ifx;

import org.apache.log4j.Logger;

public class GeneralTerminalTypeProcessor extends TerminalTypeProcessor {	
	transient Logger logger = Logger.getLogger(GeneralTerminalTypeProcessor.class);

	
	public static final GeneralTerminalTypeProcessor Instance = new GeneralTerminalTypeProcessor();
	private GeneralTerminalTypeProcessor(){};

	
	@Override
	public void messageValidation(Ifx ifx, Long messageId) throws Exception {
		
	} 
}
