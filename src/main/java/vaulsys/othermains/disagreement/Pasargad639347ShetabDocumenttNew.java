package vaulsys.othermains.disagreement;

import com.ghasemkiani.util.icu.PersianDateFormat;
import vaulsys.authorization.policy.Bank;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.report.IssShetabReportRecord;
import vaulsys.clearing.report.ShetabDisagreementService;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.clearing.report.ShetabReportRecord;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.ConfigUtil;
import vaulsys.util.StringFormat;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Pasargad639347ShetabDocumenttNew {
//	public static void main(String[] args) {
	
	private static final Logger logger = Logger.getLogger(Pasargad639347ShetabDocumenttNew.class);
	public static String getShetabFile(String path_fromShetab){
//		String path;
		
		boolean checkRecord = false;
		boolean justMigratedRecords = false;
		final String bankName = ConfigUtil.getProperty(ConfigUtil.BANK_NAME); //for tavon tav
		String fileExt = "-iss-";

//		if(args.length < 1) {
//			System.out.println("Enter report files path as input paramater...");
//			path = "D:/disagreement/form8/900220";
//		}else{
//			path = args[0];
//		}

		File folder = new File(path_fromShetab);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name){
				return name.toLowerCase().startsWith(bankName.toLowerCase()) && name.endsWith(".zip") && !name.contains("SANAD");
			}
		});
		
		List<File> fileName = new ArrayList<File>();
		for(int i=0 ; i< folder.list().length; i++){
			fileName.add(folder.listFiles()[i]) ;
		}
		
		if(files == null){
//			System.exit(0);
			return null;
		}

		ZipFile zipFile;
		
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		GeneralDao.Instance.endTransaction();
		
		
		String[] shetabReconcilationFiles = ShetabReconciliationService.getShetabReconcilationFiles(GlobalContext.getInstance().getMyInstitution().getBin());
		for(File file:files){
			logger.debug("Processing file:"+file.getName());

			try {
				String pathRes = path_fromShetab+"/"+file.getName().substring(0,file.getName().length()-4)+fileExt+"report8.txt";
				File shetabReportRes = new File(pathRes);
				if(!shetabReportRes.exists()){
					shetabReportRes.createNewFile();
				}

				BufferedWriter errors = new BufferedWriter(new FileWriter(shetabReportRes));
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();

					for(int i=0; i<shetabReconcilationFiles.length; i++) {
						if (entry.getName().endsWith(shetabReconcilationFiles[i])){
							try {
								logger.debug("Entry:"+entry.getName());
								if(!(
										entry.getName().endsWith(".Iss") 
										|| 
										entry.getName().endsWith(".Acq")
										)){
									continue;
								}
								InputStream inputStream = zipFile.getInputStream(entry);
								boolean isIssuer = ShetabReconciliationService.IS_ISSUER[i];
								int trxType = ShetabReconciliationService.TRX_TYPES[i];
								List<IssShetabReportRecord> desiredRecords = new ArrayList<IssShetabReportRecord>();
								String err = Pasargad639347ShetabDocumenttNew.extractRecordsFromShetabReport(desiredRecords, new BufferedReader(new InputStreamReader(inputStream)), isIssuer, trxType, ProcessContext.get().getMyInstitution().getBin(), checkRecord, justMigratedRecords);				
								if(err != null && !err.equals("")){
									errors.append(err);
									errors.flush();
								}else{
									err = Pasargad639347ShetabDocumenttNew.generateReport8File(desiredRecords);
									errors.append(err);
									errors.flush();									
								}
							} catch (Exception e) {
								e.printStackTrace();
								logger.error(e);
							}
							break;
						}
					}//for
				}//while
                zipFile.close();
				logger.debug("Processing file:" + file.getName());
				errors.flush();
				errors.close();
				
				return pathRes;
				
			} catch (Exception e1) {
				logger.error(e1);
			}
		}
		return null;
	}
	
	public static String generateReport8File(List<IssShetabReportRecord> records){
		int count=0;
		StringBuilder report8th = new StringBuilder();
		Institution myInstitution = ProcessContext.get().getMyInstitution();
		Long myBin = myInstitution.getBin();
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMddHHmmss");
		Long fee = new Long(0);
		boolean reversalTrx = false;
		
		for(IssShetabReportRecord record:records) {
//			System.out.println(record);
			String debitCredit = "D";
			String statementCode = ShetabReconciliationService.getStatementCode(record);
	
			if (statementCode != null) {
				//C1, C2, C3, C4
				report8th.append(dateFormatPers.format(record.origDt.toDate()));
				//Mirkamali(Task148)
				if(TerminalType.KIOSK_CARD_PRESENT.equals(record.terminalType))
					report8th.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "43" + "", '0'));
				else
					report8th.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, record.terminalType.getCode() + "", '0')); 
				report8th.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0') + "/");
				report8th.append("0000" + "/");
				report8th.append("8888" + "/");
				report8th.append("0" + "/");
				//C5
				report8th.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, "0", '0') + "/");
	
				//C6
				fee = 0L;
				if (TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.destBankId)) {
						fee = (record.feeAmount != null) ? record.feeAmount : 0L;
				} else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
					fee = (record.feeAmount != null) ? record.feeAmount : 0L;
				} else if (TrnType.BALANCEINQUIRY.equals(record.trnType)) {
					fee = (record.feeAmount != null) ? record.feeAmount : 0L;
				} else if (TrnType.WITHDRAWAL.equals(record.trnType)) {
					fee = (record.feeAmount != null) ? record.feeAmount : 0L;
				}
				report8th.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.amount + fee, '0') + "/");
				
				// C7
				// We are issuer
				if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.destBankId))
						debitCredit = "C";
				// We are acquire
				if (TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId))
					debitCredit = "C";
				if (ISOFinalMessageType.isReversalOrRepeatMessage(record.type))
					debitCredit = ("D".equals(debitCredit) ? "C" : "D");
				report8th.append(debitCredit + "/");
	
				// C8
				if (!reversalTrx) {
					if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
						report8th.append(252 + "/");
					} else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.bankId)) {
						report8th.append(253 + "/");
					} else if (TrnType.DECREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.destBankId)) {
							report8th.append(252 + "/");
					} else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType) && myBin.equals(record.destBankId)) {
							report8th.append(253 + "/");
					} else {
						report8th.append(statementCode + "/");
					}
				} else {
					report8th.append("090" + "/");
				}
				
				//C9, C10
				report8th.append("0/000/");
				//C11
				//Mirkamali(Task148)
				if(TerminalType.KIOSK_CARD_PRESENT.equals(record.terminalType))
					report8th.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "43" + "", '0') + "/");
				else
					report8th.append(StringFormat.formatNew(2, StringFormat.JUST_RIGHT, record.terminalType.getCode() + "", '0') + "/");
				// C12
				report8th.append("0000/");
				//C13
				report8th.append((ISOFinalMessageType.isReversalOrRepeatMessage(record.type) ? "R" : "N") + "/");
				// C14
				report8th.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0') + "/");
				//C15
				report8th.append("0000/");
				//C16
				Bank bank = ProcessContext.get().getBank(record.bankId.intValue());//GlobalContext.getInstance().getBank(record.bankId.intValue());
				report8th.append(((bank != null && bank.getTwoDigitCode() != null) ? bank.getTwoDigitCode() : "00") + "/");
				if(record.bankId.equals(502806L)){
					logger.debug(count + "   " + ((bank != null && bank.getTwoDigitCode() != null) ? bank.getTwoDigitCode() : "00") + "/");
				}
				//C17
				report8th.append("0000/");
				
				//C18, C19, C20
				if(TrnType.INCREMENTALTRANSFER.equals(record.trnType) || TrnType.DECREMENTALTRANSFER.equals(record.trnType)){
					if(TrnType.INCREMENTALTRANSFER.equals(record.trnType)){
						if(myBin.equals(record.bankId)){
							//C18
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
							//C19
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
							//C20
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
						}else if(myBin.equals(record.destBankId)){
							//C18
							report8th.append("0000000000000000000/");
							//C19
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
							//C20
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
						}
					}else{ //TrnType.DECREMENTALTRANSFER.equals(record.trnType)
						if( myBin.equals(record.bankId)){
							//C18
							report8th.append("0000000000000000000/");
							//C19
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
							//C20
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
						}else if(myBin.equals(record.destBankId)){
							//C18
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
							//C19
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
							//C20
							report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.destAppPan, ' ') + "/");
						}
					}
				} else{
					//C18
					report8th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ') + "/");
					//C19, C20
					report8th.append("0000000000000000000/0000000000000000000/");
				}
				//C21, C22
				report8th.append("0000000000000/0000000000000/");
	
				// C23
				if(TrnType.INCREMENTALTRANSFER.equals(record.trnType)){
					if(myBin.equals(record.bankId)){
						report8th.append("46/");
					}else if(myBin.equals(record.destBankId)){
						report8th.append("47/");
					}
				}else if(TrnType.DECREMENTALTRANSFER.equals(record.trnType)){
					if(myBin.equals(record.bankId))
						report8th.append("47/");
					else if(myBin.equals(record.destBankId))
						report8th.append("46/");
				} else {
					report8th.append("00/");
				}
	
				//C24, C25
				if (TrnType.DECREMENTALTRANSFER.equals(record.trnType)) {
					report8th.append(record.bankId + "/");
					report8th.append(myBin + "/");
				} else if (TrnType.INCREMENTALTRANSFER.equals(record.trnType)) {
					report8th.append(record.bankId + "/");
					report8th.append(myBin + "/");
				} else {
					report8th.append("000000/000000/");
				}
	
				report8th.append("0/0/\r\n");
			}
		}
		return report8th.toString();
	}
	
	public static String extractRecordsFromShetabReport(List<IssShetabReportRecord> desiredRecords, BufferedReader brShetabReport, boolean isIssuingMode, int reportType, Long myBIN, boolean checkRecord, boolean justMigratedRecords) throws Exception {
		String errors = "";
		int numCleared = 0;
		
		DateTime maxDateTime = DateTime.MIN_DATE_TIME;

		String reportRecord;
//		ShetabReportRecord record;

		String PAN_CRITERIA_APPPAN;
		String PAN_CRITERIA_SECONDAPPPAN;
//		PAN_CRITERIA_SECONDAPPPAN = " and m.eMVRqData.secondAppPan = :appPan ";
		PAN_CRITERIA_SECONDAPPPAN = " and m.eMVRqData.actualSecondAppPan = :appPan ";

//		PAN_CRITERIA_APPPAN = " and m.eMVRqData.CardAcctId.AppPAN = :appPan ";
		PAN_CRITERIA_APPPAN = " and m.eMVRqData.CardAcctId.actualAppPAN = :appPan ";
		List<Long> numTrxs = null;
		
		GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
		List<ShetabReportRecord> records;
		records = ShetabDisagreementService.parseRecords(brShetabReport, isIssuingMode, myBIN, reportType, false);
		try {
			for(ShetabReportRecord record: records){
				if(checkRecord){
					String bankCriteria = "";
					if (isIssuingMode) {
						if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
							bankCriteria = " and m.networkTrnInfo.DestBankId = :destBankId ";
						} else {
							bankCriteria = " and m.networkTrnInfo.BankId = :destBankId ";
						}
					} else {
						bankCriteria = " and m.networkTrnInfo.DestBankId = :destBankId ";
					}
					String panCriteria = "";
					if (isIssuingMode) {
						if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
							panCriteria = PAN_CRITERIA_SECONDAPPPAN;
						} else {
							panCriteria = PAN_CRITERIA_APPPAN;
						}
					} else {
						if (!record.appPan.startsWith(myBIN.toString()) && record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) {
							panCriteria = PAN_CRITERIA_SECONDAPPPAN;
						}else{
							panCriteria = PAN_CRITERIA_APPPAN;
						}
					}
					String queryString = (justMigratedRecords? "select m.networkTrnInfo.DestBankId ": "select m.id ")
							+ " from Ifx m inner join m.transaction trx "
							+ " where "
							+ " m.ifxType = :type "
							+ " and m.ifxDirection = :direction "
							+ " and m.networkTrnInfo.OrigDt = :origDt "
							+ " and m.networkTrnInfo.Src_TrnSeqCntr = :trnSeqCntr "
							+ panCriteria
							+ bankCriteria
							+ " and m.networkTrnInfo.TerminalId like '"+record.terminalId+"%' "
							+ " and m.eMVRqData.Auth_Amt= :amount "
							+ " and m.networkTrnInfo.TerminalType = :termType) "
							;
		
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("type", record.type);
					params.put("origDt", record.origDt);
					params.put("trnSeqCntr", record.trnSeqCntr);
					params.put("appPan", record.appPan);
					params.put("destBankId", record.destBankId);
					params.put("amount", record.amount);
					params.put("termType", record.terminalType);
					params.put("direction", IfxDirection.OUTGOING);
			
					if(!(
//							record.trnSeqCntr.equals("236100") ||
//							record.trnSeqCntr.equals("226011") ||
//							record.trnSeqCntr.equals("906096") ||
//							record.trnSeqCntr.equals("259397") ||
//							record.trnSeqCntr.equals("235426") ||
							record.trnSeqCntr.equals("580043")
							))
						continue;
					numTrxs = GeneralDao.Instance.find(queryString, params);
				}
				if (checkRecord && (numTrxs == null || numTrxs.size() == 0)) {
					logger.error("No transaction found for record: " + record.row + "---" + record.toString()/*reportRecord*/);
					errors += "NF:" + record.toString()/*reportRecord*/ + "\r\n";
				} else if ((!checkRecord || numTrxs.size() == 1) && (isIssuingMode || (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) ) {
					numCleared++;
//					if(record.row % 500 == 0){
//						logger.debug("row:\t"+record.row);
//					}
							
					if(justMigratedRecords && !numTrxs.get(0).equals(502229L))
						continue;
							
					IssShetabReportRecord issRecord = new IssShetabReportRecord();
							
					issRecord.amount = record.amount;
					issRecord.appPan = record.appPan;
					issRecord.bankId = record.destBankId;
//					if((record.secondAppPan != null && record.secondAppPan.equals("5022291003051503")) || (record.appPan != null && record.appPan.equals("5022291003051503"))){
//						System.out.println("************" + record.appPan);
//					}
					if (isIssuingMode) {
						if (!record.appPan.startsWith(myBIN.toString()) && (record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS))) {
						} else if(record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) 
								|| record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)
								){
							issRecord.destAppPan = record.secondAppPan;
							issRecord.destBankId = new Long(record.appPan.substring(0,6));
						}
					} else {
						if(record.type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS)){
							issRecord.appPan = record.secondAppPan;
							issRecord.destAppPan = record.appPan;
							issRecord.bankId = new Long(record.secondAppPan.substring(0,6));
						}else if(record.type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)){
							issRecord.destAppPan = record.secondAppPan;
							issRecord.appPan = record.appPan;
							issRecord.bankId = new Long(record.secondAppPan.substring(0,6));								
						}
					}

					issRecord.origDt = record.origDt;
					issRecord.terminalId = record.terminalId;
					issRecord.terminalType = record.terminalType;
					issRecord.trnSeqCntr = record.trnSeqCntr;
					issRecord.trnType = IfxType.getTrnType(record.type);
					issRecord.type = record.type;
						
					desiredRecords.add(issRecord);
							
							
				} else if (checkRecord && numTrxs.size() > 1) {
					logger.error("More than 1 row found for record: "+record.row + "---" + record.toString()/*reportRecord*/);
				}
				if (maxDateTime.before(record.origDt))
					maxDateTime = record.origDt;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
//			errors += "BD:" + reportRecord + "\r\n";
		}

		GeneralDao.Instance.endTransaction();

		if (errors == null || errors.length() == 0) {
			logger.error("No ERRORS in reconciliation...");
		} else {
			logger.error("ERRORS: " + errors);
		}

		return errors;
	}
}
