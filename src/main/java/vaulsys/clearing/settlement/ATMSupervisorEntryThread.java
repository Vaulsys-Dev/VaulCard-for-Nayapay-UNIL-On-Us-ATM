package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ATMSupervisorEntryThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());

	ATMTerminal terminal;
	
	public ATMSupervisorEntryThread(ATMTerminal terminal) {
		super();
		this.terminal = terminal;
	}


	@Override
	public void run() {
		GeneralDao.Instance.beginTransaction();
		ProcessContext.get().init();
		terminal = GeneralDao.Instance.load(ATMTerminal.class,terminal.getCode());
		logger.info("Try to Settle ATM Terminal "+ terminal.getCode());
    	List<Terminal> terminals = new ArrayList<Terminal>();
    	terminals.add(terminal);
//    	ClearingProfile clearingProfile = ProcessContext.get().getClearingProfile(terminal.getOwnOrParentClearingProfileId());
    	ClearingProfile clearingProfile = GeneralDao.Instance.load(ClearingProfile.class, terminal.getOwnOrParentClearingProfileId());
    	DateTime untilTime = DateTime.now();
    	SettlementService settlementService = ClearingService.getSettlementService(clearingProfile);
		GeneralDao.Instance.endTransaction();
    	try{
			if (settlementService instanceof ATMSettlementServiceImpl)
    			ATMSettlementServiceImpl.Instance.account(terminals, clearingProfile, untilTime, untilTime, true, true, false, false, false);
    		else if (settlementService instanceof ATMDailySettlementServiceImpl)
    			ATMDailySettlementServiceImpl.Instance.settle(terminals, clearingProfile, clearingProfile.calcNextSettleTime(untilTime), true, true, false, false);
    		else if (settlementService instanceof ATMDailyRecordSettlementServiceImpl)
    			ATMDailyRecordSettlementServiceImpl.Instance.settle(terminals, clearingProfile, clearingProfile.calcNextSettleTime(untilTime), true, true, false, true);
		}catch(Exception e){
			logger.error("Error in settlement of ATM on supervisor entry...",e);
		}
	}
}
