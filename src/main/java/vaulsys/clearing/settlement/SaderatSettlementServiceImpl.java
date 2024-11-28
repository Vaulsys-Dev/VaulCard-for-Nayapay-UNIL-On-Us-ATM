package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementDataReport;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.SettledState;
import vaulsys.util.Pair;
import vaulsys.util.Util;
import vaulsys.util.ZipUtil;
import vaulsys.wfe.ProcessContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ghasemkiani.util.icu.PersianDateFormat;

public class SaderatSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(SaderatSettlementServiceImpl.class);
	
	private SaderatSettlementServiceImpl(){}
	
	public static final SaderatSettlementServiceImpl Instance = new SaderatSettlementServiceImpl();

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
		MonthDayDate workingDay = new MonthDayDate(settleDate.toDate());
	
		DateTime cuttofFrom = new DateTime();
		cuttofFrom.setDayDate(settleDate.getDayDate());
		cuttofFrom.setDayTime(new DayTime(0, 0, 0));
		
		DateTime cuttofTo = new DateTime();
		cuttofTo.setDayDate(settleDate.getDayDate());
		cuttofTo.setDayTime(new DayTime(23, 59, 59));
		
//		MonthDayDate workingDay = FinancialEntityService.getInstitutionByCode(603769L).getLastWorkingDay().getDate();
//		DateTime cuttofFrom = FinancialEntityService.getInstitutionByCode(603769L).getLastWorkingDay().getRecievedDate();
//		DateTime cuttofTo = FinancialEntityService.getInstitutionByCode(603769L).getCurrentWorkingDay().getRecievedDate();
		generateDesiredSettlementReports(clearingProfile, settleDate, workingDay, cuttofFrom, cuttofTo);
	}

	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, DateTime settleDate, MonthDayDate workingDay, DateTime cutoffDateFrom, DateTime cutoffDateTo) throws Exception {		
		logger.info("generating desired settlement report for institution 603769 on "+ workingDay);
		PersianDateFormat dateFormat = new PersianDateFormat("yyMMdd");
		String persianDate = dateFormat.format(workingDay.toDate());

		Institution myInstitution = ProcessContext.get().getMyInstitution();
		final String REPORT_9_OLD_PATH = "MR_" + /*myInstitution.getBin()*/ 60376992 + ".acq";
		final String LOCAL_REPORT_9_OLD_PATH_ = "LMR_" + /*myInstitution.getBin()*/ 60376992  +".acq";
		// ------------------Moosavi : Task111686 : Add new Form 9 to be compatible to Shetab7----------------
		final String REPORT_9_NEW_PATH = "MR_" + /*myInstitution.getBin()*/ 60376992 + "-new"+ ".acq";
		final String LOCAL_REPORT_9_NEW_PATH = "LMR_" + /*myInstitution.getBin()*/ 60376992 + "-new" + ".acq";
		//-----------------------------------------------------------------------------------------------------

		String[] strFileName = new String[]{/*REPORT_13_PATH, */ LOCAL_REPORT_9_OLD_PATH_,LOCAL_REPORT_9_NEW_PATH};
		byte[][] bReport = new byte[4][];
		
		for(int i=0; i<bReport.length; i++){
			bReport[i]=new byte[0];
		}
		
		List<SettlementData> settleDataForReport = null;

		SwitchTerminal shetabIssSwitchTerminal = ProcessContext.get().getIssuerSwitchTerminal("603769");
		List<SettlementData> settlementData = null;
		settlementData = AccountingService.findSettlementData(null, shetabIssSwitchTerminal, clearingProfile, settleDate.getDayDate());
		
		if (!FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
			
			if (settlementData != null && !settlementData.isEmpty()) {
				logger.info("generateDesiredSettlementReports for terminal "+ shetabIssSwitchTerminal.getCode()+", settlementData.size = "+ settlementData.size());
				//------------Moosavi : Task111686 : Add new Form 9 to be compatible to Shetab7------------
			    // ---------- add change to this method : insterad of generating one report now it create two reports9 (old&new) and retur the pair of these two----------------				
				logger.debug("Generating report 9 both old and new versions(acquirerShetabReport)");
				Pair<String, String> acquirerShetabReportOldNew = new Pair<String, String>(null, null);
				try {
					acquirerShetabReportOldNew = ShetabReconciliationService.generateAcquirerShetabReport(""+myInstitution.getBin(), workingDay, cutoffDateFrom, cutoffDateTo, null, 603769L);
				} catch (Exception e) {
					logger.error("Encounter with an Exception in generating REPORT 9: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
				}
				if (!Util.hasText(acquirerShetabReportOldNew.first)){
					logger.info("setting thirdPartyRecordof stlData of terminal["+shetabIssSwitchTerminal.getCode()+"]: acquirerShetabReportOld is empty");
				}
				if (!Util.hasText(acquirerShetabReportOldNew.second)){
					logger.info("setting thirdPartyRecordof stlData of terminal["+shetabIssSwitchTerminal.getCode()+"]: acquirerShetabReportNew is empty");
				}
				
				logger.debug("Generating ZIP file of reports");
				bReport[0] = acquirerShetabReportOldNew.first.getBytes();
				bReport[1] = acquirerShetabReportOldNew.second.getBytes();
				
				logger.debug("Generating local report 9 both old and new versions(acquirerShetabReport)");
				acquirerShetabReportOldNew.first = null;
				acquirerShetabReportOldNew.second = null;
				try {
					acquirerShetabReportOldNew = ShetabReconciliationService.generateAcquirerShetabReport(""+myInstitution.getBin(), workingDay, cutoffDateFrom, cutoffDateTo, 603769L, null);
				} catch (Exception e) {
					logger.error("Encounter with an Exception in generating LOCAL REPORT 9: "+ e.getClass().getSimpleName()+"-"+ e.getMessage());
				}
				if (!Util.hasText(acquirerShetabReportOldNew.first)){
					logger.info("setting thirdPartyRecordof stlData of terminal["+shetabIssSwitchTerminal.getCode()+"]: localAcquirerShetabReportOld is empty");
				}
				if (!Util.hasText(acquirerShetabReportOldNew.second)){
					logger.info("setting thirdPartyRecordof stlData of terminal["+shetabIssSwitchTerminal.getCode()+"]: localAcquirerShetabReportNew is empty");
				}
				
				logger.debug("Generating ZIP file of reports");
				bReport[2] = acquirerShetabReportOldNew.first.getBytes();
				bReport[3] = acquirerShetabReportOldNew.second.getBytes();
				
				//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	
				settleDataForReport = settlementData;
			}
			
		}

		byte[] b = ZipUtil.getZipByteArray(strFileName, bReport);
		
		for (SettlementData data : settleDataForReport) {
			if(data.getType().equals(SettlementDataType.MAIN)){
				SettlementDataReport sdr = data.addThirdPartyReport(b);
				GeneralDao.Instance.saveOrUpdate(sdr);
			}
		}
		
		try {
			PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");
			File shetabFile = new File("/Member-rep");
			shetabFile.mkdirs();

			OutputStream fileShetab = null;
			if (!shetabFile.exists()) {
				shetabFile.createNewFile();
			}
			
			fileShetab = new FileOutputStream(shetabFile + "/" + "BSI"+dateFormatPers.format(workingDay.toDate()) + ".zip");
			fileShetab.write(b);
			fileShetab.close();
		} catch(Exception e) {
			logger.error("can't transfer file, " + e, e);
		}
		
//		try {
//			String ip = ConfigUtil.getProperty(ConfigUtil.SMB_IP);
//			if (Util.hasText(ip)) {
//				SMBFileTransferUtil.upload(b, ip, "mnt/shetab");
//			}
//		} catch (Exception e) {
//			logger.error("can't transfer file, " + e, e);
//		}


	}
	
	@Override
	public void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc, DateTime settleDate) throws Exception {
		List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, null);
		generateDocumentSettlementState(settlementStates);
	}
	
	@Override
	public void generateDocumentSettlementState(List<SettlementState> settlementStates) throws Exception {
		
		for (SettlementState settlementState : settlementStates) {
			Map<Terminal, List<SettlementData>> shetabTerminalSettlementData = new HashMap<Terminal, List<SettlementData>>();
			
			Map<Terminal, List<SettlementData>> map = AccountingService.getSettlementDatas(settlementState.getSettlementDatas());
			
			Map<Terminal, List<SettlementData>> terminalSettlementData = null;
			if (map != null && map.size() > 0) {
				for (Terminal terminal : map.keySet()) {
					if (terminal == null)
						continue;
					if (((Institution)terminal.getOwner()).getCode().equals(603769L/*SADERAT*/)) {
						terminalSettlementData = shetabTerminalSettlementData;
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
				if (!shetabTerminalSettlementData.isEmpty())
					generateShetabDesiredSettlementReports(shetabTerminalSettlementData, settlementState);
			} catch (Exception e) {
				logger.error("Error was occured in Saderat Settlement. ("+ e.getClass().getSimpleName()+": "+ e.getMessage()+")");
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
		
//		List<String> topicCodes = new ArrayList<String>();
//		topicCodes.add(FinancialEntityService.getMasterInstitution().getAccount().getAccountNumber());
//		topicCodes.add(ProcessContext.get().getMyInstitution().getAccount().getAccountNumber());
//		if (ProcessContext.get().getPeerInstitutions()!= null)
//			for (Institution i: ProcessContext.get().getPeerInstitutions()){
//				topicCodes.add(i.getAccount().getAccountNumber());
//			}
//
//		AccountingService.settleSwitchTopics(topicCodes);
	}
	
	private void generateShetabDesiredSettlementReports(Map<Terminal, List<SettlementData>> shetabTerminalSettlementData,
			SettlementState settlementState) {
		
	}

	protected List<SettlementData> getNotDocumentSettlementData(List<SettlementData> settlementDatas) {
		List<SettlementData> result = new ArrayList<SettlementData>();
		for (SettlementData settlementData: settlementDatas) {
			if (settlementData != null && !Util.hasText(settlementData.getDocumentNumber()))
				result.add(settlementData);
		}
		return result;
	}

	protected String getDocumentPersianDate(DateTime time) {
	    PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
	    return dateFormatPers.format(time.toDate());
	}
	
}