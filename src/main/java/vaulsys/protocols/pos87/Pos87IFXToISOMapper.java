package vaulsys.protocols.pos87;

import vaulsys.lottery.consts.LotteryState;
import vaulsys.mtn.MTNChargeService;
import vaulsys.mtn.exception.NoChargeAvailableException;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.pos87.encoding.BankBinToFarsi;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.util.ConfigUtil;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

public class Pos87IFXToISOMapper extends IfxToISOMapper{

	
	public static final Pos87IFXToISOMapper Instance = new Pos87IFXToISOMapper();
	
	private Pos87IFXToISOMapper(){}
	
	
    @Transient
    private static Logger logger = Logger.getLogger(Pos87IFXToISOMapper.class);
    
    private static Long MCIAppVer = 55L;

    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws Exception {

        ISOMsg isoMsg = new ISOMsg();
        ISOPackager packager = ((Pos87Protocol) ProtocolProvider.Instance
                .getByClass(Pos87Protocol.class)).getPackager();
        isoMsg.setPackager(packager);

        isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));
        isoMsg.set(2, ifx.getAppPAN());

        String processCode = "";
        processCode = "0000";

        isoMsg.set(3, mapTrnType(ifx.getTrnType())+ processCode);
        

        Long amt = ifx.getAuth_Amt();
        if (amt != null)
        	isoMsg.set(4, ifx.getAuth_Amt().toString());
        
        if (ifx.getSec_Amt() == null) {
        	isoMsg.set(6, isoMsg.getString(4));
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(amt)){
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(ifx.getTrx_Amt())) {
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null) {
        	isoMsg.set(6, ifx.getSec_Amt().toString());
        	
        }
        
		isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));
        isoMsg.set(10, ifx.getSec_CurRate());
        isoMsg.set(11, ifx.getSrc_TrnSeqCntr());

        Date currentDate = Calendar.getInstance().getTime();

        isoMsg.set(12, MyDateFormatNew.format("HHmmss", currentDate));
        isoMsg.set(13, MyDateFormatNew.format("MMdd", currentDate));
       	isoMsg.set(14, ifx.getExpDt());
        isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));
        isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));


        isoMsg.set(25, fillTerminalType(ifx));
//        isoMsg.set(32, ifx.getBankId().toString());
        isoMsg.set(32, new Long(639347L).toString());
        isoMsg.set(33, ifx.getDestBankId().toString());
        isoMsg.set(35, ifx.getTrk2EquivData());
        isoMsg.set(37, !Util.hasText(ifx.getNetworkRefId()) ? ifx.getSrc_TrnSeqCntr() : ifx.getNetworkRefId());
        isoMsg.set(38, ifx.getApprovalCode());
        
        isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));
        
        isoMsg.set(41, ifx.getTerminalId());
        isoMsg.set(42, ifx.getOrgIdNum());

        //isoMsg.set(new ISOBinaryField(43, fillField43(ifx, convertor))); //Raza commenting using field as String
		isoMsg.set(43, fillField43(ifx));
//        isoMsg.set(43, " ");
        
        isoMsg.set(new ISOBinaryField(44, fillField44(ifx, convertor)));
