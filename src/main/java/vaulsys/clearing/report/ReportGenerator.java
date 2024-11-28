package vaulsys.clearing.report;


import groovy.lang.Binding;
import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementDataReport;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.settlement.CoreConfigDataManager;
import vaulsys.clearing.settlement.IssueFCBThread;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.customer.Account;
import vaulsys.customer.AccountType;
import vaulsys.customer.Core;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Branch;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Organization;
import vaulsys.entity.impl.Shop;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.Terminal;
import vaulsys.thirdparty.consts.ThirdPartyType;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Pair;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.ZipUtil;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

import com.fanap.cms.exception.BusinessException;
import com.fanap.cms.valueobjects.corecommunication.DepositInfoForIssueDocument;
import com.fanap.cms.valueobjects.corecommunication.DocumentItemEntity;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DepositActionType;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DocumentItemEntityType;
import com.ghasemkiani.util.icu.PersianDateFormat;

public class ReportGenerator {

    private static final String bankAccountPEP = "995,2065,2";
    private static final String bankAccount = "995,2065,3";
    private static final String fanapAccount = "995,4444,1";

    private static final String defaultMeta = "<meta http-equiv=Content-Type content=\"text/html; charset=UTF8\">";
    private static final String defaultStyle = "<style><!--/* Style Definitions */.bodynormal{font-family:\"b nazanin\",\"tahoma\";font-size: 12pt;direction:rtl;text-align: center;}" +
            ".tablenormal{font-size: 12pt;font-family:\"b nazanin\", \"Tahoma\";font-weight:bold;text-align: left;border-collapse: collapse;border-color:#7ba0ce;margin-top:20pt;" +
            "/*border-spacing:5px;padding-top:1em;padding-left:3em;padding-right:3em;padding-bottom: 3em;*/" +
            "}" +
            ".tablelastRow{border-top:thick solid;border-top-color:#7ba0ce;}" +
            ".tablefirstRow{font-size: 14pt;background:#4f81bd;	color:#ffffff;}" +
            ".tableEvenRow{background:#d3dfee;}" +
            ".tableOddRow{}" +
            ".tablefirstCol{text-align:right;}" +
            "--></style>";

    private static final Logger logger = Logger.getLogger(ReportGenerator.class);

    public static void generateSettlementDataReport(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime stlDate) throws Exception{
        try{
            GeneralDao.Instance.beginTransaction();
            int firstResult=0;
            int maxResults = ConfigUtil.getInteger(ConfigUtil.GLOBAL_SETTLE_SETTLEMENTDATASIZE);
            for (int i =0; ;i++){
                GeneralDao.Instance.refresh(clearingProfile);
                int index =0;
                List<SettlementData> settlementDataList = AccountingService.findAllNotSettledSettlementDataUntilTime(terminals, clearingProfile, stlDate, firstResult+(i*maxResults), maxResults);
                if (settlementDataList.size()==0)
                    break;
                for (SettlementData settlementData : settlementDataList) {
                    logger.debug( ((i*maxResults)+index++) + " of  " + (settlementDataList.size()+ (i*maxResults)));
                    if (settlementData.getReport() == null){
                        generateSettlementDataReport(settlementData, stlDate);
                    }
                }
                GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.beginTransaction();
            }
            GeneralDao.Instance.endTransaction();
        } catch (Exception e) {
            logger.error(e);
            GeneralDao.Instance.rollback();
            throw e;
        }
    }

    public static void generateSettlementDataReportWithoutState(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime stlDate) {

        List<SettlementData> settlementDataList = new ArrayList<SettlementData>();
        if (terminals == null) {
            settlementDataList = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, stlDate, null);

        } else {
            settlementDataList = AccountingService.findAllNotSettledATMSettlementDataUntilTime(clearingProfile, stlDate, terminals);

        }
        try {
            for (SettlementData settlementData : settlementDataList)
                if (settlementData.getReport() == null)
                    generateSettlementDataReport(settlementData, settlementData.getSettlementTime());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void generateSettlementStateAndReport(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate, String title) throws Exception {
        List<SettlementData> settlementDataList = AccountingService.findAllNotSettledSettlementDataUntilTime(terminals, clearingProfile, settleDate, null);
        if (settlementDataList == null || settlementDataList.isEmpty())
            return;
        generateSettlementStateReport(clearingProfile, settlementDataList, title);
    }

    public static void generateSettlementStateAndReportNew(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate, String title) throws Exception {

        generateSettlementStateReportNew(terminals, clearingProfile, settleDate, title);
    }

    public static void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc, Boolean onlyFee) throws Exception {
        List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, null);
        generateDocumentSettlementState(settlementStates, docDesc, onlyFee, false);
    }

    public static void generateDocumentSettlementState(List<SettlementState> settlementStates, String docDesc, Boolean onlyFee, Boolean isForced) throws Exception {
        for (SettlementState settlementState: settlementStates) {
            if (settlementState != null) {
                //TODO: referesh is very bad for performance
                GeneralDao.Instance.refresh(settlementState);
                if (Core.FANAP_CORE.equals(settlementState.getCore()))
                    generateFinalFanapSettlementStateOrDataReport(settlementState, null, null, docDesc, onlyFee, isForced, null);
                else if (Core.Saderat_CORE.equals(settlementState.getCore()))
                    generateFinalSaderatSettlementStateReport(settlementState, docDesc, onlyFee, isForced);
                else /*if (settlementState.getCore().equals(Core.NEGIN_CORE))*/{
                    generateFinalNeginSettlementStateReport(settlementState, docDesc, onlyFee, isForced);
                }
            }
        }
    }

    public static void generateDocumentSettlementStateForPEP(ClearingProfile clearingProfile, String docDesc, DateTime settleDate) throws Exception {
        List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, null);
        for (SettlementState settlementState: settlementStates) {
            if (settlementState != null) {
                generateFinalNeginSettlementStateReportForPEP(settlementState, docDesc, settleDate);
            }
        }
    }

    public static void generateSettlementDataReport(SettlementData settlementData, DateTime stlDate) {
        ClearingProfile cp = settlementData.getClearingProfile();
        FinancialEntity entity = settlementData.getFinancialEntity();
        Terminal terminal = settlementData.getTerminal();
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");

        StringBuilder stlDataReport = new StringBuilder();

        String cpName = "الگوی تسویه پیش فرض";
        if (cp != null)
            cpName = cp.getName();

        String entityRole = "";
        if (entity != null) {
            entityRole = entity.getRole().getName() + " ";
            if (FinancialEntityRole.ORGANIZATION.equals(entity.getRole())) {
                Organization org = GeneralDao.Instance.load(Organization.class, entity.getId());
                entityRole += /*((Organization) entity)*/org.getType().toString();
            }
            entityRole += " " + entity.getCode().toString();

            if(FinancialEntityRole.BRANCH.equals(entity.getRole())) {
                Branch branch = GeneralDao.Instance.load(Branch.class, entity.getId());
                entityRole +=" کد شعبه" +branch.getCoreBranchCode().toString();
            }
        }
        if (terminal != null)
            entityRole += " ترمینال " + terminal.getCode();

        stlDataReport.append("<html><head><title>گزارش تراکنش های  " + entityRole + "</title>" + defaultMeta + defaultStyle + "</head><body class=bodynormal >\r\n");

        stlDataReport.append("<h2> گزارش تراکنش های  " + entityRole + "</h2>");

        stlDataReport.append("<h3>" + dateFormatPers.format(stlDate.toDate()) + "</h3>");

        stlDataReport.append("<h3> شماره حساب </h3>");

        stlDataReport.append("<h3  dir=ltr>" + ((entity != null && entity.getOwnOrParentAccount() != null) ?
                (Core.FANAP_CORE.equals(entity.getOwnOrParentAccount().getCore()) ?
                        fanapAccountFormat(entity.getOwnOrParentAccount().getAccountNumber(), entity.getOwnOrParentAccount().getType()) :
                        neginAccountFormat(entity.getOwnOrParentAccount().getAccountNumber())) :
                "-") + "</h3>");

        stlDataReport.append("<h3>الگوی تسویه حساب " + cpName + "</h3>");

        stlDataReport.append("<center><table border=1 cellspacing=0 cellpadding=\"%7\"  class=tablenormal><tr class=tablefirstRow>\r\n");

        stlDataReport.append("<td >" + "ردیف" + "</td>");

        stlDataReport.append("<td >" + "نوع تراکنش" + "</td>");

        stlDataReport.append("<td >" + "تاریخ تراکنش" + "</td>");

        stlDataReport.append("<td >" + "سریال" + "</td>");

        stlDataReport.append("<td >" + "ترمینال" + "</td>");

        stlDataReport.append("<td >" + "شماره کارت" + "</td>");

        stlDataReport.append("<td >" + "بانک پذیرنده" + "</td>");

        stlDataReport.append("<td >" + "مبلغ تراکنش" + "</td>");

        stlDataReport.append("<td >" + "مبلغ تسوِيه" + "</td>");

        stlDataReport.append("<td >" + "کارمزد" + "</td>");

        stlDataReport.append("<td >" + "مبلغ واريزی" + "</td>");

        stlDataReport.append("</tr>\r\n");

        int count = 1;
        long sumAmount = 0;
        long sumStlAmount = 0;
        long sumNetAmount = 0;
        int sumFeeAmount = 0;

        for(int i=0; ; i++){
            List<ReportRecord> records =
                    AccountingService.getSettlementDataReportRecords(settlementData,
                            i*ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE),
                            ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE));
            logger.debug("settlementData: "+settlementData.getId() + " i: "+ i);
            if(records == null || records.size() == 0)
                break;

            for (ReportRecord record:records) {

                Long trxAmount = record.auth_Amt;
                long stlFee = record.totalFee;
                long stlAmount = record.totalAmount;

                int fee = Math.round(stlFee);
                long netAmount = stlAmount + fee;

                /**********************/
                String style = "class=tableOddRow";
                if (count % 2 == 0)
                    style = "class=tableEvenRow";
                stlDataReport.append("<tr " + style + ">");

                stlDataReport.append("<td>" + count + "</td>");

                stlDataReport.append("<td class=tablefirstCol>" + record.ifxType.getName() + "</td>");

                stlDataReport.append("<td dir=ltr>" + dateFormatPers.format(record.recievedDt.toDate()) + "</td>");

                stlDataReport.append("<td>" + record.trnSeqCntr + "</td>");

                stlDataReport.append("<td>" + record.terminalId + "</td>");

                stlDataReport.append("<td>" + record.appPAN + "</td>");

                stlDataReport.append("<td>" + record.bankId + "</td>");

                stlDataReport.append("<td>" + trxAmount + "</td>");

                stlDataReport.append("<td>" + stlAmount + "</td>");

                stlDataReport.append("<td>" + fee + "</td>");

                stlDataReport.append("<td>" + netAmount + "</td>");

                stlDataReport.append("</tr>\r\n");

                sumAmount += trxAmount;
                sumStlAmount += stlAmount;
                sumNetAmount += netAmount;
                sumFeeAmount += stlFee;
                count++;

                /**********************/
            }

            if(records.size() < ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE))
                break;
        }

        stlDataReport.append("<tr class=tablelastRow>");

        stlDataReport.append("<td  colspan=7 align=center>" + "جمع" + "</td>");

        stlDataReport.append("<td >" + sumAmount + "</td>");

        stlDataReport.append("<td >" + sumStlAmount + "</td>");

        stlDataReport.append("<td >" + sumFeeAmount + "</td>");

        stlDataReport.append("<td >" + sumNetAmount + "</td>");

        stlDataReport.append("</tr>\r\n");

        stlDataReport.append("</table></center></body></html>");


        byte[] b = ZipUtil.getZipByteArray(new String[]{"Rep-"+entity.getNameEn()+"-"+entity.getCode()+"-term-"+((terminal!=null)?terminal.getCode():terminal)+".html"}, new byte[][]{stlDataReport.toString().getBytes()});
        SettlementDataReport sdr = settlementData.addReport(b);
        GeneralDao.Instance.saveOrUpdate(sdr);
    }

    public static String generateATMServiceAndReport(List<String> findReport){
        if(findReport==null || findReport.isEmpty()){
            logger.debug("there is no data for this atm in this time");
//		return null;
        }
        logger.debug("findReport.size:" + findReport.size());

        String atmReport = "";
//	DecimalFormat precision = new DecimalFormat(GlobalContext.getInstance().getRialCurrency().getPattern());

        String reportStr = "<html><head><title>گزارش درآمدزايي خودپردازهاي بانك پاسارگاد</title>" + defaultMeta + defaultStyle + "</head><body class=bodynormal>\r\n";
        atmReport += reportStr;

        reportStr = "<h3>گزارش درآمدزايي خودپردازهاي بانك پاسارگاد</h3>";
//	reportStr = "<h3>" + title + "</h3>";
        atmReport += reportStr;

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
//	reportStr = "<h3 >" + DateUtils.getPersianDateString(DateTime.now().toDate()) + "</h3>";
//	reportStr = "<td dir=ltr>" + dateFormatPers.format(DateTime.now().toDate()) + "</td>";
//	atmReport += reportStr;

        reportStr = "<center><table border=1 cellspacing=0 cellpadding=\"%7\"  class=tablenormal><tr class=tablefirstRow>\r\n";
        atmReport += reportStr;

        reportStr = "<td >" + "ردیف" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "كد شعبه" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "نام پذيرنده" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "ترمينال" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "تعداد برداشت" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "مبلغ برداشت" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "كارمزد برداشت" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "تعداد انتقال" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "كارمزد انتقال" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "تعداد قبض" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "كارمزد قبض" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "تعداد قبض شتابي" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "كارمزد قبض شتابي" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "تعداد مانده حساب" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "كارمزد مانده حساب" + "</td>";
        atmReport += reportStr;

        reportStr = "<td >" + "جمع كارمزدها" + "</td>";
        atmReport += reportStr;

        reportStr = "</tr>\r\n";
        atmReport += reportStr;

        String headerReport = atmReport;

//	Long totalStlAmount = 0L;
//	Long totalStlFee = 0L;
//	Long totalNetAmount = 0L;

        logger.debug("report header part is generated");
        int count=1;

        while(findReport.size()>=count){
            String style = "class=tableOddRow";
            if (count % 2 == 0)
                style = "class=tableEvenRow";
            reportStr = "<tr " + style + ">";
            atmReport += reportStr;


            StringTokenizer tokenizer;
            tokenizer=new StringTokenizer(findReport.get(count-1),"|");

            reportStr = "<td class=tablefirstCol>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim()+ "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim()+ "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "<td>" + tokenizer.nextToken().trim() + "</td>";
            atmReport += reportStr;

            reportStr = "</tr>\r\n";
            atmReport += reportStr;
            count++;
        }
        reportStr = "</table></center>";
        atmReport += reportStr;

        reportStr = "</body></html>";
        atmReport += reportStr;


        return atmReport;
//	String path="D:\test";
//	String pathRes = path+"atmrepost"+System.currentTimeMillis()+".html";
//	File shetabReportRes = new File(pathRes);
//	BufferedWriter errors = null;
//	if(!shetabReportRes.exists()){
//		try {
//			shetabReportRes.createNewFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	errors.append(atmReport);
//	byte[] b = ZipUtil.getZipByteArray(new String[]{"Rep-"+entity.getNameEn()+"-"+entity.getCode()+"-term-"+((terminal!=null)?terminal.getCode():terminal)+".html"}, new byte[][]{stlDataReport.getBytes()});
//		SettlementDataReport sdr = settlementData.addReport(b);
    }


    private static void generateSettlementStateReportNew(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate, String title) throws Exception {

        for (Core core: Core.getAll()) {
            List<SettlementData> settlementDataList = AccountingService.findAllNotSettledSettlementDataUntilTime(terminals, clearingProfile, settleDate, core);
            if (settlementDataList != null && !settlementDataList.isEmpty()) {
                SettlementState settlementState = AccountingService.generateSettlementState(clearingProfile, core, title);
                GeneralDao.Instance.saveOrUpdate(settlementState);
                settlementState.addAllSettlementData(settlementDataList);
            }
        }
    }

    private static void generateSettlementStateReport(ClearingProfile clearingProfile, List<SettlementData> settlementDataList, String title) throws Exception {
        if (settlementDataList == null || settlementDataList.isEmpty()){
            logger.debug("there is no settlementDataList to settle; return");
            return;
        }
        logger.debug("settlementDataList.size:" + settlementDataList.size());

        String settleStateReport = "";
//		DecimalFormat precision = new DecimalFormat(GlobalContext.getInstance().getRialCurrency().getPattern());
        DecimalFormat precision = new DecimalFormat(ProcessContext.get().getRialCurrency().getPattern());

        String reportStr = "<html><head><title>گزارش تسویه</title>" + defaultMeta + defaultStyle + "</head><body class=bodynormal>\r\n";
        settleStateReport += reportStr;

        reportStr = "<h3>گزارش تسویه</h3>";
        reportStr = "<h3>" + title + "</h3>";
        settleStateReport += reportStr;

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyy/MM/dd HH:mm:ss");
//		reportStr = "<h3 >" + DateUtils.getPersianDateString(DateTime.now().toDate()) + "</h3>";
        reportStr = "<td dir=ltr>" + dateFormatPers.format(DateTime.now().toDate()) + "</td>";
        settleStateReport += reportStr;

        reportStr = "<center><table border=1 cellspacing=0 cellpadding=\"%7\"  class=tablenormal><tr class=tablefirstRow>\r\n";
        settleStateReport += reportStr;

        reportStr = "<td >" + "ردیف" + "</td>";
        settleStateReport += reportStr;

        reportStr = "<td >" + "شماره حساب" + "</td>";
        settleStateReport += reportStr;

        reportStr = "<td >" + "کد پذیرنده" + "</td>";
        settleStateReport += reportStr;

        reportStr = "<td >" + "نام پذیرنده" + "</td>";
        settleStateReport += reportStr;

        reportStr = "<td >" + "کد ترمینال" + "</td>";
        settleStateReport += reportStr;

        reportStr = "<td >" + "مبلغ تسويه" + "</td>";
        settleStateReport += reportStr;

        reportStr = "<td >" + "مبلغ کارمزد" + "</td>";
        settleStateReport += reportStr;

        reportStr = "<td >" + "مبلغ واریزی" + "</td>";
        settleStateReport += reportStr;

        reportStr = "</tr>\r\n";
        settleStateReport += reportStr;

        String headerReport = settleStateReport;

        Long totalStlAmount = 0L;
        Long totalStlFee = 0L;
        Long totalNetAmount = 0L;

        logger.debug("report header part is generated");

        int count = 1;

        SettlementState settlementState;
        Map<SettlementState, String> stateReport = new HashMap<SettlementState, String>();
        Map<SettlementState, Long> stateTotalStlAmount = new HashMap<SettlementState, Long>();
        Map<SettlementState, Long> stateTotalStlFee = new HashMap<SettlementState, Long>();
        Map<SettlementState, Long> stateTotalNetAmount = new HashMap<SettlementState, Long>();
        Map<Core, SettlementState> coreSettlementStates = new HashMap<Core, SettlementState>();

        for (SettlementData settlementData : settlementDataList) {
            if (settlementData == null)
                continue;


//			try {
//	    		logger.debug("Try to lock settlementData " + settlementData.getId());
//				settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
//				logger.debug("settlementData locked.... " + settlementData.getId());
//    		} catch (Exception e) {
//				logger.error("Encounter an exception to lock settlementData", e);
//			}


            FinancialEntity entity = settlementData.getFinancialEntity();
            Terminal terminal = settlementData.getTerminal();
            logger.debug("entity: "+entity.getCode()+ " terminal: "+terminal+"("+count+" of "+settlementDataList.size());

            Account account = entity.getOwnOrParentAccount();
            Core core = null;

            if (FinancialEntityRole.BRANCH.equals(entity.getRole())){
                core = Core.FANAP_CORE;
            }else{
                core = account.getCore();
            }

            settlementState = coreSettlementStates.get(core);
            if (settlementState == null) {
                settlementState = AccountingService.generateSettlementState(clearingProfile, core, title);
                coreSettlementStates.put(core, settlementState);
            }
            settlementState.addSettlementData(settlementData);
            GeneralDao.Instance.saveOrUpdate(settlementData);

            settleStateReport = stateReport.get(settlementState);
            if (!Util.hasText(settleStateReport)) {
                settleStateReport = headerReport;
                stateReport.put(settlementState, settleStateReport);
            }

            long stlDataAmount = settlementData.getTotalAmount();
            long stlDataFee = settlementData.getTotalFee();
            long stlDataNetAmount = settlementData.getTotalSettlementAmount();


            /***** Total Settle Amount *****/
            totalStlAmount = stateTotalStlAmount.get(settlementState);
            if (totalStlAmount == null) {
                totalStlAmount = new Long(0L);
            }

            totalStlAmount += stlDataAmount;
            stateTotalStlAmount.put(settlementState, totalStlAmount);

            /***** Total Settle Fee *****/
            totalStlFee = stateTotalStlFee.get(settlementState);
            if (totalStlFee == null) {
                totalStlFee = new Long(0L);
            }

            totalStlFee += stlDataFee;
            stateTotalStlFee.put(settlementState, totalStlFee);

            /***** Total Net Amount *****/
            totalNetAmount = stateTotalNetAmount.get(settlementState);
            if (totalNetAmount == null) {
                totalNetAmount = new Long(0L);
            }

            totalNetAmount += stlDataNetAmount;
            stateTotalNetAmount.put(settlementState, totalNetAmount);


            String style = "class=tableOddRow";
            if (count % 2 == 0)
                style = "class=tableEvenRow";
            reportStr = "<tr " + style + ">";
            settleStateReport += reportStr;

            reportStr = "<td>" + (count++) + "</td>";
            settleStateReport += reportStr;


            Account acc = entity.getOwnOrParentAccount();
            logger.debug("account: "+acc);

            if (clearingProfile != null)
                acc = ClearingService.getSettlementService(clearingProfile).getAccount(settlementData);

            reportStr = "<td dir=ltr>" +
                    acc
//			(account != null ? account.getAccountNumber() : "-") 
                    + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + entity.getCode() + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td class=tablefirstCol>" + (entity.getName() != null ? entity.getName() : "-") + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + ((terminal == null)? "-": terminal.getCode().toString() )+ "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + precision.format(stlDataAmount) + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + stlDataFee + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + precision.format(stlDataNetAmount) + "</td>";
            settleStateReport += reportStr;

            reportStr = "</tr>\r\n";
            settleStateReport += reportStr;

            stateReport.put(settlementState, settleStateReport);
        }

        for (SettlementState state: stateReport.keySet()) {

            totalNetAmount = stateTotalNetAmount.get(state);
            totalStlAmount = stateTotalStlAmount.get(state);
            totalStlFee = stateTotalStlFee.get(state);
            settleStateReport = stateReport.get(state);

            reportStr = "<tr class=tablelastRow>";
            settleStateReport += reportStr;

            reportStr = "<td  colspan=5 align=center>" + "جمع" + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + precision.format(totalStlAmount) + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + totalStlFee + "</td>";
            settleStateReport += reportStr;

            reportStr = "<td>" + precision.format(totalNetAmount) + "</td>";
            settleStateReport += reportStr;

            reportStr = "</tr>\r\n";
            settleStateReport += reportStr;

            reportStr = "</table></center>";
            settleStateReport += reportStr;

            reportStr = "</body></html>";
            settleStateReport += reportStr;

            state.setReport(settleStateReport);
//			state.setDocumentAmount(totalNetAmount);
            GeneralDao.Instance.saveOrUpdate(state);
        }
    }

    public static void issueFanapSettlementStateReport(SettlementState settlementState, String docDesc) throws Exception{
        generateFinalFanapSettlementStateOrDataReport(settlementState, null, null, docDesc, false, false, null);
    }

    public static void issueFanapSettlementDataReport(List<SettlementData> settlementData, String docDesc, DateTime settleDate/*, LockMode lockMode*/) throws Exception{
        generateFinalFanapSettlementStateOrDataReport(null, settlementData, settleDate, docDesc, false, false, null);
//    	generateFinalFanapSettlementDataReport(settlementData, docDesc, settleDate);
    }

    public static void issueFanapSettlementDataReport(List<SettlementData> settlementData, String docDesc, DateTime settleDate, LockMode lockMode) throws Exception{
        generateFinalFanapSettlementStateOrDataReport(null, settlementData, settleDate, docDesc, false, false, lockMode);
//    	generateFinalFanapSettlementDataReport(settlementData, docDesc, settleDate);
    }

