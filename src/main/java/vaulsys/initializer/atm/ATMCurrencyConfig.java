package vaulsys.initializer.atm;

import vaulsys.calendar.DateTime;
import vaulsys.customer.Currency;
import vaulsys.initializer.DBInitializeUtil;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.protocols.ndc.constants.NDCFunctionIdentifierConstants;
import vaulsys.protocols.ndc.constants.NDCPrinterFlag;
import vaulsys.protocols.ndc.constants.ReceiptOptionType;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMDisplayFlag;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.atm.ATMResponse;
import vaulsys.terminal.atm.FunctionCommandResponse;
import vaulsys.terminal.atm.OARResponse;
import vaulsys.terminal.atm.Receipt;
import vaulsys.terminal.atm.ResponseScreen;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.terminal.impl.TerminalSharedFeature;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ATMCurrencyConfig  {
	abstract void OnError();
	
	public static HashMap<String, String> allFunctionsList;
	public static HashMap<String, String> selectedFunctionsList;
	
	
	private static Integer atmType = ATMType.OTHER;
	private static Integer bankType = BANKType.PASARGAD;
	private static Long Config = 32L;
	private static Boolean updateConfig = false;
	private static Boolean doCapture = false;	
	private static String Description = null;
	
	private static String updateablePageFontColor;
	private static String CONFIG_BANK_NAME_FA ;
	private static String CONFIG_BANK_NAME_EN ;
	private static String CONFIG_BANK_MOUNTTO_FA;
	private static String CONFIG_BANK_MOUNTTO_EN;
	
	
	private static final int CURRENCY_RIAL_CODE = 364;
	private static final Integer MAX_DESPENSING_NOTES = 40;
	private static final Integer RECEIPT_LINE_LENGTH = 40;
	private static final Integer RECEIPT_LEFT_MARGIN = 3;
	private static final String ATM_TERMINAL_GROUP_NAME = "خودپردازهای خارج از کشور";
	private static final String PARENT_TERMINAL_GROUP_NAME = "خودپردازها";
	
	
	/*PASARGAD*/
	private static final String pasargad_updateablePageFontColor = "B0;80m";
	//AldTODO before 	private static final String pasargad_updateablePageFontColor = "00;80m";
	
	private static final String PASARGAD_CONFIG_BANK_NAME_FA = "بانک پاسارگاد";
	private static final String PASARGAD_CONFIG_BANK_NAME_EN = "Bank Pasargad";
	private static final String PASARGAD_CONFIG_BANK_MOUNTTO_FA = "بانک هزاره سوم";
	private static final String PASARGAD_CONFIG_BANK_MOUNTTO_EN = "Bank Of The 3rd Millennium";
	
	

	private static final String ATM_ENGLISH_ENCODING = "1";
	private static final String ATM_RECEIPT_CONVERTOR = "STANDARD_NDC_CONVERTOR";
	private static final String ATM_SCREEN_CONVERTOR = "FANAP_NDC_CONVERTOR";
	private static final String ATM_FARSI_RCPT_ENCODING = "7";
	private static final String ATM_FARSI_EXT_RCPT_ENCODING = null;
	private static final String ATM_FARSI_SCREEN_ENCODING = (!atmType.equals(ATMType.NCR)) ? "K" : "6";
	private static final String ATM_FARSI_EXT_SCREEN_ENCODING = (!atmType.equals(ATMType.NCR)) ? "K" : "6";
	
	private static final String BANK_NAME_FA = "GR c2F(GR bnkName2F())"; 
	private static final String BANK_NAME_EN = (!atmType.equals(ATMType.NCR)) ? "GR bnkName2E()" : "GR bnkName2NCRE()";
	private static final String BANK_MOUNTTO_FA = "GR c2F(GR bnkMount2F())";
	private static final String BANK_MOUNTTO_EN = (!atmType.equals(ATMType.NCR)) ? "GR bnkMount2E()" : "GR bnkMount2NCRE()" ;
	
	private static Currency currency ;
	
	private static final String lineFa = (!atmType.equals(ATMType.NCR)) ? "[ESC](1[GR hr(0xcd)][ESC](7" : "[GR hr(0xcd)]"; 
	private static final String lineEn = (!atmType.equals(ATMType.NCR)) ? "[ESC](1[GR hr(0xcd)][ESC](1" : "[GR hr(0xcd)]"; 
	
	
//	private static final String openDoubleQuotationFa = "[ESC](1[0xAF][ESC](7"; 
//	private static final String closeDoubleQuotationFa = "[ESC](1[0xAE][ESC](7"; 
	private static final String openDoubleQuotationEn = "[0xAE]"; 
	private static final String closeDoubleQuotationEn = "[0xAF]"; 

	private static final String newLine = "[LF]";

	private static final String headerFa = (!atmType.equals(ATMType.NCR)) ? 
		"[ESC](7[GR center("+BANK_NAME_FA+") ]"
		+ "[LF][LF][GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))]" 
		:  //NCR
		"[GR center("+BANK_NAME_FA+") ]"
		+ "[LF][LF][GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))]"			
		;

	private static final String headerEn = (!atmType.equals(ATMType.NCR)) ? 
										"[GR center("+BANK_NAME_EN+")]" 
										+ "[LF][LF][GR center(GR ' ATM '+ GR atm.getOwner().getNameEn() +' '+GR ifx.TerminalId)]"
										: //NCR
										"[GR center(GR c2NCRE("+BANK_NAME_EN+"))]"
										+ "[LF][LF][GR center(GR c2NCRE(GR ' ATM '+ GR atm.getOwner().getNameEn() +' '+GR ifx.TerminalId))]";
	
	
	private static final String footerFa = (!atmType.equals(ATMType.NCR)) ? 
				"[ESC](1[GR hr(0xcd)][ESC](7"
				+ "[GR center(GR "+BANK_MOUNTTO_FA+" )]"
				+ "[FF]"
				: //NCR
				"[GR hr(0xcd)]"
				+ "[GR center(GR "+BANK_MOUNTTO_FA+" )]"
				+ "[FF]";
	
	private static final String footerEn =  (!atmType.equals(ATMType.NCR)) ?
				  "[LF][GR hr(0xcd)]" 
				+ "[GR center(GR "+BANK_NAME_EN+")]"
				+ "[FF]"
				: //NCR
				"[LF][GR hr(0xcd)]"
				+ "[GR center(GR c2NCRE("+BANK_NAME_EN+"))]"
				+ "[FF]";

	
	private static final String receivedDateFa = "[GR datePersianFormat(ifx.receivedDt)]"; 
	private static final String receivedDateEn = (!atmType.equals(ATMType.NCR)) ? "[GR dateEnglishFormat(ifx.receivedDt)]" : "[GR dateEnglishNCRFormat(ifx.receivedDt)]"; 

	private static final String formatAppPanFa = "[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))]"; 
	private static final String formatAppPanEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR appPanEn(ifx.actualAppPAN), 'Card No.')]" : "[GR justify(GR appPanNCREn(ifx.actualAppPAN), GR c2NCRE('Card No.'))]";
	
	private static final String transferAppPanFa = "[GR justify(GR c2F('از کارت'), GR appPanFa(ifx.actualAppPAN))]"; //TASK Task029 : Print Bank Name 
	private static final String transferAppPanEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR appPanEn(ifx.actualAppPAN), 'From Card No.')]"
													: "[GR justify(GR appPanNCREn(ifx.actualAppPAN), GR c2NCRE('From Card No.'))]"; //TASK Task029 : Print Bank Name
	
	
	private static final String transferSecAppPanFa = "[GR justify(GR c2F('به کارت'), GR appPanFa(ifx.actualSecondAppPan))]";//TASK Task029 : Print Bank Name
	private static final String transferSecAppPanEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR appPanEn(ifx.actualSecondAppPan), 'To Card No.')]"
													: "[GR justify(GR appPanNCREn(ifx.actualSecondAppPan), GR c2NCRE('To Card No.'))]"; //TASK Task029 : Print Bank Name
	
	//AldTODO Task029
	private static final String transferBankNameFa = "[GR right(GR c2F('از '+ GR banknameFa(ifx.DestBankId)))]"; //TASK Task029 : Print Bank Name 
	private static final String transferBankNameEn = (!atmType.equals(ATMType.NCR)) ? "[GR safeEn('From '+ GR banknameEn(ifx.DestBankId))]" : "[GR safeEn('From '+ GR c2NCRE(GR banknameEn(ifx.DestBankId)))]"; //TASK Task029 : Print Bank Name

	//AldTODO Task029
	private static final String transferSecBankNameFa = "[GR right(GR c2F('به '+ GR banknameFa(ifx.RecvBankId)))]"; //TASK Task029 : Print Bank Name 
	private static final String transferSecBankNameEn = (!atmType.equals(ATMType.NCR)) ? "[GR safeEn('To '+ GR banknameEn(ifx.RecvBankId))]" : "[GR safeEn('To '+ GR c2NCRE(GR banknameEn(ifx.RecvBankId)))]"; //TASK Task029 : Print Bank Name
	

	private static final String seqCntrFa = "[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))]";
	private static final String seqCntrEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR ifx.Src_TrnSeqCntr, 'Reference No.')]" : "[GR justify(GR ifx.Src_TrnSeqCntr, GR c2NCRE('Reference No.'))]";
	
	private static final String amountFa = "[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))]";
	private static final String amountEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2E(ifx.Auth_Amt,15) , 'Amount')]" : "[GR justify(GR amount2E(ifx.Auth_Amt,15) , GR c2NCRE('Amount'))]";
	
	//Mirkamali(TaskT79): Currency ATM
	private static final String amountRealEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2ECurrency(ifx.Real_Amt,15) , 'Currency Amount')]" : "[GR justify(GR amount2ECurrency(ifx.Real_Amt,15) , GR c2NCRE('Currency Amount'))]";
	private static final String curRateEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2E(ifx.Auth_CurRate,15) , 'Currency Rate')]" : "[GR justify(GR amount2E(ifx.Auth_CurRate,15) , GR c2NCRE('Currency Rate'))]";
	private static final String totalFeeEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2E(ifx.TotalFeeAmt,15) , 'Total Fee')]" : "[GR justify(GR amount2E(ifx.TotalFeeAmt,15) , GR c2NCRE('Total Fee'))]";
	
	private static final String amountPartialFa = "[GR justify(GR c2F('مبلغ'), GR amount2F(partialDispense(ifx),15))]";
	private static final String amountPartialEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR partialDispense(ifx) , 'Amount')]" : "[GR justify(GR partialDispense(ifx) , GR c2NCRE('Amount'))]"; 
	
	
	private static final String accBalLedgerFa = "[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))]";
	private static final String accBalLedgerEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2E(ifx.AcctBalLedgerAmt, 15), 'Ledger Amount')]" : "[GR justify(GR amount2E(ifx.AcctBalLedgerAmt, 15), GR c2NCRE('Ledger Amount'))]";
	private static final String accBalAvailableFa = "[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, 15))]";
	private static final String accBalAvailableEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2E(ifx.AcctBalAvailableAmt, 15), 'Available Amount')]" : "[GR justify(GR amount2E(ifx.AcctBalAvailableAmt, 15), GR c2NCRE('Available Amount'))]";
	private static final String subAccFa = "[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))]";	
	private static final String subAccEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(accountFormat(ifx.subsidiaryAccFrom), GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'Account Number'))]" : "[GR justify(accountFormat(ifx.subsidiaryAccFrom), GR subsidiaryState2NCRE(ifx.subsidiaryAccFrom, 'Account Number'))]";
	
	private static final String accBalLedgerCashFa = "[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, ifx, 15))]"; 
	private static final String accBalLedgerCashEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2E(ifx.AcctBalLedgerAmt, ifx, 15) , 'Ledger Amount')]" : "[GR justify(GR amount2E(ifx.AcctBalLedgerAmt, ifx, 15) , GR c2NCRE('Ledger Amount'))]"; 
	private static final String accBalAvailableCashFa = "[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, ifx, 15))]"; 
	private static final String accBalAvailableCashEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR amount2E(ifx.AcctBalAvailableAmt, ifx, 15) + ' Rials', 'Available Amount')]" : "[GR justify(GR amount2E(ifx.AcctBalAvailableAmt, ifx, 15) + ' rials'), GR c2NCRE('Available Amount'))]"; 
	
	
	//TASK Task001 : Add Sheba
	private static final String shebaCodeFa = "[GR justify(GR c2F('شماره شبا'), GR sheba2F(ifx.shebaCode))]";
	private static final String shebaCodeEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR ifx.shebaCode, 'Sheba Code')]" : "[GR justify(GR ifx.shebaCode, GR c2NCRE('Sheba Code'))]";

	//TASK Task002 : Transfer Card To Account
	private static final String transferSecAccountFa = "[GR justify(GR c2F('به  سپرده'), GR accFa(ifx.actualSecondAppPan))]"; //change in 92.04.02
	private static final String transferSecAccountEn = (!atmType.equals(ATMType.NCR)) ? "[GR justify(GR accEn(ifx.actualSecondAppPan), 'To Account No.')]" : "[GR justify(GR accNCREn(ifx.actualSecondAppPan), GR c2NCRE('To Account No.'))]"; 
	

	static {
		allFunctionsList = new HashMap<String, String>();
		allFunctionsList.put("WithdrawalCurrency", "برداشت ارز");
		allFunctionsList.put("purchaseMTNCharge", "خرید شارژ ایرانسل");
		allFunctionsList.put("purchaseMCICharge", "خرید شارژ همراه اول");
		allFunctionsList.put("purchaseRightelCharge", "خرید شارژ رایتل");
		allFunctionsList.put("purchaseTaliaCharge", "خرید شارژ تالیا");//
		allFunctionsList.put("Balance", "درخواست موجودی");
		allFunctionsList.put("BankStatement", "صورتحساب");
		allFunctionsList.put("CharityHelp", "کمک به خیریه");
		allFunctionsList.put("Transfer", "انتقال به کارت");
		allFunctionsList.put("TransferToAccount", "انتقال به سپرده");
		allFunctionsList.put("ChangePinBlock", "تغییر رمز");
		allFunctionsList.put("ChangeInternetPinBlock", "تغییر رمز دوم");
		allFunctionsList.put("CreditStatementData", "صورتحساب اعتباری");
		allFunctionsList.put("Billpayment", "پرداخت قبض");
	}
	
	public static void main(String[] args) {
		System.out.println("Please check assign Config and ATMType and updateConfig and doCapture and bankType? [yes/no]");
		String inputvalue = readString().toLowerCase().replace("\r", "");
		if (!inputvalue.equals("yes")) {
			System.out.println("exit !!!");
			System.exit(0);
		}
		
		GeneralDao.Instance.beginTransaction();
		try {
			
			GlobalContext.getInstance().startup();
			ProcessContext.get().init();
			
			currency = GlobalContext.getInstance().getCurrency(CURRENCY_RIAL_CODE);
			ATMCurrencyConfig.setSelectedFunctionList(allFunctionsList.values().toArray());
			new ATMCurrencyConfig(){

				@Override
				void OnError() {
					// TODO Auto-generated method stub
					
				}}.addConfig();
			
			System.err.println("$$$........................ is Complete ..........................$$$");
			GeneralDao.Instance.endTransaction();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
	}
	
	public /*static */void main(Long config,String bankName,String atmTypeStr,Boolean doCaptureCard,String desc){
		Config = config;
		atmType = ATMType.getATMType(atmTypeStr);
		bankType = BANKType.getBankType(bankName);
		doCapture = doCaptureCard;
		Description = desc;
		GeneralDao.Instance.beginTransaction();
		try {
			
			GlobalContext.getInstance().startup();
			ProcessContext.get().init();
			
			currency = GlobalContext.getInstance().getCurrency(CURRENCY_RIAL_CODE);
			addConfig();
//			new ATMPasargadConfig().addConfig();
			
			System.err.println("$$$........................ is Complete ..........................$$$");
			GeneralDao.Instance.endTransaction();
			//System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}		
	}
	
	public ATMCurrencyConfig() {
		
	}
	
	public ATMCurrencyConfig(Long config,String bankName,String atmTypeStr,Boolean doCaptureCard,String desc) {
		main(config,bankName,atmTypeStr,doCaptureCard,desc);
	}
	
	
	
	public void setBankParameter(Integer bankType ){
    	if (BANKType.PASARGAD.equals(bankType)){
    		updateablePageFontColor = pasargad_updateablePageFontColor;
    		CONFIG_BANK_NAME_FA = PASARGAD_CONFIG_BANK_NAME_FA;
    		CONFIG_BANK_NAME_EN = PASARGAD_CONFIG_BANK_NAME_EN;
    		CONFIG_BANK_MOUNTTO_FA = PASARGAD_CONFIG_BANK_MOUNTTO_FA;
    		CONFIG_BANK_MOUNTTO_EN = PASARGAD_CONFIG_BANK_MOUNTTO_EN;
    	} 
	}
	
    public void addConfig() throws Exception {
    	currency = DBInitializeUtil.findCurrency(CURRENCY_RIAL_CODE);
    	TerminalGroup atmTerminalGroup = getTerminalGroup(ATM_TERMINAL_GROUP_NAME);
    	setBankParameter(bankType);
    	if (BANKType.PASARGAD.equals(bankType)){
    		if(updateConfig)
    			updatePasargadConfig();
    		else
    			addPasargadConfig(atmTerminalGroup);
    	} 
    }
    
    private TerminalGroup getTerminalGroup(String name) {
    	TerminalGroup group = findTerminalGroup(name);
    	if (group != null)
    		return group;
    	
    	return createTerminalGroup(name);
    }
    
    private TerminalGroup createTerminalGroup(String name) {

		TerminalGroup terminalGroup = new TerminalGroup();
		TerminalSharedFeature sharedFeature = new TerminalSharedFeature();

		terminalGroup.setEnabled(true);
		terminalGroup.setName(name);
	
		String query = "from TerminalGroup tg where tg.name = :name";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", PARENT_TERMINAL_GROUP_NAME);
		TerminalGroup parentGroup = (TerminalGroup) getGeneralDao().findObject(query, params);
		terminalGroup.setParentGroup(parentGroup);
		sharedFeature = parentGroup.getSafeSharedFeature().copy();
		
		terminalGroup.setOwnFeeProfile(false);
		terminalGroup.setOwnSecurityProfile(false);
		terminalGroup.setOwnAuthorizationProfile(false);
		terminalGroup.setOwnClearingProfile(false);
		sharedFeature.setEnabled(true);
		getGeneralDao().saveOrUpdate(sharedFeature);
		
		terminalGroup.setSharedFeature(sharedFeature );
		terminalGroup.setCreatorUser(GlobalContext.getInstance().getSwitchUser());
		terminalGroup.setCreatedDateTime(DateTime.now());
		
		getGeneralDao().saveOrUpdate(terminalGroup);
		
		return terminalGroup;
	}
    
    private TerminalGroup findTerminalGroup(String name) {
    	String query = "from " + TerminalGroup.class.getName() + " ap where ap.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
        return (TerminalGroup) getGeneralDao().findObject(query, param);
	}

    
    private void addPasargadConfig(TerminalGroup atmTerminalGroup) throws Exception {
		ATMConfiguration configuration = null;
		if (!updateConfig){
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", Config);
			configuration = (ATMConfiguration) GeneralDao.Instance.findObject("from ATMConfiguration atmconfig where atmconfig.id = :id ", params);
			if (configuration != null )
			{
				OnError();
				return;
			} else {
				configuration = new ATMConfiguration();
				configuration.setId(Config);
			}
		}
		else
			configuration = GeneralDao.Instance.load(ATMConfiguration.class, Config);
		
		if (!atmType.equals(ATMType.NCR)) {	
			if (!Util.hasText(Description))
				configuration.setName("تنظیمات پیش فرض خودپرداز ارزی");
			else
				configuration.setName(Description);
				
		}
		else
		{
			if (!Util.hasText(Description))
				configuration.setName("نظیمات پیش فرض خودپرداز ارزی - NCR");
			else
				configuration.setName(Description+ " - NCR");
		}
		configuration.setMaxDespensingNotes(MAX_DESPENSING_NOTES);
		configuration.setReceiptLineLength(RECEIPT_LINE_LENGTH);
		configuration.setReceiptLeftMargin(RECEIPT_LEFT_MARGIN);
		configuration.setBnkFarsiName(CONFIG_BANK_NAME_FA);
		configuration.setBnkFarsiMount(CONFIG_BANK_MOUNTTO_FA);//me
		configuration.setBnkEnglishName(CONFIG_BANK_NAME_EN);//me
		configuration.setBnkEnglishMount(CONFIG_BANK_MOUNTTO_EN);//me 
		
		
//		configuration.setCassetteADenomination(500000);
//		configuration.setCassetteACurrency(currency);
//		configuration.setCassetteBDenomination(100000);
//		configuration.setCassetteBCurrency(currency);
//		configuration.setCassetteCDenomination(50000);
//		configuration.setCassetteCCurrency(currency);
//		configuration.setCassetteDDenomination(20000);
//		configuration.setCassetteDCurrency(currency);
		
		configuration.setEnglish_encoding(ATM_ENGLISH_ENCODING);
		configuration.setFarsi_reciept_encoding(ATM_FARSI_RCPT_ENCODING);
		configuration.setFarsi_extended_reciept_encoding(ATM_FARSI_EXT_RCPT_ENCODING);
		configuration.setFarsi_screen_encoding(ATM_FARSI_SCREEN_ENCODING);
		configuration.setFarsi_extended_screen_encoding(ATM_FARSI_EXT_SCREEN_ENCODING);
		
		configuration.setReceiptConvertor(ATM_RECEIPT_CONVERTOR);
		configuration.setScreenConvertor(ATM_SCREEN_CONVERTOR);
		
		configuration.setIsCurrencyConfig(true);
		
		getGeneralDao().saveOrUpdate(configuration);
		
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ResponseScreen screenFa = new ResponseScreen();
		screenFa.setLanguage(UserLanguage.FARSI_LANG);
		screenFa.setScreenno(null);
		if (!atmType.equals(ATMType.NCR)) {
			screenFa.setScreenData("[FF][SI]@@[ESC]P2068[ESC]\\" 
					+ "[ESC](K" 
					+ "[ESC][OC]B0;80m");
		} else {
			screenFa.setScreenData("[FF][SI]@@[ESC]P2068[ESC]\\" 
					+ "[ESC](6" 
					+ "[ESC][OC]B0;80m");
		}
			
		screenFa.setDesc("لیست حساب های فرعی- فارسی");
		getGeneralDao().saveOrUpdate(screenFa);
		
		
		ResponseScreen screenEn = new ResponseScreen();
		screenEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenEn.setScreenno(null);
		screenEn.setScreenData("[FF][SI]@@[ESC]P2468[ESC]\\" 
				+ "[ESC](1" 
				+ "[ESC][OC]B0;80m");
		screenEn.setDesc("لیست حساب های فرعی- انگلیسی");
		getGeneralDao().saveOrUpdate(screenEn);
		
		OARResponse responsePasargadSubsidiary = new OARResponse();
		responsePasargadSubsidiary.setAllNumericKeys(false);
		responsePasargadSubsidiary.setDisplayFlag(ATMDisplayFlag.DISPLAY);
		responsePasargadSubsidiary.setOpKeyA(true);
		responsePasargadSubsidiary.setOpKeyB(true);
		responsePasargadSubsidiary.setOpKeyC(true);
		responsePasargadSubsidiary.setOpKeyD(false);
		responsePasargadSubsidiary.setOpKeyF(true);
		responsePasargadSubsidiary.setOpKeyG(true);
		responsePasargadSubsidiary.setOpKeyH(true);
		responsePasargadSubsidiary.setOpKeyI(true);
		responsePasargadSubsidiary.setCancelKey(true);
		responsePasargadSubsidiary.setScreenTimer("030");
		responsePasargadSubsidiary.addScreen(screenEn);
		responsePasargadSubsidiary.addScreen(screenFa);
		
		
		getGeneralDao().saveOrUpdate(responsePasargadSubsidiary);
		
		
		if (CheckMustBeAddThisFunction("WithdrawalCurrency")) {
			List<ATMRequest> requestsWithdrawal = WithdrawalCurrency(configuration, responsePasargadSubsidiary);
			if (requestsWithdrawal != null)
				requests.addAll(requestsWithdrawal);
		}
		
		if (CheckMustBeAddThisFunction("Balance")) {
			List<ATMRequest> requestsBalance = Balance(configuration, responsePasargadSubsidiary);
			if (requestsBalance != null)
				requests.addAll(requestsBalance);
		}
		
		if (CheckMustBeAddThisFunction("BankStatement")) {
			List<ATMRequest> requestsBankStatement = BankStatement(configuration, responsePasargadSubsidiary);
			if (requestsBankStatement != null)
				requests.addAll(requestsBankStatement);
		}
		
//		List<ATMRequest> requestsCancel = Cancel(configuration);
//		if (requestsCancel != null)
//			requests.addAll(requestsCancel);
		
		if (CheckMustBeAddThisFunction("Billpayment")) {
			List<ATMRequest> requestsBillpayment = Billpayment(configuration, responsePasargadSubsidiary);
			if (requestsBillpayment != null)
				requests.addAll(requestsBillpayment);
		}
		
		
		if (CheckMustBeAddThisFunction("purchaseMTNCharge")) {
			List<ATMRequest> requestsCharge = purchaseMTNCharge(configuration, responsePasargadSubsidiary);
			if (requestsCharge != null)
				requests.addAll(requestsCharge);
		}
		
		
		if (CheckMustBeAddThisFunction("purchaseMCICharge")) {
			List<ATMRequest> requestsMCICharge = purchaseMCICharge(configuration, responsePasargadSubsidiary);
			if (requestsMCICharge != null)
				requests.addAll(requestsMCICharge);
		}
		
		
		if (CheckMustBeAddThisFunction("purchaseRightelCharge")) {
			List<ATMRequest> requestsRightelCharge = purchaseRightelCharge(configuration, responsePasargadSubsidiary);
			if (requestsRightelCharge != null)
				requests.addAll(requestsRightelCharge);			
		}
		
		if (CheckMustBeAddThisFunction("purchaseTaliaCharge")) {
			List<ATMRequest> requestsTaliaCharge = purchaseTaliaCharge(configuration, responsePasargadSubsidiary);
			if (requestsTaliaCharge != null)
				requests.addAll(requestsTaliaCharge);			
		}		
		
		if (CheckMustBeAddThisFunction("Transfer")) {
			List<ATMRequest> requestsTransfer = Transfer(configuration, responsePasargadSubsidiary);
			if (requestsTransfer != null)
				requests.addAll(requestsTransfer);
		}
		
		//AldTODO Task002 : Transfer Card To Account
		if (CheckMustBeAddThisFunction("TransferToAccount")) {
			List<ATMRequest> requestsTransferToAccount = TransferToAccount(configuration, responsePasargadSubsidiary);
			if (requestsTransferToAccount != null)
				requests.addAll(requestsTransferToAccount);
		}
		
		if (CheckMustBeAddThisFunction("ChangePinBlock")) {
			List<ATMRequest> requestsChangePinBlock = ChangePinBlock(configuration);
			if (requestsChangePinBlock != null)
				requests.addAll(requestsChangePinBlock);
		}
		
		if (CheckMustBeAddThisFunction("ChangeInternetPinBlock")) {
			List<ATMRequest> requestsChangeInternetPinBlock = ChangeInternetPinBlock(configuration);
			if (requestsChangeInternetPinBlock != null)
				requests.addAll(requestsChangeInternetPinBlock);
		}
		
		if (CheckMustBeAddThisFunction("CreditStatementData")) {
			List<ATMRequest> requestsCreditStatementData = CreditStatementData(configuration);
			if (requestsCreditStatementData != null)
				requests.addAll(requestsCreditStatementData);
		}
		//Task019
		if (CheckMustBeAddThisFunction("CharityHelp")) {
			List<ATMRequest> requestsThirdPArtyPayment = CharityHelp(configuration, responsePasargadSubsidiary);
			if(requestsThirdPArtyPayment != null)
				requests.addAll(requestsThirdPArtyPayment);
		}
		
		
		for (ATMRequest atmRequest : requests) {
			configuration.addRequset(atmRequest);
			getGeneralDao().saveOrUpdate(atmRequest);
		}
	
	
		
		/*********** Error Receipt ************/
//		String textError = "[LF][SO]5[ESC][LF]TRANSACTION NOT APPROVEDFUL[LF][LF]BE OMIDE DIDAR[FF]";
//		ArrayList<Receipt> receiptListError = new ArrayList<Receipt>();
//		Receipt receiptError = new Receipt();
//		receiptListError.add(receiptError);
//		receiptError.setText(textError);
//		receiptError.setPrinterFlag(NDCPrinterFlag.DONT_PRINT);
		
//		getGeneralDao().save(receiptError);
		/**************************************/
		Map<Integer, ATMResponse> responseMap = sharedResponsesShetab();
		for (Integer integer : responseMap.keySet()) {
			getGeneralDao().saveOrUpdate(responseMap.get(integer));
			configuration.addResponse(FITType.SHETAB, integer, responseMap.get(integer));
		}
		
		responseMap = sharedResponsesPasargad();
		for (Integer integer : responseMap.keySet()) {
			getGeneralDao().saveOrUpdate(responseMap.get(integer));
			configuration.addResponse(FITType.PASARGAD, integer, responseMap.get(integer));
		}
		
		responseMap = sharedResponsesCreditPasargad();
		for (Integer integer : responseMap.keySet()) {
			getGeneralDao().saveOrUpdate(responseMap.get(integer));
			configuration.addResponse(FITType.CREDIT_PASARGAD, integer, responseMap.get(integer));
		}
		
		getGeneralDao().saveOrUpdate(configuration);
	}

	
	
	private boolean CheckMustBeAddThisFunction(String feautreName) {
		return selectedFunctionsList == null || selectedFunctionsList.containsKey(feautreName);
	}
	
	public List<ATMRequest> WithdrawalCurrency(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();

		

		ATMRequest atmRequestPreWithdrawalShetabFa = new ATMRequest();
		ATMRequest atmRequestPreWithdrawalShetabEn = new ATMRequest();
		ATMRequest atmRequestPreWithdrawalPasargadFa = new ATMRequest();
		ATMRequest atmRequestPreWithdrawalPasargadEn = new ATMRequest();

		ATMRequest atmRequestTimeOutPreWithdrawalShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreWithdrawalShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPreWithdrawalPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreWithdrawalPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabFa = new ATMRequest();//Task019

		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabEn = new ATMRequest();//Task019

		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadFa = new ATMRequest();//Task019
		

		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubFa = new ATMRequest();//Task019

		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadEn = new ATMRequest();//Task019
		

		ATMRequest atmRequestPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubEn = new ATMRequest();//Task019
		
		/*************************** Baresi shavad ******************************/
		String customerReceiptFaText ="[GR simpleWithdrawalCurReceiptFa()]";
		String customerReceiptEnText = 
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('CurrencyWithdrawal Receipt')]" : "[LF][LF][GR center(GR c2NCRE('CurrencyWithdrawal Receipt'))]")
			+ newLine  
			+ lineEn
			+ receivedDateEn
			+ newLine + newLine
			+ formatAppPanEn
			+ newLine  
			+ seqCntrEn
			+ newLine  
			+ amountEn
			+ newLine
			+ amountRealEn
			+ newLine
			+ curRateEn
			+ newLine
			+ totalFeeEn
			+ newLine
			+ accBalLedgerEn
			+ newLine  
			+ accBalAvailableEn
			+ newLine  
			+ subAccEn
			+ "[GR putLF(8)]"
			+ footerEn;
		
		String journalReceiptText = "[GR simpleWithdrawalCurJournal()]" ;		
		/**************************************************************************/

		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();

		Receipt receiptFa = new Receipt();
		receiptFa.setText(customerReceiptFaText);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList.add(receiptFa);

		Receipt journal = new Receipt();
		journal.setText(journalReceiptText);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(customerReceiptEnText);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList.add(receiptEn);


		/*****************************/

		atmRequestPreWithdrawalShetabFa.setOpkey("AAAC    ");
		atmRequestPreWithdrawalShetabFa.setIfxType(IfxType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalShetabFa.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalShetabFa.setCurrency(currency);
		atmRequestPreWithdrawalShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreWithdrawalShetabFa.setFit(FITType.SHETAB);

		atmRequestTimeOutPreWithdrawalShetabFa.setOpkey("AAAC   F");
		atmRequestTimeOutPreWithdrawalShetabFa.setIfxType(IfxType.PREPARE_WITHDRAWAL_REV_REPEAT);
		atmRequestTimeOutPreWithdrawalShetabFa.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestTimeOutPreWithdrawalShetabFa.setCurrency(currency);
		atmRequestTimeOutPreWithdrawalShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreWithdrawalShetabFa.setFit(FITType.SHETAB);

		atmRequestPreWithdrawalShetabEn.setOpkey("IAAC    ");
		atmRequestPreWithdrawalShetabEn.setIfxType(IfxType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalShetabEn.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalShetabEn.setCurrency(currency);
		atmRequestPreWithdrawalShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreWithdrawalShetabEn.setFit(FITType.SHETAB);

		atmRequestTimeOutPreWithdrawalShetabEn.setOpkey("IAAC   F");
		atmRequestTimeOutPreWithdrawalShetabEn.setIfxType(IfxType.PREPARE_WITHDRAWAL_REV_REPEAT);
		atmRequestTimeOutPreWithdrawalShetabEn.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestTimeOutPreWithdrawalShetabEn.setCurrency(currency);
		atmRequestTimeOutPreWithdrawalShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreWithdrawalShetabEn.setFit(FITType.SHETAB);




		atmRequestPreWithdrawalPasargadFa.setOpkey("ABAC    ");
		atmRequestPreWithdrawalPasargadFa.setIfxType(IfxType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalPasargadFa.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalPasargadFa.setCurrency(currency);
		atmRequestPreWithdrawalPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreWithdrawalPasargadFa.setFit(FITType.PASARGAD);

		atmRequestTimeOutPreWithdrawalPasargadFa.setOpkey("ABAC   F");
		atmRequestTimeOutPreWithdrawalPasargadFa.setIfxType(IfxType.PREPARE_WITHDRAWAL_REV_REPEAT);
		atmRequestTimeOutPreWithdrawalPasargadFa.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestTimeOutPreWithdrawalPasargadFa.setCurrency(currency);
		atmRequestTimeOutPreWithdrawalPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreWithdrawalPasargadFa.setFit(FITType.PASARGAD);

		atmRequestPreWithdrawalPasargadEn.setOpkey("IBAC    ");
		atmRequestPreWithdrawalPasargadEn.setIfxType(IfxType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalPasargadEn.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestPreWithdrawalPasargadEn.setCurrency(currency);
		atmRequestPreWithdrawalPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreWithdrawalPasargadEn.setFit(FITType.PASARGAD);

		atmRequestTimeOutPreWithdrawalPasargadEn.setOpkey("IBAC   F");
		atmRequestTimeOutPreWithdrawalPasargadEn.setIfxType(IfxType.PREPARE_WITHDRAWAL_REV_REPEAT);
		atmRequestTimeOutPreWithdrawalPasargadEn.setTrnType(TrnType.PREPARE_WITHDRAWAL);
		atmRequestTimeOutPreWithdrawalPasargadEn.setCurrency(currency);
		atmRequestTimeOutPreWithdrawalPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreWithdrawalPasargadEn.setFit(FITType.PASARGAD);

		/*****************************/

		atmRequestShetabFa.setOpkey("AAAD    ");
		atmRequestShetabFa.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestShetabFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabFa.setOpkey("AAAD   F");
		atmRequestTimeOutShetabFa.setNextOpkey("AAAA   F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);

		//Task019 
		atmRequestReceiptOptionShetabFa.setOpkey("AAAD   B");
		atmRequestReceiptOptionShetabFa.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestReceiptOptionShetabFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestReceiptOptionShetabFa.setCurrency(currency);
		atmRequestReceiptOptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionShetabFa.setFit(FITType.SHETAB);
		atmRequestReceiptOptionShetabFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestShetabEn.setOpkey("IAAD    ");
		atmRequestShetabEn.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestShetabEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabEn.setOpkey("IAAD   F");
		atmRequestTimeOutShetabEn.setNextOpkey("IAAA   F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);

		//Task019 
		atmRequestReceiptOptionShetabEn.setOpkey("IAAD   B");
		atmRequestReceiptOptionShetabEn.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestReceiptOptionShetabEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestReceiptOptionShetabEn.setCurrency(currency);
		atmRequestReceiptOptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionShetabEn.setFit(FITType.SHETAB);
		atmRequestReceiptOptionShetabEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestPasargadFa.setOpkey("ABADA   ");
		atmRequestPasargadFa.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadFa.setOpkey("ABADA  F");
		atmRequestTimeOutPasargadFa.setNextOpkey("ABAAA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadFa.setOpkey("ABADA  B");
		atmRequestReceiptOptionPasargadFa.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestReceiptOptionPasargadFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestReceiptOptionPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadFa.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestPasargadSubFa.setOpkey("ABADB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABADA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadSubFa.setOpkey("ABADB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABADB  F");
//				atmRequestTimeOutPasargadSubFa.setNextOpkey("ABAAA  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadSubFa.setOpkey("ABADB  B");
		atmRequestReceiptOptionPasargadSubFa.setNextOpkey("ABADA  B");
		atmRequestReceiptOptionPasargadSubFa.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubFa.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestReceiptOptionPasargadSubFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadSubFa.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadSubFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);		
		/*****************************/

		atmRequestPasargadEn.setOpkey("IBADA   ");
		atmRequestPasargadEn.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadEn.setOpkey("IBADA  F");
		atmRequestTimeOutPasargadEn.setNextOpkey("IBADA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadEn.setOpkey("IBADA  B");
		atmRequestReceiptOptionPasargadEn.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestReceiptOptionPasargadEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestReceiptOptionPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadEn.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);		
		
		/*********************/
		atmRequestPasargadSubEn.setOpkey("IBADB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBADA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadSubEn.setOpkey("IBADB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBADB  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);
//		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ); //Task019 : Bug Repaire
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.WITHDRAWAL_CUR);
//		atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT); //Task019 : Bug Repaire
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadSubEn.setOpkey("IBADB  B");
		atmRequestReceiptOptionPasargadSubEn.setNextOpkey("IBADA  B");
		atmRequestReceiptOptionPasargadSubEn.setIfxType(IfxType.WITHDRAWAL_CUR_RQ);
		atmRequestReceiptOptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubEn.setTrnType(TrnType.WITHDRAWAL_CUR);
		atmRequestReceiptOptionPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestReceiptOptionPasargadSubEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadSubEn.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadSubEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);	
		
		/*****************************/

		List<ResponseScreen> screenAPPROVED = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenAPPROVEDFa = new ResponseScreen();
		screenAPPROVEDFa.setScreenno("394");
		screenAPPROVEDFa.setDesc("برداشت  ارز-موفق فارسی");
		screenAPPROVEDFa.setLanguage(UserLanguage.FARSI_LANG);
		screenAPPROVEDFa.setScreenData(null);
		getGeneralDao().saveOrUpdate(screenAPPROVEDFa);
		screenAPPROVED.add(screenAPPROVEDFa);
		
		ResponseScreen screenAPPROVEDEn = new ResponseScreen();
		screenAPPROVEDEn.setScreenno("794");
		screenAPPROVEDEn.setDesc("برداشت  ارز-موفق انگلیسی");
		screenAPPROVEDEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenAPPROVEDEn.setScreenData(null);
		getGeneralDao().saveOrUpdate(screenAPPROVEDEn);
		screenAPPROVED.add(screenAPPROVEDEn);
		/*************************************************************************/
		List<ResponseScreen> screenPrepareList = new ArrayList<ResponseScreen>();

		ResponseScreen screenPrepareFa = new ResponseScreen();
		screenPrepareFa.setScreenno("019");
		screenPrepareFa.setDesc("تایید برداشت ارز-موفق-فارسی");
		screenPrepareFa.setLanguage(UserLanguage.FARSI_LANG);
		if (!atmType.equals(ATMType.NCR)) {
			screenPrepareFa.setScreenData("019[ESC]P2121[ESC]\\"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]FI[GR ifx.Real_Amt]"//Currency Amt
					+ "[SI]F0[GR c2F(atm.getCurrency().getCurNameFa())]"
					+ "[SI]GI[GR ifx.Auth_CurRate]"//curRate
					+ "[SI]HI[GR ifx.TotalFeeAmt]"//Fee
					+ "[SI]II[GR ifx.Auth_Amt]"//AuthAmt 
					);
		} else {
			screenPrepareFa.setScreenData("019[ESC]P2121[ESC]\\"
					+ "[ESC](6[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]FI[GR ifx.Real_Amt]"//Currency Amt
					+ "[SI]F0[GR c2F(atm.getCurrency().getCurNameFa())]"
					+ "[SI]GI[GR ifx.Auth_CurRate]"//curRate
					+ "[SI]HI[GR ifx.TotalFeeAmt]"//Fee
					+ "[SI]II[GR ifx.Auth_Amt]"//AuthAmt 
					);
		}
		screenPrepareList.add(screenPrepareFa);
		getGeneralDao().saveOrUpdate(screenPrepareFa);

		ResponseScreen screenPrepareEn = new ResponseScreen();
		screenPrepareEn.setScreenno("419");
		screenPrepareEn.setDesc("تایید  برداشت ارز-موفق-انگلیسی");
		screenPrepareEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenPrepareEn.setScreenData("419[ESC]P2521[ESC]\\"
				+ "[ESC](1[ESC][OC]"
				+ updateablePageFontColor
				+ "[SI]FI[GR ifx.Real_Amt]"//Currency Amt
				+ "[SI]F0[GR atm.getCurrency().getCurNameEn()]"
				+ "[SI]GI[GR ifx.Auth_CurRate]"//curRate
				+ "[SI]HI[GR ifx.TotalFeeAmt]"//Fee
				+ "[SI]II[GR ifx.Auth_Amt]"//AuthAmt 
				);
		screenPrepareList.add(screenPrepareEn);
		getGeneralDao().saveOrUpdate(screenPrepareEn);
		
		/****************************************************************************/
		//Task019
		List<ResponseScreen> screen01 = new ArrayList<ResponseScreen>();
		ResponseScreen screen01Fa = new ResponseScreen();
		screen01Fa.setScreenno("381");
		screen01Fa.setDesc("برداشت  ارز عدم چاپ رسید-موفق فارسی");
		screen01Fa.setLanguage(UserLanguage.FARSI_LANG);
		screen01Fa.setScreenData(null);
		getGeneralDao().saveOrUpdate(screen01Fa);
		screen01.add(screen01Fa);
		//Task019
		ResponseScreen screen01En = new ResponseScreen();
		screen01En.setScreenno("781"); //Change in 92.05.26 481 -> 781
		screen01En.setDesc("برداشت  ارز عدم چاپ رسید-موفق انگلیسی");
		screen01En.setLanguage(UserLanguage.ENGLISH_LANG);
		screen01En.setScreenData(null);
		getGeneralDao().saveOrUpdate(screen01En);
		screen01.add(screen01En);		
		
		List<ResponseScreen> screenTimeout = new ArrayList<ResponseScreen>();
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("برداشت ارز-timeout فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		screenTimeout.add(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("برداشت ارز-timeout انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);
		screenTimeout.add(screenTimeoutEn);
		
		/******************************* Function Command Response *********************/

		FunctionCommandResponse responseShetab = new FunctionCommandResponse();
		responseShetab.setName("تایید  برداشت ارز-شتابی-موفق");
		responseShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseShetab.setNextState("521");
		responseShetab.setScreen(screenPrepareList);
		getGeneralDao().saveOrUpdate(responseShetab);

		/*****************************/

		FunctionCommandResponse responsePasargad = new FunctionCommandResponse();
		responsePasargad.setName("تایید برداشت ارز-داخلی-موفق");
		responsePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responsePasargad.setNextState("021");
		responsePasargad.setScreen(screenPrepareList);
		getGeneralDao().saveOrUpdate(responsePasargad);

		/*****************************/
		FunctionCommandResponse atmResponse00Fa = new FunctionCommandResponse();
		atmResponse00Fa.setName("برداشت ارز -موفق");
		atmResponse00Fa.setBeRetain(false);
		atmResponse00Fa.setFunctionCommand(NDCFunctionIdentifierConstants.CARD_BEFORE_CACH);
		atmResponse00Fa.setNextState("705");
		atmResponse00Fa.setScreen(screenAPPROVED);
		atmResponse00Fa.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(atmResponse00Fa);
		
		//Task019
		FunctionCommandResponse atmResponse01Fa = new FunctionCommandResponse();
		atmResponse01Fa.setName("برداشت  ارز عدم چاپ رسید-موفق");
		atmResponse01Fa.setBeRetain(false);
		atmResponse01Fa.setFunctionCommand(NDCFunctionIdentifierConstants.CARD_BEFORE_CACH);
		atmResponse01Fa.setNextState("716");
		atmResponse01Fa.setScreen(screen01);
		atmResponse01Fa.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(atmResponse01Fa);		
		
		
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("برداشت ارز-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
		responseTimeOut.setScreen(screenTimeout);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		/*******************************************************************************/
		
		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse01Fa);//Task019
		
		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse01Fa);//Task019
		
		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse01Fa);//Task019
		
		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019
		
		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse01Fa);//Task019
		
		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019
		/*****************************/

		atmRequestPreWithdrawalShetabEn.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responseShetab);
		atmRequestTimeOutPreWithdrawalShetabEn.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responseTimeOut);
		atmRequestPreWithdrawalShetabFa.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responseShetab);
		atmRequestTimeOutPreWithdrawalShetabFa.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responseTimeOut);
		atmRequestPreWithdrawalPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responsePasargad);
		atmRequestTimeOutPreWithdrawalPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responseTimeOut);
		atmRequestPreWithdrawalPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responsePasargad);
		atmRequestTimeOutPreWithdrawalPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_WITHDRAWAL, responseTimeOut);

		/*****************************/

		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabFa);//Task019

		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabEn);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadFa);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubFa);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadEn);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubEn);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPreWithdrawalShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestPreWithdrawalShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestPreWithdrawalPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestPreWithdrawalPasargadEn);

		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreWithdrawalShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreWithdrawalShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreWithdrawalPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreWithdrawalPasargadEn);


		requests.add(atmRequestShetabFa);
		requests.add(atmRequestTimeOutShetabFa);
		requests.add(atmRequestReceiptOptionShetabFa);//Task019

		requests.add(atmRequestShetabEn);
		requests.add(atmRequestTimeOutShetabEn);
		requests.add(atmRequestReceiptOptionShetabEn);//Task019

		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestTimeOutPasargadFa);
		requests.add(atmRequestReceiptOptionPasargadFa);//Task019
		
		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadSubFa);
		requests.add(atmRequestReceiptOptionPasargadSubFa);//Task019

		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestTimeOutPasargadEn);
		requests.add(atmRequestReceiptOptionPasargadEn);//Task019

		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestTimeOutPasargadSubEn);
		requests.add(atmRequestReceiptOptionPasargadSubEn);//Task019


		requests.add(atmRequestPreWithdrawalShetabFa);
		requests.add(atmRequestPreWithdrawalShetabEn);
		requests.add(atmRequestPreWithdrawalPasargadFa);
		requests.add(atmRequestPreWithdrawalPasargadEn);

		requests.add(atmRequestTimeOutPreWithdrawalShetabFa);
		requests.add(atmRequestTimeOutPreWithdrawalShetabEn);
		requests.add(atmRequestTimeOutPreWithdrawalPasargadFa);
		requests.add(atmRequestTimeOutPreWithdrawalPasargadEn);

		return requests;
	}
	
	public List<ATMRequest> purchaseMTNCharge(ATMConfiguration configuration, OARResponse oarResponse)throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();
		
		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();
		
		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		
		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();
		
		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();
		
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		

		String customerReceiptFaText = "[GR simplePurchaseMTNChargeReceiptFa()]";
