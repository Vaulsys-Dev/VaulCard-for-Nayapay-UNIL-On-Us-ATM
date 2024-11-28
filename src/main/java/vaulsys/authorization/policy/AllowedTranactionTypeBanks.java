package vaulsys.authorization.policy;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TrnType;

import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "auth_plc_alw_trx_typ_bnks")
public class AllowedTranactionTypeBanks implements IEntity<Integer>, Cloneable {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Integer id;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "type", column = @Column(name = "trx_type"))
    })
    private TrnType type;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "auth_plc_alw_trx__bnk", 
    		joinColumns = {@JoinColumn(name = "plc_alw_trx_typ")}, 
    		inverseJoinColumns = {@JoinColumn(name = "bank")}
    )
    @ForeignKey(name = "plc_alwtrx_typ_bnk_fk", inverseName = "plc_alwtrx_typ_fk")
    private List<Bank> banks;


    public AllowedTranactionTypeBanks() {
    }

    protected AllowedTranactionTypeBanks clone() {
        AllowedTranactionTypeBanks type = new AllowedTranactionTypeBanks();
        type.setBanks(new ArrayList<Bank> (this.banks));
        type.setType(this.type);
        return type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	public TrnType getType() {
		return type;
	}

	public void setType(TrnType type) {
		this.type = type;
	}

	public List<Bank> getBanks() {
		return banks;
	}

	public void setBanks(List<Bank> banks) {
		this.banks = banks;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AllowedTranactionTypeBanks))
			return false;
		AllowedTranactionTypeBanks other = (AllowedTranactionTypeBanks) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId())){
			return false;
		}
		return true;
	}

}
