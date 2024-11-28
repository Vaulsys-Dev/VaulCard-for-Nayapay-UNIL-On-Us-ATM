package vaulsys.protocols.jware93;

import vaulsys.message.Message;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.transaction.Transaction;

import org.apache.log4j.Logger;

public class Jware93ProtocolMapper extends ISOFunctions {

    transient Logger logger = Logger.getLogger(Jware93ProtocolMapper.class);

    public ISOPackager getPackager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProtocolDialog getDialog() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IfxToProtocolMapper getIfxToProtocolMapper() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProtocolToIfxMapper getProtocolToIfxMapper() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProtocolMessage fromBinary(byte[] rawdata) {

        logger.debug("getting IncomingMessage.Binary");
        try {
            byte[] binaryData = rawdata;

            int length = (binaryData[0] >= 0 ? binaryData[0] : binaryData[0] + 256);// binaryData[0];
            length = length * 256 + (binaryData[1] >= 0 ? binaryData[1] : binaryData[1] + 256);
            length = length * 256 + (binaryData[2] >= 0 ? binaryData[2] : binaryData[2] + 256);
            length = length * 256 + (binaryData[3] >= 0 ? binaryData[3] : binaryData[3] + 256);
            if (length + 4 != binaryData.length) {
                logger.warn("Message currupted.", new Exception("Message length is not correct."));
            }

            byte[] data = new byte[length];
            System.arraycopy(binaryData, 4, data, 0, length);

            ISOPackager packager = ((Jware93Protocol) ProtocolProvider.Instance.getByClass(Jware93Protocol.class))
                    .getPackager();

            logger.debug("parsing to ISOMsg");
            ISOMsg message = new ISOMsg();
            message.setPackager(packager);
            message.unpack(data);

            logger.debug("saving to IncomingMessage.GivenMessage");
            return message;
        } catch (Exception ex) {
            logger.error("Exception in parsing IncomingMessage.Binary to ISOMsg", ex);
        }
        return null;
    }

    public byte[] toBinary(ProtocolMessage protocolMessage) {
        logger.debug("getting OutgoingMessages");
        try {
            ISOMsg message = (ISOMsg) protocolMessage;

            ISOPackager packager = ((Jware93Protocol) ProtocolProvider.Instance.getByClass(Jware93Protocol.class))
                    .getPackager();

            logger.debug("producing Binary.");
            message.setPackager(packager);
            byte[] data = message.pack();
            byte[] binData = new byte[data.length + 4];
            System.arraycopy(data, 0, binData, 4, data.length);

            // TODO does not work for message larger than 255 byte

            binData[3] = data.length < 128 ? (byte) data.length : (byte) (data.length - 256);

            logger.debug("saving to OutgoingMessage.Binary");

            return binData;
        } catch (Exception ex) {
            logger.error("Exception in producing OutgoingMessage.Binary from ISOMsg", ex);
        }
        return null;
    }

    public ProtocolMessage fromIfx(Ifx ifx) {
        logger.debug("getting IFXMsg from bus");
        logger.debug("creating ISOMsg");
        logger.debug(ifx);

        try {
            ISOMsg isoMsg = Jware93IFXToISOMapper.map(ifx);

            logger.debug("saving to OutgoingMessage.MessageToBeSent");
            return isoMsg;
        } catch (Exception ex) {
            logger.error("Exception in mapping IFX to ISOMsg", ex);
        }
        return null;
    }

    public Ifx toIfx(ProtocolMessage protocolMessage) {
        logger.debug("getting ISOMsg from bus");
        try {
            ISOMsg message = (ISOMsg) protocolMessage;
            logger.debug("creating IFX");
            // Ifx ifx = getIfx(message, channel.getProtocol());
            Ifx ifx = Jware93ISOToIFXMapper.map(message);
            logger.debug("saving to IncomingMessage.IFX");
            return ifx;
        } catch (Exception ex) {
            logger.error("Exception in mapping ISOMsg to IFX", ex);
        }
        return null;
    }

    @Override
    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
        // TODO Auto-generated method stub

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

//	private Ifx getIfx(ISOMsg msg, Protocol protocol) {
//		Ifx ifx = new Ifx();
//
//		Configuration protocolCfg = ConfigurationManager.getInstance().getConfiguration("protocol");
//		String rootKey = "/protocols/protocol[@name='" + protocol.getName() + "']";
//		Iterator<String> itkeys = protocolCfg.getKeys(rootKey);
//		while (itkeys.hasNext()) {
//			String ifxField = itkeys.next();
//			String contact = ifxField.replace(rootKey + "/", "").replace("/", ".");
//			if (contact.contains("@"))
//				continue;
//			String isoField = protocolCfg.getString(ifxField);
//			isoField = isoField.replace("ISO_", "");
//			int fieldId = -1;
//			String fid = null;
//			String operator = null;
//			if (isoField.contains("(")) {
//				fid = isoField.substring(0, isoField.indexOf("("));
//				operator = isoField.substring(isoField.indexOf("("));
//			} else
//				fid = isoField;
//			fieldId = Integer.valueOf(fid);
//
//			if (msg.getComponent(fieldId) != null) {
//				try {
//					String fieldData = msg.getValue(fieldId).toString();
//					if (operator != null) {
//						int fromInd = Integer.valueOf(operator.substring(1, operator.indexOf(':')));
//						int toInd = Integer.valueOf(operator
//								.substring(operator.indexOf(':') + 1, operator.length() - 1));
//						if (fieldData.length() > toInd)
//							fieldData = fieldData.substring(fromInd, toInd + 1);
//					}
//
//					ifx.set(contact, fieldData);
//				} catch (ISOException ex) {
//					logger.error("Creating Ifx", ex);
//
//				}
//			}
//		}
//
//		return ifx;
//	}

}
