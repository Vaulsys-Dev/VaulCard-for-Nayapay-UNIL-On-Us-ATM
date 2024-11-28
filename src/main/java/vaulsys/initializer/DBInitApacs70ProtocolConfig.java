package vaulsys.initializer;

import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ProtocolConfig;
import vaulsys.protocols.ProtocolType;

public class DBInitApacs70ProtocolConfig {
	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "589210", "0"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "589463", "1"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "599999", "2"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "603769", "3"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "603770", "4"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "603799", "5"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "610433", "6"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "621986", "7"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "622106", "8"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "627353", "9"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "627412", "10"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "627488", "11"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "627648", "12"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "627760", "13"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "627961", "14"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "628023", "15"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "639347", "16"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "639607", "17"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "502229", "16"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "639346", "18"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "628157", "19"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "502807", "20"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "636214", "21"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "WA", "25"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "EL", "26"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "GA", "27"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "MC", "28"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "MTNIRANCELL", "29"));
		GeneralDao.Instance.saveOrUpdate(new ProtocolConfig(ProtocolType.APACS70, "TC", "30"));
		GeneralDao.Instance.commit();
	}
}
