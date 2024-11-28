package vaulsys.initializer;

import vaulsys.calendar.DateTime;
import vaulsys.contact.Address;
import vaulsys.contact.City;
import vaulsys.contact.Contact;
import vaulsys.contact.Country;
import vaulsys.contact.PhoneNumber;
import vaulsys.contact.State;
import vaulsys.contact.Website;
import vaulsys.customer.CustomerService;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Branch;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.base.SecurityFunction;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.jceadapter.JCESecurityModule;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.terminal.impl.TerminalSharedFeature;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class DBInitializeATM {

	/*** ATM Terminal & Branch ***/
	public static final long ATM_GENERAL_CODE = 203003;
	public static final long BRANCH_GENERAL_CODE = 203217;
	public static final String BRANCH_GENERAL_NAME = "Ø§Ù…Ø§Ù… Ø­Ø³Ù†";
	public static final String CORE_BRANCH_CODE = "323";
	
	public static final long ATM_CODE = 204972;
	public static final long BRANCH_CODE = 204901;
	public static final String BRANCH_NAME = "ØªØ³Øª Ù�Ù†Ø§Ù¾";
	public static final String CORE_CODE = "444";

	
	public static final String GENERAL_ADDRESS = "Ø§Ø´Ø±Ù�ÛŒ Ø§ØµÙ�Ù‡Ø§Ù†ÛŒ-Ø§Ù…Ø§Ù… Ø­Ø³Ù†";
//	public static final String GENERAL_ADDRESS = "Ù…ÛŒØ±Ø¯Ø§Ù…Ø§Ø¯-Ø§Ù…ÙˆØ± Ø§Ù†Ù�ÙˆØ±Ù…Ø§ØªÛŒÚ©";
	public static final String GENERAL_ACCOUNT = "220-800-36994-1";
	public static final String GENERAL_OWNERNAME = "-";
	
	private static final String ATM_TERMINAL_GROUP_NAME = "Ø®ÙˆØ¯Ù¾Ø±Ø¯Ø§Ø²Ù‡Ø§";
	
	
	/*** Geo ***/
	public static final Long CITY_TEHRAN_CODE = 100L;
	public static final Long STATE_TEHRAN_CODE = 24L;
	public static final Long COUNTRY_IRAN_CODE = 1L;

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
//			new DBInitializeATM().cereateDB();
			new DBInitializeATM().createATMSecurityProfile();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
		System.exit(0);
	}

	private void cereateDB() throws Exception {
		
		System.out.println("------- Creating ATM -------");
//		initATM(ATM_GENERAL_CODE, BRANCH_GENERAL_CODE, CORE_BRANCH_CODE, BRANCH_GENERAL_NAME);
		initATM(ATM_CODE, BRANCH_CODE, CORE_CODE, BRANCH_NAME);

		System.out.println("------- FINISHED -------");
	}

	public void initATM(Long atmCode, Long branchCode, String core_branch, String branchName ) throws Exception {
		Branch branch = FinancialEntityService.findEntity(Branch.class, branchCode.toString());
		if (branch == null) {
			branch = createBranch();
			branch.setCode(branchCode);
			branch.setName(branchName);
			branch.setCoreBranchCode(core_branch);
			branch.setCreatedDateTime(DateTime.now());
			branch.setCreatorUser(DBInitializeUtil.getUser());
			getGeneralDao().saveOrUpdate(branch);
		}
		
		ATMTerminal atmTerminal = TerminalService.findTerminal(ATMTerminal.class, atmCode);
		if (atmTerminal == null) {
			atmTerminal = new ATMTerminal();
			atmTerminal.setCode(atmCode);
			atmTerminal.setCreatorUser(DBInitializeUtil.getUser());
			atmTerminal.setCreatedDateTime(DateTime.now());
		}
		
		getGeneralDao().saveOrUpdate(branch);
		
		createKeySet(atmTerminal, GlobalContext.getInstance().getATMKey(), GlobalContext.getInstance().getATMKey(), GlobalContext.getInstance().getATMKey());
		atmTerminal.setSecurityProfile(createATMSecurityProfile());
//		branch.addTerminal(atmTerminal);
		atmTerminal.setOwner(branch);
		atmTerminal.setATMState(ATMState.UNKNOWN);
		
		TerminalGroup atmGroup = getTerminalGroup(ATM_TERMINAL_GROUP_NAME);
		if (atmGroup.getSharedFeature() == null){
			atmGroup.getSharedFeature().setConfiguration(getATMconfiguration());
		}
		atmTerminal.setParentGroup(atmGroup);
//		atmTerminal.setAuthorizationProfile(createATMAuthorizationProfile());
		
		
		TerminalSharedFeature sharedFeature = new TerminalSharedFeature();
		sharedFeature.setAuthorizationProfile(atmGroup.getSharedFeature().getAuthorizationProfile());
		sharedFeature.setClearingProfile(atmGroup.getSharedFeature().getClearingProfile());
		sharedFeature.setConfiguration(atmGroup.getSharedFeature().getConfiguration());
		sharedFeature.setFeeProfile(atmGroup.getSharedFeature().getFeeProfile());
		sharedFeature.setSecurityProfile(atmGroup.getSharedFeature().getSecurityProfile());
		sharedFeature.setEnabled(true);
		getGeneralDao().saveOrUpdate(sharedFeature);
		atmTerminal.setSharedFeature(sharedFeature);
		
		getGeneralDao().saveOrUpdate(atmTerminal);
	}

	private ATMConfiguration getATMconfiguration() {
		String query = "from "+ ATMConfiguration.class.getName()+" c ";
		return (ATMConfiguration) getGeneralDao().findObject(query, null);
	}

	private TerminalGroup getTerminalGroup(String name) {
    	String query = "from " + TerminalGroup.class.getName() + " ap where ap.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
        return (TerminalGroup) getGeneralDao().findObject(query, param);
	}

	private Branch createBranch() throws Exception {
		Branch branch = new Branch();
		branch.setContact(createContact());
		branch.setCreatorUser(GlobalContext.getInstance().getSwitchUser());
		branch.setCreatedDateTime(DateTime.now());
		
		return branch;
	}

	private Contact createContact() {
		Contact contact = new Contact();

		Country country = CustomerService.findCountry(COUNTRY_IRAN_CODE);
		if (country == null) {
			country = new Country("Ø§ÛŒØ±Ø§Ù†", COUNTRY_IRAN_CODE);
			country.setCreatorUser(GlobalContext.getInstance().getSwitchUser());
			country.setCreatedDateTime(DateTime.now());
			country.setAbbreviation("IR");
			getGeneralDao().saveOrUpdate(country);
		}
		
		State state = CustomerService.findState(STATE_TEHRAN_CODE);
		if (state == null) {
			state = new State("ØªÙ‡Ø±Ø§Ù†", STATE_TEHRAN_CODE);
			state.setCreatorUser(GlobalContext.getInstance().getSwitchUser());
			state.setCreatedDateTime(DateTime.now());
			state.setAbbreviation("THR");
			state.setCountry(country);
			getGeneralDao().saveOrUpdate(state);
		}

		City city = CustomerService.findCity(CITY_TEHRAN_CODE);
		if (city == null) {
			city = new City("ØªÙ‡Ø±Ø§Ù†", CITY_TEHRAN_CODE);
			city.setCreatorUser(GlobalContext.getInstance().getSwitchUser());
			city.setCreatedDateTime(DateTime.now());
			city.setAbbreviation("THR");
			city.setState(state);
			getGeneralDao().saveOrUpdate(city);
		}
		
		contact.setAddress(new Address());
		contact.getAddress().setAddress(GENERAL_ADDRESS);
		contact.getAddress().setCity(city);
		contact.getAddress().setState(state);
		contact.getAddress().setCountry(country);
		
//		contact.setName("Ù�Ù†Ø§Ù¾");
		contact.setPhoneNumber(new PhoneNumber());
		contact.getPhoneNumber().setNumber("88208820");
		contact.getPhoneNumber().setAreaCode("021");
		contact.setName("-");
		contact.setWebsite(new Website());
		contact.getWebsite().setWebsiteAddress("http://www.bpi.ir");
		
		return contact;
	}
		
	private SecurityProfile createATMSecurityProfile() throws Exception {
		String name = "Ø§Ù„Ú¯ÙˆÛŒ Ø§Ù…Ù†ÛŒØªÛŒ Ù¾ÛŒØ´ Ù�Ø±Ø¶ Ø®ÙˆØ¯Ù¾Ø±Ø¯Ø§Ø²";
		SecurityProfile securityProfile;

		String query = "from " + SecurityProfile.class.getName() + " ap where ap.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
		securityProfile = (SecurityProfile) getGeneralDao().findObject(query, param);
		if (securityProfile != null)
			return securityProfile;
		
		securityProfile = new SecurityProfile();
		securityProfile.setName(name);
		// for (int i = 0; i < 3; i++) {
		SecurityFunction MAC_GEN_FUNC = new SecurityFunction();
		MAC_GEN_FUNC.setName(SecurityComponent.FUNC_MAC_GEN);
		MAC_GEN_FUNC.setHost("Fanap Security Module");
		MAC_GEN_FUNC.addParameter("Algorithm", "0");
		MAC_GEN_FUNC.addParameter("MacLength", "4");
		MAC_GEN_FUNC.addParameter("Padding", "ZeroPadding");
		MAC_GEN_FUNC.setSecurityProfile(securityProfile);
		securityProfile.addFunction(MAC_GEN_FUNC);

		SecurityFunction MAC_VER_FUNC = new SecurityFunction();
		MAC_VER_FUNC.setName(SecurityComponent.FUNC_MAC_VER);
		MAC_VER_FUNC.setHost("Fanap Security Module");
		MAC_VER_FUNC.addParameter("Algorithm", "0");
		MAC_VER_FUNC.addParameter("MacLength", "4");
		MAC_VER_FUNC.addParameter("Padding", "ZeroPadding");
		MAC_VER_FUNC.addParameter("SkipLength", "9");
		MAC_VER_FUNC.setSecurityProfile(securityProfile);
		securityProfile.addFunction(MAC_VER_FUNC);

		SecurityFunction PIN_TRANS = new SecurityFunction();
		PIN_TRANS.setName(SecurityComponent.FUNC_TRANSLATEPIN);
		PIN_TRANS.setHost("Fanap Security Module");
		PIN_TRANS.addParameter("PIN Format", "01");
		PIN_TRANS.addParameter("AccountNumber Length", "12");
		PIN_TRANS.setSecurityProfile(securityProfile);
		securityProfile.addFunction(PIN_TRANS);
		// }

		getGeneralDao().saveOrUpdate(securityProfile);

		return securityProfile;
	}

	private void createKeySet(Terminal terminal, String macKey, String pinKey, String masterKey) throws Exception {
		JCESecurityModule ssm = new JCESecurityModule("/src/main/resources/config/LMK.jceks", "$3cureP@$$".toCharArray(),
				"org.bouncycastle.jce.provider.BouncyCastleProvider");

		List<SecureDESKey> keysByType = new ArrayList<SecureDESKey>();

		if(macKey != null && !macKey.isEmpty()) {
			Key mkey = new SecretKeySpec(Hex.decode(macKey), "DES");
			SecureDESKey keyMac = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, mkey);
			terminal.addSecureKey(keyMac);
			getGeneralDao().saveOrUpdate(keyMac);
		}

		if(pinKey != null && !pinKey.isEmpty()) {
			Key pkey = new SecretKeySpec(Hex.decode(pinKey), "DES");
			SecureDESKey keyPin = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, pkey);
			terminal.addSecureKey(keyPin);
			getGeneralDao().saveOrUpdate(keyPin);
		}
		
		if(masterKey != null && !masterKey.isEmpty()) {
			Key pkey = new SecretKeySpec(Hex.decode(masterKey), "DES");
			SecureDESKey keyPin = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TMK, pkey);
			terminal.addSecureKey(keyPin);
			getGeneralDao().saveOrUpdate(keyPin);
		}

		getGeneralDao().saveOrUpdate(terminal);

//		keySet.setKeysByType(keySet);
	}

	private GeneralDao getGeneralDao() {
		return GeneralDao.Instance;
	}
}
