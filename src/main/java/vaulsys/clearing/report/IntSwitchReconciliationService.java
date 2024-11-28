package vaulsys.clearing.report;

import vaulsys.authorization.policy.Bank;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.Institution;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.migration.MigrationData;
import vaulsys.migration.MigrationDataService;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.scheduler.TransferSorushTableJobInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transfermanual.BeanDataTransfer;
import vaulsys.transfermanual.BeanDataTransferSorush;
import vaulsys.transfermanual.BeanLogSorushTrx;
import vaulsys.transfermanual.BeanSorushNotTrn;
import vaulsys.transfermanual.TransferManual;
import vaulsys.user.User;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Pair;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ghasemkiani.util.icu.PersianDateFormat;
import com.ibm.icu.util.StringTokenizer;

public class IntSwitchReconciliationService {
    private static Logger logger = Logger.getLogger(IntSwitchReconciliationService.class);

    public static String[] getShetabReconcilationFiles(Long myBin) {
        String FILES[] = {

                "IRI/" + myBin + ".Acq",
                "IRI/" + myBin + ".Acq.Inq",
                "IRI/" + myBin + ".Iss",
                "IRI/" + myBin + ".Iss.Inq"};
        return FILES;
    }

    public static final int TRX_TYPES[] = {
            ShetabReconciliationService.TRX,
            ShetabReconciliationService.Inq,
            ShetabReconciliationService.TRX,
            ShetabReconciliationService.Inq
    };

    public static final boolean IS_ISSUER[] = {
            false,
            false,
            true,
            true
    };


    public static final String REVERSAL_FILES[] = {
            "isspas2"
    };

    public static final String REVERSAL_RECORDS_TYPES[] = {
            "nn",
    };

    public static final boolean REVERSAL_IS_ISSUER[] = {
            true
    };


    public static final int TRX = 1;
    public static final int Inq = 2;
    public static final int Aut = 3;
    public static final int Rev = 4;
    public static final Long shaparakId = 581672L;


    // reportType indicates the type of input report;
    // o reportType = 1 means this is *.Acq or *.Iss report
    // o reportType = 2 means this is *.Acq.Inq or *.Iss.Inq report
    // o reportType = 3 means this is *.Acq.Aut or *.Iss.Aut report
    // o reportType = 4 means this is *.Acq.Rev or *.Iss.Rev report
    // 9th Format- SHETAB report
    //------------Moosavi : Task111686 : Add new Form 9 to be compatible to Shetab7------------
    // ---------- add change to this method : insterad of generating one report now it create two reports9 (old&new) and retur the pair of these two----------------
    public static Pair<String, String> generateAcquirerShetabReport(String instBin, MonthDayDate workingDay, DateTime cutoffFrom, DateTime cutoffTo, Long inDestBankId, Long notInDestBankId) {
        int maxReportRecordSize = ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE);
		Pair<String, String> report9thOldNewPair =new Pair<String, String>(null, null);
		StringBuilder report9thOld = new StringBuilder();
		StringBuilder report9thNew = new StringBuilder();

        long t1, t2, t3;

        int row = 0;

        DateTime dateFrom = new DateTime(new DayDate(cutoffFrom.getDayDate().getYear(), cutoffFrom.getDayDate().getMonth(), cutoffFrom.getDayDate().getDay()),
                new DayTime(cutoffFrom.getDayTime().getHour(), cutoffFrom.getDayTime().getMinute(), cutoffFrom.getDayTime().getSecond() + 1));

        DateTime dateTo = new DateTime(new DayDate(cutoffFrom.getDayDate().getYear(), cutoffFrom.getDayDate().getMonth(), cutoffFrom.getDayDate().getDay()),
                new DayTime(cutoffFrom.getDayTime().getHour(), cutoffFrom.getDayTime().getMinute(), cutoffFrom.getDayTime().getSecond()));
        int iterateHour = 25;
        boolean generateTotal = false;
        if (generateTotal)
            iterateHour = 0;

        for (int j= 0; j <= iterateHour; j++) {
            logger.debug("Time: " + j);
            if (generateTotal) {
                dateFrom = cutoffFrom;
                dateTo = cutoffTo;
            } else {
                dateTo.increase(60);

            }

            logger.debug("from: " + dateFrom);
            logger.debug("to: " + dateTo);


            for (int i = 0; ; i++) {
                t1 = System.currentTimeMillis();
                logger.debug("Iteration: " + i);

                List<AcqShetabReportRecord> records = AccountingService.getAcqShetabReportRecords(instBin, workingDay, dateFrom, dateTo,
                        inDestBankId, notInDestBankId, i * maxReportRecordSize, maxReportRecordSize);

                t2 = System.currentTimeMillis();
                logger.debug("Query time: " + (t2 - t1));


                if (records == null || records.size() == 0)
                    break;

                for (AcqShetabReportRecord record : records) {
                    String trnType = ShetabReportConstants.TrnTypeToAcqReportType.get(record.trnType);
                    if (trnType != null) {
                    	
                    	 row++;
                         report9thOld.append(generateAcquirerShetabReportOld(record,row));
                         report9thNew.append(generateAcquirerShetabReportNew(record,row));
                        
                    }
                }
                t3 = System.currentTimeMillis();
                logger.debug("Report generation time: " + (t3 - t2));
            }
            dateFrom.increase(60);
        }
        report9thOldNewPair.first= report9thOld.toString();
        report9thOldNewPair.second= report9thNew.toString(); 
        
		return report9thOldNewPair;
    }
    
  //------------Moosavi : Task111686 : Add new Form 9 to be compatible to Shetab7------------
    // ----------     generate the old version shetab report ----------------
public static String generateAcquirerShetabReportOld(AcqShetabReportRecord record, int row){
	
	StringBuilder report9thOld = new StringBuilder();
	PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd|HH:mm:ss");
	
	String trnType = ShetabReportConstants.TrnTypeToAcqReportType.get(record.trnType);
        // C1
	    report9thOld.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, row + "", '0') + "|");
        // C2
	    report9thOld.append("             |");
        // C3
	    report9thOld.append(trnType + "|");
        // C4, C5
        Date origDt = record.origDt.toDate();
        report9thOld.append(dateFormatPers.format(origDt) + "|");
        // C6
        report9thOld.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0') + "|");
        // C7
        if (TrnType.INCREMENTALTRANSFER.equals(record.trnType))
        	report9thOld.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "|");
        else
        	report9thOld.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "|");
        // C8
        report9thOld.append(StringFormat.formatNew(9, StringFormat.JUST_LEFT, record.destBankId, ' ') + "|");
        // C9
        report9thOld.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, record.terminalId, '0') + "|");
        // C10
        report9thOld.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.amount, '0') + "|");
        // C11
        report9thOld.append(ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType) + "|");
        // C12, C13, C14
        report9thOld.append("0000000000000|                   |000000000000|");
        // C15
        report9thOld.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.orgIdNum, '0') + "|");
        // C16, C17, C18
        report9thOld.append("000|0|0\r\n");
        //-----------------------------------------------------------------------
	return report9thOld.toString();
	
}
//------------Moosavi : Task111686 : Add new Form 9 to be compatible to Shetab7------------
// ----------     generate the new version shetab7 report ----------------
public static String generateAcquirerShetabReportNew(AcqShetabReportRecord record,int row){
	
	StringBuilder report9thNew = new StringBuilder();
	PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd|HH:mm:ss");

	 String trnType = ShetabReportConstants.TrnTypeToAcqReportType.get(record.trnType);
         // C1
	     report9thNew.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, row + "", '0') + "|");
         // C2
	     report9thNew.append("             |");
         // C3
	     report9thNew.append(trnType + "|");
         // C4, C5
         Date origDt = record.origDt.toDate();
         report9thNew.append(dateFormatPers.format(origDt) + "|");
         // C6
         report9thNew.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0') + "|");
         // C7
         if (TrnType.INCREMENTALTRANSFER.equals(record.trnType))
        	 report9thNew.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "|");
         else
        	 report9thNew.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "|");
         // C8
         report9thNew.append(StringFormat.formatNew(9, StringFormat.JUST_LEFT, record.destBankId, ' ') + "|");
         // C9
         report9thNew.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, record.terminalId, '0') + "|");
         // C10
         report9thNew.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.amount, '0') + "|");
         // C11
         report9thNew.append(ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType) + "|");
         // C12, C13, C14
         report9thNew.append("0000000000000|                   |000000000000|");
         // C15
         report9thNew.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.orgIdNum, '0') + "|");
         // C16, C17, C18
         report9thNew.append("000|0|0|");
         // Moosavi: Task 111686 : add field 19-21 to from9 for shetab 7 version
         // C19
         report9thNew.append("0000|");
         // C20
         report9thNew.append("0000000000|");
         // C21, C22
         report9thNew.append("200|0\r\n");
         //-----------------------------------------------------------------------
	return report9thNew.toString();
}
//----------------------------------------------------------------------------------------------------------------------------------


    // 8th Format- SHETAB report
    public static String generateIssuerShetabReport(String instBin, MonthDayDate workingDay, DateTime cutoffFrom, DateTime cutoffTo, boolean reversalTrx) {
        int maxReportRecordSize = ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE);
        Institution myInstitution = ProcessContext.get().getMyInstitution();
        Long myBin = myInstitution.getBin();
        StringBuilder report8th = new StringBuilder();
        Long fee = new Long(0);
        long t1, t2, t3;

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMddHHmmss");

