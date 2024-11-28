package vaulsys.terminal.atm;

import vaulsys.persistence.IEntity;
import vaulsys.util.StringFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "term_atm_dispense")
public class Dispense implements IEntity<Integer> {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Integer id;
    private String cassette1;
    private String cassette2;
    private String cassette3;
    private String cassette4;
    private String cassette5;
    private String cassette6;

    public Dispense() {
    }

    public Dispense(String cassette1, String cassette2, String cassette3, String cassette4) {
        this.cassette1 = cassette1;
        this.cassette2 = cassette2;
        this.cassette3 = cassette3;
        this.cassette4 = cassette4;
    }

    public Dispense(String cassette1, String cassette2, String cassette3, String cassette4, String cassette5, String cassette6) {
        this.cassette1 = cassette1;
        this.cassette2 = cassette2;
        this.cassette3 = cassette3;
        this.cassette4 = cassette4;
        this.cassette5 = cassette5;
        this.cassette6 = cassette6;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCassette1() {
        return cassette1;
    }

    public void setCassette1(String cassette1) {
        this.cassette1 = cassette1;
    }

    public String getCassette2() {
        return cassette2;
    }

    public void setCassette2(String cassette2) {
        this.cassette2 = cassette2;
    }

    public String getCassette3() {
        return cassette3;
    }

    public void setCassette3(String cassette3) {
        this.cassette3 = cassette3;
    }

    public String getCassette4() {
        return cassette4;
    }

    public void setCassette4(String cassette4) {
        this.cassette4 = cassette4;
    }

    public String getCassette5() {
        return cassette5;
    }

    public void setCassette5(String cassette5) {
        this.cassette5 = cassette5;
    }

    public String getCassette6() {
        return cassette6;
    }

    public void setCassette6(String cassette6) {
        this.cassette6 = cassette6;
    }

    public String getAllCassettesAsByte() {
        String out = "";
//        StringFormat format = new StringFormat(2, StringFormat.JUST_RIGHT);

        if (cassette1 == null && cassette2 == null && cassette3 == null && cassette4 == null)
        	return null;
        
        out += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, cassette1 != null ? cassette1 : "0", '0');
        out += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, cassette2 != null ? cassette2 : "0", '0');
        out += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, cassette3 != null ? cassette3 : "0", '0');
        out += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, cassette4 != null ? cassette4 : "0", '0');
        if (cassette5 != null && !cassette5.equals("")) {
            out += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, cassette5, '0');
            out += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, cassette6, '0');
        }

        return out;
    }
    
    @Override
    public String toString() {
    	return id==null ? "":id.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Dispense))
			return false;
		Dispense other = (Dispense) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
