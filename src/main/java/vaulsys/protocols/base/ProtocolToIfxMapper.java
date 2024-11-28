package vaulsys.protocols.base;

import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.imp.Ifx;

public interface ProtocolToIfxMapper {
	 public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception;
	 
	 public String mapError(String rsCode);
}
