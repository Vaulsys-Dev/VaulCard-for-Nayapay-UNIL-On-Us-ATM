package vaulsys.security.hsm.base;


import vaulsys.config.ConfigurationManager;
import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.persistence.GeneralDao;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HSMChannelManager {

    Logger logger = Logger.getLogger(HSMChannelManager.class);

    private static HSMChannelManager manager;

    public static HSMChannelManager getInstance() {
        if (manager == null) {
            manager = new HSMChannelManager();
        }
        return manager;
    }

    private HSMChannelManager() {

    }

    public List<HSMChannel> readFromConfig() {

        Configuration config = ConfigurationManager.getInstance().getConfiguration("hsm-config");


        Map<String, String> IPAddresses = readIPAddresses(config);

        Map<String, String> myBinNumbers =readBinNumbers(config);

        Map<String, CommandType> commandTypes = readCommandTypes(config);




        logger.debug("Reading config file of HSM Channels ...");
        String[] names = config.getStringArray("Channel/@name");
        List<HSMChannel> hsmChannels = new ArrayList<HSMChannel>();
        try {
        for (String name : names) {

            String ip = IPAddresses.get(config.getString("Channel[@name='" + name + "']/address/ip"));
            int port = config.getInt("Channel[@name='" + name + "']/address/port");
            String ioFilter = config.getString("Channel[@name='" + name + "']/IOFilter");
            String bin = myBinNumbers.get(config.getString("Channel[@name='" + name + "']/bin"));
            CommandType commandType = commandTypes.get(config.getString("Channel[@name='" + name + "']/CommandType"));
            Long timeoutMilliSeconds = config.getLong("Channel[@name='" + name + "']/Timeout-MILLISECONDS");
            hsmChannels.add(new HSMChannel(bin,commandType,timeoutMilliSeconds));

        }
        } catch (Exception e) {
            logger.error("Encounter with an exception.( " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")",
                    e);
            return null;
        }
          return hsmChannels;
    }

    public List<HSMChannel> readFromDB() {

        List<HSMChannel> channelFromDB;
        Map<String, Object> dbParam;
        String channelType, dbQuery;
        List<HSMChannel> hsmChannels;
        CommandType commandType;

        logger.debug("Reading from DB ...");

        try {
            commandType = null;
            dbParam = new HashMap<String, Object>();
            hsmChannels = new ArrayList<HSMChannel>();

            channelType = "HSM";
            dbParam.put("channelType", channelType);

            dbQuery = "from " + HSMChannel.class.getName() + " c " +
                    "where " +
                    "channelType in (:channelType)";
            channelFromDB = GeneralDao.Instance.find(dbQuery, dbParam);

            for (HSMChannel channel : channelFromDB) {
                channel.setAddress(new InetSocketAddress(channel.getIp(), channel.getPort()));

                channel.setConnectionStatus(NetworkInfoStatus.SOCKET_RESET); //Raza Rest initially
                channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_RESET); //Raza Rest initially

                if (channel.getCommandTypeDesc().equals("Safenet"))
                    commandType = CommandType.SAFE_NET;
                else if (channel.getCommandTypeDesc().equals("Thales"))
                    commandType = CommandType.THALES;
                channel.setCommandType(commandType);

                channel.setIoFilterClassName(channel.getIoFilterClassName());

                hsmChannels.add(channel);

                if(GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) //Raza start updating status in DB
                {
                    GeneralDao.Instance.saveOrUpdate(channel);
                }
                else
                {
                    GeneralDao.Instance.beginTransaction();
                    GeneralDao.Instance.saveOrUpdate(channel);
                    GeneralDao.Instance.endTransaction();
                } //Raza end updating status in DB
            }
        } catch (Exception e) {
            logger.error("Encounter with an exception.( " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")",
                    e);
            return null;
        }
        return hsmChannels;
    }

    private Map<String, CommandType> readCommandTypes(Configuration config) {
       String[] commandTypes = config.getStringArray("CommandType/@name");
        Map<String,CommandType> myCommandTypes=null;

        if(commandTypes != null && commandTypes.length >0){
            myCommandTypes = new HashMap<String, CommandType>();
            for(String var : commandTypes){
                int commandType =config.getInt("CommandType[@name='" + var +"']/@value");
                switch(commandType) {
                    case 1:
                        myCommandTypes.put(var, CommandType.THALES);
                        break;
                    case 2:
                        myCommandTypes.put(var, CommandType.SAFE_NET);
                        break;
                }
            }
        }
        return myCommandTypes;
    }

    private Map<String,String> readBinNumbers(Configuration config) {
        String[] BINs = config.getStringArray("BIN/@name");
        Map<String, String> myBinNumbers = null;

        if (BINs != null && BINs.length > 0) {
            myBinNumbers = new HashMap<String, String>();
            for (String var : BINs) {
                myBinNumbers.put(var, config.getString("BIN[@name='" + var + "']/@value"));
            }
        }

        return  myBinNumbers;
    }

    private Map<String,String> readIPAddresses(Configuration config) {
        String[] IPs = config.getStringArray("IP/@name");
        Map<String, String> IPAddresses = null;
        if (IPs != null && IPs.length > 0) {
            IPAddresses = new HashMap<String, String>();
            for (String var : IPs) {
                IPAddresses.put(var, config.getString("IP[@name='" + var + "']/@value"));
            }
        }
        return  IPAddresses;
    }
}
