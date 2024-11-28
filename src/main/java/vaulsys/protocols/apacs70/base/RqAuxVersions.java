package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;

public class RqAuxVersions extends RqAuxBase {
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
	
	public String applicationNum;
	public String applicationName;
	public String applicationVersion;
	public String applicationDate;

	@Override
	public void unpack(ApacsByteArrayReader in) {
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
		in.getStringMaxToSep("file Version 1", 5, GS); // file Version 1
		in.getStringMaxToSep("file Version 2", 5, GS); // file Version 2
		in.getStringMaxToSep("file Version 3", 5, GS); // file Version 3
		in.getStringMaxToSep("file Version 4", 5, GS); // file Version 4

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

		applicationNum = in.getStringMaxToSep("applicationNum", 1, GS);
		applicationName = in.getStringMaxToSep("applicationName", 21, GS);
		applicationVersion = in.getStringMaxToSep("applicationVersion", 12, GS);
		applicationDate = in.getStringMax("applicationDate", 8);
	}

	@Override
	public void toIfx(Ifx ifx) {
		throw new RuntimeException("Sholdn't be called!");
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nApp Name: ").append(applicationName);
		builder.append("\nApp Version: ").append(applicationVersion);
		builder.append("\nApp Date: ").append(applicationDate);
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

	public String getApplicationNum() {
		return applicationNum;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public String getApplicationDate() {
		return applicationDate;
	}
}
