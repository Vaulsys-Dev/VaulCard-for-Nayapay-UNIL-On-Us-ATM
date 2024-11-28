package vaulsys.protocols.fnsPep;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.shetab87.Shetab87ISOToIFXMapper;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class FnsPepISOToIFXMapper extends Shetab87ISOToIFXMapper {
	
	public static final FnsPepISOToIFXMapper Instance = new FnsPepISOToIFXMapper();
	
	private FnsPepISOToIFXMapper(){super();}
	
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
		Ifx ifx = super.map(message, convertor);
		if (ifx!= null){
//			 Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
			 String myBin = ""+ProcessContext.get().getMyInstitution().getBin();
			 ifx.setBankId (myBin);
		}
		return ifx;
	}
	
	@Override
	public void mapFieldANFix(Ifx ifx, ISOMsg isoMsg, int fieldId) {
		String fieldData = isoMsg.getString(fieldId);
		if (!Util.hasText(fieldData))
			return;
		if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
//				&& GlobalContext.getInstance().getMyInstitution().getBin().equals(ifx.getBankId())) {
				&& ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())) {
			if (fieldId == 37) {
				ifx.setNetworkRefId(fieldData.trim());
			}
			if (fieldId == 41) {
				ifx.setTerminalId(fieldData.trim());
			}
			if (fieldId == 42) {
				ifx.setOrgIdNum(fieldData.trim());
			}
		} else {
			if (fieldId == 37) {
				ifx.setNetworkRefId(fieldData.trim());
			}
			if (fieldId == 41) {
				ifx.setTerminalId(fieldData.trim());
			}
			if (fieldId == 42) {
				ifx.setOrgIdNum(fieldData.trim());
			}
		}
	}
}
