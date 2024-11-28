package vaulsys.network;

import vaulsys.base.Manager;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.netmgmt.extended.ConnectionManager;
import vaulsys.network.channel.base.*;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.network.mina2.Mina2Acceptor;
import vaulsys.network.mina2.Mina2Connector;
import vaulsys.network.mina2.Mina2IoHandler;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.transaction.TransactionType;
import vaulsys.util.SwitchContext;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IoSession;

public class NetworkManager extends SwitchContext implements Manager {
    private static final Logger logger = Logger.getLogger(NetworkManager.class);
    private static NetworkManager networkManager = null;
    private static List<String> terminalNewConnections;

    private ConcurrentHashMap<Long, IoSession> responseOnSameSocketConnections;
    private Map<String, IoSession> terminalOpenConnections;
    private Map<Integer, Channel> inputChannelsMapByPortNo;
    private Map<Integer, Channel> outputSameSocketChannelsMapByPortNo;
    private Map<String, Channel> outputChannelsMapByIPAndPortNo;

    //For TPS generation
    public int numMessagesSent = 0;
    public Long prevNTransactionTime = 0L;
    public long tempTimeDifference = 0L;
    public float TPS = 0;
    public int modNumMessage = 0;
    final int maxMessagesInTPS = 30;
    LinkedList<Long> times = new LinkedList<Long>();

    //public static NetworkInfo[] NetworkInfoElements; //Raza TPSP Channel Add
    private NetworkManager() {
        inputChannelsMapByPortNo = new HashMap<Integer, Channel>();
        outputSameSocketChannelsMapByPortNo = new HashMap<Integer, Channel>();
        outputChannelsMapByIPAndPortNo = new HashMap<String, Channel>();
        responseOnSameSocketConnections = new ConcurrentHashMap<Long, IoSession>();
        terminalOpenConnections = new HashMap<String, IoSession>();
        NetworkManager.terminalNewConnections = Collections.synchronizedList(new ArrayList<String>());
   }

    public static NetworkManager getInstance() {
        if (networkManager == null) {
            networkManager = new NetworkManager();
        }
        return networkManager;
    }

    public ConcurrentHashMap<Long, IoSession> getResponseOnSameSocketConnections() {
    	return responseOnSameSocketConnections;
    }
    
    public IoSession getResponseOnSameSocketConnectionById(Long msgId) {
        return responseOnSameSocketConnections.get(msgId);
    }

    public void addResponseOnSameSocketConnection(Long msgId, IoSession session) {
        responseOnSameSocketConnections.put(msgId, session);
    }

    public void removeResponseOnSameSocketConnectionById(Long msgId) {
        responseOnSameSocketConnections.remove(msgId);
    }

    public Map<String, IoSession> getTerminalOpenConnections() {
		return terminalOpenConnections;
	}

    public IoSession getTerminalOpenConnection(String IP) {
    	return terminalOpenConnections.get(IP);
    }

    public void addTerminalOpenConnection(String IP, IoSession session){
    	terminalOpenConnections.put(IP, session);
    }

    public void removeTerminalOpenConnection(String IP){
    	terminalOpenConnections.remove(IP);
    }

    @Override
    public void shutdown() {
        closeAll();
    }

    private void closeAll() {
		Map<String, Channel> channels = GlobalContext.getInstance().getAllChannels();
		for (Channel chn : channels.values()) {
			if (chn instanceof InputChannel) {
				InputChannel ichn = (InputChannel) chn;
				ichn.getAcceptor().close();
				ichn.setAcceptor(null);
			} else {
				OutputChannel ochn = (OutputChannel) chn;
				if (ochn.isKeepAlive())
					ochn.getConnector().close(true);
				ochn.setConnector(null);
			}
		}
	}

    @Override
    public void startup() throws Exception {
        startupKeepAliveOutputChannels();
        startupInputChannels();
        startupWebServiceChannels();
    }

    static public void addATMConnection(String hostAddress){
		terminalNewConnections.add(hostAddress);
    }
    
    static public void removeATMConnection(String hostAddress){
    	terminalNewConnections.remove(hostAddress);
    }
    
    static public void removeATMConnection(List<String> hostAddress){
    	terminalNewConnections.removeAll(hostAddress);
    }
    
