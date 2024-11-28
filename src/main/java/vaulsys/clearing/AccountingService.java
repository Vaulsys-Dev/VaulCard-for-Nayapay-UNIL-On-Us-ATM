package vaulsys.clearing;

import groovy.lang.Binding;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.FeeType;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementDataReport;
import vaulsys.clearing.base.SettlementDataReportType;
import vaulsys.clearing.base.SettlementRecord;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.clearing.base.SettlementState;
import vaulsys.clearing.base.SettlementStateType;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.report.AcqShetabReportRecord;
import vaulsys.clearing.report.BillPayReportRecord;
import vaulsys.clearing.report.ChargeReportRecord;
import vaulsys.clearing.report.IssShetabReportRecord;
import vaulsys.clearing.report.RemainingChargeRecord;
import vaulsys.clearing.report.ReportRecord;
import vaulsys.clearing.report.TotalShetabReportRecord;
import vaulsys.clearing.settlement.CoreConfigDataManager;
import vaulsys.customer.Core;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.Institution;
import vaulsys.entity.impl.Organization;
import vaulsys.entity.impl.Shop;
import vaulsys.fee.FeeService;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.IssuingDocumentAction;
import vaulsys.scheduler.SchedulerService;
import vaulsys.security.component.SecurityComponent;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.AccountingState;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.SettlementInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.NotUsed;
import vaulsys.util.Pair;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.hibernate.transform.AliasToBeanResultTransformer;

import com.fanap.business.deposit.service.valueobjects.TransferMoneyReturnVO;
import com.fanap.cms.business.corecommunication.biz.CoreGateway;
import com.fanap.cms.exception.BusinessException;
import com.fanap.cms.valueobjects.corecommunication.DepositInfoForIssueDocument;
import com.fanap.cms.valueobjects.corecommunication.DocumentItemEntity;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO;

public class AccountingService {
    private static Logger logger = Logger.getLogger(AccountingService.class);

    public static SettlementData findSettlementData(FinancialEntity entity, Terminal terminal, ClearingProfile clearingProfile,
                                                    SettlementDataType type, DateTime settleDate) {
        String query = "from SettlementData s where "
//			+ " s.settlementTime.dayDate = :stlDate " 
//			+ " and s.settlementTime.dayTime = :stlTime ";
                + " s.settlementTimeLong = :stlDate";

        /***** BACHE joon! settlement data e ke sanad khorde, dadehasho taghir nade*****/
        query += " and s.settlementState is null "
                + " and s.settlementReport is null "
                + " and s.documentNumber is null";
        /***** LEILA *****/


        Map<String, Object> params = new HashMap<String, Object>();

        if (entity != null) {
            query += " and s.financialEntity = :entity ";
            params.put("entity", entity);
        }

        if (terminal != null) {
            query += " and s.terminal = :terminal ";
            params.put("terminal", terminal);
        }

        if (clearingProfile != null) {
            query += " and s.clearingProfile = :clearingProfile ";
            params.put("clearingProfile", clearingProfile);
        }

        if (type != null) {
            query += " and s.type = :type ";
            params.put("type", type);
        }

        params.put("stlDate", settleDate.getDateTimeLong());

        return (SettlementData) GeneralDao.Instance.findObject(query, params);
    }

    public static List<SettlementData> findSettlementData(FinancialEntity entity, ClearingProfile clearingProfile,
                                                          SettlementDataType type, DateTime settleDate) {
        String query = "from SettlementData s where "
                + " s.settlementTimeLong = :stlDate";
        Map<String, Object> params = new HashMap<String, Object>();

        if (entity != null) {
            query += " and s.financialEntity = :entity ";
            params.put("entity", entity);
        }

        if (clearingProfile != null) {
            query += " and s.clearingProfile = :clearingProfile ";
            params.put("clearingProfile", clearingProfile);
        }

        if (type != null) {
            query += " and s.type = :type ";
            params.put("type", type);
        }
        params.put("stlDate", settleDate.getDateTimeLong());
        return GeneralDao.Instance.find(query, params);
    }

    public static List<SettlementData> findSettlementData(FinancialEntity entity, Terminal terminal, DayDate settleDate) {
        return findSettlementData(entity, terminal, null, settleDate);
    }

    public static List<SettlementData> findSettlementData(FinancialEntity entity, Terminal terminal, ClearingProfile clearingProfile, DayDate settleDate) {
        logger.debug("Start findSettlementData for settleDate = " + settleDate);
        String query = "from SettlementData s where "
                + " s.settlementTimeLong between :stlDay and :nextStlDay ";

        Map<String, Object> params = new HashMap<String, Object>();

        DateTime date = new DateTime(settleDate, new DayTime(0, 0, 0));
        DateTime nextDate = new DateTime(settleDate.nextDay(), new DayTime(0, 0, 0));
        params.put("stlDay", date.getDateTimeLong());
        params.put("nextStlDay", nextDate.getDateTimeLong());

        if (entity != null) {
            query += " and s.financialEntity = :entity ";
            params.put("entity", entity);
        }

        if (terminal != null) {
            query += " and s.terminal = :terminal ";
            params.put("terminal", terminal);
        }

        if (clearingProfile != null) {
            query += " and s.clearingProfile = :clearingProfile ";
            params.put("clearingProfile", clearingProfile);
        }

        return GeneralDao.Instance.find(query, params);
    }

    public static List<SettlementInfo> getRelatedNonRoutinSettlementInfo(Transaction transaction) {
        List<SettlementInfo> routinSettleInfo = new ArrayList<SettlementInfo>();

        SettlementInfo settleInfo = transaction.getSourceSettleInfo();
        if (settleInfo != null)
            routinSettleInfo.add(settleInfo);

        settleInfo = transaction.getDestinationSettleInfo();
        if (settleInfo != null)
            routinSettleInfo.add(settleInfo);

        settleInfo = transaction.getThirdPartySettleInfo();
        if (settleInfo != null)
            routinSettleInfo.add(settleInfo);

        Map<String, Object> params = new HashMap<String, Object>();
        String query = "from " + SettlementInfo.class.getName() + " s "
                + " where "
                + " s.transaction = :transaction ";
        if (routinSettleInfo.size() > 0) {
            query += " and s not in(:routinSettleInfo) ";
            params.put("routinSettleInfo", routinSettleInfo);
        }

        params.put("transaction", transaction);
        return GeneralDao.Instance.find(query, params);
    }

