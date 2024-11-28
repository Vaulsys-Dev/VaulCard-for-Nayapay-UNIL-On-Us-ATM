package vaulsys.network.channel.base;

import vaulsys.config.ConfigurationManager;
import vaulsys.config.exception.BadConfigException;
import vaulsys.netmgmt.extended.ConnectionManager;
import vaulsys.netmgmt.extended.NetworkInfo;
import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.network.filters.Mina2CMSIoFilter;
import vaulsys.network.filters.Mina2Meganac2HDLCIoFilter;
import vaulsys.network.filters.Mina2Meganac2IoFilterNew;
import vaulsys.network.filters.Mina2Meganac4IoFilterNew;
import vaulsys.network.filters.Mina2MeganacHDLCIoFilter;
import vaulsys.network.filters.Mina2NCC2HDLCIoFilter;
import vaulsys.network.filters.Mina2NCC2IoFilterNew;
import vaulsys.network.filters.Mina2NCC4IoFilterNew;
import vaulsys.network.filters.Mina2NCCIoFilter;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.util.SwitchContext;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class ChannelManager extends SwitchContext {

	Logger logger = Logger.getLogger(this.getClass());

	private static ChannelManager manager;

	public static ChannelManager getInstance() {
		if (manager == null) {
			manager = new ChannelManager();
		}
		return manager;
	}

	private ChannelManager() {

	}

	public Channel getChannel(String name) throws Exception {
//		return GlobalContext.getInstance().getChannel(name);
		return ProcessContext.get().getChannel(name);
	}

	// public Channel getChannelFromDB(String name) throws Exception {
	// String queryString = "from Channel c where c.name= :name";
	// Map<String, Object> params = new HashMap<String, Object>();
	// params.put("name", name);
	// return (Channel) getGeneralDao().findObject(queryString, params, true);
	// }
	//
	public List<Channel> loadInDB() {
		logger.debug("Reading config file of channels ...");

		Configuration config = ConfigurationManager.getInstance().getConfiguration("channel");

		String[] variables = config.getStringArray("ip/@name");
		Map<String, String> myLocalIPAddress = null;
		if (variables != null && variables.length > 0) {
			myLocalIPAddress = new HashMap<String, String>();
			for (String var : variables) {
				myLocalIPAddress.put(var, config.getString("ip[@name='" + var + "']/@value"));
			}
		}

		String[] names = config.getStringArray("channel/@name");
		List<OutputChannel> OutputChannels = new ArrayList<OutputChannel>();
		List<InputChannel> InputChannels = new ArrayList<InputChannel>();
		//NetworkManager.NetworkInfoElements = new NetworkInfo[names.length];
		ConnectionManager.ConnectManager = new Thread[names.length];
		int index_netinfo = 0;

		try {
			for (String name : names) {
				String type = config.getString("channel[@name='" + name + "']/@type");
				String ip = config.getString("channel[@name='" + name + "']/address/ip");
				if (!isValidIPAddress(ip)) {
					// it should be one of our local ip
					// contact!
					if (myLocalIPAddress != null)
						ip = myLocalIPAddress.get(ip);
					else
						ip = "";
				}
				int port = config.getInt("channel[@name='" + name + "']/address/port");
				String protocolName = checkNull(config.getString("channel[@name='" + name + "']/protocol"),
						"protocolName can't be null");
				String protocolGenericName = checkNull(config.getString("channel[@name='" + name + "']/protocolName"),
						"protocolGenericName can't be null");
				String protocolClass = checkNull(config.getString("channel[@name='" + name + "']/protocolClass"),
						"protocolClass can't be null");
				String methodString = checkNull(config.getString("channel[@name='" + name + "']/method"),
						"methodString can't be null");
				String institutionId = config.getString("channel[@name='" + name + "']/institutionId");
				String endPointType = checkNull(config.getString("channel[@name='" + name + "']/endPoint"),
						"endPointType can't be null");
				String ioFilter = config.getString("channel[@name='" + name + "']/ioFilter");
				String ioFilterParameter = null;
				if (ioFilter.contains("[")) {
					String[] split = ioFilter.split("[\\[]");
					ioFilter = split[0];
					ioFilterParameter = split[1].substring(0, split[1].indexOf("]"));
				}

				String encodingConverter = config.getString("channel[@name='" + name + "']/encodingConvertor");
				String clearingMapper = config.getString("channel[@name='" + name + "']/clearingActionMapper");
				String clearingJobs = config.getString("channel[@name='" + name + "']/clearingActionJobs");

				EndPointType pointType = EndPointType.valueOf(endPointType);
				boolean enableMac = Boolean.valueOf(checkNull(config.getString("channel[@name='" + name
						+ "']/securityFeature/MAC"), "Security feature can't be null: MAC"));
				boolean enablePinTrans = Boolean.valueOf(checkNull(config.getString("channel[@name='" + name
						+ "']/securityFeature/PIN"), "Security feature can't be null: PIN"));
				boolean enable_ssl = false;
				String enableSslStr = config.getString("channel[@name='" + name + "']/securityFeature/SSL");
				if(enableSslStr != null)
					enable_ssl = Boolean.valueOf(enableSslStr);
				Integer keepAlive = Integer.valueOf(checkNull(config.getString("channel[@name='" + name
						+ "']/keepAlive"), "keepAlive can't be null"));
				Integer sessionNumber = Util.integerValueOf(checkNull(config.getString("channel[@name='" + name
						+ "']/sessionNumber"), "sessionNumber can't be null"));
				Integer srcTPDULen = Util.integerValueOf(checkNull(config.getString("channel[@name='" + name
						+ "']/srcTPDULen"), "srcTPDULen can't be null"));
				// Asim Shahzad, Date : 10th Dec 2016, Desc : For VISA SMS header length handling
				Integer headerLen = Util.integerValueOf(checkNull(config.getString("channel[@name='" + name
						+ "']/headerLen"), "headerLen can't be null"));
				Integer timeout = Util.integerValueOf(checkNull(config.getString("channel[@name='" + name
						+ "']/timeout"), "timeout can't be null"));
						
				String id = checkNull(config.getString("channel[@name='" + name + "']/id"), "id can't be null");
				
				String masterDep = config.getString("channel[@name='" + name + "']/clearingMasterDepended");
				Boolean masterDependant = false;
				if (Util.hasText(masterDep))
					masterDependant = Boolean.valueOf(masterDep);
				
				String isSec = config.getString("channel[@name='" + name + "']/isSecure");
				Boolean isSecure = false;
				if (Util.hasText(isSec))
					isSecure = Boolean.valueOf(isSec);
				
				String channelType = config.getString("channel[@name='" + name + "']/channelType");
				
				if (sessionNumber == null)
					sessionNumber = 1;

				Channel dc = null;
				if (type.equals("in")) {
					CommunicationMethod method;
					if (methodString.equals("AnotherSocket")) {
						method = CommunicationMethod.ANOTHER_SOCKET;
					} else {
						method = CommunicationMethod.SAME_SOCKET;
					}
					String orig = config.getString("channel[@name='" + name + "']/originatorChannelId");
//					Channel origChannel = ("".equals(orig)) ? null : new OutputChannel(null, orig, "", null, null, null, null, keepAlive, true, true, encodingConvertor, sessionNumber);
					dc = new InputChannel(ip, port, name, protocolName, protocolGenericName, protocolClass, ioFilter,
							method, institutionId, orig, enableMac, enablePinTrans, keepAlive, encodingConverter,
							sessionNumber, masterDependant, srcTPDULen, isSecure, headerLen, id, channelType, timeout);
					dc.setEndPointType(pointType);
					dc.setClearingActionJobsBean(clearingJobs);
					dc.setClearingActionMapperBean(clearingMapper);
					dc.setSslEnable(enable_ssl);
					//                    
//					if (dc.getIoFilterObject() instanceof CMSIoFilter) {
//					((CMSIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter);
//				}else if(dc.getIoFilterObject() instanceof NCCIoFilter){
//					((NCCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
//				}else if(dc.getIoFilterObject() instanceof NCC2HDLCIoFilter){
//					((NCC2HDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
//				}else if(dc.getIoFilterObject() instanceof MeganacHDLCIoFilter){
//					((MeganacHDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
//				}else if(dc.getIoFilterObject() instanceof Meganac2HDLCIoFilter){
//					((Meganac2HDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter);
//				}
					InputChannels.add((InputChannel) dc);

					/*logger.info("Adding Channels in Network Info..."); //Raza MasterCard
					NetworkInfo networkInfo;
					networkInfo = new NetworkInfo(dc.getName(),dc.getIp(),dc.getPort(),type);
					NetworkManager.NetworkInfoElements[index_netinfo] = networkInfo;
					index_netinfo++;
					NetworkInfo.addNetworkInfoinDB(networkInfo); //add Network in DB.*/
				} else if (type.equals("out")) {
					CommunicationMethod method;
					if (methodString.equals("AnotherSocket")) {
						method = CommunicationMethod.ANOTHER_SOCKET;
					} else {
						method = CommunicationMethod.SAME_SOCKET;
					}

					dc = new OutputChannel(ip, port, name, protocolName, protocolGenericName, protocolClass, ioFilter,
							method, institutionId, keepAlive, enableMac, enablePinTrans, encodingConverter, sessionNumber, masterDependant, srcTPDULen, isSecure, headerLen, id, channelType, timeout);
					dc.setEndPointType(pointType);
					dc.setClearingActionJobsBean(clearingJobs);
					dc.setClearingActionMapperBean(clearingMapper);
					dc.setSslEnable(enable_ssl);
//					if (dc.getIoFilterObject() instanceof CMSIoFilter) {
//						((CMSIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter);
//					}else if(dc.getIoFilterObject() instanceof NCCIoFilter){
//						((NCCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
//					}else if(dc.getIoFilterObject() instanceof NCC2HDLCIoFilter){
//						((NCC2HDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
//					}else if(dc.getIoFilterObject() instanceof MeganacHDLCIoFilter){
//						((MeganacHDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
//					}else if(dc.getIoFilterObject() instanceof Meganac2HDLCIoFilter){
//						((Meganac2HDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter);
//					}
					OutputChannels.add((OutputChannel) dc);
					/*logger.info("Adding Channels in Network Info..."); //Raza MasterCard
					NetworkInfo networkInfo;
					networkInfo = new NetworkInfo(dc.getName(),dc.getIp(),dc.getPort(),type);
					NetworkManager.NetworkInfoElements[index_netinfo] = networkInfo;
					index_netinfo++;
					NetworkInfo.addNetworkInfoinDB(networkInfo); //add Network in DB.*/
				}
				if (dc.getIoFilterObject() instanceof Mina2CMSIoFilter) {
					((Mina2CMSIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter);
				}else if(dc.getIoFilterObject() instanceof Mina2NCCIoFilter){
					((Mina2NCCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
				}else if(dc.getIoFilterObject() instanceof Mina2NCC2HDLCIoFilter){
					((Mina2NCC2HDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
				}else if(dc.getIoFilterObject() instanceof Mina2MeganacHDLCIoFilter){
					((Mina2MeganacHDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter); 
				}else if(dc.getIoFilterObject() instanceof Mina2Meganac2HDLCIoFilter){
					((Mina2Meganac2HDLCIoFilter) dc.getIoFilterObject()).setParameter(ioFilterParameter);
				}else if(dc.getIoFilterObject() instanceof Mina2NCC2IoFilterNew){
					((Mina2NCC2IoFilterNew) dc.getIoFilterObject()).setParameter(ioFilterParameter);
				}else if(dc.getIoFilterObject() instanceof Mina2Meganac2IoFilterNew){
					((Mina2Meganac2IoFilterNew) dc.getIoFilterObject()).setParameter(ioFilterParameter);
				}else if(dc.getIoFilterObject() instanceof Mina2Meganac4IoFilterNew){
					((Mina2Meganac4IoFilterNew) dc.getIoFilterObject()).setParameter(ioFilterParameter);
				}else if(dc.getIoFilterObject() instanceof Mina2NCC4IoFilterNew){
					((Mina2NCC4IoFilterNew) dc.getIoFilterObject()).setParameter(ioFilterParameter);
				}
			}

			logger.info("Channels Added Successfully...!");
			/*for (InputChannel c : InputChannels) {
				String origName = (c.getOriginatorChannel() != null) ? c.getOriginatorChannel().getName() : "";
				if (!"".equals(origName)) {
					int counter = 0;
					while (counter < OutputChannels.size() && !OutputChannels.get(counter).getName().equals(origName)) {
						counter++;
					}

					if (counter != OutputChannels.size())
						c.setOriginatorChannelName(OutputChannels.get(counter).getName());
					else
						c.setOriginatorChannelName(c.getName());
				} else
					c.setOriginatorChannelName(c.getName());
			}*/
		} catch (Exception e) {
			logger.error("Encounter with an exception.( " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")",
					e);
			// e.printStackTrace();
			return null;
		}
		List<Channel> result = new ArrayList<Channel>();
		result.addAll(OutputChannels);
		result.addAll(InputChannels);
		return result;
	}

	public List<Channel> loadFromDB() {

		List<InputChannel> inputChannelFromDB;
		List<OutputChannel> outputChannelFromDB;
		Map<String, Object> dbParam;
		List<OutputChannel> OutputChannels;
		List<InputChannel> InputChannels;
		List<String> channelTypes, channelIds;
		String dbQuery;

		logger.debug("Reading from DB ...");

		try {
			OutputChannels = new ArrayList<OutputChannel>();
			InputChannels = new ArrayList<InputChannel>();
			channelTypes = new ArrayList<String>();
			channelIds = new ArrayList<String>();

			//Raza Get count of channels for connection management start
			String query = "select count(*) from network_info";
			List<BigDecimal> count;
			count = GeneralDao.Instance.executeSqlQuery(query);
			int size = count.get(0).intValue();
			//logger.info("Network_Info count [" + size + "]"); //Raza TEMP
			ConnectionManager.ConnectManager = new Thread[size];
			dbParam = new HashMap<String, Object>();
			channelTypes.add("Channel");
			channelTypes.add("Webserver"); //Raza adding for WebService Channel
			//channelTypes.add("Bank");
			dbParam.put("channelType", channelTypes);

//			channelIds.add(ChannelCodes.WALLETMEZNONUSATM);
//			channelIds.add(ChannelCodes.MEZNONUSATM);

			// Added By Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
			channelIds.add(ChannelCodes.WALLETUNILONUSATM);
			channelIds.add(ChannelCodes.UNILONUSATM);
			//////////////////////////////////////////////////////////////////////////


			channelIds.add(ChannelCodes.SWITCH);
			dbParam.put("channelId", channelIds);

			dbParam.put("netType", "in");
			dbQuery = "from " + InputChannel.class.getName() + " c " +
					"where " +
					"networkType = :netType " +
					"and " +
					"channelType in (:channelType) " +
					"and channelId in (:channelId) ";
			inputChannelFromDB = GeneralDao.Instance.find(dbQuery, dbParam);

			for (Channel channel : inputChannelFromDB) {
				channel = setChannelProperties(channel);
				channel.setConnectionStatus(NetworkInfoStatus.SOCKET_RESET); //Raza Reset initially
				channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_RESET); //Raza Reset initially
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
				InputChannels.add((InputChannel) channel);
			}

			dbParam = new HashMap<String, Object>();
			dbParam.put("channelType", channelTypes);
			dbParam.put("netType", "out");
			dbParam.put("channelId", channelIds);
			dbQuery = "from " + OutputChannel.class.getName() + " c " +
					"where " +
					"networkType = :netType " +
					"and " +
					"channelType in (:channelType) " +
					"and channelId in (:channelId) ";
			outputChannelFromDB = GeneralDao.Instance.find(dbQuery, dbParam);

			//ConnectionManager.ConnectManager = new Thread[outputChannelFromDB.size()+inputChannelFromDB.size()]; //Raza commenting

			for (Channel channel : outputChannelFromDB) {
				channel = setChannelProperties(channel);
				channel.setConnectionStatus(NetworkInfoStatus.SOCKET_RESET); //Raza Rest initially
				channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_RESET); //Raza Rest initially
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
				OutputChannels.add((OutputChannel) channel);
			}
		} catch (Exception e) {
			logger.error("Encounter with an exception.( " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")",
					e);
			// e.printStackTrace();
			return null;
		}

		List<Channel> result = new ArrayList<Channel>();
		result.addAll(OutputChannels);
		result.addAll(InputChannels);
		return result;
	}
	private String checkNull(String string, String error) throws BadConfigException {
		if (string == null) {
			logger.error("Channel config exception: " + error);
			throw new BadConfigException("Channel config exception: " + error);
		}
		return string;
	}

	private boolean isValidIPAddress(String ip) {
		int counter = 0;
		int index = 0;
		while (index != -1) {
			counter++;
			index = (index == 0) ? index : index + 1;

			index = ip.indexOf(".", index);

		}

		return (counter == 4);
	}

	public Channel getChannel(String instCode, String type) {
		// List<ConfigElement> channels =
		// ConfigSvc.getInstance().getConfigElement("Bus.Configuration.ChannelList").getAllConfigElement("Channel");
		for (Channel channel : GlobalContext.getInstance().getAllChannels().values()) {
			if (!EndPointType.isSwitchTerminal(channel.getEndPointType()))
				continue;

			if (instCode.equals(channel.getInstitutionId()) && channel instanceof InputChannel) {
				if (type.equals("in"))
					return channel;
				else if (channel.getCommunicationMethod().equals(CommunicationMethod.SAME_SOCKET))
					return channel;
				else
					return ((InputChannel) channel).getOriginatorChannel();

			} else if (type.equals("out") && channel instanceof OutputChannel
					&& instCode.equals(channel.getInstitutionId())) {
				return channel;
			} else if (type.equals("") && instCode.equals(channel.getInstitutionId())) {
				return channel;
			}
		}
		return null;
	}
	public Channel setChannelProperties(Channel channel) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		String ioFilter, ioFilterParameter;
		String[] split;
		EndPointType pointType;
		CommunicationMethod method;

		ioFilterParameter = null;
		pointType = EndPointType.valueOf(channel.getEndPointTypeName());
		channel.setAddress(new InetSocketAddress(channel.getIp(), channel.getPort()));

		ioFilter = channel.getIoFilterClassName();
		if (Util.hasText(ioFilter)) {
			if (ioFilter.contains("[")) {
				split = ioFilter.split("[\\[]");
				ioFilter = split[0];
				ioFilterParameter = split[1].substring(0, split[1].indexOf("]"));
			}
		}
		channel.setIoFilterClassName(ioFilter);

		if (channel.getCommunicationMethodName().equals("AnotherSocket")) {
			method = CommunicationMethod.ANOTHER_SOCKET;
		} else {
			method = CommunicationMethod.SAME_SOCKET;
		}

		channel.setCommunicationMethod(method);
		channel.setIsSecure(false);
		channel.setSslEnable(false);
		channel.setEndPointType(pointType);
		channel.setClearingActionJobsBean(channel.getClearingActionJobsBean());
		channel.setClearingActionMapperBean(channel.getClearingActionMapperBean());

		if (channel.getIoFilterObject() instanceof Mina2CMSIoFilter) {
			((Mina2CMSIoFilter) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2NCCIoFilter) {
			((Mina2NCCIoFilter) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2NCC2HDLCIoFilter) {
			((Mina2NCC2HDLCIoFilter) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2MeganacHDLCIoFilter) {
			((Mina2MeganacHDLCIoFilter) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2Meganac2HDLCIoFilter) {
			((Mina2Meganac2HDLCIoFilter) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2NCC2IoFilterNew) {
			((Mina2NCC2IoFilterNew) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2Meganac2IoFilterNew) {
			((Mina2Meganac2IoFilterNew) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2Meganac4IoFilterNew) {
			((Mina2Meganac4IoFilterNew) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		} else if (channel.getIoFilterObject() instanceof Mina2NCC4IoFilterNew) {
			((Mina2NCC4IoFilterNew) channel.getIoFilterObject()).setParameter(ioFilterParameter);
		}

		return channel;
	}
}
