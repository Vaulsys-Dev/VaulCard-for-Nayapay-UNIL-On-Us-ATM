package vaulsys.netmgmt.extended;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.mina2.Mina2Connector;
import vaulsys.network.mina2.Mina2IoHandler;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.hsm.base.HSMConnector;
import vaulsys.security.hsm.base.HSMIOHandler;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import javax.ws.rs.core.MediaType;

public class ConnectionManager implements Runnable{

    private Mina2Connector Minaconnector;
    private boolean IsServer = false;
    private boolean IsWebServer = false;
    private IoSession Tsession;
    private Channel Tchannel;
    public static Thread[] ConnectManager;
    public static int ConnectManagerIndex = 0;
    Logger logger = Logger.getLogger(this.getClass());
    private HSMConnector Hsmconnector;


    public ConnectionManager(Mina2Connector connector) //For ME Client
    {
        this.IsServer = true;
        this.Minaconnector = connector;
        //if(connector.getSession() != null && !connector.getSession().isConnected())
        //{
        //  connector.connect();
        //}
    }

    public ConnectionManager(HSMConnector hsmconnector) //For ME Client
    {
        this.IsServer = true;
        this.Hsmconnector = hsmconnector;
        //if(connector.getSession() != null && !connector.getSession().isConnected())
        //{
        //  connector.connect();
        //}
    }

    public ConnectionManager(Channel channel) //For WebServer
    {
        this.IsServer = false;
        this.IsWebServer = true;
        this.Tchannel= channel;
    }

    public ConnectionManager(IoSession session, Channel channel) //For ME Server
    {
        this.IsServer = false;
        Tsession = session;
        Tchannel = channel;
    }


