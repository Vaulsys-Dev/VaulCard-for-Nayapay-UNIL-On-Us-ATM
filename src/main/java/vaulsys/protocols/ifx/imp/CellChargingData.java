package vaulsys.protocols.ifx.imp;

import vaulsys.mtn.MTNCharge;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.IEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "ifx_cell_charge_data")
public class CellChargingData implements IEntity<Long>, Cloneable {
	
    @Id
//    @GeneratedValue(generator="switch-gen")
    @GeneratedValue(generator="cellchargingdata-seq-gen")
    @org.hibernate.annotations.GenericGenerator(name = "cellchargingdata-seq-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    		parameters = {
    			@org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
    			@org.hibernate.annotations.Parameter(name = "increment_size", value = "100"),
    			@org.hibernate.annotations.Parameter(name = "sequence_name", value = "cellchargingdata_seq")
    				})
    Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="charge", nullable = true)
	@Cascade(value = { CascadeType.ALL })
	@ForeignKey(name="cellchargedata_charge_fk")
	private MTNCharge charge;

//	private String cardPIN;
	
	private Integer chargeCompanyCode;
	
	 @Embedded
	    @AttributeOverrides({
	    	@AttributeOverride(name = "type", column = @Column(name = "charge_state_prv")) 
	    })
	private MTNChargeState chargeStatePrv;
	
	 @Embedded
	 @AttributeOverrides({
		 @AttributeOverride(name = "type", column = @Column(name = "charge_state_nxt")) 
	 })
	 private MTNChargeState chargeStateNxt;
	 
	public CellChargingData() {
	}

	
	
	public CellChargingData(MTNCharge charge/*, String cardPIN*/, MTNChargeState prvState, MTNChargeState nxtState) {
		this.charge = charge;
//		this.cardPIN = cardPIN;
		this.chargeStatePrv = prvState;
		this.chargeStateNxt = nxtState;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	protected Object clone() {
		CellChargingData obj = new CellChargingData();
		obj.setCharge(charge);
//		obj.setCardPIN(cardPIN);
		obj.setChargeCompanyCode(chargeCompanyCode);
		obj.setChargeStatePrv(chargeStatePrv);
		obj.setChargeStateNxt(chargeStateNxt);
		return obj;
	}

	public CellChargingData copy() {
		return (CellChargingData) clone();
	}

	public void copyFields(CellChargingData source) {
		
		if(charge == null)
			charge = source.getCharge();
		
//		if (cardPIN == null || cardPIN.trim().isEmpty())
//			cardPIN = source.getCardPIN();

		if (chargeCompanyCode == null)
			chargeCompanyCode = source.getChargeCompanyCode();
		
		chargeStatePrv = source.getChargeStatePrv();
		chargeStateNxt = source.getChargeStateNxt();
	}

//	@Transient
//	public String getCardPIN() {
//		return charge.getCardPIN();
//	}

//	public void setCardPIN(String cardPIN) {
//		this.charge.setCardPIN(cardPIN);
//	}
//

	public MTNCharge getCharge() {
		return charge;
	}

	public void setCharge(MTNCharge charge) {
		this.charge = charge;
	}

	public Integer getChargeCompanyCode() {
		return chargeCompanyCode;
	}

	public void setChargeCompanyCode(Integer chargeCompanyCode) {
		this.chargeCompanyCode = chargeCompanyCode;
	}

	public MTNChargeState getChargeStatePrv() {
		return chargeStatePrv;
	}

	public void setChargeStatePrv(MTNChargeState chargeStatePrv) {
		this.chargeStatePrv = chargeStatePrv;
	}

	public MTNChargeState getChargeStateNxt() {
		return chargeStateNxt;
	}

	public void setChargeStateNxt(MTNChargeState chargeStateNxt) {
		this.chargeStateNxt = chargeStateNxt;
	}

}
