package vaulsys.auditlog;

import vaulsys.calendar.DateTime;
import vaulsys.log.LogLevel;
import vaulsys.persistence.GeneralDao;
import vaulsys.user.*;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuditLogUtil {
	
	private static Logger logger = Logger.getLogger(AuditLogUtil.class);
	
	public static SecurityLog CreateChangesLog(Auditable entityOld, Auditable entityNew, SecurityGroup securityGroup, LogLevel logLevel, Number recordID, String objectName, User user, String IP)throws Exception {
		List<AuditableProperty> lst = entityNew.getAuditableFields();
		Set<SecurityLogDetail> securityLogDetails = new HashSet<SecurityLogDetail>();
		try {
			for (AuditableProperty c : lst) {
				securityLogDetails.addAll(c.CompareObject(entityOld, entityNew));
			}
			if (securityLogDetails.size() == 0) {
				return null;
			} else {

				SecurityLog log = new SecurityLog();
				log.setActivityTime(DateTime.now());
				log.setLogLevel(logLevel);
                log.setRecordID(Long.parseLong(recordID.toString()));
				log.setGroup(securityGroup);
				log.setObjectName(objectName.toString());
				for(SecurityLogDetail securityLogDetail:securityLogDetails){
					securityLogDetail.setSecurityLog(log);
				}
				log.setLogDetail(securityLogDetails);
				log.setAction(UserAction.UPDATE);
				log.setUsername(user == null ? "" : user.getUsername());
				log.setRequestIp(IP);
				return log;
			}
			// insertDB(log);
		} catch (AuditLogException ex) {
			logger.error(ex);			
		}
		return null;
	}
	
	public static void insertDB(SecurityLog totalLog){
        try{
            GeneralDao.Instance.save(totalLog);
        if(totalLog.getLogDetail()!= null && totalLog.getId()!= null){
            for(SecurityLogDetail ldetail:totalLog.getLogDetail()){
                ldetail.setSecurityLog(totalLog);
                GeneralDao.Instance.save(ldetail);
            }
        }
        }catch(Exception e){
             e.printStackTrace();
        }
    }
	
	public static void InitialObject(Auditable entity){	
		try {
			List<AuditableProperty> lst = entity.getAuditableFields();
			for (AuditableProperty c : lst) {
				c.InitialObjectValue(entity);
			}
		} catch (AuditLogException ex) {
			logger.error(ex);
		} catch (Exception ex) {
			logger.error(ex);
		}
	}
	

}