//			  headerFa
//			+ "[LF][LF][GR center(c2F('رسید خرید شارژ ایرانسل')) ]"
//			+ newLine
//			+ lineFa
//			+ receivedDateFa
////			+ newLine + newLine + newLine
//			+ newLine + newLine
//			+ formatAppPanFa
////			+ newLine + newLine 
//			+ newLine  
//			+ seqCntrFa 
////			+ newLine + newLine 
//			+ newLine  
//			+ accBalLedgerFa
//			+ newLine
//			+ "[GR center('-----------')]"
//			+ newLine
//			+ amountFa
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))]"
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))]"  
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))]"  
//			+ newLine
//			+ "[LF][ESC](1*140*[ESC](7[GR c2F('رمز')][ESC](1#YES/OK[SO]9:[ESC](7[GR c2F('نحوه استفاده')]"
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR center(GR c2F('این شارژ قابل انتقال نیست')) ]"
//			+ newLine
//			+ "[GR center(GR c2F('شماره امداد مشتریان ایرانسل 140')) ]"
//			+ newLine
//			+ footerFa;
		
		String customerReceiptEnText = 
			 headerEn
				+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('IranCell Charge Card') ]" : "[LF][LF][GR center(GR c2NCRE('IranCell Charge Card')) ]")
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ formatAppPanEn
//				+ newLine + newLine 
				+ newLine  
				+ seqCntrEn 
//				+ newLine + newLine 
				+ newLine  
				+ accBalLedgerEn
				+ newLine
				+ "[GR center('-----------')]"
				+ newLine
				+ amountEn
//				+ newLine + newLine 
				+ newLine  
				+ (!atmType.equals(ATMType.NCR) ?  "[GR justify(realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), 'Real Credit')]"
				  : "[GR justify(GR realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), GR c2NCRE('Real Credit'))]")
//				+ newLine + newLine 
				+ newLine  
				+ (!atmType.equals(ATMType.NCR) ?  "[GR justify(c2E(decode(ifx.getChargeData().getCharge().getCardPIN())), 'PIN')]" 
				  : "[GR justify(GR decode(ifx.getChargeData().getCharge().getCardPIN()), GR c2NCRE('PIN'))]")
//				+ newLine + newLine 
				+ newLine  
				+ (!atmType.equals(ATMType.NCR) ?  "[GR justify(c2E(ifx.getChargeData().getCharge().getCardSerialNo()), 'Charge Serial Number')]" 
				  : "[GR justify(GR ifx.getChargeData().getCharge().getCardSerialNo(), GR c2NCRE('Charge Serial Number'))]")
				+ newLine
				+ (!atmType.equals(ATMType.NCR) ?  "[LF][GR justify('*140*PIN#', 'How To Use')]"
				  : "[LF][GR justify(GR c2NCRE('*140*PIN#YES/OK'), GR c2NCRE('How To Use'))]")
//				+ newLine + newLine 
				+ newLine  
				+ "[GR center() ]"
				+ newLine
				+ (!atmType.equals(ATMType.NCR) ?  "[GR center('IranCell HelpDesk Phone Number 140') ]"
				  : "[GR center(GR c2NCRE('IranCell HelpDesk Phone Number 140')) ]")
				+ newLine
				+ footerEn;
		
		String textJournal00 = "[GR simplePurchaseChargeJournal()]"; 
				//"[LF]Charge:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1[GR ifx.getChargeData().getCharge().getCardSerialNo()]";

		
		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
		
		Receipt customerReceiptFa = new Receipt();
		customerReceiptFa.setText(customerReceiptFaText);
		customerReceiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(customerReceiptFa);
		receiptList.add(customerReceiptFa);
		
		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt customerReceiptEn = new Receipt();
		customerReceiptEn.setText(customerReceiptEnText);
		customerReceiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		getGeneralDao().save(customerReceiptEn);
		receiptList.add(customerReceiptEn);

		/****************************/
		atmRequestShetabFa.setOpkey("AADA    ");
		atmRequestShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabFa.setExtraInformation("9935");
		atmRequestShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabFa.setOpkey("AADA   F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabFa.setExtraInformation("9935");
		atmRequestTimeOutShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);
		
		/****************************/
		
		atmRequestShetabEn.setOpkey("IADA    ");
		atmRequestShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabEn.setExtraInformation("9935");
		atmRequestShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabEn.setOpkey("IADA   F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabEn.setExtraInformation("9935");
		atmRequestTimeOutShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);
		
		/****************************/
		
		atmRequestPasargadFa.setOpkey("ABDAA   ");
		atmRequestPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadFa.setExtraInformation("9935");
		atmRequestPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadFa.setOpkey("ABDAA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadFa.setExtraInformation("9935");
		atmRequestTimeOutPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		/****************************/
		//TODO PURCHASE_CHARGE_RQ + GET_ACCOUNT_RQ
		
		atmRequestPasargadSubFa.setOpkey("ABDAB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABDAA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubFa.setExtraInformation("9935");
		atmRequestPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);
		
		//TODO Secondary_IFX_TYPE in time-out transaction + GET_ACCOUNT_RQ ?!
		atmRequestTimeOutPasargadSubFa.setOpkey("ABDAB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABDAB  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubFa.setExtraInformation("9935");
		atmRequestTimeOutPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);
		
		/****************************/
		
		atmRequestPasargadEn.setOpkey("IBDAA   ");
		atmRequestPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadEn.setExtraInformation("9935");
		atmRequestPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadEn.setOpkey("IBDAA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadEn.setExtraInformation("9935");
		atmRequestTimeOutPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		/****************************/
		
		
		//*****me
		/****************************/
		atmRequestPasargadSubEn.setOpkey("IBDAB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBDAA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubEn.setExtraInformation("9935");
		atmRequestPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadSubEn.setOpkey("IBDAB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBDAB  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubEn.setExtraInformation("9935");
		atmRequestTimeOutPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);
		
		/****************************/
		//*****me
		
		atmRequestCreditPasargadFa.setOpkey("ACDA    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadFa.setExtraInformation("9935");
		atmRequestCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadFa.setOpkey("ACDA   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadFa.setExtraInformation("9935");
		atmRequestTimeOutCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		/****************************/
		
		atmRequestCreditPasargadEn.setOpkey("ICDA    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadEn.setExtraInformation("9935");
		atmRequestCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutCreditPasargadEn.setOpkey("ICDA   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadEn.setExtraInformation("9935");
		atmRequestTimeOutCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		/****************************/
		List<ResponseScreen> screenChargeList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenChargeFa = new ResponseScreen();
		screenChargeFa.setScreenno("388");
		screenChargeFa.setDesc("خرید شارژ-موفق-فارسی");
		screenChargeFa.setLanguage(UserLanguage.FARSI_LANG);
		screenChargeFa.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		screenChargeList.add(screenChargeFa);
		getGeneralDao().saveOrUpdate(screenChargeFa);
		
		ResponseScreen screenBalanceEn = new ResponseScreen();
		screenBalanceEn.setScreenno("788");
		screenBalanceEn.setDesc("خرید شارژ-موفق-انگلیسی");
		screenBalanceEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenBalanceEn.setScreenData("788[FF][SI]@@[ESC]P2527[ESC]\\");
		screenChargeList.add(screenBalanceEn);
		getGeneralDao().saveOrUpdate(screenBalanceEn);
		
		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenReceiptFa = new ResponseScreen();
		screenReceiptFa.setScreenno("387");
		screenReceiptFa.setDesc("خرید شارژ-خطای رسید-فارسی");
		screenReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenReceiptFa.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		screenRecieptList.add(screenReceiptFa);
		getGeneralDao().saveOrUpdate(screenReceiptFa);
		
		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("787");
		screenRecieptEn.setDesc("خرید شارژ-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData("787[FF][SI]@@[ESC]P2543[ESC]\\");
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();

		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("خريد شارژ-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("خريد شارژ-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		/****************************/
		
		FunctionCommandResponse responseChargeShetab = new FunctionCommandResponse();
		responseChargeShetab.setName("خرید شارژ-شتابی-موفق");
		responseChargeShetab.setBeRetain(false);
		responseChargeShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeShetab.setNextState("596");
//		responseChargeShetab.setNextScreen("388");
//		responseChargeShetab.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeShetab.setScreen(screenChargeList);
		responseChargeShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeShetab);
		
		FunctionCommandResponse responseChargeReceiptExceptionShetab = new FunctionCommandResponse();
		responseChargeReceiptExceptionShetab.setName("خرید شارژ-شتابی-خطای رسید");
		responseChargeReceiptExceptionShetab.setBeRetain(false);
		responseChargeReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionShetab.setNextState("598");
//		responseChargeReceiptExceptionShetab.setNextScreen("387");
//		responseChargeReceiptExceptionShetab.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionShetab.setScreen(screenRecieptList);
		responseChargeReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionShetab);
		
		/****************************/
		
		FunctionCommandResponse responseChargePasargad = new FunctionCommandResponse();
		responseChargePasargad.setName("خرید شارژ-داخلی-موفق");
		responseChargePasargad.setBeRetain(false);
		responseChargePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargePasargad.setNextState("096");
//		responseChargePasargad.setNextScreen("388");
//		responseChargePasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargePasargad.setScreen(screenChargeList);
		responseChargePasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargePasargad);
		
		FunctionCommandResponse responseChargeReceiptExceptionPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionPasargad.setName("خرید شارژ-داخلی-خطای رسید");
		responseChargeReceiptExceptionPasargad.setBeRetain(false);
		responseChargeReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionPasargad.setNextState("098");
//		responseChargeReceiptExceptionPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionPasargad);
		
		/****************************/
		
		FunctionCommandResponse responseChargeCreditPasargad = new FunctionCommandResponse();
		responseChargeCreditPasargad.setName("خرید شارژ-اعتباری داخلی-موفق");
		responseChargeCreditPasargad.setBeRetain(false);
		responseChargeCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeCreditPasargad.setNextState("196");
//		responseChargeCreditPasargad.setNextScreen("388");
//		responseChargeCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeCreditPasargad.setScreen(screenChargeList);
		responseChargeCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeCreditPasargad);
		
		FunctionCommandResponse responseChargeReceiptExceptionCreditPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionCreditPasargad.setName("خرید شارژ-اعتباری داخلی-خطای رسید");
		responseChargeReceiptExceptionCreditPasargad.setBeRetain(false);
		responseChargeReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionCreditPasargad.setNextState("198");
//		responseChargeReceiptExceptionCreditPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionCreditPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionCreditPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionCreditPasargad);
		
		/****************************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("خريد شارژ-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);

		/****************************/
		
		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);
		
		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);
		
		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);
		
		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);
		
		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);
		
		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);
		
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);

		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);
		
		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);
		
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);

		requests.add(atmRequestShetabFa);
		requests.add(atmRequestTimeOutShetabFa);
		
		requests.add(atmRequestShetabEn);
		requests.add(atmRequestTimeOutShetabEn);
		
		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestTimeOutPasargadFa);
		
		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadSubFa);
		
		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestTimeOutPasargadEn);

		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestTimeOutPasargadSubEn);
		
		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadFa);
		
		requests.add(atmRequestCreditPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		
		return requests;
	}
	
	public List<ATMRequest> purchaseMCICharge(ATMConfiguration configuration, OARResponse oarResponse)throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();
		
		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();
		
		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		
		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();
		
		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestPasargadSubEn= new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();
		
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		
		
		String customerReceiptFaText = "[GR simplePurchaseMCIChargeReceiptFa()]";
//			headerFa
//			+ "[LF][LF][GR center(c2F('رسید خرید شارژ همراه اول')) ]"
//			+ newLine
//			+ lineFa
//			+ receivedDateFa
////			+ newLine + newLine + newLine
//			+ newLine + newLine
//			+ formatAppPanFa
////			+ newLine + newLine 
//			+ newLine  
//			+ seqCntrFa 
////			+ newLine + newLine 
//			+ newLine  
//			+ accBalLedgerFa
//			+ newLine
//			+ "[GR center('-----------')]"
//			+ newLine
//			+ amountFa
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))]"
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))]"  
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))]"  
//			+ newLine
//			+ "[LF][ESC](1*140*#[ESC](7[GR c2F('رمز')][ESC](1#YES/OK[SO]8:[ESC](7[GR c2F('نحوه استفاده')]"
////			+ newLine + newLine 
//			+ newLine  
////			+ "[GR center(GR c2F('این شارژ قابل انتقال نیست')) ]"
////			+ newLine
////			+ "[GR center(GR c2F('شماره امداد مشتریان همراه اول 140')) ]"
////			+ newLine
//			+ footerFa;
		
		String customerReceiptEnText = 
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('MCI Charge Card') ]" : "[LF][LF][GR center(GR c2NCRE('MCI Charge Card')) ]")
			+ newLine
			+ lineEn
			+ receivedDateEn
//			+ newLine + newLine + newLine
			+ newLine + newLine
			+ formatAppPanEn
//			+ newLine + newLine 
			+ newLine  
			+ seqCntrEn 
//			+ newLine + newLine 
			+ newLine  
			+ accBalLedgerEn
			+ newLine
			+ "[GR center('-----------')]"
			+ newLine
			+ amountEn
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), 'Real Credit')]"
			  : "[GR justify(GR realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), GR c2NCRE('Real Credit'))]")
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(c2E(decode(ifx.getChargeData().getCharge().getCardPIN())), 'PIN')]"  
			  : "[GR justify(GR decode(ifx.getChargeData().getCharge().getCardPIN()), GR c2NCRE('PIN'))]")	
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(c2E(ifx.getChargeData().getCharge().getCardSerialNo()), 'Charge Serial Number')]"  
			  : "[GR justify(GR ifx.getChargeData().getCharge().getCardSerialNo(), GR c2NCRE('Charge Serial Number'))]")
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[LF][GR justify('*140*#PIN#YES/OK', 'How To Use')]"
			  : "[LF][GR justify(GR c2NCRE('*140*#PIN#YES/OK'), GR c2NCRE('How To Use'))]") 
