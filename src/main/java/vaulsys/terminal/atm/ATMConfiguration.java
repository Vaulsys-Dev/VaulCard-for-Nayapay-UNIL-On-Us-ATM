package vaulsys.terminal.atm;

import vaulsys.customer.Currency;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.customizationdata.EnhancedParameterData;
import vaulsys.terminal.atm.customizationdata.FITData;
import vaulsys.terminal.atm.customizationdata.ScreenData;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.terminal.atm.customizationdata.TimerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm_config")
public class ATMConfiguration implements IEntity<Long> {

    @Id
    private Long id;
    
    private String name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="atmConfiguration")
    private List<StateData> states;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="atmConfiguration")
    private List<ScreenData> screens;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="atmConfiguration")
    private List<FITData> fits;
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="atmConfiguration")
    private List<EnhancedParameterData> params;
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="atmConfiguration")
    private List<TimerData> timers;
    

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "configuration")
    private List<ATMRequest> requests;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "term_atm_fit__response", 
  		joinColumns = {@JoinColumn(name = "config")},
  		inverseJoinColumns = {@JoinColumn(name = "rscode_response")}
  		)
  	@ForeignKey(name = "atmconf_config_fk", inverseName = "atmconf_fitresponse_fk")
	private Map<Integer, RsCodeResponses> fitResponses;
    
    private Integer maxDespensingNotes;
    
    private Integer receiptLineLength;
    
    private Integer receiptLeftMargin;
    
    @Column(name="a_denom")
    private Integer cassetteADenomination;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "a_curr")
	private Currency cassetteACurrency;
    
    @Column(name = "a_curr", insertable = false, updatable = false)
	private Long cassetteACurrencyId;

	public Long getCassetteACurrencyId() {
		return cassetteACurrencyId;
	}
    
//	@Column(name="a_curr")
//    private Integer cassetteACurrency;
    
    @Column(name="b_denom")
    private Integer cassetteBDenomination;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "b_curr")
	private Currency cassetteBCurrency;
    
    @Column(name = "b_curr", insertable = false, updatable = false)
	private Long cassetteBCurrencyId;

	public Long getCassetteBCurrencyId() {
		return cassetteBCurrencyId;
	}
    
//    @Column(name="b_curr")
//    private Integer cassetteBCurrency;
    
    @Column(name="c_denom")
    private Integer cassetteCDenomination;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "c_curr")
	private Currency cassetteCCurrency;
    
    @Column(name = "c_curr", insertable = false, updatable = false)
	private Long cassetteCCurrencyId;

	public Long getCassetteCCurrencyId() {
		return cassetteCCurrencyId;
	}
    
//    @Column(name="c_curr")
//    private Integer cassetteCCurrency;
    
    @Column(name="d_denom")
    private Integer cassetteDDenomination;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "d_curr")
	private Currency cassetteDCurrency;
    
    @Column(name = "d_curr", insertable = false, updatable = false)
	private Long cassetteDCurrencyId;

	public Long getCassetteDCurrencyId() {
		return cassetteDCurrencyId;
	}
    
