package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.PanPrefixServiceNotAllowedException;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.migration.MigrationData;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class TransferProcessor extends MessageProcessor {
    private Logger logger = Logger.getLogger(this.getClass());


    public static final TransferProcessor Instance = new TransferProcessor();
    private TransferProcessor(){};

    @Override
    public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
            throws Exception {
        Ifx incomingIfx = incomingMessage.getIfx();

        String primaryBin = incomingIfx.getDestBankId();
        String secondaryBin= incomingIfx.getRecvBankId();

        Message outgoingMessage = new Message(MessageType.OUTGOING);
        outgoingMessage.setTransaction(transaction);
        Ifx outIfx = MsgProcessor.processor(incomingIfx);
        outIfx.setNetworkTrnInfo(incomingIfx.getNetworkTrnInfo().copy());
        outgoingMessage.setIfx(outIfx);

		/*if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType())&& !ErrorCodes.isSuccess(incomingIfx.getRsCode())
				//we are acquire 
				&& !IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(transaction.getReferenceTransaction().getInputMessage().getIfx().getIfxType())) {
			
			ScheduleMessage reversalMsg = getSchedulerService().createReversalScheduleMsg(
					incomingMessage.getTransaction().getFirstTransaction(), incomingIfx.getRsCode(), null);

			//Modified(2009.09.08): Reverse TRANSFER_FROM_ACCOUNT
			ScheduleMessage reversalMsg = SchedulerService.createReversalScheduleMsg(
					incomingMessage.getTransaction().getReferenceTransaction(), incomingIfx.getRsCode(), null);
			

			getGeneralDao().saveOrUpdate(reversalMsg);

			processContext.addPendingRequests(reversalMsg);
			SchedulerService.createReversalJobInfo(reversalMsg.getTransaction().getReferenceTransaction(), 0L);
			throw new ScheduleMessageFlowBreakDown();
		} */


        String masterCode = FinancialEntityService.getMasterInstitution().getBin().toString();
        if (IfxType.TRANSFER_RQ.equals(incomingIfx.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(incomingIfx.getIfxType())) {
//			if (!primaryBin.equals(GlobalContext.getInstance().getMyInstitution().getBin()) &&
//					!secondaryBin.equals(GlobalContext.getInstance().getMyInstitution().getBin())) {
//				outIfx.setIfxType(IfxType.TRANSFER_RQ);
//				outIfx.setTrnType(TrnType.TRANSFER);
//			} else {
            outIfx.setIfxType(IfxType.TRANSFER_FROM_ACCOUNT_RQ);
            outIfx.setTrnType(TrnType.DECREMENTALTRANSFER);

//			if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(incomingIfx.getTrnType()))
//				outIfx.setTrnType(TrnType.DECREMENTALTRANSFER);

            channel = ChannelManager.getInstance().getChannel(primaryBin, "out");

            if (channel == null)
                channel = ChannelManager.getInstance().getChannel(masterCode, "out");

            setMessageFlag(outgoingMessage, true, true, true, false);

        } else if (IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(incomingIfx.getIfxType())
                && !ISOResponseCodes.isSuccess(incomingIfx.getRsCode())) {

            if (IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(incomingIfx.getIfxType())) {
                transaction.setReferenceTransaction(transaction.getFirstTransaction());
                GeneralDao.Instance.saveOrUpdate(transaction);
            }
            if(incomingIfx.getTransaction().getFirstTransaction().getIncomingIfx().getTrnType().equals(TrnType.TRANSFER)){

                outIfx.setIfxType(IfxType.TRANSFER_RS);
                outIfx.setTrnType(TrnType.TRANSFER);
            }else if (incomingIfx.getTransaction().getFirstTransaction().getIncomingIfx().getTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT)){
                outIfx.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RS);
                outIfx.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
            }
//			outIfx.setApprovalCode("      ");

            Message originatorIncomingMessage = transaction.getReferenceTransaction().getInputMessage();
            channel = ((InputChannel) originatorIncomingMessage.getChannel()).getOriginatorChannel();
            outIfx.setSrc_TrnSeqCntr(originatorIncomingMessage.getIfx().getSrc_TrnSeqCntr());
            outIfx.setMy_TrnSeqCntr(outIfx.getSrc_TrnSeqCntr());
            if(outIfx.getNetworkTrnInfo() != null && incomingIfx.getNetworkTrnInfo() != null && outIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId().equals(outIfx.getNetworkTrnInfo().getId()))
                incomingIfx.setIfxSrcTrnSeqCntr(originatorIncomingMessage.getIfx().getSrc_TrnSeqCntr());

            fillRsRqDifferenceForTransfer(outIfx, originatorIncomingMessage.getIfx());
            setMessageFlag(outgoingMessage, false, false, true, false);
        } else {

            if (IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(incomingIfx.getIfxType())) {

                outIfx.setEMVRqData(incomingIfx.getEMVRqData().copy());

                String primaryAccount = incomingIfx.getAppPAN();
                String actualAppPAN = incomingIfx.getActualAppPAN();
                String secondaryAccount = incomingIfx.getSecondAppPan();
                String actualSecAppPAN = incomingIfx.getActualSecondAppPan();

                MigrationData migData = incomingIfx.getMigrationData();
                MigrationData secMigData = incomingIfx.getMigrationSecondData();

                outIfx.setMigrationData(secMigData);
                outIfx.setMigrationSecondData(migData);
//				if(incomingIfx.getTrnType().equals(TrnType.DECREMENTALTRANSFER_CARD_TO_ACCOUNT))
                if(!incomingMessage.getTransaction().getFirstTransaction().getIncomingIfx().getTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT))
                    secondaryBin = secondaryAccount.substring(0, 6);
                primaryBin = primaryAccount.substring(0, 6);

                outIfx.setDestBankId(secondaryBin);
                outIfx.setRecvBankId(primaryBin);
                /**************/
                if (secMigData != null) {
                    secondaryAccount = secMigData.getFanapAppPan();
                    actualSecAppPAN = secMigData.getNeginAppPan();
                }
                if (migData != null) {
                    primaryAccount = migData.getFanapAppPan();
                    actualAppPAN = migData.getNeginAppPan();
                }
                /**************/

                outIfx.setAppPAN(secondaryAccount);
                if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                    incomingIfx.setIfxEncAppPAN(secondaryAccount);
                outIfx.setActualAppPAN(actualSecAppPAN);
                if (!secondaryAccount.equals(actualSecAppPAN)) {
                    if (secondaryAccount.startsWith("502229")) {
                        outIfx.setAppPAN(secondaryAccount);
                        if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                            incomingIfx.setIfxEncAppPAN(secondaryAccount);
                        outIfx.setActualAppPAN(actualSecAppPAN);
                        secondaryBin = secondaryAccount.substring(0, 6);
                    }
                }
                /**************/
                outIfx.setSecondAppPan(primaryAccount);
                outIfx.setActualSecondAppPAN(actualAppPAN);
                if (!primaryAccount.equals(actualAppPAN)) {
                    if (primaryAccount.startsWith("502229")) {
                        outIfx.setSecondAppPan(actualAppPAN);
                        outIfx.setActualSecondAppPAN(actualAppPAN);
                        primaryBin = primaryAccount.substring(0, 6);
//						outIfx.setRecvBankId(primaryBin);
                    }
                }
                /**************/


//				outIfx.setDestBankId(secondaryBin);
//				outIfx.setRecvBankId(primaryBin);
                outIfx.setPostedDt(outIfx.getSettleDt());

                if(incomingIfx.getTransaction().getFirstTransaction().getIncomingIfx().getTrnType().equals(TrnType.TRANSFER)){
                    outIfx.setIfxType(IfxType.TRANSFER_TO_ACCOUNT_RQ);
                    outIfx.setTrnType(TrnType.INCREMENTALTRANSFER);

                }
                else if(incomingIfx.getTransaction().getFirstTransaction().getIncomingIfx().getTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT)){
                    outIfx.setTrnType(TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT);
                    outIfx.setIfxType(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ);
                }

                channel = ChannelManager.getInstance().getChannel(secondaryBin, "out");
                if (channel == null)
                    channel = ChannelManager.getInstance().getChannel(masterCode, "out");

				
				/*String nextSeq = Util.generateTrnSeqCntr(seqCntrLength);
				outIfx.setSrc_TrnSeqCntr(nextSeq);
				outIfx.setMy_TrnSeqCntr(nextSeq);*/

                outIfx.setEMVRsData(null);

                Transaction origTransaction = transaction.getFirstTransaction();

                fillRsRqDifferenceForTransfer(outIfx, origTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/);

                setMessageFlag(outgoingMessage, true, true, true, true);
                transaction.setReferenceTransaction(origTransaction);

                /****************** Mirkamali(Task140): Set flag flag for Transfer_From_RS ************************/
                if(GlobalContext.getInstance().getMyInstitution().getBin().equals(incomingIfx.getBankId()))
                    TransactionService.putFlagOnTrasnferFromTransaction(origTransaction);
                /**************************************************************************************************/

            } else if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType())
                    ||
                    IfxType.TRANSFER_RS.equals(incomingIfx.getIfxType())||
                    IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType())||
                    IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType())) {

                String primaryAccount = incomingIfx.getAppPAN();
                String actualAppPAN = incomingIfx.getActualAppPAN();
                String secondaryAccount = incomingIfx.getSecondAppPan();
                String actualSecAppPAN = incomingIfx.getActualSecondAppPan();

                MigrationData migData = incomingIfx.getMigrationData();
                MigrationData secMigData = incomingIfx.getMigrationSecondData();

                outIfx.setMigrationData(secMigData);
                outIfx.setMigrationSecondData(migData);

                outIfx.setDestBankId(secondaryBin);
                outIfx.setRecvBankId(primaryBin);
                if (TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(incomingIfx.getTrnType()) || TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(incomingIfx.getIfxType())){
                    outIfx.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
                    outIfx.setIfxType(IfxType.TRANSFER_CARD_TO_ACCOUNT_RS);

                }
                else{

                    outIfx.setTrnType(TrnType.TRANSFER);

                    outIfx.setIfxType(IfxType.TRANSFER_RS);
                }

                Message originatorIncomingMessage = transaction.getReferenceTransaction().getInputMessage();
                if (IfxType.TRANSFER_RS.equals(incomingIfx.getIfxType())||
                        IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType())) {
                    originatorIncomingMessage = transaction.getFirstTransaction().getInputMessage();
                } else {
//					outIfx.setAppPAN(secondaryAccount);
//					outIfx.setActualAppPAN(actualSecAppPAN);

//					outIfx.setSecondAppPan(primaryAccount);
//					outIfx.setActualSecondAppPAN(actualAppPAN);


                    if (secMigData != null) {
                        secondaryAccount = secMigData.getFanapAppPan();
                        actualSecAppPAN = secMigData.getNeginAppPan();
                    }
                    if (migData != null) {
                        primaryAccount = migData.getFanapAppPan();
                        actualAppPAN = migData.getNeginAppPan();
                    }

                    /**************/
                    outIfx.setAppPAN(secondaryAccount);
                    if(outIfx.getEMVRqData() != null && incomingIfx.getEMVRqData() != null && outIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId() != null && incomingIfx.getEMVRqData().getId().equals(outIfx.getEMVRqData().getId()))
                        incomingIfx.setIfxEncAppPAN(secondaryAccount);
                    outIfx.setActualAppPAN(actualSecAppPAN);
//					if (!secondaryAccount.equals(actualSecAppPAN)) {
//						if (secondaryAccount.startsWith("502229")) {
////							outIfx.setAppPAN(secondaryAccount);
//							outIfx.setActualAppPAN(actualSecAppPAN);
////							secondaryBin = Long.parseLong(actualSecAppPAN.substring(0, 6));
//						}
//					}
                    /**************/
                    outIfx.setSecondAppPan(primaryAccount);
                    outIfx.setActualSecondAppPAN(actualAppPAN);
//					if (!primaryAccount.equals(actualAppPAN)) {
//						if (primaryAccount.startsWith("502229")) {
////							outIfx.setSecondAppPan(primaryAccount);
//							outIfx.setActualSecondAppPAN(actualAppPAN);
////							primaryBin = Long.parseLong(actualAppPAN.substring(0, 6));
//						}
//					}
                    /**************/
                    outIfx.setDestBankId(secondaryBin);
                    outIfx.setRecvBankId(primaryBin);
                    /**************/

                    outIfx.setSrc_TrnSeqCntr(originatorIncomingMessage.getIfx().getSrc_TrnSeqCntr());
                    outIfx.setMy_TrnSeqCntr(outIfx.getSrc_TrnSeqCntr());
                    if(outIfx.getNetworkTrnInfo() != null && incomingIfx.getNetworkTrnInfo() != null && outIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId() != null && incomingIfx.getNetworkTrnInfo().getId().equals(outIfx.getNetworkTrnInfo().getId()))
                        incomingIfx.setIfxSrcTrnSeqCntr(originatorIncomingMessage.getIfx().getSrc_TrnSeqCntr());
                }

                channel = ((InputChannel) originatorIncomingMessage.getChannel()).getOriginatorChannel();

                Ifx firstInputIfx = transaction.getFirstTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
                if ((IfxType.TRANSFER_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType()) &&
                        IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(firstInputIfx.getIfxType()) )||
                        (IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType()) &&
                                IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(firstInputIfx.getIfxType()))) {
                    outIfx.setAcctBalAvailable(firstInputIfx.getAcctBalAvailable());
                    outIfx.setAcctBalLedger(firstInputIfx.getAcctBalLedger());
                }

                fillRsRqDifferenceForTransfer(outIfx, originatorIncomingMessage.getIfx());
                setMessageFlag(outgoingMessage, false, false, true, true);


