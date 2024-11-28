package vaulsys.protocols.mehremihan;

import vaulsys.migration.MigrationDataService;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalService;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class MehreMihanIFXToISOMapper extends IfxToISOMapper {
	transient Logger logger = Logger.getLogger(MehreMihanIFXToISOMapper.class);
	public static final MehreMihanIFXToISOMapper Instance = new MehreMihanIFXToISOMapper();

	protected MehreMihanIFXToISOMapper() {
	}

	public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {

		ISOMsg isoMsg = new ISOMsg();
		ISOPackager packager = ((MehreMihanProtocol) ProtocolProvider.Instance.
				getByClass(MehreMihanProtocol.class)).getPackager();
		isoMsg.setPackager(packager);

		isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));

		isoMsg.set(2, ifx.getAppPAN());

		String processCode = "0000";

		isoMsg.set(3, mapTrnType(ifx.getTrnType()) + processCode);


		/***for return transaction***/
		if (mapTrnType(ifx.getTrnType()) + processCode == "200000")
			isoMsg.set(103, ifx.getSubsidiaryAccTo());

		if (ifx.getAuth_Amt() != null)
			isoMsg.set(4, ifx.getAuth_Amt().toString());

		if (ifx.getSec_Amt() == null && ifx.getAuth_Amt() != null)
			isoMsg.set(6, isoMsg.getString(4));
		else if (ifx.getSec_Amt() != null)
			isoMsg.set(6, ifx.getSec_Amt().toString());

		if (ifx.getTrnDt() != null)
			isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));
		isoMsg.set(10, ifx.getSec_CurRate());
		isoMsg.set(11, ifx.getSrc_TrnSeqCntr());
		isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
		isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));

		if (ifx.getExpDt() != null)
			isoMsg.set(14, ifx.getExpDt());

		if (ifx.getSettleDt() != null)
			isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));

		if (ifx.getPostedDt() != null)
			isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));

		isoMsg.set(25, fillTerminalType(ifx));
		isoMsg.set(32, ifx.getBankId().toString());

		isoMsg.set(33, ifx.getDestBankId().toString());
		if (ifx.getDestBankId() == "502908" && ifx.getAppPAN().substring(0, 7).equals("5029085"))
			isoMsg.set(33, 5029085L);
		isoMsg.set(35, ifx.getTrk2EquivData());

		isoMsg.set(37, fillFieldANFix(ifx, 37));

		isoMsg.set(38, ifx.getApprovalCode());

		isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));

		isoMsg.set(41, fillFieldANFix(ifx, 41));

		isoMsg.set(42, fillFieldANFix(ifx, 42));

		isoMsg.set(new ISOBinaryField(43, fillField43(ifx, convertor)));

		isoMsg.set(new ISOBinaryField(44, fillField44(ifx, convertor)));

		isoMsg.set(49, ifx.getAuth_Currency());
		isoMsg.set(51, (ifx.getSec_Currency() != null ? ifx.getSec_Currency() : ifx.getAuth_Currency()));

		if (ifx.getPINBlock() != null && !ifx.getPINBlock().equals(""))
			isoMsg.set(52, ifx.getPINBlock());

		if (ifx.getMode() != null && Util.hasText(ifx.getCheckDigit()))
			isoMsg.set(53, ifx.getMode().getType() + ifx.getCheckDigit() + "00000000000");

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
			if (acctBal.getAcctType().equals(AccType.CURRENT))
				strBal += "01";
			else if (acctBal.getAcctType().equals(AccType.SAVING))
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

		if (ifx.getNetworkManagementInformationCode() != null)
			isoMsg.set(70, ifx.getNetworkManagementInformationCode().getType());

		String S90 = "";

		if (ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
			if (ifx.getSafeOriginalDataElements().getMessageType() == null) {
				throw new ISOException("Invalid original data element: No Message Type for field 90");
			}

			S90 += ifx.getSafeOriginalDataElements().getMessageType();

//            S90 += trnSeqCntrFormat.format(ifx.getSafeOriginalDataElements().getTrnSeqCounter(), '0');
//            S90 += dateFormatMMDDhhmmss.format(ifx.getSafeOriginalDataElements().getOrigDt().toDate());
//            S90 += binFormat.format(ifx.getSafeOriginalDataElements().getBankId(), '0');
//            S90 += binFormat.format(ifx.getSafeOriginalDataElements().getFwdBankId(), '0');
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
				S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getBankId(), '0');
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

