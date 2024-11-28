package vaulsys.protocols.PaymentSchemes.ISO8583;

import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class IfxToISOMapper implements IfxToProtocolMapper {

	@Override
	public String mapError(IfxType type, String rsCode) {
		if (ISOResponseCodes.RESTRICTED_MIN_WITHDRAWAL_AMOUNT.equals(rsCode)){
			return ISOResponseCodes.BANK_LINK_DOWN;
		}
		
		if(rsCode != null && rsCode.length() > 2){
			return ISOResponseCodes.CUSTOMER_RELATION_NOT_FOUND;
		}
		
		return rsCode;
	}

	public int fillTerminalType(Ifx ifx) {
		return ifx.getTerminalType().getCode();
	}

	 public String fillFieldANFix(Ifx ifx, int fieldId) {
        if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())){ //&&
        		//ProcessContext.get().isMyInstitution(ifx.getInstitutionId()) //ProcessContext.get().getInputMessage().getChannel().getInstitution()) //ifx.getBankId()) //Raza use Channel Institution //Raza commenting need to FIX
        		//&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getInstitution(ifx.getInstitutionId()).getRole())) { //ProcessContext.get().getMyInstitutionByBIN(ifx.getBankId()).getRole())) { //Raza use Channel Institution //Raza commenting need to FIX
        	if (fieldId == 37) {
        		return StringFormat.formatNew(12, StringFormat.JUST_RIGHT, ifx.getMyNetworkRefId(), '0');
        	}
        	if (fieldId == 41) {
        		return StringFormat.formatNew(8, StringFormat.JUST_LEFT, ifx.getTerminalId(), ' ');
        	}
        	if (fieldId == 42) {
        		return StringFormat.formatNew(15, StringFormat.JUST_LEFT, ifx.getOrgIdNum(), ' ');
        	}
        } else {
        	if (fieldId == 37) {
                Transaction firstTrx = ifx.getTransaction().getFirstTransaction();
                if(firstTrx != null &&
                        (ifx.getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RS) ||
                                ifx.getIfxType().equals(IfxType.TRANSFER_RS) ||
                                ifx.getIfxType().equals(IfxType.TRANSFER_REV_REPEAT_RS) ||
                                ifx.getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS))
                        &&
                        (firstTrx.getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RQ) ||
                                firstTrx.getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_RQ) ||
                                firstTrx.getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_REV_REPEAT_RQ) ||
                                firstTrx.getIncomingIfx().getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ))){
                    //logger.info("ISO v7: changed from: " + ifx.getNetworkRefId() + ", to: "+firstTrx.getIncomingIfx().getNetworkRefId());
                    //return firstTrx.getIncomingIfx().getNetworkRefId();
                    return null;
                }

                return ifx.getNetworkRefId();
        	}
        	if (fieldId == 41) {
        		return ifx.getTerminalId();
        	}
        	if (fieldId == 42) {
        		return ifx.getOrgIdNum();
        	}
        }
        return "";
    }

	public String fillField43(Ifx ifx) {
		String name = (Util.hasText(ifx.getName())) ? ifx.getName() : "";
		String final43 = name;

		if (final43.length() < 40) {
			for (int i = final43.length(); i < 40; i++)
				final43 = final43 + " ";
		}

		return final43;
	}

	public byte[] fillField43(Ifx ifx, EncodingConvertor convertor) {
		StringBuilder field43 = new StringBuilder();
		String name = (Util.hasText(ifx.getName())) ? ifx.getName() : "";
		field43.append(StringFormat.formatNew(22, StringFormat.JUST_LEFT, name, ' '));

//		String city = " ";
		field43.append("             ");

//		String state = "";
		field43.append("   ");

//		String country = "";
		field43.append("  ");

		if ("".equals(field43))
			return new byte[40];

		byte[] encode = convertor.encode(field43.toString());
		byte[] final43 = new byte[40];

		if (encode.length < 40) {
			System.arraycopy(encode, 0, final43, 0, encode.length);
			for (int i = encode.length; i < 40; i++)
				final43[i] = 32;
		} else {
			System.arraycopy(encode, 0, final43, 0, 40);
		}

		return final43;
	}

	public byte[] fillField44(Ifx ifx, EncodingConvertor convertor) {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType()) || 
				IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
			try {
				for (int i = 0; i < 25; i++)
					finalBytes.write(32);
				byte[] name = null;
				byte[] family = null;

				if (UserLanguage.ENGLISH_LANG.equals(ifx.getUserLanguage())) {
					if (ifx.getCardHolderName() != null)
						name = ifx.getCardHolderName().toUpperCase().getBytes();
					if (ifx.getCardHolderFamily() != null)
					family = ifx.getCardHolderFamily().toUpperCase().getBytes();
				} else {
					name = convertor.encode(ifx.getCardHolderName());
					family = convertor.encode(ifx.getCardHolderFamily());
				}

				finalBytes.write(convertor.finalize(name, null, null));
				finalBytes.write(convertor.finalize(family, null, null));

			} catch (IOException e) {
			}
			return (finalBytes.size()==0)? null : finalBytes.toByteArray();
		}else if (IfxType.TRANSFER_RS.equals(ifx.getIfxType())) {
			return new byte[0];
		}
		return null;
	}
	
	public byte[] fillField48(Ifx ifx, EncodingConvertor convertor) {
		return "F_48".getBytes();
	}


	public String mapTrnType(TrnType trnType) {
		String processCode = "00";
		if (TrnType.PURCHASE.equals(trnType))
			processCode = ISOTransactionCodes.PURCHASE;
		else if (TrnType.WITHDRAWAL.equals(trnType))
			processCode = ISOTransactionCodes.WITHDRAWAL;
		else if (TrnType.BALANCEINQUIRY.equals(trnType))
			processCode = ISOTransactionCodes.BALANCE_INQUERY;
		else if (TrnType.BILLPAYMENT.equals(trnType) || TrnType.SADERAT_BILLPAYMENT.equals(trnType))
			processCode = ISOTransactionCodes.BILL_PAYMENT_87;
            /*else if (TrnType.RETURN.equals( trnType)) //Raza commenting - handled through REFUND
	            processCode = ISOTransactionTypes.getString(ISOTransactionTypes.RETURN, 2);*/
		else if (TrnType.CHECKACCOUNT.equals(trnType))
			processCode = ISOTransactionCodes.CHECK_ACCOUNT;
		else if (TrnType.TRANSFER.equals(trnType))
			processCode = ISOTransactionCodes.TRANSFER;
		else if (TrnType.DECREMENTALTRANSFER.equals(trnType))
			processCode = ISOTransactionCodes.TRANSFER_FROM_ACCOUNT;
		else if (TrnType.INCREMENTALTRANSFER.equals(trnType))
			processCode = ISOTransactionCodes.TRANSFER_TO_ACCOUNT;
		else if (TrnType.PURCHASECHARGE.equals(trnType))
			processCode = ISOTransactionCodes.PURCHASECHARGE;
		else if (TrnType.LASTPURCHASECHARGE.equals(trnType))
			processCode = ISOTransactionCodes.LASTPURCHASECHARGE;
		else if (TrnType.PURCHASETOPUP.equals(trnType))
			processCode = ISOTransactionCodes.PURCHASETOPUP;
		else if (TrnType.BANKSTATEMENT.equals(trnType))
			processCode = ISOTransactionCodes.GET_STATEMENT;
		else if (TrnType.CHANGEINTERNETPINBLOCK.equals(trnType))
			processCode = ISOTransactionCodes.CHANGE_PIN2;
		else if (TrnType.CHANGEPINBLOCK.equals(trnType))
			processCode = ISOTransactionCodes.CHANGE_PIN;
		else if (TrnType.GETACCOUNT.equals(trnType))
			processCode = ISOTransactionCodes.GET_ACCOUNT;
		else if (TrnType.DEPOSIT.equals(trnType))
			processCode = ISOTransactionCodes.DEPOSITE;
		else if (TrnType.DEPOSIT_CHECK_ACCOUNT.equals(trnType))
			processCode = ISOTransactionCodes.DEPOSIT_CHECK_ACCOUNT;
		else if (TrnType.SADERAT_AUTH_BILLPAYMENT.equals(trnType))
			processCode = ISOTransactionCodes.SDAERAT_AUTH_BILL;
		else if (TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(trnType))
			processCode = ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT;
		else if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(trnType))
			processCode = ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT;
		else if (TrnType.THIRD_PARTY_PAYMENT.equals(trnType))
			processCode = ISOTransactionCodes.THIRDPARTY_PAYMENT;
		else if (TrnType.REFUND.equals(trnType))
			processCode = ISOTransactionCodes.REFUND;
		else if (TrnType.PREAUTH.equals(trnType))
			processCode = ISOTransactionCodes.PREAUTH;
		else if (TrnType.DIRECT_DEBIT.equals(trnType))
			processCode = ISOTransactionCodes.DIRECT_DEBIT;
		else if (TrnType.MONEY_SEND.equals(trnType))
			processCode = ISOTransactionCodes.MONEY_SEND;
		else if (TrnType.IBFT.equals(trnType))
			processCode = ISOTransactionCodes.IBFT;
		else if (TrnType.TITLE_FETCH.equals(trnType))
			processCode = ISOTransactionCodes.TITLE_FETCH;
		else if (TrnType.ORIGINAL_CREDIT.equals(trnType))
			processCode = ISOTransactionCodes.ORIGINAL_CREDIT;
		else if (TrnType.VOID.equals(trnType)) //Raza adding for KEENU
			processCode = ISOTransactionCodes.ADJUSTMENT;

		return processCode;
	}


	public String getPOSEntryMode(Ifx ifxObj)
	{
		Boolean isPINAvailable, isTrack2DataAvailable;
		String posEntryMode;

		isTrack2DataAvailable = Util.hasText(ifxObj.getTrk2EquivData());
		isPINAvailable = Util.hasText(ifxObj.getPINBlock());

		if (isTrack2DataAvailable) {
			posEntryMode = "90";
		} else {
			posEntryMode = "01";
		}

		if (isPINAvailable)
			posEntryMode += "1";
		else
			posEntryMode += "2";

		return posEntryMode;
	}
	
	public String fillMTI(IfxType ifxType, String firstMTI){
		
        String mti = "0";
		System.out.println("fillMTI:: ifxType [" + ifxType + "] firstMTI [" + firstMTI + "]"); //Raza TEMP



		if (ifxType.equals(IfxType.BAL_INQ_RQ)
                || ifxType.equals(IfxType.BILL_PMT_RQ)
                || ifxType.equals(IfxType.PURCHASE_RQ)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_RQ)
                || ifxType.equals(IfxType.PURCHASE_TOPUP_RQ)
                || ifxType.equals(IfxType.LAST_PURCHASE_CHARGE_RQ)
                || ifxType.equals(IfxType.WITHDRAWAL_RQ)
                || ifxType.equals(IfxType.RETURN_RQ)
                || ifxType.equals(IfxType.TRANSFER_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_RQ)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ)
                || ifxType.equals(IfxType.DEPOSIT_RQ)		
                || ifxType.equals(IfxType.DEPOSIT_CHECK_ACCOUNT_RQ)		
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_RQ)		
                || ifxType.equals(IfxType.GET_ACCOUNT_RQ)		
                || ifxType.equals(IfxType.BANK_STATEMENT_RQ)		
                || ifxType.equals(IfxType.CREDIT_PURCHASE_RQ)
                || ifxType.equals(IfxType.CREDIT_BAL_INQ_RQ)
                || ifxType.equals(IfxType.SADERAT_BILL_PMT_RQ)
                || ifxType.equals(IfxType.SORUSH_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.SHAPARAK_CONFIRM_RQ)
                || ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_RQ)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_RQ)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_RQ)
				|| ifxType.equals(IfxType.PURCHASE_CANCEL_RQ)
                ){
			if ("400".equals(firstMTI))
				mti = ISOMessageTypes.REVERSAL_ADVICE_87;
			else if ("220".equals(firstMTI))
				mti = ISOMessageTypes.FINANCIAL_ADVICE_87;
			else
				mti = ISOMessageTypes.FINANCIAL_REQUEST_87;
		}else if (ifxType.equals(IfxType.BAL_INQ_RS)
                || ifxType.equals(IfxType.BILL_PMT_RS)
                || ifxType.equals(IfxType.PURCHASE_RS)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_RS)
                || ifxType.equals(IfxType.LAST_PURCHASE_CHARGE_RS)
                || ifxType.equals(IfxType.PURCHASE_TOPUP_RS)                
                || ifxType.equals(IfxType.WITHDRAWAL_RS)
                || ifxType.equals(IfxType.RETURN_RS)
                || ifxType.equals(IfxType.TRANSFER_RS)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_RS)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS)
                || ifxType.equals(IfxType.DEPOSIT_RS)		
                || ifxType.equals(IfxType.DEPOSIT_CHECK_ACCOUNT_RS)		
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_RS)		
                || ifxType.equals(IfxType.GET_ACCOUNT_RS)		
                || ifxType.equals(IfxType.BANK_STATEMENT_RS)
                || ifxType.equals(IfxType.CREDIT_PURCHASE_RS)
                || ifxType.equals(IfxType.CREDIT_BAL_INQ_RS)
                || ifxType.equals(IfxType.SADERAT_BILL_PMT_RS)
                || ifxType.equals(IfxType.SORUSH_REV_REPEAT_RS)
                || ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_RS)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_RS)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_RS)
				|| ifxType.equals(IfxType.PURCHASE_CANCEL_RS)
                ){
        	if ("400".equals(firstMTI))
        		mti = ISOMessageTypes.REVERSAL_RESPONSE_87;
        	
        	else if ("220".equals(firstMTI))
        		mti = ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87;
        	
			else
				mti = ISOMessageTypes.FINANCIAL_RESPONSE_87;
        }else if (ifxType.equals(IfxType.BAL_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.BILL_PMT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PURCHASE_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.WITHDRAWAL_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.RETURN_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ)
                || ifxType.equals(IfxType.DEPOSIT_REV_REPEAT_RQ)		
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ)		
                || ifxType.equals(IfxType.GET_ACCOUNT_REV_REPEAT_RQ)		
                || ifxType.equals(IfxType.BANK_STATEMENT_REV_REPEAT_RQ)		
                || ifxType.equals(IfxType.CREDIT_PURCHASE_REV_REPEAT_RQ)
				|| ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_REV_REPEAT_RQ)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ)
				|| ifxType.equals(IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ)
				|| ifxType.equals(IfxType.PURCHASE_CANCEL_REV_REPEAT_RQ)
				|| ifxType.equals(IfxType.PREAUTH_REV_REPEAT_RQ)
                )
        	if ("400".equals(firstMTI))
        		mti = ISOMessageTypes.REVERSAL_ADVICE_87;
			else
				mti = ISOMessageTypes.REVERSAL_ADVICE_REPEAT_87;

        else if (ifxType.equals(IfxType.BAL_REV_REPEAT_RS)
                || ifxType.equals(IfxType.BILL_PMT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PURCHASE_REV_REPEAT_RS)
                || ifxType.equals(IfxType.PURCHASE_CHARGE_REV_REPEAT_RS)
                || ifxType.equals(IfxType.WITHDRAWAL_REV_REPEAT_RS)
                || ifxType.equals(IfxType.RETURN_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.DEPOSIT_REV_REPEAT_RS)		
                || ifxType.equals(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS)		
                || ifxType.equals(IfxType.GET_ACCOUNT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.BANK_STATEMENT_REV_REPEAT_RS)
                || ifxType.equals(IfxType.CREDIT_PURCHASE_REV_REPEAT_RS)
                || ifxType.equals(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_REV_REPEAT_RS)
				|| ifxType.equals(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS)
				|| ifxType.equals(IfxType.PREAUTH_CANCEL_REV_REPEAT_RS)
				|| ifxType.equals(IfxType.PURCHASE_CANCEL_REV_REPEAT_RS)
				|| ifxType.equals(IfxType.PREAUTH_REV_REPEAT_RS)
                )
        	if ("400".equals(firstMTI))
        		mti = ISOMessageTypes.REVERSAL_RESPONSE_87;
			else
				mti = ISOMessageTypes.REVERSAL_ADVICE_RESPONSE_87;

        else if (ifxType.equals(IfxType.RECONCILIATION_RQ)|| ifxType.equals(IfxType.ACQUIRER_REC_RQ))
            mti = ISOMessageTypes.ACQUIRER_RECON_REQUEST_87;
		
        else if (ifxType.equals(IfxType.RECONCILIATION_REPEAT_RQ) || ifxType.equals(IfxType.ACQUIRER_REC_REPEAT_RQ))
            mti = ISOMessageTypes.ACQUIRER_RECON_ADVICE_87;
        else if (ifxType.equals(IfxType.RECONCILIATION_RS) || ifxType.equals(IfxType.ACQUIRER_REC_RS))
            mti = ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87;
        else if (ifxType.equals(IfxType.CARD_ISSUER_REC_RQ))
        	mti = ISOMessageTypes.ISSUER_RECON_REQUEST_87;
        else if (ifxType.equals(IfxType.CARD_ISSUER_REC_REPEAT_RQ))
        	mti = ISOMessageTypes.ISSUER_RECON_ADVICE_87;
        else if (ifxType.equals(IfxType.CUTOVER_RQ))
        	mti = ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87;
        else if (ifxType.equals(IfxType.CUTOVER_REPEAT_RQ))
        	mti = ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87;
        
        else if (ifxType.equals(IfxType.TRANSFER_CHECK_ACCOUNT_RQ) || 
        		IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RQ.equals(ifxType) ||
        		ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ) ||
				ifxType.equals(IfxType.PREAUTH_RQ) ||
				ifxType.equals(IfxType.PREAUTH_CANCEL_RQ)
        		)
            mti = ISOMessageTypes.AUTHORIZATION_REQUEST_87;
		
        else if (ifxType.equals(IfxType.TRANSFER_CHECK_ACCOUNT_RS) || 
        		IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RS.equals(ifxType) ||
        		ifxType.equals(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS) ||
		  		ifxType.equals(IfxType.PREAUTH_RS) ||
				ifxType.equals(IfxType.PREAUTH_CANCEL_RS)
        		)
            mti = ISOMessageTypes.AUTHORIZATION_RESPONSE_87;

		else if(ifxType.equals(IfxType.REFUND_ADVICE_RQ) ||
				ifxType.equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ))
			mti = ISOMessageTypes.FINANCIAL_ADVICE_87;

		else if(ifxType.equals(IfxType.REFUND_ADVICE_RS) ||
				ifxType.equals(IfxType.PREAUTH_COMPLET_ADVICE_RS))
			mti = ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87;
		//m.rehman: for loro reversal messages
		else
			mti = firstMTI;

		System.out.println("Got MTI here [" + mti + "]"); //Raza TEMP
		return mti;
	}
}
