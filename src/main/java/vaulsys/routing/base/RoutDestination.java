package vaulsys.routing.base;

import java.io.Serializable;

public class RoutDestination implements Serializable {
    public static final String DESTINATION = "Destination";
    public static final String NAME = "name";
    public static final String ID = "Id";
    public static final String CHANNEL = "Channel";
    public static final String PORT = "Port";
    public static final String CHANNELID = "Id";
    public static final String CHANNELDIRECTION = "Direction";
    public static final String PROTOCOL = "Protocol";
    public static final String ENDPOINT = "EndPoint";


    public static Long lastId = 0L;

    private Long id;
    private String name;

    private String channelName;
    private int channelId;
    private int channelPort;
    private String endPointCode;
    private String endPointType;

    public static Long getLastId() {
        lastId++;
        return lastId;
    }

    public RoutDestination(Long id) {
        super();
        this.id = id;
    }

    public RoutDestination() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public int getChannelPort() {
        return channelPort;
    }

    public void setChannelPort(int channelPort) {
        this.channelPort = channelPort;
    }

    public String getEndPointCode() {
        return endPointCode;
    }

    public void setEndPointCode(String endPointCode) {
        this.endPointCode = endPointCode;
    }

    public String getEndPointType() {
        return endPointType;
    }

    public void setEndPointType(String endPointType) {
        this.endPointType = endPointType;
    }

/*
    public BusElement toXML() {
        BusElement result = new BusElement(RoutDestination.DESTINATION);
        result.setAttribute(RoutDestination.NAME, this.name);

        // TODO here we can get the channel from bus and then call channel.toXML

        BusElement channel = new BusElement(Channel.CHANNEL);
        channel.setAttribute(Channel.NAME, this.channelName);
        channel.setAttribute(Channel.ID, this.channelId);
        channel.setAttribute(RoutDestination.PORT, this.channelPort);

        result.addBusElement(channel);

        return result;
    }


    public boolean validate() {
        // TODO Auto-generated method stub
        if (this.id == null || this.channelId == 0 || this.channelPort == 0 ){
            return false;
        }

        return true;
    }
*/
}
