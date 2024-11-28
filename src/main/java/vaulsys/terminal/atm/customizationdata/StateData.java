package vaulsys.terminal.atm.customizationdata;

import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.constants.ATMCustomizationDataType;
import vaulsys.util.StringFormat;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "STATE")
public class StateData extends ATMCustomizationData {
	@ManyToOne
    @JoinColumn(name = "state_atmconfig")
    @ForeignKey(name="state_atmconfig_fk")
    private ATMConfiguration atmConfiguration;

	public ATMConfiguration getAtmConfiguration() {
		return atmConfiguration;
	}


	public void setAtmConfiguration(ATMConfiguration atmConfiguration) {
		this.atmConfiguration = atmConfiguration;
	}

	@Override
	public ATMCustomizationDataType getType() {
		return ATMCustomizationDataType.STATE;
	}

	@Override
	public byte[] getValueForDownload() {
//		StringFormat format = new StringFormat(3, StringFormat.JUST_RIGHT);
		String out = "";

		out += StringFormat.formatNew(3, StringFormat.JUST_RIGHT, getNumber(), '0');
		out += getValue();

		return out.getBytes();
	}
	
}