    private static SettlementInfo updateAmountOnSettleInfo(Transaction transaction, Terminal terminal, long amount) {
        SettlementInfo settleInfo = TransactionService.getRelatedSettleInfo(transaction, terminal);
        if (settleInfo == null)
            settleInfo = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, DateTime.now(), transaction);
        if (settleInfo.getTotalFee() == null) {
            settleInfo.setTotalFee(0L);
        }
        settleInfo.setTotalAmount(amount);
        GeneralDao.Instance.saveOrUpdate(settleInfo);
        return settleInfo;
    }

    public static void generateSettlementDataAndUpdateSettleInfosForAllEntities(Map<FinancialEntity, Map<SettlementDataType, SettlementData>> settlementDatas, Terminal mainTerminal, Terminal endPointTerminal,
                                                                                Transaction transaction, long mainAmount, long endPointAmount, ClearingProfile clearingProfile,
                                                                                SettlementDataType type, DateTime settleDate) {
        Map<FinancialEntity, SettlementInfo> entityStlInfo = new HashMap<FinancialEntity, SettlementInfo>();
        Map<FinancialEntity, Terminal> entityTerminal = new HashMap<FinancialEntity, Terminal>();

        SettlementInfo info;
        if (!mainTerminal.equals(endPointTerminal)) {
            info = updateAmountOnSettleInfo(transaction, mainTerminal, mainAmount);
            entityStlInfo.put(mainTerminal.getOwner(), info);
            entityTerminal.put(mainTerminal.getOwner(), mainTerminal);
        } else {
            info = updateAmountOnSettleInfo(transaction, endPointTerminal, endPointAmount);
            entityStlInfo.put(endPointTerminal.getOwner(), info);
            entityTerminal.put(endPointTerminal.getOwner(), endPointTerminal);
        }

        if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
            List<Object[]> fees = FeeService.getFees(transaction, clearingProfile, FeeType.DEBIT);
            Iterator<Object[]> iterator = fees.iterator();
            while (iterator.hasNext()) {
                Object[] entityFee = iterator.next();

                if (entityFee == null || entityFee[0] == null || entityFee[1] == null)
                    continue;

                FinancialEntity otherEntity = GeneralDao.Instance.load(FinancialEntity.class, (Long)entityFee[0]);
                if (otherEntity == null)
                    continue;

                info = entityStlInfo.get(otherEntity);
                if (info == null) {
                    if (otherEntity.equals(mainTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, mainTerminal);
                        info = updateAmountOnSettleInfo(transaction, mainTerminal, mainAmount);
                        entityTerminal.put(mainTerminal.getOwner(), mainTerminal);
                    }

                    else if (otherEntity.equals(endPointTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, endPointTerminal);
                        info = updateAmountOnSettleInfo(transaction, endPointTerminal, endPointAmount);
                        entityTerminal.put(endPointTerminal.getOwner(), endPointTerminal);
                    }

                    else {
                        info = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, DateTime.now(), transaction);
                        info.setTotalAmount(0L);
                    }
                }

                info.setTotalFee(-1 * (Long) entityFee[1]);
                entityStlInfo.put(otherEntity, info);
            }

            fees = FeeService.getFees(transaction, clearingProfile, FeeType.CREDIT);
            iterator = fees.iterator();
            while (iterator.hasNext()) {
                Object[] entityFee = iterator.next();

                if (entityFee == null || entityFee[0] == null || entityFee[1] == null)
                    continue;

                FinancialEntity otherEntity = GeneralDao.Instance.load(FinancialEntity.class, (Long)entityFee[0]);
                if (otherEntity == null)
                    continue;

                info = entityStlInfo.get(otherEntity);
                if (info == null) {
                    if (otherEntity.equals(mainTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, mainTerminal);
                        info = updateAmountOnSettleInfo(transaction, mainTerminal, mainAmount);
                        entityTerminal.put(mainTerminal.getOwner(), mainTerminal);
                    }

                    else if (otherEntity.equals(endPointTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, endPointTerminal);
                        info = updateAmountOnSettleInfo(transaction, endPointTerminal, endPointAmount);
                        entityTerminal.put(endPointTerminal.getOwner(), endPointTerminal);
                    }

                    else {
                        info = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, DateTime.now(), transaction);
                        info.setTotalAmount(0L);
                    }
                }

                Long oldFee = info.getTotalFee();
                if (oldFee == null)
                    oldFee = 0L;
                info.setTotalFee(oldFee + (Long) entityFee[1]);
                entityStlInfo.put(otherEntity, info);
            }
        }

        if (entityStlInfo != null && entityStlInfo.size() > 0) {
            for (FinancialEntity entity : entityStlInfo.keySet()) {
                Terminal terminal = entityTerminal.get(entity);
                Map<SettlementDataType, SettlementData> types = settlementDatas.get(entity);
                if (types == null)
                    types = new HashMap<SettlementDataType, SettlementData>();
                SettlementData settlementData = types.get(type);

                settlementData = generateSettlementDataForEntity(settlementData, entity, terminal, entityStlInfo.get(entity), clearingProfile, type, settleDate, false);
                types.put(type, settlementData);
                settlementDatas.put(entity, types);
            }
        }
    }

    public static void generateSettlementDataAndUpdateSettleInfosForAllEntitiesPer(Map<FinancialEntity, Map<SettlementDataType, List<SettlementData>>> settlementDatas, Terminal mainTerminal, Terminal endPointTerminal,
                                                                                   Transaction transaction, long mainAmount, long endPointAmount, ClearingProfile clearingProfile,
                                                                                   SettlementDataType type, DateTime settleDate) {
        Map<FinancialEntity, SettlementInfo> entityStlInfo = new HashMap<FinancialEntity, SettlementInfo>();
        Map<FinancialEntity, Terminal> entityTerminal = new HashMap<FinancialEntity, Terminal>();

        SettlementInfo info;
        if (!mainTerminal.equals(endPointTerminal)) {
            info = updateAmountOnSettleInfo(transaction, mainTerminal, mainAmount);
            entityStlInfo.put(mainTerminal.getOwner(), info);
            entityTerminal.put(mainTerminal.getOwner(), mainTerminal);
        } else {
            info = updateAmountOnSettleInfo(transaction, endPointTerminal, endPointAmount);
            entityStlInfo.put(endPointTerminal.getOwner(), info);
            entityTerminal.put(endPointTerminal.getOwner(), endPointTerminal);
        }

        if(Boolean.TRUE.equals(clearingProfile.getHasFee())){
            List<Object[]> fees = FeeService.getFees(transaction, clearingProfile, FeeType.DEBIT);
            Iterator<Object[]> iterator = fees.iterator();
            while (iterator.hasNext()) {
                Object[] entityFee = iterator.next();

                if (entityFee == null || entityFee[0] == null || entityFee[1] == null)
                    continue;

                FinancialEntity otherEntity = GeneralDao.Instance.load(FinancialEntity.class, (Long)entityFee[0]);
                if (otherEntity == null)
                    continue;

                info = entityStlInfo.get(otherEntity);
                if (info == null) {
                    if (otherEntity.equals(mainTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, mainTerminal);
                        info = updateAmountOnSettleInfo(transaction, mainTerminal, mainAmount);
                        entityTerminal.put(mainTerminal.getOwner(), mainTerminal);
                    }

                    else if (otherEntity.equals(endPointTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, endPointTerminal);
                        info = updateAmountOnSettleInfo(transaction, endPointTerminal, endPointAmount);
                        entityTerminal.put(endPointTerminal.getOwner(), endPointTerminal);
                    }

                    else {
                        info = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, DateTime.now(), transaction);
                        info.setTotalAmount(0L);
                    }
                }

                info.setTotalFee(-1 * (Long) entityFee[1]);
                entityStlInfo.put(otherEntity, info);
            }

            fees = FeeService.getFees(transaction, clearingProfile, FeeType.CREDIT);
            iterator = fees.iterator();
            while (iterator.hasNext()) {
                Object[] entityFee = iterator.next();

                if (entityFee == null || entityFee[0] == null || entityFee[1] == null)
                    continue;

                FinancialEntity otherEntity = GeneralDao.Instance.load(FinancialEntity.class, (Long)entityFee[0]);
                if (otherEntity == null)
                    continue;

                info = entityStlInfo.get(otherEntity);
                if (info == null) {
                    if (otherEntity.equals(mainTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, mainTerminal);
                        info = updateAmountOnSettleInfo(transaction, mainTerminal, mainAmount);
                        entityTerminal.put(mainTerminal.getOwner(), mainTerminal);
                    }

                    else if (otherEntity.equals(endPointTerminal.getOwner())) {
                        info = TransactionService.getRelatedSettleInfo(transaction, endPointTerminal);
                        info = updateAmountOnSettleInfo(transaction, endPointTerminal, endPointAmount);
                        entityTerminal.put(endPointTerminal.getOwner(), endPointTerminal);
                    }

                    else {
                        info = new SettlementInfo(SettledState.NOT_SETTLED, AccountingState.NOT_COUNTED, DateTime.now(), transaction);
                        info.setTotalAmount(0L);
                    }
                }

                Long oldFee = info.getTotalFee();
                if (oldFee == null)
                    oldFee = 0L;
                info.setTotalFee(oldFee + (Long) entityFee[1]);
                entityStlInfo.put(otherEntity, info);
            }
        }

        if (entityStlInfo != null && entityStlInfo.size() > 0) {
            for (FinancialEntity entity : entityStlInfo.keySet()) {
                Terminal terminal = entityTerminal.get(entity);
                Map<SettlementDataType, List<SettlementData>> types = settlementDatas.get(entity);
                if (types == null)
                    types = new HashMap<SettlementDataType, List<SettlementData>>();
                List<SettlementData> settlementDataList = types.get(type);
                if (settlementDataList == null)
                    settlementDataList = new ArrayList<SettlementData>();

                SettlementData settlementData = null;

//				for (SettlementData item: settlementDataList) {
//					if (settleDate.equals(item.getSettlementTime()))
//						settlementData = item;
//				}

                settlementData = generateSettlementDataForEntity(settlementData, entity, terminal, entityStlInfo.get(entity), clearingProfile, type, settleDate, true);
                settlementDataList.add(settlementData);
                types.put(type, settlementDataList);
                settlementDatas.put(entity, types);
            }
        }
    }

    public static SettlementData generateSettlementDataForEntity(SettlementData settlementData, FinancialEntity entity, Terminal terminal, SettlementInfo settlementInfo,
                                                                 ClearingProfile clearingProfile, SettlementDataType type, DateTime settleDate, Boolean forceNewSettleData) {

        if (settlementData == null)
            settlementData = generateSettlementData(entity, terminal, clearingProfile, type, settleDate, forceNewSettleData);

//		if (settlementData.isDone())
//			return settlementData;

        addSettleInfoToSettleData(settlementData, settlementInfo);

        return settlementData;
    }

    public static void generateReconcilementDataForAllEntities(List<SettlementInfo> settleInfos, SettlementDataType type, DateTime settleDate) {
        for (SettlementInfo oldInfo: settleInfos) {
            if (oldInfo != null && oldInfo.getSettlementData() != null) {
                SettlementData oldSettlementData = oldInfo.getSettlementData();
                SettlementData settlementData = generateSettlementData(oldSettlementData.getFinancialEntity(), oldSettlementData.getTerminal(), null, type, settleDate, false);

                SettlementInfo newStlInfo = new SettlementInfo();
                newStlInfo.setTotalAmount(oldInfo.getTotalAmount() != null ? -1*oldInfo.getTotalAmount() : 0L);
                newStlInfo.setTotalFee(oldInfo.getTotalFee() != null ? -1*oldInfo.getTotalFee() : 0L);
                newStlInfo.setTransaction(oldInfo.getTransaction());

                addSettleInfoToSettleData(settlementData, newStlInfo);
            }
        }
    }

    private static void addSettleInfoToSettleData(SettlementData settlementData, SettlementInfo settlementInfo) {

        try {
            if(settlementData.getId() != null) {
                logger.debug("Try to lock settlementData " + settlementData.getId());
                settlementData = (SettlementData) GeneralDao.Instance.optimizedSynchObject(settlementData);
            }
//			logger.debug("settlementData locked.... " + settlementData.getId());
        } catch (Exception e) {
            logger.error("Encounter an exception to lock settlementData", e);
        }


        Long oldFee = settlementData.getTotalFee();
        if (oldFee == null)
            settlementData.setTotalFee(0L);

        Long oldAmount = settlementData.getTotalAmount();
        if (oldAmount == null)
            settlementData.setTotalAmount(0L);

        Long oldTotalAmount = settlementData.getTotalSettlementAmount();
        if (oldTotalAmount == null)
            settlementData.setTotalSettlementAmount(0L);

        settlementData.setTotalFee(settlementData.getTotalFee() + settlementInfo.getTotalFee());
        settlementData.setTotalAmount(settlementData.getTotalAmount() + settlementInfo.getTotalAmount());
        settlementData.setNumTransaction(settlementData.getNumTransaction() + 1);

//		settlementData.addSettlementInfo(settlementInfo);
//		settlementData.addTransaction(settlementInfo.getTransaction());
        settlementData.addSettlementInfoNew(settlementInfo);

        settlementData.setTotalSettlementAmount(settlementData.getTotalAmount() + settlementData.getTotalFee());
//		if(settlementData.getId() == null){
        GeneralDao.Instance.saveOrUpdate(settlementData);
        GeneralDao.Instance.flush();
//		}else{
//			GeneralDao.Instance.saveOrUpdate(settlementData);
//		}

        settlementData.addTransactionNew(settlementInfo.getTransaction());

        GeneralDao.Instance.saveOrUpdate(settlementInfo);
    }

    private static SettlementData generateSettlementData(FinancialEntity entity, Terminal terminal, ClearingProfile clearingProfile,
                                                         SettlementDataType type, DateTime settleDate, Boolean forceNewSettleData) {

        SettlementData settlementData = null;

        if (!Boolean.TRUE.equals(forceNewSettleData))
            settlementData = findSettlementData(entity, terminal, clearingProfile, type, settleDate);

//		SettlementData settlementData = findSettlementData(entity, terminal, clearingProfile, type, settleDate);
        if (settlementData == null) {
            settlementData = new SettlementData(entity, terminal, clearingProfile, type, settleDate);
        }
        return settlementData;
    }

    public static List<AcqShetabReportRecord> getAcqShetabReportRecords(String instBin, MonthDayDate trxDate, DateTime cutoffFrom, DateTime cutoffTo,
                                                                        Long inDestBankId, Long notInDestBankId, int firstResult, int maxResults){
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "select i.ifxType as type, "
                + " i.trnType as trnType, "
                + " net.OrigDt as origDt, "
                + " net.Src_TrnSeqCntr as trnSeqCntr, "
                + " rq.CardAcctId.AppPAN as appPan, "
                + " net.DestBankId as destBankId, "
                + " net.TerminalId as terminalId, "
                + " rq.Auth_Amt as amount, "
                + " net.TerminalType as terminalType, "
                + " rq.secondAppPan as destAppPan, "
                + " net.BankId as bankId, "
                + " net.OrgIdNum as orgIdNum"
                + " from "
                + " Ifx as i "
                + " inner join i.eMVRqData as rq "
                + " inner join i.networkTrnInfo as net "
                + " inner join i.transaction as trx "
                + " inner join trx.destinationClearingInfo as clrInfo "
                + " where "
                + " i.endPointTerminal = :terminal "
                + " and i.receivedDtLong between :from and :to "
                + " and i.request = false "
                + " and i.postedDt = :date "
                + " and i.ifxType in (:ifxList) "
                + " and i.trnType in (:financialList) "
                + " and clrInfo.clearingState in (:clearingStateList) ";

        if (inDestBankId != null) {
            query += " and net.DestBankId = :destBankId ";
            params.put("destBankId", inDestBankId);
        }

        if (notInDestBankId != null) {
            query += " and net.DestBankId not in (:notDestBankId) ";
            params.put("notDestBankId", notInDestBankId);
        }

        query += " order by i.transaction asc";

        params.put("terminal", ProcessContext.get().getIssuerSwitchTerminal(instBin));
        params.put("date", trxDate);
        params.put("financialList", new ArrayList<TrnType>() {
            {
                add(TrnType.PURCHASE);
                add(TrnType.WITHDRAWAL);
                add(TrnType.BILLPAYMENT);
                add(TrnType.DECREMENTALTRANSFER);
                add(TrnType.INCREMENTALTRANSFER);
            }
        });

        params.put("clearingStateList", new ArrayList<ClearingState>() {
            {
                add(ClearingState.NOT_CLEARED);
                add(ClearingState.CLEARED);
                add(ClearingState.PARTIALLY_CLEARED);
                add(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
            }
        });

        params.put("ifxList", new ArrayList<IfxType>(){{
            add(IfxType.PURCHASE_RS);
            add(IfxType.WITHDRAWAL_RS);
            add(IfxType.BILL_PMT_RS);
            add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
            add(IfxType.TRANSFER_TO_ACCOUNT_RS);
            add(IfxType.SORUSH_REV_REPEAT_RS);
        }});

        params.put("from", cutoffFrom.getDateTimeLong());
        params.put("to", cutoffTo.getDateTimeLong());

        return GeneralDao.Instance.find(query, params, firstResult, maxResults, new AliasToBeanResultTransformer(AcqShetabReportRecord.class));

    }

    public static List<TotalShetabReportRecord> getTotalShetabReportRecordsIss(String instBin, MonthDayDate trxDate, DateTime cutoffFrom, DateTime cutoffTo){
        Institution myInstitution = ProcessContext.get().getMyInstitution();
        String query = "select "
                + "1-ceil(mod(bankid,"+myInstitution.getBin()+")/999999) AS isShetab, "
                + "ifx.networkTrnInfo.TerminalType AS terminalType, "
                + "ifx.trnType AS trnType, "
                + "ifx.ifxType AS ifxType, "
                + "sum(ifx.eMVRqData.Auth_Amt) AS transactionAmount "
                + "from "
                + "Ifx ifx where "
                + " ifx.request = 0 "
                + " and ifx.ifxType in (:ifxList) "
                + " and ifx.receivedDtLong between :from and :to "
                + " and ifx.trnType in (:financialList) "
                + " and ifx.endPointTerminal = :shetabAcqTerminal "
                + " and ifx.transaction.sourceClearingInfo.clearingState in (:clearingStateList) "
                + " and ifx.settleDt = :date ";

        if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
            query += " and ifx.networkTrnInfo.DestBankId = 502229 ";
        }
        query += " group by ifx.endPointTerminal, ifx.networkTrnInfo.TerminalType , ifx.trnType, ifx.ifxType," +
                "1-ceil(mod(bankid,"+myInstitution.getBin()+")/999999)";

        query += " order by ifx.networkTrnInfo.TerminalType, ifx.trnType, ifx.ifxType, " +
                "1-ceil(mod(bankid,"+myInstitution.getBin()+")/999999)";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("ifxList", new ArrayList<IfxType>(){{
            add(IfxType.PURCHASE_RS);
            add(IfxType.WITHDRAWAL_RS);
            add(IfxType.BILL_PMT_RS);
            add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
            add(IfxType.TRANSFER_TO_ACCOUNT_RS);
            add(IfxType.SORUSH_REV_REPEAT_RS);

        }});

        params.put("from", cutoffFrom.getDateTimeLong());
        params.put("to", cutoffTo.getDateTimeLong());

        params.put("shetabAcqTerminal", ProcessContext.get().getAcquierSwitchTerminal(instBin));

        params.put("date", trxDate);
        params.put("financialList", new ArrayList<TrnType>(){{
            add(TrnType.PURCHASE);
            add(TrnType.WITHDRAWAL);
            add(TrnType.BILLPAYMENT);
            add(TrnType.DECREMENTALTRANSFER);
            add(TrnType.INCREMENTALTRANSFER);
        }});

        params.put("clearingStateList", new ArrayList<ClearingState>(){{
            add(ClearingState.NOT_CLEARED);
            add(ClearingState.CLEARED);
            add(ClearingState.PARTIALLY_CLEARED);
            add(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
        }});

        return GeneralDao.Instance.find(query, params, new AliasToBeanResultTransformer(TotalShetabReportRecord.class));
    }

    public static List<TotalShetabReportRecord> getTotalShetabReportRecordsAcq(String instBin, MonthDayDate trxDate, DateTime cutoffFrom, DateTime cutoffTo){
        Institution myInstitution = ProcessContext.get().getMyInstitution();
        if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole()))
            return null;

        String query = "select "
                + "1-ceil(mod(bankid,"+myInstitution.getBin()+")/999999) AS isShetab, "
                + "ifx.networkTrnInfo.TerminalType AS terminalType, "
                + "ifx.trnType AS trnType, "
                + "ifx.ifxType AS ifxType, "
                + "sum(ifx.eMVRqData.Auth_Amt) AS transactionAmount "
                + "from "
                + "Ifx ifx where "
                + " ifx.request = 0 "
                + " and ifx.ifxType in (:ifxList) "
                + " and ifx.receivedDtLong between :from and :to "
                + " and ifx.trnType in (:financialList) "
                + " and ifx.endPointTerminal = :shetabIssTerminal "
                + " and ifx.transaction.destinationClearingInfo.clearingState in (:clearingStateList)"
                + " and ifx.postedDt = :date "
                + " group by ifx.endPointTerminal, ifx.networkTrnInfo.TerminalType , ifx.trnType, ifx.ifxType, " +
                "1-ceil(mod(bankid,"+myInstitution.getBin()+")/999999)"
                + " order by ifx.networkTrnInfo.TerminalType, ifx.trnType, ifx.ifxType, "
                + " 1-ceil(mod(bankid,"+myInstitution.getBin()+")/999999)";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("shetabIssTerminal",ProcessContext.get().getIssuerSwitchTerminal(instBin));

        params.put("date", trxDate);
        params.put("ifxList", new ArrayList<IfxType>(){{
            add(IfxType.PURCHASE_RS);
            add(IfxType.WITHDRAWAL_RS);
            add(IfxType.BILL_PMT_RS);
            add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
            add(IfxType.TRANSFER_TO_ACCOUNT_RS);
            add(IfxType.SORUSH_REV_REPEAT_RS);

        }});

        params.put("from", cutoffFrom.getDateTimeLong());
        params.put("to", cutoffTo.getDateTimeLong());
        params.put("financialList", new ArrayList<TrnType>(){{
            add(TrnType.PURCHASE);
            add(TrnType.WITHDRAWAL);
            add(TrnType.BILLPAYMENT);
            add(TrnType.DECREMENTALTRANSFER);
            add(TrnType.INCREMENTALTRANSFER);
        }});

        params.put("clearingStateList", new ArrayList<ClearingState>(){{
            add(ClearingState.NOT_CLEARED);
            add(ClearingState.CLEARED);
            add(ClearingState.PARTIALLY_CLEARED);
            add(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
        }});

        return GeneralDao.Instance.find(query, params, new AliasToBeanResultTransformer(TotalShetabReportRecord.class));
    }

    public static List<IssShetabReportRecord> getIssShetabReportRecords(String instBin, MonthDayDate trxDate, DateTime cutoffFrom, DateTime cutoffTo,
                                                                        int firstResult, int maxResults, boolean reversalTrx){
        Institution myInstitution = ProcessContext.get().getMyInstitution();
        String query = "select i.ifxType as type, "
                + " i.trnType as trnType, "
                + " net.OrigDt as origDt, "
                + " net.Src_TrnSeqCntr as trnSeqCntr, ";

        if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
            query += " rq.CardAcctId.actualAppPAN as appPan, "
                    + " rq.actualSecondAppPan as destAppPan, ";

        } else {
            query += " rq.CardAcctId.AppPAN as appPan, "
                    + " rq.secondAppPan as destAppPan, ";
        }
        query += " net.DestBankId as destBankId, "
                + " net.TerminalId as terminalId, "
                + " rq.Auth_Amt as amount, "
                + " net.TerminalType as terminalType, "
                + " rs.totalFeeAmt as feeAmount, "
                + " net.BankId as bankId, "
                + " rq.billPaymentData.billOrgType as billOrgType"
                + " from "
                + " Ifx as i "
                + " inner join i.eMVRsData as rs "
                + " inner join i.eMVRqData as rq "
                + " inner join i.networkTrnInfo as net "
                + " inner join i.transaction as trx ";
        if (!reversalTrx)
            query += " inner join trx.sourceClearingInfo as clrInfo ";
        query +=  " where "
                + " i.endPointTerminal = :terminal"
                + " and i.receivedDtLong between :from and :to "
                + " and i.settleDt = :date "
                + " and (i.trnType in (:financialList) "
                +		" or (net.TerminalType in (:term_typeList) and i.trnType =:balance)"
                +		") ";

        if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(myInstitution.getRole())) {
            query += " and net.DestBankId = 502229 ";
        }

        Map<String, Object> params = new HashMap<String, Object>();

        if (reversalTrx){
//			query += " and i.ifxType in "+ IfxType.getRevRsOrdinalsCollectionString();
            String string = IfxType.strRevRsOrdinals;
            string = string.replaceAll(IfxType.SORUSH_REV_REPEAT_RS.getType() + " ,", "");
            string = string.replaceAll(" ," + IfxType.SORUSH_REV_REPEAT_RS.getType(), "");
            query += " and i.ifxType in "+ string;
            query += " and rs.RsCode = :success ";
            params.put("success", ISOResponseCodes.APPROVED);
        }else{
            query += " and i.ifxType in (:ifxList) ";
            query += " and i.request = false ";
            query += " and clrInfo.clearingState in (:clearingStateList)";
            params.put("clearingStateList", new ArrayList<ClearingState>() {
                {
                    add(ClearingState.NOT_CLEARED);
                    add(ClearingState.CLEARED);
                    add(ClearingState.PARTIALLY_CLEARED);
                    add(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
                }
            });

            params.put("ifxList", new ArrayList<IfxType>() {
                {
                    add(IfxType.PURCHASE_RS);
                    add(IfxType.WITHDRAWAL_RS);
                    add(IfxType.BILL_PMT_RS);
                    add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
                    add(IfxType.TRANSFER_TO_ACCOUNT_RS);
                    add(IfxType.SORUSH_REV_REPEAT_RS);

                    add(IfxType.BAL_INQ_RS);
                }
            });
        }

        query +=" order by i.transaction asc";

        params.put("from", cutoffFrom.getDateTimeLong());
        params.put("to", cutoffTo.getDateTimeLong());
//		params.put("terminal", ProcessContext.get().getAcquierSwitchTerminal(9000L));
        params.put("terminal", ProcessContext.get().getAcquierSwitchTerminal(instBin));
        params.put("date", trxDate);
        params.put("financialList", new ArrayList<TrnType>() {
            {
                add(TrnType.PURCHASE);
                add(TrnType.WITHDRAWAL);
                add(TrnType.BILLPAYMENT);
                add(TrnType.DECREMENTALTRANSFER);
                add(TrnType.INCREMENTALTRANSFER);
            }
        });
        params.put("balance", TrnType.BALANCEINQUIRY);
        params.put("term_typeList", new ArrayList<TerminalType>() {
            {
                add(TerminalType.ATM);
                add(TerminalType.PINPAD);
                add(TerminalType.KIOSK_CARD_PRESENT);
                add(TerminalType.POS);
                add(TerminalType.VRU);
                add(TerminalType.MOBILE);
            }
        });

        return GeneralDao.Instance.find(query, params, firstResult, maxResults, new AliasToBeanResultTransformer(IssShetabReportRecord.class));
    }

    public static List<IssShetabReportRecord> getIssShetabReportRecordsTransferFrom(String instBin, MonthDayDate trxDate, DateTime cutoffFrom, DateTime cutoffTo,
                                                                                    int firstResult, int maxResults){
        if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())) {
            return null;
        }

        String query = "select i.ifxType as type, "
                + " i.trnType as trnType, "
                + " net.OrigDt as origDt, "
                + " net.Src_TrnSeqCntr as trnSeqCntr, "
                + " rq.CardAcctId.AppPAN as destAppPan, "
                + " net.DestBankId as destBankId, "
                + " net.TerminalId as terminalId, "
                + " rq.Auth_Amt as amount, "
                + " net.TerminalType as terminalType, "
                + " rs.totalFeeAmt as feeAmount, "
                + " rq.secondAppPan as appPan, "
                + " net.BankId as bankId "
                + " from "
                + " Ifx as i "
                + " inner join i.eMVRsData as rs "
                + " inner join i.eMVRqData as rq "
                + " inner join i.networkTrnInfo as net "
                + " inner join i.transaction as trx "
                + " inner join trx.destinationClearingInfo as clrInfo "
                + " where "
                + " i.endPointTerminal = :terminal"
                + " and i.receivedDtLong between :from and :to "
                + " and i.postedDt = :date "
                + " and i.trnType in (:financialList) "
                + " and i.ifxType in (:ifxList) "
                + " and i.request = false "
                + " and clrInfo.clearingState in (:clearingStateList)"
                + " order by i.transaction asc";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("clearingStateList", new ArrayList<ClearingState>() {
            {
                add(ClearingState.NOT_CLEARED);
                add(ClearingState.CLEARED);
                add(ClearingState.PARTIALLY_CLEARED);
                add(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
            }
        });
//		params.put("terminal", ProcessContext.get().getIssuerSwitchTerminal(9000L));
        params.put("terminal", ProcessContext.get().getIssuerSwitchTerminal(instBin));
        params.put("date", trxDate);
        params.put("from", cutoffFrom.getDateTimeLong());
        params.put("to", cutoffTo.getDateTimeLong());
        params.put("financialList", new ArrayList<TrnType>() {
            {
                add(TrnType.DECREMENTALTRANSFER);
            }
        });
        params.put("ifxList", new ArrayList<IfxType>() {
            {
                add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
            }
        });


        return GeneralDao.Instance.find(query, params, firstResult, maxResults, new AliasToBeanResultTransformer(IssShetabReportRecord.class));
    }

    public static List<IssShetabReportRecord> getIssShetabReportRecordsTransferTo(String instBin, MonthDayDate trxDate, DateTime cutoffFrom, DateTime cutoffTo,
                                                                                  int firstResult, int maxResults){
        if (FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())) {
            return null;
        }

        String query = "select "
                + "i.ifxType as type, "
                + " i.trnType as trnType, "
                + " net.OrigDt as origDt, "
                + " net.Src_TrnSeqCntr as trnSeqCntr, "
                + " rq.CardAcctId.AppPAN as destAppPan, "
                + " net.DestBankId as destBankId, "
                + " net.TerminalId as terminalId, "
                + " rq.Auth_Amt as amount, "
                + " net.TerminalType as terminalType, "
                + " rs2.totalFeeAmt as feeAmount, "
                + " rq.secondAppPan as appPan, "
                + " net.BankId as bankId "
                + " from "
                + " Ifx as i "
                + ",Ifx as i2 "
                + " inner join i.eMVRsData as rs "
                + " inner join i.eMVRqData as rq "
                + " inner join i.networkTrnInfo as net "
                + " inner join i.transaction as trx "
                + " inner join i2.eMVRsData as rs2 "
                + " inner join trx.destinationClearingInfo as clrInfo "
                + " where "
                + " i2.transaction = trx.firstTransaction "
                + " and i.receivedDtLong between :from and :to "
                + " and i.endPointTerminal = :terminal"
                + " and i.postedDt = :date "
                + " and i.trnType in (:financialList) "
                + " and i.ifxType in (:ifxList) "
                + " and i.request = false "
                + " and clrInfo.clearingState in (:clearingStateList)"
                + " order by i.transaction asc";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("clearingStateList", new ArrayList<ClearingState>() {
            {
                add(ClearingState.NOT_CLEARED);
                add(ClearingState.CLEARED);
                add(ClearingState.PARTIALLY_CLEARED);
                add(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
            }
        });
//		params.put("terminal", ProcessContext.get().getIssuerSwitchTerminal(9000L));
        params.put("terminal", ProcessContext.get().getIssuerSwitchTerminal(instBin));
        params.put("date", trxDate);
        params.put("from", cutoffFrom.getDateTimeLong());
        params.put("to", cutoffTo.getDateTimeLong());
        params.put("financialList", new ArrayList<TrnType>() {
            {
                add(TrnType.INCREMENTALTRANSFER);
            }
        });
        params.put("ifxList", new ArrayList<IfxType>() {
            {
                add(IfxType.TRANSFER_TO_ACCOUNT_RS);
                add(IfxType.SORUSH_REV_REPEAT_RS);

            }
        });

        return GeneralDao.Instance.find(query, params, firstResult, maxResults, new AliasToBeanResultTransformer(IssShetabReportRecord.class));
    }

    public static List<ReportRecord> getSettlementDataReportRecords(SettlementData settlementData, int firstResult, int maxResults){
        String query = "select i.ifxType as ifxType, "
                + " i.receivedDt as recievedDt, "
                + " i.networkTrnInfo.Src_TrnSeqCntr as trnSeqCntr, "
                + " i.networkTrnInfo.TerminalId as terminalId, "
                + " i.networkTrnInfo.BankId as bankId, "
                + " i.eMVRqData.CardAcctId.AppPAN as appPAN, "
                + " i.eMVRqData.Auth_Amt as auth_Amt, "
                + " si.totalAmount as totalAmount, "
                + " si.totalFee as totalFee "
                + " from SettlementData s, SettlementInfo si "
                + ", "
//			+ " Message as m inner join m.ifx as i "
                + " Ifx as i "
//			+ " where m.transaction in elements (s.transactions) "
                + " where i.transaction in elements (s.transactions) "
                + " and si.settlementData = :stlData "
//			+ " and si.transaction = m.transaction "
                + " and si.transaction = i.transaction "
                + " and i.ifxDirection = :direction "
                + " and s = :stlData "

//			+ " order by i.receivedDt.dayDate asc, i.receivedDt.dayTime asc";
                + " order by i.receivedDtLong asc ";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("stlData", settlementData);
        params.put("direction", IfxDirection.OUTGOING);

        return GeneralDao.Instance.find(query, params, firstResult, maxResults, new AliasToBeanResultTransformer(ReportRecord.class));

    }

    public static List<ReportRecord> getBillPaymentThirdPartyReportRecords(SettlementData settlementData, int firstResult, int maxResults){
        String query = "select "
                + " i.receivedDt as recievedDt, "
                + " i.networkTrnInfo.Src_TrnSeqCntr as trnSeqCntr, "
                + " i.networkTrnInfo.TerminalType as terminalType, "
                + " i.eMVRqData.billPaymentData.billID as billID, "
                + " i.eMVRqData.billPaymentData.billPaymentID as billPaymentID, "
                + " i.eMVRqData.Auth_Amt as auth_Amt "
                + " from SettlementData s "
                + ", "
//			+ " Message as m inner join m.ifx as i "
//			+ " where m.transaction in elements(s.transactions) "
                + " Ifx as i "
                + " where i.transaction in elements(s.transactions) "
                + " and i.ifxDirection = :direction "
                + " and s = :stlData "

//			+ " order by i.receivedDt.dayDate asc, i.receivedDt.dayTime asc";
                + " order by i.receivedDtLong asc ";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("stlData", settlementData);
        params.put("direction", IfxDirection.OUTGOING);

        return GeneralDao.Instance.find(query, params, firstResult, maxResults, new AliasToBeanResultTransformer(BillPayReportRecord.class));
    }

    public static List<ReportRecord> getChargeThirdPartyReportRecords(SettlementData settlementData, int firstResult, int maxResults){
        String query = "select "
                + " i.receivedDt as recievedDt, "
                + " i.networkTrnInfo.Src_TrnSeqCntr as trnSeqCntr, "
                + " i.networkTrnInfo.TerminalType as terminalType, "
                + " i.chargeData.charge.cardSerialNo as cardSerialNo, "
                + " i.chargeData.charge.year as year, "
                + " i.chargeData.charge.fileId as fileId, "
                + " i.chargeData.charge.credit as auth_Amt, "
                + " i.eMVRqData.CardAcctId.AppPAN as appPAN, "
                + " i.networkTrnInfo.TerminalId as terminalId, "
                + " i.endPointTerminalCode as endPointTerminalCode "
//			+ " i.eMVRqData.Auth_Amt as auth_Amt "
                + " from SettlementData s, "
                + " Ifx as i "
                + " where i.transaction in elements(s.transactions) "
                + " and i.ifxDirection = :direction "
                + " and s = :stlData "
                + " order by i.receivedDtLong asc ";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("stlData", settlementData);
        params.put("direction", IfxDirection.OUTGOING);

        return GeneralDao.Instance.find(query, params, firstResult, maxResults, new AliasToBeanResultTransformer(ChargeReportRecord.class));
    }

    public static List<SettlementState> findSettlementState(ClearingProfile clearingProfile, SettlementStateType state) {
        String query = "from " + SettlementState.class.getName() + " s " ;
        Map<String, Object> param = new HashMap<String, Object>();

        if (clearingProfile != null) {
            query += " where s.clearingProfile = :clearingProfile ";
            param.put("clearingProfile", clearingProfile);
        } else {
            query += " where s.clearingProfile is null ";
        }

        if (state == null){
            query += " and s.state is null ";
        }else {
            query += " and s.state = :state ";
            param.put("state", state);
        }
        return GeneralDao.Instance.find(query, param);
    }

    @NotUsed
    public static List<SettlementData> findSettlementData(ClearingProfile clearingProfile, SettlementStateType state) {
        String query = "from " + SettlementData.class.getName() + " s "
                + " where s.clearingProfile = :clearingProfile "
                ;

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("clearingProfile", clearingProfile);

        if (state == null)
            query += " and s.settlementState.state is null ";
        else {
            query += " and s.settlementState.state = :state ";
            param.put("state", state);
        }

        query += " order by s.id asc ";
        return GeneralDao.Instance.find(query, param);
    }

    public static Map<Terminal, List<SettlementData>> getSettlementDatas(List<SettlementData> settlementDatas) {
        Map<Terminal, List<SettlementData>> result = new HashMap<Terminal, List<SettlementData>>();
        for (SettlementData settlementData : settlementDatas) {
            Terminal terminal= settlementData.getTerminal();
            if (terminal == null)
                continue;
            List<SettlementData> list = result.get(terminal);
            if (list == null)
                list = new ArrayList<SettlementData>();
            list.add(settlementData);
            result.put(terminal, list);
        }
        return result;
    }

    public static List<SettlementData> findAllNotSettledSettlementDataUntilTime(List<Terminal> terminals, ClearingProfile clearingProfile,
                                                                                DateTime stlDate, Core core ) {


        List<SettlementData> result = new ArrayList<SettlementData>();

        List<Terminal> termForQuery = new ArrayList<Terminal>();
        int counter = 0;

        if (terminals == null || terminals.size() == 0 ) {
            return findAllNotSettledSettlementDataUntilTimeQuery(terminals, clearingProfile, stlDate, core);
        }

        for (int i = 0; i < terminals.size(); i++) {
            termForQuery.add(terminals.get(i));
            counter++;
            if (counter == 500 || i == terminals.size() - 1) {

                result.addAll(findAllNotSettledSettlementDataUntilTimeQuery(termForQuery, clearingProfile, stlDate, core));

                counter = 0;
                termForQuery = new ArrayList<Terminal>();

            }
        }

        return result;

    }

    private static List<SettlementData> findAllNotSettledSettlementDataUntilTimeQuery(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime stlDate, Core core) {
        Map<String, Object> param = new HashMap<String, Object>();

        String query = "select s from " + SettlementData.class.getName() + " s ";

        if (core != null) {
            query += " inner join s.financialEntity fe "
                    + " where "
                    + " fe.account.core = :core and ";
            param.put("core", core);

        } else {
            query +=  " where ";

        }

        String query2 =
                " (s.totalAmount != 0 or s.totalFee != 0 or s.totalSettlementAmount != 0 or s.numTransaction != 0)" +
                        " and s.settlementState is null ";

        if (clearingProfile != null) {
            query2 += " and s.clearingProfile = :clearingProfile ";
            param.put("clearingProfile", clearingProfile);
            if(clearingProfile.getSettleGuaranteeDay() != null  && clearingProfile.getSettleGuaranteeDay() < 0){
                query2 += " and s.settlementTimeLong >= :untilDate2 ";
                param.put("untilDate2", new DateTime(stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()),
                        new DayTime(0, 0, 0)).getDateTimeLong());
                // query += " and s.settlementTime.dayDate >= :untilDate2 ";
                // param.put("untilDate2", stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()));
            }
        } else {
            query2 += " and s.clearingProfile is null ";
        }

        if (terminals != null && terminals.size() > 0) {
            query2 += " and s.terminal in (:termList) ";
            param.put("termList", terminals);
        }

