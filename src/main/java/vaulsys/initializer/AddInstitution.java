package vaulsys.initializer;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.entity.impl.Institution;
import vaulsys.fee.impl.FeeProfile;
import vaulsys.mtn.ChargeAssignmentPolicy;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.jceadapter.JCESecurityModule;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.SwitchTerminalType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.util.encoders.Hex;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

public class AddInstitution {

	public static final long INST_PEP_CODE = 539347;
	public static final long PASARGAD_BIN = 502230;
	public static final long PEP_ISSUER_TERMINAL_CODE = 222;
	public static final long PEP_ACQUIER_TERMINAL_CODE = 333;
	private static final String PEP_MAC_KEY = "1111111111111111";
	private static final String PEP_PIN_KEY = "1111111111111111";
	private static final String PEP_MASTER_KEY = "1111111111111111";
	public static final Long CITY_TEHRAN_CODE = 21L;
	public static final Long STATE_TEHRAN_CODE = 21L;
	public static final Long COUNTRY_IRAN_CODE = 1L;
	public static final String GENERAL_ADDRESS = "Ù�Ù†Ø§Ù¾";
	public static final String GENERAL_PHONE_NUMBER = "021-88208820";
	public static final String GENERAL_WEB_ADDRESS = "www.fanap.ir";

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			new AddInstitution().addToDB();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
		System.exit(0);
	}

	private void addToDB() throws Exception {

		System.out.println("------- Creating Profiles -------");
		SecurityProfile securityProfileDefault = AddSecurityProfile.createDefaultSecurityProfile(AddSecurityProfile.DEFAULT_SECURITY_PROFILE_NAME);
		AuthorizationProfile authorizationProfileDefault = AddAuthorization.createAuthorizationProfile(AddAuthorization.DEFAULT_AUTHORIZATION_PROFILE_NAME);
		FeeProfile feeProfileDefault = DBInitializeAddFee.createDefaultFeeProfile();

		TerminalGroup rootTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, null, "Ù‡Ù…Ù‡ ØªØ±Ù…ÛŒÙ†Ø§Ù„Ù‡Ø§", null);
		
		TerminalGroup switchTerminalGroup = DBInitializeUtil.createTerminalGroup(feeProfileDefault,
				securityProfileDefault, authorizationProfileDefault, null, rootTerminalGroup, "Ø³ÙˆØ¦ÛŒÚ† Ù‡Ø§", null);
		
		System.out.println("------- Creating MySelf Institution -------");
		initGeneralInst(PASARGAD_BIN, PASARGAD_BIN, FinancialEntityRole.MASTER, PEP_ACQUIER_TERMINAL_CODE,
				PEP_ISSUER_TERMINAL_CODE, switchTerminalGroup, securityProfileDefault, authorizationProfileDefault,
				null, feeProfileDefault, null,
				PEP_MAC_KEY, PEP_PIN_KEY, PEP_MAC_KEY, PEP_PIN_KEY, PEP_MASTER_KEY
				, "Ø³ÙˆØ¦ÛŒÚ† Ù�Ù†Ø§Ù¾", "Ø³ÙˆØ¦ÛŒÚ† Ù�Ù†Ø§Ù¾");
	}

	
	public static void initMyInst(long code, long bin , long acquierTerminal, long issuerTerminal,
			TerminalGroup terminalGroup, SecurityProfile securityProfile, AuthorizationProfile authorizationProfile, 
			ClearingProfile clearingProfile, 
			FeeProfile feeProfile, 
			ChargeAssignmentPolicy chargePolicy, String macKey, String pinKey, String masterkey
			,String name, String repname) throws Exception {
	
		initGeneralInst(code, bin, FinancialEntityRole.MY_SELF, acquierTerminal, issuerTerminal, terminalGroup, securityProfile, authorizationProfile, clearingProfile, feeProfile, chargePolicy, macKey, pinKey, macKey, pinKey, masterkey, name, repname);
	}
	
	public static void initGeneralInst(long code, long bin, FinancialEntityRole role, long acquierTerminal, long issuerTerminal,
			TerminalGroup terminalGroup, SecurityProfile securityProfile, AuthorizationProfile authorizationProfile, 
			ClearingProfile clearingProfile, 
			FeeProfile feeProfile, 
			ChargeAssignmentPolicy chargePolicy, String acqMacKey, String acqPinKey, String issMacKey, String issPinKey, String masterkey
			,String name, String repname) throws Exception {

		String query = "from Institution i where ";
		Map<String, Object> param = new HashMap<String, Object>();
		if (FinancialEntityRole.MY_SELF.equals(role)){
			query += " i.institutionType = :type";
			param.put("type", FinancialEntityRole.MY_SELF);
		}else {
			query += " i.code = :code";
			param.put("code", code);
		}
		
		Institution institution = (Institution) GeneralDao.Instance.findObject(query, param);

		if (institution == null) {
			institution = createInstitution(feeProfile, name, repname);
			institution.setCode(code);
			institution.setBin(bin);
			institution.setInstitutionType(role);
			institution.setAuthorizationProfile(authorizationProfile);
			
			GeneralDao.Instance.saveOrUpdate(institution);
		}
//		institution.setBin(bin);
//		institution.setInstitutionType(role);
		
		//TODO
		/*if (FinancialEntityRole.MASTER.equals(role) || code == INST_EPAY_CODE) {
			Account account = new Account("Ø­Ø³Ø§Ø¨ Ú©Ø§Ø±Ù…Ø²Ø¯Ù‡Ø§ÛŒ Ø³ÙˆØ¦ÛŒÚ† Ø§ÛŒÙ†ØªØ±Ù†ØªÛŒ Ù¾Ø§Ø³Ø§Ø±Ú¯Ø§Ø¯", ACCOUNT_SWITCH, createCurrency(),
					Core.NEGIN_CORE);
			institution.setAccount(account);
		}*/
		
		GeneralDao.Instance.saveOrUpdate(institution); 
//		ClearingDateManager.getInstance().push(MonthDayDate.now(),DateTime.now(), institution);

		if (acquierTerminal != -1) {
			SwitchTerminal aTerminal = createAcquireTerminal(acquierTerminal, acqMacKey, acqPinKey, masterkey);
			aTerminal.setOwner(institution);
			aTerminal.setSecurityProfile(securityProfile);
			aTerminal.setAuthorizationProfile(authorizationProfile);
			aTerminal.setClearingProfile(clearingProfile);
			aTerminal.setParentGroup(terminalGroup);
			if(chargePolicy != null)
				aTerminal.setChargePolicy(chargePolicy);
//			institution.addTerminal(aTerminal);
			GeneralDao.Instance.saveOrUpdate(aTerminal);
		}

		if (issuerTerminal != -1) {
			SwitchTerminal iTerminal = createIssuerTerminal(issuerTerminal, issMacKey, issPinKey, masterkey);
			iTerminal.setSecurityProfile(securityProfile);
			iTerminal.setOwner(institution);
			iTerminal.setAuthorizationProfile(authorizationProfile);
			iTerminal.setClearingProfile(clearingProfile); 
			iTerminal.setParentGroup(terminalGroup);
//			institution.addTerminal(iTerminal);
			GeneralDao.Instance.saveOrUpdate(iTerminal);
		}

		GeneralDao.Instance.saveOrUpdate(institution);
	}

	private static Institution createInstitution(FeeProfile feeProfile, String name, String repname) throws Exception {
		Institution institution = new Institution();
		institution.setFeeProfile(feeProfile);
		institution.setContact(DBInitializeUtil.createContact(repname, GENERAL_ADDRESS, GENERAL_PHONE_NUMBER, GENERAL_WEB_ADDRESS));
		institution.setName(name);
		return institution;
	}
	
	private static SwitchTerminal createIssuerTerminal(long code, String macKey, String pinKey, String masterKey) throws Exception {
		SwitchTerminal terminal;
		terminal = TerminalService.findTerminal(SwitchTerminal.class, code);
		if (terminal == null)
			terminal = new SwitchTerminal(SwitchTerminalType.ISSUER);
		terminal.setCode(code);
		GeneralDao.Instance.saveOrUpdate(terminal);
		createKeySet(terminal, macKey, pinKey, masterKey);
		terminal.setEnabled(true);
		terminal.setCreatorUser(DBInitializeUtil.getUser());
		terminal.setCreatedDateTime(DateTime.now());
		
		GeneralDao.Instance.saveOrUpdate(terminal);
		return terminal;

	}

	private static SwitchTerminal createAcquireTerminal(long code, String macKey, String pinKey, String masterKey) throws Exception {
		SwitchTerminal terminal;
		terminal = TerminalService.findTerminal(SwitchTerminal.class, code);
		if (terminal == null)
			terminal = new SwitchTerminal(SwitchTerminalType.ACQUIER);

		terminal.setCode(code);
		GeneralDao.Instance.saveOrUpdate(terminal);

		createKeySet(terminal, macKey, pinKey, masterKey);
		terminal.setEnabled(true);
		terminal.setCreatorUser(DBInitializeUtil.getUser());
		terminal.setCreatedDateTime(DateTime.now());
		
		GeneralDao.Instance.saveOrUpdate(terminal);
		return terminal;
	}
	
	private static void createKeySet(Terminal terminal, String macKey, String pinKey, String masterKey) throws Exception {
		JCESecurityModule ssm = new JCESecurityModule("/src/main/resources/config/LMK.jceks", "$3cureP@$$".toCharArray(),
				"org.bouncycastle.jce.provider.BouncyCastleProvider");

		List<SecureDESKey> keysByType = new ArrayList<SecureDESKey>();

		if(macKey != null && !macKey.isEmpty()) {
			Key mkey = new SecretKeySpec(Hex.decode(macKey), "DES");
			SecureDESKey keyMac = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, mkey);
			terminal.addSecureKey(keyMac);
			GeneralDao.Instance.saveOrUpdate(keyMac);
		}

		if(pinKey != null && !pinKey.isEmpty()) {
			Key pkey = new SecretKeySpec(Hex.decode(pinKey), "DES");
			SecureDESKey keyPin = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, pkey);
			terminal.addSecureKey(keyPin);
			GeneralDao.Instance.saveOrUpdate(keyPin);
		}
		
		if(masterKey != null && !masterKey.isEmpty()) {
			if (masterKey.length() * 4 == SMAdapter.LENGTH_DES) {
				Key msKey = new SecretKeySpec(Hex.decode(masterKey), "DES");
				SecureDESKey keyMaster = ssm.encryptToLMK(SMAdapter.LENGTH_DES, KeyType.TYPE_TMK, msKey);
				terminal.addSecureKey(keyMaster);
				GeneralDao.Instance.saveOrUpdate(keyMaster);
			} else {
				Key msKey = new SecretKeySpec(Hex.decode(masterKey), "DESede");
				SecureDESKey keyMaster = ssm.encryptToLMK(SMAdapter.LENGTH_DES3_3KEY, KeyType.TYPE_TMK, msKey);
				terminal.addSecureKey(keyMaster);
				GeneralDao.Instance.saveOrUpdate(keyMaster);
			}
		}

		GeneralDao.Instance.saveOrUpdate(terminal);
	}
}
