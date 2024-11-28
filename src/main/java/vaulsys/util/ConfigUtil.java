package vaulsys.util;

import vaulsys.security.component.SecurityComponent;
import vaulsys.security.jceadapter.JCESecurityModule;
import vaulsys.util.ConfigUtil.Key;
import vaulsys.util.encoders.Hex;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {
	public static final String CONVERTOR = "convertor";
	public static final String GLOBALPATHS = "global.path";
	
	public static final Key DB_URL = new Key("db.url", true);
	public static final Key DB_USERNAME = new Key("db.username", true);
	public static final Key DB_PASSWORD = new Key("db.password", true);
	public static final Key DB_SHOW_SQL = new Key("db.showSQL", true);
	public static final Key DB_HBM2DDL = new Key("db.hbm2ddl", true);
	public static final Key DB_SCHEMA = new Key("db.schema", true);

	public static final Key DB_USERNAME_SETTLE = new Key("db.username_settle", true);
	public static final Key DB_PASSWORD_SETTLE = new Key("db.password_settle", true);
	//m.rehman: for reports
	public static final Key DB_DRIVER = new Key("db.driver", true);
	
	public static final Key QUARTS_SETTLE_CONFIG_FILE = new Key("quarts.settle.config.file", false);
	public static final Key QUARTS_SWITCH_CONFIG_FILE = new Key("quarts.switch.config.file", false);
	//m.rehman: for reporting
	public static final Key QUARTS_REPORT_CONFIG_FILE = new Key("quarts.report.config.file", false);
	
	public static final Key GLOBAL_PATH_FILE_REPORT = new Key("global.path.file.report", false);
	public static final Key GLOBAL_PATH_FILE_LOG = new Key("global.path.file.log", false);
	public static final Key GLOBAL_PATH_FILE_CONFIG = new Key("global.path.file.config", false);
	public static final Key GLOBAL_PATH_FILE_ROUTING = new Key("global.path.file.routing", false);
	public static final Key GLOBAL_PATH_FILE_DEFAULTCFGFILE = new Key("global.path.file.defaultcfgfile", false);
	
	public static final Key GLOBAL_PATH_SCHEMA_BANKSTATEMENT = new Key("global.path.schema.bankStatement", false);
	public static final Key GLOBAL_PATH_SCHEMA_POSRECONCILE = new Key("global.path.schema.posReconcile", false);
	public static final Key GLOBAL_PATH_SCHEMA_SHETAB = new Key("global.path.schema.shetab", false);
	
	public static final Key GLOBAL_MAX_TRX_IN_ACCOUNTING_ITERATION = new Key("global.settle.recordsize", false);
	public static final Key GLOBAL_SETTLE_SETTLEMENTDATASIZE = new Key("global.settle.settlementdatasize", false);
	public static final Key GLOBAL_REPORT_RECORDSIZE = new Key("global.report.recordsize", false);

//	public static final Key GLOBAL_RECEIPT_FARSI_NAME = new Key("global.receipt.farsi.name", false);
//	public static final Key GLOBAL_RECEIPT_ENGLISH_NAME = new Key("global.receipt.english.name", false);
//	public static final Key GLOBAL_RECEIPT_FARSI_MOUNT = new Key("global.receipt.farsi.mount", false);
//	public static final Key GLOBAL_RECEIPT_ENGLISH_MOUNT = new Key("global.receipt.english.mount", false);
	
	public static final Key REPEAT_COUNT = new Key("repeat.count", false);
	//public static final Key REPEAT_INTERVAL = new Key("repeat.interval", false);
	public static final Key REPEAT_TIMEOUT = new Key("repeat.timeout", false);
	public static final Key REPEAT_SLEEP_TIME = new Key("repeat.sleeptime", false);
	public static final Key REVERSAL_COUNT = new Key("reversal.count", false);
	public static final Key REVERSAL_TIMEOUT = new Key("reversal.timeout", false);
	public static final Key REVERSAL_SLEEP_TIME = new Key("reversal.sleeptime", false);
	
	public static final Key JOB_REPEATINTERVAL = new Key("repeatInterval", false); 
	public static final Key JOB_REPEATCOUNT = new Key("repeatCount", false); 
	public static final Key JOB_CRONEXPRESSION = new Key("cronExpression", false); 
	public static final Key JOB_VOLATILIZEABLE = new Key("volatilizeable", false);
	public static final Key JOB_DURABALE = new Key("durabale", false);
	public static final Key JOB_SHOULDRECOVER = new Key("shouldRecover", false);
	public static final Key JOB_STARTTIME = new Key("startTime", false);
	public static final Key JOB_MISFIREINSTRUCTION = new Key("misfireInstruction", false);
	public static final Key JOB_EXECUTENOW = new Key("executeNow", false);
	
	public static final Key REMOTE_MANAGER_PORT = new Key("remote.manager.port", false);
	public static final Key REMOTE_MANAGER_SETTLE_PORT = new Key("remote.manager.settle.port", false);

	public static final Key MCI_WS_ADDRESS = new Key("mci.ws.address", false);
	public static final Key MCI_WS_ID = new Key("mci.ws.id", false);
	
	//MTN topUp
	public static final Key MTN_TOPUP_WS_ADDRESS = new Key("mtn.topup.ws.address", false);
	public static final Key MTNTOPUP_PRIVATE_KEY = new Key("mtn.topup.privatekey", false);
	public static final Key MTNTOPUP_COUNT = new Key("mtntopup.count", false);
	public static final Key MTNTOPUP_TIMEOUT = new Key("mtntopup.timeout", false);
	public static final Key MTNTOPUP_SLEEP_TIME = new Key("mtntopup.sleeptime", false);
	public static final Key MTNTOPUP_PRODUCTCODE = new Key("mtntopup.productcode", false);
	
	
	//mci virtual vosoli
	public static final Key MCIVOSOLI_REPEAT_COUNT = new Key("mci.vosoli.repeat.count", false);
	public static final Key MCIVOSOLI_REPEAT_INTERVAL = new Key("mci.vosoli.repeat.interval", false);
	public static final Key MCIVOSOLI_COUNT = new Key("mci.vosoli.count", false);
	public static final Key MCIVOSOLI_TIMEOUT = new Key("mci.vosoli.timeout", false);
	public static final Key MCIVOSOLI_SLEEP_TIME = new Key("mci.vosoli.sleeptime", false);
	public static final Key WIN_SREVER_IP = new Key("win_server_ip", false);
	public static final Key WIN_SREVER_PORT = new Key("win_server_port", false);
	public static final Key MCI_VIRTUAL_VOSOLI_URL = new Key("mci_virtual_vosoli_url", false);
	public static final Key MCI_VIRTUAL_VOSOLI_USER = new Key("mci_virtual_vosoli_user", false);
	public static final Key MCI_VIRTUAL_VOSOLI_PASS = new Key("mci_virtual_vosoli_pass", false);
	public static final Key MCI_COMPANYCODE = new Key("mci_companycode", false);
	public static final Key MCI_HAS_VOSOLI = new Key("mci_has_vosoli", false);
	public static final Key MCI_VOSOLI_BANKID = new Key("mci.vosoli.bankid", false);
	
	//majid_prg 20150520
	//naja vosoli 
	public static final Key NAJAVOSOLI_REPEAT_COUNT = new Key("vosoli.naja.repeat.count", false);
	public static final Key NAJAVOSOLI_REPEAT_INTERVAL = new Key("vosoli.naja.repeat.interval", false);
	public static final Key NAJAVOSOLI_COUNT = new Key("vosoli.naja.count", false);
	public static final Key NAJAVOSOLI_TIMEOUT = new Key("vosoli.naja.timeout", false);
	public static final Key NAJAVOSOLI_SLEEP_TIME = new Key("vosoli.naja.sleeptime", false);
	public static final Key NAJAServiceURL = new Key("vololi.naja.ServiceURL", false);
	public static final Key NAJAVOSOLI_USER = new Key("vosoli.naja.user", false);
	public static final Key NAJAVOSOLI_PASS = new Key("vosoli.naja.pass", false);
	public static final Key NAJAVOSOLI_COMPANYCODE = new Key("vosoli.naja.companycode", false);
	public static final Key NAJAVOSOLI_ENABLE = new Key("vosoli.naja.enable", false);
	public static final Key NAJAVOSOLI_XHOURAFTERFIRE = new Key("vosoli.naja.xhourafterfire", false);
	public static final Key NAJAVOSOLI_BANKID = new Key("vosoli.naja.bankid", false);
	
	//Ghasedak
	public static final Key GHASEDAK_DB_URL = new Key("ghasedak.db.url", false);
	public static final Key GHASEDAK_DB_NAME = new Key("ghasedak.db.name", false);
	public static final Key GHASEDAK_DB_PATH = new Key("ghasedak.db.path", false);
	public static final Key GHASEDAK_DB_USER = new Key("ghasedak.db.username", false);
	public static final Key GHASEDAK_DB_PASS = new Key("ghasedak.db.encrypted.password",false);
	public static final Key GHASEDAK_DB_KEY = new Key("ghasedak.db.key", false);
	public static final Key GHASEDAK_DB_START_HSQL = new Key("ghasedak.db.start.hsql", false);
	public static final Key GHASEDAK_DB_SHOWSQL = new Key("ghasedak.db.showsql", false);
	public static final Key GHASEDAK_DB_SCHEMA = new Key("ghasedak.db.schema", false);
	public static final Key GHASEDAK_DB_HBM2DDL = new Key("ghasedak.db.hbm2ddl", false);
	public static final Key GHASEDAK_HIBERNATE = new Key("ghasedak.hibernate", false);
	
	// T H R E A D   P O O L
	public static final Key THREADPOOL_MAIN_CORE_SIZE = new Key("threadpool.main.core.size", false);
	public static final Key THREADPOOL_MAIN_MAX_SIZE = new Key("threadpool.main.max.size", false);
	public static final Key THREADPOOL_MAIN_MAX_QUEUE_SIZE = new Key("threadpool.main.max.queue.size", false);
	public static final Key THREADPOOL_MAIN_KEEP_ALIVE_TIME = new Key("threadpool.main.keep.alive.time", false);
	public static final Key THREADPOOL_MAIN_REJECTED_THRESHOLD = new Key("threadpool.main.rejected.threshold", false); 

	public static final Key THREADPOOL_SORUSH_CORE_SIZE = new Key("threadpool.sorush.core.size", false);
	public static final Key THREADPOOL_SORUSH_MAX_SIZE = new Key("threadpool.sorush.max.size", false);
	public static final Key THREADPOOL_SORUSH_MAX_QUEUE_SIZE = new Key("threadpool.sorush.max.queue.size", false);
	public static final Key THREADPOOL_SORUSH_KEEP_ALIVE_TIME = new Key("threadpool.sorush.keep.alive.time", false);
	public static final Key THREADPOOL_SORUSH_REJECTED_THRESHOLD = new Key("threadpool.sorush.rejected.threshold", false);

	public static final Key THREADPOOL_SCHEDULE_CORE_SIZE = new Key("threadpool.schedule.core.size", false);
	public static final Key THREADPOOL_SCHEDULE_MAX_SIZE = new Key("threadpool.schedule.max.size", false);
	public static final Key THREADPOOL_SCHEDULE_MAX_QUEUE_SIZE = new Key("threadpool.schedule.max.queue.size", false);
	public static final Key THREADPOOL_SCHEDULE_KEEP_ALIVE_TIME = new Key("threadpool.schedule.keep.alive.time", false);
	
	// SMB File Transfer
	public static final Key SMB_AUTH_USERNAME = new Key("smb.auth.username", false);
	public static final Key SMB_AUTH_PASSWORD = new Key("smb.auth.password", false);
	public static final Key SMB_IP = new Key("smb.ip", false);

    // SFTP File Transfer
    public static final Key SFTP_AUTH_USERNAME = new Key("sftp.auth.username", false);
    public static final Key SFTP_AUTH_PASSWORD = new Key("sftp.auth.password", false);

    public static final Key SMS_SERVER_IP = new Key("sms.server.ip", false);
	public static final Key SMS_SERVER_PORT = new Key("sms.server.port", false);
	
	private static final Properties PROPERTIES = new Properties();
	private static final JCESecurityModule SSM;

	// check duplicate message
	public static final Key CHECK_DUPLICATE_MESSAGE = new Key("check_duplicate_message", false);
	
	public static final Key APACS_ALWAYS_PAD_NULL = new Key("apacs_always_pad_null", false);
	
	public static final Key PARTIAL_DISPENSE_SUPPORT = new Key("partial_dispense_support", false);
	
	public static final Key REV_TRANSFER_TO = new Key("rev_transfer_to", false);
	
	public static final Key INFOTECH_HAS_DAILY_MESSAGE = new Key("infotech.hasDailyMessage", false);
	public static final Key INFOTECH_HAS_NAME = new Key("infotech.hasName", false);
	public static final Key INFOTECH_HAS_ADDRESS = new Key("infotech.hasAddress", false);
	public static final Key INFOTECH_HAS_PHONE_NUMBER = new Key("infotech.hasPhoneNumber", false);
	public static final Key INFOTECH_HAS_WEB_SITE_ADDRESS = new Key("infotech.hasWebsiteAddress", false);
	public static final Key INFOTECH_HAS_CARD_PRODUCT = new Key("infotech.hasCardProduct", false);
	public static final Key INFOTECH_HAS_FOOTER = new Key("infotech.hasFooter", false);
	public static final Key INFOTECH_SPECIAL_CHAR = new Key("infotech.specialChar", false);
	
	public static final Key POS87_HAS_DAILY_MESSAGE = new Key("pos87.hasDailyMessage", false);
	public static final Key POS87_HAS_NAME = new Key("pos87.hasName", false);
	public static final Key POS87_HAS_ADDRESS = new Key("pos87.hasAddress", false);
	public static final Key POS87_HAS_PHONE_NUMBER = new Key("pos87.hasPhoneNumber", false);
	public static final Key POS87_HAS_WEB_SITE_ADDRESS = new Key("pos87.hasWebsiteAddress", false);
	public static final Key POS87_HAS_FOOTER = new Key("pos87.hasFooter", false);
	
	public static final Key APACS70_SPECIAL_CHAR = new Key("apacs70.specialChar", false);
	public static final Key APACS70_HAS_MERCHANT_HEADER = new Key("apacs70.hasMerchantHeader", false);
	public static final Key APACS70_HAS_MERCHANT_FOOTER = new Key("apacs70.hasMerchantFooter", false);
	public static final Key APACS70_HAS_CARD_HOLDER_HEADER = new Key("apacs70.hasCardHolderHeader", false);
	public static final Key APACS70_HAS_CARD_HOLDER_FOOTER = new Key("apacs70.hasCardHolderFooter", false);
	
	public static final Key CMS_SERVICE_URL = new Key("cms.service.url", false);
	public static final Key CMS_SERVICE_USERNAME = new Key("cms.service.userName", false);
	public static final Key CMS_SERVICE_PASSWORD = new Key("cms.service.password", false);
	public static final Key CMS_SERVICE_CARDGROUPHIERARCHY = new Key("cms.service.cardGroupHierarchy", false);
	//Mirkamali(Task159)
	public static final Key CMS_MAX_THREAD = new Key("cms.max.thread",false);
	
	public static final Key ATM_CHANGE_KEY_INTERVAL_DAY = new Key("atm_change_key_interval_day", false);
	
	public static final Key THREE_BIN_TRANSFER_SUPPORT = new Key("three_bin_transfer_support", false);
	
	public static final Key MY_BANK_ACRONYM = new Key("mybankacronym", false);
	public static final Key BANK_NAME = new Key("bank.name", false);
	
	public static final Key SHAPARAK_BIN = new Key("shaparak_bin", false);
	
	public static final Key SORUSH_CODE = new Key("sorush_code", false);
	
	public static final Key CHARGE_EMPTY_DB_CHECK_PERIOD = new Key("charge_empty_db_check_period", false);

	public static final Key DB_UPGRADE_LOCK_MAX_RETRY_COUNT = new Key("db_upgrade_lock_max_retry_count", false);
    public static final Key DB_UPGRADE_LOCK_WAIT = new Key("db_upgrade_lock_wait", false);
    
    
    
    //Mirkamali(Task144)
    public static final Key SHETAB_PUTON_FTP = new Key("shetab.putonftp", false);
	public static final Key SHETAB_FTP_PATH = new Key("shetab.ftp.path", false);
	public static final Key SHETAB_FTP_CONNECTION_TIMEOUT = new Key("shetab.ftp.Conection.timeout", false);
	public static final Key SHETAB_FTP_DATA_TIMEOUT = new Key("shetab.ftp.Data.timeout", false);
	public static final Key SHETAB_FTP_DEFAULT_TIMEOUT = new Key("shetab.ftp.Default.timeout", false);
	public static final Key SHETAB_FTP_IP = new Key("shetab.ftp.ip", false);
	public static final Key SHETAB_FTP_PORT = new Key("shetab.ftp.port", false);
	public static final Key SHETAB_FTP_USERNAME = new Key("shetab.ftp.username", false);
	public static final Key SHETAB_FTP_PASSWORD = new Key("shetab.ftp.password", false);
	
	//majid_prg
	public static final Key COM_POLICE = new Key("com.police", false);
	//moosavi:curreny Atm : Delete card data job
	public static final Key DELETE_CARD_DATA_REPEAT_INTERVAL = new Key("job.switch.DeleteCardDataJob.repeatInterval ", false);
	
	/**
	 *@author k.khodadi
	 *For support Query with old Stracture Table IFX (ifxSrcTrnSeqCntr,ifxEncAppPAN)    
	 */
	public static final Key OLD_QUERY_IFX_SUPPORT = new Key("old_query_ifx_support", false);
	
    
    /**
     * @author k.khodadi 
     * for thread base connction with UI
     */
    public static final Key IS_REMOTE_MESSAGE_THREAD = new Key("isremotemessagethread", false);
    
	//TASK Task074 : Get ATM Status
	public static final Key SPECIAL_ATM_LIST_FILE_PATH = new Key("special_atm_list_file_path", false);
	
	public static final Key MCI_BILL_PAYMENT_WEBSERVICE_ENDPOINT = new Key("mci.billpayment.webservice.endpoint", false);
	public static final Key MCI_BILL_PAYMENT_WEBSERVICE_RETRY_COUNT = new Key("mci.billpayment.webservice.retryCount", false);
	
	//Mirkamali(Task151) : Sinaps billPayment (copied from shaparak pasargad)
    public static final Key SINAPS_WSDL = new Key("sinaps.wsdl", false);
    public static final Key SINAPS_USERNAME = new Key("sinaps.username", false);
    public static final Key SINAPS_COMPANY_CODE = new Key("sinaps.company.code", false);
    public static final Key SINAPS_PRIVATE_KEY = new Key("sinaps.private.key", false);
    public static final Key SINAPS_BANK_ID = new Key("sinaps.bankid", false);
    public static final Key SINAPS_BRANCH = new Key("sinaps.branch", false);
    public static final Key SINAPS_UTILITY_CODE = new Key("sinaps.utilitycode", false);
    

	static {
		try {
			//PROPERTIES.load(new FileInputStream("config/config.properties"));
			//SSM = new JCESecurityModule("config/LMK.jceks", "$3cureP@$$".toCharArray(),
			//		"org.bouncycastle.jce.provider.BouncyCastleProvider");
			PROPERTIES.load(ConfigUtil.class.getResourceAsStream("/config/config.properties"));
			URL resource = ConfigUtil.class.getResource("/config/LMK.jceks");
			String file = resource.getFile();
			SSM = new JCESecurityModule(file, "$3cureP@$$".toCharArray(),
					"org.bouncycastle.jce.provider.BouncyCastleProvider");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getProperty(Key key) {
		if (PROPERTIES.containsKey(key.getProp()))
			return PROPERTIES.getProperty(key.getProp());
		else
			throw new IllegalArgumentException(String.format("[%s] key not found in config.properties!", key.getProp()));
	}

	public static String getProperty(Key key, String prefix) {
		String name = prefix+"."+ key.getProp();
		if (PROPERTIES.containsKey(name))
			return PROPERTIES.getProperty(name);
		else
			return null;
	}
	
	
	public static Boolean getBoolean(Key key) {
		return Boolean.valueOf(getProperty(key));
	}

	public static Integer getInteger(Key key){
		return Integer.valueOf(getProperty(key));
	}
	
	public static Long getLong(Key key){
		return Long.valueOf(getProperty(key));
	}
	
	public static String getDecProperty(Key key) {
		try {
			if (isDecryptionEnabled())
				return new String(SSM.rsaDecrypt(Hex.decode(getProperty(key))));
			else
				return getProperty(key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean isDecryptionEnabled() {
		String dec = PROPERTIES.getProperty("general.decryption.enabled", Boolean.TRUE.toString());
		return Boolean.valueOf(dec);
	}

	private static String getGroup() {
		return PROPERTIES.getProperty("general.group", "bank");
	}
	
	public static class Key {
		private String prop;
		private boolean groupy;
		private String groupName;

		private Key(String prop, boolean groupy) {
			this.prop = prop;
			this.groupy = groupy;
		}

		public String getProp() {
			if (groupy)
				return String.format("%s.%s", getGroup(), prop);
			else
				return prop;
		}
	}
	
	public static Map<String, String> getProperties(String prefix){
		Map<String, String> map = new HashMap<String, String>();
		String pattern = "^"+prefix+"[.]\\w+";
		for (Object k: PROPERTIES.keySet()){
			String key =  k.toString();
			if (key.matches(pattern))
				map.put(key, PROPERTIES.getProperty(key));
		}
		return map;
	}
}
