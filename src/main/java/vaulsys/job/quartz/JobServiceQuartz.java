package vaulsys.job.quartz;

import vaulsys.calendar.DateTime;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.JobSchedule;
import vaulsys.job.SwitchJobAlreadeySubmittedException;
import vaulsys.job.SwitchJobException;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.job.*;
import vaulsys.util.ConfigUtil;
import vaulsys.util.DBConfigUtil;
import vaulsys.util.Util;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

public class JobServiceQuartz {
    private static Logger logger = Logger.getLogger(JobServiceQuartz.class);
    private static Scheduler scheduler;

    public static final String SWITCH_JOB_KEY = "AbstractSwitchJob";
    private static final long JOB_START_DELAY = 5 * 1000; //five secound
    
    public static final int SWITCH_CONFIG = 1;
    public static final int SETTLE_CONFIG = 2;
    public static final int REPORT_CONFIG = 3;  //m.rehman: for reporting
    
    private static int CONFIG_PROFILE;
    
    public static void submit(AbstractSwitchJob job) throws Exception {
    	JobDetail oldJobDetail = scheduler.getJobDetail(job.getJobName(), job.getGroup().toString());
    	prepare(job);

		if (oldJobDetail != null) {
			if (CONFIG_PROFILE == SWITCH_CONFIG) {
				if (job instanceof ReversalJob || job instanceof OnlineSettlementJob || job instanceof PerTransactionSettlementJob
						|| job instanceof EODJob || job instanceof MCIVirtualVosoliJob || job instanceof NAJAVosoliJob
				 || job instanceof TransferSorushJob || job instanceof TransferSorushTableJob || job instanceof CycleGetAtmStatusJob) {
					deleteJob(job);
					createNewJob(job);
				}
			} else if (CONFIG_PROFILE == SETTLE_CONFIG) {
				if (job instanceof OnlinePerTransactionSettlementJob || job instanceof IssuingFCBDocumentJob) {
					deleteJob(job);
					createNewJob(job);
				}
			} else if (CONFIG_PROFILE == REPORT_CONFIG) {   //m.rehman: for reporting
                if (job instanceof ReportJob) {
                    deleteJob(job);
                    createNewJob(job);
                }
            }
		} else {
			createNewJob(job);
		}

	}

	private static void prepare(AbstractSwitchJob job) {
		String prefix ="";
    	if (CONFIG_PROFILE == SWITCH_CONFIG)
    		prefix = "job.switch";
    	else if (CONFIG_PROFILE == SETTLE_CONFIG)
    		prefix = "job.settle";
        else if (CONFIG_PROFILE == REPORT_CONFIG)   //m.rehman: for reporting
            prefix = "job.report";
    	
    	prefix += "."+ job.getJobName();
    	
    	String shouldRecover = ConfigUtil.getProperty(ConfigUtil.JOB_SHOULDRECOVER, prefix);
    	String durable = ConfigUtil.getProperty(ConfigUtil.JOB_DURABALE, prefix);
    	String volatilizeable = ConfigUtil.getProperty(ConfigUtil.JOB_VOLATILIZEABLE, prefix);
    	String executeNow = ConfigUtil.getProperty(ConfigUtil.JOB_EXECUTENOW, prefix);
    	String cronExpression = ConfigUtil.getProperty(ConfigUtil.JOB_CRONEXPRESSION, prefix);
    	
    	shouldRecover = Util.hasText(shouldRecover)?shouldRecover:"False";    	
    	durable = Util.hasText(durable)? durable: "False";
    	volatilizeable = Util.hasText(volatilizeable)? volatilizeable: "True";
    	executeNow = Util.hasText(cronExpression)? cronExpression: "False";
    	
    	
    	Integer repeatCount = Util.integerValueOf(ConfigUtil.getProperty(ConfigUtil.JOB_REPEATCOUNT, prefix));
    	repeatCount = (repeatCount == null)? -1: repeatCount;
    	
    	Integer misfireInstruction = Util.integerValueOf(ConfigUtil.getProperty(ConfigUtil.JOB_MISFIREINSTRUCTION, prefix));
    	Long repeatInterval = Util.longValueOf(ConfigUtil.getProperty(ConfigUtil.JOB_REPEATINTERVAL, prefix));
    	
    	Long startTime = Util.longValueOf(ConfigUtil.getProperty(ConfigUtil.JOB_STARTTIME, prefix));
    	long delayMin;
    	delayMin = (startTime != null && startTime >0)? DateTime.ONE_MINUTE_MILLIS * startTime: JOB_START_DELAY ;
		
		job.setShouldRecover(Boolean.parseBoolean(shouldRecover));
		job.setShouldRecover(Boolean.parseBoolean(durable));
		job.setShouldRecover(Boolean.parseBoolean(volatilizeable));
    	
    	if (job.getJobSchedule()== null)
    		job.setJobSchedule(new JobSchedule());
    	
		if (Util.hasText(cronExpression)){
			job.getJobSchedule().setCronExpression(cronExpression);
		}else if (repeatInterval != null){
			job.getJobSchedule().setRepeatCount(repeatCount);
			job.getJobSchedule().setRepeatInterval(repeatInterval);
		}else
			return;
		
		job.getJobSchedule().setStartTime(DateTime.fromNow(delayMin));
		
		if (misfireInstruction != null) 
			job.getJobSchedule().setMisfireInstruction(misfireInstruction);
		
		job.getJobSchedule().setExecuteNow(Boolean.parseBoolean(executeNow));
	}

