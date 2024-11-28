package vaulsys.protocols.base;

import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;

public interface IfxToProtocolMapper {

	public ProtocolMessage map(Ifx ifx, EncodingConvertor convertor) throws Exception; 
	
	public String mapError(IfxType type, String rsCode);
	
//	public EncodingConvertor getEncodingConvertor();
}