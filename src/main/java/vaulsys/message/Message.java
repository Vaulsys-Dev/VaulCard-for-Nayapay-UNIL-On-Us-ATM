package vaulsys.message;

import vaulsys.calendar.DateTime;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name="trx_message")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "MSGType", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(value = "M")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class Message implements IEntity<Long> {

    transient Logger logger = Logger.getLogger(Message.class); //Raza adding for logging

    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="msg-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "msg-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "msg_seq")
    				})
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trx", nullable = true, updatable = true)
    @ForeignKey(name="message_trx_fk")
    @Index(name="idx_msg_trx")
    private Transaction transaction;

    @Column(name = "trx", insertable = false, updatable = false)
    private Long transactionId;
    
    @Column(name = "sec_key", length = 8, nullable = true, insertable = true, updatable = true)
    private byte[] securityKey;
    
    
//    @Column(length = 3000)
//    @Lob
    @Transient
    private byte[] binaryData;

    // Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Added header handling for VISA SMS
    @Column(name = "header_data")
    private byte[] headerData;
    // End

//    @Column(name="xml")
//    @Lob
//    private String oldXML;

    private String channelName;
    
    @Transient
    private Channel channel;

    //@Column(name = "channelid") //Raza commenting
    //private String channelId; //Raza commenting

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal")
    @ForeignKey(name="message_term_fk")
    private Terminal endPointTerminal;
    
    @Column(name = "terminal", insertable = false, updatable = false)
    private Long endPointTerminalId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ifx")
    @ForeignKey(name="message_ifx_fk")
//    @Index(name="idx_msg_ifx")
    private Ifx ifx;

    @Column(name = "ifx", insertable = false, updatable = false)
    private Long ifxId;
    
    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "dayDate.date", column = @Column(name = "start_date")),
    @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "start_time"))
            })
    private DateTime startDateTime;


    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "type"))
    protected MessageType type;

    //This flag shows current message is request or response->false:respone; true:request
    protected Boolean request;

    //Message is going to be send
    protected Boolean needToBeSent = true;

    //Message needs response so we set a time-out reverse trigger for it.
    protected Boolean needResponse = false;

    //That we should send message in anyway; if we couldn't send it, it must be reversed instantly
    protected Boolean needToBeInstantlyReversed = false;

    //TODO Noroozi: I don't know the usage of this flag!
    protected Boolean sendWhenSuspended = false;

    @Transient
    private ProtocolMessage protocolMessage;

    transient Set<Message> pendingRequests = null;

    
    @OneToMany(fetch = FetchType.LAZY, mappedBy="msg")
