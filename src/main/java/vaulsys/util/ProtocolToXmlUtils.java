package vaulsys.util;

import vaulsys.message.Message;
import vaulsys.protocols.apacs70.base.BaseMsg;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.cms.CMSHttpMessage;
import vaulsys.protocols.cmsnew.CMSMessage;
import vaulsys.protocols.epay.base.EpayMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.packager.XMLPackager;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ui.MessageObject;
import vaulsys.protocols.xmlifx.XMLIFXMsg;

import org.apache.log4j.Logger;

public class ProtocolToXmlUtils {
	private static final Logger logger = Logger.getLogger(ProtocolToXmlUtils.class);

    public static void setXMLdata(Message message) {
        try {
        	ProtocolMessage protMsg = message.getProtocolMessage();
            if (protMsg instanceof ISOMsg) {
                ISOMsg isoMsg = (ISOMsg) protMsg;
                ISOPackager lastPackager = isoMsg.getPackager();
                isoMsg.setPackager(new XMLPackager());
                String xmlString = new String(isoMsg.pack());
                message.setXML(xmlString);
                isoMsg.setPackager(lastPackager);
            } else if (protMsg instanceof NDCMsg) {
                message.setXML(protMsg.toString());
            } else if (message.getProtocolMessage() instanceof CMSHttpMessage) {
            	message.setXML(protMsg.toString());
            } else if (message.getProtocolMessage() instanceof CMSMessage) {
            	message.setXML(protMsg.toString());
	        } else if (protMsg instanceof EpayMsg) {
	        	message.setXML(((EpayMsg)protMsg).xml);
	        } else if (protMsg instanceof MessageObject){
	        	message.setXML(protMsg.toString());
	        } else if (protMsg instanceof XMLIFXMsg) {
	        	message.setXML(new String (message.getBinaryData()));
	        } else if(protMsg instanceof BaseMsg/*Apacs70Msg*/) {
	        	message.setXML(protMsg.toString());
	        } else {
	        	logger.warn("No XML mapping is defined for protocol message: " + protMsg.getClass().getName());
	        }
        } catch (ISOException e) {
            if (message != null) {
                message.setXML("Error setting XML data.");
            }
        }
    }
}