//		 query2 += " and (s.settlementTime.dayDate = :stlDate and s.settlementTime.dayTime <= :stlTime " +
//				 " or s.settlementTime.dayDate < :stlDate)";
        query2 += " and (s.settlementTimeLong <= :stlDate)";

        query2 += " order by s.id asc ";

        param.put("stlDate", stlDate.getDateTimeLong());
//		 param.put("stlDate", stlDate.getDayDate());
//		 param.put("stlTime", stlDate.getDayTime());

        query += query2;

        List<SettlementData> result = GeneralDao.Instance.find(query, param);

        if (core != null) {
            query = "select s from " + SettlementData.class.getName() + " s ";
            query += " inner join s.financialEntity fe "
                    + " , Shop sh inner join sh.owner me "
                    + " where "
                    + " sh.code = fe.code and sh.account.core is null and me.account.core = :core and ";


            query += query2;

            if (result == null)
                result = new ArrayList<SettlementData>();
            List<SettlementData> result1 = GeneralDao.Instance.find(query, param);
            if (result1 != null && result1.size() > 0) {
                logger.debug(result1.size() + " settlementDatas find with shop's core Null");
                result.addAll(result1);
            } else {
                logger.debug("0 settlementData find with shop's core Null");
            }
        }

        return result;
    }


    public static List<SettlementData> findAllNotSettledSettlementDataUntilTime(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime stlDate, Integer firstResult, Integer maxResults) {

        List<SettlementData> result = new ArrayList<SettlementData>();

        List<Terminal> termForQuery = new ArrayList<Terminal>();
        int counter = 0;

        if (terminals == null || terminals.size() == 0 ) {
            return findAllNotSettledSettlementDataUntilTimeQuery(terminals, clearingProfile, stlDate, firstResult, maxResults);
        }

        for (int i = 0; i < terminals.size(); i++) {
            termForQuery.add(terminals.get(i));
            counter++;
            if (counter == 500 || i == terminals.size() - 1) {

                result.addAll(findAllNotSettledSettlementDataUntilTimeQuery(termForQuery, clearingProfile, stlDate, firstResult, maxResults));

                counter = 0;
                termForQuery = new ArrayList<Terminal>();

            }
        }

        return result;
    }

    private static List<SettlementData> findAllNotSettledSettlementDataUntilTimeQuery(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime stlDate, Integer firstResult, Integer maxResults) {

        Map<String, Object> param = new HashMap<String, Object>();
        String query = "from " + SettlementData.class.getName() + " s " +
                " where " +
                " (s.totalAmount != 0 or s.totalFee != 0 or s.totalSettlementAmount != 0 or s.numTransaction != 0)" +
                " and s.settlementState is null ";

        if (clearingProfile != null) {
            query += " and s.clearingProfile = :clearingProfile ";
            param.put("clearingProfile", clearingProfile);
            if(clearingProfile.getSettleGuaranteeDay() != null  && clearingProfile.getSettleGuaranteeDay() < 0){
//				 query += " and s.settlementTime.dayDate >= :fromDate ";
                query += " and s.settlementTimeLong >= :fromDate ";
                DayDate fromDate = stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay());
                Long fromDateLong = new DateTime(fromDate, new DayTime(0, 0, 0)).getDateTimeLong();
                param.put("fromDate", fromDateLong);
//				 DayDate fromDate = stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay());
//				 param.put("fromDate", fromDate);				 
            }
        } else {
            query += " and s.clearingProfile is null ";
        }

        if (terminals != null && terminals.size() > 0) {
            query += " and s.terminal in (:termList) ";
            param.put("termList", terminals);
        }

