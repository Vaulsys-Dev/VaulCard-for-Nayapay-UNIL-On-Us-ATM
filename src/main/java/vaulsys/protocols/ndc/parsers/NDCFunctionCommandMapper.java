package vaulsys.protocols.ndc.parsers;

import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCFunctionCommandMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCConsumerRequestMsg;
import vaulsys.protocols.ndc.constants.NDCCardRetainFlagConstants;
import vaulsys.protocols.ndc.constants.NDCConstants;
import vaulsys.protocols.ndc.constants.ReceiptOptionType;
//import vaulsys.protocols.ndc.constants.NDCFunctionIdentifierConstants;
import vaulsys.protocols.ndc.constants.NDCPrinterFlag;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.protocols.ndc.encoding.NDCConvertor;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMProducer;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.atm.ATMResponse;
import vaulsys.terminal.atm.FunctionCommandResponse;
import vaulsys.terminal.atm.Receipt;
import vaulsys.terminal.atm.action.consumer.ConsumerStartingState;
import vaulsys.terminal.impl.ATMTerminal;
//import vaulsys.transaction.TransactionService;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class NDCFunctionCommandMapper {

    transient static Logger logger = Logger.getLogger(NDCFunctionCommandMapper.class);

    public static byte[] toBinary(NDCNetworkToTerminalMsg ndcMessage) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        StringFormat lunoFormat = new StringFormat(9, StringFormat.JUST_RIGHT); 
        NDCFunctionCommandMsg msg = (NDCFunctionCommandMsg) ndcMessage;

        out.write(NDCConstants.FUNCTION_COMMAND);
        out.write(ASCIIConstants.FS);
        out.write(StringFormat.formatNew(9, StringFormat.JUST_RIGHT, msg.getLogicalUnitNumber(), '0').getBytes());
        out.write(ASCIIConstants.FS);
        out.write(msg.messageSequenceNumber.getBytes());
        out.write(ASCIIConstants.FS);
        out.write(msg.nextStateID.getBytes());
        out.write(ASCIIConstants.FS);
        out.write(msg.numberOfBillsToDispense != null ? msg.numberOfBillsToDispense.getBytes() : "00000000".getBytes());
        out.write(ASCIIConstants.FS);
        out.write(msg.transactionSerialNumber.getBytes());
        out.write(msg.functionIdentifier.getCode());
        if (msg.screenNumber != null)
        	out.write(msg.screenNumber.getBytes());
        int counter = 0;
        if (msg.screenDisplayUpdateData != null) 
        		out.write(msg.screenDisplayUpdateData);

        out.write(ASCIIConstants.FS);
        out.write(msg.messageCoordinationNumber);
        
        out.write(msg.cardReturnRetainFlag.getCode());
        
        counter = 0;
        if (msg.printerData != null && msg.printerData.size() > 0){
            for (byte[] item : msg.printerData) {
                if (counter != 0)
                    out.write(ASCIIConstants.GS);

                if(item != null){
                	out.write(item);
                }
                counter++;
            }
//            out.write(ASCIIConstants.GS);
        }else{
        	out.write(NDCPrinterFlag.DONT_PRINT.getCode());
        }
        
        
