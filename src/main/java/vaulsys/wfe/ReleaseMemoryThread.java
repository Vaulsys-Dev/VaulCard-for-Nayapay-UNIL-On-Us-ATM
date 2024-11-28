package vaulsys.wfe;

import vaulsys.caching.CheckAccountParamsForCache;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.SettlementData;
import vaulsys.network.NetworkManager;
import vaulsys.transaction.TransactionService;
import vaulsys.util.MyDateFormatNew;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

public class ReleaseMemoryThread implements Runnable {
    Logger logger = Logger.getLogger(this.getClass());
    private long ITERATION;
    private LinkedList<List<Long>> respOnSameSocketConns;

    SettlementData[] settlementDatas;

    public ReleaseMemoryThread() {
        super();
        ITERATION = 0;
        respOnSameSocketConns = new LinkedList<List<Long>>();
    }


    @Override
    public void run() {
        logger.debug("ReleaseMemoryThread: I am started...");

        while (true) {
            try {
                DateTime now = DateTime.now();
                try {
                    ITERATION++;
                    /************ Removing SecurityMap (inserted before 5 min) *************/
                    Map<Long, String> securityData = GlobalContext.getInstance().getSecurityData();
                    logger.debug("size of SecurityDataMap before removing: " + securityData.size());
                    Set<Long> keySet = securityData.keySet();
                    boolean isNeedToRemove = false;
                    DateTime insertionDate = null;
                    String secStr = "";
                    for (Long item : keySet) {
                        isNeedToRemove = false;
                        secStr = securityData.get(item);
                        try {
                            insertionDate = new DateTime(MyDateFormatNew.parse("yyyy/MM/dd, HH:mm:ss", TransactionService.getFromSecurity(secStr, "INSERT_DATE").toString()));
                        } catch (ParseException e) {
                            logger.error(e, e);
                            continue;
                        }

                        int compareDay = now.getDayDate().compareTo(insertionDate.getDayDate());
                        if (compareDay == 0) {
                            if (now.compareTo(insertionDate) < 500) {
                                continue;

                            } else {
                                isNeedToRemove = true;
                            }
                        } else if (compareDay == 1 &&
                                (insertionDate.getDayTime().compareTo(now.getDayTime()) / 10000) == 23 &&
                                (insertionDate.getDayTime().compareTo(now.getDayTime()) % 10000 > 5500)) {
                            continue;

                        } else {
                            isNeedToRemove = true;
                        }

                        if (isNeedToRemove) {
                            logger.debug("removed from security, lifeCycle: " + item);
                            securityData.remove(item);
                        }
                    }

                    logger.debug("size of SecurityDataMap after removing: " + securityData.size());
                } catch (Exception e) {
                    logger.error("Exception in ReleaseMemory: " + e.getMessage());
                }

                /************ Removing acctInfoForTransferMap(inserted before 5 min) ***********/
                try {
                    boolean isNeedToRemove = false;
                    Map<Long, String> acctInfoMap = GlobalContext.getInstance().getAcctInfoForTransfer();
                    logger.debug("size of accInfoMap befor removing: " + acctInfoMap.size());
                    Set<Long> acctInfoKeySet = acctInfoMap.keySet();
                    String insertionDateTimeStr = "";
                    String acctInfoStr = "";
                    for (Long item : acctInfoKeySet) {
                        isNeedToRemove = false;
                        acctInfoStr = acctInfoMap.get(item);
                        StringTokenizer tokenizer = new StringTokenizer(acctInfoStr, "|");
                        insertionDateTimeStr = tokenizer.nextToken().trim();
                        try {
                            insertionDateTimeStr = insertionDateTimeStr.replace("/", "").replace(":", "").replace(",", "").replace(" ", "");
                            DateTime dateTime = new DateTime(Long.valueOf(insertionDateTimeStr.trim()));

                            int compareDay = now.getDayDate().compareTo(dateTime.getDayDate());
                            if (compareDay == 0) {
                                if (now.compareTo(dateTime) < 500) {
                                    continue;

                                } else {
                                    isNeedToRemove = true;
                                }
                            } else if (compareDay == 1 &&
                                    (dateTime.getDayTime().compareTo(now.getDayTime()) / 10000) == 23 &&
                                    (dateTime.getDayTime().compareTo(now.getDayTime()) % 10000 > 5500)) {
                                continue;

                            } else {
                                isNeedToRemove = true;
                            }

                            if (isNeedToRemove) {
                                logger.debug("removed from acctInfoMap, lifeCycle: " + item);
                                acctInfoMap.remove(item);
                            }

                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                    logger.debug("size of accInfoMap after removing: " + acctInfoMap.size());
                } catch (Exception e) {
                    logger.error("Exception in ReleaseMemory: " + e.getMessage());
                }

                /*************************/

                /************ Removing BindingMap (inserted before 5 min) *************/
                try {
                    boolean isNeedToRemove = false;
                    Map<String, Long> bindingMap = GlobalContext.getInstance().getBindTransaction();
                    logger.debug("size of BindingMap before removing: " + bindingMap.size());
                    Set<String> keySetBinding = bindingMap.keySet();
                    DateTime insertionDate = null;
                    String insertionDateStr = "";
                    String[] split;
                    for (String item : keySetBinding) {
                        isNeedToRemove = false;
                        insertionDate = null;
                        split = item.split("\\|");
                        insertionDateStr = split[0];
                        try {
                            insertionDate = new DateTime(MyDateFormatNew.parse("yyyy/MM/dd, HH:mm:ss", insertionDateStr));
                        } catch (ParseException e) {
                            logger.error(e, e);
                            continue;
                        }

                        int compareDay = now.getDayDate().compareTo(insertionDate.getDayDate());
                        if (compareDay == 0) {
                            if (now.compareTo(insertionDate) < 500) {
                                continue;

                            } else {
                                isNeedToRemove = true;
                            }
                        } else if (compareDay == 1 &&
                                (insertionDate.getDayTime().compareTo(now.getDayTime()) / 10000) == 23 &&
                                (insertionDate.getDayTime().compareTo(now.getDayTime()) % 10000 > 5500)) {
                            continue;

                        } else {
                            isNeedToRemove = true;
                        }

                        if (isNeedToRemove) {
                            logger.debug("removed from binding: " + item);
                            bindingMap.remove(item);
                        }
                    }
                    logger.debug("size of BindingMap after removing: " + bindingMap.size());
                } catch (Exception e) {
                    logger.error("Exception in ReleaseMemory: " + e.getMessage());
                }

                /*************************/


                /************ Removing NetworkManager reaponseOnSameSocketConnections *************/
                try {
                    ConcurrentHashMap<Long, IoSession> reaponseOnSameSocketConnections = NetworkManager.getInstance().getResponseOnSameSocketConnections();
                    logger.debug("size of reaponseOnSameSocketConnections before removing: " + reaponseOnSameSocketConnections.size());
                    Set<Long> keySet2 = reaponseOnSameSocketConnections.keySet();
                    respOnSameSocketConns.addLast(new ArrayList<Long>(keySet2));
                    if (ITERATION > 5) {
                        for (Long msgId : respOnSameSocketConns.getFirst()) {
                            if (reaponseOnSameSocketConnections.remove(msgId) != null) {
                                logger.debug("removing msgId from reaponseOnSameSocketConnections:" + msgId);
                            }
                        }
                        respOnSameSocketConns.removeFirst();
                    }
                    logger.debug("size of reaponseOnSameSocketConnections after removing: " + reaponseOnSameSocketConnections.size());
                } catch (Exception e) {
                    logger.error("Exception in ReleaseMemory: " + e.getMessage());
                }


                /*************************/

                /******************** Removing checkAccount for transfer from 10 min ago **********************/
                try {
                    DateTime nowCheckAccount = DateTime.now();
                    nowCheckAccount.decrease(10);
                    Map<CheckAccountParamsForCache, Long> checkAccountMap = GlobalContext.getInstance().getCheckAccountForTransafer();

                    logger.debug("size of checkAccountForTransaferMap before removing: " + checkAccountMap.size());

                    Set<CheckAccountParamsForCache> keySet3 = checkAccountMap.keySet();

                    for (CheckAccountParamsForCache checkAccount : keySet3) {

                        DateTime TrxDateTime = new DateTime(checkAccount.getReceivedDt());

                        if (TrxDateTime.beforeEquals(nowCheckAccount)) {

                            logger.debug("checkAccountCache: removed from checkAccountForTransfer, TrxId: " + checkAccountMap.get(checkAccount));

                            checkAccountMap.remove(checkAccount);
                        }
                    }

                    logger.debug("checkAccountCache: size of checkAccountForTransaferMap after removing:" + checkAccountMap.size());

                } catch (Exception e) {
                    logger.error("checkAccountCache: An exception occure in releasing checkAccount cache! " + e.getMessage());
                }
                /**********************************************************************************************/
                try {
                    logger.debug("ReleaseMemoryThread: I am sleeped...");
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    logger.error(e, e);
                }
            } catch (Exception e) {
                logger.error("Exception in ReleaseMemory: " + e.getMessage());
            }
        }
    }
}
