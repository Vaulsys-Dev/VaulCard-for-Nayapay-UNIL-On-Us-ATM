package vaulsys.entity;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.report.ReportGenerator;
import vaulsys.entity.impl.Organization;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.util.NotUsed;
import vaulsys.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizationService {
	@NotUsed
	// Used only in a garbage class
    public static List<Organization> findAllOrganizations(ClearingProfile clearingProfile) {
        String query = "from Organization o where o.clearingProfile = :clearingProfile";
        Map<String, Object> param = new HashMap<String, Object>(1);
        param.put("clearingProfile", clearingProfile);
        return GeneralDao.Instance.find(query, param);
    }

    public static List<Organization> findOrganizationByType(OrganizationType type) {
    	String query = "from Organization o where o.type = :type";
    	Map<String, Object> param = new HashMap<String, Object>(1);
    	param.put("type", type);
    	return GeneralDao.Instance.find(query, param);
    }

    public static Organization findOrganizationByCompanyCode(Integer companyCode, OrganizationType orgType) {
    	String query = "from Organization i where " +
    			" i.companyCode = :companyCode " +
    			" and i.type = :type";
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("companyCode", companyCode);
        param.put("type", orgType);
        return (Organization) GeneralDao.Instance.findObject(query, param);
    }
    
    public static Organization findOrganizationByCode(Long code, OrganizationType orgType) {
    	String query = "from Organization i where " +
		" i.code = :code " +
		" and i.type = :type";
    	Map<String, Object> param = new HashMap<String, Object>();
    	param.put("code", code);
    	param.put("type", orgType);	
    	return (Organization) GeneralDao.Instance.findObject(query, param);
}
    
    public static ThirdPartyVirtualTerminal findThirdPartyVirtualTerminalByCompanyCode(Integer companyCode, OrganizationType orgType) {
    	String query = "select term from ThirdPartyVirtualTerminal term inner join term.owner org " +
    			"where org.companyCode=:companyCode and org.type=:type";
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("companyCode", companyCode);
        param.put("type", orgType);
        return (ThirdPartyVirtualTerminal) GeneralDao.Instance.findObject(query, param);
    }
    
    public static ThirdPartyVirtualTerminal findThirdPartyVirtualTerminalByOrganization(Organization org) {
    	return findThirdPartyVirtualTerminalByOrganizationCode(org.getCode());
//    	String query = "from ThirdPartyVirtualTerminal term where term.owner.id=:owner_id";
//		Map<String, Object> param = new HashMap<String, Object>();
//		param.put("owner_id", org.getCode());
//		return (ThirdPartyVirtualTerminal) GeneralDao.Instance.findObject(query, param);
    }

    public static ThirdPartyVirtualTerminal findThirdPartyVirtualTerminalByOrganizationCode(Long code) {
    	String query = "from ThirdPartyVirtualTerminal term where term.owner.id=:owner_id";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("owner_id", code);
		return (ThirdPartyVirtualTerminal) GeneralDao.Instance.findObject(query, param);
    }
    
	public static void validation(Ifx ifx, Long thirdPartyCode, OrganizationType orgType) throws Exception{
		Organization organization = findOrganizationByCode(thirdPartyCode, orgType);
		String validationMethod = organization.getValidation();
		if (Util.hasText(validationMethod)) {
			if ("charityValidation".equals(validationMethod)) {
				charityValidation(ifx);
			}
		}
	}

	private static void charityValidation(Ifx ifx) throws Exception{
	}

	public static void generateDesiredThirdPartyReport(Organization entity, SettlementData settlementData) {
		String reportMethod = entity.getReport();
		if (Util.hasText(reportMethod)) {
			if ("charityreport".equals(reportMethod)) {
				ReportGenerator.generateCharityReport(settlementData);
			}
		}
	}
}                   
