package vaulsys.protocols.base;

import java.io.Serializable;

public interface ProtocolMessage extends Serializable {

	Boolean isRequest() throws Exception;
}
