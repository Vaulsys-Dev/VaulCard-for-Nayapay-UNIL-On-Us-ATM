package vaulsys.protocols.ifx.enums;

import vaulsys.persistence.IEnum;

import java.util.HashMap;
import java.util.Map;
import vaulsys.protocols.exception.exception.*;

import javax.persistence.Embeddable;

@Embeddable
public class StatusCode implements IEnum{
	
	private static final int NotParsedBinaryToProtocolException_VALUE = 0;
	private static final int NotProducedProtocolToBinaryException_VALUE = 1;
	private static final int NotMappedIfxToProtocolException_VALUE = 2;
	private static final int NotMappedProtocolToIfxException_VALUE = 3;
	private static final int CantAddNecessaryDataToIfxException_VALUE = 4;
	private static final int CantPostProcessBinaryDataException_VALUE = 5;
	private static final int InvalidBusinessDateException_VALUE = 6;
	private static final int MacGenerationException_VALUE = 7;
	private static final int NotApplicableTypeMessageException_VALUE = 8;
	private static final int ReferenceTransactionNotFoundException_VALUE = 9;
	private static final int UncorrectMessageLengthException_VALUE = 10;
	
	public static final StatusCode NotParsedBinaryToProtocolException = new StatusCode(NotParsedBinaryToProtocolException_VALUE);
	public static final StatusCode NotProducedProtocolToBinaryException = new StatusCode(NotProducedProtocolToBinaryException_VALUE);
	public static final StatusCode NotMappedIfxToProtocolException = new StatusCode(NotMappedIfxToProtocolException_VALUE);
	public static final StatusCode NotMappedProtocolToIfxException = new StatusCode(NotMappedProtocolToIfxException_VALUE);
	public static final StatusCode CantAddNecessaryDataToIfxException = new StatusCode(CantAddNecessaryDataToIfxException_VALUE);
	public static final StatusCode CantPostProcessBinaryDataException = new StatusCode(CantPostProcessBinaryDataException_VALUE);
	public static final StatusCode InvalidBusinessDateException = new StatusCode(InvalidBusinessDateException_VALUE);
	public static final StatusCode MacGenerationException = new StatusCode(MacGenerationException_VALUE);
	public static final StatusCode NotApplicableTypeMessageException = new StatusCode(NotApplicableTypeMessageException_VALUE);
	public static final StatusCode ReferenceTransactionNotFoundException = new StatusCode(ReferenceTransactionNotFoundException_VALUE);
	public static final StatusCode UncorrectMessageLengthException = new StatusCode(UncorrectMessageLengthException_VALUE);
	
	public static final Map<StatusCode, Exception> exceptions = new HashMap<StatusCode, Exception>() {
		{
			put(NotParsedBinaryToProtocolException, new NotParsedBinaryToProtocolException());
			put( NotProducedProtocolToBinaryException , new NotProducedProtocolToBinaryException());
			put( NotMappedIfxToProtocolException , new NotMappedIfxToProtocolException());
			put( NotMappedProtocolToIfxException , new NotMappedProtocolToIfxException());
			put( CantAddNecessaryDataToIfxException , new CantAddNecessaryDataToIfxException());
			put( CantPostProcessBinaryDataException , new CantPostProcessBinaryDataException());
			put( InvalidBusinessDateException , new InvalidBusinessDateException());
			put( MacGenerationException , new MacGenerationException());
			put( NotApplicableTypeMessageException , new NotApplicableTypeMessageException());
			put( ReferenceTransactionNotFoundException , new ReferenceTransactionNotFoundException());
			put( UncorrectMessageLengthException , new UncorrectMessageLengthException());
		}
	};
	
	int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public StatusCode(int type) {
		super();
		this.type = type;
	}
	
	public StatusCode() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		StatusCode that = (StatusCode) obj;
		return type == that.type;
	}
	
	@Override
	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new StatusCode(this.type); 
	}
	
	public StatusCode copy() {
		return (StatusCode) clone();
	}

}
