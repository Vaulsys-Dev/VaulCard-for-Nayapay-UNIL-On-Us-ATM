package vaulsys.auditlog;



import vaulsys.user.SecurityLogDetail;

import java.util.List;

/**
 * Created by j.khodabande on 10/3/2015.
 */
public interface AuditableProperty {

    public void init(String field);

    public Object retrieveObjectValue(Object obj);

    public List<SecurityLogDetail> CompareObject(Object objOld,Object objNew) throws AuditLogException;
    
    public void InitialObjectValue(Object obj) throws AuditLogException;

}
