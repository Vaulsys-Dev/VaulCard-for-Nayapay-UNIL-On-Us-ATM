package vaulsys.clearing.jobs;

import vaulsys.clearing.base.ClearingAction;

import java.util.Map;

public class ClearingActionJobsImpl implements ClearingActionJobs {

    private Map<ClearingAction, ClearingJob> clearingJobMap;

    public ClearingActionJobsImpl() {
    }

    public void setClearingJobMap(Map<ClearingAction, ClearingJob> clearingJobMap) {
        this.clearingJobMap = clearingJobMap;
    }


    public ClearingJob findClearingJob(ClearingAction action) {
        return clearingJobMap.get(action);
    }
}
