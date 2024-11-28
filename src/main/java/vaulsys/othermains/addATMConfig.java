package vaulsys.othermains;

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
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addATMConfig {
	private static final int CURRENCY_RIAL_CODE = 364;
	private static final Integer MAX_DESPENSING_NOTES = 40;
	private static final Integer RECEIPT_LINE_LENGTH = 40;
	private static final Integer RECEIPT_LEFT_MARGIN = 3;
	private static final String ATM_TERMINAL_GROUP_NAME = "خودپردازها";
	private static final String PARENT_TERMINAL_GROUP_NAME = "همه ترمینالها";

	private static final String CONFIG_BANK_NAME_FA = "بانک پاسارگاد";
	private static final String CONFIG_BANK_NAME_EN = "Bank Pasargad";
	private static final String CONFIG_BANK_MOUNTTO_FA = "بانک هزاره سوم";
	private static final String CONFIG_BANK_MOUNTTO_EN = "Bank Of The 3rd. Millennium";

//	private static final String CONFIG_BANK_NAME_FA = "بانک تات";
//	private static final String CONFIG_BANK_NAME_EN = "Tat Bank";
//	private static final String CONFIG_BANK_MOUNTTO_FA = "تجربه، اعتماد، توسعه";
//	private static final String CONFIG_BANK_MOUNTTO_EN = "Bank Of The 3rd Millennium";


//	private static final String CONFIG_BANK_NAME_FA = "موسسه مالي و اعتباري رضوي" ;

//	private static final String CONFIG_BANK_NAME_FA = "بانک توسعه تعاون";
//	private static final String CONFIG_BANK_NAME_EN = "Tosee Tavon Bank";
//	private static final String CONFIG_BANK_MOUNTTO_FA = "همتی برتر برای فردایی بهتر";
//	private static final String CONFIG_BANK_MOUNTTO_EN = " ";


//	private static final String CONFIG_BANK_NAME_FA = "بانک گردشگردی";
//	private static final String CONFIG_BANK_NAME_EN = "Tourism Bank";
//	private static final String CONFIG_BANK_MOUNTTO_FA = "بانک گردشگردی";
//	private static final String CONFIG_BANK_MOUNTTO_EN = "Tourism Bank";

	private static final String ATM_ENGLISH_ENCODING = "1";
	private static final String ATM_RECEIPT_CONVERTOR = "STANDARD_NDC_CONVERTOR";
	private static final String ATM_SCREEN_CONVERTOR = "FANAP_NDC_CONVERTOR";
	private static final String ATM_FARSI_RCPT_ENCODING = "7";
	private static final String ATM_FARSI_EXT_RCPT_ENCODING = null;
	private static final String ATM_FARSI_SCREEN_ENCODING = "K";
	private static final String ATM_FARSI_EXT_SCREEN_ENCODING = "K";


	private static final String BANK_NAME_FA = "GR c2F(GR bnkName2F())";
	private static final String BANK_NAME_EN = "GR bnkName2E()";
	private static final String BANK_MOUNTTO_FA = "GR c2F(GR bnkMount2F())";
	private static final String BANK_MOUNTTO_EN = "GR bnkMount2E()";

	private static Currency currency ;

	private static final String lineFa = "[ESC](1[GR hr(0xcd)][ESC](7";
	private static final String lineEn = "[ESC](1[GR hr(0xcd)][ESC](1";

//	private static final String openDoubleQuotationFa = "[ESC](1[0xAF][ESC](7";
//	private static final String closeDoubleQuotationFa = "[ESC](1[0xAE][ESC](7";
	private static final String openDoubleQuotationEn = "[0xAE]";
	private static final String closeDoubleQuotationEn = "[0xAF]";

	private static final String newLine = "[LF]";

	private static final String headerFa =
		"[ESC](7[GR center("+BANK_NAME_FA+") ]"
//		+ "[LF][LF][GR center(GR c2F('خودپرداز ' + GR ifx.Name + ' ' + GR ifx.TerminalId ))]";
		+ "[LF][LF][GR center(GR c2F('خودپرداز ' + GR atm.getOwner().getName() + ' ' + GR ifx.TerminalId ))]";

	private static final String headerEn = "[GR center("+BANK_NAME_EN+")]"
										+ "[LF][LF][GR center(GR ' ATM '+ GR atm.getOwner().getNameEn() +' '+GR ifx.TerminalId)]";

	private static final String footerFa = "[ESC](1[GR hr(0xcd)][ESC](7"
//										+ "[GR justify(GR "+BANK_MOUNTTO_FA+",GR "+BANK_MOUNTTO_FA+" )]"
										+ "[GR center(GR "+BANK_MOUNTTO_FA+" )]"
										+ "[FF]";
	private static final String footerEn = "[LF][GR hr(0xcd)]"
//										+ "[GR justify(GR "+BANK_MOUNTTO_EN+" , "+BANK_NAME_EN+")]"
										+ "[GR center(GR "+BANK_NAME_EN+")]"
										+ "[FF]";

	private static final String receivedDateFa = "[GR datePersianFormat(ifx.receivedDt)]";
	private static final String receivedDateEn = "[GR dateEnglishFormat(ifx.receivedDt)]";

	private static final String formatAppPanFa = "[GR justify(GR c2F('شماره کارت'), GR appPanFa(ifx.actualAppPAN))]";
	private static final String formatAppPanEn = "[GR justify(GR appPanEn(ifx.actualAppPAN), 'Card No.')]";

	private static final String transferAppPanFa = "[GR justify(GR c2F('از کارت شماره'), GR appPanFa(ifx.actualAppPAN))]";
	private static final String transferAppPanEn = "[GR justify(GR appPanEn(ifx.actualAppPAN), 'From Card No.')]";

	private static final String transferSecAppPanFa = "[GR justify(GR c2F('به کارت شماره'), GR appPanFa(ifx.actualSecondAppPan))]";
	private static final String transferSecAppPanEn = "[GR justify(GR appPanEn(ifx.actualSecondAppPan), 'To Card No.')]";

	private static final String seqCntrFa = "[GR justify(GR c2F('شماره پیگیری'), GR c2F(ifx.Src_TrnSeqCntr))]";
	private static final String seqCntrEn = "[GR justify(GR ifx.Src_TrnSeqCntr, 'Reference No.')]";

	private static final String amountFa = "[GR justify(GR c2F('مبلغ'), GR amount2F(ifx.Auth_Amt, 15))]";
//	private static final String amountEn = "[GR justify(GR amount2E(ifx.Auth_Amt,15) + ' Rials', 'Amount')]";
	private static final String amountEn = "[GR justify(GR amount2E(ifx.Auth_Amt,15) , 'Amount')]";


	private static final String amountPartialFa = "[GR justify(GR c2F('مبلغ'), GR amount2F(partialDispense(ifx),15))]";
//	private static final String amountPartialEn = "[GR justify(GR partialDispense(ifx) + ' Rials', 'Amount')]";
	private static final String amountPartialEn = "[GR justify(GR partialDispense(ifx) , 'Amount')]";


	private static final String accBalLedgerFa = "[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, 15))]";
	private static final String accBalLedgerEn = "[GR justify(GR amount2E(ifx.AcctBalLedgerAmt, 15), 'Ledger Amount')]";
	private static final String accBalAvailableFa = "[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, 15))]";
	private static final String accBalAvailableEn = "[GR justify(GR amount2E(ifx.AcctBalAvailableAmt, 15), 'Available Amount')]";
	private static final String subAccFa = "[GR justify(GR c2F( GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'شماره حساب')), GR (accountFormat(ifx.subsidiaryAccFrom)))]";
	private static final String subAccEn = "[GR justify(accountFormat(ifx.subsidiaryAccFrom), GR subsidiaryState2F(ifx.subsidiaryAccFrom, 'Account Number'))]";

	private static final String accBalLedgerCashFa = "[GR justify(GR c2F('مانده حساب'), GR amount2F(ifx.AcctBalLedgerAmt, ifx, 15))]";
//	private static final String accBalLedgerCashEn = "[GR justify(GR amount2E(ifx.AcctBalLedgerAmt, ifx, 15) + ' Rials', 'Ledger Amount')]";
	private static final String accBalLedgerCashEn = "[GR justify(GR amount2E(ifx.AcctBalLedgerAmt, ifx, 15) , 'Ledger Amount')]";
	private static final String accBalAvailableCashFa = "[GR justify(GR c2F('قابل برداشت'), GR amount2F(ifx.AcctBalAvailableAmt, ifx, 15))]";
	private static final String accBalAvailableCashEn = "[GR justify(GR amount2E(ifx.AcctBalAvailableAmt, ifx, 15) + ' Rials', 'Available Amount')]";
	/******for Taavon******/
//	private static final String updateablePageFontColor = "00;67;80m";
	private static final String updateablePageFontColor = "00;80m";
	public static void main(String[] args)throws IOException{
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		try {
			currency = GlobalContext.getInstance().getCurrency(CURRENCY_RIAL_CODE);
//			currency = ProcessContext.get().getCurrency(CURRENCY_RIAL_CODE);
			new addATMConfig().addConfig();
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}finally{
			GeneralDao.Instance.endTransaction();
			System.exit(0);
		}
	}
	public void addConfig() throws Exception {
    	currency = DBInitializeUtil.findCurrency(CURRENCY_RIAL_CODE);
    	TerminalGroup atmTerminalGroup = getTerminalGroup(ATM_TERMINAL_GROUP_NAME);
//        addConfig(atmTerminalGroup);
    	thirdPartyAndTenThousandAndEnglishCofigWithMCIDefinition(atmTerminalGroup);
//        addNewConfig(atmTerminalGroup);
    }
	private TerminalGroup getTerminalGroup(String name) {
    	TerminalGroup group = findTerminalGroup(name);
    	if (group != null)
    		return group;

    	return createTerminalGroup(name);
    }
	 private TerminalGroup findTerminalGroup(String name) {
	    	String query = "from " + TerminalGroup.class.getName() + " ap where ap.name = :name ";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("name", name);
	        return (TerminalGroup) getGeneralDao().findObject(query, param);
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
		//	terminalGroup.setCreatorUser(ProcessContext.get().getSwitchUser());
			terminalGroup.setCreatedDateTime(DateTime.now());

			getGeneralDao().saveOrUpdate(terminalGroup);

			return terminalGroup;
		}
	 private void thirdPartyAndTenThousandAndEnglishCofigWithMCIDefinition(TerminalGroup atmTerminalGroup)throws Exception{
		   ATMConfiguration configuration = new ATMConfiguration();

			configuration.setName("تنظیمات پیش فرض خودپرداز با تعریف همراه اول و انگلیسی و ده هزار تومانی و کمک به خیریه ");
//			configuration.setName("تنظیمات پیش فرض خودپردازهاي موسسه مالي و اعتباري رضوي");
			configuration.setMaxDespensingNotes(MAX_DESPENSING_NOTES);
			configuration.setReceiptLineLength(RECEIPT_LINE_LENGTH);
			configuration.setReceiptLeftMargin(RECEIPT_LEFT_MARGIN);
			configuration.setBnkFarsiName(CONFIG_BANK_NAME_FA);
			configuration.setBnkFarsiMount(CONFIG_BANK_MOUNTTO_FA);
			configuration.setBnkEnglishName(CONFIG_BANK_NAME_EN);
			configuration.setBnkEnglishMount(CONFIG_BANK_MOUNTTO_EN);

			configuration.setId(6L);

			configuration.setCassetteADenomination(500000);
			configuration.setCassetteACurrency(currency);
			configuration.setCassetteBDenomination(100000);
			configuration.setCassetteBCurrency(currency);
			configuration.setCassetteCDenomination(50000);
			configuration.setCassetteCCurrency(currency);
			configuration.setCassetteDDenomination(20000);
			configuration.setCassetteDCurrency(currency);

			configuration.setEnglish_encoding(ATM_ENGLISH_ENCODING);
			configuration.setFarsi_reciept_encoding(ATM_FARSI_RCPT_ENCODING);
			configuration.setFarsi_extended_reciept_encoding(ATM_FARSI_EXT_RCPT_ENCODING);
			configuration.setFarsi_screen_encoding(ATM_FARSI_SCREEN_ENCODING);
			configuration.setFarsi_extended_screen_encoding(ATM_FARSI_EXT_SCREEN_ENCODING);

			configuration.setReceiptConvertor(ATM_RECEIPT_CONVERTOR);
			configuration.setScreenConvertor(ATM_SCREEN_CONVERTOR);

			getGeneralDao().saveOrUpdate(configuration);

//			atmTerminalGroup.getSharedFeature().setConfiguration(configuration);

			List<ATMRequest> requests = new ArrayList<ATMRequest>();

			ResponseScreen screenFa = new ResponseScreen();
			screenFa.setLanguage(UserLanguage.FARSI_LANG);
			screenFa.setScreenno(null);
			screenFa.setScreenData("[FF][SI]@@[ESC]P2068[ESC]\\"
					+ "[ESC](K"
//					+ "[ESC][OC]00;67;80m");
					+ "[ESC][OC]"
					+ updateablePageFontColor);
			screenFa.setDesc("لیست حساب های فرعی- فارسی");
			getGeneralDao().saveOrUpdate(screenFa);


			ResponseScreen screenEn = new ResponseScreen();
			screenEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenEn.setScreenno(null);
			screenEn.setScreenData("[FF][SI]@@[ESC]P2468[ESC]\\"
					+ "[ESC](1"
//					+ "[ESC][OC]00;67;80m");
					+ "[ESC][OC]"
					+ updateablePageFontColor);
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


			List<ATMRequest> requestsWithdrawal = Withdrawal(configuration, responsePasargadSubsidiary);
			if (requestsWithdrawal != null)
				requests.addAll(requestsWithdrawal);

			List<ATMRequest> requestsPartialDispense = PartialDispense(configuration, responsePasargadSubsidiary);
			if (requestsPartialDispense != null)
				requests.addAll(requestsPartialDispense);

			List<ATMRequest> requestsBalance = Balance(configuration, responsePasargadSubsidiary);
			if (requestsBalance != null)
				requests.addAll(requestsBalance);

			List<ATMRequest> requestsBankStatement = BankStatement(configuration, responsePasargadSubsidiary);
			if (requestsBankStatement != null)
				requests.addAll(requestsBankStatement);

//			List<ATMRequest> requestsCancel = Cancel(configuration);
//			if (requestsCancel != null)
//				requests.addAll(requestsCancel);

			List<ATMRequest> requestsBillpayment = Billpayment(configuration, responsePasargadSubsidiary);
			if (requestsBillpayment != null)
				requests.addAll(requestsBillpayment);

			List<ATMRequest> requestsCharge = purchaseCharge(configuration, responsePasargadSubsidiary);
			if (requestsCharge != null)
				requests.addAll(requestsCharge);


			List<ATMRequest> requestsMCICharge = purchaseMCICharge(configuration, responsePasargadSubsidiary);
			if (requestsMCICharge != null)
				requests.addAll(requestsMCICharge);

			List<ATMRequest> requestsTransfer = Transfer(configuration, responsePasargadSubsidiary);
			if (requestsTransfer != null)
				requests.addAll(requestsTransfer);

//			List<ATMRequest> requestsTransferToAccount = TransferToAccount(configuration, responsePasargadSubsidiary);
//			if (requestsTransferToAccount != null)
//				requests.addAll(requestsTransferToAccount);

			//		List<ATMRequest> requestsTransferFromCardToAccount = TransferFromCardToAccount(configuration, responsePasargadSubsidiary);
//			if (requestsTransferFromCardToAccount != null)
//				requests.addAll(requestsTransferFromCardToAccount);

			List<ATMRequest> requestsChangePinBlock = ChangePinBlock(configuration);
			if (requestsChangePinBlock != null)
				requests.addAll(requestsChangePinBlock);

			List<ATMRequest> requestsChangeInternetPinBlock = ChangeInternetPinBlock(configuration);
			if (requestsChangeInternetPinBlock != null)
				requests.addAll(requestsChangeInternetPinBlock);

			List<ATMRequest> requestsCreditStatementData = CreditStatementData(configuration);
			if (requestsCreditStatementData != null)
				requests.addAll(requestsCreditStatementData);

			List<ATMRequest> requestsThirdPArtyPayment = thirdPartyPayment(configuration, responsePasargadSubsidiary);
			if(requestsThirdPArtyPayment != null)
				requests.addAll(requestsThirdPArtyPayment);
			for (ATMRequest atmRequest : requests) {
				configuration.addRequset(atmRequest);
				getGeneralDao().saveOrUpdate(atmRequest);
			}

			/*********** Error Receipt ************/
//			String textError = "[LF][SO]5[ESC][LF]TRANSACTION NOT SUCCESSFUL[LF][LF]BE OMIDE DIDAR[FF]";
//			ArrayList<Receipt> receiptListError = new ArrayList<Receipt>();
//			Receipt receiptError = new Receipt();
//			receiptListError.add(receiptError);
//			receiptError.setText(textError);
//			receiptError.setPrinterFlag(NDCPrinterFlag.DONT_PRINT);

//			getGeneralDao().save(receiptError);
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
	 /***********Transactions***********/
	 public List<ATMRequest> Withdrawal(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
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

			String customerReceiptFaText ="[GR simpleWithdrawalReceiptFa()]"
				/*"[ESC][OC]4q[ESC][OC]2p" +*/
				/*headerFa
				+ "[LF][LF][GR center(GR c2F('رسید برداشت وجه')) ]"
				+ newLine
				+ lineFa
				+ receivedDateFa
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ formatAppPanFa
//				+ newLine + newLine
				+ newLine
				+ seqCntrFa
//				+ newLine + newLine
				+ newLine
				+ amountFa
//				+ newLine + newLine
				+ newLine
				+ accBalLedgerFa
//				+ newLine + newLine
				+ newLine
				+ accBalAvailableFa
//				+ newLine + newLine
				+ newLine
				+ subAccFa
				+ "[GR putLF(8)]"
				+ footerFa*/
				;

			String customerReceiptEnText = /*"[ESC][OC]4q[ESC][OC]2p" +*/
				headerEn
				+ "[LF][LF][GR center('Withdrawal Receipt')]"
//				+ newLine + newLine
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
				+ amountEn
//				+ newLine + newLine
				+ newLine
				+ accBalLedgerEn
//				+ newLine + newLine
				+ newLine
				+ accBalAvailableEn
//				+ newLine + newLine
				+ newLine
				+ subAccEn
				+ "[GR putLF(8)]"
				+ footerEn;

			String journalReceiptText = "[GR simpleWithdrawalJournal()]" ;
//				"[LF]Withdrawal:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";

			ArrayList<Receipt> receiptList = new ArrayList<Receipt>();

			Receipt customerReceiptFa = new Receipt();
			customerReceiptFa.setText(customerReceiptFaText);
			customerReceiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			customerReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
			getGeneralDao().save(customerReceiptFa);
			receiptList.add(customerReceiptFa);

			Receipt journalReceipt = new Receipt();
			journalReceipt.setText(journalReceiptText);
			journalReceipt.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
			getGeneralDao().save(journalReceipt);
			receiptList.add(journalReceipt);

			Receipt customerReceiptEn = new Receipt();
			customerReceiptEn.setText(customerReceiptEnText);
			customerReceiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			customerReceiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
			getGeneralDao().save(customerReceiptEn);
			receiptList.add(customerReceiptEn);

			/*********************/
			atmRequestShetabFa.setOpkey("AAAA    ");
			atmRequestShetabFa.setIfxType(IfxType.WITHDRAWAL_RQ);
			atmRequestShetabFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestShetabFa.setCurrency(currency);
			atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestShetabFa.setFit(FITType.SHETAB);

			atmRequestTimeOutShetabFa.setOpkey("AAAA   F");
			atmRequestTimeOutShetabFa.setNextOpkey("AAAA   F");
			atmRequestTimeOutShetabFa.setIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			atmRequestTimeOutShetabFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestTimeOutShetabFa.setCurrency(currency);
			atmRequestTimeOutShetabFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestTimeOutShetabFa.setFit(FITType.SHETAB);

			/*********************/
			atmRequestShetabEn.setOpkey("IAAA    ");
			atmRequestShetabEn.setIfxType(IfxType.WITHDRAWAL_RQ);
			atmRequestShetabEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestShetabEn.setCurrency(currency);
			atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestShetabEn.setFit(FITType.SHETAB);

			atmRequestTimeOutShetabEn.setOpkey("IAAA   F");
			atmRequestTimeOutShetabEn.setNextOpkey("IAAA   F");
			atmRequestTimeOutShetabEn.setIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			atmRequestTimeOutShetabEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestTimeOutShetabEn.setCurrency(currency);
			atmRequestTimeOutShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestTimeOutShetabEn.setFit(FITType.SHETAB);

			/*********************/
			atmRequestPasargadFa.setOpkey("ABAAA   ");
			atmRequestPasargadFa.setIfxType(IfxType.WITHDRAWAL_RQ);
			atmRequestPasargadFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestPasargadFa.setCurrency(currency);
			atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestPasargadFa.setFit(FITType.PASARGAD);

			atmRequestTimeOutPasargadFa.setOpkey("ABAAA  F");
			atmRequestTimeOutPasargadFa.setNextOpkey("ABAAA  F");
			atmRequestTimeOutPasargadFa.setIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestTimeOutPasargadFa.setCurrency(currency);
			atmRequestTimeOutPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestTimeOutPasargadFa.setFit(FITType.PASARGAD);

			/*********************/
			atmRequestPasargadSubFa.setOpkey("ABAAB   ");
			atmRequestPasargadSubFa.setNextOpkey("ABAAA   ");
			atmRequestPasargadSubFa.setIfxType(IfxType.WITHDRAWAL_RQ);
			atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
			atmRequestPasargadSubFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestPasargadSubFa.setCurrency(currency);
			atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestPasargadSubFa.setFit(FITType.PASARGAD);

			atmRequestTimeOutPasargadSubFa.setOpkey("ABAAB  F");
			atmRequestTimeOutPasargadSubFa.setNextOpkey("ABAAB  F");
//					atmRequestTimeOutPasargadSubFa.setNextOpkey("ABAAA  F");
			atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//			atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestTimeOutPasargadSubFa.setCurrency(currency);
			atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);

			/*********************/
			atmRequestPasargadEn.setOpkey("IBAAA   ");
			atmRequestPasargadEn.setIfxType(IfxType.WITHDRAWAL_RQ);
			atmRequestPasargadEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestPasargadEn.setCurrency(currency);
			atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestPasargadEn.setFit(FITType.PASARGAD);

			atmRequestTimeOutPasargadEn.setOpkey("IBAAA  F");
			atmRequestTimeOutPasargadEn.setNextOpkey("IBAAA  F");
			atmRequestTimeOutPasargadEn.setIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestTimeOutPasargadEn.setCurrency(currency);
			atmRequestTimeOutPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestTimeOutPasargadEn.setFit(FITType.PASARGAD);

			/*********************/
			atmRequestPasargadSubEn.setOpkey("IBAAB   ");
			atmRequestPasargadSubEn.setNextOpkey("IBAAA   ");
			atmRequestPasargadSubEn.setIfxType(IfxType.WITHDRAWAL_RQ);
			atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
			atmRequestPasargadSubEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestPasargadSubEn.setCurrency(currency);
			atmRequestPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestPasargadSubEn.setFit(FITType.PASARGAD);

			atmRequestTimeOutPasargadSubEn.setOpkey("IBAAB  F");
			atmRequestTimeOutPasargadSubEn.setNextOpkey("IBAAB  F");
			atmRequestTimeOutPasargadSubEn.setIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
//			atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestTimeOutPasargadSubEn.setCurrency(currency);
			atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);

			/*********************/
			List<ResponseScreen> screen00 = new ArrayList<ResponseScreen>();
			ResponseScreen screen00Fa = new ResponseScreen();
			screen00Fa.setScreenno("394");
			screen00Fa.setDesc("برداشت یک مرحله ای-موفق فارسی");
			screen00Fa.setLanguage(UserLanguage.FARSI_LANG);
			screen00Fa.setScreenData(null);
			getGeneralDao().saveOrUpdate(screen00Fa);
			screen00.add(screen00Fa);

			ResponseScreen screen00En = new ResponseScreen();
			screen00En.setScreenno("794");
			screen00En.setDesc("برداشت یک مرحله ای-موفق انگلیسی");
			screen00En.setLanguage(UserLanguage.ENGLISH_LANG);
			screen00En.setScreenData(null);
			getGeneralDao().saveOrUpdate(screen00En);
			screen00.add(screen00En);


			FunctionCommandResponse atmResponse00Fa = new FunctionCommandResponse();
			atmResponse00Fa.setName("برداشت یک مرحله ای-موفق");
			atmResponse00Fa.setBeRetain(false);
			atmResponse00Fa.setFunctionCommand(NDCFunctionIdentifierConstants.CARD_BEFORE_CACH);
			atmResponse00Fa.setNextState("705");
			atmResponse00Fa.setScreen(screen00);
			atmResponse00Fa.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(atmResponse00Fa);

			List<ResponseScreen> screenTimeout = new ArrayList<ResponseScreen>();
			ResponseScreen screenTimeoutFa = new ResponseScreen();
			screenTimeoutFa.setScreenno("398");
			screenTimeoutFa.setDesc("برداشت یک مرحله ای-timeout فارسی");
			screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
			screenTimeoutFa.setScreenData(null);
			getGeneralDao().saveOrUpdate(screenTimeoutFa);
			screenTimeout.add(screenTimeoutFa);

			ResponseScreen screenTimeoutEn = new ResponseScreen();
			screenTimeoutEn.setScreenno("798");
			screenTimeoutEn.setDesc("برداشت یک مرحله ای-timeout انگلیسی");
			screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenTimeoutEn.setScreenData(null);
			getGeneralDao().saveOrUpdate(screenTimeoutEn);
			screenTimeout.add(screenTimeoutEn);

			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("برداشت یک مرحله ای-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
			responseTimeOut.setScreen(screenTimeout);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);
			/*********************/

			atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
			atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
			atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
			atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00Fa);
			atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			/*********************/
			List<ResponseScreen> screenShetab100 = new ArrayList<ResponseScreen>();
			ResponseScreen screenShetab100Fa = new ResponseScreen();
			screenShetab100Fa.setScreenno("390");
			screenShetab100Fa.setDesc("برداشت جندمرحله ای-مرحله اول-شتابی-موفق فارسی");
			screenShetab100Fa.setLanguage(UserLanguage.FARSI_LANG);
			screenShetab100Fa.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]00;67;80;m"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]H4[GR ifx.currentStep]"
					+ "[SI]H2[GR ifx.totalStep]");
			getGeneralDao().saveOrUpdate(screenShetab100Fa);
			screenShetab100.add(screenShetab100Fa);

			ResponseScreen screenShetab100En = new ResponseScreen();
			screenShetab100En.setScreenno("790");
			screenShetab100En.setDesc("برداشت جندمرحله ای-مرحله اول-شتابی-موفق انگلیسی");
			screenShetab100En.setLanguage(UserLanguage.ENGLISH_LANG);
			screenShetab100En.setScreenData("783[FF][SI]@@[ESC]P2509[ESC]\\"
					+ "[ESC](1[ESC][OC]80;m"
					+ "[SI]H6[GR ifx.totalStep]"
					+ "[GS]0000791[FF][SI]@@[ESC]P2552[ESC]\\"
					+ "[ESC](1[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]HH[GR ifx.currentStep]"
					+ "[SI]HL[GR ifx.totalStep]");
			getGeneralDao().saveOrUpdate(screenShetab100En);
			screenShetab100.add(screenShetab100En);


			FunctionCommandResponse atmResponseShetab100 = new FunctionCommandResponse();
			atmResponseShetab100.setName("برداشت جندمرحله ای-مرحله اول-شتابی-موفق");
			atmResponseShetab100.setBeRetain(false);
			atmResponseShetab100.setFunctionCommand(NDCFunctionIdentifierConstants.CARD_BEFORE_CACH);

			//TODO Partial Dispense
			atmResponseShetab100.setNextState("549");
