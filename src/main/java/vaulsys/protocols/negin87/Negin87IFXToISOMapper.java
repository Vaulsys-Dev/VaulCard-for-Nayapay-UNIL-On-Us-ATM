package vaulsys.protocols.negin87;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.PersianCalendar;
import vaulsys.migration.MigrationDataService;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.CardAccountInformation;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.negin87.util.TLVTag;
import vaulsys.terminal.TerminalService;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.util.littleendian.LittleEndian;
import vaulsys.util.littleendian.LittleEndianConsts;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import com.ghasemkiani.util.icu.PersianDateFormat;

public class Negin87IFXToISOMapper extends IfxToISOMapper{

	public static final Negin87IFXToISOMapper Instance = new Negin87IFXToISOMapper();
	
	private Negin87IFXToISOMapper(){}
	
	@Transient
    private static Logger logger = Logger.getLogger(Negin87IFXToISOMapper.class);

    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {

//        MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
//        MyDateFormat dateFormathhmmss = new MyDateFormat("HHmmss");
//        MyDateFormat dateFormatMMDD = new MyDateFormat("MMdd");

        ISOMsg isoMsg = new ISOMsg();
        ISOPackager packager = ((Negin87Protocol) ProtocolProvider.Instance
                .getByClass(Negin87Protocol.class)).getPackager();
        isoMsg.setPackager(packager);

        
        isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));
        if (!ISOFinalMessageType.isReturnResponseMessage(ifx.getIfxType()))
        	isoMsg.set(2, ifx.getAppPAN());

        String processCode = "";
//		if (AccType.MAIN_ACCOUNT.equals(ifx.getAccTypeFrom())) {
//			processCode = "00";
//		} else if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeFrom())) {
//			processCode = "10";
//		} else if (AccType.CARD.equals(ifx.getAccTypeFrom())) {
//			processCode = "20";
//		} else 
			processCode = "00";
		
//		if (AccType.MAIN_ACCOUNT.equals(ifx.getAccTypeTo())) {
//			processCode += "00";
//		} else if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeTo())) {
//			processCode += "10";
//		} else if (AccType.CARD.equals(ifx.getAccTypeTo())) {
//			processCode += "20";
//		} else 
			processCode += "00";

        isoMsg.set(3, mapTrnType(ifx.getTrnType())+processCode);

        Long amt = ifx.getAuth_Amt();
        if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
        	isoMsg.set(4, ifx.getAuth_Amt().toString());
        	        	
        } else {
        	isoMsg.set(4, ifx.getTrx_Amt().toString());
        	amt = ifx.getTrx_Amt();
        	
        }
        
        if (ifx.getSec_Amt() == null) {
        	isoMsg.set(6, isoMsg.getString(4));
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(amt)){
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(ifx.getTrx_Amt())) {
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null) {
        	isoMsg.set(6, ifx.getSec_Amt().toString());
        	
        }
		
        if (ifx.getTrnDt() != null)
        	isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));
        isoMsg.set(10, ifx.getSec_CurRate());
        isoMsg.set(11, ifx.getSrc_TrnSeqCntr());
        isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
        isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));

        if (ifx.getSafeCardAcctId() != null && ifx.getExpDt() != null)
			isoMsg.set(14, ifx.getExpDt() + "");

        if (ifx.getSettleDt() != null)
        	isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));
        if (ifx.getPostedDt()!= null)
        	isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));

        isoMsg.set(25, fillTerminalType(ifx));

        isoMsg.set(32, ifx.getBankId().toString());
        
        if (IfxType.TRANSFER_RQ.equals(ifx.getIfxType()) 
        		|| IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType())
        	|| IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())) {
        	
//        	Long bin = GlobalContext.getInstance().getMyInstitution().getBin();
        	Long bin = ProcessContext.get().getMyInstitution().getBin();
        	if (!bin.equals(ifx.getRecvBankId()) && !bin.equals(ifx.getDestBankId()))
        		isoMsg.set(32, "639347");
        }
        
        isoMsg.set(33, ifx.getDestBankId().toString());
        isoMsg.set(35, ifx.getTrk2EquivData());
        isoMsg.set(37, fillFieldANFix(ifx, 37));

        if (IfxType.TRANSFER_RS.equals(ifx.getIfxType()) 
        		|| IfxType.TRANSFER_REV_REPEAT_RS.equals(ifx.getIfxType())
        		|| IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())){
//        	Long bin = GlobalContext.getInstance().getMyInstitution().getBin();
//        	if (!ifx.getBankId().equals(ifx.getRecvBankId()) && !ifx.getBankId().equals(ifx.getDestBankId()))
        		isoMsg.set(32, "639347");
//        		StringFormat format37 = new StringFormat(12, StringFormat.JUST_RIGHT);
//        		ifx.setNetworkRefId(format37.format(ifx.getNetworkRefId(), '0'));
                isoMsg.set(37, StringFormat.formatNew(12, StringFormat.JUST_RIGHT, isoMsg.getString(37), '0'));
        }
        
