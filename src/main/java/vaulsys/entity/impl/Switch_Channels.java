package vaulsys.entity.impl;

import vaulsys.persistence.IEntity;

import javax.persistence.*;

/**
 * Created by a.shehzad on 6/4/2016.
 */

@Entity
@Table(name = "config_channels")
@PrimaryKeyJoinColumn(name = "CHANNEL_ID")

public class Switch_Channels implements IEntity<String>
{
    @Id
    @Column(name="CHANNEL_ID")
    private String CHANNEL_ID;

    @Column(name="CHANNEL_NAME")
    private String Channel_Name;


    public Switch_Channels()
    {
    }

    public Switch_Channels(String Channel_ID, String Channel_Name)
    {
        this.CHANNEL_ID = Channel_ID;
        this.Channel_Name = Channel_Name;
    }

    public String getCHANNEL_ID()
    {
        return this.CHANNEL_ID;
    }

    public String getChannel_Name()
    {
        return this.Channel_Name;
    }

    @Override
    public String getId() {
        return CHANNEL_ID;
    }

    @Override
    public void setId(String Channel_ID) {
        this.CHANNEL_ID = Channel_ID;
    }
}