//		 query += " and (s.settlementTime.dayDate = :stlDate " + " and s.settlementTime.dayTime <= :stlTime " +
//		 " or s.settlementTime.dayDate < :stlDate)";

        query += " and (s.settlementTimeLong <= :stlDate)";

        query += " order by s.id asc ";
//		 query += " order by s.settlementTime.dayDate asc, s.settlementTime.dayTime asc";

        param.put("stlDate", stlDate.getDateTimeLong());
//		 param.put("stlDate", stlDate.getDayDate());
//		 param.put("stlTime", stlDate.getDayTime());

        if (firstResult == null || maxResults == null)
            return GeneralDao.Instance.find(query, param);
        else
            return GeneralDao.Instance.find(query, param, firstResult.intValue(), maxResults.intValue());
    }

    public static List<SettlementData> findAllNotSettledATMSettlementDataUntilTime(ClearingProfile clearingProfile, DateTime stlDate) {

        Map<String, Object> param = new HashMap<String, Object>();
        String query = "from " + SettlementData.class.getName() + " s " +
                " where "
//		 + "(s.settlementTime.dayDate = :stlDate " 
//		 + " and s.settlementTime.dayTime <= :stlTime "
//		 + " or s.settlementTime.dayDate < :stlDate)"
                + " s.settlementTimeLong <= :stlDate "
                + " and (s.totalAmount != 0 or s.totalFee != 0 or s.totalSettlementAmount != 0 or s.numTransaction != 0)"
                + " and s.documentNumber is null "
//		 + " and s.settlementReport is null "
                ;

        if (clearingProfile != null) {
            query += " and s.clearingProfile = :clearingProfile ";
            param.put("clearingProfile", clearingProfile);
            if (clearingProfile.getSettleGuaranteeDay() != null && clearingProfile.getSettleGuaranteeDay() < 0) {
                query += " and s.settlementTimeLong >= :untilDate2 ";
                param.put("untilDate2", new DateTime(stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()),
                        new DayTime(0, 0, 0)).getDateTimeLong());
            }
        } else {
            query += " and s.clearingProfile is null ";
        }
        query += " order by s.id asc ";
