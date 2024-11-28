package vaulsys.migration;

import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.security.exception.SMException;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class MigrationDataService {
	private static final Logger logger = Logger.getLogger(MigrationDataService.class);
	
	public static Boolean isNeededUpdatePin1Data(TerminalType terminal){
		if(terminal.equals(TerminalType.POS) ||
		   terminal.equals(TerminalType.ATM) ||
		   terminal.equals(TerminalType.PINPAD))
			return true;
		else if(terminal.equals(TerminalType.INTERNET)||
				terminal.equals(TerminalType.MOBILE) ||
				terminal.equals(TerminalType.VRU))
			return false;
		else 
			return null;
	}
	
	public static Boolean isSpecialRsCodeToSendNegin(String error){
		return (
				error.equals(ISOResponseCodes.WARM_CARD) ||
				error.equals(ISOResponseCodes.ACQUIRER_REVERSAL) ||
				error.equals(ISOResponseCodes.TRANSACTION_CODE_MISMATCH) ||
				error.equals(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE) ||
				error.equals(ISOResponseCodes.EXPIRY_DATE_MISMATCH) ||
				error.equals(ISOResponseCodes.ACQUIRER_NACK) ||
				error.equals(ISOResponseCodes.INVALID_ACCOUNT_STATUS) ||
				error.equals(ISOResponseCodes.TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION)
				);
	}
	
	public static Boolean isAcceptedRsCodeForUpdate(String error){
		return (
				error.equals(ISOResponseCodes.APPROVED) ||
				error.equals(ISOResponseCodes.INTERNAL_DATABASE_ERROR) ||
				error.equals(ISOResponseCodes.HOST_NOT_PROCESSING) ||
				error.equals(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE) ||
				error.equals(ISOResponseCodes.CASH_TRANSACTION_NOT_ALLOWED) ||
				error.equals(ISOResponseCodes.HONOUR_WITH_ID) ||
				error.equals("53") //Migrated BON Card
		);
		
	}
	
	public static Boolean isAcceptedIfxTypeForUpdate(IfxType type){
		if(type.equals(IfxType.BAL_INQ_RS ) ||
				type.equals(IfxType.BILL_PMT_RS) ||
				type.equals(IfxType.BANK_STATEMENT_RS) ||
				type.equals(IfxType.PURCHASE_CHARGE_RS) ||
				type.equals(IfxType.PURCHASE_RS) ||
				type.equals(IfxType.PURCHASE_TOPUP_RS) ||
//				type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) ||
				type.equals(IfxType.WITHDRAWAL_RS) ||
				
				type.equals(IfxType.CHANGE_PIN_BLOCK_RS)
				)
			return true;			

		return false;
	}
	
	public static Boolean checkingMigrationType(CardMigrationTransferStatusType migType){
		return true;
		//felan true barmigardone ta badan dorost beshe ba tavajoh be migration type haie ke ezafe mishe.
//		if(migType.equals(MigrationType.NOT_TRANSFER))
//			return false;
//		else
//			return true;
	}
	
	public static Boolean isChangedMainField(MigrationData migratedData, MigrationRecord migRec) throws SMException{
		
		if (ISOFinalMessageType.isChangePinBlockMessage(migRec.getIfxType())) {
			if(TrnType.CHANGEINTERNETPINBLOCK.equals(migRec.getTrnType())){
				if(migratedData.getPin2TransactionDate() == null){
					return true;
				}else if(migRec.getReceivedDtLong() > migratedData.getPin2TransactionDate()){
					logger.debug("card information has been changed");
					return true;
				}
			}
			if(TrnType.CHANGEPINBLOCK.equals(migRec.getTrnType())){
				if(migratedData.getPin1TransactionDate() == null){
					return true;
				}else if(migRec.getReceivedDtLong() > migratedData.getPin1TransactionDate()){
					logger.debug("card information has been changed");
					return true;
				}
			}
			return false;
		}
		
		if (migratedData.getSendToNegin() != null && migratedData.getSendToNegin())
			return true;
		
		if(isNeededUpdatePin1Data(migRec.getTerminalType()) && migratedData.getIsValidPin1()){
//			int trk2lenIncoming = migRec.getTrk2EquivData().length();
//			int trk2lenData = migratedData.getNeginTrack2().length();
			if(
//					!migRec.getTrk2EquivData().substring(0, trk2lenIncoming - 2).equals(migratedData.getNeginTrack2().substring(0, trk2lenData - 2)) ||			   
				!migRec.getTranslatedPINBlock().equals(migratedData.getNeginFirstPinBlock())
				){
				if(migratedData.getPin1TransactionDate() == null){
					return true;
				}else if(migRec.getReceivedDtLong() > migratedData.getPin1TransactionDate()){
						logger.debug("card information has been changed");
						return true;
				}
			}
			
		} else if(!isNeededUpdatePin1Data(migRec.getTerminalType()) && migratedData.getIsValidPin2()) {
			
			boolean checkTrxDate = false;
			boolean hasData = false;
			
			if (migRec.getCVV2() != null && Util.hasText(migRec.getCVV2().trim()) && Long.parseLong(migRec.getCVV2()) > 0) {
				hasData = true;
				if( migratedData.getNeginCVV2() == null ||
						!Util.hasText(migratedData.getNeginCVV2().trim()) ||
						Long.parseLong(migRec.getCVV2()) != Long.parseLong(migratedData.getNeginCVV2()))
					checkTrxDate = true;
			}
			
			if (migRec.getExpDt() != null && migRec.getExpDt() > 0) {
				hasData = true;
				if (migratedData.getNeginExpDt() == null ||
						!migRec.getExpDt().equals(migratedData.getNeginExpDt()))
					checkTrxDate = true;
			}
			
			if (Util.hasText(migRec.getPINBlock())) {
				hasData = true;
				if (!Util.hasText(migratedData.getNeginSecondPinBlock()) ||
						!migRec.getTranslatedPINBlock().equals(migratedData.getNeginSecondPinBlock()))
					checkTrxDate = true;
			}
			
			if (hasData) {
				if (checkTrxDate) {
					if(migratedData.getPin2TransactionDate() == null){
						return true;
					}else if(migRec.getReceivedDtLong() > migratedData.getPin2TransactionDate()){
						logger.debug("card information has been changed");
						return true;
					}
				}
			} else {
				return false;
			}
			
//			if( Long.parseLong(migRec.getCVV2()) != Long.parseLong((migratedData.getNeginCVV2())) ||
//					!migRec.getExpDt().equals(migratedData.getNeginExpDt())||
//					!migRec.getTranslatedPINBlock().equals(migratedData.getNeginSecondPinBlock())){//||
//					//(pinKeyIsChange(migRec.getIfxType())&&!migRec.getNewPINBlock().equals(null)) ){
//				if(migratedData.getPin2TransactionDate() == null){
//					return true;
//				}else if(migRec.getReceivedDtLong() > migratedData.getPin2TransactionDate()){
//					logger.debug("card information has been changed");
//					return true;
//				}
//			}
		}
		
		return true;
	} 
	
	public static Boolean findAccounts(String appPan, List<String> /*String bayad beshe az jense un table ke mikhaim besazim badan*/list){
		Map<String,Object> param=new HashMap<String, Object>();
		param.put("neginapp", appPan);
		String st="select migdata.fanapAppPan from MigrationData migdata where migdata.neginAppPan= :neginapp";
		MigrationData neginApp=(MigrationData)GeneralDao.Instance.findUniqueObject(st, param);
		
		if(neginApp!=null){
			
			String st2="from newTable newt where newt.AppPan= :neginapp ";
			list=GeneralDao.Instance.find(st2, param);
			return true;
		}
		else
			return false;
	}
	
	public static Boolean isValidNeginPINBlock(Ifx ifx, MigrationData migrationData) {
		//tasmim migirim ke isvalinneginpinblk1 ro seda kone ya isvalidneginpinblk2 ro.
		return true;
	}
	
	public static Boolean isValidNeginPinBlk1(String neginAppPan,String pinBlk){//check shavad ke pinblk be che formi inja estefade mishavad(code hast ya chi:D)
		Map<String,Object> param=new HashMap<String, Object>();
		param.put("neginapp", neginAppPan);
		String st="select migdata.neginFirstPinBlock from MigrationData migdata " +
				"where migdata.neginAppPan= :neginapp";
		MigrationData migdata=(MigrationData)GeneralDao.Instance.findUniqueObject(st, param);
		if(migdata==null)
			return false;
		if(migdata.getNeginFirstPinBlock().equals(pinBlk))
			return true;
		else
			return false;
	}
	
	public static Boolean isValidNeginPinBlk2(String neginAppPan,String pinBlk){
		Map<String,Object> param=new HashMap<String, Object>();
		param.put("neginapp", neginAppPan);
		String st="select migdata.neginSecondPinBlock from MigrationData migdata " +
				"where migdata.neginAppPan= :neginapp";
		MigrationData migdata=(MigrationData)GeneralDao.Instance.findUniqueObject(st, param);
		if(migdata==null)
			return false;
		if(migdata.getNeginSecondPinBlock().equals(pinBlk))
			return true;
		else 
			return false;
	}
	public static void insertPIN1Data(MigrationRecord migRec, boolean sendToNegin) throws SMException{
		MigrationData m=new MigrationData();
		m.setNeginAppPan(migRec.getAppPAN());
		m.setNeginTrack2(migRec.getTrk2EquivData());
		m.setPin1TransactionDate(migRec.getReceivedDtLong());
		m.setPin1TransactionId(migRec.getTransactionId());
		m.setPin1RsCode(migRec.getRsCode());
		m.setIsValidPin1(true);
		
		m.setSendToNegin(sendToNegin);
		m.setChangedFlagTrxId(migRec.getTransactionId());
		m.setChangedFlagDate(migRec.getReceivedDtLong());
		m.setMigrationStatus(CardMigrationTransferStatusType.NOT_TRANSFER);
		
		if (TrnType.CHANGEPINBLOCK.equals(migRec.getTrnType())) {
			m.setNeginFirstPinBlock(migRec.getTranslatedNewPINBlock());
			
		} else if (TrnType.CHANGEINTERNETPINBLOCK.equals(migRec.getTrnType())) {
			m.setNeginFirstPinBlock(migRec.getTranslatedPINBlock());
			m.setNeginSecondPinBlock(migRec.getTranslatedNewPINBlock());
			m.setNeginCVV2(migRec.getCVV2());
			m.setNeginExpDt(migRec.getExpDt());
			m.setPin2TransactionDate(migRec.getReceivedDtLong());
			m.setPin2TransactionId(migRec.getTransactionId());
			m.setPin2RsCode(migRec.getRsCode());
			m.setIsValidPin2(true);
			
		} else {
			m.setNeginFirstPinBlock(migRec.getTranslatedPINBlock());
			
		}
		GeneralDao.Instance.save(m);
	}
	public static void insertPIN2Data(MigrationRecord migRec, boolean sendToNegin) throws SMException{
		MigrationData m=new MigrationData();
		m.setNeginAppPan(migRec.getAppPAN());
		m.setNeginCVV2(migRec.getCVV2());
		m.setNeginExpDt(migRec.getExpDt());
		m.setPin2TransactionDate(migRec.getReceivedDtLong());
		m.setPin2TransactionId(migRec.getTransactionId());
		m.setPin2RsCode(migRec.getRsCode());
		m.setIsValidPin2(true);
		
		m.setSendToNegin(sendToNegin);
		m.setChangedFlagTrxId(migRec.getTransactionId());
		m.setChangedFlagDate(migRec.getReceivedDtLong());
		m.setMigrationStatus(CardMigrationTransferStatusType.NOT_TRANSFER);
		
		if(TrnType.CHANGEINTERNETPINBLOCK.equals(migRec.getTrnType()))
			m.setNeginSecondPinBlock(migRec.getTranslatedNewPINBlock());
		else
			m.setNeginSecondPinBlock(migRec.getTranslatedPINBlock());
		GeneralDao.Instance.save(m);
		
	}
	
	public static void fillOnLineData(Ifx ifx, Long trxId) throws Exception{
		
		MigrationRecord migRec = new MigrationRecord(ifx, trxId);
		try {

			if (!ISOFinalMessageType.isResponseMessage(ifx.getIfxType()))
				return;
			
			if (isSpecialRsCodeToSendNegin(ifx.getRsCode())) {
				MigrationData migData = getMigrationData(ifx.getAppPAN());
				if (migData != null) {
					
					String query = "update MigrationData mig set " +
							" mig.sendToNegin = true, " +
							" mig.changedFlagTrxId = :flagTrxId, " +
							" mig.changedFlagDate = :flagDate " +
							" where mig.neginAppPan= :neginappPan";
			
					Map<String,Object> param=new HashMap<String, Object>();
					param.put("neginappPan", ifx.getAppPAN());
					param.put("flagTrxId", trxId);
					param.put("flagDate", ifx.getReceivedDtLong());
					
					GeneralDao.Instance.executeUpdate(query, param);
					
					return;
				}
			}
			
			if (!isAcceptedRsCodeForUpdate(ifx.getRsCode()) ||
					!isAcceptedIfxTypeForUpdate(ifx.getIfxType()))
				return;
			
			MigrationData migData = getMigrationData(ifx.getAppPAN());
			
			if(migData != null) {
				logger.debug("appPan is saved before!");
				if(isChangedMainField(migData, migRec) && checkingMigrationType(migData.getMigrationStatus())) {
					
					if ( (ISOResponseCodes.HONOUR_WITH_ID.equals(ifx.getRsCode()))
							&& migData.getMigrationStatus().equals(CardMigrationTransferStatusType.TRANSFER))
						ifx.setRsCode(ISOResponseCodes.HOST_LINK_DOWN);
					
					Boolean neededUpdatePin1Data = isNeededUpdatePin1Data(migRec.getTerminalType());
					
					if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode())
							&& migData.getMigrationStatus().equals(CardMigrationTransferStatusType.TRANSFER)
							&& (IfxType.BAL_INQ_RS.equals(ifx.getIfxType()) || IfxType.BANK_STATEMENT_RS.equals(ifx.getIfxType()))) {
						if (Boolean.TRUE.equals(neededUpdatePin1Data) && !migData.getIsValidPin1())
							ifx.setRsCode(ISOResponseCodes.HOST_LINK_DOWN);
						
						if (Boolean.FALSE.equals(neededUpdatePin1Data) && !migData.getIsValidPin2())
							ifx.setRsCode(ISOResponseCodes.HOST_LINK_DOWN);
					}
					
					if (ifx.getRsCode().equals("53") 
							&& migData.getMigrationStatus().equals(CardMigrationTransferStatusType.TRANSFER)
							&& ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType()))
						ifx.setRsCode(ISOResponseCodes.APPROVED);
					
					if(Boolean.TRUE.equals(neededUpdatePin1Data)) {
						logger.debug("update pin1 data, trx: " + migRec.getTransactionId());
						String query = "update MigrationData mig set mig.neginAppPan= :neginapp, " +
								" mig.neginTrack2= :negintrk2, " +
								" mig.neginFirstPinBlock= :neginpinblk, " +
								" mig.pin1TransactionDate= :trxtime, " +
								" mig.pin1TransactionId= :trxid, "+
								" mig.pin1RsCode= :rscode, " +
								" mig.sendToNegin = false, " +
								" mig.changedFlagTrxId = :flagTrxId, " +
								" mig.changedFlagDate = :flagDate," +
								" mig.isValidPin1 = true ";
						
						Map<String,Object> param=new HashMap<String, Object>();
						param.put("neginapp", migRec.getAppPAN());
						param.put("negintrk2", migRec.getTrk2EquivData());
						param.put("trxtime", migRec.getReceivedDtLong());
						param.put("rscode", migRec.getRsCode());
						param.put("trxid", migRec.getTransactionId());
						param.put("flagTrxId", migRec.getTransactionId());
						param.put("flagDate", migRec.getReceivedDtLong());
						param.put("neginappPan", migData.getNeginAppPan());
						
						if(TrnType.CHANGEPINBLOCK.equals(migRec.getTrnType()))
							param.put("neginpinblk",migRec.getTranslatedNewPINBlock());
							
						else if(TrnType.CHANGEINTERNETPINBLOCK.equals(migRec.getTrnType())) {
							param.put("neginpinblk",migRec.getTranslatedPINBlock());
							
							query += " , mig.neginCVV2 = :cvv2, " +
									" mig.neginExpDt = :expDt, " +
									" mig.neginSecondPinBlock = :neginSecPinBlock, " +
									" mig.pin2TransactionDate = :pin2TrxDate, " +
									" mig.pin2TransactionId = :pin2trxid, "+
									" mig.pin2RsCode= :rscode, " +
									" mig.isValidPin2 = true ";
							
							param.put("cvv2",ifx.getCVV2());
							param.put("expDt",ifx.getExpDt());
							param.put("neginSecPinBlock",migRec.getTranslatedNewPINBlock());
							param.put("pin2TrxDate",migRec.getReceivedDtLong());
							param.put("pin2trxid",migRec.getTransactionId());
								
						} else
							param.put("neginpinblk",migRec.getTranslatedPINBlock());
						
						query += " where mig.neginAppPan= :neginappPan";
						GeneralDao.Instance.executeUpdate(query, param);
						
					} else {
						logger.debug("update pin2 data, trx: " + migRec.getTransactionId());
						String query="update MigrationData mig set mig.neginAppPan= :neginapp, " +
								" mig.neginCVV2 = :negincvv, " +
								" mig.neginExpDt = :neginxpdate, " +
								" mig.neginSecondPinBlock = :neginpinblk, " +
								" mig.pin2TransactionDate = :trxtime, " +
								" mig.pin2TransactionId = :trxid, " +
								" mig.pin2RsCode = :rscode, " +
								" mig.sendToNegin = false, " +
								" mig.changedFlagTrxId = :flagTrxId, " +
								" mig.changedFlagDate = :flagDate," +
								" mig.isValidPin2 = true ";
						
						Map<String,Object> param = new HashMap<String, Object>();
						param.put("neginapp", migRec.getAppPAN());
						param.put("negincvv", migRec.getCVV2());
						param.put("neginxpdate", migRec.getExpDt());
						param.put("trxtime",migRec.getReceivedDtLong());
						param.put("rscode", migRec.getRsCode());
						param.put("trxid", migRec.getTransactionId());
						param.put("flagTrxId", migRec.getTransactionId());
						param.put("flagDate", migRec.getReceivedDtLong());
						param.put("neginappPan", migData.getNeginAppPan());
						
						if (TrnType.CHANGEINTERNETPINBLOCK.equals(migRec.getTrnType())) {
							param.put("neginpinblk",migRec.getTranslatedNewPINBlock());
							
						} else {
							param.put("neginpinblk",migRec.getTranslatedPINBlock());
						}
						
						query += " where mig.neginAppPan= :neginappPan";
						GeneralDao.Instance.executeUpdate(query, param);
					}
				}
			} else {
				
				insertNewData(migRec, false);
			}
		}catch(Exception e){
			logger.error(e,e);
			logger.debug("trx: " + migRec.getTransactionId());
			throw new Exception(e);
		}
	}

	private static void insertNewData(MigrationRecord migrationRecord, boolean sendToNegin) throws SMException {
		logger.debug("insert new data, trx: " + migrationRecord.getTransactionId());
		if(isNeededUpdatePin1Data(migrationRecord.getTerminalType()))
			insertPIN1Data(migrationRecord, sendToNegin);
		else
			insertPIN2Data(migrationRecord, sendToNegin);
	}

	public static void searchAndUpdate(MigrationRecord migRec){
		try{
			
			if (!isAcceptedRsCodeForUpdate(migRec.getRsCode()) ||
					!isAcceptedIfxTypeForUpdate(migRec.getIfxType()))
				return;
			
			MigrationData migData = getMigrationData(migRec.getAppPAN());
			
			if(migData!=null){
				if(isChangedMainField(migData, migRec) && checkingMigrationType(migData.getMigrationStatus())) {
					
					if(isNeededUpdatePin1Data(migRec.getTerminalType())){
						
						String query = "update MigrationData mig set mig.neginAppPan= :neginapp, " +
								" mig.neginTrack2= :negintrk2, " +
								" mig.neginFirstPinBlock= :neginpinblk, " +
								" mig.pin1TransactionDate= :trxtime, " +
								" mig.pin1TransactionId= :trxid, "+
								" mig.pin1RsCode= :rscode " +
								" mig.sendToNegin = false " +
								" where mig.neginAppPan= :neginappPan";
						
						Map<String,Object> param=new HashMap<String, Object>();
						param.put("neginapp", migRec.getAppPAN());
						param.put("negintrk2", migRec.getTrk2EquivData());
						param.put("neginpinblk",migRec.getTranslatedPINBlock());
						param.put("trxtime", migRec.getReceivedDtLong());
						param.put("rscode", migRec.getRsCode());
						param.put("trxid", migRec.getTransactionId());
						param.put("neginappPan", migData.getNeginAppPan());
						if(ISOFinalMessageType.isChangePinBlockMessage(migRec.getIfxType()))
							param.put("neginpinblk",migRec.getTranslatedNewPINBlock());
						else
							param.put("neginpinblk",migRec.getTranslatedPINBlock());
						
						GeneralDao.Instance.executeUpdate(query, param);
					}else{
						String query="update MigrationData mig set mig.neginAppPan= :neginapp, " +
								" mig.neginCVV2= :negincvv, " +
								" mig.neginExpDt= :neginxpdate, " +
								" mig.neginSecondPinBlock= :neginpinblk, " +
								" mig.pin2TransactionDate= :trxtime, " +
								" mig.pin2TransactionId= :trxid " +
								" mig.pin2RsCode= :rscode, " +
								" mig.sendToNegin = false " +
								" where mig.neginAppPan= :neginappPan";
						
						Map<String,Object> param = new HashMap<String, Object>();
						param.put("neginapp", migRec.getAppPAN());
						param.put("negincvv", migRec.getCVV2());
						param.put("neginxpdate", migRec.getExpDt());
						param.put("neginpinblk",migRec.getTranslatedPINBlock());
						param.put("trxtime",migRec.getReceivedDtLong());
						param.put("rscode", migRec.getRsCode());
						param.put("trxid", migRec.getTransactionId());
						param.put("neginappPan", migData.getNeginAppPan());
						if(ISOFinalMessageType.isChangePinBlockMessage(migRec.getIfxType()))
							param.put("neginpinblk",migRec.getTranslatedNewPINBlock());
						else
							param.put("neginpinblk",migRec.getTranslatedPINBlock());
						
						GeneralDao.Instance.executeUpdate(query, param);
					}
				}
			} else {
				if(isNeededUpdatePin1Data(migRec.getTerminalType()))
					insertPIN1Data(migRec, false);
				else
					insertPIN2Data(migRec, false);
			}
		}catch(Exception e){
			System.err.println(e);
			System.err.println("trx: " + migRec.getTransactionId());
			new Exception(e);
		}
	}
	
		public static void setChangedFields(Ifx outgoingIfx, ISOMsg isoMsg, StringBuilder CVV2, StringBuilder secAppPAN) throws ISOException {
//		if (ShetabFinalMessageType.isRequestMessage(outgoingIfx.getIfxType()) &&
//				!FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(GlobalContext.getInstance().getMyInstitution().getRole())) {
//			CVV2.append(outgoingIfx.getCVV2());
//			secAppPAN.append(outgoingIfx.getSecondAppPan());
//			return;
//		}
		
		if (outgoingIfx.getIfxDirection().equals(IfxDirection.INCOMING)) {
			CVV2.append(outgoingIfx.getCVV2());
			secAppPAN.append(outgoingIfx.getSecondAppPan());
			return;
		}
		
		MigrationData migData = outgoingIfx.getMigrationData();
		MigrationData migSecData = outgoingIfx.getMigrationSecondData();
		
//		if (FinancialEntityRole.MY_SELF.equals(GlobalContext.getInstance().getMyInstitution().getRole())) {
		if (FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {
			if (!outgoingIfx.getDestBankId().equals(639347L) &&
					!outgoingIfx.getDestBankId().equals(502229L)) {
//				logger.debug("avoid 3 inst, return by ");
				CVV2.append(outgoingIfx.getCVV2());
				if (migSecData != null) {
					secAppPAN.append(migSecData.getFanapAppPan());
				} else {
					secAppPAN.append(outgoingIfx.getSecondAppPan());
				}
				return;
			}
		}
		
		
		if (migData != null) {
			logger.debug("return changed field in response message, ifx: " + outgoingIfx.getId() + " from  migrationData: " + migData.getId());
			
			isoMsg.set(2, migData.getNeginAppPan());
			//System.out.println("MigrationDataService:: Field-2 [" + isoMsg.getString(2) + "]"); //Raza TEMP
			
			TerminalType terminalType = outgoingIfx.getTerminalType();
	
			if (terminalType != null && isNeededUpdatePin1Data(terminalType) != null) {
			
				if (isNeededUpdatePin1Data(terminalType)) {
					
					if (Util.hasText(outgoingIfx.getTrk2EquivData()))
						isoMsg.set(35, migData.getNeginTrack2());
					//System.out.println("MigrationDataService:: Field-35 [" + isoMsg.getString(35) + "]"); //Raza TEMP
					
					if (Util.hasText(outgoingIfx.getPINBlock()))
						isoMsg.set(52, migData.getNeginFirstPinBlock());
					//System.out.println("MigrationDataService:: 1 Field-52 [" + isoMsg.getString(52) + "]"); //Raza TEMP
				} else {
					if (Util.hasText(outgoingIfx.getCVV2()))
						CVV2.append(migData.getNeginCVV2());
					
					if (Util.hasText(outgoingIfx.getPINBlock()))
						isoMsg.set(52, migData.getNeginSecondPinBlock());

					//System.out.println("MigrationDataService:: 2 Field-2 [" + isoMsg.getString(52) + "]"); //Raza TEMP
					
					if (outgoingIfx.getExpDt() != null)
						isoMsg.set(14, migData.getNeginExpDt());

					//System.out.println("MigrationDataService:: Field-14 [" + isoMsg.getString(14) + "]"); //Raza TEMP
					
				}
			}
			
			isoMsg.set(33, migData.getNeginAppPan().substring(0, 6));
			//System.out.println("MigrationDataService:: Field-33 [" + isoMsg.getString(33) + "]"); //Raza TEMP
			if (Util.hasText(outgoingIfx.getSecondAppPan()))
				isoMsg.set(100, outgoingIfx.getSecondAppPan().substring(0, 6));
				
			else
				isoMsg.set(100, migData.getNeginAppPan().substring(0, 6));

			//System.out.println("MigrationDataService:: Field-100 [" + isoMsg.getString(100) + "]"); //Raza TEMP
		} 
		
		if (migSecData != null) {
			logger.debug("return changed field in response message, ifx: " + outgoingIfx.getId() + " from  migrationSecondData: " + migSecData.getId());
			
			if (
//					IfxType.TRANSFER_RQ.equals(outgoingIfx.getIfxType()) ||
//					IfxType.TRANSFER_RS.equals(outgoingIfx.getIfxType()) ||
					IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(outgoingIfx.getIfxType()) ||
					IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ.equals(outgoingIfx.getIfxType())
//					|| IfxType.TRANSFER_TO_ACCOUNT_RS.equals(outgoingIfx.getIfxType())
					) {
				
				if (isNeededUpdatePin1Data(outgoingIfx.getTerminalType())) {
					
					if (Util.hasText(outgoingIfx.getTrk2EquivData()))
						isoMsg.set(35, migSecData.getNeginTrack2());

					//System.out.println("MigrationDataService:: Field-35 [" + isoMsg.getString(35) + "]"); //Raza TEMP
					
					if (Util.hasText(outgoingIfx.getPINBlock()))
						isoMsg.set(52, migSecData.getNeginFirstPinBlock());

					//System.out.println("MigrationDataService:: 3 Field-52 [" + isoMsg.getString(52) + "]"); //Raza TEMP
					
				} else {
					if (Util.hasText(outgoingIfx.getCVV2()))
						CVV2.append(migSecData.getNeginCVV2());
					
					if (Util.hasText(outgoingIfx.getPINBlock()))
						isoMsg.set(52, migSecData.getNeginSecondPinBlock());

					//System.out.println("MigrationDataService:: 4 Field-2 [" + isoMsg.getString(52) + "]"); //Raza TEMP

					if (outgoingIfx.getExpDt() != null)
						isoMsg.set(14, migSecData.getNeginExpDt());

					//System.out.println("MigrationDataService:: Field-14 [" + isoMsg.getString(14) + "]"); //Raza TEMP
					
				}
			}
			
			secAppPAN.append(migSecData.getNeginAppPan());
			isoMsg.set(100, /*outgoingIfx.getSecondAppPan()*/migSecData.getNeginAppPan().substring(0, 6));

			//System.out.println("MigrationDataService:: Field-100 [" + isoMsg.getString(100) + "]"); //Raza TEMP
		}
		
		if (!Util.hasText(CVV2.toString()))
			CVV2.append(outgoingIfx.getCVV2());

		if (!Util.hasText(secAppPAN.toString()))
			secAppPAN.append(outgoingIfx.getSecondAppPan());
	}
	
	public static void setRequiredFields(Ifx ifx) throws PanPrefixServiceNotAllowedException, SMException {
		IfxType ifxType = ifx.getIfxType();
		if (ISOFinalMessageType.isResponseMessage(ifxType))
			return;

		if (ISOFinalMessageType.isChangePinBlockMessage(ifxType))
			return;
			
		
		MigrationData migData = null;
		MigrationData migDataSecAppPAN = null;
		
		boolean checkValidationData = true;
		boolean checkValidationSecData = true;
		
		boolean sendMigDataToCMS = false;
		boolean sendMigSecDataToCMS = false;
		
		if (ifx.getDestBankId() != null && ifx.getDestBankId().equals(639347L)) {
			migData = getMigrationData(ifx.getAppPAN());
		}
			
		String secondAppPan = ifx.getSecondAppPan();
		if (ISOFinalMessageType.isTransferMessage(ifxType)) {
			if (ifx.getRecvBankId() != null && ifx.getRecvBankId().equals(639347L)) {
				migDataSecAppPAN = getMigrationData(secondAppPan);
			}
		} 

		if (IfxType.TRANSFER_RQ.equals(ifxType) ||
				ISOFinalMessageType.isTransferFromMessage(ifxType)) {
			checkValidationSecData = false;
			
		} else if (ISOFinalMessageType.isTransferCheckAccountMessage(ifxType) ||
				ISOFinalMessageType.isTransferToMessage(ifxType)) {
			checkValidationData = false;
		}
		
		if (migData != null && CardMigrationTransferStatusType.IN_TRANSFER.equals(migData.getMigrationStatus()))
			throw new PanPrefixServiceNotAllowedException("AppPAN: " + migData.getNeginAppPan() + " in migration process");
		
		if (migDataSecAppPAN != null && CardMigrationTransferStatusType.IN_TRANSFER.equals(migDataSecAppPAN.getMigrationStatus()))
			throw new PanPrefixServiceNotAllowedException("SecAppPAN: " + migDataSecAppPAN.getNeginAppPan() + " in migration process");
		
//		checkValidationData = isNeedCheckValidationData(ifx.getTerminalType(), ifxType);
		if (migData != null  && isNeededReplaceWithFanapAppPan(migData, ifx.getTerminalType(), ifxType, checkValidationData))
			sendMigDataToCMS = true;
		
		if (migDataSecAppPAN != null && isNeededReplaceWithFanapAppPan(migDataSecAppPAN, ifx.getTerminalType(), ifxType, checkValidationSecData))
			sendMigSecDataToCMS = true;
		
		if (!sendMigDataToCMS && !sendMigSecDataToCMS)
			return;
		
		boolean isNeededUpdatePin1Data = true;
		MigrationRecord migRec = new MigrationRecord(ifx, ifx.getTransactionId());
		Boolean pinTrans_enable = true;
		
		if (!ISOFinalMessageType.isReversalMessage(ifxType)) {
			try {
				pinTrans_enable = ifx.getTransaction().getInputMessage().getChannel().getPinTransEnable();
			} catch(Exception e) {
				logger.error("Exception in getting channel property, "+e, e);
			}
			
			if (sendMigDataToCMS) {
			if (isNeededUpdatePin1Data(ifx.getTerminalType())) {
				if (!ISOFinalMessageType.isTransferCheckAccountMessage(ifxType) &&
						!ISOFinalMessageType.isTransferToMessage(ifxType)) {
					
					try {
//						int trk2len = ifx.getTrk2EquivData().length();
						if (ifx.getEndPointTerminal() == null){
							logger.debug("No terminal on ifx: " + ifx.getId());
							return;							
						}
						if (ifx.getEndPointTerminal().getKeySet() == null) {
							logger.debug("Terminal: " + ifx.getEndPointTerminal().getId() + " dos'nt have keySet!");
							return;
						}
						
						if (migData != null && 
								(
//								!ifx.getTrk2EquivData().substring(0, trk2len - 2).equals(migData.getNeginTrack2().substring(0, trk2len - 2)) ||
									!migData.getNeginFirstPinBlock().equals(migRec.getTranslatedPINBlockToCheckNegin(ifx.getEndPointTerminal(), pinTrans_enable)))) {
							logger.debug("FirstPinBlock of negin different to myData!");
//							logger.debug("Track2 or FirstPinBlock of negin different to myData!");
							return;
						}
					} catch (SMException e) {
						logger.error("Exception in translating pin block from terminal: " + ifx.getEndPointTerminal().getId(), e);
						return;
					}
				} 
			} else {
				isNeededUpdatePin1Data = false;
				if (!ISOFinalMessageType.isTransferCheckAccountMessage(ifxType)
						&& !ISOFinalMessageType.isTransferToMessage(ifxType)) {
					if (migData != null) {
						if (ifx.getCVV2() != null && Util.hasText(ifx.getCVV2().trim()) && Long.parseLong(ifx.getCVV2()) > 0) {
							if (migData.getNeginCVV2() == null ||
									!Util.hasText(migData.getNeginCVV2().trim()) ||
									Long.parseLong(ifx.getCVV2()) != Long.parseLong(migData.getNeginCVV2())) {
								logger.debug("CVV2 of negin different to myData!");
								return;
							}
						}

						if (ifx.getExpDt() != null && ifx.getExpDt() > 0) {
							if (!ifx.getExpDt().equals(migData.getNeginExpDt())) {
								logger.debug("ExpDate of negin different to myData!");
								return;
							}
						}
						
						if (ifx.getEndPointTerminal().getKeySet() == null) {
							logger.debug("Terminal: " + ifx.getEndPointTerminal().getId() + " dos'nt have keySet!");
							return;
						}

						if (!migData.getNeginSecondPinBlock().equals(migRec.getTranslatedPINBlockToCheckNegin(ifx.getEndPointTerminal(), pinTrans_enable))) {
							logger.debug("SecondPinBlock of negin different to myData!");
							return;
						}

					}
				}
			}
			}
		}
		
		if (sendMigDataToCMS) {
			
			ifx.setAppPAN(migData.getFanapAppPan());
			ifx.setDestBankId("502229");
			ifx.setFwdBankId("502229");
			
			if (ifx.getOriginalDataElements() != null) {
				ifx.getOriginalDataElements().setAppPAN(migData.getFanapAppPan());
				ifx.getOriginalDataElements().setFwdBankId("502229");
			}
			
			
			if (ISOFinalMessageType.isTransferMessage(ifxType)) {
				if (sendMigSecDataToCMS)
					ifx.setRecvBankId("502229");
			} else 
				ifx.setRecvBankId("502229");
			
			ifx.setActualAppPAN(migData.getNeginAppPan());
			
			if (!ISOFinalMessageType.isReversalMessage(ifxType)) {
				if (checkValidationData) {
					if (isNeededUpdatePin1Data) {
						if (Util.hasText(ifx.getPINBlock()))
							ifx.setPINBlock(migRec.getTranslatedPINBlockToSendCMS(ifx.getEndPointTerminal(), migData.getFanapAppPan(),
									migData.getFanapFirstPinBlock(), pinTrans_enable));
						ifx.setTrk2EquivData(migData.getFanapTrack2());
					} else {
						if (checkValidationData) {
							if (Util.hasText(ifx.getPINBlock()))
								ifx.setPINBlock(migRec.getTranslatedPINBlockToSendCMS(ifx.getEndPointTerminal(), migData.getFanapAppPan(),
										migData.getFanapSecondPinBlock(), pinTrans_enable));

							if (Util.hasText(ifx.getCVV2()))
								ifx.setCVV2(migData.getFanapCVV2());

							if (ifx.getExpDt() != null && migData.getFanapExpDt() != null)
								ifx.setExpDt(migData.getFanapExpDt());
						}
					}
				}

			}
			ifx.setMigrationData(migData);
		}
		
		if (sendMigSecDataToCMS) {
			
			ifx.setSecondAppPan(migDataSecAppPAN.getFanapAppPan());
			ifx.setActualSecondAppPAN(secondAppPan);
			ifx.setRecvBankId("502229");
			
			ifx.setMigrationSecondData(migDataSecAppPAN);
		}
	}
	
	//must be saved the number of incorrect pin, and reset counter after correct pin
	private static boolean isActiveNeginCard(Ifx ifx, MigrationData migData) {
		return true;
	}
	
	public static MigrationData getMigrationData(String neginAppPAN) {
		if(!Util.hasText(neginAppPAN))
			return null;
		String query = "from " + MigrationData.class.getName() + " md " +
			" where md.neginAppPan = :neginapp ";
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("neginapp", neginAppPAN);
		
		return (MigrationData) GeneralDao.Instance.findObject(query, params);
	}
	
	public static boolean isNeededReplaceWithFanapAppPan(MigrationData migData, TerminalType terminalType, IfxType ifxType, boolean checkValidationData) {
		boolean result = false;
		
		Boolean neededUpdatePin1Data = isNeededUpdatePin1Data(terminalType);
		if (terminalType == null || neededUpdatePin1Data == null)
			result = true;
//		if (!checkValidationData)
//			result = true;
		else {
			neededUpdatePin1Data = isNeededUpdatePin1Data(terminalType);
			if (neededUpdatePin1Data) {
				result = migData.getIsValidPin1();
				
			} else { 
				result = migData.getIsValidPin2();
			}
		}
		
		if (!checkValidationData)
			result = true;
		

		return !migData.getSendToNegin() && result && migData.getMigrationStatus() != null && CardMigrationTransferStatusType.TRANSFER.equals(migData.getMigrationStatus());
	}
	
	public static boolean isNeedCheckValidationData(TerminalType terminalType, IfxType ifxType) {
		Boolean neededUpdatePin1Data = isNeededUpdatePin1Data(terminalType);
		if (terminalType == null || neededUpdatePin1Data == null)
			return false;
			
		if (ISOFinalMessageType.isTransferToMessage(ifxType) ||
				ISOFinalMessageType.isTransferCheckAccountMessage(ifxType))
			return false;
		
		return true;
		}


}
