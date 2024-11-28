package vaulsys.clearing.report;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.PersianCalendar;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.clearing.settlement.CoreConfigDataManager;
import vaulsys.customer.Core;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.SwitchTerminalType;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.MyLong;
import vaulsys.util.Pair;
import vaulsys.wfe.GlobalContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.fanap.cms.valueobjects.corecommunication.DepositInfoForIssueDocument;
import com.fanap.cms.valueobjects.corecommunication.DocumentItemEntity;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DepositActionType;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DocumentItemEntityType;
import com.ghasemkiani.util.icu.PersianDateFormat;

public class ShetabDocumentService {
	private static transient Logger logger = Logger.getLogger(ShetabDocumentService.class);

//	public static final String FILES[] = {
//		"IRI/"+GlobalContext.getInstance().getMyInstitution().getBin()+".repbal_txt",
//		"ARZI/"+GlobalContext.getInstance().getMyInstitution().getBin()+"_BEN.repbal_txt",
//	};
	
	public static String[] getFiles(Long myBin) {
		return new String[]{
				"IRI/" + myBin + ".repbal_txt"
//				,"IRI/" + myBin + ".rep_extra_txt"
//				"ARZI/" + myBin + "_BEN.repbal_txt",
			};
	}

	public static final String AMT_TYPE_FEE = "F";
	public static final String AMT_TYPE_TRX = "A";

