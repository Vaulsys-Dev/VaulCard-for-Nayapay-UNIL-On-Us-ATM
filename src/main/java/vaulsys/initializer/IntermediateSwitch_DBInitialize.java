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
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.RSAPublicKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.TerminalGroup;

import java.util.ArrayList;

public class IntermediateSwitch_DBInitialize {
	public static final long INST_MYSELF_BIN = 639347;
	public static final long INST_NEGIN_BIN = 639347;
	public static final String BANK_NAME = "پاسارگاد";

	// Institutions
	public static final long INST_MYSELF_CODE = 999999;
	public static final long INST_NEGIN_CODE = 639347;
	public static final long INST_SHETAB_CODE = 9000;


	public static final long SHETAB_ACQUIER_TERMINAL_CODE = 212;
	public static final long SHETAB_ISSUER_TERMINAL_CODE = 213;
	
	public static final long NEGIN_ACQUIER_TERMINAL_CODE = 112;
	public static final long NEGIN_ISSUER_TERMINAL_CODE = 113;

	public static final long MYSELF_ACQUIER_TERMINAL_CODE = 2;
	public static final long MYSELF_ISSUER_TERMINAL_CODE = 3;

	public static final long NO_TERMINAL_CODE = -1;

	// Keys
	private static final String My_MAC_KEY = "1111111111111111";
	private static final String My_PIN_KEY = "1111111111111111";
	
	private static final String SHETAB_MASTER_KEY  = "880AA66CC77BE9FD";
	private static final String SHETAB_ACQ_MAC_KEY = "A298C3A6EF99FFF6";
	private static final String SHETAB_ACQ_PIN_KEY = "1F1E62F77679A8C9";
	private static final String SHETAB_ISS_MAC_KEY = "A63B2518C45F7E5B";
	private static final String SHETAB_ISS_PIN_KEY = "6E1BBB2B46DFCC17";
	
	
	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new IntermediateSwitch_DBInitialize().cereateDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();

		System.exit(0);
	}

	private void cereateDB() throws Exception {
		System.out.println("------- Creating BANKS -------");
		DBInitializeUtil.createBanks();
		
		System.out.println("------- Creating RIAL Currency -------");
		DBInitializeUtil.createDefaultCurrency();

		System.out.println("------- Creating Profiles -------");
		SecurityProfile securityProfileDefault = AddSecurityProfile.createDefaultSecurityProfile(DBInitializeUtil.DEFAULT_SECURITY_PROFILE_NAME);
				
		AuthorizationProfile authorizationProfileDefault = AddAuthorization.createAuthorizationProfile(AddAuthorization.DEFAULT_AUTHORIZATION_PROFILE_NAME);
		
		FeeProfile feeProfileDefault = DBInitializeAddFee.createDefaultFeeProfile();

		System.out.println("------- Creating Terminal Groups -------");
		TerminalGroup rootTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, null, "همه ترمینالها",
				null);
		TerminalGroup switchTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, rootTerminalGroup, "سوئیچ ها", null);
		GeneralDao.Instance.flush();

		System.out.println("------- Creating Financial Entity Groups -------");
		FinancialEntityGroup rootFinancialEntityGroup = DBInitializeUtil.createFinancialEntityGroup(feeProfileDefault,
				authorizationProfileDefault, null, null, "همه موجودیت ها");

		System.out.println("------- Creating Merchant Categories -------");
		MerchantCategory rootMerchantCategory = DBInitializeUtil.createMerchantCategory(1L, "همه نوع فعالیت ها", "All", true, null);
		
		
		System.out.println("------- Creating MySelf Institution -------");
		String switchName = "سوئیچ "+BANK_NAME;
		AddInstitution.initMyInst(INST_MYSELF_CODE, INST_MYSELF_BIN, MYSELF_ACQUIER_TERMINAL_CODE, MYSELF_ISSUER_TERMINAL_CODE, 
				switchTerminalGroup, securityProfileDefault, authorizationProfileDefault, null, feeProfileDefault, null, 
				My_MAC_KEY, My_PIN_KEY, null, switchName, switchName);

		
		System.out.println("------- Creating Shetab Institution as Master -------");
		AddInstitution.initGeneralInst(INST_SHETAB_CODE, INST_SHETAB_CODE, FinancialEntityRole.MASTER, SHETAB_ACQUIER_TERMINAL_CODE,
				SHETAB_ISSUER_TERMINAL_CODE, switchTerminalGroup, securityProfileDefault, authorizationProfileDefault,
				null, feeProfileDefault, null, SHETAB_ISS_MAC_KEY, SHETAB_ISS_PIN_KEY, SHETAB_ACQ_MAC_KEY, SHETAB_ACQ_PIN_KEY, SHETAB_MASTER_KEY, "سوئیچ شتاب",
				"سوئیچ شتاب");
		
		
		System.out.println("------- Creating Negin Institution as Slave -------");
		AddInstitution.initGeneralInst(INST_NEGIN_CODE, INST_NEGIN_BIN, FinancialEntityRole.SLAVE, NEGIN_ACQUIER_TERMINAL_CODE,
				NEGIN_ISSUER_TERMINAL_CODE, switchTerminalGroup, securityProfileDefault, authorizationProfileDefault,
				null, feeProfileDefault, null, SHETAB_ACQ_MAC_KEY, SHETAB_ACQ_PIN_KEY, SHETAB_ISS_MAC_KEY, SHETAB_ISS_PIN_KEY, SHETAB_MASTER_KEY, "سوئیچ شتاب",
				"سوئیچ نگین");

		System.out.println("------- FINISHED -------");
	}
}
