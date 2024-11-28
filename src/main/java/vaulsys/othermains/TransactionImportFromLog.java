package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.SettlementState;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.cms.utils.CMSMapperUtil;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.transaction.TransactionType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.Util;
import java.util.HashMap;

public class TransactionImportFromLog {
    static final String SHETAB_IN  = "channelSHETABIn";
    static final String SHETAB_OUT = "channelSHETABOut";
    static final String EPAY_IN    = "channelFnpEpayInA";
    static final String EPAY_OUT   = "channelFnpEpayOutA";
    static final String CMS_IN     = "CMSIn";
    static final String CMS_OUT    = "CMSOut";
    static final String NDC        = "channelNDCProcachInA";
    static final String APACS_NCC  = "Apacs70NCC";
    static final String APACS_MG   = "Apacs70Meganac";
    static final String INFO_TEC   = "posInfotech";

    static Logger logger = Logger.getLogger(TransactionImportFromLog.class);

    //public static void main_Switch_LOG_PARSER(String[] args) {
    public static void main(String[] args) throws Throwable {
        String fileName = args[0]; //"C:/Users/Torki/Desktop/Switch/flow-data-loss.main";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName + "-out"));
        BufferedWriter badsWriter = new BufferedWriter(new FileWriter(fileName + "-bad"));
        BufferedWriter infoWriter = new BufferedWriter(new FileWriter(fileName + "-info"));
        BufferedWriter mapWriter = new BufferedWriter(new FileWriter(fileName + "-trx-map"));
    	
        try {
            File file = new File(fileName);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            String str;


            List<String> threadNames = new ArrayList<String>();
            List<List<String>>  threadLogs = new ArrayList<List<String>>();
            Map<String, List<String>> activeThreads = new Hashtable<String, List<String>>();
            List<String> threadLog = null;
            String threadName;
            int i;
            int j;

            final String nio = "NioProcessor";
            final String th = "[Thread";
            final String sh = "[obScheduler";
            final String pl = "[pool";
            final String ef  = "Ending MainProcess";
            boolean lastSkipped;


            lastSkipped = false;
            while (true) {
                str = randomAccessFile.readLine();
                if (str == null) {
                    break;
                }
                i = str.indexOf("[");
                j = str.indexOf("]");
                if (i >= 0 && j >= 0) {
                    threadName = str.substring(i, j+1);
                    if (threadName.indexOf(nio) >= 0) {
                        lastSkipped = true;
                        continue;
                    }
                    if (threadName.startsWith(sh)) {
                        lastSkipped = true;
                        continue;
                    }

                    if (threadName.startsWith(th)) {
                        lastSkipped = true;
                        continue;
                    }


                    i = str.indexOf(pl);
                    if (i < 0 && !lastSkipped) {
                        threadLog.add(str);
                        continue;
                    }

                    lastSkipped = false;

                    if (activeThreads.containsKey(threadName)) {
                        threadLog = activeThreads.get(threadName);
                    } else {
                        threadLog = new ArrayList<String>();

                        threadNames.add(threadName);
                        threadLogs.add(threadLog);
                        activeThreads.put(threadName, threadLog);
                    }

                    threadLog.add(str);

                    if (str.indexOf(ef) >= 0) {
                        activeThreads.remove(threadName);
                    }
                } else {
                    if (!lastSkipped)
                        threadLog.add(str);
                }
            }
            
            String line;
            String inputChannel;
            String outputChannel;
            final String rf         = "RECEIVED from ";
            final String st         = "SENT to ";
            final String mo         = "Message is going to sent to";
            final String refTrFnd   = "referenceTransaction found:";
            final String lifeCycleStr = "Try to get Lock of LifeCycle[";
            int rfIndex;
            int stIndex;
            int moIndex;

            String ifxType;
            String bankID;
            String destBankID;
            String recvBankID;
            MonthDayDate settleDt;
            MonthDayDate postedDt;
            TerminalType termType;
            String refTrx;
            String lifeCycleOfTrx;
            String trx;
            Long authAmt;
            Boolean request;
            Long receivedDtLong;
            String origDt_date;
            String origDt_time;
            DateTime origDt;
            String src_TrnSeqCntr;
            String appPAN;
            String cmsAppPAN;
            String terminalId;
            Long totalFeeAmt;
            String rsCode;
            String networkRefId;
            String docNumber;
            StringBuilder xml_in;
            StringBuilder xml_out;
            String secAppPAN;
            
            String temp;

            Map<String, TerminalType> trxNumsTermType = new Hashtable<String, TerminalType>();
            Map<String, Long> trxNumsMap = new Hashtable<String, Long>(); 
            Map<Long, String> lifeCyclesMap = new Hashtable<Long, String>();
            List<String> lifeCyclesList = new ArrayList<String>();

            DecimalFormat df = new DecimalFormat("#");
            DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
            DateFormat showDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            
            GeneralDao.Instance.beginTransaction();
            GlobalContext.getInstance().setAllChannels();
            GlobalContext.getInstance().setAllInstitutions();
            ProcessContext.get().init();
            GeneralDao.Instance.endTransaction();

	    GeneralDao.Instance.beginTransaction();
            List<String> log;
            for (int z = 0; z < threadLogs.size(); z++) {
                log =  threadLogs.get(z);
                threadName = threadNames.get(z);
                rfIndex = -1;
                stIndex = -1;
                moIndex = -1;

                refTrx = null;
                trx = null;
                lifeCycleOfTrx = null;

                for (int k = 0; k < log.size(); k++) {
                    line = log.get(k).trim();

                    bufferedWriter.write(line);
                    bufferedWriter.newLine();

                    if (line.indexOf(rf) >= 0) {
                        rfIndex = k;
                    }
                    if (line.indexOf(st) >= 0) {
                        stIndex = k;
                    }
                    if (line.indexOf(refTrFnd) >= 0) {
                        refTrx = line.substring(line.indexOf(refTrFnd) + 27);
                    }
                    if (line.indexOf(lifeCycleStr) >= 0) {
                        lifeCycleOfTrx = line.substring(line.indexOf(lifeCycleStr) + 29, line.length() - 1);
                    }
                }


                bufferedWriter.write("//==========================================================================");
                bufferedWriter.newLine();


                if (rfIndex >= 0 && stIndex < 0) {
                    line = log.get(rfIndex).trim();
                    inputChannel = line.substring(line.indexOf(rf) + 14, line.length() - 1);
                    if (inputChannel.equals(SHETAB_IN) || inputChannel.equals(CMS_IN)) {
                        for (int k = 0; k < log.size(); k++) {
                            if (log.get(k).indexOf(mo) >= 0) {
                                moIndex = k;
                            }
                        }
                    }
                }

                bankID = null;
                destBankID = null;
                recvBankID = null;
                postedDt = new MonthDayDate(2012, 10, 12);
                settleDt = new MonthDayDate(2012, 10, 12);
                termType = null;
                authAmt = null;
                request = null;
                receivedDtLong = null;
                origDt_date = null;
                origDt_time = null;
                origDt = null;
                src_TrnSeqCntr = null;
                appPAN = null;
                terminalId = null;
                totalFeeAmt = 0L;
                rsCode = null;
                networkRefId = null;
                docNumber = null;
                secAppPAN = null;
                cmsAppPAN = null;

                xml_in = new StringBuilder("");
                xml_out = new StringBuilder("");
                if (rfIndex >= 0 && (stIndex >= 0 || moIndex >= 0)) {
                    line = log.get(rfIndex).trim();
                    inputChannel = line.substring(line.indexOf(rf) + 14, line.length() - 1);

                    readXML(inputChannel, threadName, log, rfIndex, xml_in);

                    if (stIndex >= 0) {
                        line = log.get(stIndex).trim();
                        outputChannel = line.substring(line.indexOf(st) + 8, line.length() - 1);
                        readXML(outputChannel, threadName, log, stIndex, xml_out);
                    } else {
                        line = log.get(moIndex).trim();
                        outputChannel = line.substring(line.indexOf(mo) + 28, line.length() - 1);
                        readXML(outputChannel, threadName, log, moIndex, xml_out);
                    }

                    if (inputChannel.equals(SHETAB_IN)) {
                        for (int k = (rfIndex + 2); k < log.size(); k++) {
                            line = log.get(k).trim();
                            if (line.startsWith("32=")) {
                                bankID = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("33=")) {
                                destBankID = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("100=")) {
                                recvBankID = line.substring(5, line.length() - 1);
                            } else if (line.startsWith("15=")) {
                            	temp = line.substring(4, line.length() - 1);
                                settleDt = new MonthDayDate(2012, Integer.parseInt(temp.substring(0, 2)), Integer.parseInt(temp.substring(2, 4)));     
                            } else if (line.startsWith("17=")) {
                                temp = line.substring(4, line.length() - 1);
                                postedDt = new MonthDayDate(2012, Integer.parseInt(temp.substring(0, 2)), Integer.parseInt(temp.substring(2, 4)));
                            } else if (line.startsWith("25=")) {
                                termType = new TerminalType(Integer.parseInt(line.substring(4, line.length() - 1)));
                            } else if (line.startsWith("4=")) {
                                authAmt = Long.parseLong(line.substring(3, line.length() - 1));
                            } else if (line.startsWith("12=")) {
                                origDt_time = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("13=")) {
                                origDt_date = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("11=")) {
                                src_TrnSeqCntr = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("2=")) {
                                appPAN = line.substring(3, line.length() - 1);
                            } else if (line.startsWith("41=")) {
                                terminalId = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("39=")) {
                                rsCode = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("37=")) {
                                networkRefId = line.substring(4, line.length() - 1);
                            }
                        }

                        if (outputChannel.equals(CMS_OUT)) {
                            for (int k = (stIndex + 2); k < log.size(); k++) {
                                line = log.get(k).trim();
                                if (line.startsWith("feeAmt = ")) {
                                    totalFeeAmt = Long.parseLong(line.substring(9));
                                }
                                if (rsCode == null && line.startsWith("rsCode = ")) {
                                    rsCode = CMSMapperUtil.ToErrorCode.get(line.substring(9));    
                                }
                                if (line.startsWith("docNum = ")) {
                                    docNumber = line.substring(9);
                                }
                                if (line.startsWith("secPAN =")) {
                                	secAppPAN = line.substring(9);
                                }
                            }
                        }

                        origDt = new DateTime(MyDateFormatNew.parse("MMddHHmmss", origDt_date + origDt_time));

                        if (settleDt == null && postedDt == null)
                            throw new Exception("settleDt = " + settleDt + " & postedDt = " + postedDt);

                    } else if (outputChannel.equals(SHETAB_OUT)) {
                        for (int k = (stIndex + 2); k < log.size(); k++) {
                            line = log.get(k).trim();
                            if (line.startsWith("32=")) {
                                bankID = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("33=")) {
                                destBankID = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("100=")) {
                                recvBankID = line.substring(5, line.length() - 1);
                            } else if (line.startsWith("15=")) {
                                temp = line.substring(4, line.length() - 1);
                                settleDt = new MonthDayDate(2012, Integer.parseInt(temp.substring(0, 2)), Integer.parseInt(temp.substring(2, 4)));
                            } else if (line.startsWith("17=")) {
                                temp = line.substring(4, line.length() - 1);
                                postedDt = new MonthDayDate(2012, Integer.parseInt(temp.substring(0, 2)), Integer.parseInt(temp.substring(2, 4)));
                            } else if (line.startsWith("25=")) {
                                termType = new TerminalType(Integer.parseInt(line.substring(4, line.length() - 1)));
                            } else if (line.startsWith("4=")) {
                                authAmt = Long.parseLong(line.substring(3, line.length() - 1));
                            } else if (line.startsWith("12=")) {
                                origDt_time = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("13=")) {
                                origDt_date = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("11=")) {
                                src_TrnSeqCntr = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("2=")) {
                                appPAN = line.substring(3, line.length() - 1);
                            } else if (line.startsWith("41=")) {
                                terminalId = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("39=")) {
                                rsCode = line.substring(4, line.length() - 1);
                            } else if (line.startsWith("37=")) {
                                networkRefId = line.substring(4, line.length() - 1);
                            }
                        }

                        if (inputChannel.equals(CMS_IN)) {
                            for (int k = (rfIndex + 2); k < log.size(); k++) {
                                line = log.get(k).trim();
                                if (line.startsWith("feeAmt = ")) {
                                    totalFeeAmt = Long.parseLong(line.substring(9));
                                }
                                if (rsCode == null && line.startsWith("rsCode = ")) {
                                    rsCode = CMSMapperUtil.ToErrorCode.get(line.substring(9));
                                }
                                if (line.startsWith("docNum = ")) {
                                    docNumber = line.substring(9);
                                }
                                if (line.startsWith("secPAN =")) {
                                	secAppPAN = line.substring(9);
                                }
                            	if (line.startsWith("PAN = ")) {
                            		cmsAppPAN = line.substring(6);
                            	}
                            }
                        }

                        origDt = new DateTime(MyDateFormatNew.parse("MMddHHmmss", origDt_date + origDt_time));

                        if (settleDt == null && postedDt == null)
                            throw new Exception("settleDt = " + settleDt + " & postedDt = " + postedDt);
                        
                    } else if (inputChannel.equals(CMS_IN)) {
                        for (int k = (rfIndex + 2); k < log.size(); k++) {
                            line = log.get(k).trim();
                            if (line.startsWith("bnk = ")) {
                                bankID = line.substring(6);
                            } else if (line.startsWith("termType = ")) {
                                termType = CMSMapperUtil.ToTerminalType.get(Integer.parseInt(line.substring(11)));
                            } else if (line.startsWith("amt = ")) {
                                authAmt = Long.parseLong(line.substring(6));
                            } else if (line.startsWith("origDt = ")) {
                                origDt = new DateTime(dateFormat.parse(line.substring(9, line.indexOf("GMT-00:00")) + "2012"));
                            } else if (line.startsWith("seqCntr = ")) {
                                src_TrnSeqCntr = line.substring(10);
                            } else if (line.startsWith("PAN = ")) {
                                appPAN = line.substring(6);
                            } else if (line.startsWith("term = ")) {
                                terminalId = line.substring(7);
                            } else if (line.startsWith("feeAmt = ")) {
                                totalFeeAmt = Long.parseLong(line.substring(9));
                            } else if (line.startsWith("rsCode = ")) {
                                rsCode = CMSMapperUtil.ToErrorCode.get(line.substring(9));
                            } else if (line.startsWith("netRef = ")) {
                                networkRefId = line.substring(9);
                            } else if (line.startsWith("docNum = ")) {
                                docNumber = line.substring(9);
                            } else if (line.startsWith("rcvBnk = ")) {
                                recvBankID = line.substring(9);
                            } else if (line.startsWith("secPAN =")) {
                            	secAppPAN = line.substring(9);
                            }
                        }

                        destBankID = bankID;

                    } else if (outputChannel.equals(CMS_OUT)) {
                        for (int k = (rfIndex + 2); k < log.size(); k++) {
                            line = log.get(k).trim();
                            if (line.startsWith("bnk = ")) {
                                bankID = line.substring(6, line.length());
                            } else if (line.startsWith("termType = ")) {
                            	termType = CMSMapperUtil.ToTerminalType.get(Integer.parseInt(line.substring(11)));
                            } else if (line.startsWith("amt = ")) {
                                authAmt = Long.parseLong(line.substring(6));
                            } else if (line.startsWith("origDt = ")) {
                                origDt = new DateTime(dateFormat.parse(line.substring(9, line.indexOf("GMT-00:00")) + "2012"));
                            } else if (line.startsWith("seqCntr = ")) {
                                src_TrnSeqCntr = line.substring(10);
                            } else if (line.startsWith("PAN = ")) {
                                appPAN = line.substring(6);
                            } else if (line.startsWith("term = ")) {
                                terminalId = line.substring(7);
                            } else if (line.startsWith("feeAmt = ")) {
                                totalFeeAmt = Long.parseLong(line.substring(9));
                            } else if (line.startsWith("rsCode = ")) {
                                rsCode = CMSMapperUtil.ToErrorCode.get(line.substring(9));
                            } else if (line.startsWith("netRef = ")) {
                                networkRefId = line.substring(9);
                            } else if (line.startsWith("docNum = ")) {
                                docNumber = line.substring(9);
                            } else if (line.startsWith("rcvBnk = ")) {
                                recvBankID = line.substring(9);
                            } else if (line.startsWith("secPAN =")) {
                            	secAppPAN = line.substring(9);
                            }
                        }

                        destBankID = bankID;
                    }

                    line = log.get(log.size() - 1);
                    try {
                        line = line.substring(line.indexOf(ef));

                        ifxType = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                        trx = line.substring(line.indexOf("]:") + 2, line.indexOf(" in"));

                        if (ifxType.equals("ATM_JOURNALPRINTERSTATE") || ifxType.equals("ATM_CONFIGURATIONFITNESSSTATE") || ifxType.equals("POSReconcilement"))
                            continue;
                        
                        terminalId = terminalId.trim();

                        if (xml_in.toString().equals(""))
                            throw new Exception("Empty XML IN!");
                        if (xml_out.toString().equals(""))
                            throw new Exception("Empty XML OUT!");
                        
                        if (appPAN == null)
                        	throw new Exception("AppPAN is NULL!");
                        if (terminalId == null)
                        	throw new Exception("TerminalId is NULL!");
                        if (networkRefId == null)
                        	throw new Exception("NetworkRefId is NULL!");
                        
                        request = ifxType.endsWith("RQ");

                        if (!request && rsCode == null)
                            throw new Exception("rsCode is null for trx : " + trx);
                        if ((!request || (ifxType.equals("TRANSFER_TO_ACCOUNT_RQ") && inputChannel.equals(CMS_IN))) && refTrx == null)
                        	throw new Exception("refTrx is null for trx : " + trx);
                        
                        if (origDt == null)
                    		throw new Exception("origDt is null!");
                        
                        line = log.get(0).trim();
                        line = line.substring(line.indexOf("2012-10-12 ") + 11);

                        receivedDtLong = 20121012000000L + Long.parseLong(line.substring(0, 2))  * 10000 + Long.parseLong(line.substring(3, 5)) * 100 + Long.parseLong(line.substring(6, 8));
                        /**TODO: temp code                        
                        receivedDtLong = 20121023000000L + Long.parseLong(line.substring(0, 2))  * 10000 + Long.parseLong(line.substring(3, 5)) * 100 + Long.parseLong(line.substring(6, 8));**/

                        IfxType ifx_type = IfxType.valueOf(ifxType);
                        TrnType trn_type = IfxType.getTrnType(ifx_type);

                        Transaction refTransaction = null;
//                        GeneralDao.Instance.beginTransaction();
                      //================================================================================================================
//                      //TODO: temp code
//                        if (refTrx != null) {
//                        	if (refTrx.equals("2019544143")) {
//                        		refTrx = "51927569"; 
//                        	} if (refTrx.equals("2019544144")) {
//                        		refTrx = "51927555"; 
//                        	} if (refTrx.equals("2019544134")) {
//                        		refTrx = "51927558"; 
//                        	} if (refTrx.equals("2019544142")) {
//                        		refTrx = "51927559"; 
//                        	} if (refTrx.equals("2019544138")) {
//                        		refTrx = "51927582"; 
//                        	}
//                        }
//                        if (terminalId.trim().equals("226006") || terminalId.trim().equals("224806")) {
//                        	terminalId = "248800";
//                        }
//                        if (terminalId.trim().equals("299650") || terminalId.trim().equals("376147") || terminalId.trim().equals("360800") || terminalId.trim().equals("323905") || terminalId.trim().equals("106677")) {
//                        	terminalId = "200852";
//                        }
                        
                        
                        if (!request || (ifxType.equals("TRANSFER_TO_ACCOUNT_RQ") && inputChannel.equals(CMS_IN))) {
	                        if (trxNumsMap.containsKey(refTrx)) {
	                        	refTransaction = GeneralDao.Instance.getObject(Transaction.class, trxNumsMap.get(refTrx));
	                        } else {
	                        	refTransaction = GeneralDao.Instance.getObject(Transaction.class, Long.parseLong(refTrx));
	                        }
                        }
                        
                        if (termType == null) {
                            if (refTrx != null) {
                                termType = trxNumsTermType.get(refTrx);
                            }
                            if (termType == null) {
                            	termType = refTransaction.getIncomingIfx().getTerminalType();
                            }
                            if (termType == null) {
                            	throw new Exception("termType is Null!");
                            }
                        }
                        
                        if (termType != null) {
                            trxNumsTermType.put(trx, termType);
                        }
                        
                        if (authAmt == null)
                            authAmt = 0L;
                        
                        //============================
                        Transaction transaction = new Transaction();
                        LifeCycle lifeCycle;
                        
                        if (ifxType.equals("TRANSFER_FROM_ACCOUNT_RQ") || ifxType.equals("TRANSFER_TO_ACCOUNT_RQ") || ifxType.equals("TRANSFER_TO_ACCOUNT_RS") || ifxType.equals("TRANSFER_RS")) {
                        	//do nothing
                        } else {
                            if (request) {
                            	lifeCycle = new LifeCycle();
                            } else {
                            	lifeCycle = refTransaction.getLifeCycle();
                            }
                            
                    		lifeCycle.setIsComplete(true);
                    		GeneralDao.Instance.saveOrUpdate(lifeCycle);
                        	
	                        if (request) {
	                        	transaction.setFirstTransaction(transaction);
	                        } else {
	                        	transaction.setFirstTransaction(refTransaction);
	                        }
	                        
	                        transaction.setLifeCycle(lifeCycle);
	                        
	                        //==
	                        ClearingInfo src_ClearingInfo;
	                        if ((!request || ifx_type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || ifx_type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) && rsCode != null && Integer.parseInt(rsCode) > 0) {
	                        	if (refTransaction != null && refTransaction.getSourceClearingInfo() != null) {
	                        		refTransaction.setSourceClearingInfo(null);
	                        		GeneralDao.Instance.saveOrUpdate(refTransaction);
	                        	}
	                        } else {
		                        if (request) {
			                        src_ClearingInfo = new ClearingInfo();
		                        } else {
		                        	src_ClearingInfo = refTransaction.getSourceClearingInfo();
		                        	if (src_ClearingInfo == null) {
		                        		src_ClearingInfo = new ClearingInfo();
		                        		refTransaction.setSourceClearingInfo(src_ClearingInfo);
		                        	}
		                        }
		                        src_ClearingInfo.setClearingState(ClearingState.NOT_CLEARED);
		                        transaction.setSourceClearingInfo(src_ClearingInfo);
		                        GeneralDao.Instance.saveOrUpdate(src_ClearingInfo);
	                        }
	                      //==
	                        if ((!request || ifx_type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || ifx_type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) && rsCode != null && Integer.parseInt(rsCode) > 0) {
	                        	if (refTransaction != null && refTransaction.getDestinationClearingInfo() != null) {
	                        		refTransaction.setDestinationClearingInfo(null);
	                        		GeneralDao.Instance.saveOrUpdate(refTransaction);
	                        	}
	                        } else {
		                        ClearingInfo dest_ClearingInfo;
		                        if (request) {
		                        	dest_ClearingInfo = new ClearingInfo();
		                        } else {
		                        	dest_ClearingInfo = refTransaction.getDestinationClearingInfo();
		                        	if (dest_ClearingInfo == null) {
		                        		dest_ClearingInfo = new ClearingInfo();
		                        		refTransaction.setDestinationClearingInfo(dest_ClearingInfo);
		                        	}
		                        }
		                        dest_ClearingInfo.setClearingState(ClearingState.NOT_CLEARED);
		                        transaction.setDestinationClearingInfo(dest_ClearingInfo);
		                        GeneralDao.Instance.saveOrUpdate(dest_ClearingInfo);
	                        }
	                      //==
	                        SettlementInfo src_SettlementInfo;
	                        if (request) {
	                        	src_SettlementInfo = new SettlementInfo();
	                        } else {
	                        	src_SettlementInfo = refTransaction.getSourceSettleInfo();
	                        	if (src_SettlementInfo == null) {
	                        		src_SettlementInfo = new SettlementInfo();
	                        		refTransaction.setSourceSettleInfo(src_SettlementInfo);
	                        	}
	                        }
	                        src_SettlementInfo.setSettledState(SettledState.NOT_SETTLED);
	                        src_SettlementInfo.setAccountingState(AccountingState.NOT_COUNTED);
	                        transaction.setSourceSettleInfo(src_SettlementInfo);
	                        GeneralDao.Instance.saveOrUpdate(src_SettlementInfo);
	                      //==
	                        SettlementInfo dest_SettlementInfo;
	                        if (request) {
	                        	dest_SettlementInfo = new SettlementInfo();
	                        } else {
	                        	dest_SettlementInfo = refTransaction.getDestinationSettleInfo();
	                        	if (dest_SettlementInfo == null) {
	                        		dest_SettlementInfo = new SettlementInfo();
	                        		refTransaction.setDestinationSettleInfo(dest_SettlementInfo);
	                        	}
	                        }
	                        dest_SettlementInfo.setSettledState(SettledState.NOT_SETTLED);
	                        dest_SettlementInfo.setAccountingState(AccountingState.NOT_COUNTED);
	                        transaction.setDestinationSettleInfo(dest_SettlementInfo);
	                        GeneralDao.Instance.saveOrUpdate(dest_SettlementInfo);
	                      //==
	                        SettlementInfo thirdParty_SettlementInfo;
	                        if (request) {
	                        	thirdParty_SettlementInfo = new SettlementInfo();
	                        } else {
	                        	thirdParty_SettlementInfo = refTransaction.getThirdPartySettleInfo();
	                        	if (thirdParty_SettlementInfo == null) {
	                        		thirdParty_SettlementInfo = new SettlementInfo();
	                        		refTransaction.setThirdPartySettleInfo(thirdParty_SettlementInfo);
	                        	}
	                        }
	                        thirdParty_SettlementInfo.setSettledState(SettledState.NOT_SETTLED);
	                        thirdParty_SettlementInfo.setAccountingState(AccountingState.NOT_COUNTED);
	                        transaction.setThirdPartySettleInfo(thirdParty_SettlementInfo);
	                        GeneralDao.Instance.saveOrUpdate(thirdParty_SettlementInfo);
	                      //==

	                        GeneralDao.Instance.saveOrUpdate(refTransaction);
                        }
                        
                        transaction.setDebugTag(ifxType);
                        transaction.setTransactionType(TransactionType.EXTERNAL);
                        transaction.setBeginDateTime(new DateTime(receivedDtLong));
                        GeneralDao.Instance.save(transaction);
                        trxNumsMap.put(trx, transaction.getId());
                        if (lifeCycleOfTrx == null)
                        	throw new Exception("lifeCycleOfTrx is null !!!!");
                        
                        if (request) {
	                        lifeCyclesMap.put(transaction.getId(), lifeCycleOfTrx);
	                        lifeCyclesList.add(lifeCycleOfTrx);
                        }
                        
                        mapWriter.write(trx + "," + df.format(transaction.getId()));
                        mapWriter.newLine();
                        mapWriter.flush();
                        
                      //========
                        if (recvBankID == null)
                        	recvBankID = destBankID;
                        
                        Ifx incomingIfx = new Ifx();
                        if (ifxType.equals("TRANSFER_FROM_ACCOUNT_RQ") && inputChannel.equals(NDC)) {
                        	incomingIfx.setIfxType(IfxType.TRANSFER_RQ);
                        	incomingIfx.setTrnType(IfxType.getTrnType(IfxType.TRANSFER_RQ));
                        } else if (ifxType.equals("TRANSFER_TO_ACCOUNT_RQ") && inputChannel.equals(CMS_IN)) {
                        	incomingIfx.setIfxType(IfxType.TRANSFER_FROM_ACCOUNT_RS);
                        	incomingIfx.setTrnType(IfxType.getTrnType(IfxType.TRANSFER_FROM_ACCOUNT_RS));
                        } else if (ifxType.equals("TRANSFER_RS") && outputChannel.equals(NDC)) {
                        	incomingIfx.setIfxType(IfxType.TRANSFER_TO_ACCOUNT_RS);
                        	incomingIfx.setTrnType(IfxType.getTrnType(IfxType.TRANSFER_TO_ACCOUNT_RS));
                        } else {
                        	incomingIfx.setIfxType(ifx_type);
                        	incomingIfx.setTrnType(trn_type);
                        }
                        
                        if (cmsAppPAN != null && !cmsAppPAN.equals(appPAN)) {
                        	incomingIfx.setAppPAN(cmsAppPAN);					//EMVRqData.CardAccountId
                        	incomingIfx.setActualAppPAN(cmsAppPAN);			//EMVRqData.CardAccountId
                        } else {
                            incomingIfx.setAppPAN(appPAN);					//EMVRqData.CardAccountId
                        	incomingIfx.setActualAppPAN(appPAN);			//EMVRqData.CardAccountId
                        }
                        incomingIfx.setSecondAppPan(secAppPAN);			//EMVRqData
                        incomingIfx.setIfxDirection(IfxDirection.INCOMING);
                        incomingIfx.setRequest(request);
                        incomingIfx.setBankId(bankID); 					//NetworkTrnInfo
                        incomingIfx.setDestBankId(destBankID); 			//NetworkTrnInfo
                        incomingIfx.setRecvBankId(recvBankID); 			//NetworkTrnInfo
                        incomingIfx.setTerminalType(termType); 			//NetworkTrnInfo
                        incomingIfx.setAuth_Amt(authAmt);				//EMVRqData
                        incomingIfx.setReal_Amt(authAmt);				//EMVRqData
                        incomingIfx.setTrx_Amt(authAmt);				//EMVRqData
                        incomingIfx.setSec_Amt(authAmt);				//EMVRqData
                        incomingIfx.setOrigDt(origDt);					//NetworkTrnInfo
                        incomingIfx.setSrc_TrnSeqCntr(src_TrnSeqCntr);	//NetworkTrnInfo
                        incomingIfx.setMy_TrnSeqCntr(src_TrnSeqCntr);	//NetworkTrnInfo
                        incomingIfx.setTerminalId(terminalId);			//NetworkTrnInfo
                        incomingIfx.setNetworkRefId(networkRefId);		//NetworkTrnInfo
                        incomingIfx.setReceivedDtLong(receivedDtLong);
                        //incomingIfx.setReceivedDt(recievedDt);
                        incomingIfx.setTrnDt(new DateTime(receivedDtLong));	//EMVRqData
                        incomingIfx.setAuth_CurRate("1");				//EMVRqData
                        incomingIfx.setAuth_Currency(364);				//EMVRqData
                        incomingIfx.setSec_CurRate("1");				//EMVRqData
                        incomingIfx.setSec_Currency(364);				//EMVRqData
                        incomingIfx.setSettleDt(settleDt);
                        incomingIfx.setPostedDt(postedDt);
                        if (!request || incomingIfx.getIfxType().equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || incomingIfx.getIfxType().equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) {
                        	incomingIfx.setTotalFeeAmt(totalFeeAmt);	//EMVRsData
                        	incomingIfx.setRsCode(rsCode);				//EMVRsData
                        	incomingIfx.setDocumentNumber(docNumber);	//EMVRsData
                        }
                       incomingIfx.setTransaction(transaction);
                       GeneralDao.Instance.save(incomingIfx);
                       
//                       if (inputChannel.equals(APACS_MG) || inputChannel.equals(APACS_NCC) || outputChannel.equals(APACS_MG) || outputChannel.equals(APACS_NCC) || trx.equals("2019544392")) {
//                    	   System.out.println("poin !!!");
//                       }
                       
                       //========
                       Message incomingMessage = new Message(MessageType.INCOMING);
                       incomingMessage.setTransaction(transaction);
                       incomingMessage.setIfx(incomingIfx);
                       //incomingMessage.setChannelId(ProcessContext.get().getChannelIdbyName(inputChannel)); //Raza commenting
                       incomingMessage.setChannelName(inputChannel);
                       incomingMessage.setChannel(GlobalContext.getInstance().getChannel(inputChannel));

                       incomingMessage.setStartDateTime(transaction.getBeginDateTime());
                       incomingMessage.setRequest(request);
                       incomingMessage.setNeedToBeSent(true);
                       incomingMessage.setNeedToBeInstantlyReversed(false);
                       incomingMessage.setNeedResponse(false);

	                	Terminal endpointTerminal = TerminalService.findEndpointTerminal(incomingMessage, incomingIfx, getEndPointType(inputChannel));
	                	incomingIfx.setEndPointTerminal(endpointTerminal);
	                	incomingMessage.setEndPointTerminal(endpointTerminal);
	                	incomingMessage.setXML(xml_in.toString());

	                	GeneralDao.Instance.saveOrUpdate(incomingIfx);
	                	GeneralDao.Instance.saveOrUpdate(incomingMessage);

				transaction.setInputMessage(incomingMessage);
				GeneralDao.Instance.saveOrUpdate(transaction);


	                	//========
	                	if (ifxType.equals("TRANSFER_FROM_ACCOUNT_RQ") || ifxType.equals("TRANSFER_TO_ACCOUNT_RQ") || ifxType.equals("TRANSFER_TO_ACCOUNT_RS") || ifxType.equals("TRANSFER_RS")) {
		                	if (ifxType.equals("TRANSFER_FROM_ACCOUNT_RQ")) {
		                		if (!inputChannel.equals(SHETAB_IN)) {
			                		refTransaction = getCheckAccountTransactionOfTransfer(incomingIfx);
			                		if (refTransaction == null) {
			                			if (lifeCyclesList.contains(lifeCycleOfTrx)) {
			                				String lc;
									 logger.debug("lifeCycleOfTrx = " + lifeCycleOfTrx);
			                				for (Long trxNum : lifeCyclesMap.keySet()) {
												lc = lifeCyclesMap.get(trxNum);
												if (!trxNum.equals(transaction.getId()) && lifeCycleOfTrx.equals(lc)) {
													 logger.debug("trxNum = " + trxNum);
													refTransaction = GeneralDao.Instance.load(Transaction.class, trxNum);
												}
											}
			                			}
			                		}

//			                		//TODO: temp code
//			                		if (refTransaction == null) {
//			                			//throw new Exception("ref trx not found for : " + ifxType + " & trx : " + trx);
//			                			lifeCycle = new LifeCycle();
//			                			transaction.setLifeCycle(lifeCycle);
//			                			refTransaction = transaction;
//			                		}

			                		transaction.setFirstTransaction(transaction);
			                		transaction.setReferenceTransaction(refTransaction);

							logger.debug("refTransaction = " + refTransaction);
			                		lifeCycle = refTransaction.getLifeCycle();
			                		lifeCycle.setIsComplete(true);
			                		GeneralDao.Instance.saveOrUpdate(lifeCycle);

			                		if (refTransaction.getIncomingIfx().getSecondAppPan() == null && !incomingIfx.getAppPAN().equals(refTransaction.getIncomingIfx().getAppPAN())) {
			                        	Ifx refTrxIfx = refTransaction.getIncomingIfx();
			                        	refTrxIfx.setSecondAppPan(incomingIfx.getAppPAN());
			                        	GeneralDao.Instance.saveOrUpdate(refTrxIfx);
			                		}

			                		transaction.setLifeCycle(lifeCycle);
			                		GeneralDao.Instance.saveOrUpdate(transaction);
		                		} else {
			                		transaction.setFirstTransaction(transaction);

		                			lifeCycle = new LifeCycle();
			                		lifeCycle.setIsComplete(true);
			                		GeneralDao.Instance.saveOrUpdate(lifeCycle);
		                			transaction.setLifeCycle(lifeCycle);
			                		GeneralDao.Instance.saveOrUpdate(transaction);

		                		}

		                	} else if (ifxType.equals("TRANSFER_TO_ACCOUNT_RQ")) {
		                		if (inputChannel.equals(CMS_IN)) {
			                		if (refTransaction == null)
			                			throw new Exception("ref trx not found for : " + ifxType + " & trx : " + trx);

			                		transaction.setFirstTransaction(refTransaction);
			                		transaction.setReferenceTransaction(refTransaction);

			                		lifeCycle = refTransaction.getLifeCycle();
			                		lifeCycle.setIsComplete(true);
			                		GeneralDao.Instance.saveOrUpdate(lifeCycle);

			                		transaction.setLifeCycle(lifeCycle);
			                		GeneralDao.Instance.saveOrUpdate(transaction);

		                		} else if (inputChannel.equals(SHETAB_IN)) {
			                		refTransaction = getCheckAccountTransactionOfTransfer(incomingIfx);
			                		if (refTransaction == null)
			                			throw new Exception("ref trx not found for : " + ifxType + " & trx : " + trx);

			                		transaction.setFirstTransaction(transaction);
			                		transaction.setReferenceTransaction(refTransaction);

			                		lifeCycle = refTransaction.getLifeCycle();
			                		lifeCycle.setIsComplete(true);
			                		GeneralDao.Instance.saveOrUpdate(lifeCycle);

			                		transaction.setLifeCycle(lifeCycle);
			                		GeneralDao.Instance.saveOrUpdate(transaction);

		                		} else {
		                			throw new Exception("Invalid input channel for transfer to : " + inputChannel);
		                		}

		                	} else if (ifxType.equals("TRANSFER_TO_ACCOUNT_RS")) {
		                		if (refTransaction == null)
		                			throw new Exception("ref trx not found for : " + ifxType + " & trx : " + trx);

		                		transaction.setFirstTransaction(refTransaction);
		                		transaction.setReferenceTransaction(refTransaction.getReferenceTransaction());

		                		lifeCycle = refTransaction.getLifeCycle();
		                		lifeCycle.setIsComplete(true);
		                		GeneralDao.Instance.saveOrUpdate(lifeCycle);

		                		transaction.setLifeCycle(lifeCycle);
		                		GeneralDao.Instance.saveOrUpdate(transaction);

		                	} else if (ifxType.equals("TRANSFER_RS")) {
		                		if (refTransaction == null)
		                			throw new Exception("ref trx not found for : " + ifxType + " & trx : " + trx);

		                		transaction.setFirstTransaction(refTransaction);
		                		transaction.setReferenceTransaction(refTransaction.getReferenceTransaction());

		                		lifeCycle = refTransaction.getLifeCycle();
		                		lifeCycle.setIsComplete(true);
		                		GeneralDao.Instance.saveOrUpdate(lifeCycle);

		                		transaction.setLifeCycle(lifeCycle);
		                		GeneralDao.Instance.saveOrUpdate(transaction);

		                	}

	                        //==
	                        if ((!request || ifx_type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || ifx_type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) && rsCode != null && Integer.parseInt(rsCode) > 0) {
	                        	if (refTransaction != null && refTransaction.getSourceClearingInfo() != null) {
	                        		refTransaction.setSourceClearingInfo(null);
	                        		GeneralDao.Instance.saveOrUpdate(refTransaction);
	                        	}
	                        } else {
			                	ClearingInfo src_ClearingInfo;
			                	if (refTransaction != null)
			                		src_ClearingInfo = refTransaction.getSourceClearingInfo();
			                	else
			                		src_ClearingInfo = new ClearingInfo();

	                        	if (src_ClearingInfo == null) {
	                        		src_ClearingInfo = new ClearingInfo();
	                        		refTransaction.setSourceClearingInfo(src_ClearingInfo);
	                        	}
		                        src_ClearingInfo.setClearingState(ClearingState.NOT_CLEARED);
		                        transaction.setSourceClearingInfo(src_ClearingInfo);
		                        GeneralDao.Instance.saveOrUpdate(src_ClearingInfo);
	                        }
	                      //==
	                        if ((!request || ifx_type.equals(IfxType.TRANSFER_FROM_ACCOUNT_RS) || ifx_type.equals(IfxType.TRANSFER_TO_ACCOUNT_RS)) && rsCode != null && Integer.parseInt(rsCode) > 0) {
	                        	if (refTransaction != null && refTransaction.getDestinationClearingInfo() != null) {
	                        		refTransaction.setDestinationClearingInfo(null);
	                        		GeneralDao.Instance.saveOrUpdate(refTransaction);
	                        	}
	                        } else {
		                        ClearingInfo dest_ClearingInfo;
		                        if (refTransaction != null)
		                        	dest_ClearingInfo = refTransaction.getDestinationClearingInfo();
		                        else
		                        	dest_ClearingInfo = new ClearingInfo();

	                        	if (dest_ClearingInfo == null) {
	                        		dest_ClearingInfo = new ClearingInfo();
	                        		refTransaction.setDestinationClearingInfo(dest_ClearingInfo);
	                        	}
		                        dest_ClearingInfo.setClearingState(ClearingState.NOT_CLEARED);
		                        transaction.setDestinationClearingInfo(dest_ClearingInfo);
		                        GeneralDao.Instance.saveOrUpdate(dest_ClearingInfo);
	                        }
	                      //==
	                        SettlementInfo src_SettlementInfo;
	                        if (refTransaction != null)
	                        	src_SettlementInfo = refTransaction.getSourceSettleInfo();
	                        else
	                        	src_SettlementInfo = new SettlementInfo();

                        	if (src_SettlementInfo == null) {
                        		src_SettlementInfo = new SettlementInfo();
                        		refTransaction.setSourceSettleInfo(src_SettlementInfo);
                        	}
	                        src_SettlementInfo.setSettledState(SettledState.NOT_SETTLED);
	                        src_SettlementInfo.setAccountingState(AccountingState.NOT_COUNTED);
	                        transaction.setSourceSettleInfo(src_SettlementInfo);
	                        GeneralDao.Instance.saveOrUpdate(src_SettlementInfo);
	                      //==
	                        SettlementInfo dest_SettlementInfo;
	                        if (refTransaction != null)
	                        	dest_SettlementInfo = refTransaction.getDestinationSettleInfo();
	                        else
	                        	dest_SettlementInfo = new SettlementInfo();

                        	if (dest_SettlementInfo == null) {
                        		dest_SettlementInfo = new SettlementInfo();
                        		refTransaction.setDestinationSettleInfo(dest_SettlementInfo);
                        	}
	                        dest_SettlementInfo.setSettledState(SettledState.NOT_SETTLED);
	                        dest_SettlementInfo.setAccountingState(AccountingState.NOT_COUNTED);
	                        transaction.setDestinationSettleInfo(dest_SettlementInfo);
	                        GeneralDao.Instance.saveOrUpdate(dest_SettlementInfo);
	                      //==
	                        SettlementInfo thirdParty_SettlementInfo;
	                        if (refTransaction != null)
	                        	thirdParty_SettlementInfo = refTransaction.getThirdPartySettleInfo();
	                        else
	                        	thirdParty_SettlementInfo = new SettlementInfo();

                        	if (thirdParty_SettlementInfo == null) {
                        		thirdParty_SettlementInfo = new SettlementInfo();
                        		refTransaction.setThirdPartySettleInfo(thirdParty_SettlementInfo);
                        	}
	                        thirdParty_SettlementInfo.setSettledState(SettledState.NOT_SETTLED);
	                        thirdParty_SettlementInfo.setAccountingState(AccountingState.NOT_COUNTED);
	                        transaction.setThirdPartySettleInfo(thirdParty_SettlementInfo);
	                        GeneralDao.Instance.saveOrUpdate(thirdParty_SettlementInfo);
	                      //==

	                        if (incomingIfx.getSecondAppPan() == null) {
	                        	Ifx firstTrxIfx = transaction.getFirstTransaction().getIncomingIfx();
	                        	incomingIfx.setAppPAN(firstTrxIfx.getAppPAN());
	                        	incomingIfx.setActualAppPAN(incomingIfx.getAppPAN());
	                        	incomingIfx.setSecondAppPan(firstTrxIfx.getSecondAppPan());
	                        }

	                        GeneralDao.Instance.saveOrUpdate(refTransaction);
	                        GeneralDao.Instance.saveOrUpdate(transaction);

	                	}
	                	//========

                        Ifx outgoingIfx = incomingIfx.clone();
                        outgoingIfx.setIfxType(ifx_type);
                        outgoingIfx.setTrnType(trn_type);

                        if (ifxType.equals("TRANSFER_TO_ACCOUNT_RQ") && inputChannel.equals(CMS_IN)) {
                            outgoingIfx.setEMVRqData(incomingIfx.getEMVRqData().copy());
                        	outgoingIfx.setAppPAN(incomingIfx.getSecondAppPan());
                        	outgoingIfx.setSecondAppPan(incomingIfx.getAppPAN());
                        }
                        outgoingIfx.setIfxDirection(IfxDirection.OUTGOING);
                        outgoingIfx.setRequest(request);
                        outgoingIfx.setReceivedDtLong(receivedDtLong);
                        outgoingIfx.setTransaction(transaction);
                       GeneralDao.Instance.save(outgoingIfx);

                       //========
                       Message outgoingMessage = new Message(MessageType.OUTGOING);
                       outgoingMessage.setTransaction(transaction);
                       outgoingMessage.setIfx(outgoingIfx);
                       //outgoingMessage.setChannelId(ProcessContext.get().getChannelIdbyName(outputChannel)); //Raza commenting
                        outgoingMessage.setChannelName(outputChannel);
                       outgoingMessage.setChannel(GlobalContext.getInstance().getChannel(outputChannel));
                       
                       outgoingMessage.setStartDateTime(transaction.getBeginDateTime());
                       outgoingMessage.setRequest(request);
                       outgoingMessage.setNeedToBeSent(true);
                       outgoingMessage.setNeedToBeInstantlyReversed(false);
                       outgoingMessage.setNeedResponse(false);

			logger.debug("message.getTransaction() = " + outgoingMessage.getTransaction().getId());
			logger.debug("message.getTransaction().getInputMessage() = " + outgoingMessage.getTransaction().getInputMessage());
			if (outgoingMessage.getTransaction().getFirstTransaction() != null) {
				logger.debug("message.getTransaction().getFirstTransaction() = " + outgoingMessage.getTransaction().getFirstTransaction().getId());
				logger.debug("message.getTransaction().getFirstTransaction().getInputMessage() = " + outgoingMessage.getTransaction().getFirstTransaction().getInputMessage());
			}
                       
	                	endpointTerminal = TerminalService.findEndpointTerminal(outgoingMessage, outgoingIfx, getEndPointType(outputChannel));
	                	outgoingIfx.setEndPointTerminal(endpointTerminal);
	                	outgoingMessage.setEndPointTerminal(endpointTerminal);
	                	outgoingMessage.setXML(xml_out.toString());
	                	
	                	GeneralDao.Instance.saveOrUpdate(outgoingIfx);
	                	GeneralDao.Instance.saveOrUpdate(outgoingMessage);
                       
                      //================================================================================================================
//                        GeneralDao.Instance.endTransaction();


                        infoWriter.write("ifxType        = " + ifxType);
                        infoWriter.newLine();
                        infoWriter.write("rsCode         = " + rsCode);
                        infoWriter.newLine();
                        infoWriter.write("appPAN         = " + appPAN);
                        infoWriter.newLine();
                        infoWriter.write("terminalId     = " + terminalId);
                        infoWriter.newLine();
                        infoWriter.write("bankID         = " + bankID);
                        infoWriter.newLine();
                        infoWriter.write("destBankID     = " + destBankID);
                        infoWriter.newLine();
                        infoWriter.write("settleDt       = " + settleDt);
                        infoWriter.newLine();
                        infoWriter.write("postedDt       = " + postedDt);
                        infoWriter.newLine();
                        infoWriter.write("termType       = " + termType);
                        infoWriter.newLine();
                        infoWriter.write("authAmt        = " + df.format(authAmt));
                        infoWriter.newLine();
                        infoWriter.write("totalFeeAmt    = " + df.format(totalFeeAmt));
                        infoWriter.newLine();
                        infoWriter.write("request        = " + request);
                        infoWriter.newLine();
                        infoWriter.write("src_TrnSeqCntr = " + src_TrnSeqCntr);
                        infoWriter.newLine();
                        infoWriter.write("networkRefId   = " + networkRefId);
                        infoWriter.newLine();
                        infoWriter.write("receivedDtLong = " + df.format(receivedDtLong));
                        infoWriter.newLine();
                        infoWriter.write("origDt         = " + origDt);
                        infoWriter.newLine();
                        infoWriter.write("refTrx         = " + refTrx);
                        infoWriter.newLine();
                        infoWriter.write("trx            = " + trx);
                        infoWriter.newLine();
                        infoWriter.write(xml_in.toString());
                        infoWriter.write("=========");
                        infoWriter.write(xml_out.toString());
                        infoWriter.write("======================================");
                        infoWriter.newLine();
                    } catch (Exception e) {
                        System.out.println("line = " + line);
			logger.error("line = " + line);
			logger.error(e, e);
                        throw e;
                    }
                } else {
                    for (int k = 0; k < log.size(); k++) {
                        badsWriter.write(log.get(k));
                        badsWriter.newLine();
                    }
                    badsWriter.write("=========================================================");
                    badsWriter.newLine();
                }
            }

	    GeneralDao.Instance.endTransaction();

	    System.out.println("Script Finished!");
            logger.debug("Script Finished!");


        } catch (Exception e) {
            e.printStackTrace();
	    logger.error(e, e);
        } finally {
            bufferedWriter.close();
            badsWriter.close();
            infoWriter.close();
            mapWriter.close();
        }
    }

