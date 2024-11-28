package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.*;
import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.calendar.DateTime;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.mtn.MTNChargeService;
import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.apacs70.encoding.Apacs70FarsiConvertor;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.component.SecurityComponent;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSConfiguration;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.UpdateStatus;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;
import vaulsys.wfe.GlobalContext;

import java.io.IOException;
import java.text.ParseException;

import org.apache.log4j.Logger;

public class OtherDataComponent extends Apacs70Component {
	private static final Logger logger = Logger.getLogger(OtherDataComponent.class);

	public static final String RECORD_TYPE = "Z5";
	public static final int RECORD_SUB_TYPE = 1;

	//for Request
	public Long voucherType;
	public Integer lastSuccessfulSequenceNumber;
	public Integer originalSequenceNumber;
	public String originalDateAndTime;
	public String billID;
	public String billPaymentID;
	public String temporaryTerminalPassword;

	public String reportFileVersion;
	public String shiftReportFileVersion;
	public String configurationFileVersion;
	public String protocolDataFileVersion;
	public String uiResourcesFileVersion;
	public String printerFontFileVersion;
	public String displayFontFileVersion;
	public String menuCaptionsFileVersion;
	public String farsiPhrasesFileVersion;
	public String reconciliationFileVersion;
	public String receiptMessageFileVersion;
	public String fileVersion11;
	public String fileVersion12;
	public String fileVersion13;
	public String fileVersion14;

	public String bootsuldKernelVersion;
	public String basicKernelVersion;
	public String cryptokmsKernelVersion;
	public String uldpmKernelVersion;
	public String fstmsKernelVersion;
	public String ctosKernelVersion;
	public String ulkKernelVersion;
	public String scmsrKernelVersion;
	public String peripheralKernelVersion;
	public String peripheral2KernelVersion;
	public String emvl2_uepKernelVersion;
	public String tms2KernelVersion;

	public String posSerialNum;
	public String applicationNum;
	public String applicationName;
	public String applicationVersion;
	public String applicationDate;

	//for Response
	public String updateRequired;
	public String voucherSerialNumber;
	public String voucherPassword;
	public String voucherData1;
	public String voucherData2;
	public String voucherData3;
	public String voucherData4;
	public String voucherData5;
	public String billType;
	public String issuerCode;
	public String updatedTerminalCode;
	public String updatedMerchantCode;
	public String updatedPOSIPAddress;
	public byte[] encryptedMasterKey;
	public byte[] encryptedMACKey;
	public byte[] encryptedPINKey;
	public byte[] encryptedDataKey;
	public byte[] encryptedICV;
	public String receiptMessagesVersion;
	public byte[] merchantHeader;
	public byte[] merchantFooter;
	public byte[] cardholderHeader;
	public byte[] cardholderFooter;

	// Yaraneh
//	public String validSubsidyCard; // '0' or '1' // 1
//	public String subsidyAccountBalance; // 12
//	public String gasBillBalance; // 12
//	public String electricityBillBalance; // 12
//	public String waterBillBalance; // 12
//	public String debitPayment; //12
//	public String waterBillDueDate; // 8
//	public String electricityBillDueDate; // 8
//	public String gasBillDueDate; // 8

	private boolean request;

	public OtherDataComponent(boolean request) {
		this.request = request;
	}

	public void pack(ApacsByteArrayWriter out) throws IOException{
		out.write(RECORD_TYPE, 2);
		out.writePadded(RECORD_SUB_TYPE, 2, false);
		out.write(GS);
		out.write(updateRequired, 1);
		out.write(GS);

		out.write(voucherSerialNumber, 19);
		out.write(GS);
		out.write(voucherPassword, 19);
		out.write(GS);
		out.write(voucherData1, 32);
		out.write(GS);
		out.write(voucherData2, 32);
		out.write(GS);
		out.write(voucherData3, 32);
		out.write(GS);
		out.write(voucherData4, 32);
		out.write(GS);
		out.write(voucherData5, 32);
		out.write(GS);

		out.write(billType, 3);
		out.write(GS);
		out.write(issuerCode, 3);
		out.write(GS);

		out.write(updatedTerminalCode, 8);
		out.write(GS);
		out.write(updatedMerchantCode, 16);
		out.write(GS);
		out.write(updatedPOSIPAddress, 15);
		out.write(GS);
		out.write(encryptedMasterKey, 8);
		out.write(GS);
		out.write(encryptedMACKey, 8);
		out.write(GS);
		out.write(encryptedPINKey, 8);
		out.write(GS);
		out.write(encryptedDataKey, 8);
		out.write(GS);
		out.write(encryptedICV, 8);
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

//		try {
			// yaraneh
/*			content.write(GS_VALUE);
			content.write(validSubsidyCard.getBytes());
			if(validSubsidyCard.equals("1")) {
				content.write(GS_VALUE);
				content.write(subsidyAccountBalance.getBytes());
				content.write(GS_VALUE);
				content.write(gasBillBalance.getBytes());
				content.write(GS_VALUE);
				content.write(electricityBillBalance.getBytes());
				content.write(GS_VALUE);
				content.write(waterBillBalance.getBytes());
				content.write(GS_VALUE);
				content.write(debitPayment.getBytes());
				content.write(GS_VALUE);
				content.write(Apacs70Utils.convertNull(waterBillDueDate).getBytes());
				content.write(GS_VALUE);
				content.write(Apacs70Utils.convertNull(electricityBillDueDate).getBytes());
				content.write(GS_VALUE);
				content.write(Apacs70Utils.convertNull(gasBillDueDate).getBytes());
			}
			else {
				for(int i=0; i<8; i++)
					content.write(GS_VALUE);
			}*/
//		} catch (IOException e) {
//			logger.error("No Header or Footer will be sent!", e);
//		}
	}
	