    private void startupInputChannels() throws Exception {
        Map<String, Channel> channels = GlobalContext.getInstance().getAllChannels();

        for (Channel channel : channels.values())
            if (channel instanceof InputChannel && channel.getChannelType().equals(ChannelType.CHANNEL.toString()))
            	startInputChannel((InputChannel) channel);
    }
    
    public void startInputChannel(InputChannel ichn) {
        List<IoFilter> filters = new ArrayList<IoFilter>();
        filters.add(ichn.getIoFilterObject());

        Mina2Acceptor acceptor = new Mina2Acceptor(ichn, filters, new Mina2IoHandler());
        try {
            acceptor.listen();
			ichn.setAcceptor(acceptor);
			ichn.setOpen(true);
			inputChannelsMapByPortNo.put(ichn.getPort(), ichn);
			logger.info("Listenning to: " + ichn.getName() + " to\t " + ichn.getIp() + ":" + ichn.getPort());
        } catch (IOException e) {
			ichn.setOpen(false);
			ichn.setAcceptor(null);
            logger.error("Cannot listen on channel[" + ichn.getName()+"] ("+e.getClass().getSimpleName()+": "+ e.getMessage()+")");
        }
    }
    
    public void stopInputChannel(InputChannel ichn) {
    	Mina2Acceptor acceptor = ichn.getAcceptor();
    	if(acceptor!=null) {
    		acceptor.close();
    		ichn.setAcceptor(null);
    		ichn.setOpen(false);
    		inputChannelsMapByPortNo.remove(ichn.getPort());
    		logger.info("Channel Stopped: "+ichn.getName());
    	}
    }

    private void startupKeepAliveOutputChannels() throws Exception {
        Map<String, Channel> channels = GlobalContext.getInstance().getAllChannels();

        for (Channel channel : channels.values()) {
            if (channel instanceof OutputChannel && channel.getChannelType().equals(ChannelType.CHANNEL.toString())) {
                	startKeepAliveOutputChannel((OutputChannel) channel);
            }
        }
    }

    private void startupWebServiceChannels() throws Exception { //Raza adding for WebServers
        Map<String, Channel> channels = GlobalContext.getInstance().getAllChannels();

        for (Channel channel : channels.values()) {
            if (channel.getChannelType().equals(ChannelType.WEBSERVER.toString()) && channel.getNetworkType().equals(NetworkType.CHANNEL_SERVER_IN.toString())) {
                startWebServiceChannel(channel);
            }
            else if(channel.getChannelType().equals(ChannelType.WEBSERVER.toString()) && channel.getNetworkType().equals(NetworkType.CHANNEL_CLIENT_OUT.toString()))
            {
                if(channel.getPingreq())
                {
                    startPingForWebServiceChannel(channel);
                }
            }
        }
    }
    
    public void startKeepAliveOutputChannel(OutputChannel channel) {
    	if(!channel.isKeepAlive())
    		return;
    	
        List<IoFilter> filters = new ArrayList<IoFilter>();
        filters.add(channel.getIoFilterObject());

        outputChannelsMapByIPAndPortNo.put(channel.getIp()+":"+channel.getPort(), channel);
        if(channel.getCommunicationMethod().equals(CommunicationMethod.SAME_SOCKET)){
//        	inputChannelsMapByPortNo.put(channel.getPort(), channel);
        	outputSameSocketChannelsMapByPortNo.put(channel.getPort(), channel);
        }

        Mina2Connector connector = new Mina2Connector(channel, filters, new Mina2IoHandler());
        //if (!connector.connect()) {
            //TODO at first time we try to connect, but if the connection is failed, we postpone it to the first time we want to use it.
			//throw new Exception("Connection failed.");
        //}
        channel.setConnector(connector);
        channel.setOpen(true);
        connector.connect();
        //Raza for I am Client connection start
        if(channel.getMonreq()) {
            Thread t = new Thread(new ConnectionManager(connector));
            t.setName(channel.getName() + "_ConnectionThread");
            t.setDaemon(false);
            ConnectionManager.ManageConnection(t);
            //t.start();
        }
        //Raza for I am Client connection end
    }

