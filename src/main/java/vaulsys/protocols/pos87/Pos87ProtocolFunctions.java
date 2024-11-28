package vaulsys.protocols.pos87;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.ISO8583.utils.ISOMACUtils;
import vaulsys.protocols.pos87.encoding.BankBinToFarsi;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class Pos87ProtocolFunctions extends ISOFunctions {

    transient Logger logger = Logger.getLogger(Pos87ProtocolFunctions.class);

    @Override
    public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction)
            throws CantAddNecessaryDataToIfxException {
        try {
            
        	if (transaction.getInputMessage().isScheduleMessage()){
        		transaction = transaction.getReferenceTransaction();
        	}

			//m.rehman: separating BI from financial incase of limit
            //if (ISOFinalMessageType.isFinancialMessage(outgoingIFX.getIfxType())) {
			if (ISOFinalMessageType.isFinancialMessage(outgoingIFX.getIfxType(),false)) {
                // if(outgoingIFX.IfxType.equals(IfxTypeEnum.ReturnRs))
                // outgoingIFX.MsgRqHdr.EMVRqData.Auth_Amt = "0";
                try {
                	if (transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getOrgIdNum() != null && 
                			!"".equals(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getOrgIdNum())) {
                        outgoingIFX.setOrgIdNum( transaction.getInputMessage()
                        .getIfx().getOrgIdNum());
                	}else
                    outgoingIFX.setOrgIdNum (transaction.getFirstTransaction().getInputMessage()
                            .getIfx().getOrgIdNum());
                } catch (Exception e) {
                    outgoingIFX.setOrgIdNum(ISOUtil.zeroUnPad(((ISOMsg) transaction.getFirstTransaction()
							.getInputMessage().getProtocolMessage()).getString(42)));
                }

                if (outgoingIFX.getSettleDt() == null)
                    outgoingIFX.setSettleDt( MonthDayDate.now());

                if (outgoingIFX.getTrnDt() == null)
                    outgoingIFX.setTrnDt ( DateTime.now());

//                outgoingIFX.setOriginalDataElements ( null);

                //TODO this condition seems unnecessary due to ErrorMapper in Mappers
//                if (ShetabFinalMessageType.isReversalMessage(outgoingIFX.getIfxType())
//                        && !HasinErrorCodes.understandableForHasinPos(outgoingIFX.getRsCode())) {
//                    outgoingIFX.setRsCode ( "00");
//                }
            } else {
            }

//			outgoingIFX.OrigDt = currentSystemDate;

            // Terminal t = new TerminalManager().get(outgoingIFX.TerminalId);
            // outgoingIFX.Name = new String(FarsiConvertor.convert(t.getParentMerchant().getContact().getName()));
            // outgoingIFX.Country = new String(FarsiConvertor.convert(t.getParentMerchant().getContact().getCountry()));
            // outgoingIFX.City = new String(FarsiConvertor.convert(t.getParentMerchant().getContact().getCity()));
            // outgoingIFX.StateProv = new String(FarsiConvertor.convert(t.getParentMerchant().getContact().getStateProv()));

        } catch (Exception e) {
            if (true)
                throw new CantAddNecessaryDataToIfxException(e);
        }
    }

    @Override
    public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction)
            throws CantAddNecessaryDataToIfxException {
        try {
            // 6, 7, 10, 14, 17, 37, 51
            // TODO: What about international cards? exchange rates?
            incomingIFX.setSec_Amt ( incomingIFX.getAuth_Amt());
            incomingIFX.setSec_CurRate ( "1");
            incomingIFX.setAuth_CurRate ( "1");
//            incomingIFX.setSec_Currency ( GlobalContext.getInstance().getRialCurrency().getCode());
            incomingIFX.setSec_Currency ( ProcessContext.get().getRialCurrency().getCode());
//            incomingIFX.setAuth_Currency ( GlobalContext.getInstance().getRialCurrency().getCode());
            incomingIFX.setAuth_Currency ( ProcessContext.get().getRialCurrency().getCode());

            Calendar c1 = Calendar.getInstance();
            c1.setTime(new Date());
            // c1.add(Calendar.DATE, 1);

            incomingIFX.setTrnDt ( new DateTime(c1.getTime()));
            // incomingIFX.TrnSeqCntr = new Integer(Integer.parseInt(incomingIFX.TrnSeqCntr) +
            // 666000).toString();

            // 7
            // 10
            // 14 Exp Date
            // 15
            // 17

        } catch (Exception e) {
            throw new CantAddNecessaryDataToIfxException(e);
        }
    }

    @Override
    public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException {
        try {
        	Channel channel = outgoingMessage.getChannel();
			 ProtocolSecurityFunctions securityFunctions = channel.getProtocol().getSecurityFunctions();
			 Terminal t = outgoingMessage.getEndPointTerminal();
			 
			 if( t != null )
				 securityFunctions.setMac(processContext, t, t.getOwnOrParentSecurityProfileId(), t.getKeySet(), outgoingMessage, channel.getMacEnable());
			 
			 ISOMACUtils.findProfilesAndSetMac(outgoingMessage);

        } catch (Exception e) {
            throw new CantPostProcessBinaryDataException(e);
        }
    }

	@Override
	public ISOPackager getPackager() {
		return ((Pos87Protocol) ProtocolProvider.Instance.getByClass(Pos87Protocol.class))
        .getPackager();
	}

	@Override
	public IfxToProtocolMapper getIfxToProtocolMapper() {
		return Pos87IFXToISOMapper.Instance;
	}

	@Override
	public ProtocolToIfxMapper getProtocolToIfxMapper() {
		return Pos87ISOToIFXMapper.Instance;
	}

	@Override
	public ProtocolDialog getDialog() {
		return new Pos87ProtocolDialog();
	}

	@Override
	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX, EncodingConvertor convertor) throws Exception {
		ISOMsg incomingIsoMsg = (ISOMsg) incomingMessage; 
		ISOMsg outIsoMsg = incomingIsoMsg;
		Integer mti = new Integer(incomingIsoMsg.getMTI());
		mti += 10;
		outIsoMsg.setMTI("0"+ mti.toString()); 
		
		// response message has invalid mti!
		if (mti.toString().charAt(1)!= '1' && mti.toString().charAt(1)!= '3' )
			return null;
		Date currentSystemDate = Calendar.getInstance().getTime();
		
//		MyDateFormat dateFormathhmmss = new MyDateFormat("HHmmss");
//		MyDateFormat dateFormatMMDD = new MyDateFormat("MMdd");
//		MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
		
		outIsoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", currentSystemDate));
		outIsoMsg.set(12, MyDateFormatNew.format("HHmmss", currentSystemDate));
		outIsoMsg.set(13, MyDateFormatNew.format("MMdd", currentSystemDate));
		outIsoMsg.set(15, MyDateFormatNew.format("MMdd", currentSystemDate));
		
		String refN = outIsoMsg.getString(37);
		outIsoMsg.set(37, Util.hasText(refN) ? refN : outIsoMsg.getString(11));
		
		if (mti.equals(430) || mti.equals(510))
			outIsoMsg.set(39, ISOResponseCodes.APPROVED);
		else
			outIsoMsg.set(39, ISOResponseCodes.INVALID_CARD_STATUS);
		
		
		POSTerminal t = TerminalService.findTerminal(POSTerminal.class, Long.valueOf((incomingIsoMsg.getString(41).trim()))); 
		
		String str48 = "";
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
        if (t != null) {
        	
        	
			byte[] percent = getHasinFarsiConvertor().encode("%");
			
//			MyDateFormat dateFormatYY = new MyDateFormat("yy");
			String currentTime = MyDateFormatNew.format("yy", currentSystemDate);
			FinancialEntity m = t.getOwner();

//			Contact contact = t.getOwner().getContact();

			finalBytes.write((currentTime+"00000000").getBytes());
			finalBytes.write(getHasinFarsiConvertor().encode(t.getOwner().getName()));
			finalBytes.write(percent);

			String dailyMessage = t.getDailyMessage();
			if (dailyMessage != null) {
				finalBytes.write(getHasinFarsiConvertor().encode(dailyMessage));
			} else {
				finalBytes.write(getHasinFarsiConvertor().encode("روز خوشی داشته باشید"));
			}

			finalBytes.write(percent);
			finalBytes.write(getHasinFarsiConvertor().encode(t.getOwner().getSafeAddress()));

			String phoneNum = t.getOwner().getSafeFullPhoneNumber(); 
//				contact.getPhonenumber().getNumber();
			String inversePhoneNum = "";
			for (int i = phoneNum.length() - 1; i >= 0; i--)
				inversePhoneNum += phoneNum.charAt(i);

			finalBytes.write(percent);
			finalBytes.write(getHasinFarsiConvertor().encode(phoneNum));
			finalBytes.write(percent);
			String website = t.getOwner().getSafeWebsiteAddress();
			if (website!= null && website.length()>0){
				finalBytes.write(website.getBytes());
			}
			finalBytes.write(percent);
			if (Util.hasText(t.getDescription())){
				finalBytes.write(getHasinFarsiConvertor().encode(t.getDescription()));
			}
			finalBytes.write(percent);
			finalBytes.write("www.bpi.ir".getBytes());
			finalBytes.write(percent);
			Long bin = Long.MAX_VALUE;
			try{
				bin = Long.valueOf((incomingIsoMsg.getString(33).trim()));
			}catch(NumberFormatException e){
			}
			finalBytes.write(BankBinToFarsi.bankName(bin));
			
			String refundDate = "000000000000";
			finalBytes.write(refundDate.getBytes());
		}

		outIsoMsg.set(48, /*str48*/ finalBytes.toByteArray()); 
				outIsoMsg.set(51, outIsoMsg.getString(49)); 

		return outIsoMsg;
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incoMessage) throws CantPostProcessBinaryDataException {
		return incoMessage.getBinaryData();
	}

	private EncodingConvertor getHasinFarsiConvertor(){
//		return GlobalContext.getInstance().getConvertor("HASIN_CONVERTOR");
		return ProcessContext.get().getConvertor("HASIN_CONVERTOR");
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
