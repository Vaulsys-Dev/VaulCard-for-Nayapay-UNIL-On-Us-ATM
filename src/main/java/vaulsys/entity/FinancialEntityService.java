package vaulsys.entity;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.NotUsed;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinancialEntityService {
	@NotUsed
	//Only used in some initializers
	public static FinancialEntity findEntity(Long code) {
		String query = "from FinancialEntity i where i.code = :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", code);
		return (FinancialEntity) GeneralDao.Instance.findObject(query, param);
	}

	public static Institution getInstitutionByCode(String code) {
		return ProcessContext.get().getInstitution(code);
//		return GlobalContext.getInstance().getInstitution(code);
	}

	public static Institution getInstitutionByBIN(Long Bin) {
		return ProcessContext.get().getInstitutionByBIN(Bin);
//		return GlobalContext.getInstance().getInstitutionByBIN(Bin);
	}
	
	public static <T extends FinancialEntity> T  findEntity(Class<T> clazz, String code) {
		if(clazz.equals(Institution.class))
			return (T) ProcessContext.get().getInstitution(code);
		String query = "from " + clazz.getName() + " i where i.code = :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", code);
		return (T) GeneralDao.Instance.findObject(query, param);
	}

	@NotUsed
	//Only used in some initializers
	public static <T extends FinancialEntity> List<T> findAllEntities(Class<T> clazz) {
		String query = "from " + clazz.getName();
		return GeneralDao.Instance.find(query, null);
	}

//	public static Institution getMyInstitution() {
////		return GlobalContext.getInstance().getMyInstitution();
//		return ProcessContext.get().getMyInstitution();
//	}

	public static Institution getMasterInstitution() {
//		Map<Long, Institution> allInstitutions = GlobalContext.getInstance().getAllInstitutions();
		Map<String, Institution> allInstitutions = ProcessContext.get().getAllInstitutions();
		for(Institution i : allInstitutions.values()){
			if(FinancialEntityRole.MASTER.equals(i.getRole()))
				return i;
		}
		return null;
	}
	
//	public static SwitchTerminal getIssuerSwitchTerminal(Institution institution) {
//		String query = "from " + SwitchTerminal.class.getName() + " t where t.owner = :owner and t.type = :type";
//		Map<String, Object> params = new HashMap<String, Object>(1);
//		params.put("owner", institution);
//		params.put("type", SwitchTerminalType.ISSUER);
//		return (SwitchTerminal) GeneralDao.Instance.findObject(query, params);
////		return GlobalContext.getInstance().getIssuerSwitchTerminal(institution.getCode());
//	}
//	
//	public static SwitchTerminal getAcquireSwitchTerminal(Institution institution) {
//		String query = "from " + SwitchTerminal.class.getName() + " t where t.owner = :owner and t.type = :type";
//		Map<String, Object> params = new HashMap<String, Object>(1);
//		params.put("owner", institution);
//		params.put("type", SwitchTerminalType.ACQUIER);
//		return (SwitchTerminal) GeneralDao.Instance.findObject(query, params);
////		return GlobalContext.getInstance().getAcquierSwitchTerminal(institution.getCode());
//	}
//
	public static ClearingDate getLastWorkingDay(FinancialEntity entity) {
		try {
			String query = "select max(c.recievedDate.dayDate) from ClearingDate c " 
				+ " where c.owner.id = :entityId " 
				+ " and c.valid = true group by owner";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("entityId", entity.getId());
			List<DayDate> dates = GeneralDao.Instance.find(query, param);
			
			query = "select max(c.recievedDate.dayTime) from ClearingDate c "
				+ " where c.owner.id = :entityId " 
				+ " and c.recievedDate.dayDate = :date"
				+ " group by owner";
			param.put("date", dates.get(0));
			DayTime time = (DayTime) GeneralDao.Instance.findObject(query, param);
			
			DateTime date = new DateTime(dates.get(0), time);
			
			query = "from ClearingDate as a" + " where a.owner.id = :entityId and a.valid = true"
			+ " and a.recievedDate = :maxDate";
			param.clear();
			param.put("entityId", entity.getId());
			param.put("maxDate", date);
			return (ClearingDate) GeneralDao.Instance.findObject(query, param);
		} catch (Exception e) {
			return null;
		}
	}

//	public static ClearingDate getLastWorkingDay(Long code) {
//		try {
//			String query = "select i.lastWorkingDay from "+ Institution.class.getName()+" i "
//						+ " where i.code = :code ";
//			Map<String, Object> param = new HashMap<String, Object>();
//			param.put("code", code);
//			return (ClearingDate) GeneralDao.Instance.findObject(query, param);
//		} catch (Exception e) {
//			return null;
//		}
//	}
//	
//	@NotUsed
//	public List<ClearingDate> getWorkingDays(FinancialEntity institution) {
//		String query = "from ClearingDate c where c.owner = :owner";
//		Map<String, Object> param = new HashMap<String, Object>(1);
//		param.put("owner", institution);
//		return GeneralDao.Instance.find(query, param);
//	}
//	
//	@NotUsed
//	public List<ClearingDate> getValidWorkingDays(FinancialEntity institution) {
//		String query = "from ClearingDate c where c.owner = :owner " +
//				" and c.valid = true desc c.date";
//		Map<String, Object> param = new HashMap<String, Object>(1);
//		param.put("owner", institution);
//		return GeneralDao.Instance.find(query, param);
//	}
//
	public static List<Institution> findAllSlaveInstitutions() {
//		Map<Long, Institution> allInstitutions = GlobalContext.getInstance().getAllInstitutions();
		Map<String, Institution> allInstitutions = ProcessContext.get().getAllInstitutions();
		List<Institution> result = new ArrayList<Institution>();
		for(Institution i : allInstitutions.values()){
			if(FinancialEntityRole.SLAVE.equals(i.getRole()))
				result.add(i);
		}
		return result;
//		String queryString = "from Institution as i where i.institutionType = :MasterSlave";
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("MasterSlave", FinancialEntityRole.SLAVE);
//		return generalDao.find(queryString, params, true);
	}

//	@NotUsed
//    public List<FinancialEntityGroup> findEntityGroupHierarchy(FinancialEntity entity) {
//        List<FinancialEntityGroup> entityGroups = new ArrayList<FinancialEntityGroup>();
//        FinancialEntityGroup entityGroup = entity.getParentGroup();
//        while (entityGroup != null) {
//            entityGroups.add(entityGroup);
//            entityGroup = entityGroup.getParentGroup();
//        }
//        return entityGroups;
//    }
}                   
