package vaulsys.authorization.policy;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TrnType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "auth_plc_bank")
public class Bank implements IEntity<Integer>, Cloneable {

	@Id
	// @GeneratedValue(generator="switch-gen")
	// private Integer id;
	private Integer bin;

	private String name;

	private String nameEn;

	//Raza NayaPay start
	private String bankcode;

	private String shortname;

	@Column(name="ACCOUNTNUMFORMAT")
	private String accountformat;
	//Raza NayaPay end

	public String getNameEn() {
		return nameEn;
	}
	
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}


	private Integer twoDigitCode;
	@CollectionOfElements(fetch = FetchType.LAZY)
    @Enumerated
    @JoinTable(
            name = "AUTH_PLC_RULE_TRNTYPE"
            // joinColumns = @JoinColumn(name = "id")
    )
    @ForeignKey(name = "auth_trnType_bank_fk")
    private List<TrnType> trnType;
	public Bank() {
	}
	public Integer getId() {
		return bin;
	}

	public void setId(Integer id) {
		this.bin = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getBin() {
		return bin;
	}

	public void setBin(Integer bin) {
		this.bin = bin;
	}

	public Integer getTwoDigitCode() {
		return twoDigitCode;
	}

	public void setTwoDigitCode(Integer twoDigitCode) {
		this.twoDigitCode = twoDigitCode;
	}
	
	 public List<TrnType> getTrnType() {
        return trnType;
    }

    public void setTrnType(List<TrnType> trnType) {
        this.trnType = trnType;
    }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bin == null) ? 0 : bin.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Bank))
			return false;
		Bank other = (Bank) obj;
		if (getBin() == null) {
			if (other.getBin() != null)
				return false;
		} else if (!getBin().equals(other.getBin())){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", name, bin);
	}

	public String getBankcode() {
		return bankcode;
	}

	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getAccountformat() {
		return accountformat;
	}

	public void setAccountformat(String accountformat) {
		this.accountformat = accountformat;
	}
}
