package vaulsys.terminal.atm;

import vaulsys.customer.Currency;
import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCConsumerRequestMsg;
import vaulsys.protocols.ndc.constants.ReceiptOptionType;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm_request")
public class ATMRequest implements IEntity<Long> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;
    
    private String opkey;
    
    private String nextOpkey;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "ifxtype"))
    })
    private IfxType ifxType;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "sec_ifxtype"))
    })
    private IfxType secondaryIfxType;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "trntype"))
    })
    private TrnType trnType;
   
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "sec_trntype"))
    })
    private TrnType secondaryTrnType;
    
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "user_lang"))
    })
    private UserLanguage language;
    
    private Integer fit;
    
    private Boolean forceReceipt;
    
    private String amount;
    private String bufferB;
    private String bufferC;
    
    //TASK Task019 : Receipt Option
    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "rcpt_type"))
    })    
    private ReceiptOptionType receiptOption;
    
    


	@Column(name="extraInfo")
    private String extraInformation;
    
    @Column(name="extraInfoPath")
    private String extraInformationIfxPath;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "config")
	@ForeignKey(name="atmreq_config_fk")
    private ATMConfiguration configuration;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "currency")
	private Currency currency;
    
    @Column(name = "currency", insertable = false, updatable = false)
//    @Column(name = "currency")
	private Integer currencyId;

	public Integer getCurrencyId() {
		return currencyId;
	}
//    private Integer currency;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "term_atm_request__response", 
    		joinColumns = {@JoinColumn(name = "request")},
    		inverseJoinColumns = {@JoinColumn(name = "response")}
    		)
    @ForeignKey(name = "atmreq_request_fk", inverseName = "atmreq_response_fk")
    private Map<Integer, ATMResponse> responseMap;


    public ATMRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpkey() {
        return opkey;
    }

    public void setOpkey(String opkey) {
        this.opkey = opkey;
    }

    public IfxType getIfxType() {
        return ifxType;
    }

    public void setIfxType(IfxType ifxType) {
        this.ifxType = ifxType;
    }

    public TrnType getTrnType() {
        return trnType;
    }

    public void setTrnType(TrnType trnType) {
        this.trnType = trnType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Long getLongAmount(String bufferB, String bufferC) throws IOException {
        String realAmount = NDCParserUtils.getRealAmount(this.amount, bufferB, bufferC);
		return realAmount != null ? Long.parseLong(realAmount) : null;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public <T extends ATMResponse> T getAtmResponse(int responseCode) {
        ATMResponse response = responseMap.get(responseCode);
        return (T) response;
    }

    public void addAtmResponse(int responseCode, ATMResponse atmResponse) {
    	if (responseMap == null)
    		responseMap = new HashMap<Integer, ATMResponse>();
//        if (!responseMap.containsKey(responseCode)) {
        responseMap.put(responseCode, atmResponse);
//        }
    }

    public String getBufferB() {
        return bufferB;
    }

    public void setBufferB(String bufferB) {
        this.bufferB = bufferB;
    }

    public String getBufferC() {
        return bufferC;
    }

    public void setBufferC(String bufferC) {
        this.bufferC = bufferC;
    }

    public ATMConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ATMConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getRealAmount(ATMRequest atmRequest, NDCConsumerRequestMsg ndcConsumerMessage) {
        if (Ifx.getAmountPath().equals(atmRequest.getBufferB())) {
            return ndcConsumerMessage.generalBufferB;
        }
        if (Ifx.getAmountPath().equals(atmRequest.getBufferC())) {
            return ndcConsumerMessage.generalBufferC;
        }
        return ndcConsumerMessage.dollarAndCentsEntry;
    }

	public UserLanguage getLanguage() {
		return language;
	}

	public void setLanguage(UserLanguage language) {
		this.language = language;
	}

	public Integer getFit() {
		return fit;
	}

	public void setFit(Integer fit) {
		this.fit = fit;
	}

	public IfxType getSecondaryIfxType() {
		return secondaryIfxType;
	}

	public void setSecondaryIfxType(IfxType secondaryIfxType) {
		this.secondaryIfxType = secondaryIfxType;
	}

	public String getNextOpkey() {
		return nextOpkey;
	}

	public void setNextOpkey(String nextOpkey) {
		this.nextOpkey = nextOpkey;
	}

	public TrnType getSecondaryTrnType() {
		return secondaryTrnType;
	}

	public void setSecondaryTrnType(TrnType secondaryTrnType) {
		this.secondaryTrnType = secondaryTrnType;
	}

	public Boolean getForceReceipt() {
		return forceReceipt;
	}

	public void setForceReceipt(Boolean forceReceipt) {
		this.forceReceipt = forceReceipt;
	}

	public Map<Integer, ATMResponse> getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(Map<Integer, ATMResponse> responseMap) {
		this.responseMap = responseMap;
	}

	public String getExtraInformation() {
		return extraInformation;
	}

	public void setExtraInformation(String extraInformation) {
		this.extraInformation = extraInformation;
	}

	public String getExtraInformationIfxPath() {
		return extraInformationIfxPath;
	}

	public void setExtraInformationIfxPath(String extraInformationIfxPath) {
		this.extraInformationIfxPath = extraInformationIfxPath;
	}
	
	//TASK Task019 : Receipt Option
    public ReceiptOptionType getReceiptOption() {
		return receiptOption;
	}

	//TASK Task019 : Receipt Option
	public void setReceiptOption(ReceiptOptionType receiptOption) {
		this.receiptOption = receiptOption;
	}
	
}
