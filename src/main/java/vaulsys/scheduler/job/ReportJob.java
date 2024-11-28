package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.reports.DBConnection;
import vaulsys.reports.PGPEncryption;
import vaulsys.scheduler.JobLog;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;
import org.apache.mina.core.file.FileRegion;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobExecutionContext;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.*;
import java.nio.file.Files;
import java.security.Security;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import org.bouncycastle.openpgp.*;

@Entity
@DiscriminatorValue(value = "Report")
public class ReportJob extends AbstractSwitchJob {
    private static final Logger logger = Logger.getLogger(ReportJob.class);

    private static boolean isFree = true;

    public void execute(JobExecutionContext switchJobContext, JobLog log) {

        logger.debug("Starting Report Job");

        if (!isJobFree()) {
            logger.error("Another thread is running... Exiting from ReportJob");
            log.setStatus(SwitchJobStatus.FINISHED);
            log.setExceptionMessage("Job is not free");
            return;
        }

        ProcessContext.get().init();

        GeneralDao.Instance.beginTransaction();

        List<Message> requests = new ArrayList<Message>();

        try {
            Object[] reportInfo;
            List<Object[]> reportJobInfos = getToBeGenerateReportInfo();

            if (reportJobInfos != null && reportJobInfos.size() > 0) {
                logger.info("Num of Reports to generate: " + reportJobInfos.size());

                //for (List<Object> reportInfo : reportJobInfos) {
                for (int i=0; i<reportJobInfos.size(); i++) {
                    reportInfo = reportJobInfos.get(i);
                    logger.info("try to generate Report (name): (" + reportInfo[1] + ")");
                    generateReport(reportInfo);
                }
            } else {
                logger.debug("No report available to generate");
            }
            log.setStatus(SwitchJobStatus.FINISHED);
        } catch (Exception e) {
            logger.error(e);
            log.setStatus(SwitchJobStatus.FAILED);
            log.setExceptionMessage(e.getMessage());
        } finally {
            setJobFree();
            GeneralDao.Instance.endTransaction();
        }

        if (!requests.isEmpty()) {
            MessageManager.getInstance().putRequests(requests);
        }

        logger.debug("Ending Report Job");
    }

