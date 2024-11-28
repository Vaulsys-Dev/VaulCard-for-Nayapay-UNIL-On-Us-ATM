package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementDataReport;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.customer.Account;
import vaulsys.customer.Core;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.SwitchTerminalType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.TransactionService;
import vaulsys.util.MyLong;
import vaulsys.util.Pair;
import vaulsys.util.Util;
import vaulsys.util.ZipUtil;
import vaulsys.wfe.ProcessContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.fanap.cms.exception.BusinessException;
import com.fanap.cms.valueobjects.corecommunication.DocumentItemEntity;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DocumentItemEntityType;
import com.ghasemkiani.util.icu.PersianDateFormat;

public class InstitutionSettlementServiceImpl2 extends SettlementService {
	private static final Logger logger = Logger.getLogger(InstitutionSettlementServiceImpl2.class);
	
	public InstitutionSettlementServiceImpl2(){}
	
	public static final InstitutionSettlementServiceImpl2 Instance = new InstitutionSettlementServiceImpl2();

	private static final String bankAccount = "995,2065,3";
	private static final String pepAccount = "995,2065,2";
	private static final String fanapAccount = "995,4444,1";

	
	@Override
	public void account(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime accountUntilTime, DateTime settleUntilTime, Boolean update, Boolean waitForSyncObject, Boolean onlyFanapAccount, Boolean settleTime, Boolean considerClearingProcessType) throws Exception {
		logger.info("institution accounting: do nothing!");
	}
	
	@Override
	protected void generateSettlementDataReport(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
		logger.info("NO Settlement Data Report...");
	}
	
	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<SwitchTerminal> switchTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, SwitchTerminal.class, clearingProfile);
		if (switchTerminals != null && switchTerminals.size() > 0)
			terminals.addAll(switchTerminals);
		return terminals;
	}
	
	
	@Override
	List<String> findDesiredTerminalCodes(DateTime accountUntilTime, Boolean justToday, ClearingProfile clearingProfile) {
		List<String> terminals = new ArrayList<String>();
		Integer guaranteePeriod = 0;
		if(justToday){
			guaranteePeriod = clearingProfile.getAccountingGuaranteeMinute();
		}else{
			guaranteePeriod = clearingProfile.getSettleGuaranteeDay();			
		}
		List<String> switchTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(SwitchTerminal.class, accountUntilTime, justToday, guaranteePeriod);
		if (switchTerminals != null && switchTerminals.size() > 0)
			terminals.addAll(switchTerminals);
		return terminals;
	}

	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<SwitchTerminal> switchTerminals = TerminalService.findAllTerminals(SwitchTerminal.class, clearingProfile);
		if (switchTerminals != null && switchTerminals.size() > 0)
			terminals.addAll(switchTerminals);
		return terminals;
	}
	
	@Override
	public boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		if (FinancialEntityRole.MASTER.equals(entity.getRole()) ||
				FinancialEntityRole.PEER.equals(entity.getRole()) || 
				FinancialEntityRole.SLAVE.equals(entity.getRole())) {
			return true;
		}
		return false;
	}
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("destination");
		result.add("source");
		return result;
	}
	
	@Override
	public String getSettlementTypeDesc() {
		return "سوییچ";
	}
	
	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
		Collection<SwitchTerminal> terminals = ProcessContext.get().getAllSwitchTerminals();
//		List<SwitchTerminal> terminals = TerminalService.findAllTerminals(SwitchTerminal.class, clearingProfile);
		List<String> institutions = new ArrayList<String>();
		for (Terminal terminal: terminals) {
			if(clearingProfile.getId().equals(terminal.getOwnOrParentClearingProfileId())){
				String ownerId = terminal.getOwnerId().toString();
				if (!institutions.contains(ownerId)) {
					institutions.add(ownerId);
				}
			}
		}
		
		String desc = "پرداخت " + getSettlementTypeDesc();
		if (clearingProfile != null)
			desc += " الگوی تسویه حساب " + clearingProfile.getName();
		SettlementState settlementState = new SettlementState(clearingProfile, Core.FANAP_CORE, desc);
		
