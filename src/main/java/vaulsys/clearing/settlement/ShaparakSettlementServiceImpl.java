package vaulsys.clearing.settlement;

import com.fanap.cms.exception.BusinessException;
import com.fanap.cms.valueobjects.corecommunication.DocumentItemEntity;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO.DocumentItemEntityType;
import com.ghasemkiani.util.icu.PersianDateFormat;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.*;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
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
import vaulsys.util.*;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

//import vaulsys.transaction.TransactionType;

public class ShaparakSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(ShaparakSettlementServiceImpl.class);
	
	public ShaparakSettlementServiceImpl(){}
	
	public static final ShaparakSettlementServiceImpl Instance = new ShaparakSettlementServiceImpl();

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
		return "سوییچ شاپرک";
	}
	
	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate) throws Exception {
		Collection<SwitchTerminal> terminals = ProcessContext.get().getAllSwitchTerminals();
		List<String> institutions = new ArrayList<String>();
		for (Terminal terminal: terminals) {
			if(clearingProfile.getId().equals(terminal.getOwnOrParentClearingProfileId())){
				String ownerId = terminal.getOwnerId().toString();
				if (!institutions.contains(ownerId)) {
					institutions.add(ownerId);
				}
			}
		}
		/********************************************************************************************/
		String desc = "پرداخت " + getSettlementTypeDesc();
		if (clearingProfile != null)
			desc += " الگوی تسویه حساب " + clearingProfile.getName();
		SettlementState settlementState = new SettlementState(clearingProfile, Core.FANAP_CORE, desc);
		/********************************************************************************************/
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

	public SettlementState generateDesiredSettlementReports(String shaparakId, ClearingProfile clearingProfile, DateTime settleDate, MonthDayDate workingDay, DateTime cutoffDateFromActual, DateTime cutoffDateToActual, SettlementState settlementState) throws Exception {
		logger.info("generating desired settlement report for shaparak " + shaparakId + " on " + workingDay);
		PersianDateFormat dateFormat = new PersianDateFormat("yyMMdd");
		String persianDate = dateFormat.format(workingDay.toDate());

		
		Institution shaparak = FinancialEntityService.getInstitutionByCode(shaparakId);
		SwitchTerminal instIssSwitchTerminal = ProcessContext.get().getIssuerSwitchTerminal(shaparakId);

		List<SettlementData> findSettlementData = AccountingService.findSettlementData(shaparak, clearingProfile, SettlementDataType.MAIN, settleDate);
		
		Map<SwitchTerminalType, SettlementData> termSettleData = new HashMap<SwitchTerminalType, SettlementData>();
		
		if (findSettlementData != null && findSettlementData.size() > 0) {
			for (SettlementData settlementData: findSettlementData) {
				if (settlementData != null) {
					Long terminalId = settlementData.getTerminal().getId();
					
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
		final String REPORT_Acq_PATH = "PEP_" + myInstitution.getBin() + ".Acq";
		final String REPORT_Acq_Inq_PATH = "PEP_" + myInstitution.getBin() + ".Acq.Inq";
		final String REPORT_Acq_Rev_PATH = "PEP_" + myInstitution.getBin() + ".Acq.Rev";
		final String REPORT_Acq_App_PATH = "PEP_" + myInstitution.getBin() + ".Acq.App";
		

		String[] strFileName = new String[]{REPORT_Acq_PATH, REPORT_Acq_Inq_PATH, REPORT_Acq_Rev_PATH, REPORT_Acq_App_PATH};
		byte[][] bReport = new byte[4][];
		
		DateTime cutoffDateFrom = DateTime.toDateTime(cutoffDateFromActual.getTime() - 60 * DateTime.ONE_MINUTE_MILLIS);
		DateTime cutoffDateTo = DateTime.toDateTime(cutoffDateToActual.getTime() + 60 * DateTime.ONE_MINUTE_MILLIS);
		
		for(int i=0; i<bReport.length; i++){
			bReport[i]=new byte[0];
		}
		
		SettlementData settleDataForReport = null;
		/******This part is for separating generating settlementData for form 13 from form 8******/
		
		List<SettlementData> requiredStlData = new ArrayList<SettlementData>();
		SettlementData settlementData = termSettleData.get(SwitchTerminalType.ISSUER);
		if (settlementData == null) {
				if(termSettleData.get(SwitchTerminalType.ISSUER) == null && instIssSwitchTerminal != null){
					settlementData = new SettlementData(shaparak, instIssSwitchTerminal, clearingProfile, SettlementDataType.MAIN, settleDate);
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
		
		if (settlementData != null) 
			requiredStlData.add(settlementData);
		
		if (settlementData != null ) 
			logger.info("generateDesiredSettlementReports for terminal "+ instIssSwitchTerminal.getCode());

		/********************************************************************************************/
		
		/*if (!FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
			
			if (settlementData != null) {
				if(instIssSwitchTerminal != null)
					logger.info("generateDesiredSettlementReports for terminal "+ instIssSwitchTerminal.getCode());

				*//*********************************************** Generating Acq Report *********************************************//*
				logger.debug("Generating acquirer shaparak report");
				String acqShaparakReport = "";
				try {
					acqShaparakReport = ShetabReconciliationService.generateAcqShaparakReport(shaparakId, workingDay,
							cutoffDateFrom, cutoffDateTo, null, null,false, false);
				} catch (Exception e) {
					logger.error("Encounter with an Exception in generating shaparak Acq REPORT: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
				}
				if (!Util.hasText(acqShaparakReport)){
					if(instIssSwitchTerminal != null)
						logger.info("setting thirdPartyRecordof stlData of terminal["+instIssSwitchTerminal.getCode()+"]: acquirerShaparakReport is empty");
				}
				bReport[0] = acqShaparakReport.getBytes();					
				*//******************************************************************************************************************//*
				
				
				*//********************************************* Generating Acq_Inq Report *******************************************//*
				logger.debug("Generating acquirer_Inq report");
				String acqInqShaparakReport = "";
				try{
					acqInqShaparakReport = ShetabReconciliationService.generateAcqShaparakReport(shaparakId, workingDay,
							cutoffDateFrom, cutoffDateTo, null, null, true, false);
				}catch(Exception e){
					logger.error("Encounter with an Exception in generating shaparak AcqInq REPORT: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
				}
				if(!Util.hasText(acqInqShaparakReport)){
					if(instIssSwitchTerminal != null)
						logger.info("");
				}
				bReport[1] = acqInqShaparakReport.getBytes();
				*//******************************************************************************************************************//*
				
				
				*//********************************************* Generating Acq_Rev Report *******************************************//*
				logger.debug("Generating acquirer_Rev report");
				String acqRevShaparakReport = "";
				try{
					acqRevShaparakReport = ShetabReconciliationService.generateAppShaparakReport(shaparakId, workingDay,
							cutoffDateFrom, cutoffDateTo, null, null, false,true);
				}catch(Exception e){
					logger.error("Encounter with an Exception in generating shaparak AcqRev REPORT: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
				}
				if(!Util.hasText(acqRevShaparakReport))
					logger.info("");
				bReport[2] = acqRevShaparakReport.getBytes();
				*//******************************************************************************************************************//*
				
				
				*//********************************************* Generating Acq_App Report *******************************************//*
				logger.debug("Generating acquirer_App report");
				String acqAppShaparakReport = "";
				try{
					acqAppShaparakReport = ShetabReconciliationService.generateAppShaparakReport(shaparakId, workingDay,
							cutoffDateFrom, cutoffDateTo, null, null,false ,false);
				}catch(Exception e){
					logger.error("Encounter with an Exception in generating shaparak AcqApp REPORT: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
				}
				if(!Util.hasText(acqRevShaparakReport))
					logger.info("");
				bReport[3] = acqAppShaparakReport.getBytes();
				*//******************************************************************************************************************//*
				
				logger.debug("Generating ZIP file of reports");
				settleDataForReport = settlementData;
			}
			
		}*/

		byte[] b = ZipUtil.getZipByteArray(strFileName, bReport);
		
		if (settleDataForReport != null) {
			SettlementDataReport sdr = settleDataForReport.addThirdPartyReport(b);
			GeneralDao.Instance.saveOrUpdate(sdr);
		}
			
		try {
			PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");
			File shetabFile = new File("/home/reports/" + shaparak.getNameEn().toLowerCase());
			shetabFile.mkdirs();

			OutputStream fileShaparak = null;
			if (!shetabFile.exists()) {
				shetabFile.createNewFile();
			}
			
			fileShaparak = new FileOutputStream(shetabFile + "/" + myInstitution.getAbbreviatedBankName() + dateFormatPers.format(workingDay.toDate()) + ".zip");
			fileShaparak.write(b);
			fileShaparak.close();
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
		
		return settlementState;
	}
	
	@Override
	public void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc, DateTime settleDate) throws Exception {
		logger.info("documnets issue before, do nothing!");
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
		
		if(ProcessContext.get().getPeerInstitutions() == null){
			stlType = SettlementStateType.AUTOSETTLED;
			stlState = SettledState.SETTLED;
		}
		
		for (SettlementState state : settlementStates) {
			state.setState(stlType);
			state.setSettlementFileCreationDate(now);
			if(ProcessContext.get().getPeerInstitutions() == null){
				state.setSettlementDate(now);
			}
			GeneralDao.Instance.saveOrUpdate(state);
			
			AccountingService.updateSettlementInfo(state.getSettlementDatas(), stlState);
		}
		
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
    	
    	boolean isIss =true;// TransactionService.isIssuerSwitch(institution.getCode());
    	
    	long acqAmount = acqMain.value - acqSec.value;
		long issAmount = issMain.value - issSec.value;
		long creditAmount;
		Institution shetab= null;
		Institution taavon = null;
		if(isIss){
			acqAmount = acqSec.value - acqMain.value;
			shetab = GeneralDao.Instance.getObject(Institution.class, 9000L);
			taavon = GeneralDao.Instance.getObject(Institution.class, 502908L);
			creditAmount = acqAmount + issAmount;
		}
		else			
			creditAmount = acqAmount - issAmount;
		
		for(SettlementData settlementData: settlementDatas) {
			if(ProcessContext.get().getSwitchTerminal(settlementData.getTerminal().getCode()).getType().equals(SwitchTerminalType.ACQUIER)) {
				settlementData.setTotalAmount(acqAmount);
				settlementData.setTotalSettlementAmount(acqAmount);
				getGeneralDao().saveOrUpdate(settlementData);
				
			} else if (ProcessContext.get().getSwitchTerminal(settlementData.getTerminal().getCode()).getType().equals(SwitchTerminalType.ISSUER)) {
				if(isIss){
					settlementData.setTotalAmount(creditAmount);
					settlementData.setTotalSettlementAmount(creditAmount);
					
				}else{
					settlementData.setTotalAmount(issAmount);
					settlementData.setTotalSettlementAmount(issAmount);

				}
				getGeneralDao().saveOrUpdate(settlementData);
			}
		}
		
		DateTime day = DateTime.now();
		if (settlementState != null) {
			day = settlementDatas.get(0).getSettlementTime();
		}
		
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		String persionFormat = dateFormatPers.format(DateTime.now().toDate());
		
		int counter = 1;
		if(isIss){
			counter = 2;
			creditAmount = issAmount;
		}
		boolean hasAmount = false;
		for(int i =0; i<counter; i++){
    	
		if (creditAmount != 0) {
			hasAmount = true;
			String commentOfDocumentItem = "";
			boolean debtor = true;
			boolean isPSP = true;// TransactionService.isPSPSwitch(institution.getCode());
			if ((creditAmount > 0 && !isPSP )|| (creditAmount < 0 && isPSP)) {
				if(isIss){
					if(creditAmount == acqAmount)
					commentOfDocumentItem = " تسویه " + institution.getName()+ "  بابت تراکنشهای " + taavon.getName() ;
					else
						commentOfDocumentItem = " تسویه " + institution.getName()+ "  بابت تراکنشهای " + shetab.getName() ;
				}
				else
					commentOfDocumentItem = /*AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabDebitDocument)*/"خالص بدهکاری " + institution.getName();
				creditAmount = Math.abs(creditAmount);
				debtor = true;
				logger.debug("Total amount > 0; debtor = true");
			} else if ((creditAmount < 0 && !isPSP )|| (creditAmount > 0 && isPSP)) {
				if(isIss){
					if(creditAmount == acqAmount)
						commentOfDocumentItem = " تسویه " + institution.getName()+ "  بابت تراکنشهای " + taavon.getName() ;
					else
						commentOfDocumentItem = " تسویه " + institution.getName()+ "  بابت تراکنشهای " + shetab.getName() ;
					
				}
					
				else
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
		if(isIss)
			creditAmount = acqAmount;
	}

		if(hasAmount){
			
			String nameEn = institution.getNameEn();
			String comment = Util.hasText(nameEn)? nameEn + "EOD-" : "-EOD-";
			
			Pair<String, String> document = AccountingService.generateFCBDocument(/*AccountingService.getFanapCoreConfigValue(CoreConfigDataManager.ShetabEODDocumentTitle)*/ "تسویه حساب " + institution.getName() +  " مورخ "
					+ getDocumentPersianDate(day), documents, null, /*(settlementState != null) ? "stlState-" + settlementState.getId()
				+ "-ShetabEOD-"comment + day.getDayDate().toString().replace("/", "-") :*/ /*"ShetabEOD-"*/comment + day.getDayDate().toString().replace("/", "-"), null, null, null);
			SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, settlementState);
			getGeneralDao().saveOrUpdate(report);
			String transactionId = /*"1234"*/AccountingService.issueFCBDocument(report, true);
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
		}else{
			for (SettlementData data: settlementDatas) {
				data.setDocumentNumber("amount is zero");
			}
			
			if (settlementState != null) {
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
				SwitchTerminal acquireSwitchTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
				for (SettlementData settlementData: settlementState.getSettlementDatas()) {
					if (acquireSwitchTerminal != null && acquireSwitchTerminal.getId().equals(settlementData.getTerminalId())) {
						settlementData.setDocumentNumber("amount is zero");
						getGeneralDao().saveOrUpdate(settlementData);
					}
				}
			}
		}
		
	}
	
	private void prepareDocuments(MyLong acqMain, MyLong acqSec, MyLong issMain, MyLong issSec, ClearingProfile clearingProfile, Institution institution, String switchBranchId, String switchAccount, List<DocumentItemEntity> documents) {

		String fanapAccount = ProcessContext.get().getMyInstitution().getAccount().getAccountNumber();
		boolean isIss = true;//TransactionService.isIssuerSwitch(institution.getCode()) ;
		String shetabAccount = null;
		if (isIss){
			Institution shetab = GeneralDao.Instance.getObject(Institution.class, 9000L);
			 shetabAccount = shetab.getCoreAccountNumber().getAccountNumber();
		}
		
		String commentOfDocumentItem = "";
		DocumentItemEntity documentItemEntity;
		String switchName = institution.getName();

		DocumentItemEntityType topic = DocumentItemEntityType.Topic;

		/***************************************/
		Long amount = issMain.value - issSec.value;
		if(isIss)
			commentOfDocumentItem = ClearingService.getSettlementDataCriteria(clearingProfile, SettlementDataType.MAIN).getDocDesc() + " بابت تراکنش های " + switchName;
		else 
			commentOfDocumentItem = ClearingService.getSettlementDataCriteria(clearingProfile, SettlementDataType.MAIN).getDocDesc() + " " + switchName + " -حالت صادرکنندگی";

		logger.debug("Account: " + switchAccount + " debitted by the value of: " + amount);

		if (AccountingService.isTopic(switchAccount))
			topic = DocumentItemEntityType.Topic;
		else
			topic = DocumentItemEntityType.Account;

		/*if ((amount > 0 && !TransactionService.isPSPSwitch(institution.getCode()) && !TransactionService.isIssuerSwitch(institution.getCode())) ||
				(amount < 0 && (TransactionService.isPSPSwitch(institution.getCode()) || TransactionService.isIssuerSwitch(institution.getCode())))) {
			amount = Math.abs(amount);
			if(isIss)
				documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, commentOfDocumentItem, shetabAccount, topic);
			else
				documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, commentOfDocumentItem, switchAccount, topic);
		} else if ((amount < 0 && !TransactionService.isPSPSwitch(institution.getCode()) && !TransactionService.isIssuerSwitch(institution.getCode())) || 
				(amount > 0 && (TransactionService.isPSPSwitch(institution.getCode())) || TransactionService.isIssuerSwitch(institution.getCode()))) {
			amount = Math.abs(amount);
			if(isIss)
				documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, shetabAccount, topic);
			else
				documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, switchAccount, topic);
		
		} else {
			logger.error("Institution: " + institution.getCode() + " credited by the value of: " + amount);
			amount = Math.abs(amount);
			if(isIss)
				documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, shetabAccount, topic);
			else
				documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, switchAccount, topic);
		}*/
		/*if(documentItemEntity != null){
			documents.add(documentItemEntity);
			documentItemEntity = null;			
		}*/
		
		/***************************************/
		amount = acqMain.value - acqSec.value;
		if (isIss)
			amount = acqSec.value - acqMain.value;
		
		if(isIss)
			commentOfDocumentItem =  " تسویه تعاون بایت تراکنشهای " + switchName;
		else
			commentOfDocumentItem = ClearingService.getSettlementDataCriteria(clearingProfile, SettlementDataType.MAIN).getDocDesc() + " " + switchName + " -حالت پذيرندگی";
		
		logger.debug("Account: " + fanapAccount + " credited by the value of: " + amount);

		if (AccountingService.isTopic(fanapAccount))
			topic = DocumentItemEntityType.Topic;
		else
			topic = DocumentItemEntityType.Account;

		/*if ((amount > 0 && !TransactionService.isPSPSwitch(institution.getCode()))|| (amount < 0 && TransactionService.isPSPSwitch(institution.getCode()))) {
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, commentOfDocumentItem, *//*switchAccount*//*fanapAccount,
					topic);

		}else if ((amount < 0 && !TransactionService.isPSPSwitch(institution.getCode())) || (amount > 0 && TransactionService.isPSPSwitch(institution.getCode()))) {
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, commentOfDocumentItem, *//*switchAccount*//*fanapAccount, topic);
		
		} else {
			logger.error("Institution: " + institution.getCode() + " debited by the value of: " + amount);
			amount = Math.abs(amount);
			documentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, commentOfDocumentItem, *//*switchAccount*//*fanapAccount, topic);
		}*/
		/*if(documentItemEntity != null){
			documents.add(documentItemEntity);
			
		}*/
		
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
	
//	private void pars13Report(String report13, MyLong acqMain, MyLong acqSec, MyLong issMain, MyLong issSec, Institution institution) {
//		
//		StringTokenizer tokenizer = new StringTokenizer(report13 , "\r\n");
//		while(tokenizer.hasMoreTokens()) {
//			StringTokenizer lineToken = new StringTokenizer(tokenizer.nextToken(), "/");
////			while(lineToken.hasMoreTokens()) {
//				int isShetab = Integer.parseInt(lineToken.nextToken());
//				String trnType = lineToken.nextToken();
//				String terminlatype = lineToken.nextToken();
//				Long amount = Long.parseLong(lineToken.nextToken());
//				String debitCredit = lineToken.nextToken();
//				Long recevieBankId = Long.parseLong(lineToken.nextToken());
//				if(TransactionService.isIssuerSwitch(institution.getCode())){
//					List<String> FanapCreditor = new ArrayList<String>();
//					FanapCreditor.add("WD");
//					FanapCreditor.add("PU");
//					FanapCreditor.add("TF");
//					List<String> FanapDebtor = new ArrayList<String>();
//					FanapDebtor.add("TT");
//					if(isShetab == 1 && trnType.equals("TT") && recevieBankId.equals(GlobalContext.getInstance().getMyInstitution().getCode()))
//						acqSec.value += amount;
//					else if (isShetab == 1 && trnType.equals("TF") && recevieBankId.equals(GlobalContext.getInstance().getMyInstitution().getCode()))
//						acqMain.value += amount;
//					else if(isShetab == 1 && debitCredit.equals("C")) //SHETAB is Creditor like WD and PU and TF
//						issMain.value += amount;
//					else if (isShetab == 1 && debitCredit.equals("D")) //SHETAB is Debtor like TT
//						issSec.value += amount;
//					else if (isShetab == 2 && FanapDebtor.contains(trnType)) //FANAP is Debtor like TT
//						acqMain.value += amount;
//					else if (isShetab == 2 && FanapCreditor.contains(trnType)) //FANAP is Creditor like WD and PU and TF
//						acqSec.value += amount;
//					
//				}
//				/****** For ignore some Transaction for PSP switch ******/
//				else if (!TransactionService.isPSPSwitch(institution.getCode()) || 
//						(TransactionService.isPSPSwitch(institution.getCode()) && (trnType.equalsIgnoreCase("PU") || trnType.equalsIgnoreCase("RF")) )) {
//				
//					if (isShetab == 2 && debitCredit.equals("D")) {
//						acqMain.value += amount;
//						
//					} else if (isShetab == 1 && debitCredit.equals("C")) {
//						issMain.value += amount;
//						
//					} else if (isShetab == 2 && debitCredit.equals("C")) {
//						acqSec.value += amount;
//						
//					} else if (isShetab == 1 && debitCredit.equals("D")) {
//						issSec.value += amount;
//						
//					}
//				}
////			}
//			
//		}
	 private void pars13Report(String report13, MyLong acqMain, MyLong acqSec, MyLong issMain, MyLong issSec, Institution institution) {

         StringTokenizer tokenizer = new StringTokenizer(report13 , "\r\n");
         while(tokenizer.hasMoreTokens()) {
                 StringTokenizer lineToken = new StringTokenizer(tokenizer.nextToken(), "/");
//               while(lineToken.hasMoreTokens()) {
                         int isShetab = Integer.parseInt(lineToken.nextToken());
                         String trnType = lineToken.nextToken();
                         String terminlatype = lineToken.nextToken();
                         Long amount = Long.parseLong(lineToken.nextToken());
                         String debitCredit = lineToken.nextToken();
                         Long recevieBankId = 502229L/*Long.parseLong(lineToken.nextToken())*/;
                        /* if(TransactionService.isIssuerSwitch(institution.getCode())){
                                 List<String> FanapCreditor = new ArrayList<String>();
                                 FanapCreditor.add("WD");
                                 FanapCreditor.add("PU");
                                 FanapCreditor.add("TF");
                                 List<String> FanapDebtor = new ArrayList<String>();
                                 FanapDebtor.add("TT");
                                 if(isShetab == 1 && trnType.equals("TT") && recevieBankId.equals(GlobalContext.getInstance().getMyInstitution().getCode()))
                                         acqMain.value += amount;
                                 else if (isShetab == 1 && trnType.equals("TF") && recevieBankId.equals(GlobalContext.getInstance().getMyInstitution().getCode()))
                                         acqSec.value += amount;
                                 else if(isShetab == 1 && debitCredit.equals("C")) //SHETAB is Creditor like WD and PU and TF
                                         issMain.value += amount;
                                 else if (isShetab == 1 && debitCredit.equals("D")) //SHETAB is Debtor like TT
                                         issSec.value += amount;
                                 else if (isShetab == 2 && FanapDebtor.contains(trnType)) //FANAP is Debtor like TT
                                         acqMain.value += amount;
                                 else if (isShetab == 2 && FanapCreditor.contains(trnType)) //FANAP is Creditor like WD and PU and TF
                                         acqSec.value += amount;
                         }*/

                         /****** For ignore some Transaction for PSP switch ******/
                         /*else if (!TransactionService.isPSPSwitch(institution.getCode()) ||
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
//               }

         }
 }

//	}
	private static String GeneratingOriginalReport13(String report13th){
		
//		report13th = "1/WD/ATM/000000000100000/C/502908199" + "\r\n" +
//		"2/WD/ATM/000000000100000/D/502908199" + "\r\n" +
//		"1/TT/ATM/000000000100000/D/502908199" + "\r\n" +
//		"1/TF/ATM/000000000150000/C/622106" + "\r\n" +
//		"1/TT/ATM/000000000200000/D/622106" ;
		String report13WithDestAppPan ="";
		String isShetab = null;
		String trntype = null;
		String terminaltype = null;
		Long amount = null;
		String debitCredit = null;
		report13WithDestAppPan = report13th;
		String newReport13 = "";
		
		
		
		StringTokenizer firstTokenizer = new StringTokenizer(report13th , "\r\n");
		int counter = 0;
		while(firstTokenizer.hasMoreTokens()){
			firstTokenizer.nextToken();
			counter ++;
		}
		
		
		StringTokenizer tokenizer = new StringTokenizer(report13th , "\r\n");
		String checkTable [][] = new String[counter][5];
		int flag = 0;
		while(tokenizer.hasMoreTokens()) {
			StringTokenizer lineToken = new StringTokenizer(tokenizer.nextToken(), "/");
			while(lineToken.hasMoreTokens()){
				
				checkTable[flag][0] = lineToken.nextToken();
				checkTable[flag][1] = lineToken.nextToken();
				checkTable[flag][2] = lineToken.nextToken();
				checkTable[flag][3] = lineToken.nextToken();
				checkTable[flag][4] = lineToken.nextToken();
				lineToken.nextToken();
				flag ++;
			}
			
			
		}
		long temp = -1;
		int count = 0;
		ArrayList<Integer> tempo = new ArrayList<Integer>();
		for(int i =0; i <counter; i++){
			isShetab = checkTable[i][0];
			trntype = checkTable[i][1];
			terminaltype = checkTable[i][2];
			amount = Long.valueOf(checkTable[i][3]);
			debitCredit = checkTable[i][4];
//			tempo.clear();
//			count= 0;
			for(int j = 0; j<counter ; j++){
				if( j!=i && isShetab.equals(checkTable[j][0]) &&
						trntype.equals(checkTable[j][1]) &&
						terminaltype.equals(checkTable[j][2]) &&
						debitCredit.equals(checkTable[j][4]) &&
//						j!= temp
						!tempo.contains(j)){
//					tempo.add(i);
					amount += Long.valueOf(checkTable[j][3]);
//					newReport13 += isShetab + "/";
//					newReport13 += trntype + "/";
//					newReport13 += terminaltype + "/";
//					newReport13 += StringFormat.formatNew(15, StringFormat.JUST_RIGHT, amount, '0') + "/";
//					newReport13 += debitCredit + "\r\n";
					tempo.add(count, j);
					count++;
					temp = Long.valueOf(j);
				}
				else if(j == counter-1 && !tempo.contains(i)){
					newReport13 += isShetab + "/";
					newReport13 += trntype + "/";
					newReport13 += terminaltype + "/";
					newReport13 += StringFormat.formatNew(15, StringFormat.JUST_RIGHT, amount, '0') + "/";
					newReport13 += debitCredit + "\r\n";
					
				}
					
			}
		}
		
		
//		newReport13 += isShetab + "/";
//		newReport13 += trntype + "/";
//		newReport13 += terminaltype + "/";
//		newReport13 += amount + "/";
//		newReport13 += debitCredit + "\r\n";	
//		report13th = newReport13;
		
		return newReport13;
	}
}
