package vaulsys.initializer;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.base.SecurityFunction;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.component.SecurityComponent;

import java.util.HashMap;
import java.util.Map;

public class AddSecurityProfile {

	public static final String DEFAULT_SECURITY_PROFILE_NAME = "پیش فرض";
	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			
//			createDefaultSecurityProfile(DEFAULT_SECURITY_PROFILE_NAME);
			createAPACSSecurityProfile("پیش فرض APACS");
			
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}

		System.out.println("before end tranaction");
		GeneralDao.Instance.endTransaction();
		System.out.println("after end tranaction2");
		System.exit(0);
	}


	public static SecurityProfile createDefaultSecurityProfile(String name) throws Exception {
		Map<String, Map<String, String>> securityFunctions = new HashMap<String, Map<String, String>>() {
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

		return AddSecurityProfile.createSecurityProfile(name, securityFunctions);
	}

	
	public static SecurityProfile createSecurityProfile(String name, Map<String, Map<String, String>> securiryFunctions)
			throws Exception {
		
		SecurityProfile securityProfile = findSecurityProfile(name);
		if (securityProfile != null)
			return securityProfile;
		
		securityProfile = new SecurityProfile();
		securityProfile.setName(name);
		securityProfile.setCreatorUser(DBInitializeUtil.getUser());
		securityProfile.setCreatedDateTime(DateTime.now());

		if (securiryFunctions != null)
			for (String func_name : securiryFunctions.keySet()) {
				SecurityFunction function = new SecurityFunction();
				function.setName(func_name);
				function.setHost("Fanap Security Module");
				Map<String, String> params = securiryFunctions.get(func_name);
				for (String param : params.keySet()) {
					function.addParameter(param, params.get(param));
				}
				function.setSecurityProfile(securityProfile);
				securityProfile.addFunction(function);

			}

		GeneralDao.Instance.saveOrUpdate(securityProfile);

		return securityProfile;
	}

	private static SecurityProfile findSecurityProfile(String name) {
		String query = "from " + SecurityProfile.class.getName() + " s " + " where s.name = :name";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
		return (SecurityProfile) GeneralDao.Instance.findObject(query, param);

	}

	public static SecurityProfile createAPACSSecurityProfile(String name) throws Exception {
		SecurityProfile securityProfile = new SecurityProfile();
		securityProfile.setName(name);
//		securityProfile.setCreatorUser(GlobalContext.getInstance().getSwitchUser());
		securityProfile.setCreatedDateTime(DateTime.now());

		SecurityFunction MAC_GEN_FUNC = new SecurityFunction();
		MAC_GEN_FUNC.setName(SecurityComponent.FUNC_MAC_GEN);
		MAC_GEN_FUNC.setHost("Fanap Security Module");
		MAC_GEN_FUNC.addParameter("Algorithm", "0");
		MAC_GEN_FUNC.addParameter("MacLength", "4");
		MAC_GEN_FUNC.addParameter("Padding", "ZeroPadding");
		MAC_GEN_FUNC.setSecurityProfile(securityProfile);
		securityProfile.addFunction(MAC_GEN_FUNC);

		SecurityFunction MAC_VER_FUNC = new SecurityFunction();
		MAC_VER_FUNC.setName(SecurityComponent.FUNC_MAC_VER);
		MAC_VER_FUNC.setHost("Fanap Security Module");
		MAC_VER_FUNC.addParameter("Algorithm", "0");
		MAC_VER_FUNC.addParameter("MacLength", "4");
		MAC_VER_FUNC.addParameter("Padding", "ZeroPadding");
		MAC_VER_FUNC.addParameter("SkipLength", "8");
		MAC_VER_FUNC.setSecurityProfile(securityProfile);
		securityProfile.addFunction(MAC_VER_FUNC);

		SecurityFunction PIN_TRANS = new SecurityFunction();
		PIN_TRANS.setName(SecurityComponent.FUNC_TRANSLATEPIN);
		PIN_TRANS.setHost("Fanap Security Module");
		PIN_TRANS.addParameter("PIN Format", "01");
		PIN_TRANS.addParameter("AccountNumber Length", "12");
		PIN_TRANS.setSecurityProfile(securityProfile);
		securityProfile.addFunction(PIN_TRANS);

		GeneralDao.Instance.saveOrUpdate(securityProfile);

		return securityProfile;
	}

}
