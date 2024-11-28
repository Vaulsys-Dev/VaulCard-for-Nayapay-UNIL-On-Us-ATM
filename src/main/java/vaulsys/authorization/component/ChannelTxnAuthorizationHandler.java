package vaulsys.authorization.component;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.impl.NetworkAuthorization;
import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

/**
 * Created by HP on 24-Oct-17.
 */

public class ChannelTxnAuthorizationHandler extends BaseHandler {
    private transient Logger logger = Logger.getLogger(ChannelTxnAuthorizationHandler.class);
    public static final ChannelTxnAuthorizationHandler Instance = new ChannelTxnAuthorizationHandler();

    private ChannelTxnAuthorizationHandler(){}

    @Override
    public void execute(ProcessContext processContext) throws Exception {

        String inchannel = processContext.getInputMessage().getChannel().getName();
        String outchannel = ((Channel)processContext.getOutputChannel(null)).getName();

        if(!Util.hasText(inchannel) || !Util.hasText(outchannel))
        {
            logger.error("Unable to find Txn Source/Destination Channel");
            processContext.getInputMessage().getIfx().setRsCode(ISOResponseCodes.TRANSACTION_TIMEOUT);
            throw new AuthorizationException();
        }

        if(!NetworkAuthorization.AuthorizeTxn(inchannel,outchannel,processContext.getInputMessage().getIfx().getTrnType()))
        {
            logger.error("Transaction [" + processContext.getInputMessage().getIfx().getTrnType() + "] not allowed on Source [" + inchannel   + "] Destination [" + outchannel + "]");
            processContext.getInputMessage().getIfx().setRsCode(ISOResponseCodes.TRANSACTION_TIMEOUT);
            throw new AuthorizationException();
        }

//        if(!(((Channel)processContext.getOutputChannel(null)).getTransactions().contains(processContext.getInputMessage().getIfx().getTrnType().toString())))
//        {
//            logger.error("Transaction not allowed on OutGoing Channel");
//            throw new AuthorizationException();
//        }

        //Note: case of srcChannel --> DestChannel i.e if txn from channel1


    }

//    @Id
//    private
//    Long id;
//
//    private String channel;
//
//    private String transactions;
//
//    @Transient
//    public static HashMap ChannelPerm = new HashMap();
//
//    @Override
//    public Long getId() {
//        return id;
//    }
//
//    @Override
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getChannel() {
//        return channel;
//    }
//
//    public void setChannel(String channel) {
//        this.channel = channel;
//    }
//
//    public String getTransactions() {
//        return transactions;
//    }
//
//    public void setTransactions(String transactions) {
//        this.transactions = transactions;
//    }

//    public static void loadPermissions()
//    {
//        String query = "select channel, description from CMS_CUSTSTCODES";
//        List<Object[]> templist;
//        templist = GeneralDao.Instance.executeSqlQuery(query);
//
//        for (Object[] obj : templist) {
//            //System.out.println("Cust Object Key [" + obj[0] + "]"); //Raza TEMP
//            //System.out.println("Cust Object Value [" + obj[1] + "]"); //Raza TEMP
//            ChannelPerm.put(""+obj[0],""+obj[1]); //Map made for CustCodes
//            //CustStausMap.put(obj. getCode(), obj.Description);
//        }
//    }


}