//		List<IssShetabReportRecord> TT_records = new ArrayList<IssShetabReportRecord>();
//		List<IssShetabReportRecord> TF_records = new ArrayList<IssShetabReportRecord>();

        DateTime dateFrom = new DateTime(new DayDate(cutoffFrom.getDayDate().getYear(), cutoffFrom.getDayDate().getMonth(), cutoffFrom.getDayDate().getDay()),
                new DayTime(cutoffFrom.getDayTime().getHour(), cutoffFrom.getDayTime().getMinute(), cutoffFrom.getDayTime().getSecond() + 1));

        DateTime dateTo = new DateTime(new DayDate(cutoffFrom.getDayDate().getYear(), cutoffFrom.getDayDate().getMonth(), cutoffFrom.getDayDate().getDay()),
                new DayTime(cutoffFrom.getDayTime().getHour(), cutoffFrom.getDayTime().getMinute(), cutoffFrom.getDayTime().getSecond()));
        int iterateHour = 25;
        boolean generateTotal = false;
        if (generateTotal)
            iterateHour = 0;

        for (int j= 0; j <= iterateHour; j++) {
            logger.debug("Time: " + j);
            if (generateTotal) {
                dateFrom = cutoffFrom;
                dateTo = cutoffTo;
            } else {
                dateTo.increase(60);

            }

            List<IssShetabReportRecord> TT_records = new ArrayList<IssShetabReportRecord>();
            List<IssShetabReportRecord> TF_records = new ArrayList<IssShetabReportRecord>();

            logger.debug("from: " + dateFrom);
            logger.debug("to: " + dateTo);

            for (int i = 0; ; i++) {
                t1 = System.currentTimeMillis();
                logger.debug("Iteration: " + i);

                List<IssShetabReportRecord> records = AccountingService.getIssShetabReportRecords(instBin, workingDay, dateFrom, dateTo,
                        i * maxReportRecordSize, maxReportRecordSize, reversalTrx);
                logger.debug("records.size: "+records.size());

                if (!reversalTrx && TT_records != null) {
                    TT_records.clear();
                    TT_records = AccountingService.getIssShetabReportRecordsTransferTo(instBin, workingDay, dateFrom, dateTo, i * maxReportRecordSize, maxReportRecordSize);
                    logger.debug("TT_records.size: "+TT_records.size());
                }


                if (!reversalTrx && TF_records != null) {
                    TF_records.clear();
                    TF_records = AccountingService.getIssShetabReportRecordsTransferFrom(instBin, workingDay, dateFrom, dateTo, i * maxReportRecordSize, maxReportRecordSize);
                    logger.debug("TF_records.size: "+TF_records.size());
                }

                t2 = System.currentTimeMillis();
                logger.debug("Query time: " + (t2 - t1));


                if (records == null || records.size() == 0) {
                    if (records == null)
                        records = new ArrayList<IssShetabReportRecord>();
                    records.clear();

                    if (TT_records != null && TT_records.size() > 0)
                        records.addAll(TT_records);

                    if (TF_records != null && TF_records.size() > 0)
                        records.addAll(TF_records);

                    if (records.size() == 0)
                        break;

                } else {

                    if (TT_records == null || TT_records.size() == 0) {
                        TT_records = null;
                    } else
                        records.addAll(TT_records);

                    if (TF_records == null || TF_records.size() == 0) {
                        TF_records = null;
                    } else
                        records.addAll(TF_records);
                }

                for (IssShetabReportRecord record : records) {
                    String debitCredit = "D";
                    String statementCode = getStatementCode(record);

                    if (statementCode != null) {
                        // C1
                        report8th.append(dateFormatPers.format(record.origDt.toDate())
                                + StringFormat.formatNew(2, StringFormat.JUST_RIGHT, record.terminalType.getCode() + "", '0')
                                + StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0') + "/");
                        report8th.append("0000" + "/");
                        report8th.append("8888" + "/");
                        report8th.append("0" + "/");
                        // C5
                        report8th.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, "0", '0') + "/");

                        fee = 0L;
                        if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {
                            if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                if (record.destBankId.equals(502229L))
                                    fee = (record.feeAmount != null) ? record.feeAmount : 0L;
                            } else if (myBin.equals(record.destBankId)) {
                                fee = (record.feeAmount != null) ? record.feeAmount : 0L;
                            }
                        } else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
                            fee = (record.feeAmount != null) ? record.feeAmount : 0L;
                        } else if (TrnType.BALANCEINQUIRY.equals(record.trnType)) {
                            fee = (record.feeAmount != null) ? record.feeAmount : 0L;
                        } else if (TrnType.WITHDRAWAL.equals(record.trnType)) {
                            fee = (record.feeAmount != null) ? record.feeAmount : 0L;
                        }

                        report8th.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.amount + fee, '0') + "/");

                        if ( // We are issuer
                                TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
                            if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                if (record.destBankId.equals(502229L))
                                    debitCredit = "C";

                            } else if (myBin.equals(record.destBankId))
                                debitCredit = "C";
                        }

                        // We are acquire
                        if (
                                TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId))
                            debitCredit = "C";
                        // C7
                        if (ISOFinalMessageType.isReversalOrRepeatMessage(record.type) && !(IfxType.SORUSH_REV_REPEAT_RS.equals(record.type)) )
                            debitCredit = ("D".equals(debitCredit) ? "C" : "D");

                        report8th.append(debitCredit + "/");

                        // C8
                        if (!reversalTrx) {
                            if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
                                report8th.append(252 + "/");
                            } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
                                report8th.append(253 + "/");
                            } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {
                                if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                    if (record.destBankId.equals(502229L))
                                        report8th.append(252 + "/");

                                } else if (myBin.equals(record.destBankId)) {
                                    report8th.append(252 + "/");
                                }
                            } else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
                                if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                    if (record.destBankId.equals(502229L))
                                        report8th.append(253 + "/");

                                } else if (myBin.equals(record.destBankId)) {
                                    report8th.append(253 + "/");
                                }
                            } else {
                                report8th.append(statementCode + "/");
                            }
                        } else {
                            report8th.append("090" + "/");
                        }

                        report8th.append("0/000/");
                        report8th.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, record.terminalType.getCode() + "", '0') + "/");
                        // C12
                        report8th.append("0000/");
                        report8th.append((ISOFinalMessageType.isReversalOrRepeatMessage(record.type) && !(IfxType.SORUSH_REV_REPEAT_RS.equals(record.type)) ? "R" : "N") + "/");
                        // C14
                        report8th.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0') + "/");
                        report8th.append("0000/");
                        Bank bank = ProcessContext.get().getBank(record.bankId.intValue());//GlobalContext.getInstance().getBank(record.bankId.intValue());
                        report8th.append(((bank != null && bank.getTwoDigitCode() != null) ? bank.getTwoDigitCode() : "00") + "/");
                        report8th.append("0000/");

                        if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
                            // C18
                            report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");

                            // C19&20
