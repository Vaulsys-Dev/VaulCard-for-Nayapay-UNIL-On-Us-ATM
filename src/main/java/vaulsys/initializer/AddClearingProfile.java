package vaulsys.initializer;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SynchronizationFlag;
import vaulsys.clearing.base.SynchronizationObject;
import vaulsys.clearing.consts.ClearingProcessType;
import vaulsys.clearing.consts.LockObject;
import vaulsys.clearing.consts.SettlementDataCriteria;
import vaulsys.clearing.consts.SettlementDataType;
import vaulsys.clearing.cyclecriteria.CycleCriteria;
import vaulsys.clearing.cyclecriteria.CycleType;
import vaulsys.clearing.settlement.ATMCurrencySettlementServiceImpl;
import vaulsys.clearing.settlement.ATMDailySettlementServiceImpl;
import vaulsys.clearing.settlement.ATMSettlementServiceImpl;
import vaulsys.clearing.settlement.BillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.ChargeSettlementServiceImpl;
import vaulsys.clearing.settlement.InstitutionSettlementServiceImpl;
import vaulsys.clearing.settlement.MCIBillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.MerchantSettlementServiceImpl;
import vaulsys.clearing.settlement.OnlinePerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.clearing.settlement.PerTransactionOnlineBillSettlementServiceImpl;
import vaulsys.clearing.settlement.PerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.RequestBasedSettlementServiceImpl;
import vaulsys.clearing.settlement.SeveralPerDayPerTrxSettlementServiceImpl;
import vaulsys.clearing.settlement.SeveralPerDaySettlementServiceImpl;
import vaulsys.job.JobSchedule;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.scheduler.job.ATMCurrencyCycleSettlementJob;
import vaulsys.scheduler.job.ATMCycleSettlementJob;
import vaulsys.scheduler.job.ATMDailyCycleSettlementJob;
import vaulsys.scheduler.job.BillPaymentCycleSettlementJob;
import vaulsys.scheduler.job.CellChargeCycleSettlementJob;
import vaulsys.scheduler.job.CycleAccountingJob;
import vaulsys.scheduler.job.CycleSettlementJob;
import vaulsys.scheduler.job.MCIBillPaymentCycleSettlementJob;
import vaulsys.scheduler.job.MerchantCycleSettlementJob;
import vaulsys.scheduler.job.OnlineCycleSettlementJob;
import vaulsys.scheduler.job.OnlinePerTransactionCycleSettlementJob;
import vaulsys.scheduler.job.PerTransactionCycleSettlementJob;
import vaulsys.scheduler.job.PerTransactionOnlineBillCycleSettlementJob;
import vaulsys.scheduler.job.RequestBasedCycleSettlementJob;
import vaulsys.scheduler.job.SeveralPerDayCycleSettlementJob;
import vaulsys.scheduler.job.SeveralPerDayPerTrxSettlementJob;
import vaulsys.scheduler.job.ShetabCycleSettlementJob;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.util.SettlementApplication;
import vaulsys.util.Util;

import java.util.HashMap;


public class AddClearingProfile {
	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
//			JobServiceQuartz.init(JobServiceQuartz.SWITCH_CONFIG);
			JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);
//			new AddClearingProfile().createCycleAccountJob();
//			new AddClearingProfile().run();
//			new AddClearingProfile().createCycleAccountJob();
			new AddClearingProfile().rescheduleSettlementJobs();
//			new AddClearingProfile().createPerDayClearingProfile(new ThirdPartyCycleSettlementJob());
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		System.out.println("before end tranaction");
		GeneralDao.Instance.endTransaction();
		System.out.println("after end tranaction");
		System.exit(0);
	}
	
	
	private void run() throws Exception {
		createPerDayClearingProfile(new ATMCurrencyCycleSettlementJob());
//		createPerDayClearingProfile(new CellChargeCycleSettlementJob());
//		createPerDayClearingProfile(new OnlinePerTransactionCycleSettlementJob());
//		createPerDayClearingProfile(new PerTransactionOnlineBillCycleSettlementJob());
//		createPerDayClearingProfile(new PerTransactionCycleSettlementJob());
//		createPerDayClearingProfile(new ATMCycleSettlementJob());
//		createPerDayClearingProfile(new OnlineCycleSettlementJob());
//		createPerDayClearingProfile(new MCIBillPaymentCycleSettlementJob());
		
//		createPerDayClearingProfile(new SeveralPerDayCycleSettlementJob());
//		createPerDayClearingProfile(new SeveralPerDayPerTrxSettlementJob());
		
//		createPerDayClearingProfile(new CyclePerTransactionCycleSettlementJob());
		
//		createCycleAccountJob();
		System.out.println("------- FINISHED -------");
	}
	
	public void rescheduleSettlementJobs() {
//		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 170601L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new MerchantCycleSettlementJob());
//		
//		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 170602L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new BillPaymentCycleSettlementJob());
//
//		clrProf = GeneralDao.Instance.load(ClearingProfile.class, 170603L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new CellChargeCycleSettlementJob());
		
		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 170616L);
		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new ATMCurrencyCycleSettlementJob());
