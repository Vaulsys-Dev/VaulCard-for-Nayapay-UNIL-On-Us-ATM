package vaulsys.terminal.atm.currencyatm;

import java.util.List;

import vaulsys.authorization.policy.Bank;
import vaulsys.customer.Currency;
import vaulsys.persistence.IEntity;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.impl.ATMTerminal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "term_atm_currency")
public class ATMTerminalCurrency implements IEntity<Long> {

	
	@Id
//  @GeneratedValue(generator="switch-gen")
  @GeneratedValue(generator="atmcur-seq-gen")
  @org.hibernate.annotations.GenericGenerator(name = "atmcur-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
  		parameters = {
  			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
  			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
  			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "atmcur_seq")
  				})
	Long id;
	
	
//	@OneToMany(fetch = FetchType.LAZY)
//    @JoinTable(name = "term_atm__currency", 
//    		joinColumns = {@JoinColumn(name = "atm_terminal_currency")}, 
//    		inverseJoinColumns = {@JoinColumn(name = "atm_terminal")}
//    )
//    @ForeignKey(name = "atm_term_currency_atm_fk", inverseName = "atm_term_currency_fk")
//    private List<ATMTerminal> atmTerminals;
	
	@Column(name="a_denom")
    private Integer cassetteADenomination;
    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "a_curr")
	private Currency cassetteACurrency;
    
    @Column(name = "a_curr", insertable = false, updatable = false)
	private Long cassetteACurrencyId;

