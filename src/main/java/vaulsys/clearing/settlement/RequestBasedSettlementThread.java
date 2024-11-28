package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class RequestBasedSettlementThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());

	POSTerminal terminal;
	
	public RequestBasedSettlementThread(POSTerminal terminal) {
		super();
		this.terminal = terminal;
	}


	@Override
	public void run() {
		GeneralDao.Instance.beginTransaction();
		ProcessContext.get().init();
		ClearingProfile clearingProfile = ClearingService.findClearingProfile(terminal.getOwnOrParentClearingProfileId());

		logger.info("Try to Settle POS Terminal(RequestBased) " + terminal.getCode());
		List<Terminal> terminals = new ArrayList<Terminal>();
		terminals.add(terminal);
		DateTime untilTime = DateTime.now();
		DateTime settleUntilTime = clearingProfile.getSettleUntilTime(DateTime.now().nextDay());
		GeneralDao.Instance.endTransaction();
		try {
			RequestBasedSettlementServiceImpl.Instance.settle(terminals, clearingProfile, settleUntilTime, false, true, false);
		} catch (Exception e) {
			logger.error("Error in request based settlement of POS ...", e);
		}

	}
}