    public void startWebServiceChannel(Channel channel) {

        try {
            logger.info("Starting WebServer [" + channel.getName() + "]");
            Class<? extends Thread> clazz = (Class<? extends Thread>) Class.forName(channel.getProtocolClass());
            Thread t = new Thread(clazz.newInstance());
            t.setName(channel.getName() + "Thread");
            t.setDaemon(false);
            t.start();
        }
        catch (Exception e)
        {
            logger.error("Exception caught while bringing [" + channel.getName() + "] up");
            e.printStackTrace();
        }
    }

    public void startPingForWebServiceChannel(Channel channel) {

        try {
         //Create New Thread and send Ping through HttpClient -- Do this in Connection Manager
        //Raza for I am Client connection start
            if (channel.getPingreq()) {
                Thread t = new Thread(new ConnectionManager(channel));
                t.setName(channel.getName() + "_ConnectionThread");
                t.setDaemon(false);
                ConnectionManager.ManageConnection(t);
                //t.start();
            }
            //Raza for I am Client connection end


        }
        catch (Exception e)
        {
            logger.error("Exception caught while bringing [" + channel.getName() + "] up");
        }
    }

    public void stopKeepAliveOutputChannel(OutputChannel channel) {
    	if(!channel.isKeepAlive())
    		return;
    	
        channel.getConnector().close(true);
        channel.setConnector(null);
        outputChannelsMapByIPAndPortNo.remove(channel.getIp()+":"+channel.getPort());
        if(channel.getCommunicationMethod().equals(CommunicationMethod.SAME_SOCKET)){
//            inputChannelsMapByPortNo.remove(channel.getPort());
        	for (int port : outputSameSocketChannelsMapByPortNo.keySet()){
        		if (outputSameSocketChannelsMapByPortNo.get(port).equals(channel))
        			outputSameSocketChannelsMapByPortNo.remove(outputSameSocketChannelsMapByPortNo.get(port));
        	}
        }
        channel.setOpen(false);
    }

    public void suspendChannel(Channel channel) {
        channel.setIsSuspended(true);
    }

