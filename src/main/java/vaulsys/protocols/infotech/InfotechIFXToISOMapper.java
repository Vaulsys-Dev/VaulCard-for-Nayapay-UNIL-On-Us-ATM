package vaulsys.protocols.infotech;

import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.mtn.MTNChargeService;
import vaulsys.mtn.exception.NoChargeAvailableException;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

public class InfotechIFXToISOMapper extends IfxToISOMapper{
	public static final InfotechIFXToISOMapper Instance = new InfotechIFXToISOMapper();
	
	private InfotechIFXToISOMapper(){}
	
    @Transient
    private static Logger logger = Logger.getLogger(InfotechIFXToISOMapper.class);

    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws Exception {
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager packager = ((InfotechProtocol) ProtocolProvider.Instance.getByClass(InfotechProtocol.class)).getPackager();
        isoMsg.setPackager(packager);

        isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));
        isoMsg.set(2, ifx.getAppPAN());

        String processCode = "";
        processCode = "0000";

        isoMsg.set(3, mapTrnType(ifx.getTrnType())+ processCode);

        
        if (ifx.getAuth_Amt() != null && (
        		!TrnType.CHECKACCOUNT.equals(ifx.getTrnType()) || !TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType())))
			isoMsg.set(4, ifx.getAuth_Amt().toString());
        
		isoMsg.set(7, MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate()));
        isoMsg.set(11, ifx.getSrc_TrnSeqCntr());

        Date currentDate = Calendar.getInstance().getTime();

        isoMsg.set(12, MyDateFormatNew.format("HHmmss", currentDate));
        isoMsg.set(13, MyDateFormatNew.format("yyyyMMdd", currentDate));

        isoMsg.set(25, fillTerminalType(ifx));

        isoMsg.set(32, ifx.getBankId().toString());
        if(ifx.getDestBankId() != null)  /**** Avoid null pointer exception ****/
        	isoMsg.set(33, ifx.getDestBankId().toString());
        isoMsg.set(35, ifx.getTrk2EquivData());
        isoMsg.set(37, !Util.hasText(ifx.getNetworkRefId()) ? ifx.getSrc_TrnSeqCntr() : ifx.getNetworkRefId());
        
        isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));
        
        isoMsg.set(41, ifx.getTerminalId());
        isoMsg.set(42, ifx.getOrgIdNum());
        
        isoMsg.set(new ISOBinaryField(44, fillField44(ifx, convertor)));
        
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

        if (ifx.getThirdPartyCode() != null)
        	isoMsg.set(98, ifx.getThirdPartyCode());
        
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
			 * This condition only for older infotech pos with old application version added!
			 ***/
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
		//if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType()) || TrnType.CHECKACCOUNT.equals(ifx.getTrnType())
		if (ISOFinalMessageType.isFinancialMessage(ifx.getIfxType(),false) || TrnType.CHECKACCOUNT.equals(ifx.getTrnType())
				|| TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType()) || TrnType.BANKSTATEMENT.equals(ifx.getTrnType())) {
			ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
			try {
				Terminal t = TerminalService.findTerminal(Terminal.class, Long.valueOf(ifx.getTerminalId()));
				byte[] field48Rs = TerminalService.generalInfotechField48Rs(ifx, convertor, t);
				
				finalBytes.write(field48Rs);

					if (ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
						try {
							String refundDate = MyDateFormatNew.format("yyyyMMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate());
							finalBytes.write(refundDate.getBytes());
							finalBytes.write(ASCIIConstants.FS);
						} catch (Exception e) {
							logger.warn("try to find OridDt of referenceTrx: " + e +" ignoring...");
						}
						
					} else if (IfxType.PURCHASE_CHARGE_RS.equals(ifx.getIfxType()) && ifx.getChargeData() != null && ISOResponseCodes.isSuccess(ifx.getRsCode())) {
						Set<SecureKey> keySet = t.getKeySet();
						try {
							
							finalBytes.write(("IR"+ifx.getChargeData().getCharge().getCardSerialNo()).toString().getBytes());
							finalBytes.write(ASCIIConstants.FS);
							
							byte[] decryptedPIN = SecurityComponent.rsaDecrypt(Hex.decode(ifx.getChargeData().getCharge().getCardPIN()));
							
							byte[] reencryptedPIN = SecurityComponent.encrypt(decryptedPIN, SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet));
							
							finalBytes.write(new String(Hex.encode(reencryptedPIN)).getBytes());
							finalBytes.write(ASCIIConstants.FS);

							String credit = MTNChargeService.getRealChargeCredit(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode()).toString();
							finalBytes.write(credit.getBytes());
							finalBytes.write(ASCIIConstants.FS);
							
						} catch (Exception e) {
							throw new NoChargeAvailableException(e);
						}
					} else if (IfxType.BILL_PMT_RS.equals(ifx.getIfxType())) {
						Integer companyCode = ifx.getBillCompanyCode();
						String billID = ifx.getBillID();
						
						if(companyCode != null){
							Organization org = OrganizationService.findOrganizationByCompanyCode(companyCode, BillPaymentUtil.extractBillOrgType(billID));
							if(org != null){						
								finalBytes.write(convertor.encode(org.getName()));
								finalBytes.write(ASCIIConstants.FS);
							}
						}
					} else if (IfxType.THIRD_PARTY_PURCHASE_RS.equals(ifx.getIfxType())){
						Long thirdPartyCode = ifx.getThirdPartyCode();
						if(thirdPartyCode != null){
							Organization org = OrganizationService.findOrganizationByCode(thirdPartyCode, OrganizationType.THIRDPARTYPURCHASE);
							if(org != null){
								finalBytes.write(convertor.encode(org.getName()));
								finalBytes.write(ASCIIConstants.FS);
							}
						}
					} else if(IfxType.BANK_STATEMENT_RS.equals(ifx.getIfxType())){
						if(ifx.getBankStatementData()!= null){
				        	for(BankStatementData d:ifx.getBankStatementData()){
				        		finalBytes.write(MyDateFormatNew.format("yyyyMMddHHmmss", d.getTrxDt().toDate()).getBytes());
				        		finalBytes.write(ASCIIConstants.FS);
				        		
				        		finalBytes.write(d.getAmount().toString().getBytes());
				        		finalBytes.write(ASCIIConstants.FS);
				        		
				        		finalBytes.write(d.getTrnType().getBytes());
				        		finalBytes.write(ASCIIConstants.FS);
				        		
				        		finalBytes.write(d.getBalance().toString().getBytes());
				        		finalBytes.write(ASCIIConstants.FS);
				        		
//				        		finalBytes.write(convertor.encode(d.getDescription()));
//				        		finalBytes.write(ASCIIConstants.FS);
				        		
				        	}
			        	}
					}
					
				return finalBytes.toByteArray();
			} catch (Exception e) {
				logger.error("Encoutering an Exception in filling field_48: "+ e.getClass().getSimpleName()+":"+ e.getMessage());
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc("Encoutering an Exception in filling field_48: "+ e.getClass().getSimpleName()+":"+ e.getMessage());
				return finalBytes.toByteArray();
			}
		} else {
			logger.error("Bad situation: terminal["+ ifx.getTerminalId()+"] could not be found!");
			return null;
		}
	}
	@Override
	public byte[] fillField44(Ifx ifx, EncodingConvertor convertor) {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())
				|| IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
			try {
				if (ifx.getCardHolderName() != null)
					finalBytes.write(convertor.encode(ifx.getCardHolderName()));
				else 
					finalBytes.write(convertor.encode(""));
				finalBytes.write(ASCIIConstants.FS);
				if (ifx.getCardHolderFamily() != null)
					finalBytes.write(convertor.encode(ifx.getCardHolderFamily()));
				else 
					finalBytes.write(convertor.encode(""));
				finalBytes.write(ASCIIConstants.FS);
//				for (int i = 0; i < 25; i++)
//					finalBytes.write(32);
//				byte[] name = null;
//				byte[] family = null;
//
//				if (UserLanguage.ENGLISH_LANG.equals(ifx.getUserLanguage())) {
//					if (ifx.getCardHolderName() != null)
//						name = ifx.getCardHolderName().toUpperCase().getBytes();
//					if (ifx.getCardHolderFamily() != null)
//					family = ifx.getCardHolderFamily().toUpperCase().getBytes();
//				} else {
//					name = convertor.encode(ifx.getCardHolderName());
//					family = convertor.encode(ifx.getCardHolderFamily());
//				}
//
//				finalBytes.write(convertor.finalize(name, null, null));
//				finalBytes.write(convertor.finalize(family, null, null));

			} catch (IOException e) {
			}
			return (finalBytes.size()==0)? null : finalBytes.toByteArray();
		}else if (IfxType.TRANSFER_RS.equals(ifx.getIfxType())) {
			return new byte[0];
		}
		return null;
	}
}

