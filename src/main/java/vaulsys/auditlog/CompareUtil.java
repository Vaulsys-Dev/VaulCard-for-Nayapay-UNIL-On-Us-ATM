package vaulsys.auditlog;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by j.khodabande on 9/29/2015.
 */
public class CompareUtil {
    private static Logger _log = Logger.getLogger(CompareUtil.class);
    public static List<Method> getGetters(Class clazz, List<String> ignoreFields) {

        if(ignoreFields == null) {
            ignoreFields = new ArrayList<String>();
        }
        ignoreFields.add("class");

        List<Method> getters = new ArrayList<Method>();

        // get getters
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {

            if (isGetter(method) && !listContainsString(ignoreFields, method.getName())) {
                if (!method.getReturnType().toString().contains("org.hibernate"))
                    getters.add(method);
            }
        }

        return getters;
    }
    private static boolean listContainsString(List<String> list, String string) {
        boolean contains = false;
        for(String item: list) {
            if(string.toUpperCase().contains(item.toUpperCase())) {
                contains = true;
                break;
            }
        }

        return contains;
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

    public static Object retrieveObjectValue(Object obj, String property) {
        if (property.contains(".")) {
            // we need to recurse down to final object
            String props[] = property.split("\\.");
            try {
                Method method = obj.getClass().getMethod(getGetterMethodName(props[0]));
                Object ivalue = method.invoke(obj);
                if (ivalue==null)
                    return null;
                return retrieveObjectValue(ivalue,property.substring(props[0].length()+1));
            } catch (Exception e) {
                _log.error("Failed to retrieve value for "+property);
                //throw new Exception("retrieveObjectValue");
            }
        } else {
            // let's get the object value directly
            try {
                Method method = obj.getClass().getMethod(getGetterMethodName(property));
                return method.invoke(obj);
            } catch (Exception e) {
                _log.error("Failed to retrieve value for "+property);
                //throw new InvalidImplementationException("CrudUtil","retrieveObjectValue",null,"", e);
            }
        }
        return null;
    }




    public static boolean isGetter(Method method) {
        if (!(method.getName().startsWith("get") || method.getName().startsWith("is")) ) {
            return false;
        }
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        if (void.class.equals(method.getReturnType())) {
            return false;
        }
        return true;
    }
}
