package vaulsys.initializer;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.authorization.policy.Bank;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.SettlementDataCriteria;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.cyclecriteria.CycleCriteria;
import vaulsys.clearing.cyclecriteria.CycleType;
import vaulsys.clearing.settlement.BillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.ChargeSettlementServiceImpl;
import vaulsys.clearing.settlement.InstitutionSettlementServiceImpl;
import vaulsys.clearing.settlement.MerchantSettlementServiceImpl;
import vaulsys.contact.Address;
import vaulsys.contact.City;
import vaulsys.contact.Contact;
import vaulsys.contact.Country;
import vaulsys.contact.PhoneNumber;
import vaulsys.contact.State;
import vaulsys.contact.Website;
import vaulsys.customer.Account;
import vaulsys.customer.Core;
import vaulsys.customer.Currency;
import vaulsys.customer.CustomerService;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.MerchantCategory;
import vaulsys.entity.impl.FinancialEntityGroup;
import vaulsys.entity.impl.FinancialEntitySharedFeature;
import vaulsys.entity.impl.Shop;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.scheduler.job.BillPaymentCycleSettlementJob;
import vaulsys.scheduler.job.CellChargeCycleSettlementJob;
import vaulsys.scheduler.job.CycleSettlementJob;
import vaulsys.scheduler.job.MerchantCycleSettlementJob;
import vaulsys.scheduler.job.ShetabCycleSettlementJob;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.jceadapter.JCESecurityModule;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.RSAPublicKey;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.POSTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.EPAYTerminal;
import vaulsys.terminal.impl.EPAYTerminalVersion;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.POSTerminalVersion;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.terminal.impl.TerminalSharedFeature;
import vaulsys.transaction.ClearingState;
import vaulsys.user.User;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class DBInitializeUtil {
	
	private static final String DBINITIALIZER_USER = "switch";
	public static final String DEFAULT_SECURITY_PROFILE_NAME = "Ù¾ÛŒØ´ Ù�Ø±Ø¶";
	public static final String APACS_SECURITY_PROFILE_NAME = "Ù¾ÛŒØ´ Ù�Ø±Ø¶ APACS";
	
	//Geo
	public static final Long CITY_TEHRAN_CODE = 100L;
	public static final Long STATE_TEHRAN_CODE = 24L;
	public static final Long COUNTRY_IRAN_CODE = 1L;
	
	
	//Currency
	private static final int CURRENCY_RIAL_CODE = 364;
	private static final String CURRENCY_RIAL_NAME = "IR";

	
	public static void createBanks() {
		Bank bank = new Bank();
		bank.setBin(589210);
		bank.setName("Ø¨Ø§Ù†Ú© Ø³Ù¾Ù‡");
		bank.setTwoDigitCode(15);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(589463);
		bank.setName("Ø¨Ø§Ù†Ú© Ø±Ù�Ø§Ù‡ Ú©Ø§Ø±Ú¯Ø±Ø§Ù†");
		bank.setTwoDigitCode(13);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(599999);
		bank.setName("Ø¨Ø§Ù†Ú© Ù…Ø±Ú©Ø²ÛŒ");
		bank.setTwoDigitCode(null);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(603769);
		bank.setName("Ø¨Ø§Ù†Ú© ØµØ§Ø¯Ø±Ø§Øª");
		bank.setTwoDigitCode(19);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(603770);
		bank.setName("Ø¨Ø§Ù†Ú© Ú©Ø´Ø§ÙˆØ±Ø²ÛŒ");
		bank.setTwoDigitCode(16);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(603799);
		bank.setName("Ø¨Ø§Ù†Ú© Ù…Ù„ÛŒ");
		bank.setTwoDigitCode(17);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(610433);
		bank.setName("Ø¨Ø§Ù†Ú© Ù…Ù„Øª");
		bank.setTwoDigitCode(12);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(621986);
		bank.setName("Ø¨Ø§Ù†Ú© Ø³Ø§Ù…Ø§Ù†");
		bank.setTwoDigitCode(52);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(622106);
		bank.setName("Ø¨Ø§Ù†Ú© Ù¾Ø§Ø±Ø³ÛŒØ§Ù†");
		bank.setTwoDigitCode(22);
		GeneralDao.Instance.saveOrUpdate(bank);
			
		bank = new Bank();
		bank.setBin(627353);
		bank.setName("Ø¨Ø§Ù†Ú© ØªØ¬Ø§Ø±Øª");
		bank.setTwoDigitCode(18);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(627412);
		bank.setName("Ø¨Ø§Ù†Ú© Ø§Ù‚ØªØµØ§Ø¯ Ù†ÙˆÛŒÙ†");
		bank.setTwoDigitCode(55);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(627488);
		bank.setName("Ø¨Ø§Ù†Ú© Ú©Ø§Ø±Ø§Ù�Ø±ÛŒÙ†");
		bank.setTwoDigitCode(53);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(627648);
		bank.setName("Ø¨Ø§Ù†Ú© ØªÙˆØ³Ø¹Ù‡ ØµØ§Ø¯Ø±Ø§Øª");
		bank.setTwoDigitCode(20);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(627760);
		bank.setName("Ù¾Ø³Øª Ø¨Ø§Ù†Ú©");
		bank.setTwoDigitCode(21);
		GeneralDao.Instance.saveOrUpdate(bank);
			
		bank = new Bank();
		bank.setBin(627961);
		bank.setName("Ø¨Ø§Ù†Ú© ØµÙ†Ø¹Øª Ùˆ Ù…Ø¹Ø¯Ù†");
		bank.setTwoDigitCode(11);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(628023);
		bank.setName("Ø¨Ø§Ù†Ú© Ù…Ø³Ú©Ù†");
		bank.setTwoDigitCode(14);
		GeneralDao.Instance.saveOrUpdate(bank);
			
		bank = new Bank();
		bank.setBin(639347);
		bank.setName("Ø¨Ø§Ù†Ú© Ù¾Ø§Ø³Ø§Ø±Ú¯Ø§Ø¯-1");
		bank.setTwoDigitCode(57);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(639607);
		bank.setName("Ø¨Ø§Ù†Ú© Ø³Ø±Ù…Ø§ÛŒÙ‡");
		bank.setTwoDigitCode(88);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(502229);
		bank.setName("Ø¨Ø§Ù†Ú© Ù¾Ø§Ø³Ø§Ø±Ú¯Ø§Ø¯-2");
		bank.setTwoDigitCode(57);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(639346);
		bank.setName("Ø¨Ø§Ù†Ú© Ø³ÙŠÙ†Ø§");
		bank.setTwoDigitCode(59);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(628157);
		bank.setName("Ù…ÙˆØ³Ø³Ù‡ Ø§Ø¹ØªØ¨Ø§Ø±ÛŒ ØªÙˆØ³Ø¹Ù‡");
		bank.setTwoDigitCode(51);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(636214);
		bank.setName("Ø¨Ø§Ù†Ú© ØªØ§Øª");
		bank.setTwoDigitCode(62);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(606373);
		bank.setName("Ø¨Ø§Ù†Ú© Ù‚Ø±Ø¶ Ø§Ù„Ø­Ø³Ù†Ù‡ Ù…Ù‡Ø±Ø§ÛŒØ±Ø§Ù†");
		bank.setTwoDigitCode(60);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(502806);
		bank.setName("Ù…ÙˆØ³Ø³Ù‡ Ù…Ø§Ù„ÛŒ Ø§Ø¹ØªØ¨Ø§Ø±ÛŒ Ø´Ù‡Ø±");
		bank.setTwoDigitCode(61);
		GeneralDao.Instance.saveOrUpdate(bank);
		
		bank = new Bank();
		bank.setBin(502908);
		bank.setName("Ø¨Ø§Ù†Ú© ØªÙˆØ³Ø¹Ù‡ ØªØ¹Ø§ÙˆÙ†");
		bank.setTwoDigitCode(null);
		GeneralDao.Instance.saveOrUpdate(bank);
		
	}

	static public TerminalGroup createTerminalGroup(FeeProfile feeProfile, SecurityProfile securityProfile,
			AuthorizationProfile authorizationProfile, ClearingProfile clearingProfile, TerminalGroup parentGroup, String name, ChargeAssignmentPolicy cellChargePolicy) {
		TerminalGroup terminalGroup = (TerminalGroup) GeneralDao.Instance.findObject("from TerminalGroup where name='"+name+"'", null);
		
		if (terminalGroup != null) {
			return terminalGroup;
		}

		
		terminalGroup = new TerminalGroup();
		TerminalSharedFeature sharedFeature = new TerminalSharedFeature();

		terminalGroup.setEnabled(true);
		terminalGroup.setName(name);

		
		if (parentGroup != null) {
			terminalGroup.setParentGroup(parentGroup);
			sharedFeature = parentGroup.getSafeSharedFeature().copy();
		}
			
		if (feeProfile != null) {
			terminalGroup.setOwnFeeProfile(true);
			sharedFeature.setFeeProfile(feeProfile);
		} else
			terminalGroup.setOwnFeeProfile(false);
			
		if (securityProfile != null) {
			terminalGroup.setOwnSecurityProfile(true);
			sharedFeature.setSecurityProfile(securityProfile);
		}else 
			terminalGroup.setOwnSecurityProfile(false);
			
		if (authorizationProfile != null) {
			terminalGroup.setOwnAuthorizationProfile(true);
			sharedFeature.setAuthorizationProfile(authorizationProfile);
		} else
			terminalGroup.setOwnAuthorizationProfile(false);
			
		if (clearingProfile != null) {
			terminalGroup.setOwnClearingProfile(true);
			sharedFeature.setClearingProfile(clearingProfile);
		} else
			terminalGroup.setOwnClearingProfile(false);
			
		if (cellChargePolicy != null) {
			sharedFeature.setChargePolicy(cellChargePolicy);
		}
			
		sharedFeature.setEnabled(true);
	
		GeneralDao.Instance.saveOrUpdate(sharedFeature);
		
		terminalGroup.setSharedFeature(sharedFeature );
		terminalGroup.setCreatorUser(getUser(DBINITIALIZER_USER));
		terminalGroup.setCreatedDateTime(DateTime.now());
		
		GeneralDao.Instance.saveOrUpdate(terminalGroup);
		
		return terminalGroup;
	}

	
	static public FinancialEntityGroup createFinancialEntityGroup(FeeProfile feeProfile, AuthorizationProfile authorizationProfile,
			ClearingProfile clearingProfile, FinancialEntityGroup parentGroup, String name) {
		FinancialEntityGroup group = (FinancialEntityGroup) GeneralDao.Instance.findObject("from FinancialEntityGroup where name='"+name+"'", null);
		
		if (group != null) {
			return group;
		}
		
		group = new FinancialEntityGroup();
		FinancialEntitySharedFeature sharedFeature = new FinancialEntitySharedFeature();
		
		group.setEnabled(true);
		group.setName(name);
		
		if (parentGroup != null) {
			group.setParentGroup(parentGroup);
			sharedFeature = parentGroup.getSafeSharedFeature().copy();
		}
		
		if (feeProfile != null) {
			sharedFeature.setFeeProfile(feeProfile);
			group.setOwnFeeProfile(true);
		} else 
			group.setOwnFeeProfile(false);
			
		if (authorizationProfile != null) {
			sharedFeature.setAuthorizationProfile(authorizationProfile);
			group.setOwnAuthorizationProfile(true);
		} else 
			group.setOwnAuthorizationProfile(false);
		
		if (clearingProfile != null) {
			sharedFeature.setClearingProfile(clearingProfile);
			group.setOwnClearingProfile(true);
		} else 
			group.setOwnClearingProfile(false);
		
		
		sharedFeature.setEnabled(true);
		GeneralDao.Instance.saveOrUpdate(sharedFeature);
		
		group.setSharedFeature(sharedFeature );
		group.setCreatorUser(getUser(DBINITIALIZER_USER));
		group.setCreatedDateTime(DateTime.now());
		
		GeneralDao.Instance.saveOrUpdate(group);
		
		return group;
	}
	
	static public void initPOS(long pos_code, String shop_code, String pos_serial, long merchant_code, FeeProfile feeProfile, SecurityProfile securityProfile, AuthorizationProfile authorizationProfile, ClearingProfile clearingProfile, ChargeAssignmentPolicy chargeAssignmentPolicy, TerminalGroup terminalGroup) throws Exception {
		Shop shop = FinancialEntityService.findEntity(Shop.class, shop_code);
		
		POSTerminal posTerminal = TerminalService.findTerminal(POSTerminal.class, pos_code);
		if (posTerminal == null) {
			posTerminal = createPOSTerminal(pos_code);
		}
		
		posTerminal.setCreatorUser(getUser(DBINITIALIZER_USER));
		posTerminal.setCreatedDateTime(DateTime.now());
		
		TerminalSharedFeature sharedFeature = terminalGroup.getSafeSharedFeature();
		posTerminal.setParentGroup(terminalGroup);
		posTerminal.setOwner(shop);
		posTerminal.setSerialno(pos_serial);
		POSTerminalService.addDefaultKeySetForTerminal(posTerminal);
		posTerminal.setAuthorizationProfile(authorizationProfile);
		posTerminal.setSharedFeature(sharedFeature);
		posTerminal.setClearingProfile(clearingProfile);
		GeneralDao.Instance.saveOrUpdate(posTerminal);
	}
	
	static private POSTerminal createPOSTerminal(long pos_code) throws Exception {
		POSTerminal posTerminal = new POSTerminal();
		posTerminal.setCode(pos_code);
//		createKeySet(posTerminal, POS_MAC_KEY, POS_MAC_KEY, null);
		POSTerminalVersion version = new POSTerminalVersion();
		version.setCreatorUser(getUser(DBINITIALIZER_USER));
		version.setCreatedDateTime(DateTime.now());
		version.setParent(posTerminal);
		GeneralDao.Instance.saveOrUpdate(version);
		GeneralDao.Instance.saveOrUpdate(posTerminal);
		return posTerminal;
	}

	
	static private void createKeySet(Terminal terminal, String macKey, String pinKey, String masterKey) throws Exception {
		JCESecurityModule ssm = new JCESecurityModule("/src/main/resources/config/LMK.jceks", "$3cureP@$$".toCharArray(),
				"org.bouncycastle.jce.provider.BouncyCastleProvider");

		List<SecureDESKey> keysByType = new ArrayList<SecureDESKey>();

		if(macKey != null && !macKey.isEmpty()) {
			Key mkey = new SecretKeySpec(Hex.decode(macKey), "DES");
			SecureDESKey keyMac = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, mkey);
			terminal.addSecureKey(keyMac);
			GeneralDao.Instance.saveOrUpdate(keyMac);
		}

		if(pinKey != null && !pinKey.isEmpty()) {
			Key pkey = new SecretKeySpec(Hex.decode(pinKey), "DES");
			SecureDESKey keyPin = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, pkey);
			terminal.addSecureKey(keyPin);
			GeneralDao.Instance.saveOrUpdate(keyPin);
		}
		
		if(masterKey != null && !masterKey.isEmpty()) {
			if (masterKey.length() * 4 == SMAdapter.LENGTH_DES) {
				Key msKey = new SecretKeySpec(Hex.decode(masterKey), "DES");
				SecureDESKey keyMaster = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TMK, msKey);
				terminal.addSecureKey(keyMaster);
				GeneralDao.Instance.saveOrUpdate(keyMaster);
			} else {
				Key msKey = new SecretKeySpec(Hex.decode(masterKey), "DESede");
				SecureDESKey keyMaster = ssm.encryptToLMK(SMAdapter.LENGTH_DES3_3KEY, KeyType.TYPE_TMK, msKey);
				terminal.addSecureKey(keyMaster);
				GeneralDao.Instance.saveOrUpdate(keyMaster);
			}
		}

		GeneralDao.Instance.saveOrUpdate(terminal);

	}
	
	
	static public void initEPay(long epay_terminal_code, String epay_shop_code, RSAPublicKey publicKey, FeeProfile feeProfile, SecurityProfile securityProfile, AuthorizationProfile authorizationProfile, ClearingProfile clearingProfile, ChargeAssignmentPolicy chargeAssignmentPolicy, TerminalGroup terminalGroup) throws Exception {
		Shop shop = FinancialEntityService.findEntity(Shop.class, epay_shop_code);
		
		EPAYTerminal epayTerminal = TerminalService.findTerminal(EPAYTerminal.class, epay_terminal_code);
		if (epayTerminal == null) {
			epayTerminal = createEPayTerminal(epay_terminal_code, publicKey);
		}
		
		epayTerminal.setCreatorUser(getUser(DBINITIALIZER_USER));
		epayTerminal.setCreatedDateTime(DateTime.now());
		
		epayTerminal.setParentGroup(terminalGroup);
		epayTerminal.setOwner(shop);
		epayTerminal.setAuthorizationProfile(authorizationProfile);
		epayTerminal.setSharedFeature(terminalGroup.getSafeSharedFeature());
		epayTerminal.setClearingProfile(clearingProfile);
		EPAYTerminalVersion version = new EPAYTerminalVersion();
		version.setCreatorUser(getUser(DBINITIALIZER_USER));
		version.setCreatedDateTime(DateTime.now());
		version.setParent(epayTerminal);
		GeneralDao.Instance.saveOrUpdate(version);
		GeneralDao.Instance.saveOrUpdate(epayTerminal);
	}

	static private EPAYTerminal createEPayTerminal(long epay_terminal_code, RSAPublicKey publicKey) {
		EPAYTerminal epayTerminal = new EPAYTerminal();
		epayTerminal.setCode(epay_terminal_code);
//		RSAPublicKey publicKey = new RSAPublicKey(SMAdapter.LENGTH_RSA_PUBLIC_1024, SMAdapter.TYPE_TMK, "30819F300D06092A864886F70D010101050003818D0030818902818100B58E92671C3B1F21686E8D3635D0197479F55BD97C742DBF876DDDB7586B21BB84C2E68663B83B665497DB64522CBB392BA837E6045C4F8CC9DA531488528B5685F5A83130A66601A83D7913F8BDEEAFF0FB0454A9FFF7C1973FD9A3E93677E5AF08748B9497AB259AD444824F185CD732B4FD1B1F321DFA1F7BF0F3620F97070203010001");
		epayTerminal.addSecureKey(publicKey);
		
		GeneralDao.Instance.saveOrUpdate(publicKey);
		GeneralDao.Instance.saveOrUpdate(epayTerminal);
		return epayTerminal;
	}
	
	static private User getUser(String username) {
		String query = "from User i where i.username = :username";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("username", username);
		User user = (User) GeneralDao.Instance.findObject(query, param);
		if (user == null) {
			user = new User();
			user.setUsername(username);
			GeneralDao.Instance.saveOrUpdate(user);
		}
		return user;
	}

	static public User getUser() {
		return getUser(DBINITIALIZER_USER);
	}
	
	static public Account createAccount(String accountId, String holder, Core core) {
		return createAccount(accountId, holder, CURRENCY_RIAL_CODE, CURRENCY_RIAL_NAME, core);
	}
	
	static public Account createAccount(String accountId, String holder) {
		return createAccount(accountId, holder, CURRENCY_RIAL_CODE, CURRENCY_RIAL_NAME, Core.NEGIN_CORE);
	}
	
	static public Account createAccount(String accountId, String holder, int currency_code, String currency_name, Core core) {
		Account account = new Account();
		account.setAccountNumber(accountId);
		account.setCurrency(createCurrency(currency_code, currency_name));
		account.setAccountHolderName(holder);
		if (core == null)
			core = Core.NEGIN_CORE;
		account.setCore(core);
		return account;
	}

	
	
	static public Currency createCurrency(int currency_code, String currency_name) {
		Currency currency = findCurrency(currency_code);
		if (currency == null) {
			currency = new Currency();
			currency.setName(currency_name);
			currency.setCode(currency_code);
		}
		GeneralDao.Instance.saveOrUpdate(currency); 
		return currency;
	}

	static public Currency createDefaultCurrency() {
		Currency currency = findCurrency(DBInitializeUtil.CURRENCY_RIAL_CODE);
		if (currency == null) {
			currency = new Currency();
			currency.setName(DBInitializeUtil.CURRENCY_RIAL_NAME);
			currency.setCode(DBInitializeUtil.CURRENCY_RIAL_CODE);
		}
		GeneralDao.Instance.saveOrUpdate(currency); 
		return currency;
	}
	
	static public Currency findCurrency(int currencyRialCode) {
        String query = "from " + Currency.class.getName() + " c where code = :code";
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("code", currencyRialCode);
        return (Currency) GeneralDao.Instance.findObject(query, param);
	}

	static public Contact createContact(String name, String address, String phoneNumber, String webAddress) {
		Contact contact = new Contact();

		Country country = CustomerService.findCountry(COUNTRY_IRAN_CODE);
		if (country == null) {
			country = new Country("Ø§ÛŒØ±Ø§Ù†", COUNTRY_IRAN_CODE);
			country.setCreatorUser(getUser(DBINITIALIZER_USER));
			country.setCreatedDateTime(DateTime.now());
			country.setAbbreviation("IR");
			GeneralDao.Instance.saveOrUpdate(country);
		}
		
		State state = CustomerService.findState(STATE_TEHRAN_CODE);
		if (state == null) {
			state = new State("ØªÙ‡Ø±Ø§Ù†", STATE_TEHRAN_CODE);
			state.setCreatorUser(getUser(DBINITIALIZER_USER));
			state.setCreatedDateTime(DateTime.now());
			state.setAbbreviation("024");
			state.setCountry(country);
			GeneralDao.Instance.saveOrUpdate(state);
		}

		City city = CustomerService.findCity(CITY_TEHRAN_CODE);
		if (city == null) {
			city = new City("ØªÙ‡Ø±Ø§Ù†", CITY_TEHRAN_CODE);
			city.setCreatorUser(getUser(DBINITIALIZER_USER));
			city.setCreatedDateTime(DateTime.now());
			city.setAbbreviation(CITY_TEHRAN_CODE.toString());
			city.setState(state);
			GeneralDao.Instance.saveOrUpdate(city);
		}
		contact.setAddress(new Address());
		contact.getAddress().setAddress(address);
		contact.getAddress().setCity(city);
		contact.getAddress().setState(state);
		contact.getAddress().setCountry(country);
		
		contact.setName(name);
		String phone = (Util.hasText(phoneNumber))? phoneNumber: "88208820";
		String web = (Util.hasText(webAddress))? webAddress: "www.bpi.ir";
		contact.setPhoneNumber(new PhoneNumber());
		contact.getPhoneNumber().setAreaCode("021");
		contact.getPhoneNumber().setNumber(phone);
		contact.setWebsite(new Website());
		contact.getWebsite().setWebsiteAddress(web);
		
		return contact;
	}

	
	public static MerchantCategory createMerchantCategory(Long code, String name, String enName, boolean isAssignable, MerchantCategory parentGroup) {
		MerchantCategory merchantCategory = getMerchantCategory(name);
		if (merchantCategory != null)
			return merchantCategory;
		merchantCategory = new MerchantCategory();
		merchantCategory.setCode(code);
		merchantCategory.setName(name);
		merchantCategory.setParentCategory(parentGroup);
		merchantCategory.setCreatedDateTime(DateTime.now());
		merchantCategory.setCreatorUser(DBInitializeUtil.getUser());
		merchantCategory.setEnglishName(enName);
		merchantCategory.setAssignable(isAssignable);
		merchantCategory.setVisible(true);
		GeneralDao.Instance.saveOrUpdate(merchantCategory);
		return merchantCategory;
	}

	static private MerchantCategory getMerchantCategory(String name) {
		String query = "from " + MerchantCategory.class.getName() + " ap where ap.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
		MerchantCategory merchantCategory = (MerchantCategory) GeneralDao.Instance.findObject(query, param);
		return merchantCategory;
	}
	
	
	static public ClearingProfile createPerDayClearingProfile(CycleSettlementJob job) {
		ClearingProfile clearingProfile = new ClearingProfile();
		
		if (job instanceof MerchantCycleSettlementJob) {
			clearingProfile.setName("ÛŒÚ© Ø¨Ø§Ø± Ø¯Ø± Ø±ÙˆØ² Ù�Ø±ÙˆØ´Ù†Ø¯Ù‡");
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.WITHDRAWAL.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BALANCEINQUIRY.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.setDocDesc("ØªØ³ÙˆÙŠÙ‡ Ù¾Ø°ÙŠØ±Ù†Ø¯Ú¯Ø§Ù† Ø¨Ø§ ÙˆØ¶Ø¹ÛŒØª Ù†Ø§ Ù…Ø´Ø®Øµ");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			stlCriteria = new SettlementDataCriteria(SettlementDataType.SECOND);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.WITHDRAWAL.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BALANCEINQUIRY.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("ØªØ³ÙˆÙŠÙ‡ Ù¾Ø°ÙŠØ±Ù†Ø¯Ú¯Ø§Ù† Ø¨Ø§ ÙˆØ¶Ø¹ÛŒØª Ù…ØªÙˆØ§Ø²Ù†");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			clearingProfile.setSettlementClass(MerchantSettlementServiceImpl.class);
			
		}
		else if (job instanceof BillPaymentCycleSettlementJob) {
			clearingProfile.setName("ÛŒÚ© Ø¨Ø§Ø± Ø¯Ø± Ø±ÙˆØ² Ù¾Ø±Ø¯Ø§Ø®Øª Ù‚Ø¨Ø¶");
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BILLPAYMENT.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("ØªØ³ÙˆÙŠÙ‡ Ù¾Ø±Ø¯Ø§Ø®Øª Ù‚Ø¨Ø¶");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			clearingProfile.setSettlementClass(BillPaymentSettlementServiceImpl.class);
			
		}
		else if (job instanceof CellChargeCycleSettlementJob) {
			clearingProfile.setName("ÛŒÚ© Ø¨Ø§Ø± Ø¯Ø± Ø±ÙˆØ² Ø´Ø§Ø±Ú˜ Ø§ÛŒØ±Ø§Ù†Ø³Ù„");
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASECHARGE.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("ØªØ³ÙˆÙŠÙ‡ Ø§ÙŠØ±Ø§Ù†Ø³Ù„");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			clearingProfile.setSettlementClass(ChargeSettlementServiceImpl.class);
			
		}
		
		else if (job instanceof ShetabCycleSettlementJob) {
			clearingProfile.setName("ÛŒÚ© Ø¨Ø§Ø± Ø¯Ø± Ø±ÙˆØ² Ø´ØªØ§Ø¨");
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASECHARGE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BILLPAYMENT.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BALANCEINQUIRY.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.WITHDRAWAL.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("ØªØ³ÙˆÛŒÙ‡ Ø­Ø³Ø§Ø¨ ØªØ±Ø§Ú©Ù†Ø´ Ù‡Ø§ÛŒ Ø´ØªØ§Ø¨");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);

			stlCriteria = new SettlementDataCriteria(SettlementDataType.SECOND);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.INCREMENTALTRANSFER.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.DECREMENTALTRANSFER.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("ØªØ³ÙˆÛŒÙ‡ Ø­Ø³Ø§Ø¨ ØªØ±Ø§Ú©Ù†Ø´ Ù‡Ø§ÛŒ Ø§Ù†ØªÙ‚Ø§Ù„ ÙˆØ¬Ù‡ Ø´ØªØ§Ø¨");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			clearingProfile.setSettlementClass(InstitutionSettlementServiceImpl.class);
		}
		
		else 
			clearingProfile.setName("Ø§Ù„Ú¯ÙˆÛŒ ØªØ³ÙˆÛŒÙ‡ Ø­Ø³Ø§Ø¨ ÛŒÚ© Ø¨Ø§Ø± Ø¯Ø± Ø±ÙˆØ²");
		
		clearingProfile.setCreatorUser(getUser(DBINITIALIZER_USER));
		clearingProfile.setCreatedDateTime(DateTime.now());
		
		CycleCriteria criteria = new CycleCriteria();
		criteria.setCycleType(CycleType.PER_MINUTE);
		criteria.setCycleCount(7);
		
		clearingProfile.setAccountTimeOffsetMinute(1);
		
		clearingProfile.setSettleTimeOffsetDay(0);
		clearingProfile.setSettleTimeOffsetHour(18);
		
		DateTime fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(23, 59, 59));
		clearingProfile.setFireTime(fireTime);
		
		clearingProfile.setCycleCriteria(criteria, job);
		
		GeneralDao.Instance.saveOrUpdate(clearingProfile);
		return clearingProfile;
	}
	
	
	}