//        StringFormat format37 = new StringFormat(12, StringFormat.JUST_RIGHT);
//        isoMsg.set(37, format37.format(ifx.getNetworkRefId(), '0'));
        
        
        isoMsg.set(38, ifx.getApprovalCode());
        
        isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));
        
        isoMsg.set(41, fillFieldANFix(ifx, 41));
        
        isoMsg.set(42, fillFieldANFix(ifx, 42));
        
//        StringFormat format41 = new StringFormat(8, StringFormat.JUST_RIGHT);
//        StringFormat format42 = new StringFormat(15, StringFormat.JUST_RIGHT);
//        isoMsg.set(41, format41.format(ifx.getTerminalId(), '0'));
//
//        if(ifx.getOrgIdNum() != null && !ifx.getOrgIdNum().equals("")){
//        	isoMsg.set(42, format42.format(ifx.getOrgIdNum().toString(), '0'));
//        }else{
//        	//if orgIdNum is null fill it with "    " (!)
//        	isoMsg.set(42, format42.format(" ", ' '));
//        }

        isoMsg.set(new ISOBinaryField(43, fillField43(ifx, convertor)));

        isoMsg.set(new ISOBinaryField(44, fillField44(ifx, convertor)));
        
//        isoMsg.set(new ISOBinaryField(48, fillField48(ifx, convertor)));
        
        isoMsg.set(49, ifx.getAuth_Currency());
        isoMsg.set(51, ifx.getSec_Currency());
        isoMsg.set(52, ifx.getPINBlock());

        if (ifx.getMode()!= null && Util.hasText(ifx.getCheckDigit()))
        	isoMsg.set(53, ifx.getMode().getType()+ifx.getCheckDigit()+"00000000000");
        
        String P54 = "";
        String strBal = "";

        //for( AcctBal acctBal:ifx.getAcctBals) {
        for (int i = 0; i < 2; ++i) {
            AcctBal acctBal = null;
            if (i == 0)
                acctBal = ifx.getAcctBalAvailable();
            else if (i == 1)
                acctBal = ifx.getAcctBalLedger();

            if (acctBal == null)
                continue;

            strBal = "";
            if (acctBal.getAcctType().equals(AccType.CURRENT))
                strBal += "01";
            else if (acctBal.getAcctType().equals( AccType.SAVING))
                strBal += "02";
            else
                strBal += "00";

            if (acctBal.getBalType().equals(BalType.LEDGER))
                strBal += "01";
            else if (acctBal.getBalType().equals(BalType.AVAIL))
                strBal += "02";
            else
                strBal += "00";

            strBal += acctBal.getCurCode();
            strBal += acctBal.getAmt();
            P54 += strBal;
        }
        
        isoMsg.set(54, P54);

        if (ifx.getNetworkManagementInformationCode()!= null)
        	isoMsg.set(70, ifx.getNetworkManagementInformationCode().getType());
//        if (ifx.getKeyManagement()!= null && Util.hasText(ifx.getKeyManagement().getKey()))
//        	isoMsg.set(new ISOBinaryField(96, Hex.decode(ifx.getKeyManagement().getKey())));
        
        String S90 = "";

