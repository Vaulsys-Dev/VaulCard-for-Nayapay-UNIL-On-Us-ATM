package vaulsys.protocols.saderat87;

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
import vaulsys.protocols.saderat87.Saderat87IFXToISOMapper;
import vaulsys.protocols.saderat87.Saderat87ISOToIFXMapper;
import vaulsys.protocols.saderat87.Saderat87Protocol;
import vaulsys.protocols.saderat87.Saderat87ProtocolDialog;
import vaulsys.protocols.saderat87.Saderat87ProtocolFunctions;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;

import org.apache.log4j.Logger;


public class Saderat87ProtocolFunctions extends ISOFunctions {
	 transient Logger logger = Logger.getLogger(Saderat87ProtocolFunctions.class);

	    @Override
	    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
	        try {

	        	Terminal endPointTerminal = transaction.getOutgoingIfxOrMessageEndpoint();
//	        	if (TerminalType.SWITCH.equals(endPointTerminal.getTerminalType())){
	        		
	        		if (TerminalService.isNeedToSetSettleDate(transaction)) {
	        			ClearingDate wDate = ((Institution)endPointTerminal.getOwner()).getCurrentWorkingDay();
	        			MonthDayDate daydate =  wDate == null? MonthDayDate.now() : new MonthDayDate(wDate.getDate());
	        			outgoingIFX.setPostedDt(daydate);
	    				outgoingIFX.setSettleDt(daydate);
	    				
	        		} else {
	        			outgoingIFX.setPostedDt(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getPostedDt());
						outgoingIFX.setSettleDt(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getSettleDt());
	        		}
//	        	}
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
			return ((Saderat87Protocol) ProtocolProvider
	                .Instance.getByClass(Saderat87Protocol.class))
	                .getPackager();
		}

		@Override
		public IfxToProtocolMapper getIfxToProtocolMapper() {
			return Saderat87IFXToISOMapper.Instance; 
		}

		@Override
		public ProtocolToIfxMapper getProtocolToIfxMapper() {
			return Saderat87ISOToIFXMapper.Instance;
		}

		@Override
		public ProtocolDialog getDialog() {
			return new Saderat87ProtocolDialog();
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
		public byte[] encryptBinaryMessage(byte[] rawdata,
				Message incomingMessage) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}


}