//				if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType()) && ErrorCodes.shouldBeRepeated(incomingIfx.getRsCode())) {
//					// Modified(2011.07.16): Reverse TRANSFER_FROM_ACCOUNT
//
//					ScheduleMessage reversalMsg = SchedulerService.createReversalScheduleMsg(incomingMessage.getTransaction().getFirstTransaction(), incomingIfx.getRsCode(), null);
//
//					GeneralDao.Instance.saveOrUpdate(reversalMsg);
//					GeneralDao.Instance.saveOrUpdate(reversalMsg.getMsgXml());
//
//					processContext.addPendingRequests(reversalMsg);
//					SchedulerService.createReversalJobInfo(reversalMsg.getTransaction().getReferenceTransaction(), 0L);
//				} else {

                if ((IfxType.TRANSFER_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType()) ||
                        IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS.equals(incomingIfx.getIfxType()))
                        && !ISOResponseCodes.isSuccess(incomingIfx.getRsCode())

//							// Modified(2011.07.23): Reverse TRANSFER_FROM_ACCOUNT
//							&& !ErrorCodes.shouldNotBeReversedForTransfer(incomingIfx.getRsCode())

                        // Modified(2011.11.05): Reverse TRANSFER_FROM_ACCOUNT
                        && ISOResponseCodes.isMessageDone(incomingIfx.getRsCode())

                        // we are acquire
                        && !IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(transaction.getReferenceTransaction().getIncomingIfx().getIfxType())
                        && !IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(transaction.getReferenceTransaction().getIncomingIfx().getIfxType())) {

						 /* ScheduleMessage reversalMsg = getSchedulerService().createReversalScheduleMsg(incomingMessage.getTransaction().getFirstTransaction(),incomingIfx.getRsCode(), null);*/

                    // Modified(2009.09.08): Reverse TRANSFER_FROM_ACCOUNT
                    ScheduleMessage reversalMsg = SchedulerService.createReversalScheduleMsg(incomingMessage.getTransaction().getReferenceTransaction(), incomingIfx.getRsCode(), null);

                    /***
                     * nabayad javabe transfer_to ra mostaghim be terminaleman befrestim, be onvane nemoone agar dar transfer_to AuthFailure begirim nabayad atm cart ra capture konad
                     ***/
                    if (ISOResponseCodes.shouldBeCaptured(incomingIfx.getRsCode())) {
                        outIfx.setRsCode(ISOResponseCodes.INVALID_MERCHANT);
                    }

                    GeneralDao.Instance.saveOrUpdate(reversalMsg);
                    GeneralDao.Instance.saveOrUpdate(reversalMsg.getMsgXml());

                    processContext.addPendingRequests(reversalMsg);
                    SchedulerService.createReversalJobInfo(reversalMsg.getTransaction().getReferenceTransaction(), 0L);
                    // throw new ScheduleMessageFlowBreakDown();
                }
