package vaulsys.util.coreLoadBalancer;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class CoreLoadBalancer {
    static Logger logger = Logger.getLogger(CoreLoadBalancer.class);

    private static List<CoreServerVO> normalServers = new ArrayList<CoreServerVO>();



    private static boolean inResetModeNormalServers = false;
    private static boolean initializedNormalServers = false;
    private static final int maxRetryCount = 5;
    private static final long retryDelay = 2000L;

    public static Boolean isChangedServersSize(List<CoreServer> servers, Integer coreServerUsageType) {
        Boolean isChanged = false;
        switch (CoreServerUsageType.convert(coreServerUsageType)) {
            case Normal:
                isChanged = servers.size() != normalServers.size();
                break;
        }
        return isChanged;
    }

    public static void resetNormalServersList(List<CoreServer> newNormalServers) {
        logger.debug("Start resetNormalServersList");
        inResetModeNormalServers = true;
//        synchronized (syncVarCoreServers) {
        normalServers.clear();
        for (CoreServer server : newNormalServers) {
        	if (Boolean.TRUE.equals(server.getEnabled())){
        		normalServers.add(new CoreServerVO(server));
        	}
        }
//        }
        logger.debug("End resetNormalServersList");
        inResetModeNormalServers = false;
        initializedNormalServers = true;
    }

    private static int index = 0;

    public static synchronized String getCoreURL() {
        int retry = 0;
        while (!initializedNormalServers || inResetModeNormalServers) {
            if (retry >= maxRetryCount)
                return "";

            retry++;
            try {
                Thread.sleep(retryDelay);
            } catch (Throwable e) {
                logger.warn(e, e);
            }
        }

//        synchronized (syncVarCoreServers) {
        int firstIndex;
        String url;

        try {
            firstIndex = index;

            do {
                url = normalServers.get(index).getURLForUse();
                if (url != null)
                    return url;

                index = (index + 1) % normalServers.size();

                if (index == firstIndex) {
                    url = normalServers.get(index).getURLForUse();
                    if (url != null) {
                        return url;
                    } else {
                        logger.warn("ALL Core Servers are in Restarting Mode !!!!!!!!!!!!!!!!!!!!!!!!!");
                        return null;
                    }
                }
            } while (true);

        } catch (Throwable e) {
            logger.error(e, e);
            return null;
        }
//        }
    }

    public static synchronized String getAnotherCoreURL(String coreURL) {
        int retry = 0;
        while (!initializedNormalServers || inResetModeNormalServers) {
            if (retry >= maxRetryCount)
                return "";

            retry++;
            try {
                Thread.sleep(retryDelay);
            } catch (Throwable e) {
                logger.warn(e, e);
            }
        }

//        synchronized (syncVarCoreServers) {
        int firstIndex;
        String url;

        try {
            firstIndex = index;

            do {
                url = normalServers.get(index).getURLForUse();
                if (url != null && !url.equals(coreURL))
                    return url;

                index = (index + 1) % normalServers.size();

                if (index == firstIndex) {
                    url = normalServers.get(index).getURLForUse();
                    if (url != null) {
                        return url;
                    } else {
                        logger.warn("ALL Core Servers are in Restarting Mode !!!!!!!!!!!!!!!!!!!!!!!!!");
                        return null;
                    }
                }
            } while (true);

        } catch (Exception e) {
            logger.error(e, e);
            return null;
        }
//        }
    }

}
