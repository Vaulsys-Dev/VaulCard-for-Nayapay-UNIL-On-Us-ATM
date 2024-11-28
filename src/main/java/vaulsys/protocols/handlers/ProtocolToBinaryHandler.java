package vaulsys.protocols.handlers;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.cms.CMSHttpMessage;
import vaulsys.protocols.cmsnew.CMSMessage;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.shetab87.Shetab87Protocol;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.wfe.ProcessContext;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class ProtocolToBinaryHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(ProtocolToBinaryHandler.class);

    public static final ProtocolToBinaryHandler Instance = new ProtocolToBinaryHandler();

    private ProtocolToBinaryHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        try {
            Set<Message> messages = new HashSet<Message>();
            if (processContext.getOutputMessage() != null){
            	messages.add(processContext.getOutputMessage());
            }

            if (processContext.getPendingResponses()!= null && !processContext.getPendingResponses().isEmpty()){
            	messages.addAll(processContext.getPendingResponses());
            }

            for (Message outgoingMessage: messages){
            	if (outgoingMessage != null && outgoingMessage.isOutgoingMessage()) {

            		Channel channel = outgoingMessage.getChannel();
            		Terminal endPointTerminal = outgoingMessage.getEndPointTerminal();
					if (endPointTerminal != null && /*!TerminalType.SWITCH.equals(endPointTerminal.getTerminalType())*/  !EndPointType.isSwitchTerminal(channel.getEndPointType())) {
						TerminalService.setLastTransaction(endPointTerminal, outgoingMessage);
            		}

            		ProtocolFunctions mapper = channel.getProtocol().getMapper();
//            		logger.debug("producing binary message ... ");
//            		((BaseComponent) mapper).setProcessContext(processContext);
					//System.out.println("ProtocolToBinaryHandler:: Mapper [" + mapper.toString() + "]"); //Raza TEMP
            		byte[] data = mapper.toBinary(outgoingMessage.getProtocolMessage());
					//String temp = new String(data); //Raza TEMP
					//System.out.println("ProtocolToBinaryHandler:: data [" + temp + "]"); //Raza TEMP

					// Added By : Asim Shahzad, Date : 8th Dec 2016, Desc : For VISA SMS handling
					//if(channel.getName() == ChannelCodes.VISA_SMS.toString())
					//{
					if (outgoingMessage.getHeaderData() == null && outgoingMessage.getChannel().getHeaderLen() > 0) {
						outgoingMessage.setHeaderData(outgoingMessage.getTransaction().getFirstTransaction().getInputMessage().getHeaderData());

						//in case the message is schedule message, header will be null
						if (outgoingMessage.getHeaderData() == null) {
							if (outgoingMessage.getTransaction().getReferenceTransaction() != null)
								outgoingMessage.setHeaderData(outgoingMessage.getTransaction().getReferenceTransaction().getInputMessage().getHeaderData());
						}
					}

					data = outgoingMessage.setBinaryDataWithHeader(data);
					//}

            		outgoingMessage.setBinaryData(data);
					//System.out.println("ProtocolToBinaryHandler:: Outgoing Msg ID [" + outgoingMessage.getId() + "]"); //Raza TEMP
					//System.out.println("ProtocolToBinaryHandler:: Outgoing IFX-ID [" + outgoingMessage.getIfx().getId() + "]"); //Raza LOGGING ENHANCED
					//System.out.println("ProtocolToBinaryHandler:: Outgoing Msg Binary Data get [" + outgoingMessage.getBinaryData() + "]"); //Raza TEMP
					//String temp2 = new String(outgoingMessage.getBinaryData());
					//System.out.println("ProtocolToBinaryHandler:: Outgoing Msg Binary Data get direct [" + temp2 + "]"); //Raza TEMP

            		// Setting MAC
            		//mapper.postProcessBinaryMessage(processContext, outgoingMessage); //Raza commenting TEMP
            		
		        if (Boolean.TRUE.equals(channel.getIsSecure())) {
            			if (outgoingMessage.getTransaction().getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) {
            				outgoingMessage.setBinaryData(mapper.encryptBinaryMessage(outgoingMessage.getBinaryData(), outgoingMessage.getTransaction().getReferenceTransaction().getInputMessage()));
							//System.out.println("ProtocolToBinaryHandler:: Binary data - [" + outgoingMessage.getBinaryData() + "]"); //Raza TEMP
            			} else {
            				outgoingMessage.setBinaryData(mapper.encryptBinaryMessage(outgoingMessage.getBinaryData(), outgoingMessage.getTransaction().getFirstTransaction().getInputMessage()));
							//System.out.println("ProtocolToBinaryHandler:: Binary data -- [" + outgoingMessage.getBinaryData() + "]"); //Raza TEMP
            			}
            		}

            		ProtocolToXmlUtils.setXMLdata(outgoingMessage);
            		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
            		if (!(outgoingMessage.getProtocolMessage() instanceof CMSHttpMessage)) 
            			if (!(outgoingMessage.getProtocolMessage() instanceof CMSMessage)) 
            				if(!(channel.getProtocol() instanceof Shetab87Protocol))
            					GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
            		
//            		getGeneralDao().saveOrUpdate(endPointTerminal);
            		logger.info("SENT to " + channel.getName() + ":\n" + outgoingMessage.getXML()/*outgoingMessage.getProtocolMessage().toString()*/);
            	}
            }
        } catch (Exception ex) {
            logger.error("Exception: "+ ex, ex);
            throw ex;
        }
    }

}