//	@Column(name="a_curr")
//    private Integer cassetteACurrency;
    
    @Column(name="b_denom")
    private Integer cassetteBDenomination;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "b_curr")
	private Currency cassetteBCurrency;
    
    @Column(name = "b_curr", insertable = false, updatable = false)
	private Long cassetteBCurrencyId;

    @Column(name="c_denom")
    private Integer cassetteCDenomination;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "c_curr")
	private Currency cassetteCCurrency;
    
    @Column(name = "c_curr", insertable = false, updatable = false)
	private Long cassetteCCurrencyId;
    
    @Column(name="d_denom")
    private Integer cassetteDDenomination;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "d_curr")
	private Currency cassetteDCurrency;
    
    @Column(name = "d_curr", insertable = false, updatable = false)
	private Long cassetteDCurrencyId;
    
    @Column(name="cur_rate")
    private String curRate;
    
    @Column(name="Cur_name_farsi")
    private String CurNameFa;
    
    
    @Column(name="Cur_name_englis")
    private String CurNameEn;
    
    
    @Column(name="A_Key_amt")
    private String keyAmountA;
    
    @Column(name="B_Key_amt")
    private String keyAmountB;
    
    @Column(name="C_Key_amt")
    private String keyAmountC;
    
    @Column(name="D_Key_amt")
    private String keyAmountD;
    
    @Column(name="F_Key_amt")
    private String keyAmountF;
    
    @Column(name="G_Key_amt")
    private String keyAmountG;
    
    @Column(name="H_Key_amt")
    private String keyAmountH;
    
    @Column(name="I_Key_amt")
    private String keyAmountI;
    
    
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
//	public List<ATMTerminal> getAtmTerminals() {
//		return atmTerminals;
//	}
//
//	public void setAtmTerminals(List<ATMTerminal> atmTerminals) {
//		this.atmTerminals = atmTerminals;
//	}
//
//	public Long getCassetteACurrencyId() {
//		return cassetteACurrencyId;
//	}
	
	public Long getCassetteBCurrencyId() {
		return cassetteBCurrencyId;
	}
	
	public Long getCassetteCCurrencyId() {
		return cassetteCCurrencyId;
	}
	
	public Long getCassetteDCurrencyId() {
		return cassetteDCurrencyId;
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
	
	public String getCurRate() {
		return curRate;
	}

	public void setCurRate(String curRate) {
		this.curRate = curRate;
	}

	public String getCurNameFa() {
		return CurNameFa;
	}

	public void setCurNameFa(String curNameFa) {
		CurNameFa = curNameFa;
	}

	public String getCurNameEn() {
		return CurNameEn;
	}
	
	public void setCurNameEn(String curNameEn) {
		CurNameEn = curNameEn;
	}

	public String getKeyAmountA() {
		return keyAmountA;
	}

	public void setKeyAmountA(String keyAmountA) {
		this.keyAmountA = keyAmountA;
	}

	public String getKeyAmountB() {
		return keyAmountB;
	}

	public void setKeyAmountB(String keyAmountB) {
		this.keyAmountB = keyAmountB;
	}

	public String getKeyAmountC() {
		return keyAmountC;
	}

	public void setKeyAmountC(String keyAmountC) {
		this.keyAmountC = keyAmountC;
	}

	public String getKeyAmountD() {
		return keyAmountD;
	}

	public void setKeyAmountD(String keyAmountD) {
		this.keyAmountD = keyAmountD;
	}

	public String getKeyAmountF() {
		return keyAmountF;
	}

	public void setKeyAmountF(String keyAmountF) {
		this.keyAmountF = keyAmountF;
	}

	public String getKeyAmountG() {
		return keyAmountG;
	}

	public void setKeyAmountG(String keyAmountG) {
		this.keyAmountG = keyAmountG;
	}

	public String getKeyAmountH() {
		return keyAmountH;
	}

	public void setKeyAmountH(String keyAmountH) {
		this.keyAmountH = keyAmountH;
	}

	public String getKeyAmountI() {
		return keyAmountI;
	}

	public void setKeyAmountI(String keyAmountI) {
		this.keyAmountI = keyAmountI;
	}
	
	
//	@Override
//	public String toString(){
//		return String.format("%s, %s", name != null ? name:"-", id);
//	}
	
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
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ATMTerminalCurrency))
			return false;
		ATMTerminalCurrency other = (ATMTerminalCurrency) obj;
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
		
		if (curRate == null) {
			if (other.curRate != null)
				return false;
		} else if (!curRate.equals(other.curRate))
			return false;
		
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (CurNameFa == null) {
			if (other.CurNameFa != null)
				return false;
		} else if (!CurNameFa.equals(other.CurNameFa))
			return false;
		
		if (CurNameEn == null) {
			if (other.CurNameEn != null)
				return false;
		} else if (!CurNameEn.equals(other.CurNameEn))
			return false;
		
		if (keyAmountA == null) {
			if (other.keyAmountA != null)
				return false;
		} else if (!keyAmountA.equals(other.keyAmountA))
			return false;
		if (keyAmountB == null) {
			if (other.keyAmountB != null)
				return false;
		} else if (!keyAmountB.equals(other.keyAmountB))
			return false;
		if (keyAmountC == null) {
			if (other.keyAmountC != null)
				return false;
		} else if (!keyAmountC.equals(other.keyAmountC))
			return false;
		if (keyAmountD == null) {
			if (other.keyAmountD != null)
				return false;
		} else if (!keyAmountD.equals(other.keyAmountD))
			return false;
		if (keyAmountF == null) {
			if (other.keyAmountF != null)
				return false;
		} else if (!keyAmountF.equals(other.keyAmountF))
			return false;
		if (keyAmountG == null) {
			if (other.keyAmountG != null)
				return false;
		} else if (!keyAmountG.equals(other.keyAmountG))
			return false;
		if (keyAmountH == null) {
			if (other.keyAmountH != null)
				return false;
		} else if (!keyAmountH.equals(other.keyAmountH))
			return false;
		if (keyAmountI == null) {
			if (other.keyAmountI != null)
				return false;
		} else if (!keyAmountI.equals(other.keyAmountI))
			return false;
		
		
		return true;
	}
	
	public String getAmountFromKey(Integer keyValue) {
		switch (keyValue) {
		case 1:
			return keyAmountA;
		case 2: 
			return keyAmountB;
		case 3:
			return keyAmountC;
		case 4:
			return keyAmountG;
		case 5: 
			return keyAmountH;
		case 6:
			return keyAmountI;
			
		default:
			return "0";
		}
	}
	
}