	public void unpack(ApacsByteArrayReader in) {
		voucherType = in.getLongMaxToSep("voucherType", 4, GS);
		lastSuccessfulSequenceNumber = in.getIntegerMaxToSep("lastSuccessfulSequenceNumber", 4, GS);
		originalSequenceNumber = in.getIntegerMaxToSep("originalSequenceNumber", 4, GS);
		originalDateAndTime = in.getStringMaxToSep("originalDateAndTime", 10, GS);
		billID = in.getStringMaxToSep("billID", 14, GS);
		billPaymentID = in.getStringMaxToSep("billPaymentID", 14, GS);
		temporaryTerminalPassword = in.getStringMaxToSep("temporaryTerminalPassword", 4, GS);

		reportFileVersion = in.getStringMaxToSep("reportFileVersion", 5, GS);
		shiftReportFileVersion = in.getStringMaxToSep("shiftReportFileVersion", 5, GS);
		configurationFileVersion = in.getStringMaxToSep("configurationFileVersion", 5, GS);
		protocolDataFileVersion = in.getStringMaxToSep("protocolDataFileVersion", 5, GS);
		uiResourcesFileVersion = in.getStringMaxToSep("uiResourcesFileVersion", 5, GS);
		printerFontFileVersion = in.getStringMaxToSep("printerFontFileVersion", 5, GS);
		displayFontFileVersion = in.getStringMaxToSep("displayFontFileVersion", 5, GS);
		menuCaptionsFileVersion = in.getStringMaxToSep("menuCaptionsFileVersion", 5, GS);
		farsiPhrasesFileVersion = in.getStringMaxToSep("farsiPhrasesFileVersion", 5, GS);
		reconciliationFileVersion = in.getStringMaxToSep("reconciliationFileVersion", 5, GS);
		receiptMessageFileVersion = in.getStringMaxToSep("receiptMessageFileVersion", 5, GS);
		fileVersion11 = in.getStringMaxToSep("fileVersion11", 5, GS);
		fileVersion12 = in.getStringMaxToSep("fileVersion12", 5, GS);
		fileVersion13 = in.getStringMaxToSep("fileVersion13", 5, GS);
		fileVersion14 = in.getStringMaxToSep("fileVersion14", 5, GS);
		
		bootsuldKernelVersion = in.getStringMaxToSep("bootsuldKernelVersion", 17, GS);
		basicKernelVersion = in.getStringMaxToSep("basicKernelVersion", 17, GS);
		cryptokmsKernelVersion = in.getStringMaxToSep("cryptokmsKernelVersion", 17, GS);
		uldpmKernelVersion = in.getStringMaxToSep("uldpmKernelVersion", 17, GS);
		fstmsKernelVersion = in.getStringMaxToSep("fstmsKernelVersion", 17, GS);
		ctosKernelVersion = in.getStringMaxToSep("ctosKernelVersion", 17, GS);
		ulkKernelVersion = in.getStringMaxToSep("ulkKernelVersion", 17, GS);
		scmsrKernelVersion = in.getStringMaxToSep("scmsrKernelVersion", 17, GS);
		peripheralKernelVersion = in.getStringMaxToSep("peripheralKernelVersion", 17, GS);
		peripheral2KernelVersion = in.getStringMaxToSep("peripheral2KernelVersion", 17, GS);
		emvl2_uepKernelVersion = in.getStringMaxToSep("emvl2_uepKernelVersion", 17, GS);
		tms2KernelVersion = in.getStringMaxToSep("tms2KernelVersion", 17, GS);
		
		posSerialNum = in.getStringMaxToSep("posSerialNum", 20, GS);
		applicationNum = in.getStringMaxToSep("applicationNum", 1, GS);
		applicationName = in.getStringMaxToSep("applicationName", 21, GS);
		applicationVersion = in.getStringMaxToSep("applicationVersion", 12, GS);
		applicationDate = in.getStringMaxToSep("applicationDate", 8, GS);
	}

