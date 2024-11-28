package vaulsys.initializer;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.authorization.policy.FITControlPolicy;
import vaulsys.authorization.policy.PanPrefixTransactionPolicy;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.MerchantCategory;
import vaulsys.entity.impl.FinancialEntityGroup;
import vaulsys.entity.impl.Merchant;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.mtn.impl.GeneralChargeAssignmentPolicy;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.job.ATMCycleSettlementJob;
import vaulsys.scheduler.job.BillPaymentCycleSettlementJob;
import vaulsys.scheduler.job.MerchantCycleSettlementJob;
import vaulsys.scheduler.job.ShetabCycleSettlementJob;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.securekey.RSAPublicKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.TerminalGroup;

import java.util.ArrayList;

public class TaavonBank_DBInitialize {
	public static final long TAVON_BIN = 502908;
	public static final String BANK_NAME = "توسعه تعاون";

	// Institutions
	public static final long INST_MYSELF_CODE = TAVON_BIN;
	public static final long INST_SHETAB_CODE = 9000;
	public static final long INST_FNP_EPAY_CODE = 3;


	public static final long SHETAB_ACQUIER_TERMINAL_CODE = 212;
	public static final long SHETAB_ISSUER_TERMINAL_CODE = 213;

	public static final long MYSELF_ACQUIER_TERMINAL_CODE = 2;
	public static final long MYSELF_ISSUER_TERMINAL_CODE = 3;

	public static final long EPAY_FNP_TERMINAL_CODE = 115;

	// POS Terminals
	public static final long POS_GENERAL_CODE = 444;

	// POS Serial Number
	public static final String POS_SERIAL_NUMBER = "1030021976";

	// EPAY_FNP Terminals
	public static final long EPAY_SHOP_TERMINAL_CODE = 1;

	// Shops
	public static final long EPAY_FNP_SHOP_CODE = 1024;
	public static final long POS_SHOP_CODE = 4444;

	public static final long NO_TERMINAL_CODE = -1;

	// Merchants
	public static final long MERCHANT_GENERAL_CODE = 100000;

	// Merchant Addresses
	public static final String GENERAL_ADDRESS = "میرداماد- پ298- ط3";

	/*** ATM Terminal & Branch ***/
	public static final long ATM_CODE = 204972;
	public static final long BRANCH_CODE = 204901;
	public static final String BRANCH_NAME = "تست فناپ";
	public static final String CORE_CODE = "444";

	public static final String ATM_CONFIG_FILE = "C:/Documents and Settings/Administrator/My Documents/ATM-Config/PasargadATMConfig.2010-03-29.xlsx";

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
	
	private static final String SHETAB_KEY = "1C1C1C1C1C1C1C1C";
	
	private static final String EPAY_FNP_MASTER_KEY = "22232425262728292A2B2C2D2E2F30313233343536373839";
	private static final String EPAY_FNP_TERMINAL_PUBLIC_KEY = "30819F300D06092A864886F70D010101050003818D0030818902818100B58E92671C3B1F21686E8D3635D0197479F55BD97C742DBF876DDDB7586B21BB84C2E68663B83B665497DB64522CBB392BA837E6045C4F8CC9DA531488528B5685F5A83130A66601A83D7913F8BDEEAFF0FB0454A9FFF7C1973FD9A3E93677E5AF08748B9497AB259AD444824F185CD732B4FD1B1F321DFA1F7BF0F3620F97070203010001";

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new TaavonBank_DBInitialize().cereateDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();

