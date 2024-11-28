package vaulsys.network.channel.base;

/**
 * Created by Raza on 12-Mar-19.
 */

public class ChannelType {

    private String type;

    private static final String CHANNEL_VALUE = "Channel";
    private static final String WEBSERVER_VALUE = "Webserver";
    private static final String HSM_VALUE = "HSM";



    public static final ChannelType CHANNEL = new ChannelType(CHANNEL_VALUE);
    public static final ChannelType WEBSERVER = new ChannelType(WEBSERVER_VALUE);
    public static final ChannelType HSM = new ChannelType(HSM_VALUE);


    public ChannelType(String Type)
    {
        this.type = Type;
    }

    @Override
    public String toString()
    {
        return this.type+"";
    }


}