//        if (ifx.getOriginalDataElements() != null && ifx.getOriginalDataElements().getMessageType() != null) {
        if(ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {

//            StringFormat stringFormat = new StringFormat(11, StringFormat.JUST_RIGHT);

//            S90 += MyString.valueOf(ifx.getOriginalDataElements().getMessageType());
            if (ifx.getSafeOriginalDataElements().getMessageType() == null){
            	throw new ISOException("Invalid original data element: No Message Type for field 90");
            }
            
            S90 += ifx.getSafeOriginalDataElements().getMessageType();
            
//            StringFormat trnSeqCntrFormat = new StringFormat(6, StringFormat.JUST_RIGHT);
            try {
				S90 += StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getTrnSeqCounter(), '0');
			} catch (Exception e) {
				S90 += "000000";
			}
			try {
				S90 += MyDateFormatNew.format("MMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate());
			} catch (Exception e) {
				S90 += "0000000000";
			}
			try {
				String bankId = StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getBankId(), '0');
				
				if (IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType())){
//	        	Long bin = GlobalContext.getInstance().getMyInstitution().getBin();
					Long bin = ProcessContext.get().getMyInstitution().getBin();
	        	if (!bin.equals(ifx.getRecvBankId()) && !bin.equals(ifx.getDestBankId()))
	        		bankId = StringFormat.formatNew(11, StringFormat.JUST_RIGHT, "639347", '0');
	        }
				S90 += bankId;
			} catch (Exception e) {
				S90 += "00000000000";
			}
			try {
				S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getFwdBankId(), '0');
			} catch (Exception e) {
				S90 += "00000000000";
			}
			isoMsg.set(90, S90);
        }


        String S95 = "";
        if (ifx.getNew_AmtAcqCur() != null && ifx.getNew_AmtIssCur() != null) {
            S95 += ifx.getNew_AmtAcqCur();
            S95 += ifx.getNew_AmtIssCur();
            S95 += "C00000000";
            S95 += "C00000000";
            isoMsg.set(95, S95);
        }


        if (ifx.getRecvBankId() == null || ifx.getRecvBankId().equals(""))
            isoMsg.set(100, ifx.getDestBankId().toString());
        else
            isoMsg.set(100, ifx.getRecvBankId().toString());

//        if (IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType()) &&
//        		ifx.getRecvBankId().equals(639347L))
//        	isoMsg.set(32, "639347");
        
//        if (ShetabFinalMessageType.isTransferMessage(ifx.getIfxType()) && 
//        	ShetabFinalMessageType.isResponseMessage(ifx.getIfxType())){
//            isoMsg.set(54, "0001364C0000077339710002364C000007733971");        	
//        }
//        if (ShetabFinalMessageType.isTransferCheckAccountMessage(ifx.getIfxType()) && 
//            ShetabFinalMessageType.isResponseMessage(ifx.getIfxType())){
//            isoMsg.set(102, "022080000036994001");
//        }

        if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeTo()))
				isoMsg.set(103, ifx.getSubsidiaryAccTo());
        	
        if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeFrom()))
				isoMsg.set(102, ifx.getSubsidiaryAccFrom());
        
/*        if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeFrom())
        		|| AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeTo())) {
        	if (ifx.getTrnType().isDebitTrnType())
        		isoMsg.set(102, ifx.getSubsidiaryAcct());
        	else
        		isoMsg.set(103, ifx.getSubsidiaryAcct());
        }
*/        
        if (isoMsg.getMaxField() > 64) {
            isoMsg.set(128, ifx.getMsgAuthCode());
            isoMsg.unset(64);
        } else {
            isoMsg.set(64, ifx.getMsgAuthCode());
            isoMsg.unset(128);
        }
        
        
        StringBuilder CVV2 = new StringBuilder();
        StringBuilder secAppPAN = new StringBuilder();
        try {
        	MigrationDataService.setChangedFields(ifx, isoMsg, CVV2, secAppPAN);
        	
        } catch(Exception e) {
        	logger.error("Exception in return changed field, ifx: " + ifx.getId(), e);
        }
        
        /****** Don't move this line, must be haminja! ******/
        isoMsg.set(new ISOBinaryField(48, fillField48(ifx, CVV2.toString(), secAppPAN.toString(), convertor)));

        return isoMsg;
    }

    
//    @Override
	public byte[] fillField48(Ifx ifx, String CVV2, String secAppPAN, EncodingConvertor convertor) {
		String p48 = "";
		p48 += "00";
//		StringFormat formatCVV = new StringFormat(4, StringFormat.JUST_RIGHT);
		p48 += StringFormat.formatNew(4, StringFormat.JUST_RIGHT, CVV2, '0');

		if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())
//				&& ifx.getBankId().equals(GlobalContext.getInstance().getMyInstitution().getBin())
				&& ifx.getBankId().equals(ProcessContext.get().getMyInstitution().getBin())
				&& !TerminalService.isOriginatorSwitchTerminal(ifx.getTransaction().getInputMessage())) {
//			StringFormat format = new StringFormat(13, StringFormat.JUST_RIGHT);
//			StringFormat format10 = new StringFormat(10, StringFormat.JUST_RIGHT);
			p48 += StringFormat.formatNew(10, StringFormat.JUST_RIGHT, "0", '0');
//			if(ifx.getBillOrgType().equals(OrganizationType.UNDEFINED))
//				p48 += OrganizationType.GAZ.getType();
//			else
				p48 += ifx.getBillOrgType().getType();
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0');
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0');
		} 
		
		//Negin specification!! processCode=50
