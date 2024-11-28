package vaulsys.protocols.PaymentSchemes.ISO8583;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOPOSConditionCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public abstract class ISOtoIfxMapper implements ProtocolToIfxMapper {

	transient Logger logger = Logger.getLogger(ISOtoIfxMapper.class);
	
	@Override
	public String mapError(String rsCode) {
		//change this method to support saderat protocol
		if(ISOResponseCodes.ORIGINAL_ALREADY_NACKED.equals(rsCode))
			return ISOResponseCodes.ORIGINAL_DATA_ELEMENT_MISMATCH;
		else if(ISOResponseCodes.BAD_AMOUNT.equals(rsCode))
			return ISOResponseCodes.PERMISSION_DENIED;
		else if(ISOResponseCodes.NO_COMMS_KEY.equals(rsCode))
			return ISOResponseCodes.FIELD_ERROR;//in check beshe ba saderat ke vaghan manzor az in error chi bude!
		else
			return rsCode;
	}
	 
	public void mapTerminalType(Ifx ifx, String f_25) {
		String terminalTypeCodeStr = f_25.trim();
		if (terminalTypeCodeStr == null || terminalTypeCodeStr.length()== 0)
			terminalTypeCodeStr = "-1";
		Integer terminalTypeCode = Integer.parseInt(terminalTypeCodeStr);
		ifx.setTerminalType(new TerminalType(terminalTypeCode));
		logger.info("ISOtoIFX:: Terminal Type [" + ifx.getTerminalType() + "]"); //Raza LOGGING ENHANCED
	}
	
	public void mapFieldANFix(Ifx ifx, ISOMsg isoMsg, int fieldId) {
		String fieldData = isoMsg.getString(fieldId);


//		if (fieldData != null && !"".equals(fieldData) && fieldData.length() > 0) {
			if (!Util.hasText(fieldData))
				return;

			if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
					&& ProcessContext.get().isMyInstitution(Long.parseLong(ifx.getInstitutionId())) //ProcessContext.get().getInputMessage().getChannel().getInstitution()) //ifx.getBankId()) //Raza use Channel Institution
					&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getInstitution(ifx.getInstitutionId()).getRole())) { //ProcessContext.get().getMyInstitutionByBIN(ifx.getBankId()).getRole())) { //Raza use Channel Institution
				if (fieldId == 37) {
					//String str_fld37 = Util.trimLeftZeros(fieldData); //Raza commenting Trim left Zeros
					ifx.setMyNetworkRefId(fieldData);
					logger.info("ISOtoIFX::DE-37 RRN [" + fieldData + "]"); //Raza LOGGING ENHANCED
				}
				if (fieldId == 41) {
					ifx.setTerminalId(fieldData.trim());
				}
				if (fieldId == 42) {
					ifx.setOrgIdNum(fieldData.trim());
				}
			} else {
				if (fieldId == 37) {
					ifx.setNetworkRefId(fieldData);
					logger.info("ISOtoIFX::DE-37 RRN [" + fieldData + "]"); //Raza LOGGING ENHANCED
				}
				if (fieldId == 41) {
					ifx.setTerminalId(fieldData);
				}
				if (fieldId == 42) {
					ifx.setOrgIdNum(fieldData);
				}
			}
