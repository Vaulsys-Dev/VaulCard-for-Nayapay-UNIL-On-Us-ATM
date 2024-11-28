package vaulsys.network.remote;

import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.ui.MessageObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class ReadWriteRemoteMessage extends Thread {
    private final static Logger logger = Logger.getLogger(RemoteMessageManager.class);
    private Socket socket;

    public ReadWriteRemoteMessage(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run()  {

        super.run();
        RemoteMessage response = new RemoteMessage();
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            Object object = ois.readObject();
            oos = new ObjectOutputStream(socket.getOutputStream());
            RemoteMessage msg = (RemoteMessage) object;
            logger.info("Received Message: " + msg);

            switch (msg.getType()) {
                case StartChannel:
                    UtilityRemoteMessage.getInstance().startChannel((String) msg.getRequestObject());
                    break;

                case StopChannel:
                    UtilityRemoteMessage.getInstance().stopChannel((String) msg.getRequestObject());
                    break;

                case RestartChannel:
                    UtilityRemoteMessage.getInstance().stopChannel((String) msg.getRequestObject());
                    UtilityRemoteMessage.getInstance().startChannel((String) msg.getRequestObject());
                    break;

                case ChannelList:
                    response.setResponseObject(UtilityRemoteMessage.getInstance().getChannelList());
                    break;

                case UpdateChannel:
                    UtilityRemoteMessage.getInstance().updateChannel((Channel) msg.getRequestObject());
                    break;

                case IssueShetabDocument:
                    response.setResponseMessage(UtilityRemoteMessage.getInstance().issueShetabDocument((byte[]) msg.getRequestObject()));
                    break;

                case TerminalIssueDocument:
                    Long settlementDataId = (Long) msg.getRequestObject();
                    OnlineSettlementService.Instance.generatedAndPutNeginSettlement(settlementDataId);
                    break;

                case UpdateCache:
                    UtilityRemoteMessage.getInstance().updateCache(msg.getRequestObject().toString());
                    break;

                case Response:

                case ATMMessage:
                    MessageObject mo = (MessageObject) msg.getRequestObject();
                    MessageObject moTemp = new MessageObject();
					/*if(!mo.getIfxType().equals(IfxType.ATM_GO_OUT_OF_SERVICE) && !mo.getIfxType().equals(IfxType.ATM_GO_IN_SERVICE) ){
						moTemp.setParameters((HashMap<String, Serializable>) mo.getParameters());
						moTemp.setResponseCode(mo.getResponseCode());
						moTemp.setStartDateTime(mo.getStartDateTime());
						moTemp.setUsername(mo.getUsername());
						moTemp.setIfxType(IfxType.ATM_GO_OUT_OF_SERVICE);
						readWriteMessageObject(moTemp);
					}*/
                    UtilityRemoteMessage.getInstance().readWriteMessageObject(mo);
                    break;
                case Exception:
                    throw new RuntimeException("Wrong Input Message Type");
            }
            response.setType(vaulsys.network.remote.MessageType.Response);
            oos.writeObject(response);
        } catch (RemoteMessageExcaption e) {
            response.setType(vaulsys.network.remote.MessageType.Exception);
            response.setResponseMessage(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("Reading RemoteMessage from ServerSocket", e);
        } catch (InstantiationException e) {
            logger.error("", e);
        } catch (IllegalAccessException e) {
            logger.error("", e);
        }catch (Exception e) {
            logger.error("",e);
            // TODO: handle exception
        } finally {
            try{
                oos.close();
                ois.close();
            }catch (Exception ee){
                logger.error("error in close connection" + ee, ee);
            }
        }

    }
}
