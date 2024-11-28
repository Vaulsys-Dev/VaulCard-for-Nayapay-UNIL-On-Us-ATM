package vaulsys.protocols.mizan;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import org.apache.log4j.Logger;

public class MizanIfxToISOMapper extends IfxToISOMapper {
	private static final Logger logger = Logger.getLogger(MizanIfxToISOMapper.class);
	public static final MizanIfxToISOMapper Instance = new MizanIfxToISOMapper();

	private MizanIfxToISOMapper() {
	}

	@Override
	public ProtocolMessage map(Ifx ifx, EncodingConvertor convertor) throws Exception {
		ISOMsg isoMsg = new ISOMsg();
		ISOPackager packager = ((MizanProtocol) ProtocolProvider.Instance
				.getByClass(MizanProtocol.class)).getPackager();
		isoMsg.setPackager(packager);

		isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));

		// P2
		isoMsg.set(2, ifx.getAppPAN());

		// P3
		String p3 = null;
		if (TrnType.PURCHASE.equals(ifx.getTrnType()))
			p3 = "003000";
		else
			p3 = "313000";
		isoMsg.set(3, p3);

		// P4
		if (ifx.getAuth_Amt() != null)
			isoMsg.set(4, ifx.getAuth_Amt().toString());

		// P6 -> ignored

		// P7
		if (ifx.getTrnDt() != null)
			isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));

		// P10 -> ignored

		// P11
		isoMsg.set(11, ifx.getSrc_TrnSeqCntr());

		// P12 & P13
		isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
		isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));

		// P14
		if (ifx.getExpDt() != null)
			isoMsg.set(14, ifx.getExpDt());

		// P17
		if (ifx.getPostedDt() != null)
			isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));

		// P25
		isoMsg.set(25, "14");

		// P32 & P33
		isoMsg.set(32, ifx.getBankId().toString());
		isoMsg.set(33, ifx.getDestBankId().toString());

		// P35
		isoMsg.set(35, ifx.getTrk2EquivData());

		// P37
		isoMsg.set(37, ifx.getNetworkRefId());

		// P38
		isoMsg.set(38, ifx.getApprovalCode());

		// P39
		isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));

		// P41
		isoMsg.set(41, ifx.getTerminalId());

		// P42
		isoMsg.set(42, ifx.getOrgIdNum());

		// P43
		//isoMsg.set(new ISOBinaryField(43, fillField43(ifx, convertor))); //Raza commenting using as String
		isoMsg.set(43, fillField43(ifx));
		// P48 In issueri rs to Mizan
		/* if(IfxType.CREDIT_PURCHASE_RS.equals(ifx.getIfxType())) {
					MizanSpecificData mizanData = ifx.getMizanSpecificData();
					StringBuilder p48 = new StringBuilder();
					for(int i=0; i<16; i++)
						p48.append("0");
					p48.append("01");
					p48.append(String.format("%02d", mizanData.getItemsCount()));
					p48.append(String.format("%02d", mizanData.getRequestCount()));
					p48.append(mizanData.getItems());
					isoMsg.set(new ISOBinaryField(48, p48.toString().getBytes()));
				}*/

		// P52
		if (ifx.getPINBlock() != null && !ifx.getPINBlock().equals(""))
			isoMsg.set(52, ifx.getPINBlock());

		// S90 -> reversal
		String S90 = "";

		if (ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
			if (ifx.getSafeOriginalDataElements().getMessageType() == null) {
				throw new ISOException("Invalid original data element: No Message Type for field 90");
			}

			S90 += ifx.getSafeOriginalDataElements().getMessageType();

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

		// P64 or S128: MAC
		if (isoMsg.getMaxField() > 64) {
			isoMsg.set(128, ifx.getMsgAuthCode());
			isoMsg.unset(64);
		} else {
			isoMsg.set(64, ifx.getMsgAuthCode());
			isoMsg.unset(128);
		}

		return isoMsg;
	}

}