//		}
	}
	
	public void mapField44(Ifx ifx, String f_44, EncodingConvertor convertor){
		if (!Util.hasText(f_44))
			return;
		byte[] b_44 = Hex.decode(f_44);
		if (b_44 != null &&
				(TrnType.CHECKACCOUNT.equals(ifx.getTrnType()) || TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType()))
			){
        	try {
        		
        		Integer name_length = 0;
        		if (b_44.length > 25) {
	        		byte[] bNameLen = new byte[]{b_44[25], b_44[26]};
	        		String strNameLen = new String(bNameLen);
	        		name_length = Integer.parseInt(strNameLen);//b_44[24] * 256 + b_44[25];
	        		byte[] name = new byte[name_length]; 
	        		System.arraycopy(b_44, 27, name, 0, name_length);
	        		if(UserLanguage.FARSI_LANG.equals(ifx.getUserLanguage())){
	        			ifx.setCardHolderName(convertor.decode(name));
	        			ifx.setTransientCardHolderName(name);
	        		}
	        		else
	        			ifx.setCardHolderName(new String(name));
        		}
				
        		if (name_length > 0 && b_44.length > 25 + 2 + name_length) {
	        		byte[] bFamilyLen = new byte[]{b_44[27+name_length], b_44[28+name_length]};
	        		String strFamilyLen = new String(bFamilyLen);
	        		Integer family_length = Integer.parseInt(strFamilyLen);//b_44[24] * 256 + b_44[25];
	        		byte[] family = new byte[family_length]; 
	        		System.arraycopy(b_44, 29 + name_length, family, 0, family_length);
	        		if(UserLanguage.FARSI_LANG.equals(ifx.getUserLanguage())){
	        			ifx.setCardHolderFamily(convertor.decode(family));
	        			ifx.setTransientCardHolderFamily(family);
	        		}
	        		else
	        			ifx.setCardHolderFamily(new String(family));
        		}
        		//Mousavi: Task 50243 : fill CardholderName  & Family if the Name_length is zero and all data is written in bytes related to Family// for Khavarmiane Bank
        		else if(name_length == 0 && b_44.length > 27 ){
	        		byte[] bFamilyLen = new byte[]{b_44[27], b_44[28]};
	        		String strFamilyLen = new String(bFamilyLen);
	        		Integer family_length = Integer.parseInt(strFamilyLen);//b_44[24] * 256 + b_44[25];
	        		byte[] family = new byte[family_length]; 
	        		System.arraycopy(b_44, 29 + name_length, family, 0, family_length);
	        		if(UserLanguage.FARSI_LANG.equals(ifx.getUserLanguage())){
	        			ifx.setCardHolderFamily(convertor.decode(family));
	        			ifx.setTransientCardHolderFamily(family);
	        		}
	        		else
	        			ifx.setCardHolderFamily(new String(family));
        		}
			} catch (Exception e) {
				ISOException isoe = new ISOException("Bad Format: Field 44", e);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity ( Severity.ERROR);
					ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
			}
        }
	}

	public void mapField48(Ifx ifx, String f_48, EncodingConvertor convertor) {
		if (!Util.hasText(f_48) || f_48.length() < 6)
			return;

		if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType()))
			return;

		try {
			Long cvv2 = Util.longValueOf(f_48.substring(2, 6));
			if (cvv2 == null)
				ifx.setCVV2(null);
			else 
				ifx.setCVV2(String.valueOf(cvv2));
		} catch(Exception e) {
			logger.warn("Exception in getting cvv2 from field 48!" + e);
			return;
		}
	
		if (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
			try{
				try {
					int lang = Integer.parseInt(f_48.substring(6, 8));
					if (lang == 0)
						ifx.setUserLanguage(UserLanguage.FARSI_LANG);
					else
						ifx.setUserLanguage(UserLanguage.ENGLISH_LANG);
				} catch (Exception e) {
					ifx.setUserLanguage(UserLanguage.FARSI_LANG);
				}
				int appLen = Integer.parseInt(f_48.substring(8, 10));
				ifx.setSecondAppPan(f_48.substring(10, 10 + appLen));
				
				if (IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifx.getIfxType()) && ifx.getBankId().equals(936450L)) {
					int index = 10 + appLen;
					ifx.getSafeOriginalDataElements().setBankId(f_48.substring(index, index + 9));
					ifx.getOriginalDataElements().setTrnSeqCounter(String.valueOf(Util.longValueOf(f_48.substring(index + 9, index + 9 + 6))));
					
					int hh = Util.integerValueOf(f_48.substring(index + 9 + 6, index + 9 + 6 + 2)); 
					int mm = Util.integerValueOf(f_48.substring(index + 9 + 6 + 2, index + 9 + 6 + 2 + 2));
					int ss = Util.integerValueOf(f_48.substring(index + 9 + 6 + 2 + 2, index + 9 + 6 + 2 + 2 + 2));
					
					int YYYY = Util.integerValueOf(f_48.substring(index + 9 + 6 + 2 + 2 + 2, index + 9 + 6 + 2 + 2 + 2 + 4));
					int MM = Util.integerValueOf(f_48.substring(index + 9 + 6 + 2 + 2 + 2 + 4, index + 9 + 6 + 2 + 2 + 2 + 4 + 2));
					int DD = Util.integerValueOf(f_48.substring(index + 9 + 6 + 2 + 2 + 2 + 4 + 2, index + 9 + 6 + 2 + 2 + 2 + 4 + 2 + 2));
//					DateTime persianDateTime = new DateTime(new DayDate(YYYY, MM, DD), new DayTime(hh, mm, ss));
//					DateTime origDt = PersianCalendar.toGregorian(persianDateTime);
					
					DateTime origDt = new DateTime(new DayDate(YYYY, MM, DD), new DayTime(hh, mm, ss));
					
					ifx.getOriginalDataElements().setOrigDt(origDt);
					ifx.getOriginalDataElements().setTerminalId(f_48.substring(index + 9 + 6 + 6 + 8, index + 9 + 6 + 6 + 8 + 8));
					ifx.getOriginalDataElements().setAppPAN(ifx.getAppPAN());
//                    ifx.getOriginalDataElements().setFwdBankId(0L);
                }
			} catch (Exception e) {
				logger.error("Could not set p48 fileds...");
			}
		}
	}

	//public void mapTrnType(Ifx ifx, Integer emvTrnType) { //Raza commenting
	public void mapTrnType(Ifx ifx, String emvTrnType) {
		//System.out.println("ISOtoIfxMapper:: mapTrnType -- emvTrnType [" + emvTrnType + "]"); //Raza TEMP
		if (emvTrnType.equals(ISOTransactionCodes.PURCHASE))
			ifx.setTrnType(TrnType.PURCHASE);
		else if (emvTrnType.equals(ISOTransactionCodes.WITHDRAWAL))
			ifx.setTrnType(TrnType.WITHDRAWAL);
		else if (emvTrnType.equals(ISOTransactionCodes.BALANCE_INQUERY))
			ifx.setTrnType(TrnType.BALANCEINQUIRY);
		else if(emvTrnType.equals(ISOTransactionCodes.BALANCE_INQUERY_31)) //Raza adding for code 31
			ifx.setTrnType(TrnType.BALANCEINQUIRY);
		else if (emvTrnType.equals(ISOTransactionCodes.BILL_PAYMENT_87))
			ifx.setTrnType(TrnType.BILLPAYMENT);
		else if (emvTrnType.equals(ISOTransactionCodes.BILL_PAYMENT_93))
			ifx.setTrnType(TrnType.BILLPAYMENT);
		else if (emvTrnType.equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
			ifx.setTrnType(TrnType.CHECKACCOUNT);
			//System.out.println("Setting IFX Tran TYPE as [" + TrnType.CHECKACCOUNT.toString() + "]"); //Raza TEMP
			//System.out.println("ifx.getTrnType [" + ifx.getIfxType() + "]"); //Raza TEMP
		}
		else if (emvTrnType.equals(ISOTransactionCodes.TRANSFER))
			ifx.setTrnType(TrnType.TRANSFER);
		else if (emvTrnType.equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT))
			ifx.setTrnType(TrnType.DECREMENTALTRANSFER);
		else if (emvTrnType.equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT))
			ifx.setTrnType(TrnType.INCREMENTALTRANSFER);
		/*else if (emvTrnType.equals(ISOTransactionCodes.RETURN)) //Raza commenting - now handled through REFUND
			ifx.setTrnType(TrnType.RETURN);*/
		else if (emvTrnType.equals(ISOTransactionCodes.PURCHASECHARGE))
			ifx.setTrnType(TrnType.PURCHASECHARGE);
		else if (emvTrnType.equals(ISOTransactionCodes.LASTPURCHASECHARGE))
			ifx.setTrnType(TrnType.LASTPURCHASECHARGE);
		else if (emvTrnType.equals(ISOTransactionCodes.PURCHASETOPUP))
			ifx.setTrnType(TrnType.PURCHASETOPUP);
		else if (emvTrnType.equals(ISOTransactionCodes.GET_ACCOUNT))
			ifx.setTrnType(TrnType.GETACCOUNT);
		else if (emvTrnType.equals(ISOTransactionCodes.GET_STATEMENT))
			ifx.setTrnType(TrnType.BANKSTATEMENT);
		else if (emvTrnType.equals(ISOTransactionCodes.CHANGE_PIN))
			ifx.setTrnType(TrnType.CHANGEPINBLOCK);
		else if (emvTrnType.equals(ISOTransactionCodes.CHANGE_PIN2))
			ifx.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		else if (emvTrnType.equals(ISOTransactionCodes.HOTCARD_INQ))
			ifx.setTrnType(TrnType.HOTCARD);
		else if (emvTrnType.equals(ISOTransactionCodes.DEPOSITE))
			ifx.setTrnType(TrnType.DEPOSIT);
		else if (emvTrnType.equals(ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT))
			ifx.setTrnType(TrnType.DEPOSIT_CHECK_ACCOUNT);
		else if (emvTrnType.equals(ISOTransactionCodes.SDAERAT_AUTH_BILL))
			ifx.setTrnType(TrnType.SADERAT_AUTH_BILLPAYMENT);
		else if(emvTrnType.equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT))
			ifx.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		else if(emvTrnType.equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT))
			ifx.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		else if(emvTrnType.equals(ISOTransactionCodes.THIRDPARTY_PAYMENT))
			ifx.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
