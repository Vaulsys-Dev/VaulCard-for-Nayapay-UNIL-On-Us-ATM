package vaulsys.network.remote;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.authorization.policy.Bank;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.report.ShetabDocumentService;
import vaulsys.discount.DiscountProfile;
import vaulsys.entity.impl.Institution;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.message.Message;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.network.channel.base.OutputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ProtocolConfig;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandConfigurationIDLoadMsg;
import vaulsys.protocols.ui.MessageObject;
import vaulsys.security.base.SecurityProfile;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.constants.CustomizationDataLength;
import vaulsys.terminal.atm.constants.NDCUtil;
import vaulsys.terminal.atm.customizationdata.FITData;
import vaulsys.terminal.atm.customizationdata.ScreenData;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionType;
import vaulsys.util.MyInteger;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

public class UtilityRemoteMessage {

    private final static Logger logger = Logger.getLogger(RemoteMessageManager.class);
	private static final String ATM_CHANNEL = "channelNDCProcachInA";
    private static UtilityRemoteMessage instance = null;

    private UtilityRemoteMessage() {

    }

    public static UtilityRemoteMessage getInstance() {
        if (instance == null) {
            instance = new UtilityRemoteMessage();
        }
        return instance;
    }

    public void startChannel(String channelName) {
        Channel channel = GlobalContext.getInstance().getChannel(channelName);
        if (channel == null)
            throw new RemoteMessageExcaption("Channel not found: " + channelName);
        if (channel instanceof InputChannel)
            NetworkManager.getInstance().startInputChannel((InputChannel) channel);
        else
            NetworkManager.getInstance().startKeepAliveOutputChannel((OutputChannel) channel);
    }

    public void stopChannel(String channelName) {
        Channel channel = GlobalContext.getInstance().getChannel(channelName);
        if (channel == null)
            throw new RemoteMessageExcaption("Channel not found: " + channelName);
        if (channel instanceof InputChannel)
            NetworkManager.getInstance().stopInputChannel((InputChannel) channel);
        else
            NetworkManager.getInstance().stopKeepAliveOutputChannel((OutputChannel) channel);
    }