//        isoMsg.set(44, fillField44(ifx));
        
		isoMsg.set(new ISOBinaryField(48, fillField48(ifx, convertor)));
        isoMsg.set(49, ifx.getAuth_Currency());
        isoMsg.set(51, ifx.getSec_Currency());
        isoMsg.set(52, ifx.getPINBlock());

        String P54 = "";
        String strBal = "";

        for (int i = 0; i < 2; ++i) {
            AcctBal acctBal = null;
            if (i == 0)
                acctBal = ifx.getAcctBalAvailable();
            else if (i == 1)
                acctBal = ifx.getAcctBalLedger();

            if (acctBal == null)
                continue;

            strBal = "";
            if (acctBal.getAcctType().equals( AccType.CURRENT))
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

        String S90 = "";
        if((ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType())
        		|| ISOFinalMessageType.isReturnMessage(ifx.getIfxType()))
        	&& ifx.getOriginalDataElements()!= null	) {

//        	StringFormat stringFormat = new StringFormat(11, StringFormat.JUST_RIGHT);
//        	StringFormat trnSeqCntrFormat = new StringFormat(6, StringFormat.JUST_RIGHT);
        	
        	if (ifx.getOriginalDataElements().getMessageType() == null){
            	throw new ISOException("Invalid original data element: No Message Type for field 90");
        	}
        	
            S90 += ifx.getOriginalDataElements().getMessageType();
            S90 += StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getOriginalDataElements().getTrnSeqCounter(), '0');
            
            try {
				S90 += MyDateFormatNew.format("MMddHHmmss", ifx.getOriginalDataElements().getOrigDt().toDate());
			} catch (Exception e) {
				S90 += "0000000000";
			}
			S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getOriginalDataElements().getBankId(), '0');
			
            try {
				S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getOriginalDataElements().getFwdBankId(), '0');
			} catch (Exception e) {
				S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getFwdBankId(), '0');
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

        isoMsg.set(100, ifx.getRecvBankId() != null ? ifx.getRecvBankId().toString() : null);

        if (isoMsg.getMaxField() > 64) {
            isoMsg.set(128, ifx.getMsgAuthCode());
            isoMsg.unset(64);
        } else {
            isoMsg.set(64, ifx.getMsgAuthCode());
            isoMsg.unset(128);
        }

        return isoMsg;
    }