	// TASK Task130 [26476] - Repbal that not used script
	// TASK Task124 [16007] - New requet for Pasargad Document
	//TASK Task084 :  Add repextra indicator to shetab_repball (pasargad)	
	public static String issuePasargadShetabDocument(BufferedReader brRepbal, BufferedReader brExtra, BufferedReader brExtraYesterday, String billIdHeader) throws Exception {
		logger.debug("start issuePasargadShetabDocument");
		
		List<ShetabDocumentRecord> records = parseShetabDocument(brRepbal,RepExteraState.REPBAL_TODAY); //TASK Task084 :  Add repextra indicator to shetab_repball (pasargad)
		List<ShetabDocumentRecord> extraRecords = parseShetabDocument(brExtra,RepExteraState.REPEXTRA_TODAY); //TASK Task084 :  Add repextra indicator to shetab_repball (pasargad)
		List<ShetabDocumentRecord> extraYesterdayRecords = parseShetabDocument(brExtraYesterday,RepExteraState.REPEXTRA_YESTERDAY); //TASK Task084 :  Add repextra indicator to shetab_repball (pasargad)
		
		
		if( checkForDuplicateUsingExtraStateField(records) ){
			//duplicate entry of repbal file
			return "duplicate entry of repbal file...";
		}
		
		String strOut = "";
		
		DateTime settlementTime = null;
		if (records != null && !records.isEmpty()){
			String persianDateStr = records.get(0).persianDateStr;
			String[] dateFields = persianDateStr.split("-");
			int year = Integer.parseInt(dateFields[0]);
			int month = Integer.parseInt(dateFields[1]);
			int day = Integer.parseInt(dateFields[2]);
			DateTime persianDateTime = new DateTime(new DayDate(year, month, day), new DayTime(23, 59,59));
			settlementTime = PersianCalendar.toGregorian(persianDateTime);
		}
		
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		Institution shetab = FinancialEntityService.getInstitutionByCode("9000");
		String cbiAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIAccount); /*"1-995-1F1460-IRR-1";*/
		String cbiPaidFeeAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIPaidFeeAccount);/*"1F1317";*/
		String cbiReceivedFeeAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIReceivedFeeAccount); /*"2F2327";*/
		String cbiDisagreementAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIDisagreementAccount);/*"1-995-1F1295-IRR-1";*/
		String shetabCoreAccount = "";
		//========== extra
		String shetabExtraAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.shetabExtraAccount);
		
		shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
		
		
		DocumentItemEntityType cbiDepositAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;
		DocumentItemEntityType cbiFeePardakhtiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiFeeDaryaftiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiDisagreementAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;
		//========= extra
		DocumentItemEntityType extraAccountType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		
		List<DocumentItemEntity> mainDocument = new ArrayList<DocumentItemEntity>();
		List<DepositInfoForIssueDocument> mainDocumentDeposits = new ArrayList<DepositInfoForIssueDocument>();
		
		boolean debtor;
		
		//repbal
		/************************************************************************************************************************/
		ShetabSanadRecord repbal = getShetabSanadRecord(records);
		ShetabSanadRecord repExtra = getShetabSanadRecord(extraRecords);
		ShetabSanadRecord repExtra_y = getShetabSanadRecord(extraYesterdayRecords);	
		
		strOut += repbal.strOut;
		strOut += "================= repExtra:" + "\r\n" + repExtra.strOut;
		strOut += "================= repExtra_y:" + "\r\n" +repExtra_y.strOut;		
		
		long totalCBIAmount = repbal.totalAcquirer + repbal.totalIssuer - repbal.feePardakhti + repbal.feeDaryafti;
		long totalCBIAmount_extra = repExtra.totalAcquirer + repExtra.totalIssuer /*- repExtra.feePardakhti + repExtra.feeDaryafti*/;
		long totalCBIAmount_extra_y = repExtra_y.totalAcquirer + repExtra_y.totalIssuer /*- repExtra_y.feePardakhti + repExtra_y.feeDaryafti*/;

		/***************************************************** fee ***********************************************************/
		DocumentItemEntity feePardakhtiDocItem = new DocumentItemEntity(new Double(repbal.feePardakhti),
				true, switchBranchId, "کارمزد پرداختی بابت تراکنش های شتاب", cbiPaidFeeAccount, cbiFeePardakhtiAccType);
		DocumentItemEntity feeDaryaftiDocItem = new DocumentItemEntity(new Double(repbal.feeDaryafti),
				false, switchBranchId, "کارمزد دریافتی بابت تراکنش های شتاب", cbiReceivedFeeAccount, cbiFeeDaryaftiAccType);		
		
		strOut += "feePardakhti: " + repbal.feePardakhti + "\r\n";
		strOut += "feeDaryafti: " + repbal.feeDaryafti + "\r\n";
		
		mainDocument.add(feeDaryaftiDocItem);
		mainDocument.add(feePardakhtiDocItem);		
		
		/************************************************** disagreeAmt ********************************************************/
		logger.debug("settlementTime = " + settlementTime);
		String persianStlDate = getDocumentPersianDate(settlementTime);
		DayDate dayDate = settlementTime.getDayDate();
		DateTime oneDayBefore = new DateTime(new DayDate(dayDate.getYear(), dayDate.getMonth(), dayDate.getDay() - 1),settlementTime.getDayTime());
		String persianBeforeStlDate = getDocumentPersianDate(oneDayBefore);
		MyLong updatedDisagreeAmt =  new MyLong(repbal.disagreeAmt);
		if (persianStlDate.compareTo("13900430") < 0) {
			if (persianStlDate.compareTo("13900418") <= 0) {
				strOut += issueShetabDocumentItem(shetab, "1F1371", settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
			} else {
				strOut += issueShetabDocumentItem(shetab, "1F1371", settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
			}

		} else {
			if (persianStlDate.compareTo("13900712") <= 0) {
				strOut += issueShetabDocumentItemForRepBal(shetab, "1F1371", settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
			} else {
                                if (persianStlDate.compareTo("13910330") <= 0) {
                                        strOut += issueShetabDocumentItemForRepBal(shetab, "44441", settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
                                } else {
                                        strOut += issueShetabDocumentItemForRepBal(shetab, shetabCoreAccount, settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
                                }
			}
		}
		//===== change disagreeAmt
		updatedDisagreeAmt.value = updatedDisagreeAmt.value - (totalCBIAmount_extra_y - totalCBIAmount_extra);
		repbal.disagreeAmt = updatedDisagreeAmt.value;
		
		debtor = true;
		if (repbal.disagreeAmt <0){
			repbal.disagreeAmt *=-1;
			debtor = false;
		}
		
		DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(repbal.disagreeAmt), debtor, switchBranchId, "مابه التفاوت گزارش سوئیچ و گزارش بانک مرکزی مورخ  "+ persianStlDate, cbiDisagreementAccount,
				cbiDisagreementAccType);
		mainDocument.add(documentItemEntity);
		strOut += "disagreeAmt: " + repbal.disagreeAmt + "\r\n";

		/********************************************************** totalCBI ******************************************************/
		if (persianStlDate.compareTo("13900418") <= 0) {
			cbiAccount = "1-995-4F4444-IRR-1";
		}
		
		DocumentItemEntity totalCBI = new DocumentItemEntity(new Double(Math.abs(totalCBIAmount)), (totalCBIAmount > 0) ? true : false, switchBranchId, "تسویه بین بانکی شتاب طبق مورخ " + persianStlDate, cbiAccount, cbiDepositAccType);
		mainDocument.add(totalCBI);
		strOut += "totalCBIAmt: " + totalCBIAmount + "\r\n";
		DocumentItemEntity totalCBI_extra = new DocumentItemEntity(new Double(Math.abs(totalCBIAmount_extra)), (totalCBIAmount_extra > 0) ? false : true
				, switchBranchId, "تسویه بین بانکی شتاب بر اساس فایل اضافی مورخ" + persianStlDate, shetabExtraAccount, extraAccountType);
		mainDocument.add(totalCBI_extra);
		strOut += "totaCBIAmt_extra: " + totalCBIAmount_extra + "\r\n";
		DocumentItemEntity totalCBI_extra_y = new DocumentItemEntity(new Double(Math.abs(totalCBIAmount_extra_y)), (totalCBIAmount_extra_y > 0) ? true : false
				, switchBranchId,  "تسویه بین بانکی شتاب بر اساس فایل اضافی مورخ" + persianBeforeStlDate, shetabExtraAccount, extraAccountType);
		mainDocument.add(totalCBI_extra_y);
		strOut += "totalCBI_extra_y: " + totalCBIAmount_extra_y + "\r\n";
		
		/*********************************************************** sanad *********************************************************/
		Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabReportEODDocumentTitle)+ getDocumentPersianDate(settlementTime), mainDocument, mainDocumentDeposits,
				(billIdHeader == null ? "repbal-day:" : billIdHeader) + settlementTime.getDayDate().toString().replace("/", "-"), null, null, null);
		SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);
		String documentNumber = AccountingService.issueFCBDocument(report, true);
		report.setDocumentNumber(documentNumber);
		GeneralDao.Instance.saveOrUpdate(report);
//old		return	strOut;
		return	documentNumber; //change in 93.07.12
	}
	
	
	// TASK Task124 [16007] - New requet for Pasargad Document
	public static String issuePasargadShetabDocument(BufferedReader brRepbal, BufferedReader brExtra, BufferedReader brExtraYesterday) throws Exception {
		return issuePasargadShetabDocument(brRepbal, brExtra, brExtraYesterday, null);
	}
	
	public static ShetabSanadRecord getShetabSanadRecord(List<ShetabDocumentRecord> records){
		boolean debtor;
		ShetabSanadRecord sanad = new ShetabSanadRecord();
		for(ShetabDocumentRecord record : records){
			
			GeneralDao.Instance.saveOrUpdate(record);
			
			String desc = record.getTrxTypeDesc();
			if(record.isFee()){
				desc = "کارمزد " + desc;
			}
			
			if(record.amtTrxAcq_C > 0){
				debtor = true;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity1 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
//							new Double(record.amtTrxAcq_C), desc+"-حالت پذیرندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity1);
//				}else{
//					documentItemEntity1 = new DocumentItemEntity(new Double(record.amtTrxAcq_C),
//							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
//							cbiDepositAccType);					
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity1);
//				}
				if(record.isFee()){
					sanad.feeDaryafti += record.amtTrxAcq_C;
				}else{
					sanad.disagreeAmt -= record.amtTrxAcq_C;
					sanad.totalAcquirer += record.amtTrxAcq_C;
				}
				
				sanad.strOut += "Acquirering mode: debtor "+debtor+" "+record.amtTrxAcq_C+"\r\n";
			}
			if(record.amtTrxAcq_D > 0) {
				debtor = false;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity2 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//							new Double(record.amtTrxAcq_D), desc+"-حالت پذیرندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity2);
//				}else{
//					documentItemEntity2 = new DocumentItemEntity(new Double(record.amtTrxAcq_D),
//							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity2);
//				}
				if(record.isFee()){
					sanad.feePardakhti += record.amtTrxAcq_D;
				}else{
					sanad.disagreeAmt += record.amtTrxAcq_D;
					sanad.totalAcquirer -= record.amtTrxAcq_D;
				}
				sanad.strOut += "Acquirering mode: debtor: "+debtor+" "+record.amtTrxAcq_D+"\r\n";
			}

			if(record.amtTrxIss_C > 0) {
				debtor = true;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity3 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
//							new Double(record.amtTrxIss_C), desc+"-حالت صادرکنندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity3);
//				}else{
//					documentItemEntity3 = new DocumentItemEntity(new Double(record.amtTrxIss_C),
//							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity3);
//				}
				if(record.isFee()){
					sanad.feeDaryafti += record.amtTrxIss_C;
				}else{
					sanad.disagreeAmt -= record.amtTrxIss_C;
					sanad.totalIssuer += record.amtTrxIss_C;
				}
				sanad.strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_C+"\r\n";
			}

			if(record.amtTrxIss_D >0){
				debtor = false;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity4 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//							new Double(record.amtTrxIss_D), desc+"-حالت صادرکنندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity4);
//				}else{	
//					documentItemEntity4 = new DocumentItemEntity(new Double(record.amtTrxIss_D),
//							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity4);
//				}
				if(record.isFee()){
					sanad.feePardakhti += record.amtTrxIss_D;
				}else{
					sanad.disagreeAmt += record.amtTrxIss_D;
					sanad.totalIssuer -= record.amtTrxIss_D;
				}
				sanad.strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_D+"\r\n";
			}
		}
		return sanad;
	}
	
	public static String issueTavonShetabDocument(BufferedReader brShetabReport) throws Exception {
		List<ShetabDocumentRecord> records = parseShetabDocument(brShetabReport);

		if( checkForDuplicate(records) ){
			//duplicate entry of repbal file
			return "duplicate entry of repbal file...";
		}
		
		String strOut = "";
		
		DateTime settlementTime = null;
		if (records != null && !records.isEmpty()){
			String persianDateStr = records.get(0).persianDateStr;
			String[] dateFields = persianDateStr.split("-");
			int year = Integer.parseInt(dateFields[0]);
			int month = Integer.parseInt(dateFields[1]);
			int day = Integer.parseInt(dateFields[2]);
			DateTime persianDateTime = new DateTime(new DayDate(year, month, day), new DayTime(23, 59,59));
			settlementTime = PersianCalendar.toGregorian(persianDateTime);
		}
		long disagreeAmt = 0;
		long feePardakhti1 = 0;
		long feeDaryafti1 = 0;
		long totalIssuer1 = 0;
		long totalAcquirer1 = 0;
		
		
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		Institution shetab = FinancialEntityService.getInstitutionByCode("9000");
		String cbiAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIAccount); /*"1-995-1F1460-IRR-1";*/
		String cbiPaidFeeAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIPaidFeeAccount);/*"1F1317";*/
		String cbiReceivedFeeAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIReceivedFeeAccount); /*"2F2327";*/
		String cbiDisagreementAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIDisagreementAccount);/*"1-995-1F1295-IRR-1";*/
