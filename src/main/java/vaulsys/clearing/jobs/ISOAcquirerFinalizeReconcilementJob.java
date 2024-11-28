package vaulsys.clearing.jobs;

import vaulsys.terminal.TerminalClearingMode;

public class ISOAcquirerFinalizeReconcilementJob extends ISOFinalizeReconcilementJob {

	public static final ISOAcquirerFinalizeReconcilementJob Instance = new ISOAcquirerFinalizeReconcilementJob();
	private ISOAcquirerFinalizeReconcilementJob(){}
	
    protected TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.ACQUIER;
    }
    
    @Override
    public ClearingJob postJob() throws Exception {
    	return ISOReconcilementRequstJob.class.newInstance();
    }
}