		System.out.println("------- Creating Cycle Account Job -------");
		GeneralDao.Instance.beginTransaction();
		try {
			new TaavonBank_DBInitialize().createCycleAccountJob();
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

		SecurityProfile securityProfileDefault = AddSecurityProfile.createDefaultSecurityProfile(
				DBInitializeUtil.DEFAULT_SECURITY_PROFILE_NAME);
				
		SecurityProfile securityProfileAPACS = AddSecurityProfile.createAPACSSecurityProfile(
				DBInitializeUtil.APACS_SECURITY_PROFILE_NAME);
				

		AuthorizationProfile authorizationProfileDefault = AddAuthorization.createAuthorizationProfile(AddAuthorization.DEFAULT_AUTHORIZATION_PROFILE_NAME);
		AuthorizationProfile authorizationProfile_EPAY = AddAuthorization.createAuthorizationProfile(AddAuthorization.EPAY_AUTHORIZATION_PROFILE_NAME);
		AuthorizationProfile authorizationProfile_POS = AddAuthorization.createAuthorizationProfile(AddAuthorization.POS_AUTHORIZATION_PROFILE_NAME);
		AuthorizationProfile authorizationProfile_ATM = AddAuthorization.createAuthorizationProfile(AddAuthorization.ATM_AUTHORIZATION_PROFILE_NAME);

		System.out.println("------- Creating Policies -------");
		AddAuthorization initializer = new AddAuthorization();
		
		PanPrefixTransactionPolicy panPrefixTrxPolicy = initializer.addPanPrefixTransactionPolicy(authorizationProfile_POS, 
				new ArrayList<Integer>(){{
					add(new Long(TAVON_BIN).intValue());
			}});
//		initializer.addPanPrefixTransactionPolicy(authorizationProfile_EPAY,
//				new ArrayList<Integer>(){{
//					add(new Long(TAVON_BIN).intValue());
//			}});
		authorizationProfile_EPAY.addPolicy(panPrefixTrxPolicy);		
		authorizationProfile_ATM.addPolicy(panPrefixTrxPolicy);
		
		FITControlPolicy fitPolicy = initializer.addFITControlPolicy(authorizationProfile_POS);
//		initializer.addFITControlPolicy(authorizationProfile_EPAY);
		authorizationProfile_EPAY.addPolicy(fitPolicy);		
		authorizationProfile_ATM.addPolicy(fitPolicy);
		
		initializer.addPOSTerminalServicePolicy(authorizationProfile_POS);
		initializer.addEpayTerminalServicePolicy(authorizationProfile_EPAY);
		initializer.addATMTerminalServicePolicy(authorizationProfile_ATM);
		
		JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);
		ClearingProfile clearingProfilePerdayPOS = new AddClearingProfile().createPerDayClearingProfile(new MerchantCycleSettlementJob());
		ClearingProfile clearingProfileATM = new AddClearingProfile().createPerDayClearingProfile(new ATMCycleSettlementJob());
		ClearingProfile clearingProfilePerdayBILL = new AddClearingProfile().createPerDayClearingProfile(new BillPaymentCycleSettlementJob());
		ClearingProfile clearingProfilePerdayShetab = new AddClearingProfile().createPerDayClearingProfile(new ShetabCycleSettlementJob());

		FeeProfile feeProfileDefault = DBInitializeAddFee.createDefaultFeeProfile();
		FeeProfile feeProfileMerch = DBInitializeAddFee.createMerchantFeeProfile();

		System.out.println("------- Creating ChargeAssingment Policy -------");
		GeneralChargeAssignmentPolicy chargeAssignmentPolicy = null;

		System.out.println("------- Creating Terminal Groups -------");
		TerminalGroup rootTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, null, "همه ترمینالها",
				chargeAssignmentPolicy);
		TerminalGroup switchTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, rootTerminalGroup, "سوئیچ ها", null);
		TerminalGroup epayTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, clearingProfilePerdayPOS, switchTerminalGroup,
				"پرداخت اینترنتی", null);

			
		TerminalGroup posTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileAPACS, authorizationProfileDefault, clearingProfilePerdayPOS, rootTerminalGroup,
				"پایانه های فروش", null);
		TerminalGroup orgTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, rootTerminalGroup,
				"ترمينالهای مجازی ThirdParty", null);
		TerminalGroup atmTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfile_ATM, clearingProfileATM, rootTerminalGroup,
				"خودپردازها", null);
		GeneralDao.Instance.flush();

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
		MerchantCategory rootMerchantCategory = DBInitializeUtil.createMerchantCategory(1L, "همه نوع فعالیت ها", "All", true, null);
		
		System.out.println("------- Creating Organization -------");
		AddOrganization addOrganization = new AddOrganization();
		addOrganization.addBillPaymentOrg(clearingProfilePerdayBILL, feeProfileDefault, organizationFinancialEntityGroup,
				orgTerminalGroup);
		
		System.out.println("------- Creating MySelf Institution -------");
		
		String switchName = "سوئیچ "+BANK_NAME;