//		String shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
//		String shetabCoreAccount = "1635";
		String shetabCoreAccount = "";
		
//		if(GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L)){
//			shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
//		}else{
			shetabCoreAccount = "1635";
//		}

		
		DocumentItemEntityType cbiDepositAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;
		DocumentItemEntityType cbiFeePardakhtiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiFeeDaryaftiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiDisagreementAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;

		
		List<DocumentItemEntity> mainDocument = new ArrayList<DocumentItemEntity>();
//		List<DocumentItemEntity> feeDocument = new ArrayList<DocumentItemEntity>();
		List<DepositInfoForIssueDocument> mainDocumentDeposits = new ArrayList<DepositInfoForIssueDocument>();
//		List<DepositInfoForIssueDocument> feeDocumentDeposits = new ArrayList<DepositInfoForIssueDocument>();
//		DocumentItemEntity documentItemEntity1;
//		DocumentItemEntity documentItemEntity2;
//		DocumentItemEntity documentItemEntity3;
//		DocumentItemEntity documentItemEntity4;
//
//		DepositInfoForIssueDocument depositInfoEntity1;
//		DepositInfoForIssueDocument depositInfoEntity2;
//		DepositInfoForIssueDocument depositInfoEntity3;
//		DepositInfoForIssueDocument depositInfoEntity4;
//
//		boolean issueDocumentDirectlyOnCBIDeposit = true;
//		
//		if(GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L)){
//			issueDocumentDirectlyOnCBIDeposit = false;
//		}
			
		boolean debtor;
		
		for(ShetabDocumentRecord record : records){
			
			GeneralDao.Instance.saveOrUpdate(record);
			
			String desc = record.getTrxTypeDesc();
			if(record.isFee()){
				desc = "کارمزد " + desc;
			}
			
			if(record.amtTrxAcq_C > 0){
				debtor = true;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity1 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
//							new Double(record.amtTrxAcq_C), desc+"-حالت پذیرندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity1);
//				}else{
//					documentItemEntity1 = new DocumentItemEntity(new Double(record.amtTrxAcq_C),
//							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
//							cbiDepositAccType);					
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity1);
//				}
				if(record.isFee()){
					feeDaryafti1 += record.amtTrxAcq_C;
				}else{
					disagreeAmt -= record.amtTrxAcq_C;
					totalAcquirer1 += record.amtTrxAcq_C;
				}
				
				strOut += "Acquirering mode: debtor "+debtor+" "+record.amtTrxAcq_C+"\r\n";
			}
			if(record.amtTrxAcq_D > 0) {
				debtor = false;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity2 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//							new Double(record.amtTrxAcq_D), desc+"-حالت پذیرندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity2);
//				}else{
//					documentItemEntity2 = new DocumentItemEntity(new Double(record.amtTrxAcq_D),
//							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity2);
//				}
				if(record.isFee()){
					feePardakhti1 += record.amtTrxAcq_D;
				}else{
					disagreeAmt += record.amtTrxAcq_D;
					totalAcquirer1 -= record.amtTrxAcq_D;
				}
				strOut += "Acquirering mode: debtor: "+debtor+" "+record.amtTrxAcq_D+"\r\n";
			}

			if(record.amtTrxIss_C > 0) {
				debtor = true;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity3 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
//							new Double(record.amtTrxIss_C), desc+"-حالت صادرکنندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity3);
//				}else{
//					documentItemEntity3 = new DocumentItemEntity(new Double(record.amtTrxIss_C),
//							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity3);
//				}
				if(record.isFee()){
					feeDaryafti1 += record.amtTrxIss_C;
				}else{
					disagreeAmt -= record.amtTrxIss_C;
					totalIssuer1 += record.amtTrxIss_C;
				}
				strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_C+"\r\n";
			}

			if(record.amtTrxIss_D >0){
				debtor = false;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity4 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//							new Double(record.amtTrxIss_D), desc+"-حالت صادرکنندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity4);
//				}else{	
//					documentItemEntity4 = new DocumentItemEntity(new Double(record.amtTrxIss_D),
//							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity4);
//				}
				if(record.isFee()){
					feePardakhti1 += record.amtTrxIss_D;
				}else{
					disagreeAmt += record.amtTrxIss_D;
					totalIssuer1 -= record.amtTrxIss_D;
				}
				strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_D+"\r\n";
			}
		}
		
		
//		DepositInfoForIssueDocument totalAcquirer = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//				new Double(Math.abs(totalAcquirer1)), "گزارش شتاب برای حالت پذیرندگی");

