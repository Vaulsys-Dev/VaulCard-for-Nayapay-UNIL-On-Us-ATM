package vaulsys.cms.base;

import vaulsys.persistence.IEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mati on 19/03/2019.
 */
public class CMSLimitCycleType implements IEnum, Cloneable {

    private static final int DAILY_VALUE = 1;
    private static final int MONTHLY_VALUE = 2;
    private static final int YEARLY_VALUE = 3;

    public static final CMSLimitCycleType DAILY = new CMSLimitCycleType(DAILY_VALUE);
    public static final CMSLimitCycleType MONTHLY = new CMSLimitCycleType(MONTHLY_VALUE);
    public static final CMSLimitCycleType YEARLY = new CMSLimitCycleType(YEARLY_VALUE);

    public static final Map<Integer, String> valueToNameMap = new HashMap<Integer, String>();

    static{
        Field[] list = CMSLimitCycleType.class.getFields();
        Method getType = null;
        try {
            getType = CMSLimitCycleType.class.getMethod("getType");
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        }
        for (Field e : list) {
            String name = e.getName().toUpperCase();
            try {
                if (e.getName().equalsIgnoreCase("VALUETONAMEMAP"))
                    continue;

                Integer value = (Integer) getType.invoke(e.get(null), (Object[])null);
                valueToNameMap.put(value, name);
            } catch (Exception ex) {
                // TODO: handle exception
            }

        }
    }

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public CMSLimitCycleType(int type) {
        super();
        this.type = type;
    }

    public CMSLimitCycleType() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof CMSLimitCycleType))
            return false;
        CMSLimitCycleType that = (CMSLimitCycleType) obj;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type;
    }

    @Override
    protected Object clone() {
        return new CMSLimitCycleType(this.type);
    }

    public CMSLimitCycleType copy() {
        return (CMSLimitCycleType) clone();
    }

    @Override
    public String toString() {
        return valueToNameMap.get(this.type);
    }
}