    public List<ScheduleMessage> sendMessage(Message messageToSend) {
    	List<ScheduleMessage> reverseMessage = new ArrayList<ScheduleMessage>();
    	logger.debug("in sendMessage");
//	if (true) return null;
    	boolean time_out_rs = false;
    	if(messageToSend == null){
    		logger.warn("messageToSend is null!!!");
    		return reverseMessage;
    	}
	/**
	 * @author k.khodadi
	 * for Transfer sooushi from File TXT  
	 */
    	/*
        try {
 		   	 if(    ProcessContext.get().getInputMessage().getIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RS) 
	 		   	&& (ProcessContext.get().getInputMessage().getTransaction().getFirstTransaction().getTransactionType().equals(TransactionType.SELF_GENERATED))
	 		   	&&  TransactionService.IsSorush(ProcessContext.get().getInputMessage().getIfx())){
 		   		 return null;
 		   	 }
        } catch (Exception e) {
            logger.error("Exception in detecting sorush:" + e.toString());
            logger.error(e, e);
        }
    	*/
    	
    	
    	logger.debug("before if");
    
    	if (messageToSend.getNeedToBeSent() != null && messageToSend.getNeedToBeSent()){
    		//try to send message on channel
            try {
                boolean instantlyNeedToReverse = false;
                Channel channel = messageToSend.getChannel();

                Transaction transaction = messageToSend.getTransaction();

                logger.debug("Channel : " + channel.getName() + "," + channel.getIp());
                
         
                Ifx ifxToSend = messageToSend.getIfx();
				if (channel instanceof OutputChannel
                      ||
                      (TransactionType.SELF_GENERATED.equals(transaction.getTransactionType())
                    	&& !SchedulerConsts.TIME_OUT_MSG_TYPE.equals(((ScheduleMessage)transaction.getInputMessage()).getMessageType())
                    	&& !SchedulerConsts.REVERSAL_TIME_OUT_MSG_TYPE.equals(((ScheduleMessage)transaction.getInputMessage()).getMessageType()))
                      ) {
                	logger.debug("channel instanceof OutputChannel");


                    OutputChannel outChannel = (OutputChannel) channel;
                    Mina2Connector connector;
                    IoSession session = null;
                    
                	if(channel.isKeepAlive()){
                    	logger.debug("outChannel.isKeepAlive");
                    	connector = outChannel.getConnector();
                    	session = connector.getSession();
                    	if(session == null || 
                    		(session != null &&!session.isConnected()) ){
                    		instantlyNeedToReverse = true;
                    	}
                    } else {
                    	logger.debug("else outChannel.isKeepAlive");
                        List<IoFilter> filters = new ArrayList<IoFilter>();
                        filters.add(channel.getIoFilterObject());

                        connector = new Mina2Connector(outChannel, filters, new Mina2IoHandler());
                        session = connector.getSession();
                        instantlyNeedToReverse = (session == null);
                    }

                	if (!messageToSend.getNeedToBeInstantlyReversed())
                        instantlyNeedToReverse = false;

                	if (session != null && session.isConnected()) {
                        logger.info("Writing to Session - ResponseOnAnotherSocket - session :" + session.toString());
                        byte[] sendingMsg = messageToSend.getBinaryData();

                        //System.out.println("NetworkManager:: messageToSend Msg ID [" + messageToSend.getId() + "]"); //Raza TEMP
                        //System.out.println("NetworkManager:: messageToSend Msg IFX-ID [" + messageToSend.getIfx().getId() + "]"); //Raza TEMP
                        //System.out.println("NetworkManager:: messageToSend Msg Binary Data get [" + messageToSend.getBinaryData() + "]"); //Raza TEMP

                        //System.out.println("NetworkManager:: Here 1"); //Raza TEMP
                        //String temp = new String(sendingMsg);
                        //System.out.println("NetworkManager:: sendingMsg [" + temp + "]"); //Raza TEMP
                        outputSameSocketChannelsMapByPortNo.put(((InetSocketAddress)session.getLocalAddress()).getPort(), channel);
                        //System.out.println("NetworkManager:: IP-Address [" + session.getLocalAddress() + "]"); //Raza TEMP
                        //System.out.println("NetworkManager:: Port [" + ((InetSocketAddress)session.getLocalAddress()).getPort() + "]"); //Raza TEMP
                        session.write(sendingMsg);
                        logger.info("A message is sent: " + new String(Hex.encode(sendingMsg)));
                    } else {
                        logger.warn("********** SESSION OF OUTPUTCHANNEL '"+ outChannel.getName() +"' IS CLOSED BAD BAD BAD **********:" + " isCon:"
                                + (session != null ? session.isConnected() : "null"));
                        logger.warn("Writing to Session Failed - ResponseOnSameSocket - session is closed");
                        if (ifxToSend== null || !ISOFinalMessageType.isKeepRepeatTrigger(ifxToSend.getIfxType()))
                        	SchedulerService.removeReversalJobInfo(messageToSend.getTransaction().getId());
                        if (messageToSend.getRequest() && !TransactionType.SELF_GENERATED.equals(transaction.getTransactionType()))
                        	time_out_rs = true;
                    }

                } else if (channel instanceof InputChannel) {
                	logger.debug("channel instanceof InputChannel");
                    Message originatorMessage = null;

                    if (TransactionType.SELF_GENERATED.equals(transaction.getTransactionType())){
                    	logger.debug("TransactionType.SELF_GENERATED");
                    	originatorMessage = transaction.getReferenceTransaction().getInputMessage();
                    }else{
                    	logger.debug("else TransactionType.SELF_GENERATED");
                    	if (ifxToSend!= null && (IfxType.TRANSFER_RS.equals(ifxToSend.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(ifxToSend.getIfxType()))) {
                    		logger.debug("first if");
                    		if (transaction.getReferenceTransaction() == null){
                    			originatorMessage = transaction.getFirstTransaction().getInputMessage();
                    		}else
                    			originatorMessage = transaction.getReferenceTransaction().getInputMessage();
                    		
                    		if ((TerminalType.PINPAD.equals(ifxToSend.getTerminalType()) || 
                    				TerminalType.KIOSK_CARD_PRESENT.equals(ifxToSend.getTerminalType()) ||
                    				TerminalType.POS.equals(ifxToSend.getTerminalType())) &&
//                        			ifxToSend.getBankId().equals(GlobalContext.getInstance().getMyInstitution().getBin())) {
                    				ifxToSend.getBankId().equals(ProcessContext.get().getMyInstitution().getBin())) {
                    			if (IfxType.TRANSFER_RQ.equals(transaction.getFirstTransaction().getIncomingIfx().getIfxType()) ||
                    					IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(transaction.getFirstTransaction().getIncomingIfx().getIfxType())) {
                    				originatorMessage = transaction.getFirstTransaction().getInputMessage();
                    			} else 
                    				if (IfxType.TRANSFER_RQ.equals(transaction.getReferenceTransaction().getIncomingIfx().getIfxType())||
                    						IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(transaction.getReferenceTransaction().getIncomingIfx().getIfxType())) {
                    					originatorMessage = transaction.getReferenceTransaction().getInputMessage();
                    				}
                    		}
                    	} else if (ifxToSend!= null &&
                    			(IfxType.TRANSFER_REV_REPEAT_RS.equals(ifxToSend.getIfxType()) ||
                    					IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS.equals(ifxToSend.getIfxType())
                    					/*|| IfxType.TRANSFER_REV_RS.equals(messageToSend.getIfx().getIfxType())*/
                    				)) {
                    		logger.debug("second if");
                    		originatorMessage = transaction.getFirstTransaction().getFirstTransaction().getInputMessage();
                    	}

                    	else if (ifxToSend != null
                            && !ISOFinalMessageType.isReversalMessage(ifxToSend.getIfxType())
                            && !IfxType.CANCEL.equals(ifxToSend.getIfxType())
                            && !ISOFinalMessageType.isPrepareMessage(ifxToSend.getIfxType())
                            && !ISOFinalMessageType.isPrepareReversalMessage(ifxToSend.getIfxType())
                            && !IfxType.PARTIAL_DISPENSE_RS.equals(ifxToSend.getIfxType())
                            ){
                    		logger.debug("third if");
                    		originatorMessage = transaction.getFirstTransaction().getInputMessage();
                    	}else{
                    		logger.debug("else...");
                    		originatorMessage = transaction.getInputMessage();
                    	}
                    }

                    if (originatorMessage.isIncomingMessage()){
                        if(channel.getSrcTPDULen() > 0){
                        	Long srcTPDU = originatorMessage.getSrcTPDU();
                        	byte[] newSentByte = new byte[channel.getSrcTPDULen() + messageToSend.getBinaryData().length];
                        	
                        	for (int i = channel.getSrcTPDULen() - 1; i >= 0; i--) {
                        		newSentByte[i] = srcTPDU.byteValue();
                        		srcTPDU = (srcTPDU / 256);
                            }
                        	
                        	byte[] sendingMsg = messageToSend.getBinaryData();
                        	
                        	System.arraycopy(sendingMsg, 0, newSentByte, channel.getSrcTPDULen(), sendingMsg.length);                        	
                        	messageToSend.setBinaryData(newSentByte);
                        }

                		logger.debug("if originatorMessage.isIncomingMessage");
                        IoSession session = getResponseOnSameSocketConnectionById(originatorMessage.getId());
                        
                        if (channel.getEndPointType().equals(EndPointType.ATM_TERMINAL)) {
                        	String query = "select IP from ATMTerminal where code=:code";
                        	Map<String, Object> params = new HashMap<String, Object>();
                        	params.put("code", messageToSend.getEndPointTerminal().getCode());
                        	String ip = (String) GeneralDao.Instance.findObject(query, params);
                        	session = getTerminalOpenConnection(ip);
                        }

                        if (session != null && session.isConnected()) {
                            logger.info("Writing to Session - ResponseOnSameSocket - session :" + session.toString());
                            byte[] sendingMsg = messageToSend.getBinaryData();
                            session.write(messageToSend.getBinaryData());
                            //System.out.println("NetworkManager:: Here 2"); //Raza TEMP
                            //System.out.println("NetworkManager:: IP-Address [" + session.getLocalAddress() + "]"); //Raza TEMP
                            logger.info("A message is sent: " + new String(Hex.encode(sendingMsg)));
                        } else {
                            logger.warn("********** SESSION OF INPUTCHANELL '"+ channel.getName() +"' IS CLOSED BAD BAD BAD **********:" + " isCon:"
                                    + (session != null ? session.isConnected() : "null"));
                            logger.warn("Writing to Session Failed - ResponseOnSameSocket - session is closed");
                            if (ifxToSend== null || !ISOFinalMessageType.isKeepRepeatTrigger(ifxToSend.getIfxType()))
                            	SchedulerService.removeReversalJobInfo(messageToSend.getTransaction().getId());

                           	if (messageToSend.getNeedToBeInstantlyReversed())
                                instantlyNeedToReverse = true;
                           	if (messageToSend.getRequest() && !TransactionType.SELF_GENERATED.equals(transaction.getTransactionType()))
                           		time_out_rs = true;
                        }
                        removeResponseOnSameSocketConnectionById(originatorMessage.getId());
                    }
                }

//                setDoneFlag(transaction);
//        		logger.debug("after setDoneFlag");
//                if ( messageToSend.getPendingRequests() != null && !messageToSend.getPendingRequests().isEmpty()){
//                	MessageManager.getInstance().putRequests(messageToSend.getPendingRequests());
//            		logger.debug("after messageToSend.getPendingRequests()");
//                }

                if (instantlyNeedToReverse){
                	reverseMessage.add(SchedulerService.addReversalAndRepeatTrigger(messageToSend));
            		logger.debug("after  addReversalAndRepeatTrigger");
                }
                messageToSend.setNeedToBeInstantlyReversed(false);

                if (time_out_rs){
                	try {
                		reverseMessage.add(addTimeOutResponse(messageToSend));
						logger.debug("after addTimeOutResponse");
					} catch (Exception e) {
						logger.debug("TimeOutResponse could not be added! "+ e);
					}
                }
            } catch (Exception e) {
                logger.error("Exception in NetworkManager.sendMessage():" + e.toString());
                logger.error(e, e);
            }
        } // messageToSend.getSent() == true;

    	if (messageToSend.getNeedToBeInstantlyReversed() != null && messageToSend.getNeedToBeInstantlyReversed()){
    		reverseMessage.add(SchedulerService.addReversalAndRepeatTrigger(messageToSend));
			logger.debug("after  addReversalAndRepeatTrigger 2 ");
        }

    	return reverseMessage;
    }

    public void setDoneFlag(Transaction transaction) {
//        transaction.setStatus(TransactionStatus.DONE);
//        transaction.setEndDateTime(DateTime.now());
//        GeneralDao.Instance.saveOrUpdate(transaction);
    }

    private ScheduleMessage addTimeOutResponse(Message messageToSend){
    	TransactionService.updateMessageForNotSuccessful(messageToSend.getIfx(), messageToSend.getTransaction());

    	ScheduleMessage timeOutMsg = SchedulerService.createTimeOutMsgScheduleMsg(messageToSend.getTransaction(), ISOResponseCodes.INVALID_TO_ACCOUNT);
//        MessageManager.getInstance().putRequest(timeOutMsg);
    	return timeOutMsg;
    }

    public void receiveMessage(Channel channel, IoSession session, byte[] binaryData, long time) {
        logger.warn("Message received:" + new String(Hex.encode(binaryData))+" Session: "+session.toString());
//  if (true) return ;
        Message receivedMessage = new Message(MessageType.INCOMING);
        
        if(channel.getSrcTPDULen() > 0){
        	long srcTPDU = 0;
        	for(int i=0; i<channel.getSrcTPDULen(); i++){
        		srcTPDU = (srcTPDU * 256) + HSMUtil.byteToInt(binaryData[i]);
        	}
        	receivedMessage.setSrcTPDU(srcTPDU);
        
//        	byte[] b = Arrays.copyOfRange(binaryData, channel.getSrcTPDULen(), binaryData.length);
//        	receivedMessage.setBinaryData(b);

                int index = channel.getSrcTPDULen();
                int LRILength = 0;
                if((channel.getIoFilterClassName().indexOf("Meganac") > 0 || channel.getIoFilterClassName().indexOf("NCC") > 0) &&
                                binaryData[index] == (byte)'L' && binaryData[index+1] == (byte)'R' && binaryData[index+2] == (byte)'I') {
                        logger.info("LRI is detected....");
                        index += 3; //ignore LRI
                        index += 2; //ignore LLLL

                        try{
                                String ANI = ISOUtil.bcd2str(binaryData, index, 16, true);
                                logger.info("ANI: "+ANI);
                                 if(channel.getIoFilterClassName().indexOf("NCC") > 0 && ANI.contains("F"))
                                        ANI = ANI.replaceAll("F", "");
                                receivedMessage.setANI(ANI);
                                index += 8;

                                String DNIS = ISOUtil.bcd2str(binaryData, index, 16, true);
                                logger.info("DNIS: "+DNIS);
                                if(channel.getIoFilterClassName().indexOf("NCC") > 0 && DNIS.contains("F"))
                                        DNIS = DNIS.replaceAll("F", "");
                                receivedMessage.setDNIS(DNIS);
                                index += 8;

                                String LRI = "";
                                if(channel.getIoFilterClassName().indexOf("Meganac") > 0){
                                        index += 3; //ignore X<LLLL>
                                        index += 3; //ignore I<LLLL>
                                        LRI = new String(Hex.encode(binaryData, index, 6));
                                }else if(channel.getIoFilterClassName().indexOf("NCC") > 0){
                                        LRI = new String(Hex.encode(binaryData, index, 12));
                                }
                                logger.info("LRI: "+LRI);
                                receivedMessage.setLRI(LRI);
                        }catch(Exception e){
                                logger.error(e,e);
                        }
/*
                        index += 3; //ignore X<LLLL>
                        index += 3; //ignore I<LLLL>

                        String LRI = new String(Hex.encode(binaryData, index, 6));
                        logger.info("LRI: "+LRI);
                        receivedMessage.setLRI(LRI);
*/
                        LRILength = 3 + //LRI
                                        2 + //LLLL
                                        28; //Fixed Length
                }

                byte[] b = Arrays.copyOfRange(binaryData, channel.getSrcTPDULen()+LRILength, binaryData.length);
                receivedMessage.setBinaryData(b);




        }else{
        	receivedMessage.setBinaryData(binaryData);
        }
        receivedMessage.setChannel(channel);

        Transaction transaction = new Transaction(TransactionType.EXTERNAL);
        transaction.setInputMessage(receivedMessage);
//        transaction.setStatus(TransactionStatus.RECEIVED);
        transaction.setFirstTransaction(transaction);
        receivedMessage.setTransaction(transaction);

        MessageManager.getInstance().putRequest(receivedMessage, session, time);
    }

    public Channel getInputChannelOfSession(IoSession session) {
        return inputChannelsMapByPortNo.get(((InetSocketAddress) session.getServiceAddress()).getPort());
    }

    public Channel getSameSocketOutputChannelOfSession(IoSession session) {
    	return outputSameSocketChannelsMapByPortNo.get(((InetSocketAddress) session.getLocalAddress()).getPort());
    }
    
    public void removeSameSocketOutputChannel(IoSession session){
    	outputSameSocketChannelsMapByPortNo.remove(((InetSocketAddress) session.getLocalAddress()).getPort());
    }
    
    public Channel getOutputChannelOfSession(IoSession session) {
    	InetSocketAddress addr  = ((InetSocketAddress) session.getServiceAddress());
        return outputChannelsMapByIPAndPortNo.get(addr.getAddress().getHostAddress()+":"+addr.getPort());
    }
    /*public static void SetCommsUP(Channel chn)
    {
        if(NetworkManager.NetworkInfoElements != null) {
            for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                if(NetInfoElement != null) {
                    if (NetInfoElement.NetName == chn.getName()) {
                        NetInfoElement.setConnect_status(NetworkInfoStatus.SOCKET_CONNECTED);
                    }
                }
            }
        }
        else
        {
            logger.info("Network Info not Loaded or is Empty...!");
        }
    }*/
    /*public static void SetCommsDOWN(Channel chn)
    {
        if(NetworkManager.NetworkInfoElements != null) {
            for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                if(NetInfoElement != null) {
                    if (NetInfoElement.NetName == chn.getName()) {
                        NetInfoElement.setConnect_status(NetworkInfoStatus.SOCKET_DISCONNECTED);
                    }
                }
            }
        }
        else
        {
            logger.info("Network Info not Loaded or is Empty...!");
        }
    }*/
    /*public static int GetCommsStatus(Channel chn)
    {
        if(NetworkManager.NetworkInfoElements != null) {
            for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                if(NetInfoElement != null) {
                    if (NetInfoElement.NetName == chn.getName()) {
                        return NetInfoElement.getConnect_status();
                    }
                }
            }
        }
        else
        {
            logger.info("Network Info not Loaded or is Empty...!");
        }
        return 0;
    }*/
    /*public static int GetProcStatus(Channel chn)
    {
        if(NetworkManager.NetworkInfoElements != null) {
            for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                if(NetInfoElement != null) {
                    if (NetInfoElement.NetName == chn.getName()) {
                        return NetInfoElement.getProcessing_Status();
                    }
                }
            }
        }
        else
        {
            logger.info("Network Info not Loaded or is Empty...!");
        }
        return 0;
    }*/
    /*public static void SetProcEnabled(Channel chn)
    {
        if(NetworkManager.NetworkInfoElements != null) {
            for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                if(NetInfoElement != null) {
                    if (NetInfoElement.NetName == chn.getName()) {
                        NetInfoElement.setProcessing_Status(NetworkInfoStatus.PROCESSING_ENABLED);
                    }
                }
            }
        }
        else
        {
            logger.info("Network Info not Loaded or is Empty...!");
        }
    }*/
    /*public static void SetProcDisabled(Channel chn)
    {
        if(NetworkManager.NetworkInfoElements != null) {
            for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                if(NetInfoElement != null) {
                    if (NetInfoElement.NetName == chn.getName()) {
                        NetInfoElement.setProcessing_Status(NetworkInfoStatus.PROCESSING_DISABLED);
                    }
                }
            }
        }
        else
        {
            logger.info("Network Info not Loaded or is Empty...!");
        }
    }*/
    /*public static int SetCutOffDate(Channel chn, String CutOffDate)
    {
        Date CurrCutOffDate,NewCutOffDate = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(NewCutOffDate);
        int year = cal.get(Calendar.YEAR);
        CutOffDate = year + CutOffDate;
        logger.info("Final String CutOffDate [" + CutOffDate + "]");

        DateFormat df = new SimpleDateFormat("yyyyMMdd");  //Received Date Format //dd-MMM-yy");
        try {
            NewCutOffDate = df.parse(CutOffDate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        logger.info("New CutOff Date [" + NewCutOffDate + "]");

        try {
            if (NetworkManager.NetworkInfoElements != null) {
                for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                    if (NetInfoElement != null) {
                        if (NetInfoElement.NetName == chn.getName()) {
                            if (NetInfoElement.getProcessing_Status() == NetworkInfoStatus.PROCESSING_ENABLED) {
                                String DB_date = NetInfoElement.getCutOffDate();
                                CurrCutOffDate = new Date();

                                try {
                                    //if(DB_date != null) {  //Raza commenting for DB Column type issue
                                    //    df = new SimpleDateFormat("dd-MMM-yy"); //DB date Format //Raza commenting for DB Column type issue
                                    //    CurrCutOffDate = df.parse(DB_date); //Raza commenting for DB Column type issue
                                    //} //Raza commenting for DB Column type issue
                                    //else //Raza commenting for DB Column type issue
                                    //{ //Raza commenting for DB Column type issue
                                        df = new SimpleDateFormat("yyyyMMdd"); //DB date Format
                                        df.format(CurrCutOffDate);
                                    //} //Raza commenting for DB Column type issue
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                logger.info("CurrCutOffDate [" + CurrCutOffDate + "]");
                                logger.info("NewCutOffDate [" + NewCutOffDate + "]");
                                logger.info("CUTOFF DATE TO SET [" + CutOffDate + "]");
                                if (NewCutOffDate.compareTo(CurrCutOffDate) > 0) {
                                    NetInfoElement.setCutOffDate(CutOffDate);
                                        return 1;
                                } else {
                                    logger.info("Invalid CutOff Date [" + NewCutOffDate + "]");
                                    return 0;
                                }
                            } else {
                                logger.info("Cannot Process CutOff Message, Channel not in Processing Mode");
                                return -1;
                            }
                        }
                    }
                }
            } else {
                logger.info("Network Info not Loaded or is Empty...!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }*/
    /*public static void SetEchoCount(Channel chn)
    {
        if(NetworkManager.NetworkInfoElements != null) {
            for (NetworkInfo NetInfoElement : NetworkManager.NetworkInfoElements) {
                if(NetInfoElement != null) {
                    if (NetInfoElement.NetName == chn.getName()) {
                        NetInfoElement.setEchoCount();
                    }
                }
            }
        }
        else
        {
            logger.info("Network Info not Loaded or is Empty...!");
        }
    }*/
}
