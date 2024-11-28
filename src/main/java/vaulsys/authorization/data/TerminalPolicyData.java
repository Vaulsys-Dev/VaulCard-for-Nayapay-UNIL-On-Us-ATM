package vaulsys.authorization.data;

import vaulsys.terminal.impl.Terminal;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.MapKeyManyToMany;

@Entity
@DiscriminatorValue(value = "terminalpolicy")
public class TerminalPolicyData extends PolicyData {

    @ManyToMany(cascade = CascadeType.ALL)
    @MapKeyManyToMany(joinColumns={@JoinColumn(name="terminal")})
    @JoinTable(name = "auth_plc_trmplcdt_trmdt", 
    		joinColumns = {@JoinColumn(name = "plc_data")},
    		inverseJoinColumns = {@JoinColumn(name = "terminal_data")}
    		)
    @ForeignKey(name = "termplcdata_plcdata_fk", inverseName = "termplcdata_termdata_fk")
    Map<Terminal, TerminalData> termianlData;

    public TerminalPolicyData() {
    }

    public Map<Terminal, TerminalData> getTermianlData() {
        if (termianlData == null)
            termianlData = new HashMap<Terminal, TerminalData>(1);
        return termianlData;
    }

    public void setTermianlData(Map<Terminal, TerminalData> termianlData) {
        this.termianlData = termianlData;
    }

}
