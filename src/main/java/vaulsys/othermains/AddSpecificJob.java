package vaulsys.othermains;

import com.ghasemkiani.util.icu.PersianCalendar;
import vaulsys.job.JobSchedule;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.job.ATMDailyRecordCycleSettlementJob;
import vaulsys.util.SettlementApplication;
import org.apache.log4j.Logger;

import java.util.Date;

public class AddSpecificJob {
    private static final Logger logger = Logger.getLogger(AddSpecificJob.class);
    public static void main(String[] args) {

        GeneralDao.Instance.beginTransaction();
        try {
//			JobServiceQuartz.init(JobServiceQuartz.SWITCH_CONFIG);
            JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);

            String cronExpression = generateCronExpression(null/*fireTime.toDate()*/);
            submitJob(new ATMDailyRecordCycleSettlementJob(), cronExpression);
        } catch (Exception e) {
            e.printStackTrace();
            GeneralDao.Instance.rollback();
            System.exit(1);
        }
        logger.info("before end tranaction");
        GeneralDao.Instance.endTransaction();
        logger.info("after end tranaction");
        System.exit(0);
    }

    private static String generateCronExpression(Date fromDate) {
        PersianCalendar pc;
        if (fromDate == null)
            pc = new PersianCalendar(0, 0, 0, 0, 0, 0);
        else
            pc = new PersianCalendar(fromDate);
        StringBuilder sb = new StringBuilder();

        return "0 30 2 */1 * ?";/*sb.append(CycleCriteriaConsts.zero).append(CycleCriteriaConsts.space).append(
                pc.get(PersianCalendar.MINUTE)).append(CycleCriteriaConsts.space).append(
                pc.get(PersianCalendar.HOUR_OF_DAY)).append(CycleCriteriaConsts.space).append(
                CycleCriteriaConsts.star).append(CycleCriteriaConsts.slash).append(
                1).append(CycleCriteriaConsts.space).append(
                CycleCriteriaConsts.star).append(CycleCriteriaConsts.space).append(CycleCriteriaConsts.qSign)
                .toString();*/
    }

    public static void submitJob(ATMDailyRecordCycleSettlementJob atmDailyRecordCycleSettlementJob, String cronExpression) {
        if (atmDailyRecordCycleSettlementJob == null)
            return;
        JobSchedule jobSchedule = new JobSchedule(cronExpression);
        atmDailyRecordCycleSettlementJob.setJobSchedule(jobSchedule);
        SettlementApplication.get().submitJob(atmDailyRecordCycleSettlementJob);
    }

    public static void update(String[] args) {

        try {
            GeneralDao.Instance.beginTransaction();
            JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);
            ATMDailyRecordCycleSettlementJob a = GeneralDao.Instance.load(ATMDailyRecordCycleSettlementJob.class,"ATMDailyRecordCycleSettlementJob_170600");
            JobServiceQuartz.updateJob(a);

//        List a = GeneralDao.Instance.executeSqlQuery("select job_data from epay.QRTZ_STL_JOB_DETAILS where job_name='ATMDailyCycleSettleJob_170605'");
//        System.out.println(a.get(0));

            GeneralDao.Instance.endTransaction();

/*

        GeneralDao.Instance.beginTransaction();
        List<CycleSettlementJob> aa = SchedulerService.getAllCycleSettlementJob();
        for(CycleSettlementJob bb : aa){
            System.out.println(bb.getClearingProfile().getId());

        }
        GeneralDao.Instance.endTransaction();
*/
            //org.slf4j.LoggerFactory;
//        System.out.println("Helssslo");
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
        }
    }

}