//			atmResponseShetab100.setNextScreen("390");
//			atmResponseShetab100.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]H4[GR ifx.currentStep]"
//					+ "[SI]H2[GR ifx.totalStep]"
//					);
			atmResponseShetab100.setScreen(screenShetab100);
			getGeneralDao().saveOrUpdate(atmResponseShetab100);


			List<ResponseScreen> screenFirstPartialDispenseList = new ArrayList<ResponseScreen>();

			ResponseScreen screenFirstPartialDispenseFa = new ResponseScreen();
			screenFirstPartialDispenseFa.setScreenno("390");
			screenFirstPartialDispenseFa.setDesc("برداشت جندمرحله ای-مرحله اول-موفق-فارسی");
			screenFirstPartialDispenseFa.setLanguage(UserLanguage.FARSI_LANG);
			screenFirstPartialDispenseFa.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]00;67;80;m"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]H4[GR ifx.currentStep]"
					+ "[SI]H2[GR ifx.totalStep]");
			screenFirstPartialDispenseList.add(screenFirstPartialDispenseFa);
			getGeneralDao().saveOrUpdate(screenFirstPartialDispenseFa);

			ResponseScreen screenFirstPartialDispenseEn = new ResponseScreen();
			screenFirstPartialDispenseEn.setScreenno("790");
			screenFirstPartialDispenseEn.setDesc("برداشت جندمرحله ای-مرحله اول-موفق-انگلیسی");
			screenFirstPartialDispenseEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenFirstPartialDispenseEn.setScreenData("783[FF][SI]@@[ESC]P2509[ESC]\\"
					+ "[ESC](1[ESC][OC]80;m"
					+ "[SI]H6[GR ifx.totalStep]"
					+ "[GS]0000791[FF][SI]@@[ESC]P2552[ESC]\\"
//					+ "[ESC](1[ESC][OC]00;67;80;m"
					+ "[ESC](1[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]HH[GR ifx.currentStep]"
					+ "[SI]HL[GR ifx.totalStep]");
			screenFirstPartialDispenseList.add(screenFirstPartialDispenseEn);
			getGeneralDao().saveOrUpdate(screenFirstPartialDispenseEn);

			FunctionCommandResponse atmResponsePasargad100 = new FunctionCommandResponse();
			atmResponsePasargad100.setName("برداشت جندمرحله ای-مرحله اول-داخلی-موفق");
			atmResponsePasargad100.setBeRetain(false);
			atmResponsePasargad100.setFunctionCommand(NDCFunctionIdentifierConstants.CARD_BEFORE_CACH);

			//TODO Partial Dispense
			atmResponsePasargad100.setNextState("049");
