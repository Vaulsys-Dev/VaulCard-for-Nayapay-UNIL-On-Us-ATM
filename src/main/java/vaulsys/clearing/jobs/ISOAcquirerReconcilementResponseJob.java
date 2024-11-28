package vaulsys.clearing.jobs;

import vaulsys.terminal.TerminalClearingMode;

public class ISOAcquirerReconcilementResponseJob extends ISOReconcilementResponseJob {

	public static final ISOAcquirerReconcilementResponseJob Instance = new ISOAcquirerReconcilementResponseJob();
	protected ISOAcquirerReconcilementResponseJob(){}
	
    protected TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.ACQUIER;
    }

    public ClearingJob postJob() throws Exception {
//        return ISOAcquirerFinalizeReconcilementJob.class.newInstance();
    	return null;
    }

}