//    private static void generateFinalNeginSettlementStateOrDataReport(SettlementState settlementState, List<SettlementData> settlementDatas,DateTime stlDate, String docDesc,Boolean onlyFee, Boolean isForced) throws Exception{
//    	if(settlementState != null && settlementState.getSettlementDatas() == null){
//    		logger.error("no settlement data found for settlement state: "+ settlementState.getId());
//    		return;   		
//    	}
//    	if (settlementState == null  && 
//    			(settlementDatas == null || settlementDatas.size() == 0)) {
//			logger.error("no settlement data found for online settlement." );    			
//    		return;
//    	}
//    	
//    }


    public static String  perTransactionDocumentDescription(Ifx ifx, Shop shop, String sharhesanad){

        String groovyStr= "";
        String groovy = "";
//    	String tempStr = "";

        StringBuilder commentOfDocumentItem = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(sharhesanad , "|");
//    	StringTokenizer secTokenizer ;

        while(tokenizer.hasMoreTokens()){
//    		secTokenizer = new StringTokenizer(tokenizer.nextToken(), ":");
//    		tempStr = "";
            groovy = tokenizer.nextToken();
            if(Util.hasText(groovy)){
                if(groovy.contains("!")){
                    commentOfDocumentItem.append(groovy.substring(0,groovy.indexOf("!")));
                    groovyStr = groovy.substring(groovy.indexOf("!")+1);
                }
            }

            Binding scriptBinding = new Binding();

            if (shop != null)
                scriptBinding.setProperty("shop", shop);

            if (ifx != null)
                scriptBinding.setProperty("ifx", ifx);

            Object run = null;
            try {
                run = GlobalContext.getInstance().evaluateScript(groovyStr, scriptBinding);
            } catch(Exception e) {
                logger.error("Exception in perTrxDocDesc evaluateScript");
            }

            if (run != null) {
                commentOfDocumentItem.append(run.toString());
            }
        }
        return commentOfDocumentItem.toString();
    }

    private static void generateFinalFanapSettlementStateOrDataReport(SettlementState settlementState, List<SettlementData> settlementDatas, DateTime stlDate, String docDesc, Boolean onlyFee, Boolean isForced, LockMode lockMode) throws Exception {

        if (settlementState != null && settlementState.getSettlementDatas() == null) {
            logger.error("no settlement data found for settlement state: " + settlementState.getId());
            return;
        }

        if (settlementState == null  &&
                (settlementDatas == null || settlementDatas.size() == 0)) {
            logger.warn("no settlement data found for online settlement." );
            return;
        }
        Long stlStateTotalAmount = 0L;
        Long stlStateTotalFee = 0L;

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");

//    	List<DepositInfoForIssueDocument> depositItemList = new Vector<DepositInfoForIssueDocument>();
        List<SettlementData> settlementDataForUpdate = new ArrayList<SettlementData>();

        Map<SettlementData, List<DepositInfoForIssueDocument>> stlDataDepositList = new HashMap<SettlementData, List<DepositInfoForIssueDocument>>();
        Map<SettlementData, List<DocumentItemEntity>> stlDataDocumentList = new HashMap<SettlementData, List<DocumentItemEntity>>();

        List<DepositInfoForIssueDocument> reconcileDepositItemList = new Vector<DepositInfoForIssueDocument>();
        List<DocumentItemEntity> reconcileDocumentItemList = new Vector<DocumentItemEntity>();

        List<DepositInfoForIssueDocument> depositItems = new Vector<DepositInfoForIssueDocument>();
        List<DocumentItemEntity> documentItems = new Vector<DocumentItemEntity>();


        boolean debtor = true;
        boolean isReconcile = false;
        DepositActionType actionType = DepositActionType.Debtor_Deposit;
//    	DepositInfoForIssueDocument deposit = null;
//    	DocumentItemEntity documentItemEntity = null;
        Boolean isSettled = true;

        int i=0;
        int size = settlementState != null ? settlementState.getSettlementDatas().size() : settlementDatas.size();

        if (settlementState != null &&
                (settlementDatas == null || settlementDatas.size() == 0)) {
            settlementDatas = settlementState.getSettlementDatas();
        }

        DateTime date = null;

        for (SettlementData settlementData: /*settlementState.getSettlementDatas()*/settlementDatas) {
            Boolean isDeposit = true;

            DepositInfoForIssueDocument deposit = null;
            DocumentItemEntity documentItemEntity = null;

            depositItems = new Vector<DepositInfoForIssueDocument>();
            documentItems = new Vector<DocumentItemEntity>();

            if (settlementData == null)
                continue;

            if (Util.hasText(settlementData.getDocumentNumber())) {
                logger.info("settlementData: " + settlementData.getId() + " has documentNum");
                continue;
            }

//			if (lockMode != null && LockMode.UPGRADE_NOWAIT.equals(lockMode)) {
            try {
                logger.debug("Try to lock settlementData " + settlementData.getId());
                settlementData = GeneralDao.Instance.load(SettlementData.class, settlementData.getId());
                settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData, LockMode.UPGRADE_NOWAIT);
            } catch (Exception e) {
                logger.error("Encounter an exception to lock settlementData", e);
                continue;
            }
//			} else {
//
//				try {
//					logger.debug("Try to lock settlementData " + settlementData.getId());
//					settlementData = (SettlementData) GeneralDao.Instance.optimizedSynchObject(settlementData);
//					// logger.debug("settlementData locked.... " + settlementData.getId());
//				} catch (Exception e) {
//					logger.error("Encounter an exception to lock settlementData", e);
//					continue;
//				}
//			}

            if (date == null)
                date = settlementData.getSettlementTime();
            if (date.before(settlementData.getSettlementTime()))
                date = settlementData.getSettlementTime();

            if (!isForced && Util.hasText(settlementData.getDocumentNumber())){
                logger.debug("settledata ignored: !isForced && Util.hasText(settlementData.getDocumentNumber())");
                continue;
            }

            if (onlyFee && settlementData.getTotalAmount()!=0){
                logger.debug("settledata ignored: onlyFee && settlementData.getTotalAmount()!=0");
                continue;
            }

            if (((Long)settlementData.getTotalSettlementAmount()).equals(0L)){
                logger.debug("settledata ignored: ((Long)settlementData.getTotalSettlementAmount()).equals(0L)");
                settlementData.setDocumentNumber("---");
                GeneralDao.Instance.saveOrUpdate(settlementData);
                continue;
            }

            isSettled = false;

            logger.debug("settlementData: " + settlementData.getId() +"( "+i+" of "+size+" )");
            FinancialEntity entity = settlementData.getFinancialEntity();
            logger.debug("entity: " + entity.getId());

            Terminal terminal = settlementData.getTerminal();
            if (terminal!=null)
                logger.debug("terminal: " + terminal.getId());


            /************* LEILA: for second account of IranCell **********/
            Account account = entity.getOwnOrParentAccount();
            if (settlementData.getClearingProfile() != null)
                account = ClearingService.getSettlementService(settlementData.getClearingProfile()).getAccount(settlementData);
            /************* LEILA: for second account of IranCell **********/

            String accountId = /*entity.getOwnOrParentAccount()*/account.getAccountNumber();
            AccountType accType = /*entity.getOwnOrParentAccount()*/account.getType();
            accountId = fanapAccountFormat(accountId, accType);
            logger.debug("accountId: " + accountId);

            ClearingProfile clearingProfile = settlementData.getClearingProfile();
            String clrProfDesc = "";
            if (clearingProfile != null) {
                settlementDataForUpdate.add(settlementData);
//				stlDataDepositList.get(settlementData);
                clrProfDesc = ClearingService.getDocDesc(settlementData);
                isReconcile = false;
            }
            else {
                clrProfDesc = "مغايرت";
                isReconcile = true;
            }

            String commentOfDocumentItem = "";

            Long totalStlDataAmount = settlementData.getTotalSettlementAmount();
            Long totalFee = settlementData.getTotalFee();
            stlStateTotalFee += totalFee;
            stlStateTotalAmount += settlementData.getTotalAmount();

            //Mirkamali(Task179): Currency ATM
            if (FinancialEntityRole.ORGANIZATION.equals(entity.getRole()) || FinancialEntityRole.BRANCH.equals(entity.getRole())) {
            	logger.debug("entity is Branch only for Currency ATM not for normal ATMs");
                String descOrg = entity.getName() + " ";

                /*********    ORGANIZATION Fee in other description item document: Start     ********/
                Long totalStlDataFee = settlementData.getTotalFee();
                if (!totalStlDataFee.equals(0L)) {

                    commentOfDocumentItem += descOrg + "بابت کارمزد " + clrProfDesc +" مورخ "+dateFormatPers.format(settlementData.getSettlementTime().toDate());
                    if (totalStlDataFee > 0) {
                        debtor = false;
                        actionType = DepositActionType.Creditor_Deposit;
                    } else {
                        debtor = true;
                        actionType = DepositActionType.Debtor_Deposit;
                        totalStlDataFee *= -1;
                    }
                    if (AccountType.ACCOUNT.equals(accType) || AccountType.TOPIC.equals(accType)) {
                        //honarmand kiosk
                        String branchCode = "";//995 bashe behtare!
                        if (settlementData.getClearingProfile() != null)
                            branchCode = ClearingService.getSettlementService(settlementData.getClearingProfile()).getBranchCode(accountId, accType, settlementData);
                        //honarmand

                        documentItemEntity = new DocumentItemEntity(new Double(totalStlDataFee), debtor, branchCode,
                                Util.ansiFormat(commentOfDocumentItem), accountId, DocumentItemEntityType.Account);
                        isDeposit = false;

                    } else {
                        deposit = new DepositInfoForIssueDocument(accountId, actionType, new Double(totalStlDataFee), Util.ansiFormat(commentOfDocumentItem));
                        logger.debug("Account: " + accountId + ((debtor)? " debited":" credited" )+" by the value of: " + totalStlDataFee);
                    }

                    if (isReconcile) {
                        if (isDeposit)
                            reconcileDepositItemList.add(deposit);
                        else
                            reconcileDocumentItemList.add(documentItemEntity);

                    } else {
                        if (isDeposit)
                            depositItems.add(deposit);
                        else
                            documentItems.add(documentItemEntity);
                    }

                }
                /*********    ORGANIZATION Fee in other description item document: End     ********/

                commentOfDocumentItem = descOrg;

                /**** set other BAN e SANAD, mablaghe bedoon e kasre karmozd! (1389/11/02 ****/
                totalStlDataAmount = settlementData.getTotalAmount();
                /**** ****/

            } else {
                commentOfDocumentItem += entity.getRole().getName() + " " + entity.getName().replaceAll("&", "&amp;") + " ";
                if (terminal != null) {
                    commentOfDocumentItem += "ترمينال " + terminal.getCode();
                }
            }

            try {
                commentOfDocumentItem += " بابت  " + clrProfDesc;
            } catch (Exception e) {
                logger.debug("Exception in getting Document Description of SettlementData: " + settlementData.getId());
                commentOfDocumentItem += " بابت تسويه " + docDesc;
            }

            commentOfDocumentItem += " مورخ " + dateFormatPers.format(settlementData.getSettlementTime().toDate());

            commentOfDocumentItem +=" , تعداد تراکنشها " + settlementData.getNumTransaction();

            String dumyComment = commentOfDocumentItem;


            if(terminal != null && terminal.getDocumentDescriptionPattern() != null && settlementData.getTransactions()!= null){
////		if(trx2.get(0).getOutgoingIfx().getEndPointTerminal().getDocumentDescription() != null){
                if(terminal != null && terminal.getDocumentDescriptionPattern() != null){
                    Set<Transaction> trx = settlementData.getTransactions();
                    List<Transaction> trx2 = new ArrayList<Transaction>();
                    trx2.addAll(trx);
                    commentOfDocumentItem = new String();
                    logger.debug("Using Per TransaCtion Method For Sharhe Sanad...");

                    Shop shop = null;
                    if (FinancialEntityRole.SHOP.equals(settlementData.getFinancialEntity().getRole())) {
                        shop = GeneralDao.Instance.load(Shop.class, settlementData.getFinancialEntityId());
//					shop = (Shop) settlementData.getFinancialEntity();
                    }

                    try {
                        commentOfDocumentItem = perTransactionDocumentDescription(trx2.get(0).getIncomingIfx(), shop, terminal.getDocumentDescriptionPattern());
                    } catch(Exception e) {
                        logger.error("Exception in getting document description from terminal pattern(terminal: " + terminal.getCode() + ")", e);
                        commentOfDocumentItem = dumyComment;
                    }
                }
            }

            if (totalStlDataAmount > 0) {
                debtor = false;
                actionType = DepositActionType.Creditor_Deposit;
            } else {
                debtor = true;
                actionType = DepositActionType.Debtor_Deposit;
                totalStlDataAmount *= -1;
            }

            if (AccountType.ACCOUNT.equals(accType) || AccountType.TOPIC.equals(accType)) {
                //String branchCode = getBranchCode(accountId, accType);
                //honarmand kiosk
                String branchCode = "";//995 bashe behtare!
                if (settlementData.getClearingProfile() != null)
                    branchCode = ClearingService.getSettlementService(settlementData.getClearingProfile()).getBranchCode(accountId, accType, settlementData);
                //honarmand
                documentItemEntity = new DocumentItemEntity(new Double(totalStlDataAmount), debtor, branchCode,
                        Util.ansiFormat(commentOfDocumentItem), accountId, AccountType.ACCOUNT.equals(accType) ? DocumentItemEntityType.Account : DocumentItemEntityType.Topic);
                isDeposit = false;

            } else {
                deposit = new DepositInfoForIssueDocument(accountId, actionType, new Double(totalStlDataAmount), Util.ansiFormat(commentOfDocumentItem));
                logger.debug("Account: " + accountId + ((debtor)? " debited":" credited" )+" by the value of: " + totalStlDataAmount);
            }

//			deposit = new DepositInfoForIssueDocument(accountId, actionType, new Double(totalStlDataAmount), Util.ansiFormat(commentOfDocumentItem));
//			logger.debug("Account: " + accountId + ((debtor)? " debited":" credited" )+" by the value of: " + totalStlDataAmount);

            if (isReconcile) {
                if (isDeposit)
                    reconcileDepositItemList.add(deposit);
                else
                    reconcileDocumentItemList.add(documentItemEntity);


            } else {
                if (isDeposit) {
                    depositItems.add(deposit);
                    stlDataDepositList.put(settlementData, depositItems);
                } else {
                    documentItems.add(documentItemEntity);
                    stlDataDocumentList.put(settlementData, documentItems);
                }
            }

            if (isReconcile) {
                Long amount = settlementData.getTotalAmount() + settlementData.getTotalFee();
                List<SettlementData> list = new ArrayList<SettlementData>();
                list.add(settlementData);
                if (settlementState == null)
                    issueFanapDocument(list, "رفع مغايرت", docDesc, amount, reconcileDepositItemList, reconcileDocumentItemList, stlDate);
                else
                    issueFanapDocument(settlementState, list, "رفع مغايرت", docDesc, amount, reconcileDepositItemList, reconcileDocumentItemList, settlementData.getSettlementTime());
            }

            i++;
        }

        Long amount = stlStateTotalAmount + stlStateTotalFee;
        if (!amount.equals(0L)){
            isSettled = false;
            DateTime newDate = date;
            if (stlDate != null)
                newDate = stlDate;
            issueFanapDocumentNew(settlementState, stlDataDepositList, stlDataDocumentList, "تسويه حساب", docDesc, amount, newDate/*settleDate*/);
        }

        if (onlyFee || !amount.equals(0L)){
            isSettled = false;
        }

        if (settlementState != null && isSettled) {
            AccountingService.settleEmptySettlementState(settlementState);
        }
    }

    private static void issueFanapDocument(SettlementState settlementState, List<SettlementData> settlementDatas, String docCause, String docDesc, Long amount,
                                           List<DepositInfoForIssueDocument> depositList,
                                           List<DocumentItemEntity> documentList,
                                           DateTime settleDate) throws BusinessException {
        boolean debtor;
        DocumentItemEntityType topic;
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String persionFormat = dateFormatPers.format(settleDate.toDate());
        String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
        List<DocumentItemEntity> documentItemList = new Vector<DocumentItemEntity>();

        if (amount >= 0) {
            debtor = true;
        } else {
            debtor = false;
            amount *= -1;
        }

        String branchCode = switchBranchId;

        if (depositList != null && depositList.size() > 0) {
            for (DepositInfoForIssueDocument item: depositList) {
                branchCode = getBranchCode(item.getDepositNumber(), AccountType.DEPOSIT);
            }
        }

        if (documentList != null && documentList.size() > 0) {
            for (DocumentItemEntity item: documentList) {
                branchCode = item.getBranchCode();
                documentItemList.add(item);
            }
        }

//		Account fanapAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
        Account fanapAccount = ProcessContext.get().getMyInstitution().getAccount();
        if (AccountingService.isTopic(fanapAccount.getAccountNumber()))
            topic = DocumentItemEntityType.Topic;
        else
            topic = DocumentItemEntityType.Account;

        DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(amount), debtor, /*switchBranchId, */branchCode, docCause +  " پذیرندگان فناپ مورخ " + persionFormat,
                fanapAccount.getAccountNumber(), topic);
        documentItemList.add(documentItemEntity);

        Ifx ifx = null;
        String trxNumPattern = null;
        List<SettlementData> list = new ArrayList<SettlementData>();
        if (settlementState != null) {
            list = settlementState.getSettlementDatas();
        } else if (settlementDatas != null && settlementDatas.size() > 0) {
            list = settlementDatas;
        }

        Shop shop = null;
        if (list.get(0).getTerminal() != null &&
                FinancialEntityRole.SHOP.equals(list.get(0).getFinancialEntity().getRole()) &&
                list.get(0).getTerminal().getTransactionNumberPattern() != null) {

            List<Transaction> trxList = new ArrayList<Transaction>();
            trxList.addAll(list.get(0).getTransactions());

            ifx = trxList.get(0).getIncomingIfx();
            trxNumPattern = list.get(0).getTerminal().getTransactionNumberPattern();
            shop = GeneralDao.Instance.load(Shop.class, list.get(0).getFinancialEntityId());
//			shop = (Shop) list.get(0).getFinancialEntity();
        }

        Pair<String, String> document = AccountingService.generateFCBDocument(docCause + "- " +  docDesc +"- " +"پذیرندگان فناپ مورخ " + persionFormat, documentItemList, depositList
                ,"stlState-"+settlementState.getId(), ifx, shop, trxNumPattern);
        SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
        settlementState.addSettlementReport(report);
        GeneralDao.Instance.saveOrUpdate(settlementState);
        String transactionId = AccountingService.issueFCBDocument(report, true);
        report.setDocumentNumber(transactionId);
        GeneralDao.Instance.saveOrUpdate(report);

        SettlementStateType stateType = SettlementStateType.AUTOSETTLED;
        if (transactionId != null) {

            if (OnlineSettlementService.class.equals(settlementState.getClearingProfile().getSettlementClass())
                    && settlementDatas!= null && !settlementDatas.isEmpty()){
                List<SettlementData> data = new ArrayList<SettlementData>();
                for (SettlementData d : settlementDatas){
                    if (d.getTotalAmount() != 0 )
                        data.add(d);
                }
                settlementDatas.removeAll(data);
            }

            int updateSettlementData = AccountingService.updateSettlementData(settlementDatas, transactionId);
            logger.debug(updateSettlementData + " settlementData are settled in document-" + transactionId);
            int updateSettlementInfo = AccountingService.updateSettlementInfo(settlementDatas, SettledState.SETTLED);
            logger.debug(updateSettlementInfo + " settlementInfo are settled in document-" + transactionId);
        }else
            stateType = SettlementStateType.FILECREATED;

        settlementState.setState(stateType);
        settlementState.setSettlementFileCreationDate(DateTime.now());
        settlementState.setSettlementDate(DateTime.now());
        settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
        GeneralDao.Instance.saveOrUpdate(settlementState);
    }

    private static void issueFanapDocumentNew(SettlementState settlementState,
                                              Map<SettlementData, List<DepositInfoForIssueDocument>> stlDataDepositItems,
                                              Map<SettlementData, List<DocumentItemEntity>> stlDataDocumentItems,
                                              String docCause, String docDesc, Long amount, DateTime settleDate) throws BusinessException {
        boolean debtor;
        DocumentItemEntityType topic;
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String persionFormat = dateFormatPers.format(settleDate.toDate());
        String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
        List<DocumentItemEntity> documentItemList = new Vector<DocumentItemEntity>();

        logger.debug("issueFanapDocNew starting...");

//		Account fanapAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
        Account fanapAccount = ProcessContext.get().getMyInstitution().getAccount();
        if (AccountingService.isTopic(fanapAccount.getAccountNumber()))
            topic = DocumentItemEntityType.Topic;
        else
            topic = DocumentItemEntityType.Account;


        Set<SettlementData> stlDataSet = new HashSet<SettlementData>();
        int size = 0;

        if (stlDataDepositItems != null && stlDataDepositItems.keySet() != null && stlDataDepositItems.size() > 0) {
            stlDataSet.addAll(stlDataDepositItems.keySet());
            size += stlDataDepositItems.size();
        }

        if (stlDataDocumentItems != null && stlDataDocumentItems.keySet() != null && stlDataDocumentItems.size() > 0) {
            stlDataSet.addAll(stlDataDocumentItems.keySet());
            size += stlDataDocumentItems.size();
        }


        SettlementData[] sortedSettlementData = new SettlementData[size];
        stlDataSet.toArray(sortedSettlementData);

//		stlDataDepositItems.keySet().toArray(sortedSettlementData);
//		stlDataDocumentItems.keySet().toArray(sortedSettlementData);
        Arrays.sort(sortedSettlementData, new Comparator<SettlementData>(){
            @Override
            public int compare(SettlementData arg0, SettlementData arg1) {
                if(arg0.getId() > arg1.getId())
                    return 1;
                if(arg0.getId() < arg1.getId())
                    return -1;

                return 0;
            }
        });

        for (SettlementData settlementData: sortedSettlementData) {

            if (settlementData.getSettlementReport() != null) {
                logger.warn("settlementData: " + settlementData.getId() + " has report: "
                        + settlementData.getSettlementReport().getId() + " !!!");
                continue;
            }

            try {
                logger.debug("Try to lock settlementData " + settlementData.getId());
                settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
                logger.debug("settlementData locked.... " + settlementData.getId());
            } catch (Exception e) {
                logger.error("Encounter an exception to lock settlementData", e);
            }

            amount = 0L;
            documentItemList = new Vector<DocumentItemEntity>();

            List<DepositInfoForIssueDocument> depositList = null;
            List<DocumentItemEntity> documentList = null;

            if (stlDataDepositItems != null && stlDataDepositItems.size() > 0) {
                depositList = stlDataDepositItems.get(settlementData);

            }

            if (stlDataDocumentItems != null && stlDataDocumentItems.size() > 0) {
                documentList = stlDataDocumentItems.get(settlementData);

            }

            boolean isDeposit = true;

            if (depositList == null && documentList != null) {
                isDeposit = false;
            }

            String branchCode = switchBranchId;

            if (isDeposit) {
                for (DepositInfoForIssueDocument item: depositList) {
                    String depositNumber = item.getDepositNumber();
                    branchCode = getBranchCode(depositNumber, AccountType.DEPOSIT);
//					String depositNumber = "";
//					try {
//						String[] split = depositNumber.split("\\.");
//						if (Util.hasText(split[0]))
//							branchCode = split[0];
//					} catch (Exception e) {
//						logger.info("exception in getting branchCode, set 995");
//						branchCode = switchBranchId;
//					}
                    logger.debug("settlementData: " + settlementData.getId() + ", accountNum: " + depositNumber + ", branchCode: " + branchCode);

                    if (DepositActionType.Creditor_Deposit.equals(item.getType()))
                        amount += Long.parseLong(item.getAmount());
                    else if (DepositActionType.Debtor_Deposit.equals(item.getType()))
                        amount -= Long.parseLong(item.getAmount());
                }
            } else {
                for (DocumentItemEntity item: documentList) {
                    String depositNumber = item.getIdentifier();
                    branchCode = item.getBranchCode();

                    documentItemList.add(item);

//					String depositNumber = "";
//					try {
//						String[] split = depositNumber.split("\\.");
//						if (Util.hasText(split[0]))
//							branchCode = split[0];
//					} catch (Exception e) {
//						logger.info("exception in getting branchCode, set 995");
//						branchCode = switchBranchId;
//					}
                    logger.debug("settlementData: " + settlementData.getId() + ", accountNum: " + depositNumber + ", branchCode: " + branchCode);

                    if (!item.isDebtor()) {
                        amount += Long.parseLong(item.getAmount());

                    } else {
                        amount -= Long.parseLong(item.getAmount());
                    }
//					if (DepositActionType.Creditor_Deposit.equals(item.getEntityType()))
//					else if (DepositActionType.Debtor_Deposit.equals(item.getType()))
                }
            }


            if (amount >= 0) {
                debtor = true;

            } else {
                debtor = false;
                amount *= -1;
            }

            SettlementReport report = null;

            if (!amount.equals(0L)) {
                DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(amount), debtor, branchCode, docCause +  " پذیرندگان فناپ مورخ " + persionFormat,
                        fanapAccount.getAccountNumber(), topic);
                documentItemList.add(documentItemEntity);


                Ifx ifx = null;
                String trxNumPattern = null;
                Shop shop = null;

                if (settlementData.getTerminal() != null &&
						/*FinancialEntityRole.SHOP.equals(settlementData.getFinancialEntity().getRole()) &&*/
                        settlementData.getTerminal().getTransactionNumberPattern() != null) {

                    List<Transaction> trxList = new ArrayList<Transaction>();
                    trxList.addAll(settlementData.getTransactions());
                    ifx = trxList.get(0).getIncomingIfx();
                    trxNumPattern = settlementData.getTerminal().getTransactionNumberPattern();
                    if (FinancialEntityRole.SHOP.equals(settlementData.getFinancialEntity().getRole()))
                        shop = GeneralDao.Instance.load(Shop.class, settlementData.getFinancialEntityId());

//					shop = (Shop) settlementData.getFinancialEntity();
                }

                Pair<String, String> document = AccountingService.generateFCBDocument(docCause + "- " +  docDesc +"- " +"پذیرندگان فناپ مورخ " + persionFormat, documentItemList, depositList
                        ,"stlData-"+settlementData.getId(), ifx, shop, trxNumPattern);

                report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
                if (settlementState != null) {
                    settlementState.addSettlementReport(report);
                    GeneralDao.Instance.saveOrUpdate(settlementState);
                }
                settlementData.setSettlementReport(report);

                List<SettlementData> list = new ArrayList<SettlementData>();
                list.add(settlementData);
//				int updateSettlementInfo = AccountingService.updateSettlementInfo(list, report);
//				logger.debug("settlementReport" + report.getId()+ "was set on "+ updateSettlementInfo + " settlementInfo");

                GeneralDao.Instance.saveOrUpdate(report);
                GeneralDao.Instance.saveOrUpdate(settlementData);
            }
        }

        if (settlementState != null) {
            SettlementStateType stateType = SettlementStateType.AUTOSETTLED;
            settlementState.setState(stateType);

            if (settlementState.getSettlementFileCreationDate() == null)
                settlementState.setSettlementFileCreationDate(DateTime.now());

            if (settlementState.getSettlementDate() == null)
                settlementState.setSettlementDate(DateTime.now());

            settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
            GeneralDao.Instance.saveOrUpdate(settlementState);
        }

        GeneralDao.Instance.endTransaction();

        int MAX_THREADS = 1;
        if (sortedSettlementData.length > 300)
            MAX_THREADS = 2;

        int subListSize = (int) Math.ceil(sortedSettlementData.length / MAX_THREADS);
        for (int i = 0; i < MAX_THREADS; i++) {
            SettlementData[] subSortedArray = Arrays.copyOfRange(sortedSettlementData, i*subListSize, Math.min((i+1)*subListSize, sortedSettlementData.length));

            IssueFCBThread issueFCBThread = new IssueFCBThread(/*sortedSettlementData*/subSortedArray);
            Thread issueThread = new Thread(issueFCBThread);
            logger.debug("Thread: " + issueThread.getName() + " is starting...");
            issueThread.start();
        }

        GeneralDao.Instance.beginTransaction();
    }

    private static void issueFanapDocument(List<SettlementData> settlementData, String docCause, String docDesc, Long amount,
                                           List<DepositInfoForIssueDocument> depositList,
                                           List<DocumentItemEntity> documentList, DateTime settleDate) throws BusinessException {
        boolean debtor;
        DocumentItemEntityType topic;
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String persionFormat = dateFormatPers.format(settleDate.toDate());
        String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
        List<DocumentItemEntity> documentItemList = new Vector<DocumentItemEntity>();

        if (amount >= 0) {
            debtor = true;
        } else {
            debtor = false;
            amount *= -1;
        }


        /**** set merchant branch code for Online Settlement ****/
        String branchCode = switchBranchId;
        if (depositList != null && depositList.size() > 0) {
            for (DepositInfoForIssueDocument item: depositList) {
                String depositNumber = item.getDepositNumber();
                branchCode = getBranchCode(depositNumber, AccountType.DEPOSIT);
//				String depositNumber = "";
//				try {
//				String[] split = depositNumber.split("\\.");
//				if (Util.hasText(split[0]))
//						branchCode = split[0];
//				} catch (Exception e) {
//					logger.info("exception in getting branchCode, set 995");
//					branchCode = switchBranchId;
//				}
                logger.debug("settlementData: " + settlementData.get(0).getId() + ", accountNum: " + depositNumber + ", branchCode: " + branchCode);
            }
        }

        if (documentList != null && documentList.size() > 0) {
            for (DocumentItemEntity item : documentList) {
                branchCode = item.getBranchCode();
                documentItemList.add(item);
            }
        }

//		Account fanapAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
        Account fanapAccount = ProcessContext.get().getMyInstitution().getAccount();
        if (AccountingService.isTopic(fanapAccount.getAccountNumber()))
            topic = DocumentItemEntityType.Topic;
        else
            topic = DocumentItemEntityType.Account;
        DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(amount), debtor, branchCode, docCause +  " پذیرندگان فناپ مورخ " + persionFormat,
                fanapAccount.getAccountNumber(), topic);
        documentItemList.add(documentItemEntity);

        SettlementState settlementState = settlementData.get(0).getSettlementState();
        String id = "stlData-"+settlementData.get(0).getId();
        if (settlementData.size()>1){
            for (int i=1; i <settlementData.size();i++){
                id += "-"+ settlementData.get(i).getId();
            }
        }

        Ifx ifx = null;
        String trxNumPattern = null;
        Shop shop = null;
        if (settlementData.get(0).getTerminal() != null &&
                FinancialEntityRole.SHOP.equals(settlementData.get(0).getFinancialEntity().getRole()) &&
                settlementData.get(0).getTerminal().getTransactionNumberPattern() != null) {

            List<Transaction> trxList = new ArrayList<Transaction>();
            trxList.addAll(settlementData.get(0).getTransactions());

            ifx = trxList.get(0).getIncomingIfx();
            trxNumPattern = settlementData.get(0).getTerminal().getTransactionNumberPattern();
            shop = GeneralDao.Instance.load(Shop.class, settlementData.get(0).getFinancialEntityId());
//			shop = (Shop) settlementData.get(0).getFinancialEntity();
        }


        Pair<String, String> document = AccountingService.generateFCBDocument(docCause + "- " +  docDesc +"- " +"پذیرندگان فناپ مورخ " + persionFormat, documentItemList, depositList
                ,id, ifx, shop, trxNumPattern);
        SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
        if (settlementState!= null){
            settlementState.addSettlementReport(report);
            GeneralDao.Instance.saveOrUpdate(settlementState);

        } else {
            int updateSettlementData = AccountingService.updateSettlementData(settlementData, report);
            logger.debug("settlementReport" + report.getId()+ "was set on "+ updateSettlementData + " settlementData");

//			int updateSettlementInfo = AccountingService.updateSettlementInfo(settlementData, report);
//			logger.debug("settlementReport" + report.getId()+ "was set on "+ updateSettlementInfo + " settlementInfo");
        }

        String transactionId = AccountingService.issueFCBDocument(report, true);
        report.setDocumentNumber(transactionId);
        GeneralDao.Instance.saveOrUpdate(report);

        if (transactionId != null) {
//			AccountingService.removeSettlementRecord(settlementData);

            int updateSettlementData = AccountingService.updateSettlementData(settlementData, transactionId);
            logger.debug(updateSettlementData + " settlementData are settled in document-" + transactionId);

            int updateSettlementInfo = AccountingService.updateSettlementInfo(settlementData, SettledState.SETTLED);
            logger.debug(updateSettlementInfo + " settlementInfo are settled in document-" + transactionId);
        }
    }

    private static void generateFinalNeginSettlementStateReport(SettlementState settlementState, String docDesc, /*DateTime settleDate,*/
                                                                Boolean onlyFee, Boolean isForced) throws Exception {

        String settlementReport = "";
//    	DecimalFormat precision = new DecimalFormat(GlobalContext.getInstance().getRialCurrency().getPattern());
        DecimalFormat precision = new DecimalFormat(ProcessContext.get().getRialCurrency().getPattern());

        if (settlementState.getSettlementDatas() == null) {
            logger.error("no settlement data found for settlement state: " + settlementState.getId());
            return;
        }

        int docSize = 0;

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
//		String persionFormat = dateFormatPers.format(settleDate.toDate());

        Long newDocumentAmount = 0L;
        Long stlStateTotalAmount = 0L;
        Long stlStateTotalFee = 0L;

        String debitLine = "";
        String creditLine = "";

        String finalDocDesc = "";
        int i =0;
        int size = settlementState.getSettlementDatas().size();

        String clrProfName = Util.ansiFormat( "برای " + docDesc+" ");
        if (settlementState.getClearingProfile() != null)
            clrProfName = Util.ansiFormat("برای الگوی "+ settlementState.getClearingProfile().getName());
        String bankDocDesc = "بابت تسويه حساب با دارندگان حسابهای نگينی " + clrProfName;

        DateTime date = null;

        for (SettlementData settlementData: settlementState.getSettlementDatas()) {
            if (settlementData == null)
                continue;

            if (date == null)
                date = settlementData.getSettlementTime();
            if (date.before(settlementData.getSettlementTime()))
                date = settlementData.getSettlementTime();

            if (!isForced && Util.hasText(settlementData.getDocumentNumber()))
                continue;

            if (onlyFee && settlementData.getTotalAmount()!=0)
                continue;

            if (((Long)settlementData.getTotalSettlementAmount()).equals(0L))
                continue;

//    		try {
//	    		logger.debug("Try to lock settlementData " + settlementData.getId());
//				settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData);
//				logger.debug("settlementData locked.... " + settlementData.getId());
//    		} catch (Exception e) {
//				logger.error("Encounter an exception to lock settlementData", e);
//			}

            logger.debug("settlementData: " + settlementData.getId() + "( "+i+" of "+size+" )");

            String line = "";
            FinancialEntity entity = settlementData.getFinancialEntity();
            Terminal terminal = settlementData.getTerminal();
            logger.debug("entity: " + entity.getId());

            docSize += 1;
            Account account = entity.getOwnOrParentAccount();
            if (settlementData.getClearingProfile() != null)
                account = ClearingService.getSettlementService(settlementData.getClearingProfile()).getAccount(settlementData);

            String accountNumber = neginAccountFormat(account != null ? account.getAccountNumber() : ""
//    				entity.getAccount() != null ? entity.getAccount().getAccountNumber() : ""
            );
            logger.debug("accountId: " + accountNumber);
//    		if (AccountType.DEPOSIT.equals(account.getType()))
            if (isDeposit(accountNumber))
                line = "D,";
//    		else if (AccountType.ACCOUNT.equals(account.getType()))
            else
                line = "A,";
            line += accountNumber;
            line += ",";

            Long totalStlDataAmount = settlementData.getTotalSettlementAmount();
            Long amountForDocumentation = totalStlDataAmount;
            Long totalFee = settlementData.getTotalFee();
            stlStateTotalFee += totalFee;
            stlStateTotalAmount += settlementData.getTotalAmount();

            String feeLine = line;
            String desc = Util.ansiFormat(docDesc);
            if (settlementData.getClearingProfile() != null)
                desc = Util.ansiFormat(ClearingService.getDocDesc(settlementData));
            //Mirkamali(Task179): Currency ATM
            if (FinancialEntityRole.ORGANIZATION.equals(entity.getRole()) ||FinancialEntityRole.BRANCH.equals(entity.getRole())) {
            	logger.debug("entity is Branch only for Currency ATM not for normal ATMs");
                String descOrg = Util.ansiFormat(entity.getName()) + " ";

                totalStlDataAmount = settlementData.getTotalAmount();
                amountForDocumentation = totalStlDataAmount;
                if (totalStlDataAmount > 0)
                    line += "+" + precision.format(amountForDocumentation) + ",";
                else {
                    amountForDocumentation = -1 * totalStlDataAmount;
                    line += "-" + precision.format(amountForDocumentation) + ",";
                }

                line += descOrg;

                /*********    ORGANIZATION Fee in other description item document: Start     ********/
                if (!totalFee.equals(0L)) {
                    descOrg += "بابت کارمزد " + desc+" مورخ "+dateFormatPers.format(settlementData.getSettlementTime().toDate())+"\r\n";
                    if (totalFee > 0) {
                        feeLine += "+" + precision.format(totalFee) + ",";
                        creditLine += feeLine + descOrg;
                    } else {
                        feeLine += "-" + precision.format(-1 * totalFee) + ",";
                        debitLine += feeLine + descOrg;
                    }
                    docSize += 1;
                }
                /*********    ORGANIZATION Fee in other description item document: End     ********/

            } else {
                if (totalStlDataAmount > 0)
                    line += "+" + precision.format(amountForDocumentation) + ",";
                else {
                    amountForDocumentation = -1 * totalStlDataAmount;
                    line += "-" + precision.format(amountForDocumentation) + ",";
                }
                line += Util.ansiFormat(entity.getRole().getName());
                line += " " +Util.ansiFormat(entity.getName()) + " ";
                if (terminal != null) {
                    line += "ترمينال " + terminal.getCode();
                }
            }

            newDocumentAmount += amountForDocumentation;

            try {
                finalDocDesc = desc;
            } catch (Exception e) {
                logger.debug("Exception in getting Document Description of SettlementData: " + settlementData.getId());
                finalDocDesc = "تسويه " + docDesc;
            }
            line += " بابت  " + finalDocDesc;
            line += " مورخ " + dateFormatPers.format(settlementData.getSettlementTime().toDate()) + "\r\n";
            if (totalStlDataAmount > 0)
                creditLine += line;
            else
                debitLine += line;

            i++;
        }

        String persionFormat = dateFormatPers.format(date.toDate());

        List<DocumentItemEntity> documentItemList = new Vector<DocumentItemEntity>();
        String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
//		Account myAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
        Account myAccount = ProcessContext.get().getMyInstitution().getAccount();
        String myAccountNumber = myAccount.getAccountNumber();
        String negin1CommentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginMerchantDocumentItemTitle1)+ persionFormat;
        DocumentItemEntityType myTopic = DocumentItemEntityType.Topic;

        //TODO set correctly accountType in account!
