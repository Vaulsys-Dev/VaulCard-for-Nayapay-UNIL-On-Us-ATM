package vaulsys.util;

import vaulsys.clearing.base.ClearingAction;
import vaulsys.clearing.jobs.ClearingActionJobsImpl;
import vaulsys.clearing.jobs.ClearingJob;
import vaulsys.clearing.jobs.ISOAcquirerFinalizeReconcilementJob;
import vaulsys.clearing.jobs.ISOAcquirerReconcilementResponseJob;
import vaulsys.clearing.jobs.ISOClearingActionMapper;
import vaulsys.clearing.jobs.ISOCutoverResponseJob;
import vaulsys.clearing.jobs.ISOIssuerFinalizeReconcilementJob;
import vaulsys.clearing.jobs.ISOIssuerReconcilementResponseJob;
import vaulsys.clearing.jobs.ISOReconcilementRequstJob;
import vaulsys.clearing.jobs.POSAcquirerReconcilementResponseJob;
import vaulsys.clearing.jobs.negin.NeginAcquirerReconcilementResponseJob;
import vaulsys.clearing.jobs.negin.NeginIssuerReconcilementResponseJob;

import java.util.HashMap;

public class SwitchContext {
	private ClearingActionJobsImpl clearingActionJobsImpl;
	private ClearingActionJobsImpl neginClearingActionJobs;
	private ClearingActionJobsImpl posClearingActionJobs;
	
	public ClearingActionJobsImpl getClearingActionJobsImpl() {
		if (clearingActionJobsImpl == null){
			clearingActionJobsImpl = new ClearingActionJobsImpl();
			clearingActionJobsImpl.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{ 
				put(ClearingAction.COUTOVER_RESPONSE, ISOCutoverResponseJob.Instance);
				 put(ClearingAction.RECONCILEMNET_REQUEST, ISOReconcilementRequstJob.Instance);
	                put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, ISOAcquirerReconcilementResponseJob.Instance);
	                put(ClearingAction.ISSUER_RECONCILEMNET_RESPONSE, ISOIssuerReconcilementResponseJob.Instance);
	                put(ClearingAction.ACQUIRER_FINALIZE_RECONCILEMNET, ISOAcquirerFinalizeReconcilementJob.Instance);
	                put(ClearingAction.ISSUER_FINALIZE_RECONCILEMNET, ISOIssuerFinalizeReconcilementJob.Instance);
			}});
		}
			
		return clearingActionJobsImpl;
	}

	public ClearingActionJobsImpl getNeginClearingActionJobs() {
		if (neginClearingActionJobs == null){
			neginClearingActionJobs = new ClearingActionJobsImpl();
			neginClearingActionJobs.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{
				 put(ClearingAction.COUTOVER_RESPONSE, ISOCutoverResponseJob.Instance);
	                put(ClearingAction.RECONCILEMNET_REQUEST, ISOReconcilementRequstJob.Instance);
	                put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, NeginAcquirerReconcilementResponseJob.Instance);
	                put(ClearingAction.ISSUER_RECONCILEMNET_RESPONSE, NeginIssuerReconcilementResponseJob.Instance);
	                put(ClearingAction.ACQUIRER_FINALIZE_RECONCILEMNET, ISOAcquirerFinalizeReconcilementJob.Instance);
	                put(ClearingAction.ISSUER_FINALIZE_RECONCILEMNET, ISOIssuerFinalizeReconcilementJob.Instance);
			}});
		}
		return neginClearingActionJobs;
	}

	public ClearingActionJobsImpl getPosClearingActionJobs() {
		if (posClearingActionJobs == null){
			posClearingActionJobs = new ClearingActionJobsImpl();
			posClearingActionJobs.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{
				put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, POSAcquirerReconcilementResponseJob.Instance);
			}});
		}
		return posClearingActionJobs;
	}

	public ISOClearingActionMapper getIsoClearingActionMapper() {
		return ISOClearingActionMapper.Instance;
	}
}