    //m.rehman: This function will return the reports which need to generate
    public static List<Object[]> getToBeGenerateReportInfo()
            throws ParseException {
        try {
            DateTime dateTime = new DateTime(MyDateFormatNew.
                    parse("yyyyMMddhhmmss", new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())));
            String currentDate = dateTime.getDayDate().getDate().toString();
            String currentTime = Integer.toString(dateTime.getDayTime().getDayTime());

            String query = "SELECT ri.* FROM report_info ri" +
                    " where to_number(ri.report_schedule_time) < to_number('" + currentTime + "')" +
                    " and ri.report_status = '1'" +
                    " and ri.report_name not in" +
                    " (SELECT rh.report_name FROM report_history rh" +
                    " where rh.report_name = ri.report_name" +
                    " and rh.report_generate_date = '" + currentDate +"')";

            return GeneralDao.Instance.executeSqlQuery(query);

        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void generateReport(Object[] reportInfo) {

        String reportFileNameLocation, reportSPName, currentDate, fileData, reportName, reportPublicKeyLocation;
        ResultSet reportData;
        DateTime dateTime;

        try {
            fileData = "";
            dateTime = new DateTime(MyDateFormatNew.
                    parse("yyyyMMddhhmmss", new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())));
            currentDate = dateTime.getDayDate().getDate().toString();
            reportFileNameLocation = reportInfo[3].toString() + "\\" +reportInfo[5].toString() + currentDate
                    + "." + reportInfo[4].toString();
            reportSPName = reportInfo[9].toString();
            reportName = reportInfo[1].toString();
            reportPublicKeyLocation = reportInfo[10].toString();

            reportData = getReportData(reportSPName);

            //read data from DB ResultSet and save in file
            if (WriteDataToFile(reportData, reportName, reportFileNameLocation, fileData, reportPublicKeyLocation)) {
                logger.info("Report generated and saved successfully!!!");
            } else {
                logger.info("Report generation failed!!!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ResultSet getReportData(String spName) {

        Connection connection;
        CallableStatement callableStatement;
        String queryStr;
        ResultSet resultSet;

        resultSet = null;

        try {
            queryStr = "{ CALL PACKAGE_REPORTS." + spName + "(?) }";
            connection = DBConnection.InitializeDBConn();

            callableStatement = connection.prepareCall(queryStr);
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.executeUpdate();
            resultSet = (ResultSet) callableStatement.getObject(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }

    public boolean WriteDataToFile(ResultSet rsExportData, String reportName, String reportFileNameLocation,
                                   String fileData, String reportPublicKeyLocation)
            throws Exception
    {
        boolean fileWritingFlag;
        File file, tempFile;
        //FileOutputStream fStream;
        FileWriter fStream, tempFStream;
        BufferedWriter tempOut, out;
        ResultSetMetaData rsExportDataMD;
        String name, columnData;
        PGPEncryption pgpEncryption;
        String encryptedData;

        out = null;
        tempOut = null;
        encryptedData = null;
        try
        {
            fileWritingFlag = true;
            file = new File(reportFileNameLocation);
            tempFile = new File(reportFileNameLocation+".temp");
            //fStream = new FileOutputStream(file);
            fStream = new FileWriter(file);
            tempFStream = new FileWriter(tempFile);
            out = new BufferedWriter(fStream);
            tempOut = new BufferedWriter(tempFStream);
            rsExportDataMD = rsExportData.getMetaData();
            pgpEncryption = new PGPEncryption();

            while (rsExportData.next()) {
                for (int i = 1; i <= rsExportDataMD.getColumnCount(); i++) {
                    name = rsExportDataMD.getColumnName(i);
                    columnData = rsExportData.getString(name);
                    //out.write(columnData);
                    fileData += columnData;
                }
                //out.newLine();
                fileData += "\n";
            }

            if (!Util.hasText(fileData)) {
                logger.info("No transaction found for previous date");
                encryptedData = "";
            } else {

                //encrypt data through PGP encryption
                System.out.println(fileData);
                tempOut.write(fileData);
                encryptedData = pgpEncryption.encryptDecrypt(fileData, reportPublicKeyLocation);
                System.out.println(encryptedData);
                logger.info("Report encrypted successfully!!!");

                //write in file
                out.write(encryptedData);
                //fStream.write(encryptedData);

                //mark the report as generated in history table
                /*if (saveReportData(reportName, encryptedData) == 1)
                    logger.info("Report backup saved successfully!!!");
                else
                    logger.info("Report backup save failed!!!");*/

            }

            //mark the report as generated in history table
            if (saveReportData(reportName, encryptedData) == 1)
                logger.info("Record Entry saved successfully!!!");
            else
                logger.info("Record Entry save failed!!!");
        }
        catch (Exception e)
        {
            fileWritingFlag = false;
            out.close();
            tempOut.close();

            e.printStackTrace();
        }

        out.close();
        tempOut.close();
        return fileWritingFlag;
    }

    public int saveReportData(String reportName, String fileData) throws ParseException {

        try {
            DateTime dateTime = new DateTime(MyDateFormatNew.
                    parse("yyyyMMdd", new SimpleDateFormat("yyyyMMdd").format(new Date())));
            String currentDate = dateTime.getDayDate().getDate().toString();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("report_name",reportName);
            params.put("current_date",currentDate);
            params.put("file_data", fileData);

            String query = "insert into report_history values((select max(id)+1 from report_history), " +
                    ":report_name, :current_date, :file_data)";
            /*
            if (Util.hasText(fileData)) {
                params.put("file_data", fileData);
                query += ", :file_data";
            }
            query +=")";*/

            return GeneralDao.Instance.executeSqlUpdate(query, params);
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void interrupt() {
    }

    public void updateExecutionInfo() {
    }

    @Override
    public void submitJob() throws Exception {
        ReportJob newJob = new ReportJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.REPORT);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("ReportJob");
        JobServiceQuartz.submit(newJob);
    }

    @Override
    public boolean doLog() {
        return false;
    }

    public synchronized boolean isJobFree() {
        if (isFree == true) {
            isFree = false;
            return true;
        }
        return false;
    }

    public void setJobFree() {
        isFree = true;
    }
}
