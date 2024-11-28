package vaulsys.transfermanual;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.message.MessageManager;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionType;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class TransferManual {

    private static final Logger logger = Logger.getLogger(SchedulerService.class);
    private static TransferManual instance = null;

    private TransferManual(){

    }

    public static TransferManual getInstance(){
        if(instance == null){
            instance = new TransferManual();
        }
        return instance;
    }

    public void TransferSorushi(ArrayList<BeanDataTransfer> dataTransfer){
        for(BeanDataTransfer data :dataTransfer){
            StartTransferSorushi(data);
        }
    }

    public void StartTransferSorushi(BeanDataTransfer dataTransfer){
        TransferStart(dataTransfer);
    }

    public ScheduleMessage getTrxTransferSorushi(BeanDataTransfer dataTransfer){
        return TransferTrx(dataTransfer);
    }

    public void TransferStart(ArrayList<BeanDataTransfer> dataTransfer){
        for(BeanDataTransfer record  : dataTransfer){
            TransferStart(record);
        }

    }

    public ScheduleMessage TransferTrx(BeanDataTransfer dataTransfer){
        logger.debug("Trying to create Transfer Sorush message...");
        ScheduleMessage sorushMessage = createMessagNew(dataTransfer,IfxType.TRANSFER_FROM_ACCOUNT_RQ, ISOResponseCodes.APPROVED);
        return sorushMessage;
    }



    public void TransferStart(BeanDataTransfer dataTransfer){

        logger.debug("Trying to put Transfer Sorush message...");
        ScheduleMessage sorushMessage = createMessagNew(dataTransfer,IfxType.TRANSFER_FROM_ACCOUNT_RQ, ISOResponseCodes.APPROVED);
        GeneralDao.Instance.beginTransaction();
        MessageManager.getInstance().putRequest(sorushMessage, null, System.currentTimeMillis());
        GeneralDao.Instance.endTransaction();
        EndTransferSorushi(dataTransfer);

    }

    public void EndTransferSorushi(BeanDataTransfer dataTransfer){

    }

    public ScheduleMessage createMessagNew(BeanDataTransfer dataTransfer,IfxType ifxtype, String cause){

        ScheduleMessage scheduleMessage;
        scheduleMessage = new ScheduleMessage(MessageType.INCOMING.toString(), dataTransfer.getReverslSorush().amount);
        scheduleMessage.setChannel(GlobalContext.getInstance().getChannel("channelSHETABIn") );
        scheduleMessage.setResponseCode(cause);
        Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
        newTransaction.setDebugTag(IfxType.TRANSFER_TO_ACCOUNT_RQ.toString());
        newTransaction.setInputMessage(scheduleMessage);
        newTransaction.setFirstTransaction(newTransaction);
        newTransaction.setBeginDateTime(DateTime.now());
        scheduleMessage.setTransaction(newTransaction);
        Terminal endPointTerminal = GeneralDao.Instance.load(Terminal.class, new Long(113));
        scheduleMessage.setEndPointTerminal(endPointTerminal);
//        DateTime origDt = new DateTime(dataTransfer.getReverslSorush().persianDt, new DayTime(0, 0, 0));

        Ifx ifx = generateSorushMsgIfx(dataTransfer.getTrx().getIncomingIfx().getOrgIdNum(),
                dataTransfer.getReverslSorush().terminalId,
                dataTransfer.getReverslSorush().appPan,  dataTransfer.getReverslSorush().amount,
                endPointTerminal,dataTransfer.getReverslSorush().trnSeqCntr,dataTransfer.getTrx());
        scheduleMessage.setIfx(ifx);
        LifeCycle lifeCycle = new LifeCycle();
        GeneralDao.Instance.saveOrUpdate(lifeCycle);
        newTransaction.setLifeCycle(lifeCycle);
        GeneralDao.Instance.saveOrUpdate(newTransaction);
        GeneralDao.Instance.saveOrUpdate(ifx.getEMVRqData());
        GeneralDao.Instance.saveOrUpdate(ifx);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());

        return scheduleMessage;

    }

    public static Ifx generateSorushMsgIfx(String orgIdNum, String terminalId, String cardNumber, Long amount,Terminal endPoint,String trnSeqCounter,Transaction trx) {
        Ifx ifx = new Ifx();
        ifx.setIfxDirection(IfxDirection.INCOMING);
        ifx.setIfxType(IfxType.TRANSFER_TO_ACCOUNT_RQ);
        ifx.setTrnType(TrnType.INCREMENTALTRANSFER);

        ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
        ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);
        ifx.setEndPointTerminal(endPoint);
        ifx.setEndPointTerminalCode(endPoint.getCode());
        try{
            ifx.setAuth_Currency(ProcessContext.get().getRialCurrency().getCode());
        }catch(Exception e){
            ifx.setAuth_Currency(364);
        }

        ifx.setAuth_CurRate("1");
        ifx.setAuth_Amt(amount);
        ifx.setReal_Amt(amount);
        ifx.setTrx_Amt(amount);

        ifx.setSec_Currency(ifx.getAuth_Currency());
        ifx.setSec_Amt(ifx.getAuth_Amt());

        ifx.setOrigDt(trx.getIncomingIfx().getOrigDt());
        ifx.setTrnDt(trx.getIncomingIfx().getTrnDt());
        ifx.setReceivedDt(DateTime.now());

        ifx.setSrc_TrnSeqCntr( Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));
        ifx.setMy_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
        ifx.setNetworkRefId(Util.generateTrnSeqCntr(11));

        ifx.setTerminalType(TerminalType.ATM);
        ifx.setTerminalId(terminalId.toString());
        ifx.setOrgIdNum(orgIdNum.toString());

        ifx.setBankId("936450");
        ifx.setRecvBankId(ifx.getBankId());
        ifx.setAppPAN(cardNumber);
        ifx.setDestBankId(cardNumber.substring(0, 6));
        ifx.setFwdBankId(ifx.getDestBankId());
        ifx.setPostedDt(new MonthDayDate(DateTime.now().getDayDate()));
        
        if(trx != null && trx.getIncomingIfx()!= null && trx.getIncomingIfx().getBankId() != null){

            ifx.getSafeOriginalDataElements().setBankId(trx.getIncomingIfx().getBankId()/*Util.longValueOf(cardNumber.substring(0, 6))*/);
        }else{
            ifx.getSafeOriginalDataElements().setBankId(cardNumber.substring(0, 6));
        }
        ifx.getOriginalDataElements().setTrnSeqCounter(trnSeqCounter.toString());
        ifx.getOriginalDataElements().setOrigDt(trx.getIncomingIfx().getOrigDt());
        ifx.getOriginalDataElements().setTerminalId(terminalId.toString());
        ifx.getOriginalDataElements().setAppPAN(ifx.getAppPAN());

		ifx.setTrk2EquivData("9364501000000016=94091209644300000000");
//		ifx.setPINBlock("C5433EE9B5D7FC78");
		ifx.setSecondAppPan("9364501000000016");

        return ifx;
    }
}