//    	if (AccountType.TOPIC.equals(myAccount.getType()))
//			myTopic = DocumentItemEntityType.Topic;
//		else if (AccountType.ACCOUNT.equals(myAccount.getType()))
//			myTopic = DocumentItemEntityType.Account;
        if (AccountingService.isTopic(myAccountNumber)){
            myTopic = DocumentItemEntityType.Topic;
        }else{
            myTopic = DocumentItemEntityType.Account;
        }

        Account neginAccount = FinancialEntityService.getInstitutionByCode("639347").getCoreAccountNumber();
        String neginAccountNumber = neginAccount.getAccountNumber();
        DocumentItemEntityType neginTopic = DocumentItemEntityType.Topic;

        //TODO set correctly accountType in account!
//    	if (AccountType.TOPIC.equals(neginAccount.getType()))
//			neginTopic = DocumentItemEntityType.Topic;
//		else if (AccountType.ACCOUNT.equals(neginAccount.getType()))
//			neginTopic = DocumentItemEntityType.Account;
        if (AccountingService.isTopic(neginAccountNumber)){
            neginTopic = DocumentItemEntityType.Topic;
        }else{
            neginTopic = DocumentItemEntityType.Account;
        }
        String negin2CommentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginMerchantDocumentItemTitle2) + persionFormat;
        String settlementReportStr = "";


        if (stlStateTotalAmount > 0) {
            settlementReportStr = "A," + bankAccount + ",+" + stlStateTotalAmount + "," + "انتقال به واسط کانال فناپ " + bankDocDesc+ " مورخ "+persionFormat + "\r\n";
            creditLine += settlementReportStr;
            DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(stlStateTotalAmount), true, switchBranchId, negin1CommentOfDocumentItem,
                    myAccountNumber, myTopic);
            documentItemList.add(documentItemEntity);

            settlementReportStr = "A," + fanapAccount + ",-" + stlStateTotalAmount + "," + "انتقال از سیستم جامع فناپ "  + bankDocDesc+ " مورخ "+persionFormat + "\r\n";
            debitLine += settlementReportStr;
            settlementReportStr = "A," + bankAccount + ",-" + stlStateTotalAmount + "," + "پرداخت " + bankDocDesc + " مورخ "+persionFormat  + "\r\n";
            debitLine += settlementReportStr;
            documentItemEntity = new DocumentItemEntity(new Double(stlStateTotalAmount), false, switchBranchId, negin2CommentOfDocumentItem,
                    neginAccountNumber, neginTopic);
            documentItemList.add(documentItemEntity);

            newDocumentAmount += stlStateTotalAmount;
            docSize += 3;

        } else if (stlStateTotalAmount < 0) {
            settlementReportStr = "A," + bankAccount + ",-" + (-1*stlStateTotalAmount) + "," + "انتقال از واسط کانال فناپ " + bankDocDesc+ " مورخ "+persionFormat + "\r\n";
            DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double((-1*stlStateTotalAmount)), false, switchBranchId, negin1CommentOfDocumentItem,
                    myAccountNumber, myTopic);
            documentItemList.add(documentItemEntity);
            debitLine += settlementReportStr;

            settlementReportStr = "A," + fanapAccount + ",+" + (-1*stlStateTotalAmount) + "," +  "انتقال به سیستم جامع فناپ "  + bankDocDesc+ " مورخ "+persionFormat + "\r\n";
            creditLine += settlementReportStr;
            settlementReportStr = "A," + bankAccount + ",+" + (-1*stlStateTotalAmount) + "," + "دریافت " + bankDocDesc + " مورخ "+persionFormat  + "\r\n";
            creditLine += settlementReportStr;
            documentItemEntity = new DocumentItemEntity(new Double((-1*stlStateTotalAmount)), true, switchBranchId, negin2CommentOfDocumentItem,
                    neginAccountNumber, neginTopic);
            documentItemList.add(documentItemEntity);

            newDocumentAmount += (-1*stlStateTotalAmount);
            docSize += 3;
        }

        /***********************/
        if (stlStateTotalFee < 0) {
            settlementReportStr = "A," + fanapAccount + ",+" + (-1 * stlStateTotalFee) + "," + "انتقال به سیستم جامع فناپ بابت پرداخت کارمزد پذیرندگان فناپی " + clrProfName + " مورخ "+persionFormat+ "\r\n";
            creditLine += settlementReportStr;

            DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(-1*stlStateTotalFee), true, switchBranchId, negin2CommentOfDocumentItem,
                    neginAccountNumber, neginTopic);
            documentItemList.add(documentItemEntity);
            documentItemEntity = new DocumentItemEntity(new Double(-1*stlStateTotalFee), false, switchBranchId, negin1CommentOfDocumentItem,
                    myAccountNumber, myTopic);
            documentItemList.add(documentItemEntity);

            newDocumentAmount += (-1*stlStateTotalFee);
            docSize += 1;
        } else if (stlStateTotalFee > 0) {
            settlementReportStr = "A," + fanapAccount + ",-" + stlStateTotalFee + "," + "انتقال از سیستم جامع فناپ بابت پرداخت کارمزد پذیرندگان نگینی " + clrProfName + " مورخ "+persionFormat+ "\r\n";
            debitLine += settlementReportStr;
            settlementReportStr = "A," + bankAccount + ",+" + stlStateTotalFee + "," + "انتقال به واسط کانال فناپ برای پرداخت کارمزد " + bankDocDesc+ " مورخ "+persionFormat + "\r\n";
            creditLine += settlementReportStr;
            settlementReportStr = "A," + bankAccount + ",-" + stlStateTotalFee + "," + "پرداخت کارمزد "  + bankDocDesc+ " مورخ "+persionFormat + "\r\n";
            debitLine += settlementReportStr;

            DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(stlStateTotalFee), false, switchBranchId, negin2CommentOfDocumentItem,
                    neginAccountNumber, neginTopic);
            documentItemList.add(documentItemEntity);
            documentItemEntity = new DocumentItemEntity(new Double(stlStateTotalFee), true, switchBranchId, negin1CommentOfDocumentItem,
                    myAccountNumber, myTopic);
            documentItemList.add(documentItemEntity);

            newDocumentAmount += stlStateTotalFee;
            docSize += 3;
        }

        settlementReportStr = "N," + persionFormat +
                "," + newDocumentAmount +
                "," + docSize + "\r\n";
        settlementReport += settlementReportStr;

        settlementReport += debitLine;
        settlementReport += creditLine;

        settlementState.setSettlementReport(settlementReport);

        /********************/
        String folderStr = "settlement-report";
        File file = new File(folderStr);
        file.mkdirs();

        String str = folderStr + "/settlementReport.txt";
        FileWriter fw = new FileWriter(str, false);
        fw.write(settlementReport);
        fw.close();
        /********************/

        settlementState.addSettlementReport(Core.NEGIN_CORE, settlementReport, null);
        settlementState.setState(SettlementStateType.FILECREATED);
        settlementState.setSettlementFileCreationDate(DateTime.now()) ;
        GeneralDao.Instance.saveOrUpdate(settlementState);

        List<SettlementData> settleList= new ArrayList<SettlementData>();
        settleList.addAll(settlementState.getSettlementDatas());

        List<Transaction> trxList = new ArrayList<Transaction>();
        trxList.addAll(settleList.get(0).getTransactions());

        try{
            Pair<String, String> document = AccountingService.generateFCBDocument("تسويه حساب " +  docDesc + " نگين مورخ " + persionFormat, documentItemList, null
                    ,"stlState-"+settlementState.getId(),trxList.get(0).getIncomingIfx(), null, settleList.get(0).getTerminal().getTransactionNumberPattern());
            SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
            settlementState.addSettlementReport(report);
            GeneralDao.Instance.saveOrUpdate(settlementState);

            String transactionId = AccountingService.issueFCBDocument(report, true);
            report.setDocumentNumber(transactionId);
            GeneralDao.Instance.saveOrUpdate(report);

            if (transactionId!=null) {
                int updateSettlementData = AccountingService.updateSettlementData(settlementState.getSettlementDatas(),
                        transactionId);
                logger.debug(updateSettlementData + " settlementData are settled in document-" + transactionId);
                int updateSettlementInfo = AccountingService.updateSettlementInfo(settlementState.getSettlementDatas(),
                        SettledState.SENT_FOR_SETTLEMENT);
                logger.debug(updateSettlementInfo + " settlementInfo sent for settlement in document-" + transactionId);
            }
            logger.debug("Document number of settlement between Negin and Fanap is "+ transactionId + " on "+ persionFormat);
        }catch (Exception e) {
            logger.error("document of settlement between Negin and Fanap cannot be issued on "+ persionFormat+"("+ e.getClass().getSimpleName() + ": " + e.getMessage()+")");
        }
    }

    private static void generateFinalSaderatSettlementStateReport(SettlementState settlementState, String docDesc, Boolean onlyFee,
                                                                  Boolean isForced) throws Exception {

        String settlementReport = "";
        DecimalFormat precision = new DecimalFormat(ProcessContext.get().getRialCurrency().getPattern());

        if (settlementState.getSettlementDatas() == null) {
            logger.error("no settlement data found for settlement state: " + settlementState.getId());
            return;
        }

        int docSize = 0;

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");

        Long newDocumentAmount = 0L;
        Long stlStateTotalAmount = 0L;
        Long stlStateTotalFee = 0L;

        String finalDocDesc = "";
        int i = 0;
        int size = settlementState.getSettlementDatas().size();

//		String clrProfName = Util.ansiFormat("برای " + docDesc + " ");
//		if (settlementState.getClearingProfile() != null)
//			clrProfName = Util.ansiFormat("برای الگوی " + settlementState.getClearingProfile().getName());
//		String bankDocDesc = "بابت تسويه حساب با دارندگان حسابهای صادرات " + clrProfName;

        DateTime date = null;

        String line = "";
        String delim = "/";

        List<SettlementData> sendForSettle = new ArrayList<SettlementData>();

        for (SettlementData settlementData : settlementState.getSettlementDatas()) {
            if (settlementData == null)
                continue;

            if (date == null)
                date = settlementData.getSettlementTime();
            if (date.before(settlementData.getSettlementTime()))
                date = settlementData.getSettlementTime();

            if (!isForced && Util.hasText(settlementData.getDocumentNumber()))
                continue;

            if (onlyFee && settlementData.getTotalAmount() != 0)
                continue;

            if (((Long) settlementData.getTotalSettlementAmount()).equals(0L))
                continue;

            logger.debug("settlementData: " + settlementData.getId() + "( " + i + " of " + size + " )");

            FinancialEntity entity = settlementData.getFinancialEntity();
            Terminal terminal = settlementData.getTerminal();
            logger.debug("entity: " + entity.getId());

            docSize += 1;
            Account account = entity.getOwnOrParentAccount();
            if (settlementData.getClearingProfile() != null)
                account = ClearingService.getSettlementService(settlementData.getClearingProfile()).getAccount(settlementData);

            String accountNumber = account != null ? account.getAccountNumber() : "";

            logger.debug("accountId: " + accountNumber);

            line += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, accountNumber, '0');
            line += delim;

            Long totalStlDataAmount = settlementData.getTotalSettlementAmount();
            Long amountForDocumentation = totalStlDataAmount;
            Long totalFee = settlementData.getTotalFee();
            stlStateTotalFee += totalFee;
            stlStateTotalAmount += settlementData.getTotalAmount();

            String desc = Util.ansiFormat(docDesc);
            if (settlementData.getClearingProfile() != null)
                desc = Util.ansiFormat(ClearingService.getDocDesc(settlementData));

            line += StringFormat.formatNew(15, StringFormat.JUST_RIGHT, precision.format(amountForDocumentation), '0');
            line += delim;

//			String entityDocDesc = Util.ansiFormat(entity.getRole().getName()) + "," + Util.ansiFormat(entity.getName()) + ",";
            String entityDocDescShort = entity.getCode() + ",";
//			
//			String entityDocDesc = "shop:" + entity.getCode() + ",";
            StringBuilder builder = new StringBuilder("S" + entity.getCode() + ",");

//			line += Util.ansiFormat(entity.getRole().getName());
//			line += "," + Util.ansiFormat(entity.getName()) + ",";

            if (terminal != null) {
                entityDocDescShort += terminal.getCode();
//				entityDocDesc += "pos:" + terminal.getCode() + ",";
                builder.insert(0, "P" + terminal.getCode() + ",");

            }

            newDocumentAmount += amountForDocumentation;

            try {
                finalDocDesc = desc;
            } catch (Exception e) {
                logger.debug("Exception in getting Document Description of SettlementData: " + settlementData.getId());
                finalDocDesc = "تسويه " + docDesc;
            }
//			entityDocDesc += " بابت  " + finalDocDesc;
//			entityDocDesc += "date:" + dateFormatPers.format(settlementData.getSettlementTime().toDate());
            builder.insert(0, "D" + dateFormatPers.format(settlementData.getSettlementTime().toDate()));

            line += StringFormat.formatNew(15, StringFormat.JUST_LEFT, entityDocDescShort, ' ');
            line += delim;
            line += StringFormat.formatNew(30, StringFormat.JUST_LEFT, builder.toString(), ' ');
            line += delim;
            line +=  "00" + "\r\n";

            sendForSettle.add(settlementData);


            i++;
        }

