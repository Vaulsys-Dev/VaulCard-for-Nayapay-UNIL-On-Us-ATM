package vaulsys.initializer;

import vaulsys.calendar.DateTime;
import vaulsys.customer.AccountOwnerType;
import vaulsys.entity.FinancialEntityService;
import vaulsys.fee.impl.FeeItem;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.fee.impl.FixedEntityAccount;
import vaulsys.fee.impl.TransactionFee;
import vaulsys.fee.impl.VirtualEntityAccount;
import vaulsys.persistence.GeneralDao;

public class DBInitializeAddFee {
	private static final String DEFAULT_FEE_PROFILE_NAME = "بدون کارمزد";
	
	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new DBInitializeAddFee().addToDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
		System.exit(0);
	}
	
	private void addToDB() throws Exception {
		System.out.println("------- Create FeeProfiles -------");
		
		createIranCellFeeProfile();
		createOnePercentFeeProfile();
		
		System.out.println("------- FINISHED -------");
	}
	
	public static FeeProfile createIranCellFeeProfile() throws Exception {
		FeeProfile feeProfile = new FeeProfile();
		feeProfile.setCreatorUser(DBInitializeUtil.getUser());
		feeProfile.setCreatedDateTime(DateTime.now());
		feeProfile.setEnabled(true);
		feeProfile.setName("شارژ ايرانسل");
		feeProfile.setDescription("کارمزدهای ترمینالهای پايانه فروش و اينترنتی شارژ ايرانسل");
		
		
		/************* TransactionFees******************/
		/*********** TransactionFee: 1**************/
		TransactionFee transactionFee = new TransactionFee();
		transactionFee.setEnabled(true);
		transactionFee.setRule("( ifx.trnType == vaulsys.protocols.ifx.enums.trnType.PURCHASECHARGE ) && ( ifx.terminalType == vaulsys.protocols.ifx.enums.TerminalType.INTERNET )");
		transactionFee.setOwner(feeProfile);
		/************** FeeItems: 1 ***********/
		FeeItem fi = new FeeItem();
		fi.setDescription("انتقال کارمزد از ايرانسل به بانک بابت تراکنش اينترنتی");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.01");
		
		
		FixedEntityAccount accountToBeCreditedF = new FixedEntityAccount();
		accountToBeCreditedF.setFinancialEntity(FinancialEntityService.findEntity(9003L));
		fi.setAccountToBeCredited(accountToBeCreditedF);
		
		VirtualEntityAccount accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCreditedF);
		
//		Variable v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFee.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFee);
		feeProfile.addBaseFee(transactionFee);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/*************************/
		
		/*********** TransactionFee: 2**************/
		TransactionFee transactionFeePOS = new TransactionFee();
		transactionFeePOS.setEnabled(true);
		transactionFeePOS.setRule("( ifx.trnType == vaulsys.protocols.ifx.enums.trnType.PURCHASECHARGE ) && ( ifx.terminalType == vaulsys.protocols.ifx.enums.TerminalType.POS )");
		transactionFeePOS.setOwner(feeProfile);
		/************** FeeItems: 1 ***********/
		fi = new FeeItem();
		fi.setDescription("انتقال کارمزد 1% از ايرانسل به پذيرنده بابت تراکنش پايانه فروش");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.01");
		
		VirtualEntityAccount accountToBeCredited = new VirtualEntityAccount(AccountOwnerType.SHOP);
		fi.setAccountToBeCredited(accountToBeCredited);
		
		accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCredited);
		
//		v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFeePOS.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFeePOS);
		feeProfile.addBaseFee(transactionFeePOS);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/************** FeeItems: 2 ***********/
		fi = new FeeItem();
		fi.setDescription("انتقال کارمزد .05% از ايرانسل به خدمات بابت تراکنش پايانه فروش");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.005");
		
		
		accountToBeCreditedF = new FixedEntityAccount();
		accountToBeCreditedF.setFinancialEntity(FinancialEntityService.findEntity(9001L));
		fi.setAccountToBeCredited(accountToBeCreditedF);
		
		accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCreditedF);
		
//		v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFeePOS.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFeePOS);
		feeProfile.addBaseFee(transactionFeePOS);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/************** FeeItems: 3 ***********/
		fi = new FeeItem();
		fi.setDescription("انتقال کارمزد .05% از ايرانسل به فناپ بابت تراکنش پايانه فروش");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.005");
		
		
		accountToBeCreditedF = new FixedEntityAccount();
		accountToBeCreditedF.setFinancialEntity(FinancialEntityService.findEntity(9002L));
		fi.setAccountToBeCredited(accountToBeCreditedF);
		
		accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCreditedF);
		
