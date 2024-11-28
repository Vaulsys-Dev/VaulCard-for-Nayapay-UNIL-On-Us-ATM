package vaulsys.initializer;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.entity.Contract;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.MerchantCategory;
import vaulsys.entity.impl.FinancialEntityGroup;
import vaulsys.entity.impl.FinancialEntitySharedFeature;
import vaulsys.entity.impl.Merchant;
import vaulsys.entity.impl.MerchantVersion;
import vaulsys.entity.impl.Shop;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.job.MerchantCycleSettlementJob;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.jceadapter.JCESecurityModule;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.util.encoders.Hex;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class DBInitializeAddingPos {

	private static final String MERCHANT_REPNAME = "Ù�Ù†Ø§Ù¾";
	private static final String MERCHANT_NAME = "ØªØ³Øª Ù�Ù†Ø§Ù¾";

	//POS Terminals
	public static final long POS_GENERAL_CODE = 4000;

	//POS Serial Number
	public static final long POS_SERIAL_NUMBER = 1030021976;
	
	//Shops
	public static final long POS_SHOP_CODE = 4000;
	
	public static final long NO_TERMINAL_CODE = -1;
	
	//Merchants
	public static final long MERCHANT_GENERAL_CODE = 10;
	
	//Merchant Addresses
	public static final String GENERAL_ADDRESS = "Ù…ÛŒØ±Ø¯Ø§Ù…Ø§Ø¯- Ù¾298- Ø·3";
	public static final String GENERAL_PHONE_NUMBER = "021-88208820";
	public static final String GENERAL_WEB_ADDRESS = "www.bpi.ir";
	
	//Accounts
	public static final String MERCHANT_ACCOUNT = "220-800-36994-1";
	public static final String MERCHANT_OWNERNAME = "Ù„ÛŒÙ„Ø§ Ù¾Ø§Ú©Ø±ÙˆØ§Ù† Ù†Ú˜Ø§Ø¯";
	
	public static final String SHOP_POS_ACCOUNT = "219-800-234582-1";
	public static final String SHOP_POS_OWNERNAME = "Ù…Ø­Ù…Ø¯ Ù†Ú˜Ø§Ø¯ØµØ¯Ø§Ù‚Øª";
	
	//Keys
	private static final String EPAY_MAC_KEY = "1111111111111111";
	
	//Config
	private static final int MAX_SHOP = 11000;
	private static final int MAX_POS_PER_SHOP = 1;
	private static final boolean AUTO_CODE = true;

	public static void main(String[] args) {
		DBInitializeAddingPos posInitializer = new DBInitializeAddingPos();
		for (int num = 0; num < (MAX_SHOP / 200) + 1; num++) {
			GeneralDao.Instance.beginTransaction();
			try {
				posInitializer.addToDB();
			} catch (Exception e) {
				e.printStackTrace();
				GeneralDao.Instance.rollback();
				System.exit(1);
			}
			GeneralDao.Instance.endTransaction();
		}
		System.exit(0);
	}

	private void addToDB() throws Exception {
		System.out.println("------- Creating Profiles -------");
		SecurityProfile securityProfileDefault = AddSecurityProfile.createDefaultSecurityProfile(AddSecurityProfile.DEFAULT_SECURITY_PROFILE_NAME);
		AuthorizationProfile authorizationProfileDefault = AddAuthorization.createAuthorizationProfile(AddAuthorization.DEFAULT_AUTHORIZATION_PROFILE_NAME);
		AuthorizationProfile authorizationProfile_POS_ServiceType = AddAuthorization.createAuthorizationProfile(AddAuthorization.POS_AUTHORIZATION_PROFILE_NAME); 
		ClearingProfile clearingProfilePerdayPOS = new AddClearingProfile().createPerDayClearingProfile(new MerchantCycleSettlementJob());
		FeeProfile feeProfileDefault = DBInitializeAddFee.createDefaultFeeProfile();

		System.out.println("------- Creating Financial Entity Group -------");
		FinancialEntityGroup rootFinancialEntityGroup = createFinancialEntityGroup(feeProfileDefault, authorizationProfileDefault, null, null, "Ù‡Ù…Ù‡ Ù…ÙˆØ¬ÙˆØ¯ÛŒØª Ù‡Ø§");
		FinancialEntityGroup merchantFinancialEntityGroup = createFinancialEntityGroup(feeProfileDefault, authorizationProfileDefault, null, rootFinancialEntityGroup, "Ù‡Ù…Ù‡ Ù¾Ø°ÛŒØ±Ù†Ø¯Ú¯Ø§Ù†");
		FinancialEntityGroup shopFinancialEntityGroup = DBInitializeUtil.createFinancialEntityGroup(feeProfileDefault,
				authorizationProfileDefault, null, merchantFinancialEntityGroup, "Ù‡Ù…Ù‡ Ù�Ø±ÙˆØ´Ú¯Ø§Ù‡ Ù‡Ø§");

		System.out.println("------- Creating Merchant Category -------");		
		MerchantCategory rootMerchantCategory = createMerchantCategory(1L, "Ù‡Ù…Ù‡ Ø§ØµÙ†Ø§Ù�", null);
		MerchantCategory merchantCategory = createMerchantCategory(3L, "ØªÙˆØ³Ø¹Ù‡", rootMerchantCategory);
		
		System.out.println("------- Creating Merchant -------");
		Merchant merchant = initMerchant(AUTO_CODE, ""+MERCHANT_GENERAL_CODE, feeProfileDefault, authorizationProfileDefault, MERCHANT_ACCOUNT, MERCHANT_OWNERNAME, merchantFinancialEntityGroup, merchantCategory);
		
		for (int i = 0; i < MAX_SHOP && i < 200; i++) {
			System.out.println("------- ROUND "+i+" -------");
			System.out.println("------- Creating SHOP -------");
			String repname = "ØªØ³Øª Ù�Ù†Ø§Ù¾" + "- " + i;
			String name = "ØªØ³Øª Ù�Ø±ÙˆØ´Ú¯Ø§Ù‡ ØªØ³ØªÛŒ Ù¾Ø§ÛŒØ§Ù†Ù‡ Ù�Ø±ÙˆØ´" + "- " + i;
			String shop_code = ""+ POS_SHOP_CODE + i;
			Shop shop = initShop(AUTO_CODE, shop_code, /*merchant*/null, feeProfileDefault,
					authorizationProfile_POS_ServiceType, name, repname, SHOP_POS_ACCOUNT, SHOP_POS_OWNERNAME,
					shopFinancialEntityGroup);
			System.out.println("------- Creating SHOP " + shop.getCode() + "-------");
			TerminalGroup terminalGroup = getTerminalGroup("Ù¾Ø§ÛŒØ§Ù†Ù‡ Ù‡Ø§ÛŒ Ù�Ø±ÙˆØ´");

			feeProfileDefault = DBInitializeAddFee.createDefaultFeeProfile();
			for (int j = 0; j < MAX_POS_PER_SHOP; j++) {
				System.out.println("------- Creating POS -------");
				long posCode = getgeneralPOSCode(i, j);
				posCode = POS_GENERAL_CODE + i;
				POSTerminal pos = initPOS(AUTO_CODE, posCode, shop.getCode().toString(), POS_SERIAL_NUMBER + j + "",
						MERCHANT_GENERAL_CODE, feeProfileDefault, securityProfileDefault,
						authorizationProfile_POS_ServiceType, clearingProfilePerdayPOS, terminalGroup
								.getSafeSharedFeature().getChargePolicy(), terminalGroup);
				System.out.println("------- Creating POS " + pos.getCode() + "-------");
			}
		}
		
		System.out.println("------- FINISHED -------");
	}

	private long getgeneralPOSCode(int shopIndex, int posIndex) {
		return POS_GENERAL_CODE*(shopIndex+1)+ posIndex;
	}

	private TerminalGroup getTerminalGroup(String name) {
		String query = "from " + TerminalGroup.class.getName() + " ap where ap.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
		TerminalGroup terminalGroup = (TerminalGroup) GeneralDao.Instance.findObject(query, param);
		return terminalGroup;
	}

	private POSTerminal initPOS(Boolean auto_code, long pos_code, String shop_code, String pos_serial, long merchant_code, FeeProfile feeProfile, SecurityProfile securityProfile, AuthorizationProfile authorizationProfile, ClearingProfile clearingProfile, ChargeAssignmentPolicy chargeAssignmentPolicy, TerminalGroup terminalGroup) throws Exception {
		Shop shop = FinancialEntityService.findEntity(Shop.class, shop_code);
		
		POSTerminal posTerminal = null;
		if (!auto_code)
			TerminalService.findTerminal(POSTerminal.class, pos_code);
		
		if (posTerminal == null) {
			posTerminal = createPOSTerminal(pos_code, auto_code);
		}
		
		posTerminal.setCreatorUser(DBInitializeUtil.getUser());
		posTerminal.setCreatedDateTime(DateTime.now());
		
		posTerminal.setParentGroup(terminalGroup);
		posTerminal.setOwner(shop);
		posTerminal.setSerialno(pos_serial);
		posTerminal.setAuthorizationProfile(authorizationProfile);
		posTerminal.setSharedFeature(terminalGroup.getSafeSharedFeature());
		posTerminal.setClearingProfile(clearingProfile);
		GeneralDao.Instance.saveOrUpdate(posTerminal);
		return posTerminal;
	}
	
	private POSTerminal createPOSTerminal(long pos_code, Boolean auto_code) throws Exception {
		POSTerminal posTerminal = new POSTerminal();
		if (!auto_code)
			posTerminal.setCode(pos_code);
		
		createKeySet(posTerminal, EPAY_MAC_KEY, EPAY_MAC_KEY, null);
		
		GeneralDao.Instance.saveOrUpdate(posTerminal);
		return posTerminal;
	}

	public static Shop initShop(Boolean auto_code, String shop_code, Merchant merchant, FeeProfile feeProfile, AuthorizationProfile authorizationProfile, String name, String repname, String accountId, String holder, FinancialEntityGroup parentGroup) throws Exception {
		Shop shop = null;
		if (!auto_code)
			shop = FinancialEntityService.findEntity(Shop.class, shop_code);
		
		if (shop == null) {
			shop = createShop(feeProfile, authorizationProfile, name, repname, accountId, holder);
//			ShopVersion v = new ShopVersion();
//			v.setCreatorUser(DBInitializeUtil.getUser());
//			v.setCreatedDateTime(DateTime.now());
//			v.setParent(shop);
			if (!auto_code)
				shop.setCode(Long.parseLong(shop_code));
			
//			GeneralDao.Instance.saveOrUpdate(v);
//			GeneralDao.Instance.saveOrUpdate(shop);
		}
		shop.setParentGroup(parentGroup);
		shop.setSharedFeature(parentGroup.getSafeSharedFeature());
		if(merchant != null)
			shop.setOwner(merchant);
		GeneralDao.Instance.saveOrUpdate(shop);
		return shop;
	}

	public static Merchant initMerchant(Boolean auto_code, String merchant_code, FeeProfile feeProfile, AuthorizationProfile authorizationProfile, String accountId, String holder, FinancialEntityGroup parentGroup, MerchantCategory category) {
		Merchant merchant = null;
		if (!auto_code)
			merchant = FinancialEntityService.findEntity(Merchant.class, merchant_code);
		
		if (merchant != null)
			return merchant;
		
		merchant = createMerchant(feeProfile, authorizationProfile, accountId, holder);
		MerchantVersion v = new MerchantVersion();
		v.setCreatorUser(DBInitializeUtil.getUser());
		v.setCreatedDateTime(DateTime.now());
		v.setParent(merchant);
		if (!auto_code)
			merchant.setCode(Long.parseLong(merchant_code));
		
		merchant.setCategory(category);
		merchant.setSharedFeature(parentGroup.getSafeSharedFeature());
		merchant.setParentGroup(parentGroup);
		GeneralDao.Instance.saveOrUpdate(v);
		GeneralDao.Instance.saveOrUpdate(merchant);
		return merchant;
	}
	
	public static Merchant createMerchant(FeeProfile feeProfile, AuthorizationProfile authorizationProfile, String accountId, String holder) {
		Merchant merchant = new Merchant();
		merchant.setContract(new Contract());
		merchant.setEnabled(true);
		merchant.setAccount(DBInitializeUtil.createAccount(accountId, holder));
		merchant.setFeeProfile(feeProfile);
		merchant.setAuthorizationProfile(authorizationProfile);
		merchant.setContact(DBInitializeUtil.createContact(MERCHANT_REPNAME, GENERAL_ADDRESS, GENERAL_PHONE_NUMBER, GENERAL_WEB_ADDRESS));
		merchant.setName(MERCHANT_NAME);
		
		return merchant;
	}
	
	public static Shop createShop(FeeProfile feeProfile, AuthorizationProfile authorizationProfile, String name, String repname, String accountId, String holder) {
		Shop shop = new Shop();
		shop.setEnabled(true);
		shop.setAccount(DBInitializeUtil.createAccount(accountId, holder));
		shop.setFeeProfile(feeProfile);
		shop.setAuthorizationProfile(authorizationProfile);
		shop.setContact(DBInitializeUtil.createContact(repname,GENERAL_ADDRESS, GENERAL_PHONE_NUMBER, GENERAL_WEB_ADDRESS));
		shop.setName(name);
		return shop;
	}
	
	
	private void createKeySet(Terminal terminal, String macKey, String pinKey, String masterKey) throws Exception {
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
			Key pkey = new SecretKeySpec(Hex.decode(masterKey), "DESede");
			SecureDESKey keyPin = ssm.encryptToLMK(SMAdapter.LENGTH_DES3_3KEY, KeyType.TYPE_TMK, pkey);
			terminal.addSecureKey(keyPin);
			GeneralDao.Instance.saveOrUpdate(keyPin);
		}

		GeneralDao.Instance.saveOrUpdate(terminal);
	}


	private FinancialEntityGroup createFinancialEntityGroup(FeeProfile feeProfile, AuthorizationProfile authorizationProfile,
			ClearingProfile clearingProfile, FinancialEntityGroup parentGroup, String name) {
		
		FinancialEntityGroup group = new FinancialEntityGroup();
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
		group.setCreatorUser(DBInitializeUtil.getUser());
		group.setCreatedDateTime(DateTime.now());
		
		GeneralDao.Instance.saveOrUpdate(group);
		
		return group;
	}
	
	private MerchantCategory createMerchantCategory(Long code, String name, MerchantCategory parentGroup) {
		MerchantCategory merchantCategory = getMerchantCategory(name);
		if (merchantCategory != null)
			return merchantCategory;
		merchantCategory = new MerchantCategory();
		merchantCategory.setCode(code);
		merchantCategory.setName(name);
		merchantCategory.setParentCategory(parentGroup);
		GeneralDao.Instance.saveOrUpdate(merchantCategory);
		return merchantCategory;
	}
	
	private MerchantCategory getMerchantCategory(String name) {
		String query = "from " + MerchantCategory.class.getName() + " ap where ap.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
		MerchantCategory merchantCategory = (MerchantCategory) GeneralDao.Instance.findObject(query, param);
		return merchantCategory;
	}
}