//		else if(emvTrnType.equals(ISOTransactionCodes.BALANCE_INQUERY)) //Raza Adding for TranCode 30 - should be handled by ISO Msg Map Fields.
//			ifx.setTrnType(TrnType.BALANCEINQUIRY);
		else if(emvTrnType.equals(ISOTransactionCodes.PREAUTH))
			ifx.setTrnType(TrnType.PREAUTH);
		else if (emvTrnType.equals(ISOTransactionCodes.REFUND))
			ifx.setTrnType(TrnType.REFUND);
		else if(emvTrnType.equals(ISOTransactionCodes.PREAUTH))
			ifx.setTrnType(TrnType.PREAUTH);
        else if (emvTrnType.equals(ISOTransactionCodes.TITLE_FETCH))
            ifx.setTrnType(TrnType.TITLE_FETCH);
        else if (emvTrnType.equals(ISOTransactionCodes.IBFT))
            ifx.setTrnType(TrnType.IBFT);
		//m.rehman: for void transaction from NAC
		else if (emvTrnType.equals(ISOTransactionCodes.ADJUSTMENT))
			ifx.setTrnType(TrnType.VOID);
		else {
			ISOException isoe = new ISOException("Invalid Process Code :" + emvTrnType);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
		}
		//System.out.println("ISOtoIfxMapper:: mapTrnType -- ifx.getTrnType() [" + ifx.getTrnType() + "]"); //Raza TEMP
		logger.info("ISOtoIFX:: TranType [" + ifx.getTrnType() + "]"); //Raza LOGGING ENHANCED

	}


	//public void mapIfxType(Ifx ifx, String mti, Integer emvTrnType) { //Raza commenting
	public void mapIfxType(Ifx ifx, String mti, String emvTrnType) {

		//System.out.println("ISOtoIfxMapper:: mapIfxType - mti [" + mti + "]"); //Raza TEMP
		//System.out.println("ISOtoIfxMapper:: mapIfxType - emvTrnType [" + emvTrnType + "]"); //Raza TEMP

		IfxType finalMessageType = null;
		if (mti.equals(ISOMessageTypes.FINANCIAL_REQUEST_87) || mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87) ||
				mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_REPEAT_87)) {
			//switch (emvTrnType) {
			if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY))//case ISOTransactionCodes.BALANCE_INQUERY:
			{
				finalMessageType = IfxType.BAL_INQ_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
				finalMessageType = IfxType.BILL_PMT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
				finalMessageType = IfxType.BILL_PMT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
				finalMessageType = IfxType.PURCHASE_CHARGE_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.LASTPURCHASECHARGE)) {
				finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASETOPUP)) {
				finalMessageType = IfxType.PURCHASE_TOPUP_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
				finalMessageType = IfxType.WITHDRAWAL_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
				finalMessageType = IfxType.TRANSFER_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
				finalMessageType = IfxType.DEPOSIT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT)) {
				finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
				finalMessageType = IfxType.HOTCARD_INQ_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
				finalMessageType = IfxType.GET_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
				finalMessageType = IfxType.BANK_STATEMENT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
				finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY_31)) //Raza Adding for Issuing, have to change due to Pre-Auth
			{
				finalMessageType = IfxType.BAL_INQ_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
				finalMessageType = IfxType.PREAUTH_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
				if (Util.hasText(ifx.getPosConditionCode())) {
					if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
						finalMessageType = IfxType.PREAUTH_COMPLET_RQ;
					else
						finalMessageType = IfxType.PURCHASE_RQ;
				} else
					finalMessageType = IfxType.PURCHASE_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
				if (mti.equals(ISOMessageTypes.FINANCIAL_REQUEST_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))
					finalMessageType = IfxType.PURCHASE_CANCEL_RQ;
				else if (mti.equals(ISOMessageTypes.FINANCIAL_REQUEST_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
					finalMessageType = IfxType.PREAUTH_COMPLET_CANCEL_RQ;
				else if (mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
					finalMessageType = IfxType.PREAUTH_CANCEL_RQ;
			} else if (emvTrnType.equals(ISOTransactionCodes.TITLE_FETCH)) {
				finalMessageType = IfxType.TITLE_FETCH_RQ;
			}
			//m.rehman: for void transaction from NAC
			else if (emvTrnType.equals(ISOTransactionCodes.ADJUSTMENT)) {
				finalMessageType = IfxType.VOID_RQ;
			}
		}
		else if (mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_87)) {
			ifx.setMti("0220");
			//switch (emvTrnType) {
				if((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
					finalMessageType = IfxType.BAL_INQ_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
					finalMessageType = IfxType.BILL_PMT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
					finalMessageType = IfxType.BILL_PMT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
					finalMessageType = IfxType.PURCHASE_CHARGE_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.LASTPURCHASECHARGE)) {
					finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASETOPUP)) {
					finalMessageType = IfxType.PURCHASE_TOPUP_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
					finalMessageType = IfxType.WITHDRAWAL_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
					finalMessageType = IfxType.TRANSFER_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
					finalMessageType = IfxType.REFUND_ADVICE_RQ;
				/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
					finalMessageType = IfxType.RETURN_RQ;*/
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
					finalMessageType = IfxType.DEPOSIT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT)) {
					finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
					finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
					finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
					finalMessageType = IfxType.HOTCARD_INQ_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
					finalMessageType = IfxType.GET_ACCOUNT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
					finalMessageType = IfxType.BANK_STATEMENT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
					finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
					finalMessageType = IfxType.PREAUTH_RQ;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
					if (Util.hasText(ifx.getPosConditionCode())) {
						if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
							finalMessageType = IfxType.PREAUTH_COMPLET_ADVICE_RQ;
						else
							finalMessageType = IfxType.PURCHASE_RQ;
					} else
						finalMessageType = IfxType.PURCHASE_RQ;

				} else if (emvTrnType.equals(ISOTransactionCodes.IBFT))
					finalMessageType = IfxType.IBFT_ADVICE_RQ;
			//}
		} else if (mti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) || mti.equals(ISOMessageTypes.AUTHORIZATION_RESPONSE_87)
				|| mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87)) {
			//switch (emvTrnType) {
				if((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
					finalMessageType = IfxType.BAL_INQ_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
					finalMessageType = IfxType.BILL_PMT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
					finalMessageType = IfxType.BILL_PMT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
					finalMessageType = IfxType.PURCHASE_CHARGE_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.LASTPURCHASECHARGE)) {
					finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASETOPUP)) {
					finalMessageType = IfxType.PURCHASE_TOPUP_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
					finalMessageType = IfxType.WITHDRAWAL_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
					finalMessageType = IfxType.TRANSFER_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RS;
				}
				/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
					finalMessageType = IfxType.RETURN_RS;
					break;*/
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
					finalMessageType = IfxType.DEPOSIT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT)) {
					finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
					finalMessageType = IfxType.CHANGE_PIN_BLOCK_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
					finalMessageType = IfxType.CHANGE_PIN_BLOCK_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
					finalMessageType = IfxType.HOTCARD_INQ_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
					finalMessageType = IfxType.GET_ACCOUNT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
					finalMessageType = IfxType.BANK_STATEMENT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
					finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
					finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
					finalMessageType = IfxType.PREAUTH_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
					if (Util.hasText(ifx.getPosConditionCode())) {
						if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST)) {
							if (mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87))
								finalMessageType = IfxType.PREAUTH_COMPLET_ADVICE_RS;
							else
								finalMessageType = IfxType.PREAUTH_COMPLET_RS;
						} else
							finalMessageType = IfxType.PURCHASE_RS;
					} else
						finalMessageType = IfxType.PURCHASE_RS;
				}
				else if((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
					if (mti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))
						finalMessageType = IfxType.PURCHASE_CANCEL_RS;
					else if (mti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
						finalMessageType = IfxType.PREAUTH_COMPLET_CANCEL_RS;
					else if (mti.equals(ISOMessageTypes.AUTHORIZATION_RESPONSE_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
						finalMessageType = IfxType.PREAUTH_CANCEL_RS;
					else
						finalMessageType = IfxType.REFUND_ADVICE_RS;
            	} else if (emvTrnType.equals(ISOTransactionCodes.TITLE_FETCH)) {
					finalMessageType = IfxType.TITLE_FETCH_RS;

				} else if (emvTrnType.equals(ISOTransactionCodes.IBFT)) {
					finalMessageType = IfxType.IBFT_ADVICE_RS;

				}
				//m.rehman: for void transaction from NAC
				else if (emvTrnType.equals(ISOTransactionCodes.ADJUSTMENT)) {
					finalMessageType = IfxType.VOID_RS;
				}
		} else if (mti.equals(ISOMessageTypes.REVERSAL_ADVICE_87)
				|| mti.equals(ISOMessageTypes.REVERSAL_REQUEST_87)) {
			//m.rehman: commenting below line of code for proper response mti
			//ifx.setMti("0400");
			//switch (emvTrnType) {
			if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
				finalMessageType = IfxType.BAL_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
				finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
				finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
				finalMessageType = IfxType.TRANSFER_REV_REPEAT_RQ;
			}
			/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
				finalMessageType = IfxType.RETURN_REV_REPEAT_RQ;
				break;*/
			else if ((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
				finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
				finalMessageType = IfxType.HOTCARD_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
				finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
				finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
				finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
				finalMessageType = IfxType.PREAUTH_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
				/*if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
					finalMessageType = IfxType.PREAUTH_COMPLET_REV_REPEAT_RQ;
				else*/ //Raza MASTERCARD commenitng - MasterCard does not use POS Condition Code
				finalMessageType = IfxType.PURCHASE_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
				if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))
					finalMessageType = IfxType.PURCHASE_CANCEL_REV_REPEAT_RQ;
				else if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST)) {
					if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.FINANCIAL_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ;
					else if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ;
				}
			}
