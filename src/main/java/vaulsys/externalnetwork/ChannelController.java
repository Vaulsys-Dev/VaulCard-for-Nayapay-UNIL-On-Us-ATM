package vaulsys.externalnetwork;

import vaulsys.persistence.BaseEntity;

import javax.persistence.*;

/**
 * Created by HP on 10/27/2016.
 */
@Entity
@Table(name = "CONF_CHANNELS")
@PrimaryKeyJoinColumn(name = "CHANNEL_ID")
public class ChannelController extends BaseEntity<String> {
    @Id
    private String CHANNEL_ID;
    private String CHANNEL_NAME;
    private String IP;
    private String PORT;
    private String Status;

    public void ChannelController()
    {}

    public String getCHANNEL_ID()
    {
        return this.CHANNEL_ID;
    }

    public void setCHANNEL_ID(String Channel_ID)
    {
        this.CHANNEL_ID = Channel_ID;
    }

    public String getCHANNEL_NAME()
    {
        return this.CHANNEL_NAME;
    }

    public void setCHANNEL_NAME(String Channel_Name)
    {
        this.CHANNEL_NAME = Channel_Name;
    }

    public String getIP()
    {
        return this.IP;
    }

    public void setIP(String ip)
    {
        this.IP = ip;
    }

    public String getPORT()
    {
        return this.PORT;
    }

    public void setPORT(String port)
    {
        this.PORT = port;
    }

    public String getStatus()
    {
        return this.Status;
    }

    public void setStatus(String Status)
    {
        this.Status = Status;
    }

    @Override
    public String getId() {
        return this.CHANNEL_ID;
    }

    @Override
    public void setId(String id) {
        this.CHANNEL_ID = id;
    }
}