//		String persionFormat = dateFormatPers.format(date.toDate());
//
//		Account myAccount = ProcessContext.get().getMyInstitution().getAccount();
//		String myAccountNumber = myAccount.getAccountNumber();
//
//		Account saderatAccount = FinancialEntityService.getInstitutionByCode(603769L).getCoreAccountNumber();
//		String saderatAccountNumber = saderatAccount.getAccountNumber();

//		if (stlStateTotalAmount > 0) {
//			line += saderatAccountNumber + " " + newDocumentAmount + " " + bankDocDesc + " " + bankDocDesc + "0" + " " + "0";
//		} else if (stlStateTotalAmount < 0) {
//		}

        /***********************/

        settlementState.setSettlementReport(line);

        /********************/
        String folderStr = "settlement-report";
        File file = new File(folderStr);
        file.mkdirs();

        String str = folderStr + "/settlementReport.txt";
        FileWriter fw = new FileWriter(str, false);
        fw.write(settlementReport);
        fw.close();
        /********************/

        SettledState state = SettledState.SENT_FOR_SETTLEMENT;
        int updateSettlementInfo = AccountingService.updateSettlementInfo(sendForSettle, state );
        logger.debug(updateSettlementInfo + " settlementInfo are sent for settle" );

        settlementState.addSettlementReport(Core.Saderat_CORE, settlementReport, null);
        settlementState.setState(SettlementStateType.FILECREATED);
        settlementState.setSettlementFileCreationDate(DateTime.now());
        GeneralDao.Instance.saveOrUpdate(settlementState);

    }

    private static void generateFinalNeginSettlementStateReportForPEP(SettlementState settlementState, String docDesc, DateTime settleDate) throws Exception {
        String settlementReport = "";
//		DecimalFormat precision = new DecimalFormat(GlobalContext.getInstance().getRialCurrency().getPattern());
        DecimalFormat precision = new DecimalFormat(ProcessContext.get().getRialCurrency().getPattern());

        if (settlementState.getSettlementDatas() == null) {
            logger.error("no settlement data found for settlement state: " + settlementState.getId());
            return;
        }

        int docSize = 0;

        PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
        String persionFormat = dateFormatPers.format(settleDate.toDate());

        Long newDocumentAmount = 0L;
        Long stlStateTotalAmount = 0L;
        Long stlStateTotalFee = 0L;

        String debitLine = "";
        String creditLine = "";

        String finalDocDesc = "";

        String clrProfName = Util.ansiFormat( "برای " + docDesc );
        if (settlementState.getClearingProfile() != null)
            clrProfName = Util.ansiFormat("برای الگوی "+ settlementState.getClearingProfile().getName());
        String bankDocDesc = "بابت تسويه حساب با دارندگان حسابهای نگينی " + clrProfName;

        int i=0;
        int size = settlementState.getSettlementDatas().size();

        for (SettlementData settlementData: settlementState.getSettlementDatas()) {
            if (settlementData == null)
                continue;

            if (((Long)settlementData.getTotalSettlementAmount()).equals(0L))
                continue;

            logger.debug("settlementData: " + settlementData.getId()+ "( "+i+" of "+size+" )");

            String line = "";
            FinancialEntity entity = settlementData.getFinancialEntity();
            Terminal terminal = settlementData.getTerminal();
            logger.debug("entity: " + entity.getId());

            docSize += 1;

            Account account = entity.getOwnOrParentAccount();
            if (settlementData.getClearingProfile() != null)
                account = ClearingService.getSettlementService(settlementData.getClearingProfile()).getAccount(settlementData);

            String accountNumber = neginAccountFormat(account != null ? account.getAccountNumber() : "");
            logger.debug("accountId: " + accountNumber);
            if (isDeposit(accountNumber))
                line = "D,";
            else
                line = "A,";
            line += accountNumber;
            line += ",";

            Long totalStlDataAmount = settlementData.getTotalSettlementAmount();
            Long amountForDocumentation = totalStlDataAmount;
            Long totalFee = settlementData.getTotalFee();
            stlStateTotalFee += totalFee;
            stlStateTotalAmount += settlementData.getTotalAmount();

            String desc = Util.ansiFormat(docDesc);

            if (settlementData.getClearingProfile() != null)
                desc = Util.ansiFormat(ClearingService.getDocDesc(settlementData));
            if (totalStlDataAmount > 0)
                line += "+" + precision.format(amountForDocumentation) + ",";
            else {
                amountForDocumentation = -1 * totalStlDataAmount;
                line += "-" + precision.format(amountForDocumentation) + ",";
            }
            line += Util.ansiFormat(entity.getRole().getName());
            line += " " + Util.ansiFormat(entity.getName()) + " ";
            if (terminal != null) {
                line += "ترمينال " + terminal.getCode();
            }

            newDocumentAmount += amountForDocumentation;

            try {
                finalDocDesc = desc;
            } catch (Exception e) {
                logger.debug("Exception in getting Document Description of SettlementData: " + settlementData.getId());
                finalDocDesc = "تسويه " + docDesc;
            }
            line += " بابت  " + finalDocDesc;
            line += " مورخ " + dateFormatPers.format(settlementData.getSettlementTime().toDate()) + "\r\n";
            if (totalStlDataAmount > 0)
                creditLine += line;
            else
                debitLine += line;
        }

        String settlementReportStr = "";

        if (stlStateTotalAmount > 0) {
            settlementReportStr = "A," + bankAccountPEP + ",-" + stlStateTotalAmount + "," + "پرداخت " + bankDocDesc + " مورخ "+persionFormat  + "\r\n";
            debitLine += settlementReportStr;

//    		newDocumentAmount += stlStateTotalAmount;
            docSize++;

        } else if (stlStateTotalAmount < 0) {
            settlementReportStr = "A," + bankAccountPEP + ",+" + (-1*stlStateTotalAmount) + "," + "دریافت " + bankDocDesc + " مورخ "+persionFormat  + "\r\n";
            creditLine += settlementReportStr;

//    		newDocumentAmount += (-1*stlStateTotalAmount);
            docSize++;
        }

        /***********************/
        if (stlStateTotalFee < 0) {
        } else if (stlStateTotalFee > 0) {
            settlementReportStr = "A," + bankAccountPEP + ",-" + stlStateTotalFee + "," + "پرداخت کارمزد "  + bankDocDesc+ " مورخ "+persionFormat + "\r\n";
            debitLine += settlementReportStr;

//    		newDocumentAmount += stlStateTotalFee;
            docSize++;
        }

        if (docSize == 0 || newDocumentAmount.equals(0L)) {
            if (docSize != 0 || newDocumentAmount.equals(0L))
                logger.error("docSize is " + docSize + " but docAmount is " + newDocumentAmount);
        } else {
            settlementReportStr = "N," + persionFormat +
                    "," + newDocumentAmount +
                    "," + docSize + "\r\n";
            settlementReport += settlementReportStr;

            settlementReport += debitLine;
            settlementReport += creditLine;

            settlementState.setSettlementReport(settlementReport);
            settlementState.addSettlementReport(Core.NEGIN_CORE, settlementReport, null);
        }

        settlementState.setState(SettlementStateType.FILECREATED);
        settlementState.setSettlementFileCreationDate(DateTime.now());
        GeneralDao.Instance.saveOrUpdate(settlementState);

        try{
//			int updateSettlementData = accountingService.updateSettlementData(settlementState.getSettlementDatas(), transactionId);
//			logger.debug(updateSettlementData + " settlementData are settled in document-"+ transactionId );
//			
//			int updateSettlementInfo = accountingService.updateSettlementInfo(settlementState.getSettlementDatas(), SettledState.SENT_FOR_SETTLEMENT);
//			logger.debug(updateSettlementInfo + " settlementInfo sent for settlement in document-"+ transactionId );
//			
//			logger.debug("Document number of settlement between Negin and Fanap is "+ transactionId + " on "+ persionFormat);
        }catch (Exception e) {
            logger.error("document of settlement between Negin and Fanap cannot be issued on "+ persionFormat+"("+ e.getClass().getSimpleName() + ": " + e.getMessage()+")");
        }
    }

    public static void generateThirdPartySettlementReport(ClearingProfile clearingProfile, DateTime settleDate, ThirdPartyType orgType, String ip, String destPath) {
        List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, null);
        if (settlementStates == null || settlementStates.size() == 0) {
            logger.info("don't found any settlement state!");
            return;
        }

