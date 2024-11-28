package vaulsys.routing.components;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.routing.exception.NoRoutingDestinationException;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class RoutingHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(RoutingHandler.class);

    public static final RoutingHandler Instance = new RoutingHandler();

    private RoutingHandler(){
    }

    @Override
    public void execute(ProcessContext processContext) throws Exception {
        try {
            /*
            Change by: m.rehman: Need to change the below routine to get the destination from DB loaded map
             */
            String channelname, bin, mti, instid, trancode, termtype, trntype, destbnkid, bnkid, recbnkid, key, destination, refInstId;
            Ifx ifxObj;
            Channel channel;

            channelname = processContext.getInputMessage().getChannel().getName();
            if(!Util.hasText(channelname))
            {
                channelname = "*";
            }

            ifxObj = processContext.getInputMessage().getIfx();

            //m.rehman: pan info not available for Settlement and Batch messages for Keenu
            bin = (Util.hasText(ifxObj.getAppPAN())) ? ifxObj.getAppPAN().substring(0, 6) : "";
            if (!Util.hasText(bin)) {
                bin = "*";
            }

            mti = ifxObj.getMti();
            if(!Util.hasText(mti))
            {
                mti = "*";
            }

            instid = ifxObj.getInstitutionId();
            if(!Util.hasText(instid))
            {
                instid = "*";
            }
            trancode = "*"; //Raza verify this with respect to TranType

            termtype = ""+ifxObj.getTerminalType().getCode();
            if(!Util.hasText(termtype))
            {
                termtype = "*";
            }

            destbnkid = ifxObj.getDestBankId();
            if(!Util.hasText(destbnkid))
            {
                destbnkid = "*";
            }

            bnkid = ifxObj.getBankId();
            if(!Util.hasText(bnkid))
            {
                bnkid = "*";
            }

            recbnkid = ifxObj.getRecvBankId();
            if(!Util.hasText(recbnkid))
            {
                recbnkid = "*";
            }
            //else { //Raza commenting assign directly to bin from substring
            //    bin = bin.substring(0, 6);
            //}
            //key = channelname + bin + mti + instid + trancode + termtype + trntype + destbnkid + bnkid + recbnkid; //Raza verify trntype
            /*int NoOfKeys = 10; //Raza Update this implementation
            for(int i=NoOfKeys ; i>0 ; i--)
            {
                key = channelname + bin + mti + instid + trancode + termtype + trntype + destbnkid + bnkid + recbnkid; //Raza verify trntype
                switch (i)
                {
                    case 5:
                    {
                        key = channelname + bin + mti + instid + trancode + termtype + trntype + destbnkid + bnkid + recbnkid; //Raza verify trntype
                    }
                    case 4:
                    {

                    }
                    case 3:
                    {

                    }
                    case 2:
                    {

                    }
                    case 1:
                    {

                    }
                    default:
                    {

                    }
                }
            }*/


            /*
            String[] Keys = new String[NoOfKeys];
            Keys[0] = channelname;
            Keys[1] = bin;
            Keys[2] = mti;
            Keys[3] = instid;
            Keys[4] = trancode;
            Keys[5] = termtype;
            Keys[6] = destbnkid;
            Keys[7] = bnkid;
            Keys[8] = recbnkid;

            key = channelname + bin + mti + instid + trancode + termtype + destbnkid + bnkid + recbnkid;
            //find destination and if destination found
            destination = processContext.getMessageRoutingDestination(key);

            if(destination == null)
            {
                for(int i=NoOfKeys-1 ; i>0 ; i--)
                {
                    if(i==NoOfKeys-1)
                    {

                    }
                }
            }
            */
            key = channelname + bin + mti + instid + trancode + termtype + destbnkid + bnkid + recbnkid;
            destination = processContext.getMessageRoutingDestination(key);

            if(destination == null)
            {
                logger.info("NO Destination found for KEY [" + key + "]...");
                key = channelname + bin + mti + instid + trancode + termtype + destbnkid + bnkid + "*";
                destination = processContext.getMessageRoutingDestination(key);

                if(destination == null)
                {
                    logger.info("NO Destination found for KEY [" + key + "]...");
                    key = channelname + bin + mti + instid + trancode + termtype + destbnkid + "*" + "*";
                    destination = processContext.getMessageRoutingDestination(key);

                        if(destination == null)
                        {
                            logger.info("NO Destination found for KEY [" + key + "]...");
                            key = channelname + bin + mti + instid + trancode + termtype + "*" + "*" + "*";
                            destination = processContext.getMessageRoutingDestination(key);

                            if(destination == null)
                            {
                                logger.info("NO Destination found for KEY [" + key + "]...");
                                key = channelname + bin + mti + instid + trancode + "*" + "*" + "*" + "*";
                                destination = processContext.getMessageRoutingDestination(key);

                                if(destination == null)
                                {
                                    logger.info("NO Destination found for KEY [" + key + "]...");
                                    key = channelname + bin + mti + instid + "*" + "*" + "*" + "*" + "*";
                                    destination = processContext.getMessageRoutingDestination(key);

                                    if(destination == null)
                                    {
                                        logger.info("NO Destination found for KEY [" + key + "]...");
                                        key = channelname + bin + mti + "*" + "*" + "*" + "*" + "*" + "*";
                                        destination = processContext.getMessageRoutingDestination(key);

                                        if(destination == null)
                                        {
                                            logger.info("NO Destination found for KEY [" + key + "]...");
                                            key = channelname + bin + "*" + "*" + "*" + "*" + "*" + "*" + "*";
                                            destination = processContext.getMessageRoutingDestination(key);

                                            if(destination == null) //Raza case of Response w.r.t. mti & Channel
                                            {
                                                logger.info("NO Destination found for KEY [" + key + "]...");
                                                key = channelname + "*" + mti + "*" + "*" + "*" + "*" + "*" + "*";
                                                destination = processContext.getMessageRoutingDestination(key);

                                                if(destination == null)
                                                {
                                                    logger.info("NO Destination found for KEY [" + key + "]...");
                                                    key = channelname + "*" + "*" + "*" + "*" + "*" + "*" + "*" + "*";
                                                    destination = processContext.getMessageRoutingDestination(key);

                                                    if(destination == null)
                                                    {
                                                        logger.info("NO Destination found for KEY [" + key + "]...");
                                                        key = "*" + "*" + "*" + "*" + "*" + "*" + "*" + "*" + "*";
                                                        destination = processContext.getMessageRoutingDestination(key);
                                                    }
                                                    else
                                                    {
                                                        logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                                                    }
                                                }
                                                else
                                                {
                                                    logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                                                }
                                            }
                                            else
                                            {
                                                logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                                            }
                                        }
                                        else
                                        {
                                            logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                                        }
                                    }
                                    else
                                    {
                                        logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                                    }
                                }
                                else
                                {
                                    logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                                }
                            }
                            else
                            {
                                logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                            }
                        }
                        else
                        {
                            logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                        }
                }
                else
                {
                    logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
                }
            }
            else
            {
                logger.info("Destination found [" + destination + "] for KEY [" + key + "]");
            }


            /* //Raza commenting start
            if (ifxObj.getRecvBankId() != null) {
                refInstId = ifxObj.getRecvBankId().toString();

                //set key to find destination
                key = mti + bin + refInstId;

                //find destination and if destination found
                destination = processContext.getMessageRoutingDestination(key);

            } else if (ifxObj.getBankId() != null) {
                refInstId = ifxObj.getBankId().toString();

                //set key to find destination
                key = mti + bin + refInstId;

                //find destination and if destination found
                destination = processContext.getMessageRoutingDestination(key);

            } else {
                refInstId = "*";

                //set key to find destination
                key = mti + bin + refInstId;

                //find destination and if destination found
                destination = processContext.getMessageRoutingDestination(key);

                //for onelink channel, as there is no reference id (receive/forward/acquiring inst id) available
                //in message
                if (!Util.hasText(destination)) {
                    destination = processContext.getInputMessage().getChannel().getChannelId();
                }
            }
            */ //Raza commenting end
            //get destination channel
            //if the card is off us and its a response message than copy out channel of the source channel
            if(destination != null) {
                //if (destination.equals("*")) { //Raza commenting -- No such Routing as * should be defined this is the case of no Routing found for Response & is cattered in below ELSE
                  //  channel = processContext.getChannel(processContext.getTransaction().getFirstTransaction().getInputMessage().getChannel().getName()); //getChannelId()); //Raza using Channel Name //Raza commenting

                    //if (channel == null) //Raza commenting
                      //  channel = processContext.getChannel(processContext.getTransaction().getFirstTransaction().getOutputMessage().getChannel().getName()); //getChannelId()); //Raza commenting

                    //if (channel.getName().contains("In")) //Raza commenting
                        //channel = processContext.getChannel(channel.getOriginatorChannelId()); //Raza commenting

                //} else { //Raza commenting
                    //channel = processContext.getChannel(destination);
                    //System.out.println("Channel ID [" + processContext.getChannelIdbyName(destination) + "]");
                    //System.out.println("Channel [" + processContext.getChannel(processContext.getChannelIdbyName(destination)) + "]");
                    //channel = processContext.getChannel(processContext.getChannelIdbyName(destination));

                if(destination.toUpperCase().equals("ORIGINATOR")) //case of Response
                {
                    channel = processContext.getChannel(processContext.getTransaction().getFirstTransaction().getInputMessage().getChannel().getName()); //in case of Response, get Channel from First Transaction
                    logger.info("Destination found [" + channel.getName() + "]");
                    processContext.setOutputChannel(channel);
                }
                else {
                    channel = processContext.getChannel(destination);
                    //} //Raza commenting

                    if (channel == null) {
                        logger.error("Routing Channel Not found!!!");
                        logger.error("Setting incoming channel as output channel");
                        processContext.setOutputChannel(processContext.getInputMessage().getChannel());
                        logger.error("Setting response to Routing error");
                        ifxObj.setRsCode(ISOResponseCodes.ACCOUNT_LOCKED);
                        //m.rehman
                        throw new NoRoutingDestinationException();
                    } else {
                        logger.info("Routing Channel found!!! " + channel.getName());
                        processContext.setOutputChannel(channel);
                    }
                }

            }
            else
            {
                //channel = processContext.getChannel(processContext.getTransaction().getFirstTransaction().getInputMessage().getChannel().getName()); //in case of Response, get Channel from First Transaction //Raza commenting

                //if (channel == null) //Raza commenting
                    //channel = processContext.getChannel(processContext.getTransaction().getFirstTransaction().getOutputMessage().getChannel().getName()); //getChannelId()); //Raza commenting

                //if (channel != null) //Raza commenting
                //{
                //    if(channel.getName().contains("In")) //Raza commenting
                //    channel = processContext.getChannel(channel.getOriginatorChannelId()); //Raza commenting
                //}
                //else //Raza commenting
                //{
                    logger.error("Setting response to Routing error");
                    ifxObj.setRsCode(ISOResponseCodes.ACCOUNT_LOCKED);

                    //No need to shoot reversal, as acquirer will initiate rev. on timeout
                    //No need to reverse limit, as it will be reversed by reversal

                    throw new NoRoutingDestinationException();
                //}
            }


        } catch (Exception e) {
            throw e;
        }
    }
}
