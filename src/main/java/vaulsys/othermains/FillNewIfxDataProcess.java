package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ui.MessageObject;
import vaulsys.util.Util;
import vaulsys.wfe.process.SwitchThreadPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.hibernate.transform.AliasToBeanResultTransformer;

//1393/06/16 15:40 Stable
//Task110 - (2797) Main list for new ifx Data
public class FillNewIfxDataProcess {
	private static int THREAD_COUNT = 16;
    private static final String lineSeperator = System.getProperty("line.separator").toString();
	

	private static final class RunningMode {
		public static final Integer TEST = 1;
		public static final Integer REAL = 2;
	}
	private static final Logger logger = Logger.getLogger(FillNewIfxDataProcess.class);
	private static final int GLOBAL_REPORT_RECORDSIZE = 1000;
	private static Integer seqmentSize = 500;
	private static Integer RUNNING_MODE = RunningMode.REAL;


	public static void main(final String[] args) {
		String versionMsg = "Fill new ifx data ver 1.1";
		String hintMsg = "Parameters is P1 : fromDay ; P2 : toDay ; P3[test or real]; P4[Thread Count]";
		System.out.println(versionMsg);
		System.out.println(hintMsg);
		logger.info(String.format(versionMsg));
		logger.info(String.format(hintMsg));
		Long fromDayLong = 20111010000000L;
		Long toDayLong = 20111210000000L;
		try {
			if (args.length == 2) {
				fromDayLong = Long.parseLong(args[0]);
				toDayLong = Long.parseLong(args[1]);
			} else if (args.length == 3) {
				fromDayLong = Long.parseLong(args[0]);
				toDayLong = Long.parseLong(args[1]);
				if ((!args[2].trim().toLowerCase().equals("test") && !args[2].trim().toLowerCase().equals("real"))) {
						System.out.println("Wrong parameter!!!");
						logger.error("Wrong parameter!!!");
						return;
				}
				if (args[2].toLowerCase().equals("test")) {
					RUNNING_MODE = RunningMode.TEST;
				} else {
					RUNNING_MODE = RunningMode.REAL;
				}
			} else if (args.length == 4) {
				fromDayLong = Long.parseLong(args[0]);
				toDayLong = Long.parseLong(args[1]);
				if ((!args[2].trim().toLowerCase().equals("test") && !args[2].trim().toLowerCase().equals("real"))) {
						System.out.println("Wrong parameter!!!");
						logger.error("Wrong parameter!!!");
						return;
				}
				if (args[2].toLowerCase().equals("test")) {
					RUNNING_MODE = RunningMode.TEST;
				} else {
					RUNNING_MODE = RunningMode.REAL;
				}
				try {
					THREAD_COUNT = Integer.valueOf(args[3]);
				} catch(Exception e) {
					System.out.println("Wrong parameter[4] !!!");
					logger.error("Wrong parameter[4] !!!");
					return;
				}
				
			} else {
				System.out.println("Wrong parameters number !!!");
				logger.error("Wrong parameters number !!!");
				return;
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			logger.error(e.getMessage());
			return;
		}
		DateTime fromDay = new DateTime(fromDayLong);
		DateTime toDay = new DateTime(toDayLong);
		new FillNewIfxDataProcess().fillNewIfxData(fromDay, toDay);
		logger.info(String.format("Completed."));
	}

	private void fillNewIfxData(final DateTime fromDay, final DateTime toDay) {
        int maxReportRecordSize = GLOBAL_REPORT_RECORDSIZE;
        DateTime dateFrom = new DateTime(new DayDate(fromDay.getDayDate().getYear(), fromDay.getDayDate().getMonth(), fromDay.getDayDate().getDay()),
                new DayTime(fromDay.getDayTime().getHour(), fromDay.getDayTime().getMinute(), fromDay.getDayTime().getSecond() + 1));

        DateTime dateTo = new DateTime(new DayDate(fromDay.getDayDate().getYear(), fromDay.getDayDate().getMonth(), fromDay.getDayDate().getDay()),
                new DayTime(fromDay.getDayTime().getHour(), fromDay.getDayTime().getMinute(), fromDay.getDayTime().getSecond()));

        boolean generateTotal = false;
        String destinationPath = "/home/reports/fillNewIfxData/" + (generateTotal ? "T" : "F") + fromDay.getDateTimeLong() + "-" + toDay.getDateTimeLong();
        
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);//AldTODO Task074 
        

        while (dateTo.before(toDay)) {
            if (generateTotal) {
                dateFrom = fromDay.clone();
                dateTo = toDay.clone();
            } else {
                dateTo.increase(60);
            }

            executor.execute(new WorkerThread(this, maxReportRecordSize, dateFrom, dateTo, destinationPath));
            dateFrom.increase(60);
        }
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		logger.debug("Finished all FillNewIfxData threads");        
	}

