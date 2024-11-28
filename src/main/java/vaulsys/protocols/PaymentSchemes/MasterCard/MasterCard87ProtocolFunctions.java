package vaulsys.protocols.PaymentSchemes.MasterCard;

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
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;

import org.apache.log4j.Logger;

public class MasterCard87ProtocolFunctions extends ISOFunctions {

    transient Logger logger = Logger.getLogger(MasterCard87ProtocolFunctions.class);

    @Override
    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction) throws CantAddNecessaryDataToIfxException {
        try {

        	logger.debug("addOutgoingNecessaryData...");
			Terminal endPointTerminal = transaction.getOutgoingIfxOrMessageEndpoint2();
//        	Terminal endPointTerminal = transaction.getOutgoingIfxOrMessageEndpoint();
//        	if (TerminalType.SWITCH.equals(endPointTerminal.getTerminalType())){
        		
        		if (TerminalService.isNeedToSetSettleDate(transaction)) {
        			ClearingDate wDate = ((Institution)endPointTerminal.getOwner()).getCurrentWorkingDay();
        			MonthDayDate daydate =  wDate == null? MonthDayDate.now() : new MonthDayDate(wDate.getDate());
        			outgoingIFX.setPostedDt(daydate);
    				outgoingIFX.setSettleDt(daydate);
    				
        		} else {
        			outgoingIFX.setPostedDt(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getPostedDt());
					outgoingIFX.setSettleDt(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getSettleDt());
        		}
//        	}
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
		return ((MasterCard87Protocol) ProtocolProvider
                .Instance.getByClass(MasterCard87Protocol.class))
                .getPackager();
	}

	@Override
	public IfxToProtocolMapper getIfxToProtocolMapper() {
		return MasterCard87IFXToISOMapper.Instance;
	}

	@Override
	public ProtocolToIfxMapper getProtocolToIfxMapper() {
		return MasterCard87ISOToIFXMapper.Instance;
	}

	@Override
	public ProtocolDialog getDialog() {
		return new MasterCard87ProtocolDialog();
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
