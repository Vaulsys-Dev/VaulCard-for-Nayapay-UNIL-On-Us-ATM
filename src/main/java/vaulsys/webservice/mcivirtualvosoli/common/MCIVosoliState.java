package vaulsys.webservice.mcivirtualvosoli.common;

import javax.persistence.Embeddable;

import vaulsys.persistence.IEnum;
import vaulsys.terminal.atm.ATMProducer;

@Embeddable
public class MCIVosoliState implements IEnum{
	
	
	private static final int SUCCESS_VALUE = 0;
	private static final int NOT_SEND_VALUE = 1;
	private static final int SENDING_VALUE = 2;
	private static final int NO_ANSWER_VALUE = 3;
	private static final int INVALID_BANK_CODE_VALUE = 8;
	private static final int INVALID_SEND_DATE_VALUE = 9;
	private static final int INVALID_BRANCH_CODE_VALUE = 21;
	private static final int INVALID_BILLID_LENGTH_VALUE = 22;
	private static final int INVALID_PAY_DATE_VALUE = 23;
	private static final int INVALID_CHANNEL_TYPE_VALUE = 24;
	private static final int INVALID_COMPANY_CODE_VALUE = 25;
	private static final int ZERO_STARTED_NUM_VALUE = 26;
	private static final int INVALID_TERM_CODE_VALUE = 27;
	private static final int INVALID_BILLID_CHECKDIGIT_VALUE = 28;
	private static final int INVALID_BILLPAYMENT_CHECKDIGIT_FIRST_VALUE = 29;
	private static final int INVALID_BILLPATMENT_CHECKdIGIT_SECOND_VALUE =30;
	private static final int INVALID_BILLPAYMENT_VOSOLIDIGIT_TYPE_VALUE = 33;
	private static final int REPEATED_RECORD_VALUE = -1;
	private static final int SYSTEM_ERROR_VALUE = -2;
	
	
	
	public static final MCIVosoliState SUCCESS = new MCIVosoliState(SUCCESS_VALUE);
	public static final MCIVosoliState NOT_SEND = new MCIVosoliState(NOT_SEND_VALUE);
	public static final MCIVosoliState SENDING = new MCIVosoliState(SENDING_VALUE);
	public static final MCIVosoliState NO_ANSWER = new MCIVosoliState(NO_ANSWER_VALUE);
	public static final MCIVosoliState INVALID_BANK_CODE = new MCIVosoliState(INVALID_BANK_CODE_VALUE);
	public static final MCIVosoliState INVALID_SEND_DATE = new MCIVosoliState(INVALID_SEND_DATE_VALUE);
	public static final MCIVosoliState INVALID_BRANCH_CODE = new MCIVosoliState(INVALID_BRANCH_CODE_VALUE);
	public static final MCIVosoliState INVALID_BILLID_LENGTH = new MCIVosoliState(INVALID_BILLID_LENGTH_VALUE);
	public static final MCIVosoliState INVALID_PAY_DATE = new MCIVosoliState(INVALID_PAY_DATE_VALUE);
	public static final MCIVosoliState INVALID_CHANNEL_TYPE = new MCIVosoliState(INVALID_CHANNEL_TYPE_VALUE);
	public static final MCIVosoliState INVALID_COMPANY_CODE = new MCIVosoliState(INVALID_COMPANY_CODE_VALUE);
	public static final MCIVosoliState ZERO_STARTED_NUM = new MCIVosoliState(ZERO_STARTED_NUM_VALUE);
	public static final MCIVosoliState INVALID_TERM_CODE = new MCIVosoliState(INVALID_TERM_CODE_VALUE);
	public static final MCIVosoliState INVALID_BILLID_CHECKDIGIT = new MCIVosoliState(INVALID_BILLID_CHECKDIGIT_VALUE);
	public static final MCIVosoliState INVALID_BILLPAYMENT_CHECKDIGIT_FIRST = new MCIVosoliState(INVALID_BILLPAYMENT_CHECKDIGIT_FIRST_VALUE);
	public static final MCIVosoliState INVALID_BILLPATMENT_CHECKdIGIT_SECOND = new MCIVosoliState(INVALID_BILLPATMENT_CHECKdIGIT_SECOND_VALUE);
	public static final MCIVosoliState INVALID_BILLPAYMENT_VOSOLIDIGIT_TYPE = new MCIVosoliState(INVALID_BILLPAYMENT_VOSOLIDIGIT_TYPE_VALUE);
	public static final MCIVosoliState REPEATED_RECORD = new MCIVosoliState(REPEATED_RECORD_VALUE);
	public static final MCIVosoliState SYSTEM_ERROR = new MCIVosoliState(SYSTEM_ERROR_VALUE);

	
	private Integer state;
	
	public MCIVosoliState(){}
	
	public MCIVosoliState(int state){
		this.state = state;		
	}
	
	public Integer getState() {
		return state;
	}
	
	public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof MCIVosoliState)) return false;

        MCIVosoliState that = (MCIVosoliState) o;

        return state.equals(that.state);
    }

    public int hashCode() {
        return state;
    }
	
	@Override
	public String toString() {
		return state + "" ;
	}
	
	public boolean isAbleToBeReversed(){
		if(SUCCESS.equals(this) || SENDING.equals(this) || REPEATED_RECORD.equals(this))
			return false;
		return true;
	}
}