//			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.ADJUSTMENT)) { //Raza adding for KEENU
//				finalMessageType = IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ;
//			}
			//}
		}
		else if(mti == ISOMessageTypes.REVERSAL_ADVICE_REPEAT_87) {
			//switch (emvTrnType) {
			if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
				finalMessageType = IfxType.BAL_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
				finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
				finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
				finalMessageType = IfxType.TRANSFER_REV_REPEAT_RQ;
			}
				/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
					finalMessageType = IfxType.RETURN_REV_REPEAT_RQ;
					break;*/
			else if ((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
				finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
				finalMessageType = IfxType.HOTCARD_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
				finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
				finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
				finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
				finalMessageType = IfxType.PREAUTH_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
				if (Util.hasText(ifx.getPosConditionCode())) {
					if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
						finalMessageType = IfxType.PREAUTH_COMPLET_REV_REPEAT_RQ;
					else
						finalMessageType = IfxType.PURCHASE_REV_REPEAT_RQ;
				} else
					finalMessageType = IfxType.PURCHASE_REV_REPEAT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
				if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))
					finalMessageType = IfxType.PURCHASE_CANCEL_REV_REPEAT_RQ;
				else if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST)) {
					if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.FINANCIAL_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ;
					else if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ;
				}
			}
			//}
		}
		else if(mti == ISOMessageTypes.REVERSAL_ADVICE_RESPONSE_87) {
			//switch (emvTrnType) {
			if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
				finalMessageType = IfxType.BAL_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
				finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
				finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
				finalMessageType = IfxType.TRANSFER_REV_REPEAT_RS;
			}
				/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
					finalMessageType = IfxType.RETURN_REV_REPEAT_RS;
					break;*/
			else if ((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
				finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
				finalMessageType = IfxType.HOTCARD_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
				finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
				finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
				finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
				finalMessageType = IfxType.PREAUTH_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
				if (Util.hasText(ifx.getPosConditionCode())) {
					if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
						finalMessageType = IfxType.PREAUTH_COMPLET_REV_REPEAT_RS;
					else
						finalMessageType = IfxType.PURCHASE_REV_REPEAT_RS;
				} else
					finalMessageType = IfxType.PURCHASE_REV_REPEAT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
				if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))
					finalMessageType = IfxType.PURCHASE_CANCEL_REV_REPEAT_RS;
				else if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST)) {
					if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.FINANCIAL_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS;
					else if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_CANCEL_REV_REPEAT_RS;
				}
			}
			//}
		}
        else if(mti == ISOMessageTypes.REVERSAL_RESPONSE_87) {
			//switch (emvTrnType) {
			if ((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
				finalMessageType = IfxType.BAL_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
				finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
				finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
				finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
				finalMessageType = IfxType.TRANSFER_REV_REPEAT_RS;
				//ifx.setMti("0400");
			}
				/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
					finalMessageType = IfxType.RETURN_REV_REPEAT_RS;
					ifx.setMti("0400");
					break;*/
			else if ((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
				finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
				finalMessageType = IfxType.HOTCARD_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
				finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
				finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
				finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
				finalMessageType = IfxType.PREAUTH_REV_REPEAT_RS;
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
				if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))
					finalMessageType = IfxType.PURCHASE_CANCEL_REV_REPEAT_RS;
				else if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST)) {
					if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.FINANCIAL_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS;
					else if (ifx.getSafeOriginalDataElements().getMessageType().equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87))
						finalMessageType = IfxType.PREAUTH_CANCEL_REV_REPEAT_RS;
				}
				//ifx.setMti("0400");
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
				if (Util.hasText(ifx.getPosConditionCode())) {
					if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
						finalMessageType = IfxType.PREAUTH_COMPLET_REV_REPEAT_RS;
					else
						finalMessageType = IfxType.PURCHASE_REV_REPEAT_RS;
				} else
					finalMessageType = IfxType.PURCHASE_REV_REPEAT_RS;
				//ifx.setMti("0400");
			}
			//}
		}
        else if(mti == ISOMessageTypes.AUTHORIZATION_REQUEST_87) {
			//switch (emvTrnType) {
			if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
			}
			//}
		}
        else if(mti == ISOMessageTypes.AUTHORIZATION_RESPONSE_87) {
			//switch (emvTrnType) {
			if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS;
			} else if ((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RS;
			}
			//}
		}
            else if(mti == ISOMessageTypes.ACQUIRER_RECON_REQUEST_87) {
			finalMessageType = IfxType.ACQUIRER_REC_RQ;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}
            else if(mti == ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87) {
			finalMessageType = IfxType.ACQUIRER_REC_RS;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}
            else if(mti ==ISOMessageTypes.ACQUIRER_RECON_ADVICE_87) {
			finalMessageType = IfxType.ACQUIRER_REC_REPEAT_RQ;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}
            else if(mti == ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87) {
			finalMessageType = IfxType.ACQUIRER_REC_REPEAT_RS;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}
            else if(mti == ISOMessageTypes.ISSUER_RECON_REQUEST_87) {
			finalMessageType = IfxType.CARD_ISSUER_REC_RQ;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}
            else if(mti == ISOMessageTypes.ISSUER_RECON_RESPONSE_87) {
			finalMessageType = IfxType.CARD_ISSUER_REC_RS;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}
            else if(mti ==ISOMessageTypes.ISSUER_RECON_ADVICE_87) {
			finalMessageType = IfxType.CARD_ISSUER_REC_REPEAT_RQ;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}
            else if(mti == ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87) {
			finalMessageType = IfxType.CARD_ISSUER_REC_REPEAT_RS;
			ifx.setTrnType(TrnType.RECONCILIATION);
		}

            else if(mti == ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87) {
			finalMessageType = IfxType.NETWORK_MGR_RQ;
			ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		}
            else if(mti == ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87) {
			finalMessageType = IfxType.NETWORK_MGR_RS;
			ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		}
         else if(mti == ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87) {
			finalMessageType = IfxType.NETWORK_MGR_REPEAT_RQ;
			ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		}
         else if(mti == ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_87) {
			finalMessageType = IfxType.NETWORK_MGR_REPEAT_RS;
			ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		}
		//m.rehman: for loro advice/loro reversal
		else if(mti.equals(ISOMessageTypes.LORO_ADVICE_87) || mti.equals(ISOMessageTypes.LORO_ADVICE_93)
				|| mti.equals(ISOMessageTypes.LORO_ADVICE_REPEAT_87) || mti.equals(ISOMessageTypes.LORO_ADVICE_REPEAT_93)) {
			finalMessageType = IfxType.LORO_ADVICE_RQ;
			if (emvTrnType.equals(ISOTransactionCodes.WITHDRAWAL))
				ifx.setTrnType(TrnType.WITHDRAWAL);
			else if (emvTrnType.equals(ISOTransactionCodes.PURCHASE))
				ifx.setTrnType(TrnType.PURCHASE);
		} else if(mti.equals(ISOMessageTypes.LORO_ADVICE_RESPONSE_87)
				|| mti.equals(ISOMessageTypes.LORO_ADVICE_RESPONSE_93)) {
			finalMessageType = IfxType.LORO_ADVICE_RS;
			if (emvTrnType.equals(ISOTransactionCodes.WITHDRAWAL))
				ifx.setTrnType(TrnType.WITHDRAWAL);
			else if (emvTrnType.equals(ISOTransactionCodes.PURCHASE))
				ifx.setTrnType(TrnType.PURCHASE);
		} else if(mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_87)
				|| mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_93)
				|| mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_REPEAT_87)
				|| mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_REPEAT_93)) {
			finalMessageType = IfxType.LORO_REVERSAL_REPEAT_RQ;
			if (emvTrnType.equals(ISOTransactionCodes.WITHDRAWAL))
				ifx.setTrnType(TrnType.WITHDRAWAL);
			else if (emvTrnType.equals(ISOTransactionCodes.PURCHASE))
				ifx.setTrnType(TrnType.PURCHASE);
		} else if(mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_RESPONSE_87)
				|| mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_RESPONSE_93)) {
			finalMessageType = IfxType.LORO_REVERSAL_REPEAT_RS;
			if (emvTrnType.equals(ISOTransactionCodes.WITHDRAWAL))
				ifx.setTrnType(TrnType.WITHDRAWAL);
			else if (emvTrnType.equals(ISOTransactionCodes.PURCHASE))
				ifx.setTrnType(TrnType.PURCHASE);
		}
		//m.rehman: for batch transaction from NAC
		else if (mti.equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_87)
				|| mti.equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_93)) {
			//switch (emvTrnType) {
			if((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
				finalMessageType = IfxType.BAL_INQ_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
				finalMessageType = IfxType.BILL_PMT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
				finalMessageType = IfxType.BILL_PMT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
				finalMessageType = IfxType.PURCHASE_CHARGE_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.LASTPURCHASECHARGE)) {
				finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASETOPUP)) {
				finalMessageType = IfxType.PURCHASE_TOPUP_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
				finalMessageType = IfxType.WITHDRAWAL_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
				finalMessageType = IfxType.TRANSFER_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
				finalMessageType = IfxType.REFUND_ADVICE_RQ;
				/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
					finalMessageType = IfxType.RETURN_RQ;*/
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
				finalMessageType = IfxType.DEPOSIT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT)) {
				finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
				finalMessageType = IfxType.HOTCARD_INQ_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
				finalMessageType = IfxType.GET_ACCOUNT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
				finalMessageType = IfxType.BANK_STATEMENT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
				finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
				finalMessageType = IfxType.PREAUTH_RQ;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
				if (Util.hasText(ifx.getPosConditionCode())) {
					if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
						finalMessageType = IfxType.PREAUTH_COMPLET_ADVICE_RQ;
					else
						finalMessageType = IfxType.PURCHASE_RQ;
				} else
					finalMessageType = IfxType.PURCHASE_RQ;

			} else if (emvTrnType.equals(ISOTransactionCodes.IBFT))
				finalMessageType = IfxType.IBFT_ADVICE_RQ;
			//}
		} else if (mti.equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_RESPONSE_87)
				|| mti.equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_RESPONSE_93)) {
			//switch (emvTrnType) {
			if((emvTrnType.toString()).equals(ISOTransactionCodes.BALANCE_INQUERY)) {
				finalMessageType = IfxType.BAL_INQ_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_87)) {
				finalMessageType = IfxType.BILL_PMT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.BILL_PAYMENT_93)) {
				finalMessageType = IfxType.BILL_PMT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASECHARGE)) {
				finalMessageType = IfxType.PURCHASE_CHARGE_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.LASTPURCHASECHARGE)) {
				finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASETOPUP)) {
				finalMessageType = IfxType.PURCHASE_TOPUP_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.WITHDRAWAL)) {
				finalMessageType = IfxType.WITHDRAWAL_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER)) {
				finalMessageType = IfxType.TRANSFER_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_FROM_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RS;
			}
				/*case ISOTransactionCodes.RETURN: //Raza commenitng now handled through REFUND
					finalMessageType = IfxType.RETURN_RS;
					break;*/
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSITE)) {
				finalMessageType = IfxType.DEPOSIT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT)) {
				finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHANGE_PIN2)) {
				finalMessageType = IfxType.CHANGE_PIN_BLOCK_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.HOTCARD_INQ)) {
				finalMessageType = IfxType.HOTCARD_INQ_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_ACCOUNT)) {
				finalMessageType = IfxType.GET_ACCOUNT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.GET_STATEMENT)) {
				finalMessageType = IfxType.BANK_STATEMENT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)) {
				finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
				finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PREAUTH)) {
				finalMessageType = IfxType.PREAUTH_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.PURCHASE)) {
				if (Util.hasText(ifx.getPosConditionCode())) {
					if (ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST)) {
						if (mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87))
							finalMessageType = IfxType.PREAUTH_COMPLET_ADVICE_RS;
						else
							finalMessageType = IfxType.PREAUTH_COMPLET_RS;
					} else
						finalMessageType = IfxType.PURCHASE_RS;
				} else
					finalMessageType = IfxType.PURCHASE_RS;
			}
			else if((emvTrnType.toString()).equals(ISOTransactionCodes.REFUND)) {
				if (mti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.NORMAL_PRESENT))
					finalMessageType = IfxType.PURCHASE_CANCEL_RS;
				else if (mti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
					finalMessageType = IfxType.PREAUTH_COMPLET_CANCEL_RS;
				else if (mti.equals(ISOMessageTypes.AUTHORIZATION_RESPONSE_87) && ifx.getPosConditionCode().equals(ISOPOSConditionCodes.PRE_AUTHORIZATION_REQUEST))
					finalMessageType = IfxType.PREAUTH_CANCEL_RS;
				else
					finalMessageType = IfxType.REFUND_ADVICE_RS;
			} else if (emvTrnType.equals(ISOTransactionCodes.TITLE_FETCH)) {
				finalMessageType = IfxType.TITLE_FETCH_RS;

			} else if (emvTrnType.equals(ISOTransactionCodes.IBFT)) {
				finalMessageType = IfxType.IBFT_ADVICE_RS;

			}
			//m.rehman: for void transaction from NAC
			else if (emvTrnType.equals(ISOTransactionCodes.ADJUSTMENT)) {
				finalMessageType = IfxType.VOID_RS;
			}
		}

		//bkup start
		/*
		switch (mti) { //start
            case ISOMessageTypes.FINANCIAL_RQ_87:
                switch (emvTrnType) {
                    case ISOTransactionCodes.BALANCE_INQUERY:
                        finalMessageType = IfxType.BAL_INQ_RQ;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_87:
                        finalMessageType = IfxType.BILL_PMT_RQ;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_93:
                        finalMessageType = IfxType.BILL_PMT_RQ;
                        break;
                    case ISOTransactionCodes.PURCHASE:
                    	finalMessageType = IfxType.PURCHASE_RQ;
                    	break;
                    case ISOTransactionCodes.PURCHASECHARGE:
                    	finalMessageType = IfxType.PURCHASE_CHARGE_RQ;
                    	break;
                    case ISOTransactionCodes.LASTPURCHASECHARGE:
                    	finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RQ;
                    	break;
                    case ISOTransactionCodes.PURCHASETOPUP:
                    	finalMessageType = IfxType.PURCHASE_TOPUP_RQ;
                    	break;
                    case ISOTransactionCodes.WITHDRAWAL:
                    	finalMessageType = IfxType.WITHDRAWAL_RQ;
                    	break;
                    case ISOTransactionCodes.CHECK_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER:
                        finalMessageType = IfxType.TRANSFER_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER_TO_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER_FROM_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RQ;
                        break;
                    case ISOTransactionCodes.RETURN:
                        finalMessageType = IfxType.RETURN_RQ;
                        break;
                    case ISOTransactionCodes.DEPOSITE:
                    	finalMessageType = IfxType.DEPOSIT_RQ;
                    	break;
                    case ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT:
                    	finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RQ;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN2:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
                    	break;
                    case ISOTransactionCodes.HOTCARD_INQ:
                    	finalMessageType = IfxType.HOTCARD_INQ_RQ;
                    	break;
                    case ISOTransactionCodes.GET_ACCOUNT:
                    	finalMessageType = IfxType.GET_ACCOUNT_RQ;
                    	break;
                    case ISOTransactionCodes.GET_STATEMENT:
                    	finalMessageType = IfxType.BANK_STATEMENT_RQ;
                    	break;
                    case ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;
                    	break;
                    case ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ;
                    	break;
                    case ISOTransactionCodes.THIRDPARTY_PAYMENT:
                    	finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RQ;
                    	break;
                }
                break;

            case ISOMessageTypes.FINANCIAL_ADVICE_RQ_87:
            	ifx.setMti("0220");
            	switch (emvTrnType) {
            	case ISOTransactionCodes.BALANCE_INQUERY:
            		finalMessageType = IfxType.BAL_INQ_RQ;
            		break;
            	case ISOTransactionCodes.BILL_PAYMENT_87:
            		finalMessageType = IfxType.BILL_PMT_RQ;
            		break;
            	case ISOTransactionCodes.BILL_PAYMENT_93:
            		finalMessageType = IfxType.BILL_PMT_RQ;
            		break;
            	case ISOTransactionCodes.PURCHASE:
            		finalMessageType = IfxType.PURCHASE_RQ;
            		break;
            	case ISOTransactionCodes.PURCHASECHARGE:
            		finalMessageType = IfxType.PURCHASE_CHARGE_RQ;
            		break;
            	case ISOTransactionCodes.LASTPURCHASECHARGE:
            		finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RQ;
            		break;
            	case ISOTransactionCodes.PURCHASETOPUP:
            		finalMessageType = IfxType.PURCHASE_TOPUP_RQ;
            		break;
            	case ISOTransactionCodes.WITHDRAWAL:
            		finalMessageType = IfxType.WITHDRAWAL_RQ;
            		break;
            	case ISOTransactionCodes.CHECK_ACCOUNT:
            		finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
            		break;
            	case ISOTransactionCodes.TRANSFER:
            		finalMessageType = IfxType.TRANSFER_RQ;
            		break;
            	case ISOTransactionCodes.TRANSFER_TO_ACCOUNT:
            		finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RQ;
            		break;
            	case ISOTransactionCodes.TRANSFER_FROM_ACCOUNT:
            		finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RQ;
            		break;
            	case ISOTransactionCodes.RETURN:
            		finalMessageType = IfxType.RETURN_RQ;
            		break;
            	case ISOTransactionCodes.DEPOSITE:
            		finalMessageType = IfxType.DEPOSIT_RQ;
            		break;
            	case ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT:
            		finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RQ;
            		break;
            	case ISOTransactionCodes.CHANGE_PIN:
            		finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
            		break;
            	case ISOTransactionCodes.CHANGE_PIN2:
            		finalMessageType = IfxType.CHANGE_PIN_BLOCK_RQ;
            		break;
            	case ISOTransactionCodes.HOTCARD_INQ:
            		finalMessageType = IfxType.HOTCARD_INQ_RQ;
            		break;
            	case ISOTransactionCodes.GET_ACCOUNT:
            		finalMessageType = IfxType.GET_ACCOUNT_RQ;
            		break;
            	case ISOTransactionCodes.GET_STATEMENT:
            		finalMessageType = IfxType.BANK_STATEMENT_RQ;
            		break;
            	case ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT:
            		finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;
            		break;
            	case ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT:
            		finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ;
            		break;
            	case ISOTransactionCodes.THIRDPARTY_PAYMENT:
            		finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RQ;
            		break;
            	}
            	break;

            case ISOMessageTypes.FINANCIAL_RS_87:
                switch (emvTrnType) {
                    case ISOTransactionCodes.BALANCE_INQUERY:
                        finalMessageType = IfxType.BAL_INQ_RS;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_87:
                        finalMessageType = IfxType.BILL_PMT_RS;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_93:
                        finalMessageType = IfxType.BILL_PMT_RS;
                        break;
                    case ISOTransactionCodes.PURCHASE:
                    	finalMessageType = IfxType.PURCHASE_RS;
                    	break;

                    case ISOTransactionCodes.PURCHASECHARGE:
                    	finalMessageType = IfxType.PURCHASE_CHARGE_RS;
                    	break;

                    case ISOTransactionCodes.LASTPURCHASECHARGE:
                    	finalMessageType = IfxType.LAST_PURCHASE_CHARGE_RS;
                    	break;

                    case ISOTransactionCodes.PURCHASETOPUP:
                    	finalMessageType = IfxType.PURCHASE_TOPUP_RS;
                    	break;

                    case ISOTransactionCodes.WITHDRAWAL:
                    	finalMessageType = IfxType.WITHDRAWAL_RS;
                    	break;
                    case ISOTransactionCodes.CHECK_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RS;
                        break;
                    case ISOTransactionCodes.TRANSFER:
                        finalMessageType = IfxType.TRANSFER_RS;
                        break;
                    case ISOTransactionCodes.TRANSFER_TO_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_RS;
                        break;
                    case ISOTransactionCodes.TRANSFER_FROM_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_RS;
                        break;
                    case ISOTransactionCodes.RETURN:
                        finalMessageType = IfxType.RETURN_RS;
                        break;
                    case ISOTransactionCodes.DEPOSITE:
                    	finalMessageType = IfxType.DEPOSIT_RS;
                    	break;
                    case ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT:
                    	finalMessageType = IfxType.DEPOSIT_CHECK_ACCOUNT_RS;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_RS;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN2:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_RS;
                    	break;
                    case ISOTransactionCodes.HOTCARD_INQ:
                    	finalMessageType = IfxType.HOTCARD_INQ_RS;
                    	break;
                    case ISOTransactionCodes.GET_ACCOUNT:
                    	finalMessageType = IfxType.GET_ACCOUNT_RS;
                    	break;
                    case ISOTransactionCodes.GET_STATEMENT:
                    	finalMessageType = IfxType.BANK_STATEMENT_RS;
                    	break;
                    case ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS;
                    	break;
                    case ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_RS;
                    	break;
                    case ISOTransactionCodes.THIRDPARTY_PAYMENT:
                    	finalMessageType = IfxType.THIRD_PARTY_PURCHASE_RS;
                    	break;
                }
                break;

            case ISOMessageTypes.REVERSAL_ADVICE_87:
            	ifx.setMti("0400");
                switch (emvTrnType) {
                    case ISOTransactionCodes.BALANCE_INQUERY:
                        finalMessageType = IfxType.BAL_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_87:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_93:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.PURCHASE:
                    	finalMessageType = IfxType.PURCHASE_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.PURCHASECHARGE:
                    	finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.WITHDRAWAL:
                        finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER_FROM_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER_TO_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER:
                        finalMessageType = IfxType.TRANSFER_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.RETURN:
                        finalMessageType = IfxType.RETURN_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.DEPOSITE:
                    	finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN2:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.HOTCARD_INQ:
                    	finalMessageType = IfxType.HOTCARD_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.GET_ACCOUNT:
                    	finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.GET_STATEMENT:
                    	finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.THIRDPARTY_PAYMENT:
                    	finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ;
                    	break;

                }
                break;


            case ISOMessageTypes.REVERSAL_ADVICE_REPEAT_87:
                switch (emvTrnType) {
                    case ISOTransactionCodes.BALANCE_INQUERY:
                        finalMessageType = IfxType.BAL_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_87:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_93:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.PURCHASE:
                    	finalMessageType = IfxType.PURCHASE_REV_REPEAT_RQ;
                    	break;

                    case ISOTransactionCodes.PURCHASECHARGE:
                    	finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ;
                    	break;

                    case ISOTransactionCodes.WITHDRAWAL:
                        finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER_FROM_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER_TO_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.TRANSFER:
                        finalMessageType = IfxType.TRANSFER_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.RETURN:
                        finalMessageType = IfxType.RETURN_REV_REPEAT_RQ;
                        break;
                    case ISOTransactionCodes.DEPOSITE:
                    	finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN2:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.HOTCARD_INQ:
                    	finalMessageType = IfxType.HOTCARD_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.GET_ACCOUNT:
                    	finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.GET_STATEMENT:
                    	finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ;
                    	break;
                    case ISOTransactionCodes.THIRDPARTY_PAYMENT:
                    	finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ;
                    	break;
                }
                break;

            case ISOMessageTypes.REVERSAL_ADVICE_RS_REPEAT_87:
                switch (emvTrnType) {
                    case ISOTransactionCodes.BALANCE_INQUERY:
                        finalMessageType = IfxType.BAL_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_87:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_93:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.PURCHASE:
                    	finalMessageType = IfxType.PURCHASE_REV_REPEAT_RS;
                    	break;

                    case ISOTransactionCodes.PURCHASECHARGE:
                    	finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RS;
                    	break;

                    case ISOTransactionCodes.WITHDRAWAL:
                        finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.TRANSFER_FROM_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.TRANSFER_TO_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.TRANSFER:
                        finalMessageType = IfxType.TRANSFER_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.RETURN:
                        finalMessageType = IfxType.RETURN_REV_REPEAT_RS;
                        break;
                    case ISOTransactionCodes.DEPOSITE:
                    	finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RS;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN2:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
                    	break;
                    case ISOTransactionCodes.HOTCARD_INQ:
                    	finalMessageType = IfxType.HOTCARD_REV_REPEAT_RS;
                    	break;
                    case ISOTransactionCodes.GET_ACCOUNT:
                    	finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RS;
                    	break;
                    case ISOTransactionCodes.GET_STATEMENT:
                    	finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RS;
                    	break;
                    case ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS;
                    	break;
                    case ISOTransactionCodes.THIRDPARTY_PAYMENT:
                    	finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS;
                    	break;
                }
                break;


            case ISOMessageTypes.REVERSAL_ADVICE_RS_87:
                switch (emvTrnType) {
                    case ISOTransactionCodes.BALANCE_INQUERY:
                        finalMessageType = IfxType.BAL_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_87:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.BILL_PAYMENT_93:
                        finalMessageType = IfxType.BILL_PMT_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.PURCHASE:
                    	finalMessageType = IfxType.PURCHASE_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.PURCHASECHARGE:
                    	finalMessageType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.WITHDRAWAL:
                        finalMessageType = IfxType.WITHDRAWAL_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.TRANSFER_FROM_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.TRANSFER_TO_ACCOUNT:
                        finalMessageType = IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.TRANSFER:
                        finalMessageType = IfxType.TRANSFER_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.RETURN:
                        finalMessageType = IfxType.RETURN_REV_REPEAT_RS;
                        ifx.setMti("0400");
                        break;
                    case ISOTransactionCodes.DEPOSITE:
                    	finalMessageType = IfxType.DEPOSIT_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.CHANGE_PIN2:
                    	finalMessageType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.HOTCARD_INQ:
                    	finalMessageType = IfxType.HOTCARD_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.GET_ACCOUNT:
                    	finalMessageType = IfxType.GET_ACCOUNT_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.GET_STATEMENT:
                    	finalMessageType = IfxType.BANK_STATEMENT_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT:
                    	finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                    case ISOTransactionCodes.THIRDPARTY_PAYMENT:
                    	finalMessageType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS;
                    	ifx.setMti("0400");
                    	break;
                }
                break;

            case ISOMessageTypes.TRANSFER_CHECK_ACCOUNT_RQ_87:
            	switch(emvTrnType){
            		case ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT:
            			finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;
            			break;
            		case ISOTransactionCodes.CHECK_ACCOUNT:
            			finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
            			break;
            	}
            	break;
//                finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;
//                break;

            case ISOMessageTypes.TRANSFER_CHECK_ACCOUNT_RS_87:
            	switch(emvTrnType){
        		case ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT:
        			finalMessageType = IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS;
        			break;
        		case ISOTransactionCodes.CHECK_ACCOUNT:
        			finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RS;
        			break;
            	}
            	break;
//                finalMessageType = IfxType.TRANSFER_CHECK_ACCOUNT_RS;
//                break;

            case ISOMessageTypes.ACQUIRER_RECONCILIATION_RQ_87:
                finalMessageType = IfxType.ACQUIRER_REC_RQ;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;
            case ISOMessageTypes.ACQUIRER_RECONCILIATION_RS_87:
                finalMessageType = IfxType.ACQUIRER_REC_RS;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;
            case ISOMessageTypes.ACQUIRER_RECONCILIATION_RQ_REPEAT_87:
                finalMessageType = IfxType.ACQUIRER_REC_REPEAT_RQ;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;
            case ISOMessageTypes.ACQUIRER_RECONCILIATION_RQ_REPEAT_RS_87:
                finalMessageType = IfxType.ACQUIRER_REC_REPEAT_RS;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;

            case ISOMessageTypes.ISSUER_RECONCILIATION_RQ_87:
                finalMessageType = IfxType.CARD_ISSUER_REC_RQ;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;
            case ISOMessageTypes.ISSUER_RECONCILIATION_RS_87:
                finalMessageType = IfxType.CARD_ISSUER_REC_RS;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;
            case ISOMessageTypes.ISSUER_RECONCILIATION_RQ_REPEAT_87:
                finalMessageType = IfxType.CARD_ISSUER_REC_REPEAT_RQ;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;
            case ISOMessageTypes.ISSUER_RECONCILIATION_RQ_REPEAT_RS_87:
                finalMessageType = IfxType.CARD_ISSUER_REC_REPEAT_RS;
                ifx.setTrnType(TrnType.RECONCILIATION);
                break;

            case ISOMessageTypes.NETWORK_MANAGEMENT_RQ_87:
                finalMessageType = IfxType.NETWORK_MGR_RQ;
                ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
                break;
            case ISOMessageTypes.NETWORK_MANAGEMENT_RS_87:
                finalMessageType = IfxType.NETWORK_MGR_RS;
                ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
                break;
            case ISOMessageTypes.NETWORK_MANAGEMENT_RQ_REPEAT_87:
                finalMessageType = IfxType.NETWORK_MGR_REPEAT_RQ;
                ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
                break;
            case ISOMessageTypes.NETWORK_MANAGEMENT_RQ_REPEAT_RS_87:
                finalMessageType = IfxType.NETWORK_MGR_REPEAT_RS;
                ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
                break;
        } //end
		*/
		//bkup end
		ifx.setIfxType(finalMessageType);

        
        if (finalMessageType == null){
        	ISOException isoe = new ISOException("Invalid Message Type." + mti + " Process Code: " + emvTrnType);
        	if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
        	logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        	ifx.setIfxType(IfxType.UNDEFINED); 
        }
	}

}

