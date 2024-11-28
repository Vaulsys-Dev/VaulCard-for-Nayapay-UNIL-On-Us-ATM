package vaulsys.wfe;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.authorization.policy.*;
import vaulsys.base.Manager;
import vaulsys.caching.CheckAccountParamsForCache;
import vaulsys.clearing.SynchronizationService;
import vaulsys.clearing.base.ClearingAction;
import vaulsys.clearing.base.ClearingActionMapper;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.CriteriaData;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataCriteria;
import vaulsys.clearing.jobs.*;
import vaulsys.clearing.jobs.apacs70.Apacs70ClearingActionMapper;
import vaulsys.clearing.jobs.apacs70.Apacs70ClearingJob;
import vaulsys.clearing.jobs.infotech.InfotechClearingActionMapper;
import vaulsys.clearing.jobs.infotech.InfotechClearingJob;
import vaulsys.clearing.jobs.negin.NeginAcquirerReconcilementResponseJob;
import vaulsys.clearing.jobs.negin.NeginIssuerReconcilementResponseJob;
import vaulsys.cms.base.CMSProduct;
import vaulsys.cms.base.CMSProductDetail;
import vaulsys.cms.base.CMSProductKeys;
import vaulsys.cms.base.CMSTrack2Format;
import vaulsys.cms.components.CMSStatusCodes;
import vaulsys.contact.City;
import vaulsys.contact.Contact;
import vaulsys.contact.State;
import vaulsys.customer.Currency;
import vaulsys.discount.BaseDiscount;
import vaulsys.discount.DiscountProfile;
import vaulsys.entity.impl.Institution;
import vaulsys.fee.impl.BaseFee;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.loro.Loro;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.mtn.MTNChargeSpecification;
import vaulsys.mtn.impl.GeneralChargeAssignmentPolicy;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.network.channel.base.ChannelType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ChequeReturnCodes;
import vaulsys.protocols.ProtocolConfig;
import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ndc.encoding.NDCConvertor;
import vaulsys.routing.base.RoutingTable;
import vaulsys.routing.components.Routing;
import vaulsys.routing.extended.MessageRouting;
import vaulsys.security.base.SecurityFunction;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.hsm.base.HSMChannel;
import vaulsys.security.hsm.base.HSMChannelManager;
import vaulsys.terminal.atm.*;
import vaulsys.terminal.atm.customizationdata.*;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.user.User;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Pair;
import vaulsys.util.SwitchRuntimeException;
import vaulsys.util.Util;
import vaulsys.webservice.keyvault.entity.KeyVaultServer;
import vaulsys.webservice.walletcardmgmtwebservice.entity.SwitchTransactionCodes;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalContext implements Manager {
	private transient Logger logger = Logger.getLogger(GlobalContext.class);

	private static GlobalContext globalContext = null;
	private static SecureRandom secureRandom = new SecureRandom();
	private static GroovyShell groovyShell = new GroovyShell();
	
	public static final int RIAL_CURRENCY_CODE = 364;
//	public static final long REVERSAL_PROCESS_WAIT_TIME = 3000L;
//	public static final long TOPUP_PROCESS_WAIT_TIME = 3000L;
	
	//Mirkamali(Task159)
//	public static int CMS_THREAD_NUMBER = 0;
    public static AtomicInteger CMS_THREAD_NUMBER = new AtomicInteger(0);

	public static ConfigUtil.Key dbUserName = null;
	public static ConfigUtil.Key dbPasswored = null;

	private static final String CHANNELS_KEY = "channels";
	private static final String CHANNELSID_KEY = "channelsid"; //Raza adding for WebService
	private static final String ROUTING_TABLES_KEY = "routing";
	private static final String CURRENCIES_KEY = "currencies";
	private static final String SWIFTCURRENCIES_KEY = "swiftcurrencies"; //Raza adding for NayaPay will update in future look for Stream API
	private static final String NAYAPAYBANKS_KEY = "nayapaybanks"; //Raza adding for NayaPay
	private static final String STATES_KEY = "states"; //Raza adding for NayaPay
	private static final String CITIES_KEY = "cities"; //Raza adding for NayaPay
	private static final String MY_INSTITUTION_KEY = "my-institution";
	private static final String MY_INSTITUTIONS_KEY = "my-institutions";
	private static final String PEER_INSTITUTION_KEY = "peer-institution";
	private static final String PEER_INSTITUTION_BIN_KEY = "peer-institution-bin";
	private static final String INSTITUTIONS_KEY = "institutions";
//	private static final String SECURITY_PROF_KEY = "security-profile";
	private static final String SECURITY_FUNCTIONS_KEY = "security-functions";
	private static final String FEE_PROF_KEY = "fee-profile";
	private static final String DIS_PROF_KEY = "dis-profile";
	private static final String SWITCH_TERMINAL_KEY = "switch-terminal";
	private static final String MTN_CHARGE_KEY = "mtn-charge-spec";
	private static final String CONVERTOR_KEY = "convertors";
	private static final String SWITCH_USER = "switch_user";
	private static final String GENERAL_CHARGE_POLICY = "general_charge_policy";
	private static final String ATM_KEY = "atm_key"; //		"2020202020202020";
	private static final String CLEARING_ACTION_JOB = "clearing_action_job"; 
	private static final String CLEARING_ACTION_MAPPER = "clearing_action_mapper";
	private static final String BANKS_KEY = "banks";
	private static final String AUTH_PROF_KEY = "auth-profile";
	private static final String ATM_CONFIG_KEY = "atm-configuration";
	private static final String ATM_REQUEST_KEY = "atm-request";
	private static final String APPLICATION_NAME_KEY = "application_name";
	private static final String PROTOCOL_CONFIG = "protocol-config";
	private static final String CLEARING_PROFILE_KEY ="Clearing_profile";
	private static final String LOTTERY_ASSIGNMENT_POLICY_KEY = "lottery_assihnment_policy";

	// Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
	//by m.rehman
	private static final String MESSAGE_ROUTING_KEY = "message-routing";
	private static final String NETWORK_INFO_KEY = "network-info"; //Raza Adding from TPSP
	private static final String BASE_CURRENCY_KEY = "base-currency";
	private static final String LORO_KEY = "loro";
	private static final String HSM_CHANNELS_KEY = "hsm-channels";
	private static final String CMS_PRODUCTS_KEY = "cms-products";
	private static final String CMS_PRODUCT_DETAIL_KEY = "cms-product-detail";
	private static final String CMS_TRACK_2_FORMATS_KEY = "cms-track-2-formats";
	private static final String CMS_PRODUCTS_KEYS_KEY = "cms-product-keys";
	private static final String WALLET_IMDS_KEY = "wallet-imds";
	private static final String WEBSERVERS_KEY = "webservers"; //Raza NAYAPAY
	private static final String CONTACT_CORE_DOWN = "contact_core_down";
	private Map<String, Object> variables;
	private Map<String, Script> groovyScript;
	private Map<ProtocolType, Map<String, String>> protocolConfig;
	private static final String TRANCODE_TO_API_KEY = "trantoapi"; //Raza adding for NayaPay
	private static final String API_TO_TRANCODE_KEY = "apitotran"; //Raza adding for NayaPay
	//m.rehman: for NayaPay, for bin from bank code
	private static final String BANKCODE_TO_BIN_KEY = "bankcode-to-bin";
	private static final String BIN_TO_BANKCODE_KEY = "bin-to-bankcode"; //Raza adding for NayaPay linked accounts list
	private static final String TRANSACTIONCODES_KEY = "transactioncodes"; //Raza adding for WebService Reversal 25-06-2019
	private static final String FINANCIAL_TRANSACTIONS = "financial-transactions";

	//m.rehman: 05-08-2020, fetching key from vault
	private static final String PAN_ENC_KEY = "pan-enc-key";
	
	private Map<String, TerminalType> terminalId_Type ; 
	private Map<String, Long> bindTransaction ; 
	private Map<Long, String> securityData; 
	private Map<Long, String> acctInfoForTransfer;
    private Map<CheckAccountParamsForCache, Long> checkAccountForTransafer;

	//m.rehman: 22-11-2021, HSM response logging
	private Map<String, byte[]> hsmResponse;
	
	private GlobalContext() {
		variables = new ConcurrentHashMap<String, Object>();
		groovyScript = new ConcurrentHashMap<String, Script>();
		protocolConfig = new ConcurrentHashMap<ProtocolType, Map<String,String>>();
		terminalId_Type = new ConcurrentHashMap<String, TerminalType>();
		bindTransaction = new ConcurrentHashMap<String, Long>();
		securityData = new ConcurrentHashMap<Long, String>();
		acctInfoForTransfer = new ConcurrentHashMap<Long, String>();
        checkAccountForTransafer = new ConcurrentHashMap<CheckAccountParamsForCache, Long>();

		//m.rehman: 22-11-2021, HSM response logging
		hsmResponse = new ConcurrentHashMap<String, byte[]>();
	}

	public static GlobalContext getInstance() {
		if (globalContext == null) {
			globalContext = new GlobalContext();
			//globalContext.startup(); called in Application
		}
		return globalContext;
	}

	public void startup() {
		logger.debug("Puting application name in GlobalContext");
		putApplicationName();
		
		logger.debug("Releasing SyncObjects locked by application");
		releaseSyncObjectLock();
		
		logger.debug("Puting configuration data in GlobalContext");
		try {
			logger.debug("Puting Channels in GlobalContext");
			setAllChannels();
			
			//logger.debug("Just Testing");
			//TxnRule.ValidateTxn();
			logger.debug("Puting all ClearingProfile in GlobalContext");
			setAllClearingProfile();
			
			logger.debug("Puting all LotteryAssignmentPolicy in GlobalContext");
			setAllLotteryAssignmentPolicy();
			
			logger.debug("Puting all convertors in GlobalContext");
			setAllConvertors();

			logger.debug("Puting all clearing stuff in GlobalContext");
			setAllClearingStuff();

			//logger.debug("Puting routing tables in GlobalContext"); //Raza Commenting now working on Routing from DB
			//setAllRoutingTables(); //Raza Commenting now working on Routing from DB

			logger.debug("Puting currencies in GlobalContext");
			setAllCurrencies();

			logger.debug("Puting all SecurityFunctions in GlobalContext");
			setAllSecurityFunctions();

			logger.debug("Puting all FeeProfiles in GlobalContext");
			setAllFeeProfiles();
			
			logger.debug("Puting all DiscountProfiles in GlobalContext");
			setAllDiscountProfiles();
			
			logger.debug("Puting all AuthorizationProfiles in GlobalContext");
			setAllAuthorizationProfiles();

			logger.debug("Puting all institutions in GlobalContext");
			setAllInstitutions();

			logger.debug("Puting all banks in GlobalContext");
			setAllBanks();

			logger.debug("Puting ATM Key in GlobalContext");
			setATMKey();
			
			logger.debug("Puting ATM Configurations in GlobalContext");
			setAllATMConfigurations();
			
			logger.debug("Putting Message Routing in GlobalContext");
			setAllMessageRouting();

			logger.debug("Putting HSM Channels in GlobalContext");
			setAllHSMChannels();

			logger.debug("Putting Card & Account Status Codes");
			CMSStatusCodes.LoadCodes(); //Raza adding for Card,Account&Customer Status

			logger.debug("Putting States Data"); //Raza NayaPay
			setAllStates();

			logger.debug("Putting Cities Data"); //Raza NayaPay
			setAllCities();

//			logger.debug("Loading Webservers details"); //Raza NAYAPAY
//			setAllWebservers(); //Raza commenting as Channel and WebServer are managed through same object

			logger.debug("Loading Cheque Codes"); //Raza NayaPay Askari
			ChequeReturnCodes.load();

			logger.debug("Loading Transaction Codes Description"); //Raza 01072019
			setTransactionCodes();

			logger.debug("Loading Financial Transaction Codes");
			setFinancialTransactionCodes();

//			//Raza TEST start
//			Boolean ret = NetworkAuthorization.AuthorizeTxn("channelNACIn","channelNACIn", TrnType.BALANCEINQUIRY);
//
//			//Raza TEST end
			try {
				logger.debug("Puting all Cell Phone Charge Specification in GlobalContext");
				setAllCellPhoneChargeSpecification();
				logger.debug("Puting charge policy in GlobalContext");
				setGeneralChargePolicy();
			} catch (Exception e) {
				logger.warn("MTN Charge couldn't loaded in GlobalContext");
			}

			logger.debug("Puting switch user in GlobalContext");
			setSwitchUser();
			
			logger.debug("Loading protocol configs");
			setAllProtocolConfig();

			if (Util.getMainClassName().equals("vaulsys.application.VaulsysWCMS")) {
				logger.debug("Starting release memory");
				ReleaseMemoryThread rmThread = new ReleaseMemoryThread();
				Thread thread = new Thread(rmThread);
				logger.debug("Thread: " + thread.getName() + " is starting...");
				thread.start();
			}

			//m.rehman: set all loro entries
			logger.debug("Loading Loro entries");
			setAllLoroEntries();

			//m.rehman: loading cms products/product detail/track 2 format/product keys
			//logger.debug("Loading Product entries");
			//setAllCMSProducts();
			//logger.info("Not Loading Product entries"); //Raza TODO

			//m.rehman: 05-08-2020, pan encryption/decryption using vault keys
			logger.debug("Loading key from Key Vault");
			FetchKeyFromKeyVault();

			
			logger.info("configuration data was successfully loaded....");
		} catch (Exception e) {
			logger.error(e,e);
			throw new SwitchRuntimeException(e.getMessage());
		}
	}

	private void releaseSyncObjectLock() {
		if (!Util.hasText(getApplicationName()))
			throw new NullPointerException("No Application Name!!");
		SynchronizationService.releaseApplicationLock(getApplicationName());
	}
	
	public TerminalType getTerminalType(String terminalId) {
		return terminalId_Type.get(terminalId);
	}

	public void addTerminalId_Type(String terminalId, TerminalType terminalType) {
		if (!terminalId_Type.containsKey(terminalId))
			terminalId_Type.put(terminalId, terminalType);
	}

	public Map<String, Long> getBindTransaction() {
		return bindTransaction;
	}
	
	public Long getBindTransaction(String bindingParam) {
		return bindTransaction.get(bindingParam);
	}
	
	public void addBindTransaction(String bindingParam, Long transactionId) {
		if (!bindTransaction.containsKey(bindingParam))
			bindTransaction.put(bindingParam, transactionId);
	}
	
	public void removeBindTransaction(String bindingParam) {
		if (bindTransaction.containsKey(bindingParam))
			bindTransaction.remove(bindingParam);
	}
	
	public Map<Long, String> getSecurityData() {
		return securityData;
	}
	
	public String getSecurityData(Long lifeCycle) {
		return securityData.get(lifeCycle);
	}
	
	public void addSecurityData(Long lifeCycle, String securityStr) {
		if (!securityData.containsKey(lifeCycle))
			securityData.put(lifeCycle, securityStr);
	}
	
	public void removeSecurityData(Long lifeCycle) {
		if (securityData.containsKey(lifeCycle))
			securityData.remove(lifeCycle);
	}

	/************ checkAccount for transfer ***************/
	public void addCheckAccountForTransfer(CheckAccountParamsForCache checkAccount, Long trxId){
		
		if(checkAccountForTransafer.containsKey(checkAccount))
			checkAccountForTransafer.remove(checkAccount);
		
		checkAccountForTransafer.put(checkAccount, trxId);
		
			
	}

    public void removeCheckAccountForTransfer(CheckAccountParamsForCache checkAccount){
        if(checkAccountForTransafer.containsKey(checkAccount))
            checkAccountForTransafer.remove(checkAccount);
    }

    public Map<CheckAccountParamsForCache, Long> getCheckAccountForTransafer() {
        return checkAccountForTransafer;
    }

    public Long getCheckAccountForTransfer(CheckAccountParamsForCache checkAccount){
        return checkAccountForTransafer.get(checkAccount);
    }
    /******************************************************/

	/******* AcctInfo for transfer trx ********/
	public void addAcctInfoForTransfer(Long lifeCycleId, String listOfAcct) {
		if(!acctInfoForTransfer.containsKey(lifeCycleId))
			acctInfoForTransfer.put(lifeCycleId, listOfAcct);
	}
	public void removeAcctInfoForTransfer(Long lifeCycleId) {
		if(acctInfoForTransfer.containsKey(lifeCycleId))
			acctInfoForTransfer.remove(lifeCycleId);
	}
	public Map<Long, String> getAcctInfoForTransfer() {
		return acctInfoForTransfer;
	}
	
	public String getAccInfoForTransfer(Long lifeCycleId) {
		return acctInfoForTransfer.get(lifeCycleId);
	}
	/****************************************/
	
	public void setATMKey() { //Raza INITIALIZE changing scope to public
		variables.put(ATM_KEY, "2020202020202020");
	}

	public String getATMKey(){
		return (String) variables.get(ATM_KEY);
	}
	
	private void setSwitchUser() {
		String DBINITIALIZER_USER = "switch";
		String query = "from User i where i.username = :username";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("username", DBINITIALIZER_USER);
		User user = (User) GeneralDao.Instance.findObject(query, param);
		if (user == null) {
			user = new User();
			user.setUsername(DBINITIALIZER_USER);
			GeneralDao.Instance.saveOrUpdate(user);
		}
		variables.put(SWITCH_USER, user);
	}

	public User getSwitchUser(){
		return (User) variables.get(SWITCH_USER);
	}
	
	public void setAllConvertors() throws InstantiationException, IllegalAccessException, ClassNotFoundException { //Raza INITIALIZE changing scope to public
		Map<String, String> list = ConfigUtil.getProperties(ConfigUtil.CONVERTOR);
		Map<String, EncodingConvertor> convertors = new HashMap<String, EncodingConvertor>();
		
		for (String key: list.keySet()){
			String name = key.replace(ConfigUtil.CONVERTOR+".", "")+"_"+ ConfigUtil.CONVERTOR;
			String clazz = list.get(key);
			EncodingConvertor c = (EncodingConvertor) Class.forName(clazz).newInstance();
			convertors.put(name.toUpperCase(), c);
		}

		variables.put(CONVERTOR_KEY, convertors);
	}
	
	public void setAllClearingStuff(){ //Raza INITIALIZE changing scope to public
		variables.put(CLEARING_ACTION_MAPPER,getClearingActionMapper());
		variables.put(CLEARING_ACTION_JOB, getClearingActionJobs());
	}

	private HashMap<String, ClearingActionMapper> getClearingActionMapper() {
		return new HashMap<String, ClearingActionMapper>(){
			{
				put("isoClearingActionMapper", ISOClearingActionMapper.Instance);
				put("apacs70ClearingActionMapper", Apacs70ClearingActionMapper.Instance);
				put("infotechClearingActionMapper", InfotechClearingActionMapper.Instance);
			}
		};
	}

	private Map<String,ClearingActionJobsImpl> getClearingActionJobs() {
		final ClearingActionJobsImpl clearingActionJobsImpl = new ClearingActionJobsImpl();
		clearingActionJobsImpl.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{ 
			put(ClearingAction.COUTOVER_RESPONSE, ISOCutoverResponseJob.Instance);
			 put(ClearingAction.RECONCILEMNET_REQUEST, ISOReconcilementRequstJob.Instance);
                put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, ISOAcquirerReconcilementResponseJob.Instance);
                put(ClearingAction.ISSUER_RECONCILEMNET_RESPONSE, ISOIssuerReconcilementResponseJob.Instance);
                put(ClearingAction.ACQUIRER_FINALIZE_RECONCILEMNET, ISOAcquirerFinalizeReconcilementJob.Instance);
                put(ClearingAction.ISSUER_FINALIZE_RECONCILEMNET, ISOIssuerFinalizeReconcilementJob.Instance);
		}});
		
		final ClearingActionJobsImpl neginClearingActionJobs = new ClearingActionJobsImpl();
		neginClearingActionJobs.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{
			 put(ClearingAction.COUTOVER_RESPONSE, ISOCutoverResponseJob.Instance);
                put(ClearingAction.RECONCILEMNET_REQUEST, ISOReconcilementRequstJob.Instance);
                put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, NeginAcquirerReconcilementResponseJob.Instance);
                put(ClearingAction.ISSUER_RECONCILEMNET_RESPONSE, NeginIssuerReconcilementResponseJob.Instance);
                put(ClearingAction.ACQUIRER_FINALIZE_RECONCILEMNET, ISOAcquirerFinalizeReconcilementJob.Instance);
                put(ClearingAction.ISSUER_FINALIZE_RECONCILEMNET, ISOIssuerFinalizeReconcilementJob.Instance);
		}});
		
		final ClearingActionJobsImpl posClearingActionJobs = new ClearingActionJobsImpl();
		posClearingActionJobs.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{
			put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, POSAcquirerReconcilementResponseJob.Instance);
		}});

		final ClearingActionJobsImpl apacs70ClearingActionJobs = new ClearingActionJobsImpl();
		apacs70ClearingActionJobs.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{
			put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, Apacs70ClearingJob.Instance);
		}});

		final ClearingActionJobsImpl infotechClearingActionJobs = new ClearingActionJobsImpl();
		infotechClearingActionJobs.setClearingJobMap(new HashMap<ClearingAction, ClearingJob>(){{
			put(ClearingAction.ACQUIRER_RECONCILEMNET_RESPONSE, InfotechClearingJob.Instance);
		}});

		return new HashMap<String, ClearingActionJobsImpl>(){{
						put("isoClearingActionJobs",clearingActionJobsImpl);
						put("posClearingActionJobs", posClearingActionJobs);
						put("neginClearingActionJobs", neginClearingActionJobs);
						put("apacs70ClearingActionJobs", apacs70ClearingActionJobs);
						put("infotechClearingActionJobs", infotechClearingActionJobs);
					}};
	}
	
	public ClearingActionMapper getClearingActionMapper(String name){
		Map<String, ClearingActionMapper> map = (Map<String, ClearingActionMapper>) variables.get(CLEARING_ACTION_MAPPER);
		if (map!= null)
			return map.get(name);
		return null;
	}
	
	public ClearingActionJobs getClearingActionJobs(String name){
		Map<String, Map<ClearingAction, ClearingJob>> map = (Map<String, Map<ClearingAction, ClearingJob>>) variables.get(CLEARING_ACTION_JOB);
		if (map != null) {
			//logger.info("" + ((ClearingActionJobs)map.get(name)) +  "]"); //Raza TEMP
			return (ClearingActionJobs) map.get(name);
		}
		return null;
	}
	
	public void shutdown() {
	}


	public Map<String, EncodingConvertor> getAllConvertors(){
		return (Map<String, EncodingConvertor>) variables.get(CONVERTOR_KEY);
	}
	
	public void setVariable(String key, Object value) {
		variables.put(key, value);
	}

	public Object getVariable(String key) {
		return variables.get(key);
	}

	public Collection<Object> getVariables() {
		return variables.values();
	}

	@SuppressWarnings("unchecked")
	public Map<String, RoutingTable> getAllRoutingTables() {
		return (HashMap<String, RoutingTable>) variables.get(ROUTING_TABLES_KEY);
	}

	public void setAllRoutingTables() {
		Map<String, RoutingTable> routTables = new HashMap<String, RoutingTable>();
		
		List<RoutingTable> tables = Routing.initiate();
		for(RoutingTable routingTable:tables){
			routTables.put(routingTable.getName(), routingTable);
		}
		
		variables.put(ROUTING_TABLES_KEY, routTables);
	}
	
	@SuppressWarnings("unchecked")
	public void addRoutingTable(RoutingTable routingTable) {
		if (variables.get(ROUTING_TABLES_KEY) == null)
			variables.put(ROUTING_TABLES_KEY, new HashMap<String, RoutingTable>());

		((HashMap<String, RoutingTable>) variables.get(ROUTING_TABLES_KEY)).put(routingTable.getName(), routingTable);
	}
	
	public void setAllClearingProfile(){
		String query = "from "+ ClearingProfile.class.getName();
		List<ClearingProfile> list = GeneralDao.Instance.find(query);
		Map<Long, ClearingProfile> clearingProf = new HashMap<Long, ClearingProfile>();
		for (ClearingProfile i : list) {
			clearingProf.put(i.getId(), i);
			for (SettlementDataCriteria criteria : i.getCriterias()) {
				for (CriteriaData cd:criteria.getCriteriaDatas()){
					cd.getCriteriaValue();					
				}
			}
		}
		variables.put(CLEARING_PROFILE_KEY, clearingProf);
	}
	
	public void setEncodings(ATMConfiguration atmConfiguration){   	
    	Map<String,String> encod = new HashMap<String, String>();
    	encod.put(NDCConvertor.FARSI_RECIEPT_ENCODING, atmConfiguration.getFarsi_reciept_encoding());
    	encod.put(NDCConvertor.FARSI_EXTENDED_RECIEPT_ENCODING, atmConfiguration.getFarsi_extended_reciept_encoding());
    	encod.put(NDCConvertor.FARSI_SCREEN_ENCODING, atmConfiguration.getFarsi_screen_encoding());
    	encod.put(NDCConvertor.FARSI_EXTENDED_SCREEN_ENCODING, atmConfiguration.getFarsi_extended_screen_encoding());
    	encod.put(NDCConvertor.ENGLISH_ENCODING, atmConfiguration.getEnglish_encoding());
    	
    	atmConfiguration.setEncodingMap(encod);
    }
	
	public void setNDCConvertor(ATMConfiguration atmConfiguration){
		Map<String, String> convertors = new HashMap<String, String>();
		convertors.put(NDCConvertor.RECEIPT_CONVERTOR, atmConfiguration.getReceiptConvertor());
		convertors.put(NDCConvertor.SCREEN_CONVERTOR, atmConfiguration.getScreenConvertor());
		
		atmConfiguration.setConvertorsMap(convertors);
	}
	
	public Map<Long, ClearingProfile> getAllClearingProfile(){
		return (Map<Long,ClearingProfile>) variables.get(CLEARING_PROFILE_KEY);
	}
