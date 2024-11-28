package vaulsys.exception.impl;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public abstract class DecisionMakerExceptionImp extends Exception implements DecisionMakerException {
	transient Logger logger = Logger.getLogger(this.getClass());
	
	public DecisionMakerExceptionImp() {
		super();
	}

	public DecisionMakerExceptionImp(String message, Throwable cause) {
		super(message, cause);
	}

	public DecisionMakerExceptionImp(String message) {
		super(message);
	}

	public DecisionMakerExceptionImp(Throwable cause) {
		super(cause);
	}

	@Override
	public void showCause(Ifx ifx) {
		if(ifx != null) {
			ifx.setSeverity(Severity.ERROR);
			
			String strErr = this.getClass().getSimpleName() + ": " + getMessage();
			if (Util.hasText(ifx.getStatusDesc()))
				strErr = ifx.getStatusDesc()+";\r\n"+ strErr;
			ifx.setStatusDesc(strErr);
			
			Throwable cause2 = getCause();
			if (cause2!= null && cause2.getStackTrace()!= null){
				strErr ="";
				for (StackTraceElement s: cause2.getStackTrace()){
					if (s.toString().startsWith("vaulsys")){
						strErr += ";\r\n"+s.toString();
					}
				}
				ifx.setStatusDesc(ifx.getStatusDesc()+strErr);
			}
		}
	}

	@Override
	public void rollBack(Ifx ifx, Transaction transaction, Terminal terminal) {
	}
}
