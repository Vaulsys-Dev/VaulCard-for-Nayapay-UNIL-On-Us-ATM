package vaulsys.security.base;

import vaulsys.config.ConfigurationManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

public class SecurityFunctionManager {

    private static SecurityFunctionManager functionManager;
    private final String defaultSecurityConfig = "security";

    private Map<String, String> functions;

    private SecurityFunctionManager() {
        // AccessManager.addDeviceDriver(VaulsysSSMDriver.getName(), VaulsysSSMDriver.getInstance());
        /*
           * ESMDriver driver = ESMDriver.getInstance(); AccessManager.addDeviceDriver(driver.getName(), driver);
           */
    }

    public void initiate() {
        functions = loadSecurityFunctionConfig(defaultSecurityConfig);
    }

    public void addSecuirtyFunctionConfig(String securityConfigName) {
        Map<String, String> f = loadSecurityFunctionConfig(securityConfigName);

        synchronized (functions) {
            if (functions == null)
                functions = new HashMap<String, String>();

            functions.putAll(f);
        }
    }


    public Map<String, String> loadSecurityFunctionConfig(String securityConfigName) {
        Map<String, String> functions = new HashMap<String, String>();
        Configuration config = ConfigurationManager.getInstance().getConfiguration(securityConfigName);

        String[] funcNameList = config.getStringArray("Security_Function/Function/Name");
        String[] funcHostList = config.getStringArray("Security_Function/Function/Host");
        int i = 0;
        for (String name : funcNameList) {
            String host = funcHostList[i++];
            functions.put(name, host);
        }
        return functions;
    }

    public static SecurityFunctionManager getInstance() {
        if (functionManager == null) {
            functionManager = new SecurityFunctionManager();
        }
        return functionManager;
    }

    public String getFunctionAddress(String functionName) {
        // TODO (IP)Address of machine on which the function should be run, must be found according to the function configuration file!
        return functions.get(functionName);
    }
}