//			+ newLine + newLine 
//			+ newLine  
//			+ "[GR center() ]"
//			+ newLine
//			+ "[GR center('MCI HelpDesk Phone Number 140') ]"
			+ newLine
			+ footerEn;
		
		String textJournal00 = "[GR simplePurchaseChargeJournal()]"; 
				//"[LF]Charge:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1[GR ifx.getChargeData().getCharge().getCardSerialNo()]";
		
		
		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
		
		Receipt customerReceiptFa = new Receipt();
		customerReceiptFa.setText(customerReceiptFaText);
		customerReceiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(customerReceiptFa);
		receiptList.add(customerReceiptFa);
		
		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);
		
		Receipt customerReceiptEn = new Receipt();
		customerReceiptEn.setText(customerReceiptEnText);
		customerReceiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		getGeneralDao().save(customerReceiptEn);
		receiptList.add(customerReceiptEn);
		
		/****************************/
		atmRequestShetabFa.setOpkey("AADB    ");
		atmRequestShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabFa.setExtraInformation("9912");
		atmRequestShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabFa.setOpkey("AADB   F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabFa.setExtraInformation("9912");
		atmRequestTimeOutShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);
		
		/****************************/
		
		atmRequestShetabEn.setOpkey("IADB    ");
		atmRequestShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabEn.setExtraInformation("9912");
		atmRequestShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabEn.setOpkey("IADB   F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabEn.setExtraInformation("9912");
		atmRequestTimeOutShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);
		
		/****************************/
		
		atmRequestPasargadFa.setOpkey("ABDBA   ");
		atmRequestPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadFa.setExtraInformation("9912");
		atmRequestPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadFa.setOpkey("ABDBA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadFa.setExtraInformation("9912");
		atmRequestTimeOutPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		/****************************/
		//TODO PURCHASE_CHARGE_RQ + GET_ACCOUNT_RQ
		
		atmRequestPasargadSubFa.setOpkey("ABDBB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABDBA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setExtraInformation("9912");
		atmRequestPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);
		
		//TODO Secondary_IFX_TYPE in time-out transaction + GET_ACCOUNT_RQ ?!
		atmRequestTimeOutPasargadSubFa.setOpkey("ABDBB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABDBB  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubFa.setExtraInformation("9912");
		atmRequestTimeOutPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);
		
		//*****me
		atmRequestPasargadSubEn.setOpkey("IBDBB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBDBA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setExtraInformation("9912");
		atmRequestPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);
		
		//TODO Secondary_IFX_TYPE in time-out transaction + GET_ACCOUNT_RQ ?!
		atmRequestTimeOutPasargadSubEn.setOpkey("IBDBB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBDBB  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubEn.setExtraInformation("9912");
		atmRequestTimeOutPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);
		
		//*****me
		
		/****************************/
		
		atmRequestPasargadEn.setOpkey("IBDBA   ");
		atmRequestPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadEn.setExtraInformation("9912");
		atmRequestPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadEn.setOpkey("IBDBA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadEn.setExtraInformation("9912");
		atmRequestTimeOutPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		/****************************/
		
		atmRequestCreditPasargadFa.setOpkey("ACDB    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadFa.setExtraInformation("9912");
		atmRequestCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadFa.setOpkey("ACDB   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadFa.setExtraInformation("9912");
		atmRequestTimeOutCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		/****************************/
		
		atmRequestCreditPasargadEn.setOpkey("ICDB    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadEn.setExtraInformation("9912");
		atmRequestCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadEn.setOpkey("ICDB   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadEn.setExtraInformation("9912");
		atmRequestTimeOutCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		/****************************/
		List<ResponseScreen> screenChargeList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenChargeFa = new ResponseScreen();
		screenChargeFa.setScreenno("388");
		screenChargeFa.setDesc("خرید شارژ همراه اول-موفق-فارسی");
		screenChargeFa.setLanguage(UserLanguage.FARSI_LANG);
		screenChargeFa.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		screenChargeList.add(screenChargeFa);
		getGeneralDao().saveOrUpdate(screenChargeFa);
		
		ResponseScreen screenBalanceEn = new ResponseScreen();
		screenBalanceEn.setScreenno("788");
		screenBalanceEn.setDesc("خرید شارژ همراه اول-موفق-انگلیسی");
		screenBalanceEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenBalanceEn.setScreenData("788[FF][SI]@@[ESC]P2527[ESC]\\");
		screenChargeList.add(screenBalanceEn);
		getGeneralDao().saveOrUpdate(screenBalanceEn);
		
		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenReceiptFa = new ResponseScreen();
		screenReceiptFa.setScreenno("387");
		screenReceiptFa.setDesc("خرید شارژ همراه اول-خطای رسید-فارسی");
		screenReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenReceiptFa.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		screenRecieptList.add(screenReceiptFa);
		getGeneralDao().saveOrUpdate(screenReceiptFa);
		
		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("787");
		screenRecieptEn.setDesc("خرید شارژ همراه اول-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData("787[FF][SI]@@[ESC]P2543[ESC]\\");
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);
		
		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("خريد شارژ همراه اول-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("خريد شارژ همراه اول-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);
		
		/****************************/
		
		FunctionCommandResponse responseChargeShetab = new FunctionCommandResponse();
		responseChargeShetab.setName("خرید شارژ همراه اول-شتابی-موفق");
		responseChargeShetab.setBeRetain(false);
		responseChargeShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeShetab.setNextState("596");
//		responseChargeShetab.setNextScreen("388");
//		responseChargeShetab.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeShetab.setScreen(screenChargeList);
		responseChargeShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeShetab);
		
		FunctionCommandResponse responseChargeReceiptExceptionShetab = new FunctionCommandResponse();
		responseChargeReceiptExceptionShetab.setName("خرید شارژ همراه اول-شتابی-خطای رسید");
		responseChargeReceiptExceptionShetab.setBeRetain(false);
		responseChargeReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionShetab.setNextState("598");
//		responseChargeReceiptExceptionShetab.setNextScreen("387");
//		responseChargeReceiptExceptionShetab.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionShetab.setScreen(screenRecieptList);
		responseChargeReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionShetab);
		
		/****************************/
		
		FunctionCommandResponse responseChargePasargad = new FunctionCommandResponse();
		responseChargePasargad.setName("خرید شارژ همراه اول-داخلی-موفق");
		responseChargePasargad.setBeRetain(false);
		responseChargePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargePasargad.setNextState("096");
//		responseChargePasargad.setNextScreen("388");
//		responseChargePasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargePasargad.setScreen(screenChargeList);
		responseChargePasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargePasargad);
		
		FunctionCommandResponse responseChargeReceiptExceptionPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionPasargad.setName("خرید شارژ همراه اول-داخلی-خطای رسید");
		responseChargeReceiptExceptionPasargad.setBeRetain(false);
		responseChargeReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionPasargad.setNextState("098");
//		responseChargeReceiptExceptionPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionPasargad);
		
		/****************************/
		
		FunctionCommandResponse responseChargeCreditPasargad = new FunctionCommandResponse();
		responseChargeCreditPasargad.setName("خرید شارژ همراه اول-اعتباری داخلی-موفق");
		responseChargeCreditPasargad.setBeRetain(false);
		responseChargeCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeCreditPasargad.setNextState("196");
//		responseChargeCreditPasargad.setNextScreen("388");
//		responseChargeCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeCreditPasargad.setScreen(screenChargeList);
		responseChargeCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeCreditPasargad);
		
		FunctionCommandResponse responseChargeReceiptExceptionCreditPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionCreditPasargad.setName("خرید شارژ همراه اول-اعتباری داخلی-خطای رسید");
		responseChargeReceiptExceptionCreditPasargad.setBeRetain(false);
		responseChargeReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionCreditPasargad.setNextState("198");
//		responseChargeReceiptExceptionCreditPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionCreditPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionCreditPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionCreditPasargad);
		
		/****************************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("خريد شارژ همراه اول-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		
		/****************************/
		
		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);
		
		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);
		
		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);
		
		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);
		
		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);
		
		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);
		
		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);
		
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);
		
		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);
		
		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);

		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);
		
		requests.add(atmRequestShetabFa);
		requests.add(atmRequestTimeOutShetabFa);
		
		requests.add(atmRequestShetabEn);
		requests.add(atmRequestTimeOutShetabEn);
		
		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestTimeOutPasargadFa);
		
		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadSubFa);
		
		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestTimeOutPasargadEn);
		
		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestTimeOutPasargadSubEn);

		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadFa);
		
		requests.add(atmRequestCreditPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		
		return requests;
	}
	
	public List<ATMRequest> purchaseRightelCharge(ATMConfiguration configuration, OARResponse oarResponse)throws Exception {

		List<ATMRequest> requests = new ArrayList<ATMRequest>();

		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();

		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();

		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();

		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();

		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();

		ATMRequest atmRequestPasargadSubEn= new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();

		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();

		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();


		String customerReceiptFaText = "[GR simplePurchaseRightelChargeReceiptFa()]";
//			headerFa
//			+ "[LF][LF][GR center(c2F('رسید خرید شارژ رايتل')) ]"
//			+ newLine
//			+ lineFa
//			+ receivedDateFa
////			+ newLine + newLine + newLine
//			+ newLine + newLine
//			+ formatAppPanFa
////			+ newLine + newLine
//			+ newLine
//			+ seqCntrFa
////			+ newLine + newLine
//			+ newLine
//			+ accBalLedgerFa
//			+ newLine
//			+ "[GR center('-----------')]"
//			+ newLine
//			+ amountFa
////			+ newLine + newLine
//			+ newLine
//			+ "[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))]"
////			+ newLine + newLine
//			+ newLine
//			+ "[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))]"
//			+ newLine
//			+ "[GR justify(c2E(''),c2E(decode(ifx.getChargeData().getCharge().getCardPIN())))]"
////			+ newLine + newLine
//			+ newLine
//			+ "[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))]"
//			+ newLine
//			+ "[LF][ESC](1*141*[ESC](7[GR c2F('رمز')][ESC](1#YES/OK[SO]8:[ESC](7[GR c2F('نحوه استفاده')]"
////			+ newLine + newLine
//			+ newLine
////			+ "[GR center(GR c2F('این شارژ قابل انتقال نیست')) ]"
////			+ newLine
//			+ "[GR center(GR c2F('شماره پشتیبانی رایتل 0212920')) ]"
//			+ newLine
//			+ footerFa;

		String customerReceiptEnText =
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Rightel Charge Card') ]" : "[LF][LF][GR center(GR c2NCRE('Rightel Charge Card')) ]")
			+ newLine
			+ lineEn
			+ receivedDateEn
//			+ newLine + newLine + newLine
			+ newLine + newLine
			+ formatAppPanEn
//			+ newLine + newLine
			+ newLine
			+ seqCntrEn
//			+ newLine + newLine
			+ newLine
			+ accBalLedgerEn
			+ newLine
			+ "[GR center('-----------')]"
			+ newLine
			+ amountEn
//			+ newLine + newLine
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), 'Real Credit')]" : "[GR justify(GR realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), GR c2NCRE('Real Credit'))]")
//			+ newLine + newLine
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(decode(ifx.getChargeData().getCharge().getCardPIN()), 'PIN')]" : "[GR justify(GR decode(ifx.getChargeData().getCharge().getCardPIN()), GR c2NCRE('PIN'))]")
//			+ newLine + newLine
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(ifx.getChargeData().getCharge().getCardSerialNo(), 'Charge Serial Number')]" : "[GR justify(GR ifx.getChargeData().getCharge().getCardSerialNo(), GR c2NCRE('Charge Serial Number'))]")
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[LF][GR justify('*141*PIN#YES/OK', 'How To Use')]" : "[LF][GR justify(GR c2NCRE('*141*PIN#YES/OK'), GR c2NCRE('How To Use'))]")
//			+ newLine + newLine
//			+ newLine
//			+ "[GR center() ]"
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR center('Rightel HelpDesk Number 0212920') ]" : "[GR center(GR c2NCRE('Rightel HelpDesk Number 0212920')) ]")
			+ newLine
			+ footerEn;

		String textJournal00 = "[GR simplePurchaseChargeJournal()]"; 
				//"[LF]CHARGE:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId][SO]1[GR ifx.getChargeData().getCharge().getCardSerialNo()]";


		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();

		Receipt customerReceiptFa = new Receipt();
		customerReceiptFa.setText(customerReceiptFaText);
		customerReceiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(customerReceiptFa);
		receiptList.add(customerReceiptFa);

		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt customerReceiptEn = new Receipt();
		customerReceiptEn.setText(customerReceiptEnText);
		customerReceiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		getGeneralDao().save(customerReceiptEn);
		receiptList.add(customerReceiptEn);

		/****************************/
		atmRequestShetabFa.setOpkey("AADC    ");
		atmRequestShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabFa.setExtraInformation("9920");
		atmRequestShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);

		atmRequestTimeOutShetabFa.setOpkey("AADC   F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabFa.setExtraInformation("9920");
		atmRequestTimeOutShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);

		/****************************/

		atmRequestShetabEn.setOpkey("IADC    ");
		atmRequestShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabEn.setExtraInformation("9920");
		atmRequestShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);

		atmRequestTimeOutShetabEn.setOpkey("IADC   F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabEn.setExtraInformation("9920");
		atmRequestTimeOutShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);

		/****************************/

		atmRequestPasargadFa.setOpkey("ABDCA   ");
		atmRequestPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadFa.setExtraInformation("9920");
		atmRequestPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadFa.setOpkey("ABDCA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadFa.setExtraInformation("9920");
		atmRequestTimeOutPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);

		/****************************/
		//TODO PURCHASE_CHARGE_RQ + GET_ACCOUNT_RQ

		atmRequestPasargadSubFa.setOpkey("ABDCB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABDCA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setExtraInformation("9920");
		atmRequestPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);

		//TODO Secondary_IFX_TYPE in time-out transaction + GET_ACCOUNT_RQ ?!
		atmRequestTimeOutPasargadSubFa.setOpkey("ABDCB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABDCB  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubFa.setExtraInformation("9920");
		atmRequestTimeOutPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);


		atmRequestPasargadSubEn.setOpkey("IBDCB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBDCA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setExtraInformation("9920");
		atmRequestPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);

		//TODO Secondary_IFX_TYPE in time-out transaction + GET_ACCOUNT_RQ ?!
		atmRequestTimeOutPasargadSubEn.setOpkey("IBDCB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBDCB  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubEn.setExtraInformation("9920");
		atmRequestTimeOutPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);



		/****************************/

		atmRequestPasargadEn.setOpkey("IBDCA   ");
		atmRequestPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadEn.setExtraInformation("9920");
		atmRequestPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadEn.setOpkey("IBDCA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadEn.setExtraInformation("9920");
		atmRequestTimeOutPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);

		/****************************/

		atmRequestCreditPasargadFa.setOpkey("ACDC    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadFa.setExtraInformation("9920");
		atmRequestCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutCreditPasargadFa.setOpkey("ACDC   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadFa.setExtraInformation("9920");
		atmRequestTimeOutCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);

		/****************************/

		atmRequestCreditPasargadEn.setOpkey("ICDC    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadEn.setExtraInformation("9920");
		atmRequestCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutCreditPasargadEn.setOpkey("ICDC   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadEn.setExtraInformation("9920");
		atmRequestTimeOutCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);

		/****************************/
		List<ResponseScreen> screenChargeList = new ArrayList<ResponseScreen>();

		ResponseScreen screenChargeFa = new ResponseScreen();
		screenChargeFa.setScreenno("388");
		screenChargeFa.setDesc("خرید شارژ رايتل-موفق-فارسی");
		screenChargeFa.setLanguage(UserLanguage.FARSI_LANG);
		screenChargeFa.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		screenChargeList.add(screenChargeFa);
		getGeneralDao().saveOrUpdate(screenChargeFa);

		ResponseScreen screenBalanceEn = new ResponseScreen();
		screenBalanceEn.setScreenno("788");
		screenBalanceEn.setDesc("خرید شارژ رايتل-موفق-انگلیسی");
		screenBalanceEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenBalanceEn.setScreenData("788[FF][SI]@@[ESC]P2527[ESC]\\");
		screenChargeList.add(screenBalanceEn);
		getGeneralDao().saveOrUpdate(screenBalanceEn);

		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();

		ResponseScreen screenReceiptFa = new ResponseScreen();
		screenReceiptFa.setScreenno("387");
		screenReceiptFa.setDesc("خرید شارژ رايتل-خطای رسید-فارسی");
		screenReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenReceiptFa.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		screenRecieptList.add(screenReceiptFa);
		getGeneralDao().saveOrUpdate(screenReceiptFa);

		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("787");
		screenRecieptEn.setDesc("خرید شارژ رايتل-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData("787[FF][SI]@@[ESC]P2543[ESC]\\");
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();

		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("خريد شارژ رايتل-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);

		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("خريد شارژ رايتل-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		/****************************/

		FunctionCommandResponse responseChargeShetab = new FunctionCommandResponse();
		responseChargeShetab.setName("خرید شارژ رايتل-شتابی-موفق");
		responseChargeShetab.setBeRetain(false);
		responseChargeShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeShetab.setNextState("596");
//		responseChargeShetab.setNextScreen("388");
//		responseChargeShetab.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeShetab.setScreen(screenChargeList);
		responseChargeShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeShetab);

		FunctionCommandResponse responseChargeReceiptExceptionShetab = new FunctionCommandResponse();
		responseChargeReceiptExceptionShetab.setName("خرید شارژ رايتل-شتابی-خطای رسید");
		responseChargeReceiptExceptionShetab.setBeRetain(false);
		responseChargeReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionShetab.setNextState("598");
//		responseChargeReceiptExceptionShetab.setNextScreen("387");
//		responseChargeReceiptExceptionShetab.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionShetab.setScreen(screenRecieptList);
		responseChargeReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionShetab);

		/****************************/

		FunctionCommandResponse responseChargePasargad = new FunctionCommandResponse();
		responseChargePasargad.setName("خرید شارژ رايتل-داخلی-موفق");
		responseChargePasargad.setBeRetain(false);
		responseChargePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargePasargad.setNextState("096");
//		responseChargePasargad.setNextScreen("388");
//		responseChargePasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargePasargad.setScreen(screenChargeList);
		responseChargePasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargePasargad);

		FunctionCommandResponse responseChargeReceiptExceptionPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionPasargad.setName("خرید شارژ رايتل-داخلی-خطای رسید");
		responseChargeReceiptExceptionPasargad.setBeRetain(false);
		responseChargeReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionPasargad.setNextState("098");
//		responseChargeReceiptExceptionPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionPasargad);

		/****************************/

		FunctionCommandResponse responseChargeCreditPasargad = new FunctionCommandResponse();
		responseChargeCreditPasargad.setName("خرید شارژ رايتل-اعتباری داخلی-موفق");
		responseChargeCreditPasargad.setBeRetain(false);
		responseChargeCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeCreditPasargad.setNextState("196");
//		responseChargeCreditPasargad.setNextScreen("388");
//		responseChargeCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeCreditPasargad.setScreen(screenChargeList);
		responseChargeCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeCreditPasargad);

		FunctionCommandResponse responseChargeReceiptExceptionCreditPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionCreditPasargad.setName("خرید شارژ رايتل-اعتباری داخلی-خطای رسید");
		responseChargeReceiptExceptionCreditPasargad.setBeRetain(false);
		responseChargeReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionCreditPasargad.setNextState("198");
//		responseChargeReceiptExceptionCreditPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionCreditPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionCreditPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionCreditPasargad);

		/****************************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("خريد شارژ رايتل-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);

		/****************************/

		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);

		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);

		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);

		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);

		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);

		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);

		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);

		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);

		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);

		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);

		requests.add(atmRequestShetabFa);
		requests.add(atmRequestTimeOutShetabFa);

		requests.add(atmRequestShetabEn);
		requests.add(atmRequestTimeOutShetabEn);

		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestTimeOutPasargadFa);

		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadSubFa);

		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestTimeOutPasargadEn);

		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestTimeOutPasargadSubEn);

		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadFa);

		requests.add(atmRequestCreditPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadEn);

		return requests;
	}

	public List<ATMRequest> purchaseTaliaCharge(ATMConfiguration configuration, OARResponse oarResponse)throws Exception {

		List<ATMRequest> requests = new ArrayList<ATMRequest>();

		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();

		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();

		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();

		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();

		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();

		ATMRequest atmRequestPasargadSubEn= new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();

		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();

		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();

		String customerReceiptFaText = "[GR simplePurchaseTaliaChargeReceiptFa()]";

		String customerReceiptEnText =
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Talia Charge Card') ]" : "[LF][LF][GR center(GR c2NCRE('Talia Charge Card')) ]")
			+ newLine
			+ lineEn
			+ receivedDateEn
			+ newLine + newLine
			+ formatAppPanEn
			+ newLine
			+ seqCntrEn
			+ newLine
			+ accBalLedgerEn
			+ newLine
			+ "[GR center('-----------')]"
			+ newLine
			+ amountEn
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), 'Real Credit')]" : "[GR justify(GR realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), GR c2NCRE('Real Credit'))]")
//			+ newLine + newLine
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(decode(ifx.getChargeData().getCharge().getCardPIN()), 'PIN')]" : "[GR justify(GR decode(ifx.getChargeData().getCharge().getCardPIN()), GR c2NCRE('PIN'))]")
//			+ newLine + newLine
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(ifx.getChargeData().getCharge().getCardSerialNo(), 'Charge Serial Number')]" : "[GR justify(GR ifx.getChargeData().getCharge().getCardSerialNo(), GR c2NCRE('Charge Serial Number'))]")
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[LF][GR justify('*140*PIN#YES/OK', 'How To Use')]" : "[LF][GR justify(GR c2NCRE('*140*PIN#YES/OK'), GR c2NCRE('How To Use'))]")
//			+ newLine + newLine
//			+ newLine
//			+ "[GR center() ]"
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR center('Talia HelpDesk Number 09329990000') ]" : "[GR center(GR c2NCRE('Talia HelpDesk Number 09329990000')) ]")
			+ newLine
			+ footerEn;

		String textJournal00 ="[GR simplePurchaseChargeJournal()]"; 
				//"[LF]Charge:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId][SO]1[GR ifx.getChargeData().getCharge().getCardSerialNo()]";


		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();

		Receipt customerReceiptFa = new Receipt();
		customerReceiptFa.setText(customerReceiptFaText);
		customerReceiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(customerReceiptFa);
		receiptList.add(customerReceiptFa);

		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt customerReceiptEn = new Receipt();
		customerReceiptEn.setText(customerReceiptEnText);
		customerReceiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		customerReceiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		getGeneralDao().save(customerReceiptEn);
		receiptList.add(customerReceiptEn);

		/****************************/
		atmRequestShetabFa.setOpkey("AADD    ");
		atmRequestShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabFa.setExtraInformation("9932");
		atmRequestShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);

		atmRequestTimeOutShetabFa.setOpkey("AADD   F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabFa.setExtraInformation("9932");
		atmRequestTimeOutShetabFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);

		/****************************/

		atmRequestShetabEn.setOpkey("IADD    ");
		atmRequestShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestShetabEn.setExtraInformation("9932");
		atmRequestShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);

		atmRequestTimeOutShetabEn.setOpkey("IADD   F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutShetabEn.setExtraInformation("9932");
		atmRequestTimeOutShetabEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);

		/****************************/

		atmRequestPasargadFa.setOpkey("ABDDA   ");
		atmRequestPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadFa.setExtraInformation("9932");
		atmRequestPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadFa.setOpkey("ABDDA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadFa.setExtraInformation("9932");
		atmRequestTimeOutPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);

		/****************************/
		//TODO PURCHASE_CHARGE_RQ + GET_ACCOUNT_RQ

		atmRequestPasargadSubFa.setOpkey("ABDDB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABDDA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setExtraInformation("9932");
		atmRequestPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);

		//TODO Secondary_IFX_TYPE in time-out transaction + GET_ACCOUNT_RQ ?!
		atmRequestTimeOutPasargadSubFa.setOpkey("ABDDB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABDDB  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubFa.setExtraInformation("9932");
		atmRequestTimeOutPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);


		atmRequestPasargadSubEn.setOpkey("IBDDB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBDDA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setExtraInformation("9932");
		atmRequestPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);

		//TODO Secondary_IFX_TYPE in time-out transaction + GET_ACCOUNT_RQ ?!
		atmRequestTimeOutPasargadSubEn.setOpkey("IBDDB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBDDB  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubEn.setExtraInformation("9932");
		atmRequestTimeOutPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);



		/****************************/

		atmRequestPasargadEn.setOpkey("IBDDA   ");
		atmRequestPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestPasargadEn.setExtraInformation("9932");
		atmRequestPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadEn.setOpkey("IBDDA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutPasargadEn.setExtraInformation("9932");
		atmRequestTimeOutPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);

		/****************************/

		atmRequestCreditPasargadFa.setOpkey("ACDD    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadFa.setExtraInformation("9932");
		atmRequestCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutCreditPasargadFa.setOpkey("ACDD   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadFa.setExtraInformation("9932");
		atmRequestTimeOutCreditPasargadFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);

		/****************************/

		atmRequestCreditPasargadEn.setOpkey("ICDD    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestCreditPasargadEn.setExtraInformation("9932");
		atmRequestCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutCreditPasargadEn.setOpkey("ICDD   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.PURCHASECHARGE);
		atmRequestTimeOutCreditPasargadEn.setExtraInformation("9932");
		atmRequestTimeOutCreditPasargadEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);

		/****************************/
		List<ResponseScreen> screenChargeList = new ArrayList<ResponseScreen>();

		ResponseScreen screenChargeFa = new ResponseScreen();
		screenChargeFa.setScreenno("388");
		screenChargeFa.setDesc("خرید شارژ تاليا-موفق-فارسی");
		screenChargeFa.setLanguage(UserLanguage.FARSI_LANG);
		screenChargeFa.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		screenChargeList.add(screenChargeFa);
		getGeneralDao().saveOrUpdate(screenChargeFa);

		ResponseScreen screenBalanceEn = new ResponseScreen();
		screenBalanceEn.setScreenno("788");
		screenBalanceEn.setDesc("خرید شارژ تاليا-موفق-انگلیسی");
		screenBalanceEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenBalanceEn.setScreenData("788[FF][SI]@@[ESC]P2527[ESC]\\");
		screenChargeList.add(screenBalanceEn);
		getGeneralDao().saveOrUpdate(screenBalanceEn);

		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();

		ResponseScreen screenReceiptFa = new ResponseScreen();
		screenReceiptFa.setScreenno("387");
		screenReceiptFa.setDesc("خرید شارژ تاليا-خطای رسید-فارسی");
		screenReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenReceiptFa.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		screenRecieptList.add(screenReceiptFa);
		getGeneralDao().saveOrUpdate(screenReceiptFa);

		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("787");
		screenRecieptEn.setDesc("خرید شارژ تاليا-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData("787[FF][SI]@@[ESC]P2543[ESC]\\");
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();

		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("خريد شارژ تاليا-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);

		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("خريد شارژ تاليا-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		/****************************/

		FunctionCommandResponse responseChargeShetab = new FunctionCommandResponse();
		responseChargeShetab.setName("خرید شارژ تاليا-شتابی-موفق");
		responseChargeShetab.setBeRetain(false);
		responseChargeShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeShetab.setNextState("596");
//		responseChargeShetab.setNextScreen("388");
//		responseChargeShetab.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeShetab.setScreen(screenChargeList);
		responseChargeShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeShetab);

		FunctionCommandResponse responseChargeReceiptExceptionShetab = new FunctionCommandResponse();
		responseChargeReceiptExceptionShetab.setName("خرید شارژ تاليا-شتابی-خطای رسید");
		responseChargeReceiptExceptionShetab.setBeRetain(false);
		responseChargeReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionShetab.setNextState("598");
//		responseChargeReceiptExceptionShetab.setNextScreen("387");
//		responseChargeReceiptExceptionShetab.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionShetab.setScreen(screenRecieptList);
		responseChargeReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionShetab);

		/****************************/

		FunctionCommandResponse responseChargePasargad = new FunctionCommandResponse();
		responseChargePasargad.setName("خرید شارژ تاليا-داخلی-موفق");
		responseChargePasargad.setBeRetain(false);
		responseChargePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargePasargad.setNextState("096");
//		responseChargePasargad.setNextScreen("388");
//		responseChargePasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargePasargad.setScreen(screenChargeList);
		responseChargePasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargePasargad);

		FunctionCommandResponse responseChargeReceiptExceptionPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionPasargad.setName("خرید شارژ تاليا-داخلی-خطای رسید");
		responseChargeReceiptExceptionPasargad.setBeRetain(false);
		responseChargeReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionPasargad.setNextState("098");
//		responseChargeReceiptExceptionPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionPasargad);

		/****************************/

		FunctionCommandResponse responseChargeCreditPasargad = new FunctionCommandResponse();
		responseChargeCreditPasargad.setName("خرید شارژ تاليا-اعتباری داخلی-موفق");
		responseChargeCreditPasargad.setBeRetain(false);
		responseChargeCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeCreditPasargad.setNextState("196");
//		responseChargeCreditPasargad.setNextScreen("388");
//		responseChargeCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
		responseChargeCreditPasargad.setScreen(screenChargeList);
		responseChargeCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseChargeCreditPasargad);

		FunctionCommandResponse responseChargeReceiptExceptionCreditPasargad = new FunctionCommandResponse();
		responseChargeReceiptExceptionCreditPasargad.setName("خرید شارژ تاليا-اعتباری داخلی-خطای رسید");
		responseChargeReceiptExceptionCreditPasargad.setBeRetain(false);
		responseChargeReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseChargeReceiptExceptionCreditPasargad.setNextState("198");
//		responseChargeReceiptExceptionCreditPasargad.setNextScreen("387");
//		responseChargeReceiptExceptionCreditPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
		responseChargeReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
		responseChargeReceiptExceptionCreditPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionCreditPasargad);

		/****************************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("خريد شارژ تاليا-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);

		/****************************/

		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);

		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeShetab);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionShetab);

		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargePasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionPasargad);

		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);

		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseChargeCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseChargeReceiptExceptionCreditPasargad);

		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);

		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);

		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);

		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);

		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);

		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);

		requests.add(atmRequestShetabFa);
		requests.add(atmRequestTimeOutShetabFa);

		requests.add(atmRequestShetabEn);
		requests.add(atmRequestTimeOutShetabEn);

		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestTimeOutPasargadFa);

		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadSubFa);

		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestTimeOutPasargadEn);

		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestTimeOutPasargadSubEn);

		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadFa);

		requests.add(atmRequestCreditPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadEn);

		return requests;
	}	
	
	//TASK Task019 : Receipt Option
	private List<ATMRequest> Balance(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabFa = new ATMRequest(); //Task019
		
		
		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabEn = new ATMRequest(); //Task019
		
		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadFa = new ATMRequest(); //Task019
		
		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubFa = new ATMRequest(); //Task019
		
		ATMRequest atmRequestPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubEn = new ATMRequest(); //Task019
		
		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadEn = new ATMRequest(); //Task019
		
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadFa = new ATMRequest(); //Task019
		
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadEn = new ATMRequest(); //Task019
		
		

		String textFa =(!atmType.equals(ATMType.NCR) ? "[ESC](7[GR simpleBalanceReceiptFa()]" : "[GR simpleBalanceReceiptFa()]");
			
		String textEn = 
			headerEn
			+ newLine  
			+ newLine  
			+(!atmType.equals(ATMType.NCR) ? "[GR center('Balance Receipt') ]" : "[GR center(GR c2NCRE('Balance Receipt')) ]")
			+ newLine
			+ lineEn
			+ receivedDateEn 
			+ newLine + newLine
			+ formatAppPanEn 
			+ newLine  
			+ seqCntrEn
			+ newLine  
			+ accBalLedgerEn
			+ newLine  
			+ accBalAvailableEn
			+ newLine  
			+ subAccEn
			+ "[GR putLF(8)]"
			+ footerEn
			;
		
		String textJournal00 = "[GR simpleBalanceJournal()]";

		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
		
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList.add(receiptFa);
		
		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList.add(receiptEn);
		
		/********************************/

		atmRequestShetabFa.setOpkey("AACA    ");
		atmRequestShetabFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestShetabFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabFa.setOpkey("AACA   F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);

		//Task019
		atmRequestReceiptOptionShetabFa.setOpkey("AACA   B");
		atmRequestReceiptOptionShetabFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionShetabFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionShetabFa.setCurrency(currency);
		atmRequestReceiptOptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionShetabFa.setFit(FITType.SHETAB);
		atmRequestReceiptOptionShetabFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		

		atmRequestShetabEn.setOpkey("IACA    ");
		atmRequestShetabEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestShetabEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabEn.setOpkey("IACA   F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);
		
		//Task019
		atmRequestReceiptOptionShetabEn.setOpkey("IACA   B");
		atmRequestReceiptOptionShetabEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionShetabEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionShetabEn.setCurrency(currency);
		atmRequestReceiptOptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionShetabEn.setFit(FITType.SHETAB);
		atmRequestReceiptOptionShetabEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/********************************/
		
		atmRequestPasargadFa.setOpkey("ABCAA   ");
		atmRequestPasargadFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadFa.setOpkey("ABCAA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadFa.setOpkey("ABCAA  B");
		atmRequestReceiptOptionPasargadFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionPasargadFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadFa.setFit(FITType.PASARGAD);		
		atmRequestReceiptOptionPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/********************************/
		
		atmRequestPasargadSubFa.setOpkey("ABCAB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABCAA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);//Task019
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadSubFa.setOpkey("ABCAB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABCAB  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.BALANCEINQUIRY);
		//atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);//Add By Ad in 92.07.27  //AldComment ??? Bughaye Peyda Shode
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadSubFa.setOpkey("ABCAB  B");
		atmRequestReceiptOptionPasargadSubFa.setNextOpkey("ABCAA  B");
		atmRequestReceiptOptionPasargadSubFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT); //Task019
		atmRequestReceiptOptionPasargadSubFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadSubFa.setFit(FITType.PASARGAD);		
		atmRequestReceiptOptionPasargadSubFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);		
		/********************************/
		
		atmRequestPasargadEn.setOpkey("IBCAA   ");
		atmRequestPasargadEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadEn.setOpkey("IBCAA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadEn.setOpkey("IBCAA  B");
		atmRequestReceiptOptionPasargadEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionPasargadEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadEn.setFit(FITType.PASARGAD);		
		atmRequestReceiptOptionPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);		
		/********************************/
		
		atmRequestPasargadSubEn.setOpkey("IBCAB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBCAA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);//Task019
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadSubEn.setOpkey("IBCAB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBCAB  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadSubEn.setOpkey("IBCAB  B");
		atmRequestReceiptOptionPasargadSubEn.setNextOpkey("IBCAA  B");
		atmRequestReceiptOptionPasargadSubEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);//Task019
		atmRequestReceiptOptionPasargadSubEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadSubEn.setFit(FITType.PASARGAD);		
		atmRequestReceiptOptionPasargadSubEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);			
		
		/********************************/
		
		atmRequestCreditPasargadFa.setOpkey("ACCA    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadFa.setOpkey("ACCA   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019
		atmRequestReceiptOptionCreditPasargadFa.setOpkey("ACCA   B");
		atmRequestReceiptOptionCreditPasargadFa.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionCreditPasargadFa.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionCreditPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		atmRequestReceiptOptionCreditPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/********************************/
		
		atmRequestCreditPasargadEn.setOpkey("ICCA    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadEn.setOpkey("ICCA   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019
		atmRequestReceiptOptionCreditPasargadEn.setOpkey("ICCA   B");
		atmRequestReceiptOptionCreditPasargadEn.setIfxType(IfxType.BAL_INQ_RQ);
		atmRequestReceiptOptionCreditPasargadEn.setTrnType(TrnType.BALANCEINQUIRY);
		atmRequestReceiptOptionCreditPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		atmRequestReceiptOptionCreditPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);		

		/********************************/
		List<ResponseScreen> screenBalanceList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenBalanceFa = new ResponseScreen();
		screenBalanceFa.setScreenno("388");
		screenBalanceFa.setDesc("اعلام موجودی-موفق-فارسی");
		screenBalanceFa.setLanguage(UserLanguage.FARSI_LANG);
		screenBalanceFa.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		screenBalanceList.add(screenBalanceFa);
		getGeneralDao().saveOrUpdate(screenBalanceFa);
		
		ResponseScreen screenBalanceEn = new ResponseScreen();
		screenBalanceEn.setScreenno("788");
		screenBalanceEn.setDesc("اعلام موجودی-موفق-انگلیسی");
		screenBalanceEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenBalanceEn.setScreenData("788[FF][SI]@@[ESC]P2512[ESC]\\");
		screenBalanceList.add(screenBalanceEn);
		getGeneralDao().saveOrUpdate(screenBalanceEn);
		
		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenReceiptFa = new ResponseScreen();
		screenReceiptFa.setScreenno("388"); // 387 -> 388
		screenReceiptFa.setDesc("اعلام موجودی-خطای رسید-فارسی");
		screenReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
		if (!atmType.equals(ATMType.NCR)) {
			screenReceiptFa.setScreenData("388[FF][SI]@@[ESC]P2144[ESC]\\"    //387 -> 388
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]GI[GR amount2Fscr(ifx.AcctBalAvailableAmt, 15)]"
					+ "[SI]II[GR amount2Fscr(ifx.AcctBalLedgerAmt, 15)]");
		} else {
			screenReceiptFa.setScreenData("388[FF][SI]@@[ESC]P2144[ESC]\\"    //387 -> 388
					+ "[ESC](6[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]GI[GR amount2Fscr(ifx.AcctBalAvailableAmt, 15)]"
					+ "[SI]II[GR amount2Fscr(ifx.AcctBalLedgerAmt, 15)]");
		}
			
		screenRecieptList.add(screenReceiptFa);
		getGeneralDao().saveOrUpdate(screenReceiptFa);
		
		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("788"); //787 -> 788
		screenRecieptEn.setDesc("اعلام موجودی-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData("788[FF][SI]@@[ESC]P2544[ESC]\\"  //787 -> 788
				+ "[ESC](1[ESC][OC]80;m" 
				+ "[SI]GJ[GR amount2Escr(ifx.AcctBalAvailableAmt, 15)]"
				+ "[SI]IJ[GR amount2Escr(ifx.AcctBalLedgerAmt, 15)]");
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("اعلام موجودی-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("اعلام موجودی-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);
		/********************************/
		
		FunctionCommandResponse responseBalanceShetab = new FunctionCommandResponse();
		responseBalanceShetab.setName("اعلام موجودی-شتابی-موفق");
		responseBalanceShetab.setBeRetain(false);
		responseBalanceShetab.setDispense(null);
		responseBalanceShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseBalanceShetab.setNextState("596");
//		responseBalanceShetab.setNextScreen("388");
//		responseBalanceShetab.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		responseBalanceShetab.setScreen(screenBalanceList);
		responseBalanceShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseBalanceShetab);
		
		FunctionCommandResponse responseReceiptOptionShetab = new FunctionCommandResponse();
		responseReceiptOptionShetab.setName("اعلام موجودی-شتابی-عدم چاپ رسید");
		responseReceiptOptionShetab.setBeRetain(false);
		responseReceiptOptionShetab.setDispense(null);
		responseReceiptOptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptOptionShetab.setNextState("596");  //change 598 -> 596
		responseReceiptOptionShetab.setScreen(screenRecieptList);
		responseReceiptOptionShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseReceiptOptionShetab);
		
		/********************************/
		
		FunctionCommandResponse responseBalancePasargad = new FunctionCommandResponse();
		responseBalancePasargad.setName("اعلام موجودی-داخلی-موفق");
		responseBalancePasargad.setBeRetain(false);
		responseBalancePasargad.setDispense(null);
		responseBalancePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseBalancePasargad.setNextState("096");
		responseBalancePasargad.setScreen(screenBalanceList);
		responseBalancePasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseBalancePasargad);

		FunctionCommandResponse responseReceiptOptionPasargad = new FunctionCommandResponse();
		responseReceiptOptionPasargad.setName("اعلام موجودی-داخلی-عدم چاپ رسید");
		responseReceiptOptionPasargad.setBeRetain(false);
		responseReceiptOptionPasargad.setDispense(null);
		responseReceiptOptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptOptionPasargad.setNextState("096"); //Change 098 -> 096
		responseReceiptOptionPasargad.setScreen(screenRecieptList);
		responseReceiptOptionPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseReceiptOptionPasargad);
		
		/********************************/
		FunctionCommandResponse responseBalanceCreditPasargad = new FunctionCommandResponse();
		responseBalanceCreditPasargad.setName("اعلام موجودی-اعتباری داخلی-موفق");
		responseBalanceCreditPasargad.setBeRetain(false);
		responseBalanceCreditPasargad.setDispense(null);
		responseBalanceCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseBalanceCreditPasargad.setNextState("196");
		responseBalanceCreditPasargad.setScreen(screenBalanceList);
		responseBalanceCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseBalanceCreditPasargad);
		
		FunctionCommandResponse responseReceiptOptionCreditPasargad = new FunctionCommandResponse();  
		responseReceiptOptionCreditPasargad.setName("اعلام موجودی-اعتباری داخلی-عدم چاپ رسید");
		responseReceiptOptionCreditPasargad.setBeRetain(false);
		responseReceiptOptionCreditPasargad.setDispense(null);
		responseReceiptOptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptOptionCreditPasargad.setNextState("196"); //Change 198 -> 196
		responseReceiptOptionCreditPasargad.setScreen(screenRecieptList);
		responseReceiptOptionCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseReceiptOptionCreditPasargad);
		
		/********************************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("اعلام موجودی-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		/********************************/
		
		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceShetab);
		atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionShetab);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseReceiptOptionShetab);//Task019
		
		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceShetab);
		atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionShetab);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseReceiptOptionShetab);//Task019
		
		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalancePasargad);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionPasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseReceiptOptionPasargad);//Task019
		
		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionPasargad);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019
		
		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalancePasargad);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionPasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseReceiptOptionPasargad);//Task019

		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionPasargad);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019
		
		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceCreditPasargad);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseReceiptOptionCreditPasargad);//Task019
		
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceCreditPasargad);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptOptionCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseReceiptOptionCreditPasargad);//Task019
		
		/********************************/
		
		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);
		
		//Task019
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadEn);		

		requests.add(atmRequestShetabFa);
		requests.add(atmRequestShetabEn);
		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestCreditPasargadEn);
		
		requests.add(atmRequestTimeOutShetabFa);
		requests.add(atmRequestTimeOutShetabEn);
		requests.add(atmRequestTimeOutPasargadFa);
		requests.add(atmRequestTimeOutPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadEn);
		requests.add(atmRequestTimeOutPasargadSubEn);
		requests.add(atmRequestTimeOutCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		
		//Task019
		requests.add(atmRequestReceiptOptionShetabFa);        
		requests.add(atmRequestReceiptOptionShetabEn);        
		requests.add(atmRequestReceiptOptionPasargadFa);      
		requests.add(atmRequestReceiptOptionPasargadSubFa);   
		requests.add(atmRequestReceiptOptionPasargadEn);      
		requests.add(atmRequestReceiptOptionPasargadSubEn);   
		requests.add(atmRequestReceiptOptionCreditPasargadFa);		
		requests.add(atmRequestReceiptOptionCreditPasargadEn);
		             
		return requests;
	}
		
	private List<ATMRequest> BankStatement(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		ATMRequest atmRequestFa = new ATMRequest();
		ATMRequest atmRequestSubFa = new ATMRequest();
		ATMRequest atmRequestEn = new ATMRequest();
		ATMRequest atmRequestSubEn=new ATMRequest();//me
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeoutFa = new ATMRequest();
		ATMRequest atmRequestSubTimeoutFa = new ATMRequest();
		ATMRequest atmRequestTimeoutEn = new ATMRequest();
		ATMRequest atmRequestSubTimeoutEn=new ATMRequest();//me
		ATMRequest atmRequestCreditPasargadTimeoutFa = new ATMRequest();
		ATMRequest atmRequestCreditPasargadTimeoutEn = new ATMRequest();
		
		String textFa = "[GR simpleBankStatementReceiptFa()]";

		String textEn =
			headerEn
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR center('Statement Receipt')]"  : "[GR center(GR c2NCRE('Statement Receipt'))]")
			+ newLine 
			+ lineEn
			+ receivedDateEn 
			+ newLine + newLine
			+ formatAppPanEn 
			+ newLine
			+ subAccEn			
			+ "[LF][GR bankStatementTableEn()]"
			+ newLine 
			+ accBalLedgerEn
			+ "[GR putLF(2)]"
			+ footerEn
			;

		String textJournal00 = "[LF]Statement:[SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";
		
		ArrayList<Receipt> receiptList00 = new ArrayList<Receipt>();
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList00.add(receiptFa);
		
		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList00.add(receiptEn);

		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList00.add(journal);

		atmRequestFa.setOpkey("ABFAA   ");
		atmRequestFa.setIfxType(IfxType.BANK_STATEMENT_RQ);
		atmRequestFa.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestFa.setCurrency(currency);
		atmRequestFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestFa.setFit(FITType.PASARGAD);
		
		atmRequestSubFa.setOpkey("ABFAB   ");
		atmRequestSubFa.setNextOpkey("ABFAA   ");
		atmRequestSubFa.setIfxType(IfxType.BANK_STATEMENT_RQ);
		atmRequestSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestSubFa.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestSubFa.setCurrency(currency);
		atmRequestSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestSubFa.setFit(FITType.PASARGAD);
		
		atmRequestEn.setOpkey("IBFAA   ");
		atmRequestEn.setIfxType(IfxType.BANK_STATEMENT_RQ);
		atmRequestEn.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestEn.setCurrency(currency);
		atmRequestEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestEn.setFit(FITType.PASARGAD);
		//*****me
		atmRequestSubEn.setOpkey("IBFAB   ");
		atmRequestSubEn.setNextOpkey("IBFAA   ");
		atmRequestSubEn.setIfxType(IfxType.BANK_STATEMENT_RQ);
		atmRequestSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestSubEn.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestSubEn.setCurrency(currency);
		atmRequestSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestSubEn.setFit(FITType.PASARGAD);
		//*****me
		atmRequestCreditPasargadFa.setOpkey("ACFA    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.BANK_STATEMENT_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestCreditPasargadEn.setOpkey("ICFA    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.BANK_STATEMENT_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);


		atmRequestTimeoutFa.setOpkey("ABFAA  F");
		atmRequestTimeoutFa.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		atmRequestTimeoutFa.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestTimeoutFa.setCurrency(currency);
		atmRequestTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeoutFa.setFit(FITType.PASARGAD);
		
		atmRequestSubTimeoutFa.setOpkey("ABFAB  F");
		atmRequestSubTimeoutFa.setNextOpkey("ABFAA   ");
		atmRequestSubTimeoutFa.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		atmRequestSubTimeoutFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestSubTimeoutFa.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestSubTimeoutFa.setCurrency(currency);
		atmRequestSubTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestSubTimeoutFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeoutEn.setOpkey("IBFAA  F");
		atmRequestTimeoutEn.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		atmRequestTimeoutEn.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestTimeoutEn.setCurrency(currency);
		atmRequestTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeoutEn.setFit(FITType.PASARGAD);
		
		//*****me
		atmRequestSubTimeoutEn.setOpkey("IBFAB  F");
		atmRequestSubTimeoutEn.setNextOpkey("IBFAA   ");
		atmRequestSubTimeoutEn.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		atmRequestSubTimeoutEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestSubTimeoutEn.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestSubTimeoutEn.setCurrency(currency);
		atmRequestSubTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestSubTimeoutEn.setFit(FITType.PASARGAD);	
		//*****me
		
		atmRequestCreditPasargadTimeoutFa.setOpkey("ACFA   F");
		atmRequestCreditPasargadTimeoutFa.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		atmRequestCreditPasargadTimeoutFa.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestCreditPasargadTimeoutFa.setCurrency(currency);
		atmRequestCreditPasargadTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadTimeoutFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestCreditPasargadTimeoutEn.setOpkey("ICFA   F");
		atmRequestCreditPasargadTimeoutEn.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		atmRequestCreditPasargadTimeoutEn.setTrnType(TrnType.BANKSTATEMENT);
		atmRequestCreditPasargadTimeoutEn.setCurrency(currency);
		atmRequestCreditPasargadTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadTimeoutEn.setFit(FITType.CREDIT_PASARGAD);

		getGeneralDao().save(atmRequestFa);
		getGeneralDao().save(atmRequestSubFa);
		getGeneralDao().save(atmRequestEn);
		getGeneralDao().save(atmRequestSubEn);//me
		getGeneralDao().save(atmRequestCreditPasargadFa);
		getGeneralDao().save(atmRequestCreditPasargadEn);
		getGeneralDao().save(atmRequestTimeoutFa);
		getGeneralDao().save(atmRequestSubTimeoutFa);
		getGeneralDao().save(atmRequestTimeoutEn);
		getGeneralDao().save(atmRequestSubTimeoutEn);//me
		getGeneralDao().save(atmRequestCreditPasargadTimeoutFa);
		getGeneralDao().save(atmRequestCreditPasargadTimeoutEn);
		
		/*************************/
		List<ResponseScreen> screenStatementList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenStatementFa = new ResponseScreen();
		screenStatementFa.setScreenno("388");
		screenStatementFa.setDesc("صورتحساب-داخلی-موفق-فارسی");
		screenStatementFa.setLanguage(UserLanguage.FARSI_LANG);
		screenStatementFa.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		screenStatementList.add(screenStatementFa);
		getGeneralDao().saveOrUpdate(screenStatementFa);
		
		ResponseScreen screenStatementEn = new ResponseScreen();
		screenStatementEn.setScreenno("788");
		screenStatementEn.setDesc("صورتحساب-داخلی-موفق-انگلیسی");
		screenStatementEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenStatementEn.setScreenData("788[FF][SI]@@[ESC]P2512[ESC]\\");
		screenStatementList.add(screenStatementEn);
		getGeneralDao().saveOrUpdate(screenStatementEn);
		
		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("صورتحساب-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("صورتحساب-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		/*************************/
		
		FunctionCommandResponse response00 = new FunctionCommandResponse();
		response00.setName("صورتحساب-داخلی-موفق");
		response00.setBeRetain(false);
		response00.setDispense(null);
//		response00.setFunctionCommand(NDCFunctionIdentifierConstants.PRINT_IMMEDIATE);
		response00.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		response00.setNextState("096");
//		response00.setNextScreen("388");
//		response00.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		response00.setScreen(screenStatementList);
		response00.setReceipt(receiptList00);
		getGeneralDao().saveOrUpdate(response00);
		
		FunctionCommandResponse creditResponse00 = new FunctionCommandResponse();
		creditResponse00.setName("صورتحساب-اعتباری داخلی-موفق");
		creditResponse00.setBeRetain(false);
		creditResponse00.setDispense(null);
//		creditResponse00.setFunctionCommand(NDCFunctionIdentifierConstants.PRINT_IMMEDIATE);
		creditResponse00.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		creditResponse00.setNextState("196");
//		creditResponse00.setNextScreen("388");
//		creditResponse00.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		creditResponse00.setScreen(screenStatementList);
		creditResponse00.setReceipt(receiptList00);
		getGeneralDao().saveOrUpdate(response00);

		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("صورتحساب-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);

		atmRequestFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), response00);
		atmRequestSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), response00);
		atmRequestSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//me
		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), creditResponse00);
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), creditResponse00);

		atmRequestTimeoutFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestSubTimeoutFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTimeoutEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestSubTimeoutEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);//me
		atmRequestCreditPasargadTimeoutFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadTimeoutEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

		getGeneralDao().saveOrUpdate(atmRequestFa);
		getGeneralDao().saveOrUpdate(atmRequestSubFa);
		getGeneralDao().saveOrUpdate(atmRequestEn);
		getGeneralDao().saveOrUpdate(atmRequestSubEn);//me
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTimeoutFa);
		getGeneralDao().saveOrUpdate(atmRequestSubTimeoutFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeoutEn);
		getGeneralDao().saveOrUpdate(atmRequestSubTimeoutEn);//me
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadTimeoutFa);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadTimeoutEn);

		requests.add(atmRequestFa);
		requests.add(atmRequestSubFa);
		requests.add(atmRequestEn);
		requests.add(atmRequestSubEn);//me
		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestCreditPasargadEn);
		requests.add(atmRequestTimeoutFa);
		requests.add(atmRequestSubTimeoutFa);
		requests.add(atmRequestTimeoutEn);
		requests.add(atmRequestSubTimeoutEn);//me
		requests.add(atmRequestCreditPasargadTimeoutFa);
		requests.add(atmRequestCreditPasargadTimeoutEn);
		
		return requests;
	}
	
	//TASK Task019 : Receipt Option
	List<ATMRequest> CharityHelp(ATMConfiguration configuration,OARResponse oarResponse) throws Exception{//Opkey ha bayad dorost shavand
		List<ATMRequest> requests = new ArrayList<ATMRequest>();

		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabFa = new ATMRequest();//Task019

		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabEn = new ATMRequest();//Task019

		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadFa = new ATMRequest();//Task019
		

		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubFa = new ATMRequest();//Task019

		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadEn = new ATMRequest();//Task019
		

		ATMRequest atmRequestPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubEn = new ATMRequest();//Task019
		

		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadFa = new ATMRequest();//Task019
		

		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadEn = new ATMRequest();//Task019


		ATMRequest atmRequestPreThirdPartyPaymentShetabFa = new ATMRequest();
		ATMRequest atmRequestPreThirdPartyPaymentShetabEn = new ATMRequest();
		ATMRequest atmRequestPreThirdPartyPaymentPasargadFa = new ATMRequest();
		ATMRequest atmRequestPreThirdPartyPaymentPasargadEn = new ATMRequest();
		ATMRequest atmRequestPreThirdPartyPaymentCreditFa = new ATMRequest();
		ATMRequest atmRequestPreThirdPartyPaymentCreditEn = new ATMRequest();

		ATMRequest atmRequestTimeOutPreThirdPartyPaymentShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreThirdPartyPaymentShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPreThirdPartyPaymentPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreThirdPartyPaymentPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPreThirdPartyPaymentCreditFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreThirdPartyPaymentCreditEn = new ATMRequest();




		ATMRequest atmRequestReceiptExceptionShetabFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionShetabEn = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadSubEn=new ATMRequest();
		ATMRequest atmRequestReceiptExceptionCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionCreditPasargadEn = new ATMRequest();


		String textFa = "[GR simpleThirdPartyPaymentReceiptFa()]";

		String textEn =
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Charity Help Receipt')]" : "[LF][LF][GR center(GR c2NCRE('Charity Help Receipt'))]")
			+ newLine
			+ lineEn
			+ receivedDateEn
			+ newLine + newLine
			+ formatAppPanEn
			+ newLine
			+ seqCntrEn
			+ newLine
			+ subAccEn //Task019
			+ newLine //Task019
			+ amountEn
			+ newLine
			+ (!atmType.equals(ATMType.NCR) ? "has been paid to " : "[GR c2NCRE('has been paid to ')]")
			+ openDoubleQuotationEn
			+ (!atmType.equals(ATMType.NCR) ? "[GR safeEn(ifx.ThirdPartyNameEn)]" : "[GR c2NCRE('institution.')]")
			+ closeDoubleQuotationEn
			+ " institution."
			+ newLine
			+ "[GR putLF(8)]"
			+ footerEn
			;

		String textJournal00 = "[GR simpleThirdPartyPaymentJournal()]" ;


		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();

		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList.add(receiptFa);

		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList.add(receiptEn);


		/*****************************/

		atmRequestPreThirdPartyPaymentShetabFa.setOpkey("AAFGC   ");
		atmRequestPreThirdPartyPaymentShetabFa.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE);
		atmRequestPreThirdPartyPaymentShetabFa.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestPreThirdPartyPaymentShetabFa.setCurrency(currency);
		atmRequestPreThirdPartyPaymentShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreThirdPartyPaymentShetabFa.setFit(FITType.SHETAB);

		atmRequestTimeOutPreThirdPartyPaymentShetabFa.setOpkey("AAFGC  F");
		atmRequestTimeOutPreThirdPartyPaymentShetabFa.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT);
		atmRequestTimeOutPreThirdPartyPaymentShetabFa.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPreThirdPartyPaymentShetabFa.setCurrency(currency);
		atmRequestTimeOutPreThirdPartyPaymentShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreThirdPartyPaymentShetabFa.setFit(FITType.SHETAB);

		atmRequestPreThirdPartyPaymentShetabEn.setOpkey("IAFGC   ");
		atmRequestPreThirdPartyPaymentShetabEn.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE);
		atmRequestPreThirdPartyPaymentShetabEn.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestPreThirdPartyPaymentShetabEn.setCurrency(currency);
		atmRequestPreThirdPartyPaymentShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreThirdPartyPaymentShetabEn.setFit(FITType.SHETAB);

		atmRequestTimeOutPreThirdPartyPaymentShetabEn.setOpkey("IAFGC  F");
		atmRequestTimeOutPreThirdPartyPaymentShetabEn.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT);
		atmRequestTimeOutPreThirdPartyPaymentShetabEn.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPreThirdPartyPaymentShetabEn.setCurrency(currency);
		atmRequestTimeOutPreThirdPartyPaymentShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreThirdPartyPaymentShetabEn.setFit(FITType.SHETAB);




		atmRequestPreThirdPartyPaymentPasargadFa.setOpkey("ABFGC   ");
		atmRequestPreThirdPartyPaymentPasargadFa.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE);
		atmRequestPreThirdPartyPaymentPasargadFa.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestPreThirdPartyPaymentPasargadFa.setCurrency(currency);
		atmRequestPreThirdPartyPaymentPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreThirdPartyPaymentPasargadFa.setFit(FITType.PASARGAD);

		atmRequestTimeOutPreThirdPartyPaymentPasargadFa.setOpkey("ABFGC  F");
		atmRequestTimeOutPreThirdPartyPaymentPasargadFa.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT);
		atmRequestTimeOutPreThirdPartyPaymentPasargadFa.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPreThirdPartyPaymentPasargadFa.setCurrency(currency);
		atmRequestTimeOutPreThirdPartyPaymentPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreThirdPartyPaymentPasargadFa.setFit(FITType.PASARGAD);

		atmRequestPreThirdPartyPaymentPasargadEn.setOpkey("IBFGC   ");
		atmRequestPreThirdPartyPaymentPasargadEn.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE);
		atmRequestPreThirdPartyPaymentPasargadEn.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestPreThirdPartyPaymentPasargadEn.setCurrency(currency);
		atmRequestPreThirdPartyPaymentPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreThirdPartyPaymentPasargadEn.setFit(FITType.PASARGAD);

		atmRequestTimeOutPreThirdPartyPaymentPasargadEn.setOpkey("IBFGC  F");
		atmRequestTimeOutPreThirdPartyPaymentPasargadEn.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT);
		atmRequestTimeOutPreThirdPartyPaymentPasargadEn.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPreThirdPartyPaymentPasargadEn.setCurrency(currency);
		atmRequestTimeOutPreThirdPartyPaymentPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreThirdPartyPaymentPasargadEn.setFit(FITType.PASARGAD);



		atmRequestPreThirdPartyPaymentCreditFa.setOpkey("ACFGC   ");
		atmRequestPreThirdPartyPaymentCreditFa.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE);
		atmRequestPreThirdPartyPaymentCreditFa.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestPreThirdPartyPaymentCreditFa.setCurrency(currency);
		atmRequestPreThirdPartyPaymentCreditFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreThirdPartyPaymentCreditFa.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutPreThirdPartyPaymentCreditFa.setOpkey("ACFGC  F");
		atmRequestTimeOutPreThirdPartyPaymentCreditFa.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT);
		atmRequestTimeOutPreThirdPartyPaymentCreditFa.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPreThirdPartyPaymentCreditFa.setCurrency(currency);
		atmRequestTimeOutPreThirdPartyPaymentCreditFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreThirdPartyPaymentCreditFa.setFit(FITType.CREDIT_PASARGAD);

		atmRequestPreThirdPartyPaymentCreditEn.setOpkey("ICFGC   ");
		atmRequestPreThirdPartyPaymentCreditEn.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE);
		atmRequestPreThirdPartyPaymentCreditEn.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestPreThirdPartyPaymentCreditEn.setCurrency(currency);
		atmRequestPreThirdPartyPaymentCreditEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreThirdPartyPaymentCreditEn.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutPreThirdPartyPaymentCreditEn.setOpkey("ICFGC  F");
		atmRequestTimeOutPreThirdPartyPaymentCreditEn.setIfxType(IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT);
		atmRequestTimeOutPreThirdPartyPaymentCreditEn.setTrnType(TrnType.PREPARE_THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPreThirdPartyPaymentCreditEn.setCurrency(currency);
		atmRequestTimeOutPreThirdPartyPaymentCreditEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreThirdPartyPaymentCreditEn.setFit(FITType.CREDIT_PASARGAD);


		/*****************************/

		atmRequestShetabFa.setOpkey("AAFGA   ");
		atmRequestShetabFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestShetabFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);

		atmRequestTimeOutShetabFa.setOpkey("AAFGA  F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);

		atmRequestReceiptExceptionShetabFa.setOpkey("AAFGA  A");
		atmRequestReceiptExceptionShetabFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionShetabFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionShetabFa.setForceReceipt(false);
		atmRequestReceiptExceptionShetabFa.setCurrency(currency);
		atmRequestReceiptExceptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionShetabFa.setFit(FITType.SHETAB);
		
		//Task019 
		atmRequestReceiptOptionShetabFa.setOpkey("AAFGA  B");
		atmRequestReceiptOptionShetabFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionShetabFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionShetabFa.setCurrency(currency);
		atmRequestReceiptOptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionShetabFa.setFit(FITType.SHETAB);
		atmRequestReceiptOptionShetabFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestShetabEn.setOpkey("IAFGA   ");
		atmRequestShetabEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestShetabEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);

		atmRequestTimeOutShetabEn.setOpkey("IAFGA  F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);

		atmRequestReceiptExceptionShetabEn.setOpkey("IAFGA  A");
		atmRequestReceiptExceptionShetabEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionShetabEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionShetabEn.setForceReceipt(false);
		atmRequestReceiptExceptionShetabEn.setCurrency(currency);
		atmRequestReceiptExceptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionShetabEn.setFit(FITType.SHETAB);
		
		//Task019 
		atmRequestReceiptOptionShetabEn.setOpkey("IAFGA  B");
		atmRequestReceiptOptionShetabEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionShetabEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionShetabEn.setCurrency(currency);
		atmRequestReceiptOptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionShetabEn.setFit(FITType.SHETAB);
		atmRequestReceiptOptionShetabEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestPasargadFa.setOpkey("ABFGA   ");
		atmRequestPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadFa.setOpkey("ABFGA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);

		atmRequestReceiptExceptionPasargadFa.setOpkey("ABFGA  A");
		atmRequestReceiptExceptionPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionPasargadFa.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadFa.setCurrency(currency);
		atmRequestReceiptExceptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionPasargadFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadFa.setOpkey("ABFGA  B");
		atmRequestReceiptOptionPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadFa.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestPasargadSubFa.setOpkey("ABFGB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABFGA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadSubFa.setOpkey("ABFGB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABFGA  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);

		//TODO: double check for English version
		atmRequestReceiptExceptionPasargadSubFa.setOpkey("ABFGB  A");
		atmRequestReceiptExceptionPasargadSubFa.setNextOpkey("ABFGA  A");
		atmRequestReceiptExceptionPasargadSubFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptExceptionPasargadSubFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestReceiptExceptionPasargadSubFa.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadSubFa.setCurrency(currency);
		atmRequestReceiptExceptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionPasargadSubFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadSubFa.setOpkey("ABFGB  B");
		atmRequestReceiptOptionPasargadSubFa.setNextOpkey("ABFGA  B");
		atmRequestReceiptOptionPasargadSubFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestReceiptOptionPasargadSubFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadSubFa.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadSubFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestPasargadEn.setOpkey("IBFGA   ");
		atmRequestPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadEn.setOpkey("IBFGA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);

		atmRequestReceiptExceptionPasargadEn.setOpkey("IBFGA  A");
		atmRequestReceiptExceptionPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionPasargadEn.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadEn.setCurrency(currency);
		atmRequestReceiptExceptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionPasargadEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadEn.setOpkey("IBFGA  B");
		atmRequestReceiptOptionPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadEn.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		
		/*****************************/

		atmRequestPasargadSubEn.setOpkey("IBFGB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBFGA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);

		atmRequestTimeOutPasargadSubEn.setOpkey("IBFGB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBFGA  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);


		atmRequestReceiptExceptionPasargadSubEn.setOpkey("ABFGB  A");
		atmRequestReceiptExceptionPasargadSubEn.setNextOpkey("ABFGA  A");
		atmRequestReceiptExceptionPasargadSubEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptExceptionPasargadSubEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestReceiptExceptionPasargadSubEn.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadSubEn.setCurrency(currency);
		atmRequestReceiptExceptionPasargadSubEn.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionPasargadSubEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadSubEn.setOpkey("IBFGB  B");
		atmRequestReceiptOptionPasargadSubEn.setNextOpkey("IBFGA  B");
		atmRequestReceiptOptionPasargadSubEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionPasargadSubEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadSubEn.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadSubEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);

		/*****************************/

		atmRequestCreditPasargadFa.setOpkey("ACFGA   ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutCreditPasargadFa.setOpkey("ACFGA  F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);

		atmRequestReceiptExceptionCreditPasargadFa.setOpkey("ACFGA  A");
		atmRequestReceiptExceptionCreditPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionCreditPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionCreditPasargadFa.setForceReceipt(false);
		atmRequestReceiptExceptionCreditPasargadFa.setCurrency(currency);
		atmRequestReceiptExceptionCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019 
		atmRequestReceiptOptionCreditPasargadFa.setOpkey("ACFGA  B");
		atmRequestReceiptOptionCreditPasargadFa.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionCreditPasargadFa.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionCreditPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		atmRequestReceiptOptionCreditPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/

		atmRequestCreditPasargadEn.setOpkey("ICFGA   ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);

		atmRequestTimeOutCreditPasargadEn.setOpkey("ICFGA  F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);

		atmRequestReceiptExceptionCreditPasargadEn.setOpkey("ICFGA  A");
		atmRequestReceiptExceptionCreditPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptExceptionCreditPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptExceptionCreditPasargadEn.setForceReceipt(false);
		atmRequestReceiptExceptionCreditPasargadEn.setCurrency(currency);
		atmRequestReceiptExceptionCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019
		atmRequestReceiptOptionCreditPasargadEn.setOpkey("ICFGA  B");
		atmRequestReceiptOptionCreditPasargadEn.setIfxType(IfxType.THIRD_PARTY_PURCHASE_RQ);
		atmRequestReceiptOptionCreditPasargadEn.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
		atmRequestReceiptOptionCreditPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		atmRequestReceiptOptionCreditPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		
		

		/*****************************/

		List<ResponseScreen> screenBillPaymentList = new ArrayList<ResponseScreen>();

		ResponseScreen screenBillPayFa = new ResponseScreen();
		screenBillPayFa.setScreenno("388");
		screenBillPayFa.setDesc("پرداخت به خیریه-موفق-فارسی");
		screenBillPayFa.setLanguage(UserLanguage.FARSI_LANG);
		screenBillPayFa.setScreenData("388[FF][SI]@@[ESC]P2133[ESC]\\");
		screenBillPaymentList.add(screenBillPayFa);
		getGeneralDao().saveOrUpdate(screenBillPayFa);

		ResponseScreen screenBillPayEn = new ResponseScreen();
		screenBillPayEn.setScreenno("788");
		screenBillPayEn.setDesc("پرداخت به خیریه-موفق-انگلیسی");
		screenBillPayEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenBillPayEn.setScreenData("788[FF][SI]@@[ESC]P2533[ESC]\\");
		screenBillPaymentList.add(screenBillPayEn);
		getGeneralDao().saveOrUpdate(screenBillPayEn);

		List<ResponseScreen> screenCheckOrganizationList = new ArrayList<ResponseScreen>();

		ResponseScreen screenCheckOrganizationFa = new ResponseScreen();
		screenCheckOrganizationFa.setScreenno("067");
		screenCheckOrganizationFa.setDesc("تایید پرداخت به خیریه-موفق-فارسی");
		screenCheckOrganizationFa.setLanguage(UserLanguage.FARSI_LANG);
		if (!atmType.equals(ATMType.NCR)) {
			screenCheckOrganizationFa.setScreenData("067[ESC]P2131[ESC]\\"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]G0[GR c2F(ifx.networkTrnInfo.ThirdPartyName)]"
					+ "[SI]H0[GR ifx.Auth_Amt]");
		} else {
			screenCheckOrganizationFa.setScreenData("067[ESC]P2131[ESC]\\"
					+ "[ESC](6[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]G0[GR c2F(ifx.networkTrnInfo.ThirdPartyName)]"
					+ "[SI]H0[GR ifx.Auth_Amt]");
		}
		screenCheckOrganizationList.add(screenCheckOrganizationFa);
		getGeneralDao().saveOrUpdate(screenCheckOrganizationFa);

		ResponseScreen screenCheckOrganizationEn = new ResponseScreen();
		screenCheckOrganizationEn.setScreenno("467");
		screenCheckOrganizationEn.setDesc("تایید پرداخت به خیریه-موفق-انگلیسی");
		screenCheckOrganizationEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCheckOrganizationEn.setScreenData("467[ESC]P2531[ESC]\\"
				+ "[ESC](1[ESC][OC]"
				+ updateablePageFontColor
				+ "[SI]G0[GR safeEn(ifx.networkTrnInfo.ThirdPartyNameEn)]"
				+ "[SI]H0[GR ifx.Auth_Amt]");
		screenCheckOrganizationList.add(screenCheckOrganizationEn);
		getGeneralDao().saveOrUpdate(screenCheckOrganizationEn);

		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();

		ResponseScreen screenRecieptFa = new ResponseScreen();
		screenRecieptFa.setScreenno("384");
		screenRecieptFa.setDesc("کمک به خیریه-خطای رسید-فارسی");
		screenRecieptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenRecieptFa.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
		screenRecieptList.add(screenRecieptFa);
		getGeneralDao().saveOrUpdate(screenRecieptFa);

		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("784");
		screenRecieptEn.setDesc("کمک به خیریه-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData("784[FF][SI]@@[ESC]P2542[ESC]\\");
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();

		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("کمک به خیریه-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);

		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("کمک به خیریه-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		/*****************************/

		//******************* dar state table hanoz ezafe nashode
		FunctionCommandResponse responseShetab = new FunctionCommandResponse();
		responseShetab.setName("تایید پرداخت قبض-شتابی-موفق");
		responseShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseShetab.setNextState("569");
//		responseShetab.setNextScreen("032");
		responseShetab.setScreen(screenCheckOrganizationList);
		getGeneralDao().saveOrUpdate(responseShetab);

		/*****************************/

		FunctionCommandResponse responsePasargad = new FunctionCommandResponse();
		responsePasargad.setName("تایید کمک به خیریه-داخلی-موفق");
		responsePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responsePasargad.setNextState("105");
		responsePasargad.setScreen(screenCheckOrganizationList);
		getGeneralDao().saveOrUpdate(responsePasargad);

		/*****************************/
		//******************* dar state table hanoz ezafe nashode
		FunctionCommandResponse responseCreditPasargad = new FunctionCommandResponse();
		responseCreditPasargad.setName("تایید پرداخت قبض-اعتباری داخلی-موفق");
		responseCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCreditPasargad.setNextState("169");
		responseCreditPasargad.setScreen(screenCheckOrganizationList);
		getGeneralDao().saveOrUpdate(responseCreditPasargad);

		/*****************************/
		/*****************************/
		///************ dar state table hanoz ezafe nashode
		FunctionCommandResponse responseThirdPartyPaymentShetab = new FunctionCommandResponse();
		responseThirdPartyPaymentShetab.setName("کمک به خیریه-شتابی-موفق");//Task019
		responseThirdPartyPaymentShetab.setBeRetain(false);
		responseThirdPartyPaymentShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseThirdPartyPaymentShetab.setNextState("596");
		responseThirdPartyPaymentShetab.setScreen(screenBillPaymentList);
		responseThirdPartyPaymentShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseThirdPartyPaymentShetab);

		FunctionCommandResponse responseReceiptExceptionShetab = new FunctionCommandResponse();
		responseReceiptExceptionShetab.setName("پرداخت قبض-شتابی-خطای رسید");
		responseReceiptExceptionShetab.setBeRetain(false);
		responseReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionShetab.setNextState("592");
		responseReceiptExceptionShetab.setScreen(screenRecieptList);
		responseReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionShetab);

		/*****************************/

		FunctionCommandResponse responseThirdPArtyPaymentPasargad = new FunctionCommandResponse();
		responseThirdPArtyPaymentPasargad.setName("کمک به خیریه-داخلی-موفق");
		responseThirdPArtyPaymentPasargad.setBeRetain(false);
		responseThirdPArtyPaymentPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseThirdPArtyPaymentPasargad.setNextState("096");
		responseThirdPArtyPaymentPasargad.setScreen(screenBillPaymentList);
		responseThirdPArtyPaymentPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseThirdPArtyPaymentPasargad);

		FunctionCommandResponse responseReceiptExceptionPasargad = new FunctionCommandResponse();
		responseReceiptExceptionPasargad.setName("کمک به خیریه-داخلی-خطای رسید");
		responseReceiptExceptionPasargad.setBeRetain(false);
		responseReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionPasargad.setNextState("092");
		responseReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionPasargad);

		/*****************************/
		//hanoz dar state table ezafe nashode

		FunctionCommandResponse responseThirdPartyPaymentCreditPasargad = new FunctionCommandResponse();
		responseThirdPartyPaymentCreditPasargad.setName("کمک به خیریه-اعتباری داخلی-موفق");//Task019
		responseThirdPartyPaymentCreditPasargad.setBeRetain(false);
		responseThirdPartyPaymentCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseThirdPartyPaymentCreditPasargad.setNextState("196");
		responseThirdPartyPaymentCreditPasargad.setScreen(screenBillPaymentList);
		responseThirdPartyPaymentCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseThirdPartyPaymentCreditPasargad);

		FunctionCommandResponse responseReceiptExceptionCreditPasargad = new FunctionCommandResponse();
		responseReceiptExceptionCreditPasargad.setName("پرداخت قبض-اعتباری داخلی-خطای رسید");
		responseReceiptExceptionCreditPasargad.setBeRetain(false);
		responseReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionCreditPasargad.setNextState("192");
		responseReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
		responseReceiptExceptionCreditPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionCreditPasargad);

		/*****************************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("کمک به خیریه-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		/*****************************/

		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);
		atmRequestReceiptOptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);//Task019

		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);
		atmRequestReceiptOptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);//Task019

		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);//Task019

		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019

		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);//Task019

		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019

		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		atmRequestReceiptOptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);//Task019

		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		atmRequestReceiptOptionCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);//Task019

		/*****************************/

		atmRequestReceiptExceptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);
		atmRequestReceiptExceptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);
		atmRequestReceiptExceptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
		atmRequestReceiptExceptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
		atmRequestReceiptExceptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
		atmRequestReceiptExceptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
		atmRequestReceiptExceptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);
		atmRequestReceiptExceptionCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);

		/*****************************/

		atmRequestPreThirdPartyPaymentShetabEn.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseShetab);
		atmRequestTimeOutPreThirdPartyPaymentShetabEn.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseTimeOut);
		atmRequestPreThirdPartyPaymentShetabFa.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseShetab);
		atmRequestTimeOutPreThirdPartyPaymentShetabFa.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseTimeOut);
		atmRequestPreThirdPartyPaymentPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responsePasargad);
		atmRequestTimeOutPreThirdPartyPaymentPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseTimeOut);
		atmRequestPreThirdPartyPaymentPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responsePasargad);
		atmRequestTimeOutPreThirdPartyPaymentPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseTimeOut);
		atmRequestPreThirdPartyPaymentCreditFa.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseCreditPasargad);
		atmRequestTimeOutPreThirdPartyPaymentCreditFa.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseTimeOut);
		atmRequestPreThirdPartyPaymentCreditEn.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseCreditPasargad);
		atmRequestTimeOutPreThirdPartyPaymentCreditEn.addAtmResponse(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT, responseTimeOut);

		/*****************************/

		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabFa);//Task019

		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabEn);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadFa);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubFa);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadEn);//Task019

		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubEn);//Task019

		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadFa);//Task019

		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadEn);//Task019

		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionCreditPasargadEn);

		getGeneralDao().saveOrUpdate(atmRequestPreThirdPartyPaymentShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestPreThirdPartyPaymentShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestPreThirdPartyPaymentPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestPreThirdPartyPaymentPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestPreThirdPartyPaymentCreditFa);
		getGeneralDao().saveOrUpdate(atmRequestPreThirdPartyPaymentCreditEn);

		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreThirdPartyPaymentShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreThirdPartyPaymentShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreThirdPartyPaymentPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreThirdPartyPaymentPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreThirdPartyPaymentCreditFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreThirdPartyPaymentCreditEn);



		requests.add(atmRequestShetabFa);
		requests.add(atmRequestTimeOutShetabFa);
		requests.add(atmRequestReceiptOptionShetabFa);//Task019

		requests.add(atmRequestShetabEn);
		requests.add(atmRequestTimeOutShetabEn);
		requests.add(atmRequestReceiptOptionShetabEn);//Task019

		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestTimeOutPasargadFa);
		requests.add(atmRequestReceiptOptionPasargadFa);//Task019
		
		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadSubFa);
		requests.add(atmRequestReceiptOptionPasargadSubFa);//Task019

		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestTimeOutPasargadEn);
		requests.add(atmRequestReceiptOptionPasargadEn);//Task019

		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestTimeOutPasargadSubEn);
		requests.add(atmRequestReceiptOptionPasargadSubEn);//Task019

		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadFa);
		requests.add(atmRequestReceiptOptionCreditPasargadFa);//Task019

		requests.add(atmRequestCreditPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		requests.add(atmRequestReceiptOptionCreditPasargadEn);//Task019

		requests.add(atmRequestReceiptExceptionShetabFa);
		requests.add(atmRequestReceiptExceptionShetabEn);
		requests.add(atmRequestReceiptExceptionPasargadFa);
		requests.add(atmRequestReceiptExceptionPasargadSubFa);
		requests.add(atmRequestReceiptExceptionPasargadEn);
		requests.add(atmRequestReceiptExceptionPasargadSubEn);
		requests.add(atmRequestReceiptExceptionCreditPasargadFa);
		requests.add(atmRequestReceiptExceptionCreditPasargadEn);

		requests.add(atmRequestPreThirdPartyPaymentShetabFa);
		requests.add(atmRequestPreThirdPartyPaymentShetabEn);
		requests.add(atmRequestPreThirdPartyPaymentPasargadFa);
		requests.add(atmRequestPreThirdPartyPaymentPasargadEn);
		requests.add(atmRequestPreThirdPartyPaymentCreditFa);
		requests.add(atmRequestPreThirdPartyPaymentCreditEn);

		requests.add(atmRequestTimeOutPreThirdPartyPaymentShetabFa);
		requests.add(atmRequestTimeOutPreThirdPartyPaymentShetabEn);
		requests.add(atmRequestTimeOutPreThirdPartyPaymentPasargadFa);
		requests.add(atmRequestTimeOutPreThirdPartyPaymentPasargadEn);
		requests.add(atmRequestTimeOutPreThirdPartyPaymentCreditFa);
		requests.add(atmRequestTimeOutPreThirdPartyPaymentCreditEn);

		return requests;
	}
	
	
	//TASK Task019
	private List<ATMRequest> Billpayment(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ATMRequest atmRequestShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabFa = new ATMRequest();//Task019
		
		ATMRequest atmRequestShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionShetabEn = new ATMRequest();//Task019
		
		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadFa = new ATMRequest();//Task019
		
		
		ATMRequest atmRequestPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubFa = new ATMRequest();//Task019
		
		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadEn = new ATMRequest();//Task019
		
		ATMRequest atmRequestPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadSubEn = new ATMRequest();//Task019
		
		
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadFa = new ATMRequest();//Task019
		
		
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadEn = new ATMRequest();//Task019
		
		
		ATMRequest atmRequestReceiptExceptionShetabFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionShetabEn = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadSubEn=new ATMRequest();
		ATMRequest atmRequestReceiptExceptionCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionCreditPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestPreBillShetabFa = new ATMRequest();
		ATMRequest atmRequestPreBillShetabEn = new ATMRequest();
		ATMRequest atmRequestPreBillPasargadFa = new ATMRequest();
		ATMRequest atmRequestPreBillPasargadEn = new ATMRequest();
		ATMRequest atmRequestPreBillCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestPreBillCreditPasargadEn = new ATMRequest();

		ATMRequest atmRequestTimeOutPreBillShetabFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreBillShetabEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPreBillPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreBillPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutPreBillCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPreBillCreditPasargadEn = new ATMRequest();
		
		String textFa = "[GR simpleBillPaymentReceiptFa()]";
		
		String textEn =
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Bill Payment Receipt')]" : "[LF][LF][GR center(GR c2NCRE('Bill Payment Receipt'))]")
			+ newLine
			+ lineEn
			+ receivedDateEn
			+ newLine + newLine
			+ formatAppPanEn
			+ newLine  
			+ seqCntrEn
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(ifx.BillID, 'Bill ID')]" : "[GR justify(ifx.BillID, GR c2NCRE('Bill ID'))]")
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.BillPaymentID, 'Payment ID')]" : "[GR justify(GR ifx.BillPaymentID, GR c2NCRE('Payment ID'))]")
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR c2E(ifx.BillOrgType), 'Organization')]" : "[GR justify(GR c2NCRE(ifx.BillOrgType), GR c2NCRE('Organization'))]")
			+ newLine  
			+ amountEn
			+ "[GR putLF(8)]"
			+ footerEn
			;
		
		String textJournal00 = "[GR simpleBillPaymentJournal()]" ;

		
		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
		
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList.add(receiptFa);
		
		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList.add(receiptEn);

		/*****************************/
		atmRequestPreBillShetabFa.setOpkey("AAHA    ");
		atmRequestPreBillShetabFa.setIfxType(IfxType.PREPARE_BILL_PMT);
		atmRequestPreBillShetabFa.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestPreBillShetabFa.setCurrency(currency);
		atmRequestPreBillShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreBillShetabFa.setFit(FITType.SHETAB);

		atmRequestTimeOutPreBillShetabFa.setOpkey("AAHA   F");
		atmRequestTimeOutPreBillShetabFa.setIfxType(IfxType.PREPARE_BILL_PMT_REV_REPEAT);
		atmRequestTimeOutPreBillShetabFa.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestTimeOutPreBillShetabFa.setCurrency(currency);
		atmRequestTimeOutPreBillShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreBillShetabFa.setFit(FITType.SHETAB);
		
		atmRequestPreBillShetabEn.setOpkey("IAHA    ");
		atmRequestPreBillShetabEn.setIfxType(IfxType.PREPARE_BILL_PMT);
		atmRequestPreBillShetabEn.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestPreBillShetabEn.setCurrency(currency);
		atmRequestPreBillShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreBillShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTimeOutPreBillShetabEn.setOpkey("IAHA   F");
		atmRequestTimeOutPreBillShetabEn.setIfxType(IfxType.PREPARE_BILL_PMT_REV_REPEAT);
		atmRequestTimeOutPreBillShetabEn.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestTimeOutPreBillShetabEn.setCurrency(currency);
		atmRequestTimeOutPreBillShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreBillShetabEn.setFit(FITType.SHETAB);
		
		atmRequestPreBillPasargadFa.setOpkey("ABHA    ");
		atmRequestPreBillPasargadFa.setIfxType(IfxType.PREPARE_BILL_PMT);
		atmRequestPreBillPasargadFa.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestPreBillPasargadFa.setCurrency(currency);
		atmRequestPreBillPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreBillPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPreBillPasargadFa.setOpkey("ABHA   F");
		atmRequestTimeOutPreBillPasargadFa.setIfxType(IfxType.PREPARE_BILL_PMT_REV_REPEAT);
		atmRequestTimeOutPreBillPasargadFa.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestTimeOutPreBillPasargadFa.setCurrency(currency);
		atmRequestTimeOutPreBillPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreBillPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestPreBillPasargadEn.setOpkey("IBHA    ");
		atmRequestPreBillPasargadEn.setIfxType(IfxType.PREPARE_BILL_PMT);
		atmRequestPreBillPasargadEn.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestPreBillPasargadEn.setCurrency(currency);
		atmRequestPreBillPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreBillPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPreBillPasargadEn.setOpkey("IBHA   F");
		atmRequestTimeOutPreBillPasargadEn.setIfxType(IfxType.PREPARE_BILL_PMT_REV_REPEAT);
		atmRequestTimeOutPreBillPasargadEn.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestTimeOutPreBillPasargadEn.setCurrency(currency);
		atmRequestTimeOutPreBillPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreBillPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestPreBillCreditPasargadFa.setOpkey("ACHA    ");
		atmRequestPreBillCreditPasargadFa.setIfxType(IfxType.PREPARE_BILL_PMT);
		atmRequestPreBillCreditPasargadFa.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestPreBillCreditPasargadFa.setCurrency(currency);
		atmRequestPreBillCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPreBillCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutPreBillCreditPasargadFa.setOpkey("ACHA   F");
		atmRequestTimeOutPreBillCreditPasargadFa.setIfxType(IfxType.PREPARE_BILL_PMT_REV_REPEAT);
		atmRequestTimeOutPreBillCreditPasargadFa.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestTimeOutPreBillCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutPreBillCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPreBillCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestPreBillCreditPasargadEn.setOpkey("ICHA    ");
		atmRequestPreBillCreditPasargadEn.setIfxType(IfxType.PREPARE_BILL_PMT);
		atmRequestPreBillCreditPasargadEn.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestPreBillCreditPasargadEn.setCurrency(currency);
		atmRequestPreBillCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPreBillCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutPreBillCreditPasargadEn.setOpkey("ICHA   F");
		atmRequestTimeOutPreBillCreditPasargadEn.setIfxType(IfxType.PREPARE_BILL_PMT_REV_REPEAT);
		atmRequestTimeOutPreBillCreditPasargadEn.setTrnType(TrnType.PREPARE_BILL_PMT);
		atmRequestTimeOutPreBillCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutPreBillCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPreBillCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		/*****************************/
		
		atmRequestShetabFa.setOpkey("AAHB    ");
		atmRequestShetabFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestShetabFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestShetabFa.setCurrency(currency);
		atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabFa.setOpkey("AAHB   F");
		atmRequestTimeOutShetabFa.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutShetabFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutShetabFa.setCurrency(currency);
		atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);
		
		atmRequestReceiptExceptionShetabFa.setOpkey("AAHB   A");
		atmRequestReceiptExceptionShetabFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptExceptionShetabFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptExceptionShetabFa.setForceReceipt(false);
		atmRequestReceiptExceptionShetabFa.setCurrency(currency);
		atmRequestReceiptExceptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionShetabFa.setFit(FITType.SHETAB);
		
		//Task019
		atmRequestReceiptOptionShetabFa.setOpkey("AAHB   B");
		atmRequestReceiptOptionShetabFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionShetabFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionShetabFa.setCurrency(currency);
		atmRequestReceiptOptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionShetabFa.setFit(FITType.SHETAB);
		atmRequestReceiptOptionShetabFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/
		
		atmRequestShetabEn.setOpkey("IAHB    ");
		atmRequestShetabEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestShetabEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestShetabEn.setCurrency(currency);
		atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTimeOutShetabEn.setOpkey("IAHB   F");
		atmRequestTimeOutShetabEn.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutShetabEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutShetabEn.setCurrency(currency);
		atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);
		
		atmRequestReceiptExceptionShetabEn.setOpkey("IAHB   A");
		atmRequestReceiptExceptionShetabEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptExceptionShetabEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptExceptionShetabEn.setForceReceipt(false);
		atmRequestReceiptExceptionShetabEn.setCurrency(currency);
		atmRequestReceiptExceptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionShetabEn.setFit(FITType.SHETAB);
		
		//Task019
		atmRequestReceiptOptionShetabEn.setOpkey("IAHB   B");
		atmRequestReceiptOptionShetabEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionShetabEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionShetabEn.setCurrency(currency);
		atmRequestReceiptOptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionShetabEn.setFit(FITType.SHETAB);	
		atmRequestReceiptOptionShetabEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/
		
		atmRequestPasargadFa.setOpkey("ABHBA   ");
		atmRequestPasargadFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadFa.setOpkey("ABHBA  F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestReceiptExceptionPasargadFa.setOpkey("ABHBA  A");
		atmRequestReceiptExceptionPasargadFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptExceptionPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptExceptionPasargadFa.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadFa.setCurrency(currency);
		atmRequestReceiptExceptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionPasargadFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadFa.setOpkey("ABHBA  B");
		atmRequestReceiptOptionPasargadFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadFa.setFit(FITType.PASARGAD);	
		atmRequestReceiptOptionPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/
		
		atmRequestPasargadSubFa.setOpkey("ABHBB   ");
		atmRequestPasargadSubFa.setNextOpkey("ABHBA   ");
		atmRequestPasargadSubFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestPasargadSubFa.setCurrency(currency);
		atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadSubFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadSubFa.setOpkey("ABHBB  F");
		atmRequestTimeOutPasargadSubFa.setNextOpkey("ABHBB  F");
		atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);
		
		//TODO: double check for English version
		atmRequestReceiptExceptionPasargadSubFa.setOpkey("ABHBB  A");
		atmRequestReceiptExceptionPasargadSubFa.setNextOpkey("ABHBA  A");
		atmRequestReceiptExceptionPasargadSubFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptExceptionPasargadSubFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptExceptionPasargadSubFa.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadSubFa.setCurrency(currency);
		atmRequestReceiptExceptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionPasargadSubFa.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadSubFa.setOpkey("ABHBB  B");
		atmRequestReceiptOptionPasargadSubFa.setNextOpkey("ABHBA  B");
		atmRequestReceiptOptionPasargadSubFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestReceiptOptionPasargadSubFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadSubFa.setFit(FITType.PASARGAD);		
		atmRequestReceiptOptionPasargadSubFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/
		
		atmRequestPasargadEn.setOpkey("IBHBA   ");
		atmRequestPasargadEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadEn.setOpkey("IBHBA  F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestReceiptExceptionPasargadEn.setOpkey("IBHBA  A");
		atmRequestReceiptExceptionPasargadEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptExceptionPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptExceptionPasargadEn.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadEn.setCurrency(currency);
		atmRequestReceiptExceptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionPasargadEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptOptionPasargadEn.setOpkey("IBHBA  B");
		atmRequestReceiptOptionPasargadEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadEn.setFit(FITType.PASARGAD);	
		atmRequestReceiptOptionPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		
		/********************************/
		
		//TODO BILL_PMT_RQ + GET_ACCOUNT_RQ +  'IBHBB  A'
		atmRequestPasargadSubEn.setOpkey("IBHBB   ");
		atmRequestPasargadSubEn.setNextOpkey("IBHBA   ");
		atmRequestPasargadSubEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestPasargadSubEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestPasargadSubEn.setCurrency(currency);
		atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadSubEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadSubEn.setOpkey("IBHBB  F");
		atmRequestTimeOutPasargadSubEn.setNextOpkey("IBHBB  F");
		atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);
		
		//Task019
		atmRequestReceiptExceptionPasargadSubEn.setOpkey("IBHBB  A");
		atmRequestReceiptExceptionPasargadSubEn.setNextOpkey("IBHBA  A");
		atmRequestReceiptExceptionPasargadSubEn.setIfxType(IfxType.BILL_PMT_RQ);
