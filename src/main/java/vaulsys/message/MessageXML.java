package vaulsys.message;

import vaulsys.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="trx_message_xml")
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class MessageXML implements IEntity<Long> {
	
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="msgxml-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "msgxml-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "msgxml_seq")
    				})
    private Long id;

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "msg")
	private Message msg;

	@Column(name = "xml_1", length=4000)
	private String xmlPart1;

	@Column(name = "xml_2", length=4000)
	private String xmlPart2;

	
	public MessageXML() {
	}

	public MessageXML(Message msg) {
		this.msg=msg;
	}

	public MessageXML(String xmlPart1, String xmlPart2) {
		this.xmlPart1 = xmlPart1;
		this.xmlPart2 = xmlPart2;
	}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getXmlPart1() {
		return xmlPart1;
	}

	public void setXmlPart1(String xmlPart1) {
		this.xmlPart1 = xmlPart1;
	}

	public String getXmlPart2() {
		return xmlPart2;
	}

	public void setXmlPart2(String xmlPart2) {
		this.xmlPart2 = xmlPart2;
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}
}
