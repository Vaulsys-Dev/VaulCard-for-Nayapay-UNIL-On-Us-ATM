package vaulsys.security.hsm.payshield9000;



import vaulsys.security.hsm.base.HSMChannel;
import vaulsys.security.hsm.base.HSMChannelManager;
import vaulsys.security.hsm.base.HSMConnector;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PayShield9000Driver {



   private Logger logger= Logger.getLogger(PayShield9000Driver.class);

    private static PayShield9000Driver payShield9000Driver=null;
    private static List<HSMChannel> hsmChannels;




    private Map<String,HSMConnector> hsmConnectors=new ConcurrentHashMap<String, HSMConnector>();



    private PayShield9000Driver(){
            configDriver();
            initPayShieldConnection();

    }



    public static PayShield9000Driver getInstance() {
        if (payShield9000Driver == null) {
            payShield9000Driver = new PayShield9000Driver();
        }
        return payShield9000Driver;
    }

    public  void configDriver() {
        hsmChannels = HSMChannelManager.getInstance().readFromConfig();

    }

    private void initPayShieldConnection() {

//        for(HSMChannel hsmChannel : hsmChannels){
//            List<IoFilter> filters = new ArrayList<IoFilter>();
//            filters.add(hsmChannel.getIoFilterObject());
//            HSMConnector hsmconnector =new HSMConnector("",new HSMIOHandler(),hsmChannel.getIP(),hsmChannel.getPort(), filters);
//            hsmConnectors.put(hsmChannel.getName(),hsmconnector);
//            hsmconnector.connect();
//        }

    }


    public void test() {
    }
}