//		atmRequestReceiptExceptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptExceptionPasargadSubEn.setTrnType(TrnType.BILLPAYMENT);
//		atmRequestReceiptExceptionPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestReceiptExceptionPasargadSubEn.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadSubEn.setCurrency(currency);
		atmRequestReceiptExceptionPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionPasargadSubEn.setFit(FITType.PASARGAD);		
		
		//Task019
		atmRequestReceiptOptionPasargadSubEn.setOpkey("IBHBB  B");
		atmRequestReceiptOptionPasargadSubEn.setNextOpkey("IBHBA  B");
		atmRequestReceiptOptionPasargadSubEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestReceiptOptionPasargadSubEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionPasargadSubEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadSubEn.setFit(FITType.PASARGAD);
		atmRequestReceiptOptionPasargadSubEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		
		/*****************************/
		
		atmRequestCreditPasargadFa.setOpkey("ACHB    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadFa.setOpkey("ACHB   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestReceiptExceptionCreditPasargadFa.setOpkey("ACHB   A");
		atmRequestReceiptExceptionCreditPasargadFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptExceptionCreditPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptExceptionCreditPasargadFa.setForceReceipt(false);
		atmRequestReceiptExceptionCreditPasargadFa.setCurrency(currency);
		atmRequestReceiptExceptionCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019
		atmRequestReceiptOptionCreditPasargadFa.setOpkey("ACHB   B");
		atmRequestReceiptOptionCreditPasargadFa.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionCreditPasargadFa.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionCreditPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		atmRequestReceiptOptionCreditPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		/*****************************/
		
		atmRequestCreditPasargadEn.setOpkey("ICHB    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadEn.setOpkey("ICHB   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestReceiptExceptionCreditPasargadEn.setOpkey("ICHB   A");
		atmRequestReceiptExceptionCreditPasargadEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptExceptionCreditPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptExceptionCreditPasargadEn.setForceReceipt(false);
		atmRequestReceiptExceptionCreditPasargadEn.setCurrency(currency);
		atmRequestReceiptExceptionCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019
		atmRequestReceiptOptionCreditPasargadEn.setOpkey("ICHB   B");
		atmRequestReceiptOptionCreditPasargadEn.setIfxType(IfxType.BILL_PMT_RQ);
		atmRequestReceiptOptionCreditPasargadEn.setTrnType(TrnType.BILLPAYMENT);
		atmRequestReceiptOptionCreditPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		atmRequestReceiptOptionCreditPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		
		/*****************************/
		List<ResponseScreen> screenBillPaymentList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenBillPayFa = new ResponseScreen();
		screenBillPayFa.setScreenno("388");
		screenBillPayFa.setDesc("پرداخت قبض-موفق-فارسی");
		screenBillPayFa.setLanguage(UserLanguage.FARSI_LANG);
		screenBillPayFa.setScreenData("388[FF][SI]@@[ESC]P2126[ESC]\\");
		screenBillPaymentList.add(screenBillPayFa);
		getGeneralDao().saveOrUpdate(screenBillPayFa);
		
		ResponseScreen screenBillPayEn = new ResponseScreen();
		screenBillPayEn.setScreenno("788");
		screenBillPayEn.setDesc("پرداخت قبض-موفق-انگلیسی");
		screenBillPayEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenBillPayEn.setScreenData("788[FF][SI]@@[ESC]P2526[ESC]\\");
		screenBillPaymentList.add(screenBillPayEn);
		getGeneralDao().saveOrUpdate(screenBillPayEn);
		
		List<ResponseScreen> screenCheckOrganizationList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCheckOrganizationFa = new ResponseScreen();
		screenCheckOrganizationFa.setScreenno("032");
		screenCheckOrganizationFa.setDesc("تایید پرداخت قبض-موفق-فارسی");
		screenCheckOrganizationFa.setLanguage(UserLanguage.FARSI_LANG);
		if (!atmType.equals(ATMType.NCR)) {
			screenCheckOrganizationFa.setScreenData("032[ESC]P2086[ESC]\\"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]F0[GR ifx.BillID]"
					+ "[SI]G0[GR ifx.BillPaymentID]"
					+ "[SI]H0[GR c2F(ifx.ThirdPartyName)]"
					+ "[SI]I0[GR ifx.Auth_Amt]");
		} else {
			screenCheckOrganizationFa.setScreenData("032[ESC]P2086[ESC]\\"
					+ "[ESC](6[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]F0[GR ifx.BillID]"
					+ "[SI]G0[GR ifx.BillPaymentID]"
					+ "[SI]H0[GR c2F(ifx.ThirdPartyName)]"
					+ "[SI]I0[GR ifx.Auth_Amt]");			
		}
		screenCheckOrganizationList.add(screenCheckOrganizationFa);
		getGeneralDao().saveOrUpdate(screenCheckOrganizationFa);
		
		ResponseScreen screenCheckOrganizationEn = new ResponseScreen();
		screenCheckOrganizationEn.setScreenno("432");
		screenCheckOrganizationEn.setDesc("تایید پرداخت قبض-موفق-انگلیسی");
		screenCheckOrganizationEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCheckOrganizationEn.setScreenData("432[ESC]P2486[ESC]\\"
				+ "[ESC](1[ESC][OC]"
				+ updateablePageFontColor
				+ "[SI]F0[GR ifx.BillID]"
				+ "[SI]G0[GR ifx.BillPaymentID]"
				+ (!atmType.equals(ATMType.NCR) ? "[SI]H0[GR c2E(ifx.ThirdPartyNameEn)]"  : "[SI]H0[GR c2NCR_SCR_E(ifx.ThirdPartyNameEn)]")
				+ "[SI]I0[GR ifx.Auth_Amt]");
		screenCheckOrganizationList.add(screenCheckOrganizationEn);
		getGeneralDao().saveOrUpdate(screenCheckOrganizationEn);

		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenRecieptFa = new ResponseScreen();
		screenRecieptFa.setScreenno("384");
		screenRecieptFa.setDesc("قبض-خطای رسید-فارسی");
		screenRecieptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenRecieptFa.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
		screenRecieptList.add(screenRecieptFa);
		getGeneralDao().saveOrUpdate(screenRecieptFa);
		
		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("784");
		screenRecieptEn.setDesc("قبض-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData("784[FF][SI]@@[ESC]P2542[ESC]\\");
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();

		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("قبض-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("قبض-Timeout-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		/*****************************/
		FunctionCommandResponse responseShetab = new FunctionCommandResponse();
		responseShetab.setName("تایید پرداخت قبض-شتابی-موفق");
		responseShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseShetab.setNextState("545");
		responseShetab.setScreen(screenCheckOrganizationList);
		getGeneralDao().saveOrUpdate(responseShetab);
		
		/*****************************/
		
		FunctionCommandResponse responsePasargad = new FunctionCommandResponse();
		responsePasargad.setName("تایید پرداخت قبض-داخلی-موفق");
		responsePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responsePasargad.setNextState("045");
		responsePasargad.setScreen(screenCheckOrganizationList);
		getGeneralDao().saveOrUpdate(responsePasargad);
		
		/*****************************/
		
		FunctionCommandResponse responseCreditPasargad = new FunctionCommandResponse();
		responseCreditPasargad.setName("تایید پرداخت قبض-اعتباری داخلی-موفق");
		responseCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCreditPasargad.setNextState("135");
		responseCreditPasargad.setScreen(screenCheckOrganizationList);
		getGeneralDao().saveOrUpdate(responseCreditPasargad);
		
		/*****************************/
		/*****************************/
		
		FunctionCommandResponse responseBillShetab = new FunctionCommandResponse();
		responseBillShetab.setName("پرداخت قبض-شتابی-موفق");
		responseBillShetab.setBeRetain(false);
		responseBillShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseBillShetab.setNextState("596");
		responseBillShetab.setScreen(screenBillPaymentList);
		responseBillShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseBillShetab);
		
		FunctionCommandResponse responseReceiptExceptionShetab = new FunctionCommandResponse();
		responseReceiptExceptionShetab.setName("پرداخت قبض-شتابی-خطای رسید");
		responseReceiptExceptionShetab.setBeRetain(false);
		responseReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionShetab.setNextState("592");
		responseReceiptExceptionShetab.setScreen(screenRecieptList);
		responseReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionShetab);
		
		/*****************************/
		
		FunctionCommandResponse responseBillPasargad = new FunctionCommandResponse();
		responseBillPasargad.setName("پرداخت قبض-داخلی-موفق");
		responseBillPasargad.setBeRetain(false);
		responseBillPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseBillPasargad.setNextState("096");
		responseBillPasargad.setScreen(screenBillPaymentList);
		responseBillPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseBillPasargad);
		
		FunctionCommandResponse responseReceiptExceptionPasargad = new FunctionCommandResponse();
		responseReceiptExceptionPasargad.setName("پرداخت قبض-داخلی-خطای رسید");
		responseReceiptExceptionPasargad.setBeRetain(false);
		responseReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionPasargad.setNextState("092");
		responseReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionPasargad);
		
		/*****************************/
		
		FunctionCommandResponse responseBillCreditPasargad = new FunctionCommandResponse();
		responseBillCreditPasargad.setName("پرداخت قبض-اعتباری داخلی-موفق");
		responseBillCreditPasargad.setBeRetain(false);
		responseBillCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseBillCreditPasargad.setNextState("196");
		responseBillCreditPasargad.setScreen(screenBillPaymentList);
		responseBillCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseBillCreditPasargad);
		
		FunctionCommandResponse responseReceiptExceptionCreditPasargad = new FunctionCommandResponse();
		responseReceiptExceptionCreditPasargad.setName("پرداخت قبض-اعتباری داخلی-خطای رسید");
		responseReceiptExceptionCreditPasargad.setBeRetain(false);
		responseReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionCreditPasargad.setNextState("192");
		responseReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
		responseReceiptExceptionCreditPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionCreditPasargad);
		
		/*****************************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("پرداخت قبض-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		/*****************************/
		
		atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
		atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);
		atmRequestReceiptOptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);//Task019
		
		atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
		atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);
		atmRequestReceiptOptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);//Task019
		
		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);//Task019
		
		atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019
		
		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);//Task019
		
		atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestReceiptOptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);//Task019
		
		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		atmRequestReceiptOptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);//Task019
		
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		atmRequestReceiptOptionCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);//Task019
		
		/*****************************/
		
		atmRequestReceiptExceptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
		atmRequestReceiptExceptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
		atmRequestReceiptExceptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
		atmRequestReceiptExceptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
		atmRequestReceiptExceptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
		atmRequestReceiptExceptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);//me
		atmRequestReceiptExceptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);
		atmRequestReceiptExceptionCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);
		
		/*****************************/
		
		atmRequestPreBillShetabEn.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseShetab);
		atmRequestTimeOutPreBillShetabEn.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseTimeOut);
		atmRequestPreBillShetabFa.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseShetab);
		atmRequestTimeOutPreBillShetabFa.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseTimeOut);
		atmRequestPreBillPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responsePasargad);
		atmRequestTimeOutPreBillPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseTimeOut);
		atmRequestPreBillPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responsePasargad);
		atmRequestTimeOutPreBillPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseTimeOut);
		atmRequestPreBillCreditPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseCreditPasargad);
		atmRequestTimeOutPreBillCreditPasargadFa.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseTimeOut);
		atmRequestPreBillCreditPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseCreditPasargad);
		atmRequestTimeOutPreBillCreditPasargadEn.addAtmResponse(ATMErrorCodes.PREPARE_BILL_PMT, responseTimeOut);
		
		/*****************************/

		getGeneralDao().saveOrUpdate(atmRequestShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabFa);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionShetabEn);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadFa);//Task019
		
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubFa);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadEn);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadSubEn);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadFa);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadEn);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadSubEn);//me
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestPreBillShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestPreBillShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestPreBillPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestPreBillPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestPreBillCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestPreBillCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreBillShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreBillShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreBillPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreBillPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreBillCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPreBillCreditPasargadEn);
		
		

		requests.add(atmRequestShetabFa);
		requests.add(atmRequestTimeOutShetabFa);
		requests.add(atmRequestReceiptOptionShetabFa);//Task019

		
		requests.add(atmRequestShetabEn);
		requests.add(atmRequestTimeOutShetabEn);
		requests.add(atmRequestReceiptOptionShetabEn);//Task019
		
		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestTimeOutPasargadFa);
		requests.add(atmRequestReceiptOptionPasargadFa);//Task019
		
		requests.add(atmRequestPasargadSubFa);
		requests.add(atmRequestTimeOutPasargadSubFa);
		requests.add(atmRequestReceiptOptionPasargadSubFa);//Task019
		
		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestTimeOutPasargadEn);
		requests.add(atmRequestReceiptOptionPasargadEn);//Task019
		
		requests.add(atmRequestPasargadSubEn);
		requests.add(atmRequestTimeOutPasargadSubEn);
		requests.add(atmRequestReceiptOptionPasargadSubEn);//Task019
		
		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadFa);
		requests.add(atmRequestReceiptOptionCreditPasargadFa);//Task019
		
		requests.add(atmRequestCreditPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		requests.add(atmRequestReceiptOptionCreditPasargadEn);//Task019
		
		requests.add(atmRequestReceiptExceptionShetabFa);
		requests.add(atmRequestReceiptExceptionShetabEn);
		requests.add(atmRequestReceiptExceptionPasargadFa);
		requests.add(atmRequestReceiptExceptionPasargadSubFa);
		requests.add(atmRequestReceiptExceptionPasargadEn);
		requests.add(atmRequestReceiptExceptionPasargadSubEn);
		requests.add(atmRequestReceiptExceptionCreditPasargadFa);
		requests.add(atmRequestReceiptExceptionCreditPasargadEn);
		
		requests.add(atmRequestPreBillShetabFa);
		requests.add(atmRequestPreBillShetabEn);
		requests.add(atmRequestPreBillPasargadFa);
		requests.add(atmRequestPreBillPasargadEn);
		requests.add(atmRequestPreBillCreditPasargadFa);
		requests.add(atmRequestPreBillCreditPasargadEn);
		
		requests.add(atmRequestTimeOutPreBillShetabFa);
		requests.add(atmRequestTimeOutPreBillShetabEn);
		requests.add(atmRequestTimeOutPreBillPasargadFa);
		requests.add(atmRequestTimeOutPreBillPasargadEn);
		requests.add(atmRequestTimeOutPreBillCreditPasargadFa);
		requests.add(atmRequestTimeOutPreBillCreditPasargadEn);
		
		return requests;
	}
	
	private List<ATMRequest> Transfer(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ATMRequest atmRequestCheckAcountShetabFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabEn = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestCheckAcountTimeOutShetabEn = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutShetabFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestTransferShetabFa = new ATMRequest();
		ATMRequest atmRequestTransferShetabEn = new ATMRequest();
		ATMRequest atmRequestTransferPasargadFa = new ATMRequest();
		ATMRequest atmRequestTransferPasargadEn = new ATMRequest();
		ATMRequest atmRequestTransferPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTransferPasargadSubEn = new ATMRequest();
		
		ATMRequest atmRequestTransferTimeOutShetabFa = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutShetabEn = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutPasargadSubEn = new ATMRequest();
		
		ATMRequest atmRequestTransferReceiptExceptionShetabFa = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionShetabEn = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionPasargadFa = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionPasargadEn = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionPasargadSubEn = new ATMRequest();
		
		atmRequestCheckAcountShetabFa.setOpkey("AAIA    ");
		atmRequestCheckAcountShetabFa.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountShetabFa.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountShetabFa.setCurrency(currency);
		atmRequestCheckAcountShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountShetabFa.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountTimeOutShetabFa.setOpkey("AAIA   F");
		atmRequestCheckAcountTimeOutShetabFa.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutShetabFa.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountTimeOutShetabFa.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutShetabFa.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountShetabEn.setOpkey("IAIA    ");
		atmRequestCheckAcountShetabEn.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountShetabEn.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountShetabEn.setCurrency(currency);
		atmRequestCheckAcountShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountShetabEn.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountTimeOutShetabEn.setOpkey("IAIA   F");
		atmRequestCheckAcountTimeOutShetabEn.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutShetabEn.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountTimeOutShetabEn.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutShetabEn.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountPasargadFa.setOpkey("ABIA    ");
		atmRequestCheckAcountPasargadFa.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountPasargadFa.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountPasargadFa.setCurrency(currency);
		atmRequestCheckAcountPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountTimeOutPasargadFa.setOpkey("ABIA   F");
		atmRequestCheckAcountTimeOutPasargadFa.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutPasargadFa.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountTimeOutPasargadFa.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountPasargadEn.setOpkey("IBIA    ");
		atmRequestCheckAcountPasargadEn.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountPasargadEn.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountPasargadEn.setCurrency(currency);
		atmRequestCheckAcountPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountTimeOutPasargadEn.setOpkey("IBIA   F");
		atmRequestCheckAcountTimeOutPasargadEn.setIfxType(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutPasargadEn.setTrnType(TrnType.CHECKACCOUNT);
		atmRequestCheckAcountTimeOutPasargadEn.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		
		String textFa = "[GR simpleTransferReceiptFa()]";
			
		
		String textEn =
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Fund Transfer Receipt')]" : "[LF][LF][GR center(GR c2NCRE('Fund Transfer Receipt'))]")
			+ newLine
			+ lineEn
			+ receivedDateEn
			+ newLine + newLine
			+ seqCntrEn
			+ newLine  
			+ amountEn
			+ newLine  //TASK Task029 : Print Bank name  
			+ transferBankNameEn //TASK Task029 : Print Bank name
			+ newLine  
			+ transferAppPanEn
			+ newLine  //TASK Task029 : Print Bank Name
			+ transferSecBankNameEn //TASK Task029 : Print Bank Name
			+ newLine  
			+ transferSecAppPanEn
			+ newLine  
			+ "Transfered to "
			+ openDoubleQuotationEn
			+ "[GR safeEn(ifx.CardHolderName)][SO]1[GR safeEn(ifx.CardHolderFamily)]"
			+ closeDoubleQuotationEn
			+ "[GR putLF(10)]" 
			+ footerEn;
		
		String textJournal00 = "[LF]Transfer:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";

		
		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
		
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList.add(receiptFa);
		
		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList.add(receiptEn);
		
		/************************/
		atmRequestTransferShetabFa.setOpkey("AAIB    ");
		atmRequestTransferShetabFa.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferShetabFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferShetabFa.setCurrency(currency);
		atmRequestTransferShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTransferTimeOutShetabFa.setOpkey("AAIB   F");
		atmRequestTransferTimeOutShetabFa.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutShetabFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferTimeOutShetabFa.setCurrency(currency);
		atmRequestTransferTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferTimeOutShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTransferReceiptExceptionShetabFa.setOpkey("AAIB   A");
		atmRequestTransferReceiptExceptionShetabFa.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferReceiptExceptionShetabFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferReceiptExceptionShetabFa.setCurrency(currency);
		atmRequestTransferReceiptExceptionShetabFa.setForceReceipt(false);
		atmRequestTransferReceiptExceptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferReceiptExceptionShetabFa.setFit(FITType.SHETAB);
		
		/************************/
		
		atmRequestTransferShetabEn.setOpkey("IAIB    ");
		atmRequestTransferShetabEn.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferShetabEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferShetabEn.setCurrency(currency);
		atmRequestTransferShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTransferTimeOutShetabEn.setOpkey("IAIB   F");
		atmRequestTransferTimeOutShetabEn.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutShetabEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferTimeOutShetabEn.setCurrency(currency);
		atmRequestTransferTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferTimeOutShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTransferReceiptExceptionShetabEn.setOpkey("IAIB   A");
		atmRequestTransferReceiptExceptionShetabEn.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferReceiptExceptionShetabEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferReceiptExceptionShetabEn.setCurrency(currency);
		atmRequestTransferReceiptExceptionShetabEn.setForceReceipt(false);
		atmRequestTransferReceiptExceptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferReceiptExceptionShetabEn.setFit(FITType.SHETAB);
		
		/************************/
		
		atmRequestTransferPasargadFa.setOpkey("ABIBA   ");
		atmRequestTransferPasargadFa.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferPasargadFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferPasargadFa.setCurrency(currency);
		atmRequestTransferPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadFa.setOpkey("ABIBA  F");
		atmRequestTransferTimeOutPasargadFa.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferTimeOutPasargadFa.setCurrency(currency);
		atmRequestTransferTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTransferReceiptExceptionPasargadFa.setOpkey("ABIBA  A");
		atmRequestTransferReceiptExceptionPasargadFa.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferReceiptExceptionPasargadFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferReceiptExceptionPasargadFa.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadFa.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferReceiptExceptionPasargadFa.setFit(FITType.PASARGAD);
		
		/************************/
		atmRequestTransferPasargadEn.setOpkey("IBIBA   ");
		atmRequestTransferPasargadEn.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferPasargadEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferPasargadEn.setCurrency(currency);
		atmRequestTransferPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadEn.setOpkey("IBIBA  F");
		atmRequestTransferTimeOutPasargadEn.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferTimeOutPasargadEn.setCurrency(currency);
		atmRequestTransferTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTransferReceiptExceptionPasargadEn.setOpkey("IBIBA  A");
		atmRequestTransferReceiptExceptionPasargadEn.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferReceiptExceptionPasargadEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferReceiptExceptionPasargadEn.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadEn.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferReceiptExceptionPasargadEn.setFit(FITType.PASARGAD);
		
		/************************/
		
		atmRequestTransferPasargadSubFa.setOpkey("ABIBB   ");
		atmRequestTransferPasargadSubFa.setNextOpkey("ABIBA   ");
		atmRequestTransferPasargadSubFa.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestTransferPasargadSubFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTransferPasargadSubFa.setCurrency(currency);
		atmRequestTransferPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferPasargadSubFa.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadSubFa.setOpkey("ABIBB  F");
		atmRequestTransferTimeOutPasargadSubFa.setNextOpkey("ABIBB  F");
		atmRequestTransferTimeOutPasargadSubFa.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTransferTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTransferTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTransferTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferTimeOutPasargadSubFa.setFit(FITType.PASARGAD);
		
		atmRequestTransferReceiptExceptionPasargadSubFa.setOpkey("ABIBB  A");
		atmRequestTransferReceiptExceptionPasargadSubFa.setNextOpkey("ABIBA  A");
		atmRequestTransferReceiptExceptionPasargadSubFa.setIfxType(IfxType.TRANSFER_RQ);
//		atmRequestTransferReceiptExceptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestTransferReceiptExceptionPasargadSubFa.setTrnType(TrnType.TRANSFER);
		atmRequestTransferReceiptExceptionPasargadSubFa.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadSubFa.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferReceiptExceptionPasargadSubFa.setFit(FITType.PASARGAD);
		
		/************************/
		
		atmRequestTransferPasargadSubEn.setOpkey("IBIBB   ");
		atmRequestTransferPasargadSubEn.setNextOpkey("IBIBA   ");
		atmRequestTransferPasargadSubEn.setIfxType(IfxType.TRANSFER_RQ);
		atmRequestTransferPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestTransferPasargadSubEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTransferPasargadSubEn.setCurrency(currency);
		atmRequestTransferPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferPasargadSubEn.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadSubEn.setOpkey("IBIBB  F");
		atmRequestTransferTimeOutPasargadSubEn.setNextOpkey("IBIBB  F");
		atmRequestTransferTimeOutPasargadSubEn.setIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTransferTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
		atmRequestTransferTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTransferTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferTimeOutPasargadSubEn.setFit(FITType.PASARGAD);
		
		atmRequestTransferReceiptExceptionPasargadSubEn.setOpkey("IBIBB  A");
		atmRequestTransferReceiptExceptionPasargadSubEn.setNextOpkey("IBIBA  A");
		atmRequestTransferReceiptExceptionPasargadSubEn.setIfxType(IfxType.TRANSFER_RQ);
//		atmRequestTransferReceiptExceptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestTransferReceiptExceptionPasargadSubEn.setTrnType(TrnType.TRANSFER);
		atmRequestTransferReceiptExceptionPasargadSubEn.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadSubEn.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferReceiptExceptionPasargadSubEn.setFit(FITType.PASARGAD);
		
		/************************/
		List<ResponseScreen> screenTransferList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenTransferFa = new ResponseScreen();
		screenTransferFa.setScreenno("388");
		screenTransferFa.setDesc("انتقال-موفق-فارسی");
		screenTransferFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTransferFa.setScreenData("388[SI]@@[ESC]P2080[ESC]\\");
		screenTransferList.add(screenTransferFa);
		getGeneralDao().saveOrUpdate(screenTransferFa);
		
		ResponseScreen screenTransferEn = new ResponseScreen();
		screenTransferEn.setScreenno("788");
		screenTransferEn.setDesc("انتقال-موفق-انگلیسی");
		screenTransferEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTransferEn.setScreenData("788[SI]@@[ESC]P2480[ESC]\\");
		screenTransferList.add(screenTransferEn);
		getGeneralDao().saveOrUpdate(screenTransferEn);
		
		List<ResponseScreen> screenCheckAccountList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCheckAccountFa = new ResponseScreen();
		screenCheckAccountFa.setScreenno("033");
		screenCheckAccountFa.setDesc("بررسی حساب انتقال-موفق-فارسی");
		screenCheckAccountFa.setLanguage(UserLanguage.FARSI_LANG);
		if (!atmType.equals(ATMType.NCR)) {
			screenCheckAccountFa.setScreenData("033[ESC]P2084[ESC]\\" 
					+ "[ESC](K" 
					+ "[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]G0[GR c2F(ifx.CardHolderFamily)][GR c2F(ifx.CardHolderName)]"
					+ "[SI]H0[GR ifx.actualAppPAN]" 
					+ "[SI]I0[GR ifx.Auth_Amt]");
		} else {
			screenCheckAccountFa.setScreenData("033[ESC]P2084[ESC]\\" 
					+ "[ESC](6" 
					+ "[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]G0[GR c2F(ifx.CardHolderFamily)][GR c2F(ifx.CardHolderName)]"
					+ "[SI]H0[GR ifx.actualAppPAN]" 
					+ "[SI]I0[GR ifx.Auth_Amt]");
		}
		screenCheckAccountList.add(screenCheckAccountFa);
		getGeneralDao().saveOrUpdate(screenCheckAccountFa);
		
		ResponseScreen screenCheckAccountEn = new ResponseScreen();
		screenCheckAccountEn.setScreenno("433");
		screenCheckAccountEn.setDesc("بررسی حساب انتقال-موفق-انگلیسی");
		screenCheckAccountEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCheckAccountEn.setScreenData("433[ESC]P2484[ESC]\\" 
				+ "[ESC](1" 
				+ "[ESC][OC]"
				+ updateablePageFontColor
				+ "[SI]G0[GR safeEn(ifx.CardHolderFamily)][GR safeEn(ifx.CardHolderName)]"
				+ "[SI]H0[GR ifx.actualAppPAN]" 
				+ "[SI]I0[GR ifx.Auth_Amt]");
		screenCheckAccountList.add(screenCheckAccountEn);
		getGeneralDao().saveOrUpdate(screenCheckAccountEn);

		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenRecieptFa = new ResponseScreen();
		screenRecieptFa.setScreenno("384");
		screenRecieptFa.setDesc("انتقال-داخلی-خطای رسید-فارسی");
		screenRecieptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenRecieptFa.setScreenData(null);
		screenRecieptList.add(screenRecieptFa);
		getGeneralDao().saveOrUpdate(screenRecieptFa);
		
		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("784");
		screenRecieptEn.setDesc("انتقال-داخلی-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData(null);
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		/************************/
		
		FunctionCommandResponse responseCheckAccountShetab = new FunctionCommandResponse();
		responseCheckAccountShetab.setName("بررسی حساب انتقال-شتابی-موفق");
		responseCheckAccountShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountShetab.setNextState("551");
		responseCheckAccountShetab.setScreen(screenCheckAccountList);
		getGeneralDao().saveOrUpdate(responseCheckAccountShetab);
		
		FunctionCommandResponse responseCheckAccountPasargad = new FunctionCommandResponse();
		responseCheckAccountPasargad.setName("بررسی حساب انتقال-داخلی-موفق");
		responseCheckAccountPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountPasargad.setNextState("051");
		responseCheckAccountPasargad.setScreen(screenCheckAccountList);
		getGeneralDao().saveOrUpdate(responseCheckAccountPasargad);
		
		/************************/
		FunctionCommandResponse responseTransferShetab = new FunctionCommandResponse();
		responseTransferShetab.setName("انتقال-شتابی-موفق");
		responseTransferShetab.setBeRetain(false);
		responseTransferShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferShetab.setNextState("596");
		responseTransferShetab.setScreen(screenTransferList);
		responseTransferShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseTransferShetab);
		
		FunctionCommandResponse responseTransferReceiptExceptionShetab = new FunctionCommandResponse();
		responseTransferReceiptExceptionShetab.setName("انتقال-شتابی-خطای رسید");
		responseTransferReceiptExceptionShetab.setBeRetain(false);
		responseTransferReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferReceiptExceptionShetab.setNextState("592");
		responseTransferReceiptExceptionShetab.setScreen(screenRecieptList);
		responseTransferReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTransferReceiptExceptionShetab);
		
		/************************/
		
		FunctionCommandResponse responseTransferPasargad = new FunctionCommandResponse();
		responseTransferPasargad.setName("انتقال-داخلی-موفق");
		responseTransferPasargad.setBeRetain(false);
		responseTransferPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferPasargad.setNextState("096");
		responseTransferPasargad.setScreen(screenTransferList);
		responseTransferPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseTransferPasargad);
		
		FunctionCommandResponse responseTransferReceiptExceptionPasargad = new FunctionCommandResponse();
		responseTransferReceiptExceptionPasargad.setName("انتقال-داخلی-خطای رسید");
		responseTransferReceiptExceptionPasargad.setBeRetain(false);
		responseTransferReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferReceiptExceptionPasargad.setNextState("092");
		responseTransferReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseTransferReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTransferReceiptExceptionPasargad);
		
		/************************/
		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("بررسی حساب انتقال-time out-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("بررسی حساب انتقال-time out-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("انتقال-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		
		/************************/
		/************************/
		FunctionCommandResponse responseCheckAccountTimeOut = new FunctionCommandResponse();
		responseCheckAccountTimeOut.setName("بررسی حساب انتقال-time out");
		responseCheckAccountTimeOut.setBeRetain(false);
		responseCheckAccountTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountTimeOut.setNextState("713");
		responseCheckAccountTimeOut.setScreen(screenTimeoutList);
		responseCheckAccountTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountTimeOut);
		
		/************************/
		
		atmRequestCheckAcountShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountShetab);
		atmRequestCheckAcountTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountShetab);
		atmRequestCheckAcountTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountPasargad);
		atmRequestCheckAcountTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountPasargad);
		atmRequestCheckAcountTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		
		atmRequestTransferShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		atmRequestTransferShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionShetab);
		atmRequestTransferTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		
		atmRequestTransferShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		atmRequestTransferShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionShetab);
		atmRequestTransferTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		
		atmRequestTransferPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		atmRequestTransferPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		atmRequestTransferTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		
		atmRequestTransferPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTransferTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		/**********CHECK THIS****************/
		atmRequestTransferReceiptExceptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		
		atmRequestTransferPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTransferTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		/**********CHECK THIS****************/
		atmRequestTransferReceiptExceptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);

		
		atmRequestTransferPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		atmRequestTransferPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		atmRequestTransferTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		
		/************************/
		
		getGeneralDao().saveOrUpdate(configuration);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTransferShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadSubEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadSubEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionShetabEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadSubEn);
		
		requests.add(atmRequestCheckAcountShetabFa);
		requests.add(atmRequestCheckAcountShetabEn);
		requests.add(atmRequestCheckAcountPasargadFa);
		requests.add(atmRequestCheckAcountPasargadEn);
		
		requests.add(atmRequestCheckAcountTimeOutShetabFa);
		requests.add(atmRequestCheckAcountTimeOutShetabEn);
		requests.add(atmRequestCheckAcountTimeOutPasargadFa);
		requests.add(atmRequestCheckAcountTimeOutPasargadEn);
		
		requests.add(atmRequestTransferShetabFa);
		requests.add(atmRequestTransferShetabEn);
		requests.add(atmRequestTransferPasargadFa);
		requests.add(atmRequestTransferPasargadEn);
		requests.add(atmRequestTransferPasargadSubFa);
		requests.add(atmRequestTransferPasargadSubEn);
		
		requests.add(atmRequestTransferTimeOutShetabFa);
		requests.add(atmRequestTransferTimeOutShetabEn);
		requests.add(atmRequestTransferTimeOutPasargadFa);
		requests.add(atmRequestTransferTimeOutPasargadEn);
		requests.add(atmRequestTransferTimeOutPasargadSubFa);
		requests.add(atmRequestTransferTimeOutPasargadSubEn);
		
		requests.add(atmRequestTransferReceiptExceptionShetabFa);
		requests.add(atmRequestTransferReceiptExceptionShetabEn);
		requests.add(atmRequestTransferReceiptExceptionPasargadFa);
		requests.add(atmRequestTransferReceiptExceptionPasargadEn);
		requests.add(atmRequestTransferReceiptExceptionPasargadSubFa);
		requests.add(atmRequestTransferReceiptExceptionPasargadSubEn);
		
		return requests;
	}
	
	//TASK Task002 : Transfer Card To Account
	private List<ATMRequest> TransferToAccount(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {

		
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		//--------------------------Pasargad --------------------------------------------
		ATMRequest atmRequestCheckAcountPasargadPart1Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadPart1En = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadPart2Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadPart2En = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadPart3Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadPart3En = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadLastPartFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountPasargadLastPartEn = new ATMRequest();
		
		ATMRequest atmRequestCheckAcountTimeOutPasargadPart1Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutPasargadPart1En = new ATMRequest();
		
		ATMRequest atmRequestCheckAcountTimeOutPasargadPart2Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutPasargadPart2En = new ATMRequest();	

		ATMRequest atmRequestCheckAcountTimeOutPasargadPart3Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutPasargadPart3En = new ATMRequest();	
		
		ATMRequest atmRequestCheckAcountTimeOutPasargadLastPartFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutPasargadLastPartEn = new ATMRequest();
		//----------------------------------------------------------------------------------
		//--------------------------Shetab --------------------------------------------
		ATMRequest atmRequestCheckAcountShetabPart1Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabPart1En = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabPart2Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabPart2En = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabPart3Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabPart3En = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabLastPartFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountShetabLastPartEn = new ATMRequest();
		
		ATMRequest atmRequestCheckAcountTimeOutShetabPart1Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutShetabPart1En = new ATMRequest();
		
		ATMRequest atmRequestCheckAcountTimeOutShetabPart2Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutShetabPart2En = new ATMRequest();	

		ATMRequest atmRequestCheckAcountTimeOutShetabPart3Fa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutShetabPart3En = new ATMRequest();	
		
		ATMRequest atmRequestCheckAcountTimeOutShetabLastPartFa = new ATMRequest();
		ATMRequest atmRequestCheckAcountTimeOutShetabLastPartEn = new ATMRequest();
		//----------------------------------------------------------------------------------
		

		//---------------------- Pasargad ----------------------------
		ATMRequest atmRequestTransferPasargadFa = new ATMRequest();
		ATMRequest atmRequestTransferPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTransferPasargadEn = new ATMRequest();
		ATMRequest atmRequestTransferPasargadSubEn = new ATMRequest();//me
		
		ATMRequest atmRequestTransferTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutPasargadSubEn = new ATMRequest(); //me
		ATMRequest atmRequestTransferReceiptExceptionPasargadFa = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionPasargadSubFa = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionPasargadEn = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionPasargadSubEn = new ATMRequest();//me
		//----------------------------------------------------------------------------------
		
		//---------------------- Shetab ----------------------------
		ATMRequest atmRequestTransferShetabFa = new ATMRequest();
		ATMRequest atmRequestTransferShetabEn = new ATMRequest();
		
		ATMRequest atmRequestTransferTimeOutShetabFa = new ATMRequest();
		ATMRequest atmRequestTransferTimeOutShetabEn = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionShetabFa = new ATMRequest();
		ATMRequest atmRequestTransferReceiptExceptionShetabEn = new ATMRequest();
		//----------------------------------------------------------------------------------
		
		
		//------------------------------- Pasargad -----------------------
		atmRequestCheckAcountPasargadPart1Fa.setOpkey("ABICA   ");
		atmRequestCheckAcountPasargadPart1Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP);
		atmRequestCheckAcountPasargadPart1Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart1Fa.setCurrency(currency);
		atmRequestCheckAcountPasargadPart1Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountPasargadPart1Fa.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountPasargadPart2Fa.setOpkey("ABICB   ");
		atmRequestCheckAcountPasargadPart2Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart2Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart2Fa.setCurrency(currency);
		atmRequestCheckAcountPasargadPart2Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountPasargadPart2Fa.setFit(FITType.PASARGAD);	
		
		atmRequestCheckAcountPasargadPart3Fa.setOpkey("ABICC   ");
		atmRequestCheckAcountPasargadPart3Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart3Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart3Fa.setCurrency(currency);
		atmRequestCheckAcountPasargadPart3Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountPasargadPart3Fa.setFit(FITType.PASARGAD);			
		
		atmRequestCheckAcountPasargadLastPartFa.setOpkey("ABIC    ");
		atmRequestCheckAcountPasargadLastPartFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountPasargadLastPartFa.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadLastPartFa.setCurrency(currency);
		atmRequestCheckAcountPasargadLastPartFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountPasargadLastPartFa.setFit(FITType.PASARGAD);

		atmRequestCheckAcountTimeOutPasargadPart1Fa.setOpkey("ABICA  F");
		atmRequestCheckAcountTimeOutPasargadPart1Fa.setNextOpkey("ABICA  F");
		atmRequestCheckAcountTimeOutPasargadPart1Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT);
		atmRequestCheckAcountTimeOutPasargadPart1Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadPart1Fa.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadPart1Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutPasargadPart1Fa.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountTimeOutPasargadPart2Fa.setOpkey("ABICB  F");
		atmRequestCheckAcountTimeOutPasargadPart2Fa.setNextOpkey("ABICB  F");
		atmRequestCheckAcountTimeOutPasargadPart2Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutPasargadPart2Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadPart2Fa.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadPart2Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutPasargadPart2Fa.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountTimeOutPasargadPart3Fa.setOpkey("ABICC  F");
		atmRequestCheckAcountTimeOutPasargadPart3Fa.setNextOpkey("ABICC  F");
		atmRequestCheckAcountTimeOutPasargadPart3Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutPasargadPart3Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadPart3Fa.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadPart3Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutPasargadPart3Fa.setFit(FITType.PASARGAD);		
		
		atmRequestCheckAcountTimeOutPasargadLastPartFa.setOpkey("ABIC   F");
		atmRequestCheckAcountTimeOutPasargadLastPartFa.setNextOpkey("ABIC   F");
		atmRequestCheckAcountTimeOutPasargadLastPartFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutPasargadLastPartFa.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadLastPartFa.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadLastPartFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutPasargadLastPartFa.setFit(FITType.PASARGAD);

		atmRequestCheckAcountPasargadPart1En.setOpkey("IBICA   ");
		atmRequestCheckAcountPasargadPart1En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP);
		atmRequestCheckAcountPasargadPart1En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart1En.setCurrency(currency);
		atmRequestCheckAcountPasargadPart1En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountPasargadPart1En.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountPasargadPart2En.setOpkey("IBICB   ");
		atmRequestCheckAcountPasargadPart2En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart2En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart2En.setCurrency(currency);
		atmRequestCheckAcountPasargadPart2En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountPasargadPart2En.setFit(FITType.PASARGAD);		
		
		atmRequestCheckAcountPasargadPart3En.setOpkey("IBICC   ");
		atmRequestCheckAcountPasargadPart3En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart3En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadPart3En.setCurrency(currency);
		atmRequestCheckAcountPasargadPart3En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountPasargadPart3En.setFit(FITType.PASARGAD);			

		atmRequestCheckAcountPasargadLastPartEn.setOpkey("IBIC    ");
		atmRequestCheckAcountPasargadLastPartEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountPasargadLastPartEn.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountPasargadLastPartEn.setCurrency(currency);
		atmRequestCheckAcountPasargadLastPartEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountPasargadLastPartEn.setFit(FITType.PASARGAD);

		atmRequestCheckAcountTimeOutPasargadPart1En.setOpkey("IBICA  F");
		atmRequestCheckAcountTimeOutPasargadPart1En.setNextOpkey("IBICA  F");
		atmRequestCheckAcountTimeOutPasargadPart1En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT);
		atmRequestCheckAcountTimeOutPasargadPart1En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadPart1En.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadPart1En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutPasargadPart1En.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountTimeOutPasargadPart2En.setOpkey("IBICB  F");
		atmRequestCheckAcountTimeOutPasargadPart2En.setNextOpkey("IBICB  F");
		atmRequestCheckAcountTimeOutPasargadPart2En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutPasargadPart2En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadPart2En.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadPart2En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutPasargadPart2En.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountTimeOutPasargadPart3En.setOpkey("IBICA  F");//Ehtemalan IBICC F
		atmRequestCheckAcountTimeOutPasargadPart3En.setNextOpkey("IBICA  F");
		atmRequestCheckAcountTimeOutPasargadPart3En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutPasargadPart3En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadPart3En.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadPart3En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutPasargadPart3En.setFit(FITType.PASARGAD);
		
		atmRequestCheckAcountTimeOutPasargadLastPartEn.setOpkey("IBIC   F");
		atmRequestCheckAcountTimeOutPasargadLastPartEn.setNextOpkey("IBIC   F");
		atmRequestCheckAcountTimeOutPasargadLastPartEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutPasargadLastPartEn.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutPasargadLastPartEn.setCurrency(currency);
		atmRequestCheckAcountTimeOutPasargadLastPartEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutPasargadLastPartEn.setFit(FITType.PASARGAD);
		
		//-------------------------------------------------------------------
		
		//------------------------------- Shetab -----------------------
		atmRequestCheckAcountShetabPart1Fa.setOpkey("AAICA   ");
		atmRequestCheckAcountShetabPart1Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP);
		atmRequestCheckAcountShetabPart1Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart1Fa.setCurrency(currency);
		atmRequestCheckAcountShetabPart1Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountShetabPart1Fa.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountShetabPart2Fa.setOpkey("AAICB   ");
		atmRequestCheckAcountShetabPart2Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart2Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart2Fa.setCurrency(currency);
		atmRequestCheckAcountShetabPart2Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountShetabPart2Fa.setFit(FITType.SHETAB);	
		
		atmRequestCheckAcountShetabPart3Fa.setOpkey("AAICC   ");
		atmRequestCheckAcountShetabPart3Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart3Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart3Fa.setCurrency(currency);
		atmRequestCheckAcountShetabPart3Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountShetabPart3Fa.setFit(FITType.SHETAB);			
		
		atmRequestCheckAcountShetabLastPartFa.setOpkey("AAIC    ");
		atmRequestCheckAcountShetabLastPartFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountShetabLastPartFa.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabLastPartFa.setCurrency(currency);
		atmRequestCheckAcountShetabLastPartFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountShetabLastPartFa.setFit(FITType.SHETAB);

		atmRequestCheckAcountTimeOutShetabPart1Fa.setOpkey("AAICA  F");
		atmRequestCheckAcountTimeOutShetabPart1Fa.setNextOpkey("AAICA  F");
		atmRequestCheckAcountTimeOutShetabPart1Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT);
		atmRequestCheckAcountTimeOutShetabPart1Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabPart1Fa.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabPart1Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutShetabPart1Fa.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountTimeOutShetabPart2Fa.setOpkey("AAICB  F");
		atmRequestCheckAcountTimeOutShetabPart2Fa.setNextOpkey("AAICB  F");
		atmRequestCheckAcountTimeOutShetabPart2Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutShetabPart2Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabPart2Fa.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabPart2Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutShetabPart2Fa.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountTimeOutShetabPart3Fa.setOpkey("AAICC  F");
		atmRequestCheckAcountTimeOutShetabPart3Fa.setNextOpkey("AAICC  F");
		atmRequestCheckAcountTimeOutShetabPart3Fa.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutShetabPart3Fa.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabPart3Fa.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabPart3Fa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutShetabPart3Fa.setFit(FITType.SHETAB);		
		
		atmRequestCheckAcountTimeOutShetabLastPartFa.setOpkey("AAIC   F");
		atmRequestCheckAcountTimeOutShetabLastPartFa.setNextOpkey("AAIC   F");
		atmRequestCheckAcountTimeOutShetabLastPartFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutShetabLastPartFa.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabLastPartFa.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabLastPartFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCheckAcountTimeOutShetabLastPartFa.setFit(FITType.SHETAB);

		atmRequestCheckAcountShetabPart1En.setOpkey("IAICA   ");
		atmRequestCheckAcountShetabPart1En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP);
		atmRequestCheckAcountShetabPart1En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart1En.setCurrency(currency);
		atmRequestCheckAcountShetabPart1En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountShetabPart1En.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountShetabPart2En.setOpkey("IAICB   ");
		atmRequestCheckAcountShetabPart2En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart2En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart2En.setCurrency(currency);
		atmRequestCheckAcountShetabPart2En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountShetabPart2En.setFit(FITType.SHETAB);		
		
		atmRequestCheckAcountShetabPart3En.setOpkey("IAICC   ");
		atmRequestCheckAcountShetabPart3En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart3En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabPart3En.setCurrency(currency);
		atmRequestCheckAcountShetabPart3En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountShetabPart3En.setFit(FITType.SHETAB);			

		atmRequestCheckAcountShetabLastPartEn.setOpkey("IAIC    ");
		atmRequestCheckAcountShetabLastPartEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ);
		atmRequestCheckAcountShetabLastPartEn.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountShetabLastPartEn.setCurrency(currency);
		atmRequestCheckAcountShetabLastPartEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountShetabLastPartEn.setFit(FITType.SHETAB);

		atmRequestCheckAcountTimeOutShetabPart1En.setOpkey("IAICA  F");
		atmRequestCheckAcountTimeOutShetabPart1En.setNextOpkey("IAICA  F");
		atmRequestCheckAcountTimeOutShetabPart1En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT);
		atmRequestCheckAcountTimeOutShetabPart1En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabPart1En.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabPart1En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutShetabPart1En.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountTimeOutShetabPart2En.setOpkey("IAICB  F");
		atmRequestCheckAcountTimeOutShetabPart2En.setNextOpkey("IAICB  F");
		atmRequestCheckAcountTimeOutShetabPart2En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutShetabPart2En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabPart2En.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabPart2En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutShetabPart2En.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountTimeOutShetabPart3En.setOpkey("IAICA  F");
		atmRequestCheckAcountTimeOutShetabPart3En.setNextOpkey("IAICA  F");
		atmRequestCheckAcountTimeOutShetabPart3En.setIfxType(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
		atmRequestCheckAcountTimeOutShetabPart3En.setTrnType(TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabPart3En.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabPart3En.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutShetabPart3En.setFit(FITType.SHETAB);
		
		atmRequestCheckAcountTimeOutShetabLastPartEn.setOpkey("IAIC   F");
		atmRequestCheckAcountTimeOutShetabLastPartEn.setNextOpkey("IAIC   F");
		atmRequestCheckAcountTimeOutShetabLastPartEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ);
		atmRequestCheckAcountTimeOutShetabLastPartEn.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		atmRequestCheckAcountTimeOutShetabLastPartEn.setCurrency(currency);
		atmRequestCheckAcountTimeOutShetabLastPartEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCheckAcountTimeOutShetabLastPartEn.setFit(FITType.SHETAB);
		
		//-------------------------------------------------------------------
		
		String textFa = "[GR simpleTransferToAccountReceiptFa()]";
	
		String textEn =
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Fund Transfer To Account Receipt')]" : "[LF][LF][GR center(GR c2NCRE('Fund Transfer To Account Receipt'))]")
			+ newLine
			+ lineEn
			+ receivedDateEn
			+ newLine + newLine
			+ seqCntrEn
			+ newLine  
			+ amountEn
			+ newLine  //TASK Task029 : Print Bank name  
			+ transferBankNameEn //TASK Task029 : Print Bank name
			+ newLine  
			+ transferAppPanEn
			+ newLine  //TASK Task029 : Print Bank name  
			+ transferSecBankNameEn //TASK Task029 : Print Bank name
			+ newLine  
			+ transferSecAccountEn
			+ newLine  
			+ "Transfered to "
			+ openDoubleQuotationEn
			+ "[GR safeEn(ifx.CardHolderName)][SO]1[GR safeEn(ifx.CardHolderFamily)]"
			+ closeDoubleQuotationEn
			+ "[GR putLF(10)]" 
			+ footerEn;
		
		String textJournal00 = "[LF]Transfer To Account:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";

		
		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
		
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList.add(receiptFa);
		
		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList.add(receiptEn);
		
		/************************/
		//-------------------------------- Pasargad --------------------------------------
		atmRequestTransferPasargadFa.setOpkey("ABIDA   ");
		atmRequestTransferPasargadFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferPasargadFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferPasargadFa.setCurrency(currency);
		atmRequestTransferPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadFa.setOpkey("ABIDA  F");
		atmRequestTransferTimeOutPasargadFa.setNextOpkey("ABIDA  F");
		atmRequestTransferTimeOutPasargadFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferTimeOutPasargadFa.setCurrency(currency);
		atmRequestTransferTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTransferReceiptExceptionPasargadFa.setOpkey("ABIDA  A");
		atmRequestTransferReceiptExceptionPasargadFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferReceiptExceptionPasargadFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferReceiptExceptionPasargadFa.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadFa.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferReceiptExceptionPasargadFa.setFit(FITType.PASARGAD);
//		
//		/************************/
		
		atmRequestTransferPasargadSubFa.setOpkey("ABIDB   ");
		atmRequestTransferPasargadSubFa.setNextOpkey("ABIDA   ");//AldTODO Task002 : Please check this!
		atmRequestTransferPasargadSubFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestTransferPasargadSubFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT); 
		atmRequestTransferPasargadSubFa.setCurrency(currency);
		atmRequestTransferPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferPasargadSubFa.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadSubFa.setOpkey("ABIDB  F");
		atmRequestTransferTimeOutPasargadSubFa.setNextOpkey("ABIDB  F");//AldTODO Task002 : Please check this! the previous is "ABIBB  F"
		atmRequestTransferTimeOutPasargadSubFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT); 
		atmRequestTransferTimeOutPasargadSubFa.setCurrency(currency);
		atmRequestTransferTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferTimeOutPasargadSubFa.setFit(FITType.PASARGAD);
	
		
		atmRequestTransferReceiptExceptionPasargadSubFa.setOpkey("ABIDB  A");
		atmRequestTransferReceiptExceptionPasargadSubFa.setNextOpkey("ABIDA  A");
		atmRequestTransferReceiptExceptionPasargadSubFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferReceiptExceptionPasargadSubFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferReceiptExceptionPasargadSubFa.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadSubFa.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferReceiptExceptionPasargadSubFa.setFit(FITType.PASARGAD);

		/************************/
		
		atmRequestTransferPasargadEn.setOpkey("IBIDA   ");
		atmRequestTransferPasargadEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferPasargadEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferPasargadEn.setCurrency(currency);
		atmRequestTransferPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadEn.setOpkey("IBIDA  F");
		atmRequestTransferTimeOutPasargadEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferTimeOutPasargadEn.setCurrency(currency);
		atmRequestTransferTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTransferReceiptExceptionPasargadEn.setOpkey("IBIDA  A");
		atmRequestTransferReceiptExceptionPasargadEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferReceiptExceptionPasargadEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferReceiptExceptionPasargadEn.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadEn.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferReceiptExceptionPasargadEn.setFit(FITType.PASARGAD);
		
		/************************/
		//*****me
		atmRequestTransferPasargadSubEn.setOpkey("IBIDB   ");
		atmRequestTransferPasargadSubEn.setNextOpkey("IBIDA   ");//AldTODO Task002 : Please Check This i Change IBIBA -> IBIDA
		atmRequestTransferPasargadSubEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRequestTransferPasargadSubEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT); 
		atmRequestTransferPasargadSubEn.setCurrency(currency);
		atmRequestTransferPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferPasargadSubEn.setFit(FITType.PASARGAD);
		
		atmRequestTransferTimeOutPasargadSubEn.setOpkey("IBIDB  F");
		atmRequestTransferTimeOutPasargadSubEn.setNextOpkey("IBIDB  F");//AldTODO Task002 : Please Check This i Change IBIBB  F -> IBIDA  F
		atmRequestTransferTimeOutPasargadSubEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//		atmRequestTransferTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
		atmRequestTransferTimeOutPasargadSubEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT); 
		atmRequestTransferTimeOutPasargadSubEn.setCurrency(currency);
		atmRequestTransferTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferTimeOutPasargadSubEn.setFit(FITType.PASARGAD);
		
		
		atmRequestTransferReceiptExceptionPasargadSubEn.setOpkey("IBIDB  A");
		atmRequestTransferReceiptExceptionPasargadSubEn.setNextOpkey("IBIDA  A");
		atmRequestTransferReceiptExceptionPasargadSubEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
//		atmRequestTransferReceiptExceptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);//TODO AD TransferToAccount ?
		atmRequestTransferReceiptExceptionPasargadSubEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferReceiptExceptionPasargadSubEn.setCurrency(currency);
		atmRequestTransferReceiptExceptionPasargadSubEn.setForceReceipt(false);
		atmRequestTransferReceiptExceptionPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferReceiptExceptionPasargadSubEn.setFit(FITType.PASARGAD);
		
		
		
		//-------------------------------- Shetab --------------------------------------
		atmRequestTransferShetabFa.setOpkey("AAIDA   ");
		atmRequestTransferShetabFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferShetabFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferShetabFa.setCurrency(currency);
		atmRequestTransferShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTransferTimeOutShetabFa.setOpkey("AAIDA  F");
		atmRequestTransferTimeOutShetabFa.setNextOpkey("AAIDA  F");
		atmRequestTransferTimeOutShetabFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTransferTimeOutShetabFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferTimeOutShetabFa.setCurrency(currency);
		atmRequestTransferTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferTimeOutShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTransferReceiptExceptionShetabFa.setOpkey("AAIDA  A");
		atmRequestTransferReceiptExceptionShetabFa.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferReceiptExceptionShetabFa.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferReceiptExceptionShetabFa.setCurrency(currency);
		atmRequestTransferReceiptExceptionShetabFa.setForceReceipt(false);
		atmRequestTransferReceiptExceptionShetabFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTransferReceiptExceptionShetabFa.setFit(FITType.SHETAB);
		
		atmRequestTransferShetabEn.setOpkey("IAIDA   ");
		atmRequestTransferShetabEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferShetabEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferShetabEn.setCurrency(currency);
		atmRequestTransferShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTransferTimeOutShetabEn.setOpkey("IAIDA  F");
		atmRequestTransferTimeOutShetabEn.setNextOpkey("IAIDA  F");
		atmRequestTransferTimeOutShetabEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
		atmRequestTransferTimeOutShetabEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferTimeOutShetabEn.setCurrency(currency);
		atmRequestTransferTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferTimeOutShetabEn.setFit(FITType.SHETAB);
		
		atmRequestTransferReceiptExceptionShetabEn.setOpkey("IAIDA  A");
		atmRequestTransferReceiptExceptionShetabEn.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		atmRequestTransferReceiptExceptionShetabEn.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		atmRequestTransferReceiptExceptionShetabEn.setCurrency(currency);
		atmRequestTransferReceiptExceptionShetabEn.setForceReceipt(false);
		atmRequestTransferReceiptExceptionShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTransferReceiptExceptionShetabEn.setFit(FITType.SHETAB);		
		
		/***********************/
		List<ResponseScreen> screenTransferList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenTransferFa = new ResponseScreen();
		screenTransferFa.setScreenno("388");
		screenTransferFa.setDesc("انتقال-موفق-فارسی");
		screenTransferFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTransferFa.setScreenData("388[SI]@@[ESC]P2080[ESC]\\");
		screenTransferList.add(screenTransferFa);
		getGeneralDao().saveOrUpdate(screenTransferFa);
		
		ResponseScreen screenTransferEn = new ResponseScreen();
		screenTransferEn.setScreenno("788");
		screenTransferEn.setDesc("انتقال-موفق-انگلیسی");
		screenTransferEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTransferEn.setScreenData("788[SI]@@[ESC]P2480[ESC]\\");
		screenTransferList.add(screenTransferEn);
		getGeneralDao().saveOrUpdate(screenTransferEn);
		
		/**************************/
		List<ResponseScreen> screenCheckAccountListPart1 = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCheckAccountPart1Fa = new ResponseScreen();
		screenCheckAccountPart1Fa.setScreenno("050"); 
		screenCheckAccountPart1Fa.setDesc("بررسی حساب انتقال-موفق-فارسی-مرحله اول");
		screenCheckAccountPart1Fa.setLanguage(UserLanguage.FARSI_LANG);
		screenCheckAccountPart1Fa.setScreenData("050[FF][SI]@@[ESC]P2120[ESC]\\[ESC](K[ESC][OC]"+updateablePageFontColor+"[GR account2FScr(ifx.AppPAN,'LD')][SI]LI");   
