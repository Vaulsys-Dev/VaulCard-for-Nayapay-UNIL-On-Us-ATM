package vaulsys.terminal.impl;

import vaulsys.auditlog.AuditableProperty;
import vaulsys.auditlog.CollectionProperty;
import vaulsys.auditlog.SimpleProperty;
import vaulsys.calendar.DateTime;
import vaulsys.contact.City;
import vaulsys.contact.Country;
import vaulsys.contact.State;
import vaulsys.entity.impl.Branch;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMConnectionStatus;
import vaulsys.terminal.atm.ATMProducer;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.currencyatm.ATMTerminalCurrency;
import vaulsys.terminal.atm.device.ATMDevice;
import vaulsys.transaction.Transaction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm")
@ForeignKey(name = "atm_terminal_fk")
//@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class ATMTerminal extends Terminal {
    @Transient
    private transient Logger logger = Logger.getLogger(this.getClass());

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "state"))
    ATMState state = ATMState.UNKNOWN;

    @Embedded
    @AttributeOverride(name = "type", column = @Column(name = "producer"))
    ATMProducer producer;

    @Embedded
    @AttributeOverride(name = "status", column = @Column(name = "connection"))
    private ATMConnectionStatus connection = ATMConnectionStatus.NOT_CONNECTED;

//	@Column(name = "last_opkey")
//	String lastOpkey;

    @Column(name = "current_state_class")
    private String currentStateClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    @ForeignKey(name = "atm_owner_fk")
    private Branch owner;

    @Column(name = "owner", insertable = false, updatable = false)
    private Long ownerId;

    public Long getOwnerId() {
        return ownerId;
    }

    private Integer configId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_real_trx")
    @ForeignKey(name = "atm_lasttrx_fk")
    private Transaction lastRealTransaction;

    @Column(name = "last_real_trx", insertable = false, updatable = false)
    private Long lastRealTransactionId;

    @Column
    private Boolean changeKey = true;

//	@Column(name = "last_withdrawal_id")
//	private Long lastWithdrawalTrxId;
//	
//	
//	@Embedded
//    @AttributeOverrides({
//    	@AttributeOverride(name = "dayDate.date", column = @Column(name = "last_withdrawal_date")),
//    	@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "last_withdrawal_time"))
//    })
//	private DateTime lastWithdrawalTime;
    
    
    //Mirkamali(Task179) : Currency ATMs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency")
    @ForeignKey(name = "atm_currency_fk")
    private ATMTerminalCurrency currency; 
    /* Added by : Asim Shahzad, Date : 20th October 2016, Desc : To add Card Acceptor Name Location from UI */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country")
    @ForeignKey(name = "atm_country_fk")
    Country country;
    @Transient
    @Column(length = 12)
    private String cityenglishname;
    @Column(name="isChangedConfig",nullable=true)
    private boolean isChangedConfig = false;

    public boolean isChangedConfig() {
        return isChangedConfig;
    }

    public void setChangedConfig(boolean isChangedConfig) {
        this.isChangedConfig = isChangedConfig;
    }
    
    public Boolean getChangeKey() {
        return changeKey;
    }

    public void setChangeKey(Boolean changeKey) {
        this.changeKey = changeKey;
    }
     public boolean isChangeKey(){
         return this.changeKey;
     }

    public Long getLastRealTransactionId() {
        return lastRealTransactionId;
    }

    /******** ATM Terminal Version Properties ********/
    /**
     * ***** Start *******
     */
    @Column(length = 15)
    private String IP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config")
    @ForeignKey(name = "atm_config_fk")
    private ATMConfiguration configuration;

    @Column(name = "config", insertable = false, updatable = false)
    private Long configurationId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "atm", fetch = FetchType.LAZY)
    private List<ATMDevice> devices;

    private int lastSentFitIndex = 0;
    private int lastSentStateIndex = 0;
    private int lastSentScreenIndex = 0;