//		SettlementState settlementState = AccountingService.generateSettlementState(clearingProfile, Core.FANAP_CORE, getSettlementTypeDesc());
//		GeneralDao.Instance.save(settlementState);
		
		for (String ownerId: institutions) {
			try {
				logger.debug("getting working day for institution: " + ownerId);
				if(FinancialEntityService.getInstitutionByCode(ownerId).getLastWorkingDay() != null && 
						FinancialEntityService.getInstitutionByCode(ownerId).getLastWorkingDay().getRecievedDate() != null && 
						FinancialEntityService.getInstitutionByCode(ownerId).getCurrentWorkingDay() != null &&
						FinancialEntityService.getInstitutionByCode(ownerId).getCurrentWorkingDay().getRecievedDate() != null){
					MonthDayDate workingDay = FinancialEntityService.getInstitutionByCode(ownerId).getLastWorkingDay().getDate();
					DateTime cuttofFrom = FinancialEntityService.getInstitutionByCode(ownerId).getLastWorkingDay().getRecievedDate();
					DateTime cuttofTo = FinancialEntityService.getInstitutionByCode(ownerId).getCurrentWorkingDay().getRecievedDate();
					logger.debug("generating desired settlement report for institution: " + ownerId + " on "+ workingDay);
					settlementState = generateDesiredSettlementReports(ownerId, clearingProfile, settleDate, workingDay, cuttofFrom, cuttofTo, settlementState);
				}else{
					logger.info("one of required data is null for institution " + ownerId);
				}
			} catch(Exception e) {
				logger.error("Exception in getting working day or generating desired settlement report for institution: " + ownerId + e, e);
			}
		}
	}

	public SettlementState generateDesiredSettlementReports(String institutionId, ClearingProfile clearingProfile, DateTime settleDate, MonthDayDate workingDay, DateTime cutoffDateFromActual, DateTime cutoffDateToActual, SettlementState settlementState) throws Exception {
		logger.info("generating desired settlement report for institution "+institutionId+" on "+ workingDay);
		PersianDateFormat dateFormat = new PersianDateFormat("yyMMdd");
		String persianDate = dateFormat.format(workingDay.toDate());

		
		Institution institution = FinancialEntityService.getInstitutionByCode(institutionId);
		SwitchTerminal instAcqSwitchTerminal = ProcessContext.get().getAcquierSwitchTerminal(institutionId);
		SwitchTerminal instIssSwitchTerminal = ProcessContext.get().getIssuerSwitchTerminal(institutionId);
		

		List<SettlementData> findSettlementData = AccountingService.findSettlementData(institution, clearingProfile, SettlementDataType.MAIN, settleDate);
		
		Map<SwitchTerminalType, SettlementData> termSettleData = new HashMap<SwitchTerminalType, SettlementData>();
		
		if (findSettlementData != null && findSettlementData.size() > 0) {
			for (SettlementData settlementData: findSettlementData) {
				if (settlementData != null) {
					Long terminalId = settlementData.getTerminal().getId();
					
					if (instAcqSwitchTerminal != null && instAcqSwitchTerminal.getCode().equals(terminalId)) {
						termSettleData.put(SwitchTerminalType.ACQUIER, settlementData);
					}
					
					if (instIssSwitchTerminal != null && instIssSwitchTerminal.getCode().equals(terminalId)) {
						termSettleData.put(SwitchTerminalType.ISSUER, settlementData);
					}
					
					if (settlementData.getSettlementState() != null) {
						logger.debug("settlementData: " + settlementData.getId() + " has settleState: " + settlementData.getSettlementState().getId());
						settlementState = settlementData.getSettlementState();
					} else {
						if(settlementState == null ){
							String desc = "پرداخت " + getSettlementTypeDesc();
							if (clearingProfile != null)
								desc += " الگوی تسویه حساب " + clearingProfile.getName();
							settlementState = new SettlementState(clearingProfile, Core.FANAP_CORE, desc);
						}
						settlementState.addSettlementData(settlementData);
					}
				}
			}
		}
		
		getGeneralDao().saveOrUpdate(settlementState);
		
		Institution myInstitution = ProcessContext.get().getMyInstitution();
		final String REPORT_8_PATH = myInstitution.getAbbreviatedBankName() + persianDate + ".txt";
		final String REPORT_8REV_PATH = "R"+myInstitution.getAbbreviatedBankName() + persianDate + ".txt";
		final String REPORT_9_OLD_PATH = "MR_" + myInstitution.getBin()+ ".acq";
		// -------Moosavi : Task111686 : Add new Form 9 to be compatible to Shetab7-----
		final String REPORT_9_NEW_PATH = "MR_" + myInstitution.getBin()+"-New"+ ".acq";
		//------------------------------------------------------------------------------";
		final String REPORT_13_PATH = myInstitution.getAbbreviatedBankName() + "rep" + persianDate + ".txt";

		String[] strFileName = new String[]{REPORT_8_PATH, REPORT_8REV_PATH, REPORT_13_PATH, REPORT_9_OLD_PATH,REPORT_9_NEW_PATH};
		byte[][] bReport = new byte[5][];
		
		DateTime cutoffDateFrom = DateTime.toDateTime(cutoffDateFromActual.getTime() - 60 * DateTime.ONE_MINUTE_MILLIS);
		DateTime cutoffDateTo = DateTime.toDateTime(cutoffDateToActual.getTime() + 60 * DateTime.ONE_MINUTE_MILLIS);
		
		for(int i=0; i<bReport.length; i++){
			bReport[i]=new byte[0];
		}
		
		logger.debug("Generating report 13");
		String report13th = "";
		try {
			report13th = ShetabReconciliationService.generateTotalShetabReport(institutionId, workingDay, cutoffDateFrom, cutoffDateTo);
		} catch (Exception e) {
			logger.error("Encounter with an Exception in generating REPORT 13: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
		}
		
		
		SettlementData settleDataForReport = null;
		
		/******This part is for separating generating settlementData for form 13 from form 8******/
		
		List<SettlementData> requiredStlData = new ArrayList<SettlementData>();
		SettlementData settlementData = termSettleData.get(SwitchTerminalType.ACQUIER);
		if (settlementData == null) {
				if(instAcqSwitchTerminal != null && instIssSwitchTerminal != null){
					settlementData = new SettlementData(institution, instAcqSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
					termSettleData.put(SwitchTerminalType.ACQUIER, settlementData);
				}else if(termSettleData.get(SwitchTerminalType.ACQUIER) == null && instAcqSwitchTerminal != null){
					settlementData = new SettlementData(institution, instAcqSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
					termSettleData.put(SwitchTerminalType.ACQUIER, settlementData);
				}else if(termSettleData.get(SwitchTerminalType.ACQUIER) != null && instAcqSwitchTerminal != null)
					settlementData = termSettleData.get(SwitchTerminalType.ACQUIER);
				else if(termSettleData.get(SwitchTerminalType.ISSUER) == null && instIssSwitchTerminal != null){
					settlementData = new SettlementData(institution, instIssSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
					termSettleData.put(SwitchTerminalType.ISSUER, settlementData);
				}else if(termSettleData.get(SwitchTerminalType.ISSUER) != null && instIssSwitchTerminal != null)
					settlementData = termSettleData.get(SwitchTerminalType.ISSUER);
				else
					settlementData = null;
				if(settlementData != null){
					getGeneralDao().save(settlementData);
					settlementState.addSettlementData(settlementData);
				}
			
		} else if(settlementData.getSettlementReport() != null) {
			logger.info("settlementData: " + settlementData.getId() + " has report!");
			settlementData = null;
		}
		
		if (settlementData != null) {
			requiredStlData.add(settlementData);
		}
		
		if (settlementData != null ) {
			/*if(TransactionService.isIssuerSwitch(institutionId))
				logger.info("generateDesiredSettlementReports for terminal "+ instIssSwitchTerminal.getCode());
			else
				logger.info("generateDesiredSettlementReports for terminal "+ instAcqSwitchTerminal.getCode());*/

			
			
			if (!Util.hasText(report13th)) {
				if(instAcqSwitchTerminal != null)
					logger.info("setting thirdPartyRecordof stlData of terminal["+instAcqSwitchTerminal.getCode()+"]: report13th is empty");
				else
					logger.info("setting thirdPartyRecordof stlData of terminal["+instIssSwitchTerminal.getCode()+"]: report13th is empty");
			}
			
			bReport[2] = report13th.getBytes();
			
			settleDataForReport = settlementData;
		}
		/***********/
		
		
		
		logger.debug("Generating report 8 (issuerShetabReport)");
		
		
		settlementData = termSettleData.get(SwitchTerminalType.ACQUIER);
		if (settlementData == null) {
//			if(instAcqSwitchTerminal != null ){
			if(instIssSwitchTerminal != null ){
				if(instAcqSwitchTerminal != null)
					settlementData = new SettlementData(institution, instAcqSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
				else if(termSettleData.get(SwitchTerminalType.ISSUER) == null)
					settlementData = new SettlementData(institution, instIssSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
				else
					settlementData = termSettleData.get(SwitchTerminalType.ISSUER);
				getGeneralDao().save(settlementData);
				settlementState.addSettlementData(settlementData);
			}else{
				settlementData = null;
			}				
			
		} else if(settlementData.getSettlementReport() != null) {
			logger.info("settlementData: " + settlementData.getId() + " has report!");
			settlementData = null;
		}
		
		if (settlementData != null) {
			requiredStlData.add(settlementData);
		}
		
		if (settlementData != null ) {
			/*if(TransactionService.isIssuerSwitch(institutionId))
				logger.info("generateDesiredSettlementReports for terminal "+ instIssSwitchTerminal.getCode());
			else
				logger.info("generateDesiredSettlementReports for terminal "+ instAcqSwitchTerminal.getCode());*/

//			issuerShetabReport- Normal Transactions
			String issuerShetabReport = "";
			try {
				issuerShetabReport = ShetabReconciliationService.generateIssuerShetabReport(institutionId, workingDay, cutoffDateFrom, cutoffDateTo, false);
			} catch (Exception e) {
				logger.error("Encounter with an Exception in generating REPORT 8: "+ e.getClass().getSimpleName()+"-"+ e.getMessage(), e);
			}
			
			if (!Util.hasText(issuerShetabReport)) {
				if(instAcqSwitchTerminal != null)
					logger.info("setting thirdPartyRecord of stlData of terminal["+instAcqSwitchTerminal.getCode()+"]: issuerShetabReport is empty");
				else
					logger.info("setting thirdPartyRecord of stlData of terminal["+instIssSwitchTerminal.getCode()+"]: issuerShetabReport is empty");
			}

			logger.debug("Generating report 8 Reversal Transactions (issuerShetabReport)");
//			issuerShetabReport- Reversal Transactions
			String issuerShetabReport_Rev = "";
			try {
				issuerShetabReport_Rev = ShetabReconciliationService.generateIssuerShetabReport(institutionId, workingDay, cutoffDateFrom, cutoffDateTo, true);
			} catch (Exception e) {
				logger.error("Encounter with an Exception in generating REVERSAL REPORT 8: "+ e.getClass().getSimpleName()+"-"+ e.getMessage(),e);
			}
			
			if (!Util.hasText(issuerShetabReport_Rev)) {
				if(instAcqSwitchTerminal != null)
					logger.info("setting thirdPartyRecordof stlData of terminal["+instAcqSwitchTerminal.getCode()+"]: issuerShetabReport_Rev is empty");
				else
					logger.info("setting thirdPartyRecordof stlData of terminal["+instIssSwitchTerminal.getCode()+"]: issuerShetabReport_Rev is empty");
			}
			
//			if (!Util.hasText(report13th)) {
//				if(instAcqSwitchTerminal != null)
//					logger.info("setting thirdPartyRecordof stlData of terminal["+instAcqSwitchTerminal.getCode()+"]: report13th is empty");
//				else
//					logger.info("setting thirdPartyRecordof stlData of terminal["+instIssSwitchTerminal.getCode()+"]: report13th is empty");
//			}
			
			bReport[0] = issuerShetabReport.getBytes();
			bReport[1] = issuerShetabReport_Rev.getBytes();
//			bReport[2] = report13th.getBytes();
			
			settleDataForReport = settlementData;
		}

		settlementData = termSettleData.get(SwitchTerminalType.ISSUER);
		if (settlementData == null) {
//			if(instIssSwitchTerminal != null){
			if(instAcqSwitchTerminal != null){
				if(instIssSwitchTerminal != null)
					settlementData = new SettlementData(institution, instIssSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
				else if(termSettleData.get(SwitchTerminalType.ACQUIER) == null)
					settlementData = new SettlementData(institution, instAcqSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
				else
					settlementData = termSettleData.get(SwitchTerminalType.ACQUIER);
				getGeneralDao().save(settlementData);
				settlementState.addSettlementData(settlementData);
				
			}else{
				settlementData = null;
			}
			
		} else if(settlementData.getSettlementReport() != null) {
			logger.info("settlementData: " + settlementData.getId() + " has report!");
			settlementData = null;
		}
		
		if (settlementData != null) {
			requiredStlData.add(settlementData);
		}
		
		if (!FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
			
			if (settlementData != null) {
				if(instIssSwitchTerminal != null)
					logger.info("generateDesiredSettlementReports for terminal "+ instIssSwitchTerminal.getCode());
				else
					logger.info("generateDesiredSettlementReports for terminal "+ instAcqSwitchTerminal.getCode());
				logger.debug("Generating report 9 both old and new versions (acquirerShetabReport)");
			    //------------Moosavi : Task111686 : Add new Form 9 to be compatible to Shetab7------------
			    // ---------- add changes to this section : insterad of generating one report now it create two reports9 (old&new) and retur the pair of these two----------------
				Pair<String, String> acquirerShetabReportOldNew = new Pair<String, String>(null, null);
				try {
					acquirerShetabReportOldNew = ShetabReconciliationService.generateAcquirerShetabReport(institutionId, workingDay, cutoffDateFrom, cutoffDateTo, null, null);
				} catch (Exception e) {
					logger.error("Encounter with an Exception in generating REPORT 9: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
				}
				if (!Util.hasText(acquirerShetabReportOldNew.first)){
					if(instIssSwitchTerminal != null)
						logger.info("setting thirdPartyRecordof stlData of terminal["+instIssSwitchTerminal.getCode()+"]: acquirerShetabReportOld is empty");
					else
						logger.info("setting thirdPartyRecordof stlData of terminal["+instAcqSwitchTerminal.getCode()+"]: acquirerShetabReportOld is empty");
				}
				if (!Util.hasText(acquirerShetabReportOldNew.second)){
					if(instIssSwitchTerminal != null)
						logger.info("setting thirdPartyRecordof stlData of terminal["+instIssSwitchTerminal.getCode()+"]: acquirerShetabReportNew is empty");
					else
						logger.info("setting thirdPartyRecordof stlData of terminal["+instAcqSwitchTerminal.getCode()+"]: acquirerShetabReportNew is empty");
				}
				
				logger.debug("Generating ZIP file of reports");
				bReport[3] = acquirerShetabReportOldNew.first.getBytes();
				bReport[4] = acquirerShetabReportOldNew.second.getBytes();
				//--------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
				settleDataForReport = settlementData;
			}
			
		}

		byte[] b = ZipUtil.getZipByteArray(strFileName, bReport);
		
		if (settleDataForReport != null) {
			SettlementDataReport sdr = settleDataForReport.addThirdPartyReport(b);
			GeneralDao.Instance.saveOrUpdate(sdr);
		}
			
		try {
			PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");
			File shetabFile = new File("/home/reports/" + institution.getNameEn().toLowerCase());
//			File shetabFile = new File("c:/home/reports/" + institution.getNameEn());
			shetabFile.mkdirs();

			OutputStream fileShetab = null;
			if (!shetabFile.exists()) {
				shetabFile.createNewFile();
			}
			
			fileShetab = new FileOutputStream(shetabFile + "/" + myInstitution.getAbbreviatedBankName() + dateFormatPers.format(workingDay.toDate()) + ".zip");
			fileShetab.write(b);
			fileShetab.close();
		} catch(Exception e) {
			logger.error("can't transfer file, " + e, e);
		}
		
		getGeneralDao().endTransaction();
		getGeneralDao().beginTransaction();
		getGeneralDao().refresh(clearingProfile);
		getGeneralDao().refresh(settlementState);
		
		for(SettlementData sd:requiredStlData){
			getGeneralDao().refresh(sd);
		}
		
		try {
			issueDocumentsFrom13Report(report13th, requiredStlData, institution);
		} catch(Exception e) {
			logger.error("Exception in issue document from report13 for institution: " + institutionId + e , e);
		}
		
		return settlementState;
	}
	
	@Override
	public void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc, DateTime settleDate) throws Exception {
		logger.info("documnets issue before, do nothing!");
//		List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, null);
//		generateDocumentSettlementState(settlementStates);
	}
	@Override
	public void generateDocumentSettlementState(List<SettlementState> settlementStates) throws Exception {
		
		for (SettlementState settlementState : settlementStates) {
			Map<Terminal, List<SettlementData>> shetabTerminalSettlementData = new HashMap<Terminal, List<SettlementData>>();
			Map<Terminal, List<SettlementData>> neginTerminalSettlementData = new HashMap<Terminal, List<SettlementData>>();
			Map<Terminal, List<SettlementData>> pepTerminalSettlementData = new HashMap<Terminal, List<SettlementData>>();
			
			Map<Terminal, List<SettlementData>> map = AccountingService.getSettlementDatas(settlementState.getSettlementDatas());
			
			Map<Terminal, List<SettlementData>> terminalSettlementData = null;
			if (map != null && map.size() > 0) {
				for (Terminal terminal : map.keySet()) {
					if (terminal == null)
						continue;
					if (((Institution)terminal.getOwner()).getCode().equals(9000L/*SHETAB*/)) {
						terminalSettlementData = shetabTerminalSettlementData;
					} else if (((Institution)terminal.getOwner()).getCode().equals(639347L/*NEGIN*/)) {
						terminalSettlementData = neginTerminalSettlementData;
					} else if (((Institution)terminal.getOwner()).getCode().equals(202176L/*PEP*/)) {
						terminalSettlementData = pepTerminalSettlementData;
					}
					
					if (terminalSettlementData != null) {
						List<SettlementData> settlementDatas = getNotDocumentSettlementData(map.get(terminal));
						List<SettlementData> list = terminalSettlementData.get(terminal);
						if (list == null) {
							list = new ArrayList<SettlementData>();
							terminalSettlementData.put(terminal, list);
						}
						list.addAll(settlementDatas);
					}
				}
			}
			
//			try {
//				if (!shetabTerminalSettlementData.isEmpty())
//					generateShetabDesiredSettlementReports(shetabTerminalSettlementData, settlementState);
//			} catch (Exception e) {
//				logger.error("Error was occured in Shetab Settlement. ("+ e.getClass().getSimpleName()+": "+ e.getMessage()+")");
//			}
			
			try {
				if (!neginTerminalSettlementData.isEmpty())
					generateNeginDesiredSettlementReports(neginTerminalSettlementData, settlementState);
			} catch (Exception e) {
				logger.error("Error was occured in Negin Settlement. ("+ e.getClass().getSimpleName()+": "+ e.getMessage()+")");
			}
			
			try {
				if (!pepTerminalSettlementData.isEmpty())
					generatePEPDesiredSettlementReports(pepTerminalSettlementData, settlementState);
			} catch (Exception e) {
				logger.error("Error was occured in PEP Settlement. ("+ e.getClass().getSimpleName()+": "+ e.getMessage()+")");
			}
		}

		DateTime now = DateTime.now();
		SettlementStateType stlType = SettlementStateType.FILECREATED;
		SettledState stlState = SettledState.SENT_FOR_SETTLEMENT;
		
//		if(GlobalContext.getInstance().getPeerInstitutions() == null){
		if(ProcessContext.get().getPeerInstitutions() == null){
			stlType = SettlementStateType.AUTOSETTLED;
			stlState = SettledState.SETTLED;
		}
		
		for (SettlementState state : settlementStates) {
			state.setState(stlType);
			state.setSettlementFileCreationDate(now);
//			if(GlobalContext.getInstance().getPeerInstitutions() == null){
			if(ProcessContext.get().getPeerInstitutions() == null){
				state.setSettlementDate(now);
			}
			GeneralDao.Instance.saveOrUpdate(state);
			
			AccountingService.updateSettlementInfo(state.getSettlementDatas(), stlState);
		}
		
//		List<String> topicCodes = new ArrayList<String>();
//		topicCodes.add(FinancialEntityService.getMasterInstitution().getAccount().getAccountNumber());
////		topicCodes.add(GlobalContext.getInstance().getMyInstitution().getAccount().getAccountNumber());
//		topicCodes.add(ProcessContext.get().getMyInstitution().getAccount().getAccountNumber());
////		if (GlobalContext.getInstance().getPeerInstitutions()!= null)
//		if (ProcessContext.get().getPeerInstitutions()!= null)
////			for (Institution i:GlobalContext.getInstance().getPeerInstitutions()){
//			for (Institution i:ProcessContext.get().getPeerInstitutions()){
//				topicCodes.add(i.getAccount().getAccountNumber());
//			}
////		topicCodes.add(FinancialEntityService.getInstitutionByCode(9000L).getAccount().getAccountNumber());
////		topicCodes.add(FinancialEntityService.getInstitutionByCode(639347L).getAccount().getAccountNumber());
////		topicCodes.add(FinancialEntityService.getInstitutionByCode(502229L).getAccount().getAccountNumber());
//		AccountingService.settleSwitchTopics(topicCodes);
	}
	
	protected List<SettlementData> getNotDocumentSettlementData(List<SettlementData> settlementDatas) {
		List<SettlementData> result = new ArrayList<SettlementData>();
		for (SettlementData settlementData: settlementDatas) {
			if (settlementData != null && !Util.hasText(settlementData.getDocumentNumber()))
				result.add(settlementData);
		}
		return result;
	}

	void generateShetabDesiredSettlementReports(Map<Terminal, List<SettlementData>> termStlDataMap, SettlementState settlementState) throws Exception{
		logger.info("Generating Document SettlementReport...");
		if (termStlDataMap == null || termStlDataMap.size() == 0)
			return;

		logger.debug("issueShetabAcquireringDocument");
		issueAcquireringDocument(termStlDataMap, settlementState, AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabTransactions), "ShetabTrx");
		
		logger.debug("issueShetabFinalDocument");
		issueShetabFinalDocument(termStlDataMap, settlementState);
		
		logger.debug("All necessary documents issued...");
	}
	
	void generateNeginDesiredSettlementReports(Map<Terminal, List<SettlementData>> termStlDataMap, SettlementState settlementState) throws Exception{
		logger.info("Generating Document SettlementReport...");
		if (termStlDataMap == null || termStlDataMap.size() == 0)
			return;
		
		logger.debug("issueNeginAcquireringDocument");
		issueAcquireringDocument(termStlDataMap, settlementState, AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginTransactions), "NeginTrx");
		
		logger.debug("issueNeginFinalDocument");
		issueNeginFinalDocument(termStlDataMap, settlementState);
		
		logger.debug("All necessary documents issued...");
	}

	void generatePEPDesiredSettlementReports(Map<Terminal, List<SettlementData>> termStlDataMap, SettlementState settlementState) throws Exception{
		logger.info("Generating Document SettlementReport...");
		if (termStlDataMap == null || termStlDataMap.size() == 0)
			return;

		logger.debug("issuePEPFinalDocument");
		issuePEPFinalDocument(termStlDataMap, settlementState);
		
		logger.debug("All necessary documents issued...");
	}
	
	
	
	private void issueAcquireringDocument(Map<Terminal, List<SettlementData>> termStlDataMap, SettlementState settlementState, String issuerName, String issuerNameEn) throws BusinessException {
		Map<DateTime, List<DocumentItemEntity>> dailyDocuments = new HashMap<DateTime, List<DocumentItemEntity>>();
		Map<DateTime, Pair<Long, Long>> dailyAmounts = new HashMap<DateTime, Pair<Long,Long>>(); 
		
//		Account fanapSwitchAccount = GlobalContext.getInstance().getMyInstitution().getAccount();
		Account fanapSwitchAccount = ProcessContext.get().getMyInstitution().getAccount();
		String fanapSwitchAccountNumber = fanapSwitchAccount.getAccountNumber();
		String switchAccountNumber = "";
		String switchName = "";
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		if (termStlDataMap.keySet()!= null && termStlDataMap.keySet().size()>0){
			Terminal terminal = termStlDataMap.keySet().iterator().next();
			Institution institution = (Institution)terminal.getOwner();
			Account switchAccount = institution.getAccount();
			switchAccountNumber = switchAccount.getAccountNumber();
			switchName = institution.getName();
		}
		
		DocumentItemEntityType topic = DocumentItemEntityType.Topic;
		for (Terminal terminal : termStlDataMap.keySet()) {
			logger.debug("term:"+terminal.getCode());
			List<SettlementData> settlementDatas = termStlDataMap.get(terminal);
			if (settlementDatas != null && !settlementDatas.isEmpty()) {
				for (SettlementData settlementData: settlementDatas) {
					if (SettlementDataType.SECOND.equals(settlementData.getType()))
						continue;
					long amount = settlementData.getTotalSettlementAmount();
					if (((Long)amount).equals(0L))
						continue;
					String commentOfDocumentItem = "";
					DocumentItemEntity documentItemEntity;
					SwitchTerminal switchTerminal = GeneralDao.Instance.load(SwitchTerminal.class, terminal.getId());
//					SwitchTerminal switchTerminal = (SwitchTerminal) terminal;
					DateTime time = settlementData.getSettlementTime();
					logger.debug("amount: " + amount);
					if (SwitchTerminalType.ACQUIER.equals(switchTerminal.getType())) {
						//Do noting!
						logger.debug("Terminal is acquirer, do nothing in this case!");
					} else {
						commentOfDocumentItem = ClearingService.getDocDesc(settlementData) + " "+ switchName +" -حالت پذيرندگی";
						logger.debug("Account: " + fanapSwitchAccountNumber + " credited by the value of: " + amount);
						
						//TODO set correctly accountType in account!
//				    	if (AccountType.TOPIC.equals(fanapSwitchAccount.getType()))
//							topic = DocumentItemEntityType.Topic;
//						else if (AccountType.ACCOUNT.equals(fanapSwitchAccount.getType()))
//							topic = DocumentItemEntityType.Account;
						if (AccountingService.isTopic(fanapSwitchAccountNumber)){
							topic = DocumentItemEntityType.Topic;
						}else{
							topic = DocumentItemEntityType.Account;
						}
						documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId,
								commentOfDocumentItem, fanapSwitchAccountNumber, topic);
		
						List<DocumentItemEntity> currentDoc = dailyDocuments.get(time);
						if (currentDoc == null)
							currentDoc = new Vector<DocumentItemEntity>();
						currentDoc.add(documentItemEntity);
						dailyDocuments.put(time, currentDoc);
						Pair<Long, Long> currentAmount = dailyAmounts.get(time);
						if (currentAmount == null)
							currentAmount = new Pair<Long, Long>(0L,0L);
						currentAmount.first += amount;
						dailyAmounts.put(time, currentAmount);
					}
				}
			}
		}

		for (DateTime day : dailyDocuments.keySet()){
			List<DocumentItemEntity> documentTopicEntitys = dailyDocuments.get(day);
			long creditAmount = dailyAmounts.get(day).first;
			if (creditAmount != 0) {
				String commentOfDocumentItem = "";
				boolean debtor = true;
				if (creditAmount > 0) {
					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ToFanapDocumentTitle) + " - " 
						+ switchName;
					debtor = true;
					logger.debug("Total amount > 0; debtor = true");
					logger.debug("Account: " + switchAccountNumber + " debitted/credited by the value of: " + creditAmount);
					
					if (AccountingService.isTopic(switchAccountNumber)){
						topic = DocumentItemEntityType.Topic;
					}else{
						topic = DocumentItemEntityType.Account;
					}
					DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(creditAmount),
							debtor, switchBranchId, commentOfDocumentItem, switchAccountNumber,
							topic);
					documentTopicEntitys.add(documentItemEntity);
				} else {
					logger.debug("creditAmount < 0. Do nothing!");
				}
			}
			
			Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.FanapEODDocumentTitle)+ getDocumentPersianDate(day) + issuerName, 
							documentTopicEntitys, null, (settlementState!=null)?"stlState-"+settlementState.getId()+"-FanapEOD-"+ day+ "-"+ issuerNameEn:"FanapEOD-"+ day+ "-"+ issuerNameEn, null, null, null);
			SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
			getGeneralDao().saveOrUpdate(report);
			String transactionId = AccountingService.issueFCBDocument(report, true);
			report.setDocumentNumber(transactionId);
			getGeneralDao().saveOrUpdate(report);
			if (settlementState!= null){
				settlementState.addSettlementReport(report);
				getGeneralDao().saveOrUpdate(settlementState);
			}
			
			for (Terminal terminal : termStlDataMap.keySet()) {
				SwitchTerminal switchTerminal = GeneralDao.Instance.load(SwitchTerminal.class, terminal.getId());
				if (SwitchTerminalType.ISSUER.equals(/*((SwitchTerminal) terminal)*/switchTerminal.getType())) {
					List<SettlementData> settlementDatas = termStlDataMap.get(terminal);
					if (settlementDatas != null && !settlementDatas.isEmpty()) {						
						for (SettlementData settlementData : settlementDatas) {
							if (!SettlementDataType.SECOND.equals(settlementData.getType()) &&
									day.equals(settlementData.getSettlementTime())) {
								settlementData.setDocumentNumber(transactionId);
								getGeneralDao().saveOrUpdate(settlementData);
							}
						}
					}
				}
			}
		}
	}

	
	protected String getDocumentPersianDate(DateTime time) {
	    PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
	    return dateFormatPers.format(time.toDate());
	}
	
	private void prepareDocuments(Map<Terminal, List<SettlementData>> termStlDataMap,
			Map<DateTime, List<DocumentItemEntity>> dailyDocuments, Map<DateTime, Pair<Long, Long>> dailyAmounts,
			String switchBranchId, String switchAccount) {
		
		for (Terminal terminal : termStlDataMap.keySet()) {
			logger.debug("term:"+terminal.getCode());
			List<SettlementData> settlementDatas = termStlDataMap.get(terminal);
			
			if (settlementDatas != null && !settlementDatas.isEmpty()) {
				for (SettlementData settlementData : settlementDatas) {
					long amount = settlementData.getTotalSettlementAmount();
					if (((Long)amount).equals(0L))
						continue;
					DateTime time = settlementData.getSettlementTime();
					String commentOfDocumentItem = "";
					DocumentItemEntity documentItemEntity;
					SwitchTerminal switchTerminal = GeneralDao.Instance.load(SwitchTerminal.class, terminal.getId());
//					SwitchTerminal switchTerminal = (SwitchTerminal) terminal;
					String switchName = switchTerminal.getOwner().getName();
					logger.debug("amount: " + amount);

					List<DocumentItemEntity> currentDoc = dailyDocuments.get(time);
					if (currentDoc == null)
						currentDoc = new Vector<DocumentItemEntity>();
					Pair<Long, Long> currentAmount = dailyAmounts.get(time);
					if (currentAmount == null)
						currentAmount = new Pair<Long, Long>(0L, 0L);

					DocumentItemEntityType topic = DocumentItemEntityType.Topic;
					if (SwitchTerminalType.ACQUIER.equals(switchTerminal.getType())) {
						// debitAmount
						currentAmount.second += amount;
						commentOfDocumentItem = ClearingService.getDocDesc(settlementData) + " " + switchName + " -حالت صادرکنندگی";

						logger.debug("Account: " + switchAccount + " debitted by the value of: " + amount);
						
						//TODO set correctly accountType in account!
//				    	if (AccountType.TOPIC.equals(fanapAccount.getType()))
//							topic = DocumentItemEntityType.Topic;
//						else if (AccountType.ACCOUNT.equals(fanapAccount.getType()))
//							topic = DocumentItemEntityType.Account;
						if (AccountingService.isTopic(switchAccount))
							topic = DocumentItemEntityType.Topic;
						else
							topic = DocumentItemEntityType.Account;
						
							if (amount > 0)
								documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, commentOfDocumentItem,
										switchAccount, topic);
							else documentItemEntity = new DocumentItemEntity(new Double(-1 * amount), false, switchBranchId, commentOfDocumentItem,
									switchAccount, topic);
					} else {
						// creditAmount
						currentAmount.first += amount;
						commentOfDocumentItem = ClearingService.getDocDesc(settlementData) + " " + switchName + " -حالت پذيرندگی";
						logger.debug("Account: " + switchAccount + " credited by the value of: " + amount);
						
						//TODO set correctly accountType in account!
//				    	if (AccountType.TOPIC.equals(fanapAccount.getType()))
//							topic = DocumentItemEntityType.Topic;
//						else if (AccountType.ACCOUNT.equals(fanapAccount.getType()))
//							topic = DocumentItemEntityType.Account;
						if (AccountingService.isTopic(switchAccount))
							topic = DocumentItemEntityType.Topic;
						else
							topic = DocumentItemEntityType.Account;
						
								if (amount > 0)
									documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem,
											switchAccount, topic);

								else documentItemEntity = new DocumentItemEntity(new Double(-1 * amount), true, switchBranchId, commentOfDocumentItem,
										switchAccount, topic);
					}
					currentDoc.add(documentItemEntity);
					dailyAmounts.put(time, currentAmount);
					dailyDocuments.put(time, currentDoc);
				}
			}
		}
	}

	
	private void issueShetabFinalDocument(Map<Terminal, List<SettlementData>> termStlDataMap, SettlementState settlementState) throws BusinessException {
		Map<DateTime, List<DocumentItemEntity>> dailyDocuments = new HashMap<DateTime, List<DocumentItemEntity>>();
		Map<DateTime, Pair<Long, Long>> dailyAmounts = new HashMap<DateTime, Pair<Long,Long>>(); 
		
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		Institution institution = FinancialEntityService.getInstitutionByCode("9000");
		String shetabIntermediateAccount = institution.getCoreAccountNumber().getAccountNumber(); 
		String shetabAccount = institution.getAccount().getAccountNumber(); 
		prepareDocuments(termStlDataMap, dailyDocuments, dailyAmounts, switchBranchId, shetabAccount);
		
		
		for (DateTime day: dailyDocuments.keySet()){
			List<DocumentItemEntity> documentTopicEntitys = dailyDocuments.get(day);
			Pair<Long, Long> amounts = dailyAmounts.get(day);
			long creditAmount = amounts.first - amounts.second;
			if (creditAmount != 0) {
				String commentOfDocumentItem = "";
//				String account;
				boolean debtor = true;
				if (creditAmount > 0) {
					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabDebitDocument);
					debtor = true;
					logger.debug("Total amount > 0; debtor = true");
				} else {
					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabCreditDocument);
					debtor = false;
					creditAmount = -1* creditAmount;
					logger.debug("Total amount < 0; debtor = false");
				}
				logger.debug("Account: "+shetabIntermediateAccount + " debitted/credited by the value of: "+creditAmount);

				DocumentItemEntityType topic = DocumentItemEntityType.Topic;
				//TODO set correctly accountType in account!
//		    	if (AccountType.TOPIC.equals(fanapAccount.getType()))
//					topic = DocumentItemEntityType.Topic;
//				else if (AccountType.ACCOUNT.equals(fanapAccount.getType()))
//					topic = DocumentItemEntityType.Account;
				if (AccountingService.isTopic(shetabIntermediateAccount))
					topic = DocumentItemEntityType.Topic;
				else
					topic = DocumentItemEntityType.Account;
				DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(creditAmount),
						debtor, switchBranchId, commentOfDocumentItem, shetabIntermediateAccount,
						topic);
				documentTopicEntitys.add(documentItemEntity);
			}

			Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabEODDocumentTitle)+ getDocumentPersianDate(day), documentTopicEntitys, null
				,(settlementState!=null)?"stlState-"+settlementState.getId()+"-ShetabEOD-"+day:"ShetabEOD-"+day	, null, null, null);
			SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
			getGeneralDao().saveOrUpdate(report);
			String transactionId = AccountingService.issueFCBDocument(report, true);
			report.setDocumentNumber(transactionId);
			if (settlementState!=null){
				settlementState.addSettlementReport(report);
			}
			
			for (Terminal terminal : termStlDataMap.keySet()) {
				List<SettlementData> settlementDatas = termStlDataMap.get(terminal);
				if (settlementDatas != null && !settlementDatas.isEmpty()) {
					for (SettlementData settlementData : settlementDatas) {
						SwitchTerminal switchTerminal = GeneralDao.Instance.load(SwitchTerminal.class, terminal.getId());
						if (SwitchTerminalType.ACQUIER.equals(/*((SwitchTerminal) terminal)*/switchTerminal.getType())
								|| settlementData.getType().equals(SettlementDataType.SECOND)) {
							if (day.equals(settlementData.getSettlementTime())) {
								settlementData.setDocumentNumber(transactionId);
								getGeneralDao().saveOrUpdate(settlementData);
							}
						}
					}
				}
			}
		}
	}

	private void issueNeginFinalDocument(Map<Terminal, List<SettlementData>> termStlDataMap, SettlementState settlementState) throws BusinessException {
		Map<DateTime, List<DocumentItemEntity>> dailyDocuments = new HashMap<DateTime, List<DocumentItemEntity>>();
		Map<DateTime, Pair<Long, Long>> dailyAmounts = new HashMap<DateTime, Pair<Long,Long>>(); 
		
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		Institution institution = FinancialEntityService.getInstitutionByCode("639347");
		String neginCoreAccount = institution.getCoreAccountNumber().getAccountNumber();
		String neginAccount = institution.getAccount().getAccountNumber();
		prepareDocuments(termStlDataMap, dailyDocuments, dailyAmounts, switchBranchId, neginAccount);
		
    	PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		String persionFormat = dateFormatPers.format(DateTime.now().toDate());

    	int docSize = 0; 
    	Long documentAmount = 0L;
    	
    	String creditLine ="";
    	String debitLine ="";
    	
		for (DateTime day: dailyDocuments.keySet()) {
			List<DocumentItemEntity> documentTopicEntitys = dailyDocuments.get(day);
			Pair<Long, Long> amounts = dailyAmounts.get(day);
			long creditAmount = amounts.first - amounts.second;
			if (creditAmount != 0) {
				String commentOfDocumentItem = "";
//				String account = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginCoreAccount);
				boolean debtor = true;
				if (creditAmount > 0) {
					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginDebitDocument);
					debtor = true;
					logger.debug("Total amount > 0; debtor = true");
				} else {
					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginCreditDocument);
					debtor = false;
					creditAmount = -1* creditAmount;
					logger.debug("Total amount < 0; debtor = false");
				}
				logger.debug("Account: "+neginCoreAccount + " debitted/credited by the value of: "+creditAmount);

				DocumentItemEntityType topic = DocumentItemEntityType.Topic;
				//TODO set correctly accountType in account!
//		    	if (AccountType.TOPIC.equals(fanapAccount.getType()))
//					topic = DocumentItemEntityType.Topic;
//				else if (AccountType.ACCOUNT.equals(fanapAccount.getType()))
//					topic = DocumentItemEntityType.Account;
				if (AccountingService.isTopic(neginCoreAccount))
					topic = DocumentItemEntityType.Topic;
				else
					topic = DocumentItemEntityType.Account;
				DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(creditAmount),
						debtor, switchBranchId, commentOfDocumentItem, neginCoreAccount,
						topic);
				documentTopicEntitys.add(documentItemEntity);
				
				if (debtor){
					String settlementReportStr = "A," + bankAccount + ",-" + (creditAmount) + "," + "تسويه حساب واسط کانال فناپ مورخ " + getDocumentPersianDate(day) + "\r\n";
		    		debitLine += settlementReportStr;
		    		settlementReportStr = "A," + fanapAccount + ",+" + (creditAmount) + "," + "تسويه حساب core فناپ مورخ "  + getDocumentPersianDate(day) + "\r\n";
		    		creditLine += settlementReportStr;
				}else{
					String settlementReportStr = "A," + bankAccount + ",+" + (/*-1**/creditAmount) + "," + "تسويه حساب واسط کانال فناپ مورخ " + getDocumentPersianDate(day) + "\r\n";
		    		creditLine += settlementReportStr;
		    		settlementReportStr = "A," + fanapAccount + ",-" + (/*-1**/creditAmount) + "," + "تسويه حساب core فناپ مورخ "  + getDocumentPersianDate(day) + "\r\n";
		    		debitLine += settlementReportStr;
				}
				docSize +=2;
				documentAmount += creditAmount;
			}
			
			Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginEODDocumentTitle)+ getDocumentPersianDate(day), documentTopicEntitys, null
					, "stlState-"+settlementState.getId()+"-NeginEOD-"+day, null, null, null);
			SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
			String transactionId = AccountingService.issueFCBDocument(report, true);
			getGeneralDao().saveOrUpdate(report);
			settlementState.addSettlementReport(report);
			report.setDocumentNumber(transactionId);
			getGeneralDao().saveOrUpdate(report);
			
			
			for (Terminal terminal : termStlDataMap.keySet()) {
				List<SettlementData> settlementDatas = termStlDataMap.get(terminal);
				if (settlementDatas != null && !settlementDatas.isEmpty()) {
					for (SettlementData settlementData : settlementDatas) {
						SwitchTerminal switchTerminal = GeneralDao.Instance.load(SwitchTerminal.class, terminal.getId());
						if (SwitchTerminalType.ACQUIER.equals(/*((SwitchTerminal) terminal)*/switchTerminal.getType())
								|| settlementData.getType().equals(SettlementDataType.SECOND)) {
							if (day.equals(settlementData.getSettlementTime())) {
								settlementData.setDocumentNumber(transactionId);
								getGeneralDao().saveOrUpdate(settlementData);
							}
						}
					}
				}
			}
		}
		
		//generate NeginSettlementFile
		String settlementReportStr = "N," + persionFormat + "," + documentAmount + "," + docSize + "\r\n" 
		 							+ debitLine 
		 							+ creditLine;
		settlementState.setSettlementReport(settlementReportStr);
		settlementState.addSettlementReport(Core.NEGIN_CORE, settlementReportStr, null);
		settlementState.setState(SettlementStateType.FILECREATED);
		settlementState.setSettlementFileCreationDate(DateTime.now());
		getGeneralDao().saveOrUpdate(settlementState);
		
		int updateSettlementInfo = AccountingService.updateSettlementInfo(settlementState.getSettlementDatas(), SettledState.SENT_FOR_SETTLEMENT);
    	logger.debug(updateSettlementInfo + " settlementInfo sent for settlement " );
	}
	
	private void issuePEPFinalDocument(Map<Terminal, List<SettlementData>> termStlDataMap, SettlementState settlementState) throws BusinessException {
		Map<DateTime, List<DocumentItemEntity>> dailyDocuments = new HashMap<DateTime, List<DocumentItemEntity>>();
		Map<DateTime, Pair<Long, Long>> dailyAmounts = new HashMap<DateTime, Pair<Long,Long>>(); 
		
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		//TODO PEP's CODE?!!
		Institution institution = FinancialEntityService.getInstitutionByCode("202176");
//		String fanapCoreAccount = GlobalContext.getInstance().getMyInstitution().getAccount().getAccountNumber();
		String fanapCoreAccount = ProcessContext.get().getMyInstitution().getAccount().getAccountNumber();
		String pepCoreAccount = institution.getCoreAccountNumber().getAccountNumber();
		
		prepareDocuments(termStlDataMap, dailyDocuments, dailyAmounts, switchBranchId, fanapCoreAccount);
		
		
		
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		String persionFormat = dateFormatPers.format(DateTime.now().toDate());
		
		int docSize = 0; 
		Long documentAmount = 0L;
		
		String creditLine ="";
		String debitLine ="";
		
		for (DateTime day: dailyDocuments.keySet()) {
			List<DocumentItemEntity> documentTopicEntitys = dailyDocuments.get(day);
			String commentOfDocument ="";
			if (!documentTopicEntitys.isEmpty() && documentTopicEntitys.get(0)!= null){
				commentOfDocument = documentTopicEntitys.get(0).getComment();
				int lastIndexOfDash = commentOfDocument.lastIndexOf("-");
				lastIndexOfDash = (lastIndexOfDash ==-1)? commentOfDocument.length(): lastIndexOfDash;
				commentOfDocument = commentOfDocument.substring(0, lastIndexOfDash);
			}
			
			Pair<Long, Long> amounts = dailyAmounts.get(day);
			long creditAmount = amounts.first - amounts.second;
			if (creditAmount != 0) {
				boolean debtor = true;
				String commentOfDocumentItem = "";			
				if (creditAmount > 0) {
//					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginDebitDocument);
					commentOfDocumentItem = commentOfDocument + "- بدهکاری";
					debtor = true;
					logger.debug("Total amount > 0; debtor = true");
				} else {
//					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginCreditDocument);
					commentOfDocumentItem = commentOfDocument+ "- بستانکاری";
					debtor = false;
					creditAmount = -1* creditAmount;
					logger.debug("Total amount < 0; debtor = false");
				}
				logger.debug("Account: "+pepCoreAccount + " debitted/credited by the value of: "+creditAmount);
				
				DocumentItemEntityType topic = DocumentItemEntityType.Topic;
				if (AccountingService.isTopic(pepCoreAccount))
					topic = DocumentItemEntityType.Topic;
				else
					topic = DocumentItemEntityType.Account;
				DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(creditAmount),
						debtor, switchBranchId, commentOfDocumentItem, pepCoreAccount,
						topic);
				documentTopicEntitys.add(documentItemEntity);
				
				if (debtor){
					String settlementReportStr = "A," + pepAccount + ",-" + (creditAmount) + "," + commentOfDocument+ " مورخ " + getDocumentPersianDate(day) + "\r\n";
					debitLine += settlementReportStr;
					settlementReportStr = "A," + fanapAccount + ",+" + (creditAmount) + "," + commentOfDocument+ " مورخ " + getDocumentPersianDate(day) + "\r\n";
					creditLine += settlementReportStr;
				}else{
					String settlementReportStr = "A," + pepAccount + ",+" + (/*-1**/creditAmount) + "," + commentOfDocument+ " مورخ " + getDocumentPersianDate(day) + "\r\n";
					creditLine += settlementReportStr;
					settlementReportStr = "A," + fanapAccount + ",-" + (/*-1**/creditAmount) + "," + commentOfDocument+ " مورخ " + getDocumentPersianDate(day) + "\r\n";
					debitLine += settlementReportStr;
				}
				docSize +=2;
				documentAmount += creditAmount;
			}
			
			Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.NeginEODDocumentTitle)+ getDocumentPersianDate(day), documentTopicEntitys, null
					, "stlState-"+settlementState.getId(), null, null, null);
			SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
			getGeneralDao().saveOrUpdate(report);
			String transactionId = AccountingService.issueFCBDocument(report, true);
			report.setDocumentNumber(transactionId);
			getGeneralDao().saveOrUpdate(report);
			settlementState.addSettlementReport(report);
			
			for (Terminal terminal : termStlDataMap.keySet()) {
				List<SettlementData> settlementDatas = termStlDataMap.get(terminal);
				if (settlementDatas != null && !settlementDatas.isEmpty()) {
					for (SettlementData settlementData : settlementDatas) {
						SwitchTerminal switchTerminal = GeneralDao.Instance.load(SwitchTerminal.class, terminal.getId());
						if (SwitchTerminalType.ACQUIER.equals(/*((SwitchTerminal) terminal)*/switchTerminal.getType())
								|| settlementData.getType().equals(SettlementDataType.SECOND)) {
							if (day.equals(settlementData.getSettlementTime())) {
								settlementData.setDocumentNumber(transactionId);
								getGeneralDao().saveOrUpdate(settlementData);
							}
						}
					}
				}
			}
		}
		
		//generate PEP-NeginSettlementFile
		String settlementReportStr = "N," + persionFormat + "," + documentAmount + "," + docSize + "\r\n" 
		+ debitLine 
		+ creditLine;
		settlementState.setSettlementReport(settlementReportStr);
		settlementState.addSettlementReport(Core.NEGIN_CORE, settlementReportStr, null);
		settlementState.setState(SettlementStateType.FILECREATED);
		settlementState.setSettlementFileCreationDate(DateTime.now());
		getGeneralDao().saveOrUpdate(settlementState);
		
		int updateSettlementInfo = AccountingService.updateSettlementInfo(settlementState.getSettlementDatas(), SettledState.SENT_FOR_SETTLEMENT);
		logger.debug(updateSettlementInfo + " settlementInfo sent for settlement " );
	}
	
	public void issueDocumentsFrom13Report(String report13, List<SettlementData> settlementDatas, Institution institution) {
		if (settlementDatas == null || settlementDatas.size() == 0) {
			return;
		}
		MyLong acqMain = new MyLong(0L);
		MyLong issMain = new MyLong(0L);
		MyLong acqSec = new MyLong(0L);
		MyLong issSec = new MyLong(0L);
		pars13Report(report13, acqMain, acqSec, issMain, issSec, institution);
		
		try {
//			issueAcquireringDocument(acqMain, settlementState, institution, /*AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabTransactions)*/"تراکنشهای " + institution.getName(), (Util.hasText(institution.getNameEn())? institution.getNameEn() + "Trx" : "Trx"));
			issueShetabFinalDocument(acqMain, acqSec, issMain, issSec, settlementDatas, institution);
			
		} catch (BusinessException e) {
			logger.error("Exception in issue document for institution: " + institution.getCode() + e, e);
		}
	}

	private void issueAcquireringDocument(MyLong acqMain, SettlementState settlementState, Institution institution, String issuerName, String issuerNameEn) throws BusinessException {
		
		long amount = acqMain.value;
		if (((Long) amount).equals(0L))
			return;
		
		Account fanapSwitchAccount = ProcessContext.get().getMyInstitution().getAccount();
		String fanapSwitchAccountNumber = fanapSwitchAccount.getAccountNumber()  ;
		String switchAccountNumber = "";
		String switchName = "";
		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		Account switchAccount = institution.getAccount();
		switchAccountNumber = switchAccount.getAccountNumber();
		switchName = institution.getName();
		
		DocumentItemEntityType topic = DocumentItemEntityType.Topic;
		
		ClearingProfile clearingProfile = settlementState.getClearingProfile();
		String commentOfDocumentItem = "";
		DocumentItemEntity documentItemEntity;
		logger.debug("amount: " + amount);
		commentOfDocumentItem = ClearingService.getSettlementDataCriteria(clearingProfile, SettlementDataType.MAIN).getDocDesc() + " " + switchName + " -حالت پذيرندگی";
		logger.debug("Account: " + fanapSwitchAccountNumber + " credited by the value of: " + amount);

		if (AccountingService.isTopic(fanapSwitchAccountNumber)) {
			topic = DocumentItemEntityType.Topic;
		} else {
			topic = DocumentItemEntityType.Account;
		}
		documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, fanapSwitchAccountNumber, topic);
		

