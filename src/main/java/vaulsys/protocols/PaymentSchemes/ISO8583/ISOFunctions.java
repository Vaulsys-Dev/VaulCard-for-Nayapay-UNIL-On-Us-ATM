package vaulsys.protocols.PaymentSchemes.ISO8583;

import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;


public abstract class ISOFunctions implements ProtocolFunctions {
    
	private Logger logger = Logger.getLogger(this.getClass()); 
	
	abstract public ISOPackager getPackager();
	
	abstract public ProtocolDialog getDialog();
	
	abstract public IfxToProtocolMapper getIfxToProtocolMapper();
	    
	abstract public ProtocolToIfxMapper getProtocolToIfxMapper();
	
	protected String removeLeftZero(String s) {
        return Integer.toString(Integer.parseInt(s));
    }
    
	@Override
	public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
		 try {
	            ISOPackager packager = getPackager();

//	            logger.debug("parsing to ISOMsg");
	            ISOMsg message = new ISOMsg();
	            message.setPackager(packager);
			    //System.out.println("ISOFunctions:: Going to Unpack from Binary...!"); //Raza TEMP
	            message.unpack(rawdata);

			 //Raza TPSP Channel Add start
			 	message.setDirection(ISOMsg.INCOMING);
			 	ProtocolDialog dialog = getDialog();
			 	//logger.info("Translating incoming message from MasterCard...");
			    //ISOMsg.MapFieldsFromMasterCard(message);
			    dialog.TranslateToFanap(message);
			    //ISOMsg.MapFieldsFromTpsp(message); //use this for MasterCard
			 	dialog.refine(message);
			 //Raza TPSP Channel Add end




	            return message; 
	        } catch (Exception ex) {
	            logger.error("Exception in parsing IncomingMessage.Binary to ISOMsg. "+ ex, ex);
	            throw new NotParsedBinaryToProtocolException(ex);
	        }
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {
//		logger.debug("creating ISOMsg");
//		logger.debug(ifx);
		try {
			//System.out.println("Going to Call Map...!"); //Raza TEMP
			return getIfxToProtocolMapper().map(ifx, convertor);
		} catch (Exception ex) {
			logger.error("Exception in mapping IFX to ISOMsg", ex);
			throw new NotMappedIfxToProtocolException(ex);
		}
	}

	@Override
	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
		try {
            ProtocolDialog dialog = getDialog();

			ISOMsg message = (ISOMsg) protocolMessage;

            dialog.refine(protocolMessage);
			//System.out.println("protocolMessage [" + protocolMessage + "]"); //Raza TEMP
			//System.out.println("message [" + message + "]"); //Raza TEMP
			dialog.TranslateFromFanap(protocolMessage);
			//logger.info("Translating outgoing message for MasterCard...");
			//ISOMsg.MapFieldsForMasterCard(message); //Use this for MasterCard


            ISOPackager packager = getPackager();

//            logger.debug("producing Binary." + message);
            
            message.setPackager(packager);
			logger.error("Not setting Mac Response Code");
			//Raza TPSP Channel Add commenting start - mac Field 128 not required
			//byte[] mac = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
			//message.unset(128);
			//message.unset(64);
			//if (message.getMaxField() <= 64){
			//	message.set(64, mac);
			//	message.unset(128);
			//}else{
			//	message.set(128, mac);
			//	message.unset(64);
			//}
			//Raza TPSP Channel Add commenting end - mac Field 128 not required
			//System.out.println("Field 43 from message [" + message.getString(43) + "]");//Raza TEMP
			//System.out.println("Field 43 from message Value [" + message.getValue(43) + "]");//Raza TEMP

			//System.out.println("Field message field numbers [" + message.getFieldNumbers() + "]");//Raza TEMP

			return message.pack();
            
        } catch (Exception ex) {
            logger.error("Exception in producing OutgoingMessage.Binary from ISOMsg", ex);
            throw new NotProducedProtocolToBinaryException(ex);
        }
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor) throws NotMappedProtocolToIfxException {
		try {
            ISOMsg message = (ISOMsg) protocolMessage;
//            logger.debug("creating IFX");
          return  getProtocolToIfxMapper().map(message, convertor);
        } catch (Exception ex) {
            logger.error("Exception in mapping ISOMsg to IFX", ex);
            throw new NotMappedProtocolToIfxException(ex);
        }
	}

	
	@Override
	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX, EncodingConvertor convertor) throws Exception {
		ISOMsg incomingIsoMsg = (ISOMsg) incomingMessage; 
		ISOMsg outIsoMsg = incomingIsoMsg;
		Integer mti = new Integer(incomingIsoMsg.getMTI());
		mti += 10;
		// response message has invalid mti!
		if (incomingIsoMsg.isResponse())
			return null;
		
		outIsoMsg.setMTI("0"+ mti.toString()); 
		outIsoMsg.set(39, ISOResponseCodes.INVALID_CARD_STATUS);
		return outIsoMsg;
	}
	
	@Override
	public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException {
		 try {
			 Channel channel = outgoingMessage.getChannel();
			 ProtocolSecurityFunctions securityFunctions = channel.getProtocol().getSecurityFunctions();
			 Terminal t = outgoingMessage.getEndPointTerminal();
			 securityFunctions.setMac(processContext, t, t.getOwnOrParentSecurityProfileId(), t.getKeySet(), outgoingMessage, channel.getMacEnable());

	        } catch (Exception e) {
	        	logger.error("CantPostProcessBinaryDataException! "+ e);
	            throw new CantPostProcessBinaryDataException(e);
	        }
	}

}