    public ArrayList<Channel> getChannelList() throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        ArrayList<Channel> list = new ArrayList<Channel>();
        for (Channel allChannel : GlobalContext.getInstance().getAllChannels().values()) {
            for (Channel channel : GlobalContext.getInstance().getAllChannels().values()) {
                //channel.setInput(channel instanceof InputChannel); //Raza commenting
                if (channel.getOriginatorChannelId() != null)
                    channel.setOriginatorChannelId(channel.getOriginatorChannelId());
                list.add(channel);
            }
        }
        return list;
    }

    public void updateChannel(Channel info) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Channel channel = GlobalContext.getInstance().getChannel(info.getName());
        if (channel == null)
            throw new RemoteMessageExcaption("Channel not found: " + info.getName());
        if (channel instanceof OutputChannel)
            channel.setIp(info.getIp());
        channel.setPort(info.getPort());
        channel.setKeepAlive(info.getKeepAlive());
        channel.setMacEnable(info.getMacEnable());
        channel.setClearingActionJobsBean(info.getClearingActionJobsBean());
        channel.setClearingActionMapperBean(info.getClearingActionMapperBean());
        channel.setOriginatorChannelId(info.getOriginatorChannelId());
        channel.setEndPointType(info.getEndPointType());
        channel.setInstitutionId(info.getInstitutionId());
        channel.setEncodingConverter(info.getEncodingConverter());
        channel.setPinTransEnable(info.getPinTransEnable());
        channel.setIoFilterClassName(info.getIoFilterClassName());
        channel.setProtocolName(info.getProtocolName());
        channel.setName(info.getName());
    }

    public void saveConfiguration(Channel info) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Channel channel;
        if (info.getChannelType().toLowerCase().equals(""))
            channel = new InputChannel();
        else
            channel = new OutputChannel();
        if (channel instanceof OutputChannel)
            channel.setIp(info.getIp());
        channel.setPort(info.getPort());
        channel.setKeepAlive(info.getKeepAlive());
        channel.setMacEnable(info.getMacEnable());
        channel.setClearingActionJobsBean(info.getClearingActionJobsBean());
        channel.setClearingActionMapperBean(info.getClearingActionMapperBean());
        channel.setOriginatorChannelId(info.getOriginatorChannelId());
        channel.setEndPointType(info.getEndPointType());
        channel.setInstitutionId(info.getInstitutionId());
        channel.setEncodingConverter(info.getEncodingConverter());
        channel.setPinTransEnable(info.getPinTransEnable());
        channel.setIoFilterClassName(info.getIoFilterClassName());
        channel.setProtocolName(info.getProtocolName());
        channel.setName(info.getName());
        Map<String, Channel> allChannel = GlobalContext.getInstance().getAllChannels();
        allChannel.put(channel.getName(), channel);
        //xStreamToXml(channels);


    }

    public String issueShetabDocument(byte[] docBody) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(docBody);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader brShetabReport = new BufferedReader(streamReader);
            GeneralDao.Instance.beginTransaction();
            String res = ShetabDocumentService.issueShetabDocument(brShetabReport);
            GeneralDao.Instance.endTransaction();
            return res;
        } catch (Exception e) {
            GeneralDao.Instance.rollback();
            logger.error("IssueShetabDocument: ", e);
            throw new RemoteMessageExcaption(e.getMessage());
        }
    }

    public void updateCache(String name) {
        logger.info("Updating cache for " + name);
        try {
            GeneralDao.Instance.beginTransaction();
            GlobalContext gc = GlobalContext.getInstance();
            if (FeeProfile.class.getSimpleName().equals(name))
                gc.setAllFeeProfiles();
            else if (SecurityProfile.class.getSimpleName().equals(name))
                gc.setAllSecurityFunctions();
            else if (Institution.class.getSimpleName().equals(name))
                gc.setAllInstitutions();
            else if (Bank.class.getSimpleName().equals(name))
                gc.setAllBanks();
            else if (AuthorizationProfile.class.getSimpleName().equals(name))
                gc.setAllAuthorizationProfiles();
            else if (ATMConfiguration.class.getSimpleName().equals(name))
                gc.setAllATMConfigurations();
            else if (ProtocolConfig.class.getSimpleName().equals(name))
                gc.setAllProtocolConfig();
            else if (DiscountProfile.class.getSimpleName().equals(name))
                gc.setAllDiscountProfiles();
            else if (LotteryAssignmentPolicy.class.getSimpleName().equals(name))
                gc.setAllLotteryAssignmentPolicy();
            else if(ClearingProfile.class.getSimpleName().equals(name))
            	gc.setAllClearingProfile();

            GeneralDao.Instance.endTransaction();
        } catch (Exception e) {
            GeneralDao.Instance.rollback();
            logger.error("updateCache: " + e, e);
            throw new RemoteMessageExcaption(e.getMessage());
        }

    }

    public void readWriteMessageObject(MessageObject messageObject) {
        try {
            IfxType ifxType = messageObject.getIfxType();
            Message receivedMessage = new Message(vaulsys.message.MessageType.INCOMING);
            GeneralDao.Instance.beginTransaction();
            Transaction transaction = new Transaction(TransactionType.EXTERNAL);
            transaction.setInputMessage(receivedMessage);
//		transaction.setStatus(TransactionStatus.RECEIVED);
            transaction.setFirstTransaction(transaction);
            receivedMessage.setTransaction(transaction);
            GeneralDao.Instance.saveOrUpdate(transaction);

            ProcessContext processContext = new ProcessContext();

            processContext.setTransaction(transaction);
            //MessageManager.getInstance().putRequest(receivedMessage, session); //ro nazashtam!!!
            Channel channel = GlobalContext.getInstance().getChannel(ATM_CHANNEL);
            receivedMessage.setChannel(channel);
            GeneralDao.Instance.saveOrUpdate(receivedMessage);
            NetworkManager networkManager = NetworkManager.getInstance();
            List<Long> codes = (List<Long>) messageObject.getParameter("atms");
            for (Long code : codes) {
                ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, code);
                IoSession session = networkManager.getTerminalOpenConnection(atm.getIP());
                processContext.setSession(session);
                List<NDCNetworkToTerminalMsg> ndcMsgs = generateATMMsg(ifxType, atm, messageObject.getParameters());
                if (ndcMsgs == null || ndcMsgs.size() == 0)
                    continue;
                for (NDCNetworkToTerminalMsg ndcMsg : ndcMsgs) {
                    Message outMsg = new Message(vaulsys.message.MessageType.OUTGOING);
                    outMsg.setProtocolMessage(ndcMsg);
                    outMsg.setTransaction(transaction);
                    outMsg.setChannel(channel);
                    outMsg.setEndPointTerminal(atm);
                    outMsg.setRequest(true);
                    outMsg.setNeedResponse(false);
                    outMsg.setNeedToBeInstantlyReversed(false);
                    outMsg.setNeedToBeSent(true); //FIXME: it should be false, must be correct
                    outMsg.setStartDateTime(messageObject.getStartDateTime());
                    outMsg.setXML(ndcMsg.toString());
                    try {
                        byte[] binary = channel.getProtocol().getMapper().toBinary(ndcMsg);
                        outMsg.setBinaryData(binary);
                        if (NDCUtil.isNeedSetMac(ndcMsg)) {
                            ProtocolSecurityFunctions securityFunctions = channel.getProtocol().getSecurityFunctions();
                            securityFunctions.setMac(processContext, atm, atm.getOwnOrParentSecurityProfileId(), atm.getKeySet(), outMsg, channel.getMacEnable());
                        }
                        session.write(outMsg.getBinaryData());
                    } catch (NotProducedProtocolToBinaryException e) {
                        logger.error("Exception in ATM Message Management toBinary, ", e);
                    } catch (Exception e) {
                        logger.error("Cannot set MAC, ", e);
                    }

                    transaction.addOutputMessage(outMsg);
                    Ifx outIfx = new Ifx();
                    outIfx.setReceivedDt(messageObject.getStartDateTime());
                    outIfx.setIfxDirection(IfxDirection.OUTGOING);
                    outIfx.setIfxType(ifxType);
                    outIfx.setTerminalId(atm.getCode().toString());
                    outIfx.setTerminalType(TerminalType.ATM);
                    outIfx.setRequest(true);
                    outIfx.setTransaction(transaction);
                    outIfx.setRsCode(messageObject.getResponseCode());
                    outMsg.setIfx(outIfx);
                    GeneralDao.Instance.saveOrUpdate(outMsg);
                    GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
                    GeneralDao.Instance.saveOrUpdate(outIfx);
                    LifeCycle lifeCycle = new LifeCycle();
                    lifeCycle.setIsComplete(true);
                    GeneralDao.Instance.saveOrUpdate(lifeCycle);
                    transaction.setLifeCycle(lifeCycle);
                }
            }
            GeneralDao.Instance.endTransaction();
        } catch (Exception e) {
            GeneralDao.Instance.rollback();
            logger.error("readWriteMessageObject: " + e, e);
            throw new RemoteMessageExcaption(e.getMessage());
        }

    }

    public List<NDCNetworkToTerminalMsg> generateATMMsg(IfxType type, ATMTerminal atm, Map<String, Serializable> params) {
        Integer configId = null;
        if (params.containsKey("config_id"))
            configId = (Integer) params.get("config_id");
        int lastIndex = 0;
        if (params.containsKey("last_index"))
            lastIndex = (Integer) params.get("last_index");
        if (IfxType.ATM_GO_OUT_OF_SERVICE.equals(type)) {
            atm.setATMState(ATMState.OUT_OF_SERVICE);
            atm.setCurrentAbstractStateClass((AbstractState) null);
            GeneralDao.Instance.saveOrUpdate(atm);
            return Arrays.asList(ATMTerminalService.generateGoOutOfServiceMessage(atm.getCode()));
        } else if (IfxType.ATM_GO_IN_SERVICE.equals(type)) {
            atm.setATMState(ATMState.IN_SERIVCE);
            atm.setCurrentAbstractStateClass((AbstractState) null);
            GeneralDao.Instance.saveOrUpdate(atm);
            return Arrays.asList(ATMTerminalService.generateGoInServiceMessage(atm.getCode()));

        } else if (IfxType.CONFIG_INFO_REQUEST.equals(type)) {
            atm.setATMState(ATMState.CONFIG_INFO_REQUEST);
            atm.setCurrentAbstractStateClass((AbstractState) null);
            GeneralDao.Instance.saveOrUpdate(atm);
            return Arrays.asList(ATMTerminalService.generateSendConfigInfoMessage(atm.getCode(), null));

        } else if (IfxType.ATM_DATE_TIME_LOAD.equals(type)) {
            return Arrays.asList(ATMTerminalService.generateDateTimeLoadMessage(atm.getCode()));
        } else if (IfxType.MASTER_KEY_CHANGE_RQ.equals(type)) {
            return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_newMasterByCurMaster(atm.getCode()));
        } else if (IfxType.MAC_KEY_CHANGE_RQ.equals(type)) {
            return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_MACByMaster(atm.getCode()));
        } else if (IfxType.PIN_KEY_CHANGE_RQ.equals(type)) {
            return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_PINByMaster(atm.getCode()));
        } else if (IfxType.ATM_STATE_TABLE_LOAD.equals(type)) {
            if (params.containsKey("config_id"))
                configId = (Integer) params.get("config_id");
            List<StateData> states = ATMTerminalService.getCustomizationDataAfter(atm, StateData.class, configId);
            List<NDCNetworkToTerminalMsg> sendingNDCMsgs = new ArrayList<NDCNetworkToTerminalMsg>();
            while (lastIndex < states.size()) {
                int length = Math.min(CustomizationDataLength.MAX_STATES_IN_MSG, states.size() - lastIndex);
                NDCNetworkToTerminalMsg sendStateMessage = ATMTerminalService.
                        generateStateTableLoadMessage(atm.getCode(), states, lastIndex, length);
                if (sendStateMessage != null) {
                    lastIndex += length;
                    atm.setLastSentStateIndex(lastIndex);
                    sendingNDCMsgs.add(sendStateMessage);
                } else
                    break;
            }
            if (lastIndex == states.size())
                atm.setLastSentStateIndex(0);
            GeneralDao.Instance.saveOrUpdate(atm);
            logger.debug("UI Message Handler -> ATM_STATE_TABLE_LOAD -> No of msgs: " + sendingNDCMsgs.size());
            return sendingNDCMsgs;
        } else if (IfxType.ATM_SCREEN_TABLE_LOAD.equals(type)) {
            List<ScreenData> screens = ATMTerminalService.getCustomizationDataAfter(atm, ScreenData.class, configId);
            List<NDCNetworkToTerminalMsg> sendingNDCMsgs = new ArrayList<NDCNetworkToTerminalMsg>();
            while (lastIndex < screens.size()) {
                MyInteger length = new MyInteger(0);
                NDCNetworkToTerminalMsg sendStateMessage = ATMTerminalService.
                        generateScreenTableLoadMessage(atm.getCode(), screens, lastIndex, length);
                if (sendStateMessage != null) {
                    lastIndex += length.value;
                    atm.setLastSentStateIndex(lastIndex);
                    sendingNDCMsgs.add(sendStateMessage);
                } else
                    break;
            }
            if (lastIndex == screens.size())
                atm.setLastSentScreenIndex(0);
            GeneralDao.Instance.saveOrUpdate(atm);
            logger.debug("UI Message Handler -> ATM_SCREEN_TABLE_LOAD -> No of msgs: " + sendingNDCMsgs.size());
            return sendingNDCMsgs;
        } else if (IfxType.ATM_GET_ALL_KVV.equals(type)) {
            return Arrays.asList(ATMTerminalService.generateExtEncKeyChngMsg_acquireAllKVV(atm.getCode()));
        } else if (IfxType.ATM_CONFIG_ID_LOAD.equals(type)) {
            NDCWriteCommandConfigurationIDLoadMsg ndcMsg;
            ndcMsg = new NDCWriteCommandConfigurationIDLoadMsg();
            ndcMsg.logicalUnitNumber = atm.getCode();
            return Arrays.asList((NDCNetworkToTerminalMsg) ATMTerminalService.generateConfigIdLoadMessage(ndcMsg, configId));/*ATMTerminalService.getMaxCustomizationDataConfigId(atm)*/
        } else if (IfxType.ATM_SUPPLY_COUNTER_REQUEST.equals(type)) {
            return Arrays.asList(ATMTerminalService.generateSupplyCountersMessage(atm.getCode()));
        } else if (IfxType.ATM_ENHANCED_PARAMETER_TABLE_LOAD.equals(type)) {
            return Arrays.asList(ATMTerminalService.generateEnhancedParameterTableLoadMessage(atm.getCode()));
        } else if (IfxType.ATM_FIT_TABLE_LOAD.equals(type)) {
            int length;
            NDCWriteCommandConfigurationIDLoadMsg ndcMsg;
            ndcMsg = new NDCWriteCommandConfigurationIDLoadMsg();
            ndcMsg.logicalUnitNumber = atm.getCode();
            //int lastIndex = atm.getLastSentFitIndex();
            List<FITData> fits = ATMTerminalService.getCustomizationDataAfter(atm, FITData.class, configId);
            if (fits == null || fits.size() == 0) {
                return null;
            }
            if (fits.size() > lastIndex) {
                length = Math.min(CustomizationDataLength.MAX_FITS_IN_MSG, fits.size() - lastIndex);
                return Arrays.asList((NDCNetworkToTerminalMsg) ATMTerminalService.generateFITTableLoadMessage(ndcMsg, fits, lastIndex, length));
            } else
                return null;
        }

        throw new RuntimeException("IfxType not implemented!");
    }

}

