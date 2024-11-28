package vaulsys.job.quartz;

import org.quartz.JobExecutionContext;

/**
 * Adapter class to hide quartz detail from user.
 */
public class SwitchJobContextQuartz{

    private JobExecutionContext context;

    public SwitchJobContextQuartz(JobExecutionContext context) {
        this.context = context;
    }

    public Object getParameter(String paramName) {
        return context.getJobDetail().getJobDataMap().get(paramName);
    }

    public void setParameter(String paramName, Object paramValue) {
        context.getJobDetail().getJobDataMap().put(paramName, paramValue);
    }

}