//		DepositInfoForIssueDocument totalIssuer = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//				new Double(Math.abs(totalIssuer1)), "گزارش شتاب برای حالت صادرکنندگی");

		
		DocumentItemEntity feePardakhtiDocItem = new DocumentItemEntity(new Double(feePardakhti1),
				true, switchBranchId, "کارمزد پرداختی بابت تراکنش های شتاب", cbiPaidFeeAccount,
				cbiFeePardakhtiAccType);
		DocumentItemEntity feeDaryaftiDocItem = new DocumentItemEntity(new Double(feeDaryafti1),
				false, switchBranchId, "کارمزد دریافتی بابت تراکنش های شتاب", cbiReceivedFeeAccount,
				cbiFeeDaryaftiAccType);		
		
		strOut += "feePardakhti: "+feePardakhti1+"\r\n";
		strOut += "feeDaryafti: "+feeDaryafti1+"\r\n";
		
		mainDocument.add(feeDaryaftiDocItem);
		mainDocument.add(feePardakhtiDocItem);

		MyLong updatedDisagreeAmt =  new MyLong(disagreeAmt);
		strOut += issueShetabDocumentItem(shetab, shetabCoreAccount, settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
		
		disagreeAmt = updatedDisagreeAmt.value;
		
		debtor = true;
		if (disagreeAmt <0){
			disagreeAmt *=-1;
			debtor = false;
		}
		
		String persianStlDate = getDocumentPersianDate(settlementTime);
		
		DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(disagreeAmt), debtor, switchBranchId, "مابه التفاوت گزارش سوئیچ و گزارش بانک مرکزی مورخ  "+ persianStlDate, cbiDisagreementAccount,
				cbiDisagreementAccType);
		mainDocument.add(documentItemEntity);
		strOut += "disagreeAmt: "+disagreeAmt+"\r\n";
		
		long totalCBIAmount = totalAcquirer1 + totalIssuer1 - feePardakhti1 + feeDaryafti1;
		DepositInfoForIssueDocument totalCBI = new DepositInfoForIssueDocument(cbiAccount, (totalCBIAmount>0)?DepositActionType.Debtor_Deposit:DepositActionType.Creditor_Deposit, 
				new Double(Math.abs(totalCBIAmount)), /*"تسویه بین بانکی شتاب طبق گزارشات"*/"تسویه بین بانکی شتاب طبق مورخ " + persianStlDate);
		mainDocumentDeposits.add(totalCBI);
		
		
		Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabReportEODDocumentTitle)+ getDocumentPersianDate(settlementTime), mainDocument, mainDocumentDeposits,
				"repbal-day:" + settlementTime.getDayDate().toString().replace("/", "-"), null, null, null);
		SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);
		String documentNumber = AccountingService.issueFCBDocument(report, true);
		report.setDocumentNumber(documentNumber);
		GeneralDao.Instance.saveOrUpdate(report);
//		Pair<String, String> feeFCBDocument = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabFeeReportEODDocumentTitle)+ getDocumentPersianDate(settlementTime), feeDocument, null,
//				null, null, null, "fee_repbal_day:" + settlementTime.toString().replace("/", "-"));
//		SettlementReport feeReport = new SettlementReport(Core.FANAP_CORE, feeFCBDocument.first, feeFCBDocument.second, null);
//		String feeDocumentNumber = AccountingService.issueFCBDocument(feeReport, true);
//		feeReport.setDocumentNumber(feeDocumentNumber);
//		GeneralDao.Instance.saveOrUpdate(feeReport);
		
		return 
//			feeFCBDocument.first+"\r\n"+
//			((feeDocumentNumber!=null)? feeDocumentNumber: "-")+"\r\n"+ 
			strOut;
	}

	public static String issueGardeshgaryShetabDocument(BufferedReader brShetabReport) throws Exception {
		List<ShetabDocumentRecord> records = parseShetabDocument(brShetabReport);
		
		if( checkForDuplicate(records) ){
			//duplicate entry of repbal file
			return "duplicate entry of repbal file...";
		}
		
		String strOut = "";
		
		DateTime settlementTime = null;
		if (records != null && !records.isEmpty()){
			String persianDateStr = records.get(0).persianDateStr;
			String[] dateFields = persianDateStr.split("-");
			int year = Integer.parseInt(dateFields[0]);
			int month = Integer.parseInt(dateFields[1]);
			int day = Integer.parseInt(dateFields[2]);
			DateTime persianDateTime = new DateTime(new DayDate(year, month, day), new DayTime(23, 59,59));
			settlementTime = PersianCalendar.toGregorian(persianDateTime);
		}
		long disagreeAmt = 0;
		long feePardakhti1 = 0;
		long feeDaryafti1 = 0;
		long totalIssuer1 = 0;
		long totalAcquirer1 = 0;
		
		
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		Institution shetab = FinancialEntityService.getInstitutionByCode("9000");
		String cbiAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIAccount); /*"1-995-1F1460-IRR-1";*/
		String cbiPaidFeeAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIPaidFeeAccount);/*"1F1317";*/
		String cbiReceivedFeeAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIReceivedFeeAccount); /*"2F2327";*/
		String cbiDisagreementAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIDisagreementAccount);/*"1-995-1F1295-IRR-1";*/
//		String shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
//		String shetabCoreAccount = "1635";
		String shetabCoreAccount = "";
		
//		if(GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L)){
//			shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
//		}else{
		shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
//		}
		
		
		DocumentItemEntityType cbiDepositAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;
		DocumentItemEntityType cbiFeePardakhtiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiFeeDaryaftiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiDisagreementAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;
		
		
		List<DocumentItemEntity> mainDocument = new ArrayList<DocumentItemEntity>();
//		List<DocumentItemEntity> feeDocument = new ArrayList<DocumentItemEntity>();
		List<DepositInfoForIssueDocument> mainDocumentDeposits = new ArrayList<DepositInfoForIssueDocument>();