//		screenCheckAccountPart1Fa.setScreenData("050[FF][SI]@@[ESC]P2050[ESC]\\[ESC](K[ESC][OC]"+updateablePageFontColor+"[GR account2FScr(ifx.AppPAN,'LG')][SI]LK");   
		screenCheckAccountListPart1.add(screenCheckAccountPart1Fa);
		getGeneralDao().saveOrUpdate(screenCheckAccountPart1Fa);
			
		ResponseScreen screenCheckAccountPart1En = new ResponseScreen();
		screenCheckAccountPart1En.setScreenno("450"); 
		screenCheckAccountPart1En.setDesc("بررسی حساب انتقال-موفق-انگلیسی-مرحله اول");
		screenCheckAccountPart1En.setLanguage(UserLanguage.ENGLISH_LANG);  
		screenCheckAccountPart1En.setScreenData("450[FF][SI]@@[ESC]P2520[ESC]\\[ESC](1[ESC][OC]"+updateablePageFontColor+"[GR account2EScr(ifx.AppPAN,'LD')][SI]LI");   
//		screenCheckAccountPart1En.setScreenData("450[FF][SI]@@[ESC]P2450[ESC]\\[ESC](1[ESC][OC]"+updateablePageFontColor+"[GR account2EScr(ifx.AppPAN,'LG')][SI]LK");   
		screenCheckAccountListPart1.add(screenCheckAccountPart1En);
		getGeneralDao().saveOrUpdate(screenCheckAccountPart1En);
		/*********************/
		
		/**************************/
		List<ResponseScreen> screenCheckAccountListPart2 = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCheckAccountPart2Fa = new ResponseScreen();
		screenCheckAccountPart2Fa.setScreenno("051"); 
		screenCheckAccountPart2Fa.setDesc("بررسی حساب انتقال-موفق-فارسی-مرحله دوم");
		screenCheckAccountPart2Fa.setLanguage(UserLanguage.FARSI_LANG);
		screenCheckAccountPart2Fa.setScreenData("051[FF][SI]@@[ESC]P2120[ESC]\\[ESC](K[ESC][OC]"+updateablePageFontColor+"[GR account2FScr(ifx.AppPAN,'LD')][SI]LO");   
//		screenCheckAccountPart2Fa.setScreenData("051[FF][SI]@@[ESC]P2050[ESC]\\[ESC](K[ESC][OC]"+updateablePageFontColor+"[GR account2FScr(ifx.AppPAN,'LG')][SI]L0");   
		screenCheckAccountListPart2.add(screenCheckAccountPart2Fa);
		getGeneralDao().saveOrUpdate(screenCheckAccountPart2Fa);
			
		ResponseScreen screenCheckAccountPart2En = new ResponseScreen();
		screenCheckAccountPart2En.setScreenno("451"); 
		screenCheckAccountPart2En.setDesc("بررسی حساب انتقال-موفق-انگلیسی-مرحله دوم");
		screenCheckAccountPart2En.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCheckAccountPart2En.setScreenData("451[FF][SI]@@[ESC]P2520[ESC]\\[ESC](1[ESC][OC]"+updateablePageFontColor+"[GR account2EScr(ifx.AppPAN,'LD')][SI]LO");   
//		screenCheckAccountPart2En.setScreenData("451[FF][SI]@@[ESC]P2450[ESC]\\[ESC](1[ESC][OC]"+updateablePageFontColor+"[GR account2EScr(ifx.AppPAN,'LG')][SI]L0");   
		screenCheckAccountListPart2.add(screenCheckAccountPart2En);
		getGeneralDao().saveOrUpdate(screenCheckAccountPart2En);
		/*********************/		
		
		/**************************/
		List<ResponseScreen> screenCheckAccountListPart3 = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCheckAccountPart3Fa = new ResponseScreen();
		screenCheckAccountPart3Fa.setScreenno("052"); 
		screenCheckAccountPart3Fa.setDesc("بررسی حساب انتقال-موفق-فارسی-مرحله سوم");
		screenCheckAccountPart3Fa.setLanguage(UserLanguage.FARSI_LANG);
		screenCheckAccountPart3Fa.setScreenData("052[FF][SI]@@[ESC]P2120[ESC]\\[ESC](K[ESC][OC]"+updateablePageFontColor+"[GR account2FScr(ifx.AppPAN,'LD')][SI]L8");   
//		screenCheckAccountPart3Fa.setScreenData("052[FF][SI]@@[ESC]P2050[ESC]\\[ESC](K[ESC][OC]"+updateablePageFontColor+"[GR account2FScr(ifx.AppPAN,'LG')][SI]L9");   
		screenCheckAccountListPart3.add(screenCheckAccountPart3Fa);
		getGeneralDao().saveOrUpdate(screenCheckAccountPart3Fa);
			
		ResponseScreen screenCheckAccountPart3En = new ResponseScreen();
		screenCheckAccountPart3En.setScreenno("452"); 
		screenCheckAccountPart3En.setDesc("بررسی حساب انتقال-موفق-انگلیسی-مرحله سوم");
		screenCheckAccountPart3En.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCheckAccountPart3En.setScreenData("452[FF][SI]@@[ESC]P2520[ESC]\\[ESC](1[ESC][OC]"+updateablePageFontColor+"[GR account2EScr(ifx.AppPAN,'LD')][SI]L9");   
//		screenCheckAccountPart3En.setScreenData("452[FF][SI]@@[ESC]P2450[ESC]\\[ESC](1[ESC][OC]"+updateablePageFontColor+"[GR account2EScr(ifx.AppPAN,'LG')][SI]L9");   
		screenCheckAccountListPart3.add(screenCheckAccountPart3En);
		getGeneralDao().saveOrUpdate(screenCheckAccountPart3En);
		/*********************/			
		
		List<ResponseScreen> screenCheckAccountListLastPart = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCheckAccountLastPartFa = new ResponseScreen();
		screenCheckAccountLastPartFa.setScreenno("033");
		screenCheckAccountLastPartFa.setDesc("بررسی حساب انتقال-موفق-فارسی-مرحله آخر");
		screenCheckAccountLastPartFa.setLanguage(UserLanguage.FARSI_LANG);
		if (!atmType.equals(ATMType.NCR)) {
			screenCheckAccountLastPartFa.setScreenData("033[ESC]P2085[ESC]\\"   
					+ "[ESC](K" 
					+ "[ESC][OC]"+updateablePageFontColor 
					+ "[SI]G0[GR c2F(ifx.CardHolderFamily)][GR c2F(ifx.CardHolderName)]"
					+ "[SI]H0[GR printAppPan(ifx.AppPAN)]" 
					+ "[SI]I0[GR ifx.Auth_Amt]");
		} else {
			screenCheckAccountLastPartFa.setScreenData("033[ESC]P2085[ESC]\\"   
					+ "[ESC](6" 
					+ "[ESC][OC]"+updateablePageFontColor 
					+ "[SI]G0[GR c2F(ifx.CardHolderFamily)][GR c2F(ifx.CardHolderName)]"
					+ "[SI]H0[GR printAppPan(ifx.AppPAN)]" 
					+ "[SI]I0[GR ifx.Auth_Amt]");
		}
		
		screenCheckAccountListLastPart.add(screenCheckAccountLastPartFa);
		getGeneralDao().saveOrUpdate(screenCheckAccountLastPartFa);
		
		ResponseScreen screenCheckAccountLastPartEn = new ResponseScreen();
		screenCheckAccountLastPartEn.setScreenno("433");
		screenCheckAccountLastPartEn.setDesc("بررسی حساب انتقال-موفق-انگلیسی-مرحله آخر");
		screenCheckAccountLastPartEn.setLanguage(UserLanguage.ENGLISH_LANG);
		//AldComment Change in 92.04.09 //G0 -> GL , H0 -> HL , I0 -> IL  //AldTODO Task002 : Change to GN , HG , IN
		screenCheckAccountLastPartEn.setScreenData("433[ESC]P2485[ESC]\\"   
				+ "[ESC](1" 
				+ "[ESC][OC]"+updateablePageFontColor
				+ "[SI]F0[GR safeEn(ifx.CardHolderFamily)][GR safeEn(ifx.CardHolderName)]"
				+ "[SI]HG[GR ifx.AppPAN]" 
				+ "[SI]J0[GR ifx.Auth_Amt]");
		
