package vaulsys.auditlog;

import vaulsys.user.SecurityLogDetail;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by j.khodabande on 10/3/2015.
 */
public class SimpleProperty extends AbstractCompareProperty {
    private static Logger _log = Logger.getLogger(SimpleProperty.class);

    @Override
    public Object retrieveObjectValue(Object obj) {
        return basicRetrieveObjectValue(obj,fieldName);
    }

    @Override
    public List<SecurityLogDetail> CompareObject(Object objOld, Object objNew) throws AuditLogException{

        Object valOld= basicRetrieveObjectValue(objOld,fieldName);
        Object valNew= basicRetrieveObjectValue(objNew,fieldName);
        SecurityLogDetail changesLogDetail=basicCompare(valOld,valNew,fieldName);
        List<SecurityLogDetail> securityLogDetailList=new ArrayList<SecurityLogDetail>();
        if(changesLogDetail!=null)
            securityLogDetailList.add(changesLogDetail);
        return securityLogDetailList;
    }
    
//    public List<SecurityLogDetail> CompareObject(AbstractCollection objOld, AbstractCollection objNew) throws AuditLogException{
//    	return CompareObject(objOld.toArray(),objNew.toArray());
//    }
//    public List<SecurityLogDetail> CompareObject(Object[] objOld, Object[] objNew) throws AuditLogException{
//    	List<SecurityLogDetail> securityLogDetailList=new ArrayList<SecurityLogDetail>();
//    	for(int i = 0; i<objOld.length; i++){
//    		securityLogDetailList.addAll(CompareObject(objOld[i],objNew[i]));
//    	}
//    	return securityLogDetailList;
//    }

    @Override
    public void InitialObjectValue(Object obj) throws AuditLogException{
		Object oldV = basicRetrieveObjectValue(obj, fieldName);
		if (oldV != null) {
			Hibernate.initialize(oldV);
			oldV.toString();
		}
    }
    
    public SimpleProperty(String field){
        super(field);
    }
}