	@Override
	public void toIfx(Ifx ifx) {
		if (this.lastSuccessfulSequenceNumber != null)
			ifx.setLast_TrnSeqCntr(this.lastSuccessfulSequenceNumber.toString());

		if (this.originalSequenceNumber != null) {
			ifx.getSafeOriginalDataElements().setTrnSeqCounter(this.originalSequenceNumber.toString());
			if (this.originalDateAndTime != null && Util.hasText(this.originalDateAndTime))
				try {
					ifx.getSafeOriginalDataElements().setOrigDt(new DateTime(MyDateFormatNew.parse("yyMMddHHmm", this.originalDateAndTime)));
				} catch (ParseException e) {
					e.printStackTrace(); //TODO
				}
//			Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
				String myBin = ProcessContext.get().getMyInstitution().getBin().toString();
			ifx.getSafeOriginalDataElements().setBankId(myBin);
		}

		if (Util.hasText(this.billID)) {
			String billIdLong = "";
			try {
				billIdLong = String.valueOf(Long.parseLong(this.billID));
			} catch(Exception e) {
				logger.warn("bad BillID!");
			}
			ifx.setBillID(billIdLong);
        	ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billIdLong));
        	ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billIdLong));
			ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billIdLong));
		}

	if (Util.hasText(this.billPaymentID)) {
		String payIdLong = "";
		try {
			payIdLong = String.valueOf(Long.parseLong(this.billPaymentID));
		} catch(Exception e) {
			logger.warn("bad PaymentID!");
		}
		ifx.setBillPaymentID(payIdLong);
		}

		ifx.setThirdPartyCode(this.voucherType);
		
		if (GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L)
		&& ifx.getThirdPartyCode() != null
                && ifx.getThirdPartyCode().equals(9935L)) {
			ifx.setThirdPartyCode(9936L);
		}

		
		ifx.setUpdateRequired(false); // default value, Processing is moved to after POS authorization (AuthorizationComponent)
		
		ifx.setSerialno(this.posSerialNum);
	}
	
	@Override
	public void fromIfx(Ifx ifx) {
		if (ifx.getChargeData() != null && ifx.getChargeData().getCharge() != null) {
			if (ifx.getChargeData().getCharge().getCardSerialNo() != null) {
				voucherSerialNumber = ifx.getChargeData().getCharge().getCardSerialNo().toString();
				if(ifx.getThirdPartyCode()==9935 || ifx.getThirdPartyCode()==9936) {
					voucherSerialNumber = "IR" + voucherSerialNumber;
					voucherData1 = MTNChargeService.getRealChargeCredit(ifx.getChargeData().getCharge().getCredit(), ifx.getChargeData().getCharge().getEntity().getCode()).toString();
				}
			}
			if (Util.hasText(ifx.getChargeData().getCharge().getCardPIN())) {
				byte[] actualPIN = null;
				try {
					actualPIN = SecurityComponent.rsaDecrypt(Hex.decode(ifx.getChargeData().getCharge().getCardPIN()));
				} catch (Exception e) {
					logger.error("Finding ActualPIN: ", e);
				}
				voucherPassword = new String(actualPIN);
			}
		}

		if (ifx.getBillOrgType() != null) {
			String billTypeSrc = ProcessContext.get().getProtocolConfig(ProtocolType.APACS70, ifx.getBillOrgType().getType());
			billType = StringFormat.formatNew(3, StringFormat.JUST_RIGHT, billTypeSrc, '0');
		}

		issuerCode = Apacs70Utils.issuerCode(ifx);

		POSConfiguration conf = null;
		UpdateStatus updateStatus = null;
		Terminal terminal = ProcessContext.get().getOriginatorTerminal();

		if (terminal != null) {
			FinancialEntity fe = terminal.getOwner();
			if (TerminalType.POS.equals(ifx.getTerminalType())) {
				POSTerminal pos = (POSTerminal) terminal;
//				conf = pos.getConfiguration();
				conf = pos.getOwnOrParentConfiguration();
				updateStatus = pos.getUpdateStatus();
			} else if (TerminalType.PINPAD.equals(ifx.getTerminalType())) {
				PINPADTerminal pp = (PINPADTerminal) terminal;
				conf = pp.getOwnOrParentConfiguration();
				updateStatus = pp.getUpdateStatus();
			}
			try {
				if (conf != null && ifx.getUpdateReceiptRequired() != null && ifx.getUpdateReceiptRequired()) {
					Apacs70FarsiConvertor apacsConvertor = Apacs70FarsiConvertor.Instance;

					merchantHeader = apacsConvertor.convert(conf.getMerchantHeader(), ifx, fe, terminal);
					merchantFooter = apacsConvertor.convert(conf.getMerchantFooter(), ifx, fe, terminal);
					cardholderHeader = apacsConvertor.convert(conf.getCardholderHeader(), ifx, fe, terminal);
					cardholderFooter = apacsConvertor.convert(conf.getCardholderFooter(), ifx, fe, terminal);
					receiptMessagesVersion = conf.getReceiptVersion().toString();
				}
			} catch (Exception e) {
				logger.error("Error in generating receipt header and footer: ", e);
			}
		}
		this.updateRequired = UpdateStatus.NEED_UPDATE.equals(updateStatus) ? "1" : "0";
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if(request) {
			if(voucherType != null)
				b.append("\nEVoucherType: ").append(voucherType);
			b.append("\nLastSuccessfulSequenceNumber: ").append(lastSuccessfulSequenceNumber);
			b.append("\nOriginalSequenceNumber: ").append(originalSequenceNumber);
			b.append("\nOriginalDateAndTime: ").append(originalDateAndTime);
			if(Util.hasText(billID))
				b.append("\nBillID: ").append(billID);
			if(Util.hasText(billPaymentID))
				b.append("\nBillPaymentID: ").append(billPaymentID);
			if(Util.hasText(temporaryTerminalPassword))
				b.append("\nTemporaryTerminalPassword: ").append(temporaryTerminalPassword);
			b.append("\nPOSSerialNumber: ").append(posSerialNum);
			b.append("\nApplicationVersion: ").append(applicationVersion);
		}
		else {
			if(Util.hasText(updateRequired))
				b.append("\nUpdateRequired: ").append(updateRequired);
			if(Util.hasText(voucherSerialNumber))
				b.append("\nSerialNumber:  ").append(voucherSerialNumber);
			if(Util.hasText(billType))
				b.append("\nBillType: ").append(billType);
			b.append("\nIssuerCode: ").append(issuerCode);
		}
		
//		b.append("\t\n validSubsidyCard:\t\t").append(validSubsidyCard);
//		b.append("\t\n subsidyAccountBalance:\t\t").append(subsidyAccountBalance);
//		b.append("\t\n gasBillBalance:\t\t").append(gasBillBalance);
//		b.append("\t\n electricityBillBalance:\t\t").append(electricityBillBalance);
//		b.append("\t\n waterBillBalance:\t\t").append(waterBillBalance);
		return b.toString();
	}

	public String getReportFileVersion() {
		return reportFileVersion;
	}

	public String getShiftReportFileVersion() {
		return shiftReportFileVersion;
	}

	public String getConfigurationFileVersion() {
		return configurationFileVersion;
	}

	public String getProtocolDataFileVersion() {
		return protocolDataFileVersion;
	}

	public String getUiResourcesFileVersion() {
		return uiResourcesFileVersion;
	}

	public String getPrinterFontFileVersion() {
		return printerFontFileVersion;
	}

	public String getDisplayFontFileVersion() {
		return displayFontFileVersion;
	}

	public String getMenuCaptionsFileVersion() {
		return menuCaptionsFileVersion;
	}

	public String getFarsiPhrasesFileVersion() {
		return farsiPhrasesFileVersion;
	}

	public String getReconciliationFileVersion() {
		return reconciliationFileVersion;
	}

	public String getReceiptMessageFileVersion() {
		return receiptMessageFileVersion;
	}

	public String getBootsuldKernelVersion() {
		return bootsuldKernelVersion;
	}

	public String getBasicKernelVersion() {
		return basicKernelVersion;
	}

	public String getCryptokmsKernelVersion() {
		return cryptokmsKernelVersion;
	}

	public String getUldpmKernelVersion() {
		return uldpmKernelVersion;
	}

	public String getFstmsKernelVersion() {
		return fstmsKernelVersion;
	}

	public String getCtosKernelVersion() {
		return ctosKernelVersion;
	}

	public String getUlkKernelVersion() {
		return ulkKernelVersion;
	}

	public String getScmsrKernelVersion() {
		return scmsrKernelVersion;
	}

	public String getPeripheralKernelVersion() {
		return peripheralKernelVersion;
	}

	public String getPeripheral2KernelVersion() {
		return peripheral2KernelVersion;
	}

	public String getEmvl2_uepKernelVersion() {
		return emvl2_uepKernelVersion;
	}

	public String getTms2KernelVersion() {
		return tms2KernelVersion;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}
}
