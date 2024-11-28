package vaulsys.clearing.jobs.negin;

import vaulsys.clearing.jobs.ISOIssuerReconcilementResponseJob;

public class NeginIssuerReconcilementResponseJob extends ISOIssuerReconcilementResponseJob {

	public static final NeginIssuerReconcilementResponseJob Instance = new NeginIssuerReconcilementResponseJob();
	private NeginIssuerReconcilementResponseJob(){}
	
//	@Override
//	public IReconcilement getReconcilement() {
//		return (IReconcilement) SwitchApplication.get().getBean("neginReconcilement");
//	}
	
}