//		if (ShetabFinalMessageType.isBillPaymentMessage(ifx.getIfxType())) {
//			StringFormat format = new StringFormat(13, StringFormat.JUST_RIGHT);		
//			p48 += OrganizationType.getCode(ifx.getBillOrgType());
//	
//			p48 += format.format(ifx.getBillID(), '0');
//			p48 += "=";
//			p48 += format.format(ifx.getBillPaymentID(), '0');
//		} 
		else if (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())
				|| ISOFinalMessageType.isSettlementTransfer(ifx.getIfxType())) {
			UserLanguage userLanguage = ifx.getUserLanguage();
			if (UserLanguage.FARSI_LANG.equals(userLanguage))
				p48 += "00";
			else 
				p48 += "01";
			String appPan = secAppPAN;
			p48 += appPan.length();
			p48 += appPan;
		} else if (ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType())){
			
				if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
					if (TerminalType.INTERNET.equals(ifx.getTerminalType())) {
//						p48 += "000" + ifx.getNewPINBlock();
						p48 += "00" + ifx.getNewPINBlock();
					} else {
						p48 = ifx.getNewPINBlock();
					}
				} else if (TrnType.CHANGEINTERNETPINBLOCK.equals(ifx.getTrnType()) &&
						ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					try {
						bytes.write(TLVTag.CVV2_TAG);
						bytes.write(new Integer(CVV2.length()).byteValue());
						bytes.write(CVV2.getBytes());
						bytes.write(TLVTag.EXP_DATE_TAG);
						bytes.write(new Integer(10).byteValue());
						Calendar calendar = PersianCalendar.getInstance();
						calendar.set(PersianCalendar.YEAR, 1300+(int) (ifx.getExpDt() / 100));
						calendar.set(PersianCalendar.MONTH, (int) (ifx.getExpDt() % 100) - 1);
						calendar.set(PersianCalendar.DAY_OF_MONTH, 1);
						PersianDateFormat format = new PersianDateFormat("YYYY/MM/dd");
						DateTime time = new DateTime(calendar.getTime());
						String timeStr = format.format(PersianCalendar.toGregorian(time).toDate());
						bytes.write(timeStr.getBytes());
						int bytes_length = bytes.size();
						byte[] refineData = new byte[bytes_length + 1 + TLVTag.INQUIRY_DATE_TAG.length];
						System.arraycopy(TLVTag.INQUIRY_DATE_TAG, 0, refineData, 0, TLVTag.INQUIRY_DATE_TAG.length);
						refineData[TLVTag.INQUIRY_DATE_TAG.length] = new Integer(bytes_length).byteValue();
						System.arraycopy(bytes.toByteArray(), 0, refineData, TLVTag.INQUIRY_DATE_TAG.length + 1,
								bytes_length);
						p48 = new String(Hex.encode(refineData)).toUpperCase();
					} catch (IOException e) {
					}
				}
		} else if (ISOFinalMessageType.isBankStatementMessage(ifx.getIfxType()) && ifx.getBankStatementData()!= null){
			p48 = "";
			if (ifx.getBankStatementData().size()>0) {
				byte[] data = new byte[LittleEndian.SHORT_SIZE];
				LittleEndian.putShort(data, new Integer(ifx.getBankStatementData().size()).shortValue());
				p48 += new String(Hex.encode(data)).toUpperCase();
//				p48 = LittleEndian.putShort(data, value);
//				p48 = Util.toHexStringLittleEndian(new Long(ifx.getBankStatementData().size())).substring(0, 4).toUpperCase() + p48;
			}
			
			for (int index = ifx.getBankStatementData().size(); index >0 ; index--){
//			for (BankStatementData data :ifx.getBankStatementData()){
				p48 += toHexStringLittleEndian(ifx.getBankStatementData().get(index-1)).toUpperCase();
			}
			
		} else if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType()) && ifx.getCardAccountInformation()!= null){
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			int num =0;
			try {
				if(ifx.getCardAccountInformation().size() > 0){
					for (CardAccountInformation account: ifx.getCardAccountInformation()){
						byte[] data = new byte[2+1+34];
						byte[] length = new byte[LittleEndianConsts.SHORT_SIZE];
						LittleEndian.putShort(length, (short)34);
						System.arraycopy(TLVTag.CUSTOMER_ACCOUNT_TAG, 0, data, 0, 2);
						data[2] = length[0];
						byte[] tmp = account.getAccountNumber().getBytes("windows-1256");
						System.arraycopy(tmp, 0, data, 3, tmp.length);
	//					System.out.println(new String(Hex.encode(data)).toUpperCase());
						for (int i =0; i< data.length; i++){
							bytes.write(data[i]);
						}
						num++;
					}
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Error in getting card's account info.: ", e);
			}
			
			if (num>0) {
				byte[] refineData = new byte[num * 37];
				System.arraycopy(bytes.toByteArray(), 0, refineData, 0, num * 37);
				byte[] length;
				if (num * 37 > 255) {
					byte[] tmp = new byte[LittleEndianConsts.INT_SIZE];
					LittleEndian.putShort(tmp, (short) (num * 37));
					length = new byte[3];
					length[0] = (byte) 0x82;
					length[1] = tmp[0];
					length[2] = tmp[1];
				} else {
					byte[] tmp = new byte[LittleEndianConsts.SHORT_SIZE];
					LittleEndian.putShort(tmp, (short) (num * 37));
					if (num * 37 > 128) {
						length = new byte[2];
						length[1] = tmp[0];
						length[0] = (byte) 0x81;
					} else {
						length = new byte[1];
						length[0] = tmp[0];
					}
				}
				p48 = new String(Hex.encode(TLVTag.INQUIRY_DATE_TAG)).toUpperCase()
						+ new String(Hex.encode(length)).toUpperCase()
						+ new String(Hex.encode(refineData)).toUpperCase();
			}
		} else if (IfxType.DEPOSIT_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try {
				bytes.write("***".getBytes());
				bytes.write(convertor.encode(ifx.getCardHolderFamily()));
				bytes.write("*".getBytes());
				bytes.write(convertor.encode(ifx.getCardHolderName()));
				bytes.write("*/0/00000000".getBytes());
				return bytes.toByteArray();
			} catch (IOException e) {
				logger.error("Encoutering an Exception in filling field_48: DEPOSIT_CHECK_ACCOUNT_RS: "+ e.getClass().getSimpleName()+":"+ e.getMessage());
			}
		}
		return (!Util.hasText(p48)?null :(p48.getBytes()));
	}
    
    @Override
	public byte[] fillField48(Ifx ifx, EncodingConvertor convertor) {
		String p48 = "";
		p48 += "00";
//		StringFormat formatCVV = new StringFormat(4, StringFormat.JUST_RIGHT);
		p48 += StringFormat.formatNew(4, StringFormat.JUST_RIGHT, ifx.getCVV2(), '0');

		if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())