//		List<SettlementData> settlementDatas = new ArrayList<SettlementData>();
//		for (SettlementState settlementState: settlementStates) {
//			List<SettlementData> innerSettlementDatas = settlementState.getSettlementDatas();
//			if (innerSettlementDatas == null || innerSettlementDatas.size() == 0) {
//				logger.info("don't found any settlement data for settlement state: " + settlementState.getId());
//				continue;
//			} else {
//				settlementDatas.addAll(innerSettlementDatas);
//			}
//		}
//		
//		if (settlementDatas == null || settlementDatas.size() == 0) {
//			logger.info("don't found any settlement data");
//			return;
//		}

//		generateThirdPartyReport(orgType, settlementDatas);

        generateThirdPartyReportBySettlementState(orgType, settlementStates, ip, destPath);
    }

    public static void generateThirdPartyReportBySettlementState(ThirdPartyType orgType, List<SettlementState> settlementStates, String ip, String destPath) {
        List<SettlementData> settlementDatas = AccountingService.findThirdPartySettlementDatasOfSettlementStates(settlementStates);
        generateThirdPartyReportBySettlementData(orgType, settlementDatas, ip, destPath);
    }

    public static void generateThirdPartyReportBySettlementData(ThirdPartyType orgType, List<SettlementData> settlementDatas, String ip, String destPath) {

        String fieldSeprator;
        String headerFieldSeprator;

        String fileNameSeperator = "";

        final String fieldSepratorOfIranCell = ",";
        final String headerFieldSeparatorOfIrancell = ",";
        boolean isIrancell = false;

        final String fieldSepratorOfRightel = ",";
        final String headerFieldSeparatorOfRightel = "";
        boolean isRightel = false;

        boolean isHamrahAval = false;

        String pasFile = "";

        final String fieldSepratorOfOthers = "";
        final String headerFieldSeparatorOfOthers = "";

        final String cardPANSeprator = "-";
        final String cardPAN2StarChar = "**";

        Map<Organization, List<SettlementData>> thirdPartySettlementDatas = getThirdPartySettlementDatas(settlementDatas);

        if (thirdPartySettlementDatas == null || thirdPartySettlementDatas.size() == 0) {
            return;
        }
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");

        for (Organization entity : thirdPartySettlementDatas.keySet()) {

            isIrancell = false;
            isRightel = false;
            isHamrahAval = false;

            fileNameSeperator = "";

            if (entity.getCode().equals(9935L) || entity.getCode().equals(9936L)) {
                isIrancell = true;
                pasFile = ".935";
                fileNameSeperator = "-";
                fieldSeprator = fieldSepratorOfIranCell;
                headerFieldSeprator = headerFieldSeparatorOfIrancell;

            } else if(entity.getCode().equals(9920L)){
                isRightel = true;
                pasFile = ".920";
                fieldSeprator = fieldSepratorOfRightel;
                headerFieldSeprator = headerFieldSeparatorOfRightel;

            } else if(entity.getCode().equals(9912L) || entity.getCode().equals(9913L)){
                isHamrahAval = true;
                pasFile = ".912";
                fieldSeprator = fieldSepratorOfRightel;
                headerFieldSeprator = headerFieldSeparatorOfRightel;

            } else {
                fieldSeprator = fieldSepratorOfOthers;
                headerFieldSeprator = headerFieldSeparatorOfOthers;
            }

            List<SettlementData> settlementDataList = thirdPartySettlementDatas.get(entity);
            if (settlementDataList != null && settlementDataList.size() > 0)
                for (SettlementData settlementData : settlementDataList) {
                    if (settlementData == null)
                        return;


                    StringBuilder settlementReport = new StringBuilder();

                    String myBankCode = ProcessContext.get().getBank(ProcessContext.get().getMyInstitution().getBin().intValue()).getTwoDigitCode()+"";
                    Long totalAmount = 0L;
                    Integer totalSize = 0;
                    StringBuilder header = new StringBuilder("");
                    String branchCardCode = ProcessContext.get().getMyInstitution().getBranchCardCode();
                    String branchId = StringFormat.formatNew(6, StringFormat.JUST_RIGHT, branchCardCode, '0');
                    String temp= "";
                    StringBuilder row = new StringBuilder();

                    /************** MTN remaining charge ***************/
                    StringBuilder remainChargeRow = new StringBuilder();
                    String remainChargeHeader = "";
                    DateTime now = DateTime.now();
                    StringBuilder remainChargeReport = new StringBuilder();
                    /***************************************************/


                    Terminal endPointTerminal = null;
                    Long cityId = null;
                    StringBuilder appPAN = null;


                    for(int i=0; ; i++){
                        List<ReportRecord> records = null;
                        List<RemainingChargeRecord> remainingChargeRecord = null; //MTN remaining charge

                        if (ThirdPartyType.BILLPAYMENT.equals(orgType)) {
                            records = AccountingService.getBillPaymentThirdPartyReportRecords(settlementData,
                                    i*ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE),
                                    ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE));

                        } else if (ThirdPartyType.CHARGE.equals(orgType)) {
                            records = AccountingService.getChargeThirdPartyReportRecords(settlementData,
                                    i*ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE),
                                    ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE));

                            /************** MTN remaining charge ***************/
                            if(isIrancell || isRightel || isHamrahAval){
                                remainingChargeRecord = AccountingService.getRemainingChargeReportRecords(entity,
                                        i * ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE),
                                        ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE));
                            }
                            /***************************************************/

                        } else if (ThirdPartyType.THIRDPARTYPURCHASE.equals(orgType)) {
                            OrganizationService.generateDesiredThirdPartyReport(entity, settlementData);
                        }

                        logger.debug("settlementData: "+settlementData.getId() + " i: "+ i);

                        //MTN remaining charge
                        if((records == null || records.size() == 0) && (remainingChargeRecord == null || remainingChargeRecord.size() == 0))