//			atmResponsePasargad100.setNextScreen("390");
//			atmResponsePasargad100.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]H4[GR ifx.currentStep]"
//					+ "[SI]H2[GR ifx.totalStep]");
			atmResponsePasargad100.setScreen(screenFirstPartialDispenseList);
			getGeneralDao().saveOrUpdate(atmResponsePasargad100);

			atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponseShetab100);
			atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponseShetab100);
			atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponsePasargad100);
			atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponsePasargad100);
			//TODO responseWithPasargadSub00 must be changed?!
			atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponsePasargad100);
			atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponsePasargad100);

			getGeneralDao().saveOrUpdate(atmRequestShetabFa);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabFa);
			getGeneralDao().saveOrUpdate(atmRequestShetabEn);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutShetabEn);
			getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
			getGeneralDao().saveOrUpdate(atmRequestPasargadSubFa);
			getGeneralDao().saveOrUpdate(atmRequestPasargadSubEn);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubFa);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadSubEn);

			requests.add(atmRequestShetabFa);
			requests.add(atmRequestTimeOutShetabFa);
			requests.add(atmRequestShetabEn);
			requests.add(atmRequestTimeOutShetabEn);
			requests.add(atmRequestPasargadFa);
			requests.add(atmRequestTimeOutPasargadFa);
			requests.add(atmRequestPasargadEn);
			requests.add(atmRequestTimeOutPasargadEn);
			requests.add(atmRequestPasargadSubFa);
			requests.add(atmRequestPasargadSubEn);
			requests.add(atmRequestTimeOutPasargadSubFa);
			requests.add(atmRequestTimeOutPasargadSubEn);
			return requests;
		}
	 public List<ATMRequest> PartialDispense(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
			List<ATMRequest> requests = new ArrayList<ATMRequest>();
			ATMRequest atmRequestShetabFa = new ATMRequest();
			ATMRequest atmRequestShetabEn = new ATMRequest();
			ATMRequest atmRequestPasargadFa = new ATMRequest();
			ATMRequest atmRequestRevPasargadFa = new ATMRequest();
			ATMRequest atmRequestPasargadEn = new ATMRequest();
			ATMRequest atmRequestRevPasargadEn = new ATMRequest();

			String customerReceiptFaText = "[GR simpleTransferReceiptFa()]"
//				headerFa
//				+ "[LF][LF][GR center(GR c2F('رسید برداشت وجه')) ]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine
//				+ newLine
//				+ seqCntrFa
////				+ newLine + newLine
//				+ newLine
//				+ amountFa
////				+ newLine + newLine
//				+ newLine
//				+ accBalLedgerFa
////				+ newLine + newLine
//				+ newLine
//				+ accBalAvailableFa
////				+ newLine + newLine
//				+ newLine
//				+ subAccFa
//				+ "[GR putLF(8)]"
//				+ footerFa
				;


			String customerReceiptCashErrFaText = "[GR simplePartialDispenceErrorReceiptFa()]"
//				headerFa
//				+ "[LF][LF][GR center(GR c2F('رسید برداشت وجه')) ]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine
//				+ newLine
//				+ seqCntrFa
////				+ newLine + newLine
//				+ newLine
//				+ amountPartialFa
////				+ newLine + newLine
//				+ newLine
//				+ accBalLedgerCashFa
////				+ newLine + newLine
//				+ newLine
//				+ accBalAvailableCashFa
////				+ newLine + newLine
//				+ newLine
//				+ subAccFa
//				+ "[GR putLF(8)]"
//				+ footerFa
				;

			String customerReceiptEnText =
				headerEn
				+ "[LF][LF][GR center('Withdrawal Receipt')]"
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
				+ amountEn
//				+ newLine + newLine
				+ newLine
				+ accBalLedgerEn
//				+ newLine + newLine
				+ newLine
				+ accBalAvailableEn
//				+ newLine + newLine
				+ newLine
				+ subAccEn
				+ "[GR putLF(8)]"
				+ footerEn;

			String customerReceiptCashErrEnText =
				headerEn
//				+ newLine + newLine
				+ newLine
				+ newLine
				+ "[GR center('Withdrawal Receipt')]"
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
				+ amountPartialEn
//				+ newLine + newLine
				+ newLine
				+ accBalLedgerCashEn
//				+ newLine + newLine
				+ newLine
				+ accBalAvailableCashEn
//				+ newLine + newLine
				+ newLine
				+ subAccEn
				+ "[GR putLF(8)]"
				+ footerEn;

			String textJournal = "[GR simplePartialDispenceJournal()]";
//				"[LF]Partial Widthdrawal:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId][SO]1Step:[GR ifx.currentStep][SO]1Total Steps:[GR ifx.totalStep]";

			ArrayList<Receipt> receiptList = new ArrayList<Receipt>();
			ArrayList<Receipt> receiptCashErrList = new ArrayList<Receipt>();

			Receipt customerReceiptFa = new Receipt();
			customerReceiptFa.setText(customerReceiptFaText);
			customerReceiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			customerReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
			getGeneralDao().save(customerReceiptFa);
			receiptList.add(customerReceiptFa);

			Receipt journalReceiptFa = new Receipt();
			journalReceiptFa.setText(textJournal);
			journalReceiptFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
			getGeneralDao().save(journalReceiptFa);
			receiptList.add(journalReceiptFa);

			Receipt customerReceiptEn = new Receipt();
			customerReceiptEn.setText(customerReceiptEnText);
			customerReceiptEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			customerReceiptEn.setLanguage(UserLanguage.ENGLISH_LANG);
			getGeneralDao().save(customerReceiptEn);
			receiptList.add(customerReceiptEn);

			Receipt customerReceiptCashErrFa = new Receipt();
			customerReceiptCashErrFa.setText(customerReceiptCashErrFaText);
			customerReceiptCashErrFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			customerReceiptCashErrFa.setLanguage(UserLanguage.FARSI_LANG);
			getGeneralDao().save(customerReceiptCashErrFa);
			receiptCashErrList.add(customerReceiptCashErrFa);

			Receipt customerReceiptCashErrEn = new Receipt();
			customerReceiptCashErrEn.setText(customerReceiptCashErrEnText);
			customerReceiptCashErrEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			customerReceiptCashErrEn.setLanguage(UserLanguage.ENGLISH_LANG);
			getGeneralDao().save(customerReceiptCashErrEn);
			receiptCashErrList.add(customerReceiptCashErrEn);

			Receipt journalReceiptFa2 = new Receipt();
			journalReceiptFa2.setText(textJournal);
			journalReceiptFa2.setPrinterFlag(NDCPrinterFlag.PRINT_ON_JOURNAL_PRINTER_ONLY);
			getGeneralDao().save(journalReceiptFa2);
			receiptList.add(journalReceiptFa2);
			receiptCashErrList.add(journalReceiptFa2);


			atmRequestShetabFa.setOpkey("AABA    ");
			atmRequestShetabFa.setIfxType(IfxType.PARTIAL_DISPENSE_RQ);
			atmRequestShetabFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestShetabFa.setCurrency(currency);
			atmRequestShetabFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestShetabFa.setFit(FITType.SHETAB);

			atmRequestShetabEn.setOpkey("IABA    ");
			atmRequestShetabEn.setIfxType(IfxType.PARTIAL_DISPENSE_RQ);
			atmRequestShetabEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestShetabEn.setCurrency(currency);
			atmRequestShetabEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestShetabEn.setFit(FITType.SHETAB);

			atmRequestPasargadFa.setOpkey("ABBA    ");
			atmRequestPasargadFa.setIfxType(IfxType.PARTIAL_DISPENSE_RQ);
			atmRequestPasargadFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestPasargadFa.setCurrency(currency);
			atmRequestPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestPasargadFa.setFit(FITType.PASARGAD);

			atmRequestRevPasargadFa.setOpkey("ABBA   F");
			atmRequestRevPasargadFa.setIfxType(IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ);
			atmRequestRevPasargadFa.setTrnType(TrnType.WITHDRAWAL);
			atmRequestRevPasargadFa.setCurrency(currency);
			atmRequestRevPasargadFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestRevPasargadFa.setFit(FITType.PASARGAD);

			atmRequestPasargadEn.setOpkey("IBBA    ");
			atmRequestPasargadEn.setIfxType(IfxType.PARTIAL_DISPENSE_RQ);
			atmRequestPasargadEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestPasargadEn.setCurrency(currency);
			atmRequestPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestPasargadEn.setFit(FITType.PASARGAD);


			atmRequestRevPasargadEn.setOpkey("IBBA   F");
			atmRequestRevPasargadEn.setIfxType(IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ);
			atmRequestRevPasargadEn.setTrnType(TrnType.WITHDRAWAL);
			atmRequestRevPasargadEn.setCurrency(currency);
			atmRequestRevPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestRevPasargadEn.setFit(FITType.PASARGAD);


			getGeneralDao().save(atmRequestShetabFa);
			getGeneralDao().save(atmRequestShetabEn);
			getGeneralDao().save(atmRequestPasargadFa);
			getGeneralDao().save(atmRequestRevPasargadFa);
			getGeneralDao().save(atmRequestPasargadEn);
			getGeneralDao().save(atmRequestRevPasargadEn);

			/******************************/
			List<ResponseScreen> screenMiddlePartialDispenseList = new ArrayList<ResponseScreen>();

			ResponseScreen screenMiddlePartialDispenseFa = new ResponseScreen();
			screenMiddlePartialDispenseFa.setScreenno("391");
			screenMiddlePartialDispenseFa.setDesc("برداشت جندمرحله ای-مراحل بعدی-موفق-فارسی");
			screenMiddlePartialDispenseFa.setLanguage(UserLanguage.FARSI_LANG);
			screenMiddlePartialDispenseFa.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]00;67;80;m"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]H4[GR ifx.currentStep]"
					+ "[SI]H2[GR ifx.totalStep]");
			screenMiddlePartialDispenseList.add(screenMiddlePartialDispenseFa);
			getGeneralDao().saveOrUpdate(screenMiddlePartialDispenseFa);

			ResponseScreen screenMiddlePartialDispenseEn = new ResponseScreen();
			screenMiddlePartialDispenseEn.setScreenno("791");
			screenMiddlePartialDispenseEn.setDesc("برداشت جندمرحله ای-مراحل بعدی-موفق-انگلیسی");
			screenMiddlePartialDispenseEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenMiddlePartialDispenseEn.setScreenData("791[FF][SI]@@[ESC]P2552[ESC]\\"
//					+ "[ESC](1[ESC][OC]00;67;80;m"
					+ "[ESC](1[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]HH[GR ifx.currentStep]"
					+ "[SI]HL[GR ifx.totalStep]");
			screenMiddlePartialDispenseList.add(screenMiddlePartialDispenseEn);
			getGeneralDao().saveOrUpdate(screenMiddlePartialDispenseEn);


			List<ResponseScreen> screenCashErrorList = new ArrayList<ResponseScreen>();

			ResponseScreen screenCashErrorFa = new ResponseScreen();
			screenCashErrorFa.setScreenno("386");
			screenCashErrorFa.setDesc("برداشت جندمرحله ای-خطا در دادن اسکناس-فارسی");
			screenCashErrorFa.setLanguage(UserLanguage.FARSI_LANG);
			screenCashErrorFa.setScreenData("386[FF][SI]@@[ESC]P2123[ESC]\\");
			screenCashErrorList.add(screenCashErrorFa);
			getGeneralDao().saveOrUpdate(screenCashErrorFa);

			ResponseScreen screenCashErrorEn = new ResponseScreen();
			screenCashErrorEn.setScreenno("786");
			screenCashErrorEn.setDesc("برداشت جندمرحله ای-خطا در دادن اسکناس-انگلیسی");
			screenCashErrorEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenCashErrorEn.setScreenData("786[FF][SI]@@[ESC]P2523[ESC]\\");
			screenCashErrorList.add(screenCashErrorEn);
			getGeneralDao().saveOrUpdate(screenCashErrorEn);


			List<ResponseScreen> screenTimeoutList = new ArrayList<ResponseScreen>();

			ResponseScreen screenTimeoutFa = new ResponseScreen();
			screenTimeoutFa.setScreenno("398");
			screenTimeoutFa.setDesc("برداشت جند مرحله ای-Timeout-فارسی");
			screenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
			screenTimeoutFa.setScreenData(null);
			screenTimeoutList.add(screenTimeoutFa);
			getGeneralDao().saveOrUpdate(screenTimeoutFa);

			ResponseScreen screenTimeoutEn = new ResponseScreen();
			screenTimeoutEn.setScreenno("798");
			screenTimeoutEn.setDesc("برداشت جند مرحله ای-Timeout-انگلیسی");
			screenTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenTimeoutEn.setScreenData(null);
			screenTimeoutList.add(screenTimeoutEn);
			getGeneralDao().saveOrUpdate(screenTimeoutEn);

			/******************************/

			FunctionCommandResponse atmResponse00ShetabPartialDispense = new FunctionCommandResponse();
			atmResponse00ShetabPartialDispense.setName("برداشت جندمرحله ای-مراحل بعدی-شتابی-موفق");
			atmResponse00ShetabPartialDispense.setBeRetain(false);
			atmResponse00ShetabPartialDispense.setFunctionCommand(NDCFunctionIdentifierConstants.DISPENSE_AND_PRINT);
			//TODO Partial Dispense
			atmResponse00ShetabPartialDispense.setNextState("549");
//			atmResponse00ShetabPartialDispense.setNextScreen("391");
//			atmResponse00ShetabPartialDispense.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]H4[GR ifx.currentStep]"
//					+ "[SI]H2[GR ifx.totalStep]");
			atmResponse00ShetabPartialDispense.setScreen(screenMiddlePartialDispenseList);
			getGeneralDao().saveOrUpdate(atmResponse00ShetabPartialDispense);

			FunctionCommandResponse atmResponseCashErr = new FunctionCommandResponse();
			atmResponseCashErr.setName("برداشت جندمرحله ای-خطا در دادن اسکناس");
			atmResponseCashErr.setBeRetain(false);
			atmResponseCashErr.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			//TODO Partial Dispense
			atmResponseCashErr.setNextState("707");
//			atmResponseCashErr.setNextScreen("386");
//			atmResponseCashErr.setScreenData("386[FF][SI]@@[ESC]P2123[ESC]\\");
			atmResponseCashErr.setScreen(screenCashErrorList);
			atmResponseCashErr.setReceipt(receiptCashErrList);
			getGeneralDao().saveOrUpdate(atmResponseCashErr);


			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("برداشت جند مرحله ای-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);


			FunctionCommandResponse atmResponse00PasargadPartialDispense = new FunctionCommandResponse();
			atmResponse00PasargadPartialDispense.setName("برداشت جندمرحله ای-مراحل بعدی-داخلی-موفق");
			atmResponse00PasargadPartialDispense.setBeRetain(false);
			atmResponse00PasargadPartialDispense.setFunctionCommand(NDCFunctionIdentifierConstants.DISPENSE_AND_PRINT);

			//TODO Partial Dispense
			atmResponse00PasargadPartialDispense.setNextState("049");
//			atmResponse00PasargadPartialDispense.setNextScreen("391");
//			atmResponse00PasargadPartialDispense.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]H4[GR ifx.currentStep]"
//					+ "[SI]H2[GR ifx.totalStep]");
			atmResponse00PasargadPartialDispense.setScreen(screenMiddlePartialDispenseList);

			getGeneralDao().saveOrUpdate(atmResponse00PasargadPartialDispense);
			
			atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00ShetabPartialDispense);
			atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00ShetabPartialDispense);
			atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00PasargadPartialDispense);
			atmRequestRevPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), atmResponse00PasargadPartialDispense);
			atmRequestRevPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			FunctionCommandResponse atmResponseFinalState = new FunctionCommandResponse();
			atmResponseFinalState.setName("برداشت جندمرحله ای-مرحله آخر-موفق");
			atmResponseFinalState.setBeRetain(false);
			atmResponseFinalState.setFunctionCommand(NDCFunctionIdentifierConstants.DISPENSE_AND_PRINT);
			atmResponseFinalState.setNextState("706");
