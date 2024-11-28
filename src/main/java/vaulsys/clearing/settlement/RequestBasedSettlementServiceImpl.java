package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.SynchronizationService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.ClearingProcessType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;

public class RequestBasedSettlementServiceImpl extends SettlementService {
	private static final Logger logger = Logger.getLogger(RequestBasedSettlementServiceImpl.class);
	private static final int MAX_COUNTER = 100;
	
	private RequestBasedSettlementServiceImpl(){}
	
	public static final RequestBasedSettlementServiceImpl Instance = new RequestBasedSettlementServiceImpl();
	
	@Override
	public void account(List<Terminal> terminals, ClearingProfile clearingProfile, 
			DateTime accountUntilTime, DateTime settleUntilTime, 
			Boolean update, Boolean waitForSyncObject, 
			Boolean onlyFanapAccount, Boolean settleTime, 
			Boolean considerClearingProcessType) throws Exception {
		
		logger.info("Starting Terminal Accounting...");
		GeneralDao.Instance.beginTransaction();
		GeneralDao.Instance.refresh(clearingProfile);

		try {
			boolean justToday = true;
			if (settleUntilTime.equals(accountUntilTime))
				justToday = false;

			List<Terminal> tmpTerminals;
			List<Terminal> freeTerminals;
			List terminalCodes = new ArrayList();
			boolean onlineProcess = false;
			List<Terminal> firstRoundTerminal = null;

			DateTime realTimeForAccounting = accountUntilTime;
			if (settleUntilTime.before(accountUntilTime))
				realTimeForAccounting = settleUntilTime;
			
			if (terminals == null || terminals.isEmpty()){
				if(ClearingProcessType.ONLINE.equals(clearingProfile.getProcessType()) && considerClearingProcessType.equals(true)){
					onlineProcess = true;
					terminalCodes.addAll(findDesiredTerminalCodesBasedOnSettlementRecord(accountUntilTime, justToday, clearingProfile));
					
				}else{
					terminalCodes.addAll(findDesiredTerminalCodes(realTimeForAccounting, justToday, clearingProfile));
				}
				terminalCodes.addAll(findDesiredTerminalCodes(accountUntilTime, justToday, clearingProfile));
			}
			else {
				for (Terminal terminal : terminals) {
					terminalCodes.add(terminal.getCode());
				}
				terminals = null;
			}

			GeneralDao.Instance.endTransaction();


			int counter = 0;

			
			List<Long> termForQuery = new ArrayList<Long>();
			for (int i = 0; i < terminalCodes.size(); i++) {
//				logger.info("Term:" + terminalCodes.get(i));
				if (terminalCodes.get(i) instanceof String)
					termForQuery.add(Long.parseLong((String) terminalCodes.get(i)));
				else if (terminalCodes.get(i) instanceof Long)
					termForQuery.add((Long) terminalCodes.get(i));
				else if(terminalCodes.get(i) instanceof Terminal)
					termForQuery.add(((Terminal)terminalCodes.get(i)).getCode());
				counter++;
				

				if (counter == MAX_COUNTER || i == terminalCodes.size() - 1) {
					logger.debug("About to account for terminals: "+ToStringBuilder.reflectionToString(termForQuery.toArray(), ToStringStyle.MULTI_LINE_STYLE));

					if (!termForQuery.isEmpty()) {
						GeneralDao.Instance.beginTransaction();
						GeneralDao.Instance.refresh(clearingProfile);
						
						
						logger.info("terminalCodes: " + terminalCodes.size() + " termForQuery: "+ termForQuery.size());
						
						tmpTerminals = findAllTerminals(terminals, termForQuery, clearingProfile);
						
						List<Terminal> notFree = new ArrayList<Terminal>();
						freeTerminals = new ArrayList<Terminal>();
						
						if (tmpTerminals != null && tmpTerminals.size() > 0) {
							logger.info(tmpTerminals.size() + " terminal with clrProfile: " + clearingProfile.getId());
							for (Terminal pos : tmpTerminals) {
								try {
//									if (waitForSyncObject.equals(true)) {
//										SynchronizationService.getSynchornizationObject((POSTerminal) pos,POSTerminal.class, LockMode.UPGRADE);
//									} else {
//										SynchronizationService.getSynchornizationObject((POSTerminal) pos,POSTerminal.class, LockMode.UPGRADE_NOWAIT);
//									}
//									SynchronizationService.lock(pos, POSTerminal.class);
									GeneralDao.Instance.refresh(pos);

									List<Terminal> atmList = new ArrayList<Terminal>();
//									List<Long> trxSettlemRecordList = new ArrayList<Long>();
									atmList.add(pos);
									prepareForSettlement(clearingProfile, atmList, accountUntilTime, settleUntilTime, settleTime, considerClearingProcessType/*, trxSettlemRecordList*/, waitForSyncObject);
									

//									SynchronizationService.release(pos, POSTerminal.class);

									GeneralDao.Instance.endTransaction();
									GeneralDao.Instance.beginTransaction();
									GeneralDao.Instance.refresh(clearingProfile);
									freeTerminals.add(pos);
								} catch (Exception e) {
									logger.error("POS Terminal " + pos.getId() + " is busy now! "
											+ e.getClass().getSimpleName() + ": " + e.getMessage());
									notFree.add(pos);
									GeneralDao.Instance.rollback();
									GeneralDao.Instance.beginTransaction();
									GeneralDao.Instance.refresh(clearingProfile);
								}
							}
							if (update)
								updateToNowSettlementData(clearingProfile, tmpTerminals, accountUntilTime, settleUntilTime);
						}
						
						GeneralDao.Instance.endTransaction();
					}
					counter = 0;
					termForQuery = new ArrayList<Long>();
				}
			}
			
		} catch (Exception e) {
			logger.error("Encounter with an exception in terminal accounting..." + e.getClass().getSimpleName() + ": "
					+ e.getMessage());
			try {
				GeneralDao.Instance.rollback();
			} catch (Exception e1) {
				logger.error(e1);
//				throw e1;
			}
			throw e;
		} finally{
		}

		logger.info("Ending Terminal Accounting...");
	}

