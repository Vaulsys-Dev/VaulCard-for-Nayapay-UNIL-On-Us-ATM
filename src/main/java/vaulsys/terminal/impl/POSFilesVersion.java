package vaulsys.terminal.impl;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public abstract class POSFilesVersion implements IEntity<Long> {
	@Id
	@GeneratedValue(generator = "pfv-seq-gen")
	@GenericGenerator(name = "pfv-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
			@org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "pfv_seq")
		})
	private Long id;
	
	@Column(name = "app_ver", length = 12)
	private String applicationVersion;

	@Column(name = "rprt_f_ver", length = 5)
	private String reportFileVersion;

	@Column(name = "shft_f_ver", length = 5)
	private String shiftReportFileVersion;

	@Column(name = "conf_f_ver", length = 5)
	private String configurationFileVersion;

	@Column(name = "prtcl_data_f_ver", length = 5)
	private String protocolDataFileVersion;

	@Column(name = "wlcm_scr_f_ver", length = 5)
	private String uiResourcesFileVersion;

	@Column(name = "prn_fnt_f_ver", length = 5)
	private String printerFontFileVersion;

	@Column(name = "dsp_fnt_f_ver", length = 5)
	private String displayFontFileVersion;

	@Column(name = "mnu_cp_f_ver", length = 5)
	private String menuCaptionsFileVersion;

	@Column(name = "fa_phr_f_ver", length = 5)
	private String farsiPhrasesFileVersion;
	
	@Column(name = "recon_f_ver", length = 5)
	private String reconciliationFileVersion;

	// per POS, defined in POSSpeceficData 
	//@Column(name = "rcpt_msg_f_ver", length = 5)
	//private String receiptMessageFileVersion;

	// K E R N E L   F I L E S 
	
	@Column(name = "bts_k_ver", length = 20)
	private String bootsuldKernelVersion;

	@Column(name = "bsc_k_ver", length = 20)
	private String basicKernelVersion;

	@Column(name = "crypt_k_ver", length = 20)
	private String cryptokmsKernelVersion;

	@Column(name = "uldpm_k_ver", length = 20)
	private String uldpmKernelVersion;

	@Column(name = "fstms_k_ver", length = 20)
	private String fstmsKernelVersion;

	@Column(name = "ctos_k_ver", length = 20)
	private String ctosKernelVersion;

	@Column(name = "ulk_k_ver", length = 20)
	private String ulkKernelVersion;

	@Column(name = "scmsr_k_ver", length = 20)
	private String scmsrKernelVersion;

	@Column(name = "prph_k_ver", length = 20)
	private String peripheralKernelVersion;

	@Column(name = "prph2_k_ver", length = 20)
	private String peripheral2KernelVersion;

	@Column(name = "emvl2_k_ver", length = 20)
	private String emvl2_uepKernelVersion;

	@Column(name = "tms2_k_ver", length = 20)
	private String tms2KernelVersion;
	
	@AttributeOverrides({
		@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
		@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
	})
	private DateTime createdDateTime;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}
	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	public String getReportFileVersion() {
		return reportFileVersion;
	}
	public void setReportFileVersion(String reportFileVersion) {
		this.reportFileVersion = reportFileVersion;
	}

	public String getShiftReportFileVersion() {
		return shiftReportFileVersion;
	}
	public void setShiftReportFileVersion(String shiftReportFileVersion) {
		this.shiftReportFileVersion = shiftReportFileVersion;
	}

	public String getConfigurationFileVersion() {
		return configurationFileVersion;
	}
	public void setConfigurationFileVersion(String configurationFileVersion) {
		this.configurationFileVersion = configurationFileVersion;
	}

	public String getProtocolDataFileVersion() {
		return protocolDataFileVersion;
	}
	public void setProtocolDataFileVersion(String protocolDataFileVersion) {
		this.protocolDataFileVersion = protocolDataFileVersion;
	}

	public String getUiResourcesFileVersion() {
		return uiResourcesFileVersion;
	}
	public void setUiResourcesFileVersion(String uiResourcesFileVersion) {
		this.uiResourcesFileVersion = uiResourcesFileVersion;
	}

	public String getPrinterFontFileVersion() {
		return printerFontFileVersion;
	}
	public void setPrinterFontFileVersion(String printerFontFileVersion) {
		this.printerFontFileVersion = printerFontFileVersion;
	}

	public String getDisplayFontFileVersion() {
		return displayFontFileVersion;
	}
	public void setDisplayFontFileVersion(String displayFontFileVersion) {
		this.displayFontFileVersion = displayFontFileVersion;
	}

	public String getMenuCaptionsFileVersion() {
		return menuCaptionsFileVersion;
	}
	public void setMenuCaptionsFileVersion(String menuCaptionsFileVersion) {
		this.menuCaptionsFileVersion = menuCaptionsFileVersion;
	}

	public String getFarsiPhrasesFileVersion() {
		return farsiPhrasesFileVersion;
	}
	public void setFarsiPhrasesFileVersion(String farsiPhrasesFileVersion) {
		this.farsiPhrasesFileVersion = farsiPhrasesFileVersion;
	}
	
	public String getReconciliationFileVersion() {
		return reconciliationFileVersion;
	}
	public void setReconciliationFileVersion(String reconciliationFileVersion) {
		this.reconciliationFileVersion = reconciliationFileVersion;
	}

