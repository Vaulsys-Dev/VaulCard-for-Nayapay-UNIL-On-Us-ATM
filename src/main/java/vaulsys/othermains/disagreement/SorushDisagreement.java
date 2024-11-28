package vaulsys.othermains.disagreement;

import com.ghasemkiani.util.icu.PersianDateFormat;

import vaulsys.authorization.policy.Bank;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.report.SorushRecord;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.Pair;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

//TASK Task056
public class SorushDisagreement {

	private static Logger logger = Logger.getLogger(SorushDisagreement.class);

	private static final String _IRI = "iri";
	private static final String _ISS = ".iss";
	private static final String _PAS = "PAS";
	private static final String _PAS2REP = "PAS2rep";
	private static final String _PASREP = "PASrep";
	
    public static final String SHAPARAK_PRE_BIN = "581672";
    public static final String SHAPARAK_BIN = "581672000";
    public static final Long SORUSH_BIN = 936450L;

    public static final String BANK_NAME = "PAS";
    public static final String SHETAB_NAME = "pas2-";
    public static final String BANK_BIN = "502229";

    public static final Long shaparakId = 581672L;

    public static final Long ISSUER_SHETAB_TERMINAL_ID = 123L;//TASK Task056
    private static final String SHETAB_POSTFIX = "-shetab";
    private static final String BANK_POSTFIX = "-bank";
    private static Map<Integer, Long> GetBinByTwoDigit;

    static {
        GetBinByTwoDigit = new HashMap<Integer, Long>();
        GetBinByTwoDigit.put(61, 504706L);
        GetBinByTwoDigit.put(60, 606373L);
        GetBinByTwoDigit.put(16, 639217L);
        GetBinByTwoDigit.put(65, 636949L);
        GetBinByTwoDigit.put(93, 581672000L);
        GetBinByTwoDigit.put(78, 505809L);
        GetBinByTwoDigit.put(15, 589210L);
        GetBinByTwoDigit.put(13, 589463L);
        GetBinByTwoDigit.put(19, 603769L);
        GetBinByTwoDigit.put(16, 603770L);
        GetBinByTwoDigit.put(17, 603799L);
        GetBinByTwoDigit.put(12, 610433L);
        GetBinByTwoDigit.put(56, 621986L);
        GetBinByTwoDigit.put(54, 622106L);
        GetBinByTwoDigit.put(18, 627353L);
        GetBinByTwoDigit.put(55, 627412L);
        GetBinByTwoDigit.put(53, 627488L);
        GetBinByTwoDigit.put(20, 627648L);
        GetBinByTwoDigit.put(21, 627760L);
        GetBinByTwoDigit.put(11, 627961L);
        GetBinByTwoDigit.put(14, 628023L);
        GetBinByTwoDigit.put(57, 502229L);
        GetBinByTwoDigit.put(58, 639607L);
        GetBinByTwoDigit.put(59, 639346L);
        GetBinByTwoDigit.put(51, 628157L);
        GetBinByTwoDigit.put(62, 636214L);
        GetBinByTwoDigit.put(10, 636795L);
        GetBinByTwoDigit.put(52, 639599L);
        GetBinByTwoDigit.put(69, 505785L);
        GetBinByTwoDigit.put(50, 936450L);
        GetBinByTwoDigit.put(66, 502938L);
        GetBinByTwoDigit.put(22, 502908L);
        GetBinByTwoDigit.put(63, 627381L);
        GetBinByTwoDigit.put(64, 505416L);
        GetBinByTwoDigit.put(70, 504172L);
		GetBinByTwoDigit.put(75, 606256L);//موسسه اعتباری عسکریه
		GetBinByTwoDigit.put(73, 505801L);//موسسه اعتباری کوثر

    }

    public static void main(String[] args) {

        /*************************** parsing sorush file for getting workingDay *****************************/
        String path = "D:/pasargad/1/Data/sorosh/isspas920406-1092-16541.txt";
        String pathRes = path.substring(0, path.length() - 4) + "-res.txt";
        String pathError = path.substring(0, path.length() - 4) + "-Error.txt";
        String pathDuplicate = path.substring(0, path.length() - 4) + "-Duplicate.txt";
        File firstSorush = new File(path);
        Long myBin = 502229L/*myInstitution.getBin()*/;
        Pair<List<String>, File> result = null;
        try {
            result = addWorkingDayToSorushFile(new BufferedReader(new FileReader(firstSorush)), pathRes, pathError, pathDuplicate, myBin);
        } catch (Exception e) {
//				e.printStackTrace();
            logger.error(" Exception in addWorkingDayToSorushFile!!!!" + e);
        }
        /***************************** diagreement **********************/
        String zipPath = "D:/pasargad/1/Data/sorosh/New folder/real test.zip";
//			findDisagreement( zipPath , result.second);


    }

