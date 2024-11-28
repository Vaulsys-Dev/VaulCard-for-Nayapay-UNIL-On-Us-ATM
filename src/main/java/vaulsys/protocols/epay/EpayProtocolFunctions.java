package vaulsys.protocols.epay;

import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.customer.Currency;
import vaulsys.message.Message;
import vaulsys.mtn.MTNChargeService;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.epay.base.EpayConstants;
import vaulsys.protocols.epay.base.EpayErrorCodes;
import vaulsys.protocols.epay.base.EpayMsg;
import vaulsys.protocols.exception.exception.CantAddNecessaryDataToIfxException;
import vaulsys.protocols.exception.exception.CantPostProcessBinaryDataException;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.exception.exception.NotMappedIfxToProtocolException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.exception.exception.NotProducedProtocolToBinaryException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class EpayProtocolFunctions implements ProtocolFunctions {
    transient static Logger logger = Logger.getLogger(EpayProtocolFunctions.class);

	@Override
	public ProtocolMessage fromBinary(byte[] rawdata) throws NotParsedBinaryToProtocolException, Exception {
    	InputStream is = new ByteArrayInputStream(rawdata);    	
		XStream xStream = new XStream();
		xStream.alias("command", EpayMsg.class);
		EpayMsg epayMsg = (EpayMsg) xStream.fromXML(is);
//		int lastValidOffset = getLastValidOffset(rawdata);
//		String epay = new String(rawdata, 0, lastValidOffset);
		//TODO: GC Performance
//		epay = removePin2(epay, "pin2");
//		epay = removePin2(epay, "cvv2");
//		epay = removePin2(epay, "oldPin");
//		epay = removePin2(epay, "newPin");
		
		epayMsg.xml = epayMsg.toString();
		
        return epayMsg;
	}

	private String removePin2(String epay, String tag) {
		int firstIndexOf = epay.indexOf("<"+tag+">");
		int lastIndexOf = epay.indexOf("</"+tag+">");
		StringBuilder epay2 = new StringBuilder();
		
		if (firstIndexOf >= 0 && lastIndexOf >= 0) {
			epay2.append(epay.substring(0, firstIndexOf+6));
			epay2.append("?");
			epay2.append(epay.substring(lastIndexOf));
			return epay2.toString();
		}else{
			return epay;
		}		
	}

	private int getLastValidOffset(byte[] rawdata) {
		int result = rawdata.length - 1;
		while ( rawdata[result] == 0 && result >= 0)
			result--;
			
		result += 1;
		return result;
	}

	@Override
	public ProtocolMessage fromIfx(Ifx ifx, EncodingConvertor convertor) throws NotMappedIfxToProtocolException {
        EpayMsg epayMsg = new EpayMsg();

        epayMsg.commandID = EpayConstants.getCommandIdByIfxType(ifx.getIfxType(),ifx.getTrnType());
        
        if (!Util.hasText(ifx.getTrk2EquivData())) {
        	epayMsg.cardNumber = ifx.getAppPAN();
        	
        } else {
        	epayMsg.trk2 = ifx.getTrk2EquivData();
        }
        
//        epayMsg.cardNumber = ifx.getAppPAN();
        epayMsg.transactionDate = MyDateFormatNew.format("yyyy/MM/dd HH:mm:ss", ifx.getTrnDt().toDate());
        epayMsg.Result = EpayErrorCodes.getCode(ifx.getRsCode());
        epayMsg.referenceNumber = ifx.getNetworkRefId();            

        try {
			fillFromIfxTrxSpecificData(epayMsg, ifx);
		} catch (Exception e) {
			logger.error("NotMappedIfxToProtocolException. "+ e.getMessage()+": "+e.getCause().getClass().getSimpleName());
			throw new NotMappedIfxToProtocolException(e.getMessage()+": "+e.getCause().getClass().getSimpleName());
		}

        return epayMsg;		
	}

	private void fillFromIfxTrxSpecificData(EpayMsg epayMsg, Ifx ifx) throws Exception{
        if(IfxType.BILL_PMT_RS.equals(ifx.getIfxType())){
			epayMsg.billIdentificationNum = ifx.getBillID();
	        epayMsg.paymentIdentificationNum = ifx.getBillPaymentID();
	        
        }else if(IfxType.ONLINE_BILLPAYMENT_RS.equals(ifx.getIfxType())|| IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(ifx.getIfxType())){
        	epayMsg.onlineBillRef = ifx.getOnlineBillPaymentRefNum();
        	epayMsg.amount = ifx.getAuth_Amt();
	        	if(ifx.getOnlineBillPaymentData() != null && ifx.getOnlineBillPaymentData().getOnlineBillPayment() != null){
	        		epayMsg.Description = ifx.getOnlineBillPaymentData().getDescription();
	        	}
        	
        }else if(IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(ifx.getIfxType())/*||IfxType.ONLINE_BILLPAYMENT_REFOUND_RS.equals(ifx.getIfxType())*/){
        	epayMsg.amount = ifx.getAuth_Amt();
        	epayMsg.onlineBillRef = ifx.getOnlineBillPaymentRefNum();
        	if(ifx.getOnlineBillPaymentData() != null && ifx.getOnlineBillPaymentData().getOnlineBillPayment() != null){
        		epayMsg.Description = ifx.getOnlineBillPaymentData().getOnlineBillPayment().getDescription();
        	}
        	if(ifx.getOnlineBillPaymentData() != null && ifx.getOnlineBillPaymentData().getOnlineBillPayment() != null){
        		epayMsg.OnlineBillStatus = ifx.getOnlineBillPaymentData().getOnlineBillPayment().getPaymentStatus().toString();
        	}
        	
        }else if(IfxType.PURCHASE_CHARGE_RS.equals(ifx.getIfxType()) && ifx.getChargeData() != null && ISOResponseCodes.isSuccess(ifx.getRsCode())) {
			epayMsg.CardSerial = "IR" + ifx.getChargeData().getCharge().getCardSerialNo();
//			epayMsg.CardSerial = "IR"+StringFormat.formatNew(12, StringFormat.JUST_RIGHT, ifx.getChargeData().getCharge().getCardSerialNo(), '0');
			byte[] decryptedPIN = SecurityComponent.rsaDecrypt(Hex.decode(ifx.getChargeData().getCharge().getCardPIN()));
			epayMsg.CardPin = new String(decryptedPIN);
			epayMsg.CardAmount = ifx.getCharge().getCredit().toString();
			epayMsg.CardRealAmount = MTNChargeService.getRealChargeCredit(ifx.getCharge().getCredit(), ifx.getCharge().getEntity().getCode()).toString(); 			
        }else if (IfxType.THIRD_PARTY_PURCHASE_RS.equals(ifx.getIfxType()) ){
        	epayMsg.companyName = ifx.getThirdPartyName();
        }else if(IfxType.PURCHASE_RS.equals(ifx.getIfxType())) {
        }else if(IfxType.RETURN_RS.equals(ifx.getIfxType())) {
        }else if(IfxType.CHANGE_PIN_BLOCK_RS.equals(ifx.getIfxType())) {
        	
        }else if(IfxType.BAL_INQ_RS.equals(ifx.getIfxType())) { 
        	if(ifx.getAcctBalAvailable() != null && Util.hasText(ifx.getAcctBalAvailableAmt())){
        		epayMsg.AvailableAmount = ifx.getAcctBalAvailableAmt(); 
            	epayMsg.AvailableAmountCurrency = ifx.getAcctBalAvailableCurCode();
            	epayMsg.AccountType = ifx.getAcctBalAvailableType();
            	epayMsg.cardHolderMobileNo = ifx.getCardHolderMobileNo();
        	}
        	
        	if (ifx.getAcctBalLedger() != null && Util.hasText(ifx.getAcctBalLedgerAmt())) {
				epayMsg.CurrentAmount = ifx.getAcctBalLedgerAmt();
				epayMsg.CurrentAmountCurrency = ifx.getAcctBalLedgerCurCode();
				epayMsg.AccountType = ifx.getAcctBalLedgerType();
			}
        	
        	epayMsg.cardNumber = ifx.getAppPAN();
        	
        }else if(IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
        	String name = "";
        	if (Util.hasText(ifx.getCardHolderName())) {
        		name = ifx.getCardHolderName() + " ";
        	}
        	
        	if (Util.hasText(ifx.getCardHolderFamily())){
        		name += ifx.getCardHolderFamily();
        	}
        	
        	epayMsg.Name = name;
        	
        }else if(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
        	String name = "";
        	if (Util.hasText(ifx.getCardHolderName())) {
        		name = ifx.getCardHolderName() + " ";
        	}
        	
        	if (Util.hasText(ifx.getCardHolderFamily())){
        		name += ifx.getCardHolderFamily();
        	}
        	
        	epayMsg.Name = name;
        	
        }
        else if(IfxType.TRANSFER_RS.equals(ifx.getIfxType())) {
        }else if(IfxType.BANK_STATEMENT_RS.equals(ifx.getIfxType())) {
        	if(ifx.getBankStatementData()!=null){
	        	for(BankStatementData d:ifx.getBankStatementData()){
	        		if(epayMsg.statementDataList==null)
	        			epayMsg.statementDataList=new ArrayList<BankStatementData>();
	        		epayMsg.statementDataList.add(d);
	        		
	        	}
        	}
        }
	}

	@Override
	public byte[] toBinary(ProtocolMessage protocolMessage) throws NotProducedProtocolToBinaryException {
		StringBuilder strResponse = new StringBuilder();
		
		EpayMsg epayMsg = (EpayMsg) protocolMessage;
		
		strResponse.append("<Code>" + epayMsg.commandID + "</Code>\n");
		strResponse.append("<ReferenceNumber>" + epayMsg.referenceNumber + "</ReferenceNumber>\n");
		strResponse.append("<Result>" + epayMsg.Result +"</Result>\n");
		strResponse.append("<Message>" + "" + "</Message>\n");
		strResponse.append("<TransactionDate>" + epayMsg.transactionDate + "</TransactionDate>\n");
		
		
		if(EpayConstants.isBillPayMsg(epayMsg)) {
//			strResponse = "<BillPaymentRM>\n" + strResponse;
			strResponse.insert(0, "<BillPaymentRM>\n");
			strResponse.append("</BillPaymentRM>\n");
		}else if(EpayConstants.isOnlineBillPaymentMsg(epayMsg)){
			strResponse.insert(0, "<OnlineBillRM>\n");
			strResponse.append("</OnlineBillRM>\n");			
		}else if(EpayConstants.isPrepareOnlineBillPaymentMsg(epayMsg)){
			strResponse.insert(0, "<PrepareOnlineBillRM>\n");
			
			if(!EpayConstants.isReversalMsg(epayMsg)&& epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED))){
				strResponse.append("<onlineBillRef>" + epayMsg.onlineBillRef + "</onlineBillRef>\n");
				strResponse.append("<Amount>" + epayMsg.amount + "</Amount>\n");
				strResponse.append("<Description>" + epayMsg.Description + "</Description>\n");
				}
			strResponse.append("</PrepareOnlineBillRM>\n");
		}else if(EpayConstants.isOnlineBillPaymentTrackingMsg(epayMsg)){
			strResponse.insert(0, "<OnlineBillTrackingRM>\n");
			if(!EpayConstants.isReversalMsg(epayMsg) && epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED))){
				strResponse.append("<onlineBillRef>" + epayMsg.onlineBillRef + "</onlineBillRef>\n");
				strResponse.append("<Amount>" + epayMsg.amount + "</Amount>\n");
				strResponse.append("<Description>" + epayMsg.Description.toString() + "</Description>\n");
				strResponse.append("<OnlineBillPaymentStatus>" + epayMsg.OnlineBillStatus.toString() + "</OnlineBillPaymentStatus>\n");
			}
			strResponse.append("</OnlineBillTrackingRM>\n");			
		}else if(EpayConstants.isPurchaseChargeMsg(epayMsg)){
			strResponse.insert(0, "<ChargeCardRM>\n");
			if(epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED)) && epayMsg.commandID == 1002) {
				strResponse.append("<Card>\n");
				strResponse.append("<CardPin>"+epayMsg.CardPin+"</CardPin>\n");
				strResponse.append("<CardSerial>"+epayMsg.CardSerial+"</CardSerial>\n");
				strResponse.append("<CardAmount>"+epayMsg.CardAmount+"</CardAmount>\n");
				strResponse.append("<CardRealAmount>"+epayMsg.CardRealAmount+"</CardRealAmount>\n");
				strResponse.append("</Card>\n");
			}
			strResponse.append("</ChargeCardRM>\n");			
		}else if (EpayConstants.isThirdPartyPayment(epayMsg)){
			strResponse.insert(0, "<ThirdPartyPaymentRM>\n");
			if(epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED)) && epayMsg.commandID == 1017) {
				if (Util.hasText(epayMsg.companyName)) {
					strResponse.append("<CompanyName>" + epayMsg.companyName + "</CompanyName>\n");
				} else {
					strResponse.append("<CompanyName></CompanyName>\n");
				}
			}
			strResponse.append("</ThirdPartyPaymentRM>\n");			
		}else if(EpayConstants.isPurchaseTopupMsg(epayMsg)){
			strResponse.insert(0, "<MCITopupRM>\n");
			strResponse.append("</MCITopupRM>\n");			
		}else if(EpayConstants.isPurchaseMsg(epayMsg)){
			strResponse.insert(0, "<EPaymentRM>\n");
			strResponse.append("</EPaymentRM>\n");
		}else if(EpayConstants.isReturnMsg(epayMsg)){
			strResponse.insert(0, "<RefundRM>\n");
			strResponse.append("</RefundRM>\n");
		}else if(EpayConstants.isGeneralPinChangeMsg(epayMsg)){
			strResponse.insert(0, "<PINChangeRM>\n");
			strResponse.append("</PINChangeRM>\n");
		}else if(EpayConstants.isBalInqMsg(epayMsg)){
			strResponse.insert(0, "<GetBalanceRM>\n");
			if(!EpayConstants.isReversalMsg(epayMsg) && epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED))) {
				strResponse.append("<Accounts>\n");
				strResponse.append("<Account>\n");
				strResponse.append("<AccountInfo>"+epayMsg.cardNumber+"</AccountInfo>\n");
				strResponse.append("<Type>"+EpayConstants.ACC_TYPE_MAIN+"</Type>\n");
				strResponse.append("<Kind>"+EpayConstants.getAccountKind(epayMsg.AccountType)+"</Kind>\n");
				strResponse.append("<cardHolderMobileNo>"+epayMsg.cardHolderMobileNo +"</cardHolderMobileNo>\n");
				strResponse.append("<Balances>\n");
				if(Util.hasText(epayMsg.CurrentAmount)){
					strResponse.append("<Balance>\n");
					strResponse.append("<BalanceAmount>"
													+ new Long(epayMsg.CurrentAmount.substring(1)).toString()
													+"</BalanceAmount>\n");
					strResponse.append("<Currency>"+epayMsg.CurrentAmountCurrency+"</Currency>\n");
					strResponse.append("<Type>"+EpayConstants.getBalType(BalType.LEDGER)+"</Type>\n");
					strResponse.append("<Kind>"+epayMsg.CurrentAmount.substring(0,1)+"</Kind>\n");
					strResponse.append("</Balance>\n");
				}
				if(Util.hasText(epayMsg.AvailableAmount)){
					strResponse.append("<Balance>\n");
					strResponse.append("<BalanceAmount>"
													+ new Long(epayMsg.AvailableAmount.substring(1)).toString()
													+"</BalanceAmount>\n");
					strResponse.append("<Currency>"+epayMsg.AvailableAmountCurrency+"</Currency>\n");
					strResponse.append("<Type>"+EpayConstants.getBalType(BalType.AVAIL)+"</Type>\n");
					strResponse.append("<Kind>"+epayMsg.AvailableAmount.substring(0,1)+"</Kind>\n");
					strResponse.append("</Balance>\n");
				}
				strResponse.append("</Balances>\n");
				strResponse.append("</Account>\n");
				strResponse.append("</Accounts>\n");
			}
			strResponse.append("</GetBalanceRM>\n");			
		}else if(EpayConstants.isAuthorizationMsg(epayMsg)){
			strResponse.insert(0, "<CardInformationRM>\n");
			if(epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED))) {
				if(epayMsg.Name!=null)
					strResponse.append("<Name>"+epayMsg.Name+"</Name>\n");
				else
					strResponse.append("<Name></Name>\n");
			}
			strResponse.append("</CardInformationRM>\n");			
		}else if(EpayConstants.isTransferMsg(epayMsg)){
			strResponse.insert(0, "<PayOrderRM>\n");
			strResponse.append("</PayOrderRM>\n");
		}else if (EpayConstants.isAuthorizationCardToAccountMsg(epayMsg)){
			strResponse.insert(0, "<AccountInformationRM>\n");
			if(epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED))) {
				if(epayMsg.Name!=null)
					strResponse.append("<Name>"+epayMsg.Name+"</Name>\n");
				else
					strResponse.append("<Name></Name>\n");
			}
			strResponse.append("</AccountInformationRM>\n");
		}else if (EpayConstants.isTransferCardToAccountMsg(epayMsg)){
			strResponse.insert(0, "<TransferToAccountRM>\n");
			strResponse.append("</TransferToAccountRM>\n");
			
		}else if (EpayConstants.isStatementMsg(epayMsg)){
			strResponse.insert(0, "\n<GetStatementRM>\n");
			if (epayMsg.statementDataList != null && epayMsg.statementDataList.size() > 0) {
				if (!EpayConstants.isReversalMsg(epayMsg) && epayMsg.Result.equals(EpayErrorCodes.getCode(ISOResponseCodes.APPROVED))) {
					strResponse.append("<Statements>\n");
					for (int i = 0; i < epayMsg.statementDataList.size(); i++) {
						strResponse.append("<Statement>\n");

						strResponse.append("<StatementDate>"
								+ MyDateFormatNew.format("yyyy/MM/dd HH:mm:ss", epayMsg.statementDataList.get(i)
										.getTrxDt().toDate()) + "</StatementDate>\n");
						if (epayMsg.statementDataList.get(i).getTrnType().equals("C"))
							strResponse.append("<Type>" + "2" + "</Type>\n");
						else if (epayMsg.statementDataList.get(i).getTrnType().equals("D"))
							strResponse.append("<Type>" + "1" + "</Type>\n");
						strResponse.append("<Amount>" + epayMsg.statementDataList.get(i).getAmount() + "</Amount>\n");
						strResponse.append("<Balance>" + epayMsg.statementDataList.get(i).getBalance() + "</Balance>\n");
						if (epayMsg.statementDataList.get(i).getDescription() != null)
							strResponse.append("<Comment>" + epayMsg.statementDataList.get(i).getDescription()
									+ "</Comment>\n");
						else
							strResponse.append("<Comment>" + "</Comment>\n");
						strResponse.append("</Statement>\n");

					}
					strResponse.append("</Statements>\n");
				}
			}
			strResponse.append("</GetStatementRM>\n");			
		// TASK Task129 [26604] - Authenticate Cart (Pasargad)
		} else if (EpayConstants.isCardAuthenticate(epayMsg)) {
			strResponse.insert(0, "<CardAuthenticateRM>\n");
			strResponse.append("</CardAuthenticateRM>\n");			
			
		}
		
		strResponse.insert(0, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		epayMsg.xml = epayMsg.toString();
		
		return strResponse.toString().getBytes();
	}

	@Override
	public Ifx toIfx(ProtocolMessage protocolMessage, EncodingConvertor convertor) throws NotMappedProtocolToIfxException {
    	EpayMsg epayMsg = (EpayMsg) protocolMessage;
    	
        /** **************** Map Epay Message to IFX **************** */
        Ifx ifx = new Ifx();

//        ifx.setAppPAN(epayMsg.cardNumber.trim());
        
        if (Util.hasText(epayMsg.cardNumber)) {
        	ifx.setAppPAN(epayMsg.cardNumber.trim());
        	
        } else if (Util.hasText(epayMsg.trk2)) {
        	ifx.setTrk2EquivData(epayMsg.trk2.trim());
        	
        	String track2Data = epayMsg.trk2;
			int PANEndIndex = track2Data .indexOf('=');

//    		ifx.setTrk2EquivData(track2Data.substring(1, track2Data.length() - 1)); 
    		if (track2Data.length() > PANEndIndex + 5) {
    			String cardExpDt = track2Data.substring(PANEndIndex + 1, PANEndIndex + 5);
    			ifx.setExpDt(Long.parseLong(cardExpDt)); 
    		}

    		String appPAN = track2Data.substring(0, PANEndIndex);
    		
    		if (Util.hasText(appPAN) && !Util.isValidAppPan(appPAN)) { 
    			Exception e = new Exception("Invalid AppPan: "+ ifx.getSecondAppPan());
    			if (!Util.hasText(ifx.getStatusDesc())) {
    				ifx.setSeverity(Severity.WARN);
    				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
    			}
    			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
    			
    		} else {
    			ifx.setAppPAN(appPAN); 
    			ifx.setDestBankId(appPAN.substring(0, 6)); //ISO 33
    			ifx.setRecvBankId(appPAN.substring(0, 6)); //ISO 100
    		}
        	
        }
        
        if(EpayConstants.isBillPayMsg(epayMsg))
            ifx.setTrnType(TrnType.BILLPAYMENT);
		else if(EpayConstants.isPurchaseChargeMsg(epayMsg))
            ifx.setTrnType(TrnType.PURCHASECHARGE);
		else if(EpayConstants.isPurchaseMsg(epayMsg))
            ifx.setTrnType(TrnType.PURCHASE);
		else if(EpayConstants.isReturnMsg(epayMsg))
            ifx.setTrnType(TrnType.RETURN);
		else if(EpayConstants.isPin1ChangeMsg(epayMsg))
			ifx.setTrnType(TrnType.CHANGEPINBLOCK);
		else if(EpayConstants.isPin2ChangeMsg(epayMsg))
			ifx.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		else if(EpayConstants.isAuthorizationMsg(epayMsg))
			ifx.setTrnType(TrnType.CHECKACCOUNT);
		else if(EpayConstants.isBalInqMsg(epayMsg))
			ifx.setTrnType(TrnType.BALANCEINQUIRY);
		else if(EpayConstants.isTransferMsg(epayMsg))
			ifx.setTrnType(TrnType.TRANSFER);
		else if(EpayConstants.isPurchaseTopupMsg(epayMsg))
            ifx.setTrnType(TrnType.PURCHASETOPUP);
		else if(EpayConstants.isStatementMsg(epayMsg))
            ifx.setTrnType(TrnType.BANKSTATEMENT);
		else if(EpayConstants.isAuthorizationCardToAccountMsg(epayMsg))
			ifx.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		else if(EpayConstants.isTransferCardToAccountMsg(epayMsg))
			ifx.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		else if(EpayConstants.isOnlineBillPaymentMsg(epayMsg))
			ifx.setTrnType(TrnType.ONLINE_BILLPAYMENT);
		else if(EpayConstants.isPrepareOnlineBillPaymentMsg(epayMsg))
			ifx.setTrnType(TrnType.PREPARE_ONLINE_BILLPAYMENT);
		else if(EpayConstants.isOnlineBillPaymentTrackingMsg(epayMsg))
			ifx.setTrnType(TrnType.ONLINE_BILLPAYMENT);
		else if (EpayConstants.isThirdPartyPayment(epayMsg))
			ifx.setTrnType(TrnType.THIRD_PARTY_PAYMENT);
        // TASK Task129 [26604] - Authenticate Cart (Pasargad)
		else if (EpayConstants.isCardAuthenticate(epayMsg))
			ifx.setTrnType(TrnType.CARD_AUTENTICATE);        
	
		else{
			NotMappedProtocolToIfxException e = new NotMappedProtocolToIfxException("Invalid Message Type :" + epayMsg);
        	if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
        	logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
		}


        ifx.setIfxType(EpayConstants.getIfxTypeByCommandID(epayMsg.commandID));
        ifx.getIfxType();
        
		Currency currency = ProcessContext.get().getRialCurrency();//GlobalContext.getInstance().getRialCurrency();
		ifx.setAuth_Currency(currency.getCode());
        ifx.setAuth_CurRate( "1");
        if(epayMsg.amount != null && epayMsg.amount >= 0L) {
	        ifx.setAuth_Amt(epayMsg.amount);
	        ifx.setReal_Amt(epayMsg.amount);
	        ifx.setTrx_Amt(epayMsg.amount);
	        ifx.setSec_Amt(epayMsg.amount);
        }else if(epayMsg.amount == null && !EpayConstants.isReversalMsg(epayMsg)){ 
	        ifx.setAuth_Amt(0L);
	        ifx.setReal_Amt(0L);
	        ifx.setTrx_Amt(0L);
	        ifx.setSec_Amt(0L);
	        
//	        ifx.setSec_CurRate("1");
        }//TODO: throw exception if amount < 0
//        else
//        	throw new NotMappedProtocolToIfxException("Unkonwn message type...");
        
        if(epayMsg.eMail != null && !epayMsg.eMail.trim().equals(""))
        	ifx.setEmail(epayMsg.eMail.trim());
        
        if(epayMsg.IP != null && !epayMsg.IP.trim().equals(""))
        	ifx.setIP(epayMsg.IP.trim());
  
        //TASK Task049 : Epay TransferToCard From subsidiary Account 
        if(Util.hasText(epayMsg.subAccFrom))
        	ifx.setAccTypeFrom(AccType.SUBSIDIARY_ACCOUNT);
        else
        	ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
        


        DateTime origDate = null;
		try {
			origDate = new DateTime(MyDateFormatNew.parse("yyyy/MM/dd HH:mm:ss", epayMsg.transactionDate.trim()));
			ifx.setOrigDt(origDate);        		
			if(DateTime.between(origDate, 10, 10)){
			}else{
				logger.warn("Incorrect OrigDate: " + origDate);
				InvalidBusinessDateException e=new InvalidBusinessDateException("incorret OrigDate: " + origDate);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc((e.getClass().getSimpleName() + ": " + e.getMessage()));
				}
			}
		} catch (Exception e) {
			NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("Invalid transactionDate.", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(  Severity.ERROR);
				ifx.setStatusDesc( (ep.getClass().getSimpleName() + ": " + ep.getMessage()));
			}
			logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
		}
		
		if (Util.hasText(epayMsg.invoiceDate))
			ifx.setInvoiceDate(epayMsg.invoiceDate);
		
		ifx.setTrnDt(origDate);
		
		if(epayMsg.invoiceNumber != null && !epayMsg.invoiceNumber.trim().isEmpty())
			ifx.setInvoiceNumber(epayMsg.invoiceNumber);
		ifx.setNetworkRefId(epayMsg.referenceNumber.trim());
		
		ifx.setSrc_TrnSeqCntr(epayMsg.seqCounter.trim());
		ifx.setMy_TrnSeqCntr(epayMsg.seqCounter.trim());	
		
        if(epayMsg.expireYear != null && epayMsg.expireMonth != null)
        	ifx.setExpDt(epayMsg.expireYear * 100 + epayMsg.expireMonth);
        
        String myBin = ProcessContext.get().getMyInstitution().getBin().toString();

		if (epayMsg.terminalType != null) {
			TerminalType terminalType = new TerminalType(epayMsg.terminalType);
			ifx.setTerminalType(terminalType);

			/*if ( myBin.equals(502229L) || myBin.equals(502908L) || myBin.equals(505416L)) {
				if (TerminalType.MOBILE.equals(terminalType)) {
					logger.info("in pasargad switch, we changed mobile type to internet!");
					ifx.setTerminalType(TerminalType.INTERNET);
				}
			}*/

		} else {
			ifx.setTerminalType(TerminalType.INTERNET);
		}
        
		
		if (epayMsg.origTerminalType != null) {
        	TerminalType origTerminalType = new TerminalType(epayMsg.origTerminalType);
        	ifx.setOrigTerminalType(origTerminalType);
  
/*      	
        	if (myBin.equals(502229L) || myBin.equals(502908L) || myBin.equals(505416L)) {
        		if (TerminalType.MOBILE.equals(terminalType)) {
        			logger.info("in pasargad switch, we changed mobile type to internet!");
        			ifx.setTerminalType(TerminalType.INTERNET);
        		}
        	}
*/
        	
        } else if (ifx.getTerminalType() != null) {
        	ifx.setOrigTerminalType(ifx.getTerminalType());
        	
        }
        
        ifx.setBankId(myBin);
        
        if(Util.hasText(ifx.getAppPAN()) && Util.isValidAppPan(ifx.getAppPAN())) {
	        String fwdBankId = ifx.getAppPAN().substring(0, 6); 	        
	        String bankId = fwdBankId;
	        ifx.setFwdBankId(bankId);
	        ifx.setDestBankId(bankId);
        } else if (Util.hasText(ifx.getAppPAN()) && !Util.isValidAppPan(ifx.getAppPAN())) {
        	NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("cardNumber is not valid appPan...");
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
			}
			logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
        }
        
        if(epayMsg.terminalCode == null ){
        	NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("terminalCode is not given in the message....");
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
			}
			logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
			
        }else{
	        ifx.setTerminalId(epayMsg.terminalCode.toString());
	        ifx.setOrgIdNum(epayMsg.merchantCode.toString());        	
        }
        
        if(epayMsg.pin1 != null && !epayMsg.pin1.trim().equals("")) {    	
        	if (TerminalType.KIOSK_CARD_PRESENT.equals(ifx.getTerminalType())) {
        		
        		Terminal kiosk = GeneralDao.Instance.load(Terminal.class, Long.valueOf(ifx.getTerminalId()));
        		Long inProfileId = kiosk.getOwnOrParentSecurityProfileId();
				Set<SecureKey> inKeySet = kiosk.getKeySet();
				try {
					ifx.setPINBlock(decryptPIN(epayMsg.pin1.trim(), ifx.getAppPAN(), inProfileId, inKeySet));
				} catch (Exception e) {
				logger.error("Catch " + e.getClass().getSimpleName() + "- " + e.getMessage());
//				preProcessPinBlockException(incomingMessage, outgoingMessage);
//				throw new PinBlockException("PinBlock Error for Message number: "+incomingMessage.getId(),true);
				}
        	}
        }
        
        if(epayMsg.pin2 != null && !epayMsg.pin2.trim().equals(""))    	
        	ifx.setPINBlock(epayMsg.pin2.trim());
        
        if(epayMsg.cvv2 != null && !epayMsg.cvv2.trim().equals(""))    	
        	ifx.setCVV2(epayMsg.cvv2.trim());
		
		if (epayMsg.language != null) {
			if (epayMsg.language.trim().equals(EpayConstants.FA_LANG))
				ifx.setUserLanguage(UserLanguage.FARSI_LANG);
			else if (epayMsg.language.trim().equals(EpayConstants.EN_LANG))
				ifx.setUserLanguage(UserLanguage.ENGLISH_LANG);
		}
		
		if (EpayConstants.isBillPayMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)) {
			//Bill payment
			String payIdLong = "";
			String billIdLong = "";
			try {
				billIdLong = String.valueOf(Long.parseLong(epayMsg.billIdentificationNum.trim()));
				payIdLong = String.valueOf(Long.parseLong(epayMsg.paymentIdentificationNum.trim()));
			} catch(Exception e) {
				logger.warn("bad Bill or Payment ID!");
			}
			ifx.setBillPaymentID(payIdLong);
			ifx.setBillID(billIdLong);




			if (billIdLong.equals("0") && payIdLong.equals("0")) {
				String mapMerchant = ProcessContext.get().getProtocolConfig(ProtocolType.EPAY, epayMsg.merchantCode.toString());
				String mapTerminal = ProcessContext.get().getProtocolConfig(ProtocolType.EPAY, epayMsg.terminalCode.toString());
				
				ifx.setBillCompanyCode(Util.integerValueOf(mapMerchant));
				ifx.setThirdPartyTerminalId(Util.longValueOf(mapTerminal));
//				ifx.setBillCompanyCode(null);
//				ifx.setThirdPartyTerminalId(null);
				ifx.setBillOrgType(OrganizationType.UNDEFINED);
				
			} else {
				ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billIdLong));
				ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billIdLong));
				ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billIdLong));
				
			}