//    @Column(name="d_curr")
//    private Integer cassetteDCurrency;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="header")
    @ForeignKey(name = "atmconfig_header_recipet_fk")
    private Receipt receiptHeader;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="footer")
    @ForeignKey(name = "atmconfig_footer_recipet_fk")
    private Receipt receiptFooter;

    
    @Column(name="farsi_rcpt_enc", length= 1)
    private String farsi_reciept_encoding;
    
    @Column(name="farsi_ext_rcpt_enc", length= 1)
    private String farsi_extended_reciept_encoding;
    
    @Column(name="farsi_scrn_enc", length= 1)
    private String farsi_screen_encoding;
    
    @Column(name="farsi_ext_scrn_enc", length= 1)
    private String farsi_extended_screen_encoding;
    
    @Column(name="english_enc", length= 1)
    private String english_encoding;
    
    @Column(name="scr_convertor", length= 30)
    private String screenConvertor;
    
    @Column(name="rcpt_convertor", length= 30)
    private String receiptConvertor;
    
    @Column(name="bnkFarsiName", length= 40)
	private String bnkFarsiName;
    
    @Column(name="bnkFarsiMount", length= 40)
	private String bnkFarsiMount;
    
    @Column(name="bnkEnglishName", length= 40)
    private String bnkEnglishName;
	
    @Column(name="bnkEnglishMount", length= 40)
    private String bnkEnglishMount;

    @Transient
    private Map<String,String> encodingMap;
    
    @Transient
    private Map<String, String> convertorsMap;
    
    @Transient
    private Map<String, byte[]> receiptDetailsMap = new HashMap<String, byte[]>();
    
    //MIRKAMALI(Task130)
    private Boolean enabled = true;
    
    //Mirkamali(Task179): Currency ATM
    private Boolean isCurrencyConfig = false;

    public Map<String, byte[]> getReceiptDetailsMap() {
		return receiptDetailsMap;
	}

	public void setReceiptDetailsMap(String key, byte[] value) {
		this.receiptDetailsMap.put(key, value);
	}

	public ATMConfiguration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ATMRequest> getRequests() {
        return requests;
    }

    public void addRequset(ATMRequest request) {
//        if (requests == null)
//            requests = new ArrayList<ATMRequest>();
        request.setConfiguration(this);
//        requests.add(request);
    }

    public void setRequests(List<ATMRequest> requests) {
        this.requests = requests;
    }

    public ATMResponse getResponse(Integer fit, int responseCode) {
        if (fitResponses != null) {
            ATMResponse response = ATMTerminalService.getResponse(this, fit, responseCode);
            if (response == null)
            	response = ATMTerminalService.getResponse(this, fit, ATMErrorCodes.DEFAULT_RESPONSE_CODE);
            return response;
        }
        return null;
    }

    public void addResponse(Integer fit, Integer rsCode, ATMResponse response) {
    	if (fitResponses == null)
    		fitResponses = new HashMap<Integer, RsCodeResponses>();
    	
    	RsCodeResponses rsCodeResponses = fitResponses.get(fit);
    	if (rsCodeResponses == null) {
    		rsCodeResponses = new RsCodeResponses();
    		fitResponses.put(fit, rsCodeResponses);
    	}
    	rsCodeResponses.addRsCodeResponses(fit, rsCode, response);
    }
    
    public void addResponse(RsCodeResponses codeResponses){
    	if(fitResponses == null)
    		fitResponses = new HashMap<Integer, RsCodeResponses>();
    	fitResponses.put(codeResponses.getFit(), codeResponses);
    }
    
    public List<StateData> getStates() {
        return states;
    }

    public void setStates(List<StateData> states) {
        this.states = states;
    }

    public List<ScreenData> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenData> screens) {
        this.screens = screens;
    }

    public List<FITData> getFits() {
        return fits;
    }

    public void setFits(List<FITData> fits) {
        this.fits = fits;
    }

    public void addFIT(FITData fit) {
    	if (fits == null)
    		fits = new ArrayList<FITData>();
    	fits.add(fit);
        fit.setAtmConfiguration(this);
    }
    
    public List<EnhancedParameterData> getParams() {
		return params;
	}

	public void setParams(List<EnhancedParameterData> params) {
		this.params = params;
	}

    public void addParam(EnhancedParameterData param) {
    	if (params == null)
    		params = new ArrayList<EnhancedParameterData>();
    	params.add(param);
    	param.setAtmConfiguration(this);
    }
    
    public List<TimerData> getTimers() {
    	return timers;
    }
    
    public void setTimers(List<TimerData> timers) {
    	this.timers = timers;
    }
    
    public void addTimer(TimerData timer) {
    	if (timers == null)
    		timers = new ArrayList<TimerData>();
    	timers.add(timer);
    	timer.setAtmConfiguration(this);
    }

    public void addState(StateData state) {
    	if (states == null)
    		states = new ArrayList<StateData>();
    	states.add(state);
        state.setAtmConfiguration(this);
    }

    public void addScreen(ScreenData screen) {
    	if (screens == null)
    		screens = new ArrayList<ScreenData>();
    	screens.add(screen);
        screen.setAtmConfiguration(this);
    }

	public Integer getMaxDespensingNotes() {
		return maxDespensingNotes;
	}

	public void setMaxDespensingNotes(Integer maxDespensingNotes) {
		this.maxDespensingNotes = maxDespensingNotes;
	}

	public void setReceiptLineLength(Integer receiptLineLength) {
		this.receiptLineLength = receiptLineLength;
	}

	public Integer getReceiptLineLength() {
		return receiptLineLength;
	}

	public void setReceiptLeftMargin(Integer receiptLeftMargin) {
		this.receiptLeftMargin = receiptLeftMargin;
	}

	public Integer getReceiptLeftMargin() {
		return receiptLeftMargin;
	}

	public Map<Integer, RsCodeResponses> getFitResponses() {
		return fitResponses;
	}

	public Integer getCassetteADenomination() {
		return cassetteADenomination;
	}

	public void setCassetteADenomination(Integer cassetteADenomination) {
		this.cassetteADenomination = cassetteADenomination;
	}

	public Integer getCassetteBDenomination() {
		return cassetteBDenomination;
	}

	public void setCassetteBDenomination(Integer cassetteBDenomination) {
		this.cassetteBDenomination = cassetteBDenomination;
	}

	public Integer getCassetteCDenomination() {
		return cassetteCDenomination;
	}

	public void setCassetteCDenomination(Integer cassetteCDenomination) {
		this.cassetteCDenomination = cassetteCDenomination;
	}

	public Integer getCassetteDDenomination() {
		return cassetteDDenomination;
	}

	public void setCassetteDDenomination(Integer cassetteDDenomination) {
		this.cassetteDDenomination = cassetteDDenomination;
	}

	public Receipt getReceiptHeader() {
		return receiptHeader;
	}

	public void setReceiptHeader(Receipt receiptHeader) {
		this.receiptHeader = receiptHeader;
	}

	public Receipt getReceiptFooter() {
		return receiptFooter;
	}

	public void setReceiptFooter(Receipt receiptFooter) {
		this.receiptFooter = receiptFooter;
	}

	public Currency getCassetteACurrency() {
		return cassetteACurrency;
	}

	public void setCassetteACurrency(Currency cassetteACurrency) {
		this.cassetteACurrency = cassetteACurrency;
	}

	public Currency getCassetteBCurrency() {
		return cassetteBCurrency;
	}

	public void setCassetteBCurrency(Currency cassetteBCurrency) {
		this.cassetteBCurrency = cassetteBCurrency;
	}

	public Currency getCassetteCCurrency() {
		return cassetteCCurrency;
	}

	public void setCassetteCCurrency(Currency cassetteCCurrency) {
		this.cassetteCCurrency = cassetteCCurrency;
	}

	public Currency getCassetteDCurrency() {
		return cassetteDCurrency;
	}

	public void setCassetteDCurrency(Currency cassetteDCurrency) {
		this.cassetteDCurrency = cassetteDCurrency;
	}	
	
	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getIsCurrencyConfig() {
		return isCurrencyConfig;
	}

	public void setIsCurrencyConfig(Boolean isCurrencyConfig) {
		this.isCurrencyConfig = isCurrencyConfig;
	}

	@Override
	public String toString(){
		return String.format("%s, %s", name != null ? name:"-", id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((cassetteACurrencyId == null) ? 0 : cassetteACurrencyId
						.hashCode());
		result = prime
				* result
				+ ((cassetteADenomination == null) ? 0 : cassetteADenomination
						.hashCode());
		result = prime
				* result
				+ ((cassetteBCurrencyId == null) ? 0 : cassetteBCurrencyId
						.hashCode());
		result = prime
				* result
				+ ((cassetteBDenomination == null) ? 0 : cassetteBDenomination
						.hashCode());
		result = prime
				* result
				+ ((cassetteCCurrencyId == null) ? 0 : cassetteCCurrencyId
						.hashCode());
		result = prime
				* result
				+ ((cassetteCDenomination == null) ? 0 : cassetteCDenomination
						.hashCode());
		result = prime
				* result
				+ ((cassetteDCurrencyId == null) ? 0 : cassetteDCurrencyId
						.hashCode());
		result = prime
				* result
				+ ((cassetteDDenomination == null) ? 0 : cassetteDDenomination
						.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime
				* result
				+ ((maxDespensingNotes == null) ? 0 : maxDespensingNotes
						.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ATMConfiguration))
			return false;
		ATMConfiguration other = (ATMConfiguration) obj;
		if (cassetteACurrencyId == null) {
			if (other.cassetteACurrencyId != null)
				return false;
		} else if (!cassetteACurrencyId.equals(other.cassetteACurrencyId))
			return false;
		if (cassetteADenomination == null) {
			if (other.cassetteADenomination != null)
				return false;
		} else if (!cassetteADenomination.equals(other.cassetteADenomination))
			return false;
		if (cassetteBCurrencyId == null) {
			if (other.cassetteBCurrencyId != null)
				return false;
		} else if (!cassetteBCurrencyId.equals(other.cassetteBCurrencyId))
			return false;
		if (cassetteBDenomination == null) {
			if (other.cassetteBDenomination != null)
				return false;
		} else if (!cassetteBDenomination.equals(other.cassetteBDenomination))
			return false;
		if (cassetteCCurrencyId == null) {
			if (other.cassetteCCurrencyId != null)
				return false;
		} else if (!cassetteCCurrencyId.equals(other.cassetteCCurrencyId))
			return false;
		if (cassetteCDenomination == null) {
			if (other.cassetteCDenomination != null)
				return false;
		} else if (!cassetteCDenomination.equals(other.cassetteCDenomination))
			return false;
		if (cassetteDCurrencyId == null) {
			if (other.cassetteDCurrencyId != null)
				return false;
		} else if (!cassetteDCurrencyId.equals(other.cassetteDCurrencyId))
			return false;
		if (cassetteDDenomination == null) {
			if (other.cassetteDDenomination != null)
				return false;
		} else if (!cassetteDDenomination.equals(other.cassetteDDenomination))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (maxDespensingNotes == null) {
			if (other.maxDespensingNotes != null)
				return false;
		} else if (!maxDespensingNotes.equals(other.maxDespensingNotes))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getFarsi_reciept_encoding() {
		return farsi_reciept_encoding;
	}

	public void setFarsi_reciept_encoding(String farsi_reciept_encoding) {
		this.farsi_reciept_encoding = farsi_reciept_encoding;
	}

	public String getFarsi_extended_reciept_encoding() {
		return farsi_extended_reciept_encoding;
	}

	public void setFarsi_extended_reciept_encoding(String farsi_extended_reciept_encoding) {
		this.farsi_extended_reciept_encoding = farsi_extended_reciept_encoding;
	}

	public String getFarsi_screen_encoding() {
		return farsi_screen_encoding;
	}

	public void setFarsi_screen_encoding(String farsi_screen_encoding) {
		this.farsi_screen_encoding = farsi_screen_encoding;
	}

	public String getFarsi_extended_screen_encoding() {
		return farsi_extended_screen_encoding;
	}

	public void setFarsi_extended_screen_encoding(String farsi_extended_screen_encoding) {
		this.farsi_extended_screen_encoding = farsi_extended_screen_encoding;
	}

	public String getEnglish_encoding() {
		return english_encoding;
	}

	public void setEnglish_encoding(String english_encoding) {
		this.english_encoding = english_encoding;
	}

	public String getScreenConvertor() {
		return screenConvertor;
	}

	public void setScreenConvertor(String screenConvertor) {
		this.screenConvertor = screenConvertor;
	}

	public String getReceiptConvertor() {
		return receiptConvertor;
	}

	public void setReceiptConvertor(String receiptConvertor) {
		this.receiptConvertor = receiptConvertor;
	}

	public String getBnkFarsiName() {
		return bnkFarsiName;
	}

	public void setBnkFarsiName(String bnkFarsiName) {
		this.bnkFarsiName = bnkFarsiName;
	}

	public String getBnkFarsiMount() {
		return bnkFarsiMount;
	}

	public void setBnkFarsiMount(String bnkFarsiMount) {
		this.bnkFarsiMount = bnkFarsiMount;
	}

	public String getBnkEnglishName() {
		return bnkEnglishName;
	}

	public void setBnkEnglishName(String bnkEnglishName) {
		this.bnkEnglishName = bnkEnglishName;
	}

	public String getBnkEnglishMount() {
		return bnkEnglishMount;
	}

	public void setBnkEnglishMount(String bnkEnglishMount) {
		this.bnkEnglishMount = bnkEnglishMount;
	}

	public void setEncodingMap(Map<String,String> encodingMap) {
		this.encodingMap = encodingMap;
	}

	public Map<String,String> getEncodingMap() {
		return encodingMap;
	}

	public void setConvertorsMap(Map<String, String> convertorsMap) {
		this.convertorsMap = convertorsMap;
	}

	public Map<String, String> getConvertorsMap() {
		return convertorsMap;
	}
}
