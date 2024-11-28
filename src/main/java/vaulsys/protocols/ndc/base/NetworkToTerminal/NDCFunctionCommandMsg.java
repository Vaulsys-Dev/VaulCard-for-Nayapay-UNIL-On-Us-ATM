package vaulsys.protocols.ndc.base.NetworkToTerminal;

import vaulsys.protocols.ndc.constants.NDCBufferData;
import vaulsys.protocols.ndc.constants.NDCCardRetainFlagConstants;
import vaulsys.protocols.ndc.constants.NDCFunctionIdentifierConstants;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.protocols.ndc.parsers.NDCFunctionCommandMapper;

import java.io.IOException;
import java.util.List;

public class NDCFunctionCommandMsg extends NDCResponseMsg {

    public byte responseFlag;
    public String messageSequenceNumber;
    public String nextStateID;
    public String numberOfBillsToDispense;
    public String numberOfCoinsToDispose;
    public String transactionSerialNumber;
    public NDCFunctionIdentifierConstants functionIdentifier;
    public String screenNumber;
    public byte[] screenDisplayUpdateData;
    public char messageCoordinationNumber;
    public NDCCardRetainFlagConstants cardReturnRetainFlag;

    public List<byte[]> printerData;

//	public char printerFlag2;
//	public String printerData2;
//
//	public char printerFlag3;
//	public String printerData3;
//
//	public char printerFlag4;
//	public String printerData4;

    public NDCBufferData bufferData;
//	public NDCBufferIdentefier bufferIdentifierList;
//	public char bufferIdentifier;
//	public String track3DataIdentifier;

    //	public String bufferIdentitier;
    //	public String documentDestination;
    public String liftFrontReadImages;
    public String archiveLiftedFrontReadImages;
    public String checkMagnetism;
    public String checkZoneVerification;
    public String reportCandidatesIdentify;
    public String documentEntryRetries;
    //	public String bufferIdentitier2;
    //	public String documentEnableCode;
    public String documentName;

    //	public String bufferIdentitier3;
    //	public String track1Data;
    //	public String track2Data;



    public byte[] toBinary() throws IOException {
        return NDCFunctionCommandMapper.toBinary(this);
    }

    public NDCFunctionCommandMsg() {
        messageType = NDCMessageClassTerminalToNetwork.FUNCTION_COMMAND;
    }

    @Override
    public String toString() {
    	StringBuilder st = new StringBuilder();
    	st.append(super.toString() 
//    			+ "responseFlag:\t\t" + responseFlag + "\r\n"
                + "seq:\t\t" + messageSequenceNumber + "\r\n"
                + "nextState:\t\t" + nextStateID + "\r\n"
                + "bills:\t\t" + numberOfBillsToDispense + "\r\n"
//                + "numOfCoinsDispose:\t\t" + numberOfCoinsToDispose + "\r\n"
                + "serial:\t\t" + transactionSerialNumber + "\r\n"
                + "func:\t\t" + functionIdentifier + "\r\n"
                + "screen:\t\t" + screenNumber + "\r\n");
    	
    	if(screenDisplayUpdateData != null)
    		st.append("screenUpdate:\t\t" + new String(screenDisplayUpdateData) + "\r\n");
//        else
//        	st.append("screenDisplayUpdateData:\t\t" + "-" + "\r\n");
    	st.append("msgCo:\t\t" + messageCoordinationNumber + "\r\n"
                + "cardFlg:\t\t" + cardReturnRetainFlag.toString() + "\r\n"
//                + "printerData:\t\t" + ToStringBuilder.reflectionToString(printerData, ToStringStyle.SHORT_PREFIX_STYLE) + "\r\n"
//                + "bufferData:\t\t" + bufferData + "\r\n"
//                + "commandModifier\t\t" + commandModifier + "\r\n"
//                + "liftFrontReadImages:\t\t" + liftFrontReadImages + "\r\n"
//              + "archiveLiftedFrontReadImages:\t\t" + archiveLiftedFrontReadImages + "\r\n"
//              + "checkMagnetism:\t\t" + checkMagnetism + "\r\n"
//              + "checkZoneVerification\t\t" + checkZoneVerification + "\r\n"
//              + "reportCandidatesIdentify:\t\t" + reportCandidatesIdentify + "\r\n"
//              + "documentEntryRetries:\t\t" + documentEntryRetries + "\r\n"
//              + "documentName\t\t" + documentName + "\r\n"
//              + "MAC\t\t" + MAC + "\r\n";
    	);
//        if(bufferIdentifier != null)
//        		st.append("bufferIdentifier:\t\t"+bufferIdentifier+"\r\n");
        
        return st.toString();
    }

}