    private static void createNewJob(AbstractSwitchJob job) throws SwitchJobException{
    	job.updateExecutionInfo();
        job.setScheduled(true);
        GeneralDao.Instance.saveOrUpdate(job);

        String jobName = job.getJobName();
        SwitchJobGroup switchJobGroup = job.getGroup() != null ? job.getGroup() : SwitchJobGroup.GENERAL;

        JobDetail jobDetail = prepareJobDetail(job, jobName, switchJobGroup);

        Trigger trigger = prepareTrigger(job);

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            job.updateStatus(SwitchJobStatus.FAILED);
            throw new SwitchJobException(e);
        }
    }
    
    public static void updateJob(AbstractSwitchJob job) throws Exception{
        System.out.println(job);
        JobDetail jobDetail = scheduler.getJobDetail(job.getJobName(), job.getGroup().toString());
        Trigger trigger[] = scheduler.getTriggersOfJob(job.getJobName(), job.getGroup().toString());
        jobDetail.getJobDataMap().put("ClrProfile", ((CycleSettlementJob) job).getClearingProfile().getId());

//        String jobName = job.getJobName();
//        SwitchJobGroup switchJobGroup = job.getGroup() != null ? job.getGroup() : SwitchJobGroup.GENERAL;

//        JobDetail jobDetail = prepareJobDetail(job, jobName, switchJobGroup);

//        Trigger trigger = prepareTrigger(job.);
        try {
            scheduler.addJob(jobDetail, true);
        } catch (SchedulerException e) {
            job.updateStatus(SwitchJobStatus.FAILED);
            throw new SwitchJobException(e);
        }
    }

    public static void getJobData(AbstractSwitchJob job) throws Exception{
        System.out.println(job);
        JobDetail jobDetail = scheduler.getJobDetail(job.getJobName(), job.getGroup().toString());
        Long id = (Long) jobDetail.getJobDataMap().get("ClrProfile");
        System.out.println(id);
//        jobDetail.getJobDataMap().put("ClrProfile", ((CycleSettlementJob) job).getClearingProfile().getId());

//        String jobName = job.getJobName();
//        SwitchJobGroup switchJobGroup = job.getGroup() != null ? job.getGroup() : SwitchJobGroup.GENERAL;

//        JobDetail jobDetail = prepareJobDetail(job, jobName, switchJobGroup);

//        Trigger trigger = prepareTrigger(job.);
        try {
            scheduler.addJob(jobDetail, true);
        } catch (SchedulerException e) {
            job.updateStatus(SwitchJobStatus.FAILED);
            throw new SwitchJobException(e);
        }
    }

    private static Trigger prepareTrigger(AbstractSwitchJob job) {
        Trigger trigger = null;
        if (job.getJobSchedule() != null) {
            //System.out.println("JobServiceQuart:: job.getJobName() ["+ job.getJobName() + "]"); //Raza TEMP
            trigger = job.getJobSchedule().prepareTrigger(job.getJobName()+"_Trigger");
            trigger.setMisfireInstruction(job.getJobSchedule().getMisfireInstruction());
        }
        if (trigger == null) {
            Date triggerStartTime = new Date(System.currentTimeMillis() + JOB_START_DELAY);
            //System.out.println("JobServiceQuart:: job.getJobName2() ["+ job.getJobName() + "]"); //Raza TEMP
            trigger = TriggerUtils.makeImmediateTrigger(job.getJobName()+"_Trigger", 1, Integer.MAX_VALUE);
            trigger.setStartTime(triggerStartTime);
        }

        trigger.setVolatility(job.isVolatilizeable());
        return trigger;
    }

    private static JobDetail prepareJobDetail(AbstractSwitchJob job, String jobName, SwitchJobGroup switchJobGroup) {
//        JobDetail jobDetail = new JobDetail(jobName, switchJobGroup.toString(), JobProxy.class);
      JobDetail jobDetail = new JobDetail(jobName, switchJobGroup.toString(), job.getClass());

        jobDetail.setDurability(job.isDurabale());
        jobDetail.setVolatility(job.isVolatilizeable());
        jobDetail.setRequestsRecovery(job.isShouldRecover());
        
        if(job instanceof CycleSettlementJob){
        	jobDetail.getJobDataMap().put("ClrProfile", ((CycleSettlementJob) job).getClearingProfile().getId());
        }
        
        return jobDetail;
    }

    public static void init(int configProfile) throws Exception{
		try {
			JobServiceQuartz.CONFIG_PROFILE = configProfile; 
			Properties props = new Properties();
			if (configProfile == SWITCH_CONFIG)
				props.load(JobServiceQuartz.class.getResourceAsStream(ConfigUtil.getProperty(ConfigUtil.QUARTS_SWITCH_CONFIG_FILE)));
			else if (configProfile == SETTLE_CONFIG)
				props.load(new FileInputStream(ConfigUtil.getProperty(ConfigUtil.QUARTS_SETTLE_CONFIG_FILE)));
            else if (configProfile == REPORT_CONFIG)   //m.rehman: for reporting
                props.load(JobServiceQuartz.class.getResourceAsStream(
                        ConfigUtil.getProperty(ConfigUtil.QUARTS_REPORT_CONFIG_FILE)));
			else
				throw new RuntimeException("Wrong Config File Profile");
			props.put("org.quartz.jobStore.dataSource", "qzDS");
			props.put("org.quartz.dataSource.qzDS.driver", "oracle.jdbc.driver.OracleDriver");

            //m.rehman: changing below to read db config from external file
            /*
			props.put("org.quartz.dataSource.qzDS.URL", ConfigUtil.getProperty(ConfigUtil.DB_URL));
			props.put("org.quartz.dataSource.qzDS.user", ConfigUtil.getProperty(ConfigUtil.DB_USERNAME));
			props.put("org.quartz.dataSource.qzDS.password", ConfigUtil.getDecProperty(ConfigUtil.DB_PASSWORD));
			String db_schema = ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA);
			*/

            props.put("org.quartz.dataSource.qzDS.URL", DBConfigUtil.getDecProperty(DBConfigUtil.DB_URL));
            props.put("org.quartz.dataSource.qzDS.user", DBConfigUtil.getDecProperty(DBConfigUtil.DB_USERNAME));
            props.put("org.quartz.dataSource.qzDS.password", DBConfigUtil.getDecProperty(DBConfigUtil.DB_PASSWORD));
            String db_schema = DBConfigUtil.getDecProperty(DBConfigUtil.DB_SCHEMA);

			if (Util.hasText(db_schema)){
				String prefix = (String) props.get("org.quartz.jobStore.tablePrefix");
				props.put("org.quartz.jobStore.tablePrefix", db_schema+"."+prefix);
			}
			props.put("org.quartz.dataSource.qzDS.maxConnections", "10");
			scheduler = new StdSchedulerFactory(props).getScheduler();
			// scheduler.setJobFactory(jobFactory);
		} catch (Exception e) {
			logger.error("Job Init", e);
			throw e;
		}
    }
    
    public static void start() throws Exception {
    	try {
			scheduler.start();
            logger.info("Scheduler started at " + new Date());
        } catch (SchedulerException ex) {
            logger.error(ex);
            throw ex;
        }
    }

    public static void pause() throws Exception {
        try {
            scheduler.standby();
            logger.info("Scheduler paused at " + new Date());
        } catch (SchedulerException ex) {
            logger.error(ex);
            throw ex;
        }
    }

    public static void stop() throws SchedulerException {
        try {
            scheduler.shutdown(true);
            logger.info("Scheduler stopped at " + new Date());
        } catch (SchedulerException ex) {
            logger.error(ex);
            throw ex;
        }
    }

    public static void pauseJob(AbstractSwitchJob job) throws SwitchJobException {
        try {
        	logger.debug("Pausing job with id " + job.getJobName());
            SwitchJobGroup switchJobGroup = job.getGroup() != null ? job.getGroup() : SwitchJobGroup.GENERAL;
            scheduler.pauseJob(job.getJobName(), switchJobGroup.toString());
            job.updateStatus(SwitchJobStatus.PAUSED);
        } catch (SchedulerException e) {
            job.updateStatus(SwitchJobStatus.PAUSE_FAILED);
            GeneralDao.Instance.saveOrUpdate(job);
            throw new SwitchJobException("Scheduler Error in pausing job", e);
        } catch (Exception e) {
            job.updateStatus(SwitchJobStatus.PAUSE_FAILED);
            GeneralDao.Instance.saveOrUpdate(job);
            throw new SwitchJobException("Unknown Error in pausing job", e);
        } finally {
            try {
                GeneralDao.Instance.releaseLock(job);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void deleteJob(AbstractSwitchJob job) throws SwitchJobException {
        try {
            GeneralDao.Instance.delete(job);
            scheduler.pauseJob(job.getJobName(), job.getGroup().toString());
            scheduler.unscheduleJob(job.getJobName()+"_Trigger", job.getGroup().toString());
            scheduler.deleteJob(job.getJobName(), job.getGroup().toString());
        } catch (SchedulerException e) {
            throw new SwitchJobException("Scheduler Error in deleting job", e);
        } catch (Exception e) {
            throw new SwitchJobException("Unknown Error in deleting job", e);
        }
    }

    public static void resumeJob(AbstractSwitchJob job) throws SwitchJobException {
        try {
            GeneralDao.Instance.lockReadAndWrite(job);
            logger.debug("Resuming job with id " + job.getJobName());
            String jobName = job.getJobName();
            SwitchJobGroup switchJobGroup = job.getGroup() != null ? job.getGroup() : SwitchJobGroup.GENERAL;

            job.updateStatus(SwitchJobStatus.RUNNING);
            // TODO This causes a bug when there is a daily or weekly or any repeating trigger
            scheduler.resumeJob(jobName, switchJobGroup.toString());
            GeneralDao.Instance.update(job);
        } catch (Exception e) {
            throw new SwitchJobException("Unknown Error in resuming job", e);
        } finally {
            GeneralDao.Instance.releaseLock(job);
        }
    }

    public static void changeJobSchedule(AbstractSwitchJob job, JobSchedule newJobSchedule)
            throws SwitchJobAlreadeySubmittedException, SwitchJobException {
        String triggerName = job.getJobName() + "_Trigger";
        Trigger newTrigger = newJobSchedule.prepareTrigger(triggerName);
        newTrigger.setJobName(job.getJobName() + "_Trigger");
        newTrigger.setVolatility(job.isVolatilizeable());
        newTrigger.setJobGroup(job.getGroup().toString());
        try {
            scheduler.rescheduleJob(triggerName, newTrigger.getGroup(), newTrigger);
        } catch (SchedulerException e) {
            throw new SwitchJobException(e);
        }

    }

    public static void delayedPauseJob(AbstractSwitchJob job) {
        job.updateStatus(SwitchJobStatus.PAUSING);
    }

    public static void delayedResumeJob(AbstractSwitchJob job) {
        job.updateStatus(SwitchJobStatus.RESUMING);
    }

    public static void delayedDeleteJob(AbstractSwitchJob job) {
        job.updateStatus(SwitchJobStatus.DELETING);
    }

    public static JobDetail findJob(String jobName, String jobGroup) throws SwitchJobException {
        try {
            return scheduler.getJobDetail(jobName, jobGroup);
        } catch (SchedulerException e) {
            throw new SwitchJobException(e);
        }
    }

    public static Scheduler getScheduler() {
        return scheduler;
    }

//    public void removeJob(Class<? extends AbstractSwitchJob> clazz) {
//        String query = "from " + clazz.getName();
//        List<AbstractSwitchJob> switchJobs = GeneralDao.Instance.find(query, null, false);
//        for (AbstractSwitchJob switchJob : switchJobs) {
//            deleteJob(switchJob);
//        }
//    }

//    public JobFactory getJobFactory() {
//        return jobFactory;
//    }
//
//    public void setJobFactory(JobFactory jobFactory) throws SchedulerException {
//        this.jobFactory = jobFactory;
//    }

//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        try {
//
//            for (String groupName : scheduler.getJobGroupNames()) {
////                if (!JobWatchingJob.JOB_GROUP.equals(groupName)) {
//                    for (String jobName : scheduler.getJobNames(groupName)) {
//                        AbstractSwitchJob switchJob = null;
//                        try {
//                            switchJob = GeneralDao.Instance.getObject(AbstractSwitchJob.class, jobName);
//                        } catch (Exception e) {
//                        	logger.debug("Error loading job with name = " + jobName);
//                            logger.error("Error loading job with name = "+ jobName +" "+ e, e);
////                            e.printStackTrace();
//                            handleJobFailure(jobName, groupName, switchJob, e);
//                        }
//                    }
////                }
//            }
//        } catch (SchedulerException e) {
//            logger.fatal(e);
//            throw new BeanCreationException("Error in creating scheduler", e);
//        } catch (IOException e) {
//            logger.fatal(e);
//            throw new BeanCreationException("Error in configuring scheduler, config file error", e);
//        } catch (Exception e) {
//            logger.fatal(e);
//            throw new BeanCreationException("Error", e);
//        }
//
//    }

//    protected void handleJobFailure(String jobName, String groupName, AbstractSwitchJob switchJob, Exception reason) {
//            try {
//            	logger.debug("Deleting and removing job: "+jobName);
//                scheduler.pauseJob(jobName, groupName);
//                scheduler.unscheduleJob(jobName + "_Trigger", groupName);
//                scheduler.deleteJob(jobName, groupName);
//            } catch (SchedulerException e) {
////            	logger.debug("Error deleting trigger or job, ajab badbakhtieee !!! ");
//                logger.error("Error deleting trigger or job. (switchJob: "+ switchJob+"; reason: "+ reason.getClass().getSimpleName()+"-"+reason.getMessage()+")" , e);
////                e.printStackTrace();
//            }
//    }
}