    public static Pair<List<String>, File> addWorkingDayToSorushFile(BufferedReader brSorush, String pathRes, String pathError, String pathDuplicate, Long myBin) throws Exception {
//		GlobalContext.getInstance().startup();
//		ProcessContext.get().init();
        GeneralDao.Instance.beginTransaction();
        try {
            File sorushRes = new File(pathRes);
            if (!sorushRes.exists()) {
                sorushRes.createNewFile();
            }
            BufferedWriter brRes = new BufferedWriter(new FileWriter(sorushRes));
            BufferedWriter brError = new BufferedWriter(new FileWriter(pathError));
            BufferedWriter brDuplicate = new BufferedWriter(new FileWriter(pathDuplicate));
            PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
            List<String> workingDays = new ArrayList<String>();
            List<SorushRecord> sorushRecords = new ArrayList<SorushRecord>();
            Long row = 0L;
            List<Ifx> result;
            sorushRecords = getSorushRecord(brSorush, false);
            row = 0L;
            Ifx ifx1;
            Ifx ifx2;
            for (SorushRecord record : sorushRecords) {
                row++;
                result = queryForSorushSettleDt(record, false);
                logger.info("after queryForSorushSettleDte, " + DateTime.now());
                logger.info("num: " + row);
                if (result.size() == 2) {
                    ifx1 = result.get(0);
                    ifx2 = result.get(1);
                    if (!IfxType.SORUSH_REV_REPEAT_RS.equals(ifx1.getIfxType()) && !IfxType.SORUSH_REV_REPEAT_RS.equals(ifx2.getIfxType()))
                        record.isDuplicate = true;
                    else {
                        //ifx1: sorush , ifx2: mainTrx
                        if (IfxType.SORUSH_REV_REPEAT_RS.equals(ifx1.getIfxType())) {
                            record.sorushTrnSeqCntr = ifx1.getSrc_TrnSeqCntr();
                            record.sorushPersianDt = dateFormatPers.format(ifx1.getOrigDt().getDayDate().toDate());
                            record.sorushWorkingDay = dateFormatPers.format(ifx1.getSettleDt().toDate());
                            record.workingDay = dateFormatPers.format(ifx2.getSettleDt().toDate());
                            record.trxType = ifx2.getTrnType().toString();
                            record.amount = ifx2.getAuth_Amt();
                        } else {
                            record.sorushTrnSeqCntr = ifx2.getSrc_TrnSeqCntr();
                            record.sorushPersianDt = dateFormatPers.format(ifx2.getOrigDt().getDayDate().toDate());
                            record.sorushWorkingDay = dateFormatPers.format(ifx2.getSettleDt().toDate());
                            record.workingDay = dateFormatPers.format(ifx1.getSettleDt().toDate());
                            record.trxType = ifx1.getTrnType().toString();
                            record.amount = ifx1.getAuth_Amt();
                        }
                    }
                } else if (result.size() > 2) {
                    record.isDuplicate = true;
                }
                ifx1 = null;
                ifx2 = null;
                result = null;
            }
            row = 0L;
            for (SorushRecord record : sorushRecords) {
                row++;
                if ((Util.hasText(record.workingDay) && Util.hasText(record.sorushWorkingDay)) || record.isDuplicate)
                    continue;
                result = queryForSorushSettleDt(record, true);
                logger.info("after queryForSorushSettleDte(Remain), " + DateTime.now());
                logger.info("num: " + row);
                if (result.size() == 2) {
                    ifx1 = result.get(0);
                    ifx2 = result.get(1);
                    if (!IfxType.SORUSH_REV_REPEAT_RS.equals(ifx1.getIfxType()) && !IfxType.SORUSH_REV_REPEAT_RS.equals(ifx2.getIfxType()))
                        record.isDuplicate = true;
                    else {
                        //ifx1: sorush , ifx2: mainTrx
                        if (IfxType.SORUSH_REV_REPEAT_RS.equals(ifx1.getIfxType())) {
                            record.sorushTrnSeqCntr = ifx1.getSrc_TrnSeqCntr();
                            record.sorushPersianDt = dateFormatPers.format(ifx1.getOrigDt().getDayDate().toDate());
                            record.sorushWorkingDay = dateFormatPers.format(ifx1.getSettleDt().toDate());
                            record.workingDay = dateFormatPers.format(ifx2.getSettleDt().toDate());
                            record.trxType = ifx2.getTrnType().toString();
                            record.amount = ifx2.getAuth_Amt();
                        } else {
                            record.sorushTrnSeqCntr = ifx2.getSrc_TrnSeqCntr();
                            record.sorushPersianDt = dateFormatPers.format(ifx2.getOrigDt().getDayDate().toDate());
                            record.sorushWorkingDay = dateFormatPers.format(ifx2.getSettleDt().toDate());
                            record.workingDay = dateFormatPers.format(ifx1.getSettleDt().toDate());
                            record.trxType = ifx1.getTrnType().toString();
                            record.amount = ifx1.getAuth_Amt();
                        }
                    }
                } else if (result.size() > 2) {
                    record.isDuplicate = true;
                } else if (result.size() == 1) {
                    ifx1 = result.get(0);
                    if (!IfxType.SORUSH_REV_REPEAT_RS.equals(ifx1.getIfxType())) {
                        record.workingDay = dateFormatPers.format(ifx1.getSettleDt().toDate());
                        record.trxType = ifx1.getTrnType().toString();
                        record.amount = ifx1.getAuth_Amt();
                    }
                }
                ifx1 = null;
                ifx2 = null;
                result = null;
            }
            for (SorushRecord record : sorushRecords) {
                if (record.isDuplicate) {
                    brDuplicate.append(record.data).append("\r\n");
                    continue;
                }
                if (record.workingDay != null && !workingDays.contains(record.workingDay))
                    workingDays.add(record.workingDay);
                if (record.sorushWorkingDay != null && !workingDays.contains(record.sorushWorkingDay))
                    workingDays.add(record.sorushWorkingDay);
                if (!Util.hasText(record.workingDay)/* || !Util.hasText(record.sorushWorkingDay)*/)
                    brError.append(record.data).append("\r\n");
                else {
                    if (!Util.hasText(record.sorushWorkingDay))
                        brRes.append(record.data).append("|").append(record.amount.toString()).append("|").append(record.trxType).append("|").append(record.workingDay).append("|").append("\r\n");
                    else
                        brRes.append(record.data).append("|").append(record.amount.toString()).append("|").append(record.trxType).append("|").append(record.workingDay).append("|").append(record.sorushPersianDt).append("|").append(record.sorushTrnSeqCntr).append("|").append(record.sorushWorkingDay).append("\r\n");
                }

            }

            brRes.flush();
            brError.flush();
            brDuplicate.flush();
/*
            List<String> days = new ArrayList<String>();
            for (String day : workingDays) {
                boolean exist = false;
                for (String newDay : days) {
                    if (newDay.equals(newDay)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist)
                    days.add(day);
            }
*/
            return new Pair<List<String>, File>(workingDays, sorushRes);
        } catch (Exception e) {
            logger.error("Exception in addWorkingDayToSorushFile!!!!" + e);
            //return null;
            throw new RuntimeException(e);
        } finally {
            GeneralDao.Instance.endTransaction();
        }
    }

