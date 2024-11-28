package vaulsys.util;

import vaulsys.job.AbstractSwitchJob;
import vaulsys.persistence.GeneralDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fanap.cms.business.corecommunication.biz.CoreGateway;

public class SettlementApplication {

	private static final SettlementApplication INSTANCE  = new SettlementApplication();
	
	private SettlementApplication(){}
	
	transient Logger logger = Logger.getLogger(this.getClass());
	
    private List<AbstractSwitchJob> switchJobs;

    public static SettlementApplication get() {
    	return INSTANCE;
    }

    public void startup() {
    	initializeSwitchJobs();
    	initilizeCoreGateWay();
    }

    private void initilizeCoreGateWay() {
    	CoreGateway.setCoreTimeOut(20);
    }

	public void initializeSwitchJobs() {
    	Map<String, String> jobMap = ConfigUtil.getProperties("job.settle");
		switchJobs = new ArrayList<AbstractSwitchJob>();
		for (String clazz: jobMap.values())
			try {
				switchJobs.add((AbstractSwitchJob)Class.forName(clazz).newInstance());
			} catch (Exception e) {
				logger.error("Encounter with an exception in initializing SwitchJobs: "+ e.getClass().getSimpleName()+"- "+ e.getMessage());
			}
    	
        for (AbstractSwitchJob switchJob : switchJobs) { 
            try {
				switchJob.submitJob();
			} catch (Exception e) {
				logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
				e.printStackTrace();
				return;
			}
        }
    }
    
    public GeneralDao getGeneralDao() {
    	return GeneralDao.Instance;
    }

    public void afterPropertiesSet() throws Exception {
        if (getGeneralDao() == null) {
            throw new Exception("generalDao property is not defined");
        }
    }

	public void submitJob(AbstractSwitchJob job) {
        try {
			job.submitJob();
		} catch (Exception e) {
			logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
			e.printStackTrace();
		}
    }

}
