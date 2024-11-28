package vaulsys.netmgmt.component;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.OutputChannel;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionType;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public class EchoMessageGenerator {

    transient static Logger logger = Logger.getLogger(EchoMessageGenerator.class);

    private EchoMessageGenerator() {}
    
    public static void generateEchoMessage(OutputChannel channel) {
        try {
//            OutgoingMessage echoMsg = new OutgoingMessage();
        	Message echoMsg = new Message(MessageType.OUTGOING);
        	Transaction transaction = new Transaction(TransactionType.SELF_GENERATED);
        	transaction.addOutputMessage(echoMsg);
        	echoMsg.setTransaction(transaction);
            echoMsg.setIfx(null);
            //m.rehman: passing channel for channel wise echo message
//            ISOMsg isoMsg = generateIsoEchoMessage();
            ISOMsg isoMsg = generateIsoEchoMessage(channel);
            echoMsg.setProtocolMessage(isoMsg);
            echoMsg.setChannel(channel);

            byte[] binary = channel.getProtocol().getMapper().toBinary(isoMsg);
            echoMsg.setBinaryData(binary);


            MessageManager.getInstance().putResponse(echoMsg);
        } catch (Exception ex) {
            logger.error("Exception in creating echo message."+ ex , ex);
        }
    }

    private static ISOMsg generateIsoEchoMessage(Channel channel) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();

        //m.rehman: echo message for onelink
        if (channel.getChannelId().equals(ChannelCodes.ONELINK)) {
            isoMsg.setMTI(String.valueOf(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87));
            isoMsg.set(7, Long.toString(DateTime.now().getDateTimeLong()).substring(4, 14));
            isoMsg.set(11, Util.generateTrnSeqCntr(6));
            isoMsg.set(24, "001");
            isoMsg.set(70, Integer.toString(NetworkManagementInfo.ECHOTEST_1LINK.getType()));
        } else {
            isoMsg.setMTI(String.valueOf(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87));
            isoMsg.set(11, Util.generateTrnSeqCntr(6));
            isoMsg.set(48, 0);
            isoMsg.set(70, NetworkManagementInfo.ECHOTEST);
        }

        return isoMsg;
    }

}
