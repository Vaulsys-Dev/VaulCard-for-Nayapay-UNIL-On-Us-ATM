package vaulsys.network.channel.base;

/**
 * Created by Raza on 12-Mar-19.
 */

public class NetworkType {

    private String type;

    private static final String CHANNEL_IN_VALUE = "in";
    private static final String CHANNEL_OUT_VALUE = "out";



    public static final NetworkType CHANNEL_SERVER_IN = new NetworkType(CHANNEL_IN_VALUE);
    public static final NetworkType CHANNEL_CLIENT_OUT = new NetworkType(CHANNEL_OUT_VALUE);



    public NetworkType(String Type)
    {
        this.type = Type;
    }

    @Override
    public String toString()
    {
        return this.type+"";
    }


}