//						if(records == null || records.size() == 0)
                            break;

                        for (ReportRecord record:records) {
                            temp = branchId;
                            String billId = "";
                            String paymentId = "";
                            TerminalType termType = TerminalType.UNKNOWN;

                            if (ThirdPartyType.BILLPAYMENT.equals(orgType)) {
                                billId = ((BillPayReportRecord) record).billID;
                                paymentId = StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ((BillPayReportRecord) record).billPaymentID, '0');
                                termType = ((BillPayReportRecord) record).terminalType;

                                if (TerminalType.PINPAD.equals(termType)) {
                                    termType = TerminalType.POS;
                                }

                            } else if (ThirdPartyType.CHARGE.equals(orgType)) {
                                Long serialNo = ((ChargeReportRecord) record).cardSerialNo;
                                int checkDigit = BillPaymentUtil.getCheckDigit(serialNo.toString());
                                billId = Long.toString(serialNo);
                                billId += checkDigit;
                                String chargePayId = getPaymentId(record.auth_Amt, ((ChargeReportRecord) record).year, ((ChargeReportRecord) record).fileId);
                                paymentId = StringFormat.formatNew(11, StringFormat.JUST_RIGHT, chargePayId, '0');
                                paymentId += BillPaymentUtil.getCheckDigit(chargePayId);
                                termType = ((ChargeReportRecord) record).terminalType;
                                if(TerminalType.ATM.equals(termType) && (isIrancell || isRightel || isHamrahAval)){
                                    temp = StringFormat.formatNew(8, StringFormat.JUST_RIGHT, ((ChargeReportRecord) record).terminalId, '0');
                                } else if(isIrancell || isRightel || isHamrahAval){
                                    temp = StringFormat.formatNew(8, StringFormat.JUST_RIGHT, temp, '0');

                                }
                            }
                            totalAmount += record.auth_Amt;
                            totalSize += 1;


                            /*********************** common(Irancell & Rightel) ***********************/
                            if(isIrancell || isRightel || isHamrahAval) {
                                endPointTerminal = GeneralDao.Instance.getObject(Terminal.class, ((ChargeReportRecord) record).endPointTerminalCode);
                                try {
                                    cityId = endPointTerminal.getOwner().getContact().getAddress().getCityId();
                                } catch(Exception e) {
                                    logger.info("city of endpoint: " + endPointTerminal + " is NULL!");
                                    cityId = 0L;
                                }
                                appPAN = new StringBuilder(record.appPAN.substring(0, 4)).append(cardPANSeprator)
                                        .append(record.appPAN.substring(4, 6)).append(cardPAN2StarChar).append(cardPANSeprator)
                                        .append(cardPAN2StarChar).append(cardPAN2StarChar).append(cardPANSeprator)
                                        .append(record.appPAN.substring(record.appPAN.length()-4));
                            }
                            /**************************************************************************/


                            if(isIrancell){
                                row.append(temp).append(fieldSeprator);
                                row.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, termType + "", '0')).append(fieldSeprator);
                                row.append(dateFormatPers.format(record.recievedDt.toDate())).append(fieldSeprator);
                                row.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, billId.substring(0, billId.length() - 1), '0')).append(fieldSeprator);
                                row.append(billId.substring(billId.length() - 1)).append(fieldSeprator);
                                row.append(paymentId.substring(0, 11)).append(fieldSeprator);
                                row.append(paymentId.substring(11)).append(fieldSeprator);
                                row.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0')).append(fieldSeprator);
