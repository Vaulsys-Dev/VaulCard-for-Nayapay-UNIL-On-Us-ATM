package vaulsys.migration;

import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.exception.SMException;
import vaulsys.security.jceadapter.JCESecurityModule;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.EncryptedPIN;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.bouncycastle.util.encoders.Hex;

public class MigrationRecord {
	private String appPAN;
	private String trk2EquivData;
	private String CVV2;
	private Long expDt;
	private String PINBlock;
	private Long receivedDtLong;
	private Long transactionId;
	private String rsCode;
	private TerminalType terminalType;
	private IfxType ifxType;
	private String newPINBlock;
	private TrnType trnType;

	
	private static JCESecurityModule ssm;
	private static SecureDESKey neginKey;
	private static SecureDESKey key1;
	private static SecureDESKey key2;
	
	static{
		try {
			ssm = new JCESecurityModule("/config/LMK.jceks", "$3cureP@$$".toCharArray(), "org.bouncycastle.jce.provider.BouncyCastleProvider");
		} catch (SMException e) {
			e.printStackTrace();
//			System.exit(0);
		}
//		SwitchTerminal issuerSwitchTerminal = GlobalContext.getInstance().getIssuerSwitchTerminal(639347L);
		SwitchTerminal issuerSwitchTerminal = ProcessContext.get().getIssuerSwitchTerminal("639347");
//		SwitchTerminal cmsIssuerSwitchTerminal = GlobalContext.getInstance().getIssuerSwitchTerminal(502229L);
//			issuerSwitchTerminal = GlobalContext.getInstance().getIssuerSwitchTerminal(GlobalContext.getInstance().getPeerInstitutionsBin().get(0));
		neginKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, issuerSwitchTerminal.getKeySet());
//		key1 = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, cmsIssuerSwitchTerminal.getKeySet());
//		key1 = new SecureDESKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, "279D49A73BAD90E0");
		key1 = new SecureDESKey(SMAdapter.LENGTH_DES3_3KEY, KeyType.TYPE_TPK, "32DEEC95B4FB24DE2F2D82DBA351B26A32DEEC95B4FB24DE");
		key2 = new SecureDESKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, "279D49A73BAD90E0");
	}
	
	public MigrationRecord(Ifx ifx, Long trxId) {
		setAppPAN(ifx.getAppPAN());
		setTrk2EquivData(ifx.getTrk2EquivData());
		setCVV2(ifx.getCVV2());
		setExpDt(ifx.getExpDt());
		setPINBlock(ifx.getPINBlock());
		setReceivedDtLong(ifx.getReceivedDtLong());
		setRsCode(ifx.getRsCode());
		setTerminalType(ifx.getTerminalType());
		setIfxType(ifx.getIfxType());
		setNewPINBlock(ifx.getNewPINBlock());
		setTrnType(ifx.getTrnType());
		setTransactionId(trxId);
		setNewPINBlock(ifx.getNewPINBlock());
	}

	public String getCVV2() {
		if(CVV2 == null || CVV2.isEmpty())
			return "0000";
		return CVV2;
	}

	public void setCVV2(String cvv2) {
		CVV2 = cvv2;
	}
	
	public String getAppPAN() {
		return appPAN;
	}
	public void setAppPAN(String appPAN) {
		this.appPAN = appPAN;
	}
	public String getTrk2EquivData() {
		return trk2EquivData;
	}
	public void setTrk2EquivData(String trk2EquivData) {
		this.trk2EquivData = trk2EquivData;
	}
	
	public Long getExpDt() {
		return expDt;
	}
	public void setExpDt(Long expDt) {
		this.expDt = expDt;
	}
	public Long getReceivedDtLong() {
		return receivedDtLong;
	}
	public void setReceivedDtLong(Long receivedDtLong) {
		this.receivedDtLong = receivedDtLong;
	}
	public Long getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
	public String getRsCode() {
		return rsCode;
	}
	public void setRsCode(String rsCode) {
		this.rsCode = rsCode;
	}
	public TerminalType getTerminalType() {
		return terminalType;
	}
	public void setTerminalType(TerminalType terminalType) {
		this.terminalType = terminalType;
	}
	public IfxType getIfxType() {
		return ifxType;
	}
	public void setIfxType(IfxType ifxType) {
		this.ifxType = ifxType;
	}

	public String getPINBlock() {
		return PINBlock;
	}

	public void setPINBlock(String block) {
		PINBlock = block;
	}

	public String getNewPINBlock() {
		return newPINBlock;
	}

	public void setNewPINBlock(String newPINBlock) {
		this.newPINBlock = newPINBlock;
	}
	
	public String getTranslatedPINBlockToSendCMS(Terminal sender, String appPAN, String PINBlock, boolean senderPinEnable) throws SMException {
		byte[] pinBlock = Hex.decode(PINBlock);
		String PAN = appPAN.substring(appPAN.length() - 12 - 1, appPAN.length() - 1);
		EncryptedPIN pin = new EncryptedPIN(pinBlock, (byte)01, PAN);

		if (!senderPinEnable){
//	        EncryptedPIN pinEncrypted = ssm.encryptPINByKey(this.PINBlock, PAN, (byte)01, key2);
//	        return new String(Hex.encode(pinEncrypted.getPINBlock()));
		    String pinDecrypted = ssm.decryptPINByKey(pin, key1);
		    return pinDecrypted;
		} else {		    
		    SecureDESKey terminalKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, sender.getKeySet());
		    
			EncryptedPIN pinTranslated = ssm.translatePIN(pin, key1, terminalKey, (byte)01);
			return new String(Hex.encode(pinTranslated.getPINBlock()));
		}
	}
	
	public String getTranslatedPINBlockToCheckNegin(Terminal sender, boolean senderPinEnable) throws SMException {
		byte[] pinBlock;
		String PAN = this.appPAN.substring(this.appPAN.length() - 12 - 1, this.appPAN.length() - 1);
		
		if (!senderPinEnable){
	        EncryptedPIN pinEncrypted = ssm.encryptPINByKey(this.PINBlock, PAN, (byte)01, key2);
	        return new String(Hex.encode(pinEncrypted.getPINBlock()));
		}else{
			pinBlock = Hex.decode(this.PINBlock);
			EncryptedPIN pin = new EncryptedPIN(pinBlock, (byte)01, PAN);
			
			SecureDESKey terminalKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, sender.getKeySet());
			EncryptedPIN pinTranslated = ssm.translatePIN(pin, terminalKey, key2, (byte)01);
			return new String(Hex.encode(pinTranslated.getPINBlock()));
		}	
	}
	
	public String getTranslatedPINBlock() throws SMException {
		byte[] pinBlock = Hex.decode(this.PINBlock);
		
		String PAN = this.appPAN.substring(this.appPAN.length() - 12 - 1, this.appPAN.length() - 1);
		EncryptedPIN pin = new EncryptedPIN(pinBlock, (byte)01, PAN);
		
//	    ssm.decryptPINByKey(pin, key);
//		System.out.println("PIN is:"+ssm.decryptPINByKey(pin, key));
		
		EncryptedPIN pinTranslated = ssm.translatePIN(pin, neginKey, key2, (byte)01);
		
		return new String(Hex.encode(pinTranslated.getPINBlock()));
	}
	
	public String getTranslatedNewPINBlock() throws SMException {
		byte[] pinBlock = Hex.decode(this.newPINBlock);

		String PAN = this.appPAN.substring(this.appPAN.length() - 12 - 1, this.appPAN.length() - 1);
	    EncryptedPIN pin = new EncryptedPIN(pinBlock, (byte)01, PAN);
	    
	    ssm.decryptPINByKey(pin, neginKey);
//		System.out.println("PIN is:"+ssm.decryptPINByKey(pin, key));

		EncryptedPIN pinTranslated = ssm.translatePIN(pin, neginKey, key2, (byte)01);

		return new String(Hex.encode(pinTranslated.getPINBlock()));
	}

	public TrnType getTrnType() {
		return trnType;
	}

	public void setTrnType(TrnType trnType) {
		this.trnType = trnType;
	}

}