//				}
            }
        }

//		}
        outgoingMessage.setChannel(channel);
        transaction.addOutputMessage(outgoingMessage);

        Terminal findEndpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), false, processContext);

        /**** for migration transfer *****/
        if ((IfxType.TRANSFER_RS.equals(outIfx.getIfxType()) ||
                IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(outIfx.getIfxType())) &&
                (outIfx.getMigrationData() != null || outIfx.getMigrationSecondData() != null)) {
//			if (GlobalContext.getInstance().getMyInstitution().getBin().equals(outgoingMessage.getIfx().getBankId())) {
            if (ProcessContext.get().getMyInstitution().getBin().equals(outgoingMessage.getIfx().getBankId())) {
                findEndpointTerminal = (Terminal) GeneralDao.Instance.load(channel.getEndPointType().getClassType(), Long.parseLong(outIfx.getTerminalId()));
            }
        }
        outgoingMessage.setEndPointTerminal(findEndpointTerminal);

        addNecessaryDataToIfx(outIfx, channel, findEndpointTerminal);

        GeneralDao.Instance.saveOrUpdate(outIfx);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());

        transaction.setDebugTag(outIfx.getIfxType().toString());

        GeneralDao.Instance.saveOrUpdate(transaction);

        return outgoingMessage;
    }

    public void fillRsRqDifferenceForTransfer(Ifx outIfx, Ifx refIfx) {
        outIfx.setSec_Amt(refIfx.getSec_Amt());
        outIfx.setSec_CurRate(refIfx.getSec_CurRate());
        if (refIfx.getExpDt()!= null)
            outIfx.setExpDt(refIfx.getExpDt());
        outIfx.setSettleDt(refIfx.getSettleDt() != null ? refIfx.getSettleDt() : outIfx.getSettleDt());
        outIfx.setPostedDt(refIfx.getPostedDt());
        outIfx.setTerminalType(refIfx.getTerminalType());
        outIfx.setAuth_Currency(refIfx.getAuth_Currency());
        outIfx.setSec_Currency(refIfx.getSec_Currency());
        outIfx.setCardHolderFamily(refIfx.getCardHolderFamily());
        outIfx.setCardHolderName(refIfx.getCardHolderName());
        outIfx.setPINBlock(refIfx.getPINBlock());
    }

    @Override
    public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
