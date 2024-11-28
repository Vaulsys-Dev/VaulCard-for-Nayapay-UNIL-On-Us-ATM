package vaulsys.protocols.infotech;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
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
import vaulsys.security.component.SecurityComponent;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class InfotechProtocolFunctions extends ISOFunctions {

    transient Logger logger = Logger.getLogger(InfotechProtocolFunctions.class);

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
                try {
                	if (transaction.getIncomingIfx().getOrgIdNum() != null && 
                			!"".equals(transaction.getIncomingIfx().getOrgIdNum())) {
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
            } else {
            }
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
            incomingIFX.setSec_Amt(incomingIFX.getAuth_Amt());
            incomingIFX.setSec_CurRate("1");
            incomingIFX.setAuth_CurRate("1");
            incomingIFX.setSec_Currency(ProcessContext.get().getRialCurrency().getCode());
            incomingIFX.setAuth_Currency(ProcessContext.get().getRialCurrency().getCode());

            Calendar c1 = Calendar.getInstance();
            c1.setTime(new Date());
            incomingIFX.setTrnDt(new DateTime(c1.getTime()));
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
		return ((InfotechProtocol) ProtocolProvider.Instance.getByClass(InfotechProtocol.class)).getPackager();
	}

	@Override
	public IfxToProtocolMapper getIfxToProtocolMapper() {
		return InfotechIFXToISOMapper.Instance;
	}

	@Override
	public ProtocolToIfxMapper getProtocolToIfxMapper() {
		return InfotechISOToIFXMapper.Instance;
	}

	@Override
	public ProtocolDialog getDialog() {
		return new InfotechProtocolDialog();
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
		
		outIsoMsg.set(7, MyDateFormatNew.format("yyyyMMddHHmmss", currentSystemDate));
		outIsoMsg.set(12, MyDateFormatNew.format("HHmmss", currentSystemDate));
		outIsoMsg.set(13, MyDateFormatNew.format("yyyyMMdd", currentSystemDate));
		outIsoMsg.set(15, MyDateFormatNew.format("MMdd", currentSystemDate));
		
		String refN = outIsoMsg.getString(37);
		outIsoMsg.set(37, Util.hasText(refN) ? refN : outIsoMsg.getString(11));
		
		if (mti.equals(430) || mti.equals(510))
			outIsoMsg.set(39, ISOResponseCodes.APPROVED);
		else
			outIsoMsg.set(39, ISOResponseCodes.INVALID_CARD_STATUS);
		
		
		POSTerminal t = TerminalService.findTerminal(POSTerminal.class, Long.valueOf((incomingIsoMsg.getString(41).trim()))); 
		
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
        if (t != null) {        	
			String refundDate = "000000000000";
			finalBytes.write(refundDate.getBytes());
		}

        outIsoMsg.set(41, incomingIFX.getTerminalId());
        outIsoMsg.set(42, incomingIFX.getOrgIdNum());
        
		outIsoMsg.set(48, finalBytes.toByteArray()); 
		outIsoMsg.set(51, outIsoMsg.getString(49)); 

		return outIsoMsg;
	}
	//Change by: Mr.Torkey getDiff by: Mirkamali
	@Override
	public byte[] preProcessBinaryMessage(Message incoMessage) throws CantPostProcessBinaryDataException {
		return incoMessage.getBinaryData();
	}

	@Override
	public byte[] decryptSecureBinaryMessage(byte[] encryptedData, Message incomingMessage) throws Exception {
		byte[] encryptedKey = Arrays.copyOfRange(encryptedData, 0, 128);
		byte[] encryptedMsgBody = Arrays.copyOfRange(encryptedData, 128, encryptedData.length);
		
		byte[] messageKeys = Arrays.copyOfRange(SecurityComponent.rsaDecrypt(encryptedKey), 0, 24);
		byte[] decryptedMsgBody = SecurityComponent.tripleDesDecrypt(encryptedMsgBody, Arrays.copyOfRange(messageKeys, 0, 8), Arrays.copyOfRange(messageKeys, 8, 16), Arrays.copyOfRange(messageKeys, 16, 24));

		
        int b1 = decryptedMsgBody[0] - 48;
        int b2 = decryptedMsgBody[1] - 48;
        int b3 = decryptedMsgBody[2] - 48;
        int b4 = decryptedMsgBody[3] - 48;

        int len = (b1 * 1000) + b2 * 100 + b3 * 10 + b4 + 4;
		
//        int b1 = HSMUtil.byteToInt(decryptedMsgBody[0]);
//        int b2 = HSMUtil.byteToInt(decryptedMsgBody[1]);
//        int len = (b1 * 256) + b2 + 2;
        
        byte[] result = Arrays.copyOfRange(decryptedMsgBody, 4, len);

        incomingMessage.setSecurityKey(messageKeys);
        incomingMessage.setBinaryData(result);
        GeneralDao.Instance.saveOrUpdate(incomingMessage);
        return result;
	}
	//Change by: Mr.Torkey getDiff by: Mirkamali
	@Override
	public byte[] encryptBinaryMessage(byte[] rawdata, Message incomingMessage)	throws Exception {
		byte[] messageKeys = incomingMessage.getSecurityKey();
		
		int len = rawdata.length + 4;
		int mod8 = (len % 8);
		int newSize = mod8 == 0 ? len : len + (8 - mod8); 
		byte[] newData = new byte[newSize];

//		newData[0] = (byte) (rawdata.length / 256); 
//		newData[1] = (byte) (rawdata.length % 256);
		
		newData[0] = (byte) (rawdata.length / 1000 + 48);
		newData[1] = (byte) ((rawdata.length % 1000) / 100 + 48);
		newData[2] = (byte) ((rawdata.length % 100) / 10 + 48);
		newData[3] = (byte) (rawdata.length % 10 + 48);
		
		
		for (int i = 4; i < newSize; i++) {
			if (i < (rawdata.length + 4)) {
				newData[i] = rawdata[i - 4];
			} else {
				newData[i] = 0;
			}
		}
		
//		System.out.println("\n=========\n newData = " + new String(Hex.encode(newData)));
		return SecurityComponent.tripleDesEncrypt(newData, Arrays.copyOfRange(messageKeys, 0, 8), Arrays.copyOfRange(messageKeys, 8, 16), Arrays.copyOfRange(messageKeys, 16, 24));
	}
}
