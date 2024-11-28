package vaulsys.protocols.PaymentSchemes.EMV;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by m.rehman on 6/16/2016.
 */
@Entity
@Table(name = "channel_emv_tags")
public class ChannelEmvTags implements IEntity<Long> {

    @Id
    private Long Id;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "emv_tag")
    private String emvTag;

    @Column(name = "sequence")
    private Integer emvTagSequence;

    public ChannelEmvTags() { }

    public ChannelEmvTags(String channelId, String emvTag, Integer emvTagSequence) {
        this.channelId = channelId;
        this.emvTag = emvTag;
        this.emvTagSequence = emvTagSequence;
    }

    @Override
    public Long getId() {
        return Id;
    }

    @Override
    public void setId(Long id) {
        Id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getEmvTag() {
        return emvTag;
    }

    public void setEmvTag(String emvTag) {
        this.emvTag = emvTag;
    }

    public Integer getEmvTagSequence() {
        return emvTagSequence;
    }

    public void setEmvTagSequence(Integer emvTagSequence) {
        this.emvTagSequence = emvTagSequence;
    }
}