//		List<DepositInfoForIssueDocument> feeDocumentDeposits = new ArrayList<DepositInfoForIssueDocument>();
//		DocumentItemEntity documentItemEntity1;
//		DocumentItemEntity documentItemEntity2;
//		DocumentItemEntity documentItemEntity3;
//		DocumentItemEntity documentItemEntity4;
//
//		DepositInfoForIssueDocument depositInfoEntity1;
//		DepositInfoForIssueDocument depositInfoEntity2;
//		DepositInfoForIssueDocument depositInfoEntity3;
//		DepositInfoForIssueDocument depositInfoEntity4;
//
//		boolean issueDocumentDirectlyOnCBIDeposit = true;
//		
//		if(GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L)){
//			issueDocumentDirectlyOnCBIDeposit = false;
//		}
		
		boolean debtor;
		
		for(ShetabDocumentRecord record : records){
			
			GeneralDao.Instance.saveOrUpdate(record);
			
			String desc = record.getTrxTypeDesc();
			if(record.isFee()){
				desc = "کارمزد " + desc;
			}
			
			if(record.amtTrxAcq_C > 0){
				debtor = true;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity1 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
//							new Double(record.amtTrxAcq_C), desc+"-حالت پذیرندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity1);
//				}else{
//					documentItemEntity1 = new DocumentItemEntity(new Double(record.amtTrxAcq_C),
//							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
//							cbiDepositAccType);					
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity1);
//				}
				if(record.isFee()){
					feeDaryafti1 += record.amtTrxAcq_C;
				}else{
					disagreeAmt -= record.amtTrxAcq_C;
					totalAcquirer1 += record.amtTrxAcq_C;
				}
				
				strOut += "Acquirering mode: debtor "+debtor+" "+record.amtTrxAcq_C+"\r\n";
			}
			if(record.amtTrxAcq_D > 0) {
				debtor = false;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity2 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//							new Double(record.amtTrxAcq_D), desc+"-حالت پذیرندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity2);
//				}else{
//					documentItemEntity2 = new DocumentItemEntity(new Double(record.amtTrxAcq_D),
//							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity2);
//				}
				if(record.isFee()){
					feePardakhti1 += record.amtTrxAcq_D;
				}else{
					disagreeAmt += record.amtTrxAcq_D;
					totalAcquirer1 -= record.amtTrxAcq_D;
				}
				strOut += "Acquirering mode: debtor: "+debtor+" "+record.amtTrxAcq_D+"\r\n";
			}
			
			if(record.amtTrxIss_C > 0) {
				debtor = true;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity3 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
//							new Double(record.amtTrxIss_C), desc+"-حالت صادرکنندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity3);
//				}else{
//					documentItemEntity3 = new DocumentItemEntity(new Double(record.amtTrxIss_C),
//							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity3);
//				}
				if(record.isFee()){
					feeDaryafti1 += record.amtTrxIss_C;
				}else{
					disagreeAmt -= record.amtTrxIss_C;
					totalIssuer1 += record.amtTrxIss_C;
				}
				strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_C+"\r\n";
			}
			
			if(record.amtTrxIss_D >0){
				debtor = false;
//				if(issueDocumentDirectlyOnCBIDeposit){
//					depositInfoEntity4 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//							new Double(record.amtTrxIss_D), desc+"-حالت صادرکنندگی");
//					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity4);
//				}else{	
//					documentItemEntity4 = new DocumentItemEntity(new Double(record.amtTrxIss_D),
//							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
//							cbiDepositAccType);
//					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity4);
//				}
				if(record.isFee()){
					feePardakhti1 += record.amtTrxIss_D;
				}else{
					disagreeAmt += record.amtTrxIss_D;
					totalIssuer1 -= record.amtTrxIss_D;
				}
				strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_D+"\r\n";
			}
		}
		
		
//		DepositInfoForIssueDocument totalAcquirer = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//				new Double(Math.abs(totalAcquirer1)), "گزارش شتاب برای حالت پذیرندگی");
		
//		DepositInfoForIssueDocument totalIssuer = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
//				new Double(Math.abs(totalIssuer1)), "گزارش شتاب برای حالت صادرکنندگی");
		
		
		DocumentItemEntity feePardakhtiDocItem = new DocumentItemEntity(new Double(feePardakhti1),
				true, switchBranchId, "کارمزد پرداختی بابت تراکنش های شتاب", cbiPaidFeeAccount,
				cbiFeePardakhtiAccType);
		DocumentItemEntity feeDaryaftiDocItem = new DocumentItemEntity(new Double(feeDaryafti1),
				false, switchBranchId, "کارمزد دریافتی بابت تراکنش های شتاب", cbiReceivedFeeAccount,
				cbiFeeDaryaftiAccType);		
		
		strOut += "feePardakhti: "+feePardakhti1+"\r\n";
		strOut += "feeDaryafti: "+feeDaryafti1+"\r\n";
		
		mainDocument.add(feeDaryaftiDocItem);
		mainDocument.add(feePardakhtiDocItem);
		
		MyLong updatedDisagreeAmt =  new MyLong(disagreeAmt);
		strOut += issueShetabDocumentItem(shetab, shetabCoreAccount, settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
		
		disagreeAmt = updatedDisagreeAmt.value;
		
		debtor = true;
		if (disagreeAmt <0){
			disagreeAmt *=-1;
			debtor = false;
		}
		DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(disagreeAmt), debtor,switchBranchId, "مغایرت شتابی مورخ "+ getDocumentPersianDate(settlementTime), cbiDisagreementAccount,
				cbiDisagreementAccType);
		mainDocument.add(documentItemEntity);
		strOut += "disagreeAmt: "+disagreeAmt+"\r\n";
		
		long totalCBIAmount = totalAcquirer1 + totalIssuer1 - feePardakhti1 + feeDaryafti1;
		DepositInfoForIssueDocument totalCBI = new DepositInfoForIssueDocument(cbiAccount, (totalCBIAmount>0)?DepositActionType.Debtor_Deposit:DepositActionType.Creditor_Deposit, 
				new Double(Math.abs(totalCBIAmount)), "تسویه بین بانکی شتاب طبق گزارشات");
		mainDocumentDeposits.add(totalCBI);
		
		
		Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabReportEODDocumentTitle)+ getDocumentPersianDate(settlementTime), mainDocument, mainDocumentDeposits,
				"repbal-day:" + settlementTime.getDayDate().toString().replace("/", "-"), null, null, null);
		SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);
		String documentNumber = AccountingService.issueFCBDocument(report, true);
		report.setDocumentNumber(documentNumber);
		GeneralDao.Instance.saveOrUpdate(report);
//		Pair<String, String> feeFCBDocument = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabFeeReportEODDocumentTitle)+ getDocumentPersianDate(settlementTime), feeDocument, null,
//				null, null, null, "fee_repbal_day:" + settlementTime.toString().replace("/", "-"));
//		SettlementReport feeReport = new SettlementReport(Core.FANAP_CORE, feeFCBDocument.first, feeFCBDocument.second, null);
//		String feeDocumentNumber = AccountingService.issueFCBDocument(feeReport, true);
//		feeReport.setDocumentNumber(feeDocumentNumber);
//		GeneralDao.Instance.saveOrUpdate(feeReport);
		
		return 
//			feeFCBDocument.first+"\r\n"+
//			((feeDocumentNumber!=null)? feeDocumentNumber: "-")+"\r\n"+ 
		strOut;
	}
	
	public static String issueShetabDocument(BufferedReader brShetabReport) throws Exception {
		List<ShetabDocumentRecord> records = parseShetabDocument(brShetabReport);

		if( checkForDuplicate(records) ){
			//duplicate entry of repbal file
			return "duplicate entry of repbal file...";
		}
		
		String strOut = "";
		
		DateTime settlementTime = null;
		if (records != null && !records.isEmpty()){
			String persianDateStr = records.get(0).persianDateStr;
			String[] dateFields = persianDateStr.split("-");
			int year = Integer.parseInt(dateFields[0]);
			int month = Integer.parseInt(dateFields[1]);
			int day = Integer.parseInt(dateFields[2]);
			DateTime persianDateTime = new DateTime(new DayDate(year, month, day), new DayTime(23, 59,59));
			settlementTime = PersianCalendar.toGregorian(persianDateTime);
		}
		long disagreeAmt = 0;
		long feePardakhti1 = 0;
		long feeDaryafti1 = 0;
		
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		Institution shetab = FinancialEntityService.getInstitutionByCode("9000");
		String cbiAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIAccount); /*"1-995-1F1460-IRR-1";*/
		String cbiPaidFeeAccount = CoreConfigDataManager.getValue(CoreConfigDataManager.CBIPaidFeeAccount);/*"1F1317";*/
		String cbiReceivedFeeAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIReceivedFeeAccount); /*"2F2327";*/
		String cbiDisagreementAccount =  CoreConfigDataManager.getValue(CoreConfigDataManager.CBIDisagreementAccount);/*"1-995-1F1295-IRR-1";*/