//						if(Util.isAccount(record.destAppPan))
//							record.destAppPan = "5022291111111111";
                            report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
                            report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
                        } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
                            // C18
                            report8th.append("0000000000000000000/");

                            // C19&20
                            if(Util.isAccount(record.appPan))
                                record.appPan = "5022291111111111";
                            report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
                            report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
                        } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {

                            if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                if (record.destBankId.equals(502229L)) {
                                    // C18
                                    report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");

                                    // C19&20
                                    report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
                                    report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
                                }
                            } else if (myBin.equals(record.destBankId)) {
                                // C18
                                report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");

                                // C19&20
                                report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
                                report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
                            }
                        } else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
                            if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                if (record.destBankId.equals(502229L)) {
                                    // C18
                                    report8th.append("0000000000000000000/");

                                    // C19&20
//								if(Util.isAccount(record.appPan))
//									record.appPan = "5022291111111111";
                                    report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
                                    report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");

                                }
                            } else if (myBin.equals(record.destBankId)) {
                                // C18
                                report8th.append("0000000000000000000/");

                                // C19&20
                                report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
                                report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
                            }
                        } else {
                            // C18
                            report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");

                            // C19&20
                            report8th.append("0000000000000000000/0000000000000000000/");
                        }

                        // C21&22
                        report8th.append("0000000000000/0000000000000/");

                        // C23
                        if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
                            report8th.append("46/");
                        } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
                            report8th.append("47/");
                        } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {
                            if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                if (record.destBankId.equals(502229L))
                                    report8th.append("46/");
                            } else if (myBin.equals(record.destBankId)) {
                                report8th.append("46/");
                            }
                        } else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
                            if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
                                if (record.destBankId.equals(502229L))
                                    report8th.append("47/");
                            } else if (myBin.equals(record.destBankId)) {
                                report8th.append("47/");
                            }
                        } else {
                            report8th.append("00/");
                        }

                        // C24&25
                        if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {
                            report8th.append(record.bankId + "/");
                            report8th.append(myBin + "/");
                        } else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
                            report8th.append(record.bankId + "/");
                            report8th.append(myBin + "/");
                        } else {
                            report8th.append("000000/000000/");
                        }

                        report8th.append("0/0/\r\n");
                    }
                }
                t3 = System.currentTimeMillis();
                logger.debug("Report generation time: " + (t3 - t2));
            }
            dateFrom.increase(60);
        }
        return report8th.toString();
    }

    // 13th Format- SHETAB report
    public static String generateTotalShetabReport(String instBin, MonthDayDate workingDay, DateTime cutoffFrom, DateTime cutoffTo) {
        String report13th = "";
        String TT = "";
        Long TT_amt = 0L;

        for (int i = 0; i < 1; i++) {
            List<TotalShetabReportRecord> records = new ArrayList<TotalShetabReportRecord>();
            if(ProcessContext.get().getAcquierSwitchTerminal(instBin) != null){
                logger.debug("institution "+ instBin + "has Acq terminal: " + ProcessContext.get().getAcquierSwitchTerminal(instBin) );
                records = AccountingService.getTotalShetabReportRecordsIss(instBin, workingDay, cutoffFrom, cutoffTo);
            }

            if(ProcessContext.get().getIssuerSwitchTerminal(instBin) != null){
                logger.debug("instirution: " + instBin + "has Iss terminal: " + ProcessContext.get().getIssuerSwitchTerminal(instBin));
                records.addAll(AccountingService.getTotalShetabReportRecordsAcq(instBin, workingDay, cutoffFrom, cutoffTo));
            }

            if (records == null || records.size() == 0)
                break;
            for (TotalShetabReportRecord record : records) {

                if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) &&
                        record.isShetab == 0 &&
                        TerminalType.ATM.equals(record.terminalType) &&
                        (IfxType.SORUSH_REV_REPEAT_RS.equals(record.ifxType) || IfxType.TRANSFER_TO_ACCOUNT_RS.equals(record.ifxType))) {

                    TT_amt += record.transactionAmount;

                }
				/*if (IfxType.SORUSH_REV_REPEAT_RS.equals(record.ifxType) || IfxType.TRANSFER_TO_ACCOUNT_RS.equals(record.ifxType)) {
					String debitCredit = "D";
					String trnType = TrnTypeToAcqReportType.get(record.trnType);
	
					TT += record.isShetab + 1 + "/";
					TT += trnType + "/";
					TT += TerminalTypeToAcqReportTermType.get(record.terminalType) + "/";
					TT += StringFormat.formatNew(15, StringFormat.JUST_RIGHT, record.transactionAmount, '0') + "/";
	
					if (TrnType.INCREMENTALTRANSFER.equals(record.trnType))
						debitCredit = "C";
					if (ShetabFinalMessageType.isReversalOrRepeatMessage(record.ifxType) && !IfxType.SORUSH_REV_REPEAT_RS.equals(record.ifxType))
						debitCredit = ("D".equals(debitCredit) ? "C" : "D");
	
					if (record.isShetab == 0) {
						TT += ("D".equals(debitCredit) ? "C" : "D") + "\r\n";
					} else
						TT += ("D".equals(debitCredit) ? "D" : "C") + "\r\n";
					
				}*/ else {

                    String debitCredit = "D";
                    String trnType = ShetabReportConstants.TrnTypeToAcqReportType.get(record.trnType);

                    report13th += record.isShetab + 1 + "/";
                    report13th += trnType + "/";
                    report13th += ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType) + "/";
                    report13th += StringFormat.formatNew(15, StringFormat.JUST_RIGHT, record.transactionAmount, '0') + "/";

                    if (TrnType.INCREMENTALTRANSFER.equals(record.trnType))
                        debitCredit = "C";
                    if (ISOFinalMessageType.isReversalOrRepeatMessage(record.ifxType) && !IfxType.SORUSH_REV_REPEAT_RS.equals(record.ifxType))
                        debitCredit = ("D".equals(debitCredit) ? "C" : "D");

                    if (record.isShetab == 0) {
                        report13th += ("D".equals(debitCredit) ? "C" : "D") + "\r\n";
                    } else
                        report13th += ("D".equals(debitCredit) ? "D" : "C") + "\r\n";
                }
            }
        }

        if (!TT_amt.equals(0L)) {
            TT = "1" + "/" + "TT" + "/" + "ATM" + "/" + StringFormat.formatNew(15, StringFormat.JUST_RIGHT, TT_amt, '0') + "/" + "D" + "\r\n";
        }
        report13th += TT;
        return report13th;
    }

    public static String generateReconcileShetabReport(List<Ifx> ifxList) {
        String report = "";

//		StringFormat format2 = new StringFormat(2, StringFormat.JUST_RIGHT);
//		StringFormat format6 = new StringFormat(6, StringFormat.JUST_RIGHT);
//		StringFormat format12 = new StringFormat(12, StringFormat.JUST_RIGHT);
//		StringFormat format19 = new StringFormat(19, StringFormat.JUST_RIGHT);
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");

        int row = 0;
//		Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
        Long myBin = ProcessContext.get().getMyInstitution().getBin();
        String acqIss = "0";

        for (Ifx ifx : ifxList) {
            report += StringFormat.formatNew(6, StringFormat.JUST_RIGHT, row++ + "", '0');
            if (ifx.getBankId().equals(myBin))
                acqIss = "1";
            else if (ifx.getDestBankId().equals(myBin))
                acqIss = "2";
            else
                acqIss = "0";
            report += acqIss;
            report += "??";
            report += dateFormatPers.format(ifx.getOrigDt().toDate());
            report += StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getSrc_TrnSeqCntr(), '0');
            report += StringFormat.formatNew(19, StringFormat.JUST_RIGHT, ifx.getAppPAN(), '0');
            report += StringFormat.formatNew(12, StringFormat.JUST_RIGHT, ifx.getReal_Amt()/* getAuth_Amt() */, '0');
            report += ifx.getDestBankId();
            report += "1?";
            report += ifx.getBankId();
            report += "1?";
            report += ifx.getTerminalType();
            report += StringFormat.formatNew(12, StringFormat.JUST_RIGHT, ifx.getReal_Amt()/* getAuth_Amt() */, '0');
            report += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, ifx.getRsCode(), '0');

            report += "\r\n";
        }
        return report;
    }

    public static String getStatementCode(IssShetabReportRecord record) {
        if (TrnType.WITHDRAWAL.equals(record.trnType)) {
            if (TerminalType.ATM.equals(record.terminalType)) {
                return "141";
            } else if (TerminalType.PINPAD.equals(record.terminalType)) {
                return "446";
            }
        } else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {
            return "252";
        } else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
            return "253";
        } else if (TrnType.BILLPAYMENT.equals(record.trnType)) {
            if (OrganizationType.ELECTRONIC.equals(record.billOrgType)) {
                return "439";
            } else if (OrganizationType.TEL.equals(record.billOrgType)) {
                return "440";
            } else if (OrganizationType.WATER.equals(record.billOrgType)) {
                return "441";
            } else if (OrganizationType.GAZ.equals(record.billOrgType)) {
                return "442";
            } else if (OrganizationType.MOBILE.equals(record.billOrgType)) {
                return "443";
            } else if (OrganizationType.UNDEFINED.equals(record.billOrgType)) {
                return "438";
            } else {
                return "438";
            }

        } else if (TrnType.PURCHASE.equals(record.trnType)) {
            if (TerminalType.POS.equals(record.terminalType))
                return "444";
            else if (TerminalType.INTERNET.equals(record.terminalType))
                return "448";
            else if (TerminalType.MOBILE.equals(record.terminalType))
                return "448";
            else if (TerminalType.KIOSK_CARD_PRESENT.equals(record.terminalType))
                return "444";
        } else if (TrnType.BALANCEINQUIRY.equals(record.trnType)) {
            return "458";
        } else if (TrnType.TRANSFER.equals(record.trnType)) {
            return "251";
        } else if (TrnType.RETURN.equals(record.trnType))
            return "445";

        logger.info("Not in the report: AppPAN: " + record.appPan + ", TrnType: " + record.trnType + ", TrnSeqCntr: "
                + record.trnSeqCntr + ", TerminalType: " + record.terminalType);
        return null;
    }

    public static List<Transaction> getListOfTransactionInShetabReversalReport(BufferedReader brShetabReport, BufferedWriter brShetabReportRes, BufferedWriter brShetabValidateR, User user) throws Exception {
        return getListOfTransactionInShetabReversalReport(brShetabReport, brShetabReportRes, brShetabValidateR, user, ProcessContext.get().getMyInstitution().getBin());
    }

    public static List<BeanDataTransfer> getListOfTrxSorushInShetabReversalReport(BufferedReader brShetabReport, BufferedWriter brShetabReportRes, BufferedWriter brShetabValidateR, User user) throws Exception {
        return getListOfTrxSorushInShetabReversalReport(brShetabReport, brShetabReportRes, brShetabValidateR, user, ProcessContext.get().getMyInstitution().getBin());
    }

    public static List<BeanDataTransfer> getListOfNotTrxSorush(BufferedReader brShetabReport, BufferedWriter brShetabReportRes, BufferedWriter brShetabValidateR, User user) throws Exception {
        return getListOfTrxSorushNotTransfer(brShetabReport, brShetabReportRes, brShetabValidateR, user, ProcessContext.get().getMyInstitution().getBin());
    }

    public static List<BeanDataTransfer> getListOfTrxSorushNotTransfer(BufferedReader brShetabReport, BufferedWriter brShetabReportRes, BufferedWriter brShetabValidateR, User user, Long myBin) throws Exception {

        List<Transaction> retVal  = new ArrayList<Transaction>();
        List<BeanDataTransferSorush> dataSorush ;

        List<BeanDataTransfer> totalRetVal = new ArrayList<BeanDataTransfer>();
        BeanSorushNotTrn record;
        //For Duplication Checking
        ArrayList<BeanSorushNotTrn> recordArr = new ArrayList<BeanSorushNotTrn>();
        String reportRecord = "" ;
        Long row = 0L;
        DateTime now = DateTime.now();
        brShetabReportRes.append("----Processing at " + now + "\r\n");
        while (brShetabReport.ready()) {
            try {
                Thread.sleep(1000);
                record = new BeanSorushNotTrn();
                GeneralDao.Instance.beginTransaction();
                row++;
                if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                    reportRecord = reportRecord.trim();
                }
                if (reportRecord != null && reportRecord.trim().length() == 0 ){
                    GeneralDao.Instance.endTransaction();
                    continue;
                }
                record = parseSorushNotTransferReportRecord(row, reportRecord, myBin);
                logger.debug("processing record: " + record);

                //For Duplication Checking
                try {
                    if(recordArr.contains(record)){
                        logger.debug("Record num: " + reportRecord + " many found in file...: " );
                        brShetabReportRes.append("Record num: ").append(reportRecord).append("  many found in file...: ").append("\r\n");
                        GeneralDao.Instance.endTransaction();
                        continue;
                    }else{
                        recordArr.add(record);
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }

                String query =  " from BeanDataTransferSorush i where" +
                        "     i.trnSeqCntr = :trnSeqCntr  " +
                        " and i.amount = :amountTrx" +
                        " and i.appPan = :appPan ";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("trnSeqCntr", record.trnSeqCntr);
                params.put("amountTrx", record.amountTrx);
                params.put("appPan", record.appPan);
                dataSorush  = new ArrayList<BeanDataTransferSorush>();
                dataSorush = GeneralDao.Instance.find(query, params);
                if (dataSorush.size() == 0) {
                    GeneralDao.Instance.endTransaction();
                    logger.error("getListOfTrxSorushNotTransfer: Record num: " + reportRecord + " not found...");
                    brShetabReportRes.append("NOK(" + retVal.size() + "): " + reportRecord + "\r\n");
                } else if (dataSorush.size() >= 1) {
                    /**
                     * dar in function transaction close mishavad.
                     */
                    createRquest(dataSorush, brShetabReportRes, reportRecord, totalRetVal, record,user);

                    if(dataSorush.size() > 1){
                        logger.debug("Record num: " + reportRecord + " many matching trx found...: " + retVal.size());
                        brShetabReportRes.append("Record num: " + reportRecord + " many matching trx found...: " + retVal.size() + "\r\n");
                    }
                }
                brShetabReportRes.flush();
            } catch (Exception e) {
                logger.error("Error in read row " + reportRecord + " " + e );
                brShetabReportRes.append("Error in read row " + reportRecord + " " + e );
                GeneralDao.Instance.endTransaction();
            }
        }
        return totalRetVal;

    }

    public static  BeanSorushNotTrn getTrxSorushWithTrxID(BufferedReader brShetabReport, Long myBin,Transaction trx){
        BeanSorushNotTrn record;
        String reportRecord = "" ;
        Long row = 0L;
        try {
            while (brShetabReport.ready()) {
                try {
                    row++;
                    if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                        reportRecord = reportRecord.trim();
                    }
                    if (reportRecord != null && reportRecord.trim().length() == 0 ){
                        continue;
                    }
                    record = ShetabReconciliationService.parseSorushNotTransferReportRecord(row, reportRecord, myBin);
                    if(record.getAppPan().equals(trx.getIncomingIfx().getAppPAN()) &&
                            record.getTrnSeqCntr().equals(trx.getIncomingIfx().getIfxSrcTrnSeqCntr()) &&
                            record.getAmountTrx().equals(trx.getIncomingIfx().getTrx_Amt())
                            ){
                        return record;
                    }
                }catch (Exception e) {
                    logger.info("Problem");
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static List<BeanDataTransfer> getListOfTrxSorushInShetabReversalReport(BufferedReader brShetabReport, BufferedWriter brShetabReportRes, BufferedWriter brShetabValidateR, User user, Long myBin) throws Exception {
        List<Transaction> retVal  = new ArrayList<Transaction>();
        List<BeanDataTransfer> totalRetVal = new ArrayList<BeanDataTransfer>();
        BeanDataTransfer dataTransfer;
        ShetabReversalReportRecord record;
        StringBuilder validateResult;
        String reportRecord;
        Long row = 0L;
        DateTime now = DateTime.now();
        brShetabReportRes.append("----Processing at " + now + "\r\n");
        while (brShetabReport.ready()) {
            try {
                row++;
                if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                    reportRecord = reportRecord.trim();
                }
                if(reportRecord.equals("")){
                    continue;
                }
                validateResult = validateRecordField(row, reportRecord, myBin);
                if (validateResult == null)
                    continue;//break;
                if (validateResult.toString().equals("")) {

                    record = parseSorushTransferReportRecord(row, reportRecord, myBin);
                    record.issueDateTime = now;
                    record.user = user;
                    logger.debug("processing record: " + record);

                    if (record.bankId != null && record.bankId.equals(936450L)) {
                        logger.info("sorush query: empty because it is sorush bin not bank bin" );
                        retVal = Collections.emptyList();
                    } else {
                        retVal = getListOfTrxSorushNew(record,0);

                        if (retVal.size() == 0) {
                            logger.info("Search(New) record in baze ");
                            brShetabReportRes.append("Search(New) record in baze ...\r\n");
                            retVal = getListOfTrxSorushNew(record,1);
                        }
                        if(retVal.size() == 0){
                            logger.info("Search(Old) record in One Day ");
                            brShetabReportRes.append("Search(Old) record in One Day  ...\r\n");
                            retVal = getListOfTrxSorushOld(record,0);
                        }
                        if(retVal.size() == 0){
                            logger.info("Search(Old) record in baze ");
                            brShetabReportRes.append("Search(Old) record in baze  ...\r\n");
                            retVal = getListOfTrxSorushOld(record,1);
                        }
                    }
                    if (retVal.size() == 0) {
                        logger.info("Record num: " + row + " not found...");
                        brShetabReportRes.append("NOK(" + retVal.size() + "): " + reportRecord + "\r\n");
                    } else if (retVal.size() > 1) {
                        logger.error("Record num: " + row + " many matching trx found...: " + retVal.size());
                        brShetabReportRes.append("NOK(" + retVal.size() + "): " + reportRecord + "\r\n");
                    } else if (retVal.size() == 1) {
                        record.trx = retVal.get(0);
                        logger.debug("Record num: " + row + " found trx...: " + retVal.get(0));
                        record.terminalId = retVal.get(0).getIncomingIfx().getTerminalId();
                        if (recordDosnotExist(record)) {

                            dataTransfer = new BeanDataTransfer();
                            dataTransfer.setTrx(retVal.get(0));
                            dataTransfer.setReverslSorush(record);
                            totalRetVal.add(dataTransfer);
//			                        GeneralDao.Instance.saveOrUpdate(record);
                            brShetabReportRes.append("OK: " + reportRecord + "\r\n");
                        } else {
                            brShetabReportRes.append("DUP: " + reportRecord + "\r\n");
                        }
                    }
                    brShetabReportRes.flush();

                }else {
                    brShetabReportRes.append(validateResult.toString());

                    brShetabReportRes.flush();
                }
            } catch (Exception e) {
                logger.error("Error Read Line File: "+ e,e);
            }

        }
        return totalRetVal;
    }

    public static List<Transaction> getListOfTrxSorushNew(ShetabReversalReportRecord record, int LengthBaze){
        List<Transaction> retVal = new ArrayList<Transaction>();
        DateTime startDate = new DateTime(record.persianDt.nextDay(-LengthBaze), new DayTime(0, 0, 0));
        DateTime endDate = new DateTime(record.persianDt.nextDay(LengthBaze), new DayTime(23, 59, 59));
    	/*String replaceQ = null, mainQ = " and i.networkTrnInfo.BankId = :bankId ";
    	
        if ((record.bankId.toString().length() == 9) && record.bankId.toString().startsWith(shaparakId.toString())) {
            replaceQ = String.format
            		(" and i.networkTrnInfo.BankId between %1$s000 and %1$s999", shaparakId.toString());
        }*/

        String query =    "select distinct trx.firstTransaction"
                + " from Ifx i " +
                "   inner join i.transaction trx "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
                + " and i.request = false "
                //                + " and i.ifxDirection = :direction"
                //                + " and i.ifxType in (107,3,54,55,56,58,13,101) "
                + " and i.receivedDtLong between :startDate and :endDate "
                + "	and i.ifxEncAppPAN = :appPan "
                + " and i.ifxRsCode = :success "
//		                +    (replaceQ == null ? mainQ : replaceQ)
                + " and i.ifxSrcTrnSeqCntr = :trnSeqCntr"
                + " and i.ifxType not in " + IfxType.strRevRsOrdinals
                + " and i.ifxType not in " + IfxType.strRevRqOrdinals ;

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("trnSeqCntr", record.trnSeqCntr);
        MigrationData migData = MigrationDataService.getMigrationData(record.appPan);
        if (migData != null) {
            params.put("appPan", migData.getFanapAppPan());
        } else {
            params.put("appPan", record.appPan);
        }
        params.put("success", ISOResponseCodes.APPROVED);
        /* if (replaceQ == null)
             params.put("bankId", record.bankId);*/
        params.put("startDate",startDate.getDateTimeLong());
        params.put("endDate", endDate.getDateTimeLong());
//         params.put("origDt", record.persianDt);

        retVal = GeneralDao.Instance.find(query, params);

        return retVal;
    }

    public static List<Transaction> getListOfTrxSorushOld(ShetabReversalReportRecord record, int LengthBaze){

        List<Transaction> retVal = new ArrayList<Transaction>();
        DateTime startDate = new DateTime(record.persianDt.nextDay(-LengthBaze), new DayTime(0, 0, 0));
        DateTime endDate = new DateTime(record.persianDt.nextDay(LengthBaze), new DayTime(23, 59, 59));
    	/*String replaceQ = null, mainQ = " and i.networkTrnInfo.BankId = :bankId ";
    	
        if ((record.bankId.toString().length() == 9) && record.bankId.toString().startsWith(shaparakId.toString())) {
            replaceQ = String.format
            		(" and i.networkTrnInfo.BankId between %1$s000 and %1$s999", shaparakId.toString());
        }*/

        String query =    "select distinct trx.firstTransaction"
                + " from Ifx i " +
                "   inner join i.transaction trx "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
                + " and i.request = false "
                //                + " and i.ifxDirection = :direction"
                //                + " and i.ifxType in (107,3,54,55,56,58,13,101) "
                + " and i.receivedDtLong between :startDate and :endDate "
                + "	and i.eMVRqData.CardAcctId.AppPAN = :appPan "
                + " and i.eMVRsData.RsCode = :success "
//		                +    (replaceQ == null ? mainQ : replaceQ)
                + " and i.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr"
                + " and i.ifxType not in " + IfxType.strRevRsOrdinals
                + " and i.ifxType not in " + IfxType.strRevRqOrdinals ;

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("trnSeqCntr", record.trnSeqCntr);
        MigrationData migData = MigrationDataService.getMigrationData(record.appPan);
        if (migData != null) {
            params.put("appPan", migData.getFanapAppPan());
        } else {
            params.put("appPan", record.appPan);
        }
        params.put("success", ISOResponseCodes.APPROVED);
/*         if (replaceQ == null)
             params.put("bankId", record.bankId);*/
        params.put("startDate",startDate.getDateTimeLong());
        params.put("endDate", endDate.getDateTimeLong());
//         params.put("origDt", record.persianDt);

        retVal = GeneralDao.Instance.find(query, params);

        return retVal;

    }

    public static List<Transaction> getListOfTransactionInShetabReversalReport(BufferedReader brShetabReport, BufferedWriter brShetabReportRes, BufferedWriter brShetabValidateR, User user, Long myBin) throws Exception {
        ShetabReversalReportRecord record;
        String reportRecord;
        Long row = 0L;
        List<Transaction> result = null;
        DateTime now = DateTime.now();
        brShetabReportRes.append("----Processing at " + now + "\r\n");
        brShetabValidateR.append("----Processing at " + now + "\r\n");
        while (brShetabReport.ready()) {
            row++;
            if ((reportRecord = brShetabReport.readLine()).length() > 0) {
                reportRecord = reportRecord.trim();
            }
            StringBuilder validateResult = validateRecordField(row, reportRecord, myBin);
            if (validateResult.toString().equals("")) {
                record = parseShetabReversalReportRecord(row, reportRecord, myBin);
                record.issueDateTime = now;
                record.user = user;

                logger.debug("processing record: " + record);

                if (!record.recordType.equals(REVERSAL_RECORDS_TYPES[0])) {
                    brShetabReportRes.append("NOK: " + reportRecord + "\r\n");
                    brShetabReportRes.flush();
                    continue;
                }

                String query = GetQueryFindTrxNew( record.terminalId);
                		
                                 /* " select distinct trx.firstTransaction "
                                + " from Ifx i inner join i.transaction trx "
                                + " where "
                                + " i.settleDt = :origDt"
                                + " and i.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr "
                                + " and i.eMVRqData.CardAcctId.AppPAN = :appPan "
                                + (replaceQ == null ? mainQ : replaceQ)
                                + " and i.ifxType not in " + IfxType.strRevRsOrdinals
                                + " and i.ifxType not in " + IfxType.strRevRqOrdinals
                                + (!record.terminalId.equals("0") ?
                                " and i.networkTrnInfo.TerminalId = :terminal " : " ")
                                + " and i.request = true ";*/

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("origDt", record.persianDt);
                params.put("trnSeqCntr", record.trnSeqCntr);

                MigrationData migData = MigrationDataService.getMigrationData(record.appPan);
                if (migData != null) {
                    params.put("appPan", migData.getFanapAppPan());

                } else {
                    params.put("appPan", record.appPan);

                }

                if (!record.terminalId.equals("0")) {
                    params.put("terminal", record.terminalId);
                }

                try {

                    result = GeneralDao.Instance.find(query, params);

                } catch (Exception e) {
                    logger.error("Error in GetQueryFindTrxNew" + e);
                }

                if (result == null || (result.size() == 0 /*&& ConfigUtil.OLD_QUERY_IFX_SUPPORT != null && ConfigUtil.getBoolean(ConfigUtil.OLD_QUERY_IFX_SUPPORT) */)) {
                    logger.error("Record num: " + row + " not found in New Query ...");
                    query = GetQueryFindTrxOld( record.terminalId);
                    result = GeneralDao.Instance.find(query, params);
                }

                if(result.size() == 0){
                    logger.error("Record num: " + row + " not found...");
                    brShetabReportRes.append("NOK(" + result.size() + "): " + reportRecord + "\r\n");
                } else if (result.size() > 1) {
                    logger.error("Record num: " + row + " many matching trx found...: " + result.size());
                    brShetabReportRes.append("NOK(" + result.size() + "): " + reportRecord + "\r\n");
                } else if (result.size() == 1) {
                    record.trx = result.get(0);
                    logger.debug("Record num: " + row + " found trx...: " + result.get(0));
                    if (recordDosnotExist(record/*, dataSource*/)) {
                        GeneralDao.Instance.saveOrUpdate(record);
                        Ifx incomingIfx = result.get(0).getIncomingIfx();
                        Long amount = 0L;
                        if (incomingIfx.getTrx_Amt() != null) {
                            if (incomingIfx.getTrx_Amt().equals(record.amount)) {
                                amount = 0L;
                            } else {
                                amount = incomingIfx.getAuth_Amt() - record.amount;
                            }
                        }

                        if (amount < 0) {
                            amount = 0L;
                            logger.info("amount < 0  new value for amount is " + amount);
                        }
                        SchedulerService.addReversalAndRepeatTrigger(result.get(0).getInputMessage(), amount);
                        brShetabReportRes.append("OK: " + reportRecord + "\r\n");
                    } else {
                        brShetabReportRes.append("DUP: " + reportRecord + "\r\n");
                    }
                }
                brShetabReportRes.flush();
            } else {
                brShetabValidateR.append(validateResult.toString());
                brShetabValidateR.flush();
            }

        } ///end of while
        return null;
    }

    private static String GetQueryFindTrxOld(String terminalID){
        String retVal = "";
        retVal =  " select distinct trx.firstTransaction "
                + " from Ifx i inner join i.transaction trx "
                + " where "
                + " i.settleDt = :origDt"
                + " and i.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr "
                + " and i.eMVRqData.CardAcctId.AppPAN = :appPan "
                + " and i.ifxType not in " + IfxType.strRevRsOrdinals
                + " and i.ifxType not in " + IfxType.strRevRqOrdinals
                + (!terminalID.equals("0") ?
                " and i.networkTrnInfo.TerminalId = :terminal " : " ")
                + " and i.request = true ";

        return retVal;
    }

    private static String GetQueryFindTrxNew(String terminalID){
        String retVal = "";
        retVal =  " select distinct trx.firstTransaction "
                + " from Ifx i inner join i.transaction trx "
                + " where "
                + " i.settleDt = :origDt"
                + " and i.ifxSrcTrnSeqCntr = :trnSeqCntr "
                + " and i.ifxEncAppPAN = :appPan "
                + " and i.ifxType not in " + IfxType.strRevRsOrdinals
                + " and i.ifxType not in " + IfxType.strRevRqOrdinals
                + (!terminalID.equals("0") ?
                " and i.networkTrnInfo.TerminalId = :terminal " : " ")
                + " and i.request = true ";

        return retVal;
    }

    private static boolean recordDosnotExist(ShetabReversalReportRecord record) {
        List<ShetabReversalReportRecord> result;

        String query =
                " from ShetabReversalReportRecord r "
                        + " where 1=1" + (
                        !DayDate.isNullOrUnknown(record.persianDt) ?
                                " and r.persianDt = :persianDt" : "")
                        +
                        (record.trnSeqCntr != null ? " and r.trnSeqCntr = :trnSeqCntr" : "")
                        + " and r.appPan = :appPan"
                        + " and r.amount = :amount"
                        + " and r.bankId = :bankId"
                        + " and r.terminalId = :terminalId";


        Map<String, Object> params = new HashMap<String, Object>();
        if (!DayDate.isNullOrUnknown(record.persianDt))
            params.put("persianDt", record.persianDt);
        params.put("trnSeqCntr", record.trnSeqCntr);
        params.put("appPan", record.appPan);
        params.put("bankId", record.bankId);
        params.put("amount", record.amount);
        params.put("terminalId", record.terminalId);

        result = GeneralDao.Instance.find(query, params);

        if (result != null && result.size() > 0)
            return false;

        return true;
    }

    private static StringBuilder validateRecordField(Long row, String reportRecord, Long myBin) {
        StringBuilder validateResult = new StringBuilder();
        validateResult.append("");
        StringTokenizer tokenizer;
        tokenizer = new StringTokenizer(reportRecord, "|");
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String dateStr = "13" + tokenizer.nextToken().trim();
        String dateStr1 = dateStr.substring(2);
        try {
            dateFormatPers.parse(dateStr);
        } catch (ParseException e) {
            validateResult.append("-- *** INVALID DATE FORMAT *** --");
            validateResult.append("AT INPUT ITEM : '" + dateStr1 + "'\r\n");
        }
        String trnSeqCntr = ISOUtil.zeroUnPad(tokenizer.nextToken().trim());
        try {
            Long.parseLong(trnSeqCntr);
        } catch (NumberFormatException e) {
            validateResult.append("-- *** INVALID TRNSEQCNTR FORMAT *** --");
            validateResult.append("AT INPUT ITEM : '" + trnSeqCntr + "'\r\n");
        }
        String appPan = tokenizer.nextToken().trim();
        try {
            Long.parseLong(appPan);
            if (!(appPan.length() == 16 || appPan.length() == 19)) {
                validateResult.append("-- *** APPPAN NUMBER MUST BE 16 OR 19 DIGITS *** ---");
                validateResult.append("AT INPUT ITEM : '" + appPan + "'\r\n");
            }

        } catch (NumberFormatException e) {

            validateResult.append("-- *** INVALID APPPAN FORMAT *** --");
            validateResult.append("AT INPUT ITEM : '" + appPan + "'\r\n");
        }
        String amount = tokenizer.nextToken();
        try {
            Long.parseLong(amount);
        } catch (NumberFormatException e) {

            validateResult.append("-- *** INVALID AMOUNT FORMAT *** --");
            validateResult.append("AT INPUT ITEM : '" + amount + "'\r\n");
        }
        String bankId = tokenizer.nextToken();
        try {
            Long.parseLong(bankId);
        } catch (NumberFormatException e) {

            validateResult.append("-- *** INVALID BANKID FORMAT *** --");
            validateResult.append("AT INPUT ITEM : '" + bankId + "'\r\n");
        }
        String recordCode = tokenizer.nextToken();
        try {
            Long.parseLong(recordCode);
        } catch (NumberFormatException e) {

            validateResult.append("-- INVALID RECORDCODE FORMAT --");
            validateResult.append("AT INPUT ITEM : '" + recordCode + "'\r\n");
        }
        if (!validateResult.toString().equals("")) {
            validateResult.append("-- INPUT FORMAT ERROR AT LINE: ");
            validateResult.append(row + "\r\n");
        }

        return validateResult;
    }

    public static BeanSorushNotTrn parseSorushNotTransferReportRecord(Long row, String reportRecord, Long myBin) throws ParseException {
        StringTokenizer tokenizer;
        tokenizer = new StringTokenizer(reportRecord, "|");

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");

        BeanSorushNotTrn record = new BeanSorushNotTrn();

        record.row = row;

        String dateStr = "13" + tokenizer.nextToken().trim();     //930313|001898|000000100000|000000010652|000000110652|5022291017653948
        Date date = dateFormatPers.parse(dateStr);
        record.persianDt = new DayDate(date);
        record.trnSeqCntr = ISOUtil.zeroUnPad(tokenizer.nextToken().trim());
        record.amountTrx = new Long(tokenizer.nextToken().trim());
        record.amountSod = new Long(tokenizer.nextToken().trim());
        record.amount = new Long(tokenizer.nextToken().trim());
        record.appPan = tokenizer.nextToken().trim();
        return record;
    }

    public static ShetabReversalReportRecord parseSorushTransferReportRecord(Long row, String reportRecord, Long myBin) throws ParseException {

    	
        StringTokenizer tokenizer;
        tokenizer = new StringTokenizer(reportRecord, "|");

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");

        ShetabReversalReportRecord record = new ShetabReversalReportRecord();

        record.row = row;

        String dateStr = "13" + tokenizer.nextToken().trim();     //51|2012/12/29|312609|5022291003494877|100000|610433|45373|nn|null|ﻊﻟی ﺎﻣیﺮﺤﺳیﻥی, 2541|2013/01/17, 08:29:51|0
        Date date = dateFormatPers.parse(dateStr);
        record.persianDt = new DayDate(date);

        record.trnSeqCntr = ISOUtil.zeroUnPad(tokenizer.nextToken().trim());
        record.appPan = tokenizer.nextToken().trim();
        record.amount = new Long(tokenizer.nextToken().trim());
        record.bankId = new String(tokenizer.nextToken().trim());

        tokenizer.nextToken().trim();

        record.recordCode = new Long(tokenizer.nextToken().trim());
        record.recordType = tokenizer.nextToken().trim();
        record.recordTypeInsert = tokenizer.nextToken().trim();
//        tokenizer.nextToken().trim();
        if (tokenizer.hasMoreTokens()) {
            record.terminalId = tokenizer.nextToken().trim();
        } else {
            record.terminalId = "0";
        }


        if (record.bankId.equals(myBin)) {
            record.trnSeqCntr = new Long(record.trnSeqCntr).toString();
        }

        return record;

    }

    public static ShetabReversalReportRecord parseShetabReversalReportRecord(Long row, String reportRecord, Long myBin) throws ParseException {
        StringTokenizer tokenizer;
        tokenizer = new StringTokenizer(reportRecord, "|");

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");

        ShetabReversalReportRecord record = new ShetabReversalReportRecord();

        record.row = row;

        String dateStr = "13" + tokenizer.nextToken().trim();
        Date date = dateFormatPers.parse(dateStr);
        record.persianDt = new DayDate(date);

        record.trnSeqCntr = ISOUtil.zeroUnPad(tokenizer.nextToken().trim());
        record.appPan = tokenizer.nextToken().trim();
        record.amount = new Long(tokenizer.nextToken().trim());
        record.bankId = new String(tokenizer.nextToken().trim());

        tokenizer.nextToken().trim();

        record.recordCode = new Long(tokenizer.nextToken().trim());
        record.recordType = tokenizer.nextToken().trim();
        tokenizer.nextToken().trim();
        if (tokenizer.hasMoreTokens()) {
            record.terminalId = tokenizer.nextToken().trim();
        } else {
            record.terminalId = "0";
        }


        if (record.bankId.equals(myBin)) {
            record.trnSeqCntr = new Long(record.trnSeqCntr).toString();
        }

        return record;
    }

    public static void createRquest(List<BeanDataTransferSorush> dataSorush , BufferedWriter brShetabReportRes, String reportRecord, List<BeanDataTransfer> totalRetVal, BeanSorushNotTrn record,User user){

        try {
            ScheduleMessage msg = new ScheduleMessage();
            BeanDataTransfer dataTransfer = new BeanDataTransfer();
            if(dataSorush.get(0).trx != null){
                ShetabReversalReportRecord recordSorush = new ShetabReversalReportRecord( dataSorush.get(0).row,
                        dataSorush.get(0).persianDt,
                        dataSorush.get(0).trnSeqCntr,
                        dataSorush.get(0).appPan,
                        record.amount,
                        dataSorush.get(0).bankId,
                        dataSorush.get(0).recordCode,
                        dataSorush.get(0).user,
                        dataSorush.get(0).recordType,
                        dataSorush.get(0).terminalId
                );
                dataTransfer.setTrx(dataSorush.get(0).trx);
                dataTransfer.setReverslSorush(recordSorush);
                totalRetVal.add(dataTransfer);
                logger.info("Create record from row " + reportRecord + "  ");
                brShetabReportRes.append("Create record from row " + reportRecord +"  " + "\r\n");

                msg = new ScheduleMessage();

                /**@author k.khodadi
                 * Message be sorat dasti shakhte mishavad
                 */
                try {
                    msg = TransferManual.getInstance().getTrxTransferSorushi(dataTransfer);
                    msg.getIfx().getSafeOriginalDataElements().setRefSorushiTransaction(dataSorush.get(0).trx);
                } catch (Exception e) {
                    GeneralDao.Instance.endTransaction();
                    logger.error("Error in create IFX"+e,e);
                }
                try {
                	if(msg != null){
                		
                		BeanLogSorushTrx loggerSorush = new BeanLogSorushTrx();
                		loggerSorush.setTrxRef(dataSorush.get(0).trx);                        	
                		loggerSorush.setAmountRefTrx(dataSorush.get(0).amount);
                		loggerSorush.setAmountTotal(dataSorush.get(0).trx.getFirstTransaction().getIncomingIfx().getAuth_Amt());
                		loggerSorush.setAmountSodSorush(dataSorush.get(0).amount - dataSorush.get(0).trx.getFirstTransaction().getIncomingIfx().getAuth_Amt() );
                		loggerSorush.setAppPan(dataSorush.get(0).appPan);
                		loggerSorush.setTrnSeqCntr(dataSorush.get(0).trnSeqCntr);
                		loggerSorush.setSorushDateTime(DateTime.now());
                		loggerSorush.setUser(user);
                		
                		GeneralDao.Instance.save(loggerSorush);
                	}
					
				} catch (Exception e) {
					logger.error("Error in log BeanLogSorushTrx");
				}
                GeneralDao.Instance.endTransaction();
                if(msg != null){

                    logger.info("Message sorushi create manual");
                    brShetabReportRes.append("Message sorushi create manual"+ "\r\n");
                    /**
                     * Message Sent mishavad
                     */
                    MessageManager.getInstance().putRequest(msg, null, System.currentTimeMillis());
                    logger.info("Put Request Message sorushi");
                    brShetabReportRes.append("Put Request Message sorushi"+ "\r\n");
                    /**
                     * Takhir yek saniye  for send badi message
                     */
                }

            }


        } catch (Exception e) {
            // TODO: handle exception
            GeneralDao.Instance.endTransaction();
            logger.error("Error in Insert Request " + e, e);
        }
    }

    public static BeanDataTransfer setDataTransferFromTrx(Transaction trx,BufferedReader brShetabReport){
        BeanDataTransfer retVal = new BeanDataTransfer();
        BeanSorushNotTrn record = getTrxSorushWithTrxID(brShetabReport, 0L, trx);

        if(record != null){
            ShetabReversalReportRecord shetabRecord = new ShetabReversalReportRecord();
            retVal.setReverslSorush(shetabRecord);
            retVal.getReverslSorush().amount = record.getAmount();
            retVal.getReverslSorush().appPan = trx.getIncomingIfx().getAppPAN();
            retVal.getReverslSorush().bankId = trx.getIncomingIfx().getBankId();
            retVal.getReverslSorush().recordCode = 0L;
            retVal.getReverslSorush().recordType = "n";
            retVal.getReverslSorush().recordTypeInsert = "O";
            retVal.getReverslSorush().terminalId = "0";
            retVal.getReverslSorush().trnSeqCntr = "0";
            retVal.setTrx(trx);
        } else{
            retVal = null;
        }

        return retVal;
    }

    public static void TransferSorushiFromTrx(TransferSorushTableJobInfo b,User user){
        GeneralDao.Instance.beginTransaction();
        File sorushTrxSod = new File(b.getUrlFile());
        BufferedReader sorushTrxSodReader= null;
        File shetabReportRes = new File(b.getUrlFile().substring(0, b.getUrlFile().length()-4)+"-reportTrx.txt");
        BufferedWriter sorushLog =  null;
        try {
            sorushTrxSodReader =  new BufferedReader(new FileReader(sorushTrxSod));
            sorushLog =  new BufferedWriter(new FileWriter(shetabReportRes));
        } catch (Exception e) {
            logger.error("Error in read file");
        }
        try {
            BeanDataTransfer dataTransfer = ShetabReconciliationService.setDataTransferFromTrx(b.getTransaction(),sorushTrxSodReader);
            ScheduleMessage msg = new ScheduleMessage();
            /**@author k.khodadi
             * Message be sorat dasti sakhte mishavad
             */
            try {
                msg = TransferManual.getInstance().getTrxTransferSorushi(dataTransfer);
                msg.getIfx().getSafeOriginalDataElements().setRefSorushiTransaction(b.getTransaction()) ;
                GeneralDao.Instance.saveOrUpdate(msg.getIfx());
            } catch (Exception e) {
                GeneralDao.Instance.endTransaction();
                logger.error("Error in create IFX"+e,e);
                sorushLog.append("Error in create IFX \n\r");
                return;
            }
            
            try {
            	if(msg != null){
            		
            		BeanLogSorushTrx loggerSorush = new BeanLogSorushTrx();
            		loggerSorush.setTrxRef(dataTransfer.trx);                        	
            		loggerSorush.setAmountRefTrx(dataTransfer.reverslSorush.amount);
            		loggerSorush.setAmountTotal(dataTransfer.trx.getFirstTransaction().getIncomingIfx().getAuth_Amt());
            		loggerSorush.setAmountSodSorush(dataTransfer.reverslSorush.amount - dataTransfer.trx.getFirstTransaction().getIncomingIfx().getAuth_Amt() );
            		loggerSorush.setAppPan(dataTransfer.reverslSorush.appPan);
            		loggerSorush.setTrnSeqCntr(dataTransfer.reverslSorush.trnSeqCntr);
            		loggerSorush.setSorushDateTime(DateTime.now());
            		loggerSorush.setUser(user);
            		
            		GeneralDao.Instance.save(loggerSorush);
            	}
				
			} catch (Exception e) {
				logger.error("Error in log BeanLogSorushTrx");
			}
            
            GeneralDao.Instance.endTransaction();
            if(msg != null){

                logger.info("Message sorushi create manual");
                sorushLog.append("Message sorushi create manual \n\r");
                MessageManager.getInstance().putRequest(msg, null, System.currentTimeMillis());
                logger.info("Put Request Message sorushi");
                sorushLog.append("Put Request Message sorushi \n\r");
            }
            sorushLog.close();
        } catch (Exception ex) {
            GeneralDao.Instance.endTransaction();
            logger.error(ex.getMessage());
        }

    }
}