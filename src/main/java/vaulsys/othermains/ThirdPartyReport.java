package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.clearing.settlement.BillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.ChargeSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.persistence.GeneralDao;
import vaulsys.thirdparty.consts.ThirdPartyType;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class ThirdPartyReport {
	public static final Logger logger = Logger.getLogger(ThirdPartyReport.class);
	
	public static void main(String[] args) {
		ClearingProfile clearingProfile =null;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		GlobalContext.getInstance().getMyInstitution();

		try{
			if(args.length < 1){
				logger.error("bad arguements list: args.length < 1");
				System.exit(0);
			}
			
			
			if (args.length == 1) {

				 List<SettlementData> stlDatas = GeneralDao.Instance.find("from SettlementData where id in ("+args[0]+")");
//				String query = "from "+ SettlementData.class.getName() +" cp where cp.id in (:ids)";
//				Map<String, Object> params = new HashMap<String, Object>();
//				params.put("ids", args[0]);
				
//				List<SettlementData> stlDatas = GeneralDao.Instance.find(query, params);
				if (stlDatas == null || stlDatas.size() == 0) {
					GeneralDao.Instance.endTransaction();
					logger.debug("Returning ThirdPartyReport, settlementData is null");
					return;
				}
				clearingProfile = stlDatas.get(0).getClearingProfile();
				Class settlementClass = clearingProfile.getSettlementClass();
				ThirdPartyType type = ThirdPartyType.BILLPAYMENT;
				if (ChargeSettlementServiceImpl.class.equals(settlementClass)) {
					type = ThirdPartyType.CHARGE;
				}
//				SettlementService settlementService = ClearingService.getSettlementService(clearingProfile);
				
				try {
					ReportGenerator.generateThirdPartyReportBySettlementData(type, stlDatas, null, "thirdpartyreport");
				} catch (Exception e) {
					logger.error(e, e);
					GeneralDao.Instance.rollback();
					return;
				}
				
				
				
				
				
			} else if (args.length == 2) {
				String query = "from "+ ClearingProfile.class.getName() +" cp where cp.id = :id";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("id", Long.parseLong(args[0]));
				clearingProfile = (ClearingProfile) GeneralDao.Instance.findObject(query, params);
				if (clearingProfile == null){
					GeneralDao.Instance.endTransaction();
					logger.debug("Returning ThirdPartyReport, clearingProfile is null");
					return;
				}
				
				Long date = Long.parseLong(args[1]);

				SettlementService settlementService = ClearingService.getSettlementService(clearingProfile);
				
				
				DateTime settleDate = new DateTime(new DayDate((int) (date/10000L), (int) ((date%10000L)/100), (int) ((date%100)/1)), new DayTime(23, 59, 59));
				
				try {
					settlementService.generateDesiredSettlementReports(clearingProfile, settleDate);
				} catch (Exception e) {
					logger.error(e, e);
					GeneralDao.Instance.rollback();
					return;
				}
			}

			
			
			GeneralDao.Instance.endTransaction();
		} catch(Exception e) {
			logger.error(e);
			GeneralDao.Instance.rollback();
			return;
		}
}
}