//				&& ifx.getBankId().equals(GlobalContext.getInstance().getMyInstitution().getBin())
				&& ifx.getBankId().equals(ProcessContext.get().getMyInstitution().getBin())
				&& !TerminalService.isOriginatorSwitchTerminal(ifx.getTransaction().getInputMessage())) {
//			StringFormat format = new StringFormat(13, StringFormat.JUST_RIGHT);
//			StringFormat format10 = new StringFormat(10, StringFormat.JUST_RIGHT);
			p48 += StringFormat.formatNew(10, StringFormat.JUST_RIGHT, "0", '0');
//			if(ifx.getBillOrgType().equals(OrganizationType.UNDEFINED))
//				p48 += OrganizationType.GAZ.getType();
//			else
				p48 += ifx.getBillOrgType().getType();
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0');
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0');
		} 
		
		//Negin specification!! processCode=50
//		if (ShetabFinalMessageType.isBillPaymentMessage(ifx.getIfxType())) {
//			StringFormat format = new StringFormat(13, StringFormat.JUST_RIGHT);		
//			p48 += OrganizationType.getCode(ifx.getBillOrgType());
//	
//			p48 += format.format(ifx.getBillID(), '0');
//			p48 += "=";
//			p48 += format.format(ifx.getBillPaymentID(), '0');
//		} 
		else if (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())
				|| ISOFinalMessageType.isSettlementTransfer(ifx.getIfxType())) {
			UserLanguage userLanguage = ifx.getUserLanguage();
			if (UserLanguage.FARSI_LANG.equals(userLanguage))
				p48 += "00";
			else 
				p48 += "01";
			String appPan = ifx.getSecondAppPan();
			p48 += appPan.length();
			p48 += appPan;
		} else if (ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType())){
			
				if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
					if (TerminalType.INTERNET.equals(ifx.getTerminalType())) {
//						p48 += "000" + ifx.getNewPINBlock();
						p48 += "00" + ifx.getNewPINBlock();
					} else {
						p48 = ifx.getNewPINBlock();
					}
				} else if (TrnType.CHANGEINTERNETPINBLOCK.equals(ifx.getTrnType()) &&
						ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					try {
						bytes.write(TLVTag.CVV2_TAG);
						bytes.write(new Integer(ifx.getCVV2().length()).byteValue());
						bytes.write(ifx.getCVV2().getBytes());
						bytes.write(TLVTag.EXP_DATE_TAG);
						bytes.write(new Integer(10).byteValue());
						Calendar calendar = PersianCalendar.getInstance();
						calendar.set(PersianCalendar.YEAR, 1300+(int) (ifx.getExpDt() / 100));
						calendar.set(PersianCalendar.MONTH, (int) (ifx.getExpDt() % 100) - 1);
						calendar.set(PersianCalendar.DAY_OF_MONTH, 1);
						PersianDateFormat format = new PersianDateFormat("YYYY/MM/dd");
						DateTime time = new DateTime(calendar.getTime());
						String timeStr = format.format(PersianCalendar.toGregorian(time).toDate());
						bytes.write(timeStr.getBytes());
						int bytes_length = bytes.size();
						byte[] refineData = new byte[bytes_length + 1 + TLVTag.INQUIRY_DATE_TAG.length];
						System.arraycopy(TLVTag.INQUIRY_DATE_TAG, 0, refineData, 0, TLVTag.INQUIRY_DATE_TAG.length);
						refineData[TLVTag.INQUIRY_DATE_TAG.length] = new Integer(bytes_length).byteValue();
						System.arraycopy(bytes.toByteArray(), 0, refineData, TLVTag.INQUIRY_DATE_TAG.length + 1,
								bytes_length);
						p48 = new String(Hex.encode(refineData)).toUpperCase();
					} catch (IOException e) {
					}
				}
		} else if (ISOFinalMessageType.isBankStatementMessage(ifx.getIfxType()) && ifx.getBankStatementData()!= null){
			p48 = "";
			if (ifx.getBankStatementData().size()>0) {
				byte[] data = new byte[LittleEndian.SHORT_SIZE];
				LittleEndian.putShort(data, new Integer(ifx.getBankStatementData().size()).shortValue());
				p48 += new String(Hex.encode(data)).toUpperCase();
//				p48 = LittleEndian.putShort(data, value);
//				p48 = Util.toHexStringLittleEndian(new Long(ifx.getBankStatementData().size())).substring(0, 4).toUpperCase() + p48;
			}
			
			for (int index = ifx.getBankStatementData().size(); index >0 ; index--){
//			for (BankStatementData data :ifx.getBankStatementData()){
				p48 += toHexStringLittleEndian(ifx.getBankStatementData().get(index-1)).toUpperCase();
			}
			
		} else if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType()) && ifx.getCardAccountInformation()!= null){
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			int num =0;
			try {
				if(ifx.getCardAccountInformation().size() > 0){
					for (CardAccountInformation account: ifx.getCardAccountInformation()){
						byte[] data = new byte[2+1+34];
						byte[] length = new byte[LittleEndianConsts.SHORT_SIZE];
						LittleEndian.putShort(length, (short)34);
						System.arraycopy(TLVTag.CUSTOMER_ACCOUNT_TAG, 0, data, 0, 2);
						data[2] = length[0];
						byte[] tmp = account.getAccountNumber().getBytes("windows-1256");
						System.arraycopy(tmp, 0, data, 3, tmp.length);
	//					System.out.println(new String(Hex.encode(data)).toUpperCase());
						for (int i =0; i< data.length; i++){
							bytes.write(data[i]);
						}
						num++;
					}
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("Error in getting card's account info.: ", e);
			}
			
			if (num>0) {
				byte[] refineData = new byte[num * 37];
				System.arraycopy(bytes.toByteArray(), 0, refineData, 0, num * 37);
				byte[] length;
				if (num * 37 > 255) {
					byte[] tmp = new byte[LittleEndianConsts.INT_SIZE];
					LittleEndian.putShort(tmp, (short) (num * 37));
					length = new byte[3];
					length[0] = (byte) 0x82;
					length[1] = tmp[0];
					length[2] = tmp[1];
				} else {
					byte[] tmp = new byte[LittleEndianConsts.SHORT_SIZE];
					LittleEndian.putShort(tmp, (short) (num * 37));
					if (num * 37 > 128) {
						length = new byte[2];
						length[1] = tmp[0];
						length[0] = (byte) 0x81;
					} else {
						length = new byte[1];
						length[0] = tmp[0];
					}
				}
				p48 = new String(Hex.encode(TLVTag.INQUIRY_DATE_TAG)).toUpperCase()
						+ new String(Hex.encode(length)).toUpperCase()
						+ new String(Hex.encode(refineData)).toUpperCase();
			}
		} else if (IfxType.DEPOSIT_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try {
				bytes.write("***".getBytes());
				bytes.write(convertor.encode(ifx.getCardHolderFamily()));
				bytes.write("*".getBytes());
				bytes.write(convertor.encode(ifx.getCardHolderName()));
				bytes.write("*/0/00000000".getBytes());
				return bytes.toByteArray();
			} catch (IOException e) {
				logger.error("Encoutering an Exception in filling field_48: DEPOSIT_CHECK_ACCOUNT_RS: "+ e.getClass().getSimpleName()+":"+ e.getMessage());
			}
		}
		return (!Util.hasText(p48)?null :(p48.getBytes()));
	}


	public String toHexStringLittleEndian(BankStatementData data) {
		String result = "";
//		StringFormat format_61 = new StringFormat(122,StringFormat.JUST_LEFT);
//		yyyy/MM/dd HH:mm:ss
//		PersianDateFormat dateFormatPers = new PersianDateFormat((data.getTrxDt().toDate());
//		int year = new Integer(dateFormatPers.format(data.getTrxDt().toDate())).intValue();
//		dateFormatPers = new PersianDateFormat("MM");
//		int month = new Integer(dateFormatPers.format(data.getTrxDt().toDate())).intValue();
//		dateFormatPers = new PersianDateFormat("dd");
//		int day = new Integer(dateFormatPers.format(data.getTrxDt().toDate())).intValue();
//		dateFormatPers = new PersianDateFormat("HH");
//		int hour = new Integer(dateFormatPers.format(data.getTrxDt().toDate())).intValue();
//		dateFormatPers = new PersianDateFormat("mm");
//		int minute = new Integer(dateFormatPers.format(data.getTrxDt().toDate())).intValue();
//		dateFormatPers = new PersianDateFormat("ss");
//		int second = new Integer(dateFormatPers.format(data.getTrxDt().toDate())).intValue();
//		
//		DateTime time = new DateTime(new DayDate(year, month, day), new DayTime( hour, minute, second));
		DayDate persianDayDate = PersianCalendar.getPersianDayDate(data.getTrxDt().toDate());
		
		byte[] tmpData = new byte[35];
		int offset = 0;
		
		//BrCode 
		LittleEndian.putShort(tmpData, offset, new Integer(0).shortValue());
		offset += LittleEndian.SHORT_SIZE;
		
		//Document Date
//		LittleEndian.putShort(tmpData, offset, new Integer(time.getDayDate().getYear()).shortValue());
//		offset += LittleEndian.SHORT_SIZE;
//		
//		LittleEndian.putShort(tmpData, offset, new Integer(time.getDayDate().getMonth()).shortValue());
//		offset += LittleEndian.SHORT_SIZE;
//		
//		LittleEndian.putShort(tmpData, offset, new Integer(time.getDayDate().getDay()).shortValue());
//		offset += LittleEndian.SHORT_SIZE;
//		
//		LittleEndian.putShort(tmpData, offset, new Integer(time.getDayTime().getHour()).shortValue());
//		offset += LittleEndian.SHORT_SIZE;
//		
//		LittleEndian.putShort(tmpData, offset, new Integer(time.getDayTime().getMinute()).shortValue());
//		offset += LittleEndian.SHORT_SIZE;
//		
//		LittleEndian.putShort(tmpData, offset, new Integer(time.getDayTime().getSecond()).shortValue());
//		offset += LittleEndian.SHORT_SIZE;

		LittleEndian.putShort(tmpData, offset, (short)persianDayDate.getYear());
		offset += LittleEndian.SHORT_SIZE;
		
		LittleEndian.putShort(tmpData, offset, (short)persianDayDate.getMonth());
		offset += LittleEndian.SHORT_SIZE;
		
		LittleEndian.putShort(tmpData, offset, (short)persianDayDate.getDay());
		offset += LittleEndian.SHORT_SIZE;
		
		LittleEndian.putShort(tmpData, offset, (short)data.getTrxDt().getDayTime().getHour());
		offset += LittleEndian.SHORT_SIZE;
		
		LittleEndian.putShort(tmpData, offset, (short)data.getTrxDt().getDayTime().getMinute());
		offset += LittleEndian.SHORT_SIZE;
		
		LittleEndian.putShort(tmpData, offset, (short)data.getTrxDt().getDayTime().getSecond());
		offset += LittleEndian.SHORT_SIZE;

		//DocSerial
		LittleEndian.putInt(tmpData, offset, new Integer(0));
		offset += LittleEndian.INT_SIZE;
		
		byte flag = (byte) ("C".equalsIgnoreCase(data.getTrnType())? 0x01 : 0x00);
		Double amount = new Double(data.getAmount());
		amount = (flag == 0x01? amount: -1*amount);
		flag = 0x00;
		
		//Amount
		LittleEndian.putDouble(tmpData, offset, amount);
		offset += LittleEndian.DOUBLE_SIZE;
		
		//Balance
		Double balance = new Double(data.getBalance());
		LittleEndian.putDouble(tmpData, offset, balance - amount);
		offset += LittleEndian.DOUBLE_SIZE;
		
		tmpData[offset] = flag;
		offset += LittleEndian.BYTE_SIZE;
		
		result += new String(Hex.encode(tmpData)).toUpperCase();
		String dsc = "";
		try {
			dsc = (data.getDescription()==null)?"": new String (Hex.encode(data.getDescription().getBytes("windows-1256"))).toUpperCase();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} 
		result += StringFormat.formatNew(122,StringFormat.JUST_LEFT, dsc ,'0');
		return result;	
	}

	@Override
	public byte[] fillField43(Ifx ifx, EncodingConvertor convertor) {
//		StringFormat nameFormat = new StringFormat(22, StringFormat.JUST_LEFT);
//		StringFormat cityFormat = new StringFormat(13, StringFormat.JUST_LEFT);
//		StringFormat stateFormat = new StringFormat(3, StringFormat.JUST_LEFT);
//		StringFormat countryFormat = new StringFormat(2, StringFormat.JUST_LEFT);
		byte[] result = new byte[40];
				
		String name = (Util.hasText(ifx.getName())) ? ifx.getName() : "";
		name = StringFormat.formatNew(22, StringFormat.JUST_LEFT, name, ' ');
		name = name.replaceAll("ی", "ي");
		name = name.replaceAll("ء", "ئ");
		name = name.replaceAll("ک", "ك");
		
		try {
			System.arraycopy(name.getBytes("windows-1256"), 0, result, 0, 22);
		} catch (UnsupportedEncodingException e) {			
			System.arraycopy(name.getBytes(), 0, result, 0, 22);
		}
		String city = " ";
//		if (ifx.getCity() != null) {
//			city = ifx.getCity().getName();
//			if (Util.hasText(city))
//				city = city.substring(0, Math.min(13, city.length()));
//		}
			city = StringFormat.formatNew(13, StringFormat.JUST_LEFT, city, ' ');
//			city = city.replaceAll("ی", "ي");
//			city = city.replaceAll("ء", "ئ");
//			city = city.replaceAll("ک", "ك");
		
		try {
			System.arraycopy(city.getBytes("windows-1256"), 0, result, 22, 13);
		} catch (UnsupportedEncodingException e) {
			System.arraycopy(city.getBytes(), 0, result, 22, 13);
		}
		String state = " ";
//		if (ifx.getStateProv() != null) {
//			 state = (Util.hasText(ifx.getStateProv().getAbbreviation())) ? ifx.getStateProv().getAbbreviation() : "";
//		}
			state = StringFormat.formatNew(3, StringFormat.JUST_LEFT, state, ' ');
//			state = state.replaceAll("ی", "ي");
//			state = state.replaceAll("ء", "ئ");
//			state = state.replaceAll("ک", "ك");
			System.arraycopy(state.getBytes(), 0, result, 35, 3);

		String country = " ";
//		if (ifx.getCountry() != null) {
//			country = (Util.hasText(ifx.getCountry().getAbbreviation())) ? ifx.getCountry().getAbbreviation() : "";
//		}
			country = StringFormat.formatNew(2, StringFormat.JUST_LEFT, country, ' ');
//			country = country.replaceAll("ی", "ي");
//			country = country.replaceAll("ء", "ئ");
//			country = country.replaceAll("ک", "ك");
			System.arraycopy(country.getBytes(), 0, result, 38, 2);
		
		return result;
	}
}

