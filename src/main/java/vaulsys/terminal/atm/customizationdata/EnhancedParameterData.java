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
@DiscriminatorValue(value = "PARAM")
public class EnhancedParameterData extends ATMCustomizationData {

	@ManyToOne
    @JoinColumn(name = "param_atmconfig")
    @ForeignKey(name="param_atmconfig_fk")
    private ATMConfiguration atmConfiguration;

	public ATMConfiguration getAtmConfiguration() {
		return atmConfiguration;
	}


	public void setAtmConfiguration(ATMConfiguration atmConfiguration) {
		this.atmConfiguration = atmConfiguration;
	}

	@Override
	public ATMCustomizationDataType getType() {
		return ATMCustomizationDataType.PARAMETER;
	}

	@Override
	public byte[] getValueForDownload() {
//		StringFormat format2 = new StringFormat(2, StringFormat.JUST_RIGHT);
//		StringFormat format3 = new StringFormat(3, StringFormat.JUST_RIGHT);
		String out = "";

		out += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, getNumber(), '0');
		out += StringFormat.formatNew(3, StringFormat.JUST_RIGHT, getValue(), '0');

		return out.getBytes();
	}
	
}