//		 query += " order by s.settlementTime.dayDate asc, s.settlementTime.dayTime asc, id asc";

        param.put("stlDate", stlDate.getDateTimeLong());
//		 param.put("stlDate", stlDate.getDayDate());
//		 param.put("stlTime", stlDate.getDayTime());
        return GeneralDao.Instance.find(query, param);
    }

    public static List<SettlementData> findThirdPartySettlementDatasOfSettlementStates(List<SettlementState> settlementStates) {
        String queryString = "select sd from " +
                " SettlementData sd, Organization o " +
                " where sd.financialEntityId = o.code " +
                " and sd.settlementState in (:stlStates)";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stlStates", settlementStates);

        List<SettlementData> settlementDatas = GeneralDao.Instance.find(queryString, parameters);
        return settlementDatas;
    }

    public static List<SettlementData> findAllNotSettledMCISettlementDataUntilTime(ClearingProfile clearingProfile, DateTime stlDate) {

        Map<String, Object> param = new HashMap<String, Object>();
        String query = "from " + SettlementData.class.getName() + " s " +
                " where " +
//     	 		"(s.settlementTime.dayDate = :stlDate and s.settlementTime.dayTime <= :stlTime " +
//     	 		" or s.settlementTime.dayDate < :stlDate)" +
                "(s.settlementTimeLong <= :stlDate)" +
                " and (s.totalAmount != 0 or s.totalFee != 0 or s.totalSettlementAmount != 0 or s.numTransaction != 0)" +
                " and s.settlementState is null " +
                " and (" +
                " select r.id from " + SettlementDataReport.class.getName() + " r "+
                " where r.settlementData = s.id " +
                " and r.type = :thirdParty " +
                " ) is null";

        if (clearingProfile != null) {
            query += " and s.clearingProfile = :clearingProfile ";
            param.put("clearingProfile", clearingProfile);

            query += " and s.settlementTimeLong >= :untilDate2 ";
            param.put("untilDate2", new DateTime(stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()),
                    new DayTime(0, 0, 0)).getDateTimeLong());

//        	  query += " and s.settlementTime.dayDate > :untilDate2 ";
//        	  param.put("untilDate2", stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()));        	  
        } else {
            query += " and s.clearingProfile is null ";
        }

        query += " order by s.id asc ";