//								appPAN = new StringBuilder(record.appPAN.substring(0, 4)).append(cardPANSeprator)
//										.append(record.appPAN.substring(4, 6)).append(cardPAN2StarChar).append(cardPANSeprator)  
//										.append(cardPAN2StarChar).append(cardPAN2StarChar).append(cardPANSeprator)
//										.append(record.appPAN.substring(record.appPAN.length()-4));
                                row.append(StringFormat.formatNew(19, StringFormat.JUST_RIGHT, appPAN.toString(), '0')).append(fieldSeprator);

//								endPointTerminal = GeneralDao.Instance.getObject(Terminal.class, ((ChargeReportRecord) record).endPointTerminalCode);
//								try {
//								cityId = endPointTerminal.getOwner().getContact().getAddress().getCityId();
//								} catch(Exception e) {
//									logger.info("city of endpoint: " + endPointTerminal + " is NULL!");
//									cityId = 0L;
//								}

                                if (endPointTerminal.getOwner() instanceof Branch) {
                                    row.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, ((Branch)endPointTerminal.getOwner()).getCoreBranchCode(), '0')).append(fieldSeprator);
                                } else {
                                    row.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, "0", '0')).append(fieldSeprator);
                                }


                                row.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, cityId, '0'));

                                /******************************** Rightel ********************************/
                            } else if(isRightel || isHamrahAval){
                                if (endPointTerminal.getOwner() instanceof Branch) {
                                    row.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, ((Branch)endPointTerminal.getOwner()).getCoreBranchCode(), '0')).append(fieldSeprator);
                                } else {
                                    row.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, /*branchId*/"0", '0')).append(fieldSeprator);
                                }
                                row.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, cityId, '0')).append(fieldSeprator);
                                row.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, termType + "", '0')).append(fieldSeprator);
                                row.append(temp).append(fieldSeprator);
                                row.append(dateFormatPers.format(record.recievedDt.toDate())).append(fieldSeprator);
                                if (isHamrahAval) {
                                    row.append(StringFormat.formatNew(17, StringFormat.JUST_RIGHT, billId.substring(0, billId.length() - 1), '0')).append(fieldSeprator);
                                } else {
                                    row.append(StringFormat.formatNew(16, StringFormat.JUST_RIGHT, billId.substring(0, billId.length() - 1), '0')).append(fieldSeprator);
                                }
                                row.append(billId.substring(billId.length() - 1)).append(fieldSeprator);
                                row.append(paymentId.substring(0, 11)).append(fieldSeprator);
                                row.append(paymentId.substring(11)).append(fieldSeprator);
                                row.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0')).append(fieldSeprator);
                                row.append(StringFormat.formatNew(19, StringFormat.JUST_RIGHT, appPAN.toString(), '0'));


                                /*************************************************************************/
                            } else {
                                row.append(temp).append(fieldSeprator);
                                row.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, termType + "", '0')).append(fieldSeprator);
                                row.append(dateFormatPers.format(record.recievedDt.toDate())).append(fieldSeprator);
                                row.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, billId, '0'));
                                row.append(paymentId);
                                row.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0'));
                            }


                            row.append("\r\n");
                        }

                        /************** MTN remaining charge ***************/
                        if(remainingChargeRecord != null && remainingChargeRecord.size() >0) {
                            for(RemainingChargeRecord record : remainingChargeRecord){
                                remainChargeRow.append((record.credit)/1000 + ",");
                                remainChargeRow.append(StringFormat.formatNew(9, StringFormat.JUST_RIGHT, record.count, '0'));
                                remainChargeRow.append("\r\n");
                            }
                        }
                        /***************************************************/
                    }

                    //===== header
                    if (OrganizationType.MTNIRANCELL.equals(entity.getType())) {
                        header.append("9").append(headerFieldSeprator);
                        /************** MTN remaining charge ***************/
                        PersianDateFormat persFormat = new PersianDateFormat("yyMMdd");
                        persFormat.format(now.toDate());
                        remainChargeHeader = "Below is the quantity of logical PINs remaining at " + persFormat.format(now.toDate())
                                + "," + now.getDayTime().toString().replaceAll(":", "");
                        remainChargeHeader += "\r\n";
                        /***************************************************/
                    } else {
                        header.append(OrganizationType.getCode(entity.getType())).append(headerFieldSeprator);
                    }
                    header.append(StringFormat.formatNew(3, StringFormat.JUST_RIGHT, entity.getCompanyCode() + "", '0')).append(headerFieldSeprator);
                    header.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, myBankCode, '0')).append(headerFieldSeprator);
                    header.append(dateFormatPers.format(settlementData.getSettlementTime().toDate())).append(headerFieldSeprator);
                    header.append(StringFormat.formatNew(10, StringFormat.JUST_RIGHT, totalAmount / 1000L, '0')).append(headerFieldSeprator);
                    header.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, totalSize.toString(), '0')).append("\r\n");

                    settlementReport.append(header);
                    settlementReport.append(row.toString());

                    byte[] b;
                    String reportFileName;
                    if (isIrancell || isRightel || isHamrahAval) {

                        remainChargeReport.append(remainChargeHeader);
                        remainChargeReport.append(remainChargeRow);
                        byte[][] bReport = new byte[2][];
                        bReport[0] = settlementReport.toString().getBytes();
                        bReport[1] = remainChargeReport.toString().getBytes();
                        final String CHARGE_PATH = ConfigUtil.getProperty(ConfigUtil.MY_BANK_ACRONYM)+dateFormatPers.format(settlementData.getSettlementTime().toDate())+"UD02001" + fileNameSeperator + "235959" + pasFile;
                        final String REMAIN_CHARGE_PATH = ConfigUtil.getProperty(ConfigUtil.MY_BANK_ACRONYM) +
                                dateFormatPers.format(now.toDate())+ "REMN001" + fileNameSeperator + now.getDayTime().toString().replaceAll(":", "") + pasFile;

                        String[] strFileName = new String[]{CHARGE_PATH, REMAIN_CHARGE_PATH};

                        b = ZipUtil.getZipByteArray(strFileName,bReport);

//						b = ZipUtil.getZipByteArray(new String[]{ConfigUtil.getProperty(ConfigUtil.MY_BANK_ACRONYM)+dateFormatPers.format(settlementData.getSettlementTime().toDate())+"UD02001-235959.935"}, new byte[][]{settlementReport.toString().getBytes()});
                        reportFileName = ConfigUtil.getProperty(ConfigUtil.MY_BANK_ACRONYM)+dateFormatPers.format(settlementData.getSettlementTime().toDate())+"UD02001" + fileNameSeperator + "235959.zip";
                    } /*else if(isRightel){
						b = ZipUtil.getZipByteArray(new String[]{ConfigUtil.getProperty(ConfigUtil.MY_BANK_ACRONYM)+dateFormatPers.format(settlementData.getSettlementTime().toDate())+"UD02001235959.920"}, new byte[][]{settlementReport.toString().getBytes()});
						reportFileName = ConfigUtil.getProperty(ConfigUtil.MY_BANK_ACRONYM)+dateFormatPers.format(settlementData.getSettlementTime().toDate())+"UD02001235959.zip";
						
					} */else {
                        b = ZipUtil.getZipByteArray(new String[]{"Rep-"+entity.getNameEn()+"-"+entity.getCode()+"-"+dateFormatPers.format(settlementData.getSettlementTime().toDate())+".txt"}, new byte[][]{settlementReport.toString().getBytes()});
                        reportFileName = entity.getType().getType() + "-" + entity.getCode() + "-" + ".zip";
                    }

                    SettlementDataReport sdr = settlementData.addThirdPartyReport(b);
                    GeneralDao.Instance.saveOrUpdate(sdr);


                    try {
                        if(destPath != null){
                            File reportDir = new File("/home/reports/"+destPath+"/"+dateFormatPers.format(settlementData.getSettlementTime().toDate()));
                            reportDir.mkdirs();

                            OutputStream reportFile = null;
                            if (!reportDir.exists()) {
                                reportDir.createNewFile();
                            }

                            reportFile = new FileOutputStream(reportDir + "/" + reportFileName);
                            reportFile.write(b);
                            reportFile.close();
                        }
                    } catch(Exception e) {
                        logger.error("can't transfer file, " + e, e);
                    }