    private static List<Ifx> queryForSorushSettleDt(SorushRecord record, boolean needMoreSearch) {
        logger.info("before queryForSorushSettleDt, " + DateTime.now());
        long t1 = System.currentTimeMillis();//temp
        DayDate lastDay = null;
        DayDate nextDay = null;
        if (needMoreSearch) {
            lastDay = record.trxDate.previousDay();
            nextDay = record.trxDate.nextDay();
        }
        String replaceQ = null;
        String replaceQ2 = null;
        String mainQ = " i.networkTrnInfo.BankId = :bankId ";
        String mainQ2 = " ref.BankId = :bankId ";
        if ((record.bankId.toString().length() == 9) && record.bankId.toString().startsWith(shaparakId.toString())) {
            replaceQ = String.format(" net.BankId between %1$s000 and %1$s999", shaparakId.toString());
            replaceQ2 = String.format(" ref.BankId between %1$s000 and %1$s999", shaparakId.toString());
        }

        String query1 = "select i"
                + " from Ifx i "
                + " inner join i.eMVRqData rq "
                + " inner join i.eMVRsData rs "
                + " inner join i.networkTrnInfo net "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
                + " and i.request = false "
                + " and i.ifxDirection = :direction"
                + " and i.ifxType in (107,3,54,55,56,58,13,101) "
                + " and i.receivedDtLong between :fromDate and :toDate "
                + "	and rq.CardAcctId.AppPAN = :appPan "
                + " and rs.RsCode = :success "
                + " and i.trnType in (2,13,40,46,47,11,10) " //me
                + " and i.endPointTerminalCode = :issuerTerminal "  //me
                + " and "
                + (replaceQ == null ? mainQ : replaceQ)
                + " and net.Src_TrnSeqCntr = :trnSeqCntr ";


        String query2 = "select i"
                + " from Ifx i "
                + " inner join i.eMVRqData rq "
                + " inner join i.eMVRsData rs "
                + " inner join i.networkTrnInfo net "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
                + " and i.request = false "
                + " and i.ifxDirection = :direction"
                + " and i.ifxType = :sorushIfx  "
                + "	and rq.CardAcctId.AppPAN = :appPan "
                + " and rs.RsCode = :success "
                + " and net.BankId = :soroushBankId "
                + " and i.originalDataElements in (select ref.id from MessageReferenceData ref where ref.TrnSeqCounter = :trnSeqCntr "
                + " and ref.OrigDt.dayDate.date between :origDateFrom and :origDateTo "
                + " and ref.OrigDt.dayTime.dayTime between :origTimeFrom and :origTimeTo"
                + " and ref.AppPAN = :appPan "
                + " and " + (replaceQ2 == null ? mainQ2 : replaceQ2)
                + ")";
        Map<String, Object> totalParams = new HashMap<String, Object>();
        Map<String, Object> params1 = new HashMap<String, Object>();
        Map<String, Object> params2 = new HashMap<String, Object>();

        totalParams.put("direction", IfxDirection.OUTGOING);
        totalParams.put("trnSeqCntr", Integer.valueOf(record.trnSeqCntr).toString());
        totalParams.put("appPan", record.appPan);
        totalParams.put("success", ISOResponseCodes.APPROVED);


        params1.putAll(totalParams);
        params2.putAll(totalParams);

        DateTime from = new DateTime(record.trxDate, new DayTime(0, 0, 0));
        DateTime to = new DateTime(record.trxDate, new DayTime(23, 59, 59));
        if (needMoreSearch) {
            from = new DateTime(lastDay, new DayTime(0, 0, 0));
            to = new DateTime(nextDay, new DayTime(23, 59, 59));
        }
        params1.put("fromDate", from.getDateTimeLong());
        params1.put("toDate", to.getDateTimeLong());
        params1.put("issuerTerminal", ISSUER_SHETAB_TERMINAL_ID);


        params2.put("sorushIfx", IfxType.SORUSH_REV_REPEAT_RS);
        params2.put("soroushBankId", 936450L);
        params2.put("origDateFrom", from.getDayDate().getDate());
        params2.put("origDateTo", to.getDayDate().getDate());
        params2.put("origTimeFrom", from.getDayTime().getDayTime());
        params2.put("origTimeTo", to.getDayTime().getDayTime());
        if (replaceQ == null)
            params1.put("bankId", record.bankId);

        if (replaceQ2 == null)
            params2.put("bankId", record.bankId);
        /********************************************************************/

//        String query = "select i from Ifx i "
//        		+ " where "
//        		+ " i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
//                + "	and i.eMVRqData.CardAcctId.AppPAN = :appPan "
//                + " and i.eMVRsData.RsCode = :success"
//                + " and i.request = false "
//                + " and i.ifxDirection = :direction"
//                + " and (("
//                + (replaceQ == null ? mainQ : replaceQ)
//                + " and i.ifxType not in " + IfxType.strRevRsOrdinals  + " and i.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr and i.receivedDtLong between :fromDate and :toDate)" ;
//        Map<String, Object> params = new HashMap<String, Object>();
//        DateTime from = new DateTime(record.trxDate, new DayTime(0, 0, 0));
//        DateTime to = new DateTime(record.trxDate, new DayTime(23, 59, 59));
//        if(needMoreSearch){
//        	from = new DateTime(lastDay, new DayTime(0, 0, 0));
//        	to = new DateTime(nextDay, new DayTime(23, 59, 59));
//        }
//        params.put("fromDate",from.getDateTimeLong());
//        params.put("toDate", to.getDateTimeLong());
//        params.put("trnSeqCntr", Integer.valueOf(record.trnSeqCntr).toString());
//        params.put("appPan", record.appPan);
//        params.put("success", ErrorCodes.APPROVED);
//        params.put("direction", IfxDirection.OUTGOING);
//        if (replaceQ == null)
//            params.put("bankId", record.bankId);
//        List<Ifx> ifxs = GeneralDao.Instance.find(query, params);
//        if(ifxs.size() == 1)
//        {
//        	Ifx ifx = ifxs.get(0);
//        	query = "select i"
//            	+ " from Ifx i "
//                + " where "
//                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
//                + "	and i.eMVRqData.CardAcctId.AppPAN = :appPan "
//                + " and i.eMVRsData.RsCode = :success"
//                + " and i.request = false "
//                + " and i.ifxDirection = :direction"
//                + " and i.ifxType = :sorushIfx  "
//                + " and i.networkTrnInfo.BankId = :soroushBankId "
//                + " and i.originalDataElements in (select ref.id from MessageReferenceData ref where ref.TrnSeqCounter = :trnSeqCntr "
//                + " and ref.OrigDt.dayDate.date :origDate"
//                + " and ref.OrigDt.dayTime.dayTime :origTime"
//                + " and ref.AppPAN = :appPan"
//                + " and " + (replaceQ2 == null ? mainQ2 : replaceQ2);
//        	params.put("sorushIfx", IfxType.SORUSH_REV_REPEAT_RS);
//            params.put("soroushBankId", 936450L);
//            params.put("origDate", ifx.getOrigDt().getDayDate());
//            params.put("origTime", ifx.getOrigDt().getDayTime());
//            if (replaceQ2 == null)
//                params.put("bankId", record.bankId);
//        }

        System.out.println("time 1 : " + String.valueOf((System.currentTimeMillis() - t1)));//temp
        t1 = System.currentTimeMillis();//temp
        List<Ifx> result = GeneralDao.Instance.find(query1, params1);
        System.out.println("query 1 : " + String.valueOf((System.currentTimeMillis() - t1)));//temp
        t1 = System.currentTimeMillis();//temp
        result.addAll(GeneralDao.Instance.find(query2, params2));
        System.out.println("query 2 : " + String.valueOf((System.currentTimeMillis() - t1)));//temp

        return result;
    }