//			atmResponseFinalState.setNextScreen("391");
//			atmResponseFinalState.setScreenData("391[FF][SI]@@[ESC]P2152[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]H4[GR ifx.currentStep]"
//					+ "[SI]H2[GR ifx.totalStep]");
			atmResponseFinalState.setScreen(screenMiddlePartialDispenseList);
			atmResponseFinalState.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(atmResponseFinalState);

			atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponseFinalState);
			atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponseFinalState);
			atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponseFinalState);
			atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_SUCCESS_PARTIAL_DISPENSE, atmResponseFinalState);

			atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_CACH_HANDLER, atmResponseCashErr);
			atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_CACH_HANDLER, atmResponseCashErr);
			atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_CACH_HANDLER, atmResponseCashErr);
			atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_CACH_HANDLER, atmResponseCashErr);

			getGeneralDao().saveOrUpdate(atmRequestShetabFa);
			getGeneralDao().saveOrUpdate(atmRequestShetabEn);
			getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestRevPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
			getGeneralDao().saveOrUpdate(atmRequestRevPasargadEn);

			requests.add(atmRequestShetabFa);
			requests.add(atmRequestShetabEn);
			requests.add(atmRequestPasargadFa);
			requests.add(atmRequestRevPasargadFa);
			requests.add(atmRequestPasargadEn);
			requests.add(atmRequestRevPasargadEn);
			return requests;
		}
	 private List<ATMRequest> Balance(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
			List<ATMRequest> requests = new ArrayList<ATMRequest>();

			ATMRequest atmRequestShetabFa = new ATMRequest();
			ATMRequest atmRequestTimeOutShetabFa = new ATMRequest();

			ATMRequest atmRequestShetabEn = new ATMRequest();
			ATMRequest atmRequestTimeOutShetabEn = new ATMRequest();

			ATMRequest atmRequestPasargadFa = new ATMRequest();
			ATMRequest atmRequestTimeOutPasargadFa = new ATMRequest();

			ATMRequest atmRequestPasargadSubFa = new ATMRequest();
			ATMRequest atmRequestTimeOutPasargadSubFa = new ATMRequest();

			ATMRequest atmRequestPasargadSubEn = new ATMRequest();
			ATMRequest atmRequestTimeOutPasargadSubEn = new ATMRequest();

			ATMRequest atmRequestPasargadEn = new ATMRequest();
			ATMRequest atmRequestTimeOutPasargadEn = new ATMRequest();

			ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
			ATMRequest atmRequestTimeOutCreditPasargadFa = new ATMRequest();

			ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
			ATMRequest atmRequestTimeOutCreditPasargadEn = new ATMRequest();

			String textFa ="[ESC](7[GR simpleBalanceReceiptFa()]"
//					headerFa
////					+ newLine + newLine
//					+ newLine
//					+ newLine
//					+ "[GR center(GR c2F('رسید موجودی')) ]"
//					+ newLine
//					+ lineFa
//					+ receivedDateFa
////					+ newLine + newLine + newLine
//					+ newLine + newLine
//					+ formatAppPanFa
////					+ newLine + newLine
//					+ newLine
//					+ seqCntrFa
////					+ newLine + newLine
//					+ newLine
//					+ accBalLedgerFa
////					+ newLine + newLine
//					+ newLine
//					+ accBalAvailableFa
////					+ newLine + newLine
//					+ newLine
//					+ subAccFa
//					+ "[GR putLF(10)]"
//					+ footerFa
					;
//			String replacedStrngFa=BalanceReceipt(textFa, configuration);
			String textEn =
				headerEn
//				+ newLine + newLine
				+ newLine
				+ newLine
				+"[GR center('Balance Receipt') ]"
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
//				+ newLine + newLine
				+ newLine
				+ accBalAvailableEn
//				+ newLine + newLine
				+ newLine
				+ subAccEn
				+ "[GR putLF(8)]"
				+ footerEn
				;

			String textJournal00 = "[GR simpleBalanceJournal()]";
//				"[LF]Balance:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";

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
			/********************************/

			atmRequestPasargadSubFa.setOpkey("ABCAB   ");
			atmRequestPasargadSubFa.setNextOpkey("ABCAA   ");
			atmRequestPasargadSubFa.setIfxType(IfxType.BAL_INQ_RQ);
			atmRequestPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
			atmRequestPasargadSubFa.setTrnType(TrnType.BALANCEINQUIRY);
			atmRequestPasargadSubFa.setCurrency(currency);
			atmRequestPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestPasargadSubFa.setFit(FITType.PASARGAD);

			atmRequestTimeOutPasargadSubFa.setOpkey("ABCAB  F");
			atmRequestTimeOutPasargadSubFa.setNextOpkey("ABCAB  F");
			atmRequestTimeOutPasargadSubFa.setIfxType(IfxType.BAL_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
//			atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.BAL_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.BALANCEINQUIRY);
			atmRequestTimeOutPasargadSubFa.setCurrency(currency);
			atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);

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

			atmRequestPasargadSubEn.setOpkey("IBCAB   ");
			atmRequestPasargadSubEn.setNextOpkey("IBCAA   ");
			atmRequestPasargadSubEn.setIfxType(IfxType.BAL_INQ_RQ);
			atmRequestPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
			atmRequestPasargadSubEn.setTrnType(TrnType.BALANCEINQUIRY);
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
			screenReceiptFa.setScreenno("387");
			screenReceiptFa.setDesc("اعلام موجودی-خطای رسید-فارسی");
			screenReceiptFa.setLanguage(UserLanguage.FARSI_LANG);
			screenReceiptFa.setScreenData("387[FF][SI]@@[ESC]P2144[ESC]\\"
//					+ "[ESC](K[ESC][OC]00;67;80m"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]GH[GR amount2Fscr(ifx.AcctBalAvailableAmt, 15)]"
					+ "[SI]IH[GR amount2Fscr(ifx.AcctBalLedgerAmt, 15)]");
			screenRecieptList.add(screenReceiptFa);
			getGeneralDao().saveOrUpdate(screenReceiptFa);

			ResponseScreen screenRecieptEn = new ResponseScreen();
			screenRecieptEn.setScreenno("787");
			screenRecieptEn.setDesc("اعلام موجودی-خطای رسید-انگلیسی");
			screenRecieptEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenRecieptEn.setScreenData("787[FF][SI]@@[ESC]P2544[ESC]\\"
					+ "[ESC](1[ESC][OC]80;m"
					+ "[SI]GK[GR amount2Escr(ifx.AcctBalAvailableAmt, 15)]"
					+ "[SI]IK[GR amount2Escr(ifx.AcctBalLedgerAmt, 15)]");
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
//			responseBalanceShetab.setNextScreen("388");
//			responseBalanceShetab.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
			responseBalanceShetab.setScreen(screenBalanceList);
			responseBalanceShetab.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseBalanceShetab);

			FunctionCommandResponse responseReceiptExceptionShetab = new FunctionCommandResponse();
			responseReceiptExceptionShetab.setName("اعلام موجودی-شتابی-خطای رسید");
			responseReceiptExceptionShetab.setBeRetain(false);
			responseReceiptExceptionShetab.setDispense(null);
			responseReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionShetab.setNextState("598");
//			responseReceiptExceptionShetab.setNextScreen("387");
//			responseReceiptExceptionShetab.setScreenData("387[FF][SI]@@[ESC]P2144[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]GJ[GR amount2Fscr(ifx.AcctBalAvailableAmt, 15)]"
//					+ "[SI]IJ[GR amount2Fscr(ifx.AcctBalLedgerAmt, 15)]");
			responseReceiptExceptionShetab.setScreen(screenRecieptList);
			responseReceiptExceptionShetab.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionShetab);

			/********************************/

			FunctionCommandResponse responseBalancePasargad = new FunctionCommandResponse();
			responseBalancePasargad.setName("اعلام موجودی-داخلی-موفق");
			responseBalancePasargad.setBeRetain(false);
			responseBalancePasargad.setDispense(null);
			responseBalancePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseBalancePasargad.setNextState("096");
//			responseBalancePasargad.setNextScreen("388");
//			responseBalancePasargad.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
			responseBalancePasargad.setScreen(screenBalanceList);
			responseBalancePasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseBalancePasargad);

			FunctionCommandResponse responseReceiptExceptionPasargad = new FunctionCommandResponse();
			responseReceiptExceptionPasargad.setName("اعلام موجودی-داخلی-خطای رسید");
			responseReceiptExceptionPasargad.setBeRetain(false);
			responseReceiptExceptionPasargad.setDispense(null);
			responseReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionPasargad.setNextState("098");
//			responseReceiptExceptionPasargad.setNextScreen("387");
//			responseReceiptExceptionPasargad.setScreenData("387[FF][SI]@@[ESC]P2144[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]GJ[GR amount2Fscr(ifx.AcctBalAvailableAmt, 15)]"
//					+ "[SI]IJ[GR amount2Fscr(ifx.AcctBalLedgerAmt, 15)]");
			responseReceiptExceptionPasargad.setScreen(screenRecieptList);
			responseReceiptExceptionPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionPasargad);

			/********************************/

			FunctionCommandResponse responseBalanceCreditPasargad = new FunctionCommandResponse();
			responseBalanceCreditPasargad.setName("اعلام موجودی-اعتباری داخلی-موفق");
			responseBalanceCreditPasargad.setBeRetain(false);
			responseBalanceCreditPasargad.setDispense(null);
			responseBalanceCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseBalanceCreditPasargad.setNextState("196");
//			responseBalanceCreditPasargad.setNextScreen("388");
//			responseBalanceCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
			responseBalanceCreditPasargad.setScreen(screenBalanceList);
			responseBalanceCreditPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseBalanceCreditPasargad);

			FunctionCommandResponse responseReceiptExceptionCreditPasargad = new FunctionCommandResponse();
			responseReceiptExceptionCreditPasargad.setName("اعلام موجودی-اعتباری داخلی-خطای رسید");
			responseReceiptExceptionCreditPasargad.setBeRetain(false);
			responseReceiptExceptionCreditPasargad.setDispense(null);
			responseReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionCreditPasargad.setNextState("198");
//			responseReceiptExceptionCreditPasargad.setNextScreen("387");
//			responseReceiptExceptionCreditPasargad.setScreenData("387[FF][SI]@@[ESC]P2144[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]GJ[GR amount2Fscr(ifx.AcctBalAvailableAmt, 15)]"
//					+ "[SI]IJ[GR amount2Fscr(ifx.AcctBalLedgerAmt, 15)]");
			responseReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
			responseReceiptExceptionCreditPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionCreditPasargad);

			/********************************/
			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("اعلام موجودی-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);
			/********************************/

			atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceShetab);
			atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);
			atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceShetab);
			atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);
			atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalancePasargad);
			atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
			atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
			atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalancePasargad);
			atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
			atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
			atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceCreditPasargad);
			atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
			atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBalanceCreditPasargad);
			atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
			atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

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
			requests.add(atmRequestTimeOutCreditPasargadFa);
			requests.add(atmRequestTimeOutCreditPasargadEn);

			return requests;
		}
	 private List<ATMRequest> BankStatement(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
			List<ATMRequest> requests = new ArrayList<ATMRequest>();
			ATMRequest atmRequestFa = new ATMRequest();
			ATMRequest atmRequestSubFa = new ATMRequest();
			ATMRequest atmRequestEn = new ATMRequest();
			ATMRequest atmRequestSubEn=new ATMRequest();
			ATMRequest atmRequestCreditPasargadFa = new ATMRequest();
			ATMRequest atmRequestCreditPasargadEn = new ATMRequest();
			ATMRequest atmRequestTimeoutFa = new ATMRequest();
			ATMRequest atmRequestSubTimeoutFa = new ATMRequest();
			ATMRequest atmRequestTimeoutEn = new ATMRequest();
			ATMRequest atmRequestSubTimeoutEn=new ATMRequest();
			ATMRequest atmRequestCreditPasargadTimeoutFa = new ATMRequest();
			ATMRequest atmRequestCreditPasargadTimeoutEn = new ATMRequest();

			String textFa = "[GR simpleBankStatementReceiptFa()]"
//				headerFa
////				+ newLine + newLine
//				+ newLine
//				+ newLine
//				+ "[GR center(GR c2F('رسید صورتحساب'))]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
//				+ newLine
//				+ subAccFa
//				+ "[LF][GR bankStatementTableFa()]"
//				+ newLine
//				+ accBalLedgerFa
//				+ "[GR putLF(2)]"
//				+ footerFa
				;
			String textEn =
				headerEn
//				+ newLine + newLine
				+ newLine
				+ newLine
				+ "[GR center('Statement Receipt')]"
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
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

			atmRequestSubEn.setOpkey("IBFAB   ");
			atmRequestSubEn.setNextOpkey("IBFAA   ");
			atmRequestSubEn.setIfxType(IfxType.BANK_STATEMENT_RQ);
			atmRequestSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
			atmRequestSubEn.setTrnType(TrnType.BANKSTATEMENT);
			atmRequestSubEn.setCurrency(currency);
			atmRequestSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestSubEn.setFit(FITType.PASARGAD);

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


			atmRequestSubTimeoutEn.setOpkey("IBFAB  F");
			atmRequestSubTimeoutEn.setNextOpkey("IBFAA   ");
			atmRequestSubTimeoutEn.setIfxType(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
			atmRequestSubTimeoutEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
			atmRequestSubTimeoutEn.setTrnType(TrnType.BANKSTATEMENT);
			atmRequestSubTimeoutEn.setCurrency(currency);
			atmRequestSubTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestSubTimeoutEn.setFit(FITType.PASARGAD);


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
			getGeneralDao().save(atmRequestSubEn);
			getGeneralDao().save(atmRequestCreditPasargadFa);
			getGeneralDao().save(atmRequestCreditPasargadEn);
			getGeneralDao().save(atmRequestTimeoutFa);
			getGeneralDao().save(atmRequestSubTimeoutFa);
			getGeneralDao().save(atmRequestTimeoutEn);
			getGeneralDao().save(atmRequestSubTimeoutEn);
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
//			response00.setFunctionCommand(NDCFunctionIdentifierConstants.PRINT_IMMEDIATE);
			response00.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			response00.setNextState("096");
//			response00.setNextScreen("388");
//			response00.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
			response00.setScreen(screenStatementList);
			response00.setReceipt(receiptList00);
			getGeneralDao().saveOrUpdate(response00);

			FunctionCommandResponse creditResponse00 = new FunctionCommandResponse();
			creditResponse00.setName("صورتحساب-اعتباری داخلی-موفق");
			creditResponse00.setBeRetain(false);
			creditResponse00.setDispense(null);
//			creditResponse00.setFunctionCommand(NDCFunctionIdentifierConstants.PRINT_IMMEDIATE);
			creditResponse00.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			creditResponse00.setNextState("196");
//			creditResponse00.setNextScreen("388");
//			creditResponse00.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
			creditResponse00.setScreen(screenStatementList);
			creditResponse00.setReceipt(receiptList00);
			getGeneralDao().saveOrUpdate(response00);

			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("صورتحساب-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);

			atmRequestFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), response00);
			atmRequestSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), response00);
			atmRequestSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), creditResponse00);
			atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), creditResponse00);

			atmRequestTimeoutFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestSubTimeoutFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestTimeoutEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestSubTimeoutEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestCreditPasargadTimeoutFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestCreditPasargadTimeoutEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			getGeneralDao().saveOrUpdate(atmRequestFa);
			getGeneralDao().saveOrUpdate(atmRequestSubFa);
			getGeneralDao().saveOrUpdate(atmRequestEn);
			getGeneralDao().saveOrUpdate(atmRequestSubEn);
			getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);

			getGeneralDao().saveOrUpdate(atmRequestTimeoutFa);
			getGeneralDao().saveOrUpdate(atmRequestSubTimeoutFa);
			getGeneralDao().saveOrUpdate(atmRequestTimeoutEn);
			getGeneralDao().saveOrUpdate(atmRequestSubTimeoutEn);
			getGeneralDao().saveOrUpdate(atmRequestCreditPasargadTimeoutFa);
			getGeneralDao().saveOrUpdate(atmRequestCreditPasargadTimeoutEn);

			requests.add(atmRequestFa);
			requests.add(atmRequestSubFa);
			requests.add(atmRequestEn);
			requests.add(atmRequestSubEn);
			requests.add(atmRequestCreditPasargadFa);
			requests.add(atmRequestCreditPasargadEn);
			requests.add(atmRequestTimeoutFa);
			requests.add(atmRequestSubTimeoutFa);
			requests.add(atmRequestTimeoutEn);
			requests.add(atmRequestSubTimeoutEn);
			requests.add(atmRequestCreditPasargadTimeoutFa);
			requests.add(atmRequestCreditPasargadTimeoutEn);

			return requests;
		}
	 private List<ATMRequest> Billpayment(ATMConfiguration configuration, OARResponse oarResponse) throws Exception {
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

			String textFa = "[GR simpleBillPaymentReceiptFa()]"
//					headerFa
//					+ "[LF][LF][GR center(GR c2F('رسید پرداخت قبض'))]"
//					+ newLine
//					+ lineFa
//					+ receivedDateFa
////					+ newLine + newLine + newLine
//					+ newLine + newLine
//					+ formatAppPanFa
////					+ newLine + newLine
//					+ newLine
//					+ seqCntrFa
////					+ newLine + newLine
//					+ newLine
//					+ "[GR justify(GR c2F('شناسه قبض'), GR c2F(ifx.BillID))]"
////					+ newLine + newLine
//					+ newLine
//					+ "[GR justify(GR c2F('شناسه پرداخت'), GR c2F(ifx.BillPaymentID))]"
////					+ newLine + newLine
//					+ newLine
//					+ "[GR justify(GR c2F('سازمان'), GR c2F(ifx.BillOrgType))]"
////					+ newLine + newLine
//					+ newLine
//					+ amountFa
//					+ "[GR putLF(8)]"
//					+ footerFa
					;

			String textEn =
				headerEn
				+ "[LF][LF][GR center('Bill Payment Receipt')]"
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
				+ "[GR justify(ifx.BillID, 'Bill ID')]"
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(GR ifx.BillPaymentID, 'Payment ID')]"
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(GR c2E(ifx.BillOrgType), 'Organization')]"
//				+ newLine + newLine
				+ newLine
				+ amountEn
				+ "[GR putLF(8)]"
				+ footerEn
				;

			String textJournal00 = "[GR simpleBillPaymentJournal()]" ;
