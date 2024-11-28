package vaulsys.initializer;

import vaulsys.clearing.settlement.SettlementService;
import vaulsys.scheduler.job.CycleSettlementJob;

public class CyclePerTransactionCycleSettlementJob  extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void submitJob() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
