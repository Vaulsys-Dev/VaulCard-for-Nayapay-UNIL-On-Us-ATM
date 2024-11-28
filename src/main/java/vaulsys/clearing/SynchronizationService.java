package vaulsys.clearing;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.SynchronizationFlag;
import vaulsys.clearing.base.SynchronizationObject;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.IEntity;
import vaulsys.wfe.GlobalContext;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;

public class SynchronizationService {
	private static final Log logger = LogFactory.getLog(SynchronizationService.class);
	
	public static SynchronizationObject getSynchornizationObject(IEntity obj, Class clazz, LockMode lockMode) {
		String s = "from SynchronizationObject a " 
					+" where a.objectId = :object "
					+" and a.objClass = :obj "
					+" and a.lock = :f ";
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("object", obj.getId());
		parameters.put("f", SynchronizationFlag.Free);
		parameters.put("obj", clazz.getSimpleName());
		
		SynchronizationObject object = null;
		object = (SynchronizationObject) GeneralDao.Instance.findObject(s, parameters);
		
		if (object == null){
			object = checkObjectExistanceAndAddIfNotExits(obj, clazz, object);
		}
		
		object = (SynchronizationObject) GeneralDao.Instance.synchObject(object, lockMode);
		return object;
	}

	private static synchronized SynchronizationObject checkObjectExistanceAndAddIfNotExits(IEntity obj, Class clazz,
			SynchronizationObject object) {
		if( isSynchronizationObjectExists(obj, clazz) ){
			throw new LockAcquisitionException(obj.getClass().getSimpleName()+ " "+ obj.getId()+" is not free", null);
		}else{
			object = addSynchronizationObject(obj, clazz);
			if (object == null){
				throw new LockAcquisitionException(obj.getClass().getSimpleName()+ " "+ obj.getId()+" is not free", null);
			}
		}
		return object;
	}

	public static boolean isSynchronizationObjectExists(IEntity obj, Class clazz) {
		String s = "from SynchronizationObject s "
					+" where s.objectId = :object "
					+" and s.objClass = :obj ";

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("object", obj.getId());
		parameters.put("obj", clazz.getSimpleName());
		
		SynchronizationObject object = (SynchronizationObject) GeneralDao.Instance.findObject(s, parameters);
		if (object == null)
			return false;
		else
			return true;
	}
	
	private static SynchronizationObject addSynchronizationObject(IEntity obj, Class clazz) {
		logger.info("Adding SyncObj: "+clazz.getName()+" with id:"+obj.getId());
		SynchronizationObject newObj = new SynchronizationObject();
		try {
			newObj.setObjClass(clazz.getSimpleName());
			newObj.setObjectId((Long) obj.getId());
			GeneralDao.Instance.saveOrUpdate(newObj);
			return newObj;
		} catch (Exception e) {
			logger.error("Error ocured during adding syncObj ",e);
		}
		return null;
	}

	public static void release(IEntity obj, Class clazz){
		String s = "update SynchronizationObject s "
					+ " set s.lock = :free "
					+ ", s.releaseDate.dayDate = :day, s.releaseDate.dayTime = :time "
					+ ", s.application = null "
					+ "where s.objectId = :object "
					+" and s.objClass = :obj ";

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("object", obj.getId());
		parameters.put("free", SynchronizationFlag.Free);
		DateTime now = DateTime.now();
		parameters.put("day", now.getDayDate());
		parameters.put("time", now.getDayTime());
		parameters.put("obj", clazz.getSimpleName());

		GeneralDao.Instance.executeUpdate(s, parameters);
	}
	
	public static void releaseApplicationLock(String application){
		String s = "update SynchronizationObject s "
			+ " set s.lock = :free "
			+ ", s.releaseDate.dayDate = :day, s.releaseDate.dayTime = :time "
			+ ", s.application = null "
			+ "where s.application = :application ";
//			+" and s.lock = :lock ";
		
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("free", SynchronizationFlag.Free);
		DateTime now = DateTime.now();
		parameters.put("day", now.getDayDate());
		parameters.put("time", now.getDayTime());
		parameters.put("application", application);
//		parameters.put("lock", SynchronizationFlag.LOCK);
//		GeneralDao.Instance.executeUpdate(s, parameters);
		logger.info(GeneralDao.Instance.executeUpdate(s, parameters)+" SyncObjects are released by "+ application); //Raza LOGGING ENHANCED - removing from Logging File
		//System.out.println(GeneralDao.Instance.executeUpdate(s, parameters)+" SyncObjects are released by "+ application);
	}
	
	public static void lock(IEntity obj, Class clazz){
		String s = "update SynchronizationObject s "
			+" set s.lock = :lock "
			+ ", s.application = :application "
			+ ", s.lockDate.dayDate = :day, s.lockDate.dayTime = :time "
			+ ", s.releaseDate.dayDate = :rDay, s.releaseDate.dayTime = :rTime "
			+" where s.objectId = :object"
			+" and s.objClass = :obj ";
		
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("object", obj.getId());
		parameters.put("lock", SynchronizationFlag.LOCK);
		parameters.put("application", GlobalContext.getInstance().getApplicationName());
		DateTime now = DateTime.now();
		parameters.put("day", now.getDayDate());
		parameters.put("time", now.getDayTime());
		parameters.put("rDay", DayDate.UNKNOWN);
		parameters.put("rTime", DayTime.UNKNOWN);		
		parameters.put("obj", clazz.getSimpleName());
		
		GeneralDao.Instance.executeUpdate(s, parameters);
	}
}
