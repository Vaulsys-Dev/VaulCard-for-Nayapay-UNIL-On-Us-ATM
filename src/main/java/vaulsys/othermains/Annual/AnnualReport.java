package vaulsys.othermains.Annual;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.transform.AliasToBeanResultTransformer;

import com.ghasemkiani.util.icu.PersianDateFormat;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.PersianCalendar;
import vaulsys.clearing.AccountingService;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.transaction.ClearingState;

public class AnnualReport {
	private static Logger logger = Logger.getLogger(AnnualReport.class);
	
	final static String[] keyNames = new String[]{"BALANCE_NUM", "PURCHASE_NUM", "TRANSFER_NUM", "BILL_NUM", "PURCHASE_SUM", "TRANSFER_SUM", "BILL_SUM"};
	
	public static void main(String[] args) {
		
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");

		String mainPath = "home/report/AnnualReport";
		
		String begin = "13920101";
		String end = "13930101";
		
		begin = args[0];
		end = args[1];
		
		
		List<Long> termList = new ArrayList<Long>();
		if(args.length > 2){
			for(int i = 2; i < args.length; i++){
				termList.add(Long.valueOf(args[i]));
			}
		}
		
//		termList.add(384937L);
//		termList.add(223636L);
//		termList.add(385043L);
		
		Map<Long, Map<String, Double>> TERM_TO_TRX = new HashMap<Long, Map<String, Double>>();
		
		try{
			File folder = new File(mainPath);
			folder.mkdir();
			for(Long termId : termList){
				
				folder = new File(mainPath + "/" + termId);
				folder.mkdir();
				
				
				Map<String, Double> all = new HashMap<String, Double>();
				
				for(String key : keyNames)
					all.put(key, 0D);
				
				TERM_TO_TRX.put(termId, all);
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		try{
			
			DateTime perBeginDateTime = new DateTime(new DayDate(Integer.valueOf(begin.substring(0, 4)), Integer.valueOf(begin.substring(4, 6)), Integer.valueOf(begin.substring(6, 8))), new DayTime(0, 0, 0));
			
			DateTime perEndDateTime = new DateTime(new DayDate(Integer.valueOf(end.substring(0, 4)), Integer.valueOf(end.substring(4, 6)), Integer.valueOf(end.substring(6, 8))), new DayTime(0, 0, 0));
			
			DateTime beginDateTime = PersianCalendar.toGregorian(perBeginDateTime);
			
			DateTime endDateTime = PersianCalendar.toGregorian(perEndDateTime);
			
			logger.debug("Start create report for time from  "  + begin + "(" + beginDateTime + ")" + " to " + end + "(" + endDateTime + ")");
			
			String query = "select " 
					+ " i.trnType As trnType, "
					+ " i.eMVRqData.Auth_Amt As amt, "
					+ " i.endPointTerminalCode As terminalId"
					+ " from " 
	                + " Ifx as i "
	                + " where "
	                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9)"
	                + " and i.endPointTerminalCode in (:termList) "
	                + " and i.receivedDtLong between :from and :to "
	                + " and i.request = false "
	                + " and i.ifxDirection = :ifxDirection "
	                + " and i.ifxType in (:ifxList) "
	                + " and i.trnType in (:financialList) "
	                + " and i.transaction.sourceClearingInfo.clearingState in (:clearingStateList) ";
			
			Map<String, Object> params = new HashMap<String, Object>();
			
			params.put("termList", termList);
			
			params.put("ifxDirection", IfxDirection.OUTGOING);
			
			params.put("ifxList", new ArrayList<IfxType>(){{
				add(IfxType.BAL_INQ_RS);
	            add(IfxType.PURCHASE_RS);
	            add(IfxType.WITHDRAWAL_RS);
	            add(IfxType.BILL_PMT_RS);
	            add(IfxType.TRANSFER_RS);
	            add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
	            add(IfxType.TRANSFER_TO_ACCOUNT_RS);
	        }});
			
			
			params.put("financialList", new ArrayList<TrnType>(){{
				add(TrnType.BALANCEINQUIRY);
				add(TrnType.PURCHASE);
	            add(TrnType.WITHDRAWAL);
	            add(TrnType.BILLPAYMENT);
	            add(TrnType.TRANSFER);
	            add(TrnType.DECREMENTALTRANSFER);
	            add(TrnType.INCREMENTALTRANSFER);
	        }});
			
			
			params.put("clearingStateList", new ArrayList<ClearingState>(){{
	            add(ClearingState.NOT_CLEARED);
	            add(ClearingState.CLEARED);
	            add(ClearingState.PARTIALLY_CLEARED);
	            add(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
	        }});
			
			DayDate everyDay = beginDateTime.getDayDate();
			
			DateTime cutoffFrom = null;
			DateTime cutoffTo = null;
			
			List<AnnualReportRecord> result = null;
			
			GeneralDao.Instance.beginTransaction();
			
			while(!everyDay.equals(endDateTime.getDayDate())){
				result = new ArrayList<AnnualReportRecord>();
				logger.debug(dateFormatPers.format(everyDay.toDate()) + ": ");
				
				int hour = 0;
				while(hour < 24){
					hour = hour + 2;
					cutoffFrom = new DateTime(everyDay, new DayTime(hour - 2, 0, 0));
					cutoffTo = new DateTime(everyDay, new DayTime(hour - 1, 59, 59));
					params.put("from", cutoffFrom.getDateTimeLong());
			        params.put("to", cutoffTo.getDateTimeLong());
					
			        logger.debug("query to find all Trx " + dateFormatPers.format(cutoffFrom.toDate()) + " from " + cutoffFrom.getDayTime() + " to " + cutoffTo.getDayTime());
			        
					result.addAll(GeneralDao.Instance.find(query, params, new AliasToBeanResultTransformer(AnnualReportRecord.class)));
				}
				
				
				
				/******************* Use the result *******************/
				Map<String, Double> allTrx;
				logger.debug("Parsing result of query ...");
				for(AnnualReportRecord record : result){
					
					allTrx = TERM_TO_TRX.get(record.terminalId);
					
					if(TrnType.BALANCEINQUIRY.equals(record.trnType)){
						
						Double bal_num = allTrx.get(keyNames[0]);
						
						allTrx.put(keyNames[0], bal_num + 1);
						
					}else if(TrnType.PURCHASE.equals(record.trnType)){
						
						Double purchase_num = allTrx.get(keyNames[1]);
						
						allTrx.put(keyNames[1], purchase_num + 1);
						
						Double purchase_sum = allTrx.get(keyNames[4]);
						
						allTrx.put(keyNames[4], purchase_sum + record.amt);
						
						
					}else if(TrnType.BILLPAYMENT.equals(record.trnType)){
						
						Double bill_num = allTrx.get(keyNames[3]);
						
						allTrx.put(keyNames[3], bill_num + 1);
						
						Double bill_sum = allTrx.get(keyNames[6]);
						
						allTrx.put(keyNames[6], bill_sum + record.amt);
						
					}else /* Transfer */ {
						
						Double transfer_num = allTrx.get(keyNames[2]);
						
						allTrx.put(keyNames[2], transfer_num + 1);
						
						Double transfer_sum = allTrx.get(keyNames[5]);
						
						allTrx.put(keyNames[5], transfer_sum + record.amt);
					}
					
					TERM_TO_TRX.put(record.terminalId, allTrx);
				}
				/******************************************************/
				
				/********* check if a month is complete create the file *********/
				
			    String everyDayPerStr = dateFormatPers.format(everyDay.toDate());
			    String nextDayPerStr = dateFormatPers.format(everyDay.nextDay().toDate());
			    
				if(!everyDayPerStr.substring(4,6).equals(nextDayPerStr.substring(4, 6))) {
					
					logger.debug("Create File for month " + everyDayPerStr.substring(4,6));
					
					for(Long termId : termList){
						BufferedWriter bw = new BufferedWriter(new FileWriter(mainPath + "/" + termId + "/" + everyDayPerStr.substring(4,6) + ".txt"));
						bw.append("TransactionType | Number of transactions | Sum of transactions").append("\r\n");
						
						allTrx = TERM_TO_TRX.get(termId);
						
						bw.append("BALANCE        ").append("|").append(allTrx.get(keyNames[0]) + "|").append("0").append("\r\n");
						
						bw.append("PURCHASE       ").append("|").append(allTrx.get(keyNames[1]) + "|").append(allTrx.get(keyNames[4]) + "").append("\r\n");
						
						bw.append("TRANSFER       ").append("|").append(allTrx.get(keyNames[2]) + "|").append(allTrx.get(keyNames[5]) + "").append("\r\n");
						
						bw.append("BILLPAYMENT    ").append("|").append(allTrx.get(keyNames[3]) + "|").append(allTrx.get(keyNames[6]) + "").append("\r\n");
						
						bw.flush();
						bw.close();
					}
					
					for(Long termId : termList){
						Map<String, Double> all = new HashMap<String, Double>();
						
						for(String key : keyNames)
							all.put(key, 0D);
						
						TERM_TO_TRX.put(termId, all);
					}
					
					GeneralDao.Instance.endTransaction();
					GeneralDao.Instance.beginTransaction();
				}
				
				everyDay = everyDay.nextDay();
				
			}
			
			
			
		}catch (Exception e) {
			e.printStackTrace();
			
		}finally{
//			GeneralDao.Instance.endTransaction();
		}

	}

}
