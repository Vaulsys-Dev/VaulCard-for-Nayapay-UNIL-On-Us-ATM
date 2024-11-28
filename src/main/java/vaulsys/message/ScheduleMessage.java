package vaulsys.message;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "S")
public class ScheduleMessage extends Message {
    private String messageType;
    private String responseCode;
    private Long amount;
    
    public ScheduleMessage() {
        super();
//        request = true;
//        setStartDateTime(DateTime.now());
    }

    public ScheduleMessage(String messageType, Long amount) {
        super(MessageType.SCHEDULE);
        request = true;
        needToBeSent = true;
        needResponse = false;
        needToBeInstantlyReversed = false;
        this.messageType = messageType;
//        this.type = MessageType.SCHEDULE;
        this.amount = amount;
    }
    
	public ScheduleMessage(MessageType type) {
		super(type);
	}

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Long getAmount() {
		return amount;
	}
}