    private static void readXML(String channel, String threadName, List<String> log, int index, StringBuilder xml_in) throws Exception {
        if (channel.equals(SHETAB_IN) || channel.equals(SHETAB_OUT)) {
            for (int k = (index + 1); k < log.size(); k++) {
                xml_in.append(log.get(k).trim()).append("\r\n");
                if (log.get(k).startsWith("</isomsg>"))
                    break;
            }
        } else if (channel.equals(CMS_IN) || channel.equals(CMS_OUT)) {
            for (int k = (index + 1); k < log.size(); k++) {
                xml_in.append(log.get(k).trim()).append("\r\n");
                if (log.get(k).startsWith("</cmsmsg>"))
                    break;
            }
        } else if (channel.equals(EPAY_IN) || channel.equals(EPAY_OUT)) {
            for (int k = (index + 1); k < log.size(); k++) {
                xml_in.append(log.get(k).trim()).append("\r\n");
                if (log.get(k).startsWith("</command>"))
                    break;
            }
        } else if (channel.equals(NDC) || channel.equals(APACS_NCC) || channel.equals(APACS_MG)) {
            for (int k = (index + 1); k < log.size(); k++) {
                if (log.get(k).indexOf(threadName) >= 0)
                    break;
                xml_in.append(log.get(k).trim()).append("\r\n");
            }
        } else if (channel.equals(INFO_TEC)) {
            for (int k = (index + 1); k < log.size(); k++) {
                xml_in.append(log.get(k).trim()).append("\r\n");
                if (log.get(k).startsWith("</isomsg>"))
                    break;
            }
        } else {
            throw new Exception("Invalid channel : " + channel);
        }
    }
    
