package vaulsys.protocols.handlers;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.base.Protocol;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.CMSHttpMessage;
import vaulsys.protocols.cmsnew.CMSMessage;
import vaulsys.protocols.exception.exception.NotApplicableTypeMessageException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

import java.util.Arrays;

public class BinaryToProtocolHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(BinaryToProtocolHandler.class);

    public static final BinaryToProtocolHandler Instance = new BinaryToProtocolHandler();

    private BinaryToProtocolHandler() {
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        try {
//            logger.debug("BinaryToProtocolHandler Started");
            Message incomingMessage;
            ProtocolMessage fromBinary;

            int headerLen;
            byte[] dataWithoutHeader;
            byte[] headerData;

            if (processContext.getInputMessage().isIncomingMessage()){
                incomingMessage =  processContext.getInputMessage();
            } else {
                logger.debug("Only IncomingMessage type can enter main flow. Input message type was:" + processContext.getInputMessage());
                throw new NotApplicableTypeMessageException();
            }

            Channel channel = incomingMessage.getChannel();

            Protocol protocol = channel.getProtocol();
            ProtocolFunctions mapper = protocol.getMapper();

//            ((BaseComponent) mapper).setProcessContext(processContext);
            //byte[] data = mapper.preProcessBinaryMessage(incomingMessage); //Raza TEMP Commenting
            byte[] data = incomingMessage.getBinaryData();
            //if (Boolean.TRUE.equals(channel.getIsSecure())) {
            	//data = mapper.decryptSecureBinaryMessage(data, incomingMessage);
            //}

//            ProtocolMessage fromBinary = mapper.fromBinary(incomingMessage.getBinaryData());

            // Modified By : Asim Shahzad, Date : 8th Dec 2016, Desc : For VISA SMS header handling
            //ProtocolMessage fromBinary = mapper.fromBinary(data);

            headerData = null;
            headerLen = channel.getHeaderLen();

            if (headerLen > 0) {
                logger.info("Message Header Found..!");

                //check if the reject code is approved or not and act accordingly
                //headerLen = incomingMessage.validateRejectCodeFromHeader(data, headerLen);

                headerData = Arrays.copyOfRange(data, 0, headerLen);
                dataWithoutHeader = Arrays.copyOfRange(data, headerLen, data.length);

                //setting header data in message
                incomingMessage.setHeaderData(headerData);
            }
            else {
                dataWithoutHeader = data;
            }

            //ProtocolMessage fromBinary = mapper.fromBinary(data);
            fromBinary = mapper.fromBinary(dataWithoutHeader);


            if (fromBinary instanceof ISOMsg)
                ((ISOMsg) fromBinary).setHeader(headerData);

            //*************************************************************************************

            incomingMessage.setRequest(fromBinary.isRequest());
            incomingMessage.setNeedResponse(incomingMessage.getRequest());
            if (incomingMessage.getRequest() != null)
            	incomingMessage.setNeedToBeInstantlyReversed(!incomingMessage.getRequest());

			incomingMessage.setProtocolMessage(fromBinary);
            ProtocolToXmlUtils.setXMLdata(incomingMessage);
            
            if (!(incomingMessage.getProtocolMessage() instanceof CMSHttpMessage))
            	if (!(incomingMessage.getProtocolMessage() instanceof CMSMessage))
            		GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());

            logger.info("RECEIVED from " + channel.getName() + ":\n" + incomingMessage.getXML()/*incomingMessage.getProtocolMessage().toString()*/);

        } catch (Exception ex) {
        	logger.error(ex.getClass().getSimpleName()+": "+ ex.getMessage());
        	throw new NotParsedBinaryToProtocolException();
        }
        return;
    }
}