//
//		clrProf = GeneralDao.Instance.load(ClearingProfile.class, 2303701L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new ShetabCycleSettlementJob());
//
//		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 19137601L);
//		ATMCycleSettlementJob settlementJob = new ATMCycleSettlementJob();
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), settlementJob);
//		
////		clrProf = GeneralDao.Instance.load(ClearingProfile.class, 26320102L);
////		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new MCIBillPaymentCycleSettlementJob());
//		
//		clrProf = GeneralDao.Instance.load(ClearingProfile.class, 26320101L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new OnlineCycleSettlementJob());
		
//		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 49137601L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new ATMDailyCycleSettlementJob());
		
//		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 170607L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new SaderatCycleSettlementJob());
		
//		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 170609L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new PerTransactionOnlineBillCycleSettlementJob());
		
//		ClearingProfile clrProf = GeneralDao.Instance.load(ClearingProfile.class, 170610L);
//		clrProf.setCycleCriteria(clrProf.getCycleCriteria(), new OnlinePerTransactionCycleSettlementJob());
	}

	public ClearingProfile createPerDayClearingProfile(CycleSettlementJob job) {
		ClearingProfile clearingProfile = null;
		DateTime fireTime = null;
		System.out.println("------------ Add ClearingProfile: "+job.getClass().getSimpleName()+" ------------");
		CycleCriteria criteria = new CycleCriteria();
		criteria.setCycleType(CycleType.PER_DAY);
		criteria.setCycleCount(1);
		
		if (job instanceof ATMDailyCycleSettlementJob) {
			
			clearingProfile = getClearingProfile(ATMDailySettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("تسویه روزانه صندوق خودپردازها");
			clearingProfile.setId(170605L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(IfxType.class, IfxType.WITHDRAWAL_RS.getType());
//			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.PARTIALLY_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED.getState());
//			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.SUSPECTED_DISAGREEMENT.getState());
			stlCriteria.setDocDesc("تسویه روزانه صندوق خودپرداز");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(ATMDailySettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile, 5, 0, 0, 0);
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(1, 0, 0));

			
		} else if(job instanceof SeveralPerDayCycleSettlementJob){
			
			clearingProfile = getClearingProfile(SeveralPerDaySettlementServiceImpl.class);
			if(clearingProfile != null)
				return clearingProfile;
			else 
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("چند بار در روز");
			clearingProfile.setId(170614L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.PARTIALLY_CLEARED.getState());
			stlCriteria.setDocDesc("تسویه چندبار در روز");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			clearingProfile.setLockObject(LockObject.CLEARING_PROFILE);
			clearingProfile.setHasFee(true);
			clearingProfile.setProcessType(ClearingProcessType.BATCH);
			
			clearingProfile.setSettlementClass(SeveralPerDaySettlementServiceImpl.class);
//			scheduleClearingProfile(clearingProfile, 5, 0, 0, -10);
			scheduleClearingProfile(clearingProfile, 20, 0, 0, 0);
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(12, 0, 0)); 
			criteria.setCycleCount(6);
			criteria.setCycleType(CycleType.PER_HOUR);
			clearingProfile.setAccountingGuaranteeMinute(-120);
			
			
		}
		else if(job instanceof SeveralPerDayPerTrxSettlementJob){
			
			clearingProfile = getClearingProfile(SeveralPerDayPerTrxSettlementServiceImpl.class);
			if(clearingProfile != null)
				return clearingProfile;
			else 
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("چند بار در روز تراکنشی");
			clearingProfile.setId(170615L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.PARTIALLY_CLEARED.getState());
			stlCriteria.setDocDesc("تسویه چندبار در روز تراکنشی");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			clearingProfile.setLockObject(LockObject.CLEARING_PROFILE);
			clearingProfile.setHasFee(true);
			clearingProfile.setProcessType(ClearingProcessType.BATCH);
			
			clearingProfile.setSettlementClass(SeveralPerDayPerTrxSettlementServiceImpl.class);
//			scheduleClearingProfile(clearingProfile, 5, 0, 0, -10);
			scheduleClearingProfile(clearingProfile, 20, 0, 0, 0);
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(12, 0, 0)); 
			criteria.setCycleCount(6);
			criteria.setCycleType(CycleType.PER_HOUR);
			clearingProfile.setAccountingGuaranteeMinute(-120);
			
			
		}
		else if (job instanceof CyclePerTransactionCycleSettlementJob) {
			clearingProfile = getClearingProfile(CyclePerTransactionSettlementServiceImpl.class);
//			if (clearingProfile!= null)
//				return clearingProfile;
//			else
//				clearingProfile = new ClearingProfile();
//			
//			clearingProfile.setName("سیکلی تراکنشی");
//			clearingProfile.setId(19137601L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(IfxType.class, IfxType.PURCHASE_RS.getType());
			stlCriteria.addCriteriaDatas(IfxType.class, IfxType.RETURN_RS.getType());
			
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
			
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(LifeCycle.class, LifeCycleStatus.REQUEST.getState());
			stlCriteria.setDocDesc("سیکلی تراکنشی");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(CyclePerTransactionSettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,20, 0, -3, 59 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(0, 0, 0)); 
			criteria.setCycleCount(2);
			criteria.setCycleType(CycleType.PER_HOUR);
		}
		
		else if (job instanceof ATMCycleSettlementJob) {
			
			clearingProfile = getClearingProfile(ATMSettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("تسویه صندوق خودپردازها");
			clearingProfile.setId(19137601L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(IfxType.class, IfxType.WITHDRAWAL_RS.getType());
//			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.PARTIALLY_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED.getState());
//			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.SUSPECTED_DISAGREEMENT.getState());
			stlCriteria.setDocDesc("تسویه صندوق خودپرداز");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(ATMSettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,5, 0, 0, -30 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(2, 0, 0));

			
		}/*else if (job instanceof ThirdPartyCycleSettlementJob){
			clearingProfile = getClearingProfile(ThirdPartySettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("تسویه روزانه thirdParty");
			clearingProfile.setId(170611L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.THIRD_PARTY_PAYMENT.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.setDocDesc("تسویه روزانه thirdParty");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(ThirdPartySettlementServiceImpl.class);
			clearingProfile.setHasFee(Boolean.TRUE);
			clearingProfile.setLockObject(LockObject.CLEARING_PROFILE);
			clearingProfile.setProcessType(ClearingProcessType.BATCH);
			scheduleClearingProfile(clearingProfile,20, -1, 23, 0);
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(2, 30, 0));
		}*/else
			if (job instanceof RequestBasedCycleSettlementJob){
			
			clearingProfile = getClearingProfile(RequestBasedSettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("تسویه مطابق درخواست");
			clearingProfile.setId(170606L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("مطابق درخواست");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
//			stlCriteria = new SettlementDataCriteria(SettlementDataType.SETTLE_TIME);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.setDocDesc("مطابق درخواست با وضعیت نامشخص");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(OnlineSettlementService.class);
			scheduleClearingProfile(clearingProfile,0, 0, 0, 0);
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(2, 0, 0));
		} else
			if (job instanceof OnlinePerTransactionCycleSettlementJob){
				
				clearingProfile = getClearingProfile(OnlinePerTransactionSettlementServiceImpl.class);
				if (clearingProfile!= null)
					return clearingProfile;
				else
					clearingProfile = new ClearingProfile();
				
				clearingProfile.setName("تسویه برخط فروشنده جدید");
				clearingProfile.setId(170610L);
				
				SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
				stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
				stlCriteria.setDocDesc("برخط فروشنده جدید");
				GeneralDao.Instance.saveOrUpdate(stlCriteria);
				clearingProfile.addCriterias(stlCriteria);
				
				clearingProfile.setSettlementClass(OnlinePerTransactionSettlementServiceImpl.class);
				clearingProfile.setHasFee(true);
				clearingProfile.setLockObject(LockObject.TERMINAL);
				scheduleClearingProfile(clearingProfile,0, 0, 0, 0);
				fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(2, 0, 0));
			} 
			else if (job instanceof OnlineCycleSettlementJob){
				
				clearingProfile = getClearingProfile(OnlineSettlementService.class);
				if (clearingProfile!= null)
					return clearingProfile;
				else
					clearingProfile = new ClearingProfile();
				
				clearingProfile.setName("برخط فروشنده");
				clearingProfile.setId(26320101L);
				
				SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
				stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
				stlCriteria.setDocDesc("برخط پذیرنده");
				GeneralDao.Instance.saveOrUpdate(stlCriteria);
				clearingProfile.addCriterias(stlCriteria);
				
//				stlCriteria = new SettlementDataCriteria(SettlementDataType.SETTLE_TIME);
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
				stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
				stlCriteria.setDocDesc("برخط پذیرنده با وضعیت نامشخص");
				GeneralDao.Instance.saveOrUpdate(stlCriteria);
				clearingProfile.addCriterias(stlCriteria);
				
				clearingProfile.setSettlementClass(OnlineSettlementService.class);
				scheduleClearingProfile(clearingProfile,0, 0, 0, 0);
				fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(1, 30, 0));
			} 
			else if (job instanceof PerTransactionCycleSettlementJob){
				
				clearingProfile = getClearingProfile(PerTransactionSettlementServiceImpl.class);
				if (clearingProfile!= null)
					return clearingProfile;
				else
					clearingProfile = new ClearingProfile();
				
				clearingProfile.setName("صدور سند تراکنشی");
				clearingProfile.setId(170608L);
				
				SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
				stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
				stlCriteria.setDocDesc("صدور سند تراکنشی");
				GeneralDao.Instance.saveOrUpdate(stlCriteria);
				clearingProfile.addCriterias(stlCriteria);
				
				clearingProfile.setSettlementClass(PerTransactionSettlementServiceImpl.class);
				scheduleClearingProfile(clearingProfile,0, 0, 0, 0);
				fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(1, 30, 0));
			} 
			else if (job instanceof PerTransactionOnlineBillCycleSettlementJob){
				
				clearingProfile = getClearingProfile(PerTransactionOnlineBillSettlementServiceImpl.class);
				if (clearingProfile!= null)
					return clearingProfile;
				else
					clearingProfile = new ClearingProfile();
				
				clearingProfile.setName("صدور سند تراکنشی نوپاد");
				clearingProfile.setId(170609L);
				
				SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
				stlCriteria.addCriteriaDatas(TrnType.class, TrnType.ONLINE_BILLPAYMENT.getType());
				stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
				stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
				stlCriteria.setDocDesc("صدور سند تراکنشی نوپاد");
				GeneralDao.Instance.saveOrUpdate(stlCriteria);
				clearingProfile.addCriterias(stlCriteria);
				
				clearingProfile.setSettlementClass(PerTransactionOnlineBillSettlementServiceImpl.class);
				scheduleClearingProfile(clearingProfile,0, 0, 0, 0);
				fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(1, 30, 0));
			} 
			else if (job instanceof MerchantCycleSettlementJob) {
			
			clearingProfile = getClearingProfile(MerchantSettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("یک بار در روز فروشنده");
			clearingProfile.setId(170601L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.WITHDRAWAL.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BALANCEINQUIRY.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.setDocDesc("تسويه پذيرندگان با وضعیت نامشخص");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			stlCriteria = new SettlementDataCriteria(SettlementDataType.SECOND);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.RETURN.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.WITHDRAWAL.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BALANCEINQUIRY.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("تسويه پذيرندگان با وضعیت متوازن");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(MerchantSettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,20, -1, 23, 0 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(2, 0, 0));
			
		} else if (job instanceof BillPaymentCycleSettlementJob) {
			
			clearingProfile = getClearingProfile(BillPaymentSettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("یک بار در روز پرداخت قبض");
			clearingProfile.setId(170602L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BILLPAYMENT.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("تسويه پرداخت قبض");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(BillPaymentSettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,20, -1, 23, 0 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(3, 0, 0));
			
		} else if (job instanceof CellChargeCycleSettlementJob) {
			
			clearingProfile = getClearingProfile(ChargeSettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("یک بار در روز شارژ ایرانسل");
			clearingProfile.setId(170603L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASECHARGE.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("تسويه ايرانسل");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(ChargeSettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,20, -1, 23, 0 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(2, 30, 0)); 
			
		} else if (job instanceof MCIBillPaymentCycleSettlementJob) {
			
			clearingProfile = getClearingProfile(MCIBillPaymentSettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("15 دقیقه یک بار پرداخت قبض همراه اول");
			clearingProfile.setId(170604L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BILLPAYMENT.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.setDocDesc("تسويه پرداخت قبض همراه اول");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(ChargeSettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,15, -1, 23, 0 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(0, 0, 0)); 
			criteria.setCycleCount(15);
			criteria.setCycleType(CycleType.PER_MINUTE);
			
		} else if (job instanceof ShetabCycleSettlementJob) {
			
			clearingProfile = getClearingProfile(InstitutionSettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("یک بار در روز شتاب");
			clearingProfile.setId(2303701L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.INCREMENTALTRANSFER.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.DECREMENTALTRANSFER.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASECHARGE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BALANCEINQUIRY.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.PURCHASE.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.BALANCEINQUIRY.getType());
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.WITHDRAWAL.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.PARTIALLY_CLEARED.getState());
			stlCriteria.setDocDesc("تسويه شتاب");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(InstitutionSettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,20, -1, 23, 0 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(3, 30, 0));
			
		} else if(job instanceof ATMCurrencyCycleSettlementJob) {
			clearingProfile = getClearingProfile(ATMCurrencySettlementServiceImpl.class);
			if (clearingProfile!= null)
				return clearingProfile;
			else
				clearingProfile = new ClearingProfile();
			
			clearingProfile.setName("یک بار در روز خودپرداز ارزی");
			clearingProfile.setId(170616L);
			
			SettlementDataCriteria stlCriteria = new SettlementDataCriteria(SettlementDataType.MAIN);
			stlCriteria.addCriteriaDatas(TrnType.class, TrnType.WITHDRAWAL_CUR.getType());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.CLEARED.getState());
			stlCriteria.addCriteriaDatas(ClearingState.class, ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED.getState());
			stlCriteria.setDocDesc("تسويه خودپرداز ارزی");
			GeneralDao.Instance.saveOrUpdate(stlCriteria);
			clearingProfile.addCriterias(stlCriteria);
			
			clearingProfile.setSettlementClass(ATMCurrencySettlementServiceImpl.class);
			scheduleClearingProfile(clearingProfile,20, -1, 23, 0 );
			fireTime = new DateTime(new DayDate(0, 0, 0), new DayTime(1, 0, 0));
		}
		
		clearingProfile.setSettleGuaranteeDay(-2);
		
		clearingProfile.setCreatorUser(DBInitializeUtil.getUser());
		clearingProfile.setCreatedDateTime(DateTime.now());
		
		clearingProfile.setFireTime(fireTime);
		clearingProfile.setCycleCriteria(criteria, job);
		
		GeneralDao.Instance.saveOrUpdate(clearingProfile);
		createClearingProfileSyncObj(clearingProfile);
		
		return clearingProfile;
	}


	private void createClearingProfileSyncObj(ClearingProfile clearingProfile) {
		String s = "from "+ SynchronizationObject.class.getName()+" s "
		+" where s.objectId = :object ";
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("object", clearingProfile.getId());
		SynchronizationObject object = (SynchronizationObject) GeneralDao.Instance.findObject(s, parameters);
		if (object == null){
			SynchronizationObject synchronizationObject = new SynchronizationObject();
			synchronizationObject.setObjectId(clearingProfile.getId());
			synchronizationObject.setObjClass(ClearingProfile.class.getSimpleName());
			synchronizationObject.setLock(SynchronizationFlag.Free);
			GeneralDao.Instance.saveOrUpdate(synchronizationObject);
		}
	}

	private void scheduleClearingProfile(ClearingProfile clearingProfile, Integer accountTimeOffsetMinute, Integer settleTimeOffsetDay,
			Integer settleTimeOffsetHour, Integer settleTimeOffsetMinute) {
		clearingProfile.setAccountTimeOffsetMinute(accountTimeOffsetMinute);
		clearingProfile.setSettleTimeOffsetDay(settleTimeOffsetDay);
		clearingProfile.setSettleTimeOffsetHour(settleTimeOffsetHour);
		clearingProfile.setSettleTimeOffsetMinute(settleTimeOffsetMinute);
	}
	
	public void createCycleAccountJob() {
		CycleCriteria criteria = new CycleCriteria();
		criteria.setCycleType(CycleType.PER_MINUTE);
		criteria.setCycleCount(30);
		String cronExpression = Util.generateCronExpression(criteria, null);
		
		JobSchedule jobSchedule = new JobSchedule(cronExpression);
		
		CycleAccountingJob job = new CycleAccountingJob();
		job.setJobSchedule(jobSchedule);
		System.out.println("Submitting job...");
		SettlementApplication.get().submitJob(job);
		System.out.println("job submitted...");
	}
	

	public ClearingProfile getClearingProfile(String name){
		String q = "from "+ ClearingProfile.class.getName()+" p "
					+ "where p.name="+name;
		return (ClearingProfile) GeneralDao.Instance.findObject(q, null);	
	}
	
	public ClearingProfile getClearingProfile(Class clazz){
		String q = "from "+ ClearingProfile.class.getName() +" p"
					+" where p.settlementClass = :clazz"; 
		HashMap <String, Object> params = new HashMap<String, Object>(); 
		params.put("clazz", clazz);
		return (ClearingProfile) GeneralDao.Instance.findObject(q, params);
	}
}