//        if(msg.bufferData!=null){
 //       if(TransactionService.isDepositTransaction(ndcMessage.getLogicalUnitNumber())){
 //       	out.write(ASCIIConstants.FS);
  //      	if(msg.bufferData != null)
   //     		out.write(msg.bufferData.getBytes());
	//    }        
        out.write(ASCIIConstants.FS);
        msg.MAC = msg.MAC == null ? "01020304" : msg.MAC;
        out.write(msg.MAC.getBytes());

        return out.toByteArray();

        /*
           *
           *
           * ArrayList<String> strList = new ArrayList<String>(); // strList.add(msg.bufferData.getDocumentEnableCode()); // setGroupData(mainData,
           * strList, NDCBufferIdentefier.DOCUMENT_ENABLE_CODE); // strList.add(msg.bufferData.getTrack1data()); // setGroupData(mainData, strList,
           * NDCBufferIdentefier.TRACK1); // strList.add(msg.bufferData.getTrack2data()); // setGroupData(mainData, strList,
           * NDCBufferIdentefier.TRACK2); mainData.add(ASCIIConstants.FS);
           *
           * byte[] out = new byte[mainData.size()]; for (int i = 0; i < out.length; i++) out[i] = mainData.get(i);
           *
           * byte[] serial = msg.transactionSerialNumber.substring(2).getBytes(); byte[] printer = "FNP".getBytes();
           *
           * out = new byte[] { '4', ASCIIConstants.FS, '0', '0', '0', ASCIIConstants.FS, // LUNO '0', '0', '0', ASCIIConstants.FS, // Seq No '0', '0',
           * '0', ASCIIConstants.FS, // Next state '0', '2', ASCIIConstants.FS, // Number of bills to dispense serial[0], serial[1], serial[2],
           * serial[3], '2', ASCIIConstants.FS, // ASCIIConstants.FS, '7', // Coordination number '0', '3', printer[0], printer[1], printer[2],
           * ASCIIConstants.FS, ASCIIConstants.FS }; return out;
           *
           */
    }

    private static void setGroupData(ArrayList<Byte> mainData, ArrayList<String> data, byte flag) {
        // ArrayList<String> printerDataList = msg.printerData.getDontPrintList();
        String str = "";
        if (data == null)
            return;

        int lenght = data.size();
        int index = 0;
        if (data != null && lenght > 0) {
            str = data.get(index);
            setByteArray(mainData, str.getBytes());
            index++;
            while (index < lenght) {
                mainData.add(ASCIIConstants.GS);
                mainData.add(flag);
                setByteArray(mainData, data.get(index++).getBytes());
            }
        }
    }

    private static void setByteArray(ArrayList<Byte> mainData, byte[] partioalData) {
        for (int i = 0; i < partioalData.length; i++) {
            mainData.add(partioalData[i]);
        }
    }

    @SuppressWarnings("unused")
    private static void setByteArrayAndSeperator(ArrayList<Byte> mainData, byte[] partioalData, byte seperator) {
        if (partioalData != null && partioalData.length > 0) {
            for (int i = 0; i < partioalData.length; i++) {
                mainData.add(partioalData[i]);
            }
            mainData.add(seperator);
        }
    }

    public static NDCMsg fromIfx(Ifx ifx, EncodingConvertor convertor) throws Exception {
        NDCResponseMsg responseMsg = null;
        try {
        	
        	
        	String opkey = ifx.getOpkey();
        	if (ifx.getSecIfxType() != null && ISOFinalMessageType.isGetAccountMessage(ifx.getSecIfxType())) {
        		opkey = ifx.getNextOpkey();
        	}

        	int rsCode = Integer.parseInt(ifx.getRsCode());
        	
        	
        	if (IfxType.HOTCARD_REV_REPEAT_RS.equals(ifx.getIfxType())) {
        		rsCode = ATMErrorCodes.ATM_EJECT_CARD;
        		
        	} else if (IfxType.HOTCARD_INQ_RS.equals(ifx.getIfxType()) &&
        					!ISOResponseCodes.APPROVED.equals(ifx.getRsCode()) &&
        					!ISOResponseCodes.CARD_EXPIRED.equals(ifx.getRsCode()) &&
        					!ISOResponseCodes.INVALID_MERCHANT.equals(ifx.getRsCode()) &&
        					!ISOResponseCodes.HOST_LINK_DOWN.equals(ifx.getRsCode())
//        			&& TerminalType.ATM.equals(ifx.getTerminalType())
        			) {
        		rsCode = ATMErrorCodes.ATM_EJECT_CARD;
        	}

//        	ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, Long.valueOf(ifx.getTerminalId()));
        	/******** ONLY FOR TEST*/
//        	ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, ifx.getEndPointTerminal().getCode());
//        	ifx.setEndPointTerminal(atm);
        	/********/
        	ATMTerminal atm = null;
        	if(!(ifx.getEndPointTerminal() instanceof ATMTerminal)){
        		/**** in migration process, we set incorrect endpoint terminal for transfer
        		 * behtar ast khode terminalId ra load konim ****/
        		atm = GeneralDao.Instance.load(ATMTerminal.class, Long.parseLong(ifx.getTerminalId()));
//        		atm = GeneralDao.Instance.load(ATMTerminal.class, ifx.getEndPointTerminal().getCode());
        	}else{
        		atm = (ATMTerminal) ifx.getEndPointTerminal();
        	}
        	
//            ATMRequest atmRequest = ATMTerminalService.findATMRequest(atm, opkey);
            ATMRequest atmRequest = ProcessContext.get().getATMRequest(atm.getOwnOrParentConfigurationId(), opkey);
            ATMResponse atmResponse = null;
            
            /**** opkey undefined! ****/
            /*** Leila: agar opkey ersali az atm tarif nashode bashe, request null-e va fit ham nadarim.
             * az anjaii ke response ham be ezaye fit be dast miad majboor shodam fit = 1 hardcode konam!
             */
            if (atmRequest == null)
//            	atmResponse = atm.getOwnOrParentConfiguration().getResponse(1, rsCode);
            	atmResponse = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getResponse(1, rsCode);
            else 
            	atmResponse = atmRequest.getAtmResponse(rsCode);
            
			if (atmResponse == null && atmRequest != null) {
//                atmResponse = atm.getOwnOrParentConfiguration().getResponse(atmRequest.getFit(), rsCode);
                atmResponse = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getResponse(atmRequest.getFit(), rsCode);
            }else if (atmResponse == null && atmRequest == null) {
            	logger.error("atmResponse == null && atmRequest == null");
            	return null;
            }

            responseMsg = ATMTerminalService.fromIfx(ifx, atmResponse, atm, convertor);


//	if (ifx.getIfxType().equals(IfxType.SHEBA_INQ_RS) && Util.hasText(ifx.getBufferC()) && NDCConstants.BAL_TYPE_SHOW.equals(Long.parseLong(ifx.getBufferC()))) // B Key Pressed (Mode = Namayesh)
//    		{
            
            if (ifx.getReceiptOption() != null && ReceiptOptionType.WITHOUT_RECEIPT.equals(ifx.getReceiptOption())) {
    			if (responseMsg instanceof NDCFunctionCommandMsg)
    				((NDCFunctionCommandMsg)(responseMsg)).printerData = new ArrayList<byte[]>();
    			if (atmResponse instanceof FunctionCommandResponse)
    			{
    				for (Receipt r : ((FunctionCommandResponse)atmResponse).getReceipt(ifx.getUserLanguage()))  // SwitchApplication.get().getAtmTerminalService().getReceipt(atmResponse.getId())){
    				{
    						if (r.getPrinterFlag().equals(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY))
    						{
    							((NDCFunctionCommandMsg)(responseMsg)).printerData.add(((NDCConvertor)convertor).convert(r.getPrinterFlag(), r.getReplacedText(), ifx));
    						}
    				}
    			}
    			logger.info("Empty printer data if Sheba mode is namayesh (B Key Pressed)");
    		}

        } catch (Exception e) {
            throw e;
        }
        return responseMsg;
    }

    public static NDCMsg fromProtocol(NDCConsumerRequestMsg consumerMsg, int rsCode, NDCConvertor convertor) {
        NDCFunctionCommandMsg functionCommandMsg = null;
        try {
        	ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, consumerMsg.getLogicalUnitNumber());
//            ATMRequest atmRequest = ATMTerminalService.findATMRequest(atm, consumerMsg.operationKeyBuffer);
        	ATMRequest atmRequest = ProcessContext.get().getATMRequest(atm.getOwnOrParentConfigurationId(), consumerMsg.operationKeyBuffer);
            FunctionCommandResponse atmRs = atmRequest.getAtmResponse(rsCode);
            if (atmRs == null) {
//            	atmRs = (FunctionCommandResponse) atm.getOwnOrParentConfiguration().getResponse(atmRequest.getFit(), rsCode);
            	atmRs = (FunctionCommandResponse) ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getResponse(atmRequest.getFit(), rsCode);
            }
            
//            //TODO this line for default response code
//            if (atmRs == null)
//            	atmRs = atmRequest.getAtmResponse(ATMErrorCodes.DEFAULT_RESPONSE_CODE);

            functionCommandMsg = fromATMResponse(atm, atmRs, convertor);
            functionCommandMsg.messageCoordinationNumber = consumerMsg.messageCoordinationNumber;
            functionCommandMsg.MAC = null;
        } catch (Exception e) {
            logger.error(e);
        }
        return functionCommandMsg;

    }

    public static NDCFunctionCommandMsg fromATMResponse(ATMTerminal atm, FunctionCommandResponse atmRs, NDCConvertor convertor) {
        NDCFunctionCommandMsg functionCommandMsg = new NDCFunctionCommandMsg();

        try {

//            Ifx ifx = atm.getLastTransaction().getFirstTransaction().getInputMessage().getIfx();
            Ifx ifx = atm.getLastTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;
            functionCommandMsg.nextStateID = atmRs.getNextState();
            functionCommandMsg.screenNumber = atmRs.getScreen(ifx.getUserLanguage()).getScreenno();
            functionCommandMsg.cardReturnRetainFlag = NDCCardRetainFlagConstants.getByBoolean(atmRs.getCradRetain());
            
            //TODO: double check this line, change from getByteOfReciept to ndcconvertor!
            functionCommandMsg.screenDisplayUpdateData = convertor.convert(null, atmRs.getScreen(ifx.getUserLanguage()).getScreenData(), ifx);
//            functionCommandMsg.screenDisplayUpdateData = NDCParserUtils.getByteOfReciept(null, atmRs.getScreenData(), ifx);
            
            functionCommandMsg.numberOfBillsToDispense = atmRs.getDispense() != null ? atmRs.getDispense().getAllCassettesAsByte() : "00000000";
            if (functionCommandMsg.printerData == null) {
                functionCommandMsg.printerData = new ArrayList<byte[]>();
            }
            for (Receipt r : atmRs.getReceipt(ifx.getUserLanguage())) { //SwitchApplication.get().getAtmTerminalService().getReceipt(atmRs.getId())){ 
            	if(atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR) && NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY.equals(r.getPrinterFlag())){
            		ByteArrayOutputStream out = new ByteArrayOutputStream();
            		out.write("2".getBytes());
            		out.write(ASCIIConstants.ESC);
            		out.write(0x25);
            		out.write("000".getBytes());

            		out.write(convertor.convert(r.getPrinterFlag(), r.getReplacedText()/*r.getText()*/, ifx));
            		functionCommandMsg.printerData.add(out.toByteArray());            		
            	}else{            		
	    			functionCommandMsg.printerData.add(convertor.convert(r.getPrinterFlag(), r.getReplacedText()/*r.getText()*/, ifx));
	//                functionCommandMsg.printerData.add(NDCParserUtils.getByteOfReciept(r.getPrinterFlag(), r.getText()));
            	}
            }
            functionCommandMsg.functionIdentifier = atmRs.getFunctionCommand();

            functionCommandMsg.logicalUnitNumber = atm.getCode();
            atm.setCurrentAbstractStateClass(ConsumerStartingState.Instance);

            functionCommandMsg.messageSequenceNumber = ATMTerminalService.networkRefIdToTimeVariant(ifx.getNetworkRefId());
            functionCommandMsg.transactionSerialNumber = Util.generateTrnSeqCntr(4); //format.format("" + SwitchApplication.get().nextAtmTrxSeqNo(), '0');


            functionCommandMsg.messageCoordinationNumber = ifx.getCoordinationNumber();

            GeneralDao.Instance.saveOrUpdate(atm);
            functionCommandMsg.MAC = null;
        } catch (Exception e) {
            logger.error(e);
        }
        return functionCommandMsg;
    }
}