//		433[ESC]P2485[ESC]\[ESC](1[ESC][OC]00;80m[SI]GL[GR safeEn(ifx.CardHolderFamily)][GR safeEn(ifx.CardHolderName)][SI]HL[GR ifx.AppPAN][SI]IL[GR ifx.Auth_Amt]
		
		screenCheckAccountListLastPart.add(screenCheckAccountLastPartEn);
		getGeneralDao().saveOrUpdate(screenCheckAccountLastPartEn);
		/*********************/
		
		List<ResponseScreen> screenRecieptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenRecieptFa = new ResponseScreen();
		screenRecieptFa.setScreenno("384");
		screenRecieptFa.setDesc("انتقال-داخلی-خطای رسید-فارسی");
		screenRecieptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenRecieptFa.setScreenData(null);
		screenRecieptList.add(screenRecieptFa);
		getGeneralDao().saveOrUpdate(screenRecieptFa);
		
		ResponseScreen screenRecieptEn = new ResponseScreen();
		screenRecieptEn.setScreenno("784");
		screenRecieptEn.setDesc("انتقال-داخلی-خطای رسید-انگلیسی");
		screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenRecieptEn.setScreenData(null);
		screenRecieptList.add(screenRecieptEn);
		getGeneralDao().saveOrUpdate(screenRecieptEn);

		/************************/
		//------------------------ Pasargad -----------------------------------
		FunctionCommandResponse responseCheckAccountPasargadPart1 = new FunctionCommandResponse();
		responseCheckAccountPasargadPart1.setName("بررسی حساب انتقال به سپرده-داخلی-موفق-مرحله اول");
		responseCheckAccountPasargadPart1.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountPasargadPart1.setNextState("186");
		responseCheckAccountPasargadPart1.setScreen(screenCheckAccountListPart1);
		responseCheckAccountPasargadPart1.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountPasargadPart1);
		
		FunctionCommandResponse responseCheckAccountPasargadPart2 = new FunctionCommandResponse();
		responseCheckAccountPasargadPart2.setName("بررسی حساب انتقال به سپرده-داخلی-موفق-مرحله دوم");
		responseCheckAccountPasargadPart2.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountPasargadPart2.setNextState("187");
		responseCheckAccountPasargadPart2.setScreen(screenCheckAccountListPart2);
		responseCheckAccountPasargadPart2.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountPasargadPart2);	
		
		FunctionCommandResponse responseCheckAccountPasargadPart3 = new FunctionCommandResponse();
		responseCheckAccountPasargadPart3.setName("بررسی حساب انتقال به سپرده-داخلی-موفق-مرحله سوم");
		responseCheckAccountPasargadPart3.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountPasargadPart3.setNextState("188");
		responseCheckAccountPasargadPart3.setScreen(screenCheckAccountListPart3);
		responseCheckAccountPasargadPart3.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountPasargadPart3);		
		
		FunctionCommandResponse responseCheckAccountPasargadLastPart = new FunctionCommandResponse();
		responseCheckAccountPasargadLastPart.setName("بررسی حساب انتقال به سپرده-داخلی-موفق-مرحله آخر");
		responseCheckAccountPasargadLastPart.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountPasargadLastPart.setNextState("200"); 
		responseCheckAccountPasargadLastPart.setScreen(screenCheckAccountListLastPart);
		responseCheckAccountPasargadPart1.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountPasargadLastPart);
		//---------------------------------------------------------------------
		
		//------------------------ Shetab -----------------------------------
		FunctionCommandResponse responseCheckAccountShetabPart1 = new FunctionCommandResponse();
		responseCheckAccountShetabPart1.setName("بررسی حساب انتقال به سپرده-شتابی-موفق-مرحله اول");
		responseCheckAccountShetabPart1.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountShetabPart1.setNextState("634");  
//old		responseCheckAccountShetabPart1.setNextState("629");  
		responseCheckAccountShetabPart1.setScreen(screenCheckAccountListPart1);
		responseCheckAccountShetabPart1.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountShetabPart1);
		
		FunctionCommandResponse responseCheckAccountShetabPart2 = new FunctionCommandResponse();
		responseCheckAccountShetabPart2.setName("بررسی حساب انتقال به سپرده-شتابی-موفق-مرحله دوم");
		responseCheckAccountShetabPart2.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountShetabPart2.setNextState("635"); 
//old		responseCheckAccountShetabPart2.setNextState("630"); 
		responseCheckAccountShetabPart2.setScreen(screenCheckAccountListPart2);
		responseCheckAccountShetabPart2.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountShetabPart2);	
		
		FunctionCommandResponse responseCheckAccountShetabPart3 = new FunctionCommandResponse();
		responseCheckAccountShetabPart3.setName("بررسی حساب انتقال به سپرده-شتابی-موفق-مرحله سوم");
		responseCheckAccountShetabPart3.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountShetabPart3.setNextState("636"); 
//old		responseCheckAccountShetabPart3.setNextState("631"); 
		responseCheckAccountShetabPart3.setScreen(screenCheckAccountListPart3);
		responseCheckAccountShetabPart3.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountShetabPart3);		
		
		FunctionCommandResponse responseCheckAccountShetabLastPart = new FunctionCommandResponse();
		responseCheckAccountShetabLastPart.setName("بررسی حساب انتقال به سپرده-شتابی-موفق-مرحله آخر");
		responseCheckAccountShetabLastPart.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountShetabLastPart.setNextState("646"); 
//		responseCheckAccountShetabLastPart.setNextState("641"); 
		responseCheckAccountShetabLastPart.setScreen(screenCheckAccountListLastPart);
		responseCheckAccountShetabLastPart.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountShetabLastPart);
		//---------------------------------------------------------------------
		
		
		/************************/
		//---------------------------- Pasargad --------------------------------
		FunctionCommandResponse responseTransferPasargad = new FunctionCommandResponse();
		responseTransferPasargad.setName("انتقال-داخلی-موفق");
		responseTransferPasargad.setBeRetain(false);
		responseTransferPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferPasargad.setNextState("096");
		responseTransferPasargad.setScreen(screenTransferList);
		responseTransferPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseTransferPasargad);
		
		FunctionCommandResponse responseTransferReceiptExceptionPasargad = new FunctionCommandResponse();
		responseTransferReceiptExceptionPasargad.setName("انتقال-داخلی-خطای رسید");
		responseTransferReceiptExceptionPasargad.setBeRetain(false);
		responseTransferReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferReceiptExceptionPasargad.setNextState("092");
		responseTransferReceiptExceptionPasargad.setScreen(screenRecieptList);
		responseTransferReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTransferReceiptExceptionPasargad);
		
		//---------------------------- Shetab --------------------------------
		FunctionCommandResponse responseTransferShetab = new FunctionCommandResponse();
		responseTransferShetab.setName("انتقال-شتابی-موفق");
		responseTransferShetab.setBeRetain(false);
		responseTransferShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferShetab.setNextState("596"); 
		responseTransferShetab.setScreen(screenTransferList);
		responseTransferShetab.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseTransferShetab);
		
		FunctionCommandResponse responseTransferReceiptExceptionShetab = new FunctionCommandResponse();
		responseTransferReceiptExceptionShetab.setName("انتقال-شتابی-خطای رسید");
		responseTransferReceiptExceptionShetab.setBeRetain(false);
		responseTransferReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTransferReceiptExceptionShetab.setNextState("592"); 
		responseTransferReceiptExceptionShetab.setScreen(screenRecieptList);
		responseTransferReceiptExceptionShetab.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTransferReceiptExceptionShetab);
		
		/************************/
		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("بررسی حساب انتقال-time out-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("بررسی حساب انتقال-time out-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("انتقال-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		
		/************************/
		/************************/
		FunctionCommandResponse responseCheckAccountTimeOut = new FunctionCommandResponse();
		responseCheckAccountTimeOut.setName("بررسی حساب انتقال-time out");
		responseCheckAccountTimeOut.setBeRetain(false);
		responseCheckAccountTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCheckAccountTimeOut.setNextState("713");
		responseCheckAccountTimeOut.setScreen(screenTimeoutList);
		responseCheckAccountTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseCheckAccountTimeOut);
		
		/************************/
		
		//-------------------------------Pasargad --------------------------------
		atmRequestCheckAcountPasargadPart1Fa.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP, responseCheckAccountPasargadPart1);
		atmRequestCheckAcountTimeOutPasargadPart1Fa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);//TODO AD TransferToAccount Aya Niaz Hast
		atmRequestCheckAcountPasargadPart1En.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP, responseCheckAccountPasargadPart1);
		atmRequestCheckAcountTimeOutPasargadPart1En.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		
		atmRequestCheckAcountPasargadPart2Fa.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountPasargadPart2);
		atmRequestCheckAcountTimeOutPasargadPart2Fa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountPasargadPart2En.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountPasargadPart2);
		atmRequestCheckAcountTimeOutPasargadPart2En.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		
		atmRequestCheckAcountPasargadPart3Fa.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountPasargadPart3);
		atmRequestCheckAcountTimeOutPasargadPart3Fa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountPasargadPart3En.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountPasargadPart3);
		atmRequestCheckAcountTimeOutPasargadPart3En.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		
		atmRequestCheckAcountPasargadLastPartFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountPasargadLastPart);
		atmRequestCheckAcountTimeOutPasargadLastPartFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountPasargadLastPartEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountPasargadLastPart);
		atmRequestCheckAcountTimeOutPasargadLastPartEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		//-----------------------------------------------------------------------------
		
		//-------------------------------Shetab --------------------------------
		atmRequestCheckAcountShetabPart1Fa.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP, responseCheckAccountShetabPart1);
		atmRequestCheckAcountTimeOutShetabPart1Fa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountShetabPart1En.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP, responseCheckAccountShetabPart1);
		atmRequestCheckAcountTimeOutShetabPart1En.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		
		atmRequestCheckAcountShetabPart2Fa.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountShetabPart2);
		atmRequestCheckAcountTimeOutShetabPart2Fa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountShetabPart2En.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountShetabPart2);
		atmRequestCheckAcountTimeOutShetabPart2En.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		
		atmRequestCheckAcountShetabPart3Fa.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountShetabPart3);
		atmRequestCheckAcountTimeOutShetabPart3Fa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountShetabPart3En.addAtmResponse(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP, responseCheckAccountShetabPart3);
		atmRequestCheckAcountTimeOutShetabPart3En.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		
		atmRequestCheckAcountShetabLastPartFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountShetabLastPart);
		atmRequestCheckAcountTimeOutShetabLastPartFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		atmRequestCheckAcountShetabLastPartEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCheckAccountShetabLastPart);
		atmRequestCheckAcountTimeOutShetabLastPartEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseCheckAccountTimeOut);
		//-----------------------------------------------------------------------------
		

		//--------------------------------- Pasargad ----------------------------------------
		atmRequestTransferPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		atmRequestTransferPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		atmRequestTransferTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		
		atmRequestTransferPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTransferTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		/**********CHECK THIS****************///TODO AD TransferToAccount ?
		atmRequestTransferReceiptExceptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
//AldComment Task002 : backup		atmRequestTransferReceiptExceptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		
		atmRequestTransferPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		atmRequestTransferPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		atmRequestTransferTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
		
		//*****me
		atmRequestTransferPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		atmRequestTransferTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionPasargad);
		/**********CHECK THIS****************///TODO AD TransferToAccount ?
		atmRequestTransferReceiptExceptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferPasargad);
//AldComment Task002 : Backup		atmRequestTransferReceiptExceptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
		//*****me
		
		//--------------------------------- Shetab ----------------------------------------
		atmRequestTransferShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		atmRequestTransferShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionShetab);
		atmRequestTransferTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		
		atmRequestTransferShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		atmRequestTransferShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseTransferReceiptExceptionShetab);
		atmRequestTransferTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTransferReceiptExceptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseTransferShetab);
		
		/************************/
		
		getGeneralDao().saveOrUpdate(configuration);
		
		//-------------------------- Pasargad -----------------------
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadPart1Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadPart1En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadPart1Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadPart1En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadPart2Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadPart2En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadPart2Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadPart2En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadPart3Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadPart3En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadPart3Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadPart3En);		
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadLastPartFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountPasargadLastPartEn);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadLastPartFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutPasargadLastPartEn);
		//--------------------------------------------------------------
		
		//-------------------------- Shetab -----------------------
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabPart1Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabPart1En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabPart1Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabPart1En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabPart2Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabPart2En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabPart2Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabPart2En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabPart3Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabPart3En);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabPart3Fa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabPart3En);		
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabLastPartFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountShetabLastPartEn);
		
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabLastPartFa);
		getGeneralDao().saveOrUpdate(atmRequestCheckAcountTimeOutShetabLastPartEn);
		//--------------------------------------------------------------
		
		//---------------------- Pasargad ------------------------------
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferPasargadSubEn);//me
		
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutPasargadSubEn);//me
		
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadSubFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionPasargadSubEn);//me
		
		//---------------------- Shetab ------------------------------
		getGeneralDao().saveOrUpdate(atmRequestTransferShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferShetabEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferTimeOutShetabEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionShetabFa);
		getGeneralDao().saveOrUpdate(atmRequestTransferReceiptExceptionShetabEn);
		
		//--------------------------- Pasargad -------------------
		requests.add(atmRequestCheckAcountPasargadPart1Fa);
		requests.add(atmRequestCheckAcountPasargadPart1En);
		
		requests.add(atmRequestCheckAcountTimeOutPasargadPart1Fa);
		requests.add(atmRequestCheckAcountTimeOutPasargadPart1En);
		
		requests.add(atmRequestCheckAcountPasargadPart2Fa);
		requests.add(atmRequestCheckAcountPasargadPart2En);
		
		requests.add(atmRequestCheckAcountTimeOutPasargadPart2Fa);
		requests.add(atmRequestCheckAcountTimeOutPasargadPart2En);		
		
		requests.add(atmRequestCheckAcountPasargadPart3Fa);
		requests.add(atmRequestCheckAcountPasargadPart3En);
		
		requests.add(atmRequestCheckAcountTimeOutPasargadPart3Fa);
		requests.add(atmRequestCheckAcountTimeOutPasargadPart3En);		
		
		requests.add(atmRequestCheckAcountPasargadLastPartFa);
		requests.add(atmRequestCheckAcountPasargadLastPartEn);
		
		requests.add(atmRequestCheckAcountTimeOutPasargadLastPartFa);
		requests.add(atmRequestCheckAcountTimeOutPasargadLastPartEn);
		//----------------------------------------------------------
		
		//--------------------------- Shetab -------------------
		requests.add(atmRequestCheckAcountShetabPart1Fa);
		requests.add(atmRequestCheckAcountShetabPart1En);
		
		requests.add(atmRequestCheckAcountTimeOutShetabPart1Fa);
		requests.add(atmRequestCheckAcountTimeOutShetabPart1En);
		
		requests.add(atmRequestCheckAcountShetabPart2Fa);
		requests.add(atmRequestCheckAcountShetabPart2En);
		
		requests.add(atmRequestCheckAcountTimeOutShetabPart2Fa);
		requests.add(atmRequestCheckAcountTimeOutShetabPart2En);		
		
		requests.add(atmRequestCheckAcountShetabPart3Fa);
		requests.add(atmRequestCheckAcountShetabPart3En);
		
		requests.add(atmRequestCheckAcountTimeOutShetabPart3Fa);
		requests.add(atmRequestCheckAcountTimeOutShetabPart3En);		
		
		requests.add(atmRequestCheckAcountShetabLastPartFa);
		requests.add(atmRequestCheckAcountShetabLastPartEn);
		
		requests.add(atmRequestCheckAcountTimeOutShetabLastPartFa);
		requests.add(atmRequestCheckAcountTimeOutShetabLastPartEn);
		//----------------------------------------------------------
		
		//-------------------- Pasargad ----------------------------
		requests.add(atmRequestTransferPasargadFa);
		requests.add(atmRequestTransferPasargadSubFa);
		requests.add(atmRequestTransferPasargadEn);
		requests.add(atmRequestTransferPasargadSubEn);//me
		
		requests.add(atmRequestTransferTimeOutPasargadFa);
		requests.add(atmRequestTransferTimeOutPasargadSubFa);
		requests.add(atmRequestTransferTimeOutPasargadEn);
		requests.add(atmRequestTransferTimeOutPasargadSubEn);//me
		
		requests.add(atmRequestTransferReceiptExceptionPasargadFa);
		requests.add(atmRequestTransferReceiptExceptionPasargadSubFa);
		requests.add(atmRequestTransferReceiptExceptionPasargadEn);
		requests.add(atmRequestTransferReceiptExceptionPasargadSubEn);//me
		
		//-------------------- Shetab ----------------------------
		requests.add(atmRequestTransferShetabFa);
		requests.add(atmRequestTransferShetabEn);
		
		requests.add(atmRequestTransferTimeOutShetabFa);
		requests.add(atmRequestTransferTimeOutShetabEn);
		
		requests.add(atmRequestTransferReceiptExceptionShetabFa);
		requests.add(atmRequestTransferReceiptExceptionShetabEn);
		
		return requests;
	}

	//TASK Task019 : Receipt Option
	private List<ATMRequest> ChangePinBlock(ATMConfiguration configuration) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();

		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		
		//Task019
		ATMRequest atmRequestReceiptOptionPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptOptionCreditPasargadEn = new ATMRequest();
		
		
		/********************************/
		atmRequestPasargadFa.setOpkey("ABGA    ");
		atmRequestPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadFa.setOpkey("ABGA   F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		//Task019 
		atmRequestReceiptOptionPasargadFa.setOpkey("ABGA   B");
		atmRequestReceiptOptionPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptOptionPasargadFa.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestReceiptOptionPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionPasargadFa.setFit(FITType.PASARGAD);		
		atmRequestReceiptOptionPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);

		/********************************/
		atmRequestPasargadEn.setOpkey("IBGA    ");
		atmRequestPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadEn.setOpkey("IBGA   F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		//Task019 
		atmRequestReceiptOptionPasargadEn.setOpkey("IBGA   B");
		atmRequestReceiptOptionPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptOptionPasargadEn.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestReceiptOptionPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionPasargadEn.setFit(FITType.PASARGAD);		
		atmRequestReceiptOptionPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		
		
		/********************************/
		atmRequestCreditPasargadFa.setOpkey("ACGA    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadFa.setOpkey("ACGA   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019
		atmRequestReceiptOptionCreditPasargadFa.setOpkey("ACGA   B");
		atmRequestReceiptOptionCreditPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptOptionCreditPasargadFa.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestReceiptOptionCreditPasargadFa.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptOptionCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);		
		atmRequestReceiptOptionCreditPasargadFa.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);
		
		/********************************/
		atmRequestCreditPasargadEn.setOpkey("ICGA    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadEn.setOpkey("ICGA   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		//Task019
		atmRequestReceiptOptionCreditPasargadEn.setOpkey("ICGA   B");
		atmRequestReceiptOptionCreditPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptOptionCreditPasargadEn.setTrnType(TrnType.CHANGEPINBLOCK);
		atmRequestReceiptOptionCreditPasargadEn.setCurrency(currency);
		atmRequestReceiptOptionCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptOptionCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);		
		atmRequestReceiptOptionCreditPasargadEn.setReceiptOption(ReceiptOptionType.WITHOUT_RECEIPT);		

		
		/********************************/
		String textFa = "[GR simpleChangePinReceiptFa()]";		

		String textEn =
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('PIN Change Receipt')]" : "[LF][LF][GR center(GR c2NCRE('PIN Change Receipt'))]")
			+ newLine
			+ lineEn
			+ receivedDateEn
//			+ newLine + newLine + newLine
			+ newLine + newLine
			+ formatAppPanEn
//			+ newLine + newLine + newLine
			+ newLine + newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR center('Your PIN has been changed successfully')]" : "[GR center(GR c2NCRE('Your PIN has been changed successfully'))]")
			+ "[GR putLF(7)]"
			+ footerEn;
			;

		String textJournal00 = "[GR simpleChangePinJournal()]" ;			
		//String textJournal00 = "[LF]Pin Change:[SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";


		ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList.add(receiptFa);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList.add(receiptEn);

		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList.add(journal);

		/********************************/
		List<ResponseScreen> screenPasargadList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenPasargadFa = new ResponseScreen();
		screenPasargadFa.setScreenno("385");
		screenPasargadFa.setDesc("تغییر رمز اول-داخلی-موفق-فارسی");
		screenPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		screenPasargadFa.setScreenData("385[FF][SI]@@[ESC]P2140[ESC]\\");
		screenPasargadList.add(screenPasargadFa);
		getGeneralDao().saveOrUpdate(screenPasargadFa);
		
		ResponseScreen screenPasargadEn = new ResponseScreen();
		screenPasargadEn.setScreenno("785");
		screenPasargadEn.setDesc("تغییر رمز اول-داخلی-موفق-انگلیسی");
		screenPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenPasargadEn.setScreenData("785[FF][SI]@@[ESC]P2540[ESC]\\");
		screenPasargadList.add(screenPasargadEn);
		getGeneralDao().saveOrUpdate(screenPasargadEn);

		FunctionCommandResponse responsePasargad = new FunctionCommandResponse();
		responsePasargad.setName("تغییر رمز اول-داخلی-موفق");
		responsePasargad.setBeRetain(false);
		responsePasargad.setDispense(null);
		responsePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responsePasargad.setNextState("708");
		responsePasargad.setScreen(screenPasargadList);
		responsePasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responsePasargad);
		
		/*******************************/
		List<ResponseScreen> screenCreditList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCreditFa = new ResponseScreen();
		screenCreditFa.setScreenno("385");
		screenCreditFa.setDesc("تغییر رمز اول-اعتباری داخلی-موفق-فارسی");
		screenCreditFa.setLanguage(UserLanguage.FARSI_LANG);
		screenCreditFa.setScreenData("385[FF][SI]@@[ESC]P2140[ESC]\\");
		screenCreditList.add(screenCreditFa);
		getGeneralDao().saveOrUpdate(screenCreditFa);
		
		ResponseScreen screenCreditEn = new ResponseScreen();
		screenCreditEn.setScreenno("785");
		screenCreditEn.setDesc("تغییر رمز اول-اعتباری داخلی-موفق-انگلیسی");
		screenCreditEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCreditEn.setScreenData("785[FF][SI]@@[ESC]P2540[ESC]\\");
		screenCreditList.add(screenCreditEn);
		getGeneralDao().saveOrUpdate(screenCreditEn);

		FunctionCommandResponse responseCreditPasargad = new FunctionCommandResponse();
		responseCreditPasargad.setName("تغییر رمز اول-اعتباری داخلی-موفق");
		responseCreditPasargad.setBeRetain(false);
		responseCreditPasargad.setDispense(null);
		responseCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCreditPasargad.setNextState("708");
//		responseCreditPasargad.setNextScreen("385");
//		responseCreditPasargad.setScreenData("385[FF][SI]@@[ESC]P2140[ESC]\\");
		responseCreditPasargad.setScreen(screenCreditList);
		responseCreditPasargad.setReceipt(receiptList);
		getGeneralDao().saveOrUpdate(responseCreditPasargad);
		
		/*******************************/
		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("تغییر رمز اول-time out-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("تغییر رمز اول-time out-انگلیسی");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);

		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("تغییر رمز اول-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		/********************************/
		
		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);//Task019
		
		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);//Task019
		
		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);//Task019
		
		
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptOptionCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);//Task019
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		
		
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadFa);//Task019
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionPasargadEn);//Task019
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadFa);//Task019
		getGeneralDao().saveOrUpdate(atmRequestReceiptOptionCreditPasargadEn);//Task019
		

		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestCreditPasargadEn);
		
		requests.add(atmRequestTimeOutPasargadFa);
		requests.add(atmRequestTimeOutPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		
		requests.add(atmRequestReceiptOptionPasargadFa);//Task019       
		requests.add(atmRequestReceiptOptionPasargadEn);//Task019       
		requests.add(atmRequestReceiptOptionCreditPasargadFa);//Task019 
		requests.add(atmRequestReceiptOptionCreditPasargadEn);//Task019 
		
		
		return requests;
	}
	
	private List<ATMRequest> ChangeInternetPinBlock(ATMConfiguration configuration) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		
		ATMRequest atmRequestPasargadFa = new ATMRequest();
		ATMRequest atmRequestPasargadEn = new ATMRequest();
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		
		ATMRequest atmRequestReceiptExceptionPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionPasargadEn = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestReceiptExceptionCreditPasargadEn = new ATMRequest();
		
		/***************************/
		atmRequestPasargadFa.setOpkey("ABGB    ");
		atmRequestPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestPasargadFa.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestPasargadFa.setCurrency(currency);
		atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadFa.setOpkey("ABGB   F");
		atmRequestTimeOutPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadFa.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestTimeOutPasargadFa.setCurrency(currency);
		atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);
		
		atmRequestReceiptExceptionPasargadFa.setOpkey("ABGB   A");
		atmRequestReceiptExceptionPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptExceptionPasargadFa.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestReceiptExceptionPasargadFa.setCurrency(currency);
		atmRequestReceiptExceptionPasargadFa.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionPasargadFa.setFit(FITType.PASARGAD);
		
		/***************************/
		
		atmRequestPasargadEn.setOpkey("IBGB    ");
		atmRequestPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestPasargadEn.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestPasargadEn.setCurrency(currency);
		atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestTimeOutPasargadEn.setOpkey("IBGB   F");
		atmRequestTimeOutPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutPasargadEn.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestTimeOutPasargadEn.setCurrency(currency);
		atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);
		
		atmRequestReceiptExceptionPasargadEn.setOpkey("IBGB   A");
		atmRequestReceiptExceptionPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptExceptionPasargadEn.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestReceiptExceptionPasargadEn.setCurrency(currency);
		atmRequestReceiptExceptionPasargadEn.setForceReceipt(false);
		atmRequestReceiptExceptionPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionPasargadEn.setFit(FITType.PASARGAD);
		
		/***************************/
		
		atmRequestCreditPasargadFa.setOpkey("ACGB    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadFa.setOpkey("ACGB   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestReceiptExceptionCreditPasargadFa.setOpkey("ACGB   A");
		atmRequestReceiptExceptionCreditPasargadFa.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptExceptionCreditPasargadFa.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestReceiptExceptionCreditPasargadFa.setCurrency(currency);
		atmRequestReceiptExceptionCreditPasargadFa.setForceReceipt(false);
		atmRequestReceiptExceptionCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestReceiptExceptionCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		/***************************/
		
		atmRequestCreditPasargadEn.setOpkey("ICGB    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadEn.setOpkey("ICGB   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestReceiptExceptionCreditPasargadEn.setOpkey("ICGB   A");
		atmRequestReceiptExceptionCreditPasargadEn.setIfxType(IfxType.CHANGE_PIN_BLOCK_RQ);
		atmRequestReceiptExceptionCreditPasargadEn.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		atmRequestReceiptExceptionCreditPasargadEn.setCurrency(currency);
		atmRequestReceiptExceptionCreditPasargadEn.setForceReceipt(false);
		atmRequestReceiptExceptionCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestReceiptExceptionCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		/***************************/
		
		String textFa = "[GR simpleChangeInternetPinReceiptFa()]";
		
		String textEn = 
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Internet PIN Change Receipt')]" : "[LF][LF][GR center(GR c2NCRE('Internet PIN Change Receipt'))]")
			+ newLine
			+ lineEn
			+ receivedDateEn
//			+ newLine + newLine + newLine
			+ newLine + newLine
			+ formatAppPanEn
//			+ newLine + newLine + newLine
			+ newLine + newLine
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.CVV2, 'CVV2')]"  : "[GR justify(GR ifx.CVV2, GR c2NCRE('CVV2'))]")
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.ExpDt, 'Expiry date')]"  : "[GR justify(GR ifx.ExpDt, GR c2NCRE('Expiry date'))]")
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR center('Your PIN has been changed successfully')]" : "[GR center(GR c2NCRE('Your PIN has been changed successfully'))]")
			+ "[GR putLF(8)]"
			+ footerEn;

		String textJournal00 = "[GR simpleChangeInternetPinJournal()]" ; 
				//"[LF]Pin2 Change:[SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt]";

		
		ArrayList<Receipt> receiptList00 = new ArrayList<Receipt>();
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList00.add(receiptFa);

		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList00.add(receiptEn);

		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList00.add(journal);
		
		/*************************/
		List<ResponseScreen> screenPasargadList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenPasargadFa = new ResponseScreen();
		screenPasargadFa.setScreenno("388");
		screenPasargadFa.setDesc("تغییر رمز اینترنتی-داخلی-موفق-فارسی");
		screenPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		screenPasargadFa.setScreenData("388[FF][SI]@@[ESC]P2141[ESC]\\");
		screenPasargadList.add(screenPasargadFa);
		getGeneralDao().saveOrUpdate(screenPasargadFa);
		
		ResponseScreen screenPasargadEn = new ResponseScreen();
		screenPasargadEn.setScreenno("788");
		screenPasargadEn.setDesc("تغییر رمز اینترنتی-داخلی-موفق-انگلیسی");
		screenPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenPasargadEn.setScreenData("788[FF][SI]@@[ESC]P2541[ESC]\\");
		screenPasargadList.add(screenPasargadEn);
		getGeneralDao().saveOrUpdate(screenPasargadEn);

		FunctionCommandResponse responsePasargad = new FunctionCommandResponse();
		responsePasargad.setName("تغییر رمز اینترنتی-داخلی-موفق");
		responsePasargad.setBeRetain(false);
		responsePasargad.setDispense(null);
		responsePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responsePasargad.setNextState("096");
//		responsePasargad.setNextScreen("388");
//		responsePasargad.setScreenData("388[FF][SI]@@[ESC]P2141[ESC]\\");
		responsePasargad.setScreen(screenPasargadList);
		responsePasargad.setReceipt(receiptList00);
		getGeneralDao().saveOrUpdate(responsePasargad);
		
		List<ResponseScreen> screenPasargadReceiptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenPasargadRecieptFa = new ResponseScreen();
		screenPasargadRecieptFa.setScreenno("384");
		screenPasargadRecieptFa.setDesc("تغییر رمز اینترنتی-داخلی-خطای رسید-فارسی");
		screenPasargadRecieptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenPasargadRecieptFa.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
		screenPasargadReceiptList.add(screenPasargadRecieptFa);
		getGeneralDao().saveOrUpdate(screenPasargadRecieptFa);
		
		ResponseScreen screenPasargadReceiptEn = new ResponseScreen();
		screenPasargadReceiptEn.setScreenno("784");
		screenPasargadReceiptEn.setDesc("تغییر رمز اینترنتی-داخلی-خطای رسید-انگلیسی");
		screenPasargadReceiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenPasargadReceiptEn.setScreenData("784[FF][SI]@@[ESC]P2542[ESC]\\");
		screenPasargadReceiptList.add(screenPasargadReceiptEn);
		getGeneralDao().saveOrUpdate(screenPasargadReceiptEn);

		FunctionCommandResponse responseReceiptExceptionPasargad = new FunctionCommandResponse();
		responseReceiptExceptionPasargad.setName("تغییر رمز اینترنتی-داخلی-خطای رسید");
		responseReceiptExceptionPasargad.setBeRetain(false);
		responseReceiptExceptionPasargad.setDispense(null);
		responseReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionPasargad.setNextState("092");
//		responseReceiptExceptionPasargad.setNextScreen("384");
//		responseReceiptExceptionPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
		responseReceiptExceptionPasargad.setScreen(screenPasargadReceiptList);
		responseReceiptExceptionPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionPasargad);
		
		/*************************/
		
		List<ResponseScreen> screenCreditList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCreditFa = new ResponseScreen();
		screenCreditFa.setScreenno("388");
		screenCreditFa.setDesc("تغییر رمز اینترنتی-اعتباری داخلی-موفق-فارسی");
		screenCreditFa.setLanguage(UserLanguage.FARSI_LANG);
		screenCreditFa.setScreenData("388[FF][SI]@@[ESC]P2141[ESC]\\");
		screenCreditList.add(screenCreditFa);
		getGeneralDao().saveOrUpdate(screenCreditFa);
		
		ResponseScreen screenCreditEn = new ResponseScreen();
		screenCreditEn.setScreenno("788");
		screenCreditEn.setDesc("تغییر رمز اینترنتی-اعتباری داخلی-موفق-انگلیسی");
		screenCreditEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCreditEn.setScreenData("788[FF][SI]@@[ESC]P2541[ESC]\\");
		screenCreditList.add(screenCreditEn);
		getGeneralDao().saveOrUpdate(screenCreditEn);
		
		FunctionCommandResponse responseCreditPasargad = new FunctionCommandResponse();
		responseCreditPasargad.setName("تغییر رمز اینترنتی-اعتباری داخلی-موفق");
		responseCreditPasargad.setBeRetain(false);
		responseCreditPasargad.setDispense(null);
		responseCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseCreditPasargad.setNextState("196");
//		responseCreditPasargad.setNextScreen("388");
//		responseCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2141[ESC]\\");
		responseCreditPasargad.setScreen(screenCreditList);
		responseCreditPasargad.setReceipt(receiptList00);
		getGeneralDao().saveOrUpdate(responseCreditPasargad);
		
		List<ResponseScreen> screenCreditRecieptList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenCreditRecieptFa = new ResponseScreen();
		screenCreditRecieptFa.setScreenno("384");
		screenCreditRecieptFa.setDesc("تغییر رمز اینترنتی-اعتباری داخلی-خطای رسید-فارسی");
		screenCreditRecieptFa.setLanguage(UserLanguage.FARSI_LANG);
		screenCreditRecieptFa.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
		screenCreditRecieptList.add(screenCreditRecieptFa);
		getGeneralDao().saveOrUpdate(screenCreditRecieptFa);
		
		ResponseScreen screenCreditRecieptEn = new ResponseScreen();
		screenCreditRecieptEn.setScreenno("784");
		screenCreditRecieptEn.setDesc("تغییر رمز اینترنتی-اعتباری داخلی-خطای رسید-انگلیسی");
		screenCreditRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenCreditRecieptEn.setScreenData("784[FF][SI]@@[ESC]P2542[ESC]\\");
		screenCreditRecieptList.add(screenCreditRecieptEn);
		getGeneralDao().saveOrUpdate(screenCreditRecieptEn);

		FunctionCommandResponse responseReceiptExceptionCreditPasargad = new FunctionCommandResponse();
		responseReceiptExceptionCreditPasargad.setName("تغییر رمز اینترنتی-اعتباری داخلی-خطای رسید");
		responseReceiptExceptionCreditPasargad.setBeRetain(false);
		responseReceiptExceptionCreditPasargad.setDispense(null);
		responseReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseReceiptExceptionCreditPasargad.setNextState("192");
//		responseReceiptExceptionCreditPasargad.setNextScreen("384");
//		responseReceiptExceptionCreditPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
		responseReceiptExceptionCreditPasargad.setScreen(screenCreditRecieptList);
		responseReceiptExceptionCreditPasargad.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseReceiptExceptionCreditPasargad);
		
		/*************************/
		List<ResponseScreen> screenCreditTimeoutList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenScreenTimeoutFa = new ResponseScreen();
		screenScreenTimeoutFa.setScreenno("398");
		screenScreenTimeoutFa.setDesc("تغییر رمز اینترنتی-time out-فارسی");
		screenScreenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
//		screenFa.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		screenCreditTimeoutList.add(screenScreenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenScreenTimeoutFa);
		
		ResponseScreen screenCreditTimeoutEn = new ResponseScreen();
		screenCreditTimeoutEn.setScreenno("798");
		screenCreditTimeoutEn.setDesc("تغییر رمز اینترنتی-time out-انگلیسی");
		screenCreditTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
//		screenEn.setScreenData("788[FF][SI]@@[ESC]P2512[ESC]\\");
		screenCreditTimeoutList.add(screenCreditTimeoutEn);
		getGeneralDao().saveOrUpdate(screenCreditTimeoutEn);

		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("تغییر رمز اینترنتی-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenCreditTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		/*************************/
		
		atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
		atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptExceptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
		
		atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
		atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptExceptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
		
		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
		atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptExceptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
		
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
		atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestReceiptExceptionCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
		
		/*************************/
		
		getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadEn);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionCreditPasargadEn);
		
		requests.add(atmRequestPasargadFa);
		requests.add(atmRequestPasargadEn);
		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestCreditPasargadEn);
		
		requests.add(atmRequestTimeOutPasargadFa);
		requests.add(atmRequestTimeOutPasargadEn);
		requests.add(atmRequestTimeOutCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		
		requests.add(atmRequestReceiptExceptionPasargadFa);
		requests.add(atmRequestReceiptExceptionPasargadEn);
		requests.add(atmRequestReceiptExceptionCreditPasargadFa);
		requests.add(atmRequestReceiptExceptionCreditPasargadEn);

		return requests;
	}
	
	private List<ATMRequest> CreditStatementData(ATMConfiguration configuration) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();
		
		ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
		ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();
		
		/******************************/
		atmRequestCreditPasargadFa.setOpkey("ACFB    ");
		atmRequestCreditPasargadFa.setIfxType(IfxType.CREDIT_CARD_DATA_RQ);
		atmRequestCreditPasargadFa.setTrnType(TrnType.CREDITCARDDATA);
		atmRequestCreditPasargadFa.setCurrency(currency);
		atmRequestCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		atmRequestTimeOutCreditPasargadFa.setOpkey("ACFB   F");
		atmRequestTimeOutCreditPasargadFa.setIfxType(IfxType.CREDIT_CARD_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadFa.setTrnType(TrnType.CREDITCARDDATA);
		atmRequestTimeOutCreditPasargadFa.setCurrency(currency);
		atmRequestTimeOutCreditPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRequestTimeOutCreditPasargadFa.setFit(FITType.CREDIT_PASARGAD);
		
		/******************************/
		atmRequestCreditPasargadEn.setOpkey("ICFB    ");
		atmRequestCreditPasargadEn.setIfxType(IfxType.CREDIT_CARD_DATA_RQ);
		atmRequestCreditPasargadEn.setTrnType(TrnType.CREDITCARDDATA);
		atmRequestCreditPasargadEn.setCurrency(currency);
		atmRequestCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		
		//*****me
		atmRequestTimeOutCreditPasargadEn.setOpkey("ICFB   F");
		atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.CREDIT_CARD_REV_REPEAT_RQ);
		atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.CREDITCARDDATA);
		atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
		atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
		atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);
		//*****me
		
		/******************************/
		
		String textFa = "[GR simpleCreditStatementDataReceiptFa()]";
