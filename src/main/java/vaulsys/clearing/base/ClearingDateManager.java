package vaulsys.clearing.base;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class ClearingDateManager {
	private static final Logger logger = Logger.getLogger(ClearingDateManager.class);
	private static ClearingDateManager clearingDateManager;

	public static ClearingDateManager getInstance() {
		if (clearingDateManager == null)
			clearingDateManager = new ClearingDateManager();
		return clearingDateManager;
	}

	private ClearingDateManager() {

	}

	public void push(MonthDayDate date, DateTime recievedDate, Institution institution) throws Exception {
		push(date, recievedDate, false, institution);
	}

	public void refresh() {
	}

	public boolean isValidRange(List<Date> dates) {
		return dates.size() == 2;
	}

	public void push(MonthDayDate cutOverDate, DateTime recievedDate, boolean validity, Institution institution) {
		ClearingDate clearingDate = new ClearingDate(cutOverDate);
		clearingDate.setRecievedDate(recievedDate);
		clearingDate.setValid(validity);
		clearingDate.setOwner(institution);
		GeneralDao.Instance.saveOrUpdate(clearingDate);
		if (validity) {
			ClearingDate lastWorkingDay = institution.getLastWorkingDay();
			institution.setLastWorkingDay(null);
			GeneralDao.Instance.delete(lastWorkingDay);
			institution.setLastWorkingDay(institution.getCurrentWorkingDay());
			institution.setCurrentWorkingDay(clearingDate);
			GeneralDao.Instance.saveOrUpdate(institution);
		}
		logger.debug("ClearingDate "+ cutOverDate+" ("+ validity+") at "+ recievedDate+" for "+institution.getCode()+" was pushed.");
	}

	public void deleteOtherWorkingDay(Institution institution){
		String query = "delete from "+ ClearingDate.class.getName()+" c "
						+ " where c.owner = :inst "
						+ " and ((c.recievedDate.dayDate < :day)"
						+ " or (c.recievedDate.dayDate = :day and c.recievedDate.dayTime < :time))";
//						+ " and ((c.recievedDate.dayDate = :day and c.recievedDate.dayTime < :time) or c.recievedDate.dayDate < :day)";  
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("inst", institution);
		params.put("day", institution.getLastWorkingDay().getRecievedDate().getDayDate());
		params.put("time", institution.getLastWorkingDay().getRecievedDate().getDayTime());
		logger.debug("delete clearingDate: "+ GeneralDao.Instance.executeUpdate(query, params));
	}
}
