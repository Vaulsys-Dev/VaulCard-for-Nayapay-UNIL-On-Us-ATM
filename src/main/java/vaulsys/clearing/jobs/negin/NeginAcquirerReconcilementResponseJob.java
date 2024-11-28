package vaulsys.clearing.jobs.negin;

import vaulsys.clearing.jobs.ISOAcquirerReconcilementResponseJob;

public class NeginAcquirerReconcilementResponseJob extends ISOAcquirerReconcilementResponseJob{

	public static final NeginAcquirerReconcilementResponseJob Instance = new NeginAcquirerReconcilementResponseJob();
	private NeginAcquirerReconcilementResponseJob(){}
	
//	@Override
//	public IReconcilement getReconcilement() {
//		return (IReconcilement) SwitchApplication.get().getBean("neginReconcilement");
//	}
	
}
