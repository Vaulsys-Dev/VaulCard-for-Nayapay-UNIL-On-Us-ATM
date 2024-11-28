package vaulsys.clearing.components;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.reconcile.ISOCutover;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.KeyManagementMode;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;
import java.util.List;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class EODHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(EODHandler.class);

    public static final EODHandler Instance = new EODHandler();

    private EODHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
//        EODComponent component = new EODComponent();

        ISOMsg isoMsg = null;

        // Cutover
        List<Institution> instList = FinancialEntityService.findAllSlaveInstitutions();

        ChannelManager channelManager = ChannelManager.getInstance();

        for (Institution institution : instList) {
            Channel channel = channelManager.getChannel(institution.getCode().toString(), "out");

            if (channel == null)
                channel = channelManager.getChannel(institution.getCode().toString(), "");

            if (channel == null) {
                logger.error("No channel associated with slave institution " + institution.getCode());
                continue;
            }

            //TODO important!!
            isoMsg = ISOCutover.Instance.buildRequset(institution);
//            	component.generateCutOverRqMessage(institution);

            ISOPackager packager = ((ISO8583BaseProtocol)channel.getProtocol()).getPackager();
            isoMsg.setPackager(packager);

//            OutgoingMessage outMsg = new OutgoingMessage();

            Message outMsg = new Message(MessageType.OUTGOING);

            Transaction trans = processContext.getTransaction();
            if (trans.getLifeCycle() == null){
            	LifeCycle lifeCycle = new LifeCycle();
            	GeneralDao.Instance.saveOrUpdate(lifeCycle);
				trans.setLifeCycle(lifeCycle);
            }
            trans.setFirstTransaction(trans);
//            trans.setAuthorized(true);

            outMsg.setSendWhenSuspended(true);
            outMsg.setTransaction(trans);
            outMsg.setChannel(channel);
//            outMsg.setEndPointTerminal(FinancialEntityService.getIssuerSwitchTerminal(institution));
            outMsg.setEndPointTerminal(ProcessContext.get().getIssuerSwitchTerminal(institution));
            if (outMsg.getEndPointTerminal() == null)
//            	outMsg.setEndPointTerminal(FinancialEntityService.getAcquireSwitchTerminal(institution));
            	outMsg.setEndPointTerminal(ProcessContext.get().getAcquireSwitchTerminal(institution));

            outMsg.setProtocolMessage(isoMsg);
            try {
                outMsg.setIfx(createIfx(outMsg));
                GeneralDao.Instance.saveOrUpdate(outMsg.getIfx());
            } catch (Exception e) {
                logger.error(e);
                throw e;
            }

            trans.setDebugTag(outMsg.getIfx().getIfxType().toString());
            outMsg.setRequest(true);
			outMsg.setNeedToBeSent(true);
			outMsg.setNeedToBeInstantlyReversed(false);
			outMsg.setNeedResponse(true);

            trans.addOutputMessage(outMsg);
//            GeneralDao.Instance.saveOrUpdate(trans.getLifeCycle());
            GeneralDao.Instance.saveOrUpdate(outMsg);
            GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
            GeneralDao.Instance.saveOrUpdate(trans);
            logger.debug("Cutover generated for channel" + channel.getName() + " for date:" + isoMsg.getString(15));
        }
    }

    private Ifx createIfx(Message message) throws ParseException {
        ISOMsg protocolMessage = (ISOMsg) message.getProtocolMessage();
//        MyDateFormat MMdd = new MyDateFormat("MMdd");
        MonthDayDate stlDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", protocolMessage.getString(15)));

        DateTime now = DateTime.now();
        Ifx ifx = new Ifx();
        ifx.setIfxType( IfxType.CUTOVER_RQ);
        ifx.setTerminalType(TerminalType.SWITCH);
        ifx.setTerminalId(message.getEndPointTerminal().getId().toString());
        ifx.setTrnDt(now);
        ifx.setSettleDt( stlDate);
        ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad((protocolMessage).getString(11)));
        ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad((protocolMessage).getString(11)));
        ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());
        ifx.setFwdBankId(protocolMessage.getString(33));
        ifx.setDestBankId(protocolMessage.getString(33));
        ifx.setBankId(protocolMessage.getString(32));
        ifx.setIfxDirection(IfxDirection.OUTGOING);
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setMode(KeyManagementMode.ISSUER_PIN);
        ifx.setCheckDigit("0000");
        ifx.setNetworkManagementInformationCode(NetworkManagementInfo.CUTOVER);
        ifx.getKeyManagement().setKey("0000000000000000");
        ifx.setOrigDt(now);
        return ifx;
    }

}