//		String shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
//		String shetabCoreAccount = "1635";
		String shetabCoreAccount = "";
		
		if(GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L)){
			shetabCoreAccount = shetab.getCoreAccountNumber().getAccountNumber();
		}else{
			shetabCoreAccount = "1635";
		}

		
		DocumentItemEntityType cbiDepositAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;
		DocumentItemEntityType cbiFeePardakhtiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiFeeDaryaftiAccType = IssueGeneralDocVO.DocumentItemEntityType.Topic;
		DocumentItemEntityType cbiDisagreementAccType = IssueGeneralDocVO.DocumentItemEntityType.Account;

		
		List<DocumentItemEntity> mainDocument = new ArrayList<DocumentItemEntity>();
		List<DocumentItemEntity> feeDocument = new ArrayList<DocumentItemEntity>();
		List<DepositInfoForIssueDocument> mainDocumentDeposits = new ArrayList<DepositInfoForIssueDocument>();
		List<DepositInfoForIssueDocument> feeDocumentDeposits = new ArrayList<DepositInfoForIssueDocument>();
		DocumentItemEntity documentItemEntity1;
		DocumentItemEntity documentItemEntity2;
		DocumentItemEntity documentItemEntity3;
		DocumentItemEntity documentItemEntity4;

		DepositInfoForIssueDocument depositInfoEntity1;
		DepositInfoForIssueDocument depositInfoEntity2;
		DepositInfoForIssueDocument depositInfoEntity3;
		DepositInfoForIssueDocument depositInfoEntity4;

		boolean issueDocumentDirectlyOnCBIDeposit = true;
		
		if(GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L)){
			issueDocumentDirectlyOnCBIDeposit = false;
		}
			
		boolean debtor;
		
		for(ShetabDocumentRecord record : records){
		
			GeneralDao.Instance.saveOrUpdate(record);
			
			String desc = record.getTrxTypeDesc();
			if(record.isFee()){
				desc = "کارمزد " + desc;
			}
			
			if(record.amtTrxAcq_C > 0){
				debtor = true;
				if(issueDocumentDirectlyOnCBIDeposit){
					depositInfoEntity1 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
							new Double(record.amtTrxAcq_C), desc+"-حالت پذیرندگی");
					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity1);
				}else{
					documentItemEntity1 = new DocumentItemEntity(new Double(record.amtTrxAcq_C),
							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
							cbiDepositAccType);					
					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity1);
				}
				if(record.isFee())
					feeDaryafti1 += record.amtTrxAcq_C;
				else
					disagreeAmt -= record.amtTrxAcq_C;
				
				strOut += "Acquirering mode: debtor "+debtor+" "+record.amtTrxAcq_C+"\r\n";
			}
			if(record.amtTrxAcq_D > 0) {
				debtor = false;
				if(issueDocumentDirectlyOnCBIDeposit){
					depositInfoEntity2 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
							new Double(record.amtTrxAcq_D), desc+"-حالت پذیرندگی");
					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity2);
				}else{
					documentItemEntity2 = new DocumentItemEntity(new Double(record.amtTrxAcq_D),
							debtor, switchBranchId, desc+"-حالت پذیرندگی", cbiAccount,
							cbiDepositAccType);
					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity2);
				}
				if(record.isFee())
					feePardakhti1 += record.amtTrxAcq_D;
				else
					disagreeAmt += record.amtTrxAcq_D;
				strOut += "Acquirering mode: debtor: "+debtor+" "+record.amtTrxAcq_D+"\r\n";
			}

			if(record.amtTrxIss_C > 0) {
				debtor = true;
				if(issueDocumentDirectlyOnCBIDeposit){
					depositInfoEntity3 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Debtor_Deposit, 
							new Double(record.amtTrxIss_C), desc+"-حالت صادرکنندگی");
					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity3);
				}else{
					documentItemEntity3 = new DocumentItemEntity(new Double(record.amtTrxIss_C),
							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
							cbiDepositAccType);
					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity3);
				}
				if(record.isFee())
					feeDaryafti1 += record.amtTrxIss_C;
				else
					disagreeAmt -= record.amtTrxIss_C;
				strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_C+"\r\n";
			}

			if(record.amtTrxIss_D > 0){
				debtor = false;
				if(issueDocumentDirectlyOnCBIDeposit){
					depositInfoEntity4 = new DepositInfoForIssueDocument(cbiAccount, DepositActionType.Creditor_Deposit, 
							new Double(record.amtTrxIss_D), desc+"-حالت صادرکنندگی");
					(record.isFee() == true?feeDocumentDeposits:mainDocumentDeposits).add(depositInfoEntity4);
				}else{	
					documentItemEntity4 = new DocumentItemEntity(new Double(record.amtTrxIss_D),
							debtor, switchBranchId, desc+"-حالت صادرکنندگی", cbiAccount,
							cbiDepositAccType);
					(record.isFee() == true?feeDocument:mainDocument).add(documentItemEntity4);
				}
				if(record.isFee())
					feePardakhti1 += record.amtTrxIss_D;
				else
					disagreeAmt += record.amtTrxIss_D;
				strOut += "Issuing mode: debtor: "+debtor+" "+record.amtTrxIss_D+"\r\n";
			}
		}
		
		DocumentItemEntity feePardakhtiDocItem = new DocumentItemEntity(new Double(feePardakhti1),
				true, switchBranchId, "کارمزد پرداختی بابت تراکنش های شتاب", cbiPaidFeeAccount,
				cbiFeePardakhtiAccType);
		DocumentItemEntity feeDaryaftiDocItem = new DocumentItemEntity(new Double(feeDaryafti1),
				false, switchBranchId, "کارمزد دریافتی بابت تراکنش های شتاب", cbiReceivedFeeAccount,
				cbiFeeDaryaftiAccType);		
		
		strOut += "feePardakhti: "+feePardakhti1+"\r\n";
		strOut += "feeDaryafti: "+feeDaryafti1+"\r\n";
		
		feeDocument.add(feeDaryaftiDocItem);
		feeDocument.add(feePardakhtiDocItem);

		MyLong updatedDisagreeAmt =  new MyLong(disagreeAmt);
		strOut += issueShetabDocumentItem(shetab, shetabCoreAccount, settlementTime, updatedDisagreeAmt , switchBranchId, mainDocument);
		
		disagreeAmt = updatedDisagreeAmt.value;
		
		debtor = true;
		if (disagreeAmt <0){
			disagreeAmt *=-1;
			debtor = false;
		}
		DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(disagreeAmt), debtor, switchBranchId, "مغایرت شتابی مورخ "+ getDocumentPersianDate(settlementTime), cbiDisagreementAccount,
				cbiDisagreementAccType);
		mainDocument.add(documentItemEntity);
		strOut += "disagreeAmt: "+disagreeAmt+"\r\n";
		
		Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabReportEODDocumentTitle)+ getDocumentPersianDate(settlementTime), mainDocument, null,
				null, null, null, "repbal-day:" + settlementTime.toString().replace("/", "-"));
		SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);
		String documentNumber = AccountingService.issueFCBDocument(report, true);
		report.setDocumentNumber(documentNumber);
		GeneralDao.Instance.saveOrUpdate(report);
		Pair<String, String> feeFCBDocument = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabFeeReportEODDocumentTitle)+ getDocumentPersianDate(settlementTime), feeDocument, null,
				null, null, null, "fee_repbal_day:" + settlementTime.toString().replace("/", "-"));
		SettlementReport feeReport = new SettlementReport(Core.FANAP_CORE, feeFCBDocument.first, feeFCBDocument.second, null);
		String feeDocumentNumber = AccountingService.issueFCBDocument(feeReport, true);
		feeReport.setDocumentNumber(feeDocumentNumber);
		GeneralDao.Instance.saveOrUpdate(feeReport);
		
		return 
			feeFCBDocument.first+"\r\n"+
			((feeDocumentNumber!=null)? feeDocumentNumber: "-")+"\r\n"+ 
			strOut;
	}

	
	private static boolean checkForDuplicate(List<ShetabDocumentRecord> records) {
		String query = "select count(*) from ShetabDocumentRecord where persianDateStr = :persianDateStr";
		
		//check with date of one sample... 
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("persianDateStr", records.get(0).getPersianDateStr());
		
		Long count = (Long) GeneralDao.Instance.findObject(query, params);
		
		if(count > 0)
			return true;
		
		return false;
	}
	
	//Relative TASK Task060 : Resalat sanad repball
	//TASK Task061 : Add repextra indicator to shetab_repball
	//TASK Task084 : Add repextra indicator to shetab_repball (Pasargad)
	private static boolean checkForDuplicateUsingExtraStateField(List<ShetabDocumentRecord> records) {
		String query = "select count(*) from ShetabDocumentRecord where persianDateStr = :persianDateStr and repExtraState = :state";
		
		//check with date of one sample... 
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("persianDateStr", records.get(0).getPersianDateStr());
		params.put("state", RepExteraState.REPBAL_TODAY);
		
		Long count = (Long) GeneralDao.Instance.findObject(query, params);
		
		if(count > 0)
			return true;
		
		return false;
	}	

	protected static String getDocumentPersianDate(DateTime time) {
//		MyDateFormat formatYYYYMMDD = new MyDateFormat("yyyyMMdd");
//		return formatYYYYMMDD.format(PersianCalendar.getPersianDayDate(time.toDate()));
	    PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		return dateFormatPers.format(time.toDate());
	}
	
	private static String issueShetabDocumentItem(Institution shetab, String shetabCoreAccount, DateTime settlementTime, MyLong disagreeAmt,
			String switchBranchId,List<DocumentItemEntity> mainDocument) {
		logger.debug("Start issueShetabDocumentItem for " + shetabCoreAccount + " & settlementTime = " + settlementTime);
		String strOut = "issueShetabDocumentItem:::::";
		for (Terminal terminal : shetab.getTerminals()) {
			if (TerminalType.SWITCH.equals(terminal.getTerminalType())) {
				SwitchTerminal switchTerminal = (SwitchTerminal) terminal;
				String switchName = switchTerminal.getOwner().getName();
				List<SettlementData> settlementData = AccountingService.findSettlementData(shetab, terminal, settlementTime.getDayDate());
				String commentOfDocumentItem = "";
				DocumentItemEntity documentItemEntity = null;
				for (SettlementData data : settlementData) {
					long amount = data.getTotalSettlementAmount();
					DateTime time = data.getSettlementTime();
					DocumentItemEntityType topic = IssueGeneralDocVO.DocumentItemEntityType.Topic;
					if (AccountingService.isTopic(shetabCoreAccount))
						topic = IssueGeneralDocVO.DocumentItemEntityType.Topic;
					else
						topic = IssueGeneralDocVO.DocumentItemEntityType.Account;

					if (SwitchTerminalType.ACQUIER.equals(switchTerminal.getType())) {
						commentOfDocumentItem = ClearingService.getDocDesc(data) + " " + switchName
								+ " -حالت صادرکنندگی";
						strOut += "Issuing mode: ";
						
						if (amount > 0) {
							strOut += "debtor: true amount: "+amount;
							documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId,
									commentOfDocumentItem, shetabCoreAccount, topic);
						} else {
							strOut += "debtor: false amount: "+(-1 * amount);
							documentItemEntity = new DocumentItemEntity(new Double(-1 * amount), false,
									switchBranchId, commentOfDocumentItem, shetabCoreAccount, topic);
						}
						disagreeAmt.value -= amount;
						
					} else if (SwitchTerminalType.ISSUER.equals(switchTerminal.getType())) {
						commentOfDocumentItem = ClearingService.getDocDesc(data) + " " + switchName
								+ " -حالت پذيرندگی";

						strOut += "Acquiring mode: ";
						if (amount > 0){
							strOut += "debtor: false amount: "+amount;
							documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId,
									commentOfDocumentItem, shetabCoreAccount, topic);
						}else{
							strOut += "debtor: true amount: "+(-1 * amount);
							documentItemEntity = new DocumentItemEntity(new Double(-1* amount), true,
									switchBranchId, commentOfDocumentItem, shetabCoreAccount, topic);
						}
						disagreeAmt.value += amount;
					}
					if (documentItemEntity != null){
						mainDocument.add(documentItemEntity);
//						strOut += documentItemEntity.toString();
						strOut += "\r\n";
					}
				}
			}
		}
		return strOut;
	}
	
	private static String issueShetabDocumentItemForRepBal(Institution shetab, String shetabCoreAccount, DateTime settlementTime, MyLong disagreeAmt,
			String switchBranchId,List<DocumentItemEntity> mainDocument) {
		logger.debug("Start issueShetabDocumentItemForRepBal for " + shetabCoreAccount + " & settlementTime = " + settlementTime);
		String strOut = "issueShetabDocumentItem:::::";
		for (Terminal terminal : shetab.getTerminals()) {
			if (TerminalType.SWITCH.equals(terminal.getTerminalType())) {
				SwitchTerminal switchTerminal = (SwitchTerminal) terminal;
				String switchName = switchTerminal.getOwner().getName();
				List<SettlementData> settlementData = AccountingService.findSettlementData(shetab, terminal, settlementTime.getDayDate());
				logger.debug("Continue issueShetabDocumentItemForRepBal...");
				String commentOfDocumentItem = "";
				DocumentItemEntity documentItemEntity = null;
				for (SettlementData data : settlementData) {
					long amount = data.getTotalSettlementAmount();
					DateTime time = data.getSettlementTime();
					DocumentItemEntityType topic = IssueGeneralDocVO.DocumentItemEntityType.Topic;
					if (AccountingService.isTopic(shetabCoreAccount))
						topic = IssueGeneralDocVO.DocumentItemEntityType.Topic;
					else
						topic = IssueGeneralDocVO.DocumentItemEntityType.Account;

					if (SwitchTerminalType.ACQUIER.equals(switchTerminal.getType())) {
						commentOfDocumentItem = ClearingService.getDocDesc(data) + " " + switchName
								+ " -حالت صادرکنندگی";
						strOut += "Issuing mode: ";
						
						if (amount > 0) {
							strOut += "debtor: false amount: "+amount;
							documentItemEntity = new DocumentItemEntity(new Double(amount), false,
									switchBranchId, commentOfDocumentItem, shetabCoreAccount, topic);
						} else {
							strOut += "debtor: true amount: "+(-1 * amount);
							documentItemEntity = new DocumentItemEntity(new Double(-1 * amount), true, switchBranchId,
									commentOfDocumentItem, shetabCoreAccount, topic);
						}
						disagreeAmt.value += amount;
						
					} else if (SwitchTerminalType.ISSUER.equals(switchTerminal.getType())) {
						commentOfDocumentItem = ClearingService.getDocDesc(data) + " " + switchName
								+ " -حالت پذيرندگی";

						strOut += "Acquiring mode: ";
						if (amount > 0){
							strOut += "debtor: true amount: "+amount;
							documentItemEntity = new DocumentItemEntity(new Double(amount), true,
									switchBranchId, commentOfDocumentItem, shetabCoreAccount, topic);
						}else{
							strOut += "debtor: false amount: "+(-1 * amount);
							documentItemEntity = new DocumentItemEntity(new Double(-1 * amount), false, switchBranchId,
									commentOfDocumentItem, shetabCoreAccount, topic);
						}
						disagreeAmt.value -= amount;
					}
					if (documentItemEntity != null){
						mainDocument.add(documentItemEntity);
//						strOut += documentItemEntity.toString();
						strOut += "\r\n";
					}
				}
			}
		}
		return strOut;
	}

	public static List<ShetabDocumentRecord> parseShetabDocument(BufferedReader brShetabReport) throws IOException {
		List<ShetabDocumentRecord> records = new ArrayList<ShetabDocumentRecord>(); 
		
		String reportRecord;
		while (brShetabReport.ready()) {
			if ((reportRecord = brShetabReport.readLine()).length() > 0) {
				reportRecord = reportRecord.trim();
				
				records.add(parseRecord(reportRecord));
			}
		}
		
		return records;
	}
	
	//Relative TASK Task060 : Resalat sanad repball
	//TASK Task061 : Add repextra indicator to shetab_repball
	public static List<ShetabDocumentRecord> parseShetabDocument(BufferedReader brShetabReport,RepExteraState repExtraState) throws IOException {
		List<ShetabDocumentRecord> records = new ArrayList<ShetabDocumentRecord>(); 
		
		String reportRecord;
		while (brShetabReport.ready()) {
			if ((reportRecord = brShetabReport.readLine()).length() > 0) {
				reportRecord = reportRecord.trim();
				
				records.add(parseRecord(reportRecord,repExtraState));
			}
		}
		
		return records;
	}	

	//Relative TASK Task060 : Resalat sanad repball
	//TASK Task061 : Add repextra indicator to shetab_repball
	//TASK Task084 : Add repextra indicator to shetab_repball (Pasargad)
	private static ShetabDocumentRecord parseRecord(String reportRecord,RepExteraState repExtraState) { 
		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(reportRecord, "|");

		ShetabDocumentRecord record = new ShetabDocumentRecord();
		record.trxType = tokenizer.nextToken().trim();

		record.numTrxAcq_C = new Long(tokenizer.nextToken().trim());
		record.amtTrxAcq_C = new Long(tokenizer.nextToken().trim());
		record.numTrxAcq_D = new Long(tokenizer.nextToken().trim());
		record.amtTrxAcq_D = new Long(tokenizer.nextToken().trim());

		record.numTrxIss_C = new Long(tokenizer.nextToken().trim());
		record.amtTrxIss_C = new Long(tokenizer.nextToken().trim());
		record.numTrxIss_D = new Long(tokenizer.nextToken().trim());
		record.amtTrxIss_D = new Long(tokenizer.nextToken().trim());

		// +:C / -:D
		record.amtTrxTotal = new Long(tokenizer.nextToken().trim());
		
		// A: transaction / F: fee
		record.strAmtType = tokenizer.nextToken().trim();
		
//		MyDateFormat dateFormat = new MyDateFormat("yyyyMMdd");
		String dateStr = tokenizer.nextToken().trim();
		record.persianDateStr = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" +dateStr.substring(6,8);
		
		//Relative TASK Task060 : Resalat sanad repball
		//TASK Task061 : Add repextra indicator to shetab_repball
		//TASK Task084 : Add repextra indicator to shetab_repball (Pasargad)
		record.repExtraState = repExtraState;
		
		return record;
	}
	
	private static ShetabDocumentRecord parseRecord(String reportRecord) { 
		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(reportRecord, "|");

		ShetabDocumentRecord record = new ShetabDocumentRecord();
		record.trxType = tokenizer.nextToken().trim();

		record.numTrxAcq_C = new Long(tokenizer.nextToken().trim());
		record.amtTrxAcq_C = new Long(tokenizer.nextToken().trim());
		record.numTrxAcq_D = new Long(tokenizer.nextToken().trim());
		record.amtTrxAcq_D = new Long(tokenizer.nextToken().trim());

		record.numTrxIss_C = new Long(tokenizer.nextToken().trim());
		record.amtTrxIss_C = new Long(tokenizer.nextToken().trim());
		record.numTrxIss_D = new Long(tokenizer.nextToken().trim());
		record.amtTrxIss_D = new Long(tokenizer.nextToken().trim());

		// +:C / -:D
		record.amtTrxTotal = new Long(tokenizer.nextToken().trim());
		
		// A: transaction / F: fee
		record.strAmtType = tokenizer.nextToken().trim();
		
//		MyDateFormat dateFormat = new MyDateFormat("yyyyMMdd");
		String dateStr = tokenizer.nextToken().trim();
		record.persianDateStr = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" +dateStr.substring(6,8);
		
		return record;
	}
}