//    @Override
	public String mapError(IfxType type, String rsCode) {
		if (ISOFinalMessageType.isReversalRsMessage(type) || ISOFinalMessageType.isReconcilementRs(type)
				|| IfxType.PURCHASE_CHARGE_REV_REPEAT_RS.equals(type)) {

			/*** OLD APPLICATION VERSION ***/
			/***
			 * This condition only for older pos with old application version added!
			 ***/
//			if (GlobalContext.getInstance().getMyInstitution().getBin().equals(639347L))
			if (ProcessContext.get().getMyInstitution().getBin().equals(639347L))
				return ISOResponseCodes.APPROVED;
			/*******************************/
			
			
			if (ISOResponseCodes.APPROVED.equals(rsCode) || ISOResponseCodes.INVALID_ACCOUNT.equals(rsCode)
					|| ISOResponseCodes.CARD_EXPIRED.equals(rsCode) || ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED.equals(rsCode))
				return rsCode;

			return ISOResponseCodes.APPROVED;
		}
		
		if (ISOResponseCodes.INVALID_TO_ACCOUNT.equals(rsCode))
			return ISOResponseCodes.INVALID_TO_ACCOUNT;
		
		return super.mapError(type, rsCode);
	}
	
	@Override
	public byte[] fillField48(Ifx ifx, EncodingConvertor convertor) {

		//m.rehman: separating BI from financial incase of limit
		//if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType())) {
		if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType(),false)) {
			try {
				String currentTime = Calendar.getInstance().get(Calendar.YEAR) + "";
				POSTerminal t = TerminalService.findTerminal(POSTerminal.class, Long.valueOf(ifx.getTerminalId()));
				ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
				if (t != null) {
//					Contact contact = t.getOwner().getContact();

					byte[] percent = convertor.encode("%");

					finalBytes.write((currentTime.substring(2)+"00000000").getBytes());
					
					if (ConfigUtil.getBoolean(ConfigUtil.POS87_HAS_NAME)) {
						finalBytes.write(convertor.encode(t.getOwner().getName()));
					}
					finalBytes.write(percent);

					if (ConfigUtil.getBoolean(ConfigUtil.POS87_HAS_DAILY_MESSAGE)) {
						String dailyMessage = t.getDailyMessage();
						if (dailyMessage != null) {
							finalBytes.write(convertor.encode(dailyMessage));
						} else {
							finalBytes.write(convertor.encode("روز خوشی داشته باشید"));
						}
					}
//					}
					
					finalBytes.write(percent);
					if (ConfigUtil.getBoolean(ConfigUtil.POS87_HAS_ADDRESS)) {
						finalBytes.write(convertor.encode(t.getOwner().getSafeAddress()));
					}

					finalBytes.write(percent);
					if (ConfigUtil.getBoolean(ConfigUtil.POS87_HAS_PHONE_NUMBER)) {
						String fullPhoneNum = t.getOwner().getSafeFullPhoneNumber();
						if (Util.hasText(fullPhoneNum))
							finalBytes.write(convertor.encode(fullPhoneNum));
					}						
					finalBytes.write(percent);
					
					if (ConfigUtil.getBoolean(ConfigUtil.POS87_HAS_WEB_SITE_ADDRESS)) {
						String website = t.getOwner().getSafeWebsiteAddress();
						if (website != null && website.length()>0){
							finalBytes.write(website.getBytes());
						}
					}
					finalBytes.write(percent);
					
					if (ifx.getLotteryData() != null &&
							LotteryState.ASSIGNED.equals(ifx.getLotteryStateNxt())) {
					
						finalBytes.write(convertor.encode("*** "));
						finalBytes.write(convertor.encode(" شما برنده کارت با شماره مرجع "));
						finalBytes.write(convertor.encode(ifx.getLottery().getSerial() + ""));
						finalBytes.write(convertor.encode(" به مبلغ "));
						finalBytes.write(convertor.encode(ifx.getLottery().getCredit() + ""));
						finalBytes.write(convertor.encode(" ريال میباشيد"));
						finalBytes.write(convertor.encode(" ***"));
//						finalBytes.write(convertor.encode(" در صورت برگشت فاقد اعتبار است"));
						
						finalBytes.write(percent);
//						finalBytes.write("***win 50,000R***".getBytes());
//						finalBytes.write("***winner***".getBytes());
//						finalBytes.write(convertor.encode("برنده جایزه"));
						
					} else if (ifx.getLotteryData() != null 
								&& LotteryState.NOT_ASSIGNED.equals(ifx.getLotteryStateNxt())
								&& LotteryState.ASSIGNED.equals(ifx.getLotteryStatePrv()) 
							&& ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
						finalBytes.write(convertor.encode("*** "));
						finalBytes.write(convertor.encode("دارنده کارت گرامی! به علت برگشت تراکنش کارت جایزه شما فاقد اعتبار است "));
						finalBytes.write(convertor.encode(" ***"));
						
						finalBytes.write(percent);
//						finalBytes.write("***return 50,000R***".getBytes());
//						finalBytes.write("***return***".getBytes());
						
					} else if (
							(!Util.hasText(ifx.getApplicationVersion()) || 
							Long.parseLong(ifx.getApplicationVersion()) < MCIAppVer)
							&&
							(IfxType.PURCHASE_CHARGE_RS.equals(ifx.getIfxType())|| 
							IfxType.LAST_PURCHASE_CHARGE_RS.equals(ifx.getIfxType()) ) && 
							ifx.getChargeData() != null &&
							ISOResponseCodes.isSuccess(ifx.getRsCode())
							) {
						byte[] b = getMTNCharge16DigitPin(ifx, t, convertor);
						if(b == null){
							if (Util.hasText(t.getDescription())) {
								finalBytes.write(convertor.encode(t.getDescription()));
							}
						}else{
							finalBytes.write(convertor.encode("******************  "));
							finalBytes.write(b);
							finalBytes.write(convertor.encode("         ************** "));
						}
						finalBytes.write(percent);
						if (ConfigUtil.getBoolean(ConfigUtil.POS87_HAS_FOOTER)) {
							finalBytes.write("www.bpi.ir".getBytes());
						}
						
					} else {
						if (Util.hasText(t.getDescription())) {
							finalBytes.write(convertor.encode(t.getDescription()));
						}
						
						finalBytes.write(percent);
						if (ConfigUtil.getBoolean(ConfigUtil.POS87_HAS_FOOTER)) {
							finalBytes.write("www.bpi.ir".getBytes());
						}
					}
					
//					finalBytes.write(percent);
//					finalBytes.write("www.bpi.ir".getBytes());
					finalBytes.write(percent);
					finalBytes.write(BankBinToFarsi.bankName(Util.longValueOf(ifx.getDestBankId())));

					String refundDate = "000000000000";
					if (ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
						try {
//							MyDateFormat dateFormatHHMMDD = new MyDateFormat("yyMMddHHmmss");
							refundDate = MyDateFormatNew.format("yyMMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate());
						} catch (Exception e) {
							logger.warn("try to find OrigDt of referenceTrx: " + e +" ignoring...");
							refundDate = "000000000000";
						}
//						finally{
//							ifx.setOriginalDataElements(null);
//						}
					}

					finalBytes.write(percent);
					finalBytes.write(refundDate.getBytes());
					finalBytes.write(percent);

					if ((IfxType.PURCHASE_CHARGE_RS.equals(ifx.getIfxType())|| IfxType.LAST_PURCHASE_CHARGE_RS.equals(ifx.getIfxType()) ) && 
							ifx.getChargeData() != null &&
							ISOResponseCodes.isSuccess(ifx.getRsCode())) {
						
						finalBytes.write(getMTNChargeStream(ifx, t, percent));
					}
				}
				return finalBytes.toByteArray();
			} catch (Exception e) {
//				e.printStackTrace();
				logger.error("Encoutering an Exception in filling field_48: "+ e.getClass().getSimpleName()+":"+ e.getMessage());
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc("Encoutering an Exception in filling field_48: "+ e.getClass().getSimpleName()+":"+ e.getMessage());
				return null;
			}
		} else {
			logger.error("Bad situation: terminal["+ ifx.getTerminalId()+"] could not be found!");
			return null;
		}
	}

	private byte[] getMTNChargeStream(Ifx ifx, POSTerminal t, byte[] percent)
			throws NoChargeAvailableException {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		Set<SecureKey> keySet = t.getKeySet();
		try {		
			
			finalBytes.write(("IR"+ifx.getChargeData().getCharge().getCardSerialNo()).toString().getBytes());
			if (!Util.hasText(ifx.getApplicationVersion()) || Long.parseLong(ifx.getApplicationVersion()) < MCIAppVer) {
				//Seperator
				
				String decryptedPIN = "000000000000";
				//Just to support pins with more than 12 digits
				if(ifx.getChargeData().getCharge().getPinlen() <= 12){
					decryptedPIN = new String(SecurityComponent.rsaDecrypt(Hex.decode(ifx.getChargeData().getCharge().getCardPIN())));	
				}
				
				byte[] reencryptedPIN = SecurityComponent.generateCellChargePIN(keySet, decryptedPIN);
				
				finalBytes.write(reencryptedPIN);
				finalBytes.write(percent);
//				StringFormat creditFormat = new StringFormat(12, StringFormat.JUST_RIGHT);
				String credit = StringFormat.formatNew(12, StringFormat.JUST_RIGHT, MTNChargeService.getRealChargeCredit(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode()).toString(), '0');
				finalBytes.write(credit.getBytes());
				return finalBytes.toByteArray();
			} else {
				finalBytes.write(percent);

				byte[] decryptedPIN = SecurityComponent.rsaDecrypt(Hex.decode(ifx.getChargeData().getCharge().getCardPIN()));
				
				byte[] reencryptedPIN = SecurityComponent.encrypt(decryptedPIN, SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet));
				
				finalBytes.write(new String(Hex.encode(reencryptedPIN)).getBytes());
				finalBytes.write(percent);

				String credit = MTNChargeService.getRealChargeCredit(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode()).toString();
				finalBytes.write(credit.getBytes());
				return finalBytes.toByteArray();
			}
			
		} catch (Exception e) {
			throw new NoChargeAvailableException(e);
		}
	}

	private byte[] getMTNCharge16DigitPin(Ifx ifx, POSTerminal t, EncodingConvertor convertor)
		throws NoChargeAvailableException {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		Set<SecureKey> keySet = t.getKeySet();
		try {
			if(ifx.getChargeData().getCharge().getPinlen() > 12){
				String decryptedPIN = new String(SecurityComponent.rsaDecrypt(Hex.decode(ifx.getChargeData().getCharge().getCardPIN())));
							
//				finalBytes.write(convertor.encode("شماره رمز جدید "));
				finalBytes.write(convertor.encode("شماره رمز جدید(16 رقمی): "+new String(decryptedPIN)));
				return finalBytes.toByteArray();
			}
			return null;
		} catch (Exception e) {
			throw new NoChargeAvailableException(e);
		}
	}
}
