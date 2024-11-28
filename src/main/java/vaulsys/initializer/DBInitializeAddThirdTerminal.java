package vaulsys.initializer;

import vaulsys.calendar.DateTime;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Organization;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.terminal.impl.TerminalSharedFeature;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBInitializeAddThirdTerminal {

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new DBInitializeAddThirdTerminal().cereateDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
		System.exit(0);
	}
	
	private void cereateDB() throws Exception {
		System.out.println("------- Creating Virtual ThirdParty Terminals -------");
		initVirtualThirdPartyTerminal();
	}
	
	private void initVirtualThirdPartyTerminal() throws Exception {
		List<Organization> orgList = FinancialEntityService.findAllEntities(Organization.class);
		
		for (Organization organization : orgList) {
			createTerminalForOrg(organization);
		}
		
	}

	private void createTerminalForOrg(Organization organization) {
		System.out.println("------- Organization: " + organization.getId() + "-------");
		ThirdPartyVirtualTerminal terminal = new ThirdPartyVirtualTerminal();
		terminal.setCreatorUser(DBInitializeUtil.getUser());
		terminal.setCreatedDateTime(DateTime.now());
		
		TerminalGroup terminalGroup = getTerminalGroup("ترمينالهای مجازی ThirdParty");
		
//		TerminalGroup terminalGroup = createTerminalGroup(getFeeProfile("بدون کارمزد"), getSecurityProfile("الگوی امنیتی پیش فرض"), getAuthorizationProfile("بدون محدودیت"), null, rootTerminalGroup, "ترمينالهای مجازی ThirdParty", null);
		
		TerminalSharedFeature sharedFeature = terminalGroup.getSafeSharedFeature();
		terminal.setParentGroup(terminalGroup);
		terminal.setOwner(organization);
		terminal.setSharedFeature(sharedFeature);
//		terminal.setClearingProfile(organization.getClearingProfile());
		terminal.setFeeProfile(organization.getOwnOrParentFeeProfile());
		GeneralDao.Instance.saveOrUpdate(terminal);
		
	}
	
	TerminalGroup getTerminalGroup(String name) {
		String query = "from TerminalGroup i where i.name = :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		return (TerminalGroup) GeneralDao.Instance.findObject(query, param);
	}
}
