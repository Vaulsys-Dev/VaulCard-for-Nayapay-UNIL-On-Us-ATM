 /**/
 package vaulsys.protocols.ndc.base.TerminalToNetwork;

 import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.LastStatusIssued;
import vaulsys.protocols.ndc.constants.NDCMessageClassSolicitedUnSokicited;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.protocols.ndc.parsers.NDCConsumerRequestMapper;


 public class NDCConsumerRequestMsg extends NDCTerminalToNetworkMsg {
     public String timeVariantNumber;
     public char topOfFormPrint;
     public char messageCoordinationNumber;
     public String track2Data;
     public String track3Data;
     public String operationKeyBuffer;
     public String dollarAndCentsEntry;
     public String PINBuffer;
     public String generalBufferB;
     public String generalBufferC;
//     public byte track1Identifier;
     public char track1Identifier;
     public String track1Data;
//     public byte trxStatusIdentifier;
     public char trxStatusIdentifier;
     public String lastTrxSeqCounter;
//     public byte lastStatusIssue;
     public char lastStatusIssue;
     public String lastTrxNotesDispensed;
//     public byte CSPDataIdentifier;
     public char CSPDataIdentifier;
     public String CSPData;
//     public byte confirmCSPDataIndentifier;
     public char confirmCSPDataIndentifier;
     public String confirmCSPData;
     public String MAC;
     
     /*public String depositL4;
     public String depositL3;
     public String depositL2;
     
     public String moneyToRetainFlag;
     public char deviceIdentifierGraphic;
     public String LastCashInDepositTrxDirection;*/
     
     
     /*
      * ’0’ – Last TRX was not CashIn
’1’ – CashIn to safe direction
’2’ – Notes returned via FID (Rollback)
‘3’ – Notes returned in close state (Rollback)
‘4’ – Notes retracted after Rollback*/
     

     public NDCConsumerRequestMsg() {
         messageType = NDCMessageClassTerminalToNetwork.CONSUMER_REQUEST_OPERATIONAL_MESSAGE;
         solicited = NDCMessageClassSolicitedUnSokicited.UNSOLICITED_MESSAGE;
     }

     public Ifx toIfx() throws Exception {
         if ("".equals(operationKeyBuffer.trim())) {
             return NDCConsumerRequestMapper.copyIfx(this);
         } else {
             return NDCConsumerRequestMapper.toIfx(this);
         }
     }

     public NDCConsumerRequestMsg copy() {
    	 NDCConsumerRequestMsg msg = new NDCConsumerRequestMsg();
    	 msg.confirmCSPData = this.confirmCSPData;
    	 msg.confirmCSPDataIndentifier = this.confirmCSPDataIndentifier;
    	 msg.CSPData = this.CSPData;
    	 msg.CSPDataIdentifier = this.CSPDataIdentifier;
    	 msg.dollarAndCentsEntry = this.dollarAndCentsEntry;
    	 msg.generalBufferB = this.generalBufferB;
    	 msg.generalBufferC = this.generalBufferC;
    	 msg.lastStatusIssue = this.lastStatusIssue;
    	 msg.lastTrxNotesDispensed = this.lastTrxNotesDispensed;
    	 msg.lastTrxSeqCounter = this.lastTrxSeqCounter;
    	 msg.logicalUnitNumber = this.logicalUnitNumber;
    	 msg.MAC = this.MAC;
    	 msg.messageCoordinationNumber = this.messageCoordinationNumber;
    	 msg.messageType = this.messageType;
    	 msg.operationKeyBuffer = this.operationKeyBuffer;
    	 msg.PINBuffer = this.PINBuffer;
    	 msg.solicited = this.solicited;
    	 msg.timeVariantNumber = this.timeVariantNumber;
    	 msg.topOfFormPrint = this.topOfFormPrint;
    	 msg.track1Data = this.track1Data;
    	 msg.track2Data = this.track2Data;
    	 msg.track3Data = this.track3Data;
    	 msg.track1Identifier = this.track1Identifier;
    	 msg.trxStatusIdentifier = this.trxStatusIdentifier;
    	 msg.lastTrxSeqCounter = this.lastTrxSeqCounter;
    	 msg.lastTrxNotesDispensed = this.lastTrxNotesDispensed;
    	 /*msg.depositL4 = this.depositL4;
    	 msg.depositL3 = this.depositL3;
    	 msg.depositL2 = this.depositL2;
    	 msg.moneyToRetainFlag = this.moneyToRetainFlag;
    	 msg.deviceIdentifierGraphic = this.deviceIdentifierGraphic;
    	 msg.LastCashInDepositTrxDirection = this.LastCashInDepositTrxDirection;*/
//    	 msg.lastStatusIssue = this.lastStatusIssue;
    	 return msg;
     }
     
     public String toString() {
    	 return super.toString() 
    			 + "TVN:\t\t" + timeVariantNumber + "\r\n"
//               + "topOfFormPrint:\t\t" + topOfFormPrint + "\r\n"
                 + "msgCo:\t\t" + messageCoordinationNumber + "\r\n"
//                 + "track1:\t\t" + track1Data + "\r\n"
//                 + "track2:\t\t" + track2Data + "\r\n"
//                 + "track3:\t\t" + track3Data + "\r\n"
                 + "opKey:\t\t" + operationKeyBuffer + "\r\n"
                 + "amount:\t\t" + dollarAndCentsEntry + "\r\n"
//                 + "PIN:\t\t" + PINBuffer + "\r\n"
                 + "BufB:\t\t" + generalBufferB + "\r\n"
                 + "BufC:\t\t" + generalBufferC + "\r\n"
//                 + "track1Identifier:\t\t" + track1Identifier + "\r\n"
                 + "trxStatusIdentifier:\t\t" + trxStatusIdentifier + "\r\n"
                 + "lastTrxSeqCntr:\t\t" + lastTrxSeqCounter + "\r\n"
                 + "lastTrxNotesDispensed:\t\t" + lastTrxNotesDispensed + "\r\n"
                 + "lastStatusIssue:\t\t" + LastStatusIssued.get(lastStatusIssue).toString() + "\r\n"
//               + "CSPDataIdentifier:\t\t" + CSPDataIdentifier + "\r\n"
//               + "CSPData:\t\t" + CSPData + "\r\n"
//               + "confirmCSPDataIndentifier:\t\t" + confirmCSPDataIndentifier + "\r\n"
//               + "confirmCSPData\t\t" + confirmCSPData + "\r\n"
                 /*+ "depositL4\t\t" + depositL4 + "\r\n"
                 + "depositL3\t\t" + depositL3 + "\r\n"
                 + "depositL2\t\t" + depositL2 + "\r\n"
                 + "moneyToRetainFlag\t\t" + moneyToRetainFlag + "\r\n"
                 + "deviceIdentifierGraphic\t\t" +deviceIdentifierGraphic +"\r\n"
                 + "LastCashInDepositTrxDirection\t\t" + LastCashInDepositTrxDirection + "\r\n"*/
//                 + "MAC\t\t" + MAC + "\r\n"
                 ;
    	 
    }
	@Override
	public Boolean isRequest() {
		return true;
	}


 }
