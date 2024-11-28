package vaulsys.terminal.impl;


import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "term_pos_spc_files")
public class POSSpecificFilesVersion extends POSFilesVersion {

	@Column(name = "receipt_ver")
	private String receiptVer;

	@OneToOne(mappedBy = "filesVersion", fetch = FetchType.LAZY)
	private POSTerminal pos;
	
	@OneToOne(mappedBy = "filesVersion", fetch = FetchType.LAZY)
	private PINPADTerminal pinpad;

	@Column(name = "nd2updt_bitset")
	private Integer needToUpdateBitSet;

	@Column(name = "ver_cnflct")
	private Integer versionConflict; 

	public String getReceiptVer() {
		return receiptVer;
	}
	public void setReceiptVer(String receiptVer) {
		this.receiptVer = receiptVer;
	}

	public POSTerminal getPos() {
		return pos;
	}
	public void setPos(POSTerminal pos) {
		this.pos = pos;
	}
	
	public PINPADTerminal getPinpad() {
		return pinpad;
	}
	public void setPinpad(PINPADTerminal pinpad) {
		this.pinpad = pinpad;
	}

	public Integer getNeedToUpdateBitSet() {
		return needToUpdateBitSet;
	}
	public void setNeedToUpdateBitSet(Integer needToUpdateBitSet) {
		this.needToUpdateBitSet = needToUpdateBitSet;
	}
	
	public Integer getVersionConflict() {
		return versionConflict;
	}
	public void setVersionConflict(Integer versionConflict) {
		this.versionConflict = versionConflict;
	}

	private static Map<String, Integer> versionFeildToBitNo = new HashMap<String, Integer>();
	static {
		versionFeildToBitNo.put("reportFileVersion", 1);
		versionFeildToBitNo.put("shiftReportFileVersion", 2);
		versionFeildToBitNo.put("configurationFileVersion", 4);
		versionFeildToBitNo.put("protocolDataFileVersion", 8);
		versionFeildToBitNo.put("uiResourcesFileVersion", 16);
		versionFeildToBitNo.put("printerFontFileVersion", 32);
		versionFeildToBitNo.put("displayFontFileVersion", 64);
		versionFeildToBitNo.put("menuCaptionsFileVersion", 128);
		versionFeildToBitNo.put("farsiPhrasesFileVersion", 256);
		versionFeildToBitNo.put("reconciliationFileVersion", 512);
		versionFeildToBitNo.put("receiptMessageFileVersion", 1024);
		
		versionFeildToBitNo.put("bootsuldKernelVersion", 16384);
		versionFeildToBitNo.put("basicKernelVersion", 32768);
		versionFeildToBitNo.put("cryptokmsKernelVersion", 65536);
		versionFeildToBitNo.put("uldpmKernelVersion", 131072);
		versionFeildToBitNo.put("fstmsKernelVersion", 262144);
		versionFeildToBitNo.put("ctosKernelVersion", 524288);
		versionFeildToBitNo.put("ulkKernelVersion", 1048576);
		versionFeildToBitNo.put("scmsrKernelVersion", 2097152);
		versionFeildToBitNo.put("peripheralKernelVersion", 4194304);
		versionFeildToBitNo.put("peripheral2KernelVersion", 8388608);
		versionFeildToBitNo.put("emvl2_uepKernelVersion", 16777216);
		versionFeildToBitNo.put("tms2KernelVersion", 33554432);
		versionFeildToBitNo.put("applicationVersion", 67108864);
	}

	public static int getBitNo(String prop) {
		return versionFeildToBitNo.get(prop);
	}
}
