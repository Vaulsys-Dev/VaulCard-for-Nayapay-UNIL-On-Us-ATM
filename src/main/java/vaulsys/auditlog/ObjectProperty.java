package vaulsys.auditlog;


import vaulsys.user.SecurityLogDetail;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by j.khodabande on 10/3/2015.
 */
public class ObjectProperty extends AbstractCompareProperty {
    private static Logger _log = Logger.getLogger(ObjectProperty.class);

    @Override
    public Object retrieveObjectValue(Object obj) {

        Object oldV = basicRetrieveObjectValue(obj, fieldName);

        List<Method> methods = null;
        if (oldV != null) {
            methods = CompareUtil.getGetters(oldV.getClass(), null);
            List retval=new ArrayList();
            for (Method m : methods) {
                try {
                    retval.add( m.invoke(oldV));

                    //Object newValue = retrieveObjectValue(newObject, property.toString());
                    //if (oldValue == null) oldValue = new String("");

                } catch (Exception ex) {

                }
            }
            return  retval;
        }
        return null;
    }

    private List getInnerMethodsValues(Object obj){
        Object oldV = basicRetrieveObjectValue(obj, fieldName);

        List<Method> methods = null;
        if (oldV != null) {
            methods = CompareUtil.getGetters(oldV.getClass(), null);
            List retval=new ArrayList();
            for (Method m : methods) {
                try {
                    ObjectNode nd=new ObjectNode();
                    nd.Name=fieldName+"."+ m.getName();
                    nd.Value= m.invoke(oldV);
                    retval.add(nd);

                    //Object newValue = retrieveObjectValue(newObject, property.toString());
                    //if (oldValue == null) oldValue = new String("");

                } catch (Exception ex) {

                }
            }
            return  retval;
        }
        return null;
    }


    @Override
    public List<SecurityLogDetail> CompareObject(Object objOld, Object objNew) throws AuditLogException{
        List<SecurityLogDetail> securityLogDetailList=new ArrayList<SecurityLogDetail>();

        List<ObjectNode> mtOld= getInnerMethodsValues(objOld);
        List<ObjectNode> mtNew= getInnerMethodsValues(objNew);

        String retval="";

        List<ObjectNode> fullObj=mtOld;
        if(fullObj==null)
            fullObj=mtNew;

        if(fullObj!=null) {
            for (int i = 0; i < fullObj.size(); i++) {
                Object ndOld=null;
                Object ndNew=null;
                if(mtOld!=null)
                    ndOld=mtOld.get(i).Value;
                if(mtNew!=null)
                    ndNew=mtNew.get(i).Value;
                SecurityLogDetail changesLogDetail=basicCompare(ndOld, ndNew, fullObj.get(i).Name);
                if(changesLogDetail!=null)
                    securityLogDetailList.add(changesLogDetail);
            }
        }
        return securityLogDetailList;
    }
    
    @Override
    public void InitialObjectValue(Object obj) throws AuditLogException{
    	
    	Object objectval=basicRetrieveObjectValue(obj,fieldName); 
    	 
    	 Hibernate.initialize(objectval);
         List<ObjectNode> mtOld= getInnerMethodsValues(obj);
         for(ObjectNode node:mtOld){
			if (node.Value != null) {
				Hibernate.initialize(node.Value);
				node.Value.toString();
			}
         }                

    }

    public ObjectProperty(String field){
        super(field);
    }
}
