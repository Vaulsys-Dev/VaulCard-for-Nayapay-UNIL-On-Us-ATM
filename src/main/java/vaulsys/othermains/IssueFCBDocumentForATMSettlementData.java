package vaulsys.othermains;

import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.settlement.IssueATMFCBThread;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import org.apache.log4j.Logger;

public class IssueFCBDocumentForATMSettlementData {
	private static final Logger logger = Logger.getLogger(IssueFCBDocumentForATMSettlementData.class);

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		
		List<SettlementData> settlementData = GeneralDao.Instance.find("from SettlementData where id in (" +
				"384384805"+		") " +
						" and settlementReport is not null" +
						" and clearingProfile = 19137601 and documentNumber is  null order by id");
		GlobalContext.getInstance().getMyInstitution();

		GeneralDao.Instance.endTransaction();
		
		SettlementData[] stlData = new SettlementData[settlementData.size()];
		for(int i=0; i<stlData.length; i++){
			stlData[i] = settlementData.get(i);
		}
		
		IssueATMFCBThread issueFCBThread = new IssueATMFCBThread(stlData);
		Thread issueThread = new Thread(issueFCBThread);
		issueThread.setDaemon(false);
		logger.debug("Thread: " + issueThread.getName() + " is starting...");
		issueThread.start();
	}
}