//			ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billIdLong));
//			ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billIdLong));
//			ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billIdLong));
		
		}else if(EpayConstants.isPurchaseChargeMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			if (epayMsg.company != null){
		        ifx.setThirdPartyCode(epayMsg.company);
		        
/*
		        if (epayMsg.company.equals(9936L)) {
					ifx.setThirdPartyCode(9935L);
				}
*/		        
	        }else{
	        	NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("cardNumber is not valid appPan...");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
				}
				logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
	        }
			
		}else if (EpayConstants.isThirdPartyPayment(epayMsg) ){
			if (epayMsg.company != null ){
				ifx.setThirdPartyCode(epayMsg.company);
			}
		}else if((EpayConstants.isOnlineBillPaymentMsg(epayMsg) || EpayConstants.isPrepareOnlineBillPaymentMsg(epayMsg) ||
				EpayConstants.isOnlineBillPaymentTrackingMsg(epayMsg)/*|| EpayConstants.isOnlineBillPaymentRefoundMsg(epayMsg)*/)&& 
				!EpayConstants.isReturnMsg(epayMsg)){
			if(epayMsg.onlineBillRef !=null){
				ifx.setOnlineBillPaymentRefNum(epayMsg.onlineBillRef);
			}else{
				NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("Ref number for onlin billpayment is not valid! ");
				if(!Util.hasText(ifx.getStatusDesc())){
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(ep.getClass().getSimpleName()+ " : " + ep.getMessage());
				}
				logger.error(ep.getClass().getSimpleName() + " : " + ep.getMessage());
			}
			
			if (epayMsg.company != null){
		        ifx.setThirdPartyCode(epayMsg.company);
			}			
		}else if(EpayConstants.isPurchaseTopupMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
	        if (epayMsg.phoneNumber != null)
	        	ifx.setTopupCellPhoneNumber(epayMsg.phoneNumber);
	        if (epayMsg.company != null){
	        	ifx.setTopupCompanyCode(epayMsg.company);
		        ifx.setThirdPartyCode(epayMsg.company);
	        }else{
	        	NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("cardNumber is not valid appPan...");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
				}
				logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
	        }

		}else if(EpayConstants.isGeneralPinChangeMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			if(epayMsg.oldPin == null || epayMsg.oldPin.trim().equals("") || epayMsg.newPin == null || epayMsg.newPin.trim().equals("")){
				NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("oldPin or newPin is null or empty...");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
				}
				logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
			}
			ifx.setOldPINBlock(epayMsg.oldPin.trim());
			ifx.setNewPINBlock(epayMsg.newPin.trim());
			
		}else if(EpayConstants.isBalInqMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			//balance inquiry
			
		}else if(EpayConstants.isAuthorizationMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			String srcAppPan = ifx.getAppPAN();
			ifx.setSecondAppPan(srcAppPan);
			if (Util.hasText(epayMsg.destinationCardNum)){ //TASK Task050 :  //Mrs.Pakravan Please review this
				ifx.setAppPAN(epayMsg.destinationCardNum.trim());
				String destBankId = epayMsg.destinationCardNum.trim().substring(0, 6); 	        
				String bankId = destBankId;
				ifx.setFwdBankId(bankId);
				ifx.setDestBankId(bankId);
			}
			
			// authorization
			if(!Util.hasText(epayMsg.destinationCardNum) || !Util.isValidAppPan(epayMsg.destinationCardNum.trim())) {
				NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("destinationCardNum is null or empty...");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
				}
				logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
			} 
			
			String recvBankId = srcAppPan.substring(0, 6);
			ifx.setRecvBankId(recvBankId);
		}else if(EpayConstants.isTransferMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			// transfer
			if( !(ifx.getTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT))&&
					epayMsg.destinationCardNum!=null && !epayMsg.destinationCardNum.trim().equals("")){
			
				if(!Util.hasText(epayMsg.destinationCardNum)  || !Util.isValidAppPan(epayMsg.destinationCardNum.trim())){
					NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("destinationCardNum is null or empty...");
					if (!Util.hasText(ifx.getStatusDesc())) {
						ifx.setSeverity(Severity.ERROR);
						ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
					}
					logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
	
				} else {
					ifx.setSecondAppPan(epayMsg.destinationCardNum.trim());
					if(ifx.getTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT)){
						String des=ProcessContext.get().getMyInstitution().getBin().toString();
						String bankId=des;
						ifx.setFwdBankId(bankId);
						ifx.setDestBankId(bankId);
						ifx.setRecvBankId(bankId);
					}else{
						String destBankId = epayMsg.destinationCardNum.trim().substring(0, 6); 	        
						String bankId = destBankId;
						ifx.setRecvBankId(bankId);
					}
				}
			}
			//TASK Task049 : Epay TransferToCard From subsidiary Account
			if (Util.hasText(epayMsg.subAccFrom) && Util.isAccount(epayMsg.subAccFrom))
				ifx.setSubsidiaryAccFrom(epayMsg.subAccFrom.trim());
			else if (Util.hasText(epayMsg.subAccFrom) && !Util.isAccount(epayMsg.subAccFrom)){
				NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("subAccFrom is not valid account...");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
				}
				logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
			}
			
			
				
	        ifx.setAuth_Amt(epayMsg.payAmount);
	        ifx.setReal_Amt(epayMsg.payAmount);
	        ifx.setTrx_Amt(epayMsg.payAmount);
	        ifx.setSec_Amt(epayMsg.payAmount);
		}else if(EpayConstants.isTransferCardToAccountMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			ifx.setAppPAN(epayMsg.cardNumber.trim());
			ifx.setSecondAppPan(epayMsg.destinationCardNum.trim());
			String des = ProcessContext.get().getMyInstitution().getBin().toString();
			String bankId=des;
			ifx.setFwdBankId(bankId);
			ifx.setDestBankId(epayMsg.cardNumber.trim().substring(0, 6));
//			ifx.setDestBankId(bankId);
			ifx.setRecvBankId(bankId);
			ifx.setAuth_Amt(epayMsg.payAmount);
	        ifx.setReal_Amt(epayMsg.payAmount);
	        ifx.setTrx_Amt(epayMsg.payAmount);
	        ifx.setSec_Amt(epayMsg.payAmount);
		}else if(EpayConstants.isAuthorizationCardToAccountMsg(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			ifx.setAppPAN(epayMsg.destinationCardNum.trim());
			ifx.setSecondAppPan(epayMsg.cardNumber.trim());
			String des=ProcessContext.get().getMyInstitution().getBin().toString();
			String bankId=des;
			ifx.setFwdBankId(bankId);
			ifx.setDestBankId(bankId);
			ifx.setRecvBankId(bankId);
		} else if(EpayConstants.isCardAuthenticate(epayMsg) && !EpayConstants.isReversalMsg(epayMsg)){
			//card authenticate
			//AldTODO Task129
		}
        if(EpayConstants.isReversalMsg(epayMsg) || EpayConstants.isReturnMsg(epayMsg)){
        	try {
				setReversalTransactionInfo(ifx, epayMsg);
			} catch (Exception e) {
				NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("Exception in setting reversal transaction info");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
				}
				logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
			}
        }
        
        ifx.setTransferFromDesc(epayMsg.transferFromDesc);
        ifx.setTransferToDesc(epayMsg.transferToDesc);
       
        return ifx;
	}

    public void setReversalTransactionInfo(Ifx ifx, EpayMsg epayMsg) throws Exception{
		ifx.setOriginalDataElements(new MessageReferenceData());
		
		ifx.getOriginalDataElements().setTrnSeqCounter(epayMsg.originalSeqCounter);
		
		ifx.getOriginalDataElements().setOrigDt(
				new DateTime(MyDateFormatNew.parse("yyyy/MM/dd HH:mm:ss", epayMsg.originalTransactionDate)));
		ifx.getOriginalDataElements().setBankId(ProcessContext.get().getMyInstitution().getBin().toString());
		
		ifx.getOriginalDataElements().setTerminalId(epayMsg.terminalCode.toString());
		ifx.getOriginalDataElements().setAppPAN(ifx.getAppPAN());
		ifx.getOriginalDataElements().setNetworkTrnInfo(epayMsg.originalReferenceNumber);    	
    }
        
	@Override
	public void addOutgoingNecessaryData(Ifx outgoingIFX, Transaction transaction)
		throws CantAddNecessaryDataToIfxException {
	}
	
	@Override
	public void postProcessBinaryMessage(ProcessContext processContext, Message outgoingMessage) throws CantPostProcessBinaryDataException {
		EpayProtocolSecurityFunctions securityFunctions = new EpayProtocolSecurityFunctions();
		SwitchTerminal terminal = null;
				
		terminal = ProcessContext.get().getAcquierSwitchTerminal(outgoingMessage.getChannel().getInstitutionId());
		try {
			securityFunctions.encrypt(terminal.getKeySet(), outgoingMessage);
		} catch (Exception e) {
			logger.error("Cannot encrypt data for transmission..."+ e);
			throw new CantPostProcessBinaryDataException("Cannot encrypt data for transmission...");
		}
	}
	
	@Override
	public void addIncomingNecessaryData(Ifx incomingIFX, Transaction transaction)
	throws CantAddNecessaryDataToIfxException {
		return;
	}
	
	@Override
	public ProtocolMessage outgoingFromIncoming(ProtocolMessage incomingMessage, Ifx incomingIFX, EncodingConvertor convertor) throws Exception {
        EpayMsg inEpayMsg = (EpayMsg) incomingMessage;
        
        EpayMsg outEpayMsg = new EpayMsg();
        
      	outEpayMsg.commandID = inEpayMsg.commandID;
        
        outEpayMsg.referenceNumber = inEpayMsg.referenceNumber;
        outEpayMsg.cardNumber = inEpayMsg.cardNumber;
        outEpayMsg.trk2 = inEpayMsg.trk2;
        outEpayMsg.billIdentificationNum = inEpayMsg.billIdentificationNum;
        outEpayMsg.paymentIdentificationNum = inEpayMsg.paymentIdentificationNum;
        outEpayMsg.transactionDate = inEpayMsg.transactionDate;
        outEpayMsg.Result = EpayErrorCodes.getCode(ISOResponseCodes.INVALID_CARD_STATUS);
        outEpayMsg.Name = "";
        outEpayMsg.AvailableAmount = "-1";
        outEpayMsg.CurrentAmount = "-1";
        
        return outEpayMsg;		
	}
	
	private String decryptPIN(String pin, String appPAN, Long inProfileId, Set<SecureKey> inKeySet) throws SMException, Exception {
		if(pin == null || "".equals(pin))
			return null;

		String pinBlock = SecurityComponent.decryptPINByKey(inProfileId, inKeySet, Hex.decode(pin), appPAN);
		if (Util.hasText(pinBlock))
			return pinBlock;

		return null;
	}

	@Override
	public byte[] preProcessBinaryMessage(Message incoMessage) throws Exception {
		EpayProtocolSecurityFunctions securityFunctions = new EpayProtocolSecurityFunctions();
		SwitchTerminal terminal = null;
				
		terminal = ProcessContext.get().getAcquierSwitchTerminal(incoMessage.getChannel().getInstitutionId());
		 
		securityFunctions.decrypt(terminal.getKeySet(), incoMessage);
				
		return incoMessage.getBinaryData();
	}

	@Override
	public byte[] decryptSecureBinaryMessage(byte[] encryptedData,
			Message incomingMessage) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] encryptBinaryMessage(byte[] rawdata, Message incomingMessage)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
