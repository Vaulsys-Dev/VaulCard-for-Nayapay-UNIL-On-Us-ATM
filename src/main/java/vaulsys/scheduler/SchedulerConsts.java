package vaulsys.scheduler;

public class SchedulerConsts {
    public static final String DAILY_PROCESSES_GROUP = "DailyProcessesGroup";
    public static final String REPEAT_GROUP = "RepeatGroup";
    public static final String REVERSAL_GROUP = "ReversalGroup";

    public static final String DAILY_PROCESSES_TRIGGER_GROUP = "DailyProcessesTriggerGroup";
    public static final String REPEAT_REVERSAL_TRIGGER_GROUP = "RepeatReversalTriggerGroup";

    public static final String REPEAT_TRIGGER_GROUP = "RepeatTriggerGroup";

    public static final String REPEAT_JOB = "RepeatJob";
    public static final String REVERSAL_JOB = "ReversalJob";
    public static final String EOD_PRPCESS_JOB = "EODProcessJob";

    public static final String EOD_PRPCESS_TRIGER = "EODProcessTriger";

    public static final String REPEAT_MSG_TYPE = "Repeat";
    public static final String REVERSAL_MSG_TYPE = "Reversal";
    public static final String CLEAR_MSG_TYPE = "EndOfDay";
    public static final String TIME_OUT_MSG_TYPE = "TimeOutRs";
    public static final String REVERSAL_TIME_OUT_MSG_TYPE = "ReversalTimeOutRs";
    public static final String SETTLEMENT_MSG_TYPE = "settlement";
    
    public static final String CONFIRMATION_TRX_TYP = "ConfTrxProcessJob";

    //m.rehman: to differentiate SAF and Loro Introduce variables
    public static final String REVERSAL_REPEAT_MSG_TYPE = "ReversalRepeat";
    public static final String ADVICE_MSG_TYPE = "Advice";
    public static final String ADVICE_REPEAT_MSG_TYPE = "AdviceRepeat";
    public static final String LORO_MSG_TYPE = "Loro";
    public static final String LORO_REPEAT_MSG_TYPE = "LoroRepeat";
    public static final String LORO_REVERSAL_MSG_TYPE = "LoroReversal";
    public static final String LORO_REVERSAL_REPEAT_MSG_TYPE = "LoroReversalRepeat";
    public static final String WALLET_TOPUP_REVERSAL_MSG_TYPE = "WalletTopupReversal";
    public static final String WALLET_TOPUP_REVERSAL_REPEAT_MSG_TYPE = "WalletTopupReversalRepeat";
}
