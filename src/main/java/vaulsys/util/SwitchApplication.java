package vaulsys.util;

import vaulsys.job.AbstractSwitchJob;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.atm.ATMConnectionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fanap.cms.business.corecommunication.biz.CoreGateway;

public class SwitchApplication {

	private static final SwitchApplication INSTANCE  = new SwitchApplication();
	
	private SwitchApplication(){}
	
	transient Logger logger = Logger.getLogger(this.getClass());
	
    private List<AbstractSwitchJob> switchJobs;
    

    public static SwitchApplication get() {
    	return INSTANCE;
    }

    public void startup() {
    	initializeATMTerminals();
        //initializeSwitchJobs();
        CoreGateway.setCoreTimeOut(20);
    }

    private void initializeATMTerminals() {
    	GeneralDao.Instance.executeSqlUpdate("update "+DBConfigUtil.getDecProperty(DBConfigUtil.DB_SCHEMA)+".term_atm set connection=" + ATMConnectionStatus.NOT_CONNECTED_VALUE);
	}

	public void initializeSwitchJobs() {
		Map<String, String> jobMap = ConfigUtil.getProperties("job.switch");
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
