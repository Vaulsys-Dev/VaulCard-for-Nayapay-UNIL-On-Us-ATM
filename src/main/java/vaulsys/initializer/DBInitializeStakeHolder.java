package vaulsys.initializer;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Core;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;

import java.util.HashMap;
import java.util.Map;

public class DBInitializeStakeHolder {

	private static final Long PSP_CODE = 9001L;
	private static final Long PSP_BIN = 502229L;
	private static final String PSP_ACCOUNT_NUMBER = "202,810,21219,3";
	private static final String PSP_ACCOUNT_HOLDER_NAME = "حساب کارمزدهای شرکت خدمات پرداخت پاسارگاد";
	private static final Core PSP_ACCOUNT_CORE = Core.NEGIN_CORE;
	private static final String PSP_NAME = "شرکت خدمات پرداخت پاسارگاد";
	private static final String PSP_REPNAME = "شرکت خدمات پرداخت پاسارگاد";
	
	private static final Long FANAP_CODE = 9002L;
	private static final Long FANAP_BIN = 502229L;
	private static final String FANAP_ACCOUNT_NUMBER = "";
	private static final String FANAP_ACCOUNT_HOLDER_NAME = "حساب کارمزدهای فناپ";
	private static final Core FANAP_ACCOUNT_CORE = Core.NEGIN_CORE;
	private static final String FANAP_NAME = "فناپ";
	private static final String FANAP_REPNAME = "فناپ";
	
	private static final Long BANK_CODE = 9003L;
	private static final Long BANK_BIN = 502229L;
	private static final String BANK_ACCOUNT_NUMBER = "995,4252,1";
	private static final String BANK_ACCOUNT_HOLDER_NAME = "حساب کارمزدهای بانک";
	private static final Core BANK_ACCOUNT_CORE = Core.NEGIN_CORE;
	private static final String BANK_NAME = "بانک";
	private static final String BANK_REPNAME = "بانک";
	
//	private static final Long IRANCELL_POS_CODE = 9004L;
//	private static final Long IRANCELL_POS_BIN = 502229L;
//	private static final String IRANCELL_POS_ACCOUNT_NUMBER = "201,810,9355555,2";
//	private static final String IRANCELL_POS_ACCOUNT_HOLDER_NAME = "حساب تراکنشهای پايانه فروش ايرانسل";
//	private static final Core IRANCELL_POS_ACCOUNT_CORE = Core.NEGIN_CORE;
//	private static final String IRANCELL_POS_NAME = "شرکت ايرانسل";
//	private static final String IRANCELL_POS_REPNAME = "شرکت ايرانسل";

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new DBInitializeStakeHolder().addToDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
		System.exit(0);
	}
	
	private void addToDB() throws Exception {
		System.out.println("------- Create Stake Holder Institutios -------");
		
		addStakeHolder(PSP_CODE, PSP_BIN, PSP_NAME, PSP_ACCOUNT_NUMBER, PSP_ACCOUNT_HOLDER_NAME, PSP_ACCOUNT_CORE, PSP_REPNAME);
		addStakeHolder(FANAP_CODE, FANAP_BIN, FANAP_NAME, FANAP_ACCOUNT_NUMBER, FANAP_ACCOUNT_HOLDER_NAME, FANAP_ACCOUNT_CORE, FANAP_REPNAME);
		addStakeHolder(BANK_CODE, BANK_BIN, BANK_NAME, BANK_ACCOUNT_NUMBER, BANK_ACCOUNT_HOLDER_NAME, BANK_ACCOUNT_CORE, BANK_REPNAME);
//		createInstitution(IRANCELL_POS_CODE, IRANCELL_POS_BIN, IRANCELL_POS_NAME, IRANCELL_POS_ACCOUNT_NUMBER, IRANCELL_POS_ACCOUNT_HOLDER_NAME, IRANCELL_POS_ACCOUNT_CORE, IRANCELL_POS_REPNAME);
		
		System.out.println("------- FINISHED -------");
	}
	
	private void addStakeHolder(Long code, Long bin, String name, String account_number, String account_holder_name, Core account_core, String repname) {
		String query = "from Institution i where i.code = :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", code);
		Institution institution = (Institution) GeneralDao.Instance.findObject(query, param);
		if (institution == null) {
			institution = new Institution();
			institution.setAccount(DBInitializeUtil.createAccount(account_number, account_holder_name,account_core));
			institution.setBin(bin);
			institution.setCode(code);
			institution.setContact(DBInitializeUtil.createContact(repname, "-", null, null));
			institution.setInstitutionType(FinancialEntityRole.STAKE_HOLDER);
			institution.setName(name);
			
			institution.setCreatorUser(DBInitializeUtil.getUser());
			institution.setCreatedDateTime(DateTime.now());
			institution.setEnabled(true);
			GeneralDao.Instance.saveOrUpdate(institution);
		}
	}
}