//
//	@SuppressWarnings("unchecked")
//	public RoutingTable getRoutingTable(String routingTableName) {
//		return ((HashMap<String, RoutingTable>) variables.get(ROUTING_TABLES_KEY)).get(routingTableName);
//	}
	
	public void setAllLotteryAssignmentPolicy(){
		String query = "from "+ LotteryAssignmentPolicy.class.getName()+" l left join fetch l.criterias ";
		List<LotteryAssignmentPolicy> list = GeneralDao.Instance.find(query);
		Map<Integer, LotteryAssignmentPolicy> lotteryAssign = new HashMap<Integer, LotteryAssignmentPolicy>();
		for(LotteryAssignmentPolicy i :list){
			lotteryAssign.put(i.getId(), i);
		}
		variables.put(LOTTERY_ASSIGNMENT_POLICY_KEY, lotteryAssign);
	}
	
	public Map<Integer, LotteryAssignmentPolicy> getAllLotteryAssignmentPolicy(){
		return(Map<Integer,LotteryAssignmentPolicy>)variables.get(LOTTERY_ASSIGNMENT_POLICY_KEY);
	} 

	
	@SuppressWarnings("unchecked")
	public Map<String, Channel> getAllChannels() {
		return (HashMap<String, Channel>) variables.get(CHANNELS_KEY);
	}

	public void setAllChannels() throws Exception {
		Map<String, Channel> channels = new HashMap<String, Channel>();
		Map<String, Channel> channelsid = new HashMap<String, Channel>(); //Raza adding for WebService
		Map<String, Object> dbParam = new HashMap<String, Object>();
		String dbQuery;
		
		List<Channel> channelList = null;
		try {
			//channelList = ChannelManager.getInstance().loadInDB(); //Raza commenting Channels moved to DB
			channelList = ChannelManager.getInstance().loadFromDB();
		} catch (Exception e) {
			logger.debug(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
		}
		
		ProtocolProvider.Instance.init();
		
		if (channelList != null) {
			for (Channel channel : channelList) {
				channels.put(channel.getName(), channel); //Raza commenting
				channelsid.put(channel.getChannelId(), channel); //Raza adding for WebService

				//channels.put(channel.getChannelId(), channel); //Raza not using Channel ID as it is required to be unique for the key value pair
				if (!channel.getChannelType().equals(ChannelType.WEBSERVER.toString())) //Raza do not do this for WebServer
				{
					String protocolGenericName = channel.getProtocolGenericName();
					String protocolClass = channel.getProtocolClass();
					if (protocolGenericName != null && protocolClass != null) {
						ProtocolProvider.Instance.addProtocol(protocolGenericName, channel.getProtocolName(), channel.getProtocolClass());
					}
				}
			}
		}

		variables.put(CHANNELS_KEY, channels);
		variables.put(CHANNELSID_KEY, channelsid);
	}

	@SuppressWarnings("unchecked")
	public void addChannel(Channel channel) {
		if (variables.get(CHANNELS_KEY) == null)
			variables.put(CHANNELS_KEY, new HashMap<String, Channel>());

		((HashMap<String, Channel>) variables.get(CHANNELS_KEY)).put(channel.getName(), channel);
	}

	@SuppressWarnings("unchecked") //Raza adding for NayaPay GetChannel by ChannelId
	public Channel getChannelbyId(String channelId) {
		return ((HashMap<String, Channel>) variables.get(CHANNELSID_KEY)).get(channelId);
	} //Raza adding for NayaPay GetChannel by ChannelId

	@SuppressWarnings("unchecked")
	public Channel getChannel(String channelName) {
		return ((HashMap<String, Channel>) variables.get(CHANNELS_KEY)).get(channelName);
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Currency> getAllCurrencies() {
		return (HashMap<Integer, Currency>) variables.get(CURRENCIES_KEY);
	}

	public void setAllCurrencies() {
		Map<Integer, Currency> currencies = new HashMap<Integer, Currency>();
		Map<String, Currency> swiftcurrencies = new HashMap<String, Currency>();
		
		String query = "from " + Currency.class.getName() + " c ";
		Map<String, Object> param = new HashMap<String, Object>();
		List<Currency> list = GeneralDao.Instance.find(query, param);
		for (Currency currency : list) {
			if (currency.getIsBase())
				variables.put(BASE_CURRENCY_KEY, currency);
			currencies.put(currency.getCode(), currency);
			swiftcurrencies.put(currency.getName(),currency);
		}
		variables.put(CURRENCIES_KEY, currencies);
		variables.put(SWIFTCURRENCIES_KEY, swiftcurrencies);
	}

	@SuppressWarnings("unchecked")
	public Currency getCurrency(Integer code) {
		return ((HashMap<Integer, Currency>) variables.get(CURRENCIES_KEY)).get(code);
	}

	@SuppressWarnings("unchecked")
	private List<Currency> findAllCurrency() {
		String query = "from " + Currency.class.getName() + " c ";
		Map<String, Object> param = new HashMap<String, Object>();
		return GeneralDao.Instance.find(query, param);
	}

	//Raza adding for NayaPay start
	public Currency getCurrencybySwiftCode(String swiftcode){
		return ((HashMap<Integer, Currency>) variables.get(SWIFTCURRENCIES_KEY)).get(swiftcode);
	}
	//Raza adding for NayaPay end

	public Institution getMyInstitution() { //Raza commenting for MultiInstitution
		return (Institution) variables.get(MY_INSTITUTION_KEY);
	}
	
	public Map<Long,Institution> getMyInstitutions() { //Raza Adding for MultiInstitution
		return (Map<Long,Institution>) variables.get(MY_INSTITUTIONS_KEY);
	}

	public List<Institution> getPeerInstitutions(){
		return (List<Institution>) variables.get(PEER_INSTITUTION_KEY);
	}
	
	public List<Long> getPeerInstitutionsBin(){
		return (List<Long>) variables.get(PEER_INSTITUTION_BIN_KEY);
	}
	

	public void setAllInstitutions() {
		Institution myself = null;
		Map<Long, Institution> myInstitutions = new HashMap<Long, Institution>();
		List<Institution> peers = new ArrayList<Institution>();
		List<Long> peerBins = new ArrayList<Long>();

		String query = "from Institution i left join fetch i.currentWorkingDay left join fetch i.lastWorkingDay left join fetch i.parentGroup left join fetch i.sharedFeature left join fetch i.parentGroup.sharedFeature";
		List<Institution> list = GeneralDao.Instance.find(query);
		Map<String, Institution> institutions = new HashMap<String, Institution>();
		Map<Long, SwitchTerminal> switchterminals = new HashMap<Long, SwitchTerminal>();

		for (Institution i : list) {
			String terminal_query = "from SwitchTerminal a left join fetch a.keySet b left join fetch a.sharedFeature left join fetch a.parentGroup left join fetch a.parentGroup.sharedFeature where a.owner = :institutionId";
			//String terminal_query = "from SwitchTerminal a left join fetch a.keySet b left join fetch a.sharedFeature left join fetch a.parentGroup left join fetch a.parentGroup.sharedFeature where a.owner = :institutionId and b.isActive = 1";
			Map<String, Object> param = new HashMap<String, Object>(1);
			param.put("institutionId", i);
			List<SwitchTerminal> terminals = GeneralDao.Instance.find(terminal_query, param);

			if (i.getLastWorkingDay() != null) {
				i.getLastWorkingDay().getDate();
				i.getLastWorkingDay().getRecievedDate();
			}

			if (i.getCurrentWorkingDay() != null) {
				i.getCurrentWorkingDay().getDate();
				i.getCurrentWorkingDay().getRecievedDate();
			}

			for (SwitchTerminal t : terminals) {
				i.addTerminal(t);
				if (t.getChargePolicy() != null)
					t.getChargePolicy().getPolicyData();
				t.getOwner().getId();
				if (t.getOwner().getLastWorkingDay() != null) {
					t.getOwner().getLastWorkingDay().getDate();
				}
				if (t.getOwner().getCurrentWorkingDay() != null) {
					t.getOwner().getCurrentWorkingDay().getDate();
				}
				if (t.getOwner().getParentGroup() != null) {
					t.getOwner().getParentGroup().getName();
					if (t.getOwner().getParentGroup().getSharedFeature() != null) {
						t.getOwner().getParentGroup().getSharedFeature().getAuthorizationProfileId();
					}
				}
				if (t.getOwner().getSharedFeature() != null) {
					t.getOwner().getSharedFeature().getAuthorizationProfileId();
				}
				switchterminals.put(t.getCode(), t);
			}

			institutions.put(i.getCode()+"", i);

			//Raza TEMP start
			//for(int index=0 ; index<list.size() ; index++)
			//{
			//	System.out.println("All Institutions [" + list.get(index) + "], Type [" + list.get(index).getInstitutionType() + "]");
			//}
			//Raza TEMP end
			//System.out.println("This Institutions [" + i.getCode() + "], Type [" + i.getInstitutionType() + "]");
			if (FinancialEntityRole.MY_SELF.equals(i.getInstitutionType()) ||
					FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(i.getInstitutionType())) {
				//System.out.println("Is MySelf or Is MySelfIntermediate"); //Raza TEMP
				myself = i;
				if (FinancialEntityRole.MY_SELF.equals(i.getInstitutionType())) {
					//System.out.println("Is MySelf Only"); //Raza TEMP
					myInstitutions.put(i.getBin(), i);
				} else if (FinancialEntityRole.PEER.equals(i.getInstitutionType())) {
					peers.add(i);
					peerBins.add(i.getBin());
				}
			}

			//Raza removing from inside the loop. Add all first then Check
			/*if (myInstitutions == null || myInstitutions.isEmpty()) { //if (myself == null){
				logger.error("Could not find my institution...");
				throw new SwitchRuntimeException("Could not find my institution...");
			}

			if (peers == null || peers.isEmpty()) {
				logger.error("Could not find peer institution...");
			}

			variables.put(PEER_INSTITUTION_KEY, peers);
			variables.put(PEER_INSTITUTION_BIN_KEY, peerBins);

			variables.put(MY_INSTITUTION_KEY, myself);
			variables.put(MY_INSTITUTIONS_KEY, myInstitutions);

			variables.put(INSTITUTIONS_KEY, institutions);
			variables.put(SWITCH_TERMINAL_KEY, switchterminals);*/
		}
		if (myInstitutions == null || myInstitutions.isEmpty()) { //if (myself == null){
			logger.error("Could not find my institution...");
			throw new SwitchRuntimeException("Could not find my institution...");
		}

		if (myself == null){ //Raza also check myself for Old Cold.
			logger.error("Could not find my institution...");
			throw new SwitchRuntimeException("Could not find my institution...");
		}

		if (peers == null || peers.isEmpty()) {
			logger.error("Could not find peer institution...");
		}

		variables.put(PEER_INSTITUTION_KEY, peers);
		variables.put(PEER_INSTITUTION_BIN_KEY, peerBins);

		variables.put(MY_INSTITUTION_KEY, myself);
		variables.put(MY_INSTITUTIONS_KEY, myInstitutions);

		variables.put(INSTITUTIONS_KEY, institutions);
		variables.put(SWITCH_TERMINAL_KEY, switchterminals);

	}

	public void setAllBanks() {
		String query = "from "+ Bank.class.getName();
		List<Bank> list = GeneralDao.Instance.find(query);
		Map<Integer, Bank> banks = new HashMap<Integer, Bank>();
		Map<String, Bank> bankcodes = new HashMap<String, Bank>();
		Map<String, String> bankCodetoBIN = new HashMap<String, String>();
		Map<String, String> bintoBankCode = new HashMap<String, String>();
		for (Bank i : list) {
			banks.put(i.getBin(), i);
			bankcodes.put(i.getBankcode(),i); //Raza for NayaPay
			//m.rehman: from bankcode to bin relationship
			bintoBankCode.put(i.getBin().toString(),i.getBankcode()); //Raza adding for LinkedAccount List of NayaPay
			bankCodetoBIN.put(i.getBankcode(),i.getBin().toString());

		}
		variables.put(BANKS_KEY, banks);
		variables.put(NAYAPAYBANKS_KEY, bankcodes); //Raza for NayaPay
		//m.rehman: for NayaPay, from bankcode to bin relationship
		variables.put(BANKCODE_TO_BIN_KEY, bankCodetoBIN);
		variables.put(BIN_TO_BANKCODE_KEY, bintoBankCode);
	}

	//Raza adding for NayaPay start
	public Bank getBankbyCode(String bankcode){
		return ((HashMap<String, Bank>) variables.get(NAYAPAYBANKS_KEY)).get(bankcode);
	}

	//m.rehman: for NayaPay, get bin from bankcode
	public String getBinByBankCode(String bankcode) {
		return ((HashMap<String, String>) variables.get(BANKCODE_TO_BIN_KEY)).get(bankcode);
	}

	public String getBankCodeByBin(String bin) { //Raza adding for LinkedAccount List of NayaPay
		return ((HashMap<String, String>) variables.get(BIN_TO_BANKCODE_KEY)).get(bin);
	}

	public void setAllStates()
	{
		String query = "from "+ State.class.getName();
		List<State> list = GeneralDao.Instance.find(query);
		Map<String, State> states = new HashMap<String, State>();
		for (State i : list) {
			states.put(i.getName(),i);
		}
		variables.put(STATES_KEY, states);
	}

	public State getState(String state){
		return ((HashMap<String, State>) variables.get(STATES_KEY)).get(state);
	}

	public void setAllCities()
	{
		String query = "from "+ City.class.getName();
		List<City> list = GeneralDao.Instance.find(query);
		Map<String, City> cities = new HashMap<String, City>();
		for (City i : list) {
			cities.put(i.getName(),i);
		}
		variables.put(CITIES_KEY, cities);
	}

	public City getCity(String city){
		return ((HashMap<String, City>) variables.get(CITIES_KEY)).get(city);
	}
	//Raza adding for NayaPay end
	

	public Map<Long, Map<Long, Long>> getAllCellPhoneChargeSpecification(){
		return (Map<Long, Map<Long, Long>>) variables.get(MTN_CHARGE_KEY);
	}
	
	public void setAllCellPhoneChargeSpecification() {
		String query = "from MTNChargeSpecification";
		List<MTNChargeSpecification> list = GeneralDao.Instance.find(query);
		Map<Long, Map<Long, Long>> mtnChargeSpecs = new HashMap<Long, Map<Long, Long>>();

		Long newCompany = -1L;
		Map<Long, Long> map = null;
		for (MTNChargeSpecification mtn : list) {
			if (!newCompany.equals(mtn.getCompany().getCode())) {
				newCompany = mtn.getCompany().getCode();
				map = new HashMap<Long, Long>();
			}
			map.put(mtn.getCredit(), mtn.getTax());
			mtnChargeSpecs.put(mtn.getCompany().getCode(), map);
		}

		variables.put(MTN_CHARGE_KEY, mtnChargeSpecs);
	}
	@SuppressWarnings("unchecked")
	public Map<String, Institution> getAllInstitutions() {
		return (Map<String, Institution>) variables.get(INSTITUTIONS_KEY);
	}

	public Map<Long, SwitchTerminal> getAllSwitchTerminals() {
		return (Map<Long, SwitchTerminal>) variables.get(SWITCH_TERMINAL_KEY);
	}

	public void setAllSecurityFunctions() {
		String query = "from SecurityProfile";
		List<SecurityProfile> list = GeneralDao.Instance.find(query);
		Map<Long, Map<String, SecurityFunction>> profiles = new HashMap<Long, Map<String, SecurityFunction>>();

		for (SecurityProfile p : list) {
			String func_query = "from SecurityFunction a where a.securityProfile.id = :profileId ";
			Map<String, Object> param = new HashMap<String, Object>(1);
			param.put("profileId", p.getId());
			List<SecurityFunction> funcs = GeneralDao.Instance.find(func_query, param);
			Map<String, SecurityFunction> func_map = new HashMap<String, SecurityFunction>();
			for (SecurityFunction t : funcs) {
				func_map.put(t.getName(), t);
			}

//			i.getTerminals();
			profiles.put(p.getId(), func_map);
		}

		variables.put(SECURITY_FUNCTIONS_KEY, profiles);
	}

	@SuppressWarnings("unchecked")
	public Map<Long, Map<String, SecurityFunction>> getAllSecurityFunctions() {
		return (Map<Long, Map<String, SecurityFunction>>) variables.get(SECURITY_FUNCTIONS_KEY);
	}

	
	@SuppressWarnings("unchecked")
	public void setAllFeeProfiles() {
//		String query = "from FeeProfile";
		
		//Mirkamali(Task130)
		String query = "from FeeProfile f where f.enabled = true";
		
		List<FeeProfile> list = GeneralDao.Instance.find(query);
		Map<Long, List<BaseFee>> profiles = new HashMap<Long, List<BaseFee>>();

		for (FeeProfile p : list) {
			String fee_query = "select distinct i from TransactionFee i left join fetch i.feeItemList where i.owner.id = :feeProfileId";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("feeProfileId", p.getId());
			List<BaseFee> fees = GeneralDao.Instance.find(fee_query, params);
			for (BaseFee baseFee: fees) {
				List<AllowedCard> cards = baseFee.getCards();
				if (cards != null) {
					cards.size();
					for(AllowedCard ac:cards){
						if(ac.getBank() != null){
							ac.getBank().getBin();
						}
					}
				}	
			}
			profiles.put(p.getId(), fees);
		}
		variables.put(FEE_PROF_KEY, profiles);
	}
	public Map<Long, List<BaseFee>> getAllFeeProfiles(){
		return (Map<Long, List<BaseFee>>) variables.get(FEE_PROF_KEY);
	}
	
	@SuppressWarnings("unchecked")
	public void setAllDiscountProfiles() {
//		String query = "from DiscountProfile";
		//Mirkamali(Task130)
		String query = "from DiscountProfile d where d.enabled = true";
		
		List<DiscountProfile> list = GeneralDao.Instance.find(query);
		Map<Long, Pair<DiscountProfile, List<BaseDiscount>>> profiles = new HashMap<Long, Pair<DiscountProfile,List<BaseDiscount>>>();
		
		for (DiscountProfile p : list) {
			String dis_query = "select distinct i from BaseDiscount i where i.owner.id = :disProfileId ";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("disProfileId", p.getId());
			List<BaseDiscount> discounts = GeneralDao.Instance.find(dis_query, params);
			for (BaseDiscount baseDis: discounts) {
				List<AllowedCard> cards = baseDis.getCards();
				if (cards != null) {
					cards.size();
					for (AllowedCard ac: cards) {
						if(ac.getBank() != null){
							ac.getBank().getBin();
						}
					}
				}
			}
			profiles.put(p.getId(), new Pair<DiscountProfile, List<BaseDiscount>>(p, discounts));
		}
		
		variables.put(DIS_PROF_KEY, profiles);
	}
	public Map<Long, Pair<DiscountProfile, List<BaseDiscount>>> getAllDiscountProfiles(){
		return (Map<Long, Pair<DiscountProfile, List<BaseDiscount>>>) variables.get(DIS_PROF_KEY);
	}

	public void setAllAuthorizationProfiles() {
		String query = "from AuthorizationProfile";
		List<AuthorizationProfile> list = GeneralDao.Instance.find(query);
		
		Map<Long, Pair<AuthorizationProfile, List<Policy>>> profiles = new HashMap<Long, Pair<AuthorizationProfile,List<Policy>>>();
//		Map<Long, List<Policy>> profiles = new HashMap<Long, List<Policy>>();
		
		for(AuthorizationProfile auth:list){
			String policy_query = "select distinct a.policies from AuthorizationProfile a where a.id = :auth";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("auth", auth.getId());
			List<Policy> policies = GeneralDao.Instance.find(policy_query, params);
			for(Policy p:policies){
				if(p instanceof FITControlPolicy){
					p = (FITControlPolicy) GeneralDao.Instance.findUnique("from FITControlPolicy a inner join fetch a.banks where a.id = "+p.getId());
				}else if(p instanceof CardGroupRestrictionPolicy){
					p = (CardGroupRestrictionPolicy) GeneralDao.Instance.findUnique("from CardGroupRestrictionPolicy a inner join fetch a.cards where a.id = "+p.getId());
					if (p != null) {
						for (AllowedCard card: ((CardGroupRestrictionPolicy) p).getCards()) {
							if(card != null) {
								card.getId();
							}
						}
					}
				}else if(p instanceof PanPrefixTransactionPolicy){
					p = (PanPrefixTransactionPolicy) GeneralDao.Instance.findUnique("from PanPrefixTransactionPolicy a inner join fetch a.transactions where a.id = "+p.getId());
					if (p != null) {
						for (AllowedTranaction item: ((PanPrefixTransactionPolicy) p).getTransactions()) {
							if (item != null) {
								if (item.getBank() != null) {
									item.getBank().getBin();
								}
							}
						}
					}
				}else if(p instanceof TerminalServicePolicy){
					p = (TerminalServicePolicy) GeneralDao.Instance.findUnique("from TerminalServicePolicy a inner join fetch a.trmAlwdTypes where a.id = "+p.getId());
				}else if(p instanceof TrxTypePanPrefixPolicy){
					p = (TrxTypePanPrefixPolicy) GeneralDao.Instance.findUnique("from TrxTypePanPrefixPolicy a inner join fetch a.alowedList where a.id = "+p.getId());
					if (p != null) {
						for (AllowedTranactionTypeBanks item : ((TrxTypePanPrefixPolicy) p).getAlowedList()) {
							if (item != null)
								item.getBanks().size();
						}
					}
				}
			}
			profiles.put(auth.getId(), new Pair<AuthorizationProfile, List<Policy>>(auth, policies));
		}

		variables.put(AUTH_PROF_KEY, profiles);
	}
	
	public Map<Long, Pair<AuthorizationProfile, List<Policy>>> getAllAuthorizationProfiles(){
		return (Map<Long, Pair<AuthorizationProfile, List<Policy>>>) variables.get(AUTH_PROF_KEY);
	}

	public void setGeneralChargePolicy(){
		String query = "from "+ GeneralChargeAssignmentPolicy.class.getSimpleName();
		GeneralChargeAssignmentPolicy policy = (GeneralChargeAssignmentPolicy) GeneralDao.Instance.findObject(query, null);
		variables.put(GENERAL_CHARGE_POLICY, policy);
	}
	
	public GeneralChargeAssignmentPolicy getGeneralChargePolicy(){
		return (GeneralChargeAssignmentPolicy) variables.get(GENERAL_CHARGE_POLICY);
	}
	
	public double generateRandomDouble() {
		return secureRandom.nextDouble();
	}

	public Map<Integer, Bank> getAllBanks() {
		return (Map<Integer, Bank>) variables.get(BANKS_KEY);
	}
	
	public String getCardProductType(int productCode){
		switch(productCode){
		case 1: 
		case 2: 
			return "نقدی";
		case 3: 
		case 4: 
			return "بن/هدیه";
		case 6: 
			return "اعتباری";
		}
		return "";
	}
	
	private Script getGroovyScript(String groovyStr){
		if(!groovyScript.containsKey(groovyStr)){
			Script script = groovyShell.parse(groovyStr);
			groovyScript.put(groovyStr, script);
			return script;
		}else{
			return groovyScript.get(groovyStr);
		}
	}
	
	// Thread-safe groovy script executer
	public Object evaluateScript(String script, Binding binding) {
		Script groovyScript = getGroovyScript(script);
		synchronized(groovyScript) {
			groovyScript.setBinding(binding);
			return groovyScript.run();
		}		
	}

	public void setAllProtocolConfig () {
		Map<ProtocolType, Map<String, String>> protocolConfig = new HashMap<ProtocolType, Map<String,String>>();
		
		String query = "from ProtocolConfig pc order by pc.protocolType";
		List<ProtocolConfig> list = GeneralDao.Instance.find(query);
		for(ProtocolConfig pc : list) {
			Map<String, String> pcMap;
			if(protocolConfig.containsKey(pc.getProtocolType()))
				pcMap = protocolConfig.get(pc.getProtocolType());
			else
				pcMap = new HashMap<String, String>();
			pcMap.put(pc.getKey(), pc.getValue());
			protocolConfig.put(pc.getProtocolType(), pcMap);
		}
		
		variables.put(PROTOCOL_CONFIG, protocolConfig);
	}
	
	public Map<ProtocolType, Map<String, String>> getAllProtocolConfig () {
		return (Map<ProtocolType, Map<String, String>>) variables.get(PROTOCOL_CONFIG);
	}
	public void setAllATMConfigurations() {
		String query = "from ATMConfiguration a " +
				"left join fetch a.receiptHeader " +
				"left join fetch a.receiptFooter ";
		
		//MIRKAMALI(Task130)
		query += " where a.enabled = true";
		
		List<ATMConfiguration> list = GeneralDao.Instance.find(query);
		
		String st="";
		
		Map<Long, ATMConfiguration> atmConfig = new HashMap<Long, ATMConfiguration>();
		Map<Long, Map<String, ATMRequest>> atmRequest = new HashMap<Long, Map<String,ATMRequest>>();
//		Map<Receipt,String> atmReceipt =  new HashMap<Receipt, String>();
		
		for(ATMConfiguration a:list){
			
			//Mirkamali(Task179): Currency ATM
			if(Boolean.TRUE.equals(a.getIsCurrencyConfig())) {
				
			}else {
				a.getCassetteACurrency().getCode();
				a.getCassetteBCurrency().getCode();
				a.getCassetteCCurrency().getCode();
				a.getCassetteDCurrency().getCode();
			}

			for(RsCodeResponses rsCodeResponses:a.getFitResponses().values()){
				for(ATMResponse response:rsCodeResponses.getRsCodeResponses().values()){
					for(ResponseScreen respScreen:response.getScreen()){
						respScreen.getScreenData();
						respScreen.setReplacedScreenData(replace(respScreen.getScreenData(), a));
					}
					if(response instanceof FunctionCommandResponse){
						for(Receipt reciept:((FunctionCommandResponse) response).getReceipt()){
							reciept.setReplacedText(replace(reciept.getText(),a));
						}
						if(((FunctionCommandResponse) response).getDispense() != null){
							((FunctionCommandResponse) response).getDispense().getCassette1();
						}
					}
				}
			}
			for(ATMRequest request:a.getRequests()){
				if(request.getCurrency() != null)
					request.getCurrency().getCode();
				
				Map<String, ATMRequest> requests = atmRequest.get(a.getId());
				if (requests == null)
					requests = new HashMap<String, ATMRequest>();
				requests.put(request.getOpkey(), request);
				
				atmRequest.put(a.getId(), requests);
				
				for(ATMResponse response:request.getResponseMap().values()){
					for(ResponseScreen respScreen:response.getScreen()){
						respScreen.getScreenData();
						respScreen.setReplacedScreenData(replace(respScreen.getScreenData(), a));
					}
					if(response instanceof FunctionCommandResponse){
						for(Receipt reciept:((FunctionCommandResponse) response).getReceipt()){
							reciept.setReplacedText(replace(reciept.getText(), a));
						}
						if(((FunctionCommandResponse) response).getDispense() != null){
							((FunctionCommandResponse) response).getDispense().getCassette1();
						}
					}					
				}
			}
					
			for(StateData stateData:a.getStates()){
				stateData.getNumber();
			}

			for(ScreenData stateData:a.getScreens()){
				stateData.getNumber();
			}

			for(FITData stateData:a.getFits()){
				stateData.getNumber();
			}

			for(EnhancedParameterData stateData:a.getParams()){
				stateData.getNumber();
			}

			for(TimerData stateData:a.getTimers()){
				stateData.getNumber();
			}

			atmConfig.put(a.getId(), a);
			setEncodings(a);
			setNDCConvertor(a);
			
		}
		variables.put(ATM_CONFIG_KEY, atmConfig);
		variables.put(ATM_REQUEST_KEY, atmRequest);
	}
	
	
	public Map<Long, ATMConfiguration> getAllATMConfigurations() {
		return (Map<Long, ATMConfiguration>) variables.get(ATM_CONFIG_KEY);
	}
	
	public Map<Long, Map<String, ATMRequest>> getAllATMRequests() {
		return (Map<Long, Map<String, ATMRequest>>) variables.get(ATM_REQUEST_KEY);
	}

	public void putInstitution(Institution institution) {
		Map<Long, Institution> institutions = (Map<Long, Institution>) variables.get(INSTITUTIONS_KEY);
		institutions.put(institution.getCode(), institution);
		variables.put(INSTITUTIONS_KEY, institutions);
	}

	public void setTransactionCodes() { //Raza API Reversal 25-06-2019
		String query = "from "+ SwitchTransactionCodes.class.getName();
		List<SwitchTransactionCodes> list = GeneralDao.Instance.find(query);
		Map<String, SwitchTransactionCodes> mapper = new HashMap<String, SwitchTransactionCodes>();
		for (SwitchTransactionCodes i : list) {
			mapper.put(i.getTxncode(),i);
			mapper.put(i.getServicename(),i);
		}
		variables.put(TRANSACTIONCODES_KEY, mapper);
	}
	
	public void putApplicationName(){
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()){
			if (ste.toString().contains("main")){
				logger.debug("Application "+ ste.getClassName()+" is put in GlobalContext.");
				variables.put(APPLICATION_NAME_KEY, ste.getClassName());
				break;
			}
		}
	}
	
	public String getApplicationName(){
		return (String) variables.get(APPLICATION_NAME_KEY);
	}
	
	public String replace (String groovyStr, ATMConfiguration atmConfiguration){
		
		if(groovyStr != null && groovyStr.contains("GR")){
			if(groovyStr.startsWith("388"))
			{
				System.out.println(groovyStr);
			}
			if (groovyStr.contains("dateFormat(") && !groovyStr.contains("convertor.dateFormat(")){
				groovyStr = groovyStr.replace("dateFormat(", "convertor.dateFormat(");
			}
			
			if (groovyStr.contains("appPanFa(")){
				groovyStr = groovyStr.replace("appPanFa(", "convertor.appPanFormatFa(encodings, atm" +  ", ");
			}
			
			if (groovyStr.contains("appPanEn(")){
				groovyStr = groovyStr.replace("appPanEn(", "convertor.appPanFormatEn(");
			}

			if (groovyStr.contains("appPanNCREn(")){
				groovyStr = groovyStr.replace("appPanNCREn(", "convertor.appPanFormatEnForNCR(");
			}
			
			if (groovyStr.contains("printAppPan") && !groovyStr.contains("convertor.printAppPan(")){
				groovyStr = groovyStr.replace("printAppPan(", "convertor.printAppPan(");
			}
			
			if (groovyStr.contains("trimLeftZeros(") && !groovyStr.contains("convertor.trimLeftZeros(")){
				groovyStr = groovyStr.replace("trimLeftZeros(", "convertor.trimLeftZeros(");
			}
			if (groovyStr.contains("format(") && !groovyStr.contains("convertor.format(")){
				groovyStr = groovyStr.replace("format(", "convertor.format(");
			}
			if (groovyStr.contains("realChargeCredit2F(")){
				groovyStr = groovyStr.replace("realChargeCredit2F(", "convertor.realChargeCredit('"+ NDCConvertor.Language.FarsiLanguage+"', '"+atmConfiguration.getFarsi_reciept_encoding()+"', encodings, atm,");
			}
			if (groovyStr.contains("realChargeCredit2E(")){
				groovyStr = groovyStr.replace("realChargeCredit2E(", "convertor.realChargeCredit('"+NDCConvertor.Language.EnglishLanguage+"', '"+atmConfiguration.getEnglish_encoding()+"', encodings, atm,");
			}
			if (groovyStr.contains("decode(") && !groovyStr.contains("convertor.decode(")){
				groovyStr = groovyStr.replace("decode(", "convertor.decode(");
			}
			if (groovyStr.contains("amount2F(")) {
				groovyStr = groovyStr.replace("amount2F(", "convertor.accBalAvailable('"+NDCConvertor.Language.FarsiLanguage+"', '"+atmConfiguration.getFarsi_reciept_encoding()+"', encodings, atm,");
			}
			if (groovyStr.contains("stsF(") && !groovyStr.contains("convertor.bankStatementScreenTableFa(")) {//gholami statment on screen
				groovyStr = groovyStr.replace("stsF(ifx", "convertor.bankStatementScreenTableFa(ifx");
			}
			if (groovyStr.contains("stsE(") && !groovyStr.contains("convertor.bankStatementScreenTableEn(")) {//gholami statment on screen
				groovyStr = groovyStr.replace("stsE(ifx", "convertor.bankStatementScreenTableEn(ifx");
			}
			if (groovyStr.contains("amount2Fscr(")) {
				groovyStr = groovyStr.replace("amount2Fscr(", "convertor.accBalAvailable('"+NDCConvertor.Language.FarsiLanguage+"', '"+atmConfiguration.getFarsi_screen_encoding()+"', encodings, atm,");
			}
			if (groovyStr.contains("amount2E(")) {
				groovyStr = groovyStr.replace("amount2E(", "convertor.accBalAvailable('"+NDCConvertor.Language.EnglishLanguage+"', '"+atmConfiguration.getEnglish_encoding()+"', encodings, atm,");
			}
			//Mirkamali(Task179): Currency ATM
			if (groovyStr.contains("amount2ECurrency(")) {
				groovyStr = groovyStr.replace("amount2ECurrency(", "convertor.accBalAvailableCurrency('"+NDCConvertor.Language.EnglishLanguage+"', '"+atmConfiguration.getEnglish_encoding()+"', encodings, atm,");
			}
			if (groovyStr.contains("amount2Escr(")) {
				groovyStr = groovyStr.replace("amount2Escr(", "convertor.accBalAvailable('"+NDCConvertor.Language.EnglishLanguage+"', '"+atmConfiguration.getEnglish_encoding()+"', encodings, atm,");
			}
			if (groovyStr.contains("partialDispense(") && !groovyStr.contains("convertor.partialDispense(atm,")) {
				groovyStr = groovyStr.replace("partialDispense(", "convertor.partialDispense(atm,");
			}
			if (groovyStr.contains("c2F(")){
				groovyStr = groovyStr.replace("c2F(", "convertor.convert2Farsi(");
			}
			if (groovyStr.contains("c2E(")){
				groovyStr = groovyStr.replace("c2E(", "convertor.convertToEnglish(");
			}
			if (groovyStr.contains("c2NCRE(")){
				groovyStr = groovyStr.replace("c2NCRE(", "convertor.convertToEnglishForNCR(");
			}
			if (groovyStr.contains("c2NCR_SCR_E(")){
				groovyStr = groovyStr.replace("c2NCR_SCR_E(", "convertor.convertToEnglishForNCRScreen(");
			}
			
			if (groovyStr.contains("putLF(") && !groovyStr.contains("convertor.putLF(")){
				groovyStr = groovyStr.replace("putLF(", "convertor.putLF(" );
			}
			if (groovyStr.contains("test(") && !groovyStr.contains("convertor.test(")){
				groovyStr = groovyStr.replace("test(", "convertor.test(" );
			}
			
			if (groovyStr.contains("bankStatementTableFa(") && !groovyStr.contains("convertor.bankStatementTableFa(ifx,")){
				groovyStr = groovyStr.replace("bankStatementTableFa(", "convertor.bankStatementTableFa(ifx, " + atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", encodings");
			}
			
			if (groovyStr.contains("bankStatementTableEn(") && !groovyStr.contains("convertor.bankStatementTableEn(ifx,")){
				groovyStr = groovyStr.replace("bankStatementTableEn(", "convertor.bankStatementTableEn(ifx, " + atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() );
			}
			
			if (groovyStr.contains("datePersianFormat(") && !groovyStr.contains("convertor.datePersianFormat(")){
				groovyStr = groovyStr.replace("datePersianFormat(", "convertor.datePersianFormat(" + atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", atm"  + ", ");
			}
			
			if (groovyStr.contains("dateEnglishFormat(") && !groovyStr.contains("convertor.dateEnglishFormat(")){
				groovyStr = groovyStr.replace("dateEnglishFormat(", "convertor.dateEnglishFormat(" + atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", ");
			}
			
			if (groovyStr.contains("dateEnglishNCRFormat(") && !groovyStr.contains("convertor.dateEnglishNCRFormat(")){
				groovyStr = groovyStr.replace("dateEnglishNCRFormat(", "convertor.dateEnglishNCRFormat(" + atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", ");
			}
			
			if (groovyStr.contains("subsidiaryState2F(")) {
				groovyStr = groovyStr.replace("subsidiaryState2F(", "convertor.subsidiaryStatement2Farsi(");
			}
			
			if (groovyStr.contains("subsidiaryState2NCRE(")) {
				groovyStr = groovyStr.replace("subsidiaryState2NCRE(", "convertor.subsidiaryStatement2EnglishForNCR(");
			}
			
			if (groovyStr.contains("accountFormat(") && !groovyStr.contains("convertor.accountFormat(text_dir,")){
				groovyStr = groovyStr.replace("accountFormat(", "convertor.accountFormat(text_dir, ");
			}
			
			if (groovyStr.contains("bnkName2F(")){
				groovyStr = groovyStr.replace("bnkName2F(", "convertor.bnkFarsiName('"+atmConfiguration.getBnkFarsiName()+"'");
			}
			
			if (groovyStr.contains("bnkName2E(")){
				groovyStr = groovyStr.replace("bnkName2E(", "convertor.bnkEnglishName('"+atmConfiguration.getBnkEnglishName()+"'");
			}
			
			if (groovyStr.contains("bnkName2NCRE(")){
				groovyStr = groovyStr.replace("bnkName2NCRE(", "convertor.bnkEnglishNameForNCR('"+atmConfiguration.getBnkEnglishName()+"'");
			}
			
			if (groovyStr.contains("bnkMount2F(")){
				groovyStr = groovyStr.replace("bnkMount2F(", "convertor.bnkFarsiMount('"+atmConfiguration.getBnkFarsiMount()+"'");
			}
			
			if (groovyStr.contains("bnkMount2E(")){
				groovyStr = groovyStr.replace("bnkMount2E(", "convertor.bnkEnglishMount('"+atmConfiguration.getBnkEnglishMount()+"'");
			}
			
			if (groovyStr.contains("bnkMount2NCRE(")){
				groovyStr = groovyStr.replace("bnkMount2NCRE(", "convertor.bnkEnglishMountForNCR('"+atmConfiguration.getBnkEnglishMount()+"'");
			}
			
			if (groovyStr.contains("right(")){
				groovyStr = groovyStr.replace("right(", "convertor.alignTextRight(text_dir," + atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", ");
			}
			
			if (groovyStr.contains("center(") && !groovyStr.contains("convertor.center(text_dir,")){
				groovyStr = groovyStr.replace("center(", "convertor.center(text_dir,"+ atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", ");
			}
			
			if (groovyStr.contains("justify(") && !groovyStr.contains("convertor.justify(text_dir,")){
				groovyStr = groovyStr.replace("justify(", "convertor.justify(text_dir,"+ atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", ");
			}
			
			if (groovyStr.contains("hr(")){
				groovyStr = groovyStr.replace("hr(", "convertor.horizontalLine(" + atmConfiguration.getReceiptLineLength()  + ", " + atmConfiguration.getReceiptLeftMargin() + ", atm"  + ", ");
			}
			
			if (groovyStr.contains("safeEn(") && !groovyStr.contains("convertor.safeEn(")){
				groovyStr = groovyStr.replace("safeEn(", "convertor.safeEn(");
			}
			
			if (groovyStr.contains("sheba2F(")){
				groovyStr = groovyStr.replace("sheba2F(", "convertor.sheba2Farsi(");
			}			
			
			/*** TransferToAccount:Start ***/
			if (groovyStr.contains("accFa(")){
				groovyStr = groovyStr.replace("accFa(", "convertor.accountFormatFa(encodings, atm" +  ", ");
			}	
			
			if (groovyStr.contains("accEn(")){
				groovyStr = groovyStr.replace("accEn(", "convertor.accountFormatEn(");
			}	
			
			if (groovyStr.contains("accNCREn(")){
				groovyStr = groovyStr.replace("accNCREn(", "convertor.accountFormatEnForNCR(");
			}	
			
			if (groovyStr.contains("account2FScr(")){
				groovyStr = groovyStr.replace("account2FScr(", "convertor.accountScreenFormatFa(");
			}
			
			if (groovyStr.contains("account2EScr(")){
				groovyStr = groovyStr.replace("account2EScr(", "convertor.accountScreenFormatEn(");
			}
			/*** TransferToAccount:End ***/
			
			if (groovyStr.contains("banknameFa(")){
				groovyStr = groovyStr.replace("banknameFa(", "convertor.BankNameFa(encodings, atm" +  ", ");
			}
			
			if (groovyStr.contains("banknameEn(")){
				groovyStr = groovyStr.replace("banknameEn(", "convertor.BankNameEn(");
			}			
			
		}
		return groovyStr;
		
	}

	// Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Merged from TPSP for VISA SMS
	//by m.rehman
	@SuppressWarnings("unchecked")
	public Currency getBaseCurrency() {
		return (Currency) variables.get(BASE_CURRENCY_KEY);
	}


	public String getAuthIdResponseValue(String ChannelName)
	{ //Raza MasterCard
		String query;
		List<BigDecimal> result;
		System.out.println("CHANNELNAME querry...!"); //Raza TEMP
		query = "select " + ChannelName + "_auth_id_seq.nextval from dual";
		result = GeneralDao.Instance.executeSqlQuery(query);
		return result.get(0).toString();
	}

	//m.rehman: load Loro records from db for SAF/Loro
	public void setAllLoroEntries () {
		List<Loro> loroRecords;
		Map<String, Channel> loroMap;
		String query, key;
		Channel channel;

		logger.debug("Reading Loro Entries from DB");

		loroRecords = new ArrayList<Loro>();
		loroMap = new HashMap<String, Channel>();

		query = "from " + Loro.class.getName() + " l";
		loroRecords = GeneralDao.Instance.find(query);

		for (Loro loro : loroRecords) {
			key = loro.getMti()
					+ loro.getTranType()
					+ loro.getRespCode()
					+ loro.getOrigChannel()
					+ loro.getDestChannel();
			channel = getChannel(loro.getHostName());
			loroMap.put(key,channel);
		}

		variables.put(LORO_KEY, loroMap);
	}

	//m.rehman: get Loro Host Channel db for SAF/Loro
	public Channel getLoroHost (String key){
		return ((HashMap<String, Channel>) variables.get(LORO_KEY)).get(key);
	}

	//m.rehman: verify the key for SAF/Loro if exist
	public Boolean isLoroExist (String key) {
		return (((HashMap<String, Channel>) variables.get(LORO_KEY)).containsKey(key)) ? Boolean.TRUE
				: Boolean.FALSE;
	}
	
	//Raza start
	public String getSysTraceAuditNo(String channelName) {
		String query;
		List<BigDecimal> result;
		Integer index;

		index = channelName.length();
		if (channelName.contains("In") || channelName.contains("Out")) {
			index = channelName.indexOf("In");

			if (index <= 0)
				index = channelName.indexOf("Out");
		}

		query = "select stan_" + channelName.substring(0, index) + "_seq.nextval from dual";
		result = GeneralDao.Instance.executeSqlQuery(query);
		return result.get(0).toString();
	}
	//Raza end
	public void setAllMessageRouting() {
		Map<String, String> messageRouting;
		List<MessageRouting> msgRoutingFromDB;
		Map<String, Object> dbParam;
		String dbQuery;
		String key;

		messageRouting = new HashMap<String, String>();
		dbParam = new HashMap<String, Object>();

		dbQuery = "from " + MessageRouting.class.getName() + " r ";
		msgRoutingFromDB = GeneralDao.Instance.find(dbQuery, dbParam);

		for (MessageRouting msgRouting : msgRoutingFromDB) {
			//key = msgRouting.getMti() + msgRouting.getBin() + msgRouting.getReferenceInstId();
			key = msgRouting.getChannelName() + msgRouting.getBin() + msgRouting.getMti() + msgRouting.getInstitutionId() + msgRouting.getTranCode() + msgRouting.getTerminalType() + msgRouting.getDestBankId() + msgRouting.getBankId() + msgRouting.getRecBankId();
			//key = channelname + bin + mti + instid + trancode + termtype + destbnkid + bnkid + recbnkid;
			messageRouting.put(key, msgRouting.getDestination());
		}

		variables.put(MESSAGE_ROUTING_KEY, messageRouting);
	}

	/*Following routine is used to fetch a particular value against a key */
	@SuppressWarnings("unchecked")
	public String getMessageRoutingDestination(String key) {
		return ((HashMap<String, String>)variables.get(MESSAGE_ROUTING_KEY)).get(key);
	}

	/*Following routine is used to get message routing map */
	@SuppressWarnings("unchecked")
	public Map<String, String> getAllMessageRouting() {
		return (HashMap<String, String>) variables.get(MESSAGE_ROUTING_KEY);
	}

	public void setAllHSMChannels() throws Exception {
		Map<String, HSMChannel> channels;
		List<HSMChannel> channelList;

		channels = new HashMap<String, HSMChannel>();
		channelList = null;

		try {
			channelList = HSMChannelManager.getInstance().readFromDB();
		} catch (Exception e) {
			logger.debug(e.getClass().getSimpleName()+": "+ e.getMessage(), e);
		}

		if (channelList != null) {
			for (HSMChannel channel : channelList) {
				channels.put(channel.getName(), channel);
			}
		}

		variables.put(HSM_CHANNELS_KEY, channels);
	}

	@SuppressWarnings("unchecked")
	public Map<String, HSMChannel> getAllHSMChannels() {
		return (HashMap<String, HSMChannel>) variables.get(HSM_CHANNELS_KEY);
	}

	/* Following routine is used to fetch a particular value against a key */
	@SuppressWarnings("unchecked")
	public HSMChannel getHSMChannel(String key) {
		return ((HashMap<String, HSMChannel>)variables.get(HSM_CHANNELS_KEY)).get(key);
	}

	//m.rehman: loading CMS products and keys
	public void setAllCMSProducts() {
		Map<String, CMSProduct> cmsProducts;
		List<CMSProduct> cmsProductsFromDB;
		Map<String, CMSProductDetail> cmsProductDetails;
		List<CMSProductDetail> cmsProductDetailFromDB;
		CMSProductDetail cmsProductDetail;
		Map<String, List<CMSProductKeys>> cmsProductKeysList;
		List<CMSProductKeys> cmsProductKeys;
		Map<String, CMSTrack2Format> cmsTrack2Formats;
		CMSTrack2Format cmsTrack2Format;
		List<CMSProductKeys> cmsProductsKeysFromDB; //Raza TEMP
		Map<String, Object> dbParam;
		String dbQuery, productId;

		cmsProducts = new HashMap<String, CMSProduct>();
		cmsProductDetails = new HashMap<String, CMSProductDetail>();
		cmsProductKeysList = new HashMap<String, List<CMSProductKeys>>();
		cmsTrack2Formats = new HashMap<String, CMSTrack2Format>();
		cmsProductKeys = new ArrayList<CMSProductKeys>();
		dbParam = new HashMap<String, Object>();

		dbQuery = "from " + CMSProduct.class.getName() + " p ";
		cmsProductsFromDB = GeneralDao.Instance.find(dbQuery, dbParam);

		for (CMSProduct cmsProduct : cmsProductsFromDB) {
			productId = cmsProduct.getProductId();
			cmsProducts.put(productId, cmsProduct);

			if (cmsProduct.getProductDetail() != null) {
				cmsProductDetail = cmsProduct.getProductDetail();
				cmsProductDetails.put(productId, cmsProductDetail);

				if (cmsProductDetail.getTrackFormatId() != null) {
					cmsTrack2Format = cmsProductDetail.getTrackFormatId();
					cmsTrack2Formats.put(productId, cmsTrack2Format);
				}
			}

			if (cmsProduct.getProductKeys() != null) {
				cmsProductKeys = cmsProduct.getProductKeys();
				//Collections.copy(cmsProduct.getProductKeys(), cmsProductKeys);
				cmsProductKeysList.put(productId, cmsProductKeys);
			}

			/*
			dbParam.put("cmsProductId", cmsProduct.getProductId());
			dbQuery = "from " + CMSProductDetail.class.getName() + " pd where pd.cmsProductId = :cmsProductId";
			cmsProductDetailFromDB = GeneralDao.Instance.find(dbQuery, dbParam);
			if (cmsProductDetailFromDB != null) {
				cmsProductDetail = cmsProductDetailFromDB.get(0);
				cmsProductDetails.put(cmsProduct.getProductId(), cmsProductDetail);

				dbParam.put("cmsTrackFormatId", cmsProductDetail.getTrackFormatId());
				dbQuery = "from " + CMSTrack2Format.class.getName() + " t2f where t2f.track_id = :cmsTrackFormatId";
				cmsTrack2FormatFromDB = GeneralDao.Instance.find(dbQuery, dbParam);
				if (cmsTrack2FormatFromDB != null) {
					cmsTrack2Format = cmsTrack2FormatFromDB.get(0);
					cmsTrack2Formats.put(cmsProduct.getProductId(), cmsTrack2Format);
				}
			}

			dbQuery = "from " + CMSProductKeys.class.getName() + " pk where pk.cmsProductId = :cmsProductId";
			cmsProductKeysFromDB = GeneralDao.Instance.find(dbQuery, dbParam);
			if (cmsProductKeysFromDB != null)
				cmsProductKeys.put(cmsProduct.getProductId(), cmsProductKeysFromDB);
			*/
		}

		variables.put(CMS_PRODUCTS_KEY, cmsProducts);
		variables.put(CMS_PRODUCT_DETAIL_KEY, cmsProductDetails);
		variables.put(CMS_TRACK_2_FORMATS_KEY, cmsTrack2Formats);
		variables.put(CMS_PRODUCTS_KEYS_KEY, cmsProductKeysList);
	}

	@SuppressWarnings("unchecked")
	public Map<String, CMSProduct> getAllCMSProducts() {
		return (HashMap<String, CMSProduct>) variables.get(CMS_PRODUCTS_KEY);
	}

	@SuppressWarnings("unchecked")
	public CMSProduct getCMSProduct(String key) {
		return ((HashMap<String, CMSProduct>)variables.get(CMS_PRODUCTS_KEY)).get(key);
	}

	@SuppressWarnings("unchecked")
	public Map<String, CMSProductDetail> getAllCMSProductDetail() {
		return (HashMap<String, CMSProductDetail>) variables.get(CMS_PRODUCT_DETAIL_KEY);
	}

	@SuppressWarnings("unchecked")
	public CMSProductDetail getCMSProductDetail(String key) {
		return ((HashMap<String, CMSProductDetail>)variables.get(CMS_PRODUCT_DETAIL_KEY)).get(key);
	}

	@SuppressWarnings("unchecked")
	public Map<String, CMSTrack2Format> getAllCMSTrack2Formats() {
		return (HashMap<String, CMSTrack2Format>) variables.get(CMS_TRACK_2_FORMATS_KEY);
	}

	@SuppressWarnings("unchecked")
	public CMSTrack2Format getCMSTrack2Format(String key) {
		return ((HashMap<String, CMSTrack2Format>)variables.get(CMS_TRACK_2_FORMATS_KEY)).get(key);
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<CMSProductKeys>> getAllCMSProductKeys() {
		return (HashMap<String, List<CMSProductKeys>>) variables.get(CMS_PRODUCTS_KEYS_KEY);
	}

	@SuppressWarnings("unchecked")
	public List<CMSProductKeys> getCMSProductKeys(String key) {
		return ((HashMap<String, List<CMSProductKeys>>)variables.get(CMS_PRODUCTS_KEYS_KEY)).get(key);
	}

	public ArrayList<Contact> getAllCoreDownContact(){

		ArrayList<Contact> contacts = ((ArrayList<Contact>)(variables.get(CONTACT_CORE_DOWN)));
		logger.debug("Core Down Contacts Count is : " + String.valueOf(contacts.size()));
		return contacts;

	}

	//Raza commenting as channels and webservers are managed through same object
//	@SuppressWarnings("unchecked")
//	public void setAllWebservers() { //Raza NAYAPAY
//		Map<String, Channel> channels = new HashMap<String, Channel>();
//
//		String query = "from " + Channel.class.getName() + " c where c.channelType = 'Webserver'";
//		List<Channel> list = GeneralDao.Instance.find(query);
//		for (Channel channel : list) {
//			channels.put(channel.getChannelId(), channel);
//		}
//
//		variables.put(WEBSERVERS_KEY, channels);
//	}

//	@SuppressWarnings("unchecked")
//	public Channel getWebserver(String channelId) { //Raza NAYAPAY
//		return ((HashMap<String, Channel>) variables.get(WEBSERVERS_KEY)).get(channelId);
//	}

//	//m.rehman: get all webserver for process context
//	public Map<String, Channel> getAllWebservers() {
//		return (HashMap<String, Channel>) variables.get(WEBSERVERS_KEY);
//	}
	//Raza commenting as channels and webservers are managed through same object

	public String getAPIByTranCode(String trancode) { //Raza NAYAPAY
		return ((HashMap<String, String>) variables.get(TRANCODE_TO_API_KEY)).get(trancode);
	}

	public String getTranCodeByAPI(String api) { //Raza NAYAPAY
		return ((HashMap<String, String>) variables.get(API_TO_TRANCODE_KEY)).get(api);
	}

	public SwitchTransactionCodes getTransactionCodeDescbyCode(String txncode)
	{
		return ((HashMap<String, SwitchTransactionCodes>) variables.get(TRANSACTIONCODES_KEY)).get(txncode);
	}

	//m.rehman: setting financial transaction code in order to verify financial transactions from application
	public void setFinancialTransactionCodes() {
		List<String> tranList = new ArrayList<>();
		tranList.add("G6");		//CNICBasedCashWithdrawal
		tranList.add("G3");		//Purchase
		tranList.add("G2");		//CashWithDrawal
		tranList.add("G0");		//UnloadWalletInquiry
		tranList.add("FF");		//BioOpsReversalCashWithdrawal
		tranList.add("FE");		//BioOpsCashWithdrawal
		tranList.add("F8");		//CashDeposit
		tranList.add("F3");		//ReverseEnvelop
		tranList.add("F2");		//EnvelopUnload
		tranList.add("F1");		//EnvelopLoad
		tranList.add("E9");		//OnelinkBillerCoreTransaction
		tranList.add("E8");		//OnelinkBillerTransaction
		tranList.add("E6");		//MerchantReversalTransaction
		tranList.add("E5");		//OnelinkBillPayment
		tranList.add("BC");		//MerchantRetailCoreTransaction
		tranList.add("BB");		//MerchantRetailTransaction
		tranList.add("BA");		//MerchantBillerCoreTransaction
		tranList.add("B9");		//MerchantBillerTransaction
		tranList.add("B8");		//WalletTransaction
		tranList.add("B7");		//UnloadWallet
		tranList.add("B6");		//LoadWallet
		variables.put(FINANCIAL_TRANSACTIONS, tranList);
	}

	public boolean isFinancialTransactionCodes(String code) {
		return ((List<String>)variables.get(FINANCIAL_TRANSACTIONS)).contains(code);
	}

	//m.rehman: 05-08-2020, fetching key from vault
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	public void FetchKeyFromKeyVault() throws Exception {

		KeyVaultServer keyVaultServer = new KeyVaultServer();
		Map<String, String> keyMap = keyVaultServer.getKeyfromVault();
		//Map<String, String> keyMap = new HashMap<>();
		//keyMap.put(KeyType.TYPE_AES, "7CDFBAFE1BDD054F6F1C7403482FDE25A15E76E16D88C051E4C6B2FD9C1BFF5A");
		//keyMap.put(KeyType.TYPE_RSA_PUBLIC, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3msA3puDKENbUr8GRROxaA+pX1aWEwzPovRLBvhBVVZk631gufScOFlDdW00IutDXYYCmgelLpXcI6LFmDzLLCUxZ/fPLjo+FymOHhAjl3Fmv0C2t8xMmUIJNx6WuKzaQomuIIhl/L4Fz6APQKMNpL3uYWjD0/a+3FGudi0AX2riOyeDqmgm3z68B4YahlIMHmcY94HLsNIUU5hArjbHTggNImZWC/HOZuQG5BFxwD3v89sXZyq7361GoUR5Kje8ShNNE7IsPdLAERGKzDAts38DnXtFU2N8x2+UGLGU74CfOqGKOUzJIJOAjI/z3R9tDRUm8HuP3xVRqLpt7iDdmwIDAQAB");
		//keyMap.put(KeyType.TYPE_RSA_PRIVATE, "MIIEowIBAAKCAQEAqcyGlI8Rdp8MTyrFZANkdNx003cQXpGsJWl1H2ZqRUAw33ehIzpPQHVZNgFH+6jZ0dEhZ6upB0U48n8r9c8yLWJiJn7icidv2/wCwhn6HIPhzNcOb1Uu47VfTofyLY2iiez0mOOTJuTRioAOePxJjEBKwIumMSZfYvNjy0Oq+dbkbhF5FjczlSkSZH0kiLSENxL6jcFKix7LNmoiqr01Q5zjbfTrfyn/CsPCt+6VWpT0HpU+AiqYwAcnbsSDPzUfP7f0HxpkU5yOFdZvfDCSGk8dij8+gVVRRCz/zx2A6Y9XXKbwNiWwiiCNCRPM+qlM+Q9BEvCNe5LyGlnWktKv1QIDAQABAoIBAQCi2vhlOgcM16UNtT8XGq3Z66vdOkAoA92xEEF2eVsJynRVkL6rM/zZHPQQeRAaTiuEMumwLhNzVrA03UZDf5NntgnfVClKRYri2gWnNxUNDmyec9FzdKcBXW7oujjL+iFf2jYLAZKcZjCXfxTic07zRqKrpAUk9OHd3QTk6qTs897EapBFkRhOji0GEiuWrMdJNKuG/xBLUPmG6gusUozsDaQVXs37UZmMGxaI2lZLZBLiMKfMpdLCeWUgZWSNE125OED4MsJIMNAU9TtYKRQsmxqvQ35kJmyvsf0mVxz44q0MJrAD6SJIKcx2yI3Fv8ST+xc4RsT8uwFkX0LKaebtAoGBAN9wRxv9A7NRv9n4UK6j22exfoNfOBJul8+JWhyTmJVfev5APeNdjDwQStp5tHVSz0FwiUhzlUjWLswOrYpCzTK6opGTCxdWLoV9h2j7pipluUeO1n4PuwNaX4nWR6fxfO8OOkHEeKc0nKBlpKYbUHxISXO/P4y8ounJdJW6BAbHAoGBAMKLIzUmABM5shEsLpqq1JlWydgLh0eFaERlKxa78xMdUtQfNVWDRzI339W5fjZoNpGUZPkVLS/aBqhNUxQJANXq+KWegZzDSoa+rToAEYqMiGo+lYyWSWE2qAyfBr9jX6fCogzFL9C4x6GNEyV+oBlWQefZ5XJLuE4C3gxICwiDAoGANNWu6u5cVqXJEPjH01QJyK5O9S+p6anfGgMnnMRF+2RViBOWztHLHFTZ7mmDA48uVRIQVGKIdW24KSZ7YMtFDG7XjEbSA5WscIJrZ6bB9xqQwlwDWlHL7hILr2NNFGhlG34oxm53/UnSk5cVZPXmhUzumYEqvNPKTjAF8nXHB/cCgYBzi0fh7SvmP0IvSNp1KjkSyaUgF9jE9csztBKfcgmwtJ3ZNp/qpgnHHcBno7/+eG7QkB5YgedQoGGL3EEXiTFh3CjJ1RaOEL9WwDEKXbgNXeI3l+wij7ANePTeaiULzu2lKcgtZYA8q0KGBM/2WPqlkcGa/qNz+t0AGFvhV3iWGQKBgEXbAqatUnGsGz3MSawZA5qf6MJ2OcLLu4M83Ofe2TZr8Y2jeCM1hOKd/tlwBa1eNP8siK56DoyacGpNnJ3FmYwKDPDdqWWD+Fb4Ny8tNaL53cmlxFOU399pbjG4pA2X4mPAQlqVw49RGR7ahkZIuQc2uw1Gc4fgGo9YuQUzS2Gq");
		if (keyMap == null || keyMap.size() <= 0) {
			throw new Exception("Unable to fetch PAN encryption keys from Key Vault Server");
		}
		variables.put(PAN_ENC_KEY, keyMap);
	}

	public Map<String, String> getPanEncryptionKeys() {
		return (Map<String, String>) variables.get(PAN_ENC_KEY);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	//m.rehman: 22-11-2021, HSM response logging
	public byte[] getHsmResponse(String key) {
		return hsmResponse.get(key);
	}

	public void setHsmResponse(String key, byte[] value) {
		this.hsmResponse.put(key, value);
	}

	public void removeHsmResponse(String key) {
		this.hsmResponse.remove(key);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
