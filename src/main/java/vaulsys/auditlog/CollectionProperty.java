package vaulsys.auditlog;


import vaulsys.user.SecurityLogDetail;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by j.khodabande on 10/3/2015.
 */
public class CollectionProperty extends AbstractCompareProperty {
    private static Logger _log = Logger.getLogger(CollectionProperty.class);

    public CollectionProperty(String field){
        super(field);
    }

    @Override
    public Object retrieveObjectValue(Object obj) {
        return getObjectValueCollection(obj);

    }

    private Object[] getObjectValueCollection(Object obj){
        String propertyName;
        String f;
        if (fieldName.contains(".")) {
            // we need to recurse down to final object
            propertyName= fieldName.substring(0, fieldName.indexOf("."));
            f=fieldName.substring(fieldName.indexOf(".")+1);
        }else {
            propertyName=fieldName;
            f="";
        }

        Object curObj=basicRetrieveObjectValue(obj,propertyName);


        if (curObj instanceof Set) {
            Object[] arroldV = ((Set) curObj).toArray();
            if (f == "")
                return arroldV;
            else {
                Object[] retO = new Object[arroldV.length];

                for (int i = 0; i < arroldV.length; i++) {
                    retO[i] = basicRetrieveObjectValue(arroldV[i], f);
                }
                return retO;
            }

        } else if (curObj.getClass().isArray()) {

            int length = Array.getLength(curObj);
            for (int i = 0; i < length; i++) {
                Object arrayElement = Array.get(curObj, i);
                //node.Add(compareGetters(arrayElement,Name+"["+i+"]",level+1));
            }
        } else if (curObj instanceof Map) {
            Map<Object, Object> baseResult = (Map) curObj;
            for (Object o : baseResult.keySet()) {
                Object o1 = baseResult.get(o);
                //System.out.println(o1.toString());
                //node.Add(compareGetters(o1,Name+"["+o.toString()+"]",level+1));
            }
        }else if (curObj instanceof List) {
            Object[] arroldV = ((List)curObj).toArray();
            if (f == "")
                return arroldV;
            else {
                Object[] retO = new Object[arroldV.length];

                for (int i = 0; i < arroldV.length; i++) {
                    retO[i] = basicRetrieveObjectValue(arroldV[i], f);
                }
                return retO;
            }
        }
        return null;
    }

    @Override
    public List<SecurityLogDetail> CompareObject(Object objOld, Object objNew) throws AuditLogException{
        List<SecurityLogDetail> securityLogDetailList=new ArrayList<SecurityLogDetail>();
        Object[] mtOld= getObjectValueCollection(objOld);
        Object[] mtNew= getObjectValueCollection(objNew);
        String retval="";
        for(int i=0;i<mtOld.length;i++){
            boolean find=false;
            int j;
            for(j=0;j<mtNew.length;j++){

                if(mtOld[i].equals(mtNew[j]) ){
                    find=true;
                    break;
                }
            }
            if(find){
                SecurityLogDetail changesLogDetail=basicCompare(mtOld[i],mtNew[j],fieldName);
                if(changesLogDetail!=null)
                    securityLogDetailList.add(changesLogDetail);
            }else
            {
                SecurityLogDetail changesLogDetail=basicCompare(mtOld[i],null,fieldName);
                if(changesLogDetail!=null)
                    securityLogDetailList.add(changesLogDetail);
            }
        }

        for(int i=0;i<mtNew.length;i++){
            boolean find=false;
            for(int j=0;j<mtOld.length;j++){
                if( mtNew[i].equals(mtOld[j]) ||  mtNew[i]==mtOld[j]){
                    find=true;
                    break;
                }
            }
            if(!find) {
                SecurityLogDetail changesLogDetail = basicCompare(null, mtNew[i], fieldName);
                if (changesLogDetail != null)
                    securityLogDetailList.add(changesLogDetail);
            }
        }

        return securityLogDetailList;
    }
    
    @Override
    public void InitialObjectValue(Object obj) throws AuditLogException{

        String propertyName;
        String f;
        if (fieldName.contains(".")) {
            // we need to recurse down to final object
            propertyName= fieldName.substring(0, fieldName.indexOf("."));
            f=fieldName.substring(fieldName.indexOf(".")+1);
        }else {
            propertyName=fieldName;
            f="";
        }

        Object curObj=basicRetrieveObjectValue(obj,propertyName);
		if (curObj != null) {
			Hibernate.initialize(curObj);
			Object[] mtOld = getObjectValueCollection(obj);
			for (Object node : mtOld) {
				if (node != null) {
					Hibernate.initialize(node);
					node.toString();
				}
			}
		}
    	
    }
}