//				"[LF]Bill Payment:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";


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
//			atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.BILL_PMT_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.BILLPAYMENT);
			atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestTimeOutPasargadSubFa.setCurrency(currency);
			atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);

			//TODO: double check for English version
			atmRequestReceiptExceptionPasargadSubFa.setOpkey("ABHBB  A");
			atmRequestReceiptExceptionPasargadSubFa.setNextOpkey("ABHBA  A");
			atmRequestReceiptExceptionPasargadSubFa.setIfxType(IfxType.BILL_PMT_RQ);
//			atmRequestReceiptExceptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
			atmRequestReceiptExceptionPasargadSubFa.setTrnType(TrnType.BILLPAYMENT);
//			atmRequestReceiptExceptionPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestReceiptExceptionPasargadSubFa.setForceReceipt(false);
			atmRequestReceiptExceptionPasargadSubFa.setCurrency(currency);
			atmRequestReceiptExceptionPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestReceiptExceptionPasargadSubFa.setFit(FITType.PASARGAD);
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
			screenCheckOrganizationFa.setScreenData("032[ESC]P2086[ESC]\\"
//					+ "[ESC](K[ESC][OC]00;67;80;m"
					+ "[ESC](K[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]F0[GR ifx.BillID]"
					+ "[SI]G0[GR ifx.BillPaymentID]"
					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
					+ "[SI]I0[GR ifx.Auth_Amt]");
			screenCheckOrganizationList.add(screenCheckOrganizationFa);
			getGeneralDao().saveOrUpdate(screenCheckOrganizationFa);

			ResponseScreen screenCheckOrganizationEn = new ResponseScreen();
			screenCheckOrganizationEn.setScreenno("432");
			screenCheckOrganizationEn.setDesc("تایید پرداخت قبض-موفق-انگلیسی");
			screenCheckOrganizationEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenCheckOrganizationEn.setScreenData("432[ESC]P2486[ESC]\\"
//					+ "[ESC](1[ESC][OC]00;67;80;m"
					+ "[ESC](1[ESC][OC]"
					+ updateablePageFontColor
					+ "[SI]F0[GR ifx.BillID]"
					+ "[SI]G0[GR ifx.BillPaymentID]"
					+ "[SI]H0[GR c2E(ifx.BillOrgType)]"
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
//			responseShetab.setNextScreen("032");
//			responseShetab.setScreenData("032[ESC]P2086[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]F0[GR ifx.BillID]"
//					+ "[SI]G0[GR ifx.BillPaymentID]"
//					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responseShetab.setScreen(screenCheckOrganizationList);
			getGeneralDao().saveOrUpdate(responseShetab);

			/*****************************/

			FunctionCommandResponse responsePasargad = new FunctionCommandResponse();
			responsePasargad.setName("تایید پرداخت قبض-داخلی-موفق");
			responsePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responsePasargad.setNextState("045");
//			responsePasargad.setNextScreen("032");
//			responsePasargad.setScreenData("032[ESC]P2086[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]F0[GR ifx.BillID]"
//					+ "[SI]G0[GR ifx.BillPaymentID]"
//					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responsePasargad.setScreen(screenCheckOrganizationList);
			getGeneralDao().saveOrUpdate(responsePasargad);

			/*****************************/

			FunctionCommandResponse responseCreditPasargad = new FunctionCommandResponse();
			responseCreditPasargad.setName("تایید پرداخت قبض-اعتباری داخلی-موفق");
			responseCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseCreditPasargad.setNextState("135");
//			responseCreditPasargad.setNextScreen("032");
//			responseCreditPasargad.setScreenData("032[ESC]P2086[ESC]\\"
//					+ "[ESC](K"
//					+ "[ESC][OC]B0;80m"
//					+ "[SI]F0[GR ifx.BillID]"
//					+ "[SI]G0[GR ifx.BillPaymentID]"
//					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responseCreditPasargad.setScreen(screenCheckOrganizationList);
			getGeneralDao().saveOrUpdate(responseCreditPasargad);

			/*****************************/
			/*****************************/

			FunctionCommandResponse responseBillShetab = new FunctionCommandResponse();
			responseBillShetab.setName("پرداخت قبض-شتابی-موفق");
			responseBillShetab.setBeRetain(false);
			responseBillShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseBillShetab.setNextState("596");
//			responseBillShetab.setNextScreen("388");
//			responseBillShetab.setScreenData("388[FF][SI]@@[ESC]P2126[ESC]\\");
			responseBillShetab.setScreen(screenBillPaymentList);
			responseBillShetab.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseBillShetab);

			FunctionCommandResponse responseReceiptExceptionShetab = new FunctionCommandResponse();
			responseReceiptExceptionShetab.setName("پرداخت قبض-شتابی-خطای رسید");
			responseReceiptExceptionShetab.setBeRetain(false);
			responseReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionShetab.setNextState("592");
//			responseReceiptExceptionShetab.setNextScreen("384");
//			responseReceiptExceptionShetab.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionShetab.setScreen(screenRecieptList);
			responseReceiptExceptionShetab.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionShetab);

			/*****************************/

			FunctionCommandResponse responseBillPasargad = new FunctionCommandResponse();
			responseBillPasargad.setName("پرداخت قبض-داخلی-موفق");
			responseBillPasargad.setBeRetain(false);
			responseBillPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseBillPasargad.setNextState("096");
//			responseBillPasargad.setNextScreen("388");
//			responseBillPasargad.setScreenData("388[FF][SI]@@[ESC]P2126[ESC]\\");
			responseBillPasargad.setScreen(screenBillPaymentList);
			responseBillPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseBillPasargad);

			FunctionCommandResponse responseReceiptExceptionPasargad = new FunctionCommandResponse();
			responseReceiptExceptionPasargad.setName("پرداخت قبض-داخلی-خطای رسید");
			responseReceiptExceptionPasargad.setBeRetain(false);
			responseReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionPasargad.setNextState("092");
//			responseReceiptExceptionPasargad.setNextScreen("384");
//			responseReceiptExceptionPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionPasargad.setScreen(screenRecieptList);
			responseReceiptExceptionPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionPasargad);

			/*****************************/

			FunctionCommandResponse responseBillCreditPasargad = new FunctionCommandResponse();
			responseBillCreditPasargad.setName("پرداخت قبض-اعتباری داخلی-موفق");
			responseBillCreditPasargad.setBeRetain(false);
			responseBillCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseBillCreditPasargad.setNextState("196");
//			responseBillCreditPasargad.setNextScreen("388");
//			responseBillCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2126[ESC]\\");
			responseBillCreditPasargad.setScreen(screenBillPaymentList);
			responseBillCreditPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseBillCreditPasargad);

			FunctionCommandResponse responseReceiptExceptionCreditPasargad = new FunctionCommandResponse();
			responseReceiptExceptionCreditPasargad.setName("پرداخت قبض-اعتباری داخلی-خطای رسید");
			responseReceiptExceptionCreditPasargad.setBeRetain(false);
			responseReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionCreditPasargad.setNextState("192");
//			responseReceiptExceptionCreditPasargad.setNextScreen("384");
//			responseReceiptExceptionCreditPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
			responseReceiptExceptionCreditPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionCreditPasargad);

			/*****************************/
			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("پرداخت قبض-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);
			/*****************************/

			atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
			atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);

			atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
			atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);

			atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
			atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
			atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);
			atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);

			atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillCreditPasargad);
			atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);

			/*****************************/

			atmRequestReceiptExceptionShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
			atmRequestReceiptExceptionShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillShetab);
			atmRequestReceiptExceptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
			atmRequestReceiptExceptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
			atmRequestReceiptExceptionPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
			atmRequestReceiptExceptionPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseBillPasargad);
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

			getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionShetabFa);
			getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionShetabEn);
			getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadSubFa);
			getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadEn);
			getGeneralDao().saveOrUpdate(atmRequestReceiptExceptionPasargadSubEn);
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
	 public List<ATMRequest> purchaseCharge(ATMConfiguration configuration, OARResponse oarResponse)throws Exception {
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


			String customerReceiptFaText = "[GR simplePurchaseMTNChargeReceiptFa()]"
//				  headerFa
//				+ "[LF][LF][GR center(c2F('رسید خرید شارژ ایرانسل')) ]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine
//				+ newLine
//				+ seqCntrFa
////				+ newLine + newLine
//				+ newLine
//				+ accBalLedgerFa
//				+ newLine
//				+ "[GR center('-----------')]"
//				+ newLine
//				+ amountFa
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))]"
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))]"
////				+ newLine + newLine
//				+ newLine
////				+ "[GR justify(c2E(decode(ifx.getChargeData().getCharge().getCardPIN())),c2E( 'PIN'))]"
//				+ "[GR justify(c2E(''),c2E(decode(ifx.getChargeData().getCharge().getCardPIN())))]"
//				+ newLine
//				+ "[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))]"
//				+ newLine
//				+ "[LF][ESC](1*140*[ESC](7[GR c2F('رمز')][ESC](1#YES/OK[SO]9:[ESC](7[GR c2F('نحوه استفاده')]"
////				+ newLine + newLine
//				+ newLine
//				+ "[GR center(GR c2F('این شارژ قابل انتقال نیست')) ]"
//				+ newLine
//				+ "[GR center(GR c2F('شماره امداد مشتریان ایرانسل 140')) ]"
//				+ newLine
//				+ footerFa
				;

			String customerReceiptEnText =
				 headerEn
					+ "[LF][LF][GR center('IranCell Charge Card') ]"
					+ newLine
					+ lineEn
					+ receivedDateEn
//					+ newLine + newLine + newLine
					+ newLine + newLine
					+ formatAppPanEn
//					+ newLine + newLine
					+ newLine
					+ seqCntrEn
//					+ newLine + newLine
					+ newLine
					+ accBalLedgerEn
					+ newLine
					+ "[GR center('-----------')]"
					+ newLine
					+ amountEn
//					+ newLine + newLine
					+ newLine
					+ "[GR justify(realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), 'Real Credit')]"
//					+ newLine + newLine
					+ newLine
					+ "[GR justify(decode(ifx.getChargeData().getCharge().getCardPIN()), 'PIN')]"
//					+ newLine + newLine
					+ newLine
					+ "[GR justify(ifx.getChargeData().getCharge().getCardSerialNo(), 'Charge Serial Number')]"
					+ newLine
					+ "[LF][GR justify('*140*PIN#YES/OK', 'How To Use')]"
//					+ newLine + newLine
					+ newLine
					+ "[GR center() ]"
					+ newLine
					+ "[GR center('IranCell HelpDesk Phone Number 140') ]"
					+ newLine
					+ footerEn;

			String textJournal00 = "[GR simplePurchaseChargeJournal()]";
//				"[LF]Charge:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId][SO]1SERNO:[GR ifx.getChargeData().getCharge().getCardSerialNo()]";


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
//			responseChargeShetab.setNextScreen("388");
//			responseChargeShetab.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
			responseChargeShetab.setScreen(screenChargeList);
			responseChargeShetab.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseChargeShetab);

			FunctionCommandResponse responseChargeReceiptExceptionShetab = new FunctionCommandResponse();
			responseChargeReceiptExceptionShetab.setName("خرید شارژ-شتابی-خطای رسید");
			responseChargeReceiptExceptionShetab.setBeRetain(false);
			responseChargeReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeReceiptExceptionShetab.setNextState("598");
//			responseChargeReceiptExceptionShetab.setNextScreen("387");
//			responseChargeReceiptExceptionShetab.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
			responseChargeReceiptExceptionShetab.setScreen(screenRecieptList);
			responseChargeReceiptExceptionShetab.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionShetab);

			/****************************/

			FunctionCommandResponse responseChargePasargad = new FunctionCommandResponse();
			responseChargePasargad.setName("خرید شارژ-داخلی-موفق");
			responseChargePasargad.setBeRetain(false);
			responseChargePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargePasargad.setNextState("096");
//			responseChargePasargad.setNextScreen("388");
//			responseChargePasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
			responseChargePasargad.setScreen(screenChargeList);
			responseChargePasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseChargePasargad);

			FunctionCommandResponse responseChargeReceiptExceptionPasargad = new FunctionCommandResponse();
			responseChargeReceiptExceptionPasargad.setName("خرید شارژ-داخلی-خطای رسید");
			responseChargeReceiptExceptionPasargad.setBeRetain(false);
			responseChargeReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeReceiptExceptionPasargad.setNextState("098");
//			responseChargeReceiptExceptionPasargad.setNextScreen("387");
//			responseChargeReceiptExceptionPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
			responseChargeReceiptExceptionPasargad.setScreen(screenRecieptList);
			responseChargeReceiptExceptionPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionPasargad);

			/****************************/

			FunctionCommandResponse responseChargeCreditPasargad = new FunctionCommandResponse();
			responseChargeCreditPasargad.setName("خرید شارژ-اعتباری داخلی-موفق");
			responseChargeCreditPasargad.setBeRetain(false);
			responseChargeCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeCreditPasargad.setNextState("196");
//			responseChargeCreditPasargad.setNextScreen("388");
//			responseChargeCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
			responseChargeCreditPasargad.setScreen(screenChargeList);
			responseChargeCreditPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseChargeCreditPasargad);

			FunctionCommandResponse responseChargeReceiptExceptionCreditPasargad = new FunctionCommandResponse();
			responseChargeReceiptExceptionCreditPasargad.setName("خرید شارژ-اعتباری داخلی-خطای رسید");
			responseChargeReceiptExceptionCreditPasargad.setBeRetain(false);
			responseChargeReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeReceiptExceptionCreditPasargad.setNextState("198");
