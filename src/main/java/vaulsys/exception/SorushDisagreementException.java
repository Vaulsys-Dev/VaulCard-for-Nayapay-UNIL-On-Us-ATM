package vaulsys.exception;

import vaulsys.exception.base.SwitchBusinessException;

//TASK Task103 : Resalat Sorush Disagreement (new feild in sorush disagreemnt)
public class SorushDisagreementException extends SwitchBusinessException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SorushDisagreementException(String message){
		super(message);
	}
}