/*        if (ifx.getRecvBankId() == null || ifx.getRecvBankId().equals(""))
			if(ifx.getDestBankId() == 502908L && ifx.getAppPAN().substring(0, 7).equals("5029085"))
			isoMsg.set(100, 5029085L);
		else
			isoMsg.set(100, ifx.getRecvBankId().toString());*/

//		isoMsg.set(15, ifx.getExtISO.P15);//

//        isoMsg.set(18, "0743");

//        isoMsg.set(22, ((ExtISO)ifx.getIFX_Ext()).P22);
//        isoMsg.set(24, ((ExtISO)ifx.getIFX_Ext()).P24);
//        isoMsg.set(30, ((ExtISO)ifx.getIFX_Ext()).P30);
//		isoMsg.set(39, ((ExtISO)ifx.getIFX_Ext()).P39);//
//        isoMsg.set(50, ((ExtISO)ifx.getIFX_Ext()).P50);
//        isoMsg.set(53, ((ExtISO)ifx.getIFX_Ext()).P53);
//        isoMsg.set(56, ((ExtISO)ifx.getIFX_Ext()).P56);
//        isoMsg.set(74, ((ExtISO)ifx.getIFX_Ext()).P74);
//        isoMsg.set(75, ((ExtISO)ifx.getIFX_Ext()).P75);
//        isoMsg.set(76, ((ExtISO)ifx.getIFX_Ext()).P76);
//        isoMsg.set(77, ((ExtISO)ifx.getIFX_Ext()).P77);
//        isoMsg.set(78, ((ExtISO)ifx.getIFX_Ext()).P78);
//        isoMsg.set(79, ((ExtISO)ifx.getIFX_Ext()).P79);
//        isoMsg.set(81, ((ExtISO)ifx.getIFX_Ext()).P80);
//        isoMsg.set(86, ((ExtISO)ifx.getIFX_Ext()).P86);
//        isoMsg.set(87, ((ExtISO)ifx.getIFX_Ext()).P87);
//        isoMsg.set(88, ((ExtISO)ifx.getIFX_Ext()).P88);
//        isoMsg.set(89, ((ExtISO)ifx.getIFX_Ext()).P89);
//        isoMsg.set(93, ((ExtISO)ifx.getIFX_Ext()).P93);
//        isoMsg.set(94, ((ExtISO)ifx.getIFX_Ext()).P94);
//        isoMsg.set(96, ((ExtISO)ifx.getIFX_Ext()).P96);
//        isoMsg.set(97, ((ExtISO)ifx.getIFX_Ext()).P97);
//        isoMsg.set(102, ((ExtISO)ifx.getIFX_Ext()).P102);
//		isoMsg.set(124, ((ExtISO)ifx.getIFX_Ext()).P124);

		if (isoMsg.getMaxField() > 64) {
			isoMsg.set(128, ifx.getMsgAuthCode());
			isoMsg.unset(64);
		} else {
			isoMsg.set(64, ifx.getMsgAuthCode());
			isoMsg.unset(128);
		}

		if (ifx.getKeyManagement() != null && Util.hasText(ifx.getKeyManagement().getKey()))
			isoMsg.set(new ISOBinaryField(96, Hex.decode(ifx.getKeyManagement().getKey())));


		StringBuilder CVV2 = new StringBuilder();
		StringBuilder secAppPAN = new StringBuilder();
		try {
			MigrationDataService.setChangedFields(ifx, isoMsg, CVV2, secAppPAN);

		} catch (Exception e) {
			logger.error("Exception in return changed field, ifx: " + ifx.getId(), e);
		}

		/****** Don't move this line, must be haminja! ******/
		isoMsg.set(new ISOBinaryField(48, fillField48(ifx, CVV2.toString(), secAppPAN.toString(), convertor)));

		return isoMsg;
	}

	@Override
	public byte[] fillField43(Ifx ifx, EncodingConvertor convertor) {
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
//		if (ifx.getCity() != null)
//		 city = ifx.getCity().getName();
//		if (Util.hasText(city))
//			city = city.substring(0, Math.min(13, city.length()));
//		else 
//			city = " ";
		city = StringFormat.formatNew(13, StringFormat.JUST_LEFT, city, ' ');
//		city = city.replaceAll("ی", "ي");
//		city = city.replaceAll("ء", "ئ");
//		city = city.replaceAll("ک", "ك");

		try {
			System.arraycopy(city.getBytes("windows-1256"), 0, result, 22, 13);
		} catch (UnsupportedEncodingException e) {
			System.arraycopy(city.getBytes(), 0, result, 22, 13);
		}

		String state = " ";
//		if (ifx.getStateProv() != null) {
//			state = (Util.hasText(ifx.getStateProv().getAbbreviation())) ? ifx.getStateProv().getAbbreviation() : "";
//		}
		state = StringFormat.formatNew(3, StringFormat.JUST_LEFT, state, ' ');
//		state = state.replaceAll("ی", "ي");
//		state = state.replaceAll("ء", "ئ");
//		state = state.replaceAll("ک", "ك");
		System.arraycopy(state.getBytes(), 0, result, 35, 3);

		String country = " ";
//		if (ifx.getCountry() != null) {
//			country = (Util.hasText(ifx.getCountry().getAbbreviation())) ? ifx.getCountry().getAbbreviation() : "";
//		}
		country = StringFormat.formatNew(2, StringFormat.JUST_LEFT, country, ' ');
//		country = country.replaceAll("ی", "ي");
//		country = country.replaceAll("ء", "ئ");
//		country = country.replaceAll("ک", "ك");
		System.arraycopy(country.getBytes(), 0, result, 38, 2);

		return result;
	}

	@Override
	public byte[] fillField44(Ifx ifx, EncodingConvertor convertor) {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
				&& ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
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
			return (finalBytes.size() == 0) ? null : finalBytes.toByteArray();
		}
		return null;
	}


	public byte[] fillField48(Ifx ifx, String CVV2, String secAppPAN, EncodingConvertor convertor) {
		String p48 = "";
		p48 += "00";
		p48 += StringFormat.formatNew(4, StringFormat.JUST_RIGHT, CVV2, '0');

		if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())
				&& ifx.getBankId().equals(ProcessContext.get().getMyInstitution().getBin())
				&& !TerminalService.isOriginatorSwitchTerminal(ifx.getTransaction().getInputMessage())) {
			p48 += StringFormat.formatNew(10, StringFormat.JUST_RIGHT, "0", '0');
			p48 += ifx.getBillOrgType().getType();
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0');
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0');
		} else if (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
			if (
					(IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_REV_REPEAT_RS.equals(ifx.getIfxType()))
							&& !Util.hasText(secAppPAN)
					) {
				p48 = "";
			} else {
				UserLanguage userLanguage = ifx.getUserLanguage();
				if (UserLanguage.FARSI_LANG.equals(userLanguage))
					p48 += "00";
				else
					p48 += "01";
				String appPan = secAppPAN;
				if (Util.hasText(secAppPAN)) {
					p48 += appPan.length();
					p48 += appPan;
				} else {
					logger.error(ifx.getIfxType() + " doesn't have SecAppPan " + secAppPAN + "!");
				}
			}
		} else if (ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType())) {

			if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
//					if (TerminalType.INTERNET.equals(ifx.getTerminalType())) {
//						p48 += "00" + ifx.getNewPINBlock();
//					} else {
				p48 = ifx.getNewPINBlock();
//					}
			}
		}
		return (!Util.hasText(p48) ? null : (p48.getBytes()));
	}

}