    private static EndPointType getEndPointType(String channel) throws Exception {
    	if (channel.equals(SHETAB_IN) || channel.equals(SHETAB_OUT)) {
    		return EndPointType.SWITCH_TERMINAL;
    	} else if (channel.equals(CMS_IN) || channel.equals(CMS_OUT)) {
    		return EndPointType.SWITCH_TERMINAL;
    	} else if (channel.equals(EPAY_IN) || channel.equals(EPAY_OUT)) {
    		return EndPointType.EPAY_SWITCH_TERMINAL;
        } else if (channel.equals(NDC)) {
        	return EndPointType.ATM_TERMINAL;
        } else if (channel.equals(APACS_NCC)) {
        	return EndPointType.POS_TERMINAL;
        } else if (channel.equals(APACS_MG)) {
        	return EndPointType.POS_TERMINAL;
        } else if (channel.equals(INFO_TEC)) {
        	return EndPointType.POS_TERMINAL;
        } else {
            throw new Exception("Invalid channel : " + channel);
        }
    }

    public static Transaction getCheckAccountTransactionOfTransfer(Ifx ifx) {

        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = "select i.transaction from Ifx as i "
                + " where "
                + " i.dummycol in (0,1,2,3,4,5,6,7,8,9) "
                + " and i.ifxDirection = :IfxDirection "
                + " and i.eMVRqData.CardAcctId.AppPAN = :secAppPAN "
                + " and i.trnType = :TrnType "
                + " and i.receivedDtLong >= :tenMinBefore "
                + " and i.receivedDtLong <= :now "
                + " and i.request = true ";

        if (ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())) {
                queryString += " and i.endPointTerminalCode = :endPoint ";
                params.put("endPoint", ifx.getEndPointTerminal().getCode());
        } else {
                queryString += " and i.endPointTerminalCode = :endPoint ";
                params.put("endPoint", ifx.getEndPointTerminal().getCode());

                queryString += " and i.networkTrnInfo.TerminalId = :terminalId ";
                params.put("terminalId", ifx.getTerminalId());
        }

