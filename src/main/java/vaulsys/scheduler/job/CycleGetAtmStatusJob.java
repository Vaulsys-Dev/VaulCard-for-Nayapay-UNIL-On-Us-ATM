package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.Message;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandConfigurationIDLoadMsg;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifierConfigurationInfo;
import vaulsys.protocols.ui.MessageObject;
import vaulsys.scheduler.JobLog;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.TerminalStatus;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.constants.CustomizationDataLength;
import vaulsys.terminal.atm.constants.NDCUtil;
import vaulsys.terminal.atm.customizationdata.FITData;
import vaulsys.terminal.atm.customizationdata.ScreenData;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.terminal.atm.device.ATMDevice;
import vaulsys.terminal.atm.device.CardBin;
import vaulsys.terminal.atm.device.Cassette;
import vaulsys.terminal.atm.device.Printer;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionType;
import vaulsys.util.ConfigUtil;
import vaulsys.util.MyInteger;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import vaulsys.wfe.process.SwitchThreadPool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.quartz.JobExecutionContext;

import sun.security.jca.GetInstance.Instance;
//TASK Task074 : Get ATM Status
@Entity
@DiscriminatorValue(value = "Cycle_GetAtmsStatus")
public class CycleGetAtmStatusJob extends AbstractSwitchJob {
	private static final Logger logger = Logger.getLogger(CycleGetAtmStatusJob.class);
	private static final String ATM_CHANNEL = "channelNDCProcachInA";
	
	private static boolean isFree = true;
	
	@Transient
	private MessageObject messageObject = null;
	

    public void execute(JobExecutionContext switchJobContext, JobLog log) {
		try {
			logger.debug("SCHEDULER GetAtmsStatusJob: Start");
			
			if(!isJobFree()){
				logger.error("Another thread is running... Exiting from GetAtmsStatusJob");
				log.setStatus(SwitchJobStatus.FINISHED);
				log.setExceptionMessage("Job is not free");
				return;
			}
			
			ExecutorService executor = Executors.newFixedThreadPool(SwitchThreadPool.CORE_POOL_SIZE);//AldTODO Task074 
			//ExecutorService executor = Executors.newFixedThreadPool(1);//AldTODO Task074 for test 
			ArrayList<ATMTerminal> errorsAtms = new ArrayList<ATMTerminal>();

			try {
				//Special ATMs  //Agar File mojod nabashad ya list atm dar file mojod nabashad vaziate hameye atm haye fatal ra darkhast mikonad
				boolean useSpecialAtmList = false;
				List<String> specialAtmList = new ArrayList<String>();
				try {
					File file = new File(ConfigUtil.getProperty(ConfigUtil.SPECIAL_ATM_LIST_FILE_PATH)); 
					if (file.exists()) {
						BufferedReader br = new BufferedReader(new FileReader(file));
						String line ;
						while(br.ready()) {
							if ((line = br.readLine()).length() > 0){
								if (line.trim().length() > 0)
									specialAtmList.add(line);
							}
						}
						if (specialAtmList.size() > 0)
							useSpecialAtmList = true;
					}
				} catch(Exception e){
					useSpecialAtmList = false;
					logger.error(e.getMessage());
				}
				// end Special ATMs
				
				GeneralDao.Instance.beginTransaction();//AldTODO test Shavad 
				
//				//AldComment Task0074 : this if statement only for test
//				if (Util.getApplicationMainClass() != null && !Util.getApplicationMainClass().contains("VaulsysWCMS")) {
//					GlobalContext.getInstance().startup(); 
//				}
				ProcessContext.get().init();
				List<ATMTerminal> atms =  TerminalService.findAllTerminals(ATMTerminal.class, null);
			
				for(ATMTerminal atm : atms){
					boolean needGetDeviceStatus = false;
					for (ATMDevice atmDevice : atm.getDevices()){
						if (atmDevice instanceof Cassette || atmDevice instanceof Printer || atmDevice instanceof CardBin)
							if (ErrorSeverity.FATAL.equals(atmDevice.getErrorSeverity())){  //AldComment Aya Faghat FATAL 
								needGetDeviceStatus = true;
								break;
							}
						//Add in 93.02.28
						if (atmDevice instanceof Cassette) {
							if (ErrorSeverity.FATAL.equals(((Cassette) atmDevice).getTotalErrorSeverity())){
								needGetDeviceStatus = true;
								break;
							}
						}						
					}
					
					if (needGetDeviceStatus  //AldComment Faghat baraye ATM Haye nasbshode va faal va dar halate in service peygham befrestad
							&& ATMState.IN_SERIVCE.equals(atm.getState())
							&& TerminalStatus.INSTALL.equals(atm.getStatus()) 
							&& atm.isEnabled()) {
						
						if (!useSpecialAtmList)
							errorsAtms.add(atm);
						else if (specialAtmList.contains(String.valueOf(atm.getId()))){
							errorsAtms.add(atm);
						} else {
							logger.info(String.format("SKIP get status of ATM[%s]",atm.getId()));
						}
					}
					else {
						if (needGetDeviceStatus)
							logger.info(String.format("ATM[%s] State : %s, ATM[%s] Status : %s, ATM[%s] enabled : %s", 
									atm.getId(),atm.getState().toString(),atm.getId(),atm.getStatus(),atm.getId(),atm.isEnabled()));
					}
						 
				}
				
				GeneralDao.Instance.endTransaction();
				GeneralDao.Instance.close();
			}
			catch(Exception e){
				logger.error(e);
				log.setStatus(SwitchJobStatus.FAILED);
				log.setExceptionMessage(e.getMessage());

				GeneralDao.Instance.rollback();
				GeneralDao.Instance.close();
				throw e;
			}
			try{
				for (int i=0;i<errorsAtms.size();i++){
				
					MessageObject mo = new MessageObject();
					mo.setIfxType(IfxType.ATM_STATUS_MONITOR_REQUEST);
					HashMap<String, Serializable> params = new HashMap<String, Serializable>();
					params.put("atms", errorsAtms.get(i));
					System.out.println("********* " + errorsAtms.get(i));//AldTODO
					mo.setParameters(params);
					mo.setStartDateTime(DateTime.now());
					mo.setUsername(ProcessContext.get().getSwitchUser().toString()); //AldTODO 
	//				getMo().setResponseCode(mo.getResponseCode()); //AldTODO
					//readWriteMessageObject(messageObject);//comment for test thread
	
					executor.execute(new WorkerThread(mo) {
						
						@Override
						public void run() {
							readWriteMessageObject(messageObject);
						}
					});
				}	
				
				executor.shutdown();
				while (!executor.isTerminated()) {
				}
				logger.debug("Finished all GetAtmsStatusJob threads");
				
			} catch (RejectedExecutionException ex) {
	        	logger.error("Switch dropped the GetAtmsStatusJob !!!");
				logger.debug("SCHEDULER GetAtmsStatusJob: End");
	        	return;
			}			
			
			logger.debug("SCHEDULER GetAtmsStatusJob: End");
			if (log != null) 
				log.setStatus(SwitchJobStatus.FINISHED);
		} catch (Exception e) {
			logger.error("Exception in CycleGetAtmsStatusJob!!! " + e, e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		}finally{
			setJobFree();
			GeneralDao.Instance.close();
		}
	}

    public void interrupt() {
    }

    @Override
	public void submitJob() throws Exception {
        CycleGetAtmStatusJob newJob = new CycleGetAtmStatusJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.REPEAT);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("CycleGetAtmStatusJob");
        JobServiceQuartz.submit(newJob);
	}
    
