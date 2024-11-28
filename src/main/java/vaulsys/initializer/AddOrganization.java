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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddOrganization 
{
	
	private static final int CURRENCY_RIAL_CODE = 364;
//    private final String chargeIrancellSchema = GlobalPaths.getProperty(GlobalPaths.getProperty(GlobalPaths.CONFIG_FILES_PATH)) + "/clearing/schema/ChargeIrancellSchema.xml";	
//    private final String mtn = GlobalPaths.getProperty(GlobalPaths.getProperty(GlobalPaths.CONFIG_FILES_PATH)) + "/clearing/schema/mtn.txt";	
    private Long orgCode = 10000L;
    
	public void addBillPaymentOrg(ClearingProfile clearingProfile, FeeProfile feeProfile, FinancialEntityGroup parentGroup, TerminalGroup terminalGroup) throws Exception
	{
		Map<Byte, List<Integer>> map = new HashMap<Byte, List<Integer>>();
		List<Integer> list1= new ArrayList<Integer>();
		list1.add(1);
		list1.add(2);
		list1.add(3);
		list1.add(4);
		list1.add(5);
		list1.add(6);
		list1.add(8);
		list1.add(20);
		list1.add(21);
		list1.add(34);
		list1.add(47);
		map.put((byte)1,list1);
		
		List<Integer> list2= new ArrayList<Integer>();
		list2.add(41);
		list2.add(42);
		list2.add(43);
		list2.add(44);
		list2.add(45);
		list2.add(46);
		list2.add(47);
		map.put((byte)2,list2);
		
		List<Integer> list3= new ArrayList<Integer>();
		list3.add(111);
		list3.add(103);
		list3.add(203);
		list3.add(303);
		list3.add(403);
		list3.add(503);
		list3.add(603);
		list3.add(703);
		list3.add(803);
		list3.add(903);
		map.put((byte)3,list3);
		
		List<Integer> list4= new ArrayList<Integer>();
		list4.add(1);
		list4.add(22);
		list4.add(23);
		list4.add(24);
		list4.add(25);
		list4.add(26);
		list4.add(27);
		list4.add(28);
		list4.add(29);
		list4.add(30);
		list4.add(31);
		list4.add(32);
		list4.add(33);
		list4.add(34);
		list4.add(35);
		list4.add(36);
		list4.add(37);
		list4.add(38);
		list4.add(39);
		list4.add(40);
		list4.add(61);
		list4.add(62);
		map.put((byte)4,list4);
		
		List<Integer> list5= new ArrayList<Integer>();
		list5.add(91);
		map.put((byte)5,list5);

        String ACCOUNT_ID = "219-800-234582-1";
        Account account = new Account("محمد نژادصداقت" , ACCOUNT_ID, DBInitializeUtil.findCurrency(CURRENCY_RIAL_CODE), Core.NEGIN_CORE);
		
		for (byte billCode: map.keySet()) {
			for(Integer companyCode: map.get(billCode)) {
				add(account, OrganizationType.getByCode(Integer.parseInt(billCode+"")), ++orgCode, companyCode, clearingProfile, feeProfile, parentGroup, terminalGroup);
			}
		}
	}
	
	private void add(Account account, OrganizationType type, Long orgCode, Integer companyCode, ClearingProfile clearingProfile, FeeProfile feeProfile, FinancialEntityGroup parentGroup, TerminalGroup terminalGroup) {
		Organization organization = FinancialEntityService.findEntity(Organization.class, orgCode.toString());
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
		else if (OrganizationType.THIRDPARTY.equals(type))
			organization.setName("عنصر سوم");
		

		Contract contract = new Contract();
		contract.setStartDate(DayDate.now());
		contract.setEndDate(DayDate.MAX_DAY_DATE);

        organization.setCompanyCode(companyCode);
		organization.setType(type);
		organization.setContract(contract);
		organization.setCode(orgCode);
		organization.setFeeProfile(feeProfile);
		
		organization.setParentGroup(parentGroup);
		organization.setSharedFeature(parentGroup.getSafeSharedFeature());
		
//		organization.setCode(new Long(OrganizationType.getCode(type)+""+companyCode));

		organization.setAccount(account);
		organization.setContact(contactInfo);
		organization.setEnabled(true);
		
		
		/********************/
		ThirdPartyVirtualTerminal terminal = new ThirdPartyVirtualTerminal();
		terminal.setCode(orgCode);
		terminal.setCreatorUser(DBInitializeUtil.getUser());
		terminal.setCreatedDateTime(DateTime.now());
		
		TerminalSharedFeature sharedFeature = terminalGroup.getSafeSharedFeature();
		terminal.setParentGroup(terminalGroup);
		terminal.setOwner(organization);
		terminal.setSharedFeature(sharedFeature);
		terminal.setClearingProfile(clearingProfile);
		terminal.setFeeProfile(organization.getOwnOrParentFeeProfile());
		GeneralDao.Instance.saveOrUpdate(terminal);
		/********************/
		

		GeneralDao.Instance.saveOrUpdate(organization);
	}

	public void addCellChargeOrg(ClearingProfile clearingProfile, FeeProfile feeProfile, FinancialEntityGroup parentGroup, TerminalGroup terminalGroup) throws Exception{
		Map<Byte, List<Integer>> map = new HashMap<Byte, List<Integer>>();
		List<Integer> list9= new ArrayList<Integer>();
		list9.add(100);
		map.put((byte)88,list9);
		
//		List<Integer> list8= new ArrayList<Integer>();
//		list8.add(936);
//		map.put((byte)8,list8);
		
		 String ACCOUNT_ID = "219-800-234582-1";
	        Account account = new Account("محمد نژادصداقت" , ACCOUNT_ID, DBInitializeUtil.findCurrency(CURRENCY_RIAL_CODE), Core.FANAP_CORE);
			
			for (byte billCode: map.keySet()) {
				for(Integer companyCode: map.get(billCode)) {
					add(account, OrganizationType.getByCode(Integer.parseInt(billCode+"")), 8810L, companyCode, clearingProfile, feeProfile, parentGroup, terminalGroup);
				}
			}
		
//		MTNUtil util = new MTNUtil();
//		util.parse(new File(mtn), new File(chargeIrancellSchema), OrganizationType.MTNIRANCELL);
		
	}
}