/*	public String getReceiptMessageFileVersion() {
		return receiptMessageFileVersion;
	}
	public void setReceiptMessageFileVersion(String receiptMessageFileVersion) {
		this.receiptMessageFileVersion = receiptMessageFileVersion;
	}*/

	public String getBootsuldKernelVersion() {
		return bootsuldKernelVersion;
	}
	public void setBootsuldKernelVersion(String bootsuldKernelVersion) {
		this.bootsuldKernelVersion = bootsuldKernelVersion;
	}

	public String getBasicKernelVersion() {
		return basicKernelVersion;
	}
	public void setBasicKernelVersion(String basicKernelVersion) {
		this.basicKernelVersion = basicKernelVersion;
	}

	public String getCryptokmsKernelVersion() {
		return cryptokmsKernelVersion;
	}
	public void setCryptokmsKernelVersion(String cryptokmsKernelVersion) {
		this.cryptokmsKernelVersion = cryptokmsKernelVersion;
	}

	public String getUldpmKernelVersion() {
		return uldpmKernelVersion;
	}
	public void setUldpmKernelVersion(String uldpmKernelVersion) {
		this.uldpmKernelVersion = uldpmKernelVersion;
	}

	public String getFstmsKernelVersion() {
		return fstmsKernelVersion;
	}
	public void setFstmsKernelVersion(String fstmsKernelVersion) {
		this.fstmsKernelVersion = fstmsKernelVersion;
	}

	public String getCtosKernelVersion() {
		return ctosKernelVersion;
	}
	public void setCtosKernelVersion(String ctosKernelVersion) {
		this.ctosKernelVersion = ctosKernelVersion;
	}

	public String getUlkKernelVersion() {
		return ulkKernelVersion;
	}
	public void setUlkKernelVersion(String ulkKernelVersion) {
		this.ulkKernelVersion = ulkKernelVersion;
	}

	public String getScmsrKernelVersion() {
		return scmsrKernelVersion;
	}
	public void setScmsrKernelVersion(String scmsrKernelVersion) {
		this.scmsrKernelVersion = scmsrKernelVersion;
	}

	public String getPeripheralKernelVersion() {
		return peripheralKernelVersion;
	}
	public void setPeripheralKernelVersion(String peripheralKernelVersion) {
		this.peripheralKernelVersion = peripheralKernelVersion;
	}

	public String getPeripheral2KernelVersion() {
		return peripheral2KernelVersion;
	}
	public void setPeripheral2KernelVersion(String peripheral2KernelVersion) {
		this.peripheral2KernelVersion = peripheral2KernelVersion;
	}

	public String getEmvl2_uepKernelVersion() {
		return emvl2_uepKernelVersion;
	}
	public void setEmvl2_uepKernelVersion(String emvl2_uepKernelVersion) {
		this.emvl2_uepKernelVersion = emvl2_uepKernelVersion;
	}

	public String getTms2KernelVersion() {
		return tms2KernelVersion;
	}
	public void setTms2KernelVersion(String tms2KernelVersion) {
		this.tms2KernelVersion = tms2KernelVersion;
	}

	public DateTime getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(DateTime createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof POSFilesVersion))
			return false;
		POSFilesVersion other = (POSFilesVersion) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
