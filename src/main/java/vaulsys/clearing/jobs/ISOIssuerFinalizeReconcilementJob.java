package vaulsys.clearing.jobs;

import vaulsys.terminal.TerminalClearingMode;

public class ISOIssuerFinalizeReconcilementJob extends ISOFinalizeReconcilementJob {

	public static final ISOIssuerFinalizeReconcilementJob Instance = new ISOIssuerFinalizeReconcilementJob();
	private ISOIssuerFinalizeReconcilementJob(){}
	 
    protected TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.ISSUER;
    }
}