	private void processFunc(int maxReportRecordSize, DateTime dateFrom,
			DateTime dateTo, String destinationPath) {
        logger.debug("from: " + dateFrom);
        logger.debug("to: " + dateTo);
		
		GeneralDao.Instance.beginTransaction();
		List<FillNewIfxDataQueryResultRecord> list = getIfxs(dateFrom, dateTo, maxReportRecordSize); //Method2
		GeneralDao.Instance.endTransaction();
		logger.info("Count of List: " + list.size());

		boolean commited = true;
		int updatedCount = 0;
		ArrayList<String> res = new ArrayList<String>();
		DateTime tempDate = null;
		Boolean append = false;
		for (FillNewIfxDataQueryResultRecord record  : list) {
			try {
				logger.info(String.format("Process ifx[%s]", record.ifxId.toString()));
				res.add(record.toString());
				if (commited) {
					GeneralDao.Instance.beginTransaction();
					commited = false;
					tempDate = dateFrom.clone();
				}
				try {
					updatedCount++;
					Ifx ifx = GeneralDao.Instance.load(Ifx.class, record.ifxId);
					ifx.setIfxSrcTrnSeqCntr(record.trnSeqCntr);
					ifx.setIfxBankId(record.bankId);
					ifx.setIfxEncAppPAN(record.appPan);
					ifx.setIfxOrigDt(record.origDt);
					ifx.setIfxRsCode(record.rsCode);
					GeneralDao.Instance.saveOrUpdate(ifx);
					//logger.info(String.format("update ifx : %s", record.ifxId));
				} catch (org.hibernate.ObjectNotFoundException e) {
					logger.info(String.format("ifx[%s] not found", record.ifxId));
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				if (updatedCount % seqmentSize == (seqmentSize - 1)) {
					if (RunningMode.REAL.equals(RUNNING_MODE)) {
						GeneralDao.Instance.endTransaction();
					} else {
						GeneralDao.Instance.rollback();
					}
					commited = true;
					try {
						String destinationFileName = "result-" + tempDate.getDateTimeLong() + "-" + dateTo.getDateTimeLong() + ".txt";
						logger.info(String.format("Save result " + destinationPath + "/" + destinationFileName));
						saveReport(res, destinationPath, destinationFileName, append);
						append = true;
					} catch (IOException e) {
						logger.error(e);
						e.printStackTrace();
					}
				}
				
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		try {
			if (!commited) {
				if (RunningMode.REAL.equals(RUNNING_MODE)) {
					GeneralDao.Instance.endTransaction();
				} else {
					GeneralDao.Instance.rollback();
				}
				commited = true;
			    try {
			        String destinationFileName = "result-" + tempDate.getDateTimeLong() + "-" + dateTo.getDateTimeLong() + ".txt";
			        logger.info(String.format("Save result " + destinationPath + "/" + destinationFileName));
					saveReport(res, destinationPath, destinationFileName, append);
					append = true;
				} catch (IOException e) {
					logger.error(e);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

    private List getIfxs(final DateTime fromDate, final DateTime toDate,  final int maxReportRecordSize) {
        List<FillNewIfxDataQueryResultRecord> result = new ArrayList<FillNewIfxDataQueryResultRecord>();
        for (int key : TrnType.valueToNameMap.keySet()) {
        	TrnType trnType = new TrnType(key);
        	List<IfxType> ifxTypes = trnType.getIfxType(trnType);
            result.addAll(getIfxs(fromDate, toDate,IfxDirection.INCOMING, true, trnType, ifxTypes, maxReportRecordSize));
            result.addAll(getIfxs(fromDate, toDate,IfxDirection.INCOMING, false, trnType, ifxTypes, maxReportRecordSize));
            result.addAll(getIfxs(fromDate, toDate,IfxDirection.OUTGOING, true, trnType, ifxTypes, maxReportRecordSize));
            result.addAll(getIfxs(fromDate, toDate,IfxDirection.OUTGOING, false, trnType, ifxTypes, maxReportRecordSize));
            result.addAll(getIfxs(fromDate, toDate,IfxDirection.SELF_GENERATED, true, trnType, ifxTypes, maxReportRecordSize));
            result.addAll(getIfxs(fromDate, toDate,IfxDirection.SELF_GENERATED, false, trnType, ifxTypes, maxReportRecordSize));
        }
        
        
        //for other ifx
        List<IfxType> ifxTypes =  new ArrayList<IfxType>();
        ifxTypes.add(IfxType.ATM_ACKNOWLEDGE);
        ifxTypes.add(IfxType.ATM_CONFIG_ID_LOAD);
        ifxTypes.add(IfxType.ATM_DATE_TIME_LOAD);
        ifxTypes.add(IfxType.ATM_ENHANCED_PARAMETER_TABLE_LOAD);
        ifxTypes.add(IfxType.ATM_FIT_TABLE_LOAD);
        ifxTypes.add(IfxType.ATM_GO_IN_SERVICE);
        ifxTypes.add(IfxType.ATM_GO_OUT_OF_SERVICE);
        ifxTypes.add(IfxType.ATM_SCREEN_TABLE_LOAD);
        ifxTypes.add(IfxType.ATM_SEND_CONFIG_ID);
        ifxTypes.add(IfxType.ATM_STATE_TABLE_LOAD);
        ifxTypes.add(IfxType.ATM_SUPPLY_COUNTER_REQUEST);
        ifxTypes.add(IfxType.CARD_READER_WRITER);
        ifxTypes.add(IfxType.CASH_HANDLER_RESPONSE);
        ifxTypes.add(IfxType.CASH_HANDLER);
        ifxTypes.add(IfxType.CONFIG_ID_RESPONSE);
        ifxTypes.add(IfxType.CONFIG_INFO_REQUEST);
        ifxTypes.add(IfxType.CONFIG_INFO_RESPONSE);
        ifxTypes.add(IfxType.DEVICE_LOCATION);
        ifxTypes.add(IfxType.JOURNAL_PRINTER_STATE);
        ifxTypes.add(IfxType.MERCHANT_BALANCE_RQ);
        ifxTypes.add(IfxType.MERCHANT_BALANCE_RS);
        ifxTypes.add(IfxType.POWER_FAILURE);
        ifxTypes.add(IfxType.RECEIPT_PRINTER_STATE);
        ifxTypes.add(IfxType.SUPERVISOR_ENTRY);
        ifxTypes.add(IfxType.SUPERVISOR_EXIT);
        ifxTypes.add(IfxType.SUPPLY_COUNTER_RESPONSE);
        ifxTypes.add(IfxType.ACQUIRER_REC_RQ);
        ifxTypes.add(IfxType.ACQUIRER_REC_RS);
        ifxTypes.add(IfxType.ACQUIRER_REC_REPEAT_RQ);
        ifxTypes.add(IfxType.ACQUIRER_REC_REPEAT_RS);
        ifxTypes.add(IfxType.CARD_ISSUER_REC_RQ);
        ifxTypes.add(IfxType.CARD_ISSUER_REC_RS);
        ifxTypes.add(IfxType.CARD_ISSUER_REC_REPEAT_RQ);
        ifxTypes.add(IfxType.CARD_ISSUER_REC_REPEAT_RS);
        ifxTypes.add(IfxType.ENCRYPTOR_STATE);
        ifxTypes.add(IfxType.MAC_KEY_CHANGE_RQ);
        ifxTypes.add(IfxType.MAC_KEY_CHANGE_RS);
        ifxTypes.add(IfxType.MASTER_KEY_CHANGE_RQ);
        ifxTypes.add(IfxType.MASTER_KEY_CHANGE_RS);
        ifxTypes.add(IfxType.PIN_KEY_CHANGE_RQ);
        ifxTypes.add(IfxType.PIN_KEY_CHANGE_RS);
        ifxTypes.add(IfxType.MAC_REJECT);
        ifxTypes.add(IfxType.SENSOR);
        ifxTypes.add(IfxType.COMMAND_REJECT);
        ifxTypes.add(IfxType.BATCH_UPLOAD_RQ);
        ifxTypes.add(IfxType.BATCH_UPLOAD_RS);
        
        
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.INCOMING, true , TrnType.UNKNOWN , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.INCOMING, false , TrnType.UNKNOWN , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.OUTGOING, true , TrnType.UNKNOWN , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.OUTGOING, false , TrnType.UNKNOWN , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.SELF_GENERATED, true , TrnType.UNKNOWN , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.SELF_GENERATED, false , TrnType.UNKNOWN , ifxTypes, maxReportRecordSize));
        
        ifxTypes.clear();
        ifxTypes.add(IfxType.CREDIT_CARD_DATA_RQ);
        ifxTypes.add(IfxType.CREDIT_CARD_DATA_RS);
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.INCOMING, true , TrnType.CREDITCARDDATA , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.INCOMING, false , TrnType.CREDITCARDDATA , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.OUTGOING, true , TrnType.CREDITCARDDATA , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.OUTGOING, false , TrnType.CREDITCARDDATA , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.SELF_GENERATED, true , TrnType.CREDITCARDDATA , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.SELF_GENERATED, false , TrnType.CREDITCARDDATA , ifxTypes, maxReportRecordSize));
        
        ifxTypes.clear();
        ifxTypes.add(IfxType.SORUSH_REV_REPEAT_RQ);
        ifxTypes.add(IfxType.SORUSH_REV_REPEAT_RS);
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.INCOMING, true , TrnType.INCREMENTALTRANSFER , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.INCOMING, false , TrnType.INCREMENTALTRANSFER , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.OUTGOING, true , TrnType.INCREMENTALTRANSFER , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.OUTGOING, false , TrnType.INCREMENTALTRANSFER , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.SELF_GENERATED, true , TrnType.INCREMENTALTRANSFER , ifxTypes, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate, IfxDirection.SELF_GENERATED, false , TrnType.INCREMENTALTRANSFER , ifxTypes, maxReportRecordSize));
        
        //process for trnType = null ;
        result.addAll(getIfxs(fromDate, toDate,IfxDirection.INCOMING, true, null, null, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate,IfxDirection.INCOMING, false, null, null, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate,IfxDirection.OUTGOING, true, null, null, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate,IfxDirection.OUTGOING, false, null, null, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate,IfxDirection.SELF_GENERATED, true, null, null, maxReportRecordSize));
        result.addAll(getIfxs(fromDate, toDate,IfxDirection.SELF_GENERATED, false, null, null, maxReportRecordSize));
        
        return result;
    }

	private List<FillNewIfxDataQueryResultRecord> getIfxs(
			final DateTime fromDate, final DateTime toDate,final IfxDirection ifxDirection, final Boolean request, TrnType trnType , List<IfxType> ifxTypes,
			final int maxReportRecordSize) {
        List<FillNewIfxDataQueryResultRecord> result = new ArrayList<FillNewIfxDataQueryResultRecord>();
		long t1, t2;
        for (int i = 0; ; i++) {
//            logger.debug("Iteration: " + i);
            logger.debug(String.format("[TrnType : %s, Direction : %s, Request : %s ,Iteration :%s]", (trnType != null ? trnType.toString() : "null"), ifxDirection.toString(), request, i));
            StringBuilder query = new StringBuilder("");
            query.append(" select ifx.id as ifxId,net.Src_TrnSeqCntr as trnSeqCntr ,net.BankId as bankId ,rq.CardAcctId.AppPAN as appPan, net.OrigDt as origDt ,rs.RsCode as rsCode")
    		.append(" from Ifx as ifx ")
            .append(" inner join ifx.networkTrnInfo as net ")
            .append(" left join ifx.eMVRqData as rq ")
            .append(" left join ifx.eMVRsData as rs ")
            .append(" where")
            .append(" ifx.receivedDtLong between :from and :to ")
            .append(" and ifx.ifxEncAppPAN is null ")
//            .append(" and ( ")
//            .append(" 	ifx.ifxSrcTrnSeqCntr is null ")
//            .append(" or ifx.ifxBankId is null ")
//            .append(" or ifx.ifxOrigDt is null ")
//            .append(" or ifx.ifxEncAppPAN is null ")
//            .append(" or ifx.ifxRsCode is null ")
//            .append(" ) ")
            .append(" and ifx.dummycol in (0,1,2,3,4,5,6,7,8,9) ")
            .append(" and ifx.ifxDirection = :direction ")
            .append(request == true ? " and (ifx.request = :request or ifx.request is null)" : " and ifx.request = :request");
            if (trnType != null) {
            	query.append(" and ifx.trnType = :trnType ")
            	.append(" and ifx.ifxType in (:ifxTypes) ");
/*            	.append(!TrnType.UNKNOWN.equals(trnType) ? " and ifx.ifxType in (:ifxTypes) " : "");*/
            } else {
            	query.append(" and ifx.trnType is null ");
            }

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("from", fromDate.getDateTimeLong());
            params.put("to", toDate.getDateTimeLong());
            params.put("direction", ifxDirection);
            params.put("request", request);
            if (trnType != null) {
	            params.put("trnType", trnType);
            	params.put("ifxTypes", ifxTypes);
	            //old
//	            if (!TrnType.UNKNOWN.equals(trnType)) {
//	            	params.put("ifxTypes", ifxTypes);
//	            }
            }

            t1 = System.currentTimeMillis();
            List<FillNewIfxDataQueryResultRecord> list = GeneralDao.Instance.find(query.toString(), params, i * maxReportRecordSize, maxReportRecordSize, new AliasToBeanResultTransformer(FillNewIfxDataQueryResultRecord.class));
            t2 = System.currentTimeMillis();
            logger.debug("Query time: " + (t2 - t1));
            if (list == null || list.size() == 0) {
				break;
			}
            result.addAll(list);
        }
        return result;
	}

    private void saveReport(final ArrayList<String> lst, final String destinationPath, final String destinationFileName, final Boolean append) throws IOException {
        // create report date
        StringBuilder report = new StringBuilder("");
        for (String trxId : lst) {
            report.append(String.format(trxId + lineSeperator));
        }
        // --------- Save Report
        File reportPath = new File(destinationPath);
        reportPath.mkdirs();
        if (!reportPath.exists()) {
			reportPath.createNewFile();
		}

        File fReport = new File(String.format(reportPath + "/%s", destinationFileName));
//        if (fReport.exists()) {
//            logger.debug(String.format("File %s alreadey exist.replaced it !!!", fReport.getAbsoluteFile()));
//            if (fReport.delete()) {
//                logger.debug(String.format("file deleted succseefully !!!"));
//            } else {
//                logger.error(String.format("An error occure in deleting file %s", fReport));
//            }
//        }
        try {
            FileOutputStream fos = new FileOutputStream(fReport, append);
            fos.write(report.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            logger.error(e);
        } finally {
        	lst.clear();
        }
    }
    
    class WorkerThread implements Runnable {

    	protected FillNewIfxDataProcess owner;
    	private int maxReportRecordSize;
    	private DateTime dateFrom;
    	private DateTime dateTo;
    	private String destinationPath;

    	public WorkerThread(FillNewIfxDataProcess owner, int maxReportRecordSize, DateTime dateFrom, DateTime dateTo, String destinationPath) {
    		this.owner = owner;
    		this.maxReportRecordSize = maxReportRecordSize;
    		this.dateFrom = dateFrom.clone();
    		this.dateTo = dateTo.clone();
    		this.destinationPath = destinationPath;
    	}

		@Override
		public void run() {
			owner.processFunc(maxReportRecordSize, dateFrom, dateTo, destinationPath);
		}

    }    
}