        params.put("IfxDirection", IfxDirection.INCOMING);

        DateTime tenMinBefore = DateTime.toDateTime(DateTime.now().getTime() - 10 * DateTime.ONE_MINUTE_MILLIS);
        params.put("tenMinBefore", 20121012030000L);
        params.put("now",  + 20121012031600L);

        /****** NOTE: some of banks don't following this rule *******/

        if(ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())){
    /**
                if (Util.hasText(ifx.getNetworkRefId())) {
                        queryString += " and i.networkTrnInfo.NetworkRefId= :NetworkRefId ";
                        params.put("NetworkRefId", ifx.getNetworkRefId());
                }
**/

        }

        /************************************************************/

        TrnType trnType = TrnType.UNKNOWN;

        if (TrnType.TRANSFER.equals(ifx.getTrnType()) ){
                trnType = TrnType.CHECKACCOUNT;
                params.put("TrnType", trnType);
                params.put("secAppPAN", ifx.getSecondAppPan());

        }else if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())){
                trnType = TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT;
                params.put("TrnType", trnType);
                params.put("secAppPAN", ifx.getSecondAppPan());

        }else if (TrnType.INCREMENTALTRANSFER.equals(ifx.getTrnType())){
                trnType = TrnType.CHECKACCOUNT;
                params.put("TrnType", trnType);
                params.put("secAppPAN", ifx.getAppPAN());

        }else if (TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())){
                trnType = TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT;
                params.put("secAppPAN", ifx.getAppPAN());
                params.put("TrnType", trnType);

        }


        List<IfxType> ifxTypes = TrnType.getIfxType(trnType);
        List<IfxType> list = new ArrayList<IfxType>();
        for(IfxType ifxType: ifxTypes) {
                if (ISOFinalMessageType.isRequestMessage(ifxType))
                        list.add(ifxType);
        }

        if (list.size() > 0) {
                queryString += " and i.ifxType in ";
                queryString += IfxType.getIfxTypeOrdinalsOfList(list);
        }

        queryString += " order by i.receivedDtLong desc";

        logger.debug("queryString = " + queryString);
        for (String paramName : params.keySet()) {
        	logger.debug(paramName + " = " + params.get(paramName));
        }
     
        Transaction result = (Transaction) GeneralDao.Instance.findObject(queryString, params);
	 logger.debug("result = " + result);

        return result;

}


}