//			responseChargeReceiptExceptionCreditPasargad.setNextScreen("387");
//			responseChargeReceiptExceptionCreditPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
			responseChargeReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
			responseChargeReceiptExceptionCreditPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionCreditPasargad);

			/****************************/

			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("خريد شارژ-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
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


			String customerReceiptFaText = "[GR simplePurchaseMCIChargeReceiptFa()]"
//				headerFa
//				+ "[LF][LF][GR center(c2F('رسید خرید شارژ همراه اول')) ]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine
//				+ newLine
//				+ seqCntrFa
////				+ newLine + newLine
//				+ newLine
//				+ accBalLedgerFa
//				+ newLine
//				+ "[GR center('-----------')]"
//				+ newLine
//				+ amountFa
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(c2F('ارزش واقعی برای مکالمه'), realChargeCredit2F(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7))]"
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(c2F('شماره رمز'), c2F(decode(ifx.getChargeData().getCharge().getCardPIN())))]"
//				+ newLine
//				+ "[GR justify(c2E(''),c2E(decode(ifx.getChargeData().getCharge().getCardPIN())))]"
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(c2F('شماره سریال شارژ'), c2F(ifx.getChargeData().getCharge().getCardSerialNo()))]"
//				+ newLine
//				+ "[LF][ESC](1*140*#[ESC](7[GR c2F('رمز')][ESC](1#YES/OK[SO]8:[ESC](7[GR c2F('نحوه استفاده')]"
////				+ newLine + newLine
//				+ newLine
////				+ "[GR center(GR c2F('این شارژ قابل انتقال نیست')) ]"
////				+ newLine
////				+ "[GR center(GR c2F('شماره امداد مشتریان همراه اول 140')) ]"
////				+ newLine
//				+ footerFa
				;

			String customerReceiptEnText =
				headerEn
				+ "[LF][LF][GR center('MCI Charge Card') ]"
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
				+ "[GR justify(realChargeCredit2E(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode(), 7), 'Real Credit')]"
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(decode(ifx.getChargeData().getCharge().getCardPIN()), 'PIN')]"
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(ifx.getChargeData().getCharge().getCardSerialNo(), 'Charge Serial Number')]"
				+ newLine
				+ "[LF][GR justify('*140*#PIN#YES/OK', 'How To Use')]"
//				+ newLine + newLine
//				+ newLine
//				+ "[GR center() ]"
//				+ newLine
//				+ "[GR center('MCI HelpDesk Phone Number 140') ]"
				+ newLine
				+ footerEn;

			String textJournal00 = "[GR simplePurchaseChargeJournal()]";
//				"[LF]Charge:[SO]1[GR ifx.Auth_Amt][SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId][SO]1SERNO:[GR ifx.getChargeData().getCharge().getCardSerialNo()]";


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
//			atmRequestTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubFa.setTrnType(TrnType.PURCHASECHARGE);
			atmRequestTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestTimeOutPasargadSubFa.setExtraInformation("9912");
			atmRequestTimeOutPasargadSubFa.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
			atmRequestTimeOutPasargadSubFa.setCurrency(currency);
			atmRequestTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestTimeOutPasargadSubFa.setFit(FITType.PASARGAD);


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
//			atmRequestTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
			atmRequestTimeOutPasargadSubEn.setTrnType(TrnType.PURCHASECHARGE);
			atmRequestTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestTimeOutPasargadSubEn.setExtraInformation("9912");
			atmRequestTimeOutPasargadSubEn.setExtraInformationIfxPath("ifx.setThirdPartyCode(new Long(extraInformation))");
			atmRequestTimeOutPasargadSubEn.setCurrency(currency);
			atmRequestTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestTimeOutPasargadSubEn.setFit(FITType.PASARGAD);



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
//			responseChargeShetab.setNextScreen("388");
//			responseChargeShetab.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
			responseChargeShetab.setScreen(screenChargeList);
			responseChargeShetab.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseChargeShetab);

			FunctionCommandResponse responseChargeReceiptExceptionShetab = new FunctionCommandResponse();
			responseChargeReceiptExceptionShetab.setName("خرید شارژ همراه اول-شتابی-خطای رسید");
			responseChargeReceiptExceptionShetab.setBeRetain(false);
			responseChargeReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeReceiptExceptionShetab.setNextState("598");
//			responseChargeReceiptExceptionShetab.setNextScreen("387");
//			responseChargeReceiptExceptionShetab.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
			responseChargeReceiptExceptionShetab.setScreen(screenRecieptList);
			responseChargeReceiptExceptionShetab.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionShetab);

			/****************************/

			FunctionCommandResponse responseChargePasargad = new FunctionCommandResponse();
			responseChargePasargad.setName("خرید شارژ همراه اول-داخلی-موفق");
			responseChargePasargad.setBeRetain(false);
			responseChargePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargePasargad.setNextState("096");
//			responseChargePasargad.setNextScreen("388");
//			responseChargePasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
			responseChargePasargad.setScreen(screenChargeList);
			responseChargePasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseChargePasargad);

			FunctionCommandResponse responseChargeReceiptExceptionPasargad = new FunctionCommandResponse();
			responseChargeReceiptExceptionPasargad.setName("خرید شارژ همراه اول-داخلی-خطای رسید");
			responseChargeReceiptExceptionPasargad.setBeRetain(false);
			responseChargeReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeReceiptExceptionPasargad.setNextState("098");
//			responseChargeReceiptExceptionPasargad.setNextScreen("387");
//			responseChargeReceiptExceptionPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
			responseChargeReceiptExceptionPasargad.setScreen(screenRecieptList);
			responseChargeReceiptExceptionPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionPasargad);

			/****************************/

			FunctionCommandResponse responseChargeCreditPasargad = new FunctionCommandResponse();
			responseChargeCreditPasargad.setName("خرید شارژ همراه اول-اعتباری داخلی-موفق");
			responseChargeCreditPasargad.setBeRetain(false);
			responseChargeCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeCreditPasargad.setNextState("196");
//			responseChargeCreditPasargad.setNextScreen("388");
//			responseChargeCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2127[ESC]\\");
			responseChargeCreditPasargad.setScreen(screenChargeList);
			responseChargeCreditPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseChargeCreditPasargad);

			FunctionCommandResponse responseChargeReceiptExceptionCreditPasargad = new FunctionCommandResponse();
			responseChargeReceiptExceptionCreditPasargad.setName("خرید شارژ همراه اول-اعتباری داخلی-خطای رسید");
			responseChargeReceiptExceptionCreditPasargad.setBeRetain(false);
			responseChargeReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseChargeReceiptExceptionCreditPasargad.setNextState("198");
//			responseChargeReceiptExceptionCreditPasargad.setNextScreen("387");
//			responseChargeReceiptExceptionCreditPasargad.setScreenData("387[FF][SI]@@[ESC]P2143[ESC]\\");
			responseChargeReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
			responseChargeReceiptExceptionCreditPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseChargeReceiptExceptionCreditPasargad);

			/****************************/
			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("خريد شارژ همراه اول-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
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
				/*headerFa
				+ "[LF][LF][GR center(GR c2F('رسید انتقال وجه'))]"
				+ newLine
				+ lineFa
				+ receivedDateFa
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ seqCntrFa
//				+ newLine + newLine
				+ newLine
				+ amountFa
//				+ newLine + newLine
				+ newLine
				+ transferAppPanFa
//				+ newLine + newLine
				+ newLine
				+ transferSecAppPanFa
//				+ newLine + newLine
				+ newLine
				+ "[GR right(GR c2F("
				+ "'به نام ' +"
				+ " ifx.CardHolderName + ' ' + "
				+ " ifx.CardHolderFamily + ' ' + "
				+ "' انتقال یافت'"
				+ "))]"
				+ "[GR putLF(10)]"
				+ footerFa;*/

			String textEn =
				headerEn
				+ "[LF][LF][GR center('Fund Transfer Receipt')]"
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ seqCntrEn
//				+ newLine + newLine
				+ newLine
				+ amountEn
//				+ newLine + newLine
				+ newLine
				+ transferAppPanEn
//				+ newLine + newLine
				+ newLine
				+ transferSecAppPanEn
//				+ newLine + newLine
				+ newLine
				+ "Transfered to "
//				+ "[GR ifx.CardHolderName + ' ']"
//				+ "[GR ifx.CardHolderFamily]"
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
//			atmRequestTransferTimeOutPasargadSubFa.setSecondaryIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
			atmRequestTransferTimeOutPasargadSubFa.setTrnType(TrnType.TRANSFER);
			atmRequestTransferTimeOutPasargadSubFa.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestTransferTimeOutPasargadSubFa.setCurrency(currency);
			atmRequestTransferTimeOutPasargadSubFa.setLanguage(UserLanguage.FARSI_LANG);
			atmRequestTransferTimeOutPasargadSubFa.setFit(FITType.PASARGAD);

			atmRequestTransferReceiptExceptionPasargadSubFa.setOpkey("ABIBB  A");
			atmRequestTransferReceiptExceptionPasargadSubFa.setNextOpkey("ABIBA  A");
			atmRequestTransferReceiptExceptionPasargadSubFa.setIfxType(IfxType.TRANSFER_RQ);
//			atmRequestTransferReceiptExceptionPasargadSubFa.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
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
//			atmRequestTransferTimeOutPasargadSubEn.setSecondaryIfxType(IfxType.TRANSFER_REV_REPEAT_RQ);
			atmRequestTransferTimeOutPasargadSubEn.setTrnType(TrnType.TRANSFER);
			atmRequestTransferTimeOutPasargadSubEn.setSecondaryTrnType(TrnType.GETACCOUNT);
			atmRequestTransferTimeOutPasargadSubEn.setCurrency(currency);
			atmRequestTransferTimeOutPasargadSubEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestTransferTimeOutPasargadSubEn.setFit(FITType.PASARGAD);

			atmRequestTransferReceiptExceptionPasargadSubEn.setOpkey("IBIBB  A");
			atmRequestTransferReceiptExceptionPasargadSubEn.setNextOpkey("IBIBA  A");
			atmRequestTransferReceiptExceptionPasargadSubEn.setIfxType(IfxType.TRANSFER_RQ);
//			atmRequestTransferReceiptExceptionPasargadSubEn.setSecondaryIfxType(IfxType.GET_ACCOUNT_RQ);
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
			screenCheckAccountFa.setScreenData("033[ESC]P2084[ESC]\\"
					+ "[ESC](K"
//					+ "[ESC][OC]00;67;80m"
					+ "[ESC][OC]"
					+ updateablePageFontColor
//					+ "[SI]@@1"
//					+ "[SI]AA2"
//					+ "[SI]BB3"
//					+ "[SI]CC4"
//					+ "[SI]DD5"
//					+ "[SI]EE6"
//					+ "[SI]FF7"
//					+ "[SI]GG8"
//					+ "[SI]HH9"
//					+ "[SI]II10"
//					+ "[SI]JJ11"
//					+ "[SI]KK12"
//					+ "[SI]LL13"
//					+ "[SI]MM14"
//					+ "[SI]NN15"
//					+ "[SI]OO16"
//					+ "[SI]@017"
//					+ "[SI]A118"
//					+ "[SI]B219"
//					+ "[SI]C320"
//					+ "[SI]D421"
//					+ "[SI]E522"
//					+ "[SI]F623"
//					+ "[SI]G724"
//					+ "[SI]H825"
//					+ "[SI]I926"
//					+ "[SI]J:27"
//					+ "[SI]K;28"
//					+ "[SI]L<29"
//					+ "[SI]M=30"
//					+ "[SI]N>31"
//					+ "[SI]O?32"
					+ "[SI]G0[GR c2F(ifx.CardHolderFamily)][GR c2F(ifx.CardHolderName)]"
					+ "[SI]H0[GR ifx.actualAppPAN]"
					+ "[SI]I0[GR ifx.Auth_Amt]");
			screenCheckAccountList.add(screenCheckAccountFa);
			getGeneralDao().saveOrUpdate(screenCheckAccountFa);

			ResponseScreen screenCheckAccountEn = new ResponseScreen();
			screenCheckAccountEn.setScreenno("433");
			screenCheckAccountEn.setDesc("بررسی حساب انتقال-موفق-انگلیسی");
			screenCheckAccountEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenCheckAccountEn.setScreenData("433[ESC]P2484[ESC]\\"
					+ "[ESC](1"
//					+ "[ESC][OC]00;67;80m"
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
//			responseCheckAccountShetab.setNextScreen("033");
//			responseCheckAccountShetab.setScreenData("033[ESC]P2084[ESC]\\"
//					+ "[ESC](K"
//					+ "[ESC][OC]B0;80m"
//					+ "[SI]G0[GR c2F(ifx.CardHolderFamily)][GR c2F(ifx.CardHolderName)]"
//					+ "[SI]H0[GR ifx.AppPAN]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responseCheckAccountShetab.setScreen(screenCheckAccountList);
			getGeneralDao().saveOrUpdate(responseCheckAccountShetab);

			FunctionCommandResponse responseCheckAccountPasargad = new FunctionCommandResponse();
			responseCheckAccountPasargad.setName("بررسی حساب انتقال-داخلی-موفق");
			responseCheckAccountPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseCheckAccountPasargad.setNextState("051");
//			responseCheckAccountPasargad.setNextScreen("033");
//			responseCheckAccountPasargad.setScreenData("033[ESC]P2084[ESC]\\"
//					+ "[ESC](K"
//					+ "[ESC][OC]B0;80m"
//					+ "[SI]G0[GR c2F(ifx.CardHolderFamily)][GR c2F(ifx.CardHolderName)]"
//					+ "[SI]H0[GR ifx.AppPAN]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responseCheckAccountPasargad.setScreen(screenCheckAccountList);
			getGeneralDao().saveOrUpdate(responseCheckAccountPasargad);

			/************************/
			FunctionCommandResponse responseTransferShetab = new FunctionCommandResponse();
			responseTransferShetab.setName("انتقال-شتابی-موفق");
			responseTransferShetab.setBeRetain(false);
			responseTransferShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTransferShetab.setNextState("596");
//			responseTransferShetab.setNextScreen("388");
//			responseTransferShetab.setScreenData("388[SI]@@[ESC]P2080[ESC]\\");
			responseTransferShetab.setScreen(screenTransferList);
			responseTransferShetab.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseTransferShetab);

			FunctionCommandResponse responseTransferReceiptExceptionShetab = new FunctionCommandResponse();
			responseTransferReceiptExceptionShetab.setName("انتقال-شتابی-خطای رسید");
			responseTransferReceiptExceptionShetab.setBeRetain(false);
			responseTransferReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTransferReceiptExceptionShetab.setNextState("592");
//			responseTransferReceiptExceptionShetab.setNextScreen("384");
			responseTransferReceiptExceptionShetab.setScreen(screenRecieptList);
			responseTransferReceiptExceptionShetab.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTransferReceiptExceptionShetab);

			/************************/

			FunctionCommandResponse responseTransferPasargad = new FunctionCommandResponse();
			responseTransferPasargad.setName("انتقال-داخلی-موفق");
			responseTransferPasargad.setBeRetain(false);
			responseTransferPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTransferPasargad.setNextState("096");
//			responseTransferPasargad.setNextScreen("388");
//			responseTransferPasargad.setScreenData("388[SI]@@[ESC]P2080[ESC]\\");
			responseTransferPasargad.setScreen(screenTransferList);
			responseTransferPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseTransferPasargad);

			FunctionCommandResponse responseTransferReceiptExceptionPasargad = new FunctionCommandResponse();
			responseTransferReceiptExceptionPasargad.setName("انتقال-داخلی-خطای رسید");
			responseTransferReceiptExceptionPasargad.setBeRetain(false);
			responseTransferReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTransferReceiptExceptionPasargad.setNextState("092");
//			responseTransferReceiptExceptionPasargad.setNextScreen("384");
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
//			responseTimeOut.setNextScreen("398");
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
//			responseCheckAccountTimeOut.setNextScreen("398");
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

			/********************************/
			String textFa = "[GR simpleChangePinReceiptFa()]";