    public void updateExecutionInfo() {
    }
    
	public /*synchronized*/ void readWriteMessageObject(MessageObject messageObject) {
		
		try
		{
			IfxType ifxType = messageObject.getIfxType();
			Message receivedMessage = new Message(vaulsys.message.MessageType.INCOMING);
			GeneralDao.Instance.beginTransaction();//for Thread  //AldTODO Task074 : Thread
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
			ATMTerminal atm =  (ATMTerminal) messageObject.getParameter("atms");
			/*for (ATMTerminal atm : codes) */{
				IoSession session = networkManager.getTerminalOpenConnection(atm.getIP());
				processContext.setSession(session);
				List<NDCNetworkToTerminalMsg> ndcMsgs = generateATMMsg(ifxType, atm, messageObject.getParameters());
				if (ndcMsgs == null || ndcMsgs.size() == 0)
					return; //continue -> return
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
						if (session != null) {
							logger.debug("SENT to " + channel.getName() + ":\n" +outMsg.getXML());//AldTODO
							session.write(outMsg.getBinaryData());
							logger.debug("*********** After Send Message ******&&&");
						}
						else {
	                        logger.warn("Writing to Session Failed - ResponseOnSameSocket - session is closed");
						}
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
					synchronized (atm) {
						TerminalService.setLastTransaction(atm, outMsg);
						GeneralDao.Instance.saveOrUpdate(atm);//AldTODO Test
					}
					GeneralDao.Instance.saveOrUpdate(outMsg);
					GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
					GeneralDao.Instance.saveOrUpdate(outIfx);
					LifeCycle lifeCycle = new LifeCycle();
					lifeCycle.setIsComplete(true);
					GeneralDao.Instance.saveOrUpdate(lifeCycle);
					transaction.setLifeCycle(lifeCycle);
				}
			}
		    GeneralDao.Instance.endTransaction(); //AldTODO Task074 : for Thread
		}
		catch(Exception e){
			logger.error(e);
			if (GeneralDao.Instance.getCurrentSession() != null) {
				try {
					GeneralDao.Instance.rollback();
				}
				catch(Exception ex)
				{
					
				}
			}
			GeneralDao.Instance.close();			
		}
	}

    
	private List<NDCNetworkToTerminalMsg> generateATMMsg(IfxType type, ATMTerminal atm, Map<String, Serializable> params) {
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
		//TASK Task074
		} else if (IfxType.ATM_STATUS_MONITOR_REQUEST.equals(type)){  
			return Arrays.asList(ATMTerminalService.generateSendConfigInfoMessage(atm.getCode(),NDCTerminalCommandModifierConfigurationInfo.SEND_FITNESS_DATA_ONLY));
//		} else if (IfxType.CONFIG_INFO_REQUEST.equals(type)){  
//			return Arrays.asList(ATMTerminalService.generateSendConfigInfoMessage(atm.getCode(),NDCTerminalCommandModifierConfigurationInfo.SEND_FITNESS_DATA_ONLY));
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
	
	public synchronized boolean isJobFree(){
		if(isFree == true){
			isFree = false;
			return true;
		}
		return false;
	}
	
	public void setJobFree(){
		isFree = true;
	}	
    
    
    //AldTODO Task074 : remove this
    public static void main(String[] args)
    {
    	Class<?> cls = ATMTerminal.class;
    	if (cls == Terminal.class){
    		System.out.println("yes");
    	} else {
    		System.out.println("no");
    	}
    	
      
    	CycleGetAtmStatusJob caj = new CycleGetAtmStatusJob();
    	caj.execute(null,null);

    }
    
}

abstract class WorkerThread implements Runnable {

	protected MessageObject messageObject;

	public WorkerThread(MessageObject messageObject) {
		this.messageObject = messageObject;
	}

}