package vaulsys.initializer;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.MerchantCategory;
import vaulsys.entity.impl.FinancialEntityGroup;
import vaulsys.entity.impl.Merchant;
import vaulsys.entity.impl.Organization;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.lottery.Lottery;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.lottery.LotteryCriteria;
import vaulsys.lottery.consts.LotteryState;
import vaulsys.lottery.impl.GeneralLotteryAssignmentPolicy;
import vaulsys.mtn.MTNChargeSpecification;
import vaulsys.mtn.impl.GeneralChargeAssignmentPolicy;
import vaulsys.mtn.impl.RandomChargeAssignmentPolicy;
import vaulsys.mtn.impl.RandomChargePolicyData;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.job.ATMCycleSettlementJob;
import vaulsys.scheduler.job.BillPaymentCycleSettlementJob;
import vaulsys.scheduler.job.CellChargeCycleSettlementJob;
import vaulsys.scheduler.job.MerchantCycleSettlementJob;
import vaulsys.scheduler.job.OnlineCycleSettlementJob;
import vaulsys.scheduler.job.ShetabCycleSettlementJob;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.RSAPublicKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.TerminalGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasargadBank_DBInitialize {

	public static final long PASARGAD_BIN = 502229;

	// Institutions
	public static final long INST_NEGIN_CODE = 639347;
	public static final long INST_MYSELF_CODE = 502229;
	public static final long INST_SHETAB_CODE = 9000;
	public static final long INST_EPAY_CODE = 2;
	public static final long INST_FNP_EPAY_CODE = 3;

	public static final long INST_SIMULATOR_CODE = 5;

	// Switch Terminals
	public static final long NEGIN_ISSUER_TERMINAL_CODE = 112;
	public static final long NEGIN_ACQUIER_TERMINAL_CODE = 113;

	public static final long SHETAB_ACQUIER_TERMINAL_CODE = 212;
	public static final long SHETAB_ISSUER_TERMINAL_CODE = 213;

	public static final long MYSELF_ACQUIER_TERMINAL_CODE = 2;
	public static final long MYSELF_ISSUER_TERMINAL_CODE = 3;

	public static final long EPAY_TERMINAL_CODE = 114;
	public static final long EPAY_FNP_TERMINAL_CODE = 115;

	public static final long SIMULATOR_ACQUIER_TERMINAL_CODE = 116;
	public static final long SIMULATOR_ISSUER_TERMINAL_CODE = 117;

	// POS Terminals
	public static final long POS_GENERAL_CODE = 444;
	// public static final long POS_GENERAL_CODE = 100;

	// POS Serial Number
	public static final String POS_SERIAL_NUMBER = "1030021976";

	// EPAY_FNP Terminals
	public static final long EPAY_SHOP_TERMINAL_CODE = 1;

	// Shops
	public static final long EPAY_FNP_SHOP_CODE = 1024;
	// public static final long POS_SHOP_CODE = 100;
	public static final long POS_SHOP_CODE = 4444;

	public static final long NO_TERMINAL_CODE = -1;

	// Charges
	public static final long PARSNIKATEL_CODE = 9935;
	public static final long IRANCELL_CODE = 8936;

	// Merchants
	public static final long MERCHANT_GENERAL_CODE = 100000;

	// Merchant Addresses
	public static final String GENERAL_ADDRESS = "میرداماد- پ298- ط3";

	/*** ATM Terminal & Branch ***/
	public static final long ATM_GENERAL_CODE = 203003;
	public static final long BRANCH_GENERAL_CODE = 203217;
	public static final String BRANCH_GENERAL_NAME = "امام حسن";
	public static final String CORE_BRANCH_CODE = "323";

	public static final long ATM_CODE = 204972;
	public static final long BRANCH_CODE = 204901;
	public static final String BRANCH_NAME = "تست فناپ";
	public static final String CORE_CODE = "444";

	public static final String ATM_CONFIG_FILE = "c:/PasargadATMConfig.2009-07-28.xlsx";

	// Accounts
	public static final String MERCHANT_ACCOUNT = "220-800-36994-1";
	public static final String MERCHANT_OWNERNAME = "لیلا پاکروان نژاد";

	public static final String SHOP_POS_ACCOUNT = "219-800-234582-1";
	public static final String SHOP_POS_OWNERNAME = "محمد نژادصداقت";

	public static final String SHOP_EPAY_FNP_ACCOUNT = "219-10-44039-1";
	public static final String SHOP_EPAY_FNP_OWNERNAME = "محمدعلی فرداد";

	// Keys
	private static final String My_MAC_KEY = "1111111111111111";
	private static final String My_PIN_KEY = "1111111111111111";
	private static final String NEGIN_KEY = "1B1B1B1B1B1B1B1B";
	private static final String SHETAB_KEY = "1C1C1C1C1C1C1C1C";
	private static final String EPAY_MAC_KEY = "1111111111111111";
	private static final String EPAY_PIN_KEY = "1111111111111111";

	private static final String SIMULATOR_KEY = "1111111111111111";
	private static final String SIMULATOR_MASTER_KEY = "1111111111111111";

	private static final String EPAY_FNP_MASTER_KEY = "22232425262728292A2B2C2D2E2F30313233343536373839";
	private static final String EPAY_FNP_TERMINAL_PUBLIC_KEY = "30819F300D06092A864886F70D010101050003818D0030818902818100B58E92671C3B1F21686E8D3635D0197479F55BD97C742DBF876DDDB7586B21BB84C2E68663B83B665497DB64522CBB392BA837E6045C4F8CC9DA531488528B5685F5A83130A66601A83D7913F8BDEEAFF0FB0454A9FFF7C1973FD9A3E93677E5AF08748B9497AB259AD444824F185CD732B4FD1B1F321DFA1F7BF0F3620F97070203010001";

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new PasargadBank_DBInitialize().cereateDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();

		System.out.println("------- Creating Cycle Account Job -------");
		GeneralDao.Instance.beginTransaction();
		try {
			new PasargadBank_DBInitialize().createCycleAccountJob();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}

		GeneralDao.Instance.endTransaction();

		System.exit(0);
	}

	private void cereateDB() throws Exception {
		System.out.println("------- Creating BANKS -------");
		DBInitializeUtil.createBanks();

		System.out.println("------- Creating Profiles -------");

		Map<String, Map<String, String>> sucurityFunctions = new HashMap<String, Map<String, String>>() {
			{
				put(SecurityComponent.FUNC_MAC_GEN, new HashMap<String, String>() {
					{
						put("Algorithm", "0");
						put("MacLength", "8");
						put("Padding", "ZeroPadding");
					}
				});
				put(SecurityComponent.FUNC_MAC_VER, new HashMap<String, String>() {
					{
						put("Algorithm", "0");
						put("MacLength", "8");
						put("Padding", "ZeroPadding");
						put("SkipLength", "16");
					}
				});
				put(SecurityComponent.FUNC_TRANSLATEPIN, new HashMap<String, String>() {
					{
						put("PIN Format", "01");
						put("AccountNumber Length", "12");
					}
				});
			}
		};

		SecurityProfile securityProfileDefault = AddSecurityProfile.createSecurityProfile(
				DBInitializeUtil.DEFAULT_SECURITY_PROFILE_NAME, sucurityFunctions);

		AuthorizationProfile authorizationProfileDefault = AddAuthorization.createAuthorizationProfile(AddAuthorization.DEFAULT_AUTHORIZATION_PROFILE_NAME);
		AuthorizationProfile authorizationProfile_EPAY = AddAuthorization.createAuthorizationProfile(AddAuthorization.EPAY_AUTHORIZATION_PROFILE_NAME);
		AuthorizationProfile authorizationProfile_POS = AddAuthorization.createAuthorizationProfile(AddAuthorization.POS_AUTHORIZATION_PROFILE_NAME);
		AuthorizationProfile authorizationProfile_SIMULATOR = AddAuthorization.createAuthorizationProfile(AddAuthorization.SIMULATOR_AUTHORIZATION_PROFILE_NAME);

		System.out.println("------- Creating Policies -------");
		AddAuthorization initializer = new AddAuthorization();
		
		initializer.addPanPrefixTransactionPolicy(authorizationProfile_POS,
				new ArrayList<Integer>(){{
					add(new Long(PASARGAD_BIN).intValue());
					add(new Long(INST_NEGIN_CODE).intValue());
			}});
		initializer.addPanPrefixTransactionPolicy(authorizationProfile_EPAY,
				new ArrayList<Integer>(){{
					add(new Long(PASARGAD_BIN).intValue());
					add(new Long(INST_NEGIN_CODE).intValue());
			}});
		initializer.addNeginPanPrefixTransactionPolicy(authorizationProfile_SIMULATOR);
		
		initializer.addFITControlPolicy(authorizationProfile_POS);
		initializer.addFITControlPolicy(authorizationProfile_EPAY);
		
		initializer.addPOSTerminalServicePolicy(authorizationProfile_POS);
		initializer.addEpayTerminalServicePolicy(authorizationProfile_EPAY);

		
		JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);
		ClearingProfile clearingProfilePerdayPOS = new AddClearingProfile().createPerDayClearingProfile(new MerchantCycleSettlementJob());
		new AddClearingProfile().createPerDayClearingProfile(new OnlineCycleSettlementJob());
		ClearingProfile clearingProfileATM = new AddClearingProfile().createPerDayClearingProfile(new ATMCycleSettlementJob());
		ClearingProfile clearingProfilePerdayBILL = new AddClearingProfile().createPerDayClearingProfile(new BillPaymentCycleSettlementJob());
		ClearingProfile clearingProfilePerdayCELL = new AddClearingProfile().createPerDayClearingProfile(new CellChargeCycleSettlementJob());
		ClearingProfile clearingProfilePerdayShetab = new AddClearingProfile().createPerDayClearingProfile(new ShetabCycleSettlementJob());

		FeeProfile feeProfileDefault = DBInitializeAddFee.createDefaultFeeProfile();
		FeeProfile feeProfileOrg = DBInitializeAddFee.createOrgFeeProfile();
		FeeProfile feeProfileMerch = DBInitializeAddFee.createMerchantFeeProfile();

		System.out.println("------- Creating ChargeAssingment Policy -------");
		GeneralChargeAssignmentPolicy chargeAssignmentPolicy = new GeneralChargeAssignmentPolicy();
		// RandomChargeAssignmentPolicy randomChargeAssignmentPolicy =
		// createRandomChargeAssignmentPolicy();
		getGeneralDao().saveOrUpdate(chargeAssignmentPolicy);

		/*
		 * System.out.println("------- Creating LotteryAssingment Policy -------"
		 * ); GeneralLotteryAssignmentPolicy lotteryAssignmentPolicy =
		 * createGeneralLotteryAssignmentPolicy();
		 * 
		 * System.out.println("------- Creating Lottery -------"); List<Lottery>
		 * lotteryList = createLottery(lotteryAssignmentPolicy);
		 */

		System.out.println("------- Creating Terminal Groups -------");
		TerminalGroup rootTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, null, "همه ترمینالها",chargeAssignmentPolicy);
		
		TerminalGroup switchTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, rootTerminalGroup, "سوئیچ ها", null);
		
		TerminalGroup epayTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, clearingProfilePerdayPOS, switchTerminalGroup,
				"پرداخت اینترنتی", null);
		
		TerminalGroup neginTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, switchTerminalGroup, "نگین", null);
		
		TerminalGroup posTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, clearingProfilePerdayPOS, rootTerminalGroup,
				"پایانه های فروش", null);
		
		TerminalGroup orgTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, rootTerminalGroup,
				"ترمينالهای مجازی ThirdParty", null);
		
		TerminalGroup atmTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, clearingProfileATM, rootTerminalGroup,
				"خودپردازها", null);

		System.out.println("------- Creating Financial Entity Groups -------");
		FinancialEntityGroup rootFinancialEntityGroup = DBInitializeUtil.createFinancialEntityGroup(feeProfileDefault,
				authorizationProfileDefault, null, null, "همه موجودیت ها");
		FinancialEntityGroup merchantFinancialEntityGroup = DBInitializeUtil.createFinancialEntityGroup(
				feeProfileDefault, authorizationProfileDefault, null, rootFinancialEntityGroup, "همه پذیرندگان");
		FinancialEntityGroup organizationFinancialEntityGroup = DBInitializeUtil.createFinancialEntityGroup(
				feeProfileDefault, authorizationProfileDefault, clearingProfilePerdayBILL, rootFinancialEntityGroup,
				"همه سازمانها");
		FinancialEntityGroup shopFinancialEntityGroup = DBInitializeUtil.createFinancialEntityGroup(feeProfileDefault,
				authorizationProfileDefault, null, merchantFinancialEntityGroup, "همه فروشگاه ها");

		System.out.println("------- Creating Merchant Categories -------");