//			headerFa
//			+ "[LF][LF][GR center(GR c2F('اطلاعات آخرین صورتحساب کارت اعتباری'))]"
//			+ newLine
//			+ lineFa
//			+ receivedDateFa
////			+ newLine + newLine + newLine
//			+ newLine + newLine
//			+ formatAppPanFa
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(GR c2F('مبلغ تراکنش ها'), GR amount2F(ifx.CreditTotalTransactionAmount, 15))]" 
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(GR c2F('کارمزدها'), GR amount2F(ifx.CreditTotalFeeAmount, 12))]" 
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(GR c2F('جریمه دیرکرد'), GR amount2F(ifx.CreditInterest, 12))]" 
////			+ newLine + newLine 
//			+ newLine  
////			+ newLine + newLine 
//			+ newLine  
//			+ "[GR justify(GR c2F('مانده اعتبار'), GR amount2F(ifx.CreditOpenToBuy, 15))]"
//			+ "[GR putLF(6)]"
//			+ "[GR justify(GR c2F('مبلغ قابل پرداخت'), GR amount2F(ifx.CreditStatementAmount, 15))]"
////			+ newLine + newLine 
//			+ newLine  
//			+ footerFa;
		
		String textEn = 
			headerEn
			+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Last Credit Card Statement Info')]" : "[LF][LF][GR center(GR c2NCRE('Last Credit Card Statement Info'))]")
			+ newLine
			+ lineEn
			+ receivedDateEn
//			+ newLine + newLine + newLine
			+ newLine + newLine
			+ formatAppPanEn
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.CreditTotalTransactionAmount, 'Transaction Amount')]" : "[GR justify(GR amount2E(ifx.CreditTotalTransactionAmount,15), GR c2NCRE('Transaction Amount'))]") //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.CreditTotalFeeAmount, 'Fee')]" : "[GR justify(GR amount2E(ifx.CreditTotalFeeAmount,15), GR c2NCRE('Fee'))]") //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod. 
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.CreditInterest, 'Late Payment Fee')]" : "[GR justify(GR amount2E(ifx.CreditInterest ,15), GR c2NCRE('Late Payment Fee'))]") //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
//			+ newLine + newLine 
			+ newLine  
//			+ newLine + newLine 
			+ newLine  
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.CreditOpenToBuy, 'Available Credit')]" : "[GR justify(GR amount2E(ifx.CreditOpenToBuy,15), GR c2NCRE('Available Credit'))]") //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
			+ "[GR putLF(6)]"
			+ (!atmType.equals(ATMType.NCR) ? "[GR justify(GR ifx.CreditStatementAmount, 'Payable Amount')]" : "[GR justify(GR amount2E(ifx.CreditStatementAmount,15), GR c2NCRE('Payable Amount'))]")//dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
//			+ newLine + newLine 
			+ newLine  
			+ footerEn;
		
		String textJournal00 = "[GR simpleCreditStatementDataJournal()]"; 
				//"[LF]Credit Statement:[SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt]";

		
		ArrayList<Receipt> receiptList00 = new ArrayList<Receipt>();
		Receipt receiptFa = new Receipt();
		receiptFa.setText(textFa);
		receiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptFa.setLanguage(UserLanguage.FARSI_LANG);
		GeneralDao.Instance.save(receiptFa);
		receiptList00.add(receiptFa);
		
		Receipt receiptEn = new Receipt();
		receiptEn.setText(textEn);
		receiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
		GeneralDao.Instance.save(receiptEn);
		receiptList00.add(receiptEn);

		
		Receipt journal = new Receipt();
		journal.setText(textJournal00);
		journal.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
		GeneralDao.Instance.save(journal);
		receiptList00.add(journal);

		List<ResponseScreen> screenList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenFa = new ResponseScreen();
		screenFa.setScreenno("388");
		screenFa.setDesc("صورتحساب کارت اعتباری-موفق-فارسی");
		screenFa.setLanguage(UserLanguage.FARSI_LANG);
		screenFa.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		screenList.add(screenFa);
		getGeneralDao().saveOrUpdate(screenFa);
		
		ResponseScreen screenEn = new ResponseScreen();
		screenEn.setScreenno("788");
		screenEn.setDesc("صورتحساب کارت اعتباری-موفق-انگلیسی");
		screenEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenEn.setScreenData("788[FF][SI]@@[ESC]P2512[ESC]\\");
		screenList.add(screenEn);
		getGeneralDao().saveOrUpdate(screenEn);

		FunctionCommandResponse response00 = new FunctionCommandResponse();
		response00.setName("صورتحساب کارت اعتباری-موفق");
		response00.setBeRetain(false);
		response00.setDispense(null);
//		response00.setFunctionCommand(NDCFunctionIdentifierConstants.PRINT_IMMEDIATE);
		response00.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		response00.setNextState("196");
//		response00.setNextScreen("388");
//		response00.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
		response00.setScreen(screenList);
		response00.setReceipt(receiptList00);
		getGeneralDao().saveOrUpdate(response00);
		
		//*****me
//		a
		List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();
		ResponseScreen screenTimeoutFa = new ResponseScreen();
		screenTimeoutFa.setScreenno("398");
		screenTimeoutFa.setDesc("صورتحساب کارت اعتباری-Timeout-فارسی");
		screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
		screenTimeoutFa.setScreenData(null);
		screenTimeoutList.add(screenTimeoutFa);
		getGeneralDao().saveOrUpdate(screenTimeoutFa);
		
		ResponseScreen screenTimeoutEn = new ResponseScreen();
		screenTimeoutEn.setScreenno("798");
		screenTimeoutEn.setDesc("صورتحساب کارت اعتباری-Timeout-انگليسي");
		screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenTimeoutEn.setScreenData(null);
		screenTimeoutList.add(screenTimeoutEn);
		getGeneralDao().saveOrUpdate(screenTimeoutEn);
		/******************/
		FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
		responseTimeOut.setName("صورتحساب كارت اعتباري-time out");
		responseTimeOut.setBeRetain(false);
		responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		responseTimeOut.setNextState("713");
//		responseTimeOut.setNextScreen("398");
		responseTimeOut.setScreen(screenTimeoutList);
		responseTimeOut.setReceipt(null);
		getGeneralDao().saveOrUpdate(responseTimeOut);
		//*****me
		atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), response00);
		atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), response00);
		
		atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
		
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);
		
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
		getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);
		
		requests.add(atmRequestCreditPasargadFa);
		requests.add(atmRequestCreditPasargadEn);
		
		requests.add(atmRequestTimeOutCreditPasargadFa);
		requests.add(atmRequestTimeOutCreditPasargadEn);
		
		return requests;
	}
		
	private List<ATMRequest> Accounts(ArrayList<Receipt> receiptList, ATMConfiguration configuration, FunctionCommandResponse atmResponse43) throws Exception {
		List<ATMRequest> requests = new ArrayList<ATMRequest>();
		ATMRequest atmRqAccountsFa = new ATMRequest();
		
		atmRqAccountsFa.setOpkey("AAAGC   ");
		atmRqAccountsFa.setIfxType(IfxType.GET_ACCOUNT_RQ);
		atmRqAccountsFa.setTrnType(TrnType.GETACCOUNT);
		atmRqAccountsFa.setLanguage(UserLanguage.FARSI_LANG);
		atmRqAccountsFa.setFit(FITType.PASARGAD);
		atmRqAccountsFa.setCurrency(currency);
		
		OARResponse response = new OARResponse();
		response.setAllNumericKeys(false);
		response.setDisplayFlag(ATMDisplayFlag.DISPLAY);
		response.setOpKeyA(true);
		response.setOpKeyB(true);
		response.setOpKeyC(true);
		response.setOpKeyD(true);
		response.setOpKeyF(true);
		response.setOpKeyG(true);
		response.setOpKeyH(true);
		response.setOpKeyI(true);
		response.setCancelKey(true);
		response.setScreenTimer("030");
		
		ResponseScreen screenFa = new ResponseScreen();
		screenFa.setLanguage(UserLanguage.FARSI_LANG);
		screenFa.setScreenno(null);
		if (!atmType.equals(ATMType.NCR)){
			screenFa.setScreenData("086[ESC]P2086[ESC]\\" 
					+ "[ESC](7" 
					+ "[ESC][OC]B0;80m");
		} else {
			screenFa.setScreenData("086[ESC]P2086[ESC]\\" 
					+ "[ESC](6" 
					+ "[ESC][OC]B0;80m");
		}
		screenFa.setDesc("لیست حساب های فرعی- فارسی");
		getGeneralDao().saveOrUpdate(screenFa);
		
		response.addScreen(screenFa);
		
		ResponseScreen screenEn = new ResponseScreen();
		screenEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenEn.setScreenno(null);
		screenEn.setScreenData("086[ESC]P2086[ESC]\\" 
				+ "[ESC](1" 
				+ "[ESC][OC]B0;80m");
		screenEn.setDesc("لیست حساب های فرعی- انگلیسی");
		getGeneralDao().saveOrUpdate(screenEn);
		
		response.addScreen(screenEn);
/*		response.setScreenData("086[ESC]P2086[ESC]\\" 
				+ "[ESC](7" 
				+ "[ESC][OC]B0;80m" 
//				+ "[SI]DG[GR ifx.allAccounts]"
				);
*/		
		atmRqAccountsFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), response);
		
		getGeneralDao().saveOrUpdate(response);
		
		getGeneralDao().saveOrUpdate(atmRqAccountsFa);
		
		requests.add(atmRqAccountsFa);
		return requests;
	}
	
	private Map<Integer, ATMResponse> sharedResponsesShetab() throws Exception {
		List<Receipt> receiptListCapture = getCardCaptureReciept();
		List<Receipt> receiptListNotAcceptable=notAcceptableRequestReciept();
		
		Map<Integer, ATMResponse> result = new HashMap<Integer, ATMResponse>();
		
		/********** General Continued States **********/
		createATMResponse("555", "034", "434", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, null, null, null, Integer.parseInt(ISOResponseCodes.HOST_LINK_DOWN), result, "HOST_LINK_DOWN"); //55

		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.BANK_LINK_DOWN), result, "BANK_LINK_DOWN"); //57
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_TIMEOUT), result, "TRANSACTION_TIMEOUT"); //58
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE), result, "TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE"); //61
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.ACQUIRER_REVERSAL), result, "ACQUIRER_REVERSAL"); //36
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION), result, "TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION"); //62
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.CASH_TRANSACTION_NOT_ALLOWED), result, "CASH_TRANSACTION_NOT_ALLOWED"); //65
		
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2515", null, Integer.parseInt(ISOResponseCodes.INVALID_CARD_STATUS), result, "INVALID_CARD_STATUS"); //12
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2515", null, ATMErrorCodes.DEFAULT_RESPONSE_CODE, result, "DEFAULT_RESPONSE_CODE");

		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2122", "2522", null, ATMErrorCodes.ATM_NOT_SUFFICIENT_AMOUNT, result, "ATM_NOT_SUFFICIENT_AMOUNT");
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2167", "2567", null, ATMErrorCodes.ATM_NOT_ROUND_AMOUNT, result, "ATM_NOT_ROUND_AMOUNT"); 
		
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2161", "2561", null, Integer.parseInt(ISOResponseCodes.INVALID_CURRENCY_CODE), result, "INVALID_CURRENCY_CODE"); //19
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2161", "2561", null, Integer.parseInt(ISOResponseCodes.PERMISSION_DENIED), result, "PERMISSION_DENIED"); //94
		
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.FIELD_ERROR), result, "FIELD_ERROR"); //09
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.INVALID_TO_ACCOUNT), result, "INVALID_TO_ACCOUNT"); //68
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.NO_PIN_KEY), result, "NO_PIN_KEY"); //84
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.CUSTOMER_NOT_FOUND), result, "CUSTOMER_NOT_FOUND"); //90
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.INVALID_TO_ACCOUNT), result, "INVALID_TO_ACCOUNT"); //91
		
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2166", "2566", null, Integer.parseInt(ISOResponseCodes.HOST_NOT_PROCESSING), result, "HOST_NOT_PROCESSING"); //51
		
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2157", "2557", null, Integer.parseInt(ISOResponseCodes.BAD_EXPIRY_DATE), result, "BAD_EXPIRY_DATE"); //97
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2157", "2557", null, Integer.parseInt(ISOResponseCodes.ORIGINAL_AMOUNT_INCORRECT), result, "ORIGINAL_AMOUNT_INCORRECT"); //98
		
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2082", "2482", null, Integer.parseInt(ISOResponseCodes.ORIGINAL_DATA_ELEMENT_MISMATCH), result, "ORIGINAL_DATA_ELEMENT_MISMATCH"); //99
		
		//TASK Task027 : Exceed Max Dispense Note
//		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2052", "2452", null, ATMErrorCodes.ATM_EXCEED_MAX_DISPENSE_NOTES, result, "ATM_EXCEED_MAX_DISPENSE_NOTES"); //130 
//Comment in 92.07.08		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2050", "2450", null, ATMErrorCodes.ATM_EXCEED_MAX_DISPENSE_NOTES, result, "ATM_EXCEED_MAX_DISPENSE_NOTES"); //130 
		//AldTODO Task057 : Change picture
		//TASK Task057 : ATM Topup 
//		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2515", null, Integer.parseInt(ISOResponseCodes.INVALID_CELLPHONE_NUMBER), result, "INVALID_CELLPHONE_NUMBER"); //106 
		//AldTODO Task057 : Change picture
		//TASK Task057 : ATM Topup 
		createATMResponse("598", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2117", "2517", null, Integer.parseInt(ISOResponseCodes.INTERNAL_DATABASE_ERROR), result, "INTERNAL_DATABASE_ERROR");//13
		
		/********** General Not-Continued States **********/
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.EXPIRY_DATE_MISMATCH), result, "EXPIRY_DATE_MISMATCH"); //41
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.ACQUIRER_NACK), result, "ACQUIRER_NACK"); //43
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //34
		
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //34
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2123", "2523", null, ATMErrorCodes.ATM_CACH_HANDLER, result, "ATM_CACH_HANDLER");

		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.WARM_CARD), result, "WARM_CARD"); //14
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.DUPLICATE_LINKED_ACCOUNT), result, "DUPLICATE_LINKED_ACCOUNT"); //33
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.SAF_TRANSMIT_MODE), result, "SAF_TRANSMIT_MODE"); //54
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.INVALID_MERCHANT), result, "INVALID_MERCHANT"); //78
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.BAD_TRANSACTION_TYPE), result, "BAD_TRANSACTION_TYPE"); //39
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.NO_TRANSACTION_ALLOWED), result, "NO_TRANSACTION_ALLOWED"); //66
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.HONOUR_WITH_ID), result, "HONOUR_WITH_ID"); //79
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2116", "2516", null, ATMErrorCodes.ATM_UNDEFINED_OPKEY, result, "ATM_UNDEFINED_OPKEY"); //undefined opkey
		//TASK Task015 : HotCard
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2116", "2516", null, Integer.parseInt(ISOResponseCodes.HOTCARD_NOT_APPROVED), result, "HOTCARD_NOT_APPROVED"); //105
		//TASK Task015 : HotCard
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2106", "2506", null, ATMErrorCodes.ATM_EJECT_CARD, result, "ATM_EJECT_CARD"); //127

		/********** APPROVED Results **********/
		return result;
	}

	private Map<Integer, ATMResponse> sharedResponsesPasargad() throws Exception {
		List<Receipt> receiptListCapture = getCardCaptureReciept();
		List<Receipt> receiptListNotAcceptable=notAcceptableRequestReciept();
		
		Map<Integer, ATMResponse> result = new HashMap<Integer, ATMResponse>();
		
		/********** General Continued States **********/
		createATMResponse("055", "034", "434", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, null, null, null, Integer.parseInt(ISOResponseCodes.HOST_LINK_DOWN), result, "HOST_LINK_DOWN"); //55
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.BANK_LINK_DOWN), result, "BANK_LINK_DOWN"); //57
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_TIMEOUT), result, "TRANSACTION_TIMEOUT"); //58
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE), result, "TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE"); //61
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.ACQUIRER_REVERSAL), result, "ACQUIRER_REVERSAL"); //36
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION), result, "TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION"); //62
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.CASH_TRANSACTION_NOT_ALLOWED), result, "CASH_TRANSACTION_NOT_ALLOWED"); //65
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2515", null, Integer.parseInt(ISOResponseCodes.INVALID_CARD_STATUS), result, "INVALID_CARD_STATUS"); //12
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2515", null, ATMErrorCodes.DEFAULT_RESPONSE_CODE, result, "DEFAULT_RESPONSE_CODE");
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2122", "2522", null, ATMErrorCodes.ATM_NOT_SUFFICIENT_AMOUNT, result, "ATM_NOT_SUFFICIENT_AMOUNT");
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2167", "2567", null, ATMErrorCodes.ATM_NOT_ROUND_AMOUNT, result, "ATM_NOT_ROUND_AMOUNT");
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2161", "2561", null, Integer.parseInt(ISOResponseCodes.INVALID_CURRENCY_CODE), result, "INVALID_CURRENCY_CODE"); //19
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2161", "2561", null, Integer.parseInt(ISOResponseCodes.PERMISSION_DENIED), result, "PERMISSION_DENIED"); //94
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.FIELD_ERROR), result, "FIELD_ERROR"); //09
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.INVALID_TO_ACCOUNT), result, "INVALID_TO_ACCOUNT"); //68
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.NO_PIN_KEY), result, "NO_PIN_KEY"); //84
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.CUSTOMER_NOT_FOUND), result, "CUSTOMER_NOT_FOUND"); //90
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.INVALID_TO_ACCOUNT), result, "INVALID_TO_ACCOUNT"); //91
	
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2166", "2566", null, Integer.parseInt(ISOResponseCodes.HOST_NOT_PROCESSING), result, "HOST_NOT_PROCESSING"); //51
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2157", "2557", null, Integer.parseInt(ISOResponseCodes.BAD_EXPIRY_DATE), result, "BAD_EXPIRY_DATE"); //97
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2157", "2557", null, Integer.parseInt(ISOResponseCodes.ORIGINAL_AMOUNT_INCORRECT), result, "ORIGINAL_AMOUNT_INCORRECT"); //98
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2145", "2545", null, ATMErrorCodes.NO_SUBSIDIARY_ACCOUNT, result, "NO_SUBSIDIARY_ACCOUNT"); //117
		
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2082", "2482", null, Integer.parseInt(ISOResponseCodes.ORIGINAL_DATA_ELEMENT_MISMATCH), result, "ORIGINAL_DATA_ELEMENT_MISMATCH"); //99
		
		//TASK Task027 : Exceed Max Dispense Note
//		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2052", "2452", null, ATMErrorCodes.ATM_EXCEED_MAX_DISPENSE_NOTES, result, "ATM_EXCEED_MAX_DISPENSE_NOTES"); //130 
//Comment in 92.07.08		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2050", "2450", null, ATMErrorCodes.ATM_EXCEED_MAX_DISPENSE_NOTES, result, "ATM_EXCEED_MAX_DISPENSE_NOTES"); //130 
		//AldTODO Task057 : Change picture
		//TASK Task057 : ATM Topup 
//		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2515", null, Integer.parseInt(ISOResponseCodes.INVALID_CELLPHONE_NUMBER), result, "INVALID_CELLPHONE_NUMBER"); //106 
		//AldTODO Task057 : Change picture
		//TASK Task057 : ATM Topup 
		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2117", "2517", null, Integer.parseInt(ISOResponseCodes.INTERNAL_DATABASE_ERROR), result, "INTERNAL_DATABASE_ERROR");//13
		
		//AldTODO Task114 : Change Picture
		//TASK Task114 : Satna & Paya  
//		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2117", "2517", null, Integer.parseInt(ISOResponseCodes.SATNA_INCORRECT_AMOUNT), result, "SATNA_PAYA_INCORRECT_AMOUNT");//13
		//TASK Task114 : Satna & Paya  
//		createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2117", "2517", null, Integer.parseInt(ISOResponseCodes.PAYA_INCORRECT_AMOUNT), result, "SATNA_PAYA_INCORRECT_AMOUNT");//13

		
		/********** General Not-Continued States **********/
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.EXPIRY_DATE_MISMATCH), result, "EXPIRY_DATE_MISMATCH"); //41
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.ACQUIRER_NACK), result, "ACQUIRER_NACK"); //43
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75
		
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2123", "2523", null, ATMErrorCodes.ATM_CACH_HANDLER, result, "ATM_CACH_HANDLER");
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.WARM_CARD), result, "WARM_CARD"); //14
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.DUPLICATE_LINKED_ACCOUNT), result, "DUPLICATE_LINKED_ACCOUNT"); //33
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.SAF_TRANSMIT_MODE), result, "SAF_TRANSMIT_MODE"); //54
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.INVALID_MERCHANT), result, "INVALID_MERCHANT"); //78
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.BAD_TRANSACTION_TYPE), result, "BAD_TRANSACTION_TYPE"); //39
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.NO_TRANSACTION_ALLOWED), result, "NO_TRANSACTION_ALLOWED"); //66
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.HONOUR_WITH_ID), result, "HONOUR_WITH_ID"); //79
//		TASK Task015 : HotCard
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2116", "2516", null, Integer.parseInt(ISOResponseCodes.HOTCARD_NOT_APPROVED), result, "HOTCARD_NOT_APPROVED"); //105
//		TASK Task015 : HotCard
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2106", "2506", null, ATMErrorCodes.ATM_EJECT_CARD, result, "ATM_EJECT_CARD"); //127
//		//Mirkamali(Task174): Manadatory change PIN
//		createATMResponse("355", "088", "488", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2097", "2497", null, Integer.parseInt(ISOResponseCodes.MANDATORY_CHANGE_PIN), result, "Mandatory_change_PIN");
//		/********** APPROVED Results **********/	
		return result;
	}
	
	private Map<Integer, ATMResponse> sharedResponsesCreditPasargad() throws Exception {
		List<Receipt> receiptListCapture = getCardCaptureReciept();
		List<Receipt> receiptListNotAcceptable=notAcceptableRequestReciept();
		
		Map<Integer, ATMResponse> result = new HashMap<Integer, ATMResponse>();
		
		/********** General Continued States **********/
		createATMResponse("139", "034", "434", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, null, null, null, Integer.parseInt(ISOResponseCodes.HOST_LINK_DOWN), result, "HOST_LINK_DOWN"); //55
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.BANK_LINK_DOWN), result, "BANK_LINK_DOWN"); //57
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_TIMEOUT), result, "TRANSACTION_TIMEOUT"); //58
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE), result, "TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE"); //61
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.ACQUIRER_REVERSAL), result, "ACQUIRER_REVERSAL"); //36
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION), result, "TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION"); //62
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2083", "2483", null, Integer.parseInt(ISOResponseCodes.CASH_TRANSACTION_NOT_ALLOWED), result, "CASH_TRANSACTION_NOT_ALLOWED"); //65
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2565", null, Integer.parseInt(ISOResponseCodes.INVALID_CARD_STATUS), result, "INVALID_CARD_STATUS"); //12
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2565", null, ATMErrorCodes.DEFAULT_RESPONSE_CODE, result, "DEFAULT_RESPONSE_CODE");
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2122", "2522", null, ATMErrorCodes.ATM_NOT_SUFFICIENT_AMOUNT, result, "ATM_NOT_SUFFICIENT_AMOUNT");
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2167", "2567", null, ATMErrorCodes.ATM_NOT_ROUND_AMOUNT, result, "ATM_NOT_ROUND_AMOUNT"); 
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2161", "2561", null, Integer.parseInt(ISOResponseCodes.INVALID_CURRENCY_CODE), result, "INVALID_CURRENCY_CODE"); //19
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2161", "2561", null, Integer.parseInt(ISOResponseCodes.PERMISSION_DENIED), result, "PERMISSION_DENIED"); //94
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.FIELD_ERROR), result, "FIELD_ERROR"); //09
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.INVALID_TO_ACCOUNT), result, "INVALID_TO_ACCOUNT"); //68
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.NO_PIN_KEY), result, "NO_PIN_KEY"); //84
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.CUSTOMER_NOT_FOUND), result, "CUSTOMER_NOT_FOUND"); //90
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2164", "2564", null, Integer.parseInt(ISOResponseCodes.INVALID_TO_ACCOUNT), result, "INVALID_TO_ACCOUNT"); //91
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2166", "2566", null, Integer.parseInt(ISOResponseCodes.HOST_NOT_PROCESSING), result, "HOST_NOT_PROCESSING"); //51
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2157", "2557", null, Integer.parseInt(ISOResponseCodes.BAD_EXPIRY_DATE), result, "BAD_EXPIRY_DATE"); //97
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2157", "2557", null, Integer.parseInt(ISOResponseCodes.ORIGINAL_AMOUNT_INCORRECT), result, "ORIGINAL_AMOUNT_INCORRECT"); //98
		
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2082", "2482", null, Integer.parseInt(ISOResponseCodes.ORIGINAL_DATA_ELEMENT_MISMATCH), result, "ORIGINAL_DATA_ELEMENT_MISMATCH"); //99
		
		//TASK Task027 : Exceed Max Dispense Note
//		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2052", "2452", null, ATMErrorCodes.ATM_EXCEED_MAX_DISPENSE_NOTES, result, "ATM_EXCEED_MAX_DISPENSE_NOTES"); //130 
//Comment in 92.07.08		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2050", "2450", null, ATMErrorCodes.ATM_EXCEED_MAX_DISPENSE_NOTES, result, "ATM_EXCEED_MAX_DISPENSE_NOTES"); //130 
		//AldTODO Task057 : Change picture
		//TASK Task057 : ATM Topup 
//		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2115", "2515", null, Integer.parseInt(ISOResponseCodes.INVALID_CELLPHONE_NUMBER), result, "INVALID_CELLPHONE_NUMBER"); //106 
		//AldTODO Task057 : Change picture
		//TASK Task057 : ATM Topup 
		createATMResponse("198", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2117", "2517", null, Integer.parseInt(ISOResponseCodes.INTERNAL_DATABASE_ERROR), result, "INTERNAL_DATABASE_ERROR");//13
		
		/********** General Not-Continued States **********/
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.EXPIRY_DATE_MISMATCH), result, "EXPIRY_DATE_MISMATCH"); //41
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.ACQUIRER_NACK), result, "ACQUIRER_NACK"); //43
		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
//		createATMResponse("707", "386", "786", doCapture, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, doCapture ? "2103" : null, doCapture ? "2503" : null, doCapture ? receiptListCapture : null, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75
		
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2123", "2523", null, ATMErrorCodes.ATM_CACH_HANDLER, result, "ATM_CACH_HANDLER");
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.WARM_CARD), result, "WARM_CARD"); //14
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.DUPLICATE_LINKED_ACCOUNT), result, "DUPLICATE_LINKED_ACCOUNT"); //33
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.SAF_TRANSMIT_MODE), result, "SAF_TRANSMIT_MODE"); //54
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.INVALID_MERCHANT), result, "INVALID_MERCHANT"); //78
		
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.BAD_TRANSACTION_TYPE), result, "BAD_TRANSACTION_TYPE"); //39
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.NO_TRANSACTION_ALLOWED), result, "NO_TRANSACTION_ALLOWED"); //66
		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.HONOUR_WITH_ID), result, "HONOUR_WITH_ID"); //79
//		TASK Task015 : HotCard
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2116", "2516", null, Integer.parseInt(ISOResponseCodes.HOTCARD_NOT_APPROVED), result, "HOTCARD_NOT_APPROVED"); //105
//		TASK Task015 : HotCard
//		createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2106", "2506", null, ATMErrorCodes.ATM_EJECT_CARD, result, "ATM_EJECT_CARD"); //127
		//Mirkamali(Task174): Manadatory change PIN
//		createATMResponse("253", "088", "488", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2097", "2497", null, Integer.parseInt(ISOResponseCodes.MANDATORY_CHANGE_PIN), result, "Mandatory_change_PIN");
		/********** Success Results **********/
		
		return result;
	}
	/*************/
	private List<Receipt> notAcceptableRequestReciept(){//??????????????????????????
		List<Receipt> receiptListNotAcceptable = new ArrayList<Receipt>();
		
		Receipt receiptNotAcceptableFa = new Receipt();
		receiptNotAcceptableFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptNotAcceptableFa.setText("[GR simpleNotAcceptableRequestReceiptFa()]"
//				headerFa
//				+ "[LF][LF][GR center(GR c2F('رسید مشكل امنيتي'))]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ "[GR center(GR c2F('درخواست شما به دلايل امنيتي قابل اجرا نميباشد'))]"
//				+ "[GR putLF(15)]"
//				+ footerFa
		);
		receiptNotAcceptableFa.setLanguage(UserLanguage.FARSI_LANG);
		receiptNotAcceptableFa.setName("مشكل امنيتي-فارسی");
		getGeneralDao().saveOrUpdate(receiptNotAcceptableFa);
		receiptListNotAcceptable.add(receiptNotAcceptableFa);

		Receipt receiptNotAcceptableEn = new Receipt();
		receiptNotAcceptableEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptNotAcceptableEn.setText(headerEn
				+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Security Problem Reciept')]" : "[LF][LF][GR center(GR c2NCRE('Security Problem Reciept'))]")
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ formatAppPanEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ (!atmType.equals(ATMType.NCR) ? "[GR center('The requested service is not acceptable for security reasons.')]" : "[GR center(GR c2NCRE('The requested service is not acceptable for security reasons.'))]")
				+ "[GR putLF(15)]"
				+ footerEn
		);
		receiptNotAcceptableEn.setLanguage(UserLanguage.ENGLISH_LANG);
		receiptNotAcceptableEn.setName("مشكل امنيتي-انگلیسی");
		getGeneralDao().saveOrUpdate(receiptNotAcceptableEn);
		receiptListNotAcceptable.add(receiptNotAcceptableEn);
		return receiptListNotAcceptable;
	}
	/*************/
	private List<Receipt> getCardCaptureReciept() {
		List<Receipt> receiptListCapture = new ArrayList<Receipt>();
		
		Receipt receiptCaptureFa = new Receipt();
		receiptCaptureFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptCaptureFa.setText("[GR simpleCardCaptureReceiptFa()]");
//				headerFa
//				+ "[LF][LF][GR center(GR c2F('رسید ضبط کارت'))]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ "[GR center(GR c2F('کارت شما به دلایل امنیتی ضبط شد'))]"
//				+ "[GR putLF(15)]"
//				+ footerFa
//		);
		receiptCaptureFa.setLanguage(UserLanguage.FARSI_LANG);
		receiptCaptureFa.setName("ضبط کارت-فارسی");
		getGeneralDao().saveOrUpdate(receiptCaptureFa);
		receiptListCapture.add(receiptCaptureFa);

		Receipt receiptCaptureEn = new Receipt();
		receiptCaptureEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
		receiptCaptureEn.setText(headerEn
				+ (!atmType.equals(ATMType.NCR) ? "[LF][LF][GR center('Card Capture Reciept')]" : "[LF][LF][GR center(GR c2NCRE('Card Capture Reciept'))]")
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ formatAppPanEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ (!atmType.equals(ATMType.NCR) ? "[GR center('The card has been captured for security reasons.')]" : "[GR center(GR c2NCRE('The card has been captured for security reasons.'))]")
				+ "[GR putLF(15)]"
				+ footerEn
		);
		receiptCaptureEn.setLanguage(UserLanguage.ENGLISH_LANG);
		receiptCaptureEn.setName("ضبط کارت-انگلیسی");
		getGeneralDao().saveOrUpdate(receiptCaptureEn);
		receiptListCapture.add(receiptCaptureEn);
		return receiptListCapture;
	}

	private void createATMResponse(String nextState, String nextScreenFa, String nextScreenEn, 
			boolean captureCard, NDCFunctionIdentifierConstants functionCommand, String updateScreenFa, 
			String updateScreenEn, List<Receipt> receiptList, int rsCode, 
			Map<Integer, ATMResponse> result, String desc) {
		
		if (updateScreenFa != null) {
			updateScreenFa = nextScreenFa + "[FF][SI]@@[ESC]P" + updateScreenFa + "[ESC]\\";
		}
		if (updateScreenEn != null) {
			updateScreenEn = nextScreenEn + "[FF][SI]@@[ESC]P" + updateScreenEn + "[ESC]\\";
		}

		FunctionCommandResponse response;
		List<ResponseScreen> screenList = new ArrayList<ResponseScreen>();
		
		ResponseScreen screenFa = new ResponseScreen();
		screenFa.setScreenno(nextScreenFa);
		screenFa.setDesc(desc + "-فارسی");
		screenFa.setLanguage(UserLanguage.FARSI_LANG);
		screenFa.setScreenData(updateScreenFa);
		screenList.add(screenFa);
		getGeneralDao().saveOrUpdate(screenFa);
		
		ResponseScreen screenEn = new ResponseScreen();
		screenEn.setScreenno(nextScreenEn);
		screenEn.setDesc(desc + "-انگلیسی");
		screenEn.setLanguage(UserLanguage.ENGLISH_LANG);
		screenEn.setScreenData(updateScreenEn);
		screenList.add(screenEn);
		getGeneralDao().saveOrUpdate(screenEn);

		response = new FunctionCommandResponse(nextState, captureCard, functionCommand);
		response.setScreen(screenList);
		
		if (receiptList != null)
			response.setReceipt(receiptList);

		getGeneralDao().saveOrUpdate(response);
		result.put(rsCode, response);
	}

	public static HashMap<String, String> getFunctionsList(){ 
		return allFunctionsList;
	}
	
	public static void setSelectedFunctionList(Object[] functions){
		if (selectedFunctionsList == null)
			selectedFunctionsList = new HashMap<String, String>();
		 
		for (Object value : functions){
			for (String key : allFunctionsList.keySet()){
				if (allFunctionsList.get(key).equals((String) value)) {
					selectedFunctionsList.put(key, (String) value);
					break;
				}
			}
		}
	}
	

	public final static class FITType {
		final static Integer SHETAB = 1;
		final static Integer PASARGAD = 2;
		final static Integer CREDIT_PASARGAD = 3;
	}
	
	public final static class ATMType {
		final static Integer OTHER = 0;
		final static Integer NCR = 1;
		
		public static Integer getATMType(String atmType){
			if (atmType.toLowerCase().equals("ncr"))
				return ATMType.NCR;
			else 
				return ATMType.OTHER;
		}
	}
	
	public final static class BANKType {
		final static Integer PASARGAD = 0;
		final static Integer TAAVON = 1;
		final static Integer GARDESHGARI = 2;
		final static Integer RESALAT = 3;
		
		public static Integer getBankType(String bankType){
			if (bankType.equals("پاسارگاد"))
				return BANKType.PASARGAD;
			else if (bankType.equals("تعاون"))
				return BANKType.TAAVON;
			else if (bankType.equals("گردشگری"))
				return BANKType.GARDESHGARI;
			else if (bankType.equals("رسالت"))
				return BANKType.RESALAT;
			else return null;
		}
	}	
	
	public GeneralDao getGeneralDao(){
		return GeneralDao.Instance;
	}
	
	public static String readString() {
		int ch;
		String r = "";
		boolean done = false;
		while (!done) {
			try {
				ch = System.in.read();
				if (ch < 0 || (char) ch == '\n')
					done = true;
				else
					r = r + (char) ch;
			} catch (java.io.IOException e) {
				done = true;
			}
		}
		return r;
	}
	
	private void updatePasargadConfig() throws Exception {
		
	}

}
