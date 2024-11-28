package vaulsys.protocols.iso8583v93;

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
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class ISO8583v93ProtocolFunctions extends ISOFunctions {

    transient Logger logger = Logger.getLogger(ISO8583v93ProtocolFunctions.class);

    @Override
    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
    	 try {
//          Terminal endPointTerminal = transaction.getOutgoingIfxOrMessageEndpoint();
          Terminal endPointTerminal = ProcessContext.get().getSwitchTerminal(transaction.getOutputMessage().getEndPointTerminalId());
        	if (endPointTerminal instanceof SwitchTerminal){
        		ClearingDate wDate = ((Institution)endPointTerminal.getOwner()).getCurrentWorkingDay();
//        			getFinancialEntityService().getLastValidWorkingDay((Institution) endPointTerminal.getOwner());
        		MonthDayDate daydate =  wDate == null? MonthDayDate.now() : new MonthDayDate(wDate.getDate());
        		outgoingIFX.setSettleDt(daydate);
        		outgoingIFX.setPostedDt(daydate);
        	}
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
		return ((ISO8583v93Protocol) ProtocolProvider.Instance
                .getByClass(ISO8583v93Protocol.class)).getPackager();
	}

	@Override
	public IfxToProtocolMapper getIfxToProtocolMapper() {
		return ISO8583v93IFXToISOMapper.Instance;
	}

	@Override
	public ProtocolToIfxMapper getProtocolToIfxMapper() {
		return ISO8583v93ISOToIFXMapper.Instance;
	}

	@Override
	public ProtocolDialog getDialog() {
		return new ISO8583v93ProtocolDialog();
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
