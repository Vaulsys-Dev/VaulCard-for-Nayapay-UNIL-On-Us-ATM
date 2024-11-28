package vaulsys.protocols.ndc.parsers;

import groovy.lang.Binding;
import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.MCIBillPaymentUtil;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCConsumerRequestMsg;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.protocols.ndc.constants.LastStatusIssued;
import vaulsys.protocols.ndc.constants.NDCConstants;
import vaulsys.protocols.ndc.constants.NDCMessageClassSolicitedUnSokicited;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.transaction.TransactionService;
import vaulsys.util.MyInteger;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public class NDCConsumerRequestMapper {
	transient static Logger logger = Logger.getLogger(NDCConsumerRequestMapper.class);

	public static Ifx toIfx(NDCMsg ndcMessage) throws Exception {
		Ifx ifx = new Ifx();
		try {
			//TODO MNS Performance
			NDCConsumerRequestMsg ndcConsumerMessage = (NDCConsumerRequestMsg) ndcMessage;
			//            ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, ndcConsumerMessage.getLogicalUnitNumber());
			long currentTimeMillis = System.currentTimeMillis();
			logger.debug("Try to lock terminal " + ndcConsumerMessage.getLogicalUnitNumber());
			//            ATMTerminal atm = GeneralDao.Instance.load(ATMTerminal.class, ndcConsumerMessage.getLogicalUnitNumber(), LockMode.UPGRADE);
			boolean cantLock = false;
			try{
				TerminalService.lockTerminal(ndcConsumerMessage.getLogicalUnitNumber().toString(), LockMode.UPGRADE);
			}catch (Exception e) {
				cantLock = true;
				//logger.error(e, e);
			}
			ATMTerminal atm = GeneralDao.Instance.load(ATMTerminal.class, ndcConsumerMessage.getLogicalUnitNumber());
			if(!cantLock)logger.debug("terminal locked.... " + ndcConsumerMessage.getLogicalUnitNumber() + ", " + (System.currentTimeMillis()-currentTimeMillis));

			ifx.setEndPointTerminal(atm);

			// Terminal Code
			ifx.setTerminalId(ndcConsumerMessage.getLogicalUnitNumber().toString()); //ISO 41

			boolean isOARResponse = false;

			if(atm == null){
				throw new Exception("Invalid terminal");
			}

			String opkey = ndcConsumerMessage.operationKeyBuffer;


			if (Util.hasText(ndcConsumerMessage.PINBuffer)) {
				byte[] convertedPinBlock = new byte[8];
				for (int i = 0; i < ndcConsumerMessage.PINBuffer.length(); i += 2) {
					convertedPinBlock[i / 2] = (byte) (((byte) (ndcConsumerMessage.PINBuffer.charAt(i) - '0')) << 4);
					convertedPinBlock[i / 2] |= (byte) (((byte) (ndcConsumerMessage.PINBuffer.charAt(i + 1) - '0')));
				}
				// Pin Block
				ifx.setPINBlock( new String(Hex.encode(convertedPinBlock)).toUpperCase()); //ISO 52
			} else {
				logger.info("pin buffer in ndc consumer message is empty!!");
			}


			ifx.setCoordinationNumber( ndcConsumerMessage.messageCoordinationNumber);
			// Amount Info

			ifx.setAuth_Amt(Long.parseLong(ndcConsumerMessage.dollarAndCentsEntry)); //ISO 4
			ifx.setReal_Amt(ifx.getAuth_Amt());
			ifx.setTrx_Amt(ifx.getAuth_Amt());
			ifx.setSec_Amt(ifx.getAuth_Amt()); //ISO 6

			// Time Info
			Date currentSystemDate = Calendar.getInstance().getTime();
			ifx.setTrnDt(new DateTime(currentSystemDate)); //ISO 7
			ifx.setOrigDt(new DateTime(currentSystemDate)); //ISO 12
			ifx.setPostedDt(new MonthDayDate(currentSystemDate)); //ISO 17
			ifx.setReceivedDt(new DateTime(currentSystemDate));
			// Seq Counter
			Long netRefId = ATMTerminalService.timeVariantToNetworkRefId(ndcConsumerMessage.timeVariantNumber);
			Long trnSeqCntr = new Long(netRefId%10000);
			String nextSeqCntr = (trnSeqCntr.equals(0L))? "1000":trnSeqCntr.toString();
			ifx.setSrc_TrnSeqCntr(nextSeqCntr); //ISO 11
			ifx.setMy_TrnSeqCntr(nextSeqCntr);
			// Terminal Type
			ifx.setTerminalType( TerminalType.ATM); //ISO 25
			//            Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
			String myBin = ""+ProcessContext.get().getMyInstitution().getBin();
			ifx.setBankId(myBin); //ISO 32
			//ifx.setFwdBankId(ifx.getDestBankId()); //Raza commenting
			// Ref. No.
			ifx.setNetworkRefId(netRefId.toString()); //ISO 37
			// OrgIdNum
			//TODO MNS fill such fields in AuthorizationComponent
			ifx.setOrgIdNum(atm.getOwner().getCode().toString()); //ISO 42
			ifx.setCoreBranchCode(atm.getOwner().getCoreBranchCode());
			//            fillTerminalAndOrgInfo(ifx, atm);
			// Set Card Information: AppPAN, SecondAppPan, RecvBankId, DestBankId, ExpDt, Trk2EquivData
			fillGeneralCardInfo(ifx, ndcConsumerMessage.track2Data/*, ndcConsumerMessage.generalBufferB*/);

			if(ifx.getDestBankId() != null) { //check null if not a valid PAN
				ifx.setFwdBankId(ifx.getDestBankId()); //Raza get DeskBankId here, as it is filled by fillGeneralCardInfo
			}
			ifx.setBufferB(ndcConsumerMessage.generalBufferB);
			ifx.setBufferC(ndcConsumerMessage.generalBufferC);
			ifx.setLast_TrnSeqCntr(ndcConsumerMessage.lastTrxSeqCounter);
			ifx.setLastTrxStatusId(ndcConsumerMessage.trxStatusIdentifier);
			ifx.setLastTrxStatusIssue(ndcConsumerMessage.lastStatusIssue);
			ifx.setLastTrxNotesDispensed(ndcConsumerMessage.lastTrxNotesDispensed);

			// MAC
			ifx.setMsgAuthCode( ndcConsumerMessage.MAC);

			//TODO MNS Performance
			//            ATMRequest atmRequest = ATMTerminalService.findATMRequest(atm, opkey);
			ATMRequest atmRequest = ProcessContext.get().getATMRequest(atm.getOwnOrParentConfigurationId(), opkey);
			if (atmRequest == null) {
				//            	NotMappedProtocolToIfxException isoe = new NotMappedProtocolToIfxException("Can't convert NDCMessage to IFX: Not found any request by opkey: " + opkey);
				//    			if (!Util.hasText(ifx.getStatusDesc())) {
				//    				ifx.setSeverity(  Severity.ERROR);
				//    				ifx.setStatusDesc( (isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
				//    			} 
				////    			throw new MandatoryFieldException("Can't convert NDCMessage to IFX: Not found any request by opkey: " + opkey);
				ifx.setOpkey(opkey);
				return ifx;
			}

			ifx.setIfxType( atmRequest.getIfxType());
			ifx.setSecIfxType(atmRequest.getSecondaryIfxType());

			ifx.setTrnType( atmRequest.getTrnType());
			ifx.setSecTrnType(atmRequest.getSecondaryTrnType());

			ifx.setOpkey( atmRequest.getOpkey());
			ifx.setNextOpkey( atmRequest.getNextOpkey());
			ifx.setUserLanguage(atmRequest.getLanguage());
			ifx.setForceReceipt(atmRequest.getForceReceipt());
			ifx.setReceiptOption(atmRequest.getReceiptOption());


			//Mirkamali(Task179): Currency ATM: Read selected amount from ATMCurrencyTerminal
			if(TrnType.PREPARE_WITHDRAWAL.equals(ifx.getTrnType()) || TrnType.WITHDRAWAL_CUR.equals(ifx.getTrnType())) {
				if(Long.parseLong(ndcConsumerMessage.dollarAndCentsEntry) > 0L && Long.parseLong(ndcConsumerMessage.dollarAndCentsEntry) < 10L) {
					ndcConsumerMessage.dollarAndCentsEntry = atm.getCurrency().getAmountFromKey(Integer.valueOf(ndcConsumerMessage.dollarAndCentsEntry));
					ifx.setAuth_Amt(Long.parseLong(ndcConsumerMessage.dollarAndCentsEntry)); //ISO 4
					ifx.setReal_Amt(ifx.getAuth_Amt());
					ifx.setTrx_Amt(ifx.getAuth_Amt());
					ifx.setSec_Amt(ifx.getAuth_Amt()); //ISO 6
				}
			}


			if (Util.hasText(atmRequest.getExtraInformationIfxPath())){
				Binding scriptBinding = new Binding();
				scriptBinding.setProperty("ifx", ifx);
				GlobalContext.getInstance().evaluateScript(atmRequest.getExtraInformationIfxPath().replaceAll("extraInformation", atmRequest.getExtraInformation()), scriptBinding);
			}

			fillCardInfo(ifx, ndcConsumerMessage.track2Data/*, ndcConsumerMessage.generalBufferB*/);

			if (IfxType.CANCEL.equals(ifx.getIfxType())) {
				ifx.setRsCode(ATMErrorCodes.CANCEL + "");

			}else if (IfxType.PREPARE_BILL_PMT.equals(ifx.getIfxType())
					|| IfxType.PREPARE_BILL_PMT_REV_REPEAT.equals(ifx.getIfxType()) ) {
				ifx.setRsCode(ATMErrorCodes.PREPARE_BILL_PMT + "");
				/*
            	List<String> appPans = new ArrayList<String>();
            	//TODO: To be removed
            	appPans.add("6362141000010109");
            	if(appPans.contains(ifx.getAppPAN())){
            		ifx.setRsCode(ErrorCodes.BILL_PAYMENT_SUBSIDY + "");
            	}
            	//TODO: End
				 */

			}else if (IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(ifx.getIfxType()) ||
					IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT.equals(ifx.getIfxType())){
				ifx.setRsCode(ATMErrorCodes.PREPARE_THIRD_PARTY_PAYMENT + "");

			}else if(IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(ifx.getIfxType())||
					IfxType.PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT.equals(ifx.getIfxType())){
				ifx.setRsCode(ATMErrorCodes.PREPARE_ONLINE_BILLPAYMENT + "");

			} else if(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP.equals(ifx.getIfxType()) ||
					IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT.equals(ifx.getIfxType())) {
				ifx.setRsCode(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP+"");

			} else if (IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getIfxType())||
					IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT.equals(ifx.getIfxType())) {
				ifx.setRsCode(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP+"");
			}

			else if(IfxType.PREPARE_RESTRICTION.equals(ifx.getIfxType()) ||
					IfxType.PREPARE_RESTRICTION_REV_REPEAT.equals(ifx.getIfxType()))
				ifx.setRsCode(ATMErrorCodes.PREPARE_RESTRICTION + "");

			//Mirkamali(Task179)
			else if (IfxType.PREPARE_WITHDRAWAL.equals(ifx.getIfxType()) ||
					IfxType.PREPARE_WITHDRAWAL_REV_REPEAT.equals(ifx.getIfxType())){
				ifx.setRsCode(ATMErrorCodes.PREPARE_WITHDRAWAL + "");
			}

			//Mirkamali(Task175): Restriction
			else if (IfxType.PREPARE_RESTRICTION.equals(ifx.getIfxType()) ||
					IfxType.PREPARE_RESTRICTION_REV_REPEAT.equals(ifx.getIfxType())){
				ifx.setRsCode(ATMErrorCodes.PREPARE_RESTRICTION + "");
			}
			/*            else if (IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getIfxType())){
            	ifx.setRsCode(ATMErrorCodes.PREPARE_TRANSFER_CARD_TO_ACCOUNT+"");
//            	ifx.setRsCode(ErrorCodes.APPROVED+"");
            }
			 */

			else if (ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
				ifx.setOriginalDataElements ( new MessageReferenceData());
				ifx.getSafeOriginalDataElements().setMessageType("0200");
				ifx.getSafeOriginalDataElements().setBankId(myBin);
				//            	ifx.getSafeOriginalDataElements().setTrnSeqCounter(atm.getLastTransaction().getSequenceCounter());
			} else if (IfxType.TRANSFER_RQ.equals(ifx.getIfxType()))  {
				ifx.setNetworkRefId (ATMTerminalService.timeVariantToNetworkRefId(ndcConsumerMessage.timeVariantNumber).toString());
				//            ifx.setNetworkRefId (atm.getLastTransaction().getInputMessage().getIfx().getNetworkRefId());
			}else if(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifx.getIfxType())){//gholami(Task45875)
				ifx.setShenaseOfTransferToAccount(ndcConsumerMessage.generalBufferC);
			}


			ifx.setAuth_Amt(atmRequest.getLongAmount(ndcConsumerMessage.generalBufferB, ndcConsumerMessage.generalBufferC));
			ifx.setReal_Amt(ifx.getAuth_Amt());
			ifx.setTrx_Amt(ifx.getAuth_Amt());
			if (amountMustBeZero(ifx.getIfxType(), ifx.getSecIfxType())) {
				ifx.setAuth_Amt(0L);
				ifx.setReal_Amt(ifx.getAuth_Amt());
				ifx.setTrx_Amt(ifx.getAuth_Amt());
			}

			NDCParserUtils.setIfxFields(atmRequest.getBufferB(), ndcConsumerMessage.generalBufferB, ifx);
			NDCParserUtils.setIfxFields(atmRequest.getBufferC(), ndcConsumerMessage.generalBufferC, ifx);

			ifx.setAuth_Currency(atmRequest.getCurrencyId());// ISO 49
			ifx.setSec_Currency(atmRequest.getCurrencyId()); // ISO 51
			ifx.setSec_CurRate("1"); // ISO 10

			if (IfxType.BILL_PMT_RQ.equals(ifx.getIfxType()) ||
					IfxType.BILL_PMT_REV_REPEAT_RQ.equals(ifx.getIfxType()) ||
					IfxType.PREPARE_BILL_PMT.equals(ifx.getIfxType())) {
				//                String shenaseGhabz = ndcConsumerMessage.generalBufferB;
				//                String shenasePardakht = ndcConsumerMessage.generalBufferC;

				String shenaseGhabz;
				String shenasePardakht;

				if (Util.longValueOf(ndcConsumerMessage.dollarAndCentsEntry).equals(NDCConstants.BILL_PAY_TYPE_BARCODE)) {
					String barCode = Util.hasText(ndcConsumerMessage.generalBufferC) ? ndcConsumerMessage.generalBufferB.concat(ndcConsumerMessage.generalBufferC) : ndcConsumerMessage.generalBufferB;
					shenaseGhabz = barCode.substring(0, 13);
					shenasePardakht = barCode.substring(13);
				} else if (Util.longValueOf(ndcConsumerMessage.dollarAndCentsEntry).equals(NDCConstants.BILL_PAY_TYPE_MCI_PHONE_NUMBER)) {
					String phoneNumber = ndcConsumerMessage.generalBufferB;
					shenaseGhabz = phoneNumber;
					shenasePardakht = MCIBillPaymentUtil.RESERVED_PAY_ID_FOR_BILLPMT_WITH_MOBILE_NUMBER;
				} else {
					shenaseGhabz = ndcConsumerMessage.generalBufferB;
					shenasePardakht = ndcConsumerMessage.generalBufferC;
				}

				String billIdLong = "";
				String payIdLong = "";

				try {
					billIdLong = String.valueOf(Long.parseLong(shenaseGhabz));
				} catch(Exception e) {
					logger.warn("buffer B is null!");
				}

				try {
					payIdLong = String.valueOf(Long.parseLong(shenasePardakht));
				} catch(Exception e) {
					logger.warn("buffer C is null!");
				}

				ifx.setRecvBankId(ifx.getAppPAN().substring(0, 6));
				/*** if bufferB & bufferC reset in second transaction ***/

				try {
					if(MCIBillPaymentUtil.isBillPaymentWithMobileNumber(payIdLong)){
						ifx.setBillID(billIdLong);
						ifx.setBillPaymentID(payIdLong);
					}
					else
						BillPaymentUtil.setBillData(ifx, billIdLong, payIdLong);
				} catch(Exception e) {
					logger.warn("Exception in setting Bill Data...");
				}
			} else if(IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(ifx.getIfxType())){
				try{

					ifx.setThirdPartyCode(Long.valueOf(ndcConsumerMessage.generalBufferB));
					Organization organization = OrganizationService.findOrganizationByCode(ifx.getThirdPartyCode(), OrganizationType.THIRDPARTYPURCHASE);
					ThirdPartyVirtualTerminal thPVT = OrganizationService.findThirdPartyVirtualTerminalByOrganization(organization);
					ifx.setThirdPartyName(organization.getName());
					ifx.setThirdPartyNameEn(organization.getNameEn());
					ifx.setThirdPartyTerminalId(thPVT.getCode());

				} catch(Exception e){
					logger.warn("buffer B is null!" + e);
				}

			} else if(IfxType.ONLINE_BILLPAYMENT_RQ.equals(ifx.getIfxType())||
					IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RQ.equals(ifx.getIfxType())||
					IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(ifx.getIfxType())||
					IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(ifx.getIfxType())){
				try{

					ifx.setOnlineBillPaymentRefNum(ndcConsumerMessage.generalBufferB);
					ifx.setAuth_Amt(Long.parseLong(ndcConsumerMessage.dollarAndCentsEntry));
					ifx.setReal_Amt(ifx.getAuth_Amt());
					ifx.setTrx_Amt(ifx.getAuth_Amt());
					//            		ifx.getOnliBillPaymentData().getOnlineBillPayment().setDescription(description)
				}catch(Exception e){
					logger.warn("buffer B or amount buffer is null!");
				}
			}else if (ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType()) &&
					!ISOFinalMessageType.isReversalMessage(ifx.getIfxType())) {
				byte[] convertedNewPinBlock = new byte[8];
				for (int i = 0; i < ndcConsumerMessage.CSPData.length(); i += 2) {
					convertedNewPinBlock[i / 2] = (byte) (((byte) (ndcConsumerMessage.CSPData.charAt(i) - '0')) << 4);
					convertedNewPinBlock[i / 2] |= (byte) (((byte) (ndcConsumerMessage.CSPData.charAt(i + 1) - '0')));
				}
				ifx.setNewPINBlock( new String(Hex.encode(convertedNewPinBlock)).toUpperCase());
			}


			ifx.setSec_Amt(ifx.getAuth_Amt());

			//            if (!isOARResponse && 
			//            		(IfxType.GET_ACCOUNT_RQ.equals(atmRequest.getSecondaryIfxType()) ||
			//            				IfxType.GET_ACCOUNT_REV_REPEAT_RQ.equals(atmRequest.getSecondaryIfxType()))) {
			//            	
			//            	ifx.setIfxType(atmRequest.getSecondaryIfxType()/*IfxType.GET_ACCOUNT_RQ*/);
			//            	ifx.setSecIfxType(atmRequest.getIfxType());
			//            	
			//            	ifx.setTrnType(TrnType.GETACCOUNT);
			//            	ifx.setSecTrnType(atmRequest.getTrnType());
			//            	
			//            } else if (isOARResponse && IfxType.GET_ACCOUNT_RQ.equals(atmRequest.getSecondaryIfxType())) {
			//            	
			//            }

			if (!isOARResponse &&
					(IfxType.GET_ACCOUNT_RQ.equals(atmRequest.getSecondaryIfxType()) ||
							IfxType.GET_ACCOUNT_REV_REPEAT_RQ.equals(atmRequest.getSecondaryIfxType()))) {
				if (TrnType.GETACCOUNT.equals(atm.getLastIncomingTransaction().getIncomingIfx().getSecTrnType()) && ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {

				} else if(!TrnType.GETACCOUNT.equals(atm.getLastIncomingTransaction().getIncomingIfx().getSecTrnType()) && !ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
					ifx.setIfxType(atmRequest.getSecondaryIfxType());
					ifx.setSecIfxType(atmRequest.getIfxType());

					ifx.getSecTrnType();
					ifx.setTrnType(TrnType.GETACCOUNT);
					ifx.setSecTrnType(atmRequest.getTrnType());

				} else if (TrnType.GETACCOUNT.equals(atm.getLastIncomingTransaction().getIncomingIfx().getSecTrnType()) && !ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
					ifx.setIfxType(atmRequest.getSecondaryIfxType());
					ifx.setSecIfxType(atmRequest.getIfxType());

					ifx.setTrnType(TrnType.GETACCOUNT);
					ifx.setSecTrnType(atmRequest.getTrnType());

				} /*else if (!TrnType.GETACCOUNT.equals(atm.getLastIncomingTransaction().getIncomingIfx().getSecTrnType()) && ShetabFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
            		ifx.setIfxType(atmRequest.getSecondaryIfxType());
            		ifx.setSecIfxType(atmRequest.getIfxType());

            		ifx.setTrnType(TrnType.GETACCOUNT);
            		ifx.setSecTrnType(atmRequest.getTrnType());
            	}*/

				else if (!TrnType.GETACCOUNT.equals(atm.getLastIncomingTransaction().getIncomingIfx().getSecTrnType()) && ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
					if ( !ISOFinalMessageType.isContinueIfReceiptErrorRevMessage(ifx.getIfxType())) {
						ifx.setIfxType(atmRequest.getSecondaryIfxType());
						ifx.setSecIfxType(atmRequest.getIfxType());

						ifx.setTrnType(TrnType.GETACCOUNT);
						ifx.setSecTrnType(atmRequest.getTrnType());
					}
					else if (atm.getLastIncomingTransaction().getIncomingIfx().getSecTrnType() != null){
						ifx.setIfxType(atmRequest.getSecondaryIfxType());
						ifx.setSecIfxType(atmRequest.getIfxType());

						ifx.setTrnType(TrnType.GETACCOUNT);
						ifx.setSecTrnType(atmRequest.getTrnType());
					}
				}

			} else if (isOARResponse && IfxType.GET_ACCOUNT_RQ.equals(atmRequest.getSecondaryIfxType())) {

			}

			if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeFrom()) ||
					AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeTo())) {
				String subsidiaryAcct = "";
				ifx.setSubsidiaryAccTo(subsidiaryAcct);
				ifx.setSubsidiaryAccFrom(subsidiaryAcct);
			}


			if(cantLock){
				NotMappedProtocolToIfxException isoe = new NotMappedProtocolToIfxException("Can't convert NDCMessage to IFX:");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc((isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
				}
			}
		} catch (Exception e) {
			logger.error(e, e);
			//throw new NotMappedProtocolToIfxException("Can't convert NDCMessage to IFX:", e);
			NotMappedProtocolToIfxException isoe = new NotMappedProtocolToIfxException("Can't convert NDCMessage to IFX:", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc((isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
			}
		}

		return ifx;
	}

	private static boolean amountMustBeZero(IfxType ifxType, IfxType secIfxType) {
		if (ISOFinalMessageType.isBalanceInqueryMessage(ifxType) || ISOFinalMessageType.isBalanceInqueryRevMessage(ifxType) ||
				ISOFinalMessageType.isBankStatementMessage(ifxType) || ISOFinalMessageType.isBankStatementRevMessage(ifxType) ||
				ISOFinalMessageType.isChangePinBlockMessage(ifxType) ||
				ISOFinalMessageType.isCreditCardStatementMessage(ifxType) ||
				ISOFinalMessageType.isPrepareTranferCardToAccountMessage(ifxType) || ISOFinalMessageType.isPrepareTranferCardToAccountReversalMessage(ifxType) ||
				ISOFinalMessageType.isHotCardMessage(ifxType) ||
				ISOFinalMessageType.isShebaMessage(ifxType) ||
				ISOFinalMessageType.isStockMessage(ifxType) //TASK Task081 : ATM Saham Feature

				//    			||
				//    			ShetabFinalMessageType.isDepositChechAccountMessage(ifxType) ||
				//    			ShetabFinalMessageType.isGetAccountMessage(ifxType)
				//    			ShetabFinalMessageType.isTransferCheckAccountMessage(ifxType)
				)
			return true;

		if (secIfxType == null)
			return false;

		if (ISOFinalMessageType.isBalanceInqueryMessage(secIfxType) || ISOFinalMessageType.isBalanceInqueryRevMessage(secIfxType) ||
				ISOFinalMessageType.isBankStatementMessage(secIfxType) || ISOFinalMessageType.isBankStatementRevMessage(secIfxType) ||
				ISOFinalMessageType.isChangePinBlockMessage(secIfxType) ||
				ISOFinalMessageType.isCreditCardStatementMessage(secIfxType) ||
				ISOFinalMessageType.isPrepareTranferCardToAccountMessage(secIfxType) || ISOFinalMessageType.isPrepareTranferCardToAccountReversalMessage(secIfxType) ||
				ISOFinalMessageType.isHotCardMessage(secIfxType) ||
				ISOFinalMessageType.isShebaMessage(secIfxType) ||
				ISOFinalMessageType.isStockMessage(secIfxType) //TASK Task081 : ATM Saham feature
				)
			return true;
		return false;
	}

	private static void fillGeneralCardInfo(Ifx ifx, String track2Data/*, String bufferB*/) throws ParseException {
		if (!Util.hasText(track2Data))
			return;

		int PANEndIndex = track2Data.indexOf('=');
		if (PANEndIndex < 0)
			PANEndIndex = track2Data.indexOf("?");

		ifx.setTrk2EquivData(track2Data.substring(1, track2Data.length() - 1)); //ISO 35
		if (track2Data.length() > PANEndIndex + 5) {
			String cardExpDt = track2Data.substring(PANEndIndex + 1, PANEndIndex + 5);
			ifx.setExpDt(Long.parseLong(cardExpDt)); //ISO 14
		}

		String appPAN = track2Data.substring(1, PANEndIndex);

		if (Util.hasText(appPAN) && !Util.isValidAppPan(appPAN)) {
			Exception e = new Exception("Invalid AppPan: "+ ifx.getSecondAppPan());
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.WARN);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());

		} else {
			ifx.setAppPAN(appPAN); //ISO 2
			ifx.setDestBankId(appPAN.substring(0, 6)); //ISO 33
			ifx.setRecvBankId(appPAN.substring(0, 6)); //ISO 100
		}
	}

	private static void fillCardInfo(Ifx ifx, String track2Data/*, String bufferB*/) throws ParseException {
		int PANEndIndex = track2Data.indexOf('=');

		if (ifx.getIfxType() == null)
			return;

		String bufferB = ifx.getBufferB();
		String bufferC = ifx.getBufferC();

		//		Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
		String myBin = ""+ProcessContext.get().getMyInstitution().getBin();
		/*	if (IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getIfxType()) && 
				Util.hasText(bufferB) && Util.hasText(bufferC)){

    		ifx.setRecvBankId(Util.longValueOf(track2Data.substring(1, 7)));
    		ifx.setDestBankId(myBin);
    		ifx.setSecondAppPan(track2Data.substring(1, PANEndIndex));
    		ifx.setAppPAN(bufferB+"."+bufferC+".");

    		if (!Util.hasText(ifx.getSecondAppPan()) || !Util.isValidAppPan(ifx.getSecondAppPan())) { 
    			Exception e = new Exception("Invalid Second AppPan: "+ ifx.getSecondAppPan());
    			if (!Util.hasText(ifx.getStatusDesc())) {
    				ifx.setSeverity(Severity.WARN);
    				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
    			}
    			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
    		}

    	} */

		if (ISOFinalMessageType.isPrepareTranferCardToAccountMessage(ifx.getIfxType()) &&
				Util.hasText(bufferB)){

			ifx.setRecvBankId(track2Data.substring(1, 7));
			ifx.setDestBankId(myBin);
			ifx.setSecondAppPan(track2Data.substring(1, PANEndIndex));


			if (!Util.hasText(ifx.getSecondAppPan()) || !Util.isValidAppPan(ifx.getSecondAppPan())) {
				Exception e = new Exception("Invalid Second AppPan: "+ ifx.getSecondAppPan());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			}

		}

		else if ((IfxType.TRANSFER_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType()))
				&& Util.hasText(bufferB)) {

			ifx.setAppPAN(track2Data.substring(1, PANEndIndex));
			ifx.setSecondAppPan(bufferB);

			if (!Util.hasText(ifx.getAppPAN()) || !Util.isValidAppPan(ifx.getAppPAN())) {
				Exception e = new Exception("Invalid TRANSFER_RQ AppPan: "+ ifx.getAppPAN());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			if (!Util.hasText(ifx.getSecondAppPan()) || !Util.isValidAppPan(ifx.getSecondAppPan())) {
				Exception e = new Exception("Invalid Second AppPan: "+ ifx.getSecondAppPan());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			if (TrnType.TRANSFER.equals(ifx.getTrnType())) {
				ifx.setRecvBankId(bufferB.substring(0, 6));

			} else if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())) {
				ifx.setRecvBankId(myBin);

			}

		} else if ((IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifx.getIfxType()))
				&& Util.hasText(bufferB)) {
			ifx.setRecvBankId(track2Data.substring(1, 7));
			ifx.setSecondAppPan(track2Data.substring(1, PANEndIndex));
			ifx.setAppPAN(bufferB);

			if (!Util.hasText(ifx.getAppPAN()) ||
					!Util.isValidAppPan(ifx.getAppPAN())) {
				Exception e = new Exception("Invalid TRANSFER_CHECK_ACCOUNT_RQ AppPan: "+ ifx.getAppPAN());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			if (!Util.hasText(ifx.getSecondAppPan()) ||
					!Util.isValidAppPan(ifx.getSecondAppPan())) {
				Exception e = new Exception("Invalid Second AppPan: "+ ifx.getSecondAppPan());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			ifx.setDestBankId(bufferB.substring(0, 6));

		}

		else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifx.getIfxType()))
		{
			ifx.setRecvBankId(track2Data.substring(1, 7));
			ifx.setSecondAppPan(track2Data.substring(1, PANEndIndex));
			if (!Util.hasText(ifx.getSecondAppPan()) ||
					!Util.isValidAppPan(ifx.getSecondAppPan())) {
				Exception e = new Exception("Invalid TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ Second AppPan: "+ ifx.getSecondAppPan());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			if (TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType())) {



				ifx.setFwdBankId(myBin);
				ifx.setDestBankId(myBin);
			}
		}
		else if ((IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifx.getIfxType()))){ //TASK Task002 : Transfer Card To Account //AldComment Task002 : Add

			ifx.setSecondAppPan(track2Data.substring(1, PANEndIndex));
			ifx.setRecvBankId(myBin);

			if (!Util.hasText(ifx.getSecondAppPan()) || !Util.isValidAppPan(ifx.getSecondAppPan())) {
				Exception e = new Exception("Invalid Second AppPan: "+ ifx.getSecondAppPan());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		} else if  ((IfxType.HOTCARD_INQ_RQ.equals(ifx.getIfxType()) || IfxType.HOTCARD_REV_REPEAT_RQ.equals(ifx.getIfxType()))
				&& Util.hasText(bufferB)) {

			ifx.setAppPAN(bufferB);


			ifx.setSecondAppPan(track2Data.substring(1, PANEndIndex));

			if (!Util.hasText(ifx.getAppPAN()) || !Util.isValidAppPan(ifx.getAppPAN())) {
				Exception e = new Exception("Invalid HOTCARD_RQ AppPan: "+ ifx.getAppPAN());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			if (!Util.hasText(ifx.getSecondAppPan()) || !Util.isValidAppPan(ifx.getSecondAppPan())) {
				Exception e = new Exception("Invalid Second AppPan: "+ ifx.getSecondAppPan());
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.WARN);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			if (TrnType.HOTCARD.equals(ifx.getTrnType())) {
				ifx.setDestBankId(bufferB.substring(0, 6));
				ifx.setRecvBankId(track2Data.substring(1, 7));
			}

		}
	}

	public static ProtocolMessage fromBinary(Integer index, byte[] rawdata, NDCMessageClassTerminalToNetwork messageType, NDCMessageClassSolicitedUnSokicited solicited)
			throws NotParsedBinaryToProtocolException {
		MyInteger offset = new MyInteger(index);
		NDCConsumerRequestMsg ndcMessage = new NDCConsumerRequestMsg();
		ndcMessage.solicited = solicited;
		ndcMessage.messageType = messageType;

		ndcMessage.logicalUnitNumber = Long.valueOf(NDCParserUtils.readUntilFS(rawdata, offset));
		NDCParserUtils.readFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.timeVariantNumber = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.topOfFormPrint = (char) rawdata[offset.value++];
		ndcMessage.messageCoordinationNumber = (char) rawdata[offset.value++];
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.track2Data = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.track3Data = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.operationKeyBuffer = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.dollarAndCentsEntry = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.PINBuffer = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.generalBufferB = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		ndcMessage.generalBufferC = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);

		//        byte decisionData = rawdata[offset.value++];
		if(rawdata[offset.value] == ASCIIConstants.FS)
			offset.value++;

		char decisionData = (char) rawdata[offset.value++];

		if (decisionData == '1') {
			String str = NDCParserUtils.readUntilFS(rawdata, offset);
			if (offset.value != rawdata.length) {
				ndcMessage.track1Identifier = decisionData;
				ndcMessage.track1Data = str;
				NDCParserUtils.readFS(rawdata, offset);
				//                decisionData = rawdata[offset.value++];
				decisionData = (char) rawdata[offset.value++];
			} else {
				ndcMessage.MAC = decisionData + str;
				return ndcMessage;
			}
		}
		if (decisionData == '2') {
			String str = NDCParserUtils.readUntilFS(rawdata, offset);
			if (offset.value != rawdata.length) {
				ndcMessage.trxStatusIdentifier = decisionData;
				ndcMessage.lastTrxSeqCounter = str.substring(0, 4);
				//                ndcMessage.lastStatusIssue = (byte) str.charAt(4);
				ndcMessage.lastStatusIssue = str.charAt(4);

				if (ndcMessage.lastStatusIssue == '0') {
					logger.fatal("lastStatue: " + LastStatusIssued.get(ndcMessage.lastStatusIssue).toString());
				}

				ndcMessage.lastTrxNotesDispensed = str.substring(5);
				NDCParserUtils.readFS(rawdata, offset);
				//                getATMTerminalService().checkLastTransactionStatus(ndcMessage.getLogicalUnitNumber(),
				//                        ndcMessage.lastTrxSeqCounter, ndcMessage.lastStatusIssue, ndcMessage.lastTrxNotesDispensed);
				//                decisionData = rawdata[offset.value++];
				decisionData = (char) rawdata[offset.value++];
			} else {
				ndcMessage.MAC = decisionData + str;
				return ndcMessage;
			}
		}


		while (decisionData == 'A' || decisionData == 'B' || decisionData == 'C' || decisionData == 'D' || decisionData == 'E'
				|| decisionData == 'F' || decisionData == 'G' || decisionData == 'H' || decisionData == 'I' || decisionData == 'J'
				|| decisionData == 'K' || decisionData == 'L' || decisionData == 'Q' || decisionData == 'R' || decisionData == 'S'
				|| decisionData == 'T') {
			String str = NDCParserUtils.readUntilFS(rawdata, offset);
			if (offset.value != rawdata.length) {
				NDCParserUtils.readFS(rawdata, offset);
				decisionData = (char) rawdata[offset.value++];
			} else {
				ndcMessage.MAC = decisionData + str;
				return ndcMessage;
			}
		}

		if (decisionData == 'U') {
			String str = NDCParserUtils.readUntilFS(rawdata, offset);
			if (offset.value != rawdata.length) {
				ndcMessage.CSPDataIdentifier = decisionData;
				ndcMessage.CSPData = str; //NDCParserUtils.readUntilFS(rawdata, offset);
				NDCParserUtils.readFS(rawdata, offset);

				//				decisionData = rawdata[offset.value++];
				decisionData = (char) rawdata[offset.value++];
			} else {
				ndcMessage.MAC = decisionData + str;
				return ndcMessage;
			}
		}
		if (decisionData == 'V') {
			String str = NDCParserUtils.readUntilFS(rawdata, offset);
			if (offset.value != rawdata.length) {
				ndcMessage.confirmCSPDataIndentifier = decisionData;
				ndcMessage.confirmCSPData = str; //NDCParserUtils.readUntilFS(rawdata, offset);
				NDCParserUtils.readFS(rawdata, offset);

				//                decisionData = rawdata[offset.value++];
				decisionData = (char) rawdata[offset.value++];
			} else {
				ndcMessage.MAC = decisionData + str;
				return ndcMessage;
			}
		}

		if (decisionData == 'a' && rawdata[offset.value] == '0') {

			String str = NDCParserUtils.readUntilFS(rawdata, offset);
			if (offset.value != rawdata.length) {
				NDCParserUtils.readFS(rawdata, offset);
				decisionData = (char) rawdata[offset.value++];
			} else {
				ndcMessage.MAC = decisionData + str;
				return ndcMessage;
			}
		}

		if (decisionData == 'C') {
			String str = NDCParserUtils.readUntilFS(rawdata, offset);
			if (offset.value != rawdata.length) {
				NDCParserUtils.readFS(rawdata, offset);
				decisionData = (char) rawdata[offset.value++];
			} else {
				ndcMessage.MAC = decisionData + str;
				return ndcMessage;
			}
		}

		offset.value--;
		ndcMessage.MAC = NDCParserUtils.readUntilFS(rawdata, offset);

		return ndcMessage;
	}

	public static Ifx copyIfx(NDCConsumerRequestMsg ndcConsumerMessage) throws Exception {

		//    	ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, ndcConsumerMessage.getLogicalUnitNumber());
		ATMTerminal atm = GeneralDao.Instance.load(ATMTerminal.class, ndcConsumerMessage.getLogicalUnitNumber(), LockMode.UPGRADE);
		Ifx refIfx = null;
		Ifx ifx = null;
		try {
			refIfx = atm.getLastTransaction().getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
			ifx = MsgProcessor.processor(refIfx);
			ifx.setNetworkTrnInfo(refIfx.getNetworkTrnInfo().copy());
			ifx.setAtmSpecificData(refIfx.getAtmSpecificData().copy());

			Long netRefId = 0L;
			if (Util.hasText(ndcConsumerMessage.timeVariantNumber)) {
				netRefId = ATMTerminalService.timeVariantToNetworkRefId(ndcConsumerMessage.timeVariantNumber);
			}

			Long  trnSeqCntr = new Long(netRefId%10000);
			String nextSeqCntr = (trnSeqCntr.equals(0L))? "1000":trnSeqCntr.toString();

			ifx.setSrc_TrnSeqCntr(nextSeqCntr); // ISO 11
			ifx.setMy_TrnSeqCntr(nextSeqCntr);
			ifx.setNetworkRefId(netRefId.toString());
			ifx.setCoordinationNumber(ndcConsumerMessage.messageCoordinationNumber);
			ifx.setBufferB(ndcConsumerMessage.generalBufferB);
			ifx.setBufferC(ndcConsumerMessage.generalBufferC);
			ifx.setMsgAuthCode(ndcConsumerMessage.MAC);

			//			 ATMRequest atmRequest = getATMTerminalService().findATMRequest(atm, opkey);
			//	            if (atmRequest == null) {
			//	            	NotMappedProtocolToIfxException isoe = new NotMappedProtocolToIfxException("Can't convert NDCMessage to IFX: Not found any request by opkey: " + opkey);
			//	    			if (!Util.hasText(ifx.getStatusDesc())) {
			//	    				ifx.setSeverity(  Severity.ERROR);
			//	    				ifx.setStatusDesc( (isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
			//	    			} 
			//	    			return ifx;
			//	            }

			IfxType ifxType = ifx.getIfxType();
			TrnType trnType = ifx.getTrnType();
			if (ISOFinalMessageType.isGetAccountMessage(ifxType)) {

				if (NDCConstants.KEYS_E.equals(ndcConsumerMessage.generalBufferB)
						|| NDCConstants.KEYS_T.equals(ndcConsumerMessage.generalBufferB)) {

					ifx.setIfxType(IfxType.CANCEL);
					ifx.setSecIfxType(null);

					ifx.setTrnType(TrnType.CANCEL);
					ifx.setSecTrnType(null);

					ifx.setRsCode(ATMErrorCodes.CANCEL + "");
				} else {

					Ifx refRsIfx = atm.getLastTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
					ifx.setSubsidiaryAccFrom(refRsIfx.getSubsidiaryAccByIndex(ndcConsumerMessage.generalBufferB));
					ifx.setAccTypeFrom(AccType.SUBSIDIARY_ACCOUNT);

					ifx.setIfxType(ifx.getSecIfxType());
					ifx.setSecIfxType(ifxType);

					ifx.setTrnType(ifx.getSecTrnType());
					ifx.setSecTrnType(trnType);
				}
			}

		} catch (Exception e) {
			logger.error(e, e);
			if (refIfx == null || ifx == null)
				throw new NotMappedProtocolToIfxException("Can't convert NDCMessage to IFX:", e);
			ISOException isoe = new ISOException("Can't convert NDCMessage to IFX:", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc((isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
			}
		}
		return ifx;
	}
}
