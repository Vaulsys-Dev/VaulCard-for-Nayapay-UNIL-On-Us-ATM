package vaulsys.terminal.atm.customizationdata;

import vaulsys.protocols.ndc.encoding.VaulsysNDCConvertor;
import vaulsys.protocols.ndc.encoding.NDCConvertor;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.constants.ATMCustomizationDataType;
import vaulsys.util.StringFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "SCREEN")
public class ScreenData extends ATMCustomizationData {

	@ManyToOne
    @JoinColumn(name = "screen_atmconfig")
    @ForeignKey(name="screen_atmconfig_fk")
    private ATMConfiguration atmConfiguration;

	public ATMConfiguration getAtmConfiguration() {
		return atmConfiguration;
	}


	public void setAtmConfiguration(ATMConfiguration atmConfiguration) {
		this.atmConfiguration = atmConfiguration;
	}

	@Override
	public ATMCustomizationDataType getType() {
		return ATMCustomizationDataType.SCREEN;
	}
	
	@Override
	public byte[] getValueForDownload() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		StringFormat format = new StringFormat(3, StringFormat.JUST_RIGHT);
		NDCConvertor convertor = new VaulsysNDCConvertor();
		try {
			out.write(StringFormat.formatNew(3, StringFormat.JUST_RIGHT, getNumber(), '0').getBytes());
			out.write(convertor.convert(null, getValue(), null));
		} catch (IOException e) {
			logger.error("Exception in convert number: " + getNumber() + " or value: " + getValue());
			logger.error(e);
		}
		return out.toByteArray();
	}
	
}