//				headerFa
//					+ "[LF][LF][GR center(GR c2F('رسید تغییر رمز'))]"
//					+ newLine
//					+ lineFa
//					+ receivedDateFa
////					+ newLine + newLine + newLine
//					+ newLine + newLine
//					+ "[GR center(GR c2F('رمز شما با موفقیت تغییر یافت'))]"
////					+ newLine + newLine + newLine
//					+ newLine + newLine
//					+ formatAppPanFa
//					+ "[GR putLF(7)]"
//					+ footerFa;
			String textEn =
				headerEn
				+ "[LF][LF][GR center('PIN Change Receipt')]"
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ formatAppPanEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ "[GR center('Your PIN has been changed successfully')]"
				+ "[GR putLF(7)]"
				+ footerEn;
				;

			String textJournal00 = "[GR simpleChangePinJournal()]" ;
//				"[LF]Pin Change:[SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";


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
//			responsePasargad.setNextScreen("385");
//			responsePasargad.setScreenData("385[FF][SI]@@[ESC]P2140[ESC]\\");
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
//			responseCreditPasargad.setNextScreen("385");
//			responseCreditPasargad.setScreenData("385[FF][SI]@@[ESC]P2140[ESC]\\");
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
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);
			/********************************/

			atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
			atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
			atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
			atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
			atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);

			getGeneralDao().saveOrUpdate(atmRequestPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestPasargadEn);
			getGeneralDao().saveOrUpdate(atmRequestCreditPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestCreditPasargadEn);

			getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutPasargadEn);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadFa);
			getGeneralDao().saveOrUpdate(atmRequestTimeOutCreditPasargadEn);

			requests.add(atmRequestPasargadFa);
			requests.add(atmRequestPasargadEn);
			requests.add(atmRequestCreditPasargadFa);
			requests.add(atmRequestCreditPasargadEn);

			requests.add(atmRequestTimeOutPasargadFa);
			requests.add(atmRequestTimeOutPasargadEn);
			requests.add(atmRequestTimeOutCreditPasargadFa);
			requests.add(atmRequestTimeOutCreditPasargadEn);

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
//				headerFa
//				+ "[LF][LF][GR center(GR c2F('رسید تغییر رمز اینترنتی'))]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ "[GR center(GR c2F('رمز شما با موفقیت تغییر یافت'))]"
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(GR c2F('کد اعتبارسنجی دوم'), GR c2F(ifx.CVV2))]"
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(GR c2F('تاریخ انقضای کارت'), GR c2F(ifx.ExpDt))]"
//				+ "[GR putLF(8)]"
//				+ footerFa;

			String textEn =
				headerEn
				+ "[LF][LF][GR center('Internet PIN Change Receipt')]"
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ formatAppPanEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ "[GR justify(GR ifx.CVV2, 'CVV2')]"
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(GR ifx.ExpDt, 'Expiry date')]"
//				+ newLine + newLine
				+ newLine
				+ newLine
				+ "[GR center('Your PIN has been changed successfully')]"
				+ "[GR putLF(8)]"
				+ footerEn;

			String textJournal00 = "[GR simpleChangeInternetPinJournal()]" ;
//				"[LF]Pin2 Change:[SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";


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
//			responsePasargad.setNextScreen("388");
//			responsePasargad.setScreenData("388[FF][SI]@@[ESC]P2141[ESC]\\");
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
//			responseReceiptExceptionPasargad.setNextScreen("384");
//			responseReceiptExceptionPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionPasargad.setScreen(screenPasargadReceiptList);
			responseReceiptExceptionPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionPasargad);

			//List<ResponseScreen> screenPasargadLengthList = new ArrayList<ResponseScreen>();

		//	ResponseScreen screenPasargadLenghFa = new ResponseScreen();
			//screenPasargadLenghFa.setScreenno("044");
			//screenPasargadLenghFa.setDesc("تغییر رمز اینترنتی-داخلی-خطای طول-فارسی");
			//screenPasargadLenghFa.setLanguage(UserLanguage.FARSI_LANG);
			//screenPasargadLenghFa.setScreenData("044[FF][SI]@@[ESC]P2033[ESC]\\");
			//screenPasargadLengthList.add(screenPasargadLenghFa);
			//getGeneralDao().saveOrUpdate(screenPasargadLenghFa);

			//ResponseScreen screenPasargadLenghEn = new ResponseScreen();
			//screenPasargadLenghEn.setScreenno("444");
			//screenPasargadLenghEn.setDesc("تغییر رمز اینترنتی-داخلی-خطای طول-انگلیسی");
			//screenPasargadLenghEn.setLanguage(UserLanguage.ENGLISH_LANG);
			//screenPasargadLenghEn.setScreenData("444[FF][SI]@@[ESC]P2433[ESC]\\");
			//screenPasargadLengthList.add(screenPasargadLenghEn);
			//getGeneralDao().saveOrUpdate(screenPasargadLenghEn);


		//	FunctionCommandResponse responseLengthExceptionPasargad = new FunctionCommandResponse();
			//responseLengthExceptionPasargad.setName("تغییر رمز اینترنتی-داخلی-خطای طول");
			//responseLengthExceptionPasargad.setBeRetain(false);
			//responseLengthExceptionPasargad.setDispense(null);
			//responseLengthExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			//responseLengthExceptionPasargad.setNextState("064");
			//responseLengthExceptionPasargad.setScreen(screenPasargadLengthList);
			//responseLengthExceptionPasargad.setReceipt(null);
			//getGeneralDao().saveOrUpdate(responseLengthExceptionPasargad);

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
//			responseCreditPasargad.setNextScreen("388");
//			responseCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2141[ESC]\\");
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
//			responseReceiptExceptionCreditPasargad.setNextScreen("384");
//			responseReceiptExceptionCreditPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionCreditPasargad.setScreen(screenCreditRecieptList);
			responseReceiptExceptionCreditPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionCreditPasargad);

		//	List<ResponseScreen> screenCreditLengthList = new ArrayList<ResponseScreen>();
			//ResponseScreen screenCreditPasargadLenghFa = new ResponseScreen();
			//screenCreditPasargadLenghFa.setScreenno("044");
			//screenCreditPasargadLenghFa.setDesc("تغییر رمز اینترنتی-اعتباری داخلی-خطای طول-فارسی");
			//screenCreditPasargadLenghFa.setLanguage(UserLanguage.FARSI_LANG);
			//screenCreditPasargadLenghFa.setScreenData("044[FF][SI]@@[ESC]P2033[ESC]\\");
			//screenCreditLengthList.add(screenCreditPasargadLenghFa);
			//getGeneralDao().saveOrUpdate(screenCreditPasargadLenghFa);

			//ResponseScreen screenCreditPasargadLenghEn = new ResponseScreen();
			//screenCreditPasargadLenghEn.setScreenno("444");
			//screenCreditPasargadLenghEn.setDesc("تغییر رمز اینترنتی-اعتباری داخلی-خطای طول-انگلیسی");
			//screenCreditPasargadLenghEn.setLanguage(UserLanguage.ENGLISH_LANG);
			//screenCreditPasargadLenghEn.setScreenData("444[FF][SI]@@[ESC]P2433[ESC]\\");
			//screenCreditLengthList.add(screenCreditPasargadLenghEn);
			//getGeneralDao().saveOrUpdate(screenCreditPasargadLenghEn);


			//FunctionCommandResponse responseLengthExceptionCredit = new FunctionCommandResponse();
			//responseLengthExceptionCredit.setName("تغییر رمز اینترنتی-اعتباری داخلی-خطای طول");
			//responseLengthExceptionCredit.setBeRetain(true);
			//responseLengthExceptionCredit.setDispense(null);
			//responseLengthExceptionCredit.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
		//	responseLengthExceptionCredit.setNextState("064");
			//responseLengthExceptionCredit.setScreen(screenCreditLengthList);
			//responseLengthExceptionPasargad.setReceipt(null);
			//getGeneralDao().saveOrUpdate(responseLengthExceptionPasargad);



			/*************************/
			List<ResponseScreen> screenCreditTimeoutList = new ArrayList<ResponseScreen>();

			ResponseScreen screenScreenTimeoutFa = new ResponseScreen();
			screenScreenTimeoutFa.setScreenno("398");
			screenScreenTimeoutFa.setDesc("تغییر رمز اینترنتی-time out-فارسی");
			screenScreenTimeoutFa.setLanguage(UserLanguage.FARSI_LANG);
//			screenFa.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
			screenCreditTimeoutList.add(screenScreenTimeoutFa);
			getGeneralDao().saveOrUpdate(screenScreenTimeoutFa);

			ResponseScreen screenCreditTimeoutEn = new ResponseScreen();
			screenCreditTimeoutEn.setScreenno("798");
			screenCreditTimeoutEn.setDesc("تغییر رمز اینترنتی-time out-انگلیسی");
			screenCreditTimeoutEn.setLanguage(UserLanguage.ENGLISH_LANG);
//			screenEn.setScreenData("788[FF][SI]@@[ESC]P2512[ESC]\\");
			screenCreditTimeoutList.add(screenCreditTimeoutEn);
			getGeneralDao().saveOrUpdate(screenCreditTimeoutEn);

			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("تغییر رمز اینترنتی-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenCreditTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);
			/*************************/

			atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
			atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		//	atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.PIN_LENGTH_INCORRECT), responseLengthExceptionPasargad);
			atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestReceiptExceptionPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);

			atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);
			atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);
		//	atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.PIN_LENGTH_INCORRECT), responseLengthExceptionPasargad);
			atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestReceiptExceptionPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responsePasargad);

			atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
			atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		//	atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.PIN_LENGTH_INCORRECT), responseLengthExceptionCredit);
			atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestReceiptExceptionCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);

			atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseCreditPasargad);
			atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);
		//	atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.PIN_LENGTH_INCORRECT), responseLengthExceptionCredit);
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


			atmRequestTimeOutCreditPasargadEn.setOpkey("ICFB   F");
			atmRequestTimeOutCreditPasargadEn.setIfxType(IfxType.CREDIT_CARD_REV_REPEAT_RQ);
			atmRequestTimeOutCreditPasargadEn.setTrnType(TrnType.CREDITCARDDATA);
			atmRequestTimeOutCreditPasargadEn.setCurrency(currency);
			atmRequestTimeOutCreditPasargadEn.setLanguage(UserLanguage.ENGLISH_LANG);
			atmRequestTimeOutCreditPasargadEn.setFit(FITType.CREDIT_PASARGAD);


			/******************************/

			String textFa = "[GR simpleCreditStatementDataReceiptFa()]";
//				headerFa
//				+ "[LF][LF][GR center(GR c2F('اطلاعات آخرین صورتحساب کارت اعتباری'))]"
//				+ newLine
//				+ lineFa
//				+ receivedDateFa
////				+ newLine + newLine + newLine
//				+ newLine + newLine
//				+ formatAppPanFa
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(GR c2F('مبلغ تراکنش ها'), GR amount2F(ifx.CreditTotalTransactionAmount, 15))]"
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(GR c2F('کارمزدها'), GR amount2F(ifx.CreditTotalFeeAmount, 12))]"
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(GR c2F('جریمه دیرکرد'), GR amount2F(ifx.CreditInterest, 12))]"
////				+ newLine + newLine
//				+ newLine
////				+ newLine + newLine
//				+ newLine
//				+ "[GR justify(GR c2F('مانده اعتبار'), GR amount2F(ifx.CreditOpenToBuy, 15))]"
//				+ "[GR putLF(6)]"
//				+ "[GR justify(GR c2F('مبلغ قابل پرداخت'), GR amount2F(ifx.CreditStatementAmount, 15))]"
////				+ newLine + newLine
//				+ newLine
//				+ footerFa;

			String textEn =
				headerEn
				+ "[LF][LF][GR center('Last Credit Card Statement Info')]"
				+ newLine
				+ lineEn
				+ receivedDateEn
//				+ newLine + newLine + newLine
				+ newLine + newLine
				+ formatAppPanEn
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(GR amount2E(ifx.CreditTotalTransactionAmount,15), 'Transaction Amount')]" //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(GR amount2E(ifx.CreditTotalFeeAmount,15), 'Fee')]" //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(GR amount2E(ifx.CreditInterest ,15), 'Late Payment Fee')]" //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
//				+ newLine + newLine
				+ newLine
//				+ newLine + newLine
				+ newLine
				+ "[GR justify(GR amount2E(ifx.CreditOpenToBuy,15), 'Available Credit')]" //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
				+ "[GR putLF(6)]"
				+ "[GR justify(GR amount2E(ifx.CreditStatementAmount,15), 'Payable Amount')]" //dar resid Rials ra nemigozasht baraie hamin amount2E ezafe shod.
//				+ newLine + newLine
				+ newLine
				+ footerEn;

			String textJournal00 = "[GR simpleCreditStatementDataJournal()]";
//				"[LF]Credit Statement:[SO]1REF:[GR ifx.Src_TrnSeqCntr][SO]1PAN:[GR ifx.actualAppPAN][SO]1TIME:[GR ifx.receivedDt][SO]1TERMINAL:[GR ifx.TerminalId]";


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
//			response00.setFunctionCommand(NDCFunctionIdentifierConstants.PRINT_IMMEDIATE);
			response00.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			response00.setNextState("196");
//			response00.setNextScreen("388");
//			response00.setScreenData("388[FF][SI]@@[ESC]P2112[ESC]\\");
			response00.setScreen(screenList);
			response00.setReceipt(receiptList00);
			getGeneralDao().saveOrUpdate(response00);



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
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);

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
	 private List<ATMRequest> thirdPartyPayment(ATMConfiguration configuration,OARResponse oarResponse) throws Exception{
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
				+ "[LF][LF][GR center('Payment Receipt')]"
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
				+ "has been paid to "
				+ openDoubleQuotationEn
				+ "[GR safeEn(ifx.ThirdPartyNameEn)]"
				+ closeDoubleQuotationEn
//				+ "[GR ifx.ThirdPartyNameEn + ' ']"
				+ "institution."
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
			screenCheckOrganizationFa.setScreenData("067[ESC]P2131[ESC]\\"
//					+ "[ESC](K[ESC][OC]00;67;80;m"
					+ "[ESC](K[ESC][OC]"
//					+ updateablePageFontColor
					+ "00;80m"
					+ "[SI]F0[GR c2F(ifx.networkTrnInfo.ThirdPartyName)]"
					+ "[SI]G0[GR ifx.Auth_Amt]");
//					+ "[SI]I0[GR c2F(ifx.thirdPartyPaymentData.company.name)]");
//					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			screenCheckOrganizationList.add(screenCheckOrganizationFa);
			getGeneralDao().saveOrUpdate(screenCheckOrganizationFa);

			ResponseScreen screenCheckOrganizationEn = new ResponseScreen();
			screenCheckOrganizationEn.setScreenno("467");
			screenCheckOrganizationEn.setDesc("تایید پرداخت به خیریه-موفق-انگلیسی");
			screenCheckOrganizationEn.setLanguage(UserLanguage.ENGLISH_LANG);
			screenCheckOrganizationEn.setScreenData("467[ESC]P2531[ESC]\\"
					+ "[ESC](1[ESC][OC]"
					+ "00;80m"
					+ "[SI]F0[GR safeEn(ifx.networkTrnInfo.ThirdPartyNameEn)]"
					+ "[SI]G0[GR ifx.Auth_Amt]");


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

			FunctionCommandResponse responseShetab = new FunctionCommandResponse();
			responseShetab.setName("تایید پرداخت به خیریه-شتابی-موفق");
			responseShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseShetab.setNextState("569");
//			responseShetab.setNextScreen("032");
//			responseShetab.setScreenData("032[ESC]P2086[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]F0[GR ifx.BillID]"
//					+ "[SI]G0[GR ifx.BillPaymentID]"
//					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responseShetab.setScreen(screenCheckOrganizationList);
			getGeneralDao().saveOrUpdate(responseShetab);

			/*****************************/

			FunctionCommandResponse responsePasargad = new FunctionCommandResponse();
			responsePasargad.setName("تایید پرداخت به خیریه-داخلی-موفق");
			responsePasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responsePasargad.setNextState("105");
//			responsePasargad.setNextScreen("032");
//			responsePasargad.setScreenData("032[ESC]P2086[ESC]\\"
//					+ "[ESC](K[ESC][OC]80;m"
//					+ "[SI]F0[GR ifx.BillID]"
//					+ "[SI]G0[GR ifx.BillPaymentID]"
//					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responsePasargad.setScreen(screenCheckOrganizationList);
			getGeneralDao().saveOrUpdate(responsePasargad);

			/*****************************/
			FunctionCommandResponse responseCreditPasargad = new FunctionCommandResponse();
			responseCreditPasargad.setName("تایید پرداخت به خیریه-اعتباری داخلی-موفق");
			responseCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseCreditPasargad.setNextState("169");
//			responseCreditPasargad.setNextScreen("032");
//			responseCreditPasargad.setScreenData("032[ESC]P2086[ESC]\\"
//					+ "[ESC](K"
//					+ "[ESC][OC]B0;80m"
//					+ "[SI]F0[GR ifx.BillID]"
//					+ "[SI]G0[GR ifx.BillPaymentID]"
//					+ "[SI]H0[GR c2F(ifx.BillOrgType)]"
//					+ "[SI]I0[GR ifx.Auth_Amt]");
			responseCreditPasargad.setScreen(screenCheckOrganizationList);
			getGeneralDao().saveOrUpdate(responseCreditPasargad);

			/*****************************/
			/*****************************/
			FunctionCommandResponse responseThirdPartyPaymentShetab = new FunctionCommandResponse();
			responseThirdPartyPaymentShetab.setName("کمک به خیریه-شتابی-موفق");
			responseThirdPartyPaymentShetab.setBeRetain(false);
			responseThirdPartyPaymentShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseThirdPartyPaymentShetab.setNextState("596");
//			responseBillShetab.setNextScreen("388");
//			responseBillShetab.setScreenData("388[FF][SI]@@[ESC]P2126[ESC]\\");
			responseThirdPartyPaymentShetab.setScreen(screenBillPaymentList);
			responseThirdPartyPaymentShetab.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseThirdPartyPaymentShetab);

			FunctionCommandResponse responseReceiptExceptionShetab = new FunctionCommandResponse();
			responseReceiptExceptionShetab.setName("کمک به خیریه-شتابی-خطای رسید");
			responseReceiptExceptionShetab.setBeRetain(false);
			responseReceiptExceptionShetab.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionShetab.setNextState("592");
