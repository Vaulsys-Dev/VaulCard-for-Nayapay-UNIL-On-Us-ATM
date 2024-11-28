package vaulsys.protocols.apacs70.base;

import org.apache.log4j.Logger;

import vaulsys.protocols.base.ProtocolMessage;

public abstract class BaseMsg implements ProtocolMessage {
	protected static final Logger logger = Logger.getLogger(BaseMsg.class);

	public Integer dialIndicator;
	public Long    terminalIdentity;
	public Integer messageNumber;
	public String  messageType;
	public String  MAC;
	
	public AuxiliaryDataComponent auxiliaryData;

	protected abstract void msgString(StringBuilder builder);
	
	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Terminal Code: ").append(terminalIdentity);
		builder.append("\nSequence Number: ").append(messageNumber);
		builder.append("\nType: ").append(ApacsMsgType.toString(messageType));
		msgString(builder);
		builder.append("\nMAC: ").append(MAC);
		return builder.toString();
	}
}