//		super.messageValidation(ifx, incomingMessage);

        if (!Boolean.TRUE.equals(ConfigUtil.getBoolean(ConfigUtil.THREE_BIN_TRANSFER_SUPPORT)))
            check3BinInTransferMessage(ifx);
//		check3BinInTransferMessage(ifx);
        if (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())&& TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType())) {
            if (!Util.hasText(ifx.getAppPAN()) || /*!ifx.getAppPAN().matches("\\d+.\\d+.\\d+.\\d+")*/ !Util.isAccount(ifx.getAppPAN()))
                throw new MandatoryFieldException("Invalid Account Number: " + ifx.getAppPAN());
            if (!Util.hasText(ifx.getSecondAppPan())|| (ifx.getSecondAppPan().length() != 16 && ifx.getSecondAppPan().length() != 19)) {
                throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has wrong SecAppPan: "
                        + ifx.getSecondAppPan());
            }
        } else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifx.getIfxType())&& TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())) {
            if (!Util.hasText(ifx.getSecondAppPan()) || /*!ifx.getSecondAppPan().matches("\\d+.\\d+.\\d+.\\d+")*/ !Util.isAccount(ifx.getSecondAppPan())) {
                throw new MandatoryFieldException("Invalid Account Number: " + ifx.getSecondAppPan());
            }
            if (!Util.hasText(ifx.getAppPAN()) || (ifx.getAppPAN().length() != 16 && ifx.getAppPAN().length() != 19)) {
                throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has wrong AppPan: "
                        + ifx.getAppPAN());
            }
        } else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifx.getIfxType())&& TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType())) {
            if (!Util.hasText(ifx.getSecondAppPan()) || (!Util.isAccount(ifx.getSecondAppPan()) && ifx.getSecondAppPan().length() != 16 && ifx.getSecondAppPan().length() != 19)) {
                throw new MandatoryFieldException("Invalid Account Number: " + ifx.getSecondAppPan());
            }
            if (!Util.hasText(ifx.getAppPAN()) || (!Util.isAccount(ifx.getAppPAN()) && ifx.getAppPAN().length() != 16 && ifx.getAppPAN().length() != 19)) {
                throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has wrong AppPan: "
                        + ifx.getAppPAN());
            }
        }else if (!Util.hasText(ifx.getAppPAN()) ||(ifx.getAppPAN().length() != 16 && ifx.getAppPAN().length() != 19)
                || !Util.hasText(ifx.getSecondAppPan()) ||(ifx.getSecondAppPan().length() != 16 && ifx.getSecondAppPan().length() != 19))
            throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has wrong AppPan: " + ifx.getAppPAN() +
                    " or SecAppPan: " + ifx.getSecondAppPan());

        if (!IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType()) && !IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())
                && !IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifx.getIfxType()) &&
                !IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifx.getIfxType()))
            if (ifx.getAuth_Amt() == null || ifx.getAuth_Amt() <= 0L)
                throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has wrong Amount: " + ifx.getAuth_Amt());
    }


    private void check3BinInTransferMessage(Ifx ifx) throws PanPrefixServiceNotAllowedException {
        if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())
                && ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {

//			Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
            Long myBin = ProcessContext.get().getMyInstitution().getBin();
            /**** we are acquire ****/
//			if (myBin.equals(ifx.getBankId())) {
//				if (!ifx.getDestBankId().equals(639347L) && !ifx.getRecvBankId().equals(639347L)) {
//			if (!GlobalContext.getInstance().isPeerInstitution(ifx.getDestBankId()) && !GlobalContext.getInstance().isPeerInstitution(ifx.getRecvBankId()) ){
            if (!ProcessContext.get().isPeerInstitution(Util.longValueOf(ifx.getDestBankId()))&& !ProcessContext.get().isPeerInstitution(Util.longValueOf(ifx.getRecvBankId()))){
                if (!myBin.equals(ifx.getDestBankId()) && !myBin.equals(ifx.getRecvBankId())) {
                    if (ifx.getDestBankId() == null || !ifx.getDestBankId().equals(ifx.getRecvBankId()))
                        throw new PanPrefixServiceNotAllowedException("Failed: Transfer Message with 3 engage institution. 32(" + ifx.getBankId() +"), 33("
                                + ifx.getDestBankId() + "), 100(" + ifx.getRecvBankId() + ")");
                }
            }
//			}
        }
    }

}
