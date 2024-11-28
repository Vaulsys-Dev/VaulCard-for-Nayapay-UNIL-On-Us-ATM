package vaulsys.clearing.report;

import com.ghasemkiani.util.icu.PersianDateFormat;
import com.ibm.icu.util.StringTokenizer;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.othermains.disagreement.CompareShetabForm8;
import vaulsys.othermains.disagreement.Pasargad639347ShetabDocumenttNew;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Pair;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ShetabDisagreementService {

    private static Logger logger = Logger.getLogger(ShetabReconciliationService.class);
    
    
    public static final int TRX = 1;
    public static final int Inq = 2;
    public static final int Aut = 3;
    public static final int Rev = 4;
    public static final Long shaparakId = 581672L;
    
    
    public static ShetabReportRecord parseNormalRecord(String reportRecord, boolean isIssuingMode, Long myBIN) throws Exception {
        StringTokenizer tokenizer;
        tokenizer = new StringTokenizer(reportRecord, "|");

        // MyDateFormat dateFormat = new MyDateFormat("yyyy/MM/dd HH:mm:ss");
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");

        ShetabReportRecord record = new ShetabReportRecord();

        record.row = new Long(tokenizer.nextToken().trim());

        //for form9: toShetab file hase a space before type
        String trnType;
        if (!Util.hasText(trnType = tokenizer.nextToken().trim()))
            trnType = tokenizer.nextToken().trim();
        if (trnType.equals("WD"))
            trnType = "W";
        else if (trnType.equals("PU"))
            trnType = "P";
        record.type = ShetabReportConstants.shetabTrnTypeToIfxType.get(trnType);

        String dateStr = tokenizer.nextToken().trim();
        String timeStr = tokenizer.nextToken().trim();
        // Date date = dateFormat.parse(dateStr + " " + timeStr);
        Date date = dateFormatPers.parse(dateStr + " " + timeStr);
        // record.origDt = PersianCalendar.toGregorian(new DateTime(date));
        record.origDt = new DateTime(date);

        record.trnSeqCntr = tokenizer.nextToken().trim();
        record.appPan = tokenizer.nextToken().trim();
        record.destBankId = new Long(tokenizer.nextToken().trim());

        record.terminalId = tokenizer.nextToken().trim();

        record.amount = new Long(tokenizer.nextToken().trim());
        record.terminalType = ShetabReportConstants.shetabTermTypeToTerminalType.get(tokenizer.nextToken().trim());

        //Fee
        tokenizer.nextToken();
        if (tokenizer.hasMoreElements()) {
            record.secondAppPan = tokenizer.nextToken().trim();
        }

        if (isIssuingMode) {
            if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
                record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
                record.terminalId = Util.trimLeftZeros(record.terminalId);
            } else if (record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS))) {
                record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
                record.terminalId = record.terminalId;
            } else {
                record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
                record.terminalId = record.terminalId;
            }
        } else {
            record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
            record.terminalId = Util.trimLeftZeros(record.terminalId);
        }

        // fee is the other part that is ignored

        return record;
    }
    
    public static ShetabReportRecord parseBalInqRecord(String reportRecord, boolean isIssuingMode, Long myBIN) throws ParseException, Exception {

        StringTokenizer tokenizer;
        tokenizer = new StringTokenizer(reportRecord, "|");

        // MyDateFormat dateFormat = new MyDateFormat("yyyy/MM/dd HH:mm:ss");
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");

        ShetabReportRecord record = new ShetabReportRecord();

        record.row = new Long(tokenizer.nextToken().trim());

        String dateStr = tokenizer.nextToken().trim();
        String timeStr = tokenizer.nextToken().trim();
        // Date date = dateFormat.parse(dateStr + " " + timeStr);
        Date date = dateFormatPers.parse(dateStr + " " + timeStr);
        // record.origDt = PersianCalendar.toGregorian(new DateTime(date));
        record.origDt = new DateTime(date);

        record.appPan = tokenizer.nextToken().trim();
        record.destBankId = new Long(tokenizer.nextToken().trim());
        if (isIssuingMode) {
            record.terminalId = tokenizer.nextToken().trim();
            record.trnSeqCntr = Util.trimLeftZeros(tokenizer.nextToken().trim());
        } else {
            record.terminalId = Util.trimLeftZeros(tokenizer.nextToken().trim());
            record.trnSeqCntr = Util.trimLeftZeros(tokenizer.nextToken().trim());
        }
        record.terminalType = ShetabReportConstants.shetabTermTypeToTerminalType.get(tokenizer.nextToken().trim());

        // fee is the other part that is ignored
        tokenizer.nextToken().trim();

        record.type = ShetabReportConstants.shetabTrnTypeToIfxType.get(tokenizer.nextToken().trim());
        record.amount = new Long(tokenizer.nextToken().trim());

        return record;
    }
    
    
    /******************************************* For all disagreements ***************************************************/
    public static String compareFiles(String pathShetab, String pathSwitch, String path, boolean isPSP, boolean isTotalRep) {
        // TODO Auto-generated method stub
        StringTokenizer tokenizer = new StringTokenizer(pathShetab, "-");
        tokenizer.nextToken();
        String pathRes = path + "/comparison-" + tokenizer.nextToken() + "-" + System.currentTimeMillis() + ".txt";

        File switch_file = new File(pathSwitch);
        File shetab_file = new File(pathShetab);

        File disagreement_file = new File(pathRes);
        if (!disagreement_file.exists()) {
            try {
                disagreement_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e);
            }
        }

        BufferedWriter disagreement_bw = null;
        try {
            disagreement_bw = new BufferedWriter(new FileWriter(disagreement_file));
        } catch (IOException e2) {
            e2.printStackTrace();
            logger.error(e2);
        }
        String disagreement_str = "No error found";
        try {
            disagreement_str = finDisagreement(new BufferedReader(new FileReader(switch_file)), new BufferedReader(
                    new FileReader(shetab_file)), isPSP, isTotalRep);
        } catch (Exception e1) {
            e1.printStackTrace();
            logger.error(e1);
        }
        try {
            disagreement_bw.append(disagreement_str);
            disagreement_bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
        }
        return pathRes;
    }

    public static String finDisagreement(BufferedReader switch_br, BufferedReader shetab_br, boolean isPSP, boolean isTotalRep) throws IOException {

        String SWITCH_OK = "";
        String SWITCH_NOT = "";
        if (isPSP) {
            SWITCH_OK = "Switch-OK_PSP-NOT: ";
            SWITCH_NOT = "PSP-OK_Switch-NOT: ";
        } else {
            SWITCH_OK = "Switch-OK_SHETAB-NOT: ";
            SWITCH_NOT = "SHETAB-OK_Switch-NOT: ";
        }
        String[] switch_str = null;
        String[] shetab_str = null;
        String strTemp;
        List<String> list = new ArrayList<String>();

        StringBuilder difference_shetabOK_swichNOT = new StringBuilder();
        StringBuilder difference_shetabNOT_switchOK = new StringBuilder();
        StringBuilder difference = new StringBuilder();

        while (switch_br.ready())
            if ((strTemp = switch_br.readLine()).length() > 0)
                list.add(strTemp);
        switch_str = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            switch_str[i] = list.get(i);
        }
        list.clear();

        while (shetab_br.ready())
            if ((strTemp = shetab_br.readLine()).length() > 0)
                list.add(strTemp);
        shetab_str = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            shetab_str[i] = list.get(i);
        }
        list.clear();

        Arrays.sort(switch_str, new Comparator<String>() {
            @Override
            public int compare(String f1, String f2) {
                return f1.compareToIgnoreCase(f2);
            }
        });

        Arrays.sort(shetab_str, new Comparator<String>() {
            @Override
            public int compare(String f1, String f2) {
                return f1.compareToIgnoreCase(f2);
            }
        });

        int i = 0, j = 0;
        for (; i < switch_str.length && j < shetab_str.length; ) {
            //form13 & form13Extra
            if (isTotalRep) {
                if (switch_str[i].equals(shetab_str[j])) {
                    i++;
                    j++;
                    continue;
                } else {
                    difference.append("Switch: " + switch_str[i] + "\r\n");
                    difference.append("Shetab: " + shetab_str[j] + "\r\n");
                    difference.append("\r\n");

                    logger.debug("Switch: " + switch_str[i]);
                    logger.debug("Shetab: " + shetab_str[j] + "\r\n");

                    i++;
                    j++;
                }
            } else {
                if (switch_str[i].equals(shetab_str[j])) {
                    i++;
                    j++;
                    continue;
                } else if (switch_str[i].compareToIgnoreCase(shetab_str[j]) < 0) {
                    difference_shetabNOT_switchOK.append(SWITCH_OK + switch_str[i] + "\r\n");
                    logger.debug(switch_str[i]);
                    i++;
                } else {
                    difference_shetabOK_swichNOT.append(SWITCH_NOT + shetab_str[j] + "\r\n");
                    logger.debug(shetab_str[j]);
                    j++;
                }
            }
        }

        for (; i < switch_str.length; i++) {
            if (isTotalRep)
                difference.append("Switch: " + switch_str[i] + "\r\n");
            else
                difference_shetabNOT_switchOK.append(SWITCH_OK + switch_str[i] + "\r\n");
            logger.debug(switch_str[i]);
        }

        for (; j < shetab_str.length; j++) {
            if (isTotalRep)
                difference.append("Shetab: " + shetab_str[j] + "\r\n");
            else
                difference_shetabOK_swichNOT.append(SWITCH_NOT + shetab_str[j] + "\r\n");
            logger.debug(shetab_str[j]);
        }

        switch_br.close();
        shetab_br.close();

        difference.append(difference_shetabOK_swichNOT);
        difference.append("*********************************************" + "\r\n");
        difference.append(difference_shetabNOT_switchOK);
        return difference.toString();
    }

    public static List<ShetabReportRecord> parseRecords(BufferedReader brShetabReport, boolean isIssuingMode, Long myBIN, int reportType, boolean isPSP) throws Exception {
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
        List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
        ShetabReportRecord record;
        String reportRecord;
        StringTokenizer tokenizer;

        while (brShetabReport.ready()) {
            if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                try {
                    reportRecord = reportRecord.trim();
                    if (reportRecord == null || reportRecord.equals(""))
                        continue;
                    if (reportRecord.contains("---")) {
                        int indexOf = reportRecord.indexOf("---");
                        reportRecord = reportRecord.substring(indexOf + 3);
                    }
                    if (reportRecord.startsWith("BD:"))
                        reportRecord = reportRecord.substring(3);
                    if (reportRecord.startsWith("NF:"))
                        reportRecord = reportRecord.substring(3);

                    if (reportType == ShetabReconciliationService.Inq) {
                        records.add(parseBalInqRecord(reportRecord, isIssuingMode, myBIN));
                    } else if (reportType == ShetabReconciliationService.TRX) {
                        tokenizer = new StringTokenizer(reportRecord, "|");
                        record = new ShetabReportRecord();
                        record.row = new Long(tokenizer.nextToken().trim());

                        // for form9: toShetab file hase a space before type
                        String trnType;
                        trnType = tokenizer.nextToken().trim();
                        if (!Util.hasText(trnType) || "0000000000000".equals(trnType))
                            trnType = tokenizer.nextToken().trim();
                        if (trnType.equals("WD"))
                            trnType = "W";
                        else if (trnType.equals("PU"))
                            trnType = "P";
                        record.type = ShetabReportConstants.shetabTrnTypeToIfxType.get(trnType);

                        String dateStr = tokenizer.nextToken().trim();
                        String timeStr = tokenizer.nextToken().trim();
                        Date date = dateFormatPers.parse(dateStr + " " + timeStr);
                        record.origDt = new DateTime(date);
                        record.trnSeqCntr = tokenizer.nextToken().trim();
                        record.appPan = tokenizer.nextToken().trim();
                        record.destBankId = new Long(tokenizer.nextToken().trim());
                        record.terminalId = tokenizer.nextToken().trim();
                        record.amount = new Long(tokenizer.nextToken().trim());
                        record.terminalType = ShetabReportConstants.shetabTermTypeToTerminalType.get(tokenizer.nextToken().trim());
                        if (isPSP) {
                            tokenizer.nextToken();
                            tokenizer.nextToken();
                            tokenizer.nextToken();
                            String token = tokenizer.nextToken().trim();
                            record.merchantId = Util.trimLeftZeros(token);
                        } else {
                            // Fee
                            tokenizer.nextToken();
                            if (tokenizer.hasMoreElements())
                                record.secondAppPan = tokenizer.nextToken().trim();
                        }

                        if (isIssuingMode) {
                            if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS)
                                    || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
                                record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
                                record.terminalId = Util.trimLeftZeros(record.terminalId);
                            } else if (record.appPan.startsWith(myBIN.toString())
                                    && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS))) {
                                record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
                                record.terminalId = record.terminalId;
                            } else {
                                record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
                                record.terminalId = record.terminalId;
                            }
                        } else {
                            record.trnSeqCntr = Util.trimLeftZeros(record.trnSeqCntr);
                            record.terminalId = Util.trimLeftZeros(record.terminalId);
                        }
                        if (record.row % 500 == 0)
                            logger.debug("row:\t" + record.row);
                        records.add(record);
                    } else {
                        logger.error("Unknown report type...");
                        throw new Exception("Unknown report type...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            }
        }
        return records;
    }

    /******************************************* Form8 disagreement service ***********************************************/
    public static String compareForm8(String path_switch, String path_shetab) throws Exception {
        String path = path_shetab;
        String path_shetab_new;
        String pathOfResultFile = "";
        try {
            path_shetab_new = Pasargad639347ShetabDocumenttNew.getShetabFile(path_shetab);
            pathOfResultFile = CompareShetabForm8.compareFiles(path_shetab_new, path_switch, path);

            // decrease the number of params in each record
            summarizeResultOfForm8(pathOfResultFile);
            return pathOfResultFile;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }
    }

    private static void summarizeResultOfForm8(String pathOfResultFile) {
        try {
            File file = new File(pathOfResultFile);
            if (file != null) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String record;
                StringBuilder newResult = new StringBuilder();
                StringTokenizer tokenizer;
                String switchOK = "Switch-OK_Shetab-NOT: ";
                String shetabOK = "Shetab-OK_Switch-NOT: ";
                while (br.ready()) {
                    if ((record = br.readLine()).length() > 0) {
                        if (record.startsWith("*")) {
                            newResult.append("\r\n");
                            continue;
                        }
                        if (record.startsWith(shetabOK)) {
                            newResult.append(shetabOK);
                            record = record.substring(shetabOK.length());
                        } else if (record.startsWith(switchOK)) {
                            newResult.append(switchOK);
                            record = record.substring(switchOK.length());
                        }
                        tokenizer = new StringTokenizer(record, "/");
                        String s = tokenizer.nextToken();
                        newResult.append(s.substring(0, 12)).append("/"); // dateTime
                        newResult.append(ShetabReportConstants.TerminalTypeToAcqReportTermType.get(new TerminalType(Integer.valueOf(s.substring(12, 14)))))
                                .append("/"); // terminalType
                        newResult.append(s.substring(14)).append("/"); // trnSeqCntr
                        for (int i = 0; i < 4; i++)
                            tokenizer.nextToken();
                        newResult.append(tokenizer.nextToken()).append("/"); // amount
                        tokenizer.nextToken();
                        String trnType = ShetabReportConstants.statementCodeToShetabTrnType.get(tokenizer.nextToken());
                        if ("P".equals(trnType))
                            trnType += "U";
                        if ("W".equals(trnType))
                            trnType += "D";
                        newResult.append(trnType).append("/"); // trnType
                        for (int i = 0; i < 9; i++)
                            tokenizer.nextToken();
                        for (int i = 0; i < 3; i++)
                            newResult.append(tokenizer.nextToken()).append("/"); // cards
                    }
                    newResult.append("\r\n");
                }
                BufferedWriter result = new BufferedWriter(new FileWriter(pathOfResultFile));
                result.append(newResult.toString());
                result.flush();
                result.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
        }
    }

    public static List<Ifx> findForm8DisagreementInDB(String path) throws Exception {
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            List<IssShetabReportRecord> records = new ArrayList<IssShetabReportRecord>();
            List<Ifx> ifx = new ArrayList<Ifx>();
            List<Ifx> ifxs = new ArrayList<Ifx>();
            String str = "";
            while (br.ready()) {
                if ((str = br.readLine()).length() > 0)
                    records.add(parseIssuerShetabReport(str));
            }
            GeneralDao.Instance.beginTransaction();
            for (int i = 0; i < records.size(); i++) {
                IssShetabReportRecord record = records.get(i);

                String queryString = "select m "
                        + " from Ifx m "
                        + " where "
                        + " m.networkTrnInfo.OrigDt = :origDt "
//					+ " and m.eMVRqData.Auth_Amt = :amount "
                        + " and m.trnType = :trnType "
                        + " and m.ifxType = :ifxType "
                        + " and m.networkTrnInfo.TerminalType = :terminalType "
                        + " and m.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr "
                        + " and m.eMVRqData.CardAcctId.actualAppPAN = :appPan "
//					+ " and m.networkTrnInfo.BankId = :bankId "
                        ;
                if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)
                        || TrnType.DECREMENTALTRANSFER.equals(record.trnType)
                        || TrnType.TRANSFER.equals(record.trnType))
                    queryString += " and m.eMVRqData.actualSecondAppPan = :destAppPan "
