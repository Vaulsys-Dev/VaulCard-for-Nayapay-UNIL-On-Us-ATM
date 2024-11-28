package vaulsys.othermains;

import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

public class IssueDocument {

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		
		List<SettlementState> settlementStates = new ArrayList<SettlementState>();
		SettlementService settlementService = null;
		
		try {

			settlementStates = GeneralDao.Instance.find("from SettlementState where id in ("+args[0]+")");
			ClearingProfile clearingProfile = settlementStates.get(0).getClearingProfile();
			settlementService = ClearingService.getSettlementService(clearingProfile);
			
			GlobalContext.getInstance().getMyInstitution();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			return;
		}

		try {
			ReportGenerator.generateDocumentSettlementState(settlementStates, settlementService.getSettlementTypeDesc(), false, false);
			GeneralDao.Instance.endTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			return;
		}
		
	
	}
}
