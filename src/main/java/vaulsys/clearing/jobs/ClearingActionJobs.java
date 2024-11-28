package vaulsys.clearing.jobs;

import vaulsys.clearing.base.ClearingAction;

public interface ClearingActionJobs {

    ClearingJob findClearingJob(ClearingAction action);

}
