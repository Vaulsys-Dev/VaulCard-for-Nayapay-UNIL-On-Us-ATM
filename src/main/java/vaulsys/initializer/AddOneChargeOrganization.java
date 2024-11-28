package vaulsys.initializer;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.contact.Contact;
import vaulsys.customer.Account;
import vaulsys.customer.Core;
import vaulsys.entity.Contract;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.FinancialEntityGroup;
import vaulsys.entity.impl.Organization;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.terminal.impl.TerminalSharedFeature;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;

public class AddOneChargeOrganization 
{
	private static final int CURRENCY_RIAL_CODE = 364;
	
	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new AddOneChargeOrganization().addToDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
	}
	
	private void addToDB() throws Exception {
		System.out.println("------- Creating IranCell Organization -------");
		addCellChargeOrg();
		System.out.println("------- FINISHED -------");
	}
	
	private void add(Account account, OrganizationType type, String orgCode, Integer companyCode, ClearingProfile clearingProfile, FeeProfile feeProfile, FinancialEntityGroup parentGroup, TerminalGroup terminalGroup) {
		Organization organization = FinancialEntityService.findEntity(Organization.class, orgCode);
		if (organization == null)
			organization = new Organization();
		
		organization.setCreatorUser(DBInitializeUtil.getUser());
		organization.setCreatedDateTime(DateTime.now());
		
		Contact contactInfo = new Contact();
		if (OrganizationType.WATER.equals(type))
			organization.setName("آب");
		else if (OrganizationType.ELECTRONIC.equals(type))
			organization.setName("برق");
		else if (OrganizationType.GAZ.equals(type))
			organization.setName("گاز");
		else if (OrganizationType.TEL.equals(type))
			organization.setName("تلفن ثابت");
		else if (OrganizationType.MOBILE.equals(type))
			organization.setName("تلفن همراه");
		else if (OrganizationType.MANAGE_NET.equals(type))
			organization.setName("شهرداری");
		else if (OrganizationType.MTNIRANCELL.equals(type))
			organization.setName("ایرانسل");
		else if (OrganizationType.MTNPARSNIKATEL.equals(type))
			organization.setName("پارس نیکاتل");
		

		Contract contract = new Contract();
		contract.setStartDate(DayDate.now());
		contract.setEndDate(DayDate.MAX_DAY_DATE);

        organization.setCompanyCode(companyCode);
		organization.setType(type);
		organization.setContract(contract);
		organization.setCode(Long.parseLong(orgCode));
		organization.setFeeProfile(feeProfile);
		
		organization.setParentGroup(parentGroup);
		organization.setSharedFeature(parentGroup.getSafeSharedFeature());
		
		organization.setAccount(account);
		organization.setContact(contactInfo);
		organization.setEnabled(true);
		
		GeneralDao.Instance.saveOrUpdate(organization);
		
		/********************/
		ThirdPartyVirtualTerminal terminal = new ThirdPartyVirtualTerminal();
		terminal.setCode(Long.parseLong(orgCode));
		terminal.setCreatorUser(DBInitializeUtil.getUser());
		terminal.setCreatedDateTime(DateTime.now());
		
		TerminalSharedFeature sharedFeature = terminalGroup.getSafeSharedFeature();
		terminal.setParentGroup(terminalGroup);
		terminal.setOwner(organization);
		terminal.setSharedFeature(sharedFeature);
		terminal.setClearingProfile(clearingProfile);
		terminal.setFeeProfile(feeProfile);
		GeneralDao.Instance.saveOrUpdate(terminal);
		/********************/
		

	}
	
	public void addCellChargeOrg() throws Exception {
		Organization iranCell = (Organization) FinancialEntityService.findEntity(9935L);

		String ACCOUNT_ID = "201,810,9355555,2";
		Account account = new Account("حساب تراکنشهای پايانه فروش ايرانسل", ACCOUNT_ID, DBInitializeUtil.findCurrency(CURRENCY_RIAL_CODE), Core.NEGIN_CORE);

		add(account, OrganizationType.MTNIRANCELL, "99352", 935, iranCell.getTerminal().getOwnOrParentClearingProfile(), iranCell
				.getTerminal().getOwnOrParentFeeProfile(), iranCell.getParentGroup(), iranCell.getTerminal().getParentGroup());

	}
}