//			responseReceiptExceptionShetab.setNextScreen("384");
//			responseReceiptExceptionShetab.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionShetab.setScreen(screenRecieptList);
			responseReceiptExceptionShetab.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionShetab);

			/*****************************/

			FunctionCommandResponse responseThirdPArtyPaymentPasargad = new FunctionCommandResponse();
			responseThirdPArtyPaymentPasargad.setName("کمک به خیریه-داخلی-موفق");
			responseThirdPArtyPaymentPasargad.setBeRetain(false);
			responseThirdPArtyPaymentPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseThirdPArtyPaymentPasargad.setNextState("096");
//			responseBillPasargad.setNextScreen("388");
//			responseBillPasargad.setScreenData("388[FF][SI]@@[ESC]P2126[ESC]\\");
			responseThirdPArtyPaymentPasargad.setScreen(screenBillPaymentList);
			responseThirdPArtyPaymentPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseThirdPArtyPaymentPasargad);

			FunctionCommandResponse responseReceiptExceptionPasargad = new FunctionCommandResponse();
			responseReceiptExceptionPasargad.setName("کمک به خیریه-داخلی-خطای رسید");
			responseReceiptExceptionPasargad.setBeRetain(false);
			responseReceiptExceptionPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionPasargad.setNextState("092");
//			responseReceiptExceptionPasargad.setNextScreen("384");
//			responseReceiptExceptionPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionPasargad.setScreen(screenRecieptList);
			responseReceiptExceptionPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionPasargad);

			/*****************************/

			FunctionCommandResponse responseThirdPartyPaymentCreditPasargad = new FunctionCommandResponse();
			responseThirdPartyPaymentCreditPasargad.setName("کمک به خیریه-اعتباری داخلی-موفق");
			responseThirdPartyPaymentCreditPasargad.setBeRetain(false);
			responseThirdPartyPaymentCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseThirdPartyPaymentCreditPasargad.setNextState("196");
//			responseBillCreditPasargad.setNextScreen("388");
//			responseBillCreditPasargad.setScreenData("388[FF][SI]@@[ESC]P2126[ESC]\\");
			responseThirdPartyPaymentCreditPasargad.setScreen(screenBillPaymentList);
			responseThirdPartyPaymentCreditPasargad.setReceipt(receiptList);
			getGeneralDao().saveOrUpdate(responseThirdPartyPaymentCreditPasargad);

			FunctionCommandResponse responseReceiptExceptionCreditPasargad = new FunctionCommandResponse();
			responseReceiptExceptionCreditPasargad.setName("کمک به خیریه-اعتباری داخلی-خطای رسید");
			responseReceiptExceptionCreditPasargad.setBeRetain(false);
			responseReceiptExceptionCreditPasargad.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseReceiptExceptionCreditPasargad.setNextState("192");
//			responseReceiptExceptionCreditPasargad.setNextScreen("384");
//			responseReceiptExceptionCreditPasargad.setScreenData("384[FF][SI]@@[ESC]P2142[ESC]\\");
			responseReceiptExceptionCreditPasargad.setScreen(screenRecieptList);
			responseReceiptExceptionCreditPasargad.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseReceiptExceptionCreditPasargad);

			/*****************************/
			FunctionCommandResponse responseTimeOut = new FunctionCommandResponse();
			responseTimeOut.setName("کمک به خیریه-time out");
			responseTimeOut.setBeRetain(false);
			responseTimeOut.setFunctionCommand(NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT);
			responseTimeOut.setNextState("713");
//			responseTimeOut.setNextScreen("398");
			responseTimeOut.setScreen(screenTimeoutList);
			responseTimeOut.setReceipt(null);
			getGeneralDao().saveOrUpdate(responseTimeOut);
			/*****************************/

			atmRequestShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);
			atmRequestTimeOutShetabFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestShetabFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);

			atmRequestShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentShetab);
			atmRequestTimeOutShetabEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestShetabEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionShetab);

			atmRequestPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
			atmRequestTimeOutPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestTimeOutPasargadSubFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadSubFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPArtyPaymentPasargad);
			atmRequestTimeOutPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), oarResponse);
			atmRequestTimeOutPasargadSubEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestPasargadSubEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionPasargad);

			atmRequestCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);
			atmRequestTimeOutCreditPasargadFa.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestCreditPasargadFa.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);

			atmRequestCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.APPROVED), responseThirdPartyPaymentCreditPasargad);
			atmRequestTimeOutCreditPasargadEn.addAtmResponse(Integer.parseInt(ISOResponseCodes.CARD_EXPIRED), responseTimeOut);
			atmRequestCreditPasargadEn.addAtmResponse(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT, responseReceiptExceptionCreditPasargad);

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

	 /*****End Of Transactions****/
	 /**************/
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


			/********** General Not-Continued States **********/
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.EXPIRY_DATE_MISMATCH), result, "EXPIRY_DATE_MISMATCH"); //41
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.ACQUIRER_NACK), result, "ACQUIRER_NACK"); //43
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //34

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //34

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2123", "2523", null, ATMErrorCodes.ATM_CACH_HANDLER, result, "ATM_CACH_HANDLER");

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.WARM_CARD), result, "WARM_CARD"); //14
//			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.DUPLICATE_LINKED_ACCOUNT), result, "DUPLICATE_LINKED_ACCOUNT"); //33
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.SAF_TRANSMIT_MODE), result, "SAF_TRANSMIT_MODE"); //54
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.INVALID_MERCHANT), result, "INVALID_MERCHANT"); //78

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.BAD_TRANSACTION_TYPE), result, "BAD_TRANSACTION_TYPE"); //39
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.NO_TRANSACTION_ALLOWED), result, "NO_TRANSACTION_ALLOWED"); //66
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.HONOUR_WITH_ID), result, "HONOUR_WITH_ID"); //79
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2116", "2516", null, ATMErrorCodes.ATM_UNDEFINED_OPKEY, result, "ATM_UNDEFINED_OPKEY"); //undefined opkey

			/********** Success Results **********/
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

			createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2117", "2517", null, Integer.parseInt(ISOResponseCodes.INTERNAL_DATABASE_ERROR), result, "INTERNAL_DATABASE_ERROR");
			createATMResponse("098", "387", "787", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2118", "2518", null, Integer.parseInt(ISOResponseCodes.INCORRECT_ONLINE_REFNUMBER), result, "INCORRECT_ONLINE_REFNUMBER");

			/********** General Not-Continued States **********/
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.EXPIRY_DATE_MISMATCH), result, "EXPIRY_DATE_MISMATCH"); //41
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.ACQUIRER_NACK), result, "ACQUIRER_NACK"); //43
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2123", "2523", null, ATMErrorCodes.ATM_CACH_HANDLER, result, "ATM_CACH_HANDLER");

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.WARM_CARD), result, "WARM_CARD"); //14
//			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.DUPLICATE_LINKED_ACCOUNT), result, "DUPLICATE_LINKED_ACCOUNT"); //33
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.SAF_TRANSMIT_MODE), result, "SAF_TRANSMIT_MODE"); //54
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.INVALID_MERCHANT), result, "INVALID_MERCHANT"); //78

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.BAD_TRANSACTION_TYPE), result, "BAD_TRANSACTION_TYPE"); //39
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.NO_TRANSACTION_ALLOWED), result, "NO_TRANSACTION_ALLOWED"); //66
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.HONOUR_WITH_ID), result, "HONOUR_WITH_ID"); //79

			/********** Success Results **********/
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

			/********** General Not-Continued States **********/
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.EXPIRY_DATE_MISMATCH), result, "EXPIRY_DATE_MISMATCH"); //41
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.ACQUIRER_NACK), result, "ACQUIRER_NACK"); //43
			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
//			createATMResponse("707", "386", "786", true, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2103", "2503", receiptListCapture, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE), result, "UNKNOWN_TRANSACTION_SOURCE"); //75
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.TRANSACTION_CODE_MISMATCH), result, "TRANSACTION_CODE_MISMATCH"); //38
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.INVALID_ACCOUNT_STATUS), result, "INVALID_ACCOUNT_STATUS"); //67
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2169", "2569", receiptListNotAcceptable, Integer.parseInt(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED), result, "ORIGINAL_NOT_AUTHORIZED"); //75

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2123", "2523", null, ATMErrorCodes.ATM_CACH_HANDLER, result, "ATM_CACH_HANDLER");

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.WARM_CARD), result, "WARM_CARD"); //14
//			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2160", "2560", null, Integer.parseInt(ISOResponseCodes.SENT_TO_HOST), result, "SENT_TO_HOST"); //56

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.DUPLICATE_LINKED_ACCOUNT), result, "DUPLICATE_LINKED_ACCOUNT"); //33
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.SAF_TRANSMIT_MODE), result, "SAF_TRANSMIT_MODE"); //54
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2162", "2562", null, Integer.parseInt(ISOResponseCodes.INVALID_MERCHANT), result, "INVALID_MERCHANT"); //78

			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.BAD_TRANSACTION_TYPE), result, "BAD_TRANSACTION_TYPE"); //39
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.NO_TRANSACTION_ALLOWED), result, "NO_TRANSACTION_ALLOWED"); //66
			createATMResponse("707", "386", "786", false, NDCFunctionIdentifierConstants.SET_NEXT_STATE_AND_PRINT, "2163", "2563", null, Integer.parseInt(ISOResponseCodes.HONOUR_WITH_ID), result, "HONOUR_WITH_ID"); //79
			
			/********** Success Results **********/
			
			return result;
		}
		private List<Receipt> getCardCaptureReciept() {
			List<Receipt> receiptListCapture = new ArrayList<Receipt>();
			
			Receipt receiptCaptureFa = new Receipt();
			receiptCaptureFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			receiptCaptureFa.setText("[GR simpleCardCaptureReceiptFa()]"
					
//					headerFa
//					+ "[LF][LF][GR center(GR c2F('رسید ضبط کارت'))]"
//					+ newLine
//					+ lineFa
//					+ receivedDateFa
////					+ newLine + newLine + newLine
//					+ newLine + newLine
//					+ formatAppPanFa
////					+ newLine + newLine + newLine
//					+ newLine + newLine
//					+ "[GR center(GR c2F('کارت شما به دلایل امنیتی ضبط شد'))]"
//					+ "[GR putLF(15)]"
//					+ footerFa
			);
			receiptCaptureFa.setLanguage(UserLanguage.FARSI_LANG);
			receiptCaptureFa.setName("ضبط کارت-فارسی");
			getGeneralDao().saveOrUpdate(receiptCaptureFa);
			receiptListCapture.add(receiptCaptureFa);

			Receipt receiptCaptureEn = new Receipt();
			receiptCaptureEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
			receiptCaptureEn.setText(headerEn
					+ "[LF][LF][GR center('Card Capture Reciept')]"
					+ newLine
					+ lineEn
					+ receivedDateEn
//					+ newLine + newLine + newLine
					+ newLine + newLine
					+ formatAppPanEn
//					+ newLine + newLine + newLine
					+ newLine + newLine
					+ "[GR center('The card has been captured for security reasons.')]"
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
		private List<Receipt> notAcceptableRequestReciept(){//??????????????????????????
			List<Receipt> receiptListNotAcceptable = new ArrayList<Receipt>();
					
					Receipt receiptNotAcceptableFa = new Receipt();
					receiptNotAcceptableFa.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
					receiptNotAcceptableFa.setText( "[GR simpleNotAcceptableRequestReceiptFa()]"
							/*headerFa
							+ "[LF][LF][GR center(GR c2F('رسید مشكل امنيتي'))]"
							+ newLine
							+ lineFa
							+ receivedDateFa
//							+ newLine + newLine + newLine
							+ newLine + newLine
							+ formatAppPanFa
//							+ newLine + newLine + newLine
							+ newLine + newLine
							+ "[GR center(GR c2F('درخواست شما به دلايل امنيتي قابل اجرا نميباشد'))]"
							+ "[GR putLF(15)]"
							+ footerFa*/
					);
					receiptNotAcceptableFa.setLanguage(UserLanguage.FARSI_LANG);
					receiptNotAcceptableFa.setName("مشكل امنيتي-فارسی");
					getGeneralDao().saveOrUpdate(receiptNotAcceptableFa);
					receiptListNotAcceptable.add(receiptNotAcceptableFa);

					Receipt receiptNotAcceptableEn = new Receipt();
					receiptNotAcceptableEn.setPrinterFlag(NDCPrinterFlag.PRINT_ON_CUSTOMER_PRINTER_ONLY);
					receiptNotAcceptableEn.setText(headerEn
							+ "[LF][LF][GR center('Security Problem Reciept')]"
							+ newLine
							+ lineEn
							+ receivedDateEn
//							+ newLine + newLine + newLine
							+ newLine + newLine
							+ formatAppPanEn
//							+ newLine + newLine + newLine
							+ newLine + newLine
							+ "[GR center('The requested service is not acceptable for security reasons.')]"
							+ "[GR putLF(15)]"
							+ footerEn
					);
					receiptNotAcceptableEn.setLanguage(UserLanguage.ENGLISH_LANG);
					receiptNotAcceptableEn.setName("مشكل امنيتي-انگلیسی");
					getGeneralDao().saveOrUpdate(receiptNotAcceptableEn);
					receiptListNotAcceptable.add(receiptNotAcceptableEn);
					return receiptListNotAcceptable;
				}

	 /**************/
	 public final static class FITType {
			final static Integer SHETAB = 1;
			final static Integer PASARGAD = 2;
			final static Integer CREDIT_PASARGAD = 3;
		}
	 public GeneralDao getGeneralDao(){
			return GeneralDao.Instance;
	 }

}
