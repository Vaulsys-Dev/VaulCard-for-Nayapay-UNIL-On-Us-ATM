package vaulsys.protocols.negin87;

import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.terminal.TerminalService;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class Negin87ProtocolFunctions extends ISOFunctions {

    transient Logger logger = Logger.getLogger(Negin87ProtocolFunctions.class);

//    public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException {
//        try {
//            int length = rawdata.length;
//            byte[] data = new byte[length];
//            System.arraycopy(rawdata, 0, data, 0, length);
//
//            ISOPackager packager = getPackager();
//
//            logger.debug("parsing to ISOMsg");
//            ISOMsg message = new ISOMsg();
//            message.setPackager(packager);
//            message.unpack(data);
//
//            logger.debug("saving to IncomingMessage.GivenMessage");
//            return message; 
////            if (ISO8583v87Validator.validate(message))
////            	return message;
////            else 
////            	throw new NotParsedBinaryToProtocolException("Validity Check Failed");
//        } catch (Exception ex) {
//            logger
//                    .debug(
//                            "Exception in parsing IncomingMessage.Binary to ISOMsg",
//                            ex);
//            throw new NotParsedBinaryToProtocolException(ex);
//        }
//    }

//	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
//        try {
//            ProtocolDialog dialog = getDialog();
//            dialog.refine(protocolMessage);
//            ISOMsg message = (ISOMsg) protocolMessage;
//
//
//            ISOPackager packager = getPackager();
//
//            logger.debug("producing Binary." + message);
//            //message.getValue(48);
//            message.setPackager(packager);
//
//            return message.pack();
//
//        } catch (Exception ex) {
//            logger
//                    .debug(
//                            "Exception in producing OutgoingMessage.Binary from ISOMsg",
//                            ex);
//            throw new NotProducedProtocolToBinaryException(ex);
//        }
//    }
//
//    public ProtocolMessage fromIfx(Ifx ifx) throws NotMappedIfxToProtocolException {
//        logger.debug("creating ISOMsg");
//        logger.debug(ifx);
//        try {
//            //	    logger.debug("saving to OutgoingMessage.MessageToBeSent");
////            return ISO8583v87IFXToISOMapper.map(ifx);
//            getProtocolToIfxMapper().map(ifx);
//        } catch (Exception ex) {
//            logger.debug("Exception in mapping IFX to ISOMsg", ex);
//            throw new NotMappedIfxToProtocolException(ex);
//        }
//    }
//
//    public Ifx toIfx(ProtocolMessage protocolMessage) throws NotMappedProtocolToIfxException {
//        try {
//            ISOMsg message = (ISOMsg) protocolMessage;
//            logger.debug("creating IFX");
//            // Ifx ifx = getIfx(message, channel.getProtocol());
//            //	    logger.debug("saving to IncomingMessage.IFX");
//            return ISO8583v87ISOToIFXMapper.map(message);
//        } catch (Exception ex) {
//            logger.debug("Exception in mapping ISOMsg to IFX", ex);
//            throw new NotMappedProtocolToIfxException(ex);
//        }
//    }

    @Override
    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
        try {
//        	Institution owner = (Institution) transaction.getOutgoingIfxOrMessageEndpoint().getOwner();
        	Institution owner = ProcessContext.get().getInstitution(ProcessContext.get().getSwitchTerminal(transaction.getOutputMessage().getEndPointTerminalId()).getOwnerId().toString());
        	if (TerminalService.isNeedToSetSettleDate(transaction)) {
    			ClearingDate wDate = owner.getCurrentWorkingDay();
    			MonthDayDate daydate =  wDate == null? MonthDayDate.now() : new MonthDayDate(wDate.getDate());
    			outgoingIFX.setPostedDt(daydate);
				outgoingIFX.setSettleDt(daydate);
				
    		} else {
    			outgoingIFX.setPostedDt(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getPostedDt());
				outgoingIFX.setSettleDt(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getSettleDt());
    		}
        	
    		/*ClearingDate wDate = owner.getCurrentWorkingDay();
    		MonthDayDate daydate =  wDate == null? MonthDayDate.now() : new MonthDayDate(wDate.getDate());
    		outgoingIFX.setSettleDt(daydate);
    		outgoingIFX.setPostedDt(daydate);*/
        } catch (Exception e) {
            throw new CantAddNecessaryDataToIfxException(e);
        }

    }

    @Override
    public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
        // TODO Auto-generated method stub

    }


	@Override
	public ISOPackager getPackager() {
		return ((Negin87Protocol) ProtocolProvider
                .Instance.getByClass(Negin87Protocol.class))
                .getPackager();
	}

	@Override
	public IfxToProtocolMapper getIfxToProtocolMapper() {
		return Negin87IFXToISOMapper.Instance; 
	}

	@Override
	public ProtocolToIfxMapper getProtocolToIfxMapper() {
		return Negin87ISOToIFXMapper.Instance;
	}

	@Override
	public ProtocolDialog getDialog() {
		return new Negin87ProtocolDialog();
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incoMessage) throws CantPostProcessBinaryDataException {
		return incoMessage.getBinaryData();
	}

	@Override
	public byte[] decryptSecureBinaryMessage(byte[] encryptedData,
			Message incomingMessage) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] encryptBinaryMessage(byte[] rawdata, Message incomingMessage)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