//          query += " order by s.settlementTime.dayDate asc, s.settlementTime.dayTime asc";

        param.put("stlDate", stlDate.getDateTimeLong());
//          param.put("stlDate", stlDate.getDayDate());
//          param.put("stlTime", stlDate.getDayTime());
        param.put("thirdParty", SettlementDataReportType.THIRDPARTY_REPORT);

        return GeneralDao.Instance.find(query, param);
    }

    public static List<SettlementData> findAllNotSettledATMSettlementDataUntilTime(ClearingProfile clearingProfile, DateTime stlDate, List<Terminal> terminals) {
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "from " + SettlementData.class.getName() + " s " +
                " where "
                + " (s.totalAmount != 0 or s.totalFee != 0 or s.totalSettlementAmount != 0 or s.numTransaction != 0)"
                + " and s.documentNumber is null ";
//		 + " and s.settlementReport is null "

        if (terminals != null) {
            query += " and s.terminal in (:terminals) ";
            params.put("terminals", terminals);
        }

        if (clearingProfile != null) {
            query += " and s.clearingProfile = " + clearingProfile.getId();
//			 query += " and s.clearingProfile = :clearingProfile ";
//			 params.put("clearingProfile", clearingProfile);
            if(clearingProfile.getSettleGuaranteeDay() != null  && clearingProfile.getSettleGuaranteeDay() < 0){
                query += " and s.settlementTimeLong >= :fromDate ";
                params.put("fromDate", new DateTime(stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()),
                        new DayTime(0, 0, 0)).getDateTimeLong());
//				 query += " and s.settlementTime.dayDate >= :fromDate ";
//				 DayDate fromDate = stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay());
//				 params.put("fromDate", fromDate);				 
            }
        } else {
            query += " and s.clearingProfile is null ";
        }

//		 query += " and (s.settlementTime.dayDate = :stlDate " 
//			 	+ " and s.settlementTime.dayTime <= :stlTime "
//			 	+ " or s.settlementTime.dayDate < :stlDate)";
        query += " and (s.settlementTimeLong <= :stlDate)";

        query += " order by s.id asc ";
//		 query += " order by s.settlementTime.dayDate asc, s.settlementTime.dayTime asc, id asc";

        params.put("stlDate", stlDate.getDateTimeLong());
