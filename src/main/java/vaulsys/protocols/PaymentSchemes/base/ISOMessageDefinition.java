package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by HP on 11/11/2016.
 */

@Entity
@Table(name = "iso_message_definition")
public class ISOMessageDefinition implements IEntity<Long> {

    @Id
    private Long Id;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "field_id")
    private String fieldId;

    @Column(name = "require")
    private String require;

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getRequire() {
        return require;
    }

    public void setRequire(String require) {
        this.require = require;
    }
}
