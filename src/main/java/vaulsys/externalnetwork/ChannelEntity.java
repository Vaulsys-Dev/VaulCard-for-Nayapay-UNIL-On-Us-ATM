package vaulsys.externalnetwork;

/**
 * Created by HP on 10/28/2016.
 */
public class ChannelEntity {
    private String Channel_ID;
    private String Command;

    public ChannelEntity()
    {}

    public String getChannelID()
    {
        return this.Channel_ID;
    }

    public void setChannel_ID(String Channel_ID)
    {
        this.Channel_ID = Channel_ID;
    }

    public String getCommand()
    {
        return this.Command;
    }

    public void setCommand(String Core_Command)
    {
        this.Command = Core_Command;
    }
}