//					try {
//						if (Util.hasText(ip)) {
//							SMBFileTransferUtil.upload(b, ip, destPath);
//						} 
//					} catch (Exception e) {
//						logger.error("can't transfer file, " + e, e);
//					}
                }
        }
    }

    public static void generateThirdPartyReportBySettlementDataOld(ThirdPartyType orgType, List<SettlementData> settlementDatas, String ip, String destPath) {
        Map<Organization, List<SettlementData>> thirdPartySettlementDatas = getThirdPartySettlementDatas(settlementDatas);

        if (thirdPartySettlementDatas == null || thirdPartySettlementDatas.size() == 0) {
            return;
        }
        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");

        for (Organization entity : thirdPartySettlementDatas.keySet()) {
            List<SettlementData> settlementDataList = thirdPartySettlementDatas.get(entity);
            if (settlementDataList != null && settlementDataList.size() > 0)
                for (SettlementData settlementData : settlementDataList) {
                    if (settlementData == null)
                        return;


                    StringBuilder settlementReport = new StringBuilder();

//					String myBankCode = GlobalContext.getInstance().getBank(GlobalContext.getInstance().getMyInstitution().getBin().intValue()).getTwoDigitCode()+"";
//					String myBankCode = ProcessContext.get().getBank(GlobalContext.getInstance().getMyInstitution().getBin().intValue()).getTwoDigitCode()+"";
                    String myBankCode = ProcessContext.get().getBank(ProcessContext.get().getMyInstitution().getBin().intValue()).getTwoDigitCode()+"";
                    Long totalAmount = 0L;
                    Integer totalSize = 0;
                    String header = "";
//					String branchCardCode = GlobalContext.getInstance().getMyInstitution().getBranchCardCode();
                    String branchCardCode = ProcessContext.get().getMyInstitution().getBranchCardCode();
                    String branchId = StringFormat.formatNew(6, StringFormat.JUST_RIGHT, branchCardCode, '0');
                    String temp= "";
                    StringBuilder row = new StringBuilder();

                    for(int i=0; ; i++){
                        List<ReportRecord> records = null;

                        if (ThirdPartyType.BILLPAYMENT.equals(orgType)) {
                            records = AccountingService.getBillPaymentThirdPartyReportRecords(settlementData,
                                    i*ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE),
                                    ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE));

                        } else if (ThirdPartyType.CHARGE.equals(orgType)) {
                            records = AccountingService.getChargeThirdPartyReportRecords(settlementData,
                                    i*ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE),
                                    ConfigUtil.getInteger(ConfigUtil.GLOBAL_REPORT_RECORDSIZE));

                        } else if (ThirdPartyType.THIRDPARTYPURCHASE.equals(orgType)) {
                            OrganizationService.generateDesiredThirdPartyReport(entity, settlementData);
                        }

                        logger.debug("settlementData: "+settlementData.getId() + " i: "+ i);
                        if(records == null || records.size() == 0)
                            break;

                        for (ReportRecord record:records) {
                            temp = branchId;
                            String billId = "";
                            String paymentId = "";
                            TerminalType termType = TerminalType.UNKNOWN;
                            if (ThirdPartyType.BILLPAYMENT.equals(orgType)) {
                                billId = ((BillPayReportRecord) record).billID;
                                paymentId = StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ((BillPayReportRecord) record).billPaymentID, '0');
                                termType = ((BillPayReportRecord) record).terminalType;

                                if (TerminalType.PINPAD.equals(termType)) {
                                    termType = TerminalType.POS;
                                }

                            } else if (ThirdPartyType.CHARGE.equals(orgType)) {
                                Long serialNo = ((ChargeReportRecord) record).cardSerialNo;
                                int checkDigit = BillPaymentUtil.getCheckDigit(serialNo.toString());
                                billId = Long.toString(serialNo);
                                billId += checkDigit;
                                String chargePayId = getPaymentId(record.auth_Amt, ((ChargeReportRecord) record).year, ((ChargeReportRecord) record).fileId);
                                paymentId = StringFormat.formatNew(11, StringFormat.JUST_RIGHT, chargePayId, '0');
                                paymentId += BillPaymentUtil.getCheckDigit(chargePayId);
                                termType = ((ChargeReportRecord) record).terminalType;
                                if(TerminalType.ATM.equals(termType) && entity.getCode().equals(9935L)){
                                    temp = StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ((ChargeReportRecord) record).terminalId, '0');

                                }

                            }
                            totalAmount += record.auth_Amt;
                            totalSize += 1;
                            row.append(temp);
                            row.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, termType + "", '0'));
                            row.append(dateFormatPers.format(record.recievedDt.toDate()));
                            row.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, billId, '0'));
                            row.append(paymentId);
                            row.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0'));
                            row.append("\r\n");
                        }
                    }
                    if (OrganizationType.MTNIRANCELL.equals(entity.getType())) {
                        header += "9";

                    } else {
                        header += OrganizationType.getCode(entity.getType());

                    }

                    header += StringFormat.formatNew(3, StringFormat.JUST_RIGHT, entity.getCompanyCode() + "", '0');
                    header += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, myBankCode, '0');
                    header += dateFormatPers.format(settlementData.getSettlementTime().toDate());
                    header += StringFormat.formatNew(10, StringFormat.JUST_RIGHT, totalAmount / 1000L, '0');
                    header += StringFormat.formatNew(8, StringFormat.JUST_RIGHT, totalSize.toString(), '0');
                    header += "\r\n";

                    settlementReport.append(header);
                    settlementReport.append(row.toString());

                    byte[] b = ZipUtil.getZipByteArray(new String[]{"Rep-"+entity.getNameEn()+"-"+entity.getCode()+"-"+dateFormatPers.format(settlementData.getSettlementTime().toDate())+".txt"}, new byte[][]{settlementReport.toString().getBytes()});
                    SettlementDataReport sdr = settlementData.addThirdPartyReport(b);
                    GeneralDao.Instance.saveOrUpdate(sdr);


                    try {
                        if(destPath != null){
                            File reportDir = new File("/home/reports/"+destPath+"/"+dateFormatPers.format(settlementData.getSettlementTime().toDate()));
                            reportDir.mkdirs();

                            OutputStream reportFile = null;
                            if (!reportDir.exists()) {
                                reportDir.createNewFile();
                            }

                            reportFile = new FileOutputStream(reportDir + "/" + entity.getType().getType() + "-" + entity.getCode() + "-" + ".zip");
                            reportFile.write(b);
                            reportFile.close();
                        }
                    } catch(Exception e) {
                        logger.error("can't transfer file, " + e, e);
                    }

//					try {
//						if (Util.hasText(ip)) {
//							SMBFileTransferUtil.upload(b, ip, destPath);
//						} 
//					} catch (Exception e) {
//						logger.error("can't transfer file, " + e, e);
//					}
                }
        }
    }

    private static Map<Organization, List<SettlementData>> getThirdPartySettlementDatas(List<SettlementData> settlementDatas) {
        Map<Organization, List<SettlementData>> result = new HashMap<Organization, List<SettlementData>>();

        for (SettlementData settlementData : settlementDatas) {
            Organization organization = GeneralDao.Instance.load(Organization.class, settlementData.getFinancialEntityId());
            List<SettlementData> list = result.get(organization);
            if (list == null)
                list = new ArrayList<SettlementData>();
            list.add(settlementData);
            result.put(organization, list);
        }
        return result;
    }

    private static String getPaymentId(Long amount, Integer year, Integer fileId) {
        String result = "";
        Long amountTh = amount / 1000L;
        result += amountTh;
        result += (year.toString()).substring(year.toString().length() - 1);
        result += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, fileId.toString(), '0');
        return result;
    }

    private static boolean isDeposit(String accountId) {
        String[] strings = accountId.split(",");
        return strings.length == 4;
    }

    private static String neginAccountFormat(String accountId) {
        if (accountId == null)
            return "";
        String result = accountId;
        result = result.replaceAll("-", ",");
        result = result.replaceAll("_", ",");
        result = result.replaceAll("\\.", ",");
        return result;
    }

    private static String fanapAccountFormat(String accountId, AccountType accType) {
        if (accountId == null)
            return "";
        String result = accountId;
        if(AccountType.ACCOUNT.equals(accType)){
            result = result.replaceAll("\\.", "-");
            result = result.replaceAll("_", "-");
            result = result.replaceAll(",", "-");
            return result;
        }

        result = result.replaceAll("-", ".");
        result = result.replaceAll("_", ".");
        result = result.replaceAll(",", ".");
        return result;
    }

    public static String getBranchCode(String accountId, AccountType accType) {
        /**** set merchant branch code ****/

        String branchCode = "995";

        try {
            if (AccountType.ACCOUNT.equals(accType)) {

                String[] split = accountId.split("\\-");
                if (Util.hasText(split[1]))
                    branchCode = split[1];

            } else {
                String[] split = accountId.split("\\.");
                if (Util.hasText(split[0]))
                    branchCode = split[0];

            }
        } catch (Exception e) {
            logger.info("exception in getting branchCode, set 995");
            branchCode = "995";
        }

        return branchCode;
    }



    /************* Set service property :Start****************/

    /************* Set service property :End  ****************/

    public static void generatePosReconcileFile(Date startDate, Date endDate)
    {
        generateDisAgreeFile(false, startDate, endDate, null);
        generateDisAgreeFile(false, startDate, endDate, ClearingState.DISAGREEMENT);
    }

    public static void generateCoreReconcileFile(Date startDate, Date endDate)
    {
        generateDisAgreeFile(true, startDate, endDate, ClearingState.DISAGREEMENT);
    }

    public static void generateDisAgreeFile(boolean isCore, Date startDate, Date endDate, ClearingState flag)
    {
        List<Transaction> trxList = new ArrayList<Transaction>();
        if (flag == null)
        {
            trxList = TransactionService.getDesiredTrxs(ClearingState.DISAGREEMENT, startDate, endDate, isCore);
        } else {
            trxList = TransactionService.getDesiredTrxs(flag, startDate, endDate, isCore);
        }

//		MyDateFormat dateFormathhmmss = new MyDateFormat("HH:mm:ss");
//		MyDateFormat dateFormathhmmss2 = new MyDateFormat("HH-mm-ss");

//		MyDateFormat dateFormatYYYYMMDD = new MyDateFormat("yyyy/MM/dd");
//		MyDateFormat dateFormatYYYYMMDD2 = new MyDateFormat("yyyy-MM-dd");

//		StringFormat format6 = new StringFormat(6, StringFormat.JUST_RIGHT);
//		StringFormat format8 = new StringFormat(8, StringFormat.JUST_RIGHT);
//		StringFormat format9 = new StringFormat(9, StringFormat.JUST_LEFT);
//		StringFormat format12 = new StringFormat(12, StringFormat.JUST_RIGHT);
//		StringFormat format19 = new StringFormat(19, StringFormat.JUST_LEFT);

        String nowDate = MyDateFormatNew.format("yyyy-MM-dd", Calendar.getInstance().getTime());
        String nowTime = MyDateFormatNew.format("HH-mm-ss", Calendar.getInstance().getTime());

        String folderStr = ConfigUtil.getProperty(ConfigUtil.GLOBAL_PATH_FILE_REPORT);
        if (isCore)
            folderStr += "/coredisagreement";
        else
            folderStr += "/posdisagreement";

        File file = new File(folderStr);
        file.mkdirs();

        String disAgreeFileStr = folderStr + "/" + ((flag == null) ? "disAgree" : flag.getName(/*Integer.parseInt(flag.toString())*/)) + "-trx" + nowDate
                + "_" + nowTime + ".txt";

        FileWriter fwDisAgree;
        try
        {
            fwDisAgree = new FileWriter(disAgreeFileStr, false);
            Ifx ifx;
            DateTime trxDt;
            String row;
            String type;
            String date;
            String time;
            String appPan;
            String destBankId;
            String trxSeqCntr;
            String terminalId;
            String amount;

            for (Transaction transaction : trxList)
            {
                ifx = transaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
                if (ifx == null)
                    continue;
                if (!ISOFinalMessageType.isResponseMessage(ifx.getIfxType()))
                    continue;
                row = StringFormat.formatNew(6, StringFormat.JUST_RIGHT, Util.countLine(disAgreeFileStr) + "", '0');
                if (ifx.getIfxType() == null)
                    type = "??";
                else if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType()))
                    type = "B ";
                else if (ISOFinalMessageType.isPurchaseBalanceMessage(ifx.getIfxType()))
                    type = "P ";
                else if (ISOFinalMessageType.isReturnMessage(ifx.getIfxType()))
                    type = "R ";
                else if (ISOFinalMessageType.isReversalMessage(ifx.getIfxType()))
                    type = "V ";
                else
                    type = "W ";
                trxDt = ifx.getReceivedDt();
                date = MyDateFormatNew.format("yyyy/MM/dd", trxDt.toDate());
                time = MyDateFormatNew.format("HH:mm:ss", trxDt.toDate());
                trxSeqCntr = StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getSrc_TrnSeqCntr(), '0');
                appPan = StringFormat.formatNew(19, StringFormat.JUST_LEFT, ifx.getAppPAN(), ' ');
                destBankId = StringFormat.formatNew(9, StringFormat.JUST_LEFT, ifx.getDestBankId().toString(), ' ');
                terminalId = StringFormat.formatNew(8, StringFormat.JUST_RIGHT, ifx.getTerminalId(), '0');
                amount = StringFormat.formatNew(12, StringFormat.JUST_RIGHT, ifx.getReal_Amt()/*getAuth_Amt()*/, '0');
                String newLine = row + "|" + type + "|" + date + "|" + time + "|" + trxSeqCntr + "|" + appPan + "|"
                        + destBankId + "|" + terminalId + "|" + amount;
                fwDisAgree.write(newLine + System.getProperty("line.separator"));
            }
            fwDisAgree.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void generateCharityReport(SettlementData settlementData) {

    }
}