    public void run() {
        try {
            while (true)
            {
            if (IsServer) {
                if (Minaconnector != null) {
                    logger.info("Thread running for [" + Minaconnector.getChannel().getName() + "]");
                    //System.out.println("Thread running for [" + Minaconnector.getChannel().getName() + "]");
                } else if (Hsmconnector != null) {
                    logger.info("Thread running for [" + Hsmconnector.getChannel().getName() + "]");
                    //System.out.println("Thread running for [" + Hsmconnector.getChannel().getName() + "]");
                }
                //System.out.println("****************************I AM ALIVE...!****************************");
                //if ((Minaconnector.getSession() != null) && !Minaconnector.getSession().isConnected()) { //Raza commenitng due to null exception on 2nd condition
                //if ((Minaconnector != null) && (Minaconnector.getSession() != null) && (!Minaconnector.getSession().isConnected())) { //Raza commenting
                if ((Minaconnector != null)) {

                    Thread.sleep(Minaconnector.getChannel().getIdleTimeSec() * 1000);

                    logger.info("Checking Connectivity of [" + Minaconnector.getChannel().getName() + "]"); //Raza adding for KEENU

                    if ((Minaconnector.getSession() == null) || (!Minaconnector.getSession().isConnected())) {
                        //System.out.println("Going to RECONNECT...! [" + Minaconnector.getChannel().getName() + "]");
                        logger.info("Going to RECONNECT...! [" + Minaconnector.getChannel().getName() + "] IP [" + Minaconnector.getChannel().getIp() + "] Port [" + Minaconnector.getChannel().getPort() + "]");

                        //Minaconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
                        //Minaconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_DISABLED);
                        if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) //Raza start updating status in DB
                        {
                            Minaconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
                            Minaconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_DISABLED);
                            GeneralDao.Instance.saveOrUpdate(Minaconnector.getChannel());
                        } else {
                            GeneralDao.Instance.beginTransaction();
                            Minaconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
                            Minaconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_DISABLED);
                            GeneralDao.Instance.saveOrUpdate(Minaconnector.getChannel());
                            GeneralDao.Instance.endTransaction();
                        } //Raza end updating status in DB

                        //Mina2Connector tempconnector = Minaconnector;
                        //Minaconnector = new Mina2Connector(tempconnector.getChannel(), tempconnector.getFilters(), new Mina2IoHandler());
                        Minaconnector.connect(); //Raza commenting as it is done in above getSession Check
                        //Minaconnector.reconnect(); //Raza commenting
                    }
                    else if (Minaconnector.getChannel().getConnectionStatus().equals(NetworkInfoStatus.SOCKET_DISCONNECTED)) //case of switch start when Session is not connected at thread but got connected meanwhile
                    {
                        if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) //Raza start updating status in DB
                        {
                            Minaconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_CONNECTED);
                            if (!Minaconnector.getChannel().getSignonreq()) { //Raza For Channels where SIGNON is not supported
                                Minaconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED);
                            }
                            GeneralDao.Instance.saveOrUpdate(Minaconnector.getChannel());
                        } else {
                            GeneralDao.Instance.beginTransaction();
                            Minaconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_CONNECTED);
                            if (!Minaconnector.getChannel().getSignonreq()) { //Raza For Channels where SIGNON is not supported
                                Minaconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED);
                            }
                            GeneralDao.Instance.saveOrUpdate(Minaconnector.getChannel());
                            GeneralDao.Instance.endTransaction();
                        } //Raza end updating status in DB
                    }
                    //Thread.sleep(Minaconnector.getChannel().getIdleTimeSec() * 1000);
                }
                else if ((Hsmconnector != null))    //Raza for HSM
                {
                    Thread.sleep(Hsmconnector.getChannel().getIdleTimeSec() * 1000);
                    logger.info("Checking Connectivity of [" + Hsmconnector.getChannel().getName() + "]"); //Raza adding for KEENU
                    /*
                    logger.info(Hsmconnector.getChannel().getName() + "BothIdle [" + Hsmconnector.getSession().isBothIdle() + "]"); //Raza adding for KEENU
                    logger.info(Hsmconnector.getChannel().getName() + "ReadIdle [" + Hsmconnector.getSession().isReaderIdle() + "]"); //Raza adding for KEENU
                    logger.info(Hsmconnector.getChannel().getName() + "WriteIdle [" + Hsmconnector.getSession().isWriterIdle() + "]"); //Raza adding for KEENU
                    logger.info(Hsmconnector.getChannel().getName() + "ReadSuspended [" + Hsmconnector.getSession().isReadSuspended() + "]"); //Raza adding for KEENU
                    logger.info(Hsmconnector.getChannel().getName() + "WriteSuspended [" + Hsmconnector.getSession().isWriteSuspended() + "]"); //Raza adding for KEENU
                    */
                    if (Hsmconnector.getSession() != null && Hsmconnector.getSession().isBothIdle()) //Raza adding for Halt issue 20-06-2019
                    {
                        logger.info("Both Idle acheived, reconnecting...");
                        Hsmconnector.reconnect();
                        //return;
                    }
                    else
                    {

                    //Hsmconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);


                    if ((Hsmconnector.getSession() == null) || (!Hsmconnector.getSession().isConnected())) {

                        //System.out.println("Going to RECONNECT HSM...! [" + Hsmconnector.getChannel().getName() + "]");
                        logger.info("Going to RECONNECT HSM...! [" + Hsmconnector.getChannel().getName() + "]");
                        if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) //Raza start updating status in DB
                        {
                            Hsmconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
                            Hsmconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_DISABLED);
                            GeneralDao.Instance.saveOrUpdate(Hsmconnector.getChannel());
                        } else {
                            GeneralDao.Instance.beginTransaction();
                            Hsmconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
                            Hsmconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_DISABLED);
                            GeneralDao.Instance.saveOrUpdate(Hsmconnector.getChannel());
                            GeneralDao.Instance.endTransaction();
                        } //Raza end updating status in DB

                        //HSMConnector tempconnector = Hsmconnector;
                        //Hsmconnector = new HSMConnector(new HSMIOHandler(), tempconnector.getFilters(), tempconnector.getChannel());
                        Hsmconnector.connect(); //Raza maybe use reconnect
                    } else if (Hsmconnector.getChannel().getConnectionStatus().equals(NetworkInfoStatus.SOCKET_DISCONNECTED)) //case of switch start when Session is not connected at thread but got connected meanwhile
                    {
                        if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) //Raza start updating status in DB
                        {
                            Hsmconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_CONNECTED);
                            if (!Hsmconnector.getChannel().getSignonreq()) {
                                Hsmconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED); //set proc enabled for HSM no SIGNON req
                            }
                            GeneralDao.Instance.saveOrUpdate(Hsmconnector.getChannel());
                        } else {
                            GeneralDao.Instance.beginTransaction();
                            Hsmconnector.getChannel().setConnectionStatus(NetworkInfoStatus.SOCKET_CONNECTED);
                            if (!Hsmconnector.getChannel().getSignonreq()) {
                                Hsmconnector.getChannel().setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED); //set proc enabled for HSM no SIGNON req
                            }
                            GeneralDao.Instance.saveOrUpdate(Hsmconnector.getChannel());
                            GeneralDao.Instance.endTransaction();
                        } //Raza end updating status in DB
                    }
                    //Thread.sleep(Hsmconnector.getChannel().getIdleTimeSec() * 1000);
                }
                }
            } else if (!IsWebServer) {
                logger.info("Thread running for [" + Tchannel.getName() + "]");
                    if (Tsession != null && !Tsession.isConnected()) {
                        //System.out.println("Going to Set COMMS DOWN for [" + Tchannel.getName() + "]");
                    /*if(!(NetworkManager.GetCommsStatus(Tchannel) == NetworkInfoStatus.SOCKET_DISCONNECTED)) {
                        NetworkManager.SetCommsDOWN(Tchannel);
                    }*/
                        if (!(Tchannel.getConnectionStatus() == NetworkInfoStatus.SOCKET_DISCONNECTED)) {
                            Tchannel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
                        }
                    }
                Thread.sleep(Tchannel.getIdleTimeSec()*1000);
            } else {
                logger.info("Thread running for WebServer [" + Tchannel.getName() + "], pinging...");
                Client client = null;
                try {
                    WebResource webResource = null;
                    client = Client.create();
                    client.setReadTimeout(Tchannel.getReadtimeout());
                    client.setConnectTimeout(Tchannel.getConnecttimeout());
                    logger.info("pinging [" + "http://" + Tchannel.getIp() + ":" + Tchannel.getPort() + Tchannel.getWebserviceURL() + "ping" + "]");
                    webResource = client.resource("http://" + Tchannel.getIp() + ":" + Tchannel.getPort() + Tchannel.getWebserviceURL() + "ping");
                    String Resp = webResource.type(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .get(String.class);
                    logger.info("Thread Response [" + Resp + "] for WebServer [" + Tchannel.getName() + "]");
                } catch (Exception e1) {
                    logger.error("Exception caught while pinging WebServer [" + Tchannel.getName() + "]");
                    client.destroy();
                }
                Thread.sleep(Tchannel.getIdleTimeSec()*1000);
            }

        }
        } catch (Exception e) {
            logger.error("Exception caught while checking connectivity [" + e.getMessage() + "]");
            e.printStackTrace();
        }
    }

    public static void ManageConnection(Thread thread)
    {
        boolean IsAdded = false;
        for(int i=0 ; i<ConnectionManager.ConnectManager.length ; i++)
        {
            if(ConnectionManager.ConnectManager[i] != null) {
                //System.out.println("THREAD ARRAY at [" + i + "] = [" + ConnectionManager.ConnectManager[i].getName() + "] -- new Thread [" + thread.getName() + "]"); //Raza TEMP
                if (ConnectionManager.ConnectManager[i].getName().equals(thread.getName())) {
                    IsAdded = true;
                    ConnectionManager.ConnectManager[i] = thread; //Update Thread Object
                }
            }
        }

        //System.out.println("IS - ADDED [" + IsAdded + "]"); //Raza TEMP

        if(!IsAdded)
        {
            ConnectionManager.ConnectManager[ConnectManagerIndex] = thread;
            ConnectManagerIndex++;
            thread.start();
        }
        else
        {
            //System.out.println("Thread already added for [" + thread.getName() + "]");
            //thread.destroy(); //Raza Destroy new object as it is already being managed before
            thread = null;
        }
    }

}
