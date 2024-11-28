package vaulsys.protocols.base;

import java.util.HashMap;
import java.util.Map;

public class ProtocolProvider {
    private Map<String, String> protocols = new HashMap<String, String>();
    private Map<String, String> backprotocols = new HashMap<String, String>();
    private Map<String, String> protocolNames = new HashMap<String, String>();
    private Map<String, Protocol> protocolObjects = new HashMap<String, Protocol>();
    public static final ProtocolProvider Instance = new ProtocolProvider();

    private ProtocolProvider() {

    }

//    public static ProtocolProvider getInstance() {
//        if (ProtocolProvider.provider == null) {
//            ProtocolProvider.provider = new ProtocolProvider();
//        }
//        return ProtocolProvider.provider;
//    }

    public void init() {

    }

    public void addProtocol(String name, String pName, String path) throws Exception {
        protocols.put(name, path);
        backprotocols.put(path, pName);
        protocolNames.put(pName, name);
        protocolObjects.put(pName, (Protocol) (Class.forName(protocols.get(name)).getConstructor(String.class).newInstance(pName)));
    }

    public Protocol getByClass(Class<? extends Protocol> protocolClass) {
        try {

//		for (String pname: protocolNames.keySet()){
//		    if (protocols.get(protocolNames.get(pname)).equals(protocolClass.getName()) ){
//			return (Protocol)protocolClass.getConstructor(String.class).newInstance(pname); 
//		    }
//		}
            //System.out.println("ProtocolProvider:: protocolClass.getName() [" + protocolClass.getName() + "]"); //Raza TEMP
            return protocolObjects.get(backprotocols.get(protocolClass.getName()));
//		return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Protocol getByName(String protocolName) {
        try {
            return protocolObjects.get(protocolName);
        } catch (Exception e) {
            return null;
        }
    }
}
