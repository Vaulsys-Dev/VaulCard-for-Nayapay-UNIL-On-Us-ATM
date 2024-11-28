package vaulsys.clearing.jobs;

import vaulsys.terminal.TerminalClearingMode;


public class ISOIssuerReconcilementResponseJob extends ISOReconcilementResponseJob {

	public static final ISOIssuerReconcilementResponseJob Instance = new ISOIssuerReconcilementResponseJob();
	protected ISOIssuerReconcilementResponseJob(){}
	
    protected TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.ISSUER;
    }

    public ClearingJob postJob() throws Exception {
//        return ISOIssuerFinalizeReconcilementJob.class.newInstance();
    	return null;
    }
}