//		v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFeePOS.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFeePOS);
		feeProfile.addBaseFee(transactionFeePOS);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/*************************/

		return feeProfile;
	}
	
	public static FeeProfile createOnePercentFeeProfile() throws Exception {
		FeeProfile feeProfile = new FeeProfile();
		feeProfile.setCreatorUser(DBInitializeUtil.getUser());
		feeProfile.setCreatedDateTime(DateTime.now());
		feeProfile.setEnabled(true);
		feeProfile.setName("شارژ ايرانسل");
		feeProfile.setDescription("کارمزدهای ترمینالهای پايانه فروش و اينترنتی شارژ ايرانسل");
		
		
		/************* TransactionFees******************/
		/*********** TransactionFee: 1**************/
		TransactionFee transactionFee = new TransactionFee();
		transactionFee.setEnabled(true);
		transactionFee.setRule("( ifx.trnType == vaulsys.protocols.ifx.enums.trnType.PURCHASECHARGE ) && ( ifx.TerminalType == vaulsys.protocols.ifx.enums.TerminalType.INTERNET )");
		transactionFee.setOwner(feeProfile);
		/************** FeeItems: 1 ***********/
		FeeItem fi = new FeeItem();
		fi.setDescription("انتقال کارمزد از ايرانسل به بانک بابت تراکنش اينترنتی");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.01");
		
		
		FixedEntityAccount accountToBeCreditedF = new FixedEntityAccount();
		accountToBeCreditedF.setFinancialEntity(FinancialEntityService.findEntity(9003L));
		fi.setAccountToBeCredited(accountToBeCreditedF);
		
		VirtualEntityAccount accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCreditedF);
		
//		Variable v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFee.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFee);
		feeProfile.addBaseFee(transactionFee);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/*************************/
		
		/*********** TransactionFee: 2**************/
		TransactionFee transactionFeePOS = new TransactionFee();
		transactionFeePOS.setEnabled(true);
		transactionFeePOS.setRule("( ifx.trnType == vaulsys.protocols.ifx.enums.trnType.PURCHASECHARGE ) && ( ifx.TerminalType == vaulsys.protocols.ifx.enums.TerminalType.POS )");
		transactionFeePOS.setOwner(feeProfile);
		/************** FeeItems: 1 ***********/
		fi = new FeeItem();
		fi.setDescription("انتقال کارمزد 1% از ايرانسل به پذيرنده بابت تراکنش پايانه فروش");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.01");
		
		VirtualEntityAccount accountToBeCredited = new VirtualEntityAccount(AccountOwnerType.SHOP);
		fi.setAccountToBeCredited(accountToBeCredited);
		
		accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCredited);
		
//		v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFeePOS.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFeePOS);
		feeProfile.addBaseFee(transactionFeePOS);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/************** FeeItems: 2 ***********/
		fi = new FeeItem();
		fi.setDescription("انتقال کارمزد .05% از ايرانسل به خدمات بابت تراکنش پايانه فروش");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.005");
		
		
		accountToBeCreditedF = new FixedEntityAccount();
		accountToBeCreditedF.setFinancialEntity(FinancialEntityService.findEntity(9001L));
		fi.setAccountToBeCredited(accountToBeCreditedF);
		
		accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCreditedF);
		
//		v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFeePOS.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFeePOS);
		feeProfile.addBaseFee(transactionFeePOS);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/************** FeeItems: 3 ***********/
		fi = new FeeItem();
		fi.setDescription("انتقال کارمزد .05% از ايرانسل به فناپ بابت تراکنش پايانه فروش");
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.005");
		
		
		accountToBeCreditedF = new FixedEntityAccount();
		accountToBeCreditedF.setFinancialEntity(FinancialEntityService.findEntity(9002L));
		fi.setAccountToBeCredited(accountToBeCreditedF);
		
		accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCreditedF);
		