//    @PrimaryKeyJoinColumn
    private List<MessageXML> msgXmls;

    protected Long srcTPDU;

    @Transient
    private String ANI;

    @Transient
    private String DNIS;

    @Transient
    private String LRI;

    
    public Message() {
        super();
    }


    public Message(MessageType type) {
    	this();
    	this.type = type;
    	this.startDateTime = DateTime.now();
    }

    protected Message(Long id, byte[] binaryData, Channel channel, MessageType type) {
        this();
        this.id = id;
        this.binaryData = binaryData;
        this.channel = channel;
        this.channelName = channel.getName();
        this.type = type;
        this.startDateTime = DateTime.now();
    }


    public Message(Transaction transaction, DateTime startDateTime, MessageType type) {
        this();
        this.transaction = transaction;
        this.startDateTime = startDateTime;
        this.type = type;
    }


	public void setProtocolMessage(ProtocolMessage protocolMessage) {
        this.protocolMessage = protocolMessage;
    }

    public ProtocolMessage getProtocolMessage() {
        return protocolMessage;
    }

    public byte[] getBinaryData() {
        return this.binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ifx getIfx() {
        return ifx;
    }

    public void setIfx(Ifx ifx) {
        this.ifx = ifx;
        
        setIfxRelatedData(this.ifx);

        if(isOutgoingMessage()){
    		transaction.setOutgoingIfx(ifx);
    	}else if(this.isIncomingMessage()){
    		transaction.setIncomingIfx(ifx);
    	}

//        if (ifx != null) {
//	        if (this.transaction != null)
//	        	this.ifx.setTransaction(transaction);
//	        
//	        if (this.endPointTerminal != null)
//	        	this.ifx.setEndPointTerminal(endPointTerminal);
//	        
//	        if (this.request != null)
//	        	this.ifx.setRequest(request);
//        }
    }

    public void setIfxRelatedData(Ifx ifx) {
        if (ifx != null) {
	        if (this.transaction != null)
	        	ifx.setTransaction(transaction);
	        
	        if (this.endPointTerminal != null)
	        	ifx.setEndPointTerminal(endPointTerminal);
	        
	        if (this.request != null)
	        	ifx.setRequest(request);
        }
    	
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        if (this.ifx != null)
        	this.ifx.setTransaction(transaction);
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Channel getChannel() { 
    	if(this.channel == null)
    		this.channel = GlobalContext.getInstance().getChannel(this.channelName);
    		//this.channel = ProcessContext.get().getChannel(this.channelId);
    	
        return this.channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        if (channel != null)
        	this.channelName = channel.getName();
    }

    public String getXML() {
    	if(this.msgXmls == null || this.msgXmls.size() == 0){
    		return null;
    	}
    	
    	if(Util.hasText(this.msgXmls.get(0).getXmlPart1()) && Util.hasText(this.msgXmls.get(0).getXmlPart2()))
    		return this.msgXmls.get(0).getXmlPart1() + this.msgXmls.get(0).getXmlPart2();
    	
    	if(Util.hasText(this.msgXmls.get(0).getXmlPart1()))
    		return this.msgXmls.get(0).getXmlPart1();
    	
    	return null;
    }

    public void setXML(String readableXML) {
    	if(!Util.hasText(readableXML))
    		return;
    	
    	MessageXML messageXML;
    	if(this.msgXmls == null){
    		this.msgXmls = new ArrayList<MessageXML>(1);
    		messageXML = new MessageXML(this);
    		this.msgXmls.add(messageXML);
    	}else{
    		messageXML = this.msgXmls.get(0);
    	}
    	
    	if(readableXML.length() > 4000){
    		messageXML.setXmlPart1(readableXML.substring(0, 4000));
    		if(readableXML.length() > 8000)
    			messageXML.setXmlPart2(readableXML.substring(4000, 8000));
    		else
    			messageXML.setXmlPart2(readableXML.substring(4000));
    	}else{
    		messageXML.setXmlPart1(readableXML);
    	}
    }

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	
	public Boolean getRequest() {
		return request;
	}


	public void setRequest(Boolean request) {
		this.request = request;
		if (this.ifx != null)
			this.ifx.setRequest(request);
	}


	public Boolean getNeedToBeSent() {
		return needToBeSent;
	}

	public void setNeedToBeSent(Boolean needToBeSent) {
		this.needToBeSent = needToBeSent;
	}

	public Boolean getNeedResponse() {
		return needResponse;
	}

	public void setNeedResponse(Boolean needResponse) {
		this.needResponse = needResponse;
	}

	public Boolean getSendWhenSuspended() {
		return sendWhenSuspended;
	}

	public void setSendWhenSuspended(Boolean sendWhenSuspended) {
		this.sendWhenSuspended = sendWhenSuspended;
	}
	
    public Object get(String address) {
        String[] addrs = address.split("\\.");

        Object currObj = this;
        try {
            for (int i = 1; i < addrs.length; i++) {
                Class currClass = currObj.getClass();
                String fldName = addrs[i];
                String getter = "get" + fldName.substring(0, 1).toUpperCase() + fldName.substring(1, fldName.length());
                Method m = currClass.getMethod(getter);
                // Field fld = currClass.getField(fldName);
                // Object obj = fld.get(currObj);
                Object obj = m.invoke(currObj);

                if (obj == null) {
                    // TODO
                    return null;
                }
                currObj = obj;
            }
            return currObj;

        } catch (Exception ex) {
            // getLogger().error("Error in Creating Ifx Object", ex);
            // TODO
            return null;
        }
    }
    
    public Boolean isOutgoingMessage(){
    	return MessageType.OUTGOING.equals(getType());
    }
    
    public Boolean isIncomingMessage(){
    	return MessageType.INCOMING.equals(getType());
    }
    
    public Boolean isScheduleMessage(){
    	return MessageType.SCHEDULE.equals(getType());
    }
    
    public void setPendingRequests(Set<Message> pendingRequests) {
        this.pendingRequests = pendingRequests;

    }

    public Set<Message> getPendingRequests() {
        return pendingRequests;
    }


	public Boolean getNeedToBeInstantlyReversed() {
		return needToBeInstantlyReversed;
	}


	public void setNeedToBeInstantlyReversed(Boolean needToBeInstantlyReversed) {
		this.needToBeInstantlyReversed = needToBeInstantlyReversed;
	}

	public Terminal getEndPointTerminal() {
	    return this.endPointTerminal;
	}

	public void setEndPointTerminal(Terminal endPointTerminal) {
		this.endPointTerminal = endPointTerminal;
		if (this.ifx != null)
        	this.ifx.setEndPointTerminal(endPointTerminal);
		if (endPointTerminal != null)
			endPointTerminalId = endPointTerminal.getId();
	}

	public String getChannelName() { //Raza commenting start
		return channelName;
	}


	public void setChannelName(String channelName) { //Raza commenting start
		this.channelName = channelName;
	}
	
	@Override
	public String toString(){
		return id!=null ? id.toString():"";
	}

	public Long getTransactionId() {
    	return transactionId;
    }
	
    public byte[] getSecurityKey() {
		return securityKey;
	}

	public void setSecurityKey(byte[] securityKey) {
		this.securityKey = securityKey;
	}

	public Long getEndPointTerminalId() {
    	return endPointTerminalId;
    }
        
    public Long getIfxId() {
    	return ifxId;
    }

	public MessageXML getMsgXml() {
		if(this.msgXmls == null || this.msgXmls.size() == 0)
			return null;
		return this.msgXmls.get(0);
	}


	public List<MessageXML> getMsgXmls() {
		return this.msgXmls;
	}

	public void setMsgXmls(List<MessageXML> msgXmls) {
		this.msgXmls = msgXmls;
	}


	public Long getSrcTPDU() {
		return srcTPDU;
	}


	public void setSrcTPDU(Long srcTPDU) {
		this.srcTPDU = srcTPDU;
	}

        public String getANI() {
                return ANI;
        }


        public void setANI(String aNI) {
                ANI = aNI;
        }


        public String getDNIS() {
                return DNIS;
        }


        public void setDNIS(String dNIS) {
                DNIS = dNIS;
        }


        public String getLRI() {
                return LRI;
        }


        public void setLRI(String lRI) {
                LRI = lRI;
        }

    // Added by : Asim Shahzad, Date : 24th Nov 2016, Desc : Added header handling for VISA SMS
    public byte[] getHeaderData() {
        return headerData;
    }

    public void setHeaderData(byte[] headerData) {
        this.headerData = headerData;
    }

    public byte[] getUpdatedMessageHeader(Channel channel) {
        byte[] header;
        byte[] sourceId;
        byte[] destinationId;
        byte[] updatedHeader;
        String institutionId;

        header = this.getHeaderData().clone();
		//Raza Adding from TPSP
        if (channel.getChannelId().equals(ChannelCodes.UNION_PAY)) {
            institutionId = ProcessContext.get().getInstitution(channel.getInstitutionId()).getBin().toString();
            sourceId = Arrays.copyOfRange(header, 17, 28);
            updatedHeader = ArrayUtils.addAll(Arrays.copyOfRange(header, 0, 6), sourceId);
            institutionId = String.format("%1$-11s", institutionId);
            destinationId = institutionId.getBytes();	
            updatedHeader = ArrayUtils.addAll(updatedHeader, destinationId);
            updatedHeader = ArrayUtils.addAll(updatedHeader, Arrays.copyOfRange(header, 28, header.length));
        }
		else if (channel.getChannelId().equals(ChannelCodes.VISA_SMS.toString()))
        {
            institutionId = ProcessContext.get().getInstitution(channel.getInstitutionId()).getBin().toString();
            sourceId = Arrays.copyOfRange(header, 8, 11);
            updatedHeader = ArrayUtils.addAll(Arrays.copyOfRange(header, 0, 5), sourceId);
            destinationId = Hex.decode(institutionId.toString());
            updatedHeader = ArrayUtils.addAll(updatedHeader, destinationId);
            updatedHeader = ArrayUtils.addAll(updatedHeader, Arrays.copyOfRange(header, 11, header.length));

        } else if (channel.getChannelId().equals(ChannelCodes.VISA_BASE_I)) {
            sourceId = Arrays.copyOfRange(header, 8, 11);
            updatedHeader = ArrayUtils.addAll(Arrays.copyOfRange(header, 0, 5), sourceId);
            destinationId = Arrays.copyOfRange(header, 5, 8);
            updatedHeader = ArrayUtils.addAll(updatedHeader, destinationId);
            updatedHeader = ArrayUtils.addAll(updatedHeader, Arrays.copyOfRange(header, 11, header.length));
        } else if(channel.getChannelId().equals(ChannelCodes.NAC)){ //Raza adding for KEENU

            //Raza TEMP start
//            logger.info("Printing Header start");
//            for(int i =0 ; i<header.length ; i++)
//            {
//                logger.info("Header at i [" + i + "] = [" + header[i] + "]");
//            }
//            String temp = header.toString();
//            logger.info("Printing Header String [" + temp + "]");
//            logger.info("Printing Header end");
            //Raza TEMP end


            byte byte1 = header[1]; //Raza swap byte1,2 with byte3,4 for Response
            byte byte2 = header[2];
            header[1] = header[3];
            header[2] = header[4];
            header[3] = byte1;
            header[4] = byte2;
            updatedHeader = header;
//            logger.info("Header NOW");
//            for(int i =0 ; i<header.length ; i++)
//            {
//                logger.info("Header at i [" + i + "] = [" + header[i] + "]");
//            }
//            sourceId = Arrays.copyOfRange(header, 6, 8);
//            destinationId = Arrays.copyOfRange(header, 8, 10);
//            updatedHeader = ArrayUtils.addAll(Arrays.copyOfRange(header, 0, 6), destinationId);
//            updatedHeader = ArrayUtils.addAll(updatedHeader, sourceId);
        }
        else {
            updatedHeader = null;
        }

        return updatedHeader;
    }

    public byte[] getMessageHeader(Channel channel) {
        byte[] header;
        byte[] headerSubFields;
        String headerStr;
        String sourceId;

        header = null;

        if (channel.getName().equals(ChannelCodes.VISA_SMS)) {
            sourceId = ProcessContext.get().getInstitution(channel.getInstitutionId()).getBin().toString();
            //1,2,3
            header = new byte[] {22, 1, 2};

            //4(00) //5(000)
            headerSubFields = new byte[] {0,0,0,0,0};
            header = ArrayUtils.addAll(header,headerSubFields);

            //6
            headerStr = StringUtils.leftPad(sourceId, 6, "0");
            header = ArrayUtils.addAll(header, headerStr.getBytes());

            //9(000) //10(0) //11(000) //12(0)
            headerSubFields = new byte[] {0,0,0,0,0,0,0,0};
            header = ArrayUtils.addAll(header, headerSubFields);

            System.out.print("header " + Arrays.toString(header));

        } else if (channel.getName().equals(ChannelCodes.VISA_BASE_I)) {
            sourceId = ProcessContext.get().getInstitution(channel.getInstitutionId()).getBin().toString();
            //1,2,3,4,5
            header = new byte[] {22, 1, 1, 0, 0, 0, 0, 0};

            //6
            headerSubFields = Hex.decode(sourceId);
            header = ArrayUtils.addAll(header, headerSubFields);

            //7,8,9,10,11,12.13,14
            headerSubFields = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            header = ArrayUtils.addAll(header, headerSubFields);

            System.out.print("header " + Arrays.toString(header));
        }
		else if (channel.getChannelId().equals(ChannelCodes.UNION_PAY)) {
            sourceId = ProcessContext.get().getInstitution(channel.getInstitutionId()).getBin().toString();
            //1, 2 (42, 2)
            header = new byte[] {46, 2};

            //3,4,5
            headerStr = "000000010344   " + String.format("%1$-11s", sourceId);
            header = ArrayUtils.addAll(header, headerStr.getBytes());

            //6 (000) //7 (0)
            headerSubFields = new byte[] {0, 0, 0, 0};
            header = ArrayUtils.addAll(header, headerSubFields);

            //8
            headerStr = "00000000";
            header = ArrayUtils.addAll(header, headerStr.getBytes());

            //9 (0)
            headerSubFields = new byte[] {0};
            header = ArrayUtils.addAll(header, headerSubFields);

            //10 (00000)
            headerStr = "00000";
            header = ArrayUtils.addAll(header, headerStr.getBytes());
            System.out.print("header " + Arrays.toString(header));

        }

        return header;
    }

    public byte[] setBinaryDataWithHeader(byte[] incomingData) throws Exception {
        byte[] headerData;
        byte[] headerFirstData;
        byte[] headerLastData;
        byte[] updatedHeaderData;
        byte[] combinedData;
        byte[] messageLengthInBytes;
        int messageLength;
        int factor;
        Channel channel;
        String messageHexLength;

        try {
            headerData = this.getHeaderData();
            channel = this.getChannel();
            combinedData = null;

            if (channel.getHeaderLen() > 0) {

                //if (headerData != null) {
                if (headerData != null) {
                    //set source and destination id in header
                    headerData = this.getUpdatedMessageHeader(channel);
                } else {
                    headerData = this.getMessageHeader(channel);
                }

                combinedData = ArrayUtils.addAll(headerData, incomingData);

                messageLength = combinedData.length;

                if (channel.getChannelId().equals(ChannelCodes.UNION_PAY)) {
                    messageLengthInBytes = new byte[4];
                    factor = 1000;
                    for (int i = 0; i < 4; i++) {
                        messageLengthInBytes[i] = (byte) ((messageLength / factor) + 48);
                        messageLength = messageLength % factor;
                        factor /= 10;
                    }

                    //need to set this length in message header
                    headerFirstData = Arrays.copyOfRange(headerData, 0, 2);
                    headerLastData = Arrays.copyOfRange(headerData, 6, headerData.length);
                    updatedHeaderData = ArrayUtils.addAll(headerFirstData, messageLengthInBytes);
                    headerData = ArrayUtils.addAll(updatedHeaderData, headerLastData);

                }
				else if(channel.getName().equals(ChannelCodes.VISA_SMS.toString()) ||
                        channel.getName().equals(ChannelCodes.VISA_BASE_I.toString()))
                {
                    messageLengthInBytes = new byte[2];

                    messageHexLength = Integer.toHexString(messageLength);
                    messageHexLength = StringUtils.leftPad(messageHexLength, 4, "0");
                    messageLengthInBytes[0] = (byte)(Integer.parseInt(messageHexLength.substring(0,2), 16));
                    messageLengthInBytes[1] = (byte)(Integer.parseInt(messageHexLength.substring(2,4), 16));

                    //need to set this length in message header
                    headerFirstData = Arrays.copyOfRange(headerData, 0, 3);
                    headerLastData = Arrays.copyOfRange(headerData, 5, headerData.length);
                    updatedHeaderData = ArrayUtils.addAll(headerFirstData, messageLengthInBytes);
                    headerData = ArrayUtils.addAll(updatedHeaderData, headerLastData);

                }

                combinedData = ArrayUtils.addAll(headerData, incomingData);

            } else {
                combinedData = incomingData;
            }
        } catch (Exception ex) {
            throw ex;
        }

        return  combinedData;
    }
    // End
//    public String getChannelId() {
//        return channelId;
//    }

//    public void setChannelId(String channelId) {
//        this.channelId = channelId;
//    }
}
