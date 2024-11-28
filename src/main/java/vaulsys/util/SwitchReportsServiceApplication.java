package vaulsys.util;

import vaulsys.job.AbstractSwitchJob;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.atm.ATMConnectionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class SwitchReportsServiceApplication {

	private static final SwitchReportsServiceApplication INSTANCE  = new SwitchReportsServiceApplication();

	private SwitchReportsServiceApplication(){}
	
	transient Logger logger = Logger.getLogger(this.getClass());
	
    private List<AbstractSwitchJob> switchReportsServiceJobs;
    

    public static SwitchReportsServiceApplication get() {
    	return INSTANCE;
    }

    public void startup() {
    	//initializeATMTerminals();
        initializeSwitchReportServiceJobs();
        //CoreGateway.setCoreTimeOut(20);
    }

    private void initializeATMTerminals() {
    	GeneralDao.Instance.executeSqlUpdate("update "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".term_atm set connection=" + ATMConnectionStatus.NOT_CONNECTED_VALUE);
	}

	public void initializeSwitchReportServiceJobs() {
		Map<String, String> jobMap = ConfigUtil.getProperties("job.report");
		switchReportsServiceJobs = new ArrayList<AbstractSwitchJob>();
		for (String clazz: jobMap.values())
			try {
				switchReportsServiceJobs.add((AbstractSwitchJob)Class.forName(clazz).newInstance());
			} catch (Exception e) {
				logger.error("Encounter with an exception in initializing SwitchJobs: "+ e.getClass().getSimpleName()+"- "+ e.getMessage());
			}
		
        for (AbstractSwitchJob switchJob : switchReportsServiceJobs) {
            try {
				switchJob.submitJob();
			} catch (Exception e) {
				logger.error(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
				e.printStackTrace();
				return;
			}
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

    public void afterPropertiesSet() throws Exception {
        if (GeneralDao.Instance == null) {
            throw new Exception("generalDao property is not defined");
        }
    }
}
