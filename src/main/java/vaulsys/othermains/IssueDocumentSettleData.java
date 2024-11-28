package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.clearing.settlement.ATMDailyRecordSettlementServiceImpl;
import vaulsys.clearing.settlement.ATMDailySettlementServiceImpl;
import vaulsys.clearing.settlement.ATMSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class IssueDocumentSettleData {

    private static final Logger logger = Logger.getLogger(IssueDocumentSettleData.class);

    public static void main(String[] args) {
        GeneralDao.Instance.beginTransaction();
        GlobalContext.getInstance().startup();
        ProcessContext.get().init();

        List<SettlementData> settlementDatas = new ArrayList<SettlementData>();
        List<SettlementData> settlementDatasToBeRemove = new ArrayList<SettlementData>();
        SettlementService settlementService = null;
        DateTime settlementTime = DateTime.now();

        try {

            settlementDatas = GeneralDao.Instance.find("from SettlementData where id in ("+args[0]+") order by settlementTimeLong");
            ClearingProfile clearingProfile = settlementDatas.get(0).getClearingProfile();
            settlementTime = settlementDatas.get(0).getSettlementTime();
            logger.info("Starting...settlementDatas len = " + settlementDatas.size() + ",clearingProfile=" + clearingProfile.getId() + ", and settlementTime=" + settlementTime.getDayDate());
            for(SettlementData settlementData : settlementDatas){
                if(!settlementData.getSettlementTime().getDayDate().equals(settlementTime.getDayDate())
                        || settlementData.getSettlementTime().after(DateTime.now())
                        || !settlementData.getClearingProfile().equals(clearingProfile)
                        ){
                    logger.debug("This is stlDataId is not ok:"+settlementData.getId()+", dayDate="+settlementData.getSettlementTime()+", clearingProfile="+settlementData.getClearingProfile().getId());
                    settlementDatasToBeRemove.add(settlementData);
                }
            }
            for(SettlementData settlementData : settlementDatasToBeRemove)
                settlementDatas.remove(settlementData);

            logger.info("after remove settlementDatas len = " + settlementDatas.size());
            settlementService = ClearingService.getSettlementService(clearingProfile);

            GlobalContext.getInstance().getMyInstitution();
        } catch (Exception e) {
            e.printStackTrace();
            GeneralDao.Instance.rollback();
            return;
        }

        try {
            if (settlementService.equals(ATMSettlementServiceImpl.Instance) ||
                    settlementService.equals(ATMDailySettlementServiceImpl.Instance) ||
                    settlementService.equals(ATMDailyRecordSettlementServiceImpl.Instance)) { //Task121 - IssueDocumentSettleData for ATMDailyRecordSettlementServiceImpl  //vaghti ke yek settleData settleReport Nadashte bashe va sanad nakhore va clrProf = ATMDailyRecordSettlementServiceImpl bashe docXML ghalati baraye an sakhte mishode ke be nazar mirese ba in sharte jadid dorost shavad
                ATMSettlementServiceImpl.Instance.postPrepareForSettlement(settlementDatas);

            } else {
                ReportGenerator.issueFanapSettlementDataReport(settlementDatas, settlementService.getSettlementTypeDesc(), settlementTime);

            }
            GeneralDao.Instance.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            GeneralDao.Instance.rollback();
            return;
        }


    }
}
