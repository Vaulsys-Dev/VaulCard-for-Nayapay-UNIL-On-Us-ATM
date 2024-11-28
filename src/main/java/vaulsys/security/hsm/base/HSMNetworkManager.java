package vaulsys.security.hsm.base;


import vaulsys.base.Manager;
import vaulsys.netmgmt.extended.ConnectionManager;
import vaulsys.security.hsm.base.exception.NotAvailableHSMChannelFoundException;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IoSession;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HSMNetworkManager implements Manager {

    Logger logger = Logger.getLogger(HSMNetworkManager.class);
    private static HSMNetworkManager instance;


    private static Map<String, HSMChannel> hsmChannelsMapByIPAndPortNo;
    private Map<String, RoundRobin<HSMConnector>> hsmConnectedConnectors = new ConcurrentHashMap<String, RoundRobin<HSMConnector>>();
    private Map<String, List<HSMConnector>> hsmConnectors = new ConcurrentHashMap<String, List<HSMConnector>>();


    public static HSMNetworkManager getInstance() {


        if (instance == null)
            instance = new HSMNetworkManager();
        return instance;
    }


    private HSMNetworkManager() {
        hsmChannelsMapByIPAndPortNo = new HashMap<String, HSMChannel>();
    }

    @Override
    public void startup() throws Exception {
        startHSMChannels();
        logger.info("");
    }

    private void startHSMChannels() {
        //List<HSMChannel> hsmChannels = HSMChannelManager.getInstance().readFromConfig();
        Map<String, HSMChannel> hsmChannels = GlobalContext.getInstance().getAllHSMChannels();
        for (HSMChannel hsmChannel : hsmChannels.values()) {
            startHSMChannel(hsmChannel);
        }
    }

    public void startHSMChannel(HSMChannel hsmChannel) {
        List<IoFilter> filters = new ArrayList<IoFilter>();
        filters.add(hsmChannel.getIoFilterObject());
        HSMConnector hsmconnector = new HSMConnector(new HSMIOHandler(), filters, hsmChannel);
        hsmChannel.setConnector(hsmconnector);
        hsmChannelsMapByIPAndPortNo.put(hsmChannel.getIp() + ":" + hsmChannel.getPort(), hsmChannel);
        if (!hsmConnectors.containsKey(hsmChannel.getBin())) {
            hsmConnectors.put(hsmChannel.getBin(), (new ArrayList<HSMConnector>()));
            hsmConnectedConnectors.put(hsmChannel.getBin(), (new RoundRobin(new ArrayList<HSMConnector>())));
        }
        hsmConnectors.get(hsmChannel.getBin()).add(hsmconnector);
        hsmconnector.connect();
        //Raza for I am Client connection start
        Thread t = new Thread(new ConnectionManager(hsmconnector));
        t.setName(hsmChannel.getName() + "_ConnectionThread");
        t.setDaemon(false);
        ConnectionManager.ManageConnection(t);
        //t.start();
        //Raza for I am Client connection end
    }

    @Override
    public void shutdown() {
        closeAll();
    }

    private void closeAll() {
        for (HSMChannel channel : hsmChannelsMapByIPAndPortNo.values()) {
            channel.getConnector().close(true);
            channel.setConnector(null);
        }
    }

    //ToDO: handle exception and null in upper layer
    public byte[] sendRequestReceiveResponse(byte[] msg, String bin) throws InterruptedException, NotAvailableHSMChannelFoundException {
        Boolean isCompleted = false;
        HSMConnector hsmConnector;
        byte[] response = null;

        while (!isCompleted) {
            hsmConnector = getHSMConnector(bin);
            hsmConnector.getFairLock().lock();
            if (hsmConnector.getFairLock().isEnable()) {
                try {
                    isCompleted = true;
                    logger.info("Starting Thread " + Thread.currentThread().getId() + "hsmChannel: " + hsmConnector.getChannel().getName() + " with loadCount = " + hsmConnector.getLoadCount().get());
                    sendRequest(msg, hsmConnector);
                    response = receiveResponse(hsmConnector);
                    logger.info("Ending Thread " + Thread.currentThread().getId());
                } finally {
                    hsmConnector.getFairLock().unlock();
                }
            }
        }

        return response;
    }

    private void sendRequest(byte[] msg, HSMConnector hsmConnector) {
        hsmConnector.getSession().write(msg);
    }

    private byte[] receiveResponse(HSMConnector hsmConnector) throws InterruptedException {
        byte[] response = ((HSMIOHandler) (hsmConnector.getHandler())).getReceivedMessage();

        try {
            if (response != null) {
                logger.debug("Not Null Response Received from HSM [" + new String(response, "UTF-8") + "]");
            } else {
                logger.error("Null Response Received from HSM");
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while logging HSM Response.... [" + e.getMessage() + "]");
        }

        //logger.info("HSMNetworkManager:: Response Received from HSM [" + new String(response) + "]"); //Raza TEMP
        return response;
    }


    public HSMConnector getHSMConnector(String bin) throws NotAvailableHSMChannelFoundException {

        RoundRobin<HSMConnector> hsmConnectorsPerBank = hsmConnectedConnectors.get(bin);

        HSMConnector selectedHSMConnector = applyRoundRobinAlgorithmForLoadBalancing(hsmConnectorsPerBank);

        selectedHSMConnector = getConnectorBaseOnLeastLoad(hsmConnectorsPerBank.getCollection(), selectedHSMConnector);

        selectedHSMConnector.getLoadCount().incrementAndGet();

        return selectedHSMConnector;
    }


    private HSMConnector applyRoundRobinAlgorithmForLoadBalancing(RoundRobin<HSMConnector> connectorsList) throws NotAvailableHSMChannelFoundException {
        HSMConnector selectedHSMConnector = null;
        if (connectorsList.getCollection().size() == 0 || connectorsList.getCollection() == null) {
            logger.error("*****************None of hsmChannels is connected*************************");
            throw new NotAvailableHSMChannelFoundException("No Open HSM Channel is found");
        } else {
            selectedHSMConnector = connectorsList.iterator().next();
            int i = 0;
            while ((Boolean.TRUE.equals(selectedHSMConnector.getIsClosing()) || Boolean.FALSE.equals(selectedHSMConnector.getIsSessionConnected().get())) && i < connectorsList.getCollection().size()) {
                selectedHSMConnector = connectorsList.iterator().next();
            }
            if (selectedHSMConnector == null || Boolean.TRUE.equals(selectedHSMConnector.getIsClosing()) || Boolean.FALSE.equals(selectedHSMConnector.getIsSessionConnected().get())) {
                logger.error("*****************None of hsmChannels is connected*************************");
                throw new NotAvailableHSMChannelFoundException("No Open HSM Channel is found");
            }
        }
        return selectedHSMConnector;
    }

    private HSMConnector getConnectorBaseOnLeastLoad(List<HSMConnector> hsmConnectorsPerBank, HSMConnector selectedHSMConnector) {
        int count = selectedHSMConnector.getLoadCount().get();

        for (HSMConnector hsmConnector : hsmConnectorsPerBank) {
            if (count > hsmConnector.getLoadCount().get() && Boolean.TRUE.equals(hsmConnector.getIsSessionConnected().get())) {
                selectedHSMConnector = hsmConnector;
                count = selectedHSMConnector.getLoadCount().get();
            }
        }
        return selectedHSMConnector;
    }

    public HSMChannel getChannelOfSession(IoSession session) {
        InetSocketAddress address = ((InetSocketAddress) session.getServiceAddress());
        return hsmChannelsMapByIPAndPortNo.get(address.getAddress().getHostAddress() + ":" + address.getPort());
    }

    public void addToNotConnectedConnectors(HSMConnector hsmConnector) {
//        GlobalContext.getInstance().addConnector(hsmConnector.getChannel().getIP() + ":" + hsmConnector.getChannel().getPort(), hsmConnector);

    }

    public void addToConnectedConnectorList(HSMConnector connector) {
        if (!hsmConnectedConnectors.containsKey(connector.getChannel().getBin())) {
            hsmConnectedConnectors.put(connector.getChannel().getBin(), (new RoundRobin(new ArrayList<HSMConnector>())));
        }
        if (!hsmConnectedConnectors.get(connector.getChannel().getBin()).getCollection().contains(connector)) {
            hsmConnectedConnectors.get(connector.getChannel().getBin()).add(connector);
        }

    }

    public void removeFromConnectedConnectorList(HSMConnector connector) {
        if (hsmConnectedConnectors.containsKey(connector.getChannel().getBin())) {
            hsmConnectedConnectors.get(connector.getChannel().getBin()).getCollection().remove(connector);
        }

    }

    //m.rehman: 22-11-2021, HSM response logging
    public void sendRequestToHSM(byte[] msg, String bin) throws InterruptedException, NotAvailableHSMChannelFoundException {
        Boolean isCompleted = false;
        HSMConnector hsmConnector;
        byte[] response = null;

        while (!isCompleted) {
            hsmConnector = getHSMConnector(bin);
            hsmConnector.getFairLock().lock();
            if (hsmConnector.getFairLock().isEnable()) {
                try {
                    isCompleted = true;
                    logger.info("Starting Thread " + Thread.currentThread().getId() + "hsmChannel: " + hsmConnector.getChannel().getName() + " with loadCount = " + hsmConnector.getLoadCount().get());
                    sendRequest(msg, hsmConnector);
                    //response = receiveResponse(hsmConnector);
                    logger.info("Ending Thread " + Thread.currentThread().getId());
                } finally {
                    hsmConnector.getFairLock().unlock();
                }
            }
        }

        //return response;
    }

    public void receiveResponseFromHSM(byte[] msg, HSMChannel hsmChannel) throws InterruptedException, NotAvailableHSMChannelFoundException,
            UnsupportedEncodingException {
        //String identifier;
        String hsmResponse = new String(msg, "UTF-8");
        if (hsmChannel != null && Util.hasText(hsmChannel.getCommandTypeDesc())
                && hsmChannel.getCommandTypeDesc().equals(CommandType.ATALLA.toString())) {
            if (hsmResponse.contains("^")) {
                int index = hsmResponse.indexOf("^");
                index += 1;
                String sequence = hsmResponse.substring(index, hsmResponse.indexOf("#",index));

                //Long threadId = Long.parseLong(sequence);
                String threadName = sequence;
                Thread origTxn = null;
                Set<Thread> threads = Thread.getAllStackTraces().keySet();
                for (Thread thread : threads) {
                    if (thread.getName().contains(threadName)) {
                        origTxn = thread;
                        GlobalContext.getInstance().setHsmResponse(thread.getName(), msg);
                        break;
                    }
                }

                if (origTxn != null) {
                    logger.info("Original Thread found with Name [" + threadName + "]");
                    synchronized (origTxn) {
                        origTxn.notify();
                    }

                } else {
                    logger.info("No Thread found with Name [" + threadName + "]");
                }
            } else {
                logger.error("sequence not found in response message ...");
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}