    public static List<String> findDisagreement(Pair<List<String>, File> result, String zipPath) throws IOException {
        List<String> pathes = null;
        try {
            List<String> days = result.first;
            File sorush = result.second;
            logger.debug("zip path is : " + zipPath);
            PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
            /******************* initialize path of results ***********************/
            String resultPath = zipPath.substring(0, zipPath.lastIndexOf("/")) + "/";

            File folders = unZip(zipPath, zipPath.substring(0, zipPath.indexOf(".zip")));
            File file1 = new File(resultPath + "in8sh_in8b");
            logger.info("in8sh_in8b path : " + file1.getCanonicalPath());
            BufferedWriter in8sh_in8b = new BufferedWriter(new FileWriter(file1));
            logger.info("in8sh_in8b " + file1.exists());
            File file2 = new File(resultPath + "in8sh_notIn8b");
            BufferedWriter in8sh_notIn8b = new BufferedWriter(new FileWriter(file2));

            File file3 = new File(resultPath + "notIn8sh_in8b");
            BufferedWriter notIn8sh_in8b = new BufferedWriter(new FileWriter(file3));

            File file4 = new File(resultPath + "notIn8sh_notIn8b");
            BufferedWriter notIn8sh_notIn8b = new BufferedWriter(new FileWriter(file4));

            //TF
            File file5 = new File(resultPath + "in8sh_in8b_tf");
            BufferedWriter in8sh_in8b_tf = new BufferedWriter(new FileWriter(file5));
            File file6 = new File(resultPath + "in8sh_notIn8b_tf");
            BufferedWriter in8sh_notIn8b_tf = new BufferedWriter(new FileWriter(file6));
            File file7 = new File(resultPath + "notIn8sh_in8b_tf");
            BufferedWriter notIn8sh_in8b_tf = new BufferedWriter(new FileWriter(file7));
            File file8 = new File(resultPath + "notIn8sh_notIn8b_tf");
            BufferedWriter notIn8sh_notIn8b_tf = new BufferedWriter(new FileWriter(file8));

            //TT
            File file9 = new File(resultPath + "in8sh_in8b_tt");
            BufferedWriter in8sh_in8b_tt = new BufferedWriter(new FileWriter(file9));
            File file10 = new File(resultPath + "in8sh_notIn8b_tt");
            BufferedWriter in8sh_notIn8b_tt = new BufferedWriter(new FileWriter(file10));
            File file11 = new File(resultPath + "notIn8sh_in8b_tt");
            BufferedWriter notIn8sh_in8b_tt = new BufferedWriter(new FileWriter(file11));
            File file12 = new File(resultPath + "notIn8sh_notIn8b_tt");
            BufferedWriter notIn8sh_notIn8b_tt = new BufferedWriter(new FileWriter(file12));

            File file13 = new File(resultPath + "in8sh_in8b_sorush");
            BufferedWriter in8sh_in8b_sorush = new BufferedWriter(new FileWriter(file13));

            File file14 = new File(resultPath + "in8sh_notIn8b_sorush");
            BufferedWriter in8sh_notIn8b_sorush = new BufferedWriter(new FileWriter(file14));

            File file15 = new File(resultPath + "notIn8sh_in8b_sorush");
            BufferedWriter notIn8sh_in8b_sorush = new BufferedWriter(new FileWriter(file15));

            File file16 = new File(resultPath + "notIn8sh_notIn8b_sorush");
            BufferedWriter notIn8sh_notIn8b_sorush = new BufferedWriter(new FileWriter(file16));

            //TF

            File file = new File(resultPath + "in8sh_in8b_sorush_tf");
            BufferedWriter in8sh_in8b_sorush_tf = new BufferedWriter(new FileWriter(file));
            File file17 = new File(resultPath + "in8sh_notIn8b_sorush_tf");
            BufferedWriter in8sh_notIn8b_sorush_tf = new BufferedWriter(new FileWriter(file17));
            File file18 = new File(resultPath + "notIn8sh_in8b_sorush_tf");
            BufferedWriter notIn8sh_in8b_sorush_tf = new BufferedWriter(new FileWriter(file18));
            File file19 = new File(resultPath + "notIn8sh_notIn8b_sorush_tf");
            BufferedWriter notIn8sh_notIn8b_sorush_tf = new BufferedWriter(new FileWriter(file19));

            //TT
            File file20 = new File(resultPath + "in8sh_in8b_sorush_tt");
            BufferedWriter in8sh_in8b_sorush_tt = new BufferedWriter(new FileWriter(file20));
            File file21 = new File(resultPath + "in8sh_notIn8b_sorush_tt");
            BufferedWriter in8sh_notIn8b_sorush_tt = new BufferedWriter(new FileWriter(file21));
            File file22 = new File(resultPath + "notIn8sh_in8b_sorush_tt");
            BufferedWriter notIn8sh_in8b_sorush_tt = new BufferedWriter(new FileWriter(file22));
            File file23 = new File(resultPath + "notIn8sh_notIn8b_sorush_tt");
            BufferedWriter notIn8sh_notIn8b_sorush_tt = new BufferedWriter(new FileWriter(file23));

            pathes = Arrays.asList(resultPath + "in8sh_in8b",
                    resultPath + "in8sh_notIn8b",
                    resultPath + "notIn8sh_in8b",
                    resultPath + "notIn8sh_notIn8b",
                    resultPath + "in8sh_in8b_tf",
                    resultPath + "in8sh_notIn8b_tf",
                    resultPath + "notIn8sh_in8b_tf",
                    resultPath + "notIn8sh_notIn8b_tf",
                    resultPath + "in8sh_in8b_tt",
                    resultPath + "in8sh_notIn8b_tt",
                    resultPath + "notIn8sh_in8b_tt",
                    resultPath + "notIn8sh_notIn8b_tt",
                    resultPath + "in8sh_in8b_sorush",
                    resultPath + "in8sh_notIn8b_sorush",
                    resultPath + "notIn8sh_in8b_sorush",
                    resultPath + "notIn8sh_notIn8b_sorush",
                    resultPath + "in8sh_in8b_sorush_tf",
                    resultPath + "in8sh_notIn8b_sorush_tf",
                    resultPath + "notIn8sh_in8b_sorush_tf",
                    resultPath + "notIn8sh_notIn8b_sorush_tf",
                    resultPath + "in8sh_in8b_sorush_tt",
                    resultPath + "in8sh_notIn8b_sorush_tt",
                    resultPath + "notIn8sh_in8b_sorush_tt",
                    resultPath + "notIn8sh_notIn8b_sorush_tt");


            /******************************* get workingday from sorush file and map bufferedReaders to workingDay********************/
            List<SorushRecord> sorushRecords = getSorushRecord(new BufferedReader(new FileReader(sorush)), true);
            List<String> workingDays = new ArrayList<String>();
            for (SorushRecord record : sorushRecords) {
                if (!workingDays.contains(record.workingDay))
                    workingDays.add(record.workingDay);
            }

            /*********************** check workingDays from UI is complete ********************************/
            List<String> folderNameWorkingDays = new ArrayList<String>();
            for (final File folder : folders.listFiles()) {
                folderNameWorkingDays.add(folder.getName());
            }

            for (String workingDay1 : days) {
                if (!folderNameWorkingDays.contains(workingDay1))
                    throw new RuntimeException("not completed file for workingDays");
            }


            Map<String, BufferedReader> map = new HashMap<String, BufferedReader>();
            for (final File folder : folders.listFiles()) {
                for (String workingDay : workingDays) {
                    if (workingDay.equals(folder.getName())) {
                        readZipContasinForm8(folder, map, workingDay.substring(2));
                        break;
                    }
                }
            }
            /****************** get sorushFile, shetabFile, bankFile and find diffs ******************/
            List<String> sorushWorkingDays;
            Date date = null;
            String lastPersianWorkingDay = "";
            String nextPrsWorkingDay = "";
            List<SorushRecord> shetabRecords;
            List<SorushRecord> bankRecords;
            BufferedReader br;
            for (String workingDay : workingDays) {
                for (String name : map.keySet()) {
                    if (name.contains(workingDay)) {
                        if (name.contains(SHETAB_POSTFIX)) {
                            br = map.get(name);
                            map.put(name, br);
                            shetabRecords = getShetabRecordForSorush(br);
                            for (SorushRecord sorushRecord : sorushRecords) {
                                if (!workingDay.equals(sorushRecord.workingDay))
                                    continue;
                                date = dateFormatPers.parse(sorushRecord.persianDt);
                                lastPersianWorkingDay = dateFormatPers.format(new DayDate(date).previousDay().toDate());
                                nextPrsWorkingDay = dateFormatPers.format(new DayDate(date).nextDay().toDate());
                                for (SorushRecord shetabRecord : shetabRecords) {
                                    if (sorushRecord.trnSeqCntr.equals(shetabRecord.trnSeqCntr)
                                            && sorushRecord.appPan.equals(shetabRecord.appPan)
                                            && sorushRecord.bankId.equals(shetabRecord.bankId)
                                            && (sorushRecord.persianDt.equals(shetabRecord.persianDt) || lastPersianWorkingDay.equals(shetabRecord.persianDt) || nextPrsWorkingDay.equals(shetabRecord.persianDt))) {
                                        sorushRecord.in8Shetab = true;
                                        break;
                                    }
                                }
                            }
                        } else if (name.contains(BANK_POSTFIX)) {
                            br = map.get(name);
                            map.put(name, br);
                            bankRecords = getBankRecordForSorush(br);
                            for (SorushRecord sorushRecord : sorushRecords) {
                                if (!workingDay.equals(sorushRecord.workingDay))
                                    continue;
                                date = dateFormatPers.parse(sorushRecord.persianDt);
                                lastPersianWorkingDay = dateFormatPers.format(new DayDate(date).previousDay().toDate());
                                nextPrsWorkingDay = dateFormatPers.format(new DayDate(date).nextDay().toDate());
                                for (SorushRecord bankRecord : bankRecords) {
                                    if (sorushRecord.trnSeqCntr.equals(bankRecord.trnSeqCntr)
                                            && sorushRecord.appPan.equals(bankRecord.appPan)
                                            && sorushRecord.bankId.equals(bankRecord.bankId)
                                            && (sorushRecord.persianDt.equals(bankRecord.persianDt) || lastPersianWorkingDay.equals(bankRecord.persianDt) || nextPrsWorkingDay.equals(bankRecord.persianDt))) {
                                        sorushRecord.in8Bank = true;
                                        break;
                                    }
                                }
                            }
                            bankRecords = null;
                        }
                    }
                }
            }
            /******************** search for sorush transfer trx ***************/
            sorushWorkingDays = new ArrayList<String>();
            for (SorushRecord sorushRecord : sorushRecords) {
//				if(sorushRecord.workingDay.equals(workingDay)){
                if (sorushRecord.in8Shetab && sorushRecord.in8Bank) {
                    if (Util.hasText(sorushRecord.sorushWorkingDay)) {
                        if (!sorushWorkingDays.contains(sorushRecord.sorushWorkingDay))
                            sorushWorkingDays.add(sorushRecord.sorushWorkingDay);
                    }
                    /*else {
                                     String record = sorushRecord.persianDt + "|" + sorushRecord.trnSeqCntr + "|" + sorushRecord.appPan + "|" + sorushRecord.bankId + "|" + sorushRecord.workingDay;
                                     System.out.println("notIn8sh_notIn8b_sorush: " + record);
                                     notIn8sh_notIn8b_sorush.append(record).append("\r\n");
                                 }*/

                }
//				}
            }

            for (String sorushWorkingDay : sorushWorkingDays) {
//				boolean exist = false;
//				for(String name : map.keySet()){
//					if(name.contains(sorushWorkingDay)){
//						exist = true;
//						break;
//					}
//				}
//				if(!exist){
                for (final File folder : folders.listFiles()) {
                    if (sorushWorkingDay.equals(folder.getName())) {
                        readZipContasinForm8(folder, map, sorushWorkingDay.substring(2));
                        break;
                    }
                }
//				}
            }
            for (String sorushWorkingDay : sorushWorkingDays) {
                for (String name : map.keySet()) {
                    if (name.contains(sorushWorkingDay)) {
                        if (name.contains(SHETAB_POSTFIX)) {
                            shetabRecords = getShetabRecordForSorush(map.get(name));
                            for (SorushRecord sorushRecord : sorushRecords) {
                                if (!sorushWorkingDay.equals(sorushRecord.sorushWorkingDay) || !sorushRecord.in8Bank || !sorushRecord.in8Shetab)
                                    continue;
                                date = dateFormatPers.parse(sorushRecord.sorushPersianDt);
                                lastPersianWorkingDay = dateFormatPers.format(new DayDate(date).previousDay().toDate());
                                nextPrsWorkingDay = dateFormatPers.format(new DayDate(date).nextDay().toDate());

                                for (SorushRecord shetabRecord : shetabRecords) {
                                    if (sorushRecord.sorushTrnSeqCntr.equals(shetabRecord.trnSeqCntr)
                                            && sorushRecord.appPan.equals(shetabRecord.appPan)
                                            && shetabRecord.bankId.equals(SORUSH_BIN)
                                            && (sorushRecord.sorushPersianDt.equals(shetabRecord.persianDt) || lastPersianWorkingDay.equals(shetabRecord.persianDt) || nextPrsWorkingDay.equals(shetabRecord.persianDt))) {
                                        sorushRecord.sorushIn8Shetab = true;
                                        break;
                                    }
                                }
                            }
                            shetabRecords = null;
                        } else if (name.contains(BANK_POSTFIX)) {
                            bankRecords = getBankRecordForSorush(map.get(name));
                            for (SorushRecord sorushRecord : sorushRecords) {
                                if (!sorushWorkingDay.equals(sorushRecord.sorushWorkingDay) || !sorushRecord.in8Bank || !sorushRecord.in8Shetab)
                                    continue;
                                date = dateFormatPers.parse(sorushRecord.sorushPersianDt);
                                lastPersianWorkingDay = dateFormatPers.format(new DayDate(date).previousDay().toDate());
                                nextPrsWorkingDay = dateFormatPers.format(new DayDate(date).nextDay().toDate());

                                for (SorushRecord bankRecord : bankRecords) {
                                    if (sorushRecord.sorushTrnSeqCntr.equals(bankRecord.trnSeqCntr)
                                            && sorushRecord.appPan.equals(bankRecord.appPan)
                                            && bankRecord.bankId.equals(SORUSH_BIN)
                                            && (sorushRecord.sorushPersianDt.equals(bankRecord.persianDt) || lastPersianWorkingDay.equals(bankRecord.persianDt) || nextPrsWorkingDay.equals(bankRecord.persianDt))) {
                                        sorushRecord.soroushIn8Bank = true;
                                        break;
                                    }
                                }
                            }
                            bankRecords = null;
                        }
                    }
                }
            }
            /************************* Write middle result to files ****************************/
            for (SorushRecord sorushRecord : sorushRecords) {
                String record = sorushRecord.persianDt + "|" + sorushRecord.trnSeqCntr + "|" + sorushRecord.appPan + "|" + sorushRecord.bankId + "|" + sorushRecord.workingDay
                        + "||" + sorushRecord.sorushPersianDt + "|" + sorushRecord.sorushTrnSeqCntr + "|" + sorushRecord.sorushWorkingDay + "|" + sorushRecord.trxType + "|" + sorushRecord.amount.toString();
                if (!sorushRecord.in8Shetab && !sorushRecord.in8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_notIn8b: " + record);
                        notIn8sh_notIn8b.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_notIn8b_tf: " + record);
                        notIn8sh_notIn8b_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_notIn8b_tt: " + record);
                        notIn8sh_notIn8b_tt.append(record).append("\r\n");
                    }
                } else if (!sorushRecord.in8Shetab && sorushRecord.in8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_in8b: " + record);
                        notIn8sh_in8b.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_in8b_tf: " + record);
                        notIn8sh_in8b_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_in8b_tt: " + record);
                        notIn8sh_in8b_tt.append(record).append("\r\n");
                    }

                } else if (sorushRecord.in8Shetab && !sorushRecord.in8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_notIn8b: " + record);
                        in8sh_notIn8b.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_notIn8b_tf: " + record);
                        in8sh_notIn8b_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_notIn8b_tt: " + record);
                        in8sh_notIn8b_tt.append(record).append("\r\n");
                    }
                } else if (sorushRecord.in8Shetab && sorushRecord.in8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_in8b: " + record);
                        in8sh_in8b.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_in8b_tf: " + record);
                        in8sh_in8b_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_in8b_tt: " + record);
                        in8sh_in8b_tt.append(record).append("\r\n");
                    }
                }
            }

            /***************** get sorush record that need to check for next day ******************/
            for (SorushRecord sorushRecord : sorushRecords) {
                if (!sorushRecord.in8Bank || !sorushRecord.in8Shetab)
                    continue;
                String record = sorushRecord.persianDt + "|" + sorushRecord.trnSeqCntr + "|" + sorushRecord.appPan + "|" + sorushRecord.bankId + "|" + sorushRecord.workingDay
                        + "||" + sorushRecord.sorushPersianDt + "|" + sorushRecord.sorushTrnSeqCntr + "|" + sorushRecord.sorushWorkingDay + "|" + sorushRecord.trxType + "|" + sorushRecord.amount;
                if (sorushRecord.sorushIn8Shetab && sorushRecord.soroushIn8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_in8b_sorush: " + record);
                        in8sh_in8b_sorush.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_in8b_sorush_tf: " + record);
                        in8sh_in8b_sorush_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_in8b_sorush_tt: " + record);
                        in8sh_in8b_sorush_tt.append(record).append("\r\n");
                    }
                }
                if (sorushRecord.sorushIn8Shetab && !sorushRecord.soroushIn8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_notIn8b_sorush: " + record);
                        in8sh_notIn8b_sorush.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_notIn8b_sorush_tf: " + record);
                        in8sh_notIn8b_sorush_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("in8sh_notIn8b_sorush_tt: " + record);
                        in8sh_notIn8b_sorush_tt.append(record).append("\r\n");
                    }
                }
                if (!sorushRecord.sorushIn8Shetab && sorushRecord.soroushIn8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_in8b_sorush: " + record);
                        notIn8sh_in8b_sorush.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_in8b_sorush_tf: " + record);
                        notIn8sh_in8b_sorush_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_in8b_sorush_tt: " + record);
                        notIn8sh_in8b_sorush_tt.append(record).append("\r\n");
                    }
                }
                if (!sorushRecord.sorushIn8Shetab && !sorushRecord.soroushIn8Bank) {
                    if (!TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType) && !TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_notIn8b_sorush: " + record);
                        notIn8sh_notIn8b_sorush.append(record).append("\r\n");
                    } else if (TrnType.DECREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_notIn8b_sorush_tf: " + record);
                        notIn8sh_notIn8b_sorush_tf.append(record).append("\r\n");
                    } else if (TrnType.INCREMENTALTRANSFER.toString().equals(sorushRecord.trxType)) {
                        System.out.println("notIn8sh_notIn8b_sorush_tt: " + record);
                        notIn8sh_notIn8b_sorush_tt.append(record).append("\r\n");
                    }
                }
            }
            notIn8sh_notIn8b.close();
            notIn8sh_in8b.close();
            in8sh_notIn8b.close();
            in8sh_in8b.close();

            notIn8sh_notIn8b_tf.close();
            notIn8sh_in8b_tf.close();
            in8sh_notIn8b_tf.close();
            in8sh_in8b_tf.close();

            notIn8sh_notIn8b_tt.close();
            notIn8sh_in8b_tt.close();
            in8sh_notIn8b_tt.close();
            in8sh_in8b_tt.close();

            notIn8sh_in8b_sorush.flush();
            notIn8sh_notIn8b_sorush.flush();
            in8sh_in8b_sorush.flush();
            in8sh_notIn8b_sorush.flush();


            notIn8sh_in8b_sorush_tf.flush();
            notIn8sh_notIn8b_sorush_tf.flush();
            in8sh_in8b_sorush_tf.flush();
            in8sh_notIn8b_sorush_tf.flush();

            notIn8sh_in8b_sorush_tt.flush();
            notIn8sh_notIn8b_sorush_tt.flush();
            in8sh_in8b_sorush_tt.flush();
            in8sh_notIn8b_sorush_tt.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathes;
    }

    public static List<String> getWorkingDayFromSorushFile(File file, List<SorushRecord> records) {
        List<String> workingDays = new ArrayList<String>();
        String line;
        SorushRecord record = new SorushRecord();
        StringTokenizer tokenizer;
        String date = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.ready()) {
                if ((line = br.readLine()).length() > 0) {
                    record = new SorushRecord();
                    tokenizer = new StringTokenizer(line, "|");
                    date = tokenizer.nextToken().trim();
                    record.persianDt = "13" + date;
                    record.trnSeqCntr = tokenizer.nextToken().trim();
                    record.appPan = tokenizer.nextToken().trim();
                    record.amount = Long.valueOf(tokenizer.nextToken().trim());
                    record.bankId = Long.valueOf(tokenizer.nextToken().trim());
                    tokenizer.nextToken().trim();
                    tokenizer.nextToken().trim();
                    tokenizer.nextToken().trim();
                    tokenizer.nextToken().trim();
                    tokenizer.nextToken().trim();
                    if (tokenizer.hasMoreTokens()) {
                        record.workingDay = tokenizer.nextToken().trim();
                        if (!workingDays.contains(record.workingDay.toString()))
                            workingDays.add(record.workingDay.toString());
                    }
                }
                records.add(record);
            }
            return workingDays;
        } catch (Exception e) {
            logger.error("Exception in getWorkingDayFromSorushFile!!!!" + e);
            return workingDays;
        }
    }

    public static List<String> readZipContasinForm8(File folder, Map<String, BufferedReader> map, String password) throws IOException {

        List<String> brNames = new ArrayList<String>();
        for (File file : folder.listFiles()) {
            if (file.getName().contains(".zip")) {
                if (file.getName().contains(SHETAB_NAME)) {
                    ZipFile shetabZip = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = shetabZip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
						//System.out.println(entry.getName().toLowerCase());
						if(entry.getName().toLowerCase().endsWith(_ISS) && entry.getName().toLowerCase().contains(_IRI)){
							//System.out.println("**** "+entry.getName().toLowerCase());
                            brNames.add(folder.getName() + SHETAB_POSTFIX + entry.getName());
                            map.put(folder.getName() + SHETAB_POSTFIX + entry.getName(), new BufferedReader(new InputStreamReader(shetabZip.getInputStream(entry))));
                        }
                    }
                } else if (file.getName().contains(BANK_NAME)) {
                    net.lingala.zip4j.core.ZipFile bankZip;
                    try {
                        bankZip = new net.lingala.zip4j.core.ZipFile(file);
                        List fileHeaderList = bankZip.getFileHeaders();
                        for (int i = 0; i < fileHeaderList.size(); i++) {
                            FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                            if (fileHeader.getFileName().startsWith(_PAS) && !fileHeader.getFileName().startsWith(_PASREP) && !fileHeader.getFileName().startsWith(_PAS2REP)) {
                                if (bankZip.isEncrypted()) {
//					            	String pasword = Reverse(fileHeader.getFileName());
//					            	password = Reverse(password.substring(4,10));
//					            	bankZip.setPassword(fileHeader.getFileName().substring(3,9));
					            	bankZip.setPassword(password);
                                }
                                brNames.add(folder.getName() + BANK_POSTFIX + fileHeader.getFileName());
                                map.put(folder.getName() + BANK_POSTFIX + fileHeader.getFileName(), new BufferedReader(new InputStreamReader(bankZip.getInputStream(fileHeader)/*inputStream*/)));
                            }
                        }
                    } catch (ZipException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return brNames;
    }

    public static File unZip(String zipFile, String outputFolder) {
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(new File(zipFile)));
            ZipEntry entry;
            String name, dir;
            File outDir = new File(outputFolder);
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                if (entry.isDirectory()) {
                    mkdirs(outDir, name);
                    continue;
                }
                dir = dirpart(name);
                if (dir != null)
                    mkdirs(outDir, dir);
                extractFile(zin, outDir, name);
            }
            zin.close();
            return outDir;
        } catch (IOException e) {
            logger.error("Exception in unZip!!!!" + e);
            //return null;
            throw new RuntimeException(e);
        }
    }

    public static List<SorushRecord> getSorushRecord(BufferedReader br, boolean isDisagreement) {
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String line = "";
        SorushRecord record = null;
        List<SorushRecord> result = new ArrayList<SorushRecord>();
        StringTokenizer tokenizer;
        Long row = 0L;
        try {
            while (br.ready()) {
                if ((line = br.readLine()).length() > 0) {
                    row++;
                    logger.info("sorush" + row + ": " + line);
                    record = new SorushRecord();
                    tokenizer = new StringTokenizer(line, "|");
                    record.persianDt = "13" + tokenizer.nextToken().trim();
                    Date date = dateFormatPers.parse(record.persianDt);
                    record.trxDate = new DayDate(date);
                    record.trnSeqCntr = tokenizer.nextToken().trim();
                    record.appPan = tokenizer.nextToken().trim();
                    tokenizer.nextToken();
                    record.bankId = Long.valueOf(tokenizer.nextToken().trim());
                    for (int i = 0; i < 3; i++)
                        tokenizer.nextToken();
                    if ("I".equals(tokenizer.nextToken()))
                        continue;
                    tokenizer.nextToken();
                    if (isDisagreement) {
                        if (tokenizer.hasMoreTokens()) {
                            record.amount = Long.valueOf(tokenizer.nextToken().trim()); //me
                            record.trxType = tokenizer.nextToken().trim();//me
                            record.workingDay = tokenizer.nextToken().trim();
                            if (tokenizer.hasMoreTokens()) {
                                record.sorushPersianDt = tokenizer.nextToken().trim();
                                record.sorushTrnSeqCntr = tokenizer.nextToken().trim();
                                record.sorushWorkingDay = tokenizer.nextToken().trim();
                            }
                            result.add(record);
                        }
                    } else {
                        record.data = line;
                        result.add(record);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Exception in getSorushRecord!!!!" + e);
            //return null;
            throw new RuntimeException(e);
        }
    }

    public static List<SorushRecord> getShetabRecordForSorush(BufferedReader br) {
        String line = "";
        SorushRecord record = null;
        List<SorushRecord> result = new ArrayList<SorushRecord>();
        StringTokenizer tokenizer;
        Long row = 0L;
        try {
            while (br.ready()) {
                if ((line = br.readLine()).length() > 0) {
                    row++;
                    if (row % 1000 == 0)
                        logger.info("shetab row: " + +row);
                    record = new SorushRecord();
                    tokenizer = new StringTokenizer(line, "|");
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    record.persianDt = tokenizer.nextToken().replaceAll("/", "").trim();
                    tokenizer.nextToken();
                    record.trnSeqCntr = tokenizer.nextToken().trim();
                    record.appPan = tokenizer.nextToken().trim();
                    String bankId = tokenizer.nextToken().trim();
                    if (bankId.startsWith(SHAPARAK_PRE_BIN))
                        bankId = SHAPARAK_BIN;
                    record.bankId = Long.valueOf(bankId);
                }
                result.add(record);
            }
            return result;
        } catch (Exception e) {
            logger.error("Exception in getShetabRecordForSorush !!!!" + e);
            //return null;
            throw new RuntimeException(e);
        }
    }

    public static List<SorushRecord> getBankRecordForSorush(BufferedReader br) {
        String line = "";
        SorushRecord record = null;
        List<SorushRecord> result = new ArrayList<SorushRecord>();
        StringTokenizer tokenizer;
        Long row = 0L;
        try {
            while (br.ready()) {
                if ((line = br.readLine()).length() > 0) {
                    row++;
                    if (row % 1000 == 0)
                        logger.info("bank row: " + row);
                    record = new SorushRecord();
                    tokenizer = new StringTokenizer(line, "/");
                    record.persianDt = "13" + tokenizer.nextToken().trim().substring(0, 6);
                    for (int i = 0; i < 12; i++)
                        tokenizer.nextToken();
                    record.trnSeqCntr = tokenizer.nextToken().trim();
                    tokenizer.nextToken();
                    Integer bankCode = Integer.valueOf(tokenizer.nextToken().trim());
                    record.bankId = GetBinByTwoDigit.get(bankCode);
                    tokenizer.nextToken();
                    String appPan = tokenizer.nextToken().trim();
                    if (appPan.contains(BANK_BIN))
                        record.appPan = appPan;
                    else {
                        appPan = tokenizer.nextToken().trim();
                        if (appPan.contains(BANK_BIN))
                            record.appPan = appPan;
                        else
                            record.appPan = tokenizer.nextToken().trim();
                    }
                }
                result.add(record);
            }
            return result;
        } catch (Exception e) {
            logger.error(" Exception in getBankRecordForSorush!!!!" + e);
            //return null;
            throw new RuntimeException(e);
        }
    }

    public static Long getBinByTwoDigit(Integer twoDigitCode) {
        Map<Integer, Bank> banks = GlobalContext.getInstance().getAllBanks();
        return Long.valueOf(banks.get(twoDigitCode).getBin());
    }

    private static void extractFile(ZipInputStream in, File outdir, String name) throws IOException {
        byte[] buffer = new byte[1024];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
        int count = -1;
        while ((count = in.read(buffer)) != -1)
            out.write(buffer, 0, count);
        out.close();
    }

    private static void mkdirs(File outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists())
            d.mkdirs();
    }

    private static String dirpart(String name) {
        int s = name.lastIndexOf(File.separatorChar);
        return s == -1 ? null : name.substring(0, s);
    }
}