//						+ " and m.networkTrnInfo.DestBankId = :destBankId "
                            ;

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("origDt", record.origDt);
//				params.put("amount", record.amount);
                params.put("trnType", record.trnType);
                params.put("ifxType", record.type);
                params.put("terminalType", record.terminalType);
                params.put("trnSeqCntr", record.trnSeqCntr);
                params.put("appPan", record.appPan);
//				params.put("bankId", record.bankId);
                params.put("destAppPan", record.destAppPan);
//				params.put("destBankId", record.destBankId);

                ifx = GeneralDao.Instance.find(queryString, params);

                if (ifx == null || ifx.size() == 0) {
                    logger.debug("Record: " + record.toString() + "	Not fount in dataBase");
                } else if (ifx.size() == 1) {
                    logger.debug("record: " + record.toString() + " found!");
                    ifxs.add(ifx.get(0));
                } else if (ifx.size() > 1) {
                    logger.debug("Found more than 1 ifx for record: " + record.toString());
                }
            }
            GeneralDao.Instance.endTransaction();
            return ifxs;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }
    }

    public static IssShetabReportRecord parseIssuerShetabReport(String line) throws Exception {
        try {
            IssShetabReportRecord record = new IssShetabReportRecord();
            if (line.startsWith("FromShetab:")) {
                line = line.substring("FromShetab:".length());
            } else if (line.startsWith("ToShetab:  ")) {
                line = line.substring("ToShetab:  ".length());
            }
            line = line.trim();
            StringTokenizer tokenizer = new StringTokenizer(line, "/");
            //C1
            String c1 = tokenizer.nextToken();
            PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = "13" + c1.substring(0, 2) + "/" + c1.substring(2, 4) + "/" + c1.substring(4, 6);
            String time = c1.substring(6, 8) + ":" + c1.substring(8, 10) + ":" + c1.substring(10, 12);
            Date dateTime = dateFormatPers.parse(date + " " + time);

            record.origDt = new DateTime(dateTime);

            //C2, C3, C4, C5
            for (int i = 0; i < 4; i++)
                tokenizer.nextToken();
            //C6
            record.amount = Long.valueOf(tokenizer.nextToken().trim());
            //C7
            String debitCredit = tokenizer.nextToken();
            //C8
            String statementCode = tokenizer.nextToken().trim();
            record.trnType = ShetabReportConstants.statementCodeToTrnType.get(statementCode);
            record.type = ShetabReportConstants.shetabTrnTypeToIfxType.get(ShetabReportConstants.statementCodeToShetabTrnType.get(statementCode));
            //C9, C10
            for (int i = 0; i < 2; i++)
                tokenizer.nextToken();
            //C11
            record.terminalType = new TerminalType(Integer.parseInt(tokenizer.nextToken().trim()));
            //C12, C13
            for (int i = 0; i < 2; i++)
                tokenizer.nextToken();
            //C14
            record.trnSeqCntr = tokenizer.nextToken().trim();
            //C15
            tokenizer.nextToken();
            //C16 : twoDigitBankCode
            String twoDigitCode = tokenizer.nextToken();
            //C17
            tokenizer.nextToken();
            /****** Transfer ******/
            //C18, C19, C20
            String C18 = tokenizer.nextToken();
            String C19 = tokenizer.nextToken();
            String C20 = tokenizer.nextToken();
            if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
                if (!C18.equals("0000000000000000000")) {
                    record.appPan = C19;//C18
                    record.destAppPan = C18; //C19
                } else {
                    record.appPan = C20;//C19
                    record.destAppPan = C19;//C20
                }
            } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {
                if (!C18.equals("0000000000000000000")) {
                    record.appPan = C18;//C19
                    record.destAppPan = C19;//C18
                } else {
                    record.appPan = C20; //C19
                    record.destAppPan = C19; //C20
                }
            } else if (TrnType.TRANSFER.equals(record.trnType)) {
                record.appPan = C18; //C19
                record.destAppPan = C19; //C18
            } else
                record.appPan = C18;
            /*********************/
            record.bankId = Long.valueOf(record.appPan.substring(0, 6));
            if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)
                    || TrnType.DECREMENTALTRANSFER.equals(record.trnType)
                    || TrnType.TRANSFER.equals(record.trnType))
                record.destBankId = Long.valueOf(record.destAppPan.substring(0, 6));

            return record;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }

    }


    /******************************************** Form9PSP disagreement service *****************************************************/
   
    /****************************************** Form9 disagreement service *****************************************************/
    public static String compareForm9(String path_switch, String path_shetab) throws Exception {
        String path = path_shetab;
        String path_switch_new;
        String path_shetab_new;
        String pathOfResultFile = "";
        try {
            path_switch_new = reconcileMyForm9(path_switch, path, false);
            path_shetab_new = reconcileShetabForm9(path_shetab);
            pathOfResultFile = compareFiles(path_shetab_new, path_switch_new, path, false, false);

            // decrease the number of params in each record
            summarizeResultOfForm9(pathOfResultFile);
            return pathOfResultFile;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }
    }

    private static void summarizeResultOfForm9(String pathOfResultFile) {
        try {
            File file = new File(pathOfResultFile);
            StringTokenizer tokenizer;
            if (file != null) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String record;
                StringBuilder newResult = new StringBuilder();
                while (br.ready()) {
                    if ((record = br.readLine()).length() > 0) {
                        if (record.startsWith("*")) {
                            newResult.append("\r\n");
                            continue;
                        }
                        tokenizer = new StringTokenizer(record, "|");
                        for (int i = 0; i < 4; i++)
                            newResult.append(tokenizer.nextToken()).append("|");
                        tokenizer.nextToken(); // delete bankBin
                        for (int i = 0; i < 3; i++)
                            newResult.append(tokenizer.nextToken()).append("|");
                    }
                    newResult.append("\r\n");
                }
                BufferedWriter result = new BufferedWriter(new FileWriter(pathOfResultFile));
                result.append(newResult.toString());
                result.flush();
                result.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
        }
    }

    public static String reconcileMyForm9(String path_toShetab, String path, boolean isPSP) throws FileNotFoundException {
        String fileExt = "-acq-";
        // if(args.length < 1) {
        // System.out.println("Enter report files path as input paramater...");
        // path = "D:/disagreement/form9/900926";
        // filePath = "MR_502229.txt";
        // }else{
        // path = args[0];
        // }

        GeneralDao.Instance.beginTransaction();
        GlobalContext.getInstance().startup();
        ProcessContext.get().init();
        GeneralDao.Instance.endTransaction();

        try {
            // Input file
            File file = new File(path_toShetab);
            if (file == null)
                return null;
            BufferedReader br = new BufferedReader(new FileReader(file));

            // Output File
            String pathRes;
            if (isPSP)
                pathRes = path + "/" + file.getName().substring(0, file.getName().length() - 4) + fileExt + "report9ToPSP.txt";
            else
                pathRes = path + "/" + file.getName().substring(0, file.getName().length() - 4) + fileExt + "report9ToShetab.txt";
            File shetabReportRes = new File(pathRes);
            BufferedWriter result = new BufferedWriter(new FileWriter(shetabReportRes));
            List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
            records = parseRecords(br, false, ProcessContext.get().getMyInstitution().getBin(),
                    ShetabReconciliationService.TRX, isPSP)/*extractAndParseForm9Record(br, isPSP)*/;
            result.append(generateReport9File(records, isPSP));
            result.flush();
            result.close();

            return pathRes;
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public static String reconcileShetabForm9(String path_fromShetab) {

        final String bankName = "pas";
        String fileExt = "-acq-";
        // if(args.length < 1) {
        // System.out.println("Enter report files path as input paramater...");
        // path = "D:/disagreement/form9/900926";
        // }else{
        // path = args[0];
        // }

        File folder = new File(path_fromShetab);
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().startsWith(bankName.toLowerCase()) && name.endsWith(".zip") && !name.contains("SANAD");
            }
        });

        List<File> fileName = new ArrayList<File>();
        for (int i = 0; i < folder.list().length; i++) {
            fileName.add(folder.listFiles()[i]);
        }

        if (files == null) {
            // System.exit(0);
            return null;
        }

        ZipFile zipFile;

        GeneralDao.Instance.beginTransaction();
        GlobalContext.getInstance().startup();
        ProcessContext.get().init();
        GeneralDao.Instance.endTransaction();

        String[] shetabReconcilationFiles = ShetabReconciliationService.getShetabReconcilationFiles(GlobalContext.getInstance()
                .getMyInstitution().getBin());
        for (File file : files) {
            logger.debug("Processing file:" + file.getName());
            try {
                String pathRes = path_fromShetab + "/" + file.getName().substring(0, file.getName().length() - 4) + fileExt + "report9FromShetab.txt";
                File shetabReportRes = new File(pathRes);
                if (!shetabReportRes.exists()) {
                    shetabReportRes.createNewFile();
                }
                BufferedWriter result = new BufferedWriter(new FileWriter(shetabReportRes));
                zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    for (int i = 0; i < shetabReconcilationFiles.length; i++) {
                        if (entry.getName().endsWith(shetabReconcilationFiles[i])) {
                            try {
                                logger.debug("Entry:" + entry.getName());
                                if (!entry.getName().endsWith(".Acq"))
                                    continue;
                                InputStream inputStream = zipFile.getInputStream(entry);
                                List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
                                records = parseRecords(new BufferedReader(new InputStreamReader(inputStream)), false, ProcessContext.get().getMyInstitution().getBin(),
                                        ShetabReconciliationService.TRX, false);
                                /*extractAndParseForm9Record(new BufferedReader(new InputStreamReader(inputStream)), false);*/
                                result.append(generateReport9File(records, false));
                                result.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error(e);
                            }
                            break;
                        }
                    }// for
                }// while
                logger.debug("Processing file:" + file.getName());
                result.flush();
                result.close();

                return pathRes;

            } catch (Exception e1) {
                e1.printStackTrace();
                logger.error(e1);
            }
        }
        return null;
    }

    public static List<Ifx> findForm9DisagreementInDB(String path) throws Exception {
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            List<AcqShetabReportRecord> records = new ArrayList<AcqShetabReportRecord>();
            List<Ifx> ifx = new ArrayList<Ifx>();
            List<Ifx> ifxs = new ArrayList<Ifx>();
            String str = "";
            while (br.ready()) {
                if ((str = br.readLine()).length() > 0)
                    records.add(parseAcquireShetabReport(str));
            }
            GeneralDao.Instance.beginTransaction();
            for (int i = 0; i < records.size(); i++) {
                AcqShetabReportRecord record = records.get(i);

                String queryString = "select m "
                        + " from Ifx m "
                        + " where "
                        + " m.ifxType = :ifxType "
                        + " and m.networkTrnInfo.OrigDt = :origDt "
                        + " and m.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr ";
                if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(record.type) || IfxType.TRANSFER_TO_ACCOUNT_RS.equals(record.type))
                    queryString += " and m.eMVRqData.actualSecondAppPan = :destAppPan ";
                else
                    queryString += " and m.eMVRqData.CardAcctId.actualAppPAN = :appPan ";
                queryString +=
//					+ " and m.eMVRqData.Auth_Amt = :amount "
//					+ " and m.networkTrnInfo.DestBankId = :destBankId "
//					+ " and m.networkTrnInfo.BankId = :bankId "
                        " and m.networkTrnInfo.TerminalId = :terminalId"
                                + " and m.networkTrnInfo.TerminalType = :terminalType "
                ;

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("ifxType", record.type);
                params.put("origDt", record.origDt);
                params.put("trnSeqCntr", record.trnSeqCntr);
                if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(record.type))
                    params.put("destAppPan", record.destAppPan);
                else
                    params.put("appPan", record.appPan);