//    private int lastSentParamIndex = 0;

    @Transient
    @AttributeOverrides({
            @AttributeOverride(name = "dayDate.date", column = @Column(name = "last_key_date")),
            @AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "last_key_time"))})
    private DateTime lastKeyChangeDate; //TODO: Its Usage!

    @Column(name = "lastkeychange_dt")
    private Long lastKeyChangeDateLong;

    public Long getLastKeyChangeDateLong() {
        return lastKeyChangeDateLong;
    }

    public void setLastKeyChangeDateLong(Long lastKeyChangeDateLong) {
        this.lastKeyChangeDateLong = lastKeyChangeDateLong;
    }

    @Column(length = 50)
    private String description;

    @Column(name = "cardacceptornamelocation", length = 40)
    private String cardacceptnamelocation;

    public Long getConfigurationId() {
        return configurationId;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

//	public ATMConfiguration getOwnOrParentConfiguration() {
//		if (configuration != null)
//			return configuration;
//		if (sharedFeature != null)
//			return sharedFeature.getConfiguration();
//		return null;
//	}

    public Long getOwnOrParentConfigurationId() {
        if (configurationId != null)
            return configurationId;
        if (sharedFeature != null)
            return sharedFeature.getConfigurationId();
        return null;
    }

    public ATMConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ATMConfiguration configuration) {
        this.configuration = configuration;
    }
    
    
    public ATMTerminalCurrency getCurrency() {
		return currency;
	}

	public void setCurrency(ATMTerminalCurrency currency) {
		this.currency = currency;
	}

	/******** End ********/
    /**
     * ***** ATM Terminal Version Properties *******
     */


    public ATMTerminal() {
        state = ATMState.UNKNOWN;
    }

    public ATMTerminal(Long code) {
        super(code);
        state = ATMState.UNKNOWN;
    }

    public ATMState getState() {
        return state;
    }

    public void setATMState(ATMState state) {
        this.state = state;
    }

    @Override
    public Branch getOwner() {
        return owner;
    }

    @Override
    public TerminalType getTerminalType() {
        return TerminalType.ATM;
    }

    public void setOwner(Branch owner) {
        this.owner = owner;
    }

    public TerminalClearingMode getClearingMode() {
        return TerminalClearingMode.TERMINAL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((IP == null) ? 0 : IP.hashCode());
        result = prime * result + ((configurationId == null) ? 0 : configurationId.hashCode());
        result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((changeKey == null) ? 0 : changeKey.hashCode());
        result = prime * result + ((producer == null) ? 0 : producer.hashCode());
        result = prime * result + ((lastKeyChangeDateLong == null) ? 0 : lastKeyChangeDateLong.hashCode());
        return result;
    }

    public void setConnection(ATMConnectionStatus connection) {
        this.connection = connection;
    }

    public ATMConnectionStatus getConnection() {
        return connection;
    }

    public List<ATMDevice> getDevices() {
        return devices;
    }

    public <T extends ATMDevice> T getDevice(Class<T> clazz) {
        if (devices == null)
            devices = new ArrayList<ATMDevice>(1);
        for (ATMDevice device : devices) {
            if (clazz.equals(device.getClass())) {
                return (T) device;
            }
        }
        T device = null;
        try {
            device = clazz.newInstance();
            devices.add(device);
            device.setAtm(this);
        } catch (Exception e) {
            logger.error(e.getMessage() + e);
        }
        return device;
    }

    public void addDevice(ATMDevice device) {
        if (devices == null)
            devices = new ArrayList<ATMDevice>();
        device.setAtm(this);
        devices.add(device);
    }

    public int getLastSentFitIndex() {
        return lastSentFitIndex;
    }

    public void setLastSentFitIndex(int lastSentFitIndex) {
        this.lastSentFitIndex = lastSentFitIndex;
    }

    public int getLastSentStateIndex() {
        return lastSentStateIndex;
    }

    public void setLastSentStateIndex(int lastSentStateIndex) {
        this.lastSentStateIndex = lastSentStateIndex;
    }

    public int getLastSentScreenIndex() {
        return lastSentScreenIndex;
    }

    public void setLastSentScreenIndex(int lastSentScreenIndex) {
        this.lastSentScreenIndex = lastSentScreenIndex;
    }

    public void setDevices(List<ATMDevice> devices) {
        this.devices = devices;
    }

    public Integer getConfigId() {
        return configId;
    }

    public void setConfigId(Integer configId) {
        this.configId = configId;
    }

    public Transaction getLastRealTransaction() {
        return lastRealTransaction;
    }

    public void setLastRealTransaction(Transaction lastRealTransaction) {
        this.lastRealTransaction = lastRealTransaction;
    }

    public String getCurrentStateClass() {
        return this.currentStateClass;
    }

    public void setCurrentStateClass(String stClassName) {
        this.currentStateClass = stClassName;
    }

    public AbstractState getCurrentAbstractStateClass() {
        if (this.currentStateClass == null) {
            return null;
        }

        try {
            return (AbstractState) Class.forName(currentStateClass).getField("Instance").get(null);
        } catch (Exception e) {
            logger.error("Error in getting atm terminal state class:", e);
        }
        return null;
    }

    public void setCurrentAbstractStateClass(AbstractState stClass) {
        if (stClass == null) {
            this.currentStateClass = null;
            return;
        }

        this.currentStateClass = stClass.getClass().getCanonicalName();
    }

    public DateTime getLastKeyChangeDate() {
        if (lastKeyChangeDate != null)
            return lastKeyChangeDate;
        if (lastKeyChangeDateLong != null)
            return new DateTime(lastKeyChangeDateLong);
        return null;
    }

    public void setLastKeyChangeDate(DateTime lastKeyChangeDate) {
        this.lastKeyChangeDate = lastKeyChangeDate;
        if (lastKeyChangeDate != null)
            this.lastKeyChangeDateLong = lastKeyChangeDate.getDateTimeLong();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
    public String getCityenglishname()
    {
        return this.cityenglishname;
    }

    public void setCityenglishname(String cityenglishname)
    {
        this.cityenglishname = cityenglishname;
    }
    public ATMProducer getProducer() {
        return producer;
    }

    public void setProducer(ATMProducer producer) {
        this.producer = producer;
    }

    public String getcardacceptornamelocation() //Raza Adding for Field 43 as TPSP require it in English ; not using current columns as they are used by Shetab etc
    {
        return cardacceptnamelocation;
    }
    public void setcardacceptornamelocation(String cardacceptornamelocation)
    {
        this.cardacceptnamelocation = cardacceptornamelocation;
    }

//	public Long getLastWithdrawalTrxId() {
//		return lastWithdrawalTrxId;
//	}
//
//	public void setLastWithdrawalTrxId(Long lastWithdrawalTrxId) {
//		this.lastWithdrawalTrxId = lastWithdrawalTrxId;
//	}
//
//	public DateTime getLastWithdrawalTime() {
//		return lastWithdrawalTime;
//	}
//
//	public void setLastWithdrawalTime(DateTime lastWithdrawalTime) {
//		this.lastWithdrawalTime = lastWithdrawalTime;
//	}
    @Override
    public List<AuditableProperty> getAuditableFields() {
    	List<AuditableProperty> props = new ArrayList<AuditableProperty>();
    	props.addAll(super.getAuditableFields());
    	props.add(new SimpleProperty("state"));
    	props.add(new SimpleProperty("producer"));
    	props.add(new SimpleProperty("connection.status"));
    	props.add(new SimpleProperty("currentStateClass"));
    	props.add(new SimpleProperty("owner"));
    	props.add(new SimpleProperty("configId"));
    	props.add(new CollectionProperty("devices"));
    	props.add(new SimpleProperty("IP"));
    	props.add(new SimpleProperty("configuration"));
    	props.add(new SimpleProperty("description"));
    	return props;
    }

}
