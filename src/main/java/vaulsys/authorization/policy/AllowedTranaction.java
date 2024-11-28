package vaulsys.authorization.policy;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TrnType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "auth_plc_alw_trx")
public class AllowedTranaction implements IEntity<Integer>, Cloneable {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bank")
	@ForeignKey(name="auth_plc_alwd_trx_bnk_fk")
	private Bank bank;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "trntype"))
    })
    private TrnType trnType;
    
    @Column(name = "min_amount")
    private Long minAmount;
    
    @Column(name = "max_amount")
    private Long maxAmount;

    public AllowedTranaction() {
    }

    protected AllowedTranaction clone() {
        AllowedTranaction type = new AllowedTranaction();
        type.setBank(bank);
        type.setMinAmount(minAmount);
        type.setMaxAmount(maxAmount);
        type.setTrnType(trnType);
        return type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	
	public Long getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(Long minAmount) {
		this.minAmount = minAmount;
	}

	public Long getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(Long maxAmount) {
		this.maxAmount = maxAmount;
	}

	public TrnType getTrnType() {
		return trnType;
	}

	public void setTrnType(TrnType trnType) {
		this.trnType = trnType;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AllowedTranaction))
			return false;
		AllowedTranaction other = (AllowedTranaction) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId())){
			return false;
		}
		return true;
	}
}