//		v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFeePOS.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFeePOS);
		feeProfile.addBaseFee(transactionFeePOS);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/*************************/
		
		return feeProfile;
	}

	static public FeeProfile createDefaultFeeProfile() throws Exception {
		FeeProfile feeProfile = findFeeProfile(DEFAULT_FEE_PROFILE_NAME);
		if (feeProfile != null)
			return feeProfile;
		
		 feeProfile = new FeeProfile();
		feeProfile.setCreatorUser(DBInitializeUtil.getUser());
		feeProfile.setCreatedDateTime(DateTime.now());
		feeProfile.setEnabled(true);
		feeProfile.setName(DEFAULT_FEE_PROFILE_NAME);
		
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		return feeProfile;
	}
	
	private static FeeProfile findFeeProfile(String name) {
		return (FeeProfile) GeneralDao.Instance.findObject("from "+ FeeProfile.class.getName()+" f where f.name='"+ name+"'", null);
	}

	static public FeeProfile createMerchantFeeProfile() throws Exception {
		FeeProfile feeProfile = findFeeProfile("کارمزد یک درصد پذیرنده");
		if (feeProfile != null)
			return feeProfile;
		feeProfile = new FeeProfile();
		
		feeProfile.setCreatorUser(DBInitializeUtil.getUser());
		feeProfile.setCreatedDateTime(DateTime.now());
		feeProfile.setEnabled(true);
		feeProfile.setName("کارمزد یک درصد پذیرنده");
		
		TransactionFee transactionFee = new TransactionFee();
		transactionFee.setEnabled(true);
		transactionFee.setRule("(ifx.eMVRqData.auth_Amt > 10)");
		transactionFee.setOwner(feeProfile);
		FeeItem fi = new FeeItem();
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.01");
		fi.setDescription("کارمزد پذیرنده");
		
		VirtualEntityAccount accountToBeCredited = new VirtualEntityAccount(AccountOwnerType.ACQUIRER);
		fi.setAccountToBeCredited(accountToBeCredited);
		
		VirtualEntityAccount accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.SHOP);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCredited);
		
//		Variable v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
		// GeneralDao.Instance.saveOrUpdate(v);
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFee.addFeeItemList(fi);
		
		GeneralDao.Instance.saveOrUpdate(transactionFee);
		feeProfile.addBaseFee(transactionFee);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		return feeProfile;
	}
	
	static public FeeProfile createOrgFeeProfile() throws Exception {
		FeeProfile feeProfile = findFeeProfile("کارمزد یک در صد");
		if (feeProfile != null)
			return feeProfile;
		feeProfile = new FeeProfile();
		
		feeProfile.setCreatorUser(DBInitializeUtil.getUser());
		feeProfile.setCreatedDateTime(DateTime.now());
		feeProfile.setEnabled(true);
		feeProfile.setName("کارمزد یک در صد");

		TransactionFee transactionFee = new TransactionFee();
		transactionFee.setEnabled(true);
		transactionFee.setRule("(ifx.eMVRqData.auth_Amt > 10)");
		transactionFee.setOwner(feeProfile);
		FeeItem fi = new FeeItem();
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.01");

		VirtualEntityAccount accountToBeCredited = new VirtualEntityAccount(AccountOwnerType.SHOP);
		fi.setAccountToBeCredited(accountToBeCredited);
		
		VirtualEntityAccount accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);

		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCredited);

//		Variable v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");

//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFee.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFee);
		feeProfile.addBaseFee(transactionFee);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/*************************/
		fi = new FeeItem();
		fi.setEnabled(true);
		fi.setFormula("ifx.eMVRqData.auth_Amt * 0.02");
		
		
		FixedEntityAccount accountToBeCreditedF = new FixedEntityAccount();
		accountToBeCreditedF.setFinancialEntity(FinancialEntityService.findEntity(502229L));
		fi.setAccountToBeCredited(accountToBeCreditedF);
		
		accountToBeDebited = new VirtualEntityAccount(AccountOwnerType.ORGANIZATION);
		fi.setAccountToBeDebited(accountToBeDebited);
		
		GeneralDao.Instance.saveOrUpdate(accountToBeDebited);
		GeneralDao.Instance.saveOrUpdate(accountToBeCreditedF);
		
//		v = new Variable();
//		v.setName("ifx.eMVRqData.Auth_Amt");
		
//		fi.addVariable(v);
		GeneralDao.Instance.saveOrUpdate(fi);
		transactionFee.addFeeItemList(fi);
		GeneralDao.Instance.saveOrUpdate(transactionFee);
		feeProfile.addBaseFee(transactionFee);
		GeneralDao.Instance.saveOrUpdate(feeProfile);
		/*************************/

		return feeProfile;
	}

}
