package vaulsys.clearing.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import org.apache.log4j.Logger;

public class OnlinePerTransactionSettlementThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());

	List<Terminal> terminals;
	
	public OnlinePerTransactionSettlementThread(List<Terminal> terminals) {
		super();
		this.terminals = terminals;
	}

	@Override
	public void run() {
		GeneralDao.Instance.beginTransaction();
		ProcessContext.get().init();
		ClearingProfile clearingProfile = ClearingService.findClearingProfile(OnlinePerTransactionSettlementServiceImpl.class);

		logger.info("Try to Settle POS Terminal(OnlinePerTransaction) " );
		DateTime untilTime = DateTime.now();
		GeneralDao.Instance.endTransaction();
		try {
			OnlinePerTransactionSettlementServiceImpl.Instance.settle(terminals, clearingProfile, untilTime, false, false, false, true);
		} catch (Exception e) {
			logger.error("Error in request based settlement of POS ...", e);
		}
	}
}
