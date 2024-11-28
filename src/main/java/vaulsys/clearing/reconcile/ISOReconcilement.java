package vaulsys.clearing.reconcile;

import vaulsys.calendar.DayDate;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.TransactionService;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

public class ISOReconcilement extends AbstractReconcilement {

	public static final ISOReconcilement Instance = new ISOReconcilement();
	
	protected ISOReconcilement(){
		setResponseDataProcessor(ISOResponseDataProcessor.Instance);
		setRequestDataProcessor(ISORequestDataProcessor.Instance);
	}
	
    static Logger logger = Logger.getLogger(ISOReconcilement.class);

    public ProtocolMessage buildRequest(Terminal terminal) throws Exception {
        Date currentSystemDate = Calendar.getInstance().getTime();

        Institution institution = (Institution) terminal.getOwner();
        MonthDayDate lastWorkingDay = FinancialEntityService.getLastWorkingDay(institution).getDate();
        DayDate stlDate = new DayDate(lastWorkingDay.getYear(), lastWorkingDay.getMonth(), lastWorkingDay.getDay());
		Map<Integer, String> map = getRequestDataProcessor().process(null, terminal, stlDate);

		ISOMsg isoMsg = new ISOMsg();
		isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", currentSystemDate));
		isoMsg.set(11, Util.generateTrnSeqCntr(6));
		isoMsg.set(15, MyDateFormatNew.format("MMdd", stlDate));
		isoMsg.set(17, MyDateFormatNew.format("MMdd", stlDate));
		Institution myInstitution = ProcessContext.get().getMyInstitution();
		Long myBin = myInstitution.getBin();
		if (TerminalClearingMode.ISSUER.equals(terminal.getClearingMode())) {
			isoMsg.setMTI(String.valueOf(ISOMessageTypes.ISSUER_RECON_REQUEST_87));
			isoMsg.set(32, myBin);
			isoMsg.set(33, institution.getBin());
		} else if (TerminalClearingMode.ACQUIER.equals(terminal.getClearingMode())) {
			isoMsg.setMTI(String.valueOf(ISOMessageTypes.ACQUIRER_RECON_REQUEST_87));
			isoMsg.set(32, institution.getBin());
			isoMsg.set(33, myBin);
		}

		isoMsg.set(74, map.get(74));
		isoMsg.set(75, map.get(75));
		isoMsg.set(76, map.get(76));
		isoMsg.set(77, map.get(77));
		isoMsg.set(78, map.get(78));
		isoMsg.set(79, map.get(79));
		isoMsg.set(80, map.get(80));
		isoMsg.set(81, map.get(81));
		isoMsg.set(82, map.get(82));
		isoMsg.set(83, map.get(83));
		isoMsg.set(84, map.get(84));
		isoMsg.set(85, map.get(85));
		isoMsg.set(86, map.get(86));
		isoMsg.set(87, map.get(87));
		isoMsg.set(88, map.get(88));
		isoMsg.set(89, map.get(89));
		Long total = Util.longValueOf(map.get(97));
		String type = (total > 0) ? "C" : "D";
		total *= (total<0)? -1 : 1;
		isoMsg.set(97, type+ total);
		Long masterBin = FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole()) ? FinancialEntityService.getMasterInstitution().getBin() : myBin;
		isoMsg.set(99, masterBin);
    	return isoMsg;
    }

    public ProtocolMessage buildResponse(ProtocolMessage message,Ifx ifx, Terminal terminal, ProcessContext processContext) throws Exception {
        ISOMsg incomingMsg = (ISOMsg) message;
        ISOMsg outMsg = new ISOMsg();

    	int mti = Integer.parseInt(incomingMsg.getMTI());

        String responseMTI = generateResponseMTI(mti);
        outMsg.setMTI(responseMTI);

        Date str2Date = MyDateFormatNew.parse("MMdd", incomingMsg.getString(15));
        MonthDayDate stlDate = new MonthDayDate(str2Date);
        
        //this block just for decreasing process
        //if received message is "repeat" don't any processing!
        logger.debug("process build response!!");
        if (!isRepeatMTI(outMsg.getMTI())) {        	
			logger.debug("TERMINAL: "+ terminal.getCode());
			
			Map<Integer, String> processedData = getResponseDataProcessor().process(incomingMsg, terminal, stlDate);
			logger.debug("not repeat mti!");
			
			outMsg.set(66, processedData.get(66));
		} else {
			logger.debug("repeat mti!");
			
			IfxType ifxType = ifx.getIfxType();
			if (ISOFinalMessageType.isRequestMessage(ifxType))
				ifxType = IfxType.getResponseIfxType(ifxType);
			
			String rsCode = "2";
			
			try {
				rsCode = TransactionService.findResponseOfFirstTransactionType(processContext.getTransaction().getLifeCycleId(), ifxType);
			} catch (Exception e) {
			}
			if (rsCode == null || rsCode.isEmpty())
				rsCode = "2";
			
			outMsg.set(66, rsCode);
		}
        
        String P7 = MyDateFormatNew.format("MMddHHmmss", Calendar.getInstance().getTime());
        outMsg.set(7, P7);
        outMsg.set(11, ISOUtil.zeroUnPad(incomingMsg.getString(11)));
        outMsg.set(15, "0000");
        outMsg.set(50, incomingMsg.getString(50));
        outMsg.set(99, incomingMsg.getString(99));
        outMsg.set(128, "0000000000000000");
        return outMsg;
    }

    private String generateResponseMTI(int mti) {
        String responseMTI = "0";
        /*switch (mti) { //Raza commenting now working on String
            case ISOMessageTypes.ISSUER_RECON_REQUEST_87:
                responseMTI += ISOMessageTypes.ISSUER_RECON_RESPONSE_87;
                break;
            case ISOMessageTypes.ISSUER_RECON_ADVICE_87:
                responseMTI += ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87;
                break;
            case ISOMessageTypes.ACQUIRER_RECON_REQUEST_87:
                responseMTI += ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87;
                break;
            case ISOMessageTypes.ACQUIRER_RECON_ADVICE_87:
                responseMTI += ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87;
                break;
        }*/

		if(mti == Integer.parseInt(ISOMessageTypes.ISSUER_RECON_REQUEST_87))
		{
			responseMTI += ISOMessageTypes.ISSUER_RECON_RESPONSE_87;
		}
		else if(mti == Integer.parseInt(ISOMessageTypes.ISSUER_RECON_ADVICE_87))
		{
			responseMTI += ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87;
		}
		else if(mti == Integer.parseInt(ISOMessageTypes.ACQUIRER_RECON_REQUEST_87))
		{
			responseMTI += ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87;
		}
		else if(mti == Integer.parseInt(ISOMessageTypes.ACQUIRER_RECON_ADVICE_87))
		{
			responseMTI += ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87;
		}

        return responseMTI;
    }

    private boolean isRepeatMTI(String mti) {
    	if (mti.contains(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87) ||
    			mti.contains(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87))
    		return true;
    	else return false;
    }
}