	public void settle(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime settleUntilTime, Boolean update, Boolean settleTime, Boolean generateSettleState) {
		//A trick to settle in case that we encounter an exception in accounting step
		//We will try for 3 times, if error exist then we do not proceed settlement process
		int numTries = 0;
		int maxTries = 3;
		boolean isFinishedAccounting = false;
		
		while(numTries < maxTries && !isFinishedAccounting){
			try{
				boolean waiting = true;
				if(RequestBasedSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass()))
					waiting = false;
				account(terminals, clearingProfile, settleUntilTime, settleUntilTime, update, waiting, settleTime, settleTime, false);
				isFinishedAccounting = true;
			}catch(LockAcquisitionException e){
				logger.error("Exception in accounting. LockAcquisitionException: "+numTries+" ",e);
				try {
					Thread.sleep(60000L);
				} catch (InterruptedException e1) {
					continue;
				}
			}catch(Exception e){
				logger.error("Exception in accounting. numTries: "+numTries+" ",e);
				numTries++;
			}
		}

		if(!isFinishedAccounting){
			logger.error("We faced to maxTries Exception in accounting, so we don't proceed in settlement...");
			return;
		}

		DateTime settleDate = settleUntilTime;
		
		try {
			logger.info("Generating Settlement Data Report...");
			try {
				generateSettlementDataReport(terminals, clearingProfile, settleDate);
			} catch (Exception e) {
				logger.error("Exception in Generating Settlement Data Report " + e);
			}

			GeneralDao.Instance.beginTransaction();
			GeneralDao.Instance.refresh(clearingProfile);
			
			logger.info("Generating Final Settlement State Report...");
			try {
				List<SettlementData> settlementDataList = AccountingService.findAllNotSettledSettlementDataUntilTime(terminals, clearingProfile, settleDate, null);
				if (settlementDataList != null && settlementDataList.size() > 0) {
					ReportGenerator.issueFanapSettlementDataReport(settlementDataList, getSettlementTypeDesc(), settleDate);
				} else {
					logger.debug("all settlementData is settled!");
				}
				
			} catch (Exception e) {
				logger.error("Exception in Generating Final Settlement State Report  " + e);
			}
		
			if (generateSettleState) {
			
				logger.info("Generating Settlement State Report...");
				try {
					generateSettlementStateAndReport(terminals, clearingProfile, settleDate, getSettlementTypeDesc());
				} catch (Exception e) {
					logger.error("Exception in Generating Settlement State Report, must be rollback beacuase incorrect SettlementState created! "+ e);
					GeneralDao.Instance.rollback();
					return;
				}
				
				GeneralDao.Instance.endTransaction();
				GeneralDao.Instance.beginTransaction();
				GeneralDao.Instance.refresh(clearingProfile);
	
				logger.info("Generating Desired Terminal Settlement Report...");
				try {
					generateDesiredSettlementReports(clearingProfile, settleDate);
				} catch (Exception e) {
					logger.error("Exception in Generating Desired Terminal Settlement Report  " + e);
				}
				
				logger.info("Generating Final Settlement State Report...");
				try {
					generateDocumentSettlementState(clearingProfile, getSettlementTypeDesc(), settleDate);
					
				} catch (Exception e) {
					logger.error("Exception in Generating Final Settlement State Report  " + e);
				}
			}
			GeneralDao.Instance.endTransaction();
			GeneralDao.Instance.beginTransaction();
			GeneralDao.Instance.refresh(clearingProfile);
			
		} catch (Exception e) {
			logger.error(e);
			GeneralDao.Instance.rollback();
			return;
		}
		GeneralDao.Instance.endTransaction();
	}
	
	@Override
	protected void generateDocumentSettlementState(ClearingProfile clearingProfile, String docDesc,DateTime settleDate) throws Exception {
		List<SettlementState> settlementStates = AccountingService.findSettlementState(clearingProfile, /*settleDate,*/ null);
    	for (SettlementState settlementState: settlementStates) {
	    	if (settlementState != null && AccountingService.isAllSettlementDataSettled(settlementState)) {
	    		settlementState.setState(SettlementStateType.AUTOSETTLED);
	    		DateTime now = DateTime.now();
				settlementState.setSettlementFileCreationDate(now );
	    		settlementState.setSettlementDate(now);
//	    		settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
	    		settlementState.setSettlingUser(ProcessContext.get().getSwitchUser());
	    		GeneralDao.Instance.saveOrUpdate(settlementState);
	    	}
    	}
    	ReportGenerator.generateDocumentSettlementState(clearingProfile, docDesc, /*settleDate,*/ false);
	}

	
	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, List<Long> termCodes, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(terminals, termCodes, POSTerminal.class, clearingProfile);
		
		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);

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
		
		List<String> posTerminals = TerminalService.findAllTerminalsWithTrxUntilTime(POSTerminal.class, accountUntilTime, justToday, guaranteePeriod);

		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);
	
		return terminals;
	}

	@Override
	public List<Terminal> findAllTerminals(List<Terminal> terminals, ClearingProfile clearingProfile) {
		if (terminals == null)
			terminals = new ArrayList<Terminal>();
		List<POSTerminal> posTerminals = TerminalService.findAllTerminals(POSTerminal.class, clearingProfile);

		if (posTerminals != null && posTerminals.size() > 0)
			terminals.addAll(posTerminals);

		return terminals;
	}
	
	@Override
	public void generateDesiredSettlementReports(ClearingProfile clearingProfile, /*List<Terminal> terminals, */DateTime settleDate) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean isDesiredOwnerForPreprocessing(FinancialEntity entity) {
		return (FinancialEntityRole.SHOP.equals(entity.getRole()) ||
				FinancialEntityRole.MERCHANT.equals(entity.getRole()));
	}
	
	@Override
	public List<String> getSrcDest() {
		List<String> result = new ArrayList<String>();
		result.add("source");
		return result;
	}
	
	@Override
	public String getSettlementTypeDesc() {
		return "پذيرندگان";
	}
}
