package vaulsys.protocols.xmlifx;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class XMLIFXProtocolFunctions implements ProtocolFunctions {
    transient static Logger logger = Logger.getLogger(XMLIFXProtocolFunctions.class);

	@Override
	public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction)
			throws CantAddNecessaryDataToIfxException {
		
	}

	@Override
	public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction)
			throws CantAddNecessaryDataToIfxException {
	}

	@Override
	public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
    	InputStream is = new ByteArrayInputStream(rawdata);    	
//    	logger.debug(new String(rawdata));
		XStream xStream = new XStream();
		xStream.alias("ifx", XMLIFXMsg.class);
		XMLIFXMsg ifxMsg = (XMLIFXMsg) xStream.fromXML(is);
		
//		ifxMsg.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(ifxMsg.getSrc_TrnSeqCntr()));
		ifxMsg.setMy_TrnSeqCntr(ifxMsg.getSrc_TrnSeqCntr());
//		ifxMsg.setNetworkRefId(ISOUtil.zeroUnPad(ifxMsg.getNetworkRefId()));
		if (ifxMsg.getTrnType() == null || TrnType.UNKNOWN.equals(ifxMsg.getTrnType()))
			ifxMsg.setTrnType(getTrnTypeByIfxType(ifxMsg.getIfxType()));
//		ifxMsg.setBankId(GlobalContext.getInstance().getMyInstitution().getBin());
		ifxMsg.setBankId(ProcessContext.get().getMyInstitution().getBin().toString());
		ifxMsg.setDestBankId(ifxMsg.getAppPAN().substring(0, 6));
		ifxMsg.setAccTypeFrom(AccType.MAIN_ACCOUNT);
		ifxMsg.setAccTypeTo(AccType.MAIN_ACCOUNT);
		
		ifxMsg.xml = new String(rawdata);
		return ifxMsg;
	}

	private TrnType getTrnTypeByIfxType(IfxType ifxType) {
		if (ISOFinalMessageType.isBalanceInqueryMessage(ifxType))
			return TrnType.BALANCEINQUIRY;
		
		if (ISOFinalMessageType.isPurchaseMessage(ifxType) ||
				ISOFinalMessageType.isPurchaseReverseMessage(ifxType))
			return TrnType.PURCHASE;
		
		return TrnType.UNKNOWN;
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {
		return new XMLIFXMsg(ifx);
	}

	@Override
	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX, EncodingConvertor convertor) throws Exception {
		return new XMLIFXMsg((Ifx) incomingMessage);
	}

	@Override
	public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException {
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incommingMessage) throws Exception {
		return incommingMessage.getBinaryData();
	}

	@Override
	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
		XMLIFXMsg ifxMsg = (XMLIFXMsg) protocolMessage;
		
//		ifxMsg.setSrc_TrnSeqCntr(ISOUtil.zeropad(ifxMsg.getSrc_TrnSeqCntr()));
//		ifxMsg.setMy_TrnSeqCntr(ISOUtil.zeropad(ifxMsg.getSrc_TrnSeqCntr()));
//		ifxMsg.setNetworkRefId(ISOUtil.zeropad(ifxMsg.getNetworkRefId()));
		
//		ifxMsg.setAccTypeFrom(null);
//		ifxMsg.setAccTypeTo(null);
//		ifxMsg.setIfxDirection(null);
//		ifxMsg.setTrnType(null);
//		ifxMsg.setReceivedDt(null);
//		ifxMsg.setPostedDt(null);
//		ifxMsg.setSettleDt(null);
//		ifxMsg.setTrnDt(null);
//		ifxMsg.setBankStatementData(null);
		
		
		XStream xStream = new XStream();
		xStream.alias("ifx", XMLIFXMsg.class);
		String xml = xStream.toXML(protocolMessage);
//		System.out.println(xml);
		return xml.getBytes();
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor)
			throws NotMappedProtocolToIfxException {
		return ((XMLIFXMsg) protocolMessage).ifxClone();
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
