package vaulsys.protocols.maskantest;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.epay.EpayProtocolFunctions;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class MaskanProtocolFunctions implements ProtocolFunctions {
    transient static Logger logger = Logger.getLogger(EpayProtocolFunctions.class);
    
	@Override
	public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
		MaskanMsg msg = new MaskanMsg();
		msg.unpack(rawdata);
		
		logger.debug(new String(rawdata));
		return msg;
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {
        MaskanMsg maskanMsg = new MaskanMsg();
        
        maskanMsg.depositNumber = ifx.getAppPAN();
        maskanMsg.date  = ifx.getOrigDt();
        maskanMsg.ref1  = ifx.getSrc_TrnSeqCntr();
        maskanMsg.ref2 = ifx.getNetworkRefId();
        maskanMsg.branch = ifx.getTerminalId();
        maskanMsg.type = ISOFinalMessageType.isRequestMessage(ifx.getIfxType())?"request":"response";
        
        if(Util.hasText(ifx.getRsCode())){
	        if(ifx.getRsCode().equals("12"))
	        	maskanMsg.result = "No Result";
	        else
	        	maskanMsg.result = ifx.getAcctBalAvailableAmt();
        }

        return maskanMsg;		
	}

	@Override
	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
		return ((MaskanMsg) protocolMessage).pack();
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor) throws NotMappedProtocolToIfxException {
    	MaskanMsg maskanMsg = (MaskanMsg) protocolMessage;
    	
//        MyDateFormat dateFormatyyyyMMDDhhmmss = new MyDateFormat("yyyy/MM/dd HH:mm:ss");
        /** **************** Map Epay Message to IFX **************** */

        Ifx ifx = new Ifx();

        ifx.setAppPAN( maskanMsg.depositNumber.trim() );
		
        ifx.setTrnType(TrnType.BALANCEINQUIRY);

        if(maskanMsg.type.equals("request")){
        	ifx.setIfxType(IfxType.BAL_INQ_RQ);
        }else if(maskanMsg.type.equals("response")){
        	ifx.setIfxType(IfxType.BAL_INQ_RS);
        }
        
        ifx.setAuth_Amt(0L);
        ifx.setReal_Amt(0L);
        ifx.setSec_Amt(0L);
              
        //TODO: AccTypeFrom and AccTypeTo must be defined in EPAY Protocol
        ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
        ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);

        ifx.setOrigDt(maskanMsg.date);
		ifx.setTrnDt(maskanMsg.date);
		ifx.setNetworkRefId(maskanMsg.ref2.trim());
		
		ifx.setSrc_TrnSeqCntr(maskanMsg.ref1.trim());
		ifx.setMy_TrnSeqCntr(maskanMsg.ref1.trim());	
		
        ifx.setTerminalType(TerminalType.POS);
        
        String myBin = ProcessContext.get().getMyInstitution().getBin().toString();
        
        ifx.setBankId(myBin);      
       	ifx.setFwdBankId(myBin);
	    ifx.setDestBankId(myBin);
        
        ifx.setTerminalId("444");
        ifx.setOrgIdNum("4444");        	
        
       	ifx.setPINBlock("0000");
       	
       	if(maskanMsg.result != null && !maskanMsg.result.equals("")){
	       	if(maskanMsg.result.equalsIgnoreCase("no result")){
	       		ifx.setRsCode("12");
	       	}else{
	       		ifx.setRsCode("00");
	       		ifx.setAcctBalAvailableAmt(maskanMsg.result);
	       	}
       	}
        
        return ifx;
	}

	@Override
	public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction)
		throws CantAddNecessaryDataToIfxException {
	}
	
	@Override
	public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException {
	}
	
	@Override
	public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
		return;
	}
	
	@Override
	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX, EncodingConvertor convertor) throws Exception {
		MaskanMsg msg = (MaskanMsg) incomingMessage;
		msg.result = "Error";
        return msg;		
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incoMessage) throws Exception {
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
