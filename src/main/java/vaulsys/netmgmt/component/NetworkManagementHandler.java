package vaulsys.netmgmt.component;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.protocols.base.Protocol;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.infotech.InfotechProtocolFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCConsumerRequestMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.transaction.LifeCycle;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class NetworkManagementHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(NetworkManagementHandler.class);

    public static final NetworkManagementHandler Instance = new NetworkManagementHandler();

    private NetworkManagementHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {

        try {
            Message inputMessage = processContext.getInputMessage();
            ProtocolMessage protocolMessage = inputMessage.getProtocolMessage();
            NetworkManagementAction action = NetworkManagementAction.NO_ACTION_MESSAGE_UNSUPPORTED;
            //TODO: Refactor and attach net-mngmt to protocol
            if (protocolMessage instanceof NDCMsg) {
                if (inputMessage.getProtocolMessage() instanceof NDCUnsolicitedStatusMsg ||
                        inputMessage.getProtocolMessage() instanceof NDCSolicitedStatusMsg ||
                        inputMessage.getProtocolMessage() instanceof NDCConsumerRequestMsg) {
//                    NDCNetworkManagementComponent ndcnetcomp = new NDCNetworkManagementComponent();
//                    NDCNetworkManagementComponent.setProcessContext(processContext);
                    action = NDCNetworkManagementComponent.processNDCNetworkManagementMessage(processContext, inputMessage);
                }
            } else if (inputMessage.getProtocolMessage() instanceof ISOMsg) {
            	 Channel channel = inputMessage.getChannel();
                 Protocol protocol = channel.getProtocol();
                 ProtocolFunctions mapper = protocol.getMapper();
                 if (mapper  instanceof InfotechProtocolFunctions) {
//                	 InfotechNetworkManagementComponent mbc = new InfotechNetworkManagementComponent();
//                	 mbc.setProcessContext(processContext);
                	 action = InfotechNetworkManagementComponent.processISONetworkManagementMessage(processContext, inputMessage);
                	 
                 } else {
//                	ISONetworkManagementComponent mbc = new ISONetworkManagementComponent();
//                	mbc.setProcessContext(processContext);
                	//action = ISONetworkManagementComponent.processISONetworkManagementMessage(processContext, inputMessage); //Raza commenting
                     System.out.println("NetworkManagementHandler:: Going to Process Payment Scheme ISO Message...!"); //Raza TEMP
					action = ISONetworkManagementComponent.processPaymentSchemesISONetworkManagementMessage(processContext, inputMessage);	                
                 }
            } else if(protocolMessage instanceof RqBaseMsg/*Apacs70Msg*/) {
            	action = Apacs70NetworkManagementComponent.processNetworkManagementMessage(processContext, inputMessage);
            }

            if (action == NetworkManagementAction.NO_ACTION_MESSAGE_UNSUPPORTED) {
                logger.info("Leaving network management: unsupported network message.");
                leaveToEndState(processContext);
                return;
            }

            if (action == NetworkManagementAction.DONE_WITHOUT_OUTPUT) {
                logger.info("Network management successfully processed message without output.");
                leaveToEndState(processContext);
                return;
            }

            if (action == NetworkManagementAction.OUTPUT_MESSAGE_CREATED) {
                logger.info("Network management successfully processed message with output.");
                return;
            }

            if (inputMessage.getTransaction().getLifeCycle() == null){
            	LifeCycle lifeCycle = new LifeCycle();
            	GeneralDao.Instance.saveOrUpdate(lifeCycle);
            	inputMessage.getTransaction().setLifeCycle(lifeCycle);
            	GeneralDao.Instance.saveOrUpdate(inputMessage.getTransaction());
            }

        } catch (Exception ex) {
        	if(ex instanceof MacFailException){
        		logger.warn("Network management error:" + ex, ex);        		
        	}else{
        		logger.error("Network management error:" + ex, ex);
        	}
            throw ex;
        }
    }

}