//				params.put("destBankId", record.destBankId);
//				params.put("bankId", record.bankId);
//				params.put("amount", record.amount);
                params.put("terminalId", record.terminalId);
                params.put("terminalType", record.terminalType);

                ifx = GeneralDao.Instance.find(queryString, params);

                if (ifx == null || ifx.size() == 0) {
                    logger.debug("Record: " + record.toString() + "	Not fount in dataBase");
                } else if (ifx.size() == 1) {
                    logger.debug("record: " + record.toString() + " found!");
                    ifxs.add(ifx.get(0));
                } else if (ifx.size() > 1) {
                    logger.debug("Found more than 1 ifx for record: " + record.toString());
                }
            }
            GeneralDao.Instance.endTransaction();
            return ifxs;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }
    }

    public static AcqShetabReportRecord parseAcquireShetabReport(String line) throws Exception {
        try {
            AcqShetabReportRecord record = new AcqShetabReportRecord();
            if (line.startsWith("FromShetab:")) {
                line = line.substring("FromShetab:".length());
            } else if (line.startsWith("ToShetab:  ")) {
                line = line.substring("ToShetab:  ".length());
            }
            line = line.trim();
            StringTokenizer tokenizer = new StringTokenizer(line, "|");
            //C1
            record.type = ShetabReportConstants.shetabTrnTypeToIfxType.get(tokenizer.nextToken().trim());
            //C2
            String dateTimeStr = tokenizer.nextToken().trim();
            PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = "13" + dateTimeStr.substring(0, 2) + "/" + dateTimeStr.substring(2, 4) + "/" + dateTimeStr.substring(4, 6);
            String time = dateTimeStr.substring(6, 8) + ":" + dateTimeStr.substring(8, 10) + ":" + dateTimeStr.substring(10, 12);
            Date dateTime = dateFormatPers.parse(date + " " + time);
            record.origDt = new DateTime(dateTime);
            //C3 (yekbar be long va dobare be String tabdil mikonim ta sefrhae ezafe samte chepesh bardashte shavad)
            record.trnSeqCntr = (Long.valueOf(tokenizer.nextToken().trim())).toString();
            //C4
            if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(record.type))
                record.destAppPan = tokenizer.nextToken().trim();
            else
                record.appPan = tokenizer.nextToken().trim();
            //C5
            record.destBankId = Long.valueOf(tokenizer.nextToken().trim());
            //C6 (yekbar be long va dobare be String tabdil mikonim ta sefrhae ezafe samte chepesh bardashte shavad)
            record.terminalId = (Long.valueOf(tokenizer.nextToken().trim())).toString();
            //C7
            record.amount = Long.valueOf(tokenizer.nextToken().trim());
            //C8
            record.terminalType = ShetabReportConstants.shetabTermTypeToTerminalType.get(tokenizer.nextToken().trim());

            return record;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }

    }

    public static String generateReport9File(List<ShetabReportRecord> records, boolean isPSP) {
        StringBuilder report9th = new StringBuilder();
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMddHHmmss");
        for (ShetabReportRecord record : records) {
            report9th.append(StringFormat.formatNew(2, StringFormat.JUST_LEFT,
            		ShetabReportConstants.ifxTypeToShetabTrnType.get(record.type), ' ')).append('|');
            report9th.append(dateFormatPers.format(record.origDt.toDate())).append('|');
            report9th.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0')).append('|');
            report9th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ')).append('|');
            report9th.append(StringFormat.formatNew(9, StringFormat.JUST_LEFT, record.destBankId, ' ')).append('|');
            report9th.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, record.terminalId, '0')).append('|');
            report9th.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.amount, '0')).append('|');
            report9th.append(StringFormat.formatNew(3, StringFormat.JUST_RIGHT,
            		ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType) + "", '0')).append('|');
            if (isPSP)
                report9th.append(StringFormat.formatNew(12, StringFormat.JUST_LEFT, record.merchantId, '0')).append('|');
            report9th.append("\r\n");
        }
        return report9th.toString();
    }

    /********************************** Form9PSP disagreement service ***********************************/
    public static String comparePSPform9(String path_switch, String path_PSP, String path) throws Exception {
        String path_switch_new;
        String path_PSP_new;
        String pathOfResultFile = "";
        try {
            path_switch_new = reconcileMyForm9(path_switch, path, true);
            path_PSP_new = recocilePSPform9(path_PSP, path, path_switch);
            pathOfResultFile = compareFiles(path_PSP_new, path_switch_new, path, true, false);
            return pathOfResultFile;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }
    }

    public static String recocilePSPform9(String path_PSP, String path, String path_switch) {

        GeneralDao.Instance.beginTransaction();
        GlobalContext.getInstance().startup();
        ProcessContext.get().init();
        GeneralDao.Instance.endTransaction();

        try {
            File file = new File(path_PSP);
            if (file == null)
                return null;
            logger.debug("Processing file:" + file.getName());

            String pathRes = path + "/" + path_switch.substring(path_switch.indexOf("MR_") + "MR_".length(), path_switch.indexOf(".acq")) + "-report9FromPSP.txt";
            File PSPreportRes = new File(pathRes);
            if (!PSPreportRes.exists()) {
                PSPreportRes.createNewFile();
            }
            BufferedWriter result = new BufferedWriter(new FileWriter(PSPreportRes));
/*			ZipEntry zipEntry;
			FileInputStream inputStream =null;
			ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
			for(zipEntry = zip.getNextEntry(); zipEntry != null; zipEntry = zip.getNextEntry()){
				if(!zipEntry.isDirectory() && zipEntry.getName().endsWith(".acq"))
					break;
			}
			FileOutputStream fos = new FileOutputStream(path+"/FromPSP.txt");
			int n=0;
			byte[] buf = new byte[5120];
			while((n = zip.read(buf,0,1024))> -1)
				fos.write(buf, 0, n);
			fos.close();
			zip.closeEntry();*/
            List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
            records = parseRecords(new BufferedReader(new InputStreamReader(new FileInputStream(new File(path_PSP)))), false, ProcessContext.get().getMyInstitution().getBin(),
                    ShetabReconciliationService.TRX, true);
            result.append(generateReport9File(records, true));
            result.flush();
            result.close();

            return pathRes;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return null;
    }

    /**
     * ********************************************** Form13 disagreement service ******************************************************
     */
    public static String compareForm13(String path_switch, String path_shetab) throws Exception {
        String path = path_shetab;
        String path_shetab_new;
        String pathOfResultFile = "";
        try {
            path_shetab_new = reconcileShetabForm13(path_shetab);
            pathOfResultFile = compareFiles(path_shetab_new, path_switch, path, false, true);
            return pathOfResultFile;
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e);
            throw e;
        }
    }

    public static String reconcileShetabForm13(String path_shetab) {
        final String bankName = ConfigUtil.getProperty(ConfigUtil.BANK_NAME);// "pas";
        String fileExt = "-rep-";

        File folder = new File(path_shetab);
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().startsWith(bankName.toLowerCase()) && name.endsWith(".zip") && !name.contains("SANAD");
            }
        });

        List<File> fileName = new ArrayList<File>();
        for (int i = 0; i < folder.list().length; i++) {
            fileName.add(folder.listFiles()[i]);
        }

        if (files == null) {
//			System.exit(0);
            return null;
        }

        ZipFile zipFile;

        GeneralDao.Instance.beginTransaction();
        GlobalContext.getInstance().startup();
        ProcessContext.get().init();
        GeneralDao.Instance.endTransaction();

        for (File file : files) {
            logger.debug("Processing file:" + file.getName());
            try {
                String pathRes = path_shetab + "/" + file.getName().substring(0, file.getName().length() - 4) + fileExt + "report13FromShetab.txt";
                File shetabReportRes = new File(pathRes);
                if (!shetabReportRes.exists()) {
                    shetabReportRes.createNewFile();
                }
                BufferedWriter result = new BufferedWriter(new FileWriter(shetabReportRes));
                zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
//					for(int i=0; i<shetabReconcilationFiles.length; i++) {
                    if (entry.getName().endsWith(".rep_txt") && !entry.getName().toUpperCase().contains("ARZI")) {
                        try {
                            logger.debug("Entry:" + entry.getName());
                            if (!entry.getName().endsWith(".rep_txt"))
                                continue;
                            InputStream inputStream = zipFile.getInputStream(entry);
                            List<TotalShetabRepRecord> records = new ArrayList<TotalShetabRepRecord>();
                            records = extractAndParseTotalShetabRecord(new BufferedReader(new InputStreamReader(inputStream)), false);

                            result.append(generateReport13File(records, false));
//								result.flush();
                            result.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error(e);
                        }
                        break;
                    }
//					}//for
                }//while
                zipFile.close();
                logger.debug("Processing file:" + file.getName());
//				result.flush();
                result.close();

                return pathRes;

            } catch (Exception e1) {
                e1.printStackTrace();
                logger.error(e1);
            }
        }
        return null;
    }

    private static List<TotalShetabRepRecord> extractAndParseTotalShetabRecord(
            BufferedReader brShetabReport, boolean isPSP) throws IOException {
        List<TotalShetabRepRecord> records = new ArrayList<TotalShetabRepRecord>();
        TotalShetabRepRecord record = null;
        String reportRecord;
        StringTokenizer tokenizer;
        try {
            while (brShetabReport.ready()) {
                if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                    logger.debug("record: " + reportRecord);
                    tokenizer = new StringTokenizer(reportRecord, "|");
                    for (int i = 0; i < 10; i++)
                        tokenizer.nextToken();
                    if ("F".equals(tokenizer.nextToken().toUpperCase()))
                        continue;
                    else {
                        tokenizer = new StringTokenizer(reportRecord, "|");
                        String s = tokenizer.nextToken().trim();
                        record = new TotalShetabRepRecord();
                        record.terminalType = ShetabReportConstants.shetabTermTypeToTerminalType.get(s.substring(0, 3));
                        String recordTrnType = s.substring(3, 5);
                        String cardType = s.substring(5);
                        if ("WD".equals(recordTrnType))
                            recordTrnType = "W";
                        if ("PU".equals(recordTrnType))
                            recordTrnType = "P";
                        record.ifxType = ShetabReportConstants.shetabTrnTypeToIfxType.get(recordTrnType);

                        tokenizer.nextToken(); // number of acqCreditTrx
                        record.amount_acq_credit_3 = Long.valueOf(tokenizer.nextToken());

                        tokenizer.nextToken(); // number of acqDebitTrx
                        record.amount_acq_debit_5 = Long.valueOf(tokenizer.nextToken());

                        tokenizer.nextToken(); // number of issCreditTrx
                        record.amount_iss_credit_7 = Long.valueOf(tokenizer.nextToken());

                        tokenizer.nextToken(); // number of issDebitTrx
                        record.amount_iss_debit_9 = Long.valueOf(tokenizer.nextToken());

                        tokenizer.nextToken(); // pure amount

                        record.recordType = tokenizer.nextToken();

                        if ("O".equals(cardType)) {
                            if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                                tokenizer = new StringTokenizer(reportRecord, "|");
                                for (int i = 0; i < 10; i++)
                                    tokenizer.nextToken();
                                if ("F".equals(tokenizer.nextToken().toUpperCase()))
                                    continue;
                                else {
                                    tokenizer = new StringTokenizer(reportRecord, "|");
                                    s = tokenizer.nextToken();
                                    TerminalType terminalType = ShetabReportConstants.shetabTermTypeToTerminalType.get(s.substring(0, 3));
                                    String trnType = s.substring(3, 5);
                                    if ("WD".equals(trnType))
                                        trnType = "W";
                                    if ("PU".equals(trnType))
                                        trnType = "P";
                                    if (record.terminalType.equals(terminalType) && recordTrnType.equals(trnType) && "C".equals(s.substring(5))) {
                                        tokenizer.nextToken(); // number of
                                        // acqCreditTrx
                                        // if(record.amount_acq_credit_3 != 0)
                                        String m = tokenizer.nextToken();
                                        record.amount_acq_credit_3 += Long.valueOf(m);

                                        tokenizer.nextToken(); // number of
                                        // acqDebitTrx
                                        // if(record.amount_acq_debit_5 != 0)
                                        m = tokenizer.nextToken();
                                        record.amount_acq_debit_5 += Long.valueOf(m);

                                        tokenizer.nextToken(); // number of
                                        // issCreditTrx
                                        // if(record.amount_iss_credit_7 != 0)
                                        m = tokenizer.nextToken();
                                        record.amount_iss_credit_7 += Long.valueOf(m);

                                        tokenizer.nextToken(); // number of
                                        // issDebitTrx
                                        // if(record.amount_iss_debit_9 != 0)
                                        m = tokenizer.nextToken();
                                        record.amount_iss_debit_9 += Long.valueOf(m);
                                    }
                                }
                            }
                        }
                    }
                }
                records.add(record);
            }
            return records;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return null;
    }

    public static String generateReport13File(List<TotalShetabRepRecord> records, boolean isPSP) {
        StringBuilder report13th_iss = new StringBuilder();
        StringBuilder report13th_acq = new StringBuilder();
        StringBuilder report13th = new StringBuilder();
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMddHHmmss");
        for (TotalShetabRepRecord record : records) {
            String ifxType = ShetabReportConstants.ifxTypeToShetabTrnType.get(record.ifxType);
            if ("W".equals(ifxType))
                ifxType = "WD";
            if ("P".equals(ifxType))
                ifxType = "PU";
            if (record.amount_acq_debit_5 == 0 && record.amount_iss_credit_7 == 0) {
                if (record.amount_iss_debit_9 != 0) {
                    report13th_iss.append("1").append("/");
                    report13th_iss.append(ifxType).append("/");
                    report13th_iss.append(ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType)).append("/");
                    report13th_iss.append(StringFormat.formatNew(15, StringFormat.JUST_RIGHT, record.amount_iss_debit_9, '0')).append("/");
                    report13th_iss.append("C"); //az nazare shetab ma debit hastim vali ma dar file khodemoon miguim shetab credit ast ---> pas C migozarim
                    report13th_iss.append("\r\n");
                }
                /**************************************************************************************************************************/
                if (record.amount_acq_credit_3 != 0) {
                    report13th_acq.append("2").append("/");
                    report13th_acq.append(ifxType).append("/");
                    report13th_acq.append(ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType)).append("/");
                    report13th_acq.append(StringFormat.formatNew(15, StringFormat.JUST_RIGHT, record.amount_acq_credit_3, '0')).append("/");
                    report13th_acq.append("D");    //tozih mese tozihe morede foq ast
                    report13th_acq.append("\r\n");
                }
            } else if (record.amount_acq_credit_3 == 0 && record.amount_iss_debit_9 == 0) {
                if (record.amount_iss_credit_7 != 0) {
                    report13th_iss.append("1").append("/");
                    report13th_iss.append(ifxType).append("/");
                    report13th_iss.append(ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType)).append("/");
                    report13th_iss.append(StringFormat.formatNew(15, StringFormat.JUST_RIGHT, record.amount_iss_credit_7, '0')).append("/");
                    report13th_iss.append("D");
                    report13th_iss.append("\r\n");
                }
                /***************************************************************************************************************************/
                if (record.amount_acq_debit_5 != 0) {
                    report13th_acq.append("2").append("/");
                    report13th_acq.append(ifxType).append("/");
                    report13th_acq.append(ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType)).append("/");
                    report13th_acq.append(StringFormat.formatNew(15, StringFormat.JUST_RIGHT, record.amount_acq_debit_5, '0')).append("/");
                    report13th_acq.append("C");    //tozih mese tozihe morede foq ast
                    report13th_acq.append("\r\n");
                }
            }
        }
        report13th.append(report13th_iss).append(report13th_acq);
        return report13th.toString();
    }

    /******************************************** Form13Extra disagreement service *******************************************************/
    /**
     * ******************************************** Form13Extra disagreement service ***************************************************
     */
    public static String compareExtraForm13(String pathSwitch,
                                            String pathShetab_rep_today, String pathShetab_rep_yesterday, String path) {
        StringTokenizer tokenizer = new StringTokenizer(pathShetab_rep_today,
                "/");
        //String path = "";
       /* for (int i = 0; i < 4; i++)
            if (i == 3)
                path += tokenizer.nextToken();
            else
                path += tokenizer.nextToken() + "/";*/
        String pathShetab_rep_new = reconcileShetabForm13Extra(pathShetab_rep_today,
                pathShetab_rep_yesterday, path);
        if (pathShetab_rep_new != null) {
            String path_result = compareFiles(pathShetab_rep_new, pathSwitch,
                    path, false, true);
            return path_result;
        } else {
            logger.debug("An Exception occure in producing shetab file from today and yesterday file");
            return null;
        }
    }

    private static String reconcileShetabForm13Extra(String pathShetab_rep_today, String pathShetab_rep_yesterday, String path) {
        final String bankName = "pas";
        String fileExt = "-rep-";

        GeneralDao.Instance.beginTransaction();
        GlobalContext.getInstance().startup();
        ProcessContext.get().init();
        GeneralDao.Instance.endTransaction();

        List<TotalShetabRepRecord> records_repbal_today = new ArrayList<TotalShetabRepRecord>();
        List<TotalShetabRepRecord> records_repExtra_today = new ArrayList<TotalShetabRepRecord>();
        List<TotalShetabRepRecord> records_repExtra_yesterday = new ArrayList<TotalShetabRepRecord>();
        List<TotalShetabRepRecord> records_repResult = new ArrayList<TotalShetabRepRecord>();
        /**************************** today :repBal,repExtra ******************************/
        try {
            File file_today = new File(pathShetab_rep_today);
            ZipFile zipFile_today = new ZipFile(file_today);
            String pathRes = path
                    + "/"
                    + file_today.getName().substring(0,
                    file_today.getName().length() - 4) + fileExt
                    + "report13eXTRAFromShetab.txt";
            logger.info("pathRes is " + pathRes);
            File pathResFile = new File(pathRes);
            if (!pathResFile.exists()){
                logger.info("pathResFile does not exist!");
                pathResFile.createNewFile();
            }
            BufferedWriter result = new BufferedWriter(new FileWriter(pathResFile));
            logger.info("result file with name : " + path + " has been created!");
            Enumeration<? extends ZipEntry> entries_today = zipFile_today
                    .entries();
            while (entries_today.hasMoreElements()) {
                ZipEntry entry = entries_today.nextElement();
                if (/* entry.getName().endsWith(".repbal_txt") && */!entry
                        .getName().toUpperCase().contains("ARZI")
                        && entry.getName().toUpperCase().contains("IRI")) {
                    logger.debug("Entry:" + entry.getName());
                    if (entry.getName().endsWith(".repbal_txt")) {
                        InputStream inputStream = zipFile_today
                                .getInputStream(entry);
                        records_repbal_today = extractAndParseTotalShetabRecord(
                                new BufferedReader(new InputStreamReader(
                                        inputStream)), false);
                    } else if (entry.getName().endsWith(".rep_extra_txt")) {
                        InputStream inputStream = zipFile_today
                                .getInputStream(entry);
                        records_repExtra_today = extractAndParseTotalShetabRecord(
                                new BufferedReader(new InputStreamReader(
                                        inputStream)), false);
                    } else
                        continue;
                }
            }
            zipFile_today.close();
            /***************** yesterday : repExtra *****************/
            File file_yesterday = new File(pathShetab_rep_yesterday);
            ZipFile zipFile_yesterday = new ZipFile(file_yesterday);
            Enumeration<? extends ZipEntry> entries_yesterday = zipFile_yesterday
                    .entries();
            while (entries_yesterday.hasMoreElements()) {
                ZipEntry entry = entries_yesterday.nextElement();
                if (/* entry.getName().endsWith(".repbal_txt") && */!entry
                        .getName().toUpperCase().contains("ARZI")
                        && entry.getName().toUpperCase().contains("IRI")) {
                    if (entry.getName().endsWith(".rep_extra_txt")) {
                        InputStream inputStream = zipFile_yesterday
                                .getInputStream(entry);
                        records_repExtra_yesterday = extractAndParseTotalShetabRecord(
                                new BufferedReader(new InputStreamReader(
                                        inputStream)), false);
                    } else
                        continue;
                }
            }
            zipFile_yesterday.close();
            /***************** combine today & yesterday file ****************/
            TotalShetabRepRecord resultRecord;
            if (records_repbal_today != null && records_repbal_today.size() > 0
                    && records_repExtra_today != null
                    && records_repExtra_today.size() > 0
                    && records_repExtra_yesterday != null
                    && records_repExtra_yesterday.size() > 0) {

                for (int i = 0; i < records_repbal_today.size(); i++) {
                    if (records_repbal_today.get(i) != null
                            && records_repExtra_today.get(i) != null
                            && records_repExtra_yesterday.get(i) != null) {

                        resultRecord = new TotalShetabRepRecord();
                        resultRecord.terminalType = records_repbal_today.get(i).terminalType;
                        resultRecord.ifxType = records_repbal_today.get(i).ifxType;
                        resultRecord.amount_acq_credit_3 = records_repbal_today
                                .get(i).amount_acq_credit_3
                                + records_repExtra_yesterday.get(i).amount_acq_credit_3
                                - records_repExtra_today.get(i).amount_acq_credit_3;
                        resultRecord.amount_acq_debit_5 = records_repbal_today
                                .get(i).amount_acq_debit_5
                                + records_repExtra_yesterday.get(i).amount_acq_debit_5
                                - records_repExtra_today.get(i).amount_acq_debit_5;
                        resultRecord.amount_iss_credit_7 = records_repbal_today
                                .get(i).amount_iss_credit_7
                                + records_repExtra_yesterday.get(i).amount_iss_credit_7
                                - records_repExtra_today.get(i).amount_iss_credit_7;
                        resultRecord.amount_iss_debit_9 = records_repbal_today
                                .get(i).amount_iss_debit_9
                                + records_repExtra_yesterday.get(i).amount_iss_debit_9
                                - records_repExtra_today.get(i).amount_iss_debit_9;

                        records_repResult.add(resultRecord);
                    } else if (records_repbal_today.get(i) == null) {
                        logger
                                .debug("Today repBal file has less record than other files");
                        break;
                    } else if (records_repExtra_today == null) {
                        logger
                                .debug("Today repExtra fileis has less record than other file");
                        break;
                    } else if (records_repExtra_yesterday == null) {
                        logger
                                .debug("Yesterday repExtra file has less record than other file");
                        break;
                    }
                }

                result.append(generateReport13File(records_repResult, false));
                result.flush();
                result.close();
            }

            logger.debug("Processing file: " + file_today.getName() + " and "
                    + file_yesterday);
            // result.flush();
            result.close();

            return pathRes;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return null;
    }
    
    //29th format-this is NOT shetab report
    public static String generateReportNumber29(MonthDayDate workingDay) {
        int maxReportRecordSize = ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE);
        String report29th = "";
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd");
        PersianDateFormat timeFormatPers = new PersianDateFormat("HHmmss");


        for (int i = 0; ; i++) {
            List<SaderatReportRecord> records = null;//AccountingService.getSaderatReportRecord(workingDay, i * maxReportRecordSize, maxReportRecordSize);

            if (records == null || records.size() == 0)
                break;
            for (SaderatReportRecord record : records) {
                String trnType = ShetabReportConstants.TrnTypeForSaderat.get(record.trnType);
                //C1
//				report29th += record.trnSeqCntr + "|";
                report29th += StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, ' ');
                report29th += "|";
                //C2
//				report29th += record.OrgDt.toDate() + "|";
                Date origDt = record.OrgDt.toDate();
                report29th += dateFormatPers.format(origDt) + "|";
                //C3
                report29th += dateFormatPers.format(origDt) + "|";
                //C4
                report29th += "   |";
                //C5
                long origTime = record.OrgDt.getTime();
                report29th += timeFormatPers.format(origTime) + "|";
                //C6
                report29th += "0|";
                //C7
                report29th += "200|";
                //C8
                report29th += "210|";
                //C9
                report29th += "0|";

                //C10
                if (TrnType.PURCHASE.equals(record.trnType))
                    report29th += "                           0|";
                else if (TrnType.BILLPAYMENT.equals(record.trnType))
                    report29th += "                      170000|";

                //C11
                report29th += record.appPan + "|";

                //C12
                report29th += StringFormat.formatNew(15, StringFormat.JUST_RIGHT, record.amount, '0');
                report29th += "|";
                //C13
                report29th += "                            |";//hesabe moshtari(28 space)
                //C14
                report29th += "                            |";//hesabe pazirande(28 space)
                //C15
                report29th += trnType + "|";
                //C16
                report29th += "603769|";
                //C17
                report29th += "603769|";
                //C18
                report29th += dateFormatPers.format(origDt) + "|";
                //C19
                report29th += record.terminalType + "|";
                //C20
                report29th += StringFormat.formatNew(8, StringFormat.JUST_RIGHT, record.terminalId, ' ');/* record.terminalId + "|";*/
                report29th += "|";
                //C21
                report29th += "|";
                //C22
                report29th += StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.NetworkRefId, ' ');/*record.NetworkRefId + "|";*/

                //C23
                report29th += "|";

                report29th += "\r\n";
            }
        }
        return report29th.toString();
    }

    // 13th Format- SHETAB report


    public static Pair<String, ShetabReversalReportRecord> getListOfTransactionInShetabReversalReport(Transaction trx, Long myBin) {
        ShetabReversalReportRecord record = new ShetabReversalReportRecord();
        record.trx = trx;
        record.appPan = trx.getIncomingIfx().getAppPAN() != null ? trx.getIncomingIfx().getAppPAN() : "";
        Ifx incomingIfx = trx.getIncomingIfx();
        record.amount = incomingIfx.getTrx_Amt() != null ? incomingIfx.getTrx_Amt() : 0L;
        record.bankId = incomingIfx.getBankId();
        record.terminalId = incomingIfx.getTerminalId() != null ? incomingIfx.getTerminalId() : "";

        Long amount = 0L;
        if (record.bankId.equals(myBin))
            record.trnSeqCntr = trx.getSequenceCounter();
        //else record.trnSeqCntr = "";
        //record.persianDt = new DayDate();
        record.issueDateTime = DateTime.now();
        //record.user = PrincipalUtil.getCurrentUser();

        //if (recordDosnotExist(record)) {
        GeneralDao.Instance.saveOrUpdate(record);
        //SchedulerService.addReversalAndRepeatTrigger(trx.getInputMessage(), amount);

        //kiaei 92/03/25
        LifeCycle lifeCycle = trx.getAndLockLifeCycle(LockMode.UPGRADE);
        lifeCycle.setIsComplete(false);
        lifeCycle.setIsFullyReveresed(LifeCycleStatus.REQUEST);
        GeneralDao.Instance.saveOrUpdate(lifeCycle);

        SchedulerService.createReversalJobInfo(trx, ISOResponseCodes.APPROVED, amount);
        return new Pair("با موفقیت انجام شد", record);
/*else

    {
        return new Pair("رکورد تکراری", null);
    }*/

    }
    
    public static String reconcileByShetabReport(BufferedReader brShetabReport, boolean isIssuingMode, int reportType) throws Exception {
//		return reconcileByShetabReport(brShetabReport, isIssuingMode, reportType, GlobalContext.getInstance().getMyInstitution().getBin());
        return reconcileByShetabReport(brShetabReport, isIssuingMode, reportType, ProcessContext.get().getMyInstitution().getBin());
    }
    
    public static String reconcileByShetabReport(BufferedReader brShetabReport, boolean isIssuingMode, int reportType, Long myBIN) throws Exception {
        String errors = "";
        int numCleared = 0;
        int count = 0;

        DateTime maxDateTime = DateTime.MIN_DATE_TIME;

        String reportRecord;
        ShetabReportRecord record;

        GeneralDao.Instance.beginTransaction();

        while (brShetabReport.ready()) {
            if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                try {
                    reportRecord = reportRecord.trim();
                    if (reportRecord == null || reportRecord.equals(""))
                        return "No record found in the file...";
                    if (reportRecord.contains("---")) {
                        int indexOf = reportRecord.indexOf("---");
                        reportRecord = reportRecord.substring(indexOf + 3);
                    }
                    if (reportRecord.startsWith("BD:")) {
                        reportRecord = reportRecord.substring(3);
                    }
                    if (reportRecord.startsWith("NF:")) {
                        reportRecord = reportRecord.substring(3);
                    }

                    switch (reportType) {
                        case TRX:
                            record = ShetabDisagreementService.parseNormalRecord(reportRecord, isIssuingMode, myBIN);
                            break;
                        case Inq:
                            record = parseBalInqRecord(reportRecord, isIssuingMode, myBIN);
                            break;
                        default:
                            logger.error("Unknown report type...");
                            throw new Exception("Unknown report type...");
                    }

                    String clrCriteria = "";
                    if (isIssuingMode) {
                        if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
                            clrCriteria = "select trx.destinationClearingInfo ";
                        } else {
                            clrCriteria = "select trx.sourceClearingInfo ";
                        }
                    } else {
                        clrCriteria = "select trx.destinationClearingInfo ";
                    }

                    String bankCriteria = "";
                    if (isIssuingMode) {
                        if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
                            bankCriteria = " and m.networkTrnInfo.DestBankId = :destBankId ";
                        } else {
                            bankCriteria = " and m.networkTrnInfo.BankId = :destBankId ";
                        }
                    } else {
                        bankCriteria = " and m.networkTrnInfo.DestBankId = :destBankId ";
                    }

                    String panCriteria = "";
                    if (isIssuingMode) {
                        if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
                            panCriteria = " and m.eMVRqData.secondAppPan = :appPan ";
                        } else {
                            panCriteria = " and m.eMVRqData.CardAcctId.AppPAN = :appPan ";
                        }
                    } else {
                        if (!record.appPan.startsWith(myBIN.toString()) && record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) {
                            panCriteria = " and m.eMVRqData.secondAppPan = :appPan ";
                        } else {
                            panCriteria = " and m.eMVRqData.CardAcctId.AppPAN = :appPan ";
                        }
                    }


                    String queryString = "update ClearingInfo ci set ci.clearingState = :flag "
                            + "where ci in ( "
                            + clrCriteria
                            + " from Ifx m inner join m.transaction trx "
                            + " where "
                            + " m.ifxType = :type "
                            + " and m.networkTrnInfo.OrigDt = :origDt "
                            + " and m.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr "
                            + panCriteria
                            + bankCriteria
                            + " and m.networkTrnInfo.TerminalId = :terminalId "
                            + " and m.eMVRqData.Auth_Amt= :amount "
                            + " and m.networkTrnInfo.TerminalType = :termType) "
                            + " and ci.clearingState in (:clrState, :flag)";

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("flag", ClearingState.CLEARED);
                    params.put("type", record.type);
                    params.put("origDt", record.origDt);
                    params.put("trnSeqCntr", record.trnSeqCntr);
                    params.put("appPan", record.appPan);
                    params.put("destBankId", record.destBankId);
                    params.put("terminalId", record.terminalId);
                    params.put("amount", record.amount);
                    params.put("termType", record.terminalType);
                    params.put("clrState", ClearingState.NOT_CLEARED);

                    int numAffected;
                    //if (dataSource instanceof GeneralDao)
                    numAffected = GeneralDao.Instance.executeUpdate(queryString, params);
                    //else
                    //	numAffected = hibernateExecuteUpdate(((Session) dataSource), queryString, params);

                    if (numAffected == 0) {
                        logger.error("No transaction found for record: " + record.row + "---" + reportRecord);
                        errors += "NF:" + reportRecord + "\r\n";
                    } else if (numAffected == 1) {
//					logger.debug("A transaction is flagged as cleared: " + record.row);
                        numCleared++;
                    } else if (numAffected > 1) {
                        logger.error("More than one transaction found for record: " + record.row);
                        params.remove("flag");
                        params.put("flag", ClearingState.DISPUTE);
                        int numAffected2;
                        //if (dataSource instanceof GeneralDao)
                        numAffected2 = GeneralDao.Instance.executeUpdate(queryString, params);
                        //else
                        //	numAffected2 = hibernateExecuteUpdate((Session) dataSource, queryString, params);
                        logger.error(numAffected2 + " transactions are flaged as DISPUTE for record: " + record.row + "---" + reportRecord);
                        errors += "MR:" + reportRecord + "\r\n";
                    }
                    System.out.println("row:\t" + record.row);

                    if (maxDateTime.before(record.origDt))
                        maxDateTime = record.origDt;
                } catch (Exception e) {
                    logger.error(e, e);
                    errors += "BD:" + reportRecord + "\r\n";
                }

                count++;
                if (count % 100 == 0) {
                    GeneralDao.Instance.endTransaction();
                    GeneralDao.Instance.beginTransaction();
                    count = 0;
                }
            }
        }

        GeneralDao.Instance.endTransaction();
//		errors += findAllOtherTransactions(maxDateTime, isIssuingMode, reportType, dataSource, myBIN);

        if (errors == null || errors.length() == 0) {
            logger.debug("No ERRORS in reconciliation...");
        } else {
            logger.error("ERRORS: " + errors);
        }

//		errors += "\r\nNum of transactions flagged as cleared = " + numCleared+"\r\n";

        return errors;
    }

}