//		for (DateTime day : dailyDocuments.keySet()){
		
		DateTime day = settlementState.getSettlementDatas().get(0).getSettlementTime();
			List<DocumentItemEntity> documentTopicEntitys = new ArrayList<DocumentItemEntity>();
			documentTopicEntitys.add(documentItemEntity);
			long creditAmount = amount;
			if (creditAmount != 0) {
				commentOfDocumentItem = "";
				boolean debtor = true;
				if (creditAmount > 0) {
					commentOfDocumentItem = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ToFanapDocumentTitle) + " - " + switchName;
					debtor = true;
					logger.debug("Total amount > 0; debtor = true");
					logger.debug("Account: " + switchAccountNumber + " debitted/credited by the value of: " + creditAmount);
					
					if (AccountingService.isTopic(switchAccountNumber)){
						topic = DocumentItemEntityType.Topic;
					}else{
						topic = DocumentItemEntityType.Account;
					}
					documentItemEntity = new DocumentItemEntity(new Double(creditAmount), debtor, switchBranchId, commentOfDocumentItem, switchAccountNumber, topic);
					documentTopicEntitys.add(documentItemEntity);
				} else {
					logger.debug("creditAmount < 0. Do nothing!");
				}
			}
			
			Pair<String, String> document = AccountingService.generateFCBDocument(AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.FanapEODDocumentTitle)+ getDocumentPersianDate(day) + issuerName, 
							documentTopicEntitys, null, (settlementState!=null)?"stlState-"+settlementState.getId()+"-FanapEOD-"+ day+ "-"+ issuerNameEn:"FanapEOD-"+ day+ "-"+ issuerNameEn, null, null, null);
			SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
			getGeneralDao().saveOrUpdate(report);
			String transactionId = AccountingService.issueFCBDocument(report, true);
			report.setDocumentNumber(transactionId);
			getGeneralDao().saveOrUpdate(report);
			if (settlementState!= null){
				settlementState.addSettlementReport(report);
				getGeneralDao().saveOrUpdate(settlementState);
			}
			
			SwitchTerminal issuerSwitchTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
			
			for (SettlementData settlementData: settlementState.getSettlementDatas()) {
				if (issuerSwitchTerminal != null && settlementData.getTerminalId().equals(issuerSwitchTerminal.getId())) {
					settlementData.setDocumentNumber(transactionId);
					getGeneralDao().saveOrUpdate(settlementData);
				}
			}
	}

	private void issueShetabFinalDocument(MyLong acqMain, MyLong acqSec, MyLong issMain, MyLong issSec, List<SettlementData> settlementDatas, Institution institution)
			throws BusinessException {
		List<DocumentItemEntity> documents = new ArrayList<DocumentItemEntity>();

		String switchBranchId = AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);
		String institutionIntermediateAccount = institution.getCoreAccountNumber().getAccountNumber();
		String shetabAccount = institution.getAccount().getAccountNumber();
		
		SettlementState settlementState = settlementDatas.get(0).getSettlementState();
		
		prepareDocuments(acqMain, acqSec, issMain, issSec, settlementState.getClearingProfile(), institution, switchBranchId, shetabAccount, documents);

		int docSize = 0; 
    	Long documentAmount = 0L;
    	
    	String creditLine ="";
    	String debitLine ="";
    	
    	long acqAmount = acqMain.value - acqSec.value;
		long issAmount = issMain.value - issSec.value;
		long creditAmount = acqAmount - issAmount;
		
		for(SettlementData settlementData: settlementDatas) {
			if(ProcessContext.get().getSwitchTerminal(settlementData.getTerminal().getCode()).getType().equals(SwitchTerminalType.ACQUIER)) {
				settlementData.setTotalAmount(acqAmount);
				settlementData.setTotalSettlementAmount(acqAmount);
				getGeneralDao().saveOrUpdate(settlementData);
				
			} else if (ProcessContext.get().getSwitchTerminal(settlementData.getTerminal().getCode()).getType().equals(SwitchTerminalType.ISSUER)) {
				settlementData.setTotalAmount(issAmount);
				settlementData.setTotalSettlementAmount(issAmount);
				getGeneralDao().saveOrUpdate(settlementData);
			}
		}
		
		DateTime day = DateTime.now();
		if (settlementState != null) {
			day = settlementDatas.get(0).getSettlementTime();
		}
		
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		String persionFormat = dateFormatPers.format(DateTime.now().toDate());
    	
		if (creditAmount != 0) {
			String commentOfDocumentItem = "";
			boolean debtor = true;
			boolean isPSP = true;//TransactionService.isPSPSwitch(institution.getCode());
			if ((creditAmount > 0 && !isPSP )|| (creditAmount < 0 && isPSP)) {
				commentOfDocumentItem = /*AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabDebitDocument)*/"خالص بدهکاری " + institution.getName();
				creditAmount = Math.abs(creditAmount);
				debtor = true;
				logger.debug("Total amount > 0; debtor = true");
			} else if ((creditAmount < 0 && !isPSP )|| (creditAmount > 0 && isPSP)) {
				commentOfDocumentItem = /*AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabCreditDocument)*/"خالص بستانکاری " + institution.getName();
				debtor = false;
				creditAmount = Math.abs(creditAmount);
//				creditAmount = -1 * creditAmount;
				logger.debug("Total amount < 0; debtor = false");
			}
			logger.debug("Account: " + institutionIntermediateAccount + " debitted/credited by the value of: " + creditAmount);

			DocumentItemEntityType topic = DocumentItemEntityType.Topic;

			if (AccountingService.isTopic(institutionIntermediateAccount))
				topic = DocumentItemEntityType.Topic;
			else
				topic = DocumentItemEntityType.Account;
			DocumentItemEntity documentItemEntity = new DocumentItemEntity(new Double(creditAmount), debtor, switchBranchId, commentOfDocumentItem, institutionIntermediateAccount, topic);

			documents.add(documentItemEntity);
			
			if (institution.getCode().equals(639347L)) {
				if (debtor){
					String settlementReportStr = "A," + bankAccount + ",-" + (creditAmount) + "," + "تسويه حساب واسط کانال فناپ مورخ " + getDocumentPersianDate(day) + "\r\n";
		    		debitLine += settlementReportStr;
		    		settlementReportStr = "A," + fanapAccount + ",+" + (creditAmount) + "," + "تسويه حساب core فناپ مورخ "  + getDocumentPersianDate(day) + "\r\n";
		    		creditLine += settlementReportStr;
				}else{
					String settlementReportStr = "A," + bankAccount + ",+" + (/*-1**/creditAmount) + "," + "تسويه حساب واسط کانال فناپ مورخ " + getDocumentPersianDate(day) + "\r\n";
		    		creditLine += settlementReportStr;
		    		settlementReportStr = "A," + fanapAccount + ",-" + (/*-1**/creditAmount) + "," + "تسويه حساب core فناپ مورخ " + getDocumentPersianDate(day) + "\r\n";
		    		debitLine += settlementReportStr;
				}
				docSize +=2;
				documentAmount += creditAmount;
	
				String settlementReportStr = "N," + persionFormat + "," + documentAmount + "," + docSize + "\r\n" 
				+ debitLine 
				+ creditLine;
				settlementState.setSettlementReport(settlementReportStr);
				settlementState.addSettlementReport(Core.NEGIN_CORE, settlementReportStr, null);
				settlementState.setState(SettlementStateType.FILECREATED);
				settlementState.setSettlementFileCreationDate(DateTime.now());
				getGeneralDao().saveOrUpdate(settlementState);
			}
		}

		String nameEn = institution.getNameEn();
		String comment = Util.hasText(nameEn)? nameEn + "EOD-" : "-EOD-";
		
		Pair<String, String> document = AccountingService.generateFCBDocument(/*AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabEODDocumentTitle)*/ "تسویه حساب " + institution.getName() +  " مورخ "
				+ getDocumentPersianDate(day), documents, null, /*(settlementState != null) ? "stlState-" + settlementState.getId()
				+ "-ShetabEOD-"comment + day.getDayDate().toString().replace("/", "-") :*/ /*"ShetabEOD-"*/comment + day.getDayDate().toString().replace("/", "-"), null, null, null);
		SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
		getGeneralDao().saveOrUpdate(report);
		String transactionId = AccountingService.issueFCBDocument(report, true);
		report.setDocumentNumber(transactionId);
		
		for (SettlementData data: settlementDatas) {
			data.setSettlementReport(report);
			data.setDocumentNumber(transactionId);
		}
		
		/****************/
		if (settlementState != null) {
			settlementState.addSettlementReport(report);
			DateTime now = DateTime.now();
			SettlementStateType stlType = SettlementStateType.FILECREATED;
			
			if(ProcessContext.get().getPeerInstitutions() == null){
				stlType = SettlementStateType.AUTOSETTLED;
			}
			
			settlementState.setState(stlType);
			settlementState.setSettlementFileCreationDate(now);
			if(ProcessContext.get().getPeerInstitutions() == null){
				settlementState.setSettlementDate(now);
			}
			GeneralDao.Instance.saveOrUpdate(settlementState);
		}
		
		/****************/

		SwitchTerminal acquireSwitchTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
		for (SettlementData settlementData: settlementState.getSettlementDatas()) {
			if (acquireSwitchTerminal != null && acquireSwitchTerminal.getId().equals(settlementData.getTerminalId())) {
				settlementData.setDocumentNumber(transactionId);
				getGeneralDao().saveOrUpdate(settlementData);
			}
		}
	}
	
	private void prepareDocuments(MyLong acqMain, MyLong acqSec, MyLong issMain, MyLong issSec, ClearingProfile clearingProfile, Institution institution, String switchBranchId, String switchAccount, List<DocumentItemEntity> documents) {

		String fanapAccount = ProcessContext.get().getMyInstitution().getAccount().getAccountNumber();
		
		String commentOfDocumentItem = "";
		DocumentItemEntity documentItemEntity;
		String switchName = institution.getName();

		DocumentItemEntityType topic = DocumentItemEntityType.Topic;

		/***************************************/
		Long amount = issMain.value - issSec.value;

		commentOfDocumentItem = ClearingService.getSettlementDataCriteria(clearingProfile, SettlementDataType.MAIN).getDocDesc() + " " + switchName + " -حالت صادرکنندگی";

		logger.debug("Account: " + switchAccount + " debitted by the value of: " + amount);

		if (AccountingService.isTopic(switchAccount))
			topic = DocumentItemEntityType.Topic;
		else
			topic = DocumentItemEntityType.Account;

		if ((amount > 0  /*&&!TransactionService.isPSPSwitch(institution.getCode())*/ ) || (amount < 0 /*&& TransactionService.isPSPSwitch(institution.getCode())*/)) {
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, commentOfDocumentItem, switchAccount, topic);
		} else if ((amount < 0 /*&& !TransactionService.isPSPSwitch(institution.getCode())*/) || (amount > 0 /*&& TransactionService.isPSPSwitch(institution.getCode())*/)) {
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(/*-1 * amount*/amount), false, switchBranchId, commentOfDocumentItem, switchAccount, topic);
		
		} else {
			logger.error("Institution: " + institution.getCode() + " credited by the value of: " + amount);
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(/*-1 * amount*/amount), false, switchBranchId, commentOfDocumentItem, switchAccount, topic);
		}
		
		documents.add(documentItemEntity);
		
		/***************************************/
		amount = acqMain.value - acqSec.value;
		
		commentOfDocumentItem = ClearingService.getSettlementDataCriteria(clearingProfile, SettlementDataType.MAIN).getDocDesc() + " " + switchName + " -حالت پذيرندگی";
		
		logger.debug("Account: " + fanapAccount + " credited by the value of: " + amount);

		if (AccountingService.isTopic(fanapAccount))
			topic = DocumentItemEntityType.Topic;
		else
			topic = DocumentItemEntityType.Account;

		if ((amount > 0 /*&& !TransactionService.isPSPSwitch(institution.getCode())*/)|| (amount < 0 /*&& TransactionService.isPSPSwitch(institution.getCode())*/)) {
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, /*switchAccount*/fanapAccount,
					topic);

		}else if ((amount < 0 /*&& !TransactionService.isPSPSwitch(institution.getCode())*/) || (amount > 0 /*&& TransactionService.isPSPSwitch(institution.getCode())*/)) {
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(/*-1 * amount*/amount), true, switchBranchId, commentOfDocumentItem, /*switchAccount*/fanapAccount, topic);
		
		} else {
			logger.error("Institution: " + institution.getCode() + " debited by the value of: " + amount);
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(/*-1 * amount*/amount), true, switchBranchId, commentOfDocumentItem, /*switchAccount*/fanapAccount, topic);
		}
		
		documents.add(documentItemEntity);
		
		/***************************************/