//		MerchantCategory rootMerchantCategory = DBInitializeUtil.createMerchantCategory(1L, "همه اصناف", null);
//		MerchantCategory merchantCategory = DBInitializeUtil.createMerchantCategory(3L, "توسعه", rootMerchantCategory);
		
		System.out.println("------- Creating Organization -------");
		AddOrganization addOrganization = new AddOrganization();
		addOrganization.addBillPaymentOrg(clearingProfilePerdayBILL, feeProfileOrg, organizationFinancialEntityGroup,orgTerminalGroup);
		addOrganization.addCellChargeOrg(clearingProfilePerdayCELL, feeProfileOrg, organizationFinancialEntityGroup,orgTerminalGroup);

		System.out.println("------- Creating MySelf Institution -------");
		
		AddInstitution.initMyInst(PASARGAD_BIN, PASARGAD_BIN, MYSELF_ACQUIER_TERMINAL_CODE, MYSELF_ISSUER_TERMINAL_CODE, switchTerminalGroup, securityProfileDefault, authorizationProfileDefault, null, feeProfileDefault, null, My_MAC_KEY, My_PIN_KEY, null, "سوئیچ فناپ", "سوئیچ فناپ");

		System.out.println("------- Creating Shetab Institution as Master -------");
	/*	AddInstitution.initGeneralInst(INST_SHETAB_CODE, INST_SHETAB_CODE, FinancialEntityRole.MASTER, SHETAB_ACQUIER_TERMINAL_CODE,
				SHETAB_ISSUER_TERMINAL_CODE, switchTerminalGroup, securityProfileDefault, authorizationProfileDefault,
				clearingProfilePerdayShetab, feeProfileDefault, null, SHETAB_KEY, SHETAB_KEY, SHETAB_KEY, "سوئیچ شتاب",
				"سوئیچ شتاب");*/

		System.out.println("------- Creating Negin Institution as Peer -------");
