package vaulsys.protocols.ifx.imp;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.PersianCalendar;
import vaulsys.persistence.IEntity;
import vaulsys.util.StringFormat;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "ifx_bnk_state_dt")
public class BankStatementData implements IEntity<Long>, Cloneable {

	@Id
//	@GeneratedValue(generator = "switch-gen")
    @GeneratedValue(generator="bankstatement-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "bankstatement-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "bankstatement_seq")
    				})
	private Long id;

	@ManyToOne
	@JoinColumn(name="emv_rs_data")
	@ForeignKey(name="bnk_state_dt__rs_dt_fk")
	private EMVRsData emvRsData;

	private String trnType;
	
	@Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "trx_date")),
    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "trx_time"))
    })
    private DateTime trxDt;
	
    private Long amount;
    
    private Long balance;
    
    private String description;
    
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public void setEmvRsData(EMVRsData emvRsData) {
		this.emvRsData = emvRsData;
	}

	public EMVRsData getEmvRsData() {
		return emvRsData;
	}

	public String getTrnType() {
		return trnType;
	}

	public void setTrnType(String trnType) {
		this.trnType = trnType;
	}

	public DateTime getTrxDt() {
		return trxDt;
	}

	public void setTrxDt(DateTime trxDt) {
		this.trxDt = trxDt;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}
	
	@Override
	public String toString() {
		DayDate persianDayDate = PersianCalendar.getPersianDayDate(trxDt.toDate());
		String dateStr = persianDayDate.toString();
		dateStr = dateStr.replaceAll("/", "-");
		String timeStr = trxDt.getDayTime().toString().replaceAll(":", "-").substring(0, 5);
		String date = timeStr+ " " + dateStr ;
//		StringFormat amountFormat = new StringFormat(10, StringFormat.JUST_LEFT);
		return  (trnType.equalsIgnoreCase("D")? '-' : '%')+" "+ StringFormat.formatNew(10, StringFormat.JUST_LEFT, amount+"") +" "+ date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
