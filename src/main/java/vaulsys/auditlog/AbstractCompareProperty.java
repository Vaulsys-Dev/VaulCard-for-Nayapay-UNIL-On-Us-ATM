package vaulsys.auditlog;

import vaulsys.user.SecurityLogDetail;
import vaulsys.user.UserAction;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

/**
 * Created by j.khodabande on 10/3/2015.
 */
public abstract class AbstractCompareProperty implements AuditableProperty {
	private static Logger _log = Logger.getLogger(AbstractCompareProperty.class);
    protected String fieldName="";
    public void init(String field){
        this.fieldName=field;
    }


    /**
     * Retrieves the getter method name for a given property.
     * (e.g. name will return getName)
     * @param propertyName
     * @return
     */
    public static String getGetterMethodName(String propertyName) {
        if (StringUtils.isEmpty(propertyName) || propertyName.length()<=0)
            return null;
        char c = Character.toUpperCase(propertyName.charAt(0));
        return "get"+c+propertyName.substring(1);
    }
    public static String getIsGetterMethodName(String propertyName) {
        if (StringUtils.isEmpty(propertyName) || propertyName.length()<=0)
            return null;
        char c = Character.toUpperCase(propertyName.charAt(0));
        return "is"+c+propertyName.substring(1);
    }

    protected Object basicRetrieveObjectValue(Object obj, String property) {
        if (property.contains(".")) {
            // we need to recurse down to final object
            String props[] = property.split("\\.");
            try {
                Method method = obj.getClass().getMethod(getGetterMethodName(props[0]));
                Object ivalue = method.invoke(obj);
                
                if(ivalue==null)
                	return null;
                return basicRetrieveObjectValue(ivalue, property.substring(props[0].length() + 1));
            } catch (Exception e) {
            	try {
                    Method method = obj.getClass().getMethod(getIsGetterMethodName(props[0]));
                    Object ivalue = method.invoke(obj);
                    
                    if(ivalue==null)
                    	return null;
                    return basicRetrieveObjectValue(ivalue, property.substring(props[0].length() + 1));
                } catch (Exception ex) {            	
                }
            }
        } else {
            // let's get the object value directly
            try {
                Method method = obj.getClass().getMethod(getGetterMethodName(property));
				return method.invoke(obj);
            } catch (Exception e) {
                try {
                    Method method = obj.getClass().getMethod(getIsGetterMethodName(property));
                    return method.invoke(obj);
                } catch (Exception ex) {
                }
            }

        }
        return null;
    }

    protected SecurityLogDetail basicCompare(Object objOld,Object objNew,String propertyName){

        SecurityLogDetail logDetail = null;
        
        
        
        String retval = propertyName + " [ ";
        if (objOld == null && objNew != null) {
            retval += "null >>" + objNew.toString();
            logDetail = createUpdateLogDetail(objOld, objNew, propertyName, UserAction.ADD);
        } else if (objOld != null && objNew == null) {
            retval += objOld.toString() + " >> null";
            logDetail = createUpdateLogDetail(objOld, objNew, propertyName, UserAction.DELETE);
        } else if (objOld == null && objNew == null) {
            retval += "null -- null";
        } else if (!objOld.equals(objNew)) {
            retval += objOld.toString() + " >> " + objNew.toString();
            logDetail = createUpdateLogDetail(objOld, objNew, propertyName, UserAction.UPDATE);
        } else {
            retval += objOld.toString() + " -- " + objNew.toString();
        }
        retval += " ] ";
        _log.debug(retval);
        return logDetail;
    }
    private SecurityLogDetail createUpdateLogDetail(Object oldObj,Object newObj,String fieldName,UserAction action){
        SecurityLogDetail logDetail = new SecurityLogDetail();

        if (oldObj != null) {
            logDetail.setOldValue(oldObj.toString());
            logDetail.setOldValueStr(oldObj.toString());

        } else {
            logDetail.setOldValue("");
            logDetail.setOldValueStr("");

        }
        if (newObj != null) {
            logDetail.setNewValue(newObj.toString());
            logDetail.setNewValueStr(newObj.toString());
        } else {
            logDetail.setNewValue("");
            logDetail.setNewValueStr("");
        }
        logDetail.setFeildType(Object.class);
        logDetail.setFieldName(fieldName);
        logDetail.setAction(action);
        return logDetail;
    }


    public AbstractCompareProperty(String field){
        init(field);
    }


    public AbstractCompareProperty(){

    }
}