/*		AddInstitution.initGeneralInst(INST_NEGIN_CODE, INST_NEGIN_CODE, FinancialEntityRole.PEER, NEGIN_ACQUIER_TERMINAL_CODE,
				NEGIN_ISSUER_TERMINAL_CODE, neginTerminalGroup, securityProfileDefault, authorizationProfileDefault,
				null, feeProfileDefault, null, NEGIN_KEY, NEGIN_KEY, null, "سوئیچ نگین", "سوئیچ نگین");
*/
		System.out.println("------- Creating Epayment_Institution as Slave -------");
	/*	AddInstitution.initGeneralInst(INST_EPAY_CODE, PASARGAD_BIN, FinancialEntityRole.SLAVE, EPAY_TERMINAL_CODE, NO_TERMINAL_CODE,
				epayTerminalGroup, securityProfileDefault, authorizationProfileDefault, null, feeProfileDefault, null,
				EPAY_MAC_KEY, EPAY_PIN_KEY, null, "سايت اینترنتی پاسارگاد", "سايت اينترنتی (IPG) قدیم");
*/
		System.out.println("------- Creating Fanap Epayment_Institution as Slave -------");
	/*	AddInstitution.initGeneralInst(INST_FNP_EPAY_CODE, PASARGAD_BIN, FinancialEntityRole.SLAVE, EPAY_FNP_TERMINAL_CODE,
				NO_TERMINAL_CODE, epayTerminalGroup, securityProfileDefault, authorizationProfile_EPAY, null,
				feeProfileDefault, chargeAssignmentPolicy, null, null, EPAY_FNP_MASTER_KEY, "سايت اينترنتی (IPG) جدید",
				"سايت اینترنتی پاسارگاد");
*/
/*		AddInstitution.initGeneralInst(INST_SIMULATOR_CODE, PASARGAD_BIN, FinancialEntityRole.SLAVE, SIMULATOR_ACQUIER_TERMINAL_CODE,
				SIMULATOR_ISSUER_TERMINAL_CODE, epayTerminalGroup, securityProfileDefault,
				authorizationProfile_SIMULATOR, null, feeProfileDefault, chargeAssignmentPolicy, SIMULATOR_KEY,
				SIMULATOR_KEY, SIMULATOR_MASTER_KEY, "سیمولاتور", "سیمولاتور");

		System.out.println("------- Creating Merchant -------");
		Merchant merchant = DBInitializeAddingPos.
		initMerchant(false, MERCHANT_GENERAL_CODE, feeProfileDefault,authorizationProfileDefault, MERCHANT_ACCOUNT, MERCHANT_OWNERNAME, merchantFinancialEntityGroup, merchantCategory);

		System.out.println("------- Creating SHOP -------");
		DBInitializeAddingPos.initShop(false, POS_SHOP_CODE, merchant, feeProfileMerch, authorizationProfile_POS,
				"فروشگاه تستی پایانه فروش", "فناپ", SHOP_POS_ACCOUNT, SHOP_POS_OWNERNAME, shopFinancialEntityGroup);
		DBInitializeAddingPos.initShop(false, EPAY_FNP_SHOP_CODE, merchant, feeProfileDefault, authorizationProfile_EPAY,
				"فروشگاه اینترنتی", "فناپ", SHOP_EPAY_FNP_ACCOUNT, SHOP_EPAY_FNP_OWNERNAME, shopFinancialEntityGroup);
*/
		System.out.println("------- Creating POS -------");
		DBInitializeUtil.initPOS(POS_GENERAL_CODE, ""+POS_SHOP_CODE, POS_SERIAL_NUMBER, MERCHANT_GENERAL_CODE,
				feeProfileDefault, securityProfileDefault, authorizationProfile_POS, clearingProfilePerdayPOS,
				chargeAssignmentPolicy, posTerminalGroup);

		System.out.println("------- Creating EPAY -------");
		RSAPublicKey publicKey = new RSAPublicKey(SMAdapter.LENGTH_RSA_PUBLIC_1024, SMAdapter.TYPE_TMK,
				EPAY_FNP_TERMINAL_PUBLIC_KEY);
		DBInitializeUtil.initEPay(EPAY_SHOP_TERMINAL_CODE, ""+EPAY_FNP_SHOP_CODE, publicKey, feeProfileDefault, securityProfileDefault,
				authorizationProfile_EPAY, clearingProfilePerdayPOS, chargeAssignmentPolicy, epayTerminalGroup);

		// System.out.println("------- Creating MTN Charge Specification -------");
		// initMTNChargeSpecification();

		System.out.println("------- Creating ATM -------");
		try {
			new ATMPasargadConfigImpl().addConfig();
			new ReadConfigData().startFromExcel(ATM_CONFIG_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		new DBInitializeATM().initATM(ATM_GENERAL_CODE, BRANCH_GENERAL_CODE, CORE_BRANCH_CODE, BRANCH_GENERAL_NAME);
		new DBInitializeATM().initATM(ATM_CODE, BRANCH_CODE, CORE_CODE, BRANCH_NAME);

		System.out.println("------- FINISHED -------");
	}

	private void createCycleAccountJob() throws Exception {
		JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);
		new AddClearingProfile().createCycleAccountJob();
	}

	private void initMTNChargeSpecification() {
		Organization parsnikatel = FinancialEntityService.findEntity(Organization.class, ""+PARSNIKATEL_CODE);
		Organization irancell = FinancialEntityService.findEntity(Organization.class, ""+IRANCELL_CODE);

		MTNChargeSpecification chargeSpecification = new MTNChargeSpecification(20000L, 200L, parsnikatel);
		getGeneralDao().saveOrUpdate(chargeSpecification);
		chargeSpecification = new MTNChargeSpecification(50000L, 500L, parsnikatel);
		getGeneralDao().saveOrUpdate(chargeSpecification);
		chargeSpecification = new MTNChargeSpecification(100000L, 1000L, parsnikatel);
		getGeneralDao().saveOrUpdate(chargeSpecification);
		chargeSpecification = new MTNChargeSpecification(200000L, 2000L, parsnikatel);
		getGeneralDao().saveOrUpdate(chargeSpecification);

		chargeSpecification = new MTNChargeSpecification(20000L, 200L, irancell);
		getGeneralDao().saveOrUpdate(chargeSpecification);
		chargeSpecification = new MTNChargeSpecification(50000L, 500L, irancell);
		getGeneralDao().saveOrUpdate(chargeSpecification);
		chargeSpecification = new MTNChargeSpecification(100000L, 1000L, irancell);
		getGeneralDao().saveOrUpdate(chargeSpecification);
		chargeSpecification = new MTNChargeSpecification(200000L, 2000L, irancell);
		getGeneralDao().saveOrUpdate(chargeSpecification);
	}

	private GeneralLotteryAssignmentPolicy createGeneralLotteryAssignmentPolicy() {
		GeneralLotteryAssignmentPolicy policy = new GeneralLotteryAssignmentPolicy();

		LotteryCriteria criteria = new LotteryCriteria(
				"ifx.TrnType == vaulsys.protocols.ifx.enums.TrnType.PURCHASE && ifx.Auth_Amt > 50000 && (ifx.DestBankId == 502229 || ifx.DestBankId == 639347) && rnd > 0.9",
				"200000", "", 1);
		getGeneralDao().saveOrUpdate(criteria);
		policy.addCriterias(criteria);

		criteria = new LotteryCriteria(
				"ifx.TrnType == vaulsys.protocols.ifx.enums.TrnType.PURCHASE && ifx.Auth_Amt > 50000 && (ifx.DestBankId == 502229 || ifx.DestBankId == 639347) && rnd > 0.8",
				"100000", "", 2);
		getGeneralDao().saveOrUpdate(criteria);
		policy.addCriterias(criteria);

		criteria = new LotteryCriteria(
				"ifx.TrnType == vaulsys.protocols.ifx.enums.TrnType.PURCHASE && ifx.Auth_Amt > 50000 && (ifx.DestBankId == 502229 || ifx.DestBankId == 639347) && rnd > 0.7",
				"50000", "", 3);
		getGeneralDao().saveOrUpdate(criteria);
		policy.addCriterias(criteria);

		getGeneralDao().saveOrUpdate(policy);
		return policy;

	}

	private List<Lottery> createLottery(LotteryAssignmentPolicy lotteryAssignmentPolicy) {
		Long card = 6393473000000040L;
		List<Lottery> lotteryList = new ArrayList<Lottery>();
		for (int i = 0; i < 10; i++) {
			Lottery lottery = new Lottery(card++, 200000L, LotteryState.NOT_ASSIGNED, lotteryAssignmentPolicy);
			getGeneralDao().saveOrUpdate(lottery);
			lotteryList.add(lottery);
		}

		card = 6393473000000050L;
		lotteryList = new ArrayList<Lottery>();
		for (int i = 0; i < 10; i++) {
			Lottery lottery = new Lottery(card++, 100000L, LotteryState.NOT_ASSIGNED, lotteryAssignmentPolicy);
			getGeneralDao().saveOrUpdate(lottery);
			lotteryList.add(lottery);
		}

		card = 6393473000000060L;
		lotteryList = new ArrayList<Lottery>();
		for (int i = 0; i < 10; i++) {
			Lottery lottery = new Lottery(card++, 50000L, LotteryState.NOT_ASSIGNED, lotteryAssignmentPolicy);
			getGeneralDao().saveOrUpdate(lottery);
			lotteryList.add(lottery);
		}
		return lotteryList;
	}

	private RandomChargeAssignmentPolicy createRandomChargeAssignmentPolicy() {
		RandomChargeAssignmentPolicy policy = new RandomChargeAssignmentPolicy();

		RandomChargePolicyData policyData = createRandomPolicyData();
		policy.setPolicyData(policyData);
		getGeneralDao().saveOrUpdate(policy);
		return policy;
	}

	private RandomChargePolicyData createRandomPolicyData() {
		Map<Organization, Integer> portions = new HashMap<Organization, Integer>();
		Organization nikatel = getGeneralDao().getObject(Organization.class, 8936L);
		Organization irancell = getGeneralDao().getObject(Organization.class, 9935L);

		portions.put(irancell, 50);
		portions.put(nikatel, 50);

		RandomChargePolicyData policyData = new RandomChargePolicyData(portions);
		getGeneralDao().saveOrUpdate(policyData);
		return policyData;
	}

	private GeneralDao getGeneralDao() {
		return GeneralDao.Instance;
	}
}