//		amount = issSec.value;
//
//		commentOfDocumentItem = ClearingService.getSettlementDataCriteria(settlementState.getClearingProfile(), SettlementDataType.SECOND).getDocDesc() + " " + switchName + " -حالت صادرکنندگی";
//
//		logger.debug("Account: " + switchAccount + " debitted by the value of: " + amount);
//
//		if (AccountingService.isTopic(switchAccount))
//			topic = IssueGeneralDocVO.DocumentItemEntityType.Topic;
//		else
//			topic = IssueGeneralDocVO.DocumentItemEntityType.Account;
//
//		if (amount > 0)
//			documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, commentOfDocumentItem, switchAccount, topic);
//		else
//			documentItemEntity = new DocumentItemEntity(new Double(-1 * amount), false, switchBranchId, commentOfDocumentItem, switchAccount, topic);
//		
//		documents.add(documentItemEntity);
//		/***************************************/
//		amount = acqMain.value;
//		
//		commentOfDocumentItem = ClearingService.getSettlementDataCriteria(settlementState.getClearingProfile(), SettlementDataType.MAIN).getDocDesc() + " " + switchName + " -حالت پذيرندگی";
//		
//		logger.debug("Account: " + switchAccount + " credited by the value of: " + amount);
//
//		if (AccountingService.isTopic(switchAccount))
//			topic = IssueGeneralDocVO.DocumentItemEntityType.Topic;
//		else
//			topic = IssueGeneralDocVO.DocumentItemEntityType.Account;
//
//		if (amount > 0)
//			documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, switchAccount, topic);
//
//		else
//			documentItemEntity = new DocumentItemEntity(new Double(-1 * amount), true, switchBranchId, commentOfDocumentItem, switchAccount, topic);
//
//		documents.add(documentItemEntity);
		
		/***************************************/
	}
	
	private void pars13Report(String report13, MyLong acqMain, MyLong acqSec, MyLong issMain, MyLong issSec, Institution institution) {
		
		StringTokenizer tokenizer = new StringTokenizer(report13 , "\r\n");
		while(tokenizer.hasMoreTokens()) {
			StringTokenizer lineToken = new StringTokenizer(tokenizer.nextToken(), "/");
//			while(lineToken.hasMoreTokens()) {
				int isShetab = Integer.parseInt(lineToken.nextToken());
				String trnType = lineToken.nextToken();
				String terminlatype = lineToken.nextToken();
				Long amount = Long.parseLong(lineToken.nextToken());
				String debitCredit = lineToken.nextToken();
				
				/****** For ignore some Transaction for PSP switch ******/
				/*if (!TransactionService.isPSPSwitch(institution.getCode()) ||
						(TransactionService.isPSPSwitch(institution.getCode()) && (trnType.equalsIgnoreCase("PU") || trnType.equalsIgnoreCase("RF")) )) {
				
					if (isShetab == 2 && debitCredit.equals("D")) {
						acqMain.value += amount;
						
					} else if (isShetab == 1 && debitCredit.equals("C")) {
						issMain.value += amount;
						
					} else if (isShetab == 2 && debitCredit.equals("C")) {
						acqSec.value += amount;
						
					} else if (isShetab == 1 && debitCredit.equals("D")) {
						issSec.value += amount;
						
					}
				}*/
//			}
			
		}
	}
}