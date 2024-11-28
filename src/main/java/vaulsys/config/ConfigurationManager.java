package vaulsys.config;

import vaulsys.base.Manager;
import vaulsys.util.ConfigUtil;

import java.lang.reflect.Field;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.log4j.Logger;

public class ConfigurationManager implements Manager {

    transient Logger logger = Logger.getLogger(ConfigurationManager.class);

    public static final String DefaultCfgFile = ConfigUtil.getProperty(ConfigUtil.GLOBAL_PATH_FILE_DEFAULTCFGFILE);

    private static ConfigurationManager cfgManager = null;

    private CombinedConfiguration combinedConfiguration = null;

    DefaultConfigurationBuilder builder = null;

    private ConfigurationManager() {
        builder = new DefaultConfigurationBuilder();
        builder.setFileName(DefaultCfgFile);
        try {
            combinedConfiguration = builder.getConfiguration(true);
        } catch (ConfigurationException e) {
            logger.error("Exception in ConfigurationManager, "+e,  e);
        }

    }

    synchronized public static ConfigurationManager getInstance() {
        if (ConfigurationManager.cfgManager == null) {
            ConfigurationManager.cfgManager = new ConfigurationManager();
        }
        return ConfigurationManager.cfgManager;
    }

    public Configuration getConfiguration(String name) {
        return combinedConfiguration.getConfiguration(name);
    }

    @SuppressWarnings("unchecked")
    synchronized public void loadConfig(Object obj) {
        Class cls = obj.getClass();
        if (obj.getClass().getName().equals("java.lang.Class")) {
            cls = (Class) obj;
        }
        String className = cls.getName();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Configure cfg = field.getAnnotation(Configure.class);
            if (cfg != null) {
                String elementName = cfg.value();
                try {
                    Object value = getConfigValue(className, elementName);
                    Object val = cast(field.getType(), value);
                    field.set(obj, val);
                } catch (Exception ex) {
                    logger.error("Exception "+ex , ex);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object cast(Class type, Object value) {
        String fieldType = type.getName();
        if (fieldType.equals("int")) {
            return Integer.valueOf(value.toString());
        }
        if (fieldType.equals("long")) {
            return Long.valueOf(value.toString());
        }
        if (fieldType.equals("boolean")) {
            return Boolean.valueOf(value.toString());
        }
        if (fieldType.equals("java.lang.String")) {
            return String.valueOf(value);
        }
        return null;
    }

    private Object getConfigValue(String className, String field)
            throws Exception {

        String path = "classes/class[@name='" + className + "']/field[@name='"
                + field + "']/value";
        try {
            Object obj = getConfiguration("application").getProperty(path);
            return obj;
        } catch (Exception ex) {
            logger.error("Exception "+ex, ex);
            return null;
        }
    }

    public void shutdown() {
    }

    public void startup() {
    }
}
