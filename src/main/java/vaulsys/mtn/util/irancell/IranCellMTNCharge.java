package vaulsys.mtn.util.irancell;

import vaulsys.calendar.DateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "mtn_charge")
public class IranCellMTNCharge {
	public static final Integer NOT_ASSIGNED_VALUE = 0;
	public static final Integer LOCKED_VALUE = 3;

	
	@Id
	private Long cardSerialNo;
	
	@Column(length=1024)
	private String cardPIN;
	
	private Long credit;
	
	private Integer fileId;
	
	private Integer year;
	
	private String helpDesk;
	
	private Integer ir;

    private String provider;
	
	 @Embedded
	    @AttributeOverrides({
	    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "state_date")),
	    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "state_time"))
	    })
	private DateTime stateDate;

	@Column(name = "charge_state") 
	private Integer state = NOT_ASSIGNED_VALUE;

	//Organization Code
	@Column(name = "company")
	private Long entity;
	 
	@Column(name="pinlen")
	private Integer pinlen;

	public Long getCredit() {
		return credit;
	}

	public void setCredit(Long credit) {
		this.credit = credit;
	}

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Long getCardSerialNo() {
		return cardSerialNo;
	}

	public void setCardSerialNo(Long cardSerialNo) {
		this.cardSerialNo = cardSerialNo;
	}

	public String getCardPIN() {
		return cardPIN;
	}

	public void setCardPIN(String cardPIN) {
		this.cardPIN = cardPIN;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
		setStateDate(DateTime.now());
	}

	public DateTime getStateDate() {
		return stateDate;
	}

	public void setStateDate(DateTime stateDate) {
		this.stateDate = stateDate;
	}

	public Long getEntity() {
		return entity;
	}

	public void setEntity(Long entity) {
		this.entity = entity;
	}

	public String getHelpDesk() {
		return helpDesk;
	}

	public void setHelpDesk(String helpDesk) {
		this.helpDesk = helpDesk;
	}

	public Integer getIr() {
		return ir;
	}

	public void setIr(Integer ir) {
		this.ir = ir;
	}

	public Integer getPinlen() {
		return pinlen;
	}

	public void setPinlen(Integer pinlen) {
		this.pinlen = pinlen;
	}

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "IranCellMTNCharge{" +
                "cardSerialNo=" + cardSerialNo +
                ", provider=" + provider +
                '}';
    }
}