//		 params.put("stlDate", stlDate.getDayDate());
//		 params.put("stlTime", stlDate.getDayTime());
        return GeneralDao.Instance.find(query, params);
    }

    public static List<SettlementData> findAllNotSettledATMSettlementDataUntilTime(List<SettlementState> settlementStates) {

        Map<String, Object> param = new HashMap<String, Object>();
        String query = "from " + SettlementData.class.getName() + " s " +
                " where " +
                " s.settlementState in (:settlementStates) " +
                " and s.documentNumber is null " +
                " and (s.totalAmount != 0 or s.totalFee != 0 or s.totalSettlementAmount != 0 or s.numTransaction != 0)";

        query += " order by s.id asc ";
//		 query += " order by s.settlementTime.dayDate asc, s.settlementTime.dayTime asc";

        param.put("settlementStates", settlementStates);
        return GeneralDao.Instance.find(query, param);
    }

    public static List<SettlementData> findAllNotSettledOnlineSettlementDataUntilTime(List<Terminal> terminals, ClearingProfile clearingProfile, DateTime stlDate) {

        Map<String, Object> param = new HashMap<String, Object>();
        String query = "from " + SettlementData.class.getName() + " s " +
                " where " +
                " s.settlementTimeLong <= :stlDate "+
//		 "(s.settlementTime.dayDate = :stlDate " + " and s.settlementTime.dayTime <= :stlTime " +
//		 " or s.settlementTime.dayDate < :stlDate)" +
                " and (s.totalAmount != 0 or s.totalFee != 0 or s.totalSettlementAmount != 0 or s.numTransaction != 0)" +
                " and not (s.totalFee != 0 and s.totalAmount = 0)" +
                " and s.documentNumber is null "
                + " and s.terminal in (:terminals) ";

        if (clearingProfile != null) {
            query += " and s.clearingProfile = :clearingProfile ";
            param.put("clearingProfile", clearingProfile);

            if (clearingProfile.getSettleGuaranteeDay() != null && clearingProfile.getSettleGuaranteeDay() < 0) {
                query += " and s.settlementTimeLong >= :untilDate2 ";
                param.put("untilDate2", new DateTime(stlDate.getDayDate().nextDay(clearingProfile.getSettleGuaranteeDay()),
                        new DayTime(0, 0, 0)).getDateTimeLong());
            }

        } else {
            query += " and s.clearingProfile is null ";
        }
        query += " order by s.id asc ";
//		 query += " order by s.settlementTime.dayDate asc, s.settlementTime.dayTime asc";

        param.put("stlDate", stlDate.getDateTimeLong());
//		 param.put("stlDate", stlDate.getDayDate());
//		 param.put("stlTime", stlDate.getDayTime());
        param.put("terminals", terminals);

        return GeneralDao.Instance.find(query, param);
    }

    public static SettlementState generateSettlementState(ClearingProfile clearingProfile, Core core, String desc) {
        desc = "پرداخت " + desc;
        if (clearingProfile != null)
            desc += " الگوی تسويه حساب " + clearingProfile.getName();
        SettlementState settlementState = new SettlementState(clearingProfile, core, desc);
        GeneralDao.Instance.saveOrUpdate(settlementState);
        return settlementState;
    }

    @NotUsed
    public int updateSettlementState(List<SettlementState> states) {
        String query = "update " + SettlementState.class.getName() + " s set "
                +"s.settlingUser.id = :user ,"
                + " s.settlementDate.dayDate = :date ,"
                + " s.settlementDate.dayTime = :time "
                +" where s in (:states)";
        Map<String, Object> params = new HashMap<String, Object>();
//		params.put("user", GlobalContext.getInstance().getSwitchUser().getId());
        params.put("user",ProcessContext.get().getSwitchUser().getId());
        DateTime currentTime = DateTime.now();
        params.put("date", currentTime.getDayDate());
        params.put("time", currentTime.getDayTime());
        params.put("states", states);
        return GeneralDao.Instance.executeUpdate(query, params);
    }

    public static int updateSettlementData(List<SettlementData> settleData, String documentNumber) {
        if (settleData == null || settleData.size() == 0)
            return 0;

        for(int i=0; i<settleData.size(); i++){
            logger.debug("Try to update settleData: "+settleData.get(i).getId());
        }

        List<SettlementData> stlDataForQuery = new ArrayList<SettlementData>();
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "";
        int counter = 0;
        int totalUpdate = 0;

        for (int i = 0; i < settleData.size(); i++) {
            stlDataForQuery.add(settleData.get(i));
            counter++;
            if(counter == 500 || i == settleData.size() - 1) {

                query = "update " + SettlementData.class.getName() + " s set "
                        +"s.documentNumber = :docNum "
                        +" where s in (:data)"
                        +" and s.documentNumber is null";

                params = new HashMap<String, Object>();
                params.put("docNum", documentNumber);
                params.put("data", stlDataForQuery);
                int updateSettlementData = GeneralDao.Instance.executeUpdate(query, params);
                logger.debug(updateSettlementData + " settlementData are settled in partial update in document-"+ documentNumber );

                totalUpdate += updateSettlementData;
                counter = 0;
                stlDataForQuery = new ArrayList<SettlementData>();
            }
        }
        return totalUpdate;
    }

    public static void updateSettlementData(List<SettlementData> settleData, SettlementState settlementState) {
        List<Long> stlDataForQuery = new ArrayList<Long>();
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "";
        int counter = 0;
        int totalUpdate = 0;

        GeneralDao.Instance.saveOrUpdate(settlementState);
        GeneralDao.Instance.flush();

        for (int i = 0; i < settleData.size(); i++) {
            stlDataForQuery.add(settleData.get(i).getId());
            counter++;
            if(counter == 500 || i == settleData.size() - 1) {

                query = "update "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".settlement_data set stl_state = :state where id in (:data) ";
                params.put("state", settlementState.getId());
                params.put("data", stlDataForQuery);

                int updateSettlementData = GeneralDao.Instance.executeSqlUpdate(query, params);
                logger.debug("settlementState: " + settlementState.getId() + " is set in partial update to " + updateSettlementData + " settlementData");

                totalUpdate += updateSettlementData;
                counter = 0;
                stlDataForQuery = new ArrayList<Long>();
            }
        }
    }

    public static int updateSettlementInfo(List<SettlementData> settleData, SettledState state) {
        if (settleData == null || settleData.size() == 0)
            return 0;

        DateTime now = DateTime.now();

        List<SettlementData> stlDataForQuery = new ArrayList<SettlementData>();
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "";
        int counter = 0;
        int totalUpdate = 0;

        for (int i = 0; i < settleData.size(); i++) {
            stlDataForQuery.add(settleData.get(i));
            counter++;
            if(counter == 500 || i == settleData.size() - 1) {

                query = "update " + SettlementInfo.class.getName() + " s set "
                        + " s.settledState = :state "
                        + " ,s.settledDate.dayDate = :settleDate "
                        + " ,s.settledDate.dayTime = :settleTime "
                        + " where s.settlementData in (:data)";

                params = new HashMap<String, Object>();
                params.put("state", state);
                params.put("settleDate", now.getDayDate());
                params.put("settleTime", now.getDayTime());
                params.put("data", stlDataForQuery);
                int updateSettlementInfo = GeneralDao.Instance.executeUpdate(query, params);
                logger.debug(updateSettlementInfo + " settlementInfo are settled in partial update");

                totalUpdate += updateSettlementInfo;
                counter = 0;
                stlDataForQuery = new ArrayList<SettlementData>();
            }
        }
        return totalUpdate;
    }

    public static void settleSettlementData(SettlementData settlementData) {
        DateTime now = DateTime.now();
        String documentNumber = settlementData.getClearingProfile().getName()+"- "+now;
        List<SettlementData> list = new ArrayList<SettlementData>();
        list.add(settlementData);
        int updateSettlementData = updateSettlementData(list, documentNumber);
        logger.debug(updateSettlementData + " settlementData are settled in document-"+ documentNumber );

        int updateSettlementInfo = updateSettlementInfo(list, SettledState.SETTLED);
        logger.debug(updateSettlementInfo + " settlementInfo are settled in document-"+ documentNumber );
		
		/*settlementState.setState(SettlementStateType.AUTOSETTLED);
		settlementState.setSettlementFileCreationDate(now);
		settlementState.setSettlementDate(now);
		settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
		GeneralDao.Instance.saveOrUpdate(settlementState);*/
    }

    public static int updateSettlementData(ClearingProfile clearingProfile, List<Terminal> terminals,
                                           DateTime accountUntilTime, DateTime settleUntilTime, List<SettlementDataType> types){

        logger.debug("Try to update all settlement data for terminals ("+ToStringBuilder.reflectionToString(terminals.toArray(), ToStringStyle.MULTI_LINE_STYLE)+")");

        Map<String, Object> parameters = new HashMap<String, Object>();

        String query = "update "+ SettlementData.class.getName()+ " s set"
//		+ " s.settlementTime.dayDate = :date, "
//		+ " s.settlementTime.dayTime = :time "
                + " s.settlementTimeLong = :date"
                + " where s.terminal in (:terminals) "
                + " and s.clearingProfile = :clearingProfile "
//		+ " and s.settlementTime.dayDate = :untilDate "
//		+ " and s.settlementTime.dayTime = :untilTime ";
                + " and s.settlementTimeLong = :untilDate ";

        if(types != null && types.size() > 0){
            query += " and s.type in (:types)";
            parameters.put("types", types);
        }

        DateTime now = DateTime.now();

        parameters.put("terminals", terminals);
        parameters.put("clearingProfile", clearingProfile);
        parameters.put("date", now.getDateTimeLong());
//		parameters.put("date", now.getDayDate());
//		parameters.put("time", now.getDayTime());
        parameters.put("untilDate", settleUntilTime.getDateTimeLong());
//		parameters.put("untilDate", settleUntilTime.getDayDate());
//		parameters.put("untilTime", settleUntilTime.getDayTime());

        int numUpdate = GeneralDao.Instance.executeUpdate(query, parameters);
        if (numUpdate >= 1) {
            settleUntilTime.setDayDate(now.getDayDate());
            settleUntilTime.setDayTime(now.getDayTime());
        }
        return numUpdate;
    }

    public static int updateSettlementData(SettlementData settlementData){

        Map<String, Object> parameters = new HashMap<String, Object>();

        String query = "update "+ SettlementData.class.getName()+ " s set"
//		+ " s.settlementTime.dayDate = :date, "
//		+ " s.settlementTime.dayTime = :time "
                + " s.settlementTimeLong = :date"
                + " where s.id = :stlData "
                ;

        DateTime now = DateTime.now();

        parameters.put("stlData", settlementData.getId());
        parameters.put("date", now.getDateTimeLong());
//		parameters.put("date", now.getDayDate());
//		parameters.put("time", now.getDayTime());

        return GeneralDao.Instance.executeUpdate(query, parameters);
    }

    public static void settleEmptySettlementState(SettlementState settlementState) {
        DateTime now = DateTime.now();
        String documentNumber = settlementState.getClearingProfile().getName()+"- "+now;
        int updateSettlementData = updateSettlementData(settlementState.getSettlementDatas(), documentNumber);
        logger.debug(updateSettlementData + " settlementData are settled in document-"+ documentNumber );

        int updateSettlementInfo = updateSettlementInfo(settlementState.getSettlementDatas(), SettledState.SETTLED);
        logger.debug(updateSettlementInfo + " settlementInfo are settled in document-"+ documentNumber );

        settlementState.setState(SettlementStateType.AUTOSETTLED);
        settlementState.setSettlementFileCreationDate(now);
        settlementState.setSettlementDate(now);
//		settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
        settlementState.setSettlingUser(ProcessContext.get().getSwitchUser());
        GeneralDao.Instance.saveOrUpdate(settlementState);
    }

    public static Pair<String, String> generateFCBDocument(String commentOnDocument, List<DocumentItemEntity> documentTopicEntitys, List<DepositInfoForIssueDocument> depositInfos
            ,String id, Ifx ifx, Shop shop, String documentNumber) throws BusinessException{
//		String password = getFanapCoreConfigValue(CoreConfigDataManager.CorePassword);
//		try {
//			password = new String(SecurityComponent.rsaDecrypt(Hex.decode(password)));
//		} catch (Exception e) {
//			logger.error("Encountere exception as decrypting password. ("+ e.getClass().getSimpleName()+": "+ e.getMessage());
//			logger.error(e);
//			throw new BusinessException(e);
//		}

        String password = getFanapCoreConfigValue(CoreConfigDataManager.CoreFakePass);

        String switchBranchId = getFanapCoreConfigValue(CoreConfigDataManager.SwitchBranchId);

        IssueGeneralDocVO issueGeneralDocVO = new IssueGeneralDocVO(commentOnDocument);
        if (depositInfos != null)
            issueGeneralDocVO.getDeposits().addAll(depositInfos);
        issueGeneralDocVO.getEntities().addAll(documentTopicEntitys);
        String transactionNumber = getFCBTransactionNumber(id, ifx, shop, documentNumber);
        String docXML = CoreGateway.generateDocumentRequest(issueGeneralDocVO, getFanapCoreConfigValue(CoreConfigDataManager.CoreUserName), password, switchBranchId, false, transactionNumber);
        return new Pair<String, String>(docXML, transactionNumber);
    }

    public static Pair<String, String> generateFCBDocumentByUserName(String commentOnDocument, List<DocumentItemEntity> documentTopicEntitys, List<DepositInfoForIssueDocument> depositInfos, String switchBranchId, String username, String password
            ,String id) throws BusinessException{
        IssueGeneralDocVO issueGeneralDocVO = new IssueGeneralDocVO(commentOnDocument);
        if (depositInfos != null)
            issueGeneralDocVO.getDeposits().addAll(depositInfos);
        issueGeneralDocVO.getEntities().addAll(documentTopicEntitys);
        String transactionNumber = getFCBTransactionNumber(id, null, null, null);
        String docXML = CoreGateway.generateDocumentRequest(issueGeneralDocVO, username, password, switchBranchId, false, transactionNumber);
        return new Pair<String, String>(docXML, transactionNumber);
    }

    public static Pair<String, String> issueFCBDocumentByRsCode(SettlementReport report, boolean retry) {
        String transactionId = null;
        String rsCode = null;
        try {
//            logger.info("before CoreGateway.issueDocument(report.getReport(), getFanapCoreConfigValue(CoreConfigDataManager.CoreUrl))");

            String fakePassword = getFanapCoreConfigValue(CoreConfigDataManager.CoreFakePass);
            String docXML = report.getReport();

            if (docXML.contains(fakePassword)) {
                logger.info("reort.getReport() : " + report.getReport());
                String password = getFanapCoreConfigValue(CoreConfigDataManager.CorePassword);
                try {
                    password = new String(SecurityComponent.rsaDecrypt(Hex.decode(password)));
                } catch (Exception e) {
                    logger.error("Encountere exception as decrypting password. ("+ e.getClass().getSimpleName()+": "+ e.getMessage());
                    logger.error(e);
                    throw new BusinessException(e);
                }

                docXML = docXML.replace(fakePassword, password);
            }


            TransferMoneyReturnVO returnVO = CoreGateway.issueDocument(docXML, getFanapCoreConfigValue(CoreConfigDataManager.CoreUrl));

            transactionId = returnVO.getTransactionCode();
            rsCode = returnVO.getRsCode();
            logger.debug("Response Code: " + rsCode + ", trans-id: " + transactionId);

        } catch (BusinessException e) {
            logger.warn("Encounter exception as issuing document. ("+ e.getClass().getSimpleName() + ": " + e.getMessage());
//			logger.warn(e, e);
            if (retry)
                SchedulerService.addInstantIssuingFCBDocumentTriggers(report, IssuingDocumentAction.REISSUE);

            return new Pair<String, String>(null, null);
        }
        return new Pair<String, String>(rsCode, transactionId);

    }

    public static String issueFCBDocument(SettlementReport report, boolean retry) {
        return issueFCBDocumentByRsCode(report, retry).second;
    }
	
	/*public static String issueFCBDocument(SettlementReport report, boolean retry) {
		try {
			TransferMoneyReturnVO returnVO = CoreGateway.issueDocument(report.getReport(), getFanapCoreConfigValue(CoreConfigDataManager.CoreUrl));
			
			String transactionId = returnVO.getTransactionCode();
			logger.debug("Response Code: "+ returnVO.getRsCode()+", trans-id: " + transactionId);
			return transactionId;
			
		} catch (BusinessException e) {
			logger.warn("Encounter exception as issuing document. (" +e.getClass().getSimpleName()+": "+ e.getMessage());
//			logger.warn(e,e);
			if (retry)
				SchedulerService.addInstantIssuingFCBDocumentTriggers(report, IssuingDocumentAction.REISSUE);
			return null;
		}
	}*/

    public static Boolean fullyReverseFCBDocument(String referenceDocumentNumber) throws BusinessException{
        String password = getFanapCoreConfigValue(CoreConfigDataManager.CorePassword);
        try {
            password = new String(SecurityComponent.rsaDecrypt(Hex.decode(password)));
        } catch (Exception e) {
            logger.error("Encountere exception as decrypting password. ("+ e.getClass().getSimpleName()+": "+ e.getMessage());
            logger.error(e);
            throw new BusinessException(e);
        }
        try {
            boolean fullyReverse = CoreGateway.fullReverseTransaction(referenceDocumentNumber,getFanapCoreConfigValue(CoreConfigDataManager.CoreUrl),getFanapCoreConfigValue(CoreConfigDataManager.CoreUserName),  password);
            return fullyReverse;
        } catch (BusinessException e) {
            logger.warn("Encounter exception as issuing document. (" +e.getClass().getSimpleName()+": "+ e.getMessage());
            logger.warn(e,e);
            return false;
        }
    }

    public static void settleSwitchTopics(List<String> topicCodes) throws BusinessException {
        String password = getFanapCoreConfigValue(CoreConfigDataManager.CorePassword);
        try {
            password = new String(SecurityComponent.rsaDecrypt(Hex.decode(password)));
        } catch (Exception e) {
            logger.error("Encountere exception as decrypting password. ("+ e.getClass().getSimpleName()+": "+ e.getMessage());
            logger.error(e);
            throw new BusinessException(e);
        }
        for (String topicCode : topicCodes) {
            try {
                CoreGateway.settleSwitchTopics(topicCode, getFanapCoreConfigValue(CoreConfigDataManager.CoreUrl), getFanapCoreConfigValue(CoreConfigDataManager.CoreUserName), password);
            } catch (BusinessException e) {
                logger.error("Encountere exception as settleSwitchTopic: "+topicCode+". ("+ e.getClass().getSimpleName()+": "+ e.getMessage());
                logger.error(e);
            }
        }
    }

    public static Double settleSwitchTopics(String topicCode) throws BusinessException{
        String password = getFanapCoreConfigValue(CoreConfigDataManager.CorePassword);
        try {
            password = new String(SecurityComponent.rsaDecrypt(Hex.decode(password)));
        } catch (Exception e) {
            logger.error("Encountere exception as decrypting password. ("+ e.getClass().getSimpleName()+": "+ e.getMessage());
            logger.error(e);
            throw new BusinessException(e);
        }
        try {
            return CoreGateway.settleSwitchTopics(topicCode, getFanapCoreConfigValue(CoreConfigDataManager.CoreUrl), getFanapCoreConfigValue(CoreConfigDataManager.CoreUserName), password);
        } catch (BusinessException e) {
            logger.error("Encountere exception as settleSwitchTopic: "+topicCode+". ("+ e.getClass().getSimpleName()+": "+ e.getMessage());
            logger.error(e);
            return null;
        }
    }

    public static boolean isAllSettlementDataSettled(SettlementState settlementState) {
        for (SettlementData data :settlementState.getSettlementDatas())
            if (!Util.hasText(data.getDocumentNumber()))
                return false;
        return true;
    }

    public static String getFanapCoreConfigValue(String varName){
        return CoreConfigDataManager.getValue(varName);
    }

    public static boolean isTopic(String accountId) {
        String[] strings = accountId.split("-");
        return strings.length <= 1;
    }

    public static List<SettlementReport> findSettlementReport(SettlementState settlementState, Core core){
        String query = "from "+ SettlementReport.class.getName()+" r "
                + " where r.settlementState = :state "
                + " and r.report is not null "
                + " and r.documentNumber is null ";
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("state", settlementState);
        if (core != null){
            query += " and r.core = :core ";
            params.put("core", core);
        }
        return GeneralDao.Instance.find(query, params);
    }

    public static String getFCBTransactionNumber(String id , Ifx ifx, Shop shop, String trxNumberPattern){
        String n = "1001/";
        String timeInMillis="";

        timeInMillis += (Util.hasText(id))?id:UUID.randomUUID().toString();

        if(trxNumberPattern != null) {
            try {
                logger.info("trxNum Pattern: " + trxNumberPattern + ", id: " + id);
                timeInMillis = generateTransactionNumberByPattern(ifx, shop, trxNumberPattern) + " " + timeInMillis ;
            } catch(Exception e) {
                logger.error("Exception in generating Transaction Number by pattern..." , e);
                timeInMillis = (Util.hasText(id))?id:UUID.randomUUID().toString();
            }
        }

        n +=timeInMillis+"/";
        int checkDigit = 2;
        for (int index =0; index<timeInMillis.length(); index++)
            checkDigit += timeInMillis.codePointAt(index)-48;

        return n+checkDigit;
    }


    public static String generateTransactionNumberByPattern(Ifx ifx, Shop shop, String trxNumberPattern){
        String groovyStr="";
        String groovy ="";

        StringBuilder commentOfDocumentItem = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(trxNumberPattern , "|");

        while(tokenizer.hasMoreTokens()){
            groovy = tokenizer.nextToken();
            if(Util.hasText(groovy)){
                if(groovy.contains("!")){
                    commentOfDocumentItem.append(groovy.substring(0,groovy.indexOf("!")));
                    groovyStr = groovy.substring(groovy.indexOf("!")+1);
                }
                Binding scriptBinding = new Binding();

                if(ifx != null)
                    scriptBinding.setProperty("ifx", ifx);

                if(shop != null)
                    scriptBinding.setProperty("shop", shop);
                Object run = null;

                try {
                    run = GlobalContext.getInstance().evaluateScript(groovyStr, scriptBinding);
                } catch(Exception e) {
                    logger.error("Exception in perTrxNumPatt evaluateScript " + groovyStr);
                }
                if(run != null)
                    commentOfDocumentItem.append(run.toString());

            }
        }

        return commentOfDocumentItem.toString();
    }
    public static List<SettlementData> findSettlementData(SettlementReport report){
        String query = "from "+ SettlementData.class.getName() + " d " +
                " where d.settlementReport = :report " +
                " order by d.id asc ";
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("report", report);
        return GeneralDao.Instance.find(query, params);
    }

    public static int updateSettlementData(List<SettlementData> settlementData, SettlementReport report) {
        if (settlementData == null || settlementData.size() == 0)
            return 0;

        List<SettlementData> stlDataForQuery = new ArrayList<SettlementData>();
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "";
        int counter = 0;
        int totalUpdate = 0;
        GeneralDao.Instance.saveOrUpdate(report);
        GeneralDao.Instance.flush();

        for (int i = 0; i < settlementData.size(); i++) {
            stlDataForQuery.add(settlementData.get(i));
            counter++;
            if(counter == 500 || i == settlementData.size() - 1) {

                query = "update " + SettlementData.class.getName() + " s set "
                        +" s.settlementReport = :report "
                        +" where s in (:data)";
//					+" and not exists elements (s.settlementDataReport)";

//				query = "update " + SettlementData.class.getName() + " s set "
//						+"s.settlementReport = :report "
//						+" where s in (:data)"
//						+" and s.settlementDataReport is null";

                params = new HashMap<String, Object>();
                params.put("report", report);
                params.put("data", stlDataForQuery);
                int updateSettlementData = GeneralDao.Instance.executeUpdate(query, params);
                logger.debug(updateSettlementData + " settlementData are settled in partial update-"+ report );

                totalUpdate += updateSettlementData;
                counter = 0;
                stlDataForQuery = new ArrayList<SettlementData>();
            }
        }
        return totalUpdate;
    }

    public static int updateSettlementInfo(List<SettlementData> settlementData, SettlementReport report) {
        if (settlementData == null || settlementData.size() == 0)
            return 0;

        List<SettlementData> stlDataForQuery = new ArrayList<SettlementData>();
        Map<String, Object> params = new HashMap<String, Object>();
        String query = "";
        int counter = 0;
        int totalUpdate = 0;
        GeneralDao.Instance.saveOrUpdate(report);
        GeneralDao.Instance.flush();

        for (int i = 0; i < settlementData.size(); i++) {
            stlDataForQuery.add(settlementData.get(i));
            counter++;
            if(counter == 500 || i == settlementData.size() - 1) {

                query = "update " + SettlementInfo.class.getName() + " s set "
                        +" s.settlementReport = :report "
                        +" where s.settlementData in (:data)";

                params = new HashMap<String, Object>();
                params.put("report", report);
                params.put("data", stlDataForQuery);
                int updateSettlementInfo = GeneralDao.Instance.executeUpdate(query, params);
                logger.debug(updateSettlementInfo + " settlementInfo are settled in partial update-"+ report );

                totalUpdate += updateSettlementInfo;
                counter = 0;
                stlDataForQuery = new ArrayList<SettlementData>();
            }
        }
        return totalUpdate;
    }

    public static void removeSettlementRecord(List<Transaction> transactions, ClearingProfile inClrProf, ClearingProfile notInClrProf) {
        if(notInClrProf == null || inClrProf != null)return;
        List<Transaction> allTransactions = new ArrayList<Transaction>();
        if (transactions != null && !transactions.isEmpty()) {
            allTransactions.addAll(transactions);
            List<Long> trxForQuery = new ArrayList<Long>();
            int counter = 0;

            for (int i = 0; i < allTransactions.size(); i++) {
                trxForQuery.add(allTransactions.get(i).getId());
                counter++;
                if (counter == 500 || i == allTransactions.size() - 1) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("trxList", trxForQuery);

                    String query = "delete " + SettlementRecord.class.getName() + " sr " +
                            " where sr.transactionId in (:trxList) ";

                    if (inClrProf != null) {
                        query += " and sr.clearingProfile = :clrProf ";
                        params.put("clrProf", inClrProf);
                    }

                    if (notInClrProf != null) {
                        query += " and sr.clearingProfile != :notClrProf ";
                        params.put("notClrProf", notInClrProf);
                    }

                    int deleteSettlementRecord = GeneralDao.Instance.executeUpdate(query, params);
                    logger.debug(deleteSettlementRecord + " settlementRecord are deleted");

                    counter = 0;
                    trxForQuery = new ArrayList<Long>();
                }
            }
        }
    }

    public static List<RemainingChargeRecord> getRemainingChargeReportRecords(Organization org, int firstResult, int maxResults) {
//		DateTime lastTime = DateTime.now();
//		lastTime.setDayTime(DayTime.MIN_DAY_TIME);
        String query = " select"
                + " count(*) as count,"
                + " mtn.credit as credit "
                + " from MTNCharge as mtn "
                + " where "
                + " mtn.entity = :org "
                + " and "
//			+ " ("
                + " mtn.state in (:freeStateList) "
//			+ " or "
//			+ " (mtn.state in (:lastAssignedList) and mtn.stateDate between :from and :to )"
//			+ " )"
                + " group by mtn.credit"
                ;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("org", org);
        params.put("freeStateList", new ArrayList<MTNChargeState>(){{
            add(MTNChargeState.NOT_ASSIGNED);
            add(MTNChargeState.LOCKED);
            add(MTNChargeState.CACHED);
            add(MTNChargeState.SOLD_BEFORE);
        }});
        GeneralDao.Instance.find(query, params);
        return GeneralDao.Instance.find(query,params, firstResult, maxResults, new AliasToBeanResultTransformer(RemainingChargeRecord.class));
    }

}
