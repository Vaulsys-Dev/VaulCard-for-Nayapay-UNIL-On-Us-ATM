package vaulsys.othermains.settlement;

import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.persistence.GeneralDao;
import vaulsys.thirdparty.consts.ThirdPartyType;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ThirdPartySettlement {
	public static void main (String args[])throws IOException{
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		SettlementData settlementDatas = GeneralDao.Instance.getObject(SettlementData.class, 51298604L);
		String ip = ConfigUtil.getProperty(ConfigUtil.SMB_IP);
		List<SettlementData> list = new ArrayList<SettlementData>();
		list.add(settlementDatas);
		ReportGenerator.generateThirdPartyReportBySettlementData(ThirdPartyType.CHARGE, list, ip, "charge");
		GeneralDao.Instance.endTransaction();
	}

}