//		AddInstitution.initMyInst(TAVON_BIN, TAVON_BIN, FinancialEntityRole.MY_SELF, MYSELF_ACQUIER_TERMINAL_CODE, MYSELF_ISSUER_TERMINAL_CODE, switchTerminalGroup, securityProfileDefault, authorizationProfileDefault, null, feeProfileDefault, null, My_MAC_KEY, My_PIN_KEY, null, switchName, switchName);

		System.out.println("------- Creating Shetab Institution as Master -------");
		AddInstitution.initGeneralInst(INST_SHETAB_CODE, INST_SHETAB_CODE, FinancialEntityRole.MASTER, SHETAB_ACQUIER_TERMINAL_CODE,
				SHETAB_ISSUER_TERMINAL_CODE, switchTerminalGroup, securityProfileDefault, authorizationProfileDefault,
				clearingProfilePerdayShetab, feeProfileDefault, null, SHETAB_KEY, SHETAB_KEY, SHETAB_KEY, SHETAB_KEY, SHETAB_KEY, "سوئیچ شتاب",
				"سوئیچ شتاب");

		System.out.println("------- Creating Fanap Epayment_Institution as Slave -------");
		AddInstitution.initGeneralInst(INST_FNP_EPAY_CODE, TAVON_BIN, FinancialEntityRole.SLAVE, EPAY_FNP_TERMINAL_CODE,
				NO_TERMINAL_CODE, epayTerminalGroup, securityProfileDefault, authorizationProfile_EPAY, null,
				feeProfileDefault, chargeAssignmentPolicy, null, null, null, null, EPAY_FNP_MASTER_KEY, "سايت اينترنتی" ,
				"سايت اینترنتی "+switchName);
		
		
		System.out.println("------- Creating Merchant -------");
		Merchant merchant = DBInitializeAddingPos.initMerchant(false, ""+MERCHANT_GENERAL_CODE, feeProfileDefault,authorizationProfileDefault, MERCHANT_ACCOUNT, MERCHANT_OWNERNAME, merchantFinancialEntityGroup, rootMerchantCategory);

		System.out.println("------- Creating SHOP -------");
		DBInitializeAddingPos.initShop(false, ""+POS_SHOP_CODE, merchant, feeProfileMerch, authorizationProfile_POS,
				"فروشگاه تستی پایانه فروش", "فناپ", SHOP_POS_ACCOUNT, SHOP_POS_OWNERNAME, shopFinancialEntityGroup);
		DBInitializeAddingPos.initShop(false, ""+EPAY_FNP_SHOP_CODE, merchant, feeProfileDefault, authorizationProfile_EPAY,
				"فروشگاه اینترنتی", "فناپ", SHOP_EPAY_FNP_ACCOUNT, SHOP_EPAY_FNP_OWNERNAME, shopFinancialEntityGroup);

		
		System.out.println("------- Creating POS -------");
		DBInitializeUtil.initPOS(POS_GENERAL_CODE, ""+POS_SHOP_CODE, POS_SERIAL_NUMBER, MERCHANT_GENERAL_CODE,
				feeProfileDefault, securityProfileDefault, authorizationProfile_POS, clearingProfilePerdayPOS,
				chargeAssignmentPolicy, posTerminalGroup);

		System.out.println("------- Creating EPAY -------");
		RSAPublicKey publicKey = new RSAPublicKey(SMAdapter.LENGTH_RSA_PUBLIC_1024, SMAdapter.TYPE_TMK,
				EPAY_FNP_TERMINAL_PUBLIC_KEY);
		DBInitializeUtil.initEPay(EPAY_SHOP_TERMINAL_CODE, ""+EPAY_FNP_SHOP_CODE, publicKey, feeProfileDefault, securityProfileDefault,
				authorizationProfile_EPAY, clearingProfilePerdayPOS, chargeAssignmentPolicy, epayTerminalGroup);

		
//			System.out.println("------- Creating MTN Charge Specification -------");
//			initMTNChargeSpecification();

		System.out.println("------- Creating ATM -------");
		try {
			new ATMPasargadConfigImpl().addConfig();
			new ReadConfigData().startFromExcel(ATM_CONFIG_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		new DBInitializeATM().initATM(ATM_CODE, BRANCH_CODE, CORE_CODE, BRANCH_NAME);

		System.out.println("------- FINISHED -------");
	}

	private void createCycleAccountJob() throws Exception {
		JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);
		new AddClearingProfile().createCycleAccountJob();
	}
}
