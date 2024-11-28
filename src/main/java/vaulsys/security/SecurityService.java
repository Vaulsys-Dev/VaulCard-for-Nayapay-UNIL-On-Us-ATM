package vaulsys.security;

import java.util.HashMap;
import java.util.Map;

import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.security.base.SecurityFunction;
import vaulsys.security.base.SecurityProfile;
import vaulsys.wfe.ProcessContext;

public class SecurityService {
	
	public static SecurityFunction findSecurityFunction(Long profileId, String name) {
		//return GlobalContext.getInstance().getSecurityFunction(profile.getId(), name);
		return ProcessContext.get().getSecurityFunction(profileId, name);

//		String query = "from SecurityFunction s where s.securityProfile.id = :secid " +
//				" and s.name = :name ";
//        Map<String, Object> param = new HashMap<String, Object>(2);
//        param.put("secid", profile.getId());
//        param.put("name", name);
//        return  (SecurityFunction) generalDao.findObject(query, param, false);
	}
	
	public static SecurityProfile findSecurityProfile(String name) {
		String query = "from " + SecurityProfile.class.getName() + " sp where sp.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		
		param.put("name", name);
		
		return (SecurityProfile) GeneralDao.Instance.findObject(query, param);
		
	}
	
	public static SecurityProfile findSecurityProfileLikeName(String name) {
		String query = "from " + SecurityProfile.class.getName() + " sp where sp.name like :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		
		param.put("name", "%" + name + "%");
		return (SecurityProfile) GeneralDao.Instance.findObject(query, param);
		
	}
	
	public static boolean isTranslatePIN(Ifx ifx) {
		IfxType inIfxType = ifx.getIfxType();
		IfxType secIfxType = ifx.getSecIfxType();
		
		if (ISOFinalMessageType.isRequestMessage(inIfxType) &&
				!ISOFinalMessageType.isReversalOrRepeatMessage(inIfxType) &&
				!ISOFinalMessageType.isTransferCheckAccountMessage(inIfxType) &&
				!ISOFinalMessageType.isTransferToacChechAccountMessage(inIfxType) &&
				!ISOFinalMessageType.isTransferToMessage(inIfxType) &&
				!ISOFinalMessageType.isTransferToAccountTransferToMessage(inIfxType) &&
				!ISOFinalMessageType.isGetAccountMessage(secIfxType) &&
				!ISOFinalMessageType.isDepositChechAccountMessage(inIfxType) &&
				!ISOFinalMessageType.isDepositMessage(inIfxType))
			return true;
		
		return false;
	}
}
