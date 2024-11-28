package vaulsys.othermains;


import vaulsys.calendar.DayDate;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.atm.ATMProducer;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.ConfigUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vaulsys.util.Pair;
import vaulsys.util.Util;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

public class NcrJournalATMService {


    public static final Logger logger = Logger.getLogger(NcrJournalATMService.class);

    public static void main(String[] args) throws Exception{
        String dayDate = null;
//        List<String> ipList = new ArrayList<String>();
//		String userName = "ssh";
//		String password= "NcrDotinAtm";
        String remoteDirectory = "journal/EJBackups/";
//		String remoteDirectory = "journal/";
        String localPath = "/switch/ATMsJournals/";
//		String ip = "10.20.12.70";
        String userName, password;
        List<Pair<Pair<String, Long>, String>> atmData;
        userName = ConfigUtil.getProperty(ConfigUtil.SFTP_AUTH_USERNAME);
        password = ConfigUtil.getDecProperty(ConfigUtil.SFTP_AUTH_PASSWORD);

        try {


            String addedQuery = "";
            Map<String, Object> params = new HashMap<String, Object>();
            if (args.length < 1) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                Calendar cal = Calendar.getInstance();
                dayDate = dateFormat.format(cal.getTime());
            } else {
                dayDate = args[0];
                if(dayDate.indexOf("/") != -1)
                    dayDate = (new DayDate((new SimpleDateFormat("yyyy/MM/dd").parse(dayDate)))).toString();
                else
                    dayDate = (new DayDate((new SimpleDateFormat("yyyyMMdd").parse(dayDate)))).toString();
                if (args.length > 1 && Util.hasText(args[1]) && Util.isValidInteger(args[1])) {
                    addedQuery = " and atm.code = :atmCode";
                    params.put("atmCode", Long.valueOf(args[1]));
                }

            }
            logger.info("day for get journal: " + dayDate);

            GeneralDao.Instance.beginTransaction();
            String query = "from ATMTerminal as atm where atm.producer = :type" + addedQuery;
            params.put("type", ATMProducer.NCR);

            List<ATMTerminal> atmList = GeneralDao.Instance.find(query, params);
            if (atmList != null && atmList.size() > 0) {
                atmData = new ArrayList<Pair<Pair<String, Long>, String>>();
                for (ATMTerminal atm : atmList) {
                    if(!atm.isEnabled()){
                        logger.info("atm with code " + atm.getCode() + ", is disabled");
                        continue;
                    }
                    if(!Util.hasText(atm.getIP())){
                        logger.info("atm with code " + atm.getCode() + ", hasnt ip");
                        continue;
                    }
                    logger.info("atm with code " + atm.getCode() + ", added to list");
                    atmData.add(new Pair(new Pair(atm.getIP(), atm.getCode()), atm.getOwner().getNameEn()));
                }
                GeneralDao.Instance.endTransaction();
            } else {
                logger.warn("not found any ATM!");
                GeneralDao.Instance.endTransaction();
                return;
            }
        } catch (Exception e) {
            logger.error("error in initilization" + e, e);
            GeneralDao.Instance.endTransaction();
            return;
        }

        NcrJournalATMService jouranalGetter = new NcrJournalATMService();
        jouranalGetter.startSftp(atmData, userName, password, remoteDirectory, localPath, dayDate);

    }

    public void startSftp(List<Pair<Pair<String, Long>, String>> atmList, String user, String password, String remoteDirectory, String localPath, String dayDate) {

        StandardFileSystemManager manager = new StandardFileSystemManager();
        FileObject remoteFile = null;
        for (Pair<Pair<String, Long>, String> atm : atmList) {
            try {
                manager.init();


                // Create remote file object
                logger.info("connecting to: " + atm.first.first + ", " + atm.first.second + ", " + atm.second);
                remoteFile = manager.resolveFile(createConnectionString(atm.first.first, user, password, remoteDirectory), createDefaultOptions());

                logger.info("connected successfully");
                if (remoteFile.exists()) {
                    //logger.info("remoteFile: " + remoteFile.getName());

                    if (remoteFile.getType().equals(FileType.FILE)) {
                        logger.warn("In this path is a File Not folder. we need a folder ");
                    } else if (remoteFile.getType().equals(FileType.FOLDER) && remoteFile.isReadable()) {
                        FileObject[] children = remoteFile.getChildren();
                        logger.info("Directory with " + children.length + " files");
                        for (int iterChildren = 0; iterChildren < children.length; iterChildren++) {
                            FileObject fileObject = children[iterChildren];


                            logger.info("file: " + fileObject.getName() + ", "+fileObject.getName().getBaseName()+", "+new SimpleDateFormat("yyyy/MM/dd").parse(new SimpleDateFormat("yyyy/MM/dd").format(fileObject.getContent().getLastModifiedTime())));
                            if (fileObject.getName().getBaseName().startsWith("EJ") && fileObject.getType().equals(FileType.FILE) && (new SimpleDateFormat("yyyy/MM/dd").parse(dayDate)).equals(new SimpleDateFormat("yyyy/MM/dd").parse(new SimpleDateFormat("yyyy/MM/dd").format(fileObject.getContent().getLastModifiedTime())))) {
                                String remoteDirectorywithFile = remoteDirectory + File.separator + fileObject.getName().getBaseName();
                                String connectionString = createConnectionString(atm.first.first, user, password, remoteDirectorywithFile);
                                remoteFile = manager.resolveFile(connectionString, createDefaultOptions());

                                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(dateFormatter.parse(dayDate));
                                cal.add(Calendar.DATE, -1);
                                String yesterdayDate = dateFormatter.format(cal.getTime());

                                //this path with date name
                                String downloadJournalPath = localPath + atm.first.second + "-" + atm.second + File.separator + (new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy/MM/dd").parse(yesterdayDate))) + "-" + System.currentTimeMillis() + ".txt";

                                // Create local file object
                                FileObject localFile = manager.resolveFile(downloadJournalPath);


                                localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);


                                logger.info("file +" + localFile.getName().getBaseName() + " downloaded success");

                            }
                        }

                    }
                }


            } catch (Exception e) {
                logger.error(e, e);
            } finally {
                manager.close();
            }
        }
        logger.info("finished");
    }

    public static String createConnectionString(String hostName,
                                                String username, String password, String remoteFilePath) {

        return "sftp://" + username + ":" + password + "@" + hostName + "/" + remoteFilePath;
    }

    public static FileSystemOptions createDefaultOptions() throws FileSystemException {
        // Create SFTP options
        FileSystemOptions opts = new FileSystemOptions();

        // SSH Key checking
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
                opts, "no");

        // Root directory set to user home
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);

        // Timeout is count by Milliseconds
        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

        return opts;
    }
}

