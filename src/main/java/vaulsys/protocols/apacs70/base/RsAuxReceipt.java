package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.lottery.consts.LotteryState;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.apacs70.encoding.Apacs70FarsiConvertor;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSConfiguration;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;

public class RsAuxReceipt extends RsAuxBase {
	public String receiptMessagesVersion;
	public byte[] merchantHeader;
	public byte[] merchantFooter;
	public byte[] cardholderHeader;
	public byte[] cardholderFooter;

	public RsAuxReceipt() {
		super("99");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		// NOTE: no need to call super here

		try {
			Apacs70FarsiConvertor apacsConvertor = Apacs70FarsiConvertor.Instance;
			Terminal terminal = ProcessContext.get().getOriginatorTerminal();
			POSConfiguration conf = null;

			if (terminal != null) {
				FinancialEntity fe = terminal.getOwner();
				if (TerminalType.POS.equals(ifx.getTerminalType()))
					conf = ((POSTerminal) terminal).getOwnOrParentConfiguration();
				else if (TerminalType.PINPAD.equals(ifx.getTerminalType()))
					conf = ((PINPADTerminal) terminal).getOwnOrParentConfiguration();

				/******** Lottery Receipt *******/
				String cardHolderHeaderStr = "";
				if (ifx.getLotteryData() != null && LotteryState.ASSIGNED.equals(ifx.getLotteryStateNxt())) {
					cardHolderHeaderStr = "شما برنده کارت با شماره مرجع";
					cardHolderHeaderStr += apacsConvertor.convertStr(ifx.getLottery().getSerial().toString(), ifx, fe, terminal);
					cardHolderHeaderStr += " به مبلغ ";
					cardHolderHeaderStr += apacsConvertor.convertStr(ifx.getLottery().getCredit().toString(), ifx, fe, terminal);
					cardHolderHeaderStr += " ريال میباشيد";
					cardHolderHeaderStr += '¶';

					// Construct string for merchant footer(for lottery):
					// merchantFooterStr = "برنده 50,000 ریال جایزه";
					// merchantFooterStr += '¶';

				} else if (ifx.getLotteryData() != null && LotteryState.NOT_ASSIGNED.equals(ifx.getLotteryStateNxt())
						&& LotteryState.ASSIGNED.equals(ifx.getLotteryStatePrv())
						&& ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {

					cardHolderHeaderStr = "دارنده کارت گرامی! به علت برگشت تراکنش کارت جایزه شما فاقد اعتبار است";
					cardHolderHeaderStr += '¶';

					// merchantFooterStr =
					// "***برگشت جایزه 50,000 به علت برگشت تراکنش***";
					// merchantFooterStr += '¶';

				}
				/***** lottery receipt *****/

				if (conf != null) {
					// cardholderHeader =
					// apacsConvertor.convert(conf.getCardholderHeader(), ifx,
					// fe, terminal);
					cardHolderHeaderStr += apacsConvertor.convertStr(conf.getCardholderHeader(), ifx, fe, terminal);
					cardholderHeader = apacsConvertor.encode(cardHolderHeaderStr);
					cardholderFooter = apacsConvertor.convert(conf.getCardholderFooter(), ifx, fe, terminal);

					merchantHeader = apacsConvertor.convert(conf.getMerchantHeader(), ifx, fe, terminal);
					// merchantFooterStr +=
					// apacsConvertor.convertStr(conf.getMerchantFooter(), ifx,
					// fe, terminal);
					// merchantFooter =
					// apacsConvertor.encode(merchantFooterStr);
					merchantFooter = apacsConvertor.convert(conf.getMerchantFooter(), ifx, fe, terminal);

					receiptMessagesVersion = conf.getReceiptVersion().toString();

				} else {
					if (Util.hasText(cardHolderHeaderStr)) {
						cardholderHeader = apacsConvertor.encode(cardHolderHeaderStr);
					}

					try {
						String specialChar = ConfigUtil.getProperty(ConfigUtil.APACS70_SPECIAL_CHAR);
						if ("space".equalsIgnoreCase(specialChar)) {
							specialChar = " ";
						}

						if (!ConfigUtil.getBoolean(ConfigUtil.APACS70_HAS_CARD_HOLDER_FOOTER)) {
							cardholderFooter = apacsConvertor.encode(specialChar);
						}

						if (!ConfigUtil.getBoolean(ConfigUtil.APACS70_HAS_MERCHANT_HEADER)) {
							merchantHeader = apacsConvertor.encode(specialChar);
						}

						if (!ConfigUtil.getBoolean(ConfigUtil.APACS70_HAS_MERCHANT_FOOTER)) {
							merchantFooter = apacsConvertor.encode(specialChar);
						}
					} catch (Exception e) {
						logger.error("Exception in setting null config " + e, e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in generating receipt header and footer: ", e);
		}
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		out.write("Z6", 2);
		out.write("99", 2);
		out.write(GS);
		out.writePadded(receiptMessagesVersion, 5, false);
		out.write(GS);
		Apacs70Utils.truncateReceiptWithoutNL(out, merchantHeader);
		out.write(GS);
		Apacs70Utils.truncateReceiptWithoutNL(out, merchantFooter);
		out.write(GS);
		Apacs70Utils.truncateReceiptWithoutNL(out, cardholderHeader);
		out.write(GS);
		Apacs70Utils.truncateReceiptWithoutNL(out, cardholderFooter);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nReceipt Version: ").append(receiptMessagesVersion);
	}
}
