package vaulsys.network;

import vaulsys.base.Manager;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.atm.ATMConnectionStatus;
import vaulsys.util.ConfigUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import vaulsys.util.DBConfigUtil;

public class ATMSessionManager implements Manager, Runnable{
	private static final Logger logger = Logger.getLogger(ATMSessionManager.class);
	private static ATMSessionManager instance;
	private static boolean doLoop = true; 
	
	private ATMSessionManager(){}
	
	public static ATMSessionManager get() {
		if(instance == null)
			instance = new ATMSessionManager();
		return instance;
	}
	
	private BlockingQueue<ATMSessionState> queue = new LinkedBlockingQueue<ATMSessionState>();
	
	public void addATMSessionStatus(String atmIp, boolean connect){
		queue.add(new ATMSessionState(atmIp, connect));
	}

	@Override
	public void startup() throws Exception {
		setAllATMsDisconnected();
		Thread th = new Thread(this);
		th.start();
		logger.info("ATM Session Manager Started");
	}

	@Override
	public void shutdown() {
		doLoop = false;
		if(queue.size()==0)
			queue.add(new ATMSessionState());
		logger.info("ATM Session Manager Stopped");
	}

	@Override
	public void run() {
		while(doLoop){
			try {
				ATMSessionState atm = queue.take();
				if(atm.atmIp==null)
					continue;

				GeneralDao.Instance.beginTransaction();
				
				byte bConn;
				if(atm.connected){
					bConn = ATMConnectionStatus.CONNECTED_VALUE;
				}else{
					bConn = ATMConnectionStatus.NOT_CONNECTED_VALUE;
				}
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("connection", bConn);
				params.put("ip", atm.atmIp);
				GeneralDao.Instance.executeSqlUpdate("update "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".term_atm set connection= :connection where IP= :ip",params);
//				GeneralDao.Instance.executeSqlUpdate("update "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".term_atm set connection="+bConn+" where IP= '"+atm.atmIp+"'");
				GeneralDao.Instance.commit();
			} catch (Exception e) {
				logger.error("ATM Session Manager: ", e);
				GeneralDao.Instance.rollback();
			} finally {
				GeneralDao.Instance.close();
			}
		}
	}
	
	private void setAllATMsDisconnected(){
		byte bConn = ATMConnectionStatus.NOT_CONNECTED_VALUE;
		GeneralDao.Instance.executeSqlUpdate("update "+ DBConfigUtil.getDecProperty(DBConfigUtil.DB_SCHEMA)+".term_atm set connection="+bConn);
	}
	
	private class ATMSessionState {
		public String atmIp = null;
		public Boolean connected = null;
		
		public ATMSessionState() {}

		public ATMSessionState(String atmIp, boolean connected) {
			this.atmIp = atmIp;
			this.connected = connected;
		}
	}
}
